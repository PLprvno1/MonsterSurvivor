package com.example.monstersurvivor.skill;

import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.weapon.CombatGeometry;
import com.example.monstersurvivor.world.CollisionGrid;

/** Session-owned automatic class skills; fixed pools keep combat bounded and allocation-free. */
public final class ClassSkillSystem {
    public static final int MAX_SKILL_SLOTS=2,PROJECTILE_CAPACITY=12,FX_CAPACITY=16;
    public enum FxKind { SLASH,SPARK,PARRY_ARC,PARRY_FULL_ARC,ARROW_WARNING,ARROW_PULSE,ICE_IMPACT,TRAP_CAST,TRAP_BLAST,SHIELD,SHIELD_PULSE,CHANNEL_CAST,WAR_CRY,POISON_IMPACT }
    public static final class Fx {
        public boolean active;public FxKind kind;public float x,y,age,duration,size,dirX,dirY;
    }
    public static final class Wave {
        private final int[] hitIds=new int[4];
        public boolean active,arrow,ice,poison,smallIceShard;public float x,y,dx,dy,age,distance,speed,range,lifetime;
        public int volleyId=-1;
        private int damage,pierce,hitCount,slowPercent,poisonDamage,poisonTicks;
        private float slowDuration,poisonDuration;
        public int hitCount(){return hitCount;}
    }
    public static final class Trap {
        public boolean active,pulsing;public float x,y,age,radius,pulseClock;public int damage,pulseIndex;
    }
    private static final class WaveVolley {
        final int[] ids=new int[64],hits=new int[64];boolean active;int count;
        void reset(){java.util.Arrays.fill(ids,0);java.util.Arrays.fill(hits,0);count=0;active=true;}
    }

    private final CharacterId character;
    private final ClassSkillId[] candidates;
    private final int[] levels=new int[ClassSkillId.values().length];
    private final float[] cooldowns=new float[ClassSkillId.values().length];
    private final ClassSkillId[] slots=new ClassSkillId[MAX_SKILL_SLOTS];
    private final boolean[] evolved=new boolean[ClassSkillId.values().length];
    private final Wave[] waves=new Wave[PROJECTILE_CAPACITY];
    private final Wave[] waveScratch=new Wave[3];
    private final Wave[] waveUpdateScratch=new Wave[PROJECTILE_CAPACITY];
    private final WaveVolley[] volleys={new WaveVolley(),new WaveVolley(),new WaveVolley(),new WaveVolley()};
    private final Fx[] effects=new Fx[FX_CAPACITY];
    private final Trap[] traps=new Trap[ClassSkillTuning.trapCapacity()];
    private final int[] rainHitIds=new int[64],rainAcceptedHits=new int[64];
    private int learnedCount;
    private int revision;
    private java.util.function.IntSupplier passiveSlotCount=()->1+learnedCount;
    private java.util.function.IntSupplier evolvedPassiveCount=this::evolvedCount;
    private int markTargetId,markCharges,rainHitCount,rainPulseCount,rainPulsesDone,rainDamage;
    private float markTime,markX,markY,rainAge,rainX,rainY,rainRadius;
    private boolean markTransferred,whirlwindPending;
    private float whirlwindDelay,whirlwindRadius;
    private int whirlwindDamage;
    private float warCryTime,warCryDamageReductionFraction;
    private float channelTime,channelX,channelY,channelCooldownMultiplier=1;
    private int channelBonusDamage;
    private boolean rainActive;
    public int whirlwindActivations,waveFired,waveHits,parryBlocks,parryHits,droppedFx;
    public int markApplied,markBonusHits,focusFired,focusHits,rainActivations,rainPulses,rainHits;
    public int iceFired,iceHits,iceSlows,trapPlacements,trapExplosions,trapHits,shieldBlocks,channelActivations;
    public int warCryActivations,warCryHits,warCryKnockbacks,poisonFired,poisonHits,poisonApplications;
    public int evolvedActivations,evolvedHits,iceShardSplinters,trapPulseHits,shieldPulseHits,poisonSpreads,markTransfers;

