# NATIVE UI BUG AUDIT — MONSTER SURVIVOR LOBBY & ACHIEVEMENTS

**Mã nhiệm vụ:** UI04 — Native Lobby Bug Fixes & Achievements UI  
**Kỹ sư thực hiện:** Antigravity (Senior Android UI/UX Engineer)  
**Thời gian lập audit:** 16/11/2026  
**Mục tiêu thiết bị:** Google Pixel 4 Landscape (2280 × 1080 px, API 29–37), Android Studio Run  

---

## 1. TỔNG QUAN KHẢO SÁT HIỆN TRẠNG

Khảo sát toàn bộ các tập tin Native của Monster Survivor bao gồm:
- **6 Activity Native:** `HomeActivity`, `GameModeActivity`, `SettingsActivity`, `AchievementsActivity`, `HowToPlayActivity`, `ResultPreviewActivity`.
- **Hệ thống Audio Native:** `NativeAudioActivity`, `NativeAudio`, `AudioFocus`.
- **Model & Preferences:** `GamePreferences`, `AchievementManager`.
- **AndroidManifest.xml:** Khai báo launcher, orientations, flags, themes.
- **Layouts & Resources:** `activity_*.xml`, `values/strings.xml`, `values-en/strings.xml`, `colors.xml`, `styles.xml`, `dimens.xml`, `anim/`.

---

## 2. BẢNG TỔNG HỢP LỖI (BUG MATRIX)

