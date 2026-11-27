package com.example.monstersurvivor.android;

import android.os.Bundle;
import android.content.Intent;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.monstersurvivor.MonsterSurvivorGame;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.android.model.GamePreferences;
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterOwnership;
import com.example.monstersurvivor.android.model.CharacterProgressManager;
import com.example.monstersurvivor.consumable.PotionUseBridge;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    /** Normal Home/GameMode launches ignore all test files and diagnostic Intent extras. */
    protected com.example.monstersurvivor.debug.DiagnosticRun diagnosticsForLaunch() {return com.example.monstersurvivor.debug.DiagnosticRun.NONE;}
    private final com.example.monstersurvivor.audio.AudioEnvironment audioState=new com.example.monstersurvivor.audio.AudioEnvironment();
    private com.example.monstersurvivor.android.audio.AudioFocus audioFocus;
    private final java.util.concurrent.ExecutorService potionIo=java.util.concurrent.Executors.newSingleThreadExecutor();
    private GamePreferences prefs;
    private boolean foreground;
    private final android.content.SharedPreferences.OnSharedPreferenceChangeListener audioPreferences=(stored,key)->{
        if(key!=null && (key.equals(GamePreferences.KEY_MUSIC_ENABLED) || key.equals(GamePreferences.KEY_SFX_ENABLED)
            || key.equals(GamePreferences.KEY_MUSIC_VOLUME) || key.equals(GamePreferences.KEY_SFX_VOLUME)))audioState.settings=prefs.audioSettings();
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.monstersurvivor.android.audio.NativeAudio.enterGame();
        prefs=GamePreferences.getInstance(this);audioState.settings=prefs.audioSettings();audioState.focus(false);prefs.listen(audioPreferences);
        audioFocus=new com.example.monstersurvivor.android.audio.AudioFocus(this,focused->{audioState.focus(foreground && focused);});
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.
        configuration.maxSimultaneousSounds=16;
        GameConfig gameConfig = GameConfig.fromIntent(
            getIntent().getStringExtra(GameModeActivity.EXTRA_DIFFICULTY),
            getIntent().getStringExtra(GameModeActivity.EXTRA_GAME_MODE),
            GamePreferences.getInstance(this).getGameLanguage());
        var diagnosticRun=diagnosticsForLaunch();
        CharacterProgressManager characterProgress=CharacterProgressManager.getInstance(this);
        java.util.Set<String> ownedCharacters=characterProgress.ownedCharacterIds();
        String characterId=CharacterOwnership.selectableOrWarrior(
            getIntent().getStringExtra(GameModeActivity.EXTRA_CHARACTER_ID),ownedCharacters).stableId();
        initialize(new MonsterSurvivorGame(gameConfig, () -> runOnUiThread(() -> {
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            finish();
        }),audioState,diagnosticRun,result->{
            characterProgress.creditRunCoins(result.matchId(),result.runCoins(),result.mode(),result.diagnostic(),"RESULT");
            if(!diagnosticRun.enabled() && !gameConfig.debug) {
                // Synchronous durable transaction on the game thread before Home/Retry can dispose it.
                com.example.monstersurvivor.android.model.AchievementManager.getInstance(this).recordMatchResult(result);
            }
        },characterId,ownedCharacters,settlement->characterProgress.creditRunCoins(
            settlement.matchUuid(),settlement.amount(),settlement.mode(),settlement.diagnostic(),"HOME"),characterProgress.potionCounts(),
            (type,completion)->{
                try {potionIo.execute(()->completion.accept(characterProgress.consumePotion(type)));}
                catch(java.util.concurrent.RejectedExecutionException stopped){completion.accept(false);}
            }), configuration);
    }
    @Override protected void onResume() {
        foreground=true;if(audioFocus!=null)audioState.focus(audioFocus.acquire());super.onResume();
    }
    @Override protected void onPause() {
        foreground=false;audioState.focus(false);
        super.onPause();if(audioFocus!=null)audioFocus.release();
    }
    @Override protected void onDestroy() {
        foreground=false;audioState.focus(false);
        if(audioFocus!=null)audioFocus.release();if(prefs!=null)prefs.unlisten(audioPreferences);potionIo.shutdown();super.onDestroy();
    }
}
