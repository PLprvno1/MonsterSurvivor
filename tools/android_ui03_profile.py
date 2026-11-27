"""Paired UI-only Android comparison; existing Studio build, owned diagnostic namespace."""
from pathlib import Path
import sys,time,re,json
from android_runtime import adb,read,AutomatedRun,PKG
out=Path(__file__).resolve().parents[1]/'core/build/reports/ui03';out.mkdir(parents=True,exist_ok=True)
label=sys.argv[1];paths=['shared_prefs/monster_survivor_prefs.xml']+['files/'+n for n in ['skill-test.json','skill-command.json','boss-test.json','boss-command.json','combat-profile.json','map-seed.txt','camera-check.json','asset-visual-check']]
snapshot={p:read(p) for p in paths};run=None
try:
    adb('shell','am','force-stop',PKG);run=AutomatedRun();run.write('skill-test.json','{"protectedHp":true,"progress":0}');run.write('map-seed.txt','42');run.launch('HARD','NORMAL')
    pid=adb('shell','pidof',PKG).strip().split()[0]
    # Same seed, baseline loadout, protected HP, no input and fixed wall-clock capture interval.
    time.sleep(3)
    start=adb('logcat','-d','--pid='+pid,'-s','SkillDiagnostics:I','*:S').count('window frames=120')
    time.sleep(25)
    log=adb('logcat','-d','--pid='+pid,'-s','SkillDiagnostics:I','AndroidRuntime:E','*:S')
    windows=re.findall(r'window frames=120[^\n]*',log)[start:]
    assert len(windows)>=2 and all('glError=0' in w for w in windows),windows
    (out/('profile-'+label+'.json')).write_text(json.dumps({'conditions':'Pixel_4 Hard Normal seed42 protectedHp Sword1 no input, seconds 3..28','windows':windows},indent=2))
    (out/('profile-'+label+'.png')).write_bytes(adb('exec-out','screencap','-p',binary=True))
    print(label,len(windows),'GL-clean windows',flush=True)
finally:
    adb('shell','am','force-stop',PKG)
    if run:run.cleanup()
    unchanged={p:read(p)==v for p,v in snapshot.items()}
    (out/('profile-'+label+'-state.json')).write_text(json.dumps(unchanged,indent=2));assert all(unchanged.values())
    adb('shell','am','start','-W','-n',PKG+'/.android.HomeActivity')
