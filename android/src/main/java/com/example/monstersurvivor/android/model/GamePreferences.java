package com.example.monstersurvivor.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.monstersurvivor.localization.GameLanguage;

/**
 * Manages persistent user preferences (Audio, Vibration, Theme) via SharedPreferences.
 */
public class GamePreferences {

    private static final String PREF_NAME = "monster_survivor_prefs";

    public static final String KEY_MUSIC_ENABLED = "pref_music_enabled";
    public static final String KEY_SFX_ENABLED = "pref_sfx_enabled";
    public static final String KEY_MUSIC_VOLUME = "pref_music_volume";
    public static final String KEY_SFX_VOLUME = "pref_sfx_volume";
    public static final String KEY_VIBRATION_ENABLED = "pref_vibration_enabled";
    public static final String KEY_THEME = "pref_theme";
    public static final String KEY_GAME_LANGUAGE = "game_language";

    public static final String THEME_FOREST = "forest";
    public static final String THEME_DARK = "dark";

    private static GamePreferences sInstance;
    private final SharedPreferences mPrefs;

    private GamePreferences(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized GamePreferences getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GamePreferences(context);
        }
        return sInstance;
    }

    public boolean isMusicEnabled() {
        return mPrefs.getBoolean(KEY_MUSIC_ENABLED, true);
    }

    public void setMusicEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();
    }

    public boolean isSfxEnabled() {
        return mPrefs.getBoolean(KEY_SFX_ENABLED, true);
    }

    public void setSfxEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_SFX_ENABLED, enabled).apply();
    }

    public int getMusicVolume() {
        return mPrefs.getInt(KEY_MUSIC_VOLUME, 80);
    }

    public void setMusicVolume(int volume) {
        mPrefs.edit().putInt(KEY_MUSIC_VOLUME, Math.max(0, Math.min(100, volume))).apply();
    }

    public int getSfxVolume() {
        return mPrefs.getInt(KEY_SFX_VOLUME, 80);
    }

    public void setSfxVolume(int volume) {
        mPrefs.edit().putInt(KEY_SFX_VOLUME, Math.max(0, Math.min(100, volume))).apply();
    }

    public boolean isVibrationEnabled() {
        return mPrefs.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    public String getTheme() {
        return mPrefs.getString(KEY_THEME, THEME_FOREST);
    }

    public void setTheme(String theme) {
        mPrefs.edit().putString(KEY_THEME, theme).apply();
    }

    /** UI02 language selector must reuse this key in monster_survivor_prefs. */
    public String getGameLanguage() {
        try {return GameLanguage.parse(mPrefs.getString(KEY_GAME_LANGUAGE,"vi")).code;}
        catch(ClassCastException invalidStoredType) {return "vi";}
    }
    public void setGameLanguage(String code) {
        mPrefs.edit().putString(KEY_GAME_LANGUAGE,GameLanguage.parse(code).code).apply();
    }

    public void resetDefaults() {
        mPrefs.edit()
                .putBoolean(KEY_MUSIC_ENABLED, true)
                .putBoolean(KEY_SFX_ENABLED, true)
                .putInt(KEY_MUSIC_VOLUME, 80)
                .putInt(KEY_SFX_VOLUME, 80)
                .putBoolean(KEY_VIBRATION_ENABLED, true)
                .putString(KEY_THEME, THEME_FOREST)
                .putString(KEY_GAME_LANGUAGE,"vi")
                .apply();
    }
}
