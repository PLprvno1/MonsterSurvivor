"""Native modes/preferences bridge and real floating gestures on the Studio Run build; restores preferences."""
from pathlib import Path
import json,re,subprocess,time,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task04a_1'
ADB=str(Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe');PKG='com.example.monstersurvivor'
HOME=PKG+'/.android.HomeActivity';GAME=PKG+'/.android.AndroidLauncher';PREF='shared_prefs/monster_survivor_prefs.xml'
checks=[];runs=[];all_logs=[];pid=''
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def write(path,value):subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > {path}'"],input=value,text=True,encoding='utf-8',capture_output=True,check=True)
def exists(path):
    folder,name=path.rsplit('/',1)
    return name in adb('shell','run-as',PKG,'ls',folder).splitlines()
def check(value,label):
    if not value:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def wait(fn,seconds=12):
    end=time.monotonic()+seconds
    while time.monotonic()<end:
        value=fn()
        if value:return value
        time.sleep(.2)
    raise TimeoutError('Expected Android telemetry not observed')
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','AndroidRuntime:E','*:S')
def tap(x,y):adb('shell','input','tap',str(round(x)),str(round(y)))
def tap_id(name):
    path='/data/local/tmp/task04a1-window.xml';adb('shell','uiautomator','dump',path)
    root=ET.fromstring(adb('exec-out','cat',path));node=next(n for n in root.iter('node') if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));tap((x+r)/2,(y+b)/2)
def at_home():return re.search(r'(?:topResumedActivity|ResumedActivity)[:=].*\.HomeActivity',adb('shell','dumpsys','activity','activities'))
def pause():
    old=logs().count('User pause=true');adb('shell','input','keyevent','4');wait(lambda:logs().count('User pause=true')>old)
    return re.findall(r'User pause=true[^\n]*',logs())[-1]
def resume():
    old=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>old)
    return re.findall(r'User pause=false[^\n]*',logs())[-1]
def position(line):return tuple(map(float,re.search(r'player=([\d.]+),([\d.]+)',line).groups()))
def motion(action,x,y):adb('shell','input','motionevent',action,str(round(x)),str(round(y)))
def settings(code,wrong_type=False):
    root=ET.fromstring(original_pref) if original_pref is not None else ET.Element('map')
    for n in list(root):
        if n.get('name')=='game_language':root.remove(n)
    if code is not None:
        n=ET.SubElement(root,'int' if wrong_type else 'string',name='game_language')
        if wrong_type:n.set('value','7')
        else:n.text=code
    write(PREF,ET.tostring(root,encoding='unicode',xml_declaration=True))
original_pref=adb('exec-out','run-as',PKG,'cat',PREF) if exists(PREF) else None
flags={name:adb('exec-out','run-as',PKG,'cat','files/'+name) if exists('files/'+name) else None for name in ['map-seed.txt','combat-profile.json']}
if original_pref is not None:(OUT/'preferences-before.xml').write_text(original_pref,encoding='utf-8')
try:
    for index,(code,difficulty,mode,expected) in enumerate([(None,'EASY','NORMAL','vi'),('en','HARD','DEMO','en'),('invalid','HARD','NORMAL','vi'),('en','EASY','DEMO','en'),('wrong-type','EASY','NORMAL','vi')]):
        adb('shell','am','force-stop',PKG);settings(code,index==4);write('files/map-seed.txt','42');adb('shell','run-as',PKG,'rm','-f','files/combat-profile.json')
        adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
        for name in ['btn_play','card_diff_'+difficulty.lower(),'card_mode_'+mode.lower(),'btn_start_game']:tap_id(name)
        wait(lambda:'Ready: '+difficulty+' '+mode in logs())
        ready=re.findall(r'Ready:[^\n]*',logs())[-1]
        check('language='+expected in ready,'Settings language bridge '+str(code)+' -> '+expected)
        check('initialEnemies='+('8' if difficulty=='HARD' else '6') in ready and 'residentCap='+('60' if difficulty=='HARD' else '40') in ready,'Native difficulty/mode and new balance '+difficulty+'/'+mode)
        check('roster=82 textures=19' in ready,'Existing roster/assets load')
        if index==0:
            start=pause();resume();motion('DOWN',730,626);time.sleep(.3);motion('UP',730,626);time.sleep(.3);state=pause()
            check(position(start)==position(state),'Wide-area tap without drag causes no movement')
            moves=[]
            for label,dx,dy in [('right across screen',900,0),('left',-170,0),('up',0,-170),('down',0,170),('diagonal',170,-170)]:
                origin=position(state);resume();motion('DOWN',730,626);motion('MOVE',730+dx,626+dy);time.sleep(.35);motion('UP',730+dx,626+dy);time.sleep(.65);state=pause();end=position(state)
                vx,vy=end[0]-origin[0],end[1]-origin[1]
                check(vx*(1 if dx>0 else -1)>8 if dx else abs(vx)<2,'Floating gesture horizontal '+label)
                check(vy*(-1 if dy>0 else 1)>8 if dy else abs(vy)<2,'Floating gesture vertical '+label)
                check((vx*vx+vy*vy)**.5<70,'Release stops before following pause '+label)
                moves.append({'gesture':label,'from':origin,'to':end})
            paused_time=re.search(r'time=([^ ]+)',state)[1];time.sleep(.4);resumed=resume();check(re.search(r'time=([^ ]+)',resumed)[1]==paused_time,'Pause freezes actual elapsed')
            motion('DOWN',400,800);motion('MOVE',570,800);time.sleep(.15);adb('shell','input','keyevent','3');time.sleep(.3);motion('UP',570,800)
            adb('shell','am','start','-W','--activity-reorder-to-front','-n',GAME);wait(lambda:'Lifecycle resume: waiting for explicit resume' in logs())
            life=re.findall(r'Lifecycle pause[^\n]*',logs())[-1];returned=resume();check(position(life)==position(returned),'Lifecycle resume keeps player still and requires Resume')
            time.sleep(.5);state=pause();check(position(state)==position(returned),'No stuck movement after interrupted touch')
            runs.append({'code':code,'difficulty':difficulty,'mode':mode,'ready':ready,'moves':moves,'finalPause':state})
        else:
            time.sleep(.4);state=pause();runs.append({'code':code,'difficulty':difficulty,'mode':mode,'ready':ready,'pause':state})
        tap(1140,783);wait(at_home);check(True,'Native Home '+difficulty+'/'+mode)
        log=logs();check('FATAL EXCEPTION' not in log and 'GdxRuntimeException' not in log,'No crash/missing font asset');all_logs.append(log);pid=''
    (OUT/'android-smoke.json').write_text(json.dumps({'checks':checks,'runs':runs,'method':'Studio Run; native menus for five preference cases; actual Android pointer gestures, Back, lifecycle and Home; no enemy/HP injection'},indent=2),encoding='utf-8')
    print('ANDROID TASK 04A.1 PASSED:',len(checks),'checks',flush=True)
finally:
    if pid:all_logs.append(logs())
    (OUT/'android-logcat.txt').write_text('\n'.join(all_logs),encoding='utf-8');adb('shell','am','force-stop',PKG)
    if original_pref is None:adb('shell','run-as',PKG,'rm','-f',PREF)
    else:write(PREF,original_pref)
    for name,value in flags.items():
        if value is None:adb('shell','run-as',PKG,'rm','-f','files/'+name)
        else:write('files/'+name,value)
    adb('shell','rm','-f','/data/local/tmp/task04a1-window.xml');adb('shell','am','start','-W','-n',HOME)
