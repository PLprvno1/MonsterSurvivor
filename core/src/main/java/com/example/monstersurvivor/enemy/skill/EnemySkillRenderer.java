package com.example.monstersurvivor.enemy.skill;

import com.badlogic.gdx.graphics.g2d.*;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.ai.EnemySystem;

/** One atlas, culled and bounded. Sprites provide flight/impact/status; geometry supplies fair warnings. */
public final class EnemySkillRenderer {
    private final GameAssets assets;private final TextureRegion pixel;
    private static final float[] X=new float[25],Y=new float[25],ANGLE=new float[24];
    private static final String[] STATUS_OVERLAYS={"overlay_poison","overlay_burn","overlay_slow"};
    static{for(int i=0;i<=24;i++){X[i]=(float)Math.cos(i*Math.PI/12);Y[i]=(float)Math.sin(i*Math.PI/12);}for(int i=0;i<24;i++)ANGLE[i]=(float)Math.toDegrees(Math.atan2(Y[i+1]-Y[i],X[i+1]-X[i]));}
    public EnemySkillRenderer(GameAssets assets){this.assets=assets;pixel=assets.region("enemy_pixel");}
    private void line(SpriteBatch b,float x,float y,float dx,float dy,float width){float len=(float)Math.hypot(dx,dy);b.draw(pixel,x,y-width/2,0,width/2,len,width,1,1,(float)Math.toDegrees(Math.atan2(dy,dx)));}
    private void ring(SpriteBatch b,float x,float y,float radius){for(int i=0;i<24;i++)b.draw(pixel,x+X[i]*radius,y+Y[i]*radius-.65f,0,.65f,radius*.2610524f,1.3f,1,1,ANGLE[i]);}
    private boolean visible(float x,float y,float radius,float left,float bottom,float right,float top){return x+radius>=left && x-radius<=right && y+radius>=bottom && y-radius<=top;}
    private void sprite(SpriteBatch b,String clip,float age,float x,float y,float width){var r=assets.enemyFrame(clip,age,clip.startsWith("overlay_"));float h=width*r.getRegionHeight()/r.getRegionWidth();b.draw(r,x-width/2,y-h/2,width,h);}
    public void draw(SpriteBatch b,EnemySystem enemies,Player player,float left,float bottom,float right,float top){
        for(int i=0;i<enemies.size();i++){
            var e=enemies.get(i);var s=e.specialSkill;var d=s.definition;
            if(!e.boss && e.alive() && e.slowRemaining()>0 && visible(e.x,e.y,28,left,bottom,right,top)) {
                b.setColor(.58f,.86f,1,.7f);sprite(b,"overlay_slow",e.animationTime,e.x,e.y+12,32);
            }
            if(e.alive() && e.poisoned() && visible(e.x,e.y,28,left,bottom,right,top)) {
                b.setColor(.48f,1,.32f,.8f);sprite(b,"overlay_poison",e.animationTime,e.x,e.y+12,32);
            }
            if(e.boss || !e.alive() || (s.phase!=EnemySkillInstance.Phase.PREPARE && s.phase!=EnemySkillInstance.Phase.RELEASE))continue;
            if(d.kind==EnemySkillDefinition.Kind.SHOT && visible(e.x,e.y,45,left,bottom,right,top)){
                b.setColor(1,.32f,.25f,.9f);ring(b,e.x,e.y,9);
                float length=Math.max(.001f,(float)Math.hypot(s.aimX,s.aimY)),dx=s.aimX/length,dy=s.aimY/length;
                for(int lane=0;lane<d.count;lane++){
                    double angle=Math.toRadians((lane-(d.count-1)*.5f)*d.spreadOrRadius);float c=(float)Math.cos(angle),sn=(float)Math.sin(angle);
                    line(b,e.x,e.y,(dx*c-dy*sn)*34,(dx*sn+dy*c)*34,.85f);
                }
                b.setColor(1,.65f,.45f,.7f);sprite(b,"cast",s.age,e.x,e.y+13,13);
            }else if(d.kind!=EnemySkillDefinition.Kind.SHOT && visible(s.targetX,s.targetY,d.radius(),left,bottom,right,top)){
                b.setColor(1,.25f,.35f,.95f);ring(b,s.targetX,s.targetY,d.radius());
                b.setColor(1,.65f,.3f,.85f);ring(b,s.targetX,s.targetY,d.radius()*Math.min(1,s.age/Math.max(.01f,s.windup)));
                line(b,s.targetX-4,s.targetY,8,0,1);line(b,s.targetX,s.targetY-4,0,8,1);
            }
        }
        var pool=enemies.skills.projectiles;
        for(int i=0;i<EnemyProjectiles.AREA_CAPACITY;i++){
            var a=pool.area(i);if(!a.active || !visible(a.x,a.y,20,left,bottom,right,top))continue;
            b.setColor(.85f,.25f,.85f,.85f);ring(b,a.x,a.y,18);
            b.setColor(.45f,1,.4f,a.age<.65f?.3f:.7f);sprite(b,"overlay_poison",a.age,a.x,a.y,36);
        }
        for(int i=0;i<EnemyProjectiles.CAPACITY;i++){
            var s=pool.shot(i);if(!s.active || !visible(s.x,s.y,12,left,bottom,right,top))continue;
            b.setColor(1,.15f,.2f,.4f);b.draw(pixel,s.x-4,s.y-4,8,8);
            b.setColor(1,.82f,.82f,1);var frame=assets.enemyFrame(s.definition.directional[s.direction],s.age);
            b.draw(frame,s.x-8,s.y-8,8,8,16,16,1,1,s.rotation);
        }
        for(int i=0;i<EnemyProjectiles.FX_CAPACITY;i++){
            var f=pool.effect(i);if(!f.active || !visible(f.x,f.y,f.size,left,bottom,right,top))continue;
            b.setColor(1,.8f,.8f,Math.min(1,(f.duration-f.age)*8));sprite(b,f.clip,f.age,f.x,f.y,f.size);
        }
        if(player.alive() && visible(player.x,player.y,32,left,bottom,right,top))for(var type:PlayerStatuses.TYPES)if(player.statuses.active(type)){
            b.setColor(1,1,1,.55f);sprite(b,STATUS_OVERLAYS[type.ordinal()],player.animationTime,player.x,player.y+12,32);
        }
        b.setColor(1,1,1,1);
    }
}
