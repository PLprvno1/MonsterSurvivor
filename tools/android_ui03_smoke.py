"""UI03 Android UI/input evidence. Uses existing Studio deployment and one-use Task06.1 capability.
Never installs/builds an APK, writes root fixtures/preferences or directly grants weapon levels.
"""
from pathlib import Path
import json,re,time,base64,sys
from android_runtime import adb,read,AutomatedRun,PKG
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/ui03';OUT.mkdir(parents=True,exist_ok=True)
paths=['shared_prefs/monster_survivor_prefs.xml']+['files/'+n for n in ['skill-test.json','skill-command.json','boss-test.json','boss-command.json','combat-profile.json','map-seed.txt','camera-check.json','asset-visual-check']]
snapshot={p:read(p) for p in paths};(OUT/'android-original-state.json').write_text(json.dumps({p:None if b is None else base64.b64encode(b).decode() for p,b in snapshot.items()},indent=2))
run=None;pid='0';serial=0;checks=[];evidence=[];completed=False
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','MatchBaseline:I','WeaponRewards:I','SkillDiagnostics:I','EvolutionDiagnostics:I','DiagnosticGate:I','Skills:I','GameAudio:I','AndroidRuntime:E','*:S')
def wait(fn,seconds=20):
    end=time.monotonic()+seconds
    while time.monotonic()<end:
        v=fn()
        if v:return v
        time.sleep(.15)
    raise TimeoutError('Expected Android state')
def check(v,m):
    if not v:raise AssertionError(m)
    checks.append(m);print('PASS',m,flush=True)
def field(s,k):
    m=re.search(r'(?:^| )'+k+r'=(\S+)',s)
    if not m:raise AssertionError(k+' missing '+s)
    return m[1]
def command(action,**values):
    global serial
    serial+=1;run.write('skill-command.json',json.dumps(dict(serial=serial,action=action,**values)))
    return wait(lambda:(re.findall('status='+action+' serial='+str(serial)+r'[^\n]*',logs()) or [None])[-1],15)
def status():
    command('status');return re.findall(r'EvolutionDiagnostics: status=status serial='+str(serial)+r'[^\n]*',logs())[-1]
def capture(name):(OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p',binary=True))
def tap(x,y):adb('shell','input','tap',str(x),str(y))
def box():
    old=logs().count('WeaponRewards: Choice ');command('weaponBox');wait(lambda:logs().count('WeaponRewards: Choice ')>old)
    line=re.findall(r'WeaponRewards: Choice [^\n]*',logs())[-1];return field(line,'offers').split('|'),field(line,'token')
def choose(offers,index,token):
    capture('android-before-choice-'+token);time.sleep(.3)
    def selected():return (re.findall(r'Weapon selected [^\n]*',logs()) or [None])[-1]
    old=selected();tap(1140,333+index*198)
    try:wait(lambda:selected() and selected()!=old)
    except Exception:capture('android-failed-choice-'+token);raise
    line=re.findall(r'Weapon selected [^\n]*',logs())[-1];check('loadout=' in line,'actual Android touch commits '+offers[index]+' token='+token)
    tap(1140,333+index*198);time.sleep(.1);check(selected()==line,'double tap cannot repeat reward')
    return field(line,'loadout')
def earn(type):
    for attempt in range(35):
        offers,token=box();index=next((i for i,v in enumerate(offers) if v.startswith(type+':')),None)
        if index is None:index=0
        if offers[index].startswith(type+':EVOLUTION:'):
            capture('android-offer-'+type)
            check(':5->5' in offers[index],'eligible evolution retains Lv5 '+type)
            # Actual pause/resume over a pending evolution modal; underlying item/token survives.
            old=logs().count('User pause=true');adb('shell','input','keyevent','4');wait(lambda:logs().count('User pause=true')>old)
            time.sleep(.25);old=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>old)
            loadout=choose(offers,index,token);check(type+':5:EVOLVED' in loadout,'earned evolution '+type)
            return
        choose(offers,index,token)
    raise AssertionError('Progression did not reach '+type)
