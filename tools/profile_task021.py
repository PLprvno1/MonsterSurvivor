"""Collect one finite in-app profile from the app deployed by Android Studio Run. Never builds/installs."""
from pathlib import Path
import csv
import io
import json
import statistics
import subprocess
import sys
import time

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'core/build/reports/task02_1';OUT.mkdir(parents=True,exist_ok=True)
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe'
PACKAGE='com.example.monstersurvivor'
def adb(*args,check=True):return subprocess.run([ADB,*args],capture_output=True,text=True,check=check).stdout
label=sys.argv[1] if len(sys.argv)>1 else 'before'
assert label in ('before','after')
flags=json.dumps({'label':label,'warmupFrames':30,'sampleFrames':60})
adb('shell','run-as',PACKAGE,'rm','-f','files/combat-profile-'+label+'.csv','files/combat-profile-'+label+'-environment.txt')
subprocess.run([ADB,'shell',f"run-as {PACKAGE} sh -c 'cat > files/combat-profile.json'"],input=flags,text=True,check=True)
adb('shell','am','force-stop',PACKAGE)
adb('shell','am','start','-W','-n',PACKAGE+'/'+PACKAGE+'.android.AndroidLauncher','--es','EXTRA_DIFFICULTY','HARD','--es','EXTRA_GAME_MODE','NORMAL')
start=time.monotonic();data=''
while time.monotonic()-start<50:
    result=subprocess.run([ADB,'exec-out','run-as',PACKAGE,'cat','files/combat-profile-'+label+'.csv'],capture_output=True,text=True)
    if result.returncode==0 and 'population,' in result.stdout:data=result.stdout;break
    time.sleep(1)
if not data:raise RuntimeError('No new in-app profile. Check Android Studio Run/deployment and Logcat.')
(OUT/f'android-{label}.csv').write_text(data,encoding='utf-8')
environment=adb('exec-out','run-as',PACKAGE,'cat','files/combat-profile-'+label+'-environment.txt')
(OUT/f'android-{label}-environment.txt').write_text(environment,encoding='utf-8')
rows=list(csv.DictReader(io.StringIO(data)));summary={'environment':environment,'method':'in-app render callbacks; opt-in CPU instrumentation; no GPU timing','populations':[]}
if label=='after':
    assert all(int(float(r['separationPairs']))==int(float(r['population']))*(int(float(r['population']))-1)//2 for r in rows), 'Installed app is not the optimized once-per-frame version; no after result accepted'
for population in (8,20,40,60):
    group=[r for r in rows if int(float(r['population']))==population]
    times=sorted(float(r['callbackMs']) for r in group)
    means={k:statistics.mean(float(r[k]) for r in group) for k in group[0] if k.endswith('Ms')}
    counters={k:statistics.mean(float(r[k]) for r in group) for k in ('living','separationPairs','occupancyChecks','aStarSearches','worldDrawCalls','visibleSprites')}
    summary['populations'].append({'population':population,'frames':len(group),'callbackFps':1000/statistics.mean(times),
        'p95CallbackMs':times[max(0,int(len(times)*.95)-1)],'meanMs':means,'meanCounters':counters})
(OUT/f'android-{label}-summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
logs=adb('logcat','-d','-s','MonsterSurvivor:I','AndroidRuntime:E','*:S')
(OUT/f'android-{label}-logcat.txt').write_text(logs,encoding='utf-8')
adb('shell','run-as',PACKAGE,'rm','files/combat-profile.json')
assert 'FATAL EXCEPTION' not in logs[-6000:]
print(json.dumps(summary,indent=2),flush=True)
