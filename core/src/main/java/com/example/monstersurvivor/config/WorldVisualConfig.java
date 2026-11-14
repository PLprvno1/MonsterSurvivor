package com.example.monstersurvivor.config;

/** Presentation only: never changes actor size, collisions, speed or simulation clocks. */
public final class WorldVisualConfig {
    public final float zoom;
    public final float nightStart=300,nightFadeSeconds=3,lightRadius=120,darkness=.68f;
    public WorldVisualConfig() {this(1.15f);}
    public WorldVisualConfig(float zoom) {
        if(!Float.isFinite(zoom) || zoom<1 || zoom>1.3f)throw new IllegalArgumentException("World zoom");
        this.zoom=zoom;
    }
}
