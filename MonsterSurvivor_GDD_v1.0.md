# MONSTER SURVIVOR — GAME DESIGN DOCUMENT (GDD) v1.0

**Ngày lập:** 13/11/2026  
**Trạng thái:** Thiết kế tiền triển khai; chưa phải kết quả kiểm thử gameplay  
**Nền tảng:** Android (chạy offline trên Android Emulator, laptop Windows)  
**Stack:** Java + XML/AndroidX/Material Components (UI native) + libGDX (gameplay); gdx-ai/gdx-freetype khi cần  
**Độ ưu tiên:** Chơi được và ổn định > trải nghiệm rõ ràng > hiệu ứng đẹp > mở rộng tính năng.

> **Quy ước:** `[CHỐT]` là quyết định được người dùng xác nhận trong hội thoại. `[ĐỀ XUẤT]` là thiết kế mặc định cần playtest/duyệt khi triển khai. `[MỞ]` là điểm chưa quyết định hoặc có rủi ro. Agent không được biến đề xuất thành yêu cầu bất biến.

## 1. Tóm tắt dự án

Monster Survivor là game hành động sinh tồn 2D góc nhìn top-down, 1 người chơi, offline. Người chơi điều khiển chiến binh trên một bản đồ rừng 100×100 ô; vũ khí tự động đánh quái, Dash và Ultimate kích hoạt thủ công. Quái xuất hiện ngoài vùng nhìn, truy đuổi bằng steering trực tiếp khi thông thoáng và A* khi gặp vật cản. Hộp nâng cấp xuất hiện gần người chơi mỗi 15–20 giây. Qua phút 5, thế giới chuyển sang đêm, có vùng sáng quanh nhân vật và quái mạnh hơn. Phút 9 xuất hiện boss Lich Lord. **Thắng chỉ khi đã đạt 10 phút sinh tồn VÀ hạ Lich Lord**. Nếu sau phút 10 boss còn sống, đồng hồ tiếp tục chạy, quái thường ngừng spawn (quái cũ còn tồn tại, boss vẫn có thể triệu hồi theo giới hạn).

**Đối tượng demo:** Giảng viên chấm trực tiếp, ứng dụng chạy offline trong Android Emulator. Có Demo Mode khoảng 90 giây, nén các mốc sự kiện nhưng giữ tốc độ di chuyển/đạn/animation bình thường.

## 2. Phạm vi MVP và ngoài phạm vi

### 2.1 Bắt buộc [CHỐT]
- Một bản đồ rừng **100×100 tile**, có biên giới; đồng cỏ rộng ở trung tâm, rừng và chướng ngại xung quanh.
- Một nhân vật chính; camera theo nhân vật; joystick ảo 360°.
- 10 loại kẻ địch: 9 thường + Lich Lord boss; giới hạn **60 quái thường hoạt động đồng thời** (boss được theo dõi riêng).
- Vũ khí tự động, tối đa **3 loại được trang bị cùng lúc**. Danh mục 4 vũ khí: kiếm, cung, cầu lửa, sét. Vũ khí tối đa cấp 5, có thể tiến hóa một lần sau khi đạt cấp 5.
- Hai nút thủ công Dash và Ultimate; hồi chiêu lần lượt 5s và 30s.
- Hộp Damage/Health/Speed/Weapon xuất hiện mỗi 15–20s, thời gian tồn tại 60s, nhấp nháy trước khi biến mất; có mũi tên chỉ hộp. Không có XP/level nhân vật.
- Kẻ địch có hành vi khác nhau, xuất hiện theo loại được mở khóa dần. Độ khó tăng liên tục theo thời gian cộng các đợt đặc biệt.
- Đêm từ phút 5; có vùng sáng quanh nhân vật và quái đêm mạnh hơn; không chuyển/khởi tạo lại map.
- Boss xuất hiện phút 9, có hai phase (phase 2 dưới 50% HP), dùng phép và triệu hồi có giới hạn.
- Hai mức khó có nhãn **Easy** và **Hard** (Hard được cân bằng gần mức medium, không quá khắc nghiệt).
- Màn hình menu Android native, hỗ trợ Options Menu, ContextMenu, SubMenu, Navigation Bar, animation, event listeners và Intent đúng yêu cầu học phần.
- Có thống kê kết quả, lưu offline, Pause đầy đủ, Demo Mode ngắn.

### 2.2 Không làm trong MVP [ĐỀ XUẤT]
- Không multiplayer/.io online, không backend/Docker, không cloud, không quảng cáo/in-app purchase.
- Không hệ thống XP, cây kỹ năng, tiền tệ hoặc mở khóa vĩnh viễn.
- Không world generation vô hạn, không bản đồ thứ hai, không tài khoản.
- Không cố mở 150–300 quái nếu chưa có số liệu FPS.

