# Task 09 — Achievements & Match Statistics

Hoàn thành ngày 16/11/2026. Đã đọc `UI04_FIX_REPORT.md`, handoff Thành tích, GamePreferences và luồng gameplay hiện tại.

## File sửa

- Core: `gameplay/MatchResult.java`, `gameplay/GameSession.java`, `screen/GameScreen.java`, `MonsterSurvivorGame.java`.
- Android: `AndroidLauncher.java`, `model/AchievementManager.java`, `AchievementsActivity.java`.
- Resources: `layout/activity_achievements.xml`, `values/strings.xml`, `values-en/strings.xml`.

Không đổi Enemy Skills, Boss AI/HP/Victory, cơ chế Evolution, Dash/Ultimate, balance, audio, GamePreferences hoặc theme/Dialog/navigation UI04.

## Dữ liệu và sự kiện

SharedPreferences **monster_survivor_stats**, namespace **official_v1.**. Một lần `commit()` lưu đồng thời counters, ngày mở khóa và tập UUID trận đã ghi; cùng UUID bị từ chối kể cả khi tạo lại manager/process. Không dùng sessionId dạng bộ đếm trong process làm khóa lưu trữ. Retry tạo GameSession và UUID mới.

Lưu đủ chín chỉ số: trận hoàn thành, kills, victories, tổng thời gian chơi, thời gian sống sót tốt nhất, Mini Boss kills, Lich kills, Hard wins, số lần tiến hóa vũ khí. Tổng thời gian giữ phần lẻ ở milliseconds, hiển thị phút:giây; dùng elapsed thực, không dùng progress nén.

Enemy death callback hiện có chỉ đếm khi chuyển sang Death. Mini/Lich lấy cờ từ BossSystem thật. Evolution chỉ tăng sau commit Weapon Box thành công. GameSession phát MatchResult sau animation Game Over/Victory; callback đi qua GameScreen → MonsterSurvivorGame → AndroidLauncher → AchievementManager. Ghi đồng bộ khi nhận kết quả, trước Home/Retry; không thay điều kiện Victory. Kết quả Victory thiếu cờ Lich death hoặc progress600 bị từ chối.

**Chính sách:** chỉ trận **Normal đã kết thúc** được cộng toàn bộ thống kê/thành tích. Trận thoát giữa chừng, bị hủy hoặc process chết trước khi có kết quả không được cộng kills/thời gian/evolution hay trận hoàn thành. Demo và diagnostic/debug/preview bị loại; snapshot constructor cũ thiếu UUID/provenance cũng bị loại. Không phục dựng lịch sử trước Task09 hoặc nhập counters placeholder cũ không xác định được mode/nguồn. Dữ liệu chính thức mới nằm riêng với cài đặt nên đổi/reset âm lượng, ngôn ngữ, theme không xóa thống kê.

## Catalog

| ID ổn định | Tên VI / EN | Điều kiện trong dữ liệu Normal chính thức |
|---|---|---|
| first_match | Bước Khởi Đầu / First Steps | Hoàn thành 1 trận |
| normal_survivor | Người Sống Sót / Survivor | Sống sót ít nhất300 giây thực trong 1 trận hoàn thành |
| monster_slayer | Thợ Săn / Hunter | Tổng100 kills |
| thousand_kills | Sát Thủ Quái Vật / Monster Assassin | Tổng1.000 kills |
| mini_boss_hunter | Kẻ Diệt Boss / Boss Slayer | Hạ1 Mini Boss |
| legendary_victor | Chiến Thắng Đầu Tiên / First Victory | Hạ Lich và Victory |
| hard_victor | Chiến Binh Bất Khuất / Indomitable Warrior | Victory trên Hard/Normal |
| weapon_master | Bậc Thầy Vũ Khí / Weapon Master | Commit1 lần Evolution |

Mỗi mục có tiến độ/target, Locked/Unlocked và timestamp lần đầu đạt; ghi thêm kết quả không đổi ngày đã đạt.

## Native UI

Bind lại khi mở/resume. Dùng năm thẻ tổng quan cũ, thêm một TextView cho thời gian chơi/Mini/Hard/evolution và một hàng hai thẻ sao chép style hiện có để đủ8 mục. ProgressBar lấy max từ catalog. Card và ContextMenu dùng cùng model, hiển thị trạng thái, tiến độ, ngày đạt; copy/share lấy dữ liệu hiện tại. Giữ `UiUtils.styleDialog` và navigation nguyên trạng. Cài mới:0 thống kê,0/8, empty state. Antigravity có thể tinh chỉnh bố trí dòng thống kê phụ sau; chức năng đã có binding.

## Kiểm tra

- `gradlew.bat :core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain`: **exit0**, compile Java/Android thành công. Có cảnh báo SDK XML/deprecated API. [Log](core/build/reports/task09/compile.log).
- **18 kiểm tra ngắn PASS**: luồng Enemy death/GameSession result, bỏ trận dở dang, đọc lại temporary disk store, chống trùng UUID sau reload, loại Demo/diagnostic/snapshot cũ, guard Victory, counters,8 điều kiện và ngày đạt. SharedPreferences dùng test double lưu trên đĩa JVM, không ghi dữ liệu người chơi. [Log](core/build/reports/task09/focused-check.log).
- XML VI/EN parse được; không trùng resource IDs/string keys, tất cả ID binding resolve; `git diff --check` exit0.

Chưa chạy Android/emulator để kiểm tra bố trí thực tế hoặc đóng/mở app thật; chủ dự án tự kiểm thử. Không chạy stress/benchmark/Studio Run, không xuất APK/AAB hoặc commit/push. Dừng tại Task09.
