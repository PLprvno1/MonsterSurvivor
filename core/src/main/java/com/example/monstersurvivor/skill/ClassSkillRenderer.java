package com.example.monstersurvivor.skill;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.entity.Player;

/** Bounded class-skill FX built from the existing evolution and enemy-FX atlases. */
public final class ClassSkillRenderer {
    private final GameAssets assets;
    private final com.badlogic.gdx.graphics.g2d.TextureRegion pixel;
    public ClassSkillRenderer(GameAssets assets){this.assets=assets;pixel=assets.region("evolution_pixel");}
    public void draw(SpriteBatch batch,ClassSkillSystem skills,float left,float bottom,float right,float top) {
        draw(batch,skills,null,left,bottom,right,top);
    }
    public void draw(SpriteBatch batch,ClassSkillSystem skills,Player player,float left,float bottom,float right,float top) {
        float old=batch.getPackedColor();
        for(int i=0;i<skills.projectileCapacity();i++) {
            var shot=skills.projectile(i);if(!shot.active || shot.x<left-20 || shot.x>right+20 || shot.y<bottom-20 || shot.y>top+20)continue;
            float rotation=MathUtils.atan2(shot.dy,shot.dx)*MathUtils.radiansToDegrees;batch.setColor(shot.poison?.55f:.82f,shot.poison?.96f:.92f,shot.poison?.28f:1,1);
            if(shot.ice)enemySprite(batch,"ice_shard_right",shot.age,shot.x,shot.y,shot.smallIceShard?12:18,rotation);
            else sprite(batch,shot.arrow?"arrow":"slash",shot.age,shot.x,shot.y,shot.arrow?13:19,rotation);
        }
        if(skills.marked() && visible(skills.markX(),skills.markY(),10,left,bottom,right,top)) {
            batch.setColor(1,.72f,.24f,.9f);ring(batch,skills.markX(),skills.markY(),10);
            sprite(batch,"spark",0,skills.markX(),skills.markY()+10,10,0);
        }
        for(int i=0;i<skills.trapCapacity();i++) {
            var trap=skills.trap(i);if(!trap.active || !visible(trap.x,trap.y,trap.radius,left,bottom,right,top))continue;
            float armed=Math.min(1,trap.age/ClassSkillTuning.trapArmDelay());
            if(trap.pulsing) {
                float pulse=.5f+.5f*MathUtils.sin(trap.pulseClock*18);
                batch.setColor(.78f,.58f,1,.62f+pulse*.3f);ring(batch,trap.x,trap.y,trap.radius*(.72f+pulse*.18f));
            } else {batch.setColor(.62f,.48f,1,.42f+armed*.38f);ring(batch,trap.x,trap.y,ClassSkillTuning.trapTriggerRadius());}
            batch.setColor(.85f,.74f,1,.55f+armed*.35f);drawRune(batch,trap.x,trap.y,9);
        }
        if(skills.channelActive() && player!=null && player.alive() && visible(player.x,player.y,24,left,bottom,right,top)) {
            float pulse=.5f+.5f*MathUtils.sin(skills.channelRemaining()*8);
            batch.setColor(.45f,.78f,1,.5f+pulse*.35f);ring(batch,player.x,player.y,12+pulse*2);
            batch.setColor(.78f,.92f,1,.45f+pulse*.35f);sprite(batch,"spark",skills.channelRemaining(),player.x,player.y+13,14,0);
        }
        if(skills.warCryActive() && player!=null && player.alive() && visible(player.x,player.y,24,left,bottom,right,top)) {
            float pulse=.5f+.5f*MathUtils.sin(skills.warCryRemaining()*9);
            batch.setColor(1,.72f,.2f,.38f+pulse*.25f);ring(batch,player.x,player.y,9+pulse*2);
            batch.setColor(1,.87f,.48f,.5f+pulse*.35f);sprite(batch,"spark",skills.warCryRemaining(),player.x,player.y+11,15,0);
        }
        for(int i=0;i<skills.effectCapacity();i++) {
            var fx=skills.effect(i);if(!fx.active || !visible(fx.x,fx.y,fx.size,left,bottom,right,top))continue;
            float alpha=Math.max(0,1-fx.age/fx.duration);
            if(fx.kind==ClassSkillSystem.FxKind.SLASH) {
                batch.setColor(.72f,.88f,1,alpha*.85f);sprite(batch,"slash",fx.age,fx.x,fx.y,fx.size,fx.age/fx.duration*300);
            } else if(fx.kind==ClassSkillSystem.FxKind.SPARK) {
                batch.setColor(1,.92f,.62f,alpha);sprite(batch,"spark",fx.age,fx.x,fx.y,fx.size,0);
            } else if(fx.kind==ClassSkillSystem.FxKind.ARROW_WARNING) {
                batch.setColor(1,.76f,.24f,alpha*.85f);ring(batch,fx.x,fx.y,fx.size*.5f);
            } else if(fx.kind==ClassSkillSystem.FxKind.ARROW_PULSE) {
                batch.setColor(1,.94f,.62f,alpha);sprite(batch,"arrow",fx.age,fx.x,fx.y,fx.size,MathUtils.atan2(fx.dirY,fx.dirX)*MathUtils.radiansToDegrees);
            } else if(fx.kind==ClassSkillSystem.FxKind.ICE_IMPACT) {
                batch.setColor(.65f,.88f,1,alpha);enemySprite(batch,"ice_shard_impact",fx.age,fx.x,fx.y,fx.size,0);
            } else if(fx.kind==ClassSkillSystem.FxKind.POISON_IMPACT) {
                batch.setColor(.48f,1,.3f,alpha);enemySprite(batch,"overlay_poison",fx.age,fx.x,fx.y,fx.size,0);
            } else if(fx.kind==ClassSkillSystem.FxKind.WAR_CRY) {
                float radius=fx.size*.5f*(.4f+.6f*fx.age/fx.duration);
                batch.setColor(1,.7f,.18f,alpha*.85f);ring(batch,fx.x,fx.y,radius);
                batch.setColor(1,.88f,.5f,alpha);sprite(batch,"spark",fx.age,fx.x,fx.y+9,20,0);
            } else if(fx.kind==ClassSkillSystem.FxKind.TRAP_CAST || fx.kind==ClassSkillSystem.FxKind.CHANNEL_CAST) {
                batch.setColor(.8f,.7f,1,alpha);enemySprite(batch,"cast",fx.age,fx.x,fx.y,fx.size,0);
            } else if(fx.kind==ClassSkillSystem.FxKind.TRAP_BLAST) {
                batch.setColor(.72f,.58f,1,alpha);sprite(batch,"blast",fx.age,fx.x,fx.y,fx.size,0);
            } else if(fx.kind==ClassSkillSystem.FxKind.SHIELD) {
                batch.setColor(.58f,.86f,1,alpha);enemySprite(batch,"magic_orb_impact",fx.age,fx.x,fx.y,fx.size,0);
            } else if(fx.kind==ClassSkillSystem.FxKind.SHIELD_PULSE || fx.kind==ClassSkillSystem.FxKind.PARRY_FULL_ARC) {
                float radius=fx.size*.5f*(.45f+.55f*fx.age/fx.duration);
                batch.setColor(fx.kind==ClassSkillSystem.FxKind.SHIELD_PULSE?.58f:.92f,fx.kind==ClassSkillSystem.FxKind.SHIELD_PULSE?.86f:.82f,1,alpha*.9f);ring(batch,fx.x,fx.y,radius);
            } else drawParry(batch,fx.x,fx.y,fx.dirX,fx.dirY,alpha);
        }
        batch.setPackedColor(old);
    }
    private boolean visible(float x,float y,float margin,float left,float bottom,float right,float top) {
        return x+margin>=left && x-margin<=right && y+margin>=bottom && y-margin<=top;
    }
    private void drawParry(SpriteBatch batch,float x,float y,float dx,float dy,float alpha) {
        float center=MathUtils.atan2(dy,dx),radius=15;
        batch.setColor(.4f,.78f,1,alpha*.9f);
        float px=x+MathUtils.cos(center-MathUtils.degreesToRadians*70)*radius;
        float py=y+MathUtils.sin(center-MathUtils.degreesToRadians*70)*radius;
        for(int i=1;i<=12;i++) {
            float angle=center-MathUtils.degreesToRadians*70+MathUtils.degreesToRadians*(140f*i/12);
            float nx=x+MathUtils.cos(angle)*radius,ny=y+MathUtils.sin(angle)*radius;
            drawLine(batch,px,py,nx,ny,2);px=nx;py=ny;
        }
        batch.setColor(.9f,.98f,1,alpha);sprite(batch,"spark",0,x+dx*8,y+dy*8,15,0);
    }
    private void drawRune(SpriteBatch batch,float x,float y,float radius) {
        for(int i=0;i<4;i++) {
            float angle=MathUtils.PI/4+i*MathUtils.PI/2,dx=MathUtils.cos(angle),dy=MathUtils.sin(angle);
            drawLine(batch,x+dx*radius*.6f,y+dy*radius*.6f,x+dx*radius,y+dy*radius,2);
        }
        ring(batch,x,y,radius*.42f);
    }
    private void ring(SpriteBatch batch,float x,float y,float radius) {
        float px=x+radius,py=y;
        for(int i=1;i<=24;i++) {
            float angle=MathUtils.PI2*i/24,nx=x+MathUtils.cos(angle)*radius,ny=y+MathUtils.sin(angle)*radius;
            drawLine(batch,px,py,nx,ny,2);px=nx;py=ny;
        }
    }
    private void drawLine(SpriteBatch batch,float x,float y,float nx,float ny,float width) {
        float dx=nx-x,dy=ny-y;
        batch.draw(pixel,x,y-width/2,0,width/2,(float)Math.hypot(dx,dy),width,1,1,MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees);
    }
    private void sprite(SpriteBatch batch,String name,float age,float x,float y,float size,float rotation) {
        var region=assets.evolutionFrame(name,age);
        batch.draw(region,Math.round(x)-size/2,Math.round(y)-size/2,size/2,size/2,size,size,1,1,rotation);
    }
    private void enemySprite(SpriteBatch batch,String name,float age,float x,float y,float width,float rotation) {
        var region=assets.enemyFrame(name,age);float height=width*region.getRegionHeight()/region.getRegionWidth();
        batch.draw(region,Math.round(x)-width/2,Math.round(y)-height/2,width/2,height/2,width,height,1,1,rotation);
    }
}
