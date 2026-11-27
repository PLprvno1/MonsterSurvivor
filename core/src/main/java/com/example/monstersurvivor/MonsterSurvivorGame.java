package com.example.monstersurvivor;

import com.badlogic.gdx.Game;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterOwnership;
import com.example.monstersurvivor.gameplay.CoinSettlement;
import com.example.monstersurvivor.consumable.PotionUseBridge;
import com.example.monstersurvivor.consumable.PotionType;
import java.util.Set;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MonsterSurvivorGame extends Game {
    private final GameConfig config;
    private final Runnable home;
    private final com.example.monstersurvivor.audio.AudioEnvironment audio;
    private final com.example.monstersurvivor.debug.DiagnosticRun diagnostics;
    private final java.util.function.Consumer<com.example.monstersurvivor.gameplay.MatchResult> completed;
    private final String characterId;
    private final Set<String> ownedCharacterIds;
    private final java.util.function.Consumer<CoinSettlement> coinSettlements;
    private final int[] potionCounts;
    private final PotionUseBridge potionBridge;
    public MonsterSurvivorGame() { this(GameConfig.defaults(), () -> com.badlogic.gdx.Gdx.app.exit()); }
    public MonsterSurvivorGame(GameConfig config,Runnable home) {this(config,home,new com.example.monstersurvivor.audio.AudioEnvironment());}
    public MonsterSurvivorGame(GameConfig config,Runnable home,com.example.monstersurvivor.audio.AudioEnvironment audio) {this(config,home,audio,com.example.monstersurvivor.debug.DiagnosticRun.NONE);}
    public MonsterSurvivorGame(GameConfig config,Runnable home,com.example.monstersurvivor.audio.AudioEnvironment audio,com.example.monstersurvivor.debug.DiagnosticRun diagnostics) {this(config,home,audio,diagnostics,result->{});}
    public MonsterSurvivorGame(GameConfig config,Runnable home,com.example.monstersurvivor.audio.AudioEnvironment audio,com.example.monstersurvivor.debug.DiagnosticRun diagnostics,
                              java.util.function.Consumer<com.example.monstersurvivor.gameplay.MatchResult> completed) {
        this(config,home,audio,diagnostics,completed,CharacterId.WARRIOR.stableId());
    }
    public MonsterSurvivorGame(GameConfig config,Runnable home,com.example.monstersurvivor.audio.AudioEnvironment audio,com.example.monstersurvivor.debug.DiagnosticRun diagnostics,
                              java.util.function.Consumer<com.example.monstersurvivor.gameplay.MatchResult> completed,String characterId) {
        this(config,home,audio,diagnostics,completed,characterId,Set.of(CharacterId.WARRIOR.stableId()),settlement->{});
    }
    public MonsterSurvivorGame(GameConfig config,Runnable home,com.example.monstersurvivor.audio.AudioEnvironment audio,com.example.monstersurvivor.debug.DiagnosticRun diagnostics,
                              java.util.function.Consumer<com.example.monstersurvivor.gameplay.MatchResult> completed,String characterId,
                              Set<String> ownedCharacterIds,java.util.function.Consumer<CoinSettlement> coinSettlements) {
        this(config,home,audio,diagnostics,completed,characterId,ownedCharacterIds,coinSettlements,new int[PotionType.values().length],PotionUseBridge.NONE);
    }
    public MonsterSurvivorGame(GameConfig config,Runnable home,com.example.monstersurvivor.audio.AudioEnvironment audio,com.example.monstersurvivor.debug.DiagnosticRun diagnostics,
                              java.util.function.Consumer<com.example.monstersurvivor.gameplay.MatchResult> completed,String characterId,
                              Set<String> ownedCharacterIds,java.util.function.Consumer<CoinSettlement> coinSettlements,
                              int[] potionCounts,PotionUseBridge potionBridge) {
        this.config=config;this.home=home;this.audio=audio;this.diagnostics=diagnostics;this.completed=completed;
        this.ownedCharacterIds=ownedCharacterIds==null?Set.of(CharacterId.WARRIOR.stableId()):Set.copyOf(ownedCharacterIds);
        this.characterId=CharacterOwnership.selectableOrWarrior(characterId,this.ownedCharacterIds).stableId();
        this.coinSettlements=coinSettlements==null?settlement->{}:coinSettlements;
        this.potionCounts=potionCounts==null?new int[PotionType.values().length]:potionCounts.clone();
        this.potionBridge=potionBridge==null?PotionUseBridge.NONE:potionBridge;
    }
    @Override
    public void create() {
        GameScreen gameplay=new GameScreen(config,home,audio,diagnostics,characterId,ownedCharacterIds,coinSettlements,potionCounts,potionBridge);
        gameplay.onMatchCompleted(completed);setScreen(gameplay);
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
