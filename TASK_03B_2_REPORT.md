# TASK 03B.2 — Pickup icon & scale refinement

## File đã sửa

Chỉ một file runtime thay đổi: `core/src/main/java/com/example/monstersurvivor/pickup/PickupRenderer.java`.

- Render trực tiếp region của từng loại; Damage/Health/Speed không còn vẽ thêm chest.
- Scale riêng cho health drop, culling bao gồm silhouette nhỏ và hiệu ứng thu nhặt hiện có.
- Bỏ toàn bộ nền glow và ba cạnh khung vuông lớn của Task 03B.1.

Thêm hai tool kiểm tra: `tools/smoke/PickupRefinementProbe.java`, `tools/run_pickup_refinement.py`; thêm báo cáo này. Không sửa `PickupType`, `GameAssets`, atlas/manifest, source sprite, GDD hoặc báo cáo cũ. Không thay Android Native UI, zoom/PixelCamera, AI/combat/map, PickupSystem, collision, reward handler, xác suất, lịch spawn hay TTL.

Đối chiếu SHA-256 **8.923 file baseline**: chỉ `PickupRenderer.java` thay đổi, không mất file nào. Bằng chứng: `scope-audit.json`; asset list và toàn bộ asset cũ cũng giữ nguyên.

## Mapping và scale cuối cùng

Dùng nguyên region runtime từ `assets/game/dungeon-pickups.json`, nguồn **Kenney Tiny Dungeon CC0**, `asset_sources/kenney_tiny-dungeon/Tilemap/tilemap_packed.png`. Tọa độ ô 16×16, zero-based, không spacing.

| Loại | Region | Ô nguồn | Scale ổn định | Canvas world trước zoom |
| --- | --- | --- | --- | --- |
| Damage | potion_red | (7,9) | 1.00× | 16×16 |
| Speed | potion_blue | (8,9) | 1.00× | 16×16 |
| Health | potion_green | (6,9) | 1.00× | 16×16 |
| Weapon Box | pickup_chest | (7,7) | 1.00× | 16×16 |
| Health Drop | potion_green | (6,9) | 0.75× | 12×12 |

Scale này thay cho 1.50× trước đây. Source có vùng trong suốt nên phần mực của potion nhỏ hơn canvas 16×16. Camera vẫn 1.15×; không nhân lại scale vào hitbox. `PickupType.box` vẫn biểu diễn nhóm gameplay của hộp, không còn quyết định việc vẽ chest sau potion.

Damage vẫn +1/+2/+3; Speed vẫn +1/cap 20; Health giữ hồi HP hoặc tăng Max HP. Weapon modal/offers/upgrade giữ nguyên; Health Drop vẫn heal theo cấu hình cũ.

## Hiệu ứng đã điều chỉnh

- Potion và chest giữ màu source, aspect vuông và Nearest filtering hiện có; không sửa texture hoặc dùng Linear.
- Silhouette tối alpha 0.4 dịch một source pixel theo hình alpha của icon, thay cho khung vuông. Shadow nhỏ 6×2 ở dưới sprite, scale theo item.
- Health có dấu cộng 3×3 nhỏ ở góc; Weapon có dấu vũ khí tím 3×4. Health Drop không có badge và nhỏ hơn Health.
- Không thêm bob, pulse hay blink liên tục. Giữ hiệu ứng xuất hiện/thu nhỏ khi nhặt 0.3 s hiện có và cảnh báo hết hạn trong 5 giây cuối; không thay vòng đời item.
- Không tạo texture, đọc asset hay cấp phát object mới trong draw loop. GameAssets vẫn quản lý 18 texture như trước.

## Ảnh so sánh

Cùng viewport 1140×540, zoom **1.15**, camera SCREEN cùng vị trí/phase, vị trí item và tuổi item = 1 s. Ba hàng nền stone/dirt/grass; cùng NightRenderer khi so cảnh đêm. Các ảnh chỉ vẽ Kenney CC0, hình học và font mặc định, không vẽ/xuất art player/quái.

