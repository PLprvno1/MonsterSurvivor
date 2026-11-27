# Task 07 — Weapon Evolution

Ngày 15/11/2026, Asia/Saigon. Project `D:\Android\MonsterSurvivor`. Chỉ Task07; không Task08, commit/push, export APK/AAB hoặc release. Working tree có thay đổi từ các task trước và được giữ nguyên.

## 1. Source audit

Đã khảo sát WeaponLoadout/Instance/System, SwordCombat, ProjectileSystem/Geometry/Targeting, WeaponOffer/Selection, PickupSystem, GameSession, GameScreen, WeaponHud/GameHud, AudioManager và Enemy/Boss damage. Đọc các báo cáo Task06.1, Task06, Task05.1B, Task05, UI02, Integration Notes và Combat FX plan. Các ghi chú cũ về root fixture hoặc chưa có Boss/Evolution là lịch sử; Task06.1 và báo cáo này là contract hiện hành.

Mở rộng hệ thống đang có: cùng loadout ba slot, pool projectile/effect48, Enemy.damage/death hook và transaction Weapon Box. Không dựng hệ thống vũ khí song song, không sửa AI/HP Boss hoặc enemy roster82. Chủ dự án đã xác nhận chỉ Codex đang sửa source, không có Antigravity chạy đồng thời.

## 2. File tạo/sửa

Tạo `config/EvolutionConfig.java`; assets `game/evolution-fx.png/json`, `game/licenses/MonstersCreatures-Evolution.txt`; tools `prepare_evolution_fx.py`, `PackEvolutionFx.java`, `run_evolution_checks.py`, `checks/EvolutionChecks.java`, `checks/EvolutionBalance.java`, `smoke/EvolutionSmoke.java`, `android_evolution_smoke.py`, `run_studio_task07.ps1`; báo cáo này.

Sửa WeaponInstance/Loadout/Offer/Selection/System/Renderer, ProjectileSystem, WeaponConfig (chỉ mở trần validation cho evolved tuning), SwordCombat (gate bỏ sword cũ khi evolved), GameSession (cue sau commit), GameScreen (log offer có cấu trúc), GameStrings, WeaponHud và GameAssets. SkillDiagnostics thêm command/evidence trong capability hiện có; không thay isolation. Baseline harness và touch-helper builder nhận thêm `--task07` để giữ evidence lịch sử. WeaponChecks cập nhật kỳ vọng Lv5→Evolution→fallback; UpgradeSmoke cập nhật một atlas tăng thêm. Append Integration Notes.

Không sửa normal AndroidLauncher, DiagnosticLauncher/manifest/lease, native XML/preferences, Gradle, audio selections/manifest, map, navigation, enemy/Boss stats hoặc AI, skill tuning và source artwork.

## 3. Evolution architecture

WeaponInstance giữ `level=1..5` và boolean `evolved` riêng. `tuning()` trả bảng thường hoặc EvolutionConfig immutable; không có Lv6 hoặc lưu progression vào preferences. `evolutionEligible()` chỉ đúng tại Lv5 chưa evolved. Mutation evolve có scope package và tăng revision loadout đúng một lần. Projectile giữ snapshot damage/tuning lúc khai hỏa; projectile cũ đang bay không biến thành evolved khi chọn reward.

Sword evolved bỏ toàn bộ đòn kiếm cũ qua gate SwordCombat; contact damage của quái vẫn đi đường cũ. Ranged tiếp tục dùng WeaponSystem và ProjectileSystem. End/death/Victory clear projectile, effect, volley references và fire zones; Retry tạo session mới Sword1.

## 4. Điều kiện unlock và commit

Lv5 chỉ đánh dấu eligible. Phải mở thêm Weapon Box và chọn offer EVOLUTION hợp lệ. Offer revalidate alive/level/evolved; GameSession còn kiểm phase, token, index và pending item còn tồn tại. Chỉ sau apply thành công mới consume item qua collection handler. Không tự evolve từ render, thời gian, Boss hoặc Skill Box. Không đổi số slot hoặc reset về Lv1 sau evolution.

