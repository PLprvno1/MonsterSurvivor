"""Read-only asset metadata/PCM audit. Uses only stdlib, JDK and already cached project decoder jars.
Does not install software, play, modify, normalize, transcode or copy audio into runtime.
"""
from pathlib import Path
import csv,hashlib,json,os,struct,subprocess,collections

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'asset_sources/audio'
OUT=ROOT/'core/build/reports/task05_1a'
EXTENSIONS={'.ogg','.mp3','.wav','.flac','.aac','.m4a','.opus','.aif','.aiff','.wma','.mid','.midi','.mod','.xm'}
CRC_TABLE=[]
for value in range(256):
    crc=value<<24
    for _ in range(8):crc=((crc<<1)^(0x04C11DB7 if crc&0x80000000 else 0))&0xffffffff
    CRC_TABLE.append(crc)

def ogg_metadata(data):
    at=0;streams={};errors=[];pages=0
    while at<len(data):
        if data[at:at+4]!=b'OggS' or at+27>len(data):
            errors.append('OGG_PAGE_TRUNCATED_OR_BAD_CAPTURE');break
        count=data[at+26];header_end=at+27+count
        segments=data[at+27:header_end];end=header_end+sum(segments)
        if len(segments)!=count or end>len(data):errors.append('OGG_TRUNCATED_BODY');break
        page=bytearray(data[at:end]);expected=struct.unpack_from('<I',page,22)[0];page[22:26]=b'\0'*4;crc=0
        for byte in page:crc=((crc<<8)&0xffffffff)^CRC_TABLE[((crc>>24)^byte)&255]
        if crc!=expected:errors.append('OGG_CRC_MISMATCH')
        flags=data[at+5];granule=struct.unpack_from('<q',data,at+6)[0];serial,sequence=struct.unpack_from('<II',data,at+14)
        state=streams.setdefault(serial,{'packet':bytearray(),'headers':[],'sequence':-1,'granule':0,'eos':False})
        if state['sequence']+1!=sequence:errors.append('OGG_SEQUENCE_GAP')
        state['sequence']=sequence;state['eos']=bool(flags&4);state['granule']=max(state['granule'],granule)
        cursor=header_end
        for n in segments:
            if len(state['headers'])<3:state['packet'].extend(data[cursor:cursor+n])
            cursor+=n
            if n<255 and len(state['headers'])<3:state['headers'].append(bytes(state['packet']));state['packet'].clear()
        at=end;pages+=1
    meta={'oggPages':pages,'containerErrors':sorted(set(errors)),'streams':len(streams)}
    if len(streams)!=1:return meta
    state=next(iter(streams.values()));headers=state['headers']
    if not state['eos']:meta['containerErrors'].append('OGG_MISSING_EOS')
    if not headers or not headers[0].startswith(b'\x01vorbis'):meta['containerErrors'].append('NOT_VORBIS');return meta
    _,channels,rate,maximum,nominal,minimum=struct.unpack_from('<IBIiii',headers[0],7)
    meta.update(codec='Vorbis',sampleRate=rate,channels=channels,granule=state['granule'],duration=state['granule']/rate,nominalBitrate=nominal if nominal>0 else None)
    tags={}
    if len(headers)>1 and headers[1].startswith(b'\x03vorbis'):
        packet=headers[1];offset=7;length=struct.unpack_from('<I',packet,offset)[0];offset+=4;meta['vendor']=packet[offset:offset+length].decode('utf-8','replace');offset+=length
        total=struct.unpack_from('<I',packet,offset)[0];offset+=4
        for _ in range(total):
            length=struct.unpack_from('<I',packet,offset)[0];offset+=4;comment=packet[offset:offset+length].decode('utf-8','replace');offset+=length
            key,sep,value=comment.partition('=');tags.setdefault(key,[]).append(value if sep else comment)
    meta['tags']=tags
    return meta

