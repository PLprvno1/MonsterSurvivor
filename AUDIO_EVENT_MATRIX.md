# Audio Event Matrix — Task 05.1A

Đường dẫn tính từ `asset_sources/audio/`. Candidate A đứng đầu chỉ là đề xuất ưu tiên để **nghe duyệt**, không phải lựa chọn đã chốt. Không quá3 file/event. Confidence MEDIUM dựa tên + metadata + mô tả nguồn; LOW là proxy cần nghiêm túc so sánh/nghe, không phải đã nghe. Không có confidence HIGH về âm sắc. Priority P0=ưu tiên cốt lõi, P1=thông thường, P2=phụ/tương lai.

`REAL`=gameplay/UI có thật, nhưng **chưa có hook audio**. `REAL_DAMAGE_ROUTING_PLANNED`=damage có thật, chưa có group audio riêng. `PLANNED_NO_GAMEPLAY`/`PLANNED_WITH_ANIMATION_ONLY` không được tích hợp trước khi có cơ chế thật. Không hook vào frame render/clip. Dash/Ultimate/Evolution chưa tồn tại; Special của phần lớn82 loài là clip/metadata, ngoại lệ Boss và legacy charge đã có logic thật.

## Đề xuất theo sự kiện

### BGM

| Event ID | Sự kiện | File đề xuất | Duration | Priority | Confidence | Cần nghe duyệt | Lý do / giới hạn |
| --- | --- | --- | --- | --- | --- | --- | --- |
| BGM_MENU | Menu | A: [`end_dungeon.mp3`](asset_sources/audio/end_dungeon.mp3) | A: 165.564s | P2 | LOW | Có | Tái dùng dungeon theme ở gain thấp; chưa xác nhận độ dịu hoặc hợp menu. Không suy từ chữ end rằng đây là nhạc kết thúc. |
| BGM_DAY | Day Gameplay | A: [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg)<br>B: [`battle_music_01-loop.ogg`](asset_sources/audio/battle_music_01-loop.ogg) | A: 69.818s<br>B: 152.000s | P1 | MEDIUM | Có | Hope được tác giả mô tả sáng, orchestral; Battle là phương án chiptune nếu chọn phong cách retro nhất quán. |
| BGM_NIGHT | Night Gameplay | A: [`end_dungeon.mp3`](asset_sources/audio/end_dungeon.mp3) | A: 165.564s | P1 | LOW | Có | Tác giả mô tả dungeon theme, có thể hợp không khí đêm; MP3 có rủi ro gap. |
| BGM_MINI | Mini Boss | A: [`battle_music_01-loop.ogg`](asset_sources/audio/battle_music_01-loop.ogg)<br>B: [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg) | A: 152.000s<br>B: 69.818s | P1 | MEDIUM | Có | Battle là RPG battle/chiptune, cần xử lý intro/loop về khoảng7,5s; Hope là fallback loop toàn bài dễ thử hơn. |
| BGM_FINAL | Lich Lord | A: [`heavy_boss_battle_1_bpm200.ogg`](asset_sources/audio/heavy_boss_battle_1_bpm200.ogg) | A: 108.000s | P0 | MEDIUM | Có | Nguồn tác giả mô tả boss/metal/dark và loopable; chỉ là lựa chọn sơ bộ, chưa nghe mức phù hợp pixel fantasy. |
| BGM_VICTORY | Victory | A: [`hope_orchestral_battle_music_bpm165.ogg`](asset_sources/audio/hope_orchestral_battle_music_bpm165.ogg) | A: 69.818s | P2 | MEDIUM | Có | Tái dùng bài sáng sau Victory, loop off hoặc tiếp tục nhẹ; đây là BGM dài, không phải jingle ngắn. |
| BGM_GAME_OVER | Game Over | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Đề xuất dừng BGM + cue kết quả ngắn; không bắt buộc mua/tìm thêm một bài nhạc riêng. |

### Weapons

