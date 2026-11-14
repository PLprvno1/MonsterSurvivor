# TASK 03A — Upgrade Boxes & Health Drops

Ngày thực hiện: **13/11/2026**. Java 17, libGDX 1.14.2, Pixel_4 Emulator API 37. Áp dụng cân bằng Task 02.1 thay cho thông số GDD cũ.

## 1. Chức năng hoàn thành

- Damage Box, Health Box, Speed Box và bình hồi máu từ quái đã nối vào trận gameplay thật.
- Tự nhặt bằng va chạm bàn chân; tăng chỉ số có tác dụng thật, phản hồi chữ ngắn và hiệu ứng xuất hiện/thu nhỏ khi nhặt. Không có popup.
- Rương vàng/cam cho Damage, rương gỗ xanh lá cho Health, rương bạc xanh lam cho Speed; chỉ dẫn D/H/S và mũi tên khi hộp ngoài camera. Bình xanh lá dành cho health drop.
- HUD hiển thị HP/Max HP, Damage, Speed thực. Pause/lifecycle đóng băng item, Retry tạo session mới và reset chỉ số.
- Đã chạy core checks, Desktop OpenGL và **Android Studio Run trên Pixel 4**. Có phép đo ngắn với đủ 6 hộp + 20 bình máu; số liệu và giới hạn ở mục 9.
- Chỉ chuẩn bị hợp đồng mở rộng; chưa có Weapon Box, popup vũ khí hoặc nội dung Task 03B.

## 2. File tạo/sửa

Đường dẫn Java dưới `core/src/main/java/com/example/monstersurvivor/`:

| File | Vai trò/thay đổi |
|---|---|
| `config/PickupConfig.java` | Mới: cân bằng, xác suất, thời hạn, khoảng spawn và giới hạn |
| `entity/PlayerStats.java` | Mới: Damage, Max HP, Speed và quy đổi tốc độ trong từng trận |
| `pickup/PickupType.java`, `Pickup.java` | Mới: bốn loại có chức năng, trạng thái và vòng đời item |
| `pickup/PickupSpawner.java` | Mới: tìm đất hợp lệ, cùng component và fallback hữu hạn |
| `pickup/PickupSystem.java` | Mới: lịch spawn, thu hồi, collection handlers, nâng cấp, drop và feedback |
| `pickup/PickupRenderer.java`, `PickupIndicator.java` | Mới: sprite dùng chung y-sort/culling và hình học chỉ hướng HUD |
| `config/GameConfig.java` | Cấu hình pickup bất biến qua `Builder.pickups(...)` |
| `entity/Player.java` | HP tối đa động, heal có guard, movement dùng Speed; lưu lại các đoạn di chuyển đã qua collision |
| `combat/SwordCombat.java` | Đọc `player.stats.damage()` tại hit; thuật toán targeting/sector giữ nguyên |
| `entity/Enemy.java`, `ai/EnemySystem.java` | Callback duy nhất ở chuyển tiếp HP về 0; không phát lại từ Death animation |
| `gameplay/GameSession.java` | Sở hữu pickups, đồng hồ và RNG từ seed của map; áp dụng pickup trước hit kiếm trong cùng frame |
| `screen/GameScreen.java`, `ui/GameHud.java` | Nối render/HUD/Retry, log collection và log spawn khi bật fixed seed/debug |
| `render/WorldSprite.java` | Bỏ `final` để sprite pickup override draw; không đổi pivot, draw hoặc culling hiện có |
| `debug/CombatProfile.java`, `FrameMetrics.java` | Probe tùy chọn gồm hai mức 0/26 item với cùng 8 quái; thêm mục CPU PICKUPS |

Ngoài core:

- Mới: `assets/game/pickups.png` **128×32**, `pickups.json`; bốn cell Kenney CC0 nguyên pixel, padding extrude 2px. `GameAssets.java` nạp atlas một lần, Nearest/ClampToEdge; tổng 15 texture, gồm 14 texture cũ.
- `assets/assets.txt` được task Gradle `generateAssetList` hiện có cập nhật tự động để thêm hai asset mới.
- Mới: `tools/PreparePickups.java`, `tools/checks/PickupChecks.java`, `PickupAssetCheck.java`, `PickupRoute.java`, `tools/smoke/PickupSmoke.java`, `tools/run_pickup_checks.py`, `tools/run_studio_task03a.ps1`, `tools/profile_pickups_android.py`, `tools/android_pickups_smoke.py`.
- Cập nhật assertion số texture 14 → 15 trong `tools/smoke/SmokeLauncher.java`, `CombatSmokeLauncher.java`, `Task021Smoke.java`, `tools/android_smoke.py`; không chạy lại các stress suite cũ.
- Mới: báo cáo này. Nối thêm hợp đồng vào `INTEGRATION_NOTES.md`, giữ nguyên nội dung lịch sử.

