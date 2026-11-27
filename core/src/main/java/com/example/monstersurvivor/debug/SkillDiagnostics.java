package com.example.monstersurvivor.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.EnemyRegistry;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.skill.*;

/** Local opt-in Android evidence only. Absent skill-test.json: no instance, tuning, polling or allocations. */
public final class SkillDiagnostics {
    public final FrameMetrics metrics=new FrameMetrics();
    private final float progress;
    private final boolean protectedHp;
    private final DiagnosticRun run;
    private boolean pendingDeath;
    private boolean boxOnCast;
    private long session=-1,previous;
    private int frame,serial=-1;
    private final float[] frames=new float[120],activeFrames=new float[120];
    private final long[] cpu=new long[FrameMetrics.Part.values().length];
    private static final FrameMetrics.Part[] PARTS=FrameMetrics.Part.values();
    private int activeCount;
    private int enemyTraceUntil,enemyTraceSerial;
    private float maxActiveMs;
    private SkillDiagnostics(JsonValue v,DiagnosticRun run) {this.run=run;progress=Math.max(0,Math.min(600,v.getFloat("progress",0)));protectedHp=v.getBoolean("protectedHp",true);}
    public static SkillDiagnostics load(DiagnosticRun run) {var v=run.object("skill-test.json");return v==null?null:new SkillDiagnostics(v,run);}
    private JsonValue command() {
        return run.object("skill-command.json");
    }
    /** Explicit non-Normal diagnostic chest placement; use the real collection/commit path. */
    private boolean skillBox(GameScreen screen) {
        var s=screen.getSession();
        if(s.config.mode==GameMode.NORMAL)return false;
        if(s.pickups.spawn(com.example.monstersurvivor.pickup.PickupType.TREASURE_CHEST,s.player.x,s.player.y,1)!=null)return true;
        for(int i=0;i<s.pickups.size();i++)if(s.pickups.get(i).type==com.example.monstersurvivor.pickup.PickupType.TREASURE_CHEST) {
            s.pickups.collect(s.pickups.get(i).id);return s.choosingSkill();
        }
        return false;
    }
    public GameConfig tuning(GameConfig c) {
        return new GameConfig.Builder().difficulty(c.difficulty).mode(c.mode).language(c.language).skills(c.skills).weapons(c.weapons).pickups(c.pickups).bosses(c.bosses).visuals(c.visuals)
            .playerHp(protectedHp?100000:c.playerHp).debug(true).build();
    }
    private void status(GameScreen screen,String reason) {
        var s=screen.getSession();var d=s.skills.view(SkillType.DASH);var u=s.skills.view(SkillType.ULTIMATE);
        Gdx.app.log("SkillDiagnostics","status="+reason+" serial="+serial+" session="+s.id+" phase="+s.phase()+" paused="+screen.isPaused()+" elapsed="+s.elapsed()+" progress="+s.progress()
            +" player="+s.player.x+","+s.player.y+" hp="+s.player.hp()+" dashCd="+d.cooldownRemaining()+" dashActive="+d.activeRemaining()+" dashCount="+d.activations()
            +" dashLevel="+d.level()+" dashTotal="+d.totalCooldown()+" ultLevel="+u.level()+" ultTotal="+u.totalCooldown()
            +" ultCd="+u.cooldownRemaining()+" ultActive="+u.activeRemaining()+" ultCount="+u.activations()+" impacts="+s.skills.ultimateImpacts+" hits="+s.skills.ultimateHits
            +" bossHits="+s.skills.ultimateBossHits+" knockbacks="+s.skills.knockbacks+" weapons="+s.weapons.loadout.describe()+" normal="+s.enemies.normalSize()+" bosses="+s.enemies.bossSize());
        Gdx.app.log("EvolutionDiagnostics","status="+reason+" serial="+serial+" weapons="+s.weapons.loadout.describe()+" projectiles="+s.weapons.projectiles.size()+" effects="+s.weapons.projectiles.effectCount()
            +" zones="+s.weapons.projectiles.zoneCount()+" orbitHits="+s.weapons.orbitHits+" explosions="+s.weapons.projectiles.explosions+" burnHits="+s.weapons.projectiles.burnHits+" lightningHits="+s.weapons.lightningHits
            +" normal="+s.enemies.normalSize()+" bosses="+s.enemies.bossSize()+" miniHp="+(s.bosses.mini()==null?-1:s.bosses.mini().hp())+" lichHp="+(s.bosses.lich()==null?-1:s.bosses.lich().hp())+" lichPhase="+(s.bosses.lich()==null?0:s.bosses.lich().phase()));
        var hostile=s.enemies.skills;var pool=hostile.projectiles;
        Gdx.app.log("EnemyDiagnostics","status="+reason+" serial="+serial+" phase="+s.phase()+" enabled="+hostile.enabled()+" shots="+pool.size()+" areas="+pool.areaCount()+" fx="+pool.effectCount()+" casters="+hostile.activeCasters()+" telegraphs="+hostile.telegraphs()+" starts="+hostile.starts+" releases="+hostile.releases+" fired="+pool.fired+" hits="+pool.hits+" blocked="+pool.blocked+" cancelled="+hostile.cancelled+" maxCasters="+hostile.maxCasters+" maxTelegraphs="+hostile.maxTelegraphs+" astarMax="+s.enemies.maxSearchesObserved+" poison="+s.player.statuses.remaining(com.example.monstersurvivor.enemy.skill.PlayerStatuses.Type.POISON)+" burn="+s.player.statuses.remaining(com.example.monstersurvivor.enemy.skill.PlayerStatuses.Type.BURN)+" slow="+s.player.statuses.remaining(com.example.monstersurvivor.enemy.skill.PlayerStatuses.Type.SLOW));
        for(int i=0;i<com.example.monstersurvivor.enemy.skill.EnemyProjectiles.CAPACITY;i++){
            var p=pool.shot(i);if(p.active)Gdx.app.log("EnemyDiagnostics","shot serial="+serial+" id="+p.id+" owner="+p.ownerId+" skill="+p.definition+" x="+p.x+" y="+p.y+" vx="+p.vx+" vy="+p.vy+" age="+p.age);
        }
    }
    private void enemyArena(GameScreen screen,String group){
        var s=screen.getSession();s.enemies.diagnosticClearOrdinary();
        String[] ids=switch(group){
            case "nonHoming"->new String[]{"mc_flame_skull_soul"};
            case "poisonOnly"->new String[]{"mc_mushroom_folk_toxic"};
            case "iceOnly"->new String[]{"mc_frost_fox"};
            case "target"->new String[]{"mc_mummy_cat"};
            case "straight"->new String[]{"sc_goblin","mc_kobold_miner","mc_mimic_jar","mc_flytrap_walker"};
            case "fan"->new String[]{"mc_thorn_sprite","mc_swamp_hag","mc_dust_devil","mc_flame_skull","mc_flame_skull_soul","mc_frost_moth"};
            case "poisonIce"->new String[]{"mc_mushroom_folk_toxic","mc_bog_lurker","mc_frost_fox","mc_frost_moth"};
            case "aoe"->new String[]{"mc_mummy_cat","mc_goblin_bomber","mc_moss_golem","mc_jelly"};
            default->new String[]{"mc_ash_hound","mc_magma_slug","mc_wraith","mc_lily_snapper"};
        };
        int placed=0;var grid=s.enemies.navigation.collision;
        for(String id:ids){boolean found=false;var d=EnemyRegistry.get(id);
            for(int y=-6;y<=6 && !found;y++)for(int x=-6;x<=6 && !found;x++){
                float tx=s.player.x+x*16,ty=s.player.y+y*16,distance=(float)Math.hypot(tx-s.player.x,ty-s.player.y);
                var camera=screen.getCamera();if(tx<camera.position.x-camera.viewportWidth/2+20 || tx>camera.position.x+camera.viewportWidth/2-20 || ty<camera.position.y-camera.viewportHeight/2+com.example.monstersurvivor.enemy.skill.EnemySkillSystem.BOTTOM_MARGIN+6 || ty>camera.position.y+camera.viewportHeight/2-com.example.monstersurvivor.enemy.skill.EnemySkillSystem.TOP_MARGIN-6)continue;
                boolean slam=d.skill.kind==com.example.monstersurvivor.enemy.skill.EnemySkillDefinition.Kind.SLAM;
                if(distance<(slam?35:68) || distance>(slam?54:96) || !grid.canOccupy(tx,ty,d.halfWidth,d.halfHeight,d.flying) || !grid.clearLine(tx,ty,s.player.x,s.player.y,2,2,true))continue;
                boolean occupied=false;for(int i=0;i<s.enemies.size();i++){var e=s.enemies.get(i);if(Math.hypot(e.x-tx,e.y-ty)<22)occupied=true;}if(occupied)continue;
                var e=s.enemies.spawn(d,tx,ty,s.progress());if(e!=null){e.specialSkill.cooldown=1+(placed%4)*.5f;placed++;found=true;}
            }
        }
        Gdx.app.log("EnemyDiagnostics","arena group="+group+" placed="+placed+" (diagnostic placement, normal stats/caps, no equipment grants)");
    }
    /** Fixture placement uses actual grid/LOS and leaves Boss HP/AI/tuning unchanged. */
    private void arena(GameScreen screen) {
        var s=screen.getSession();s.weapons.clearTransient();s.weapons.loadout.reset();
        var nav=s.enemies.navigation;var grid=nav.collision;int placed=0;
        // A few durable ordinary targets survive base Ultimate to exercise real knockback.
        for(int y=-6;y<=6 && placed<4;y++)for(int x=-6;x<=6 && placed<4;x++) {
            float tx=s.player.x+x*16,ty=s.player.y+y*16,d=(float)Math.hypot(tx-s.player.x,ty-s.player.y);
            if(d<65 || d>90 || !grid.clearLine(s.player.x,s.player.y,tx,ty,6,4,false))continue;
            if(s.enemies.spawn(EnemyRegistry.get("rock_golem"),tx,ty,s.progress())!=null)placed++;
        }
        var boss=s.bosses.lich();boolean moved=false;
        if(boss!=null && boss.actor.alive()) {
            for(int y=-5;y<=5 && !moved;y++)for(int x=-5;x<=5 && !moved;x++) {
                float tx=s.player.x+x*16,ty=s.player.y+y*16,d=(float)Math.hypot(tx-s.player.x,ty-s.player.y);
                if(d<70 || d>90 || !grid.canOccupy(tx,ty,9,6) || !grid.clearLine(s.player.x,s.player.y,tx,ty,.5f,.5f,true))continue;
                boss.actor.x=tx;boss.actor.y=ty;boss.actor.path.clear();boss.actor.pathIndex=0;boss.actor.lineTimer=boss.actor.pathTimer=0;moved=true;
            }
        }
        Gdx.app.log("SkillDiagnostics","arena ordinary="+placed+" bossPlaced="+moved+" bossHp="+(boss==null?-1:boss.hp())+" (fixture placement, no HP/AI changes)");
    }
    public void before(GameScreen screen) {
        var s=screen.getSession();
        if(session!=s.id) {session=s.id;previous=0;frame=activeCount=enemyTraceUntil=0;maxActiveMs=0;pendingDeath=boxOnCast=false;s.diagnosticProgress(progress);
            Gdx.app.log("SkillDiagnostics","LOCAL FIXTURE protectedHp="+protectedHp+" progress="+progress+" skill tuning/cooldowns unchanged");}
        metrics.reset();
        if(frame%15==0) {
            var command=command();if(command!=null) {int next=command.getInt("serial",0);
                if(next!=serial) {serial=next;String action=command.getString("action","");
                    if(action.equals("progress"))s.diagnosticProgress(command.getFloat("value"));
                    else if(action.equals("enemyArena") && s.phase()==GameSession.Phase.RUNNING)enemyArena(screen,command.getString("group","straight"));
                    else if(action.equals("enemySkills"))s.enemies.skills.diagnosticEnabled(command.getBoolean("enabled",true));
                    else if(action.equals("enemyTrace")){enemyTraceUntil=command.getBoolean("enabled",true)?frame+180:0;enemyTraceSerial=serial;}
                    else if(action.equals("arena") && s.phase()==GameSession.Phase.RUNNING)arena(screen);
                    else if(action.equals("evolutionTargets") && s.phase()==GameSession.Phase.RUNNING) {
                        var grid=s.enemies.navigation.collision;int placed=0;
                        for(int y=-3;y<=3 && placed<12;y++)for(int x=2;x<=7 && placed<12;x++) {
                            float tx=s.player.x+x*16,ty=s.player.y+y*16;
                            if(!grid.canOccupy(tx,ty,6,4) || !grid.clearLine(s.player.x,s.player.y,tx,ty,1,1,true))continue;
                            if(s.enemies.spawn(EnemyRegistry.get("rock_golem"),tx,ty,s.progress())!=null)placed++;
                        }
                        Gdx.app.log("EvolutionDiagnostics","targets placed="+placed+" (fixture only, unchanged resident cap)");
                    }
                    else if(action.equals("evolutionBossArena") && s.phase()==GameSession.Phase.RUNNING) {
                        var grid=s.enemies.navigation.collision;
                        for(int i=0;i<2;i++) {
                            var boss=i==0?s.bosses.mini():s.bosses.lich();if(boss==null || !boss.actor.alive())continue;boolean moved=false;
                            for(int y=-4;y<=4 && !moved;y++)for(int x=3;x<=7 && !moved;x++) {
                                float tx=s.player.x+x*16,ty=s.player.y+y*16;
                                if(!grid.canOccupy(tx,ty,9,6) || !grid.clearLine(s.player.x,s.player.y,tx,ty,1,1,true))continue;
                                boss.actor.x=tx;boss.actor.y=ty;boss.actor.path.clear();boss.actor.pathIndex=0;boss.actor.lineTimer=boss.actor.pathTimer=0;moved=true;
                            }
                        }
                    }
                    else if(action.equals("phase2") && s.bosses.lich()!=null)s.bosses.lich().actor.damage(s.bosses.lich().maxHp());
                    else if(action.equals("weaponBox") && s.phase()==GameSession.Phase.RUNNING) {
                        boolean placed=s.config.mode!=GameMode.NORMAL
                            && s.pickups.spawn(com.example.monstersurvivor.pickup.PickupType.TREASURE_CHEST,s.player.x,s.player.y,1)!=null;
                        if(!placed)for(int i=0;i<s.pickups.size();i++)if(s.pickups.get(i).type==com.example.monstersurvivor.pickup.PickupType.TREASURE_CHEST){s.pickups.collect(s.pickups.get(i).id);placed=s.choosingChest();break;}
                        Gdx.app.log("EvolutionDiagnostics","treasureChest placed="+placed);
                    }
                    else if(action.equals("modal") && s.phase()==GameSession.Phase.RUNNING) {
                        var p=s.config.mode==GameMode.NORMAL?null:s.pickups.spawn(com.example.monstersurvivor.pickup.PickupType.TREASURE_CHEST,s.player.x,s.player.y,1);
                        Gdx.app.log("SkillDiagnostics","modal placement="+(p!=null));
                    }
                    else if(action.equals("playerDeath"))pendingDeath=true;
                    else if(action.equals("boxOnCast"))boxOnCast=true;
                    else if(action.equals("skillBox") && s.phase()==GameSession.Phase.RUNNING) {
                        Gdx.app.log("SkillDiagnostics","treasureChest placement="+skillBox(screen));
                    }
                    status(screen,action);
                }
            }
        }
        if(boxOnCast && s.phase()==GameSession.Phase.RUNNING && (s.skills.view(SkillType.DASH).phase()==ActiveSkillSystem.Phase.ACTIVE || s.skills.view(SkillType.ULTIMATE).phase()==ActiveSkillSystem.Phase.ACTIVE)) {
            if(skillBox(screen)) {boxOnCast=false;Gdx.app.log("SkillDiagnostics","boxOnCast Treasure Chest placed");}
        }
        // Exercise the real damage path after existing immunity expires, without bypassing either guard.
        if(pendingDeath && !screen.isPaused() && s.phase()==GameSession.Phase.RUNNING && !s.player.invulnerable()) {
            s.player.damage(Integer.MAX_VALUE);pendingDeath=false;
        }
    }
    public void after(GameScreen screen) {
        var s=screen.getSession();long now=System.nanoTime();float ms=previous==0?0:(now-previous)/1_000_000f;previous=now;frames[frame%frames.length]=ms;
        boolean active=s.skills.view(SkillType.DASH).phase()==ActiveSkillSystem.Phase.ACTIVE || s.skills.ultimateVisible();
        if(active && !screen.isPaused() && s.phase()==GameSession.Phase.RUNNING) {activeFrames[activeCount++%activeFrames.length]=ms;maxActiveMs=Math.max(maxActiveMs,ms);}
        for(int i=0;i<cpu.length;i++)cpu[i]+=metrics.value(PARTS[i]);
        if(frame<enemyTraceUntil)for(int i=0;i<com.example.monstersurvivor.enemy.skill.EnemyProjectiles.CAPACITY;i++){
            var p=s.enemies.skills.projectiles.shot(i);if(p.active)Gdx.app.log("EnemyDiagnostics","trace="+enemyTraceSerial+" frame="+frame+" id="+p.id+" x="+p.x+" y="+p.y+" vx="+p.vx+" vy="+p.vy+" age="+p.age+" playerX="+s.player.x+" playerY="+s.player.y+" dashActive="+s.skills.view(SkillType.DASH).activeRemaining()+" hits="+s.enemies.skills.projectiles.hits);
        }
        if(++frame%120==0) {
            float total=0,max=0;for(float v:frames){total+=v;max=Math.max(max,v);}var sorted=frames.clone();java.util.Arrays.sort(sorted);
            var line=new StringBuilder("window frames=120 fps=").append(120000/total).append(" meanMs=").append(total/120).append(" p95Ms=").append(sorted[113])
                .append(" maxMs=").append(max).append(" elapsed=").append(s.elapsed()).append(" phase=").append(s.phase()).append(" paused=").append(screen.isPaused())
                .append(" normal=").append(s.enemies.normalSize()).append(" bosses=").append(s.enemies.bossSize()).append(" activeSamples=").append(activeCount).append(" activeMaxMs=").append(maxActiveMs);
            line.append(" weapons=").append(s.weapons.loadout.describe()).append(" projectiles=").append(s.weapons.projectiles.size()).append(" zones=").append(s.weapons.projectiles.zoneCount())
                .append(" lichPhase=").append(s.bosses.lich()==null?0:s.bosses.lich().phase());
            int count=Math.min(activeCount,activeFrames.length);float sum=0;for(int i=0;i<count;i++)sum+=activeFrames[i];line.append(" activeMeanMs=").append(count==0?0:sum/count);
            for(int i=0;i<cpu.length;i++){line.append(' ').append(PARTS[i]).append("Ms=").append(cpu[i]/120_000_000f);cpu[i]=0;}
            line.append(" glError=").append(Gdx.gl.glGetError());
            line.append(" enemyShots=").append(s.enemies.skills.projectiles.size()).append(" enemyAreas=").append(s.enemies.skills.projectiles.areaCount()).append(" enemyFx=").append(s.enemies.skills.projectiles.effectCount()).append(" enemyCasters=").append(s.enemies.skills.activeCasters()).append(" enemyTelegraphs=").append(s.enemies.skills.telegraphs()).append(" enemyEnabled=").append(s.enemies.skills.enabled());
            Gdx.app.log("SkillDiagnostics",line.toString());
        }
    }
}
