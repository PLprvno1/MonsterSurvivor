"""Task08 Android enemy gameplay evidence. Uses existing Studio deployment and one-use Task06.1 capability.
Never installs/builds an APK, writes root fixtures/preferences or directly grants weapon levels.
"""
from pathlib import Path
import json,re,time,base64,sys,subprocess
from android_runtime import adb,read,AutomatedRun,PKG,ADB
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task08';OUT.mkdir(parents=True,exist_ok=True)
paths=['shared_prefs/monster_survivor_prefs.xml']+['files/'+n for n in ['skill-test.json','skill-command.json','boss-test.json','boss-command.json','combat-profile.json','map-seed.txt','camera-check.json','asset-visual-check']]
snapshot={p:read(p) for p in paths};(OUT/'android-original-state.json').write_text(json.dumps({p:None if b is None else base64.b64encode(b).decode() for p,b in snapshot.items()},indent=2))
armed=None;run=None;pid='0';serial=0;checks=[];evidence=[];completed=False
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','MatchBaseline:I','WeaponRewards:I','SkillDiagnostics:I','EvolutionDiagnostics:I','DiagnosticGate:I','Skills:I','EnemyDiagnostics:I','GameAudio:I','AndroidRuntime:E','*:S')
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

def enemy():
    command('status');return re.findall(r'EnemyDiagnostics\s*: status=status serial='+str(serial)+r'[^\n]*',logs())[-1]

def flights():
    value=enemy();rows=re.findall(r'EnemyDiagnostics\s*: shot serial='+str(serial)+r'[^\n]*',logs())
    return value,{field(row,'id'):row for row in rows}

def arena(group,progress):
    current=float(field(skill_status(),'progress'))
    if progress>current:command('progress',value=progress)
    command('enemyArena',group=group)
    row=re.findall(r'EnemyDiagnostics\s*: arena group='+group+r'[^\n]*',logs())[-1]
    check(int(field(row,'placed'))>0,'actual registered enemy arena '+group)

def scene(group,progress,seconds=9):
    arena(group,progress);before=enemy();time.sleep(seconds);after=enemy();capture('android-enemy-'+group)
    check(int(field(after,'releases'))>int(field(before,'releases')),'actual visible gameplay skill release '+group)
    check(int(field(after,'shots'))<=24 and int(field(after,'areas'))<=3 and int(field(after,'fx'))<=24,'ordinary projectile/area/FX pools bounded '+group)
    check(int(field(after,'maxTelegraphs'))<=4 and int(field(after,'maxCasters'))<=10 and int(field(after,'astarMax'))<=2,'cast/telegraph/Astar budgets '+group)
    evidence.append(logs())

