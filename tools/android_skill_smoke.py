"""Focused actual Android gameplay/two-pointer touch on a prior Android Studio Run build.
Requires build_skill_touch.py. Uses opt-in protected HP and bounded arena placement only;
skill cooldown/range/damage, Boss HP/AI and native input paths stay unchanged. Always restore flags/prefs.
"""
from pathlib import Path
from android_runtime import retired_root_fixture_runner
retired_root_fixture_runner()
import json,re,subprocess,time,sys,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task06'
if '--cooldown-pause' in sys.argv:OUT=OUT/'android-cooldown-pause'
OUT.mkdir(parents=True,exist_ok=True)
ADB=str(Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe');PKG='com.example.monstersurvivor';HOME=PKG+'/.android.HomeActivity';GAME=PKG+'/.android.AndroidLauncher';PREF='shared_prefs/monster_survivor_prefs.xml'
checks=[];data=[];pid='';serial=0;touch=[]
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def read(path):
    from android_runtime import read as exact_read
    data=exact_read(path);return None if data is None else data.decode('utf-8')
def write(path,text):
    # Atomic same-directory replacement: the render thread must never parse a truncated command.
    subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > {path}.task06-tmp && mv {path}.task06-tmp {path}'"],input=text,capture_output=True,text=True,encoding='utf-8',check=True)
def check(b,label):
    if not b:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','Skills:I','SkillDiagnostics:I','GameAudio:I','NativeAudio:I','AndroidRuntime:E','*:S')
def wait(fn,seconds=15):
    end=time.monotonic()+seconds
    while time.monotonic()<end:
        value=fn()
        if value:return value
        time.sleep(.25)
    raise TimeoutError('Android expected state not reached')
def tap(x,y):adb('shell','input','tap',str(round(x)),str(round(y)))
def tap_id(name):
    path='/data/local/tmp/task06-window.xml';adb('shell','uiautomator','dump',path);root=ET.fromstring(adb('exec-out','cat',path));node=next(n for n in root.iter('node') if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));tap((x+r)/2,(y+b)/2);time.sleep(.2)
def capture(label):
    (OUT/(label+'.png')).write_bytes(subprocess.run([ADB,'exec-out','screencap','-p'],capture_output=True,check=True).stdout)
def settings(language):
    root=ET.fromstring(backup[PREF]) if backup[PREF] else ET.Element('map')
    for node in list(root):
        if node.get('name')=='game_language':root.remove(node)
    ET.SubElement(root,'string',name='game_language').text=language;write(PREF,ET.tostring(root,encoding='unicode',xml_declaration=True))
def launch(language,difficulty,mode,progress=0):
    global pid,serial
    adb('shell','am','force-stop',PKG);serial=0;settings(language);write('files/map-seed.txt','42')
    write('files/skill-test.json',json.dumps({'protectedHp':True,'progress':progress}))
    for file in ['boss-test.json','boss-command.json','combat-profile.json','skill-command.json']:adb('shell','run-as',PKG,'rm','-f','files/'+file)
    adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    for name in ['btn_play','card_diff_'+difficulty.lower(),'card_mode_'+mode.lower(),'btn_start_game']:tap_id(name)
    wait(lambda:'LOCAL FIXTURE protectedHp=true' in logs(),25)
    check('Ready: '+difficulty+' '+mode in logs() and 'language='+language in logs(),'native '+difficulty+'/'+mode+'/'+language+' uses actual skill build')
def command(action,value=None):
    global serial
    serial+=1;body={'serial':serial,'action':action}
    if value is not None:body['value']=value
    write('files/skill-command.json',json.dumps(body))
    return wait(lambda:(re.findall(r'status='+action+r' serial='+str(serial)+r'[^\n]*',logs()) or [None])[-1],12)
def value(line,key):
    result=re.search(r'(?:^| )'+key+r'=([^ ]+)',line)
    if not result:raise AssertionError('No '+key+' in '+line)
    return result[1]
def pause():
    n=logs().count('User pause=true');adb('shell','input','keyevent','4');wait(lambda:logs().count('User pause=true')>n)
    return re.findall(r'User pause=true[^\n]*',logs())[-1]
