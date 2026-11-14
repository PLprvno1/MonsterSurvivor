# TASK 02.1 — Combat Balancing, Performance & Procedural Map

Ngày: **13/11/2026**. Project `D:\Android\MonsterSurvivor`, Java 17/libGDX 1.14.2. Thực hiện lần lượt A → B → C → D; kiểm tra sau từng nhóm. Không sửa GDD; các thông số mới dưới đây thay cho cân bằng Task 02.

## 1. Nguyên nhân trải nghiệm chưa tốt

- Kiếm cũ range 32, windup 0,45s và cooldown 3,1s; Attack nguồn 2,7–3s được giữ nguyên tốc độ. Thời gian đến hit dài và ít cơ hội chém khi chạy trốn.
- Một biến hướng vừa phục vụ movement/facing vừa phục vụ Attack; đòn cũ không theo dõi hoặc đổi mục tiêu trong windup. Khi player/target đã dịch chuyển, hướng và phạm vi cũ dễ tạo cú chém hụt.
- HP Slime/Bat/Boar 2/2/4 trong khi damage player 1 khiến farm đầu trận chậm, nhất là khi nhận Hurt ngắt đòn.
- `GameSession` chia delta >33ms thành nhiều lần update **toàn bộ** AI/separation/LOS/combat. Ở emulator dưới 30 FPS, điều này làm tăng tải ngay khi frame đã chậm. Movement vốn đã có bước collision ≤3 units.
- LOS cũ lấy mẫu occupancy mỗi 2 units; props render duyệt toàn bộ 2.394 props mỗi frame. Các chi phí này có thể giảm mà không đổi renderer/camera.
- Map cố định có 7.203/10.000 ô đi được (~72%), quá nhiều vật cản so với yêu cầu mới.
- Renderer emulator thực tế báo **Google SwiftShader**. Số đo không cho thấy A* là nguồn lag chính; sau tối ưu, rendering và pacing/backend vẫn đáng kể.

## 2. File và logic thay đổi

Core dưới `core/src/main/java/com/example/monstersurvivor/`:

| File | Nội dung |
|---|---|
| `entity/Player.java` | Tách movementDirection/attackDirection; aim trong windup không reset clock; playback Attack theo thời lượng cấu hình |
| `combat/SwordCombat.java` | Detection 360°, target tracking, retarget/cancel, hit một lần đúng range và không xuyên vật cản |
| `config/GameConfig.java` | Sword tuning; HP theo difficulty/stage; spawn/cap theo tiến trình; fixed seed |
| `ai/EnemySystem.java` | Stats theo giai đoạn khi spawn; profiler tùy chọn, giữ ngân sách A* |
| `spawn/EnemySpawner.java` | Lấy cadence và HP từ stage hiện tại |
| `gameplay/GameSession.java` | Một lần AI/update mỗi frame; giữ sweep collision, pause/timer và phase; instrumentation tùy chọn |
| `world/CollisionGrid.java` | Occupancy đọc mask trực tiếp; thay LOS lấy mẫu bằng DDA + segment/expanded AABB |
| `world/ForestGenerator.java` **mới** | Map 100×100 theo seed, clearing/roads, cụm vật cản, BFS/repair/fallback |
| `world/ForestMap.java` | Chuyển layout sinh mới sang ground/props; index props theo chunk, giữ fixture JSON cho debug có chủ đích |
| `screen/GameScreen.java` | Map mới khi bắt đầu/Retry; fixed seed debug; cull trước chọn frame quái; giữ SCREEN camera; log seed/kết quả |
| `debug/FrameMetrics.java` **mới** | Các bộ đếm/timing tái sử dụng, không hoạt động trong trận thường |
| `debug/CombatProfile.java` **mới** | Một lượt đo hữu hạn khi có flag local, không readback art, không có nút/UI mới |

Công cụ/tài liệu:

