# Task 02 — Hợp đồng core / Android Native

Ngày: 13/11/2026. Ghi nhận từ source thực tế, không thay thế thiết kế UI của Antigravity.

## Hiện trạng đã xác minh

- `HomeActivity` là launcher trong Manifest; `GameModeActivity` mở `AndroidLauncher` bằng explicit Intent.
- `GameModeActivity.EXTRA_DIFFICULTY` có giá trị key `EXTRA_DIFFICULTY`, value `EASY` hoặc `HARD`.
- `GameModeActivity.EXTRA_GAME_MODE` có giá trị key `EXTRA_GAME_MODE`, value `NORMAL` hoặc `DEMO`.
- Task 02 đọc đúng hai hằng số hiện có trong `AndroidLauncher`, chuyển vào `GameConfig.fromIntent`. Thiếu/không nhận diện value: mặc định EASY + NORMAL. Core không phụ thuộc Android.
- Có `ResultPreviewActivity` và XML Pause/Game Over/Victory. Đây là luồng preview; source chưa có API nhận thống kê trận hoặc callback kết quả thật. Không nối kết quả gameplay vào màn preview bằng dữ liệu giả.
- Không tìm thấy `UI_DESIGN_SPEC.md`, `HUD_SPEC.md`, `UI01_REPORT.md` hoặc tài liệu tích hợp cũ trong workspace khi bắt đầu task.

## Luồng đang hoạt động trong core

`RUNNING → DYING → GAME_OVER`

HP = 0 dừng AI, combat, spawn và hai đồng hồ. Chỉ Death của player tiếp tục đến hết clip; sau đó libGDX hiện Game Over tạm với thời gian sống **thực trong mô phỏng**, kills, Retry và Home.

- Retry tạo `GameSession` mới, giữ cùng `GameConfig`, map, AssetManager và SpriteBatch. Không mở thêm Activity hoặc nạp lại texture.
- Home ở Pause/Game Over gọi `Runnable` đã truyền qua `MonsterSurvivorGame(GameConfig, Runnable)` và `GameScreen(GameConfig, Runnable)`.
- Launcher chuyển callback về UI thread bằng `runOnUiThread`, mở Home với `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP`, rồi `finish()` gameplay Activity. Dùng cùng cách về Home đã có trong source preview.
- Android Back/Esc trong trận mở Pause; Back/Esc ở Game Over về Home. Pause có Resume và Home. Lifecycle quay lại app vẫn yêu cầu Resume; delta đầu sau resume bị bỏ.
- Không sửa Activity giao diện, XML, menu, navigation, theme, Manifest, Settings hoặc Achievements.

## Đồng hồ và cấu hình

`GameSession.elapsed()` là số giây gameplay không pause, dùng cho thống kê sống sót. `progress()` là đồng hồ tiến trình riêng: NORMAL ×1; DEMO ×600/90. HUD DEMO hiển thị thời gian tiến trình và nhãn DEMO; Game Over ghi thời gian sống thực.

Movement, animation, cooldown và miễn nhiễm luôn dùng delta thực đã giới hạn, không dùng hệ số Demo. Task 02 không có boss/Victory; vượt mốc tiến trình 600 giây không tự kết thúc trận.

Điểm chỉnh gameplay: `GameConfig.Builder`, gồm difficulty/mode, HP player, sword, population, enemy stats, path budget và debug. `GameScreen(6..60)` là cảnh debug preview độc lập, không phải đường khởi chạy trận chính. Intent bình thường không bật preview.

## Đề xuất nối màn kết quả native ở task sau

Đây là **đề xuất**, chưa phải API đã triển khai:

1. Chốt một DTO bất biến gồm `matchId`, `difficulty`, `gameMode`, `elapsedSeconds`, `progressSeconds`, `kills`, `outcome=GAME_OVER`.
2. Chỉ gửi kết quả một lần sau khi Death hoàn tất. Phía Android nhận trên UI thread; phía core ngừng update và tiếp tục sở hữu/dispose tài nguyên theo lifecycle.
3. Native Retry dùng lại difficulty/mode của trận; Home dọn stack như hiện tại. Chốt rõ bên nào ghi thống kê/Achievements, tránh ghi hai lần khi lifecycle resume hoặc Retry.
4. DEMO được phân biệt khi ghi kỷ lục. Không dùng màn Victory để kết thúc ở 90 giây/10 phút khi boss chưa được làm.
5. Sau khi Antigravity cung cấp điểm nhận dữ liệu thật, thay màn kết quả libGDX tạm bằng bridge; giữ nguyên XML/theme đã thiết kế.