| Mã lỗi | Màn hình / Thành phần | Hiện tượng & Lỗi | Nguyên nhân cốt lõi | Mức độ | Tệp cần sửa |
| :---: | :--- | :--- | :--- | :---: | :--- |
| **BUG-01** | `HomeActivity` | Bấm phím Back vật lý/cử chỉ trên màn Home bị đóng app đột ngột, không hiện hộp thoại xác nhận Thoát/Hủy. | `NativeAudioActivity` đăng ký `OnBackPressedCallback` mặc định gọi ngay `onBackPressed()`. `HomeActivity` chưa ghi đè callback này để hiển thị dialog xác nhận. | **Nghiêm trọng** | `HomeActivity.java` |
| **BUG-02** | `HomeActivity` / `NativeAudio` | Khi bấm xác nhận Thoát, nhạc nền Menu (`BGM_MENU`) có thể tiếp tục phát thêm một đoạn ngắn; task ứng dụng chưa được dọn sạch khỏi danh sách gần đây. | `showExitConfirmationDialog()` gọi `finishAffinity()` nhưng chưa giải phóng/ngắt `NativeAudio.pause(this)` tức thời, chưa gọi `finishAndRemoveTask()` (API 21+). | **Nghiêm trọng** | `HomeActivity.java`, `NativeAudio.java` |
| **BUG-03** | Toàn bộ Native UI (`UiUtils`) | Nhấp đúp (double-tap) hoặc bấm nhanh liên tục trên các nút điều hướng (`btn_play`, `btn_settings`, `btn_achievements`, `btn_how_to_play`) làm mở nhiều Activity trùng lặp. | `UiUtils.animateClick()` bật lại `view.setEnabled(true)` ngay sau khi kết thúc scale animation (~160ms), không có debounce / gate chặn click kép đa chạm. | **Nghiêm trọng** | `UiUtils.java` |
| **BUG-04** | `GameModeActivity` | Bấm nhanh nút `BẮT ĐẦU CHIẾN ĐẤU` (`btn_start_game`) có thể khởi chạy 2 trận đấu libGDX (`AndroidLauncher`) cùng lúc. | `launchGame()` không có cờ khóa chuyển cảnh nguyên tử (atomic navigation latch). | **Nghiêm trọng** | `GameModeActivity.java` |
| **BUG-05** | `AndroidManifest.xml` / Navigation | Nguy cơ tạo nhiều `HomeActivity` chồng lấn trong backstack khi điều hướng vòng quanh giữa các Activity hoặc quay về từ Gameplay. | `HomeActivity` chưa cấu hình `android:launchMode="singleTask"` trong `AndroidManifest.xml`. | **Trung bình** | `AndroidManifest.xml` |
| **BUG-06** | Các Activity con (`GameMode`, `Settings`, `Achievements`, `HowToPlay`) | Hiệu ứng chuyển cảnh khi bấm nút Back (Toolbar hoặc cử chỉ) không đồng bộ với hiệu ứng mở màn hình. | Khi mở dùng `slide_in_right, slide_out_left`, nhưng khi Back chỉ gọi `finish()` dùng animation hệ thống mặc định. | **Thẩm mỹ** | `GameModeActivity.java`, `SettingsActivity.java`, `AchievementsActivity.java`, `HowToPlayActivity.java` |
| **BUG-07** | `AchievementsActivity` | Thiếu danh sách thành tích chi tiết, thiếu thanh tiến độ tổng quan, không phân định trạng thái Khóa / Mở Khóa, chưa tách bạch giữa Kỷ lục trọn đời và Thành tích. | Giao diện cũ từ UI01 chỉ hiển thị 5 card số liệu thô; chưa xây dựng UI model danh mục thành tích dũng sĩ. | **Nghiêm trọng** | `activity_achievements.xml`, `AchievementsActivity.java`, `AchievementManager.java` |
| **BUG-08** | `AchievementsActivity` | Empty state thô sơ, chưa truyền tải thông tin trung thực về việc hệ thống sẽ tự động ghi nhận khi hoàn thành trận đấu. | `tv_empty_badge` chỉ là một nhãn nhỏ màu xám, không có mô tả hướng dẫn người chơi rõ ràng. | **Trung bình** | `activity_achievements.xml`, `AchievementsActivity.java` |
| **BUG-09** | `HowToPlayActivity` | Nội dung hướng dẫn sinh tồn bị lỗi thời: chưa cập nhật kỹ năng Dash, Ultimate, Tiến hóa vũ khí, Hộp kỹ năng, 49 kỹ năng đặc biệt của quái vật, Trùm Mini & Trùm cuối Lich Lord. | Các chuỗi và thẻ hướng dẫn được viết từ Task 01B chưa được cập nhật theo các mốc Task 05, 06, 06.1, 07, 08. | **Nghiêm trọng** | `activity_how_to_play.xml`, `strings.xml`, `values-en/strings.xml` |
| **BUG-10** | `GameModeActivity` / `ResultPreviewActivity` | Nút `XEM TRƯỚC GIAO DIỆN KẾT QUẢ` (`btn_preview_screens`) dẫn người dùng vào màn hình chứa số liệu thành tích giả (10:00, 150 kills). | `ResultPreviewActivity` là màn hình mockup thử nghiệm cũ từ giai đoạn UI01, không phải luồng thực tế của game. | **Nghiêm trọng** | `activity_game_mode.xml`, `GameModeActivity.java` |
| **BUG-11** | `SettingsActivity` | Cần rà soát tính bền vững của các cấu hình sau khi khởi động lại app: Ngôn ngữ, Âm thanh, Âm lượng, Rung, Chủ đề. | Phải đảm bảo mọi thay đổi ghi vào `GamePreferences` được tải lại chính xác 100% khi mở lại app. | **Quan trọng** | `SettingsActivity.java`, `GamePreferences.java` |
| **BUG-12** | Song ngữ Việt / Anh | Kiểm tra nguy cơ thiếu chuỗi hoặc lệch định dạng giữa `values/strings.xml` và `values-en/strings.xml`. | Bổ sung các chuỗi mới phục vụ danh mục thành tích, cập nhật hướng dẫn chơi và loại bỏ văn bản cứng. | **Quan trọng** | `strings.xml`, `values-en/strings.xml` |

---

