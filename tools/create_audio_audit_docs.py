"""Create audit documents and an offline, user-controlled audition list from measured source files.
Planning data only. This module is not imported by the game and never plays or edits source audio.
"""
from pathlib import Path
from collections import Counter
import html,json,math

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'core/build/reports/task05_1a'
AUDIT=json.loads((OUT/'audio-audit.json').read_text(encoding='utf-8'))
FILES={row['path']:row for row in AUDIT['files']}
EVENTS=[]
def I(name):return 'kenney_interface-sounds/Audio/'+name+'.ogg'
def R(name):return 'kenney_rpg-audio/Audio/'+name+'.ogg'
def add(id,group,label,files,reason,hook,priority='P1',confidence='MEDIUM',status='REAL',missing=None):
    assert len(files)<=3
    for name in files:assert name in FILES,name
    EVENTS.append(dict(id=id,group=group,label=label,candidates=files,reason=reason,hook=hook,priority=priority,confidence=confidence if files else 'N/A',status=status,missing=missing))

BATTLE='battle_music_01-loop.ogg';DUNGEON='end_dungeon.mp3';HEAVY='heavy_boss_battle_1_bpm200.ogg';HOPE='hope_orchestral_battle_music_bpm165.ogg'
add('BGM_MENU','BGM','Menu',[DUNGEON],'Tái dùng dungeon theme ở gain thấp; chưa xác nhận độ dịu hoặc hợp menu. Không suy từ chữ end rằng đây là nhạc kết thúc.','HomeActivity.onResume/GameMode/Settings là Native; chưa có Gdx menu/audio host.', 'P2','LOW')
add('BGM_DAY','BGM','Day Gameplay',[HOPE,BATTLE],'Hope được tác giả mô tả sáng, orchestral; Battle là phương án chiptune nếu chọn phong cách retro nhất quán.','GameSession RUNNING, !night.active(), không Boss ACTIVE.', 'P1')
add('BGM_NIGHT','BGM','Night Gameplay',[DUNGEON],'Tác giả mô tả dungeon theme, có thể hợp không khí đêm; MP3 có rủi ro gap.','NightState.active() đổi false→true tại progress300; Mini có ưu tiên cao hơn.', 'P1','LOW')
add('BGM_MINI','BGM','Mini Boss',[BATTLE,HOPE],'Battle là RPG battle/chiptune, cần xử lý intro/loop về khoảng7,5s; Hope là fallback loop toàn bài dễ thử hơn.','BossSystem.miniState SPAWNING→ACTIVE; không đợi Mini chết mới cho Final.', 'P1')
add('BGM_FINAL','BGM','Lich Lord',[HEAVY],'Nguồn tác giả mô tả boss/metal/dark và loopable; chỉ là lựa chọn sơ bộ, chưa nghe mức phù hợp pixel fantasy.','BossSystem.finalState SPAWNING→ACTIVE; giữ cùng bài qua phase2.', 'P0')
add('BGM_VICTORY','BGM','Victory',[HOPE],'Tái dùng bài sáng sau Victory, loop off hoặc tiếp tục nhẹ; đây là BGM dài, không phải jingle ngắn.','GameSession.finish(true) sau Death Lich; không phát tại progress600.', 'P2')
add('BGM_GAME_OVER','BGM','Game Over',[],'Đề xuất dừng BGM + cue kết quả ngắn; không bắt buộc mua/tìm thêm một bài nhạc riêng.','GameSession.finish(false); phân biệt DYING với GAME_OVER.', 'P2',missing='OPTIONAL')

add('WEAPON_SWORD_SWING','Weapons','Sword swing',[R('knifeSlice'),R('knifeSlice2')],'Tên slice hợp động tác chém; A có khoảng50ms đầu/đuôi im lặng, B có sample sát full scale, cần nghe so A/B.','SwordCombat.update khi Player.startAttack(...) trả true; một lần/swing, không theo frame animation.', 'P0')
add('WEAPON_SWORD_HIT','Weapons','Sword hit',[R('chop')],'Impact chém vật lý dựa tên chop; có sample full scale cần nghe/gain headroom.','Nhánh hit trong SwordCombat sau range/sector/LOS; gộp một cue/swing, tách HP delta khỏi kill boolean.', 'P0')
add('WEAPON_BOW_RELEASE','Weapons','Bow release',[I('pluck_002'),I('pluck_001')],'Pluck chỉ là proxy dây bật từ bộ interface, chưa xác nhận tiếng cung; A/B đều có seam/peak cần duyệt, có quyền từ chối cả hai.','WeaponSystem.update khi BOW fired=true; không phát khi target retry hoặc không còn projectile slot.', 'P1','LOW',missing='RECOMMENDED_IF_REJECTED')
add('WEAPON_ARROW_HIT','Weapons','Arrow hit',[R('chop'),R('dropLeather')],'Tái dùng impact vật lý/chạm vật mềm, không khẳng định là tiếng mũi tên.','ProjectileSystem.update nhánh BOW target hit; piercing cần cooldown theo mục tiêu/global, không một âm cho mọi frame chạm.', 'P1','LOW')
add('WEAPON_FIRE_CAST','Weapons','Fireball cast',[],'Không thấy file được nhận diện là fire/whoosh/magic cast; không dùng tiếng cửa hay click để lấp bảng.','WeaponSystem.volley fired=true, một âm cho cả fan activation.', 'P0',missing='REQUIRED')
add('WEAPON_FIRE_EXPLOSION','Weapons','Fireball explosion',[],'Cần impact/explosion ngắn có lực; bộ hiện có là UI/foley, chưa có ứng viên đủ cơ sở.','ProjectileSystem.explode một lần/projectile; gộp nhiều lane gần nhau, không phát theo số nạn nhân.', 'P0',missing='REQUIRED')
add('WEAPON_LIGHTNING_ACTIVATE','Weapons','Lightning activation',[],'Không có zap/electric/magic đã nhận diện; glitch không mặc nhiên là sét.','WeaponSystem.chain một cue/activation, không theo mỗi effect segment.', 'P0',missing='REQUIRED')
add('WEAPON_LIGHTNING_IMPACT','Weapons','Lightning impact',[],'Có thể dùng cùng asset điện với activation sau khi nghe và duyệt gain/pitch; hiện chưa có.','Điểm target.damage trong chain; coalesce chain targets, không suy HP damage từ lightningHits counter.', 'P1',missing='REQUIRED')

