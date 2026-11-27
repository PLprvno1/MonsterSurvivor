# Audio Runtime Manifest — Task 05.1B

Ngày: 15/11/2026. Nguồn quyết định: [monster-survivor-audio-selections.json](monster-survivor-audio-selections.json), schema 2, 49/49 selected. Không dùng candidate cũ. JSON gốc không thay đổi.

SHA-256 JSON gốc: `ca01f7a8e99e72f6acae920b24130cd31880c31a7e888a15586a8fb0dd961dc4`.

Runtime machine-readable: [assets/audio/manifest.json](assets/audio/manifest.json). 49 mapping / 36 file = 3 Music + 33 Sound. Runtime source-relative suffix được giữ nguyên; chỉ thay `asset_sources/audio/` bằng `audio/` đúng một lần. Tất cả runtime audio giống byte nguồn; không trim, amplify, normalize hay transcode.

## Mapping và chính sách

Gain là hệ số mặc định; volume Settings nhân thêm 0–1. Priority số lớn ưu tiên voice SFX; BGM không dùng voice-priority này, mà dùng state priority kết quả > Final > Mini > Night > Day. SFX không loop. Cooldown ở bảng là theo event; thêm shared-file 35ms, enemy category 120ms, actor reaction 1s, khoảng cách enemy 220 world units và cap 16 voice. Fire/Lightning/Sword còn gộp theo cấu trúc activation/hit thật.

