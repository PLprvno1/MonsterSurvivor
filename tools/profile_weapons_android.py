"""Short finite comparison of Sword vs two legal three-weapon loadouts on the Studio Run build."""
from pathlib import Path
import csv,io,json,statistics,subprocess,time
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task03b'
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe';PKG='com.example.monstersurvivor'
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def write(name,text):subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/{name}'"],input=text,text=True,encoding='utf-8',capture_output=True,check=True)
files=adb('shell','run-as',PKG,'ls','files').splitlines();prior=adb('exec-out','run-as',PKG,'cat','files/combat-profile.json') if 'combat-profile.json' in files else None
try:
    write('combat-profile.json',json.dumps({'label':'weapons','weaponProbe':True,'warmupFrames':30,'sampleFrames':60}))
    adb('shell','run-as',PKG,'rm','-f','files/combat-profile-weapons.csv','files/combat-profile-weapons-environment.txt')
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/'+PKG+'.android.AndroidLauncher','--es','EXTRA_DIFFICULTY','HARD','--es','EXTRA_GAME_MODE','NORMAL')
    start=time.monotonic();data=''
    while time.monotonic()-start<45:
        if 'combat-profile-weapons.csv' in adb('shell','run-as',PKG,'ls','files').splitlines():
            data=adb('exec-out','run-as',PKG,'cat','files/combat-profile-weapons.csv');break
        time.sleep(1)
    if not data:raise RuntimeError('No completed weapon probe; verify the latest Studio Run deployment.')
    rows=list(csv.DictReader(io.StringIO(data)));(OUT/'android-weapons.csv').write_text(data,encoding='utf-8')
    env=adb('exec-out','run-as',PKG,'cat','files/combat-profile-weapons-environment.txt');(OUT/'android-weapons-environment.txt').write_text(env,encoding='utf-8')
    summary={'environment':env,'method':'3 sequential 8-enemy stages, legacy map; 30 warmup + 60 measured frames/stage; ranged Lv3, diagnostic enemy HP to hold population; CPU callback timing, no GPU timing','stages':[]}
    for stage,label in enumerate(['Sword 1','Sword 1 + Bow 3 + Fireball 3','Sword 1 + Fireball 3 + Lightning 3']):
        group=[r for r in rows if int(float(r['probeStage']))==stage];assert len(group)==60 and all(int(float(r['living']))==8 for r in group)
        times=sorted(float(r['callbackMs']) for r in group)
        summary['stages'].append({'loadout':label,'samples':len(group),'fps':1000/statistics.mean(times),'p95Ms':times[56],
            'combatCpuMs':statistics.mean(float(r['COMBATMs']) for r in group),'worldRenderCpuMs':statistics.mean(float(r['WORLD_RENDERMs']) for r in group),
            'worldDrawCalls':statistics.mean(float(r['worldDrawCalls']) for r in group),'maxProjectiles':max(float(r['projectiles']) for r in group),'maxEffects':max(float(r['weaponEffects']) for r in group)})
    (OUT/'android-weapons-summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8');print(json.dumps(summary,indent=2),flush=True)
finally:
    if prior is None:adb('shell','run-as',PKG,'rm','-f','files/combat-profile.json')
    else:write('combat-profile.json',prior)
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/'+PKG+'.android.HomeActivity')
