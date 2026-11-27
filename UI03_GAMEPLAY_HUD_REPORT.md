# UI03 — Gameplay HUD, Active Skills & Evolution Polish

Ngày: **15/11/2026**. Project: `D:\Android\MonsterSurvivor`.

## Kết quả và phạm vi

Đã đồng bộ HUD theo khung fantasy tối, góc pixel, accent cyan cho Dash/Skill Box và vàng cho Ultimate/evolution. Gameplay landscape 2280×1080 dùng ExtendViewport tối thiểu 800×480; trên Pixel_4 là khoảng 1013,33×480 HUD units. UI đọc state hiện có; không cấp thưởng hoặc thay đổi combat từ render.

Đã đối chiếu các báo cáo Task 05, 05.1B, 06, 06.1, 07, UI02 và Integration Notes. Theo xác nhận trước của chủ dự án, chỉ một người sửa project; không chạy agent khác đồng thời. Giữ nguyên các thay đổi có sẵn của task trước trong working tree.

### File runtime tạo/sửa

| File | Thay đổi UI03 |
|---|---|
| `core/.../ui/HudStyle.java` — mới | Khung góc pixel, icon primitive, ring dùng bảng tọa độ cache, helper layout/fit và phục hồi blending cho modal |
| `core/.../ui/GameHud.java` | HP riêng hàng, stats/kills có giới hạn chiều rộng; safe insets; Pause và kết quả; vùng dành riêng cho chỉ báo pickup |
| `core/.../ui/SkillHud.java` | Icon kỹ năng, level tab, trạng thái và cooldown ring |
| `core/.../ui/SkillRewardHud.java` | Skill Box có icon, mô tả, trước/sau và trạng thái max không tương tác |
| `core/.../ui/WeaponHud.java` | Ba slot có icon/tên đầy đủ; badge tiến hóa; phân biệt loại thưởng trên card |
| `core/.../ui/BossHud.java` | Dải Boss tương phản cao, tên/HP/phase, chỉ báo hướng tránh nút kỹ năng |
| `core/.../pickup/PickupRenderer.java` | Chest kỹ năng cyan, đai tím và sao năng lượng |
| `core/.../localization/GameStrings.java` | Các nhãn và mô tả VI/EN ngắn gọn, thống nhất |

`core/...` tương ứng `core/src/main/java/com/example/monstersurvivor/`. Audit so với snapshot đầu UI03: chỉ 7 file runtime cũ thay đổi, thêm 1 helper UI. Các file source/asset cũ còn lại giữ nguyên; xem [production-scope.json](core/build/reports/ui03/production-scope.json).

Tool mới: `tools/run_ui03_checks.py`, `tools/smoke/HudBeforeSmoke.java`, `HudPolishSmoke.java`, `HudEvolutionSmoke.java`, `tools/run_studio_ui03.ps1`, `tools/build_ui03_touch.py`, `tools/android_ui03_baseline.py`, `tools/android_ui03_profile.py`, `tools/android_ui03_smoke.py`. Evidence riêng trong `core/build/reports/ui03/`. Không sửa báo cáo lịch sử; nối thêm Integration Notes.

## UI02 → UI03

| Thành phần | Trước | UI03 |
|---|---|---|
| HP và chỉ số | HP và kills cùng hàng, HP lớn có thể chồng chữ | HP/bar toàn hàng; stats và kills ở hàng dưới; layout cache có fit |
| Skill | Tên + level cùng trong vòng nhỏ | Icon trung tâm, cooldown rõ, tab Lv, tên dưới nút |
| Skill Box | Card tối giản | Khung cyan; icon; lợi ích ngắn; số trước/sau; max status riêng |
| Weapon slot | Tên viết tắt, tiến hóa chỉ TH/EV | Tên vũ khí thật, tên evolved đầy đủ, icon, badge và viền vàng |
| Weapon reward | Dùng chung kiểu card | Nhãn MỞ KHÓA / NÂNG CẤP / TIẾN HÓA / CHỈ SỐ; tiến hóa có viền vàng |
| Modal highlight | Alpha của lượt shapes sau batch có thể bị bỏ qua | Bật lại blending trước overlay; nền tối vẫn giữ tương phản chữ |
| Boss | Text và bar cũ, chỉ báo có thể đi vào vùng skill | Dải riêng dưới stats, phase rõ, marker tránh vùng điều khiển |
| Kết quả | Khung và nút chưa đồng bộ | Khung/nút chung; Victory có họa tiết vàng; Game Over viền đỏ; số liệu trận thật |

