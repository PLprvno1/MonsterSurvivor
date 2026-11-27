"""Validate the authoritative v2 selection, decode only selected files, import byte-identical runtime copies.
No source writes, packaging, installation or candidate substitution. --check verifies without copying.
"""
from pathlib import Path
import hashlib, json, os, shutil, subprocess, sys
from audit_audio_assets import ogg_metadata, mp3_tags

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'core/build/reports/task05_1b'
SELECTION = ROOT / 'monster-survivor-audio-selections.json'
RUNTIME = ROOT / 'assets/audio'
EXPECTED = '''BGM_MENU BGM_DAY BGM_NIGHT BGM_MINI BGM_FINAL BGM_VICTORY BGM_GAME_OVER
WEAPON_SWORD_SWING WEAPON_SWORD_HIT WEAPON_BOW_RELEASE WEAPON_ARROW_HIT WEAPON_FIRE_CAST WEAPON_FIRE_EXPLOSION WEAPON_LIGHTNING_ACTIVATE WEAPON_LIGHTNING_IMPACT
PICKUP_DAMAGE PICKUP_HEALTH PICKUP_SPEED PICKUP_HEALTH_DROP PICKUP_WEAPON_OPEN PICKUP_WEAPON_SELECTED PICKUP_WEAPON_UPGRADED
MINI_APPEARS MINI_SLAM_WARNING MINI_SLAM_IMPACT MINI_DEFEATED LICH_APPEARS LICH_PROJECTILE LICH_AOE_WARNING LICH_AOE_IMPACT LICH_PHASE2 LICH_DEFEATED
UI_TAP UI_NAVIGATION UI_CONFIRM UI_CANCEL UI_PAUSE UI_RESUME UI_GAME_OVER UI_VICTORY
ENEMY_HIT ENEMY_DEATH ENEMY_FLYING_REACTION ENEMY_BEAST_REACTION ENEMY_UNDEAD_REACTION ENEMY_TANK_REACTION ENEMY_LEGACY_CHARGE PLAYER_HURT PLAYER_DEATH'''.split()

