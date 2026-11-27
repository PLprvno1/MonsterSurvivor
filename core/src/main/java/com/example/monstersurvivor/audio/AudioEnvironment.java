package com.example.monstersurvivor.audio;

/** UI-thread publisher, game-thread reader. Snapshots change only on preference/lifecycle events. */
public final class AudioEnvironment {
    public volatile AudioSettings settings=AudioSettings.DEFAULT;
    public volatile boolean focused=true;
    public volatile long focusLossSerial;
    public void focus(boolean value) {
        if(focused && !value)focusLossSerial++;
        focused=value;
    }
}
