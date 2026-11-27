# TASK 12A – Character Classes, Skills & Asset Audit

**Phạm vi:** khảo sát source/asset và đề xuất cho Task 12B. Không sửa gameplay, không thêm/tải asset, không build hoặc chạy test/emulator.

## Kết luận

- Giữ `player` hiện tại làm Warrior mặc định, không đổi atlas, animation, kích thước, điều khiển, HP/tốc độ hoặc Sword Lv1. Người chơi mới sở hữu Warrior và bắt đầu với Warrior.
- Giá trị mặc định đọc từ config hiện tại: HP 10, damage Sword 1, speed stat 10 × 8 = 80 world units/s (`Player.SPEED` cũng là 80). Profile Warrior cần dùng nguyên các giá trị/config này.
- Chưa có sẵn cặp Archer/Mage hoàn chỉnh trong runtime. Nguồn ghép cặp miễn phí đáng xem nhất là [GegX Archer](https://gegx.itch.io/archer-hero-128) và [GegX Mage](https://gegx.itch.io/mage-hero-128): cùng họa phong, 8 hướng, Idle/Walk, cho phép dùng thương mại, không bắt buộc ghi công. Attack/Hurt/Death nằm trong các gói riêng; canvas 176×176 và 168×168 cần thu nhỏ, kiểm tra pixel density trước khi chốt.
- Không lấy hero trong demo Willosuna làm sprite: license của demo loại trừ rõ các hero đó. FX runtime hiện tại đến từ MonstersCreatures và có license riêng trong repo.
- Vũ khí mới phải khởi tạo theo lớp. Hiện `WeaponLoadout` luôn cấp Sword; `WeaponSystem` và `GameSession` cũng gọi Sword mà không kiểm tra vũ khí có tồn tại. Chỉ thay loại vũ khí khởi đầu sẽ gây lỗi/đánh Sword ngầm cho Archer/Mage.

## 1. Asset nhân vật

| Nguồn hiện có | Nội dung/độ phù hợp | License và điều kiện |
|---|---|---|
| **Player đang dùng:** `assets/game/tiny/player.png`, ID `player` trong `assets/game/catalog.json`; nguồn `asset_sources/tiny-questers-free-starter-pack/.../warrior_free` | Tiny Questers Warrior, atlas runtime **1024×1916**, displayScale 1; cell cơ sở 64×49, attack 128×128, hurt 2 frame, death 10, victory 10. Idle 5 frame trước/sau và pose đầu của Walk ở hai hướng ngang; Walk 4 frame/hướng; Attack 18–20 frame/hướng. Game map đủ Idle/Walk/Attack/Hurt/Death và 4 hướng bằng clip dùng chung/mirror. Đây chính là Warrior phải giữ nguyên. | Bản Starter có license local `assets/game/licenses/Tiny-Questers.txt`: dùng game thương mại miễn phí, được chỉnh sửa, credit Bobddadoo không bắt buộc; cấm bán/đóng gói lại asset riêng và AI training. |
| **Tiny Questers Mage / Elf Archer** | Không có file các lớp này trong nguồn local. README của Starter mô tả chúng là các gói hero riêng, nên có triển vọng khớp style nhất với Warrior hiện tại. Chưa có sprite hay license sản phẩm cụ thể để kiểm tra. | Chỉ cân nhắc sau khi đọc license của gói tương ứng; không suy rộng license của Starter. Trang tác giả: [Bobddadoo](https://bobddadoo.itch.io). |
| **N3XUS Starter FREE – Arcane Mage** | Có local `arcane-mage-32x64.png` **192×512**, 6×8 frame; 4 hướng, Walk và Talk (frame đầu dùng làm Idle), thêm sheet top-down **768×256** và portrait. Mage fantasy phù hợp, nhưng thiếu Attack/Hurt/Death; không có archer phù hợp trong Starter. Khung 32×64 lớn và nét đậm hơn Player hiện tại. | [Trang chính thức](https://samura133.itch.io/n3xus-starter-free); license local cho phép game thương mại, nhưng **bắt buộc** credit `Art by N3XUS - n3xus.dev`, cấm phát tán file asset riêng và cấm upload asset lên public repository. Kiểm tra điều kiện repo trước khi chọn. |
| **Kenney Roguelike Characters** | Local sheet **918×203**, ô 16×16, cách 1 px; có nhiều bộ phận/đồ nghề và hình nhân vật tĩnh. Không có metadata hay clip Idle/Walk/Attack/Hurt/Death; chỉ phù hợp dựng prototype/đồ họa tĩnh, chưa phải nhân vật hoàn chỉnh. | [Trang pack](https://kenney.nl/assets/roguelike-characters) và license local xác nhận CC0; dùng thương mại, không cần credit. |
| **SpriteCanvas, MonstersCreatures** | Các thư mục `spritecanvas-monsters-cc0` và `MonstersCreatures_v1.0` là quái/creature, không có hero Archer/Mage. SpriteCanvas local chỉ có hướng/clip quái; không nên tái phân vai thành class hero. | SpriteCanvas local ghi CC0; MonstersCreatures local cho phép game thương mại, credit tùy ý, nhưng hai license chỉ liên quan asset của từng pack. |
| **Willosuna Free Demo** | Có các `demo/heroes`/`demo_v2/heroes` trong source archive, nhưng không thuộc gói quái miễn phí. Không dùng các hero này. | [License/README của demo](https://willosuna.itch.io/pixel-rpg-monsters-top-down) ghi hero/map demo thuộc gói khác và nằm ngoài license Free Demo. License local `asset_sources/pixel-rpg-monsters-top-down-demo-v2-r2/LICENSE.txt` xác nhận điều này. |
| **GegX Archer + Mage (đề xuất để đánh giá ở 12B)** | Hai pack miễn phí cùng dòng: Archer 201 PNG, canvas 176×176, chân y=150; Mage 198 PNG, canvas 168×168, chân y=143; 8 hướng và 16 animation loop mỗi nhân vật. Trang mô tả Idle/Walk/static; không bao gồm đánh hay hurt/death. Đây là cặp mới hợp lý nhất về hướng nhìn và license, nhưng có mật độ pixel/khung lớn hơn Player. | Trang hai pack cho phép game thương mại/miễn phí, không cần credit; cấm bán/phân phối sprite thành asset pack. Tác giả ghi pack được AI hỗ trợ và biên tập thủ công. Các gói Attack/Combat bổ sung là trả phí; chưa tải/mua. |

**Ưu tiên chốt asset:** giữ nguyên Warrior local. So sánh GegX với asset Tiny Questers Mage/Elf Archer (nếu sau này xác minh điều khoản sản phẩm) bằng ảnh trong game trước khi chọn. Nếu dùng GegX bản miễn phí, có thể tái dùng Idle/Walk và fallback art cho các trạng thái còn thiếu; muốn đủ Attack/Hurt/Death phải xem xét license và chi phí của từng gói bổ sung. Không có asset mới nào được tải trong audit này.

## 2. Vũ khí, tiến trình và HUD hiện tại

- `WeaponType` hiện có Sword, Bow, Fireball, Lightning; `WeaponConfig` có 5 level cho từng loại; `EvolutionConfig` chứa evolution hiện tại. Bow đã tăng số bolt theo cấp **1→2→2→3→3**, Fireball gây nổ và evolution tạo meteor/fire zones, Lightning đã đánh chuỗi nhiều mục tiêu và có nhánh chain khi evolution, Sword là đánh tự động theo hướng trước mặt và evolution tạo orbit blades.
- Mỗi loadout có **3 slot**; hiện slot đầu là Sword Lv1. Weapon Box cung cấp tối đa 3 lựa chọn, ưu tiên evolution đủ điều kiện, nâng cấp/weapon mới rồi stat fallback. Skill Box hiện chỉ cho nâng Dash/Ultimate. Hai active skill này có Lv1–5; Dash cho dịch chuyển và invulnerability ngắn, Ultimate gây burst diện rộng/knockback.
- Đề xuất: truyền `CharacterId`/profile vào session; profile Warrior giữ nguyên mọi constructor mặc định và đường chơi cũ. Profile Archer khởi tạo Bow Lv1, Mage khởi tạo Fireball Lv1. Vũ khí đặc trưng chiếm một trong ba slot, vẫn nâng Lv1–5 và evolution theo đúng bảng hiện tại; hai slot còn lại dành cho progression hiện có. Không thêm `WeaponType` mới hoặc đổi thứ tự enum/config evolution.
- Weapon Box vẫn là nơi lấy/nâng vũ khí; danh sách offer cần xét loại khởi đầu và không tự phát Sword cho Archer/Mage. Có thể mở Sword như vũ khí phụ về sau, nhưng cần quy định animation đánh tương ứng; nếu chưa có animation, 12B nên lọc Sword khỏi offer của hai lớp mới. Với Warrior, giữ nguyên offer và xác suất/progression hiện hành.
- Bắt buộc sửa nhánh vũ khí không có Sword: `WeaponSystem.update()` đang gọi `sword.update()` và dereference `loadout.get(SWORD)`; `GameSession.chooseWeapon()` gọi `swordCooldown()` sau mọi loại reward. Làm SwordCombat optional/gated theo loadout, và bảo đảm Warrior vẫn đi đúng nhánh cũ. Không sửa stats/điều khiển Player gốc.

## 3. Mười kỹ năng đề xuất và FX

Danh sách dưới đây là **kỹ năng lớp mới**, tách khỏi 3 weapon slots. Giữ Dash/Ultimate dùng chung, điều khiển như hiện tại. Kỹ năng mới nên tự kích hoạt theo cooldown để không thêm nút điều khiển làm đổi layout; Skill Box cho học/nâng cấp, HUD hiển thị icon/level/cooldown riêng với hàng vũ khí.

| Lớp | Kỹ năng đề xuất | Chống trùng / FX sẵn có |
|---|---|---|
| Warrior | Xoáy Kiếm | Pulse 360° theo đợt, không phải orbit blades tồn tại liên tục; dùng `slash`, `spark`. |
| Warrior | Sóng Kiếm | Đường chém bay thẳng; khác Sword hiện tại đánh cận chiến; dùng `slash`/`arrow`. |
| Warrior | Đỡ Đòn Phản Kích *(thay Chém Lao Tới)* | Chặn phía trước rồi phản công; Chém Lao Tới trùng vai trò dịch chuyển của Dash. Dùng `spark`/primitive hiện có. |
| Archer | Đánh Dấu Thợ Săn *(thay Bắn Nhiều Tên)* | Bow đã có volley nhiều bolt ở các level cao; đánh dấu mục tiêu và tăng sát thương nhận trong thời gian ngắn sẽ khác cơ chế. Dùng `cast`/`spark`. |
| Archer | Ngắm Bắn Tập Trung *(thay Tên Xuyên Thấu)* | Bow đã tăng số mục tiêu xuyên từ 1 lên tối đa 3 theo level, nên kỹ năng xuyên tên sẽ trùng. Đòn ngắm chậm, sát thương đơn mục tiêu/ưu tiên boss tạo vai trò khác. Dùng `arrow`, `arrow_impact`. |
| Archer | Mưa Tên | Mưa projectile theo đợt vào vùng mục tiêu, không để lại fire zone lâu dài; dùng `arrow`, `quake`/`shockwave` làm cảnh báo/impact. |
| Mage | Băng Tiễn | Đạn đơn gây slow; dùng `ice_shard`/`snowball` và `overlay_slow`. |
| Mage | Bẫy Bí Thuật *(thay Sét Lan Truyền)* | Đặt rune nổ theo điều kiện; Lightning hiện đã chain và evolution đã chain nhiều nhánh. Dùng `cast`, `magic_orb_impact`, `blast`. |
| Mage | Khiên Phép | Giảm/chặn một phần sát thương trong cửa sổ ngắn; không trùng Dash vì không dịch chuyển. Có thể vẽ vòng khiên bằng primitive + `spark`, chưa cần asset mới. |
| Mage | Dồn Năng Lượng *(thay Vùng Lửa)* | Buff ngắn giảm cooldown Fireball/skills; Fireball evolution đã có fire zones. Dùng `cast`/glow hiện có. |

FX trong runtime đã có `assets/game/evolution-fx.png/json` (meteor, arrow, blast, slash, spark, burn; 4–6 frame) và `assets/game/enemy-fx.png/json` (arrow, ice shard, fireball, magic impact, slow overlay, quake/shockwave). Hai atlas FX này được cắt từ MonstersCreatures; license local `MonstersCreatures-Evolution.txt` và `MonstersCreatures-Enemy-FX.txt` cho phép game thương mại, credit không bắt buộc. Không cần tải FX mới cho danh sách trên. Giữ skill offers bất biến/token như `SkillSelection`; mỗi offer là học skill chưa có hoặc tăng level skill lớp hiện tại (đề xuất Lv1–5, chưa thêm evolution skill). Khi hết lựa chọn hợp lệ, retire/fallback Skill Box để không tạo reward rỗng. `SkillHud`/`SkillRewardHud` cần thêm nhãn, icon, level và thời gian hồi; không trộn skill vào ba weapon slots.

## 4. Xu và mở khóa

- Giá khởi điểm để cân chỉnh: **Archer 100 xu, Mage 220 xu**. Rơi xu từ quái thường: **8% × 1 xu**; elite rơi đảm bảo 3 xu; miniboss đảm bảo 8 xu; boss cuối đảm bảo 20 xu. Đây là điểm bắt đầu để cân bằng sau, không phải kết quả đo của run.
- Xu nhặt được chỉ cộng vào bộ đếm tạm của trận. Khi trận NORMAL hợp lệ kết thúc, gửi tổng xu trong `MatchResult`; lưu offline một lần bằng `SharedPreferences` qua `CharacterProgressManager`, khóa theo `matchId` UUID. Dùng `commit()` nguyên tử cho coin balance + ledger ID; không ghi ổ cứng mỗi lần nhặt. Demo, debug và diagnostic không tạo reward chính thức: chỉ nhận xu khi `mode == NORMAL`, `diagnostic == false`, không debug và matchId hợp lệ.
- Mặc định sở hữu/chọn Warrior; Archer/Mage mặc định khóa. Mua chỉ thành công một lần khi đủ tiền: kiểm tra số dư, trừ xu và thêm `unlockedCharacterIds` trong cùng transaction. Chọn ID không thuộc danh sách sở hữu phải bị từ chối/fallback Warrior cả ở UI lẫn lúc launch; không tin Intent để bypass khóa. Không xóa quyền sở hữu khi reset setting.
- Dùng SharedPreferences riêng (ví dụ `monster_survivor_character_progress`) để lưu `coins`, `unlockedCharacterIds`, `selectedCharacterId`, `processedMatchIds`; lưu ID ổn định, không lưu ordinal. Màn Character Select cho xem mô tả/giá với nhân vật khóa nhưng disable Start; Warrior luôn có thể bắt đầu trận. Có thể dùng icon xu Kenney RPG CC0 sau khi chọn đúng crop, hoặc vẽ icon đơn giản; chưa cần tải asset.

## 5. File dự kiến cho Task 12B

- **Android/profile:** `HomeActivity.java`, `GameModeActivity.java`, `AndroidLauncher.java`; thêm `CharacterSelectActivity.java`, layout/manifest/string song ngữ; thêm `CharacterProgressManager.java` và giữ dữ liệu độc lập với reset setting.
- **Core:** thêm profile/ID character; `GameSession.java`, `MatchResult.java`, `Player.java`/player rendering chỉ nếu cần chọn SpriteSet; `WeaponLoadout.java`, `WeaponSystem.java`, `SwordCombat.java`, `WeaponSelection.java`/`WeaponOffer.java`; `PickupType.java`, `PickupSystem.java`, `PickupRenderer.java`; `skill/SkillType.java`, `SkillSelection.java`, `SkillOffer.java`, hệ thống resolve/render và `SkillHud`/`SkillRewardHud`.
- **Asset/localization:** `assets/game/catalog.json`, `GameAssets.java`, atlas/runtime metadata mới nếu chốt asset; `assets/game/licenses/` và `CREDITS.txt` theo license đã chọn; icon HUD và chuỗi VI/EN. Không chỉnh hoặc thay file Warrior gốc.

