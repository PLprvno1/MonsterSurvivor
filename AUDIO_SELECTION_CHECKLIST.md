# Audio Selection Checklist — Chủ dự án nghe và chọn

Mở [AUDIO_AUDITION.html](AUDIO_AUDITION.html) bằng trình duyệt desktop để nghe từng candidate; chỉ một player, không autoplay/preload hàng loạt. Có lọc nhóm và xuất lựa chọn JSON. File này cũng có link mở file trực tiếp nếu trình duyệt không decode được. **Chưa có mục nào được tự động duyệt.** HTML không tự sửa checklist này và không ghi vào asset.

Mọi đường dẫn bên dưới tính từ `asset_sources/audio/`. Tất cả đề xuất dựa tên/metadata/mô tả tác giả, chưa nghe trực tiếp. Nghe ở âm lượng thiết bị vừa phải, đặc biệt file có flag peak; so A/B cùng ngữ cảnh trước khi chốt. Có thể chọn một file chung cho nhiều event; không cần tải thêm nếu palette hiện có đủ.

Mỗi mục: chọn A/B/C nếu hợp; dùng Đồng ý để duyệt candidate đã đánh dấu, Thay nếu muốn file khác, hoặc giữ Chưa chọn. Các ô là lựa chọn người dùng, không phải kết quả audit. MISSING không có file để mở, không tự tạo candidate.

## BGM

### 01. Menu — `BGM_MENU`

Tái dùng dungeon theme ở gain thấp; chưa xác nhận độ dịu hoặc hợp menu. Không suy từ chữ end rằng đây là nhạc kết thúc.

- [ ] Chọn A: [`end_dungeon.mp3`](asset_sources/audio/end_dungeon.mp3) — **165.564s**; review: MUSIC_NOT_SOUND_SIZE.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 02. Day Gameplay — `BGM_DAY`

Hope được tác giả mô tả sáng, orchestral; Battle là phương án chiptune nếu chọn phong cách retro nhất quán.

- [ ] Chọn A: [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg) — **69.818s**; review: MUSIC_NOT_SOUND_SIZE.
- [ ] Chọn B: [`battle_music_01-loop.ogg`](asset_sources/audio/battle_music_01-loop.ogg) — **152.000s**; review: PCM_FULL_SCALE_REVIEW, DC_OFFSET_REVIEW, MUSIC_NOT_SOUND_SIZE.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 03. Night Gameplay — `BGM_NIGHT`

Tác giả mô tả dungeon theme, có thể hợp không khí đêm; MP3 có rủi ro gap.

- [ ] Chọn A: [`end_dungeon.mp3`](asset_sources/audio/end_dungeon.mp3) — **165.564s**; review: MUSIC_NOT_SOUND_SIZE.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 04. Mini Boss — `BGM_MINI`

Battle là RPG battle/chiptune, cần xử lý intro/loop về khoảng7,5s; Hope là fallback loop toàn bài dễ thử hơn.

- [ ] Chọn A: [`battle_music_01-loop.ogg`](asset_sources/audio/battle_music_01-loop.ogg) — **152.000s**; review: PCM_FULL_SCALE_REVIEW, DC_OFFSET_REVIEW, MUSIC_NOT_SOUND_SIZE.
- [ ] Chọn B: [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg) — **69.818s**; review: MUSIC_NOT_SOUND_SIZE.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 05. Lich Lord — `BGM_FINAL`

Nguồn tác giả mô tả boss/metal/dark và loopable; chỉ là lựa chọn sơ bộ, chưa nghe mức phù hợp pixel fantasy.

- [ ] Chọn A: [`heavy_boss_battle_1_bpm200.ogg`](asset_sources/audio/heavy_boss_battle_1_bpm200.ogg) — **108.000s**; review: MUSIC_NOT_SOUND_SIZE.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 06. Victory — `BGM_VICTORY`

Tái dùng bài sáng sau Victory, loop off hoặc tiếp tục nhẹ; đây là BGM dài, không phải jingle ngắn.

- [ ] Chọn A: [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg) — **69.818s**; review: MUSIC_NOT_SOUND_SIZE.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 07. Game Over — `BGM_GAME_OVER`

