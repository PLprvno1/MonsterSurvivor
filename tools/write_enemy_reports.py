"""Reproducible roster and future FX notes from the actual import plan, not hand-copied counts."""
from pathlib import Path
import collections,json,re,struct
ROOT=Path(__file__).resolve().parents[1]
def generate(plan):
    lines=['# Enemy roster — Task 04A','',
        '82 normal definitions: 40 MonstersCreatures base species + 27 recolor variants + 9 existing Willosuna + 2 Tiny Questers + 4 SpriteCanvas. All are registered for natural weighted spawn. New sources share one 2048x2048 atlas; old sprite assets remain unchanged. Bosses are excluded.','',
        'Tune `tools/enemy_balance.json`, then run `python tools/prepare_enemy_assets.py`. This regenerates the atlas, runtime animation catalog and the pure-Java registry data together. IDs are globally unique; `mc_` and `sc_` prefixes prevent collisions with existing Living Armor/Bone Hound. `EnemyType` is only the original three-type API adapter; enumerate `EnemyRegistry.all()` for the complete roster.','',
        '## Shared behavior and animation','',
        '- CHASER: direct chase; cached A* on obstruction; contact damage.',
        '- FAST_MELEE: shared chase with faster configured speed and modest HP. Only the existing Wild Boar retains its charge; no new burst skill is claimed.',
        '- TANK: higher HP, lower speed; late unlock for large/strong tanks.',
        '- FLYING: only WATER is passable in addition to ordinary ground; trees, rocks and map boundary still block.',
        '- NIGHT: progress gate and post-300 spawn preference; does not imply wall phasing.',
        '- RANGED_CANDIDATE / SPECIAL_CANDIDATE: safe chase/contact now; metadata names the future skill. No ranged/projectile/special behavior is claimed implemented.',
        '', 'All use the same EnemySystem, real-delta animation clock, Enemy.damage/death hook, shared player immunity, collision grid, separation and two-search/frame budget. Collision dimensions are per definition, constrained to fit the 16px navigation cell; actual LOS and swept movement use them. Foot positions/scales come from the same generated source as animation metadata.','',
        'MC mapping: idle/walk loop, attack/hurt/death play once; SPECIAL is retained but not automatically played without its future behavior. Contact damage remains the existing overlap event, not a newly delayed hit at source attack frame 2. SpriteCanvas uses JSON frame-duration milliseconds; LEFT is source, RIGHT mirrored, UP/DOWN use the side view. Its uniform Nearest normalization and fixed pivot are intentional adapters.','',
        '## Base combat tuning','',
        'HP/DMG columns are base Easy/Hard before stage bonuses. Existing stage HP increments remain small: ordinary definitions use the Slime bonus, tanks/elites use the Boar bonus; legacy aliases retain their type-specific stage columns. Task 04A.1 stage tuning, strong/elite weight multipliers and late Hard damage floors live in GameConfig; the base definition tables below stay unchanged. Hard movement is x1.10. Source suggested HP/DMG values are not copied directly.','',
        '| ID | Name | Biome | Archetype | HP E/H | DMG E/H | Speed | Half footprint | Scale | Feet x,y |',
        '| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |']
    for d in plan['definitions']:
        lines.append(f"| {d['id']} | {d['name']} | {d['biome']} | {d['archetype']} | {d['easyHp']}/{d['hardHp']} | {d['damage']}/{d['hardDamage']} | {d['speed']} | {d['halfWidth']}x{d['halfHeight']} | {d['scale']} | {d['feetX']:.3f},{d['feetY']:.3f} |")
    lines+=['','## Spawn tuning','',
        'Weights are relative, not percentages. Each match gets seeded x0.80–1.25 weight variation. Hard/night factors and GameConfig stage strong/elite multipliers multiply the base. Per-species resident limits count corpses, and every family (base plus recolors) is limited to ceil(25% of the existing 40/60 cap). Repeated picks are damped after two. Short seeded 18-progress-second biome themes after 300 temporarily multiply weight x1.65; they never bypass unlocks or add spawn bursts.','',
        '0–120: seven initial weak definitions, then light Forest/Plains/Goblin. 120–240: more Cave/Desert. 240–300: Coast/Swamp. 300–420: Night preference plus Snow. 420–540: tanks/Volcanic and rare recolors. 540–600: unlocked roster continues; `lich_lord` at 540 is a future boss contract only. Natural spawn stops at 600 without declaring Victory; living actors remain.','',
        'Spawn cadence/density and resident limits Easy 40 / Hard 60 are unchanged. Placement uses 64 random attempts plus at most 256 local fallback cells around the current camera; it never searches the entire 200x200 map per enemy or falls back to remote map corners. Normal/Demo share progress; movement/animation/cooldowns use real gameplay delta.','',
        '| ID | Unlock progress s | Base weight | Hard factor | Night factor | Type resident max | Family | Future skill metadata |',
        '| --- | --- | --- | --- | --- | --- | --- | --- |']
    for d in plan['definitions']:lines.append(f"| {d['id']} | {d['unlock']} | {d['weight']:.3f} | {d['hardWeight']} | {d['nightWeight']} | {d['residentLimit']} | {d['family']} | {d['special']} |")
    lines+=['','## Functional coverage','',
        'Per-definition evidence: `core/build/reports/task04a/per-type-checks.json`. Every ID is actually spawned and killed by Sword, Bow, Fireball and Lightning in both Normal and Demo fixtures, with exactly one kill and a forced-drop diagnostic. All also chase, display Walk/Hurt/Attack/Death, damage the player on contact and finish actions. Separate tree/water fixtures exercise actual footprint movement and bounded A*. GameScreen/OpenGL tests cover every ID and three Retry cycles; a catalog/preview alone is not counted as gameplay integration.','',
        'Weighted sampling is probabilistic: all 82 have a positive eligible weight before 600, but one ordinary match is not guaranteed to show every rare variant. Bosses, future ranged skills and future specials are deliberately not part of this completion claim.']
    (ROOT/'ENEMY_ROSTER.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    counts=collections.Counter(f['category'] for f in plan['fx']);fx={Path(f['file']).name:f for f in plan['fx']}
    lines=['# Combat FX integration plan — research only, Task 04A','',
        'No new combat FX, status gameplay, loot system or UI is enabled in this task. Task 04B can implement enemy skills; full combat FX remains a later task (04C). Existing weapon VFX and pickup presentation remain unchanged.','',
        '## Actual sources','',
        'MonstersCreatures README/LICENSE were read; all files below were enumerated by actual PNG dimensions, and 391 FX/bestiary PNGs were successfully decoded. Counts include duplicate combined sheets; they are not unique-frame counts. No standalone asset delivery is prepared.','',
        '| Folder | Actual PNG count | Scope |','| --- | --- | --- |']
    for key,count in counts.items():lines.append(f"| {key} | {count} | {'Reference only' if key=='07_Bestiary' else 'Candidate for future integration'} |")
    lines+=['',
        'Source provides names/frame counts in strip filenames and general README layouts. There is no per-FX JSON or authored exact FPS in this pack. Future playback must be configured and playtested, not represented as exact source timing. Directional projectiles have separate right/left/up/down strips; impacts are 4-frame strips. Status overlays have 4 frames with 16px and 32px versions; icons are static.','',
        '## Proposed mapping','',
        '| Use | Actual candidate | Trigger / future integration |','| --- | --- | --- |',
        '| Sword | slash_white_32x32_4f, hit_spark_16x16_4f | Attach slash to the committed sword action; impact at the single hit event. Keep direction/pivot tied to attack, independent of joystick. |',
        '| Bow | arrow_right_2f plus directional equivalents, arrow_impact_4f | Replace only projectile presentation; preserve swept collision, penetration and unique-hit IDs. |',
        '| Fireball | fireball_right_4f and directions, fireball_impact_4f, explosion_32x32_6f, slash_fire_32x32_4f | Use projectile age and the one explosion event. Never derive damage ticks from animation frames. |',
        '| Lightning | magic_orb_impact_4f, sonic_wave_impact_4f, slash_spirit_32x32_4f | Impact accents are candidates. No dedicated lightning-bolt strip exists; retain current geometric bolt/chain or explicitly design a compatible future bolt. |',
        '| Enemy melee | claw_marks_16x16_3f, bite_16x16_4f, ground_quake_32x32_5f | Future attack event once, owner/team filtering, readable telegraph; tank slam is not enabled now. |',
        '| Enemy ranged | thorn, seed, hex_bolt, spore, bomb, shadow_orb, rock, snowball, lava_glob and impacts | Candidate special names map to these families; future EnemyProjectile ownership/lifetime and collision are required. |',
        '| Utility | spawn_32x32_6f, teleport_16x32_6f, smoke_16x32_6f, heal_16x32_5f, shield_bubble_32x32_4f, elite_aura_32x32_4f | Visual only until an explicit behavior/status system exists; cap overlays and attach to feet. |',
        '| Status | poison/burn/freeze/stun/sleep/charm/bleed/slow/haste/shield/weaken/wet | An overlay/icon is not a completed status effect; define duration, stacking, immunity and pause separately. |',
        '| Loot/UI | drop icons, coins/gems/orbs, chest-open strips, damage numbers, markers, boss bar/banners | Research only; do not change existing Damage/Health/Speed/Weapon rewards, health drop probabilities or native UI. |',
        '', '## Level/evolution presentation contract','',
        'Keep WeaponConfig level tuning and pooled effect snapshots. A later sprite FX profile can select clip, tint, scale and playback per level. Evolution requires an explicit eligibility/state after level 5, a recognizable new animation/VFX and meaningful gameplay change, in the existing weapon slot. Damage alone or recoloring an unchanged hit is insufficient. No evolution is implemented here.','',
        '## Resource/render budget','',
        'Prepare only selected strips, not combined sheets plus every duplicate. A curated 256x256 atlas is 0.25 MiB RGBA; 512x512 is 1 MiB. Extrusion adds 4 px to each cell extent; preserve Nearest and original pixels. Start with one shared atlas, no full-screen shader/FBO and bounded pooled effect capacity (existing cap 48). Avoid extra blend layers for every resident. Load once before use and never unload while referenced.','',
        'Example minimum padded cell storage: hit spark 4x(16+4)x(16+4)x4 = 6,400 bytes; explosion 6x(32+4)x(32+4)x4 = 31,104 bytes, before atlas rounding. Rendering an effect may add texture switches; batching and culling must be measured on SwiftShader. The current new enemy atlas is separate and has no FX import cost.','',
        '## Actual file appendix','',
        '| File | PNG size | Frame information | Raw RGBA bytes |','| --- | --- | --- | --- |']
    for f in plan['fx']:
        if f['category']=='07_Bestiary':continue
        w,h=f['size'];m=re.search(r'_(\d+)f(?:\b|_)',Path(f['file']).stem)
        info=(m[1]+' horizontal frames; cell '+str(w//int(m[1]))+'x'+str(h)) if m and w%int(m[1])==0 else 'Static or combined sheet; use README layout'
        lines.append(f"| `{f['file']}` | {w}x{h} | {info} | {w*h*4} |")
    lines+=['','No optional hit-FX sample was added: the complete roster/import/spawn work has priority and existing combat presentation remains stable.']
    (ROOT/'COMBAT_FX_INTEGRATION_PLAN.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    # Account for non-gameplay folders too, so the inventory covers the complete source tree.
    pack=ROOT/'asset_sources/MonstersCreatures_v1.0/MonstersCreatures';summary=['','## Complete source tree summary','','| Top-level folder | Files | Extensions |','| --- | --- | --- |']
    for folder in sorted(p for p in pack.iterdir() if p.is_dir()):
        files=[p for p in folder.rglob('*') if p.is_file()];extensions=dict(collections.Counter(p.suffix.lower() for p in files));summary.append(f'| {folder.name} | {len(files)} | {extensions} |')
    summary+=['','Example scenes, previews, palette and bestiary are reference material and are not copied into runtime. Boss source sheets/strips are metadata-only; Tide Kraken actually has seven sheet columns and a seven-frame Death, unlike the generic eight-frame Death description.']
    path=ROOT/'ENEMY_ASSET_INVENTORY.md'
    inventory=path.read_text(encoding='utf-8').split('\n## Complete source tree summary',1)[0].rstrip()+'\n'
    path.write_text(inventory+'\n'.join(summary)+'\n',encoding='utf-8')
if __name__=='__main__':
    generate(json.loads((ROOT/'core/build/reports/task04a/enemy-import-plan.json').read_text(encoding='utf-8')))
