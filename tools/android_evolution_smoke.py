"""Task 07 Android touch evidence. Uses existing Studio deployment and one-use Task06.1 capability.
Never installs/builds an APK, writes root fixtures/preferences or directly grants weapon levels.
"""
from pathlib import Path
import json,re,time,base64,sys
from android_runtime import adb,read,AutomatedRun,PKG
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task07';OUT.mkdir(parents=True,exist_ok=True)
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
try:
    adb('shell','am','force-stop',PKG);run=AutomatedRun();run.write('skill-test.json','{"protectedHp":true,"progress":0}');run.write('map-seed.txt','42');run.launch('HARD','NORMAL')
    pid=adb('shell','pidof',PKG).strip().split()[0];wait(lambda:'LOCAL FIXTURE protectedHp=true' in logs());time.sleep(1)
    check('Accepted one-use' in logs() and read(run.root+'/lease.json') is None,'existing debug-only lease consumed, explicit isolated fixture')
    check(field(status(),'weapons')=='SWORD:1','diagnostic starts Sword1; no free test loadout')
    # First unlock Bow (existing three-new-weapons policy), then earn Sword through four upgrades + box.
    offers,token=box();choose(offers,next(i for i,v in enumerate(offers) if v.startswith('BOW:')),token)
    earn('SWORD');shoot_capture('SWORD');sample('one-evolved')
    earn('BOW');shoot_capture('BOW');earn('FIREBALL');shoot_capture('FIREBALL');sample('three-evolved')
    value=status();check(field(value,'weapons').count(':EVOLVED')==3,'three evolved weapons occupy exactly three existing slots')
    command('progress',value=300);wait(lambda:int(field(status(),'bosses'))>=1);command('evolutionBossArena');sample('mini-boss')
    command('progress',value=600);wait(lambda:int(field(status(),'lichHp'))>0);command('evolutionBossArena')
    for i in range(3):
        if field(status(),'lichPhase')=='2':break
        command('phase2');time.sleep(.25)
    wait(lambda:field(status(),'lichPhase')=='2');sample('final-phase2')
    # Separate real Retry permits the fourth weapon without exceeding the three-slot loadout.
    retry();earn('LIGHTNING');shoot_capture('LIGHTNING');sample('web')
    # A genuine Skill Box still routes its negative token and cannot grant a weapon evolution.
    before=status();old=len(re.findall(r'Skills\s*: Choice id=',logs()));command('skillBox');wait(lambda:len(re.findall(r'Skills\s*: Choice id=',logs()))>old);capture('android-skill-box-regression');time.sleep(.3);tap(1140,333);wait(lambda:'Upgraded type=DASH' in logs());after=status()
    check(field(before,'weapons')==field(after,'weapons'),'Skill Box upgrade preserves evolved loadout')
    # Real two-pointer input retains joystick owner while Dash activates with an evolved loadout.
    result=adb('shell','env','CLASSPATH=/data/local/tmp/task06-touch.dex','app_process','/','SkillTouch','multi','243','837','300','837','1920','716','250','100')
    (OUT/'android-touch.log').write_text(result,encoding='utf-8');check('joystick=0' in logs() and 'Activate type=DASH' in logs(),'joystick plus Dash multitouch survives evolution')
    evidence.append(logs());check('FATAL EXCEPTION' not in logs() and 'Playback failed' not in logs(),'no Android crash or audio playback error')
    retry();evidence.append(logs());completed=True;print('Android evolution PASS',len(checks),flush=True)
finally:
    try:evidence.append(logs())
    except Exception:pass
    (OUT/'evolution-android.log').write_text('\n'.join(evidence),encoding='utf-8');(OUT/'evolution-android-checks.json').write_text(json.dumps({'pass':completed,'count':len(checks),'checks':checks},indent=2))
    adb('shell','am','force-stop',PKG)
    if run:run.cleanup()
    unchanged={p:read(p)==b for p,b in snapshot.items()};(OUT/'android-restoration.json').write_text(json.dumps(unchanged,indent=2))
    if not all(unchanged.values()):raise AssertionError('External app state changed; preserved for inspection')
    adb('shell','am','start','-W','-n',PKG+'/.android.HomeActivity')
