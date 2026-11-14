"""Presented-frame before/after test on the connected emulator. No frame pixel export.
Cold launches each installed APK, warms up 10s, samples SurfaceFlinger in the same scene.
"""
from pathlib import Path
import sys,json,time,subprocess,struct,statistics
import android_smoke as a

OUT=a.ROOT/'core/build/reports/task01b2';OUT.mkdir(parents=True,exist_ok=True)
def measure(label,apk):
    print(label,a.adb('install','-r',str(apk)),flush=True)
    a.adb('shell','am','force-stop',a.PACKAGE);a.adb('logcat','-c');a.adb('shell','am','start','-W','-n',a.ACTIVITY)
    print('Warm-up 10s',flush=True);time.sleep(10)
    png=subprocess.check_output([a.ADB,'exec-out','screencap','-p']);w,h=struct.unpack('>II',png[16:24])
    layer=next(x for x in a.adb('shell','dumpsys','SurfaceFlinger','--list').splitlines() if 'SurfaceView[' in x and '(BLAST)' in x)
    layer=layer.removeprefix('RequestedLayerState{').split(' parentId=')[0]
    def sample():
        rows=a.adb('shell','dumpsys SurfaceFlinger --latency "'+layer+'"').splitlines()[1:]
        return {int(r.split()[1]) for r in rows if len(r.split())==3 and 0<int(r.split()[1])<2**63-1}
    result={'apk':str(apk.relative_to(a.ROOT)),'warmupSeconds':10,'resolution':[w,h],'method':'SurfaceFlinger unique actualPresentTime'}
    for name in ['idle','horizontal']:
        cutoff=max(sample());seen=set();swipe=None
        if name=='horizontal':
            s=min(w/800,h/480)
            swipe=subprocess.Popen([a.ADB,'shell','input','swipe',str(round(108*s)),str(round(h-108*s)),str(round(174*s)),str(round(h-108*s)),'11000'],stdout=subprocess.DEVNULL)
        for _ in range(10):time.sleep(1);seen.update(t for t in sample() if t>cutoff)
        if swipe:swipe.wait()
        ts=sorted(seen);dt=sorted((b-c)/1e6 for c,b in zip(ts,ts[1:]));assert len(dt)>150
        result[name]={'frames':len(ts),'seconds':(ts[-1]-ts[0])/1e9,'fps':(len(ts)-1)*1e9/(ts[-1]-ts[0]),
            'medianMs':statistics.median(dt),'p95Ms':dt[round((len(dt)-1)*.95)],'p99Ms':dt[round((len(dt)-1)*.99)],
            'frameTimesMs':dt}
    log=a.adb('logcat','-d','--pid='+a.adb('shell','pidof',a.PACKAGE).split()[0])
    assert 'FATAL EXCEPTION' not in log and 'GdxRuntimeException' not in log
    (OUT/('frame-pacing-'+label+'.json')).write_text(json.dumps(result,indent=2)+'\n')
    print(label,{k:{key:value for key,value in v.items() if key!='frameTimesMs'} for k,v in result.items() if isinstance(v,dict)},flush=True)
    a.adb('shell','am','force-stop',a.PACKAGE)

for label,apk in [('before',OUT/'baseline.apk'),('after',a.ROOT/'android/build/outputs/apk/debug/android-debug.apk')]:measure(label,apk)
