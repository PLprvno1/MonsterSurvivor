# TASK 04A.1 — Gameplay difficulty, floating joystick & localization foundation

Ngày thực hiện: 14/11/2026, Asia/Saigon. Project: `D:\Android\MonsterSurvivor`.

## 1. Kết quả và phân tích trước khi sửa

Đã tăng áp lực theo progress bằng cấu hình tập trung; giữ phần làm quen Easy, cap resident 40/60, 82 định nghĩa và unlock cũ. Joystick nhận chạm trong vùng rộng bên trái, tâm đặt tại ngón tay, có pointer ownership/deadzone/reset. Core có 60 translation key vi/en; Android đọc một key ngôn ngữ từ preferences hiện có. Chưa thêm bộ chọn ngôn ngữ hoặc thiết kế HUD/native XML; phần đó giao Antigravity UI02.

Các nguyên nhân có thể góp phần làm người chơi thấy dễ, từ source thực tế:

- Trước task, target đầu trận Easy/Hard chỉ 6/7 và spawn 1,8/1,5 giây thực; mốc tăng stage đầu tiên ở progress 120. Không phải mọi frame đều có đủ target vì quái đang chết, bị giết và cần thời gian spawn/tiếp cận.
- HP cơ bản nhiều loài chỉ 1–3; trước progress 300, phần lớn quái thường chưa nhận bonus HP. Weight gốc của Slime/Bat cao, chưa có hệ số tăng ưu tiên nhóm bền theo stage ngoài hard/night factor của định nghĩa.
- Kiếm cơ bản có damage 1, cycle 1 giây, hit sau 0,15 giây, range 48/detection 60; vừa di chuyển vừa tự đánh. Tốc độ player ban đầu 80, cao hơn cả các quái nhanh. Hard trước task chỉ nhân speed 1,08.
- Damage Box +1/+2/+3 tăng base damage cho **mọi** vũ khí; level vũ khí tiếp tục nhân base đó. Một hoặc hai hộp có thể biến nhiều quái thấp HP thành một hit. Box spawn mỗi 15–20 progress giây; Demo tăng lịch này nhưng không tăng cooldown vật lý.
- Health Drop giữ xác suất Easy 8% / Hard 5%, cộng 4 điểm phần trăm khi HP ≤3, hồi 1–2 HP. Health Box hồi 3 HP hoặc tăng Max HP 2 và hồi 2. Các cơ chế này hỗ trợ farm lâu; chúng không bị giảm trong task.

Mô phỏng đứng yên **bản cũ** trên map 200×200 seed 42/43, player HP10 và kiếm cơ bản, đã thua Easy sau 50,95/81,95 giây, Hard sau 22,00/19,75 giây. Vì vậy không kết luận bản cũ cho phép đứng yên an toàn suốt trận. Phản hồi người chơi liên quan cả di chuyển/nâng cấp; mô phỏng không chứng minh cảm giác chơi của người thật. Thay đổi tập trung vào áp lực từ phút 1–2, độ bền/mix quái và phân biệt Hard, không nhân đôi HP hàng loạt.

## 2. Population và cadence trước/sau

Target là mục tiêu resident, không phải số quái sống bảo đảm. Spawn interval dùng giây thực ở cả Normal/Demo; stage được chọn bằng `GameSession.progress()`. Mỗi update chỉ tạo tối đa một quái, không có burst để bù nhiều lượt đã trễ.

| Progress s | Target Easy c? -> m?i | Target Hard c? -> m?i | Spawn Easy gi?y c? -> m?i | Spawn Hard gi?y c? -> m?i |
| ---: | --- | --- | --- | --- |
| 0 | 6 -> 6 | 7 -> 8 | 1.80 -> 1.80 | 1.50 -> 1.35 |
| 60 | 8 -> 9 | 10 -> 12 | 1.80 -> 1.45 | 1.50 -> 1.08 |
| 120 | 10 -> 12 | 13 -> 16 | 1.50 -> 1.20 | 1.20 -> 0.88 |
| 210 | 15 -> 17 | 21 -> 22 | 1.35 -> 1.08 | 1.10 -> 0.80 |
| 300 | 20 -> 23 | 29 -> 30 | 1.15 -> 0.95 | 0.95 -> 0.72 |
| 420 | 28 -> 32 | 41 -> 43 | 1.15 -> 0.85 | 0.95 -> 0.65 |
| 450 | 30 -> 34 | 44 -> 46 | 1.00 -> 0.85 | 0.85 -> 0.65 |
| 540 | 36 -> 40 | 54 -> 55 | 1.00 -> 0.80 | 0.85 -> 0.60 |
| 599 | 39 -> 40 | 60 -> 60 | 1.00 -> 0.80 | 0.85 -> 0.60 |