| Event ID | Sự kiện | File đề xuất | Duration | Priority | Confidence | Cần nghe duyệt | Lý do / giới hạn |
| --- | --- | --- | --- | --- | --- | --- | --- |
| WEAPON_SWORD_SWING | Sword swing | A: [`kenney_rpg-audio/Audio/knifeSlice.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice.ogg)<br>B: [`kenney_rpg-audio/Audio/knifeSlice2.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/knifeSlice2.ogg) | A: 0.600s<br>B: 0.569s | P0 | MEDIUM | Có | Tên slice hợp động tác chém; A có khoảng50ms đầu/đuôi im lặng, B có sample sát full scale, cần nghe so A/B. |
| WEAPON_SWORD_HIT | Sword hit | A: [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg) | A: 0.240s | P0 | MEDIUM | Có | Impact chém vật lý dựa tên chop; có sample full scale cần nghe/gain headroom. |
| WEAPON_BOW_RELEASE | Bow release | A: [`kenney_interface-sounds/Audio/pluck_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_002.ogg)<br>B: [`kenney_interface-sounds/Audio/pluck_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/pluck_001.ogg) | A: 0.165s<br>B: 0.102s | P1 | LOW | Có | Pluck chỉ là proxy dây bật từ bộ interface, chưa xác nhận tiếng cung; A/B đều có seam/peak cần duyệt, có quyền từ chối cả hai. |
| WEAPON_ARROW_HIT | Arrow hit | A: [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg)<br>B: [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) | A: 0.240s<br>B: 0.415s | P1 | LOW | Có | Tái dùng impact vật lý/chạm vật mềm, không khẳng định là tiếng mũi tên. |
| WEAPON_FIRE_CAST | Fireball cast | **MISSING** | — | P0 | N/A | Cần quyết định thiếu/im lặng | Không thấy file được nhận diện là fire/whoosh/magic cast; không dùng tiếng cửa hay click để lấp bảng. |
| WEAPON_FIRE_EXPLOSION | Fireball explosion | **MISSING** | — | P0 | N/A | Cần quyết định thiếu/im lặng | Cần impact/explosion ngắn có lực; bộ hiện có là UI/foley, chưa có ứng viên đủ cơ sở. |
| WEAPON_LIGHTNING_ACTIVATE | Lightning activation | **MISSING** | — | P0 | N/A | Cần quyết định thiếu/im lặng | Không có zap/electric/magic đã nhận diện; glitch không mặc nhiên là sét. |
| WEAPON_LIGHTNING_IMPACT | Lightning impact | **MISSING** | — | P1 | N/A | Cần quyết định thiếu/im lặng | Có thể dùng cùng asset điện với activation sau khi nghe và duyệt gain/pitch; hiện chưa có. |

### Pickups

| Event ID | Sự kiện | File đề xuất | Duration | Priority | Confidence | Cần nghe duyệt | Lý do / giới hạn |
| --- | --- | --- | --- | --- | --- | --- | --- |
| PICKUP_DAMAGE | Damage Potion | A: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg)<br>B: [`kenney_interface-sounds/Audio/glass_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_003.ogg) | A: 0.125s<br>B: 0.124s | P1 | MEDIUM | Có | Cue glass ngắn hợp vật phẩm bình; cùng nhóm với Health/Speed để tiết kiệm voice. |
| PICKUP_HEALTH | Health Potion / Max HP | A: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg)<br>B: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) | A: 0.125s<br>B: 0.322s | P1 | MEDIUM | Có | Glass ngắn hoặc confirmation cho hồi/tăng HP; không cần hai file cho hai nhánh. |
| PICKUP_SPEED | Speed Potion | A: [`kenney_interface-sounds/Audio/glass_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_003.ogg)<br>B: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) | A: 0.124s<br>B: 0.125s | P1 | MEDIUM | Có | Dùng chung palette glass; chỉ phân biệt bằng variant nếu người dùng nghe thấy khác biệt hữu ích. |
| PICKUP_HEALTH_DROP | Health Drop | A: [`kenney_interface-sounds/Audio/glass_005.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_005.ogg)<br>B: [`kenney_interface-sounds/Audio/glass_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/glass_002.ogg) | A: 0.111s<br>B: 0.125s | P1 | MEDIUM | Có | Cue ngắn, định gain thấp hơn potion; tránh chuỗi heal lấn combat. |
| PICKUP_WEAPON_OPEN | Weapon Box mở modal | A: [`kenney_interface-sounds/Audio/open_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_001.ogg)<br>B: [`kenney_rpg-audio/Audio/metalLatch.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalLatch.ogg) | A: 0.148s<br>B: 0.261s | P0 | MEDIUM | Có | Open là cue UI rõ chức năng; metalLatch là proxy rương có full-scale samples cần duyệt. |
| PICKUP_WEAPON_SELECTED | Chọn vũ khí mới | A: [`kenney_interface-sounds/Audio/confirmation_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg)<br>B: [`kenney_interface-sounds/Audio/select_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_001.ogg) | A: 0.290s<br>B: 0.043s | P0 | MEDIUM | Có | Cue commit ngắn, chỉ chọn một trong hai, không chồng thêm generic confirm cùng thao tác. |
| PICKUP_WEAPON_UPGRADED | Nâng cấp vũ khí | A: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg)<br>B: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) | A: 0.322s<br>B: 0.258s | P0 | MEDIUM | Có | Positive confirmation hoặc maximize ngắn, dùng chung với selected nếu muốn palette nhỏ. |

