# Monster Survivor — Đặc tả V2: Kỹ năng bị động, Rương chung, Đột phá và Cửa hàng bình

**Ngày chốt:** 17/11/2026  
**Mục đích:** Đặc tả thiết kế cho Codex triển khai, ưu tiên tái sử dụng code hiện có. Tài liệu này **thay thế phần quy hoạch kỹ năng/rương** trong `MONSTER_SURVIVOR_TASK12_GAME_DESIGN.md`; phần asset/nhân vật/xu không mâu thuẫn thì giữ nguyên. Không phải báo cáo xác minh source sau Task UI07.

## A. Quy ước và cấu trúc nhân vật

- **Kỹ năng bị động** = mọi đòn/tác dụng tự kích hoạt, kể cả Sword/Bow/Fireball/Lightning và các class skill. Không dùng thuật ngữ “vũ khí” cho các ô HUD/phần thưởng nữa; trong code cũ có thể giữ tên class `Weapon*` để hạn chế refactor.
- **Kỹ năng chủ động** = hai nút có sẵn **Lướt (Dash)** và **Tuyệt Kỹ (Ultimate)**, mặc định Lv1, nâng đến Lv5, không đột phá trong giai đoạn này.
- Mỗi nhân vật có **catalog 5 kỹ năng bị động độc quyền**: **1 có sẵn Lv1** và **4 kỹ năng có thể học**. Một trận trang bị **tối đa 3 kỹ năng bị động đồng thời**, gồm kỹ năng mặc định chiếm ô đầu + tối đa 2 kỹ năng học từ bốn lựa chọn. Những kỹ năng chưa học vẫn có thể được đề xuất khi còn ô; khi hết ô, chỉ đề xuất nâng cấp/đột phá kỹ năng đã học. Điều này phù hợp **ba ô hiện hữu ở hàng dưới bên trái** và ba nút bình dưới chúng. Không ngầm chuyển sang 5 ô HUD.
- Mỗi bị động Lv1–5. Đạt Lv5 thì **mới đủ điều kiện** xuất hiện đề nghị đột phá khi mở rương; chỉ đột phá nếu người chơi chọn. Giữ Lv5 và cờ `evolved`, không tạo Lv6. Mỗi kỹ năng có đúng **một hình thái đột phá**. Mỗi trận có tối đa ba kỹ năng bị động được đột phá.
- **Không học chéo nhân vật:** Warrior không được cung/phép, Archer không được kiếm/phép, Mage không được kiếm/cung. Giữ sprite Warrior hiện tại nguyên trạng; Archer freddyka, Mage GandalfHardcore.
- Không đổi Enemy AI, Boss, Endless, dash iframe, điều kiện thắng, audio selection, giá mở khóa Warrior miễn phí / Archer 100 xu / Mage 220 xu.

### Catalog 15 kỹ năng

