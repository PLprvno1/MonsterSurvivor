package com.example.monstersurvivor.android.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Data contract and persistence manager for game statistics and achievements.
 * Codex can call `recordMatchResult` or update keys when gameplay ends.
 */
public class AchievementManager {

    private static final String PREF_NAME = "monster_survivor_stats";

    public static final String KEY_BEST_SURVIVAL_SECONDS = "best_survival_time_seconds";
    public static final String KEY_TOTAL_KILLS = "total_kills";
    public static final String KEY_TOTAL_VICTORIES = "total_victories";
    public static final String KEY_TOTAL_MATCHES = "total_matches";
    public static final String KEY_BOSS_DEFEATED_COUNT = "boss_defeated_count";

    private static AchievementManager sInstance;
    private final SharedPreferences mPrefs;

    private AchievementManager(Context context) {
        mPrefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AchievementManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AchievementManager(context);
        }
        return sInstance;
    }

    public int getBestSurvivalSeconds() {
        return mPrefs.getInt(KEY_BEST_SURVIVAL_SECONDS, 0);
    }

    public int getTotalKills() {
        return mPrefs.getInt(KEY_TOTAL_KILLS, 0);
    }

    public int getTotalVictories() {
        return mPrefs.getInt(KEY_TOTAL_VICTORIES, 0);
    }

    public int getTotalMatches() {
        return mPrefs.getInt(KEY_TOTAL_MATCHES, 0);
    }

    public int getBossDefeatedCount() {
        return mPrefs.getInt(KEY_BOSS_DEFEATED_COUNT, 0);
    }

    public boolean hasAnyRecordedMatches() {
        return getTotalMatches() > 0;
    }

    /**
     * Formats seconds into MM:SS string.
     */
    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    /**
     * Records a completed match result.
     *
     * @param survivalSeconds seconds survived in the match
     * @param kills           enemies defeated in the match
     * @param isVictory       true if match won (>= 10m and boss defeated)
     * @param bossDefeated    true if Lich Lord was defeated
     */
    public void recordMatchResult(int survivalSeconds, int kills, boolean isVictory, boolean bossDefeated) {
        int currentBest = getBestSurvivalSeconds();
        int newBest = Math.max(currentBest, survivalSeconds);
        int newTotalKills = getTotalKills() + kills;
        int newMatches = getTotalMatches() + 1;
        int newVictories = getTotalVictories() + (isVictory ? 1 : 0);
        int newBossDefeated = getBossDefeatedCount() + (bossDefeated ? 1 : 0);

        mPrefs.edit()
                .putInt(KEY_BEST_SURVIVAL_SECONDS, newBest)
                .putInt(KEY_TOTAL_KILLS, newTotalKills)
                .putInt(KEY_TOTAL_MATCHES, newMatches)
                .putInt(KEY_TOTAL_VICTORIES, newVictories)
                .putInt(KEY_BOSS_DEFEATED_COUNT, newBossDefeated)
                .apply();
    }

    /**
     * Clears stats for testing purposes.
     */
    public void clearStats() {
        mPrefs.edit().clear().apply();
    }
}
