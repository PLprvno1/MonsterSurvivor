package com.example.monstersurvivor.gameplay;

import com.example.monstersurvivor.config.WorldVisualConfig;

/** Advanced only by the running match. Modal, pause, death and lifecycle do not advance it. */
public final class NightState {
    private final WorldVisualConfig config;
    private float strength;
    private boolean active;
    public NightState(WorldVisualConfig config) {this.config=config;}
    public void update(float realDelta,float progress) {
        active=progress>=config.nightStart;
        if(active)strength=Math.min(1,strength+realDelta/config.nightFadeSeconds);
    }
    public boolean active() {return active;}
    public float strength() {return strength;}
}
