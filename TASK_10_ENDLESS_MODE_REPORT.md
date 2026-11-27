# Task 10 — Endless Mode

Ngày 16/11/2026. Đã đối chiếu source hiện tại và báo cáo Task05/07/08/08.1/09.

## Cơ chế

Thêm **ENDLESS / VÔ HẠN** cạnh Normal và Demo, vẫn chọn Easy/Hard. Bắt đầu HP10, Sword1, Dash/Ultimate1; đồng hồ chạy từ0 theo thời gian gameplay thực. Không có giới hạn600 giây, Lich tự xuất hiện hoặc Victory. Death → Game Over; Home kết thúc trận; Retry tạo session mới với mọi bộ đếm/progression reset.

Quái thường và lịch hộp thưởng tiếp tục sau phút10. Giữ cap40/60,67 loài có skill, hai A* mỗi frame và các projectile/FX pools hiện có. Khi cả hai skill đạtLv5, Skill Box trong Endless đi qua lựa chọn thưởng Weapon Box hiện có; nếu vũ khí đã max/evolve thì dùng Damage/MaxHP/Speed fallback hợp lệ. Không sửa mechanics vũ khí, Evolution hay Dash/Ultimate.

## Boss và scaling

- Mốc300/600/900/1200… giây. Tái sử dụng BossSystem, placement/navigation, BossInstance, damage guard, Mini slam, cảnh báo/HP HUD và quake FX đã import.
- Pool5 loài thật đã có clip/collider phù hợp: Moss Golem, Living Armor, Cyclops, Crab Knight, Rock Golem. Chọn ngẫu nhiên mỗi đợt; scale tương đối với loài thường1,65→1,95, giữ collider Boss18×12 và tên VI/EN hiện có.
- Tối đa **một Boss sống**. Các mốc đến trong lúc Boss còn sống được gộp vào **một cờ hoãn**; sau Death animation mới thử spawn đợt kế. Placement lỗi thử lại mỗi0,5s; không xếp hàng hàng loạt đợt. Hạ Boss chỉ tăng Boss kills và tiếp tục trận.
- Quái mới sinh nhận HP nền của giai đoạn hiện tại × `1,15^floor(seconds/300)`, làm tròn. Damage tăng1 mỗi20 phút Easy/15 phút Hard. Giới hạn HP50.000, damage8; không sửa HP của quái đang sống.
- Boss theo số đợt thực tế: HP gốc120/200 Easy/Hard × `1,25^(wave−1)`, tối đa1.000.000. Contact damage gốc2/3, tăng1 mỗi3/2 đợt, cap16; Slam Easy thêm1 theo Mini framework cũ. Giới hạn số mũ/counters trước phép tính để tránh overflow. Hard giữ windup/cooldown Mini khó hơn Easy.

## HUD, kết quả và thống kê

HUD có ENDLESS, difficulty, thời gian, kills, giai đoạn và Boss kills; Boss có tên/thanh HP/cảnh báo. Game Over thêm Boss kills thật. MatchResult bổ sung `endlessBossKills`/`endlessStage`, giữ constructor tương thích cũ.

Không sửa AchievementManager, GamePreferences hoặc SharedPreferences. Guard `mode != NORMAL` của Task09 loại Endless trước khi truy cập store; không ghi `official_v1` hoặc mở thành tích Normal. Chưa lưu kỷ lục Endless riêng.

## File

- Mới: `core/.../config/EndlessRules.java`.
- Sửa Core: `config/GameMode.java`, `GameConfig.java`; `boss/BossDefinition.java`, `BossSystem.java`; `gameplay/GameSession.java`, `MatchResult.java`; `spawn/EnemySpawner.java`, `EnemySpawnProfile.java`; `localization/GameStrings.java`; `ui/BossHud.java`, `GameHud.java`.
- Native chỉ phần chọn chế độ: `GameModeActivity.java`, `activity_game_mode.xml`, thêm3 string VI/EN. Không sửa Lobby/theme/Dialog/navigation khác.

## Kiểm tra và giới hạn

Compile `:core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain`: **exit0**, còn warning SDK XML có sẵn. [Log](core/build/reports/task10/compile.log).

Smoke mô phỏng ngắn **30 PASS**: Intent/defaults, Boss300, hoãn/gộp đợt, Boss death không Victory, spawn/pickups sau600, scaling/caps, animation pool5 loài, upgrade cuốiLv5/fallback, Pause, death/result, khởi tạo Retry, Task09 isolation, và Mini/Lich/Victory của cả Normal/Demo. Nhảy clock chỉ trong fixture JVM; không chạy trận dài hoặc ghi dữ liệu người chơi. [Log](core/build/reports/task10/smoke.log). XML/binding IDs và `git diff --check` đều qua.

Chưa chạy Android/GL, emulator automation, stress hoặc FPS benchmark. Chủ dự án cần kiểm tra bố cục lựa chọn3 mode, HUD và balance khi chơi lâu trên Android Studio. Pool Endless hiện có5 Boss dùng slam; không thêm artwork/SFX/leaderboard/backend. Không commit/push/export APK/AAB. Dừng tại Task10.
