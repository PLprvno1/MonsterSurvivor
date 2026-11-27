# Enemy FX Asset Mapping — Task08

Nguồn thực tế: `asset_sources/MonstersCreatures_v1.0/MonstersCreatures`. Audit lại123 PNG trong03_Projectiles_FX và39 PNG trong04_Status_Effects; xem `core/build/reports/task08/source-fx-inventory.json` (kích thước, frame/cell, SHA256). Kiểm kê này gồm các combined sheets; chúng không được import.

Runtime: **game/enemy-fx.png,512×512 RGBA (1MiB),81 strip/icon,274 frames**. Nearest, extrude2px, giữ nguyên pixel và thứ tự frame; không resample/AI. Bốn hướng của14 projectile được chọn theo hướng rồi xoay phần góc còn lại. Coin shot dùng playback ping-pong; các impact hữu hạn theo FX instance. FPS dưới đây là tuning game, nguồn không quy định FPS chính xác.

License: `assets/game/licenses/MonstersCreatures-Enemy-FX.txt`. Cho phép tích hợp/sửa trong game lớn hơn; không phân phối asset độc lập, không NFT hoặc AI training. Tool Java/Python crop/pack cục bộ. Từng SHA256 nguồn và rect runtime nằm trong `assets/game/enemy-fx.json`.

