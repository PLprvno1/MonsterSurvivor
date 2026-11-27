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

## Cập nhật Task UI02 — 14/11/2026

- Antigravity hoàn thiện toàn diện thiết kế HUD gameplay libGDX và hệ thống bản địa hóa song ngữ Việt - Anh.
- Bộ chọn ngôn ngữ RadioGroup trong `SettingsActivity` (`rb_lang_vi`, `rb_lang_en`), lưu vào SharedPreferences `monster_survivor_prefs.xml` theo hợp đồng `GamePreferences.KEY_GAME_LANGUAGE = "game_language"`.
- Bọc ngữ cảnh động `UiUtils.wrapLocale(Context)` trong `attachBaseContext` cho toàn bộ 6 Activity Native (`HomeActivity`, `GameModeActivity`, `SettingsActivity`, `AchievementsActivity`, `HowToPlayActivity`, `ResultPreviewActivity`). `SettingsActivity` và `HomeActivity` tự động `recreate()` làm mới ngôn ngữ ngay lập tức.
- `AndroidLauncher` đọc `GamePreferences.getGameLanguage()` và truyền vào `GameConfig.fromIntent(extraDifficulty, extraMode, language)`. Core engine nhận trực tiếp `GameConfig.strings` (60 khóa `GameStrings`).
- Thiết kế lại `GameHud`: Thanh HP bo góc bán trong suốt đỏ tươi (`rgba(18, 24, 32, 0.82)`), chỉ số Kills, DMG pill, SPD pill; huy hiệu đồng hồ & độ khó Dễ/Khó tại đỉnh giữa; nút Pause viền vàng an toàn góc phải trên; hộp thoại Pause và Game Over phong cách Fantasy.
- Hoàn thiện đồ họa Floating Joystick: Vòng tròn gợi ý mờ tại `(108, 108)` (alpha 0.12); vành hào quang ma thuật kép (R=66) tại điểm chạm ngón tay; tia dẫn hướng lực kéo phát sáng; núm ngọc 3D (R=26); trả về tức thì khi nhấc tay, không bao giờ kẹt vector.
- Thiết kế lại `WeaponHud`: 3 slot vũ khí đáy màn hình với màu nhận diện đặc trưng (Kiếm = Hổ phách, Cung = Xanh lá, Cầu lửa = Đỏ cam, Sét = Lam ngọc), badge cấp `Lv.1`..`Lv.5` và thanh tiến trình hồi chiêu cyan. Hộp thoại `Weapon Box Modal` chuyển thành 3 thẻ bài hoàng gia với bước tiến cấp độ `Cấp X → Cấp Y`, mô tả hiệu ứng, nút Về Trang Chủ và gợi ý chân trang. Bảo toàn tuyệt đối hitbox và hợp đồng chọn vũ khí `offerAt()` / `homeAt()`.
- Tài nguyên XML: Hoàn thiện `android/res/values-en/strings.xml` đối ứng 1:1 với `values/strings.xml`, không còn text hardcode.
- Chi tiết đặc tả & báo cáo: `UI02_HUD_SPEC.md`, `UI02_LOCALIZATION_MATRIX.md`, `UI02_REPORT.md`. Đã vượt qua 2534 logic checks và 1781 GL smoke checks.

## Cập nhật Task 05 — 14/11/2026

