package com.example.monstersurvivor.combat;

import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.world.CollisionGrid;

/** Automatic targeting and one frontal hit event per completed swing windup. */
public final class SwordCombat {
    public com.example.monstersurvivor.audio.AudioSink audio=com.example.monstersurvivor.audio.AudioSink.NONE;
    private final GameConfig config;
    private final com.example.monstersurvivor.weapon.WeaponInstance weapon;
    private com.example.monstersurvivor.skill.ClassSkillSystem classSkills;
    public int kills,swings,hitEvents;
    public int canceledSwings;
    private Enemy target;
    public SwordCombat(GameConfig config) {this(config,null);}
    public SwordCombat(GameConfig config,com.example.monstersurvivor.weapon.WeaponInstance weapon) {this.config=config;this.weapon=weapon;}
    public float range() {return config.swordRange+(weapon==null?0:weapon.tuning().range-48);}
    private float detection() {return config.swordDetectionRange+range()-config.swordRange;}
    private float cycle() {return config.swordCooldown*(weapon==null?1:weapon.tuning().cooldown);}
    public Enemy target() { return target; }
    public void classSkills(com.example.monstersurvivor.skill.ClassSkillSystem skills) {classSkills=skills;}
    private boolean valid(Player p,Enemy e,CollisionGrid grid,float range) {
        if(e==null || !e.alive())return false;
        float dx=e.x-p.x,dy=e.y-p.y;
        return dx*dx+dy*dy<=range*range && grid.clearLine(p.x,p.y,e.x,e.y,.5f,.5f,true);
    }
    private Enemy nearest(Player p,EnemySystem enemies,CollisionGrid grid,float range) {
        Enemy found=null;float best=range*range;
        for(int i=0;i<enemies.size();i++) {
            Enemy e=enemies.get(i);if(!e.alive())continue;
            float dx=e.x-p.x,dy=e.y-p.y,d2=dx*dx+dy*dy;
            if(d2<=best && (found==null || d2<best) && grid.clearLine(p.x,p.y,e.x,e.y,.5f,.5f,true)) {found=e;best=d2;}
        }
        return found;
    }
    public void update(Player p,EnemySystem enemies,CollisionGrid grid) {
        if(weapon!=null && weapon.evolved()) {target=null;p.cancelAttack();p.takeSwingHit();return;}
        if(!p.alive()) {target=null;return;}
        // Keep a valid swing target stable. Replace it immediately if it dies, is occluded,
        // or leaves the actual blade range; detection alone never grants a hit.
        if(p.windingUp()) {
            if(!valid(p,target,grid,range()))target=nearest(p,enemies,grid,range());
            if(target==null) {p.cancelAttack();canceledSwings++;return;}
            p.aimAttack(target.x-p.x,target.y-p.y);
        } else if(!valid(p,target,grid,detection()))target=null;
        if(p.takeSwingHit()) {
            hitEvents++;
            boolean landed=false;
            for(int i=0;i<enemies.size();i++) {
                Enemy e=enemies.get(i);if(!e.alive())continue;
                float dx=e.x-p.x,dy=e.y-p.y,d2=dx*dx+dy*dy;
                if(d2>range()*range())continue;
                // Broad frontal sector, including contact overlap; walls block the blade.
                if(d2>4 && dx*p.swingX()+dy*p.swingY()<.25f*(float)Math.sqrt(d2))continue;
                if(!grid.clearLine(p.x,p.y,e.x,e.y,.5f,.5f,true))continue;
                int before=e.hp();
                int damage=weapon==null?p.stats.damage():weapon.tuning().damage(p.stats.damage());
                if(classSkills!=null)damage=(int)Math.min(Integer.MAX_VALUE,(long)damage+classSkills.warCryDirectBonus());
                if(e.damage(damage))kills++;
                landed|=e.hp()<before;
            }
            if(landed)audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_SWORD_HIT);
        }
        if(!p.canAttack())return;
        target=nearest(p,enemies,grid,detection());
        if(valid(p,target,grid,range()) && p.startAttack(target.x-p.x,target.y-p.y,cycle())) {
            swings++;audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_SWORD_SWING);
        }
    }
    public void contact(Player p,EnemySystem enemies) {
        if(!p.alive() || p.immunity()>0)return;
        for(int i=0;i<enemies.size();i++) {
            Enemy e=enemies.get(i);if(!e.alive() || e.acting() || e.aiLocked)continue;
            float dx=p.x-e.x,dy=p.y-e.y;
            if(dx*dx+dy*dy<=e.definition.contactRadius()*e.definition.contactRadius()
                && enemies.navigation.collision.clearLine(e.x,e.y,p.x,p.y,.5f,.5f,e.definition.flying)) {
                e.attack(dx,dy);
                if(p.damageDirect(e.stats.damage,e.x,e.y))return; // Shared player immunity, even in a crowd.
            }
        }
    }
}
