package com.example.monstersurvivor.config;

import com.example.monstersurvivor.boss.BossDefinition;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.localization.GameStrings.Key;
import java.util.List;

/** Central event/combat budgets. All cooldowns, windups and projectile physics use real seconds. */
public final class BossConfig {
    public record Candidate(String species,Key name) {}
    public final float miniProgress=300,finalProgress=600,miniScale=1.65f,demoHp=.80f;
    public final float burstFraction=.20f,burstWindow=.20f,announcementSeconds=2.8f,retrySeconds=.5f;
    public final int actorCap=2,projectileCap=24;
    public final List<BossDefinition> miniPool;
    public final BossDefinition lich;
    public BossConfig() {this(List.of(new Candidate("mc_moss_golem",Key.BOSS_MOSS),new Candidate("mc_living_armor",Key.BOSS_ARMOR),
        new Candidate("mc_cyclops",Key.BOSS_CYCLOPS),new Candidate("mc_crab_knight",Key.BOSS_CRAB),
        new Candidate("mc_dust_devil",Key.BOSS_DUST),new Candidate("mc_flytrap_walker",Key.BOSS_FLYTRAP),
        new Candidate("mc_goblin_bomber",Key.BOSS_GOBLIN),new Candidate("rock_golem",Key.BOSS_ROCK)));}
    public BossConfig(List<Candidate> candidates) {
        if(candidates.isEmpty())throw new IllegalArgumentException("Mini boss pool required");
        miniPool=candidates.stream().map(c->{var d=EnemyRegistry.get(c.species());
            return new BossDefinition(BossDefinition.Kind.MINI,EnemyDefinition.boss("boss_mini_"+d.id,d.name,d.spriteId,d.scale*miniScale,d.feetX,d.feetY,d.attackSeconds,d.hurtSeconds,d.deathSeconds),c.name());}).toList();
        // Actual existing catalog: 64x64, feet(32,56), Attack6/14, Hurt3/10, Death6/9.
        lich=new BossDefinition(BossDefinition.Kind.LICH_LORD,EnemyDefinition.boss("boss_lich_lord","Lich Lord","lich_lord",1,32,56,6f/14,3f/10,6f/9),Key.LICH_LORD);
    }
    public GameConfig.EnemyStats stats(BossDefinition definition,Difficulty difficulty,GameMode mode) {
        boolean hard=difficulty==Difficulty.HARD,finalBoss=definition.kind()==BossDefinition.Kind.LICH_LORD;
        int hp=finalBoss?(hard?280:220):(hard?200:120);
        if(mode==GameMode.DEMO)hp=Math.round(hp*demoHp);
        return new GameConfig.EnemyStats(hp,finalBoss?(hard?3:2):(hard?2:1),finalBoss?(hard?27:24):(hard?32:30));
    }
}
