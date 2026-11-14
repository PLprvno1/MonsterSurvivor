# TASK 03B — Weapon Box & Multi-Weapon Combat

Ngày thực hiện: **13/11/2026**. Java 17, libGDX 1.14.2. Dùng cân bằng Task 02.1 và pickup Task 03A. Đã đọc GDD, các báo cáo, integration notes và source thực tế; không sửa tài liệu GDD.

## 1. Kiến trúc hệ thống vũ khí

`GameSession` sở hữu `WeaponSystem`, `WeaponLoadout`, `WeaponSelection` và `PickupSystem`. Trận mới chỉ có Sword cấp 1; tối đa ba loại vũ khí đồng thời. Bow, Fireball và Lightning tự hoạt động song song với kiếm, không thay hướng di chuyển theo hướng bắn.

- `WeaponConfig.Level`: bảng chỉ số bất biến theo loại/cấp, hỗ trợ override qua `GameConfig.Builder.weapons(...)` / `withLevel(...)`.
- `WeaponInstance`: level, cooldown, retry timer và số lần kích hoạt riêng của từng vũ khí. Sword tiếp tục dùng action/cooldown gốc trong `Player`; instance phản ánh lại trạng thái này cho HUD.
- `WeaponLoadout`: ba slot cố định, chống trùng loại, kiểm tra cấp và capacity trước mọi grant.
- `WeaponSystem`: điều phối kiếm hiện có và hai vũ khí bổ sung trong loadout. Tìm mục tiêu khi đến lượt bắn; khi chưa có mục tiêu hợp lệ, thử lại sau 0,12s.
- `ProjectileSystem`: pool cố định cho projectile/effect, swept collision, hit, AoE và thu hồi.
- `WeaponSelection` / `WeaponOffer`: pending pickup ID, token, snapshot offer hợp lệ và transaction chọn thưởng; độc lập UI.
- `WeaponRenderer` / `WeaponHud`: hiệu ứng hình học, ba slot và modal libGDX; không có Android UI mới.

SwordCombat được mở rộng bằng hook tuning level, không viết lại thuật toán tìm mục tiêu, windup, sector hit hay contact damage. Sword cấp 1 giữ range 48, detection 60, windup 0,15s, chu kỳ 1s, playback 0,65s và khả năng vừa chạy vừa đánh phía sau.

## 2. File đã tạo/sửa

Đường dẫn core dưới `core/src/main/java/com/example/monstersurvivor/`:

| File | Thay đổi |
|---|---|
| `config/WeaponConfig.java` | Mới: bảng cấp 1–5, scaling và cap tài nguyên |
| `weapon/WeaponType.java`, `WeaponInstance.java`, `WeaponLoadout.java` | Mới: loại, state và ba slot duy nhất |
| `weapon/WeaponOffer.java`, `WeaponSelection.java` | Mới: offer, fallback và pending/token transaction |
| `weapon/CombatGeometry.java`, `WeaponTargeting.java` | Mới: swept hit, wall fraction và tìm mục tiêu có LOS |
| `weapon/ProjectileSystem.java`, `WeaponSystem.java`, `WeaponRenderer.java` | Mới: đạn, AoE, chain, clocks, pooling và VFX |
| `ui/WeaponHud.java` | Mới: HUD ba slot và modal tối thiểu |
| `config/GameConfig.java`, `PickupConfig.java` | Weapon config và xác suất Weapon Box |
| `entity/Player.java`, `combat/SwordCombat.java` | Cho phép cycle/tuning kiếm theo cấp; giữ action clock, movement, targeting và collision |
| `gameplay/GameSession.java` | Sở hữu weapons/selection, phase chọn vũ khí, commit/end và tổng kills chung |
| `pickup/PickupType.java`, `PickupSystem.java` | Weapon Box, weighted pool, pending collection và complete qua handler |
| `pickup/PickupIndicator.java` | Tránh vùng ba slot HUD mới |
| `assets/GameAssets.java` | Một texture trắng 1×1 tạo đúng một lần, dùng chung VFX và dispose cùng assets |
| `input/GameInput.java` | Modal input priority, press/release, debounce, reset joystick và Back |
| `ui/GameHud.java`, `screen/GameScreen.java` | Nối modal/slots/VFX, tổng kills, lifecycle/Retry/Home và telemetry |
| `debug/CombatProfile.java` | Probe hữu hạn cho hai tổ hợp ba vũ khí hợp lệ |

