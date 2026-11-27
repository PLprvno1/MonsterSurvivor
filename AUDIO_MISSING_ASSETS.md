# Audio Missing Assets — Task 05.1A

`MISSING` nghĩa là chưa nhận diện được file phù hợp từ nguồn hiện có bằng tên/mô tả/metadata; **chưa nghe mọi file** nên không khẳng định chúng tuyệt đối không thể tái sử dụng. Không yêu cầu tải thêm nếu chủ dự án nghe và duyệt một candidate hiện có. Không có license block đang chưa xác minh trong tập hiện tại.

## Thiếu có ảnh hưởng cốt lõi

| Mức | Loại âm cụ thể | Event cần | Đặc điểm mong muốn | Khả năng dùng chung / nguồn hiện tại |
| --- | --- | --- | --- | --- |
| REQUIRED | Fire / magic cast whoosh ngắn | WEAPON_FIRE_CAST | Khoảng0,1–0,4s, onset rõ, không voice/nhạc | Một file dùng cho một fan cast. Chưa có file được nhận diện; không dùng door/open làm fire chỉ để đủ bảng. |
| REQUIRED | Heavy impact / compact explosion | WEAPON_FIRE_EXPLOSION, MINI_SLAM_IMPACT, LICH_AOE_IMPACT | Khoảng0,2–0,8s, thump có lực, tail gọn | Có thể dùng1–2 file chia sẻ bằng gain/pitch sau duyệt; chop hiện có chỉ là blade/physical proxy, không khẳng định blast/stone. |
| REQUIRED | Electric zap / magic bolt | WEAPON_LIGHTNING_ACTIVATE/IMPACT, LICH_PROJECTILE | Khoảng0,1–0,5s, transient rõ và tail ngắn | Một zap có thể dùng release/impact khác gain; không tự gán glitch UI là điện khi chưa nghe. |
| RECOMMENDED | Player hurt feedback | PLAYER_HURT | Ngắn, rõ khi HP thật giảm, có thể không vocal | Hiện có impact Boss/vũ khí làm fallback; chỉ bổ sung nếu cần nhận biết player nhận damage. |
| RECOMMENDED nếu từ chối proxy | Bow twang / arrow release | WEAPON_BOW_RELEASE | Một dây bật ngắn, không sounding như nút UI | Nghe pluck_002/001 trước. Nếu đủ demo thì không cần file mới. |
| RECOMMENDED nếu từ chối proxy | Body/creature death reaction chung | ENEMY_DEATH | 0,15–0,5s, dùng chung nhiều loài | Nghe dropLeather trước; không cần82 âm hoặc vocal riêng cho từng loài. |
| OPTIONAL | Flying/beast/undead reaction groups | ENEMY_FLYING/BEAST/UNDEAD_REACTION | Một palette nhỏ theo nhóm thật sự phân biệt | Generic impact đã có, chưa cần trong demo. |
| OPTIONAL | Boss entrance/phase riser/roar | MINI_APPEARS, LICH_APPEARS/PHASE2 | Một cue0,3–1s không che cảnh báo tiếp theo | Bong/maximize hiện có là cue UI tạm; không bắt buộc thêm nếu người dùng chấp nhận. |
| OPTIONAL | Victory/GameOver sting hoặc menu theme riêng | BGM_GAME_OVER, UI_VICTORY/GAME_OVER, BGM_MENU | Sting0,5–2s nếu muốn; theme menu không bắt buộc riêng | Bốn BGM có thể dùng lại, confirmation/error làm result cue. End Dungeon dài165,564s là dungeon theme, không phải sting thắng. |
| OPTIONAL / FUTURE | Dash / Ultimate / Evolution / extended special | FUTURE_* | Chỉ xác định sau khi có gameplay/timing | Không hook vào clip/catalog để giả có action. Không triển khai ở audit. |

## Đã đủ candidate để chủ dự án chọn, không cần xin thêm ngay

UI tap/select/confirm/back/pause/resume; pickup glass/weapon open/commit/upgrade; sword slice/chop; armor metal click; BGM Day/Night/Mini/Final và reuse menu/Victory. Tất cả candidate có thật trong [matrix](AUDIO_EVENT_MATRIX.md). Hãy nghe và đánh giá trước; tên phù hợp chưa bảo đảm chất âm.

## Việc kỹ thuật không phải thiếu asset

- Loop chiptune cần entry0 rồi repeat xấp xỉ7,5s theo tác giả; hiện chưa có exact loop sample tag. Cần xác định seam sau khi nghe, không phát `setLooping(true)` rồi gọi là seamless.
- MP3 End Dungeon có delay/padding và loop gap cần kiểm tra backend. Cả Ogg cũng cần kiểm tra Android sau tích hợp; audit decode local không bảo đảm gapless.
- 26 file có full-scale sample,1 file DC offset>1%, vài lead/tail dài và RMS chênh lệch. Cần nghe/gain tuning hoặc xin duyệt derivative nếu cần chỉnh. Không phải26 file hỏng, không cần tải bản mới hàng loạt.
- Preview26,127s là montage nguồn, không nạp như SFX/BGM trong game. RPG local51 SFX so với trang50 không tự chứng minh thiếu/hỏng; giữ nguyên.
- License đã đối chiếu riêng sáu nguồn. Không cần bổ sung license link cho bốn BGM hiện có, nhưng nên giữ provenance/credit khi lựa chọn được đóng gói ở task sau.

Ưu tiên tối thiểu: duyệt candidate đang có; nếu các khoảng trống combat còn quan trọng, tìm **ba nhóm âm cụ thể** ở trên thay vì yêu cầu một pack lớn. Task này không tải thêm hoặc chỉnh nguồn.
