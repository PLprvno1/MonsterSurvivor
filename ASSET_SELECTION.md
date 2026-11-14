# ASSET_SELECTION – nhân vật và quái

## Quyết định đề xuất

- **Nhân vật chính:** N3XUS Iron Knight, `iron-knight-32x64.png`. Đây là sheet top-down 4 hướng có walk/talk được mô tả; idle lấy frame 0 của talk. Thiếu attack/death.
- **Đội quái:** 9 quái thường và 1 boss từ Pixel RPG Monsters Top-Down Free Demo, chỉ dùng `x1` và sheet gốc cùng JSON. Tất cả có frame 4 hướng và metadata rõ. Chọn Lich Lord làm boss vì có thêm entrance/slam và bộ animation đầy đủ.
- **Phong cách:** kích thước khớp theo bội số tile Kenney 16×16, nhưng sắc độ/độ chi tiết khác nhau. Cần duyệt hình trong game trước khi chốt art direction. Chưa có nhân vật nào vừa có animation chiến đấu hoàn chỉnh vừa khớp hoàn toàn với Kenney.

## Kiểm kê gói mới

| Gói | Nội dung được xác minh | Góc nhìn / animation |
|---|---|---|
| `asset_sources/N3XUS-Starter-FREE-v1.1/N3XUS-Starter-FREE-v1.1/` | 4 nhân vật: N3XUS Dev, Netrunner, Arcane Mage, Iron Knight. 6 quái: Acid Slime, Bat, Ghost, Skeleton, Zombie, Goblin. Có thêm 3 pet và 1 mount; không có boss. | README ghi top-down 4 hướng. Nhân vật `*-32x64.png`: 192×512, ô 32×64, 6 cột × 8 hàng; mỗi hướng có walk 6 frame và talk 6 frame, frame talk đầu làm idle. Sáu quái: idle/walk/attack/hurt/death, 6 frame mỗi state/hướng; 32×32 cho Acid Slime/Bat/Ghost, 32×64 cho Skeleton/Zombie/Goblin. JSON Aseprite có tag và thời lượng frame. |
| `asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/willosuna_rpg_monsters/` | 12 quái: Honey Slime, Wild Boar, Giant Bee, Red Cap, Red Crab, Giant Frog, Rock Golem, Living Armor, Mimic, Bone Hound, Lesser Demon, Fire Elemental. 2 boss: Boar King, Lich Lord; thêm biến thể phase 2 riêng của Lich Lord. Manifest có 15 record tính cả phase 2. | `manifest.json` xác nhận top-down 4 hướng. Quái 32×32, boss 64×64; pivot chân y=28/y=56. Sheet x1 + JSON có 5 state cơ bản. Các quái cũ có `_v2_sheet.png` riêng cho run/special/spawn/stun/dissolve; không được tính là frame trong sheet cơ bản. Lich Lord có 12 state trong sheet cơ bản; phase 2 là PNG riêng. Có thêm x2/x4 và dữ liệu Godot/Aseprite. |
| `asset_sources/spritecanvas-monsters-cc0/` | 4 quái: Goblin, Golem, Mushroom, Slime. Mỗi loại có 5 PNG state và 5 JSON atlas. Không có nhân vật chính hoặc boss. | README chỉ xác nhận quay mặt trái và mirror sang phải; không có hướng lên/xuống. Cell 298×291, 288×301, 221×156 và 178×164 tương ứng. Chi tiết frame ở bảng dưới. Không chọn cho bộ top-down 4 hướng. |

Frame của nhân vật N3XUS được mô tả trong `README.txt`, nhưng **không có JSON animation riêng cho nhân vật**. Dùng sheet chuẩn `*-32x64.png`; layout của biến thể `*-topdown.png` không đủ rõ để chọn thay thế. JSON trong các gói là Aseprite/TexturePacker/Phaser-Pixi hoặc Godot, không phải libGDX `.atlas` dùng trực tiếp.

## Mapping cuối cùng

Đường dẫn tương đối dưới đây bắt đầu từ `asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/willosuna_rpg_monsters/x1/`. Mỗi PNG sheet có JSON cùng tên trong cùng thư mục, trừ nhân vật N3XUS; frame count trong cột animation là **mỗi hướng**. `I/W/A/H/D` = idle/walk/attack/hurt/death. Vai trò là đề xuất gameplay, không phải metadata của asset.