### Boss

| Event ID | Sự kiện | File đề xuất | Duration | Priority | Confidence | Cần nghe duyệt | Lý do / giới hạn |
| --- | --- | --- | --- | --- | --- | --- | --- |
| MINI_APPEARS | Mini Boss xuất hiện | A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg)<br>B: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) | A: 0.123s<br>B: 0.258s | P0 | LOW | Có | Cue cảnh báo/nhấn mạnh UI tạm, chưa có roar hoặc fanfare chuyên dụng. |
| MINI_SLAM_WARNING | Mini slam warning | A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) | A: 0.123s | P0 | LOW | Có | Một tone ngắn làm tín hiệu báo trước; visual vẫn là telegraph chính, không loop tone mỗi frame. |
| MINI_SLAM_IMPACT | Mini slam impact | **MISSING** | — | P0 | N/A | Cần quyết định thiếu/im lặng | Cần thump/stone/heavy impact ngắn; không mặc nhiên coi doorClose/metalPot là slam. |
| MINI_DEFEATED | Mini defeated | A: [`kenney_interface-sounds/Audio/confirmation_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_002.ogg)<br>B: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) | A: 0.539s<br>B: 0.322s | P1 | MEDIUM | Có | Cue positive ngắn, không cần một bản nhạc thắng riêng. |
| LICH_APPEARS | Lich Lord xuất hiện | A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg)<br>B: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg) | A: 0.123s<br>B: 0.258s | P0 | LOW | Có | Có thể tái dùng cảnh báo Mini, khác biệt chính bằng BGM Heavy và gain; chưa có boss entrance roar. |
| LICH_PROJECTILE | Lich projectile / fan | **MISSING** | — | P1 | N/A | Cần quyết định thiếu/im lặng | Có thể dùng chung magic/electric release với Lightning, nhưng hiện chưa có file đúng vai trò. |
| LICH_AOE_WARNING | Lich AoE warning | A: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) | A: 0.123s | P0 | LOW | Có | Dùng cùng một warning palette, ưu tiên audible/readable hơn nhiều cue mới. |
| LICH_AOE_IMPACT | Lich AoE impact | **MISSING** | — | P0 | N/A | Cần quyết định thiếu/im lặng | Có thể chia sẻ impact với Fireball/Mini slam sau duyệt, chưa có candidate đủ cơ sở. |
| LICH_PHASE2 | Lich Phase 2 | A: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg)<br>B: [`kenney_interface-sounds/Audio/bong_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/bong_001.ogg) | A: 0.258s<br>B: 0.123s | P0 | LOW | Có | Cue transition UI tạm; giữ cùng BGM, không restart nhạc mỗi frame phase2. |
| LICH_DEFEATED | Lich defeated | A: [`kenney_interface-sounds/Audio/confirmation_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg) | A: 0.490s | P0 | MEDIUM | Có | Cue dứt điểm ngắn; tránh chồng cùng cue Victory sau Death0,67s. |

### UI

| Event ID | Sự kiện | File đề xuất | Duration | Priority | Confidence | Cần nghe duyệt | Lý do / giới hạn |
| --- | --- | --- | --- | --- | --- | --- | --- |
| UI_TAP | Button tap | A: [`kenney_interface-sounds/Audio/click_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_001.ogg)<br>B: [`kenney_interface-sounds/Audio/click_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/click_002.ogg) | A: 0.100s<br>B: 0.010s | P0 | MEDIUM | Có | Tên click và thời lượng ngắn hợp UI; chưa nghe chọn màu sắc cuối. |
| UI_NAVIGATION | Navigation / đổi lựa chọn | A: [`kenney_interface-sounds/Audio/select_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_001.ogg)<br>B: [`kenney_interface-sounds/Audio/select_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/select_002.ogg) | A: 0.043s<br>B: 0.043s | P1 | MEDIUM | Có | Select ngắn cho mode/difficulty/tab; không phát do loadValues/recreate tự đặt lựa chọn. |
| UI_CONFIRM | Confirm | A: [`kenney_interface-sounds/Audio/confirmation_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_001.ogg)<br>B: [`kenney_interface-sounds/Audio/confirmation_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_003.ogg) | A: 0.290s<br>B: 0.322s | P1 | MEDIUM | Có | Confirmation positive, dùng thay click khi commit quan trọng. |
| UI_CANCEL | Back / Cancel | A: [`kenney_interface-sounds/Audio/back_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_001.ogg)<br>B: [`kenney_interface-sounds/Audio/back_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_002.ogg) | A: 0.064s<br>B: 0.070s | P1 | MEDIUM | Có | Nhóm back đúng chức năng, có thể dùng cả toolbar và hardware Back nếu xử lý thành công. |
| UI_PAUSE | Pause | A: [`kenney_interface-sounds/Audio/minimize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/minimize_001.ogg)<br>B: [`kenney_interface-sounds/Audio/back_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/back_001.ogg) | A: 0.258s<br>B: 0.064s | P1 | MEDIUM | Có | Minimize/back làm cue đóng hành động; chỉ cho user pause, không cho lifecycle tự pause. |
| UI_RESUME | Resume | A: [`kenney_interface-sounds/Audio/maximize_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/maximize_001.ogg)<br>B: [`kenney_interface-sounds/Audio/open_001.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/open_001.ogg) | A: 0.258s<br>B: 0.148s | P1 | MEDIUM | Có | Maximize/open làm cue trở lại; không tự phát khi Activity.onResume nhưng game vẫn chờ Resume. |
| UI_GAME_OVER | Game Over | A: [`kenney_interface-sounds/Audio/error_003.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_003.ogg)<br>B: [`kenney_interface-sounds/Audio/error_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/error_004.ogg) | A: 0.533s<br>B: 0.103s | P1 | LOW | Có | Negative UI cue tạm thay losing sting; chưa biết có hợp cảm xúc kết trận, cần nghe. |
| UI_VICTORY | Victory | A: [`kenney_interface-sounds/Audio/confirmation_004.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_004.ogg)<br>B: [`kenney_interface-sounds/Audio/confirmation_002.ogg`](asset_sources/audio/kenney_interface-sounds/Audio/confirmation_002.ogg) | A: 0.490s<br>B: 0.539s | P1 | MEDIUM | Có | Cue positive ngắn; có thể chỉ phát LICH_DEFEATED và để Victory không thêm cue nhằm tránh lặp. |

