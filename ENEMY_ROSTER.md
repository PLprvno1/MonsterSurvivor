# Enemy roster — Task 04A

82 normal definitions: 40 MonstersCreatures base species + 27 recolor variants + 9 existing Willosuna + 2 Tiny Questers + 4 SpriteCanvas. All are registered for natural weighted spawn. New sources share one 2048x2048 atlas; old sprite assets remain unchanged. Bosses are excluded.

Tune `tools/enemy_balance.json`, then run `python tools/prepare_enemy_assets.py`. This regenerates the atlas, runtime animation catalog and the pure-Java registry data together. IDs are globally unique; `mc_` and `sc_` prefixes prevent collisions with existing Living Armor/Bone Hound. `EnemyType` is only the original three-type API adapter; enumerate `EnemyRegistry.all()` for the complete roster.

## Shared behavior and animation

- CHASER: direct chase; cached A* on obstruction; contact damage.
- FAST_MELEE: shared chase with faster configured speed and modest HP. Only the existing Wild Boar retains its charge; no new burst skill is claimed.
- TANK: higher HP, lower speed; late unlock for large/strong tanks.
- FLYING: only WATER is passable in addition to ordinary ground; trees, rocks and map boundary still block.
- NIGHT: progress gate and post-300 spawn preference; does not imply wall phasing.
- RANGED_CANDIDATE / SPECIAL_CANDIDATE: safe chase/contact now; metadata names the future skill. No ranged/projectile/special behavior is claimed implemented.

All use the same EnemySystem, real-delta animation clock, Enemy.damage/death hook, shared player immunity, collision grid, separation and two-search/frame budget. Collision dimensions are per definition, constrained to fit the 16px navigation cell; actual LOS and swept movement use them. Foot positions/scales come from the same generated source as animation metadata.

MC mapping: idle/walk loop, attack/hurt/death play once; SPECIAL is retained but not automatically played without its future behavior. Contact damage remains the existing overlap event, not a newly delayed hit at source attack frame 2. SpriteCanvas uses JSON frame-duration milliseconds; LEFT is source, RIGHT mirrored, UP/DOWN use the side view. Its uniform Nearest normalization and fixed pivot are intentional adapters.

## Base combat tuning

HP/DMG columns are base Easy/Hard before stage bonuses. Existing stage HP increments remain small: ordinary definitions use the Slime bonus, tanks/elites use the Boar bonus; legacy aliases retain their type-specific stage columns. Task 04A.1 stage tuning, strong/elite weight multipliers and late Hard damage floors live in GameConfig; the base definition tables below stay unchanged. Hard movement is x1.10. Source suggested HP/DMG values are not copied directly.

