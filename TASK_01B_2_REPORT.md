# TASK 01B.2 — Camera smoothness & frame pacing

## Kết quả

Đã chuyển camera/player sang **cùng lưới screen pixel**, tách tọa độ render khỏi tọa độ mô phỏng. Giảm được các bước nhảy do làm tròn world pixel và dao động điểm bám đã tái hiện. Viewport, gameplay, Player, joystick, collision, map và asset giữ nguyên.

**Frame pacing trên emulator chưa cải thiện đáng kể**: vẫn khoảng 33–34 FPS, p99 khi di chuyển khoảng 51ms. Cần playtest thủ công để xác nhận cảm giác rung/giật đã chấp nhận được; các kiểm tra tự động không xác nhận được cảm nhận này.

## 1. Chẩn đoán đã xác minh

Đã đọc báo cáo 01B.1 và GameScreen, Player, GameInput, ForestMap, WorldSprite; kiểm tra backend Android 1.14.2 trong source JAR cache.

- Camera cũ làm tròn tọa độ player sang **world pixel**, sau đó căn camera sang screen pixel. WorldSprite còn tự làm tròn tọa độ riêng. Trên emulator, 1 world pixel bằng **4,5 screen pixel**: khi đẩy joystick nhẹ, tọa độ mô phỏng tăng nhưng camera có nhiều frame đứng nguyên rồi nhảy 4–5 pixel.
- Đo Android: camera cũ đứng nguyên **49,3% frame đi ngang nhẹ, 60% frame đi chéo nhẹ** dù mô phỏng vẫn di chuyển. Sai số bước dịch chuyển RMS lần lượt **2,05 / 3,11 screen pixel**. Điểm bám player/camera dao động khoảng **0,50 pixel mỗi trục**.
- Tái hiện thêm lỗi cửa sổ lẻ **961×541**: căn mép camera bằng round trên phép trừ float có thể làm điểm bám dao động gần **1 pixel**. Test tái hiện lưu trong `core/build/reports/task01b2/camera-checks-initial.txt`; bản sửa qua kiểm tra cùng trường hợp.
- Nearest sampling ở zoom phân số có thể đổi lựa chọn texel tại vị trí sát ranh giới do sai số float. Probe Desktop trước cố định pha: SCREEN có **3.072 pixel khác mẫu** sau dịch chuyển nguyên pixel; cố định pha lấy mẫu đưa số này về **0**. Đây là sai khác mẫu bên trong tile; số pixel lấy nhầm màu từ region khác vẫn bằng 0.
- Backend Android đang dùng trả delta thực từ `System.nanoTime`, không có bước lấy trung bình delta tại đây. Trong các đoạn trace đã phân tích, không ghi nhận mất bước vì cap delta 100ms. Giữ nguyên delta guard và pause/resume hiện có.

Emulator: AVD **Pixel_4**, API **37**, landscape **2280×1080**. `dumpsys SurfaceFlinger` xác nhận **Android Emulator OpenGL ES Translator (Google SwiftShader)**. Đây là renderer phần mềm; chưa tách riêng ảnh hưởng CPU/GPU/compositor/scheduling để kết luận nguyên nhân duy nhất của pacing. Không thay cấu hình emulator/backend hoặc áp thêm FPS cap.

## 2. Logic và file sửa

Trong `core/src/main/java/com/example/monstersurvivor/`:

| File | Nội dung |
|---|---|
| `render/PixelCamera.java` — mới | Tính tọa độ trình bày và clamp; hỗ trợ LEGACY/FREE/WORLD/SCREEN để so sánh. SCREEN là mặc định trong game |
| `render/WorldSprite.java` | Bỏ round world pixel ẩn trong drawX/drawY; draw/cull cùng tọa độ trình bày và pivot hiện có |
| `screen/GameScreen.java` | Cập nhật mô phỏng như cũ, đưa tọa độ render vào playerSprite và camera; clamp không còn tự round world pixel |
| `render/CameraTrace.java` — mới | Đo tùy chọn bằng file local riêng của app; phát touch vào GameInput, ghi buffer tọa độ/timing rồi xuất CSV sau đo; probe terrain chạy sau sampling |

