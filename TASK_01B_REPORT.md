# TASK 01B – Asset integration & gameplay foundation

## Kết quả

Đã triển khai GameScreen chạy offline trên Desktop và Android: map rừng 100×100 tile, nhân vật Kenney, Idle/Walk theo 4 hướng, joystick 360°, WASD/phím mũi tên, camera bám và giới hạn biên, collision chân với cây/đá/nước, HUD HP 10/10 + timer + FPS, Pause/Resume và xử lý Home/quay lại ứng dụng.

Đã tích hợp đủ 9 quái thường và Lich Lord. Cảnh mặc định trưng bày 6 sprite cố định: Honey Slime, Wild Boar, Giant Bee, Red Cap, Living Armor và Lich Lord. Nút ANIM/DIR hoặc F2/F3 kiểm tra 5 state cơ bản và 4 hướng; chúng chỉ điều khiển preview. Chưa có combat, damage, nâng cấp, spawn tự động, A*, boss AI hoặc ngày/đêm.

## Lựa chọn nhân vật và giấy phép

Không dùng Iron Knight. File `asset_sources/N3XUS-Starter-FREE-v1.1/N3XUS-Starter-FREE-v1.1/LICENSE.txt` có điều khoản cấm dùng art cho AI, bao gồm “or as prompts for them”. Theo yêu cầu Task 01B, trong task này chỉ đọc văn bản license; không đọc/chuyển đổi sprite N3XUS, không đưa chúng vào runtime.

Nhân vật thay thế lấy từ `asset_sources/kenney_roguelike-characters/Spritesheet/roguelikeChar_transparent.png`: ô cột 0, hàng 11, tọa độ PNG `(0,187,16,16)`. License đi kèm là CC0. Sheet gốc chỉ có pose tĩnh: bốn hướng và bước chân trong `player.png` là **biến thể tạo bằng code cho project**, gồm mirror/profile, mặt sau helmet/cape, bob và chuyển động chân. Không tuyên bố đây là bộ walk có sẵn của Kenney. Đây là art tạm, có thể thay bằng sheet top-down hoàn chỉnh qua catalog.

Environment dùng Kenney Roguelike/RPG Pack, theo `License.txt` CC0 trong gói. Quái dùng Willosuna Free Demo theo `asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/LICENSE.txt`: cho phép dùng/chỉnh sửa trong game cá nhân/thương mại, credit không bắt buộc, cấm phân phối lại như asset pack hoặc nhận là tác giả. Không lấy hero/map demo ngoài phạm vi license. Runtime giữ nguyên văn 3 license và có credit Kenney/Willosuna. Không commit/push source hoặc art.

## Asset runtime và mapping

Toàn bộ `assets/` có **19 file, 496.932 byte** tại thời điểm kiểm tra. Có **12 PNG**, khoảng **11,77 MiB RGBA** khi nạp, chưa tính overhead/driver/font. Không chứa preview, x2/x4, frame PNG rời, N3XUS, hero/map demo hoặc JSON nguồn không dùng. JSON gốc ở lại `asset_sources/`; `catalog.json` chứa metadata đã chuyển đổi cần thiết.

| File runtime | Nội dung |
|---|---|
| `assets/assets.txt` | Danh mục được task Gradle hiện có sinh lại |
| `assets/game/catalog.json` | Rect frame, FPS, loop, state/hướng, foot pivot và region atlas |
| `assets/game/forest.json` | Map, terrain, props, collision/navigation grid, spawn, seed |
| `assets/game/world.png` | Atlas 128×64 từ các vùng Kenney đã chọn |
| `assets/game/player.png` | Sheet 64×64, cell 16×16; bốn hàng down/left/right/up |
| `assets/game/monsters/honey_slime.png` | Sheet x1 192×640 |
| `assets/game/monsters/wild_boar.png` | Sheet x1 192×640 |
| `assets/game/monsters/giant_bee.png` | Sheet x1 192×640 |
| `assets/game/monsters/red_cap.png` | Sheet x1 192×640 |
| `assets/game/monsters/red_crab.png` | Sheet x1 192×640 |
| `assets/game/monsters/giant_frog.png` | Sheet x1 192×640 |
| `assets/game/monsters/rock_golem.png` | Sheet x1 192×640 |
| `assets/game/monsters/living_armor.png` | Sheet x1 192×640 |
| `assets/game/monsters/bone_hound.png` | Sheet x1 192×640 |
| `assets/game/monsters/lich_lord.png` | Sheet x1 640×3072 |
| `assets/game/licenses/Kenney-Characters.txt` | License gốc nhân vật |
| `assets/game/licenses/Kenney-RPG.txt` | License gốc environment |
| `assets/game/licenses/Willosuna-Free-Demo.txt` | License gốc quái |
| `assets/game/licenses/CREDITS.txt` | Nguồn art và ghi chú biến thể nhân vật |

