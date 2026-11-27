"""Focused live Boss checks on the Android Studio Run build. No build/install/export commands.
Uses local opt-in HP protection/equipment. Demo uses the unshifted 45/90-second clock;
Hard Normal explicitly uses clock fixtures. Screenshots are local evidence, not AI art input.
"""
from pathlib import Path
from android_runtime import retired_root_fixture_runner
retired_root_fixture_runner()
import json,re,subprocess,time,sys,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task05';OUT.mkdir(parents=True,exist_ok=True)
ADB=str(Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe');PKG='com.example.monstersurvivor'
HOME=PKG+'/.android.HomeActivity';GAME=PKG+'/.android.AndroidLauncher';PREF='shared_prefs/monster_survivor_prefs.xml'
checks=[];runs=[];pid='';all_logs=[];serial=0
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def write(path,value):subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > {path}'"],input=value,text=True,encoding='utf-8',capture_output=True,check=True)
def exists(path):
    folder,name=path.rsplit('/',1)
    return name in adb('shell','run-as',PKG,'ls',folder).splitlines()
def check(value,label):
    if not value:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','BossDiagnostics:I','AndroidRuntime:E','*:S')
def wait(fn,seconds=15):
    end=time.monotonic()+seconds
    while time.monotonic()<end:
        value=fn()
        if value:return value
        time.sleep(.3)
    raise TimeoutError('Android expected telemetry missing')
def tap(x,y):adb('shell','input','tap',str(round(x)),str(round(y)))
def tap_id(name):
    path='/data/local/tmp/task05-window.xml';adb('shell','uiautomator','dump',path)
    root=ET.fromstring(adb('exec-out','cat',path));node=next(n for n in root.iter('node') if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));tap((x+r)/2,(y+b)/2)
def capture(name):
    r=subprocess.run([ADB,'exec-out','screencap','-p'],capture_output=True,check=True);(OUT/(name+'.png')).write_bytes(r.stdout)
def settings(code):
    root=ET.fromstring(original_pref) if original_pref else ET.Element('map')
    for n in list(root):
        if n.get('name')=='game_language':root.remove(n)
    ET.SubElement(root,'string',name='game_language').text=code;write(PREF,ET.tostring(root,encoding='unicode',xml_declaration=True))
def launch(code,difficulty,mode,progress=0):
    global pid,serial
    adb('shell','am','force-stop',PKG);settings(code);serial=0
    write('files/map-seed.txt','42');write('files/boss-test.json',json.dumps({'progress':progress,'weaponLevel':3,'holdHp':True}))
    adb('shell','run-as',PKG,'rm','-f','files/combat-profile.json','files/boss-command.json')
    adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    for name in ['btn_play','card_diff_'+difficulty.lower(),'card_mode_'+mode.lower(),'btn_start_game']:tap_id(name)
    wait(lambda:'LOCAL FIXTURE' in logs());check('Ready: '+difficulty+' '+mode in logs() and 'language='+code in logs(),'native '+difficulty+'/'+mode+'/'+code+' bridge')
def command(action,value=None):
    global serial
    serial+=1;body={'serial':serial,'action':action}
    if value is not None:body['value']=value
    write('files/boss-command.json',json.dumps(body));wait(lambda:'serial='+str(serial)+' ' in logs() or 'serial='+str(serial)+'\n' in logs(),8)
def pause():
    count=logs().count('User pause=true');adb('shell','input','keyevent','4');wait(lambda:logs().count('User pause=true')>count)
    return re.findall(r'User pause=true[^\n]*',logs())[-1]
def resume():
    count=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>count)
    return re.findall(r'User pause=false[^\n]*',logs())[-1]
