package com.example.monstersurvivor.config;

public enum Difficulty { EASY, HARD;
    public static Difficulty parse(String value) { return "HARD".equalsIgnoreCase(value) ? HARD : EASY; }
}
