# TASK 05 — Mini Boss, Lich Lord & Victory

Ngày thực hiện: 14/11/2026, Asia/Saigon. Project: `D:\Android\MonsterSurvivor`.

## 1. Kết quả

Đã triển khai encounter thật: Mini Boss tại `GameSession.progress() >= 300`, Lich Lord tại `>= 600`, có animation, navigation, HP, damage, telegraph và HUD. Lich có hai phase. Đạt 600 khi Boss còn sống tiếp tục trận; chỉ hạ Lich thật, chạy hết Death animation mới hiện Victory.

Mốc Lich **10 phút thay thế contract 9 phút/540 cũ**. Night vẫn bắt đầu tại 300, quái thường vẫn dừng spawn tại 600. Không sửa GDD hoặc các báo cáo lịch sử. Không triển khai Endless, active skill, weapon evolution, achievements hoặc toàn bộ FX 04C.

Android Studio Run trên Pixel_4 đã triển khai và chạy bản cuối. Demo được kiểm tra với đồng hồ tự nhiên từ 0: Mini tại **45,018955 giây**, Final tại **90,02314 giây gameplay thực**. Đây là lượt kiểm tra có bảo vệ HP và cấp sẵn vũ khí, không phải lượt chơi thường không hỗ trợ. Phần giảm HP để kiểm tra phase/Victory được ghi riêng bên dưới.

## 2. File tạo/sửa

Các đường dẫn Java dưới đây tương đối với `core/src/main/java/com/example/monstersurvivor/`.

| Nhóm | File | Mục đích |
|---|---|---|
| Mới | `boss/BossDefinition.java`, `BossInstance.java` | Định nghĩa và trạng thái encounter, HP/phase/cast đọc được bởi presentation |
| Mới | `boss/BossSystem.java` | Hai event độc lập, placement/retry, AI kỹ năng, phase, summon |
| Mới | `boss/BossProjectiles.java`, `BossRenderer.java` | Đạn hostile có cap và FX từ pixel runtime sẵn có |
| Mới | `config/BossConfig.java` | Pool, scale, HP/stat, mốc và ngân sách Boss |
| Mới | `ui/BossHud.java`, `gameplay/MatchResult.java` | Component HUD UI02 và snapshot kết quả thật |
| Mới | `debug/BossDiagnostics.java` | Fixture cục bộ, chỉ tạo khi có `boss-test.json` |
| Sửa | `gameplay/GameSession.java`, `screen/GameScreen.java`, `input/GameInput.java` | Encounter lifecycle, WINNING/VICTORY, cleanup, Retry/Home, render integration |
| Sửa | `ai/EnemySystem.java`, `ai/NavigationGrid.java` | Budget Boss riêng, navigation footprint lớn, dùng chung budget A* |
| Sửa | `enemy/EnemyDefinition.java`, `entity/Enemy.java` | Body Boss ngoài registry, scale, damage guard, pose và death hook chung |
| Sửa | `combat/SwordCombat.java` | Contact không đánh trong entry/telegraph/phase transition bị khóa AI |
| Sửa | `spawn/EnemySpawner.java`, `EnemySpawnProfile.java` | Cap thường không tính Boss; contract Lich thành 600 |
| Sửa | `config/GameConfig.java`, `localization/GameStrings.java` | Boss config và 22 key vi/en |
| Sửa | `ui/GameHud.java` | Gắn BossHud; thêm Victory vào dialog UI02, giữ hit-test nút cũ |
| Sửa | `debug/CombatProfile.java` | Không dùng index Boss `-1` để truy cập mảng 82 loài |
| Tool mới | `tools/checks/BossChecks.java`, `tools/smoke/BossSmoke.java`, `tools/run_task05_checks.py` | Kiểm tra logic, OpenGL thật, regression có trọng tâm |
| Tool mới | `tools/android_boss_smoke.py`, `tools/run_studio_task05.ps1` | Điều khiển native/emulator và gọi Run của Studio đang mở |
| Tool sửa | `tools/checks/PickupChecks.java` | Giữ HP cao trong fixture tuổi tồn tại pickup vì đồng hồ Demo nay đi qua Mini Boss thật |
| Tài liệu | `TASK_05_BOSS_REPORT.md`, nối thêm `INTEGRATION_NOTES.md` | Bàn giao và contract UI |