Easy giữ nguyên 0–60, sau đó target thường tăng khoảng 10–20% so với cũ trước khi chạm cap. Hard target cao hơn Easy khoảng 29–35% tại phần lớn mốc giữa trận; cuối trận tỷ lệ có thể lớn hơn khi Easy đã chạm 40 trong khi Hard còn tăng tới 60. Đây là lựa chọn có giới hạn, không ép tỷ lệ 20–35% một cách máy móc khi hai cap khác nhau.

Nhịp tăng target của stage mới Easy/Hard lần lượt: `0: 30/20`, `60: 20/15`, `120: 18/14`, `210: 15/12`, `300: 14/10`, `420: 12/10`, `540: 12/10` progress giây cho một resident. Không tăng cap hoặc budget A*.

### HP, speed, damage và trọng số

Bonus HP sau đây chỉ áp dụng khi **spawn mới**, không tăng/hồi HP quái đang đánh dở. Ba cột bonus giữ adapter Slime/Bat/Boar; quái registry thường dùng cột Slime, tank/elite dùng cột Boar.

| Progress bắt đầu | Bonus HP Easy S/B/Boar | Bonus HP Hard S/B/Boar | Strong weight E/H | Elite weight E/H |
| ---: | --- | --- | --- | --- |
| 0 | 0/0/0 | 0/0/0 | 1,00 / 1,15 | 1,00 / 1,00 |
| 60 | 0/0/0 | 0/0/0 | 1,05 / 1,30 | 1,00 / 1,00 |
| 120 | 0/0/1 | 1/0/2 | 1,20 / 1,60 | 1,00 / 1,10 |
| 210 | 0/0/2 | 1/1/3 | 1,35 / 1,85 | 1,05 / 1,20 |
| 300 | 1/1/3 | 2/1/4 | 1,55 / 2,15 | 1,10 / 1,30 |
| 420 | 1/1/4 | 2/2/5 | 1,85 / 2,50 | 1,15 / 1,40 |
| 540 | 1/1/4 | 2/2/5 | 2,00 / 2,70 | 1,20 / 1,50 |

`strong` nghĩa là base Easy HP ≥3, hoặc tank, hoặc elite. Weight cuối = base weight × biến thiên seed × hard factor × night factor × strong/elite stage factor × biome theme nếu có. Elite có thể nhận cả strong và elite factor; base weight hiếm và resident/family cap cũ vẫn hạn chế mật độ. Unlock, Night gate 300 và stop-spawn 600 không đổi.

Ví dụ Tiny Slime Easy vẫn 1 HP trước 300, Hard từ 1 lên 2 ở 120 và 3 ở 300. Moss Golem khi được phép mở ở 420 có HP Easy 10 / Hard 13, so với 7/9 ở cùng progress trước task. Base stats/scale/pivot vẫn ở `tools/enemy_balance.json`; stage overlay nằm trong `GameConfig`, không chỉnh generated `EnemyCatalogData.java` hoặc chạy lại enemy atlas pipeline vì bảng base không đổi. [ENEMY_ROSTER.md](ENEMY_ROSTER.md) đã cập nhật mô tả công thức để không còn ghi hệ số cũ.

Hard speed = base × **1,10**, trước đó ×1,08; Easy ×1. Đây là +10% so với Easy, không phải tăng thêm 10% so với bản Hard cũ. Bat nhanh nhất 57,2 units/s; charge Wild Boar khoảng 68,97, vẫn dưới player ban đầu 80. Không đổi animation/cooldown hoặc vật lý theo Demo.

Ở Hard từ 300, nhóm strong registry có contact damage tối thiểu 2; ba legacy loại nhẹ giữ damage 1. Một số base definition vốn đã damage 2 trước task khi đủ unlock. Không thêm damage >2 hoặc áp damage 2 hàng loạt ở đầu trận. Player HP10 và immunity 0,7 giây giữ nguyên.

