package com.example.monstersurvivor.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Comparator;
import com.example.monstersurvivor.assets.GameAssets.FrameRegion;

public class WorldSprite {
    public static final Comparator<WorldSprite> BACK_TO_FRONT = (a, b) -> Float.compare(b.y, a.y);
    public float x, y;
    public final float originX, originY;
    public final float scale;
    public TextureRegion frame;
    public WorldSprite(TextureRegion frame, float x, float y, float originX, float originY) {
        this(frame,x,y,originX,originY,1);
    }
    public WorldSprite(TextureRegion frame, float x, float y, float originX, float originY, float scale) {
        this.frame = frame; this.x = x; this.y = y; this.originX = originX; this.originY = originY;
        this.scale=scale;
    }
    // x/y are presentation coordinates. The camera owner applies the shared pixel grid once.
    public float drawX() { return x - (frame instanceof FrameRegion ? ((FrameRegion)frame).originX : originX)*scale; }
    public float drawY() { return y - (frame instanceof FrameRegion ? ((FrameRegion)frame).originY : originY)*scale; }
    public boolean visible(float left, float bottom, float right, float top) {
        float sx = drawX(), sy = drawY();
        return sx < right && sx + frame.getRegionWidth()*scale > left && sy < top && sy + frame.getRegionHeight()*scale > bottom;
    }
    public void draw(SpriteBatch batch) { batch.draw(frame,drawX(),drawY(),frame.getRegionWidth()*scale,frame.getRegionHeight()*scale); }
}
