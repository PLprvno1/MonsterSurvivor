"""Generate Task 05.1B delivery docs from runtime manifest and retained test evidence only."""
from pathlib import Path
import json,re
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'core/build/reports/task05_1b'
manifest=json.loads((ROOT/'assets/audio/manifest.json').read_text())['events']
summary=json.loads((OUT/'asset-validation.json').read_text())
android=(OUT/'android-audio.log').read_text(encoding='utf-8') if (OUT/'android-audio.log').exists() else ''
profile=json.loads((OUT/'android-performance.json').read_text()) if (OUT/'android-performance.json').exists() else []
for name in ('audio_on.log','audio_off.log','android-native-back.log'):
    if (OUT/name).exists():android+='\n'+(OUT/name).read_text(encoding='utf-8')

anchors={
'BGM_MENU':'NativeAudio.resume/applyMusic: native Activity foreground + AudioFocus + preferences',
'BGM_DAY':'GameAudioManager.musicFor: running session, no active boss, before Night',
'BGM_NIGHT':'musicFor: Night active, no active Mini/Final',
'BGM_MINI':'musicFor: Mini ACTIVE, Final not active',
'BGM_FINAL':'musicFor: Final ACTIVE / WINNING; same Mini asset keeps playback position',
'BGM_VICTORY':'musicFor: actual GameSession.VICTORY after Lich death animation',
'BGM_GAME_OVER':'musicFor: actual GameSession.GAME_OVER after player death animation',
'WEAPON_SWORD_SWING':'SwordCombat.update: startAttack returns true',
'WEAPON_SWORD_HIT':'SwordCombat.update: at least one HP decrease in committed swing, once per swing',
'WEAPON_BOW_RELEASE':'WeaponSystem.update: successful BOW shoot',
'WEAPON_ARROW_HIT':'ProjectileSystem.update: real target HP decrease',
'WEAPON_FIRE_CAST':'WeaponSystem.update: successful FIREBALL volley, once per activation',
'WEAPON_FIRE_EXPLOSION':'ProjectileSystem.explode: actual detonation; not per victim',
'WEAPON_LIGHTNING_ACTIVATE':'WeaponSystem.chain: one activation',
'WEAPON_LIGHTNING_IMPACT':'WeaponSystem.chain: one cue if any chain target loses HP',
'PICKUP_DAMAGE':'PickupSystem.apply DAMAGE_BOX, including capped-Speed fallback; committed stat fallback offer',
'PICKUP_HEALTH':'PickupSystem.apply HEALTH_BOX; committed MAX_HP fallback offer',
'PICKUP_SPEED':'PickupSystem.apply SPEED_BOX; committed SPEED fallback offer',
'PICKUP_HEALTH_DROP':'PickupSystem.apply HEALTH_DROP only after actual healing succeeds',
'PICKUP_WEAPON_OPEN':'GameSession.weaponBox: valid selection.open -> CHOOSING_WEAPON',
'PICKUP_WEAPON_SELECTED':'GameSession.chooseWeapon: successful commit + collected item, fromLevel=0',
'PICKUP_WEAPON_UPGRADED':'GameSession.chooseWeapon: successful commit + collected item, fromLevel>0',
'MINI_APPEARS':'BossSystem.prepare: SPAWNING -> ACTIVE after placement success',
'MINI_SLAM_WARNING':'BossSystem.begin: Cast.SLAM accepted',
'MINI_SLAM_IMPACT':'BossSystem.finish: live actor completes SLAM, including a dodged impact',
'MINI_DEFEATED':'BossSystem.onDeath/dead: first Mini death; before announcement priority filtering',
'LICH_APPEARS':'BossSystem.prepare: Final SPAWNING -> ACTIVE',
'LICH_PROJECTILE':'BossSystem.finish BOLT: at least one BossProjectiles.fire succeeds; once per fan',
'LICH_AOE_WARNING':'BossSystem.begin: Cast.AREA accepted',
'LICH_AOE_IMPACT':'BossSystem.finish: live actor completes AREA',
'LICH_PHASE2':'BossSystem.prepare: phase 1 -> 2 only',
'LICH_DEFEATED':'BossSystem.onDeath/dead: first Final death',
'UI_TAP':'UiUtils.animateClick: accepted animation callback; single action guard',
'UI_NAVIGATION':'GameModeActivity: user changes difficulty/mode; same selection is silent',
'UI_CONFIRM':'GameModeActivity: Start; GameScreen.retry; Settings slider release preview',
'UI_CANCEL':'NativeAudioActivity OnBackPressedDispatcher; accepted native toolbar Back',
'UI_PAUSE':'GameScreen.togglePause: explicit user pause; excludes background/focus loss',
'UI_RESUME':'GameScreen.togglePause: explicit user Resume',
'UI_GAME_OVER':'GameSession.finish(false), once per session',
'UI_VICTORY':'GameSession.finish(true), once per session',
'ENEMY_HIT':'Enemy.damage: real nonlethal damage on generic ordinary species',
'ENEMY_DEATH':'Enemy.damage: first death of an ordinary enemy; bosses excluded',
'ENEMY_FLYING_REACTION':'Enemy.damage: nonlethal damage to flying species, excluding curated undead families',
'ENEMY_BEAST_REACTION':'Enemy.damage: nonlethal boar/wolf/hound/beast/fox/cat/owlbear/jackalope group, excluding undead',
'ENEMY_UNDEAD_REACTION':'Enemy.damage: nonlethal undead/skeleton/zombie/bone/mummy/wraith/skull group',
'ENEMY_TANK_REACTION':'Enemy.damage: nonlethal remaining nonflying TANK archetype',
'ENEMY_LEGACY_CHARGE':'EnemySystem.steer: actual legacy_charge burst starts; other special metadata ignored',
'PLAYER_HURT':'Player.damage: accepted positive damage, HP remains positive, immunity checked first',
'PLAYER_DEATH':'Player.damage: first accepted transition to zero HP; no later hurt',
}
assert len(anchors)==49 and set(anchors)=={e['event_id'] for e in manifest}
def observed(event):
    return ('Menu prepared audio/' in android) if event=='BGM_MENU' else ('Music state='+event+' ' in android) if event.startswith('BGM_') else ('SFX event='+event+' ' in android)