So với snapshot đầu task gồm **10.550 file**, audit không thấy file bị xóa hoặc thay đổi asset/native UI. Các thay đổi UI02 có sẵn trong working tree được giữ. `WeaponHud.java`, Android XML/Java/preferences/Intent, registry/data sinh tự động, source sprite, GameAssets/catalog, weapon tuning, map generator, PixelCamera và Night renderer không bị task này sửa. Xem `core/build/reports/task05/scope-audit.json`.

## 3. Event và ownership

`BossSystem` thuộc một `GameSession`; không phụ thuộc lượt spawn của `EnemySpawner`. Mỗi event có `NOT_TRIGGERED → SPAWNING → ACTIVE → DEFEATED`. Kiểm tra `>=`, không so float bằng đúng mốc. Mini sống hoặc đang tìm vị trí không chặn Final. Khi placement thất bại, event giữ SPAWNING và thử lại mỗi 0,5 giây thực; không bỏ sự kiện.

Session chỉ chạy scheduler/combat khi RUNNING. Pause, weapon modal, DYING, GAME_OVER, WINNING, VICTORY và ENDED không trigger thêm encounter. Resume giữ cùng session; Retry tạo session mới. RNG Boss có seed riêng từ seed trận; lựa chọn Mini diễn ra đúng một lần, trước các lần thử placement, nên fixed seed tái hiện đúng loài.

Boss dùng runtime body có ID `boss_mini_<species>` hoặc `boss_lich_lord`, index/familyIndex `-1`; **không đăng ký vào EnemyRegistry hoặc weighted pool thường**. Actor ID vẫn lấy từ cùng bộ đếm của EnemySystem. Boss ở trong danh sách target hiện có nên Sword/Bow/Fireball/Lightning đều đi qua `Enemy.damage`, cùng một death hook.

## 4. Pool và chỉ số

Pool tám loài: `mc_moss_golem`, `mc_living_armor`, `mc_cyclops`, `mc_crab_knight`, `mc_dust_devil`, `mc_flytrap_walker`, `mc_goblin_bomber`, `rock_golem`. Có thể thay/mở rộng bằng danh sách `BossConfig.Candidate`; không sao chép AI cho từng loài. Clip Walk/Attack/Hurt/Death của toàn pool được kiểm tra từ asset runtime thật.

Mini scale **1,65 × scale thường** theo cả hai trục; ví dụ loài thường 0,85 thành 1,4025, loài 1,0 thành 1,65. Giữ origin từng frame của catalog; không kéo méo hoặc đổi foot pivot. Lich dùng scale catalog 1,0, frame 64×64, feet (32,56). Collider Boss 18×12 world units; không lấy kích thước ảnh làm hitbox.

| Boss / difficulty | HP Normal | HP Demo | Contact / phép | Slam Mini | Speed world units/s |
|---|---:|---:|---:|---:|---:|
| Mini Easy | 120 | 96 | 1 | 2 | 30 |
| Mini Hard | 200 | 160 | 2 | 2 | 32 |
| Lich Easy | 220 | 176 | 2 | — | 24 |
| Lich Hard | 280 | 224 | 3 | — | 27 |

Demo HP ×0,80 nằm trong BossConfig. Movement, animation, windup, cooldown và tốc độ đạn không nhân progressScale. Speed Boss không nhận thêm multiplier quái thường Hard; player ban đầu vẫn 80 units/s.

Boss có ngân sách chống burst: tối đa `ceil(20% maxHP)` trong mỗi bucket **0,20 giây thực**. Damage dư trong bucket đó bị chặn; không tích lũy lại. Mục đích là một Fireball/Lightning hoặc nhiều hit cùng frame không xóa toàn thanh HP. Chỉ Boss có guard này; damage/reward của quái thường và vũ khí không đổi. Thanh HP đọc HP sau guard, không trừ số damage giả.

Probe 32 trường hợp dùng chính Sword + Fireball + Lightning, level1/5, base damage6/12, cả hai difficulties/modes và hai Boss: **2,6–26,2 giây** để hạ mục tiêu đứng yên. Ví dụ Normal, level1/base6: Mini Easy10,2s, Lich Easy20,3s; Lich Hard26,2s. Đây là phép đo throughput với Boss AI tắt và một target ở cự ly32, không phải thời gian đánh Boss khi né đòn/có quái khác. `weapon-ttk.csv` lưu đầy đủ. Build mạnh level5 có thể kết thúc nhanh; vẫn cần playtest cân bằng với người thật.

