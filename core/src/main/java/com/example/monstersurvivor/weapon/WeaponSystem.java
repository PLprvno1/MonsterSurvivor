package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.world.CollisionGrid;
import com.badlogic.gdx.math.MathUtils;

/** Independent real-time weapon clocks. Sword reuses the established melee combat/action state. */
public final class WeaponSystem {
    public final WeaponLoadout loadout;
    public final ProjectileSystem projectiles;
    private final SwordCombat sword;
    private final GameConfig config;
    private final int[] visited=new int[5];
    private final Enemy[] roots=new Enemy[2];
    public int lightningHits,lightningCasts,lightningInitialBolts;
    public com.example.monstersurvivor.debug.FrameMetrics metrics;
    public WeaponSystem(GameConfig config,WeaponLoadout loadout,SwordCombat sword) {
        this.config=config;this.loadout=loadout;this.sword=sword;projectiles=new ProjectileSystem(config.weapons);
    }
    public void update(float dt,Player player,EnemySystem enemies,CollisionGrid grid) {
        if(!player.alive()) {projectiles.clear();return;}
        long start=metrics==null?0:metrics.start(),collisionBefore=metrics==null?0:metrics.value(com.example.monstersurvivor.debug.FrameMetrics.Part.COLLISION);
        projectiles.update(dt,enemies,grid);
        if(start!=0)metrics.add(com.example.monstersurvivor.debug.FrameMetrics.Part.PROJECTILES,System.nanoTime()-start-(metrics.value(com.example.monstersurvivor.debug.FrameMetrics.Part.COLLISION)-collisionBefore));
        sword.update(player,enemies,grid);
        WeaponInstance melee=loadout.get(WeaponType.SWORD);melee.cooldownRemaining=player.cooldown();melee.activations=sword.swings;
        for(int i=0;i<loadout.size();i++) {
            WeaponInstance weapon=loadout.slot(i);if(weapon.type==WeaponType.SWORD)continue;
            weapon.cooldownRemaining=Math.max(0,weapon.cooldownRemaining-dt);weapon.retryRemaining=Math.max(0,weapon.retryRemaining-dt);
            if(weapon.cooldownRemaining>0 || weapon.retryRemaining>0)continue;
            var level=weapon.tuning();float clearance=weapon.type==WeaponType.FIREBALL?2:weapon.type==WeaponType.BOW?.75f:.5f;
            Enemy target=WeaponTargeting.nearest(player.x,player.y,level.range,enemies,grid,visited,0,clearance);
            boolean fired=false;
            if(target!=null) {
                int damage=level.damage(player.stats.damage());
                if(weapon.type==WeaponType.LIGHTNING) {chain(player,target,weapon,enemies,grid,damage);fired=true;}
                else if(weapon.type==WeaponType.FIREBALL)fired=volley(player,target,weapon,enemies,grid,damage);
                else fired=projectiles.shoot(weapon.type,player.x,player.y,target.x,target.y,damage,level);
            }
            if(fired) {weapon.cooldownRemaining=level.cooldown;weapon.activations++;}
            else weapon.retryRemaining=config.weapons.retrySeconds;
        }
    }
    private void chain(Player player,Enemy target,WeaponInstance weapon,EnemySystem enemies,CollisionGrid grid,int damage) {
        lightningCasts++;var level=weapon.tuning();int count=0,rootCount=0;
        // Reserve both primaries before chaining. A chain cannot steal the other initial bolt.
        while(target!=null && rootCount<level.initialBolts) {
            roots[rootCount++]=target;visited[count++]=target.id;
            target=WeaponTargeting.nearest(player.x,player.y,level.range,enemies,grid,visited,count,.5f);
        }
        for(int i=0;i<rootCount;i++) {
            target=roots[i];projectiles.effect(ProjectileSystem.EffectType.LIGHTNING,player.x,player.y,target.x,target.y,0,level);
            target.damage(damage);lightningHits++;lightningInitialBolts++;
        }
        // One shared activation budget and visited set, including both roots.
        for(int i=0;i<rootCount && count<level.targets;i++) {
            float x=roots[i].x,y=roots[i].y;
            while(count<level.targets && (target=WeaponTargeting.nearest(x,y,level.chainRange,enemies,grid,visited,count,.5f))!=null) {
                visited[count++]=target.id;projectiles.effect(ProjectileSystem.EffectType.LIGHTNING,x,y,target.x,target.y,0,level);
                x=target.x;y=target.y;target.damage(damage);lightningHits++;
            }
        }
        roots[0]=roots[1]=null;
    }
    private boolean volley(Player player,Enemy first,WeaponInstance weapon,EnemySystem enemies,CollisionGrid grid,int damage) {
        var level=weapon.tuning();if(projectiles.available()<level.projectileCount)return false;
        int selected=0;boolean fired=false;
        for(int i=0;i<level.projectileCount;i++) {
            Enemy target=i==0?first:WeaponTargeting.nearest(player.x,player.y,level.range,enemies,grid,visited,selected,2);
            float tx,ty;
            if(target!=null) {visited[selected++]=target.id;tx=target.x;ty=target.y;}
            else {tx=first.x;ty=first.y;}
            // Distinct lanes even with one enemy or collinear targets; collision remains swept.
            float angle=(i-(level.projectileCount-1)*.5f)*level.fanDegrees*MathUtils.degreesToRadians;
            float dx=tx-player.x,dy=ty-player.y,c=MathUtils.cos(angle),s=MathUtils.sin(angle);
            fired|=projectiles.shoot(weapon.type,player.x,player.y,player.x+dx*c-dy*s,player.y+dx*s+dy*c,damage,level);
        }
        return fired;
    }
    public float swordCooldown() {return config.swordCooldown*loadout.get(WeaponType.SWORD).tuning().cooldown;}
    public void clearTransient() {projectiles.clear();}
}