Đề xuất dừng BGM + cue kết quả ngắn; không bắt buộc mua/tìm thêm một bài nhạc riêng.

**MISSING** — chọn im lặng + cue UI hiện có cũng được; không bắt buộc BGM riêng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

## Weapons

### 08. Sword swing — `WEAPON_SWORD_SWING`

Tên slice hợp động tác chém; A có khoảng50ms đầu/đuôi im lặng, B có sample sát full scale, cần nghe so A/B.

- [ ] Chọn A: [`kenney_rpg-audio/Audio/knifeSlice.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice.ogg) — **0.600s**; review: LEAD_GT_30MS, TAIL_GT_100MS.
- [ ] Chọn B: [`kenney_rpg-audio/Audio/knifeSlice2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice2.ogg) — **0.569s**; review: PCM_FULL_SCALE_REVIEW.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 09. Sword hit — `WEAPON_SWORD_HIT`

Impact chém vật lý dựa tên chop; có sample full scale cần nghe/gain headroom.

- [ ] Chọn A: [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg) — **0.240s**; review: PCM_FULL_SCALE_REVIEW.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 10. Bow release — `WEAPON_BOW_RELEASE`

Pluck chỉ là proxy dây bật từ bộ interface, chưa xác nhận tiếng cung; A/B đều có seam/peak cần duyệt, có quyền từ chối cả hai.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/pluck_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_002.ogg) — **0.165s**; review: ENDPOINT_JUMP_REVIEW.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/pluck_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_001.ogg) — **0.102s**; review: PCM_FULL_SCALE_REVIEW, ENDPOINT_JUMP_REVIEW.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 11. Arrow hit — `WEAPON_ARROW_HIT`

Tái dùng impact vật lý/chạm vật mềm, không khẳng định là tiếng mũi tên.

- [ ] Chọn A: [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg) — **0.240s**; review: PCM_FULL_SCALE_REVIEW.
- [ ] Chọn B: [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) — **0.415s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 12. Fireball cast — `WEAPON_FIRE_CAST`

Không thấy file được nhận diện là fire/whoosh/magic cast; không dùng tiếng cửa hay click để lấp bảng.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 13. Fireball explosion — `WEAPON_FIRE_EXPLOSION`

Cần impact/explosion ngắn có lực; bộ hiện có là UI/foley, chưa có ứng viên đủ cơ sở.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 14. Lightning activation — `WEAPON_LIGHTNING_ACTIVATE`

Không có zap/electric/magic đã nhận diện; glitch không mặc nhiên là sét.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 15. Lightning impact — `WEAPON_LIGHTNING_IMPACT`

Có thể dùng cùng asset điện với activation sau khi nghe và duyệt gain/pitch; hiện chưa có.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

## Pickups

### 16. Damage Potion — `PICKUP_DAMAGE`

Cue glass ngắn hợp vật phẩm bình; cùng nhóm với Health/Speed để tiết kiệm voice.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) — **0.125s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/glass_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_003.ogg) — **0.124s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 17. Health Potion / Max HP — `PICKUP_HEALTH`

Glass ngắn hoặc confirmation cho hồi/tăng HP; không cần hai file cho hai nhánh.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) — **0.125s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) — **0.322s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 18. Speed Potion — `PICKUP_SPEED`

Dùng chung palette glass; chỉ phân biệt bằng variant nếu người dùng nghe thấy khác biệt hữu ích.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/glass_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_003.ogg) — **0.124s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) — **0.125s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 19. Health Drop — `PICKUP_HEALTH_DROP`

Cue ngắn, định gain thấp hơn potion; tránh chuỗi heal lấn combat.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/glass_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_005.ogg) — **0.111s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) — **0.125s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 20. Weapon Box mở modal — `PICKUP_WEAPON_OPEN`

Open là cue UI rõ chức năng; metalLatch là proxy rương có full-scale samples cần duyệt.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/open_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_001.ogg) — **0.148s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_rpg-audio/Audio/metalLatch.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalLatch.ogg) — **0.261s**; review: PCM_FULL_SCALE_REVIEW.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 21. Chọn vũ khí mới — `PICKUP_WEAPON_SELECTED`