## 5. Placement và navigation

Mỗi cửa sổ thử tối đa 64 vị trí ellipse gần mép camera, thêm khoảng24 units. Fallback duyệt tối đa256 ô trong vùng17×17 quanh player, có chỉ số tiếp tục cho lần retry. Không quét góc xa của map200×200. Có khoảng cách an toàn tối thiểu64 từ player, giới hạn xa dựa theo camera và khoảng cách với residents.

Placement kiểm tra collider9×6, component liên thông của navigation Boss riêng và vị trí tiếp cận player. Nếu player nằm trong khe chỉ collider nhỏ đi được, tìm node tiếp cận gần có line-of-sight. Actor không đi xuyên cây/đá/nước. Boss dùng cùng logic chase, separation, swept movement; chỉ thay navigation phù hợp footprint. Có cache LOS/path như quái thường, **tối đa hai A* tổng cộng mỗi frame**, gồm cả Boss và summon.

Marker hướng Boss ngoài camera nằm trong strip bên phải vùng joystick44%, dưới HUD trên và trên weapon slots. Marker ẩn khi tâm Boss vào camera. Lượt Demo thực tế: Mini đã có sprite trong viewport sau khoảng1,23 giây từ spawn; Lich có sprite trong viewport ngay frame ghi nhận event.

## 6. Combat, animation và FX

Mini dùng chase/contact chung và slam quanh vị trí đã chốt. Khi đủ gần/LOS, telegraph bán kính48, windup Easy1,10s/Hard0,95s; cooldown6/5s. Có thể chạy khỏi vùng trước impact. MC dùng Special khi clip có sẵn; fallback Attack cho loài không có Special. Entry có khoảng khóa ngắn0,4s; không contact trong lúc cảnh báo.

Lich tận dụng asset hiện có của Willosuna; đã đọc license local và catalog thực. Không chỉnh/repack artwork hoặc tải asset mới. Các clip dùng: Idle6@6, Walk6@10, Attack6@14, Hurt3@10, Death6@9, Spawn7@10, Entrance10@10, Slam8@10, đủ bốn hướng. Clip Special cũng được kiểm tra tồn tại nhưng không gán kỹ năng Lich riêng trong task này. Khi Attack ngắn hơn windup, giữ frame cuối đến lúc cast; Hurt được phép kết thúc rồi trở lại pose của kỹ năng.

| Hành vi Lich | Phase1, HP >50% | Phase2, HP ≤50% |
|---|---|---|
| Di chuyển | Chase, giữ khoảng cách khoảng68–104 units khi có LOS | Giữ cơ chế di chuyển; tăng áp lực bằng pattern/cadence |
| Bolt | Một đạn; windup1s; cooldown Easy3,8/Hard3,4s | Fan Easy3/Hard5; windup0,75/0,65s; cooldown2,4/2s |
| AoE | Tâm player lúc chốt; radius34; windup1,2s; cooldown7/6s | Radius44; windup1s; cooldown4,8/4s |
| Chuyển phase | — | Một lần, Entrance1s + vòng mở rộng + thông báo; dọn đạn cũ |
| Summon | Không | Một bat swarm mỗi10/8s; tối đa2/3 con sống của owner và phải còn chỗ trong cap thường |

AoE chốt vị trí, không chạy theo player trong windup. Có LOS trước cast và khi impact. Bolt chốt hướng, tốc độ85/95 units/s. Phase2 đổi fan đạn, cadence, aura và summon; không chỉ tăng damage. Summon vẫn có HP/AI/death/drop thường và chia sẻ A* budget; không được vượt40/60 residents kể cả corpse.

Projectile có owner actor ID, team ENEMY, cap24 preallocated, lifetime3s. Swept collision chọn va chạm player/tường trên đoạn di chuyển; đạn kết thúc sau một impact dù player đang có immunity. Không truy vấn damage lên đồng minh. `Player.damage` vẫn quản lý immunity0,7s. Death owner dọn đạn của owner; Game Over/Victory/end dọn tất cả.

FX chỉ dùng `weapon_pixel` đã load: aura nhỏ, vòng telegraph tiến dần, line hướng bolt, impact fade0,25s, lõi đạn nhỏ. Culling theo camera và giới hạn số đoạn vòng24. Không thêm texture, FBO, shader toàn màn hình hoặc bộ sưu tập render mới mỗi frame.

