package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.config.WeaponConfig;

/** One owned weapon. Sword's authoritative action clock remains in Player. */
public final class WeaponInstance {
    public final WeaponType type;
    private final WeaponConfig config;
    private int level=1;
    public float cooldownRemaining,retryRemaining;
    public int activations;
    WeaponInstance(WeaponType type,WeaponConfig config) {this.type=type;this.config=config;}
    public int level() {return level;}
    public WeaponConfig.Level tuning() {return config.level(type,level);}
    boolean upgrade() {
        if(level>=WeaponConfig.MAX_LEVEL)return false;level++;
        cooldownRemaining=Math.min(cooldownRemaining,tuning().cooldown);return true;
    }
}