Công cụ mới: `tools/checks/WeaponChecks.java`, `tools/smoke/WeaponSmoke.java`, `tools/run_weapon_checks.py`, `tools/run_studio_task03b.ps1`, `tools/android_weapons_smoke.py`, `tools/profile_weapons_android.py`.

Cập nhật fixture số texture trong `PickupSmoke.java`, `Task021Smoke.java`, `SmokeLauncher.java`, `CombatSmokeLauncher.java`; `PickupRoute.java` nhận đường dẫn output để không ghi đè bằng chứng task cũ. Thêm báo cáo này và nối mục Task 03B vào `INTEGRATION_NOTES.md`.

Đối chiếu SHA-256 **8.859 file bảo vệ: không thay đổi**, gồm Android Java/XML/theme/resources/Manifest, Gradle, GDD, báo cáo cũ, toàn bộ asset nguồn/runtime. PixelCamera, WorldSprite, CollisionGrid, ForestMap/ForestGenerator, A*/EnemySystem/NavigationGrid giữ nguyên byte. Không có asset tải mới, không sửa sprite Tiny Questers/Willosuna, không dùng N3XUS, không đưa hình Tiny Questers vào công cụ AI. Không commit/push.

## 3. Weapon Box và popup

- Dùng lịch spawn chung **15–20 giây tiến trình**, cap **6 hộp**, cùng kiểm tra footprint, đất hợp lệ, component đi bộ và fallback hữu hạn của Task 03A. Tuổi hộp vẫn **60 giây thực**, không tăng tốc bởi Demo.
- `PickupConfig.weaponChance` mặc định **0,33**. Khi bật xác suất >0, bảo đảm ít nhất một trong **hai lượt spawn hộp thành công đầu tiên** là Weapon Box. Sau đó dùng xác suất 33%; các lượt còn lại chia đều cho Damage/Health/Speed còn hữu ích. Giá trị 0 tắt Weapon Box tự sinh và bảo đảm sớm.
- Chỉ bật pool khi session đã đăng ký handler có chức năng và còn thưởng hợp lệ. Khi ba vũ khí đạt cấp 5, vẫn còn các stat fallback hữu ích; không sinh hộp vũ khí giả.
- Dùng lại rương Kenney CC0 hiện có, tint tím, chỉ dẫn **W**; không thêm PNG hay sửa atlas.
- Tiếp xúc gọi handler và giữ hộp chưa tiêu thụ, lưu pending ID, tạo offer một lần, chuyển `RUNNING → CHOOSING_WEAPON`. Sau đó không chạy combat/spawn trong phần còn lại của frame mở modal.
- Khi đang chọn, `GameSession.update` dừng movement, AI, animation, cooldown, projectile/VFX, hai đồng hồ trận và tuổi pickup. UI tiếp tục render để người chơi chọn.
- Lượt đầu luôn cho ba lựa chọn mới **Bow / Fireball / Lightning**. Các lượt sau lấy tối đa ba loại đủ điều kiện, không trùng; nếu thiếu, thêm stat fallback.
- Chọn hợp lệ áp dụng đúng một lần, handler trả true qua `PickupSystem.collect(pendingId)`, system mới remove hộp/chạy feedback; clear pending, trở lại RUNNING. Cả token và phase được kiểm tra lại khi commit.
- Token tăng duy nhất trong process, **không lặp lại sau Retry/session mới**. Callback cũ không thể chọn nhầm popup mới dù item ID của trận mới bắt đầu lại từ 1.
- Touch giữ quyền theo pointer, chỉ commit khi thả trong cùng card. Touch bị cancel không nhận thưởng. Reset input lúc mở/đóng, bỏ delta đầu khi tiếp tục và debounce 250ms sau commit; không kéo joystick cũ sang trận tiếp tục.
- Back/Esc/P mở Pause phía trên lựa chọn; Resume quay về cùng offer, Home kết thúc session. Popup cũng có nút Home. Không bấm xuyên xuống joystick hoặc gameplay.