Cooldown ranged đang hồi được scale theo tỷ lệ old/new total, không cấp thêm lần bắn READY. Projectile/action đã khai hỏa giữ snapshot. Evolved Sword thay action cũ từ update tiếp theo; pending sword hit cũ bị hủy, không cộng hai cơ chế.

## 5. Weapon Box offer policy

Giữ ba card và chính sách hộp đầu Bow/Fireball/Lightning. Mỗi hộp tiếp theo **bảo đảm hiển thị mọi owned weapon eligible**, tối đa ba, trước khi lấp bằng unlock/upgrade random và stat fallback. Trường hợp chỉ Sword đã Lv5 cũng được offer evolution. Không chờ xác suất hàng chục hộp và không tự commit.

`WeaponOffer.Kind.WEAPON` giữ tương thích; `action()` phân biệt UNLOCK/UPGRADE/EVOLUTION/FALLBACK, Kind.EVOLUTION riêng cho evolution. `fromLevel/toLevel=5/5`, không 5/6. Vũ khí đã evolved bị loại khỏi cả upgrade/evolution pool. Đủ ba evolved thì dùng Damage+2, MaxHP+2/heal2, Speed+1 tới20 như trước; không modal rỗng hoặc consume khi open/apply thất bại. Trần integer cuối cùng vẫn dùng eligibility fallback cũ.

## 6. Sword — Vòng Kiếm / Blade Orbit

Hai lưỡi kiếm quay quanh tâm player, radius42, angular speed3.5 rad/s (mỗi nửa vòng khoảng0.898s). Hitbox thật có half-width9 cộng collider target. Quét substep theo góc/translation, tối đa64, bao gồm dịch chuyển player/Dash; mỗi enemy có hit cooldown0.55s, ledger64 entry cho cap60+2. Tối đa6 hit trong một update. LOS từ tâm tới blade và target ngăn xuyên cây/đá. Không damage theo render.

Hai blade thép bằng texel có sẵn, kèm slash strip chạy12fps theo gameplay clock và hit feedback pooled. Swing cue khoảng mỗi nửa vòng; hit cue gộp, giới hạn0.12s và chỉ phát khi HP giảm. Kiếm chém/windup cũ không cộng vào orbit.

## 7. Bow — Mưa Tên / Arrow Storm

Một activation tạo năm tên, góc -18/-9/0/9/18 độ theo hướng target lúc bắn. Speed350, lifetime1.25s, không homing. Mỗi tên xuyên tối đa hai target; cả fan chia sẻ ledger tối đa10 ID, mỗi target chỉ nhận một hit/activation dù nhiều lane giao nhau. Pool phải đủ chỗ cho cả năm mới bắn, không partial fan hoặc tăng pool.

Swept enemy/wall collision, hit trước tường mới hợp lệ; lifetime/clear trả lại pool và reference của fan. Ledger projectile được mở tới8 theo trần config chung của Lightning; Arrow Storm thực tế vẫn hai pierces. Một release cue và tối đa một arrow-hit cue cho cả fan. Projectile sprite strip hai frame12fps, Nearest.

## 8. Fireball — Thiên Thạch Lửa / Inferno Meteor

Một projectile lớn40 world units về mặt trình bày, collision radius4, speed185, lifetime2s và aimed-distance detonation. Một cast không tạo ba meteor. Blast radius58, tối đa12 target hợp lệ/impact; mỗi target damage một lần qua LOS.

Blast để lại zone2.25 gameplay seconds. Tick chung0.5s (4–5 cơ hội tùy pha clock), tối đa12 target/tick. Không catch-up burst. Tối đa ba zone, thay zone già nhất khi đầy. Overlap dùng damage mạnh nhất **một lần mỗi target mỗi tick**, không cộng DoT theo số zone. Zone không đánh ngoài radius hoặc xuyên LOS. Pause/modal freeze tuổi và clock; clear/end dọn hết. DoT không tự phát fire-cast/explosion SFX theo tick.

## 9. Lightning — Lôi Võng / Thunder Web

Reserve ba root gần player trong range215, sau đó mỗi nhánh chain tối đa hai hop trong range94. Budget chung tám target/activation, visited ID chung gồm cả root; thực tế có thể ít hơn nếu LOS/range/crowd không đủ. Không steal root, quay lại target cũ hoặc chain vô tận. Các edge thật được lưu vào effect pool và render đường nối từ player/root/target tương ứng.

