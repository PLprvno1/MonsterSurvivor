package com.example.monstersurvivor.config;

import com.example.monstersurvivor.boss.BossDefinition;
import com.example.monstersurvivor.enemy.EnemyDefinition;
import com.example.monstersurvivor.enemy.EnemyRegistry;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Endless-only growth; finite stat ceilings, unchanged resident/projectile budgets. */
public final class EndlessRules {
    public static final int BOSS_INTERVAL=300,MAX_ENEMY_HP=50_000,MAX_BOSS_HP=1_000_000;
    private static final BossConfig.Candidate[] BOSSES={
        new BossConfig.Candidate("mc_moss_golem",BOSS_MOSS),
        new BossConfig.Candidate("mc_living_armor",BOSS_ARMOR),
        new BossConfig.Candidate("mc_cyclops",BOSS_CYCLOPS),
        new BossConfig.Candidate("mc_crab_knight",BOSS_CRAB),
        new BossConfig.Candidate("rock_golem",BOSS_ROCK)
    };
    private EndlessRules() { }
    /** Zero-based growth stage. Saturating conversion also handles unrealistic diagnostic inputs. */
    public static int stage(float seconds) {
        if(Float.isNaN(seconds) || seconds<=0)return 0;
        return (int)Math.min(Integer.MAX_VALUE-1,Math.floor((double)seconds/BOSS_INTERVAL));
    }
    private static int hp(int base,double factor,int cap) {
        return (int)Math.max(1,Math.min(cap,Math.round(base*factor)));
    }
    public static GameConfig.EnemyStats enemy(GameConfig.EnemyStats base,float seconds,Difficulty difficulty) {
        int stage=stage(seconds);
        if(stage==0)return base;
        int hp=hp(base.hp,Math.pow(1.15,Math.min(stage,64)),MAX_ENEMY_HP);
        int damage=(int)Math.min(8,(long)base.damage+stage/(difficulty==Difficulty.HARD?3:4));
        return new GameConfig.EnemyStats(hp,damage,base.speed);
    }
    public static int bossCandidates(){return BOSSES.length;}
    public static BossDefinition boss(int candidate,int wave) {
        var c=BOSSES[candidate];var species=EnemyRegistry.get(c.species());
        float scale=1.65f+Math.min(.30f,Math.max(0L,(long)wave-1)*.03f);
        var body=EnemyDefinition.boss("boss_endless_"+wave+"_"+species.id,species.name,species.spriteId,
            species.scale*scale,species.feetX,species.feetY,species.attackSeconds,species.hurtSeconds,species.deathSeconds);
        return new BossDefinition(BossDefinition.Kind.ENDLESS,body,c.name());
    }
    public static GameConfig.EnemyStats bossStats(Difficulty difficulty,int wave) {
        boolean hard=difficulty==Difficulty.HARD;long growth=Math.max(0L,(long)wave-1);
        int hp=hp(hard?200:120,Math.pow(1.25,Math.min(growth,64)),MAX_BOSS_HP);
        int damage=(int)Math.min(16,(hard?3:2)+growth/(hard?2:3));
        return new GameConfig.EnemyStats(hp,damage,hard?32:30);
    }
}