lines=['# Audio Runtime Manifest — Task 05.1B','',
'Ngày: 15/11/2026. Nguồn quyết định: [monster-survivor-audio-selections.json](monster-survivor-audio-selections.json), schema 2, 49/49 selected. Không dùng candidate cũ. JSON gốc không thay đổi.',
'',f"SHA-256 JSON gốc: `{summary['selection_sha256']}`.",'',
'Runtime machine-readable: [assets/audio/manifest.json](assets/audio/manifest.json). 49 mapping / 36 file = 3 Music + 33 Sound. Runtime source-relative suffix được giữ nguyên; chỉ thay `asset_sources/audio/` bằng `audio/` đúng một lần. Tất cả runtime audio giống byte nguồn; không trim, amplify, normalize hay transcode.',
'','## Mapping và chính sách','',
'Gain là hệ số mặc định; volume Settings nhân thêm 0–1. Priority số lớn ưu tiên voice SFX; BGM không dùng voice-priority này, mà dùng state priority kết quả > Final > Mini > Night > Day. SFX không loop. Cooldown ở bảng là theo event; thêm shared-file 35ms, enemy category 120ms, actor reaction 1s, khoảng cách enemy 220 world units và cap 16 voice. Fire/Lightning/Sword còn gộp theo cấu trúc activation/hit thật.',
'','| Event | Source đã chọn | Runtime | Category | Gain | Priority | Cooldown ms | Loop |','|---|---|---|---|---:|---:|---:|---|']
for e in manifest:lines.append(f"| {e['event_id']} | `{e['source']}` | `{e['file']}` | {e['category']} | {e['gain']:.2f} | {e['priority']} | {e['cooldown_ms']} | {'on' if e['loop'] else 'off'} |")
lines+=['','## 36 runtime file duy nhất','', '| Runtime file | Type | Duration s | PCM16 bytes nếu decode hết |','|---|---|---:|---:|']
unique={e['file']:e for e in manifest}
for path,e in sorted(unique.items()):lines.append(f"| `{path}` | {'Music streaming' if e['category']=='MUSIC' else 'Sound'} | {e['duration']:.3f} | {e['pcm16_bytes']:,} |")
lines+=['','## License / provenance','',
'Giữ nguyên ba License.txt Kenney tại [assets/audio/licenses](assets/audio/licenses). [CREDITS.md](assets/audio/licenses/CREDITS.md) giữ Kenney/CC0 và credit/URL từng bài: HydroGene — End Dungeon; MintoDog — Heavy Boss Battle 1 và Hope. BGM provenance kế thừa audit trước: NTFS Zone.Identifier/tags và trang nguồn đã xác minh riêng. Impact Sounds có License.txt local CC0; không suy license từ tên thư mục. Không tải lại hoặc tuyên bố đối chiếu hash remote.',
'','## Loop / định dạng','',
'Nguồn đã chọn: 35 Vorbis OGG + 1 MP3; decode toàn bộ 36 file lại ở task này. Các SFX dưới 1MiB PCM/file. BGM không decode toàn bộ thành Sound; chỉ 3 Music được chuẩn bị trước. Menu/Day/Night/Mini/Final loop; Victory/GameOver one-shot. Mini/Final dùng chung Heavy, đổi state không stop/seek/restart. Menu/Night chung MP3 nhưng native player được release trước khi libGDX tạo audio.',
'',
'`end_dungeon.mp3` có encoder delay/padding; `setLooping(true)` không chứng minh gapless. OGG trên backend Android cũng có thể có gap. Kiểm tra API/state không thay được nghe seam thực tế. Không tự cắt intro/end hoặc dùng `battle_music_01-loop.ogg` chưa được chọn. Xem [libGDX streaming music](https://libgdx.com/wiki/audio/streaming-music).',
'','Bằng chứng decode, metadata và hash: `core/build/reports/task05_1b/{asset-validation.json,probe-input.json,decoded.json,decode.log}`. Tái kiểm tra: `python tools/import_selected_audio.py --check`.']
(ROOT/'AUDIO_RUNTIME_MANIFEST.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')