- Mới: `tools/checks/Task021Checks.java`, `tools/smoke/Task021Smoke.java`, `tools/run_task02_1_checks.py`, `tools/profile_task021.py`, `tools/android_task021_smoke.py`, `tools/run_studio_task021.ps1`, báo cáo này.
- `tools/run_combat_checks.py` là entry tương thích chuyển sang suite Task 02.1; helper Java Task 02 có thông số cũ được giữ làm lịch sử, không dùng để đánh giá cân bằng mới.
- Bổ sung phần cập nhật Task 02.1 vào `INTEGRATION_NOTES.md`. Không sửa GDD hoặc báo cáo Task 01B/02.
- Không sửa **bất kỳ file Android native/launcher/XML/theme/menu/Manifest**, Gradle, sprite, catalog hoặc camera SCREEN. Không thêm dependency.

Audit cuối đối chiếu SHA-256 **130 file được bảo vệ, 0 thay đổi**. Camera/renderer sprite/asset loader giữ nguyên byte. Không còn flag seed/profile trên emulator và không có APK/AAB trong thư mục bàn giao; chi tiết `core/build/reports/task02_1/final-audit.json`.

## 3. Hướng đánh và target tracking

Joystick cập nhật `movementDirection` độc lập. Khi bắt đầu Attack, SwordCombat chọn quái sống gần nhất trong detection 60 units, có đường kiếm hợp lệ; bắt đầu chém khi vào range 48. Hướng Attack lấy từ vector tới target, quy về bốn hướng sprite. Vận tốc vẫn do joystick quyết định, không có thời gian xoay hoặc dừng để chém.

Trong windup, giữ target đang hợp lệ để tránh nhấp nháy khi hai quái đổi thứ tự khoảng cách. Target chết/bị che/rời **range hit** thì tìm quái hợp lệ khác ngay; đổi hướng không reset animation clock. Không có target thay thế thì hủy đòn, không gây damage, chờ tối đa 0,15s trước cơ hội tấn công lại. Chỉ giữ hướng sau sự kiện hit đến hết animation, tránh xoay canvas liên tục trong recovery.

Hit kiểm tra lại vị trí hiện tại, range, sector phía trước và vật cản; một swing chỉ phát một hit event. Corpse không được chọn. Cây/đá chặn kiếm; WATER không chặn kiếm, giữ khả năng đánh Bat gần bờ. Hướng joystick không tham gia chọn target.

## 4. Tốc độ và tầm kiếm trước/sau

| Thông số | Task 02 | Task 02.1 |
|---|---:|---:|
| Damage | 1 | 1 |
| Range hit | 32 | **48** |
| Detection | 32 | **60** |
| Windup | 0,45s | **0,15s** |
| Chu kỳ bắt đầu đòn | 3,1s | **1,0s** |
| Attack playback | 2,7–3s nguồn | **0,65s** |
| Movement | 80 units/s | 80 units/s, độc lập hướng chém |

Không sửa pixel, frame list, FPS nguồn hoặc pivot của asset. Render ánh xạ tiến độ 0,65s sang toàn bộ clip Attack stay tương ứng hướng. Chỉ Attack đổi playback; Hurt/Death/Walk, movement và cooldown miễn nhiễm không bị nhân tốc độ. Clip Death player vẫn 1,5s. Camera và foot pivot dùng cơ chế cũ.

Hurt vẫn có thể ngắt windup; đây là chủ ý giữ phản hồi trúng đòn, không phải khóa di chuyển. Đòn hụt khi target thực sự ra ngoài range là hợp lệ, không kéo dài hitbox để tạo damage giả.

## 5. HP quái và spawn balancing

HP player 10, damage 1, UI Speed 10 tương ứng 80 units/s. Damage contact 1 và miễn nhiễm 0,7s giữ nguyên.

| Difficulty | HP Slime/Bat/Boar đầu trận | Mật độ đầu | Trần resident |
|---|---|---:|---:|
| EASY | **1 / 1 / 2** | **6** | **40** |
| HARD | **1 / 2 / 3** | **7** | **60** |

