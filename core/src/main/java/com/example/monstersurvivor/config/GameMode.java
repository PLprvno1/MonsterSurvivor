package com.example.monstersurvivor.config;

public enum GameMode { NORMAL(1), DEMO(600f/90f), ENDLESS(1);
    public final float progressScale;
    GameMode(float progressScale) { this.progressScale=progressScale; }
    public static GameMode parse(String value) { return "ENDLESS".equalsIgnoreCase(value)?ENDLESS:"DEMO".equalsIgnoreCase(value)?DEMO:NORMAL; }
}
