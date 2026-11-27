"""Read-only Task08 source audit. Combined sheets remain explicitly unparsed; no guessed frame counts."""
from pathlib import Path
import json,re,struct,hashlib
ROOT=Path(__file__).resolve().parents[1];SOURCE=ROOT/'asset_sources/MonstersCreatures_v1.0/MonstersCreatures';OUT=ROOT/'core/build/reports/task08'
def metadata(path):
    data=path.read_bytes();w,h=struct.unpack('>II',data[16:24]);m=re.search(r'_(\d+)f\.png$',path.name)
    count=int(m[1]) if m else 1 if path.parent.name=='Icons' else None
    if count is not None:assert w%count==0
    return {'source':path.relative_to(SOURCE).as_posix(),'width':w,'height':h,'frames':count,'cell':[w//count,h] if count else None,'layout':'horizontal strip' if m else 'static icon' if count else 'combined sheet; not imported','sha256':hashlib.sha256(data).hexdigest()}
items=[metadata(p) for g in ['03_Projectiles_FX','04_Status_Effects'] for p in sorted((SOURCE/g).rglob('*.png'))]
assert sum(x['source'].startswith('03_') for x in items)==123 and sum(x['source'].startswith('04_') for x in items)==39
poses=[metadata(p) for g in sorted(SOURCE.glob('01_Monsters_*')) for p in sorted(g.rglob('*.png')) if re.search(r'_(attack|special)_(down|left|right|up)_\d+f\.png$',p.name)]
(OUT/'source-fx-inventory.json').write_text(json.dumps(items,indent=2));(OUT/'monster-attack-special-inventory.json').write_text(json.dumps(poses,indent=2))
atlas=json.loads((ROOT/'assets/game/enemy-fx.json').read_text())
for p in atlas['provenance']:assert hashlib.sha256((SOURCE/p['source']).read_bytes()).hexdigest()==p['sha256']
assert (SOURCE/'LICENSE.txt').read_bytes()==(ROOT/'assets/game/licenses/MonstersCreatures-Enemy-FX.txt').read_bytes()
print('Source audit PASS:123 FX,39 status PNGs,',len(poses),'actual monster Attack/Special strips;',sum(x['frames'] is None for x in items),'combined sheets explicitly not parsed;81 imported provenance hashes/license match')
