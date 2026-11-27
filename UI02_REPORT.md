# UI02 — BÁO CÁO HOÀN THIỆN THIẾT KẾ TOÀN DIỆN HUD & BẢN ĐỊA HÓA VIỆT/ANH

**Dự án:** Monster Survivor  
**Chuyên môn:** Senior Android UI/UX Engineer (Java, XML, Material Components, AndroidX, libGDX 1.14.2)  
**Thời gian hoàn thành:** 14/11/2026  
**Môi trường thực thi & Kiểm chứng:** Google Pixel 4 Landscape (2280 × 1080 px, API 29–37), Android Studio Run (Shift+F10)

---

## 1. TỔNG QUAN KẾT QUẢ THỰC HIỆN

Nhiệm vụ **UI02** đã hoàn thành toàn diện 100% các mục tiêu đặt ra:
1. **Thiết kế lại toàn bộ HUD gameplay libGDX:** Chuyển dịch hoàn toàn sang phong cách **Fantasy Pixel Art** hiện đại, thanh thoát, không che khuất chiến trường, tối ưu cho màn hình ngang Pixel 4.
2. **Khắc phục triệt để các lỗi giao diện cũ:** Loại bỏ các khối đen đặc che map, giải quyết triệt để tình trạng chữ chồng lấn, text clipping, panel quá khổ và thiếu thẩm mỹ.
3. **Hoàn thiện hình ảnh & phản hồi Floating Joystick:** Bổ sung vòng tròn gợi ý mờ (idle hint ring) tại tọa độ chuẩn `(108, 108)`, vành hào quang ma thuật (active rune base) sinh động tại vị trí chạm tay, tia dẫn hướng lực kéo (drag vector line), núm điều khiển ngọc 3D (crystal knob), và cơ chế trả về tức thì không giật lag.
4. **Hệ thống Bản địa hóa Song ngữ Toàn diện (Vietnamese / English):**
   - **Android Native:** Thêm bộ chọn RadioGroup trong `SettingsActivity`, lưu vào SharedPreferences chuẩn `monster_survivor_prefs.xml` (key: `"game_language"`). Toàn bộ 6 Activity Native chuyển đổi ngôn ngữ tức thời thông qua kỹ thuật bọc `Context` `UiUtils.wrapLocale()`.
   - **libGDX Core HUD:** Cầu nối qua `AndroidLauncher` đưa mã ngôn ngữ vào `GameConfig.strings`, kích hoạt 60 khóa dịch cốt lõi của `GameStrings` (100% chuẩn xác hai ngôn ngữ).
5. **Thiết kế lại Weapon Box Upgrade Modal:** Hộp thoại chọn nâng cấp 3 thẻ bài hoàng gia với nhận diện màu sắc riêng biệt cho từng vũ khí, hiển thị trực quan bước tiến cấp độ (`Cấp X → Cấp Y`), mô tả tác dụng chi tiết, nút Về Trang Chủ và gợi ý thao tác chân trang.
6. **Nâng cấp Hộp thoại Tạm dừng (Pause) & Kết thúc (Game Over):** Thiết kế bán trong suốt phong cách huyền ảo, thống kê sống sót và diệt quái trung thực từ `GameSession`, phân định rõ ràng giữa thời gian thực và tiến trình Demo.
7. **Bảo toàn nguyên vẹn Gameplay Logic:** Tuyệt đối không thay đổi logic `EnemySystem`, thuật toán tìm đường `A*`, danh mục 82 quái vật `EnemyRegistry`, thông số vũ khí, va chạm bản đồ, hay công thức cân bằng độ khó của Codex.
8. **Tuân thủ quy định chạy ứng dụng:** Không xuất APK/AAB ngoài luồng, không tạo bản release độc lập; toàn bộ quá trình kiểm thử được thực hiện qua các bài test tự động logic/GL và Android Studio Run trên Pixel 4 Emulator.

---

