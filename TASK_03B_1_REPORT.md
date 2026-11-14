# TASK 03B.1 — Weapon rebalance, presentation, night và world expansion

Triển khai theo A → G trên Java/libGDX hiện có. Không triển khai Evolution hay các tính năng Task 03C. Bằng chứng kiểm tra cục bộ nằm trong `core/build/reports/task03b_1/`.

## 1. File thay đổi

Core sửa: `config/WeaponConfig.java`, `config/GameConfig.java`, `weapon/WeaponSystem.java`, `weapon/ProjectileSystem.java`, `weapon/WeaponRenderer.java`, `weapon/WeaponOffer.java`, `gameplay/GameSession.java`, `screen/GameScreen.java`, `pickup/PickupType.java`, `pickup/PickupRenderer.java`, `world/ForestGenerator.java`, `world/ForestMap.java`, `assets/GameAssets.java`, `debug/FrameMetrics.java`, `debug/CombatProfile.java`.

Core mới: `config/WorldVisualConfig.java`, `gameplay/NightState.java`, `render/NightRenderer.java`. Các đường dẫn core trên nằm dưới `core/src/main/java/com/example/monstersurvivor/`.

Asset mới: `assets/game/dungeon-pickups.png/json`, `world-expanded.png/json`, `PRESENTATION_SOURCES.md`, `licenses/Kenney-Tiny-Dungeon.txt`. `assets/assets.txt` được tác vụ sẵn có của Gradle cập nhật.

Tool mới: `tools/PreparePresentationAssets.java`, `run_task03b1_checks.py`, `checks/Task03B1Checks.java`, `smoke/PresentationProbe.java`, `smoke/WorldNightSmoke.java`, `smoke/ZoomTerrainProbe.java`, `smoke/WeaponVfxProbe.java`, `run_studio_task03b1.ps1`, `android_task03b1_smoke.py`, `android_world_flow_smoke.py`, `profile_world_night_android.py`.

Fixture/tool hồi quy cập nhật: `checks/PickupChecks.java`, `checks/PickupRoute.java`, `checks/Task021Checks.java`, `smoke/PickupSmoke.java`, `smoke/WeaponSmoke.java` dưới `tools/`. Thêm `ENEMY_EXPANSION_NOTES.md`, báo cáo này và phần cập nhật trong `INTEGRATION_NOTES.md`.

Audit SHA-256 so với snapshot trước task: 8.860 file được bảo vệ; chỉ `assets/assets.txt` thay đổi. Android native, Gradle, GDD, báo cáo cũ, source asset và các PNG/catalog cũ giữ nguyên. Không commit/push.

## 2. WeaponConfig trước/sau

Trước: Fire chỉ một projectile ở cả năm cấp; Lightning chỉ một tia đầu tiên, số `targets` biểu diễn cả chuỗi. Bảng cũ giảm cooldown tuyến tính: Fire 3.00/2.85/2.70/2.55/2.40; Lightning 4.00/3.80/3.60/3.40/3.20 giây.

Sau: `Level` bổ sung `projectileCount`, `initialBolts`, `visualScale`, `glow`, `boltWidth`, `fanDegrees`. Constructor chín tham số cũ còn tương thích. Các giá trị có validation, immutable và được snapshot vào projectile/effect khi phát đòn. Sword/Bow giữ nguyên bảng tuning và hành vi cũ.

Không có nhánh kiểm tra level trong combat/renderer. Runtime đọc tuning; nâng cấp clamp cooldown đang chạy xuống cooldown mới, không reset để bắn liên tục. Cooldown dùng delta gameplay thực; Demo chỉ nhân đồng hồ tiến trình.

## 3. Damage, cooldown và multishot từng cấp

Damage = `round(currentPlayerDamage × multiplier + bonus)`, tối thiểu 1. Cột damage dưới dùng base damage = 1; buff Damage Box vẫn được đọc khi phát đòn.

| Fire | Multiplier + bonus | Damage | CD thực | Projectile | AoE radius |
| --- | --- | --- | --- | --- | --- |
| 1 | 1.50 + 0 | 2 | 3.0 s | 1 | 35 |
| 2 | 1.50 + 0 | 2 | 3.0 s | 2 | 38 |
| 3 | 2.50 + 1 | 4 | 3.0 s | 2 | 41 |
| 4 | 2.75 + 1 | 4 | 3.0 s | 3 | 44 |
| 5 | 3.50 + 1 | 5 | 2.1 s | 3 | 47 |