## 7. HUD UI02 và localization

Thêm BossHud vào hai pass shapes/text hiện có, không thay toàn HUD. Một bar rộng tối đa480 HUD units nằm dưới player HUD/timer/pickup notice. Hiện loại/tên, HP hiện tại/tối đa, ratio thật và phase Lich. Final được ưu tiên; Mini đang tồn tại có dòng phụ. Bar còn0HP trong Death và biến mất khi animation hoàn tất.

Announcement2,8 giây gameplay thực, fade alpha0,4s cuối, có tên Boss; không khóa input hoặc phủ màn hình. Thông báo Final được ưu tiên nếu Mini chết cùng lúc Lich xuất hiện/chuyển phase. Đồng hồ pause/modal cũng dừng announcement. Mini/Night cùng300 vẫn dùng Night renderer cũ; Boss FX vẽ sau Night để dễ đọc.

Bổ sung22 key vi/en cho loại/tên Boss, appears/warning/defeated, phase, HP và Victory/result status. Giữ font BMFont261 glyph/Nearest hiện có, không regenerate font. Kiểm tra `getGlyph` thật cho mọi ký tự của tất cả key ở cả hai ngôn ngữ đã qua. Ảnh `hud-boss-vi/en.png`, `hud-victory-vi/en.png` là UI render trên nền trơn, không chứa artwork game, được dùng kiểm tra bố cục. HP100000 chỉ là fixture và làm số HP ở panel UI02 cũ dài hơn bình thường.

## 8. Victory, Game Over và lifecycle

State mới: `RUNNING → WINNING → VICTORY`. WINNING chỉ xảy ra khi player còn sống, Final đã chết qua death hook và progress≥600. Ngay khi hạ Final, dừng clock/AI/combat/spawn, dọn projectile/effect/selection; chỉ advance các Death animation. Sau Death Lich mới chốt MatchResult và mở dialog Victory. Mini còn sống không chặn chiến thắng.

Victory dùng dialog UI02 với viền vàng, Retry/Home giữ vùng chạm cũ. Hiện thời gian sống thật, kills, trạng thái Mini/Final, difficulty/mode. Player chết trước Final vẫn đi DYING → GAME_OVER và không có Victory. Retry giữ config difficulty/mode/language, tạo seed mới theo quy tắc cũ; fixed seed diagnostic vẫn lặp seed đó. Home có guard chống callback lặp, native bridge CLEAR_TOP/SINGLE_TOP hiện có giữ một Home Activity.

Không gọi ResultPreviewActivity, không viết điểm hay achievements giả. `MatchResult` là snapshot bất biến; xem contract mục12.

## 9. Kiểm thử logic/OpenGL

Compile offline đã qua `:core:compileJava :android:compileDebugJavaWithJavac`; có cảnh báo SDK XML tooling version hiện có, không phải compile error.

| Bài kiểm tra | Kết quả cuối |
|---|---:|
| BossChecks | 2.901 assertions qua |
| BossSmoke, OpenGL thật | 5.368 assertions qua |
| WeaponChecks | 223 qua |
| PickupChecks | 21.438 qua |
| Task04A1Checks | 2.622 qua |

Bao phủ: vượt mốc bằng frame, 32 seed/pool, blocker/biên/camera clamp, pending event phục hồi sau khi player đến vùng hợp lệ, procedural seeds1/42/12345/987654, collider lớn,60+2 caps, hai A*/frame, damage của cả bốn vũ khí × hai Boss × modes/difficulties, burst guard, bolt swept/wall/immunity/cap/lifetime/owner, Mini slam, Lich phase1/2, AoE né được, summon limit, death một lần,600/720 không tự Victory, Death trước Victory, pause/modal/lifecycle, Game Over, Retry/Home, vi/en và glyph/texture/GL error.

Đồng hồ logic chạy đủ600 progress giây cho Normal và Demo; Normal không chờ mười phút wall-clock trong bài JVM. GL kiểm tra cả Retry fixed seed và Retry Hard/Demo/English với seed ngẫu nhiên mới. Không chạy lại stress suite đầy đủ82 loài hoặc pipeline xử lý artwork.

Lệnh tái hiện: `python tools/run_task05_checks.py BossChecks BossSmoke WeaponChecks PickupChecks Task04A1Checks` sau compile core. Log nằm trong `core/build/reports/task05/`.