SCREEN lấy `round(simulationPosition × pixelsPerWorldUnit)` làm điểm neo chung. Camera dùng neo nguyên pixel và tâm viewport nguyên để ổn định cả cửa sổ chẵn/lẻ; cửa sổ lẻ có điểm bám lệch tâm hình học dưới 1 pixel. Một pha chiếu cố định **1/16 screen pixel** tránh các điểm lấy mẫu Nearest mơ hồ tại zoom 4,5×/2,25× đã kiểm tra. Bước dịch chuyển camera vẫn theo screen pixel; pha này không thay đổi theo frame.

Tọa độ mô phỏng không nhận ngược tọa độ render. Sai số lượng tử player giới hạn khoảng nửa screen pixel mỗi trục. Không thêm smoothing có độ trễ hoặc interpolation, không sửa tốc độ/animation/collision. Viewport ExtendViewport 320×240 và FOV hiện có được giữ nguyên; giữ Nearest, extrusion atlas và render trực tiếp bằng SpriteBatch.

Luồng bình thường không tạo thêm Texture/Animation/object mỗi frame. PixelCamera được tạo một lần; khi không có flag đo, CameraTrace không cấp buffer hoặc chạy probe. Framebuffer chẩn đoán chỉ được tạo khi yêu cầu đo và được dispose sau kiểm tra.

**Không sửa runtime asset, Tiny Questers, license, GDD, Gradle, Manifest, launcher hoặc Android Native UI.** SHA-256 của toàn bộ 21 file runtime và GDD giống trước task (`protected-hashes.json`). Không triển khai tính năng gameplay mới, không commit/push.

Công cụ tạo/sửa: `tools/camera_compare.py`, `frame_pacing.py`, `checks/CameraChecks.java`, `smoke/CameraLauncher.java`; cập nhật công cụ regression `run_checks.py`, `smoke/SmokeLauncher.java`, `smoke/TerrainProbe.java`, `android_smoke.py`, `android_visual_check.py`, `desktop_jar_smoke.py` để kiểm tra tọa độ trình bày mới và ghi vào thư mục report 01B.2.

## 3. So sánh camera

So sánh bốn mode trên cùng map/cảnh 6 preview, cùng viewport và chuỗi joystick qua GameInput. Android warm-up **10s**, mỗi đoạn **2s**, input nhẹ 22% và input đầy; bắt đầu mỗi đoạn trung tâm ở cùng vị trí. Đã chạy đứng yên, bốn hướng, bốn đường chéo. Bản cuối còn chạy bốn biên map; CSV kiểm tra chuyển động khớp input thực và camera không vượt biên.

RMS dưới đây là sai số bước dịch chuyển camera so với bước mô phỏng, tính bằng **screen pixel**, bỏ frame chuyển đoạn:

| Mode Android | RMS ngang nhẹ | RMS dọc nhẹ | RMS chéo nhẹ | Frame camera đứng nguyên ngang / chéo |
|---|---:|---:|---:|---:|
| LEGACY: world round + screen align cũ | 2,05 | 2,31 | 3,11 | 49,3% / 60,0% |
| FREE: không snap player/camera | 0,00 | 0,00 | 0,00 | 0% / 0% |
| WORLD: snap chung theo world pixel | 2,12 | 2,06 | 3,05 | 46,0% / 60,0% |
| **SCREEN: bản chọn** | **0,41** | **0,39** | **0,62** | **0% / 0%** |

Điểm bám player/camera của SCREEN có biên dao động số học dưới **0,001 screen pixel** ở các đoạn trung tâm đã đo. FREE có tọa độ liên tục nhưng probe raster ghi nhận thay đổi mẫu texel; SCREEN giữ được mẫu ổn định trong các cấu hình đã thử.

Desktop 1140×540, zoom 2,25×: RMS ngang/chéo từ **1,08/1,31** xuống **0,49/0,71 pixel**. Ở tốc độ rất thấp, lượng dịch chuyển mong muốn nhỏ hơn 1 screen pixel/frame vẫn có frame lặp: ngang nhẹ khoảng **70,1% → 33,3%**; không tuyên bố loại được mọi bước lượng tử ở mọi tốc độ.