def sha(path): return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    OUT.mkdir(parents=True, exist_ok=True)
    original = sha(SELECTION)
    data = json.loads(SELECTION.read_text(encoding='utf-8-sig'))
    assert data['schema_version'] == 2
    events = data['events']; ids = [e['event_id'] for e in events]
    assert len(ids) == len(set(ids)) == 49 and set(ids) == set(EXPECTED)
    assert all(e['status'] == 'selected' for e in events)
    paths = sorted({e['file_path'] for e in events}); assert len(paths) == 36
    rows = []
    for path in paths:
        assert path.startswith('asset_sources/audio/') and '..' not in Path(path).parts
        source = ROOT / path
        assert source.is_file() and source.suffix in ('.ogg', '.mp3') and 'preview' not in path.lower(), path
        raw = source.read_bytes()
        row = dict(path=path, absolute=str(source.resolve()), bytes=len(raw), sha256=sha(source))
        row.update(ogg_metadata(raw) if source.suffix == '.ogg' else mp3_tags(raw))
        assert not row.get('containerErrors'), row
        rows.append(row)
    # Same cached decoder/JDK as the existing audit; no audio device or dependency install.
    cache = Path.home()/'.gradle/caches/modules-2/files-2.1'
    jars = []
    for relative in ['com.badlogicgames.gdx/gdx/1.14.2','com.badlogicgames.gdx/gdx-backend-lwjgl3/1.14.2','org.jcraft/jorbis/0.0.17','com.badlogicgames.jlayer/jlayer/1.0.1-gdx']:
        found = [p for p in (cache/relative).rglob('*.jar') if not p.name.endswith(('-sources.jar','-javadoc.jar'))]
        assert len(found) == 1, relative
        jars += found
    version = sorted(p for p in (cache/'org.lwjgl/lwjgl').iterdir() if p.is_dir())[-1]
    jars += [p for p in version.rglob('*.jar') if p.name in (f'lwjgl-{version.name}.jar',f'lwjgl-{version.name}-natives-windows.jar')]
    classes=OUT/'probe-classes'; classes.mkdir(exist_ok=True)
    cp=os.pathsep.join(map(str,jars))
    subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(ROOT/'tools/audio/AudioProbe.java')],check=True)
    (OUT/'probe-input.json').write_text(json.dumps(rows),encoding='utf-8')
    args=OUT/'probe-args.txt'; args.write_text('-cp\n"'+(cp+os.pathsep+str(classes)).replace('\\','/')+'"\n',encoding='utf-8')
    result=subprocess.run(['java','@'+str(args),'AudioProbe',str(OUT/'probe-input.json'),str(OUT/'decoded.json')],capture_output=True,text=True)
    (OUT/'decode.log').write_text(result.stdout+result.stderr,encoding='utf-8'); result.check_returncode()
    decoded={r['path']:r for r in json.loads((OUT/'decoded.json').read_text())}
    lookup={r['path']:r for r in rows}
    manifest=[]
    for e in events:
        event=e['event_id']; path=e['file_path']; pcm=decoded[path]
        assert pcm['decode']=='OK', (event,pcm)
        music=event.startswith('BGM_'); category='MUSIC' if music else 'ENEMY' if event.startswith('ENEMY_') else 'WEAPON' if event.startswith('WEAPON_') else 'PICKUP' if event.startswith('PICKUP_') else 'UI' if event.startswith('UI_') else 'CRITICAL'
        priority=100 if category=='CRITICAL' or event in ('UI_GAME_OVER','UI_VICTORY') else 70 if category=='WEAPON' else 60 if category in ('PICKUP','UI') else 20
        gain=.26 if music else .24 if category=='ENEMY' else .65 if category=='CRITICAL' else .5
        if event=='WEAPON_SWORD_SWING': gain=1.0 # source deliberately quiet; no substitution/normalization
        if event in ('BGM_MINI','BGM_FINAL'): gain=.22
        cooldown=180 if category=='ENEMY' else 65 if category=='WEAPON' else 80 if category=='UI' else 0
        manifest.append(dict(event_id=event,source=path,file='audio/'+path.removeprefix('asset_sources/audio/'),category=category,gain=gain,priority=priority,cooldown_ms=cooldown,loop=music and event not in ('BGM_VICTORY','BGM_GAME_OVER'),duration=lookup[path].get('duration',pcm['decodedSeconds']),pcm16_bytes=pcm['pcm16Bytes'],sha256=lookup[path]['sha256']))
        if not music: assert pcm['pcm16Bytes'] < 1048576, event
    allowed={e['file'] for e in manifest}
    existing={'audio/'+p.relative_to(RUNTIME).as_posix() for p in RUNTIME.rglob('*') if p.suffix.lower() in ('.ogg','.mp3')}
    assert not existing-allowed, ('Unexpected runtime audio; refusing to remove automatically',existing-allowed)
    if '--check' not in sys.argv:
        for path in paths:
            target=RUNTIME/path.removeprefix('asset_sources/audio/'); target.parent.mkdir(parents=True,exist_ok=True)
            shutil.copyfile(ROOT/path,target)
        (RUNTIME/'manifest.json').write_text(json.dumps(dict(schema_version=1,selection_sha256=original,events=manifest),indent=2)+'\n',encoding='utf-8')
        licenses=RUNTIME/'licenses'; licenses.mkdir(exist_ok=True)
        for pack in ('kenney_interface-sounds','kenney_rpg-audio','kenney_impact-sounds'):
            shutil.copyfile(ROOT/'asset_sources/audio'/pack/'License.txt',licenses/(pack+'.txt'))
        (licenses/'CREDITS.md').write_text('''# Selected audio credits

Kenney Interface Sounds, RPG Audio, Impact Sounds: Kenney, CC0-1.0. Local licenses preserved alongside this file.
https://kenney.nl/assets/interface-sounds
https://kenney.nl/assets/rpg-audio
https://kenney.nl/assets/impact-sounds

end_dungeon.mp3: HydroGene, CC0; https://opengameart.org/content/jrpg-end-dungeon
heavy_boss_battle_1_bpm200.ogg: MintoDog, CC0; https://opengameart.org/content/heavy-boss-battle-1
hope_orchestral_battle_music_bpm165.ogg: MintoDog, CC0; https://opengameart.org/content/hopeorchestral-battle-music

BGM provenance retained from the earlier source audit (Zone.Identifier/tags and individual source-page verification). No remote byte identity claimed. Runtime copies are byte-identical; no trim, normalization or transcoding.
''',encoding='utf-8')
    actual=json.loads((RUNTIME/'manifest.json').read_text())
    assert actual['selection_sha256']==original and actual['events']==manifest
    for e in manifest: assert sha(ROOT/'assets'/e['file']) == sha(ROOT/e['source']) == e['sha256']
    unique_sfx={e['file']:e for e in manifest if e['category']!='MUSIC'}
    summary=dict(events=len(events),unique_files=len(paths),music_files=3,sfx_files=len(unique_sfx),compressed_bytes=sum(r['bytes'] for r in rows),sfx_pcm16_bytes=sum(e['pcm16_bytes'] for e in unique_sfx.values()),selection_sha256=original,decode='36/36 OK',source_unchanged=all(sha(ROOT/r['path'])==r['sha256'] for r in rows))
    assert sha(SELECTION)==original
    (OUT/'asset-validation.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(summary,indent=2))

if __name__=='__main__': main()
