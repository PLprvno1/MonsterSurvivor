# Task 06 — Dash & Ultimate

Ngày: 15/11/2026. Project: `D:\Android\MonsterSurvivor`. Hai kỹ năng cấp cơ bản thuộc từng trận, độc lập với ba Weapon Slots. Không thêm skill rewards, upgrades, evolution hoặc đổi Boss HP/AI. Các thay đổi Task 05/UI02/audio có sẵn trong working tree được giữ lại; không commit/push.

## 1. File tạo/sửa trong Task 06

Đường dẫn Java dưới `core/src/main/java/com/example/monstersurvivor/`:

| Tạo | Vai trò |
|---|---|
| `config/SkillConfig.java` | Immutable timing/range/damage/knockback tuning |
| `skill/SkillType.java`, `skill/ActiveSkillSystem.java` | Session state, activation, clocks, one-shot resolution |
| `skill/SkillRenderer.java` | Bounded streak/ring geometry từ texture hiện có |
| `ui/SkillHud.java` | Hai nút, countdown, progress, disabled state, hit test |
| `debug/SkillDiagnostics.java` | Fixture opt-in, placement, status và frame metrics trên Android |

Sửa: `config/GameConfig.java`, `entity/Player.java`, `ai/EnemySystem.java`, `gameplay/GameSession.java`, `input/GameInput.java`, `screen/GameScreen.java`, `ui/GameHud.java`, `localization/GameStrings.java`, `audio/AudioEvent.java`, `audio/GameAudioManager.java`, `debug/BossDiagnostics.java`, `debug/CombatProfile.java`. Các config-copy diagnostics giữ `skills` của config nguồn; Boss death fixture tôn trọng cả Dash immunity.

Verification mới: `tools/checks/SkillChecks.java`, `tools/smoke/SkillSmoke.java`, `tools/run_skill_checks.py`, `tools/run_studio_task06.ps1`, `tools/android/SkillTouch.java`, `tools/build_skill_touch.py`, `tools/android_skill_smoke.py`. Báo cáo này và phần Task 06 nối thêm vào `INTEGRATION_NOTES.md`. Không sửa GDD, báo cáo cũ, native XML/activity hoặc registry trong Task 06. Không import asset mới.

## 2. ActiveSkill architecture

`GameSession` tạo và sở hữu `skills`. `SkillConfig` nằm trong config của session, các giá trị immutable; không tạo level giả. `ActiveSkillSystem` không giữ Player/Session/Boss reference và không phụ thuộc Gdx/render/filesystem. `activate`, `advance`, `resolve`, `cancel`, `reset` tách thao tác nhận input, di chuyển/clock, damage, cleanup. State có READY/ACTIVE/COOLDOWN, remaining/total/duration và token tăng đơn điệu. Lệnh bị từ chối không tạo token, không khởi động cooldown.

`View` có getter chỉ đọc. Ultimate pending impact vẫn ACTIVE cho đến khi resolve, kể cả modal xuất hiện ở frame kết thúc cast. `impactedId` chặn release lặp; không lưu danh sách target lâu dài. Retry gọi `end()` session cũ rồi tạo session mới, không tái sử dụng state. HUD chỉ cache session ID/layout, renderer nhận state mỗi draw.

## 3. Dash config và hướng

| Thông số | Giá trị |
|---|---:|
| Cooldown / ban đầu | 5s / READY |
| Duration | 0,20s |
| Khoảng cách khi không bị chặn | 75 world units |
| Tốc độ Dash | 375 units/s |
| I-frame | 0,17s đầu |

So với Speed10 = 80 units/s, Dash nhanh khoảng 4,69 lần. Không tiêu HP hoặc sửa stats.speed. Vector input được normalize và snapshot một lần; không đổi hướng theo drag trong Dash. Không input: hướng dịch chuyển thường thực sự được chấp nhận gần nhất, sau đó fallback facing ban đầu. Dash giữ action clock và Sword windup/aim; auto-target vẫn do weapon system xử lý bình thường.

## 4. Collision và immunity

Tách hàm swept axis movement có sẵn của Player và dùng lại cho Dash, substep tối đa 3 units. Mỗi trục kiểm tra `CollisionGrid.canOccupy` với collider Player 4×3. Không teleport endpoint, không đổi walkability; có thể trượt cạnh vật cản. Các accepted segments vẫn ghi vào buffer tái sử dụng để pickup sweep không mất item nằm trên đường lướt. Phần thời gian Dash thay thế normal displacement; phần thời gian dư trong frame trở lại normal movement.

