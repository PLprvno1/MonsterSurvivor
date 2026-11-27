package com.example.monstersurvivor.character;

import java.util.Set;

/** Shared ownership and price rules used by native selection and Core launch validation. */
public final class CharacterOwnership {
    private CharacterOwnership() { }
    public static int price(CharacterId id) {
        if(id==null)return 0;
        return switch(id) {case WARRIOR->0;case ARCHER->100;case MAGE->220;};
    }
    public static boolean known(String stableId) {
        if(stableId==null)return false;
        for(CharacterId id:CharacterId.values())if(id.stableId().equalsIgnoreCase(stableId.trim()))return true;
        return false;
    }
    public static boolean isOwned(String stableId,Set<String> owned) {
        if(!known(stableId) || owned==null)return false;
        for(String id:owned)if(id!=null && id.equalsIgnoreCase(stableId.trim()))return true;
        return false;
    }
    /** Untrusted or locked launch IDs always fall back to the original Warrior. */
    public static CharacterId selectableOrWarrior(String stableId,Set<String> owned) {
        return isOwned(stableId,owned)?CharacterId.fromStableId(stableId):CharacterId.WARRIOR;
    }
}
