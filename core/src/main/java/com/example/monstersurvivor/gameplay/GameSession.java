package com.example.monstersurvivor.gameplay;

import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.spawn.EnemySpawner;
import com.example.monstersurvivor.world.CollisionGrid;
import com.example.monstersurvivor.debug.FrameMetrics;
import static com.example.monstersurvivor.debug.FrameMetrics.Part.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.pickup.*;

/** Simulation owner. Only RUNNING advances the real/progress clocks and gameplay systems. */
public final class GameSession {
    public enum Phase { RUNNING, CHOOSING_WEAPON, DYING, GAME_OVER, ENDED }
    public final GameConfig config;
    public final Player player;
    public final EnemySystem enemies;
    public final SwordCombat combat;
    public final EnemySpawner spawner;
    public final com.example.monstersurvivor.pickup.PickupSystem pickups;
    public final WeaponSystem weapons;
    public final WeaponSelection selection;
    public final NightState night;
    public int kills;
    private boolean rewardCommitted;
    private final CollisionGrid grid;
    private double elapsed,progress;
    private Phase phase=Phase.RUNNING;
    public final FrameMetrics metrics;
    public GameSession(GameConfig config,CollisionGrid grid,float spawnX,float spawnY,GameAssets assets) {
        this(config,grid,spawnX,spawnY,assets,null);
    }
    public GameSession(GameConfig config,CollisionGrid grid,float spawnX,float spawnY,GameAssets assets,FrameMetrics metrics) {
        this(config,grid,spawnX,spawnY,assets,metrics,config.fixedMapSeed==null?System.nanoTime():config.fixedMapSeed);
    }
    public GameSession(GameConfig config,CollisionGrid grid,float spawnX,float spawnY,GameAssets assets,FrameMetrics metrics,long seed) {
        this.config=config;this.grid=grid;
        night=new NightState(config.visuals);
        this.metrics=metrics;grid.metrics=metrics;
        player=new Player(spawnX,spawnY,assets==null?null:assets.sprite("player"),config);
        enemies=new EnemySystem(new NavigationGrid(grid),assets,config);
        enemies.metrics=metrics;
        WeaponLoadout loadout=new WeaponLoadout(config.weapons);
        combat=new SwordCombat(config,loadout.get(WeaponType.SWORD));weapons=new WeaponSystem(config,loadout,combat);
        weapons.metrics=metrics;
        selection=new WeaponSelection(seed,config.strings);spawner=new EnemySpawner(config,enemies,seed^0xE2L);
        pickups=new com.example.monstersurvivor.pickup.PickupSystem(config,enemies.navigation,player,seed);
        pickups.register(PickupType.WEAPON_BOX,this::weaponBox);
        pickups.enableWeaponBoxes(()->phase==Phase.RUNNING && WeaponSelection.hasReward(loadout,player));
        pickups.suspendWhen(()->phase==Phase.CHOOSING_WEAPON);
        enemies.onDeath(enemy->{if(phase!=Phase.ENDED) {kills++;pickups.enemyDied(enemy,player);}});
    }
    public boolean choosingWeapon() {return phase==Phase.CHOOSING_WEAPON;}
    private boolean weaponBox(Pickup item,Player p) {
        if(!p.alive())return false;
        if(selection.pendingId()==item.id)return rewardCommitted;
        if(phase==Phase.RUNNING && selection.open(item.id,weapons.loadout,p))phase=Phase.CHOOSING_WEAPON;
        return false;
    }
    /** Token and pending identity make double taps, stale UI callbacks and repeated commits harmless. */
    public boolean chooseWeapon(int index,long token) {
        if(!choosingWeapon() || !player.alive() || token!=selection.token() || index<0 || index>=selection.size()
            || !pickups.contains(selection.pendingId()))return false;
        WeaponOffer offer=selection.get(index);if(!offer.apply(weapons.loadout,player))return false;
        rewardCommitted=true;
        if(!pickups.collect(selection.pendingId()))throw new IllegalStateException("Pending weapon item lost");
        pickups.announce(PickupType.WEAPON_BOX,offer.title);selection.clear();rewardCommitted=false;phase=Phase.RUNNING;
        player.limitSwordCooldown(weapons.swordCooldown());return true;
    }
    public void end() {phase=Phase.ENDED;selection.clear();rewardCommitted=false;weapons.clearTransient();enemies.clear();}
    public Phase phase() { return phase; }
    public float elapsed() { return (float)elapsed; }
    public float progress() { return (float)progress; }
    public void update(float delta,float axisX,float axisY,boolean paused,float left,float bottom,float right,float top) {
        if(choosingWeapon() && !player.alive()) {selection.clear();phase=Phase.DYING;weapons.clearTransient();}
        if(paused || phase==Phase.GAME_OVER || phase==Phase.ENDED || choosingWeapon() || delta<=0)return;
        delta=Math.min(delta,.1f);
        enemies.beginFrame();
        // Movement already sweeps in <=3-unit substeps. Repeating all AI/LOS/separation
        // when a frame exceeds 33ms caused a load feedback loop on the emulator.
        float dt=delta;
        if(phase==Phase.RUNNING && !player.alive()) {phase=Phase.DYING;weapons.clearTransient();}
        if(phase==Phase.DYING) {
            player.update(dt,0,0,grid);
            if(player.deathFinished())phase=Phase.GAME_OVER;
            return;
        }
        if(phase!=Phase.RUNNING)return;
        elapsed+=dt;progress+=dt*config.mode.progressScale;
        night.update(dt,progress());
        long start=metrics==null?0:metrics.start(),collisionBefore=metrics==null?0:metrics.value(COLLISION);
        player.update(dt,axisX,axisY,grid);
        if(start!=0)metrics.add(PLAYER,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore));
        enemies.update(dt,player);
        // Apply physical pickups before a pending sword hit reads the current match stats.
        start=metrics==null?0:metrics.start();collisionBefore=metrics==null?0:metrics.value(COLLISION);
        pickups.update(dt,progress(),player);
        if(start!=0)metrics.add(PICKUPS,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore));
        if(choosingWeapon())return;
        start=metrics==null?0:metrics.start();collisionBefore=metrics==null?0:metrics.value(COLLISION);
        long projectileBefore=metrics==null?0:metrics.value(PROJECTILES);
        weapons.update(dt,player,enemies,grid);
        combat.contact(player,enemies);
        if(start!=0)metrics.add(COMBAT,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore)-(metrics.value(PROJECTILES)-projectileBefore));
        if(!player.alive()) {phase=Phase.DYING;weapons.clearTransient();return;}
        start=metrics==null?0:metrics.start();collisionBefore=metrics==null?0:metrics.value(COLLISION);
        spawner.update(dt,progress(),player,left,bottom,right,top);
        if(start!=0)metrics.add(SPAWN,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore));
    }
}