`Player.invulnerable()` kết hợp hai nguồn độc lập: immunity sau mất HP 0,7s và supplier Dash iframe. `damage()` từ chối trước mutation/PLAYER_HURT. Không kéo dài immunity 0,7s khi skill chặn hit. Enemy contact, Mini Slam, Lich projectile và Lich AoE đều đi qua đường damage này. Cancel/terminal xóa Dash iframe; spam không nối dài miễn thương.

## 5. Ultimate config/damage/knockback

| Thông số | Giá trị |
|---|---:|
| Cooldown / ban đầu | 30s / 15s |
| Cast | 0,35s |
| Radius | 105 units |
| Damage | max(4, ceil(snapshot PlayerStats.damage × 2,5)) |
| Knockback ordinary / tank | 25 / 8,75 units |
| Boss knockback | 0 |
| Hit FX tối đa / activation | 12, trong pool hiện có |

Snapshot vị trí tâm và damage khi nhận cast. Damage tại lúc cast kết thúc, một lần/enemy trong bán kính, không tăng theo frame VFX. Số lớn được clamp vào int, tránh overflow. Không projectile mới, không DoT. Người chơi vẫn di chuyển/auto-attack trong cast; vòng năng lượng giữ tâm snapshot.

## 6. Combat với enemy/Boss

Một lần quét residents ở release, dùng `CollisionGrid.clearLine(...,.5,.5,true)` như LOS của combat hiện có: obstacle cứng chặn, nước không chặn tia. Không thêm pathfinding. Ring VFX có thể vẽ vượt vật cản nhưng target phía sau vẫn không bị damage.

Gọi `Enemy.damage`, giữ nguyên death hook kills/Boss progression/Health Drops. Boss vẫn qua burst budget 20% maxHP mỗi bucket 0,2s của Task 05; cả Mini và Final nhận skill damage và không bị knockback. Final defeat vẫn đi qua WINNING → Death → VICTORY hiện có. Không thay điều kiện Victory hoặc HP/AI.

Ordinary sống sót được đẩy bằng swept move hiện có của EnemySystem; nước/tường/bounds/collider theo navigation của từng actor. Clear path/index/direct/stuck và yêu cầu AI tìm đường lại, không sửa speed. Center trùng target dùng hướng +X xác định. Không tạo trạng thái knockback kéo dài hoặc collection không giới hạn.

## 7. Mobile buttons

Hai nút tròn bên phải, Dash radius30, Ultimate40 HUD units. Với viewport Pixel_4 2280×1080 → 1013,33×480: tâm physical gần Dash(1920,716), Ultimate(2127,675). Dùng safe inset right/bottom từ libGDX, chia theo scale HUD. Layout nằm trên Weapon Slots và debug controls, dưới Pause, ngoài vùng joystick44%. Không đổi bố cục HUD UI02 chính. Geometry draw/hit test dùng cùng `SkillHud`.

## 8. Pointer ownership/multitouch

GameInput tách owner joystick, Dash, Ultimate và modal; kiểm tra duplicate pointer ID. Nút skill chỉ gọi activate trên touch-down, không kích hoạt từ drag/hold/render. Release/cancel skill không release joystick; drag joystick sang nút vẫn thuộc joystick. Cooldown/phase guard chặn mọi duplicate action. Modal/Pause/UI được xử lý trước gameplay.

Shift trái/phải → Dash, Space → Ultimate; latch riêng chặn OS key-repeat, clear khi key-up. WASD/arrows giữ logic cũ. Reset lifecycle/modal/Pause xóa pointer/movement; không chuyển một ngón đang giữ thành press mới sau Resume. Keyboard latches giữ trạng thái đến key-up để không tự cast vì key-repeat.

## 9. Cooldown, Pause/lifecycle

Skill clock dùng cùng delta gameplay RUNNING với elapsed, không nhân Demo progressScale. Session giữ clamp delta tối đa0,1s hiện có. Pause, background, modal và terminal không tick skill; active cast/Dash freeze rồi tiếp tục khi gameplay Resume. DYING/WINNING/end hủy pending impact, iframe và VFX. GameScreen chặn activation khi disposed, exhibit, mất audio focus hoặc paused; focus gain/background Resume vẫn cần explicit Resume. Retry reset Dash READY, Ultimate15s và các counters/token state.

