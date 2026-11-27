# Audio Asset Inventory — Task 05.1A

Ngày audit:14/11/2026. Mọi đường dẫn audio trong tài liệu tính từ **`asset_sources/audio/`**, giữ nguyên chữ hoa, dấu gạch nối và extension thật. Không đổi tên, chỉnh, chuyển hay đóng gói file nguồn.

## Phạm vi và phương pháp

Quét đệ quy **162 file**: **156 audio =4 BGM +151 SFX +1 Preview**, cùng6 License/URL. Format155 Ogg Vorbis +1 MP3. Audio nén tổng12,935,717 bytes (12.336MiB), duration tổng576.853s. BGM495,382s; SFX55,344s; Preview26,127s. Không có file trùng SHA-256.

Không có ffprobe/ffmpeg trong PATH. `tools/audit_audio_assets.py` + `tools/audio/AudioProbe.java` dùng Java17 và decoder **đã cache của project**: libGDX1.14.2/JOrbis0.0.17, JLayer1.0.1-gdx, LWJGL BufferUtils. Đọc Vorbis header/comment/granule, kiểm CRC/sequence/EOS từng Ogg page và **decode hết156 file thành PCM trong bộ nhớ**; không phát âm thanh hoặc ghi file audio. MP3 duration là số frame PCM decoded, có thể chứa encoder delay/padding. Ogg duration lấy granule/rate, được so với decoder. Decode OK không phải kiểm thử MediaPlayer/SoundPool Android.

**Chưa nghe trực tiếp, chưa đo LUFS/true peak/phổ hoặc xác nhận musical seam.** PCM peak/RMS và endpoint jump không chứng minh cảm giác âm thanh, clipping gốc hoặc loop liền mạch. Mọi lựa chọn cần người dùng nghe duyệt. Công cụ không normalize/transcode/trim.

## License và provenance