### Mật độ quái sống thực trong mô phỏng

Đối chiếu hai phiên bản bằng **source trước task đã lưu** và code hiện tại, cùng seed 42/43, map/góc camera, Normal, đứng yên, kiếm cấp 1, không cấp nâng cấp. Pass riêng giữ HP100000 để lấy đủ sáu cửa sổ; số dưới là mean living, không gồm corpse, trung bình hai seed. Đây là mô phỏng CPU hữu hạn, không phải FPS benchmark hoặc playtest mười phút của người thật.

| Progress s | Mean living Easy c? -> m?i | Mean living Hard c? -> m?i |
| --- | ---: | ---: |
| 0-60 | 4.22 -> 4.22 | 6.17 -> 7.30 |
| 60-120 | 4.77 -> 5.84 | 9.11 -> 11.09 |
| 120-300 | 8.04 -> 12.05 | 13.98 -> 20.11 |
| 300-420 | 16.26 -> 21.62 | 23.93 -> 33.18 |
| 420-540 | 20.61 -> 30.04 | 28.46 -> 45.07 |
| 540-600 | 25.28 -> 34.96 | 32.06 -> 55.25 |

Hard có living cao hơn Easy rõ ràng; tỷ lệ living không bằng tỷ lệ target vì quái Hard sống lâu hơn. Không coi số liệu này là bằng chứng cảm giác chơi đã được cân bằng hoàn hảo. Pass HP10 sau sửa: Easy seed42 vẫn 50,95 giây, seed43 88,85; Hard cả hai seed 19,25. Seed43 Easy sống lâu hơn dù target tăng cho thấy kết quả còn phụ thuộc mix/đường đi, không thể suy từ một seed. Damage Box vẫn tăng damage cùng lượng như cũ và có ý nghĩa khi đối mặt quái bền.

Bằng chứng: [balance-before.json](core/build/reports/task04a_1/balance-before.json), [balance-after.json](core/build/reports/task04a_1/balance-after.json), tool `tools/run_balance_comparison.py`. Không có quái vượt cap trong check spawner thực; vị trí spawn vẫn kiểm tra navigation/footprint.

## 3. Floating joystick và API cho Antigravity

`GameInput` nhận touch bằng screen pixel rồi gọi `Surface.unproject`, sử dụng HUD units với gốc **dưới trái**. `GameHud` hiện thực `GameInput.Surface`; layout vẫn dùng ExtendViewport tối thiểu 800×480, chiều rộng thực thay đổi theo màn hình.

Vùng kích hoạt: `x ∈ [0, width × 0.44]`, `y ∈ [28, height − 96]`. Dành 96 HUD units phía trên cho HUD và 28 dưới cho footer. Pause/debug button và mọi modal được xử lý **trước** vùng joystick. Chạm ngoài vùng không nhận pointer mới, nhưng pointer đang sở hữu vẫn được kéo qua nửa phải/ra ngoài vùng.

- Down hợp lệ: giữ pointer ID, tâm = điểm chạm, vector bằng 0. Chạm không kéo không làm player di chuyển.
- Drag: vector từ tâm ban đầu, chia bán kính 66 HUD units, clamp magnitude ≤1; radial deadzone 0,12. Giữ analog magnitude ngoài deadzone, không méo chéo và không chờ animation.
- Ngón thứ hai không giành pointer; release/cancel của ngón khác không ảnh hưởng owner.
- Owner Up/Cancel: xóa pointer, vector/axes về 0 ngay; bàn phím đang giữ vẫn có thể tiếp quản ở lần sample tiếp theo.
- Pause/Back, modal, DYING/Game Over, Home, hide/resize/lifecycle và Retry xóa input/pointer/key cũ. Sau lifecycle cần Resume rõ ràng, không tự chạy lại. WASD/phím hướng giữ normalization chéo.
- Khi nghỉ chỉ có vòng gợi ý alpha 0,12 ở `(108,108)`; khi giữ mới vẽ base/knob tại tâm nổi. Đây là presentation tối thiểu để thử logic, không phải HUD UI02 hoàn thiện.