Đã đối chiếu SHA-256 **8.853 file** baseline trước task: Android Java/XML/resources/Manifest, Gradle, GDD, báo cáo Task 02/02.1, sprite nguồn và các atlas/catalog cũ được giữ nguyên. `assets/assets.txt` là thay đổi sinh tự động duy nhất trong tập này, nêu trên. PixelCamera, CollisionGrid và procedural generator không thay đổi. Bằng chứng nằm trong `core/build/reports/task03a/`. Không stage/commit/push.

## 3. Cơ chế spawn hộp

- Lượt đầu sau một khoảng ngẫu nhiên đều **15–20 giây tiến trình**; mỗi lượt tiếp theo chọn lại khoảng này. NORMAL ×1, DEMO dùng `progressScale = 600/90` có sẵn.
- Tối đa **6 hộp chưa nhặt**, độc lập giới hạn health drops. Một update chỉ xét tối đa một sự kiện. Khi đầy hoặc hết chỗ hợp lệ, bỏ lượt và hẹn khoảng tiếp theo; không tích lũy burst spawn.
- Chọn loại đều nhau giữa ba hộp. Khi Speed đã đạt 20, chỉ chọn Damage/Health.
- Vị trí tâm tile trong bán kính **48–280 units** quanh player; có thể ở trong hoặc ngoài camera. Khoảng cách tối thiểu ngăn sinh ngay dưới chân. Tránh item khác và kiểm tra footprint 10×8.
- **48 lần thử ngẫu nhiên**, sau đó tìm theo vòng từ gần đến xa với tối đa **1.024 ô**. Không tìm A* hay flood-fill tại thời điểm spawn. Không có đất phù hợp thì bỏ lượt.
- Tất cả vị trí phải thuộc ground component của player trên navigation của map hiện tại, chặn nước/cây/đá/biên. API đặt item theo sự kiện cũng kiểm tra đất, component, khoảng cách item và cap.
- Hộp hết hạn sau **60 giây gameplay thực**, kể cả Demo; nhấp nháy 5 giây cuối. Pause và các phase dừng gameplay không tăng tuổi item.
- Seed map truyền vào session; RNG cho lịch, loại, phần thưởng, drop và vị trí được tách riêng bằng seed dẫn xuất. Seed + cùng diễn biến input/timestep tái hiện được kết quả; không kỳ vọng timestamp Android/Desktop giống tuyệt đối nếu timestep khác.

## 4. Hệ thống nâng cấp chỉ số

| Loại | Tác dụng |
|---|---|
| Damage | Ban đầu 1; mỗi hộp +1/+2/+3 đều nhau. Hit kiếm đọc chỉ số hiện tại, kể cả nhặt hộp ở đúng frame phát hit |
| Health A | Hồi 3, chặn ở Max HP |
| Health B | Tăng Max HP 2, hồi 2. Khi đang thiếu máu, xác suất A/B mặc định 50/50; khi đầy máu luôn chọn B |
| Speed | Ban đầu 10 = 80 units/s; +1 mỗi hộp, tối đa 20 = 160 units/s; quy đổi `Speed × 8` |

Speed Box đã có trên map khi đạt trần sẽ áp dụng phần thưởng Damage, hiển thị `+Damage`, để hộp vẫn có tác dụng. Không có cap cân bằng bổ sung cho Damage/Max HP; phép cộng bão hòa ở giới hạn `int` chỉ để tránh overflow.

Giữ nguyên normalize đường chéo, delta gameplay và collision substep ≤3 units. Chỉ số mới áp dụng vào displacement lần cập nhật kế tiếp; Damage/HP có hiệu lực ngay khi collection được xử lý. Miễn nhiễm 0,7 giây không bị heal hay nâng cấp reset. `Player.heal` và collection từ chối player đã chết.

Retry mới trả về **10/10 HP, Damage 1, Speed 10**, không giữ item, hiệu ứng hay toast của session cũ. Pause/Resume giữ nguyên nâng cấp hiện có.