EASY: Slime/Bat một hit, Boar hai hit. HARD tương đương áp lực trung bình; tốc độ quái giữ 28/52/38 units/s và hệ số HARD ×1,08 từ Task 02. Không cân bằng theo giả định có Damage/Weapon Box.

Stage theo **giây tiến trình** (DEMO nén tiến trình, không nén vật lý):

| Bắt đầu | HP cộng Slime/Bat/Boar | Target EASY tại mốc / nhịp tăng / spawn | Target HARD tại mốc / nhịp tăng / spawn |
|---:|---|---|---|
| 0s | 0/0/0 | 6; +1 mỗi 30s; 1,8s/lượt | 7; +1 mỗi 20s; 1,5s/lượt |
| 120s | 0/0/0 | 10; +1 mỗi 18s; 1,5s/lượt | 13; +1 mỗi 12s; 1,2s/lượt |
| 210s | 0/0/1 | 15; +1 mỗi 18s; 1,35s/lượt | 21; +1 mỗi 11s; 1,1s/lượt |
| 300s | 1/1/1 | 20; +1 mỗi 15s; 1,15s/lượt | 29; +1 mỗi 10s; 0,95s/lượt |
| 450s+ | 1/1/2 | 30; +1 mỗi 15s; 1s/lượt, cap 40 | 44; +1 mỗi 9s; 0,85s/lượt, cap 60 |

HP stage chỉ áp dụng cho **quái mới spawn**, không hồi/tăng HP quái đang đánh dở. Cuối các stage hiện tại EASY tối đa 2/2/4 HP, HARD 2/3/5 HP; không tiếp tục nhân HP vô hạn. `GameConfig.Stage` và `Builder.stages(...)` là điểm mở rộng cho task nâng cấp sau này.

Trần gồm corpse đang phát Death; không xóa quái đang đánh để đạt FPS. Không điều chỉnh count theo FPS tức thời. Spawn giữ kiểm tra ngoài camera, giới hạn map, connected component, khoảng cách player và số lần thử hữu hạn. Không triển khai lịch boss/10 phút hoặc Victory.

## 6. AI và performance

- **Một update AI mỗi render frame** với delta giới hạn 0,1s. Player/enemy vẫn chia displacement thành các bước ≤3 units và giải collision từng trục. Không tăng tốc mô phỏng, không bỏ cooldown/AI khi pause.
- DDA duyệt các ô dọc segment; kiểm tra lân cận footprint với slab intersection trên AABB của vật cản đã nới theo half-size. Không còn occupancy mỗi 2 units dọc toàn đường. Có kiểm tra tangent/grazing, hướng đảo, nước/bay và biên.
- Occupancy có fast path khi footprint nằm trong một ô; các trường hợp nhiều ô đọc byte mask trực tiếp, tránh các lớp method/floor lặp không cần thiết.
- A* vẫn cache path, refresh theo interval/đổi ô, budget **2 search/frame**; không viết lại A* khi số đo cho thấy chi phí nhỏ.
- Separation giảm số lần chạy nhờ một update/frame. Ở 60 quái, counter từ trung bình ~3.127 xuống đúng **1.770 cặp/frame**; chi phí separation sau đo ~0,08ms nên chưa thêm spatial grid cho quái.
- Props tĩnh có chunk index 160 units. Chỉ duyệt chunk giao vùng camera, sau đó dùng chính `WorldSprite.visible`; kiểm tra bằng cách so tập visible với full scan tại tâm/biên/toàn map. Giữ y-sort chung với actor và renderer cũ.
- Cull quái ngoài camera + margin 80 trước chọn TextureRegion; vẫn cập nhật clock/vòng đời để Death và gameplay đúng khi ngoài màn hình.
- Tái sử dụng các mảng/heap/queue/metrics; không load lại texture, tạo Animation hoặc cấp collection mỗi frame. Generator/index chỉ chạy khi bắt đầu trận hoặc Retry.

