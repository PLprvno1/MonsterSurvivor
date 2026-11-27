# Task08 — Enemy Special Skills, Projectiles & Combat FX

Ngày: **15/11/2026**. Project: `D:\Android\MonsterSurvivor`.

## 1. Source và asset audit

Đối chiếu UI03, Task07, Task06.1, Task06, Task05, Task04A/04A.1, Enemy Roster/Inventory, Combat FX Integration Plan và Integration Notes. Kiểm tra EnemyDefinition/Registry/System, Enemy/Player, navigation/collision, GameSession/GameScreen, hai projectile system cũ, GameAssets, input/active skills, configs/localization và audio pipeline.

Roster vẫn **82** loài. Task08 bổ sung policy trực tiếp vào EnemyDefinition; không regenerate hoặc đổi bảng HP/damage/speed/spawn weight/pivot/catalog nhân vật. Có **49** loài dùng skill, **33** loài chủ động giữ NONE/contact-only. Chi tiết từng ID: [ENEMY_SKILL_ASSIGNMENT.md](ENEMY_SKILL_ASSIGNMENT.md).

Audit lại **123 PNG** `03_Projectiles_FX` và **39 PNG** `04_Status_Effects`, đọc kích thước/SHA256 thật; cell/frame chỉ ghi khi xác định được từ strip hoặc icon. Chín combined sheets không import được đánh dấu chưa phân tích layout, không đoán frame count. Kiểm kê thêm **320** strip Attack/Special bốn hướng thực tế và đối chiếu catalog; không suy ra clip từ tên loài. Nguồn không có FPS FX bắt buộc: timing runtime là lựa chọn game, không giả thành authored metadata. [FX inventory](core/build/reports/task08/source-fx-inventory.json), [Attack/Special inventory](core/build/reports/task08/monster-attack-special-inventory.json).

## 2. File tạo/sửa

Package mới `core/src/main/java/com/example/monstersurvivor/enemy/skill/`:

| File mới | Vai trò |
|---|---|
| `EnemySkillDefinition.java` | 23 preset immutable và NONE, loại/range/CD/windup/pattern/FX/status/gate |
| `EnemySkillAssignments.java` | Mapping theo family và các ngoại lệ variant có chủ đề |
| `EnemySkillInstance.java` | State/timer/target snapshot/animation clock theo actor |
| `EnemySkillSystem.java` | Lượt cast chung, lock/recover/cancel, release sau weapon/Ultimate |
| `EnemyProjectiles.java` | Pool hostile thường riêng, swept collision, impact và vùng độc |
| `PlayerStatuses.java` | Ba slot Poison/Burn/Slow hữu hạn, damage qua pipeline hiện có |
| `EnemySkillRenderer.java` | Sprite flight/impact/status và geometry telegraph, culling/batch |

File runtime sửa: `enemy/EnemyDefinition`, `entity/Enemy`, `entity/Player`, `ai/EnemySystem`, `gameplay/GameSession`, `assets/GameAssets`, `screen/GameScreen`, `ui/GameHud`, `localization/GameStrings`; diagnostic `debug/FrameMetrics`, `debug/SkillDiagnostics`. `assets/assets.txt` được Gradle cập nhật danh sách asset.

Asset mới: `assets/game/enemy-fx.png`, `enemy-fx.json`, `assets/game/licenses/MonstersCreatures-Enemy-FX.txt`. Tool mới: `tools/PackEnemyFx.java`, `prepare_enemy_fx.py`, `audit_enemy_skill_sources.py`, `write_enemy_fx_mapping.py`, `run_enemy_skill_checks.py`, `checks/EnemySkillChecks.java`, `checks/EnemySkillManifest.java`, `smoke/EnemySkillSmoke.java`, `run_studio_task08.ps1`, `build_task08_touch.py`, `android_enemy_skill_smoke.py`, `android_task08_profile.py`, `android_task08_baseline.py`.

Regression tools `EvolutionSmoke.java`/`UpgradeSmoke.java` cập nhật expected texture budget21 và hỗ trợ `ms.report`, để log/ảnh Task08 không ghi vào thư mục evidence cũ. `tools/android/SkillTouch.java` thêm chế độ armed: khởi động shell VM trước, chờ stdin rồi gửi cùng MotionEvent thật, không gọi trực tiếp game API. Tài liệu mới gồm báo cáo này, hai bảng mapping; nối thêm Integration Notes. Không sửa báo cáo lịch sử.

