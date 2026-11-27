# Task UI07 – Character Selection UI

## Đã sửa

- `android/res/layout/activity_game_mode.xml`: thay spinner bằng ba thẻ nhân vật có chân dung, vũ khí, mô tả và trạng thái/giá; ví xu hiển thị phía trên; giữ khu vực Easy/Hard và Normal/Demo/Endless, thêm cuộn dọc khi màn hình thấp. Nút mở khóa nằm cạnh luồng Start hiện có.
- `android/src/main/java/com/example/monstersurvivor/android/GameModeActivity.java`: dùng frame idle từ atlas hiện có cho Warrior/Archer/Mage, cập nhật thẻ/ví ngay sau mua, vô hiệu hóa mở khóa khi thiếu xu và vẫn chặn Start cho nhân vật chưa sở hữu.
- `android/res/values/strings.xml`, `android/res/values-en/strings.xml`: thêm mô tả, tên vũ khí và trạng thái thẻ tiếng Việt/Anh.

## Kiểm tra

- `:android:compileDebugJavaWithJavac` — thành công.
- Không chạy emulator hoặc test suite. Bố cục thực tế trên thiết bị và Dialog cần được chủ dự án kiểm tra thủ công.

Không thay đổi gameplay, giá, xu, lưu trữ, hoặc asset sprite gốc.
