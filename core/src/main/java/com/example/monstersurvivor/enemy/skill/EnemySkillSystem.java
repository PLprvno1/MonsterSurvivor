package com.example.monstersurvivor.enemy.skill;

import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.assets.GameAssets.State;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.world.CollisionGrid;
import static com.example.monstersurvivor.enemy.skill.EnemySkillInstance.Phase.*;

/** Part of EnemySystem: bounded casts, existing steering/animation, separate hostile projectile pool. */
public final class EnemySkillSystem {
    public static final float SIDE_MARGIN=14,BOTTOM_MARGIN=44,TOP_MARGIN=50;
    public static boolean castVisible(float x,float y,float left,float bottom,float right,float top){return x>=left+SIDE_MARGIN && x<=right-SIDE_MARGIN && y>=bottom+BOTTOM_MARGIN && y<=top-TOP_MARGIN;}
    public final EnemyProjectiles projectiles=new EnemyProjectiles();
    private final EnemySystem enemies;private final CollisionGrid grid;private final GameConfig config;private final boolean hard;
    private int rotation;private float startGate;private boolean enabled=true;
    public int starts,releases,cancelled,maxCasters,maxTelegraphs;
    public EnemySkillSystem(EnemySystem enemies,GameConfig config){this.enemies=enemies;this.config=config;grid=enemies.navigation.collision;hard=config.difficulty==Difficulty.HARD;}
    public boolean enabled(){return enabled;}
    /** Existing opt-in diagnostic callers only; never a preference or normal launch switch. */
    public void diagnosticEnabled(boolean value){if(!config.debug)throw new IllegalStateException("Diagnostic only");enabled=value;if(!value)clear();}
    public int activeCasters(){int n=0;for(int i=0;i<enemies.size();i++)if(!enemies.get(i).boss && enemies.get(i).specialSkill.busy())n++;return n;}
    public int telegraphs(){int n=0;for(int i=0;i<enemies.size();i++){var e=enemies.get(i);if(!e.boss && (e.specialSkill.phase==PREPARE || e.specialSkill.phase==RELEASE))n++;}return n;}
    public void cancel(Enemy e){if(!e.boss && e.specialSkill.busy()){e.specialSkill.cancel(e);cancelled++;}}
    public void clear(){for(int i=0;i<enemies.size();i++)cancel(enemies.get(i));projectiles.clear();startGate=0;}
    public void prepare(float dt,Player player,float progress,float left,float bottom,float right,float top){
        if(!enabled || !player.alive())return;startGate=Math.max(0,startGate-dt);
        for(int i=0;i<enemies.size();i++){
            if(!player.alive()){projectiles.clear();return;}
            var e=enemies.get(i);if(e.boss)continue;var s=e.specialSkill;var d=s.definition;
            if(d==EnemySkillDefinition.NONE)continue;
            s.cooldown=Math.max(0,s.cooldown-dt);
            e.standOffRange=progress>=d.unlock && d.kind==EnemySkillDefinition.Kind.SHOT && castVisible(e.x,e.y,left,bottom,right,top)?(hard?96:86):0;
            if(!s.busy())continue;
            if(!e.alive() || e.state()==State.HURT || Math.abs(e.x-s.originX)+Math.abs(e.y-s.originY)>4){cancel(e);continue;}
            s.age+=dt;
            if(s.phase==PREPARE){
                if(d.kind==EnemySkillDefinition.Kind.SHOT){s.aimX=player.x-e.x;s.aimY=player.y-e.y;}
                if(s.age>=s.windup){s.phase=RELEASE;s.age=0;}
            }else if(s.phase==RECOVER && s.age>=s.recovery){s.phase=COOLDOWN;s.age=0;e.aiLocked=false;e.releasePose();}
        }
        int busy=activeCasters(),warnings=telegraphs();
        int cap=progress<90?(hard?4:3):(hard?10:8),warningCap=hard?4:3;
        if(startGate>0 || busy>=cap || warnings>=warningCap || enemies.size()==0)return;
        int first=rotation++%enemies.size();
        for(int n=0;n<enemies.size();n++){
            var e=enemies.get((first+n)%enemies.size());var s=e.specialSkill;var d=s.definition;
            if(e.boss || !e.alive() || e.acting() || e.aiLocked || s.busy() || d==EnemySkillDefinition.NONE || s.cooldown>0 || progress<d.unlock)continue;
            // Leave room for UI03's opaque top panels/bottom slots and the caster's visible cue.
            if(!castVisible(e.x,e.y,left,bottom,right,top))continue;
            float dx=player.x-e.x,dy=player.y-e.y,distance2=dx*dx+dy*dy;
            if(distance2>d.range*d.range || (d.kind!=EnemySkillDefinition.Kind.SLAM && distance2<48*48))continue;
            if(!grid.clearLine(e.x,e.y,player.x,player.y,2,2,true) || d.kind==EnemySkillDefinition.Kind.SHOT && !projectiles.room(d.count))continue;
            s.phase=PREPARE;s.age=0;s.windup=d.windup(hard);s.recovery=d.recovery(hard);s.originX=e.x;s.originY=e.y;
            s.targetX=d.kind==EnemySkillDefinition.Kind.SLAM?e.x:player.x;s.targetY=d.kind==EnemySkillDefinition.Kind.SLAM?e.y:player.y;s.aimX=dx;s.aimY=dy;
            e.aiLocked=true;e.pose(e.sprites!=null && e.sprites.has(State.SPECIAL,e.direction)?State.SPECIAL:State.ATTACK,dx,dy);
            s.cooldown=d.cooldown(hard);startGate=hard?.24f:.42f;starts++;maxCasters=Math.max(maxCasters,busy+1);maxTelegraphs=Math.max(maxTelegraphs,warnings+1);break;
        }
    }
    /** After player weapons/Ultimate: a killed or knocked-back caster cannot release a pending shot. */
    public void resolve(float dt,Player player){
        if(!enabled || !player.alive())return;
        for(int i=0;i<enemies.size();i++){
            if(!player.alive()){projectiles.clear();return;}
            var e=enemies.get(i);if(e.boss)continue;var s=e.specialSkill;var d=s.definition;
            if(s.phase!=RELEASE)continue;
            if(!e.alive() || e.state()==State.HURT){cancel(e);continue;}
            if(d.kind==EnemySkillDefinition.Kind.SHOT){
                float dx=player.x-e.x,dy=player.y-e.y,length=(float)Math.hypot(dx,dy);
                // Revalidate at release; target may have escaped behind cover or reached the muzzle.
                if(length<48 || length>d.range*1.2f || !grid.clearLine(e.x,e.y,player.x,player.y,2,2,true) || !projectiles.room(d.count)){cancel(e);continue;}
                dx/=length;dy/=length;
                for(int lane=0;lane<d.count;lane++){
                    double angle=Math.toRadians((lane-(d.count-1)*.5f)*d.spreadOrRadius);float c=(float)Math.cos(angle),sn=(float)Math.sin(angle);
                    float vx=dx*c-dy*sn,vy=dx*sn+dy*c;
                    if(grid.clearLine(e.x,e.y,e.x+vx*8,e.y+vy*8,2,2,true))projectiles.fire(e,d,e.x+vx*8,e.y+vy*8,vx,vy);
                }
                projectiles.effect("cast",e.x,e.y,20,.3f);
            }else{
                EnemyProjectiles.hitDirectArea(player,grid,s.targetX,s.targetY,d.radius(),d.damage,d.status,e.x,e.y);
                projectiles.effect(d.impact,s.targetX,s.targetY,d.radius()*2,.4f);
            }
            s.phase=RECOVER;s.age=0;s.casts++;releases++;
        }
        projectiles.update(dt,player,grid);
    }
}