Quái gốc nằm dưới `asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/willosuna_rpg_monsters/x1/`. Pipeline đọc `_sheet.json` và `manifest.json`, đối chiếu số frame/tag/FPS/loop, tọa độ không vượt PNG và điều kiện không trim/rotate. PNG runtime giống byte-for-byte với sheet x1 đã chọn; kiểm tra bằng SHA-256. Loader tạo sẵn **236 clip**, gồm 8 clip player và 228 clip state/hướng của quái.

`I/W/A/H/D` dưới đây là idle/walk/attack/hurt/death; số frame tính **mỗi hướng**, tất cả có 4 hướng down/left/right/up.

| Sprite / nguồn tương đối trong x1 | Frame cell | I/W/A/H/D | FPS tương ứng |
|---|---|---|---|
| Player Kenney dẫn xuất, `assets/game/player.png` | 16×16 | 1/4/không có/không có/không có | idle/walk 8 |
| `meadow/honey_slime/honey_slime_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `meadow/wild_boar/wild_boar_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `meadow/giant_bee/giant_bee_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `forest/red_cap/red_cap_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `river/red_crab/red_crab_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `river/giant_frog/giant_frog_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `mountain/rock_golem/rock_golem_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `castle/living_armor/living_armor_sheet.png` | 32×32 | 6/6/6/3/6 | 6/10/14/10/9 |
| `ruins/bone_hound/bone_hound_sheet.png` | 32×32 | 4/6/5/3/5 | 6/10/12/10/9 |
| `bosses/lich_lord/lich_lord_sheet.png` | 64×64 | 6/6/6/3/6 | 6/10/14/10/9 |

Lich Lord còn được nạp run 6@14fps, special 6@11, spawn 7@10, stun 6@8, dissolve 7@10, entrance 10@10, slam 8@10, mỗi state có 4 hướng. Không đóng gói phase 2 hoặc các sheet `_v2` riêng của quái thường. Idle/walk lặp; attack/hurt/death giữ frame cuối theo metadata. State thiếu ở player fallback về idle; không tạo animation giả cho combat.

Atlas environment lấy từ `asset_sources/kenney_roguelike-rpg-pack/Spritesheet/roguelikeSheet_transparent.png`, cell 16×16, spacing 1 px. Tọa độ sau là cột/hàng từ góc trái trên: grass `(5,0)`, dirt `(6,0)`, water `(0,0)`, shore `(2..4,0..2)` trừ cell giữa; tree `(13,10)+(13,11)`, pine `(16,10)+(16,11)`, bush `(19,9)`, rock `(54,21)`, stump `(53,20)`. Hai cell thân cây được ghép bỏ khoảng cách giữa tile.

## World, input và tài nguyên

- Map JSON tạo xác định với seed 10101: 100×100 tile, tile 16, world 1600×1600, spawn `(808,808)`. Đồng cỏ trung tâm hình ellipse bán kính khoảng 13×9 tile; ba hồ, đường nối bốn phía, cây/đá/bụi/gốc cây bên ngoài. Có 2.394 props cản. Offline flood-fill bảo đảm toàn bộ **7.203 ô đi được** nối tới spawn.
- Collision y-up, row-major, byte mask `TREE=1`, `ROCK=2`, `WATER=4`, `BOUNDARY=8`. `index`, `mask`, `isWalkable` sẵn cho graph A* về sau. Chưa chạy A* trong game.
- Player dùng foot box 8×6 world units; tốc độ nền 80 units/s, chuẩn hóa input chéo. Di chuyển chia bước tối đa 3 units và giải collision từng trục để trượt theo vách. Delta tối đa 0,1s khi game bị stall; bỏ delta đầu sau resume.
- World viewport ExtendViewport tối thiểu 320×240, tối đa bằng map; camera clamp bốn biên, kể cả cửa sổ có tỷ lệ cực rộng. Ground chỉ render tile nhìn thấy; props/actors được cull và sắp theo tọa độ chân để che khuất hợp lý.
- Joystick quản lý pointer sở hữu riêng, clamp bán kính, có deadzone và reset khi pause/resize/hide. Input và HUD dùng viewport riêng để quy đổi touch đúng theo độ phân giải.
- `GameAssets` sở hữu texture qua AssetManager, nạp một lần và tạo sẵn Animation/TextureRegion. Texture dùng Nearest; render không đọc file hoặc tạo Animation. Queue/vector/layout được tái dùng; timer/FPS chỉ cập nhật text khi cần. Screen dispose texture, batch, font, ShapeRenderer; packaged desktop được đóng bình thường trong smoke test.
- Pause chủ động và lifecycle dùng cùng owner GameScreen. Về từ Home vẫn paused, cần Resume; joystick/keyboard được reset để tránh kẹt điều khiển. Quái preview không có collision với player hoặc AI.

## File tạo/sửa

Trong `core/src/main/java/com/example/monstersurvivor/`:

| File | Thay đổi |
|---|---|
| `MonsterSurvivorGame.java` | Chuyển starter sang Game + GameScreen, quản lý dispose |
| `assets/GameAssets.java` | Loader texture, region và clip tập trung |
| `entity/Player.java` | Facing, Idle/Walk, di chuyển/collision |
| `input/GameInput.java` | Joystick đa chạm, keyboard, pause/preview commands |
| `render/WorldSprite.java` | Sprite theo foot pivot, cull, draw, comparator |
| `screen/GameScreen.java` | Render/update/lifecycle, camera, preview cố định |
| `ui/GameHud.java` | HUD, joystick, nút preview, Pause/Resume tạm |
| `world/CollisionGrid.java` | Grid collision/navigation bất biến |
| `world/ForestMap.java` | Đọc map, terrain/props và render phần nhìn thấy |

Thêm bộ kiểm tra độc lập `tools/checks/FoundationChecks.java`; các file hỗ trợ `tools/PrepareKenney.java`, `tools/prepare_assets.py`, `tools/run_checks.py`, `tools/smoke/SmokeLauncher.java`, `tools/android_smoke.py`, `tools/desktop_jar_smoke.py`. Runtime được liệt kê đầy đủ phía trên; bỏ logo starter `assets/libgdx.png`, sinh lại `assets/assets.txt` bằng task sẵn có. Không sửa Gradle, Manifest, AndroidLauncher, Android XML hay module native UI.

## Kết quả build/test đã thực hiện

Ngày thực hiện: 13/11/2026. Dùng Java 17 và dependency project hiện tại, build `--offline`.

| Kiểm tra | Kết quả và bằng chứng |
|---|---|
| `gradlew.bat :lwjgl3:jar :android:assembleDebug --offline` | **PASS**, exit 0. Log `core/build/reports/task01b-build.log`. Có warning SDK XML v3/v4 của toolchain hiện có; không chặn build. |
| Desktop JAR | `lwjgl3/build/libs/MonsterSurvivor-1.0.0.jar` (20.995.784 byte). Chạy `java -jar`, mở GameScreen từ resource đóng gói, đóng cửa sổ bình thường, exit 0. `core/build/reports/task01b/desktop-jar.json` và `.log`. |
| Android debug APK | `android/build/outputs/apk/debug/android-debug.apk` (7.363.035 byte). Cài `adb install -r`, cold launch thành công. ZIP của APK xác nhận chỉ có 12 PNG asset đã chọn. |
| Metadata/runtime assets | **PASS**: hash 10 PNG, count/FPS/loop của mọi animation đã nạp đối chiếu manifest; báo cáo `core/build/reports/task01b/assets-verified.json`. |
| Logic không cần GL | **PASS, 280 assertions**: FPS 30/60/120, chuẩn hóa chéo, chia bước chống tunneling, trượt vách, bốn mask cản, boundary, camera clamp, kích thước map, spawn, clearing và toàn bộ navigation cells liên thông. `core/build/reports/task01b/foundation.txt`. |
| Task Gradle test mặc định | `:core:cleanCompileTestJava :core:test --offline` exit 0 sau khi đưa helper độc lập ra `tools/checks`. Không có framework test trong source set này; 280 assertions được thực thi bằng Java helper, không được tính là kết quả của task Gradle. Log `core/build/reports/task01b-core-test-task.log`. |
| Desktop OpenGL | **PASS, 547 checks**: nạp 10 bộ quái, core clip/cell/final frame, inject joystick/keyboard, pause/lifecycle, collision trên cây/đá/nước thật, bốn biên/camera, resize và tỷ lệ cực rộng, 5 state preview/4 hướng, render 60 exhibits. `core/build/reports/task01b/desktop-smoke.txt`. |
| Android runtime | **PASS, 15 assertions** qua ADB trên AVD **Pixel_4**, model `sdk_gphone16k_x86_64`, API **37**, landscape 2280×1080. Touch pause/resume, joystick, Home/quay lại, timer ngừng/tiếp tục, không kẹt input và không thấy crash/missing-asset exception trong log đã thu thập. `core/build/reports/task01b/android-smoke.json`, `android-events.txt`, `android-logcat.txt`. |

Android lượt cuối: joystick đưa player từ `(808,808)` đến `(885.22485,808)`. Timer trước Home và tại explicit Resume cùng bằng `5.263892s`; sau resume tiếp tục tăng, vị trí không tự trôi. Đây là kiểm tra touch tự động qua ADB và xem ảnh, không phải playtest bằng tay trên điện thoại thật.

Bộ kiểm tra Java có `main()` chạy qua `tools/run_checks.py`, không dùng JUnit. Đã sửa việc đặt helper ban đầu trong `core/src/test` khiến Gradle báo không tìm thấy test theo framework; chuyển sang `tools/checks` để task test mặc định giữ hành vi không có test source như ban đầu, không sửa Gradle hoặc thêm test dependency.

Hiệu năng Desktop: 60 sprite animate đứng tại chỗ, 180 frame thực ở 640×480, **58,76 FPS** toàn lượt; thời gian render CPU/driver trung bình khoảng **0,226ms**, 62 world draw calls/frame. Đây là benchmark render, không có AI/combat và không đại diện hiệu năng Android.

Android cảnh mặc định 6 preview: các snapshot lượt cuối là **37/38/36 FPS** khi hoạt động/đã ổn định; getter FPS ngay sau Home/resume cho **16** ở mẫu đầu. Khoảng đo FPS quanh lifecycle có thể gồm thời gian ngừng render, nên không dùng mẫu đó để kết luận frametime ổn định. **Chưa xác nhận 60 FPS hoặc 60 quái trên Android**, chưa đo percentile/long-run hay physical device.

Ảnh đã tạo và xem gồm `core/build/reports/task01b/desktop-start.png`, `desktop-animation-*.png`, `desktop-paused.png`, `desktop-resized.png`, `desktop-60-preview.png`, `android-start.png`, `android-walk-preview.png`, `android-attack-preview.png`, `android-after-joystick.png`, `android-lifecycle-resume.png`. Không tuyên bố đã kiểm tra process-death restore hoặc GPU context loss.

## Hướng dẫn chạy

Từ root project, Java 17 và Python 3 có trong PATH:

```powershell
# Chỉ khi muốn tạo lại asset runtime từ nguồn có sẵn:
python tools/prepare_assets.py

