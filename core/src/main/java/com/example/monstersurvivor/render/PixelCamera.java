package com.example.monstersurvivor.render;

import com.badlogic.gdx.math.MathUtils;

/** Presentation coordinates only. Never writes to simulation or collision positions. */
public final class PixelCamera {
    /** Fixed physical-pixel sample phase, avoiding nearest-sample ties at 4.5x/2.25x zoom. */
    public static final float RASTER_PHASE_PIXELS=1f/16;
    public enum Mode { LEGACY, FREE, WORLD, SCREEN }
    public final Mode mode;
    public float playerX, playerY, cameraX, cameraY;
    public PixelCamera(Mode mode) { this.mode=mode; }

    public void update(float x,float y,float width,float height,int pixelsX,int pixelsY,float mapWidth,float mapHeight) {
        float sx=pixelsX>0 ? pixelsX/width : 1, sy=pixelsY>0 ? pixelsY/height : 1;
        playerX=quantize(x,sx);playerY=quantize(y,sy);
        if(mode==Mode.SCREEN) {
            int px=MathUtils.round(x*sx),py=MathUtils.round(y*sy);
            // Integer arithmetic fixes the anchor on odd-sized windows as well as even ones.
            cameraX=clamp((px-pixelsX/2+RASTER_PHASE_PIXELS)/sx+width/2,width/2,mapWidth);
            cameraY=clamp((py-pixelsY/2+RASTER_PHASE_PIXELS)/sy+height/2,height/2,mapHeight);
        } else if(mode==Mode.LEGACY) {
            cameraX=alignEdge(clamp(MathUtils.round(x),width/2,mapWidth),width/2,mapWidth,sx);
            cameraY=alignEdge(clamp(MathUtils.round(y),height/2,mapHeight),height/2,mapHeight,sy);
        } else {
            cameraX=clamp(playerX,width/2,mapWidth);cameraY=clamp(playerY,height/2,mapHeight);
        }
    }
    private float quantize(float value,float scale) {
        switch(mode) {
            case LEGACY: case WORLD: return MathUtils.round(value);
            case SCREEN: return MathUtils.round(value*scale)/scale;
            default: return value;
        }
    }
    private static float alignEdge(float center,float half,float map,float scale) {
        return clamp(MathUtils.round((center-half)*scale)/scale+half,half,map);
    }
    public static float clamp(float target,float half,float map) {
        return map<=half*2 ? map/2 : MathUtils.clamp(target,half,map-half);
    }
}