## Asset và chi phí render

Không thêm bitmap/atlas/font/shader/FBO. Icon kiếm, cung, lửa, sét, Dash và sóng năng lượng vẽ bằng primitive trong ShapeRenderer hiện có. Skill Box dùng chest Kenney Tiny Dungeon đã cấp phép trong project (`assets/game/licenses/Kenney-Tiny-Dungeon.txt`), tint và chi tiết sao/đai màu bằng white texel hiện có. Không dùng ảnh sinh bởi AI hoặc nhập thư viện asset mới.

Giữ **20 textures / 44.548.100 bytes RGBA** theo GameAssets, font HUD giữ nguyên. Font/ShapeRenderer chỉ tạo theo vòng đời GameHud và dispose cùng HUD; Retry tiếp tục tái sử dụng tài nguyên screen. Layout chỉ cập nhật theo HP, level, revision, token, giây cooldown hoặc state thay đổi; result chỉ tạo một lần mỗi session. Không có Texture/font mới mỗi frame, particle mới hay cấp phát danh sách trong vòng vẽ. Telemetry FPS/DIR/ANIM chỉ cập nhật/hiện khi debug opt-in.

Probe GL HUD riêng ghi tối đa **4 draw calls**, kể cả modal. Đây là phép đo HUD trên desktop GL, không phải draw-call budget của toàn world.

## Nút kỹ năng và Skill Box

Dash giữ radius30, Ultimate40; Ultimate lớn hơn. Cyan chevron cho Dash, diamond/sóng vàng cho Ultimate. READY có accent tĩnh; cooldown icon giảm sáng, số giây làm tròn lên và ring recharge; ACTIVE đổi glow trong thời gian sử dụng thật. Lv1–5 nằm trong tab riêng, tên bên dưới. Không thêm flash liên tục.

Giữ nguyên `SkillHud.centerX/centerY/radius/at`, nên tọa độ vẽ và vùng chạm cùng nguồn. Các nút bị vô hiệu khi Pause, modal hoặc không ở RUNNING. Không sửa GameInput, pointer ownership, cooldown, invulnerability, damage hoặc snapshot activation.

Skill Box ghi **HỘP KỸ NĂNG / SKILL BOX**. Mỗi thẻ chứa tên và cấp trước→sau, lợi ích ngắn, cooldown/distance hoặc radius, iframe hoặc damage/knockback theo `SkillOffer`. Khi Dash max, chỉ còn offer Ultimate; Dash hiện dòng CẤP TỐI ĐA không có hitbox chọn thưởng. Khi cả hai max, logic cũ ngừng hộp/nâng cấp. UI không tự tạo offer hay upgrade; token âm, transaction, debounce và commit khi thả tay giữ nguyên.

## Weapon và evolution

Ba slot vẫn thụ động, không nhận touch; mỗi slot 138×54, pitch146, có icon, tên, Lv và gauge cooldown. Vũ khí evolved dùng `WeaponInstance.evolved()` để chọn viền vàng, badge TH/EV và tên đầy đủ:

| VI | EN |
|---|---|
| Vòng Kiếm | Blade Orbit |
| Mưa Tên | Arrow Storm |
| Thiên Thạch Lửa | Inferno Meteor |
| Lôi Võng | Thunder Web |

Tên được wrap trong 94 HUD units. Không hiển thị Lv6. Badge dùng chữ tối trên nền vàng để giữ tương phản. Gauge tiếp tục đọc cooldown hiện tại, không sửa chu kỳ tấn công.

