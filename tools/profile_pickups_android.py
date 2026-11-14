"""One 8-enemy baseline versus 6-box/20-drop probe after Android Studio Run; no installation."""
from pathlib import Path
import csv,io,json,statistics,subprocess,time
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task03a'
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe';PACKAGE='com.example.monstersurvivor'
def adb(*args):
    return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def write_private(name,data):
    subprocess.run([ADB,'shell',f"run-as {PACKAGE} sh -c 'cat > files/{name}'"],input=data,text=True,encoding='utf-8',check=True,capture_output=True)
listing=adb('shell','run-as',PACKAGE,'ls','files').splitlines()
backup=adb('exec-out','run-as',PACKAGE,'cat','files/combat-profile.json') if 'combat-profile.json' in listing else None
try:
    write_private('combat-profile.json',json.dumps({'label':'pickups','pickupProbe':True,'warmupFrames':30,'sampleFrames':60}))
    adb('shell','run-as',PACKAGE,'rm','-f','files/combat-profile-pickups.csv','files/combat-profile-pickups-environment.txt')
    adb('shell','am','force-stop',PACKAGE)
    adb('shell','am','start','-W','-n',PACKAGE+'/'+PACKAGE+'.android.AndroidLauncher','--es','EXTRA_DIFFICULTY','HARD','--es','EXTRA_GAME_MODE','NORMAL')
    start=time.monotonic();data=''
    while time.monotonic()-start<40:
        names=adb('shell','run-as',PACKAGE,'ls','files').splitlines()
        if 'combat-profile-pickups.csv' in names:
            data=adb('exec-out','run-as',PACKAGE,'cat','files/combat-profile-pickups.csv');break
        time.sleep(1)
    if not data:raise RuntimeError('No new pickup probe. Verify Android Studio Run deployed current classes.')
    rows=list(csv.DictReader(io.StringIO(data)));assert 'pickupCount' in rows[0]
    (OUT/'android-pickups.csv').write_text(data,encoding='utf-8')
    environment=adb('exec-out','run-as',PACKAGE,'cat','files/combat-profile-pickups-environment.txt')
    (OUT/'android-pickups-environment.txt').write_text(environment,encoding='utf-8')
    summary={'environment':environment,'method':'Two sequential 8-enemy stages; 30 warmup + 60 measured frames each; CPU callback timing, no GPU timing; legacy comparison map','stages':[]}
    for count in [0,26]:
        group=[r for r in rows if int(float(r['pickupCount']))==count];assert len(group)==60
        assert all(int(float(r['population']))==8 and int(float(r['living']))==8 for r in group)
        ms=sorted(float(r['callbackMs']) for r in group)
        summary['stages'].append({'pickups':count,'samples':len(group),'fps':1000/statistics.mean(ms),'p95Ms':ms[int(len(ms)*.95)-1],
            'meanCpuMs':{k:statistics.mean(float(r[k]) for r in group) for k in ['PICKUPSMs','WORLD_RENDERMs','HUDMs']},
            'worldDrawCalls':statistics.mean(float(r['worldDrawCalls']) for r in group)})
    (OUT/'android-pickups-summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8');print(json.dumps(summary,indent=2))
finally:
    if backup is None:adb('shell','run-as',PACKAGE,'rm','-f','files/combat-profile.json')
    else:write_private('combat-profile.json',backup)
    adb('shell','am','force-stop',PACKAGE)
    adb('shell','am','start','-W','-n',PACKAGE+'/'+PACKAGE+'.android.HomeActivity')
