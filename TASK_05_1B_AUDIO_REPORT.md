# TASK 05.1B — Full Audio Integration

Ngày thực hiện: **15/11/2026**, project `D:\Android\MonsterSurvivor`. Tích hợp dựa trên lựa chọn cuối của chủ dự án, không yêu cầu duyệt lại.

## Kết quả và nguồn dữ liệu

Đã đọc JSON schema 2 thật, kiểm đủ 49 ID không trùng, status selected, đúng 36 path duy nhất và file tồn tại. Decode lại đủ 36 bằng decoder local của project; kiểm header/CRC/sequence/EOS Vorbis và MP3 decode. Hash kiểm tra source/runtime trùng. Không sửa JSON gốc, asset_sources, GDD hoặc báo cáo lịch sử. Không commit/push.

JSON SHA-256: `ca01f7a8e99e72f6acae920b24130cd31880c31a7e888a15586a8fb0dd961dc4`. Runtime **8,557,728 bytes (8.161 MiB)**. SFX PCM16 cộng **1,601,924 bytes (1.528 MiB)**, chưa gồm overhead backend. BGM giữ streaming, không nạp toàn bài vào RAM.

Import chính xác 36 audio, không Preview, không toàn thư viện, không chiptune battle_music_01-loop. Ba license local và credits/provenance được giữ trong runtime. Không tạo derivative. Mapping, policy và inventory đầy đủ: [AUDIO_RUNTIME_MANIFEST.md](AUDIO_RUNTIME_MANIFEST.md); trạng thái từng ID: [AUDIO_EVENT_INTEGRATION_STATUS.md](AUDIO_EVENT_INTEGRATION_STATUS.md).

## Kiến trúc và sự kiện

`AudioEvent`/`AudioSink` tách simulation khỏi backend. GameSession gắn sink vào player, enemy, weapon, projectile, pickup và Boss; không lấy âm thanh từ render/animation. Sink NONE giữ các logic checks không cần audio device. GameScreen sở hữu một GameAudioManager trong cả vòng Retry; manager load trước 33 Sound + 3 Music từ manifest và deduplicate theo path. Native UI dùng một NativeAudio với MediaPlayer + SoundPool, không cần GL context.

Sword start/hit theo accepted attack và HP delta, một hit cue cho cả sector. Bow chỉ release khi shoot thành công; arrow hit theo HP delta. Fire cast một/volley, explode một/detonation và cooldown không theo victim. Lightning một activate/chain và một impact nếu có damage thật. Không thay damage, cooldown, projectile count hoặc level tuning.

Pickup phát sau reward thật; Speed cap dùng Damage cue. Weapon open theo selection.open thành công, selected/upgraded sau commit và collect, giữ token/double-tap guards; stat fallback dùng cue reward. Modal dừng BGM, vẫn cho open/commit cue kết thúc; user Pause dừng voices trước cue Pause. Boss spawn/cast/phase/death gọi tại chuyển trạng thái thật, chết không phát generic enemy death hoặc đòn mới. Player immunity không phát hurt, death một lần. Enemy nhận một generic/group reaction cho nonlethal damage hoặc một death, lọc xa và chống spam.

## Music state và mixing

Priority: **VICTORY/GAME_OVER > FINAL > MINI > NIGHT > DAY**. Menu theo native lifecycle. WINNING giữ Final cho đến VICTORY thật. Một track chính phát tại một thời điểm; không crossfade. So path trước khi stop/start nên Mini -> Final chung Heavy giữ vị trí, phase2 không phát lại intro. Gain Music 0,26 (Heavy 0,22); SFX enemy 0,24, thường 0,50, critical 0,65; sword swing nhỏ sẵn dùng 1,00. Đây là gain kỹ thuật ban đầu, không tuyên bố loudness đã nghe cân bằng.

16 preallocated voice slots; expired estimates theo duration+40ms, stop bằng Sound ID trước khi tái sử dụng/evict. Cue priority critical100 > weapon70 > pickup/UI60 > enemy20. Enemy chung 120ms, per-event180ms, per-actor1s, radius220 world units; shared-file35ms để tránh trùng cue dùng chung. Weapon65ms, UI80ms; các one-shot action còn được gộp bằng cấu trúc cast/token/state. Cảnh báo có thể đẩy voice thấp hơn; enemy không đẩy critical. Không thêm ducking/crossfade. Không Sound/Music mới trong update/draw, không filesystem I/O audio trong gameplay loop.

