# TASK 04A — Full Enemy Roster, Animation Integration & Dynamic Spawn

Ngày thực hiện: 13–14/11/2026. Project: `D:\Android\MonsterSurvivor`.

## Kết quả

Runtime có **82 định nghĩa quái thường tham gia gameplay thật**, tăng từ ba loại đã được nối gameplay trước task. Gồm 40 loài gốc và 27 biến thể MonstersCreatures, chín Willosuna, hai Tiny Questers và bốn SpriteCanvas. Không có quái thường MonstersCreatures nào bị bỏ qua. Đã kiểm tra từng ID qua spawn, chase, contact damage, bốn vũ khí, kill/drop và Death; kiểm tra GameScreen/OpenGL bổ sung cho toàn roster.

Hệ thống dùng EnemyDefinition/Registry, AI dùng chung và spawn có trọng số theo progress. Cap resident vẫn Easy 40 / Hard 60, bao gồm xác đang chạy Death. Android Studio Run trên Pixel_4 thành công. Phép đo ngắn SwiftShader đạt 21,31–25,13 FPS; chưa đạt 60 FPS.

Tài liệu chi tiết:

- [ENEMY_ASSET_INVENTORY.md](ENEMY_ASSET_INVENTORY.md): nguồn thực tế, từng loài/biến thể, clip, pivot, file phụ, license và boss metadata.
- [ENEMY_ROSTER.md](ENEMY_ROSTER.md): toàn bộ combat tuning, footprint, scale, pivot, unlock, weight và family cap.
- [COMBAT_FX_INTEGRATION_PLAN.md](COMBAT_FX_INTEGRATION_PLAN.md): nghiên cứu FX, status, loot/UI; chưa triển khai Combat FX mới.

## Kiểm kê nguồn thực tế

Đã đọc README/LICENSE và kiểm tra kích thước PNG, strip/sheet, JSON SpriteCanvas. Không lấy số frame hoặc kích thước từ báo cáo cũ thay cho source.

| Thư mục MonstersCreatures | Loài gốc | Recolor được đăng ký riêng | Tổng định nghĩa thường |
| --- | ---: | ---: | ---: |
| 01_Monsters_Cave_Dungeon | 9 | 8 | 17 |
| 01_Monsters_Coast | 3 | 2 | 5 |
| 01_Monsters_Desert | 4 | 3 | 7 |
| 01_Monsters_Forest | 6 | 4 | 10 |
| 01_Monsters_Night | 3 | 2 | 5 |
| 01_Monsters_Plains | 4 | 0 | 4 |
| 01_Monsters_Snow | 3 | 2 | 5 |
| 01_Monsters_Swamp | 5 | 4 | 9 |
| 01_Monsters_Volcanic | 3 | 2 | 5 |
| **Tổng** | **40** | **27** | **67** |

| Nhóm | READY | ADAPTABLE | BLOCKED | Gameplay runtime |
| --- | ---: | ---: | ---: | ---: |
| MonstersCreatures thường | 67 | 0 | 0 | 67 |
| Willosuna thường đã có asset | 9 | 0 | 0 | 9 |
| Tiny Questers Slime/Bat hiện có | 2 | 0 | 0 | 2 |
| SpriteCanvas Goblin/Golem/Mushroom/Slime | 0 | 4 | 0 | 4 |
| **Tổng thường** | **78** | **4** | **0** | **82** |

SpriteCanvas được đánh dấu ADAPTABLE vì chỉ có side view: LEFT dùng source, RIGHT mirror, UP/DOWN dùng cùng side view. Không tuyên bố có artwork bốn hướng mới.

Ngoài roster: `02_Bosses` có sáu boss; `03_Projectiles_FX` 123 PNG; `04_Status_Effects` 39 PNG; `05_Loot_Drops` 70 PNG; `06_Combat_UI` 65 PNG; `07_Bestiary` 94 PNG. Các số PNG bao gồm combined sheet và bản lặp, không phải số animation độc lập. Cây nguồn và file phụ của từng loài nằm trong inventory.

### Asset chưa nối gameplay và lý do

- Sáu boss `bone_archmage`, `dragon_whelp`, `elder_treant`, `gel_monarch`, `golem_king`, `tide_kraken`: đã lưu kích thước/cell/clip/pivot, không import vào atlas mới hoặc pool thường theo phạm vi task. Tide Kraken thực tế có bảy cột và Death bảy frame, khác mô tả tám frame chung trong README.
- `lich_lord`: tiếp tục là hợp đồng boss chính ở progress 540; chưa có boss AI hoặc boss spawn. Asset Lich Lord đã tồn tại trong catalog cũ được giữ nguyên.
- FX/status/loot/UI, bestiary, palette, scene/preview: khảo sát và ghi kế hoạch; không copy nguyên pack vào runtime. Không thêm hit-FX mẫu tùy chọn.
- N3XUS: không đưa artwork vào coding agent/pipeline; chỉ đọc điều khoản dạng text. Tiny Questers Slime/Bat không bị thay thế hoặc đóng gói lại.

