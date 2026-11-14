# TASK 02 — Enemy AI & Core Combat

Ngày thực hiện: **13/11/2026**. Java 17, libGDX 1.14.2; làm việc offline trong `D:\Android\MonsterSurvivor`.

## 1. Chức năng đã triển khai

- Ba loại quái gameplay: Tiny Slime, Tiny Bat, Wild Boar; tự spawn ngoài camera, truy đuổi, separation và collision địa hình.
- Steering trực tiếp khi đường đi thông thoáng; A* trên navigation grid khi gặp vật cản; giới hạn số lượt tìm đường trong mỗi frame.
- Kiếm tự động tìm quái gần nhất, quay đúng hướng, windup và một sự kiện hit cho mỗi đòn; Hurt, HP và Death của quái.
- Player HP 10, damage 1, movement giữ 80 world units/s tương ứng Speed UI 10. Miễn nhiễm 0,7s, Hurt và phản hồi nhấp nháy đỏ sau trúng đòn.
- `RUNNING → DYING → GAME_OVER`, thống kê thời gian sống/kills, Retry sạch trạng thái và đường Home qua launcher.
- EASY/HARD và NORMAL/DEMO từ Intent có thật; hai đồng hồ riêng; Pause đóng băng gameplay và cooldown, lifecycle giữ yêu cầu explicit Resume.
- HUD cũ được bổ sung HP thực và kills; FPS/preview ẩn trong trận mặc định. Cảnh exhibits cũ chỉ còn qua đường debug/measurement chủ động.

**Trạng thái xác minh:** logic và Desktop OpenGL đã chạy; Android đã compile thành công. **Chưa xác nhận runtime hoặc FPS Task 02 trên emulator**; checklist Run ở mục 11.

Không triển khai boss, 10 loại quái gameplay, lịch spawn 10 phút, các vũ khí khác, Dash/Ultimate hoặc Victory. Các sprite khác vẫn nguyên trong catalog.

## 2. File tạo/sửa và phạm vi bảo vệ

Đường dẫn core dưới `core/src/main/java/com/example/monstersurvivor/`:

| File | Thay đổi |
|---|---|
| `config/Difficulty.java`, `config/GameMode.java`, `config/GameConfig.java` | Mới: enum, parsing Intent, cấu hình bất biến và builder |
| `entity/EnemyType.java`, `entity/Enemy.java` | Mới: loại, chỉ số, HP, state/animation clock, path và vòng đời quái |
| `ai/NavigationGrid.java` | Mới: node theo footprint và connected components mặt đất/bay |
| `ai/AStarPathfinder.java` | Mới: A* bốn hướng với heap và workspace tái sử dụng |
| `ai/EnemySystem.java` | Mới: quản lý quái, ngân sách A*, steering, charge, separation, thu hồi corpse |
| `spawn/EnemySpawner.java` | Mới: vị trí ngoài camera, kiểm tra liên thông, số lần thử hữu hạn |
| `combat/SwordCombat.java` | Mới: tìm mục tiêu, sector hit, đếm kills, contact damage |
| `gameplay/GameSession.java` | Mới: owner mô phỏng, hai đồng hồ và phase trận |
| `entity/Player.java` | HP, miễn nhiễm, Attack/Hurt/Death; giữ quy đổi movement và collision cũ |
| `world/CollisionGrid.java` | Thêm occupancy bay và kiểm tra hành lang swept footprint |
| `screen/GameScreen.java` | Nối session, render quái/state thật, flash damage, Retry/Home; giữ renderer/camera |
| `input/GameInput.java` | Khóa preview trong trận mặc định, xử lý kết quả và Home |
| `ui/GameHud.java` | HP/kills thực, nhãn DEMO, Game Over và Home tạm trong libGDX |
| `MonsterSurvivorGame.java` | Nhận GameConfig và callback Home |

Ngoài core:

| File | Thay đổi |
|---|---|
| `android/src/main/java/com/example/monstersurvivor/android/AndroidLauncher.java` | Đọc hai extras đã tồn tại; callback Home trên UI thread |
| `tools/checks/CombatChecks.java` | Mới: kiểm thử logic hành vi độc lập |
| `tools/smoke/CombatSmokeLauncher.java` | Mới: runtime OpenGL, input/lifecycle và stress 10/30/60 quái |
| `tools/run_combat_checks.py` | Mới: compile/chạy helper bằng dependency cache; không tạo APK/AAB |
| `tools/smoke/SmokeLauncher.java` | Chọn explicit debug scene; cho phép đổi thư mục kết quả bằng system property |
| `tools/smoke/TerrainProbe.java` | Cho phép xuất kết quả regression vào thư mục Task 02 |
| `INTEGRATION_NOTES.md`, `TASK_02_REPORT.md` | Mới: ghi nhận hợp đồng thực tế và bàn giao |