## 5. Cơ chế rơi máu

- `Enemy.damage(...)` phát callback đúng một lần tại chuyển tiếp HP >0 → 0. Corpse còn phát đủ Death clip, không gọi lại drop. Các nguồn damage tương lai dùng cùng API vẫn đi qua hook này.
- EASY **8%**, HARD **5%**; khi HP ≤3 cộng **4 điểm phần trăm** thành 12%/9%, chặn xác suất tối đa 100%.
- Mỗi bình chọn đều **1–2 HP**. Tự nhặt, không vượt Max HP; khi đang đầy máu bình vẫn nằm lại để dùng sau, không bị tiêu hao vô ích.
- Chọn đất gần tâm tile của xác; nếu không hợp lệ, tìm vòng gần trước, tối đa 96 units và ngân sách fallback chung. Vì vậy dơi chết trên nước có thể để bình trên bờ cùng vùng đi bộ của player. Không có bờ hợp lệ thì không sinh bình.
- Tối đa **20 bình**, tuổi thọ **30 giây gameplay thực**, nhấp nháy 5 giây cuối. Đây là lựa chọn cân bằng ban đầu để giới hạn bộ nhớ và lượng đồ trên map. Không có XP.

## 6. Cấu hình xác suất và giới hạn

Điểm chỉnh duy nhất là `PickupConfig`, truyền qua `new GameConfig.Builder().pickups(tuning)`. Constructor có tham số cho interval/lifetime/caps, xác suất Easy/Hard/boost và khoảng heal drop; các baseline còn lại được khai báo rõ trong cùng lớp để chỉnh sau playtest.

| Field | Mặc định |
|---|---:|
| `intervalMin`, `intervalMax` | 15 / 20 giây tiến trình |
| `boxCap`, `boxLifetime`, `blinkSeconds` | 6 / 60s / 5s |
| `spawnMin`, `spawnMax` | 48 / 280 units |
| `randomAttempts`, `fallbackCells` | 48 / 1.024 |
| `damageMin`, `damageMax` | 1 / 3 |
| `healthHeal`, `maxHpGain`, `maxHpHeal`, `maxHpChance` | 3 / 2 / 2 / 0,5 |
| `initialSpeed`, `maxSpeed`, `unitsPerSpeed` | 10 / 20 / 8 |
| `easyDropChance`, `hardDropChance` | 0,08 / 0,05 |
| `lowHpThreshold`, `lowHpBonus` | 3 / 0,04 |
| `dropHealMin`, `dropHealMax`, `dropCap`, `dropLifetime`, `dropRadius` | 1 / 2 / 20 / 30s / 96 units |

Collection effects tối đa 8, tồn tại 0,3s; toast 1,4s. Không tạo texture/animation hay đọc lại sprite nguồn trong loop. Renderer giữ cùng AssetManager, SpriteBatch, y-sort và culling; item/effect hết hạn được bỏ khỏi cả system lẫn cache renderer.

## 7. Các trường hợp collision/map

- Collection quét AABB chân player với footprint item qua **các đoạn từng trục đã được collision chấp nhận**, thay vì nối tắt từ đầu tới cuối frame. Không bỏ sót hộp chạy qua ở Speed 20, không nhặt xuyên góc khi trượt tường.
- Có ground line-of-sight guard tại điểm tiếp xúc; không nhặt xuyên cây/đá/nước. Vị trí item phải đủ footprint và liên thông trước khi đặt.
- Map kín hoặc chỉ còn ô player: tìm hữu hạn, bỏ lượt, hẹn lại; không lặp vô hạn hoặc đặt vào blocker.
- Health drop được kiểm tra bằng lớp ground ngay cả khi quái là Bat.
- Chỉ dẫn nằm dưới vùng HP/stats/timer/Pause; tránh joystick, dòng trợ giúp dưới cùng, toast và các nút/FPS debug. Chỉ hộp ngoài camera có chỉ dẫn; bình máu không làm tăng số mũi tên. Các mũi tên gần cùng hướng có thể trùng nhau.
- `PixelCamera`, world atlas/padding và thuật toán collision/procedural map cũ giữ nguyên. Atlas pickup mới có padding riêng, không ghi đè `world.png` hoặc `catalog.json`.

## 8. Kết quả build/kiểm thử