License MonstersCreatures cho phép tích hợp/chỉnh sửa trong project, cấm phân phối asset độc lập và dùng làm dữ liệu huấn luyện AI. SpriteCanvas là CC0; bốn license đã được đối chiếu. Bản license cần thiết nằm trong runtime licenses; source gốc không thay đổi.

## Pipeline và animation

Chạy lại bằng `python tools/prepare_enemy_assets.py` với JDK và libGDX đã có trong Gradle cache. `tools/enemy_balance.json` là nơi chỉnh gameplay; các chỉnh sửa tuning được giữ khi regenerate. Pipeline đọc metadata/PNG, tạo import plan, kiểm tra pixel bằng Java ImageIO, đóng gói atlas và sinh animation catalog cùng dữ liệu registry Java. `--audit-only` kiểm tra nguồn mà không ghi atlas hoặc generated Java.

- 960 strip MonstersCreatures được so với sheet, tổng 1.631.232 pixel khớp.
- 8.732 frame reference được kiểm tra; 4.608 cell runtime sau deduplicate; 402 LEFT clip được chứng minh mirror chính xác của RIGHT. Không có frame reference rỗng.
- 391 PNG FX/bestiary được decode thành công.
- Một atlas mới 2048×2048, extrusion hai pixel; 3.000.448 phép kiểm tra pixel/cell/padding. Catalog mới khoảng 533 KB, PNG mới khoảng 800 KB trên đĩa; dung lượng RGBA không phụ thuộc kích thước PNG nén.
- AssetManager load texture một lần trước gameplay, Nearest/ClampToEdge, dispose theo GameAssets. Clip/region/Animation được tạo lúc load; không tạo texture hoặc Animation theo entity/frame.
- MC giữ cell nguồn 16×16, 16×32 hoặc 32×32. Scale lần lượt 1,0 / 0,85 / 1,0, pivot `(W/2,H−2)` ổn định cho mọi clip.
- SpriteCanvas được chuẩn hóa offline bằng Nearest, đồng nhất X/Y theo cạnh dài 32 pixel (Golem 48); pivot dùng cùng phép biến đổi, runtime scale 0,75. Không sửa sprite nguồn. Cách này giảm bộ nhớ đáng kể nhưng chi tiết nhỏ của bản độ phân giải cao có thể mất.

| Nguồn | Idle | Walk | Attack | Hurt | Death | Special / hướng |
| --- | --- | --- | --- | --- | --- | --- |
| MonstersCreatures, cả recolor | 4f, 8 FPS, loop | 4f, 10 FPS, loop | 5f, 12 FPS, once | 3f, 12 FPS, once | 6f, 10 FPS, once | 5f, 12 FPS, once; bốn hướng |
| SC Goblin | 21f × 200ms | 20f × 83ms | 26f × 62ms | 10f × 56ms | 19f × 71ms | Side view + mirror/fallback |
| SC Golem | 21f × 200ms | 20f × 100ms | 28f × 67ms | 8f × 67ms | 24f × 67ms | Side view + mirror/fallback |
| SC Mushroom | 21f × 200ms | 12f × 83ms | 19f × 62ms | 11f × 56ms | 17f × 62ms | Side view + mirror/fallback |
| SC Slime | 35f × 125ms | 12f × 83ms | 21f × 62ms | 8f × 62ms | 21f × 71ms | Side view + mirror/fallback |
| Tiny/Willosuna hiện có | Giữ catalog cũ | Giữ catalog cũ | Giữ catalog cũ | Giữ catalog cũ | Giữ catalog cũ | Đã kiểm tra đủ năm trạng thái/bốn hướng |

README MC chỉ gợi ý Idle/Walk 8–10 FPS và Attack 12 FPS; các tốc độ còn lại là chính sách importer được ghi rõ. SpriteCanvas dùng duration millisecond thật trong JSON. Idle/Walk loop, Attack/Hurt/Death không loop. SPECIAL được giữ cho task sau; tên special chưa phải kỹ năng đã triển khai. Attack hit frame 2 trong source được ghi lại trong tài liệu, chưa nối thành damage event mới; contact damage tiếp tục dùng overlap hiện tại.