# Build hai nền tảng bằng dependency đã cache:
.\gradlew.bat :lwjgl3:jar :android:assembleDebug --offline

# Desktop, dùng một trong hai cách:
.\gradlew.bat :lwjgl3:run --offline
java -jar lwjgl3/build/libs/MonsterSurvivor-1.0.0.jar

# Kiểm tra tự động trên Windows sau build:
python tools/run_checks.py
python tools/desktop_jar_smoke.py
python tools/android_smoke.py
```

`android_smoke.py` tìm SDK từ `local.properties`, yêu cầu một emulator kết nối, tự cài APK và chạy touch/lifecycle checks. Có thể cài thủ công bằng `<SDK>/platform-tools/adb install -r android/build/outputs/apk/debug/android-debug.apk`, sau đó mở app hoặc Run module android trong Android Studio. Launcher hiện có vào thẳng GameScreen.

Điều khiển: joystick hoặc WASD/mũi tên; P/Esc/Back hoặc PAUSE để dừng; RESUME để tiếp tục. F2/ANIM chọn Idle → Walk → Attack → Hurt → Death của quái preview; F3/DIR đổi down → left → right → up. Không có nút tấn công hay gameplay combat trong task này.

## Hạn chế và đề xuất tiếp theo

- Player là art Kenney dẫn xuất với walk đơn giản, không có bộ attack/hurt/death; cần duyệt hoặc thay asset top-down có animation hoàn chỉnh. Kenney 16px và quái Willosuna 32/64px khác độ chi tiết; chưa coi art direction là bản cuối.
- Collision hiện theo tile/foot box, không có physics động, va chạm quái, lưu trận hoặc khôi phục sau process bị kill.
- Cần profiling dài hơn trên Pixel 4 với 60 actor trước khi thêm AI/combat; kết quả Desktop không thay cho số liệu emulator.
- Bước tiếp theo đề xuất là duyệt chuyển động/player và art scale, thống nhất callback/game state với native UI của Antigravity, rồi giao task gameplay tiếp theo riêng. **Chưa triển khai Task 02.**