| Kiểm tra | Kết quả |
|---|---|
| `:core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain --info` | **PASS**; không gọi task xuất gói riêng |
| `python tools/run_pickup_checks.py` | **PASS**: 21.438 assertions logic, 17 checks targeting/combat Task 02.1, 26 checks Desktop OpenGL |
| Asset pixel/padding | **PASS**: 1.601 checks, chỉ giải mã Kenney CC0; các cell gốc và extrude khớp pixel |
| Core | +1/+2/+3 thật; same-frame sword damage; hai nhánh Health; cap/no resurrection; tốc độ thật 30/60/120 FPS; chạm nhanh; tường/component; pause; 60s thực Normal/Demo; Demo cap; RNG ba seed; death callback và Bat trên nước |
| Desktop GL/GameScreen | Input thật, HUD/item/indicator không có GL error; đủ 6+20 item; hết hạn; Pause/lifecycle; Death/Game Over; Retry xóa session/item/effect/toast và reset stats; tái dùng 15 texture; callback Home |
| Android Studio Run + ADB | **PASS, 31 checks thực tế**: native EASY/DEMO và EASY/NORMAL, seed 42/8.883 ô đi được, progress spawn, Pause/lifecycle, joystick nhặt cả ba hộp, chỉ số thay đổi và trận mới reset baseline; không crash/missing asset |

Bằng chứng: `compile.log`, `PickupChecks.log`, `PickupAssetCheck.log`, `Task021Checks.log`, `PickupSmoke.log`, `android-gameplay-logcat.txt`, `android-pickups-smoke.json`, `studio-run.log` trong `core/build/reports/task03a/`. Assertion lặp trên nhiều frame/seed được tính vào tổng logic; không phải 21.438 kịch bản độc lập.

SDK có warning schema XML v4/v3 và Gradle có deprecation warning hiện có; compile kết thúc thành công. Không chạy lại toàn bộ stress suite AI/map Task 02.1.

## 9. Tình trạng FPS

Đo trên **Pixel_4 API 37, 2280×1080, Google SwiftShader**, thông qua callback render của app đã được Android Studio Run triển khai. Hai stage liên tiếp, cùng 8 quái, map so sánh legacy cố định; mỗi stage 30 frame warmup + 60 frame lấy mẫu. Flag chẩn đoán cục bộ tắt sau đo; gameplay bình thường không dùng tuning/profile này.

| Tải | FPS callback trung bình | p95 callback | CPU pickup update | World draw calls TB |
|---|---:|---:|---:|---:|
| 8 quái, 0 item | 32,73 | 45,80 ms | 0,026 ms | 12,07 |
| 8 quái, 6 hộp + 20 bình | 30,41 | 50,38 ms | 0,129 ms | 13,47 |

Tải item tối đa có chi phí đo được: khoảng 7% FPS trong mẫu ngắn này, thêm khoảng 1,4 world draw calls; phần render chiếm nhiều thời gian hơn logic. Đây là số đo emulator phần mềm, có nhiễu và hai stage không cùng thời điểm, không suy ra FPS điện thoại thật hay chứng minh đạt 60 FPS. Không có đo GPU hoặc stress dài hạn.

CPU probe nhỏ trên desktop JVM, 20.000 update: khoảng 0,029 µs khi rỗng / 0,181 µs với 26 item; chỉ tham khảo CPU logic, không thay thế số đo Android. Dữ liệu Android: `android-pickups.csv`, `android-pickups-summary.json`, `android-pickups-environment.txt`.

## 10. Hạn chế còn tồn tại

- Balance là baseline; chưa có playtest dài hạn cho Damage/Max HP không giới hạn gameplay. Health drops bị chặn khi không có đất hợp lệ gần xác hoặc đã đầy cap.
- Cùng frame có nhiều pickup thì mọi hiệu ứng áp dụng, mọi sprite có hiệu ứng nhặt, nhưng chỉ toast cuối được hiển thị. Mũi tên gần nhau có thể chồng nhau; chưa có gom nhóm/khoảng cách số.
- Hết hạn 60s, Speed 20, các nhánh heal/max và death-drop bắt buộc đã được xác minh bằng core/GL; không coi đó là bằng chứng đã thao tác từng trường hợp trên Android. Lượt Android nhặt ba hộp dùng xác suất tự nhiên, không ép damage/drop hoặc dịch chuyển player; việc rơi máu từ kill theo xác suất được kiểm thử quyết định ở core, hiển thị bình được kiểm thử GL/probe Android.
- Đợt Android kiểm tra trận mới reset baseline; reset bằng nút Retry và dọn item/stats được kiểm tra trực tiếp ở Desktop GL. Retry/Home/HP immunity vẫn giữ kiến trúc đã có; chưa làm bridge kết quả/Achievements native.
- Không triển khai Weapon Box, vũ khí mới, Dash/Ultimate, boss, ngày/đêm, Victory hoặc thay đổi Android UI.

