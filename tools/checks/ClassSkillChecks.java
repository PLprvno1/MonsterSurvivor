import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.ai.NavigationGrid;
import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.localization.GameStrings;
import com.example.monstersurvivor.localization.GameLanguage;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.world.CollisionGrid;

/** Focused contracts for Task 12B2a only. No desktop, Android or long simulation. */
public final class ClassSkillChecks {
    private static int checks;
    private static void check(boolean condition,String label){if(!condition)throw new AssertionError(label);checks++;}
    private static CollisionGrid open(){return new CollisionGrid(80,80,16,new byte[6400]);}
    private static GameConfig config(){return new GameConfig.Builder().playerHp(100).population(0,40)
        .enemy(EnemyType.SLIME,100,1,1).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();}
    private static GameSession session(){return new GameSession(config(),open(),600,600,null,null,42);}
    private static void tuningAndSlots(){
        float[] cd1={7,6.5f,6,5.5f,5},radii={30,34,38,42,46},cd2={9,8.5f,8,7.5f,7},cd3={14,13,12,11,10};
        float[] cryCd={18,17,16,15,14},cryDuration={4,4.5f,5,5.5f,6},cryRadius={60,60,70,70,70};
        int[] damage={1,1,2,2,3},parryDamage={2,2,3,3,4},pierce={2,2,3,3,4};
        int[] cryDamage={1,2,2,2,3},cryReduction={10,12,14,16,18},cryKnockback={8,8,8,8,12};
        for(int i=1;i<=5;i++) {
            check(ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_WHIRLWIND,i)==cd1[i-1],"W1 cooldown Lv"+i);
            check(ClassSkillTuning.whirlwindRadius(i)==radii[i-1] && ClassSkillTuning.damage(ClassSkillId.WARRIOR_WHIRLWIND,i)==damage[i-1],"W1 tuning Lv"+i);
            check(ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_SWORD_WAVE,i)==cd2[i-1] && ClassSkillTuning.wavePierce(i)==pierce[i-1],"W2 tuning Lv"+i);
            check(ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_PARRY,i)==cd3[i-1] && ClassSkillTuning.damage(ClassSkillId.WARRIOR_PARRY,i)==parryDamage[i-1],"W3 tuning Lv"+i);
            check(ClassSkillTuning.cooldown(ClassSkillId.WARRIOR_WAR_CRY,i)==cryCd[i-1]
                && ClassSkillTuning.damage(ClassSkillId.WARRIOR_WAR_CRY,i)==cryDamage[i-1]
                && ClassSkillTuning.warCryDuration(i)==cryDuration[i-1] && ClassSkillTuning.warCryRadius(i)==cryRadius[i-1]
                && ClassSkillTuning.warCryKnockback(i)==cryKnockback[i-1]
                && ClassSkillTuning.warCryDamageReductionPercent(i)==cryReduction[i-1],"War Cry tuning Lv"+i);
        }
        ClassSkillSystem skills=new ClassSkillSystem(CharacterId.WARRIOR);
        check(skills.upgrade(ClassSkillId.WARRIOR_WHIRLWIND,0) && skills.upgrade(ClassSkillId.WARRIOR_SWORD_WAVE,0),"learn first two class slots");
        check(!skills.canUpgrade(ClassSkillId.WARRIOR_PARRY),"third class skill blocked at two slots");
        check(!new ClassSkillSystem(CharacterId.ARCHER).canUpgrade(ClassSkillId.WARRIOR_WHIRLWIND)
            && !new ClassSkillSystem(CharacterId.MAGE).canUpgrade(ClassSkillId.WARRIOR_WHIRLWIND)
            && !new ClassSkillSystem(CharacterId.ARCHER).canUpgrade(ClassSkillId.WARRIOR_WAR_CRY)
            && new ClassSkillSystem(CharacterId.WARRIOR).canUpgrade(ClassSkillId.WARRIOR_WAR_CRY)
            && new ClassSkillSystem(CharacterId.MAGE).hasUpgrade(),"no cross-class offers; Mage skills are enabled");
    }
    private static void selection(){
        var active=new ActiveSkillSystem(new SkillConfig());var classes=new ClassSkillSystem(CharacterId.WARRIOR);
        var selection=new SkillSelection(GameStrings.of(GameLanguage.EN),919);
        check(selection.open(17,active,classes) && selection.size()==3,"Skill Box exposes up to three offers");
        int classOffers=0,activeOffers=0;for(int i=0;i<selection.size();i++){var offer=selection.get(i);if(offer.classSkill!=null)classOffers++;else activeOffers++;}
        check(classOffers>=1 && activeOffers>=1,"new class skill and Dash/Ultimate remain represented");
        long token=selection.token();int at=-1;ClassSkillId selected=null;
        for(int i=0;i<selection.size();i++)if(selection.get(i).classSkill!=null){at=i;selected=selection.get(i).classSkill;break;}
        check(at>=0 && selected!=null,"Warrior candidate available");
        check(selection.commit(at,token,active,classes) && classes.level(selected)==1,"class offer commits once");
        check(!selection.commit(at,token,active,classes),"stale skill token rejected");
        var mage=new SkillSelection(GameStrings.of(GameLanguage.EN),919);var mageSkills=new ClassSkillSystem(CharacterId.MAGE);
        check(mage.open(18,active,mageSkills),"Mage retains global skill choices");
        for(int i=0;i<mage.size();i++)if(mage.get(i).classSkill!=null)
            check(mage.get(i).classSkill.owner==CharacterId.MAGE,"Mage receives only Mage skills");
    }
    private static void whirlwindAndPause(){
        var s=session();check(s.classSkills.upgrade(ClassSkillId.WARRIOR_WHIRLWIND,0),"learn Whirlwind");
        var enemy=s.enemies.spawn(EnemyType.SLIME,620,600);check(enemy!=null,"spawn W1 target");int hp=enemy.hp();
        s.update(.05f,0,0,true,0,0,1280,1280);check(s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WHIRLWIND)==0 && enemy.hp()==hp,"Pause freezes class clocks and attacks");
        s.update(.05f,0,0,false,0,0,1280,1280);
        check(enemy.hp()==hp-1 && s.classSkills.whirlwindActivations==1,"Whirlwind hits in radius once");
        check(s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WHIRLWIND)==7,"Whirlwind starts exact cooldown");
        s.update(1,0,0,true,0,0,1280,1280);check(s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WHIRLWIND)==7,"Pause holds active cooldown");s.end();
    }
    private static void wavePierces(){
        var grid=open();var player=new Player(600,600,null,config());var enemies=new EnemySystem(new NavigationGrid(grid),null,config());
        var one=enemies.spawn(EnemyType.SLIME,625,600);var two=enemies.spawn(EnemyType.SLIME,655,600);int oneHp=one.hp(),twoHp=two.hp();
        var skills=new ClassSkillSystem(CharacterId.WARRIOR);check(skills.upgrade(ClassSkillId.WARRIOR_SWORD_WAVE,0),"learn Sword Wave");
        skills.resolve(.05f,player,enemies,grid);check(skills.waveFired==1 && skills.cooldownRemaining(ClassSkillId.WARRIOR_SWORD_WAVE)==9,"Wave fires once and starts cooldown");
        for(int i=0;i<8;i++)skills.resolve(.1f,player,enemies,grid);
        check(skills.waveHits==2 && one.hp()<oneHp && two.hp()<twoHp,"non-homing wave pierces two distinct targets");
    }
    private static void parryBoundary(){
        var front=session();front.classSkills.upgrade(ClassSkillId.WARRIOR_PARRY,0);
        var target=front.enemies.spawn(EnemyType.SLIME,600,580);int targetHp=target.hp(),playerHp=front.player.hp();
        check(!front.player.damageDirect(2,600,580) && front.player.hp()==playerHp,"front direct strike is parried");
        check(target.hp()<targetHp && front.classSkills.parryBlocks==1 && front.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_PARRY)==14,"counter hits and starts cooldown");front.end();
        var rear=session();rear.classSkills.upgrade(ClassSkillId.WARRIOR_PARRY,0);int hp=rear.player.hp();
        check(rear.player.damageDirect(1,600,620) && rear.player.hp()==hp-1 && rear.classSkills.parryBlocks==0,"rear strike is not parried");rear.end();
        var dash=session();dash.classSkills.upgrade(ClassSkillId.WARRIOR_PARRY,0);dash.activateSkill(SkillType.DASH,0,-1,false);hp=dash.player.hp();
        check(!dash.player.damageDirect(1,600,580) && dash.player.hp()==hp && dash.classSkills.parryBlocks==0,"Dash iframe does not spend Parry");dash.end();
        var dot=session();dot.classSkills.upgrade(ClassSkillId.WARRIOR_PARRY,0);hp=dot.player.hp();
        check(dot.player.damage(1) && dot.player.hp()==hp-1 && dot.classSkills.parryBlocks==0,"non-direct damage bypasses Parry");dot.end();
    }
    private static void warCry(){
        var s=session();check(s.classSkills.upgrade(ClassSkillId.WARRIOR_WAR_CRY,0)
            && s.classSkills.upgrade(ClassSkillId.WARRIOR_WHIRLWIND,0),"learn War Cry and a direct passive attack");
        var target=s.enemies.spawn(EnemyType.SLIME,600,600);s.enemies.spawn(EnemyType.SLIME,640,600);
        s.enemies.spawn(EnemyType.SLIME,645,610);s.enemies.spawn(EnemyType.SLIME,650,590);int hp=target.hp();
        s.classSkills.resolve(.05f,s.player,s.enemies,open());
        check(s.classSkills.warCryActivations==1 && s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WAR_CRY)==18
            && s.classSkills.warCryActive(),"War Cry triggers on four visible enemies and starts cooldown/buff");
        check(target.hp()==hp-3 && s.classSkills.whirlwindActivations==1,"War Cry pulse and following Warrior passive get direct bonus");
        check(s.classSkills.warCryKnockbacks>0,"War Cry pushes ordinary hit targets");
        float remaining=s.classSkills.warCryRemaining();s.update(.5f,0,0,true,0,0,1280,1280);
        check(s.classSkills.warCryRemaining()==remaining,"Pause freezes War Cry buff duration");
        int playerHp=s.player.hp();check(s.player.damage(1) && s.player.hp()==playerHp-1,"War Cry does not reduce non-direct damage");
        s.player.update(.71f,0,0,open());
        playerHp=s.player.hp();check(s.player.damageDirect(10,600,580) && s.player.hp()==playerHp-9,"War Cry reduces direct damage but does not block it");s.end();
    }
    public static void main(String[] args){tuningAndSlots();selection();whirlwindAndPause();wavePierces();parryBoundary();warCry();System.out.println("ClassSkillChecks PASS "+checks+" assertions");}
}