## Kiến trúc và cân bằng

`EnemyDefinition` có ID, sprite, scale/pivot, HP/DMG Easy–Hard, speed, footprint, archetype, unlock, weights, flying, family cap và tên special tương lai. `EnemyRegistry.all()` là API toàn roster. `EnemyType` chỉ giữ adapter tương thích ba loại cũ. Dữ liệu registry được sinh thành Java thuần, an toàn khi Android tạo GameConfig trước GL/Gdx.files.

| Archetype | Số ID | Hành vi thật trong Task 04A |
| --- | ---: | --- |
| CHASER | 17 | Chase/LOS, A* khi bị chặn, contact |
| FAST_MELEE | 14 | Chase nhanh; chỉ Wild Boar giữ charge đã có |
| TANK | 20 | HP cao hơn, chậm; tank mạnh mở muộn |
| FLYING | 17 | Vượt WATER; cây/đá/biên vẫn chặn |
| NIGHT | 5 | Mở từ progress 300, tăng ưu tiên ban đêm |
| RANGED_CANDIDATE | 8 | Chase/contact an toàn, tên projectile/special dành cho 04B |
| SPECIAL_CANDIDATE | 1 | Chase/contact an toàn, chưa có leap/special mới |

AI dùng chung EnemySystem, collision footprint từng định nghĩa, separation và budget tối đa hai A* mỗi frame. Không quét toàn map để update mỗi enemy. Sword contact radius và swept projectile dùng footprint của định nghĩa; damage/death vẫn qua `Enemy.damage` và death hook cũ. Sau Death không chạy chase/contact. Session end/Retry xóa residents, cached path và death listener cũ.

HP cơ bản Easy 1–7, Hard 1–10; DMG Easy 1, Hard 1–2; speed cơ bản 18–52. Hard giữ hệ số speed 1,08; các stage HP increment nhỏ hiện có được tái dùng, không copy HP 14–60 của source vào gameplay. Bảy lựa chọn ban đầu có HP Easy 1–2 để farm bằng kiếm cơ bản; tank mạnh/elite mở muộn. Không đổi WeaponConfig, cấp vũ khí, rewards, pickup TTL/probability, hoặc modal Weapon Box.

### Toàn bộ ID và tuning cơ bản

HP/DMG là Easy/Hard trước stage bonus; speed trước hệ số Hard. Weight là trọng số tương đối, không phải phần trăm. Chi tiết scale/pivot/footprint, hard/night multiplier, cap và special của từng ID nằm trong [ENEMY_ROSTER.md](ENEMY_ROSTER.md).