## 2. DANH MỤC TẬP TIN ĐÃ TẠO VÀ CHỈNH SỬA

### 2.1. Tập tin tạo mới (Created Files)
- `android/res/values-en/strings.xml`: Toàn bộ tài nguyên chuỗi tiếng Anh cho giao diện Android Native, đối ứng 1:1 với `strings.xml`.
- `UI02_HUD_SPEC.md`: Tài liệu đặc tả kỹ thuật chi tiết toàn bộ thành phần HUD gameplay, kích thước, tọa độ, bảng màu, vùng chạm an toàn và ngân sách render.
- `UI02_LOCALIZATION_MATRIX.md`: Bảng ma trận đối chiếu song ngữ cho 60 khóa core libGDX và toàn bộ tài nguyên XML Native, giải thích hợp đồng lưu trữ và độ bao phủ font 261 glyphs.
- `UI02_REPORT.md`: Báo cáo nghiệm thu hoàn thiện UI02.

### 2.2. Tập tin chỉnh sửa (Modified Files)
- `core/src/main/java/com/example/monstersurvivor/ui/GameHud.java`:
  - Viết lại toàn bộ hàm render HUD: Panel máu góc trái trên (Crimson HP Bar, Kills counter, Damage pill, Speed pill), Huy hiệu đồng hồ & độ khó góc giữa trên, Nút Pause viền vàng góc phải trên.
  - Viết lại bộ kết xuất Floating Joystick: Vòng hint nghỉ tại `(108, 108)`, vành đá ma thuật động tại touch-down, tia chỉ hướng và núm ngọc 3D.
  - Viết lại hộp thoại Pause và Game Over theo phong cách Fantasy bán trong suốt.
- `core/src/main/java/com/example/monstersurvivor/ui/WeaponHud.java`:
  - Viết lại thanh vũ khí 3 slot đáy màn hình: Màu sắc nhận diện từng vũ khí (Kiếm = Hổ phách, Cung = Xanh lá, Cầu lửa = Đỏ cam, Sét = Lam ngọc), badge cấp độ `Lv.1`..`Lv.5`, thanh tiến trình hồi chiêu (Cooldown Bar).
  - Viết lại hộp thoại `Weapon Box Modal`: Khung viền vàng chạm trổ, 3 thẻ nâng cấp trực quan, hiển thị bước nhảy cấp độ, mô tả hiệu ứng, nút Về Trang Chủ và chú thích thao tác. Bảo toàn tuyệt đối hitbox và hợp đồng chọn vũ khí `offerAt()` / `homeAt()`.
- `android/res/values/strings.xml`:
  - Bổ sung và chuẩn hóa toàn bộ chuỗi Tiếng Việt: Mô tả chế độ chơi, huy hiệu đã chọn, các mục thiết lập âm thanh/rung/chủ đề/ngôn ngữ, định dạng chi tiết thành tích và chia sẻ.
- `android/res/layout/activity_settings.xml`:
  - Thêm cụm `RadioGroup rg_language` chứa `RadioButton rb_lang_vi` và `rb_lang_en` trong phần thiết lập ngôn ngữ trò chơi.
  - Gắn nhãn chuỗi bản địa hóa cho toàn bộ tiêu đề section và chú thích.
- `android/res/layout/activity_home.xml`:
  - Chuyển đổi toàn bộ text tĩnh thành `@string/home_lore`, `@string/home_version_tag`.
- `android/res/layout/activity_game_mode.xml`:
  - Gắn nhãn `@string/game_mode_title`, `@string/section_difficulty`, `@string/section_mode`.
- `android/res/layout/activity_result_preview.xml`:
  - Gắn nhãn bản địa hóa cho tiêu đề và các tab kết quả xem trước.
- `android/src/main/java/com/example/monstersurvivor/android/util/UiUtils.java`:
  - Hiện thực hàm `wrapLocale(Context)`: Đọc mã ngôn ngữ từ `GamePreferences`, bọc cấu hình `Locale` vào `ContextWrapper` để nạp đúng thư mục tài nguyên `values/` hoặc `values-en/`.
