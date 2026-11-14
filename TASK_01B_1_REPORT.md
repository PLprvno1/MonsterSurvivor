# TASK 01B.1 — Tiny Questers integration & visual fix

## Kết quả

Đã thay player bằng Warrior, thêm Slime/Bat, giữ đủ 9 quái Willosuna và Lich Lord trong catalog. Cảnh mặc định có Warrior + Tiny Slime, Tiny Bat, Giant Bee, Red Cap, Living Armor và Lich Lord. ANIM/F2 duyệt Idle → Walk → Attack → Hurt → Death → Victory; DIR/F3 đổi hướng. Preview áp dụng cả Warrior khi đứng yên; di chuyển luôn dùng Walk. State thiếu của quái fallback Idle.

Player, joystick, tốc độ, collision và pause/lifecycle được tái sử dụng. `forest.json` đối chiếu với APK Task 01B **giống nguyên dữ liệu**: 100×100, spawn (808,808), 2.394 props, 7.203 ô đi được liên thông. Không thêm combat/AI/Dash gameplay.

## Nguồn và license

Root thực tế: `asset_sources/tiny-questers-free-starter-pack/tiny-questers-free-starter-pack/` (hai cấp cùng tên). Đã đọc `README.txt`, `LICENSE.txt`, ASSET_SELECTION, GDD và báo cáo Task 01B.

- License Bobddadoo cho phép game cá nhân/thương mại, chỉnh sửa và phân phối trong sản phẩm; ghi công tùy chọn. Cấm phân phối riêng/thành asset pack hoặc template art, sublicense, nhận là tác giả, và đưa art làm dữ liệu/input cho mô hình AI/ML. Runtime giữ nguyên license và thêm credit tác giả.
- Chỉ dùng ImageIO/Python cục bộ để đọc, đối chiếu và đóng gói; không đưa pixel Tiny Questers vào mô hình AI, không tạo hình hoặc huấn luyện. Ảnh chụp có Tiny Questers chỉ lưu cục bộ, chưa được mô hình mở để đánh giá thẩm mỹ.
- Kenney dùng license CC0 trong gói; Willosuna giữ license Free Demo đã chọn ở Task 01B. Ba license runtime được đối chiếu byte với bản nguồn. Không tải thêm tài nguyên, không commit/push.

## Kiểm kê thực tế

Mỗi gói có PNG `single`, `single_pot`, `atlas`, `atlas_pot`; GIF preview 1x/5x. Không có JSON, file LibGDX `.atlas` hoặc pivot số đi kèm. Có hai NPC ngoài phạm vi, không đóng gói.

| Gói | Clip / frame PNG gốc | Canvas thực tế |
|---|---:|---|
| `warrior_free` | 19 / 220 | Idle/Walk/Hit 64×49; Die 73×49; Win 64×64; Attack 128×128 |
| `slime_free` | 17 / 111 | Tất cả 64×64 |
| `bat_free` | 16 / 120 | Thường 64×64; Attack down 64×137, up 64×145, left/right 128×128 |

Attack Warrior nguồn là sheet 2048×256, cell 128×128, tối đa 16 frame/hàng; không cắt thành một hàng duy nhất. Pipeline kiểm tra số/tên frame liên tiếp, kích thước PNG và **so sánh từng pixel giữa single và atlas gốc** trước khi pack; kiểm tra PNG runtime sau ghi không đổi pixel.

Timing lấy từ block delay GIF thực tế: Warrior 150ms, riêng Win 70ms; Slime 150ms, riêng Die 120ms; Bat 100ms. README nói khoảng 6,67 FPS toàn gói chưa phản ánh các ngoại lệ này. Slime `walk_down` có **6 PNG / 5 frame GIF**, `hit_down` có **4 PNG / 3 frame GIF**: giữ đầy đủ PNG ở 150ms/frame, không bỏ frame theo GIF.

## Mapping đã triển khai

Tiền tố dưới đây đều nằm trong root nguồn nêu trên:

- **W** = `warrior_free/png/atlas/`
- **S** = `slime_free/png/atlas/`
- **B** = `bat_free/png/atlas/`

Số frame theo thứ tự **down / left / right / up**. Tên viết trong `{...}` là các file đã kiểm tra tồn tại.