## 11. Chạy bằng Android Studio Run

1. Mở `D:\Android\MonsterSurvivor`; chọn cấu hình **android**, thiết bị **Pixel_4**, nhấn **Run/Rerun** đầy đủ. Thay đổi field/class cần full Run thay cho Apply Changes.
2. Home → Play → EASY hoặc HARD; NORMAL hoặc DEMO → Start. Đi bằng joystick đến rương. Theo dõi DMG, SPEED và HP/Max HP; nhặt bình khi thiếu máu.
3. NORMAL: đợi lượt đầu 15–20 giây gameplay. DEMO: lịch tiến trình nhanh hơn, nhưng hộp vẫn sống 60 giây thực, tối đa 6; dừng Pause rồi Resume để kiểm tra tuổi/lịch không trôi.
4. Khi Game Over, Retry phải về 10/10 HP, DMG 1, SPEED 10, không còn item cũ; Home quay về native Home. Nút Home hệ điều hành rồi quay lại vẫn cần explicit Resume.
5. Tái hiện map bằng `Builder.fixedSeed(42)` hoặc file local app `files/map-seed.txt` nội dung `42`. Bỏ flag để quay về map mới mỗi trận. Các flag kiểm thử đã được dọn/khôi phục sau phiên này.

Công cụ của phiên đã kích hoạt cấu hình Run có sẵn, xác nhận deployment mới trong log Android Studio và chạy input trên emulator. Package debug nội bộ do **Run** sinh/triển khai không phải sản phẩm bàn giao. Không gọi assemble/bundle/release/signing/publishing/install riêng, không xuất/bàn giao APK/AAB, không sửa Gradle/Manifest/native UI.

Để kiểm tra lại core: compile như mục 8 rồi chạy `python tools/run_pickup_checks.py`. Sau khi Android Studio Run đã triển khai bản mới, có thể dùng `python tools/android_pickups_smoke.py` hoặc probe ngắn `python tools/profile_pickups_android.py`; cả hai không build/install. Script Run theo tọa độ toolbar chỉ phù hợp bố cục IDE đã kiểm tra của máy này.

## 12. Hợp đồng tích hợp Weapon Box cho Task 03B

- Session là owner duy nhất của item; mở rộng `PickupType` bằng loại **có chức năng**, region tương ứng và danh sách loại spawn đủ điều kiện. Hiện random pool chỉ gồm ba hộp Task 03A; không có Weapon Box ẩn/chưa dùng.
- `PickupSystem.register(type, CollectionHandler)` đăng ký xử lý. Handler nhận item và player; **true** nghĩa là đã hoàn tất hiệu ứng, system mới remove/đánh dấu collected và chạy visual. **false** giữ item chưa tiêu thụ.
- Task 03B phải giữ `pending item id` để không mở lại modal mỗi frame tiếp xúc. Tạo offer hợp lệ theo slot/level, khóa input và dừng simulation bằng state modal rõ ràng; không cộng đồng hồ tiến trình hay tuổi pickup khi modal dừng trận. Phần state/modal này chưa được làm ở Task 03A.
- Xác nhận lựa chọn áp dụng đúng một lần rồi hoàn tất collection. Khi hủy hoặc không có offer hợp lệ, giữ item hoặc chọn fallback có tác dụng đã được định nghĩa; không sinh hộp vô hiệu. Dùng checked placement hiện có để giữ component/collision/cap/lifetime.
- Retry/Home phải xóa pending selection cùng session; không giữ callback/modal của player cũ. Giữ riêng Damage/Health/Speed auto-collect, không thêm popup cho ba loại này.
- Vũ khí tương lai tiếp tục gây damage qua `Enemy.damage(...)` để drop chỉ phát ở transition death; dùng current player stats theo hợp đồng damage của vũ khí. Không phát drop từ renderer hay Death frame.