| ID | Name | Biome | Archetype | HP E/H | DMG E/H | Speed | Half footprint | Scale | Feet x,y |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| tiny_slime | Tiny Questers Slime | Tiny | CHASER | 1/1 | 1/1 | 28 | 5x3 | 1 | 32.000,48.000 |
| tiny_bat | Tiny Questers Bat | Tiny | FLYING | 1/2 | 1/1 | 52 | 5x3 | 0.5 | 32.000,48.000 |
| honey_slime | Honey Slime | Willosuna | CHASER | 1/2 | 1/1 | 26 | 5x3 | 1 | 16.000,28.000 |
| wild_boar | Wild Boar | Willosuna | FAST_MELEE | 2/3 | 1/1 | 38 | 5x3 | 1 | 16.000,28.000 |
| giant_bee | Giant Bee | Willosuna | FLYING | 1/2 | 1/1 | 46 | 5x3 | 1 | 16.000,28.000 |
| red_cap | Red Cap | Willosuna | CHASER | 2/3 | 1/1 | 28 | 5x3 | 1 | 16.000,28.000 |
| red_crab | Red Crab | Willosuna | TANK | 4/5 | 1/1 | 22 | 5x3 | 1 | 16.000,28.000 |
| giant_frog | Giant Frog | Willosuna | SPECIAL_CANDIDATE | 3/4 | 1/1 | 32 | 5x3 | 1 | 16.000,28.000 |
| rock_golem | Rock Golem | Willosuna | TANK | 6/8 | 1/1 | 18 | 5x3 | 1 | 16.000,28.000 |
| living_armor | Living Armor | Willosuna | TANK | 5/7 | 1/1 | 23 | 5x3 | 1 | 16.000,28.000 |
| bone_hound | Bone Hound | Willosuna | FAST_MELEE | 3/4 | 1/1 | 48 | 5x3 | 1 | 16.000,28.000 |
| mc_bat_swarm | Bat Swarm | Cave | FLYING | 1/2 | 1/1 | 45 | 5x3 | 1 | 8.000,14.000 |
| mc_bone_hound | Bone Hound | Cave | FAST_MELEE | 3/4 | 1/1 | 42 | 6x4 | 1 | 16.000,30.000 |
| mc_crystal_cube | Crystal Cube | Cave | TANK | 3/4 | 1/1 | 22 | 5x3 | 1 | 8.000,14.000 |
| mc_crystal_cube_ember | Crystal Cube / ember | Cave | TANK | 4/6 | 1/1 | 22 | 5x3 | 1 | 8.000,14.000 |
| mc_crystal_cube_toxic | Crystal Cube / toxic | Cave | TANK | 4/6 | 1/1 | 22 | 5x3 | 1 | 8.000,14.000 |
| mc_gargoyle | Gargoyle | Cave | FLYING | 3/4 | 1/1 | 35 | 5x3 | 1 | 8.000,14.000 |
| mc_kobold_miner | Kobold Miner | Cave | CHASER | 2/3 | 1/1 | 30 | 5x3 | 1 | 8.000,14.000 |
| mc_kobold_miner_blue | Kobold Miner / blue | Cave | CHASER | 3/5 | 1/1 | 30 | 5x3 | 1 | 8.000,14.000 |
| mc_living_armor | Living Armor | Cave | TANK | 5/7 | 1/2 | 23 | 5x3 | 0.85 | 8.000,30.000 |
| mc_living_armor_dark | Living Armor / dark | Cave | TANK | 6/9 | 1/2 | 23 | 5x3 | 0.85 | 8.000,30.000 |
| mc_living_armor_gold | Living Armor / gold | Cave | TANK | 6/9 | 1/2 | 23 | 5x3 | 0.85 | 8.000,30.000 |
| mc_mimic_jar | Mimic Jar | Cave | RANGED_CANDIDATE | 3/4 | 1/1 | 27 | 5x3 | 1 | 8.000,14.000 |
| mc_mimic_jar_gold | Mimic Jar / gold | Cave | RANGED_CANDIDATE | 4/6 | 1/1 | 27 | 5x3 | 1 | 8.000,14.000 |
| mc_rock_crab | Rock Crab | Cave | TANK | 4/5 | 1/1 | 20 | 5x3 | 1 | 8.000,14.000 |
| mc_rock_crab_crystal | Rock Crab / crystal | Cave | TANK | 5/7 | 1/1 | 20 | 5x3 | 1 | 8.000,14.000 |
| mc_roller_bug | Roller Bug | Cave | CHASER | 2/3 | 1/1 | 32 | 5x3 | 1 | 8.000,14.000 |
| mc_roller_bug_lava | Roller Bug / lava | Cave | CHASER | 3/5 | 1/1 | 32 | 5x3 | 1 | 8.000,14.000 |
| mc_crab_knight | Crab Knight | Coast | TANK | 4/5 | 1/1 | 26 | 5x3 | 0.85 | 8.000,30.000 |
| mc_crab_knight_royal | Crab Knight / royal | Coast | TANK | 5/7 | 1/1 | 26 | 5x3 | 0.85 | 8.000,30.000 |
| mc_jelly | Jelly | Coast | FLYING | 2/3 | 1/1 | 30 | 5x3 | 1 | 8.000,14.000 |
| mc_jelly_sea_glass | Jelly / sea_glass | Coast | FLYING | 3/5 | 1/1 | 30 | 5x3 | 1 | 8.000,14.000 |
| mc_pufferfish | Pufferfish | Coast | CHASER | 2/3 | 1/1 | 28 | 5x3 | 1 | 8.000,14.000 |
| mc_antlion | Antlion | Desert | CHASER | 3/4 | 1/1 | 25 | 5x3 | 1 | 8.000,14.000 |
| mc_dust_devil | Dust Devil | Desert | FLYING | 2/3 | 1/1 | 40 | 5x3 | 0.85 | 8.000,30.000 |
| mc_dust_devil_ash | Dust Devil / ash | Desert | FLYING | 3/5 | 1/1 | 40 | 5x3 | 0.85 | 8.000,30.000 |
| mc_dust_devil_snow | Dust Devil / snow | Desert | FLYING | 3/5 | 1/1 | 40 | 5x3 | 0.85 | 8.000,30.000 |
| mc_frill_lizard | Frill Lizard | Desert | FAST_MELEE | 2/3 | 1/1 | 42 | 5x3 | 1 | 8.000,14.000 |
| mc_mummy_cat | Mummy Cat | Desert | FAST_MELEE | 2/3 | 1/1 | 42 | 5x3 | 1 | 8.000,14.000 |
| mc_mummy_cat_shadow | Mummy Cat / shadow | Desert | FAST_MELEE | 3/5 | 1/1 | 42 | 5x3 | 1 | 8.000,14.000 |
| mc_flytrap_walker | Flytrap Walker | Forest | RANGED_CANDIDATE | 3/4 | 1/1 | 23 | 5x3 | 0.85 | 8.000,30.000 |
| mc_flytrap_walker_fire | Flytrap Walker / fire | Forest | RANGED_CANDIDATE | 4/6 | 1/1 | 23 | 5x3 | 0.85 | 8.000,30.000 |
| mc_jackalope | Jackalope | Forest | FAST_MELEE | 1/2 | 1/1 | 40 | 5x3 | 1 | 8.000,14.000 |
| mc_jackalope_snow | Jackalope / snow | Forest | FAST_MELEE | 2/4 | 1/1 | 40 | 5x3 | 1 | 8.000,14.000 |
| mc_moss_golem | Moss Golem | Forest | TANK | 6/8 | 1/2 | 18 | 6x4 | 1 | 16.000,30.000 |
| mc_moss_golem_autumn | Moss Golem / autumn | Forest | TANK | 7/10 | 1/2 | 18 | 6x4 | 1 | 16.000,30.000 |
| mc_mushroom_folk | Mushroom Folk | Forest | CHASER | 1/2 | 1/1 | 28 | 5x3 | 0.85 | 8.000,30.000 |
| mc_mushroom_folk_toxic | Mushroom Folk / toxic | Forest | CHASER | 2/4 | 1/1 | 28 | 5x3 | 0.85 | 8.000,30.000 |
| mc_owlbear_cub | Owlbear Cub | Forest | CHASER | 2/3 | 1/1 | 28 | 5x3 | 1 | 8.000,14.000 |
| mc_thorn_sprite | Thorn Sprite | Forest | FLYING | 1/1 | 1/1 | 40 | 5x3 | 1 | 8.000,14.000 |
| mc_pumpkin_head | Pumpkin Head | Night | NIGHT | 4/5 | 1/1 | 28 | 5x3 | 0.85 | 8.000,30.000 |
| mc_pumpkin_head_winter | Pumpkin Head / winter | Night | NIGHT | 5/7 | 1/1 | 28 | 5x3 | 0.85 | 8.000,30.000 |
| mc_shadow_cat | Shadow Cat | Night | NIGHT | 2/3 | 1/1 | 45 | 5x3 | 1 | 8.000,14.000 |
| mc_wraith | Wraith | Night | NIGHT | 3/4 | 1/1 | 34 | 5x3 | 0.85 | 8.000,30.000 |
| mc_wraith_ember | Wraith / ember | Night | NIGHT | 4/6 | 1/1 | 34 | 5x3 | 0.85 | 8.000,30.000 |
| mc_bumble_guard | Bumble Guard | Plains | FLYING | 1/2 | 1/1 | 42 | 5x3 | 1 | 8.000,14.000 |
| mc_cyclops | Cyclops | Plains | TANK | 6/8 | 1/2 | 18 | 5x3 | 0.85 | 8.000,30.000 |
| mc_goblin_bomber | Goblin Bomber | Plains | RANGED_CANDIDATE | 2/3 | 1/1 | 30 | 5x3 | 1 | 8.000,14.000 |
| mc_ratfolk_thief | Ratfolk Thief | Plains | FAST_MELEE | 2/3 | 1/1 | 42 | 5x3 | 1 | 8.000,14.000 |
| mc_frost_fox | Frost Fox | Snow | FAST_MELEE | 3/4 | 1/1 | 44 | 5x3 | 1 | 8.000,14.000 |
| mc_frost_fox_arctic_night | Frost Fox / arctic_night | Snow | FAST_MELEE | 4/6 | 1/1 | 44 | 5x3 | 1 | 8.000,14.000 |
| mc_frost_moth | Frost Moth | Snow | FLYING | 2/3 | 1/1 | 36 | 5x3 | 1 | 8.000,14.000 |
| mc_frost_moth_luna | Frost Moth / luna | Snow | FLYING | 3/5 | 1/1 | 36 | 5x3 | 1 | 8.000,14.000 |
| mc_yeti_cub | Yeti Cub | Snow | RANGED_CANDIDATE | 3/4 | 1/1 | 28 | 5x3 | 1 | 8.000,14.000 |
| mc_bog_lurker | Bog Lurker | Swamp | CHASER | 3/4 | 1/1 | 24 | 5x3 | 1 | 8.000,14.000 |
| mc_giant_mosquito | Giant Mosquito | Swamp | FLYING | 1/2 | 1/1 | 45 | 5x3 | 1 | 8.000,14.000 |
| mc_giant_mosquito_plague | Giant Mosquito / plague | Swamp | FLYING | 2/4 | 1/1 | 45 | 5x3 | 1 | 8.000,14.000 |
| mc_lily_snapper | Lily Snapper | Swamp | TANK | 4/5 | 1/1 | 20 | 5x3 | 1 | 8.000,14.000 |
| mc_lily_snapper_desert | Lily Snapper / desert | Swamp | TANK | 5/7 | 1/1 | 20 | 5x3 | 1 | 8.000,14.000 |
| mc_lizardfolk | Lizardfolk | Swamp | CHASER | 3/4 | 1/1 | 30 | 5x3 | 0.85 | 8.000,30.000 |
| mc_lizardfolk_desert | Lizardfolk / desert | Swamp | CHASER | 4/6 | 1/1 | 30 | 5x3 | 0.85 | 8.000,30.000 |
| mc_swamp_hag | Swamp Hag | Swamp | RANGED_CANDIDATE | 4/5 | 1/1 | 23 | 5x3 | 0.85 | 8.000,30.000 |
| mc_swamp_hag_frost | Swamp Hag / frost | Swamp | RANGED_CANDIDATE | 5/7 | 1/1 | 23 | 5x3 | 0.85 | 8.000,30.000 |
| mc_ash_hound | Ash Hound | Volcanic | FAST_MELEE | 5/6 | 1/1 | 44 | 6x4 | 1 | 16.000,30.000 |
| mc_ash_hound_frost | Ash Hound / frost | Volcanic | FAST_MELEE | 6/8 | 1/1 | 44 | 6x4 | 1 | 16.000,30.000 |
| mc_flame_skull | Flame Skull | Volcanic | FLYING | 3/4 | 1/1 | 42 | 5x3 | 1 | 8.000,14.000 |
| mc_flame_skull_soul | Flame Skull / soul | Volcanic | FLYING | 4/6 | 1/1 | 42 | 5x3 | 1 | 8.000,14.000 |
| mc_magma_slug | Magma Slug | Volcanic | TANK | 5/7 | 1/2 | 20 | 5x3 | 1 | 8.000,14.000 |
| sc_goblin | SpriteCanvas Goblin | CC0 | FAST_MELEE | 2/3 | 1/1 | 34 | 5x3 | 0.75 | 15.893,27.490 |
| sc_golem | SpriteCanvas Golem | CC0 | TANK | 6/8 | 1/1 | 18 | 6x4 | 0.75 | 23.761,43.216 |
| sc_mushroom | SpriteCanvas Mushroom | CC0 | CHASER | 2/3 | 1/1 | 28 | 5x3 | 0.75 | 14.769,20.706 |
| sc_slime | SpriteCanvas Slime | CC0 | CHASER | 1/2 | 1/1 | 28 | 5x3 | 0.75 | 18.697,27.506 |

