package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.world.CollisionGrid;

/** Called only at an attack/retry boundary, with a fixed maximum of 60 candidates. Water does not block shots. */
public final class WeaponTargeting {
    private WeaponTargeting() { }
    public static Enemy nearest(float x,float y,float range,EnemySystem enemies,CollisionGrid grid,int[] excluded,int count,float clearance) {
        Enemy best=null;float distance=range*range;
        search:for(int i=0;i<enemies.size();i++) {
            Enemy e=enemies.get(i);if(!e.alive())continue;
            for(int j=0;j<count;j++)if(e.id==excluded[j])continue search;
            float dx=e.x-x,dy=e.y-y,d2=dx*dx+dy*dy;
            if(d2<=distance && (best==null || d2<distance) && grid.clearLine(x,y,e.x,e.y,clearance,clearance,true)) {best=e;distance=d2;}
        }
        return best;
    }
}
