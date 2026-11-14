# Enemy asset inventory — Task 04A

Inspected actual source PNG headers, README/license and SpriteCanvas JSON. Pixel decoding/strip comparison results are in `core/build/reports/task04a/asset-validation.json`. No N3XUS artwork processed.

MonstersCreatures: 40 base species + 27 elite recolors, all included. Six bosses are metadata-only. Existing Tiny/Willosuna: 11 regular species retained/enabled. SpriteCanvas: four ADAPTABLE species. Total regular definitions: 82.

MC source layout: 6 columns / 24 rows. IDLE 4f @8 fps loop; WALK 4f @10 fps loop; ATTACK 5f @12 fps once; HURT 3f @12 fps once; DEATH 6f @10 fps once; SPECIAL 5f @12 fps once. Directions DOWN/UP/RIGHT/LEFT. README suggests idle/walk 8–10 and attack 12; remaining playback rates are explicit importer policy, not claimed source metadata. Source attack hit frame 2 is documented for future skills, not wired as a runtime damage event; contact damage behavior is documented separately.

MC frames remain native pixels. LEFT uses a verified mirrored RIGHT only where every source pixel agrees. Feet = (W/2,H-2), fixed for all clips. Recolors use the base layout and are validated individually. SPECIAL clips are imported for later behavior; no corresponding skill is claimed complete.