def at_home():return re.search(r'(?:topResumedActivity|ResumedActivity)[:=].*\.HomeActivity',adb('shell','dumpsys','activity','activities'))
original_pref=adb('exec-out','run-as',PKG,'cat',PREF) if exists(PREF) else None
flags={name:adb('exec-out','run-as',PKG,'cat','files/'+name) if exists('files/'+name) else None for name in ['map-seed.txt','combat-profile.json','boss-test.json','boss-command.json']}
(OUT/'android-state-backup.json').write_text(json.dumps({'pref':original_pref,'flags':flags}),encoding='utf-8')
try:
    if '--hard-only' not in sys.argv and '--terminal-only' not in sys.argv:
        launch('vi','EASY','DEMO')
        print('Waiting for natural Demo Mini event (~45 gameplay seconds)',flush=True)
        wait(lambda:'Boss event=MINI_BOSS_APPEARS' in logs(),65);capture('android-demo-mini-announcement')
        mini=re.search(r'Boss event=MINI_BOSS_APPEARS progress=([\d.]+) elapsed=([\d.]+)',logs());check(300<=float(mini[1])<301 and 45<=float(mini[2])<45.2,'natural Demo Mini at 45 real gameplay seconds')
        wait(lambda:'VISIBLE actor=boss_mini_' in logs(),15);capture('android-demo-mini-visible');check(True,'Mini rendered inside actual gameplay camera')
        before=pause();time.sleep(1);after=resume();check(re.search(r'time=([^ ]+)',before)[1]==re.search(r'time=([^ ]+)',after)[1],'Boss pause freezes elapsed and event scheduler')
        adb('shell','input','keyevent','3');time.sleep(.4);adb('shell','am','start','-W','--activity-reorder-to-front','-n',GAME)
        wait(lambda:'Lifecycle resume: waiting for explicit resume' in logs());resume();check(logs().count('Boss event=MINI_BOSS_APPEARS')==1,'Activity resume retains exactly one Mini')
        print('Waiting for natural Demo Lich event (~90 gameplay seconds)',flush=True)
        wait(lambda:'Boss event=FINAL_BOSS_APPEARS' in logs(),65);capture('android-demo-final-announcement')
        final=re.search(r'Boss event=FINAL_BOSS_APPEARS progress=([\d.]+) elapsed=([\d.]+)',logs());check(600<=float(final[1])<601 and 90<=float(final[2])<90.2,'natural Demo Final at 90 real gameplay seconds')
        wait(lambda:'VISIBLE actor=boss_lich_lord' in logs(),15);capture('android-demo-final-visible');check(True,'Lich rendered inside actual gameplay camera')
        time.sleep(12);check('Victory result=' not in logs(),'live Final continues beyond 90 seconds')
        for i in range(3):command('phase2');time.sleep(.4)
        wait(lambda:'Boss event=BOSS_PHASE_TWO' in logs());capture('android-demo-phase2');time.sleep(14)
        check(logs().count('Boss event=BOSS_PHASE_TWO')==1,'single Lich phase 2 transition on Android')
        for i in range(8):
            if 'Victory result=' in logs():break
            command('defeat');time.sleep(.4)
        wait(lambda:'Victory result=' in logs());capture('android-demo-victory');check('finalBossDefeated=true' in logs() and 'victory=true' in logs(),'real actor death produces actual Victory result (diagnostic damage)')
        count=logs().count('Retry seed=');tap(1140,612);wait(lambda:logs().count('Retry seed=')>count);check('Retry seed=42' in logs(),'Victory Retry retains fixed diagnostic seed')
        time.sleep(1);state=pause();check('phase=RUNNING' in state and 'progress=' in state,'Retry resets clock, Boss and terminal state')
        tap(1140,783);wait(at_home);check(True,'Victory Retry -> Pause -> Native Home')
        data=logs();all_logs.append(data);runs.append({'case':'Easy Demo vi','clock':'natural 0 -> 300 -> 600, HP protected, equipment 3; phase/death use explicit commands','mini':mini.group(0),'final':final.group(0)})
    if '--terminal-only' in sys.argv:
        launch('en','HARD','NORMAL',600);time.sleep(3)
    else:
        launch('en','HARD','NORMAL',290)
        wait(lambda:'Boss event=MINI_BOSS_APPEARS' in logs(),20);capture('android-hard-mini');time.sleep(14)
        command('progress',599.99);wait(lambda:'Boss event=FINAL_BOSS_APPEARS' in logs(),6);capture('android-hard-final');time.sleep(14)
        for i in range(3):command('phase2');time.sleep(.4)
        wait(lambda:'Boss event=BOSS_PHASE_TWO' in logs());time.sleep(14);capture('android-hard-phase2')
    command('playerDeath');wait(lambda:'Game Over seed=' in logs());capture('android-hard-gameover');check('Victory result=' not in logs(),'Hard Normal player death before Final remains GameOver')
    adb('shell','input','keyevent','46');wait(lambda:'Retry seed=' in logs());time.sleep(.4);state=pause();check('hp=100000/100000' in state and 'phase=RUNNING' in state,'GameOver Retry clears old attacks and preserves diagnostic difficulty/mode/language')
    if '--terminal-only' in sys.argv:
        resume()
        for i in range(7):
            if 'Victory result=' in logs():break
            command('defeat');time.sleep(.4)
        wait(lambda:'Victory result=' in logs());capture('android-final-victory')
        check('finalBossDefeated=true' in logs(),'final Studio build actual actor Death -> Victory')
        count=logs().count('Retry seed=');adb('shell','input','keyevent','46');wait(lambda:logs().count('Retry seed=')>count);time.sleep(.4);state=pause()
        check('hp=100000/100000' in state and 'phase=RUNNING' in state,'Victory Retry ignores stale diagnostic commands and resets actual encounter')
    tap(1140,783);wait(at_home);activities=adb('shell','dumpsys','activity','activities');check(len(re.findall(r'Hist\s+#\d+: ActivityRecord\{[^\n]*\.HomeActivity',activities))==1,'Home reuses single native Home Activity')
    data=logs();all_logs.append(data);runs.append({'case':'Hard Normal en','clock':('explicit progress600 terminal fixture' if '--terminal-only' in sys.argv else 'explicit progress290/599.99 fixtures')+'; real Boss AI/render; HP protected and equipment3'})
    check(all('FATAL EXCEPTION' not in log and 'GdxRuntimeException' not in log for log in all_logs),'no render/missing-asset/Android exception')
    (OUT/('android-terminal-results.json' if '--terminal-only' in sys.argv else 'android-boss-results.json')).write_text(json.dumps({'checks':checks,'runs':runs,'method':'Android Studio Run on Pixel_4; no standalone packaging/install'},indent=2),encoding='utf-8')
    print('ANDROID BOSS PASSED',len(checks),flush=True)
finally:
    if pid:all_logs.append(logs())
    (OUT/('android-terminal-logcat.txt' if '--terminal-only' in sys.argv else 'android-hard-logcat.txt' if '--hard-only' in sys.argv else 'android-boss-logcat.txt')).write_text('\n'.join(all_logs),encoding='utf-8');adb('shell','am','force-stop',PKG)
    if original_pref is None:adb('shell','run-as',PKG,'rm','-f',PREF)
    else:write(PREF,original_pref)
    for name,value in flags.items():
        if value is None:adb('shell','run-as',PKG,'rm','-f','files/'+name)
        else:write('files/'+name,value)
    adb('shell','rm','-f','/data/local/tmp/task05-window.xml');adb('shell','am','start','-W','-n',HOME)
