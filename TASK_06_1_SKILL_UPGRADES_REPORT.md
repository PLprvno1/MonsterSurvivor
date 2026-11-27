# Task 06.1 — Test isolation & Skill Upgrades

Ngày: 15/11/2026, Asia/Saigon. Project: `D:\Android\MonsterSurvivor`.

Đã sửa leak diagnostic trước khi triển khai upgrades. Bản cuối chạy bằng Android Studio Run trên Pixel_4: trận thường HP10/Sword1, Skill Box sinh tự nhiên và nâng cấp qua modal; diagnostic riêng kiểm tra Lv3/Lv5/Boss, sau đó Start thường được xác minh lại. Không commit/push, export APK/AAB, cài APK thủ công, release/signing hoặc backend. Không triển khai Weapon Evolution hay Enemy Special Skills.

## 1. Nguyên nhân thực tế của HP100000 và trang bị test

Đọc trực tiếp app-private runtime khi emulator khởi động lại, trước cleanup: **6 file tồn tại thật** gồm skill-test, skill-command, boss-test, boss-command, combat-profile và map-seed. Mỗi file chứa chuỗi `cat: files/<tên>: No such file or directory\n`, không chứa cấu hình JSON hợp lệ. Nội dung khớp backup của runner Task06 tại `core/build/reports/task06/android-state-backup.json`. Bằng chứng mới: `core/build/reports/task061/runtime-before.json`.

Runner cũ dùng exit code của `adb exec-out ... cat` để phân biệt absent/present. Trên thiết bị này, lỗi cat vẫn có thể đi kèm exit code0; runner lưu stdout lỗi làm nội dung gốc rồi restore thành file thật. Verification Task06 chỉ so restored bytes với backup đã sai, nên kết quả true trước đó **không chứng minh các fixture vắng mặt**. Đây là lỗi của runner và điều kiện opt-in cũ.

Các loader cũ chỉ kiểm tra `file.exists()`. JsonReader của libGDX chấp nhận chuỗi trên như scalar; `getBoolean/getInt` với default vẫn trả protectedHP=true, weaponLevel=1, progress=0. `SkillDiagnostics.tuning` hoặc `BossDiagnostics.tuning` cấp HP100000/debug=true. Dù config ưu tiên SkillDiagnostics, GameScreen vẫn chạy callback của cả ba diagnostic đã load. `BossDiagnostics.before` cấp Fireball và Lightning ngay cả weaponLevel1; điều này giải thích đúng bộ Sword/Fireball/Lightning và overlay. `map-seed.txt` đang chứa lỗi cat nên parse seed thất bại; không có bằng chứng seed42 đang là giá trị hợp lệ trong file này.

Nguồn baseline chính thức: `GameConfig.Builder.playerHp=10`, swordDamage1; `PlayerStats` dùng initialSpeed10, 8 units/s mỗi Speed; `WeaponLoadout.reset()` chỉ grant Sword1. AndroidLauncher chỉ lấy difficulty/mode và language qua GamePreferences. Preferences thực tế chỉ có theme, language và audio volumes; không có HP/equipment/debug level được lưu. Không suy nguyên nhân từ ảnh.

## 2. File diagnostic/fixture đã kiểm tra

| Runtime/source | Kết quả/đường xử lý mới |
|---|---|
| skill-test.json, skill-command.json | Hai file lỗi do runner cũ; nay chỉ đọc trong run UUID được truyền tường minh |
| boss-test.json, boss-command.json | Hai file lỗi; không tự cấp equipment khi Start thường |
| combat-profile.json | File lỗi; không tự bật protectedHP/profile hoặc sửa roster/loadout |
| map-seed.txt | File lỗi; normal launch không đọc root seed file |
| camera-check.json, asset-visual-check | Không tồn tại trên device lúc audit; đã gate cả hai đường này |
| profile CSV/camera CSV/profileInstalled | Output/cache, không phải input kích hoạt; giữ nguyên |
| monster_survivor_prefs.xml | Giữ nguyên byte, gồm game_language=vi; không thêm diagnostic preference |
| AndroidLauncher/Intent/Home/Retry | Đường thường luôn DiagnosticRun.NONE, không nhận test extras |

