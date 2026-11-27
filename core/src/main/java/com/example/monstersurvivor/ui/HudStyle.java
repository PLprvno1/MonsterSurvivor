package com.example.monstersurvivor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.example.monstersurvivor.skill.SkillType;
import com.example.monstersurvivor.weapon.WeaponType;

/** Shared pixel-cut frames and icons. Uses the existing filled-shapes pass; owns no GPU resource. */
public final class HudStyle {
    private static final float[] CIRCLE_X=new float[33],CIRCLE_Y=new float[33];
    static {for(int i=0;i<=32;i++){double a=i*Math.PI/16;CIRCLE_X[i]=(float)Math.cos(a);CIRCLE_Y[i]=(float)Math.sin(a);}}
    private HudStyle() { }
    public static void beginOverlay(ShapeRenderer shapes) {
        // SpriteBatch.end disables blending. Restore it for the later modal shapes pass.
        Gdx.gl.glEnable(GL20.GL_BLEND);Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }
    public static void ring(ShapeRenderer s,float x,float y,float radius,float thickness) {
        float px=x+radius,py=y;
        for(int i=1;i<=32;i++) {float nx=x+radius*CIRCLE_X[i],ny=y+radius*CIRCLE_Y[i];
            s.rectLine(px,py,nx,ny,thickness);px=nx;py=ny;}
    }
    public static void panel(ShapeRenderer s,float x,float y,float w,float h,float r,float g,float b) {
        s.setColor(.025f,.045f,.065f,.94f);s.rect(x,y,w,h);
        s.setColor(r*.42f,g*.42f,b*.42f,1);s.rect(x,y,w,1);s.rect(x,y+h-1,w,1);s.rect(x,y,1,h);s.rect(x+w-1,y,1,h);
        s.setColor(r,g,b,1);
        s.rect(x,y+h-3,12,3);s.rect(x+w-12,y+h-3,12,3);s.rect(x,y,12,3);s.rect(x+w-12,y,12,3);
        s.rect(x,y,3,10);s.rect(x+w-3,y,3,10);s.rect(x,y+h-10,3,10);s.rect(x+w-3,y+h-10,3,10);
    }
    public static void card(ShapeRenderer s,float x,float y,boolean selected,boolean gold,float r,float g,float b) {
        panel(s,x,y,620,76,gold?1:r,gold?.78f:g,gold?.28f:b);
        if(selected) {s.setColor(r,g,b,.16f);s.rect(x+3,y+3,614,70);}
        s.setColor(r,g,b,1);s.rect(x+2,y+2,4,72);
        s.setColor(.08f,.13f,.19f,1);s.rect(x+15,y+14,48,48);
        if(selected) {s.setColor(1,.88f,.5f,1);s.triangle(x+602,y+38,x+593,y+44,x+593,y+32);}
    }
    public static void skill(ShapeRenderer s,SkillType type,float x,float y,float size) {
        if(type==SkillType.DASH) {
            for(int i=0;i<2;i++) {float a=x-size*.65f+i*size*.85f;
                s.rectLine(a,y-size*.55f,a+size*.55f,y,3);s.rectLine(a+size*.55f,y,a,y+size*.55f,3);}
            s.rect(x-size,y-1,size*.45f,2);
        } else {
            s.triangle(x,y+size*.75f,x-size*.4f,y,x+size*.4f,y);
            s.triangle(x,y-size*.75f,x-size*.4f,y,x+size*.4f,y);
            s.rectLine(x-size*.65f,y-size*.55f,x-size,y,2);s.rectLine(x-size,y,x-size*.65f,y+size*.55f,2);
            s.rectLine(x+size*.65f,y-size*.55f,x+size,y,2);s.rectLine(x+size,y,x+size*.65f,y+size*.55f,2);
        }
    }
    public static void classSkill(ShapeRenderer s,com.example.monstersurvivor.skill.ClassSkillId type,float x,float y,float size) {
        switch(type) {
            case WARRIOR_WHIRLWIND -> {
                s.arc(x,y,size*.62f,25,300,18);s.arc(x,y,size*.38f,195,300,18);
                s.triangle(x+size*.62f,y+size*.24f,x+size*.45f,y+size*.34f,x+size*.55f,y+size*.48f);
            }
            case WARRIOR_SWORD_WAVE -> {
                s.rectLine(x-size*.72f,y,x+size*.5f,y,Math.max(2,size*.16f));
                s.triangle(x+size*.8f,y,x+size*.32f,y+size*.38f,x+size*.32f,y-size*.38f);
            }
            case WARRIOR_PARRY -> {
                s.rectLine(x-size*.5f,y+size*.55f,x+size*.5f,y+size*.55f,Math.max(2,size*.13f));
                s.rectLine(x-size*.5f,y+size*.55f,x-size*.35f,y-size*.3f,Math.max(2,size*.13f));
                s.rectLine(x+size*.5f,y+size*.55f,x+size*.35f,y-size*.3f,Math.max(2,size*.13f));
                s.rectLine(x-size*.35f,y-size*.3f,x,y-size*.62f,Math.max(2,size*.13f));
                s.rectLine(x+size*.35f,y-size*.3f,x,y-size*.62f,Math.max(2,size*.13f));
            }
            case WARRIOR_WAR_CRY -> {
                s.arc(x,y,size*.45f,205,130,14);s.arc(x,y,size*.72f,215,110,14);
                s.rectLine(x-size*.2f,y,x+size*.2f,y,Math.max(2,size*.13f));
                s.rectLine(x,y-size*.2f,x,y+size*.2f,Math.max(2,size*.13f));
            }
            case ARCHER_MARK -> {
                s.circle(x,y,size*.58f,12);s.circle(x,y,size*.18f,8);
                s.rectLine(x-size*.9f,y,x-size*.42f,y,Math.max(2,size*.12f));
                s.rectLine(x+size*.42f,y,x+size*.9f,y,Math.max(2,size*.12f));
                s.rectLine(x,y-size*.9f,x,y-size*.42f,Math.max(2,size*.12f));
                s.rectLine(x,y+size*.42f,x,y+size*.9f,Math.max(2,size*.12f));
            }
            case ARCHER_FOCUS -> {
                s.rectLine(x-size*.58f,y-size*.65f,x-size*.12f,y,Math.max(2,size*.12f));
                s.rectLine(x-size*.12f,y,x-size*.58f,y+size*.65f,Math.max(2,size*.12f));
                s.rectLine(x-size*.58f,y-size*.65f,x-size*.58f,y+size*.65f,Math.max(2,size*.08f));
                s.rectLine(x-size*.25f,y,x+size*.65f,y,Math.max(2,size*.1f));
                s.triangle(x+size*.92f,y,x+size*.52f,y+size*.24f,x+size*.52f,y-size*.24f);
            }
            case ARCHER_RAIN -> {
                for(int i=0;i<3;i++) {
                    float offset=(i-1)*size*.42f;
                    s.rectLine(x-size*.52f+offset,y+size*.48f,x+size*.26f+offset,y-size*.3f,Math.max(2,size*.1f));
                    s.triangle(x+size*.32f+offset,y-size*.38f,x+size*.12f+offset,y-size*.18f,x+size*.48f+offset,y-size*.12f);
                }
            }
            case ARCHER_POISON_ARROW -> {
                s.rectLine(x-size*.7f,y-size*.6f,x+size*.55f,y+size*.5f,Math.max(2,size*.13f));
                s.triangle(x+size*.82f,y+size*.75f,x+size*.45f,y+size*.68f,x+size*.68f,y+size*.4f);
                s.circle(x-size*.35f,y+size*.48f,size*.11f,8);s.circle(x-size*.05f,y+size*.68f,size*.09f,8);
                s.circle(x-size*.5f,y+size*.18f,size*.08f,8);
            }
            case MAGE_ICE_SHARD -> {
                s.triangle(x,y+size*.9f,x-size*.34f,y+size*.12f,x+size*.34f,y+size*.12f);
                s.triangle(x,y-size*.9f,x-size*.34f,y-size*.12f,x+size*.34f,y-size*.12f);
                s.rectLine(x-size*.34f,y+size*.12f,x-size*.34f,y-size*.12f,Math.max(2,size*.12f));
                s.rectLine(x+size*.34f,y+size*.12f,x+size*.34f,y-size*.12f,Math.max(2,size*.12f));
            }
            case MAGE_ARCANE_TRAP -> {
                s.circle(x,y,size*.62f,12);s.circle(x,y,size*.3f,8);
                s.rectLine(x-size*.72f,y,x+size*.72f,y,Math.max(2,size*.1f));
                s.rectLine(x,y-size*.72f,x,y+size*.72f,Math.max(2,size*.1f));
            }
            case MAGE_SHIELD -> {
                s.arc(x,y,size*.68f,20,320,18);s.arc(x,y,size*.42f,35,290,18);
                s.triangle(x+size*.68f,y+size*.24f,x+size*.46f,y+size*.32f,x+size*.57f,y+size*.5f);
            }
            case MAGE_CHANNEL -> {
                s.arc(x,y,size*.62f,45,250,18);s.arc(x,y,size*.38f,205,250,18);
                s.triangle(x+size*.7f,y+size*.36f,x+size*.48f,y+size*.34f,x+size*.6f,y+size*.16f);
                s.circle(x,y,size*.16f,8);
            }
            default -> {s.circle(x,y,size*.5f,12);s.circle(x,y,size*.24f,12);}
        }
    }
    public static void weapon(ShapeRenderer s,WeaponType type,float x,float y,float size) {
        switch(type) {
            case SWORD -> {s.rectLine(x-size*.55f,y-size*.65f,x+size*.5f,y+size*.6f,3);s.triangle(x+size*.5f,y+size*.6f,x+size*.2f,y+size*.55f,x+size*.5f,y+size*.85f);s.rectLine(x-size*.65f,y-size*.15f,x-size*.1f,y-size*.65f,3);}
            case BOW -> {s.rectLine(x-size*.5f,y-size*.75f,x+size*.3f,y,2);s.rectLine(x+size*.3f,y,x-size*.5f,y+size*.75f,2);s.rectLine(x-size*.5f,y-size*.75f,x-size*.5f,y+size*.75f,1);s.rectLine(x-size*.7f,y,x+size*.8f,y,2);s.triangle(x+size,y,x+size*.5f,y+size*.25f,x+size*.5f,y-size*.25f);}
            case FIREBALL -> {s.triangle(x,y+size,x-size*.65f,y-size*.35f,x+size*.65f,y-size*.35f);s.circle(x,y-size*.25f,size*.6f,10);}
            case LIGHTNING -> {s.triangle(x+size*.2f,y+size,x-size*.65f,y-size*.1f,x+size*.2f,y-size*.1f);s.triangle(x-size*.2f,y-size,x+size*.65f,y+size*.1f,x-size*.2f,y+size*.1f);}
        }
    }
    /** Cache only when state changes. Returns the scale needed to draw this fitted layout. */
    public static float fit(BitmapFont font,GlyphLayout layout,CharSequence text,float scale,float width) {
        float sx=font.getData().scaleX,sy=font.getData().scaleY;font.getData().setScale(scale);layout.setText(font,text);
        if(layout.width>width) {scale*=width/layout.width;font.getData().setScale(scale);layout.setText(font,text);}
        font.getData().setScale(sx,sy);return scale;
    }
    public static void wrap(BitmapFont font,GlyphLayout layout,CharSequence text,float scale,float width) {
        float sx=font.getData().scaleX,sy=font.getData().scaleY;font.getData().setScale(scale);
        layout.setText(font,text,Color.WHITE,width,Align.left,true);font.getData().setScale(sx,sy);
    }
    public static void draw(SpriteBatch batch,BitmapFont font,GlyphLayout layout,float scale,float x,float y) {
        float sx=font.getData().scaleX,sy=font.getData().scaleY;font.getData().setScale(scale);font.draw(batch,layout,x,y);font.getData().setScale(sx,sy);
    }
}