Profiler chỉ tạo khi có `files/combat-profile.json`; không có flag thì không gọi nanoTime cho profiling. Không đổi renderer, chất lượng đồ họa, SCREEN camera, extrusion/Nearest hoặc cấu hình emulator.

## 7. FPS/frame time thực đo trên Android

**Pixel_4 emulator, API 37, landscape 2280×1080; renderer: Android Emulator OpenGL ES Translator (Google SwiftShader).** App được triển khai bằng Run/Rerun của Android Studio; không dùng Desktop thay bằng chứng Android.

So sánh trước/sau **giai đoạn C**, sau khi A/B đã xong, cùng fixture `forest.json` cũ để giữ địa hình giống nhau. Diagnostic giữ đúng 8/20/40/60 quái bằng HP player cao và range kiếm 1; không phải trận mặc định. Mỗi mức warm-up 30 frame rồi lấy 60 frame callback, khoảng 2 giây đo/mức. Dùng hai lượt hợp lệ, loại lượt deploy còn dùng mã cũ; không benchmark dài.

| Quái | FPS callback trước → sau | Mean frame trước → sau | p95 callback trước → sau |
|---:|---:|---:|---:|
| 8 | 30,91 → **34,06** | 32,35 → 29,36ms | 43,91 → 44,80ms |
| 20 | 31,61 → **32,38** | 31,63 → 30,89ms | 41,67 → 48,35ms |
| 40 | 29,34 → **30,72** | 34,08 → 32,55ms | 53,17 → 47,88ms |
| 60 | 25,46 → **30,65** | 39,28 → 32,62ms | 58,31 → 43,07ms |

Không khẳng định p95 cải thiện ở mọi mức: 8/20 quái có p95 xấu hơn trong mẫu ngắn. Đây là **render callback timing**, không phải timestamp trình bày SurfaceFlinger hay GPU timer. Instrumentation có overhead, đặc biệt khi gọi nhiều occupancy; không coi chênh lệch của một lượt ngắn là cam kết FPS dài hạn.

Chi phí sau tối ưu, trung bình **ms/frame**:

| Quái | AI riêng | A* | Separation | Collision/LOS | Animation | World render | HUD |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 8 | 0,435 | 0,026 | 0,106 | 0,530 | 0,247 | 14,745 | 2,119 |
| 20 | 0,858 | 0,138 | 0,207 | 0,670 | 0,449 | 14,232 | 2,111 |
| 40 | 1,364 | 0,292 | 0,083 | 1,076 | 0,813 | 15,108 | 1,467 |
| 60 | 1,674 | 0,374 | 0,080 | 1,330 | 1,115 | 17,192 | 1,163 |

AI riêng đã trừ các mục con được đo. Player, combat và spawn có cột riêng trong CSV. Animation gồm clock quái và chọn frame hiển thị; player update có phần timing riêng. Các số không đo phần wait/swap/presentation nằm ngoài GameScreen.

Trước tối ưu, 60 quái: AI riêng ~7,447ms, collision ~7,261ms; tổng ~14,708ms. Sau: tổng ~3,004ms. Cách phân loại LOS đổi từ các occupancy con sang toàn raycast, nên dùng **tổng AI + collision** khi so trước/sau; không diễn giải hai cột riêng như hai phép đo hoàn toàn đồng nhất. Render world không cho thấy cải thiện timing trong lượt này; không gán mọi tăng FPS cho chunk index.

Kết quả phù hợp với giảm tải CPU gameplay; rendering/pacing vẫn lớn và renderer là SwiftShader. Đây là bằng chứng emulator/software rendering còn là giới hạn, **chưa tách riêng GPU và backend wait để kết luận nguyên nhân duy nhất**. Chưa đạt 60 FPS emulator, không hạ chất lượng hoặc đổi cấu hình GPU để che vấn đề.

Bản procedural cuối đã smoke/playtest thật trên Android, nhưng **chưa đo lại ma trận 8/20/40/60 trên map mới** để tránh thêm một đợt benchmark không cần thiết. Không lấy kết quả fixture cũ làm FPS chuẩn của mọi seed.

