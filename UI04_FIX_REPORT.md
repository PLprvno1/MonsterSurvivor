# UI04 FIX REPORT – SỬA LỖI GIAO DIỆN NATIVE LOBBY & DIALOG

**Ngày hoàn thành:** 16/11/2026  
**Dự án:** Monster Survivor (`D:\Android\MonsterSurvivor`)  
**Kỹ sư thực hiện:** Antigravity (Senior Android UI/UX Engineer)

---

## 1. Nguyên nhân gốc của lỗi Dialog (Ảnh chụp phản ánh)

Qua điều tra chi tiết hệ thống Style/Theme và cấu trúc phân cấp View của `AlertDialog`:

1. **Gán nhầm `android:background` vào theme Dialog thay vì `android:windowBackground`:**
   - Trong `styles.xml`, style `Theme.MonsterSurvivor.Dialog` khai báo:
     `<item name="android:background">@drawable/panel_fantasy_frame</item>`.
   - Trong Android, thuộc tính `android:background` trong theme sẽ được kế thừa tự động bởi mọi ViewGroup con trong cây phân cấp nếu không ghi đè.
   - Cấu trúc layout nội bộ của `AlertDialog` (`abc_alert_dialog_material`) gồm: `AlertDialogLayout` (root) -> `topPanel` (tiêu đề + icon) -> `contentPanel` (thông điệp) -> `customPanel` (vùng rỗng dự phòng) -> `buttonPanel` (ButtonBar chứa các nút).
   - Từng panel này đều nhận `@drawable/panel_fantasy_frame` làm background riêng!
2. **Hệ quả của việc lặp border và padding:**
   - `@drawable/panel_fantasy_frame` có viền vàng `stroke` 3dp và `padding` 16dp cho cả 4 cạnh.
   - Do đó, viền vàng xuất hiện lồng nhau tới 4 lớp (ngoài cùng, quanh tiêu đề, quanh nội dung thông điệp, quanh vùng custom rỗng, và quanh thanh nút bấm).
   - `customPanel` rỗng tạo ra một hình chữ nhật viền vàng trống vô lý ở giữa dialog.
   - Tổng padding cộng dồn (16dp * 4 lớp = 64dp mỗi phía) khiến Dialog bị phình to bất thường, chiếm gần trọn màn hình ngang (Landscape).
   - Chiều cao màn hình ngang Pixel 4 bị chèn ép khiến các nút bấm ở `buttonPanel` bị đẩy xuống đáy hoặc tràn mép, chữ bị cắt và co rúm.
3. **Thiếu ràng buộc kích thước Dialog trên Landscape:**
   - Thiếu cấu hình `android:windowMinWidthMajor` và giới hạn chiều rộng tối đa, khiến Dialog tự bung rộng theo tỉ lệ mặc định của Material Alert.

---

## 2. Các thay đổi và khắc phục triệt để

### A. Tách riêng Window Background và khử sạch Border lồng nhau
- **Tạo mới `android/res/drawable/dialog_fantasy_window_bg.xml`:**
  - Sử dụng `<inset>` (cách lề 16dp ngang, 12dp dọc) kết hợp `<shape>` nền tối bán trong suốt (`#F40A1710`), bo góc 12dp, viền vàng kim 2dp sạch sẽ (`@color/gold_border`).
  - Không gán padding nội bộ vào shape để tránh ép các View con.
- **Sửa `android/res/values/styles.xml`:**
  - Chuyển background Dialog sang `android:windowBackground="@drawable/dialog_fantasy_window_bg"`.
  - Khai báo rõ ràng `<item name="android:background">@android:color/transparent</item>`: đảm bảo toàn bộ các panel con (`topPanel`, `contentPanel`, `customPanel`, `buttonPanel`) hoàn toàn trong suốt, **chỉ còn duy nhất một khung viền vàng chính bên ngoài**.
  - Bổ sung cấu hình landscape: `android:windowMinWidthMajor="@dimen/dialog_min_width_major"` (440dp).
  - Bổ sung style riêng cho text tiêu đề (`16sp bold gold`), nội dung (`13sp primary`), và nút bấm (`Widget.MonsterSurvivor.Dialog.Button.Positive` vàng kim, `Negative` slate secondary, `Neutral` muted).
  - Khai báo cả `alertDialogTheme` và `materialAlertDialogTheme` trong `Theme.MonsterSurvivor.Base`.

### B. Cố định kích thước và bảo vệ hiển thị trên Code Java (`UiUtils.styleDialog`)
- **Bổ sung `UiUtils.styleDialog(AlertDialog dialog)`:**
  - Sau khi `dialog.show()`, ép kích thước Window dialog: `maxWidth = Math.min((int)(dm.widthPixels * 0.70f), (int)(480 * dm.density))` và `WRAP_CONTENT` chiều cao.
  - Tự động giới hạn chiều rộng Dialog trong khoảng 400–440dp trên màn hình ngang, luôn căn giữa trang trọng, không bao giờ choán hết màn hình.
  - Ép màu sắc và độ đậm chuẩn cho `BUTTON_POSITIVE` (vàng sáng, bold) và `BUTTON_NEGATIVE` (slate, bold), đảm bảo tương phản cao và hiển thị đầy đủ nhãn nút "Đồng ý" / "Hủy bỏ" hoặc "Đóng".