## 3. Luồng người chơi

`Home → Chọn chế độ (Normal 10 phút / Demo 90 giây) → Chọn độ khó (Easy / Hard) → Play → Gameplay → (Pause / Weapon Choice / Game Over / Victory) → Thống kê → Home hoặc Replay`

- **Normal:** đồng hồ trận tăng theo thời gian gameplay không-paused; mốc đêm 05:00; boss 09:00; tại 10:00 dừng spawn thường, đồng hồ không dừng. Chỉ có Victory sau khi thời gian ≥10:00 và boss đã chết.
- **Boss chết trước 10:00:** [ĐỀ XUẤT] vẫn sống sót đến 10:00 mới thắng; không respawn boss.
- **HP người chơi về 0:** Game Over ngay, bất kể đã đạt mốc thời gian nào.
- **Demo:** mốc sự kiện nén 600 giây thời gian trận thành ~90 giây thực, sử dụng đồng hồ tiến trình ảo để hiển thị/điều kiện; tốc độ vật lý, cooldown thực, projectile và animation giữ nguyên. Tại mốc ảo 10:00, quy tắc boss tương tự Normal; có thể kéo dài hơn 90 giây nếu boss còn sống. Hiển thị nhãn `DEMO` rõ ràng. Không cộng kỷ lục Demo vào kỷ lục Normal [ĐỀ XUẤT].
- **Pause/Weapon Choice:** đóng băng đồng hồ gameplay, AI, physics, tấn công, cooldown, spawn và thời gian tồn tại hộp; UI vẫn tương tác. Resume không tạo cú nhảy delta.

## 4. Nhân vật và điều khiển

### 4.1 Chỉ số người chơi [CHỐT]
| Tham số | Ban đầu | Ghi chú |
|---|---:|---|
| HP hiện tại / HP tối đa | 10 / 10 | Số nhỏ, dễ đọc |
| Speed (chỉ số UI) | 10 | Quy đổi sang world units/second qua cấu hình, không hiểu là 10 px/s |
| Base Damage | 1 | Áp dụng vào công thức mỗi vũ khí |
| Giới hạn Speed | 20 | Speed Box +1, không vượt quá 20 |
| Miễn nhiễm sau hit | 0,7 giây | Nhấp nháy để phản hồi |
| Dash cooldown | 5 giây | Nút riêng |
| Ultimate cooldown | 30 giây | Nút riêng |

### 4.2 Điều khiển [CHỐT + ĐỀ XUẤT]
- Joystick ảo bên trái; bên phải hai nút Dash và Ultimate. Auto-attack không cần nút bắn.
- [ĐỀ XUẤT] Dash lướt theo hướng di chuyển gần nhất; khi không có input thì dùng hướng nhìn cuối cùng. Có thời gian lướt ngắn và giới hạn va chạm địa hình; không đi xuyên vật cản.
- [ĐỀ XUẤT] Ultimate gây nổ diện rộng có telegraph/hiệu ứng, sát thương có cấu hình và không miễn nhiễm toàn bộ quái.
- Nhân vật bị chặn bởi cây, đá, nước; quái bay có thể vượt một số vùng nước.
- Tách **UI Speed** (10–20) khỏi **movement speed** tính bằng units/second; game loop dùng delta-time hoặc fixed timestep.

## 5. Chiến đấu / vũ khí

### 5.1 Vũ khí [CHỐT]
- Bắt đầu với **kiếm**; auto-attack.
- 4 vũ khí có thể thu thập: **Kiếm / Cung / Cầu lửa / Sét**.
- Tối đa 3 vũ khí trang bị một lúc. Khi đủ 3, Weapon Box chỉ đưa ra lựa chọn nâng cấp cho vũ khí đang có.
- Weapon Box dừng trận và đưa ra **3 lựa chọn ngẫu nhiên hợp lệ**, người chơi chọn đúng một. Nếu pool còn dưới 3 mục, giao diện hiển thị số mục thực tế hoặc các nâng cấp lặp *chỉ khi luật cho phép*; không tạo lựa chọn vô hiệu.
- Mỗi vũ khí đạt cấp 5; sau đó có thể mở một lần tiến hóa. Khi max và đã tiến hóa, bỏ khỏi pool nâng cấp.
- **Kiếm:** giai đoạn đầu tự động chém hướng quái gần nhất, nâng cấp cao mở dạng kiếm xoay quanh nhân vật. Không nhầm với cơ chế bắt đầu bằng kiếm xoay ngay ở cấp 1.

