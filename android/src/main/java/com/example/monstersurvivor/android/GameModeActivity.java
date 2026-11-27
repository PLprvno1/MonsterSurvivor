package com.example.monstersurvivor.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.util.UiUtils;
import com.example.monstersurvivor.android.model.CharacterProgressManager;
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterOwnership;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Screen to configure game difficulty (EASY/HARD) and game mode (NORMAL/DEMO).
 * Launches AndroidLauncher with explicit intent extras.
 */
public class GameModeActivity extends NativeAudioActivity {

    public static final String EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY";
    public static final String EXTRA_GAME_MODE = "EXTRA_GAME_MODE";
    public static final String EXTRA_CHARACTER_ID = "EXTRA_CHARACTER_ID";

    public static final String DIFFICULTY_EASY = "EASY";
    public static final String DIFFICULTY_HARD = "HARD";
    public static final String MODE_NORMAL = "NORMAL";
    public static final String MODE_DEMO = "DEMO";
    public static final String MODE_ENDLESS = "ENDLESS";

    private String mSelectedDifficulty = DIFFICULTY_EASY;
    private String mSelectedMode = MODE_NORMAL;
    private String mSelectedCharacterId = "warrior";
    private boolean mIsLaunchingGame = false;
    private CharacterProgressManager mCharacterProgress;

    private View mCardDiffEasy;
    private View mCardDiffHard;
    private View mCardModeNormal;
    private View mCardModeDemo;
    private View mCardModeEndless;

    private TextView mTvDiffEasyTitle;
    private TextView mTvDiffHardTitle;
    private TextView mTvModeNormalTitle;
    private TextView mTvModeDemoTitle;
    private TextView mTvModeEndlessTitle;

