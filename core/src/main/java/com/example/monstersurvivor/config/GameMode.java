package com.example.monstersurvivor.config;

public enum GameMode { NORMAL(1), DEMO(600f/90f);
    public final float progressScale;
    GameMode(float progressScale) { this.progressScale=progressScale; }
    public static GameMode parse(String value) { return "DEMO".equalsIgnoreCase(value) ? DEMO : NORMAL; }
}