### 5.2 Bảng cơ chế [ĐỀ XUẤT; playtest]
| Vũ khí | Cơ chế | Lộ trình cấp 1–5 | Tiến hóa một lần |
|---|---|---|---|
| Kiếm | Chém tự động theo quái gần nhất | C1: 1 nhát/1,2s; C2 tăng tầm; C3 giảm hồi; C4 thêm chém; C5 mở kiếm xoay | Blade Storm: tăng số kiếm/tầm xoay |
| Cung | Bắn projectile nhắm quái gần nhất | C1: 1 mũi/1,5s; sau tăng số mũi, xuyên/giảm hồi | Rain of Arrows |
| Cầu lửa | Projectile nổ bán kính nhỏ | Tăng bán kính, số đạn và hiệu ứng cháy | Inferno Orb |
| Sét | Đánh trúng mục tiêu tức thời/chain | Tăng mục tiêu liên kết, phạm vi và tần suất | Thunder Chain |

**Công thức gợi ý (chưa chốt):** `damage = max(1, round(baseDamage * weaponMultiplier * levelMultiplier))` cho từng hit. Tránh tăng vô hạn theo số hộp: mức damage +1..+3 đã chốt, nhưng **trọng số hộp, số hộp có mặt và HP quái** cần thử nghiệm để tránh quá mạnh. Không âm thầm đổi hộp damage thành +%.

### 5.3 Quy tắc combat [CHỐT]
- Quái thường chủ yếu gây sát thương khi tiếp xúc, có cooldown hit; quái đặc biệt và boss thực hiện đòn riêng có tín hiệu rõ ràng.
- Nhận hit: mất HP rồi miễn nhiễm 0,7s (nhấp nháy), tránh nhiều cú chạm đồng thời cộng dồn trong một frame.
- Hồi máu chỉ từ Health Box và vật phẩm máu do quái rơi; không tự regen.

## 6. Hộp và vật phẩm

### 6.1 Bốn loại hộp [CHỐT]
| Hộp | Hiệu ứng |
|---|---|
| Damage | Tăng **ngẫu nhiên +1, +2 hoặc +3** Base Damage, áp dụng cho toàn bộ vũ khí |
| Health | Ngẫu nhiên **hồi HP** hoặc **tăng Max HP**, có phản hồi rõ hiệu ứng |
| Speed | Tăng **+1 Speed**, tối đa **20** |
| Weapon | Tạm dừng trận, chọn 1 trong tối đa 3 nâng cấp/vũ khí hợp lệ |

### 6.2 Quy tắc xuất hiện [CHỐT + ĐỀ XUẤT]
- Spawn ngẫu nhiên mỗi **15–20s gameplay**, trong vùng gần người chơi; có thể trong hoặc ngoài camera, có mũi tên chỉ đến hộp.
- Không nằm trên ô cản, nước, ngoài map hoặc vùng không thể tiếp cận; kiểm tra reachability từ vị trí người chơi (flood fill/A* khi cần).
- Hộp tồn tại 60s gameplay, nhấp nháy trong khoảng 10s cuối [ĐỀ XUẤT]; chạm hộp để nhặt.
- [ĐỀ XUẤT] Giới hạn 3–5 hộp đồng thời nhằm tránh tích tụ/che khuất; số lượng chính xác tùy playtest.
- **Vật phẩm máu từ quái:** xác suất rơi cơ bản có cấu hình; tăng nhẹ khi HP người chơi thấp; tự nhặt khi tiếp xúc; không áp dụng cho boss reward trừ khi thiết kế riêng.

## 7. Bản đồ / vật cản / ánh sáng

### 7.1 World [CHỐT]
- Map duy nhất **100×100 tile**, tile source 16×16; bắt đầu ở đồng cỏ trung tâm; cây, đá, hồ và rừng tăng dần về phía ngoài.
- Cây/đá/nước chặn người chơi và quái mặt đất; một số quái bay đi qua nước (không xuyên mọi vật cản).
- Camera bám theo nhân vật và giới hạn trong biên map.
- Minimap góc màn hình hiển thị **người chơi và boss**; vị trí được ánh xạ đúng hệ tọa độ map.
- Mũi tên chỉ các hộp cần thiết/đang nằm ngoài camera; ưu tiên chỉ hộp gần nhất khi quá nhiều dấu chỉ [ĐỀ XUẤT].

### 7.2 Chuyển đêm [CHỐT]
- Bắt đầu lúc 05:00 (hoặc mốc tương ứng Demo). Không đổi layout/chướng ngại.
- Chuyển ánh sáng mượt, màn hình tối hơn nhưng luôn có vùng sáng quanh nhân vật.
- Quái ban đêm mạnh hơn và có thể xuất hiện các loại chưa có ở ban ngày.
- [ĐỀ XUẤT] Không dùng hiệu ứng tối che hết HUD, đường đi hoặc dấu nguy hiểm; ưu tiên overlay/light mask nhẹ, tương thích Android/OpenGL.