add('PICKUP_DAMAGE','Pickups','Damage Potion',[I('glass_002'),I('glass_003')],'Cue glass ngắn hợp vật phẩm bình; cùng nhóm với Health/Speed để tiết kiệm voice.','PickupSystem.apply nhánh DAMAGE_BOX thành công; Speed đã cap fallback thành Damage dùng cùng cue hiệu lực.', 'P1')
add('PICKUP_HEALTH','Pickups','Health Potion / Max HP',[I('glass_002'),I('confirmation_003')],'Glass ngắn hoặc confirmation cho hồi/tăng HP; không cần hai file cho hai nhánh.','PickupSystem.apply HEALTH_BOX sau heal/maxHP thực sự thành công.', 'P1')
add('PICKUP_SPEED','Pickups','Speed Potion',[I('glass_003'),I('glass_002')],'Dùng chung palette glass; chỉ phân biệt bằng variant nếu người dùng nghe thấy khác biệt hữu ích.','PickupSystem.apply SPEED_BOX tăng speed thực; không phát Speed khi fallback Damage.', 'P1')
add('PICKUP_HEALTH_DROP','Pickups','Health Drop',[I('glass_005'),I('glass_002')],'Cue ngắn, định gain thấp hơn potion; tránh chuỗi heal lấn combat.','PickupSystem.apply HEALTH_DROP khi healed>0; full HP trả false thì không phát.', 'P1')
add('PICKUP_WEAPON_OPEN','Pickups','Weapon Box mở modal',[I('open_001'),R('metalLatch')],'Open là cue UI rõ chức năng; metalLatch là proxy rương có full-scale samples cần duyệt.','GameSession.weaponBox khi phase RUNNING→CHOOSING_WEAPON và selection token mới, không đợi collect.', 'P0')
add('PICKUP_WEAPON_SELECTED','Pickups','Chọn vũ khí mới',[I('confirmation_001'),I('select_001')],'Cue commit ngắn, chỉ chọn một trong hai, không chồng thêm generic confirm cùng thao tác.','GameSession.chooseWeapon sau offer.apply và commit thành công; WEAPON offer fromLevel=0.', 'P0')
add('PICKUP_WEAPON_UPGRADED','Pickups','Nâng cấp vũ khí',[I('confirmation_003'),I('maximize_001')],'Positive confirmation hoặc maximize ngắn, dùng chung với selected nếu muốn palette nhỏ.','chooseWeapon thành công, WeaponOffer WEAPON fromLevel>0; stat fallback không phải nâng cấp vũ khí.', 'P0')

add('MINI_APPEARS','Boss','Mini Boss xuất hiện',[I('bong_001'),I('maximize_001')],'Cue cảnh báo/nhấn mạnh UI tạm, chưa có roar hoặc fanfare chuyên dụng.','Mini event SPAWNING→ACTIVE; không phát ở mỗi retry SPAWNING hoặc HUD notice redraw.', 'P0','LOW')
add('MINI_SLAM_WARNING','Boss','Mini slam warning',[I('bong_001')],'Một tone ngắn làm tín hiệu báo trước; visual vẫn là telegraph chính, không loop tone mỗi frame.','BossSystem.begin Cast.SLAM một lần trước windup0,95/1,10s.', 'P0','LOW')
add('MINI_SLAM_IMPACT','Boss','Mini slam impact',[],'Cần thump/stone/heavy impact ngắn; không mặc nhiên coi doorClose/metalPot là slam.','BossSystem.finish SLAM lúc castRemaining qua0, kể cả người chơi né được.', 'P0',missing='REQUIRED')
add('MINI_DEFEATED','Boss','Mini defeated',[I('confirmation_002'),I('confirmation_003')],'Cue positive ngắn, không cần một bản nhạc thắng riêng.','BossSystem.onDeath Mini một lần; không hook noticeSerial vì notice Mini có thể bị ưu tiên Final che.', 'P1')
add('LICH_APPEARS','Boss','Lich Lord xuất hiện',[I('bong_001'),I('maximize_001')],'Có thể tái dùng cảnh báo Mini, khác biệt chính bằng BGM Heavy và gain; chưa có boss entrance roar.','Final SPAWNING→ACTIVE; một cue, ưu tiên hơn Mini nếu hai event cùng tick.', 'P0','LOW')
add('LICH_PROJECTILE','Boss','Lich projectile / fan',[],'Có thể dùng chung magic/electric release với Lightning, nhưng hiện chưa có file đúng vai trò.','BossSystem.finish BOLT khi ít nhất một BossProjectiles.fire thành công; một âm/fan, không5 âm.', 'P1',missing='REQUIRED')
add('LICH_AOE_WARNING','Boss','Lich AoE warning',[I('bong_001')],'Dùng cùng một warning palette, ưu tiên audible/readable hơn nhiều cue mới.','BossSystem.begin AREA chốt vị trí player, chỉ phát khi cast bắt đầu.', 'P0','LOW')
add('LICH_AOE_IMPACT','Boss','Lich AoE impact',[],'Có thể chia sẻ impact với Fireball/Mini slam sau duyệt, chưa có candidate đủ cơ sở.','BossSystem.finish AREA sau windup; impact không phụ thuộc player có bị hit.', 'P0',missing='REQUIRED')
add('LICH_PHASE2','Boss','Lich Phase 2',[I('maximize_001'),I('bong_001')],'Cue transition UI tạm; giữ cùng BGM, không restart nhạc mỗi frame phase2.','BossSystem.prepare tại phase1→2 đúng một lần, không dựa theo Entrance frame.', 'P0','LOW')
add('LICH_DEFEATED','Boss','Lich defeated',[I('confirmation_004')],'Cue dứt điểm ngắn; tránh chồng cùng cue Victory sau Death0,67s.','BossSystem.onDeath Final một lần; ưu tiên thay generic enemy death, BGM chuyển theo WINNING/VICTORY.', 'P0')

