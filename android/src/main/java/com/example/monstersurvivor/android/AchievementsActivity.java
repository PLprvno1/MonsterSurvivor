package com.example.monstersurvivor.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.model.AchievementManager;
import com.example.monstersurvivor.android.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Achievements Activity displaying survival statistics and achievements.
 * Features ContextMenu for viewing details, copying stats, and sharing via Intent.
 */
public class AchievementsActivity extends AppCompatActivity {

    private AchievementManager mManager;

    private View mCardBestTime;
    private View mCardKills;
    private View mCardVictories;
    private View mCardMatches;
    private View mCardBoss;

    private int mSelectedCardId = View.NO_ID;
    private BottomNavigationView mBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_achievements);

        mManager = AchievementManager.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_achievements);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        bindData();
        setupContextMenu();
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
        mCardBestTime = findViewById(R.id.card_ach_best_time);
        mCardKills = findViewById(R.id.card_ach_kills);
        mCardVictories = findViewById(R.id.card_ach_victories);
        mCardMatches = findViewById(R.id.card_ach_matches);
        mCardBoss = findViewById(R.id.card_ach_boss);
    }

    private void bindData() {
        TextView tvBestTime = findViewById(R.id.tv_val_best_time);
        TextView tvKills = findViewById(R.id.tv_val_total_kills);
        TextView tvVictories = findViewById(R.id.tv_val_victories);
        TextView tvMatches = findViewById(R.id.tv_val_matches);
        TextView tvBoss = findViewById(R.id.tv_val_boss);
        TextView tvEmptyBadge = findViewById(R.id.tv_empty_badge);

        int bestSec = mManager.getBestSurvivalSeconds();
        int kills = mManager.getTotalKills();
        int victories = mManager.getTotalVictories();
        int matches = mManager.getTotalMatches();
        int bossDefeated = mManager.getBossDefeatedCount();

        tvBestTime.setText(AchievementManager.formatTime(bestSec));
        tvKills.setText(String.valueOf(kills));
        tvVictories.setText(String.valueOf(victories));
        tvMatches.setText(String.valueOf(matches));

        if (bossDefeated > 0) {
            tvBoss.setText(getString(R.string.ach_boss_status_defeated, bossDefeated));
        } else {
            tvBoss.setText(R.string.ach_boss_status_none);
        }

        if (mManager.hasAnyRecordedMatches()) {
            tvEmptyBadge.setVisibility(View.GONE);
        } else {
            tvEmptyBadge.setVisibility(View.VISIBLE);
        }
    }

    private void setupContextMenu() {
        // Register each card to display ContextMenu on long click
        registerForContextMenu(mCardBestTime);
        registerForContextMenu(mCardKills);
        registerForContextMenu(mCardVictories);
        registerForContextMenu(mCardMatches);
        registerForContextMenu(mCardBoss);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mSelectedCardId = v.getId();
        getMenuInflater().inflate(R.menu.menu_context_achievement, menu);

        // Set contextual header title
        if (mSelectedCardId == R.id.card_ach_best_time) {
            menu.setHeaderTitle(R.string.ach_best_time);
        } else if (mSelectedCardId == R.id.card_ach_kills) {
            menu.setHeaderTitle(R.string.ach_total_kills);
        } else if (mSelectedCardId == R.id.card_ach_victories) {
            menu.setHeaderTitle(R.string.ach_victories);
        } else if (mSelectedCardId == R.id.card_ach_matches) {
            menu.setHeaderTitle(R.string.ach_matches);
        } else if (mSelectedCardId == R.id.card_ach_boss) {
            menu.setHeaderTitle(R.string.ach_boss);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.context_view_details) {
            showAchievementDetails(mSelectedCardId);
            return true;
        } else if (itemId == R.id.context_copy_info) {
            copyAchievementInfo(mSelectedCardId);
            return true;
        } else if (itemId == R.id.context_share_info) {
            shareAchievementInfo(mSelectedCardId);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showAchievementDetails(int cardId) {
        String title;
        String message;

        if (cardId == R.id.card_ach_best_time) {
            title = getString(R.string.ach_best_time);
            message = getString(R.string.ach_best_time_desc) + "\n\nKỷ lục hiện tại: "
                    + AchievementManager.formatTime(mManager.getBestSurvivalSeconds())
                    + "\nMục tiêu: Sống sót tối thiểu 10:00 (600 giây).";
        } else if (cardId == R.id.card_ach_kills) {
            title = getString(R.string.ach_total_kills);
            message = getString(R.string.ach_total_kills_desc) + "\n\nTổng quái đã hạ: "
                    + mManager.getTotalKills() + " quái vật.";
        } else if (cardId == R.id.card_ach_victories) {
            title = getString(R.string.ach_victories);
            message = getString(R.string.ach_victories_desc) + "\n\nSố trận thắng: "
                    + mManager.getTotalVictories() + " trận.";
        } else if (cardId == R.id.card_ach_matches) {
            title = getString(R.string.ach_matches);
            message = getString(R.string.ach_matches_desc) + "\n\nTổng trận đã tham gia: "
                    + mManager.getTotalMatches() + " trận.";
        } else if (cardId == R.id.card_ach_boss) {
            title = getString(R.string.ach_boss);
            message = getString(R.string.ach_boss_desc) + "\n\nSố lần tiêu diệt: "
                    + mManager.getBossDefeatedCount() + " lần.";
        } else {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_trophy)
                .setPositiveButton(R.string.dialog_close, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String getAchievementShareText(int cardId) {
        if (cardId == R.id.card_ach_best_time) {
            return "[Monster Survivor] Kỷ lục sinh tồn: "
                    + AchievementManager.formatTime(mManager.getBestSurvivalSeconds())
                    + " trong rừng quái vật!";
        } else if (cardId == R.id.card_ach_kills) {
            return "[Monster Survivor] Tổng quái vật đã tiêu diệt: "
                    + mManager.getTotalKills() + " quái!";
        } else if (cardId == R.id.card_ach_victories) {
            return "[Monster Survivor] Số trận chiến thắng: "
                    + mManager.getTotalVictories() + " trận!";
        } else if (cardId == R.id.card_ach_matches) {
            return "[Monster Survivor] Tổng số trận đã tham chiến: "
                    + mManager.getTotalMatches() + " trận!";
        } else if (cardId == R.id.card_ach_boss) {
            return "[Monster Survivor] Chúa tể Lich Lord đã bị tiêu diệt: "
                    + mManager.getBossDefeatedCount() + " lần!";
        }
        return "[Monster Survivor] Bảng vàng thành tích sinh tồn!";
    }

    private void copyAchievementInfo(int cardId) {
        String text = getAchievementShareText(cardId);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("MonsterSurvivor Achievement", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareAchievementInfo(int cardId) {
        String text = getAchievementShareText(cardId);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
    }
}