| ID | Biome | Status | Source | Cell/runtime canvas | Feet (top-left) | Special / adaptation |
| --- | --- | --- | --- | --- | --- | --- |
| mc_bat_swarm | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/bat_swarm_16x16_sheet.png` | 16x16 | [8.0, 14] | scatter  |
| mc_bone_hound | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/bone_hound_32x32_sheet.png` | 32x32 | [16.0, 30] | reassemble  |
| mc_crystal_cube | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/crystal_cube_16x16_sheet.png` | 16x16 | [8.0, 14] | split  |
| mc_crystal_cube_ember | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/recolors/crystal_cube_ember_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | split  |
| mc_crystal_cube_toxic | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/recolors/crystal_cube_toxic_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | split  |
| mc_gargoyle | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/gargoyle_16x16_sheet.png` | 16x16 | [8.0, 14] | stone_skin  |
| mc_kobold_miner | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/kobold_miner_16x16_sheet.png` | 16x16 | [8.0, 14] | dig  |
| mc_kobold_miner_blue | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/recolors/kobold_miner_blue_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | dig  |
| mc_living_armor | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/living_armor_16x32_sheet.png` | 16x32 | [8.0, 30] | spirit_slash  |
| mc_living_armor_dark | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/recolors/living_armor_dark_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | spirit_slash  |
| mc_living_armor_gold | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/recolors/living_armor_gold_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | spirit_slash  |
| mc_mimic_jar | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/mimic_jar_16x16_sheet.png` | 16x16 | [8.0, 14] | coin_spit  |
| mc_mimic_jar_gold | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/recolors/mimic_jar_gold_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | coin_spit  |
| mc_rock_crab | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/rock_crab_16x16_sheet.png` | 16x16 | [8.0, 14] | shell_hide  |
| mc_rock_crab_crystal | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/recolors/rock_crab_crystal_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | shell_hide  |
| mc_roller_bug | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/roller_bug_16x16_sheet.png` | 16x16 | [8.0, 14] | roll  |
| mc_roller_bug_lava | Cave | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/recolors/roller_bug_lava_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | roll  |
| mc_crab_knight | Coast | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/crab_knight_16x32_sheet.png` | 16x32 | [8.0, 30] | shell_guard  |
| mc_crab_knight_royal | Coast | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/recolors/crab_knight_royal_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | shell_guard  |
| mc_jelly | Coast | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/jelly_16x16_sheet.png` | 16x16 | [8.0, 14] | shock_pulse  |
| mc_jelly_sea_glass | Coast | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/recolors/jelly_sea_glass_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | shock_pulse  |
| mc_pufferfish | Coast | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/pufferfish_16x16_sheet.png` | 16x16 | [8.0, 14] | puff_up  |
| mc_antlion | Desert | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/antlion_16x16_sheet.png` | 16x16 | [8.0, 14] | burrow  |
| mc_dust_devil | Desert | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/dust_devil_16x32_sheet.png` | 16x32 | [8.0, 30] | sand_blast  |
| mc_dust_devil_ash | Desert | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/recolors/dust_devil_ash_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | sand_blast  |
| mc_dust_devil_snow | Desert | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/recolors/dust_devil_snow_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | sand_blast  |
| mc_frill_lizard | Desert | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/frill_lizard_16x16_sheet.png` | 16x16 | [8.0, 14] | frill_display  |
| mc_mummy_cat | Desert | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/mummy_cat_16x16_sheet.png` | 16x16 | [8.0, 14] | curse  |
| mc_mummy_cat_shadow | Desert | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/recolors/mummy_cat_shadow_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | curse  |
| mc_flytrap_walker | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/flytrap_walker_16x32_sheet.png` | 16x32 | [8.0, 30] | seed_spit  |
| mc_flytrap_walker_fire | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/recolors/flytrap_walker_fire_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | seed_spit  |
| mc_jackalope | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/jackalope_16x16_sheet.png` | 16x16 | [8.0, 14] | antler_charge  |
| mc_jackalope_snow | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/recolors/jackalope_snow_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | antler_charge  |
| mc_moss_golem | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/moss_golem_32x32_sheet.png` | 32x32 | [16.0, 30] | root_slam  |
| mc_moss_golem_autumn | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/recolors/moss_golem_autumn_recolor_32x32_sheet.png` | 32x32 | [16.0, 30] | root_slam  |
| mc_mushroom_folk | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/mushroom_folk_16x32_sheet.png` | 16x32 | [8.0, 30] | spore_cloud  |
| mc_mushroom_folk_toxic | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/recolors/mushroom_folk_toxic_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | spore_cloud  |
| mc_owlbear_cub | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/owlbear_cub_16x16_sheet.png` | 16x16 | [8.0, 14] | screech  |
| mc_thorn_sprite | Forest | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/thorn_sprite_16x16_sheet.png` | 16x16 | [8.0, 14] | thorn_volley  |
| mc_pumpkin_head | Night | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/pumpkin_head_16x32_sheet.png` | 16x32 | [8.0, 30] | flame_spit  |
| mc_pumpkin_head_winter | Night | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/recolors/pumpkin_head_winter_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | flame_spit  |
| mc_shadow_cat | Night | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/shadow_cat_16x16_sheet.png` | 16x16 | [8.0, 14] | shadow_blink  |
| mc_wraith | Night | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/wraith_16x32_sheet.png` | 16x32 | [8.0, 30] | soul_lantern  |
| mc_wraith_ember | Night | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/recolors/wraith_ember_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | soul_lantern  |
| mc_bumble_guard | Plains | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/bumble_guard_16x16_sheet.png` | 16x16 | [8.0, 14] | pollen_cloud  |
| mc_cyclops | Plains | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/cyclops_16x32_sheet.png` | 16x32 | [8.0, 30] | ground_pound  |
| mc_goblin_bomber | Plains | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/goblin_bomber_16x16_sheet.png` | 16x16 | [8.0, 14] | big_bomb  |
| mc_ratfolk_thief | Plains | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/ratfolk_thief_16x16_sheet.png` | 16x16 | [8.0, 14] | smoke_bomb  |
| mc_frost_fox | Snow | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/frost_fox_16x16_sheet.png` | 16x16 | [8.0, 14] | ice_breath  |
| mc_frost_fox_arctic_night | Snow | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/recolors/frost_fox_arctic_night_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | ice_breath  |
| mc_frost_moth | Snow | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/frost_moth_16x16_sheet.png` | 16x16 | [8.0, 14] | frost_dust  |
| mc_frost_moth_luna | Snow | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/recolors/frost_moth_luna_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | frost_dust  |
| mc_yeti_cub | Snow | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/yeti_cub_16x16_sheet.png` | 16x16 | [8.0, 14] | snowball  |
| mc_bog_lurker | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/bog_lurker_16x16_sheet.png` | 16x16 | [8.0, 14] | submerge  |
| mc_giant_mosquito | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/giant_mosquito_16x16_sheet.png` | 16x16 | [8.0, 14] | drain  |
| mc_giant_mosquito_plague | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/recolors/giant_mosquito_plague_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | drain  |
| mc_lily_snapper | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/lily_snapper_16x16_sheet.png` | 16x16 | [8.0, 14] | bubble_spit  |
| mc_lily_snapper_desert | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/recolors/lily_snapper_desert_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | bubble_spit  |
| mc_lizardfolk | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/lizardfolk_16x32_sheet.png` | 16x32 | [8.0, 30] | tail_whip  |
| mc_lizardfolk_desert | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/recolors/lizardfolk_desert_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | tail_whip  |
| mc_swamp_hag | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/swamp_hag_16x32_sheet.png` | 16x32 | [8.0, 30] | hex_bolt  |
| mc_swamp_hag_frost | Swamp | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/recolors/swamp_hag_frost_recolor_16x32_sheet.png` | 16x32 | [8.0, 30] | hex_bolt  |
| mc_ash_hound | Volcanic | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/ash_hound_32x32_sheet.png` | 32x32 | [16.0, 30] | flame_breath  |
| mc_ash_hound_frost | Volcanic | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/recolors/ash_hound_frost_recolor_32x32_sheet.png` | 32x32 | [16.0, 30] | flame_breath  |
| mc_flame_skull | Volcanic | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/flame_skull_16x16_sheet.png` | 16x16 | [8.0, 14] | fire_burst  |
| mc_flame_skull_soul | Volcanic | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/recolors/flame_skull_soul_recolor_16x16_sheet.png` | 16x16 | [8.0, 14] | fire_burst  |
| mc_magma_slug | Volcanic | READY | `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/magma_slug_16x16_sheet.png` | 16x16 | [8.0, 14] | lava_trail  |
| sc_goblin | CC0 | ADAPTABLE | `asset_sources/spritecanvas-monsters-cc0/monster-goblin` | 32x32 | [15.89261744966443, 27.48993288590604] | none Nearest normalization; LEFT original, RIGHT mirrored; UP/DOWN reuse LEFT side view. |
| sc_golem | CC0 | ADAPTABLE | `asset_sources/spritecanvas-monsters-cc0/monster-golem` | 46x48 | [23.76079734219269, 43.21594684385382] | none Nearest normalization; LEFT original, RIGHT mirrored; UP/DOWN reuse LEFT side view. |
| sc_mushroom | CC0 | ADAPTABLE | `asset_sources/spritecanvas-monsters-cc0/monster-mushroom` | 32x23 | [14.76923076923077, 20.705882352941178] | none Nearest normalization; LEFT original, RIGHT mirrored; UP/DOWN reuse LEFT side view. |
| sc_slime | CC0 | ADAPTABLE | `asset_sources/spritecanvas-monsters-cc0/monster-slime` | 32x30 | [18.696629213483146, 27.50561797752809] | none Nearest normalization; LEFT original, RIGHT mirrored; UP/DOWN reuse LEFT side view. |
| tiny_slime | Tiny | READY | `game/tiny/tiny_slime.png` | 64x64 | [32, 48] | none Existing runtime pixels/catalog retained; enabled in registry. |
| tiny_bat | Tiny | READY | `game/tiny/tiny_bat.png` | 64x64 | [32, 48] | none Existing runtime pixels/catalog retained; enabled in registry. |
| honey_slime | Willosuna | READY | `game/monsters/honey_slime.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |
| wild_boar | Willosuna | READY | `game/monsters/wild_boar.png` | 32x32 | [16.0, 28] | legacy_charge Existing runtime pixels/catalog retained; enabled in registry. |
| giant_bee | Willosuna | READY | `game/monsters/giant_bee.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |
| red_cap | Willosuna | READY | `game/monsters/red_cap.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |
| red_crab | Willosuna | READY | `game/monsters/red_crab.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |
| giant_frog | Willosuna | READY | `game/monsters/giant_frog.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |
| rock_golem | Willosuna | READY | `game/monsters/rock_golem.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |
| living_armor | Willosuna | READY | `game/monsters/living_armor.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |
| bone_hound | Willosuna | READY | `game/monsters/bone_hound.png` | 32x32 | [16.0, 28] | none Existing runtime pixels/catalog retained; enabled in registry. |