Đối chiếu SHA-256 **128 file được bảo vệ: không thay đổi**, gồm Android UI/resources/Manifest, Gradle, assets runtime, GDD và ba báo cáo cũ. Không sửa `PixelCamera`, atlas/padding, `ForestMap`, sprite nguồn hoặc catalog. Không stage/commit/push; trạng thái nhiều file untracked có từ trước task.

Các tài liệu `UI_DESIGN_SPEC.md`, `HUD_SPEC.md`, `UI01_REPORT.md` và `INTEGRATION_NOTES.md` cũ chưa có khi kiểm tra. Kết luận UI dựa trên source native thực tế, không giả định UI01 hoàn tất.

## 3. Cấu trúc AI và A*

`GameSession` sở hữu Player, EnemySystem, SwordCombat và EnemySpawner. Mỗi Enemy sở hữu một path, state và animation clock. GameScreen giữ nguyên SpriteBatch/AssetManager, ground rendering, y-sort, culling và camera SCREEN từ Task 01B.2.

- Navigation dùng CollisionGrid 100×100 hiện có, tâm tile, footprint 10×6 world units; lớp mặt đất chặn mọi mask, lớp bay chỉ bỏ WATER, vẫn chặn cây/đá/boundary.
- Connected components tính lúc tạo session cho hai lớp. Không flood-fill trong render loop hoặc tìm đường từ vị trí spawn mỗi frame.
- A* custom bốn hướng, heuristic Manhattan, không cắt chéo góc. Heap có cập nhật priority; mảng cost/parent/stamp/heap dùng chung cho mọi lượt tìm. Mỗi search đóng tối đa số node của grid; thất bại trả path rỗng.
- Mặc định **tối đa 2 search cho toàn bộ một lần update/render**, kể cả khi delta được chia substep. Duyệt quái luân phiên để phân phối ngân sách.
- Kiểm tra đường trực tiếp cách khoảng 0,22–0,30s, có kiểm tra ngay lần update đầu. Repath khoảng 0,8–1,12s hoặc khi mục tiêu đổi ít nhất hai ô; không tìm thấy đường thì backoff khoảng 1,4s.
- Khi cần, đi về tâm tile trước góc hẹp; không kéo actor lùi về tâm tile ở mọi lần refresh. Khi không dịch chuyển hơn 1s dù còn xa player, bỏ path và ưu tiên navigation một khoảng ngắn trước khi thử direct lại.
- Mọi di chuyển, kể cả separation/charge, giải collision từng trục với substep tối đa 3 world units. Delta mô phỏng tối đa 0,1s, chia update ≤1/30s. Không đẩy quái xuyên vật cản.
- Separation nhẹ theo cặp trong bán kính 24; giới hạn tốc độ tổng. O(n²) ở n≤60, không thêm physics engine hoặc allocation vector mỗi frame.

Đường không tồn tại được xử lý bằng chờ/retry có giới hạn; không cố xuyên tường. Bản đồ và mask là tĩnh trong task này; thay terrain động cần xây lại navigation/components.

## 4. Spawn và difficulty

| Loại | HP / damage | Tốc độ EASY | Hành vi |
|---|---|---:|---|
| Tiny Slime | 2 / 1 | 28 units/s | Chậm, đuổi trực tiếp; A* khi bị cản |
| Tiny Bat | 2 / 1 | 52 units/s | Đi qua WATER, vẫn tránh cây/đá; separation |
| Wild Boar | 4 / 1 | 38 units/s | Charge ×1,65 trong 0,7s khi đường thoáng, cách 45–150 units; cooldown 3,5s |

Chỉ số HP/damage/speed từng loại chỉnh bằng `GameConfig.Builder.enemy(...)`. HARD nhân tốc độ quái ×1,08 và tăng áp lực spawn; không đổi movement player.