| Sprite / state | PNG nguồn sau tiền tố | Frame runtime / hướng | FPS |
|---|---|---|---:|
| Warrior Idle | W `idle_{down,up}.png`; side dùng frame đầu `walk_{left,right}.png` | 5 / 1 / 1 / 5 | 6,67 |
| Warrior Walk | W `walk_{down,left,right,up}.png` | 4 / 4 / 4 / 4 | 6,67 |
| Warrior Attack | W `attack_{down,left,right,up}_stay.png` | 20 / 18 / 18 / 20 | 6,67 |
| Warrior Hurt | W `hit.png`, dùng chung | 2 / 2 / 2 / 2 | 6,67 |
| Warrior Death | W `die.png`, dùng chung | 10 / 10 / 10 / 10 | 6,67 |
| Warrior Victory | W `win_left.png`, `win_right.png`; down/up dùng right | 10 / 10 / 10 / 10 | 14,29 |
| Slime Idle | S `slime_idle_{down,up}.png`; side dùng frame đầu `slime_walk_{left,right}.png` | 5 / 1 / 1 / 5 | 6,67 |
| Slime Walk | S `slime_walk_{down,left,right,up}.png` | 6 / 5 / 5 / 5 | 6,67 |
| Slime Attack | S `slime_attack_{down,left,right,up}.png` | 8 / 9 / 9 / 7 | 6,67 |
| Slime Hurt | S `slime_hit_{down,right,up}.png`; left mirror right | 4 / 3 / 3 / 3 | 6,67 |
| Slime Death | S `slime_die_a.png`, dùng chung | 17 / 17 / 17 / 17 | 8,33 |
| Bat Idle | B `bat_idle_{down,up}.png`; side dùng frame đầu `bat_walk_{left,right}.png` | 6 / 1 / 1 / 6 | 10 |
| Bat Walk | B `bat_walk_{down,left,right,up}.png` | 6 / 6 / 6 / 6 | 10 |
| Bat Attack | B `bat_attack_{down,left,right,up}.png` | 12 / 12 / 12 / 12 | 10 |
| Bat Hurt | B `bat_hit_down.png`, dùng chung | 6 / 6 / 6 / 6 | 10 |
| Bat Death | B `bat_die_down.png`, dùng chung | 8 / 8 / 8 / 8 | 10 |

Idle/Walk lặp; các state còn lại giữ frame cuối. Không có Dash gốc: enum/API `DASH` đã sẵn, `has(DASH)` trả false và `clip(DASH)` fallback Walk. Không tạo animation Dash giả. Hurt/Death dùng chung không được coi là bốn bản vẽ theo hướng riêng.

## Pivot và tỷ lệ

README chỉ mô tả feet-center, không cho tọa độ số. Các điểm sau là **adapter của project**, xác định từ biên chân và đối chiếu dịch chuyển pixel cục bộ; không phải metadata tác giả. Tọa độ tính từ góc trái trên PNG:

| Sprite / clip | Pivot (x,y) nguồn | Scale đồng đều |
|---|---|---:|
| Warrior Idle/Walk/Hit | (32,38) | 1 |
| Warrior Attack stay | (64,78) | 1 |
| Warrior Die | (42,38), bù thân dịch +10px trên canvas rộng | 1 |
| Warrior Win | (32,53), bù +15px so với canvas thường | 1 |
| Slime, mọi clip | (32,48) | 1 |
| Bat thường và Attack down | (32,48) | 0,5 |
| Bat Attack left / right / up | (96,80) / (32,80) / (32,129) | 0,5 |

Region mang pivot riêng; render/culling dùng cùng anchor và scale. Không căn lại bounding box theo từng frame. Kiểm tra toàn bộ mapping xác nhận điểm neo world cố định khi đổi canvas; squash, flap và lunge có sẵn trong art được giữ nguyên. Warrior chọn biến thể attack `_stay` để hạn chế chuyển động thân trong clip.

Warrior front khoảng 25×23 world units, Slime 21×15; sải cánh Bat khoảng 28×22 sau scale. Willosuna thường cell 32×32, boss 64×64 giữ scale 1. Không kéo méo, tất cả Nearest, không mipmap. Bat 0,5 có đánh đổi mất chi tiết khi thu nhỏ. Kenney ít chi tiết, Tiny Questers viền rõ và Willosuna bảng màu tối vẫn khác phong cách; cần người dùng duyệt trực tiếp preview/pivot trước khi chốt art direction.