- Ngày: [Trước](core/build/reports/task03b_2/before-day.png) · [Sau](core/build/reports/task03b_2/after-day.png).
- Đêm: [Trước](core/build/reports/task03b_2/before-night.png) · [Sau](core/build/reports/task03b_2/after-night.png).

Baseline renderer được lưu trước khi sửa và compile vào classpath riêng khi tái tạo ảnh “before”; không khôi phục đè file runtime đang chỉnh. Ảnh/bằng chứng là artifact cục bộ trong `core/build/reports/task03b_2/`.

## Kiểm thử tập trung

- `:core:compileJava :android:compileDebugJavaWithJavac --offline --console=plain`: **PASS**, chỉ có warning SDK XML version sẵn có.
- `python tools/run_pickup_refinement.py after`: **PASS**. Probe sau có **354** kiểm tra gồm mapping theo vai trò, một icon full-color mỗi item, không chest sau potion, kích thước 16/12 và aspect, không khung lớn, Nearest, không GL error/missing region, không pixel magenta trên ba nền ở ngày/đêm, texture count giữ nguyên.
- `PickupChecks`: **21.438** điều kiện logic qua, bao gồm rewards, cap, heal/drop, collision/collection, thời hạn và pause. Đây là tập pickup cũ, không chạy lại stress AI/map/vũ khí.
- `PickupSmoke`: **27** kiểm tra GameScreen/OpenGL qua: thu nhặt và stats thật, di chuyển qua pickup, Pause/lifecycle, expiry, Retry reset và Home. Probe không readback art actor.
- Mẫu render ngắn riêng trên Desktop GTX 1650: 15 item, cùng cảnh, 20 warmup + 60 mẫu, timing submit + `glFinish`, không I/O trong phần đo. Trước khoảng **0.59 ms**, sau các lượt khoảng **0.60–0.81 ms**; chênh nhỏ tuyệt đối và có dao động. Không quy đổi mẫu này thành FPS Android hay tuyên bố FPS được cải thiện. Không thêm texture hoặc xử lý AI/collision/map mỗi frame.

Log, ảnh và JSON timing nằm trong thư mục bằng chứng; `after-render.json` là lượt probe cuối. Lệnh chỉ chạy lại ảnh/probe: `python tools/run_pickup_refinement.py after PickupRefinementProbe`.

## Android Studio Run — Pixel_4

Đã thực hiện **Run trong Android Studio** bằng action Run/Shift+F10, configuration `android`, Pixel_4 `emulator-5554`. IDE log xác nhận deploy bản debug nội bộ mới lúc **23:06:47 ngày 13/11/2026** và kích hoạt HomeActivity lúc 23:06:48. Không gọi standalone assemble/bundle/install/export để bàn giao.

Smoke thực tế **6 kiểm tra qua**: chọn Easy/Demo bằng menu native, gameplay render sau Resume, zoom 1.15, pickup vẫn tự sinh, không runtime/missing-asset exception, Pause/Resume/Home. Telemetry cuối: elapsed 6.95 s, progress 46.32, 2 hộp, HP 10/10. App được trả về Home; không tạo/thay flag seed hay profiler.

Không tự nhận đã nhặt đủ năm loại bằng touch trên Android trong lượt ngắn này; các nhánh thu nhặt được kiểm tra qua logic/GameScreen Desktop nêu trên. Bằng chứng: `studio-run.log`, `studio-deploy.txt`, `android-logcat.txt`, `android-smoke.json` trong thư mục task.

## Hạn chế còn lại

Sprite nhỏ hơn có ít chi tiết trên màn hình, đặc biệt Health Drop 0.75×; dấu cộng giúp phân biệt Health với Drop. Silhouette/shadow chỉ hỗ trợ đọc hình, không phát sáng xuyên lớp đêm. Không thay Night Mode để tăng độ sáng item.

Không đo FPS Android trước/sau bằng profiler riêng và không chạy stress dài. Ảnh so sánh là cảnh kiểm tra CC0 có kiểm soát trên Desktop; không phải screenshot gameplay có nhân vật trên Android. Không triển khai Task 03C, không xuất APK/AAB, không release/sign/export, không commit/push.
