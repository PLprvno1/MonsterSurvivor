package com.example.monstersurvivor.localization;

import com.example.monstersurvivor.weapon.WeaponType;
import com.example.monstersurvivor.pickup.PickupType;
import java.util.Locale;

/** Complete, immutable core translations. UI02 can reuse these keys without Android resources. */
public final class GameStrings {
    public enum Key {
        HP("HP","HP"), DAMAGE("Sát thương","Damage"), SPEED("Tốc độ","Speed"), KILLS("HẠ","KILLS"), TIME("Thời gian","Time"),
        DAMAGE_SHORT("ST","DMG"), SPEED_SHORT("TỐC","SPEED"),
        PAUSE("TẠM DỪNG","PAUSE"), PAUSED("TẠM DỪNG","PAUSED"), RESUME("TIẾP TỤC","RESUME"),
        RETRY("CHƠI LẠI","RETRY"), HOME("TRANG CHỦ","HOME"), GAME_OVER("KẾT THÚC","GAME OVER"),
        CHOOSE_WEAPON("CHỌN VŨ KHÍ","CHOOSE A WEAPON"), SURVIVED("SỐNG SÓT","SURVIVED"),
        SWORD("Kiếm","Sword"), BOW("Cung","Bow"), FIREBALL("Cầu lửa","Fireball"), LIGHTNING("Sét","Lightning"),
        SWORD_SHORT("KIẾM","SW"), BOW_SHORT("CUNG","BOW"), FIREBALL_SHORT("LỬA","FIRE"), LIGHTNING_SHORT("SÉT","LTN"),
        LEVEL("Cấp","Level"), WEAPON_LEVEL("%s  C%d","%s  L%d"), NEW_WEAPON("%s  MỚI - Cấp 1","%s  NEW - Lv 1"),
        UPGRADE_WEAPON("%s  Cấp %d -> %d","%s  Lv %d -> %d"), EMPTY_SLOT("-- TRỐNG","-- EMPTY"),
        WEAPON_HINT("Chạm chọn thưởng  |  1 / 2 / 3  |  BACK: tạm dừng","Tap one reward  |  1 / 2 / 3  |  BACK: pause"),
        CONTROLS("KÉO để chạy / WASD / phím hướng    P: tạm dừng","DRAG to move / WASD / arrows    P: pause"),
        DEBUG_CONTROLS("KÉO để chạy / WASD / phím hướng    P: tạm dừng    F2/F3: xem thử","DRAG to move / WASD / arrows    P: pause    F2/F3: preview"),
        ANIMATION("ANIM: ","ANIM: "), DIRECTION("HƯỚNG: ","DIR: "), FPS("FPS ","FPS "),
        NORMAL("Thường","Normal"), DEMO("DEMO","DEMO"), EASY("Dễ","Easy"), HARD("Khó","Hard"),
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
        DAMAGE_ICON("ST","D"), HEALTH_ICON("HP","H"), SPEED_ICON("T","S"), WEAPON_ICON("VK","W");
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
    public String weaponDescription(WeaponType type,boolean upgrade) {
        return get(upgrade?switch(type) {case SWORD->Key.SWORD_UPGRADE;case BOW->Key.BOW_UPGRADE;case FIREBALL->Key.FIREBALL_UPGRADE;case LIGHTNING->Key.LIGHTNING_UPGRADE;}
            :switch(type) {case SWORD->Key.SWORD_NEW;case BOW->Key.BOW_NEW;case FIREBALL->Key.FIREBALL_NEW;case LIGHTNING->Key.LIGHTNING_NEW;});
    }
    public String pickupLabel(PickupType type) {return type==PickupType.HEALTH_DROP?"+":get(switch(type) {
        case DAMAGE_BOX->Key.DAMAGE_ICON;case HEALTH_BOX->Key.HEALTH_ICON;case SPEED_BOX->Key.SPEED_ICON;case WEAPON_BOX->Key.WEAPON_ICON;default->throw new IllegalArgumentException();});}
}