try:
    adb('shell','am','force-stop',PKG);run=AutomatedRun();run.write('skill-test.json','{"protectedHp":true,"progress":0}');run.write('map-seed.txt','42');run.launch('HARD','NORMAL')
    pid=adb('shell','pidof',PKG).strip().split()[0];wait(lambda:'LOCAL FIXTURE protectedHp=true' in logs());time.sleep(1)
    check('Accepted one-use' in logs() and read(run.root+'/lease.json') is None,'explicit isolated one-use fixture, no normal trigger')
    scene('straight',60);scene('fan',240)
    for group,status_key in [('poisonOnly','poison'),('iceOnly','slow')]:
        arena(group,240);observed=wait(lambda:(v if float(field(v,status_key))>0 else None) if (v:=enemy()) else None,25);capture('android-status-'+status_key)
        check(float(field(observed,status_key))>0,'actual accepted-hit status '+status_key);evidence.append(logs())
    # Real Dash after fire, compare the same surviving projectile IDs across actual Android movement.
    wait(lambda:float(field(skill_status(),'dashCd'))==0,10)
    armed=subprocess.Popen([ADB,'shell','env','CLASSPATH=/data/local/tmp/task06-touch.dex','app_process','/','SkillTouch','multi','243','837','243','894','1920','716','250','100','armed'],stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE,creationflags=subprocess.CREATE_NO_WINDOW)
    check(armed.stdout.readline().strip()==b'READY','shell input VM warmed before short-lived projectile')
    arena('nonHoming',420)
    command('enemyTrace');trace_serial=serial
    def traces():return re.findall(r'EnemyDiagnostics\s*: trace='+str(trace_serial)+r'[^\n]*',logs())
    first=wait(lambda:next((row for row in traces() if float(field(row,'age'))<.3),None),25)
    # Capture logs during flight; an adb screenshot can outlast a valid 2.6-second projectile.
    touch_bytes,touch_err=armed.communicate(input=b'\n',timeout=15);check(armed.returncode==0,'armed helper injects real Android input');touch=touch_bytes.decode('utf-8',errors='replace')
    (OUT/'android-dash-touch.log').write_text(touch);rows=traces();groups={}
    for row in rows:groups.setdefault(field(row,'id'),[]).append(row)
    moving={key:values for key,values in groups.items() if len(values)>1 and any(float(field(v,'dashActive'))>0 for v in values)}
    check(moving,'same projectile IDs recorded during actual Android Dash')
    for key,values in moving.items():
        check(len({(field(v,'vx'),field(v,'vy')) for v in values})==1,'Android no homing, fixed velocity id='+key)
        check(len({(field(v,'x'),field(v,'y')) for v in values})>1,'Android projectile advances along fixed vector id='+key)
        check(len({(field(v,'playerX'),field(v,'playerY')) for v in values})>1,'Android player actually moves while projectile flies id='+key)
    after=enemy();check(field(after,'hits')==field(first,'hits'),'real movement/Dash avoids ordinary projectile damage in sample')
    check(re.search(r'Activate type=DASH[^\n]*joystick=0',logs()) is not None,'real Dash keeps joystick owner')
    (OUT/'android-nonhoming-proof.json').write_text(json.dumps({'first':first,'after':after,'tracked':moving},indent=2));command('enemyTrace',enabled=False);capture('android-nonhoming-after');evidence.append(logs())
    scene('aoe',420);scene('elemental',420)
    # Pause freezes ordinary shots/casts; no diagnostic can make the simulation advance while paused.
    arena('fan',420);wait(lambda:int(field(enemy(),'shots'))>0,25);before,old=flights()
    oldPause=logs().count('User pause=true');adb('shell','input','keyevent','4');wait(lambda:logs().count('User pause=true')>oldPause)
    frozen,shots=flights();time.sleep(1);still,shots2=flights();capture('android-enemy-paused')
    check(bool(shots) and set(shots)==set(shots2) and all(field(shots[k],'x')==field(shots2[k],'x') and field(shots[k],'age')==field(shots2[k],'age') for k in shots),'Pause freezes hostile projectile position/lifetime')
    oldPause=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>oldPause)
    # Actual box/evolution transaction with ordinary skills active, preserving pools and rewards.
    offers,token=box();choose(offers,next(i for i,v in enumerate(offers) if v.startswith('BOW:')),token);earn('SWORD');capture('android-enemy-blade-orbit')
    arena('fan',420);time.sleep(3);multi('ULTIMATE',2127,675);capture('android-enemy-ultimate')
    command('progress',value=600);wait(lambda:int(field(status(),'lichHp'))>0);capture('android-enemy-lich-phase1');command('evolutionBossArena')
    for i in range(3):
        if field(status(),'lichPhase')=='2':break
        command('phase2');time.sleep(.3)
    wait(lambda:field(status(),'lichPhase')=='2');capture('android-enemy-lich-phase2');sample('task08-lich-phase2',9)
    for i in range(12):
        if phase() in ('WINNING','VICTORY'):break
        command('phase2');time.sleep(.3)
    wait(lambda:phase()=='VICTORY',25);capture('android-enemy-victory');final=enemy();check(field(final,'shots')=='0' and field(final,'areas')=='0','Victory clears ordinary hostile pools')
    retry();fresh=enemy();check(field(fresh,'shots')=='0' and field(fresh,'poison')=='0.0' and field(fresh,'slow')=='0.0','Retry clears hostile projectiles/status')
    command('playerDeath');wait(lambda:phase()=='GAME_OVER',25);capture('android-enemy-game-over');retry()
    evidence.append(logs());check('FATAL EXCEPTION' not in logs() and 'Playback failed' not in logs(),'Android no crash or audio playback failure')
    windows=re.findall(r'window frames=120[^\n]*','\n'.join(evidence));check(windows and all('glError=0' in w for w in windows),'all captured Android gameplay windows GL-clean')
    completed=True;print('Android Task08 PASS',len(checks),flush=True)
finally:
    if armed is not None and armed.poll() is None:
        try:armed.communicate(input=b'',timeout=3)
        except subprocess.TimeoutExpired:armed.kill();armed.wait()
    try:evidence.append(logs())
    except Exception:pass
    (OUT/'enemy-android.log').write_text('\n'.join(evidence),encoding='utf-8');(OUT/'enemy-android-checks.json').write_text(json.dumps({'pass':completed,'count':len(checks),'checks':checks},indent=2))
    adb('shell','am','force-stop',PKG)
    if run:run.cleanup()
    unchanged={p:read(p)==b for p,b in snapshot.items()};(OUT/'android-restoration.json').write_text(json.dumps(unchanged,indent=2));assert all(unchanged.values()),unchanged
    adb('shell','am','start','-W','-n',PKG+'/.android.HomeActivity')