Fire ưu tiên các mục tiêu khác nhau, đồng thời chia các lane theo góc fan 12°. Với một quái vẫn có các projectile và đường bay khác nhau. Đạn nổ khi va quái/tường, tới khoảng cách ngắm hoặc hết lifetime. Việc nổ ở khoảng cách ngắm giúp các lane lệch vẫn gây AoE lên quái nhỏ. Mỗi đạn chỉ nổ một lần; mỗi quái chỉ được xét một lần trong mỗi vụ nổ, có LOS. Nhiều đạn độc lập có thể gây nhiều hit hợp lệ.

| Lightning | Multiplier + bonus | Damage/hit | CD thực | Tia đầu tiên tối đa | Tổng mục tiêu/activation |
| --- | --- | --- | --- | --- | --- |
| 1 | 1.00 + 0 | 1 | 4.0 s | 1 | 2 |
| 2 | 1.00 + 0 | 1 | 3.4 s | 1 | 2 |
| 3 | 2.00 + 1 | 3 | 3.4 s | 1 | 3 |
| 4 | 2.50 + 1 | 4 | 2.7 s | 1 | 3 |
| 5 | 3.50 + 1 | 5 | 2.7 s | 2 | 4 |

Cấp 5 giữ chỗ hai primary khác nhau trước khi chain, rồi phát hai tia trực tiếp từ player trong cùng update. Một visited set và budget chung gồm cả primary ngăn chain đánh trùng. Nếu chỉ có một quái hợp lệ thì chỉ phát một tia đầu tiên, không nhân đôi damage. Death/drop tiếp tục đi qua `Enemy.damage` và death listener hiện có.

Projectile cap 48, effect cap 48; pool được cấp trước. Fire chỉ phát volley khi còn đủ chỗ cho cả loạt. Thiếu target/cap dùng retry 0.12 s; không cấp thêm texture hay particle theo frame.

## 4. VFX theo level

Fire có scale 1.00/1.12/1.24/1.36/1.48 và glow 1.00/1.25/1.50/1.75/2.00. Cấp 2 nhận hai core/đường bay; cấp 3 core và tâm nổ lớn/sáng hơn cùng damage tăng; cấp 4 có ba đường; cấp 5 giữ ba đường nhưng nhịp nhanh và vụ nổ sáng hơn. AoE vật lý vẫn do radius riêng quyết định.

Lightning cấp 2 glow tăng và nhịp nhanh; cấp 3 độ dày từ 2 lên 2.5 world unit (+25%), impact sáng/lớn hơn; cấp 4 glow tăng và CD 2.7 s; cấp 5 có hai tia đầu tiên. Glow, zigzag và impact dùng các hình chữ nhật trên một texel trắng dùng chung. Không shader tùy chỉnh/FBO/particle engine. VFX vũ khí vẽ sau lớp đêm nên vẫn nổi bật.

## 5. Night Mode

`GameSession.progress() >= 300`: Normal 300 giây gameplay thực; Demo 45 giây gameplay thực với hệ số 600/90. Đồng hồ nội bộ dùng double để tránh tích lũy sai số float ở ngưỡng; API vẫn trả float. Không đọc giờ hệ điều hành.

`NightState` fade trong khoảng 3 giây gameplay thực. Pause, lifecycle, modal, death/end không tiến fade/progress. Retry tạo NightState mới ở ban ngày.

Lớp phủ xanh tím alpha tối đa 0.68, vùng sáng quanh player radius 120 world unit, tâm vẫn giữ khoảng 94% độ sáng. Ngoài vùng sáng còn nhìn được môi trường. Renderer chỉ phủ viewport bằng lưới vertex-color 20×12 quad, một pass SpriteBatch, không duyệt map 40.000 ô, không full-map FBO hay shader GLES riêng.

Thứ tự: world → night → weapon VFX → HUD/modal/joystick/pause/indicator. UI dùng viewport cũ và giữ độ sáng. Lighting không đổi collision, AI hay lịch spawn. GL Desktop và bản Android trên SwiftShader đã thực thi pass này không có lỗi shader/missing asset.

## 6. Zoom trước/sau

Trước: minimum world viewport 320×240, zoom 1.00. Sau: 320/1.15 × 240/1.15, mặc định gần hơn 15%; Android landscape 2280×1080 nhìn khoảng 440.58×208.70 world unit thay cho 506.67×240.