Bolt width3, fade0.24s, spark impact bốn frame trong0.14s. Spark được vẽ cùng atlas theo nhóm, không đổi texture mỗi branch. Một activate cue và một impact cue nếu có HP giảm, không một cue mỗi nhánh.

## 10. Balance thực tế

Damage = round(baseDamage × multiplier + bonus), min1, saturate int. **Bảng thường Lv1–5 không đổi**.

| Dạng | Multiplier + bonus | Damage khi base1 / 3 / 6 | Chu kỳ | Giới hạn |
|---|---|---|---|---|
| Blade Orbit | 1.5× +2 | 4 / 7 / 11 | per-target0.55s; hai blade quay3.5rad/s | 6 hit/update; thực tế phải chạm blade |
| Arrow Storm | 1.5× +2 | 4 / 7 / 11 | 1.3s | 5 lane ×2 pierce; dedup toàn fan |
| Inferno Meteor | 9× +4 | 13 / 31 / 58 | 2.3s | 1 projectile; blast12 target/R58 |
| Burning Ground | 0.85× +1 | 2 / 4 / 6 | tick0.5s, duration2.25s | 3 zone; max12 target/tick, không stack |
| Thunder Web | 3.6× +1 | 5 / 12 / 23 | 2.9s | 3 root; 2 hop/branch; tổng8 target |

Probe paired20s, baseDamage3,30 target đứng yên trên ba vòng40/72/104, không AI di chuyển, đo HP delta thật; không cộng Sword1 vào ranged probe:

| Weapon | DPS tổng Lv5 | DPS tổng evolved | Target đã trúng Lv5/evolved | Activation Lv5/evolved |
|---|---:|---:|---:|---:|
| Sword | 30.0 | 78.4 | 4/10 | 25/22 nửa-vòng |
| Bow | 5.7 | 15.75 | 1/3 | 19/15 |
| Fireball | 120.0 | 172.4 | 16/8 | 10/9 |
| Lightning | 19.2 | 33.6 | 4/8 | 8/7 |

Trong cùng probe, hit trực tiếp trung bình/activation Lv5→evolved: Sword4→10.18 (orbit tính theo nửa vòng), Bow1→3, Fire20→8, Lightning4→8. Fire cũ có thể lặp target giữa ba blast trong một volley; đây là số hit registration trung bình, không gọi20 là20 target duy nhất. Meteor mới chỉ một blast, mỗi target một hit. CSV có thêm trung bình số target nhận burn/tick để tách damage trực tiếp và DoT.

CSV đầy đủ: `core/build/reports/task07/balance.csv`, base1/3/6 và10/30/60 residents. Single target/geometry khác có thể khác: không gọi DPS tổng trên nhiều quái là single-target DPS. Peak ranged trong probe: Bow5, Meteor1; pool vẫn48. Probe dồn60 quái và damage cao xác nhận tổng blast12+orbit6+web8 không xóa60 trong một update; ground tick cũng capped12. Default stage-scaled Slime/Boar tại progress0/210/540 và cả EASY/HARD × NORMAL/DEMO được kiểm riêng. Đây không thay playtest sinh tồn đủ10phút hoặc82-species stress.

## 11. VFX và assets

Khảo sát folder123 projectile/combat PNG và candidate trong Combat FX plan. Không có lightning bolt strip riêng, nên dùng geometric bolts hiện có và hit spark. Import **sáu strip /24 frame**, một atlas256×256, +262144 bytes RGBA: fireball_right4f, arrow_right2f, explosion32×32_6f, slash_white32×32_4f, hit_spark16×16_4f và overlay_burn32×32_4f. Tất cả đều được dùng. Không import enemy skill FX, cả pack hoặc asset marketplace mới.

`evolution-fx.json` giữ source path/SHA256/frame rectangles/FPS; license đi kèm. Tool ImageIO local crop/copy lossless, giữ nguyên pixel/frame order. Một texel trắng tại(255,255) cho geometry dùng cùng texture với sprite FX, loại các lần đổi texture nội bộ WeaponRenderer; không đổi màu/hitbox/tuning của Lv1–5. README không ấn định exact authored FX timing: playback là tuning rõ ràng12fps projectile/slash,18.75fps blast,28.571fps spark,8fps burn. Nearest filtering, không resample PNG hoặc texture mỗi frame. GameAssets từ19 lên20 textures, RGBA44,548,100 bytes; không tính font/driver/CPU copies.

