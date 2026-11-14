# Combat FX integration plan — research only, Task 04A

No new combat FX, status gameplay, loot system or UI is enabled in this task. Task 04B can implement enemy skills; full combat FX remains a later task (04C). Existing weapon VFX and pickup presentation remain unchanged.

## Actual sources

MonstersCreatures README/LICENSE were read; all files below were enumerated by actual PNG dimensions, and 391 FX/bestiary PNGs were successfully decoded. Counts include duplicate combined sheets; they are not unique-frame counts. No standalone asset delivery is prepared.

| Folder | Actual PNG count | Scope |
| --- | --- | --- |
| 03_Projectiles_FX | 123 | Candidate for future integration |
| 04_Status_Effects | 39 | Candidate for future integration |
| 05_Loot_Drops | 70 | Candidate for future integration |
| 06_Combat_UI | 65 | Candidate for future integration |
| 07_Bestiary | 94 | Reference only |

Source provides names/frame counts in strip filenames and general README layouts. There is no per-FX JSON or authored exact FPS in this pack. Future playback must be configured and playtested, not represented as exact source timing. Directional projectiles have separate right/left/up/down strips; impacts are 4-frame strips. Status overlays have 4 frames with 16px and 32px versions; icons are static.

## Proposed mapping

| Use | Actual candidate | Trigger / future integration |
| --- | --- | --- |
| Sword | slash_white_32x32_4f, hit_spark_16x16_4f | Attach slash to the committed sword action; impact at the single hit event. Keep direction/pivot tied to attack, independent of joystick. |
| Bow | arrow_right_2f plus directional equivalents, arrow_impact_4f | Replace only projectile presentation; preserve swept collision, penetration and unique-hit IDs. |
| Fireball | fireball_right_4f and directions, fireball_impact_4f, explosion_32x32_6f, slash_fire_32x32_4f | Use projectile age and the one explosion event. Never derive damage ticks from animation frames. |
| Lightning | magic_orb_impact_4f, sonic_wave_impact_4f, slash_spirit_32x32_4f | Impact accents are candidates. No dedicated lightning-bolt strip exists; retain current geometric bolt/chain or explicitly design a compatible future bolt. |
| Enemy melee | claw_marks_16x16_3f, bite_16x16_4f, ground_quake_32x32_5f | Future attack event once, owner/team filtering, readable telegraph; tank slam is not enabled now. |
| Enemy ranged | thorn, seed, hex_bolt, spore, bomb, shadow_orb, rock, snowball, lava_glob and impacts | Candidate special names map to these families; future EnemyProjectile ownership/lifetime and collision are required. |
| Utility | spawn_32x32_6f, teleport_16x32_6f, smoke_16x32_6f, heal_16x32_5f, shield_bubble_32x32_4f, elite_aura_32x32_4f | Visual only until an explicit behavior/status system exists; cap overlays and attach to feet. |
| Status | poison/burn/freeze/stun/sleep/charm/bleed/slow/haste/shield/weaken/wet | An overlay/icon is not a completed status effect; define duration, stacking, immunity and pause separately. |
| Loot/UI | drop icons, coins/gems/orbs, chest-open strips, damage numbers, markers, boss bar/banners | Research only; do not change existing Damage/Health/Speed/Weapon rewards, health drop probabilities or native UI. |

## Level/evolution presentation contract

Keep WeaponConfig level tuning and pooled effect snapshots. A later sprite FX profile can select clip, tint, scale and playback per level. Evolution requires an explicit eligibility/state after level 5, a recognizable new animation/VFX and meaningful gameplay change, in the existing weapon slot. Damage alone or recoloring an unchanged hit is insufficient. No evolution is implemented here.

## Resource/render budget

Prepare only selected strips, not combined sheets plus every duplicate. A curated 256x256 atlas is 0.25 MiB RGBA; 512x512 is 1 MiB. Extrusion adds 4 px to each cell extent; preserve Nearest and original pixels. Start with one shared atlas, no full-screen shader/FBO and bounded pooled effect capacity (existing cap 48). Avoid extra blend layers for every resident. Load once before use and never unload while referenced.

Example minimum padded cell storage: hit spark 4x(16+4)x(16+4)x4 = 6,400 bytes; explosion 6x(32+4)x(32+4)x4 = 31,104 bytes, before atlas rounding. Rendering an effect may add texture switches; batching and culling must be measured on SwiftShader. The current new enemy atlas is separate and has no FX import cost.

