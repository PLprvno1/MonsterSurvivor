# TASK 05.1A — Audio Asset Audit & Sound Selection Plan

Ngày khảo sát: **14/11/2026**, project `D:\Android\MonsterSurvivor`. Phạm vi: audit, lựa chọn sơ bộ và kế hoạch; **chưa tích hợp audio**.

## 1. Kết quả bàn giao

Đã quét toàn bộ `asset_sources/audio`, đọc metadata, kiểm tra container Ogg và giải mã hết audio bằng thư viện local đã có. Đã đối chiếu source gameplay/UI hiện tại, lập **53 event**, đề xuất candidate từ file thật và đánh dấu khoảng trống. Không giả vờ đã nghe hoặc duyệt chất âm từ metadata.

| Tài liệu / công cụ | Nội dung |
|---|---|
| [AUDIO_ASSET_INVENTORY.md](AUDIO_ASSET_INVENTORY.md) | Toàn bộ156 audio,6 file phụ; metadata, duration/codec/bitrate, PCM measurements, loop, license |
| [AUDIO_EVENT_MATRIX.md](AUDIO_EVENT_MATRIX.md) | 53 event, tối đa3 candidate/event, duration/priority/confidence, source anchor, trạng thái thật/tương lai |
| [AUDIO_SELECTION_CHECKLIST.md](AUDIO_SELECTION_CHECKLIST.md) | 49 mục thuộc6 nhóm để chủ dự án chọn A/B, Đồng ý/Thay/Chưa chọn |
| [AUDIO_MISSING_ASSETS.md](AUDIO_MISSING_ASSETS.md) | REQUIRED/RECOMMENDED/OPTIONAL; khoảng trống có thể chia sẻ asset |
| [AUDIO_AUDITION.html](AUDIO_AUDITION.html) | Danh sách nghe offline theo nhóm, một player, không autoplay/preload hàng loạt, xuất lựa chọn JSON |
| `tools/audit_audio_assets.py`, `tools/audio/AudioProbe.java` | Audit read-only bằng Python stdlib/JDK và decoder đã cache |
| `tools/create_audio_audit_docs.py` | Dữ liệu đề xuất và sinh bốn tài liệu/checklist HTML từ metadata đã đo |

Dữ liệu máy đọc/CSV, tags, SHA-256, provenance, raw PCM stats và log trong `core/build/reports/task05_1a/`. Tất cả đường dẫn audio trong bốn tài liệu là đường dẫn tương đối chính xác từ **`asset_sources/audio/`**, không phải tên runtime giả định.

## 2. Số lượng và kỹ thuật

| Nhóm thực tế | File | Nén bytes | PCM16 tương đương | Duration |
|---|---:|---:|---:|---:|
| BGM ở thư mục gốc | 4 | 10.926.479 (~10,420MiB) | ~83,337MiB | 495,382s |
| Kenney Interface Sounds/Audio | 100 | Bao gồm trong SFX dưới | — | — |
| Kenney RPG Audio/Audio | 51 | Bao gồm trong SFX dưới | — | — |
| Tổng SFX | 151 | 1.688.118 (~1,610MiB) | ~7,679MiB | 55,344s |
| RPG `Preview.ogg` | 1 | 321.120 | ~4,784MiB | 26,127s |
| License/URL, không audio | 6 | Không tính vào audio | — | — |

**162 file tổng;156 audio**, nén12.935.717bytes (~12,336MiB), duration576,853s. **155 Ogg Vorbis +1 MP3**, không có extension kép bất ngờ, không có file audio trùng SHA-256. Tên folder thực tế `kenney_rpg-audio`, khác dấu gạch với ví dụ `kenney_rpg_audio` trong yêu cầu. Website RPG ghi50 sound nhưng local có51 SFX +Preview; không tự xóa hay đổi tên file chênh lệch.