Đã lưu nội dung 6 file lỗi rồi chỉ gỡ những file **khớp chính xác fingerprint/nội dung lỗi do runner tạo**. Danh sách: `owned-artifact-cleanup.json`. Không xóa fixture hợp lệ hoặc file không xác định ownership của người dùng. Các báo cáo Task05/Task06, GDD, JSON audio và asset nguồn không sửa.

## 3. Cách sửa và chống tái phát

`DiagnosticRun.NONE` là mặc định của MonsterSurvivorGame/GameScreen và normal AndroidLauncher. NONE không đọc bất kỳ file diagnostic nào, kể cả khi có debug build hoặc root fixture cũ. Config `.debug(true)` do developer truyền chủ động bằng code vẫn là lựa chọn riêng; không tự cho phép fixture.

Automated tests dùng `DiagnosticLauncher` chỉ trong **debug source set/manifest**, không có trong main/release manifest. Test runner tạo UUID32hex và thư mục riêng `files/diagnostic-runs/<UUID>/`, ghi lease owner/token ngẫu nhiên/thời điểm device, rồi gọi đúng component với run ID + token. Launcher kiểm tra ID, token, owner, tuổi lease tối đa60s, xóa lease trước initialization và gỡ extras. Replay stale Intent không có lease bị từ chối, dùng gameplay chuẩn. Start từ Home/GameMode không đi qua component này, kể cả một test trước đó bị ngắt và namespace còn tồn tại.

Loader chỉ nhận JSON object, bỏ qua empty/partial/scalar. Chỉ chọn **một** fixture tuning theo ưu tiên Skill → Boss → Combat; camera trace chỉ chọn khi không có fixture tuning. Không còn chạy đồng thời các callback cấp trang bị/profile khác nhau.

`android_runtime.py` đọc bằng sentinel EXISTS/MISSING trong shell, không dựa exit code exec-out. Phân biệt file vắng mặt với file rỗng; write dùng atomic replacement. Runner mới chỉ mutate UUID namespace thuộc chính nó; cleanup chỉ namespace đó. Preferences/legacy files được kiểm tra unchanged bằng bytes/presence, không ghi lại hoặc xóa file bên ngoài namespace. Intent test cũ/namespace sót lại đã được test fail-closed trên Android. Bốn runner root-fixture cũ bị chặn ngay trước mọi mutation và chỉ dẫn sang helper/harness mới; code đọc file cũ cũng đã sửa.

File tạo: `debug/DiagnosticRun.java`, `android/src/debug/AndroidManifest.xml`, `android/src/debug/java/.../DiagnosticLauncher.java`, `tools/android_runtime.py`, `tools/android_baseline_check.py`, `tools/android_upgrade_smoke.py`, `tools/checks/DiagnosticIsolationChecks.java`, `tools/run_studio_task061.ps1`, `tools/run_upgrade_checks.py`. Sửa: MonsterSurvivorGame, GameScreen, AndroidLauncher, SkillDiagnostics/BossDiagnostics/CombatProfile, CameraTrace, AssetVisualCheck và bốn historical Android runners. Layout/resources native UI02 không sửa trong task này.

## 4. Bằng chứng Fresh Start không fixture

Phase0 đã được chạy và xác minh trước implementation upgrades; sau diagnostic/upgrades đã chạy lại cùng kiểm tra trên bản cuối. `android_baseline_check.py`: **38 checks PASS**, Home → GameMode → Start và Home → Start mới. Mỗi lần init log:

```text
diagnostics=none debug=false hp=10/10 damage=1 speed=10 weapons=SWORD:1
fixedSeed=false progress=0.0 elapsed=0.0 dashLevel=1 ultLevel=1 dashCd=0.0 ultCd=15.0
```