    public ClassSkillSystem(CharacterId character) {
        this.character=character==null?CharacterId.WARRIOR:character;
        candidates=ClassSkillId.forCharacter(this.character);
        for(int i=0;i<waves.length;i++)waves[i]=new Wave();
        for(int i=0;i<effects.length;i++)effects[i]=new Fx();
        for(int i=0;i<traps.length;i++)traps[i]=new Trap();
    }
    public CharacterId character(){return character;}
    public int size(){return learnedCount;}
    public int revision(){return revision;}
    /** Supplies the shared Weapon + Class Skill slot count. The starter passive occupies slot one. */
    public void passiveSlotCount(java.util.function.IntSupplier count){passiveSlotCount=count==null?()->1+learnedCount:count;}
    /** Shared cap hook used when future class passives gain an evolved state. */
    public void evolvedPassiveCount(java.util.function.IntSupplier count){evolvedPassiveCount=count==null?this::evolvedCount:count;}
    public int evolvedCount(){int count=0;for(boolean value:evolved)if(value)count++;return count;}
    public boolean evolved(ClassSkillId id){return id!=null && evolved[id.ordinal()];}
    public boolean canEvolve(ClassSkillId id){return ownsCandidate(id) && id!=ClassSkillId.MAGE_CHANNEL && level(id)==ClassSkillTuning.MAX_LEVEL && !evolved(id) && evolvedPassiveCount.getAsInt()<3;}
    boolean markEvolved(ClassSkillId id){if(!canEvolve(id))return false;evolved[id.ordinal()]=true;revision++;return true;}
    public ClassSkillId slot(int index){if(index<0 || index>=learnedCount)throw new IndexOutOfBoundsException();return slots[index];}
    public int level(ClassSkillId id){return id==null?0:levels[id.ordinal()];}
    public float cooldownRemaining(ClassSkillId id){return id==null?0:cooldowns[id.ordinal()];}
    public boolean ready(ClassSkillId id){return level(id)>0 && cooldownRemaining(id)<=0;}
    public int projectileCount(){int count=0;for(Wave wave:waves)if(wave.active)count++;return count;}
    public int projectileCapacity(){return waves.length;}
    public Wave projectile(int index){return waves[index];}
    public int effectCount(){int count=0;for(Fx fx:effects)if(fx.active)count++;return count;}
    public int effectCapacity(){return effects.length;}
    public Fx effect(int index){return effects[index];}
    public int trapCapacity(){return traps.length;}
    public int trapCount(){int count=0;for(Trap trap:traps)if(trap.active)count++;return count;}
    public Trap trap(int index){return traps[index];}
    public boolean marked(){return markTargetId!=0 && markTime>0 && markCharges>0;}
    public int markedTargetId(){return marked()?markTargetId:0;}
    public int markChargesRemaining(){return marked()?markCharges:0;}
    public float markX(){return markX;} public float markY(){return markY;}
    public float markTimeRemaining(){return markTime;}
    public boolean warCryActive(){return warCryTime>0;}
    public float warCryRemaining(){return warCryTime;}
    public int warCryDirectBonus(){return character==CharacterId.WARRIOR && warCryActive()?1:0;}
    /** Apply the passive's direct-only mitigation after iframe and reactive block checks. */
    public int reduceDirectDamage(int amount) {
        int level=levels[ClassSkillId.WARRIOR_WAR_CRY.ordinal()];
        if(character!=CharacterId.WARRIOR || amount<=1 || level<=0 || !warCryActive())return amount;
        float accumulated=warCryDamageReductionFraction+amount*ClassSkillTuning.warCryDamageReductionPercent(level)*.01f;
        int reduction=Math.min(amount-1,(int)Math.floor(accumulated));warCryDamageReductionFraction=accumulated-reduction;
        return Math.max(1,amount-reduction);
    }
    public boolean channelActive(){return channelTime>0;}
    public float channelRemaining(){return channelTime;}
    public float channelX(){return channelX;} public float channelY(){return channelY;}
    public float fireballCooldownMultiplier(){return channelActive()?channelCooldownMultiplier:1;}
    public int fireballDirectBonus(){return channelActive()?channelBonusDamage:0;}
    public boolean rainWarning(){return rainActive && rainAge<ClassSkillTuning.rainWarning();}
    public float rainX(){return rainX;} public float rainY(){return rainY;} public float rainRadius(){return rainRadius;}
    public boolean ownsCandidate(ClassSkillId id){if(id==null || id.owner!=character)return false;for(ClassSkillId candidate:candidates)if(candidate==id)return true;return false;}
    public boolean canUpgrade(ClassSkillId id) {
        if(!ownsCandidate(id) || !id.implemented())return false;
        int level=levels[id.ordinal()];return level<ClassSkillTuning.MAX_LEVEL && (level>0 || learnedCount<MAX_SKILL_SLOTS && passiveSlotCount.getAsInt()<3);
    }
    public boolean hasUpgrade(){for(ClassSkillId id:candidates)if(canUpgrade(id))return true;return false;}
    /** Reward commit only; the expected level makes stale class-skill offers harmless. */
    public boolean upgrade(ClassSkillId id,int expectedLevel) {
        if(!canUpgrade(id) || levels[id.ordinal()]!=expectedLevel)return false;
        if(expectedLevel==0)slots[learnedCount++]=id;
        else if(cooldowns[id.ordinal()]>0)cooldowns[id.ordinal()]*=ClassSkillTuning.cooldown(id,expectedLevel+1)/ClassSkillTuning.cooldown(id,expectedLevel);
        levels[id.ordinal()]=expectedLevel+1;revision++;return true;
    }

    /** Advance only from GameSession's RUNNING branch, so Pause and reward modals freeze clocks. */
    public void advance(float dt) {
        advance(dt,dt);
    }
    /** Cooldown potion changes only learned-skill cooldowns; all durations/effects use unchanged gameplay dt. */
    public void advance(float dt,float cooldownDelta) {
        if(dt<=0 || !Float.isFinite(dt))return;
        for(ClassSkillId id:candidates)if(levels[id.ordinal()]>0)cooldowns[id.ordinal()]=Math.max(0,cooldowns[id.ordinal()]-Math.max(0,cooldownDelta));
        if(whirlwindPending)whirlwindDelay-=dt;
        if(markTargetId!=0 && (markTime-=dt)<=0)clearMark();
        if(warCryTime>0 && (warCryTime-=dt)<=0){warCryTime=0;warCryDamageReductionFraction=0;}
        if(channelTime>0 && (channelTime-=dt)<=0){channelTime=0;channelCooldownMultiplier=1;channelBonusDamage=0;}
        for(Fx fx:effects)if(fx.active){fx.age+=dt;if(fx.age>=fx.duration)fx.active=false;}
    }

    /** Existing projectiles resolve first, then ready skills take one deterministic target scan. */
    public void resolve(float dt,Player player,EnemySystem enemies,CollisionGrid grid) {
        if(dt<=0 || !player.alive())return;
        validateMark(enemies,grid);advanceWaves(dt,player,enemies,grid);advanceRain(dt,enemies,grid);advanceTraps(dt,enemies,grid);
        if(whirlwindPending && whirlwindDelay<=0) {
            whirlwindPending=false;damageCircle(player.x,player.y,whirlwindRadius,whirlwindDamage,enemies,grid,FxKind.SLASH);
        }
        for(int i=0;i<learnedCount;i++) {
            ClassSkillId id=slots[i];int level=levels[id.ordinal()];if(level<1 || cooldowns[id.ordinal()]>0)continue;
            if(id==ClassSkillId.WARRIOR_WHIRLWIND)whirlwind(level,player,enemies,grid);
            else if(id==ClassSkillId.WARRIOR_SWORD_WAVE)swordWave(level,player,enemies,grid);
            else if(id==ClassSkillId.WARRIOR_WAR_CRY)warCry(level,player,enemies,grid);
            else if(id==ClassSkillId.ARCHER_MARK)hunterMark(level,player,enemies,grid);
            else if(id==ClassSkillId.ARCHER_FOCUS)focusShot(level,player,enemies,grid);
            else if(id==ClassSkillId.ARCHER_RAIN && !rainActive)arrowRain(level,player,enemies,grid);
            else if(id==ClassSkillId.ARCHER_POISON_ARROW)poisonArrow(level,player,enemies,grid);
            else if(id==ClassSkillId.MAGE_ICE_SHARD)iceShard(level,player,enemies,grid);
            else if(id==ClassSkillId.MAGE_ARCANE_TRAP)arcaneTrap(level,player,enemies,grid);
            else if(id==ClassSkillId.MAGE_CHANNEL && !channelActive())arcaneChannel(level,player,enemies,grid);
        }
    }

    /** Bonus is offered only to direct Bow and Focus Shot contacts; consume after accepted damage. */
    public int markBonus(Enemy target) {return marked() && target!=null && target.alive() && target.id==markTargetId?1:0;}
    public void acceptedMarkedHit(Enemy target,int bonus) {
        if(bonus>0 && target!=null && target.id==markTargetId && markTime>0 && markCharges>0) {
            markCharges--;markBonusHits++;if(markCharges<=0)clearMark();
        }
    }