## Spawn tuning

Weights are relative, not percentages. Each match gets seeded x0.80–1.25 weight variation. Hard/night factors and GameConfig stage strong/elite multipliers multiply the base. Per-species resident limits count corpses, and every family (base plus recolors) is limited to ceil(25% of the existing 40/60 cap). Repeated picks are damped after two. Short seeded 18-progress-second biome themes after 300 temporarily multiply weight x1.65; they never bypass unlocks or add spawn bursts.

0–120: seven initial weak definitions, then light Forest/Plains/Goblin. 120–240: more Cave/Desert. 240–300: Coast/Swamp. 300–420: Night preference plus Snow. 420–540: tanks/Volcanic and rare recolors. 540–600: unlocked roster continues; `lich_lord` at 540 is a future boss contract only. Natural spawn stops at 600 without declaring Victory; living actors remain.

Spawn cadence/density and resident limits Easy 40 / Hard 60 are unchanged. Placement uses 64 random attempts plus at most 256 local fallback cells around the current camera; it never searches the entire 200x200 map per enemy or falls back to remote map corners. Normal/Demo share progress; movement/animation/cooldowns use real gameplay delta.

| ID | Unlock progress s | Base weight | Hard factor | Night factor | Type resident max | Family | Future skill metadata |
| --- | --- | --- | --- | --- | --- | --- | --- |
| tiny_slime | 0 | 3.000 | 1 | 1 | 10 | tiny_slime | none |
| tiny_bat | 0 | 2.500 | 1 | 1 | 10 | tiny_bat | none |
| honey_slime | 0 | 1.400 | 1 | 1 | 10 | honey_slime | none |
| wild_boar | 0 | 2.000 | 1 | 1 | 10 | wild_boar | legacy_charge |
| giant_bee | 90 | 0.900 | 1 | 1 | 10 | giant_bee | none |
| red_cap | 120 | 0.800 | 1 | 1 | 10 | red_cap | none |
| red_crab | 240 | 0.600 | 1.15 | 1 | 10 | red_crab | none |
| giant_frog | 240 | 0.700 | 1 | 1 | 10 | giant_frog | none |
| rock_golem | 420 | 0.500 | 1.15 | 1 | 10 | rock_golem | none |
| living_armor | 420 | 0.500 | 1.15 | 1 | 10 | living_armor | none |
| bone_hound | 240 | 0.700 | 1 | 1 | 10 | bone_hound | none |
| mc_bat_swarm | 120 | 0.900 | 1.2 | 1 | 8 | mc_bat_swarm | scatter |
| mc_bone_hound | 240 | 0.700 | 1.2 | 1 | 8 | mc_bone_hound | reassemble |
| mc_crystal_cube | 120 | 0.800 | 1.2 | 1 | 4 | mc_crystal_cube | split |
| mc_crystal_cube_ember | 420 | 0.144 | 1.2 | 1 | 2 | mc_crystal_cube | split |
| mc_crystal_cube_toxic | 420 | 0.144 | 1.2 | 1 | 2 | mc_crystal_cube | split |
| mc_gargoyle | 180 | 0.600 | 1 | 1 | 8 | mc_gargoyle | stone_skin |
| mc_kobold_miner | 120 | 0.900 | 1 | 1 | 8 | mc_kobold_miner | dig |
| mc_kobold_miner_blue | 420 | 0.162 | 1 | 1 | 2 | mc_kobold_miner | dig |
| mc_living_armor | 420 | 0.500 | 1.2 | 1 | 4 | mc_living_armor | spirit_slash |
| mc_living_armor_dark | 480 | 0.090 | 1.2 | 1 | 2 | mc_living_armor | spirit_slash |
| mc_living_armor_gold | 480 | 0.090 | 1.2 | 1 | 2 | mc_living_armor | spirit_slash |
| mc_mimic_jar | 150 | 0.700 | 1 | 1 | 8 | mc_mimic_jar | coin_spit |
| mc_mimic_jar_gold | 420 | 0.126 | 1 | 1 | 2 | mc_mimic_jar | coin_spit |
| mc_rock_crab | 180 | 0.600 | 1.2 | 1 | 4 | mc_rock_crab | shell_hide |
| mc_rock_crab_crystal | 420 | 0.108 | 1.2 | 1 | 2 | mc_rock_crab | shell_hide |
| mc_roller_bug | 150 | 0.800 | 1 | 1 | 8 | mc_roller_bug | roll |
| mc_roller_bug_lava | 420 | 0.144 | 1 | 1 | 2 | mc_roller_bug | roll |
| mc_crab_knight | 285 | 0.600 | 1.2 | 1 | 4 | mc_crab_knight | shell_guard |
| mc_crab_knight_royal | 420 | 0.108 | 1.2 | 1 | 2 | mc_crab_knight | shell_guard |
| mc_jelly | 240 | 0.700 | 1 | 1 | 8 | mc_jelly | shock_pulse |
| mc_jelly_sea_glass | 420 | 0.126 | 1 | 1 | 2 | mc_jelly | shock_pulse |
| mc_pufferfish | 250 | 0.700 | 1 | 1 | 8 | mc_pufferfish | puff_up |
| mc_antlion | 240 | 0.700 | 1 | 1 | 8 | mc_antlion | burrow |
| mc_dust_devil | 240 | 0.600 | 1 | 1 | 8 | mc_dust_devil | sand_blast |
| mc_dust_devil_ash | 420 | 0.108 | 1 | 1 | 2 | mc_dust_devil | sand_blast |
| mc_dust_devil_snow | 420 | 0.108 | 1 | 1 | 2 | mc_dust_devil | sand_blast |
| mc_frill_lizard | 210 | 0.800 | 1.2 | 1 | 8 | mc_frill_lizard | frill_display |
| mc_mummy_cat | 180 | 0.800 | 1.2 | 1 | 8 | mc_mummy_cat | curse |
| mc_mummy_cat_shadow | 420 | 0.144 | 1.2 | 1 | 2 | mc_mummy_cat | curse |
| mc_flytrap_walker | 150 | 0.700 | 1 | 1 | 8 | mc_flytrap_walker | seed_spit |
| mc_flytrap_walker_fire | 420 | 0.126 | 1 | 1 | 2 | mc_flytrap_walker | seed_spit |
| mc_jackalope | 60 | 1.000 | 1.2 | 1 | 8 | mc_jackalope | antler_charge |
| mc_jackalope_snow | 420 | 0.180 | 1.2 | 1 | 2 | mc_jackalope | antler_charge |
| mc_moss_golem | 420 | 0.500 | 1.2 | 1 | 4 | mc_moss_golem | root_slam |
| mc_moss_golem_autumn | 480 | 0.090 | 1.2 | 1 | 2 | mc_moss_golem | root_slam |
| mc_mushroom_folk | 0 | 1.200 | 1 | 1 | 8 | mc_mushroom_folk | spore_cloud |
| mc_mushroom_folk_toxic | 420 | 0.216 | 1 | 1 | 2 | mc_mushroom_folk | spore_cloud |
| mc_owlbear_cub | 90 | 0.800 | 1 | 1 | 8 | mc_owlbear_cub | screech |
| mc_thorn_sprite | 40 | 1.000 | 1.2 | 1 | 8 | mc_thorn_sprite | thorn_volley |
| mc_pumpkin_head | 300 | 0.800 | 1 | 3 | 8 | mc_pumpkin_head | flame_spit |
| mc_pumpkin_head_winter | 420 | 0.144 | 1 | 3 | 2 | mc_pumpkin_head | flame_spit |
| mc_shadow_cat | 300 | 0.800 | 1.2 | 3 | 8 | mc_shadow_cat | shadow_blink |
| mc_wraith | 330 | 0.700 | 1 | 3 | 8 | mc_wraith | soul_lantern |
| mc_wraith_ember | 420 | 0.126 | 1 | 3 | 2 | mc_wraith | soul_lantern |
| mc_bumble_guard | 60 | 0.900 | 1 | 1 | 8 | mc_bumble_guard | pollen_cloud |
| mc_cyclops | 450 | 0.400 | 1.2 | 1 | 4 | mc_cyclops | ground_pound |
| mc_goblin_bomber | 150 | 0.700 | 1 | 1 | 8 | mc_goblin_bomber | big_bomb |
| mc_ratfolk_thief | 110 | 0.900 | 1.2 | 1 | 8 | mc_ratfolk_thief | smoke_bomb |
| mc_frost_fox | 300 | 0.700 | 1.2 | 1 | 8 | mc_frost_fox | ice_breath |
| mc_frost_fox_arctic_night | 420 | 0.126 | 1.2 | 1 | 2 | mc_frost_fox | ice_breath |
| mc_frost_moth | 360 | 0.600 | 1 | 1 | 8 | mc_frost_moth | frost_dust |
| mc_frost_moth_luna | 420 | 0.108 | 1 | 1 | 2 | mc_frost_moth | frost_dust |
| mc_yeti_cub | 330 | 0.700 | 1 | 1 | 8 | mc_yeti_cub | snowball |
| mc_bog_lurker | 240 | 0.800 | 1 | 1 | 8 | mc_bog_lurker | submerge |
| mc_giant_mosquito | 240 | 0.700 | 1.2 | 1 | 8 | mc_giant_mosquito | drain |
| mc_giant_mosquito_plague | 420 | 0.126 | 1.2 | 1 | 2 | mc_giant_mosquito | drain |
| mc_lily_snapper | 270 | 0.600 | 1.2 | 1 | 4 | mc_lily_snapper | bubble_spit |
| mc_lily_snapper_desert | 420 | 0.108 | 1.2 | 1 | 2 | mc_lily_snapper | bubble_spit |
| mc_lizardfolk | 260 | 0.800 | 1 | 1 | 8 | mc_lizardfolk | tail_whip |
| mc_lizardfolk_desert | 420 | 0.144 | 1 | 1 | 2 | mc_lizardfolk | tail_whip |
| mc_swamp_hag | 285 | 0.600 | 1 | 1 | 8 | mc_swamp_hag | hex_bolt |
| mc_swamp_hag_frost | 420 | 0.108 | 1 | 1 | 2 | mc_swamp_hag | hex_bolt |
| mc_ash_hound | 480 | 0.400 | 1.2 | 1 | 8 | mc_ash_hound | flame_breath |
| mc_ash_hound_frost | 540 | 0.072 | 1.2 | 1 | 2 | mc_ash_hound | flame_breath |
| mc_flame_skull | 450 | 0.600 | 1 | 1 | 8 | mc_flame_skull | fire_burst |
| mc_flame_skull_soul | 510 | 0.108 | 1 | 1 | 2 | mc_flame_skull | fire_burst |
| mc_magma_slug | 420 | 0.600 | 1.2 | 1 | 4 | mc_magma_slug | lava_trail |
| sc_goblin | 60 | 0.800 | 1 | 1 | 8 | sc_goblin | none |
| sc_golem | 420 | 0.500 | 1 | 1 | 4 | sc_golem | none |
| sc_mushroom | 0 | 0.800 | 1 | 1 | 8 | sc_mushroom | none |
| sc_slime | 0 | 0.800 | 1 | 1 | 8 | sc_slime | none |

## Functional coverage

Per-definition evidence: `core/build/reports/task04a/per-type-checks.json`. Every ID is actually spawned and killed by Sword, Bow, Fireball and Lightning in both Normal and Demo fixtures, with exactly one kill and a forced-drop diagnostic. All also chase, display Walk/Hurt/Attack/Death, damage the player on contact and finish actions. Separate tree/water fixtures exercise actual footprint movement and bounded A*. GameScreen/OpenGL tests cover every ID and three Retry cycles; a catalog/preview alone is not counted as gameplay integration.

Weighted sampling is probabilistic: all 82 have a positive eligible weight before 600, but one ordinary match is not guaranteed to show every rare variant. Bosses, future ranged skills and future specials are deliberately not part of this completion claim.