Đã thử 1.10/1.15/1.20 qua kiểm tra physical pixel anchor, clamp và framebuffer terrain. Chọn 1.15 để tăng độ đọc mà giữ khoảng nhìn. Có thể đổi bằng `GameConfig.Builder.visuals(new WorldVisualConfig(zoom))`; không thêm Settings native.

Không scale actor/hitbox, không đổi tốc độ/range. Giữ nguyên `PixelCamera.Mode.SCREEN`, quantization và raster phase 1/16 physical pixel. Đổi kích thước ExtendViewport thay vì camera.zoom để culling/clamp đọc đúng extent. HUD không đổi.

## 7. Pickup asset mapping, kích thước và hiệu ứng

Đã mở và tạo contact sheet có tọa độ từ **Kenney Tiny Dungeon CC0**, `Tilemap/tilemap_packed.png`, ô 16×16 không spacing:

| Chức năng | Ô source | Cách trình bày |
| --- | --- | --- |
| Damage Box | red potion (7,9) + chest (7,7) | khung đỏ |
| Speed Box | blue potion (8,9) + chest | khung xanh dương |
| Health Box | green potion (6,9) + chest | khung xanh lá, dấu cộng |
| Health Drop | green potion (6,9) | potion đơn, không chest |
| Weapon Box | chest (7,7) | khung tím và dấu vàng |

Art 16 px vẽ ở scale 1.5 (24 px trước zoom), giữ aspect; potion nằm trên chest nên dễ phân biệt các loại hộp. Khung/glow tĩnh tránh rung do bob dưới một pixel; chỉ hiệu ứng collection ngắn có rise/scale. Hitbox/cách thu nhặt cũ giữ nguyên. Không sửa `PickupConfig`, reward handler, xác suất, lịch spawn, TTL hay cap.

PNG và manifest mới có extrusion 2 px, Nearest/ClampToEdge, không xử lý lại source. Xem `assets/game/PRESENTATION_SOURCES.md` và các preview `pickups-zoom*-night*.png` trong thư mục bằng chứng; các preview chỉ chứa Kenney CC0 và hình học, không có art nhân vật/quái bị hạn chế AI.

## 8. Procedural map 200×200

Gameplay thường là 200×200 tile 16 = 3200×3200; spawn tại (1608,1608). Clearing tâm ellipse bán kính 22×18 tile; các đường lớn rộng 5 tile qua tâm và các phần tư nối vùng xa. 32 patch terrain liên tục; 16 cụm pond, 104 cụm cây, 40 cụm đá với thử đặt hữu hạn và buffer 3 tile. Chặn nội thất tối đa 4.000 tile, repair tối đa 12 lần rồi fallback open-edge có kết nối.

Nền gồm grass, biến thể grass, dirt/path, stone, sand và pond/shore. Dirt/stone/sand có 16 biến thể chuyển tiếp theo láng giềng, tạo từ pixel Kenney và grass bằng dither biên 3 px. Các ô source thật đã được kiểm tra bằng contact sheet; tọa độ và canvas nằm trong manifest/tài liệu asset.

Prop bổ sung: cây/pine xanh teal, cây khô, bụi hoa, tuft, hoa đỏ/trắng, đá nhỏ/đá rêu, fence remnants và vài cụm campfire/barrel/stool. Giữ tree/pine/bush/rock/stump cũ. Cây/đá/nước dùng mask blocking; decor nhẹ không ghi collision. Không đặt potion giả làm decor.

World atlas mở rộng chứa cả region nền/prop cũ theo pixel source nguyên gốc, giúp ground/props dùng một texture. File atlas/catalog lịch sử không sửa. Chỉ vẽ ground trong camera, prop theo chunk 160 unit, navigation build một lần/session; A* dùng kích thước grid động và budget cũ 2 search/frame. Enemy cap Easy 40/Hard 60 giữ nguyên; spawn lấy mẫu gần camera, không quét 40.000 ô/enemy/frame. Fixture 100×100 chỉ còn ở exhibit/camera/profiler lịch sử được bật rõ ràng.

## 9. Ba seed và navigation

| Seed | Walkable / 40.000 | Tỷ lệ | Props | Repair / fallback | Path xa dài nhất | A* expanded lớn nhất trong mẫu |
| --- | --- | --- | --- | --- | --- | --- |
| 42 | 36.322 | 90.805% | 4.464 | 0 / false | 275 bước | 1.763 |
| 20261113 | 36.417 | 91.0425% | 4.482 | 0 / false | 275 bước | 1.667 |
| 8675309 | 36.182 | 90.455% | 4.739 | 0 / false | 275 bước | 6.215 |