## Source files and per-creature details

### mc_bat_swarm

Source suggested stats (not runtime balance): `{'hp': 6, 'damage': 1, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/bat_swarm_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bat_swarm/strips/bat_swarm_walk_up_4f.png`

### mc_bone_hound

Source suggested stats (not runtime balance): `{'hp': 22, 'damage': 5, 'speedClass': 'fast', 'hitbox': [6, 12, 20, 16]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/bone_hound_32x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/bone_hound/strips/bone_hound_walk_up_4f.png`

### mc_crystal_cube

Source suggested stats (not runtime balance): `{'hp': 20, 'damage': 3, 'speedClass': 'slow', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 27.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/crystal_cube_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/recolors/crystal_cube_ember_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/recolors/crystal_cube_toxic_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/crystal_cube/strips/crystal_cube_walk_up_4f.png`

### mc_gargoyle

Source suggested stats (not runtime balance): `{'hp': 22, 'damage': 4, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/gargoyle_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/gargoyle/strips/gargoyle_walk_up_4f.png`

### mc_kobold_miner

Source suggested stats (not runtime balance): `{'hp': 10, 'damage': 3, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/kobold_miner_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/recolors/kobold_miner_blue_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/kobold_miner/strips/kobold_miner_walk_up_4f.png`

### mc_living_armor

