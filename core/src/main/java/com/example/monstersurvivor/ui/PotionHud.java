package com.example.monstersurvivor.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.monstersurvivor.consumable.PotionSystem;
import com.example.monstersurvivor.consumable.PotionType;
import com.example.monstersurvivor.gameplay.GameSession;

/** Three consumable buttons aligned directly below the existing three passive slots. */
public final class PotionHud {
    private static final PotionType[] TYPES=PotionType.values();
    private static final float BUTTON_WIDTH=46,BUTTON_HEIGHT=24,ROW_OFFSET=1;
    private final GlyphLayout[] counts={new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};
    private final int[] lastCounts={-1,-1,-1};
    private static final float[] R={.88f,.22f,.22f},G={.25f,.82f,.45f},B={.35f,.62f,1f};

    public static float centerX(float width,int index){return width/2-146+index*146;}
    public static float centerY(float safeBottom){return safeBottom+ROW_OFFSET+BUTTON_HEIGHT/2;}
    public static PotionType at(float x,float y,float width,float safeBottom){
        if(y<safeBottom || y>safeBottom+BUTTON_HEIGHT+2)return null;
        for(int i=0;i<TYPES.length;i++)if(Math.abs(x-centerX(width,i))<=BUTTON_WIDTH/2+2)return TYPES[i];
        return null;
    }
    public void prepare(BitmapFont font,GameSession session){
        for(PotionType type:TYPES){
            int i=type.ordinal(),count=session.potions.count(type);
            if(lastCounts[i]!=count){lastCounts[i]=count;counts[i].setText(font,Integer.toString(count));}
        }
    }
    public void shapes(ShapeRenderer shapes,GameSession session,boolean enabled,float width,float safeBottom,boolean pending){
        for(PotionType type:TYPES){
            int i=type.ordinal();float x=centerX(width,i),y=safeBottom+ROW_OFFSET;
            PotionSystem.Availability availability=session.potionAvailability(type);
            boolean usable=enabled && !pending && availability==PotionSystem.Availability.USABLE;
            boolean active=type==PotionType.SPEED?session.potions.speedRemaining()>0
                :type==PotionType.COOLDOWN && session.potions.cooldownRemaining()>0;
            float alpha=usable||active?.95f:.48f;
            float r=R[i],g=G[i],b=B[i];
            HudStyle.panel(shapes,x-BUTTON_WIDTH/2,y,BUTTON_WIDTH,BUTTON_HEIGHT,r,g,b);
            shapes.setColor(r,g,b,alpha);drawIcon(shapes,type,x,y+14);
            if(active){
                float remaining=type==PotionType.SPEED?session.potions.speedRemaining():session.potions.cooldownRemaining();
                shapes.setColor(1,.82f,.30f,.95f);shapes.rect(x-BUTTON_WIDTH/2+2,y+2,(BUTTON_WIDTH-4)*Math.max(0,Math.min(1,remaining/type.duration())),2);
            }
            shapes.setColor(.025f,.04f,.04f,.9f);shapes.rect(x+10,y+1,13,11);
        }
    }
    private static void drawIcon(ShapeRenderer s,PotionType type,float x,float y){
        if(type==PotionType.HEALTH){
            s.rect(x-7,y+1,5,5);s.rect(x+2,y+1,5,5);s.rect(x-9,y-3,5,5);s.rect(x+4,y-3,5,5);
            s.rect(x-5,y-3,10,5);s.rect(x-3,y-7,6,4);s.rect(x-1,y-10,2,3);
        }else if(type==PotionType.SPEED){
            s.rect(x-9,y-5,5,3);s.rect(x-6,y-1,5,3);s.rect(x-3,y+3,5,3);
            s.triangle(x+1,y-3,x+1,y+9,x+10,y+3);
        }else{
            s.circle(x,y+1,7,12);s.setColor(.10f,.16f,.29f,.95f);s.circle(x,y+1,5,12);
            s.setColor(.35f,.62f,1f,.95f);s.rect(x-1,y+1,2,4);s.rect(x,y+1,4,2);
        }
    }
    public void text(SpriteBatch batch,BitmapFont font,GameSession session,float width,float safeBottom,boolean enabled,boolean pending){
        font.setColor(1,1,1,1);
        for(PotionType type:PotionType.values()){
            int i=type.ordinal();float x=centerX(width,i),y=safeBottom+ROW_OFFSET;
            PotionSystem.Availability availability=session.potionAvailability(type);
            boolean active=type==PotionType.SPEED?session.potions.speedRemaining()>0
                :type==PotionType.COOLDOWN && session.potions.cooldownRemaining()>0;
            if(active)font.setColor(1,.88f,.45f,1);
            else if(enabled && !pending && availability==PotionSystem.Availability.USABLE)font.setColor(1,1,1,1);
            else font.setColor(.62f,.64f,.62f,1);
            font.draw(batch,counts[i],x+16.5f-counts[i].width/2,y+10);
        }
        font.setColor(1,1,1,1);
    }
}
