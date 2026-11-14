"""Actual native launch, joystick Weapon Box contact and modal touch on the Android Studio Run build."""
from pathlib import Path
from collections import deque
import json,math,re,subprocess,time,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task03b'
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe';PKG='com.example.monstersurvivor'
HOME=PKG+'/'+PKG+'.android.HomeActivity';GAME=PKG+'/'+PKG+'.android.AndroidLauncher'
checks=[];cases=[];pid='';cursor=0
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def check(ok,label):
    if not ok:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','AndroidRuntime:E','*:S')
def current():return logs()[cursor:]
def tap(x,y):adb('shell','input','tap',str(x),str(y))
def wait(fn,seconds=8):
    start=time.monotonic()
    while time.monotonic()-start<seconds:
        value=fn()
        if value:return value
        time.sleep(.2)
    raise TimeoutError('Expected weapon UI/telemetry was not observed')
def tap_id(name):
    adb('shell','uiautomator','dump','/data/local/tmp/task03b-window.xml')
    root=ET.fromstring(adb('exec-out','cat','/data/local/tmp/task03b-window.xml'))
    node=next(n for n in root.iter('node') if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));tap((x+r)//2,(y+b)//2)
pause_re=re.compile(r'User pause=(true|false) time=([\d.]+) player=([\d.]+),([\d.]+) fps=(\d+) hp=(\d+)/(\d+) dmg=(\d+) speed=(\d+) boxes=(\d+) drops=(\d+) collected=(\d+)([^\n]*)')
choice_re=re.compile(r'Weapon choice id=(\d+) token=(\d+) time=([\d.]+) player=([\d.]+),([\d.]+) offers=([^\n]+)')
box_re=re.compile(r'Box WEAPON_BOX at=([\d.]+),([\d.]+) time=([\d.]+) progress=([\d.]+)')
def pause():
    before=len(pause_re.findall(current()));tap(2136,72)
    return wait(lambda:[s for s in pause_re.findall(current())[before:] if s[0]=='true'] or None)[-1]
def resume():
    before=len(pause_re.findall(current()));tap(1140,612)
    return wait(lambda:[s for s in pause_re.findall(current())[before:] if s[0]=='false'] or None)[-1]
def at_home():return re.search(r'(?:topResumedActivity|ResumedActivity)[:=].*\.HomeActivity',adb('shell','dumpsys','activity','activities'))
def route(x,y,tx,ty):
    cells=(OUT/'route-42.txt').read_text().splitlines();start=(int(x//16),int(y//16));goal=(int(tx//16),int(ty//16))
    queue=deque([start]);parents={start:None}
    while queue and goal not in parents:
        at=queue.popleft()
        for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]:
            nxt=at[0]+dx,at[1]+dy
            if 0<=nxt[0]<100 and 0<=nxt[1]<100 and cells[nxt[1]][nxt[0]]=='.' and nxt not in parents:parents[nxt]=at;queue.append(nxt)
    if goal not in parents:raise RuntimeError('Spawn is not reachable')
    path=[];at=goal
    while at!=start:path.append(at);at=parents[at]
    path.reverse()
    if not path:return tx,ty
    if abs(x-(start[0]*16+8))>3 or abs(y-(start[1]*16+8))>3:return start[0]*16+8,start[1]*16+8
    end=path[0];direction=end[0]-start[0],end[1]-start[1]
    for at in path[1:4]:
        if (at[0]-end[0],at[1]-end[1])!=direction:break
        end=at
    return end[0]*16+8,end[1]*16+8
def launch():
    global cursor
    cursor=len(logs());tap_id('btn_play');tap_id('card_diff_easy');tap_id('card_mode_demo');tap_id('btn_start_game')
    wait(lambda:'Ready: EASY DEMO' in current());check('seed=42 walkable=8883' in current(),'Native launch uses valid fixed-seed procedural map')
def walk_to_box():
    wait(lambda:box_re.findall(current()),12);state=pause();start=time.monotonic();moves=[]
    while time.monotonic()-start<60:
        text=current();choices=choice_re.findall(text)
        if choices:return choices[-1],moves
        check('Game Over seed=' not in text,'Player remains alive on route')
        x,y=float(state[2]),float(state[3]);speed=int(state[8]);boxes=box_re.findall(text)
        tx,ty,_,_=min(boxes,key=lambda b:math.hypot(float(b[0])-x,float(b[1])-y));tx,ty=float(tx),float(ty)
        nx,ny=route(x,y,tx,ty);dx,dy=nx-x,ny-y;distance=math.hypot(dx,dy)
        resume()
        if distance<1:time.sleep(.15)
        else:
            duration=max(120,min(1700,round(2000*distance/(speed*8))))
            adb('shell','input','swipe','243','837',str(round(243+148*dx/distance)),str(round(837-148*dy/distance)),str(duration))
        choices=choice_re.findall(current())
        if choices:return choices[-1],moves
        state=pause();moves.append({'player':[float(state[2]),float(state[3])],'target':[tx,ty]});print('ROUTE',moves[-1],flush=True)
    raise TimeoutError('Did not touch Weapon Box within bounded route')
names=adb('shell','run-as',PKG,'ls','files').splitlines();prior=adb('exec-out','run-as',PKG,'cat','files/map-seed.txt') if 'map-seed.txt' in names else None
try:
    subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/map-seed.txt'"],input='42',text=True,encoding='utf-8',capture_output=True,check=True)
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    for index,weapon in enumerate(['BOW','FIREBALL','LIGHTNING']):
        launch();choice,moves=walk_to_box();check(True,'Real joystick contact opens Weapon Box for '+weapon)
        check(choice[5].startswith('Bow  NEW') and 'Fireball  NEW' in choice[5] and 'Lightning  NEW' in choice[5],'First modal offers three valid distinct new weapons')
        time.sleep(.5);check(len(choice_re.findall(current()))==1,'Pending box opens the modal once')
        if index==0:
            adb('shell','input','keyevent','4');state=wait(lambda:[s for s in pause_re.findall(current()) if s[0]=='true' and 'phase=CHOOSING_WEAPON' in s[12]] or None)[-1]
            check(state[1]==choice[2] and state[2:4]==choice[3:5],'Back pauses over modal with frozen clocks and position')
            count=current().count('Ready:');resumes=current().count('Lifecycle resume:');adb('shell','input','keyevent','3');time.sleep(.3)
            adb('shell','am','start','-W','-n',GAME,'-f','0x20000000')
            wait(lambda:current().count('Lifecycle resume:')>resumes);time.sleep(1.5)
            check(current().count('Ready:')==count,'Lifecycle keeps the same pending weapon session')
            state=resume();check(state[1]==choice[2] and 'phase=CHOOSING_WEAPON' in state[12],'Explicit Resume returns to the frozen pending modal')
        before=current().count('Weapon selected ');tap(1140,333+index*198)
        wait(lambda:current().count('Weapon selected ')>before)
        check('loadout=SWORD:1,'+weapon+':1' in current(),'Actual touch equips '+weapon+' level 1')
        tap(1140,333+index*198);time.sleep(.2);check(current().count('Weapon selected ')==before+1,'Double tap applies only one reward')
        time.sleep(3.5);state=pause();suffix=state[12]
        check(abs(float(state[2])-float(choice[3]))<.02 and abs(float(state[3])-float(choice[4]))<.02,'Released joystick stays neutral after selection')
        casts=re.search(r'casts=([^\s]+)',suffix);check(casts is not None and re.search(weapon+r':[1-9]\d*',casts.group(1)) is not None,weapon+' attacks automatically on the emulator')
        if weapon=='FIREBALL':check(int(re.search(r'explosions=(\d+)',suffix).group(1))>0,'Fireball impact produces an explosion on Android')
        if weapon=='LIGHTNING':check(int(re.search(r'lightningHits=(\d+)',suffix).group(1))>0,'Lightning hits living enemies on Android')
        check(int(state[11])==1,'Pending Weapon Box is consumed exactly once')
        cases.append({'weapon':weapon,'choice':choice,'pause':state,'route':moves})
        tap(1140,783);wait(at_home);check(True,'Gameplay Home returns to native Home')
    check(len({case['choice'][1] for case in cases})==3,'Selection tokens stay unique across native match sessions')
    captured=logs();check('FATAL EXCEPTION' not in captured and 'GdxRuntimeException' not in captured,'No Android runtime or missing-asset exception')
    (OUT/'android-weapons-smoke.json').write_text(json.dumps({'checks':checks,'cases':cases,'method':'Native menus + real joystick routing + libGDX modal touch; no inventory/damage injection'},indent=2),encoding='utf-8')
    print('ANDROID TASK 03B PASSED:',len(checks),'checks',flush=True)
finally:
    if pid:(OUT/'android-gameplay-logcat.txt').write_text(logs(),encoding='utf-8')
    if prior is None:adb('shell','run-as',PKG,'rm','-f','files/map-seed.txt')
    else:subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/map-seed.txt'"],input=prior,text=True,encoding='utf-8',capture_output=True,check=True)
    adb('shell','rm','-f','/data/local/tmp/task03b-window.xml')
