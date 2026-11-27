package com.example.monstersurvivor.skill;

import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.audio.*;
import com.example.monstersurvivor.config.SkillConfig;
import com.example.monstersurvivor.enemy.EnemyArchetype;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.weapon.ProjectileSystem;
import com.example.monstersurvivor.world.CollisionGrid;

/** Session-owned real-gameplay clocks. No Player/Session references, I/O, rendering or per-frame allocations. */
public final class ActiveSkillSystem {
    public enum Phase { READY, ACTIVE, COOLDOWN }
    public static final class View {
        private SkillConfig.Tuning tuning;
        private int level=1;
        private float activationDuration;
        private double cooldown,remaining;
        private float rechargeWindow;
        private long activationId;
        private int activations;
        private boolean pendingImpact;
        private View(SkillConfig.Tuning tuning) {this.tuning=tuning;reset();}
        private void reset() {cooldown=tuning.initialCooldown;remaining=0;activationId=0;activations=0;pendingImpact=false;rechargeWindow=tuning.initialCooldown;}
        public Phase phase() {return remaining>0 || pendingImpact?Phase.ACTIVE:cooldown>0?Phase.COOLDOWN:Phase.READY;}
        public float cooldownRemaining() {return (float)cooldown;}
        public float totalCooldown() {return tuning.cooldown;}
        public float activeRemaining() {return (float)remaining;}
        public float duration() {return phase()==Phase.ACTIVE?activationDuration:tuning.duration;}
        public int level() {return level;}
        public SkillConfig.Tuning tuning() {return tuning;}
        public float rechargeProgress() {return rechargeWindow<=0?1:Math.max(0,Math.min(1,1-(float)cooldown/rechargeWindow));}
        public long activationId() {return activationId;}
        public int activations() {return activations;}
        private void activate(long id) {activationDuration=tuning.duration;remaining=tuning.duration;cooldown=tuning.cooldown;rechargeWindow=tuning.cooldown;activationId=id;activations++;}
        private void upgrade(SkillConfig.Tuning next) {
            float ratio=next.cooldown/tuning.cooldown;
            cooldown*=ratio;rechargeWindow*=ratio;tuning=next;level++;
        }
    }
    private static final java.util.concurrent.atomic.AtomicLong TOKENS=new java.util.concurrent.atomic.AtomicLong();
    private final SkillConfig config;
    private final View dash,ultimate;
    private AudioSink audio=AudioSink.NONE;
    private float dashX,dashY,ultimateX,ultimateY,ultimateFade,dashTrail;
    private int ultimateDamage;
    private SkillConfig.Dash dashActivation;
    private SkillConfig.Ultimate ultimateActivation;
    private int dashActivationLevel=1,ultimateActivationLevel=1;
    private long impactedId;
    private final float[] trailX=new float[6],trailY=new float[6];
    private int trailCount;
    public int ultimateImpacts,ultimateHits,ultimateKills,ultimateBossHits,knockbacks;
    public ActiveSkillSystem(SkillConfig config) {this.config=config;dash=new View(config.dash);ultimate=new View(config.ultimate);dashActivation=config.dash;ultimateActivation=config.ultimate;}
    public SkillConfig config() {return config;}
    public View view(SkillType type) {return type==SkillType.DASH?dash:ultimate;}
    public void audio(AudioSink sink) {audio=sink;}
    public boolean canUpgrade(SkillType type) {return type!=null && view(type).level<SkillConfig.MAX_LEVEL;}
    public boolean hasUpgrade() {return canUpgrade(SkillType.DASH) || canUpgrade(SkillType.ULTIMATE);}
    /** Reward commit only; expected level makes a stale offer harmless. */
    boolean upgrade(SkillType type,int expectedLevel) {
        if(!canUpgrade(type))return false;
        View state=view(type);if(state.level!=expectedLevel)return false;
        state.upgrade(type==SkillType.DASH?config.dash(state.level+1):config.ultimate(state.level+1));return true;
    }
    public boolean dashInvulnerable() {return dash.remaining>0 && dashActivation.duration-dash.remaining<dashActivation.invulnerability-1e-6;}
    public int dashActivationLevel() {return dashActivationLevel;}
    public int ultimateActivationLevel() {return ultimateActivationLevel;}
    public float dashX() {return dashX;} public float dashY() {return dashY;}
    public float ultimateX() {return ultimateX;} public float ultimateY() {return ultimateY;}
    public float ultimateRadius() {return ultimateActivation.radius;}
    public float ultimateProgress() {return ultimate.remaining>0?1-(float)ultimate.remaining/ultimateActivation.duration:1;}
    public float ultimateOpacity() {return ultimate.remaining>0?.75f:ultimateFade/.28f*.8f;}
    public boolean ultimateVisible() {return ultimate.remaining>0 || ultimateFade>0;}
    public int trailCount() {return trailCount;}
    public float trailX(int i) {return trailX[i];} public float trailY(int i) {return trailY[i];}
    public float trailOpacity() {return dashTrail/.14f;}
    /** Called through GameSession after phase/pause checks. A rejected press changes no clocks/tokens. */
    public boolean activate(SkillType type,Player player,float x,float y) {
        if(type==null || !player.alive() || !Float.isFinite(x) || !Float.isFinite(y))return false;
        View state=view(type);if(state.phase()!=Phase.READY)return false;
        if(type==SkillType.DASH) {
            dashActivation=(SkillConfig.Dash)state.tuning;dashActivationLevel=state.level;
            if(Math.abs(x)+Math.abs(y)<.0001f) {x=player.lastMoveX();y=player.lastMoveY();}
            if(Math.abs(x)+Math.abs(y)<.0001f) {
                x=player.direction==com.example.monstersurvivor.assets.GameAssets.Direction.LEFT?-1:player.direction==com.example.monstersurvivor.assets.GameAssets.Direction.RIGHT?1:0;
                y=player.direction==com.example.monstersurvivor.assets.GameAssets.Direction.DOWN?-1:player.direction==com.example.monstersurvivor.assets.GameAssets.Direction.UP?1:0;
            }
            double length=Math.hypot(x,y);dashX=(float)(x/length);dashY=(float)(y/length);
            trailCount=0;dashTrail=0;audio.play(AudioEvent.SKILL_DASH_ACTIVATE);
        } else {
            ultimateActivation=(SkillConfig.Ultimate)state.tuning;ultimateActivationLevel=state.level;
            ultimateX=player.x;ultimateY=player.y;ultimateDamage=ultimateActivation.damage(player.stats.damage());
            ultimateFade=0;audio.play(AudioEvent.SKILL_ULTIMATE_CAST);
        }
        state.activate(TOKENS.incrementAndGet());return true;
    }
    private static double tick(double value,float dt) {double result=value-dt;return result<.000001?0:result;}
    /** Player's existing swept movement also records every dash segment for pickup contact. */
    public void advance(float dt,float axisX,float axisY,Player player,CollisionGrid grid) {
        advance(dt,axisX,axisY,player,grid,dt,0);
    }
    /** Buffs scale only cooldown clocks and ordinary running time; cast and Dash clocks stay at gameplay time. */
    public void advance(float dt,float axisX,float axisY,Player player,CollisionGrid grid,float cooldownDelta,float speedBoostSeconds) {
        if(dt<=0 || !player.alive())return;
        float dashSeconds=(float)Math.min(dt,dash.remaining);
        if(dashSeconds>0) {
            if(trailCount==trailX.length) {System.arraycopy(trailX,1,trailX,0,trailX.length-1);System.arraycopy(trailY,1,trailY,0,trailY.length-1);trailCount--;}
            trailX[trailCount]=player.x;trailY[trailCount++]=player.y;dashTrail=.14f;
        } else {dashTrail=Math.max(0,dashTrail-dt);if(dashTrail==0)trailCount=0;}
        float normalSpeedBoost=Math.max(0,Math.min(dt,speedBoostSeconds)-dashSeconds);
        player.update(dt,axisX,axisY,grid,dashSeconds,dashX*dashActivation.distance/dashActivation.duration*dashSeconds,
            dashY*dashActivation.distance/dashActivation.duration*dashSeconds,cooldownDelta,normalSpeedBoost);
        dash.cooldown=tick(dash.cooldown,cooldownDelta);dash.remaining=tick(dash.remaining,dt);
        ultimate.cooldown=tick(ultimate.cooldown,cooldownDelta);ultimateFade=Math.max(0,ultimateFade-dt);
        if(ultimate.remaining>0) {ultimate.remaining=tick(ultimate.remaining,dt);if(ultimate.remaining==0)ultimate.pendingImpact=true;}
    }
    /** Exactly one target scan when the cast releases. Damage hook owns kills/drops/Boss guards. */
    public void resolve(Player player,EnemySystem enemies,CollisionGrid grid,ProjectileSystem effects) {
        if(!ultimate.pendingImpact || !player.alive() || impactedId==ultimate.activationId)return;
        ultimate.pendingImpact=false;impactedId=ultimate.activationId;ultimateImpacts++;ultimateFade=.28f;
        audio.play(AudioEvent.SKILL_ULTIMATE_IMPACT);int fx=0;
        for(int i=0;i<enemies.size();i++) {
            var enemy=enemies.get(i);if(!enemy.alive())continue;
            float dx=enemy.x-ultimateX,dy=enemy.y-ultimateY;
            if(dx*dx+dy*dy>ultimateActivation.radius*ultimateActivation.radius || !grid.clearLine(ultimateX,ultimateY,enemy.x,enemy.y,.5f,.5f,true))continue;
            int before=enemy.hp();boolean killed=enemy.damage(ultimateDamage);if(enemy.hp()==before)continue;
            ultimateHits++;if(killed)ultimateKills++;
            if(enemy.boss)ultimateBossHits++;
            if(effects!=null && fx++<ultimateActivation.hitFxCap)effects.effect(ProjectileSystem.EffectType.HIT,enemy.x,enemy.y,0,0,4);
            if(!enemy.boss && enemy.alive()) {
                float distance=ultimateActivation.knockback*(enemy.definition.archetype==EnemyArchetype.TANK?ultimateActivation.tankKnockbackScale:1);
                if(enemies.knockback(enemy,dx,dy,distance))knockbacks++;
            }
        }
    }
    public void cancel() {dash.remaining=ultimate.remaining=0;ultimate.pendingImpact=false;ultimateFade=dashTrail=0;trailCount=0;}
    public void reset() {
        cancel();dash.level=ultimate.level=1;dash.tuning=config.dash;ultimate.tuning=config.ultimate;dash.reset();ultimate.reset();
        dashActivation=config.dash;ultimateActivation=config.ultimate;dashActivationLevel=ultimateActivationLevel=1;
        impactedId=0;ultimateImpacts=ultimateHits=ultimateKills=ultimateBossHits=knockbacks=0;
    }
}