    /** Death-listener hook for Poison Arrow breakout. Secondary applications cannot spread again. */
    public void onEnemyDeath(Enemy dead,EnemySystem enemies,CollisionGrid grid) {
        if(dead==null)return;boolean canSpread=dead.consumePoisonDeathSpread();
        if(!canSpread || !evolved(ClassSkillId.ARCHER_POISON_ARROW))return;
        int damage=dead.poisonDeathDamage(),ticks=dead.poisonDeathTicks();float duration=dead.poisonDeathDuration();
        if(damage<=0 || ticks<=0 || duration<=0)return;
        int firstId=0;
        for(int spread=0;spread<2;spread++) {
            Enemy nearest=null;float best=ClassSkillTuning.poisonSpreadRadius()*ClassSkillTuning.poisonSpreadRadius();
            for(int i=0;i<enemies.size();i++) {
                Enemy enemy=enemies.get(i);if(!enemy.alive() || enemy.id==dead.id || enemy.id==firstId)continue;
                float dx=enemy.x-dead.x,dy=enemy.y-dead.y,d2=dx*dx+dy*dy;
                if(d2>best || !grid.clearLine(dead.x,dead.y,enemy.x,enemy.y,.5f,.5f,true))continue;
                if(nearest==null || d2<best || d2==best && enemy.id<nearest.id){nearest=enemy;best=d2;}
            }
            if(nearest==null)break;
            firstId=nearest.id;
            if(nearest.applyPoison(damage,duration,ticks,false)) {poisonSpreads++;effect(FxKind.POISON_IMPACT,nearest.x,nearest.y,26,.28f,0,0);}
        }
    }