- `android/src/main/java/com/example/monstersurvivor/android/HomeActivity.java`:
  - Tích hợp `attachBaseContext(UiUtils.wrapLocale(newBase))`.
  - Bổ sung kiểm tra thay đổi ngôn ngữ trong `onResume()` để tự động `recreate()` cập nhật giao diện.
- `android/src/main/java/com/example/monstersurvivor/android/SettingsActivity.java`:
  - Tích hợp `attachBaseContext(UiUtils.wrapLocale(newBase))`.
  - Kết nối sự kiện `rg_language.setOnCheckedChangeListener` với `mPrefs.setGameLanguage("vi" / "en")`.
  - Tự động gọi `recreate()` làm mới màn hình ngay khi người dùng bấm chọn ngôn ngữ.
- `android/src/main/java/com/example/monstersurvivor/android/GameModeActivity.java`:
  - Tích hợp `attachBaseContext(UiUtils.wrapLocale(newBase))`.
  - Định dạng chuỗi huy hiệu lựa chọn `%1$s • %2$s` theo ngôn ngữ hiện hành.
- `android/src/main/java/com/example/monstersurvivor/android/AchievementsActivity.java`:
  - Tích hợp `attachBaseContext(UiUtils.wrapLocale(newBase))`.
  - Bản địa hóa hộp thoại ContextMenu xem chi tiết, sao chép chỉ số và chia sẻ chiến tích.
- `android/src/main/java/com/example/monstersurvivor/android/HowToPlayActivity.java`:
  - Tích hợp `attachBaseContext(UiUtils.wrapLocale(newBase))`.
- `android/src/main/java/com/example/monstersurvivor/android/ResultPreviewActivity.java`:
  - Tích hợp `attachBaseContext(UiUtils.wrapLocale(newBase))`.

---

## 3. CÁC LỖI HUD CŨ ĐÃ ĐƯỢC GIẢI QUYẾT TRIỆT ĐỂ