add('UI_TAP','UI','Button tap',[I('click_001'),I('click_002')],'Tên click và thời lượng ngắn hợp UI; chưa nghe chọn màu sắc cuối.','Native UiUtils.animateClick/direct click callbacks và libGDX GameInput accepted action; không emit ở mọi touchDown.', 'P0')
add('UI_NAVIGATION','UI','Navigation / đổi lựa chọn',[I('select_001'),I('select_002')],'Select ngắn cho mode/difficulty/tab; không phát do loadValues/recreate tự đặt lựa chọn.','GameModeActivity selection listeners, Native navigation; GameInput accepted action.', 'P1')
add('UI_CONFIRM','UI','Confirm',[I('confirmation_001'),I('confirmation_003')],'Confirmation positive, dùng thay click khi commit quan trọng.','Callback thành công của thao tác UI; weapon commit đã có cue riêng thì bỏ generic confirm.', 'P1')
add('UI_CANCEL','UI','Back / Cancel',[I('back_001'),I('back_002')],'Nhóm back đúng chức năng, có thể dùng cả toolbar và hardware Back nếu xử lý thành công.','Native toolbar/back và GameInput Commands; không lặp khi touch/keyboard cùng route.', 'P1')
add('UI_PAUSE','UI','Pause',[I('minimize_001'),I('back_001')],'Minimize/back làm cue đóng hành động; chỉ cho user pause, không cho lifecycle tự pause.','GameScreen.togglePause userPaused false→true; UI bus còn được phép phát trong pause.', 'P1')
add('UI_RESUME','UI','Resume',[I('maximize_001'),I('open_001')],'Maximize/open làm cue trở lại; không tự phát khi Activity.onResume nhưng game vẫn chờ Resume.','GameScreen.togglePause userPaused true→false sau thao tác người dùng.', 'P1')
add('UI_GAME_OVER','UI','Game Over',[I('error_003'),I('error_004')],'Negative UI cue tạm thay losing sting; chưa biết có hợp cảm xúc kết trận, cần nghe.','GameSession.finish(false) một lần sau Player Death animation; không replay theo render.', 'P1','LOW')
add('UI_VICTORY','UI','Victory',[I('confirmation_004'),I('confirmation_002')],'Cue positive ngắn; có thể chỉ phát LICH_DEFEATED và để Victory không thêm cue nhằm tránh lặp.','GameSession.finish(true) sau Lich Death; không phát khi chạm600.', 'P1')

add('ENEMY_HIT','Enemy','Enemy nhận damage',[R('chop'),R('dropLeather')],'Impact chung vật lý, tái dùng với weapon hit và chọn một owner phát để tránh double sound.','Enemy.damage: HP sau < trước; return boolean hiện chỉ báo chết, false vẫn có thể là nonlethal hit.', 'P1','LOW')
add('ENEMY_DEATH','Enemy','Enemy death chung',[R('dropLeather')],'Proxy body/drop foley, không phải vocal đã được nghe xác nhận; có thể bỏ nếu weapon impact đã đủ rõ.','Fanout death hook đã có của GameSession; không thay EnemySystem.onDeath làm mất kills/drop handler.', 'P1','LOW')
add('ENEMY_FLYING_REACTION','Enemy','Flying reaction riêng',[],'Chưa thấy wing/insect/flying reaction phù hợp; generic hit/death có thể dùng trong demo.','Damage/death thật đã có; routing theo definition.flying mới chỉ là kế hoạch.', 'P2',status='REAL_DAMAGE_ROUTING_PLANNED',missing='OPTIONAL')
add('ENEMY_BEAST_REACTION','Enemy','Beast reaction riêng',[],'Chưa có grunt/beast reaction đã nhận diện, không cần mỗi loài một file.','Damage/death thật; curated species/family→audio group chưa được triển khai.', 'P2',status='REAL_DAMAGE_ROUTING_PLANNED',missing='OPTIONAL')
add('ENEMY_UNDEAD_REACTION','Enemy','Undead reaction riêng',[],'Chưa có bone/undead vocal được xác nhận.','Damage/death thật; không suy loài undead chỉ từ clip SPECIAL.', 'P2',status='REAL_DAMAGE_ROUTING_PLANNED',missing='OPTIONAL')
add('ENEMY_TANK_REACTION','Enemy','Tank / armor reaction',[R('metalClick')],'Metal foley có thể hợp armor, nhưng RMS thấp và không đại diện mọi tank đá/cây.','Enemy.damage HP delta; chỉ tag armor đã duyệt, không dùng toàn bộ archetype TANK.', 'P2','LOW',status='REAL_DAMAGE_ROUTING_PLANNED')
add('ENEMY_LEGACY_CHARGE','Enemy','Legacy boar charge',[],'Có speed burst thật cho legacy_charge nhưng chưa có charge/roar riêng; không cần bổ sung để hoàn thành demo.','EnemySystem.steer khi chargeTime0→0,7s; chỉ các definition.charge hiện có.', 'P2',missing='OPTIONAL')
add('PLAYER_HURT','Enemy','Player hurt',[],'Thiếu cue đau/va chạm riêng rõ ràng; vũ khí/Boss impact hiện là fallback, tránh dùng UI error tùy tiện.','Player.damage trả true và hp>0, immunity0,7s chặn mọi hit thất bại.', 'P1',missing='RECOMMENDED')
add('PLAYER_DEATH','Enemy','Player death',[],'Có thể dùng GameOver cue sau animation thay voice riêng; không bắt buộc vocal trong demo.','Player.damage thành công và hp==0; khác GameSession.finish(false).', 'P2',missing='OPTIONAL')