- Mật độ đích ban đầu 7. EASY spawn cách 1,5s, tăng đích +1 mỗi 24 giây tiến trình; HARD cách 0,85s, +1 mỗi 12 giây tiến trình. DEMO tăng **tiến trình** nhanh hơn, không tăng tốc vật lý.
- Có thể chỉnh initial/max bằng `population(initial, limit)`. Tối đa tuyệt đối 60 resident, tính cả corpse đang phát Death để không vượt bộ nhớ/chi phí entity. Quái chết chưa chiếm lượt AI/combat.
- Vị trí ngẫu nhiên trong vòng quanh camera, lấy tâm ô; ngoài mép camera ít nhất 80 units, xa player ít nhất 96 units, tránh overlap spawn trong bán kính 24.
- Chỉ nhận node đúng lớp di chuyển và cùng connected component với player. Ground không spawn trên nước/cây/đá hoặc vùng không tiếp cận được. Bat được spawn trên phần nước thuộc lớp bay hợp lệ.
- Mỗi lượt tối đa 64 ứng viên ngẫu nhiên + 256 ô fallback; nếu camera phủ hết map hoặc không có chỗ thì bỏ lượt, thử lại sau cooldown. Không vòng lặp vô hạn.
- Luân phiên Slime/Bat/Boar; không bật các loại khác/boss. Đây là tăng mật độ nền để kiểm thử, chưa phải lịch sự kiện 10 phút.

## 5. Combat, HP và kết quả

Kiếm chọn quái sống gần nhất trong **32 units**, không xuyên cây/đá. Hướng chém khóa theo bốn hướng sprite. Sau **0,45s windup**, áp dụng đúng một sự kiện hit vào các quái trong sector phía trước (dot ≥0,25), range và đường kiếm không bị vật cản. WATER không chặn đường kiếm, để vẫn đánh được Bat gần bờ. Mỗi quái trong sector nhận damage một lần/swing; không damage lặp mỗi frame.

Damage mặc định 1, cooldown 3,1s; có cấu hình. Clip Warrior Attack stay nguồn dài 2,7–3s được phát hết; cooldown cấu hình ngắn hơn không cưỡng ép reset clip. Player có thể di chuyển trong Attack, giữ hướng chém. Hurt có thể ngắt Attack và hủy hit chưa diễn ra.

Contact nằm trong bán kính tổng 10 units, kiểm tra vật cản. Quái sống, không Hurt/Attack/Death mới bắt đầu contact attack. Player nhận tối đa một hit trong khoảng miễn nhiễm dùng chung 0,7s, kể cả nhiều quái chạm cùng lúc; HP không âm. Quái trúng kiếm chuyển Hurt; HP 0 chuyển Death một lần và ghi kill một lần. Corpse không còn AI/contact/target, chỉ phát Death đến hết rồi loại khỏi danh sách.

Player HP 0 dừng gameplay ngay; chỉ Death player tiếp tục. Sau clip, hiện Game Over libGDX tạm. Hiển thị thời gian sống không pause và kills; Retry giữ cấu hình nhưng reset trận, không nạp lại texture. Không tự Victory ở 90s/10 phút.

## 6. Mapping animation và vòng đời

Giữ nguyên tất cả metadata FPS, frame, scale và pivot trong catalog. Clip lấy từ SpriteSet đã load; không tạo Texture/Animation trong update/render loop.

| Actor | Idle / Walk | Attack | Hurt | Death |
|---|---|---|---|---|
| Warrior Tiny Questers | Mapping hiện có, 4 hướng | `attack_*_stay`, 18/20 frame @6,67fps; 2,7/3s | `hit` dùng chung, 2 frame @6,67fps; 0,3s | `die` dùng chung, 10 frame @6,67fps; 1,5s |
| Tiny Slime | Mapping hiện có, 4 hướng | 7–9 frame tùy hướng @6,67fps | 3/4 frame @6,67fps; left mirror right | `die_a` dùng chung, 17 frame @8,33fps; 2,04s |
| Tiny Bat | Mapping hiện có, 4 hướng | 12 frame @10fps; pivot theo canvas từng hướng | `hit_down` dùng chung, 6 frame @10fps | `die_down` dùng chung, 8 frame @10fps; 0,8s |
| Wild Boar | 4 frame @6fps / 6 frame @10fps | 5 frame @12fps | 3 frame @10fps | 5 frame @9fps; ~0,556s |

Idle/Walk lặp. Attack/Hurt/Death one-shot, clock chỉ reset khi đổi state; Hurt đang chạy không bị reset bởi damage lặp. Hướng action giữ ổn định; render dùng foot pivot từng FrameRegion, không dịch simulation theo canvas Attack. Không coi các clip Hurt/Death dùng chung là art bốn hướng độc lập.