| Clip | Source (relative to MonstersCreatures) | Skill / use | Frames / cell | FPS | Atlas / type |
|---|---|---|---|---:|---|
| `arrow_right` | `03_Projectiles_FX/Projectiles/arrow_right_2f.png` | ARROW | 2 / 16×16 | 12 | enemy-fx / flight |
| `arrow_left` | `03_Projectiles_FX/Projectiles/arrow_left_2f.png` | ARROW | 2 / 16×16 | 12 | enemy-fx / flight |
| `arrow_up` | `03_Projectiles_FX/Projectiles/arrow_up_2f.png` | ARROW | 2 / 16×16 | 12 | enemy-fx / flight |
| `arrow_down` | `03_Projectiles_FX/Projectiles/arrow_down_2f.png` | ARROW | 2 / 16×16 | 12 | enemy-fx / flight |
| `arrow_impact` | `03_Projectiles_FX/Projectiles/Impacts/arrow_impact_4f.png` | ARROW | 4 / 16×16 | 16 | enemy-fx / impact |
| `rock_right` | `03_Projectiles_FX/Projectiles/rock_right_4f.png` | DUST_FAN, ROCK | 4 / 16×16 | 12 | enemy-fx / flight |
| `rock_left` | `03_Projectiles_FX/Projectiles/rock_left_4f.png` | DUST_FAN, ROCK | 4 / 16×16 | 12 | enemy-fx / flight |
| `rock_up` | `03_Projectiles_FX/Projectiles/rock_up_4f.png` | DUST_FAN, ROCK | 4 / 16×16 | 12 | enemy-fx / flight |
| `rock_down` | `03_Projectiles_FX/Projectiles/rock_down_4f.png` | DUST_FAN, ROCK | 4 / 16×16 | 12 | enemy-fx / flight |
| `rock_impact` | `03_Projectiles_FX/Projectiles/Impacts/rock_impact_4f.png` | DUST_FAN, ROCK | 4 / 16×16 | 16 | enemy-fx / impact |
| `coin_shot_right` | `03_Projectiles_FX/Projectiles/coin_shot_right_pingpong_3f.png` | COIN | 3 / 16×16 | 12 | enemy-fx / flight |
| `coin_shot_left` | `03_Projectiles_FX/Projectiles/coin_shot_left_pingpong_3f.png` | COIN | 3 / 16×16 | 12 | enemy-fx / flight |
| `coin_shot_up` | `03_Projectiles_FX/Projectiles/coin_shot_up_pingpong_3f.png` | COIN | 3 / 16×16 | 12 | enemy-fx / flight |
| `coin_shot_down` | `03_Projectiles_FX/Projectiles/coin_shot_down_pingpong_3f.png` | COIN | 3 / 16×16 | 12 | enemy-fx / flight |
| `coin_shot_impact` | `03_Projectiles_FX/Projectiles/Impacts/coin_shot_impact_4f.png` | COIN | 4 / 16×16 | 16 | enemy-fx / impact |
| `seed_right` | `03_Projectiles_FX/Projectiles/seed_right_2f.png` | SEED | 2 / 16×16 | 12 | enemy-fx / flight |
| `seed_left` | `03_Projectiles_FX/Projectiles/seed_left_2f.png` | SEED | 2 / 16×16 | 12 | enemy-fx / flight |
| `seed_up` | `03_Projectiles_FX/Projectiles/seed_up_2f.png` | SEED | 2 / 16×16 | 12 | enemy-fx / flight |
| `seed_down` | `03_Projectiles_FX/Projectiles/seed_down_2f.png` | SEED | 2 / 16×16 | 12 | enemy-fx / flight |
| `seed_impact` | `03_Projectiles_FX/Projectiles/Impacts/seed_impact_4f.png` | SEED | 4 / 16×16 | 16 | enemy-fx / impact |
| `thorn_right` | `03_Projectiles_FX/Projectiles/thorn_right_2f.png` | THORN_FAN | 2 / 16×16 | 12 | enemy-fx / flight |
| `thorn_left` | `03_Projectiles_FX/Projectiles/thorn_left_2f.png` | THORN_FAN | 2 / 16×16 | 12 | enemy-fx / flight |
| `thorn_up` | `03_Projectiles_FX/Projectiles/thorn_up_2f.png` | THORN_FAN | 2 / 16×16 | 12 | enemy-fx / flight |
| `thorn_down` | `03_Projectiles_FX/Projectiles/thorn_down_2f.png` | THORN_FAN | 2 / 16×16 | 12 | enemy-fx / flight |
| `thorn_impact` | `03_Projectiles_FX/Projectiles/Impacts/thorn_impact_4f.png` | THORN_FAN | 4 / 16×16 | 16 | enemy-fx / impact |
| `spore_right` | `03_Projectiles_FX/Projectiles/spore_right_4f.png` | SPORE, TOXIC | 4 / 16×16 | 12 | enemy-fx / flight |
| `spore_left` | `03_Projectiles_FX/Projectiles/spore_left_4f.png` | SPORE, TOXIC | 4 / 16×16 | 12 | enemy-fx / flight |
| `spore_up` | `03_Projectiles_FX/Projectiles/spore_up_4f.png` | SPORE, TOXIC | 4 / 16×16 | 12 | enemy-fx / flight |
| `spore_down` | `03_Projectiles_FX/Projectiles/spore_down_4f.png` | SPORE, TOXIC | 4 / 16×16 | 12 | enemy-fx / flight |
| `spore_impact` | `03_Projectiles_FX/Projectiles/Impacts/spore_impact_4f.png` | SPORE, TOXIC | 4 / 16×16 | 16 | enemy-fx / impact |
| `poison_spit_right` | `03_Projectiles_FX/Projectiles/poison_spit_right_4f.png` | POISON | 4 / 16×16 | 12 | enemy-fx / flight |
| `poison_spit_left` | `03_Projectiles_FX/Projectiles/poison_spit_left_4f.png` | POISON | 4 / 16×16 | 12 | enemy-fx / flight |
| `poison_spit_up` | `03_Projectiles_FX/Projectiles/poison_spit_up_4f.png` | POISON | 4 / 16×16 | 12 | enemy-fx / flight |
| `poison_spit_down` | `03_Projectiles_FX/Projectiles/poison_spit_down_4f.png` | POISON | 4 / 16×16 | 12 | enemy-fx / flight |
| `poison_spit_impact` | `03_Projectiles_FX/Projectiles/Impacts/poison_spit_impact_4f.png` | POISON | 4 / 16×16 | 16 | enemy-fx / impact |
| `fireball_right` | `03_Projectiles_FX/Projectiles/fireball_right_4f.png` | FIREBALL, FIRE_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `fireball_left` | `03_Projectiles_FX/Projectiles/fireball_left_4f.png` | FIREBALL, FIRE_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `fireball_up` | `03_Projectiles_FX/Projectiles/fireball_up_4f.png` | FIREBALL, FIRE_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `fireball_down` | `03_Projectiles_FX/Projectiles/fireball_down_4f.png` | FIREBALL, FIRE_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `fireball_impact` | `03_Projectiles_FX/Projectiles/Impacts/fireball_impact_4f.png` | FIREBALL, FIRE_BURST | 4 / 16×16 | 16 | enemy-fx / impact |
| `lava_glob_right` | `03_Projectiles_FX/Projectiles/lava_glob_right_4f.png` | LAVA | 4 / 16×16 | 12 | enemy-fx / flight |
| `lava_glob_left` | `03_Projectiles_FX/Projectiles/lava_glob_left_4f.png` | LAVA | 4 / 16×16 | 12 | enemy-fx / flight |
| `lava_glob_up` | `03_Projectiles_FX/Projectiles/lava_glob_up_4f.png` | LAVA | 4 / 16×16 | 12 | enemy-fx / flight |
| `lava_glob_down` | `03_Projectiles_FX/Projectiles/lava_glob_down_4f.png` | LAVA | 4 / 16×16 | 12 | enemy-fx / flight |
| `lava_glob_impact` | `03_Projectiles_FX/Projectiles/Impacts/lava_glob_impact_4f.png` | LAVA | 4 / 16×16 | 16 | enemy-fx / impact |
| `ice_shard_right` | `03_Projectiles_FX/Projectiles/ice_shard_right_2f.png` | FROST_FAN, ICE | 2 / 16×16 | 12 | enemy-fx / flight |
| `ice_shard_left` | `03_Projectiles_FX/Projectiles/ice_shard_left_2f.png` | FROST_FAN, ICE | 2 / 16×16 | 12 | enemy-fx / flight |
| `ice_shard_up` | `03_Projectiles_FX/Projectiles/ice_shard_up_2f.png` | FROST_FAN, ICE | 2 / 16×16 | 12 | enemy-fx / flight |
| `ice_shard_down` | `03_Projectiles_FX/Projectiles/ice_shard_down_2f.png` | FROST_FAN, ICE | 2 / 16×16 | 12 | enemy-fx / flight |
| `ice_shard_impact` | `03_Projectiles_FX/Projectiles/Impacts/ice_shard_impact_4f.png` | FROST_FAN, ICE | 4 / 16×16 | 16 | enemy-fx / impact |
| `snowball_right` | `03_Projectiles_FX/Projectiles/snowball_right_2f.png` | SNOWBALL | 2 / 16×16 | 12 | enemy-fx / flight |
| `snowball_left` | `03_Projectiles_FX/Projectiles/snowball_left_2f.png` | SNOWBALL | 2 / 16×16 | 12 | enemy-fx / flight |
| `snowball_up` | `03_Projectiles_FX/Projectiles/snowball_up_2f.png` | SNOWBALL | 2 / 16×16 | 12 | enemy-fx / flight |
| `snowball_down` | `03_Projectiles_FX/Projectiles/snowball_down_2f.png` | SNOWBALL | 2 / 16×16 | 12 | enemy-fx / flight |
| `snowball_impact` | `03_Projectiles_FX/Projectiles/Impacts/snowball_impact_4f.png` | SNOWBALL | 4 / 16×16 | 16 | enemy-fx / impact |
| `hex_bolt_right` | `03_Projectiles_FX/Projectiles/hex_bolt_right_4f.png` | MAGIC_FAN | 4 / 16×16 | 12 | enemy-fx / flight |
| `hex_bolt_left` | `03_Projectiles_FX/Projectiles/hex_bolt_left_4f.png` | MAGIC_FAN | 4 / 16×16 | 12 | enemy-fx / flight |
| `hex_bolt_up` | `03_Projectiles_FX/Projectiles/hex_bolt_up_4f.png` | MAGIC_FAN | 4 / 16×16 | 12 | enemy-fx / flight |
| `hex_bolt_down` | `03_Projectiles_FX/Projectiles/hex_bolt_down_4f.png` | MAGIC_FAN | 4 / 16×16 | 12 | enemy-fx / flight |
| `hex_bolt_impact` | `03_Projectiles_FX/Projectiles/Impacts/hex_bolt_impact_4f.png` | MAGIC_FAN | 4 / 16×16 | 16 | enemy-fx / impact |
| `shadow_orb_right` | `03_Projectiles_FX/Projectiles/shadow_orb_right_4f.png` | SHADOW, SHADOW_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `shadow_orb_left` | `03_Projectiles_FX/Projectiles/shadow_orb_left_4f.png` | SHADOW, SHADOW_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `shadow_orb_up` | `03_Projectiles_FX/Projectiles/shadow_orb_up_4f.png` | SHADOW, SHADOW_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `shadow_orb_down` | `03_Projectiles_FX/Projectiles/shadow_orb_down_4f.png` | SHADOW, SHADOW_BURST | 4 / 16×16 | 12 | enemy-fx / flight |
| `shadow_orb_impact` | `03_Projectiles_FX/Projectiles/Impacts/shadow_orb_impact_4f.png` | SHADOW, SHADOW_BURST | 4 / 16×16 | 16 | enemy-fx / impact |
| `bubble_right` | `03_Projectiles_FX/Projectiles/bubble_right_4f.png` | BUBBLE | 4 / 16×16 | 12 | enemy-fx / flight |
| `bubble_left` | `03_Projectiles_FX/Projectiles/bubble_left_4f.png` | BUBBLE | 4 / 16×16 | 12 | enemy-fx / flight |
| `bubble_up` | `03_Projectiles_FX/Projectiles/bubble_up_4f.png` | BUBBLE | 4 / 16×16 | 12 | enemy-fx / flight |
| `bubble_down` | `03_Projectiles_FX/Projectiles/bubble_down_4f.png` | BUBBLE | 4 / 16×16 | 12 | enemy-fx / flight |
| `bubble_impact` | `03_Projectiles_FX/Projectiles/Impacts/bubble_impact_4f.png` | BUBBLE | 4 / 16×16 | 16 | enemy-fx / impact |
| `bomb_impact` | `03_Projectiles_FX/Projectiles/Impacts/bomb_impact_4f.png` | TARGET_BOMB | 4 / 16×16 | 16 | enemy-fx / impact |
| `magic_orb_impact` | `03_Projectiles_FX/Projectiles/Impacts/magic_orb_impact_4f.png` | CURSE_AOE | 4 / 16×16 | 16 | enemy-fx / impact |
| `quake` | `03_Projectiles_FX/Combat_FX/ground_quake_32x32_5f.png` | SLAM | 5 / 32×32 | 12.5 | enemy-fx / impact |
| `shockwave` | `03_Projectiles_FX/Combat_FX/shockwave_64x32_6f.png` | SPIRIT_SLAM | 6 / 64×32 | 15 | enemy-fx / impact |
| `cast` | `03_Projectiles_FX/Combat_FX/hit_spark_16x16_4f.png` | Projectile muzzle/cast cue | 4 / 16×16 | 16 | enemy-fx / cast |
| `overlay_poison` | `04_Status_Effects/Overlays_32x32/overlay_poison_32x32_4f.png` | Player accepted-hit status overlay | 4 / 32×32 | 8 | enemy-fx / overlay |
| `status_poison` | `04_Status_Effects/Icons/status_poison.png` | HUD status icon | 1 / 16×16 | 1 | enemy-fx / status |
| `overlay_burn` | `04_Status_Effects/Overlays_32x32/overlay_burn_32x32_4f.png` | Player accepted-hit status overlay | 4 / 32×32 | 8 | enemy-fx / overlay |
| `status_burn` | `04_Status_Effects/Icons/status_burn.png` | HUD status icon | 1 / 16×16 | 1 | enemy-fx / status |
| `overlay_slow` | `04_Status_Effects/Overlays_32x32/overlay_slow_32x32_4f.png` | Player accepted-hit status overlay | 4 / 32×32 | 8 | enemy-fx / overlay |
| `status_slow` | `04_Status_Effects/Icons/status_slow.png` | HUD status icon | 1 / 16×16 | 1 | enemy-fx / status |

Geometry chỉ bổ sung dấu nguy hiểm: vòng tĩnh + vòng tiến độ cho AoE/slam, tia định hướng fan, viền đỏ quanh đạn hostile và vòng magenta của vùng độc. Gameplay damage không lấy từ frame animation.

Pool thường24 projectile,24 visual FX,3 poison areas; ba status slots trên Player. Không mượn BossProjectiles24 hoặc weapon/evolution projectile48. Owner chết hủy đạn/vùng độc ở lần cập nhật kế; impact không damage và tự hết hạn.

Atlas nhân vật/quái/weapon/evolution cũ giữ nguyên. GameAssets tổng21 textures /45.596.676 bytes RGBA (font không tính). Chỉ thêm1MiB; không tạo texture/animation khi cast hoặc trong vòng render.