| ID | Archetype | HP E/H | DMG E/H | Speed | Unlock s | Weight |
| --- | --- | --- | --- | ---: | ---: | ---: |
| `tiny_slime` | CHASER | 1/1 | 1/1 | 28 | 0 | 3.000 |
| `tiny_bat` | FLYING | 1/2 | 1/1 | 52 | 0 | 2.500 |
| `honey_slime` | CHASER | 1/2 | 1/1 | 26 | 0 | 1.400 |
| `wild_boar` | FAST_MELEE | 2/3 | 1/1 | 38 | 0 | 2.000 |
| `giant_bee` | FLYING | 1/2 | 1/1 | 46 | 90 | 0.900 |
| `red_cap` | CHASER | 2/3 | 1/1 | 28 | 120 | 0.800 |
| `red_crab` | TANK | 4/5 | 1/1 | 22 | 240 | 0.600 |
| `giant_frog` | SPECIAL_CANDIDATE | 3/4 | 1/1 | 32 | 240 | 0.700 |
| `rock_golem` | TANK | 6/8 | 1/1 | 18 | 420 | 0.500 |
| `living_armor` | TANK | 5/7 | 1/1 | 23 | 420 | 0.500 |
| `bone_hound` | FAST_MELEE | 3/4 | 1/1 | 48 | 240 | 0.700 |
| `mc_bat_swarm` | FLYING | 1/2 | 1/1 | 45 | 120 | 0.900 |
| `mc_bone_hound` | FAST_MELEE | 3/4 | 1/1 | 42 | 240 | 0.700 |
| `mc_crystal_cube` | TANK | 3/4 | 1/1 | 22 | 120 | 0.800 |
| `mc_crystal_cube_ember` | TANK | 4/6 | 1/1 | 22 | 420 | 0.144 |
| `mc_crystal_cube_toxic` | TANK | 4/6 | 1/1 | 22 | 420 | 0.144 |
| `mc_gargoyle` | FLYING | 3/4 | 1/1 | 35 | 180 | 0.600 |
| `mc_kobold_miner` | CHASER | 2/3 | 1/1 | 30 | 120 | 0.900 |
| `mc_kobold_miner_blue` | CHASER | 3/5 | 1/1 | 30 | 420 | 0.162 |
| `mc_living_armor` | TANK | 5/7 | 1/2 | 23 | 420 | 0.500 |
| `mc_living_armor_dark` | TANK | 6/9 | 1/2 | 23 | 480 | 0.090 |
| `mc_living_armor_gold` | TANK | 6/9 | 1/2 | 23 | 480 | 0.090 |
| `mc_mimic_jar` | RANGED_CANDIDATE | 3/4 | 1/1 | 27 | 150 | 0.700 |
| `mc_mimic_jar_gold` | RANGED_CANDIDATE | 4/6 | 1/1 | 27 | 420 | 0.126 |
| `mc_rock_crab` | TANK | 4/5 | 1/1 | 20 | 180 | 0.600 |
| `mc_rock_crab_crystal` | TANK | 5/7 | 1/1 | 20 | 420 | 0.108 |
| `mc_roller_bug` | CHASER | 2/3 | 1/1 | 32 | 150 | 0.800 |
| `mc_roller_bug_lava` | CHASER | 3/5 | 1/1 | 32 | 420 | 0.144 |
| `mc_crab_knight` | TANK | 4/5 | 1/1 | 26 | 285 | 0.600 |
| `mc_crab_knight_royal` | TANK | 5/7 | 1/1 | 26 | 420 | 0.108 |
| `mc_jelly` | FLYING | 2/3 | 1/1 | 30 | 240 | 0.700 |
| `mc_jelly_sea_glass` | FLYING | 3/5 | 1/1 | 30 | 420 | 0.126 |
| `mc_pufferfish` | CHASER | 2/3 | 1/1 | 28 | 250 | 0.700 |
| `mc_antlion` | CHASER | 3/4 | 1/1 | 25 | 240 | 0.700 |
| `mc_dust_devil` | FLYING | 2/3 | 1/1 | 40 | 240 | 0.600 |
| `mc_dust_devil_ash` | FLYING | 3/5 | 1/1 | 40 | 420 | 0.108 |
| `mc_dust_devil_snow` | FLYING | 3/5 | 1/1 | 40 | 420 | 0.108 |
| `mc_frill_lizard` | FAST_MELEE | 2/3 | 1/1 | 42 | 210 | 0.800 |
| `mc_mummy_cat` | FAST_MELEE | 2/3 | 1/1 | 42 | 180 | 0.800 |
| `mc_mummy_cat_shadow` | FAST_MELEE | 3/5 | 1/1 | 42 | 420 | 0.144 |
| `mc_flytrap_walker` | RANGED_CANDIDATE | 3/4 | 1/1 | 23 | 150 | 0.700 |
| `mc_flytrap_walker_fire` | RANGED_CANDIDATE | 4/6 | 1/1 | 23 | 420 | 0.126 |
| `mc_jackalope` | FAST_MELEE | 1/2 | 1/1 | 40 | 60 | 1.000 |
| `mc_jackalope_snow` | FAST_MELEE | 2/4 | 1/1 | 40 | 420 | 0.180 |
| `mc_moss_golem` | TANK | 6/8 | 1/2 | 18 | 420 | 0.500 |
| `mc_moss_golem_autumn` | TANK | 7/10 | 1/2 | 18 | 480 | 0.090 |
| `mc_mushroom_folk` | CHASER | 1/2 | 1/1 | 28 | 0 | 1.200 |
| `mc_mushroom_folk_toxic` | CHASER | 2/4 | 1/1 | 28 | 420 | 0.216 |
| `mc_owlbear_cub` | CHASER | 2/3 | 1/1 | 28 | 90 | 0.800 |
| `mc_thorn_sprite` | FLYING | 1/1 | 1/1 | 40 | 40 | 1.000 |
| `mc_pumpkin_head` | NIGHT | 4/5 | 1/1 | 28 | 300 | 0.800 |
| `mc_pumpkin_head_winter` | NIGHT | 5/7 | 1/1 | 28 | 420 | 0.144 |
| `mc_shadow_cat` | NIGHT | 2/3 | 1/1 | 45 | 300 | 0.800 |
| `mc_wraith` | NIGHT | 3/4 | 1/1 | 34 | 330 | 0.700 |
| `mc_wraith_ember` | NIGHT | 4/6 | 1/1 | 34 | 420 | 0.126 |
| `mc_bumble_guard` | FLYING | 1/2 | 1/1 | 42 | 60 | 0.900 |
| `mc_cyclops` | TANK | 6/8 | 1/2 | 18 | 450 | 0.400 |
| `mc_goblin_bomber` | RANGED_CANDIDATE | 2/3 | 1/1 | 30 | 150 | 0.700 |
| `mc_ratfolk_thief` | FAST_MELEE | 2/3 | 1/1 | 42 | 110 | 0.900 |
| `mc_frost_fox` | FAST_MELEE | 3/4 | 1/1 | 44 | 300 | 0.700 |
| `mc_frost_fox_arctic_night` | FAST_MELEE | 4/6 | 1/1 | 44 | 420 | 0.126 |
| `mc_frost_moth` | FLYING | 2/3 | 1/1 | 36 | 360 | 0.600 |
| `mc_frost_moth_luna` | FLYING | 3/5 | 1/1 | 36 | 420 | 0.108 |
| `mc_yeti_cub` | RANGED_CANDIDATE | 3/4 | 1/1 | 28 | 330 | 0.700 |
| `mc_bog_lurker` | CHASER | 3/4 | 1/1 | 24 | 240 | 0.800 |
| `mc_giant_mosquito` | FLYING | 1/2 | 1/1 | 45 | 240 | 0.700 |
| `mc_giant_mosquito_plague` | FLYING | 2/4 | 1/1 | 45 | 420 | 0.126 |
| `mc_lily_snapper` | TANK | 4/5 | 1/1 | 20 | 270 | 0.600 |
| `mc_lily_snapper_desert` | TANK | 5/7 | 1/1 | 20 | 420 | 0.108 |
| `mc_lizardfolk` | CHASER | 3/4 | 1/1 | 30 | 260 | 0.800 |
| `mc_lizardfolk_desert` | CHASER | 4/6 | 1/1 | 30 | 420 | 0.144 |
| `mc_swamp_hag` | RANGED_CANDIDATE | 4/5 | 1/1 | 23 | 285 | 0.600 |
| `mc_swamp_hag_frost` | RANGED_CANDIDATE | 5/7 | 1/1 | 23 | 420 | 0.108 |
| `mc_ash_hound` | FAST_MELEE | 5/6 | 1/1 | 44 | 480 | 0.400 |
| `mc_ash_hound_frost` | FAST_MELEE | 6/8 | 1/1 | 44 | 540 | 0.072 |
| `mc_flame_skull` | FLYING | 3/4 | 1/1 | 42 | 450 | 0.600 |
| `mc_flame_skull_soul` | FLYING | 4/6 | 1/1 | 42 | 510 | 0.108 |
| `mc_magma_slug` | TANK | 5/7 | 1/2 | 20 | 420 | 0.600 |
| `sc_goblin` | FAST_MELEE | 2/3 | 1/1 | 34 | 60 | 0.800 |
| `sc_golem` | TANK | 6/8 | 1/1 | 18 | 420 | 0.500 |
| `sc_mushroom` | CHASER | 2/3 | 1/1 | 28 | 0 | 0.800 |
| `sc_slime` | CHASER | 1/2 | 1/1 | 28 | 0 | 0.800 |