Trace cuối có **1.090 frame Android / 1.920 frame Desktop**, 16 đoạn mỗi nền tảng. Kiểm tra điểm mô phỏng và input ghi nhận: `movement-verified.json`. Desktop có sai số trục phụ khoảng 0,0067 do tọa độ touch nguyên ở scale HUD 1,125; đối chiếu chuyển động dùng input thực ghi nhận, không giả định trục phụ bằng 0. Giữ nguyên GameInput.

## 4. FPS và frame time trước/sau

### Android — frame thực được trình bày

Cài tuần tự APK 01B.1 lưu trước sửa và APK mới; cold start, warm-up **10s** mỗi APK. Cùng emulator/cảnh/độ phân giải, không bật trace/probe và không chạy Desktop benchmark đồng thời. Lấy timestamp `actualPresentTime` duy nhất từ SurfaceFlinger, khoảng **11s mỗi cảnh**, 372–379 frame/cảnh. Joystick dùng cùng lệnh ADB swipe. p95/p99 tính trên các khoảng giữa frame trình bày:

| Cảnh | FPS trước → sau | p95 trước → sau | p99 trước → sau |
|---|---:|---:|---:|
| Đứng yên | 34,47 → **34,35** | 42,11 → **41,55ms** | 46,50 → **46,29ms** |
| Joystick ngang | 33,67 → **33,52** | 43,33 → **44,02ms** | 50,26 → **50,82ms** |

Không có bằng chứng cải thiện FPS/pacing đáng kể từ lượt đo này. Camera được sửa về lượng tử hóa; dao động thời gian trình bày vẫn còn. Chưa đạt 60 FPS Android, chưa có kết luận dài hạn hoặc trên thiết bị vật lý.

### Callback render — kiểm tra các hướng

Các dòng dưới dùng delta callback render, **khác nguồn đo SurfaceFlinger**. Warm-up 10s; mỗi đoạn 2s nên percentile là mẫu ngắn phục vụ chẩn đoán. Desktop dùng cùng launcher đo, VSync và giới hạn 60 FPS; LEGACY/SCREEN có cùng chuỗi input:

| Nền tảng / cảnh | FPS trước → sau | p95 trước → sau | p99 trước → sau |
|---|---:|---:|---:|
| Android dọc đầy | 33,42 → 34,45 | 44,81 → 44,01ms | 47,88 → 46,37ms |
| Android chéo đầy | 34,12 → 33,43 | 45,01 → 41,20ms | 46,70 → 44,15ms |
| Desktop đứng yên | 60,00 → 60,00 | 17,18 → 16,68ms | 17,77 → 17,08ms |
| Desktop ngang đầy | 60,00 → 60,00 | 17,00 → 17,00ms | 17,32 → 17,61ms |
| Desktop dọc đầy | 60,00 → 60,00 | 17,20 → 16,95ms | 17,51 → 17,59ms |
| Desktop chéo đầy | 59,99 → 60,00 | 16,91 → 17,81ms | 17,34 → 18,09ms |

Các mode FREE/WORLD cũng đã chạy trên hai nền tảng; raw CSV/JSON chứa timing từng đoạn. Không lấy thay đổi nhỏ giữa các lượt đơn lẻ làm kết luận tăng hiệu năng.

## 5. Kiểm tra texture bleeding và raster

Probe chỉ render/readback terrain Kenney CC0; không đưa hình Tiny Questers vào mô hình AI. So sánh mẫu terrain sau khi bù dịch chuyển nguyên pixel:

| Nền tảng / mode | Pixel so sánh | Khác mẫu | Lấy nhầm màu region khác |
|---|---:|---:|---:|
| Desktop LEGACY | 999.936 | 2.904 | 0 |
| Desktop FREE | 999.936 | 8.145 | 0 |
| Desktop WORLD | 999.936 | 8.880 | 0 |
| **Desktop SCREEN** | **999.936** | **0** | **0** |
| Android LEGACY | 999.712 | 0 | 0 |
| Android FREE | 999.936 | 5.184 | 0 |
| Android WORLD | 999.712 | 5.060 | 0 |
| **Android SCREEN** | **999.936** | **0** | **0** |

