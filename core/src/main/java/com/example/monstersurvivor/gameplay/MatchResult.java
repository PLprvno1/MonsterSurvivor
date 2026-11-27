package com.example.monstersurvivor.gameplay;

import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.localization.GameLanguage;

/** Immutable completed-match event. The UUID remains unique across process restarts. */
public record MatchResult(long sessionId,boolean victory,float survivedSeconds,float progressSeconds,int kills,
                          boolean miniBossDefeated,boolean finalBossDefeated,Difficulty difficulty,GameMode mode,GameLanguage language,
                          String matchId,boolean diagnostic,int weaponEvolutions,int endlessBossKills,int endlessStage,int runCoins) {
    public MatchResult(long sessionId,boolean victory,float survivedSeconds,float progressSeconds,int kills,
                       boolean miniBossDefeated,boolean finalBossDefeated,Difficulty difficulty,GameMode mode,GameLanguage language,
                       String matchId,boolean diagnostic,int weaponEvolutions,int endlessBossKills,int endlessStage) {
        this(sessionId,victory,survivedSeconds,progressSeconds,kills,miniBossDefeated,finalBossDefeated,difficulty,mode,language,
            matchId,diagnostic,weaponEvolutions,endlessBossKills,endlessStage,0);
    }
    public MatchResult(long sessionId,boolean victory,float survivedSeconds,float progressSeconds,int kills,
                       boolean miniBossDefeated,boolean finalBossDefeated,Difficulty difficulty,GameMode mode,GameLanguage language,
                       String matchId,boolean diagnostic,int weaponEvolutions) {
        this(sessionId,victory,survivedSeconds,progressSeconds,kills,miniBossDefeated,finalBossDefeated,difficulty,mode,language,
            matchId,diagnostic,weaponEvolutions,0,0);
    }
    /** Legacy/test snapshots have no official match identity and cannot enter lifetime statistics. */
    public MatchResult(long sessionId,boolean victory,float survivedSeconds,float progressSeconds,int kills,
                       boolean miniBossDefeated,boolean finalBossDefeated,Difficulty difficulty,GameMode mode,GameLanguage language) {
        this(sessionId,victory,survivedSeconds,progressSeconds,kills,miniBossDefeated,finalBossDefeated,
            difficulty,mode,language,null,true,0);
    }
}
