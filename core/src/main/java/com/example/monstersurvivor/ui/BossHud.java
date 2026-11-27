package com.example.monstersurvivor.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.monstersurvivor.boss.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.localization.GameStrings;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Cached, high-contrast Boss strip below player stats; never mutates encounter state. */
final class BossHud {
    private final GlyphLayout name=new GlyphLayout(),health=new GlyphLayout(),secondary=new GlyphLayout(),announcement=new GlyphLayout(),annName=new GlyphLayout();
    private final GlyphLayout victory=new GlyphLayout(),miniResult=new GlyphLayout(),finalResult=new GlyphLayout(),modeResult=new GlyphLayout();
    private final StringBuilder text=new StringBuilder(96);
    private BossInstance primary,mini;private long session=-1;private int hp=-1,phase=-1,notice=-1;private boolean miniDead;
    private float fade,markerX,markerY,dx,dy;private boolean marker;
    private boolean preparedResult;
    private float nameScale=.85f;
    void prepare(BitmapFont font,GameSession s,OrthographicCamera camera,float w,float h,float safeRight) {
        if(session!=s.id) {session=s.id;hp=phase=notice=-1;primary=null;preparedResult=false;miniDead=false;}
        var boss=s.bosses;var current=boss.primary();var strings=s.config.strings;
        if(current!=primary || current!=null && (hp!=current.hp() || phase!=current.phase())) {
            primary=current;
            if(primary!=null) {
                hp=primary.hp();phase=primary.phase();text.setLength(0);
                text.append(strings.get(s.endless()?ENDLESS_BOSS:primary.finalBoss()?FINAL_BOSS:MINI_BOSS)).append(" - ").append(strings.get(primary.definition.nameKey()));
                if(primary.finalBoss())text.append(" / ").append(strings.get(phase==1?PHASE_ONE:PHASE_TWO));
                nameScale=HudStyle.fit(font,name,text,.85f,464);text.setLength(0);text.append(strings.get(BOSS_HP)).append(' ').append(hp).append('/').append(primary.maxHp());HudStyle.fit(font,health,text,.75f,464);
            }
        }
        var m=boss.mini();boolean dead=m!=null && !m.actor.alive();
        if(m!=mini || miniDead!=dead) {mini=m;miniDead=dead;text.setLength(0);if(m!=null)text.append(strings.get(MINI_BOSS)).append(": ").append(strings.get(dead?BOSS_DEFEATED:m.definition.nameKey()));HudStyle.fit(font,secondary,text,.75f,464);}
        if(notice!=boss.noticeSerial) {
            notice=boss.noticeSerial;announcement.setText(font,strings.get(boss.noticeKey()));
            annName.setText(font,boss.noticeBoss()==null?"":strings.get(boss.noticeBoss().nameKey()));
        }
        fade=Math.min(1,boss.noticeTime()/.4f);marker=false;
        if(primary!=null && primary.actor.alive()) {
            dx=primary.actor.x-camera.position.x;dy=primary.actor.y-camera.position.y;
            if(Math.abs(dx)>camera.viewportWidth/2 || Math.abs(dy)>camera.viewportHeight/2) {
                marker=true;float len=(float)Math.hypot(dx,dy);dx/=len;dy/=len;
                // Reserved strip above weapon slots, outside the floating joystick activation zone.
                markerX=Math.max(w*.47f,Math.min(w-safeRight-230,w/2+dx*w*.4f));markerY=Math.max(170,Math.min(h-210,h/2+dy*h*.3f));
                if(fade>0)markerY=Math.min(markerY,h-258);
            }
        }
        if(s.victory() && !preparedResult) {
            preparedResult=true;victory.setText(font,strings.get(VICTORY));
            miniResult.setText(font,strings.get(MINI_BOSS)+": "+strings.get(s.result().miniBossDefeated()?YES:NO));
            finalResult.setText(font,strings.get(FINAL_BOSS)+": "+strings.get(s.result().finalBossDefeated()?YES:NO));
            modeResult.setText(font,strings.get(s.config.difficulty==com.example.monstersurvivor.config.Difficulty.HARD?HARD:EASY)+" / "+strings.get(s.config.mode==com.example.monstersurvivor.config.GameMode.DEMO?DEMO:NORMAL));
        }
    }
    void shapes(ShapeRenderer shapes,float w,float h) {
        if(primary!=null) {
            float width=Math.min(480,w-160),x=(w-width)/2;
            HudStyle.panel(shapes,x-8,h-154,width+16,56,.8f,.48f,.65f);
            shapes.setColor(.6f,.43f,.22f,1);shapes.rect(x,h-145,width,10);
            shapes.setColor(.15f,.035f,.08f,1);shapes.rect(x+1,h-144,width-2,8);
            shapes.setColor(primary.finalBoss()?.65f:.75f,.13f,primary.finalBoss()?.65f:.18f,1);
            shapes.rect(x+1,h-144,(width-2)*primary.hp()/primary.maxHp(),8);
            if(primary.finalBoss() && mini!=null && mini.visible()) {
                shapes.setColor(.025f,.045f,.065f,.94f);shapes.rect(x-8,h-177,width+16,21);
            }
        }
        if(fade>0) {float width=Math.max(announcement.width,annName.width)+32;shapes.setColor(.045f,.025f,.065f,.82f*fade);shapes.rect((w-width)/2,h-228,width,47);}
        if(marker) {
            shapes.setColor(.75f,.35f,.9f,1);shapes.circle(markerX,markerY,8,12);
            shapes.triangle(markerX+dx*21,markerY+dy*21,markerX-dy*6,markerY+dx*6,markerX+dy*6,markerY-dx*6);
        }
    }
    void text(SpriteBatch batch,BitmapFont font,float w,float h) {
        font.setColor(1,.9f,.7f,1);
        if(primary!=null) {
            HudStyle.draw(batch,font,name,nameScale,(w-name.width)/2,h-107);
            HudStyle.draw(batch,font,health,.75f,(w-health.width)/2,h-124);
            if(primary.finalBoss() && mini!=null && mini.visible())HudStyle.draw(batch,font,secondary,.75f,(w-secondary.width)/2,h-162);
        }
        if(fade>0) {
            var cache=font.getCache();cache.clear();cache.addText(announcement,(w-announcement.width)/2,h-187);cache.addText(annName,(w-annName.width)/2,h-208);
            cache.setColors(1,.78f,.4f,fade);cache.draw(batch);
        }
        font.setColor(1,1,1,1);
    }
    void victoryText(SpriteBatch batch,BitmapFont font,float w,float h) {
        font.setColor(1,.88f,.4f,1);font.draw(batch,victory,(w-victory.width)/2,h/2+118);
        font.setColor(1,.92f,.75f,1);font.draw(batch,miniResult,(w-miniResult.width)/2,h/2+65);font.draw(batch,finalResult,(w-finalResult.width)/2,h/2+44);font.draw(batch,modeResult,(w-modeResult.width)/2,h/2+23);
    }
}
