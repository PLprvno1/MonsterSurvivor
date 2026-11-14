"""Inspect file metadata and prepare selected Tiny Questers using local ImageIO only.
No image is submitted to any AI/image-generation/training service.
"""
from pathlib import Path
import struct,json,re,subprocess

ROOT=Path(__file__).resolve().parents[1]
PACK=ROOT/'asset_sources/tiny-questers-free-starter-pack/tiny-questers-free-starter-pack'
DIRS=['down','left','right','up']

def gif_delays(path):
    """Parse GIF blocks without decoding artwork. Delays are hundredths of a second."""
    b=path.read_bytes();assert b[:6] in (b'GIF87a',b'GIF89a');i=13
    if b[10]&128:i+=3*(2**((b[10]&7)+1))
    delays=[];delay=None
    def skip_blocks(pos):
        while b[pos]:pos+=1+b[pos]
        return pos+1
    while i<len(b):
        marker=b[i];i+=1
        if marker==0x3b:break
        if marker==0x21:
            label=b[i];i+=1
            if label==0xf9:
                assert b[i]==4;delay=struct.unpack('<H',b[i+2:i+4])[0];i+=6
            else:i=skip_blocks(i)
        elif marker==0x2c:
            packed=b[i+8];i+=9
            if packed&128:i+=3*(2**((packed&7)+1))
            i+=1;i=skip_blocks(i);delays.append(delay)
        else:raise ValueError('Unknown GIF block')
    assert delays and len(set(delays))==1 and delays[0]>0,path
    return delays

def prepare_tiny():
    work=ROOT/'core/build/asset-tools';work.mkdir(parents=True,exist_ok=True)
    records=[]
    inventory=[]
    for folder,id,title,base,scale in [('warrior_free','player','Tiny Questers Warrior',(64,49,32,38),1),
        ('slime_free','tiny_slime','Tiny Questers Slime',(64,64,32,48),1),
        ('bat_free','tiny_bat','Tiny Questers Bat',(64,64,32,48),.5)]:
        p=PACK/folder
        groups={}
        for f in (p/'png/single').rglob('*.png'):groups.setdefault(f.parent,[]).append(f)
        clips={}
        for d,fs in sorted(groups.items()):
            name=str(d.relative_to(p/'png/single')).replace('\\','_').replace('/','_')
            if folder!='warrior_free':name=name.split('_',1)[1]
            fs.sort(key=lambda f:int(re.search(r'(\d+)\.png$',f.name)[1]))
            assert [int(re.search(r'(\d+)\.png$',f.name)[1]) for f in fs]==list(range(1,len(fs)+1))
            dims={struct.unpack('>II',f.read_bytes()[16:24]) for f in fs};assert len(dims)==1
            w,h=dims.pop();prefix='' if id=='player' else ('slime_' if id=='tiny_slime' else 'bat_')
            atlas=p/'png/atlas'/f'{prefix}{name}.png';assert atlas.exists()
            aw,ah=struct.unpack('>II',atlas.read_bytes()[16:24]);assert aw%w==0 and ah%h==0 and aw//w*(ah//h)>=len(fs)
            gif=p/'gif/1x'/f'{prefix}{name}.gif'
            if not gif.exists():gif=p/'gif/1x'/f'{prefix}{name.removesuffix("_stay")}.gif'
            delays=gif_delays(gif);fps=100/delays[0]
            info={'source':str(d.relative_to(ROOT)).replace('\\','/'),'atlas':str(atlas.relative_to(ROOT)).replace('\\','/'),
                  'clip':name,'frames':len(fs),'cell':[w,h],'sheet':[aw,ah],'fps':fps,'gifFrames':len(delays),'delayMs':delays[0]*10}
            info['gif']=str(gif.relative_to(ROOT)).replace('\\','/')
            inventory.append(info);clips[name]=(fs,w,h,fps)
        chosen={};animations=[]
        def add(state,direction,clip,pivot=None,mirror=False,first=False,note=None):
            assert clip in clips
            if clip not in chosen:chosen[clip]=None
            animations.append({'state':state,'direction':direction,'sourceClip':clip,'fps':clips[clip][3],
                'loop':state in ('idle','walk'),'feetX':(pivot or base[2:])[0],'feetY':(pivot or base[2:])[1],
                'mirrorX':mirror,'firstFrameOnly':first,'note':note or 'source animation'})
        for d in DIRS:
            side=d in ('left','right')
            add('idle',d,'idle_'+d if not side else 'walk_'+d,first=side and id!='tiny_bat',
                note=('side hover reuses complete walk flap loop' if id=='tiny_bat' else 'side idle uses first walk pose') if side else None)
            add('walk',d,'walk_'+d)
            if id=='player':
                add('attack',d,'attack_'+d+'_stay',(64,78))
                add('hurt',d,'hit',note='single source facing shared across directions')
                add('death',d,'die',(42,38),note='single source facing; initial body shifted +10px on wider canvas')
                add('victory',d,'win_left' if d=='left' else 'win_right',(32,53),note='two source facings; up/down use right')
            elif id=='tiny_slime':
                add('attack',d,'attack_'+d)
                add('hurt',d,'hit_right' if d=='left' else 'hit_'+d,mirror=d=='left',note='left mirrors hit_right' if d=='left' else None)
                add('death',d,'die_a',note='single death clip shared across directions')
            else:
                pivots={'down':(32,48),'up':(32,129),'left':(96,80),'right':(32,80)}
                add('attack',d,'attack_'+d,pivots[d])
                add('hurt',d,'hit_down',note='single down-facing source shared across directions')
                add('death',d,'die_down',note='single down-facing source shared across directions')
        # Shelf pack only unique selected clips, with 2px extrusion on every cell.
        x=y=2;row_h=0;plan=[];rects={}
        for clip in chosen:
            fs,w,h,fps=clips[clip];rects[clip]=[]
            atlas_info=next(v for v in inventory if v['source'].startswith(str((p/'png/single').relative_to(ROOT)).replace('\\','/')) and v['clip']==clip)
            atlas_path=ROOT/atlas_info['atlas'];columns=atlas_info['sheet'][0]//w
            for index,f in enumerate(fs):
                if x+w+2>1024:x=2;y+=row_h+4;row_h=0
                rects[clip].append([x,y,w,h]);plan.append(f'{f}\t{x}\t{y}\t{w}\t{h}\t{atlas_path}\t{index%columns*w}\t{index//columns*h}')
                x+=w+4;row_h=max(row_h,h)
        height=y+row_h+2
        plan_path=work/(id+'.tsv');plan_path.write_text('\n'.join(plan)+'\n',encoding='utf8')
        texture='game/tiny/'+id+'.png'
        subprocess.run(['java','tools/PackPng.java',str(plan_path),str(ROOT/'assets'/texture),'1024',str(height)],cwd=ROOT,check=True)
        for a in animations:
            a['frames']=rects[a['sourceClip']][:1] if a.pop('firstFrameOnly') else rects[a['sourceClip']]
            a['source']=next(v['source'] for v in inventory if v['source'].startswith(str((p/'png/single').relative_to(ROOT)).replace('\\','/')) and v['clip']==a['sourceClip'])
        records.append({'id':id,'title':title,'texture':texture,'frameWidth':base[0],'frameHeight':base[1],
            'feetX':base[2],'feetY':base[3],'displayScale':scale,'animations':animations,
            'pivotEvidence':'README feet-center + local pixel translation; explicit per-clip anchors, not embedded metadata',
            'dashAvailable':False})
    (work/'tiny-inventory.json').write_text(json.dumps(inventory,indent=2)+'\n',encoding='utf8')
    return records
