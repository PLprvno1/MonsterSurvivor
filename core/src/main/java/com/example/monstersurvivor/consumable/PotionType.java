package com.example.monstersurvivor.consumable;

/** Consumable configuration kept in one place for shop and gameplay. */
public enum PotionType {
    HEALTH("potion_health",10,4,1.25f,8f),
    SPEED("potion_speed",15,0,1.25f,8f),
    COOLDOWN("potion_cooldown",20,0,1.25f,8f);

    public static final int MAX_INVENTORY=9;
    public static final float MAX_COMBINED_COOLDOWN_MULTIPLIER=1.40f;
    private final String preferenceKey;
    private final int price,healing;
    private final float multiplier,duration;
    PotionType(String preferenceKey,int price,int healing,float multiplier,float duration) {
        this.preferenceKey=preferenceKey;this.price=price;this.healing=healing;this.multiplier=multiplier;this.duration=duration;
    }
    public String preferenceKey(){return preferenceKey;}
    public int price(){return price;}
    public int healing(){return healing;}
    public float multiplier(){return multiplier;}
    public float duration(){return duration;}
}