## Nguyên nhân và sửa sọc xanh

`ForestMap.java` render đúng các tile 16×16; dữ liệu ground/collision không gây dải màu. Atlas cũ đặt grass `(0,0,16,16)` ngay trên `shore_sw` ở y=16, **không có padding/extrusion**. Sai số lấy mẫu tại mép TextureRegion dưới phép chiếu phân số lấy sang shoreline, kể cả Nearest.

Đã tái hiện bằng framebuffer chỉ render cỏ: camera phase 0,9999, viewport cao 239,7 tại 960×540 tạo **900 pixel sai**, gồm màu nước `#63c5cf`, `#8ed4db` và màu bờ `#8f673f`, `#c58f5c`. Ảnh terrain-only: `core/build/reports/task01b1/terrain-before.png`.

Sửa atlas `world.png` thành 128×128, thêm **2px extrusion riêng mỗi region**, cập nhật rect catalog; nội dung tile gốc giữ nguyên. Tiny atlas cũng có extrusion 2px. Camera căn mép lưới chiếu theo pixel màn hình rồi clamp trong map. Không dùng lớp phủ hoặc đổi ground/collision. Cùng 20 mẫu chiếu sau sửa: **0 pixel sai trên Desktop và Android GLES**. Ảnh đối chứng `terrain-after.png` cùng thư mục.

## Runtime và file thay đổi

`assets/` hiện có **21 file, 626.549 byte; 14 PNG**, khoảng **25,72 MiB RGBA** trước overhead/font/driver (Task 01B khoảng 11,77 MiB).

- Mới: `assets/game/tiny/player.png` 1024×1916; `tiny_slime.png` 1024×476; `tiny_bat.png` 1024×1176. Chỉ chứa **323 frame nguồn được chọn**: Warrior 134, Slime 91, Bat 98; không kèm NPC/POT/GIF/FX Bat/biến thể thừa.
- Cập nhật `assets/game/world.png`, `catalog.json`, `licenses/CREDITS.txt`; thêm `licenses/Tiny-Questers.txt`. Bỏ player Kenney cũ `assets/game/player.png` và license Characters không còn dùng. `assets/assets.txt` được task Gradle sẵn có sinh lại.
- Giữ 10 PNG `assets/game/monsters/{honey_slime,wild_boar,giant_bee,red_cap,red_crab,giant_frog,rock_golem,living_armor,bone_hound,lich_lord}.png` giống byte nguồn x1; giữ `licenses/Kenney-RPG.txt`, `Willosuna-Free-Demo.txt`. Mapping Willosuna không đổi.
- Catalog có **292 clip, 1.643 frame entry**, gồm alias theo hướng. AssetManager nạp 14 texture một lần; Animation/FrameRegion tạo khi load. Render tiếp tục dùng SpriteBatch, queue tái sử dụng, culling và dispose như trước; không đọc file hoặc tạo Texture/Animation mỗi frame.

Core dưới `core/src/main/java/com/example/monstersurvivor/`:

| File | Thay đổi |
|---|---|
| `assets/GameAssets.java` | Pivot theo region, scale, Victory/Dash và fallback |
| `render/WorldSprite.java` | Draw/cull theo pivot từng clip và scale đồng đều |
| `screen/GameScreen.java` | Preview mới, Warrior preview, camera pixel alignment |
| `assets/AssetVisualCheck.java` | Kiểm tra GPU tùy chọn bằng flag local lúc show, không chạy trong render loop |

Công cụ tạo/sửa: `tools/prepare_assets.py`, `PrepareKenney.java`, `tiny_assets.py`, `PackPng.java`, `InspectTiny.java`, `CheckTinyAnchors.java`; công cụ kiểm tra `run_checks.py`, `smoke/SmokeLauncher.java`, `smoke/TerrainProbe.java`, `android_smoke.py`, `android_visual_check.py`, `measure_android_fps.py`, `desktop_jar_smoke.py`. Inventory 52 clip nguồn được sinh tại `core/build/asset-tools/tiny-inventory.json`.

Không sửa Java Player, ForestMap, CollisionGrid, Android Native UI, Gradle, Manifest, Launcher hoặc GDD.

## Kết quả build/test thực chạy

