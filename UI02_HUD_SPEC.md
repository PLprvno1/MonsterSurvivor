# UI02 — MONSTER SURVIVOR: HUD & PRESENTATION SPECIFICATION

**Dự án:** Monster Survivor (Android Native + libGDX 1.14.2)  
**Phiên bản:** UI02 Specification v1.0  
**Tác giả:** Senior Android UI/UX Engineer  
**Ngày hoàn thiện:** 14/11/2026  
**Mục tiêu thiết bị:** Google Pixel 4 Landscape (2280 × 1080 px, API 29–37)

---

## 1. TỔNG QUAN & NGUYÊN TẮC THIẾT KẾ (ART DIRECTION)

### 1.1. Phong cách Fantasy Pixel Art
- **Tone màu chủ đạo:**
  - *Xanh rừng cổ đại (Deep Forest Slate):* `rgba(22, 28, 36, 0.88)` – Nền panel bán trong suốt, chống che khuất tầm nhìn quái và bản đồ.
  - *Vàng kim hoàng gia (Imperial Gold):* `#8A7350` (viền chuẩn), `#FFD54F` (viền nổi bật / accent), `#FFA000` (highlight).
  - *Đỏ máu dũng sĩ (Crimson Red):* `#C42020` đến `#E54040` (thanh HP và cảnh báo nguy hiểm).
  - *Tím ma thuật (Arcane Violet):* `#7B1FA2` đến `#BA68C8` (Weapon Box, kinh nghiệm và năng lượng phép).
  - *Xanh ngọc / Băng tuyết (Cyan / Frost Teal):* `#00BCD4` đến `#00E5FF` (chỉ số tốc độ, tia sét).
- **Nguyên tắc tương phản & chiều sâu:**
  - Viền kép (double-stroke border): Viền ngoài sẫm màu (`#0D1217`), viền trong ánh kim (`#8A7350`) tạo độ vát nổi (bevel) chuẩn phong cách pixel cổ điển.
  - Loại bỏ hoàn toàn các khối đen đặc (opaque black rectangles) che khuất bản đồ.
  - Không xếp chồng nhiều lớp alpha overdraw không cần thiết.

### 1.2. Hệ tọa độ & Safe Insets
- **Viewport HUD:** Virtual resolution được co giãn và unproject tương thích màn hình ngang Pixel 4 (tỷ lệ 19:9, 2280 × 1080).
- **Safe Zone Insets:**
  - *Top Margin:* 16px an toàn dưới mép status bar.
  - *Right Margin:* 24px an toàn tránh camera cutout / notch.
  - *Left Margin:* 20px an toàn tránh cạnh bo tròn.
  - *Bottom Margin:* 14px an toàn trên thanh điều hướng hệ thống (system navigation bar).

---

## 2. BỐ CỤC CHI TIẾT HUD GAMEPLAY

```
+--------------------------------------------------------------------------------------------------+
| [TOP-LEFT STATS PANEL]              [TOP-CENTER TIMER BADGE]                 [TOP-RIGHT PAUSE]   |
| [♥ HP 10/10 [========]]                  [ 04:25 | DỄ ]                            [ || ]        |
| [💀 HẠ GỤC: 42 | ⚔ ST: 3 | 👢 TỐC: 12]                                                           |
|                                                                                                  |
|                                                                                                  |
|                                       [GAME WORLD]                                               |
|                                                                                                  |
|                                                                                                  |
| [FLOATING JOYSTICK ZONE]                                                     [WEAPON BOX MODAL]  |
| (Idle Hint at 108,108 /                                                      (When triggered)    |
|  Dynamic Center on Touch)                                                                        |
|                                    [BOTTOM-CENTER WEAPONS]                                       |
|                              [⚔ Lv.3 (CD)] [🏹 Lv.2 (CD)] [🔥 Lv.1 (CD)]                         |
+--------------------------------------------------------------------------------------------------+
```

---

## 3. CHI TIẾT CÁC THÀNH PHẦN HUD

### 3.1. Góc trái phía trên (V1: Player Status Panel)
- **Tọa độ & Kích thước:**
  - Vị trí: `x = 20`, `y = height - 90`.
  - Kích thước panel nền: `width = 250px`, `height = 74px`.
  - Nền: `rgba(18, 24, 32, 0.82)`, bo góc nhẹ, viền vàng sẫm `#8A7350` dày 1.5px.
