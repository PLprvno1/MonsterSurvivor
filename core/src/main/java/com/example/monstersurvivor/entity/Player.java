package com.example.monstersurvivor.entity;

import com.example.monstersurvivor.assets.GameAssets.Direction;
import com.example.monstersurvivor.assets.GameAssets.State;
import com.example.monstersurvivor.assets.GameAssets.SpriteSet;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.world.CollisionGrid;

public final class Player {
    public static final float SPEED = 80, HALF_WIDTH = 4, HALF_HEIGHT = 3, MAX_STEP = 3;
    public float x, y, animationTime;
    public Direction direction = Direction.DOWN;
    public Direction movementDirection=Direction.DOWN, attackDirection=Direction.DOWN;
    public boolean walking;
    public static final float INVULNERABILITY=.7f;
    private final SpriteSet sprites;
    private final GameConfig config;
    public final PlayerStats stats;
    // Exact accepted axis segments, reused each frame (including wall slides at Speed 20).
    private final com.badlogic.gdx.utils.FloatArray movement=new com.badlogic.gdx.utils.FloatArray(64);
    private State state=State.IDLE;
    private int hp;
    private float immunity, cooldown, swingX, swingY;
    private boolean pendingHit, hitReady;
    public Player(float x, float y) { this(x,y,null,GameConfig.defaults()); }
    public Player(float x,float y,SpriteSet sprites,GameConfig config) {
        this.x=x;this.y=y;this.sprites=sprites;this.config=config;hp=config.playerHp;
        stats=new PlayerStats(config);movement.add(x,y);
    }
    public State state() { return state; }
    public int hp() { return hp; }
    public int maxHp() { return stats.maxHp(); }
    public int heal(int amount) {
        if(!alive() || amount<=0)return 0;
        int old=hp;hp=(int)Math.min(maxHp(),(long)hp+amount);return hp-old;
    }
    public boolean alive() { return hp>0; }
    public float immunity() { return immunity; }
    public float cooldown() { return cooldown; }
    public float swingX() { return swingX; }
    public float swingY() { return swingY; }
    private float duration(State s) {
        if(s==State.ATTACK)return config.swordAnimationDuration;
        if(sprites!=null)return sprites.clip(s,direction).animation.getAnimationDuration();
        return s==State.DEATH?1.5f:s==State.HURT?.3f:3f;
    }
    public boolean deathFinished() { return !alive() && animationTime>=duration(State.DEATH); }
    private void enter(State next) { if(state!=next) { state=next;animationTime=0; } }
    public boolean damage(int amount) {
        if(!alive() || immunity>0 || amount<=0)return false;
        hp=Math.max(0,hp-amount);immunity=INVULNERABILITY;
        pendingHit=hitReady=false;
        enter(alive()?State.HURT:State.DEATH);walking=false;return true;
    }
    public boolean canAttack() { return alive() && state!=State.HURT && state!=State.ATTACK && cooldown<=0; }
    public boolean startAttack(float dx,float dy) {
        return startAttack(dx,dy,config.swordCooldown);
    }
    public void limitSwordCooldown(float maximum) {cooldown=Math.min(cooldown,maximum);}
    public boolean startAttack(float dx,float dy,float cycle) {
        if(!canAttack())return false;
        enter(State.ATTACK);cooldown=cycle;pendingHit=true;hitReady=false;
        aimAttack(dx,dy);return true;
    }
    /** Tracking during windup changes facing, never the action clock or movement vector. */
    public void aimAttack(float dx,float dy) {
        if(state!=State.ATTACK || (!pendingHit && !hitReady))return;
        attackDirection=Direction.fromVector(dx,dy);direction=attackDirection;
        // Match the cardinal source animation and its frontal sword sector.
        swingX=direction==Direction.LEFT?-1:direction==Direction.RIGHT?1:0;
        swingY=direction==Direction.DOWN?-1:direction==Direction.UP?1:0;
    }
    public boolean windingUp() { return state==State.ATTACK && (pendingHit || hitReady); }
    public void cancelAttack() {
        if(!windingUp())return;
        pendingHit=hitReady=false;enter(walking?State.WALK:State.IDLE);direction=movementDirection;
        cooldown=Math.min(cooldown,.15f);
    }
    public float renderAnimationTime() {
        if(state!=State.ATTACK)return animationTime;
        float source=sprites==null?3f:sprites.clip(State.ATTACK,attackDirection).animation.getAnimationDuration();
        return animationTime/config.swordAnimationDuration*source;
    }
    public boolean takeSwingHit() { boolean ready=hitReady;hitReady=false;return ready; }
    public void update(float delta, float axisX, float axisY, CollisionGrid grid) {
        movement.clear();movement.add(x,y);
        immunity=Math.max(0,immunity-delta);cooldown=Math.max(0,cooldown-delta);
        if(state==State.DEATH) { animationTime+=delta;return; }
        boolean action=state==State.ATTACK || state==State.HURT;
        if(action) {
            animationTime+=delta;
            if(state==State.ATTACK && pendingHit && animationTime>=config.swordHitTime) {pendingHit=false;hitReady=true;}
            if(animationTime>=duration(state)) {enter(State.IDLE);action=false;}
        }
        float length2 = axisX * axisX + axisY * axisY;
        if (length2 > 1) { float inv = 1f / (float) Math.sqrt(length2); axisX *= inv; axisY *= inv; }
        Direction oldDirection = direction;
        if(length2>.0001f)movementDirection=Direction.fromVector(axisX,axisY);
        if(!action)direction=movementDirection;
        float oldX = x, oldY = y;
        float dx = axisX * stats.movementSpeed() * delta, dy = axisY * stats.movementSpeed() * delta;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / MAX_STEP));
        dx /= steps; dy /= steps;
        for (int i = 0; i < steps; i++) {
            if (dx!=0 && grid.canOccupy(x + dx, y, HALF_WIDTH, HALF_HEIGHT)) {x += dx;movement.add(x,y);}
            if (dy!=0 && grid.canOccupy(x, y + dy, HALF_WIDTH, HALF_HEIGHT)) {y += dy;movement.add(x,y);}
        }
        boolean moved = Math.abs(x - oldX) + Math.abs(y - oldY) > .0001f;
        if (!action) {
            if (walking != moved || direction != oldDirection) animationTime = 0;
            enter(moved?State.WALK:State.IDLE);
            animationTime += delta;
        }
        walking = moved;
    }
    /** Swept feet AABB along actual movement, with a ground LOS guard against corner/wall pickup. */
    public boolean touchesMovement(float px,float py,float hw,float hh,CollisionGrid grid) {
        float ex=HALF_WIDTH+hw,ey=HALF_HEIGHT+hh;
        for(int i=0;i<movement.size;i+=2) {
            float ax=movement.items[i],ay=movement.items[i+1];
            float bx=i+2<movement.size?movement.items[i+2]:ax,by=i+2<movement.size?movement.items[i+3]:ay;
            float dx=bx-ax,dy=by-ay,lo=0,hi=1;
            if(dx==0) {if(Math.abs(ax-px)>ex)continue;}
            else {float a=(px-ex-ax)/dx,b=(px+ex-ax)/dx;lo=Math.max(lo,Math.min(a,b));hi=Math.min(hi,Math.max(a,b));}
            if(dy==0) {if(Math.abs(ay-py)>ey)continue;}
            else {float a=(py-ey-ay)/dy,b=(py+ey-ay)/dy;lo=Math.max(lo,Math.min(a,b));hi=Math.min(hi,Math.max(a,b));}
            if(lo<=hi && grid.clearLine(ax+dx*lo,ay+dy*lo,px,py,0,0,false))return true;
        }
        return false;
    }
}