## 12. Modal/HUD/localization

Offer hiện nhãn TIẾN HÓA/EVOLUTION đầy đủ trong title, tên gốc Lv5→tên evolved và mô tả cơ chế. Badge TH/EV, stripe vàng; title0.85 và description0.75 đo fit518 units. Slot giữ tên ngắn +TH/EV và viền vàng, không Lv6, không mở rộng geometry hoặc chồng skill/Boss. VI/EN đầy đủ, kiểm glyph thật và ảnh HUD nền trơn; không đổi font hoặc `game_language`.

Weapon tokens vẫn dương, skill âm; same pending collection semantics. GameScreen/GameInput route modal theo phase, chặn Dash/Ultimate, reset pointers khi mở/commit và drop delta sau Resume. Pause trên modal giữ pending offer, lifecycle cần explicit Resume; Retry/Home/end clear cả hai selections. Không sửa joystick ownership/hit-test skill.

## 13. Audio integration

Giữ49 mappings,36 file và toàn bộ manifest/selections. JSON SHA256 `0f185d2d5cfba00504e5ccedc55d9e35a63d2887c0d649c0812c77280e8f7cfd`. Evolution commit dùng đúng existing weapon-upgraded cue. Các weapon cue gốc được gộp theo activation/fan/impact như phần6–9; không thêm sound quái, mapping hoặc cue tự chọn. Ba optional Dash/Ultimate hooks vẫn silent; không thêm WEAPON_EVOLUTION ID chưa cần dùng. AudioChecks190 PASS không đồng nghĩa đã audition mọi cue.

## 14. Boss/damage/death/drop

Mọi damage vẫn qua Enemy.damage, gồm orbit, pierce, explosion, burn và chain. Boss burst budget ceil20%maxHP/0.2s không bị bypass; không tăng HP hoặc knockback Boss. Tests phủ Mini/Lich với bốn evolved weapons trong cả bốn tổ hợp modes/difficulty; actual session phase2 và Victory đi qua damage/Death animation thật.

Death hook duy nhất vẫn cấp GameSession.kills và pickup deathRoll; repeated corpse/DoT không nhân kill/drop. Forced100% health-drop chỉ nằm trong test fixture để assert chính xác, không sửa xác suất gameplay. Counters hit của subsystem là lần hit registration/attempt; HP delta là số đo damage sau guard.

## 15. Test results

| Suite | Kết quả |
|---|---:|
| EvolutionChecks | 10,527 PASS |
| EvolutionSmoke | 3,748 PASS |
| DiagnosticIsolationChecks | 20 PASS |
| UpgradeChecks | 1,690 PASS |
| SkillChecks | 1,473 PASS |
| Task04A1Checks | 2,770 PASS |
| WeaponChecks | 235 PASS |
| PickupChecks | 29,227 PASS |
| BossChecks | 2,901 PASS |
| AudioChecks | 190 PASS |
| UpgradeSmoke | 219 PASS |
| Android evolution/touch | 73 PASS |
| Android normal Fresh Start/Home Start | 38 PASS |

Compile core + Android Java: BUILD SUCCESSFUL in 16s. EvolutionChecks phủ actual pickup→modal→commit, stale/cross-kind/Retry tokens, cap/fallback; hit cooldown, fan dedup/sweep/pierce, LOS, fire duration/tick/overlap, three-weapon60-resident burst, all-mode Boss guard và actual Victory/death/drop. GL smoke dùng touch down/up thật qua GameInput, glyph/fit VI/EN và GL_NO_ERROR; có ảnh HUD nền trơn để kiểm UI riêng. Không đồng nhất GL với Android. Reproduce: `python tools/run_evolution_checks.py`, `python tools/run_evolution_checks.py EvolutionBalance`; Android Studio Run trước khi dùng `python tools/android_evolution_smoke.py`, sau đó `python tools/android_baseline_check.py --task07`. Evidence/log/screenshot dưới `core/build/reports/task07/`.

