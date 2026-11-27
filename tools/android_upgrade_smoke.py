"""Android Studio Run evidence: genuine unmodified match/Skill Box, then one-use diagnostics.
No game build, APK, install, direct level mutation or normal-game fixture commands.
"""
from pathlib import Path
import base64,json,math,re,sys,time,xml.etree.ElementTree as ET
from android_runtime import adb,read,AutomatedRun,PKG

ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task061';OUT.mkdir(parents=True,exist_ok=True)
PREF='shared_prefs/monster_survivor_prefs.xml';HOME=PKG+'/.android.HomeActivity';GAME=PKG+'/.android.AndroidLauncher'
pid=adb('shell','pidof',PKG).strip().split()[0];serial=0;run=None;checks=[];evidence=[];touch=[]
completed=False
paths=[PREF]+['files/'+n for n in ['skill-test.json','skill-command.json','boss-test.json','boss-command.json','combat-profile.json','map-seed.txt','camera-check.json','asset-visual-check']]
snapshot={p:read(p) for p in paths}
(OUT/'upgrade-android-backup.json').write_text(json.dumps({p:None if v is None else base64.b64encode(v).decode() for p,v in snapshot.items()},indent=2),encoding='utf-8')

def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','MatchBaseline:I','Skills:I','SkillDiagnostics:I','BossDiagnostics:I','DiagnosticGate:I','GameAudio:I','AndroidRuntime:E','*:S')
def wait(fn,seconds=20):
    end=time.monotonic()+seconds
    while time.monotonic()<end:
        v=fn()
        if v:return v
        time.sleep(.2)
    raise TimeoutError('Expected Android state')
def check(v,label):
    if not v:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def field(line,key):
    match=re.search(r'(?:^| )'+key+r'=([^ ]+)',line)
    if not match:raise AssertionError('Missing '+key+' in '+line)
    return match[1]
def tap(x,y):adb('shell','input','tap',str(round(x)),str(round(y)))
def tap_id(name):
    adb('shell','uiautomator','dump','/data/local/tmp/task061-window.xml')
    root=ET.fromstring(adb('exec-out','cat','/data/local/tmp/task061-window.xml'));node=next(n for n in root.iter('node') if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));tap((x+r)/2,(y+b)/2)
def capture(name):(OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p',binary=True))
def command(action):
    global serial
    serial+=1;run.write('skill-command.json',json.dumps({'serial':serial,'action':action}))
    return wait(lambda:(re.findall(r'status='+action+r' serial='+str(serial)+r'[^\n]*',logs()) or [None])[-1],12)
def state():return command('status')
def multi(skill,dx=0,dy=0,hold=200,tail=100):
    sx,sy=(1920,716) if skill=='DASH' else (2127,675)
    result=adb('shell','env','CLASSPATH=/data/local/tmp/task06-touch.dex','app_process','/','SkillTouch','multi','243','837',str(243+dx),str(837+dy),str(sx),str(sy),str(hold),str(tail))
    touch.append(result);print(result.strip(),flush=True)
def choose(type,expected,spawn=True):
    before=len(re.findall(r'Choice id=[^\n]*',logs()))
    if spawn:command('skillBox')
    wait(lambda:len(re.findall(r'Choice id=[^\n]*',logs()))>before if spawn else 'Choice id=' in logs())
    line=re.findall(r'Choice id=[^\n]*',logs())[-1];offers=field(line,'offers').split('|');index=next(i for i,v in enumerate(offers) if v.startswith(type+':'))
    old=logs().count('Upgraded type=');tap(1140,333+index*198);wait(lambda:logs().count('Upgraded type=')>old)
    result=re.findall(r'Upgraded type=[^\n]*',logs())[-1]
    check(field(result,'type')==type and int(field(result,'to'))==expected,'Android actual Skill Box commits '+type+' Lv'+str(expected))
    return result
def pause():
    n=logs().count('User pause=true');adb('shell','input','keyevent','4');wait(lambda:logs().count('User pause=true')>n)
    return re.findall(r'User pause=true[^\n]*',logs())[-1]
def resume():
    n=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>n)
def home():
    if 'User pause=true' not in logs().split('User pause=false')[-1]:pause()
    adb('shell','input','keyevent','36')