| Event | Source đã chọn | Runtime | Category | Gain | Priority | Cooldown ms | Loop |
|---|---|---|---|---:|---:|---:|---|
| BGM_MENU | `asset_sources/audio/end_dungeon.mp3` | `audio/end_dungeon.mp3` | MUSIC | 0.26 | 20 | 0 | on |
| BGM_DAY | `asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg` | `audio/hope_orchestral_battle_music_bpm165.ogg` | MUSIC | 0.26 | 20 | 0 | on |
| BGM_NIGHT | `asset_sources/audio/end_dungeon.mp3` | `audio/end_dungeon.mp3` | MUSIC | 0.26 | 20 | 0 | on |
| BGM_MINI | `asset_sources/audio/heavy_boss_battle_1_bpm200.ogg` | `audio/heavy_boss_battle_1_bpm200.ogg` | MUSIC | 0.22 | 20 | 0 | on |
| BGM_FINAL | `asset_sources/audio/heavy_boss_battle_1_bpm200.ogg` | `audio/heavy_boss_battle_1_bpm200.ogg` | MUSIC | 0.22 | 20 | 0 | on |
| BGM_VICTORY | `asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg` | `audio/hope_orchestral_battle_music_bpm165.ogg` | MUSIC | 0.26 | 20 | 0 | off |
| BGM_GAME_OVER | `asset_sources/audio/end_dungeon.mp3` | `audio/end_dungeon.mp3` | MUSIC | 0.26 | 20 | 0 | off |
| WEAPON_SWORD_SWING | `asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice2.ogg` | `audio/kenney_rpg-audio/Audio/knifeSlice2.ogg` | WEAPON | 1.00 | 70 | 65 | off |
| WEAPON_SWORD_HIT | `asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg` | `audio/kenney_rpg-audio/Audio/chop.ogg` | WEAPON | 0.50 | 70 | 65 | off |
| WEAPON_BOW_RELEASE | `asset_sources/audio/kenney_interface-sounds/Audio/scratch_001.ogg` | `audio/kenney_interface-sounds/Audio/scratch_001.ogg` | WEAPON | 0.50 | 70 | 65 | off |
| WEAPON_ARROW_HIT | `asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg` | `audio/kenney_rpg-audio/Audio/dropLeather.ogg` | WEAPON | 0.50 | 70 | 65 | off |
| WEAPON_FIRE_CAST | `asset_sources/audio/kenney_interface-sounds/Audio/maximize_005.ogg` | `audio/kenney_interface-sounds/Audio/maximize_005.ogg` | WEAPON | 0.50 | 70 | 65 | off |
| WEAPON_FIRE_EXPLOSION | `asset_sources/audio/kenney_impact-sounds/Audio/impactPlate_medium_004.ogg` | `audio/kenney_impact-sounds/Audio/impactPlate_medium_004.ogg` | WEAPON | 0.50 | 70 | 65 | off |
| WEAPON_LIGHTNING_ACTIVATE | `asset_sources/audio/kenney_interface-sounds/Audio/maximize_007.ogg` | `audio/kenney_interface-sounds/Audio/maximize_007.ogg` | WEAPON | 0.50 | 70 | 65 | off |
| WEAPON_LIGHTNING_IMPACT | `asset_sources/audio/kenney_interface-sounds/Audio/minimize_005.ogg` | `audio/kenney_interface-sounds/Audio/minimize_005.ogg` | WEAPON | 0.50 | 70 | 65 | off |
| PICKUP_DAMAGE | `asset_sources/audio/kenney_impact-sounds/Audio/impactPunch_medium_003.ogg` | `audio/kenney_impact-sounds/Audio/impactPunch_medium_003.ogg` | PICKUP | 0.50 | 60 | 0 | off |
| PICKUP_HEALTH | `asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg` | `audio/kenney_interface-sounds/Audio/confirmation_003.ogg` | PICKUP | 0.50 | 60 | 0 | off |
| PICKUP_SPEED | `asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg` | `audio/kenney_interface-sounds/Audio/glass_002.ogg` | PICKUP | 0.50 | 60 | 0 | off |
| PICKUP_HEALTH_DROP | `asset_sources/audio/kenney_interface-sounds/Audio/glass_005.ogg` | `audio/kenney_interface-sounds/Audio/glass_005.ogg` | PICKUP | 0.50 | 60 | 0 | off |
| PICKUP_WEAPON_OPEN | `asset_sources/audio/kenney_interface-sounds/Audio/open_001.ogg` | `audio/kenney_interface-sounds/Audio/open_001.ogg` | PICKUP | 0.50 | 60 | 0 | off |
| PICKUP_WEAPON_SELECTED | `asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg` | `audio/kenney_interface-sounds/Audio/confirmation_001.ogg` | PICKUP | 0.50 | 60 | 0 | off |
| PICKUP_WEAPON_UPGRADED | `asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg` | `audio/kenney_interface-sounds/Audio/confirmation_003.ogg` | PICKUP | 0.50 | 60 | 0 | off |
| MINI_APPEARS | `asset_sources/audio/kenney_interface-sounds/Audio/scroll_002.ogg` | `audio/kenney_interface-sounds/Audio/scroll_002.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| MINI_SLAM_WARNING | `asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg` | `audio/kenney_interface-sounds/Audio/bong_001.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| MINI_SLAM_IMPACT | `asset_sources/audio/kenney_impact-sounds/Audio/impactPunch_medium_004.ogg` | `audio/kenney_impact-sounds/Audio/impactPunch_medium_004.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| MINI_DEFEATED | `asset_sources/audio/kenney_interface-sounds/Audio/close_002.ogg` | `audio/kenney_interface-sounds/Audio/close_002.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| LICH_APPEARS | `asset_sources/audio/kenney_interface-sounds/Audio/scroll_002.ogg` | `audio/kenney_interface-sounds/Audio/scroll_002.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| LICH_PROJECTILE | `asset_sources/audio/kenney_interface-sounds/Audio/minimize_001.ogg` | `audio/kenney_interface-sounds/Audio/minimize_001.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| LICH_AOE_WARNING | `asset_sources/audio/kenney_interface-sounds/Audio/scratch_005.ogg` | `audio/kenney_interface-sounds/Audio/scratch_005.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| LICH_AOE_IMPACT | `asset_sources/audio/kenney_interface-sounds/Audio/question_001.ogg` | `audio/kenney_interface-sounds/Audio/question_001.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| LICH_PHASE2 | `asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg` | `audio/kenney_interface-sounds/Audio/maximize_001.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| LICH_DEFEATED | `asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg` | `audio/kenney_interface-sounds/Audio/confirmation_004.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| UI_TAP | `asset_sources/audio/kenney_interface-sounds/Audio/click_001.ogg` | `audio/kenney_interface-sounds/Audio/click_001.ogg` | UI | 0.50 | 60 | 80 | off |
| UI_NAVIGATION | `asset_sources/audio/kenney_interface-sounds/Audio/select_002.ogg` | `audio/kenney_interface-sounds/Audio/select_002.ogg` | UI | 0.50 | 60 | 80 | off |
| UI_CONFIRM | `asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg` | `audio/kenney_interface-sounds/Audio/confirmation_001.ogg` | UI | 0.50 | 60 | 80 | off |
| UI_CANCEL | `asset_sources/audio/kenney_interface-sounds/Audio/back_003.ogg` | `audio/kenney_interface-sounds/Audio/back_003.ogg` | UI | 0.50 | 60 | 80 | off |
| UI_PAUSE | `asset_sources/audio/kenney_interface-sounds/Audio/minimize_001.ogg` | `audio/kenney_interface-sounds/Audio/minimize_001.ogg` | UI | 0.50 | 60 | 80 | off |
| UI_RESUME | `asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg` | `audio/kenney_interface-sounds/Audio/maximize_001.ogg` | UI | 0.50 | 60 | 80 | off |
| UI_GAME_OVER | `asset_sources/audio/kenney_interface-sounds/Audio/error_003.ogg` | `audio/kenney_interface-sounds/Audio/error_003.ogg` | UI | 0.50 | 100 | 80 | off |
| UI_VICTORY | `asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg` | `audio/kenney_interface-sounds/Audio/confirmation_004.ogg` | UI | 0.50 | 100 | 80 | off |
| ENEMY_HIT | `asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg` | `audio/kenney_rpg-audio/Audio/chop.ogg` | ENEMY | 0.24 | 20 | 180 | off |
| ENEMY_DEATH | `asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg` | `audio/kenney_rpg-audio/Audio/dropLeather.ogg` | ENEMY | 0.24 | 20 | 180 | off |
| ENEMY_FLYING_REACTION | `asset_sources/audio/kenney_impact-sounds/Audio/footstep_concrete_003.ogg` | `audio/kenney_impact-sounds/Audio/footstep_concrete_003.ogg` | ENEMY | 0.24 | 20 | 180 | off |
| ENEMY_BEAST_REACTION | `asset_sources/audio/kenney_rpg-audio/Audio/footstep02.ogg` | `audio/kenney_rpg-audio/Audio/footstep02.ogg` | ENEMY | 0.24 | 20 | 180 | off |
| ENEMY_UNDEAD_REACTION | `asset_sources/audio/kenney_interface-sounds/Audio/click_001.ogg` | `audio/kenney_interface-sounds/Audio/click_001.ogg` | ENEMY | 0.24 | 20 | 180 | off |
| ENEMY_TANK_REACTION | `asset_sources/audio/kenney_impact-sounds/Audio/footstep_grass_001.ogg` | `audio/kenney_impact-sounds/Audio/footstep_grass_001.ogg` | ENEMY | 0.24 | 20 | 180 | off |
| ENEMY_LEGACY_CHARGE | `asset_sources/audio/kenney_impact-sounds/Audio/impactBell_heavy_000.ogg` | `audio/kenney_impact-sounds/Audio/impactBell_heavy_000.ogg` | ENEMY | 0.24 | 20 | 180 | off |
| PLAYER_HURT | `asset_sources/audio/kenney_interface-sounds/Audio/error_007.ogg` | `audio/kenney_interface-sounds/Audio/error_007.ogg` | CRITICAL | 0.65 | 100 | 0 | off |
| PLAYER_DEATH | `asset_sources/audio/kenney_interface-sounds/Audio/error_004.ogg` | `audio/kenney_interface-sounds/Audio/error_004.ogg` | CRITICAL | 0.65 | 100 | 0 | off |

