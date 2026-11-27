package com.example.monstersurvivor.ui;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.skill.*;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Icon-led skills; geometry/hit-test share the same HUD-space layout. No skill mutation. */
public final class SkillHud {
    public static final float FONT_SCALE=.65f;
    private static final SkillType[] TYPES=SkillType.values();
    private final GlyphLayout[] names={new GlyphLayout(),new GlyphLayout()},status={new GlyphLayout(),new GlyphLayout()},levels={new GlyphLayout(),new GlyphLayout()};
    private final int[] previous={Integer.MIN_VALUE,Integer.MIN_VALUE};
    private final int[] previousLevel={0,0};
    private final StringBuilder countdown=new StringBuilder(12);
    private long sessionId=-1;
    public static float radius(SkillType type) {return type==SkillType.DASH?30:40;}
    public static float centerX(SkillType type,float width,float safeRight) {return width-safeRight-(type==SkillType.DASH?160:68);}
    public static float centerY(SkillType type,float height,float safeBottom) {return Math.min(height-116-radius(type),safeBottom+(type==SkillType.DASH?162:180));}
    public static SkillType at(float x,float y,float width,float height,float safeRight,float safeBottom) {
        for(SkillType type:TYPES) {float dx=x-centerX(type,width,safeRight),dy=y-centerY(type,height,safeBottom),r=radius(type);if(dx*dx+dy*dy<=r*r)return type;}
        return null;
    }
    public void prepare(BitmapFont font,GameSession session,boolean enabled) {
        float sx=font.getData().scaleX,sy=font.getData().scaleY;font.getData().setScale(FONT_SCALE);
        if(sessionId!=session.id) {
            sessionId=session.id;java.util.Arrays.fill(previous,Integer.MIN_VALUE);
            java.util.Arrays.fill(previousLevel,0);
        }
        for(SkillType type:TYPES) {
            var state=session.skills.view(type);int value=!enabled?-3:state.phase()==ActiveSkillSystem.Phase.READY?-1:state.phase()==ActiveSkillSystem.Phase.ACTIVE?-2:(int)Math.ceil(state.cooldownRemaining());
            int i=type.ordinal();
            if(previousLevel[i]!=state.level()) {
                previousLevel[i]=state.level();names[i].setText(font,session.config.strings.get(type==SkillType.DASH?SKILL_DASH:SKILL_ULTIMATE));
                levels[i].setText(font,"Lv"+state.level());
            }
            if(previous[i]==value)continue;previous[i]=value;
            if(value<0)status[i].setText(font,session.config.strings.get(value==-1?SKILL_READY:value==-2?SKILL_ACTIVE:SKILL_DISABLED));
            else {countdown.setLength(0);countdown.append(value);status[i].setText(font,countdown);}
        }
        font.getData().setScale(sx,sy);
    }
    public void shapes(ShapeRenderer shapes,GameSession session,boolean enabled,float width,float height,float right,float bottom) {
        for(SkillType type:TYPES) {
            var state=session.skills.view(type);float x=centerX(type,width,right),y=centerY(type,height,bottom),r=radius(type);
            boolean ready=enabled && state.phase()==ActiveSkillSystem.Phase.READY;
            float red=type==SkillType.DASH?.3f:.95f,green=type==SkillType.DASH?.85f:.72f,blue=type==SkillType.DASH?1:.25f;
            boolean active=enabled && state.phase()==ActiveSkillSystem.Phase.ACTIVE;
            shapes.setColor(red,green,blue,active?.55f:ready?.18f:.06f);shapes.circle(x,y,r+5,32);
            shapes.setColor(.035f,.055f,.085f,.94f);shapes.circle(x,y,r+2,32);
            shapes.setColor(red,green,blue,enabled?.25f:.12f);shapes.circle(x,y,r,32);
            shapes.setColor(red,green,blue,ready?.95f:enabled?.65f:.25f);
            if(state.rechargeProgress()>0)shapes.arc(x,y,r,90,360*state.rechargeProgress(),32);
            shapes.setColor(.035f,.065f,.095f,.96f);shapes.circle(x,y,r-3,32);
            shapes.setColor(red,green,blue,ready||active?1:enabled?.45f:.2f);
            HudStyle.skill(shapes,type,x,y+9,type==SkillType.DASH?11:17);
            // Level tab and steady ready tick: no idle animation or flash.
            HudStyle.panel(shapes,x-14,y-r-6,28,14,red*.75f,green*.75f,blue*.75f);
            if(ready) {shapes.setColor(red,green,blue,1);shapes.rect(x-3,y+r-4,6,3);}
        }
    }
    public void text(SpriteBatch batch,BitmapFont font,boolean enabled,float width,float height,float right,float bottom) {
        float sx=font.getData().scaleX,sy=font.getData().scaleY;font.getData().setScale(FONT_SCALE);
        font.setColor(enabled?1:.55f,enabled?.95f:.55f,enabled?.75f:.55f,1);
        for(SkillType type:TYPES) {int i=type.ordinal();float x=centerX(type,width,right),y=centerY(type,height,bottom);
            font.draw(batch,names[i],x-names[i].width/2,y-radius(type)-15);
            font.draw(batch,status[i],x-status[i].width/2,y-10);
            font.draw(batch,levels[i],x-levels[i].width/2,y-radius(type)+4);}
        font.getData().setScale(sx,sy);font.setColor(1,1,1,1);
    }
}