## 3. Enemy Skill architecture

EnemyDefinition chia sẻ preset; mỗi Enemy có một EnemySkillInstance; EnemySystem sở hữu EnemySkillSystem và pool. Không có subsystem AI độc lập hoặc82 thuật toán riêng. State: CHASE/PREPARE/RELEASE/RECOVER/COOLDOWN. Config lưu damage1, cooldown/windup/range/speed/pattern/FX/status/gate; runtime giữ owner, origin, target, timer, cooldown và activation counters.

GameSession chỉ chạy khi RUNNING. Chuẩn bị skill trước steering, khóa actor trong windup/recover; release sau weapon update và Ultimate resolve. Enemy bị giết/Hurt/knockback trước release sẽ không bắn. Actor vẫn dùng navigation, separation và swept movement cũ; không tăng hai A* mỗi frame.

## 4. Archetype và tuning

| Nhóm | Preset tiêu biểu | Cơ chế |
|---|---|---|
| Straight | ARROW, ROCK, COIN, SEED, BUBBLE | Một đạn tuyến tính, LOS và động tác chuẩn bị |
| Fan | THORN_FAN, MAGIC_FAN, DUST_FAN, FROST_FAN | Ba lane cách24°, cùng tâm hướng lúc release |
| Fire/magic | FIREBALL, LAVA, SHADOW | Flight/impact sprite; fire/lava có Burn sau accepted hit |
| Poison/spore | SPORE, POISON, TOXIC | Đạn độc thẳng; TOXIC thêm vùng nhỏ có grace |
| Ice | ICE, SNOWBALL, FROST_FAN | Slow25% hữu hạn; không khóa skill |
| Slam/shockwave | SLAM, SPIRIT_SLAM | Snapshot tại chân caster, warning rồi hit R38/40 |
| Targeted ground | CURSE_AOE, TARGET_BOMB | Chốt vị trí Player lúc bắt đầu; R26/28, không đuổi |
| Burst | SHADOW_BURST, FIRE_BURST | Năm lane cách18°, một activation đồng thời, CD dài |

Preset cụ thể và cooldown Easy/Hard có trong bảng82 loài. Đạn speed60–85 world units/s, lifetime2,6s; ranged không bắt đầu/phóng khi Player cách dưới48. Windup gốc0,8–1,2s; Easy nhân1,15. Mọi skill damage1; không lấy late contact damage làm đạn mạnh hơn.

## 5. Assignment82 species

Family dùng chung policy; variant ember/toxic/frost/snow/soul có ngoại lệ phù hợp. Forest có seed/thorn/spore/slam; Swamp poison/bubble/hex; Snow ice/slow; Volcanic fire/lava/burst; Cave rock/coin/spirit; Desert dust/curse; Night shadow/targeted magic. Goblin SpriteCanvas dùng Attack fallback cho đạn arrow, không tuyên bố có clip cầm cung/Special riêng. Không có Skeleton trong roster hiện hành nên không thêm loài giả.

Các loài đơn giản, thú cận chiến, defensive tanks hoặc animation chưa phù hợp giữ NONE; Wild Boar giữ legacy charge. Gợi ý `special` từ Task04A vẫn là metadata nghiên cứu, `definition.skill` mới là policy gameplay authoritative.

## 6. Direction snapshot — không homing

Projectile lấy hướng trung tâm từ Player tại release, normalize một lần; mỗi lane xoay theo góc preset và lưu vx/vy. Trong flight, chỉ `x += vx*dt`, `y += vy*dt`. Sprite direction/residual rotation cũng chốt khi fire. Update đọc Player cho collision, không dùng vị trí mới để sửa vận tốc. Không đọc owner để kéo đạn theo quái.

AoE chốt targetX/Y tại PREPARE; warning và impact cùng tọa độ đó. Slam chốt origin. Ranged warning có thể theo aim trong windup, nhưng toàn volley chốt một lần khi release; không hướng lại từng viên.