Probe 20 cấu hình chiếu của Task 01B.1 cũng được chạy lại trên Desktop và Android: **0 pixel sai**; Android render **491 lượt frame/mapping Tiny Questers**, GL error **0**. Không tái xuất hiện texture bleeding trong các kiểm tra đã chạy. Fixed phase được kiểm tra tại độ phân giải/zoom nêu trên; chưa khẳng định mọi zoom phân số/HiDPI đều không có shimmer.

## 6. Build và regression đã thực hiện

| Hạng mục | Kết quả thực chạy |
|---|---|
| `:lwjgl3:jar :android:assembleDebug --offline` | Thành công, exit 0. Còn cảnh báo SDK XML v3/v4 có sẵn |
| Camera projection checks | **44.781 assertion**: 24/30/34/60/120 FPS, hướng/chéo, lượng tử ≤0,5 screen pixel, không sửa mô phỏng, chẵn/lẻ, biên/viewport lớn |
| Foundation checks | **280 assertion**: tốc độ theo FPS, collision, boundary, map liên thông |
| Desktop OpenGL regression | **2.136 check**: frame/pivot, joystick/keyboard, camera, resize, pause/resume, 60 preview cố định |
| Desktop JAR đóng gói | Mở cảnh, đóng cửa sổ và dispose bình thường, exit 0 |
| Android debug APK | **15 check**: cài/chạy, touch joystick thực qua ADB, pause, Home/resume, timer, không lỗi asset/runtime trong log |
| Camera trace Desktop/Android | Đã chạy 4 mode; bản cuối xác minh 8 hướng và 4 biên, mô phỏng khớp input ghi nhận |
| Terrain/GPU | Các số đo tại mục 5; không gắn PASS cho cảm giác rung chủ quan |
| Asset/GDD | 22 SHA-256 không đổi; kiểm tra hash Willosuna, metadata và license nguồn vẫn qua |

APK: `android/build/outputs/apk/debug/android-debug.apk`; JAR: `lwjgl3/build/libs/MonsterSurvivor-1.0.0.jar`.

Chứng cứ: `core/build/reports/task01b2/` gồm `android-camera-comparison.json`, `desktop-camera-comparison.json`, các CSV theo mode, `*-raster.csv`, `frame-pacing-before.json`, `frame-pacing-after.json`, `movement-verified.json`, `environment.txt`, regression logs/ảnh cục bộ. Build log ở `core/build/reports/task01b2-build.log`; log lượt cuối `task01b2-final-validation.log`.

## 7. Chạy lại và hạn chế

```powershell
.\gradlew.bat :lwjgl3:jar :android:assembleDebug --offline
python tools/run_checks.py
python tools/desktop_jar_smoke.py
python tools/android_smoke.py
python tools/android_visual_check.py
$env:MS_CAMERA_LONG='1'
$env:MS_CAMERA_RASTER='1'
python tools/camera_compare.py desktop
python tools/camera_compare.py android
python tools/frame_pacing.py
```

Các script Android cần emulator kết nối và có thể cài lại APK debug. Camera compare tự tạo/xóa flag local trong files riêng của app hoặc `core/build/camera-run`; không có menu debug mới. `frame_pacing.py` cần APK cũ đã lưu ở `core/build/reports/task01b2/baseline.apk`; nếu mất file này thì không dùng APK mới thay làm baseline.

**Cần playtest thủ công**: giữ joystick nhẹ/đầy, xoay liên tục qua góc chéo, đi sát biên và thử Pause/Resume trên emulator. Chưa xác nhận bằng cảm nhận rằng rung đã hết hoàn toàn; chưa kiểm tra physical device, HiDPI khác cấu hình, process-death/context-loss hoặc đo pacing dài hạn. Nhịp animation nguồn được giữ nguyên.

Bước tiếp theo phù hợp: duyệt cảm giác di chuyển, profiling trình bày/GPU trên môi trường có renderer phần cứng và thiết bị thật trước khi quyết định tối ưu khác. Không tự đổi thiết lập emulator, hạ chất lượng hoặc triển khai gameplay ngoài task.