Task 02 chưa ghi thành tích/kỷ lục hoặc liên kết Settings vào Pause gameplay. Nút/luồng preview native vẫn giữ nguyên chức năng trước task.

## Kiểm tra Android cần thực hiện bằng Run

Trong Android Studio chọn cấu hình module `android`, thiết bị Pixel_4, nhấn Run. Kiểm tra Home → chọn EASY/HARD, NORMAL/DEMO → Play; Pause/Resume, nút Home của hệ điều hành, quay lại app; HP = 0 → Death → Game Over → Retry/Home. Kiểm tra Retry giữ lựa chọn và Home không tạo bản sao Activity.

Phiên Task 02 chỉ compile Android, chưa thao tác Android Studio Run và chưa xác minh runtime/FPS của bản mới trên emulator. Không dùng kết quả Android Task 01B.2 làm bằng chứng cho gameplay mới. Không chạy quy trình assemble/export/install APK/AAB hoặc phát hành.

## Cập nhật Task 02.1 — 13/11/2026

Các mục Task 02 phía trên là ghi nhận lịch sử. Task 02.1 giữ nguyên Intent và bridge Home, cập nhật các điểm sau:

- Mỗi trận gameplay thường dùng `ForestGenerator` 100×100 với seed ngẫu nhiên. Retry tạo map, CollisionGrid, navigation và session mới; không tái sử dụng path cũ. AssetManager/SpriteBatch giữ nguyên. Pause/Resume và lifecycle không đổi map.
- `GameConfig.Builder.fixedSeed(long)` hoặc file local riêng của app `files/map-seed.txt` bật seed cố định để tái hiện lỗi. Khi bật fixed seed, Retry tái hiện cùng bố cục; bỏ flag để quay về seed mới mỗi trận. Seed và số ô đi được có trong Logcat `Ready`/`Retry`.
- Kiếm: range 48, detection 60, windup 0,15s, cycle 1s; playback Attack nén còn 0,65s, chỉ Attack đổi tốc độ. Hướng chạy và hướng chém độc lập; không có Intent/UI mới cho cơ chế này.
- HP đầu trận EASY Slime/Bat/Boar = 1/1/2; HARD = 1/2/3. EASY trần 40; HARD trần 60. Tiến trình được cấu hình theo stage, không phụ thuộc FPS tức thời. DEMO chỉ nén đồng hồ tiến trình.
- Đã chạy bản Task 02.1 bằng **Run của Android Studio** trên Pixel_4, API 37, 2280×1080; 31 kiểm tra native/ADB qua bốn tổ hợp difficulty/mode, joystick, pause/lifecycle, Home, Game Over và Retry. Fixed seed 42 trên Android có 8.883 ô đi được, khớp Desktop.
- Android Studio Run có thể dùng package debug nội bộ để triển khai; không thực hiện task xuất/bàn giao APK/AAB hoặc release riêng. Thay đổi field/class trong đợt này cần Run/Rerun đầy đủ, không dùng Apply Changes.
- `combat-profile.json` là flag đo cục bộ, chỉ dành cho chẩn đoán có giới hạn; bình thường không có profiler/tuning chẩn đoán. Flag đã được xóa khỏi emulator sau đo; `map-seed.txt` kiểm thử cũng đã được dọn/khôi phục.

Chi tiết thông số, giới hạn đo và bằng chứng: `TASK_02_1_REPORT.md`, `core/build/reports/task02_1/`. Native results/statistics/Achievements vẫn chưa có bridge nhận kết quả thật; không thay đổi thiết kế UI hoặc tự làm Task 03.

## Cập nhật Task 03A — 13/11/2026