Source suggested stats (not runtime balance): `{'hp': 36, 'damage': 6, 'speedClass': 'slow', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 27.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/living_armor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/recolors/living_armor_dark_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/recolors/living_armor_gold_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/living_armor/strips/living_armor_walk_up_4f.png`

### mc_mimic_jar

Source suggested stats (not runtime balance): `{'hp': 16, 'damage': 4, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/mimic_jar_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/recolors/mimic_jar_gold_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/mimic_jar/strips/mimic_jar_walk_up_4f.png`

### mc_rock_crab

Source suggested stats (not runtime balance): `{'hp': 18, 'damage': 3, 'speedClass': 'slow', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/rock_crab_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/recolors/rock_crab_crystal_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/rock_crab/strips/rock_crab_walk_up_4f.png`

### mc_roller_bug

Source suggested stats (not runtime balance): `{'hp': 14, 'damage': 3, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/roller_bug_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/recolors/roller_bug_lava_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Cave_Dungeon/roller_bug/strips/roller_bug_walk_up_4f.png`

### mc_crab_knight

Source suggested stats (not runtime balance): `{'hp': 30, 'damage': 5, 'speedClass': 'normal', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/crab_knight_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/recolors/crab_knight_royal_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/crab_knight/strips/crab_knight_walk_up_4f.png`

### mc_jelly

Source suggested stats (not runtime balance): `{'hp': 8, 'damage': 3, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/jelly_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/recolors/jelly_sea_glass_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/jelly/strips/jelly_walk_up_4f.png`

### mc_pufferfish

Source suggested stats (not runtime balance): `{'hp': 10, 'damage': 3, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/pufferfish_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Coast/pufferfish/strips/pufferfish_walk_up_4f.png`

### mc_antlion

Source suggested stats (not runtime balance): `{'hp': 16, 'damage': 4, 'speedClass': 'slow', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/antlion_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/antlion/strips/antlion_walk_up_4f.png`

### mc_dust_devil

