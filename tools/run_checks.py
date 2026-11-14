"""Run after gradlew :lwjgl3:classes. Uses cached project dependencies, never downloads."""
from pathlib import Path
import hashlib
import json
import subprocess

ROOT=Path(__file__).resolve().parents[1]
REPORT=ROOT/'core/build/reports/task01b2'
REPORT.mkdir(parents=True,exist_ok=True)
CACHE=Path.home()/'.gradle/caches/modules-2/files-2.1'
entries=[str(ROOT/'core/build/classes/java/main')]
for group in ['com.badlogicgames.gdx','org.lwjgl','com.badlogicgames.jlayer','org.jcraft']:
    parent=CACHE/group
    if not parent.exists(): continue
    for artifact in parent.iterdir():
        versions=sorted(p for p in artifact.iterdir() if p.is_dir())
        if not versions: continue
        for jar in versions[-1].rglob('*.jar'):
            if not jar.name.endswith(('-sources.jar','-javadoc.jar')): entries.append(str(jar))
out=ROOT/'core/build/verification-classes'
out.mkdir(parents=True,exist_ok=True)
cp=';'.join(entries)
sources=[str(ROOT/'tools/checks/FoundationChecks.java'),str(ROOT/'tools/smoke/SmokeLauncher.java'),str(ROOT/'tools/smoke/TerrainProbe.java'),str(ROOT/'tools/checks/CameraChecks.java')]
subprocess.run(['javac','-encoding','UTF-8','-cp',cp,'-d',str(out),*sources],check=True,cwd=ROOT)
cp+=';'+str(out)
args=REPORT/'java-args.txt'
args.write_text('-cp\n"'+cp.replace('\\','/')+'"\n',encoding='utf-8')
catalog=json.loads((ROOT/'assets/game/catalog.json').read_text())
pack=ROOT/'asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/willosuna_rpg_monsters'
manifest=json.loads((pack/'manifest.json').read_text())
verified=[]
for sprite in catalog['sprites']:
    if sprite['id'] in ('player','tiny_slime','tiny_bat'): continue
    original=next(m for m in manifest['monsters'] if m['name']==sprite['id'])
    png=ROOT/'assets'/sprite['texture']
    assert hashlib.sha256(png.read_bytes()).digest()==hashlib.sha256((pack/'x1'/original['sheet']).read_bytes()).digest()
    assert len(sprite['animations'])==len(original['animations'])
    for a in sprite['animations']:
        ref=next(r for r in original['animations'] if r['animation']==a['state'] and r['direction']==a['direction'])
        assert len(a['frames'])==ref['frames'] and a['fps']==ref['fps'] and a['loop']==ref['loop']
    verified.append({'id':sprite['id'],'png_sha256':hashlib.sha256(png.read_bytes()).hexdigest(),
                     'animationDirections':len(sprite['animations']),'frames':sum(len(a['frames']) for a in sprite['animations'])})
assert len(verified)==10 and len(catalog['textures'])==14
# Cross-check Tiny mapping against actual single-frame paths and GIF block timing.
from tiny_assets import gif_delays
for sprite in catalog['sprites'][:3]:
    assert sprite['id'] in ('player','tiny_slime','tiny_bat')
    for anim in sprite['animations']:
        source=ROOT/anim['source'];pngs=list(source.glob('*.png'))
        assert pngs and len(anim['frames']) in (1,len(pngs))
        folder=next(part for part in source.parts if part in ('warrior_free','slime_free','bat_free'))
        clip=anim['sourceClip'];prefix='' if folder=='warrior_free' else ('slime_' if folder=='slime_free' else 'bat_')
        gif=ROOT/'asset_sources/tiny-questers-free-starter-pack/tiny-questers-free-starter-pack'/folder/'gif/1x'/(prefix+clip.removesuffix('_stay')+'.gif')
        assert abs(anim['fps']-100/gif_delays(gif)[0])<.0001
        assert anim['loop']==(anim['state'] in ('idle','walk'))
assert len(catalog['sprites'])==13
for name,source in [('Kenney-RPG.txt',ROOT/'asset_sources/kenney_roguelike-rpg-pack/License.txt'),
                    ('Tiny-Questers.txt',ROOT/'asset_sources/tiny-questers-free-starter-pack/tiny-questers-free-starter-pack/LICENSE.txt'),
                    ('Willosuna-Free-Demo.txt',ROOT/'asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/LICENSE.txt')]:
    assert (ROOT/'assets/game/licenses'/name).read_bytes()==source.read_bytes()
(REPORT/'assets-verified.json').write_text(json.dumps(verified,indent=2)+'\n')
result=subprocess.run(['java','@'+str(args),'com.example.monstersurvivor.FoundationChecks'],cwd=ROOT/'assets',text=True,capture_output=True)
(REPORT/'foundation.txt').write_text(result.stdout+result.stderr)
print(result.stdout+result.stderr)
result.check_returncode()
subprocess.run(['java','@'+str(args),'CameraChecks'],cwd=ROOT/'assets',check=True)
subprocess.run(['java','@'+str(args),'SmokeLauncher'],cwd=ROOT/'assets',check=True)
subprocess.run(['java','@'+str(args),'TerrainProbe'],cwd=ROOT/'assets',check=True)
print('Asset metadata, deterministic checks and actual OpenGL desktop smoke completed. Reports:',REPORT)