### Enemy

| Event ID | Sự kiện | File đề xuất | Duration | Priority | Confidence | Cần nghe duyệt | Lý do / giới hạn |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ENEMY_HIT | Enemy nhận damage | A: [`kenney_rpg-audio/Audio/chop.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/chop.ogg)<br>B: [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) | A: 0.240s<br>B: 0.415s | P1 | LOW | Có | Impact chung vật lý, tái dùng với weapon hit và chọn một owner phát để tránh double sound. |
| ENEMY_DEATH | Enemy death chung | A: [`kenney_rpg-audio/Audio/dropLeather.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/dropLeather.ogg) | A: 0.415s | P1 | LOW | Có | Proxy body/drop foley, không phải vocal đã được nghe xác nhận; có thể bỏ nếu weapon impact đã đủ rõ. |
| ENEMY_FLYING_REACTION | Flying reaction riêng | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Chưa thấy wing/insect/flying reaction phù hợp; generic hit/death có thể dùng trong demo. |
| ENEMY_BEAST_REACTION | Beast reaction riêng | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Chưa có grunt/beast reaction đã nhận diện, không cần mỗi loài một file. |
| ENEMY_UNDEAD_REACTION | Undead reaction riêng | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Chưa có bone/undead vocal được xác nhận. |
| ENEMY_TANK_REACTION | Tank / armor reaction | A: [`kenney_rpg-audio/Audio/metalClick.ogg`](asset_sources/audio/kenney_rpg-audio/Audio/metalClick.ogg) | A: 0.446s | P2 | LOW | Có | Metal foley có thể hợp armor, nhưng RMS thấp và không đại diện mọi tank đá/cây. |
| ENEMY_LEGACY_CHARGE | Legacy boar charge | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Có speed burst thật cho legacy_charge nhưng chưa có charge/roar riêng; không cần bổ sung để hoàn thành demo. |
| PLAYER_HURT | Player hurt | **MISSING** | — | P1 | N/A | Cần quyết định thiếu/im lặng | Thiếu cue đau/va chạm riêng rõ ràng; vũ khí/Boss impact hiện là fallback, tránh dùng UI error tùy tiện. |
| PLAYER_DEATH | Player death | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Có thể dùng GameOver cue sau animation thay voice riêng; không bắt buộc vocal trong demo. |