## 10. Android Studio Run

Thiết bị: Pixel_4 emulator-5554, API37,2280×1080, SwiftShader. Build/deploy do **Android Studio Run** thực hiện. Lần deploy bản cuối xác nhận trong idea.log: **15:27:20 ngày14/11/2026**; bằng chứng `studio-deploy-proof.txt`. Không chạy assemble/bundle/release/signing/install như tác vụ shell riêng, không xuất hay bàn giao APK/AAB. Artifact nội bộ do Run tạo vẫn ở build directory của Studio.

- **Easy/Demo/vi, clock không nhảy:** Mini300,12637/elapsed45,018955; Final600,1543/elapsed90,02314. Mini hiển thị, bị vũ khí hạ tại elapsed66,67144. Lich hiển thị; trận còn RUNNING sau90 giây. Pause và lifecycle resume không duplicate Mini. Có ảnh announcement/Boss visible trong thư mục evidence.
- Lượt đó giữ HP100000, cấp Sword/Fireball/Lightning3, dùng seed42. Ba lệnh damage giảm HP Final để đi qua phase2; phần HP còn lại được vũ khí hạ tại elapsed121,40378. Victory ghi kills88, Mini/Final đều defeated. Không coi đây là chiến thắng tự nhiên không hỗ trợ. `android-demo-evidence.json` và `android-boss-logcat.txt` lưu thời điểm và các lệnh.
- **Hard/Normal/en:** fixture progress290/599,99 cho Mini/Final; có hai Boss đồng thời, phase2 và projectiles. Có log/capture tại `android-hard-logcat.txt`. Đây là kiểm tra actor/AI/render thật với clock fixture, không phải lượt chờ đủ5/10 phút.
- **Bản cuối, terminal follow-up:** `python tools/android_boss_smoke.py --terminal-only` qua **7 checks**: native bridge; player chết trước Final → Game Over; Retry; Death → Victory; Retry từ Victory; một Home Activity; không Android/render/asset exception. Dùng fixture600 và diagnostic damage. Xem `android-terminal-results.json`, `android-terminal-logcat.txt`, `android-final-victory.png`.
- Harness đã được sửa để lệnh chẩn đoán chết chờ hết immunity và không replay lệnh cũ sau Retry; regex dumpsys chịu được khoảng trắng API37. Các lỗi harness ban đầu không được tính thành test pass; lượt terminal cuối đã chạy lại thành công trên bản Studio mới nhất.
- Sau kiểm tra đã khôi phục nguyên preferences/các file cờ, xóa cờ chẩn đoán do test tạo. Đã mở lại Easy/Normal thường: debug=false, seed ngẫu nhiên, roster82/textures19, không BossDiagnostics. Native Home được để mở. Xem `android-default-restored.log`.

## 11. FPS và giới hạn phép đo

Probe120 frame/cửa sổ, lấy thời gian giữa frame từ monotonic clock; không lấy FPS suy ra từ tốc độ loop JVM. Bỏ cửa sổ có elapsed gameplay không tương ứng thời gian frame vì pause/background/terminal. Không chạy lại full82 stress.

| Cảnh Hard/Normal cô lập | Cửa sổ ổn định | FPS min–max | Mean frame ms | P95 lớn nhất ms | Quái thường ở cuối cửa sổ |
|---|---:|---:|---:|---:|---:|
| Trước Mini | 1 | 25,89 | 38,62 | 59,43 | 10 |
| Mini + Night | 3 | 23,41–24,86 | 41,74 | 63,05 | 13–14 |
| Final phase1 | 4 | 23,30–24,54 | 42,23 | 71,40 | 0–12 |
| Final phase2 | 3 | 24,72–25,07 | 40,09 | 58,60 | 0–1 |

Cảnh phase2 có hai Boss tồn tại và tối đa4 đạn còn sống ở các điểm lấy mẫu. World-render CPU trung bình khoảng16,5–17,8ms; HUD3,1–3,5ms; AI0,08–0,59ms. Renderer/world/HUD chiếm phần lớn số đo CPU, không thấy bùng nổ A* hoặc summon. Không có GPU timer để khẳng định toàn bộ bottleneck là SwiftShader.

