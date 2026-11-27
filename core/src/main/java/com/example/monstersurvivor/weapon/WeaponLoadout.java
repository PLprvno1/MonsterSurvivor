package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.config.WeaponConfig;

/** Three unique weapon slots, initially populated by the selected class weapon. */
public final class WeaponLoadout {
    private final WeaponInstance[] slots=new WeaponInstance[WeaponConfig.SLOTS];
    private final WeaponConfig config;
    private final WeaponType starter;
    private final boolean swordAvailable;
    private int size,revision;
    private java.util.function.IntSupplier passiveSlotCount=()->size;
    public WeaponLoadout(WeaponConfig config) {this(config,WeaponType.SWORD);}
    public WeaponLoadout(WeaponConfig config,WeaponType starter) {
        if(starter==null)throw new IllegalArgumentException("Starter weapon required");
        this.config=config;this.starter=starter;this.swordAvailable=starter==WeaponType.SWORD;reset();
    }
    public void reset() {java.util.Arrays.fill(slots,null);size=0;revision++;grant(starter);}
    public int size() {return size;}
    public int revision() {return revision;}
    /** Supplies the shared Weapon + Class Skill slot count, including the character starter. */
    public void passiveSlotCount(java.util.function.IntSupplier count) {passiveSlotCount=count==null?()->size:count;}
    public int evolvedCount() {int count=0;for(int i=0;i<size;i++)if(slots[i].evolved())count++;return count;}
    public String describe() {
        StringBuilder text=new StringBuilder();for(int i=0;i<size;i++) {if(i>0)text.append(',');text.append(slots[i].type).append(':').append(slots[i].level());if(slots[i].evolved())text.append(":EVOLVED");}return text.toString();
    }
    public String activity() {
        StringBuilder text=new StringBuilder();for(int i=0;i<size;i++) {if(i>0)text.append(',');text.append(slots[i].type).append(':').append(slots[i].activations);}return text.toString();
    }
    public WeaponInstance slot(int index) {return slots[index];}
    public WeaponInstance get(WeaponType type) {for(int i=0;i<size;i++)if(slots[i].type==type)return slots[i];return null;}
    public boolean canGrant(WeaponType type) {
        if(type==null || !classOwns(type))return false;WeaponInstance owned=get(type);
        return owned==null?size<slots.length && passiveSlotCount.getAsInt()<3:owned.level()<WeaponConfig.MAX_LEVEL;
    }
    private boolean classOwns(WeaponType type) {
        return switch(starter) {
            case SWORD -> type==WeaponType.SWORD && swordAvailable;
            case BOW -> type==WeaponType.BOW;
            case FIREBALL -> type==WeaponType.FIREBALL || type==WeaponType.LIGHTNING;
            case LIGHTNING -> type==WeaponType.LIGHTNING;
        };
    }
    public boolean grant(WeaponType type) {
        if(!canGrant(type))return false;
        WeaponInstance owned=get(type);if(owned==null)slots[size++]=new WeaponInstance(type,config);else owned.upgrade();
        revision++;return true;
    }
    public boolean canEvolve(WeaponType type) {var w=type==null?null:get(type);return w!=null && w.evolutionEligible();}
    boolean evolve(WeaponType type) {if(!canEvolve(type))return false;get(type).evolve();revision++;return true;}
}