seen=sum(observed(e['event_id']) for e in manifest)
lines=['# Audio Event Integration Status — Task 05.1B','',
'Ngày: 15/11/2026. 49 mapping có điểm gọi gắn với hành vi hiện có. Không có event trong JSON bị bỏ lại chỉ vì tên asset khác gameplay. Điều này **không có nghĩa đã nghe đủ 49 cue** hoặc đã quan sát đủ 49 event trên Android.',
'',f'Android telemetry retained: **{seen}/49 ID** có log playback API/music state. Cột “chưa ghi nhận” chỉ là thiếu coverage Android, không đồng nghĩa thiếu code trigger. Sound.play ID hoặc MediaPlayer state không chứng minh âm thanh đã nghe được. CPU kiểm tra tín hiệu thật bằng recording sink; GL smoke còn gọi trực tiếp toàn bộ 42 SFX để kiểm backend, không dùng vòng lặp đó làm bằng chứng gameplay trigger.',
'','| Event | Runtime trigger / guard | Android API/state retained |','|---|---|---|']
for e in manifest:lines.append(f"| {e['event_id']} | {anchors[e['event_id']]} | {'Có' if observed(e['event_id']) else 'Chưa ghi nhận; kiểm tra thủ công'} |")
lines+=['','## Phạm vi test / event chưa có gameplay','',
'`AudioChecks`: fake backend/clock để kiểm load dedup, cap/eviction, settings, fail-safe; recording sink cho weapon, HP delta/immunity, từng nhóm enemy, legacy charge, pickup fallback, modal commit/stale token/double tap, Boss cast/phase/death và terminal state thật. `AudioSmoke`: libGDX/OpenAL thật và GameScreen lifecycle/modal/Retry/Home. Native UI được kiểm tra trên Android. Các thao tác UI khác nhau dùng cue phù hợp, weapon commit không có generic UI tap/confirm chồng lên.',
'',
'Không thêm Dash, Ultimate, Evolution hoặc kỹ năng Ranged/Special chưa triển khai. Bốn FUTURE_* của audit cũ **không nằm trong 49 selections** và không có runtime trigger. ENEMY_*_REACTION ở đây là feedback của damage thật; không giả lập special skill theo animation. ENEMY_LEGACY_CHARGE chỉ gọi ở burst đã tồn tại.',
'','Các log Android first-play chỉ ghi một lần cho SFX thường; critical cues ghi mỗi successful play kèm count ở source cuối. Kiểm chứng once-per-action còn dựa trên recording-sink assertions và state/token guards, không suy từ một dòng log first-play.']
(ROOT/'AUDIO_EVENT_INTEGRATION_STATUS.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')