- **Thanh máu (HP Bar):**
  - Vị trí trong panel: `x = 32`, `y = height - 42`, `width = 226px`, `height = 18px`.
  - Lòng thanh HP: Nền tối `#1A0808` chống nhầm lẫn.
  - Mức máu hiện tại: Gradient ngang từ `#B71C1C` (đỏ thẫm) sang `#E53935` (đỏ tươi).
  - Nhãn hiển thị: Text nằm giữa thanh `MÁU 10 / 10` (hoặc `HP 10 / 10`), chữ trắng viền đổ bóng pixel đen, đảm bảo đọc rõ trong mọi điều kiện ánh sáng rừng/đêm.
  - Phản hồi khi mất máu: Thanh cập nhật ngay lập tức theo giá trị logic thực từ `Player.getHp()` / `getMaxHp()`, không delay gây lệch gameplay.
- **Chỉ số Kills & Combat:**
  - Dòng thứ 2: `y = height - 68`.
  - Biểu tượng Skull + `HẠ GỤC: <N>` (hoặc `KILLS: <N>`), màu vàng sáng `#FFE082`. Lấy trực tiếp từ `GameSession.kills` tổng.
- **Chỉ số Sát thương & Tốc độ (DMG & SPD Pills):**
  - Dòng thứ 3 (phía dưới):
    - *Pill Sát thương:* Biểu tượng Kiếm + `ST: <dmg>` (hoặc `DMG: <dmg>`), màu cam ánh kim `#FFB74D`.
    - *Pill Tốc độ:* Biểu tượng Giày/Cánh + `TỐC: <spd>` (hoặc `SPD: <spd>`), màu xanh ngọc `#4DD0E1`.
  - Bố trí dạng thẻ con gọn gàng, cách nhau 10px, không tạo cảm giác rối mắt.

### 3.2. Giữa phía trên (V2: Match Timer & Badges)
- **Tọa độ & Kích thước:**
  - Vị trí: Căn giữa màn hình `x = (width - 220) / 2`, `y = height - 52`.
  - Kích thước panel: `width = 220px`, `height = 38px`.
  - Khung viền: Vòm bán nguyệt dạng huy hiệu hoàng gia (heraldic badge), màu nền đá phiến tối `rgba(14, 20, 27, 0.90)` với viền vàng `#FFD54F`.
- **Đồng hồ thời gian (Timer):**
  - Font chữ số lớn rõ ràng, định dạng `MM:SS`.
  - *Chế độ NORMAL:* Đo đếm `GameSession.elapsed()` (thời gian sống thực tế).
  - *Chế độ DEMO:* Đo đếm `GameSession.progress()` (tiến trình sự kiện nén 90 giây) với huy hiệu `DEMO` màu cam rực rỡ bên cạnh để người chơi không nhầm lẫn.
- **Huy hiệu độ khó (Difficulty Badge):**
  - Nằm ngay dưới đồng hồ:
    - *DỄ / EASY:* Badge xanh lục lá rừng (`#43A047`).
    - *KHÓ / HARD:* Badge đỏ thẫm rực lửa (`#E53935`).
- **Trạng thái Night Mode:**
  - Khi `progress >= 300.0s`: Tự động hiện biểu tượng trăng khuyết tím ánh ma thuật (`#CE93D8`) kế bên đồng hồ, báo hiệu giai đoạn quái đêm nguy hiểm.

### 3.3. Góc phải phía trên (V3: Pause Button)
- **Tọa độ & Kích thước:**
  - Vị trí: `x = width - 70`, `y = height - 56`.
  - Kích thước hiển thị: `width = 46px`, `height = 42px`.
  - **Vùng nhận diện cảm ứng (Touch Hitbox):** Mở rộng an toàn ra `64 × 60 px` để người chơi chạm chính xác bằng ngón cái mà không bấm hụt.
- **Tạo hình:**
  - Khung viền vàng nổi dạng đá quý, nền đen obsidian `rgba(10, 14, 20, 0.92)`.
  - Hai thanh Pause song song màu vàng ánh kim `#FFD54F`.
  - Cách mép phải 24px để cách ly hoàn toàn khỏi tai thỏ / thanh camera.