- Boss đã chạy thật, thay contract chưa triển khai ở các ghi chú cũ: Mini progress≥300, Final Lich≥600 (**10 phút, thay mốc540/9 phút cũ**). Night300 và ordinary spawn stop600 giữ nguyên;600 không tự Victory. `BossSystem` thuộc session, hai event độc lập với NOT_TRIGGERED/SPAWNING/ACTIVE/DEFEATED, finite placement/retry gần camera và fixed-seed Mini selection.
- Pool Mini tám loài phù hợp trong `BossConfig`, scale1,65× bản thường. Lich dùng asset/catalog hiện có, hai phase và các clip Spawn/Attack/Slam/Entrance/Death thật. Telegraph, hostile swept projectile cap24/lifetime3s và summon2/3 con có owner, collision/immunity/cleanup thật. Demo chỉ giảm Boss HP×0,8; các cooldown/movement dùng giây thực.
- Boss body ngoài registry, index−1, vẫn nhận bốn vũ khí qua Enemy.damage/death hook chung. Boss budget2 riêng ngoài cap quái thường40/60 kể cả corpse; navigation collider9×6 riêng, dùng chung tối đa2A*/frame. Có damage burst budget20%maxHP mỗi bucket0,2s chỉ cho Boss. Không sửa82 registry/generated data, assets, weapon/pickup tuning, map/camera/Night hoặc UI02 native files.
- `BossHud` thêm vào pass UI02 hiện có: một primary bar ưu tiên Final, Mini phụ, phase/HP thật, marker hướng ngoài camera và announcement2,8s.22keyvi/en mới, font hiện có đủ glyph. Không thay WeaponHud, floating joystick hoặc modal contract.
- State WINNING đóng băng combat/AI/spawn/clock, chỉ chạy Death; sau đó VICTORY với `MatchResult` thật. Snapshot gồm sessionId (process-local), victory, survivedSeconds, progressSeconds, kills, miniBossDefeated, finalBossDefeated, difficulty/mode/language. Native bridge sau này phải dùng snapshot/idempotency, không gọi ResultPreviewActivity với stats giả. Retry/Home giữ contract cũ, Home chống callback lặp.
- LogicBoss2.901/GL5.368 checks qua; regression weapon223/pickup21.438/Task04A1 2.622 qua. Android Studio Run Pixel_4 xác nhận Mini45,019s/Final90,023s bằng đồng hồ Demo tự nhiên với HP/equipment hỗ trợ; phase/death còn dùng damage fixture. Bản cuối có7 terminal Android checks qua, cả Game Over/Victory/Retry/singleHome; cờ và preferences đã khôi phục, mặc định debug=false.
- FPS probe Hard cô lập: Mini23,41–24,86; Final phase1 23,30–24,54; phase2 24,72–25,07 trên SwiftShader. Demo chạy cùng tác vụ GL desktop thấp hơn18,86–23,28 ở cảnh Boss, được giữ trong evidence. Chưa đạt60FPS, chưa đo Android60+2 residents hoặc thiết bị thật; không suy probe này thành bảo đảm mọi cảnh. Chi tiết tuning, test fixtures, số liệu và manual05:00/10:00: [TASK_05_BOSS_REPORT.md](TASK_05_BOSS_REPORT.md).

## Cập nhật Task 05.1B — 15/11/2026