| Nhân vật | Bị động mặc định | Bốn bị động có thể học |
|---|---|---|
| **Warrior** | **Chém Kiếm** (Sword) | Xoáy Kiếm (Whirlwind), Sóng Kiếm (Sword Wave), Đỡ Đòn Phản Kích (Parry Counter), **Chiến Hống (War Cry) mới** |
| **Archer** | **Bắn Cung** (Bow) | Đánh Dấu Thợ Săn (Hunter's Mark), Ngắm Bắn Tập Trung (Focus Shot), Mưa Tên (Arrow Rain), **Tên Độc (Poison Arrow) mới** |
| **Mage** | **Cầu Lửa** (Fireball) | Băng Tiễn (Ice Shard), Bẫy Bí Thuật (Arcane Trap), Khiên Phép (Magic Shield), **Sét (Lightning) bổ sung** |

- Kỹ năng **Dồn Năng Lượng (Arcane Channel)** đã tồn tại trong code Mage hiện tại: **không còn là offer/ô bị động**. Dùng lại ý tưởng thành nội tại của **Cầu Lửa đột phá**: sau mỗi 3 lần Fireball cast thành công, kích hoạt **tăng tốc hồi riêng Fireball 10% trong 3 giây**, với thời gian chờ nội tại tối thiểu 15 giây. Không tác động đến Ultimate/các kỹ năng khác; không cộng dồn với chính nó. Mục tiêu là không xóa phần hữu ích nhưng cũng không có kỹ năng thứ sáu.
- Các nội tại bổ sung từ buff bình áp dụng theo giới hạn ở mục D, tránh cộng dồn tốc độ hồi không kiểm soát.

## B. Tuning hai kỹ năng mới

### Warrior — Chiến Hống / War Cry

- Tự kích hoạt khi **ít nhất 4 quái còn sống trong bán kính 70**; Boss tính là 1 mục tiêu, không tự kích hoạt nếu không đủ bốn mục tiêu. Không phát khi đang Pause/modal/Game Over.
- **Cooldown Lv1–5**: `[18,17,16,15,14]` giây; **thời lượng** `[4,4.5,5,5.5,6]` giây.
- Khi kích hoạt: đòn đánh **bị động của Warrior** cộng **+1 sát thương trực tiếp** (không cộng cho burn/poison DoT), đồng thời giảm **10/12/14/16/18% sát thương trực tiếp nhận vào** theo cấp, vẫn theo damage pipeline và boss burst guard hiện có. Không làm player bất tử.
- Hiện FX `spark`/quầng nhỏ, không thêm SFX. Mỗi buff chỉ có một instance, không tự cộng dồn. Khi chết/Retry/hết trận phải clear.

### Archer — Tên Độc / Poison Arrow

- Tự bắn **một mũi tên độc** vào quái gần nhất đủ LOS trong bán kính 150, ưu tiên quái chưa bị độc; **hướng cố định lúc bắn**, tường chặn; tốc độ 165, range 150.
- **Cooldown Lv1–5** `[13,12,11,10,9]`; **damage trúng trực tiếp** `[1,1,2,2,3]` theo class-skill damage pipeline.
- Nếu direct hit được chấp nhận, áp độc **1 damage mỗi 1 giây**, trong `[2,2,2.5,2.5,3]` giây (tối đa 2/2/2/2/3 tick); độc không cộng stack, có thể refresh thời lượng hữu hạn. Damage DoT đi qua damage pipeline và Boss guard; không kích hoạt thưởng kill hai lần. Độc không gây ảnh hưởng lên Player.
- FX dùng arrow/spore/poison có sẵn; pool hữu hạn, không spawn allocations liên tục.

## C. 15 đột phá bị động: giữ 4 cũ, bổ sung 11 mới

**Giữ nguyên 4 cơ chế hiện có**, chỉ hạn chế theo class và đổi nhãn trên UI nếu cần:

1. Warrior **Chém Kiếm → Vòng Kiếm (Blade Orbit)**: hai kiếm quay và gây hit theo cooldown hiện có.
2. Archer **Bắn Cung → Bão Tên Quạt (Arrow Storm)**: fan năm tên hiện có, phân biệt với class skill “Mưa Tên”.
3. Mage **Cầu Lửa → Thiên Thạch Lửa (Inferno Meteor)**: meteor + vùng cháy hiện có; thêm nội tại Arcane Channel mô tả ở mục A.
4. Mage **Sét → Lôi Võng (Thunder Web)**: chain roots/hops hiện có, không cho class khác học.

**11 đột phá mới**, luôn dùng đúng Lv5 base tuning làm nền, chỉ cộng thay đổi ghi dưới; dùng FX hiện có, không cần asset mới:

| Bị động Lv5 | Tên đột phá | Cơ chế bổ sung sau khi chọn |
|---|---|---|
| Warrior Xoáy Kiếm | **Lốc Kiếm Kép** | Thay một pulse bằng **hai pulse 360°** cách nhau 0,28s; mỗi mục tiêu tối đa một hit/pulse, đợt hai gây 75% damage gốc (ít nhất 1); giữ cooldown Lv5. |
| Warrior Sóng Kiếm | **Tam Liên Kiếm Khí** | Phóng **3 sóng thẳng** cùng lúc ở góc −12°/0°/+12°; mỗi mục tiêu tối đa 2 hit/cast; mỗi sóng dùng damage Lv5 và pool cap; không homing. |
| Warrior Phản Kích | **Thánh Thuẫn Phản Đòn** | Chặn **một direct hit từ 360°** thay vì chỉ hướng trước; sau khi chặn phát shockwave R34 gây damage phản công Lv5 một lần/mục tiêu; vẫn không chặn DoT/Dash-iframe. |
| Warrior Chiến Hống | **Chiến Hống Chiến Thần** | Khi kích hoạt, thêm một pulse **knockback quái thường R40** (không hất Boss), kéo dài buff thêm 1s; giữ cooldown gốc. |
| Archer Đánh Dấu | **Ấn Săn Mồi** | Khi mục tiêu bị đánh dấu chết, **chuyển dấu sang quái gần nhất trong R65 đúng một lần/activation**; kế thừa thời gian còn lại và tối đa 2 lượt bonus chưa dùng; không tạo chuỗi lan vô tận. |
| Archer Ngắm Bắn | **Xuyên Tâm Liên Tiễn** | Một tên tập trung **xuyên tối đa 2 mục tiêu**, mục tiêu thứ hai nhận 70% damage (ít nhất 1); bay thẳng/swept collision; Boss guard vẫn áp dụng. |
| Archer Mưa Tên | **Thiên Vũ** | Thêm **1 đợt mưa tên** vào chu kỳ Lv5; giới hạn tổng tối đa **3 hit hợp lệ/mục tiêu/activation**, không tạo burning ground. |
| Archer Tên Độc | **Độc Tiễn Lây Lan** | Khi mục tiêu đang chịu độc từ Tên Độc chết, phát tán độc sang **tối đa 2 quái gần nhất trong R45 một lần**, không lan tiếp từ mục tiêu thứ cấp; không stack DoT. |
| Mage Băng Tiễn | **Băng Vỡ Liên Hoàn** | Sau hit hợp lệ, tạo **3 mảnh băng nhỏ** tỏa từ điểm va chạm, mỗi mảnh tối đa một hit, damage bằng 50% Ice Shard Lv5 (ít nhất 1); Slow của mảnh chỉ 50% hiệu lực Slow gốc; tường chặn. |
| Mage Bẫy Bí Thuật | **Ma Trận Liên Hoàn** | Khi bẫy nổ, tạo **2 pulse** cách 0,35s; mỗi quái tối đa một hit/pulse, vẫn tôn trọng LOS và tối đa 2 bẫy đang tồn tại. |
| Mage Khiên Phép | **Khiên Phản Phép** | Sau khi chặn thành công một direct hit, phát **shockwave R30 damage1** lên quái quanh Player, một lần/lần chặn; không chặn DoT, không gây ảnh hưởng Player. |

**Cân bằng/giới hạn chung:** Mọi hit vẫn tuân thủ immunity, owner/team, LOS/collision hợp lý và guard sát thương Boss; không gây multi-hit vô hạn, không tạo pool vô hạn hoặc đột phá tự động. Đột phá chỉ được đề xuất qua rương sau Lv5 và một lần mỗi kỹ năng.

## D. Một rương chung thay hai loại

- **Chỉ còn một loại rương trên map** (tên hiển thị “Rương Báu” / “Treasure Chest”), tái sử dụng art/texture hiện có. Retire hai spawn/offer UI Weapon Box và Skill Box thành một pipeline; không để hệ thống cũ sinh song song hoặc phát thưởng hai lần. Giữ phương án rơi/nhặt, modal, token stale/double tap hiện có.
- Sau khi nhặt: **ngẫu nhiên nhánh 65% bị động / 35% chủ động**; trong nhánh bị động, tối đa 3 lựa chọn hợp lệ gồm học skill mới nếu còn ô, nâng Lv1→5, và **đột phá Lv5 nếu chưa evolved**. **Ưu tiên hiện ít nhất một lựa chọn đột phá** khi skill đủ điều kiện trong nhánh bị động. Nhánh chủ động chỉ chọn nâng Dash/Ultimate Lv1→5.
- Nếu nhánh được random đã hết offer hợp lệ, **chuyển sang nhánh còn lại**; nếu cả hai đã max, dùng stat reward hiện có. Không rương rỗng. Modal hiển thị rõ nhánh và tên/lv/đột phá của offer. Đừng cho offer cùng skill hai lần trong một modal.
- **Lịch rương gợi ý**: một rương mỗi **18–24 giây theo progress gameplay** (không phải wall-clock của thiết bị), tối đa 2 rương tồn tại, TTL60 giây; Normal ~25–33 cơ hội trong 10 phút; Demo dùng progress accelerated có sẵn; Endless tiếp tục vô hạn. Nếu lịch hiện có đảm bảo số lượng tương đương, có thể dùng lịch đó; **không được cộng cả hai lịch cũ**. Pause/modal dừng tiến trình. Không phát rương từ debug fixture trong Normal.
- **Skill độc quyền từng nhân vật**, mỗi trận 3 ô max; không thể mở ô thứ tư thông qua rương hay fallback. Skill mặc định chiếm ô1. Core xác thực offer và commit, UI không được quyết định loại/class.
- Giữ 4/11 evolution ở trên; nếu có thói quen metadata `WeaponType`/`ClassSkillId` cũ, tạo adapter chung, không xóa nguyên hệ thống vũ khí khi có thể tái sử dụng.

## E. Cửa hàng và 3 loại bình tiêu hao

### Bảng bình và hiệu ứng

| Vật phẩm | Giá xu | Dùng trong trận | Giới hạn |
|---|---:|---|---|
| **Bình Máu** (Health Potion) | **10 xu** | Hồi **4 HP** ngay, không vượt `maxHP`; chỉ dùng khi HP < maxHP | Không hồi sinh; không dùng khi đầy máu |
| **Bình Tăng Tốc** (Speed Potion) | **15 xu** | +**25% tốc độ di chuyển bình thường trong 8 giây gameplay**; không tăng Dash distance/iframe | Không tự cộng dồn với cùng bình; hết buff mới dùng lại |
| **Bình Giảm Hồi Chiêu** (Cooldown Potion) | **20 xu** | **Tốc độ chạy bộ đếm hồi chiêu ×1,25 trong 8 giây gameplay** (tương đương thời gian hồi thực tế ngắn hơn khoảng20%) cho **Dash, Ultimate và 3 bị động đã học** | Không ảnh hưởng telegraph/windup/DoT, cooldown bình hoặc spam cast trong cùng frame; hết buff mới dùng lại |

- Giá dùng chung số dư hiện có từ `CharacterProgressManager`; **không tạo ví xu thứ hai**. Giá mua nhân vật vẫn Archer100/Mage220. Inventory bình lưu offline theo loại, **tối đa 9 bình/loại**. Không tạo nguồn bình miễn phí từ drop trong task này.
- **Shop** mở từ Home/GameMode bằng nút “Cửa hàng” / “Shop”, giữ bố cục/theme Native hiện có. Có 3 card bình, mô tả ngắn, giá, số đang sở hữu và nút Mua. Mua một lần = trừ xu và tăng 1 bình trong **một transaction commit**, không âm tiền/nhân đôi item khi double tap. Không thay đổi thống kê Task09.
- Bình đã mua **không tự mất khi kết thúc trận** nếu chưa dùng. Khi dùng phải **trừ 1 đơn vị từ inventory thực trước** rồi mới áp dụng hiệu ứng. Dùng bình là hành động tiêu hao ngay, **không quyết toán sau trận**, tránh thoát Home/force close để nhân bình. Không dùng khi Pause/modal/Game Over, khi đang chờ xác nhận mua hoặc khi inventory0. Với bình Máu không trừ khi HP đầy.
- Để không làm khựng render, thực hiện persistent consume qua Android bridge ngoài render loop/worker thích hợp; chỉ apply effect sau khi báo thành công về game thread. Ngăn double tap/duplicate callbacks bằng request token một lần. Có thể dùng cách đồng bộ tối giản nếu bảo đảm chỉ thao tác hiếm và không gây treo GL; không tự thêm backend/database.
- **Chỉ sử dụng bình trong Normal/Endless trận thật**. Trong Demo/diagnostic: nút disabled, không tiêu inventory. Timer buff dừng khi Pause/modal và reset khi Retry/Session kết thúc; bình chưa dùng vẫn nằm trong ví. Buff tốc độ và giảm hồi chiêu có thể cùng hoạt động, cùng loại không stack; các modifier cũ giữ logic gốc. Với Cầu Lửa đột phá + buff giảm hồi chiêu, tổng tốc độ hồi tối đa ×**1,40**, không vượt quá.
- **HUD**: Giữ đúng **3 ô kỹ năng bị động hiện có** ở hàng dưới. Đặt **3 nút nhỏ** tương ứng Bình Máu / Bình Tăng Tốc / Bình Giảm Hồi Chiêu **ngay bên dưới hàng 3 ô này**, mỗi nút có icon, số lượng còn lại và trạng thái có thể dùng; không che joystick, nút Dash/Ultimate, boss bar hoặc nội dung game; canh landscape theo safe area. Không tạo một hàng kỹ năng mới ở góc phải; chỉ 2 kỹ năng chủ động ở góc phải.
- HUD mua/bán chỉ hiện tên thân thiện với người chơi; không để nhãn `Weapon`, `ClassSkill`, `Fixture`, `Task` ở bản chơi thường.

## F. Cách chia task cho Codex — không làm tất cả trong một prompt

**13A — Hợp nhất bị động và rương:** chuyển 3 slot cũ thành 3 passive-only slots của class; chuyển hệ hai rương thành một rương 65/35; disable skill cross-class; Warrior/Sword, Archer/Bow, Mage/Fireball khởi đầu; đưa Lightning vào Mage; gỡ Channel khỏi offers. Giữ legacy 4 evolution. Chưa làm evolution mới hoặc shop.

**13B — Hoàn thiện hai kỹ năng mới:** thêm War Cry cho Warrior, Poison Arrow cho Archer theo tuning; bảo đảm không ảnh hưởng 10 class skills cũ và hệ rương mới.

**13C — 11 đột phá mới:** thêm 11 cơ chế chính xác ở mục C, tái dùng pools/FX/damage hooks. Nếu scope lớn, chia Warrior/Archer/Mage thành 3 lượt riêng; không sửa shop.

**13D — Shop & potions:** thêm inventory bền vững vào cùng ví xu, Native Shop 3 thẻ bám theme có sẵn, 3 nút dưới 3 passive HUD, bridge tiêu hao an toàn, 3 hiệu ứng bảng E.

**13E — Chỉnh chữ/UI cuối:** chỉ chỉnh bố cục để toàn bộ HUD, Shop, Modal rõ ràng trên landscape, bản dịch VI/EN, không thêm UI trang trí nặng.

**Mỗi giai đoạn:** Codex chỉ compile core/android và một smoke rất ngắn liên quan; không emulator automation, stress benchmark, ngàn assertions, không APK/AAB, không commit/push; chủ dự án tự chạy Android Studio và báo lỗi. Bàn giao 5–10 dòng, không làm báo cáo kỹ thuật dài.

## G. Ưu tiên giữ tương thích

1. Không thay asset Player Warrior; không sửa asset Archer/Mage hay phạm vi license.
2. Giữ 3 nhân vật và quyền sở hữu mở bằng xu; cho UI chọn nhân vật hoạt động.
3. Giữ Boss, Endless Mode, Enemy Skills, Weapon/skill balance đã chốt; thay đổi chỉ liên quan hợp nhất slot/rương/bị động.
4. Khi chọn skill khác type, không được cấp Sword ngầm, duplicate Bow/Fireball, hay cast class skill không học.
5. Khi GameSession reset, reset skill levels, evolved flags, buff, chest selection; **không reset coin wallet, character unlock hay inventory bình đã mua**.
6. Chỉ normal result tiếp tục cấp achievement Task09 như trước; không nhập Demo/Endless vào `official_v1`.