Unit tests kiểm float-bit vận tốc qua nhiều frame khi Player đổi vị trí, vượt range và Dash thật. Bằng chứng Android theo projectile ID/velocity/player movement/active Dash được lưu trong phần xác nhận cuối, tách khỏi bằng chứng fixture hoặc ảnh tĩnh.

## 7. Collision, ownership và pool

Pool thường24 shots,24 FX,3 areas, đều preallocate. Shot có ID, owner reference/ID, team ENEMY, damage, radius2, lifetime, vị trí/vận tốc và animation state. Boss owner bị từ chối ở pool thường. BossProjectiles24 và weapon pool48 không đổi hoặc mượn capacity.

Dùng CombatGeometry swept segment–expanded Player AABB; wall TOI lấy từ CollisionGrid hard-obstacle sweep, chọn impact đến trước. Chặn tree/rock/boundary; đạn bay qua water. Invalid/zero velocity bị từ chối. Impact một lần, kể cả khi Player đang iframe/immunity thì shot vẫn kết thúc; không gây lặp nhiều frame. Update không duyệt đồng minh để damage.

Owner chết: shot/poison area bị gỡ ở lần cập nhật kế; impact visual còn lại hữu hạn và không damage. Dying/Winning/Home/Retry dọn transients/status. Các vòng scan chỉ giới hạn resident/pool, không scan map200×200.

## 8. Telegraph và AoE

Cast chỉ bắt đầu trong camera và LOS/range hợp lệ. Chừa14 world units hai cạnh,44 phía dưới và50 phía trên để tránh các panel/slot UI03 che cue. Caster chưa vào vùng nhìn rõ tiếp tục tiếp cận; không đứng ngoài margin mãi do stand-off.

Ranged có ring đỏ, aim/fan guide và animation; AoE/slam có outer ring cố định, vòng tiến độ và tâm. Sau windup mới kiểm đúng circle–Player footprint cùng LOS. Player đã ra ngoài không bị hit; không hit lại người bước vào sau impact.

TOXIC tạo areaR18 khi projectile impact, cap3, grace0,65s, tổng2,3s, tick mỗi1s. Nó có ring và animated poison overlay. Area có thể xuất hiện cả khi direct hit bị immunity chặn: đây là hazard riêng, có warning/grace và vẫn phải qua Player.damage mới áp poison; không tự biến blocked hit thành status. Owner chết dọn area.

## 9. Status effects

Ba slot cố định, không cộng stack hoặc duration vô hạn:

| Status | Duration | Gameplay |
|---|---:|---|
| Poison | 2,4s | Tick1 damage mỗi1s, tối đa hai tick cho một application |
| Burn | 1,8s | Tick1 damage mỗi1s, một tick cho một application |
| Slow | 1,5s | Normal movement×0,75; base stat không sửa |

Chỉ áp sau `Player.damage` true và Player còn sống; DoT cũng đi qua pipeline đó. Immunity0,7s và Dash iframe chặn hit/status; nhiều DoT cùng đến vẫn chịu shared immunity. Accepted hit mới refresh duration hữu hạn, không tăng potency. Pause/modal dừng clock; death/Home/Retry clear. Slow không giảm Dash distance, khóa điều khiển hoặc Ultimate.

## 10. Animation Attack/Special

Trong49 loài có skill: **46** có Special thật đủ bốn hướng, **3** dùng Attack fallback thật. Prepare dùng phần đầu clip, release/recover dùng phần cuối; timing retime theo skill, không giả authored frame marker. Hurt/Death ưu tiên và hủy skill; hết recover trả Idle/steering. Không tạo Animation khi cast.

## 11. FX atlas và license

[ENEMY_FX_ASSET_MAPPING.md](ENEMY_FX_ASSET_MAPPING.md) liệt kê đủ81 strip/icon, path nguồn, frame/cell, FPS, role và atlas. Tổng274 frames trong một512² atlas, extrude2px, Nearest; giữ pixel bằng crop/copy Java, không AI/resample. Flight chọn bốn hướng thật rồi xoay góc còn lại. Impact play once; coin flight ping-pong; status/area overlays loop hữu hạn theo state.

Thêm đúng **1MiB RGBA**, GameAssets thành **21 textures /45.596.676 bytes RGBA**, font không tính. Không sửa atlas nhân vật/quái/weapon/evolution cũ. License/provenance giữ trong runtime, không phân phối asset độc lập.