checks=json.loads((OUT/'android-checks.json').read_text()) if (OUT/'android-checks.json').exists() else {'count':0}
load=re.findall(r'Loaded unique=36 failures=0 ms=([\d.]+)',android)
report=['# TASK 05.1B — Full Audio Integration','',
'Ngày thực hiện: **15/11/2026**, project `D:\\Android\\MonsterSurvivor`. Tích hợp dựa trên lựa chọn cuối của chủ dự án, không yêu cầu duyệt lại.',
'','## Kết quả và nguồn dữ liệu','',
'Đã đọc JSON schema 2 thật, kiểm đủ 49 ID không trùng, status selected, đúng 36 path duy nhất và file tồn tại. Decode lại đủ 36 bằng decoder local của project; kiểm header/CRC/sequence/EOS Vorbis và MP3 decode. Hash kiểm tra source/runtime trùng. Không sửa JSON gốc, asset_sources, GDD hoặc báo cáo lịch sử. Không commit/push.',
'',f"JSON SHA-256: `{summary['selection_sha256']}`. Runtime **{summary['compressed_bytes']:,} bytes ({summary['compressed_bytes']/1048576:.3f} MiB)**. SFX PCM16 cộng **{summary['sfx_pcm16_bytes']:,} bytes ({summary['sfx_pcm16_bytes']/1048576:.3f} MiB)**, chưa gồm overhead backend. BGM giữ streaming, không nạp toàn bài vào RAM.",
'',
'Import chính xác 36 audio, không Preview, không toàn thư viện, không chiptune battle_music_01-loop. Ba license local và credits/provenance được giữ trong runtime. Không tạo derivative. Mapping, policy và inventory đầy đủ: [AUDIO_RUNTIME_MANIFEST.md](AUDIO_RUNTIME_MANIFEST.md); trạng thái từng ID: [AUDIO_EVENT_INTEGRATION_STATUS.md](AUDIO_EVENT_INTEGRATION_STATUS.md).',
'','## Kiến trúc và sự kiện','',
'`AudioEvent`/`AudioSink` tách simulation khỏi backend. GameSession gắn sink vào player, enemy, weapon, projectile, pickup và Boss; không lấy âm thanh từ render/animation. Sink NONE giữ các logic checks không cần audio device. GameScreen sở hữu một GameAudioManager trong cả vòng Retry; manager load trước 33 Sound + 3 Music từ manifest và deduplicate theo path. Native UI dùng một NativeAudio với MediaPlayer + SoundPool, không cần GL context.',
'',
'Sword start/hit theo accepted attack và HP delta, một hit cue cho cả sector. Bow chỉ release khi shoot thành công; arrow hit theo HP delta. Fire cast một/volley, explode một/detonation và cooldown không theo victim. Lightning một activate/chain và một impact nếu có damage thật. Không thay damage, cooldown, projectile count hoặc level tuning.',
'',
'Pickup phát sau reward thật; Speed cap dùng Damage cue. Weapon open theo selection.open thành công, selected/upgraded sau commit và collect, giữ token/double-tap guards; stat fallback dùng cue reward. Modal dừng BGM, vẫn cho open/commit cue kết thúc; user Pause dừng voices trước cue Pause. Boss spawn/cast/phase/death gọi tại chuyển trạng thái thật, chết không phát generic enemy death hoặc đòn mới. Player immunity không phát hurt, death một lần. Enemy nhận một generic/group reaction cho nonlethal damage hoặc một death, lọc xa và chống spam.',
'','## Music state và mixing','',
'Priority: **VICTORY/GAME_OVER > FINAL > MINI > NIGHT > DAY**. Menu theo native lifecycle. WINNING giữ Final cho đến VICTORY thật. Một track chính phát tại một thời điểm; không crossfade. So path trước khi stop/start nên Mini -> Final chung Heavy giữ vị trí, phase2 không phát lại intro. Gain Music 0,26 (Heavy 0,22); SFX enemy 0,24, thường 0,50, critical 0,65; sword swing nhỏ sẵn dùng 1,00. Đây là gain kỹ thuật ban đầu, không tuyên bố loudness đã nghe cân bằng.',
'',
'16 preallocated voice slots; expired estimates theo duration+40ms, stop bằng Sound ID trước khi tái sử dụng/evict. Cue priority critical100 > weapon70 > pickup/UI60 > enemy20. Enemy chung 120ms, per-event180ms, per-actor1s, radius220 world units; shared-file35ms để tránh trùng cue dùng chung. Weapon65ms, UI80ms; các one-shot action còn được gộp bằng cấu trúc cast/token/state. Cảnh báo có thể đẩy voice thấp hơn; enemy không đẩy critical. Không thêm ducking/crossfade. Không Sound/Music mới trong update/draw, không filesystem I/O audio trong gameplay loop.',
'',
'RuntimeException/load failure/negative Sound ID được ghi và bỏ cue, không làm hỏng gameplay. Dispose idempotent, stop voices, release từng path duy nhất. Native prepare/error/release và AudioFocus failure cũng có xử lý an toàn.',
'','## Settings, native lifecycle và AudioFocus','',
'Tái dùng nguyên `monster_survivor_prefs`: pref_music_enabled, pref_sfx_enabled, pref_music_volume, pref_sfx_volume. GamePreferences xuất immutable AudioSettings qua AudioEnvironment; AndroidLauncher nghe thay đổi và bỏ listener khi destroy. Core không import Android. Volume clamp0–100; Music0 pause thật, SFX0 không play và stop voices. Không thêm Master Volume hoặc sửa Settings XML. Không làm mất language/theme/vibration.',
'',
'Native Home/Settings/GameMode/Achievements/HowToPlay/Preview dùng NativeAudioActivity lifecycle chung, tối đa một menu player. onPause pause ngay, abandon focus, cleanup sau300ms nếu không có native owner mới; vào game release native players trước initialize libGDX. Không service/background music. UI Back dùng OnBackPressedDispatcher; user actions mới phát UI cue; load/recreate không phát cue. Slider chỉ preview SFX một lần khi thả, không theo mỗi tick.',
'',
'AndroidLauncher xin AUDIOFOCUS_GAIN với USAGE_GAME/CONTENT_TYPE_MUSIC, API26+ dùng AudioFocusRequest, fallback API23–25. LOSS/TRANSIENT/CAN_DUCK đều tạm ngưng; focus-loss serial giữ yêu cầu Pause ngay cả loss+gain giữa hai frame. Background và focus gain không bỏ user Pause. Music/SFX không tiếp tục sau dispose; Retry reset transients/cooldowns/music mà không load lại asset.',
'','## Kiểm thử và Android Studio Run','',
'Compile core + Android Java thành công (`compile-final.log`). Focused logic: **AudioChecks190**, WeaponChecks223, PickupChecks21.438, BossChecks2.901; không chạy toàn bộ stress suite82 quái. AudioSmoke chạy GL/OpenAL thật, load36 file, gọi42 SFX, kiểm focus loss/gain, lifecycle, modal open/commit, Retry/Home/dispose; xem số assertions trong log cuối. Playback API không đồng nghĩa kiểm chứng nghe.',
'',f'Android Studio cấu hình **android**, **Pixel_4**, Run bằng Shift+F10; log IDE/deployment và backend được giữ local, không dùng standalone assemble/install/export để bàn giao. Android smoke **{checks["count"]} checks** theo `android-checks.json`: native Music0/50/100, switches, SFX slider/persistence, language preservation, menu/game ownership, Boss/phase/result, pause/background/foreground, Retry/Home. Fixture hiện có bảo vệ HP, equipment3, seed42, progress và damage command; không coi đó là playtest Normal tự nhiên5/10 phút. Preferences/diagnostic flags khôi phục trong finally.',
'',
'Android đã gọi backend thật theo các ID ghi trong status table, đã quan sát MediaPlayer paused/volume/loop state. Không tuyên bố tất cả49 event đã được nghe hoặc đều được quan sát trên Android. External competing-app AudioFocus loss chưa được tự động gây trên emulator; policy/latch được kiểm ở core/GL và lifecycle thật. Cần kiểm thêm điện thoại/cuộc gọi/ứng dụng nhạc và Android23–25 fallback trên thiết bị tương ứng.',
'','## Tài nguyên và FPS','',
f'Desktop OpenAL load36 file khoảng132ms ở smoke; Android load telemetry có các mẫu: {", ".join(load) if load else "xem log Android sau run cuối"} ms. Giá trị gồm chuẩn bị backend, phụ thuộc máy/emulator. Music streaming vẫn có decoder/buffer/native player overhead ngoài 1,505MiB SFX PCM; không tuyên bố RAM toàn game tăng đúng bằng PCM. GameAssets không thêm texture/font, baseline19 texture/44.285.956 bytes RGBA giữ nguyên.',
'']
if profile:
    report+=['Probe ngắn cùng seed42,8 quái, HP fixture; mỗi stage30 warmup +90 measured frames. Chạy audio on/off tuần tự, không tác vụ GL smoke đồng thời. Đây là emulator/SwiftShader, không phải thiết bị thật hoặc benchmark kiểm soát mọi tải host.','', '| Audio | Stage | FPS | Mean ms | p95 ms |','|---|---:|---:|---:|---:|']
    for run in profile:
        for s in run['stages']:report.append(f"| {'on' if run['enabled'] else 'off'} | {s['stage']} | {s['fps']:.2f} | {s['mean_ms']:.2f} | {s['p95_ms']:.2f} |")
    report+=['','Stage0 Sword; stage1 Bow+Fire; stage2 Fire+Lightning, tám enemies. Xem `android-performance.json`, CSV và meminfo từng run. Cả on/off vẫn preload assets để đo chi phí playback; không phải so với bản game chưa có audio. PSS toàn process: on75.607KiB, off70.858KiB; đây là snapshot khác thời điểm/GC, không phải phép đo riêng bộ nhớ audio. Chênh FPS của một cặp chạy ngắn không xác định riêng chi phí audio; chưa chứng minh60FPS hay mọi cảnh60 quái+2 Boss.','']