Card evolution có nhãn TIẾN HÓA/EVOLUTION, tên gốc Lv5 → tên evolved và mô tả cơ chế từ GameStrings; icon theo weapon type. Unlock/upgrade/fallback có nhãn riêng. Số card, thứ tự, điều kiện và nội dung reward vẫn do selection cung cấp. Card vẫn 620×76, `cardY=h/2+54−88*i`; Home modal giữ vùng cũ. Press chỉ highlight, release cùng thẻ mới commit; ngoài thẻ không tự chọn.

## Main HUD, Boss và màn hình kết quả

HP toàn hàng 234 units, không đè kills khi diagnostic HP100000. Damage/Speed và kills có chiều rộng riêng, fit khi cần. Timer và độ khó gọn ở giữa; nút Pause96×46 có icon và phản hồi bằng modal ngay sau touch. Safe left/top/right/bottom áp dụng cho stats, Pause, skill, slot và hint/toast liên quan.

Joystick giữ nguyên floating activation, radius và owner. Vành thực sự rỗng, nền bán trong suốt, knob rõ; không đổi movement/deadzone. Chỉ báo rương được dời khỏi dải Boss, ba slot, toast và các vòng kỹ năng. Chỉ báo hướng Boss nằm ở hành lang giữa, tránh nút skill kể cả safe-right inset.

Boss có nền gần đen, border hồng/vàng, tên + phase, HP text và gauge tách hàng; Mini thứ hai có nền riêng. Banner xuất hiện/phase ngắn hạn giữ thời gian logic cũ. Boss strip nằm dưới stats và không đè Pause; pickup toast nằm trên slot dưới màn hình.

Pause/Game Over/Victory dùng cùng ngôn ngữ khung/nút. Victory có năm điểm sáng vàng tĩnh; failure viền đỏ. Scrim giữ nền game nhìn được. Result đọc elapsed/kills thực tại trạng thái kết thúc; thông tin Boss và mode lấy từ session/result hiện có. Callback Resume/Retry/Home không đổi; không thêm thao tác lưu thành tích giả.

## Localization và audio

Toàn bộ nhãn mới có VI/EN trong GameStrings. Kiểm glyph thật từ BMFont đang ship, không chấp nhận fallback thay dấu; kiểm cả width/height của layout đã cache. Ảnh GL cả hai ngôn ngữ ở phần evidence phía dưới. Không đổi `game_language`, schema preferences hoặc native Settings.

Không sửa `monster-survivor-audio-selections.json`, manifest, 49 mappings, 36 audio files hoặc manager. Không phát âm thanh từ render; action tiếp tục đi qua callback cũ, tránh phát hai lần. Ba optional skill cues vẫn silent như Task06.1/07. AudioChecks qua, không coi việc gọi playback API là bằng chứng đã nghe cân âm.

## Kiểm thử

Biên dịch bằng `:core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain --info`: **BUILD SUCCESSFUL**. Chỉ dùng Android Studio Run để deploy trên Pixel_4; không gọi lệnh export/install APK độc lập. Log còn cảnh báo SDK XML version và deprecation Gradle có sẵn; không sửa Gradle cho UI03.

| Bộ kiểm tra | Kết quả |
|---|---:|
| HudPolishSmoke — GL/font/insets/modal/highlight/multitouch/Boss/results VI/EN | 3.425 PASS |
| HudEvolutionSmoke — bốn evolution VI/EN, full slot names, layout bounds, touch/Retry | 4.129 PASS |
| DiagnosticIsolationChecks | 20 PASS |
| Task04A1Checks — input/localization | 2.790 PASS |
| UpgradeChecks | 1.690 PASS |
| SkillChecks | 1.463 PASS |
| EvolutionChecks | 10.527 PASS |
| WeaponChecks | 235 PASS |
| PickupChecks | 29.227 PASS |
| BossChecks | 2.901 PASS |
| AudioChecks | 190 PASS |
| Android UI/input — thực sự gửi MotionEvent/tap | 68 PASS |
| Android focused Boss/Victory/Retry/Pause trên build cuối | 8 PASS |
| Android normal baseline | 38 PASS |

GL tests kiểm center draw/hit-test ở nhiều landscape widths và right inset, mô phỏng cả bốn safe inset, HP100000 không tràn; pointer0 giữ joystick trong khi pointer1 kích hoạt Dash hoặc Ultimate. Có kiểm màu framebuffer để bảo đảm highlight không biến nền card thành màu sáng che chữ. Max HUD4 draw calls, GL không lỗi.

