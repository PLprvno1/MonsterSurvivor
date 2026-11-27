"""Short paired 8-enemy weapon probe, audio enabled/disabled. Android Studio build only.
No packaging/install, no 82-enemy suite. Restores existing preferences and opt-in flags.
"""
from pathlib import Path
from android_runtime import retired_root_fixture_runner
retired_root_fixture_runner()
import csv,io,json,re,subprocess,time,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task05_1b'
ADB=str(Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe');PKG='com.example.monstersurvivor';PREF='shared_prefs/monster_survivor_prefs.xml'
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def read(path):
    from android_runtime import read as exact_read
    data=exact_read(path);return None if data is None else data.decode('utf-8')
def write(path,value):subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > {path}'"],input=value,capture_output=True,text=True,encoding='utf-8',check=True)
backup={p:read(p) for p in [PREF,'files/combat-profile.json','files/map-seed.txt','files/boss-test.json','files/boss-command.json']};results=[]
(OUT/'profile-state-backup.json').write_text(json.dumps(backup),encoding='utf-8')
try:
    for enabled in [True,False]:
        adb('shell','am','force-stop',PKG)
        root=ET.fromstring(backup[PREF]) if backup[PREF] else ET.Element('map')
        for name,value in [('pref_music_enabled',str(enabled).lower()),('pref_sfx_enabled',str(enabled).lower())]:
            for node in list(root):
                if node.get('name')==name:root.remove(node)
            ET.SubElement(root,'boolean',name=name,value=value)
        for name in ['pref_music_volume','pref_sfx_volume']:
            for node in list(root):
                if node.get('name')==name:root.remove(node)
            ET.SubElement(root,'int',name=name,value='100')
        write(PREF,ET.tostring(root,encoding='unicode',xml_declaration=True));write('files/map-seed.txt','42')
        adb('shell','run-as',PKG,'rm','-f','files/boss-test.json','files/boss-command.json')
        label='audio_on' if enabled else 'audio_off'
        write('files/combat-profile.json',json.dumps({'weaponProbe':True,'label':label,'warmupFrames':30,'sampleFrames':90}))
        adb('shell','am','start','-W','-n',PKG+'/.android.AndroidLauncher');pid=adb('shell','pidof',PKG).strip().split()[0]
        end=time.monotonic()+90
        while time.monotonic()<end:
            logs=adb('logcat','-d','--pid='+pid,'-s','MonsterSurvivor:I','GameAudio:I','AndroidRuntime:E','*:S')
            if 'Profile complete '+label in logs:break
            if 'FATAL EXCEPTION' in logs:raise RuntimeError(logs)
            time.sleep(.5)
        else:raise TimeoutError('profile timed out')
        (OUT/(label+'.log')).write_text(logs,encoding='utf-8')
        text=read('files/combat-profile-'+label+'.csv');(OUT/(label+'.csv')).write_text(text,encoding='utf-8')
        environment=read('files/combat-profile-'+label+'-environment.txt');(OUT/(label+'-environment.txt')).write_text(environment,encoding='utf-8')
        (OUT/(label+'-meminfo.txt')).write_text(adb('shell','dumpsys','meminfo',PKG),encoding='utf-8')
        rows=list(csv.DictReader(io.StringIO(text)));stages=[]
        for stage in range(3):
            times=sorted(float(r['callbackMs']) for r in rows if int(float(r['probeStage']))==stage)
            stages.append({'stage':stage,'frames':len(times),'mean_ms':sum(times)/len(times),'fps':1000/(sum(times)/len(times)),'p95_ms':times[int(len(times)*.95)-1]})
        results.append({'enabled':enabled,'fixture':'8 enemies, seed42, protected HP, 3 stages (Sword; Bow+Fire; Fire+Lightning), 30 warmup +90 samples/stage','load_ms':re.findall(r'Loaded unique=36 failures=\d+ ms=([^ ]+)',logs),'stages':stages,'environment':environment})
        print(label,json.dumps(stages),flush=True)
finally:
    adb('shell','am','force-stop',PKG)
    for path,value in backup.items():
        if value is None:adb('shell','run-as',PKG,'rm','-f',path)
        else:write(path,value)
    (OUT/'android-performance.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
    adb('shell','am','start','-W','-n',PKG+'/.android.HomeActivity')
    print('Restored preferences/diagnostic flags',flush=True)