- `GameSession.pickups` sở hữu Damage/Health/Speed Box và Health Drop. Session mới khi Retry xóa cả item, effect, toast và trả PlayerStats về 1 Damage, 10 Speed, 10/10 HP. Không có Intent hoặc native UI mới.
- `GameConfig.pickups` / `Builder.pickups(PickupConfig)` là điểm tuning. Spawn hộp 15–20 giây progress, cap 6; hết hạn 60 giây thực. Health drops cap 20, 30 giây thực, Easy/Hard 8%/5%, HP ≤3 cộng 4 điểm phần trăm, heal 1–2. Pause không tăng lịch hoặc tuổi.
- `Player.stats` chứa Damage/Max HP/Speed của trận; `Player.heal` không hồi sinh. Movement dùng Speed ×8 và giữ normalize/substep. SwordCombat đọc Damage tại hit; pickup tiếp xúc được xử lý trước hit cùng frame. Health đầy luôn tăng max; Speed Box đã tồn tại khi đạt 20 chuyển thành Damage.
- `Enemy.damage` có hook chuyển tiếp death duy nhất, nối qua `EnemySystem.onDeath` vào pickups. Nguồn damage tương lai dùng API này; không spawn drop trong animation/render.
- `game/pickups.png` và `pickups.json` là atlas Kenney CC0 riêng, load một lần; tổng 15 texture. `world.png`, catalog, PixelCamera, collision/procedural generator và native/Gradle giữ nguyên; asset list tự sinh thêm hai đường dẫn.
- RNG session dẫn xuất từ seed map, các stream pickup độc lập. Fixed-seed/debug có log vị trí hộp; collection có log chỉ số thực. Không đọc lại nguồn sprite trong trận.
- Task 03B cần thêm loại/handler và eligible pool khi đã có chức năng. Handler `true` hoàn tất collection, `false` giữ item; modal phải có pending id, khóa input/dừng simulation rõ ràng và cleanup khi Retry/Home. Task 03A chưa có Weapon Box hay modal.
- Đã chạy core, Desktop GL và Android Studio Run/Pixel_4; nhặt ba hộp bằng joystick trên emulator. Probe ngắn 8 quái với 0/26 item khoảng 32,73/30,41 FPS trên SwiftShader, không phải cam kết FPS điện thoại thật. Profiler/map-seed flag kiểm thử được dọn/khôi phục.

Chi tiết kiểm thử, giới hạn và hợp đồng Weapon Box: `TASK_03A_REPORT.md`; bằng chứng `core/build/reports/task03a/`. Các báo cáo Task 02/02.1 phía trước được giữ nguyên như lịch sử.

## Cập nhật Task 03B — 13/11/2026

- Session sở hữu `WeaponSystem` / `WeaponLoadout` / `WeaponSelection`. Trận mới Sword 1; tối đa ba loại duy nhất, cấp thường 1–5. Không có Evolution.
- Sword cấp 1 giữ range 48 / windup 0,15s / cycle 1s / playback 0,65s; reuse SwordCombat. Bow, Fireball, Lightning có cooldown riêng, vẫn dùng giây thực trong Demo và không điều khiển hướng joystick.
- Tuning theo level trong `WeaponConfig`, override qua `GameConfig.Builder.weapons(...)`. Damage = round(base player damage × multiplier + bonus), ít nhất 1, chống overflow. Kiếm đọc lúc hit, projectile chụp lúc phóng, Lightning chụp lúc bắt đầu chuỗi.
- Weapon Box dùng lịch/cap/collision Task 03A, xác suất mặc định 33%, bảo đảm có trong hai lượt hộp thành công đầu tiên khi bật. Rương Kenney tint tím, chỉ dẫn W, không thêm file asset hoặc sửa atlas.
- `CHOOSING_WEAPON` đóng băng simulation/clocks/tuổi item. Handler giữ false lúc mở; chỉ sau choice hợp lệ mới trả true qua `PickupSystem.collect(id)` để tiêu thụ. Pending ID + token duy nhất giữa các session chặn callback Retry cũ/double commit.
- UI đọc `selection.size/get/token/pendingId`; commit qua `GameScreen.chooseWeapon(index, token)` trên libGDX thread để giữ guard Pause và reset input. Back mở Pause trên modal; Resume trở lại cùng offer, Home kết thúc session. `WeaponHud` là presentation tạm cho UI02, không có Intent/native UI mới.
- Khi đủ slot chỉ nâng vũ khí đã sở hữu; ba vũ khí cấp 5 cho fallback Damage +2 / Max HP +2 & heal 2 / Speed +1 nếu dưới 20. Hiển thị ít hơn ba lựa chọn khi cần, không offer loại thứ tư hoặc level 6.
- Tất cả damage qua `Enemy.damage` và hook death Task 03A. **HUD/kết quả dùng `GameSession.kills` tổng**, `SwordCombat.kills` chỉ là số kill của kiếm; không cộng lại. Death/end/Retry thu hồi projectile/effect và xóa pending.
- 48 projectiles / 48 effects pooled; swept projectile + wall LOS, blast có LOS và chain bỏ ID đã đánh. VFX dùng chung texture thủ tục trắng 1×1 load/dispose một lần; 15 texture file cũ + 1 procedural = 16, không thêm PNG, không phân tích artwork Tiny Questers bằng AI.
- Đã chạy core/GL và Android Studio Run trên Pixel_4; joystick tới hộp tự sinh, touch chọn cả ba ranged, kiểm tra Back/lifecycle và chống double tap. Không suy các nhánh cap/level 5/Retry Android từ kiểm thử GL. Probe FPS ngắn và giới hạn đo nằm trong báo cáo.

