package com.example.monstersurvivor.skill;

import com.example.monstersurvivor.localization.GameStrings;
import java.util.Random;

/** Seeded, token-protected Skill Box offers, shared by Dash/Ultimate and class-specific skills. */
public final class SkillSelection {
    private static final SkillType[] ACTIVE_TYPES=SkillType.values();
    private static final java.util.concurrent.atomic.AtomicLong TOKENS=new java.util.concurrent.atomic.AtomicLong();
    private final SkillOffer[] offers=new SkillOffer[3];
    private final GameStrings strings;
    private final Random random;
    private int count;
    private long pending=-1,token;
    private boolean committed;
    public SkillSelection(GameStrings strings,long seed) {this.strings=strings;random=new Random(seed);}
    /** Compatibility for isolated callers; real sessions always use their seeded constructor. */
    public SkillSelection(GameStrings strings) {this(strings,0x51A11L);}
    public long pendingId() {return pending;}
    public long token() {return token;}
    public int size() {return count;}
    public SkillOffer get(int i) {if(i<0 || i>=count)throw new IndexOutOfBoundsException();return offers[i];}

    /** Active-skill-only compatibility path used by existing isolated Skill Box diagnostics. */
    public boolean open(long id,ActiveSkillSystem active) {
        if(pending!=-1 || active==null || !active.hasUpgrade())return false;
        count=0;for(SkillType skill:ACTIVE_TYPES)if(active.canUpgrade(skill))offers[count++]=new SkillOffer(skill,active,strings);
        pending=id;token=-TOKENS.incrementAndGet();committed=false;return count>0;
    }

    public boolean open(long id,ActiveSkillSystem active,ClassSkillSystem classSkills) {
        if(pending!=-1 || active==null || classSkills==null || (!active.hasUpgrade() && !classSkills.hasUpgrade()))return false;
        ClassSkillId[] classPool=new ClassSkillId[ClassSkillId.values().length];int classCount=0,newCount=0;
        for(ClassSkillId skill:ClassSkillId.forCharacter(classSkills.character()))if(classSkills.canUpgrade(skill)) {
            classPool[classCount++]=skill;if(classSkills.level(skill)==0)newCount++;
        }
        SkillType[] activePool=new SkillType[ACTIVE_TYPES.length];int activeCount=0;
        for(SkillType skill:ACTIVE_TYPES)if(active.canUpgrade(skill))activePool[activeCount++]=skill;
        count=0;
        // When a class slot is open, guarantee one new class skill if any are available.
        if(classSkills.size()<ClassSkillSystem.MAX_SKILL_SLOTS && newCount>0) {
            int chosen=randomNew(classPool,classCount,classSkills);add(classPool[chosen],classSkills);classPool[chosen]=null;classCount--;
        } else if(classCount>0) {
            int chosen=random.nextInt(classCount);add(classPool[chosen],classSkills);classPool[chosen]=null;classCount--;
        }
        // Keep Dash/Ultimate in the reward choices whenever either remains upgradeable.
        if(activeCount>0) {int chosen=random.nextInt(activeCount);add(activePool[chosen],active);activePool[chosen]=null;activeCount--;}
        while(count<offers.length && (classCount>0 || activeCount>0)) {
            int total=classCount+activeCount,chosen=random.nextInt(total);
            if(chosen<classCount) {
                int at=availableIndex(classPool,chosen);add(classPool[at],classSkills);classPool[at]=null;classCount--;
            } else {
                int at=availableIndex(activePool,chosen-classCount);add(activePool[at],active);activePool[at]=null;activeCount--;
            }
        }
        if(count==0)return false;
        pending=id;token=-TOKENS.incrementAndGet();committed=false;return true;
    }
    private int randomNew(ClassSkillId[] pool,int count,ClassSkillSystem skills) {
        int matches=0;for(int i=0;i<count;i++)if(skills.level(pool[i])==0)matches++;
        int chosen=random.nextInt(matches);for(int i=0;i<count;i++)if(skills.level(pool[i])==0 && chosen--==0)return i;
        throw new IllegalStateException("Missing class-skill candidate");
    }
    private static int availableIndex(Object[] values,int ordinal) {
        for(int i=0;i<values.length;i++)if(values[i]!=null && ordinal--==0)return i;
        throw new IndexOutOfBoundsException();
    }
    private void add(ClassSkillId skill,ClassSkillSystem skills) {offers[count++]=new SkillOffer(skill,skills,strings);}
    private void add(SkillType skill,ActiveSkillSystem skills) {offers[count++]=new SkillOffer(skill,skills,strings);}
    public boolean commit(int index,long expectedToken,ActiveSkillSystem active,ClassSkillSystem classSkills) {
        if(pending==-1 || committed || token!=expectedToken || index<0 || index>=count)return false;
        SkillOffer offer=offers[index];
        boolean applied=offer.classSkill==null?active.upgrade(offer.type,offer.fromLevel):classSkills.upgrade(offer.classSkill,offer.fromLevel);
        if(!applied)return false;committed=true;return true;
    }
    public boolean commit(int index,long expectedToken,ActiveSkillSystem active) {
        if(pending==-1 || committed || token!=expectedToken || index<0 || index>=count || offers[index].classSkill!=null)return false;
        if(!active.upgrade(offers[index].type,offers[index].fromLevel))return false;committed=true;return true;
    }
    public void clear() {java.util.Arrays.fill(offers,null);count=0;pending=-1;committed=false;token=-TOKENS.incrementAndGet();}
}