### 3.4. Giữa phía dưới (V4: Weapon Loadout HUD)
- **Tọa độ & Bố cục:**
  - Vị trí: Căn giữa đáy màn hình, `y = 16px`.
  - Gồm 3 slot vũ khí cố định (`MAX_SLOTS = 3`).
  - Kích thước mỗi slot: `54 × 54 px`, khoảng cách giữa các slot: `14px`.
- **Màu sắc nhận diện vũ khí:**
  - ⚔ **Kiếm (Sword):** Vàng hổ phách `#FFA000`, icon gươm hiệp sĩ.
  - 🏹 **Cung (Bow):** Xanh lục rừng `#4CAF50`, icon mũi tên / cung tiễn.
  - 🔥 **Cầu lửa (Fireball):** Đỏ lửa cam `#FF5722`, icon ngọn lửa ma thuật.
  - ⚡ **Tia sét (Lightning):** Xanh ngọc điện `#00E5FF`, icon tia sét bão táp.
  - ⬜ **Slot trống:** Nền xám đen sẫm `#212832`, viền nét đứt mờ `#455A64`.
- **Chi tiết hiển thị trong mỗi slot:**
  - *Góc trên bên phải:* Badge cấp độ `Lv.1` .. `Lv.5` nền vàng chữ đậm.
  - *Thanh hồi chiêu (Cooldown Bar):* Thanh đo nạp tiến trình nằm ở đáy slot vũ khí. Khi vũ khí đang chờ hồi chiêu, thanh lấp đầy từ 0% đến 100% bằng màu cyan rực rỡ, giúp người chơi nắm bắt thời điểm tung đòn kế tiếp.

### 3.5. Chỉ dẫn vật phẩm ngoài màn hình (V5: Offscreen Pickup Indicators)
- **Tọa độ & Giới hạn:**
  - Tự động ghim (clamp) vào cạnh màn hình theo hướng vector từ Player tới vật phẩm.
  - Giới hạn trong vùng an toàn: `x ∈ [32, width - 32]`, `y ∈ [80, height - 80]`.
  - Cách ly hoàn toàn khỏi khu vực joystick bên trái, panel máu góc trên và thanh vũ khí đáy màn hình.
- **Hình dạng & Nhận diện:**
  - ❤️ **Hồi máu (Health Box / Drop):** Mũi tên đỏ thẫm với biểu tượng chữ thập y tế.
  - ⚔ **Tăng sát thương (Damage Box):** Mũi tên cam hổ phách với biểu tượng kiếm.
  - 👟 **Tăng tốc (Speed Box):** Mũi tên xanh ngọc với biểu tượng cánh gió.
  - 🎁 **Rương vũ khí (Weapon Box):** Mũi tên tím ma thuật viền vàng lấp lánh với biểu tượng rương báu.

---

## 4. QUY CHUẨN GIAO DIỆN FLOATING JOYSTICK

Logic vector và vùng cảm ứng do Codex đảm bảo ở Task 04A.1. UI02 hoàn thiện toàn bộ phần hiển thị đồ họa và cảm giác thao tác:

```
[Vùng nghỉ (Idle State)]             [Vùng kích hoạt (Active State)]
       (x=108, y=108)                         (Tại vị trí ngón tay chạm)
           . - - .                                  . - - - - - .
        '         '                             '     (R=66)      '
       (  Hint ○  )                            (   +-----> ● (Knob))
        '  (a=0.12) '                           '     \  (R=26)   '
           ' - - '                                  ' - - - - - '
                                                Vành ma thuật + Tia dẫn
```

### 4.1. Trạng thái nghỉ (Idle State)
- Vị trí gợi ý: Neo tại góc dưới bên trái `x = 108px`, `y = 108px`.
- Kích thước: Bán kính `R = 32px`.
- Độ mờ: Alpha tinh tế `0.12` (vòng tròn viền mỏng nét mờ).
- Tác dụng: Hướng dẫn người chơi mới biết vị trí đặt ngón tay cái mà không gây xao nhãng hoặc che tầm nhìn map.

### 4.2. Trạng thái chạm kích hoạt (Active State)
- **Tâm joystick linh hoạt:** Tự động sinh ra ngay tại tọa độ touch-down của ngón tay trong vùng `x ∈ [0, 44% width]`, `y ∈ [28, height - 96]`.
- **Vành đế (Base Ring):**
  - Bán kính tối đa: `R = 66px`.
  - Đồ họa: Vòng hào quang kép (dual-ring rune) với viền ngoài vàng kim rực rỡ và nền đá tối bán trong suốt `rgba(18, 24, 34, 0.45)`.