## 12. Easy/Hard, spawn pressure và fairness

Cap vẫn Easy40 / Hard60, Boss riêng2. Đầu progress<150 chỉ2/3 caster đồng thời; sau đó8/10. Telegraph tối đa3/4; global start gate0,75s Easy /0,5s Hard. Preset cooldown Easy×1,2, Hard×0,9; Hard không tăng projectile speed/damage. Năm viên chỉ có burst late gate420, không áp cho mọi loài. Ranged stand-off76 chỉ trong vùng cast nhìn được; minimum fire distance48 và LOS được revalidate khi release.

Không tăng HP/damage quái để bù skill, giảm enemy cap để che regression hoặc thay spawn weights. Budget test gồm crowd40/60 và overload500 requests vào fixed pools. Windup + travel cho Player80 có thời gian phản ứng; Dash không phải miễn nhiễm toàn bộ volley sau khi iframe kết thúc.

## 13. Boss compatibility

EnemyDefinition.boss luôn skillNONE; Mini không tự nhận skill theo species body. BossSystem/BossProjectiles/BossConfig/HP guard/phase/summon/Victory giữ nguyên. Ordinary and Boss pools/team ownership không giao nhau. BossChecks qua; GL kiểm guarded death/Victory/Retry và Android kiểm hai phase cùng ordinary attacks.

## 14. Active skills, evolution và rewards

Dash Lv1–5 kiểm iframe thật, tiêu thụ projectile blocked, vận tốc đạn không đổi khi Dash, Slow không rút distance. Ultimate Lv1–5 kiểm hit/knockback hủy enemy windup trước release. GL chạy cả Blade Orbit/Arrow Storm/Inferno Meteor/Thunder Web cùng ordinary casts; Enemy/projectile deaths dùng pipeline cũ.

Weapon/Skill Box vẫn đi qua pickup, selection token và commit cũ; không thêm grant từ UI/normal gameplay. Pause/modal freeze cast/shot/area/status. Player Burning Ground nằm trong weapon pool implicit PLAYER và chỉ damage Enemy; poison area có team ENEMY, owner Enemy và chỉ damage Player. Không sửa combat/tuning/evolution configs hoặc ProjectileSystem của Player.

## 15. UI03 và localization

Giữ nguyên vị trí/hit-test joystick, skill buttons, slots, HP/timer/Pause/Boss/modal. Chỉ thêm ba pill status nhỏ có icon và nhãn ĐỘC/CHÁY/CHẬM hoặc POISON/BURN/SLOW bên dưới stats, cùng overlay nhẹ trên Player. Pill dọc tránh dải Boss và nút skill; chỉ hiện khi active. Không đổi preferences hoặc game_language. Kiểm glyph thật/fit VI/EN trong GL.

## 16. Audio

Không thêm SFX/AudioEvent, sửa manager/mapping, tải thêm sound hoặc gán cue mới cho enemy projectile. Enemy hit/death/reaction và Player/Boss audio giữ pipeline cũ. JSON selection SHA256 vẫn `0f185d2d5cfba00504e5ccedc55d9e35a63d2887c0d649c0812c77280e8f7cfd`;49 selections/36 files giữ nguyên. AudioChecks190 PASS.

Lượt GL tăng tốc đầu tiên gặp `Sound.play` rejected ở backend LWJGL mặc định16 sources (SFX cùng streaming music chia nguồn). Đã archive log, cấu hình **test harness**32 backend sources rồi chạy lại: failures0, voice budget production16 không sửa. Android kiểm playback failures riêng. Không tuyên bố đã nghe/cân âm.

## 17. Tests

Compile Java core + Android offline thành công; không đổi dependency/Gradle. Các kết quả logic/GL cuối:

| Check | PASS |
|---|---:|
| EnemySkillChecks —82 policies,23 presets, direction/collision/status/cancel/session/caps | 3.772 |
| EnemySkillSmoke — actual GL/gameplay/clips/FX/VI-EN/status/evolution/results | 4.418 |
| EvolutionSmoke — real boxes/touch, bốn evolution VI/EN | 3.778 |
| BossChecks | 2.901 |
| SkillChecks | 1.463 |
| EvolutionChecks | 10.527 |
| AudioChecks | 190 |
| Task04A1Checks — input/localization | 2.802 |
| DiagnosticIsolationChecks | 20 |
| UpgradeChecks | 1.690 |
| WeaponChecks | 235 |
| PickupChecks | 29.227 |
| UpgradeSmoke — GL/input/actual upgrades/Retry | 219 |

