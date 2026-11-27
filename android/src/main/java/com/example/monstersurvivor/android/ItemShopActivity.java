package com.example.monstersurvivor.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.example.monstersurvivor.R;
import com.example.monstersurvivor.android.model.CharacterProgressManager;
import com.example.monstersurvivor.android.util.UiUtils;
import com.example.monstersurvivor.character.CharacterProgressLedger;
import com.example.monstersurvivor.consumable.PotionType;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Native, offline shop that uses the existing character coin wallet. */
public final class ItemShopActivity extends NativeAudioActivity {
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private final PotionType[] types=PotionType.values();
    private final int[] titleIds={R.string.potion_health_name,R.string.potion_speed_name,R.string.potion_cooldown_name};
    private final int[] descriptionIds={R.string.potion_health_desc,R.string.potion_speed_desc,R.string.potion_cooldown_desc};
    private final int[] priceIds={R.id.tv_shop_health_price,R.id.tv_shop_speed_price,R.id.tv_shop_cooldown_price};
    private final int[] countIds={R.id.tv_shop_health_count,R.id.tv_shop_speed_count,R.id.tv_shop_cooldown_count};
    private final int[] buttonIds={R.id.btn_shop_health,R.id.btn_shop_speed,R.id.btn_shop_cooldown};
    private final int[] cardTitleIds={R.id.tv_shop_health_title,R.id.tv_shop_speed_title,R.id.tv_shop_cooldown_title};
    private final int[] cardDescriptionIds={R.id.tv_shop_health_desc,R.id.tv_shop_speed_desc,R.id.tv_shop_cooldown_desc};
    private CharacterProgressManager progress;
    private TextView wallet;
    private MaterialButton[] buttons;
    private boolean purchasePending;

    @Override protected void attachBaseContext(Context base){super.attachBaseContext(UiUtils.wrapLocale(base));}
    @Override protected void onCreate(@Nullable Bundle savedInstanceState){
        UiUtils.applyAppTheme(this);super.onCreate(savedInstanceState);UiUtils.applyImmersiveMode(this);
        setContentView(R.layout.activity_item_shop);
        progress=CharacterProgressManager.getInstance(this);
        MaterialToolbar toolbar=findViewById(R.id.toolbar_item_shop);toolbar.setNavigationOnClickListener(v->finish());
        toolbar.inflateMenu(R.menu.menu_item_shop);
        toolbar.setOnMenuItemClickListener(item->{
            if(item.getItemId()!=R.id.action_recharge_coins)return false;
            showCoinPacks();return true;
        });
        wallet=findViewById(R.id.tv_shop_wallet);buttons=new MaterialButton[types.length];
        for(int i=0;i<types.length;i++){
            final PotionType type=types[i];
            ((TextView)findViewById(cardTitleIds[i])).setText(titleIds[i]);
            ((TextView)findViewById(cardDescriptionIds[i])).setText(descriptionIds[i]);
            buttons[i]=findViewById(buttonIds[i]);buttons[i].setOnClickListener(v->purchase(type));
        }
        refresh();
    }
    @Override protected void onResume(){super.onResume();UiUtils.applyImmersiveMode(this);refresh();}
    private void refresh(){
        if(progress==null || wallet==null)return;
        int balance=progress.coins();wallet.setText(getString(R.string.shop_wallet,balance));
        for(int i=0;i<types.length;i++){
            PotionType type=types[i];int count=progress.potionCount(type);
            ((TextView)findViewById(priceIds[i])).setText(getString(R.string.shop_price,type.price()));
            ((TextView)findViewById(countIds[i])).setText(getString(R.string.shop_owned,count));
            buttons[i].setText(count>=PotionType.MAX_INVENTORY?R.string.shop_full:R.string.shop_buy);
            buttons[i].setEnabled(!purchasePending && count<PotionType.MAX_INVENTORY);
        }
    }
    private void purchase(PotionType type){
        if(purchasePending)return;
        purchasePending=true;refresh();
        io.execute(()->{
            CharacterProgressLedger.PotionPurchaseResult result=progress.purchasePotion(type);
            main.post(()->{
                if(isFinishing() || isDestroyed())return;
                purchasePending=false;refresh();
                int message=switch(result){
                    case PURCHASED->R.string.shop_purchase_success;
                    case INSUFFICIENT_COINS->R.string.shop_purchase_insufficient;
                    case INVENTORY_FULL->R.string.shop_full;
                    case SAVE_FAILED->R.string.shop_purchase_failed;
                };
                Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
            });
        });
    }
    private void showCoinPacks(){
        int[] packs={100,1000,9999};
        String[] options=new String[packs.length];
        for(int i=0;i<packs.length;i++)options[i]=getString(R.string.coin_test_option,packs[i]);
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle(R.string.coin_test_title)
            .setItems(options,(d,which)->grantTestCoins(packs[which]))
            .setNegativeButton(R.string.dialog_close,(d,which)->d.dismiss())
            .create();
        dialog.show();UiUtils.styleDialog(dialog);
    }
    private void grantTestCoins(int amount){
        if(progress.grantVirtualTestCoins(amount)){
            refresh();Toast.makeText(this,getString(R.string.coin_test_success,amount),Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this,R.string.coin_test_error,Toast.LENGTH_SHORT).show();
    }
    @Override protected void onDestroy(){io.shutdown();super.onDestroy();}
}
