import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.entity.EnemyType;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.localization.GameLanguage;
import com.example.monstersurvivor.localization.GameStrings;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.weapon.WeaponType;
import com.example.monstersurvivor.world.CollisionGrid;

/** Focused Task 12B2c mechanics checks; no renderer, emulator or long simulation. */
public final class MageSkillChecks {
    private static int checks;
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    private static CollisionGrid open(){return new CollisionGrid(80,80,16,new byte[6400]);}
    private static GameConfig config(){return new GameConfig.Builder().playerHp(100).population(0,40)
        .enemy(EnemyType.SLIME,100,1,1).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();}
    private static GameSession mage(){return new GameSession(config(),open(),600,600,null,null,42,"mage");}
    private static void tick(GameSession s,float dt){s.classSkills.advance(dt);s.classSkills.resolve(dt,s.player,s.enemies,open());}
    private static void ownershipAndTuning(){
        var s=mage();
        check(s.character.id==CharacterId.MAGE && s.weapons.loadout.size()==1
            && s.weapons.loadout.slot(0).type==WeaponType.FIREBALL && s.weapons.loadout.slot(0).level()==1,
            "Mage keeps Fireball Lv1 starter");
        var selection=new SkillSelection(GameStrings.of(GameLanguage.EN),17);
        check(selection.open(1,s.skills,s.classSkills),"Mage Skill Box opens");boolean mageOffer=false;
        for(int i=0;i<selection.size();i++)if(selection.get(i).classSkill!=null) {
            check(selection.get(i).classSkill.owner==CharacterId.MAGE,"Skill Box only offers Mage class skills");mageOffer=true;
        }
        check(mageOffer,"Mage Skill Box includes a class skill");s.end();
        float[] iceCd={7.5f,7,6.5f,6,5.5f},iceDuration={1.5f,1.7f,1.9f,2.1f,2.3f};
        int[] iceDamage={1,1,2,2,3},iceSlow={20,22,25,27,30};
        float[] trapCd={14,13,12,11,10},trapRadius={25,28,31,34,37};int[] trapDamage={2,2,3,3,4};
        float[] shieldCd={20,18,16,14,12},channelCd={18,17,16,15,14},channelDuration={3,3.5f,4,4.5f,5};
        float[] fireballMultiplier={.90f,.85f,.80f,.75f,.70f};
        for(int level=1;level<=5;level++) {
            int i=level-1;
            check(ClassSkillTuning.cooldown(ClassSkillId.MAGE_ICE_SHARD,level)==iceCd[i]
                && ClassSkillTuning.damage(ClassSkillId.MAGE_ICE_SHARD,level)==iceDamage[i]
                && ClassSkillTuning.iceSlowPercent(level)==iceSlow[i]
                && ClassSkillTuning.iceSlowDuration(level)==iceDuration[i],"Ice Shard Lv"+level+" tuning");
            check(ClassSkillTuning.cooldown(ClassSkillId.MAGE_ARCANE_TRAP,level)==trapCd[i]
                && ClassSkillTuning.damage(ClassSkillId.MAGE_ARCANE_TRAP,level)==trapDamage[i]
                && ClassSkillTuning.trapExplosionRadius(level)==trapRadius[i],"Arcane Trap Lv"+level+" tuning");
            check(ClassSkillTuning.cooldown(ClassSkillId.MAGE_SHIELD,level)==shieldCd[i]
                && ClassSkillTuning.cooldown(ClassSkillId.MAGE_CHANNEL,level)==channelCd[i]
                && ClassSkillTuning.channelDuration(level)==channelDuration[i]
                && ClassSkillTuning.channelFireballCooldownMultiplier(level)==fireballMultiplier[i],"Shield/Channel Lv"+level+" tuning");
        }
        check(ClassSkillTuning.trapCapacity()==2 && ClassSkillTuning.iceRange()==150
            && ClassSkillTuning.iceSpeed()==160 && ClassSkillTuning.trapArmDelay()==.5f
            && ClassSkillTuning.trapLifetime()==3.5f && ClassSkillTuning.trapTriggerRadius()==19
            && ClassSkillTuning.shieldEffectDuration()==.35f && ClassSkillTuning.channelRange()==130,
            "Mage skill shared range and lifetime tuning");
        var skills=new ClassSkillSystem(CharacterId.MAGE);
        check(skills.upgrade(ClassSkillId.MAGE_ICE_SHARD,0) && skills.upgrade(ClassSkillId.MAGE_ARCANE_TRAP,0)
            && !skills.canUpgrade(ClassSkillId.MAGE_SHIELD),"Mage remains limited to two learned class skills");
        check(!new ClassSkillSystem(CharacterId.WARRIOR).canUpgrade(ClassSkillId.MAGE_ICE_SHARD)
            && !new ClassSkillSystem(CharacterId.ARCHER).canUpgrade(ClassSkillId.MAGE_CHANNEL),"Mage skills are class locked");
    }
    private static void iceShardAndBossImmunity(){
        var s=mage();check(s.classSkills.upgrade(ClassSkillId.MAGE_ICE_SHARD,0),"learn Ice Shard");
        Enemy target=s.enemies.spawn(EnemyType.SLIME,620,600);int hp=target.hp();tick(s,.01f);
        check(s.classSkills.iceFired==1 && s.classSkills.cooldownRemaining(ClassSkillId.MAGE_ICE_SHARD)==7.5f,
            "Ice Shard auto-fires and starts cooldown");
        var shot=s.classSkills.projectile(0);
        check(shot.active && shot.ice && shot.speed==160 && shot.range==150,"Ice Shard is a straight pooled projectile");
        tick(s,.1f);
        check(target.hp()==hp-s.player.stats.damage() && s.classSkills.iceHits==1 && s.classSkills.iceSlows==1,
            "Ice Shard direct hit damages and applies enemy slow");
        check(target.slowRemaining()==1.5f && Math.abs(target.movementMultiplier()-.8f)<.001f,
            "Ice slow uses its level values on Enemy");
        check(target.applySlow(30,.5f) && target.slowRemaining()==.5f
            && Math.abs(target.movementMultiplier()-.7f)<.001f,"reapplying slow refreshes duration without stacking");s.end();

        var bossSession=mage();bossSession.classSkills.upgrade(ClassSkillId.MAGE_ICE_SHARD,0);
        var boss=bossSession.enemies.spawnBoss(bossSession.config.bosses.lich.body(),
            bossSession.config.bosses.stats(bossSession.config.bosses.lich,bossSession.config.difficulty,bossSession.config.mode),640,600);
        check(boss!=null,"spawn boss for Ice Shard immunity check");hp=boss.hp();tick(bossSession,.01f);
        check(bossSession.classSkills.iceFired==1,"Ice Shard targets boss in range");tick(bossSession,.25f);
        check(boss.hp()<hp && boss.slowRemaining()==0 && bossSession.classSkills.iceSlows==0,
            "Boss takes Ice Shard damage and ignores slow");bossSession.end();
    }
    private static void arcaneTrap(){
        var s=mage();check(s.classSkills.upgrade(ClassSkillId.MAGE_ARCANE_TRAP,0),"learn Arcane Trap");
        Enemy target=s.enemies.spawn(EnemyType.SLIME,620,600);int hp=target.hp();tick(s,.01f);
        check(s.classSkills.trapCount()==1 && s.classSkills.trapPlacements==1
            && s.classSkills.cooldownRemaining(ClassSkillId.MAGE_ARCANE_TRAP)==14,"Trap is placed and starts cooldown");
        var trap=s.classSkills.trap(0);check(trap.x==620 && trap.y==600,"Trap snapshots its target location");
        target.x=640;tick(s,.48f);
        check(target.hp()==hp && s.classSkills.trapCount()==1,"Trap waits through its half-second arming delay");
        tick(s,.02f);
        int expected=ClassSkillTuning.damage(ClassSkillId.MAGE_ARCANE_TRAP,1)*s.player.stats.damage();
        check(target.hp()==hp-expected && s.classSkills.trapExplosions==1 && s.classSkills.trapHits==1
            && s.classSkills.trapCount()==0,"armed trap explodes once at its original point");s.end();
    }
    private static void magicShieldBoundary(){
        var s=mage();check(s.classSkills.upgrade(ClassSkillId.MAGE_SHIELD,0),"learn Magic Shield");int hp=s.player.hp();
        check(!s.player.damageDirect(1,600,620) && s.player.hp()==hp && s.classSkills.shieldBlocks==1
            && s.classSkills.cooldownRemaining(ClassSkillId.MAGE_SHIELD)==20,"Shield blocks a direct hit from behind");
        check(s.player.damageDirect(1,600,580) && s.player.hp()==hp-1,"Shield cooldown allows the next direct hit through");s.end();

        var dot=mage();dot.classSkills.upgrade(ClassSkillId.MAGE_SHIELD,0);hp=dot.player.hp();
        check(dot.player.damage(1) && dot.player.hp()==hp-1 && dot.classSkills.shieldBlocks==0
            && dot.classSkills.cooldownRemaining(ClassSkillId.MAGE_SHIELD)==0,"Damage over time bypasses Magic Shield");dot.end();
        var dash=mage();dash.classSkills.upgrade(ClassSkillId.MAGE_SHIELD,0);dash.activateSkill(SkillType.DASH,0,-1,false);hp=dash.player.hp();
        check(!dash.player.damageDirect(1,600,620) && dash.player.hp()==hp && dash.classSkills.shieldBlocks==0
            && dash.classSkills.cooldownRemaining(ClassSkillId.MAGE_SHIELD)==0,"Dash iframe does not spend Magic Shield");dash.end();
    }
    private static void arcaneChannel(){
        var s=mage();check(s.classSkills.upgrade(ClassSkillId.MAGE_CHANNEL,0),"learn Arcane Channel");
        Enemy primary=s.enemies.spawn(EnemyType.SLIME,640,600),splash=s.enemies.spawn(EnemyType.SLIME,640,615);
        int primaryHp=primary.hp(),splashHp=splash.hp();tick(s,.01f);
        check(s.classSkills.channelActive() && s.classSkills.channelActivations==1
            && s.classSkills.cooldownRemaining(ClassSkillId.MAGE_CHANNEL)==18
            && s.classSkills.fireballCooldownMultiplier()==.9f && s.classSkills.fireballDirectBonus()==1,
            "Channel activates automatically with a visible enemy");
        s.weapons.update(.01f,s.player,s.enemies,open());var fireball=s.weapons.loadout.get(WeaponType.FIREBALL);
        float expectedCooldown=s.config.weapons.level(WeaponType.FIREBALL,1).cooldown*.9f;
        check(Math.abs(fireball.cooldownRemaining-expectedCooldown)<.001f,"Channel scales the next Fireball cooldown");
        check(s.weapons.projectiles.size()==1 && s.weapons.projectiles.get(0).primaryDirectTargetId==primary.id
            && s.weapons.projectiles.get(0).directBonusDamage==1,"Only primary Fireball projectile receives direct bonus");
        for(int i=0;i<8 && s.weapons.projectiles.size()>0;i++)s.weapons.projectiles.update(.1f,s.enemies,open());
        int base=s.config.weapons.level(WeaponType.FIREBALL,1).damage(s.player.stats.damage());
        check(primary.hp()==primaryHp-base-1 && splash.hp()==splashHp-base,
            "Fireball primary hit gains one damage while splash stays unchanged");
        s.classSkills.advance(3.1f);
        check(!s.classSkills.channelActive() && s.classSkills.fireballCooldownMultiplier()==1
            && s.classSkills.fireballDirectBonus()==0,"Channel returns Fireball to base values after expiry");s.end();
    }
    public static void main(String[] args){ownershipAndTuning();iceShardAndBossImmunity();arcaneTrap();magicShieldBoundary();arcaneChannel();
        System.out.println("MageSkillChecks PASS "+checks+" assertions");}
}