EnemySkillManifest sinh bảng82 policy từ runtime/catalog thật. Không chạy full82 animation stress hoặc asset pipeline quái cũ. New atlas pipeline chỉ pack subset được dùng.

Log: [logic](core/build/reports/task08/EnemySkillChecks.log), [GL](core/build/reports/task08/EnemySkillSmoke.log), [evolution GL](core/build/reports/task08/EvolutionSmoke.log), [compile](core/build/reports/task08/compile.log). Ảnh sạch HUD status: [VI](core/build/reports/task08/hud-three-statuses-vi.png), [EN](core/build/reports/task08/hud-three-statuses-en.png). Ảnh warning/flight/impact từng archetype có prefix `enemy-warning-*`, `enemy-release-*`, `enemy-flight-impact-*` trong thư mục evidence; đây là explicit GL fixtures.

## 18. Android Studio Run và FPS

Run cấu hình android/Pixel_4 bằng Android Studio, landscape2280×1080/API37/SwiftShader. Build được Studio deploy; không export/manual install APK. Java compile và log IDE được lưu riêng. Các kết quả Android, probe trước/sau và scene population cuối được tổng hợp ở phần xác nhận bàn giao.

FrameMetrics thêm ENEMY_SKILLS, ENEMY_PROJECTILES, ENEMY_FX; vẫn ghi AI/collision/world/HUD và wall-clock frame. CPU render submit/flush không phải GPU timer độc lập. Chỉ dùng window RUNNING/unpaused ngoài trace để đánh giá FPS, không lấy ảnh diagnostic làm chứng cứ thắng tự nhiên.

## 19. Diagnostic isolation và bằng chứng test

Không đổi normal DiagnosticRun.NONE/AndroidLauncher hoặc one-use debug gate. SkillDiagnostics chỉ trong UUID được cấp lease: các command enemyArena/enemySkills/trace là opt-in, không preference/UI hoặc file root mới. Arena đặt loài registry trên ground/LOS thật, giữ caps/stats bình thường, không cấp equipment; progression/HP protection của fixture phải ghi rõ. Trace ngắn theo frame ghi identity/velocity/Player/Dash để không lệ thuộc độ trễ adb screenshot.

Tool snapshot preferences + tám legacy paths, chỉ write/cleanup namespace owned; verify byte-equal, không restore đè dữ liệu khác. Normal Fresh Start sau test phải HP10/Sword1/skills1/debugfalse/random seed, không legacy trigger. Shell input helper chỉ inject MotionEvent, không gọi activate/evolve trực tiếp.

Các attempt harness trước được archive: đọc status sau screenshot có thể thấy status đã hết; status threshold quá cao bỏ lỡ một application còn0,51s; cold shell VM/screenshot có thể lâu hơn lifetime2,6s của projectile. Đã sửa thu thập snapshot/trace và warm shell VM, không tăng lifetime/status để chiều test. Không che lỗi hoặc dùng diagnostic screenshots làm bằng chứng gameplay tự nhiên.

## 20. Giới hạn

Chưa có playtest mười phút không hỗ trợ trên điện thoại thật hoặc kết luận balance đầy đủ. Một số loài giữ NONE có chủ ý; không thêm blink/split/summon/buff chỉ vì có sprite Special. Goblin arrow dùng Attack chung, chưa có clip cung chuyên biệt. Telegraph là geometry bổ sung; flight/impact/status dùng asset thật. AI vẫn chung navigation, không có tactical planner mới.

Mẫu FPS ngắn trên SwiftShader chịu dao động host; không cam kết60FPS hoặc suy ra GPU speedup. Chưa đo mọi tổ hợp worst-case48 hostile shots (ordinary + Boss),3 evolved weapons và60 normal residents trên thiết bị thật. DoT dùng shared immunity nên Poison/Burn cùng tick có thể chỉ nhận một damage; đây là luật hữu hạn đã chọn. Không cân âm lại, hot-switch ngôn ngữ hoặc thêm native status settings.

