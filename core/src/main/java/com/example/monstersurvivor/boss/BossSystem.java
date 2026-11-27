package com.example.monstersurvivor.boss;

import com.badlogic.gdx.math.RandomXS128;
import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.assets.GameAssets.State;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.enemy.EnemyRegistry;
import com.example.monstersurvivor.localization.GameStrings.Key;

/** Two independent, once-per-session events plus bounded real-time boss combat. No ordinary spawn dependency. */
public final class BossSystem {
    private double nextEndlessBoss=EndlessRules.BOSS_INTERVAL;
    private boolean endlessPending;
    private int endlessWave,endlessKills;
    public int endlessWave(){return endlessWave;}
    public int endlessKills(){return endlessKills;}
    public boolean endlessPending(){return endlessPending;}
    public com.example.monstersurvivor.audio.AudioSink audio=com.example.monstersurvivor.audio.AudioSink.NONE;
    public enum EventState { NOT_TRIGGERED, SPAWNING, ACTIVE, DEFEATED }
    private final GameConfig game;
    private final EnemySystem enemies;
    private final RandomXS128 random;
    public final BossProjectiles projectiles;
    private EventState miniState=EventState.NOT_TRIGGERED,finalState=EventState.NOT_TRIGGERED;
    private BossDefinition selected;
    private BossInstance mini,lich;
    private float miniRetry,finalRetry;
    private int fallback;
    public int spawnWindows,spawnFailures,noticeSerial;
    private Key notice=Key.MINI_BOSS_APPEARS;
    private BossDefinition noticeBoss;
    private float noticeTime;
    public BossSystem(GameConfig game,EnemySystem enemies,long seed) {
        this.game=game;this.enemies=enemies;random=new RandomXS128(seed^0xB05551L);projectiles=new BossProjectiles(game.bosses.projectileCap);
    }
    public EventState miniState(){return miniState;} public EventState finalState(){return finalState;}
    public BossInstance mini(){return mini;} public BossInstance lich(){return lich;}
    public BossInstance primary(){return lich!=null && lich.visible()?lich:mini!=null && mini.visible()?mini:null;}
    public boolean miniDefeated(){return miniState==EventState.DEFEATED;}
    public boolean finalDefeated(){return finalState==EventState.DEFEATED;}
    public Key noticeKey(){return notice;} public BossDefinition noticeBoss(){return noticeBoss;} public float noticeTime(){return noticeTime;}
    public void onDeath(Enemy enemy) {
        if(mini!=null && mini.actor==enemy && miniState!=EventState.DEFEATED) {
            miniState=EventState.DEFEATED;
            if(mini.definition.kind()==BossDefinition.Kind.ENDLESS && endlessKills<Integer.MAX_VALUE)endlessKills++;
            dead(mini);
        }
        if(lich!=null && lich.actor==enemy && finalState!=EventState.DEFEATED) {finalState=EventState.DEFEATED;dead(lich);}
    }
    private void dead(BossInstance b) {
        audio.play(b.finalBoss()?com.example.monstersurvivor.audio.AudioEvent.LICH_DEFEATED:com.example.monstersurvivor.audio.AudioEvent.MINI_DEFEATED);
        projectiles.clearOwner(b.actor.id);b.cast=BossInstance.Cast.NONE;b.actor.aiLocked=true;
        // A simultaneous Mini kill must not erase the Final entrance/phase/death announcement.
        if(!b.finalBoss() && noticeTime>0 && noticeBoss!=null && noticeBoss.kind()==BossDefinition.Kind.LICH_LORD)return;
        announce(Key.BOSS_DEFEATED,b.definition);
    }
    private void announce(Key key,BossDefinition boss) {notice=key;noticeBoss=boss;noticeTime=game.bosses.announcementSeconds;noticeSerial++;}
    public void prepare(float dt,float progress,Player player,float left,float bottom,float right,float top) {
        noticeTime=Math.max(0,noticeTime-dt);
        if(game.mode==GameMode.ENDLESS){prepareEndless(dt,progress,player,left,bottom,right,top);return;}
        if(progress>=game.bosses.miniProgress && miniState==EventState.NOT_TRIGGERED) {
            selected=game.bosses.miniPool.get(random.nextInt(game.bosses.miniPool.size()));miniState=EventState.SPAWNING;announce(Key.BOSS_WARNING,selected);
        }
        if(progress>=game.bosses.finalProgress && finalState==EventState.NOT_TRIGGERED) {finalState=EventState.SPAWNING;announce(Key.BOSS_WARNING,game.bosses.lich);}
        miniRetry-=dt;finalRetry-=dt;
        if(miniState==EventState.SPAWNING && miniRetry<=0) {
            miniRetry=game.bosses.retrySeconds;mini=place(selected,player,left,bottom,right,top);
            if(mini!=null) {miniState=EventState.ACTIVE;announce(Key.MINI_BOSS_APPEARS,selected);audio.play(com.example.monstersurvivor.audio.AudioEvent.MINI_APPEARS);}
        }
        if(finalState==EventState.SPAWNING && finalRetry<=0) {
            finalRetry=game.bosses.retrySeconds;lich=place(game.bosses.lich,player,left,bottom,right,top);
            if(lich!=null) {finalState=EventState.ACTIVE;announce(Key.FINAL_BOSS_APPEARS,game.bosses.lich);audio.play(com.example.monstersurvivor.audio.AudioEvent.LICH_APPEARS);}
        }
        prepare(mini,dt,player);prepare(lich,dt,player);
    }
    /** One live encounter and at most one deferred event, never a queue of missed intervals. */
    private void prepareEndless(float dt,float progress,Player player,float left,float bottom,float right,float top) {
        if(progress>=nextEndlessBoss){
            endlessPending=true;
            nextEndlessBoss=(Math.floor((double)progress/EndlessRules.BOSS_INTERVAL)+1)*EndlessRules.BOSS_INTERVAL;
        }
        if(endlessPending && miniState!=EventState.SPAWNING && (mini==null || mini.actor.deathFinished())) {
            endlessPending=false;endlessWave=Math.min(Integer.MAX_VALUE-1,endlessWave+1);
            selected=EndlessRules.boss(random.nextInt(EndlessRules.bossCandidates()),endlessWave);
            mini=null;miniState=EventState.SPAWNING;miniRetry=0;announce(Key.BOSS_WARNING,selected);
        }
        miniRetry-=dt;
        if(miniState==EventState.SPAWNING && miniRetry<=0){
            miniRetry=game.bosses.retrySeconds;mini=place(selected,player,left,bottom,right,top);
            if(mini!=null){miniState=EventState.ACTIVE;announce(Key.ENDLESS_BOSS_APPEARS,selected);audio.play(com.example.monstersurvivor.audio.AudioEvent.MINI_APPEARS);}
        }
        prepare(mini,dt,player);
    }
    private BossInstance place(BossDefinition definition,Player player,float left,float bottom,float right,float top) {
        spawnWindows++;var nav=enemies.bossNavigation;int goal=nav.approach(player.x,player.y,false);
        if(goal>=0) {
            for(int i=0;i<64;i++) {
                float angle=random.nextFloat()*6.2831853f;
                float x=(left+right)*.5f+(float)Math.cos(angle)*((right-left)*.5f+24);
                float y=(bottom+top)*.5f+(float)Math.sin(angle)*((top-bottom)*.5f+24);
                int cell=nav.cell(x,y);
                if(valid(cell,goal,player,left,bottom,right,top))return spawn(definition,nav.x(cell),nav.y(cell));
            }
            // Deterministic near-player fallback, never a remote map corner.
            int cx=(int)(player.x/nav.collision.tileSize),cy=(int)(player.y/nav.collision.tileSize);
            for(int i=0;i<256;i++) {
                int offset=fallback++%289,x=cx-8+offset%17,y=cy-8+offset/17;
                if(x<0 || y<0 || x>=nav.collision.width || y>=nav.collision.height)continue;int cell=y*nav.collision.width+x;
                if(valid(cell,goal,player,left,bottom,right,top))return spawn(definition,nav.x(cell),nav.y(cell));
            }
        }
        spawnFailures++;return null; // SPAWNING retained; the next finite retry is scheduled above.
    }
    private boolean valid(int cell,int goal,Player player,float left,float bottom,float right,float top) {
        var nav=enemies.bossNavigation;if(!nav.connected(cell,goal,false))return false;
        float x=nav.x(cell),y=nav.y(cell),dx=x-player.x,dy=y-player.y;
        float limit=Math.max(right-left,top-bottom)*.6f+48;
        if(dx*dx+dy*dy<64*64 || dx*dx+dy*dy>limit*limit)return false;
        for(int i=0;i<enemies.size();i++) {var e=enemies.get(i);dx=x-e.x;dy=y-e.y;float space=e.boss?40:20;if(dx*dx+dy*dy<space*space)return false;}
        return nav.collision.canOccupy(x,y,9,6,false);
    }
    private BossInstance spawn(BossDefinition definition,float x,float y) {
        var stats=definition.kind()==BossDefinition.Kind.ENDLESS?EndlessRules.bossStats(game.difficulty,endlessWave):game.bosses.stats(definition,game.difficulty,game.mode);
        var actor=enemies.spawnBoss(definition.body(),stats,x,y);
        if(actor==null)return null;
        actor.limitBurst(game.bosses.burstFraction,game.bosses.burstWindow);actor.aiLocked=true;
        var boss=new BossInstance(definition,actor);boss.pose(State.SPAWN,x,y-1);return boss;
    }
    private void prepare(BossInstance b,float dt,Player player) {
        if(b==null || !b.actor.alive())return;
        var e=b.actor;b.impactTime=Math.max(0,b.impactTime-dt);
        if(b.entry>0) {b.entry-=dt;e.aiLocked=true;if(b.entry<=0)e.releasePose();return;}
        if(b.finalBoss() && b.phase==1 && e.hp()<=(long)e.stats.hp/2) {
            b.phase=2;b.phaseChanges++;projectiles.clearOwner(e.id);b.cast=BossInstance.Cast.PHASE_SHIFT;b.castDuration=b.castRemaining=1;
            b.pose(State.ENTRANCE,player.x,player.y);announce(Key.BOSS_PHASE_TWO,b.definition);
            audio.play(com.example.monstersurvivor.audio.AudioEvent.LICH_PHASE2);
        }
        e.aiLocked=b.cast!=BossInstance.Cast.NONE;
        if(e.aiLocked) {
            // A completed Hurt clip may return to the current spell pose; repeated hits never restart Hurt.
            if(e.state()==State.IDLE)b.pose(b.cast==BossInstance.Cast.BOLT?State.ATTACK:b.cast==BossInstance.Cast.PHASE_SHIFT?State.ENTRANCE:b.finalBoss()?State.SLAM:State.SPECIAL,b.aimX,b.aimY);
            return;
        }
        e.standOffRange=b.finalBoss()?104:0;
        b.spellCooldown-=dt;b.areaCooldown-=dt;b.summonCooldown-=dt;
        float dx=player.x-e.x,dy=player.y-e.y,d2=dx*dx+dy*dy;
        boolean clear=enemies.navigation.collision.clearLine(e.x,e.y,player.x,player.y,.5f,.5f,true);
        boolean hard=game.difficulty==Difficulty.HARD;
        if(!b.finalBoss()) {
            if(b.areaCooldown<=0 && d2<80*80 && clear)begin(b,BossInstance.Cast.SLAM,e.x,e.y,48,hard?.95f:1.1f,State.SPECIAL);
        } else {
            if(b.areaCooldown<=0 && d2<240*240 && clear)begin(b,BossInstance.Cast.AREA,player.x,player.y,b.phase==1?34:44,b.phase==1?1.2f:1,State.SLAM);
            else if(b.spellCooldown<=0 && d2<240*240 && clear)begin(b,BossInstance.Cast.BOLT,player.x,player.y,0,b.phase==1?1:b.phase==2 && hard?.65f:.75f,State.ATTACK);
        }
    }
    private void begin(BossInstance b,BossInstance.Cast cast,float x,float y,float radius,float windup,State pose) {
        b.cast=cast;b.castRemaining=b.castDuration=windup;b.aimX=x;b.aimY=y;b.radius=radius;b.actor.aiLocked=true;b.pose(pose,x,y);
        if(cast==BossInstance.Cast.SLAM)audio.play(com.example.monstersurvivor.audio.AudioEvent.MINI_SLAM_WARNING);
        else if(cast==BossInstance.Cast.AREA)audio.play(com.example.monstersurvivor.audio.AudioEvent.LICH_AOE_WARNING);
    }
    /** Called only after player weapons/contact and only if the player is still alive. */
    public void fight(float dt,float progress,Player player) {
        finish(mini,dt,progress,player);if(player.alive())finish(lich,dt,progress,player);
        if(player.alive())projectiles.update(dt,player,enemies.navigation.collision);
    }
    private void finish(BossInstance b,float dt,float progress,Player player) {
        if(b==null || !b.actor.alive())return;boolean hard=game.difficulty==Difficulty.HARD;
        if(b.cast!=BossInstance.Cast.NONE) {
            b.castRemaining-=dt;if(b.castRemaining>0)return;
            var cast=b.cast;b.cast=BossInstance.Cast.NONE;b.actor.aiLocked=false;b.actor.releasePose();
            if(cast==BossInstance.Cast.PHASE_SHIFT) {b.spellCooldown=.6f;b.areaCooldown=2;b.summonCooldown=0;return;}
            if(cast==BossInstance.Cast.BOLT) {
                int lanes=b.phase==1?1:hard?5:3;float dx=b.aimX-b.actor.x,dy=b.aimY-b.actor.y;
                boolean released=false;
                for(int i=0;i<lanes;i++) {float a=(i-(lanes-1)*.5f)*.24f,c=(float)Math.cos(a),s=(float)Math.sin(a);
                    released|=projectiles.fire(b.actor.id,b.actor.x,b.actor.y+4,dx*c-dy*s,dx*s+dy*c,hard?95:85,b.actor.stats.damage);}
                b.primaryCasts++;b.spellCooldown=b.phase==1?(hard?3.4f:3.8f):(hard?2:2.4f);
                if(released)audio.play(com.example.monstersurvivor.audio.AudioEvent.LICH_PROJECTILE);
            } else {
                audio.play(b.finalBoss()?com.example.monstersurvivor.audio.AudioEvent.LICH_AOE_IMPACT:com.example.monstersurvivor.audio.AudioEvent.MINI_SLAM_IMPACT);
                float dx=player.x-b.aimX,dy=player.y-b.aimY;
                if(dx*dx+dy*dy<=(b.radius+Player.HALF_WIDTH)*(b.radius+Player.HALF_WIDTH)
                    && enemies.navigation.collision.clearLine(b.aimX,b.aimY,player.x,player.y,.5f,.5f,true))player.damageDirect(b.actor.stats.damage+(!b.finalBoss() && !hard?1:0),b.actor.x,b.actor.y);
                b.areaCasts++;b.impactTime=.25f;b.areaCooldown=!b.finalBoss()?(hard?5:6):b.phase==1?(hard?6:7):(hard?4:4.8f);
                if(b.definition.kind()==BossDefinition.Kind.ENDLESS)enemies.skills.projectiles.effect("quake",b.aimX,b.aimY,b.radius*2,.4f);
            }
        }
        if(b.finalBoss() && b.phase==2 && b.summonCooldown<=0) {
            b.summonCooldown=hard?8:10;int count=0;
            for(int i=0;i<enemies.size();i++) {var e=enemies.get(i);if(e.summonOwner==b.actor.id && e.alive())count++;}
            if(count>=(hard?3:2) || enemies.normalSize()>=game.maxEnemies)return;
            var type=EnemyRegistry.get("mc_bat_swarm");var nav=enemies.navigation;int goal=nav.cell(player.x,player.y);
            for(int i=0;i<16;i++) {
                float a=random.nextFloat()*6.2831853f,x=b.actor.x+(float)Math.cos(a)*40,y=b.actor.y+(float)Math.sin(a)*40;
                if(!nav.connected(nav.cell(x,y),goal,type.flying) || Math.hypot(x-player.x,y-player.y)<32)continue;
                var e=enemies.spawn(type,x,y,progress);if(e!=null) {e.summonOwner=b.actor.id;b.summoned++;break;}
            }
        }
    }
    public void stopCombat() {
        projectiles.clear();for(int i=0;i<2;i++) {var b=i==0?mini:lich;if(b!=null) {b.cast=BossInstance.Cast.NONE;b.impactTime=0;b.actor.aiLocked=true;}}
    }
    public void clear() {stopCombat();mini=lich=null;miniState=finalState=EventState.NOT_TRIGGERED;noticeTime=0;
        endlessWave=endlessKills=0;endlessPending=false;nextEndlessBoss=EndlessRules.BOSS_INTERVAL;}
}