## 36 runtime file duy nhất

| Runtime file | Type | Duration s | PCM16 bytes nếu decode hết |
|---|---|---:|---:|
| `audio/end_dungeon.mp3` | Music streaming | 165.564 | 29,205,504 |
| `audio/heavy_boss_battle_1_bpm200.ogg` | Music streaming | 108.000 | 19,051,200 |
| `audio/hope_orchestral_battle_music_bpm165.ogg` | Music streaming | 69.818 | 12,315,928 |
| `audio/kenney_impact-sounds/Audio/footstep_concrete_003.ogg` | Sound | 0.110 | 19,492 |
| `audio/kenney_impact-sounds/Audio/footstep_grass_001.ogg` | Sound | 0.674 | 118,824 |
| `audio/kenney_impact-sounds/Audio/impactBell_heavy_000.ogg` | Sound | 1.480 | 261,104 |
| `audio/kenney_impact-sounds/Audio/impactPlate_medium_004.ogg` | Sound | 0.534 | 94,252 |
| `audio/kenney_impact-sounds/Audio/impactPunch_medium_003.ogg` | Sound | 0.455 | 80,308 |
| `audio/kenney_impact-sounds/Audio/impactPunch_medium_004.ogg` | Sound | 0.543 | 95,772 |
| `audio/kenney_interface-sounds/Audio/back_003.ogg` | Sound | 0.093 | 8,192 |
| `audio/kenney_interface-sounds/Audio/bong_001.ogg` | Sound | 0.123 | 10,834 |
| `audio/kenney_interface-sounds/Audio/click_001.ogg` | Sound | 0.100 | 8,822 |
| `audio/kenney_interface-sounds/Audio/close_002.ogg` | Sound | 0.314 | 27,676 |
| `audio/kenney_interface-sounds/Audio/confirmation_001.ogg` | Sound | 0.290 | 25,564 |
| `audio/kenney_interface-sounds/Audio/confirmation_003.ogg` | Sound | 0.322 | 28,402 |
| `audio/kenney_interface-sounds/Audio/confirmation_004.ogg` | Sound | 0.490 | 43,254 |
| `audio/kenney_interface-sounds/Audio/error_003.ogg` | Sound | 0.533 | 94,104 |
| `audio/kenney_interface-sounds/Audio/error_004.ogg` | Sound | 0.103 | 18,116 |
| `audio/kenney_interface-sounds/Audio/error_007.ogg` | Sound | 0.192 | 16,934 |
| `audio/kenney_interface-sounds/Audio/glass_002.ogg` | Sound | 0.125 | 11,052 |
| `audio/kenney_interface-sounds/Audio/glass_005.ogg` | Sound | 0.111 | 9,772 |
| `audio/kenney_interface-sounds/Audio/maximize_001.ogg` | Sound | 0.258 | 22,790 |
| `audio/kenney_interface-sounds/Audio/maximize_005.ogg` | Sound | 0.526 | 46,426 |
| `audio/kenney_interface-sounds/Audio/maximize_007.ogg` | Sound | 0.186 | 16,384 |
| `audio/kenney_interface-sounds/Audio/minimize_001.ogg` | Sound | 0.258 | 22,790 |
| `audio/kenney_interface-sounds/Audio/minimize_005.ogg` | Sound | 0.526 | 46,426 |
| `audio/kenney_interface-sounds/Audio/open_001.ogg` | Sound | 0.148 | 13,034 |
| `audio/kenney_interface-sounds/Audio/question_001.ogg` | Sound | 0.491 | 43,278 |
| `audio/kenney_interface-sounds/Audio/scratch_001.ogg` | Sound | 0.139 | 12,288 |
| `audio/kenney_interface-sounds/Audio/scratch_005.ogg` | Sound | 0.325 | 28,672 |
| `audio/kenney_interface-sounds/Audio/scroll_002.ogg` | Sound | 1.000 | 88,202 |
| `audio/kenney_interface-sounds/Audio/select_002.ogg` | Sound | 0.043 | 7,636 |
| `audio/kenney_rpg-audio/Audio/chop.ogg` | Sound | 0.240 | 46,128 |
| `audio/kenney_rpg-audio/Audio/dropLeather.ogg` | Sound | 0.415 | 79,696 |
| `audio/kenney_rpg-audio/Audio/footstep02.ogg` | Sound | 0.242 | 46,492 |
| `audio/kenney_rpg-audio/Audio/knifeSlice2.ogg` | Sound | 0.569 | 109,208 |

