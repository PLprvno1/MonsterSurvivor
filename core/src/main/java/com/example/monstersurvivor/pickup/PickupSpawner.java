package com.example.monstersurvivor.pickup;

import com.example.monstersurvivor.ai.NavigationGrid;
import com.example.monstersurvivor.config.PickupConfig;
import com.example.monstersurvivor.entity.Player;
import java.util.Random;

/** Bounded searches over the existing ground components; no A* or flood fill at spawn time. */
public final class PickupSpawner {
    private final NavigationGrid nav;
    private final PickupConfig config;
    private final Random random;
    public PickupSpawner(NavigationGrid nav,PickupConfig config,long seed) {this.nav=nav;this.config=config;random=new Random(seed);}
    public int boxCell(Player player,PickupSystem items) {
        for(int i=0;i<config.randomAttempts;i++) {
            double a=random.nextDouble()*Math.PI*2;
            float radius=config.spawnMin+random.nextFloat()*(config.spawnMax-config.spawnMin);
            int cell=nav.cell(player.x+(float)Math.cos(a)*radius,player.y+(float)Math.sin(a)*radius);
            if(valid(cell,player,items,true,player.x,player.y,config.spawnMax))return cell;
        }
        return fallback(player.x,player.y,config.spawnMax,player,items,true);
    }
    public int dropCell(float x,float y,Player player,PickupSystem items) {
        int cell=nav.cell(x,y);
        if(valid(cell,player,items,false,x,y,config.dropRadius))return cell;
        return fallback(x,y,config.dropRadius,player,items,false);
    }
    private int fallback(float x,float y,float radius,Player player,PickupSystem items,boolean box) {
        // Nearest rings first, with a strict visit budget even on completely blocked maps.
        int col=(int)Math.floor(x/nav.collision.tileSize),row=(int)Math.floor(y/nav.collision.tileSize),visited=0;
        int rings=(int)Math.ceil(radius/nav.collision.tileSize);
        for(int ring=0;ring<=rings;ring++)for(int dy=-ring;dy<=ring;dy++)for(int dx=-ring;dx<=ring;dx++) {
            if(Math.max(Math.abs(dx),Math.abs(dy))!=ring)continue;
            if(++visited>config.fallbackCells)return -1;
            int cx=col+dx,cy=row+dy;
            if(cx<0 || cy<0 || cx>=nav.collision.width || cy>=nav.collision.height)continue;
            int cell=cy*nav.collision.width+cx;
            if(valid(cell,player,items,box,x,y,radius))return cell;
        }
        return -1;
    }
    private boolean valid(int cell,Player player,PickupSystem items,boolean box,float x,float y,float radius) {
        if(!nav.connected(nav.cell(player.x,player.y),cell,false))return false;
        float px=nav.x(cell),py=nav.y(cell),dx=px-x,dy=py-y;
        if(dx*dx+dy*dy>radius*radius || (box && dx*dx+dy*dy<config.spawnMin*config.spawnMin))return false;
        return nav.collision.canOccupy(px,py,Pickup.HALF_WIDTH,Pickup.HALF_HEIGHT) && !items.occupied(px,py);
    }
}