## 4. Quy tắc ba weapon slots

Ban đầu `Sword 1 / Empty / Empty`. Nhận loại chưa sở hữu chỉ được thêm nếu còn slot. Nhận loại đã sở hữu tăng một cấp, không tạo instance thứ hai. Khi đủ ba slot, offer chỉ gồm vũ khí đang sở hữu còn nâng được và stat fallback; không có loại thứ tư.

HUD nằm giữa phía dưới, trên footer, tránh joystick/debug controls. Hiển thị SW/BOW/FIRE/LTN, level, slot trống và thanh cooldown. Chỉ dẫn hộp được đẩy ra khỏi vùng này; HP, timer, Pause và camera giữ vị trí/cơ chế cũ.

## 5. Level 1–5

Mỗi grant lại cùng loại tăng đúng một level; cấp 5 là trần thường. Mọi level có thay đổi chiến đấu thật: damage/bonus hoặc range, cadence, piercing, blast radius hay chain count. Cooldown đang chạy được chặn về chu kỳ mới khi nâng cấp; không reset animation hay tạo lại weapon.

Khi hết nâng cấp vũ khí, dùng các offer có tác dụng:

- **Damage +2**: base damage chung cho cả bốn vũ khí.
- **Max HP +2** và heal 2, chặn ở Max HP; không hồi sinh.
- **Speed +1** nếu dưới 20.

Khi Speed đã 20, chỉ hiển thị hai fallback còn hợp lệ thay vì lựa chọn giả. Damage/Max HP không có cap cân bằng bổ sung, phép cộng bão hòa ở giới hạn số nguyên. **Không có Evolution**.

## 6. Cấu hình từng vũ khí

Mỗi dãy dưới đây lần lượt là level 1 → 5; units là world units, thời gian là giây gameplay thực.

| Vũ khí | Cooldown | Range | Damage multiplier | Bonus damage |
|---|---|---|---|---|
| Sword | 1 / 0,945 / 0,89 / 0,835 / 0,78 | 48 / 48 / 52 / 52 / 56 | 1 / 1,0625 / 1,125 / 1,1875 / 1,25 | 0 / 1 / 1 / 2 / 2 |
| Bow | 1,4 / 1,325 / 1,25 / 1,175 / 1,1 | 220 / 228 / 236 / 244 / 252 | 1 / 1,1 / 1,2 / 1,3 / 1,4 | 0 / 1 / 1 / 2 / 2 |
| Fireball | 3 / 2,85 / 2,7 / 2,55 / 2,4 | 180 / 188 / 196 / 204 / 212 | 1,5 / 1,625 / 1,75 / 1,875 / 2 | 0 / 0 / 1 / 1 / 2 |
| Lightning | 4 / 3,8 / 3,6 / 3,4 / 3,2 | 170 / 178 / 186 / 194 / 202 | 1 / 1,125 / 1,25 / 1,375 / 1,5 | 0 / 1 / 1 / 2 / 2 |

| Thuộc tính | Level 1 → 5 |
|---|---|
| Bow speed | 250 / 270 / 290 / 310 / 330 |
| Bow số mục tiêu tối đa mỗi mũi tên | 1 / 1 / 2 / 2 / 3 |
| Bow lifetime | 1,4s ở mọi cấp |
| Fireball speed | 150 / 160 / 170 / 180 / 190 |
| Fireball blast radius | 35 / 39 / 43 / 47 / 51 |
| Fireball lifetime | 2s ở mọi cấp |
| Lightning số mục tiêu tối đa | 2 / 2 / 3 / 3 / 4 |
| Lightning khoảng lan mỗi bước | 70 / 75 / 80 / 85 / 90 |

