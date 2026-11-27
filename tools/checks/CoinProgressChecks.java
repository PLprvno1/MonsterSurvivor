import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterOwnership;
import com.example.monstersurvivor.character.CharacterProgressLedger;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.EnemyType;
import com.example.monstersurvivor.gameplay.CoinSettlement;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.pickup.PickupType;
import com.example.monstersurvivor.world.CollisionGrid;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Focused checks for coin drops/collection and the wallet ownership ledger. */
public final class CoinProgressChecks {
    private static int checks;
    private static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    private static CollisionGrid open(){return new CollisionGrid(80,80,16,new byte[6400]);}
    private static GameConfig config(GameMode mode){return new GameConfig.Builder().playerHp(100).population(0,40).mode(mode)
        .enemy(EnemyType.SLIME,1,1,1).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();}
    private static long seedForCoinDrop(){Random candidates=new Random(12);for(int i=0;i<10000;i++){long seed=candidates.nextLong();if(new Random(seed^0xC01A5L).nextFloat()<.12f)return seed;}throw new AssertionError("coin seed");}
    private static void dropAndCollect(){
        check(PickupType.COIN!=null && com.example.monstersurvivor.pickup.PickupSystem.COIN_DROP_CHANCE==.12f
            && com.example.monstersurvivor.pickup.PickupSystem.COIN_LIFETIME==20
            && com.example.monstersurvivor.pickup.PickupSystem.COIN_CAPACITY==48,"ordinary coin drop limits");
        long seed=seedForCoinDrop();var s=new GameSession(config(GameMode.NORMAL),open(),600,600,null,null,seed);
        var enemy=s.enemies.spawn(EnemyType.SLIME,660,600);check(enemy!=null,"spawn ordinary coin-drop target");enemy.damage(1);
        check(s.pickups.coinDropRolls==1 && s.pickups.coinCount()==1 && s.runCoins()==0,"real enemy death spawns an unbanked map coin");
        com.example.monstersurvivor.pickup.Pickup coin=null;
        for(int i=0;i<s.pickups.size();i++)if(s.pickups.get(i).type==PickupType.COIN)coin=s.pickups.get(i);
        check(coin!=null && coin.lifetime==20,"coin is a visible timed pickup");
        s.pickups.update(.1f,0,s.player);check(s.pickups.coinCount()==1 && s.runCoins()==0,"coin requires player contact");
        s.player.x=coin.x;s.player.y=coin.y;s.pickups.update(.1f,0,s.player);
        check(s.pickups.coinCount()==0 && s.pickups.collectedCoins==1 && s.runCoins()==1,"contact collects one temporary coin");
        check(!s.pickups.collect(coin.id) && s.runCoins()==1,"collected coin cannot be collected twice");s.end();

        var demo=new GameSession(config(GameMode.DEMO),open(),600,600,null,null,seed);
        check(!demo.coinEligible() && demo.pickups.spawn(PickupType.COIN,600,600,1)==null,"Demo cannot create official coins");demo.end();
        var debugConfig=new GameConfig.Builder().debug(true).population(0,40).build();
        var debug=new GameSession(debugConfig,open(),600,600,null,null,seed);
        check(!debug.coinEligible() && debug.pickups.spawn(PickupType.COIN,600,600,1)==null,"debug fixtures cannot create official coins");debug.end();
    }
    private static void walletPurchaseAndSettlement(){
        String run=UUID.randomUUID().toString();var ledger=new CharacterProgressLedger(0,Set.of("warrior"),"warrior",Set.of());
        check(ledger.coins()==0 && ledger.isOwned("warrior") && !ledger.isOwned("archer") && ledger.selected().equals("warrior"),"first-run progress defaults");
        check(ledger.creditRunCoins(run,100,GameMode.NORMAL,false) && ledger.coins()==100,"Normal settlement credits wallet");
        check(!ledger.creditRunCoins(run,100,GameMode.NORMAL,false) && ledger.coins()==100,"same UUID is rejected on repeat settlement");
        check(!ledger.creditRunCoins(UUID.randomUUID().toString(),10,GameMode.DEMO,false)
            && !ledger.creditRunCoins(UUID.randomUUID().toString(),10,GameMode.ENDLESS,true),"Demo and diagnostic settlements are rejected");
        check(ledger.purchase("archer") && ledger.coins()==0 && ledger.isOwned("archer") && ledger.selected().equals("archer"),"Archer purchase debits and selects atomically");
        check(!ledger.purchase("archer") && ledger.coins()==0,"repeat purchase does not charge again");
        check(!ledger.purchase("mage") && !ledger.select("mage"),"locked Mage cannot be purchased without funds or selected");
        String endless=UUID.randomUUID().toString();check(ledger.creditRunCoins(endless,220,GameMode.ENDLESS,false)
            && ledger.purchase("mage") && ledger.coins()==0 && ledger.selected().equals("mage"),"Endless earnings unlock and select Mage");
        check(!ledger.creditRunCoins("invalid",100,GameMode.NORMAL,false),"malformed settlement UUID is rejected");
        var saturated=new CharacterProgressLedger(Integer.MAX_VALUE-1,Set.of("warrior"),"warrior",Set.of());
        check(saturated.creditRunCoins(UUID.randomUUID().toString(),5,GameMode.NORMAL,false)
            && saturated.coins()==Integer.MAX_VALUE,"wallet addition clamps on overflow");
        check(CharacterOwnership.price(CharacterId.ARCHER)==100 && CharacterOwnership.price(CharacterId.MAGE)==220
            && CharacterOwnership.selectableOrWarrior("mage",Set.of("warrior"))==CharacterId.WARRIOR
            && CharacterOwnership.selectableOrWarrior("mage",Set.of("warrior","mage"))==CharacterId.MAGE,
            "fixed prices and launch ownership checks");
        check(new CoinSettlement(UUID.randomUUID().toString(),0,GameMode.NORMAL,false).eligible()
            && !new CoinSettlement(UUID.randomUUID().toString(),1,GameMode.DEMO,false).eligible(),"result/Home settlement eligibility");
    }
    public static void main(String[] args){dropAndCollect();walletPurchaseAndSettlement();System.out.println("CoinProgressChecks PASS "+checks+" assertions");}
}