Android68 gồm actual Skill Box/touch cho tám nâng cấp, Dash max bị loại khỏi offer, outside tap, hai-pointer Dash/Ultimate, real Weapon Box nâng Sword/Bow tới Lv5 rồi chọn evolution, double tap/token guard, Pause trong modal, Mini/night, Lich phase1/2, Victory và Game Over thật, Retry xóa level/evolution, Pause chặn skill. Sau tinh chỉnh vị trí marker Boss cuối cùng, chạy focused Boss/Victory/Pause/Retry trên build cuối và normal baseline lại; số kiểm tra/log cuối được ghi ở phần xác nhận bên dưới.

Diagnostic chỉ qua DiagnosticLauncher debug-only, lease một lần trong UUID riêng, protected HP/progress/placement để tái hiện state. Những ảnh đó không phải bằng chứng trận tự nhiên hoặc trận bình thường. Normal baseline khởi động qua Home → chọn Easy/Normal → Start, HP10/10, Damage1, Speed10, Sword1, Dash1 ready, Ultimate1 initial15s, progress/elapsed0, seed ngẫu nhiên khác nhau và debugfalse. Preferences cùng tám legacy paths được so byte trước/sau; không ghi fixture root hoặc khôi phục đè dữ liệu ngoài namespace sở hữu.

Log: [compile](core/build/reports/ui03/compile.log), [GL HUD](core/build/reports/ui03/HudPolishSmoke.log), [GL evolution](core/build/reports/ui03/HudEvolutionSmoke.log), [Android68](core/build/reports/ui03/ui03-android-checks.json), [normal38](core/build/reports/ui03/phase0-checks.json), [restoration](core/build/reports/ui03/android-restoration.json).

Focused final build: [8 checks](core/build/reports/ui03/ui03-boss-final-checks.json), [log](core/build/reports/ui03/ui03-boss-final.log), [state byte-equal](core/build/reports/ui03/android-boss-final-restoration.json), [Boss phase2](core/build/reports/ui03/android-final-boss-phase2.png), [Victory](core/build/reports/ui03/android-final-victory.png). Lượt68 diễn ra trước tinh chỉnh hành lang marker Boss cuối cùng; focused8 và GL3.425 kiểm lại thay đổi đó trên bản cuối. Không thay đổi hình học hit-test kỹ năng/modal giữa hai lượt.

## Android Studio Run và hiệu năng

Build cuối được gọi bằng Shift+F10 trên cửa sổ MonsterSurvivor, cấu hình android/Pixel_4: **21:35:48 Asia/Saigon**, IDE deploy và mở HomeActivity **21:35:52–53**. Xem [studio-run.log](core/build/reports/ui03/studio-run.log) và [studio-deploy-final.log](core/build/reports/ui03/studio-deploy-final.log). Emulator landscape2280×1080, Android API37, renderer SwiftShader; không đo trên điện thoại thật.

So sánh trước/sau dùng cùng emulator, Hard/Normal seed42, Sword1, protectedHP, không input, khoảng giây3–28. Mỗi window120 frames; chỉ nhận RUNNING và `glError=0`. Tổng hợp cuối nằm trong [performance-summary.json](core/build/reports/ui03/performance-summary.json). Đây là mẫu ngắn, không phải benchmark kiểm soát hoàn toàn tải host; không suy ra cải thiện nhân quả hoặc cam kết60FPS. CPU HUD bao gồm chi phí submit/flush theo backend, không phải GPU timer cô lập.

| Chỉ số — 4 windows / 480 frames mỗi bản | Trước UI03 | UI03 cuối |
|---|---:|---:|
| FPS tổng hợp từ thời gian frame | 23,91 | 26,13 |
| Frame trung bình | 41,83 ms | 38,26 ms |
| Khoảng p95 của từng window | 50,20–89,12 ms | 51,46–56,63 ms |
| Frame lớn nhất | 128,94 ms | 98,95 ms |
| HUD CPU trung bình được ghi | 3,65 ms | 4,94 ms |
| GL errors | 0 | 0 |