Chi tiết cân bằng, kiểm thử, giới hạn và hợp đồng UI02: `TASK_03B_REPORT.md`, bằng chứng `core/build/reports/task03b/`. Native UI/Intent/Settings, map/AI/collision/camera, asset cũ, Gradle và GDD giữ nguyên. Task 03C mới chỉ là đề xuất.

## Cập nhật Task 03B.1 — 13/11/2026

- Gameplay mới dùng ForestGenerator 200×200 tile 16 (3200×3200), spawn (1608,1608); các ghi chú 100×100 ở phần cũ là lịch sử. Retry thường tạo seed mới; fixedSeed/map-seed.txt tiếp tục tái hiện bố cục để debug. Nav/collision/spawn dùng kích thước grid, cap Easy 40/Hard 60 và budget A* giữ nguyên.
- `GameConfig.visuals` / `WorldVisualConfig` đặt world zoom mặc định 1.15, Night trigger 300 progress, fade 3 giây thực, light radius 120. PixelCamera SCREEN và HUD viewport giữ nguyên; thay minimum world viewport để culling/clamp đúng.
- Fire cấp 1–5 có 1/2/2/3/3 projectile, cấp 5 CD 2.1 s. Lightning cấp 5 giữ chỗ hai primary rồi phát hai tia trực tiếp, dùng visited/budget chung cho cả chuỗi; một quái chỉ nhận một hit/activation. Cooldown, pool và modal vẫn theo clock thực.
- `NightState` thuộc session, chỉ tiến khi gameplay chạy. World → night overlay → weapon VFX → HUD. NightRenderer dùng 240 quad trên viewport và SpriteBatch shader có sẵn; không full-map FBO hay custom shader. Retry xóa night cùng session cũ.
- Hai atlas Kenney CC0 mới được tạo bởi `tools/PreparePresentationAssets.java`; không sửa source/PNG/catalog cũ. World atlas mở rộng chứa cả region Kenney cũ để giảm đổi texture. Pickup dùng potion Tiny Dungeon đỏ/xanh dương/xanh lá, scale 1.5, chest/khung/dấu riêng; reward, probability, cap và TTL không đổi.
- `combat-profile.json` có opt-in `worldProbe: true`: sáu stage ngắn 10/30/60 quái × day/night, HP chẩn đoán, Fire/Lightning 5 và lighting ép riêng để đo. Nó không chứng minh trigger 300 giây; dùng logic/Screen checks riêng cho clock. Chạy profiler độc lập với build/GL tests. Tool khôi phục/xóa flag và trả app về Home.
- N3XUS chỉ được đọc license/README dạng text; artwork chưa vào pipeline AI hay runtime do điều khoản cấm AI. Vai trò Ghost/Goblin/Skeleton/Zombie và điều kiện tích hợp/evolution tương lai nằm trong [ENEMY_EXPANSION_NOTES.md](ENEMY_EXPANSION_NOTES.md).
- Đã Run đầy đủ bằng Android Studio trên Pixel_4; 53 kiểm tra Weapon Box/joystick/modal và 31 kiểm tra native modes/lifecycle/Game Over/Retry qua. SwiftShader đo bản cuối khoảng 20.37–24.37 FPS trong mẫu ngắn, chưa đạt 60 FPS. Chi tiết cấu hình, số đo và giới hạn: [TASK_03B_1_REPORT.md](TASK_03B_1_REPORT.md).

