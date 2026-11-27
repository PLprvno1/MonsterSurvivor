package com.example.monstersurvivor.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterOwnership;
import com.example.monstersurvivor.character.CharacterProgressLedger;
import com.example.monstersurvivor.config.GameMode;
import com.example.monstersurvivor.consumable.PotionType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Offline wallet and permanent character ownership, isolated from resettable game settings. */
public final class CharacterProgressManager {
    private static final String PREF_NAME="monster_survivor_character_progress";
    private static final String KEY_COINS="coins",KEY_OWNED="owned_character_ids",KEY_SELECTED="selected_character_id",KEY_SETTLED="settled_match_uuids";
    private static CharacterProgressManager instance;
    private final SharedPreferences prefs;

    public enum PurchaseResult { PURCHASED, ALREADY_OWNED, INSUFFICIENT_COINS, INVALID_CHARACTER, SAVE_FAILED }
    private CharacterProgressManager(Context context) {
        prefs=context.getApplicationContext().getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);ensureDefaults();
    }
    public static synchronized CharacterProgressManager getInstance(Context context) {
        if(instance==null)instance=new CharacterProgressManager(context);return instance;
    }
    private synchronized void ensureDefaults() {
        if(prefs.contains(KEY_COINS) && prefs.contains(KEY_OWNED) && prefs.contains(KEY_SELECTED) && prefs.contains(KEY_SETTLED))return;
        SharedPreferences.Editor edit=prefs.edit();
        if(!prefs.contains(KEY_COINS))edit.putInt(KEY_COINS,0);
        if(!prefs.contains(KEY_OWNED))edit.putStringSet(KEY_OWNED,new HashSet<>(Collections.singleton(CharacterId.WARRIOR.stableId())));
        if(!prefs.contains(KEY_SELECTED))edit.putString(KEY_SELECTED,CharacterId.WARRIOR.stableId());
        if(!prefs.contains(KEY_SETTLED))edit.putStringSet(KEY_SETTLED,new HashSet<>());
        edit.commit();
    }
    private synchronized CharacterProgressLedger ledger() {
        ensureDefaults();
        int coins;try{coins=prefs.getInt(KEY_COINS,0);}catch(ClassCastException invalid){coins=0;}
        int[] potions=new int[PotionType.values().length];
        for(PotionType type:PotionType.values())potions[type.ordinal()]=intValue(type.preferenceKey(),0);
        return new CharacterProgressLedger(coins,stringSet(KEY_OWNED),string(KEY_SELECTED,CharacterId.WARRIOR.stableId()),stringSet(KEY_SETTLED),potions);
    }
    private int intValue(String key,int fallback){try{return prefs.getInt(key,fallback);}catch(ClassCastException invalid){return fallback;}}
    private Set<String> stringSet(String key) {
        try {Set<String> value=prefs.getStringSet(key,Collections.emptySet());return value==null?Collections.emptySet():new HashSet<>(value);}
        catch(ClassCastException invalid){return Collections.emptySet();}
    }
    private String string(String key,String fallback) {try{return prefs.getString(key,fallback);}catch(ClassCastException invalid){return fallback;}}
    public synchronized int coins(){return ledger().coins();}
    /** Test-only simulated top-up. No store or payment service is contacted. */
    public synchronized boolean grantVirtualTestCoins(int amount) {
        if(amount<=0)return false;
        long updated=(long)ledger().coins()+amount;
        return updated<=Integer.MAX_VALUE && prefs.edit().putInt(KEY_COINS,(int)updated).commit();
    }
    public synchronized int potionCount(PotionType type){return ledger().potionCount(type);}
    public synchronized int[] potionCounts(){return ledger().potionCounts();}
    public synchronized String selectedCharacterId(){return ledger().selected();}
    public synchronized Set<String> ownedCharacterIds(){return ledger().owned();}
    public synchronized boolean isOwned(String stableId){return ledger().isOwned(stableId);}
    public synchronized boolean selectCharacter(String stableId) {
        CharacterProgressLedger state=ledger();if(!state.select(stableId))return false;
        return prefs.edit().putString(KEY_SELECTED,state.selected()).commit();
    }
    /** Balance and UUID ledger share one synchronous SharedPreferences transaction. */
    public synchronized boolean creditRunCoins(String matchUuid,int amount,GameMode mode,boolean diagnostic,String provenance) {
        if(!"RESULT".equals(provenance) && !"HOME".equals(provenance))return false;
        CharacterProgressLedger state=ledger();if(!state.creditRunCoins(matchUuid,amount,mode,diagnostic))return false;
        return prefs.edit().putInt(KEY_COINS,state.coins()).putStringSet(KEY_SETTLED,new HashSet<>(state.settledMatches())).commit();
    }
    /** Ownership, balance debit, and post-purchase selection are written together. */
    public synchronized PurchaseResult purchase(String stableId) {
        if(!CharacterOwnership.known(stableId))return PurchaseResult.INVALID_CHARACTER;
        CharacterProgressLedger state=ledger();CharacterId id=CharacterId.fromStableId(stableId);
        if(state.isOwned(id.stableId()))return PurchaseResult.ALREADY_OWNED;
        if(state.coins()<CharacterOwnership.price(id))return PurchaseResult.INSUFFICIENT_COINS;
        if(!state.purchase(id.stableId()))return PurchaseResult.INSUFFICIENT_COINS;
        boolean saved=prefs.edit().putInt(KEY_COINS,state.coins())
            .putStringSet(KEY_OWNED,new HashSet<>(state.owned())).putString(KEY_SELECTED,state.selected()).commit();
        return saved?PurchaseResult.PURCHASED:PurchaseResult.SAVE_FAILED;
    }
    /** Wallet debit and potion credit are persisted in one SharedPreferences commit. */
    public synchronized CharacterProgressLedger.PotionPurchaseResult purchasePotion(PotionType type) {
        CharacterProgressLedger state=ledger();
        int oldCoins=state.coins();int[] oldPotions=state.potionCounts();
        CharacterProgressLedger.PotionPurchaseResult result=state.purchasePotion(type);
        if(result!=CharacterProgressLedger.PotionPurchaseResult.PURCHASED)return result;
        SharedPreferences.Editor edit=prefs.edit().putInt(KEY_COINS,state.coins());
        for(PotionType item:PotionType.values())edit.putInt(item.preferenceKey(),state.potionCount(item));
        if(edit.commit())return result;
        SharedPreferences.Editor rollback=prefs.edit().putInt(KEY_COINS,oldCoins);
        for(PotionType item:PotionType.values())rollback.putInt(item.preferenceKey(),oldPotions[item.ordinal()]);
        rollback.commit();return CharacterProgressLedger.PotionPurchaseResult.SAVE_FAILED;
    }
    /** Durable consume used by the gameplay bridge; failed/empty consumes never apply effects. */
    public synchronized boolean consumePotion(PotionType type) {
        CharacterProgressLedger state=ledger();int[] oldPotions=state.potionCounts();if(!state.consumePotion(type))return false;
        SharedPreferences.Editor edit=prefs.edit();
        for(PotionType item:PotionType.values())edit.putInt(item.preferenceKey(),state.potionCount(item));
        if(edit.commit())return true;
        SharedPreferences.Editor rollback=prefs.edit();
        for(PotionType item:PotionType.values())rollback.putInt(item.preferenceKey(),oldPotions[item.ordinal()]);
        rollback.commit();return false;
    }
}