Tuning tập trung trong `WeaponConfig`, không sửa HP/speed/cadence quái Task 02.1. Hook kiếm tôn trọng các override `GameConfig.sword(...)` sẵn có; các hệ số level chỉ bổ sung lên cấu hình đó. Windup và Attack playback không đổi theo cấp.

## 7. Projectile, AoE và lightning chain

**Bow:** chọn quái sống gần nhất trong range, có hành lang bắn hợp lệ. Đạn có vị trí, hướng chuẩn hóa, speed, age/lifetime, damage snapshot và danh sách ID đã hit. Swept segment–expanded AABB tìm hit sớm nhất trong frame; không nhảy qua mục tiêu khi chạy nhanh. Cấp 1 dừng ở hit đầu; cấp cao giữ ID để không đánh lại cùng quái khi piercing qua nhiều frame.

**Vật cản:** dùng `CollisionGrid.clearLine` hiện có; không sửa grid. Nếu đường tới vị trí mới bị chặn, tìm điểm va chạm bằng tối đa 12 bước chia đôi rồi chỉ nhận hit trước điểm đó. Cây/đá/biên chặn đạn; nước không chặn vũ khí bay, giữ khả năng đánh Bat ở gần nước. Hết lifetime/ra khỏi map phải thu hồi.

**Fireball:** bay tới hướng target lúc phóng. Chạm enemy hoặc wall tạo đúng một vụ nổ tại điểm va chạm; duyệt resident sống một lần, kiểm tra khoảng cách tròn và LOS từ tâm nổ, gọi damage một lần cho mỗi mục tiêu. Hiệu ứng nổ ngắn 0,32s không phải vùng gây damage liên tục; không đổi terrain. Đạn hết lifetime không tự nổ gây damage.

**Lightning:** tìm primary có LOS, đánh tức thời rồi lặp tối đa số mục tiêu cấu hình. Mỗi bước chọn mục tiêu sống gần nhất có LOS trong chain range, bỏ các ID đã đánh. Cấp 1 tối đa hai mục tiêu, cấp 5 bốn. Dùng mảng ID tái sử dụng; không tìm lại chuỗi ở các frame hiệu ứng. Bolt hiển thị 0,24s bằng ba đoạn gấp khúc ngắn.

Giới hạn **48 projectiles + 48 effects**, pool được điền trước. Đạt cap đạn thì bỏ lần bắn và thử lại sau 0,12s; cap effect thì thu hồi một effect cũ. Impact effect 0,14s. Không tạo texture/animation/collection mỗi frame; culling trước khi vẽ. VFX dùng chung một texel trắng Nearest/ClampToEdge tạo ở startup, dispose cùng GameAssets. Tổng 16 texture runtime = 15 texture file cũ + một texture thủ tục 1×1; các PNG/catalog/atlas cũ giữ nguyên.

## 8. Scaling sát thương và death-drop integration

`damage = max(1, round(PlayerStats.damage() × multiplier[level] + bonus[level]))`, chặn overflow ở `Integer.MAX_VALUE`.

- Sword đọc damage/tuning ở đúng sự kiện hit, nên Damage Box cùng frame vẫn có tác dụng ngay.
- Bow/Fireball chụp damage lúc phóng; nâng Damage sau đó áp dụng cho đạn mới, không thay đổi ngược đạn đang bay.
- Lightning đọc damage lúc bắt đầu chuỗi và dùng cùng giá trị cho chuỗi đó.
- Mọi vũ khí gọi `Enemy.damage(...)`, bỏ quái đã chết, dùng hook death transition duy nhất của Task 03A. Renderer/Death animation không phát damage hoặc drop.
- **`GameSession.kills` là tổng kills dùng cho HUD/Game Over**, tăng từ hook death chung. `SwordCombat.kills` chỉ còn là thống kê riêng của kiếm phục vụ tương thích/debug; không cộng thêm nó lần nữa.
- Player chết hoặc session kết thúc sẽ thu hồi projectile/effect; phase không RUNNING không cập nhật combat. Retry tạo hệ thống mới, Home/end vô hiệu pending/token cũ.