| Nguồn | Tác giả | License / trạng thái | Bằng chứng |
| --- | --- | --- | --- |
| Kenney Interface Sounds | Kenney | CC0-1.0 / VERIFIED_LOCAL_AND_OFFICIAL | License local ghi Interface Sounds1.0/11-02-2020; trang chính thức100 files/CC0. [Nguồn chính thức](https://kenney.nl/assets/interface-sounds) / `kenney_interface-sounds/License.txt` |
| Kenney RPG Audio | Kenney | CC0-1.0 / VERIFIED_LOCAL_AND_OFFICIAL | License local RPG Audio; trang chính thức50 files, thực tế51 SFX + Preview, không tự xóa file chênh lệch. [Nguồn chính thức](https://kenney.nl/assets/rpg-audio) / `kenney_rpg-audio/License.txt` |
| battle_music_01-loop.ogg | pmiller | CC0 / VERIFIED_SOURCE_PAGE | Zone.Identifier trỏ đúng bài/file; trang tác giả chỉ định lặp từ khoảng7,5s. [Nguồn chính thức](https://opengameart.org/content/chiptune-battle-music) |
| end_dungeon.mp3 | HydroGene | CC0 / VERIFIED_SOURCE_PAGE | Zone.Identifier host end_dungeon_0.mp3, tên local end_dungeon.mp3; trang ghi dungeon theme/loopable. [Nguồn chính thức](https://opengameart.org/content/jrpg-end-dungeon) |
| heavy_boss_battle_1_bpm200.ogg | MintoDog | CC0 / VERIFIED_SOURCE_PAGE | Zone.Identifier host có hậu tố_0; tags TITLE/ARTIST/BPM khớp; trang ghi boss/metal/loopable. [Nguồn chính thức](https://opengameart.org/content/heavy-boss-battle-1) |
| hope_orchestral_battle_music_bpm165.ogg | MintoDog | CC0 / VERIFIED_SOURCE_PAGE | Zone.Identifier và tags khớp; trang tác giả mô tả bright/orchestral/loopable. [Nguồn chính thức](https://opengameart.org/content/hopeorchestral-battle-music) |

Bốn bài được đối chiếu riêng từ URL ReferrerUrl/HostUrl trong **NTFS Zone.Identifier** của chính file, cộng trang OpenGameArt tương ứng; không suy tất cả OpenGameArt là CC0. Một số lần open trả403, nội dung trang chính thức vẫn có trong kết quả tìm kiếm của chính domain. Không tải lại asset hoặc xác minh byte/hash với file remote. Tags artist/year/BPM chỉ bổ sung provenance; không tự là giấy phép. Credit tùy chọn nên ghi Kenney, pmiller, HydroGene, MintoDog và URL tương ứng. Không có LICENSE_UNVERIFIED trong tập hiện tại; file mới thiếu nguồn phải dùng trạng thái đó, không kế thừa license của hàng xóm.

Tên thư mục thực tế là **`kenney_rpg-audio`**, không phải `kenney_rpg_audio`. Website ghi50 sound, local có51 trong Audio và1 Preview; audit giữ số thực tế, không coi chênh lệch là lỗi decode. Không có README; hai License local vẫn đủ thông tin Kenney. Preview không phải một BGM thứ năm và không đưa vào runtime.

## Từng file: định dạng, metadata và khả năng sử dụng

Bitrate dưới đây là **kbit/s trung bình toàn file nén**, gồm container/header, không phải nominal bitrate của Vorbis. Nominal/tags/SHA/provenance có trong [JSON](core/build/reports/task05_1a/audio-audit.json); nominal Vorbis thay đổi theo encoder/pack, không suy từ bitrate trung bình. MP3 frame đầu128kbit/s, không thấy thay đổi bitrate. Tất cả decode OK, Ogg CRC/sequence/EOS không có lỗi. Một số decoder Ogg trả thêm padding dưới23ms ngoài granule; phép đo vùng sample/lead/tail ở đây chặn đúng granule của nguồn, không ghi padding thành asset bị lỗi.

Loop: `ONE_SHOT`=SFX một lần, không có cơ sở thành loop; `AUTHOR_7.5`=tác giả yêu cầu loop-start xấp xỉ7,5s; `AUTHOR_LOOP`=tác giả ghi loopable nhưng chưa nghe/backend test; `MP3_RISK`=author-loopable nhưng có delay/padding/gap; `EXCLUDE`=Preview. Cột vấn đề chỉ là flag review, xem định nghĩa sau bảng.

| Đường dẫn / filename thật | Phân loại | Format/codec | Bytes | Duration s | Hz | Ch | kbit/s avg | Decode | Loop | Review |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| [`battle_music_01-loop.ogg`](asset_sources/audio/battle_music_01-loop.ogg) | BGM | ogg/Vorbis | 2724366 | 152.000 | 44100 | 2 | 143.4 | OK | AUTHOR_7.5 | PCM_FULL_SCALE_REVIEW, DC_OFFSET_REVIEW, MUSIC_NOT_SOUND_SIZE |
| [`end_dungeon.mp3`](asset_sources/audio/end_dungeon.mp3) | BGM | mp3/MP3 | 2649095 | 165.564 | 44100 | 2 | 128.0 | OK | MP3_RISK | MUSIC_NOT_SOUND_SIZE |
| [`heavy_boss_battle_1_bpm200.ogg`](asset_sources/audio/heavy_boss_battle_1_bpm200.ogg) | BGM | ogg/Vorbis | 3360172 | 108.000 | 44100 | 2 | 248.9 | OK | AUTHOR_LOOP | MUSIC_NOT_SOUND_SIZE |
| [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg) | BGM | ogg/Vorbis | 2192846 | 69.818 | 44100 | 2 | 251.3 | OK | AUTHOR_LOOP | MUSIC_NOT_SOUND_SIZE |
| [`kenney_interface-sounds/Audio/back_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_001.ogg) | UI / back | ogg/Vorbis | 6997 | 0.064 | 44100 | 1 | 876.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/back_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_002.ogg) | UI / back | ogg/Vorbis | 7029 | 0.070 | 44100 | 1 | 805.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/back_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_003.ogg) | UI / back | ogg/Vorbis | 7208 | 0.093 | 44100 | 1 | 620.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/back_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_004.ogg) | UI / back | ogg/Vorbis | 6923 | 0.073 | 44100 | 1 | 758.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) | UI / bong | ogg/Vorbis | 4848 | 0.123 | 44100 | 1 | 315.7 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/click_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_001.ogg) | UI / click | ogg/Vorbis | 4876 | 0.100 | 44100 | 1 | 390.0 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/click_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_002.ogg) | UI / click | ogg/Vorbis | 4275 | 0.010 | 44100 | 1 | 3412.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/click_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_003.ogg) | UI / click | ogg/Vorbis | 4371 | 0.010 | 44100 | 1 | 3488.9 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/click_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_004.ogg) | UI / click | ogg/Vorbis | 4486 | 0.010 | 44100 | 1 | 3580.7 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/click_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_005.ogg) | UI / click | ogg/Vorbis | 4410 | 0.010 | 44100 | 1 | 3520.0 | OK | ONE_SHOT | DC_OFFSET_REVIEW |
| [`kenney_interface-sounds/Audio/close_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_001.ogg) | UI / close | ogg/Vorbis | 9915 | 0.148 | 44100 | 1 | 536.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/close_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_002.ogg) | UI / close | ogg/Vorbis | 14752 | 0.314 | 44100 | 1 | 376.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/close_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_003.ogg) | UI / close | ogg/Vorbis | 15648 | 0.314 | 44100 | 1 | 398.9 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/close_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_004.ogg) | UI / close | ogg/Vorbis | 16263 | 0.323 | 44100 | 1 | 402.9 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/confirmation_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg) | UI / confirmation | ogg/Vorbis | 8968 | 0.290 | 44100 | 1 | 247.5 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/confirmation_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_002.ogg) | UI / confirmation | ogg/Vorbis | 14169 | 0.539 | 44100 | 1 | 210.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) | UI / confirmation | ogg/Vorbis | 9535 | 0.322 | 44100 | 1 | 236.9 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/confirmation_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg) | UI / confirmation | ogg/Vorbis | 12391 | 0.490 | 44100 | 1 | 202.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/drop_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_001.ogg) | UI / drop | ogg/Vorbis | 5859 | 0.110 | 44100 | 2 | 426.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/drop_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_002.ogg) | UI / drop | ogg/Vorbis | 6411 | 0.191 | 44100 | 2 | 268.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/drop_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_003.ogg) | UI / drop | ogg/Vorbis | 6288 | 0.191 | 44100 | 2 | 263.2 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/drop_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_004.ogg) | UI / drop | ogg/Vorbis | 7230 | 0.287 | 44100 | 2 | 201.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/error_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_001.ogg) | UI / error | ogg/Vorbis | 7373 | 0.165 | 44100 | 2 | 358.4 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/error_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_002.ogg) | UI / error | ogg/Vorbis | 7369 | 0.165 | 44100 | 2 | 358.2 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW, ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/error_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_003.ogg) | UI / error | ogg/Vorbis | 12256 | 0.533 | 44100 | 2 | 183.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/error_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_004.ogg) | UI / error | ogg/Vorbis | 6393 | 0.103 | 44100 | 2 | 498.0 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/error_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_005.ogg) | UI / error | ogg/Vorbis | 7266 | 0.500 | 44100 | 1 | 116.2 | OK | ONE_SHOT | LEAD_GT_30MS, TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/error_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_006.ogg) | UI / error | ogg/Vorbis | 8288 | 0.500 | 44100 | 1 | 132.6 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/error_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_007.ogg) | UI / error | ogg/Vorbis | 10225 | 0.192 | 44100 | 1 | 426.1 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/error_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_008.ogg) | UI / error | ogg/Vorbis | 9215 | 0.139 | 44100 | 1 | 529.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/glass_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_001.ogg) | UI / glass | ogg/Vorbis | 6103 | 0.278 | 44100 | 1 | 175.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) | UI / glass | ogg/Vorbis | 4917 | 0.125 | 44100 | 1 | 313.9 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/glass_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_003.ogg) | UI / glass | ogg/Vorbis | 5732 | 0.124 | 44100 | 1 | 370.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/glass_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_004.ogg) | UI / glass | ogg/Vorbis | 8323 | 0.692 | 44100 | 1 | 96.2 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/glass_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_005.ogg) | UI / glass | ogg/Vorbis | 6206 | 0.111 | 44100 | 1 | 448.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/glass_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_006.ogg) | UI / glass | ogg/Vorbis | 6956 | 0.111 | 44100 | 1 | 502.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/glitch_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_001.ogg) | UI / glitch | ogg/Vorbis | 5121 | 0.020 | 44100 | 2 | 2046.1 | OK | ONE_SHOT | DC_OFFSET_REVIEW, ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/glitch_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_002.ogg) | UI / glitch | ogg/Vorbis | 5473 | 0.030 | 44100 | 2 | 1458.4 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/glitch_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_003.ogg) | UI / glitch | ogg/Vorbis | 4915 | 0.010 | 44100 | 2 | 3923.1 | OK | ONE_SHOT | DC_OFFSET_REVIEW, ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/glitch_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_004.ogg) | UI / glitch | ogg/Vorbis | 5225 | 0.023 | 44100 | 2 | 1825.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) | UI / maximize | ogg/Vorbis | 12111 | 0.258 | 44100 | 1 | 375.0 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_002.ogg) | UI / maximize | ogg/Vorbis | 9915 | 0.258 | 44100 | 1 | 307.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_003.ogg) | UI / maximize | ogg/Vorbis | 11748 | 0.212 | 44100 | 1 | 443.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_004.ogg) | UI / maximize | ogg/Vorbis | 17723 | 0.418 | 44100 | 1 | 339.2 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_005.ogg) | UI / maximize | ogg/Vorbis | 20900 | 0.526 | 44100 | 1 | 317.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_006.ogg) | UI / maximize | ogg/Vorbis | 11618 | 0.380 | 44100 | 1 | 244.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_007.ogg) | UI / maximize | ogg/Vorbis | 8097 | 0.186 | 44100 | 1 | 348.7 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_008.ogg) | UI / maximize | ogg/Vorbis | 9085 | 0.225 | 44100 | 1 | 322.5 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/maximize_009.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_009.ogg) | UI / maximize | ogg/Vorbis | 7650 | 0.225 | 44100 | 1 | 272.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_001.ogg) | UI / minimize | ogg/Vorbis | 11924 | 0.258 | 44100 | 1 | 369.2 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_002.ogg) | UI / minimize | ogg/Vorbis | 9441 | 0.258 | 44100 | 1 | 292.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_003.ogg) | UI / minimize | ogg/Vorbis | 11739 | 0.212 | 44100 | 1 | 443.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_004.ogg) | UI / minimize | ogg/Vorbis | 17099 | 0.418 | 44100 | 1 | 327.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_005.ogg) | UI / minimize | ogg/Vorbis | 19501 | 0.526 | 44100 | 1 | 296.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_006.ogg) | UI / minimize | ogg/Vorbis | 11292 | 0.380 | 44100 | 1 | 237.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_007.ogg) | UI / minimize | ogg/Vorbis | 7677 | 0.186 | 44100 | 1 | 330.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_008.ogg) | UI / minimize | ogg/Vorbis | 9298 | 0.225 | 44100 | 1 | 330.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/minimize_009.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_009.ogg) | UI / minimize | ogg/Vorbis | 7665 | 0.225 | 44100 | 1 | 272.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/open_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_001.ogg) | UI / open | ogg/Vorbis | 9834 | 0.148 | 44100 | 1 | 532.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/open_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_002.ogg) | UI / open | ogg/Vorbis | 14975 | 0.314 | 44100 | 1 | 381.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/open_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_003.ogg) | UI / open | ogg/Vorbis | 15046 | 0.314 | 44100 | 1 | 383.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/open_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_004.ogg) | UI / open | ogg/Vorbis | 15879 | 0.323 | 44100 | 1 | 393.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/pluck_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_001.ogg) | UI / pluck | ogg/Vorbis | 6317 | 0.102 | 44100 | 2 | 493.4 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW, ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/pluck_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_002.ogg) | UI / pluck | ogg/Vorbis | 6941 | 0.165 | 44100 | 2 | 337.4 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/question_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_001.ogg) | UI / question | ogg/Vorbis | 11700 | 0.491 | 44100 | 1 | 190.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/question_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_002.ogg) | UI / question | ogg/Vorbis | 9455 | 0.333 | 44100 | 1 | 227.5 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/question_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_003.ogg) | UI / question | ogg/Vorbis | 8233 | 0.332 | 44100 | 1 | 198.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/question_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_004.ogg) | UI / question | ogg/Vorbis | 9821 | 0.332 | 44100 | 1 | 236.3 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scratch_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_001.ogg) | UI / scratch | ogg/Vorbis | 8989 | 0.139 | 44100 | 1 | 516.2 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scratch_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_002.ogg) | UI / scratch | ogg/Vorbis | 9319 | 0.139 | 44100 | 1 | 535.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scratch_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_003.ogg) | UI / scratch | ogg/Vorbis | 8849 | 0.123 | 44100 | 1 | 573.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scratch_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_004.ogg) | UI / scratch | ogg/Vorbis | 16478 | 0.325 | 44100 | 1 | 405.5 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scratch_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_005.ogg) | UI / scratch | ogg/Vorbis | 15824 | 0.325 | 44100 | 1 | 389.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scroll_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_001.ogg) | UI / scroll | ogg/Vorbis | 21633 | 1.000 | 44100 | 1 | 173.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scroll_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_002.ogg) | UI / scroll | ogg/Vorbis | 24628 | 1.000 | 44100 | 1 | 197.0 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scroll_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_003.ogg) | UI / scroll | ogg/Vorbis | 29488 | 1.000 | 44100 | 1 | 235.9 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scroll_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_004.ogg) | UI / scroll | ogg/Vorbis | 20348 | 1.000 | 44100 | 1 | 162.8 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/scroll_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_005.ogg) | UI / scroll | ogg/Vorbis | 20448 | 1.000 | 44100 | 1 | 163.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/select_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_001.ogg) | UI / select | ogg/Vorbis | 5468 | 0.043 | 44100 | 2 | 1010.5 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/select_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_002.ogg) | UI / select | ogg/Vorbis | 5463 | 0.043 | 44100 | 2 | 1009.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/select_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_003.ogg) | UI / select | ogg/Vorbis | 6901 | 0.383 | 44100 | 1 | 144.2 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/select_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_004.ogg) | UI / select | ogg/Vorbis | 6909 | 0.383 | 44100 | 1 | 144.4 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/select_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_005.ogg) | UI / select | ogg/Vorbis | 6767 | 0.383 | 44100 | 1 | 141.4 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/select_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_006.ogg) | UI / select | ogg/Vorbis | 8101 | 1.944 | 44100 | 1 | 33.3 | OK | ONE_SHOT | LEAD_GT_30MS, TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/select_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_007.ogg) | UI / select | ogg/Vorbis | 6580 | 0.047 | 44100 | 1 | 1121.5 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/select_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_008.ogg) | UI / select | ogg/Vorbis | 6339 | 0.047 | 44100 | 1 | 1080.4 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/switch_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_001.ogg) | UI / switch | ogg/Vorbis | 6753 | 0.618 | 44100 | 2 | 87.5 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/switch_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_002.ogg) | UI / switch | ogg/Vorbis | 7097 | 0.611 | 44100 | 2 | 92.9 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/switch_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_003.ogg) | UI / switch | ogg/Vorbis | 9653 | 0.500 | 44100 | 2 | 154.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/switch_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_004.ogg) | UI / switch | ogg/Vorbis | 7248 | 0.500 | 44100 | 2 | 116.0 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/switch_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_005.ogg) | UI / switch | ogg/Vorbis | 7814 | 0.612 | 44100 | 2 | 102.2 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/switch_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_006.ogg) | UI / switch | ogg/Vorbis | 7873 | 0.611 | 44100 | 2 | 103.2 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/switch_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_007.ogg) | UI / switch | ogg/Vorbis | 7921 | 0.614 | 44100 | 2 | 103.1 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_interface-sounds/Audio/tick_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/tick_001.ogg) | UI / tick | ogg/Vorbis | 4497 | 0.023 | 44100 | 1 | 1549.4 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/tick_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/tick_002.ogg) | UI / tick | ogg/Vorbis | 4514 | 0.023 | 44100 | 1 | 1555.2 | OK | ONE_SHOT | ENDPOINT_JUMP_REVIEW |
| [`kenney_interface-sounds/Audio/tick_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/tick_004.ogg) | UI / tick | ogg/Vorbis | 5031 | 0.055 | 44100 | 1 | 734.4 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/toggle_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_001.ogg) | UI / toggle | ogg/Vorbis | 7369 | 0.139 | 44100 | 1 | 423.1 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/toggle_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_002.ogg) | UI / toggle | ogg/Vorbis | 8056 | 0.139 | 44100 | 1 | 462.6 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/toggle_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_003.ogg) | UI / toggle | ogg/Vorbis | 8654 | 0.139 | 44100 | 1 | 496.9 | OK | ONE_SHOT | — |
| [`kenney_interface-sounds/Audio/toggle_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_004.ogg) | UI / toggle | ogg/Vorbis | 6695 | 0.066 | 44100 | 1 | 809.5 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/beltHandle1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/beltHandle1.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 10820 | 0.277 | 48000 | 2 | 312.8 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/beltHandle2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/beltHandle2.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 15410 | 0.477 | 48000 | 2 | 258.7 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/bookClose.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookClose.ogg) | SFX foley / book | ogg/Vorbis | 9292 | 0.231 | 48000 | 2 | 322.4 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/bookFlip1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookFlip1.ogg) | SFX foley / book | ogg/Vorbis | 18248 | 0.769 | 48000 | 2 | 189.9 | OK | ONE_SHOT | LEAD_GT_30MS, PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/bookFlip2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookFlip2.ogg) | SFX foley / book | ogg/Vorbis | 12393 | 0.430 | 48000 | 2 | 230.3 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/bookFlip3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookFlip3.ogg) | SFX foley / book | ogg/Vorbis | 9077 | 0.231 | 48000 | 2 | 314.9 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/bookOpen.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookOpen.ogg) | SFX foley / book | ogg/Vorbis | 8273 | 0.154 | 48000 | 2 | 430.5 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/bookPlace1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookPlace1.ogg) | SFX foley / book | ogg/Vorbis | 9901 | 0.277 | 48000 | 2 | 286.3 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/bookPlace2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookPlace2.ogg) | SFX foley / book | ogg/Vorbis | 11185 | 0.354 | 48000 | 2 | 253.1 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/bookPlace3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookPlace3.ogg) | SFX foley / book | ogg/Vorbis | 9570 | 0.261 | 48000 | 2 | 293.0 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg) | SFX foley / blade candidate | ogg/Vorbis | 9370 | 0.240 | 48000 | 2 | 312.0 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/cloth1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth1.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 16508 | 0.661 | 48000 | 2 | 199.8 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/cloth2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth2.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 11787 | 0.415 | 48000 | 2 | 227.2 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/cloth3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth3.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 12432 | 0.477 | 48000 | 2 | 208.7 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/cloth4.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth4.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 11043 | 0.384 | 48000 | 2 | 229.9 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/clothBelt.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/clothBelt.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 21491 | 0.723 | 48000 | 2 | 238.0 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/clothBelt2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/clothBelt2.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 20013 | 0.753 | 48000 | 2 | 212.5 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_rpg-audio/Audio/creak1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/creak1.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 15761 | 0.661 | 48000 | 2 | 190.7 | OK | ONE_SHOT | LEAD_GT_30MS |
| [`kenney_rpg-audio/Audio/creak2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/creak2.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 19012 | 0.830 | 48000 | 2 | 183.2 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/creak3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/creak3.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 10408 | 0.338 | 48000 | 2 | 246.2 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/doorClose_1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_1.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 18564 | 0.675 | 48000 | 2 | 220.0 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/doorClose_2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_2.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 17598 | 0.612 | 48000 | 2 | 229.9 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/doorClose_3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_3.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 20889 | 0.714 | 48000 | 2 | 234.1 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/doorClose_4.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_4.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 16902 | 0.603 | 48000 | 2 | 224.1 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/doorOpen_1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorOpen_1.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 22848 | 0.919 | 48000 | 2 | 198.9 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/doorOpen_2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorOpen_2.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 30280 | 1.413 | 48000 | 2 | 171.4 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/drawKnife1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/drawKnife1.ogg) | SFX foley / blade candidate | ogg/Vorbis | 11134 | 0.400 | 48000 | 2 | 222.8 | OK | ONE_SHOT | LEAD_GT_30MS, LOW_RMS_REVIEW |
| [`kenney_rpg-audio/Audio/drawKnife2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/drawKnife2.ogg) | SFX foley / blade candidate | ogg/Vorbis | 12395 | 0.446 | 48000 | 2 | 222.4 | OK | ONE_SHOT | LOW_RMS_REVIEW |
| [`kenney_rpg-audio/Audio/drawKnife3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/drawKnife3.ogg) | SFX foley / blade candidate | ogg/Vorbis | 12726 | 0.477 | 48000 | 2 | 213.6 | OK | ONE_SHOT | LEAD_GT_30MS, TAIL_GT_100MS |
| [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 11695 | 0.415 | 48000 | 2 | 225.4 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/footstep00.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep00.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 9475 | 0.249 | 48000 | 2 | 304.6 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/footstep01.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep01.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 9900 | 0.282 | 48000 | 2 | 280.4 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/footstep02.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep02.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 9528 | 0.242 | 48000 | 2 | 314.8 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/footstep03.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep03.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 10559 | 0.269 | 48000 | 2 | 314.0 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/footstep04.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep04.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 11303 | 0.323 | 48000 | 2 | 280.1 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/footstep05.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep05.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 10288 | 0.276 | 48000 | 2 | 298.5 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/footstep06.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep06.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 9885 | 0.242 | 48000 | 2 | 326.6 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/footstep07.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep07.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 10264 | 0.256 | 48000 | 2 | 321.3 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/footstep08.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep08.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 9242 | 0.256 | 48000 | 2 | 289.3 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/footstep09.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep09.ogg) | SFX foley / footsteps (không gắn tự động) | ogg/Vorbis | 9356 | 0.215 | 48000 | 2 | 347.8 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/handleCoins.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleCoins.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 25394 | 0.846 | 48000 | 2 | 240.3 | OK | ONE_SHOT | LEAD_GT_30MS, PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/handleCoins2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleCoins2.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 13094 | 0.338 | 48000 | 2 | 309.7 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/handleSmallLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleSmallLeather.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 10492 | 0.338 | 48000 | 2 | 248.2 | OK | ONE_SHOT | TAIL_GT_100MS |
| [`kenney_rpg-audio/Audio/handleSmallLeather2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleSmallLeather2.ogg) | SFX foley / cloth, leather, coins, creak | ogg/Vorbis | 10028 | 0.307 | 48000 | 2 | 260.9 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/knifeSlice.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice.ogg) | SFX foley / blade candidate | ogg/Vorbis | 15532 | 0.600 | 48000 | 2 | 207.3 | OK | ONE_SHOT | LEAD_GT_30MS, TAIL_GT_100MS |
| [`kenney_rpg-audio/Audio/knifeSlice2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice2.ogg) | SFX foley / blade candidate | ogg/Vorbis | 15099 | 0.569 | 48000 | 2 | 212.4 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/metalClick.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalClick.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 13615 | 0.446 | 48000 | 2 | 244.3 | OK | ONE_SHOT | — |
| [`kenney_rpg-audio/Audio/metalLatch.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalLatch.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 10208 | 0.261 | 48000 | 2 | 312.5 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/metalPot1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalPot1.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 31619 | 1.460 | 48000 | 2 | 173.2 | OK | ONE_SHOT | LEAD_GT_30MS, PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/metalPot2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalPot2.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 22471 | 0.938 | 48000 | 2 | 191.7 | OK | ONE_SHOT | LEAD_GT_30MS, PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Audio/metalPot3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalPot3.ogg) | SFX foley / metal, mechanism | ogg/Vorbis | 23142 | 0.907 | 48000 | 2 | 204.1 | OK | ONE_SHOT | PCM_FULL_SCALE_REVIEW |
| [`kenney_rpg-audio/Preview.ogg`](asset_sources/audio/kenney_rpg-audio/Preview.ogg) | PREVIEW_NOT_RUNTIME | ogg/Vorbis | 321120 | 26.127 | 48000 | 2 | 98.3 | OK | EXCLUDE | PCM_FULL_SCALE_REVIEW, MUSIC_NOT_SOUND_SIZE |