add('FUTURE_DASH','Future','Dash',[],'Chưa có gameplay Dash; clip/catalog có tên tương tự không tạo thành action thật.','Không có hook gameplay; chỉ lập backlog sau khi action được triển khai.', 'P2',status='PLANNED_NO_GAMEPLAY',missing='OPTIONAL')
add('FUTURE_ULTIMATE','Future','Ultimate',[],'Chưa có gameplay Ultimate; chưa thể chọn âm theo cơ chế/timing chưa định nghĩa.','Không có hook hiện tại.', 'P2',status='PLANNED_NO_GAMEPLAY',missing='OPTIONAL')
add('FUTURE_EVOLUTION','Future','Weapon Evolution',[],'Level1–5 là upgrade hiện có; Evolution là tính năng khác chưa triển khai.','Không hook WeaponOffer hiện tại rồi gọi đó là evolution.', 'P2',status='PLANNED_NO_GAMEPLAY',missing='OPTIONAL')
add('FUTURE_ENEMY_SPECIAL','Future','Enemy special skills mở rộng',[],'Nhiều loài chỉ có special metadata/animation; ngoài Boss và legacy charge chưa có đủ skill gameplay.','Catalog SPECIAL playback chỉ là ANIMATION_ONLY; cần skill start/impact events thật trong task tương lai.', 'P2',status='PLANNED_WITH_ANIMATION_ONLY',missing='OPTIONAL')

SOURCES=[
 dict(group='Kenney Interface Sounds',license='CC0-1.0',status='VERIFIED_LOCAL_AND_OFFICIAL',author='Kenney',url='https://kenney.nl/assets/interface-sounds',local='kenney_interface-sounds/License.txt',basis='License local ghi Interface Sounds1.0/11-02-2020; trang chính thức100 files/CC0.'),
 dict(group='Kenney RPG Audio',license='CC0-1.0',status='VERIFIED_LOCAL_AND_OFFICIAL',author='Kenney',url='https://kenney.nl/assets/rpg-audio',local='kenney_rpg-audio/License.txt',basis='License local RPG Audio; trang chính thức50 files, thực tế51 SFX + Preview, không tự xóa file chênh lệch.'),
 dict(group=BATTLE,license='CC0',status='VERIFIED_SOURCE_PAGE',author='pmiller',url='https://opengameart.org/content/chiptune-battle-music',local=None,basis='Zone.Identifier trỏ đúng bài/file; trang tác giả chỉ định lặp từ khoảng7,5s.'),
 dict(group=DUNGEON,license='CC0',status='VERIFIED_SOURCE_PAGE',author='HydroGene',url='https://opengameart.org/content/jrpg-end-dungeon',local=None,basis='Zone.Identifier host end_dungeon_0.mp3, tên local end_dungeon.mp3; trang ghi dungeon theme/loopable.'),
 dict(group=HEAVY,license='CC0',status='VERIFIED_SOURCE_PAGE',author='MintoDog',url='https://opengameart.org/content/heavy-boss-battle-1',local=None,basis='Zone.Identifier host có hậu tố_0; tags TITLE/ARTIST/BPM khớp; trang ghi boss/metal/loopable.'),
 dict(group=HOPE,license='CC0',status='VERIFIED_SOURCE_PAGE',author='MintoDog',url='https://opengameart.org/content/hopeorchestral-battle-music',local=None,basis='Zone.Identifier và tags khớp; trang tác giả mô tả bright/orchestral/loopable.'),
]
def fmt(value,n=3):return 'N/A' if value is None else f'{value:.{n}f}'
def duration(path):return fmt(FILES[path]['duration'])+'s'
def link(path):return f'[`{path}`](asset_sources/audio/{path.replace(" ","%20")})'
def table(headers,rows):return '| '+' | '.join(headers)+' |\n| '+' | '.join('---' for _ in headers)+' |\n'+''.join('| '+' | '.join(str(x).replace('|','/') for x in row)+' |\n' for row in rows)
def family(row):
    name=Path(row['path']).stem
    if row['class']!='SFX':return row['class']
    if 'interface' in row['path']:return 'UI / '+name.split('_')[0]
    if name.startswith('footstep'):return 'SFX foley / footsteps (không gắn tự động)'
    if name.startswith(('knife','drawKnife','chop')):return 'SFX foley / blade candidate'
    if name.startswith(('metal','door')):return 'SFX foley / metal, mechanism'
    if name.startswith('book'):return 'SFX foley / book'
    return 'SFX foley / cloth, leather, coins, creak'

