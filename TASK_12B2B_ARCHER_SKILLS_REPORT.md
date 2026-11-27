# Task 12B2b – Archer Skills

## Đã triển khai

- `ARCHER_MARK`, `ARCHER_FOCUS`, `ARCHER_RAIN` đã được bật trong `ClassSkillSystem`; chỉ Archer nhận các lựa chọn này. Skill Box giữ giới hạn hai kỹ năng riêng và các lựa chọn Dash/Ultimate.
- Hunter’s Mark chọn mục tiêu có LOS trong tầm 130, ưu tiên Boss; lưu thời lượng và số lượt theo cấp. Bow và Focus Shot cộng `+1` vào hit trực tiếp, chỉ tiêu lượt khi HP giảm; bỏ dấu khi hết giờ hoặc mục tiêu chết.
- Focus Shot chốt hướng lúc bắn, dùng projectile pool 12, damage/cooldown theo bảng, swept collision với quái và tường, không xuyên mục tiêu.
- Arrow Rain chọn cụm theo mật độ trong tầm 125, chốt tâm và cảnh báo 0,4 giây; bắn 3/4 đợt cách 0,22 giây, đúng bán kính theo cấp. Mỗi quái nhận tối đa hai hit được chấp nhận trong một lần kích hoạt.
- Cooldown và FX tiếp tục dùng clock RUNNING; projectile/FX dùng pool hữu hạn. Tái sử dụng arrow, spark và pixel ring; không thêm âm thanh hay sửa sprite/Bow/Evolution.

## File thay đổi

- Hạ tầng và tuning: `ClassSkillSystem.java`, `ClassSkillTuning.java`, `ClassSkillId.java`, `ClassSkillRenderer.java`.
- Reward/HUD: `SkillOffer.java`, `GameStrings.java`, `HudStyle.java`.
- Hook nhận bonus cho hit Bow: `ProjectileSystem.java`, nối từ `GameSession.java`.
- Kiểm tra tập trung: `tools/checks/ArcherSkillChecks.java`.

## Kiểm tra

- `:core:compileJava :android:compileDebugJavaWithJavac` — thành công.
- `ArcherSkillChecks` — PASS, 32 assertions, including Bow/Focus Mark charge behavior and Arrow Rain hit cap.
- `ClassSkillChecks` (Warrior và shared Skill Box) — PASS, 45 assertions.

Không còn lỗi triển khai đã biết. Chưa chạy emulator; cần kiểm thử cảm giác và hiển thị trong trận. Skill Mage cùng coin/unlock vẫn thuộc các task tiếp theo.