Ảnh regression Tiny Questers chỉ lưu cục bộ bằng công cụ game; không mở pixel làm input cho mô hình AI. Không tạo/chỉnh sửa art hoặc đổi nhịp clip nguồn.

## 7. Tình trạng kết nối UI01

Đã nối **Intent có thật** từ GameModeActivity. AndroidLauncher là file Android duy nhất được sửa: parse config và bridge Home. Native ResultPreviewActivity mới là preview; chưa có API nhận kết quả trận thật, nên dùng fallback libGDX đúng phạm vi task.

Chi tiết và đề xuất DTO/callback một lần ở `INTEGRATION_NOTES.md`. Chưa ghi kết quả vào Achievements/kỷ lục; chưa kết nối Settings với Pause gameplay. Không thay native layout/theme/menu/navigation.

## 8. Kiểm thử thực tế

Lệnh compile đã chạy, exit **0**, dùng cache offline và không gọi packaging:

```powershell
.\gradlew.bat :core:compileJava :lwjgl3:classes :android:compileDebugJavaWithJavac --offline
python tools/run_combat_checks.py
```

| Kiểm tra | Kết quả thực chạy |
|---|---|
| Java/core/Desktop/Android Java compilation | Thành công; có warning SDK XML v3/v4 hiện hữu, không đổi toolchain/Gradle |
| Foundation | 280 assertion: movement theo FPS, collision, map connectivity, boundary |
| Camera | 44.781 assertion: lượng tử camera SCREEN, biên, chẵn/lẻ, nhiều FPS |
| Combat logic | 39.037 assertion: A* qua khe/vùng không có đường, Bat qua nước, AI thực đi vòng tường; spawn ngoài camera/liên thông, full-map viewport, cap 10/30/60, collision trong crowd, separation, sword/windup/sector/one hit, HP/immunity/Death, pause, đồng hồ Demo, không Victory giả, movement quái 30/60/120 FPS |
| Gameplay OpenGL | 695 kiểm tra hành vi; tổng 1.604 gồm kiểm tra stress từng frame. Chạy auto sword/Hurt/Death, pause bằng touch, lifecycle, Home callback, Retry, keyboard/camera, spawn thật trong GameScreen, clip/pivot và GL error |
| OpenGL regression foundation | 2.136 check: render frame Tiny/pivot, input, 6 state/4 hướng debug, collision thực, biên, resize, 60 exhibits; không dùng exhibits làm bằng chứng AI |
| Terrain bleeding | 20 cấu hình chiếu terrain, **0 pixel sai** trên Desktop |
| File bảo vệ | SHA-256: 128 file, 0 thay đổi |
| Android runtime / luồng Home → Game → Home | **Chưa kiểm tra bản Task 02 bằng Android Studio Run**. Callback đã kiểm tra trên Desktop và Java Android compile; chưa đủ để đánh dấu Android runtime PASS |

Các lỗi phát hiện trong quá trình kiểm tra đã sửa: độ trễ kiểm tra đường ở update đầu làm sai lệch nhẹ giữa FPS; re-center không cần thiết khi refresh path; khả năng Resume ở phase Death sau lifecycle pause. Đã chạy lại suite sau các thay đổi gameplay cuối.

Bằng chứng nằm trong `core/build/reports/task02/`: `compile.log`, `compile-result.json`, `FoundationChecks.log`, `CameraChecks.log`, `CombatChecks.log`, `CombatSmokeLauncher.log`, `desktop-combat.txt`, `desktop-performance.json`, `SmokeLauncher.log`, `TerrainProbe.log`, `protected-check.json`. Regression ảnh/log riêng ở `regression/`; không ghi đè báo cáo Markdown cũ.

## 9. FPS và giới hạn đo

Desktop Windows, **GeForce GTX 1650**, LWJGL3 960×540, VSync và giới hạn 60 FPS. Mỗi mật độ warm-up 60 frame rồi đo 240 frame callback (~4s). Quái thật dùng cả ba loại, AI, contact, separation và một số đường bị chặn cần A*. Test tăng HP player và giảm range kiếm để giữ đúng số lượng, không phải cân bằng trận mặc định.

| Quái resident | FPS callback | p95 / p99 callback | Render CPU/driver trung bình | A* trong 300 frame toàn cảnh |
|---:|---:|---:|---:|---:|
| 10 | 59,999 | 18,358 / 19,334ms | 0,269ms | 14 |
| 30 | 59,986 | 18,902 / 20,904ms | 0,283ms | 39 |
| 60 | 59,974 | 18,496 / 20,897ms | 0,289ms | 85 |