- Audio đã tích hợp từ `monster-survivor-audio-selections.json` schema2: 49/49 selected, 36 file duy nhất (3 Music streaming,33 Sound). JSON/source không sửa; runtime36 file giống byte nguồn,8.159MiB nén, SFX PCM16 khoảng1.505MiB trước overhead backend. Không Preview/chiptune hoặc audio ngoài danh sách. Manifest máy đọc: `assets/audio/manifest.json`; license/credits ở `assets/audio/licenses`.
- `GameScreen` sở hữu `GameAudioManager` xuyên Retry; `GameSession.audio(AudioSink)` gắn sink vào action/HP/reward/cast thật. Constructor của simulation mặc định sink NONE để dùng headless. Không gọi audio từ animation frame, HUD redraw hoặc notice text. Không thay listener kills/drop. Boss death loại generic enemy death; enemy reaction chỉ chọn một group; legacy_charge chỉ burst đã có. Bốn FUTURE_* audit không thuộc49 selections, không được triển khai.
- Music priority: kết quả > Final/WINNING > Mini > Night > Day; Menu thuộc native lifecycle. Mini/Final chung Heavy không stop/seek/restart, phase2 chỉ SFX. Victory/GameOver one-shot sau terminal state thật. Một track chính, không crossfade. User Pause dừng voices trước UI_PAUSE; modal chỉ pause nhạc để không cắt open/commit cue vừa phát. Focus/background mute toàn audio và giữ Pause đến thao tác Resume.
- Cap16 voices, priority100/70/60/20, duration-based expiry và stop theo Sound ID, per-event/category cooldown, shared-file35ms, enemy actor1s/radius220. Không tạo Sound/Music hoặc làm audio filesystem I/O trong gameplay loop. Backend failure/negative ID chỉ bỏ cue và log; reset/dispose idempotent.
- Native screens dùng `NativeAudioActivity`/`NativeAudio`: một MediaPlayer Menu và SoundPool4 voices, pause/abandon focus ngay khi ra nền, release nếu không có native owner mới trong300ms. `AndroidLauncher` release native players trước libGDX initialize, dùng AudioFocusRequest/fallback và unregister preference listener khi destroy. Native Back qua OnBackPressedDispatcher, UI chỉ phát ở thao tác thật, slider preview khi thả.
- Dùng lại chính bốn key pref_music_enabled/pref_sfx_enabled/pref_music_volume/pref_sfx_volume trong monster_survivor_prefs. AudioSettings/AudioEnvironment là snapshot core thuần Java; core không biết Context/SharedPreferences. Music0 pause thật; SFX0 không play/stop voices. Không sửa Settings XML hoặc thêm Master Volume; giữ language/theme/vibration.
- Focused checks: Audio190, weapon223, pickup21.438, Boss2.901, GL/OpenAL audio14. Android Studio Run trên Pixel_4/SwiftShader đã kiểm native volume0/50/100/persistence, mode bridge, Boss/phase/results, background/foreground/Retry/Home; test dùng HP/equipment/clock/damage fixture có ghi rõ và khôi phục preferences/flags. API playback không phải xác nhận đã nghe đủ49 cue; coverage từng ID và giới hạn AudioFocus cạnh tranh/loop seam trong [AUDIO_EVENT_INTEGRATION_STATUS.md](AUDIO_EVENT_INTEGRATION_STATUS.md).
- Paired probe8 enemies: audio on26.04–27.92FPS, off22.92–28.23FPS,3 stage90 measured frames/stage trên emulator2280×1080; không có GL smoke đồng thời. Một cặp ngắn có tải host biến thiên, không kết luận nhân quả hoặc thiết bị thật/60FPS. Chi tiết tài nguyên, runtime mapping, kết quả và hướng dẫn nghe: [TASK_05_1B_AUDIO_REPORT.md](TASK_05_1B_AUDIO_REPORT.md), [AUDIO_RUNTIME_MANIFEST.md](AUDIO_RUNTIME_MANIFEST.md). Không commit/push, release/signing hoặc xuất APK/AAB bàn giao.


## Cập nhật Task 06 — 15/11/2026