## 9. Kiểm thử

| Kiểm tra | Kết quả |
|---|---|
| Core + Android compile | **PASS**: `:core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain --info` |
| `WeaponChecks` | **223 checks PASS** |
| `WeaponSmoke` trên Desktop OpenGL | **45 checks PASS** |
| Sword Task 02.1, nhóm targeting/combat | **17 checks PASS** |
| Pickup Task 03A logic | **21.438 assertions PASS**; gồm các bất biến lặp qua frame/seed |
| Pickup Task 03A Desktop GL | **27 checks PASS**, tương thích texture/các loại pickup mới |
| Android native/ADB | **52 checks PASS**, chi tiết mục 10 |

Đã xác minh hành vi: Sword 1 ban đầu; pool/bảo đảm sớm; reserve/commit một lần; modal freeze cả projectile/VFX/cooldown đang hoạt động; slot/cấp tối đa; fallback khi ba vũ khí đạt cấp 5; stat scaling cả bốn loại; arrow swept nearest-hit/piercing và blocker; Fire AoE/occlusion/không hit lặp; chain unique/LOS/cap; các vũ khí hoạt động cùng kiếm; kill/drop duy nhất từ ranged; Normal/Demo cùng cooldown thực; cap/thu hồi; input cancel, Back, lifecycle, double tap, Home và Retry.

Kiểm thử riêng token cũ của trận trước trên popup đầu trận sau: core và GL đều từ chối. GL dùng touch press/release thật qua InputAdapter, kiểm tra không leak phím/joystick, không có GL error và Retry tái dùng texture. Các kiểm thử không readback artwork nhân vật/quái.

Bằng chứng tại `core/build/reports/task03b/`: `compile.log`, `WeaponChecks.log`, `WeaponSmoke.log`, `Task021Checks.log`, `PickupChecks.log`, `PickupSmoke.log`, `protected-audit.json`. SDK XML/deprecation warning hiện có không làm compile thất bại. Không chạy lại stress suite map/AI lớn không liên quan.

## 10. Android Studio Run

Đã kích hoạt cấu hình **android / Pixel_4** đang mở trong Android Studio bằng Run/Rerun đầy đủ; xác nhận deployment mới trong log IDE trước khi kiểm tra. Thiết bị API 37, landscape 2280×1080, Google SwiftShader.

ADB chỉ dùng để thao tác/đọc telemetry sau Run, không build/install gói riêng:

- Native Home → chọn EASY/DEMO → Start, map fixed seed 42 có 8.883 ô đi được.
- Đi bằng joystick thật tới Weapon Box tự sinh; không dịch chuyển player hoặc tiêm inventory/damage.
- Ba trận chọn riêng Bow, Fireball, Lightning bằng touch trên card. Xác nhận cả ba tự kích hoạt, Fireball có explosion, Lightning có hit thật; Weapon Box tiêu thụ một lần.
- Modal chỉ mở một lần cho pending item; double tap không nhận hai reward; thả joystick giữ nguyên vị trí sau chọn.
- Back mở Pause trên modal, lifecycle trở về cùng trận; explicit Resume quay về lựa chọn cũ với cùng thời gian và vị trí.
- Token giữa các trận native khác nhau; Home về đúng Activity native, không crash hoặc missing asset.

Bằng chứng: `studio-run.log`, `studio-deployment.txt`, `android-gameplay-logcat.txt`, `android-weapons-smoke.json`. Không suy kết quả slot đầy/cấp 5/Retry từ lượt Android: các nhánh này được xác minh quyết định ở core và Desktop GL; ba trận Android xác minh luồng thật với thưởng tự nhiên.

