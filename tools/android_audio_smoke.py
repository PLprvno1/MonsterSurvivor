"""Focused Pixel_4 checks against Android Studio Run. No build/install/export commands.
Existing opt-in boss fixture supplies protected HP and equipment. Always restore prefs/flags.
Confirms API/MediaPlayer state, not audible listening or perceptual loop quality.
"""
from pathlib import Path
from android_runtime import retired_root_fixture_runner
retired_root_fixture_runner()
import json,re,subprocess,time,sys,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task05_1b';OUT.mkdir(parents=True,exist_ok=True)
ADB=str(Path.home()/'AppData/Local/Android/Sdk/platform-tools/adb.exe');PKG='com.example.monstersurvivor';HOME=PKG+'/.android.HomeActivity';GAME=PKG+'/.android.AndroidLauncher';PREF='shared_prefs/monster_survivor_prefs.xml'
checks=[];evidence=[];pid='';serial=0
def adb(*args):return subprocess.run([ADB,*args],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
def write(path,text):subprocess.run([ADB,'shell',f"run-as {PKG} sh -c 'cat > {path}'"],input=text,capture_output=True,text=True,encoding='utf-8',check=True)
def read(path):
    from android_runtime import read as exact_read
    data=exact_read(path);return None if data is None else data.decode('utf-8')
def check(b,label):
    if not b:raise AssertionError(label)
    checks.append(label);print('PASS',label,flush=True)
def wait(fn,seconds=20):
    end=time.monotonic()+seconds
    while time.monotonic()<end:
        val=fn()
        if val:return val
        time.sleep(.25)
    raise TimeoutError('Expected condition not reached')
def logs():return adb('logcat','-d','--pid='+pid,'-s','GameAudio:I','NativeAudio:I','MonsterSurvivor:I','BossDiagnostics:I','AndroidRuntime:E','*:S')
def nodes():
    path='/data/local/tmp/task05_1b-window.xml';adb('shell','uiautomator','dump',path);return list(ET.fromstring(adb('exec-out','cat',path)).iter('node'))
def bounds(name):
    node=next(n for n in nodes() if n.get('resource-id')==PKG+':id/'+name)
    return tuple(map(int,re.findall(r'\d+',node.get('bounds'))))
def tap(x,y):adb('shell','input','tap',str(round(x)),str(round(y)))
def tap_id(name):
    x,y,r,b=bounds(name);tap((x+r)/2,(y+b)/2);time.sleep(.25)
def prefs():return {n.get('name'):n.get('value',n.text) for n in ET.fromstring(read(PREF))}
def media(label):
    data=adb('shell','dumpsys','media.player');(OUT/(label+'-media.txt')).write_text(data,encoding='utf-8');return data
def game_stats():return re.findall(r'User pause=true[^\n]*',logs())[-1]
def pause():
    old=logs().count('User pause=true');adb('shell','input','keyevent','4');wait(lambda:logs().count('User pause=true')>old);return game_stats()
def resume():
    old=logs().count('User pause=false');tap(1140,612);wait(lambda:logs().count('User pause=false')>old)
def snapshot(label):
    (OUT/(label+'.png')).write_bytes(subprocess.run([ADB,'exec-out','screencap','-p'],capture_output=True,check=True).stdout)
def set_slider(name,target,key):
    x,y,r,b=bounds(name);tap(x+16+(r-x-32)*target/100,(y+b)/2)
    value=int(prefs()[key]);delta=target-value
    if delta:adb('shell','input','keyevent',*(['22' if delta>0 else '21']*abs(delta)))
    time.sleep(.2);check(int(prefs()[key])==target,name+' saves '+str(target))
def launch(progress=299.99):
    global pid,serial
    adb('shell','am','force-stop',PKG);serial=0
    write('files/map-seed.txt','42');write('files/boss-test.json',json.dumps({'progress':progress,'weaponLevel':3,'holdHp':True}))
    adb('shell','run-as',PKG,'rm','-f','files/combat-profile.json','files/boss-command.json')
    adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    for name in ['btn_play','card_diff_hard','card_mode_demo','btn_start_game']:tap_id(name)
    wait(lambda:'Loaded unique=36 failures=0' in logs());wait(lambda:'LOCAL FIXTURE' in logs())
def command(action,value=None):
    global serial
    serial+=1;body={'serial':serial,'action':action}
    if value is not None:body['value']=value
    write('files/boss-command.json',json.dumps(body));wait(lambda:'command='+action+' serial='+str(serial)+' ' in logs(),12)

backup={path:read(path) for path in [PREF,'files/map-seed.txt','files/boss-test.json','files/boss-command.json','files/combat-profile.json']}
(OUT/'android-state-backup.json').write_text(json.dumps(backup),encoding='utf-8')
try:
    adb('shell','am','force-stop',PKG)
    for path in backup:
        if path.startswith('files/'):adb('shell','run-as',PKG,'rm','-f',path)
    adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    wait(lambda:'Menu prepared' in logs());check('looping(true)' in media('native-home'),'native menu loops through MediaPlayer')
    tap_id('btn_settings');wait(lambda:'SettingsActivity' in adb('shell','dumpsys','activity','activities'))
    initial=prefs()
    # Normalize switches using real UI, preserving all non-audio settings for comparison.
    for key,name in [('pref_music_enabled','switch_music'),('pref_sfx_enabled','switch_sfx')]:
        if prefs().get(key,'true')!='true':tap_id(name)
    for value in [0,50,100]:
        set_slider('seekbar_music_volume',value,'pref_music_volume');data=media('native-music-'+str(value))
        if value==0:check('paused(1)' in data,'volume zero pauses actual native Music')
        else:check(f'volume({value/100*.26:.6f}, {value/100*.26:.6f})' in data and 'paused(0)' in data,'native Music gain '+str(value))
    for value in [0,50,100]:set_slider('seekbar_sfx_volume',value,'pref_sfx_volume')
    tap_id('switch_music');check(prefs()['pref_music_enabled']=='false' and 'paused(1)' in media('native-disabled'),'Music off changes actual backend')
    tap_id('switch_music');check(prefs()['pref_music_enabled']=='true' and 'paused(0)' in media('native-enabled'),'Music on resumes actual backend')
    tap_id('switch_sfx');check(prefs()['pref_sfx_enabled']=='false','SFX disabled persists');tap_id('switch_sfx')
    check(prefs().get('game_language')==initial.get('game_language'),'audio settings preserve language')
    after=prefs();adb('shell','am','force-stop',PKG);adb('shell','am','start','-W','-n',HOME);pid=adb('shell','pidof',PKG).strip().split()[0]
    time.sleep(1);check(all(prefs().get(k)==after.get(k) for k in ['pref_music_enabled','pref_sfx_enabled','pref_music_volume','pref_sfx_volume','game_language']),'audio and language preferences survive process relaunch')
    evidence.append(logs());launch()
    check('Ready: HARD DEMO' in logs(),'native mode launch uses actual selected config')
    check('Players released' in logs() and 'Music state=BGM_DAY' in logs(),'menu released before gameplay Day')
    wait(lambda:'SFX event=MINI_APPEARS' in logs());check(logs().count('SFX event=MINI_APPEARS')==1,'Mini cue returned actual Android Sound ID once')
    command('progress',600)
    wait(lambda:'SFX event=LICH_APPEARS' in logs());check('Music state=BGM_FINAL' in logs(),'Final BGM and announcement while Mini was active')
    wait(lambda:'SFX event=LICH_AOE_IMPACT' in logs(),30)
    wait(lambda:'SFX event=LICH_PROJECTILE' in logs(),30)
    check('SFX event=WEAPON_FIRE_CAST' in logs() and 'SFX event=WEAPON_LIGHTNING_ACTIVATE' in logs(),'actual Fireball and Lightning backend cues')
    before=pause();paused_time=re.search(r'time=([^ ]+)',before)[1];time.sleep(1)
    check('paused(1)' in media('game-paused'),'user pause stops actual combat Music');resume()
    adb('shell','input','keyevent','3');time.sleep(.6);check('paused(0)' not in media('game-background'),'background retains paused players without streaming')
    adb('shell','am','start','-W','--activity-reorder-to-front','-n',GAME);wait(lambda:'Lifecycle resume: waiting for explicit resume' in logs());time.sleep(.5)
    check('paused(1)' in media('game-foreground-paused'),'foreground requires explicit Resume before Music')
    resume();check(logs().count('SFX event=LICH_APPEARS')==1,'foreground does not replay Final cue')
    for i in range(3):command('phase2');time.sleep(.3)
    wait(lambda:'SFX event=LICH_PHASE2' in logs());check(logs().count('SFX event=LICH_PHASE2')==1,'Phase2 once through Android backend')
    snapshot('android-phase2');time.sleep(6)
    for i in range(8):
        if 'Victory result=' in logs():break
        command('defeat');time.sleep(.35)
    wait(lambda:'Victory result=' in logs());check('Music state=BGM_VICTORY' in logs() and 'SFX event=UI_VICTORY' in logs() and 'SFX event=LICH_DEFEATED' in logs(),'real Final death -> Victory cue and Music')
    snapshot('android-victory');old=logs().count('Retry seed=');adb('shell','input','keyevent','46');wait(lambda:logs().count('Retry seed=')>old)
    command('playerDeath');wait(lambda:'Game Over seed=' in logs());check('Music state=BGM_GAME_OVER' in logs() and 'SFX event=PLAYER_DEATH' in logs() and 'SFX event=UI_GAME_OVER' in logs(),'real player death -> GameOver cues and Music')
    snapshot('android-gameover');menus=logs().count('Menu prepared');adb('shell','input','keyevent','36');wait(lambda:'Music state=null' in logs());wait(lambda:logs().count('Menu prepared')>menus);time.sleep(1)
    check('HomeActivity' in adb('shell','dumpsys','activity','activities') and 'Disposed played=' in logs(),'Home disposes gameplay audio and restores native menu')
    data=logs();evidence.append(data);check('Playback failed' not in data and 'Load failed' not in data and 'FATAL EXCEPTION' not in data,'no Android playback/load/crash errors')
    (OUT/'android-meminfo.txt').write_text(adb('shell','dumpsys','meminfo',PKG),encoding='utf-8')
    # Leave Final unspawned: the real Mini death should expose Night music.
    launch();wait(lambda:'Music state=BGM_NIGHT' in logs(),55)
    check('SFX event=MINI_DEFEATED' in logs() and 'Music state=BGM_MINI' in logs(),'Mini death returns to selected Night MP3 before Final')
    check('SFX event=LICH_APPEARS' not in logs(),'Night fixture did not invent a Final event');evidence.append(logs())
    print('Android audio smoke PASS',len(checks),flush=True)
finally:
    if pid:
        try:evidence.append(logs())
        except Exception:pass
    (OUT/'android-audio.log').write_text('\n'.join(evidence),encoding='utf-8')
    (OUT/'android-checks.json').write_text(json.dumps({'passed':checks,'count':len(checks),'scope':'API/backend state; protected HP/level3/clock and damage fixtures; no audible claim'},indent=2),encoding='utf-8')
    adb('shell','am','force-stop',PKG)
    for path,value in backup.items():
        if value is None:adb('shell','run-as',PKG,'rm','-f',path)
        else:write(path,value)
    adb('shell','am','start','-W','-n',HOME)
    print('Restored original preferences and local diagnostic flags',flush=True)
