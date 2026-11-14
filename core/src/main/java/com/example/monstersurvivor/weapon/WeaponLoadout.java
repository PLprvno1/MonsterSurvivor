package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.config.WeaponConfig;

/** Three unique slots, initially Sword 1. All mutation goes through eligibility checks. */
public final class WeaponLoadout {
    private final WeaponInstance[] slots=new WeaponInstance[WeaponConfig.SLOTS];
    private final WeaponConfig config;
    private int size,revision;
    public WeaponLoadout(WeaponConfig config) {this.config=config;reset();}
    public void reset() {java.util.Arrays.fill(slots,null);size=0;revision++;grant(WeaponType.SWORD);}
    public int size() {return size;}
    public int revision() {return revision;}
    public String describe() {
        StringBuilder text=new StringBuilder();for(int i=0;i<size;i++) {if(i>0)text.append(',');text.append(slots[i].type).append(':').append(slots[i].level());}return text.toString();
    }
    public String activity() {
        StringBuilder text=new StringBuilder();for(int i=0;i<size;i++) {if(i>0)text.append(',');text.append(slots[i].type).append(':').append(slots[i].activations);}return text.toString();
    }
    public WeaponInstance slot(int index) {return slots[index];}
    public WeaponInstance get(WeaponType type) {for(int i=0;i<size;i++)if(slots[i].type==type)return slots[i];return null;}
    public boolean canGrant(WeaponType type) {
        if(type==null)return false;WeaponInstance owned=get(type);
        return owned==null?size<slots.length:owned.level()<WeaponConfig.MAX_LEVEL;
    }
    public boolean grant(WeaponType type) {
        if(!canGrant(type))return false;
        WeaponInstance owned=get(type);if(owned==null)slots[size++]=new WeaponInstance(type,config);else owned.upgrade();
        revision++;return true;
    }
}