Source suggested stats (not runtime balance): `{'hp': 14, 'damage': 3, 'speedClass': 'fast', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 27.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/dust_devil_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/recolors/dust_devil_ash_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/recolors/dust_devil_snow_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/dust_devil/strips/dust_devil_walk_up_4f.png`

### mc_frill_lizard

Source suggested stats (not runtime balance): `{'hp': 12, 'damage': 3, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/frill_lizard_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/frill_lizard/strips/frill_lizard_walk_up_4f.png`

### mc_mummy_cat

Source suggested stats (not runtime balance): `{'hp': 10, 'damage': 2, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/mummy_cat_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/recolors/mummy_cat_shadow_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Desert/mummy_cat/strips/mummy_cat_walk_up_4f.png`

### mc_flytrap_walker

Source suggested stats (not runtime balance): `{'hp': 18, 'damage': 4, 'speedClass': 'slow', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/flytrap_walker_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/recolors/flytrap_walker_fire_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/flytrap_walker/strips/flytrap_walker_walk_up_4f.png`

### mc_jackalope

Source suggested stats (not runtime balance): `{'hp': 8, 'damage': 2, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/jackalope_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/recolors/jackalope_snow_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/jackalope/strips/jackalope_walk_up_4f.png`

### mc_moss_golem

Source suggested stats (not runtime balance): `{'hp': 60, 'damage': 8, 'speedClass': 'slow', 'hitbox': [6, 12, 20, 16]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/moss_golem_32x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/recolors/moss_golem_autumn_recolor_32x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/moss_golem/strips/moss_golem_walk_up_4f.png`

### mc_mushroom_folk

Source suggested stats (not runtime balance): `{'hp': 14, 'damage': 3, 'speedClass': 'normal', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/mushroom_folk_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/recolors/mushroom_folk_toxic_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/mushroom_folk/strips/mushroom_folk_walk_up_4f.png`

### mc_owlbear_cub

Source suggested stats (not runtime balance): `{'hp': 16, 'damage': 4, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/owlbear_cub_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/owlbear_cub/strips/owlbear_cub_walk_up_4f.png`

### mc_thorn_sprite

Source suggested stats (not runtime balance): `{'hp': 6, 'damage': 2, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/thorn_sprite_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Forest/thorn_sprite/strips/thorn_sprite_walk_up_4f.png`

### mc_pumpkin_head

Source suggested stats (not runtime balance): `{'hp': 26, 'damage': 5, 'speedClass': 'normal', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/pumpkin_head_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/recolors/pumpkin_head_winter_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/pumpkin_head/strips/pumpkin_head_walk_up_4f.png`

### mc_shadow_cat

Source suggested stats (not runtime balance): `{'hp': 10, 'damage': 3, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/shadow_cat_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/shadow_cat/strips/shadow_cat_walk_up_4f.png`

### mc_wraith

Source suggested stats (not runtime balance): `{'hp': 20, 'damage': 4, 'speedClass': 'normal', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/wraith_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/recolors/wraith_ember_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Night/wraith/strips/wraith_walk_up_4f.png`

### mc_bumble_guard

Source suggested stats (not runtime balance): `{'hp': 8, 'damage': 2, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/bumble_guard_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/bumble_guard/strips/bumble_guard_walk_up_4f.png`

### mc_cyclops

Source suggested stats (not runtime balance): `{'hp': 40, 'damage': 7, 'speedClass': 'slow', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/cyclops_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/cyclops/strips/cyclops_walk_up_4f.png`

### mc_goblin_bomber

Source suggested stats (not runtime balance): `{'hp': 10, 'damage': 6, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/goblin_bomber_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/goblin_bomber/strips/goblin_bomber_walk_up_4f.png`

### mc_ratfolk_thief

Source suggested stats (not runtime balance): `{'hp': 10, 'damage': 3, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/ratfolk_thief_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Plains/ratfolk_thief/strips/ratfolk_thief_walk_up_4f.png`

### mc_frost_fox