Sample rates/channels:77 mono44,1kHz;27 stereo44,1kHz;52 stereo48kHz. MP3 là stereo44,1kHz, bitrate frame128kbit/s, không thấy đổi bitrate. Vorbis có nominal bitrate khác nhau; bảng inventory ghi **file-average bitrate**, phân biệt với nominal/tag. Mọi file decode thành công; không có Ogg CRC/sequence/EOS error. Không có file hiện tại bị đánh dấu unsupported.

Không có ffprobe/ffmpeg trong PATH. Đã dùng Java17 + libGDX1.14.2 OggInputStream/JOrbis0.0.17 + JLayer1.0.1-gdx và LWJGL BufferUtils **có sẵn trong Gradle cache**, không cài gói nặng, không mở audio device. PCM được phân tích trong bộ nhớ rồi bỏ, không ghi WAV/MP3 mới. Ogg duration theo granule/sample-rate; vài decoder trả thêm padding dưới23ms, phép đo được giới hạn đúng sample nguồn. MP3 duration là decoded frame count, chưa loại encoder delay/padding.

**Giới hạn:** chưa nghe trực tiếp, chưa đo LUFS/true peak hoặc phân tích phổ/hòa âm/musical seam. Decode desktop không chứng minh Android SoundPool/MediaPlayer đã được kiểm thử. Không chạy Android Studio Run vì chưa sửa runtime.

## 3. Chất lượng cần nghe lại

- **26 file** có sample PCM16 đạt full scale. Đây là flag headroom; nén lossy/decoder có thể gây overshoot/clamp, chưa đủ kết luận26 file hỏng hoặc clipping nghe được. Ví dụ `chop`, `knifeSlice2`, một số metal/door/footstep; Preview có nhiều FS samples nhưng bị loại khỏi runtime plan.
- **10 file** có lead>30ms; **16 file** có tail>100ms ở ngưỡng khoảng−60dBFS. `knifeSlice.ogg` lead~50ms có thể tạo cảm giác trễ; `select_006.ogg` tail~1,69s và một số switch tail~0,3–0,48s. Không tự trim.
- **2 SFX** RMS dưới−40dBFS: `drawKnife1/2` (~−44,69/−41,35). RMS toàn tập SFX nằm khoảng−44,69 đến−10,02dBFS; không coi đây là mức loudness perceptual hoặc lý do normalize tất cả.
- `battle_music_01-loop.ogg`:28 full-scale samples, mean DC~0,01178. Cần nghe/đo sâu trước quyết định gain/derivative; không chỉnh file nguồn.
- **12 file** có endpoint jump>0,1 ở nối end→0. Không nên tự loop những SFX đó. Một endpoint bằng0 vẫn không chứng minh nhạc loop liền, nhất là MP3 hoặc intro/loop khác điểm bắt đầu.
- Tất cả SFX riêng lẻ dưới1MiB PCM tương đương; lớn nhất `metalPot1.ogg`280.408bytes. BGM/Preview vượt ngưỡng này và không nên nạp như Sound.

Định nghĩa ngưỡng và từng measurement có trong inventory/JSON. Không normalize, transcode, resample, downmix hoặc sửa source trong task này.

## 4. License từng nguồn