Trước Android harness, chạy `python tools/build_skill_touch.py --task07` để chuẩn bị shell touch helper; không build/cài APK. Sau test helper tạm được gỡ.

## 16. Android Studio Run

Pixel_4 emulator-5554, API37/Android17, SwiftShader, landscape2280×1080. Source cuối được triển khai qua existing android configuration bằng Android Studio Run: `2026-11-15T13:40:51.3563999Z Invoked Android Studio Run (Shift+F10), existing android configuration, project MonsterSurvivor.` (timestamp UTC; cộng7 giờ cho Asia/Saigon). IDE deploy/launch evidence ở `studio-deployment-evidence.log`. Không manual install/assemble/export hoặc signing/release; packaging nội bộ là phần của IDE Run.

Lượt diagnostic cuối **73 checks PASS**. Seed42/protectedHP100000/progress command có khai báo qua namespace/one-use lease, bắt đầu Sword1 và không free equipment. Actual Weapon Box + touchscreen down/up nhận đủ Sword/Bow/Fireball/Lightning Lv5 rồi Evolution; ba evolved cùng lúc giữ đúng ba slot, Lightning ở lượt Retry riêng. Kiểm double tap, Pause trên evolution modal, Skill Box/Dash upgrade không thay loadout, joystick+Dash bằng hai pointer thật, Boss phase2/Victory, GameOver/Retry reset. Thư viện touch chỉ là shell dex InputManager trong /data/local/tmp, không APK/cài app. Android screenshot VFX và offer cả bốn: `android-vfx-<TYPE>-*.png`, `android-offer-<TYPE>.png`; GL snapshot riêng có prefix `evolution-vfx/offer`.

Sau cleanup, native Home→GameMode→Start và Home→Start mới **38 checks PASS** trên cùng build: diagnostics=none, debug=false, HP10/10, damage1, speed10, Sword1, Dash1 READY, Ultimate1/15s, progress/elapsed0. Hai random seed mới `-3625435954476037707` và `-2745989618795052490`, không42. Không LOCAL FIXTURE/Accepted diagnostic trong normal logs. Preferences giữ nguyên bytes, tám root trigger files vắng mặt, toàn bộ chín state checks unchanged. Cuối ở Home. Đây là bằng chứng baseline chuẩn; ảnh protectedHP không phải lượt chơi tự nhiên.

## 17. FPS/frame time và tải scene

120-frame windows, System.nanoTime + CPU FrameMetrics; không chạy GL desktop song song. Bảng lấy window RUNNING cuối trong mỗi stage (resident count tại cuối window), loại window Pause/terminal và window đầu lẫn capture/automation khi còn window steady sau đó.

| Scene | Normal/Boss | FPS | Mean ms | p95 ms | Max ms | Weapon render ms |
|---|---:|---:|---:|---:|---:|---:|
| 1 evolved: Sword + Bow1 | 5/0 | 25.32 | 39.50 | 60.88 | 85.58 | 0.69 |
| 3 evolved: Sword/Bow/Fire | 4/0 | 25.80 | 38.77 | 57.79 | 72.93 | 0.65 |
| Mini Boss + 3 evolved | 12/1 | 23.71 | 42.17 | 56.25 | 64.89 | 1.73 |
| Lich phase2 + 3 evolved | 1/1 | 22.24 | 44.97 | 62.44 | 70.91 | 2.52 |
| Thunder Web + Sword1/ranged thường | 5/0 | 24.83 | 40.27 | 59.19 | 81.63 | 0.35 |

Android đã đọc glGetError tại 155 window records trong evidence; tất cả0 (log có thể lặp snapshot khi archive). Tải scene thực tế được ghi ở từng window, không suy thành benchmark60+2 residents. SwiftShader/host không phải thiết bị thật; sample ngắn và population thay đổi, chưa có paired build tắt evolution để tách nhân quả. Weapon renderer có thêm chi phí atlas/blade/flame; primitive và sprite dùng chung atlas để tránh texture switches nội bộ. Không tuyên bố overhead bằng0 hoặc60FPS. So với mốc Task06.1 khoảng23–25FPS ở Boss, số đo trên dùng để xem mức thực tế, không phải bằng chứng cải thiện FPS. Không tăng cap quái, Boss budget, A*, scan full map/pathfinding, pool48 hoặc tạo FX vô hạn. Full log và scene JSON giữ cả window capture/terminal ngoài bảng.