## 8. Thuật toán sinh map

`ForestGenerator` không phụ thuộc GL/IO, đầu ra gồm CollisionGrid, terrain region names và props. Mỗi trận dùng seed long ngẫu nhiên; Retry thường sinh seed mới, asset manager/batch giữ nguyên. Pause/Resume không gọi generator.

1. Tạo map 100×100, tile 16, spawn `(808,808)`, biên mask BOUNDARY.
2. Bảo vệ đồng cỏ ellipse trung tâm bán kính 17×14 tile. Bốn nhánh đường đất rộng 5 tile, hơi uốn theo seed, luôn nối tâm ra các phía.
3. Đặt hữu hạn các đảo/cụm hồ, cây và đá theo ellipse có mép nhiễu nhỏ. Giữa các cụm có buffer 3 ô; không đặt vào clearing/đường. Giới hạn tổng vật cản nội thất 950 tile, biên 396 tile.
4. BFS từ spawn. Nếu có vùng không nối, tối đa 12 lượt carve đường nối rộng 3 tile rồi kiểm tra lại. Nếu vẫn không hợp lệ hoặc walkable <85%, dùng fallback đồng cỏ/đường/biên rừng hợp lệ. Mỗi cụm tối đa 40 thử, không vòng lặp vô hạn.
5. Chọn shoreline từ bộ Kenney đã có. Cây/đá vẫn chặn; nước chặn ground nhưng lớp Bat bỏ WATER. Bụi nhỏ trang trí scale 0,5 không chặn, phân biệt với cụm cây lớn.
6. Dựng props/chunk index và navigation của **map mới**. Session và path cũ không được dùng sau Retry.

Fixture JSON cũ giữ nguyên chỉ cho debug exhibits/camera hoặc profiling so sánh có chủ đích. Gameplay thường không dùng fixture này. Nếu generator lỗi runtime, ForestMap ghi seed và dùng fallback hợp lệ; không crash do cố tạo lại vô hạn.

## 9. Tỷ lệ vật cản và liên thông

Giới hạn lý thuyết tối đa 950 interior +396 boundary =1.346 ô chặn, tức ít nhất **86,54%** đi được trước các bước sửa; bước kiểm tra bắt buộc vẫn yêu cầu ≥85%. BFS đảm bảo mọi ô đi được của map nối tới spawn, không chỉ clearing. Main roads 5 tile và khoảng cách cụm 3 tile giúp giảm các hành lang hẹp.

Các seed đã kiểm tra đều không cần repair hoặc fallback. Ground spawn dùng đúng navigation của trận, tất cả spawn/AI được kiểm tra occupancy và budget; Bat giữ quy tắc vượt nước. Không đổi mask theo hình ảnh rồi bỏ qua collision.

## 10. Ba seed mẫu đã xác minh

| Seed | Đi được | Tree tile | Rock tile | Water tile | Props tổng | Repair / fallback |
|---:|---:|---:|---:|---:|---:|---|
| 42 | **8.883 — 88,83%** | 467 | 59 | 195 | 1.107 | 0 / false |
| 20261113 | **8.898 — 88,98%** | 540 | 38 | 128 | 1.167 | 0 / false |
| 8675309 | **8.923 — 89,23%** | 556 | 27 | 98 | 1.167 | 0 / false |

Mỗi seed: so sánh tái tạo collision/terrain, flood-fill/navigation, A* tới bốn góc trong biên, spawn và AI thực chạy trên grid mới. Tất cả ≥85%, spawn hợp lệ, các bố cục khác nhau. Seed 42 còn chạy thật trên Android, có đúng 8.883 ô đi được.

Ảnh overview `core/build/reports/task02_1/map-42.png` chỉ chứa terrain/props **Kenney CC0** và đã được xem để kiểm tra bố cục. Không đưa pixel Warrior/Slime/Bat Tiny Questers vào mô hình AI, không sửa hoặc tạo lại art nguồn.

