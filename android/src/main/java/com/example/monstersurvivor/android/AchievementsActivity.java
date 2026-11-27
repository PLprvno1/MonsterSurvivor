package com.example.monstersurvivor.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.model.AchievementManager;
import com.example.monstersurvivor.android.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Achievements Activity displaying survival statistics and achievements.
 */
public class AchievementsActivity extends NativeAudioActivity {

    private AchievementManager mManager;

    private TextView mTvProgressSummary;
    private ProgressBar mProgressBar;
    private View mPanelEmptyState;

    private static final int[][] ACHIEVEMENT_VIEWS={
        {R.id.card_ach_item_1, R.id.iv_ach_item_1_icon, R.id.tv_ach_item_1_title, R.id.tv_ach_item_1_desc, R.id.tv_ach_item_1_prog, R.id.tv_ach_item_1_badge},
        {R.id.card_ach_item_2, R.id.iv_ach_item_2_icon, R.id.tv_ach_item_2_title, R.id.tv_ach_item_2_desc, R.id.tv_ach_item_2_prog, R.id.tv_ach_item_2_badge},
        {R.id.card_ach_item_3, R.id.iv_ach_item_3_icon, R.id.tv_ach_item_3_title, R.id.tv_ach_item_3_desc, R.id.tv_ach_item_3_prog, R.id.tv_ach_item_3_badge},
        {R.id.card_ach_item_4, R.id.iv_ach_item_4_icon, R.id.tv_ach_item_4_title, R.id.tv_ach_item_4_desc, R.id.tv_ach_item_4_prog, R.id.tv_ach_item_4_badge},
        {R.id.card_ach_item_5, R.id.iv_ach_item_5_icon, R.id.tv_ach_item_5_title, R.id.tv_ach_item_5_desc, R.id.tv_ach_item_5_prog, R.id.tv_ach_item_5_badge},
        {R.id.card_ach_item_6, R.id.iv_ach_item_6_icon, R.id.tv_ach_item_6_title, R.id.tv_ach_item_6_desc, R.id.tv_ach_item_6_prog, R.id.tv_ach_item_6_badge},
        {R.id.card_ach_item_7, R.id.iv_ach_item_7_icon, R.id.tv_ach_item_7_title, R.id.tv_ach_item_7_desc, R.id.tv_ach_item_7_prog, R.id.tv_ach_item_7_badge},
        {R.id.card_ach_item_8, R.id.iv_ach_item_8_icon, R.id.tv_ach_item_8_title, R.id.tv_ach_item_8_desc, R.id.tv_ach_item_8_prog, R.id.tv_ach_item_8_badge}
    };

