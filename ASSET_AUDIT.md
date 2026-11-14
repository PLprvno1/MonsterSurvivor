# Asset audit – MonsterSurvivor

Phạm vi: kiểm tra project và `asset_sources/`; không sửa mã nguồn, Gradle, Manifest hay runtime. Project hiện là libGDX starter: `core/.../MonsterSurvivorGame.java` chỉ nạp `libgdx.png`. Runtime `assets/` hiện chỉ có `libgdx.png` và `assets.txt`; các gói trong `asset_sources/` chưa được đưa vào runtime.

## Danh mục thực tế

| Bộ asset | Nội dung và kích thước | Khả năng dùng |
|---|---|---|
| `asset_sources/kenney_roguelike-characters/Spritesheet/roguelikeChar_transparent.png` (918×203) và `_magenta.png` (918×203) | 2 biến thể nền của sheet nhân vật/module trang phục, vũ khí; tài liệu kèm ghi tile 16×16, lề 1 px. | Ứng viên nhân vật chính và biến thể nhân vật humanoid. Chọn bản transparent; magenta hữu ích làm nền kiểm tra/cắt sprite. |
| `asset_sources/kenney_roguelike-rpg-pack/Spritesheet/roguelikeSheet_transparent.png` (968×526) và `_magenta.png` (968×526) | 2 biến thể sheet lớn; tile 16×16, lề 1 px. Có địa hình, cây/cỏ/nước, công trình, vật cản, đồ vật, vật chứa và hình nhân vật trong ảnh mẫu. | Nguồn chính cho bản đồ rừng, vật cản/đồ nhặt và hình hộp/rương. |
| `asset_sources/kenney_roguelike-rpg-pack/Map/sample_map.tmx` và `Map/sample_indoor.tmx` | 2 TMX, mỗi map 100×100 ô 16 px. `sample_map` có 5 layer ngoài trời; `sample_indoor` có 4 layer nội thất. Cùng tham chiếu sheet RPG. | `sample_map.tmx` là nền tốt để làm map rừng; `sample_indoor.tmx` không phải map rừng. |
| `asset_sources/kenney_tiny-dungeon/Tilemap/tilemap.png` (203×186), `tilemap_packed.png` (192×176), `Tiles/tile_0000.png`–`tile_0131.png` | 2 sheet và 132 ảnh tile rời 16×16. TSX `Tiled/sampleSheet.tsx` mô tả 132 tile, 12 cột, spacing 1 px và tham chiếu `tilemap.png`. | Tường, sàn, cửa, vật dụng dungeon; không phải nguồn địa hình rừng. Dùng một dạng sheet, không cần kèm thêm 132 tile rời nếu đã dùng sheet. |
| Map Tiny Dungeon | `Tiled/sampleMap.tmx`, 32×20 ô 16 px, layer `Dungeon`, `Objects`, `Carts`. | Map mẫu dungeon nhỏ. |
| Preview/sample | 7 PNG, mỗi ảnh 918×515: Preview/Sample của Characters và Tiny Dungeon; Preview/Sample1/Sample2 của RPG pack. | Ảnh minh họa/ảnh chụp mẫu, không dùng làm sprite runtime. |

Tổng trong `asset_sources/`: **145 PNG** (gồm 7 preview/sample, 4 sheet nhân vật/RPG, 2 tilemap Tiny Dungeon và 132 tile rời), **3 TMX**, **1 TSX**, **6 TXT** và **6 URL shortcut**. Trong đó 3 TXT là license, 3 TXT còn lại là thông tin sheet/tile. Không thấy JSON/atlas, file animation hay font riêng.

## Phân loại và đề xuất

