package com.example.monstersurvivor.boss;

import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.assets.GameAssets.*;

/** Mutable encounter state; HUD reads scalar getters, never owns timers or damage. */
public final class BossInstance {
    public enum Cast { NONE, SLAM, BOLT, AREA, PHASE_SHIFT }
    public final BossDefinition definition;
    public final Enemy actor;
    int phase=1;
    public int phaseChanges,primaryCasts,areaCasts,summoned;
    float entry,spellCooldown=2.5f,areaCooldown=4,castRemaining,castDuration,aimX,aimY,radius,impactTime,summonCooldown;
    Cast cast=Cast.NONE;
    BossInstance(BossDefinition definition,Enemy actor) {this.definition=definition;this.actor=actor;entry=definition.kind()==BossDefinition.Kind.LICH_LORD?.7f:.4f;}
    public boolean finalBoss(){return definition.kind()==BossDefinition.Kind.LICH_LORD;}
    public int phase(){return phase;}
    public int hp(){return actor.hp();}
    public int maxHp(){return actor.stats.hp;}
    public boolean visible(){return !actor.deathFinished();}
    public Cast cast(){return cast;}
    public float warningX(){return aimX;} public float warningY(){return aimY;} public float warningRadius(){return radius;}
    public float warningProgress(){return castDuration==0?0:1-Math.max(0,castRemaining)/castDuration;}
    public float impactTime(){return impactTime;}
    void pose(State requested,float x,float y) {
        State chosen=actor.sprites==null || actor.sprites.has(requested,actor.direction)?requested:State.ATTACK;
        actor.pose(chosen,x-actor.x,y-actor.y);
    }
}