- GameSession sở hữu ActiveSkillSystem cho DASH/ULTIMATE, ngoài ba Weapon Slots. SkillConfig immutable tách tuning/presentation, chưa có level/reward nâng cấp. Dash5s/0,20s/75units/iframe0,17s; Ultimate30s, initial15s, cast0,35s, radius105, max(4,ceil(damage×2,5)), knockback25/tank8,75/Boss0. Clock chỉ theo RUNNING elapsed, không theo Demo progress.
- Dash dùng swept Player movement≤3units/collision hiện có và snapshot vector joystick → last actual normal move → facing. Không sửa speed hoặc immunity0,7s sau mất HP; Player.invulnerable kết hợp hai guard độc lập, không PLAYER_HURT khi Dash chặn hit. Ultimate resolve một lần/token qua Enemy.damage, LOS obstacle, death/drop/Boss burst guard hiện có; knockback swept và invalidate navigation, không đổi Boss HP/AI/Victory.
- GameInput có pointer owners riêng cho joystick/Dash/Ultimate/modal, one touch-down, không auto-spam từ hold/drag; Shift/Space edge-latched. SkillHud draw/hit-test chung HUD units và safe insets; View getter chỉ đọc, cache theo session.id. SkillRenderer tái dùng weapon_pixel,≤6streaks/hai vòng32segments/hitFX≤12; không thêm texture/FBO/shader/resident cap. Antigravity thay presentation qua contract trong TASK_06_ACTIVE_SKILLS_REPORT.md, không tự tick cooldown hoặc mutate weapon slots.
- Ba audio ID optional SKILL_DASH_ACTIVATE/SKILL_ULTIMATE_CAST/SKILL_ULTIMATE_IMPACT hiện silent, null-safe. Chờ chọn cue qua AUDIO_AUDITION ở task sau; không fallback hoặc sửa49mapping/JSON/source/runtime audio. Kiểm tra36/36decode và JSON SHA gốc không đổi. GameStrings vi/en/font đủ glyph; không đổi game_language.
- Focused skill839/desktopGL328 checks PASS; joystick/localization2658, weapon223, pickup21438, Boss2901, audio190 PASS. Core+Android Java compile thành công. Android Studio Run Pixel_4/SwiftShader, actual two-pointer MotionEvents qua Android/libGDX,24checks PASS; Ultimate hits6 gồm2Boss, knockbacks4, single impact; EasyNormal/vi và HardDemo/en, modal/Retry/Home/audio lifecycle qua. Test dùng protectedHP/progress600/placement fixture, không đổi skill timing/Boss HP/AI; preferences/flags đã khôi phục. Không chạy lại82species stress, không export/cài APK thủ công, không commit/push.
- HardDemo2Boss+4ordinary: trước cast19,52FPS, window có cast19,35, sau20,61;11active samples mean57,71ms/max87,04ms. Baseline trước arena có16,23FPS; đây là probe ngắn có tải host, không bảo đảm60FPS hoặc mọi cảnh/thiết bị. Giới hạn, evidence, API UI và đề xuất06.1 ở [TASK_06_ACTIVE_SKILLS_REPORT.md](TASK_06_ACTIVE_SKILLS_REPORT.md).
- Probe Android Pause/background với cooldown khác0 thêm4checks PASS: DashCD3,6106863s và UltimateCD12,128319s giữ nguyên, elapsed2,8716815s không tick. Đối chiếu7preferences/fixture paths khớp gốc; helper dex tạm đã gỡ, kết thúc ở Home. Đây là evidence bổ sung cho24checks Android ở trên.

## Cập nhật Task 06.1 — 15/11/2026

