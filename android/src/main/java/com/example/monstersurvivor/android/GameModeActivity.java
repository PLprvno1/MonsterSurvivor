package com.example.monstersurvivor.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Screen to configure game difficulty (EASY/HARD) and game mode (NORMAL/DEMO).
 * Launches AndroidLauncher with explicit intent extras.
 */
public class GameModeActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY";
    public static final String EXTRA_GAME_MODE = "EXTRA_GAME_MODE";

    public static final String DIFFICULTY_EASY = "EASY";
    public static final String DIFFICULTY_HARD = "HARD";
    public static final String MODE_NORMAL = "NORMAL";
    public static final String MODE_DEMO = "DEMO";

    private String mSelectedDifficulty = DIFFICULTY_EASY;
    private String mSelectedMode = MODE_NORMAL;

    private View mCardDiffEasy;
    private View mCardDiffHard;
    private View mCardModeNormal;
    private View mCardModeDemo;

    private TextView mTvDiffEasyTitle;
    private TextView mTvDiffHardTitle;
    private TextView mTvModeNormalTitle;
    private TextView mTvModeDemoTitle;

    private TextView mTvBadge;
    private TextView mTvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_game_mode);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_mode);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupListeners();
        updateSelectionUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UiUtils.applyImmersiveMode(this);
    }

    private void initViews() {
        mCardDiffEasy = findViewById(R.id.card_diff_easy);
        mCardDiffHard = findViewById(R.id.card_diff_hard);
        mCardModeNormal = findViewById(R.id.card_mode_normal);
        mCardModeDemo = findViewById(R.id.card_mode_demo);

        mTvDiffEasyTitle = findViewById(R.id.tv_diff_easy_title);
        mTvDiffHardTitle = findViewById(R.id.tv_diff_hard_title);
        mTvModeNormalTitle = findViewById(R.id.tv_mode_normal_title);
        mTvModeDemoTitle = findViewById(R.id.tv_mode_demo_title);

        mTvBadge = findViewById(R.id.tv_detail_selection_badge);
        mTvDescription = findViewById(R.id.tv_mode_description);
    }

    private void setupListeners() {
        mCardDiffEasy.setOnClickListener(v -> {
            mSelectedDifficulty = DIFFICULTY_EASY;
            updateSelectionUI();
        });

        mCardDiffHard.setOnClickListener(v -> {
            mSelectedDifficulty = DIFFICULTY_HARD;
            updateSelectionUI();
        });

        mCardModeNormal.setOnClickListener(v -> {
            mSelectedMode = MODE_NORMAL;
            updateSelectionUI();
        });

        mCardModeDemo.setOnClickListener(v -> {
            mSelectedMode = MODE_DEMO;
            updateSelectionUI();
        });

        View btnStartGame = findViewById(R.id.btn_start_game);
        btnStartGame.setOnClickListener(v -> UiUtils.animateClick(v, this::launchGame));

        View btnPreview = findViewById(R.id.btn_preview_screens);
        btnPreview.setOnClickListener(v -> UiUtils.animateClick(v, () -> {
            Intent intent = new Intent(GameModeActivity.this, ResultPreviewActivity.class);
            startActivity(intent);
        }));
    }

    private void updateSelectionUI() {
        // Update Difficulty cards
        boolean isEasy = DIFFICULTY_EASY.equals(mSelectedDifficulty);
        mCardDiffEasy.setBackgroundResource(isEasy ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvDiffEasyTitle.setTextColor(ContextCompat.getColor(this, isEasy ? R.color.gold_bright : R.color.text_secondary));

        mCardDiffHard.setBackgroundResource(!isEasy ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvDiffHardTitle.setTextColor(ContextCompat.getColor(this, !isEasy ? R.color.gold_bright : R.color.text_secondary));

        // Update Mode cards
        boolean isNormal = MODE_NORMAL.equals(mSelectedMode);
        mCardModeNormal.setBackgroundResource(isNormal ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvModeNormalTitle.setTextColor(ContextCompat.getColor(this, isNormal ? R.color.gold_bright : R.color.text_secondary));

        mCardModeDemo.setBackgroundResource(!isNormal ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvModeDemoTitle.setTextColor(ContextCompat.getColor(this, !isNormal ? R.color.gold_bright : R.color.text_secondary));

        // Update Summary badge & description
        String badgeText = mSelectedDifficulty + " + " + (isNormal ? "NORMAL (10 PHÚT)" : "DEMO (~90 GIÂY)");
        mTvBadge.setText(badgeText);

        if (isNormal) {
            mTvDescription.setText(R.string.desc_mode_normal);
        } else {
            mTvDescription.setText(R.string.desc_mode_demo);
        }
    }

    private void launchGame() {
        Intent intent = new Intent(this, AndroidLauncher.class);
        intent.putExtra(EXTRA_DIFFICULTY, mSelectedDifficulty);
        intent.putExtra(EXTRA_GAME_MODE, mSelectedMode);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