### Spawn progression

| Progress giây | Pool / điều kiện |
| --- | --- |
| 0–120 | Tiny Slime/Bat, Wild Boar, Honey Slime, MC Mushroom Folk, SC Mushroom/Slime ngay đầu; mở thêm Forest/Plains/Goblin yếu theo các mốc 40–110 |
| 120–240 | Bổ sung Cave/Dungeon, Desert và flying phù hợp |
| 240–300 | Coast/Swamp, chỉ số nhỉnh hơn |
| 300–420 | Night/Snow, Night weight tăng; biome theme ngẫu nhiên ngắn |
| 420–540 | Tank lớn, Volcanic, recolor hiếm |
| 540–600 | Tiếp tục toàn pool đã mở; boss ID `lich_lord` là hợp đồng tương lai |
| ≥600 | Dừng spawn tự nhiên; quái còn sống vẫn hoạt động, không tự tuyên bố Victory |

Mỗi seed tạo biến thiên weight ×0,80–1,25. Hard và Night nhân hệ số riêng. Sau hai lần chọn cùng loại, giảm weight lần tiếp theo ×0,2. Giới hạn mỗi loài và mỗi family (base + recolor) không quá ceil(25% cap resident). Theme 18 progress giây sau mốc 300 nhân weight biome ×1,65; không vượt unlock, không tăng cap/cadence, không thêm burst.

