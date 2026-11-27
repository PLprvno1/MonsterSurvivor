# Audio Event Integration Status — Task 05.1B

Ngày: 15/11/2026. 49 mapping có điểm gọi gắn với hành vi hiện có. Không có event trong JSON bị bỏ lại chỉ vì tên asset khác gameplay. Điều này **không có nghĩa đã nghe đủ 49 cue** hoặc đã quan sát đủ 49 event trên Android.

Android telemetry retained: **42/49 ID** có log playback API/music state. Cột “chưa ghi nhận” chỉ là thiếu coverage Android, không đồng nghĩa thiếu code trigger. Sound.play ID hoặc MediaPlayer state không chứng minh âm thanh đã nghe được. CPU kiểm tra tín hiệu thật bằng recording sink; GL smoke còn gọi trực tiếp toàn bộ 42 SFX để kiểm backend, không dùng vòng lặp đó làm bằng chứng gameplay trigger.

| Event | Runtime trigger / guard | Android API/state retained |
|---|---|---|
| BGM_MENU | NativeAudio.resume/applyMusic: native Activity foreground + AudioFocus + preferences | Có |
| BGM_DAY | GameAudioManager.musicFor: running session, no active boss, before Night | Có |
| BGM_NIGHT | musicFor: Night active, no active Mini/Final | Có |
| BGM_MINI | musicFor: Mini ACTIVE, Final not active | Có |
| BGM_FINAL | musicFor: Final ACTIVE / WINNING; same Mini asset keeps playback position | Có |
| BGM_VICTORY | musicFor: actual GameSession.VICTORY after Lich death animation | Có |
| BGM_GAME_OVER | musicFor: actual GameSession.GAME_OVER after player death animation | Có |
| WEAPON_SWORD_SWING | SwordCombat.update: startAttack returns true | Có |
| WEAPON_SWORD_HIT | SwordCombat.update: at least one HP decrease in committed swing, once per swing | Có |
| WEAPON_BOW_RELEASE | WeaponSystem.update: successful BOW shoot | Có |
| WEAPON_ARROW_HIT | ProjectileSystem.update: real target HP decrease | Có |
| WEAPON_FIRE_CAST | WeaponSystem.update: successful FIREBALL volley, once per activation | Có |
| WEAPON_FIRE_EXPLOSION | ProjectileSystem.explode: actual detonation; not per victim | Có |
| WEAPON_LIGHTNING_ACTIVATE | WeaponSystem.chain: one activation | Có |
| WEAPON_LIGHTNING_IMPACT | WeaponSystem.chain: one cue if any chain target loses HP | Có |
| PICKUP_DAMAGE | PickupSystem.apply DAMAGE_BOX, including capped-Speed fallback; committed stat fallback offer | Chưa ghi nhận; kiểm tra thủ công |
| PICKUP_HEALTH | PickupSystem.apply HEALTH_BOX; committed MAX_HP fallback offer | Chưa ghi nhận; kiểm tra thủ công |
| PICKUP_SPEED | PickupSystem.apply SPEED_BOX; committed SPEED fallback offer | Chưa ghi nhận; kiểm tra thủ công |
| PICKUP_HEALTH_DROP | PickupSystem.apply HEALTH_DROP only after actual healing succeeds | Chưa ghi nhận; kiểm tra thủ công |
| PICKUP_WEAPON_OPEN | GameSession.weaponBox: valid selection.open -> CHOOSING_WEAPON | Chưa ghi nhận; kiểm tra thủ công |
| PICKUP_WEAPON_SELECTED | GameSession.chooseWeapon: successful commit + collected item, fromLevel=0 | Chưa ghi nhận; kiểm tra thủ công |
| PICKUP_WEAPON_UPGRADED | GameSession.chooseWeapon: successful commit + collected item, fromLevel>0 | Chưa ghi nhận; kiểm tra thủ công |
| MINI_APPEARS | BossSystem.prepare: SPAWNING -> ACTIVE after placement success | Có |
| MINI_SLAM_WARNING | BossSystem.begin: Cast.SLAM accepted | Có |
| MINI_SLAM_IMPACT | BossSystem.finish: live actor completes SLAM, including a dodged impact | Có |
| MINI_DEFEATED | BossSystem.onDeath/dead: first Mini death; before announcement priority filtering | Có |
| LICH_APPEARS | BossSystem.prepare: Final SPAWNING -> ACTIVE | Có |
| LICH_PROJECTILE | BossSystem.finish BOLT: at least one BossProjectiles.fire succeeds; once per fan | Có |
| LICH_AOE_WARNING | BossSystem.begin: Cast.AREA accepted | Có |
| LICH_AOE_IMPACT | BossSystem.finish: live actor completes AREA | Có |
| LICH_PHASE2 | BossSystem.prepare: phase 1 -> 2 only | Có |
| LICH_DEFEATED | BossSystem.onDeath/dead: first Final death | Có |
| UI_TAP | UiUtils.animateClick: accepted animation callback; single action guard | Có |
| UI_NAVIGATION | GameModeActivity: user changes difficulty/mode; same selection is silent | Có |
| UI_CONFIRM | GameModeActivity: Start; GameScreen.retry; Settings slider release preview | Có |
| UI_CANCEL | NativeAudioActivity OnBackPressedDispatcher; accepted native toolbar Back | Có |
| UI_PAUSE | GameScreen.togglePause: explicit user pause; excludes background/focus loss | Có |
| UI_RESUME | GameScreen.togglePause: explicit user Resume | Có |
| UI_GAME_OVER | GameSession.finish(false), once per session | Có |
| UI_VICTORY | GameSession.finish(true), once per session | Có |
| ENEMY_HIT | Enemy.damage: real nonlethal damage on generic ordinary species | Có |
| ENEMY_DEATH | Enemy.damage: first death of an ordinary enemy; bosses excluded | Có |
| ENEMY_FLYING_REACTION | Enemy.damage: nonlethal damage to flying species, excluding curated undead families | Có |
| ENEMY_BEAST_REACTION | Enemy.damage: nonlethal boar/wolf/hound/beast/fox/cat/owlbear/jackalope group, excluding undead | Có |
| ENEMY_UNDEAD_REACTION | Enemy.damage: nonlethal undead/skeleton/zombie/bone/mummy/wraith/skull group | Có |
| ENEMY_TANK_REACTION | Enemy.damage: nonlethal remaining nonflying TANK archetype | Có |
| ENEMY_LEGACY_CHARGE | EnemySystem.steer: actual legacy_charge burst starts; other special metadata ignored | Có |
| PLAYER_HURT | Player.damage: accepted positive damage, HP remains positive, immunity checked first | Có |
| PLAYER_DEATH | Player.damage: first accepted transition to zero HP; no later hurt | Có |

## Phạm vi test / event chưa có gameplay

`AudioChecks`: fake backend/clock để kiểm load dedup, cap/eviction, settings, fail-safe; recording sink cho weapon, HP delta/immunity, từng nhóm enemy, legacy charge, pickup fallback, modal commit/stale token/double tap, Boss cast/phase/death và terminal state thật. `AudioSmoke`: libGDX/OpenAL thật và GameScreen lifecycle/modal/Retry/Home. Native UI được kiểm tra trên Android. Các thao tác UI khác nhau dùng cue phù hợp, weapon commit không có generic UI tap/confirm chồng lên.

Không thêm Dash, Ultimate, Evolution hoặc kỹ năng Ranged/Special chưa triển khai. Bốn FUTURE_* của audit cũ **không nằm trong 49 selections** và không có runtime trigger. ENEMY_*_REACTION ở đây là feedback của damage thật; không giả lập special skill theo animation. ENEMY_LEGACY_CHARGE chỉ gọi ở burst đã tồn tại.

Các log Android first-play chỉ ghi một lần cho SFX thường; critical cues ghi mỗi successful play kèm count ở source cuối. Kiểm chứng once-per-action còn dựa trên recording-sink assertions và state/token guards, không suy từ một dòng log first-play.
