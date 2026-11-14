package com.example.monstersurvivor.world;

import com.badlogic.gdx.math.MathUtils;
import com.example.monstersurvivor.debug.FrameMetrics;

/** Static row-major, y-up navigation grid. Outside the map is always blocked. */
public final class CollisionGrid {
    public static final int TREE = 1, ROCK = 2, WATER = 4, BOUNDARY = 8;
    public final int width, height, tileSize;
    private final byte[] masks;
    public FrameMetrics metrics;
    public CollisionGrid(int width, int height, int tileSize, byte[] masks) {
        if (width <= 0 || height <= 0 || tileSize <= 0 || masks.length != width * height)
            throw new IllegalArgumentException("Invalid collision grid dimensions");
        this.width = width; this.height = height; this.tileSize = tileSize;
        this.masks = masks.clone();
    }
    public int index(int x, int y) { return y * width + x; }
    public int mask(int x, int y) {
        return x < 0 || y < 0 || x >= width || y >= height ? BOUNDARY : masks[index(x, y)] & 0xff;
    }
    public boolean isWalkable(int x, int y) { return mask(x, y) == 0; }
    /** Flying ignores WATER only; trees, rocks and boundaries still block. */
    public boolean isWalkable(int x, int y, boolean flying) {
        return (mask(x, y) & (flying ? ~WATER : 0xff)) == 0;
    }
    public boolean canOccupy(float x, float y, float halfWidth, float halfHeight) {
        return canOccupy(x, y, halfWidth, halfHeight, false);
    }
    public boolean canOccupy(float x, float y, float halfWidth, float halfHeight, boolean flying) {
        if(metrics==null || !metrics.active)return canOccupyRaw(x,y,halfWidth,halfHeight,flying);
        long start=metrics.start();metrics.occupancyChecks++;
        try {return canOccupyRaw(x,y,halfWidth,halfHeight,flying);}
        finally {metrics.finish(FrameMetrics.Part.COLLISION,start);}
    }
    private boolean canOccupyRaw(float x,float y,float halfWidth,float halfHeight,boolean flying) {
        int minX = MathUtils.floor((x - halfWidth) / tileSize);
        int maxX = MathUtils.floor((x + halfWidth - .001f) / tileSize);
        int minY = MathUtils.floor((y - halfHeight) / tileSize);
        int maxY = MathUtils.floor((y + halfHeight - .001f) / tileSize);
        if(minX<0 || minY<0 || maxX>=width || maxY>=height)return false;
        int blocked=flying?~WATER:0xff;
        if(minX==maxX && minY==maxY)return (masks[minY*width+minX]&blocked)==0;
        for (int row = minY; row <= maxY; row++)
            for (int col = minX; col <= maxX; col++) if ((masks[row*width+col]&blocked)!=0) return false;
        return true;
    }

    /** Exact swept rectangle against blocked tiles visited by a center-line DDA. */
    public boolean clearLine(float x, float y, float targetX, float targetY,
                             float halfWidth, float halfHeight, boolean flying) {
        long start=metrics==null?0:metrics.start();
        try {return clearLineRaw(x,y,targetX,targetY,halfWidth,halfHeight,flying);}
        finally {if(metrics!=null)metrics.finish(FrameMetrics.Part.COLLISION,start);}
    }
    private boolean clearLineRaw(float x,float y,float targetX,float targetY,float halfWidth,float halfHeight,boolean flying) {
        if(!canOccupyRaw(x,y,halfWidth,halfHeight,flying) || !canOccupyRaw(targetX,targetY,halfWidth,halfHeight,flying))return false;
        float dx=targetX-x,dy=targetY-y;
        int col=MathUtils.floor(x/tileSize),row=MathUtils.floor(y/tileSize);
        int endCol=MathUtils.floor(targetX/tileSize),endRow=MathUtils.floor(targetY/tileSize);
        int stepX=dx>0?1:dx<0?-1:0,stepY=dy>0?1:dy<0?-1:0;
        float invX=dx==0?0:1/dx,invY=dy==0?0:1/dy;
        float nextX=dx==0?Float.POSITIVE_INFINITY:((col+(stepX>0?1:0))*tileSize-x)*invX;
        float nextY=dy==0?Float.POSITIVE_INFINITY:((row+(stepY>0?1:0))*tileSize-y)*invY;
        float deltaX=dx==0?Float.POSITIVE_INFINITY:Math.abs(tileSize*invX),deltaY=dy==0?Float.POSITIVE_INFINITY:Math.abs(tileSize*invY);
        int radiusX=(int)Math.ceil(halfWidth/tileSize),radiusY=(int)Math.ceil(halfHeight/tileSize),blocked=flying?~WATER:0xff;
        for(int steps=0;steps<=width+height+2;steps++) {
            for(int cy=Math.max(0,row-radiusY);cy<=Math.min(height-1,row+radiusY);cy++)
                for(int cx=Math.max(0,col-radiusX);cx<=Math.min(width-1,col+radiusX);cx++) {
                    if((masks[cy*width+cx]&blocked)==0)continue;
                    if(intersects(x,y,dx,dy,invX,invY,cx*tileSize-halfWidth+.001f,cy*tileSize-halfHeight+.001f,
                        (cx+1)*tileSize+halfWidth-.001f,(cy+1)*tileSize+halfHeight-.001f))return false;
                }
            if(col==endCol && row==endRow)return true;
            if(nextX<nextY) {col+=stepX;nextX+=deltaX;} else {row+=stepY;nextY+=deltaY;}
        }
        return false;
    }
    private static boolean intersects(float x,float y,float dx,float dy,float invX,float invY,float left,float bottom,float right,float top) {
        float enter=0,exit=1;
        if(dx==0) {if(x<left || x>right)return false;}
        else {float a=(left-x)*invX,b=(right-x)*invX;enter=Math.max(enter,Math.min(a,b));exit=Math.min(exit,Math.max(a,b));}
        if(dy==0) {if(y<bottom || y>top)return false;}
        else {float a=(bottom-y)*invY,b=(top-y)*invY;enter=Math.max(enter,Math.min(a,b));exit=Math.min(exit,Math.max(a,b));}
        return enter<=exit;
    }
}
