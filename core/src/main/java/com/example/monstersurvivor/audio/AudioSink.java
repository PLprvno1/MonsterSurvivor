package com.example.monstersurvivor.audio;

/** Simulation emits committed actions; headless checks and disabled audio need no backend. */
@FunctionalInterface
public interface AudioSink {
    AudioSink NONE = event -> {};
    void play(AudioEvent event);
    default void at(AudioEvent event,float x,float y,int actor) {play(event);}
}