- Sửa kết luận restore cũ: audit runtime phát hiện6 file trigger chứa chính stdout `cat: ... No such file or directory`, trùng backup sai của Task06. `adb exec-out` exit0 không chứng minh file tồn tại. Loader chỉ kiểm tra exists và JsonReader scalar dùng default khiến HP100000/debug=true; callback Boss còn grant Fireball/Lightning dù Skill fixture có ưu tiên config. Đã archive nội dung, gỡ đúng6 artifact do runner tạo, giữ file khác/preferences. Verification byte-equal của Task06 đã so với backup sai; evidence/kết luận mới trong báo cáo06.1 thay thế kết luận không-leak trước đó.
- Normal AndroidLauncher/MonsterSurvivorGame/GameScreen mặc định DiagnosticRun.NONE và không đọc root fixtures/seed/camera flag, kể cả debug build. DiagnosticLauncher chỉ trong debug source set, cần UUID namespace + token/lease dùng một lần, TTL60s; consume lease trước init, reject stale Intent. Chỉ một fixture tuning/callback, JSON phải object. Helper Android mới phân biệt EXISTS/MISSING, atomic writes, chỉ cleanup namespace UUID owned; bốn historical root-fixture runners bị chặn trước mutation.
- Phase0 xác nhận trước upgrades, lặp sau diagnostic: Android Studio Run Pixel_4,38baseline checks PASS. Fresh/Home→Start HP10/10,Damage1,Speed10,Sword1,DashLv1 READY/UltimateLv1 initial15s,progress0,random seeds khác nhau,debugfalse/no overlay. Root trigger files vắng mặt, game_language/prefs không đổi. Stale diagnostic Intent dù còn namespace vẫn rơi về baseline chuẩn.
- SkillConfig có bảng Lv1–5 immutable; View.level riêng từng skill. Dash cooldown5/4,6/4,2/3,8/3,5, distance75/80/86/93/100, iframe0,17/0,17/0,18/0,18/0,20. Ultimate CD30/28/26/24/22,radius105/115/125/135/145,multiplier2,5/2,9/3,3/3,7/4,2,knockback25/27/29/30/33; minDamage4/tank35%/Boss0. Active snapshot giữ config cũ; upgrade scale remaining và recharge window theo newCD/oldCD. Không level theo thời gian/Boss, Retry về1, không lưu level vào preferences hoặc weapon slots.
- Skill Box riêng dùng tinted licensed Kenney chest + cyan star, spawn đầu35–45progress giây, sau40–55,life60gameplay giây,cap2 riêng ngoài6ordinary boxes. RNG riêng giữ pool/schedule cũ. CHOOSING_SKILL/choosingReward đóng băng simulation; hộp pending đến commit, offers snapshot/current→new, token âm không trùng Weapon Box token dương. Max bỏ choice, cảhai max ngừng spawn/retire leftovers. Không sửa Weapon Box rewards/Boss HP/AI/progress/Victory.
- HUD tên+Lv,CD/gauge,geometry/hit-test Task06 giữ nguyên; SkillRewardHud hai cards dùng WeaponHud geometry. GameInput modal API tên cũ trên GameScreen route cảhai loại reward; Antigravity cần phân biệt session.choosingSkill/choosingWeapon khi draw. VFX snapshot,≤6trailpoints và96ringsegments ởLv4–5,hitFXcap12,19textures như cũ; vi/en/font-fit qua. Ba optional audio hooks vẫn silent,49JSON/mapping/assets không đổi.
- Focused checks PASS: isolation20,upgrades1690,skills1473,joystick/localization2718,weapon223,pickup29227,Boss2901,audio190,GL219. Android43checks PASS: trận chuẩn EasyDemo tự spawn/nhặt/nâng DashLv2 không fixture; diagnostic riêng nhận8upgrades qua actual boxes/modal,two-pointer,Lv3/5/Boss,ACTIVE snapshot,Pause/background,GameOver/Retry và stale lease replay. Cả9 preferences/legacy paths unchanged, namespace owned cleanup, sau đó baseline38checks qua lại.
- Probe HardDemo2Boss: windows gameplay khoảng23,08–25,37FPS; window UltimateLv5≈25,00FPS,37active samples mean43,10ms/max75,10ms. Background max821,75ms giữ riêng trong raw log, không quy cho skill. Không cam kết60FPS/thiết bị thật hoặc full10min balance; không chạy82stress, không export/manual APK install/commit/push. Bảng config, root cause, evidence, limits và UI API: [TASK_06_1_SKILL_UPGRADES_REPORT.md](TASK_06_1_SKILL_UPGRADES_REPORT.md).


## Cập nhật Task 07 — 15/11/2026

- WeaponInstance thêm `evolved` độc lập level1–5. Lv5 chỉ eligible; Weapon Box kế tiếp ưu tiên mọi owned evolution, actual token/commit mới evolve. `WeaponOffer.action()` phân biệt UNLOCK/UPGRADE/EVOLUTION/FALLBACK, Kind.WEAPON giữ tương thích, Evolution5→5. Ba slot, fallback và Skill Box riêng giữ nguyên; Retry Sword1.
- Vòng Kiếm: hai blade R42,3.5rad/s, target CD0.55s,max6 hit/update, LOS; bỏ sword cũ. Mưa Tên:5 lane thẳng,2pierce/lane, dedup toàn fan. Thiên Thạch:1meteor,R58,max12 blast targets; ground2.25s/tick0.5s,max3zones/max12targets/tick, overlap max damage. Lôi Võng:3root,2hop/branch,max8unique/activation, LOS. Tuning chỉ evolved trong EvolutionConfig; Lv1–5/Boss HP/AI/caps không đổi.
- Tất cả damage qua Enemy.damage/burst guard/death hook. Pool projectile/effect48 giữ nguyên; cap burst ngăn ba evolved xóa60 cùng một update. Audio49 mapping/JSON không sửa, skill optional cues vẫn silent. VFX chỉ sáu licensed strips/24frames vào atlas256²,+0.25MiB RGBA,20textures/44,548,100 bytes. VI/EN evolution title/description và slot TH/EV, giữ modal/hit-test/joystick UI02.
- DiagnosticRun.NONE và one-use DiagnosticLauncher của Task06.1 giữ nguyên. Android fixture chỉ trong own UUID, actual boxes/touch để lên cấp, không free equipment hoặc root writes. Core EvolutionChecks10,527 và GL3,748 PASS; regression skills/weapons/pickups/Boss/audio PASS. Android73 và fresh-baseline38 PASS, HP10/Sword1/debugfalse/random seed sau cleanup; nine original-state paths unchanged.
- Bản cuối dùng Android Studio Run Pixel_4; FPS/residents và giới hạn ghi trong [TASK_07_WEAPON_EVOLUTION_REPORT.md](TASK_07_WEAPON_EVOLUTION_REPORT.md). Chưa chứng minh evolution trong run sinh tồn không hỗ trợ, chưa full10min balance/physical device/full82 stress. Không Task08, release/export hoặc commit/push. Antigravity đọc evolved/offer/action và commit qua GameScreen.chooseWeapon; không mutate level từ UI.