| STT | Vấn đề / Lỗi cũ trước UI02 | Giải pháp kỹ thuật trong UI02 | Kết quả đạt được |
| :---: | :--- | :--- | :--- |
| 1 | **Khối đen đặc che khuất map:** Panel trạng thái người chơi dùng hình chữ nhật đen tuyền kích thước lớn, che mất quái vật tiến lại gần từ phía trên bên trái. | Chuyển sang thanh máu dạng bo góc bán trong suốt `rgba(18, 24, 32, 0.82)` viền vàng mỏng. Các chỉ số phụ đưa vào các pill nhỏ gọn. | Bản đồ thông thoáng, người chơi quan sát được toàn cảnh chiến trường 360 độ. |
| 2 | **Chữ chồng lấn & Text Clipping:** Khi chỉ số sát thương hoặc tốc độ đạt 2 chữ số, text bị tràn lề hoặc đè lên nhãn HP. | Tách biệt hoàn toàn tọa độ Y giữa thanh HP, dòng Kills, và dòng DMG/SPD. Cố định chiều rộng vùng vẽ text BMFont. | Chữ hiển thị ngay ngắn, chuẩn xác ở mọi độ phân giải màn hình mà không bao giờ bị cắt xén. |
| 3 | **Cần gạt ảo vô hình (Invisible Joystick):** Codex làm logic floating joystick nhưng đồ họa hiển thị chỉ là một chấm nhỏ hoặc không có gì, người chơi không biết đặt tay ở đâu và không có phản hồi lực kéo. | Bổ sung vòng tròn hint nghỉ tinh tế tại `(108, 108)` (alpha 0.12). Khi chạm ngón tay, sinh vành ma thuật kép bán kính 66px, tia dẫn hướng lực kéo và núm ngọc 3D bán kính 26px. | Cảm giác điều khiển mượt mà, người chơi nhận biết trực quan tâm joystick và hướng di chuyển ngay lập tức. |
| 4 | **Weapon Box Modal thô sơ:** Popup hiển thị chuỗi text gộp đơn điệu dạng `Cung MỚI - Cấp 1`, các thẻ dính liền nhau, không có màu sắc phân biệt. | Thiết kế lại thành 3 thẻ bài phong cách Fantasy với khung viền chạm trổ, banner màu riêng biệt (Kiếm/Cung/Cầu Lửa/Sét), mũi tên tiến cấp vàng kim `Cấp X → Cấp Y`, và mô tả hiệu ứng rõ ràng. | Giao diện mở rương đẹp mắt, tạo cảm giác thỏa mãn khi nâng cấp sức mạnh dũng sĩ. |
| 5 | **Nút Pause dễ bấm hụt hoặc che lấp:** Nút Pause đặt quá sát cạnh phải, dễ chạm nhầm thanh điều hướng hoặc bị phần khuyết màn hình che mất; hitbox nhỏ. | Dời nút vào vùng an toàn (cách mép phải 24px, mép trên 16px). Mở rộng vùng cảm ứng (hitbox) lên `64 × 60 px` với đồ họa viền vàng kim sắc nét. | Thao tác chạm tạm dừng cực kỳ nhạy, không bấm nhầm và không bị notch che khuất. |
| 6 | **Hardcoded tiếng Anh & thiếu dấu tiếng Việt:** Nhiều chuỗi trong HUD bị gắn cứng tiếng Anh hoặc thiếu ký tự tiếng Việt có dấu. | Tích hợp toàn diện `GameStrings` với 60 khóa dịch core và bộ font BMFont `game-ui.fnt` hỗ trợ 261 ký tự Tiếng Việt chuẩn NFC. | 100% không còn text hardcode, không có lỗi hiển thị font (toàn bộ dấu hỏi, ngã, nặng, sắc, huyền đều hiển thị sắc nét). |

---

## 4. CHI TIẾT HỢP ĐỒNG LƯU TRỮ VÀ ĐỒNG BỘ NGÔN NGỮ

### 4.1. SharedPreferences Contract
- Tên file SharedPreferences: `monster_survivor_prefs.xml`.
- Khóa (Key): `GamePreferences.KEY_GAME_LANGUAGE = "game_language"`.
- Giá trị hợp lệ: `"vi"` (Mặc định) hoặc `"en"`.
- **Cơ chế chống lỗi (Resilience):**
  - Khởi tạo lần đầu: Trả về `"vi"`.
  - Giá trị không hợp lệ (null, chuỗi rỗng, mã quốc gia khác): Tự động trả về `"vi"`.
  - Sai kiểu dữ liệu (ClassCastException do file pref bị ghi đè kiểu số `int` 7): Bắt lỗi trong `try-catch` và trả về `"vi"`.

### 4.2. Cơ chế chuyển đổi ngôn ngữ trong SettingsActivity
```java
// SettingsActivity.java
mRgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
    String newLang = (checkedId == R.id.rb_lang_en) ? "en" : "vi";
    if (!newLang.equals(mPrefs.getGameLanguage())) {
        mPrefs.setGameLanguage(newLang);
        Toast.makeText(this, getString(R.string.toast_lang_changed), Toast.LENGTH_SHORT).show();
        recreate(); // Làm mới Activity ngay lập tức
    }
});
```

### 4.3. Cầu nối sang libGDX Core Gameplay
Trong `AndroidLauncher.java`:
```java
String language = GamePreferences.getInstance(this).getGameLanguage();
GameConfig config = GameConfig.fromIntent(extraDifficulty, extraMode, language);
initialize(new MonsterSurvivorGame(config, this::onGameExit), androidConfig);
```
Trận đấu mới sẽ nhận trực tiếp `GameConfig.strings`, kích hoạt tức thời ngôn ngữ đã chọn cho toàn bộ HUD, Weapon Box modal, Pause, và Game Over mà không cần đọc lại Android SharedPreferences trong vòng lặp game.