- **Áp dụng đồng bộ:**
  - `HomeActivity`: Dialog Xác nhận Thoát (`showExitConfirmationDialog`) và Dialog Về trò chơi (`showAboutDialog`).
  - `SettingsActivity`: Dialog Thông tin trò chơi (`showAboutDialog`).
  - `AchievementsActivity`: Dialog Chi tiết thành tích khi mở qua ContextMenu (`showAchievementDetailsDialog`).

### C. Rà soát và hoàn thiện các màn hình Native khác
- **`panel_fantasy_frame.xml`:** Giảm độ dày stroke từ 3dp xuống 2dp tinh tế, giữ phong cách Pixel Art sắc nét nhưng không rối mắt.
- **`activity_game_mode.xml`:**
  - Đặt `android:visibility="gone"` cho nút xem trước giả lập `btn_preview_screens`, tránh hiển thị nút dư thừa.
- **`activity_settings.xml`:**
  - Cập nhật hai nút "Đặt lại mặc định" và "Về Monster Survivor": thêm `paddingStart="4dp"`, `paddingEnd="4dp"`, `maxLines="1"`, `ellipsize="end"` để không bao giờ bị cắt chữ khi đổi ngôn ngữ hoặc cỡ chữ hệ thống.
- **`activity_home.xml`:**
  - Tối ưu 4 nút phụ 2 cột (`btn_achievements`, `btn_settings`, `btn_how_to_play`, `btn_exit`): tinh chỉnh padding và icon padding, đảm bảo cả tiếng Việt ("THÀNH TÍCH", "CÀI ĐẶT") và tiếng Anh ("ACHIEVEMENTS", "SETTINGS") hiển thị trọn vẹn, không tràn dòng.
- **`dimens.xml`:**
  - Khai báo `dialog_min_width_major` (440dp) và `dialog_min_width_minor` (320dp).

---

## 3. Danh sách file đã chỉnh sửa

1. `android/res/drawable/dialog_fantasy_window_bg.xml` *(Tạo mới)*: Drawable khung viền Dialog đơn nhất dạng `<inset>`.
2. `android/res/drawable/panel_fantasy_frame.xml` *(Chỉnh sửa)*: Chuẩn hóa stroke 2dp đồng bộ toàn bộ game.
3. `android/res/values/dimens.xml` *(Chỉnh sửa)*: Bổ sung kích thước chuẩn Dialog landscape.
4. `android/res/values/styles.xml` *(Chỉnh sửa)*: Cấu hình chuẩn Dialog Theme (bỏ `android:background`, dùng `android:windowBackground`, thêm button & text styles).
5. `android/res/layout/activity_home.xml` *(Chỉnh sửa)*: Tối ưu padding và chống tràn text trên 4 nút hành động phụ.
6. `android/res/layout/activity_game_mode.xml` *(Chỉnh sửa)*: Ẩn nút xem trước `btn_preview_screens` (`android:visibility="gone"`).
7. `android/res/layout/activity_settings.xml` *(Chỉnh sửa)*: Chống cắt chữ cho nút Reset Defaults và About.
8. `android/src/main/java/com/example/monstersurvivor/android/util/UiUtils.java` *(Chỉnh sửa)*: Bổ sung phương thức `styleDialog()`.
9. `android/src/main/java/com/example/monstersurvivor/android/HomeActivity.java` *(Chỉnh sửa)*: Áp dụng `styleDialog()` cho Dialog Thoát và Dialog About.
10. `android/src/main/java/com/example/monstersurvivor/android/SettingsActivity.java` *(Chỉnh sửa)*: Áp dụng `styleDialog()` cho Dialog About.
11. `android/src/main/java/com/example/monstersurvivor/android/AchievementsActivity.java` *(Chỉnh sửa)*: Áp dụng `styleDialog()` cho Dialog chi tiết thành tích.

---

## 4. Kiểm tra cú pháp và biên dịch

Đã thực hiện biên dịch kiểm tra cú pháp duy nhất bằng lệnh:
```bash
./gradlew :core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain
```
**Kết quả:** `BUILD SUCCESSFUL` (Exit code: 0). Không có bất kỳ lỗi cú pháp hoặc xung đột tài nguyên nào.

---

## 5. Phần còn thiếu (Bàn giao cho Codex)

- **Backend lưu trữ số liệu Thành tích (Lifetime Stats):** UI Thành tích đã được chuẩn bị sẵn empty state chân thực (0 trận, 0 kills, 0/6 hoàn thành) và model dữ liệu `AchievementItem`. Cần Codex hoàn thành cầu nối giữa `MatchResult` từ `GameSession` vào `AchievementManager.recordMatchResult()` (chi tiết đã ghi nhận đầy đủ tại `CODEX_ACHIEVEMENTS_BACKEND_HANDOFF.md`).

---

*Hệ thống đã sẵn sàng để Chủ dự án chạy thực tế và kiểm thử.*