| API | Ý nghĩa |
| --- | --- |
| `GameInput.Surface` | width/height, unproject và hit-test các nút/card hiện có; không phụ thuộc renderer cụ thể |
| `inJoystickRegion(x,y)` | Tọa độ HUD; kiểm tra vùng khởi tạo, không dùng để cắt drag của owner |
| `activePointer()` / `joystickActive()` | `-1` khi nghỉ / trạng thái owner |
| `centerX()/centerY()` | Tâm HUD units; về vị trí hint khi nghỉ |
| `knobX()/knobY()` | Độ lệch tương đối với tâm, tối đa radius 66 |
| `axisX()/axisY()` | Vector di chuyển sau `sampleAxes()`; PlayerStats vẫn quyết định tốc độ |
| `reset()` | Hủy input, modal selection pointer và key state khi luồng bị ngắt |

Antigravity chỉ cần thay renderer joystick theo các getter này và duy trì Surface/Commands. UI ưu tiên mới phải được route trước khi nhận joystick; không cắt owner drag tại biên 44%, không tạo input processor thứ hai cùng điều khiển player. Modal vẫn dùng pending ID/token và commit reward cũ.

## 4. Localization contract

Core mới có `localization/GameLanguage` (`VI`, `EN`) và immutable `GameStrings`, không import Android Context/R.string. `GameConfig.language` / `GameConfig.strings` giữ ngôn ngữ của trận.

```java
GameConfig vi = new GameConfig.Builder().language("vi").build();
GameConfig en = new GameConfig.Builder().language(GameLanguage.EN).build();
String pause = en.strings.get(GameStrings.Key.PAUSE);
String level = en.strings.format(GameStrings.Key.WEAPON_LEVEL, "BOW", 3);
```

Mặc định/fallback là `vi` cho null, rỗng hoặc mã không hỗ trợ. Chấp nhận trim/case của `vi`/`en`; không tự đổi Locale toàn hệ thống. `format` dùng Locale.ROOT và cùng placeholder giữa hai bản dịch. Ngôn ngữ cố định trong config của trận; Retry giữ config, thay đổi Settings áp dụng khi tạo trận mới. Difficulty/mode, RNG, stats và reward không phụ thuộc text.

60 key đều có hai bản dịch. Các nhóm:

- Stats: `HP`, `DAMAGE`, `SPEED`, `KILLS`, `TIME`, `DAMAGE_SHORT`, `SPEED_SHORT`, `FPS`.
- Flow: `PAUSE`, `PAUSED`, `RESUME`, `RETRY`, `HOME`, `GAME_OVER`, `SURVIVED`, `CHOOSE_WEAPON`.
- Modes: `NORMAL`, `DEMO`, `EASY`, `HARD`.
- Weapons: bốn name, bốn `_SHORT`, bốn `_NEW`, bốn `_UPGRADE`; `LEVEL`, `WEAPON_LEVEL`, `NEW_WEAPON`, `UPGRADE_WEAPON`, `EMPTY_SLOT`, `WEAPON_HINT`.
- Fallback rewards: `FALLBACK_DAMAGE/HP/SPEED` và các `_DESC` tương ứng.
- Pickup: `PICKUP_DAMAGE/MAX_HP/HEAL/SPEED`, `DAMAGE/HEALTH/SPEED/WEAPON_ICON`.
- Controls/debug: `CONTROLS`, `DEBUG_CONTROLS`, `ANIMATION`, `DIRECTION`.

Các placeholder: `WEAPON_LEVEL(name,level)`, `NEW_WEAPON(name)`, `UPGRADE_WEAPON(name,from,to)`, các `PICKUP_*` nhận số tăng thực tế. Toàn bộ chữ và API tên/icon vũ khí nằm tại [GameStrings.java](core/src/main/java/com/example/monstersurvivor/localization/GameStrings.java). HUD tạm, offers/descriptions/fallback và feedback pickup đã dùng keys. Enum/ID trong debug log vẫn là định danh kỹ thuật; không dùng chúng làm label người chơi. Không đổi reward handler, offer eligibility, level cap hoặc modal token.

### Android Settings → core

Preferences hiện có là **`monster_survivor_prefs`**; trước task chưa có language key. Dùng duy nhất **`game_language`**, giá trị canonical `vi` hoặc `en`.

```java
GamePreferences prefs = GamePreferences.getInstance(context);
prefs.setGameLanguage("en");       // UI02 selector gọi hàm này
String code = prefs.getGameLanguage();
```

