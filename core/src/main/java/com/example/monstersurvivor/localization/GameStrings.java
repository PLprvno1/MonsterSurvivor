package com.example.monstersurvivor.localization;

import com.example.monstersurvivor.weapon.WeaponType;
import com.example.monstersurvivor.pickup.PickupType;
import com.example.monstersurvivor.skill.ClassSkillId;
import java.util.Locale;

/** Complete, immutable core translations. UI02 can reuse these keys without Android resources. */
public final class GameStrings {
    public enum Key {
        CLASS_EVO_WHIRLWIND("Lốc Kiếm Kép","Double Sword Cyclone"), CLASS_EVO_WHIRLWIND_DESC("Thêm một nhát xoáy 360° sau 0,28 giây; nhát sau gây 75% sát thương.","Adds a second 360° pulse after 0.28s; the second deals 75% damage."),
        CLASS_EVO_WAVE("Tam Liên Kiếm Khí","Triple Sword Qi"), CLASS_EVO_WAVE_DESC("Phóng ba sóng thẳng ở góc −12°, 0°, +12°; mỗi mục tiêu nhận tối đa hai hit mỗi lần.","Fires three straight waves at −12°, 0°, +12°; each target can be hit twice per cast."),
        CLASS_EVO_PARRY("Thánh Thuẫn Phản Đòn","Sacred Counter Shield"), CLASS_EVO_PARRY_DESC("Chặn đòn trực tiếp từ mọi hướng và phản công trong bán kính 34.","Blocks direct hits from every direction and counters within radius 34."),
        CLASS_EVO_WAR_CRY("Chiến Hống Chiến Thần","War God's Cry"), CLASS_EVO_WAR_CRY_DESC("Thêm nhịp đẩy lùi quái thường trong bán kính 40; buff kéo dài thêm 1 giây.","Adds a knockback pulse against normal enemies within radius 40; buff lasts 1s longer."),
        CLASS_EVO_MARK("Ấn Săn Mồi","Predator's Mark"), CLASS_EVO_MARK_DESC("Khi mục tiêu chết, dấu ấn chuyển một lần sang quái gần nhất trong 65; giữ thời gian còn lại, tối đa hai hit.","On target death, the mark transfers once to the nearest enemy within 65; keeps remaining time and up to two hits."),
        CLASS_EVO_FOCUS("Xuyên Tâm Liên Tiễn","Heartpiercer"), CLASS_EVO_FOCUS_DESC("Mũi tên xuyên tối đa hai mục tiêu; mục tiêu thứ hai nhận 70% sát thương.","The arrow pierces up to two targets; the second takes 70% damage."),
        CLASS_EVO_RAIN("Thiên Vũ","Heavenly Rain"), CLASS_EVO_RAIN_DESC("Thêm một đợt mưa tên; mỗi mục tiêu nhận tối đa ba hit mỗi lần kích hoạt.","Adds one arrow pulse; each target can take up to three hits per activation."),
        CLASS_EVO_POISON("Độc Tiễn Lây Lan","Infectious Poison"), CLASS_EVO_POISON_DESC("Khi mục tiêu nhiễm độc chết, độc lan tới tối đa hai quái gần nhất trong 45; không lan tiếp.","When a poisoned target dies, poison spreads to up to two nearby enemies within 45; no further spread."),
        CLASS_EVO_ICE("Băng Vỡ Liên Hoàn","Ice Shatter Chain"), CLASS_EVO_ICE_DESC("Hit hợp lệ tạo ba mảnh băng tại điểm va chạm; mỗi mảnh gây nửa sát thương và nửa hiệu lực làm chậm.","Accepted hits create three ice shards at impact; each deals half damage and half slow."),
        CLASS_EVO_TRAP("Ma Trận Liên Hoàn","Arcane Matrix"), CLASS_EVO_TRAP_DESC("Bẫy phát nổ hai nhịp cách nhau 0,35 giây; mỗi mục tiêu chỉ trúng một lần mỗi nhịp.","Trap blasts twice, 0.35s apart; each target can be hit once per pulse."),
        CLASS_EVO_SHIELD("Khiên Phản Phép","Spell Shield"), CLASS_EVO_SHIELD_DESC("Sau khi chặn đòn trực tiếp, phát sóng xung kích bán kính 30, sát thương 1.","After blocking a direct hit, emits a radius-30 shockwave for 1 damage."),
        CLASS_WAR_CRY("Chiến Hống","War Cry"),
        CLASS_WAR_CRY_SUMMARY("Khi đủ bốn quái ở gần, gây sát thương và đẩy lùi; tăng sát thương, giảm sát thương trực tiếp nhận vào.","When four enemies are nearby, damage and repel them; gain direct damage and direct damage reduction."),
        CLASS_WAR_CRY_DETAIL("Sát thương %d -> %d  |  Bán kính %.0f -> %.0f, đẩy %d -> %d  |  Buff %.1f -> %.1fs, giảm nhận %d%% -> %d%%","Damage %d -> %d  |  Radius %.0f -> %.0f, knockback %d -> %d  |  Buff %.1f -> %.1fs, reduction %d%% -> %d%%"),
        CLASS_ARCHER_POISON("Tên Độc","Poison Arrow"),
        CLASS_ARCHER_POISON_SUMMARY("Tự bắn một mũi tên thẳng; hit hợp lệ gây độc theo thời gian.","Automatically fire a straight arrow; accepted hits apply poison over time."),
        CLASS_ARCHER_POISON_DETAIL("Sát thương tên %d -> %d  |  Độc %d ST, %d -> %d nhịp, %.1f -> %.1fs","Arrow damage %d -> %d  |  Poison %d dmg, %d -> %d ticks, %.1f -> %.1fs"),
        STATUS_POISON("ĐỘC","POISON"),STATUS_BURN("CHÁY","BURN"),STATUS_SLOW("CHẬM","SLOW"),
        EVOLUTION("TIẾN HÓA","EVOLUTION"), EVOLVED("ĐÃ TIẾN HÓA","EVOLVED"), EVOLUTION_ICON("TH","EV"),
        EVOLUTION_TITLE("%s Lv5 -> %s","%s Lv5 -> %s"), EVOLVED_SLOT("%s TH","%s EV"),
        BLADE_ORBIT("Vòng Kiếm","Blade Orbit"), ARROW_STORM("Mưa Tên","Arrow Storm"),
        INFERNO_METEOR("Thiên Thạch Lửa","Inferno Meteor"), THUNDER_WEB("Lôi Võng","Thunder Web"),
        ORBIT_DESC("Hai kiếm xoay quanh người; mỗi quái có hồi hit.","Two orbiting blades; each target has a hit cooldown."),
        STORM_DESC("Năm tên theo quạt; xuyên hai quái, không đuổi.","Five straight fan arrows; pierce two targets."),
        METEOR_DESC("Một thiên thạch lớn; nổ và để lại vùng lửa.","One large meteor; blast and a brief burning zone."),
        WEB_DESC("Ba nhánh sét; tối đa tám quái, không đánh lặp.","Three lightning branches; eight unique targets max."),
        REWARD_UNLOCK("MỞ KHÓA","UNLOCK"), REWARD_UPGRADE("NÂNG CẤP","UPGRADE"), REWARD_STAT("CHỈ SỐ","STAT BOOST"),
        SKILL_BOX("Hộp Kỹ Năng","Skill Box"), CHOOSE_SKILL("HỘP KỸ NĂNG","SKILL BOX"),
        TREASURE_CHEST("Rương Báu","Treasure Chest"), CHEST_PASSIVE_BRANCH("KỸ NĂNG BỊ ĐỘNG","PASSIVE SKILLS"),
        CHEST_ACTIVE_BRANCH("KỸ NĂNG CHỦ ĐỘNG","ACTIVE SKILLS"), CHEST_FALLBACK_BRANCH("CHỈ SỐ NHÂN VẬT","CHARACTER STATS"),
        CHEST_REWARD_HINT("Chọn một phần thưởng","Choose one reward"),
        COIN_ICON("XU","COIN"), COINS_TOTAL("Xu: %d","Coins: %d"), PICKUP_COIN("+1 xu","+1 coin"),
        SKILL_NAME_LEVEL("%s Lv%d","%s Lv%d"), SKILL_UPGRADE_TITLE("%s  Cấp %d -> %d","%s  Lv%d -> Lv%d"),
        SKILL_CURRENT_LEVEL("Cấp hiện tại","Current level"), SKILL_NEW_LEVEL("Cấp mới","New level"),
        SKILL_MAX_LEVEL("CẤP TỐI ĐA","MAX LEVEL"), SKILL_ICON("KN","SK"),
        SKILL_UPGRADE_DASH("Nâng cấp Lướt","Upgrade Dash"), SKILL_UPGRADE_ULTIMATE("Nâng cấp Tuyệt Kỹ","Upgrade Ultimate"),
        SKILL_REWARD_HINT("Chọn một nâng cấp kỹ năng","Choose one skill upgrade"),
        SKILL_DASH_SUMMARY("Lướt xa hơn, hồi nhanh hơn","Dash farther, recharge faster"),
        SKILL_ULTIMATE_SUMMARY("Sóng năng lượng rộng và mạnh hơn","A wider, stronger energy burst"),
        CLASS_WHIRLWIND("Xoáy Kiếm","Whirlwind"), CLASS_WAVE("Sóng Kiếm","Sword Wave"), CLASS_PARRY("Đỡ Đòn Phản Kích","Parry Counter"),
        CLASS_WHIRLWIND_SUMMARY("Chém một vòng quanh Kiếm sĩ.","Slash once around the Warrior."),
        CLASS_WAVE_SUMMARY("Phóng kiếm khí thẳng, xuyên qua nhiều quái.","Fire a straight crescent through enemies."),
        CLASS_PARRY_SUMMARY("Chặn đòn phía trước rồi phản công.","Block a frontal hit and counter."),
        CLASS_ARCHER_MARK("Đánh Dấu Thợ Săn","Hunter's Mark"),
        CLASS_ARCHER_FOCUS("Ngắm Bắn Tập Trung","Focus Shot"),
        CLASS_ARCHER_RAIN("Mưa Tên","Arrow Rain"),
        CLASS_ARCHER_MARK_SUMMARY("Đánh dấu mục tiêu; các phát Cung và Focus Shot đầu được +1 sát thương.","Mark a target; the next Bow and Focus Shot hits gain +1 damage."),
        CLASS_ARCHER_FOCUS_SUMMARY("Bắn một mũi tên thẳng mạnh, ưu tiên Boss.","Fire one powerful straight arrow, prioritizing a Boss."),
        CLASS_ARCHER_RAIN_SUMMARY("Cảnh báo một cụm quái rồi bắn nhiều đợt tên.","Mark a dense enemy cluster, then strike it with several arrow pulses."),
        CLASS_ARCHER_MARK_DETAIL("Số hit được +1 ST: %d -> %d  |  Thời gian: %.1f -> %.1fs","Bonus hits: %d -> %d  |  Duration: %.1f -> %.1fs"),
        CLASS_ARCHER_FOCUS_DETAIL("Sát thương %d -> %d  |  Tầm 170, tốc độ 180","Damage %d -> %d  |  Range 170, speed 180"),
        CLASS_ARCHER_RAIN_DETAIL("Đợt tên %d -> %d  |  Bán kính %.0f -> %.0f","Pulses %d -> %d  |  Radius %.0f -> %.0f"),
        CLASS_MAGE_ICE("Băng Tiễn","Ice Shard"),
        CLASS_MAGE_TRAP("Bẫy Bí Thuật","Arcane Trap"),
        CLASS_MAGE_SHIELD("Khiên Phép","Magic Shield"),
        CLASS_MAGE_CHANNEL("Dồn Năng Lượng","Arcane Channel"),
        CLASS_MAGE_ICE_SUMMARY("Bắn mảnh băng thẳng; hit hợp lệ làm chậm quái thường.","Fire a straight ice shard; accepted hits slow ordinary enemies."),
        CLASS_MAGE_TRAP_SUMMARY("Đặt rune cố định, kích nổ khi quái bước vào.","Place a fixed rune that explodes when an enemy enters."),
        CLASS_MAGE_SHIELD_SUMMARY("Tự chặn một đòn trực tiếp từ bất kỳ hướng nào.","Automatically block one direct hit from any direction."),
        CLASS_MAGE_CHANNEL_SUMMARY("Tăng nhịp Fireball và sát thương hit trực tiếp chính.","Improve Fireball cadence and its primary direct hit."),
        CLASS_MAGE_ICE_DETAIL("Sát thương %d -> %d  |  Chậm %d%% -> %d%%, %.1f -> %.1fs","Damage %d -> %d  |  Slow %d%% -> %d%%, %.1f -> %.1fs"),
        CLASS_MAGE_TRAP_DETAIL("Sát thương %d -> %d  |  Bán kính nổ %.0f -> %.0f","Damage %d -> %d  |  Blast radius %.0f -> %.0f"),
        CLASS_MAGE_SHIELD_DETAIL("Chặn một đòn trực tiếp  |  Không phản công","Blocks one direct hit  |  No counterattack"),
        CLASS_MAGE_CHANNEL_DETAIL("Thời gian %.1f -> %.1fs  |  Fireball cooldown x%.2f -> x%.2f","Duration %.1f -> %.1fs  |  Fireball cooldown x%.2f -> x%.2f"),
        CLASS_WAVE_SPEC("Kiếm khí bay thẳng (tầm 120, tốc độ 145).","A straight crescent (range 120, speed 145)."),
        CLASS_COOLDOWN("Hồi %.1f -> %.1f giây","Cooldown %.1f -> %.1fs"),
        CLASS_RADIUS("Bán kính %.0f -> %.0f","Radius %.0f -> %.0f"),
        CLASS_DAMAGE("Sát thương %d -> %d","Damage %d -> %d"),
        CLASS_WAVE_DETAIL("Sát thương %d -> %d  |  Xuyên %d -> %d quái","Damage %d -> %d  |  Pierce %d -> %d enemies"),
        CLASS_NEW_COOLDOWN_RADIUS("Hồi %.1f giây  |  Bán kính %.0f","Cooldown %.1fs  |  Radius %.0f"),
        CLASS_NEW_COOLDOWN("Hồi %.1f giây","Cooldown %.1fs"),
        CLASS_NEW_DAMAGE("Sát thương %d","Damage %d"),
        CLASS_NEW_WAVE_DETAIL("Sát thương %d  |  Xuyên %d quái","Damage %d  |  Pierce %d enemies"),
        CLASS_NEW_PARRY_DETAIL("Sát thương %d  |  Bán kính 28","Damage %d  |  Radius 28"),
        CLASS_WHIRLWIND_DETAIL("Bán kính %.0f -> %.0f  |  Sát thương %d -> %d","Radius %.0f -> %.0f  |  Damage %d -> %d"),
        CLASS_PARRY_DETAIL("Sát thương %d -> %d  |  Bán kính 28","Damage %d -> %d  |  Radius 28"),
        SKILL_DASH_BENEFIT("Hồi %.1f -> %.1fs  |  Lướt %.0f -> %.0f","Cooldown %.1f -> %.1fs  |  Distance %.0f -> %.0f"),
        SKILL_DASH_IFRAME("Miễn sát thương %.2f -> %.2fs","Invulnerability %.2f -> %.2fs"),
        SKILL_BURST_BENEFIT("Hồi %.0f -> %.0fs  |  Bán kính %.0f -> %.0f","Cooldown %.0f -> %.0fs  |  Radius %.0f -> %.0f"),
        SKILL_BURST_DAMAGE("Sát thương x%.1f -> x%.1f  |  Đẩy %.0f -> %.0f","Damage x%.1f -> x%.1f  |  Knockback %.0f -> %.0f"),
        SKILL_DASH("LƯỚT","DASH"), SKILL_ULTIMATE("TUYỆT KỸ","ULTIMATE"), SKILL_ENERGY_BURST("Sóng Năng Lượng","Energy Burst"),
        SKILL_READY("SẴN SÀNG","READY"), SKILL_COOLDOWN("Hồi chiêu","Cooldown"), SKILL_ACTIVE("ĐANG DÙNG","ACTIVE"),
        SKILL_UNAVAILABLE("Không thể dùng kỹ năng lúc này","Skill unavailable now"), SKILL_DISABLED("KHÓA","LOCKED"),
        SKILL_CONTROLS("Chạm nút: Lướt / Sóng Năng Lượng. Shift: lướt; Space: tuyệt kỹ.","Tap Dash / Energy Burst. Shift: dash; Space: ultimate."),
        POTION_HEALTH("Bình Máu","Healing Potion"), POTION_HEALTH_DESC("Hồi ngay 4 HP; không dùng khi đầy máu.","Restore 4 HP now; unavailable at full health."),
        POTION_SPEED("Bình Tăng Tốc","Speed Potion"), POTION_SPEED_DESC("Chạy nhanh hơn 25% trong 8 giây.","Run 25% faster for 8 seconds."),
        POTION_COOLDOWN("Bình Giảm Hồi Chiêu","Cooldown Potion"), POTION_COOLDOWN_DESC("Dash, Tuyệt Kỹ và kỹ năng bị động hồi nhanh hơn 25% trong 8 giây.","Dash, Ultimate and passive skills recharge 25% faster for 8 seconds."),
        POTION_NO_INVENTORY("Đã hết bình này.","No potions of this type left."),
        POTION_HEALTH_FULL("Máu đã đầy.","Health is already full."),
        POTION_ALREADY_ACTIVE("Hiệu ứng bình này đang hoạt động.","This potion effect is already active."),
        POTION_MODE_DISABLED("Chỉ dùng bình trong Normal hoặc Endless.","Potions are available in Normal and Endless only."),
        POTION_NOT_RUNNING("Không thể dùng bình lúc này.","A potion cannot be used now."),
        POTION_SAVING("Đang xác nhận bình...","Confirming potion..."),
        POTION_APPLIED("Đã dùng bình.","Potion used."),
        POTION_SAVE_FAILED("Không thể lưu lượt dùng bình.","Could not save potion use."),
        HP("HP","HP"), DAMAGE("Sát thương","Damage"), SPEED("Tốc độ","Speed"), KILLS("HẠ","KILLS"), TIME("Thời gian","Time"),
        DAMAGE_SHORT("ST","DMG"), SPEED_SHORT("TỐC","SPEED"),
        PAUSE("TẠM DỪNG","PAUSE"), PAUSED("TẠM DỪNG","PAUSED"), RESUME("TIẾP TỤC","RESUME"),
        RETRY("CHƠI LẠI","RETRY"), HOME("TRANG CHỦ","HOME"), GAME_OVER("KẾT THÚC","GAME OVER"),
        HOME_CONFIRM_TITLE("Về trang chủ?","Return Home?"), HOME_CONFIRM("XÁC NHẬN","CONFIRM"), CANCEL("TIẾP TỤC","CANCEL"),
        CHOOSE_WEAPON("CHỌN VŨ KHÍ","CHOOSE A WEAPON"), SURVIVED("SỐNG SÓT","SURVIVED"),
        SWORD("Kiếm","Sword"), BOW("Cung","Bow"), FIREBALL("Cầu lửa","Fireball"), LIGHTNING("Sét","Lightning"),
        SWORD_SHORT("KIẾM","SW"), BOW_SHORT("CUNG","BOW"), FIREBALL_SHORT("LỬA","FIRE"), LIGHTNING_SHORT("SÉT","LTN"),
        LEVEL("Cấp","Level"), WEAPON_LEVEL("%s  C%d","%s  L%d"), NEW_WEAPON("%s  MỚI - Cấp 1","%s  NEW - Lv 1"),
        UPGRADE_WEAPON("%s  Cấp %d -> %d","%s  Lv %d -> %d"), EMPTY_SLOT("-- TRỐNG","-- EMPTY"),
        WEAPON_HINT("Chọn một phần thưởng","Choose one reward"),
        CONTROLS("Kéo để di chuyển","Drag to move"),
        DEBUG_CONTROLS("Kéo / WASD   Shift: lướt   Space: tuyệt kỹ   P: dừng   F2/F3","Drag / WASD   Shift: dash   Space: ultimate   P: pause   F2/F3"),
        ANIMATION("ANIM: ","ANIM: "), DIRECTION("HƯỚNG: ","DIR: "), FPS("FPS ","FPS "),
        NORMAL("Thường","Normal"), DEMO("DEMO","DEMO"), ENDLESS("ENDLESS","ENDLESS"),
        ENDLESS_BOSS("BOSS VÔ HẠN","ENDLESS BOSS"), ENDLESS_BOSS_APPEARS("BOSS VÔ HẠN XUẤT HIỆN!","ENDLESS BOSS APPEARS!"),
        ENDLESS_STAGE("GĐ","STAGE"), BOSS_KILLS("BOSS ĐÃ HẠ","BOSSES DEFEATED"),
        EASY("Dễ","Easy"), HARD("Khó","Hard"),
        SWORD_NEW("Tự động chém cận chiến khi di chuyển.","Automatic melee slashes while moving."),
        BOW_NEW("Tự bắn tên vào mục tiêu gần có đường bắn.","Automatic arrows at the nearest clear target."),
        FIREBALL_NEW("Phóng cầu lửa, phát nổ khi trúng mục tiêu.","Launch fireballs that explode on impact."),
        LIGHTNING_NEW("Sét tức thời, lan sang quái ở gần.","Instant lightning chaining to nearby enemies."),
        SWORD_UPGRADE("Chém mạnh hơn và hồi đòn nhanh hơn.","Stronger slashes and a shorter attack cycle."),
        BOW_UPGRADE("Tên mạnh, nhanh hơn; xuyên ở cấp 3 và 5.","Stronger, faster arrows; pierce at Lv 3 and 5."),
        FIREBALL_UPGRADE("Thêm cầu lửa, nổ mạnh hơn, giảm hồi chiêu.","More fireballs, stronger blasts, shorter cooldowns."),
        LIGHTNING_UPGRADE("Sét mạnh hơn; hai tia đầu ở cấp 5.","Stronger chains; two initial bolts at Lv 5."),
        FALLBACK_DAMAGE("Sát thương +2","Damage +2"), FALLBACK_DAMAGE_DESC("Tăng sát thương gốc của mọi vũ khí.","Increase base damage for every weapon."),
        FALLBACK_HP("HP tối đa +2","Max HP +2"), FALLBACK_HP_DESC("Tăng 2 HP tối đa và hồi 2 HP.","Gain 2 maximum HP and heal 2 HP."),
        FALLBACK_SPEED("Tốc độ +1","Speed +1"), FALLBACK_SPEED_DESC("Di chuyển nhanh hơn. Tốc độ tối đa 20.","Move faster. Maximum Speed is 20."),
        PICKUP_DAMAGE("+Sát thương %d","+Damage %d"), PICKUP_MAX_HP("+HP tối đa %d","+Max HP %d"),
        PICKUP_HEAL("+HP %d","+HP %d"), PICKUP_SPEED("+Tốc độ %d","+Speed %d"),
        DAMAGE_ICON("ST","D"), HEALTH_ICON("HP","H"), SPEED_ICON("T","S"), WEAPON_ICON("VK","W"),
        MINI_BOSS("MINI BOSS","MINI BOSS"), FINAL_BOSS("BOSS CUỐI","FINAL BOSS"),
        MINI_BOSS_APPEARS("MINI BOSS XUẤT HIỆN!","MINI BOSS APPEARS!"), FINAL_BOSS_APPEARS("BOSS CUỐI XUẤT HIỆN!","FINAL BOSS APPEARS!"),
        BOSS_DEFEATED("ĐÃ HẠ BOSS!","BOSS DEFEATED!"), BOSS_PHASE_TWO("BOSS BƯỚC VÀO GIAI ĐOẠN 2!","BOSS ENTERS PHASE 2!"),
        BOSS_WARNING("BOSS ĐANG TÌM ĐƯỜNG XUẤT HIỆN...","BOSS IS FINDING AN ENTRANCE..."),
        BOSS_HP("HP BOSS","BOSS HP"), PHASE_ONE("Giai đoạn 1","Phase 1"), PHASE_TWO("Giai đoạn 2","Phase 2"),
        VICTORY("CHIẾN THẮNG!","VICTORY!"), YES("Đã hạ","Defeated"), NO("Chưa hạ","Not defeated"), LICH_LORD("LICH LORD","LICH LORD"),
        BOSS_MOSS("KHỔNG LỒ RÊU","MOSS GOLEM"), BOSS_ARMOR("GIÁP SỐNG","LIVING ARMOR"), BOSS_CYCLOPS("ĐỘC NHÃN","CYCLOPS"),
        BOSS_CRAB("HIỆP SĨ CUA","CRAB KNIGHT"), BOSS_DUST("QUỶ CÁT","DUST DEVIL"), BOSS_FLYTRAP("CÂY ĂN THỊT","FLYTRAP WALKER"),
        BOSS_GOBLIN("GOBLIN NÉM BOM","GOBLIN BOMBER"), BOSS_ROCK("GOLEM ĐÁ","ROCK GOLEM");
        private final String vi,en;
        Key(String vi,String en) {this.vi=vi;this.en=en;}
    }
    private static final GameStrings VI=new GameStrings(GameLanguage.VI),EN=new GameStrings(GameLanguage.EN);
    public final GameLanguage language;
    private GameStrings(GameLanguage language) {this.language=language;}
    public static GameStrings of(GameLanguage language) {return language==GameLanguage.EN?EN:VI;}
    public String get(Key key) {return language==GameLanguage.EN?key.en:key.vi;}
    public String format(Key key,Object... args) {return String.format(Locale.ROOT,get(key),args);}
    public String weaponName(WeaponType type) {return get(switch(type) {case SWORD->Key.SWORD;case BOW->Key.BOW;case FIREBALL->Key.FIREBALL;case LIGHTNING->Key.LIGHTNING;});}
    public String weaponShort(WeaponType type) {return get(switch(type) {case SWORD->Key.SWORD_SHORT;case BOW->Key.BOW_SHORT;case FIREBALL->Key.FIREBALL_SHORT;case LIGHTNING->Key.LIGHTNING_SHORT;});}
    public String evolvedName(WeaponType type) {return get(switch(type) {case SWORD->Key.BLADE_ORBIT;case BOW->Key.ARROW_STORM;case FIREBALL->Key.INFERNO_METEOR;case LIGHTNING->Key.THUNDER_WEB;});}
    public String evolutionDescription(WeaponType type) {return get(switch(type) {case SWORD->Key.ORBIT_DESC;case BOW->Key.STORM_DESC;case FIREBALL->Key.METEOR_DESC;case LIGHTNING->Key.WEB_DESC;});}
    public String evolvedClassSkillName(ClassSkillId id) {return get(evolvedClassSkillNameKey(id));}
    public String evolvedClassSkillDescription(ClassSkillId id) {return get(evolvedClassSkillDescriptionKey(id));}
    private static Key evolvedClassSkillNameKey(ClassSkillId id) {return switch(id) {
        case WARRIOR_WHIRLWIND -> Key.CLASS_EVO_WHIRLWIND;case WARRIOR_SWORD_WAVE -> Key.CLASS_EVO_WAVE;case WARRIOR_PARRY -> Key.CLASS_EVO_PARRY;case WARRIOR_WAR_CRY -> Key.CLASS_EVO_WAR_CRY;
        case ARCHER_MARK -> Key.CLASS_EVO_MARK;case ARCHER_FOCUS -> Key.CLASS_EVO_FOCUS;case ARCHER_RAIN -> Key.CLASS_EVO_RAIN;case ARCHER_POISON_ARROW -> Key.CLASS_EVO_POISON;
        case MAGE_ICE_SHARD -> Key.CLASS_EVO_ICE;case MAGE_ARCANE_TRAP -> Key.CLASS_EVO_TRAP;case MAGE_SHIELD -> Key.CLASS_EVO_SHIELD;
        case MAGE_CHANNEL -> throw new IllegalArgumentException("Arcane Channel has no evolution");
    };}
    private static Key evolvedClassSkillDescriptionKey(ClassSkillId id) {return switch(id) {
        case WARRIOR_WHIRLWIND -> Key.CLASS_EVO_WHIRLWIND_DESC;case WARRIOR_SWORD_WAVE -> Key.CLASS_EVO_WAVE_DESC;case WARRIOR_PARRY -> Key.CLASS_EVO_PARRY_DESC;case WARRIOR_WAR_CRY -> Key.CLASS_EVO_WAR_CRY_DESC;
        case ARCHER_MARK -> Key.CLASS_EVO_MARK_DESC;case ARCHER_FOCUS -> Key.CLASS_EVO_FOCUS_DESC;case ARCHER_RAIN -> Key.CLASS_EVO_RAIN_DESC;case ARCHER_POISON_ARROW -> Key.CLASS_EVO_POISON_DESC;
        case MAGE_ICE_SHARD -> Key.CLASS_EVO_ICE_DESC;case MAGE_ARCANE_TRAP -> Key.CLASS_EVO_TRAP_DESC;case MAGE_SHIELD -> Key.CLASS_EVO_SHIELD_DESC;
        case MAGE_CHANNEL -> throw new IllegalArgumentException("Arcane Channel has no evolution");
    };}
    public String weaponDescription(WeaponType type,boolean upgrade) {
        return get(upgrade?switch(type) {case SWORD->Key.SWORD_UPGRADE;case BOW->Key.BOW_UPGRADE;case FIREBALL->Key.FIREBALL_UPGRADE;case LIGHTNING->Key.LIGHTNING_UPGRADE;}
            :switch(type) {case SWORD->Key.SWORD_NEW;case BOW->Key.BOW_NEW;case FIREBALL->Key.FIREBALL_NEW;case LIGHTNING->Key.LIGHTNING_NEW;});
    }
    public String pickupLabel(PickupType type) {return type==PickupType.HEALTH_DROP?"+":get(switch(type) {
        case DAMAGE_BOX->Key.DAMAGE_ICON;case HEALTH_BOX->Key.HEALTH_ICON;case SPEED_BOX->Key.SPEED_ICON;case WEAPON_BOX->Key.WEAPON_ICON;case SKILL_BOX->Key.SKILL_ICON;case TREASURE_CHEST->Key.TREASURE_CHEST;case COIN->Key.COIN_ICON;
        case HEALTH_DROP->throw new IllegalArgumentException("Health drops use the plus label");});}
}