- **Nhân vật chính:** chọn một nhân vật modular từ `kenney_roguelike-characters/Spritesheet/roguelikeChar_transparent.png`; dùng sheet RPG nếu cần nhân vật đồng bộ trực tiếp với mẫu map. Hình là pixel art nhỏ 16 px, nên phóng theo bội số nguyên.
- **10 loại quái:** không tìm thấy bộ 10 quái được đặt tên hoặc xác nhận là monster. Nếu chấp nhận kẻ địch humanoid tạm thời, có thể phối màu/trang bị trên sheet Characters thành 10 archetype: kiếm sĩ nhẹ, lính khiên, thương thủ, cung thủ, đạo tặc, pháp sư, giáp nặng, song kiếm, trinh sát biến thể màu và đội trưởng/elite. Đây là 10 cách tái phối asset có sẵn, **không phải 10 sprite quái hoàn chỉnh**; cần bổ sung sprite quái riêng nếu yêu cầu quái có hình thái khác humanoid.
- **4 hộp nâng cấp:** lấy bốn hình vật chứa khác nhau nhìn thấy trong `roguelikeSheet_transparent.png` (thùng gỗ, hòm gỗ, hòm/thùng có viền kim loại, hòm màu sáng). Atlas không kèm tên hoặc ID cho từng vùng sprite, nên cần chọn/cắt vùng bằng đối chiếu hình ảnh trước khi gán ID trong game.
- **Bản đồ rừng:** dùng `kenney_roguelike-rpg-pack/Map/sample_map.tmx` cùng `Spritesheet/roguelikeSheet_transparent.png`; sheet có cỏ, cây, nước, đường, hoa/đá và vật cản phù hợp cảnh rừng. Dùng `sample_indoor.tmx` hoặc Tiny Dungeon cho khu vực dungeon riêng.
- **Vũ khí/vật phẩm:** có sprite vũ khí và đồ vật trong hai sheet Roguelike; các vật chứa có thể làm icon loot. **Hiệu ứng:** không thấy sheet hiệu ứng hoặc chuỗi đánh/chết. **UI:** không thấy bộ nút, panel HUD hoặc font; icon vật phẩm không thay thế UI.

## Animation, phong cách và giấy phép

Không có sprite nào được tài liệu trong gói xác nhận có đủ **idle/run/attack/death**. Có nhiều hình/biến thể và hướng nhìn để chọn, nhưng không có nhãn state, thứ tự frame hay metadata để khẳng định đó là animation; vì vậy cần xem như ảnh tĩnh cho đến khi tự lập frame map và kiểm tra từng sprite. Thiếu rõ nhất là animation chạy/đánh/chết, 10 quái riêng, hiệu ứng và bộ UI.

Ba bộ đều là pixel art Kenney, tile cơ sở 16×16; Roguelike Characters và RPG có cùng ghi chú lề 1 px nên phối hợp phù hợp khi dùng cùng tỉ lệ và nearest-neighbor. Tiny Dungeon cũng cùng kích thước pixel nhưng thiên về tường đá/tông dungeon tối, hợp làm màn riêng hơn là trộn vào rừng sáng.

Tài liệu có sẵn tại `kenney_roguelike-characters/License.txt`, `kenney_roguelike-rpg-pack/License.txt` và `kenney_tiny-dungeon/License.txt` đều ghi **Creative Commons Zero (CC0)**; các file nêu có thể dùng trong dự án cá nhân/thương mại và ghi công là không bắt buộc. Báo cáo chỉ dựa trên license nằm trong từng gói.

## Đề xuất đóng gói runtime

Android Gradle hiện trỏ asset source vào `../assets`, nên file nào được chép vào `assets/` sẽ có thể được đóng gói cùng app. Giữ nguyên các gói gốc trong `asset_sources/`; khi triển khai, chỉ chép sprite đã chọn vào các nhóm `assets/game/player`, `enemies`, `world`, `items`, `ui`. Cắt/đóng gói các vùng được dùng thành atlas nhỏ và giữ metadata vùng; chỉ kèm ảnh tilemap cùng TMX/TSX thật sự tham chiếu. Loại preview/sample, bản magenta, sheet trùng mục đích và tile rời không dùng khỏi runtime. Hiện tại không có asset nào từ ba gói này trong APK/runtime.