Chỉ Task08; không Endless Mode, backend/Docker, release/signing/publishing, export APK/AAB, commit hoặc push.

## Xác nhận bàn giao cuối

**Android68 PASS**, sau đó **Normal Fresh Start38 PASS** trên bản cuối. Studio Run **22:43:39 Asia/Saigon**, deploy/mở Home **22:43:44**, Pixel_4/emulator-5554. [IDE deployment](core/build/reports/task08/studio-deploy-final.log), [Android checks](core/build/reports/task08/enemy-android-checks.json), [Android log](core/build/reports/task08/enemy-android.log), [normal checks](core/build/reports/task08/phase0-checks.json).

Android đã chạy các scene straight, fan, poison, ice/slow, AoE, elemental/night; Pause freeze projectile, actual Weapon Box nâng Sword rồi chọn Blade Orbit, actual hai-pointer Ultimate, Mini/Lich phase1/2, guarded Boss death/Victory, Game Over và Retry. Không crash/Playback failed, các window thu được GLerror0. Hai ngôn ngữ được kiểm ở GL; Android giữ preference VI hiện tại.

### Bằng chứng không homing và né bằng Dash

[android-nonhoming-proof.json](core/build/reports/task08/android-nonhoming-proof.json) lưu năm projectile **ID33–37** của burst, velocity không đổi theo từng ID qua nhiều frame. Trong trace, PlayerX1608 giữ nguyên, PlayerY đi từ1608 xuống1516,70 (Dash cộng normal movement), có frame Dash active. Projectile tiếp tục di chuyển theo vector cũ; số accepted hits của pool thường giữ5→5 trong mẫu. [Touch stream](core/build/reports/task08/android-dash-touch.log) là MotionEvent hai ngón thật, joystick owner0 được giữ. Đây là controlled diagnostic case, không khẳng định Dash miễn nhiễm cả trận hoặc mọi hướng né đều đúng.

Shell VM được warm bằng chế độ armed trước khi viên đạn sinh ra; lúc trigger vẫn gửi chuỗi touch nguyên bản qua Android InputManager. Không tăng lifetime, bỏ collision, sửa vận tốc hoặc bypass activation. Trace chỉ bật ngắn trong diagnostic và không dùng để báo cáo FPS.

### FPS/frame time

Đối chứng cùng Pixel_4, Hard/Normal, seed42, protected HP, Sword1, không input, khoảng wall-clock giây3–28. Mỗi bản lấy4 window120 frames, tổng480 frames. Đây là tải đầu trận4–8 quái, trước skill gate đầu tiên; nó đo overhead baseline, không đại diện cảnh projectile bão hòa.

| Chỉ số | UI03 trước Task08 | Task08 |
|---|---:|---:|
| FPS tổng hợp | 25,92 | 25,19 |
| Mean frame | 38,57 ms | 39,69 ms |
| Khoảng p95 từng window | 49,10–58,63 ms | 51,77–64,18 ms |
| Max frame | 84,77 ms | 93,66 ms |
| AI CPU trung bình | 0,216 ms | 0,228 ms |

FPS mẫu giảm2,81%, mean frame tăng1,12ms; chưa thấy regression nghiêm trọng trong phép đo ngắn này. Không suy ra statistical significance hoặc đảm bảo cho mọi thiết bị/tải. [Raw summary](core/build/reports/task08/performance-summary.json), [before](core/build/reports/task08/profile-before.json), [after](core/build/reports/task08/profile-after.json).

Các scene active dưới đây dùng window sau ít nhất6s từ lúc đặt arena, RUNNING/unpaused; bỏ scene trace và không trộn ba lần arena fan vào cùng một hàng. CPU là trung bình window; chưa cô lập GPU/flush backend.

| Scene | Normal / Boss | FPS | Mean / p95 ms | AI ms | Enemy prepare ms | Enemy release/projectile ms | Enemy FX submit ms |
|---|---|---:|---|---:|---:|---:|---:|
| Straight | 9–12 /0 | 25,15 | 39,76 /54,15 | 0,415 | 0,117 | 0,152 | 0,539 |
| Fan đầu | 15–19 /0 | 24,98 | 40,07 /53,79 | 0,518 | 0,083 | 0,175 | 0,653 |
| AoE | 20 /1 | 23,40 | 42,73 /60,00 | 0,493 | 0,054 | 0,028 | 0,810 |
| Elemental/night | 16–24 /1 | 22,50 | 44,59 /71,53 | 0,730 | 0,051 | 0,050 | 1,159 |