## License / provenance

Giữ nguyên ba License.txt Kenney tại [assets/audio/licenses](assets/audio/licenses). [CREDITS.md](assets/audio/licenses/CREDITS.md) giữ Kenney/CC0 và credit/URL từng bài: HydroGene — End Dungeon; MintoDog — Heavy Boss Battle 1 và Hope. BGM provenance kế thừa audit trước: NTFS Zone.Identifier/tags và trang nguồn đã xác minh riêng. Impact Sounds có License.txt local CC0; không suy license từ tên thư mục. Không tải lại hoặc tuyên bố đối chiếu hash remote.

## Loop / định dạng

Nguồn đã chọn: 35 Vorbis OGG + 1 MP3; decode toàn bộ 36 file lại ở task này. Các SFX dưới 1MiB PCM/file. BGM không decode toàn bộ thành Sound; chỉ 3 Music được chuẩn bị trước. Menu/Day/Night/Mini/Final loop; Victory/GameOver one-shot. Mini/Final dùng chung Heavy, đổi state không stop/seek/restart. Menu/Night chung MP3 nhưng native player được release trước khi libGDX tạo audio.

`end_dungeon.mp3` có encoder delay/padding; `setLooping(true)` không chứng minh gapless. OGG trên backend Android cũng có thể có gap. Kiểm tra API/state không thay được nghe seam thực tế. Không tự cắt intro/end hoặc dùng `battle_music_01-loop.ogg` chưa được chọn. Xem [libGDX streaming music](https://libgdx.com/wiki/audio/streaming-music).

Bằng chứng decode, metadata và hash: `core/build/reports/task05_1b/{asset-validation.json,probe-input.json,decoded.json,decode.log}`. Tái kiểm tra: `python tools/import_selected_audio.py --check`.
