package com.example.monstersurvivor.skill;

/** Exact level tables for class skills. No WeaponConfig level is applied here. */
public final class ClassSkillTuning {
    public static final int MAX_LEVEL=5;
    private static final float[] W1_COOLDOWN={7,6.5f,6,5.5f,5},W1_RADIUS={30,34,38,42,46};
    private static final int[] W1_DAMAGE={1,1,2,2,3};
    private static final float[] W2_COOLDOWN={9,8.5f,8,7.5f,7};
    private static final int[] W2_DAMAGE={1,1,2,2,3},W2_PIERCE={2,2,3,3,4};
    private static final float[] W3_COOLDOWN={14,13,12,11,10};
    private static final int[] W3_DAMAGE={2,2,3,3,4};
    private static final float[] W4_COOLDOWN={18,17,16,15,14},W4_DURATION={4,4.5f,5,5.5f,6},W4_RADIUS={60,60,70,70,70};
    private static final int[] W4_DAMAGE={1,2,2,2,3},W4_KNOCKBACK={8,8,8,8,12},W4_REDUCTION={10,12,14,16,18};
    private static final float[] A1_COOLDOWN={12,11,10,9,8},A1_DURATION={3,3.5f,4,4.5f,5};
    private static final int[] A1_CHARGES={1,2,3,4,5};
    private static final float[] A2_COOLDOWN={9,8.5f,8,7.5f,7};
    private static final int[] A2_DAMAGE={3,3,4,5,6};
    private static final float[] A3_COOLDOWN={17,16,15,14,13},A3_RADIUS={28,30,32,34,36};
    private static final int[] A3_PULSES={3,3,3,4,4};
    private static final float[] A4_COOLDOWN={13,12,11,10,9},A4_POISON_DURATION={2,2,2.5f,2.5f,3};
    private static final int[] A4_DAMAGE={1,1,2,2,3},A4_POISON_TICKS={2,2,2,2,3};
    private static final float[] M1_COOLDOWN={7.5f,7,6.5f,6,5.5f},M1_SLOW_DURATION={1.5f,1.7f,1.9f,2.1f,2.3f};
    private static final int[] M1_DAMAGE={1,1,2,2,3},M1_SLOW_PERCENT={20,22,25,27,30};
    private static final float[] M2_COOLDOWN={14,13,12,11,10},M2_RADIUS={25,28,31,34,37};
    private static final int[] M2_DAMAGE={2,2,3,3,4};
    private static final float[] M3_COOLDOWN={20,18,16,14,12};
    private static final float[] M4_COOLDOWN={18,17,16,15,14},M4_DURATION={3,3.5f,4,4.5f,5},M4_FIREBALL_MULTIPLIER={.90f,.85f,.80f,.75f,.70f};
    private ClassSkillTuning() { }
    private static int index(int level) {if(level<1 || level>MAX_LEVEL)throw new IllegalArgumentException("Skill level 1..5");return level-1;}
    public static float cooldown(ClassSkillId id,int level) {
        int i=index(level);return switch(id) {
            case WARRIOR_WHIRLWIND -> W1_COOLDOWN[i];
            case WARRIOR_SWORD_WAVE -> W2_COOLDOWN[i];
            case WARRIOR_PARRY -> W3_COOLDOWN[i];
            case WARRIOR_WAR_CRY -> W4_COOLDOWN[i];
            case ARCHER_MARK -> A1_COOLDOWN[i];
            case ARCHER_FOCUS -> A2_COOLDOWN[i];
            case ARCHER_RAIN -> A3_COOLDOWN[i];
            case ARCHER_POISON_ARROW -> A4_COOLDOWN[i];
            case MAGE_ICE_SHARD -> M1_COOLDOWN[i];
            case MAGE_ARCANE_TRAP -> M2_COOLDOWN[i];
            case MAGE_SHIELD -> M3_COOLDOWN[i];
            case MAGE_CHANNEL -> M4_COOLDOWN[i];
            default -> throw new IllegalArgumentException("No Task 12B2a tuning for "+id);
        };
    }
    public static int damage(ClassSkillId id,int level) {
        int i=index(level);return switch(id) {
            case WARRIOR_WHIRLWIND -> W1_DAMAGE[i];
            case WARRIOR_SWORD_WAVE -> W2_DAMAGE[i];
            case WARRIOR_PARRY -> W3_DAMAGE[i];
            case WARRIOR_WAR_CRY -> W4_DAMAGE[i];
            case ARCHER_FOCUS -> A2_DAMAGE[i];
            case ARCHER_RAIN -> 1;
            case ARCHER_POISON_ARROW -> A4_DAMAGE[i];
            case ARCHER_MARK -> 1;
            case MAGE_ICE_SHARD -> M1_DAMAGE[i];
            case MAGE_ARCANE_TRAP -> M2_DAMAGE[i];
            case MAGE_SHIELD,MAGE_CHANNEL -> 0;
            default -> throw new IllegalArgumentException("No Task 12B2a tuning for "+id);
        };
    }
    public static float whirlwindRadius(int level) {return W1_RADIUS[index(level)];}
    public static float whirlwindSecondPulseDelay() {return .28f;}
    public static int wavePierce(int level) {return W2_PIERCE[index(level)];}
    public static float waveFanCos() {return .9781476f;}
    public static float waveFanSin() {return .20791169f;}
    public static float waveRange() {return 120;}
    public static float waveSpeed() {return 145;}
    public static float waveLifetime() {return .9f;}
    public static float parryRadius() {return 28;}
    public static float parryBreakoutRadius() {return 34;}
    public static float parryHalfAngleCos() {return .34202015f;}
    public static float warCryTriggerRadius() {return 70;}
    public static float warCryBreakoutRadius() {return 40;}
    public static float warCryRadius(int level) {return W4_RADIUS[index(level)];}
    public static float warCryDuration(int level) {return W4_DURATION[index(level)];}
    public static int warCryKnockback(int level) {return W4_KNOCKBACK[index(level)];}
    public static int warCryDamageReductionPercent(int level) {return W4_REDUCTION[index(level)];}
    public static float markDuration(int level) {return A1_DURATION[index(level)];}
    public static int markCharges(int level) {return A1_CHARGES[index(level)];}
    public static float markRange() {return 130;}
    public static float markTransferRange() {return 65;}
    public static float focusRange() {return 170;}
    public static float focusSpeed() {return 180;}
    public static float rainSearchRange() {return 125;}
    public static float rainRadius(int level) {return A3_RADIUS[index(level)];}
    public static int rainPulses(int level) {return A3_PULSES[index(level)];}
    public static int evolvedRainPulses(int level) {return A3_PULSES[index(level)]+1;}
    public static int rainTargetHitCap(boolean evolved) {return evolved?3:2;}
    public static float rainWarning() {return .4f;}
    public static float rainPulseInterval() {return .22f;}
    public static float poisonArrowRange() {return 150;}
    public static float poisonArrowSpeed() {return 165;}
    public static float poisonDuration(int level) {return A4_POISON_DURATION[index(level)];}
    public static int poisonTickDamage() {return 1;}
    public static int poisonTicks(int level) {return A4_POISON_TICKS[index(level)];}
    public static float poisonSpreadRadius() {return 45;}
    public static float iceRange() {return 150;}
    public static float iceSpeed() {return 160;}
    public static int iceSplinterCount() {return 3;}
    public static float iceSplinterHalfAngleDegrees() {return 45;}
    public static int iceSlowPercent(int level) {return M1_SLOW_PERCENT[index(level)];}
    public static float iceSlowDuration(int level) {return M1_SLOW_DURATION[index(level)];}
    public static float trapRange() {return 120;}
    public static float trapArmDelay() {return .5f;}
    public static int trapBreakoutPulses() {return 2;}
    public static float trapBreakoutInterval() {return .35f;}
    public static float trapLifetime() {return 3.5f;}
    public static float trapTriggerRadius() {return 19;}
    public static float trapExplosionRadius(int level) {return M2_RADIUS[index(level)];}
    public static int trapCapacity() {return 2;}
    public static float shieldEffectDuration() {return .35f;}
    public static float shieldBreakoutRadius() {return 30;}
    public static int shieldBreakoutDamage() {return 1;}
    public static float channelRange() {return 130;}
    public static float channelDuration(int level) {return M4_DURATION[index(level)];}
    public static float channelFireballCooldownMultiplier(int level) {return M4_FIREBALL_MULTIPLIER[index(level)];}
    public static int channelDirectDamageBonus() {return 1;}
}
