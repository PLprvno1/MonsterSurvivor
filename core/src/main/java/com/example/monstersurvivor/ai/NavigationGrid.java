package com.example.monstersurvivor.ai;

import com.example.monstersurvivor.world.CollisionGrid;

/** Static footprint-aware nodes and connected components, shared by all actors. */
public final class NavigationGrid {
    public static final float HALF_WIDTH=5, HALF_HEIGHT=3;
    public final CollisionGrid collision;
    private final boolean[][] open;
    private final int[][] components;
    public NavigationGrid(CollisionGrid collision) {
        this(collision,HALF_WIDTH,HALF_HEIGHT);
    }
    public NavigationGrid(CollisionGrid collision,float halfWidth,float halfHeight) {
        this.collision=collision;
        int count=collision.width*collision.height;
        open=new boolean[2][count]; components=new int[2][count];
        int[] queue=new int[count];
        for(int layer=0;layer<2;layer++) {
            for(int id=0;id<count;id++) open[layer][id]=collision.canOccupy(x(id),y(id),halfWidth,halfHeight,layer==1);
            int component=0;
            for(int id=0;id<count;id++) if(open[layer][id] && components[layer][id]==0) {
                component++;int head=0,tail=1;queue[0]=id;components[layer][id]=component;
                while(head<tail) {
                    int current=queue[head++];
                    for(int dir=0;dir<4;dir++) {
                        int next=neighbor(current,dir);
                        if(next>=0 && open[layer][next] && components[layer][next]==0) {
                            components[layer][next]=component;queue[tail++]=next;
                        }
                    }
                }
            }
        }
    }
    public int cell(float x,float y) {
        int col=(int)Math.floor(x/collision.tileSize),row=(int)Math.floor(y/collision.tileSize);
        return col<0 || row<0 || col>=collision.width || row>=collision.height ? -1 : collision.index(col,row);
    }
    public float x(int id) { return (id%collision.width+.5f)*collision.tileSize; }
    public float y(int id) { return (id/collision.width+.5f)*collision.tileSize; }
    public boolean walkable(int id,boolean flying) { return id>=0 && id<open[0].length && open[flying?1:0][id]; }
    public boolean connected(int a,int b,boolean flying) {
        return walkable(a,flying) && walkable(b,flying) && components[flying?1:0][a]==components[flying?1:0][b];
    }
    /** Bounded nearby approach node when a small player stands inside a gap too narrow for a boss. */
    public int approach(float x,float y,boolean flying) {
        int center=cell(x,y);if(walkable(center,flying))return center;
        int found=-1;float best=Float.POSITIVE_INFINITY;
        int cx=(int)(x/collision.tileSize),cy=(int)(y/collision.tileSize);
        for(int iy=cy-4;iy<=cy+4;iy++)for(int ix=cx-4;ix<=cx+4;ix++) {
            if(ix<0 || iy<0 || ix>=collision.width || iy>=collision.height)continue;int id=iy*collision.width+ix;
            float dx=x(id)-x,dy=y(id)-y,d2=dx*dx+dy*dy;
            if(walkable(id,flying) && d2<best && collision.clearLine(x(id),y(id),x,y,.5f,.5f,true)) {found=id;best=d2;}
        }
        return found;
    }
    public int neighbor(int id,int direction) {
        int x=id%collision.width,y=id/collision.width;
        switch(direction) {
            case 0:return x>0?id-1:-1;
            case 1:return x+1<collision.width?id+1:-1;
            case 2:return y>0?id-collision.width:-1;
            default:return y+1<collision.height?id+collision.width:-1;
        }
    }
}