- **Tia chỉ hướng (Drag Vector Guide):**
  - Đường nét phát sáng nối từ tâm đế tới núm điều khiển, đổi độ dày theo cường độ kéo (magnitude), tạo cảm giác phản hồi lực căng dây cung.
- **Núm điều khiển (Thumb Knob):**
  - Bán kính: `R = 26px`.
  - Đồ họa: Khối ngọc ma thuật 3D (faceted rune gem) với ánh sáng phản chiếu hướng tâm (specular highlight), viền vàng nổi bật.
  - Giới hạn: Bị khóa chặt (clamped) trong bán kính 66px khi kéo vượt quá giới hạn vật lý.

### 4.3. Trạng thái thả tay (Release State)
- Ngay khi nhấc ngón tay (`touchUp`), toàn bộ đồ họa tâm động và núm biến mất tức thì.
- Vòng gợi ý mờ tại `(108, 108)` khôi phục nhẹ nhàng.
- Tuyệt đối không để lại tàn dư đồ họa hoặc hiện tượng kẹt núm điều khiển.

---

## 5. MODAL CHỌN NÂNG CẤP VŨ KHÍ (WEAPON BOX MODAL)

Khi người chơi nhặt Weapon Box, game chuyển sang trạng thái `CHOOSING_WEAPON`. Toàn bộ mô phỏng thế giới tạm dừng để người chơi lựa chọn 1 trong tối đa 3 thẻ vũ khí:

```
+--------------------------------------------------------------------------------------------------+
|                                    CHỌN VŨ KHÍ / CHOOSE A WEAPON                                 |
|                                                                                                  |
|       +-------------------+     +-------------------+     +-------------------+                  |
|       |     🔥 CẦU LỬA    |     |      🏹 CUNG      |     |      ⚡ TIA SÉT   |                  |
|       |                   |     |                   |     |                   |                  |
|       |  Cấp 1  ->  Cấp 2 |     |  MỚI   ->  Cấp 1  |     |  Cấp 2  ->  Cấp 3 |                  |
|       |                   |     |                   |     |                   |                  |
|       | +1 Tia lửa        |     | Bắn mũi tên xuyên |     | Tăng sát thương   |                  |
|       | Giảm hồi chiêu 10%|     | mục tiêu gần nhất |     | Giật thêm 1 quái  |                  |
|       |                   |     |                   |     |                   |                  |
|       +-------------------+     +-------------------+     +-------------------+                  |
|                                                                                                  |
| [ VỀ TRANG CHỦ ]                          Chạm vào thẻ vũ khí để chọn nâng cấp                    |
+--------------------------------------------------------------------------------------------------+
```

### 5.1. Khung hộp thoại chính
- Kích thước: `width = 720px`, `height = 460px`, căn giữa màn hình tuyệt đối.
- Nền: Đá phiến xanh thẫm hoàng gia `rgba(14, 18, 25, 0.95)`, phủ mờ toàn cảnh nền thế giới (backdrop blur / dim alpha 0.75).
- Khung viền: Viền vàng kim đôi chạm trổ hoa văn pixel fantasy cổ điển.
- Tiêu đề: Banner vàng rực rỡ phía trên: `CHỌN VŨ KHÍ` (VI) hoặc `CHOOSE A WEAPON` (EN).

### 5.2. Cấu trúc 3 thẻ bài nâng cấp (Upgrade Cards)
- Số lượng thẻ: Tối đa 3 thẻ xếp ngang đều đặn. Kích thước mỗi thẻ: `width = 200px`, `height = 290px`.
- Vùng chạm (Hitbox): Toàn bộ diện tích thẻ đều nhận diện click/tap.
- Nội dung mỗi thẻ:
  1. *Header thẻ:* Tên vũ khí cùng màu sắc nhận diện đặc trưng (Đỏ lửa, Xanh cung, Vàng kiếm, Lam sét).
  2. *Huy hiệu trạng thái:* Thẻ mới nhận huy hiệu `MỚI / NEW`; thẻ nâng cấp hiển thị rõ ràng bước tiến `Cấp X → Cấp Y` (`Lv. X → Lv. Y`) bằng mũi tên vàng kim.
  3. *Mô tả chỉ số nâng cấp:* Liệt kê chi tiết hiệu ứng (tăng sát thương, số đạn, hồi chiêu) bằng câu văn bản địa hóa ngắn gọn, ngắt dòng tự động chuẩn xác.
  4. *Fallback Offers:* Khi tất cả vũ khí đạt Cấp 5 tối đa, tự động hiện các thẻ bồi dưỡng dũng sĩ: `+2 Sát thương`, `+2 Máu tối đa & Hồi 2`, `+1 Tốc độ`.