def inventory():
    s=AUDIT['summary'];text=f'''# Audio Asset Inventory — Task 05.1A

Ngày audit:14/11/2026. Mọi đường dẫn audio trong tài liệu tính từ **`asset_sources/audio/`**, giữ nguyên chữ hoa, dấu gạch nối và extension thật. Không đổi tên, chỉnh, chuyển hay đóng gói file nguồn.

## Phạm vi và phương pháp

Quét đệ quy **{s['totalFiles']} file**: **{s['audioFiles']} audio =4 BGM +151 SFX +1 Preview**, cùng6 License/URL. Format155 Ogg Vorbis +1 MP3. Audio nén tổng{s['audioBytes']:,} bytes ({s['audioBytes']/1048576:.3f}MiB), duration tổng{s['audioSeconds']:.3f}s. BGM495,382s; SFX55,344s; Preview26,127s. Không có file trùng SHA-256.

Không có ffprobe/ffmpeg trong PATH. `tools/audit_audio_assets.py` + `tools/audio/AudioProbe.java` dùng Java17 và decoder **đã cache của project**: libGDX1.14.2/JOrbis0.0.17, JLayer1.0.1-gdx, LWJGL BufferUtils. Đọc Vorbis header/comment/granule, kiểm CRC/sequence/EOS từng Ogg page và **decode hết156 file thành PCM trong bộ nhớ**; không phát âm thanh hoặc ghi file audio. MP3 duration là số frame PCM decoded, có thể chứa encoder delay/padding. Ogg duration lấy granule/rate, được so với decoder. Decode OK không phải kiểm thử MediaPlayer/SoundPool Android.

**Chưa nghe trực tiếp, chưa đo LUFS/true peak/phổ hoặc xác nhận musical seam.** PCM peak/RMS và endpoint jump không chứng minh cảm giác âm thanh, clipping gốc hoặc loop liền mạch. Mọi lựa chọn cần người dùng nghe duyệt. Công cụ không normalize/transcode/trim.

## License và provenance

'''
    text+=table(['Nguồn','Tác giả','License / trạng thái','Bằng chứng'],[(x['group'],x['author'],x['license']+' / '+x['status'],x['basis']+' [Nguồn chính thức]('+x['url']+')'+(' / `'+x['local']+'`' if x['local'] else '')) for x in SOURCES])
    text+='''
Bốn bài được đối chiếu riêng từ URL ReferrerUrl/HostUrl trong **NTFS Zone.Identifier** của chính file, cộng trang OpenGameArt tương ứng; không suy tất cả OpenGameArt là CC0. Một số lần open trả403, nội dung trang chính thức vẫn có trong kết quả tìm kiếm của chính domain. Không tải lại asset hoặc xác minh byte/hash với file remote. Tags artist/year/BPM chỉ bổ sung provenance; không tự là giấy phép. Credit tùy chọn nên ghi Kenney, pmiller, HydroGene, MintoDog và URL tương ứng. Không có LICENSE_UNVERIFIED trong tập hiện tại; file mới thiếu nguồn phải dùng trạng thái đó, không kế thừa license của hàng xóm.

Tên thư mục thực tế là **`kenney_rpg-audio`**, không phải `kenney_rpg_audio`. Website ghi50 sound, local có51 trong Audio và1 Preview; audit giữ số thực tế, không coi chênh lệch là lỗi decode. Không có README; hai License local vẫn đủ thông tin Kenney. Preview không phải một BGM thứ năm và không đưa vào runtime.

## Từng file: định dạng, metadata và khả năng sử dụng

Bitrate dưới đây là **kbit/s trung bình toàn file nén**, gồm container/header, không phải nominal bitrate của Vorbis. Nominal/tags/SHA/provenance có trong [JSON](core/build/reports/task05_1a/audio-audit.json); nominal Vorbis thay đổi theo encoder/pack, không suy từ bitrate trung bình. MP3 frame đầu128kbit/s, không thấy thay đổi bitrate. Tất cả decode OK, Ogg CRC/sequence/EOS không có lỗi. Một số decoder Ogg trả thêm padding dưới23ms ngoài granule; phép đo vùng sample/lead/tail ở đây chặn đúng granule của nguồn, không ghi padding thành asset bị lỗi.

Loop: `ONE_SHOT`=SFX một lần, không có cơ sở thành loop; `AUTHOR_7.5`=tác giả yêu cầu loop-start xấp xỉ7,5s; `AUTHOR_LOOP`=tác giả ghi loopable nhưng chưa nghe/backend test; `MP3_RISK`=author-loopable nhưng có delay/padding/gap; `EXCLUDE`=Preview. Cột vấn đề chỉ là flag review, xem định nghĩa sau bảng.

'''
    labels={'ONE_SHOT_NOT_AUTHORED_LOOP':'ONE_SHOT','AUTHOR_LOOP_START_APPROX_7_5S_REVIEW':'AUTHOR_7.5','AUTHOR_LOOPABLE_MP3_GAP_RISK':'MP3_RISK','AUTHOR_LOOPABLE_BACKEND_AUDITION_REQUIRED':'AUTHOR_LOOP','PACK_PREVIEW_EXCLUDE':'EXCLUDE'}
    text+=table(['Đường dẫn / filename thật','Phân loại','Format/codec','Bytes','Duration s','Hz','Ch','kbit/s avg','Decode','Loop','Review'],[(link(r['path']),family(r),r['format']+'/'+r['codec'],r['bytes'],fmt(r['duration']),r['sampleRate'],r['channels'],fmt(r['fileAverageBitrate']/1000,1),r['pcm']['decode'],labels.get(r['loopAssessment'],'UNVERIFIED'),', '.join(r['issues']) or '—') for r in AUDIT['files']])
    text+='''
## Từng file: quality measurements

Peak và RMS tính trên PCM16 decoded (không weighting). Silence: frame có peak tất cả channel≤32/32768, khoảng−60dBFS. FS samples đếm |sample|≥32767; codec có thể overshoot/clamp, **không tự kết luận file hỏng hoặc clipping nghe được**. Endpoint là |last−first|/32768 lớn nhất giữa các channel; chỉ là kiểm tra nối **end→0**, không xác minh seam end→7,5s của chiptune. PCM KiB là kích thước raw16-bit tương đương sample nguồn nếu nạp hết, chưa tính padding/overhead backend.

'''
    text+=table(['File','Peak dBFS','RMS dBFS','Lead ms','Tail ms','FS samples','Endpoint jump','PCM KiB'],[(link(r['path']),fmt(r['pcm'].get('samplePeakDbfs'),2),fmt(r['pcm'].get('rmsDbfs'),2),fmt(r['pcm']['leadingSilenceSeconds']*1000,1),fmt(r['pcm']['trailingSilenceSeconds']*1000,1),r['pcm']['fullScaleSamples'],fmt(r['pcm']['endpointJump'],4),fmt(r['pcm']['pcm16Bytes']/1024,1)) for r in AUDIT['files']])
    counts=Counter(i for r in AUDIT['files'] for i in r['issues'])
    text+='\n### Ý nghĩa flag và kết quả đáng chú ý\n\n'
    descriptions={'PCM_FULL_SCALE_REVIEW':'Có sample đạt full scale trên PCM16; nghe kiểm tra headroom, không tự sửa nguồn.','LEAD_GT_30MS':'Đầu file yên hơn−60dBFS quá30ms, có thể làm SFX phản hồi chậm.','TAIL_GT_100MS':'Đuôi yên quá100ms; chưa tự trim, không tính thời lượng này như năng lượng âm.','LOW_RMS_REVIEW':'RMS toàn file dưới−40dBFS; có thể nhỏ hoặc nhiều khoảng lặng, chưa phải LUFS.','DC_OFFSET_REVIEW':'Mean PCM có |DC|>0,01; cần nghe/đo sâu trước quyết định chỉnh derivative.','ENDPOINT_JUMP_REVIEW':'Endpoint end→0 lệch>0,1; không nên tự loop SFX đó.','MUSIC_NOT_SOUND_SIZE':'PCM>1MiB: dùng streaming Music; đây là quyết định routing, không phải lỗi file.'}
    text+=table(['Flag','Số file','Đánh giá'],[(key,counts[key],value) for key,value in descriptions.items()])
    text+='''
- `drawKnife1/2` RMS−44,69/−41,35dBFS; đối chiếu UI confirmation khoảng−11dBFS cho thấy không thể cùng gain1 mà kỳ vọng cùng độ rõ. Khoảng34,67dB của RMS toàn tập SFX chưa phải chênh lệch loudness nghe được.
- `select_006.ogg` có khoảng1,69s đuôi yên; các `switch_*.ogg` có đuôi khoảng0,3–0,48s. Không chọn variant dài chỉ vì cùng prefix; nghe lại trước khi chọn.
- `knifeSlice.ogg` có lead~50ms; các `metalPot` có full-scale samples và một số lead~49–64ms. `knifeSlice2/chop` cũng cần nghe peak. Không coi các flag này là file hỏng.
- `battle_music_01-loop.ogg` có28 FS samples, DC mean~0,01178 và tác giả chỉ định lặp từ~7,5s. `end_dungeon.mp3` có tổng lead/tail khoảng40ms ở ngưỡng đã chọn; endpoint0 không chứng minh loop không gap.
- Mono77 file, stereo79 file. 48kHz stereo52 file thuộc RPG (bao gồm Preview), còn104 file44,1kHz. Không có lý do tự downmix/resample trong audit.

## Sáu file không phải audio

'''
    text+=table(['Đường dẫn','Bytes','Loại'],[(r['path'],r['bytes'],'License' if r['path'].endswith('License.txt') else 'Windows Internet Shortcut; không phát âm thanh') for r in AUDIT['ancillary']])
    text+='\nDữ liệu đầy đủ: [CSV](core/build/reports/task05_1a/audio-audit.csv), [JSON](core/build/reports/task05_1a/audio-audit.json). Tái hiện: `python tools/audit_audio_assets.py`. Không cần Android Studio Run cho audit.\n'
    (ROOT/'AUDIO_ASSET_INVENTORY.md').write_text(text,encoding='utf-8')

