# Enemy Skill Assignment — Task08.1

82 loài, policy lấy trực tiếp từ EnemyDefinition.skill; không sửa HP/damage/contact/spawn weights cũ. Boss body luôn NONE và không nằm trong bảng này. Cooldown tính giữa các lần bắt đầu cast; skill damage1. Gate dùng progress, các timer dùng giây gameplay thực. Chỉ bắt đầu trên camera, có LOS; đạn chốt hướng lúc release.

SPECIAL chỉ ghi khi catalog thật có clip cho đủ bốn hướng, còn lại ATTACK fallback. Windup/recover retime clip thật, không suy đoán asset Special. NONE chủ động giữ melee; chuỗi special cũ trong roster vẫn chỉ là gợi ý nghiên cứu.

| Enemy ID | Loài | Biome / family | Skill / pattern | Range | CD Easy / Hard | Windup E/H | Speed | FX | Animation | Balance / lý do |
|---|---|---|---|---:|---:|---:|---:|---|---|---|
| tiny_slime | Tiny Questers Slime | Tiny / tiny_slime | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| tiny_bat | Tiny Questers Bat | Tiny / tiny_bat | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| honey_slime | Honey Slime | Willosuna / honey_slime | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| wild_boar | Wild Boar | Willosuna / wild_boar | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ legacy charge, không cộng skill mới |
| giant_bee | Giant Bee | Willosuna / giant_bee | STINGER / 1 straight | 130.0 | 4.75 / 3.17 | 0.45 / 0.39 | 135.0 | thorn / thorn_impact | ATTACK (fallback) | damage1; gate20 |
| red_cap | Red Cap | Willosuna / red_cap | SPORE / 1 straight | 125.0 | 6.05 / 4.03 | 0.54 / 0.46 | 100.0 | spore / spore_impact | ATTACK (fallback) | damage1; gate45; POISON |
| red_crab | Red Crab | Willosuna / red_crab | CLAW_SLAM / SLAM R30.0 | 42.0 | 5.18 / 3.46 | 0.78 / 0.66 | 0.0 | quake / quake | ATTACK (fallback) | damage1; gate20 |
| giant_frog | Giant Frog | Willosuna / giant_frog | BUBBLE / 1 straight | 135.0 | 5.18 / 3.46 | 0.49 / 0.41 | 105.0 | bubble / bubble_impact | ATTACK (fallback) | damage1; gate30 |
| rock_golem | Rock Golem | Willosuna / rock_golem | SLAM / SLAM R38.0 | 60.0 | 6.48 / 4.32 | 0.84 / 0.72 | 0.0 | quake / quake | ATTACK (fallback) | damage1; gate75 |
| living_armor | Living Armor | Willosuna / living_armor | SPIRIT_SLAM / SLAM R40.0 | 66.0 | 7.99 / 5.33 | 0.89 / 0.75 | 0.0 | shockwave / shockwave | ATTACK (fallback) | damage1; gate150 |
| bone_hound | Bone Hound | Willosuna / bone_hound | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_bat_swarm | Bat Swarm | Cave / mc_bat_swarm | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_bone_hound | Bone Hound | Cave / mc_bone_hound | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_crystal_cube | Crystal Cube | Cave / mc_crystal_cube | CRYSTAL_SHARD / 1 straight | 145.0 | 5.62 / 3.74 | 0.52 / 0.44 | 130.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate90 |
| mc_crystal_cube_ember | Crystal Cube / ember | Cave / mc_crystal_cube | FIREBALL / 1 straight | 150.0 | 5.62 / 3.74 | 0.49 / 0.41 | 125.0 | fireball / fireball_impact | SPECIAL | damage1; gate90; BURN |
| mc_crystal_cube_toxic | Crystal Cube / toxic | Cave / mc_crystal_cube | TOXIC / 1 straight | 135.0 | 7.99 / 5.33 | 0.65 / 0.55 | 95.0 | spore / spore_impact | SPECIAL | damage1; gate150; POISON; poison area cap3 |
| mc_gargoyle | Gargoyle | Cave / mc_gargoyle | ROCK / 1 straight | 140.0 | 5.18 / 3.46 | 0.52 / 0.44 | 115.0 | rock / rock_impact | SPECIAL | damage1; gate30 |
| mc_kobold_miner | Kobold Miner | Cave / mc_kobold_miner | ROCK / 1 straight | 140.0 | 5.18 / 3.46 | 0.52 / 0.44 | 115.0 | rock / rock_impact | SPECIAL | damage1; gate30 |
| mc_kobold_miner_blue | Kobold Miner / blue | Cave / mc_kobold_miner | ROCK / 1 straight | 140.0 | 5.18 / 3.46 | 0.52 / 0.44 | 115.0 | rock / rock_impact | SPECIAL | damage1; gate30 |
| mc_living_armor | Living Armor | Cave / mc_living_armor | SPIRIT_SLAM / SLAM R40.0 | 66.0 | 7.99 / 5.33 | 0.89 / 0.75 | 0.0 | shockwave / shockwave | SPECIAL | damage1; gate150 |
| mc_living_armor_dark | Living Armor / dark | Cave / mc_living_armor | SPIRIT_SLAM / SLAM R40.0 | 66.0 | 7.99 / 5.33 | 0.89 / 0.75 | 0.0 | shockwave / shockwave | SPECIAL | damage1; gate150 |
| mc_living_armor_gold | Living Armor / gold | Cave / mc_living_armor | SPIRIT_SLAM / SLAM R40.0 | 66.0 | 7.99 / 5.33 | 0.89 / 0.75 | 0.0 | shockwave / shockwave | SPECIAL | damage1; gate150 |
| mc_mimic_jar | Mimic Jar | Cave / mc_mimic_jar | COIN / 1 straight | 140.0 | 5.18 / 3.46 | 0.45 / 0.39 | 125.0 | coin_shot / coin_shot_impact | SPECIAL | damage1; gate45 |
| mc_mimic_jar_gold | Mimic Jar / gold | Cave / mc_mimic_jar | COIN / 1 straight | 140.0 | 5.18 / 3.46 | 0.45 / 0.39 | 125.0 | coin_shot / coin_shot_impact | SPECIAL | damage1; gate45 |
| mc_rock_crab | Rock Crab | Cave / mc_rock_crab | CLAW_SLAM / SLAM R30.0 | 42.0 | 5.18 / 3.46 | 0.78 / 0.66 | 0.0 | quake / quake | SPECIAL | damage1; gate20 |
| mc_rock_crab_crystal | Rock Crab / crystal | Cave / mc_rock_crab | CRYSTAL_SHARD / 1 straight | 145.0 | 5.62 / 3.74 | 0.52 / 0.44 | 130.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate90 |
| mc_roller_bug | Roller Bug | Cave / mc_roller_bug | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_roller_bug_lava | Roller Bug / lava | Cave / mc_roller_bug | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_crab_knight | Crab Knight | Coast / mc_crab_knight | SLAM / SLAM R38.0 | 60.0 | 6.48 / 4.32 | 0.84 / 0.72 | 0.0 | quake / quake | SPECIAL | damage1; gate75 |
| mc_crab_knight_royal | Crab Knight / royal | Coast / mc_crab_knight | SLAM / SLAM R38.0 | 60.0 | 6.48 / 4.32 | 0.84 / 0.72 | 0.0 | quake / quake | SPECIAL | damage1; gate75 |
| mc_jelly | Jelly | Coast / mc_jelly | SPIRIT_SLAM / SLAM R40.0 | 66.0 | 7.99 / 5.33 | 0.89 / 0.75 | 0.0 | shockwave / shockwave | SPECIAL | damage1; gate150 |
| mc_jelly_sea_glass | Jelly / sea_glass | Coast / mc_jelly | SPIRIT_SLAM / SLAM R40.0 | 66.0 | 7.99 / 5.33 | 0.89 / 0.75 | 0.0 | shockwave / shockwave | SPECIAL | damage1; gate150 |
| mc_pufferfish | Pufferfish | Coast / mc_pufferfish | THORN_FAN / 3 fan, 24.0° gap | 145.0 | 6.70 / 4.46 | 0.56 / 0.48 | 120.0 | thorn / thorn_impact | SPECIAL | damage1; gate75 |
| mc_antlion | Antlion | Desert / mc_antlion | DUST_FAN / 3 fan, 24.0° gap | 140.0 | 7.13 / 4.75 | 0.56 / 0.48 | 115.0 | rock / rock_impact | SPECIAL | damage1; gate90 |
| mc_dust_devil | Dust Devil | Desert / mc_dust_devil | DUST_FAN / 3 fan, 24.0° gap | 140.0 | 7.13 / 4.75 | 0.56 / 0.48 | 115.0 | rock / rock_impact | SPECIAL | damage1; gate90 |
| mc_dust_devil_ash | Dust Devil / ash | Desert / mc_dust_devil | FIREBALL / 1 straight | 150.0 | 5.62 / 3.74 | 0.49 / 0.41 | 125.0 | fireball / fireball_impact | SPECIAL | damage1; gate90; BURN |
| mc_dust_devil_snow | Dust Devil / snow | Desert / mc_dust_devil | FROST_FAN / 3 fan, 24.0° gap | 145.0 | 7.56 / 5.04 | 0.60 / 0.52 | 115.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate150; SLOW |
| mc_frill_lizard | Frill Lizard | Desert / mc_frill_lizard | POISON / 1 straight | 140.0 | 5.83 / 3.89 | 0.52 / 0.44 | 110.0 | poison_spit / poison_spit_impact | SPECIAL | damage1; gate75; POISON |
| mc_mummy_cat | Mummy Cat | Desert / mc_mummy_cat | CURSE_AOE / TARGET R26.0 | 150.0 | 8.64 / 5.76 | 0.86 / 0.74 | 0.0 | magic_orb / magic_orb_impact | SPECIAL | damage1; gate210 |
| mc_mummy_cat_shadow | Mummy Cat / shadow | Desert / mc_mummy_cat | CURSE_AOE / TARGET R26.0 | 150.0 | 8.64 / 5.76 | 0.86 / 0.74 | 0.0 | magic_orb / magic_orb_impact | SPECIAL | damage1; gate210 |
| mc_flytrap_walker | Flytrap Walker | Forest / mc_flytrap_walker | SEED / 1 straight | 130.0 | 4.75 / 3.17 | 0.43 / 0.37 | 110.0 | seed / seed_impact | SPECIAL | damage1; gate20 |
| mc_flytrap_walker_fire | Flytrap Walker / fire | Forest / mc_flytrap_walker | FIREBALL / 1 straight | 150.0 | 5.62 / 3.74 | 0.49 / 0.41 | 125.0 | fireball / fireball_impact | SPECIAL | damage1; gate90; BURN |
| mc_jackalope | Jackalope | Forest / mc_jackalope | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_jackalope_snow | Jackalope / snow | Forest / mc_jackalope | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_moss_golem | Moss Golem | Forest / mc_moss_golem | SLAM / SLAM R38.0 | 60.0 | 6.48 / 4.32 | 0.84 / 0.72 | 0.0 | quake / quake | SPECIAL | damage1; gate75 |
| mc_moss_golem_autumn | Moss Golem / autumn | Forest / mc_moss_golem | SLAM / SLAM R38.0 | 60.0 | 6.48 / 4.32 | 0.84 / 0.72 | 0.0 | quake / quake | SPECIAL | damage1; gate75 |
| mc_mushroom_folk | Mushroom Folk | Forest / mc_mushroom_folk | SPORE / 1 straight | 125.0 | 6.05 / 4.03 | 0.54 / 0.46 | 100.0 | spore / spore_impact | SPECIAL | damage1; gate45; POISON |
| mc_mushroom_folk_toxic | Mushroom Folk / toxic | Forest / mc_mushroom_folk | TOXIC / 1 straight | 135.0 | 7.99 / 5.33 | 0.65 / 0.55 | 95.0 | spore / spore_impact | SPECIAL | damage1; gate150; POISON; poison area cap3 |
| mc_owlbear_cub | Owlbear Cub | Forest / mc_owlbear_cub | CLAW_SLAM / SLAM R30.0 | 42.0 | 5.18 / 3.46 | 0.78 / 0.66 | 0.0 | quake / quake | SPECIAL | damage1; gate20 |
| mc_thorn_sprite | Thorn Sprite | Forest / mc_thorn_sprite | THORN_FAN / 3 fan, 24.0° gap | 145.0 | 6.70 / 4.46 | 0.56 / 0.48 | 120.0 | thorn / thorn_impact | SPECIAL | damage1; gate75 |
| mc_pumpkin_head | Pumpkin Head | Night / mc_pumpkin_head | FIREBALL / 1 straight | 150.0 | 5.62 / 3.74 | 0.49 / 0.41 | 125.0 | fireball / fireball_impact | SPECIAL | damage1; gate90; BURN |
| mc_pumpkin_head_winter | Pumpkin Head / winter | Night / mc_pumpkin_head | SNOWBALL / 1 straight | 140.0 | 5.18 / 3.46 | 0.49 / 0.41 | 110.0 | snowball / snowball_impact | SPECIAL | damage1; gate45; SLOW |
| mc_shadow_cat | Shadow Cat | Night / mc_shadow_cat | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_wraith | Wraith | Night / mc_wraith | SHADOW / 1 straight | 155.0 | 6.26 / 4.18 | 0.54 / 0.46 | 130.0 | shadow_orb / shadow_orb_impact | SPECIAL | damage1; gate180 |
| mc_wraith_ember | Wraith / ember | Night / mc_wraith | FIRE_BURST / 5 fan, 18.0° gap | 150.0 | 9.50 / 6.34 | 0.65 / 0.55 | 120.0 | fireball / fireball_impact | SPECIAL | damage1; gate300; BURN |
| mc_bumble_guard | Bumble Guard | Plains / mc_bumble_guard | STINGER / 1 straight | 130.0 | 4.75 / 3.17 | 0.45 / 0.39 | 135.0 | thorn / thorn_impact | SPECIAL | damage1; gate20 |
| mc_cyclops | Cyclops | Plains / mc_cyclops | SLAM / SLAM R38.0 | 60.0 | 6.48 / 4.32 | 0.84 / 0.72 | 0.0 | quake / quake | SPECIAL | damage1; gate75 |
| mc_goblin_bomber | Goblin Bomber | Plains / mc_goblin_bomber | TARGET_BOMB / TARGET R28.0 | 150.0 | 8.42 / 5.62 | 0.89 / 0.75 | 0.0 | bomb / bomb_impact | SPECIAL | damage1; gate120 |
| mc_ratfolk_thief | Ratfolk Thief | Plains / mc_ratfolk_thief | COIN / 1 straight | 140.0 | 5.18 / 3.46 | 0.45 / 0.39 | 125.0 | coin_shot / coin_shot_impact | SPECIAL | damage1; gate45 |
| mc_frost_fox | Frost Fox | Snow / mc_frost_fox | ICE / 1 straight | 145.0 | 6.05 / 4.03 | 0.52 / 0.44 | 120.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate75; SLOW |
| mc_frost_fox_arctic_night | Frost Fox / arctic_night | Snow / mc_frost_fox | ICE / 1 straight | 145.0 | 6.05 / 4.03 | 0.52 / 0.44 | 120.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate75; SLOW |
| mc_frost_moth | Frost Moth | Snow / mc_frost_moth | FROST_FAN / 3 fan, 24.0° gap | 145.0 | 7.56 / 5.04 | 0.60 / 0.52 | 115.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate150; SLOW |
| mc_frost_moth_luna | Frost Moth / luna | Snow / mc_frost_moth | FROST_FAN / 3 fan, 24.0° gap | 145.0 | 7.56 / 5.04 | 0.60 / 0.52 | 115.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate150; SLOW |
| mc_yeti_cub | Yeti Cub | Snow / mc_yeti_cub | SNOWBALL / 1 straight | 140.0 | 5.18 / 3.46 | 0.49 / 0.41 | 110.0 | snowball / snowball_impact | SPECIAL | damage1; gate45; SLOW |
| mc_bog_lurker | Bog Lurker | Swamp / mc_bog_lurker | POISON / 1 straight | 140.0 | 5.83 / 3.89 | 0.52 / 0.44 | 110.0 | poison_spit / poison_spit_impact | SPECIAL | damage1; gate75; POISON |
| mc_giant_mosquito | Giant Mosquito | Swamp / mc_giant_mosquito | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_giant_mosquito_plague | Giant Mosquito / plague | Swamp / mc_giant_mosquito | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |
| mc_lily_snapper | Lily Snapper | Swamp / mc_lily_snapper | BUBBLE / 1 straight | 135.0 | 5.18 / 3.46 | 0.49 / 0.41 | 105.0 | bubble / bubble_impact | SPECIAL | damage1; gate30 |
| mc_lily_snapper_desert | Lily Snapper / desert | Swamp / mc_lily_snapper | DUST_FAN / 3 fan, 24.0° gap | 140.0 | 7.13 / 4.75 | 0.56 / 0.48 | 115.0 | rock / rock_impact | SPECIAL | damage1; gate90 |
| mc_lizardfolk | Lizardfolk | Swamp / mc_lizardfolk | POISON / 1 straight | 140.0 | 5.83 / 3.89 | 0.52 / 0.44 | 110.0 | poison_spit / poison_spit_impact | SPECIAL | damage1; gate75; POISON |
| mc_lizardfolk_desert | Lizardfolk / desert | Swamp / mc_lizardfolk | ROCK / 1 straight | 140.0 | 5.18 / 3.46 | 0.52 / 0.44 | 115.0 | rock / rock_impact | SPECIAL | damage1; gate30 |
| mc_swamp_hag | Swamp Hag | Swamp / mc_swamp_hag | MAGIC_FAN / 3 fan, 24.0° gap | 155.0 | 7.56 / 5.04 | 0.59 / 0.51 | 125.0 | hex_bolt / hex_bolt_impact | SPECIAL | damage1; gate150 |
| mc_swamp_hag_frost | Swamp Hag / frost | Swamp / mc_swamp_hag | FROST_FAN / 3 fan, 24.0° gap | 145.0 | 7.56 / 5.04 | 0.60 / 0.52 | 115.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate150; SLOW |
| mc_ash_hound | Ash Hound | Volcanic / mc_ash_hound | FIREBALL / 1 straight | 150.0 | 5.62 / 3.74 | 0.49 / 0.41 | 125.0 | fireball / fireball_impact | SPECIAL | damage1; gate90; BURN |
| mc_ash_hound_frost | Ash Hound / frost | Volcanic / mc_ash_hound | ICE / 1 straight | 145.0 | 6.05 / 4.03 | 0.52 / 0.44 | 120.0 | ice_shard / ice_shard_impact | SPECIAL | damage1; gate75; SLOW |
| mc_flame_skull | Flame Skull | Volcanic / mc_flame_skull | FIRE_BURST / 5 fan, 18.0° gap | 150.0 | 9.50 / 6.34 | 0.65 / 0.55 | 120.0 | fireball / fireball_impact | SPECIAL | damage1; gate300; BURN |
| mc_flame_skull_soul | Flame Skull / soul | Volcanic / mc_flame_skull | SHADOW_BURST / 5 fan, 18.0° gap | 150.0 | 9.50 / 6.34 | 0.65 / 0.55 | 115.0 | shadow_orb / shadow_orb_impact | SPECIAL | damage1; gate300 |
| mc_magma_slug | Magma Slug | Volcanic / mc_magma_slug | LAVA / 1 straight | 125.0 | 7.56 / 5.04 | 0.63 / 0.53 | 100.0 | lava_glob / lava_glob_impact | SPECIAL | damage1; gate150; BURN |
| sc_goblin | SpriteCanvas Goblin | CC0 / sc_goblin | ARROW / 1 straight | 145.0 | 4.54 / 3.02 | 0.43 / 0.37 | 135.0 | arrow / arrow_impact | ATTACK (fallback) | damage1; gate15 |
| sc_golem | SpriteCanvas Golem | CC0 / sc_golem | SLAM / SLAM R38.0 | 60.0 | 6.48 / 4.32 | 0.84 / 0.72 | 0.0 | quake / quake | ATTACK (fallback) | damage1; gate75 |
| sc_mushroom | SpriteCanvas Mushroom | CC0 / sc_mushroom | SPORE / 1 straight | 125.0 | 6.05 / 4.03 | 0.54 / 0.46 | 100.0 | spore / spore_impact | ATTACK (fallback) | damage1; gate45; POISON |
| sc_slime | SpriteCanvas Slime | CC0 / sc_slime | NONE / contact-only | 0.0 | 0.00 / 0.00 | 0.00 / 0.00 | 0.0 | — | melee cũ | Giữ chase/contact; không ép phép từ clip Special |

**Tổng:** 67 có kỹ năng; 15 NONE. Không có skill thường trên Boss. Budget chung: caster tối đa8 Easy/10 Hard (progress<90:3/4), telegraph3/4, projectile24, FX24, poison area3. Global gate0,42/0,24s; recovery projectile0,18/0,12s, melee/AoE0,26/0,20s; stand-off86/96. Không tăng hai A* mỗi frame.