Hai Kenney có License.txt local ghi đúng tên pack và CC0, khớp trang chính thức [Interface Sounds](https://kenney.nl/assets/interface-sounds) và [RPG Audio](https://kenney.nl/assets/rpg-audio). Không có README không cản audit.

Bốn bản nhạc được nhận diện từ **ReferrerUrl/HostUrl trong NTFS Zone.Identifier của chính file**; Heavy/Hope còn có TITLE/ARTIST/BPM tags tương ứng. Đã đối chiếu riêng:

| File local | Tác giả / license ghi trên trang nguồn | Bằng chứng |
|---|---|---|
| `battle_music_01-loop.ogg` | pmiller / CC0 | [Chiptune Battle Music](https://opengameart.org/content/chiptune-battle-music) |
| `end_dungeon.mp3` | HydroGene / CC0 | [JRPG — End Dungeon](https://opengameart.org/content/jrpg-end-dungeon), HostUrl có hậu tố `_0` |
| `heavy_boss_battle_1_bpm200.ogg` | MintoDog / CC0 | [Heavy Boss Battle 1](https://opengameart.org/content/heavy-boss-battle-1), HostUrl hậu tố `_0`, tags khớp |
| `hope_orchestral_battle_music_bpm165.ogg` | MintoDog / CC0 | [Hope](https://opengameart.org/content/hopeorchestral-battle-music), HostUrl hậu tố `_0`, tags khớp |

Một số lần mở trực tiếp gặp403/robots, nhưng nội dung trang chính thức có trong kết quả tìm kiếm của đúng domain. `source-verification.json` lưu URL/basis/trạng thái. Không tải lại nhạc hay so byte với remote. **Không suy license từ việc cùng nằm trên OpenGameArt.** Không còn LICENSE_UNVERIFIED trong tập hiện tại; file bổ sung thiếu provenance phải dùng trạng thái đó. Credit tác giả/URL tuy không bắt buộc theo các nguồn này vẫn nên giữ cùng manifest chọn sau này.

## 5. Bốn BGM: lựa chọn sơ bộ và loop

Mood/độ động dưới đây suy từ **mô tả tác giả/tags**, không phải nghe. BPM chỉ là metadata, không là tiêu chí chốt.

| File | Duration / format | Mood/độ động dự kiến | Vai trò sơ bộ | Loop / cần duyệt |
|---|---|---|---|---|
| `end_dungeon.mp3` | 165,564s / MP3 | Dungeon/fantasy, độ động chưa xác nhận; ID3 BPM116 | Menu gain thấp và Night dùng chung | Tác giả nói loopable; MP3 lead/tail tổng~40ms và backend có thể gap. Không coi đây là result jingle vì chữ “end”. |
| `hope_orchestral_battle_music_bpm165.ogg` | 69,818s / Vorbis | Bright orchestral battle; năng lượng dự kiến vừa/cao | Day, Victory; fallback Mini | Tác giả ghi loopable, chưa nghe seam/backend. Victory có thể loop off, đây vẫn là BGM dài. |
| `battle_music_01-loop.ogg` | 152,000s / Vorbis | RPG chiptune battle, độ căng cần nghe | Mini; phương án Day nếu chọn retro palette | Tác giả yêu cầu lần đầu từ0, repeat về **khoảng7,5s**. Không loop mù end→0 hoặc coi7,5s là exact sample point. |
| `heavy_boss_battle_1_bpm200.ogg` | 108,000s / Vorbis | Heavy/metal/dark boss, dự kiến mạnh | Final Lich, giữ qua phase2 | Tác giả ghi loopable; nghe mức ăn nhập với orchestral/chiptune và kiểm seam. |

Như vậy bốn bài đủ kế hoạch tái dùng cho sáu trạng thái có nhạc; Game Over có thể **im lặng + cue UI**. Không bắt buộc tìm bảy BGM. Nhạc/asset nào chưa hợp sau khi nghe thì giữ Chưa chọn/Thay. Chiptune có intro-loop riêng: task sau cần xác định seam chính xác, có thể xin duyệt derivative intro/loop từ nguồn giữ nguyên hoặc chọn Hope làm fallback; callback seek về7,5s trên backend mặc định không tự bảo đảm seamless. libGDX phân biệt Music streaming và Sound, đồng thời ghi nhận giới hạn loop gap theo backend. [Music API và looping](https://libgdx.com/wiki/audio/streaming-music).

## 6. Gameplay thực tế và candidate ưu tiên

Đã đọc các báo cáo Task05,04A.1,UI02,Integration Notes và source GameSession/Screen, EnemySystem/Enemy/Player, SwordCombat/WeaponSystem/ProjectileSystem, BossSystem/BossProjectiles, PickupSystem/WeaponOffer, GameInput/WeaponHud và native Settings/Preferences/Launcher. `rg` không thấy audio manager, Gdx.audio.newSound/newMusic hoặc bridge audio preference trong runtime hiện tại.

53 event chia7 BGM,8 Weapons,7 Pickups,10 Boss,8 UI,9 Enemy/player/legacy charge và4 Future. **35 event có candidate,18 MISSING**. Mỗi event có file/duration/lý do/source anchor trong matrix; candidate đầu là gợi ý ưu tiên để nghe, chưa là quyết định tích hợp.

| Nhóm | File ưu tiên để nghe trước | Điều cần lưu ý |
|---|---|---|
| Kiếm | `kenney_rpg-audio/Audio/knifeSlice.ogg`, `chop.ogg` | Swing/hit vật lý; lead/peak cần nghe |
| Cung | `kenney_interface-sounds/Audio/pluck_002.ogg` | Chỉ proxy dây bật, confidence LOW, được từ chối |
| Pickup potion | `glass_002/003/005.ogg` trong Interface/Audio | Dùng chung palette, có thể cùng một file cho Damage/Health/Speed |
| Weapon Box | `open_001.ogg`; chọn `confirmation_001.ogg`; nâng cấp `confirmation_003.ogg` | Open theo modal token, commit một cue; không phát khi lựa chọn không hợp lệ |
| Boss warning/phase | `bong_001.ogg`, `maximize_001.ogg` | Proxy UI tạm, không giả là roar/magic; không phát mỗi frame telegraph |
| UI | `click_001`, `select_001`, `back_001`, `confirmation_001` | Chọn một cue/action; không phát theo UI recreate/loadValues |
| Enemy | `dropLeather.ogg`, `chop.ogg`; armor `metalClick.ogg` | LOW, không khẳng định là tiếng sinh vật; chung impact tránh trùng weapon hit |
| Fire/electric/heavy blast | **MISSING** | Chưa có file đủ cơ sở, không lấy cửa/pot/glitch chỉ để lấp tên |

Tên rút gọn trong bảng này được mở rộng thành path chính xác, kiểm tồn tại, trong matrix/checklist. Confidence MEDIUM/LOW phản ánh tên/metadata/mô tả nguồn; không có sound-fit HIGH khi chưa nghe.

Gameplay có thật: start swing/hit, release/cast/explosion/chain, damage/death, pickup apply/commit, Boss spawn/slam/bolt/AoE/phase, terminal state và các thao tác UI. Legacy charge là speed burst thật trong EnemySystem cho definition.charge. Dash/Ultimate/Evolution chưa có; nhiều SPECIAL chỉ là catalog/clip, không phải event kỹ năng. Không đặt hook audio vào chúng ở task tích hợp trước khi có logic tương ứng.

## 7. Thiếu tài nguyên và quyết định cần duyệt

**7 event REQUIRED chưa có candidate** có thể phủ bằng **ba nhóm asset nhỏ**: (1) fire/magic cast whoosh, (2) compact explosion/heavy impact dùng chung Fireball/Mini slam/Lich AoE, (3) electric/magic zap dùng chung Lightning/Lich projectile. Không cần một pack lớn hoặc sound riêng cho từng lane/nạn nhân.

Player hurt là RECOMMENDED. Bow twang và generic creature death chỉ nên bổ sung nếu chủ dự án từ chối proxy hiện có. Mười event MISSING còn lại là OPTIONAL/future, gồm reaction group, player death riêng, legacy charge, GameOver BGM và4 tính năng chưa triển khai. Không có yêu cầu82 voice riêng. Các lựa chọn UI/pickup/kiếm/BGM đã có file thật để duyệt trước khi tìm thêm.

Chủ dự án cần nghe/chọn:

1. Palette BGM và việc phối orchestral/chiptune/metal; Day Hope hay Battle, Mini Battle hay Hope; Menu/Night dùng chung Dungeon; Game Over im lặng hay nhạc riêng.
2. Loop point chiptune, mức chấp nhận MP3 gap và gain giữa bốn bài. Đừng chỉ nghe intro hoặc dựa BPM.
3. Sword A/B, Bow proxy, glass/UI cues, warning Boss và các biến thể đủ khác nhau để random.
4. Có cần player hurt/death/reaction riêng trong demo; ba khoảng trống combat nào cần bổ sung.
5. Có duyệt chỉnh **derivative runtime** nếu sau khi nghe thật sự cần trim/gain/loop preparation. Audit hiện tại không chỉnh hoặc tự cấp phép triển khai các chỉnh sửa đó.

## 8. Kế hoạch Audio System sau duyệt — chưa triển khai

### Ownership, API và tải asset

Đề xuất một AudioManager core với bus Music, Gameplay SFX, Critical SFX và UI; emit từ nơi gameplay chấp nhận event, không polling sprite/render. `Sound` cho short SFX preload subset đã duyệt, `Music` cho BGM streaming. BGM không decode toàn bộ vào RAM. Android Sound backend có ngưỡng sample khoảng1MiB PCM và phát dồn nhiều sound có thể gây stall; vì vậy preload/cooldown/coalescing trước khi cân nhắc đổi backend. [Sound và giới hạn Android](https://libgdx.com/wiki/audio/sound-effects), [backend hiện có](https://libgdx.com/wiki/audio/audio).

Không load mới ở mỗi hit/spawn, không rebuild asset ở Retry, dispose theo owner. Ưu tiên `AssetManager`/tài nguyên đã load, tải xong trước gameplay rồi mới bật cue. Handle `play()` thất bại và không giữ ID của asset đã dispose. Giữ source tại asset_sources; task sau chỉ copy **subset đã duyệt** vào runtime kèm manifest license/gain/event, không đóng gói cả162 file hay Preview.

Native Home/GameMode/Settings không có Gdx context. Cần Android audio coordinator riêng cho quyền sở hữu music/focus/settings và menu playback; core giữ Gdx Music khi gameplay hoạt động. Native→core dừng/fade menu trước khi cấp owner mới; core→Home dừng core trước khi menu bắt đầu. Không tạo hai player menu mỗi lần Settings.recreate(), không giữ static reference Activity. Đây là contract thiết kế, chưa tạo code/bridge.

### Music state priority

Policy sơ bộ: `VICTORY/GAME_OVER (100) > FINAL_ACTIVE (80) > MINI_ACTIVE (70) > NIGHT (40) > DAY (30) > MENU (10)`. WINNING fade combat music và chờ Death; chỉ Victory phase thật mới dùng nhạc/cue thắng. DYING có player death cue nếu được chọn; GameOver cue tại GAME_OVER. Tính một desired state cuối tick; so với state hiện tại trước khi đổi.

SPAWNING/retry không restart nhạc; Mini chết khi Final sống vẫn giữ Final. Phase2 đổi action pattern nhưng không phát lại intro cùng BGM. Night300 cùng Mini chỉ chọn Boss music ưu tiên, không chồng hai BGM. Không dùng ResultPreviewActivity hoặc bảng thống kê giả để kích hoạt âm thắng/thua gameplay.

Một Music bình thường; tối đa hai khi crossfade khoảng0,4–0,8s đã duyệt. Giữ cache nhỏ, không tạo bảy Music instance chỉ vì có bảy trạng thái. Với author-loop bình thường vẫn phải nghe vòng nối trên Android; với chiptune cần xử lý intro-loop riêng trước khi chốt.

### SFX cooldown, coalescing và voices

Các số dưới đây là budget khởi điểm **đề xuất**, chưa đo/áp dụng:

| Bus/nhóm | Cooldown thực | Voice đề xuất | Quy tắc |
|---|---:|---:|---|
| UI | 60–80ms/action | 2 | Chỉ action được chấp nhận, không touch/render/lifecycle spam |
| Player hurt | Theo damage thật và immunity0,7s | 2 | HP phải giảm; hit bị immunity/burst chặn không thành hurt cue |
| Cast/release | 80–100ms/group + activation ID | 4 | Một fan/chain một cue, không mỗi đạn |
| Common hit/death | Hit80ms, death150ms/global + actor dedup | 6 | Culling/range, bỏ bớt low priority khi đông |
| Boss warning/result | 200–300ms duplicate suppression + event ID | 2 reserved | Ưu tiên hơn common impact; không drop cảnh báo vì60 quái |

Tổng mục tiêu **16 SFX voices**, riêng BGM1/tối đa2 khi crossfade. Software routing ưu tiên request, stop/steal low priority bằng sound handle nếu backend hỗ trợ; cần account duration/TTL vì Sound không có completion callback như Music. Chỉ random **variant đã nghe và duyệt**, tối đa2–3/group; không random mọi file cùng prefix. Pitch variation nhỏ chỉ sau duyệt và tránh cue cảnh báo có nghĩa. Nếu backend asynchronous không cung cấp đủ handle, phải thiết kế lại voice accounting, không thay backend mù để tối ưu.

Enemy hit/death chỉ một owner phát; Boss death thay generic death. Không đăng ký listener đè GameSession kill/drop. `Enemy.damage` boolean là death, không phải hit acceptance; cần HP delta sau burst guard. Weapon physical impact có thể phát khi chạm, nhưng không nhân thêm hurt giả. Fire AoE/Lightning coalesce theo cluster/activation; không phát60 âm khi60 target nhận damage. Có thể duck BGM~6dB tạm cho warning/result quan trọng, không duck liên tục vì mọi hit.

### Settings bridge giữ hợp đồng hiện tại

Source hiện có `GamePreferences` lưu `monster_survivor_prefs`:

| Key đang có | Kiểu / mặc định | Bridge dự kiến |
|---|---|---|
| `pref_music_enabled` | boolean / true | Mute/stop Music nhưng giữ desired state nếu cần bật lại |
| `pref_sfx_enabled` | boolean / true | Mute cả UI và gameplay SFX |
| `pref_music_volume` | int0–100 /80 | Clamp, float V/100 × track trim × duck factor |
| `pref_sfx_volume` | int0–100 /80 | Clamp, float V/100 × approved per-asset/category gain |

SettingsActivity đã có switch/SeekBar/getters/setters, nhưng AndroidLauncher mới đọc difficulty/mode/language, chưa truyền audio settings. Sau duyệt, Android adapter đọc các key này, phát snapshot immutable cho core khi mở/resume/đổi cài đặt; UI/native music cùng snapshot. Core không phụ thuộc Android SharedPreferences. Không tạo key/schema/XML mới chỉ để làm lại controls đã có; resetDefaults phải cập nhật audio owner. Preview volume nếu cần chỉ khi người dùng dừng kéo slider, không spam từ mọi onProgressChanged hoặc loadValues/recreate. Chưa sửa preference/XML trong task này.

### Pause, modal, lifecycle và AudioFocus

User Pause và Weapon Box modal đóng bus gameplay, dừng/duck Music theo policy duyệt; UI Resume/commit còn dùng được. Không replay SFX đã bỏ qua sau resume. Demo dùng giây thực cho cooldown/fade/pitch. Home/Retry dọn voices, pending event IDs và session references trước session mới.

Khi app ra nền: ngừng audio, không chờ fade cần render tick; bỏ focus đúng ownership, không cho music tiếp tục phía sau app. GameScreen.resume hiện chờ người dùng Resume nên AudioManager cũng phải chờ, không dựa đơn thuần vào libGDX backend tự resume audio. AudioFocus coordinator chặn phát khi không được focus; transient loss pause/duck, permanent loss stop, focus gain chỉ tiếp tục nếu vẫn là foreground owner và không user-paused/modal/disabled. Callback Android marshalled về owner/thread phù hợp, không điều khiển Gdx từ Activity đã bị hủy.

Native dùng AudioAttributes phù hợp game/music và AudioFocusRequest API26+, fallback tương thích minSdk23. Target36 hiện có chịu điều kiện foreground của API35+; trên emulator API37 còn phải kiểm tra background audio policy. Không cần thêm foreground service để game tiếp tục phát khi ra nền; kế hoạch là tắt audio nền. Không sửa system volume của thiết bị để giả volume setting. [Audio focus Android](https://developer.android.com/media/optimize/audio-focus), [background audio API37](https://developer.android.com/about/versions/17/changes/bg-audio).

Task tích hợp sau duyệt mới kiểm thử focus denial, notification/call, Home/Resume với user pause, switch app, Settings.recreate, Retry, mute/volume0, nhiều Boss/impact. Cần quan sát backend stall/latency trên Pixel4 trước khi chọn async hoặc third-party engine; task audit không thêm dependency/backend.

## 9. Chi phí tài nguyên dự kiến

Nếu chỉ chọn candidate A của mỗi event SFX, sau dedup là **20 file,187.385bytes nén (~183KiB),717.700bytes PCM16 tương đương (~0,684MiB)**. Đây là cỡ palette ban đầu, chưa phải approval; biến thể B/C và asset còn thiếu sẽ thay đổi chi phí. Nạp tất cả151 SFX là~7,679MiB PCM trước overhead, nhiều Sound objects không cần thiết. Streaming bốn BGM lưu~10,420MiB nén trên đĩa nhưng tránh~83,337MiB PCM nạp hết; RAM codec/buffer thực cần đo backend, không bằng dung lượng nén. Không nạp Preview5MB PCM.

Thay đổi hiện tại chỉ thêm tài liệu/tool/evidence, nên **runtime thêm0 audio/0 player/0 voice**. Không có phép đo FPS audio trong task này, không suy từ FPS Task05. Kế hoạch giới hạn1–2 Music,16 SFX voices và tránh load/hàng loạt play trên render loop; phải kiểm chứng khi tích hợp.

## 10. Kiểm tra và điểm dừng

- Audit156/156 full decode OK,155 Ogg container kiểm tra không lỗi. Candidate/path trong matrix và HTML đều trỏ file thật, không quá3/event, không dùng Preview làm runtime candidate.
- HTML có49 card cho sáu nhóm,4 Future chỉ là backlog. Inline JavaScript đã kiểm syntax local bằng Node và đã render bằng Edge headless: đủ49 card, một audio element, `preload=none`, không `autoplay` hoặc media source lúc khởi tạo. Nguồn chỉ đặt sau click. **Chưa tự phát/nghe** hoặc bảo đảm mọi default Windows player hỗ trợ Ogg. Mở HTML bằng trình duyệt; nếu không phát được dùng link file với player hỗ trợ codec. DOM/log kiểm tra nằm trong thư mục evidence.
- Đã lưu baseline hash353 file gồm runtime/source/native/settings/assets, audio source và các báo cáo liên quan; kiểm tra trước/sau để xác nhận chúng không bị sửa. `scope-verification.json` lưu kết quả. Existing UI02/Task05 working-tree changes được giữ nguyên, không reset.
- Không triển khai AudioManager, không hook Weapon/Boss/Enemy/UI, không sửa gameplay/HUD/Settings/XML/preferences, không chỉnh hoặc copy audio vào runtime, không build/run/export APK/AAB, không stress suite và không commit/push.

Audit đã có đủ metadata, đề xuất, danh sách nghe và kế hoạch để chủ dự án chọn. **Dừng ở Task05.1A; Task05.1B chưa bắt đầu.** Sau khi nghe, gửi event ID + A/B hoặc JSON từ HTML, cùng quyết định cho MISSING; việc tích hợp vẫn cần yêu cầu tiếp theo.
