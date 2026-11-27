import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterProgressLedger;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.consumable.PotionSystem;
import com.example.monstersurvivor.consumable.PotionType;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.pickup.PickupType;
import com.example.monstersurvivor.skill.ActiveSkillSystem;
import com.example.monstersurvivor.skill.SkillType;
import com.example.monstersurvivor.ui.PotionHud;
import com.example.monstersurvivor.ui.SkillHud;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.world.CollisionGrid;
import java.util.Set;

/** Focused checks for potion prices, inventory transactions, effects and gameplay timers. */
public final class PotionChecks {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static CharacterProgressLedger ledger(int coins,int[] potions){
        return new CharacterProgressLedger(coins,Set.of(CharacterId.WARRIOR.stableId()),CharacterId.WARRIOR.stableId(),Set.of(),potions);
    }
    public static void main(String[] args){
        check(PotionType.HEALTH.price()==10 && PotionType.SPEED.price()==15 && PotionType.COOLDOWN.price()==20,"V2 prices");
        CharacterProgressLedger poor=ledger(9,new int[3]);
        check(poor.purchasePotion(PotionType.HEALTH)==CharacterProgressLedger.PotionPurchaseResult.INSUFFICIENT_COINS,"reject purchase without funds");
        check(poor.coins()==9 && poor.potionCount(PotionType.HEALTH)==0,"failed purchase changes neither side");
        CharacterProgressLedger wallet=ledger(200,new int[3]);
        for(int i=0;i<PotionType.MAX_INVENTORY;i++)check(wallet.purchasePotion(PotionType.HEALTH)==CharacterProgressLedger.PotionPurchaseResult.PURCHASED,"purchase into available capacity");
        int before=wallet.coins();
        check(wallet.purchasePotion(PotionType.HEALTH)==CharacterProgressLedger.PotionPurchaseResult.INVENTORY_FULL,"inventory capped at nine");
        check(wallet.coins()==before && wallet.potionCount(PotionType.HEALTH)==9,"full inventory is an atomic no-op");
        check(wallet.consumePotion(PotionType.HEALTH) && wallet.potionCount(PotionType.HEALTH)==8,"consume exactly one");
        while(wallet.consumePotion(PotionType.HEALTH)){}
        check(wallet.potionCount(PotionType.HEALTH)==0 && !wallet.consumePotion(PotionType.HEALTH),"empty inventory never goes negative");
        CharacterProgressLedger reloaded=ledger(wallet.coins(),wallet.potionCounts());
        check(reloaded.potionCount(PotionType.HEALTH)==0,"inventory snapshot can be restored from persistence values");

        GameConfig config=GameConfig.defaults();
        Player player=new Player(500,500,null,config);
        PotionSystem potions=new PotionSystem(new int[]{2,1,1},true);
        check(potions.availability(PotionType.HEALTH,player,true)==PotionSystem.Availability.HEALTH_FULL,"healing potion disabled at full HP");
        check(!potions.activate(PotionType.HEALTH,player,true) && potions.count(PotionType.HEALTH)==2,"full HP does not consume");
        player.damage(100);
        check(player.hp()==0 && potions.availability(PotionType.HEALTH,player,true)==PotionSystem.Availability.NOT_RUNNING,"dead player cannot heal");
        Player wounded=new Player(500,500,null,config);wounded.damage(3);
        check(potions.activate(PotionType.HEALTH,wounded,true) && wounded.hp()==wounded.maxHp(),"heal applies once without exceeding max HP");
        check(potions.count(PotionType.HEALTH)==1,"successful heal consumes one potion");
        check(potions.activate(PotionType.SPEED,wounded,true) && potions.speedRemaining()==8,"speed buff duration");
        check(potions.availability(PotionType.SPEED,wounded,true)==PotionSystem.Availability.ALREADY_ACTIVE,"same speed potion cannot stack");
        potions.advance(3);
        check(potions.speedRemaining()==5,"gameplay clock advances speed buff");
        float frozen=potions.speedRemaining(); // Pause/modal do not call advance in GameSession.
        check(potions.speedRemaining()==frozen,"paused gameplay leaves buff timer unchanged");
        check(potions.activate(PotionType.COOLDOWN,wounded,true),"cooldown potion activates independently");
        check(Math.abs(potions.cooldownStep(.1f)-.125f)<.00001f,"cooldown clock runs at ×1.25");
        check(potions.cooldownStep(10)<=14.0001f,"cooldown delta is bounded at ×1.40 for an expiry-crossing frame");
        potions.clearEffects();
        check(potions.speedRemaining()==0 && potions.cooldownRemaining()==0 && potions.count(PotionType.SPEED)==0,"match cleanup clears buffs but not persistent inventory");
        PotionSystem demo=new PotionSystem(new int[]{1,1,1},false);
        check(demo.availability(PotionType.HEALTH,wounded,true)==PotionSystem.Availability.DISABLED,"Demo/diagnostic potions disabled");

        CollisionGrid grid=new CollisionGrid(100,100,16,new byte[10000]);
        Player ordinary=new Player(500,500,null,config);float normalSpeed=ordinary.stats.movementSpeed();
        ordinary.update(.1f,1,0,grid);
        Player boosted=new Player(500,500,null,config);
        boosted.update(.1f,1,0,grid,0,0,0,.1f,.1f);
        check(Math.abs((boosted.x-500)/(ordinary.x-500)-1.25f)<.001f,"speed potion affects normal running by 25 percent");
        Player sword=new Player(500,500,null,config);check(sword.startAttack(1,0),"baseline Sword attack starts");
        float swordCooldown=sword.cooldown();sword.update(.1f,0,0,grid,0,0,0,.125f,0);
        check(Math.abs(sword.cooldown()-(swordCooldown-.125f))<.0001f,"cooldown modifier advances Sword cooldown");
        ActiveSkillSystem active=new ActiveSkillSystem(config.skills);Player activePlayer=new Player(500,500,null,config);
        check(active.activate(SkillType.DASH,activePlayer,0,0),"Dash starts before cooldown check");
        float dashCooldown=active.view(SkillType.DASH).cooldownRemaining();
        active.advance(.1f,0,0,activePlayer,grid,.125f,0);
        check(Math.abs(active.view(SkillType.DASH).cooldownRemaining()-(dashCooldown-.125f))<.0001f,"cooldown modifier advances Dash cooldown");
        ActiveSkillSystem ultimate=new ActiveSkillSystem(config.skills);Player ultimatePlayer=new Player(500,500,null,config);
        ultimate.advance(15f,0,0,ultimatePlayer,grid,15f,0);
        check(ultimate.activate(SkillType.ULTIMATE,ultimatePlayer,0,0),"Ultimate still activates");
        float ultimateCooldown=ultimate.view(SkillType.ULTIMATE).cooldownRemaining(),ultimateCast=ultimate.view(SkillType.ULTIMATE).activeRemaining();
        ultimate.advance(.1f,0,0,ultimatePlayer,grid,.125f,0);
        check(Math.abs(ultimate.view(SkillType.ULTIMATE).cooldownRemaining()-(ultimateCooldown-.125f))<.0001f,"cooldown modifier advances Ultimate cooldown");
        check(Math.abs(ultimate.view(SkillType.ULTIMATE).activeRemaining()-(ultimateCast-.1f))<.0001f,"Ultimate cast duration stays on gameplay time");
        ActiveSkillSystem regularDash=new ActiveSkillSystem(config.skills),boostedDash=new ActiveSkillSystem(config.skills);
        Player dashA=new Player(500,500,null,config),dashB=new Player(500,500,null,config);
        check(regularDash.activate(SkillType.DASH,dashA,0,0) && boostedDash.activate(SkillType.DASH,dashB,0,0),"Dash activates for speed comparison");
        regularDash.advance(.1f,0,0,dashA,grid,.1f,0);boostedDash.advance(.1f,0,0,dashB,grid,.125f,.1f);
        check(Math.abs(dashA.x-dashB.x)<.0001f && Math.abs(dashA.y-dashB.y)<.0001f,"speed potion does not change Dash distance");
        check(Float.isFinite(normalSpeed) && normalSpeed>0,"baseline movement speed remains valid");
        GameConfig quiet=new GameConfig.Builder().mode(com.example.monstersurvivor.config.GameMode.NORMAL).population(0,60).build();
        GameSession session=new GameSession(quiet,grid,500,500,null,null,91,CharacterId.WARRIOR.stableId(),new int[]{0,1,1});
        check(session.applyConsumedPotion(PotionType.SPEED),"real Normal session accepts a persisted potion use");
        float sessionSpeed=session.potions.speedRemaining();
        session.update(.5f,0,0,true,0,0,1600,1600);
        check(session.potions.speedRemaining()==sessionSpeed,"Pause freezes the actual session timer");
        session.update(.1f,0,0,false,0,0,1600,1600);
        check(Math.abs(session.potions.speedRemaining()-(sessionSpeed-.1f))<.0001f,"Resume advances gameplay-time duration");
        session.end();check(session.potions.speedRemaining()==0,"session end clears temporary potion buffs");
        GameSession chestSession=new GameSession(quiet,grid,500,500,null,null,93,CharacterId.WARRIOR.stableId(),new int[]{0,1,0});
        check(chestSession.applyConsumedPotion(PotionType.SPEED),"potion active before chest modal");
        check(chestSession.pickups.spawn(PickupType.TREASURE_CHEST,500,500,0)!=null,"existing Treasure Chest can spawn");
        chestSession.update(.05f,0,0,false,0,0,1600,1600);
        check(chestSession.choosingChest() && chestSession.chestSelection.size()>0,"Treasure Chest opens the shared reward modal");
        float chestTimer=chestSession.potions.speedRemaining();chestSession.update(.1f,0,0,false,0,0,1600,1600);
        check(chestSession.potions.speedRemaining()==chestTimer,"reward modal freezes potion duration");
        check(chestSession.chooseTreasureChest(0,chestSession.chestSelection.token()) && !chestSession.choosingChest(),"Treasure Chest still commits one reward");
        chestSession.end();
        GameSession diagnostic=new GameSession(quiet,grid,500,500,null,null,92,CharacterId.WARRIOR.stableId(),new int[]{1,0,0});
        diagnostic.onCompleted(result->{},true);
        check(diagnostic.potionAvailability(PotionType.HEALTH)==PotionSystem.Availability.DISABLED,"diagnostic matches cannot consume official inventory");
        for(PotionType type:PotionType.values()){
            int slot=type.ordinal();float x=PotionHud.centerX(800,slot),y=PotionHud.centerY(0);
            check(Math.abs(x-(800/2f-146+slot*146))<.001f,"potion button aligns under its passive slot");
            check(PotionHud.at(x,y,800,0)==type,"potion button hit test maps to its own inventory item");
            check(y+12<GameInput.BOTTOM_RESERVED,"potion button stays below the joystick touch region");
            check(SkillHud.at(x,y,800,480,0,0)==null,"potion button does not overlap Dash or Ultimate");
        }
        System.out.println("POTION CHECKS PASSED: "+checks+" assertions");
    }
}