Mọi node ground đi được đều cùng component với spawn. Kiểm tra đường bốn hướng tới mép/góc và nhiều đích băng qua cụm, safe spawn, population ban đầu, spawn không trong blocker và budget AI. Buffer/đường rộng tránh kẹt hành lang một tile. Seed khác tạo terrain/blocker khác nhau; fixed seed tiếp tục tái hiện cùng bố cục. GameScreen thực đã kiểm tra camera cả bốn góc và Retry đổi seed/reset session.

## 10. FPS và frame time

Pixel_4 API 37, 2280×1080, **Android Emulator OpenGL ES Translator / Google SwiftShader**. Sáu stage ngắn tuần tự, mỗi stage 30 warmup + 60 measured frame; cùng map seed 42, Sword 1 + Fire 5 + Lightning 5. HP chẩn đoán giữ population, lighting day/night được ép riêng trong profiler để so chi phí; đây không phải kiểm tra trigger 300 giây. Không chạy stress dài hay GL test/build song song khi đo.

| Quái | Lighting | FPS đo | Mean ms | P95 ms | AI+A*+separation ms | World submit ms | Night submit ms | Projectile update ms |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 10 | day | 24.37 | 41.03 | 60.67 | 0.549 | 19.007 | 0.017 | 0.038 |
| 10 | night | 23.63 | 42.32 | 67.48 | 0.548 | 17.857 | 3.365 | 0.030 |
| 30 | day | 23.83 | 41.97 | 62.73 | 1.557 | 21.585 | 0.018 | 0.057 |
| 30 | night | 22.82 | 43.82 | 59.95 | 1.070 | 18.304 | 2.072 | 0.042 |
| 60 | day | 22.42 | 44.60 | 61.51 | 1.935 | 22.727 | 0.019 | 0.072 |
| 60 | night | 20.37 | 49.10 | 70.71 | 1.921 | 24.226 | 1.257 | 0.146 |

Combat update khoảng 0.14–0.24 ms; weapon render submit 0.25–0.48 ms. Trong mẫu có tối đa 3 projectile/4 effect hoạt động, không phải test đầy cap 48. CSV tách các phần để tránh cộng đúp projectile vào combat và night/VFX vào world. Submit timing có thể gồm chờ driver, không phải GPU timer; callback FPS bao gồm render/pacing nên không bằng tổng các cột CPU.

Lần đo đầu trước gom atlas có khoảng 36.43 world draw call ở 10/day và 68.23 ở 60/day. Sau gom còn 7.10 và 38.82; FPS hai lần có dao động và không cho thấy cải thiện chắc chắn. Không tuyên bố đạt 60 FPS: bản cuối đo khoảng 20.37–24.37 FPS trên SwiftShader. So với số đo Task 03B cũ cũng khác map/population/level, không dùng để suy ra phần trăm regression. Dữ liệu trước/sau lưu riêng `android-world-night-before-batching*` và `android-world-night*`.

## 11. Kiểm thử Desktop/Android thực chạy

