package com.example.monstersurvivor.config;

import com.example.monstersurvivor.entity.EnemyType;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.localization.*;

/** Immutable match tuning. No Android types or dependency changes. */
public final class GameConfig {
    public static final int ABSOLUTE_ENEMY_LIMIT=60;
    public static final class EnemyStats {
        public final int hp, damage;
        public final float speed;
        public EnemyStats(int hp, int damage, float speed) {
            if(hp<1 || damage<1 || !Float.isFinite(speed) || speed<=0) throw new IllegalArgumentException("Enemy stats");
            this.hp=hp; this.damage=damage; this.speed=speed;
        }
    }
    /** Progress-time stages; only newly spawned enemies receive the stage HP bonus. */
    public static final class Stage {
        public final float startSeconds,growthSeconds,spawnInterval;
        public final int population;
        private final int[] hpBonus;
        public final float strongWeight,eliteWeight;
        public final int strongDamageFloor;
        public Stage(float startSeconds,int population,float growthSeconds,float spawnInterval,int slimeBonus,int batBonus,int boarBonus) {
            this(startSeconds,population,growthSeconds,spawnInterval,slimeBonus,batBonus,boarBonus,1,1,1);
        }
        public Stage(float startSeconds,int population,float growthSeconds,float spawnInterval,int slimeBonus,int batBonus,int boarBonus,
                     float strongWeight,float eliteWeight,int strongDamageFloor) {
            if(!Float.isFinite(startSeconds) || startSeconds<0 || population<0 || !Float.isFinite(growthSeconds) || growthSeconds<=0
                || !Float.isFinite(spawnInterval) || spawnInterval<=0 || slimeBonus<0 || batBonus<0 || boarBonus<0
                || !Float.isFinite(strongWeight) || strongWeight<=0 || !Float.isFinite(eliteWeight) || eliteWeight<=0
                || strongDamageFloor<1 || strongDamageFloor>2)throw new IllegalArgumentException("Stage tuning");
            this.startSeconds=startSeconds;this.population=population;this.growthSeconds=growthSeconds;this.spawnInterval=spawnInterval;
            hpBonus=new int[]{slimeBonus,batBonus,boarBonus};
            this.strongWeight=strongWeight;this.eliteWeight=eliteWeight;this.strongDamageFloor=strongDamageFloor;
        }
    }
    public final Difficulty difficulty;
    public final GameMode mode;
    public final GameLanguage language;
    public final GameStrings strings;
    public final int playerHp, swordDamage, initialEnemies, maxEnemies, pathSearchesPerFrame;
    public final float swordRange, swordCooldown, swordHitTime, swordDetectionRange, swordAnimationDuration, spawnInterval, densityGrowthSeconds;
    public final float enemySpeedMultiplier;
    public final boolean debug;
    public final Long fixedMapSeed;
    public final PickupConfig pickups;
    public final WeaponConfig weapons;
    public final WorldVisualConfig visuals;
    private final EnemyStats[] stats;
    private final Stage[] stages;
    private final EnemyStats[][] stageStats;
    private final EnemyStats[][] rosterStats;
    private final int defaultInitial;
    private GameConfig(Builder b) {
        difficulty=b.difficulty; mode=b.mode; playerHp=b.playerHp; swordDamage=b.swordDamage;
        language=b.language;strings=GameStrings.of(language);
        pickups=b.pickups;
        weapons=b.weapons;
        visuals=b.visuals;
        swordRange=b.swordRange; swordCooldown=b.swordCooldown; swordHitTime=b.swordHitTime;
        swordDetectionRange=b.swordDetectionRange;swordAnimationDuration=b.swordAnimationDuration;
        defaultInitial=difficulty==Difficulty.HARD?8:6;
        enemySpeedMultiplier=difficulty==Difficulty.HARD?1.10f:1;
        initialEnemies=b.initialEnemies<0?defaultInitial:b.initialEnemies;
        maxEnemies=b.maxEnemies<0?(difficulty==Difficulty.HARD?60:40):b.maxEnemies;debug=b.debug;
        pathSearchesPerFrame=b.pathSearchesPerFrame;
        fixedMapSeed=b.fixedMapSeed;
        stats=b.stats.clone();
        boolean hard=difficulty==Difficulty.HARD;
        for(EnemyType type:EnemyType.values())if(stats[type.ordinal()]==null) {
            EnemyDefinition d=EnemyRegistry.legacy(type);stats[type.ordinal()]=new EnemyStats(hard?d.hardHp:d.easyHp,hard?d.hardDamage:d.damage,d.speed);
        }
        stages=b.stages==null?(hard?new Stage[]{
            new Stage(0,8,20,1.35f,0,0,0,1.15f,1,1),new Stage(60,12,15,1.08f,0,0,0,1.3f,1,1),
            new Stage(120,16,14,.88f,1,0,2,1.6f,1.1f,1),new Stage(210,22,12,.8f,1,1,3,1.85f,1.2f,1),
            new Stage(300,30,10,.72f,2,1,4,2.15f,1.3f,2),new Stage(420,43,10,.65f,2,2,5,2.5f,1.4f,2),
            new Stage(540,55,10,.6f,2,2,5,2.7f,1.5f,2)
        }:new Stage[]{
            new Stage(0,6,30,1.8f,0,0,0),new Stage(60,9,20,1.45f,0,0,0,1.05f,1,1),
            new Stage(120,12,18,1.2f,0,0,1,1.2f,1,1),new Stage(210,17,15,1.08f,0,0,2,1.35f,1.05f,1),
            new Stage(300,23,14,.95f,1,1,3,1.55f,1.1f,1),new Stage(420,32,12,.85f,1,1,4,1.85f,1.15f,1),
            new Stage(540,40,12,.8f,1,1,4,2,1.2f,1)
        }):b.stages.clone();
        spawnInterval=stages[0].spawnInterval;densityGrowthSeconds=stages[0].growthSeconds;
        stageStats=new EnemyStats[stages.length][stats.length];
        for(int s=0;s<stages.length;s++)for(int i=0;i<stats.length;i++) {
            EnemyStats base=stats[i];stageStats[s][i]=new EnemyStats(base.hp+stages[s].hpBonus[i],base.damage,base.speed);
        }
        rosterStats=new EnemyStats[stages.length][EnemyRegistry.all().size()];
        for(int s=0;s<stages.length;s++)for(EnemyDefinition d:EnemyRegistry.all()) {
            if(d.legacyType!=null)rosterStats[s][d.index]=stageStats[s][d.legacyType.ordinal()];
            else {
                EnemyStats base=b.rosterOverrides.get(d.id);
                if(base==null)base=new EnemyStats(hard?d.hardHp:d.easyHp,hard?d.hardDamage:d.damage,d.speed);
                int bonus=stages[s].hpBonus[d.archetype==EnemyArchetype.TANK || d.elite?2:0];
                int damage=strong(d)?Math.max(base.damage,stages[s].strongDamageFloor):base.damage;
                rosterStats[s][d.index]=new EnemyStats(base.hp+bonus,damage,base.speed);
            }
        }
    }
    public EnemyStats enemy(EnemyType type) { return stats[type.ordinal()]; }
    public EnemyStats enemy(EnemyType type,float progress) { return stageStats[stageIndex(progress)][type.ordinal()]; }
    public EnemyStats enemy(EnemyDefinition definition,float progress) {return rosterStats[stageIndex(progress)][definition.index];}
    private int stageIndex(float progress) {int i=stages.length-1;while(i>0 && progress<stages[i].startSeconds)i--;return i;}
    public Stage stage(float progress) {return stages[stageIndex(progress)];}
    public int targetPopulation(float progressSeconds) {
        Stage stage=stage(progressSeconds);
        return Math.min(maxEnemies,Math.max(initialEnemies,stage.population+initialEnemies-defaultInitial
            +(int)(Math.max(0,progressSeconds-stage.startSeconds)/stage.growthSeconds)));
    }
    public float spawnInterval(float progressSeconds) {return stage(progressSeconds).spawnInterval;}
    public static boolean strong(EnemyDefinition d) {return d.easyHp>=3 || d.elite || d.archetype==EnemyArchetype.TANK;}
    public float spawnWeightMultiplier(EnemyDefinition d,float progress) {
        Stage s=stage(progress);return (strong(d)?s.strongWeight:1)*(d.elite?s.eliteWeight:1);
    }
    public static GameConfig defaults() { return new Builder().build(); }
    public static GameConfig fromIntent(String difficulty, String mode) {
        return fromIntent(difficulty,mode,null);
    }
    public static GameConfig fromIntent(String difficulty,String mode,String language) {
        return new Builder().difficulty(Difficulty.parse(difficulty)).mode(GameMode.parse(mode)).language(language).build();
    }
    public static final class Builder {
        private Difficulty difficulty=Difficulty.EASY;
        private GameMode mode=GameMode.NORMAL;
        private GameLanguage language=GameLanguage.VI;
        private int playerHp=10, swordDamage=1, initialEnemies=-1, maxEnemies=-1, pathSearchesPerFrame=2;
        // Retain the entire source clip and scale playback to gameplay time without modifying the asset.
        private float swordRange=48, swordCooldown=1f, swordHitTime=.15f, swordDetectionRange=60, swordAnimationDuration=.65f;
        private boolean debug;
        private Long fixedMapSeed;
        private PickupConfig pickups=new PickupConfig();
        private WeaponConfig weapons=new WeaponConfig();
        private WorldVisualConfig visuals=new WorldVisualConfig();
        private final EnemyStats[] stats=new EnemyStats[3];
        private final java.util.Map<String,EnemyStats> rosterOverrides=new java.util.HashMap<>();
        private Stage[] stages;
        public Builder difficulty(Difficulty v) { difficulty=v; return this; }
        public Builder mode(GameMode v) { mode=v; return this; }
        public Builder language(GameLanguage v) {language=v==null?GameLanguage.VI:v;return this;}
        public Builder language(String v) {return language(GameLanguage.parse(v));}
        public Builder playerHp(int v) { playerHp=v; return this; }
        public Builder sword(int damage,float range,float cooldown,float hitTime) {
            swordDamage=damage;swordRange=range;swordCooldown=cooldown;swordHitTime=hitTime;
            swordDetectionRange=Math.max(swordDetectionRange,range);return this;
        }
        public Builder swordAnimation(float detectionRange,float duration) {swordDetectionRange=detectionRange;swordAnimationDuration=duration;return this;}
        public Builder population(int initial,int limit) { initialEnemies=initial;maxEnemies=limit;return this; }
        public Builder pathBudget(int count) { pathSearchesPerFrame=count;return this; }
        public Builder debug(boolean v) { debug=v;return this; }
        public Builder fixedSeed(long seed) {fixedMapSeed=seed;return this;}
        public Builder pickups(PickupConfig tuning) {if(tuning==null)throw new IllegalArgumentException("Pickup tuning required");pickups=tuning;return this;}
        public Builder weapons(WeaponConfig tuning) {if(tuning==null)throw new IllegalArgumentException("Weapon tuning required");weapons=tuning;return this;}
        public Builder visuals(WorldVisualConfig tuning) {if(tuning==null)throw new IllegalArgumentException("Visual tuning required");visuals=tuning;return this;}
        public Builder enemy(EnemyType type,int hp,int damage,float speed) { stats[type.ordinal()]=new EnemyStats(hp,damage,speed);return this; }
        public Builder enemy(EnemyDefinition definition,int hp,int damage,float speed) {
            if(definition.legacyType!=null)return enemy(definition.legacyType,hp,damage,speed);
            rosterOverrides.put(definition.id,new EnemyStats(hp,damage,speed));return this;
        }
        public Builder stages(Stage... value) {stages=value.clone();return this;}
        public GameConfig build() {
            int limit=maxEnemies<0?(difficulty==Difficulty.HARD?60:40):maxEnemies;
            int initial=initialEnemies<0?(difficulty==Difficulty.HARD?8:6):initialEnemies;
            if(difficulty==null || mode==null || playerHp<1 || swordDamage<1 || initialEnemies< -1 || initial>limit
                || maxEnemies< -1 || limit<1 || limit>ABSOLUTE_ENEMY_LIMIT || pathSearchesPerFrame<1 || pathSearchesPerFrame>8
                || !Float.isFinite(swordRange) || swordRange<=0 || !Float.isFinite(swordCooldown) || swordCooldown<=0
                || !Float.isFinite(swordHitTime) || swordHitTime<0 || swordHitTime>=swordAnimationDuration
                || !Float.isFinite(swordDetectionRange) || swordDetectionRange<swordRange
                || !Float.isFinite(swordAnimationDuration) || swordAnimationDuration<=0 || swordAnimationDuration>swordCooldown)
                throw new IllegalArgumentException("Invalid game tuning");
            if(stages!=null) {
                if(stages.length==0 || stages[0]==null || stages[0].startSeconds!=0)throw new IllegalArgumentException("Stages must start at zero");
                for(int i=1;i<stages.length;i++)if(stages[i]==null || stages[i].startSeconds<=stages[i-1].startSeconds)throw new IllegalArgumentException("Ordered stages required");
            }
            return new GameConfig(this);
        }
    }
}
