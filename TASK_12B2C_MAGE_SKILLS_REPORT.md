# Task 12B2c – Mage Skills

## Hoàn thành

- Thêm tuning Lv1–5 và offer riêng cho Mage; giữ giới hạn 2 class skill mỗi trận và Skill Box chung với Dash/Ultimate.
- **Ice Shard:** tự bắn projectile thẳng có swept collision/wall collision; trúng quái thường gây slow riêng trên `Enemy`, slow không cộng dồn và boss chỉ nhận damage.
- **Arcane Trap:** đặt tại vị trí mục tiêu đã chốt, arm sau 0,5 giây, hết hạn ở 3,5 giây; pool tối đa 2 bẫy, nổ một lần theo bán kính và LOS.
- **Magic Shield:** chặn hit trực tiếp từ mọi hướng tại damage boundary hiện có; không chặn DoT và không tiêu hao qua Dash/iframe.
- **Arcane Channel:** tự kích hoạt khi có mục tiêu nhìn thấy; Fireball khởi chạy trong thời gian buff dùng cooldown nhân theo cấp và +1 damage chỉ lên mục tiêu bị hit trực tiếp chính. Fireball splash/evolution không nhận bonus.
- Cooldown, bẫy, projectile và buff chỉ tiến triển trong nhánh RUNNING; Retry tạo session mới. FX dùng atlas hiện có (`ice_shard`, `overlay_slow`, `cast`, `magic_orb_impact`, `blast`) cùng rune/glow vẽ bằng primitive; không thêm asset hoặc âm thanh.

## File chính

- Skill system/tuning/UI: `ClassSkillSystem.java`, `ClassSkillTuning.java`, `ClassSkillId.java`, `SkillOffer.java`, `ClassSkillRenderer.java`, `GameStrings.java`, `HudStyle.java`, `GameScreen.java`.
- Slow trên quái: `Enemy.java`, `EnemySystem.java`.
- Bonus hit Fireball: `WeaponSystem.java`, `ProjectileSystem.java`.
- Kiểm tra: `MageSkillChecks.java`; cập nhật kỳ vọng Mage trong `ClassSkillChecks.java`.

## Kiểm tra

- `:core:compileJava :android:compileDebugJavaWithJavac` — thành công.
- `python tools/run_skill_checks.py MageSkillChecks ClassSkillChecks` — thành công, lần lượt 48 và 45 assertions.
- Không chạy emulator hoặc suite dài; không export APK/AAB, commit hay push.

## Còn lại

- Không còn hạng mục Mage skill trong Task 12B2c. Coin/unlock vẫn ngoài phạm vi task này.