RuntimeException/load failure/negative Sound ID được ghi và bỏ cue, không làm hỏng gameplay. Dispose idempotent, stop voices, release từng path duy nhất. Native prepare/error/release và AudioFocus failure cũng có xử lý an toàn.

## Settings, native lifecycle và AudioFocus

Tái dùng nguyên `monster_survivor_prefs`: pref_music_enabled, pref_sfx_enabled, pref_music_volume, pref_sfx_volume. GamePreferences xuất immutable AudioSettings qua AudioEnvironment; AndroidLauncher nghe thay đổi và bỏ listener khi destroy. Core không import Android. Volume clamp0–100; Music0 pause thật, SFX0 không play và stop voices. Không thêm Master Volume hoặc sửa Settings XML. Không làm mất language/theme/vibration.

Native Home/Settings/GameMode/Achievements/HowToPlay/Preview dùng NativeAudioActivity lifecycle chung, tối đa một menu player. onPause pause ngay, abandon focus, cleanup sau300ms nếu không có native owner mới; vào game release native players trước initialize libGDX. Không service/background music. UI Back dùng OnBackPressedDispatcher; user actions mới phát UI cue; load/recreate không phát cue. Slider chỉ preview SFX một lần khi thả, không theo mỗi tick.

AndroidLauncher xin AUDIOFOCUS_GAIN với USAGE_GAME/CONTENT_TYPE_MUSIC, API26+ dùng AudioFocusRequest, fallback API23–25. LOSS/TRANSIENT/CAN_DUCK đều tạm ngưng; focus-loss serial giữ yêu cầu Pause ngay cả loss+gain giữa hai frame. Background và focus gain không bỏ user Pause. Music/SFX không tiếp tục sau dispose; Retry reset transients/cooldowns/music mà không load lại asset.

## Kiểm thử và Android Studio Run

Compile core + Android Java thành công (`compile-final.log`). Focused logic: **AudioChecks190**, WeaponChecks223, PickupChecks21.438, BossChecks2.901; không chạy toàn bộ stress suite82 quái. AudioSmoke chạy GL/OpenAL thật, load36 file, gọi42 SFX, kiểm focus loss/gain, lifecycle, modal open/commit, Retry/Home/dispose; xem số assertions trong log cuối. Playback API không đồng nghĩa kiểm chứng nghe.

Android Studio cấu hình **android**, **Pixel_4**, Run bằng Shift+F10; log IDE/deployment và backend được giữ local, không dùng standalone assemble/install/export để bàn giao. Android smoke **31 checks** theo `android-checks.json`: native Music0/50/100, switches, SFX slider/persistence, language preservation, menu/game ownership, Boss/phase/result, pause/background/foreground, Retry/Home. Fixture hiện có bảo vệ HP, equipment3, seed42, progress và damage command; không coi đó là playtest Normal tự nhiên5/10 phút. Preferences/diagnostic flags khôi phục trong finally.

Android đã gọi backend thật theo các ID ghi trong status table, đã quan sát MediaPlayer paused/volume/loop state. Không tuyên bố tất cả49 event đã được nghe hoặc đều được quan sát trên Android. External competing-app AudioFocus loss chưa được tự động gây trên emulator; policy/latch được kiểm ở core/GL và lifecycle thật. Cần kiểm thêm điện thoại/cuộc gọi/ứng dụng nhạc và Android23–25 fallback trên thiết bị tương ứng.

## Tài nguyên và FPS

Desktop OpenAL load36 file khoảng132ms ở smoke; Android load telemetry có các mẫu: 129.9868, 152.074, 152.074, 547.7833, 415.8067 ms. Giá trị gồm chuẩn bị backend, phụ thuộc máy/emulator. Music streaming vẫn có decoder/buffer/native player overhead ngoài 1,505MiB SFX PCM; không tuyên bố RAM toàn game tăng đúng bằng PCM. GameAssets không thêm texture/font, baseline19 texture/44.285.956 bytes RGBA giữ nguyên.

Probe ngắn cùng seed42,8 quái, HP fixture; mỗi stage30 warmup +90 measured frames. Chạy audio on/off tuần tự, không tác vụ GL smoke đồng thời. Đây là emulator/SwiftShader, không phải thiết bị thật hoặc benchmark kiểm soát mọi tải host.