## 3. CHI TIẾT NGUYÊN NHÂN VÀ PHƯƠNG ÁN KHẮC PHỤC

### 3.1. BUG-01 & BUG-02: Nút Thoát và Xử lý Back tại Home
- **Hiện trạng:**
  - `NativeAudioActivity` đã đăng ký một `OnBackPressedCallback` dùng chung cho toàn bộ Activity native:
    ```java
    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
        @Override public void handleOnBackPressed() {
            NativeAudio.play(NativeAudioActivity.this, AudioEvent.UI_CANCEL);
            setEnabled(false);
            try { getOnBackPressedDispatcher().onBackPressed(); }
            finally { setEnabled(true); }
        }
    });
    ```
  - Khi người dùng ở `HomeActivity` và bấm phím Back hoặc vuốt cử chỉ Back, callback này gọi `onBackPressed()`, làm Activity lập tức bị `finish()` mà không hiện hộp thoại hỏi người chơi có muốn thoát không.
  - Khi bấm nút `btn_exit` trên giao diện, hộp thoại hiện ra, nhưng khi chọn `Đồng ý` (`dialog_yes`), app gọi `finishAffinity()` nhưng không dừng triệt để `NativeAudio`, khiến âm thanh nền có thể phát dở dang.
- **Giải pháp:**
  1. Trong `HomeActivity.onCreate()`, đăng ký `OnBackPressedCallback` riêng cho `HomeActivity`:
     ```java
     getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
         @Override public void handleOnBackPressed() {
             showExitConfirmationDialog();
         }
     });
     ```
  2. Trong `showExitConfirmationDialog()`:
     - Nút Đồng ý (`dialog_yes`): Dừng ngay `NativeAudio.pause(this)`, giải phóng tài nguyên âm thanh, gọi `finishAffinity()`, và gọi `finishAndRemoveTask()` (trên Android API 21+). Không dùng `System.exit(0)`.
     - Nút Hủy (`dialog_no`): Phát âm thanh `AudioEvent.UI_CANCEL`, đóng dialog và giữ nguyên trạng thái ở `HomeActivity`.
     - Sự kiện `onCancel`: Xử lý tương tự nút Hủy.

### 3.2. BUG-03 & BUG-04: Ngăn chặn Click kép / Double-Tap & Đa Chạm
- **Hiện trạng:**
  - `UiUtils.animateClick()` chỉ vô hiệu hóa view tạm thời bằng `view.setEnabled(false)` trong thời gian animation (`btn_press_scale.xml` chạy trong 160ms). Ngay sau đó `view.setEnabled(true)` được gọi lại, dẫn đến nếu chạm đúp nhanh, hành động điều hướng sẽ bị gọi 2 lần.
  - Người dùng chạm 2 ngón tay đồng thời vào 2 nút (ví dụ `btn_play` và `btn_settings`) sẽ kích hoạt cả 2 Intent.
  - Trong `GameModeActivity`, việc double-tap nút `btn_start_game` có thể gửi 2 Intent khởi chạy `AndroidLauncher`.
- **Giải pháp:**
  1. Bổ sung cơ chế Debounce toàn cục trong `UiUtils`:
     - Định nghĩa `sLastNavigationTime` với ngưỡng tối thiểu 500ms giữa 2 lần điều hướng.
     - Hàm `UiUtils.animateClick()` áp dụng kiểm tra thời gian này trước khi kích hoạt callback.
  2. Trong `GameModeActivity`:
     - Bổ sung cờ nguyên tử `mIsLaunchingGame`: Khi bắt đầu khởi chạy game, đặt `mIsLaunchingGame = true` và khóa nút; nếu `onResume()` được gọi lại (ví dụ khi game đóng hoặc quay lại), reset cờ về `false`.

### 3.3. BUG-05 & BUG-06: Chuẩn hóa Điều hướng & Backstack
- **Hiện trạng:**
  - `AndroidManifest.xml` khai báo `HomeActivity` không có `android:launchMode`.