## 8. Kẻ địch và AI

### 8.1 Danh sách 9 thường + 1 boss [CHỐT]
Tất cả quái thường sử dụng sprite Willosuna top-down x1 và metadata JSON đã kiểm kê; danh sách vai trò dưới đây là **đề xuất gameplay**, không phải metadata sprite.

| Quái | HP Hard nền [ĐỀ XUẤT] | Damage nền [ĐỀ XUẤT] | Hành vi [ĐỀ XUẤT] |
|---|---:|---:|---|
| Honey Slime | 2 | 1 | Chậm, đi nhóm |
| Wild Boar | 4 | 1 | Lao thẳng theo đợt, báo trước |
| Giant Bee | 2 | 1 | Nhanh, có thể bắn nọc, bay qua nước |
| Red Cap | 4 | 1 | Tiếp cận và gây độc ngắn |
| Red Crab | 6 | 1 | Giáp cao, di chuyển chậm |
| Giant Frog | 5 | 1 | Nhảy vào khu vực mục tiêu, có báo trước |
| Rock Golem | 12 | 2 | Tank chậm |
| Living Armor | 10 | 2 | Elite đánh vùng có telegraph |
| Bone Hound | 6 | 2 | Truy đuổi theo nhóm |
| Lich Lord | **250** (Hard) | 3 | Boss pháp thuật, triệu hồi, phase 2 <50% HP |

**Lưu ý:** Boss HP Hard 250 là quyết định đã chọn; số HP còn lại, damage và chi tiết vai trò là cấu hình đề xuất. Boss Easy tạm 200 HP [ĐỀ XUẤT].

### 8.2 AI tránh vật cản [CHỐT]
- Di chuyển theo vector đến player khi có line-of-sight/đường trống; khi bị cản dùng **A*** trên lưới cản của Tiled.
- Không chạy A* mỗi frame cho mọi quái. Cache path, stagger update, có ngân sách pathfinding/frame; cập nhật khi mục tiêu/đường đi thay đổi đáng kể; phương án flow field/distance field chia sẻ nếu cần.
- Quái va chạm nhẹ và tách nhau (separation/steering), không xuyên/chồng hoàn toàn; không dùng physics nặng cho từng quái nếu không cần.
- Kiểm tra lớp chướng ngại riêng với sprite; quái bay có movement mask khác với quái mặt đất.
- Nhiều hành vi khác nhau nhưng dùng entity base + thành phần hành vi (data-driven), không nhân bản 10 hệ thống AI.

### 8.3 Spawn và lịch mở khóa [CHỐT + ĐỀ XUẤT]
- Spawn **ngoài vùng camera**, gần người chơi, tránh vị trí không hợp lệ và tránh hiện ngay trong camera.
- Khởi đầu **5–10 quái**, tối đa **60 quái thường hoạt động đồng thời**; không tự sửa giới hạn do FPS thấp; tối ưu render/AI thay vào đó.
- Quái được mở khóa từng loại theo thời gian; số lượng/HP/damage/speed tăng từng giai đoạn, đồng thời có đợt đặc biệt.
- [ĐỀ XUẤT] Lịch mở khóa ban đầu: 00:00 Honey Slime; 01:00 Wild Boar; 02:00 Giant Bee; 03:00 Red Cap; 04:00 Red Crab; 05:00 Giant Frog; 06:00 Bone Hound; 07:00 Rock Golem; 08:00 Living Armor; 09:00 Lich Lord. Có thể điều chỉnh để không dồn quái khó quá sớm.
- Tại 10:00 **dừng spawn quái thường của hệ thống**, không xóa quái đang sống. Boss có thể triệu hồi quái giới hạn theo capacity riêng hoặc theo trần 60, tránh vô hạn.

### 8.4 Boss Lich Lord [CHỐT + ĐỀ XUẤT]
- Spawn đúng mốc 09:00, có hiệu ứng cảnh báo rõ.
- Phase 1: đạn phép, vùng nguy hiểm; triệu hồi đợt quái nhỏ định kỳ.
- Phase 2 khi HP <50%: tăng cường phép/slam, animation/asset phase 2 **chỉ khi license và runtime được xác minh**.
- HP Hard 250, Easy 200 đề xuất. Boss được đếm riêng, vẫn chịu giới hạn tài nguyên/render.
- Điều kiện thắng: `elapsedGameTime >= 600s && lichLordDefeated && playerHP > 0`.
- [ĐỀ XUẤT] Nếu boss bị hạ trước mốc 10:00, vẫn còn spawn quái thường đến đúng 10:00.

