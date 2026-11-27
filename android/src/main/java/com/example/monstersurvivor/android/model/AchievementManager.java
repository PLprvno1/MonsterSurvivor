package com.example.monstersurvivor.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.monstersurvivor.R;
import com.example.monstersurvivor.config.Difficulty;
import com.example.monstersurvivor.config.GameMode;
import com.example.monstersurvivor.gameplay.MatchResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/** Official completed Normal matches only. Settings use a separate preference file. */
public final class AchievementManager {
    private static final String PREF_NAME="monster_survivor_stats";
    // Previous UI placeholders had no verified mode/source. Never import them as official results.
    private static final String PREFIX="official_v1.";
    public static final String KEY_BEST_SURVIVAL_SECONDS=PREFIX+"best_survival_seconds";
    public static final String KEY_TOTAL_KILLS=PREFIX+"total_kills";
    public static final String KEY_TOTAL_VICTORIES=PREFIX+"total_victories";
    public static final String KEY_TOTAL_MATCHES=PREFIX+"total_matches";
    public static final String KEY_BOSS_DEFEATED_COUNT=PREFIX+"lich_kills";
    private static final String MINI=PREFIX+"mini_kills",HARD=PREFIX+"hard_wins",EVOLUTIONS=PREFIX+"evolutions";
    private static final String PLAY_MS=PREFIX+"play_ms",RECORDED=PREFIX+"recorded_match_ids";
    private static AchievementManager sInstance;
    private final SharedPreferences mPrefs;
    private AchievementManager(Context context) {
        this(context.getApplicationContext().getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE));
    }
    AchievementManager(SharedPreferences preferences) {mPrefs=preferences;}
    public static synchronized AchievementManager getInstance(Context context) {
        if(sInstance==null)sInstance=new AchievementManager(context);
        return sInstance;
    }
    private long value(String key) {return mPrefs.getLong(key,0);}
    public int getBestSurvivalSeconds() {return (int)value(KEY_BEST_SURVIVAL_SECONDS);}
    public long getTotalKills() {return value(KEY_TOTAL_KILLS);}
    public long getTotalVictories() {return value(KEY_TOTAL_VICTORIES);}
    public long getTotalMatches() {return value(KEY_TOTAL_MATCHES);}
    public long getBossDefeatedCount() {return value(KEY_BOSS_DEFEATED_COUNT);}
    public long getMiniBossDefeatedCount() {return value(MINI);}
    public long getHardVictories() {return value(HARD);}
    public long getWeaponEvolutions() {return value(EVOLUTIONS);}
    public long getTotalPlaySeconds() {return value(PLAY_MS)/1000;}
    public boolean hasAnyRecordedMatches() {return getTotalMatches()>0;}

    public static final class AchievementItem {
        public final String id;
        public final int titleResId,descResId,iconResId;
        public final boolean isUnlocked;
        public final long currentProgress,targetProgress,unlockedAtMillis;
        public final String progressText;
        private AchievementItem(String id,int title,int desc,int icon,long current,long target,long achieved) {
            this.id=id;titleResId=title;descResId=desc;iconResId=icon;
            currentProgress=Math.min(current,target);targetProgress=target;
            isUnlocked=achieved>0;unlockedAtMillis=achieved;
            progressText=(id.equals("normal_survivor")?formatTime(currentProgress)+" / 05:00":currentProgress+" / "+targetProgress);
        }
    }
    private void add(List<AchievementItem> list,String id,int title,int desc,int icon,long current,long target) {
        list.add(new AchievementItem(id,title,desc,icon,current,target,value(PREFIX+"unlocked."+id)));
    }
    private List<AchievementItem> catalog(long matches,long kills,long best,long mini,long victories,long hard,long evolutions) {
        List<AchievementItem> list=new ArrayList<>(8);
        add(list,"first_match",R.string.ach_item_first_blood_title,R.string.ach_item_first_blood_desc,R.drawable.ic_sword,matches,1);
        add(list,"normal_survivor",R.string.ach_item_novice_survivor_title,R.string.ach_item_novice_survivor_desc,R.drawable.ic_clock,best,300);
        add(list,"monster_slayer",R.string.ach_item_monster_slayer_title,R.string.ach_item_monster_slayer_desc,R.drawable.ic_skull,kills,100);
        add(list,"thousand_kills",R.string.ach_item_centurion_slayer_title,R.string.ach_item_centurion_slayer_desc,R.drawable.ic_sparkles,kills,1000);
        add(list,"mini_boss_hunter",R.string.ach_item_lich_bane_title,R.string.ach_item_lich_bane_desc,R.drawable.ic_shield,mini,1);
        add(list,"legendary_victor",R.string.ach_item_legendary_victor_title,R.string.ach_item_legendary_victor_desc,R.drawable.ic_crown,victories,1);
        add(list,"hard_victor",R.string.ach_item_hard_title,R.string.ach_item_hard_desc,R.drawable.ic_shield,hard,1);
        add(list,"weapon_master",R.string.ach_item_evolution_title,R.string.ach_item_evolution_desc,R.drawable.ic_sparkles,evolutions,1);
        return list;
    }
    public synchronized List<AchievementItem> getAchievements() {
        return catalog(getTotalMatches(),getTotalKills(),getBestSurvivalSeconds(),getMiniBossDefeatedCount(),getTotalVictories(),getHardVictories(),getWeaponEvolutions());
    }
    public int getUnlockedAchievementsCount() {int n=0;for(var item:getAchievements())if(item.isUnlocked)n++;return n;}
    public int getTotalAchievementsCount() {return 8;}
    public static String formatTime(long totalSeconds) {
        return String.format(Locale.getDefault(),"%02d:%02d",totalSeconds/60,totalSeconds%60);
    }

    /** One atomic disk commit includes counters, unlock dates and the durable idempotency ledger. */
    public synchronized boolean recordMatchResult(MatchResult result) {
        if(result==null || result.mode()!=GameMode.NORMAL || result.diagnostic() || result.difficulty()==null
            || result.matchId()==null || !result.matchId().matches("[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}")
            || !Float.isFinite(result.survivedSeconds()) || result.survivedSeconds()<0
            || !Float.isFinite(result.progressSeconds()) || result.progressSeconds()<0
            || result.kills()<0 || result.weaponEvolutions()<0 || result.weaponEvolutions()>4
            || (result.victory() && (!result.finalBossDefeated() || result.progressSeconds()<600)))return false;
        var recorded=new HashSet<>(mPrefs.getStringSet(RECORDED,java.util.Collections.emptySet()));
        if(!recorded.add(result.matchId()))return false;
        long matches=getTotalMatches()+1,kills=getTotalKills()+result.kills();
        long best=Math.max(getBestSurvivalSeconds(),(int)Math.min(Integer.MAX_VALUE,result.survivedSeconds()));
        long mini=getMiniBossDefeatedCount()+(result.miniBossDefeated()?1:0);
        long lich=getBossDefeatedCount()+(result.finalBossDefeated()?1:0);
        long victories=getTotalVictories()+(result.victory()?1:0);
        long hard=getHardVictories()+(result.victory() && result.difficulty()==Difficulty.HARD?1:0);
        long evolutions=getWeaponEvolutions()+result.weaponEvolutions();
        long play=value(PLAY_MS)+(long)(result.survivedSeconds()*1000d);
        var editor=mPrefs.edit().putLong(KEY_TOTAL_MATCHES,matches).putLong(KEY_TOTAL_KILLS,kills)
            .putLong(KEY_BEST_SURVIVAL_SECONDS,best).putLong(MINI,mini).putLong(KEY_BOSS_DEFEATED_COUNT,lich)
            .putLong(KEY_TOTAL_VICTORIES,victories).putLong(HARD,hard).putLong(EVOLUTIONS,evolutions)
            .putLong(PLAY_MS,play).putStringSet(RECORDED,recorded);
        long now=System.currentTimeMillis();
        for(var item:catalog(matches,kills,best,mini,victories,hard,evolutions)) {
            if(!item.isUnlocked && item.currentProgress>=item.targetProgress)editor.putLong(PREFIX+"unlocked."+item.id,now);
        }
        boolean saved=editor.commit();
        if(!saved)Log.e("Achievements","Could not persist completed match "+result.matchId());
        return saved;
    }
}