def shoot_capture(type):
    command('evolutionTargets')
    # Series captures make projectile, impact and persistent ground independently reviewable.
    for i in range(18 if type=='LIGHTNING' else 8):capture('android-vfx-'+type+'-'+str(i));time.sleep(.02 if type=='LIGHTNING' else .12)
    value=status();check(int(field(value,'projectiles'))<=48 and int(field(value,'effects'))<=48 and int(field(value,'zones'))<=3,'Android bounded combat '+type)
def sample(label,seconds=11):
    value=status();start=logs().count('window frames=120');time.sleep(seconds);value=status();capture('android-scene-'+label)
    windows=re.findall(r'window frames=120[^\n]*',logs())[start:]
    (OUT/('scene-'+label+'.json')).write_text(json.dumps({'state':value,'windows':windows,'newWindows':len(windows),'seconds':seconds},indent=2))
    check(int(field(value,'normal'))<=60 and int(field(value,'bosses'))<=2,'resident budgets '+label)
def retry():
    def terminal():
        command('status');phase=re.findall(r'SkillDiagnostics: status=status serial='+str(serial)+r'[^\n]*',logs())[-1]
        return field(phase,'phase') in ('VICTORY','GAME_OVER')
    if not terminal():command('playerDeath')
    # A pending real Lich kill can win between status and playerDeath. Accept either terminal state.
    wait(terminal,30)
    old=logs().count('Retry seed=');tap(1140,612);wait(lambda:logs().count('Retry seed=')>old)
    value=status();check(field(value,'weapons')=='SWORD:1','Android Retry clears evolution and restores Sword1')
def skill_status():
    command('status');return re.findall(r'SkillDiagnostics: status=status serial='+str(serial)+r'[^\n]*',logs())[-1]

def phase():return field(skill_status(),'phase')

def multi(type,x,y):
    before=field(skill_status(),'dashCount' if type=='DASH' else 'ultCount')
    result=adb('shell','env','CLASSPATH=/data/local/tmp/task06-touch.dex','app_process','/','SkillTouch','multi','243','837','300','837',str(x),str(y),'250','100')
    (OUT/('android-touch-'+type+'.log')).write_text(result)
    after=skill_status();check(int(field(after,'dashCount' if type=='DASH' else 'ultCount'))==int(before)+1,'real two-pointer '+type+' activates once')
    check(re.search(r'Activate type='+type+r'[^\n]*joystick=0',logs()) is not None,'joystick pointer retained for '+type)

