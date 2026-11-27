import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.entity.EnemyType;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.localization.GameLanguage;
import com.example.monstersurvivor.localization.GameStrings;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.weapon.WeaponType;
import com.example.monstersurvivor.world.CollisionGrid;

/** Short logic checks for the Archer skills; no renderer, emulator or long simulation. */
public final class ArcherSkillChecks {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static CollisionGrid open(){return new CollisionGrid(80,80,16,new byte[6400]);}
    private static GameConfig config(){return new GameConfig.Builder().playerHp(100).population(0,40)
        .enemy(EnemyType.SLIME,100,1,1).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();}
    private static GameSession archer(){return new GameSession(config(),open(),600,600,null,null,42,"archer");}
    private static void ownershipAndOffers(){
        var s=archer();check(s.weapons.loadout.size()==1 && s.weapons.loadout.slot(0).type==WeaponType.BOW && s.weapons.loadout.slot(0).level()==1,"Archer keeps Bow Lv1 as starter");
        var selection=new SkillSelection(GameStrings.of(GameLanguage.EN),11);
        check(selection.open(1,s.skills,s.classSkills),"Archer Skill Box opens");
        for(int i=0;i<selection.size();i++)if(selection.get(i).classSkill!=null)
            check(selection.get(i).classSkill.owner==com.example.monstersurvivor.character.CharacterId.ARCHER,"offers only Archer class skills");
        check(s.classSkills.canUpgrade(ClassSkillId.ARCHER_POISON_ARROW)
            && !s.classSkills.canUpgrade(ClassSkillId.WARRIOR_WAR_CRY),"Poison Arrow is Archer-only");
        check(s.classSkills.upgrade(ClassSkillId.ARCHER_MARK,0) && s.classSkills.upgrade(ClassSkillId.ARCHER_FOCUS,0)
            && !s.classSkills.canUpgrade(ClassSkillId.ARCHER_RAIN),"two learned skills fill class slots");s.end();
    }
    private static void tuning(){
        float[] markCd={12,11,10,9,8},markDuration={3,3.5f,4,4.5f,5},focusCd={9,8.5f,8,7.5f,7},rainCd={17,16,15,14,13},radius={28,30,32,34,36};
        int[] charges={1,2,3,4,5},focusDamage={3,3,4,5,6},pulses={3,3,3,4,4};
        float[] poisonCd={13,12,11,10,9},poisonDuration={2,2,2.5f,2.5f,3};int[] poisonDamage={1,1,2,2,3},poisonTicks={2,2,2,2,3};
        for(int i=1;i<=5;i++){
            check(ClassSkillTuning.cooldown(ClassSkillId.ARCHER_MARK,i)==markCd[i-1] && ClassSkillTuning.markDuration(i)==markDuration[i-1] && ClassSkillTuning.markCharges(i)==charges[i-1],"Hunter's Mark Lv"+i);
            check(ClassSkillTuning.cooldown(ClassSkillId.ARCHER_FOCUS,i)==focusCd[i-1] && ClassSkillTuning.damage(ClassSkillId.ARCHER_FOCUS,i)==focusDamage[i-1],"Focus Shot Lv"+i);
            check(ClassSkillTuning.cooldown(ClassSkillId.ARCHER_RAIN,i)==rainCd[i-1] && ClassSkillTuning.rainPulses(i)==pulses[i-1] && ClassSkillTuning.rainRadius(i)==radius[i-1],"Arrow Rain Lv"+i);
            check(ClassSkillTuning.cooldown(ClassSkillId.ARCHER_POISON_ARROW,i)==poisonCd[i-1]
                && ClassSkillTuning.damage(ClassSkillId.ARCHER_POISON_ARROW,i)==poisonDamage[i-1]
                && ClassSkillTuning.poisonDuration(i)==poisonDuration[i-1] && ClassSkillTuning.poisonTicks(i)==poisonTicks[i-1],"Poison Arrow Lv"+i);
        }
        check(ClassSkillTuning.poisonArrowRange()==150 && ClassSkillTuning.poisonArrowSpeed()==165
            && ClassSkillTuning.poisonTickDamage()==1,"Poison Arrow flight and DoT tuning");
    }
    private static void markBowHit(){
        var s=archer();check(s.classSkills.upgrade(ClassSkillId.ARCHER_MARK,0),"learn Mark");
        Enemy target=s.enemies.spawn(EnemyType.SLIME,640,600);int hp=target.hp();
        s.classSkills.resolve(.05f,s.player,s.enemies,open());
        check(s.classSkills.markedTargetId()==target.id && s.classSkills.markChargesRemaining()==1,"Mark chooses visible target");
        check(s.weapons.projectiles.shoot(WeaponType.BOW,600,600,target.x,target.y,1,s.config.weapons.level(WeaponType.BOW,1)),"Bow projectile accepted");
        s.weapons.projectiles.update(.2f,s.enemies,open());
        check(target.hp()==hp-2 && !s.classSkills.marked() && s.classSkills.markBonusHits==1,"accepted direct Bow hit gains and consumes Mark bonus");s.end();
    }
    private static void focusShot(){
        var s=archer();check(s.classSkills.upgrade(ClassSkillId.ARCHER_MARK,0) && s.classSkills.upgrade(ClassSkillId.ARCHER_FOCUS,0),"learn Mark and Focus Shot");
        Enemy target=s.enemies.spawn(EnemyType.SLIME,650,600);int hp=target.hp();
        s.classSkills.resolve(.01f,s.player,s.enemies,open());
        check(s.classSkills.focusFired==1 && s.classSkills.cooldownRemaining(ClassSkillId.ARCHER_FOCUS)==9,"Focus Shot fires with exact cooldown");
        for(int i=0;i<5 && s.classSkills.focusHits==0;i++){s.classSkills.advance(.1f);s.classSkills.resolve(.1f,s.player,s.enemies,open());}
        check(s.classSkills.focusHits==1 && target.hp()==hp-4*s.player.stats.damage(),"fixed Focus Shot receives Mark bonus on direct hit");
        check(s.classSkills.markBonusHits==1 && !s.classSkills.marked(),"Focus Shot consumes accepted Mark charge");s.end();
    }
    private static void arrowRain(){
        var s=archer();check(s.classSkills.upgrade(ClassSkillId.ARCHER_RAIN,0),"learn Arrow Rain");
        Enemy target=s.enemies.spawn(EnemyType.SLIME,640,600);s.enemies.spawn(EnemyType.SLIME,650,600);s.enemies.spawn(EnemyType.SLIME,660,600);
        int hp=target.hp();s.classSkills.resolve(.01f,s.player,s.enemies,open());
        check(s.classSkills.rainActivations==1 && target.hp()==hp && s.classSkills.cooldownRemaining(ClassSkillId.ARCHER_RAIN)==17,"Arrow Rain snapshots cluster, warns and starts exact cooldown");
        s.classSkills.advance(.39f);s.classSkills.resolve(.39f,s.player,s.enemies,open());
        check(s.classSkills.rainPulses==0 && target.hp()==hp,"warning lasts 0.4 seconds");
        for(int i=0;i<5;i++){s.classSkills.advance(.1f);s.classSkills.resolve(.1f,s.player,s.enemies,open());}
        check(s.classSkills.rainPulses==3 && target.hp()==hp-2*s.player.stats.damage(),"three rain pulses cap accepted hits at two per target");
        s.end();
    }
    private static void poisonArrow(){
        var s=archer();check(s.classSkills.upgrade(ClassSkillId.ARCHER_POISON_ARROW,0),"learn Poison Arrow");
        Enemy target=s.enemies.spawn(EnemyType.SLIME,640,600);int hp=target.hp();
        s.classSkills.resolve(.01f,s.player,s.enemies,open());
        check(s.classSkills.poisonFired==1 && s.classSkills.cooldownRemaining(ClassSkillId.ARCHER_POISON_ARROW)==13,
            "Poison Arrow auto-fires and starts its cooldown");
        var shot=s.classSkills.projectile(0);check(shot.active && shot.poison && shot.speed==165 && shot.range==150,"Poison Arrow uses the bounded straight-arrow pool");
        s.classSkills.advance(.3f);s.classSkills.resolve(.3f,s.player,s.enemies,open());
        check(target.hp()==hp-1 && target.poisoned() && target.poisonRemaining()==2 && s.classSkills.poisonApplications==1,
            "accepted arrow hit applies one finite Enemy poison state");
        float cooldown=s.classSkills.cooldownRemaining(ClassSkillId.ARCHER_POISON_ARROW);int afterHit=target.hp();
        s.update(.5f,0,0,true,0,0,1280,1280);
        check(target.poisonRemaining()==2 && s.classSkills.cooldownRemaining(ClassSkillId.ARCHER_POISON_ARROW)==cooldown,
            "Pause freezes poison and skill cooldown");
        target.advance(1);
        check(target.hp()==afterHit-1 && target.poisonRemaining()==1,"Poison deals its first one-second tick");
        target.advance(1);
        check(target.hp()==afterHit-2 && !target.poisoned(),"Poison deals exactly two Lv1 ticks and expires");
        Enemy refreshed=s.enemies.spawn(EnemyType.SLIME,660,600);int refreshedHp=refreshed.hp();
        check(refreshed.applyPoison(1,2,2),"apply initial poison for refresh check");refreshed.advance(.5f);
        check(refreshed.applyPoison(2,2.5f,2) && refreshed.poisonRemaining()==2.5f,"reapplication refreshes one state with new power and duration");
        refreshed.advance(1);check(refreshed.hp()==refreshedHp-2,"refreshed poison uses updated damage without stacking entities");s.end();
    }
    public static void main(String[] args){tuning();ownershipAndOffers();markBowHit();focusShot();arrowRain();poisonArrow();System.out.println("ArcherSkillChecks PASS "+checks+" assertions");}
}
