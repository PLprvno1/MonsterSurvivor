package com.example.monstersurvivor.pickup;

/** Simulation-only item. Removed from the live collection before applying its effect. */
public final class Pickup {
    public static final float HALF_WIDTH=5,HALF_HEIGHT=4;
    public final long id;
    public final PickupType type;
    public final float x,y,lifetime;
    public final int heal;
    public float age,effectAge;
    public boolean collected;
    Pickup(long id,PickupType type,float x,float y,float lifetime,int heal) {
        this.id=id;this.type=type;this.x=x;this.y=y;this.lifetime=lifetime;this.heal=heal;
    }
    public boolean blinking(float seconds) {return lifetime-age<=seconds && ((int)(age*8)&1)==0;}
}