    private void validateMark(EnemySystem enemies,CollisionGrid grid) {
        if(markTargetId==0)return;
        boolean dead=true;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);
            if(enemy.id==markTargetId) {if(enemy.alive()){markX=enemy.x;markY=enemy.y;dead=false;}break;}
        }
        if(!dead)return;
        if(evolved(ClassSkillId.ARCHER_MARK) && !markTransferred && markTime>0 && markCharges>0) {
            Enemy nearest=null;float best=ClassSkillTuning.markTransferRange()*ClassSkillTuning.markTransferRange();
            for(int i=0;i<enemies.size();i++) {
                Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
                float dx=enemy.x-markX,dy=enemy.y-markY,d2=dx*dx+dy*dy;
                if(d2>best || !grid.clearLine(markX,markY,enemy.x,enemy.y,.5f,.5f,true))continue;
                if(nearest==null || d2<best || d2==best && enemy.id<nearest.id){nearest=enemy;best=d2;}
            }
            if(nearest!=null) {markTargetId=nearest.id;markX=nearest.x;markY=nearest.y;markCharges=Math.min(2,markCharges);markTransferred=true;markTransfers++;return;}
        }
        clearMark();
    }
    private void clearMark(){markTargetId=markCharges=0;markTime=markX=markY=0;markTransferred=false;}
    private void hunterMark(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy target=priorityTarget(player,enemies,grid,ClassSkillTuning.markRange());
        if(target==null)return;
        markTargetId=target.id;markX=target.x;markY=target.y;markTime=ClassSkillTuning.markDuration(level);markCharges=ClassSkillTuning.markCharges(level);markTransferred=false;
        cooldowns[ClassSkillId.ARCHER_MARK.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.ARCHER_MARK,level);markApplied++;
    }
    private void focusShot(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy target=priorityTarget(player,enemies,grid,ClassSkillTuning.focusRange());if(target==null)return;
        Wave arrow=freeWave();if(arrow==null)return;
        float dx=target.x-player.x,dy=target.y-player.y,length=(float)Math.hypot(dx,dy);if(length<.001f)return;
        arrow.active=true;arrow.arrow=true;arrow.ice=false;arrow.poison=false;arrow.smallIceShard=false;arrow.volleyId=-1;arrow.x=player.x;arrow.y=player.y+2;arrow.dx=dx/length;arrow.dy=dy/length;
        arrow.age=arrow.distance=0;arrow.damage=skillDamage(ClassSkillId.ARCHER_FOCUS,level,player);arrow.pierce=evolved(ClassSkillId.ARCHER_FOCUS)?2:1;arrow.hitCount=0;
        arrow.slowPercent=0;arrow.slowDuration=0;
        arrow.speed=ClassSkillTuning.focusSpeed();arrow.range=ClassSkillTuning.focusRange();arrow.lifetime=arrow.range/arrow.speed;
        cooldowns[ClassSkillId.ARCHER_FOCUS.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.ARCHER_FOCUS,level);focusFired++;
    }
    private void poisonArrow(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy target=poisonTarget(player,enemies,grid);if(target==null)return;
        Wave arrow=freeWave();if(arrow==null)return;
        float dx=target.x-player.x,dy=target.y-player.y,length=(float)Math.hypot(dx,dy);if(length<.001f)return;
        arrow.active=true;arrow.arrow=true;arrow.ice=false;arrow.poison=true;arrow.smallIceShard=false;arrow.volleyId=-1;arrow.x=player.x;arrow.y=player.y+2;arrow.dx=dx/length;arrow.dy=dy/length;
        arrow.age=arrow.distance=0;arrow.damage=skillDamage(ClassSkillId.ARCHER_POISON_ARROW,level,player);arrow.pierce=1;arrow.hitCount=0;
        arrow.slowPercent=0;arrow.slowDuration=0;arrow.poisonDamage=ClassSkillTuning.poisonTickDamage();
        arrow.poisonDuration=ClassSkillTuning.poisonDuration(level);arrow.poisonTicks=ClassSkillTuning.poisonTicks(level);
        arrow.speed=ClassSkillTuning.poisonArrowSpeed();arrow.range=ClassSkillTuning.poisonArrowRange();arrow.lifetime=arrow.range/arrow.speed;
        cooldowns[ClassSkillId.ARCHER_POISON_ARROW.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.ARCHER_POISON_ARROW,level);poisonFired++;
    }
    private Enemy poisonTarget(Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy clean=null,any=null;float range2=ClassSkillTuning.poisonArrowRange()*ClassSkillTuning.poisonArrowRange();float cleanD2=range2,anyD2=range2;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-player.x,dy=enemy.y-player.y,d2=dx*dx+dy*dy;
            if(d2>range2 || !grid.clearLine(player.x,player.y,enemy.x,enemy.y,.5f,.5f,true))continue;
            if(any==null || d2<anyD2 || d2==anyD2 && enemy.id<any.id){any=enemy;anyD2=d2;}
            if(!enemy.poisoned() && (clean==null || d2<cleanD2 || d2==cleanD2 && enemy.id<clean.id)){clean=enemy;cleanD2=d2;}
        }
        return clean==null?any:clean;
    }
    private void arrowRain(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy center=rainCenter(player,enemies,grid,ClassSkillTuning.rainSearchRange(),ClassSkillTuning.rainRadius(level));if(center==null)return;
        rainX=center.x;rainY=center.y;rainRadius=ClassSkillTuning.rainRadius(level);rainAge=0;rainActive=true;
        rainPulseCount=evolved(ClassSkillId.ARCHER_RAIN)?ClassSkillTuning.evolvedRainPulses(level):ClassSkillTuning.rainPulses(level);rainPulsesDone=rainHitCount=0;rainDamage=skillDamage(ClassSkillId.ARCHER_RAIN,level,player);
        cooldowns[ClassSkillId.ARCHER_RAIN.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.ARCHER_RAIN,level);rainActivations++;
        effect(FxKind.ARROW_WARNING,rainX,rainY,rainRadius*2,ClassSkillTuning.rainWarning(),0,-1);
    }
    private Enemy priorityTarget(Player player,EnemySystem enemies,CollisionGrid grid,float range) {
        Enemy boss=null,normal=null;float bossD2=range*range,normalD2=range*range;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-player.x,dy=enemy.y-player.y,d2=dx*dx+dy*dy;
            if(d2>range*range || !grid.clearLine(player.x,player.y,enemy.x,enemy.y,.5f,.5f,true))continue;
            if(enemy.boss) {if(boss==null || d2<bossD2 || d2==bossD2 && enemy.id<boss.id){boss=enemy;bossD2=d2;}}
            else if(normal==null || d2<normalD2 || d2==normalD2 && enemy.id<normal.id){normal=enemy;normalD2=d2;}
        }
        return boss==null?normal:boss;
    }
    private Enemy rainCenter(Player player,EnemySystem enemies,CollisionGrid grid,float range,float radius) {
        Enemy best=null;int bestCount=0;float bestDistance=Float.POSITIVE_INFINITY,r2=radius*radius,range2=range*range;
        for(int i=0;i<enemies.size();i++) {
            Enemy candidate=enemies.get(i);if(!candidate.alive())continue;
            float pdx=candidate.x-player.x,pdy=candidate.y-player.y,d2=pdx*pdx+pdy*pdy;
            if(d2>range2 || !grid.clearLine(player.x,player.y,candidate.x,candidate.y,.5f,.5f,true))continue;
            int count=0;
            for(int j=0;j<enemies.size();j++) {
                Enemy target=enemies.get(j);if(!target.alive())continue;
                float dx=target.x-candidate.x,dy=target.y-candidate.y;
                if(dx*dx+dy*dy<=r2 && grid.clearLine(candidate.x,candidate.y,target.x,target.y,.5f,.5f,true))count++;
            }
            if(best==null || count>bestCount || count==bestCount && (d2<bestDistance || d2==bestDistance && candidate.id<best.id)) {
                best=candidate;bestCount=count;bestDistance=d2;
            }
        }
        return best;
    }
    private void advanceRain(float dt,EnemySystem enemies,CollisionGrid grid) {
        if(!rainActive)return;
        rainAge+=dt;
        while(rainActive && rainPulsesDone<rainPulseCount && rainAge>=ClassSkillTuning.rainWarning()+rainPulsesDone*ClassSkillTuning.rainPulseInterval()) {
            rainPulse(enemies,grid);rainPulsesDone++;
            if(rainPulsesDone>=rainPulseCount)rainActive=false;
        }
    }
    private void rainPulse(EnemySystem enemies,CollisionGrid grid) {
        rainPulses++;effect(FxKind.ARROW_PULSE,rainX,rainY,18,.2f,0,-1);float r2=rainRadius*rainRadius;
        for(int i=0;i<enemies.size();i++) {
            Enemy target=enemies.get(i);if(!target.alive())continue;
            float dx=target.x-rainX,dy=target.y-rainY;if(dx*dx+dy*dy>r2 || !grid.clearLine(rainX,rainY,target.x,target.y,.5f,.5f,true))continue;
            int ledger=rainLedger(target.id);if(ledger>=0 && rainAcceptedHits[ledger]>=ClassSkillTuning.rainTargetHitCap(evolved(ClassSkillId.ARCHER_RAIN)))continue;
            int before=target.hp();target.damage(rainDamage);
            if(target.hp()<before) {
                if(ledger<0 && rainHitCount<rainHitIds.length){ledger=rainHitCount++;rainHitIds[ledger]=target.id;rainAcceptedHits[ledger]=0;}
                if(ledger>=0)rainAcceptedHits[ledger]++;
                rainHits++;effect(FxKind.SPARK,target.x,target.y,16,.18f,0,0);
            }
        }
    }
    private int rainLedger(int id){for(int i=0;i<rainHitCount;i++)if(rainHitIds[i]==id)return i;return -1;}
    private Wave freeWave(){for(Wave wave:waves)if(!wave.active)return wave;return null;}
    private int volleyHits(int volleyId,int enemyId) {
        if(volleyId<0 || volleyId>=volleys.length)return 0;WaveVolley volley=volleys[volleyId];
        for(int i=0;i<volley.count;i++)if(volley.ids[i]==enemyId)return volley.hits[i];return 0;
    }
    private void recordVolleyHit(int volleyId,int enemyId) {
        if(volleyId<0 || volleyId>=volleys.length)return;WaveVolley volley=volleys[volleyId];
        for(int i=0;i<volley.count;i++)if(volley.ids[i]==enemyId){volley.hits[i]++;return;}
        if(volley.count<volley.ids.length){volley.ids[volley.count]=enemyId;volley.hits[volley.count++]=1;}
    }
    private void releaseFinishedVolleys() {
        for(int i=0;i<volleys.length;i++)if(volleys[i].active) {
            boolean live=false;for(Wave wave:waves)if(wave.active && wave.volleyId==i){live=true;break;}
            if(!live)volleys[i].active=false;
        }
    }

    private void iceShard(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy target=nearest(player,enemies,grid,ClassSkillTuning.iceRange());if(target==null)return;
        Wave shard=freeWave();if(shard==null)return;
        float dx=target.x-player.x,dy=target.y-player.y,length=(float)Math.hypot(dx,dy);if(length<.001f)return;
        shard.active=true;shard.arrow=false;shard.ice=true;shard.poison=false;shard.smallIceShard=false;shard.volleyId=-1;shard.x=player.x;shard.y=player.y+2;shard.dx=dx/length;shard.dy=dy/length;
        shard.age=shard.distance=0;shard.damage=skillDamage(ClassSkillId.MAGE_ICE_SHARD,level,player);shard.pierce=1;shard.hitCount=0;
        shard.slowPercent=ClassSkillTuning.iceSlowPercent(level);shard.slowDuration=ClassSkillTuning.iceSlowDuration(level);
        shard.speed=ClassSkillTuning.iceSpeed();shard.range=ClassSkillTuning.iceRange();shard.lifetime=shard.range/shard.speed;
        cooldowns[ClassSkillId.MAGE_ICE_SHARD.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.MAGE_ICE_SHARD,level);iceFired++;
    }
    private void arcaneTrap(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy target=nearest(player,enemies,grid,ClassSkillTuning.trapRange());if(target==null)return;
        Trap trap=null;for(Trap candidate:traps)if(!candidate.active){trap=candidate;break;}if(trap==null)return;
        trap.active=true;trap.pulsing=false;trap.pulseIndex=0;trap.pulseClock=0;trap.x=target.x;trap.y=target.y;trap.age=0;trap.radius=ClassSkillTuning.trapExplosionRadius(level);
        trap.damage=skillDamage(ClassSkillId.MAGE_ARCANE_TRAP,level,player);
        cooldowns[ClassSkillId.MAGE_ARCANE_TRAP.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.MAGE_ARCANE_TRAP,level);trapPlacements++;
        effect(FxKind.TRAP_CAST,trap.x,trap.y,22,.28f,0,0);
    }
    private void advanceTraps(float dt,EnemySystem enemies,CollisionGrid grid) {
        for(Trap trap:traps)if(trap.active) {
            if(trap.pulsing) {
                trap.pulseClock+=dt;
                if(trap.pulseIndex<ClassSkillTuning.trapBreakoutPulses() && trap.pulseClock>=ClassSkillTuning.trapBreakoutInterval()) {
                    trap.pulseClock-=ClassSkillTuning.trapBreakoutInterval();trap.pulseIndex++;explodeTrapPulse(trap,enemies,grid);
                    if(trap.pulseIndex>=ClassSkillTuning.trapBreakoutPulses())trap.active=false;
                }
                continue;
            }
            trap.age+=dt;if(trap.age>=ClassSkillTuning.trapLifetime()){trap.active=false;continue;}
            if(trap.age<ClassSkillTuning.trapArmDelay())continue;
            float radius2=ClassSkillTuning.trapTriggerRadius()*ClassSkillTuning.trapTriggerRadius();boolean triggered=false;
            for(int i=0;i<enemies.size();i++) {
                Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
                float dx=Math.max(0,Math.abs(enemy.x-trap.x)-enemy.definition.halfWidth);
                float dy=Math.max(0,Math.abs(enemy.y-trap.y)-enemy.definition.halfHeight);
                if(dx*dx+dy*dy<=radius2){triggered=true;break;}
            }
            if(triggered) {
                trapExplosions++;trap.pulsing=evolved(ClassSkillId.MAGE_ARCANE_TRAP);trap.pulseIndex=1;trap.pulseClock=0;trap.age=0;
                effect(FxKind.TRAP_BLAST,trap.x,trap.y,trap.radius*2,.36f,0,0);explodeTrapPulse(trap,enemies,grid);
                if(!trap.pulsing)trap.active=false;else evolvedActivations++;
            }
        }
    }
    private void explodeTrapPulse(Trap trap,EnemySystem enemies,CollisionGrid grid) {
        float x=trap.x,y=trap.y,radius=trap.radius;int damage=trap.damage;float r2=radius*radius;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-x,dy=enemy.y-y;
            if(dx*dx+dy*dy>r2 || !grid.clearLine(x,y,enemy.x,enemy.y,.5f,.5f,true))continue;
            int before=enemy.hp();enemy.damage(damage);
            if(enemy.hp()<before){trapHits++;trapPulseHits++;effect(FxKind.SPARK,enemy.x,enemy.y,16,.18f,0,0);}
        }
    }
    private void arcaneChannel(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        if(nearest(player,enemies,grid,ClassSkillTuning.channelRange())==null)return;
        channelTime=ClassSkillTuning.channelDuration(level);channelCooldownMultiplier=ClassSkillTuning.channelFireballCooldownMultiplier(level);
        channelBonusDamage=ClassSkillTuning.channelDirectDamageBonus();channelX=player.x;channelY=player.y;
        cooldowns[ClassSkillId.MAGE_CHANNEL.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.MAGE_CHANNEL,level);channelActivations++;
        effect(FxKind.CHANNEL_CAST,player.x,player.y+10,24,.35f,0,0);
    }

    private Enemy nearest(Player player,EnemySystem enemies,CollisionGrid grid,float range) {
        Enemy found=null;float best=range*range;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-player.x,dy=enemy.y-player.y,distance=dx*dx+dy*dy;
            if(distance>range*range || !grid.clearLine(player.x,player.y,enemy.x,enemy.y,.5f,.5f,true))continue;
            if(found==null || distance<best || distance==best && enemy.id<found.id){found=enemy;best=distance;}
        }
        return found;
    }
    private void whirlwind(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        float radius=ClassSkillTuning.whirlwindRadius(level);
        if(nearest(player,enemies,grid,radius)==null)return;
        cooldowns[ClassSkillId.WARRIOR_WHIRLWIND.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_WHIRLWIND,level);
        whirlwindActivations++;int damage=skillDamage(ClassSkillId.WARRIOR_WHIRLWIND,level,player);
        damageCircle(player.x,player.y,radius,damage,enemies,grid,FxKind.SLASH);
        if(evolved(ClassSkillId.WARRIOR_WHIRLWIND)) {
            whirlwindPending=true;whirlwindDelay=ClassSkillTuning.whirlwindSecondPulseDelay();whirlwindRadius=radius;
            whirlwindDamage=(int)Math.max(1L,(long)damage*75/100);evolvedActivations++;
        }
    }
    private void damageCircle(float x,float y,float radius,int damage,EnemySystem enemies,CollisionGrid grid,FxKind visual) {
        effect(visual,x,y,radius*2,.24f,0,0);float r2=radius*radius;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-x,dy=enemy.y-y;
            if(dx*dx+dy*dy>r2 || !grid.clearLine(x,y,enemy.x,enemy.y,.5f,.5f,true))continue;
            int before=enemy.hp();enemy.damage(damage);if(enemy.hp()<before){if(evolved(ClassSkillId.WARRIOR_WHIRLWIND))evolvedHits++;effect(FxKind.SPARK,enemy.x,enemy.y,16,.18f,0,0);}
        }
    }
    private void warCry(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        float triggerRadius=ClassSkillTuning.warCryTriggerRadius(),trigger2=triggerRadius*triggerRadius;int nearby=0;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-player.x,dy=enemy.y-player.y;
            if(dx*dx+dy*dy<=trigger2 && grid.clearLine(player.x,player.y,enemy.x,enemy.y,.5f,.5f,true))nearby++;
        }
        if(nearby<4)return;
        float radius=ClassSkillTuning.warCryRadius(level),r2=radius*radius;
        int damage=skillDamage(ClassSkillId.WARRIOR_WAR_CRY,level,player),knockback=ClassSkillTuning.warCryKnockback(level);
        cooldowns[ClassSkillId.WARRIOR_WAR_CRY.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_WAR_CRY,level);
        warCryTime=ClassSkillTuning.warCryDuration(level)+(evolved(ClassSkillId.WARRIOR_WAR_CRY)?1:0);warCryDamageReductionFraction=0;warCryActivations++;
        effect(FxKind.WAR_CRY,player.x,player.y,radius*2,.48f,0,0);
        effect(FxKind.SPARK,player.x,player.y+8,20,.32f,0,0);
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-player.x,dy=enemy.y-player.y;
            if(dx*dx+dy*dy>r2 || !grid.clearLine(player.x,player.y,enemy.x,enemy.y,.5f,.5f,true))continue;
            int before=enemy.hp();enemy.damage(damage);
            if(enemy.hp()<before) {
                warCryHits++;effect(FxKind.SPARK,enemy.x,enemy.y,16,.18f,0,0);
                if(!enemy.boss && enemy.alive() && enemies.knockback(enemy,dx,dy,knockback))warCryKnockbacks++;
            }
        }
        if(evolved(ClassSkillId.WARRIOR_WAR_CRY)) {
            float pulse2=ClassSkillTuning.warCryBreakoutRadius(),pulse2sq=pulse2*pulse2;
            effect(FxKind.WAR_CRY,player.x,player.y,pulse2*2,.42f,0,0);evolvedActivations++;
            for(int i=0;i<enemies.size();i++) {
                Enemy enemy=enemies.get(i);if(!enemy.alive() || enemy.boss)continue;
                float dx=enemy.x-player.x,dy=enemy.y-player.y;
                if(dx*dx+dy*dy>pulse2sq || !grid.clearLine(player.x,player.y,enemy.x,enemy.y,.5f,.5f,true))continue;
                if(enemies.knockback(enemy,dx,dy,knockback))warCryKnockbacks++;
            }
        }
    }
    private void swordWave(int level,Player player,EnemySystem enemies,CollisionGrid grid) {
        Enemy target=nearest(player,enemies,grid,ClassSkillTuning.waveRange());if(target==null)return;
        float dx=target.x-player.x,dy=target.y-player.y,length=(float)Math.hypot(dx,dy);if(length<.001f)return;
        int damage=skillDamage(ClassSkillId.WARRIOR_SWORD_WAVE,level,player);
        if(evolved(ClassSkillId.WARRIOR_SWORD_WAVE)) {
            int available=0;for(Wave candidate:waves)if(!candidate.active && available<3)waveScratch[available++]=candidate;
            WaveVolley volley=freeVolley();if(available<3 || volley==null)return;volley.reset();int group=volleyIndex(volley);
            float ux=dx/length,uy=dy/length,c=ClassSkillTuning.waveFanCos(),s=ClassSkillTuning.waveFanSin();
            configureSwordWave(waveScratch[0],player.x,player.y+2,ux*c+uy*s,uy*c-ux*s,damage,ClassSkillTuning.wavePierce(level),group);
            configureSwordWave(waveScratch[1],player.x,player.y+2,ux,uy,damage,ClassSkillTuning.wavePierce(level),group);
            configureSwordWave(waveScratch[2],player.x,player.y+2,ux*c-uy*s,uy*c+ux*s,damage,ClassSkillTuning.wavePierce(level),group);
            evolvedActivations++;
        } else {
            Wave wave=freeWave();if(wave==null)return;configureSwordWave(wave,player.x,player.y+2,dx/length,dy/length,damage,ClassSkillTuning.wavePierce(level),-1);
        }
        cooldowns[ClassSkillId.WARRIOR_SWORD_WAVE.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_SWORD_WAVE,level);waveFired++;
    }
    private void configureSwordWave(Wave wave,float x,float y,float dx,float dy,int damage,int pierce,int volleyId) {
        wave.active=true;wave.arrow=wave.ice=wave.poison=wave.smallIceShard=false;wave.volleyId=volleyId;wave.x=x;wave.y=y;wave.dx=dx;wave.dy=dy;wave.age=wave.distance=0;
        wave.damage=damage;wave.pierce=pierce;wave.hitCount=0;wave.slowPercent=0;wave.slowDuration=0;
        wave.speed=ClassSkillTuning.waveSpeed();wave.range=ClassSkillTuning.waveRange();wave.lifetime=ClassSkillTuning.waveLifetime();
    }
    private WaveVolley freeVolley(){for(WaveVolley volley:volleys)if(!volley.active)return volley;return null;}
    private int volleyIndex(WaveVolley wanted){for(int i=0;i<volleys.length;i++)if(volleys[i]==wanted)return i;return -1;}
    private int skillDamage(ClassSkillId id,int level,Player player) {
        long value=(long)ClassSkillTuning.damage(id,level)*player.stats.damage();
        if(id.owner==CharacterId.WARRIOR)value+=warCryDirectBonus();
        return (int)Math.min(Integer.MAX_VALUE,value);
    }
    private static boolean hitBefore(Wave wave,int enemyId) {for(int i=0;i<wave.hitCount;i++)if(wave.hitIds[i]==enemyId)return true;return false;}
    private void advanceWaves(float dt,Player player,EnemySystem enemies,CollisionGrid grid) {
        int updateCount=0;for(Wave candidate:waves)if(candidate.active)waveUpdateScratch[updateCount++]=candidate;
        for(int updateIndex=0;updateIndex<updateCount;updateIndex++) {
            Wave wave=waveUpdateScratch[updateIndex];if(!wave.active)continue;
            float step=Math.min(dt,Math.min(wave.lifetime-wave.age,(wave.range-wave.distance)/wave.speed));
            if(step<=0){wave.active=false;continue;}
            float startX=wave.x,startY=wave.y,endX=startX+wave.dx*wave.speed*step,endY=startY+wave.dy*wave.speed*step;
            float projectileRadius=wave.smallIceShard?1.1f:wave.arrow?1.5f:2;
            float wall=CombatGeometry.wall(grid,startX,startY,endX,endY,projectileRadius);
            float stopX=startX+(endX-startX)*wall,stopY=startY+(endY-startY)*wall,cursorX=startX,cursorY=startY;
            int safety=0;
            while(wave.hitCount<wave.pierce && safety++<wave.pierce) {
                Enemy found=null;float best=Float.POSITIVE_INFINITY;
                for(int i=0;i<enemies.size();i++) {
                    Enemy enemy=enemies.get(i);if(!enemy.alive() || hitBefore(wave,enemy.id))continue;
                    if(wave.volleyId>=0 && volleyHits(wave.volleyId,enemy.id)>=2)continue;
                    float t=CombatGeometry.hit(cursorX,cursorY,stopX,stopY,enemy.x,enemy.y,enemy.definition.halfWidth+projectileRadius,enemy.definition.halfHeight+projectileRadius);
                    if(t<best || t==best && found!=null && enemy.id<found.id){found=enemy;best=t;}
                }
                if(found==null || !Float.isFinite(best) || wall<1 && best>=1)break;
                wave.hitIds[wave.hitCount++]=found.id;
                if(wave.volleyId>=0)recordVolleyHit(wave.volleyId,found.id);
                int bonus=wave.arrow && !wave.poison?markBonus(found):0;
                int direct=wave.arrow && !wave.poison && wave.hitCount>1?(int)Math.max(1L,(long)wave.damage*70/100):wave.damage;
                int amount=direct>Integer.MAX_VALUE-bonus?Integer.MAX_VALUE:direct+bonus;
                if(wave.poison)found.preparePoisonHit(wave.poisonDamage,wave.poisonDuration,wave.poisonTicks,true);
                int before=found.hp();found.damage(amount);boolean accepted=found.hp()<before;
                if(wave.arrow) {
                    if(wave.poison) {
                        if(!accepted)found.cancelPreparedPoisonHit();
                        else {poisonHits++;if(found.alive() && found.applyPoison(wave.poisonDamage,wave.poisonDuration,wave.poisonTicks,true)){poisonApplications++;effect(FxKind.POISON_IMPACT,found.x,found.y,30,.3f,0,0);}}
                    } else {focusHits++;if(accepted && bonus>0)acceptedMarkedHit(found,bonus);}
                    effect(FxKind.SPARK,found.x,found.y,16,.18f,0,0);
                    if(wave.poison || wave.hitCount>=wave.pierce){wave.active=false;break;}
                    float dx=stopX-cursorX,dy=stopY-cursorY,length=(float)Math.hypot(dx,dy);if(length<.001f)break;
                    cursorX+=dx*best+wave.dx*.02f;cursorY+=dy*best+wave.dy*.02f;
                    if(cursorX==stopX && cursorY==stopY)break;
                    continue;
                }
                if(wave.ice) {
                    iceHits++;effect(FxKind.ICE_IMPACT,found.x,found.y,22,.24f,0,0);
                    if(accepted && found.applySlow(wave.slowPercent,wave.slowDuration))iceSlows++;
                    wave.active=false;
                    if(accepted && !wave.smallIceShard && evolved(ClassSkillId.MAGE_ICE_SHARD))spawnIceSplinters(wave,found.x,found.y,wave.dx,wave.dy,wave.damage,wave.slowPercent,wave.slowDuration);
                    break;
                }
                waveHits++;effect(FxKind.SPARK,found.x,found.y,16,.18f,0,0);
                float dx=stopX-cursorX,dy=stopY-cursorY,length=(float)Math.hypot(dx,dy);if(length<.001f)break;
                cursorX+=dx*best+wave.dx*.02f;cursorY+=dy*best+wave.dy*.02f;
                if(cursorX==stopX && cursorY==stopY)break;
            }
            float traveled=(float)Math.hypot(stopX-startX,stopY-startY);wave.distance+=traveled;wave.age+=step;
            wave.x=stopX;wave.y=stopY;
            if(wall<1 || wave.hitCount>=wave.pierce || wave.age>=wave.lifetime || wave.distance>=wave.range)wave.active=false;
        }
        releaseFinishedVolleys();
    }

    private void spawnIceSplinters(Wave exclude,float x,float y,float dx,float dy,int baseDamage,int slowPercent,float slowDuration) {
        int count=ClassSkillTuning.iceSplinterCount(),found=0;for(Wave candidate:waves)if(candidate!=exclude && !candidate.active && found<count)waveScratch[found++]=candidate;
        if(found<count)return;
        float angle=(float)Math.toRadians(ClassSkillTuning.iceSplinterHalfAngleDegrees()),c=(float)Math.cos(angle),s=(float)Math.sin(angle);
        configureIceSplinter(waveScratch[0],x,y,dx*c+dy*s,dy*c-dx*s,Math.max(1,baseDamage/2),Math.max(1,slowPercent/2),slowDuration);
        configureIceSplinter(waveScratch[1],x,y,dx,dy,Math.max(1,baseDamage/2),Math.max(1,slowPercent/2),slowDuration);
        configureIceSplinter(waveScratch[2],x,y,dx*c-dy*s,dy*c+dx*s,Math.max(1,baseDamage/2),Math.max(1,slowPercent/2),slowDuration);
        iceShardSplinters+=count;
    }
    private void configureIceSplinter(Wave shard,float x,float y,float dx,float dy,int damage,int slowPercent,float slowDuration) {
        shard.active=true;shard.arrow=false;shard.ice=true;shard.poison=false;shard.smallIceShard=true;shard.volleyId=-1;
        shard.x=x;shard.y=y;shard.dx=dx;shard.dy=dy;shard.age=shard.distance=0;shard.damage=damage;shard.pierce=1;shard.hitCount=0;
        shard.slowPercent=slowPercent;shard.slowDuration=slowDuration;shard.speed=ClassSkillTuning.iceSpeed();shard.range=ClassSkillTuning.iceRange();shard.lifetime=shard.range/shard.speed;
    }

    /** Called only after Player has rejected dead, invalid, ordinary-iframe and Dash-iframe hits. */
    public boolean interceptDirectHit(int amount,float sourceX,float sourceY,Player player,EnemySystem enemies,CollisionGrid grid) {
        int shieldLevel=levels[ClassSkillId.MAGE_SHIELD.ordinal()];
        if(amount>0 && shieldLevel>0 && cooldowns[ClassSkillId.MAGE_SHIELD.ordinal()]<=0) {
            cooldowns[ClassSkillId.MAGE_SHIELD.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.MAGE_SHIELD,shieldLevel);
            shieldBlocks++;effect(FxKind.SHIELD,player.x,player.y,32,ClassSkillTuning.shieldEffectDuration(),0,0);
            if(evolved(ClassSkillId.MAGE_SHIELD)) {
                shieldPulseHits+=damageCircleCount(player.x,player.y,ClassSkillTuning.shieldBreakoutRadius(),ClassSkillTuning.shieldBreakoutDamage(),enemies,grid);
                effect(FxKind.SHIELD_PULSE,player.x,player.y,ClassSkillTuning.shieldBreakoutRadius()*2,.3f,0,0);evolvedActivations++;
            }
            return true;
        }
        int level=levels[ClassSkillId.WARRIOR_PARRY.ordinal()];
        boolean fullCircle=evolved(ClassSkillId.WARRIOR_PARRY);
        if(level<=0 || cooldowns[ClassSkillId.WARRIOR_PARRY.ordinal()]>0)return false;
        float fx=player.lastMoveX(),fy=player.lastMoveY(),facingLength=(float)Math.hypot(fx,fy);
        if(!fullCircle) {
            if(!Float.isFinite(sourceX) || !Float.isFinite(sourceY))return false;
            float sx=sourceX-player.x,sy=sourceY-player.y,length=(float)Math.hypot(sx,sy);if(length<.001f)return false;
            if(facingLength<.001f) {
                var direction=player.direction;
                fx=direction==com.example.monstersurvivor.assets.GameAssets.Direction.LEFT?-1:direction==com.example.monstersurvivor.assets.GameAssets.Direction.RIGHT?1:0;
                fy=direction==com.example.monstersurvivor.assets.GameAssets.Direction.DOWN?-1:direction==com.example.monstersurvivor.assets.GameAssets.Direction.UP?1:0;
                facingLength=(float)Math.hypot(fx,fy);
            }
            if(facingLength<.001f || (sx*fx+sy*fy)/(length*facingLength)<ClassSkillTuning.parryHalfAngleCos())return false;
        }
        cooldowns[ClassSkillId.WARRIOR_PARRY.ordinal()]=ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_PARRY,level);parryBlocks++;
        if(fullCircle)effect(FxKind.PARRY_FULL_ARC,player.x,player.y,ClassSkillTuning.parryBreakoutRadius()*2,.35f,0,0);
        else {fx/=facingLength;fy/=facingLength;effect(FxKind.PARRY_ARC,player.x,player.y,38,.35f,fx,fy);}
        float radius=fullCircle?ClassSkillTuning.parryBreakoutRadius():ClassSkillTuning.parryRadius();int damage=skillDamage(ClassSkillId.WARRIOR_PARRY,level,player);
        if(fullCircle)evolvedActivations++;
        float r2=radius*radius;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-player.x,dy=enemy.y-player.y;
            if(dx*dx+dy*dy>r2 || !grid.clearLine(player.x,player.y,enemy.x,enemy.y,.5f,.5f,true))continue;
            int before=enemy.hp();enemy.damage(damage);if(enemy.hp()<before){parryHits++;effect(FxKind.SPARK,enemy.x,enemy.y,16,.18f,0,0);}
        }
        return true;
    }
    private int damageCircleCount(float x,float y,float radius,int damage,EnemySystem enemies,CollisionGrid grid) {
        int hits=0;float r2=radius*radius;
        for(int i=0;i<enemies.size();i++) {
            Enemy enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-x,dy=enemy.y-y;
            if(dx*dx+dy*dy>r2 || !grid.clearLine(x,y,enemy.x,enemy.y,.5f,.5f,true))continue;
            int before=enemy.hp();enemy.damage(damage);if(enemy.hp()<before){hits++;effect(FxKind.SPARK,enemy.x,enemy.y,16,.18f,0,0);}
        }
        return hits;
    }
    private void effect(FxKind kind,float x,float y,float size,float duration,float dx,float dy) {
        for(Fx fx:effects)if(!fx.active){fx.active=true;fx.kind=kind;fx.x=x;fx.y=y;fx.size=size;fx.age=0;fx.duration=duration;fx.dirX=dx;fx.dirY=dy;return;}
        droppedFx++;
    }
    /** Death/Home cleanup also clears cooldown clocks and all in-flight class projectiles. */
    public void cancel() {
        for(ClassSkillId id:candidates)cooldowns[id.ordinal()]=0;
        for(Wave wave:waves){wave.active=false;wave.volleyId=-1;}for(Fx fx:effects)fx.active=false;
        for(WaveVolley volley:volleys)volley.active=false;whirlwindPending=false;whirlwindDelay=whirlwindRadius=0;whirlwindDamage=0;
        clearMark();rainActive=false;rainAge=rainX=rainY=rainRadius=0;rainPulseCount=rainPulsesDone=rainHitCount=0;
        for(Trap trap:traps){trap.active=false;trap.pulsing=false;trap.pulseIndex=0;trap.pulseClock=0;}
        warCryTime=warCryDamageReductionFraction=0;
        channelTime=0;channelCooldownMultiplier=1;channelBonusDamage=0;channelX=channelY=0;
    }
    public void reset() {
        cancel();java.util.Arrays.fill(levels,0);java.util.Arrays.fill(slots,null);java.util.Arrays.fill(evolved,false);learnedCount=0;revision++;
        whirlwindActivations=waveFired=waveHits=parryBlocks=parryHits=droppedFx=0;
        markApplied=markBonusHits=focusFired=focusHits=rainActivations=rainPulses=rainHits=0;
        iceFired=iceHits=iceSlows=trapPlacements=trapExplosions=trapHits=shieldBlocks=channelActivations=0;
        warCryActivations=warCryHits=warCryKnockbacks=poisonFired=poisonHits=poisonApplications=0;
        evolvedActivations=evolvedHits=iceShardSplinters=trapPulseHits=shieldPulseHits=poisonSpreads=markTransfers=0;
    }
}