Mẫu này không thấy giảm FPS toàn frame. CPU ghi ở phần HUD tăng khoảng **1,29 ms**; không bỏ qua chi phí đó hoặc suy ra UI03 đã tối ưu GPU. Các window ngắn chịu dao động tải host/SwiftShader, và phần submit có thể chứa chi phí flush của backend. Log thô giữ cả before/after, không chỉ chọn frame thuận lợi.

## Screenshot VI/EN

Ảnh GL là HUD trên nền đơn sắc để kiểm bố cục, không dùng thay cho bằng chứng Android. Android có ảnh game thực ở2280×1080, ngôn ngữ preference hiện tại là VI; VI/EN không đổi preference để chụp.

| State | VI | EN |
|---|---|---|
| Fresh HUD, GL config không fixture | [VI](core/build/reports/ui03/hud-normal-vi.png) | [EN](core/build/reports/ui03/hud-normal-en.png) |
| Skill Box + max Dash | [VI](core/build/reports/ui03/hud-skill-modal-ULTIMATE-vi.png) | [EN](core/build/reports/ui03/hud-skill-modal-ULTIMATE-en.png) |
| Meteor evolved slot | [VI](core/build/reports/ui03/hud-evolved-FIREBALL-vi.png) | [EN](core/build/reports/ui03/hud-evolved-FIREBALL-en.png) |
| Evolution offer | [VI](core/build/reports/ui03/hud-offer-FIREBALL-vi.png) | [EN](core/build/reports/ui03/hud-offer-FIREBALL-en.png) |
| Boss phase2 | [VI](core/build/reports/ui03/hud-lich-phase2-vi.png) | [EN](core/build/reports/ui03/hud-lich-phase2-en.png) |
| Victory | [VI](core/build/reports/ui03/hud-victory-vi.png) | [EN](core/build/reports/ui03/hud-victory-en.png) |
| Game Over | [VI](core/build/reports/ui03/hud-game-over-vi.png) | [EN](core/build/reports/ui03/hud-game-over-en.png) |

Trước UI03: [slot Meteor cũ](core/build/reports/ui03/before/hud-evolved-FIREBALL-en.png). Android: [trận chuẩn](core/build/reports/ui03/phase0-fresh-first-seconds.png), [Skill Box](core/build/reports/ui03/android-ui03-skill-modal-ULTIMATE-2.png), [hai evolved](core/build/reports/ui03/android-ui03-two-evolved.png), [Victory](core/build/reports/ui03/android-ui03-victory.png). Tất cả ảnh evolved/Boss/terminal được ghi rõ là state có hỗ trợ test.

## Giới hạn

Chưa test điện thoại thật, tai thỏ thật hoặc accessibility font scaling hệ thống; safe insets được kiểm qua Graphics proxy và emulator. Không hot-switch ngôn ngữ giữa trận. Không sửa font/atlas của UI02, nên nét chữ nhỏ vẫn theo BMFont Nearest đang dùng. Chưa thực hiện một run mười phút không hỗ trợ để tự kiếm đủ evolution/Victory; không tuyên bố balance hoặc khả năng60FPS. Không chạy full82-roster stress vì không sửa roster/AI/combat.

Không Task08, Enemy Special Skills, backend, release/signing/publishing, export APK/AAB, commit hoặc push.

## Xác nhận bàn giao cuối

Sau lượt focused8, normal38 qua lại trên bản cuối. Fresh seed3012100746639728625, HP10/10, Damage1, Speed10, Sword1, Dash1/Ultimate1, debugfalse và diagnosticsnone. Tám root trigger vắng mặt, cả chín đường dẫn app state byte-equal, các UUID test đã cleanup; hai file shell helper do runner tạo cũng đã gỡ. App kết thúc ở HomeActivity. Xem [final-normal-state.json](core/build/reports/ui03/final-normal-state.json).

74 bản ghi window trong hai log Android UI/focused đều `glError=0` (log snapshot có bản ghi lặp, không tính thành74 window độc lập). `git diff --check` exit0. Hash source UI cuối và scope được lưu để phân biệt với build/evidence của các task trước.