## Cập nhật UI03 — 15/11/2026

- HUD dùng chung `HudStyle` cho khung góc pixel và primitive icons; không thêm texture/font/atlas. HP toàn hàng, Damage/Speed/kills fit trong vùng riêng; Pause, slots và skill tôn trọng safe insets. Joystick giữ owner/activation/deadzone; chỉ đổi vành/knob. Pickup/Boss markers tránh dải Boss, slot, toast và nút skill.
- Dash/Ultimate có icon cyan/vàng, Lv tab, READY/ACTIVE/countdown và recharge ring; tâm/radius/hit-test Task06 giữ nguyên. Skill Box dùng licensed chest cyan/đai tím/sao, modal HỘP KỸ NĂNG/SKILL BOX có trước→sau và max status không tương tác. Weapon slot hiển thị tên evolved đầy đủ, badge TH/EV và viền vàng từ `evolved()`; modal phân biệt UNLOCK/UPGRADE/EVOLUTION/FALLBACK. Không đổi reward/token/commit hoặc GameInput.
- Sửa blending ở lượt shapes modal sau `SpriteBatch.end()` để highlight không che chữ. Pause/Victory/Game Over đồng bộ khung/nút, số liệu thật. VI/EN glyph/fit qua; không sửa preferences, audio49/mapping/assets, combat/AI/tuning hoặc normal diagnostic isolation.
- GL HUD3.425/evolution4.129 assertions PASS; max4 draw calls riêng HUD, vẫn20 textures/44.548.100 bytes. Android68 UI/input PASS, focused build cuối8 PASS, normal baseline38 PASS và các regression có trọng tâm PASS. Android Studio Run Pixel_4 bản cuối21:35:48, deploy21:35:52–53; không export/manual install/commit/push.
- Mẫu ngắn cùng seed42/emulator trước→sau:23,91→26,13FPS,41,83→38,26ms mean frame; HUD CPU ghi3,65→4,94ms. Không thấy giảm FPS toàn frame trong mẫu, không suy ra tối ưu GPU/60FPS hoặc điện thoại thật. Evidence VI/EN, cleanup namespace và giới hạn: [UI03_GAMEPLAY_HUD_REPORT.md](UI03_GAMEPLAY_HUD_REPORT.md). Không tự chuyển Task08.

## Cập nhật Task08 — 15/11/2026