## 9. Thời gian, độ khó, thống kê

### 9.1 Các mốc [CHỐT]
- 00:00–05:00: ban ngày, quái mở khóa tăng dần.
- 05:00: chuyển đêm, có vùng sáng và pool quái nguy hiểm hơn.
- 09:00: boss Lich Lord xuất hiện.
- 10:00: dừng hệ thống spawn quái thường; đồng hồ **tiếp tục chạy**; tiêu diệt boss để kết thúc.

### 9.2 Cân bằng Easy/Hard [CHỐT nhãn, ĐỀ XUẤT hệ số]
| Tham số | Easy | Hard |
|---|---:|---:|
| HP/Speed/Damage ban đầu người chơi | 10 / 10 / 1 | 10 / 10 / 1 |
| HP quái | ×0,75 | ×1,0 |
| Damage quái | ×0,75 | ×1,0 |
| Speed quái | ×0,9 | ×1,0 |
| Nhịp spawn | ×0,75 | ×1,0 |
| Trần quái thường | 40 | 60 |
| Boss HP | 200 | 250 |
| Xác suất rơi máu | Cao hơn | Bình thường |

- `Easy` dễ tiếp cận; `Hard` **chỉ tương đương khó trung bình** trong thực tế, không tối đa thử thách.
- Với damage số nguyên nhỏ, tránh việc `1 × 0,75` làm tròn xuống 0: [ĐỀ XUẤT] sử dụng floating damage accumulator / minimum hit damage 1 hoặc thay thế tỷ lệ bằng tần suất đòn/HP. Quy tắc cuối cần playtest.
- Chỉ số tăng theo từng giai đoạn, có spike khi đêm bắt đầu; giá trị tăng theo từng phút được config; **không chốt công thức 5–10% mỗi phút** khi chưa test.
- Lưu Normal/Demo/độ khó tách biệt trong thống kê [ĐỀ XUẤT].

### 9.3 UI thống kê [ĐỀ XUẤT]
Hiển thị thời gian sinh tồn thực/ảo tương ứng, kill count, boss defeated, số nâng cấp, sát thương gây ra, sát thương nhận, độ khó, chế độ, kết quả, thời gian tốt nhất. Lưu local (SharedPreferences hoặc Room nếu cần dữ liệu nhiều) và hỗ trợ chia sẻ kết quả qua Android Intent.

## 10. UI/UX và yêu cầu học phần

### 10.1 Hai lớp UI không chồng trách nhiệm
**Native Android (Java/XML + Material/AndroidX):** Home, lựa chọn độ khó/mode, Settings, Achievements/History, How to Play, About/Credits; Options Menu, ContextMenu, SubMenu, BottomNavigationView và điều hướng bằng Intent. Menu phải có thao tác thực sự hoạt động, không chỉ trang trí.

**libGDX:** GameScreen, joystick ảo, HP/Timer/Damage/Speed, slots vũ khí, dash/ultimate cooldown, minimap, mũi tên item, overlay ngày/đêm, popup Weapon Choice, Pause khi đang chơi, boss health bar, Game Over/Victory trong game hoặc chuyển native bằng callback có hợp đồng rõ.

**Gợi ý đối chiếu tiêu chí môn học:**
| Yêu cầu | Vị trí thể hiện |
|---|---|
| XML + Layout Editor | Home, Settings, Achievements; lưu ảnh Layout Editor |
| Java UI/event | OnClick/OnLongClick, menu listeners, intent extras |
| Material + AndroidX | Buttons, Cards, TextInput/Slider, BottomNavigationView, dialogs |
| Options Menu | Từ toolbar Home: Settings, Help, About |
| ContextMenu | Long-press một record trong Achievements: Details / Delete (có xác nhận) |
| SubMenu | Options → Theme → Day/Night/System (giao diện menu, không làm thay đổi logic chuyển đêm trong trận) |
| Navigation Bar | Home / Achievements / Settings |
| Intent | Home→Game, Home→Settings/Achievements, share result via ACTION_SEND |
| Animation | Menu transitions, UI feedback, popup, joystick, attack/death, night transition |

### 10.2 Điều hướng kỹ thuật [ĐỀ XUẤT]
- Android `HomeActivity` là launcher; `AndroidLauncher`/`GameActivity` chứa view libGDX.
- Hợp đồng truyền vào game `mode`, `difficulty`; trả về result DTO hoặc intent extras ở game end.
- Tránh hai hệ thống pause riêng không đồng bộ. **Chỉ một owner điều khiển gameplay state.**
- UI Android về Portrait hay Landscape: [MỞ] ưu tiên landscape toàn ứng dụng cho đồng nhất với game, nhưng phải đảm bảo đáp ứng XML trực quan.