Đây là mẫu ngắn Desktop có cap/VSync, không phải FPS Android, không phải thời gian GPU hoặc khả năng throughput không giới hạn. Không kết luận đã đạt 60 FPS emulator hoặc nhanh hơn Task 01B.2. Số liệu Android 33–34 FPS trong báo cáo cũ không đo gameplay mới.

Workspace/heap A* và vector separation tái sử dụng. Chỉ cấp entity/path/sprite khi spawn/session; path có thể tăng capacity khi gặp đường dài. Không pool entity vì tối đa 60 và churn thấp, chưa có profiling chứng minh cần pool. Giới hạn chi phí theo count/search budget; chưa benchmark dài hạn hoặc worst-case mọi quái ở các góc map trên emulator.

## 10. Hạn chế còn tồn tại

- Chưa chạy bản mới trong Pixel_4 bằng Android Studio Run, chưa đo FPS/frame pacing Android hoặc thiết bị vật lý. Chưa xác nhận cảm giác chơi/pivot bằng mắt trên emulator, context loss hoặc process death.
- Cân bằng kiếm theo clip nguồn khá dài; Hurt có thể ngắt đòn. Cần playtest trước khi đổi nhịp art hoặc stats; không tự tăng tốc clip để đạt tốc độ đánh khác.
- Separation là steering mềm; vẫn có thể overlap sprite trong đám đông/chỗ hẹp. A* bốn hướng ưu tiên an toàn hơn đường chéo mượt; không bảo đảm mọi tình huống crowd đều hết ùn tắc.
- Navigation tĩnh; spawn fallback có thể xa player khi camera ở gần biên. Nếu không có chỗ ngoài màn hình thì chủ động bỏ lượt.
- Native results/statistics/Achievements chưa được nối; fallback libGDX đang dùng. Các tính năng Task 03+ nằm ngoài phạm vi.

## 11. Chạy bằng Android Studio Run trên emulator

1. Mở project `D:\Android\MonsterSurvivor` bằng Android Studio; dùng Java 17/toolchain hiện có. Không đổi Gradle, Manifest hoặc minSdk.
2. Mở Device Manager, chạy AVD **Pixel_4**. Chọn cấu hình chạy ứng dụng module **android**, entry mặc định HomeActivity; nhấn **Run**. Không dùng Generate Signed Bundle/APK hoặc release/export.
3. Home → chọn chế độ/độ khó → Play. Kiểm tra bốn tổ hợp EASY/HARD × NORMAL/DEMO. Trận chính không có ANIM/DIR; DEMO có nhãn và đồng hồ tiến trình nhanh, movement/animation giữ tốc độ.
4. Đứng/chạy quanh map để thấy 5–10 quái đầu; đi qua cây/đá/khu hồ. Xác nhận ground vòng vật cản, Bat qua nước, kiếm tự đánh và corpse phát hết Death.
5. Để nhiều quái chạm: kiểm tra HP giảm, nhấp nháy và không mất nhiều HP trong 0,7s; HP 0 → Death → Game Over có thời gian/kills.
6. Pause rồi chờ: timer/AI/cooldown/spawn đứng yên. Resume không nhảy vị trí. Thử nút Home hệ điều hành và quay lại, kể cả lúc Death; cần Resume và tiếp tục được.
7. Retry phải sạch HP/kills/time/quái và giữ mode/difficulty. Home từ Pause/Game Over phải trở về native Home đúng stack; lặp lại Home → Game → Home.
8. Xem Logcat (`MonsterSurvivor`, `AndroidRuntime`) và FPS/frame time ở mật độ tăng dần nếu chỉnh cấu hình debug. Ghi cấu hình/độ dài lượt đo trước khi kết luận hiệu năng.

Phiên này không tạo/bàn giao APK/AAB, signing key, bản release hoặc quy trình publishing. Các script Android cũ có bước install APK không được dùng trong Task 02.

## 12. Đề xuất Task 03

Ưu tiên playtest và profiling bản Task 02 trên emulator; thống nhất bridge kết quả native và lưu thống kê với Antigravity. Sau đó mới giao riêng Task 03 cho tiến trình/nâng cấp hoặc vũ khí tiếp theo. Không tự triển khai Task 03 và chưa thay đổi GDD.
