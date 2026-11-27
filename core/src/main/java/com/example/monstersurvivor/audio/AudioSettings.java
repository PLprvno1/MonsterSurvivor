package com.example.monstersurvivor.audio;

/** Immutable platform-neutral snapshot. No Context/SharedPreferences dependency in core. */
public final class AudioSettings {
    public static final AudioSettings DEFAULT=new AudioSettings(true,true,80,80);
    public final boolean musicEnabled,sfxEnabled;
    public final float musicVolume,sfxVolume;
    public AudioSettings(boolean music,boolean sfx,int musicVolume,int sfxVolume) {
        musicEnabled=music;sfxEnabled=sfx;
        this.musicVolume=Math.max(0,Math.min(100,musicVolume))/100f;
        this.sfxVolume=Math.max(0,Math.min(100,sfxVolume))/100f;
    }
}
