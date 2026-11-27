package com.example.monstersurvivor.boss;

import com.badlogic.gdx.graphics.g2d.*;

/** Small, bounded effects using the already loaded white pixel; no new textures or full-screen passes. */
public final class BossRenderer {
    private final TextureRegion pixel;
    public BossRenderer(TextureRegion pixel){this.pixel=pixel;}
    private void line(SpriteBatch batch,float x,float y,float dx,float dy,float width) {
        float length=(float)Math.hypot(dx,dy);
        batch.draw(pixel,x,y-width/2,0,width/2,length,width,1,1,(float)Math.toDegrees(Math.atan2(dy,dx)));
    }
    private void ring(SpriteBatch batch,float x,float y,float radius,float width) {
        for(int i=0;i<24;i++) {double a=i*Math.PI/12,b=(i+1)*Math.PI/12;
            float ax=x+(float)Math.cos(a)*radius,ay=y+(float)Math.sin(a)*radius;
            line(batch,ax,ay,x+(float)Math.cos(b)*radius-ax,y+(float)Math.sin(b)*radius-ay,width);}
    }
    public void draw(SpriteBatch batch,BossSystem system,float left,float bottom,float right,float top) {
        for(int i=0;i<2;i++) {
            BossInstance b=i==0?system.mini():system.lich();if(b==null || !b.actor.alive())continue;
            float x=b.actor.x,y=b.actor.y;
            if(x>=left-28 && x<=right+28 && y>=bottom-28 && y<=top+28) {
                batch.setColor(b.finalBoss()?.65f:1,b.phase()==2?.16f:.55f,b.finalBoss()?1:.15f,.6f);
                ring(batch,x,y,15,1);
                if(b.cast()==BossInstance.Cast.PHASE_SHIFT)ring(batch,x,y,18+b.warningProgress()*22,2);
                if(b.cast()==BossInstance.Cast.BOLT)line(batch,x,y+4,b.warningX()-x,b.warningY()-y-4,.7f);
            }
            if(b.warningRadius()>0 && (b.cast()==BossInstance.Cast.SLAM || b.cast()==BossInstance.Cast.AREA || b.impactTime()>0)) {
                x=b.warningX();y=b.warningY();float r=b.warningRadius();
                if(x+r<left || x-r>right || y+r<bottom || y-r>top)continue;
                batch.setColor(1,b.finalBoss()?.22f:.65f,b.finalBoss()?.65f:.15f,b.impactTime()>0?b.impactTime()*3:.8f);
                ring(batch,x,y,r,1.5f);ring(batch,x,y,r*(b.impactTime()>0?1:b.warningProgress()),1);
            }
        }
        for(int i=0;i<system.projectiles.capacity();i++) {
            var p=system.projectiles.get(i);if(!p.active || p.x<left-6 || p.x>right+6 || p.y<bottom-6 || p.y>top+6)continue;
            batch.setColor(.65f,.15f,1,.28f);batch.draw(pixel,p.x-5,p.y-5,10,10);
            batch.setColor(.95f,.5f,1,1);batch.draw(pixel,p.x-2,p.y-2,4,4);
        }
        batch.setColor(1,1,1,1);
    }
}