try:
    adb('shell','am','force-stop',PKG);run=AutomatedRun();run.write('skill-test.json','{"protectedHp":true,"progress":0}');run.write('map-seed.txt','42');run.launch('HARD','NORMAL')
    pid=adb('shell','pidof',PKG).strip().split()[0];wait(lambda:'LOCAL FIXTURE protectedHp=true' in logs());time.sleep(1)
    check('Accepted one-use' in logs() and read(run.root+'/lease.json') is None,'explicit debug-only one-use capability consumed')
    check(field(status(),'weapons')=='SWORD:1','diagnostic has baseline loadout')
    if '--boss-only' in sys.argv:
        command('progress',value=600);wait(lambda:int(field(status(),'lichHp'))>0);capture('android-final-boss-phase1')
        for i in range(3):
            if field(status(),'lichPhase')=='2':break
            command('phase2');time.sleep(.3)
        wait(lambda:field(status(),'lichPhase')=='2');capture('android-final-boss-phase2');check(field(status(),'lichPhase')=='2','final build Boss phase2 and direction indicators')
        for i in range(12):
            if phase() in ('WINNING','VICTORY'):break
            command('phase2');time.sleep(.3)
        wait(lambda:phase()=='VICTORY',25);capture('android-final-victory');check('Victory result=MatchResult' in logs(),'final build actual Victory')
        retry();old=logs().count('User pause=true');tap(2136,78);wait(lambda:logs().count('User pause=true')>old);capture('android-final-pause')
        old=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>old);check(phase()=='RUNNING','final build actual Pause Resume touch')
        time.sleep(6)
    else:
        multi('DASH',1920,716);capture('android-ui03-skill-cooldown')
        for type in ['DASH','ULTIMATE']:
            for level in range(2,6):
                old=len(re.findall(r'Skills\s*: Choice id=',logs()));command('skillBox');wait(lambda:len(re.findall(r'Skills\s*: Choice id=',logs()))>old)
                choice=re.findall(r'Skills\s*: Choice id=[^\n]*',logs())[-1];check(int(field(choice,'token'))<0 and type+':'+str(level-1)+'->'+str(level) in choice,'actual eligible skill offer and negative token')
                if type=='ULTIMATE':check('DASH' not in choice,'max Dash absent from eligible offers')
                before=skill_status();tap(150,500);time.sleep(.15);check(phase()=='CHOOSING_SKILL','outside card leaves modal pending')
                capture('android-ui03-skill-modal-'+type+'-'+str(level));time.sleep(.3);tap(1140,333)
                wait(lambda:field(skill_status(),'dashLevel' if type=='DASH' else 'ultLevel')==str(level))
                check(field(status(),'weapons')=='SWORD:1','skill modal preserves weapon slots')
        wait(lambda:float(field(skill_status(),'ultCd'))==0,30);multi('ULTIMATE',2127,675);capture('android-ui03-skills-max')
        # Real boxes/touch for evolution; no grant/evolve shortcuts.
        offers,token=box();choose(offers,next(i for i,v in enumerate(offers) if v.startswith('BOW:')),token)
        earn('SWORD');capture('android-ui03-evolved-sword');earn('BOW');capture('android-ui03-two-evolved');sample('ui03-two-evolved',7)
        command('progress',value=300);wait(lambda:int(field(status(),'bosses'))>=1);capture('android-ui03-mini-night')
        command('progress',value=600);wait(lambda:int(field(status(),'lichHp'))>0);capture('android-ui03-lich-phase1')
        for i in range(3):
            if field(status(),'lichPhase')=='2':break
            command('phase2');time.sleep(.3)
        wait(lambda:field(status(),'lichPhase')=='2');capture('android-ui03-lich-phase2')
        # Existing diagnostic damage command still passes the Boss burst guard and real death lifecycle.
        for i in range(12):
            if phase() in ('WINNING','VICTORY'):break
            command('phase2');time.sleep(.3)
        wait(lambda:phase()=='VICTORY',25);capture('android-ui03-victory');check('Victory result=MatchResult' in logs(),'actual Victory result rendered')
        retry();check(field(skill_status(),'dashLevel')=='1' and field(skill_status(),'ultLevel')=='1','Retry clears skill levels')
        command('playerDeath');wait(lambda:phase()=='GAME_OVER',25);capture('android-ui03-game-over');retry()
        before=skill_status();old=logs().count('User pause=true');tap(2136,78);wait(lambda:logs().count('User pause=true')>old);capture('android-ui03-pause')
        tap(1920,716);time.sleep(.2);after=skill_status();check(field(before,'dashCount')==field(after,'dashCount'),'Pause blocks skill touch')
        old=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>old)
    evidence.append(logs());check('FATAL EXCEPTION' not in logs() and 'Playback failed' not in logs(),'no Android crash or audio error')
    windows=re.findall(r'window frames=120[^\n]*',logs());check(windows and all('glError=0' in w for w in windows),'all captured diagnostic windows GL-clean')
    completed=True;print('Android UI03 PASS',len(checks),flush=True)

finally:
    try:evidence.append(logs())
    except Exception:pass
    (OUT/('ui03-boss-final.log' if '--boss-only' in sys.argv else 'ui03-android.log')).write_text('\n'.join(evidence),encoding='utf-8');(OUT/('ui03-boss-final-checks.json' if '--boss-only' in sys.argv else 'ui03-android-checks.json')).write_text(json.dumps({'pass':completed,'count':len(checks),'checks':checks},indent=2))
    adb('shell','am','force-stop',PKG)
    if run:run.cleanup()
    unchanged={p:read(p)==b for p,b in snapshot.items()};(OUT/('android-boss-final-restoration.json' if '--boss-only' in sys.argv else 'android-restoration.json')).write_text(json.dumps(unchanged,indent=2))
    if not all(unchanged.values()):raise AssertionError('External app state changed; preserved for inspection')
    adb('shell','am','start','-W','-n',PKG+'/.android.HomeActivity')
