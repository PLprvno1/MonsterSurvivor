package com.example.monstersurvivor.entity;

import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.config.PickupConfig;

/** Mutable match upgrades. HP and the no-resurrection guard remain owned by Player. */
public final class PlayerStats {
    private int damage,maxHp,speed;
    private final PickupConfig tuning;
    public PlayerStats(GameConfig config) {
        damage=config.swordDamage;maxHp=config.playerHp;tuning=config.pickups;speed=tuning.initialSpeed;
    }
    public int damage() {return damage;}
    public int maxHp() {return maxHp;}
    public int speed() {return speed;}
    public float movementSpeed() {return speed*tuning.unitsPerSpeed;}
    public int addDamage(int amount) {int old=damage;damage=add(damage,amount);return damage-old;}
    public int addMaxHp(int amount) {int old=maxHp;maxHp=add(maxHp,amount);return maxHp-old;}
    public int addSpeed() {int old=speed;speed=Math.min(tuning.maxSpeed,speed+1);return speed-old;}
    // No gameplay damage/HP cap; saturating integer arithmetic prevents overflow.
    private static int add(int value,int amount) {return (int)Math.min(Integer.MAX_VALUE,(long)value+Math.max(0,amount));}
}