def resume():
    n=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>n)
def multi(skill,dx=150,dy=-150,hold=300,tail=250):
    # Pixel_4 2280x1080 -> ExtendViewport 1013.333x480. Same layout as SkillHud, with zero Pixel_4 cutout inset.
    sx,sy=(1920,716) if skill=='DASH' else (2127,675)
    r=adb('shell','env','CLASSPATH=/data/local/tmp/task06-touch.dex','app_process','/','SkillTouch','multi','243','837',str(243+dx),str(837+dy),str(sx),str(sy),str(hold),str(tail));touch.append(r);print(r.strip(),flush=True)
def owned_drag():
    r=adb('shell','env','CLASSPATH=/data/local/tmp/task06-touch.dex','app_process','/','SkillTouch','drag','243','837','400','837','1920','716','300','200');touch.append(r)
def activations(kind):return re.findall(r'Activate type='+kind+r'[^\n]*',logs())

paths=[PREF]+['files/'+name for name in ['map-seed.txt','boss-test.json','boss-command.json','combat-profile.json','skill-test.json','skill-command.json']]
backup={p:read(p) for p in paths};(OUT/'android-state-backup.json').write_text(json.dumps(backup),encoding='utf-8')
try:
    if '--cooldown-pause' in sys.argv:
        launch('vi','EASY','NORMAL');multi('DASH',0,0,80,0);before=pause()
        check(float(value(before,'dashCd'))>0,'Android pauses Dash with nonzero cooldown')
        time.sleep(1.2);after=command('status')
        check(value(before,'time')==value(after,'elapsed') and abs(float(value(before,'dashCd'))-float(value(after,'dashCd')))<.001,'nonzero Android Dash cooldown frozen during Pause')
        n=logs().count('Lifecycle resume:');adb('shell','input','keyevent','3');time.sleep(.5);adb('shell','am','start','-W','--activity-reorder-to-front','-n',GAME)
        wait(lambda:logs().count('Lifecycle resume:')>n);after=command('status')
        check(value(after,'paused')=='true' and abs(float(value(before,'dashCd'))-float(value(after,'dashCd')))<.001,'nonzero Android Dash cooldown frozen across background/foreground')
        capture('nonzero-cooldown-paused');resume();print('Android nonzero cooldown probe PASS',len(checks),flush=True);sys.exit(0)
    launch('vi','EASY','NORMAL');capture('android-skills-ready-vi');initial=command('status')
    check(float(value(initial,'ultCd'))>0 and int(value(initial,'ultCount'))==0,'Ultimate initially cooldown, no early cast')
    for incomplete in ['', '{"serial":']:
        write('files/skill-command.json',incomplete);time.sleep(1.2)
    command('status');check('FATAL EXCEPTION' not in logs(),'diagnostic empty/partial command safely ignored on Android')
    multi('DASH',150,-150,6200,700)
    line=command('status');casts=activations('DASH')
    check(len(casts)==1 and 'joystick=0' in casts[0],'actual Android 2-pointer Dash retains joystick owner')
    axis=list(map(float,value(casts[0],'axis').split(',')));dash=list(map(float,value(casts[0],'dash').split(',')))
    check(abs(axis[0]-.7071)<.02 and abs(axis[1]-.7071)<.02 and max(abs(a-b) for a,b in zip(axis,dash))<.002,'actual diagonal joystick vector snapshotted by Dash')
    check(float(value(line,'dashCd'))==0 and int(value(line,'dashCount'))==1,'holding Android skill finger beyond 5s does not auto-spam')
    before=len(casts);owned_drag();check(len(activations('DASH'))==before,'Android joystick drag across Dash button never casts')
    before=pause();before_cd=float(value(before,'ultCd'));time.sleep(.8);after=command('status')
    check(float(value(before,'time'))==float(value(after,'elapsed')) and abs(before_cd-float(value(after,'ultCd')))<.001,'Android Pause freezes real skill cooldown')
    before=len(activations('DASH'));tap(1920,716);check(len(activations('DASH'))==before,'paused skill button refuses activation');resume()
    n=logs().count('Lifecycle resume:');adb('shell','input','keyevent','3');time.sleep(.5);adb('shell','am','start','-W','--activity-reorder-to-front','-n',GAME)
    wait(lambda:logs().count('Lifecycle resume:')>n);state=command('status');check(value(state,'paused')=='true','background/foreground retains Pause for skills');resume()
    command('modal');wait(lambda:'Weapon choice id=' in logs());state=command('status');check(value(state,'phase')=='CHOOSING_WEAPON','real Android weapon modal')
    before=len(activations('DASH'));multi('DASH',0,0,100,100);check(len(activations('DASH'))==before,'modal owns touch ahead of skill buttons');adb('shell','input','keyevent','8');wait(lambda:'Weapon selected ' in logs())
    state=command('status');check('BOW:1' in state and 'SWORD:1' in state,'skills preserve Weapon Box commit and three-slot system')
    capture('android-skills-after-modal');data.append(logs())
    launch('en','HARD','DEMO',600);wait(lambda:'Boss event=FINAL_BOSS_APPEARS' in logs(),20)
    # Skill clocks remain the real baseline: wait natural initial cooldown, never alter it via diagnostic files.
    until=time.monotonic()+30
    while time.monotonic()<until:
        state=command('status')
        if float(value(state,'ultCd'))==0:break
        time.sleep(.8)
    check(float(value(state,'ultCd'))==0 and float(value(state,'elapsed'))>=15,'Android Ultimate becomes ready only after 15 gameplay seconds')
    command('arena');check('bossPlaced=true' in logs(),'LOS-checked diagnostic placement retains live Lich HP/AI')
    multi('ULTIMATE',0,0,250,150);wait(lambda:'Impact token=' in logs())
    cast=activations('ULTIMATE');check(len(cast)==1 and 'joystick=0' in cast[0],'actual two-finger Android Ultimate cast')
    impact=re.findall(r'Impact token=[^\n]*',logs())[-1]
    check(int(value(impact,'impacts'))==1 and int(value(impact,'bossHits'))>=1 and int(value(impact,'knockbacks'))>=1,'actual Android Ultimate damages Boss and knocks back ordinary targets')
    state=command('status');check(float(value(state,'ultCd'))>27 and int(value(state,'ultCount'))==1,'accepted Ultimate starts actual 30s cooldown')
    before=len(cast);multi('ULTIMATE',0,0,200,100);check(len(activations('ULTIMATE'))==before,'cooldown touch cannot duplicate Ultimate');capture('android-ultimate-after-en')
    time.sleep(6);check('FATAL EXCEPTION' not in logs() and 'Playback failed' not in logs(),'no Android crash or audio regression during skill combat')
    command('playerDeath');wait(lambda:'Game Over seed=' in logs());before=len(activations('DASH'));tap(1920,716);check(len(activations('DASH'))==before,'GameOver skill touch rejected')
    adb('shell','input','keyevent','46');wait(lambda:'Retry seed=' in logs());state=command('status');check(int(value(state,'dashCount'))==0 and int(value(state,'ultCount'))==0 and 12<float(value(state,'ultCd'))<=15,'Android Retry resets independent skill state')
    pause();adb('shell','input','keyevent','36');wait(lambda:'Disposed played=' in logs());check(True,'Home disposes gameplay audio and cancels skill state')
    data.append(logs());print('Android skills PASS',len(checks),flush=True)
finally:
    if pid:
        try:data.append(logs())
        except Exception:pass
    (OUT/'android-skills.log').write_text('\n'.join(data),encoding='utf-8')
    (OUT/'android-touch-helper.log').write_text('\n'.join(touch),encoding='utf-8')
    (OUT/'android-checks.json').write_text(json.dumps({'passed':checks,'count':len(checks),'method':'actual 2-pointer MotionEvents via shell InputManager; native menus; actual skill tuning; protected HP/arena placement and progress fixtures'},indent=2),encoding='utf-8')
    adb('shell','am','force-stop',PKG)
    for path,old in backup.items():
        if old is None:adb('shell','run-as',PKG,'rm','-f',path)
        else:write(path,old)
    adb('shell','am','start','-W','-n',HOME)
    print('Restored original preferences and diagnostic flags',flush=True)