Chạy lại: chọn **android**, thiết bị **Pixel_4**, nhấn **Run/Rerun**; không dùng Apply Changes cho thay đổi class/field. Sau khi Run đã triển khai, `python tools/android_weapons_smoke.py` thao tác menus/joystick/modal; cần grid route do `python tools/run_weapon_checks.py` sinh. Helper Run theo tọa độ chỉ phù hợp bố cục IDE đã kiểm tra của máy này.

Không chạy assemble/bundle/export/release/signing/publishing hoặc install như nhiệm vụ riêng. Package debug nội bộ do Android Studio Run tạo/triển khai không phải artifact bàn giao; không yêu cầu cài APK thủ công, không xuất APK/AAB.

## 11. FPS và chi phí

Đo trên **Pixel_4 API 37, landscape 2280×1080, Google SwiftShader**.

| Loadout | FPS callback TB | p95 callback | CPU combat TB | World draw calls TB | Đạn/effect tối đa trong mẫu |
|---|---:|---:|---:|---:|---:|
| Sword 1 | 31,21 | 44,95 ms | 0,132 ms | 7,63 | 0 / 0 |
| Sword 1 + Bow 3 + Fireball 3 | 31,13 | 48,72 ms | 0,190 ms | 8,67 | 2 / 1 |
| Sword 1 + Fireball 3 + Lightning 3 | 32,40 | 49,27 ms | 0,171 ms | 8,35 | 1 / 3 |

FPS mẫu cuối nằm khoảng 31,1–32,4; phần CPU combat gồm cả kiếm/contact/đạn tăng từ khoảng 0,132 ms lên 0,171–0,190 ms ở hai tổ hợp. Sai khác nhỏ về FPS có thể do nhiễu/pacing, không coi trường hợp nhiều vũ khí nhanh hơn là cải thiện hiệu năng.

Probe hữu hạn gồm ba stage liên tiếp, mỗi stage **8 quái, 30 frame warmup + 60 frame lấy mẫu**, map legacy cố định để so sánh; hai vũ khí ranged cấp 3. HP quái/player chẩn đoán giữ population, range kiếm chẩn đoán 1; đây không phải cân bằng gameplay. Không chạy đồng thời bộ kiểm thử GL khi lấy mẫu cuối.

Đây là FPS callback CPU trên emulator phần mềm, không có GPU timing và không suy ra hiệu năng máy thật. Các stage diễn ra nối tiếp, vị trí/animation quái khác nhau và mẫu ngắn có nhiễu; không diễn giải chênh lệch như benchmark tuyệt đối hay cam kết 60 FPS. Cap tài nguyên được kiểm thử logic, không benchmark kéo dài ở 48 đạn/48 effect.

CSV/environment/summary nằm trong `android-weapons.csv`, `android-weapons-environment.txt`, `android-weapons-summary.json`. Flag `combat-profile.json` được dọn/khôi phục sau đo; không có profiler hoặc tuning chẩn đoán trong trận thường.

## 12. Hạn chế còn tồn tại

- Đây là baseline combat, chưa playtest dài hạn về sức mạnh tích lũy/ba vũ khí cấp 5 hoặc kinh tế Weapon Box. Damage/Max HP vẫn không có cap cân bằng mới.
- Bow/Fireball nhắm vị trí khi bắn, không dẫn đường hay tự đuổi target; quái có thể né hoặc rời đường bay. Collision projectile quét với footprint enemy ở bước mô phỏng hiện tại, không thêm physics engine hay swept trajectory riêng cho enemy.
- Warrior không có animation giương cung/niệm phép mới; ranged chạy độc lập, giữ animation kiếm gốc. Icon là viết tắt, VFX hình học tối thiểu; hiệu ứng nổ là pulse hình học, damage dùng bán kính tròn có LOS.
- Popup libGDX là UI chức năng để Antigravity thay sau. Chưa localization, âm thanh vũ khí, save/load loadout hoặc bridge kết quả/Achievements native.
- Khi nhiều effect vượt cap, ưu tiên giữ giới hạn bộ nhớ và thay một effect cũ; không tăng cap theo FPS. Không có dữ liệu FPS điện thoại thật hay soak test dài.
- Không triển khai Evolution, Dash/Ultimate, Lich Lord, Night Mode, Victory hoặc sửa Settings/native UI.

