"""Compare actual joystick/camera traces. Local metadata only, no artwork input to AI.
python tools/camera_compare.py android|desktop [LEGACY FREE WORLD SCREEN]
"""
from pathlib import Path
import sys,subprocess,json,time,csv,math,statistics,os
import android_smoke as adbtools

ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task01b2';OUT.mkdir(parents=True,exist_ok=True)
MODES=sys.argv[2:] or ['LEGACY','FREE','WORLD','SCREEN'];PLATFORM=sys.argv[1]

def percentile(values,q):return sorted(values)[round((len(values)-1)*q)]
def summarize(path):
    data=[{k:float(v) for k,v in row.items()} for row in csv.DictReader(path.open())]
    result={}
    for seg in sorted({int(r['segment']) for r in data}):
        rows=[r for r in data if int(r['segment'])==seg][2:]
        assert len(rows)>=10,(seg,len(rows))
        pairs=list(zip(rows,rows[1:]));errors=[];stalls=0;moving=0
        for a,b in pairs:
            dx=(b['simX']-a['simX'])*b['scaleX'];dy=(b['simY']-a['simY'])*b['scaleY']
            cx=(b['cameraX']-a['cameraX'])*b['scaleX'];cy=(b['cameraY']-a['cameraY'])*b['scaleY']
            errors.append(math.hypot(cx-dx,cy-dy))
            if math.hypot(dx,dy)>.05:
                moving+=1;stalls+=math.hypot(cx,cy)<.05
        dt=[r['delta']*1000 for r in rows]
        cpu=[r['cpuMs'] for r in rows]
        result[str(seg)]={'frames':len(rows),'renderFps':1000/statistics.mean(dt),'deltaP95Ms':percentile(dt,.95),
            'deltaP99Ms':percentile(dt,.99),'cpuMeanMs':statistics.mean(cpu),'cpuP99Ms':percentile(cpu,.99),
            'cameraDisplacementErrorRmsPx':math.sqrt(statistics.mean(e*e for e in errors)),
            'cameraStallPercent':100*stalls/moving if moving else 0,
            'playerOffsetRangePx':[max(r['offsetXpx'] for r in rows)-min(r['offsetXpx'] for r in rows),
                                  max(r['offsetYpx'] for r in rows)-min(r['offsetYpx'] for r in rows)],
            'maxDeltaClampMs':max((r['delta']-r['step'])*1000 for r in rows)}
    return result

if PLATFORM=='android':
    print(adbtools.adb('install','-r',str(ROOT/'android/build/outputs/apk/debug/android-debug.apk')),flush=True)
else:
    assert PLATFORM=='desktop'
    args=OUT/'java-args.txt';cp=args.read_text().splitlines()[1].strip('"')
    subprocess.run(['javac','-cp',cp,'-d',str(ROOT/'core/build/verification-classes'),str(ROOT/'tools/smoke/CameraLauncher.java')],check=True)
    cp+=';'+str(ROOT/'assets')
    run=ROOT/'core/build/camera-run';run.mkdir(parents=True,exist_ok=True)
summary_file=OUT/(PLATFORM+'-camera-comparison.json')
summaries=json.loads(summary_file.read_text()) if summary_file.exists() else {}
try:
    for mode in MODES:
        config={'mode':mode,'warmup':10 if PLATFORM=='android' else 3,'segmentSeconds':2 if PLATFORM=='android' else 1,'exit':True}
        if os.environ.get('MS_CAMERA_LONG')=='1':config['warmup']=10;config['segmentSeconds']=2
        config['raster']=mode=='SCREEN' and os.environ.get('MS_CAMERA_RASTER')=='1'
        if PLATFORM=='android':
            temp=OUT/'camera-check.json';temp.write_text(json.dumps(config))
            adbtools.adb('push',str(temp),'/data/local/tmp/ms-camera-check.json')
            adbtools.adb('shell','run-as',adbtools.PACKAGE,'cp','/data/local/tmp/ms-camera-check.json','files/camera-check.json')
            adbtools.adb('shell','am','force-stop',adbtools.PACKAGE);adbtools.adb('logcat','-c')
            adbtools.adb('shell','am','start','-W','-n',adbtools.ACTIVITY)
            deadline=time.monotonic()+90
            while True:
                logs=adbtools.logs()
                if 'CameraTrace complete mode='+mode in logs:break
                if 'FATAL EXCEPTION' in logs or time.monotonic()>deadline:raise RuntimeError(logs)
                time.sleep(1)
            trace=subprocess.check_output([adbtools.ADB,'exec-out','run-as',adbtools.PACKAGE,'cat','files/camera-trace.csv']).decode('utf8')
            if config['raster']:
                (OUT/(PLATFORM+'-raster.csv')).write_bytes(subprocess.check_output([adbtools.ADB,'exec-out','run-as',adbtools.PACKAGE,'cat','files/camera-raster.csv']))
            (OUT/(PLATFORM+'-'+mode+'.log')).write_text(logs,encoding='utf8')
        else:
            (run/'camera-check.json').write_text(json.dumps(config))
            with (OUT/(PLATFORM+'-'+mode+'.log')).open('w') as log:
                subprocess.run(['java','-cp',cp,'CameraLauncher'],cwd=run,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=75)
            trace=(run/'camera-trace.csv').read_text()
            if config['raster']:(OUT/(PLATFORM+'-raster.csv')).write_text((run/'camera-raster.csv').read_text())
        path=OUT/(PLATFORM+'-'+mode+'.csv');path.write_text(trace)
        summaries[mode]=summarize(path)
        (OUT/(PLATFORM+'-camera-comparison.json')).write_text(json.dumps(summaries,indent=2)+'\n')
        print(PLATFORM,mode,'low horizontal:',summaries[mode]['1'],'low diagonal:',summaries[mode]['5'],flush=True)
finally:
    if PLATFORM=='android':
        adbtools.adb('shell','run-as',adbtools.PACKAGE,'rm','-f','files/camera-check.json')
    else:(run/'camera-check.json').unlink(missing_ok=True)