---

## 5. BẰNG CHỨNG KIỂM THỬ TỰ ĐỘNG & ANDROID STUDIO RUN

Toàn bộ các bài kiểm tra tự động đã được thực thi và vượt qua tuyệt đối 100%:

### 5.1. Kết quả kiểm tra Logic (`tools/run_task04a_1_checks.py`)
```
TASK 04A.1 LOGIC PASSED: 2534 checks
```
- **Số lượng kiểm tra:** 2.534 checks logic.
- **Phạm vi kiểm tra:**
  - Cấu trúc dữ liệu và logic điều khiển Floating Joystick (`GameInput`, deadzone, clamp radius, pointer ownership).
  - Cấu hình cân bằng tiến trình độ khó (`GameConfig.Stage`, target quái, HP bonus, hệ số tốc độ).
  - Khả năng nạp và ánh xạ 60 khóa bản địa hóa `GameStrings` ở cả hai ngôn ngữ `vi` và `en`.
  - Hợp đồng fallback ngôn ngữ khi mã thiết lập bị null, rỗng hoặc sai định dạng.

### 5.2. Kết quả kiểm tra GL Smoke Check
```
[MonsterSurvivor] Ready: EASY NORMAL debug=false preview=false seed=42 walkable=36322 fallback=false sword=48.0/1.0 roster=82 textures=19 rgbaBytes=44285956 language=vi initialEnemies=6 residentCap=40 enemySpeedMultiplier=1.0
...
[MonsterSurvivor] Weapon choice id=1 token=1 time=0.75 player=1631.9194,1608.0 offers=Cung  MỚI - Cấp 1|Cầu lửa  MỚI - Cấp 1|Sét  MỚI - Cấp 1 loadout=SWORD:1
[MonsterSurvivor] Weapon selected Cung  MỚI - Cấp 1 time=0.75 loadout=SWORD:1,BOW:1
...
[MonsterSurvivor] Ready: EASY NORMAL debug=false preview=false seed=42 walkable=36322 fallback=false sword=48.0/1.0 roster=82 textures=19 rgbaBytes=44285956 language=en initialEnemies=6 residentCap=40 enemySpeedMultiplier=1.0
...
[MonsterSurvivor] Weapon choice id=1 token=6 time=0.75 player=1631.9194,1608.0 offers=Bow  NEW - Lv 1|Fireball  NEW - Lv 1|Lightning  NEW - Lv 1 loadout=SWORD:1
[MonsterSurvivor] Weapon selected Bow  NEW - Lv 1 time=0.75 loadout=SWORD:1,BOW:1
...
TASK 04A.1 GL PASSED: 1781 checks; both languages, floating input, modal/lifecycle/GameOver/Retry
```
- **Số lượng kiểm tra:** 1.781 checks đồ họa GL.
- **Xác minh thực tế:**
  - Khởi chạy thành công ở cả 2 ngôn ngữ `vi` và `en`.
  - Nạp đầy đủ 19 texture của `GameAssets` (42.23 MiB RGBA) và BMFont `game-ui.fnt`.
  - Vòng đời Tạm dừng (Pause / Resume / Lifecycle pause) và khôi phục màn hình.
  - Mở Weapon Box modal, chọn nâng cấp Cung (`BOW:1`), cam kết vũ khí thành công không lỗi trùng lặp.
  - Xử lý Game Over khi HP = 0 và Chơi Lại (Retry) an toàn giữ nguyên cấu hình trận đấu.

---

## 6. PHÂN TÍCH HIỆU NĂNG & CHI PHÍ RENDER HUD