def mp3_tags(data):
    tags={};meta={}
    if data[:3]==b'ID3' and len(data)>=10:
        version=data[3];size=sum((data[6+i]&127)<<(7*(3-i)) for i in range(4));at=10
        meta.update(id3Version=version,id3Bytes=size+10)
        if version in [3,4]:
            while at+10<=min(len(data),size+10):
                key=data[at:at+4].decode('ascii','replace')
                if not key.strip('\0'):break
                raw=data[at+4:at+8];n=int.from_bytes(raw,'big') if version==3 else sum((raw[i]&127)<<(7*(3-i)) for i in range(4));body=data[at+10:at+10+n]
                if key.startswith('T') and body:
                    encoding={0:'latin-1',1:'utf-16',2:'utf-16-be',3:'utf-8'}.get(body[0],'utf-8');tags[key]=body[1:].decode(encoding,'replace').strip('\0')
                at+=10+n
    if len(data)>=128 and data[-128:-125]==b'TAG':
        tail=data[-128:]
        for key,start,n in [('title',3,30),('artist',33,30),('album',63,30),('year',93,4)]:tags['ID3v1_'+key]=tail[start:start+n].decode('latin-1').strip('\0 ')
    return dict(meta,tags=tags)

def provenance(path):
    try:return Path(str(path)+':Zone.Identifier').read_text(encoding='utf-8-sig').strip().replace('\x00','')
    except OSError:return None