Hai seed khác nhau, không42; không LOCAL FIXTURE hoặc Accepted diagnostic. Cả8 legacy trigger files vắng mặt. Preferences không thay đổi. Screenshot actual gameplay cho thấy HP10, một Sword slot, Dash READY/Ultimate countdown và không FPS/HƯỚNG/ANIM. Không dùng ảnh protectedHP để chứng minh baseline.

Evidence cuối trong `core/build/reports/task061/`: `phase0-checks.json`, `phase0-normal.log`, `phase0-home.png`, `phase0-fresh-mode.png`, `phase0-fresh-first-seconds.png`, `phase0-home-start-first-seconds.png`. Capture đợi GL window hiển thị sau Ready vì Ready được log trong constructor trước lần swap đầu tiên.

## 5. Skill Level architecture

GameSession vẫn sở hữu một ActiveSkillSystem, hai View có level độc lập1–5. SkillConfig chứa bảng immutable5 cấp, clone array đầu vào; config/level thuộc trận, không lưu preferences. Getter View chỉ đọc level/tuning/phase/timing. `SkillOffer` immutable snapshot loại skill và from/to level cùng mô tả localized; chỉ commit reward thành công mới gọi mutator package-private của skill system. Không free level theo thời gian/Boss/render.

Cooldown upgrade: `newRemaining = oldRemaining × newTotalCooldown / oldTotalCooldown`. Cửa sổ recharge gauge cũng scale cùng tỷ lệ, giữ progress; initial Ultimate15s thành14s nếu nâng Lv2 ngay đầu, không reset thành28s hoặc cấp READY. READY vẫn READY. Upgrade không đổi active remaining.

Activation giữ riêng snapshot Dash config/level và Ultimate config/level/damage/tâm. Cast đang chạy hoàn thành bằng snapshot cũ, kể cả iframe/distance/radius/knockback/FX; cấu hình mới dùng từ lần activation tiếp theo. Pause giữ level; Retry tạo session mới với cả hai Lv1, Dash READY/Ultimate15s. End/DYING/WINNING clear pending selection/cast như trước.

Sửa `config/SkillConfig.java`, `skill/ActiveSkillSystem.java`, `gameplay/GameSession.java`; tạo `skill/SkillOffer.java`, `skill/SkillSelection.java`. Không thêm subsystem cho từng level, không đổi Player normal movement hoặc WeaponLoadout.

## 6. Bảng Lv1–5 thực tế

| Dash | Cooldown s | Distance | I-frame s | Duration s |
|---|---:|---:|---:|---:|
| Lv1 | 5,0 | 75 | 0,17 | 0,20 |
| Lv2 | 4,6 | 80 | 0,17 | 0,20 |
| Lv3 | 4,2 | 86 | 0,18 | 0,20 |
| Lv4 | 3,8 | 93 | 0,18 | 0,20 |
| Lv5 | 3,5 | 100 | 0,20 | 0,20 |

| Ultimate | Cooldown s | Radius | Damage multiplier | Ordinary knockback |
|---|---:|---:|---:|---:|
| Lv1 | 30 | 105 | 2,5× | 25 |
| Lv2 | 28 | 115 | 2,9× | 27 |
| Lv3 | 26 | 125 | 3,3× | 29 |
| Lv4 | 24 | 135 | 3,7× | 30 |
| Lv5 | 22 | 145 | 4,2× | 33 |

Ultimate initial15s ở Lv1, cast0,35s mọi cấp; damage = max(4, ceil(snapshot base damage × multiplier)), saturate Integer.MAX_VALUE. Tank nhận35% knockback, Boss0. Bảng giữ nguyên đề xuất, không tăng damage/bỏ guard để hỗ trợ test. Với baseDamage1, minDamage4 khiến damage Lv1–4 cùng4; radius/cooldown/knockback vẫn tăng, Lv5 gây5. Pickup Damage vẫn tăng base damage như gameplay cũ.