Source suggested stats (not runtime balance): `{'hp': 12, 'damage': 3, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/frost_fox_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/recolors/frost_fox_arctic_night_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_fox/strips/frost_fox_walk_up_4f.png`

### mc_frost_moth

Source suggested stats (not runtime balance): `{'hp': 8, 'damage': 2, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/frost_moth_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/recolors/frost_moth_luna_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/frost_moth/strips/frost_moth_walk_up_4f.png`

### mc_yeti_cub

Source suggested stats (not runtime balance): `{'hp': 18, 'damage': 3, 'speedClass': 'normal', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/yeti_cub_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Snow/yeti_cub/strips/yeti_cub_walk_up_4f.png`

### mc_bog_lurker

Source suggested stats (not runtime balance): `{'hp': 16, 'damage': 3, 'speedClass': 'slow', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/bog_lurker_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/bog_lurker/strips/bog_lurker_walk_up_4f.png`

### mc_giant_mosquito

Source suggested stats (not runtime balance): `{'hp': 6, 'damage': 2, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/giant_mosquito_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/recolors/giant_mosquito_plague_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/giant_mosquito/strips/giant_mosquito_walk_up_4f.png`

### mc_lily_snapper

Source suggested stats (not runtime balance): `{'hp': 20, 'damage': 4, 'speedClass': 'slow', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/lily_snapper_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/recolors/lily_snapper_desert_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lily_snapper/strips/lily_snapper_walk_up_4f.png`

### mc_lizardfolk

Source suggested stats (not runtime balance): `{'hp': 26, 'damage': 5, 'speedClass': 'normal', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/lizardfolk_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/recolors/lizardfolk_desert_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/lizardfolk/strips/lizardfolk_walk_up_4f.png`

### mc_swamp_hag

Source suggested stats (not runtime balance): `{'hp': 24, 'damage': 5, 'speedClass': 'slow', 'hitbox': [3, 22, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/swamp_hag_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/recolors/swamp_hag_frost_recolor_16x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Swamp/swamp_hag/strips/swamp_hag_walk_up_4f.png`

### mc_ash_hound

Source suggested stats (not runtime balance): `{'hp': 30, 'damage': 6, 'speedClass': 'fast', 'hitbox': [6, 12, 20, 16]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/ash_hound_32x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/recolors/ash_hound_frost_recolor_32x32_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/ash_hound/strips/ash_hound_walk_up_4f.png`

### mc_flame_skull

Source suggested stats (not runtime balance): `{'hp': 10, 'damage': 4, 'speedClass': 'fast', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 26.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/flame_skull_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/recolors/flame_skull_soul_recolor_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/flame_skull/strips/flame_skull_walk_up_4f.png`

### mc_magma_slug

Source suggested stats (not runtime balance): `{'hp': 20, 'damage': 4, 'speedClass': 'slow', 'hitbox': [3, 6, 10, 8]}`. Actual auxiliary files: 25.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/magma_slug_16x16_sheet.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_attack_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_attack_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_attack_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_attack_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_death_down_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_death_left_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_death_right_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_death_up_6f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_hurt_down_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_hurt_left_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_hurt_right_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_hurt_up_3f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_idle_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_idle_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_idle_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_idle_up_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_special_down_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_special_left_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_special_right_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_special_up_5f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_walk_down_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_walk_left_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_walk_right_4f.png`
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/01_Monsters_Volcanic/magma_slug/strips/magma_slug_walk_up_4f.png`

### sc_goblin

| Move | Frames | Source cell | Source pivot | Declared fps | Frame duration ms |
| --- | --- | --- | --- | --- | --- |
| attack | 26 | [298, 291] | [148, 256] | 16 | 62 |
| death | 19 | [298, 291] | [148, 256] | 14 | 71 |
| hurt | 10 | [298, 291] | [148, 256] | 18 | 56 |
| idle | 21 | [298, 291] | [148, 256] | 5 | 200 |
| walk | 20 | [298, 291] | [148, 256] | 12 | 83 |