Cue commit ngắn, chỉ chọn một trong hai, không chồng thêm generic confirm cùng thao tác.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/confirmation_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg) — **0.290s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/select_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_001.ogg) — **0.043s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 22. Nâng cấp vũ khí — `PICKUP_WEAPON_UPGRADED`

Positive confirmation hoặc maximize ngắn, dùng chung với selected nếu muốn palette nhỏ.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) — **0.322s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) — **0.258s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

## Boss

### 23. Mini Boss xuất hiện — `MINI_APPEARS`

Cue cảnh báo/nhấn mạnh UI tạm, chưa có roar hoặc fanfare chuyên dụng.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) — **0.123s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) — **0.258s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 24. Mini slam warning — `MINI_SLAM_WARNING`

Một tone ngắn làm tín hiệu báo trước; visual vẫn là telegraph chính, không loop tone mỗi frame.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) — **0.123s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 25. Mini slam impact — `MINI_SLAM_IMPACT`

Cần thump/stone/heavy impact ngắn; không mặc nhiên coi doorClose/metalPot là slam.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 26. Mini defeated — `MINI_DEFEATED`

Cue positive ngắn, không cần một bản nhạc thắng riêng.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/confirmation_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_002.ogg) — **0.539s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) — **0.322s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 27. Lich Lord xuất hiện — `LICH_APPEARS`

Có thể tái dùng cảnh báo Mini, khác biệt chính bằng BGM Heavy và gain; chưa có boss entrance roar.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) — **0.123s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) — **0.258s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 28. Lich projectile / fan — `LICH_PROJECTILE`

Có thể dùng chung magic/electric release với Lightning, nhưng hiện chưa có file đúng vai trò.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 29. Lich AoE warning — `LICH_AOE_WARNING`

Dùng cùng một warning palette, ưu tiên audible/readable hơn nhiều cue mới.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) — **0.123s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 30. Lich AoE impact — `LICH_AOE_IMPACT`

Có thể chia sẻ impact với Fireball/Mini slam sau duyệt, chưa có candidate đủ cơ sở.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 31. Lich Phase 2 — `LICH_PHASE2`

Cue transition UI tạm; giữ cùng BGM, không restart nhạc mỗi frame phase2.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) — **0.258s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) — **0.123s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 32. Lich defeated — `LICH_DEFEATED`

Cue dứt điểm ngắn; tránh chồng cùng cue Victory sau Death0,67s.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/confirmation_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg) — **0.490s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

## UI

### 33. Button tap — `UI_TAP`

Tên click và thời lượng ngắn hợp UI; chưa nghe chọn màu sắc cuối.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/click_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_001.ogg) — **0.100s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/click_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_002.ogg) — **0.010s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 34. Navigation / đổi lựa chọn — `UI_NAVIGATION`

Select ngắn cho mode/difficulty/tab; không phát do loadValues/recreate tự đặt lựa chọn.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/select_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_001.ogg) — **0.043s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/select_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_002.ogg) — **0.043s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 35. Confirm — `UI_CONFIRM`

Confirmation positive, dùng thay click khi commit quan trọng.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/confirmation_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg) — **0.290s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) — **0.322s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 36. Back / Cancel — `UI_CANCEL`

Nhóm back đúng chức năng, có thể dùng cả toolbar và hardware Back nếu xử lý thành công.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/back_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_001.ogg) — **0.064s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/back_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_002.ogg) — **0.070s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 37. Pause — `UI_PAUSE`

Minimize/back làm cue đóng hành động; chỉ cho user pause, không cho lifecycle tự pause.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/minimize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_001.ogg) — **0.258s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/back_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_001.ogg) — **0.064s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 38. Resume — `UI_RESUME`

Maximize/open làm cue trở lại; không tự phát khi Activity.onResume nhưng game vẫn chờ Resume.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) — **0.258s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/open_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_001.ogg) — **0.148s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 39. Game Over — `UI_GAME_OVER`

Negative UI cue tạm thay losing sting; chưa biết có hợp cảm xúc kết trận, cần nghe.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/error_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_003.ogg) — **0.533s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/error_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_004.ogg) — **0.103s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 40. Victory — `UI_VICTORY`

