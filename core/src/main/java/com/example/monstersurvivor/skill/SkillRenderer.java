package com.example.monstersurvivor.skill;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;

/** Existing white texel only: <=6 dash streaks, two 32-segment rings and a short center flash. */
public final class SkillRenderer {
    private static final float[] X=new float[33],Y=new float[33];
    static {for(int i=0;i<33;i++) {X[i]=MathUtils.cos(i*MathUtils.PI2/32);Y[i]=MathUtils.sin(i*MathUtils.PI2/32);}}
    private final TextureRegion pixel;
    public SkillRenderer(TextureRegion pixel) {this.pixel=pixel;}
    private void line(SpriteBatch batch,float x,float y,float dx,float dy,float width) {
        batch.draw(pixel,x,y-width/2,0,width/2,(float)Math.hypot(dx,dy),width,1,1,MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees);
    }
    private void ring(SpriteBatch batch,float x,float y,float radius,float width) {
        for(int i=0;i<32;i++)line(batch,x+X[i]*radius,y+Y[i]*radius,(X[i+1]-X[i])*radius,(Y[i+1]-Y[i])*radius,width);
    }
    public void draw(SpriteBatch batch,ActiveSkillSystem skills,float playerX,float playerY,float left,float bottom,float right,float top) {
        float old=batch.getPackedColor();
        for(int i=0;i<skills.trailCount();i++) {
            float x=skills.trailX(i),y=skills.trailY(i);if(x<left-15 || x>right+15 || y<bottom-15 || y>top+15)continue;
            float fade=skills.trailOpacity()*(i+1)/skills.trailCount();
            batch.setColor(.35f,.85f,1,.34f*fade);
            // Each segment joins accepted positions, so a blocked dash does not draw through a wall.
            float tx=i+1<skills.trailCount()?skills.trailX(i+1):playerX,ty=i+1<skills.trailCount()?skills.trailY(i+1):playerY;
            line(batch,x,y+5,tx-x,ty-y,4+.35f*(skills.dashActivationLevel()-1));batch.setColor(.75f,.98f,1,.65f*fade);line(batch,x,y+7,tx-x,ty-y,1);
        }
        if(skills.ultimateVisible()) {
            float x=skills.ultimateX(),y=skills.ultimateY(),r=skills.ultimateRadius()*(.15f+.85f*skills.ultimateProgress()),a=Math.min(1,skills.ultimateOpacity()*(1+.06f*(skills.ultimateActivationLevel()-1)));
            if(x+r>=left && x-r<=right && y+r>=bottom && y-r<=top) {
                batch.setColor(.2f,.75f,1,a*.16f);ring(batch,x,y,r,5);
                batch.setColor(.65f,.95f,1,a);ring(batch,x,y,r,1.5f);
                if(skills.ultimateActivationLevel()>=4) {batch.setColor(.4f,.9f,1,a*.45f);ring(batch,x,y,r*.94f,1);}
                float flash=10*(1-skills.ultimateProgress());batch.setColor(1,.95f,.55f,a*.8f);
                batch.draw(pixel,x-flash/2,y-flash/2,flash,flash);
            }
        }
        batch.setPackedColor(old);
    }
}