## 13. Hợp đồng cho Antigravity UI02

UI đọc state, không trực tiếp grant weapon hoặc tiêu thụ pickup:

| Dữ liệu/API | Hợp đồng |
|---|---|
| `GameSession.phase()` / `choosingWeapon()` | CHOOSING_WEAPON là state mô phỏng dừng; Pause/lifecycle có thể phủ thêm lên modal |
| `selection.pendingId()`, `selection.token()` | Identity của yêu cầu đang mở; token opaque, duy nhất giữa các session trong process |
| `selection.size()`, `get(i)` | 0–3 immutable offers; title/description/icon/color đã sẵn; hiển thị số lựa chọn thực tế |
| `WeaponOffer.kind`, `weapon`, `fromLevel`, `toLevel` | Chỉ dùng level khi kind=WEAPON; fallback stat không có level vũ khí |
| `GameScreen.chooseWeapon(index, token)` | Entry UI ưu tiên: kiểm tra Pause/current session, commit, reset input và bỏ stale delta; true nghĩa là thành công |
| `GameSession.chooseWeapon(index, token)` | Transaction core; không gọi khi UI đang Pause; có guard phase/token/eligibility/pending item |
| `weapons.loadout.slot(0..2)` | Null là slot trống; đọc type/level/tuning và cooldown. Sword cooldown chuẩn nằm ở `player.cooldown()` |
| `GameSession.kills` | Tổng kill cả bốn vũ khí, không cộng với `SwordCombat.kills` |
| `home()` / `retry()` / `end()` | Dọn pending selection, vô hiệu token/callback cũ và thu hồi transient combat |

Mọi thao tác gameplay/UI commit phải được chuyển về libGDX thread (ví dụ `Gdx.app.postRunnable` nếu UI02 dùng native view). Capture current session/token; bỏ callback khi view/session đã đóng. Không giữ offer hoặc callback của trận cũ. Không gọi `PickupSystem.update`, `collect` hoặc grant trực tiếp từ native UI.

Nếu thay `WeaponHud`, giữ input modal ưu tiên cao nhất, pointer press/release/cancel, chống multi-touch/double tap, reset joystick lúc mở/đóng và explicit Resume sau lifecycle. Back phải cho tiếp tục lựa chọn hoặc về Home; không tự tiêu thụ box khi mở/ẩn view. Giữ tối đa ba loại, cấp 5 thường, eligible offers và stat fallback do core quyết định.

Task này không thêm Intent extras, Activity/XML/theme hoặc bridge native mới. Antigravity có thể thay phần trình bày và bindings sau mà không cần viết lại combat/loadout.

## 14. Đề xuất Task 03C

Chưa triển khai Task 03C. Trước Evolution nên chốt bảng evolve cho từng loại, điều kiện ngoài cấp 5, scaling theo base damage và tương tác ba slot. Dùng definition/offer mới có version để phân biệt nâng thường với evolve; không tăng `MAX_LEVEL` tùy tiện hoặc nhân bản weapon type để lách slot.

Tiếp theo cần test các transition level 5 → evolve, offer khi mọi slot đã đầy, fallback hết thưởng, projectile/cooldown đang hoạt động khi đổi definition, Retry/loadout reset và UI02 token/lifecycle. Cần playtest định lượng và profile ngắn trên thiết bị thật trước khi chốt balance; không tự thêm boss/night/victory hoặc thay native UI trong bước đó.