## Cập nhật Task 04A — 13–14/11/2026

- Toàn roster hiện có 82 định nghĩa thường: 67 MonstersCreatures (40 base + 27 recolor), chín Willosuna, hai Tiny Questers và bốn SpriteCanvas. Enumerate `EnemyRegistry.all()`; `EnemyType` chỉ là adapter ba loại cũ. Không dùng enum đó để đếm hoặc lọc toàn bộ quái.
- Chỉnh combat/spawn tại `tools/enemy_balance.json`, chạy `python tools/prepare_enemy_assets.py` để sinh đồng bộ atlas, animation catalog và `EnemyCatalogData.java`. Registry Java thuần dùng được trước khi Gdx.files sẵn sàng trên Android. Một atlas mới 2048×2048, Nearest/extrusion hai pixel; tổng GameAssets 19 texture, khoảng 42,23 MiB RGBA, load/dispose theo cùng vòng đời hiện tại.
- Spawn theo progress 0–600, seed/weight riêng cho trận, unlock theo biome, Night từ 300, rare/tank muộn, type/family resident cap và chống chọn liên tiếp quá nhiều. Cap Easy 40/Hard 60 vẫn tính corpse; cadence/density không tăng. Sau 600 dừng spawn thường nhưng không tự Victory. `lich_lord`/540 chỉ là hợp đồng boss, chưa spawn boss.
- Mọi định nghĩa dùng AI chase/contact chung với footprint riêng và budget hai A*/frame. Flying chỉ vượt nước; tree/rock vẫn chặn. Ranged/Special candidate có tên kỹ năng và clip dành cho 04B, hiện chưa có projectile/special mới. Attack source frame 2 chưa là hit event mới. SpriteCanvas dùng side view/mirror/fallback, nên được phân loại ADAPTABLE.
- Damage từ cả bốn vũ khí vẫn qua Enemy.damage và một death hook: HUD đọc GameSession.kills, không cộng lại kill của SwordCombat. Session end/Retry dọn residents, cached path và listener cũ. Modal/pause/clocks/weapon levels/rewards giữ hợp đồng Task 03B; pickup dùng hình/scale Task 03B.2, ảnh ngày/đêm regression trùng bản đó.
- Đã kiểm tra từng ID với bốn vũ khí trong Normal/Demo, thực thi GameScreen đủ 82 loại và ba vòng Retry. Android Studio Run trên Pixel_4, bốn tổ hợp native modes và gameplay tự nhiên qua; FPS probe ngắn 10/30/60 mixed species đạt 21,31–25,13 trên SwiftShader, chưa đạt 60 FPS. Không suy ba Retry desktop thành kiểm chứng Retry touch Android trong task này.
- Bảng tuning/ID: [ENEMY_ROSTER.md](ENEMY_ROSTER.md). Nguồn/clip/license: [ENEMY_ASSET_INVENTORY.md](ENEMY_ASSET_INVENTORY.md). FX tương lai: [COMBAT_FX_INTEGRATION_PLAN.md](COMBAT_FX_INTEGRATION_PLAN.md). Bằng chứng và giới hạn: [TASK_04A_REPORT.md](TASK_04A_REPORT.md). Không sửa GDD/native UI hoặc triển khai 04B/04C.

## Cập nhật Task 04A.1 — 14/11/2026