SpriteCanvas runtime uses uniform Nearest normalization to a 32px longest edge (48px Golem), with the same transform for pivot and all animation canvases. Source files are untouched. Playback uses actual JSON duration milliseconds.

### sc_golem

| Move | Frames | Source cell | Source pivot | Declared fps | Frame duration ms |
| --- | --- | --- | --- | --- | --- |
| attack | 28 | [288, 301] | [149, 271] | 15 | 67 |
| death | 24 | [288, 301] | [149, 271] | 15 | 67 |
| hurt | 8 | [288, 301] | [149, 271] | 15 | 67 |
| idle | 21 | [288, 301] | [149, 271] | 5 | 200 |
| walk | 20 | [288, 301] | [149, 271] | 10 | 100 |

SpriteCanvas runtime uses uniform Nearest normalization to a 32px longest edge (48px Golem), with the same transform for pivot and all animation canvases. Source files are untouched. Playback uses actual JSON duration milliseconds.

### sc_mushroom

| Move | Frames | Source cell | Source pivot | Declared fps | Frame duration ms |
| --- | --- | --- | --- | --- | --- |
| attack | 19 | [221, 156] | [102, 143] | 16 | 62 |
| death | 17 | [221, 156] | [102, 143] | 16 | 62 |
| hurt | 11 | [221, 156] | [102, 143] | 18 | 56 |
| idle | 21 | [221, 156] | [102, 143] | 5 | 200 |
| walk | 12 | [221, 156] | [102, 143] | 12 | 83 |

SpriteCanvas runtime uses uniform Nearest normalization to a 32px longest edge (48px Golem), with the same transform for pivot and all animation canvases. Source files are untouched. Playback uses actual JSON duration milliseconds.

### sc_slime

| Move | Frames | Source cell | Source pivot | Declared fps | Frame duration ms |
| --- | --- | --- | --- | --- | --- |
| attack | 21 | [178, 164] | [104, 153] | 16 | 62 |
| death | 21 | [178, 164] | [104, 153] | 14 | 71 |
| hurt | 8 | [178, 164] | [104, 153] | 16 | 62 |
| idle | 35 | [178, 164] | [104, 153] | 8 | 125 |
| walk | 12 | [178, 164] | [104, 153] | 12 | 83 |

SpriteCanvas runtime uses uniform Nearest normalization to a 32px longest edge (48px Golem), with the same transform for pivot and all animation canvases. Source files are untouched. Playback uses actual JSON duration milliseconds.

## Boss metadata (not regular enemies)

### bone_archmage

`asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/bone_archmage_48x48_sheet.png`, actual sheet (384, 576), cell [48, 48], bottom-center pivot [24.0, 48]. Not imported; boss pool excluded.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_bone_spikes_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_death_8f.png`: 8 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_intro_roar_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_orb_barrage_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_p2_bone_spikes_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_p2_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_p2_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_p2_orb_barrage_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_p2_walk_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/bone_archmage/strips/bone_archmage_walk_4f.png`: 4 frames

### dragon_whelp

`asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/dragon_whelp_48x48_sheet.png`, actual sheet (384, 576), cell [48, 48], bottom-center pivot [24.0, 48]. Not imported; boss pool excluded.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_death_8f.png`: 8 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_fire_breath_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_intro_roar_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_p2_fire_breath_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_p2_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_p2_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_p2_tail_swipe_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_p2_walk_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_tail_swipe_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/dragon_whelp/strips/dragon_whelp_walk_4f.png`: 4 frames

### elder_treant

`asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/elder_treant_64x64_sheet.png`, actual sheet (512, 768), cell [64, 64], bottom-center pivot [32.0, 64]. Not imported; boss pool excluded.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_branch_slam_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_death_8f.png`: 8 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_intro_roar_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_p2_branch_slam_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_p2_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_p2_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_p2_root_spikes_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_p2_walk_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_root_spikes_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/elder_treant/strips/elder_treant_walk_4f.png`: 4 frames

