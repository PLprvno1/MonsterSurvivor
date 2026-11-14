"""Real native menus and joystick pickups on the Studio Run build. No packaging/install or damage injection."""
from pathlib import Path
from collections import deque
import json,math,re,subprocess,time,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task03a'
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe';PKG='com.example.monstersurvivor'
HOME=PKG+'/'+PKG+'.android.HomeActivity';GAME=PKG+'/'+PKG+'.android.AndroidLauncher'
checks=[];observed=[];captured='';pid=''
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def check(ok,label):
    if not ok:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','AndroidRuntime:E','*:S')
def tap(x,y):adb('shell','input','tap',str(x),str(y))
def wait(fn,seconds=8):
    start=time.monotonic()
    while time.monotonic()-start<seconds:
        value=fn()
        if value:return value
        time.sleep(.2)
    raise TimeoutError('Expected gameplay telemetry not seen')
def tap_id(name):
    adb('shell','uiautomator','dump','/data/local/tmp/task03a-window.xml')
    root=ET.fromstring(adb('exec-out','cat','/data/local/tmp/task03a-window.xml'))
    node=next(n for n in root.iter('node') if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));tap((x+r)//2,(y+b)//2)
def launch(mode):
    count=logs().count('Ready:');tap_id('btn_play');tap_id('card_diff_easy');tap_id('card_mode_'+mode.lower());tap_id('btn_start_game')
    wait(lambda:logs().count('Ready:')>count)
    check('Ready: EASY '+mode in logs(),'Native EASY/'+mode+' reaches gameplay')
pause_re=re.compile(r'User pause=(true|false) time=([\d.]+) player=([\d.]+),([\d.]+) fps=(\d+) hp=(\d+)/(\d+) dmg=(\d+) speed=(\d+) boxes=(\d+) drops=(\d+) collected=(\d+)')
box_re=re.compile(r'Box (DAMAGE_BOX|HEALTH_BOX|SPEED_BOX) at=([\d.]+),([\d.]+) time=([\d.]+) progress=([\d.]+)')
def pause():
    before=len(pause_re.findall(logs()));tap(2136,72)
    state=wait(lambda:[s for s in pause_re.findall(logs())[before:] if s[0]=='true'] or None)[-1]
    check(state[0]=='true','Touch Pause works');return state
def resume():
    before=len(pause_re.findall(logs()));tap(1140,612)
    wait(lambda:[s for s in pause_re.findall(logs())[before:] if s[0]=='false'] or None)
def home():
    tap(1140,783)
    wait(lambda:re.search(r'(?:topResumedActivity|ResumedActivity)[:=].*\.HomeActivity',adb('shell','dumpsys','activity','activities')))
def route(x,y,tx,ty):
    cells=(OUT/'route-42.txt').read_text().splitlines();start=(int(x//16),int(y//16));goal=(int(tx//16),int(ty//16))
    queue=deque([start]);parents={start:None}
    while queue and goal not in parents:
        at=queue.popleft()
        for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]:
            nxt=(at[0]+dx,at[1]+dy)
            if 0<=nxt[0]<100 and 0<=nxt[1]<100 and cells[nxt[1]][nxt[0]]=='.' and nxt not in parents:
                parents[nxt]=at;queue.append(nxt)
    if goal not in parents:return None
    path=[];at=goal
    while at!=start:path.append(at);at=parents[at]
    path.reverse()
    # Remain on straight runs of safe tile centers and limit each swipe to 64 world units.
    if not path:return tx,ty
    if abs(x-(start[0]*16+8))>3 or abs(y-(start[1]*16+8))>3:return start[0]*16+8,start[1]*16+8
    end=path[0];direction=(end[0]-start[0],end[1]-start[1])
    for at in path[1:4]:
        if (at[0]-end[0],at[1]-end[1])!=direction:break
        end=at
    return end[0]*16+8,end[1]*16+8
listing=adb('shell','run-as',PKG,'ls','files').splitlines();prior=adb('exec-out','run-as',PKG,'cat','files/map-seed.txt') if 'map-seed.txt' in listing else None
try:
    subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/map-seed.txt'"],input='42',text=True,encoding='utf-8',capture_output=True,check=True)
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    launch('DEMO');wait(lambda:box_re.findall(logs()),12);state=pause()
    check('seed=42 walkable=8883' in logs(),'Android uses the reproducible procedural map')
    first=box_re.findall(logs())[0];check(15<=float(first[4])<21,'Demo first box follows the progress clock')
    before=logs();time.sleep(.6);resume();time.sleep(.2);after=pause_re.findall(logs())[-1]
    check(after[0]=='false' and state[1:4]==after[1:4],'Pause freezes simulation time and player position')
    adb('shell','input','keyevent','3');time.sleep(.3);count=logs().count('Ready:')
    adb('shell','am','start','-W','-n',GAME,'-f','0x20000000');time.sleep(.4)
    check(logs().count('Ready:')==count,'Lifecycle resumes the same match')
    resume();state=pause();check(state[0]=='true','Lifecycle still supports explicit Resume then Pause')
    start=time.monotonic();visited=set();targets=[]
    while time.monotonic()-start<90 and len(observed)<3:
        text=logs()
        if 'Game Over seed=' in text:break
        x,y=float(state[2]),float(state[3]);speed=int(state[8]);options=[]
        for kind,sx,sy,when,progress in box_re.findall(text):
            key=(kind,float(sx),float(sy))
            if key in visited or kind in observed or float(state[1])-float(when)>59:continue
            options.append((math.hypot(float(sx)-x,float(sy)-y),key))
        if not options:
            resume();time.sleep(1);state=pause();continue
        _,key=min(options);kind,tx,ty=key;next_point=route(x,y,tx,ty)
        if next_point is None:visited.add(key);continue
        nx,ny=next_point;dx,dy=nx-x,ny-y;distance=math.hypot(dx,dy)
        if math.hypot(tx-x,ty-y)<=10:
            visited.add(key);continue
        if distance<1:visited.add(key);continue
        before_count=int(state[11]);old_damage=int(state[7]);old_speed=int(state[8]);old_max=int(state[6])
        resume();duration=max(120,min(1700,round(2000*distance/(speed*8))))
        adb('shell','input','swipe','243','837',str(round(243+148*dx/distance)),str(round(837-148*dy/distance)),str(duration))
        if 'Game Over seed=' in logs():break
        state=pause();targets.append({'type':kind,'target':[tx,ty],'player':[float(state[2]),float(state[3])],'collected':int(state[11])})
        print('ROUTE',targets[-1],flush=True)
        collected=int(state[11])>before_count
        effect=(int(state[7])>old_damage if kind=='DAMAGE_BOX' else int(state[8])>old_speed if kind=='SPEED_BOX' else int(state[6])>old_max or '+HP' in logs()[len(text):])
        if collected and effect and math.hypot(float(state[2])-tx,float(state[3])-ty)<20:
            observed.append(kind);visited.add(key);check(True,'Actual joystick collects '+kind+' with an immediate stat effect')
    captured=logs();check('Pickup +' in captured,'Real emulator emits successful pickup feedback')
    if state[0]=='true':home()
    else:adb('shell','am','start','-W','-n',HOME)
    launch('NORMAL');time.sleep(.5);state=pause()
    check(state[5:9]==('10','10','1','10'),'New native match resets HP/max/damage/speed')
    home();check('FATAL EXCEPTION' not in captured and 'GdxRuntimeException' not in captured,'No Android crash or missing asset exception')
    (OUT/'android-pickups-smoke.json').write_text(json.dumps({'checks':checks,'observedBoxTypes':observed,'routes':targets,'method':'Native menu taps, actual joystick swipes and telemetry; fixed seed 42; no gameplay injection'},indent=2),encoding='utf-8')
    print('ANDROID TASK 03A:',len(checks),'checks; observed',observed,flush=True)
finally:
    if pid:(OUT/'android-gameplay-logcat.txt').write_text(logs(),encoding='utf-8')
    if prior is None:adb('shell','run-as',PKG,'rm','-f','files/map-seed.txt')
    else:subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/map-seed.txt'"],input=prior,text=True,encoding='utf-8',capture_output=True,check=True)
    adb('shell','rm','-f','/data/local/tmp/task03a-window.xml')
