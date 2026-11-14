package com.example.monstersurvivor.weapon;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.example.monstersurvivor.assets.GameAssets;

/** Pixel rectangles on one shared white texel. Culls before drawing; no particles/shaders/allocations. */
public final class WeaponRenderer {
    private final TextureRegion pixel;
    public WeaponRenderer(GameAssets assets) {pixel=assets.region("weapon_pixel");}
    public void draw(SpriteBatch batch,WeaponSystem weapons,float left,float bottom,float right,float top) {
        float old=batch.getPackedColor();var system=weapons.projectiles;
        for(int i=0;i<system.size();i++) {
            var p=system.get(i);if(p.x<left-12 || p.x>right+12 || p.y<bottom-12 || p.y>top+12)continue;
            float x=Math.round(p.x),y=Math.round(p.y);
            if(p.type==WeaponType.BOW) {
                batch.setColor(.9f,.8f,.5f,1);line(batch,x-p.dx*5,y-p.dy*5,x+p.dx*5,y+p.dy*5,2);
                batch.setColor(.9f,1,1,1);batch.draw(pixel,x+p.dx*5-1,y+p.dy*5-1,3,3);
            } else {
                float r=3*p.visualScale;
                batch.setColor(1,.25f,.08f,.14f*p.glow);batch.draw(pixel,x-r*2,y-r*2,r*4,r*4);
                batch.setColor(1,.25f,.08f,.8f);batch.draw(pixel,x-p.dx*4-3,y-p.dy*4-3,6,6);
                batch.setColor(1,.65f,.15f,1);batch.draw(pixel,x-r,y-r,r*2,r*2);
                batch.setColor(1,1,.6f,1);batch.draw(pixel,x-r/2,y-r/2,r,r);
            }
        }
        for(int i=0;i<system.effectCount();i++) {
            var e=system.effect(i);float size=Math.max(8,e.radius);
            if(e.type==ProjectileSystem.EffectType.LIGHTNING) {
                if(Math.max(e.x,e.x2)<left || Math.min(e.x,e.x2)>right || Math.max(e.y,e.y2)<bottom || Math.min(e.y,e.y2)>top)continue;
                float dx=e.x2-e.x,dy=e.y2-e.y,len=(float)Math.sqrt(dx*dx+dy*dy),sx=len>.001f?-dy/len*4:0,sy=len>.001f?dx/len*4:0;
                float ax=Math.round(e.x+dx*.33f+sx),ay=Math.round(e.y+dy*.33f+sy);
                float bx=Math.round(e.x+dx*.67f-sx),by=Math.round(e.y+dy*.67f-sy);
                float a=1-e.age/e.lifetime,w=e.boltWidth;
                batch.setColor(.25f,.45f,1,a*.18f*e.glow);
                line(batch,e.x,e.y,ax,ay,w+3*e.glow);line(batch,ax,ay,bx,by,w+3*e.glow);line(batch,bx,by,e.x2,e.y2,w+3*e.glow);
                batch.setColor(.65f,.85f,1,a);
                line(batch,e.x,e.y,ax,ay,w);line(batch,ax,ay,bx,by,w);line(batch,bx,by,e.x2,e.y2,w);
                float r=3*e.visualScale;batch.setColor(.8f,.95f,1,a);batch.draw(pixel,Math.round(e.x2)-r,Math.round(e.y2)-r,r*2,r*2);
            } else {
                if(e.x+size<left || e.x-size>right || e.y+size<bottom || e.y-size>top)continue;
                float alpha=1-e.age/e.lifetime;
                if(e.type==ProjectileSystem.EffectType.EXPLOSION) {
                    float r=Math.round(4+e.radius*e.age/e.lifetime),x=Math.round(e.x),y=Math.round(e.y),core=6*e.visualScale;
                    batch.setColor(1,.2f,.05f,alpha*.1f*e.glow);batch.draw(pixel,x-r,y-r,r*2,r*2);
                    batch.setColor(1,.55f,.15f,alpha);
                    batch.draw(pixel,x-r,y-r,r*2,2);batch.draw(pixel,x-r,y+r-2,r*2,2);
                    batch.draw(pixel,x-r,y-r,2,r*2);batch.draw(pixel,x+r-2,y-r,2,r*2);
                    batch.setColor(1,.95f,.6f,alpha*.8f);batch.draw(pixel,x-core/2,y-core/2,core,core);
                } else {
                    batch.setColor(1,1,.8f,alpha);float x=Math.round(e.x),y=Math.round(e.y);
                    batch.draw(pixel,x-5,y-1,3,2);batch.draw(pixel,x+2,y-1,3,2);batch.draw(pixel,x-1,y+2,2,3);batch.draw(pixel,x-1,y-5,2,3);
                }
            }
        }
        batch.setPackedColor(old);
    }
    private void line(SpriteBatch batch,float x,float y,float tx,float ty,float width) {
        float dx=tx-x,dy=ty-y,length=(float)Math.sqrt(dx*dx+dy*dy);
        batch.draw(pixel,x,y-width/2,0,width/2,length,width,1,1,MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees);
    }
}
