package com.example.monstersurvivor.config;

/** All pickup balance lives here; clocks use seconds, distances use world units. */
public final class PickupConfig {
    public final float intervalMin,intervalMax,boxLifetime,blinkSeconds,dropLifetime;
    public final int boxCap,dropCap,randomAttempts,fallbackCells;
    public final float spawnMin,spawnMax,dropRadius,easyDropChance,hardDropChance,lowHpBonus,maxHpChance;
    public final int lowHpThreshold,dropHealMin,dropHealMax,healthHeal,maxHpGain,maxHpHeal;
    public final int damageMin,damageMax,initialSpeed,maxSpeed;
    public final float unitsPerSpeed;
    public final float weaponChance;
    public PickupConfig() { this(15,20,60,6,20,.08f,.05f,.04f,1,2); }
    /** Constructor also allows deterministic forced/disabled drops in isolated checks. */
    public PickupConfig(float intervalMin,float intervalMax,float lifetime,int boxCap,int dropCap,
                        float easyChance,float hardChance,float lowHpBonus,int healMin,int healMax) {
        this(intervalMin,intervalMax,lifetime,boxCap,dropCap,easyChance,hardChance,lowHpBonus,healMin,healMax,.33f);
    }
    public PickupConfig(float intervalMin,float intervalMax,float lifetime,int boxCap,int dropCap,
                        float easyChance,float hardChance,float lowHpBonus,int healMin,int healMax,float weaponChance) {
        if(!Float.isFinite(intervalMin) || !Float.isFinite(intervalMax) || intervalMin<=0 || intervalMax<intervalMin
            || !Float.isFinite(lifetime) || lifetime<=0 || lifetime>60 || boxCap<1 || boxCap>6 || dropCap<0 || dropCap>32
            || !probability(easyChance) || !probability(hardChance) || !probability(lowHpBonus) || !probability(weaponChance) || healMin<1 || healMax<healMin)
            throw new IllegalArgumentException("Pickup tuning");
        this.intervalMin=intervalMin;this.intervalMax=intervalMax;boxLifetime=lifetime;this.boxCap=boxCap;this.dropCap=dropCap;
        this.weaponChance=weaponChance;
        easyDropChance=easyChance;hardDropChance=hardChance;this.lowHpBonus=lowHpBonus;dropHealMin=healMin;dropHealMax=healMax;
        blinkSeconds=5;dropLifetime=30;randomAttempts=48;fallbackCells=1024;
        spawnMin=48;spawnMax=280;dropRadius=96;lowHpThreshold=3;
        healthHeal=3;maxHpGain=2;maxHpHeal=2;maxHpChance=.5f;
        damageMin=1;damageMax=3;initialSpeed=10;maxSpeed=20;unitsPerSpeed=8;
    }
    private static boolean probability(float p) {return Float.isFinite(p) && p>=0 && p<=1;}
    public float dropChance(Difficulty difficulty,int hp) {
        return Math.min(1,(difficulty==Difficulty.HARD?hardDropChance:easyDropChance)+(hp<=lowHpThreshold?lowHpBonus:0));
    }
}
