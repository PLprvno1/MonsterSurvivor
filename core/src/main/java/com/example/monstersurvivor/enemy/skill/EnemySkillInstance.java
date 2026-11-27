package com.example.monstersurvivor.enemy.skill;

import com.example.monstersurvivor.entity.Enemy;

/** Actor-owned state; target point never changes during an AoE cast. */
public final class EnemySkillInstance {
    public enum Phase { CHASE,PREPARE,RELEASE,RECOVER,COOLDOWN }
    public final EnemySkillDefinition definition;
    public Phase phase=Phase.CHASE;
    public float cooldown,age,windup,recovery=.2f,targetX,targetY,originX,originY,aimX,aimY;
    public int casts,cancels;
    public EnemySkillInstance(EnemySkillDefinition definition,int owner){this.definition=definition;cooldown=.45f+(owner%7)*.09f;}
    public boolean busy(){return phase==Phase.PREPARE || phase==Phase.RELEASE || phase==Phase.RECOVER;}
    public void cancel(Enemy e){
        if(!busy())return;cancels++;phase=Phase.COOLDOWN;cooldown=Math.max(cooldown,1.2f);age=0;e.aiLocked=false;e.releasePose();
    }
    public float animationTime(Enemy e){
        float progress=phase==Phase.PREPARE?Math.min(.64f,age/Math.max(.01f,windup)*.64f):.65f+.35f*Math.min(1,age/Math.max(.01f,recovery));
        return e.duration(e.state())*progress;
    }
}