Placement dùng 64 thử ngẫu nhiên và tối đa 256 cell fallback trong vùng quanh camera. Kiểm tra connectivity, vật cản, khoảng cách player/viewport và residents; tránh fallback về góc xa của map 200×200. Random cùng seed tái hiện được các lựa chọn; seed mới cho thành phần khác. Normal/Demo dùng cùng progress; Demo không tăng movement/animation/cooldown. Mọi ID có weight dương trước 600, nhưng một trận ngẫu nhiên không được bảo đảm gặp đủ 82 ID.

## Kiểm thử đã chạy

| Kiểm tra | Kết quả và phạm vi |
| --- | --- |
| Compile core + Android Java | Qua; không chạy assemble/bundle/export như tác vụ riêng |
| EnemyRosterChecks | 124.277 check qua: 82 ID × 4 vũ khí × 2 mode = 656 ca kill thật, movement/contact/actions, navigation, weighted progression và cap |
| EnemyAssetSmoke / OpenGL | 19.641 check qua; 8.535 frame draw cho năm trạng thái, đủ 82 sprite; rect/UV, Nearest, pivot, loop và GL error |
| EnemyRosterSmoke / GameScreen | 346 check qua: render/combat đủ 82 ID, Weapon Box modal/Pause/lifecycle, Game Over, ba vòng Retry và Home |
| WeaponChecks / WeaponSmoke | 223 / 45 check qua |
| PickupChecks / PickupSmoke | 21.438 / 27 check qua |
| CameraChecks / Task021Checks phase a | 44.781 / 17 check qua; không chạy lại stress suite AI/map cũ |
| WorldNightSmoke / PickupRefinementProbe | 17 / 354 check qua |
| Android native smoke | 13 check qua trên bản Studio Run cuối; chi tiết bên dưới |

Mỗi dòng trong [per-type-checks.json](core/build/reports/task04a/per-type-checks.json) ghi tám ca weapon/mode và kết quả chase/Walk/contact/Attack/Hurt/Death, single kill/drop cho chính ID đó. Fixture đặt HP/drop probability có kiểm soát để xác minh đường thực thi và chống double kill/drop, không dùng để kết luận cân bằng của trận mặc định. Kiểm tra map nhỏ có cây/nước cho từng ID (300 frame/fixture), flying chỉ vượt nước, vẫn tuân thủ budget A*. Kiểm tra spawner thực trên map 200×200 seed 42 đạt cap Easy 40/Hard 60, đếm cả corpse, vị trí hợp lệ/offscreen và trong vùng camera.

Seed 42 lặp lại chuỗi weighted pick, seed 43 khác; unlock/Night 300/stop 600 và progress Normal/Demo đã kiểm tra độc lập. Phép sampling có chống dominance và xác nhận mọi ID có weight eligible dương trước 600; không dùng số loại nhìn thấy trong một sample làm tổng pool.

Qua ba Retry trong GameScreen, game-owned texture luôn 19, RGBA estimate luôn 44.285.956 byte và dùng lại cùng GameAssets; session/path/residents cũ được dọn. Đây là kiểm tra vòng đời và tài nguyên texture, chưa là phép chứng minh toàn bộ Java/native heap không rò rỉ dài hạn.

Ảnh [roster-new-assets.png](core/build/reports/task04a/roster-new-assets.png) chỉ chứa 40 MC base và bốn SC để kiểm tra trình bày nguồn mới; không đọc lại artwork Tiny Questers/N3XUS vào agent. Hai ảnh pickup ngày/đêm cùng điều kiện của Task 03B.2 được render lại: PNG SHA-256 trùng hoàn toàn, giúp xác nhận pickup visual không bị đổi. Kiểm tra cell extrusion/UV và frame draw không phát hiện texture bleeding hoặc missing asset.

## Texture và hiệu năng Android

GameAssets có **19 texture**, 44.285.956 byte ≈ **42,23 MiB RGBA**. Atlas mới đóng góp 16 MiB; phần cũ khoảng 26,23 MiB. Không tạo texture riêng cho mỗi actor. Ước tính này là `width × height × 4` của texture do GameAssets quản lý, không phải PSS/Java heap; không gồm font texture, driver overhead hoặc bản sao phục hồi context. 71 sprite set nguồn mới chia sẻ một atlas, không cần lazy load/unload trong lúc trận chạy.

