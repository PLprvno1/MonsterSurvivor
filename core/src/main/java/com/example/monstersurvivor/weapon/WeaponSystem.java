package com.example.monstersurvivor.weapon;

import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.world.CollisionGrid;
import com.badlogic.gdx.math.MathUtils;
import com.example.monstersurvivor.config.EvolutionConfig;
import com.example.monstersurvivor.skill.ClassSkillSystem;

/** Independent real-time weapon clocks. Sword reuses the established melee combat/action state. */
public final class WeaponSystem {
    public com.example.monstersurvivor.audio.AudioSink audio=com.example.monstersurvivor.audio.AudioSink.NONE;
    public final WeaponLoadout loadout;
    public final ProjectileSystem projectiles;
    private final SwordCombat sword;
    private final GameConfig config;
    private ClassSkillSystem classSkills;
    private final int[] visited=new int[8];
    private final Enemy[] roots=new Enemy[3];
    private final int[] orbitIds=new int[EvolutionConfig.ORBIT_TRACKED];
    private final float[] orbitNext=new float[EvolutionConfig.ORBIT_TRACKED];
    private float orbitTime,orbitCooldownTime,orbitAngle,orbitX,orbitY;
    private boolean orbitStarted;
    private float nextOrbitSound;
    private float infernoCooldownBoostTime,infernoCooldownBoostLockout;
    private int infernoFireballCasts;
    public int infernoCooldownBoostActivations;
    public int orbitHits;
    public float orbitAngle() {return orbitAngle;}
    public float orbitTime() {return orbitTime;}
    public float orbitX() {return orbitX;} public float orbitY() {return orbitY;}
    public int lightningHits,lightningCasts,lightningInitialBolts;
    public com.example.monstersurvivor.debug.FrameMetrics metrics;
    public WeaponSystem(GameConfig config,WeaponLoadout loadout,SwordCombat sword) {
        this.config=config;this.loadout=loadout;this.sword=sword;projectiles=new ProjectileSystem(config.weapons);
    }
    public void classSkills(ClassSkillSystem skills) {classSkills=skills;sword.classSkills(skills);projectiles.classSkills(skills);}
    public void update(float dt,Player player,EnemySystem enemies,CollisionGrid grid) {
        update(dt,player,enemies,grid,dt);
    }
    /** Cooldown potion advances passive activation clocks while projectile and evolution clocks keep gameplay dt. */
    public void update(float dt,Player player,EnemySystem enemies,CollisionGrid grid,float cooldownDelta) {
        if(!player.alive()) {projectiles.clear();return;}
        float infernoBoostStep=Math.min(dt,infernoCooldownBoostTime);
        infernoCooldownBoostTime=Math.max(0,infernoCooldownBoostTime-dt);
        infernoCooldownBoostLockout=Math.max(0,infernoCooldownBoostLockout-dt);
        long start=metrics==null?0:metrics.start(),collisionBefore=metrics==null?0:metrics.value(com.example.monstersurvivor.debug.FrameMetrics.Part.COLLISION);
        projectiles.update(dt,enemies,grid);
        if(start!=0)metrics.add(com.example.monstersurvivor.debug.FrameMetrics.Part.PROJECTILES,System.nanoTime()-start-(metrics.value(com.example.monstersurvivor.debug.FrameMetrics.Part.COLLISION)-collisionBefore));
        WeaponInstance melee=loadout.get(WeaponType.SWORD);
        if(melee!=null) {
            sword.update(player,enemies,grid);
            if(melee.evolved())orbit(dt,cooldownDelta,player,enemies,grid,melee);
            else {melee.cooldownRemaining=player.cooldown();melee.activations=sword.swings;orbitStarted=false;}
        } else {
            player.cancelAttack();player.takeSwingHit();orbitStarted=false;
        }
        for(int i=0;i<loadout.size();i++) {
            WeaponInstance weapon=loadout.slot(i);if(weapon.type==WeaponType.SWORD)continue;
            float cooldownStep=Math.max(dt,cooldownDelta);
            if(weapon.type==WeaponType.FIREBALL && weapon.evolved())cooldownStep+=infernoBoostStep*.10f;
            cooldownStep=Math.min(cooldownStep,dt*com.example.monstersurvivor.consumable.PotionType.MAX_COMBINED_COOLDOWN_MULTIPLIER);
            weapon.cooldownRemaining=Math.max(0,weapon.cooldownRemaining-cooldownStep);weapon.retryRemaining=Math.max(0,weapon.retryRemaining-dt);
            if(weapon.cooldownRemaining>0 || weapon.retryRemaining>0)continue;
            var level=weapon.tuning();float clearance=weapon.type==WeaponType.FIREBALL?2:weapon.type==WeaponType.BOW?.75f:.5f;
            Enemy target=WeaponTargeting.nearest(player.x,player.y,level.range,enemies,grid,visited,0,clearance);
            boolean fired=false;
            if(target!=null) {
                int damage=level.damage(player.stats.damage());
                if(weapon.type==WeaponType.LIGHTNING) {chain(player,target,weapon,enemies,grid,damage);fired=true;}
                else if(weapon.type==WeaponType.FIREBALL)fired=weapon.evolved()?projectiles.meteor(player.x,player.y,target.x,target.y,damage,EvolutionConfig.burnDamage(player.stats.damage()),level):volley(player,target,weapon,enemies,grid,damage);
                else fired=weapon.evolved()?projectiles.storm(player.x,player.y,target.x,target.y,damage,level):projectiles.shoot(weapon.type,player.x,player.y,target.x,target.y,damage,level);
            }
            if(fired) {
                if(melee==null)player.showRangedAttack(target.x-player.x,target.y-player.y);
                float cooldown=level.cooldown;
                if(weapon.type==WeaponType.FIREBALL && classSkills!=null)cooldown*=classSkills.fireballCooldownMultiplier();
                weapon.cooldownRemaining=cooldown;weapon.activations++;
                // Former Arcane Channel tuning now belongs only to Inferno Meteor: every third
                // successful evolved Fireball grants 10% faster Fireball cooldown for 3s, at most
                // once per 15s. No WeaponConfig values or other skill clocks are modified.
                if(weapon.type==WeaponType.FIREBALL && weapon.evolved() && ++infernoFireballCasts>=3) {
                    infernoFireballCasts=0;
                    if(infernoCooldownBoostLockout<=0) {
                        infernoCooldownBoostTime=3;infernoCooldownBoostLockout=15;infernoCooldownBoostActivations++;
                    }
                }
                if(weapon.type==WeaponType.BOW)audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_BOW_RELEASE);
                else if(weapon.type==WeaponType.FIREBALL)audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_FIRE_CAST);
            }
            else weapon.retryRemaining=config.weapons.retrySeconds;
        }
    }
    private void chain(Player player,Enemy target,WeaponInstance weapon,EnemySystem enemies,CollisionGrid grid,int damage) {
        audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_LIGHTNING_ACTIVATE);
        boolean landed=false;
        lightningCasts++;var level=weapon.tuning();int count=0,rootCount=0;
        // Reserve both primaries before chaining. A chain cannot steal the other initial bolt.
        while(target!=null && rootCount<level.initialBolts) {
            roots[rootCount++]=target;visited[count++]=target.id;
            target=WeaponTargeting.nearest(player.x,player.y,level.range,enemies,grid,visited,count,.5f);
        }
        for(int i=0;i<rootCount;i++) {
            target=roots[i];projectiles.effect(ProjectileSystem.EffectType.LIGHTNING,player.x,player.y,target.x,target.y,0,level);
            int before=target.hp();target.damage(damage);landed|=target.hp()<before;lightningHits++;lightningInitialBolts++;
        }
        // One shared activation budget and visited set, including both roots.
        for(int i=0;i<rootCount && count<level.targets;i++) {
            float x=roots[i].x,y=roots[i].y;
            int branch=0;
            while(count<level.targets && (!weapon.evolved() || branch<EvolutionConfig.WEB_CHAIN_PER_BRANCH) && (target=WeaponTargeting.nearest(x,y,level.chainRange,enemies,grid,visited,count,.5f))!=null) {
                branch++;
                visited[count++]=target.id;projectiles.effect(ProjectileSystem.EffectType.LIGHTNING,x,y,target.x,target.y,0,level);
                x=target.x;y=target.y;int before=target.hp();target.damage(damage);landed|=target.hp()<before;lightningHits++;
            }
        }
        java.util.Arrays.fill(roots,null);
        if(landed)audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_LIGHTNING_IMPACT);
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
            int directBonus=weapon.type==WeaponType.FIREBALL && i==0 && classSkills!=null?classSkills.fireballDirectBonus():0;
            int directTarget=weapon.type==WeaponType.FIREBALL && i==0?target==null?first.id:target.id:0;
            fired|=projectiles.shoot(weapon.type,player.x,player.y,player.x+dx*c-dy*s,player.y+dx*s+dy*c,damage,level,directTarget,directBonus);
        }
        return fired;
    }
    public float swordCooldown() {
        WeaponInstance sword=loadout.get(WeaponType.SWORD);
        return config.swordCooldown*(sword==null?1:sword.tuning().cooldown);
    }
    private void orbit(float dt,float cooldownDelta,Player p,EnemySystem enemies,CollisionGrid grid,WeaponInstance weapon) {
        if(!orbitStarted) {orbitStarted=true;orbitX=p.x;orbitY=p.y;java.util.Arrays.fill(orbitIds,-1);java.util.Arrays.fill(orbitNext,0);}
        orbitTime+=dt;orbitCooldownTime+=Math.max(dt,cooldownDelta);float turn=dt*EvolutionConfig.ORBIT_SPEED;
        // Bounded angular/translation substeps; render frame rate never decides damage frequency.
        int steps=Math.min(64,Math.max(1,(int)Math.ceil(Math.max(turn/.12f,Math.hypot(p.x-orbitX,p.y-orbitY)/4))));
        boolean landed=false;int damage=weapon.tuning().damage(p.stats.damage());if(classSkills!=null)damage=(int)Math.min(Integer.MAX_VALUE,(long)damage+classSkills.warCryDirectBonus());int frameHits=0;
        for(int n=0;n<enemies.size();n++) {
            if(frameHits>=EvolutionConfig.ORBIT_HITS_PER_STEP)break;
            Enemy e=enemies.get(n);if(!e.alive())continue;
            int slot=-1,free=-1;for(int i=0;i<orbitIds.length;i++) {if(orbitIds[i]==e.id){slot=i;break;}if(free<0 && orbitNext[i]<=orbitCooldownTime)free=i;}
            if(slot>=0 && orbitNext[slot]>orbitCooldownTime)continue;if(slot<0)slot=free;if(slot<0)continue;
            boolean hit=false;
            for(int s=1;s<=steps && !hit;s++) {
                float t=s/(float)steps,cx=orbitX+(p.x-orbitX)*t,cy=orbitY+(p.y-orbitY)*t;
                for(int b=0;b<EvolutionConfig.ORBIT_BLADES;b++) {
                    float angle=orbitAngle+turn*t+b*MathUtils.PI;
                    float bx=cx+MathUtils.cos(angle)*EvolutionConfig.ORBIT_RADIUS,by=cy+MathUtils.sin(angle)*EvolutionConfig.ORBIT_RADIUS;
                    if(Math.abs(e.x-bx)<=e.definition.halfWidth+EvolutionConfig.ORBIT_HALF_WIDTH && Math.abs(e.y-by)<=e.definition.halfHeight+EvolutionConfig.ORBIT_HALF_WIDTH
                        && grid.clearLine(cx,cy,bx,by,.5f,.5f,true) && grid.clearLine(cx,cy,e.x,e.y,.5f,.5f,true)) {hit=true;break;}
                }
            }
            if(hit) {
                orbitIds[slot]=e.id;orbitNext[slot]=orbitCooldownTime+EvolutionConfig.ORBIT_HIT_COOLDOWN;
                int before=e.hp();e.damage(damage);landed|=e.hp()<before;orbitHits++;frameHits++;
                projectiles.effect(ProjectileSystem.EffectType.HIT,e.x,e.y,0,0,5);
            }
        }
        int before=(int)(orbitAngle/MathUtils.PI);orbitAngle=(orbitAngle+turn)%(MathUtils.PI2);
        if(before!=(int)(orbitAngle/MathUtils.PI)) {weapon.activations++;audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_SWORD_SWING);}
        if(landed && orbitTime>=nextOrbitSound) {audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_SWORD_HIT);nextOrbitSound=orbitTime+.12f;}
        weapon.cooldownRemaining=Math.max(0,EvolutionConfig.ORBIT_HIT_COOLDOWN-orbitCooldownTime%EvolutionConfig.ORBIT_HIT_COOLDOWN);
        orbitX=p.x;orbitY=p.y;
    }
    public void clearTransient() {
        projectiles.clear();orbitStarted=false;infernoCooldownBoostTime=infernoCooldownBoostLockout=0;infernoFireballCasts=0;
    }
}