    private BottomNavigationView mBottomNav;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UiUtils.wrapLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_achievements);

        mManager = AchievementManager.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_achievements);
        toolbar.setNavigationOnClickListener(v -> UiUtils.navigateBack(this));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                UiUtils.navigateBack(AchievementsActivity.this);
            }
        });

        initViews();
        bindData();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.applyImmersiveMode(this);
        if (mBottomNav != null) {
            mBottomNav.setSelectedItemId(R.id.nav_achievements);
        }
        bindData();
    }

    private void initViews() {
        mTvProgressSummary = findViewById(R.id.tv_ach_progress_summary);
        mProgressBar = findViewById(R.id.progress_achievements);
        mPanelEmptyState = findViewById(R.id.panel_ach_empty_state);

    }

    private void bindData() {
        // 1. Progress Summary Header
        int unlocked = mManager.getUnlockedAchievementsCount();
        int total = mManager.getTotalAchievementsCount();
        int percent = total > 0 ? (unlocked * 100 / total) : 0;
        if (mTvProgressSummary != null) {
            mTvProgressSummary.setText(getString(R.string.ach_progress_summary, unlocked, total, percent));
        }
        if (mProgressBar != null) {
            mProgressBar.setMax(total);
            mProgressBar.setProgress(unlocked);
        }

        // 2. Empty State Banner
        if (mPanelEmptyState != null) {
            mPanelEmptyState.setVisibility(mManager.hasAnyRecordedMatches() ? View.GONE : View.VISIBLE);
        }

        // 3. Lifetime Records
        TextView tvBestTime = findViewById(R.id.tv_val_best_time);
        TextView tvKills = findViewById(R.id.tv_val_total_kills);
        TextView tvVictories = findViewById(R.id.tv_val_victories);
        TextView tvMatches = findViewById(R.id.tv_val_matches);
        TextView tvBoss = findViewById(R.id.tv_val_boss);

        int bestSec = mManager.getBestSurvivalSeconds();
        long kills = mManager.getTotalKills();
        long victories = mManager.getTotalVictories();
        long matches = mManager.getTotalMatches();
        long bossDefeated = mManager.getBossDefeatedCount();

        if (tvBestTime != null) tvBestTime.setText(AchievementManager.formatTime(bestSec));
        if (tvKills != null) tvKills.setText(String.valueOf(kills));
        if (tvVictories != null) tvVictories.setText(String.valueOf(victories));
        if (tvMatches != null) tvMatches.setText(String.valueOf(matches));
        if (tvBoss != null) {
            if (bossDefeated > 0) {
                tvBoss.setText(getString(R.string.ach_boss_status_defeated, bossDefeated));
            } else {
                tvBoss.setText(R.string.ach_boss_status_none);
            }
        }

        // 4. Hero Achievements List
        List<AchievementManager.AchievementItem> items = mManager.getAchievements();
        for(int i=0;i<Math.min(items.size(),ACHIEVEMENT_VIEWS.length);i++) {
            int[] ids=ACHIEVEMENT_VIEWS[i];
            bindAchievementCard(findViewById(ids[0]),ids[1],ids[2],ids[3],ids[4],ids[5],items.get(i));
        }
        TextView extra=findViewById(R.id.tv_ach_extra_stats);
        extra.setText(getString(R.string.ach_extra_stats,AchievementManager.formatTime(mManager.getTotalPlaySeconds()),
            mManager.getMiniBossDefeatedCount(),mManager.getHardVictories(),mManager.getWeaponEvolutions()));
    }


    private void bindAchievementCard(View cardView, int iconId, int titleId, int descId, int progId, int badgeId,
                                     AchievementManager.AchievementItem item) {
        if (cardView == null) return;

        cardView.setBackgroundResource(item.isUnlocked
                ? R.drawable.panel_fantasy_card_selected
                : R.drawable.panel_fantasy_card);

        ImageView ivIcon = cardView.findViewById(iconId);
        if (ivIcon != null) {
            ivIcon.setImageResource(item.iconResId);
            ivIcon.setColorFilter(ContextCompat.getColor(this, item.isUnlocked
                    ? R.color.gold_bright
                    : R.color.text_muted));
        }

        TextView tvTitle = cardView.findViewById(titleId);
        if (tvTitle != null) {
            tvTitle.setText(item.titleResId);
            tvTitle.setTextColor(ContextCompat.getColor(this, item.isUnlocked
                    ? R.color.gold_bright
                    : R.color.text_secondary));
        }

        TextView tvDesc = cardView.findViewById(descId);
        if (tvDesc != null) {
            tvDesc.setText(item.descResId);
        }

        TextView tvProg = cardView.findViewById(progId);
        if (tvProg != null) {
            tvProg.setText(item.progressText+(item.isUnlocked?"\n"+unlockDate(item):""));
            tvProg.setTextColor(ContextCompat.getColor(this, item.isUnlocked
                    ? R.color.gold_text
                    : R.color.text_muted));
        }

        TextView tvBadge = cardView.findViewById(badgeId);
        if (tvBadge != null) {
            tvBadge.setText(item.isUnlocked ? R.string.ach_status_unlocked : R.string.ach_status_locked);
            tvBadge.setBackgroundResource(item.isUnlocked
                    ? R.drawable.panel_fantasy_badge_unlocked
                    : R.drawable.panel_fantasy_badge_locked);
            tvBadge.setTextColor(ContextCompat.getColor(this, item.isUnlocked
                    ? R.color.gold_bright
                    : R.color.text_muted));
        }
    }

    private String unlockDate(AchievementManager.AchievementItem item) {
        return getString(R.string.ach_unlocked_date,android.text.format.DateFormat.getDateFormat(this)
            .format(new java.util.Date(item.unlockedAtMillis)));
    }

    private void setupBottomNavigation() {
        mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setSelectedItemId(R.id.nav_achievements);
        mBottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(AchievementsActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                return true;
            } else if (itemId == R.id.nav_achievements) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(AchievementsActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

}