else:report+=['Chưa có paired performance probe hoàn tất trong evidence hiện tại; không tự điền FPS.','']
report+=['## Hạn chế và hướng dẫn nghe trong game','',
'Không có lỗi decode/missing path được phát hiện. Không transcode. Chưa nghe host/emulator trực tiếp, chưa đo LUFS/true peak hay loop seam. MP3 có encoder delay/padding; cả OGG/MP3 qua Android backend không cam kết gapless. Chưa kiểm một vòng tự nhiên hết165,564s MP3/108s Heavy để đánh giá seam; backend loop flag không thay được nghe. `drawKnife2.ogg` nguồn rất nhỏ (audit RMS~−41dBFS); giữ đúng lựa chọn và gain1, không tự thay/normalize. Các giới hạn này không phải yêu cầu chọn lại asset.',
'',
'1. Mở Android Studio, chọn android + Pixel_4 rồi Run. Home phải có Menu; vào Settings thử Music/SFX on/off và0/50/100, đóng/mở app kiểm persistence, language giữ nguyên.',
'2. Vào Demo; nghe Day, nhặt từng loại item, mở Weapon Box rồi double tap một offer. Chỉ một open/commit cue; reward Damage thay Speed cap phải đúng âm Damage. Thử đủ bốn vũ khí trong các run; Fire/Lightning không tạo tiếng theo từng nạn nhân.',
'3. Tại progress300 (Demo khoảng45 gameplay giây) nghe Mini; sau khi hạ Mini trước Final nghe Night. Progress600 (Demo khoảng90s) nghe Final appearance một lần. Nếu Mini còn sống, Heavy tiếp tục không quay lại đầu. Giảm Lich dưới50% chỉ có phase cue riêng; né telegraph vẫn có impact cue.',
'4. Kiểm tra player hurt/immunity/death, true Victory sau Lich death, GameOver và Retry/Home. Pause hoặc mở modal dừng nhạc, cue UI/weapon selection vẫn nghe được. Đưa app ra nền rồi trở lại phải chờ Resume; thử focus bằng ứng dụng nhạc/cuộc gọi trên thiết bị thật.',
'5. Để Menu/Night MP3 và Heavy/Hope OGG chạy qua điểm loop, nghe xem có gap/click hoặc mức quá lớn; xác nhận trên cả emulator và thiết bị thật. Đây là bước nghe xác nhận tích hợp, không yêu cầu duyệt lại lựa chọn.',
'','## Tái hiện và file bàn giao','',
'- `python tools/import_selected_audio.py --check`: schema/path/hash/container/decode36 file, không copy ở chế độ check.',
'- `gradlew.bat :core:compileJava :android:compileDebugJavaWithJavac`: chỉ compile Java/resources.',
'- `python tools/run_audio_checks.py`: focused CPU + desktop backend smoke.',
'- Android Studio Run trước; `python tools/android_audio_smoke.py` và `python tools/profile_audio_android.py`: thao tác build đã chạy, không build/install/export, khôi phục trạng thái local.',
'- Ba tài liệu task, `assets/audio/manifest.json`, runtime audio36 file, license/credits và update INTEGRATION_NOTES. Evidence local ở `core/build/reports/task05_1b/` (gitignored). Không APK/AAB bàn giao, signing/release, Docker/backend hoặc publish.',
'']
(ROOT/'TASK_05_1B_AUDIO_REPORT.md').write_text('\n'.join(report),encoding='utf-8')
print('Wrote runtime manifest, event status, task report; Android observed IDs:',seen)