Thiết bị: Pixel_4 AVD `emulator-5554`, API 37, renderer `Android Emulator OpenGL ES Translator (Google SwiftShader)`, landscape 2280×1080. Đo độc lập với build và GL tests. Map 200×200 seed 42, zoom 1,15; ba cặp population 10/30/60 với cùng thành phần day/night trong từng cặp, mỗi enemy khác ID. Sword 1 + Fireball 5 + Lightning 5; HP chẩn đoán để giữ population, lighting ép riêng. Mỗi stage 30 frame warm-up + 60 frame đo, tổng 360 sample. Đây là probe ngắn, không phải trận 10 phút mặc định.

| Resident / lighting | FPS callback | Mean frame ms | p95 ms | CPU AI+A*+separation ms | CPU update* ms | CPU world render ms | CPU Night ms | World draw calls TB |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 10 / day | 25,13 | 39,79 | 54,75 | 0,69 | 1,79 | 16,77 | 0,02 | 3,43 |
| 10 / night | 22,23 | 44,98 | 67,49 | 0,50 | 1,17 | 14,79 | 3,75 | 4,58 |
| 30 / day | 24,19 | 41,34 | 54,92 | 1,86 | 3,24 | 14,66 | 0,02 | 3,85 |
| 30 / night | 21,31 | 46,92 | 70,73 | 1,39 | 3,10 | 14,29 | 3,61 | 4,83 |
| 60 / day | 22,25 | 44,95 | 63,31 | 2,23 | 4,44 | 18,94 | 0,04 | 17,73 |
| 60 / night | 21,57 | 46,37 | 63,21 | 1,82 | 3,74 | 17,85 | 1,78 | 20,03 |

*CPU update là tổng phần được instrument PLAYER/AI/ASTAR/SEPARATION/COLLISION/COMBAT/SPAWN/PICKUPS/PROJECTILES, không gồm ANIMATION vì cột đó bao gồm cả chuẩn bị sprite trong render. Animation riêng khoảng 0,26–1,41 ms. CPU AI không gồm collision/animation; world render không gồm Night, weapon render và HUD. Đây là thời gian CPU submit/callback, không phải GPU timer; các cột không cộng thành toàn bộ frame interval.*

Cả sáu stage giữ đúng 10/30/60 living và distinct ID, 19 texture. Tối đa ba projectile/bốn effect trong mẫu này. A* search đo được bằng 0 vì các vị trí probe chủ yếu có LOS; budget/đường vòng được kiểm tra bằng fixture riêng, không tuyên bố đây là benchmark A* nặng.

World render submit/SwiftShader và pacing emulator là phần lớn chi phí nhìn thấy; nhiều texture cũ xen với atlas mới làm draw calls tăng ở 60 loại. Chưa có baseline trước/sau cùng roster để định lượng regression do riêng task. Mẫu ngắn và thứ tự stage tuần tự có nhiễu; không suy ra lighting tối luôn nhanh/chậm một tỷ lệ cố định. Chưa đo thiết bị Android vật lý hoặc GPU hardware.

Bằng chứng: [CSV](core/build/reports/task04a/android-roster.csv), [summary](core/build/reports/task04a/android-roster-summary.json), [CPU detail](core/build/reports/task04a/android-roster-cpu-detail.json), [environment](core/build/reports/task04a/android-roster-environment.txt). Tool `tools/profile_roster_android.py` khôi phục/xóa flag profile và map-seed sau đo, force-stop gameplay và đưa app về Native Home.

## Android Studio Run

Đã thao tác **Run (Shift+F10)** cấu hình `android` trong Android Studio trên Pixel_4; lần deployment cuối 23:52:42 ngày 13/11/2026 (Asia/Saigon), sau thay đổi runtime cuối cùng. IDE kích hoạt `HomeActivity`; runtime log xác nhận `roster=82 textures=19 rgbaBytes=44285956`. Không chạy release/signing/publishing, không assemble/bundle/export như tác vụ riêng, không cài thủ công hay tạo APK/AAB bàn giao.

Kiểm tra native menu Easy/Hard × Normal/Demo, Play → gameplay → Pause → Home trên cả bốn tổ hợp. Easy/Demo seed 42 chơi tự nhiên hơn mười giây thực: thấy `mc_mushroom_folk`, `sc_mushroom`, `sc_slime`, `tiny_bat`; kiếm giết quái, Pause giữ nguyên elapsed, Resume và joystick thật di chuyển, zoom 1,15/range kiếm 48 giữ nguyên. Ba tổ hợp còn lại dùng seed ngẫu nhiên và xác nhận load/render/Pause/Home. Không phát hiện Android crash hoặc missing asset trong log PID của từng lượt.

