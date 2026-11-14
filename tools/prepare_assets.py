"""Prepare only licensed, selected runtime art. Python stdlib + JDK 17; no downloads.

Run from repository root: python tools/prepare_assets.py
Never reads or converts N3XUS art. Original sources are left intact.
"""
from pathlib import Path
import json
import random
import shutil
import struct
import subprocess
from tiny_assets import prepare_tiny

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'assets/game'
SOURCE = ROOT / 'asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2'
PACK = SOURCE / 'willosuna_rpg_monsters'
NAMES = ['honey_slime', 'wild_boar', 'giant_bee', 'red_cap', 'red_crab',
         'giant_frog', 'rock_golem', 'living_armor', 'bone_hound', 'lich_lord']
DIRECTIONS = ['down', 'left', 'right', 'up']


def write(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, separators=(',', ':')) + '\n', encoding='utf-8')


def prepare():
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / 'monsters').mkdir(exist_ok=True)
    (OUT / 'licenses').mkdir(exist_ok=True)
    subprocess.run(['java', 'tools/PrepareKenney.java'], cwd=ROOT, check=True)
    tiny = prepare_tiny()
    catalog = {'textures': ['game/world.png'] + [v['texture'] for v in tiny], 'sprites': tiny}
    manifest = json.loads((PACK / 'manifest.json').read_text(encoding='utf-8'))
    for name in NAMES:
        info = next(x for x in manifest['monsters'] if x['name'] == name)
        src_png = PACK / 'x1' / info['sheet']
        source_meta = PACK / 'x1' / info['sheet_json']
        meta = json.loads(source_meta.read_text(encoding='utf-8'))
        png_w, png_h = struct.unpack('>II', src_png.read_bytes()[16:24])
        assert (png_w, png_h) == (meta['meta']['size']['w'], meta['meta']['size']['h'])
        sprite = {'id': name, 'title': info['title'], 'texture': f'game/monsters/{name}.png',
            'frameWidth': info['frame_w'], 'frameHeight': info['frame_h'],
            'feetX': info['frame_w'] / 2, 'feetY': info['feet_y'], 'animations': []}
        frames = meta['frames']
        tags = {t['name']: t for t in meta['meta']['frameTags']}
        for anim in info['animations']:
            tag = tags[anim['name']]
            prefix = f"{name}_{anim['name']}_"
            keys = sorted(k for k in frames if k.startswith(prefix))
            assert len(keys) == anim['frames'] == tag['to'] - tag['from'] + 1
            assert tag['fps'] == anim['fps'] and tag['loop'] == anim['loop']
            rects = []
            for k in keys:
                frame = frames[k]
                assert not frame['rotated'] and not frame['trimmed']
                r = frame['frame']
                assert (r['w'], r['h']) == (info['frame_w'], info['frame_h'])
                assert r['x'] + r['w'] <= png_w and r['y'] + r['h'] <= png_h
                rects.append([r['x'], r['y'], r['w'], r['h']])
            sprite['animations'].append({'state': anim['animation'], 'direction': anim['direction'],
                'fps': anim['fps'], 'loop': anim['loop'], 'frames': rects})
        catalog['textures'].append(sprite['texture'])
        catalog['sprites'].append(sprite)
        shutil.copyfile(src_png, OUT / 'monsters' / f'{name}.png')
        # Original JSON stays in asset_sources. Only normalized, consumed metadata goes in APK.
        obsolete = OUT / 'monsters' / f'{name}.source.json'
        if obsolete.exists():
            obsolete.unlink()
    regions = {}
    for line in (ROOT/'core/build/asset-tools/world-regions.tsv').read_text().splitlines():
        name, *rect = line.split('\t'); regions[name] = list(map(int, rect))
    catalog['regions'] = regions
    write(OUT / 'catalog.json', catalog)
    for name, source in [('Kenney-RPG.txt', ROOT/'asset_sources/kenney_roguelike-rpg-pack/License.txt'),
                         ('Tiny-Questers.txt', ROOT/'asset_sources/tiny-questers-free-starter-pack/tiny-questers-free-starter-pack/LICENSE.txt'),
                         ('Willosuna-Free-Demo.txt', SOURCE/'LICENSE.txt')]:
        shutil.copyfile(source, OUT / 'licenses' / name)
    (OUT / 'licenses/CREDITS.txt').write_text(
        'Environment: Kenney (kenney.nl), CC0.\n'
        'Pixel art by Bobddadoo https://bobddadoo.itch.io (Tiny Questers Warrior, Slime, Bat).\n'
        'Monsters: Art by Willosuna (willosuna.itch.io), Free Demo license.\n'
        'Art is distributed only as part of this game; no standalone redistribution.\n', encoding='utf-8')
    for old in [OUT/'player.png', OUT/'licenses/Kenney-Characters.txt']:
        if old.exists(): old.unlink()
    make_map(list(regions))
    print(f'Prepared {len(NAMES)} x1 monster sheets; metadata rectangles/tags verified against PNG dimensions.')