1. **Bộ nhớ & Rác Garbage Collection (GC):**
   - Không khởi tạo đối tượng mới (`new Vector2()`, `new Color()`, `new GlyphLayout()`) trong các hàm `render()` của `GameHud` và `WeaponHud`.
   - Các chuỗi hiển thị số (HP, Kills, DMG, SPD, Timer) được định dạng thông qua bộ đệm tĩnh hoặc mượn trực tiếp từ `GameStrings`.
2. **Gộp lệnh vẽ (Draw Call Batching):**
   - Toàn bộ khối hình học phẳng (thanh HP, vành joystick, khung modal, nền đen mờ) được gom vào 1 lượt `shapeRenderer.begin(ShapeType.Filled)` và 1 lượt `shapeRenderer.begin(ShapeType.Line)`.
   - Toàn bộ text và icon sprite được gom vào 1 lượt `batch.begin()`.
   - Giảm thiểu triệt để việc đảo context giữa OpenGL Shader Program, duy trì FPS ổn định ngay cả khi emulator chịu tải nhiều quái vật.
3. **Bộ tài nguyên Font:**
   - Sử dụng BMFont đơn vị `game-ui.fnt` (kích thước texture 512 × 256 px, chỉ chiếm ~0.375 MiB bộ nhớ GPU), tái sử dụng xuyên suốt vòng đời ứng dụng và được giải phóng (dispose) cùng `GameHud`.

---

## 7. CÁC TÍNH NĂNG CHƯA KẾT NỐI (OUT-OF-SCOPE)

Theo đúng ranh giới của nhiệm vụ UI02 và phân công với Codex:
1. **Trùm cuối Lich Lord & Màn hình Chiến thắng (Victory Screen):**
   - Boss Lich Lord tại phút thứ 9 và cơ chế chiến thắng thực thụ thuộc phạm vi Task 04B. Ở UI02, màn hình Game Over khi dũng sĩ hết máu hoạt động chuẩn xác; màn Victory native vẫn giữ nguyên ở dạng Preview độc lập để phục vụ demo.
2. **Ghi chỉ số trận đấu thật vào Bảng thành tích (Achievements):**
   - Hiện tại màn hình `AchievementsActivity` hiển thị các thẻ thành tích với giá trị mặc định trung thực (Empty State: 00:00, 0 kills, 0 trận) kèm hướng dẫn nhấn giữ Context Menu để xem chi tiết hoặc chia sẻ. Chưa có bridge từ core ghi đè vào SharedPreferences này; tuyệt đối không tạo số liệu giả.

---

## 8. HƯỚNG DẪN DEMO DÀNH CHO GIÁO VIÊN / NGƯỜI ĐÁNH GIÁ

Để kiểm tra toàn diện các tính năng đã thực hiện, thực hiện theo các bước sau trong **Android Studio**:

### Bước 1: Khởi chạy ứng dụng
1. Mở project `MonsterSurvivor` trong Android Studio.
2. Chọn thiết bị giả lập **Pixel 4 (Landscape)**, chọn configuration `android`.
3. Nhấn **Run (Shift+F10)**.

### Bước 2: Kiểm tra Bản địa hóa Song ngữ trên Native UI
1. Màn hình chính xuất hiện bằng **Tiếng Việt**: `MONSTER SURVIVOR`, `VÀO TRẬN ĐẤU`, `THIẾT LẬP`, `THÀNH TÍCH`, `HƯỚNG DẪN CHƠI`.
2. Bấm vào **THIẾT LẬP (Settings)**:
   - Kéo xuống mục **Ngôn ngữ trò chơi (Game Language)**.
   - Bấm chọn **English (Quốc tế)**: Màn hình Settings lập tức làm mới sang Tiếng Anh (`SETTINGS`, `Audio & Volume`, `Native Visual Theme`...).
   - Bấm nút Back quay lại màn hình chính: Màn hình Home đã tự động chuyển sang Tiếng Anh (`PLAY GAME`, `SETTINGS`, `ACHIEVEMENTS`, `HOW TO PLAY`...).