| Tên | PNG | Animation và frame | Vai trò đề xuất |
|---|---|---|---|
| Iron Knight (người chơi) | `asset_sources/N3XUS-Starter-FREE-v1.1/N3XUS-Starter-FREE-v1.1/characters/iron-knight/iron-knight-32x64.png` | 4 hướng; walk 6, talk 6; idle = frame 0 của talk. Không có attack/death. | Nhân vật chính cận chiến |
| Honey Slime | `meadow/honey_slime/honey_slime_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Quái cơ bản theo đàn |
| Wild Boar | `meadow/wild_boar/wild_boar_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Lao nhanh, áp sát |
| Giant Bee | `meadow/giant_bee/giant_bee_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Quái bay nhanh |
| Red Cap | `forest/red_cap/red_cap_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Quái nấm áp sát |
| Red Crab | `river/red_crab/red_crab_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Quái lì đòn |
| Giant Frog | `river/giant_frog/giant_frog_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Đột kích tầm gần |
| Rock Golem | `mountain/rock_golem/rock_golem_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Quái chậm, nhiều máu |
| Living Armor | `castle/living_armor/living_armor_sheet.png` | 4 hướng; I6/W6/A6/H3/D6; 6/10/14/10/9 fps | Elite cận chiến |
| Bone Hound | `ruins/bone_hound/bone_hound_sheet.png` | 4 hướng; I4/W6/A5/H3/D5; 6/10/12/10/9 fps | Quái truy đuổi theo nhóm |
| Lich Lord (boss) | `bosses/lich_lord/lich_lord_sheet.png` | 4 hướng; I6/W6/A6/H3/D6; thêm run6, special6, spawn7, stun6, dissolve7, entrance10, slam8. Tốc độ/state và loop có trong JSON. | Boss phép thuật; dùng special/slam làm đòn chủ lực |

Các PNG mapping trên là sheet frame **32×32** (quái thường), **64×64** (Lich Lord); Iron Knight dùng cell **32×64**. Tất cả khớp theo số nguyên với tile Kenney 16×16: 2×2, 4×4 và 2×4 tile tương ứng. Độ đổ bóng và mật độ chi tiết cao hơn Kenney, nên không thể khẳng định đồng bộ phong cách chỉ từ kích thước.

## Animation còn có và asset không chọn

- Pixel RPG có thêm run 6, special 6, spawn 7, stun 6, dissolve 7 frame ở sheet `_v2_sheet` của các quái cũ; Boar King cũng có sheet v2 riêng. Lich Lord có entrance 10, slam 8 và phase 2 riêng với phase_shift 6. Những sheet v2 không cần đóng gói nếu gameplay chỉ dùng 5 state cơ bản.
- N3XUS có đủ 6 quái top-down nhưng không có boss, nên không đủ yêu cầu 10. Có thể thay bằng một bộ nhất quán N3XUS nếu bổ sung 4 sprite/boss sau này.
- SpriteCanvas có frame count thực tế `idle/walk/attack/hurt/death`: Goblin `21/20/26/10/19` (cell 298×291); Golem `21/20/28/8/24` (288×301); Mushroom `21/12/19/11/17` (221×156); Slime `35/12/21/8/21` (178×164). Các sheet có 8 frame mỗi hàng và JSON frame/pivot, nhưng chỉ có hướng trái; thiếu hai hướng top-down còn lại và cell quá lớn so với Kenney. Không dùng trong mapping.
- Không dùng hero hoặc map trong `pixel-rpg-monsters.../demo/`: `LICENSE.txt` nói rõ chúng thuộc các gói Willosuna khác và nằm ngoài license của Free Demo.

## License và điều kiện phân phối theo file đi kèm

| Gói được chọn/đánh giá | Bằng chứng trong repository | Điều khoản ghi trong tài liệu |
|---|---|---|
| N3XUS | `.../N3XUS-Starter-FREE-v1.1/LICENSE.txt` | Cho phép dự án cá nhân/thương mại; **bắt buộc** credit chính xác `Art by N3XUS - n3xus.dev`. Cấm phân phối lại asset rời/như asset pack và upload file để người khác tải; cấm NFT và huấn luyện/đánh giá AI. N3XUS giữ bản quyền gốc. |
| Willosuna Free Demo | `asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/LICENSE.txt` | Cho phép dùng/chỉnh sửa trong dự án cá nhân/thương mại, không yêu cầu credit (được khuyến khích). Cấm bán/phân phối lại chính file như asset pack hoặc nhận là tác giả. Điều khoản này không bao phủ hero/map demo. |
| SpriteCanvas | `spritecanvas-monsters-cc0/monster-*/LICENSE.txt` | Các file ghi CC0 1.0; cho phép dùng/chỉnh sửa/phân phối kể cả thương mại, không bắt buộc credit. |

Đây là tóm tắt đúng theo văn bản license có trong gói, không phải kết luận pháp lý độc lập. Nếu source repository được công khai, điều khoản N3XUS cấm upload các file art cho người khác tải; nên giữ asset nguồn ngoài source công khai và đưa credit vào game/store page.

## Việc cần giải quyết trước khi tích hợp

1. Quyết định chấp nhận Iron Knight thiếu attack/death hay bổ sung animation/nhân vật khác; có thể dùng attack effect riêng nhưng không thay thế animation nhân vật.
2. Duyệt palette/độ chi tiết khi ghép N3XUS, Willosuna và Kenney. Các frame khớp kích thước lưới nhưng phong cách không hoàn toàn đồng nhất.
3. Viết/import metadata cho libGDX: hiện project chỉ nạp PNG; JSON Aseprite/TexturePacker và `.tres` Godot không được xác nhận là libGDX-ready.
4. Chỉ đưa PNG/JSON x1 được chọn vào runtime; không chép x2/x4, preview, frame rời hay toàn bộ 6,876 PNG của gói Willosuna. Giữ nguyên điều kiện license và credit N3XUS trong bản phát hành.
