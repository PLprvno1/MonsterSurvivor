package com.example.monstersurvivor.character;

/** Stable external IDs; never persist enum ordinals. */
public enum CharacterId {
    WARRIOR("warrior"), ARCHER("archer"), MAGE("mage");

    private final String stableId;

    CharacterId(String stableId) { this.stableId=stableId; }

    public String stableId() { return stableId; }

    /** Missing or malformed IDs safely preserve the original Warrior path. */
    public static CharacterId fromStableId(String id) {
        if(id!=null)for(CharacterId value:values())if(value.stableId.equalsIgnoreCase(id.trim()))return value;
        return WARRIOR;
    }
}
