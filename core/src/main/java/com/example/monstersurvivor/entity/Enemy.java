package com.example.monstersurvivor.entity;

import com.badlogic.gdx.utils.IntArray;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.config.GameConfig.EnemyStats;
import com.example.monstersurvivor.render.WorldSprite;
import com.example.monstersurvivor.enemy.*;

/** One actor owns its animation clock and path; dead actors have no AI or contact damage. */
public final class Enemy {
    public com.example.monstersurvivor.audio.AudioSink audio=com.example.monstersurvivor.audio.AudioSink.NONE;
    private com.example.monstersurvivor.audio.AudioEvent reaction() {
        var d=definition;
        if(d.family.contains("undead") || d.id.contains("skeleton") || d.id.contains("zombie") || d.id.contains("bone") || d.id.contains("mummy") || d.id.contains("wraith") || d.id.contains("skull"))return com.example.monstersurvivor.audio.AudioEvent.ENEMY_UNDEAD_REACTION;
        if(d.flying)return com.example.monstersurvivor.audio.AudioEvent.ENEMY_FLYING_REACTION;
        if(d.archetype==EnemyArchetype.TANK)return com.example.monstersurvivor.audio.AudioEvent.ENEMY_TANK_REACTION;
        if(d.id.contains("boar") || d.id.contains("wolf") || d.id.contains("hound") || d.id.contains("beast") || d.id.contains("fox") || d.id.contains("cat") || d.id.contains("owlbear") || d.id.contains("jackalope"))return com.example.monstersurvivor.audio.AudioEvent.ENEMY_BEAST_REACTION;
        return com.example.monstersurvivor.audio.AudioEvent.ENEMY_HIT;
    }
    public static final float HALF_WIDTH=5, HALF_HEIGHT=3, CONTACT_RADIUS=10;
    public final int id;
    public final EnemyType type;
    public final EnemyDefinition definition;
    public final boolean boss;
    public final com.example.monstersurvivor.enemy.skill.EnemySkillInstance specialSkill;
    public boolean aiLocked;
    public float standOffRange;
    public int summonOwner;
    private int burstCap=Integer.MAX_VALUE,burstSpent;
    private float burstAge,burstWindow=.2f;
    public final EnemyStats stats;
    public final SpriteSet sprites;
    public final WorldSprite sprite;
    public final IntArray path=new IntArray(128);
    public float x,y,animationTime,pathTimer,lineTimer,stuckTime,chargeTime,chargeCooldown;
    public float separationX,separationY;
    public int pathIndex,targetCell=-1;
    public boolean direct;
    public Direction direction=Direction.DOWN;
    private int hp;
    private float slowRemaining,slowMultiplier=1;
    private float poisonRemaining,poisonTickRemaining;
    private int poisonDamage,poisonTicksRemaining;
    private boolean poisonSpreadAllowed,poisonDeathSpreadEligible,preparedPoisonHit;
    private float preparedPoisonDuration,poisonDeathDuration;
    private int preparedPoisonDamage,preparedPoisonTicks,poisonDeathDamage,poisonDeathTicks;
    private java.util.function.Consumer<Enemy> deathListener;
    public void onDeath(java.util.function.Consumer<Enemy> listener) {deathListener=listener;}
    private State state=State.IDLE;
    public Enemy(int id,EnemyType type,EnemyStats stats,SpriteSet sprites,float x,float y) {
        this(id,EnemyRegistry.legacy(type),stats,sprites,x,y);
    }
    public Enemy(int id,EnemyDefinition definition,EnemyStats stats,SpriteSet sprites,float x,float y) {
        this.id=id;this.definition=definition;boss=definition.index<0;this.type=definition.legacyType;this.stats=stats;this.sprites=sprites;this.x=x;this.y=y;hp=stats.hp;
        specialSkill=new com.example.monstersurvivor.enemy.skill.EnemySkillInstance(definition.skill,id);
        // Check the initial line immediately; staggering starts with subsequent refreshes.
        lineTimer=0;pathTimer=0;
        sprite=sprites==null?null:new WorldSprite(sprites.frame(state,direction,0),x,y,sprites.feetX,sprites.height-sprites.feetY,boss?definition.scale:sprites.displayScale);
    }
    public State state() { return state; }
    public int hp() { return hp; }
    public boolean alive() { return hp>0; }
    public float slowRemaining() {return slowRemaining;}
    public float poisonRemaining() {return poisonRemaining;}
    public boolean poisoned() {return alive() && poisonRemaining>0;}
    public float movementMultiplier() {return boss || slowRemaining<=0?1:slowMultiplier;}
    /** Enemy-only slow; repeated applications refresh duration without stacking strength. */
    public boolean applySlow(int percent,float duration) {
        if(boss || !alive() || percent<=0 || duration<=0 || !Float.isFinite(duration))return false;
        float multiplier=1-Math.min(100,percent)*.01f;
        slowMultiplier=Math.min(slowMultiplier,multiplier);slowRemaining=duration;return true;
    }
    /** One bounded Enemy-owned poison state. Reapplication refreshes its strength and lifetime. */
    public boolean applyPoison(int damage,float duration,int ticks) {
        return applyPoison(damage,duration,ticks,true);
    }
    public boolean applyPoison(int damage,float duration,int ticks,boolean maySpreadOnDeath) {
        if(!alive() || damage<=0 || duration<=0 || !Float.isFinite(duration) || ticks<=0)return false;
        poisonDamage=damage;poisonRemaining=duration;poisonTicksRemaining=ticks;poisonTickRemaining=1;poisonSpreadAllowed=maySpreadOnDeath;return true;
    }
    public void preparePoisonHit(int damage,float duration,int ticks,boolean maySpreadOnDeath) {
        preparedPoisonHit=maySpreadOnDeath && damage>0 && duration>0 && Float.isFinite(duration) && ticks>0;
        preparedPoisonDamage=damage;preparedPoisonDuration=duration;preparedPoisonTicks=ticks;
    }
    public void cancelPreparedPoisonHit() {preparedPoisonHit=false;preparedPoisonDamage=preparedPoisonTicks=0;preparedPoisonDuration=0;}
    public boolean consumePoisonDeathSpread() {boolean result=poisonDeathSpreadEligible;poisonDeathSpreadEligible=false;return result;}
    public int poisonDeathDamage() {return poisonDeathDamage;}
    public int poisonDeathTicks() {return poisonDeathTicks;}
    public float poisonDeathDuration() {return poisonDeathDuration;}
    public void clearPoison() {poisonDamage=poisonTicksRemaining=0;poisonRemaining=poisonTickRemaining=0;poisonSpreadAllowed=false;}
    public float duration(State s) {
        if(sprites!=null)return sprites.clip(s,direction).animation.getAnimationDuration();
        if(s==State.HURT)return definition.hurtSeconds;
        if(s==State.DEATH)return definition.deathSeconds;
        return definition.attackSeconds;
    }
    private void enter(State next) { if(state!=next) {state=next;animationTime=0;} }
    public void advance(float dt) {
        if(slowRemaining>0 && (slowRemaining-=dt)<=0){slowRemaining=0;slowMultiplier=1;}
        if(boss && (burstAge+=dt)>=burstWindow) {burstAge=0;burstSpent=0;}
        advancePoison(dt);
        animationTime+=dt;
        if((state==State.HURT || state==State.ATTACK && !aiLocked) && animationTime>=duration(state))enter(State.IDLE);
        pathTimer-=dt;lineTimer-=dt;chargeTime=Math.max(0,chargeTime-dt);chargeCooldown=Math.max(0,chargeCooldown-dt);
    }
    private void advancePoison(float dt) {
        if(poisonRemaining<=0 || !alive()) {if(poisonRemaining>0 || poisonTicksRemaining>0)clearPoison();return;}
        float activeStep=Math.min(dt,poisonRemaining);poisonRemaining-=activeStep;poisonTickRemaining-=activeStep;
        while(alive() && poisonTicksRemaining>0 && poisonTickRemaining<=.00001f) {
            poisonTicksRemaining--;damage(poisonDamage);poisonTickRemaining+=1;
        }
        if(!alive() || poisonRemaining<=.00001f)clearPoison();
    }
    public boolean deathFinished() { return !alive() && animationTime>=duration(State.DEATH); }
    public boolean acting() { return state==State.ATTACK || state==State.SPECIAL || state==State.HURT || state==State.DEATH; }
    public void locomotion(boolean moved,float dx,float dy) {
        if(acting())return;
        if(moved)direction=Direction.fromVector(dx,dy);
        enter(moved?State.WALK:State.IDLE);
    }
    public void attack(float dx,float dy) {
        if(acting())return;
        direction=Direction.fromVector(dx,dy);enter(State.ATTACK);
    }
    /** Returns true exactly once on the transition to death, for kill accounting. */
    public boolean damage(int amount) {
        if(!alive() || amount<=0)return false;
        if(boss) {amount=Math.min(amount,burstCap-burstSpent);if(amount<=0)return false;burstSpent+=amount;}
        if(!boss)specialSkill.cancel(this);
        hp=Math.max(0,hp-amount);chargeTime=0;
        if(!boss)audio.at(hp==0?com.example.monstersurvivor.audio.AudioEvent.ENEMY_DEATH:reaction(),x,y,id);
        if(hp==0) {
            boolean activePoison=poisonSpreadAllowed && (poisonRemaining>0 || poisonTicksRemaining>0 || poisonTickRemaining<=0);
            if(preparedPoisonHit || activePoison) {
                poisonDeathSpreadEligible=true;
                poisonDeathDamage=preparedPoisonHit?preparedPoisonDamage:poisonDamage;
                poisonDeathDuration=preparedPoisonHit?preparedPoisonDuration:poisonRemaining;
                poisonDeathTicks=preparedPoisonHit?preparedPoisonTicks:poisonTicksRemaining;
            }
            cancelPreparedPoisonHit();slowRemaining=0;slowMultiplier=1;clearPoison();enter(State.DEATH);path.clear();if(deathListener!=null)deathListener.accept(this);return true;
        }
        cancelPreparedPoisonHit();
        // A running Hurt clip is allowed to finish; another hit doesn't restart it.
        enter(State.HURT);return false;
    }
    public void limitBurst(float fraction,float seconds) {burstCap=Math.max(1,(int)Math.ceil(stats.hp*fraction));burstWindow=seconds;}
    public void pose(State pose,float dx,float dy) {
        if(!alive())return;direction=Direction.fromVector(dx,dy);enter(pose);
    }
    public void releasePose() {if(alive() && state!=State.HURT)enter(State.IDLE);}
    public void prepareSprite() {
        sprite.x=x;sprite.y=y;sprite.frame=sprites.frame(state,direction,!boss && specialSkill.busy()?specialSkill.animationTime(this):animationTime);
    }
}
