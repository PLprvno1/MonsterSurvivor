package com.example.monstersurvivor.skill;

import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.localization.GameStrings;

/** Stable class-skill catalog; implementation is enabled by character phase. */
public enum ClassSkillId {
    WARRIOR_WHIRLWIND("warrior_whirlwind", CharacterId.WARRIOR),
    WARRIOR_SWORD_WAVE("warrior_wave", CharacterId.WARRIOR),
    WARRIOR_PARRY("warrior_parry", CharacterId.WARRIOR),
    WARRIOR_WAR_CRY("warrior_war_cry", CharacterId.WARRIOR),
    ARCHER_MARK("archer_mark", CharacterId.ARCHER),
    ARCHER_FOCUS("archer_focus", CharacterId.ARCHER),
    ARCHER_RAIN("archer_rain", CharacterId.ARCHER),
    ARCHER_POISON_ARROW("archer_poison_arrow", CharacterId.ARCHER),
    MAGE_ICE_SHARD("mage_ice_shard", CharacterId.MAGE),
    MAGE_ARCANE_TRAP("mage_arcane_trap", CharacterId.MAGE),
    MAGE_SHIELD("mage_shield", CharacterId.MAGE),
    MAGE_CHANNEL("mage_channel", CharacterId.MAGE);

    private static final ClassSkillId[] WARRIOR={WARRIOR_WHIRLWIND,WARRIOR_SWORD_WAVE,WARRIOR_PARRY,WARRIOR_WAR_CRY};
    private static final ClassSkillId[] ARCHER={ARCHER_MARK,ARCHER_FOCUS,ARCHER_RAIN,ARCHER_POISON_ARROW};
    // Lightning is the Mage's fourth learnable passive and is backed by WeaponLoadout.
    // Arcane Channel remains in the enum for save/debug compatibility, but is no longer offered.
    private static final ClassSkillId[] MAGE={MAGE_ICE_SHARD,MAGE_ARCANE_TRAP,MAGE_SHIELD};
    private final String stableId;
    public final CharacterId owner;

    ClassSkillId(String stableId,CharacterId owner) {this.stableId=stableId;this.owner=owner;}
    public String stableId() {return stableId;}
    public GameStrings.Key nameKey() {
        return switch(this) {
            case WARRIOR_WHIRLWIND -> GameStrings.Key.CLASS_WHIRLWIND;
            case WARRIOR_SWORD_WAVE -> GameStrings.Key.CLASS_WAVE;
            case WARRIOR_PARRY -> GameStrings.Key.CLASS_PARRY;
            case WARRIOR_WAR_CRY -> GameStrings.Key.CLASS_WAR_CRY;
            case ARCHER_MARK -> GameStrings.Key.CLASS_ARCHER_MARK;
            case ARCHER_FOCUS -> GameStrings.Key.CLASS_ARCHER_FOCUS;
            case ARCHER_RAIN -> GameStrings.Key.CLASS_ARCHER_RAIN;
            case ARCHER_POISON_ARROW -> GameStrings.Key.CLASS_ARCHER_POISON;
            case MAGE_ICE_SHARD -> GameStrings.Key.CLASS_MAGE_ICE;
            case MAGE_ARCANE_TRAP -> GameStrings.Key.CLASS_MAGE_TRAP;
            case MAGE_SHIELD -> GameStrings.Key.CLASS_MAGE_SHIELD;
            case MAGE_CHANNEL -> GameStrings.Key.CLASS_MAGE_CHANNEL;
            default -> throw new IllegalStateException("Skill name is added with its implementation: "+stableId);
        };
    }
    public GameStrings.Key summaryKey() {
        return switch(this) {
            case WARRIOR_WHIRLWIND -> GameStrings.Key.CLASS_WHIRLWIND_SUMMARY;
            case WARRIOR_SWORD_WAVE -> GameStrings.Key.CLASS_WAVE_SPEC;
            case WARRIOR_PARRY -> GameStrings.Key.CLASS_PARRY_SUMMARY;
            case WARRIOR_WAR_CRY -> GameStrings.Key.CLASS_WAR_CRY_SUMMARY;
            case ARCHER_MARK -> GameStrings.Key.CLASS_ARCHER_MARK_SUMMARY;
            case ARCHER_FOCUS -> GameStrings.Key.CLASS_ARCHER_FOCUS_SUMMARY;
            case ARCHER_RAIN -> GameStrings.Key.CLASS_ARCHER_RAIN_SUMMARY;
            case ARCHER_POISON_ARROW -> GameStrings.Key.CLASS_ARCHER_POISON_SUMMARY;
            case MAGE_ICE_SHARD -> GameStrings.Key.CLASS_MAGE_ICE_SUMMARY;
            case MAGE_ARCANE_TRAP -> GameStrings.Key.CLASS_MAGE_TRAP_SUMMARY;
            case MAGE_SHIELD -> GameStrings.Key.CLASS_MAGE_SHIELD_SUMMARY;
            case MAGE_CHANNEL -> GameStrings.Key.CLASS_MAGE_CHANNEL_SUMMARY;
            default -> throw new IllegalStateException("Skill summary is added with its implementation: "+stableId);
        };
    }
    public static ClassSkillId[] forCharacter(CharacterId id) {
        return switch(id==null?CharacterId.WARRIOR:id) {
            case WARRIOR -> WARRIOR.clone();
            case ARCHER -> ARCHER.clone();
            case MAGE -> MAGE.clone();
        };
    }
    public boolean implemented() {return true;}
}