Fixed seed: `new GameConfig.Builder().fixedSeed(42).build()`, hoặc ghi `42` vào file private `files/map-seed.txt` của app debug. Khi cố định seed, Retry tạo session/grid mới nhưng tái hiện bố cục; xóa flag để Retry quay về seed ngẫu nhiên. Seed xuất trong Logcat, không cần thêm UI native.

## 11. Kết quả kiểm thử

Compile Java/core/Desktop/Android offline thành công sau từng giai đoạn; warning SDK XML v3/v4 có sẵn, không sửa toolchain. Lệnh kiểm tra mã nguồn cuối:

```powershell
.\gradlew.bat :core:compileJava :lwjgl3:classes :android:compileDebugJavaWithJavac --offline
python tools/run_task02_1_checks.py final
```

| Nhóm | Kết quả thực chạy |
|---|---|
| A combat | 17 check: chạy và chém sau lưng, tracking ổn định, đổi mục tiêu chết, range 48/detection 60, hủy đòn không damage giả, FPS 30/60/120 |
| B balance | 1.841 check: HP/hits-to-kill hai difficulty, count tăng đơn điệu/cap, stage, quái cũ không nhận HP mới |
| C collision/update | 36.990 assertion: DDA ngang/dọc/chéo/grazing, đảo hướng, bay/nước, các điểm dọc sweep, long frame chỉ update AI một lần, pause và combat regression |
| D map | 93.941 assertion trên ba seed: đồng cỏ/biên, liên thông mọi ô đi được, tái tạo, A* tới bốn phía, spawn/AI/collision, fallback |
| Foundation/camera | 280 +44.781 assertion; camera SCREEN và movement/collision cũ không hỏng |
| Desktop OpenGL | 2.922 check: map/asset thật, chunk culling bằng full scan, kiting giết Slime một đòn, pause/lifecycle/Death, Retry seed mới/fixed, sạch session/path, giữ textures, collision/camera biên, GL error 0 |
| Texture bleeding | 20 cấu hình terrain Desktop: **0 pixel sai**; atlas/padding và PixelCamera không thay đổi |
| Android runtime cuối | **31 check thực** qua ADB trên app triển khai từ Android Studio Run; bốn tổ hợp EASY/HARD × NORMAL/DEMO, native Home/Game/Home, joystick thật, pause timer/position, lifecycle cùng map, Game Over/Retry và fixed seed |

Trong trận HARD/NORMAL quan sát bình thường, không inject damage: Game Over tại **24,344593s gameplay, 6 kills**, sau Death; Retry đổi seed `7037265598404373260 → 6010721381944519757`, map mới có 9.004 ô đi được, HP 10/kills 0. Đây là kiểm tra đứng yên để xác minh vòng đời, không phải đánh giá cân bằng khi người chơi né/chạy.

Không phát hiện FATAL EXCEPTION, GdxRuntimeException hoặc thiếu asset trong log process Android đã thu. Các lỗi ở helper đọc UTF-8/tên field dumpsys API 37 và cleanup khi `adb exec-out` trả mã 0 cho file không tồn tại đã sửa; không phải lỗi gameplay. Flag seed còn sót đã xóa và audit xác nhận không còn flag debug hoạt động. Android Studio Apply Changes không nhận thêm field trong một lượt thử; dùng Run/Rerun đầy đủ thành công, lượt deploy chưa đúng không được dùng làm số đo sau tối ưu.

Bằng chứng: `core/build/reports/task02_1/` gồm `compile-a/b/c/d/final.log`, `phase-a/b/c/d.log`, `Task021Checks-a/b.log`, `FoundationChecks.log`, `CameraChecks.log`, `Task021Smoke.log`, `desktop-smoke.txt`, `TerrainProbe.log`, `android-before/after.csv`, `android-before/after-summary.json`, environment/logcat, `android-final-smoke.json`, `android-final-logcat.txt`, `studio-run.log`, và audit file bảo vệ.