### gel_monarch

`asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/gel_monarch_48x48_sheet.png`, actual sheet (384, 576), cell [48, 48], bottom-center pivot [24.0, 48]. Not imported; boss pool excluded.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_death_8f.png`: 8 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_intro_roar_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_p2_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_p2_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_p2_royal_slam_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_p2_spawn_cubes_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_p2_walk_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_royal_slam_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_spawn_cubes_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/gel_monarch/strips/gel_monarch_walk_4f.png`: 4 frames

### golem_king

`asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/golem_king_64x64_sheet.png`, actual sheet (512, 768), cell [64, 64], bottom-center pivot [32.0, 64]. Not imported; boss pool excluded.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_death_8f.png`: 8 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_fist_quake_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_intro_roar_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_p2_fist_quake_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_p2_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_p2_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_p2_rock_throw_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_p2_walk_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_rock_throw_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/golem_king/strips/golem_king_walk_4f.png`: 4 frames

### tide_kraken

`asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/tide_kraken_64x64_sheet.png`, actual sheet (448, 768), cell [64, 64], bottom-center pivot [32.0, 64]. Not imported. Tide Kraken actual death strip is 7f; generic README says 8f.

- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_death_7f.png`: 7 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_ink_spray_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_intro_roar_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_p2_hurt_3f.png`: 3 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_p2_idle_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_p2_ink_spray_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_p2_tentacle_slam_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_p2_walk_4f.png`: 4 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_tentacle_slam_6f.png`: 6 frames
- `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/02_Bosses/tide_kraken/strips/tide_kraken_walk_4f.png`: 4 frames

Lich Lord remains the designated main boss from the existing catalog/GDD. No boss is registered in EnemyRegistry or natural spawn.

## License and exclusions

MonstersCreatures LICENSE allows personal/commercial projects and modification/cutting, prohibits standalone asset redistribution and AI-training datasets. This task uses the art only inside the game, not as training data. SpriteCanvas licenses are CC0. Runtime license copies accompany derived assets. Tiny Questers/Willosuna use existing licensed runtime files unchanged; no restricted artwork is exported for AI inspection. N3XUS is excluded by its previously audited AI restriction.

READY 78 (67 MC plus 11 existing); ADAPTABLE 4 SpriteCanvas; BLOCKED regular species 0 after successful decode validation. Bosses and FX are intentionally not normal-enemy imports. Recolor variants are counted separately from base species.

## Complete source tree summary

| Top-level folder | Files | Extensions |
| --- | --- | --- |
| 01_Monsters_Cave_Dungeon | 233 | {'.png': 233} |
| 01_Monsters_Coast | 77 | {'.png': 77} |
| 01_Monsters_Desert | 103 | {'.png': 103} |
| 01_Monsters_Forest | 154 | {'.png': 154} |
| 01_Monsters_Night | 77 | {'.png': 77} |
| 01_Monsters_Plains | 100 | {'.png': 100} |
| 01_Monsters_Snow | 77 | {'.png': 77} |
| 01_Monsters_Swamp | 129 | {'.png': 129} |
| 01_Monsters_Volcanic | 77 | {'.png': 77} |
| 02_Bosses | 78 | {'.png': 78} |
| 03_Projectiles_FX | 123 | {'.png': 123} |
| 04_Status_Effects | 39 | {'.png': 39} |
| 05_Loot_Drops | 70 | {'.png': 70} |
| 06_Combat_UI | 65 | {'.png': 65} |
| 07_Bestiary | 94 | {'.png': 94} |
| Example_Scenes | 16 | {'.png': 8, '.gif': 8} |
| Palette | 3 | {'.gpl': 1, '.hex': 1, '.png': 1} |
| Previews | 12 | {'.png': 12} |

Example scenes, previews, palette and bestiary are reference material and are not copied into runtime. Boss source sheets/strips are metadata-only; Tide Kraken actually has seven sheet columns and a seven-frame Death, unlike the generic eight-frame Death description.