## 10. VFX và tài nguyên

Đã khảo sát `asset_sources/MonstersCreatures_v1.0/MonstersCreatures/03_Projectiles_FX/Combat_FX`, gồm shockwave và ground quake. Chọn geometry với texel `weapon_pixel` hiện có để tránh thêm texture/spritesheet. Dash tối đa6 accepted-position streaks, fade0,14s. Ultimate hai vòng32 segments, flash tâm, fade0,28s; chỉ hit FX dùng pool projectile hiện có, cap12/activation. Culling theo camera, dùng lại SpriteBatch/pass weapon FX và phục hồi packed color.

Không Texture/Animation/FBO/shader mới, không cấp phát per-frame trong skill simulation/renderer. HUD cache GlyphLayout và chỉ cập nhật text khi trạng thái/countdown đổi. Resident cap40/60 và Boss budget2 không đổi. GameAssets vẫn19 textures, 44.285.956 byte RGBA như trước.

## 11. Audio hooks và cue còn thiếu

Thêm optional IDs `SKILL_DASH_ACTIVATE`, `SKILL_ULTIMATE_CAST`, `SKILL_ULTIMATE_IMPACT`. Phát đúng ở accepted activation / release, không từ HUD hoặc animation. AudioManager cho phép ba ID này thiếu mapping và trả về an toàn; 49 ID cũ vẫn bắt buộc. Reset/limiter null-safe. Không chọn fallback hoặc tái gán SFX vũ khí: ba cue kỹ năng hiện im lặng có chủ đích.

Chờ người dùng chọn qua AUDIO_AUDITION ở task sau: Dash whoosh ngắn; Ultimate charge/cast khoảng0,35s; energy impact rõ nhưng không nhầm Fireball/Lightning. Chỉ sau lựa chọn mới thêm mapping/config/audio asset. Task này không yêu cầu tải thêm hoặc duyệt cue ngay.

`import_selected_audio.py --check`: 49 events,36 unique files,36/36 decode OK,8.555.024 bytes, source_unchanged=true. SHA-256 JSON gốc: `0f185d2d5cfba00504e5ccedc55d9e35a63d2887c0d649c0812c77280e8f7cfd`. Runtime manifest/original selections không sửa.

## 12. Localization

Thêm GameStrings vi/en cho Dash/Lướt, Ultimate/Tuyệt Kỹ, Energy Burst/Sóng Năng Lượng, Ready, Cooldown, Active, Unavailable, Locked và mô tả Shift/Space. Gameplay không hardcode nhãn tiếng Anh. Font hiện có đủ toàn bộ glyph NFC, không thêm font/texture. Scale0,65 đã đo label fit trong vòng tròn cả vi/en. Không thêm hoặc đổi preference key `game_language`; fixture lưu/khôi phục giá trị gốc.

## 13. Test results

| Suite | Kết quả |
|---|---:|
| SkillChecks | 839 assertions PASS |
| SkillSmoke desktop GL/input/OpenAL | 328 checks PASS |
| Task04A1Checks joystick/localization | 2.658 PASS |
| WeaponChecks | 223 PASS |
| PickupChecks | 21.438 PASS |
| BossChecks | 2.901 PASS |
| AudioChecks | 190 PASS |
| Android native/menu/two-pointer/gameplay | 24 PASS |
| Android nonzero cooldown Pause/background probe | 4 PASS |

SkillChecks phủ8 hướng/fallback/snapshot, quãng đường/cooldown/không sửa speed hoặc aim; tree/rock/water/boundary và frame0,02/0,05/0,1/0,5s; iframe với contact/Slam/projectile/AoE thật; Ultimate initial/cast/one-hit/LOS/damage snapshot/knockback/bounds/tank/Boss guard bốn tổ hợp mode/difficulty; Final death/kill/drop một lần; modal/Pause/terminal/Retry; pointer duplicate/release/cancel, keyboard repeat và font.

GL smoke dùng GameScreen/GameInput thật, chụp HUD/Dash/Energy/Modal vi/en; freeze cast giữa Weapon Box modal, tiếp tục release đúng một lần; kiểm tra GL error, texture count, GameOver/Retry/Home và dispose idempotent. Đây là evidence desktop riêng, không thay thế Android touch. Registry không đổi nên không chạy lại toàn bộ82-species stress suite.

