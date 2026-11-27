package com.example.monstersurvivor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.skill.SkillType;
import com.example.monstersurvivor.skill.TreasureChestOffer;
import com.example.monstersurvivor.skill.TreasureChestSelection;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Unified read-only Treasure Chest modal for passive, active and fallback rewards. */
public final class SkillRewardHud {
    private final GlyphLayout title=new GlyphLayout(),branch=new GlyphLayout(),hint=new GlyphLayout(),home=new GlyphLayout();
    private final GlyphLayout[] names=layouts(),benefits=layouts(),details=layouts(),summaries=layouts();
    private final float[] nameScale=new float[3],benefitScale=new float[3],detailScale=new float[3],summaryScale=new float[3];
    private long token;
    private static GlyphLayout[] layouts(){return new GlyphLayout[]{new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};}

    public void prepare(BitmapFont font,GameSession session) {
        if(!session.choosingChest() || token==session.chestSelection.token())return;
        token=session.chestSelection.token();var strings=session.config.strings;
        HudStyle.fit(font,title,strings.get(TREASURE_CHEST),1,600);
        var branchKey=switch(session.chestSelection.branch()) {
            case PASSIVE -> CHEST_PASSIVE_BRANCH;
            case ACTIVE -> CHEST_ACTIVE_BRANCH;
            case FALLBACK -> CHEST_FALLBACK_BRANCH;
        };
        HudStyle.fit(font,branch,strings.get(branchKey),.75f,480);
        HudStyle.fit(font,home,strings.get(HOME),.85f,90);
        HudStyle.fit(font,hint,strings.get(CHEST_REWARD_HINT),.8f,480);
        for(int i=0;i<session.chestSelection.size();i++) {
            var offer=session.chestSelection.get(i);
            nameScale[i]=HudStyle.fit(font,names[i],offer.title,.95f,500);
            summaryScale[i]=HudStyle.fit(font,summaries[i],offer.summary,.65f,500);
            benefitScale[i]=HudStyle.fit(font,benefits[i],offer.benefit,.75f,500);
            detailScale[i]=HudStyle.fit(font,details[i],offer.detail,.75f,500);
        }
    }

    public void modal(SpriteBatch batch,ShapeRenderer shapes,BitmapFont font,GameSession session,GameInput input,float width,float height) {
        HudStyle.beginOverlay(shapes);shapes.setColor(.01f,.02f,.035f,.78f);shapes.rect(0,0,width,height);
        HudStyle.panel(shapes,width/2-330,height/2-200,660,400,.86f,.68f,.3f);
        for(int i=0;i<session.chestSelection.size();i++) {
            float x=width/2-310,y=WeaponHud.cardY(height,i);var offer=session.chestSelection.get(i);
            HudStyle.card(shapes,x,y,input.selectedOffer()==i,offer.evolvedOffer(),offer.r,offer.g,offer.b);
            shapes.setColor(offer.r,offer.g,offer.b,1);
            switch(offer.kind) {
                case PASSIVE_WEAPON -> HudStyle.weapon(shapes,offer.weapon.weapon,x+39,y+38,17);
                case EVOLUTION -> {
                    if(offer.weapon!=null)HudStyle.weapon(shapes,offer.weapon.weapon,x+39,y+38,17);
                    else HudStyle.classSkill(shapes,offer.skill.classSkill,x+39,y+38,16);
                }
                case PASSIVE_CLASS -> HudStyle.classSkill(shapes,offer.skill.classSkill,x+39,y+38,16);
                case ACTIVE -> HudStyle.skill(shapes,offer.skill.type,x+39,y+38,16);
                case STAT -> {shapes.circle(x+39,y+38,13,16);shapes.rect(x+32,y+36,14,4);}
            }
        }
        HudStyle.panel(shapes,width/2+200,height/2-184,100,36,.45f,.64f,.68f);shapes.end();
        batch.begin();HudStyle.draw(batch,font,title,1,width/2-title.width/2,height/2+177);
        HudStyle.draw(batch,font,branch,.75f,width/2-branch.width/2,height/2+145);
        for(int i=0;i<session.chestSelection.size();i++) {
            float x=width/2-232,y=WeaponHud.cardY(height,i);var offer=session.chestSelection.get(i);
            HudStyle.draw(batch,font,names[i],nameScale[i],x,y+66);
            if(summaries[i].width>0)HudStyle.draw(batch,font,summaries[i],summaryScale[i],x,y+49);
            if(benefits[i].width>0)HudStyle.draw(batch,font,benefits[i],benefitScale[i],x,y+34);
            if(details[i].width>0)HudStyle.draw(batch,font,details[i],detailScale[i],x,y+19);
        }
        HudStyle.draw(batch,font,hint,.8f,width/2-310,height/2-163);
        HudStyle.draw(batch,font,home,.85f,width/2+250-home.width/2,height/2-160);
        font.setColor(Color.WHITE);batch.end();
    }
}
