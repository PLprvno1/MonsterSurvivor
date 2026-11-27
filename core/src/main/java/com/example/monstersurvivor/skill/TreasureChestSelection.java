package com.example.monstersurvivor.skill;

import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.localization.GameStrings;
import com.example.monstersurvivor.weapon.WeaponLoadout;
import com.example.monstersurvivor.weapon.WeaponOffer;
import com.example.monstersurvivor.weapon.WeaponType;
import java.util.Random;

/** One token-protected chest pipeline for passive and active skill rewards. */
public final class TreasureChestSelection {
    public enum Branch { PASSIVE, ACTIVE, FALLBACK }
    private static final java.util.concurrent.atomic.AtomicLong TOKENS=new java.util.concurrent.atomic.AtomicLong();
    private static final SkillType[] ACTIVE_TYPES=SkillType.values();
    private final TreasureChestOffer[] offers=new TreasureChestOffer[3];
    private final TreasureChestOffer[] pool=new TreasureChestOffer[ClassSkillId.values().length+WeaponType.values().length+3];
    private final GameStrings strings;
    private final Random random;
    private int count,poolCount;
    private long pending=-1,token;
    private boolean committed;
    private Branch branch=Branch.PASSIVE;

    public TreasureChestSelection(GameStrings strings,long seed) {this.strings=strings;random=new Random(seed^0x7EA5C4E57L);}
    public long pendingId(){return pending;}
    public long token(){return token;}
    public int size(){return count;}
    public Branch branch(){return branch;}
    public TreasureChestOffer get(int index){if(index<0 || index>=count)throw new IndexOutOfBoundsException();return offers[index];}

    public static boolean hasReward(WeaponLoadout loadout,ClassSkillSystem classSkills,ActiveSkillSystem active,Player player) {
        if(loadout==null || classSkills==null || active==null || player==null || !player.alive())return false;
        if(hasPassive(loadout,classSkills) || active.hasUpgrade())return true;
        return hasFallback(loadout,player);
    }
    private static boolean hasPassive(WeaponLoadout loadout,ClassSkillSystem classSkills) {
        for(WeaponType type:WeaponType.values())if(loadout.canGrant(type))return true;
        if(loadout.evolvedCount()+classSkills.evolvedCount()<3)
            for(WeaponType type:WeaponType.values())if(loadout.canEvolve(type))return true;
        for(ClassSkillId id:ClassSkillId.forCharacter(classSkills.character()))if(classSkills.canEvolve(id))return true;
        return classSkills.hasUpgrade();
    }
    private static boolean hasFallback(WeaponLoadout loadout,Player player) {
        return WeaponOffer.stat(WeaponOffer.Kind.DAMAGE).valid(loadout,player)
            || WeaponOffer.stat(WeaponOffer.Kind.MAX_HP).valid(loadout,player)
            || WeaponOffer.stat(WeaponOffer.Kind.SPEED).valid(loadout,player);
    }

    public boolean open(long id,WeaponLoadout loadout,Player player,ActiveSkillSystem active,ClassSkillSystem classSkills) {
        if(pending!=-1 || !player.alive())return false;
        count=0;poolCount=0;
        boolean passiveFirst=random.nextInt(100)<65;
        boolean built=passiveFirst?buildPassive(loadout,player,classSkills):buildActive(active);
        if(built)branch=passiveFirst?Branch.PASSIVE:Branch.ACTIVE;
        else {
            built=passiveFirst?buildActive(active):buildPassive(loadout,player,classSkills);
            if(built)branch=passiveFirst?Branch.ACTIVE:Branch.PASSIVE;
        }
        if(!built) {built=buildFallback(loadout,player);branch=Branch.FALLBACK;}
        if(!built)return false;
        pending=id;token=TOKENS.incrementAndGet();committed=false;return true;
    }
    private boolean buildPassive(WeaponLoadout loadout,Player player,ClassSkillSystem classSkills) {
        poolCount=0;
        boolean canEvolve=loadout.evolvedCount()+classSkills.evolvedCount()<3;
        if(canEvolve)for(WeaponType type:WeaponType.values())if(loadout.canEvolve(type))
            pool[poolCount++]=TreasureChestOffer.weapon(WeaponOffer.evolution(type,strings));
        if(canEvolve)for(ClassSkillId id:ClassSkillId.forCharacter(classSkills.character()))if(classSkills.canEvolve(id))
            pool[poolCount++]=TreasureChestOffer.skill(new SkillOffer(id,strings));
        int evolutionCount=poolCount;
        for(WeaponType type:WeaponType.values())if(loadout.canGrant(type))
            pool[poolCount++]=TreasureChestOffer.weapon(WeaponOffer.weapon(type,loadout,strings));
        for(ClassSkillId id:ClassSkillId.forCharacter(classSkills.character()))if(classSkills.canUpgrade(id))
            pool[poolCount++]=TreasureChestOffer.skill(new SkillOffer(id,classSkills,strings));
        count=0;
        if(evolutionCount>0)takeRandom(0,evolutionCount);
        while(count<offers.length && poolCount>0)takeRandom(0,poolCount);
        return count>0;
    }
    private boolean buildActive(ActiveSkillSystem active) {
        poolCount=0;count=0;
        for(SkillType type:ACTIVE_TYPES)if(active.canUpgrade(type))pool[poolCount++]=TreasureChestOffer.skill(new SkillOffer(type,active,strings));
        while(count<offers.length && poolCount>0)takeRandom(0,poolCount);
        return count>0;
    }
    private boolean buildFallback(WeaponLoadout loadout,Player player) {
        poolCount=0;count=0;
        WeaponOffer.Kind[] kinds={WeaponOffer.Kind.DAMAGE,WeaponOffer.Kind.MAX_HP,WeaponOffer.Kind.SPEED};
        for(WeaponOffer.Kind kind:kinds) {
            WeaponOffer offer=WeaponOffer.stat(kind,strings);
            if(offer.valid(loadout,player))pool[poolCount++]=TreasureChestOffer.weapon(offer);
        }
        while(count<offers.length && poolCount>0)takeRandom(0,poolCount);
        return count>0;
    }
    private void takeRandom(int from,int bound) {
        int selected=from+random.nextInt(bound-from);TreasureChestOffer offer=pool[selected];
        offers[count++]=offer;pool[selected]=pool[--poolCount];pool[poolCount]=null;
    }
    public boolean commit(int index,long expectedToken,WeaponLoadout loadout,Player player,
                          ActiveSkillSystem active,ClassSkillSystem classSkills) {
        if(pending<0 || committed || token!=expectedToken || index<0 || index>=count || !player.alive())return false;
        TreasureChestOffer offer=offers[index];boolean applied;
        if(offer.kind==TreasureChestOffer.Kind.EVOLUTION && loadout.evolvedCount()+classSkills.evolvedCount()>=3)return false;
        if(offer.skill!=null && offer.skill.classSkill!=null)
            applied=offer.skill.evolution?classSkills.markEvolved(offer.skill.classSkill):classSkills.upgrade(offer.skill.classSkill,offer.fromLevel);
        else if(offer.weapon!=null)applied=offer.weapon.apply(loadout,player);
        else applied=active.upgrade(offer.skill.type,offer.fromLevel);
        if(!applied)return false;committed=true;return true;
    }
    public void clear(){java.util.Arrays.fill(offers,null);java.util.Arrays.fill(pool,null);count=poolCount=0;pending=-1;committed=false;token=TOKENS.incrementAndGet();}
}