def matrix_and_checklist():
    text='''# Audio Event Matrix — Task 05.1A

Đường dẫn tính từ `asset_sources/audio/`. Candidate A đứng đầu chỉ là đề xuất ưu tiên để **nghe duyệt**, không phải lựa chọn đã chốt. Không quá3 file/event. Confidence MEDIUM dựa tên + metadata + mô tả nguồn; LOW là proxy cần nghiêm túc so sánh/nghe, không phải đã nghe. Không có confidence HIGH về âm sắc. Priority P0=ưu tiên cốt lõi, P1=thông thường, P2=phụ/tương lai.

`REAL`=gameplay/UI có thật, nhưng **chưa có hook audio**. `REAL_DAMAGE_ROUTING_PLANNED`=damage có thật, chưa có group audio riêng. `PLANNED_NO_GAMEPLAY`/`PLANNED_WITH_ANIMATION_ONLY` không được tích hợp trước khi có cơ chế thật. Không hook vào frame render/clip. Dash/Ultimate/Evolution chưa tồn tại; Special của phần lớn82 loài là clip/metadata, ngoại lệ Boss và legacy charge đã có logic thật.

## Đề xuất theo sự kiện

'''
    for group in ['BGM','Weapons','Pickups','Boss','UI','Enemy','Future']:
        text+='### '+group+'\n\n'
        rows=[]
        for event in EVENTS:
            if event['group']!=group:continue
            names='<br>'.join(chr(65+i)+': '+link(path) for i,path in enumerate(event['candidates'])) or '**MISSING**'
            times='<br>'.join(chr(65+i)+': '+duration(path) for i,path in enumerate(event['candidates'])) or '—'
            rows.append((event['id'],event['label'],names,times,event['priority'],event['confidence'],'Có' if event['candidates'] else 'Cần quyết định thiếu/im lặng',event['reason']))
        text+=table(['Event ID','Sự kiện','File đề xuất','Duration','Priority','Confidence','Cần nghe duyệt','Lý do / giới hạn'],rows)+'\n'
    text+='## Source anchor và trạng thái thật\n\n'
    text+=table(['Event ID','Trạng thái','Điểm phát đề xuất sau duyệt'],[(e['id'],e['status'],e['hook']) for e in EVENTS])
    text+='''
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
'''
    (ROOT/'AUDIO_EVENT_MATRIX.md').write_text(text,encoding='utf-8')
    text='''# Audio Selection Checklist — Chủ dự án nghe và chọn

Mở [AUDIO_AUDITION.html](AUDIO_AUDITION.html) bằng trình duyệt desktop để nghe từng candidate; chỉ một player, không autoplay/preload hàng loạt. Có lọc nhóm và xuất lựa chọn JSON. File này cũng có link mở file trực tiếp nếu trình duyệt không decode được. **Chưa có mục nào được tự động duyệt.** HTML không tự sửa checklist này và không ghi vào asset.

Mọi đường dẫn bên dưới tính từ `asset_sources/audio/`. Tất cả đề xuất dựa tên/metadata/mô tả tác giả, chưa nghe trực tiếp. Nghe ở âm lượng thiết bị vừa phải, đặc biệt file có flag peak; so A/B cùng ngữ cảnh trước khi chốt. Có thể chọn một file chung cho nhiều event; không cần tải thêm nếu palette hiện có đủ.

Mỗi mục: chọn A/B/C nếu hợp; dùng Đồng ý để duyệt candidate đã đánh dấu, Thay nếu muốn file khác, hoặc giữ Chưa chọn. Các ô là lựa chọn người dùng, không phải kết quả audit. MISSING không có file để mở, không tự tạo candidate.

'''
    index=0
    for group in ['BGM','Weapons','Pickups','Boss','UI','Enemy']:
        text+='## '+group+'\n\n'
        for event in EVENTS:
            if event['group']!=group:continue
            index+=1;text+=f'### {index:02d}. {event["label"]} — `{event["id"]}`\n\n{event["reason"]}\n\n'
            for i,path in enumerate(event['candidates']):
                row=FILES[path];text+=f'- [ ] Chọn {chr(65+i)}: {link(path)} — **{duration(path)}**; review: '+(', '.join(row['issues']) or 'không có flag vượt ngưỡng')+'.\n'
            if not event['candidates']:text+='**MISSING** — '+('chọn im lặng + cue UI hiện có cũng được; không bắt buộc BGM riêng.' if event['id']=='BGM_GAME_OVER' else 'chưa có ứng viên đủ cơ sở; xem danh sách thiếu.')+'\n'
            text+='\n- [ ] Đồng ý với candidate đã chọn\n- [ ] Thay / cần âm thanh khác\n- [ ] Chưa chọn\n\nGhi chú nghe thử: __________________________\n\n'
    text+='''## Những quyết định nên chốt trước

1. Chọn palette BGM (chiptune/orchestral/metal có chấp nhận phối chung không); Day Hope hay Battle; Menu/Night có dùng chung End Dungeon; Game Over chọn im lặng hay cần nhạc riêng.
2. Nghe vòng nối chiptune end→~7,5s; không duyệt chỉ nghe đoạn đầu. End Dungeon là MP3, cần nghe cả loop/backend sau này. Nghe đối chiếu gain các BGM, không chọn chỉ theo BPM.
3. Chốt Sword swing/hit, Bow proxy có đủ dùng không, pickup glass và UI confirm/back; cho phép dùng chung và random variant đã nghe.
4. Ba nhóm thiếu quan trọng: fire cast, heavy/explosion impact, magic/electric. Có thể chia sẻ file cho nhiều event, không cần tải một gói lớn chỉ để đủ tên.
5. Cue player hurt và nhóm enemy có cần riêng trong demo hay tạm dùng impact chung. Các file có flag chỉ cần nghe đánh giá, chưa đồng ý normalize/trim/transcode.

Backlog Future (Dash/Ultimate/Evolution/extended specials) xem matrix, chưa yêu cầu chọn âm hoặc triển khai. Sau khi gửi lựa chọn, Task05.1B vẫn cần được yêu cầu riêng; audit không tự bắt đầu tích hợp.
'''
    (ROOT/'AUDIO_SELECTION_CHECKLIST.md').write_text(text,encoding='utf-8')

