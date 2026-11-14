package com.example.monstersurvivor.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.monstersurvivor.config.WorldVisualConfig;

/** One camera-sized, vertex-colored pass: 20x12 quads, no FBO, custom shader or map traversal. */
public final class NightRenderer {
    private static final int COLS=20,ROWS=12;
    private final float[] vertices=new float[COLS*ROWS*20];
    private final OrthographicCamera camera=new OrthographicCamera();
    private final TextureRegion pixel;
    public NightRenderer(TextureRegion pixel) {this.pixel=pixel;}
    public void draw(SpriteBatch batch,WorldVisualConfig config,float strength,float width,float height,float px,float py) {
        if(strength<=0)return;
        int index=0;
        for(int y=0;y<ROWS;y++)for(int x=0;x<COLS;x++) {
            index=vertex(index,x*width/COLS,y*height/ROWS,px,py,config,strength);
            index=vertex(index,x*width/COLS,(y+1)*height/ROWS,px,py,config,strength);
            index=vertex(index,(x+1)*width/COLS,(y+1)*height/ROWS,px,py,config,strength);
            index=vertex(index,(x+1)*width/COLS,y*height/ROWS,px,py,config,strength);
        }
        camera.setToOrtho(false,width,height);batch.setProjectionMatrix(camera.combined);
        batch.begin();batch.draw(pixel.getTexture(),vertices,0,vertices.length);batch.end();
    }
    private int vertex(int i,float x,float y,float px,float py,WorldVisualConfig config,float strength) {
        float dx=x-px,dy=y-py,t=Math.min(1,Math.max(0,((float)Math.sqrt(dx*dx+dy*dy)-config.lightRadius*.35f)/(config.lightRadius*.65f)));
        t=t*t*(3-2*t);float alpha=strength*(.06f+(config.darkness-.06f)*t);
        vertices[i++]=x;vertices[i++]=y;vertices[i++]=Color.toFloatBits(.025f,.035f,.16f,alpha);
        vertices[i++]=pixel.getU();vertices[i++]=pixel.getV();return i;
    }
}
