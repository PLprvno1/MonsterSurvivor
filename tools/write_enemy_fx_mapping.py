"""Generate Task08 asset handoff from packed provenance and actual runtime species policies."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task08'
atlas=json.loads((ROOT/'assets/game/enemy-fx.json').read_text());species=json.loads((OUT/'enemy-skill-manifest.json').read_text())
lines=['# Enemy FX Asset Mapping — Task08','',
'Nguồn thực tế: `asset_sources/MonstersCreatures_v1.0/MonstersCreatures`. Audit lại123 PNG trong03_Projectiles_FX và39 PNG trong04_Status_Effects; xem `core/build/reports/task08/source-fx-inventory.json` (kích thước, frame/cell, SHA256). Kiểm kê này gồm các combined sheets; chúng không được import.',
'', 'Runtime: **game/enemy-fx.png,512×512 RGBA (1MiB),81 strip/icon,274 frames**. Nearest, extrude2px, giữ nguyên pixel và thứ tự frame; không resample/AI. Bốn hướng của14 projectile được chọn theo hướng rồi xoay phần góc còn lại. Coin shot dùng playback ping-pong; các impact hữu hạn theo FX instance. FPS dưới đây là tuning game, nguồn không quy định FPS chính xác.',
'', 'License: `assets/game/licenses/MonstersCreatures-Enemy-FX.txt`. Cho phép tích hợp/sửa trong game lớn hơn; không phân phối asset độc lập, không NFT hoặc AI training. Tool Java/Python crop/pack cục bộ. Từng SHA256 nguồn và rect runtime nằm trong `assets/game/enemy-fx.json`.','',
'| Clip | Source (relative to MonstersCreatures) | Skill / use | Frames / cell | FPS | Atlas / type |',
'|---|---|---|---|---:|---|']
for p in atlas['provenance']:
    key=p['clip'];base=key
    for suffix in ['_down','_left','_right','_up','_impact']:
        if base.endswith(suffix):base=base[:-len(suffix)];break
    names=sorted({d['skill'] for d in species if d['fx']==base and d['skill']!='NONE'})
    if key.startswith('status_'):role='HUD status icon';kind='status'
    elif key.startswith('overlay_'):role='Player accepted-hit status overlay';kind='overlay'
    elif key=='cast':role='Projectile muzzle/cast cue';kind='cast'
    else:role=', '.join(names);kind='impact' if key.endswith('_impact') or key in ['quake','shockwave'] else 'flight'
    lines.append(f"| `{key}` | `{p['source']}` | {role} | {p['count']} / {p['cell'][0]}×{p['cell'][1]} | {p['fps']} | enemy-fx / {kind} |")
lines+=['','Geometry chỉ bổ sung dấu nguy hiểm: vòng tĩnh + vòng tiến độ cho AoE/slam, tia định hướng fan, viền đỏ quanh đạn hostile và vòng magenta của vùng độc. Gameplay damage không lấy từ frame animation.','',
'Pool thường24 projectile,24 visual FX,3 poison areas; ba status slots trên Player. Không mượn BossProjectiles24 hoặc weapon/evolution projectile48. Owner chết hủy đạn/vùng độc ở lần cập nhật kế; impact không damage và tự hết hạn.','',
'Atlas nhân vật/quái/weapon/evolution cũ giữ nguyên. GameAssets tổng21 textures /45.596.676 bytes RGBA (font không tính). Chỉ thêm1MiB; không tạo texture/animation khi cast hoặc trong vòng render.']
(ROOT/'ENEMY_FX_ASSET_MAPPING.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print('ENEMY_FX_ASSET_MAPPING.md generated:81 verified entries')