def missing():
    text='''# Audio Missing Assets — Task 05.1A

`MISSING` nghĩa là chưa nhận diện được file phù hợp từ nguồn hiện có bằng tên/mô tả/metadata; **chưa nghe mọi file** nên không khẳng định chúng tuyệt đối không thể tái sử dụng. Không yêu cầu tải thêm nếu chủ dự án nghe và duyệt một candidate hiện có. Không có license block đang chưa xác minh trong tập hiện tại.

## Thiếu có ảnh hưởng cốt lõi

'''
    text+=table(['Mức','Loại âm cụ thể','Event cần','Đặc điểm mong muốn','Khả năng dùng chung / nguồn hiện tại'],[
        ('REQUIRED','Fire / magic cast whoosh ngắn','WEAPON_FIRE_CAST','Khoảng0,1–0,4s, onset rõ, không voice/nhạc','Một file dùng cho một fan cast. Chưa có file được nhận diện; không dùng door/open làm fire chỉ để đủ bảng.'),
        ('REQUIRED','Heavy impact / compact explosion','WEAPON_FIRE_EXPLOSION, MINI_SLAM_IMPACT, LICH_AOE_IMPACT','Khoảng0,2–0,8s, thump có lực, tail gọn','Có thể dùng1–2 file chia sẻ bằng gain/pitch sau duyệt; chop hiện có chỉ là blade/physical proxy, không khẳng định blast/stone.'),
        ('REQUIRED','Electric zap / magic bolt','WEAPON_LIGHTNING_ACTIVATE/IMPACT, LICH_PROJECTILE','Khoảng0,1–0,5s, transient rõ và tail ngắn','Một zap có thể dùng release/impact khác gain; không tự gán glitch UI là điện khi chưa nghe.'),
        ('RECOMMENDED','Player hurt feedback','PLAYER_HURT','Ngắn, rõ khi HP thật giảm, có thể không vocal','Hiện có impact Boss/vũ khí làm fallback; chỉ bổ sung nếu cần nhận biết player nhận damage.'),
        ('RECOMMENDED nếu từ chối proxy','Bow twang / arrow release','WEAPON_BOW_RELEASE','Một dây bật ngắn, không sounding như nút UI','Nghe pluck_002/001 trước. Nếu đủ demo thì không cần file mới.'),
        ('RECOMMENDED nếu từ chối proxy','Body/creature death reaction chung','ENEMY_DEATH','0,15–0,5s, dùng chung nhiều loài','Nghe dropLeather trước; không cần82 âm hoặc vocal riêng cho từng loài.'),
        ('OPTIONAL','Flying/beast/undead reaction groups','ENEMY_FLYING/BEAST/UNDEAD_REACTION','Một palette nhỏ theo nhóm thật sự phân biệt','Generic impact đã có, chưa cần trong demo.'),
        ('OPTIONAL','Boss entrance/phase riser/roar','MINI_APPEARS, LICH_APPEARS/PHASE2','Một cue0,3–1s không che cảnh báo tiếp theo','Bong/maximize hiện có là cue UI tạm; không bắt buộc thêm nếu người dùng chấp nhận.'),
        ('OPTIONAL','Victory/GameOver sting hoặc menu theme riêng','BGM_GAME_OVER, UI_VICTORY/GAME_OVER, BGM_MENU','Sting0,5–2s nếu muốn; theme menu không bắt buộc riêng','Bốn BGM có thể dùng lại, confirmation/error làm result cue. End Dungeon dài165,564s là dungeon theme, không phải sting thắng.'),
        ('OPTIONAL / FUTURE','Dash / Ultimate / Evolution / extended special','FUTURE_*','Chỉ xác định sau khi có gameplay/timing','Không hook vào clip/catalog để giả có action. Không triển khai ở audit.'),
    ])
    text+='''
## Đã đủ candidate để chủ dự án chọn, không cần xin thêm ngay

UI tap/select/confirm/back/pause/resume; pickup glass/weapon open/commit/upgrade; sword slice/chop; armor metal click; BGM Day/Night/Mini/Final và reuse menu/Victory. Tất cả candidate có thật trong [matrix](AUDIO_EVENT_MATRIX.md). Hãy nghe và đánh giá trước; tên phù hợp chưa bảo đảm chất âm.

## Việc kỹ thuật không phải thiếu asset

- Loop chiptune cần entry0 rồi repeat xấp xỉ7,5s theo tác giả; hiện chưa có exact loop sample tag. Cần xác định seam sau khi nghe, không phát `setLooping(true)` rồi gọi là seamless.
- MP3 End Dungeon có delay/padding và loop gap cần kiểm tra backend. Cả Ogg cũng cần kiểm tra Android sau tích hợp; audit decode local không bảo đảm gapless.
- 26 file có full-scale sample,1 file DC offset>1%, vài lead/tail dài và RMS chênh lệch. Cần nghe/gain tuning hoặc xin duyệt derivative nếu cần chỉnh. Không phải26 file hỏng, không cần tải bản mới hàng loạt.
- Preview26,127s là montage nguồn, không nạp như SFX/BGM trong game. RPG local51 SFX so với trang50 không tự chứng minh thiếu/hỏng; giữ nguyên.
- License đã đối chiếu riêng sáu nguồn. Không cần bổ sung license link cho bốn BGM hiện có, nhưng nên giữ provenance/credit khi lựa chọn được đóng gói ở task sau.

Ưu tiên tối thiểu: duyệt candidate đang có; nếu các khoảng trống combat còn quan trọng, tìm **ba nhóm âm cụ thể** ở trên thay vì yêu cầu một pack lớn. Task này không tải thêm hoặc chỉnh nguồn.
'''
    (ROOT/'AUDIO_MISSING_ASSETS.md').write_text(text,encoding='utf-8')