`GamePreferences.KEY_GAME_LANGUAGE` là constant chung; `resetDefaults()` trả về `vi`. Getter xử lý cả string không hợp lệ lẫn giá trị sai kiểu SharedPreferences bằng fallback `vi`. `AndroidLauncher` đọc getter rồi gọi `GameConfig.fromIntent(difficulty, mode, language)`. Core không đọc SharedPreferences. Không thêm Intent key ngôn ngữ thứ hai, không sửa extras EASY/HARD–NORMAL/DEMO.

**Bộ chọn ngôn ngữ Native Settings và resource native vi/en chưa được thêm**, đúng phân công UI02. Native Home/Settings hiện vẫn dùng giao diện/chuỗi cũ; config ngôn ngữ trong task được kiểm tra bằng preferences tạm và config nội bộ, không tuyên bố đã có ứng dụng native song ngữ hoàn chỉnh.

### Font tiếng Việt

Font libGDX mặc định `lsans-15` có 168 glyph và không có glyph thật cho `ệ`, `đ`; missing-glyph fallback có thể khiến `hasGlyph()` trả true, nên kiểm tra dùng `getGlyph()` thật. Đã thay font **HUD tạm** bằng một BMFont Roboto 15px, 261 glyph NFC Latin/Vietnamese, lineHeight 18, Nearest. Không đổi tọa độ/màu/layout của HUD ngoài joystick tối thiểu.

Font nguồn lấy offline từ `/system/fonts/RobotoStatic-Regular.ttf` của Pixel_4, không tải Internet. Copyright 2011 Google; name table và Android NOTICE xác nhận Apache-2.0. Tool `tools/prepare_ui_font.py` / `PrepareUiFont.java` tạo bitmap monochrome; source TTF chỉ nằm trong build evidence, không đưa vào runtime. Runtime chỉ thêm `game-ui.fnt`, `game-ui.png` và `Roboto-LICENSE.txt` dưới `assets/game/fonts/`, tổng khoảng 43,5 KB trên đĩa.

Một trang font 512×256 = 0,5 MiB RGBA, thay trang cũ 256×128 = 0,125 MiB, tăng **0,375 MiB**. Font load một lần khi tạo GameHud, reuse qua Retry và dispose cùng HUD; không có font engine mới hoặc sinh glyph mỗi frame. GameAssets vẫn 19 texture / 42,23 MiB; trang HUD được quản lý riêng như trước, tổng hai phần khoảng 42,73 MiB, không phải Java/native heap/PSS.

Đã kiểm tra glyph thật cho mọi ký tự của cả 60 key, xác nhận NFC, độ rộng mô tả vừa card hiện có và xem ảnh chỉ chứa chữ: [vi](core/build/reports/task04a_1/localization-vi.png), [en](core/build/reports/task04a_1/localization-en.png). Không đọc lại artwork Tiny Questers/N3XUS vào agent. UI02 thêm text mới cần giữ NFC/glyph coverage hoặc bổ sung font tương ứng.

## 5. Kiểm thử

| Kiểm tra | Kết quả |
| --- | --- |
| Core + Android Java compile offline | Qua; chỉ warning SDK XML đã có |
| Task04A1Checks | 2.534 check qua: stage/cap/weights/seed/stats, actual opening Sword, movement multiplier, Demo clock, pointer/modal/keyboard, vi/en/fallback và rewards |
| Task04A1Smoke / GameScreen OpenGL | 1.781 check qua: glyph/text, viewport floating movement, tap/release, modal overlap, Back/lifecycle, Game Over/Retry/Home, language giữ qua Retry ở cả vi/en |
| WeaponChecks / WeaponSmoke | 223 / 45 check qua; offers/upgrade/modal/commit không đổi |
| PickupChecks | 21.438 check qua; heal/drop/reward/cap không đổi |
| Task021Checks phase b | 1.841 check qua; cập nhật expected balance có chủ đích, không chạy stress phase khác |
| Android Studio Run + native smoke | 44 check qua, chi tiết bên dưới |

Không chạy lại full stress suite 82 quái, atlas import, map stress hoặc weapon stress không liên quan vì registry/animation/map/WeaponConfig không đổi. Mô phỏng không được coi là bằng chứng người thật thấy cân bằng tốt hơn.

