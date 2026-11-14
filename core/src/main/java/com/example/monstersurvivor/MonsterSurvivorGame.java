package com.example.monstersurvivor;

import com.badlogic.gdx.Game;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.config.GameConfig;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MonsterSurvivorGame extends Game {
    private final GameConfig config;
    private final Runnable home;
    public MonsterSurvivorGame() { this(GameConfig.defaults(), () -> com.badlogic.gdx.Gdx.app.exit()); }
    public MonsterSurvivorGame(GameConfig config,Runnable home) { this.config=config;this.home=home; }
    @Override
    public void create() {
        setScreen(new GameScreen(config,home));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
    }
}