## 7. Skill Box spawn và pickup

SkillConfig.Boxes: spawn đầu uniform35–45 progress seconds, sau đó40–55, lifetime60 **gameplay seconds**, cap2 còn sống. Random schedule và Random placement riêng, dùng lại PickupSpawner/nav components/collision/caps; không tiêu RNG pool/reward cũ. Mỗi due update thử tối đa một hộp, không catch-up burst. Ordinary boxes giữ cap6 riêng, health drops giữ cap cũ; tổng live pickups vẫn hữu hạn. Không tăng resident enemies/A* budget.

`PickupType.SKILL_BOX` dùng pickup_chest có sẵn, tint turquoise và badge sao bốn cánh từ weapon_pixel. Source: Kenney Tiny Dungeon chest(7,7), license sẵn `assets/game/licenses/Kenney-Tiny-Dungeon.txt`, provenance `PRESENTATION_SOURCES.md`. Không dùng potion, không sửa source asset, không texture mới.

Khi cả hai Lv5, không spawn thêm; hộp còn sống được retire khỏi live list, không mở modal rỗng hoặc tự tặng reward. Sửa `pickup/PickupSystem.java`, `PickupType.java`, `PickupRenderer.java`. Kiểm tra connected ground component và canOccupy hiện có; không flood fill/pathfinding mới lúc spawn.

## 8. Modal/token/commit

Thêm CHOOSING_SKILL và `choosingReward()`; CHOOSING_WEAPON giữ nghĩa cũ. Hai loại modal đều đóng băng simulation, clocks và pickup lifetime. Hộp được giữ pending, chỉ consume khi một offer commit thành công. SkillSelection dùng token âm đơn điệu, WeaponSelection dùng token dương; stale/cross-kind/cross-session/double-tap không trùng token. Offer kiểm tra expected level và cap trước mutation. Failed open không consume item.

Lv5 bị loại khỏi offers; còn một skill thì một card; hai Lv5 không mở selection. Commit đúng một cấp/một hộp, reset input và dropNextDelta. Retry/Home/death/Victory dọn cả hai selections. Weapon Box pool/ba slots/rewards giữ nguyên; regression mới phân biệt cap ordinary và skill.

GameInput modal protocol hiện có được dùng lại: `weaponSelection/weaponToken/weaponOfferCount/chooseWeapon` trên GameScreen route theo phase. Surface `weaponOfferAt/weaponHomeAt` dùng cùng geometry cho hai modal, không thay joystick hoặc hit area nút skill. Audio BGM pause/resume theo `choosingReward()`.

## 9. HUD, VFX và localization

SkillHud hiện tên + Lv1–5, trạng thái/countdown và recharge gauge. Radius30/40, tâm và safe-inset hit test của Task06 giữ nguyên. Font scale0,65 đủ tên có level ở vi/en; GL đo thực tế width trong circle. SkillRewardHud dùng hai card620×76 theo WeaponHud.cardY, title/from→to và hai dòng thông số, body0,75. Không đổi HUD UI02/native layout chính.

GameStrings thêm Skill Box, Upgrade Dash/Ultimate, current/new/max level, title/benefit/iframe/damage/knockback và hint. Cả vi/en được kiểm glyph/NFC/font-fit, không regenerate font hoặc đổi game_language.

Dash streak vẫn tối đa6 điểm, dài theo displacement thật, width tăng nhẹ đến5,4 ở Lv5. Ultimate radius theo activation thật; brightness tăng vừa phải, Lv4–5 thêm một vòng32 segments (tối đa96 segments), hit FX cap12/pool cũ. Không shader/FBO/Texture/Animation mới hoặc object collection mỗi frame. GameAssets vẫn19 textures,44.285.956 bytes RGBA.

Sửa `ui/SkillHud.java`, `ui/GameHud.java`, `skill/SkillRenderer.java`, `localization/GameStrings.java`; tạo `ui/SkillRewardHud.java`.