- Compile `:core:compileJava :android:compileDebugJavaWithJavac` thành công. Chỉ có cảnh báo SDK XML version từ toolchain sẵn có.
- Task03B1 logic: **112.812** điều kiện qua, gồm level/volley/root bolts, fan gây damage, một vụ nổ/đạn, cooldown Normal/Demo/Pause, night threshold/fade/modal, ba zoom và map/nav/spawn.
- Hồi quy: WeaponChecks **223**, PickupChecks **21.438**, Sword phase A **17**, CameraChecks **44.781** qua. Map phase D được cập nhật fixture 200×200: **358.876** điều kiện qua, gồm tái hiện seed và fallback.
- Desktop thực có GL: WeaponSmoke **45**, PickupSmoke **27**, WorldNightSmoke **17**, PresentationProbe **33** qua. WorldNightSmoke chạy GameScreen với population chẩn đoán kiểm soát, delta simulation tới Demo 45 s, full night, pause, vũ khí, các góc map, death/Retry; không phải chờ 45 giây wall clock. WeaponVfxProbe render cả **10** cấu hình Fire/Lightning cấp 1–5 và xuất gallery hình học để kiểm tra độ đọc; không vẽ art actor.
- Terrain framebuffer ở zoom 1.10/1.15/1.20 và nhiều phase: **0 pixel ngoài palette grass**. Preview pickup/night: không magenta, marker HUD trắng giữ nguyên; không readback/export art player/quái.
- Đã nhấn **Run Android Studio** trên project đang mở, configuration android / Pixel_4. IDE log ghi full deploy và kích hoạt HomeActivity; không dùng standalone assemble/install để bàn giao.
- Android native + joystick tới Weapon Box trên seed 42: **53** kiểm tra qua, chọn cả Bow/Fire/Lightning bằng touch, tự đánh có hit/explosion, modal một lần, double tap, Pause/lifecycle/Resume, Home, token duy nhất qua các trận.
- Android world/native flow: **31** kiểm tra qua cả Easy/Hard × Normal/Demo, native Intent, map seed mới, joystick, Pause/lifecycle, combat thường tới Game Over, Retry đổi seed và khôi phục HP/kills, fixed seed 42 và Home. Không inject damage để ép Game Over trong lượt Android này.
- Pass ngày/đêm và vũ khí cấp 5 đã render thực trên Android trong profiler; trigger Normal 300/Demo 45 và freeze kiểm tra bằng simulation riêng. Diagnostic flags được dọn/khôi phục sau chạy; app trả về Home.

Lệnh tái hiện tập trung: `python tools/run_task03b1_checks.py Task03B1Checks WeaponChecks PickupChecks Task021Checks CameraChecks WeaponSmoke PickupSmoke WorldNightSmoke PresentationProbe ZoomTerrainProbe`. Chạy các tool Android sau Run trong Studio; profiler cần chạy riêng để số đo không bị workload khác làm lệch.

## 12. Lỗi/hạn chế còn lại

Chưa đạt 60 FPS trên renderer phần mềm; mẫu đo ngắn không đại diện GPU điện thoại thật, mọi vùng camera hay thời gian chơi dài. Night vẫn có chi phí blend full viewport, dù chỉ một pass. VFX hình học không phải animation sprite mới. Chưa benchmark đầy 48 projectile/effect hoặc nhiều thiết bị/GLES driver.

Không chạy chờ 5 phút Normal trên emulator; trigger được xác nhận bằng logic/simulation và GLES pass bằng diagnostic. Các preview AI xem chỉ có Kenney CC0; không tuyên bố đã đánh giá mỹ thuật ảnh player/quái có license hạn chế. Quái N3XUS chưa được nhập runtime vì điều kiện license ở mục 13. Không có Evolution/Boss/Victory/Dash/Ultimate/Achievements bridge trong task này.

## 13. Nguồn quái bổ sung

Đã đọc LICENSE.txt và README.txt gốc của N3XUS trước khi xử lý ảnh. License có điều khoản cấm artwork làm đầu vào/prompt cho AI và cấm một số hình thức phân phối standalone; quyền dùng cá nhân/thương mại có yêu cầu credit không xóa các điều kiện này. Task không mở, decode, pack hay gửi ảnh N3XUS vào AI và không đưa nó vào catalog/runtime.

Đã chuẩn bị vai trò và seam tích hợp cho Ghost (special approach), Goblin (fast melee), Skeleton (ranged tương lai), Zombie (slow/tanky/crowd) trong `ENEMY_EXPANSION_NOTES.md`. Không thay Bat/Slime hiện có, không sửa lịch spawn 10 phút. Tài liệu cũng chỉ ra candidate CC0 Tiny Dungeon và giới hạn của chúng: icon tĩnh, chưa phải đủ directional animation. Phần chuẩn bị hoàn thành; phần nhập art N3XUS được hoãn rõ ràng, không đánh dấu hoàn tất tích hợp quái.

## 14. Chuẩn bị Task 03C

Giữ normal cap 5 và ba unique weapon slot. Khi thực hiện 03C cần eligibility sau cấp 5, gameplay mạnh hơn và animation/VFX khác biệt rõ, thay vũ khí trong slot hiện có, giữ token/pending pickup của reward modal. Không gọi việc tăng riêng damage là evolution. Config và snapshot VFX mới hỗ trợ thêm profile presentation, nhưng task này không tự mở trạng thái evolved hay level 6.

Nên xác nhận gameplay/độ đọc trên GPU Android thật, chọn asset AI-compatible cho quái, rồi thiết kế eligibility/evolved profile và budget trước khi triển khai. Không tự chuyển sang Task 03C.
