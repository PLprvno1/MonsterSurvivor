package com.example.monstersurvivor.android;

import android.os.Bundle;
import android.content.Intent;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.monstersurvivor.MonsterSurvivorGame;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.android.model.GamePreferences;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.
        GameConfig gameConfig = GameConfig.fromIntent(
            getIntent().getStringExtra(GameModeActivity.EXTRA_DIFFICULTY),
            getIntent().getStringExtra(GameModeActivity.EXTRA_GAME_MODE),
            GamePreferences.getInstance(this).getGameLanguage());
        initialize(new MonsterSurvivorGame(gameConfig, () -> runOnUiThread(() -> {
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            finish();
        })), configuration);
    }
}