Riêng Lich phase2 + Blade Orbit/Bow1,3–5 normal và2 Boss: hai window22,07/23,40FPS, mean45,31/42,74ms, p9567,50/60,12ms, max107,15/104,07ms. Hai window đó ghi enemyShots0 tại cuối window; đây là compatibility/load sample, không phải proof worst-case24 shots. [Scene windows](core/build/reports/task08/scene-performance.json), [Lich phase2](core/build/reports/task08/scene-task08-lich-phase2.json).

### Screenshot

| Android controlled state | Evidence |
|---|---|
| Straight projectiles | [scene](core/build/reports/task08/android-enemy-straight.png) |
| Fan | [scene](core/build/reports/task08/android-enemy-fan.png) |
| Poison | [status](core/build/reports/task08/android-status-poison.png) |
| Ice/Slow | [status](core/build/reports/task08/android-status-slow.png) |
| Movement/Dash sau fire | [after](core/build/reports/task08/android-nonhoming-after.png) |
| Ground AoE | [scene](core/build/reports/task08/android-enemy-aoe.png) |
| Elemental/night | [scene](core/build/reports/task08/android-enemy-elemental.png) |
| Blade Orbit + enemy skill | [scene](core/build/reports/task08/android-enemy-blade-orbit.png) |
| Lich phase2 | [scene](core/build/reports/task08/android-enemy-lich-phase2.png) |
| Victory / Game Over | [Victory](core/build/reports/task08/android-enemy-victory.png), [Game Over](core/build/reports/task08/android-enemy-game-over.png) |
| Normal fresh, không diagnostic | [first seconds](core/build/reports/task08/phase0-fresh-first-seconds.png) |

Ảnh GL VI/EN dùng config riêng để nhìn FX; enemy HP1000/speed thấp trong GL showcase không phải tuning runtime bình thường. Android arena dùng stats bình thường, protected HP chỉ trên Player; rewards/evolution qua actual boxes/touch. Không gộp các attempt harness fail thành PASS. Attempt4 đã chứng minh vector cố định, nhưng input VM cold đến sau hit đầu tiên; sau warm/armed helper, mẫu cuối thực sự né và cả68 checks qua.

Scope/hash và package: [scope-final.json](core/build/reports/task08/scope-final.json), [source fingerprints](core/build/reports/task08/final-source-hashes.json), [packaged asset check](core/build/reports/task08/packaged-asset-check.json). Atlas/metadata deploy khớp source, không có fixture JSON/diagnostic-runs trong package. Audio JSON và các config/Boss/weapon/input contracts cũ giữ nguyên. Snapshot preferences +8 legacy paths byte-equal sau toàn bộ lượt test; xác nhận cleanup cuối ở [final-normal-state.json](core/build/reports/task08/final-normal-state.json).


### Ch?t cleanup ng?y 16/11/2026

Sau khi m? l?i emulator, ph?t hi?n s?u legacy fixture ch? ch?a chu?i l?i `cat: ? No such file or directory`. ?? l?u nguy?n byte ? [backup](core/build/reports/task08/reopened-emulator-invalid-fixtures-backup.json), x?c nh?n n?i dung kh?ng ??i r?i x?a ??ng s?u file l?i; kh?ng ghi l?i preferences. Fresh Start ch?y l?i **38 PASS**, hai l??t Start d?ng seed ng?u nhi?n kh?c nhau, HP10/Sword1/skills1/debug=false/diagnostics=none; runtime ghi ??ng21 textures/45.596.676 bytes RGBA.

Ki?m tra cu?i: ch?n app paths byte-equal v?i snapshot g?c, t?m legacy triggers v?ng m?t, hai helper t?m ?? x?a. C?22 fingerprint runtime v?n kh?p source ?? qua ki?m th?;198 file g?c ???c ??i chi?u ph?m vi. `git diff --check` exit0. Ki?m tra provenance81 ngu?n v? b?n license nguy?n byte ??u PASS. Kh?ng thay gameplay sau c?c l??t ki?m th?.