Không chạy benchmark Desktop để tuyên bố FPS Android. Các kiểm tra Java helper không phải số test của Gradle `:core:test`.

## 12. Giới hạn và lỗi còn tồn tại

- Emulator SwiftShader vẫn khoảng 30–34 FPS trong lượt profiling; frame pacing chưa ổn định, không đạt 60 FPS. Chưa có GPU timing, SurfaceFlinger presented-frame matrix, physical-device hoặc long-run profiling cho bản này.
- Ma trận hiệu năng dùng cùng fixture cũ cho so sánh C; các seed procedural mới chỉ kiểm tra runtime/bố cục, chưa có benchmark tổng quát theo seed.
- Một lượt ngắn không chứng minh cân bằng 0–10 phút. Đứng yên ở HARD vẫn có thể thua nhanh; cần người dùng playtest kiting/đọc animation và cân chỉnh tiếp nếu cần.
- Clusters/đường là generator đơn giản, chưa phải art direction cuối. Có thể overlap sprite trong crowd hoặc ùn tại một số góc; separation mềm, không physics cứng. Map tĩnh trong từng trận, không hỗ trợ phá vật cản.
- Bộ Kenney shoreline hữu hạn; mép hồ theo tile còn thô. Preview toàn map chỉ dùng để kiểm tra bố cục, không thay duyệt cảm giác chơi trên emulator.
- Native kết quả/statistics/Achievements chưa có bridge mới. Không thêm hộp, vũ khí khác, Dash/Ultimate, boss, ngày/đêm, Victory hoặc achievement mới.

## 13. Chạy bằng Android Studio Run

1. Mở project, chọn cấu hình **android**, thiết bị **Pixel 4 / Pixel_4** đang chạy, nhấn **Run/Rerun đầy đủ**. Giữ Java 17, Manifest, minSdk và Gradle hiện có. Khi thay field/class, không dùng Apply Changes thay cho Run.
2. Từ Home chọn EASY/HARD và NORMAL/DEMO rồi Play. Trận thường có seed mới và không có ANIM/DIR. Kiểm tra joystick chạy một hướng, quái phía sau trong 48 units và Warrior tự chém theo hướng quái.
3. EASY đầu trận: Slime/Bat chết một hit, Boar hai hit. Kiểm tra range thật, hủy đòn khi target thoát và không chém xuyên cây/đá.
4. Pause, Home hệ điều hành, quay lại và Resume: time/AI/cooldown không tự chạy trong pause, map giữ nguyên. HP 0 → full Death → Game Over → Retry phải đổi seed và sạch trạng thái; Home về native đúng stack.
5. Đọc Logcat `MonsterSurvivor` để lấy seed. Khi tái hiện lỗi, bật fixed seed qua builder/file local; xóa file để về random. Map-seed/profile flag của phiên kiểm thử đã được dọn/khôi phục; app cuối đang ở Home.
6. Chỉ chạy profiling helper khi cần một phép đo mới có mục tiêu; helper không tự build/install. Các script dùng tọa độ màn hình/UI toolbar của môi trường đã đo, cần điều chỉnh nếu đổi độ phân giải hoặc bố cục IDE.

Không gọi assemble/bundle/release/signing/export như nhiệm vụ riêng và không tạo package để bàn giao. **Android Studio Run tự dùng package debug nội bộ để triển khai**, là phần bình thường của Run; không xuất/copy APK/AAB sang artefact bàn giao. Không commit/push.

## 14. Gợi ý Task 03

Ưu tiên người dùng duyệt cảm giác kiting và cân bằng mới trên Pixel_4. Sau đó thống nhất bridge kết quả/lưu thống kê với Antigravity và giao riêng Task 03 cho Damage/Health/Weapon Box hoặc tiến trình vũ khí. Chỉ xem xét tối ưu GPU/backend/renderer tiếp theo khi có profiling tách GPU/presentation rõ ràng. Không tự triển khai Task 03 và không sửa GDD.
