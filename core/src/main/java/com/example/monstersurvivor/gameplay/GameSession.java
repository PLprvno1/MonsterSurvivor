package com.example.monstersurvivor.gameplay;

import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterProfile;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.consumable.PotionSystem;
import com.example.monstersurvivor.consumable.PotionType;
import com.example.monstersurvivor.spawn.EnemySpawner;
import com.example.monstersurvivor.world.CollisionGrid;
import com.example.monstersurvivor.debug.FrameMetrics;
import static com.example.monstersurvivor.debug.FrameMetrics.Part.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.boss.BossSystem;
import com.example.monstersurvivor.boss.BossDefinition;

/** Simulation owner. Only RUNNING advances the real/progress clocks and gameplay systems. */
public final class GameSession {
    private com.example.monstersurvivor.audio.AudioSink audio=com.example.monstersurvivor.audio.AudioSink.NONE;
    public void audio(com.example.monstersurvivor.audio.AudioSink sink) {
        audio=sink;player.audio=sink;enemies.audio(sink);combat.audio=sink;weapons.audio=sink;
        weapons.projectiles.audio=sink;pickups.audio=sink;bosses.audio=sink;
        skills.audio(sink);
    }
    public enum Phase { RUNNING, CHOOSING_SKILL, DYING, GAME_OVER, WINNING, VICTORY, ENDED }
    private static final java.util.concurrent.atomic.AtomicLong IDS=new java.util.concurrent.atomic.AtomicLong();
    public final long id=IDS.incrementAndGet();
    private MatchResult result;
    private final String matchId=java.util.UUID.randomUUID().toString();
    private int weaponEvolutions;
    private int directCoinRewards;
    private boolean normalVictoryCoinsAwarded;
    private boolean diagnosticResult;
    private java.util.function.Consumer<MatchResult> completed=result->{};
    /** Persistence observer only; combat and victory remain owned by the simulation. */
    public void onCompleted(java.util.function.Consumer<MatchResult> observer,boolean diagnostic) {
        completed=observer==null?result->{}:observer;diagnosticResult=diagnostic || config.debug;
    }
    public final BossSystem bosses;
    public final GameConfig config;
    public final CharacterProfile character;
    public final Player player;
    public final EnemySystem enemies;
    public final SwordCombat combat;
    public final EnemySpawner spawner;
    public final com.example.monstersurvivor.pickup.PickupSystem pickups;
    public final WeaponSystem weapons;
    public final com.example.monstersurvivor.skill.TreasureChestSelection chestSelection;
    public final NightState night;
    public final com.example.monstersurvivor.skill.ActiveSkillSystem skills;
    public final com.example.monstersurvivor.skill.ClassSkillSystem classSkills;
    public final PotionSystem potions;
    public boolean activateSkill(com.example.monstersurvivor.skill.SkillType type,float x,float y,boolean paused) {
        return !paused && phase==Phase.RUNNING && player.alive() && skills.activate(type,player,x,y);
    }
    public PotionSystem.Availability potionAvailability(PotionType type) {
        boolean modeEligible=config.mode==com.example.monstersurvivor.config.GameMode.NORMAL || endless();
        boolean realMatch=modeEligible && !config.debug && !diagnosticResult;
        if(!realMatch)return PotionSystem.Availability.DISABLED;
        return potions.availability(type,player,phase==Phase.RUNNING);
    }
    /** Called only after Android confirms the persisted consume transaction. */
    public boolean applyConsumedPotion(PotionType type) {
        return potionAvailability(type)==PotionSystem.Availability.USABLE && potions.activate(type,player,true);
    }
    public int kills;
    private boolean chestRewardCommitted;
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
        this(config,grid,spawnX,spawnY,assets,metrics,seed,CharacterId.WARRIOR.stableId());
    }
    public GameSession(GameConfig config,CollisionGrid grid,float spawnX,float spawnY,GameAssets assets,FrameMetrics metrics,long seed,String characterId) {
        this(config,grid,spawnX,spawnY,assets,metrics,seed,characterId,null);
    }
    public GameSession(GameConfig config,CollisionGrid grid,float spawnX,float spawnY,GameAssets assets,FrameMetrics metrics,long seed,String characterId,int[] potionCounts) {
        this.config=config;this.grid=grid;
        boolean potionMode=config.mode==com.example.monstersurvivor.config.GameMode.NORMAL || config.mode==com.example.monstersurvivor.config.GameMode.ENDLESS;
        potions=new PotionSystem(potionCounts,potionMode && !config.debug);
        character=CharacterProfile.forId(characterId);
        night=new NightState(config.visuals);
        this.metrics=metrics;grid.metrics=metrics;
        player=new Player(spawnX,spawnY,assets==null?null:assets.sprite(character.spriteId),config);
        skills=new com.example.monstersurvivor.skill.ActiveSkillSystem(config.skills);player.skillInvulnerability(skills::dashInvulnerable);
        enemies=new EnemySystem(new NavigationGrid(grid),assets,config);
        classSkills=new com.example.monstersurvivor.skill.ClassSkillSystem(character.id);
        player.directHitInterceptor((amount,sourceX,sourceY)->classSkills.interceptDirectHit(amount,sourceX,sourceY,player,enemies,grid));
        player.directDamageModifier(classSkills::reduceDirectDamage);
        enemies.metrics=metrics;
        bosses=new BossSystem(config,enemies,seed);
        WeaponLoadout loadout=new WeaponLoadout(config.weapons,character.startingWeapon);
        loadout.passiveSlotCount(()->loadout.size()+classSkills.size());
        classSkills.passiveSlotCount(()->loadout.size()+classSkills.size());
        classSkills.evolvedPassiveCount(()->loadout.evolvedCount()+classSkills.evolvedCount());
        combat=new SwordCombat(config,loadout.get(WeaponType.SWORD));weapons=new WeaponSystem(config,loadout,combat);
        weapons.classSkills(classSkills);
        weapons.metrics=metrics;
        spawner=new EnemySpawner(config,enemies,seed^0xE2L);
        chestSelection=new com.example.monstersurvivor.skill.TreasureChestSelection(config.strings,seed^0x7EA5C4E57L);
        pickups=new com.example.monstersurvivor.pickup.PickupSystem(config,enemies.navigation,player,seed);
        pickups.register(PickupType.TREASURE_CHEST,this::treasureChest);
        pickups.enableCoinDrops(this::coinEligible);
        pickups.enableTreasureChests(()->phase==Phase.RUNNING && com.example.monstersurvivor.skill.TreasureChestSelection.hasReward(loadout,classSkills,skills,player));
        pickups.suspendWhen(this::choosingReward);
        enemies.onDeath(enemy->{if(phase==Phase.RUNNING) {
            classSkills.onEnemyDeath(enemy,enemies,grid);
            kills++;int bossReward=bossCoinReward(enemy);bosses.onDeath(enemy);
            if(bossReward>0)addRunCoins(bossReward);
            pickups.enemyDied(enemy,player);
        }});
    }
    public boolean choosingSkill() {return phase==Phase.CHOOSING_SKILL;}
    public boolean choosingChest() {return choosingSkill();}
    public boolean choosingReward() {return choosingChest();}
    public boolean endless(){return config.mode==com.example.monstersurvivor.config.GameMode.ENDLESS;}
    public boolean coinEligible(){return !config.debug && !diagnosticResult
        && (config.mode==com.example.monstersurvivor.config.GameMode.NORMAL || endless());}
    public int runCoins(){return (int)Math.min(Integer.MAX_VALUE,(long)directCoinRewards+pickups.collectedCoins);}
    public CoinSettlement coinSettlement(){return new CoinSettlement(matchId,runCoins(),config.mode,diagnosticResult||config.debug);}
    private void addRunCoins(int amount) {
        if(!coinEligible() || amount<=0)return;
        directCoinRewards=(int)Math.min(Integer.MAX_VALUE,(long)directCoinRewards+amount);
    }
    private int bossCoinReward(com.example.monstersurvivor.entity.Enemy enemy) {
        var mini=bosses.mini();
        if(mini!=null && mini.actor==enemy && !bosses.miniDefeated())
            return mini.definition.kind()==BossDefinition.Kind.ENDLESS?12:10;
        var lich=bosses.lich();
        if(lich!=null && lich.actor==enemy && !bosses.finalDefeated())return 25;
        return 0;
    }
    public int endlessStage(){return com.example.monstersurvivor.config.EndlessRules.stage(progress())+1;}
    private boolean treasureChest(Pickup item,Player p) {
        if(!p.alive())return false;
        if(chestSelection.pendingId()==item.id)return chestRewardCommitted;
        if(phase==Phase.RUNNING && chestSelection.open(item.id,weapons.loadout,p,skills,classSkills)) {
            phase=Phase.CHOOSING_SKILL;audio.play(com.example.monstersurvivor.audio.AudioEvent.PICKUP_WEAPON_OPEN);
        }
        return false;
    }
    public boolean chooseTreasureChest(int index,long token) {
        if(!choosingChest() || !player.alive() || !pickups.contains(chestSelection.pendingId()))return false;
        if(!chestSelection.commit(index,token,weapons.loadout,player,skills,classSkills))return false;
        var offer=chestSelection.get(index);chestRewardCommitted=true;
        if(!pickups.collect(chestSelection.pendingId()))throw new IllegalStateException("Pending treasure chest lost");
        pickups.announce(PickupType.TREASURE_CHEST,offer.title);chestSelection.clear();chestRewardCommitted=false;phase=Phase.RUNNING;
        if(offer.kind==com.example.monstersurvivor.skill.TreasureChestOffer.Kind.EVOLUTION) {
            if(offer.weapon!=null)weaponEvolutions++;
            audio.play(com.example.monstersurvivor.audio.AudioEvent.PICKUP_WEAPON_UPGRADED);
        } else if(offer.weapon!=null)audio.play(switch(offer.weapon.kind) {
            case WEAPON -> offer.weapon.fromLevel==0?com.example.monstersurvivor.audio.AudioEvent.PICKUP_WEAPON_SELECTED:com.example.monstersurvivor.audio.AudioEvent.PICKUP_WEAPON_UPGRADED;
            case EVOLUTION -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_WEAPON_UPGRADED;
            case DAMAGE -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_DAMAGE;
            case MAX_HP -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_HEALTH;
            case SPEED -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_SPEED;
        });
        if(weapons.loadout.get(WeaponType.SWORD)!=null)player.limitSwordCooldown(weapons.swordCooldown());return true;
    }
    public boolean chooseSkill(int index,long token) {
        return chooseTreasureChest(index,token);
    }
    private void clearSelections() {chestSelection.clear();chestRewardCommitted=false;}
    public void end() {phase=Phase.ENDED;potions.clearEffects();skills.cancel();classSkills.cancel();player.statuses.clear();enemies.clearPoisonStatuses();clearSelections();weapons.clearTransient();bosses.clear();enemies.clear();}
    public boolean victory() {return phase==Phase.VICTORY;}
    public MatchResult result() {return result;}
    private void dying() {phase=Phase.DYING;potions.clearEffects();skills.cancel();classSkills.cancel();player.statuses.clear();enemies.clearPoisonStatuses();enemies.skills.clear();clearSelections();weapons.clearTransient();bosses.stopCombat();}
    private void finish(boolean won) {
        potions.clearEffects();
        if(won && config.mode==com.example.monstersurvivor.config.GameMode.NORMAL && !normalVictoryCoinsAwarded) {
            normalVictoryCoinsAwarded=true;addRunCoins(15);
        }
        phase=won?Phase.VICTORY:Phase.GAME_OVER;
        audio.play(won?com.example.monstersurvivor.audio.AudioEvent.UI_VICTORY:com.example.monstersurvivor.audio.AudioEvent.UI_GAME_OVER);
        result=new MatchResult(id,won,elapsed(),progress(),kills,bosses.miniDefeated(),bosses.finalDefeated(),config.difficulty,config.mode,config.language,
            matchId,diagnosticResult || config.debug,weaponEvolutions,bosses.endlessKills(),endless()?endlessStage():0,runCoins());
        completed.accept(result);
    }
    public Phase phase() { return phase; }
    public float elapsed() { return (float)elapsed; }
    public float progress() { return (float)progress; }
    /** Explicit local diagnostic only; never used by normal play or native Intent extras. */
    public void diagnosticProgress(float value) {
        if(!config.debug || phase!=Phase.RUNNING || !Float.isFinite(value) || value<progress || value>1200)throw new IllegalStateException("Diagnostic clock");
        progress=value;
    }
    public void update(float delta,float axisX,float axisY,boolean paused,float left,float bottom,float right,float top) {
        if(choosingReward() && !player.alive())dying();
        if(paused || phase==Phase.GAME_OVER || phase==Phase.VICTORY || phase==Phase.ENDED || choosingReward() || delta<=0)return;
        delta=Math.min(delta,.1f);
        enemies.beginFrame();
        // Movement already sweeps in <=3-unit substeps. Repeating all AI/LOS/separation
        // when a frame exceeds 33ms caused a load feedback loop on the emulator.
        float dt=delta;
        if(phase==Phase.WINNING) {
            enemies.advanceDeaths(dt);
            if(bosses.lich().actor.deathFinished())finish(true);
            return;
        }
        if(phase==Phase.RUNNING && !player.alive())dying();
        if(phase==Phase.DYING) {
            player.update(dt,0,0,grid);
            if(player.deathFinished())finish(false);
            return;
        }
        if(phase!=Phase.RUNNING)return;
        elapsed+=dt;progress+=dt*config.mode.progressScale;
        night.update(dt,progress());
        long start=metrics==null?0:metrics.start(),collisionBefore=metrics==null?0:metrics.value(COLLISION);
        float speedBoostSeconds=potions.speedBoostSeconds(dt),cooldownDelta=potions.cooldownStep(dt);
        potions.advance(dt);
        skills.advance(dt,axisX,axisY,player,grid,cooldownDelta,speedBoostSeconds);classSkills.advance(dt,cooldownDelta);
        player.statuses.update(dt,player);
        if(start!=0)metrics.add(PLAYER,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore));
        if(!player.alive()){dying();return;}
        bosses.prepare(dt,progress(),player,left,bottom,right,top);
        start=metrics==null?0:metrics.start();
        enemies.skills.prepare(dt,player,progress(),left,bottom,right,top);
        if(metrics!=null)metrics.finish(ENEMY_SKILLS,start);
        enemies.update(dt,player);
        classSkills.resolve(dt,player,enemies,grid);
        // Apply physical pickups before a pending sword hit reads the current match stats.
        start=metrics==null?0:metrics.start();collisionBefore=metrics==null?0:metrics.value(COLLISION);
        pickups.update(dt,progress(),player);
        if(start!=0)metrics.add(PICKUPS,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore));
        if(choosingReward())return;
        start=metrics==null?0:metrics.start();collisionBefore=metrics==null?0:metrics.value(COLLISION);
        long projectileBefore=metrics==null?0:metrics.value(PROJECTILES);
        weapons.update(dt,player,enemies,grid,cooldownDelta);
        if(!bosses.finalDefeated())skills.resolve(player,enemies,grid,weapons.projectiles);
        if(!bosses.finalDefeated()){
            long enemyStart=metrics==null?0:metrics.start();enemies.skills.resolve(dt,player);
            if(metrics!=null)metrics.finish(ENEMY_PROJECTILES,enemyStart);
        }
        // A completed final kill freezes combat before any surviving enemy can hit.
        if(!bosses.finalDefeated())combat.contact(player,enemies);
        if(start!=0)metrics.add(COMBAT,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore)-(metrics.value(PROJECTILES)-projectileBefore));
        if(!player.alive()) {dying();return;}
        if(!endless() && bosses.finalDefeated() && progress()>=config.bosses.finalProgress) {
            phase=Phase.WINNING;potions.clearEffects();skills.cancel();classSkills.cancel();player.statuses.clear();enemies.clearPoisonStatuses();enemies.skills.clear();clearSelections();weapons.clearTransient();bosses.stopCombat();return;
        }
        bosses.fight(dt,progress(),player);
        if(!player.alive()) {dying();return;}
        start=metrics==null?0:metrics.start();collisionBefore=metrics==null?0:metrics.value(COLLISION);
        spawner.update(dt,progress(),player,left,bottom,right,top);
        if(start!=0)metrics.add(SPAWN,System.nanoTime()-start-(metrics.value(COLLISION)-collisionBefore));
    }
}
