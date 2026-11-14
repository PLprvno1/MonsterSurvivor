package com.example.monstersurvivor.debug;

import java.util.Arrays;

/** Opt-in instrumentation only. Normal matches have no instance and no nanoTime calls. */
public final class FrameMetrics {
    public enum Part { PLAYER, AI, ASTAR, SEPARATION, COLLISION, ANIMATION, COMBAT, SPAWN, PICKUPS, WORLD_RENDER, HUD, PROJECTILES, NIGHT_RENDER, WEAPON_RENDER }
    private final long[] nanos=new long[Part.values().length];
    public boolean active=true;
    public int separationPairs,occupancyChecks;
    public void reset() {Arrays.fill(nanos,0);separationPairs=occupancyChecks=0;}
    public long start() {return active?System.nanoTime():0;}
    public void finish(Part part,long start) {if(start!=0 && active)add(part,System.nanoTime()-start);}
    public void add(Part part,long value) {if(active)nanos[part.ordinal()]+=Math.max(0,value);}
    public long value(Part part) {return nanos[part.ordinal()];}
}
