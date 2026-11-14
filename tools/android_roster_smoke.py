"""Native Android flow and natural roster sampling on the Android Studio Run deployment."""
from pathlib import Path
import json,re,subprocess,time,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task04a'
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe';PKG='com.example.monstersurvivor'
HOME=PKG+'/'+PKG+'.android.HomeActivity';GAME=PKG+'/'+PKG+'.android.AndroidLauncher'
checks=[];runs=[];pid='';cursor=0
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def write(name,text):subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/{name}'"],input=text,text=True,encoding='utf-8',capture_output=True,check=True)
def check(value,label):
    if not value:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','AndroidRuntime:E','*:S')
def current():return logs()[cursor:]
def tap(x,y):adb('shell','input','tap',str(x),str(y))
def wait(fn,seconds=10):
    start=time.monotonic()
    while time.monotonic()-start<seconds:
        value=fn()
        if value:return value
        time.sleep(.25)
    raise TimeoutError('Expected native/game telemetry not observed')
def tap_id(name):
    adb('shell','uiautomator','dump','/data/local/tmp/task04a-window.xml')
    root=ET.fromstring(adb('exec-out','cat','/data/local/tmp/task04a-window.xml'))
    node=next(n for n in root.iter('node') if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));tap((x+r)//2,(y+b)//2)
def at_home():return re.search(r'(?:topResumedActivity|ResumedActivity)[:=].*\.HomeActivity',adb('shell','dumpsys','activity','activities'))
def pause():
    before=current().count('User pause=true');tap(2136,72)
    wait(lambda:current().count('User pause=true')>before)
    return re.findall(r'User pause=true[^\n]*',current())[-1]
files=adb('shell','run-as',PKG,'ls','files').splitlines()
previous={name:adb('exec-out','run-as',PKG,'cat','files/'+name) if name in files else None for name in ['map-seed.txt','combat-profile.json']}
try:
    adb('shell','run-as',PKG,'rm','-f','files/combat-profile.json');write('map-seed.txt','42')
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    for index,(difficulty,mode) in enumerate([('EASY','DEMO'),('EASY','NORMAL'),('HARD','DEMO'),('HARD','NORMAL')]):
        cursor=len(logs())
        for name in ['btn_play','card_diff_'+difficulty.lower(),'card_mode_'+mode.lower(),'btn_start_game']:tap_id(name)
        wait(lambda:'Ready: '+difficulty+' '+mode in current())
        check('roster=82 textures=19 rgbaBytes=44285956' in current(),'Registry/atlas loaded through native Intent '+difficulty+'/'+mode)
        if index==0:
            # Observe ordinary scheduled spawns, not debug-injected enemies.
            wait(lambda:len(set(re.findall(r'Enemy species=((?:mc_|sc_)[^ ]+)',current())))>=2,22)
            time.sleep(3);state=pause();species=sorted(set(re.findall(r'Enemy species=([^ ]+)',current())))
            check(len(species)>=3 and sum(s.startswith(('mc_','sc_')) for s in species)>=2,'Natural early gameplay includes multiple new species')
            check('zoom=1.15' in state and 'sword=48.0/1.0' in current(),'World zoom and Sword tuning preserved')
            time.sleep(.4);tap(1140,612);wait(lambda:'User pause=false' in current())
            resumed=re.findall(r'User pause=false[^\n]*',current())[-1]
            check(re.search(r'time=([^ ]+)',state)[1]==re.search(r'time=([^ ]+)',resumed)[1],'Pause freezes actual clock')
            adb('shell','input','swipe','243','837','391','837','700');state=pause()
            check(float(re.search(r'player=([\d.]+)',state)[1])>1608,'Joystick still moves with new roster')
            runs.append({'difficulty':difficulty,'mode':mode,'species':species,'pause':state})
        else:
            time.sleep(1);state=pause();runs.append({'difficulty':difficulty,'mode':mode,'pause':state})
        tap(1140,783);wait(at_home);check(True,'Pause/Home '+difficulty+'/'+mode)
        if index==0:adb('shell','run-as',PKG,'rm','-f','files/map-seed.txt')
    check('FATAL EXCEPTION' not in logs() and 'GdxRuntimeException' not in logs(),'No Android crash or missing asset')
    (OUT/'android-roster-smoke.json').write_text(json.dumps({'checks':checks,'runs':runs,'method':'Native menus; natural weighted spawns and real joystick/Pause/Home, no enemy/HP injection'},indent=2),encoding='utf-8')
    print('ANDROID ROSTER FLOW PASSED:',len(checks),'checks',flush=True)
finally:
    if pid:(OUT/'android-roster-logcat.txt').write_text(logs(),encoding='utf-8')
    for name,value in previous.items():
        if value is None:adb('shell','run-as',PKG,'rm','-f','files/'+name)
        else:write(name,value)
    adb('shell','rm','-f','/data/local/tmp/task04a-window.xml')
