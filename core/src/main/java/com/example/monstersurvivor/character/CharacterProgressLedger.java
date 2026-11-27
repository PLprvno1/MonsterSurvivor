package com.example.monstersurvivor.character;

import com.example.monstersurvivor.config.GameMode;
import com.example.monstersurvivor.consumable.PotionType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Small persistence-independent ledger; Android commits each successful mutation atomically. */
public final class CharacterProgressLedger {
    public enum PotionPurchaseResult { PURCHASED, INSUFFICIENT_COINS, INVENTORY_FULL, SAVE_FAILED }
    private int coins;
    private final HashSet<String> owned=new HashSet<>(),settledMatches=new HashSet<>();
    private final int[] potions=new int[PotionType.values().length];
    private String selected;
    public CharacterProgressLedger(int coins,Set<String> owned,String selected,Set<String> settledMatches) {
        this(coins,owned,selected,settledMatches,null);
    }
    public CharacterProgressLedger(int coins,Set<String> owned,String selected,Set<String> settledMatches,int[] potionCounts) {
        this.coins=Math.max(0,coins);
        if(potionCounts!=null)for(PotionType type:PotionType.values()) {
            int value=type.ordinal()<potionCounts.length?potionCounts[type.ordinal()]:0;
            potions[type.ordinal()]=Math.max(0,Math.min(PotionType.MAX_INVENTORY,value));
        }
        if(owned!=null)for(String id:owned)if(CharacterOwnership.known(id))this.owned.add(CharacterId.fromStableId(id).stableId());
        this.owned.add(CharacterId.WARRIOR.stableId());
        this.selected=CharacterOwnership.isOwned(selected,this.owned)?CharacterId.fromStableId(selected).stableId():CharacterId.WARRIOR.stableId();
        if(settledMatches!=null)for(String id:settledMatches) {
            String canonical=canonicalUuid(id);if(canonical!=null)this.settledMatches.add(canonical);
        }
    }
    public int coins(){return coins;}
    public String selected(){return selected;}
    public Set<String> owned(){return Collections.unmodifiableSet(new HashSet<>(owned));}
    public Set<String> settledMatches(){return Collections.unmodifiableSet(new HashSet<>(settledMatches));}
    public int potionCount(PotionType type){return type==null?0:potions[type.ordinal()];}
    public int[] potionCounts(){return potions.clone();}
    public boolean isOwned(String id){return CharacterOwnership.isOwned(id,owned);}
    public boolean select(String id) {
        if(!CharacterOwnership.isOwned(id,owned))return false;
        selected=CharacterId.fromStableId(id).stableId();return true;
    }
    public boolean purchase(String id) {
        if(!CharacterOwnership.known(id))return false;
        CharacterId character=CharacterId.fromStableId(id);int price=CharacterOwnership.price(character);
        if(character==CharacterId.WARRIOR || owned.contains(character.stableId()) || coins<price)return false;
        coins-=price;owned.add(character.stableId());selected=character.stableId();return true;
    }
    /** Debits the existing wallet and adds one potion in the same in-memory transaction. */
    public PotionPurchaseResult purchasePotion(PotionType type) {
        if(type==null)return PotionPurchaseResult.INVENTORY_FULL;
        int index=type.ordinal();
        if(potions[index]>=PotionType.MAX_INVENTORY)return PotionPurchaseResult.INVENTORY_FULL;
        if(coins<type.price())return PotionPurchaseResult.INSUFFICIENT_COINS;
        coins-=type.price();potions[index]++;return PotionPurchaseResult.PURCHASED;
    }
    public boolean consumePotion(PotionType type) {
        if(type==null || potions[type.ordinal()]<=0)return false;
        potions[type.ordinal()]--;return true;
    }
    /** One UUID can be settled once; even a zero-coin result consumes its settlement identity. */
    public boolean creditRunCoins(String matchUuid,int amount,GameMode mode,boolean diagnostic) {
        String canonical=canonicalUuid(matchUuid);
        if(!eligible(mode,diagnostic) || canonical==null || amount<0 || settledMatches.contains(canonical))return false;
        coins=(int)Math.min(Integer.MAX_VALUE,(long)coins+amount);settledMatches.add(canonical);return true;
    }
    public static boolean eligible(GameMode mode,boolean diagnostic) {
        return !diagnostic && (mode==GameMode.NORMAL || mode==GameMode.ENDLESS);
    }
    public static boolean validUuid(String value) {
        return canonicalUuid(value)!=null;
    }
    private static String canonicalUuid(String value) {
        if(value==null || value.isBlank())return null;
        try {
            String canonical=UUID.fromString(value).toString();
            return canonical.equalsIgnoreCase(value)?canonical:null;
        } catch(IllegalArgumentException invalid){return null;}
    }
}