## 10. Collision, damage và Boss guard

Dash mọi cấp đi qua Player swept movement≤3units; không đổi stats.speed hoặc immunity0,7s sau hit. I-frame là snapshot0,17/0,18/0,20s, không được nối dài bởi upgrade/spam. Kiểm tra trực tiếp iframe timing mọi cấp; regression Task06 dùng contact, Mini Slam, Lich projectile và Lich AoE thật qua Player.damage, không PLAYER_HURT khi bị iframe chặn.

Ultimate mỗi release quét một lần, LOS obstacle hiện có, một Enemy.damage/enemy/token. Boss vẫn qua burst budget20% maxHP/bucket0,2s, không knockback. Ordinary/tank dùng swept knockback, invalidate path, giữ speed và terrain/bounds. Kiểm tra damage/LOS/collision/guard từng cấp, cả4 tổ hợp difficulty/mode; lethal Ultimate tạo đúng một kill/death-roll/Health Drop từng cấp. Không sửa Boss HP, AI, phase, progress300/600 hoặc Victory.

Android: cast Lv3 mở Skill Box giữa ACTIVE, nâng Lv4 rồi release vẫn radius125, hits6/knockbacks4/BossHits2. Lần cast Lv5 sau đó radius145; cumulative counters sau hai cast: impacts2, hits15, kills3, knockbacks8, BossHits4. Hai Boss đều nhận damage theo đường hiện có, không instakill hoặc bị đẩy.

## 11. Audio status

49 lựa chọn cũ và runtime manifest/assets giữ nguyên. SHA-256 `monster-survivor-audio-selections.json`: `0f185d2d5cfba00504e5ccedc55d9e35a63d2887c0d649c0812c77280e8f7cfd`. Ba optional hooks Dash Activate/Ultimate Cast/Ultimate Impact vẫn silent, không tự gán SFX. Skill Box chưa có cue được chọn nên không mượn SFX Weapon Box. Không thêm âm thanh quái hoặc sửa audition selections. AudioChecks190 PASS và Android không Playback failed/FATAL; nghe cảm nhận/cue selection vẫn là bước AUDIO_AUDITION riêng sau này.

## 12. Test results

| Focused suite | Kết quả |
|---|---:|
| DiagnosticIsolationChecks | 20 PASS |
| UpgradeChecks | 1.690 PASS |
| SkillChecks | 1.473 PASS |
| Task04A1Checks | 2.718 PASS |
| WeaponChecks | 223 PASS |
| PickupChecks | 29.227 PASS |
| BossChecks | 2.901 PASS |
| AudioChecks | 190 PASS |
| UpgradeSmoke GL/input | 219 PASS |
| Android normal + isolated upgrades | 43 PASS |
| Android baseline sau diagnostic | 38 PASS |

Số check legacy tăng vì thêm localized glyphs và bounded Skill Box residents; không phải chạy lại82-species stress. UpgradeChecks dùng hộp thật/selection/session commit để lên cấp, phủ initial/running cooldown ratio, ACTIVE snapshot, max/leftovers, stale/cross-kind/Retry token, cap/spawn/accessibility, iframe/collision/damage/LOS/Boss/tank/death-drop mọi cấp. GL smoke phủ tên/mô tả vi/en, actual touch-down/up selection, hidden old root fixtures, explicit namespace, no GL error/extra texture và Retry baseline. GL evidence không được dùng thay Android touch.

Tooling mới: `tools/checks/UpgradeChecks.java`, `tools/smoke/UpgradeSmoke.java`; sửa WeaponChecks/PickupChecks cho cap skill riêng. Touch helper `tools/android/SkillTouch.java` được tái dùng/mở rộng, `tools/build_skill_touch.py` ghi output Task061. Cả4 historical runner đã được kiểm tra từ chối trước mutation (`retired-runners-check.json`). Các log ở `core/build/reports/task061/`.

## 13. Android Studio Run và hai loại test