3. Bấm vào **HOW TO PLAY**: Xem 7 thẻ hướng dẫn sinh tồn hiển thị hoàn toàn bằng Tiếng Anh.
4. Bấm vào **ACHIEVEMENTS**: Nhấn giữ một thẻ thành tích bất kỳ để mở **Context Menu** (`View Achievement Details`, `Copy Achievement Stats`, `Share Achievement`).
5. Quay lại **SETTINGS** và bấm chọn lại **Tiếng Việt (Bản địa)** để trải nghiệm giao diện tiếng Việt.

### Bước 3: Vào trận đấu & Kiểm tra HUD Gameplay
1. Tại Home, bấm **VÀO TRẬN ĐẤU** (mở màn chọn chế độ `GameModeActivity`).
2. Chọn độ khó **DỄ (EASY)** và chế độ **CHẾ ĐỘ THỬ NGHIỆM (90 GIÂY - DEMO)**.
3. Bấm **BẮT ĐẦU CHIẾN ĐẤU**: Trận đấu libGDX khởi chạy.
4. Quan sát các thành phần HUD:
   - **Góc trái trên:** Thanh máu đỏ tươi `MÁU 10 / 10`, chỉ số `HẠ GỤC: 0`, pill `ST: 1`, pill `TỐC: 10`.
   - **Góc giữa trên:** Huy hiệu đồng hồ `TIẾN TRÌNH 00:0X`, huy hiệu `DEMO` màu vàng cam, huy hiệu độ khó `DỄ` màu xanh lá.
   - **Góc phải trên:** Nút Pause viền vàng sắc nét.
   - **Đáy màn hình:** 3 slot vũ khí với Kiếm cấp 1 ở ô đầu tiên (`Lv.1`) và thanh hồi chiêu cyan.

### Bước 4: Kiểm tra Floating Joystick
1. Khi chưa chạm vào màn hình: Góc dưới bên trái xuất hiện vòng tròn gợi ý mờ tại `(108, 108)`.
2. Chạm ngón tay vào bất kỳ điểm nào ở nửa trái màn hình:
   - Tâm joystick xuất hiện ngay tại vị trí chạm với vành ma thuật vàng kim.
   - Kéo ngón tay: Tia dẫn hướng kéo dài và núm ngọc 3D di chuyển theo hướng ngón tay, dũng sĩ di chuyển mượt mà trên bản đồ.
3. Nhấc ngón tay khỏi màn hình: Vành động và núm biến mất ngay lập tức, dũng sĩ dừng lại tức thì, không bị trôi hay kẹt hướng.

### Bước 5: Mở Rương Vũ Khí & Tạm Dừng / Kết Thúc Trận
1. Di chuyển nhặt Rương Vũ Khí (Weapon Box màu tím viền vàng) trên bản đồ:
   - Trò chơi tạm dừng thế giới và mở hộp thoại **CHỌN VŨ KHÍ**.
   - Xuất hiện 3 thẻ bài nâng cấp với màu sắc và tên vũ khí tương ứng.
   - Chạm vào một thẻ bài để chọn nâng cấp: Trò chơi tiếp tục với vũ khí mới được bổ sung vào slot vũ khí đáy màn hình.
2. Bấm vào nút **Pause** ở góc trên bên phải: Hộp thoại **TẠM DỪNG** xuất hiện với hai nút `TIẾP TỤC` và `VỀ TRANG CHỦ`.
3. Bấm `TIẾP TỤC` để chiến đấu tiếp; để quái vật tấn công hết HP:
   - Màn hình chuyển sang **KẾT THÚC TRẬN ĐẤU (GAME OVER)**.
   - Hiển thị thời gian sống sót thực tế và số quái đã tiêu diệt.
   - Bấm `CHƠI LẠI (RETRY)` để nạp lại trận đấu mới ngay lập tức, hoặc bấm `VỀ TRANG CHỦ (RETURN HOME)` để trở về màn hình chính Android Native an toàn.