Môi trường: Java 17, Windows; AVD **Pixel_4**, model `sdk_gphone16k_x86_64`, API **37**, landscape **2280×1080**. Đây là emulator, không phải điện thoại Pixel 4 vật lý.

| Kiểm tra | Kết quả |
|---|---|
| `:lwjgl3:jar :android:assembleDebug --offline` | Thành công, exit 0; cảnh báo SDK XML v3/v4 có sẵn, không sửa Gradle |
| Pack PNG, atlas/single, timing, license, hash Willosuna | Thành công; đối chiếu dữ liệu nguồn thực tế |
| Foundation | 280 assertion: FPS độc lập, collision cây/đá/nước, biên, liên thông map |
| Desktop OpenGL | 2.136 assertion: tất cả frame Tiny, pivot, one-shot, Victory/Dash fallback, joystick, camera theo/align/biên, pause/lifecycle, resize |
| Desktop JAR đóng gói | Mở đúng launcher/cảnh, đóng cửa sổ bình thường, exit 0 |
| Android debug APK | Cài/chạy thực, 15 check về joystick, pause, Home/resume, timer, asset/lỗi runtime |
| Android GPU | 20 mẫu terrain, **0 pixel sai**; render **481 lượt frame/mapping Tiny** kể cả alias, GL error 0 |
| 60 preview Desktop, không AI/combat | 180 frame thực ở 640×480: **57,72 FPS**, render CPU/driver trung bình 0,230ms, 62 world draw call/frame |

APK: `android/build/outputs/apk/debug/android-debug.apk`; Desktop: `lwjgl3/build/libs/MonsterSurvivor-1.0.0.jar`. Logs/JSON/ảnh cục bộ: `core/build/reports/task01b1/`; build log: `core/build/reports/task01b1-build.log`.

### FPS Android trước/sau

Đo lại tuần tự bằng APK Task 01B lưu cục bộ và APK mới, cùng emulator/độ phân giải/cảnh 6 preview. Mỗi APK cold start, warm-up **10 giây**; lấy timestamp `actualPresentTime` SurfaceFlinger duy nhất trong khoảng thực tế khoảng **10,8–11,7 giây mỗi cảnh**, không dùng snapshot HUD ngay sau resume.

| Cảnh | Trước FPS | Sau FPS | p95 frametime trước → sau |
|---|---:|---:|---:|
| Đứng yên | 34,80 | **34,61** | 43,19 → 41,13ms |
| Joystick/camera di chuyển | 33,23 | **34,03** | 45,57 → 42,32ms |

Bản mới chưa đạt 60 FPS trên emulator. Một lượt ngắn không đủ kết luận chênh lệch hiệu năng ổn định; không giảm chất lượng/thay gameplay để tăng FPS. Dữ liệu: `fps-before.json`, `fps-after.json`; chưa benchmark 60 quái trên Android hoặc thiết bị vật lý.

## Chạy lại và việc còn lại

```powershell
python tools/prepare_assets.py
.\gradlew.bat :lwjgl3:jar :android:assembleDebug --offline
java -jar lwjgl3/build/libs/MonsterSurvivor-1.0.0.jar
python tools/run_checks.py
python tools/desktop_jar_smoke.py
python tools/android_smoke.py
python tools/android_visual_check.py
python tools/measure_android_fps.py after
```

Các script Android yêu cầu emulator đang kết nối; smoke tự cài APK. Visual check tạo/xóa flag trong files riêng của app, render offscreen rồi trả GL viewport; không bật mặc định.

Còn thiếu: Dash riêng, idle side riêng của cả ba sprite, các hướng Hurt/Death độc lập và Victory up/down của Warrior. Những trường hợp này đã có fallback rõ ràng, không được báo là art gốc đầy đủ. Cần người dùng xem trực tiếp preview để duyệt thẩm mỹ, tỷ lệ Bat và các pivot suy ra; kiểm tra số liệu/GPU không thay thế bước duyệt hình này. Chưa kiểm tra dài hạn, process-death/context-loss hoặc gameplay combat.

Đề xuất tiếp theo: duyệt hình trên emulator, chốt pivot/tỷ lệ với tác giả nếu cần, rồi profiling Android dài hơn trước khi giao task gameplay tiếp theo. **Không tự triển khai Task 02.**
