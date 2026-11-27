package com.example.monstersurvivor.config;

import com.example.monstersurvivor.weapon.WeaponType;

/** Immutable evolution tuning, independent of the unchanged five ordinary levels. */
public final class EvolutionConfig {
    public static final int ORBIT_BLADES=2, ORBIT_TRACKED=64, FIRE_ZONES=3, WEB_BRANCHES=3, WEB_TARGETS=8;
    public static final int ORBIT_HITS_PER_STEP=6, METEOR_TARGETS=12, FIRE_TICK_TARGETS=12;
    public static final float ORBIT_RADIUS=42, ORBIT_HALF_WIDTH=9, ORBIT_SPEED=3.5f, ORBIT_HIT_COOLDOWN=.55f;
    public static final float FIRE_DURATION=2.25f, FIRE_TICK=.5f;
    public static final int WEB_CHAIN_PER_BRANCH=2;
    private static final WeaponConfig.Level[] LEVELS={
        new WeaponConfig.Level(1.5f,2,ORBIT_HIT_COOLDOWN,56,0,0,0,1,0),
        new WeaponConfig.Level(1.5f,2,1.3f,260,350,1.25f,0,2,0,5,1,1.2f,1,2,9),
        new WeaponConfig.Level(9,4,2.3f,230,185,2,58,1,0,1,1,2,1.6f,2,0),
        new WeaponConfig.Level(3.6f,1,2.9f,215,0,0,0,WEB_TARGETS,94,1,WEB_BRANCHES,1.5f,1.5f,3,0)
    };
    public static WeaponConfig.Level level(WeaponType type) {return LEVELS[type.ordinal()];}
    public static int burnDamage(int base) {return (int)Math.min(Integer.MAX_VALUE,Math.max(1,Math.round(base*.85d+1)));}
    private EvolutionConfig() {}
}