### Android thực tế

Đã bấm **Run (Shift+F10)** cấu hình `android` trên Pixel_4. IDE deployment cuối lúc **13:28:57–58 ngày 14/11/2026, Asia/Saigon**; runtime log xác nhận roster82, texture19, language và initial/cap/movement multiplier mới. Chạy offline, không assemble/bundle/export/release/signing như tác vụ riêng, không cài thủ công hoặc xuất APK/AAB bàn giao.

Native menu → Play đã qua năm trường hợp:

| Giá trị preferences tạm | Difficulty / mode | Ngôn ngữ core nhận |
| --- | --- | --- |
| Không có key | Easy / Normal | vi |
| en | Hard / Demo | en |
| invalid | Hard / Normal | vi |
| en | Easy / Demo | en |
| int 7, sai kiểu | Easy / Normal | vi |

Trận Easy/Normal seed42 dùng player HP10 và spawn tự nhiên: chạm ở vùng xa joystick cũ không chạy, kéo bốn hướng/chéo làm player di chuyển đúng, owner kéo qua nửa phải vẫn chạy, release dừng; Back/Pause/Resume giữ elapsed. Kiểm tra về Home của hệ điều hành khi đang kéo rồi quay lại cùng Activity: chờ Resume, player không tự chạy tiếp. Cuối mỗi case Pause → Native Home; không crash hoặc missing font/asset. Preferences được snapshot/khôi phục, seed/profile flags được khôi phục/xóa và app về Native Home.

Multitouch ownership/cancel và modal precedence được kiểm tra core/GL bằng nhiều pointer ID; Android ADB gesture mới chỉ kiểm tra một pointer, chưa phải thao tác hai ngón đồng thời. Game Over/Retry đã kiểm tra GameScreen desktop, không gán thành kiểm tra touch Retry Android mới.

Bằng chứng: [android-smoke.json](core/build/reports/task04a_1/android-smoke.json), [logcat](core/build/reports/task04a_1/android-logcat.txt), [studio-deploy](core/build/reports/task04a_1/studio-deploy.txt), các log riêng trong `core/build/reports/task04a_1/`.

## 6. FPS ngắn

Pixel_4 API37, SwiftShader, 2280×1080, map200×200 seed42 và zoom1,15. Sáu stage 10/30/60 mixed species × day/night, mỗi stage 30 warm-up +60 đo. Cùng recipe diagnostic Task04A: HP cao để giữ population, Sword1/Fire5/Lightning5, lighting ép; không phải natural match, không có GPU timer. Chạy độc lập với compile/GL tests.

| Population / lighting | FPS callback | Mean frame ms | p95 ms | CPU AI+A*+separation ms | CPU world render ms |
| --- | ---: | ---: | ---: | ---: | ---: |
| 10 day | 26.33 | 37.98 | 57.19 | 0.64 | 16.16 |
| 10 night | 25.32 | 39.49 | 55.09 | 0.48 | 14.66 |
| 30 day | 26.17 | 38.22 | 58.67 | 1.31 | 15.09 |
| 30 night | 24.01 | 41.65 | 56.65 | 1.02 | 15.78 |
| 60 day | 24.39 | 41.00 | 54.58 | 1.97 | 17.45 |
| 60 night | 23.43 | 42.69 | 57.18 | 1.81 | 16.83 |

Không thấy regression FPS nghiêm trọng trong mẫu này; **không kết luận task cải thiện FPS** vì phép đo cách thời điểm, host load và pacing emulator khác nhau. Bản Task04A trước ghi khoảng 21,31–25,13 FPS; bản này 23,43–26,33, vẫn chưa đạt 60 FPS. World CPU render khoảng 14,66–17,45 ms; AI+A*+separation khoảng 0,48–1,97 ms. Tối đa A* search trong probe là 0 do bố trí có LOS; không suy ra hiệu năng đường vòng nặng.

CSV/summary: [android-roster.csv](core/build/reports/task04a_1/android-roster.csv), [android-roster-summary.json](core/build/reports/task04a_1/android-roster-summary.json). Population thực được giữ đúng 10/30/60, assets19, không burst/quá cap. Chưa đo GPU thiết bị thật hoặc phiên dài có nâng cấp đầy đủ.

## 7. File sửa và bảo toàn phạm vi