### Future

| Event ID | Sự kiện | File đề xuất | Duration | Priority | Confidence | Cần nghe duyệt | Lý do / giới hạn |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FUTURE_DASH | Dash | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Chưa có gameplay Dash; clip/catalog có tên tương tự không tạo thành action thật. |
| FUTURE_ULTIMATE | Ultimate | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Chưa có gameplay Ultimate; chưa thể chọn âm theo cơ chế/timing chưa định nghĩa. |
| FUTURE_EVOLUTION | Weapon Evolution | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Level1–5 là upgrade hiện có; Evolution là tính năng khác chưa triển khai. |
| FUTURE_ENEMY_SPECIAL | Enemy special skills mở rộng | **MISSING** | — | P2 | N/A | Cần quyết định thiếu/im lặng | Nhiều loài chỉ có special metadata/animation; ngoài Boss và legacy charge chưa có đủ skill gameplay. |

## Source anchor và trạng thái thật

| Event ID | Trạng thái | Điểm phát đề xuất sau duyệt |
| --- | --- | --- |
| BGM_MENU | REAL | HomeActivity.onResume/GameMode/Settings là Native; chưa có Gdx menu/audio host. |
| BGM_DAY | REAL | GameSession RUNNING, !night.active(), không Boss ACTIVE. |
| BGM_NIGHT | REAL | NightState.active() đổi false→true tại progress300; Mini có ưu tiên cao hơn. |
| BGM_MINI | REAL | BossSystem.miniState SPAWNING→ACTIVE; không đợi Mini chết mới cho Final. |
| BGM_FINAL | REAL | BossSystem.finalState SPAWNING→ACTIVE; giữ cùng bài qua phase2. |
| BGM_VICTORY | REAL | GameSession.finish(true) sau Death Lich; không phát tại progress600. |
| BGM_GAME_OVER | REAL | GameSession.finish(false); phân biệt DYING với GAME_OVER. |
| WEAPON_SWORD_SWING | REAL | SwordCombat.update khi Player.startAttack(...) trả true; một lần/swing, không theo frame animation. |
| WEAPON_SWORD_HIT | REAL | Nhánh hit trong SwordCombat sau range/sector/LOS; gộp một cue/swing, tách HP delta khỏi kill boolean. |
| WEAPON_BOW_RELEASE | REAL | WeaponSystem.update khi BOW fired=true; không phát khi target retry hoặc không còn projectile slot. |
| WEAPON_ARROW_HIT | REAL | ProjectileSystem.update nhánh BOW target hit; piercing cần cooldown theo mục tiêu/global, không một âm cho mọi frame chạm. |
| WEAPON_FIRE_CAST | REAL | WeaponSystem.volley fired=true, một âm cho cả fan activation. |
| WEAPON_FIRE_EXPLOSION | REAL | ProjectileSystem.explode một lần/projectile; gộp nhiều lane gần nhau, không phát theo số nạn nhân. |
| WEAPON_LIGHTNING_ACTIVATE | REAL | WeaponSystem.chain một cue/activation, không theo mỗi effect segment. |
| WEAPON_LIGHTNING_IMPACT | REAL | Điểm target.damage trong chain; coalesce chain targets, không suy HP damage từ lightningHits counter. |
| PICKUP_DAMAGE | REAL | PickupSystem.apply nhánh DAMAGE_BOX thành công; Speed đã cap fallback thành Damage dùng cùng cue hiệu lực. |
| PICKUP_HEALTH | REAL | PickupSystem.apply HEALTH_BOX sau heal/maxHP thực sự thành công. |
| PICKUP_SPEED | REAL | PickupSystem.apply SPEED_BOX tăng speed thực; không phát Speed khi fallback Damage. |
| PICKUP_HEALTH_DROP | REAL | PickupSystem.apply HEALTH_DROP khi healed>0; full HP trả false thì không phát. |
| PICKUP_WEAPON_OPEN | REAL | GameSession.weaponBox khi phase RUNNING→CHOOSING_WEAPON và selection token mới, không đợi collect. |
| PICKUP_WEAPON_SELECTED | REAL | GameSession.chooseWeapon sau offer.apply và commit thành công; WEAPON offer fromLevel=0. |
| PICKUP_WEAPON_UPGRADED | REAL | chooseWeapon thành công, WeaponOffer WEAPON fromLevel>0; stat fallback không phải nâng cấp vũ khí. |
| MINI_APPEARS | REAL | Mini event SPAWNING→ACTIVE; không phát ở mỗi retry SPAWNING hoặc HUD notice redraw. |
| MINI_SLAM_WARNING | REAL | BossSystem.begin Cast.SLAM một lần trước windup0,95/1,10s. |
| MINI_SLAM_IMPACT | REAL | BossSystem.finish SLAM lúc castRemaining qua0, kể cả người chơi né được. |
| MINI_DEFEATED | REAL | BossSystem.onDeath Mini một lần; không hook noticeSerial vì notice Mini có thể bị ưu tiên Final che. |
| LICH_APPEARS | REAL | Final SPAWNING→ACTIVE; một cue, ưu tiên hơn Mini nếu hai event cùng tick. |
| LICH_PROJECTILE | REAL | BossSystem.finish BOLT khi ít nhất một BossProjectiles.fire thành công; một âm/fan, không5 âm. |
| LICH_AOE_WARNING | REAL | BossSystem.begin AREA chốt vị trí player, chỉ phát khi cast bắt đầu. |
| LICH_AOE_IMPACT | REAL | BossSystem.finish AREA sau windup; impact không phụ thuộc player có bị hit. |
| LICH_PHASE2 | REAL | BossSystem.prepare tại phase1→2 đúng một lần, không dựa theo Entrance frame. |
| LICH_DEFEATED | REAL | BossSystem.onDeath Final một lần; ưu tiên thay generic enemy death, BGM chuyển theo WINNING/VICTORY. |
| UI_TAP | REAL | Native UiUtils.animateClick/direct click callbacks và libGDX GameInput accepted action; không emit ở mọi touchDown. |
| UI_NAVIGATION | REAL | GameModeActivity selection listeners, Native navigation; GameInput accepted action. |
| UI_CONFIRM | REAL | Callback thành công của thao tác UI; weapon commit đã có cue riêng thì bỏ generic confirm. |
| UI_CANCEL | REAL | Native toolbar/back và GameInput Commands; không lặp khi touch/keyboard cùng route. |
| UI_PAUSE | REAL | GameScreen.togglePause userPaused false→true; UI bus còn được phép phát trong pause. |
| UI_RESUME | REAL | GameScreen.togglePause userPaused true→false sau thao tác người dùng. |
| UI_GAME_OVER | REAL | GameSession.finish(false) một lần sau Player Death animation; không replay theo render. |
| UI_VICTORY | REAL | GameSession.finish(true) sau Lich Death; không phát khi chạm600. |
| ENEMY_HIT | REAL | Enemy.damage: HP sau < trước; return boolean hiện chỉ báo chết, false vẫn có thể là nonlethal hit. |
| ENEMY_DEATH | REAL | Fanout death hook đã có của GameSession; không thay EnemySystem.onDeath làm mất kills/drop handler. |
| ENEMY_FLYING_REACTION | REAL_DAMAGE_ROUTING_PLANNED | Damage/death thật đã có; routing theo definition.flying mới chỉ là kế hoạch. |
| ENEMY_BEAST_REACTION | REAL_DAMAGE_ROUTING_PLANNED | Damage/death thật; curated species/family→audio group chưa được triển khai. |
| ENEMY_UNDEAD_REACTION | REAL_DAMAGE_ROUTING_PLANNED | Damage/death thật; không suy loài undead chỉ từ clip SPECIAL. |
| ENEMY_TANK_REACTION | REAL_DAMAGE_ROUTING_PLANNED | Enemy.damage HP delta; chỉ tag armor đã duyệt, không dùng toàn bộ archetype TANK. |
| ENEMY_LEGACY_CHARGE | REAL | EnemySystem.steer khi chargeTime0→0,7s; chỉ các definition.charge hiện có. |
| PLAYER_HURT | REAL | Player.damage trả true và hp>0, immunity0,7s chặn mọi hit thất bại. |
| PLAYER_DEATH | REAL | Player.damage thành công và hp==0; khác GameSession.finish(false). |
| FUTURE_DASH | PLANNED_NO_GAMEPLAY | Không có hook gameplay; chỉ lập backlog sau khi action được triển khai. |
| FUTURE_ULTIMATE | PLANNED_NO_GAMEPLAY | Không có hook hiện tại. |
| FUTURE_EVOLUTION | PLANNED_NO_GAMEPLAY | Không hook WeaponOffer hiện tại rồi gọi đó là evolution. |
| FUTURE_ENEMY_SPECIAL | PLANNED_WITH_ANIMATION_ONLY | Catalog SPECIAL playback chỉ là ANIMATION_ONLY; cần skill start/impact events thật trong task tương lai. |

