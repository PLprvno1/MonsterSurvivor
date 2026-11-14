package com.example.monstersurvivor.entity;

import com.badlogic.gdx.utils.IntArray;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.config.GameConfig.EnemyStats;
import com.example.monstersurvivor.render.WorldSprite;
import com.example.monstersurvivor.enemy.*;

/** One actor owns its animation clock and path; dead actors have no AI or contact damage. */
public final class Enemy {
    public static final float HALF_WIDTH=5, HALF_HEIGHT=3, CONTACT_RADIUS=10;
    public final int id;
    public final EnemyType type;
    public final EnemyDefinition definition;
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
    private java.util.function.Consumer<Enemy> deathListener;
    public void onDeath(java.util.function.Consumer<Enemy> listener) {deathListener=listener;}
    private State state=State.IDLE;
    public Enemy(int id,EnemyType type,EnemyStats stats,SpriteSet sprites,float x,float y) {
        this(id,EnemyRegistry.legacy(type),stats,sprites,x,y);
    }
    public Enemy(int id,EnemyDefinition definition,EnemyStats stats,SpriteSet sprites,float x,float y) {
        this.id=id;this.definition=definition;this.type=definition.legacyType;this.stats=stats;this.sprites=sprites;this.x=x;this.y=y;hp=stats.hp;
        // Check the initial line immediately; staggering starts with subsequent refreshes.
        lineTimer=0;pathTimer=0;
        sprite=sprites==null?null:new WorldSprite(sprites.frame(state,direction,0),x,y,sprites.feetX,sprites.height-sprites.feetY,sprites.displayScale);
    }
    public State state() { return state; }
    public int hp() { return hp; }
    public boolean alive() { return hp>0; }
    public float duration(State s) {
        if(sprites!=null)return sprites.clip(s,direction).animation.getAnimationDuration();
        if(s==State.HURT)return definition.hurtSeconds;
        if(s==State.DEATH)return definition.deathSeconds;
        return definition.attackSeconds;
    }
    private void enter(State next) { if(state!=next) {state=next;animationTime=0;} }
    public void advance(float dt) {
        animationTime+=dt;
        if((state==State.HURT || state==State.ATTACK) && animationTime>=duration(state))enter(State.IDLE);
        pathTimer-=dt;lineTimer-=dt;chargeTime=Math.max(0,chargeTime-dt);chargeCooldown=Math.max(0,chargeCooldown-dt);
    }
    public boolean deathFinished() { return !alive() && animationTime>=duration(State.DEATH); }
    public boolean acting() { return state==State.ATTACK || state==State.HURT || state==State.DEATH; }
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
        hp=Math.max(0,hp-amount);chargeTime=0;
        if(hp==0) { enter(State.DEATH);path.clear();if(deathListener!=null)deathListener.accept(this);return true; }
        // A running Hurt clip is allowed to finish; another hit doesn't restart it.
        enter(State.HURT);return false;
    }
    public void prepareSprite() {
        sprite.x=x;sprite.y=y;sprite.frame=sprites.frame(state,direction,animationTime);
    }
}
