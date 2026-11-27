package com.example.monstersurvivor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.weapon.*;
import static com.example.monstersurvivor.localization.GameStrings.Key.EMPTY_SLOT;
import static com.example.monstersurvivor.localization.GameStrings.Key.EVOLUTION_ICON;

/** Read-only shared passive-skill slots. Card geometry is reused by the Treasure Chest modal. */
public final class WeaponHud {
    private final GlyphLayout[] slots=layouts(),levels=layouts();
    private GameSession previous;private long revision=-1;
    private static GlyphLayout[] layouts(){return new GlyphLayout[]{new GlyphLayout(),new GlyphLayout(),new GlyphLayout()};}
    public static float cardY(float height,int index){return height/2+54-index*88;}
    public static int offerAt(float x,float y,float width,float height,int count){
        if(Math.abs(x-width/2)>310)return -1;
        for(int i=0;i<count;i++)if(y>=cardY(height,i) && y<=cardY(height,i)+76)return i;
        return -1;
    }
    public static boolean homeAt(float x,float y,float width,float height){return x>=width/2+200 && x<=width/2+300 && y>=height/2-184 && y<=height/2-148;}
    public void prepare(BitmapFont font,GameSession s){
        if(previous!=s){previous=s;revision=-1;}
        long passiveRevision=((long)s.weapons.loadout.revision()<<32) ^ (s.classSkills.revision()&0xffffffffL);
        if(revision!=passiveRevision){
            revision=passiveRevision;
            for(int i=0;i<3;i++){
                var w=weaponAt(s,i);var id=classSkillAt(s,i);boolean classEvolved=id!=null && s.classSkills.evolved(id);
                String name=w!=null?(w.evolved()?s.config.strings.evolvedName(w.type):s.config.strings.weaponName(w.type))
                    :id!=null?(classEvolved?s.config.strings.evolvedClassSkillName(id):s.config.strings.get(id.nameKey())):s.config.strings.get(EMPTY_SLOT);
                HudStyle.wrap(font,slots[i],name,.75f,w==null && id==null?124:94);
                if(w!=null && w.evolved() || classEvolved)font.setColor(.04f,.06f,.08f,1);
                HudStyle.fit(font,levels[i],w!=null?(w.evolved()?s.config.strings.get(EVOLUTION_ICON):"Lv"+w.level())
                    :id!=null?(classEvolved?s.config.strings.get(EVOLUTION_ICON):"Lv"+s.classSkills.level(id)):"",.65f,32);
                font.setColor(Color.WHITE);
            }
        }
    }
    public void slotShapes(ShapeRenderer shapes,GameSession s,float width){
        slotShapes(shapes,s,width,0);
    }
    public void slotShapes(ShapeRenderer shapes,GameSession s,float width,float bottom){
        for(int i=0;i<3;i++){
            float x=width/2-215+i*146;var w=weaponAt(s,i);var id=classSkillAt(s,i);
            boolean classEvolved=id!=null && s.classSkills.evolved(id);
            float r=w!=null?(w.evolved()?1:w.type.r):id!=null?(classEvolved?1:.95f):.2f;
            float g=w!=null?(w.evolved()?.78f:w.type.g):id!=null?(classEvolved?.78f:.68f):.3f;
            float b=w!=null?(w.evolved()?.28f:w.type.b):id!=null?(classEvolved?.28f:.3f):.36f;
            HudStyle.panel(shapes,x,32+bottom,138,54,r,g,b);
            if(w==null && id==null)continue;
            if(w!=null){shapes.setColor(w.type.r,w.type.g,w.type.b,1);HudStyle.weapon(shapes,w.type,x+19,62+bottom,10);}
            else {shapes.setColor(.95f,.76f,.42f,1);HudStyle.classSkill(shapes,id,x+19,62+bottom,10);}
            if(w!=null && w.evolved() || classEvolved){shapes.setColor(1,.8f,.25f,1);shapes.rect(x+106,40+bottom,27,12);}
            float total=w!=null?(w.type==WeaponType.SWORD?s.weapons.swordCooldown():w.tuning().cooldown)
                :com.example.monstersurvivor.skill.ClassSkillTuning.cooldown(id,s.classSkills.level(id));
            float remaining=w!=null?(w.type==WeaponType.SWORD && !w.evolved()?s.player.cooldown():w.cooldownRemaining)
                :s.classSkills.cooldownRemaining(id);
            shapes.setColor(.1f,.17f,.22f,1);shapes.rect(x+6,35+bottom,126,3);
            shapes.setColor(r,g,b,1);shapes.rect(x+6,35+bottom,126*Math.max(0,Math.min(1,1-remaining/total)),3);
        }
    }
    public void slotText(SpriteBatch batch,BitmapFont font,float width){
        slotText(batch,font,width,0);
    }
    public void slotText(SpriteBatch batch,BitmapFont font,float width,float bottom){
        for(int i=0;i<3;i++){
            float x=width/2-215+i*146;var w=weaponAt(previous,i);var id=classSkillAt(previous,i);
            HudStyle.draw(batch,font,slots[i],.75f,x+(w==null && id==null?7:36),76+bottom);
            HudStyle.draw(batch,font,levels[i],.65f,x+119-levels[i].width/2,50+bottom);
        }
        font.setColor(Color.WHITE);
    }
    private static WeaponInstance weaponAt(GameSession session,int slot) {
        return slot<session.weapons.loadout.size()?session.weapons.loadout.slot(slot):null;
    }
    private static com.example.monstersurvivor.skill.ClassSkillId classSkillAt(GameSession session,int slot) {
        int classSlot=slot-session.weapons.loadout.size();
        return classSlot>=0 && classSlot<session.classSkills.size()?session.classSkills.slot(classSlot):null;
    }
}