Pixel_4 emulator-5554, API37/Android17, SwiftShader, landscape2280×1080. Phase0 và bản upgrades đều chạy bằng existing android Run configuration trong Android Studio; Run cuối source18:18:14 (UTC+7), IDE deploy/launch18:18:19. Chỉ compile Java trực tiếp; IDE tự tạo/deploy debug nội bộ cho Run, không export/copy file cài đặt hoặc manual install. Compile core + Android Java: BUILD SUCCESSFUL19s.

**Test1 — không fixture:** Native Home → GameMode Easy/Demo → Start, HP10/Sword1/two skills1/debugfalse/random seed. Demo là lựa chọn chính thức để rút ngắn chờ progress, không đổi skill clock. Box đầu xuất hiện elapsed6,260869/progress41,73913 tại(1512,1496); joystick + Dash thực nhặt hộp. Hai choices, một touch chọn Dash Lv2, double tap không cấp thêm. Accepted reward elapsed8,217291; weapons vẫn SWORD:1. Screenshot HP10, DashLv2/CD4, UltimateLv1/CD7, không overlay. Root fixture files vắng mặt, preferences không ghi.

**Test2 — isolated diagnostic:** One-use run UUID, protectedHP100000/progress600/seed42 có chủ đích; Skill fixture được chọn, Boss/Combat fixtures cùng namespace không được chạy chồng. Dùng actual spawned/scheduled Skill Boxes và collection handler để mở modal, touch để nhận đủ8 upgrades. Khi cap2 đầy, fixture tái dùng hộp đang tồn tại qua collect, không nới cap hoặc cấp level trực tiếp. Arena chỉ đặt một số ordinary targets/Lich trên collider+LOS hợp lệ, không sửa Boss HP/AI; không dùng nó chứng minh Test1. Box-on-cast test giữ Dash80units snapshot khi Lv2→3 và radius125 khi UltimateLv3→4. Lv5 Dash giữ ngón4s (>CD3,5s) chỉ cast một lần, pointer joystick vẫn0. UltimateLv5/R145/Boss, Pause/background, GameOver/Retry và stale consumed-token replay đều qua.

Android touch dùng MotionEvents thực (pointer IDs0/1, POINTER_DOWN/UP) qua InputManager shell → Android/libGDX queue, không gọi tắt activate. Helper dex tạm, không APK. Sau test, namespace owned được gỡ, cả9 preferences/legacy paths unchanged theo bytes/presence; `upgrade-restoration.json`. Replay test Intent bị từ chối dù namespace còn tồn tại; sau cleanup đã lặp Home → Start không fixture38checks PASS. Không dùng ảnh fixture cho trạng thái thường.

Evidence: `upgrade-android-checks.json`, `upgrade-android.log`, `upgrade-touch.log`, `upgrade-restoration.json`; ảnh `upgrade-normal-home/mode/first-seconds/natural-box-modal/earned-lv2`, `upgrade-diagnostic-both-lv3/both-lv5/lv5-after-impact`. Baseline ảnh/log riêng phần4. Historical Android root-fixture runners đã retired; dùng `android_runtime.AutomatedRun`, `android_upgrade_smoke.py` và `android_baseline_check.py` cho test mới.

Đối chiếu cuối sau lượt Start không fixture: preferences và sự vắng mặt của8 trigger files được kiểm tra lại tại `final-normal-state.json`; app ở Home. Đã gỡ đúng helper dex và UI dump tạm khỏi `/data/local/tmp`. IDE evidence lưu riêng `studio-deployment-evidence.log`.

## 14. FPS/frame time

120-frame windows, System.nanoTime + FrameMetrics của project, không GL smoke desktop song song. Hard/Demo, Night, hai Boss. Windows có modal/background được ghi riêng, không dùng làm steady gameplay baseline.