Weapon render là CPU thời gian gọi draw; SpriteBatch có thể flush cuối shared pass, nên không phải GPU timing độc lập hoặc toàn bộ chi phí render của vũ khí. FPS/frame time đã bao gồm toàn frame.

## 18. Diagnostic isolation

Giữ nguyên DiagnosticRun.NONE, debug-only DiagnosticLauncher, UUID namespace, token/lease owner/expiry và consume-once của Task06.1. Không loader/root fixture mới. SkillDiagnostics hiện có thêm command spawn/reuse Weapon Box, đặt target trên collider/LOS hợp lệ, đưa Boss về arena và damage phase2 **chỉ sau explicit opt-in**. Không command grant level/evolve; các level và evolution trên Android đều nhận bằng actual modal/touch. Ordinary cap40/60, Boss2, HP/AI/tuning không đổi.

Runner chỉ tạo/cleanup UUID thuộc nó; atomic write và sentinel read Task06.1 được tái dùng. Không restore string lỗi hoặc overwrite preferences/legacy files. Cuối test so byte/presence của chín paths, rồi Fresh Start chính thức không fixture, seed mới, HP10/Sword1/debugfalse. Không lấy screenshot protectedHP làm bằng chứng gameplay tự nhiên.

## 19. Hạn chế và lỗi harness đã xử lý

Chưa hoàn thành một evolution trong lượt chơi Android tự nhiên không hỗ trợ: để lên cấp cần nhiều Weapon Box; Task07 chứng minh đủ progression bằng actual box/commit nhưng diagnostic có HP bảo vệ/placement/progress. Không tuyên bố đây là trận thắng tự nhiên hay cân bằng toàn lượt10phút. Cần playtest với người chơi để đánh giá thời điểm kiếm đủ hộp và mức khó sau evolution. Không thử điện thoại/ngón tay vật lý, stress toàn82 loại hoặc release build.

Các lượt Android trước dừng do harness: chờ hai Boss dù Mini đã chết; parser giữ CR cuối dòng nên không nhận `phase=2`; tag Skills bị logcat chèn khoảng trắng; lệnh ADB phím R sau Victory làm IME hiện và che card. Đã đối chiếu ảnh, chuyển sang chạm nút Retry thật, regex chịu whitespace và trạng thái Lich thật; evidence cũ giữ riêng. Không sửa HP/Boss để làm test qua. UI đã chỉnh label evolution đủ cỡ đọc sau kiểm tra HUD nền trơn; atlas gom primitive/sprite để giảm texture switches. Không còn lỗi compile/runtime đã biết trong lượt xác nhận cuối; giới hạn FPS nêu ở phần17.

## 20. Contract UI để Antigravity hoàn thiện

Đọc `session.weapons.loadout.size/slot/revision/describe`, `WeaponInstance.level/evolved/evolutionEligible/tuning/cooldownRemaining`. `level` tối đa5; dùng `strings.evolvedName(type)` hoặc badge localized để hiển thị evolved. Không suy state từ màu hoặc tên text.

Modal đọc `session.choosingWeapon()`, `selection.token/size/get/pendingId`; WeaponOffer.kind/action/weapon/fromLevel/toLevel/title/description/icon/r/g/b là snapshot. Evolution5→5 có action EVOLUTION, không +1 level. Commit qua `GameScreen.chooseWeapon(index,token)` trên GL thread, không gọi apply/grant/evolve từ UI hoặc giữ callback session cũ. Skill modal phải kiểm `choosingSkill()` riêng, token âm và SkillOffer riêng.

Giữ `WeaponHud.cardY/offerAt/homeAt`, HUD projection/safe insets, touch-release/token guard và floating joystick owner. Renderer đọc orbit clock/angle/center, snapshot projectile/effects/zones; không áp damage, tự tick cooldown hoặc tạo texture trong draw. Có thể polish asset/màu/layout sau khi giữ contract này; Task08 enemy special FX chưa triển khai.
