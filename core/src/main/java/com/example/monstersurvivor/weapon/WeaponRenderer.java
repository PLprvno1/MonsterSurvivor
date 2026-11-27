package com.example.monstersurvivor.weapon;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.example.monstersurvivor.assets.GameAssets;

/** Pixel rectangles on one shared white texel. Culls before drawing; no particles/shaders/allocations. */
public final class WeaponRenderer {
    private final TextureRegion pixel;
    private final GameAssets assets;
    public WeaponRenderer(GameAssets assets) {this.assets=assets;pixel=assets.region("evolution_pixel");}
    public void draw(SpriteBatch batch,WeaponSystem weapons,float left,float bottom,float right,float top) {
        float old=batch.getPackedColor();var system=weapons.projectiles;
        var sword=weapons.loadout.get(WeaponType.SWORD);
        if(sword!=null && sword.evolved())for(int i=0;i<2;i++) {
            float a=weapons.orbitAngle()+i*MathUtils.PI,cx=weapons.orbitX(),cy=weapons.orbitY();
            float x=cx+MathUtils.cos(a)*com.example.monstersurvivor.config.EvolutionConfig.ORBIT_RADIUS,y=cy+MathUtils.sin(a)*com.example.monstersurvivor.config.EvolutionConfig.ORBIT_RADIUS;
            if(x<left-24 || x>right+24 || y<bottom-24 || y>top+24)continue;
            batch.setColor(.6f,.85f,1,.45f);sprite(batch,"slash",weapons.orbitTime(),x,y,28,a*MathUtils.radiansToDegrees);
            // Two visibly distinct steel blades, tangent to the orbit; shared existing white texel.
            float dx=-MathUtils.sin(a),dy=MathUtils.cos(a);
            batch.setColor(.35f,.55f,.85f,1);line(batch,x-dx*10,y-dy*10,x+dx*12,y+dy*12,5);
            batch.setColor(.9f,.97f,1,1);line(batch,x-dx*6,y-dy*6,x+dx*12,y+dy*12,2);
            batch.setColor(1,.8f,.25f,1);line(batch,x-dx*6-dy*4,y-dy*6+dx*4,x-dx*6+dy*4,y-dy*6-dx*4,2);
        }
        for(int i=0;i<3;i++) {
            var z=system.zone(i);if(!z.active || z.x+z.radius<left || z.x-z.radius>right || z.y+z.radius<bottom || z.y-z.radius>top)continue;
            float alpha=Math.min(1,(com.example.monstersurvivor.config.EvolutionConfig.FIRE_DURATION-z.age)*3);
            batch.setColor(1,.4f,.1f,alpha*.45f);sprite(batch,"burn",z.age,z.x,z.y,z.radius*2,0);
            for(int n=0;n<4;n++) {float a=n*MathUtils.PI/2+z.age;batch.setColor(1,.65f,.2f,alpha*.7f);sprite(batch,"burn",z.age+n*.07f,z.x+MathUtils.cos(a)*z.radius*.55f,z.y+MathUtils.sin(a)*z.radius*.55f,24,0);}
        }
        for(int i=0;i<system.size();i++) {
            var p=system.get(i);if(p.x<left-12 || p.x>right+12 || p.y<bottom-12 || p.y>top+12)continue;
            float x=Math.round(p.x),y=Math.round(p.y);
            if(p.evolved) {
                batch.setColor(1,1,1,1);sprite(batch,p.type==WeaponType.BOW?"arrow":"meteor",p.age,x,y,p.type==WeaponType.BOW?22:40,MathUtils.atan2(p.dy,p.dx)*MathUtils.radiansToDegrees);
                continue;
            }
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
                    if(e.visualScale>=2) {batch.setColor(1,1,1,alpha);sprite(batch,"blast",e.age,e.x,e.y,e.radius*2,0);continue;}
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
        // Group evolved web impact sprites together on the FX atlas to avoid one switch per branch.
        for(int i=0;i<system.effectCount();i++) {
            var e=system.effect(i);
            if(e.type==ProjectileSystem.EffectType.LIGHTNING && e.visualScale>=1.5f && e.age<.14f && e.x2>=left-8 && e.x2<=right+8 && e.y2>=bottom-8 && e.y2<=top+8) {
                batch.setColor(.8f,.95f,1,1);sprite(batch,"spark",e.age,e.x2,e.y2,16,0);
            }
        }
        batch.setPackedColor(old);
    }
    private void sprite(SpriteBatch batch,String name,float age,float x,float y,float size,float rotation) {
        var region=assets.evolutionFrame(name,age);batch.draw(region,Math.round(x)-size/2,Math.round(y)-size/2,size/2,size/2,size,size,1,1,rotation);
    }
    private void line(SpriteBatch batch,float x,float y,float tx,float ty,float width) {
        float dx=tx-x,dy=ty-y,length=(float)Math.sqrt(dx*dx+dy*dy);
        batch.draw(pixel,x,y-width/2,0,width/2,length,width,1,1,MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees);
    }
}
