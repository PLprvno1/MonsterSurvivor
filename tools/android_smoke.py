"""ADB smoke for a connected emulator. Installs this project's debug APK; no network.
Reads actual game pause telemetry, injects touch events, records screenshots/logs.
"""
from pathlib import Path
import json
import re
import struct
import subprocess
import time
import zipfile

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'core/build/reports/task01b2'
OUT.mkdir(parents=True,exist_ok=True)
sdk=next(line.split('=',1)[1].strip() for line in (ROOT/'local.properties').read_text().splitlines() if line.startswith('sdk.dir='))
ADB=str(Path(sdk)/'platform-tools/adb.exe')
PACKAGE='com.example.monstersurvivor'
ACTIVITY=PACKAGE+'/com.example.monstersurvivor.android.AndroidLauncher'


def adb(*args):
    return subprocess.check_output([ADB,*args],text=True,stderr=subprocess.STDOUT).strip()


def logs(): return adb('logcat','-d','-s','MonsterSurvivor:I','AndroidRuntime:E','*:S')


def capture(name):
    png=subprocess.check_output([ADB,'exec-out','screencap','-p'])
    (OUT/name).write_bytes(png)
    return struct.unpack('>II',png[16:24])


results=[]
def check(ok,label):
    if not ok: raise AssertionError(label)
    results.append(label)
    print('OK',label,flush=True)


def run():
    apk=ROOT/'android/build/outputs/apk/debug/android-debug.apk'
    with zipfile.ZipFile(apk) as z:
        pngs=[n for n in z.namelist() if n.startswith('assets/') and n.endswith('.png')]
        check(len(pngs)==15 and not any('n3xus' in n.lower() or 'demo/' in n for n in z.namelist()),'APK contains 15 selected PNG textures including pickups; no excluded art')
    print(adb('install','-r',str(apk)),flush=True)
    adb('shell','am','force-stop',PACKAGE);adb('logcat','-c')
    launch=adb('shell','am','start','-W','-n',ACTIVITY)
    check('Status: ok' in launch,'Android activity cold launch')
    deadline=time.monotonic()+20
    while 'Ready: 100x100 forest' not in logs():
        if time.monotonic()>deadline: raise TimeoutError('Game ready event not received')
        time.sleep(.2)
    time.sleep(2)
    w,h=capture('android-start.png')
    check(w>h,'Landscape GL surface')
    scale=min(w/800,h/480);uiw=w/scale;uih=h/scale
    def point(x,y): return round(x*scale),round(h-y*scale)
    def tap(x,y): adb('shell','input','tap',*map(str,point(x,y)));time.sleep(.25)
    def snapshot():
        hits=re.findall(r'User pause=(true|false) time=([0-9.]+) player=([0-9.]+),([0-9.]+) fps=(\d+)',logs())
        check(bool(hits),'Game emitted pause telemetry')
        state,t,x,y,fps=hits[-1]
        return {'paused':state=='true','time':float(t),'x':float(x),'y':float(y),'fps':int(fps)}
    tap(uiw-64,uih-35);initial=snapshot();check(initial['paused'],'Touch pause button')
    time.sleep(2)
    capture('android-paused.png')
    tap(uiw/2,uih/2-32);resumed=snapshot()
    check(not resumed['paused'] and resumed['time']==initial['time'],'Paused timer does not advance; touch Resume works')
    start=point(108,108);end=point(174,108)
    adb('shell','input','swipe',*map(str,(*start,*end)), '2000')
    tap(uiw-64,uih-35);moved=snapshot()
    check(moved['x']>initial['x']+35 and abs(moved['y']-initial['y'])<1,'Actual Android joystick moves the player horizontally')
    capture('android-after-joystick.png')
    adb('shell','input','keyevent','KEYCODE_HOME');time.sleep(2)
    adb('shell','am','start','-W','-n',ACTIVITY);time.sleep(.5)
    capture('android-lifecycle-resume.png')
    check('Lifecycle pause' in logs() and 'Lifecycle resume: waiting for explicit resume' in logs(),'Android Home/return executes pause/resume lifecycle')
    tap(uiw/2,uih/2-32);returned=snapshot()
    check(not returned['paused'] and returned['time']==moved['time'],'Background/resume freezes timer and requires explicit resume')
    tap(uiw-112,41);tap(uiw-112,91);time.sleep(1)
    capture('android-walk-preview.png')
    tap(uiw-112,41);tap(uiw-112,91);time.sleep(.25)
    capture('android-attack-preview.png')
    time.sleep(2)
    tap(uiw-64,uih-35);steady=snapshot()
    check(steady['time']>returned['time']+2 and steady['x']==moved['x'] and steady['y']==moved['y'],'Timer advances after resume and joystick does not stick')
    capture('android-steady-resume.png')
    tap(uiw/2,uih/2-32)
    pid=adb('shell','pidof',PACKAGE).split()[0]
    app_log=adb('logcat','-d','--pid='+pid)
    (OUT/'android-logcat.txt').write_text(app_log,encoding='utf-8')
    check('FATAL EXCEPTION' not in app_log and 'GdxRuntimeException' not in app_log,'No app crash or missing-asset exception in collected runtime log')
    report={'checks':results,'emulator':adb('emu','avd','name'),'model':adb('shell','getprop','ro.product.model'),
            'api':adb('shell','getprop','ro.build.version.sdk'),'resolution':[w,h],
            'initial':initial,'afterJoystick':moved,'afterLifecycle':returned,'steadyAfterResume':steady,
            'note':'FPS values are snapshots of the default six-exhibit scene, not a 60-monster Android benchmark.'}
    (OUT/'android-smoke.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('ANDROID SMOKE PASSED',len(results),'checks. FPS snapshots:',initial['fps'],moved['fps'],returned['fps'],steady['fps'])


if __name__=='__main__':
    try:run()
    finally:(OUT/'android-events.txt').write_text(logs(),encoding='utf-8')