- **Giải pháp:**
  1. Thêm `android:launchMode="singleTask"` cho `HomeActivity` trong `AndroidManifest.xml` để đảm bảo trong task chỉ luôn có duy nhất một phiên bản `HomeActivity`.
  2. Bổ sung hiệu ứng chuyển cảnh mượt mà khi Back ở các màn hình con (`overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)`).

### 3.4. BUG-07 & BUG-08: Tái thiết kế Màn hình Thành tích (Achievements)
- **Hiện trạng:**
  - Màn hình `AchievementsActivity` hiện chỉ có 5 card dạng text thô. Chưa có danh sách thành tích cá nhân, chưa có thanh tiến độ mở khóa (Progress bar), chưa có trạng thái Khóa / Mở Khóa với biểu tượng ổ khóa, và empty state quá sơ sài.
- **Giải pháp:**
  1. Thiết kế lại `activity_achievements.xml`:
     - **Header Tổng Quan Tiến Độ:** Hiển thị tỉ lệ mở khóa (ví dụ `0 / 6 Thành Tích Đã Mở`), thanh `ProgressBar` màu vàng kim `progress_fantasy_gold`.
     - **Phần 1: Kỷ Lục Sinh Tồn Trọn Đời (Lifetime Records):**
       - 5 card thông số: Thời gian sống sót lâu nhất, Tổng số quái vật đã hạ, Tổng số trận thắng, Tổng số trận đã tham gia, Số trùm Lich Lord đã tiêu diệt. Đọc trung thực từ `AchievementManager`.
     - **Phần 2: Danh Mục Thành Tích Dũng Sĩ (Hero Achievements List):**
       - 6 thành tích tiêu biểu:
         1. *Khởi Đầu Sinh Tồn (First Blood):* Tiêu diệt quái vật đầu tiên.
         2. *Chiến Binh Kiên Cường (Novice Survivor):* Sống sót tối thiểu 3 phút (180 giây).
         3. *Thợ Săn Quái Vật (Monster Slayer):* Tiêu diệt tích lũy 100 quái vật.
         4. *Đồ Tể Chiến Trường (Centurion Slayer):* Tiêu diệt tích lũy 500 quái vật.
         5. *Khắc Tinh Bóng Đêm (Bane of Lich Lord):* Đánh bại Chúa tể bóng đêm Lich Lord.
         6. *Dũng Sĩ Huyền Thoại (Legendary Victor):* Giành chiến thắng hoàn toàn trong trận đấu chuẩn 10 phút.
       - Mỗi thẻ có biểu tượng (icon), tiêu đề, mô tả điều kiện đạt, huy hiệu `ĐÃ ĐẠT / CHƯA ĐẠT` và thanh tiến độ con.
     - **Phần 3: Empty / Unavailable State trung thực:**
       - Khi chưa có trận đấu nào được ghi nhận (`hasAnyRecordedMatches() == false`), hiển thị khung thông báo thân thiện: "Chưa có dữ liệu sinh tồn. Hoàn thành trận đấu để tự động ghi nhận chiến tích và mở khóa thành tích!"
  2. Cập nhật `AchievementManager.java` với model `AchievementItem` chuẩn mực để phục vụ handoff cho Codex.

### 3.5. BUG-09: Cập nhật Toàn Diện Hướng Dẫn Chơi (How To Play)
- **Hiện trạng:**
  - Hướng dẫn sinh tồn thiếu vắng toàn bộ các tính năng cốt lõi mới từ Task 05 đến Task 08.