| Audio | Stage | FPS | Mean ms | p95 ms |
|---|---:|---:|---:|---:|
| on | 0 | 26.04 | 38.41 | 52.19 |
| on | 1 | 26.31 | 38.01 | 53.19 |
| on | 2 | 27.92 | 35.82 | 44.26 |
| off | 0 | 22.92 | 43.64 | 62.43 |
| off | 1 | 27.95 | 35.78 | 44.12 |
| off | 2 | 28.23 | 35.42 | 48.19 |

Stage0 Sword; stage1 Bow+Fire; stage2 Fire+Lightning, tám enemies. Xem `android-performance.json`, CSV và meminfo từng run. Cả on/off vẫn preload assets để đo chi phí playback; không phải so với bản game chưa có audio. PSS toàn process: on75.607KiB, off70.858KiB; đây là snapshot khác thời điểm/GC, không phải phép đo riêng bộ nhớ audio. Chênh FPS của một cặp chạy ngắn không xác định riêng chi phí audio; chưa chứng minh60FPS hay mọi cảnh60 quái+2 Boss.

## Hạn chế và hướng dẫn nghe trong game

Không có lỗi decode/missing path được phát hiện. Không transcode. Chưa nghe host/emulator trực tiếp, chưa đo LUFS/true peak hay loop seam. MP3 có encoder delay/padding; cả OGG/MP3 qua Android backend không cam kết gapless. Chưa kiểm một vòng tự nhiên hết165,564s MP3/108s Heavy để đánh giá seam; backend loop flag không thay được nghe. `drawKnife2.ogg` nguồn rất nhỏ (audit RMS~−41dBFS); giữ đúng lựa chọn và gain1, không tự thay/normalize. Các giới hạn này không phải yêu cầu chọn lại asset.

1. Mở Android Studio, chọn android + Pixel_4 rồi Run. Home phải có Menu; vào Settings thử Music/SFX on/off và0/50/100, đóng/mở app kiểm persistence, language giữ nguyên.
2. Vào Demo; nghe Day, nhặt từng loại item, mở Weapon Box rồi double tap một offer. Chỉ một open/commit cue; reward Damage thay Speed cap phải đúng âm Damage. Thử đủ bốn vũ khí trong các run; Fire/Lightning không tạo tiếng theo từng nạn nhân.
3. Tại progress300 (Demo khoảng45 gameplay giây) nghe Mini; sau khi hạ Mini trước Final nghe Night. Progress600 (Demo khoảng90s) nghe Final appearance một lần. Nếu Mini còn sống, Heavy tiếp tục không quay lại đầu. Giảm Lich dưới50% chỉ có phase cue riêng; né telegraph vẫn có impact cue.
4. Kiểm tra player hurt/immunity/death, true Victory sau Lich death, GameOver và Retry/Home. Pause hoặc mở modal dừng nhạc, cue UI/weapon selection vẫn nghe được. Đưa app ra nền rồi trở lại phải chờ Resume; thử focus bằng ứng dụng nhạc/cuộc gọi trên thiết bị thật.
5. Để Menu/Night MP3 và Heavy/Hope OGG chạy qua điểm loop, nghe xem có gap/click hoặc mức quá lớn; xác nhận trên cả emulator và thiết bị thật. Đây là bước nghe xác nhận tích hợp, không yêu cầu duyệt lại lựa chọn.

## Tái hiện và file bàn giao

- `python tools/import_selected_audio.py --check`: schema/path/hash/container/decode36 file, không copy ở chế độ check.
- `gradlew.bat :core:compileJava :android:compileDebugJavaWithJavac`: chỉ compile Java/resources.
- `python tools/run_audio_checks.py`: focused CPU + desktop backend smoke.
- Android Studio Run trước; `python tools/android_audio_smoke.py` và `python tools/profile_audio_android.py`: thao tác build đã chạy, không build/install/export, khôi phục trạng thái local.
- Ba tài liệu task, `assets/audio/manifest.json`, runtime audio36 file, license/credits và update INTEGRATION_NOTES. Evidence local ở `core/build/reports/task05_1b/` (gitignored). Không APK/AAB bàn giao, signing/release, Docker/backend hoặc publish.
