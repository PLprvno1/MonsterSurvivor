import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.CollisionGrid;

/** Focused behavior checks: actual weapon damage/collision, modal transaction and legacy clocks. */
public final class WeaponChecks {
    private static int checks;
    private static void check(boolean ok,String message) {if(!ok)throw new AssertionError(message);checks++;}
    private static void close(float a,float b,float epsilon,String message) {check(Math.abs(a-b)<=epsilon,message+": "+a+" / "+b);}
    private static CollisionGrid open() {return new CollisionGrid(100,100,16,new byte[10000]);}
    private static GameConfig config(GameMode mode) {
        return new GameConfig.Builder().mode(mode).population(0,60).enemy(EnemyType.SLIME,100,1,.01f)
            .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();
    }
    private static void tick(GameSession s,int frames,float dt) {for(int i=0;i<frames;i++)s.update(dt,0,0,false,600,680,1000,920);}
    private static Pickup modal(GameSession s) {
        Pickup item=s.pickups.spawn(PickupType.WEAPON_BOX,s.player.x,s.player.y,0);
        check(item!=null,"Functional weapon box can be placed on reachable ground");tick(s,1,.01f);
        check(s.choosingWeapon() && s.selection.pendingId()==item.id && !item.collected,"Contact reserves one unconsumed pending item");return item;
    }
    private static void selection() {
        var c=config(GameMode.NORMAL);var s=new GameSession(c,open(),808,808,null,null,42);
        check(s.weapons.loadout.size()==1 && s.weapons.loadout.slot(0).type==WeaponType.SWORD && s.weapons.loadout.slot(0).level()==1,"Initial Sword level 1 only");
        var enemy=s.enemies.spawn(EnemyType.SLIME,940,808);var item=modal(s);long token=s.selection.token(),originalToken=token;
        check(s.selection.size()==3,"First choice offers three new weapons");
        for(int i=0;i<3;i++)check(s.selection.get(i).weapon==WeaponType.values()[i+1] && s.selection.get(i).valid(s.weapons.loadout,s.player),"First offers Bow/Fireball/Lightning, all valid");
        float real=s.elapsed(),progress=s.progress(),age=item.age,x=s.player.x,clock=enemy.animationTime;
        for(int i=0;i<50;i++)s.update(.1f,1,1,false,600,680,1000,920);
        check(s.elapsed()==real && s.progress()==progress && item.age==age && s.player.x==x && enemy.animationTime==clock,"Modal freezes clocks, movement, AI, animations and pickup age");
        check(s.selection.token()==token && s.pickups.collectedItems==0,"No repeated opening/early consumption while touching");
        check(!s.chooseWeapon(-1,token) && !s.chooseWeapon(0,token+1),"Invalid index and stale modal token rejected");
        check(s.chooseWeapon(0,token),"Bow choice commits");
        check(!s.chooseWeapon(0,token) && s.weapons.loadout.size()==2 && s.pickups.collectedItems==1 && !s.pickups.contains(item.id),"Double commit cannot duplicate reward or consumption");
        tick(s,12,.05f);check(s.weapons.loadout.get(WeaponType.BOW).activations>0 && enemy.hp()<100,"Chosen Bow actually fires and damages distant target");
        s.weapons.projectiles.shoot(WeaponType.BOW,s.player.x,s.player.y,1000,808,1,c.weapons.level(WeaponType.BOW,1));
        s.weapons.projectiles.effect(ProjectileSystem.EffectType.LIGHTNING,808,808,940,808,0);
        item=modal(s);float projectileAge=s.weapons.projectiles.get(0).age,effectAge=s.weapons.projectiles.effect(0).age;
        float bowCooldown=s.weapons.loadout.get(WeaponType.BOW).cooldownRemaining;
        tick(s,20,.1f);
        check(s.weapons.projectiles.get(0).age==projectileAge && s.weapons.projectiles.effect(0).age==effectAge
            && s.weapons.loadout.get(WeaponType.BOW).cooldownRemaining==bowCooldown,"Modal also freezes active projectiles, VFX and independent cooldowns");
        int upgrade=-1;
        for(int i=0;i<s.selection.size();i++)if(s.selection.get(i).weapon!=null && s.selection.get(i).fromLevel>0)upgrade=i;
        check(upgrade>=0,"Later offers include an existing weapon upgrade");
        var type=s.selection.get(upgrade).weapon;int old=s.weapons.loadout.get(type).level();token=s.selection.token();
        check(s.chooseWeapon(upgrade,token) && s.weapons.loadout.get(type).level()==old+1 && s.weapons.loadout.size()==2,"Selecting owned weapon upgrades, never duplicates slot");
        check(s.weapons.loadout.grant(WeaponType.FIREBALL),"Third slot can be filled");
        check(!s.weapons.loadout.grant(WeaponType.LIGHTNING) && s.weapons.loadout.size()==3,"Fourth weapon rejected");
        modal(s);
        for(int i=0;i<s.selection.size();i++)check(s.selection.get(i).weapon==null || s.weapons.loadout.get(s.selection.get(i).weapon)!=null,"Full loadout never offers an unowned fourth type");
        s.chooseWeapon(0,s.selection.token());
        for(int i=0;i<3;i++)while(s.weapons.loadout.grant(s.weapons.loadout.slot(i).type)) { }
        for(int i=0;i<3;i++)check(s.weapons.loadout.slot(i).level()==5 && !s.weapons.loadout.grant(s.weapons.loadout.slot(i).type),"Normal level cap is exactly 5");
        modal(s);check(s.selection.size()==3,"All maxed weapons receive useful stat alternatives");
        for(int i=0;i<3;i++)check(s.selection.get(i).kind!=WeaponOffer.Kind.WEAPON && s.selection.get(i).valid(s.weapons.loadout,s.player),"Fallback is applicable, not a capped weapon");
        old=s.player.stats.damage();s.chooseWeapon(0,s.selection.token());check(s.player.stats.damage()==old+2,"Fallback applies a real stat reward");
        for(int i=0;i<10;i++)s.player.stats.addSpeed();modal(s);check(s.selection.size()==2,"Fewer offers shown when Speed fallback is capped");
        token=s.selection.token();s.end();check(s.selection.pendingId()==-1 && !s.chooseWeapon(0,token) && s.weapons.projectiles.size()==0,"Home/end clears pending selection and transient combat; stale choice harmless");
        var fresh=new GameSession(c,open(),808,808,null,null,42);
        check(fresh.weapons.loadout.size()==1 && fresh.weapons.loadout.slot(0).level()==1 && fresh.selection.pendingId()==-1 && fresh.weapons.projectiles.size()==0,"Fresh/Retry session has only Sword 1 and no modal/projectiles");
        modal(fresh);check(!fresh.chooseWeapon(0,originalToken),"Old match token cannot choose the first offer of a new match");fresh.end();
    }
    private static void projectiles() {
        var c=config(GameMode.NORMAL);var grid=open();var enemies=new EnemySystem(new NavigationGrid(grid),null,c);
        var a=enemies.spawn(EnemyType.SLIME,120,88);var b=enemies.spawn(EnemyType.SLIME,160,88);
        var system=new ProjectileSystem(c.weapons);
        var fast=new WeaponConfig.Level(1,0,1,300,2000,1,0,1,0);
        check(system.shoot(WeaponType.BOW,88,88,300,88,3,fast),"Arrow launched");system.update(.1f,enemies,grid);
        check(a.hp()==97 && b.hp()==100 && system.size()==0,"Fast non-piercing arrow hits nearest swept target exactly once");
        system.update(.1f,enemies,grid);check(a.hp()==97 && system.hits==1,"Impact effect never repeats damage");
        enemies.clear();a=enemies.spawn(EnemyType.SLIME,120,88);b=enemies.spawn(EnemyType.SLIME,160,88);var third=enemies.spawn(EnemyType.SLIME,200,88);
        var pierce=new WeaponConfig.Level(1,0,1,300,2000,1,0,2,0);system.shoot(WeaponType.BOW,88,88,300,88,2,pierce);system.update(.1f,enemies,grid);
        check(a.hp()==98 && b.hp()==98 && third.hp()==100 && system.size()==0,"Upgraded piercing arrow resolves two earliest unique hits in one frame");
        enemies.clear();a=enemies.spawn(EnemyType.SLIME,120,88);
        var slow=new WeaponConfig.Level(1,0,1,300,1,1,0,3,0);system.shoot(WeaponType.BOW,120,88,300,88,2,slow);
        for(int i=0;i<5;i++)system.update(.1f,enemies,grid);check(a.hp()==98,"Piercing projectile overlapping a target never hits it twice");system.clear();
        enemies.clear();a=enemies.spawn(EnemyType.SLIME,120,88);b=enemies.spawn(EnemyType.SLIME,128,104);third=enemies.spawn(EnemyType.SLIME,168,88);
        var fire=new WeaponConfig.Level(1,0,3,180,500,1,35,1,0);system.shoot(WeaponType.FIREBALL,88,88,120,88,4,fire);system.update(.1f,enemies,grid);
        check(a.hp()==96 && b.hp()==96 && third.hp()==100 && system.explosions==1,"Fireball explodes once and applies actual AoE within radius");
        for(int i=0;i<5;i++)system.update(.1f,enemies,grid);check(a.hp()==96 && b.hp()==96 && system.explosions==1,"Explosion animation has no persistent repeated damage");
        byte[] masks=new byte[400];for(int row=0;row<20;row++)masks[row*20+5]=CollisionGrid.TREE;
        grid=new CollisionGrid(20,20,16,masks);enemies=new EnemySystem(new NavigationGrid(grid),null,c);
        a=enemies.spawn(EnemyType.SLIME,104,88);b=enemies.spawn(EnemyType.SLIME,56,104);
        system.shoot(WeaponType.BOW,56,88,104,88,5,fast);system.update(.1f,enemies,grid);
        check(a.hp()==100 && system.size()==0,"Wall blocks a fast arrow before a target behind it");
        system.shoot(WeaponType.FIREBALL,56,88,104,88,4,fire);system.update(.1f,enemies,grid);
        check(a.hp()==100 && b.hp()==96,"Wall impact explodes on near side; AoE cannot cross the tree");
        system.clear();grid=open();enemies=new EnemySystem(new NavigationGrid(grid),null,c);
        for(int i=0;i<c.weapons.projectileCap;i++)check(system.shoot(WeaponType.BOW,88,88,300,88,1,fast),"Capacity slot accepted");
        check(!system.shoot(WeaponType.BOW,88,88,300,88,1,fast),"Projectile cap enforced");
        system.update(2,enemies,grid);check(system.size()==0,"All expired projectiles reclaimed");
        for(int i=0;i<60;i++)system.effect(ProjectileSystem.EffectType.HIT,10,10,0,0,3);
        check(system.effectCount()==c.weapons.effectCap,"Effect cap enforced");system.update(1,enemies,grid);check(system.effectCount()==0,"Expired effects reclaimed");
    }
    private static void lightningAndClocks() {
        var c=config(GameMode.NORMAL);var grid=open();var s=new GameSession(c,grid,808,808,null,null,1);
        s.weapons.loadout.grant(WeaponType.LIGHTNING);var a=s.enemies.spawn(EnemyType.SLIME,908,808);var b=s.enemies.spawn(EnemyType.SLIME,940,808);var d=s.enemies.spawn(EnemyType.SLIME,972,808);
        tick(s,1,.01f);check(a.hp()==99 && b.hp()==99 && d.hp()==100 && s.weapons.lightningHits==2,"Lightning level 1 chains to exactly two unique living enemies");
        tick(s,10,.05f);check(a.hp()==99 && b.hp()==99 && s.weapons.lightningCasts==1,"Lingering bolt effect cannot repeat chain damage");
        for(int i=0;i<4;i++)s.weapons.loadout.grant(WeaponType.LIGHTNING);
        check(s.weapons.loadout.get(WeaponType.LIGHTNING).tuning().targets==4,"Lightning max level increases real target cap");
        for(WeaponType type:WeaponType.values()) {
            int first=c.weapons.level(type,1).damage(3),last=c.weapons.level(type,5).damage(3);
            check(last>first && c.weapons.level(type,5).cooldown<c.weapons.level(type,1).cooldown,"Every weapon's level upgrades damage and cooldown: "+type);
            check(c.weapons.level(type,1).damage(4)>c.weapons.level(type,1).damage(1),"Player Damage scales every weapon: "+type);
        }
        int[] activations=new int[2];int index=0;
        for(GameMode mode:GameMode.values()) {
            s=new GameSession(config(mode),open(),808,808,null,null,42);s.weapons.loadout.grant(WeaponType.BOW);s.enemies.spawn(EnemyType.SLIME,968,808);
            tick(s,64,.05f);activations[index++]=s.weapons.loadout.get(WeaponType.BOW).activations;
            float remaining=s.weapons.loadout.get(WeaponType.BOW).cooldownRemaining;int projectiles=s.weapons.projectiles.size();
            for(int i=0;i<20;i++)s.update(.1f,0,0,true,600,680,1000,920);
            check(s.weapons.loadout.get(WeaponType.BOW).cooldownRemaining==remaining && s.weapons.projectiles.size()==projectiles,"Pause freezes weapon/projectile state in "+mode);
        }
        check(activations[0]==3 && activations[1]==3,"Demo uses the same real-time Bow cooldown as Normal");
        for(WeaponType ranged:new WeaponType[]{WeaponType.FIREBALL,WeaponType.LIGHTNING}) {
            s=new GameSession(c,open(),808,808,null,null,1);s.weapons.loadout.grant(WeaponType.BOW);s.weapons.loadout.grant(ranged);
            s.enemies.spawn(EnemyType.SLIME,840,808);tick(s,20,.05f);
            check(s.combat.swings>0 && s.combat.hitEvents>0 && s.weapons.loadout.get(WeaponType.BOW).activations>0
                && s.weapons.loadout.get(ranged).activations>0,"Sword and both ranged weapons activate independently: "+ranged);
        }
        byte[] masks=new byte[400];for(int row=0;row<20;row++)masks[row*20+8]=CollisionGrid.ROCK;
        s=new GameSession(c,new CollisionGrid(20,20,16,masks),72,72,null,null,1);s.weapons.loadout.grant(WeaponType.LIGHTNING);
        a=s.enemies.spawn(EnemyType.SLIME,104,72);b=s.enemies.spawn(EnemyType.SLIME,104,104);d=s.enemies.spawn(EnemyType.SLIME,152,72);
        tick(s,1,.01f);check(a.hp()==99 && b.hp()==99 && d.hp()==100,"Lightning initial and chain targeting both respect walls");
    }
    private static void spawnAndDeath() {
        for(float probability:new float[]{0,.33f,1}) {
            var tuning=new PickupConfig(15,20,60,6,20,1,1,0,1,2,probability);
            var c=new GameConfig.Builder().pickups(tuning).population(0,60).enemy(EnemyType.SLIME,1,1,.01f).build();
            var s=new GameSession(c,open(),808,808,null,null,42);int weapon=0;
            for(int event=0;event<30;event++) {
                for(int i=0;i<s.pickups.size();i++)s.pickups.get(i).age=60;
                float due=s.pickups.nextSpawnProgress();s.pickups.update(0,due,s.player);
                check(s.pickups.size()==1 && s.pickups.nextSpawnProgress()-due>=15 && s.pickups.nextSpawnProgress()-due<=20,"Weapon pool preserves common schedule and valid placement");
                if(s.pickups.get(0).type==PickupType.WEAPON_BOX)weapon++;
                if(event==1 && probability>0)check(weapon>=1,"At least one of first two boxes is Weapon");
            }
            check(probability==0?weapon==0:probability==1?weapon==30:weapon>=5 && weapon<=20,"Configurable weighted Weapon Box pool: "+probability);
            s=new GameSession(c,open(),808,808,null,null,42);s.weapons.loadout.grant(WeaponType.BOW);var e=s.enemies.spawn(EnemyType.SLIME,928,808);
            tick(s,15,.05f);check(!e.alive() && s.kills==1 && s.combat.kills==0 && s.pickups.deathRolls==1 && s.pickups.healthDrops()==1,"Ranged kill uses one unified kill/death-drop event");
            for(int i=0;i<10;i++) {e.damage(5);e.advance(.1f);}
            check(s.pickups.deathRolls==1 && s.kills==1,"Corpse cannot generate more ranged drops/kills");
        }
        close(CombatGeometry.hit(0,0,100,0,50,0,5,3),.45f,.0001f,"Swept hit fraction");
    }
    public static void main(String[] args) {selection();projectiles();lightningAndClocks();spawnAndDeath();System.out.println("TASK 03B LOGIC PASSED: "+checks+" checks");}
}