def make_map(region_names):
    width = height = 100
    rng = random.Random(10101)
    grass = region_names.index('grass')
    ground = [grass] * (width * height)
    collision = [0] * (width * height)
    props = []
    # JSON rows use y-up world coordinates. Boundary bit=8, tree=1, rock=2, water=4.
    for y in range(height):
        for x in range(width):
            idx = y * width + x
            if x in (0,99) or y in (0,99):
                collision[idx] = 8
            if abs(x - 50) <= 1 and abs(y - 50) > 10:
                ground[idx] = region_names.index('dirt')
            if abs(y - 50) <= 1 and abs(x - 50) > 14:
                ground[idx] = region_names.index('dirt')
    for left, bottom, w, h in [(65,61,15,11), (20,67,12,9), (67,22,10,13)]:
        for y in range(bottom, bottom+h):
            for x in range(left, left+w):
                col = 'w' if x == left else 'e' if x == left+w-1 else ''
                row = 's' if y == bottom else 'n' if y == bottom+h-1 else ''
                key = 'shore_' + row + col if row or col else 'water'
                ground[y*width+x] = region_names.index(key)
                collision[y*width+x] = 4
    for y in range(height):
        for x in range(width):
            idx = y*width+x
            clear = ((x-50)/13)**2 + ((y-50)/9)**2 <= 1
            path = abs(x-50) <= 2 or abs(y-50) <= 2
            if clear or (path and not collision[idx]) or collision[idx] == 4:
                continue
            edge = min(x, y, 99-x, 99-y)
            probability = .55 if edge < 3 else .21 if edge < 18 else .22
            if collision[idx] == 8 or rng.random() < probability:
                kind = rng.choices(['tree','pine','rock','bush','stump'], [35,35,18,7,5])[0]
                collision[idx] |= 2 if kind == 'rock' else 1
                props.append({'region': kind, 'x': x*16+8, 'y': y*16+2})
    # Seal tiny isolated grass pockets with visible bushes. Every remaining navigation
    # cell is reachable from the meadow; no runtime pathfinding is introduced here.
    reached = {50*width+50}
    pending = [50*width+50]
    while pending:
        current = pending.pop()
        x, y = current % width, current // width
        for nx, ny in [(x-1,y), (x+1,y), (x,y-1), (x,y+1)]:
            nxt = ny*width+nx
            if 0 <= nx < width and 0 <= ny < height and not collision[nxt] and nxt not in reached:
                reached.add(nxt)
                pending.append(nxt)
    for idx in range(width*height):
        if not collision[idx] and idx not in reached:
            collision[idx] = 1
            props.append({'region': 'bush', 'x': (idx%width)*16+8, 'y': (idx//width)*16+2})
    print(f'Forest navigation: {len(reached)} connected walkable cells, {len(props)} visible obstacles.')
    assert not any(collision[y*100+x] for y in range(44,57) for x in range(41,60))
    write(OUT / 'forest.json', {'width':100, 'height':100, 'tileSize':16,
        'regionOrder':region_names, 'ground':ground, 'collision':collision, 'props':props,
        'spawnX':808, 'spawnY':808, 'seed':10101})


if __name__ == '__main__':
    prepare()
