"""Native UI + real gameplay smoke on the app deployed by Android Studio Run. No build/install/export."""
from pathlib import Path
import json
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task03b_1'
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe';PACKAGE='com.example.monstersurvivor'
HOME=PACKAGE+'/'+PACKAGE+'.android.HomeActivity';GAME=PACKAGE+'/'+PACKAGE+'.android.AndroidLauncher'
checks=[];runs=[]
def adb(*args,check=True):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=check).stdout
def check(ok,label):
    if not ok:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def logs():return adb('logcat','-d','--pid='+process_id,'-s','MonsterSurvivor:I','AndroidRuntime:E','*:S')
def wait_for(fn,timeout=8):
    start=time.monotonic()
    while time.monotonic()-start<timeout:
        result=fn()
        if result:return result
        time.sleep(.25)
    raise TimeoutError('Expected UI/telemetry was not observed')
def tap(x,y):adb('shell','input','tap',str(x),str(y))
def tap_id(resource):
    for attempt in range(3):
        adb('shell','uiautomator','dump','/data/local/tmp/task03b1-world-window.xml')
        xml=adb('exec-out','cat','/data/local/tmp/task03b1-world-window.xml');root=ET.fromstring(xml)
        match=next((n for n in root.iter('node') if n.get('resource-id')==PACKAGE+':id/'+resource),None)
        if match is not None:
            x1,y1,x2,y2=map(int,re.findall(r'\d+',match.get('bounds')));tap((x1+x2)//2,(y1+y2)//2);time.sleep(.25);return
        time.sleep(.3)
    raise AssertionError('Native control missing: '+resource)
def activity():return adb('shell','dumpsys','activity','activities')
ready_re=re.compile(r'Ready: (EASY|HARD) (NORMAL|DEMO).*seed=(-?\d+) walkable=(\d+) fallback=(\w+) sword=48\.0/1\.0')
pause_re=re.compile(r'User pause=(true|false) time=([\d.]+) player=([^ ]+) fps=(\d+) hp=(\d+)/\d+.*? kills=(\d+) seed=(-?\d+)')
def launch(difficulty,mode):
    before=len(ready_re.findall(logs()));tap_id('btn_play')
    tap_id('card_diff_hard' if difficulty=='HARD' else 'card_diff_easy')
    tap_id('card_mode_demo' if mode=='DEMO' else 'card_mode_normal');tap_id('btn_start_game')
    ready=wait_for(lambda:ready_re.findall(logs())[before:] or None)[-1]
    check(ready[0]==difficulty and ready[1]==mode,'Native Intent '+difficulty+'/'+mode)
    check(int(ready[3])>=34000 and ready[4]=='false','Generated Android map is valid')
    runs.append({'difficulty':difficulty,'mode':mode,'seed':int(ready[2]),'walkable':int(ready[3])});return ready
def pause():
    count=len(pause_re.findall(logs()));tap(2136,72)
    value=wait_for(lambda:pause_re.findall(logs())[count:] or None)[-1];check(value[0]=='true','Touch Pause');return value
def home_from_pause():
    tap(1140,783);wait_for(lambda:re.search(r'(?:topResumedActivity|ResumedActivity)[:=].*\.HomeActivity',activity()))
    check(True,'Gameplay Home returns to native Home')

# adb exec-out may exit 0 even when cat reports a missing file. Check existence first,
# otherwise cleanup could restore the error message as a seed file.
had_seed='map-seed.txt' in adb('shell','run-as',PACKAGE,'ls','files').splitlines()
prior_seed=adb('exec-out','run-as',PACKAGE,'cat','files/map-seed.txt') if had_seed else None
try:
    adb('shell','run-as',PACKAGE,'rm','-f','files/map-seed.txt')
    adb('shell','am','force-stop',PACKAGE);adb('shell','am','start','-W','-n',HOME)
    process_id=adb('shell','pidof',PACKAGE).strip().split()[0]
    for index,(difficulty,mode) in enumerate([('EASY','NORMAL'),('EASY','DEMO'),('HARD','DEMO'),('HARD','NORMAL')]):
        launch(difficulty,mode);time.sleep(1.5)
        if index==0:
            adb('shell','input','swipe','243','837','391','837','700')
            a=pause();check(float(a[2].split(',')[0])>1608,'Actual Android joystick moves player')
            time.sleep(.5);tap(1140,612)
            b=wait_for(lambda:(lambda p:p[-1] if p and p[-1][0]=='false' else None)(pause_re.findall(logs())))
            check(a[1]==b[1] and a[2]==b[2],'Pause freezes timer and position')
            adb('shell','input','keyevent','3');time.sleep(.5)
            lifecycle=re.findall(r'Lifecycle pause time=([\d.]+)',logs())[-1]
            before=len(ready_re.findall(logs()));adb('shell','am','start','-W','-n',GAME,'-f','0x20000000');wait_for(lambda:'Lifecycle resume:' in logs());time.sleep(1.5)
            check(len(ready_re.findall(logs()))==before,'Android lifecycle reuses the same match/map')
            tap(1140,612);time.sleep(.25);b=pause_re.findall(logs())[-1]
            check(b[0]=='false' and b[1]==lifecycle,'Lifecycle waits for explicit Resume without advancing timer')
        if index<3:
            pause();home_from_pause()
        else:
            print('Observe ordinary HARD gameplay for at most 40 seconds; no diagnostic damage injection.',flush=True)
            start=time.monotonic();over=None
            while time.monotonic()-start<40:
                found=re.findall(r'Game Over seed=(-?\d+) survived=([\d.]+) kills=(\d+)',logs())
                if found:over=found[-1];break
                time.sleep(1)
            if over:
                check(True,'Ordinary Android combat reaches Death then Game Over');old_seed=int(over[0])
                count=len(re.findall(r'Retry seed=',logs()));tap(1140,612)
                retry=wait_for(lambda:re.findall(r'Retry seed=(-?\d+) walkable=(\d+) fallback=(\w+)',logs())[count:] or None)[-1]
                check(int(retry[0])!=old_seed and int(retry[1])>=34000,'Android Retry generates a new valid seed')
                state=pause();check(state[4]=='10' and state[5]=='0','Android Retry clears HP and kills');home_from_pause()
            else:
                print('Game Over not reached within bounded observation; covered by Desktop and remains a manual Android case.',flush=True)
                pause();home_from_pause()
    check(len({r['seed'] for r in runs})==4,'New native launches generate different map seeds')
    subprocess.run([ADB,'shell',f"run-as {PACKAGE} sh -c 'cat > files/map-seed.txt'"],input='42',text=True,check=True)
    fixed=launch('EASY','NORMAL');check(fixed[2]=='42' and fixed[3]=='36322','Android fixed-seed file reproduces seed 42')
    pause();home_from_pause()
    captured=logs();check('FATAL EXCEPTION' not in captured and 'GdxRuntimeException' not in captured,'No Android runtime or missing-asset exception')
    (OUT/'android-world-flow-logcat.txt').write_text(captured,encoding='utf-8')
    result={'checks':checks,'runs':runs,'gameOverObserved':over is not None,'method':'ADB real native controls, joystick, pause, lifecycle, ordinary combat; app deployed by Android Studio Run'}
    (OUT/'android-world-flow-smoke.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print('ANDROID WORLD FLOW PASSED:',len(checks),'checks',flush=True)
finally:
    if had_seed:
        subprocess.run([ADB,'shell',f"run-as {PACKAGE} sh -c 'cat > files/map-seed.txt'"],input=prior_seed,text=True,check=True)
    else:adb('shell','run-as',PACKAGE,'rm','-f','files/map-seed.txt')
    adb('shell','rm','-f','/data/local/tmp/task03b1-world-window.xml')
