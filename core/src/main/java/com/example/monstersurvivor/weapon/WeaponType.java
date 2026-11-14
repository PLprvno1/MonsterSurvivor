package com.example.monstersurvivor.weapon;

public enum WeaponType {
    SWORD("Sword","SW",.85f,.9f,1), BOW("Bow","BOW",.6f,1,.65f),
    FIREBALL("Fireball","FIRE",1,.55f,.25f), LIGHTNING("Lightning","LTN",.6f,.8f,1);
    public final String title,abbreviation;
    public final float r,g,b;
    WeaponType(String title,String abbreviation,float r,float g,float b) {
        this.title=title;this.abbreviation=abbreviation;this.r=r;this.g=g;this.b=b;
    }
}
