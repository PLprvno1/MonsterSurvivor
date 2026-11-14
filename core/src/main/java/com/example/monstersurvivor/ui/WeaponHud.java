package com.example.monstersurvivor.ui;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.weapon.*;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Minimal replaceable libGDX UI. No native dependencies, callbacks or weapon mutation. */
public final class WeaponHud {
    private final GlyphLayout[] slots={new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};
    private final GlyphLayout[] names={new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};
    private final GlyphLayout[] descriptions={new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};
    private final GlyphLayout[] icons={new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};
    private final GlyphLayout title=new GlyphLayout(),hint=new GlyphLayout(),home=new GlyphLayout();
    private GameSession previous;
    private int revision=-1;
    private long token=-1;
    private boolean initialized;
    public static float cardY(float height,int index) {return height/2+54-index*88;}
    public static int offerAt(float x,float y,float width,float height,int count) {
        if(Math.abs(x-width/2)>310)return -1;
        for(int i=0;i<count;i++)if(y>=cardY(height,i) && y<=cardY(height,i)+76)return i;return -1;
    }
    public static boolean homeAt(float x,float y,float width,float height) {
        return x>=width/2+200 && x<=width/2+300 && y>=height/2-184 && y<=height/2-148;
    }
    public void prepare(BitmapFont font,GameSession session) {
        if(!initialized) {
            initialized=true;title.setText(font,session.config.strings.get(CHOOSE_WEAPON));hint.setText(font,session.config.strings.get(WEAPON_HINT));home.setText(font,session.config.strings.get(HOME));
        }
        if(previous!=session) {previous=session;revision=-1;token=-1;}
        if(revision!=session.weapons.loadout.revision()) {
            revision=session.weapons.loadout.revision();
            for(int i=0;i<3;i++) {var weapon=session.weapons.loadout.slot(i);
                slots[i].setText(font,weapon==null?session.config.strings.get(EMPTY_SLOT):session.config.strings.format(WEAPON_LEVEL,session.config.strings.weaponShort(weapon.type),weapon.level()));
            }
        }
        if(session.choosingWeapon() && token!=session.selection.token()) {
            token=session.selection.token();
            for(int i=0;i<session.selection.size();i++) {
                var offer=session.selection.get(i);names[i].setText(font,offer.title);descriptions[i].setText(font,offer.description);
                font.setColor(offer.r,offer.g,offer.b,1);icons[i].setText(font,offer.icon);font.setColor(1,1,1,1);
            }
        }
    }
    public void slotShapes(ShapeRenderer shapes,GameSession session,float width) {
        for(int i=0;i<3;i++) {
            float x=width/2-156+i*108;var weapon=session.weapons.loadout.slot(i);
            shapes.setColor(.035f,.07f,.075f,.9f);shapes.rect(x,40,100,40);
            if(weapon!=null) {
                shapes.setColor(weapon.type.r,weapon.type.g,weapon.type.b,1);
                float total=weapon.type==WeaponType.SWORD?session.weapons.swordCooldown():weapon.tuning().cooldown;
                float remaining=weapon.type==WeaponType.SWORD?session.player.cooldown():weapon.cooldownRemaining;
                shapes.rect(x+5,44,90*(1-Math.min(1,remaining/total)),3);
            }
        }
    }
    public void slotText(SpriteBatch batch,BitmapFont font,float width) {
        for(int i=0;i<3;i++)font.draw(batch,slots[i],width/2-156+i*108+50-slots[i].width/2,68);
    }
    public void modal(SpriteBatch batch,ShapeRenderer shapes,BitmapFont font,GameSession session,GameInput input,float width,float height) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0,0,0,.76f);shapes.rect(0,0,width,height);
        shapes.setColor(.055f,.10f,.115f,.98f);shapes.rect(width/2-330,height/2-200,660,400);
        for(int i=0;i<session.selection.size();i++) {
            var offer=session.selection.get(i);float x=width/2-310,y=cardY(height,i);
            if(i==input.selectedOffer())shapes.setColor(.22f,.36f,.32f,1);else shapes.setColor(.1f,.18f,.20f,1);
            shapes.rect(x,y,620,76);shapes.setColor(offer.r,offer.g,offer.b,1);shapes.rect(x,y,4,76);
            shapes.setColor(.035f,.07f,.075f,1);shapes.rect(x+16,y+17,56,42);
        }
        shapes.setColor(.18f,.28f,.25f,1);shapes.rect(width/2+200,height/2-184,100,36);shapes.end();
        batch.begin();font.draw(batch,title,width/2-title.width/2,height/2+166);
        for(int i=0;i<session.selection.size();i++) {
            float x=width/2-310,y=cardY(height,i);
            font.draw(batch,icons[i],x+44-icons[i].width/2,y+43);
            font.draw(batch,names[i],x+88,y+55);font.draw(batch,descriptions[i],x+88,y+28);
        }
        font.draw(batch,hint,width/2-310,height/2-163);font.draw(batch,home,width/2+250-home.width/2,height/2-160);batch.end();
    }
}
