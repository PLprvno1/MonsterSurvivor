package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.world.CollisionGrid;

/** Allocation-free time of impact for a segment and an expanded enemy rectangle. */
public final class CombatGeometry {
    private CombatGeometry() { }
    public static float hit(float ax,float ay,float bx,float by,float cx,float cy,float hw,float hh) {
        float dx=bx-ax,dy=by-ay,lo=0,hi=1;
        if(dx==0) {if(Math.abs(ax-cx)>hw)return Float.POSITIVE_INFINITY;}
        else {float a=(cx-hw-ax)/dx,b=(cx+hw-ax)/dx;lo=Math.max(lo,Math.min(a,b));hi=Math.min(hi,Math.max(a,b));}
        if(dy==0) {if(Math.abs(ay-cy)>hh)return Float.POSITIVE_INFINITY;}
        else {float a=(cy-hh-ay)/dy,b=(cy+hh-ay)/dy;lo=Math.max(lo,Math.min(a,b));hi=Math.min(hi,Math.max(a,b));}
        return lo<=hi?lo:Float.POSITIVE_INFINITY;
    }
    public static float wall(CollisionGrid grid,float x,float y,float nx,float ny,float radius) {
        if(grid.clearLine(x,y,nx,ny,radius,radius,true))return 1;
        float lo=0,hi=1;
        for(int i=0;i<12;i++) {float mid=(lo+hi)*.5f;
            if(grid.clearLine(x,y,x+(nx-x)*mid,y+(ny-y)*mid,radius,radius,true))lo=mid;else hi=mid;
        }
        return lo;
    }
}
