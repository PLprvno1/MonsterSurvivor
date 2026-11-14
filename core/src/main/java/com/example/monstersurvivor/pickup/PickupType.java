package com.example.monstersurvivor.pickup;

/** Only functional items are registered. Task 03B can add a type and its collection handler. */
public enum PickupType {
    DAMAGE_BOX("potion_red","D",1,.25f,.25f,true),
    HEALTH_BOX("potion_green","H",.3f,1,.45f,true),
    SPEED_BOX("potion_blue","S",.25f,.7f,1,true),
    HEALTH_DROP("potion_green","+",.3f,1,.45f,false),
    WEAPON_BOX("pickup_chest","W",.85f,.4f,1,true);
    public final String region,label;
    public final float r,g,b;
    public final boolean box;
    PickupType(String region,String label,float r,float g,float b,boolean box) {
        this.region=region;this.label=label;this.r=r;this.g=g;this.b=b;this.box=box;
    }
}