### 10.3 Quy tắc trải nghiệm [ĐỀ XUẤT]
- Font pixel art cho tiêu đề nếu đọc được; chữ nhỏ/HUD dùng font dễ đọc.
- Đảm bảo độ tương phản ở chế độ đêm; vùng bấm Dash/Ultimate đủ lớn; joystick không che nhân vật.
- Khi nhặt hộp hiện toast trong game như `DMG +2`/`SPEED +1`/`HP +3` và hiệu ứng rõ.
- Không để màn hình Weapon Choice bị đè bởi pause/back; giao diện một modal tại một thời điểm.
- Màn hình Pause: Resume / Restart / Settings / Exit; Restart và Exit có xác nhận khi cần.

## 11. Asset và giấy phép

### 11.1 Phương án đang định hướng
- Tilemap/rừng: `kenney_roguelike-rpg-pack`, đặc biệt `sample_map.tmx` và spritesheet liên quan, giấy phép CC0 theo License.txt trong gói.
- Player dự kiến: **N3XUS Iron Knight** `iron-knight-32x64.png`, 4 hướng, walk/talk; không có attack/death trong sheet chuẩn (cần hiệu ứng riêng).
- Quái thường: 9 loại từ Willosuna Pixel RPG Monsters Top-Down Free Demo: Honey Slime, Wild Boar, Giant Bee, Red Cap, Red Crab, Giant Frog, Rock Golem, Living Armor, Bone Hound.
- Boss: Lich Lord từ Willosuna; chỉ dùng asset thuộc phạm vi Free Demo.
- Hộp/vũ khí: sprite Kenney sau khi chọn vùng thực tế.

### 11.2 Ràng buộc phải kiểm tra trước khi tự động tích hợp
- **N3XUS:** license được khảo sát ghi credit bắt buộc chính xác `Art by N3XUS - n3xus.dev`, hạn chế upload/phân phối asset rời và **cấm dùng asset cho huấn luyện/đánh giá AI**. Không tự suy diễn rằng việc cung cấp asset cho agent AI là hợp lệ; nếu không xác nhận được phạm vi sử dụng hoặc có rủi ro, **dùng nhân vật Kenney CC0 tạm thời hoặc tìm asset thay thế**, không đưa N3XUS vào đầu vào các dịch vụ AI. Giữ code/atlas không phụ thuộc sprite cụ thể.
- **Willosuna:** license Free Demo cho phép dùng trong game nhưng không phân phối asset như một pack; không lấy hero/map trong thư mục `demo/` vì báo cáo cho thấy ngoài license của gói.
- Không commit `asset_sources` lên repo public nếu chứa bộ asset không cho phép phân phối lại. Kiểm tra `.gitignore` trước git add/push. Runtime chỉ chứa những file cần thiết và quyền sử dụng được xác minh.
- Aseprite JSON không phải `.atlas` của libGDX; phải parse metadata chính xác hoặc tạo file chuyển đổi có kiểm chứng thay vì cắt frame theo phỏng đoán.
- Ghi rõ artist credits trong màn About/Credits, kể cả Kenney/Willosuna nếu muốn, nhưng tuân thủ bắt buộc cho gói được dùng.

## 12. Kiến trúc runtime (đề xuất)

```text
Android Java/XML / Material
  HomeActivity, SettingsActivity, AchievementsActivity, Credits
  └── Intent → AndroidLauncher (libGDX)
                      ├── GameScreen (state machine)
                      ├── World / Tilemap / CollisionGrid
                      ├── Player + Health / Weapon Inventory / Skills
                      ├── EnemySystem (spawn + AI + A*)
                      ├── Projectile / Damage / Item / Loot
                      ├── DayNight / Timer / DifficultyRules
                      ├── HudRenderer + Input (joystick)
                      └── Results bridge → Android native persistence
```

**Tách ownership khi triển khai:**
- **Codex:** `core`, gameplay logic, asset loading/runtime, pathfinding, gameplay HUD rendering theo giao diện/hợp đồng đã chốt.
- **Antigravity:** Android native XML/Java cho Home, Settings, Achievements, Options/Context/SubMenu, BottomNavigation, thiết kế UI spec HUD; **không sửa `core`**, trừ khi được giao tích hợp có kiểm soát.
- **Shared:** `android` launcher/Manifest/Gradle/Intent result contract — một agent chỉnh tại một thời điểm hoặc làm trên nhánh/worktree riêng, kiểm tra git diff trước merge.

