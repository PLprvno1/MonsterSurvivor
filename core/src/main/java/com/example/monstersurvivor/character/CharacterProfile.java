package com.example.monstersurvivor.character;

import com.example.monstersurvivor.weapon.WeaponType;

/** Immutable class data shared by launch routing and gameplay construction. */
public final class CharacterProfile {
    private static final CharacterProfile WARRIOR=new CharacterProfile(CharacterId.WARRIOR,"player",WeaponType.SWORD,true);
    private static final CharacterProfile ARCHER=new CharacterProfile(CharacterId.ARCHER,"archer",WeaponType.BOW,false);
    private static final CharacterProfile MAGE=new CharacterProfile(CharacterId.MAGE,"mage",WeaponType.FIREBALL,false);

    public final CharacterId id;
    public final String spriteId;
    public final WeaponType startingWeapon;
    public final boolean swordAvailable;

    private CharacterProfile(CharacterId id,String spriteId,WeaponType startingWeapon,boolean swordAvailable) {
        this.id=id;this.spriteId=spriteId;this.startingWeapon=startingWeapon;this.swordAvailable=swordAvailable;
    }

    public static CharacterProfile forId(String id) { return forId(CharacterId.fromStableId(id)); }

    public static CharacterProfile forId(CharacterId id) {
        return switch(id==null?CharacterId.WARRIOR:id) {
            case ARCHER -> ARCHER;
            case MAGE -> MAGE;
            default -> WARRIOR;
        };
    }
}