Compile trực tiếp chỉ `:core:compileJava :android:compileDebugJavaWithJavac`: BUILD SUCCESSFUL. Log/evidence tại `core/build/reports/task06/`. Android kết quả chi tiết ở phần14–16.

## 14. Android Studio Run

Pixel_4 emulator-5554, Android17/API37, SwiftShader, landscape2280×1080. Chạy existing `android` configuration bằng Android Studio Run (Shift+F10), không cài APK thủ công. IDE tự thực hiện build/deploy debug nội bộ theo workflow Run; không export/copy APK/AAB để bàn giao, không release/signing/publishing. `studio-run.log` ghi các lần yêu cầu Run; runtime LOCAL FIXTURE/Skills logs xác nhận source kỹ năng thật.

Android input evidence dùng shell-UID `InputManager` MotionEvents thực với pointer IDs0/1, ACTION_POINTER_DOWN/UP, qua Android/libGDX input queue. `SkillTouch` biên dịch thành dex helper tạm trong `/data/local/tmp`, không APK và không gọi tắt API activate của game. Ngón trái giữ joystick khi ngón phải nhấn skill; giữ Dash hơn6,2s để kiểm tra không auto-spam qua cooldown5s.

Fixture opt-in `skill-test.json`: protectedHP100000, seed42, progress600 để có Boss mà không chờ10phút; arena đặt tối đa4 rock golem và đưa Lich sống tới vị trí có collider/LOS hợp lệ. Không sửa skill timing/damage/radius/Boss HP/AI; initial cooldown vẫn chờ15 gameplay seconds tự nhiên. Modal mở qua Weapon Box thật; GameOver qua `Player.damage` sau khi immunity thật hết. Không có fixture thì không tạo/poll SkillDiagnostics. Preferences và diagnostic files gốc được lưu/khôi phục trong finally; kết thúc ở Home.

Lượt cuối dùng Studio Run 16:37:37 (UTC+7), IDE deploy/launch Home lúc16:37:44, sau đó native Easy/Normal/vi và Hard/Demo/en. 24 checks trong `android-checks.json` PASS: hai ngón, hold6,2s, drag qua button, Pause, background, modal/commit Bow, initial15s, Ultimate cooldown30s, Boss/knockback, GameOver/Retry/Home và optional audio an toàn. Runtime Ultimate lúc elapsed19,660s, release20,038s: `impacts=1 hits=6 kills=0 knockbacks=4 bossHits=2`. Hai Boss thật đều sống sau hit, không bị đẩy. Retry trả Dash count0/Ultimate count0, UltimateCD14,029s ở lần đọc đầu sau khoảng0,971s gameplay. Native language/mode/difficulty bridge vẫn đúng. Không FATAL EXCEPTION/Playback failed trong lượt xác nhận cuối; preferences/fixtures được khôi phục.

Screenshots: `android-skills-ready-vi.png`, `android-skills-after-modal.png`, `android-ultimate-after-en.png` trong evidence folder. HUD HP100000 của fixture tràn nhãn HP/kills cũ; đây là giá trị hỗ trợ test, không phải thay đổi UI/tuning thường. Screenshot có overlay công cụ emulator ở phía trái, không phải HUD game mới.

Probe bổ sung `python tools/android_skill_smoke.py --cooldown-pause`: 4 checks PASS, gồm native launch. Sau Dash, Pause tại elapsed2,8716815s với DashCD3,6106863s và UltimateCD12,128319s. Sau chờ Pause rồi background/foreground, cả elapsed và hai cooldown giữ nguyên chính xác; explicit Resume hoạt động. Evidence riêng trong `android-cooldown-pause/`, tránh dùng cooldown0 làm bằng chứng freeze. Đối chiếu cuối `android-restore-verified.json`: cả7 preferences/fixture paths khớp bản gốc, helper dex/window XML tạm đã gỡ khỏi device, app ở Home.

## 15. FPS/frame time

Đo 120-frame windows bằng System.nanoTime và CPU FrameMetrics của project, có sample active skill/VFX riêng. Không chạy desktop GL song song trong lượt Android cuối. Không suy ra60FPS hoặc hiệu năng thiết bị thật. Window chứa UI automation/background phải được phân biệt với cảnh combat.

