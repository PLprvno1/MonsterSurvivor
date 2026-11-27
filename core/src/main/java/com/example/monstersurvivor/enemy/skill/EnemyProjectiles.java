package com.example.monstersurvivor.enemy.skill;

import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.weapon.CombatGeometry;
import com.example.monstersurvivor.world.CollisionGrid;

/** Ordinary-enemy pool only. A shot's velocity is written on fire and never changed during flight. */
public final class EnemyProjectiles {
    public enum Team { ENEMY }
    public static final int CAPACITY=24,FX_CAPACITY=24,AREA_CAPACITY=3;
    public static final class Shot {
        public final Team team=Team.ENEMY;
        public boolean active;public Enemy owner;public int id,ownerId,damage;
        public EnemySkillDefinition definition;
        public float x,y,vx,vy,age,lifetime,radius;
        public int direction;public float rotation;
    }
    public static final class Fx {public boolean active;public String clip;public float x,y,age,duration,size;}
    public static final class Area {public final Team team=Team.ENEMY;public boolean active;public Enemy owner;public float x,y,age,tick;}
    private final Shot[] shots=new Shot[CAPACITY];private final Fx[] effects=new Fx[FX_CAPACITY];private final Area[] areas=new Area[AREA_CAPACITY];
    public int fired,hits,blocked,expired,ownerCleared,areaHits,droppedFx;
    public EnemyProjectiles(){for(int i=0;i<shots.length;i++)shots[i]=new Shot();for(int i=0;i<effects.length;i++)effects[i]=new Fx();for(int i=0;i<areas.length;i++)areas[i]=new Area();}
    public Shot shot(int index){return shots[index];}public Fx effect(int index){return effects[index];}public Area area(int index){return areas[index];}
    public int size(){int n=0;for(var s:shots)if(s.active)n++;return n;}
    public int effectCount(){int n=0;for(var f:effects)if(f.active)n++;return n;}
    public int areaCount(){int n=0;for(var a:areas)if(a.active)n++;return n;}
    public boolean room(int count){return size()+count<=CAPACITY;}
    public boolean fire(Enemy owner,EnemySkillDefinition definition,float x,float y,float dx,float dy){
        float length=(float)Math.hypot(dx,dy);if(owner==null || owner.boss || !owner.alive() || !Float.isFinite(length) || !Float.isFinite(x) || !Float.isFinite(y) || length<.001f || definition.kind!=EnemySkillDefinition.Kind.SHOT)return false;
        for(var s:shots)if(!s.active){
            s.active=true;s.owner=owner;s.ownerId=owner.id;s.definition=definition;s.damage=definition.damage;s.radius=2;s.x=x;s.y=y;
            s.vx=dx/length*definition.speed;s.vy=dy/length*definition.speed;
            var direction=com.example.monstersurvivor.assets.GameAssets.Direction.fromVector(dx,dy);s.direction=direction.ordinal();
            s.rotation=(float)Math.toDegrees(Math.atan2(dy,dx))-switch(direction){case DOWN->-90;case LEFT->180;case RIGHT->0;case UP->90;};
            s.age=0;s.lifetime=2.6f;s.id=++fired;return true;
        }
        return false;
    }
    public void effect(String clip,float x,float y,float size,float duration){
        for(var f:effects)if(!f.active){f.active=true;f.clip=clip;f.x=x;f.y=y;f.size=size;f.age=0;f.duration=duration;return;}droppedFx++;
    }
    private void poisonArea(Enemy owner,float x,float y,CollisionGrid grid){
        if(!grid.canOccupy(x,y,2,2,true))return;
        for(var a:areas)if(!a.active){a.active=true;a.owner=owner;a.x=x;a.y=y;a.age=0;a.tick=.65f;return;}
    }
    public void clear(){for(var s:shots){s.active=false;s.owner=null;}for(var f:effects)f.active=false;for(var a:areas){a.active=false;a.owner=null;}}
    public static boolean hitArea(Player player,CollisionGrid grid,float x,float y,float radius,int damage,PlayerStatuses.Type status){
        if(!player.alive())return false;
        float dx=Math.max(0,Math.abs(player.x-x)-Player.HALF_WIDTH),dy=Math.max(0,Math.abs(player.y-y)-Player.HALF_HEIGHT);
        if(dx*dx+dy*dy>radius*radius || !grid.clearLine(x,y,player.x,player.y,.5f,.5f,true))return false;
        if(!player.damage(damage))return false;
        if(player.alive())player.statuses.apply(status);return true;
    }
    /** Immediate enemy area attacks can be parried; lingering poison zones keep using hitArea above. */
    public static boolean hitDirectArea(Player player,CollisionGrid grid,float x,float y,float radius,int damage,PlayerStatuses.Type status,float sourceX,float sourceY){
        if(!player.alive())return false;
        float dx=Math.max(0,Math.abs(player.x-x)-Player.HALF_WIDTH),dy=Math.max(0,Math.abs(player.y-y)-Player.HALF_HEIGHT);
        if(dx*dx+dy*dy>radius*radius || !grid.clearLine(x,y,player.x,player.y,.5f,.5f,true))return false;
        if(!player.damageDirect(damage,sourceX,sourceY))return false;
        if(player.alive())player.statuses.apply(status);return true;
    }
    public void update(float dt,Player player,CollisionGrid grid){
        if(!player.alive()){clear();return;}
        for(var f:effects)if(f.active){f.age+=dt;if(f.age>=f.duration)f.active=false;}
        for(var a:areas)if(a.active){
            if(!a.owner.alive()){a.active=false;a.owner=null;continue;}
            float step=Math.min(dt,2.3f-a.age);a.age+=step;a.tick-=step;
            if(a.tick<=.00001f){a.tick+=1;if(hitArea(player,grid,a.x,a.y,18,1,PlayerStatuses.Type.POISON))areaHits++;}
            if(a.age>=2.3f)a.active=false;
        }
        for(var s:shots)if(s.active){
            if(!s.owner.alive()){s.active=false;s.owner=null;ownerCleared++;continue;}
            float step=Math.min(dt,s.lifetime-s.age);if(step<=0){s.active=false;expired++;continue;}
            float nx=s.x+s.vx*step,ny=s.y+s.vy*step;
            float wall=CombatGeometry.wall(grid,s.x,s.y,nx,ny,s.radius);
            float hit=CombatGeometry.hit(s.x,s.y,nx,ny,player.x,player.y,Player.HALF_WIDTH+s.radius,Player.HALF_HEIGHT+s.radius);
            float impact=hit<=wall && hit<=1?hit:wall;
            if(hit<=wall && hit<=1){
                float impactX=s.x+(nx-s.x)*impact,impactY=s.y+(ny-s.y)*impact;
                if(player.damageDirect(s.damage,impactX,impactY)){hits++;if(player.alive())player.statuses.apply(s.definition.status);}s.active=false;
            }else if(wall<1){blocked++;s.active=false;}
            if(!s.active){
                s.x+=(nx-s.x)*impact;s.y+=(ny-s.y)*impact;
                effect(s.definition.impact,s.x,s.y,20,.28f);
                if(s.definition.poisonArea && player.alive())poisonArea(s.owner,s.x,s.y,grid);
            }else{s.x=nx;s.y=ny;}
            s.age+=step;if(s.active && s.age>=s.lifetime){s.active=false;expired++;}
        }
    }
}