Android smoke không tiêm enemy/HP. Probe hiệu năng riêng có cấu hình chẩn đoán như đã mô tả. Game Over/Retry và weapon modal được kiểm tra trên GameScreen desktop trong task này; không gán kết quả đó thành kiểm tra touch/Game Over/Retry Android mới. Không chạy lại trận mười phút hoặc stress suite cũ.

Bằng chứng: [studio-run.log](core/build/reports/task04a/studio-run.log), [studio-deploy.txt](core/build/reports/task04a/studio-deploy.txt), [android-roster-smoke.json](core/build/reports/task04a/android-roster-smoke.json), [logcat](core/build/reports/task04a/android-roster-logcat.txt).

## File thay đổi và bảo toàn phạm vi

Các đường dẫn Java bên dưới tương đối với `core/src/main/java/com/example/monstersurvivor/`:

- Mới: `enemy/EnemyArchetype.java`, `EnemyDefinition.java`, `EnemyRegistry.java`, generated `EnemyCatalogData.java`; `spawn/EnemySpawnProfile.java`.
- Sửa: `entity/Enemy.java`, `EnemyType.java`; `ai/EnemySystem.java`; `spawn/EnemySpawner.java`; `config/GameConfig.java`; `assets/GameAssets.java`.
- Kết nối combat/lifecycle/chẩn đoán: `combat/SwordCombat.java`, `weapon/ProjectileSystem.java`, `gameplay/GameSession.java`, `screen/GameScreen.java`, `debug/CombatProfile.java`.
- Runtime mới: `assets/game/enemies-atlas-0.png`, `enemy-catalog.json`, `licenses/MonstersCreatures.txt`, `licenses/SpriteCanvas-CC0.txt`; cập nhật `assets/assets.txt` cho danh sách asset.
- Pipeline: `tools/prepare_enemy_assets.py`, `PrepareEnemyAtlas.java`, `enemy_balance.json`, `write_enemy_reports.py`.
- Kiểm tra: `tools/checks/EnemyRosterChecks.java`, `tools/smoke/EnemyAssetSmoke.java`, `EnemyRosterSmoke.java`, `tools/run_task04a_checks.py`, `run_studio_task04a.ps1`, `android_roster_smoke.py`, `profile_roster_android.py`. Các fixture cũ liên quan chỉ được cập nhật expected texture count 18→19, truy cập definition và đường dẫn output để không ghi đè bằng chứng cũ.
- Tài liệu: bốn tài liệu Task 04A và phần bổ sung mới trong `INTEGRATION_NOTES.md`. Log/generated proof nằm trong `core/build/reports/task04a/`.

SHA-256 baseline 10.453 file được đối chiếu: source asset, PNG/catalog cũ, GDD, báo cáo cũ, Android source/XML/Theme/Manifest, Gradle, map generator, PixelCamera, NightRenderer và PickupRenderer không đổi. Không có file baseline mất. Danh sách thay đổi cũ chính xác tại [final-scope-audit.json](core/build/reports/task04a/final-scope-audit.json); `INTEGRATION_NOTES.md` chỉ bổ sung hợp đồng mới. Không commit/push.

## Giới hạn còn lại và đề xuất 04B

- Cân bằng được giữ nhẹ đầu trận bằng config và kiểm thử cơ chế; vẫn cần người chơi đánh giá một trận mặc định đầy đủ. Roster ngẫu nhiên không bảo đảm gặp mọi recolor hiếm trong một trận.
- Ranged/Special chỉ chase/contact, có metadata/clip chuẩn bị sẵn. Chưa có projectile của enemy, status gameplay, teleport, split, summon, slam hoặc boss AI. Không diễn giải animation Special là kỹ năng đã xong.
- SpriteCanvas dùng fallback side view và giảm độ phân giải; nên review mỹ thuật trên thiết bị thật. Các footprint tank vẫn đủ nhỏ để đi trên navigation 16 pixel, không phải collider theo toàn ảnh.
- FPS SwiftShader chưa đạt 60; draw calls và CPU render còn đáng kể. Texture estimate không thay thế heap/PSS dài hạn. Ba vòng Retry giữ texture ổn định, chưa chứng minh không có mọi loại memory leak.
- Task 04B nên ưu tiên contract attack event/telegraph/owner-team và projectile pool dùng chung cho một vài ranged candidate; định nghĩa cooldown, pause, LOS, single-hit/death guard trước khi triển khai special theo family. Giữ cap và A* budget, đo lại các trường hợp có vật cản, rồi mới mở rộng. Boss Lich Lord vẫn là task riêng theo GDD; FX đầy đủ theo kế hoạch 04C. Không triển khai 04B/04C trong task này.