- `EnemyDefinition.skill` là policy authoritative:49 loài có skill,33 NONE/contact-only;23 preset dùng chung +NONE. `EnemySystem` sở hữu scheduler/pool, actor có instance PREPARE/RELEASE/RECOVER/CD. Cast lock movement; Hurt/Death/knockback hủy pending release. Release sau player weapons/Ultimate. Actor Boss luôn NONE, không kế thừa skill từ body species.
- Đạn thường chốt normalized velocity khi fire, update tuyến tính; fan3/burst5 chốt cả volley một lần. AoE snapshot target lúc bắt đầu, không chạy theo Player. Swept Player/wall collision, impact tiêu thụ cả khi iframe chặn; không damage đồng minh. Pool riêng24 shots/24 FX/3 poison areas, Boss24 và player48 giữ nguyên. Caps40/60 và A*2/frame không đổi; cast/telegraph budgets chung, có gate đầu/mid/night/late và vùng camera dành chỗ HUD.
- Poison2,4s/tick1s, Burn1,8s/tick1s, Slow1,5s/normal movement×0,75; refresh-only, không stack potency. Direct/status/DoT qua Player.damage/immunity/Dash. Slow không rút Dash hoặc khóa Ultimate. Pause/modal freeze; death/Winning/Home/Retry clear. UI03 chỉ thêm status pills/overlay, không dời controls hoặc modal.
- FX thật:81 strip/icon/274 frames, một atlas512² Nearest/extrude, +1MiB, tổng21 textures/45.596.676 bytes RGBA. License/provenance giữ nguyên, không AI processing/resample. Không thêm AudioEvent/SFX hoặc đổi49 selections/36 files; Enemy/Boss/Player cues cũ giữ nguyên.
- EnemySkillChecks3.772, EnemySkillSmoke4.418, EvolutionSmoke3.778, UpgradeSmoke219 và focused Boss/skills/evolution/audio/input/isolation PASS. Android68 và normal38 PASS. Trace năm ID33–37 giữ nguyên velocity khi Dash thật, joystick owner0 giữ; hit pool thường5→5 trong mẫu. One-use UUID diagnostic, byte-equal preferences/8 legacy paths và không fixture đóng gói.
- Android Studio Run Pixel_4 bản cuối22:43:39, deploy22:43:44. Đối chứng đầu trận25,92→25,19FPS, mean38,57→39,69ms (−2,81% FPS); scene active22,50–25,15FPS với9–24 normal và0/1 Boss. Không cam kết60FPS/physical device/full-match balance. Chi tiết, scene population/CPU, lỗi harness đã archive và giới hạn: [TASK_08_ENEMY_SPECIAL_SKILLS_REPORT.md](TASK_08_ENEMY_SPECIAL_SKILLS_REPORT.md), [ENEMY_SKILL_ASSIGNMENT.md](ENEMY_SKILL_ASSIGNMENT.md), [ENEMY_FX_ASSET_MAPPING.md](ENEMY_FX_ASSET_MAPPING.md). Không tự chuyển Endless/Task tiếp theo.

## Cập nhật UI04 — 16/11/2026

- **Sửa triệt để lỗi Dialog lồng border và vỡ layout:** Xóa `android:background="@drawable/panel_fantasy_frame"` trong `Theme.MonsterSurvivor.Dialog` (nguyên nhân khiến mọi view con của `AlertDialog` bị lặp viền vàng và phình padding). Sử dụng `android:windowBackground="@drawable/dialog_fantasy_window_bg"` với một khung duy nhất (`inset` + viền 2dp vàng kim trên nền tối bán trong suốt) và đặt `android:background="@android:color/transparent"`.
- **Ràng buộc kích thước Dialog Landscape:** Bổ sung `dialog_min_width_major` (440dp) và phương thức `UiUtils.styleDialog(AlertDialog)` giới hạn chiều rộng tối đa 440–480dp / 70% màn hình ngang, tránh việc dialog chiếm trọn màn hình. Nút bấm được định kiểu chữ rõ ràng, độ tương phản cao, không bao giờ bị cắt chữ.
- **Rà soát Native UI:** Chuẩn hóa `panel_fantasy_frame.xml` stroke 2dp; tối ưu padding và thuộc tính chống cắt chữ trên các nút của `HomeActivity`, `GameModeActivity`, `SettingsActivity`; ẩn nút xem trước giả lập `btn_preview_screens`; hoàn thiện giao diện Thành tích chân thực với empty state và handoff `CODEX_ACHIEVEMENTS_BACKEND_HANDOFF.md`.
- **Biên dịch cú pháp:** Đã compile sạch `:core:compileJava` và `:android:compileDebugJavaWithJavac` (Exit code: 0). Báo cáo chi tiết: [UI04_FIX_REPORT.md](UI04_FIX_REPORT.md). Không commit/push. Dừng lại để Chủ dự án tự kiểm thử thực tế.