    private TextView mTvBadge;
    private TextView mTvDescription;
    private TextView mTvCharacterWallet;
    private Button mBtnUnlockCharacter,mBtnStartGame;
    private final String[] mCharacterIds={"warrior","archer","mage"};
    private final View[] mCharacterCards=new View[3];
    private final ImageView[] mCharacterPortraits=new ImageView[3];
    private final TextView[] mCharacterNames=new TextView[3];
    private final TextView[] mCharacterWeapons=new TextView[3];
    private final TextView[] mCharacterDescriptions=new TextView[3];
    private final TextView[] mCharacterStates=new TextView[3];
    private static final String[] CHARACTER_PORTRAIT_ASSETS={
        "game/tiny/player.png","game/characters/archer.png","game/characters/mage.png"
    };
    private static final int[][] CHARACTER_PORTRAIT_FRAMES={
        {2,2,64,49},{0,0,32,32},{0,0,48,48}
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(UiUtils.wrapLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.applyAppTheme(this);
        super.onCreate(savedInstanceState);
        mCharacterProgress=CharacterProgressManager.getInstance(this);
        mSelectedCharacterId=savedInstanceState!=null?savedInstanceState.getString(EXTRA_CHARACTER_ID,mCharacterProgress.selectedCharacterId())
            :mCharacterProgress.selectedCharacterId();
        UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_game_mode);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_mode);
        toolbar.setNavigationOnClickListener(v -> UiUtils.navigateBack(this));
        toolbar.inflateMenu(R.menu.menu_shop_action);
        toolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId()!=R.id.action_item_shop)return false;
            startActivity(new Intent(GameModeActivity.this,ItemShopActivity.class));
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                UiUtils.navigateBack(GameModeActivity.this);
            }
        });

        initViews();
        setupListeners();
        updateSelectionUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsLaunchingGame = false;
        if(mCharacterProgress!=null) updateSelectionUI();
        UiUtils.applyImmersiveMode(this);
    }

    private void initViews() {
        mCardDiffEasy = findViewById(R.id.card_diff_easy);
        mCardDiffHard = findViewById(R.id.card_diff_hard);
        mCardModeNormal = findViewById(R.id.card_mode_normal);
        mCardModeDemo = findViewById(R.id.card_mode_demo);
        mCardModeEndless = findViewById(R.id.card_mode_endless);

        mTvDiffEasyTitle = findViewById(R.id.tv_diff_easy_title);
        mTvDiffHardTitle = findViewById(R.id.tv_diff_hard_title);
        mTvModeNormalTitle = findViewById(R.id.tv_mode_normal_title);
        mTvModeDemoTitle = findViewById(R.id.tv_mode_demo_title);
        mTvModeEndlessTitle = findViewById(R.id.tv_mode_endless_title);

        mTvBadge = findViewById(R.id.tv_detail_selection_badge);
        mTvDescription = findViewById(R.id.tv_mode_description);
        mTvCharacterWallet=findViewById(R.id.tv_character_wallet);
        mBtnUnlockCharacter=findViewById(R.id.btn_unlock_character);mBtnStartGame=findViewById(R.id.btn_start_game);
        int[] cardIds={R.id.card_character_warrior,R.id.card_character_archer,R.id.card_character_mage};
        int[] portraitIds={R.id.iv_character_warrior,R.id.iv_character_archer,R.id.iv_character_mage};
        int[] nameIds={R.id.tv_character_warrior_name,R.id.tv_character_archer_name,R.id.tv_character_mage_name};
        int[] weaponIds={R.id.tv_character_warrior_weapon,R.id.tv_character_archer_weapon,R.id.tv_character_mage_weapon};
        int[] descriptionIds={R.id.tv_character_warrior_description,R.id.tv_character_archer_description,R.id.tv_character_mage_description};
        int[] stateIds={R.id.tv_character_warrior_status,R.id.tv_character_archer_status,R.id.tv_character_mage_status};
        for(int i=0;i<3;i++) {
            final int index=i;
            mCharacterCards[i]=findViewById(cardIds[i]);mCharacterPortraits[i]=findViewById(portraitIds[i]);
            mCharacterNames[i]=findViewById(nameIds[i]);mCharacterWeapons[i]=findViewById(weaponIds[i]);
            mCharacterDescriptions[i]=findViewById(descriptionIds[i]);mCharacterStates[i]=findViewById(stateIds[i]);
            loadPortrait(i);
            mCharacterCards[i].setOnClickListener(v->selectCharacter(index));
        }
    }

    private void selectCharacter(int index) {
        mSelectedCharacterId=mCharacterIds[index];
        if(mCharacterProgress.isOwned(mSelectedCharacterId)) {
            mCharacterProgress.selectCharacter(mSelectedCharacterId);
            com.example.monstersurvivor.android.audio.NativeAudio.play(this,com.example.monstersurvivor.audio.AudioEvent.UI_NAVIGATION);
        }
        updateSelectionUI();
    }

    private void loadPortrait(int index) {
        Bitmap sheet=null,frame=null;
        try(java.io.InputStream stream=getAssets().open(CHARACTER_PORTRAIT_ASSETS[index])) {
            BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
            sheet=BitmapFactory.decodeStream(stream,null,options);
            int[] crop=CHARACTER_PORTRAIT_FRAMES[index];
            if(sheet!=null && crop[0]+crop[2]<=sheet.getWidth() && crop[1]+crop[3]<=sheet.getHeight()) {
                frame=Bitmap.createBitmap(sheet,crop[0],crop[1],crop[2],crop[3]);frame.setDensity(Bitmap.DENSITY_NONE);
                BitmapDrawable drawable=new BitmapDrawable(getResources(),frame);drawable.setTargetDensity(getResources().getDisplayMetrics().densityDpi);
                drawable.setFilterBitmap(false);mCharacterPortraits[index].setImageDrawable(drawable);
            }
        } catch(java.io.IOException | IllegalArgumentException ignored) {
            mCharacterPortraits[index].setImageResource(R.drawable.ic_sword);
        } finally {
            if(sheet!=null && !sheet.isRecycled())sheet.recycle();
        }
        if(mCharacterPortraits[index].getDrawable()==null)mCharacterPortraits[index].setImageResource(R.drawable.ic_sword);
    }

    private void confirmCharacterPurchase() {
        if(mCharacterProgress==null || mCharacterProgress.isOwned(mSelectedCharacterId))return;
        int price=CharacterOwnership.price(CharacterId.fromStableId(mSelectedCharacterId));
        if(mCharacterProgress.coins()<price) {
            Toast.makeText(this,getString(R.string.character_unlock_insufficient,price),Toast.LENGTH_SHORT).show();return;
        }
        String purchaseId=mSelectedCharacterId;
        String name=getString("archer".equals(purchaseId)?R.string.character_archer:R.string.character_mage);
        new AlertDialog.Builder(this).setTitle(R.string.character_unlock_title)
            .setMessage(getString(R.string.character_unlock_confirm,name,price))
            .setNegativeButton(R.string.character_unlock_cancel, (dialog,which)->{})
            .setPositiveButton(R.string.character_unlock_button,(dialog,which)->{
                CharacterProgressManager.PurchaseResult result=mCharacterProgress.purchase(purchaseId);
                if(result==CharacterProgressManager.PurchaseResult.PURCHASED) {
                    mSelectedCharacterId=mCharacterProgress.selectedCharacterId();updateSelectionUI();
                    Toast.makeText(this,R.string.character_unlock_success,Toast.LENGTH_SHORT).show();
                } else if(result==CharacterProgressManager.PurchaseResult.INSUFFICIENT_COINS) {
                    Toast.makeText(this,getString(R.string.character_unlock_insufficient,price),Toast.LENGTH_SHORT).show();
                } else if(result==CharacterProgressManager.PurchaseResult.ALREADY_OWNED) {
                    updateSelectionUI();
                } else if(result==CharacterProgressManager.PurchaseResult.SAVE_FAILED) {
                    Toast.makeText(this,R.string.character_unlock_save_failed,Toast.LENGTH_SHORT).show();
                }
            }).show();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString(EXTRA_CHARACTER_ID,mSelectedCharacterId);
        super.onSaveInstanceState(state);
    }

    private void setupListeners() {
        mCardDiffEasy.setOnClickListener(v -> {
            if(DIFFICULTY_EASY.equals(mSelectedDifficulty))return;
            com.example.monstersurvivor.android.audio.NativeAudio.play(this,com.example.monstersurvivor.audio.AudioEvent.UI_NAVIGATION);
            mSelectedDifficulty = DIFFICULTY_EASY;
            updateSelectionUI();
        });

        mCardDiffHard.setOnClickListener(v -> {
            if(DIFFICULTY_HARD.equals(mSelectedDifficulty))return;
            com.example.monstersurvivor.android.audio.NativeAudio.play(this,com.example.monstersurvivor.audio.AudioEvent.UI_NAVIGATION);
            mSelectedDifficulty = DIFFICULTY_HARD;
            updateSelectionUI();
        });

        mCardModeNormal.setOnClickListener(v -> {
            if(MODE_NORMAL.equals(mSelectedMode))return;
            com.example.monstersurvivor.android.audio.NativeAudio.play(this,com.example.monstersurvivor.audio.AudioEvent.UI_NAVIGATION);
            mSelectedMode = MODE_NORMAL;
            updateSelectionUI();
        });

        mCardModeDemo.setOnClickListener(v -> {
            if(MODE_DEMO.equals(mSelectedMode))return;
            com.example.monstersurvivor.android.audio.NativeAudio.play(this,com.example.monstersurvivor.audio.AudioEvent.UI_NAVIGATION);
            mSelectedMode = MODE_DEMO;
            updateSelectionUI();
        });

        mCardModeEndless.setOnClickListener(v->{
            if(MODE_ENDLESS.equals(mSelectedMode))return;
            com.example.monstersurvivor.android.audio.NativeAudio.play(this,com.example.monstersurvivor.audio.AudioEvent.UI_NAVIGATION);
            mSelectedMode=MODE_ENDLESS;updateSelectionUI();
        });
        View btnStartGame = findViewById(R.id.btn_start_game);
        btnStartGame.setEnabled(mCharacterProgress.isOwned(mSelectedCharacterId));
        btnStartGame.setOnClickListener(v -> UiUtils.animateClick(v, com.example.monstersurvivor.audio.AudioEvent.UI_CONFIRM, this::launchGame));
        mBtnUnlockCharacter.setOnClickListener(v -> {
            if(mCharacterProgress.coins()<CharacterOwnership.price(CharacterId.fromStableId(mSelectedCharacterId)))return;
            confirmCharacterPurchase();
        });

        View btnPreview = findViewById(R.id.btn_preview_screens);
        if (btnPreview != null) {
            // Hide preview mockup button in production lobby flow to prevent fake stats exposure
            btnPreview.setVisibility(View.GONE);
        }
    }

    private void updateSelectionUI() {
        updateCharacterCards();
        // Update Difficulty cards
        boolean isEasy = DIFFICULTY_EASY.equals(mSelectedDifficulty);
        mCardDiffEasy.setBackgroundResource(isEasy ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvDiffEasyTitle.setTextColor(ContextCompat.getColor(this, isEasy ? R.color.gold_bright : R.color.text_secondary));

        mCardDiffHard.setBackgroundResource(!isEasy ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvDiffHardTitle.setTextColor(ContextCompat.getColor(this, !isEasy ? R.color.gold_bright : R.color.text_secondary));

        // Update Mode cards
        boolean isNormal = MODE_NORMAL.equals(mSelectedMode);
        boolean isDemo=MODE_DEMO.equals(mSelectedMode),isEndless=MODE_ENDLESS.equals(mSelectedMode);
        mCardModeNormal.setBackgroundResource(isNormal ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvModeNormalTitle.setTextColor(ContextCompat.getColor(this, isNormal ? R.color.gold_bright : R.color.text_secondary));

        mCardModeDemo.setBackgroundResource(isDemo ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvModeDemoTitle.setTextColor(ContextCompat.getColor(this, isDemo ? R.color.gold_bright : R.color.text_secondary));
        mCardModeEndless.setBackgroundResource(isEndless ? R.drawable.panel_fantasy_card_selected : R.drawable.panel_fantasy_card);
        mTvModeEndlessTitle.setTextColor(ContextCompat.getColor(this,isEndless?R.color.gold_bright:R.color.text_secondary));

        // Update Summary badge & description
        String diffText = isEasy ? getString(R.string.difficulty_easy) : getString(R.string.difficulty_hard);
        String modeText = getString(isEndless?R.string.mode_endless:isNormal?R.string.mode_normal:R.string.mode_demo);
        String badgeText = getString(R.string.mode_badge_format, diffText, modeText);
        mTvBadge.setText(badgeText);
        int wallet=mCharacterProgress.coins();boolean owned=mCharacterProgress.isOwned(mSelectedCharacterId);
        int price=CharacterOwnership.price(CharacterId.fromStableId(mSelectedCharacterId));
        mTvCharacterWallet.setText(getString(R.string.character_wallet_format,wallet));
        mBtnUnlockCharacter.setVisibility(owned?View.GONE:View.VISIBLE);
        mBtnUnlockCharacter.setText(getString(R.string.character_unlock_button_price,price));
        mBtnUnlockCharacter.setEnabled(wallet>=price);
        mBtnStartGame.setEnabled(owned);mBtnStartGame.setAlpha(owned?1f:.52f);

        if(isEndless){
            mTvDescription.setText(R.string.desc_mode_endless);
        } else if (isNormal) {
            mTvDescription.setText(R.string.desc_mode_normal);
        } else {
            mTvDescription.setText(R.string.desc_mode_demo);
        }
    }

    private void updateCharacterCards() {
        if(mCharacterProgress==null)return;
        int[] nameRes={R.string.character_warrior,R.string.character_archer,R.string.character_mage};
        int[] weaponRes={R.string.character_card_weapon_warrior,R.string.character_card_weapon_archer,R.string.character_card_weapon_mage};
        int[] descriptionRes={R.string.character_card_desc_warrior,R.string.character_card_desc_archer,R.string.character_card_desc_mage};
        int wallet=mCharacterProgress.coins();
        for(int i=0;i<3;i++) {
            String id=mCharacterIds[i];boolean owned=mCharacterProgress.isOwned(id),selected=id.equals(mSelectedCharacterId);
            mCharacterCards[i].setBackgroundResource(selected?R.drawable.panel_fantasy_card_selected:R.drawable.panel_fantasy_card);
            String accessibleState=owned
                ?getString(i==0?R.string.character_card_owned_free:R.string.character_card_owned)
                :getString(R.string.character_card_locked,CharacterOwnership.price(CharacterId.fromStableId(id)));
            mCharacterCards[i].setContentDescription(getString(nameRes[i])+", "+accessibleState);
            mCharacterPortraits[i].setAlpha(owned?1f:.56f);
            mCharacterNames[i].setText(nameRes[i]);mCharacterNames[i].setTextColor(ContextCompat.getColor(this,selected?R.color.gold_bright:R.color.text_secondary));
            mCharacterWeapons[i].setText(weaponRes[i]);mCharacterDescriptions[i].setText(descriptionRes[i]);
            int statusRes=owned?(i==0?R.string.character_card_owned_free:R.string.character_card_owned):R.string.character_card_locked;
            mCharacterStates[i].setText(owned?getString(statusRes):getString(statusRes,CharacterOwnership.price(CharacterId.fromStableId(id))));
            mCharacterStates[i].setTextColor(ContextCompat.getColor(this,owned?R.color.gold_bright:R.color.text_secondary));
            mCharacterCards[i].setAlpha(owned?1f:.92f);
            if(!owned && wallet>=CharacterOwnership.price(CharacterId.fromStableId(id)))mCharacterStates[i].setTextColor(ContextCompat.getColor(this,R.color.gold_bright));
        }
    }

    private void launchGame() {
        if (mIsLaunchingGame) return;
        if(!mCharacterProgress.isOwned(mSelectedCharacterId)) {
            Toast.makeText(this,R.string.character_locked_start,Toast.LENGTH_SHORT).show();return;
        }
        if(!mCharacterProgress.selectCharacter(mSelectedCharacterId))return;
        mIsLaunchingGame = true;
        Intent intent = new Intent(this, AndroidLauncher.class);
        intent.putExtra(EXTRA_DIFFICULTY, mSelectedDifficulty);
        intent.putExtra(EXTRA_GAME_MODE, mSelectedMode);
        intent.putExtra(EXTRA_CHARACTER_ID,mSelectedCharacterId);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