## Từng file: quality measurements

Peak và RMS tính trên PCM16 decoded (không weighting). Silence: frame có peak tất cả channel≤32/32768, khoảng−60dBFS. FS samples đếm |sample|≥32767; codec có thể overshoot/clamp, **không tự kết luận file hỏng hoặc clipping nghe được**. Endpoint là |last−first|/32768 lớn nhất giữa các channel; chỉ là kiểm tra nối **end→0**, không xác minh seam end→7,5s của chiptune. PCM KiB là kích thước raw16-bit tương đương sample nguồn nếu nạp hết, chưa tính padding/overhead backend.

| File | Peak dBFS | RMS dBFS | Lead ms | Tail ms | FS samples | Endpoint jump | PCM KiB |
| --- | --- | --- | --- | --- | --- | --- | --- |
| [`battle_music_01-loop.ogg`](asset_sources/audio/battle_music_01-loop.ogg) | 0.00 | -16.05 | 0.4 | 0.0 | 28 | 0.0869 | 26184.4 |
| [`end_dungeon.mp3`](asset_sources/audio/end_dungeon.mp3) | -0.36 | -14.38 | 27.1 | 13.3 | 0 | 0.0000 | 28521.0 |
| [`heavy_boss_battle_1_bpm200.ogg`](asset_sources/audio/heavy_boss_battle_1_bpm200.ogg) | -1.00 | -13.16 | 0.0 | 0.0 | 0 | 0.0263 | 18604.7 |
| [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg) | -0.91 | -12.54 | 0.0 | 0.0 | 0 | 0.0653 | 12027.3 |
| [`kenney_interface-sounds/Audio/back_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_001.ogg) | -0.96 | -21.45 | 0.0 | 6.0 | 0 | 0.0001 | 5.5 |
| [`kenney_interface-sounds/Audio/back_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_002.ogg) | -0.96 | -15.85 | 0.0 | 0.0 | 0 | 0.0006 | 6.0 |
| [`kenney_interface-sounds/Audio/back_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_003.ogg) | -0.90 | -19.75 | 0.0 | 0.0 | 0 | 0.0001 | 8.0 |
| [`kenney_interface-sounds/Audio/back_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_004.ogg) | -0.94 | -16.67 | 0.0 | 0.0 | 0 | 0.0036 | 6.3 |
| [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) | -0.89 | -16.31 | 0.0 | 0.0 | 0 | 0.0147 | 10.6 |
| [`kenney_interface-sounds/Audio/click_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_001.ogg) | -1.35 | -26.52 | 0.0 | 0.0 | 0 | 0.0544 | 8.6 |
| [`kenney_interface-sounds/Audio/click_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_002.ogg) | -0.85 | -15.52 | 0.0 | 0.0 | 0 | 0.0572 | 0.9 |
| [`kenney_interface-sounds/Audio/click_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_003.ogg) | -1.36 | -16.03 | 0.0 | 0.0 | 0 | 0.0471 | 0.9 |
| [`kenney_interface-sounds/Audio/click_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_004.ogg) | -1.23 | -15.26 | 0.0 | 0.0 | 0 | 0.0465 | 0.9 |
| [`kenney_interface-sounds/Audio/click_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_005.ogg) | -1.22 | -12.96 | 0.0 | 0.0 | 0 | 0.0536 | 0.9 |
| [`kenney_interface-sounds/Audio/close_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_001.ogg) | -0.92 | -14.77 | 0.0 | 1.7 | 0 | 0.0026 | 12.7 |
| [`kenney_interface-sounds/Audio/close_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_002.ogg) | -1.03 | -16.37 | 0.0 | 3.2 | 0 | 0.0016 | 27.0 |
| [`kenney_interface-sounds/Audio/close_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_003.ogg) | -0.92 | -15.15 | 0.0 | 1.5 | 0 | 0.0027 | 27.0 |
| [`kenney_interface-sounds/Audio/close_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/close_004.ogg) | -1.01 | -17.36 | 0.0 | 2.1 | 0 | 0.0050 | 27.8 |
| [`kenney_interface-sounds/Audio/confirmation_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg) | -0.93 | -11.31 | 0.0 | 0.0 | 0 | 0.0013 | 25.0 |
| [`kenney_interface-sounds/Audio/confirmation_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_002.ogg) | -0.96 | -14.88 | 0.0 | 4.4 | 0 | 0.0009 | 46.4 |
| [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) | -0.98 | -15.22 | 0.0 | 2.4 | 0 | 0.0023 | 27.7 |
| [`kenney_interface-sounds/Audio/confirmation_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg) | -0.91 | -11.78 | 0.3 | 3.3 | 0 | 0.0000 | 42.2 |
| [`kenney_interface-sounds/Audio/drop_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_001.ogg) | -0.89 | -20.38 | 0.0 | 0.0 | 0 | 0.0047 | 18.9 |
| [`kenney_interface-sounds/Audio/drop_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_002.ogg) | -1.06 | -19.83 | 0.0 | 0.0 | 0 | 0.0167 | 32.9 |
| [`kenney_interface-sounds/Audio/drop_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_003.ogg) | -1.11 | -19.67 | 0.0 | 0.0 | 0 | 0.0018 | 32.9 |
| [`kenney_interface-sounds/Audio/drop_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/drop_004.ogg) | -0.86 | -20.70 | 0.0 | 0.0 | 0 | 0.0007 | 49.4 |
| [`kenney_interface-sounds/Audio/error_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_001.ogg) | -0.80 | -19.07 | 0.0 | 0.0 | 0 | 0.7057 | 28.3 |
| [`kenney_interface-sounds/Audio/error_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_002.ogg) | -0.00 | -17.53 | 0.0 | 0.0 | 2 | 0.6339 | 28.3 |
| [`kenney_interface-sounds/Audio/error_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_003.ogg) | -0.50 | -20.04 | 3.5 | 0.1 | 0 | 0.0008 | 91.9 |
| [`kenney_interface-sounds/Audio/error_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_004.ogg) | -1.25 | -16.30 | 0.0 | 0.0 | 0 | 0.0170 | 17.7 |
| [`kenney_interface-sounds/Audio/error_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_005.ogg) | -0.96 | -18.74 | 34.0 | 277.2 | 0 | 0.0001 | 43.1 |
| [`kenney_interface-sounds/Audio/error_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_006.ogg) | -0.78 | -18.00 | 21.2 | 234.3 | 0 | 0.0006 | 43.1 |
| [`kenney_interface-sounds/Audio/error_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_007.ogg) | -0.89 | -13.09 | 0.0 | 0.0 | 0 | 0.9143 | 16.5 |
| [`kenney_interface-sounds/Audio/error_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_008.ogg) | -0.96 | -20.55 | 0.0 | 0.0 | 0 | 0.0021 | 12.0 |
| [`kenney_interface-sounds/Audio/glass_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_001.ogg) | -1.03 | -20.09 | 0.0 | 0.0 | 0 | 0.0132 | 24.0 |
| [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) | -1.04 | -19.04 | 0.0 | 0.0 | 0 | 0.0113 | 10.8 |
| [`kenney_interface-sounds/Audio/glass_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_003.ogg) | -0.95 | -16.36 | 0.0 | 0.0 | 0 | 0.0118 | 10.7 |
| [`kenney_interface-sounds/Audio/glass_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_004.ogg) | -1.12 | -17.97 | 0.0 | 0.0 | 0 | 0.0227 | 59.6 |
| [`kenney_interface-sounds/Audio/glass_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_005.ogg) | -0.93 | -19.81 | 0.0 | 21.0 | 0 | 0.0014 | 9.5 |
| [`kenney_interface-sounds/Audio/glass_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_006.ogg) | -0.98 | -18.65 | 0.0 | 11.6 | 0 | 0.0019 | 9.5 |
| [`kenney_interface-sounds/Audio/glitch_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_001.ogg) | -0.99 | -10.08 | 0.0 | 0.0 | 0 | 0.2357 | 3.4 |
| [`kenney_interface-sounds/Audio/glitch_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_002.ogg) | -1.16 | -13.98 | 0.0 | 0.0 | 0 | 0.2172 | 5.2 |
| [`kenney_interface-sounds/Audio/glitch_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_003.ogg) | -0.50 | -11.62 | 0.0 | 0.0 | 0 | 0.2649 | 1.7 |
| [`kenney_interface-sounds/Audio/glitch_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glitch_004.ogg) | -0.93 | -16.16 | 0.0 | 0.0 | 0 | 0.0569 | 3.9 |
| [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) | -0.97 | -13.01 | 2.3 | 0.0 | 0 | 0.0071 | 22.3 |
| [`kenney_interface-sounds/Audio/maximize_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_002.ogg) | -0.92 | -10.29 | 1.9 | 0.0 | 0 | 0.0087 | 22.2 |
| [`kenney_interface-sounds/Audio/maximize_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_003.ogg) | -0.95 | -20.26 | 5.0 | 0.0 | 0 | 0.0010 | 18.2 |
| [`kenney_interface-sounds/Audio/maximize_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_004.ogg) | -0.94 | -15.15 | 16.6 | 0.4 | 0 | 0.0001 | 36.0 |
| [`kenney_interface-sounds/Audio/maximize_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_005.ogg) | -0.91 | -16.55 | 4.4 | 0.2 | 0 | 0.0001 | 45.3 |
| [`kenney_interface-sounds/Audio/maximize_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_006.ogg) | -0.93 | -14.20 | 0.0 | 9.0 | 0 | 0.0007 | 32.7 |
| [`kenney_interface-sounds/Audio/maximize_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_007.ogg) | -0.98 | -13.64 | 0.0 | 7.6 | 0 | 0.0045 | 16.0 |
| [`kenney_interface-sounds/Audio/maximize_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_008.ogg) | -0.91 | -12.71 | 0.2 | 16.1 | 0 | 0.0000 | 19.4 |
| [`kenney_interface-sounds/Audio/maximize_009.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_009.ogg) | -0.93 | -10.62 | 0.2 | 16.5 | 0 | 0.0000 | 19.4 |
| [`kenney_interface-sounds/Audio/minimize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_001.ogg) | -0.93 | -13.01 | 0.0 | 2.1 | 0 | 0.0065 | 22.3 |
| [`kenney_interface-sounds/Audio/minimize_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_002.ogg) | -0.90 | -10.30 | 0.0 | 1.9 | 0 | 0.0102 | 22.2 |
| [`kenney_interface-sounds/Audio/minimize_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_003.ogg) | -1.00 | -20.26 | 0.0 | 5.0 | 0 | 0.0026 | 18.2 |
| [`kenney_interface-sounds/Audio/minimize_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_004.ogg) | -0.94 | -15.15 | 0.4 | 16.6 | 0 | 0.0000 | 36.0 |
| [`kenney_interface-sounds/Audio/minimize_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_005.ogg) | -0.82 | -16.56 | 0.2 | 4.3 | 0 | 0.0000 | 45.3 |
| [`kenney_interface-sounds/Audio/minimize_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_006.ogg) | -0.96 | -14.18 | 8.3 | 0.0 | 0 | 0.0008 | 32.7 |
| [`kenney_interface-sounds/Audio/minimize_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_007.ogg) | -0.95 | -13.64 | 7.6 | 0.0 | 0 | 0.0064 | 16.0 |
| [`kenney_interface-sounds/Audio/minimize_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_008.ogg) | -0.94 | -12.73 | 16.2 | 0.2 | 0 | 0.0004 | 19.4 |
| [`kenney_interface-sounds/Audio/minimize_009.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_009.ogg) | -0.93 | -10.63 | 16.5 | 0.0 | 0 | 0.0020 | 19.4 |
| [`kenney_interface-sounds/Audio/open_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_001.ogg) | -0.86 | -14.78 | 1.7 | 0.0 | 0 | 0.0018 | 12.7 |
| [`kenney_interface-sounds/Audio/open_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_002.ogg) | -0.92 | -16.36 | 3.2 | 0.0 | 0 | 0.0004 | 27.0 |
| [`kenney_interface-sounds/Audio/open_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_003.ogg) | -0.98 | -15.16 | 1.5 | 0.0 | 0 | 0.0011 | 27.0 |
| [`kenney_interface-sounds/Audio/open_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_004.ogg) | -0.95 | -17.37 | 2.1 | 0.0 | 0 | 0.0028 | 27.8 |
| [`kenney_interface-sounds/Audio/pluck_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_001.ogg) | -0.00 | -20.57 | 0.0 | 0.1 | 2 | 0.6203 | 17.6 |
| [`kenney_interface-sounds/Audio/pluck_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_002.ogg) | -0.46 | -21.82 | 0.0 | 11.1 | 0 | 0.7445 | 28.3 |
| [`kenney_interface-sounds/Audio/question_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_001.ogg) | -0.90 | -11.77 | 0.4 | 3.3 | 0 | 0.0000 | 42.3 |
| [`kenney_interface-sounds/Audio/question_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_002.ogg) | -0.91 | -10.02 | 0.1 | 3.3 | 0 | 0.0000 | 28.6 |
| [`kenney_interface-sounds/Audio/question_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_003.ogg) | -0.94 | -10.05 | 0.1 | 2.6 | 0 | 0.0001 | 28.6 |
| [`kenney_interface-sounds/Audio/question_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/question_004.ogg) | -0.94 | -10.05 | 0.2 | 2.8 | 0 | 0.0000 | 28.6 |
| [`kenney_interface-sounds/Audio/scratch_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_001.ogg) | -0.92 | -12.29 | 0.0 | 16.5 | 0 | 0.0014 | 12.0 |
| [`kenney_interface-sounds/Audio/scratch_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_002.ogg) | -1.01 | -11.66 | 0.0 | 16.2 | 0 | 0.0031 | 12.0 |
| [`kenney_interface-sounds/Audio/scratch_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_003.ogg) | -0.99 | -18.10 | 0.0 | 0.0 | 0 | 0.0018 | 10.6 |
| [`kenney_interface-sounds/Audio/scratch_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_004.ogg) | -0.96 | -20.48 | 0.0 | 27.5 | 0 | 0.0011 | 28.0 |
| [`kenney_interface-sounds/Audio/scratch_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scratch_005.ogg) | -0.99 | -15.38 | 0.0 | 12.1 | 0 | 0.0029 | 28.0 |
| [`kenney_interface-sounds/Audio/scroll_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_001.ogg) | -0.86 | -21.23 | 0.0 | 0.0 | 0 | 0.0018 | 86.1 |
| [`kenney_interface-sounds/Audio/scroll_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_002.ogg) | -0.92 | -21.97 | 0.0 | 0.0 | 0 | 0.0035 | 86.1 |
| [`kenney_interface-sounds/Audio/scroll_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_003.ogg) | -0.93 | -21.89 | 0.0 | 0.0 | 0 | 0.0055 | 86.1 |
| [`kenney_interface-sounds/Audio/scroll_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_004.ogg) | -0.90 | -20.11 | 0.0 | 37.5 | 0 | 0.0031 | 86.1 |
| [`kenney_interface-sounds/Audio/scroll_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/scroll_005.ogg) | -0.92 | -18.03 | 0.0 | 40.0 | 0 | 0.0044 | 86.1 |
| [`kenney_interface-sounds/Audio/select_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_001.ogg) | -1.13 | -17.63 | 0.0 | 0.1 | 0 | 0.0099 | 7.5 |
| [`kenney_interface-sounds/Audio/select_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_002.ogg) | -0.97 | -16.18 | 0.0 | 0.0 | 0 | 0.0061 | 7.5 |
| [`kenney_interface-sounds/Audio/select_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_003.ogg) | -0.82 | -18.80 | 11.9 | 148.7 | 0 | 0.0003 | 33.0 |
| [`kenney_interface-sounds/Audio/select_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_004.ogg) | -0.73 | -19.23 | 12.0 | 148.8 | 0 | 0.0002 | 33.0 |
| [`kenney_interface-sounds/Audio/select_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_005.ogg) | -1.32 | -18.72 | 12.3 | 145.9 | 0 | 0.0004 | 33.0 |
| [`kenney_interface-sounds/Audio/select_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_006.ogg) | -0.77 | -22.48 | 30.9 | 1691.0 | 0 | 0.0001 | 167.4 |
| [`kenney_interface-sounds/Audio/select_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_007.ogg) | -0.96 | -13.63 | 0.0 | 0.0 | 0 | 0.9007 | 4.0 |
| [`kenney_interface-sounds/Audio/select_008.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_008.ogg) | -1.00 | -19.14 | 0.0 | 0.6 | 0 | 0.6809 | 4.0 |
| [`kenney_interface-sounds/Audio/switch_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_001.ogg) | -0.95 | -27.35 | 0.0 | 479.3 | 0 | 0.0023 | 106.4 |
| [`kenney_interface-sounds/Audio/switch_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_002.ogg) | -0.95 | -25.06 | 0.1 | 467.8 | 0 | 0.0002 | 105.2 |
| [`kenney_interface-sounds/Audio/switch_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_003.ogg) | -0.91 | -15.30 | 0.0 | 0.0 | 0 | 0.0008 | 86.1 |
| [`kenney_interface-sounds/Audio/switch_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_004.ogg) | -0.96 | -23.88 | 0.0 | 343.7 | 0 | 0.0030 | 86.1 |
| [`kenney_interface-sounds/Audio/switch_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_005.ogg) | -0.91 | -22.63 | 0.0 | 456.8 | 0 | 0.0002 | 105.4 |
| [`kenney_interface-sounds/Audio/switch_006.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_006.ogg) | -0.92 | -24.60 | 0.0 | 468.4 | 0 | 0.0031 | 105.2 |
| [`kenney_interface-sounds/Audio/switch_007.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/switch_007.ogg) | -0.95 | -25.02 | 0.0 | 468.6 | 0 | 0.0023 | 105.8 |
| [`kenney_interface-sounds/Audio/tick_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/tick_001.ogg) | -0.81 | -18.61 | 0.0 | 9.5 | 0 | 0.8143 | 2.0 |
| [`kenney_interface-sounds/Audio/tick_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/tick_002.ogg) | -1.21 | -17.22 | 0.0 | 0.0 | 0 | 0.8589 | 2.0 |
| [`kenney_interface-sounds/Audio/tick_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/tick_004.ogg) | -0.86 | -15.85 | 0.0 | 0.0 | 0 | 0.0165 | 4.7 |
| [`kenney_interface-sounds/Audio/toggle_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_001.ogg) | -0.91 | -14.11 | 0.0 | 36.8 | 0 | 0.0009 | 12.0 |
| [`kenney_interface-sounds/Audio/toggle_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_002.ogg) | -0.95 | -12.18 | 0.0 | 37.5 | 0 | 0.0044 | 12.0 |
| [`kenney_interface-sounds/Audio/toggle_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_003.ogg) | -0.94 | -14.33 | 0.0 | 36.6 | 0 | 0.0013 | 12.0 |
| [`kenney_interface-sounds/Audio/toggle_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/toggle_004.ogg) | -0.94 | -16.46 | 0.0 | 6.2 | 0 | 0.0015 | 5.7 |
| [`kenney_rpg-audio/Audio/beltHandle1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/beltHandle1.ogg) | -3.29 | -28.23 | 0.6 | 18.0 | 0 | 0.0001 | 51.9 |
| [`kenney_rpg-audio/Audio/beltHandle2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/beltHandle2.ogg) | -4.17 | -31.97 | 5.3 | 50.4 | 0 | 0.0002 | 89.4 |
| [`kenney_rpg-audio/Audio/bookClose.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookClose.ogg) | 0.00 | -18.76 | 0.0 | 0.0 | 28 | 0.0013 | 43.2 |
| [`kenney_rpg-audio/Audio/bookFlip1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookFlip1.ogg) | 0.00 | -29.82 | 48.0 | 27.1 | 9 | 0.0000 | 144.1 |
| [`kenney_rpg-audio/Audio/bookFlip2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookFlip2.ogg) | -8.27 | -37.01 | 0.0 | 30.4 | 0 | 0.0061 | 80.7 |
| [`kenney_rpg-audio/Audio/bookFlip3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookFlip3.ogg) | -4.56 | -32.13 | 0.0 | 0.2 | 0 | 0.0028 | 43.2 |
| [`kenney_rpg-audio/Audio/bookOpen.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookOpen.ogg) | -11.54 | -33.65 | 0.0 | 1.3 | 0 | 0.0014 | 28.8 |
| [`kenney_rpg-audio/Audio/bookPlace1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookPlace1.ogg) | 0.00 | -18.01 | 0.0 | 2.2 | 25 | 0.0036 | 51.9 |
| [`kenney_rpg-audio/Audio/bookPlace2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookPlace2.ogg) | 0.00 | -18.25 | 0.0 | 61.0 | 76 | 0.0074 | 66.3 |
| [`kenney_rpg-audio/Audio/bookPlace3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/bookPlace3.ogg) | 0.00 | -17.46 | 0.0 | 0.2 | 62 | 0.0018 | 49.0 |
| [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg) | 0.00 | -18.47 | 0.0 | 0.0 | 16 | 0.0044 | 45.0 |
| [`kenney_rpg-audio/Audio/cloth1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth1.ogg) | -3.49 | -24.56 | 4.3 | 27.6 | 0 | 0.0007 | 123.9 |
| [`kenney_rpg-audio/Audio/cloth2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth2.ogg) | -7.94 | -28.72 | 0.2 | 20.4 | 0 | 0.0015 | 77.8 |
| [`kenney_rpg-audio/Audio/cloth3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth3.ogg) | -10.09 | -29.28 | 0.0 | 0.0 | 0 | 0.0011 | 89.4 |
| [`kenney_rpg-audio/Audio/cloth4.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/cloth4.ogg) | -14.64 | -34.61 | 0.0 | 0.4 | 0 | 0.0047 | 72.1 |
| [`kenney_rpg-audio/Audio/clothBelt.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/clothBelt.ogg) | -4.31 | -31.09 | 0.0 | 0.5 | 0 | 0.0022 | 135.5 |
| [`kenney_rpg-audio/Audio/clothBelt2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/clothBelt2.ogg) | -6.69 | -31.18 | 0.0 | 105.9 | 0 | 0.0010 | 141.2 |
| [`kenney_rpg-audio/Audio/creak1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/creak1.ogg) | -6.70 | -25.76 | 30.7 | 7.6 | 0 | 0.0002 | 123.9 |
| [`kenney_rpg-audio/Audio/creak2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/creak2.ogg) | -7.04 | -25.98 | 0.2 | 45.1 | 0 | 0.0004 | 155.7 |
| [`kenney_rpg-audio/Audio/creak3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/creak3.ogg) | -7.41 | -21.69 | 0.2 | 5.7 | 0 | 0.0003 | 63.4 |
| [`kenney_rpg-audio/Audio/doorClose_1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_1.ogg) | 0.00 | -17.29 | 2.0 | 2.6 | 77 | 0.0004 | 126.6 |
| [`kenney_rpg-audio/Audio/doorClose_2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_2.ogg) | -2.41 | -18.84 | 2.3 | 0.6 | 0 | 0.0003 | 114.8 |
| [`kenney_rpg-audio/Audio/doorClose_3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_3.ogg) | 0.00 | -13.10 | 0.0 | 0.0 | 249 | 0.0027 | 133.9 |
| [`kenney_rpg-audio/Audio/doorClose_4.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorClose_4.ogg) | 0.00 | -18.16 | 4.2 | 0.0 | 19 | 0.0054 | 113.1 |
| [`kenney_rpg-audio/Audio/doorOpen_1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorOpen_1.ogg) | 0.00 | -20.21 | 0.0 | 0.0 | 15 | 0.0025 | 172.3 |
| [`kenney_rpg-audio/Audio/doorOpen_2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/doorOpen_2.ogg) | 0.00 | -22.65 | 0.0 | 0.0 | 16 | 0.0027 | 265.0 |
| [`kenney_rpg-audio/Audio/drawKnife1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/drawKnife1.ogg) | -24.28 | -44.69 | 56.2 | 85.3 | 0 | 0.0002 | 74.9 |
| [`kenney_rpg-audio/Audio/drawKnife2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/drawKnife2.ogg) | -22.95 | -41.35 | 0.5 | 95.7 | 0 | 0.0003 | 83.6 |
| [`kenney_rpg-audio/Audio/drawKnife3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/drawKnife3.ogg) | -11.37 | -38.61 | 33.8 | 144.0 | 0 | 0.0003 | 89.4 |
| [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) | -0.12 | -18.72 | 0.0 | 40.1 | 0 | 0.0019 | 77.8 |
| [`kenney_rpg-audio/Audio/footstep00.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep00.ogg) | -0.00 | -19.73 | 0.0 | 0.2 | 5 | 0.0012 | 46.7 |
| [`kenney_rpg-audio/Audio/footstep01.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep01.ogg) | 0.00 | -18.30 | 0.0 | 4.2 | 14 | 0.0002 | 53.0 |
| [`kenney_rpg-audio/Audio/footstep02.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep02.ogg) | -1.18 | -22.13 | 0.0 | 4.0 | 0 | 0.0050 | 45.4 |
| [`kenney_rpg-audio/Audio/footstep03.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep03.ogg) | 0.00 | -20.05 | 1.8 | 0.0 | 9 | 0.0016 | 50.4 |
| [`kenney_rpg-audio/Audio/footstep04.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep04.ogg) | -3.52 | -25.14 | 1.5 | 17.8 | 0 | 0.0009 | 60.5 |
| [`kenney_rpg-audio/Audio/footstep05.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep05.ogg) | -2.99 | -26.22 | 0.0 | 0.0 | 0 | 0.0024 | 51.7 |
| [`kenney_rpg-audio/Audio/footstep06.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep06.ogg) | -0.26 | -21.90 | 0.0 | 0.0 | 0 | 0.0099 | 45.4 |
| [`kenney_rpg-audio/Audio/footstep07.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep07.ogg) | 0.00 | -21.63 | 0.0 | 0.4 | 3 | 0.0016 | 47.9 |
| [`kenney_rpg-audio/Audio/footstep08.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep08.ogg) | 0.00 | -17.03 | 0.0 | 0.0 | 77 | 0.0052 | 47.9 |
| [`kenney_rpg-audio/Audio/footstep09.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/footstep09.ogg) | -6.09 | -23.47 | 0.0 | 0.0 | 0 | 0.0085 | 40.4 |
| [`kenney_rpg-audio/Audio/handleCoins.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleCoins.ogg) | 0.00 | -28.84 | 31.5 | 80.8 | 2 | 0.0001 | 158.5 |
| [`kenney_rpg-audio/Audio/handleCoins2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleCoins2.ogg) | -10.70 | -34.02 | 2.8 | 17.4 | 0 | 0.0005 | 63.4 |
| [`kenney_rpg-audio/Audio/handleSmallLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleSmallLeather.ogg) | -11.62 | -35.73 | 15.8 | 108.9 | 0 | 0.0001 | 63.4 |
| [`kenney_rpg-audio/Audio/handleSmallLeather2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/handleSmallLeather2.ogg) | -16.47 | -39.93 | 20.5 | 48.5 | 0 | 0.0003 | 57.7 |
| [`kenney_rpg-audio/Audio/knifeSlice.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice.ogg) | -1.32 | -21.51 | 49.5 | 127.5 | 0 | 0.0001 | 112.4 |
| [`kenney_rpg-audio/Audio/knifeSlice2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice2.ogg) | -0.00 | -19.07 | 3.9 | 64.0 | 5 | 0.0005 | 106.6 |
| [`kenney_rpg-audio/Audio/metalClick.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalClick.ogg) | -6.93 | -34.33 | 0.0 | 63.0 | 0 | 0.0159 | 83.6 |
| [`kenney_rpg-audio/Audio/metalLatch.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalLatch.ogg) | 0.00 | -20.49 | 0.1 | 13.7 | 14 | 0.0003 | 49.0 |
| [`kenney_rpg-audio/Audio/metalPot1.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalPot1.ogg) | 0.00 | -15.88 | 49.3 | 0.0 | 141 | 0.0013 | 273.8 |
| [`kenney_rpg-audio/Audio/metalPot2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalPot2.ogg) | 0.00 | -13.85 | 64.3 | 0.0 | 536 | 0.0040 | 175.8 |
| [`kenney_rpg-audio/Audio/metalPot3.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalPot3.ogg) | 0.00 | -11.70 | 0.0 | 48.0 | 948 | 0.0025 | 170.1 |
| [`kenney_rpg-audio/Preview.ogg`](asset_sources/audio/kenney_rpg-audio/Preview.ogg) | 0.00 | -19.44 | 0.0 | 27.4 | 2717 | 0.0065 | 4898.9 |

### Ý nghĩa flag và kết quả đáng chú ý

| Flag | Số file | Đánh giá |
| --- | --- | --- |
| PCM_FULL_SCALE_REVIEW | 26 | Có sample đạt full scale trên PCM16; nghe kiểm tra headroom, không tự sửa nguồn. |
| LEAD_GT_30MS | 10 | Đầu file yên hơn−60dBFS quá30ms, có thể làm SFX phản hồi chậm. |
| TAIL_GT_100MS | 16 | Đuôi yên quá100ms; chưa tự trim, không tính thời lượng này như năng lượng âm. |
| LOW_RMS_REVIEW | 2 | RMS toàn file dưới−40dBFS; có thể nhỏ hoặc nhiều khoảng lặng, chưa phải LUFS. |
| DC_OFFSET_REVIEW | 4 | Mean PCM có /DC/>0,01; cần nghe/đo sâu trước quyết định chỉnh derivative. |
| ENDPOINT_JUMP_REVIEW | 12 | Endpoint end→0 lệch>0,1; không nên tự loop SFX đó. |
| MUSIC_NOT_SOUND_SIZE | 5 | PCM>1MiB: dùng streaming Music; đây là quyết định routing, không phải lỗi file. |

- `drawKnife1/2` RMS−44,69/−41,35dBFS; đối chiếu UI confirmation khoảng−11dBFS cho thấy không thể cùng gain1 mà kỳ vọng cùng độ rõ. Khoảng34,67dB của RMS toàn tập SFX chưa phải chênh lệch loudness nghe được.
- `select_006.ogg` có khoảng1,69s đuôi yên; các `switch_*.ogg` có đuôi khoảng0,3–0,48s. Không chọn variant dài chỉ vì cùng prefix; nghe lại trước khi chọn.
- `knifeSlice.ogg` có lead~50ms; các `metalPot` có full-scale samples và một số lead~49–64ms. `knifeSlice2/chop` cũng cần nghe peak. Không coi các flag này là file hỏng.
- `battle_music_01-loop.ogg` có28 FS samples, DC mean~0,01178 và tác giả chỉ định lặp từ~7,5s. `end_dungeon.mp3` có tổng lead/tail khoảng40ms ở ngưỡng đã chọn; endpoint0 không chứng minh loop không gap.
- Mono77 file, stereo79 file. 48kHz stereo52 file thuộc RPG (bao gồm Preview), còn104 file44,1kHz. Không có lý do tự downmix/resample trong audit.

## Sáu file không phải audio

| Đường dẫn | Bytes | Loại |
| --- | --- | --- |
| kenney_interface-sounds/Kenney.url | 45 | Windows Internet Shortcut; không phát âm thanh |
| kenney_interface-sounds/License.txt | 574 | License |
| kenney_interface-sounds/Patreon.url | 55 | Windows Internet Shortcut; không phát âm thanh |
| kenney_rpg-audio/License.txt | 478 | License |
| kenney_rpg-audio/Visit Kenney.url | 45 | Windows Internet Shortcut; không phát âm thanh |
| kenney_rpg-audio/Visit Patreon.url | 55 | Windows Internet Shortcut; không phát âm thanh |

Dữ liệu đầy đủ: [CSV](core/build/reports/task05_1a/audio-audit.csv), [JSON](core/build/reports/task05_1a/audio-audit.json). Tái hiện: `python tools/audit_audio_assets.py`. Không cần Android Studio Run cho audit.