| Hard Demo, Night, hai Boss | Residents thường | FPS | Mean ms | p95 ms | Max ms |
|---|---:|---:|---:|---:|---:|
| Trước arena, không skill (window thứ2) | 0 | 16,23 | 61,63 | 109,07 | 223,60 |
| Arena trước cast | 4 | 19,52 | 51,23 | 85,85 | 114,75 |
| Window chứa Ultimate/các hit | 4 | 19,35 | 51,67 | 88,36 | 97,30 |
| Sau cast | 4 | 20,61 | 48,52 | 78,23 | 114,44 |
| Sau cast, trước GameOver | 4 | 18,98 | 52,69 | 91,02 | 131,03 |

Ultimate có11 sample active/cast/fade: mean57,71ms, max87,04ms. CPU COMBAT trung bình window trước cast0,339ms và chứa cast0,283ms; WORLD_RENDER18,91/19,18ms, HUD5,62/6,00ms. Đây là CPU trung bình cả window, không phải đo riêng chính xác release frame. Window cast không có max spike vượt window ngay trước; không thấy sự sụt nghiêm trọng gắn với activation trong mẫu ngắn. Baseline host lượt này thấp hơn mốc20–28 cũ ngay cả trước skill. Không có paired build tắt toàn bộ skill HUD nên không kết luận nhân quả về overhead HUD, không bảo đảm mọi scene; tối ưu renderer/UI tổng thể vẫn là việc riêng nếu cần. Không benchmark62residents hoặc thiết bị thật trong task này.

## 16. Lỗi đã xử lý và hạn chế

- Font0,75 làm nhãn ACTIVE tiếng Việt vượt bề ngang nút; giảm0,65 và rerun GL fit checks.
- Fixture modal ban đầu ở Demo chạm boxCap6; chuyển modal case sang Easy Normal, Boss case sang Hard Demo. Gameplay cap/rewards không đổi.
- Fixture command ghi trực tiếp từng gây đọc file rỗng/JSON dở dang sau Resume. Đã đổi write sang atomic replace và reader bỏ qua empty/partial command; không phải lỗi cooldown hoặc production gameplay (fixture mặc định vắng mặt). Lượt Android cuối kiểm tra lại regression này.
- UI là baseline geometry; Antigravity có thể thay mỹ thuật. Ultimate ring là báo cast + release radius, damage xảy ra đồng loạt cuối cast, không theo thời điểm từng đoạn vòng chạm enemy.
- Chưa audition ba skill cue, chưa thử ngón tay vật lý trên điện thoại thật. Không tuyên bố nghe đủ49 cue từ API playback. Các test Android Boss có hỗ trợ placement/protectedHP/progress đã công bố, không phải run cân bằng tự nhiên10phút hoặc stress62residents.

## 17. Hợp đồng UI dành cho Antigravity

Đọc `session.skills.view(SkillType)` qua `phase()`, `cooldownRemaining()`, `totalCooldown()`, `activeRemaining()`, `duration()`, `rechargeProgress()`, `activationId()`; `config()` cung cấp tuning immutable. `rechargeProgress()` dùng cửa sổ15s ban đầu/30s sau activation cho Ultimate. Không tự tick clock trong UI.

Input gọi `GameScreen.activateSkill(type,axisX,axisY)` → session guard. Geometry mới phải cập nhật cùng draw/hit test ở `SkillHud.radius/centerX/centerY/at`; `GameHud.skillAt` nhận HUD units sau unproject. Enabled chỉ khi RUNNING/alive/không Pause; locked state phải phản ánh modal/terminal. Không mutate View, Player speed, WeaponLoadout hoặc gọi damage từ UI. World VFX đọc getter `trail*`/`ultimate*`, không giữ session cũ; widget cache invalidate theo session.id. Giữ pointer ownership/one-touch-down semantics nếu đổi hình nút.

## 18. Đề xuất Task 06.1

Dùng level table1–5 tạo `SkillConfig.Dash/Ultimate` mới thuộc session, với cooldown/distance/radius/damage/FX giới hạn rõ; xác định cách xử lý cooldown đang chạy khi nâng cấp. Sau đó mới thêm reward skill bằng contract modal/token riêng hoặc mở rộng reward rõ ràng, không chiếm Weapon Slots. Cần test burst guard/late-game balance và effect caps mỗi level, chọn ba cue qua AUDIO_AUDITION, rồi polish UI Antigravity. Task 06 chưa thêm các tính năng này.