**Hiệu năng:** batch sprite draws; texture atlas có nguồn hợp lệ; texture filtering nearest cho pixel art; object pooling cho quái/projectile/particles; tránh allocate trong render loop; tránh pathfinding cho mọi quái mỗi frame. 60 quái là trần gameplay đã chốt, không phải FPS đảm bảo. Mục tiêu 60 FPS, sàn chấp nhận 30 FPS ổn định tùy test emulator; đo thật trên máy, không tuyên bố PASS từ build thành công.

## 13. Kế hoạch thực thi theo cột mốc (chưa giao agent)

1. **Preflight / Art & Integration Contract:** đóng băng mapping asset, xem trước phong cách, giải quyết N3XUS license, định nghĩa owner file và UI↔Game intent contract. Không viết gameplay lớn.
2. **Vertical slice:** Home native → vào trận với tilemap, sprite nhân vật hợp lệ, joystick, camera, 1 quái, 1 vũ khí tự động, 1 loại hộp, Pause/Resume. Chạy trên Pixel 4 Emulator.
3. **Combat systems:** 4 vũ khí, 3 slots, lv1–5 + evolution, Dash, Ultimate, HUD, item tables.
4. **Enemy systems:** đủ 9 quái với behavior, separation, A* hybrid, wave + scaling, drop HP, giới hạn 60.
5. **Final encounter:** Night at 5m, boss at 9m, phase 2, summons, stop natural spawn at 10m, win condition.
6. **Modes & polish:** Easy/Hard, Demo clock mapping, UI native đủ menu/Intent, achievements, credits, animations.
7. **QA & Evidence:** test Android runtime offline; không crash khi pause/rotate/back; performance; capture screenshot XML/menu/gameplay/day/night/boss/victory; mã nguồn vào Word theo yêu cầu nộp.

## 14. Acceptance criteria và ca kiểm thử

### 14.1 Logic
- [ ] Trận Normal bắt đầu đúng 10 HP / 10 Speed / 1 Base Damage và một kiếm.
- [ ] Di chuyển joystick theo 360° độc lập FPS; không ra ngoài bản đồ, không xuyên vật cản.
- [ ] Quái dùng direct chase trên đường thoáng và A* vòng vật cản; quái có separation.
- [ ] Quái spawn ngoài camera; giới hạn 60 quái thường; mở khóa từng loại theo mốc.
- [ ] Hộp spawn mỗi 15–20s gameplay tại vị trí đi được; tồn tại 60s; pickup đúng hiệu ứng.
- [ ] Damage +1..+3, Speed +1 tới tối đa 20, Health hồi hoặc tăng max, Weapon modal 3 lựa chọn hợp lệ.
- [ ] Tối đa 3 vũ khí auto, 5 cấp, cho evolve một lần; Dash/Ultimate cooldown đúng 5/30s.
- [ ] Chạm quái chỉ mất hit theo quy tắc immunity 0,7s; chết -> Game Over.
- [ ] Ban đêm sau 05:00, không load lại map; HUD vẫn đọc được.
- [ ] Lich Lord tại 09:00; phase 2 dưới 50% HP; triệu hồi có trần.
- [ ] Đạt 10:00 dừng **natural spawn**, đồng hồ vẫn chạy; không thắng nếu boss chưa chết; hạ boss và đã đủ 10 phút -> Victory.
- [ ] Pause và chọn vũ khí ngừng đúng simulation time; resume không có delta spike.
- [ ] Demo nén mốc vào 90s, movement/animation/cooldown thực không tăng tốc; nhãn DEMO rõ; thắng thua theo clock mô phỏng.

### 14.2 Android học phần
- [ ] Màn Home/Settings/Achievements XML có thể mở trong Layout Editor.
- [ ] Có AndroidX/Material, ít nhất một minh chứng animation/event.
- [ ] Options Menu, ContextMenu, SubMenu và Bottom Navigation hoạt động thật.
- [ ] Intent chuyển Activity và ACTION_SEND chia sẻ kết quả.
- [ ] Chạy được offline trong Android Emulator từ Android Studio.
- [ ] Có màn kết quả, lưu thành tích, màn Credits/license.
- [ ] Chuẩn bị ảnh chụp màn hình và tài liệu mã nguồn Word cho bài nộp.