- Balance overlay mới ở `GameConfig.Stage`, mốc 0/60/120/210/300/420/540 progress giây. Easy giữ phần làm quen, tăng áp lực sau 60; Hard target/cadence cao hơn, speed base ×1,10 (thay ×1,08). Tank/elite nhận bonus HP cột Boar; strong/elite có stage weight multiplier, một số strong Hard damage floor 2 từ 300. Base data `tools/enemy_balance.json`, registry82 và unlock không đổi; không regenerate enemy assets vì không đổi bảng base. Cap resident 40/60, A*2/frame, Demo chỉ nhân progress.
- `GameInput` là logic floating joystick dùng `GameInput.Surface` cho viewport/hit-test. Tọa độ HUD gốc dưới trái sau unproject; activation x0–44%width, y28–height−96; UI/modal ưu tiên trước. Tâm chính là touch-down, radius66, radial deadzone0,12, owner pointer tiếp tục drag qua nửa phải. Một ngón khác không giành owner. `activePointer/joystickActive/centerX/centerY/knobX/knobY/axisX/axisY` cung cấp trạng thái cho UI02. `reset()` cho pause/modal/death/Home/lifecycle/Retry; không tự khôi phục vector sau Resume.
- Presentation joystick hiện chỉ vòng hint alpha0,12 khi nghỉ và base/knob tại tâm active. Antigravity thay renderer qua các getter, giữ logic/pointer ownership và modal token. Vùng UI mới cần được ưu tiên trước activation; không giới hạn drag của owner trong vùng bên trái.
- `GameLanguage` VI/EN và `GameStrings` có 60 key/đủ hai bản dịch. Core dùng `GameConfig.language/strings`, không đọc Android Context/SharedPreferences. Default/invalid/null →vi; Retry giữ ngôn ngữ trận. Chuyển Settings chỉ áp dụng khi tạo trận mới, không có hot-switch. HUD tạm, weapon offers/description/fallback và pickup feedback đã dùng strings; không đổi reward/offer eligibility/level cap.
- **Một key** `GamePreferences.KEY_GAME_LANGUAGE = "game_language"`, trong **`monster_survivor_prefs`** hiện có. UI02 gọi `getGameLanguage()` / `setGameLanguage("vi"/"en")`; reset defaults về vi. Getter chịu được value sai kiểu. AndroidLauncher đọc getter và truyền tham số thứ ba cho `GameConfig.fromIntent(difficulty,mode,language)`, không thay difficulty/mode extras và không tạo key ngôn ngữ khác.
- Native selector/ngôn ngữ resource của Home/Settings **chưa có**, giao Antigravity. UI02 cần thêm lựa chọn Việt/Anh và hoàn thiện native translations, text layout, HUD chính thức; core API/logic đã sẵn sàng. Không viết lại joystick movement hoặc handler thưởng.
- Font mặc định thiếu glyph Việt thật. HUD tạm dùng BMFont Roboto15px, Nearest,261 glyph NFC, một page512×256; tool offline `tools/prepare_ui_font.py` dùng font Apache2.0 có sẵn trong emulator, không ship sourceTTF. UI font tăng0,375MiB RGBA so với cũ; GameAssets19 texture/42,23MiB vẫn không đổi. Font được giữ qua Retry và dispose cùng HUD. UI02 thêm chuỗi mới cần kiểm tra glyph thật, không chỉ hasGlyph fallback.
- Đã compile offline, chạy checks logic/GL cho input, language, modal/lifecycle/GameOver/Retry và regression weapon/pickup. Android Studio Run trên Pixel_4 có44 check qua với bốn tổ hợp modes, vi/en/invalid/wrong-type preferences, gesture thật và lifecycle đang giữ touch. FPS probe ngắn23,43–26,33 trên SwiftShader, chưa đạt60 và chưa chứng minh cải thiện FPS. Mô phỏng trước/sau có paired seed không thay playtest cảm giác người thật.
- Chi tiết bảng trước/sau, API, placeholder keys, bằng chứng và giới hạn: [TASK_04A_1_REPORT.md](TASK_04A_1_REPORT.md); log/proof trong `core/build/reports/task04a_1/`. Không sửa native XML/theme/Manifest/GDD, WeaponConfig, map/camera/Night/pickup renderer hoặc triển khai04B.

