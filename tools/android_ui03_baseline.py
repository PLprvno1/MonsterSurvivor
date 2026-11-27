"""Normal Android Studio Run baseline. Never enables a diagnostic or changes player/map/preferences."""
from pathlib import Path
import json,re,time,sys,xml.etree.ElementTree as ET
from android_runtime import adb,read,PKG

OUT=Path(__file__).resolve().parents[1]/'core/build/reports'/'ui03';OUT.mkdir(parents=True,exist_ok=True)
pid=adb('shell','pidof',PKG).strip().split()[0];checks=[]
def logs():return adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','MatchBaseline:I','DiagnosticGate:I','SkillDiagnostics:I','BossDiagnostics:I','GameAudio:I','AndroidRuntime:E','*:S')
def wait(fn,seconds=20):
    end=time.monotonic()+seconds
    while time.monotonic()<end:
        v=fn()
        if v:return v
        time.sleep(.2)
    raise TimeoutError('Expected normal Android state')
def check(value,label):
    if not value:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def nodes():
    adb('shell','uiautomator','dump','/data/local/tmp/task061-window.xml')
    return list(ET.fromstring(adb('exec-out','cat','/data/local/tmp/task061-window.xml')).iter('node'))
def tap_id(name):
    node=next(n for n in nodes() if n.get('resource-id')==PKG+':id/'+name)
    x,y,r,b=map(int,re.findall(r'\d+',node.get('bounds')));adb('shell','input','tap',str((x+r)//2),str((y+b)//2))
def capture(name): (OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p',binary=True))
def start(label):
    old=len(re.findall(r'MatchBaseline: Start ',logs()));tap_id('btn_play');capture(label+'-mode')
    tap_id('card_diff_easy');tap_id('card_mode_normal');tap_id('btn_start_game')
    wait(lambda:len(re.findall(r'MatchBaseline: Start ',logs()))>old)
    line=re.findall(r'MatchBaseline: Start [^\n]*',logs())[-1]
    # Ready is logged during construction, before the native window's first GL swap.
    time.sleep(1.2);capture(label+'-first-seconds')
    for expected in ['diagnostics=none','debug=false','hp=10/10','damage=1','speed=10','weapons=SWORD:1 ', 'fixedSeed=false','progress=0.0','elapsed=0.0','dashLevel=1','ultLevel=1','dashCd=0.0','ultCd=15.0']:
        check(expected in line,label+': '+expected)
    return line

pref=read('shared_prefs/monster_survivor_prefs.xml');result={}
try:
    for name in ['skill-test.json','skill-command.json','boss-test.json','boss-command.json','combat-profile.json','map-seed.txt','camera-check.json','asset-visual-check']:
        check(read('files/'+name) is None,'normal baseline: no legacy '+name)
    capture('phase0-home');first=start('phase0-fresh')
    adb('shell','input','keyevent','4');wait(lambda:'User pause=true' in logs());adb('shell','input','keyevent','36');wait(lambda:'Disposed played=' in logs())
    capture('phase0-return-home');second=start('phase0-home-start')
    seed=lambda s:re.search(r' seed=(-?\d+)',s).group(1)
    check(seed(first)!=seed(second) and seed(first)!='42' and seed(second)!='42','new Home->Start uses new random seed')
    check('LOCAL FIXTURE' not in logs() and 'Accepted one-use' not in logs(),'no diagnostic instantiated in normal play')
    check('FATAL EXCEPTION' not in logs(),'normal Android run has no crash')
    check(read('shared_prefs/monster_survivor_prefs.xml')==pref,'normal test does not change preferences')
    adb('shell','input','keyevent','4');adb('shell','input','keyevent','36')
    result={'pass':True,'checks':checks,'first':first,'homeStart':second}
    print('Normal Android baseline PASS',len(checks),flush=True)
finally:
    (OUT/'phase0-normal.log').write_text(logs(),encoding='utf-8')
    (OUT/'phase0-checks.json').write_text(json.dumps(result or {'pass':False,'checks':checks},indent=2),encoding='utf-8')
