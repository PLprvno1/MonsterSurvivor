package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.entity.Player;
import java.util.Random;

/** Offers and a pending item identity, owned by the session, independent of libGDX UI. */
public final class WeaponSelection {
    private final WeaponOffer[] offers=new WeaponOffer[3];
    private static final WeaponType[] TYPES=WeaponType.values();
    private final WeaponType[] candidates=new WeaponType[TYPES.length];
    private final Random random;
    private final com.example.monstersurvivor.localization.GameStrings strings;
    // UI callbacks may outlive Retry; tokens must not repeat in a different session.
    private static final java.util.concurrent.atomic.AtomicLong NEXT_TOKEN=new java.util.concurrent.atomic.AtomicLong();
    private int count;
    private long pending=-1,token;
    public WeaponSelection(long seed) {this(seed,com.example.monstersurvivor.localization.GameStrings.of(com.example.monstersurvivor.localization.GameLanguage.VI));}
    public WeaponSelection(long seed,com.example.monstersurvivor.localization.GameStrings strings) {this.strings=strings;random=new Random(seed^0x35B100L);}
    public long pendingId() {return pending;}
    public long token() {return token;}
    public int size() {return count;}
    public WeaponOffer get(int index) {if(index<0 || index>=count)throw new IndexOutOfBoundsException();return offers[index];}
    public static boolean hasReward(WeaponLoadout loadout,Player player) {
        if(!player.alive())return false;
        for(WeaponType type:TYPES)if(loadout.canGrant(type) || loadout.canEvolve(type))return true;
        return player.stats.damage()<Integer.MAX_VALUE || player.maxHp()<Integer.MAX_VALUE || player.stats.speed()<20;
    }
    public boolean open(long id,WeaponLoadout loadout,Player player) {
        if(pending!=-1 || !player.alive())return false;
        pending=id;token=NEXT_TOKEN.incrementAndGet();count=0;int n=0;
        // Every eligible owned evolution is guaranteed on this next box (at most three).
        for(WeaponType type:TYPES)if(loadout.canEvolve(type))offers[count++]=WeaponOffer.evolution(type,strings);
        for(WeaponType type:TYPES)if(loadout.canGrant(type) && (loadout.size()>1 || type!=WeaponType.SWORD))candidates[n++]=type;
        // First box offers all three new weapons; later boxes randomize eligible unique types.
        if(loadout.size()>1)for(int i=n-1;i>0;i--) {int j=random.nextInt(i+1);WeaponType t=candidates[i];candidates[i]=candidates[j];candidates[j]=t;}
        for(int i=0;i<n && count<offers.length;i++)offers[count++]=WeaponOffer.weapon(candidates[i],loadout,strings);
        for(WeaponOffer.Kind kind:STAT_KINDS) {
            if(count==offers.length)break;WeaponOffer stat=WeaponOffer.stat(kind,strings);if(stat.valid(loadout,player))offers[count++]=stat;
        }
        if(count==0) {clear();return false;}return true;
    }
    private static final WeaponOffer.Kind[] STAT_KINDS={WeaponOffer.Kind.DAMAGE,WeaponOffer.Kind.MAX_HP,WeaponOffer.Kind.SPEED};
    public void clear() {java.util.Arrays.fill(offers,null);pending=-1;count=0;token=NEXT_TOKEN.incrementAndGet();}
}
