"""Measure compositor presentation timestamps; no screenshot pixels sent to services.
Cold starts the installed game, then measures after warm-up. Uses the connected emulator.
"""
import sys,time,json,subprocess,statistics,struct
from pathlib import Path
import android_smoke as a

label=sys.argv[1] if len(sys.argv)>1 else 'after'
out=a.ROOT/'core/build/reports/task01b1';out.mkdir(parents=True,exist_ok=True)
a.adb('shell','am','force-stop',a.PACKAGE);a.adb('logcat','-c');a.adb('shell','am','start','-W','-n',a.ACTIVITY)
print('Warm-up 10 seconds after cold launch',flush=True);time.sleep(10)
layer=next(x for x in a.adb('shell','dumpsys','SurfaceFlinger','--list').splitlines() if 'SurfaceView[' in x and '(BLAST)' in x)
layer=layer.removeprefix('RequestedLayerState{').split(' parentId=')[0]
def sample():
    rows=a.adb('shell','dumpsys SurfaceFlinger --latency "'+layer+'"').splitlines()[1:]
    return {int(r.split()[1]) for r in rows if len(r.split())==3 and 0<int(r.split()[1])<2**63-1}
def measure(moving):
    seen=sample();cutoff=max(seen);seen.clear()
    swipe=None
    if moving:
        # Pixel 4 landscape 2280x1080: same UI viewport conversion as android_smoke.
        png=subprocess.check_output([a.ADB,'exec-out','screencap','-p']);w,h=struct.unpack('>II',png[16:24]);assert w>h;s=min(w/800,h/480)
        swipe=subprocess.Popen([a.ADB,'shell','input','swipe',str(round(108*s)),str(round(h-108*s)),str(round(174*s)),str(round(h-108*s)),'11000'],stdout=subprocess.DEVNULL)
    for _ in range(10): time.sleep(1);seen.update(t for t in sample() if t>cutoff)
    if swipe: swipe.wait()
    ts=sorted(seen);dt=[(y-x)/1e6 for x,y in zip(ts,ts[1:])]
    assert len(ts)>100
    return {'frames':len(ts),'seconds':(ts[-1]-ts[0])/1e9,'fps':(len(ts)-1)*1e9/(ts[-1]-ts[0]),'frameMsMedian':statistics.median(dt),'frameMsP95':sorted(dt)[int(.95*(len(dt)-1))]}
result={'label':label,'warmupSeconds':10,'method':'SurfaceFlinger actualPresentTime, unique frames, 10 polling intervals per scene; actual elapsed reported','idle':measure(False),'joystick':measure(True)}
(out/('fps-'+label+'.json')).write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result,indent=2),flush=True)