Core dưới `core/src/main/java/com/example/monstersurvivor/`:

- Cấu hình/AI/spawn: `config/GameConfig.java`, `ai/EnemySystem.java`, `spawn/EnemySpawnProfile.java`, `spawn/EnemySpawner.java`.
- Input/UI tạm: `input/GameInput.java`, `ui/GameHud.java`, `ui/WeaponHud.java`.
- Localization mới: `localization/GameLanguage.java`, `GameStrings.java`.
- Chuyển text, giữ logic reward: `weapon/WeaponOffer.java`, `weapon/WeaponSelection.java`, `pickup/PickupSystem.java`, `gameplay/GameSession.java`.
- Wiring/lifecycle/diagnostic: `screen/GameScreen.java`, `debug/CombatProfile.java` (giữ language khi tạo diagnostic config).
- Android chỉ `AndroidLauncher.java`, `model/GamePreferences.java`; không sửa SettingsActivity/HomeActivity/XML/theme/Manifest/Gradle.
- Asset chỉ font tối thiểu nêu trên và generated `assets/assets.txt`; không sửa sprite, enemy/pickup atlas/catalog hoặc source artwork.
- Tools mới: `PrepareUiFont.java`, `prepare_ui_font.py`, `checks/BalanceProbe.java`, `checks/Task04A1Checks.java`, `smoke/Task04A1Smoke.java`, `run_task04a_1_checks.py`, `run_balance_comparison.py`, `run_studio_task04a_1.ps1`, `android_task04a_1_smoke.py`, `profile_task04a_1_android.py`.
- Tools hiện có: `checks/Task021Checks.java` cập nhật expected balance; `write_enemy_reports.py` cập nhật mô tả stage formula để lần regenerate sau không ghi hệ số cũ. Tài liệu: báo cáo này, ghi chú `ENEMY_ROSTER.md` và phần bổ sung `INTEGRATION_NOTES.md`.

Đối chiếu SHA-256 baseline tại [final-scope-audit.json](core/build/reports/task04a_1/final-scope-audit.json): enemy definitions/registry generated, base balance JSON, source asset, sprite/atlas/catalog cũ, map generator, PixelCamera, NightRenderer, WeaponConfig, PlayerStats, pickup config/render và báo cáo cũ giữ nguyên. Không thay GDD hoặc commit/push.

## 8. Hướng dẫn Run và phần giao UI02

1. Trong Android Studio chọn cấu hình module `android`, Pixel_4, bấm Run. Chọn Easy/Hard–Normal/Demo qua Native Home như hiện tại.
2. Thử chạm/kéo tại nhiều điểm trong 44% bên trái, dưới HUD, kể cả xa vòng gợi ý; kéo qua bên phải, chạm không kéo, thả/cancel, hai ngón, Pause/Back/lifecycle và modal. Retry phải xóa input nhưng giữ ngôn ngữ.
3. Antigravity dùng `GamePreferences.setGameLanguage("vi"/"en")` trong selector Settings, tạo trận mới để kiểm tra core. Hiện chưa có selector cho người chơi bấm; kiểm thử nội bộ dùng `GameConfig.Builder.language(...)` hoặc script Android snapshot preferences.
4. So sánh áp lực cùng seed/progress; chạy thử có di chuyển và nâng cấp thực, đặc biệt 2–5 và sau 5 phút. Dữ liệu mô phỏng hiện là điểm khởi đầu, chưa thay playtest.

Giao Antigravity UI02: thiết kế vòng/knob mờ và trạng thái active theo getter, hoàn thiện HUD chính thức; selector Việt/Anh trong Settings; native resources/translation UI; nhãn/dàn dòng/độ dài text, các lỗi HUD đã biết; cập nhật vùng hit-test nếu thêm nút, vẫn ưu tiên modal và reset input. Tái dùng key preferences, GameStrings và input logic hiện có. Không cần viết lại movement hoặc reward handler.

Giới hạn còn lại: native UI chưa song ngữ đầy đủ; chưa có hot-switch ngôn ngữ trong trận; font hiện chỉ cho VI/EN NFC; FPS SwiftShader chưa đạt60; chưa xác nhận cảm giác cân bằng của người chơi sau một trận nâng cấp đầy đủ. Không triển khai boss, enemy special skills, Combat FX, evolution hoặc Task04B.