def main():
    OUT.mkdir(parents=True,exist_ok=True);rows=[];ancillary=[]
    for path in sorted(SOURCE.rglob('*')):
        if not path.is_file():continue
        data=path.read_bytes();rel=path.relative_to(SOURCE).as_posix();ext=path.suffix.lower()
        base={'path':rel,'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest()}
        if ext not in EXTENSIONS:ancillary.append(base);continue
        row=dict(base,format=ext[1:],absolute=str(path.resolve()),provenance=provenance(path))
        row['class']='PREVIEW_NOT_RUNTIME' if rel=='kenney_rpg-audio/Preview.ogg' else 'BGM' if '/' not in rel else 'SFX'
        try:row.update(ogg_metadata(data) if ext=='.ogg' else mp3_tags(data) if ext=='.mp3' else {})
        except Exception as error:row['metadataError']=str(error)
        rows.append(row)
    (OUT/'probe-input.json').write_text(json.dumps(rows,ensure_ascii=False),encoding='utf-8')
    cache=Path.home()/'.gradle/caches/modules-2/files-2.1';jars=[]
    for relative in ['com.badlogicgames.gdx/gdx/1.14.2','com.badlogicgames.gdx/gdx-backend-lwjgl3/1.14.2','org.jcraft/jorbis/0.0.17','com.badlogicgames.jlayer/jlayer/1.0.1-gdx']:
        matches=[p for p in (cache/relative).rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar'))]
        if len(matches)!=1:raise RuntimeError('Expected cached project jar: '+relative)
        jars.extend(matches)
    # OggInputStream uses BufferUtils; loading these cached core jars does not open an audio device.
    lwjgl=cache/'org.lwjgl/lwjgl'
    version=sorted(p for p in lwjgl.iterdir() if p.is_dir())[-1]
    jars.extend(p for p in version.rglob('*.jar') if p.name in [f'lwjgl-{version.name}.jar',f'lwjgl-{version.name}-natives-windows.jar'])
    cp=os.pathsep.join(map(str,jars));classes=OUT/'probe-classes';classes.mkdir(exist_ok=True)
    subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(ROOT/'tools/audio/AudioProbe.java')],check=True)
    args=OUT/'probe-java-args.txt';args.write_text('-cp\n"'+(cp+os.pathsep+str(classes)).replace('\\','/')+'"\n',encoding='utf-8')
    result=subprocess.run(['java','-Dfile.encoding=UTF-8','@'+str(args),'AudioProbe',str(OUT/'probe-input.json'),str(OUT/'decoded-pcm.json')],text=True,capture_output=True,encoding='utf-8',errors='replace')
    (OUT/'probe.log').write_text(result.stdout+result.stderr,encoding='utf-8');print(result.stdout+result.stderr);result.check_returncode()
    decoded={r['path']:r for r in json.loads((OUT/'decoded-pcm.json').read_text(encoding='utf-8'))}
    for row in rows:
        measurement=decoded[row['path']];row['pcm']=measurement
        for key in ['codec','channels','sampleRate']:
            if key in measurement:row.setdefault(key,measurement[key])
        row.setdefault('duration',measurement.get('decodedSeconds'))
        if row.get('duration'):row['fileAverageBitrate']=8*row['bytes']/row['duration']
        issues=list(row.get('containerErrors',[]))
        if measurement.get('decode')!='OK':issues.append('DECODE_ERROR')
        if measurement.get('leadingSilenceSeconds',0)>.03:issues.append('LEAD_GT_30MS')
        if measurement.get('trailingSilenceSeconds',0)>.1:issues.append('TAIL_GT_100MS')
        if measurement.get('fullScaleSamples',0)>0:issues.append('PCM_FULL_SCALE_REVIEW')
        if measurement.get('rmsDbfs') is not None and measurement['rmsDbfs']< -40:issues.append('LOW_RMS_REVIEW')
        if abs(measurement.get('dcOffset',0))>.01:issues.append('DC_OFFSET_REVIEW')
        if measurement.get('endpointJump',0)>.1:issues.append('ENDPOINT_JUMP_REVIEW')
        if measurement.get('pcm16Bytes',0)>1048576:issues.append('MUSIC_NOT_SOUND_SIZE')
        if row.get('duration',0)<=0:issues.append('INVALID_DURATION')
        row['issues']=issues
        if row['class']=='SFX':row['loopAssessment']='ONE_SHOT_NOT_AUTHORED_LOOP'
        elif row['class']=='PREVIEW_NOT_RUNTIME':row['loopAssessment']='PACK_PREVIEW_EXCLUDE'
        elif row['path']=='battle_music_01-loop.ogg':row['loopAssessment']='AUTHOR_LOOP_START_APPROX_7_5S_REVIEW'
        elif row['format']=='mp3':row['loopAssessment']='AUTHOR_LOOPABLE_MP3_GAP_RISK'
        elif row['path'] in ['hope_orchestral_battle_music_bpm165.ogg','heavy_boss_battle_1_bpm200.ogg']:row['loopAssessment']='AUTHOR_LOOPABLE_BACKEND_AUDITION_REQUIRED'
        else:row['loopAssessment']='LOOP_UNVERIFIED_AUDITION_REQUIRED'
    groups=collections.defaultdict(list)
    for row in rows:groups[row['sha256']].append(row['path'])
    summary={'totalFiles':len(rows)+len(ancillary),'audioFiles':len(rows),'ancillaryFiles':len(ancillary),'byClass':dict(collections.Counter(r['class'] for r in rows)),
             'byFormat':dict(collections.Counter(r['format'] for r in rows)),'audioBytes':sum(r['bytes'] for r in rows),'audioSeconds':sum(r.get('duration',0) for r in rows),
             'duplicates':[names for names in groups.values() if len(names)>1],'decodeErrors':[r['path'] for r in rows if r['pcm'].get('decode')!='OK'],
             'tools':'Python stdlib; cached libGDX1.14.2 OggInputStream/JOrbis0.0.17; JLayer1.0.1-gdx; Java17; no playback, ffmpeg, installation, source edits or audio output',
             'measurements':'PCM16 sample peak/RMS/DC/full-scale counts; -60dBFS per-frame silence threshold; not LUFS, true-peak, spectral or perceptual assessment'}
    (OUT/'audio-audit.json').write_text(json.dumps({'summary':summary,'files':rows,'ancillary':ancillary},ensure_ascii=False,indent=2),encoding='utf-8')
    keys=['path','class','format','codec','bytes','duration','sampleRate','channels','fileAverageBitrate','loopAssessment','issues','decode','samplePeakDbfs','rmsDbfs','leadingSilenceSeconds','trailingSilenceSeconds','fullScaleSamples','endpointJump','pcm16Bytes']
    with (OUT/'audio-audit.csv').open('w',encoding='utf-8-sig',newline='') as stream:
        writer=csv.DictWriter(stream,fieldnames=keys);writer.writeheader()
        for row in rows:
            combined=dict(row,**{k:v for k,v in row['pcm'].items() if k!='path'});combined['issues']=';'.join(row['issues']);writer.writerow({k:combined.get(k,'') for k in keys})
    print(json.dumps(summary,indent=2))

if __name__=='__main__':main()