## Quy tắc tích hợp được đề xuất, chưa sửa code

- `Enemy.damage` trả true khi **chết**, không phải mọi hit thành công; Boss còn có burst guard. Cue hurt phải dựa HP giảm; counter `hits/lightningHits` không tự chứng minh HP giảm. Projectile impact vật lý có thể vẫn hợp lệ khi burst guard chặn HP.
- Chỉ một nơi phát damage/death cue: chia sẻ/fanout hook `GameSession` đang giữ kill/drop, không đăng ký listener mới ghi đè `EnemySystem.onDeath`. Boss death thay generic enemy death, không chồng cả hai.
- Không gọi thêm `Player.takeSwingHit()` chỉ để lấy âm thanh vì sẽ tiêu thụ flag gameplay. Emission tương lai nằm tại nơi logic đã nhận sự kiện.
- Fireball explosion và Lightning chain gộp theo activation/impact cluster; không phát theo mỗi nạn nhân, mỗi effect frame hoặc mỗi fan lane. Phase2 Lich5 đạn chỉ một release cue.
- `WeaponBox` mở modal trước khi item được collect; dùng phase/token mới để mở cue một lần. Chọn reward chỉ phát sau commit thành công; token sai/double tap không phát. Không xếp chồng tap+confirm+selected+pickup cho một lần chọn.
- Health Drop full HP không collect nên không phát; Speed đã cap fallback Damage phải dùng cue theo reward thực. UI text/notice đã localized không phải nguồn sự kiện âm thanh.
- BGM/night/Boss priority được tính từ trạng thái cuối tick; không xếp hàng nhạc Night rồi Mini cùng progress300. Final cao hơn Mini; phase2 không restart cùng bản nhạc.
- Demo chỉ nhân progress, **không** pitch/tốc độ nhạc, SFX, cooldown âm hoặc fade. Pause/modal dừng bus gameplay; UI Resume/selection còn dùng được. Lifecycle pause không phát cue Pause của người chơi.

Tham khảo lifecycle/routing/voice budget đầy đủ trong [báo cáo](TASK_05_1A_AUDIO_AUDIT_REPORT.md). Quyết định âm sắc vẫn đợi chủ dự án nghe.
