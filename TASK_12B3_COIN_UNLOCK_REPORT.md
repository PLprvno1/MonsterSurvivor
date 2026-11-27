# Task 12B3 – Coin & Character Unlock

## Hoàn thành

- Thêm `CharacterProgressManager` với SharedPreferences riêng `monster_survivor_character_progress`. Ví, nhân vật sở hữu/đang chọn và UUID trận đã quyết toán được lưu offline; số dư và UUID ghi cùng một `commit()`. Reset Settings không chạm tới dữ liệu này.
- Quái thường có 12% cơ hội rơi 1 xu trên bản đồ; xu cần nhặt trong bán kính 12 và tự biến mất sau 20 giây. Giới hạn 48 xu rơi cùng lúc. HUD hiển thị tổng xu tạm của trận, gồm xu nhặt được và thưởng trực tiếp.
- Thưởng một lần khi Boss chết hợp lệ: Mini Boss 10, Lich Lord 25, Endless Boss 12; thắng Normal cộng 15. Demo, diagnostic và debug không tạo/nhận xu chính thức. Game Over/Victory và xác nhận về Home quyết toán cùng UUID; UUID trùng bị từ chối.
- Warrior miễn phí; Archer giá 100 xu; Mage giá 220 xu. Màn chọn nhân vật hiển thị ví, trạng thái và giá; nút mở khóa chỉ hoạt động khi đủ xu. Mua trừ xu, lưu quyền sở hữu và chọn nhân vật trong cùng commit. `GameModeActivity`, `AndroidLauncher` và Core đều kiểm tra quyền sở hữu; ID thiếu/sai/đang khóa rơi về Warrior.
- Dùng lại coin FX có sẵn; không thêm asset hoặc âm thanh. Chuỗi UI có tiếng Việt và tiếng Anh.

## File chính

- Core: `CharacterOwnership`, `CharacterProgressLedger`, `CoinSettlement`, `PickupType`, `PickupSystem`, `PickupRenderer`, `GameSession`, `MatchResult`, `GameHud`, `GameScreen`, `MonsterSurvivorGame`, `GameStrings`.
- Android: `CharacterProgressManager`, `AndroidLauncher`, `GameModeActivity`, `activity_game_mode.xml`, `values/strings.xml`, `values-en/strings.xml`.
- Kiểm tra: `tools/checks/CoinProgressChecks.java`.

## Kiểm tra

- `:core:compileJava :android:compileDebugJavaWithJavac` — thành công.
- `python tools/run_skill_checks.py CoinProgressChecks` — đạt 21 assertions, gồm tạo/nhặt xu tạm, loại Demo/debug, quyết toán UUID trùng, giá/mua nhân vật, kiểm tra quyền sở hữu và chống tràn số dư.
- Không chạy emulator. SharedPreferences được kiểm tra qua code path và compile; không chạy thử lưu trữ trên thiết bị.

Không sửa thống kê/thành tích Task09; không commit/push hoặc xuất APK/AAB.