def audition():
    # Keep the offline picker/catalog current when regenerating audit documents.
    from build_audio_audition import build
    build(events=EVENTS)


def main():
    assert len(EVENTS)==len({e['id'] for e in EVENTS})
    inventory();matrix_and_checklist();missing();audition()
    (OUT/'audio-event-plan.json').write_text(json.dumps(EVENTS,ensure_ascii=False,indent=2),encoding='utf-8')
    (OUT/'source-verification.json').write_text(json.dumps({'checked':'2026-11-14','method':'local licenses + NTFS provenance + official source-page content; no asset downloads','sources':SOURCES},ensure_ascii=False,indent=2),encoding='utf-8')
    chosen={e['candidates'][0] for e in EVENTS if e['candidates'] and e['group']!='BGM'}
    stats=dict(events=len(EVENTS),groups=dict(Counter(e['group'] for e in EVENTS)),missing=len([e for e in EVENTS if not e['candidates']]),uniqueFirstChoiceSfx=len(chosen),firstChoiceSfxBytes=sum(FILES[p]['bytes'] for p in chosen),firstChoiceSfxPcmBytes=sum(FILES[p]['pcm']['pcm16Bytes'] for p in chosen))
    (OUT/'selection-summary.json').write_text(json.dumps(stats,indent=2),encoding='utf-8');print(json.dumps(stats,indent=2))

if __name__=='__main__':main()