- **Giải pháp:**
  - Thiết kế lại 8 thẻ bài trong `activity_how_to_play.xml` với icon và nội dung chuẩn xác theo gameplay thực tế:
    1. *Cần gạt ảo Floating Joystick (360°):* Điều khiển dũng sĩ linh hoạt né đòn quái vật.
    2. *Vũ khí tự động tấn công (4 loại vũ khí):* Kiếm Thánh, Cung Rừng, Cầu Lửa, Tia Sét (tối đa 3 vũ khí cùng lúc).
    3. *Tiến hóa vũ khí thần thoại (Weapon Evolution):* Vòng Kiếm (Blade Orbit), Mưa Tên (Arrow Storm), Thiên Thạch Lửa (Inferno Meteor), Lôi Võng (Thunder Web) khi vũ khí đạt cấp 5.
    4. *Kỹ năng tác chiến chủ động (Active Skills):* Lướt (Dash) né đòn không nhận sát thương & Tuyệt kỹ Nộ (Ultimate) sóng xung kích đẩy lùi quái.
    5. *Hộp báu chiến trường & Hộp kỹ năng:* Hộp Máu, Hộp Sát Thương, Hộp Tốc Độ, Rương Vũ Khí và Hộp Kỹ Năng (Skill Box) nâng cấp Dash / Ultimate.
    6. *Màn đêm tử thần & Kỹ năng quái vật:* Đêm buông xuống từ phút thứ 5; 49 loài quái vật sở hữu kỹ năng đặc biệt (đạn độc, cầu lửa, làm chậm, slam, chướng khí) có vệt telegraph báo trước để dũng sĩ né tránh.
    7. *Trùm Mini & Chúa tể Lich Lord:* Trùm Mini xuất hiện phút 5; Chúa tể Lich Lord thức tỉnh phút 9 với 2 phase và kỹ năng hủy diệt.
    8. *Điều kiện chiến thắng vinh quang:* Sống sót 10 phút (hoặc 90 giây ở chế độ Demo) và tiêu diệt Chúa tể Lich Lord.
  - Tuyệt đối không đề cập đến chế độ Endless Mode chưa triển khai.

### 3.6. BUG-10: Loại bỏ Nút Xem Trước Mockup Gây Hiểu Nhầm
- **Hiện trạng:**
  - Nút `btn_preview_screens` trong `GameModeActivity` mở `ResultPreviewActivity` hiển thị số liệu giả (10:00, 150 kills).
- **Giải pháp:**
  - Ẩn nút `btn_preview_screens` (`android:visibility="gone"`) khỏi màn hình chọn chế độ chơi để người dùng không truy cập vào màn hình mockup số liệu giả.

---

## 4. KẾ HOẠCH TRIỂN KHAI VÀ BÀN GIAO

1. **Bước 1:** Cập nhật `UiUtils.java` với cơ chế chống click kép toàn cục và animation back chuẩn.
2. **Bước 2:** Cập nhật `AndroidManifest.xml` với `launchMode="singleTask"` cho `HomeActivity`.
3. **Bước 3:** Sửa `HomeActivity.java` với callback nút Back hệ thống, xử lý đóng task sạch sẽ và ngắt nhạc Menu tức thời khi thoát.
4. **Bước 4:** Sửa `GameModeActivity.java` với cờ chống double-tap khởi chạy 2 game và ẩn nút preview số liệu giả.
5. **Bước 5:** Nâng cấp `AchievementManager.java`, `activity_achievements.xml` và `AchievementsActivity.java` với UI Thành tích hoàn chỉnh, thanh tiến độ, trạng thái Khóa/Mở, và empty state chân thực.
6. **Bước 6:** Cập nhật `activity_how_to_play.xml`, `strings.xml` và `values-en/strings.xml` với 8 thẻ hướng dẫn chi tiết chuẩn gameplay Task 08.
7. **Bước 7:** Soạn thảo tài liệu `CODEX_ACHIEVEMENTS_BACKEND_HANDOFF.md` mô tả kiến trúc handoff sang Codex.
8. **Bước 8:** Biên dịch, kiểm thử tự động, chạy thực nghiệm trên Pixel 4 Emulator và lập báo cáo `UI04_NATIVE_LOBBY_REPORT.md`.