Lượt Demo đầu có tác vụ GL desktop/compile cùng máy: Mini21,94–23,28; Final phase1 19,53–23,17; phase2 18,86–19,36FPS. Giữ các số này trong evidence, không bỏ qua để tuyên bố mọi cảnh luôn24–25FPS. Lượt Hard tách tác vụ render đồng thời không thấy sụt nghiêm trọng do Boss; chưa phải so sánh A/B cùng population và không chứng minh60FPS. Cần kiểm tra thiết bị Android thật. Cap60+2 được kiểm tra logic; **chưa đo Android với đủ60 quái thường và hai Boss cùng lúc**. `fps-windows.json`, `fps-summary.json` lưu số đo/cách phân nhóm.

GameAssets vẫn19 texture/44.285.956 bytes RGBA (~42,23MiB), font UI thêm0,5MiB như trước. Không load texture lúc Boss spawn. Chi phí thêm chủ yếu là navigation/A* footprint lớn preallocated theo session, khoảng1,3MiB primitive arrays cho map200×200, cộng trạng thái hai Boss và24 đạn. Không thêm tài nguyên GPU Boss.

## 12. Contract UI/native bàn giao

HUD chỉ đọc `session.bosses.primary()/mini()/lich()`, event states, notice key/name/time và scalar HP/maxHP/phase/cast. Không để presentation thay timer, HP hoặc phase. World FX và BossHud nhận cùng session; không giữ reference actor qua Retry.

`session.result()` có sau GAME_OVER hoặc VICTORY, gồm `sessionId`, `victory`, `survivedSeconds`, `progressSeconds`, `kills`, `miniBossDefeated`, `finalBossDefeated`, `difficulty`, `mode`, `language`. sessionId hiện duy nhất trong vòng đời process; native persistence sau này cần ID bền vững/idempotency riêng. Nếu nối Native Victory, truyền chính snapshot này trên UI thread, có lifecycle guard, không dùng preview stats hoặc tự cộng achievements nhiều lần. Bridge đó chưa nằm trong task.

Antigravity có thể tinh chỉnh màu/spacing trong BossHud và dialog Victory, giữ API và hit-test Retry/Home hiện tại. Hai tên/phase và số HP được cache khi thay đổi; không tạo lại font/texture. Không thêm nút native, sửa XML, theme, menu hoặc preference key.

## 13. Hướng dẫn kiểm tra thủ công và hạn chế còn lại

1. Android Studio chọn cấu hình `android`, Pixel_4 rồi Run. Đảm bảo không có `files/boss-test.json`, `boss-command.json`, `combat-profile.json` hoặc seed diagnostic nếu muốn seed mới. Chọn ngôn ngữ ở Settings, difficulty và Normal/Demo tại menu gốc.
2. **Normal:** chơi và nhặt nâng cấp; tại05:00 kiểm tra Night + Mini announcement/bar/marker; tại10:00 kiểm tra Lich dù Mini chưa chết. Sau10:00 Boss sống phải tiếp tục trận. **Demo:** hai mốc khoảng45/90 giây gameplay thực; thời gian pause/modal không tính.
3. Né slam/AoE sau telegraph, thử bắn/đánh qua vật cản, giảm Lich xuống50% để thấy Entrance/thông báo/fan đạn/nhịp nhanh hơn. Kiểm tra HP bar giảm theo hit thật và immunity0,7s còn hoạt động.
4. Hạ Final: chờ Death xong, đối chiếu Victory với kills/thời gian/Boss đã hạ. Retry phải giữ lựa chọn mode/difficulty/language, seed mới khi không fixed. Thử chết khi Boss sống, Home, Back/Pause và đưa app ra nền lúc đang giữ joystick.
5. Chỉ dùng Studio Run; không cần export APK/AAB. Fixture có thể rút ngắn kiểm tra nhưng phải ghi rõ trong kết quả. Script Android khôi phục cờ/preferences ở `finally` và lưu bản backup trong reports.

Đã xác nhận gameplay/runtime theo các phạm vi trên, chưa có playtest không hỗ trợ đủ5/10 phút Normal, chưa có thiết bị vật lý hoặc mọi tình huống địa hình trên mọi seed. Pool Mini chỉ tám loài phù hợp và chia sẻ slam; chưa có kỹ năng riêng cho từng loài. Balance guard/HP là baseline để tiếp tục đánh giá cảm giác chơi. Native result bridge/achievements vẫn chưa triển khai. Các giới hạn này không thay bằng thống kê hoặc kết quả giả.