try:
    if '--diagnostic-only' not in sys.argv:
        # Official Demo selection shortens progress waiting, never skill cooldown or initial HP/equipment.
        for p in paths[1:]:check(read(p) is None,'no-fixture normal match: absent '+p)
        capture('upgrade-normal-home');tap_id('btn_play');capture('upgrade-normal-mode');tap_id('card_diff_easy');tap_id('card_mode_demo');tap_id('btn_start_game')
        wait(lambda:'MatchBaseline: Start ' in logs());time.sleep(1.2);capture('upgrade-normal-first-seconds')
        baseline=re.findall(r'MatchBaseline: Start [^\n]*',logs())[-1]
        check(all(s in baseline for s in ['diagnostics=none','debug=false','hp=10/10','weapons=SWORD:1 ','fixedSeed=false','progress=0.0','dashLevel=1','ultLevel=1','ultCd=15.0']),'normal Android match official baseline, no fixture or forced seed')
        box=wait(lambda:(re.findall(r'Box id=[^\n]*',logs()) or [None])[-1],20)
        progress=float(field(box,'progress'));check(35<=progress<46,'Skill Box spawns naturally after 35-45 progress seconds')
        px,py=map(float,field(box,'player').split(','));x,y=map(float,field(box,'item').split(','));dx,dy=x-px,y-py;distance=math.hypot(dx,dy)
        # First box is inside the generator's protected central clearing. Walk via the real joystick, with Dash.
        multi('DASH',150*dx/distance,-150*dy/distance,round((distance+15)/80*1000),0)
        wait(lambda:'Choice id=' in logs());capture('upgrade-normal-natural-box-modal')
        check('offers=DASH:1->2|ULTIMATE:1->2' in logs(),'natural pickup opens two upgrade offers')
        count=logs().count('Upgraded type=');tap(1140,333);wait(lambda:logs().count('Upgraded type=')>count)
        tap(1140,333);time.sleep(.4);capture('upgrade-normal-earned-lv2')
        accepted=re.findall(r'Upgraded type=[^\n]*',logs());check(len(accepted)==1 and 'type=DASH from=1 to=2' in accepted[-1] and 'weapons=SWORD:1' in accepted[-1],'natural Skill Box grants one Dash level and preserves weapon slots')
        check('LOCAL FIXTURE' not in logs() and 'Accepted one-use' not in logs(),'natural upgrade uses no diagnostic capability')
        check(read(PREF)==snapshot[PREF],'normal upgrade does not change preferences');home();time.sleep(.5);evidence.append(logs())
        if '--normal-only' in sys.argv:completed=True;print('Android natural-upgrade PASS',len(checks),flush=True);sys.exit(0)
    # Intentional, explicit diagnostic path; no root fixture files are created or removed.
    adb('shell','am','force-stop',PKG);run=AutomatedRun();run.write('skill-test.json',json.dumps({'protectedHp':True,'progress':600}));run.write('map-seed.txt','42')
    # Conflicting old fixture kinds within this run must not stack equipment/HP overrides.
    run.write('boss-test.json','{"holdHp":true,"weaponLevel":5,"progress":600}');run.write('combat-profile.json','{"worldProbe":true}')
    run.launch('HARD','DEMO');pid=adb('shell','pidof',PKG).strip().split()[0];serial=0
    wait(lambda:'LOCAL FIXTURE protectedHp=true' in logs());wait(lambda:'Boss event=FINAL_BOSS_APPEARS' in logs())
    check('Ready: HARD DEMO' in logs() and 'Accepted one-use' in logs(),'debug-only one-use launcher enables explicit Hard/Demo diagnostic')
    check(read(run.root+'/lease.json') is None,'diagnostic lease consumed before game initialization')
    current=state();check(field(current,'weapons')=='SWORD:1' and 'BossDiagnostics' not in logs(),'one fixture kind only, no stacked Fireball/Lightning loadout')
    choose('DASH',2);command('boxOnCast');multi('DASH',150,0,160,0);wait(lambda:'boxOnCast actual' in logs());before=state()
    check(field(before,'phase')=='CHOOSING_SKILL' and float(field(before,'dashActive'))>0,'real Skill Box freezes active Lv2 Dash')
    lastCast=re.findall(r'Activate type=DASH[^\n]*',logs())[-1];oldCd=float(field(before,'dashCd'));result=choose('DASH',3,False)
    check(abs(float(field(result,'dashCd'))-oldCd*4.2/4.6)<.01,'Android active upgrade preserves Dash cooldown ratio')
    time.sleep(.5);after=state();startX=float(field(lastCast,'player').split(',')[0]);endX=float(field(after,'player').split(',')[0]);check(abs(endX-startX-80)<1,'active Lv2 Dash completes old 80-unit snapshot after upgrading Lv3')
    capture('upgrade-diagnostic-dash-lv3');choose('ULTIMATE',2);choose('ULTIMATE',3);capture('upgrade-diagnostic-both-lv3')
    end=time.monotonic()+30
    while time.monotonic()<end:
        current=state()
        if float(field(current,'ultCd'))==0:break
        time.sleep(.6)
    check(float(field(current,'ultCd'))==0,'Lv3 Ultimate cooldown expires naturally')
    command('arena');command('boxOnCast');multi('ULTIMATE',0,0,100,0);wait(lambda:field(state(),'phase')=='CHOOSING_SKILL')
    before=state();check(float(field(before,'ultActive'))>0,'real Skill Box freezes active Lv3 Ultimate');oldCd=float(field(before,'ultCd'))
    result=choose('ULTIMATE',4,False);check(abs(float(field(result,'ultCd'))-oldCd*24/26)<.01,'Android Ultimate upgrade preserves cooldown ratio')
    wait(lambda:'Impact token=' in logs());impact=re.findall(r'Impact token=[^\n]*',logs())[-1]
    check(field(impact,'radius')=='125.0' and int(field(impact,'bossHits'))>=1 and int(field(impact,'knockbacks'))>=1,'Lv3 cast retains radius125 and hits real Boss/ordinary targets after upgrading Lv4')
    choose('DASH',4);choose('DASH',5);choose('ULTIMATE',5);capture('upgrade-diagnostic-both-lv5')
    command('skillBox');check('skillBox placement=false' in logs(),'max skills refuse additional Skill Box, no empty modal')
    current=state();check(field(current,'dashLevel')=='5' and field(current,'ultLevel')=='5' and field(current,'weapons')=='SWORD:1','both skills Lv5 without occupying weapon slots')
    old=logs().count('Activate type=DASH');multi('DASH',0,0,4000,300)
    check(logs().count('Activate type=DASH')==old+1 and 'joystick=0' in re.findall(r'Activate type=DASH[^\n]*',logs())[-1],'Lv5 Dash hold across 3.5s cooldown casts once and retains joystick owner')
    before=pause();time.sleep(.8);after=state();check(abs(float(field(before,'ultCd'))-float(field(after,'ultCd')))<.001,'Pause freezes upgraded cooldown')
    n=logs().count('Lifecycle resume:');adb('shell','input','keyevent','3');time.sleep(.5);adb('shell','am','start','-W','--activity-reorder-to-front','-n',PKG+'/.android.DiagnosticLauncher')
    wait(lambda:logs().count('Lifecycle resume:')>n);after=state();check(field(after,'paused')=='true' and field(after,'ultLevel')=='5','background keeps active diagnostic session paused with levels intact');resume()
    end=time.monotonic()+35
    while time.monotonic()<end:
        current=state()
        if float(field(current,'ultCd'))==0:break
        time.sleep(.8)
    check(float(field(current,'ultCd'))==0,'Lv5 cooldown becomes ready at 22s tuning')
    command('arena');old=logs().count('Impact token=');multi('ULTIMATE',0,0,300,100);wait(lambda:logs().count('Impact token=')>old)
    impact=re.findall(r'Impact token=[^\n]*',logs())[-1];check(field(impact,'radius')=='145.0' and int(field(impact,'bossHits'))>=1,'actual two-pointer Lv5 Ultimate radius145 hits Boss through guarded damage path')
    old=logs().count('Activate type=ULTIMATE');multi('ULTIMATE',0,0,200,100);check(logs().count('Activate type=ULTIMATE')==old,'Lv5 hold/repeated touch cannot bypass cooldown');capture('upgrade-diagnostic-lv5-after-impact');time.sleep(6)
    command('playerDeath');wait(lambda:'Game Over seed=' in logs(),25);adb('shell','input','keyevent','46');wait(lambda:'Retry seed=' in logs());current=state()
    check(field(current,'dashLevel')=='1' and field(current,'ultLevel')=='1' and 12<float(field(current,'ultCd'))<=15,'diagnostic Retry resets skill levels and initial cooldown')
    check('FATAL EXCEPTION' not in logs() and 'Playback failed' not in logs(),'no Android crash/audio failure in explicit upgrade/Boss test');evidence.append(logs())
    # Replay the same stale test Intent with its consumed lease. It must fail closed into baseline gameplay.
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/.android.DiagnosticLauncher','--es','diagnostic_run',run.id,'--es','diagnostic_token',run.token)
    pid=adb('shell','pidof',PKG).strip().split()[0];wait(lambda:'MatchBaseline: Start ' in logs());baseline=re.findall(r'MatchBaseline: Start [^\n]*',logs())[-1]
    check('Rejected diagnostic launch' in logs() and all(v in baseline for v in ['diagnostics=none','hp=10/10','weapons=SWORD:1 ','fixedSeed=false','progress=0.0']),'stale diagnostic Intent and remaining namespace cannot enable test data');evidence.append(logs())
    completed=True;print('Android upgrades PASS',len(checks),flush=True)
finally:
    try:evidence.append(logs())
    except Exception:pass
    (OUT/'upgrade-android.log').write_text('\n'.join(evidence),encoding='utf-8');(OUT/'upgrade-touch.log').write_text('\n'.join(touch),encoding='utf-8')
    (OUT/'upgrade-android-checks.json').write_text(json.dumps({'pass':completed,'count':len(checks),'checks':checks},indent=2),encoding='utf-8')
    adb('shell','am','force-stop',PKG)
    if run:run.cleanup()
    # This runner changes only its UUID namespace. Never overwrite/delete a concurrently-created user file.
    unchanged={p:read(p)==v for p,v in snapshot.items()};(OUT/'upgrade-restoration.json').write_text(json.dumps(unchanged,indent=2))
    if not all(unchanged.values()):raise AssertionError('External app state changed; preserved for inspection')
    adb('shell','am','start','-W','-n',HOME);print('Restored exact app state, removed only owned diagnostic namespace, normal Home',flush=True)