- Phản hồi tương tác: Khi chạm vào thẻ, khung thẻ sáng bừng viền vàng kim `#FFF59D` trước khi gửi lệnh cam kết lựa chọn (commit selection).

### 5.3. Nút phụ & Chú thích chân trang
- *Nút Về Trang Chủ (Home Button):* Nằm ở góc dưới bên trái hộp thoại `x = 48`, `y = 36`, kích thước `140 × 40 px`, cho phép thoát trận đấu an toàn.
- *Dòng hướng dẫn chân trang:* `Chạm vào thẻ vũ khí để chọn nâng cấp` / `Tap a weapon card to select an upgrade`, màu xám bạc mờ `#90A4AE`.

---

## 6. HỘP THOẠI TẠM DỪNG (PAUSE) & KẾT THÚC (GAME OVER)

### 6.1. Hộp thoại Tạm dừng (Pause Overlay)
- **Nền mờ:** Phủ đen `rgba(0, 0, 0, 0.65)` làm dịu chiến trường.
- **Bảng thông tin:** Kích thước `460 × 340 px`, viền vàng thanh lịch.
- **Tiêu đề:** `TẠM DỪNG / PAUSED`.
- **Nút hành động:**
  - `TIẾP TỤC / RESUME`: Nút chính màu xanh ngọc viền vàng, bấm để tiếp tục trận đấu.
  - `VỀ TRANG CHỦ / RETURN HOME`: Nút phụ màu đá phiến xám, đưa người chơi về màn hình chính Android Native an toàn.

### 6.2. Hộp thoại Kết thúc trận đấu (Game Over Overlay)
- **Nền mờ:** Phủ sắc đỏ thẫm tang thương `rgba(28, 4, 4, 0.82)`.
- **Bảng thông tin:** Kích thước `520 × 380 px`, viền đầu lâu cổ ngữ.
- **Tiêu đề:** `KẾT THÚC TRẬN ĐẤU / GAME OVER` sắc đỏ rực `#E53935`.
- **Thống kê thực chiến:**
  - *Thời gian sinh tồn:* Hiển thị số phút giây sống sót thực tế (`GameSession.elapsed()`).
  - *Quái vật đã tiêu diệt:* Tổng số quái hạ gục (`GameSession.kills`).
- **Nút hành động:**
  - `CHƠI LẠI / RETRY`: Nút chính nổi bật, tải lại trận đấu với cùng thiết lập bản đồ và độ khó mà không cần thoát ra Native.
  - `VỀ TRANG CHỦ / RETURN HOME`: Đưa người chơi trở về Native Home Activity.

---

## 7. NGÂN SÁCH HIỆU NĂNG & TỐI ƯU RENDER

1. **Không cấp phát bộ nhớ trong vòng lặp Render (Zero Garbage Collection):**
   - Tái sử dụng đối tượng `GlyphLayout`, `StringBuilder`, `Color`, `Vector2`.
   - Toàn bộ chuỗi hiển thị tĩnh được nạp sẵn từ `GameStrings`.
2. **Gộp lệnh vẽ (Draw Call Batching):**
   - Quy trình phân lớp rõ ràng:
     1. Khối ShapeRenderer (vành joystick, thanh HP, khung modal, nền đen mờ).
     2. Khối SpriteBatch (các icon vũ khí, sprite pickup indicator, text BMFont).
   - Không chuyển đổi qua lại giữa `ShapeRenderer.begin()` và `SpriteBatch.begin()` nhiều lần trong 1 frame.
3. **Bộ font tối ưu:**
   - Dùng BMFont duy nhất `game-ui.fnt` (kích thước page 512×256) chứa đầy đủ 261 ký tự Tiếng Việt chuẩn NFC và Latin.
   - Không sinh dynamic font TTF runtime gây tụt khung hình trên Android Emulator.