| Scene/window | Ordinary/Boss | FPS | Mean ms | p95 ms | Max ms |
|---|---|---:|---:|---:|---:|
| Trước skill, đầu encounter | 0/2 | 23,62 | 42,33 | 64,14 | 97,07 |
| Sau DashLv2→3 | 0/2 | 24,10 | 41,50 | 60,94 | 80,63 |
| Window chứa release UltimateLv3 và upgrades | 4/2 | 23,29 | 42,94 | 66,42 | 73,89 |
| Lv5/đang combat | 4/2 | 25,37 | 39,42 | 59,40 | 65,20 |
| Trước UltimateLv5 | 7/2 | 24,51 | 40,81 | 60,37 | 73,52 |
| Window chứa release UltimateLv5 | 4/2 | 25,00 | 40,01 | 56,96 | 75,10 |
| Sau release | 4/2 | 24,85 | 40,24 | 65,45 | 88,86 |

Tổng37 active/cast/fade samples: mean43,10ms, max75,10ms. Window Lv5 COMBAT trung bình0,299ms, WEAPON_RENDER1,009ms, HUD3,278ms. Pause/background window có max821,75ms, được giữ trong raw log và không quy thành spike do skill. Window cuối level3/5 không cho thấy sụt nghiêm trọng so encounter trước; đây là probe ngắn có tải host và residents thay đổi, không paired experiment tắt toàn bộ HUD. Không cam kết60FPS hoặc mọi cảnh/thiết bị thật.

## 15. Hạn chế và lỗi đã xử lý

- Test1 đã chứng minh một nâng cấp từ spawn tự nhiên ở gameplay chuẩn. Chưa chơi trọn10phút để đánh giá xác suất người chơi tự thu đủ8 hộp/balance late game; toàn bộ Lv1–5 được xác minh bằng logic/GL và Android diagnostic có khai báo.
- Chưa thử điện thoại vật lý, notch thật hoặc player chạm tay; Android dùng injected touchscreen events thật trên emulator. Safe-inset geometry giữ contract Task06.
- ProtectedHP của diagnostic vẫn có thể làm HP label cũ dài; không hiện trong baseline thường. Các skill name/level/countdown và card descriptions đã đo fit riêng.
- Fixture initial test chạm cap2 khiến request hộp mới bị từ chối; đã sửa fixture tái dùng một hộp thật. Production cap/rewards giữ nguyên. Empty/partial/scalar fixture, legacy files và stale test Intent đều fail closed.
- UI là presentation tối thiểu, chưa polish artwork/audio. Không nâng cap quái/A*, không import FX library, không chạy lại full82 stress hoặc release build. Không có thay đổi đồng thời từ Antigravity theo xác nhận lại của chủ dự án.

## 16. Hợp đồng UI để Antigravity polish

Đọc `session.skills.view(type)`: level(), phase(), tuning(), totalCooldown(), cooldownRemaining(), rechargeProgress(), activeRemaining(), duration(), activationId(). Current tuning/level dùng cho badge/next action; VFX dùng `dashActivationLevel/ultimateActivationLevel`, `ultimateRadius` và trail getters để giữ snapshot đang active.

Modal đọc `session.choosingSkill()`, `skillSelection.token/size/get/pendingId`, SkillOffer.type/fromLevel/toLevel/title/benefit/detail. Commit qua `GameScreen.chooseWeapon(index,token)` (generic existing input router) hoặc guarded `session.chooseSkill`; không gọi mutator hoặc tự tick clock từ draw. Khi render Weapon Box kiểm tra `choosingWeapon()` riêng, không suy từ generic `GameInput.weaponSelection()`.

Nút skill giữ `SkillHud.radius/centerX/centerY/at`, HUD unproject và safe insets. Card/home hit test dùng `WeaponHud.offerAt/homeAt/cardY` chung; nếu đổi layout phải đổi cả draw/hit-test, giữ pointer ownership và token-release semantics. Không mutate level theo render/animation, không chiếm weapon slots hoặc giữ session cũ. Config tables immutable đủ cho polish/balance sau này; reward/weapon evolution/enemy skills ngoài phạm vi task.
