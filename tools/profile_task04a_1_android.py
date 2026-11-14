"""Short mixed-roster 10/30/60 day/night profile on the Android Studio Run build."""
from pathlib import Path
import csv,io,json,statistics,subprocess,time
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task04a_1'
ADB='C:/Users/holon/AppData/Local/Android/Sdk/platform-tools/adb.exe';PKG='com.example.monstersurvivor'
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def write(name,text):subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > files/{name}'"],input=text,text=True,encoding='utf-8',capture_output=True,check=True)
files=adb('shell','run-as',PKG,'ls','files').splitlines();prior=adb('exec-out','run-as',PKG,'cat','files/combat-profile.json') if 'combat-profile.json' in files else None
seed_prior=adb('exec-out','run-as',PKG,'cat','files/map-seed.txt') if 'map-seed.txt' in files else None
try:
    write('map-seed.txt','42')
    write('combat-profile.json',json.dumps({'label':'roster','rosterProbe':True,'warmupFrames':30,'sampleFrames':60}))
    adb('shell','run-as',PKG,'rm','-f','files/combat-profile-roster.csv','files/combat-profile-roster-environment.txt')
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/'+PKG+'.android.AndroidLauncher','--es','EXTRA_DIFFICULTY','HARD','--es','EXTRA_GAME_MODE','NORMAL')
    start=time.monotonic();data=''
    while time.monotonic()-start<55:
        if 'combat-profile-roster.csv' in adb('shell','run-as',PKG,'ls','files').splitlines():
            data=adb('exec-out','run-as',PKG,'cat','files/combat-profile-roster.csv');break
        time.sleep(1)
    if not data:raise RuntimeError('No completed roster probe; verify the latest Studio Run deployment.')
    rows=list(csv.DictReader(io.StringIO(data)));(OUT/'android-roster.csv').write_text(data,encoding='utf-8')
    env=adb('exec-out','run-as',PKG,'cat','files/combat-profile-roster-environment.txt');(OUT/'android-roster-environment.txt').write_text(env,encoding='utf-8')
    summary={'environment':env,'method':'6 sequential mixed-roster 200x200 seed-42 stages: 10/30/60 enemies, day/night at each population; 30 warmup + 60 measured frames; Sword 1 + Fire 5 + Lightning 5; 82-species registry, paired populations with fixed mixed species per pair; diagnostic HP and forced lighting only, CPU callback timing, no GPU timing','stages':[]}
    for stage,label in enumerate(['10 day','10 night','30 day','30 night','60 day','60 night']):
        group=[r for r in rows if int(float(r['probeStage']))==stage];assert len(group)==60 and all(int(float(r['living']))==[10,10,30,30,60,60][stage] for r in group)
        times=sorted(float(r['callbackMs']) for r in group)
        summary['stages'].append({'loadout':label,'samples':len(group),'fps':1000/statistics.mean(times),'p95Ms':times[56],
            'combatCpuMs':statistics.mean(float(r['COMBATMs']) for r in group),'worldRenderCpuMs':statistics.mean(float(r['WORLD_RENDERMs']) for r in group),
            'aiCpuMs':statistics.mean(float(r['AIMs'])+float(r['ASTARMs'])+float(r['SEPARATIONMs']) for r in group),'nightCpuMs':statistics.mean(float(r['NIGHT_RENDERMs']) for r in group),'projectileCpuMs':statistics.mean(float(r['PROJECTILESMs']) for r in group),'weaponRenderCpuMs':statistics.mean(float(r['WEAPON_RENDERMs']) for r in group),'meanMs':statistics.mean(times),'worldDrawCalls':statistics.mean(float(r['worldDrawCalls']) for r in group),'maxProjectiles':max(float(r['projectiles']) for r in group),'maxEffects':max(float(r['weaponEffects']) for r in group),'distinctSpecies':max(int(float(r['distinctSpecies'])) for r in group),'textures':max(int(float(r['gameTextureCount'])) for r in group),'rgbaBytes':max(int(float(r['gameTextureBytes'])) for r in group),'maxAStarSearches':max(int(float(r['aStarSearches'])) for r in group)})
        update_parts=['PLAYERMs','AIMs','ASTARMs','SEPARATIONMs','COLLISIONMs','COMBATMs','SPAWNMs','PICKUPSMs','PROJECTILESMs']
        summary['stages'][-1]['instrumentedUpdateCpuMsExcludingAnimation']=statistics.mean(sum(float(r[k]) for k in update_parts) for r in group)
    (OUT/'android-roster-summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8');print(json.dumps(summary,indent=2),flush=True)
finally:
    if seed_prior is None:adb('shell','run-as',PKG,'rm','-f','files/map-seed.txt')
    else:write('map-seed.txt',seed_prior)
    if prior is None:adb('shell','run-as',PKG,'rm','-f','files/combat-profile.json')
    else:write('combat-profile.json',prior)
    adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',PKG+'/'+PKG+'.android.HomeActivity')
