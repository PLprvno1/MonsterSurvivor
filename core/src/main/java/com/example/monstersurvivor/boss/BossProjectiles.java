package com.example.monstersurvivor.boss;

import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.weapon.CombatGeometry;
import com.example.monstersurvivor.world.CollisionGrid;

/** Hostile, single-hit swept projectiles. Never queries/damages allied enemies. Preallocated at session creation. */
public final class BossProjectiles {
    public enum Team { ENEMY }
    public static final class Shot {
        public final Team team=Team.ENEMY;
        public boolean active;public int owner,damage;public float x,y,vx,vy,age,lifetime;
    }
    private final Shot[] shots;
    public int fired,hits,blocked;
    public BossProjectiles(int capacity){shots=new Shot[capacity];for(int i=0;i<capacity;i++)shots[i]=new Shot();}
    public int capacity(){return shots.length;}
    public Shot get(int i){return shots[i];}
    public int size(){int n=0;for(var s:shots)if(s.active)n++;return n;}
    public boolean fire(int owner,float x,float y,float dx,float dy,float speed,int damage) {
        float length=(float)Math.hypot(dx,dy);if(length<.001f)return false;
        for(var s:shots)if(!s.active) {s.active=true;s.owner=owner;s.x=x;s.y=y;s.vx=dx/length*speed;s.vy=dy/length*speed;s.age=0;s.lifetime=3;s.damage=damage;fired++;return true;}
        return false;
    }
    public void clearOwner(int owner){for(var s:shots)if(s.owner==owner)s.active=false;}
    public void clear(){for(var s:shots)s.active=false;}
    public void update(float dt,Player player,CollisionGrid grid) {
        for(var s:shots)if(s.active) {
            float step=Math.min(dt,s.lifetime-s.age),nx=s.x+s.vx*step,ny=s.y+s.vy*step;
            float wall=CombatGeometry.wall(grid,s.x,s.y,nx,ny,2);
            float hit=CombatGeometry.hit(s.x,s.y,nx,ny,player.x,player.y,Player.HALF_WIDTH+2,Player.HALF_HEIGHT+2);
            if(hit<=wall && hit<=1) {if(player.damageDirect(s.damage,s.x,s.y))hits++;s.active=false;}
            else if(wall<1) {blocked++;s.active=false;}
            s.x=nx;s.y=ny;s.age+=step;if(s.age>=s.lifetime)s.active=false;
        }
    }
}
