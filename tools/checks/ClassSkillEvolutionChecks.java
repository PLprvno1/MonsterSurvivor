import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.weapon.WeaponOffer;
import com.example.monstersurvivor.weapon.WeaponType;
import com.example.monstersurvivor.world.CollisionGrid;

/** Focused Task 13C contracts for class breakouts, chest eligibility and bounded combat effects. */
public final class ClassSkillEvolutionChecks {
    private static int checks;
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
    private static CollisionGrid grid(){return new CollisionGrid(100,100,16,new byte[10000]);}
    private static GameConfig config(){return new GameConfig.Builder().playerHp(100).population(0,40)
        .enemy(EnemyType.SLIME,2000,1,1).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();}
    private static GameSession session(String character){return new GameSession(config(),grid(),800,800,null,null,23,character);}
    private static GameSession warrior(){return session("warrior");}
    private static GameSession archer(){return session("archer");}
    private static GameSession mage(){return session("mage");}
    private static void commitEvolution(GameSession s,ClassSkillId id){
        GameStrings strings=s.config.strings;
        for(long seed=0;seed<300;seed++) {
            var selection=new TreasureChestSelection(strings,seed);
            if(!selection.open(8,s.weapons.loadout,s.player,s.skills,s.classSkills))continue;
            if(selection.branch()==TreasureChestSelection.Branch.PASSIVE)for(int i=0;i<selection.size();i++) {
                var offer=selection.get(i);
                if(offer.kind==TreasureChestOffer.Kind.EVOLUTION && offer.skill!=null && offer.skill.classSkill==id) {
                    check(selection.commit(i,selection.token(),s.weapons.loadout,s.player,s.skills,s.classSkills),id+" breakout selected from chest");
                    return;
                }
            }
            selection.clear();
        }
        throw new AssertionError("No eligible chest breakout offer for "+id);
    }
    private static void maxAndEvolve(GameSession s,ClassSkillId id){
        for(int level=0;level<5;level++)check(s.classSkills.upgrade(id,level),id+" upgrades to Lv"+(level+1));
        check(s.classSkills.level(id)==5 && s.classSkills.canEvolve(id),id+" becomes eligible at Lv5");commitEvolution(s,id);
        check(s.classSkills.evolved(id),id+" evolves after chest selection");
    }
    private static void resolve(GameSession s,float dt){s.classSkills.advance(dt);s.classSkills.resolve(dt,s.player,s.enemies,grid());}
    private static void catalogAndChest(){
        ClassSkillId[][] expected={
            {ClassSkillId.WARRIOR_WHIRLWIND,ClassSkillId.WARRIOR_SWORD_WAVE,ClassSkillId.WARRIOR_PARRY,ClassSkillId.WARRIOR_WAR_CRY},
            {ClassSkillId.ARCHER_MARK,ClassSkillId.ARCHER_FOCUS,ClassSkillId.ARCHER_RAIN,ClassSkillId.ARCHER_POISON_ARROW},
            {ClassSkillId.MAGE_ICE_SHARD,ClassSkillId.MAGE_ARCANE_TRAP,ClassSkillId.MAGE_SHIELD}
        };
        var characters=com.example.monstersurvivor.character.CharacterId.values();
        for(int c=0;c<expected.length;c++) {
            ClassSkillId[] actual=ClassSkillId.forCharacter(characters[c]);check(actual.length==expected[c].length,"class catalog size");
            for(int i=0;i<actual.length;i++)check(actual[i]==expected[c][i],"class catalog preserves ownership and evolution root");
        }
        for(GameLanguage language:GameLanguage.values()) {
            var strings=GameStrings.of(language);
            for(ClassSkillId[] group:expected)for(ClassSkillId id:group)
                check(!strings.evolvedClassSkillName(id).isBlank() && !strings.evolvedClassSkillDescription(id).isBlank(),"evolution name and detail localized: "+language+" "+id);
        }

        var s=warrior();check(s.classSkills.upgrade(ClassSkillId.WARRIOR_WHIRLWIND,0),"learn breakout root");
        for(int level=1;level<4;level++)check(s.classSkills.upgrade(ClassSkillId.WARRIOR_WHIRLWIND,level),"raise root to Lv"+(level+1));
        check(!s.classSkills.canEvolve(ClassSkillId.WARRIOR_WHIRLWIND),"Lv4 cannot unlock a breakout");
        check(s.classSkills.upgrade(ClassSkillId.WARRIOR_WHIRLWIND,4),"raise breakout root to Lv5");
        var strings=s.config.strings;
        TreasureChestSelection selected=null;int choice=-1;
        for(long seed=0;seed<300 && selected==null;seed++) {
            var candidate=new TreasureChestSelection(strings,seed);
            if(!candidate.open(8,s.weapons.loadout,s.player,s.skills,s.classSkills))continue;
            if(candidate.branch()==TreasureChestSelection.Branch.PASSIVE)for(int i=0;i<candidate.size();i++)
                if(candidate.get(i).kind==TreasureChestOffer.Kind.EVOLUTION && candidate.get(i).skill!=null
                    && candidate.get(i).skill.classSkill==ClassSkillId.WARRIOR_WHIRLWIND){selected=candidate;choice=i;break;}
            if(selected==null)candidate.clear();
        }
        check(selected!=null,"passive Treasure Chest offers the Lv5 class evolution");
        long token=selected.token();int slots=s.weapons.loadout.size()+s.classSkills.size();
        check(selected.commit(choice,token,s.weapons.loadout,s.player,s.skills,s.classSkills),"chest commits class evolution");
        check(!selected.commit(choice,token,s.weapons.loadout,s.player,s.skills,s.classSkills),"class evolution cannot be committed twice");
        check(s.classSkills.evolved(ClassSkillId.WARRIOR_WHIRLWIND) && s.classSkills.level(ClassSkillId.WARRIOR_WHIRLWIND)==5
            && s.weapons.loadout.size()+s.classSkills.size()==slots,"evolution keeps Lv5 and does not add a passive slot");
        check(!s.classSkills.canEvolve(ClassSkillId.WARRIOR_WHIRLWIND),"duplicate evolution rejected");
        for(int level=0;level<5;level++)check(s.classSkills.upgrade(ClassSkillId.WARRIOR_SWORD_WAVE,level),"second Warrior passive fills the last existing slot");
        s.classSkills.evolvedPassiveCount(()->3);
        check(!s.classSkills.canEvolve(ClassSkillId.WARRIOR_SWORD_WAVE),"shared three-evolution cap blocks a fourth class evolution");s.end();
        var mageSkills=new ClassSkillSystem(com.example.monstersurvivor.character.CharacterId.MAGE);
        check(ClassSkillId.forCharacter(com.example.monstersurvivor.character.CharacterId.MAGE).length==3
            && !mageSkills.canUpgrade(ClassSkillId.MAGE_CHANNEL),"Arcane Channel stays outside Mage reward catalog");
    }
    private static void legacyWeaponEvolutions(){
        for(WeaponType type:WeaponType.values()) {
            var s=switch(type){case SWORD->warrior();case BOW->archer();case FIREBALL,LIGHTNING->mage();};
            while(s.weapons.loadout.canGrant(type))s.weapons.loadout.grant(type);
            var offer=WeaponOffer.evolution(type,s.config.strings);
            check(s.weapons.loadout.get(type).level()==5 && offer.valid(s.weapons.loadout,s.player),"legacy Lv5 evolution remains eligible: "+type);
            check(offer.apply(s.weapons.loadout,s.player) && s.weapons.loadout.get(type).evolved()
                && s.weapons.loadout.get(type).level()==5,"legacy evolution retains its existing state: "+type);
            check(!s.config.strings.evolvedName(type).isBlank() && !s.config.strings.evolutionDescription(type).isBlank(),"legacy evolution keeps localized HUD copy: "+type);
            s.end();
        }
    }
    private static void whirlwind(){
        var s=warrior();maxAndEvolve(s,ClassSkillId.WARRIOR_WHIRLWIND);var target=s.enemies.spawn(EnemyType.SLIME,820,800);int hp=target.hp();
        resolve(s,.01f);check(target.hp()==hp-3 && s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WHIRLWIND)==5,"first evolved pulse uses Lv5 damage and cooldown");
        float cd=s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WHIRLWIND);s.update(1,0,0,true,0,0,1280,1280);
        check(target.hp()==hp-3 && s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WHIRLWIND)==cd,"pause freezes delayed pulse and cooldown");
        s.classSkills.advance(.27f);s.classSkills.resolve(.27f,s.player,s.enemies,grid());check(target.hp()==hp-3,"second pulse waits 0.28 seconds");
        s.classSkills.advance(.02f);s.classSkills.resolve(.02f,s.player,s.enemies,grid());
        check(target.hp()==hp-5 && !s.classSkills.projectile(0).active,"second pulse deals floor(75% Lv5 damage) once");s.end();
    }
    private static void swordWave(){
        var s=warrior();maxAndEvolve(s,ClassSkillId.WARRIOR_SWORD_WAVE);var target=s.enemies.spawn(EnemyType.SLIME,820,800);int hp=target.hp();
        resolve(s,.01f);check(s.classSkills.projectileCount()==3,"breakout fires three simultaneous straight waves");
        s.classSkills.advance(.2f);s.classSkills.resolve(.2f,s.player,s.enemies,grid());
        check(s.classSkills.waveHits==2 && target.hp()==hp-6,"shared volley ledger caps a target at two Lv5 hits");s.end();
    }
    private static void parry(){
        var s=warrior();maxAndEvolve(s,ClassSkillId.WARRIOR_PARRY);var target=s.enemies.spawn(EnemyType.SLIME,800,820);int hp=target.hp(),playerHp=s.player.hp();
        check(!s.player.damageDirect(1,800,820) && s.player.hp()==playerHp,"evolved Parry blocks a rear direct hit");
        check(target.hp()==hp-4 && s.classSkills.parryBlocks==1 && s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_PARRY)==10,"full-circle block counters at Lv5 radius and damage");s.end();
    }
    private static void warCry(){
        var s=warrior();maxAndEvolve(s,ClassSkillId.WARRIOR_WAR_CRY);var first=s.enemies.spawn(EnemyType.SLIME,810,800);
        s.enemies.spawn(EnemyType.SLIME,820,800);s.enemies.spawn(EnemyType.SLIME,830,800);s.enemies.spawn(EnemyType.SLIME,840,800);int hp=first.hp();
        resolve(s,.01f);
        check(s.classSkills.warCryActive() && s.classSkills.warCryRemaining()==7 && s.classSkills.cooldownRemaining(ClassSkillId.WARRIOR_WAR_CRY)==14,"War Cry breakout adds one second but keeps cooldown");
        check(first.hp()==hp-3 && s.classSkills.warCryKnockbacks>=4,"extra pulse is bounded knockback without duplicate AoE damage");s.end();
    }
    private static void hunterMark(){
        var s=archer();maxAndEvolve(s,ClassSkillId.ARCHER_MARK);var first=s.enemies.spawn(EnemyType.SLIME,820,800);var second=s.enemies.spawn(EnemyType.SLIME,850,800);
        resolve(s,.01f);check(s.classSkills.markedTargetId()==first.id && s.classSkills.markChargesRemaining()==5,"Lv5 Hunter Mark begins with five hits");
        first.damage(5000);resolve(s,.01f);
        check(s.classSkills.markedTargetId()==second.id && s.classSkills.markChargesRemaining()==2 && s.classSkills.markTransfers==1,"mark transfers once and caps remaining charges at two");
        second.damage(5000);resolve(s,.01f);check(!s.classSkills.marked() && s.classSkills.markTransfers==1,"transferred mark cannot chain again");s.end();
    }
    private static void focusShot(){
        var s=archer();maxAndEvolve(s,ClassSkillId.ARCHER_FOCUS);var first=s.enemies.spawn(EnemyType.SLIME,830,800);var second=s.enemies.spawn(EnemyType.SLIME,850,800);
        resolve(s,.01f);s.classSkills.advance(.25f);s.classSkills.resolve(.25f,s.player,s.enemies,grid());
        check(first.hp()==1994 && second.hp()==1996 && s.classSkills.focusHits==2,"Focus Shot pierces two; second target receives 70% damage");s.end();
    }
    private static void arrowRain(){
        var s=archer();maxAndEvolve(s,ClassSkillId.ARCHER_RAIN);var target=s.enemies.spawn(EnemyType.SLIME,820,800);s.enemies.spawn(EnemyType.SLIME,830,800);s.enemies.spawn(EnemyType.SLIME,840,800);int hp=target.hp();
        resolve(s,.01f);for(int i=0;i<7;i++){s.classSkills.advance(.2f);s.classSkills.resolve(.2f,s.player,s.enemies,grid());}
        check(s.classSkills.rainPulses==5 && target.hp()==hp-3,"Lv5 Heavenly Rain adds one pulse and caps accepted hits at three");s.end();
    }
    private static void poisonArrow(){
        var s=archer();maxAndEvolve(s,ClassSkillId.ARCHER_POISON_ARROW);var first=s.enemies.spawn(EnemyType.SLIME,820,800);
        var second=s.enemies.spawn(EnemyType.SLIME,830,800);var third=s.enemies.spawn(EnemyType.SLIME,840,800);var fourth=s.enemies.spawn(EnemyType.SLIME,850,800);
        resolve(s,.01f);s.classSkills.advance(.2f);s.classSkills.resolve(.2f,s.player,s.enemies,grid());
        check(first.poisoned() && s.classSkills.poisonApplications==1,"Poison Arrow applies one owned status on accepted hit");
        first.damage(5000);
        check(second.poisoned() && third.poisoned() && !fourth.poisoned() && s.classSkills.poisonSpreads==2,"poisoned death spreads to two nearest targets");
        second.damage(5000);check(!fourth.poisoned() && s.classSkills.poisonSpreads==2,"secondary poison cannot chain");s.end();
    }
    private static void iceShard(){
        var s=mage();maxAndEvolve(s,ClassSkillId.MAGE_ICE_SHARD);var first=s.enemies.spawn(EnemyType.SLIME,830,800);first.damage(1997);
        var center=s.enemies.spawn(EnemyType.SLIME,844,802);var upper=s.enemies.spawn(EnemyType.SLIME,839,809);var lower=s.enemies.spawn(EnemyType.SLIME,839,795);
        resolve(s,.01f);s.classSkills.advance(.2f);s.classSkills.resolve(.2f,s.player,s.enemies,grid());
        check(s.classSkills.iceShardSplinters==3 && s.classSkills.projectileCount()==3,"accepted Ice Shard creates three bounded splinters");
        s.classSkills.advance(.12f);s.classSkills.resolve(.12f,s.player,s.enemies,grid());
        check(center.hp()<2000 && upper.hp()<2000 && lower.hp()<2000 && center.movementMultiplier()<1 && first.hp()<2000,"splinters deal half damage and apply half slow without recursing");s.end();
    }
    private static void arcaneTrap(){
        var s=mage();maxAndEvolve(s,ClassSkillId.MAGE_ARCANE_TRAP);var target=s.enemies.spawn(EnemyType.SLIME,820,800);int hp=target.hp();
        resolve(s,.01f);s.classSkills.advance(.5f);s.classSkills.resolve(.5f,s.player,s.enemies,grid());
        check(target.hp()==hp-4 && s.classSkills.trapCount()==1 && s.classSkills.trapPulseHits==1,"first Lv5 trap pulse hits once and retains its bounded slot");
        s.classSkills.advance(.34f);s.classSkills.resolve(.34f,s.player,s.enemies,grid());check(target.hp()==hp-4,"second pulse waits 0.35 seconds");
        s.classSkills.advance(.02f);s.classSkills.resolve(.02f,s.player,s.enemies,grid());
        check(target.hp()==hp-8 && s.classSkills.trapPulseHits==2 && s.classSkills.trapCount()==0,"second trap pulse hits once and releases its slot");s.end();
    }
    private static void magicShield(){
        var s=mage();maxAndEvolve(s,ClassSkillId.MAGE_SHIELD);var target=s.enemies.spawn(EnemyType.SLIME,820,800);int hp=target.hp(),playerHp=s.player.hp();
        check(s.classSkills.cooldownRemaining(ClassSkillId.MAGE_SHIELD)==0,"Magic Shield starts ready");
        check(!s.player.damageDirect(1,800,820) && s.player.hp()==playerHp,"Magic Shield blocks an incoming direct hit");
        check(target.hp()==hp-1 && s.classSkills.shieldPulseHits==1 && s.classSkills.cooldownRemaining(ClassSkillId.MAGE_SHIELD)==12,"successful block emits radius-30 damage-1 shockwave and starts cooldown");
        check(s.player.damage(1) && s.classSkills.shieldBlocks==1,"non-direct damage does not consume Magic Shield");s.end();
    }
    public static void main(String[] args){
        catalogAndChest();legacyWeaponEvolutions();whirlwind();swordWave();parry();warCry();hunterMark();focusShot();arrowRain();poisonArrow();iceShard();arcaneTrap();magicShield();
        System.out.println("ClassSkillEvolutionChecks PASS "+checks+" assertions");
    }
}