### 14.3 Kiểm thử thủ công ưu tiên
- Ca 1: khởi động offline, vào/thoát nhiều lần; không lỗi resource/lifecycle.
- Ca 2: đứng sát mép map/cây/đá/nước, di chuyển/dash; không xuyên.
- Ca 3: quái bị che bởi vật cản, xem path đi vòng; với nhiều quái đo FPS.
- Ca 4: nhặt 3 Weapon Box, full slots, nâng lên cấp 5 và evolve; không đưa offer vô hiệu.
- Ca 5: skip timeline qua **debug/test harness có gắn nhãn** (không đổi luật Normal) kiểm tra 05:00, 09:00, 10:00+.
- Ca 6: boss chết trước và sau 10:00; kiểm tra thắng đúng điều kiện.
- Ca 7: đòn gây hit cùng frame; shield immunity ngăn trừ HP nhiều lần.
- Ca 8: Pause/Resume, Alt-Tab, Back, màn hình tắt, khôi phục; không đẩy timer.
- Ca 9: Demo Mode 90s không làm quái/đạn đi nhanh bất thường.

## 15. Điểm còn mở và cách quản lý

1. **Player sprite N3XUS:** thiếu attack/death và license hạn chế AI. Cần lựa chọn hợp lệ trước khi tích hợp. Gameplay code phải không phụ thuộc Iron Knight.
2. **Art direction:** Kenney 16×16, N3XUS 32×64, Willosuna 32×32/64×64 đồng bội tile nhưng độ chi tiết khác nhau; cần review trực tiếp trên emulator.
3. **Các bảng định lượng:** HP/damage/speed từng quái ngoài boss, projectile speed, damage coefficient, weight box, drop rate, spawn rates theo phút, boss attacks phải là data/config điều chỉnh được; values trong v1.0 chỉ đề xuất.
4. **World scale và camera:** tile source 16×16 nhưng kích thước hiển thị, viewport và collision scale xác định bằng đo máy; không mặc định 1.600 px đủ trải nghiệm 10 phút.
5. **Landscape native UI:** xác nhận lựa chọn toàn ứng dụng hoặc riêng gameplay trước khi Antigravity lập XML cuối cùng.
6. **Mốc Demo:** định nghĩa chuyển clock ảo/clock thực thống nhất (đặc biệt cooldown và thời gian tồn tại hộp); phải test kỹ.
7. **Lich Lord summons sau 10:00:** dùng trần 60 tổng quái thường hay trần riêng nhỏ? Mặc định đề xuất tính vào trần 60 để không tràn.
8. **Damage Easy:** làm tròn damage nhỏ phải tránh sát thương bằng 0.

## 16. Nhật ký quyết định (54 câu, rút gọn)

**01–11**: 1A map hữu hạn; 2C auto attack + skills; 3B tối đa 3 weapon; 4C boxes mixed; 5B no XP; 6A offscreen spawn; 7C liên tục + đợt đặc biệt; 8B boss phút 9; 9C night light + harder enemies; **10B** 10 phút + hạ boss; 11A Demo Mode.  
**12–20**: 12 tự đặt 10 HP/10 Speed/1 Damage; 13 A+C (Health Box + enemy HP drop); 14B immunity 0,7s; 15A 4 auto weapons kiếm/cung/lửa/sét; 16C Dash+Ultimate; 17B Weapon Box chọn 3; 18B 5 cấp; 19A *điều chỉnh 15–20s*; 20A chết ngay.  
**21–29**: 21B enemy separation; 22C thường contact/special riêng; 23B hybrid direct/A*; 24B some ranged enemies; 25B unlock theo thời gian; 26B HP drop; 27B boss phase 2 <50%; 28B summon định kỳ; **29A sửa** đồng hồ chạy tiếp sau 10:00, ngừng natural spawn.  
**30–38**: 30A start sword; 31C sword slash rồi spin cấp cao; 32C Damage Box +1..+3; 33C Health Box random heal/max; 34C Speed +1 cap 20; 35A full weapons → upgrades only; 36B evolve after lv5; 37A Dash5s/Ultimate30s; 38C box expire 60s/blink.  
**39–46**: 39A map 100×100; 40C center meadow/outside forest; 41C rocks/trees/water block ground, flyers cross water; 42A night overlay same map; 43A minimap player+boss; 44C box nearby, arrows; 45B Demo event schedule compressed but normal movement; 46A full Pause.  
**47–54**: 47A 5–10 initial enemies; 48A cap60; 49C difficulty phases + night spike; 50B increased HP drop on low HP; 51B damage per type grows over time; 52C fixed cap/no dynamic FPS difficulty; 53B boss250HP; 54A Easy; **bổ sung Hard nhãn Khó nhưng cân bằng mức trung bình**.

---

**Bước kế tiếp khuyến nghị:** Duyệt GDD v1.0, giải quyết player sprite/license và chọn các điểm `[MỞ]` cần ưu tiên; sau đó chia hai workstream theo hợp đồng file rõ ràng: Codex = gameplay/engine; Antigravity = native UI/UX. **Chưa phát lệnh code hàng loạt trước khi hai agent cùng đọc GDD.**