## Actual file appendix

| File | PNG size | Frame information | Raw RGBA bytes |
| --- | --- | --- | --- |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/bite_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/claw_marks_16x16_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/death_poof_16x16_6f.png` | 96x16 | 6 horizontal frames; cell 16x16 | 6144 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/death_poof_32x32_6f.png` | 192x32 | 6 horizontal frames; cell 32x32 | 24576 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/elite_aura_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/explosion_32x32_6f.png` | 192x32 | 6 horizontal frames; cell 32x32 | 24576 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/ground_quake_32x32_5f.png` | 160x32 | 5 horizontal frames; cell 32x32 | 20480 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/heal_16x32_5f.png` | 80x32 | 5 horizontal frames; cell 16x32 | 10240 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/hit_spark_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/shield_bubble_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/shockwave_64x32_6f.png` | 384x32 | 6 horizontal frames; cell 64x32 | 49152 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/slash_fire_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/slash_spirit_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/slash_white_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/smoke_16x32_6f.png` | 96x32 | 6 horizontal frames; cell 16x32 | 12288 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/spawn_32x32_6f.png` | 192x32 | 6 horizontal frames; cell 32x32 | 24576 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX/teleport_16x32_6f.png` | 96x32 | 6 horizontal frames; cell 16x32 | 12288 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/arrow_down_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/arrow_left_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/arrow_right_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/arrow_up_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bomb_down_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bomb_left_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bomb_right_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bomb_up_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bone_shard_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bone_shard_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bone_shard_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bone_shard_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bubble_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bubble_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bubble_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/bubble_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/coin_shot_down_pingpong_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/coin_shot_left_pingpong_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/coin_shot_right_pingpong_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/coin_shot_up_pingpong_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/fireball_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/fireball_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/fireball_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/fireball_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/hex_bolt_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/hex_bolt_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/hex_bolt_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/hex_bolt_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ice_shard_down_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ice_shard_left_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ice_shard_right_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ice_shard_up_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/arrow_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/bomb_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/bone_shard_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/bubble_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/coin_shot_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/fireball_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/hex_bolt_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/ice_shard_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/ink_blob_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/lava_glob_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/magic_orb_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/poison_spit_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/rock_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/seed_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/shadow_orb_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/snowball_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/sonic_wave_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/spore_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/thorn_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/Impacts/web_impact_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ink_blob_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ink_blob_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ink_blob_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/ink_blob_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/lava_glob_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/lava_glob_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/lava_glob_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/lava_glob_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/magic_orb_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/magic_orb_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/magic_orb_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/magic_orb_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/poison_spit_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/poison_spit_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/poison_spit_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/poison_spit_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/rock_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/rock_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/rock_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/rock_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/seed_down_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/seed_left_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/seed_right_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/seed_up_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/shadow_orb_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/shadow_orb_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/shadow_orb_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/shadow_orb_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/snowball_down_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/snowball_left_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/snowball_right_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/snowball_up_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/sonic_wave_down_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/sonic_wave_left_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/sonic_wave_right_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/sonic_wave_up_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/spore_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/spore_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/spore_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/spore_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/thorn_down_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/thorn_left_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/thorn_right_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/thorn_up_2f.png` | 32x16 | 2 horizontal frames; cell 16x16 | 2048 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/web_down_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/web_left_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/web_right_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Projectiles/web_up_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Shadows/shadow_dither_16x16.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Shadows/shadow_dither_32x32.png` | 32x32 | Static or combined sheet; use README layout | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Shadows/shadow_dither_64x64.png` | 64x64 | Static or combined sheet; use README layout | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Shadows/shadow_solid_16x16.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Shadows/shadow_solid_32x32.png` | 32x32 | Static or combined sheet; use README layout | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Shadows/shadow_solid_64x64.png` | 64x64 | Static or combined sheet; use README layout | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_bleed.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_burn.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_charm.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_freeze.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_haste.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_poison.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_shield.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_sleep.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_slow.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_stun.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_weaken.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Icons/status_wet.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_bleed_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_burn_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_charm_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_freeze_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_haste_16x16_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_poison_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_shield_16x16_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_sleep_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_slow_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_stun_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_weaken_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_16x16/overlay_wet_16x16_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_bleed_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_burn_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_charm_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_freeze_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_haste_32x32_3f.png` | 96x32 | 3 horizontal frames; cell 32x32 | 12288 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_poison_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_shield_32x32_3f.png` | 96x32 | 3 horizontal frames; cell 32x32 | 12288 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_sleep_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_slow_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_stun_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_weaken_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/Overlays_32x32/overlay_wet_32x32_4f.png` | 128x32 | 4 horizontal frames; cell 32x32 | 16384 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/status_icons_16x16_sheet.png` | 192x16 | Static or combined sheet; use README layout | 12288 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/status_overlays_16x16_sheet.png` | 64x192 | Static or combined sheet; use README layout | 49152 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/04_Status_Effects/status_overlays_32x32_sheet.png` | 128x384 | Static or combined sheet; use README layout | 196608 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Chests/chest_boss_open_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Chests/chest_gold_open_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Chests/chest_iron_open_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Chests/chest_loot_burst_32x32_6f.png` | 192x32 | 6 horizontal frames; cell 32x32 | 24576 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Chests/chest_wood_open_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/antler_shard.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/archmage_tome.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/bat_wing.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/black_powder.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/bog_mud.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/bone.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/candle_stub.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/charred_skull.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/crab_claw.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/crab_shell_plate.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/crystal_gel.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/cyclops_eye_gem.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/ectoplasm.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/ember_fang.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/frost_fur.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/frost_wing_scale.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/gargoyle_chip.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/gel_crown.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/golem_core.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/hag_charm.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/haunted_plate.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/honeycomb.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/jelly_gel.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/kraken_ink_sac.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/lily_pad.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/living_moss.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/lizard_scale.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/lizard_scale_plate.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/magma_core.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/mandible.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/mimic_tooth.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/mosquito_wing.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/old_bandage.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/owl_feather.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/puffer_spine.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/pumpkin_seed.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/shadow_fur.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/shell_plate.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/spore_dust.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/stolen_coin_pouch.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/thorn.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/trap_jaw.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/treant_heartwood.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/whelp_scale.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/whirl_sand.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Drops/yeti_tuft.png` | 16x16 | Static or combined sheet; use README layout | 1024 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/monster_drops_16x16_sheet.png` | 128x96 | Static or combined sheet; use README layout | 49152 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/coin_copper_piles_3.png` | 48x16 | Static or combined sheet; use README layout | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/coin_copper_spin_6f.png` | 96x16 | 6 horizontal frames; cell 16x16 | 6144 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/coin_gold_piles_3.png` | 48x16 | Static or combined sheet; use README layout | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/coin_gold_spin_6f.png` | 96x16 | 6 horizontal frames; cell 16x16 | 6144 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/coin_silver_piles_3.png` | 48x16 | Static or combined sheet; use README layout | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/coin_silver_spin_6f.png` | 96x16 | 6 horizontal frames; cell 16x16 | 6144 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/drop_sparkle_overlay_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/gem_amethyst_spin_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/gem_emerald_spin_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/gem_ruby_spin_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/gem_sapphire_spin_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/gem_topaz_spin_5f.png` | 80x16 | 5 horizontal frames; cell 16x16 | 5120 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/heart_big_bob_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/heart_heart_bob_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/heart_small_bob_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/orb_mana_bob_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/orb_soul_bob_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/05_Loot_Drops/Pickups/orb_xp_bob_4f.png` | 64x16 | 4 horizontal frames; cell 16x16 | 4096 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_ambush.png` | 126x28 | Static or combined sheet; use README layout | 14112 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_boss_battle.png` | 174x28 | Static or combined sheet; use README layout | 19488 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_defeated.png` | 138x28 | Static or combined sheet; use README layout | 15456 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_final_wave.png` | 162x28 | Static or combined sheet; use README layout | 18144 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_victory.png` | 138x28 | Static or combined sheet; use README layout | 15456 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_wave_1.png` | 114x28 | Static or combined sheet; use README layout | 12768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_wave_2.png` | 114x28 | Static or combined sheet; use README layout | 12768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Banners/banner_wave_3.png` | 114x28 | Static or combined sheet; use README layout | 12768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/boss_bar_200x16_100.png` | 200x16 | Static or combined sheet; use README layout | 12800 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/boss_bar_200x16_30.png` | 200x16 | Static or combined sheet; use README layout | 12800 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/boss_bar_200x16_60.png` | 200x16 | Static or combined sheet; use README layout | 12800 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/boss_bar_200x16_empty.png` | 200x16 | Static or combined sheet; use README layout | 12800 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/boss_bar_200x16_phase2_100.png` | 200x16 | Static or combined sheet; use README layout | 12800 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/boss_bar_200x16_phase2_30.png` | 200x16 | Static or combined sheet; use README layout | 12800 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/boss_bar_200x16_phase2_60.png` | 200x16 | Static or combined sheet; use README layout | 12800 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/example_full_bone_archmage.png` | 208x30 | Static or combined sheet; use README layout | 24960 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/example_full_dragon_whelp.png` | 208x30 | Static or combined sheet; use README layout | 24960 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/example_full_elder_treant.png` | 208x30 | Static or combined sheet; use README layout | 24960 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/example_full_gel_monarch.png` | 208x30 | Static or combined sheet; use README layout | 24960 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/example_full_golem_king.png` | 208x30 | Static or combined sheet; use README layout | 24960 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/example_full_tide_kraken.png` | 208x30 | Static or combined sheet; use README layout | 24960 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/name_plate_bone_archmage.png` | 95x14 | Static or combined sheet; use README layout | 5320 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/name_plate_dragon_whelp.png` | 89x14 | Static or combined sheet; use README layout | 4984 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/name_plate_elder_treant.png` | 89x14 | Static or combined sheet; use README layout | 4984 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/name_plate_gel_monarch.png` | 83x14 | Static or combined sheet; use README layout | 4648 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/name_plate_golem_king.png` | 77x14 | Static or combined sheet; use README layout | 4312 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Boss_Bar/name_plate_tide_kraken.png` | 83x14 | Static or combined sheet; use README layout | 4648 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/digits_crit_14x18.png` | 140x18 | Static or combined sheet; use README layout | 10080 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/digits_enemy_7x9.png` | 70x9 | Static or combined sheet; use README layout | 2520 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/digits_heal_7x9.png` | 70x9 | Static or combined sheet; use README layout | 2520 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/digits_normal_7x9.png` | 70x9 | Static or combined sheet; use README layout | 2520 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/digits_poison_7x9.png` | 70x9 | Static or combined sheet; use README layout | 2520 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_block.png` | 31x9 | Static or combined sheet; use README layout | 1116 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_crit.png` | 31x9 | Static or combined sheet; use README layout | 1116 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_immune.png` | 37x9 | Static or combined sheet; use README layout | 1332 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_ko.png` | 38x18 | Static or combined sheet; use README layout | 2736 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_minus.png` | 7x9 | Static or combined sheet; use README layout | 252 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_miss.png` | 25x9 | Static or combined sheet; use README layout | 900 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_plus.png` | 7x9 | Static or combined sheet; use README layout | 252 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Damage_Numbers/word_plusxp.png` | 19x9 | Static or combined sheet; use README layout | 684 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_medium_32x6_0.png` | 32x6 | Static or combined sheet; use README layout | 768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_medium_32x6_100.png` | 32x6 | Static or combined sheet; use README layout | 768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_medium_32x6_25.png` | 32x6 | Static or combined sheet; use README layout | 768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_medium_32x6_50.png` | 32x6 | Static or combined sheet; use README layout | 768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_medium_32x6_75.png` | 32x6 | Static or combined sheet; use README layout | 768 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_16x4_0.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_16x4_100.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_16x4_25.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_16x4_50.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_16x4_75.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_elite_16x4_100.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_elite_16x4_25.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_elite_16x4_50.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Health_Bars/hp_small_elite_16x4_75.png` | 16x4 | Static or combined sheet; use README layout | 256 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Loot_Popup/loot_popup_example_bone.png` | 75x24 | Static or combined sheet; use README layout | 7200 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Loot_Popup/loot_popup_example_crystal_gel.png` | 117x24 | Static or combined sheet; use README layout | 11232 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Loot_Popup/loot_popup_example_golem_core.png` | 111x24 | Static or combined sheet; use README layout | 10656 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Loot_Popup/loot_popup_frame_96x24.png` | 96x24 | Static or combined sheet; use README layout | 9216 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Markers/aggro_alert_pop_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Markers/aggro_lost_pop_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Markers/elite_danger_pop_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Markers/idle_sleep_pop_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Markers/tamed_pop_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Markers/target_ground_ring_32x16_4f.png` | 128x16 | 4 horizontal frames; cell 32x16 | 8192 |
| `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/06_Combat_UI/Markers/target_marker_pingpong_3f.png` | 48x16 | 3 horizontal frames; cell 16x16 | 3072 |

No optional hit-FX sample was added: the complete roster/import/spawn work has priority and existing combat presentation remains stable.