Cue positive ngắn; có thể chỉ phát LICH_DEFEATED và để Victory không thêm cue nhằm tránh lặp.

- [ ] Chọn A: [`kenney_interface-sounds/Audio/confirmation_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg) — **0.490s**; review: không có flag vượt ngưỡng.
- [ ] Chọn B: [`kenney_interface-sounds/Audio/confirmation_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_002.ogg) — **0.539s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

## Enemy

### 41. Enemy nhận damage — `ENEMY_HIT`

Impact chung vật lý, tái dùng với weapon hit và chọn một owner phát để tránh double sound.

- [ ] Chọn A: [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg) — **0.240s**; review: PCM_FULL_SCALE_REVIEW.
- [ ] Chọn B: [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) — **0.415s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 42. Enemy death chung — `ENEMY_DEATH`

Proxy body/drop foley, không phải vocal đã được nghe xác nhận; có thể bỏ nếu weapon impact đã đủ rõ.

- [ ] Chọn A: [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) — **0.415s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 43. Flying reaction riêng — `ENEMY_FLYING_REACTION`

Chưa thấy wing/insect/flying reaction phù hợp; generic hit/death có thể dùng trong demo.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 44. Beast reaction riêng — `ENEMY_BEAST_REACTION`

Chưa có grunt/beast reaction đã nhận diện, không cần mỗi loài một file.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 45. Undead reaction riêng — `ENEMY_UNDEAD_REACTION`

Chưa có bone/undead vocal được xác nhận.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 46. Tank / armor reaction — `ENEMY_TANK_REACTION`

Metal foley có thể hợp armor, nhưng RMS thấp và không đại diện mọi tank đá/cây.

- [ ] Chọn A: [`kenney_rpg-audio/Audio/metalClick.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalClick.ogg) — **0.446s**; review: không có flag vượt ngưỡng.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 47. Legacy boar charge — `ENEMY_LEGACY_CHARGE`

Có speed burst thật cho legacy_charge nhưng chưa có charge/roar riêng; không cần bổ sung để hoàn thành demo.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 48. Player hurt — `PLAYER_HURT`

Thiếu cue đau/va chạm riêng rõ ràng; vũ khí/Boss impact hiện là fallback, tránh dùng UI error tùy tiện.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

### 49. Player death — `PLAYER_DEATH`

Có thể dùng GameOver cue sau animation thay voice riêng; không bắt buộc vocal trong demo.

**MISSING** — chưa có ứng viên đủ cơ sở; xem danh sách thiếu.

- [ ] Đồng ý với candidate đã chọn
- [ ] Thay / cần âm thanh khác
- [ ] Chưa chọn

Ghi chú nghe thử: __________________________

## Những quyết định nên chốt trước

1. Chọn palette BGM (chiptune/orchestral/metal có chấp nhận phối chung không); Day Hope hay Battle; Menu/Night có dùng chung End Dungeon; Game Over chọn im lặng hay cần nhạc riêng.
2. Nghe vòng nối chiptune end→~7,5s; không duyệt chỉ nghe đoạn đầu. End Dungeon là MP3, cần nghe cả loop/backend sau này. Nghe đối chiếu gain các BGM, không chọn chỉ theo BPM.
3. Chốt Sword swing/hit, Bow proxy có đủ dùng không, pickup glass và UI confirm/back; cho phép dùng chung và random variant đã nghe.
4. Ba nhóm thiếu quan trọng: fire cast, heavy/explosion impact, magic/electric. Có thể chia sẻ file cho nhiều event, không cần tải một gói lớn chỉ để đủ tên.
5. Cue player hurt và nhóm enemy có cần riêng trong demo hay tạm dùng impact chung. Các file có flag chỉ cần nghe đánh giá, chưa đồng ý normalize/trim/transcode.

Backlog Future (Dash/Ultimate/Evolution/extended specials) xem matrix, chưa yêu cầu chọn âm hoặc triển khai. Sau khi gửi lựa chọn, Task05.1B vẫn cần được yêu cầu riêng; audit không tự bắt đầu tích hợp.
