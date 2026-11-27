import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.audio.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;
import java.util.*;
import static com.example.monstersurvivor.skill.SkillType.*;

/** Levels are earned exclusively through real pickup -> selection -> session commit in these checks. */
public final class UpgradeChecks {
    static int checks;
    static void check(boolean b,String label){if(!b)throw new AssertionError(label);checks++;}
    static void close(float a,float b,String label){check(Math.abs(a-b)<.004f,label+" actual="+a+" expected="+b);}
    static CollisionGrid open(){return new CollisionGrid(80,80,16,new byte[6400]);}
    static GameConfig config(Difficulty d,GameMode m){return new GameConfig.Builder().difficulty(d).mode(m).debug(true).playerHp(100000).population(0,60)
        .enemy(EnemyType.SLIME,1000,1,1).stages(new GameConfig.Stage(0,0,100000,1000,0,0,0)).build();}
    static GameSession session(){return new GameSession(config(Difficulty.EASY,GameMode.NORMAL),open(),600,600,null,null,42);}
    static void tick(GameSession s,float dt){s.update(dt,0,0,false,460,496,740,704);}
    static int offer(GameSession s,SkillType type){for(int i=0;i<s.skillSelection.size();i++)if(s.skillSelection.get(i).type==type)return i;throw new AssertionError("No offer "+type);}
    static long box(GameSession s){
        var p=s.pickups.spawn(PickupType.SKILL_BOX,s.player.x,s.player.y,1);check(p!=null,"real skill box placement");tick(s,.001f);
        check(s.choosingSkill() && s.pickups.contains(p.id),"modal holds real unconsumed box");return p.id;
    }
    static void upgrade(GameSession s,SkillType type){
        int before=s.skills.view(type).level();long id=box(s),token=s.skillSelection.token();int selected=offer(s,type);
        check(token<0 && !s.chooseSkill(selected,token+10000),"stale/foreign token rejected");
        check(s.chooseSkill(selected,token),"one reward committed");check(!s.chooseSkill(selected,token),"duplicate commit rejected");
        check(s.skills.view(type).level()==before+1 && !s.pickups.contains(id) && s.phase()==GameSession.Phase.RUNNING,"exactly one level and one box consumed");
    }
    static void level(GameSession s,SkillType type,int target){while(s.skills.view(type).level()<target)upgrade(s,type);}
    static void tablesAndRewards(){
        var cfg=new SkillConfig();float[] cd={5,4.6f,4.2f,3.8f,3.5f},distance={75,80,86,93,100},iframe={.17f,.17f,.18f,.18f,.2f};
        float[] ucd={30,28,26,24,22},radius={105,115,125,135,145},mult={2.5f,2.9f,3.3f,3.7f,4.2f},kb={25,27,29,30,33};
        for(int n=1;n<=5;n++) {
            close(cfg.dash(n).cooldown,cd[n-1],"Dash table");close(cfg.dash(n).distance,distance[n-1],"Dash distance");close(cfg.dash(n).invulnerability,iframe[n-1],"Dash iframe");
            close(cfg.ultimate(n).cooldown,ucd[n-1],"Ultimate cooldown");close(cfg.ultimate(n).radius,radius[n-1],"Ultimate radius");close(cfg.ultimate(n).damageMultiplier,mult[n-1],"Ultimate multiplier");close(cfg.ultimate(n).knockback,kb[n-1],"Ultimate knockback");
            check(cfg.ultimate(n).damage(Integer.MAX_VALUE)==Integer.MAX_VALUE,"integer overflow guard at every level");
        }
        var s=session();for(int i=0;i<100;i++)tick(s,.1f);check(s.skills.view(DASH).level()==1 && s.skills.view(ULTIMATE).level()==1,"no time-based free level");
        level(s,DASH,5);check(!s.skills.canUpgrade(DASH) && s.skills.canUpgrade(ULTIMATE),"independent cap");
        box(s);check(s.skillSelection.size()==1 && s.skillSelection.get(0).type==ULTIMATE,"omit max-level skill");s.chooseSkill(0,s.skillSelection.token());
        // An existing distant box must retire after both max, rather than opening an empty modal.
        var extra=s.pickups.spawn(PickupType.SKILL_BOX,750,600,1);check(extra!=null,"leftover max handling fixture");
        level(s,ULTIMATE,5);check(!s.skills.hasUpgrade() && s.pickups.skillBoxes()==0 && s.pickups.retiredSkillBoxes==1,"max retires leftover boxes");
        check(s.pickups.spawn(PickupType.SKILL_BOX,600,600,1)==null && !s.skillSelection.open(999,s.skills),"no spawn or empty modal at max");
        check(s.weapons.loadout.describe().equals("SWORD:1"),"all eight upgrades use zero weapon slots");s.end();
        var fresh=session();check(fresh.skills.view(DASH).level()==1 && fresh.skills.view(ULTIMATE).level()==1 && fresh.skills.view(ULTIMATE).cooldownRemaining()==15,"Retry baseline Lv1/15s");
    }
    static void cooldownAndSnapshots(){
        var s=session();box(s);float old=s.skills.view(ULTIMATE).cooldownRemaining(),progress=s.skills.view(ULTIMATE).rechargeProgress();
        s.chooseSkill(offer(s,ULTIMATE),s.skillSelection.token());close(s.skills.view(ULTIMATE).cooldownRemaining(),old*28/30,"initial remaining proportion");close(s.skills.view(ULTIMATE).rechargeProgress(),progress,"initial gauge preserved");
        check(s.skills.view(ULTIMATE).phase()==ActiveSkillSystem.Phase.COOLDOWN,"upgrade does not make initial Ultimate free");
        s=session();s.activateSkill(DASH,1,0,false);s.skills.advance(1,0,0,s.player,open());box(s);old=s.skills.view(DASH).cooldownRemaining();progress=s.skills.view(DASH).rechargeProgress();
        float frozen=s.elapsed();for(int i=0;i<30;i++)tick(s,.1f);close(s.elapsed(),frozen,"skill modal freezes time");close(s.skills.view(DASH).cooldownRemaining(),old,"skill modal freezes cooldown");
        check(!s.activateSkill(DASH,1,0,false) && !s.activateSkill(ULTIMATE,0,0,false),"modal rejects activation");
        s.chooseSkill(offer(s,DASH),s.skillSelection.token());close(s.skills.view(DASH).cooldownRemaining(),old*4.6f/5,"running cooldown ratio");close(s.skills.view(DASH).rechargeProgress(),progress,"running gauge preserved");
        // Four upgrades during one active Dash keep its original 75 units and .17s iframe.
        s=session();s.activateSkill(DASH,1,0,false);level(s,DASH,5);check(s.skills.dashActivationLevel()==1,"Dash activation keeps old level snapshot");
        s.skills.advance(.16f,0,0,s.player,open());check(s.player.dashInvulnerable(),"old iframe still in first .17s");s.skills.advance(.01f,0,0,s.player,open());check(!s.player.dashInvulnerable(),"upgrade does not extend active iframe to .20s");
        s.skills.advance(.03f,0,0,s.player,open());close(s.player.x,675,"active distance stays old 75");
        s.skills.advance(5,0,0,s.player,open());s.activateSkill(DASH,1,0,false);s.skills.advance(.2f,0,0,s.player,open());close(s.player.x,775,"next activation uses Lv5 distance100");
        // Ultimate upgraded while ACTIVE: radius, damage and knockback remain the cast's snapshot.
        s=session();s.player.stats.addDamage(3);s.skills.advance(15,0,0,s.player,open());s.activateSkill(ULTIMATE,0,0,false);
        var inside=s.enemies.spawn(EnemyType.SLIME,690,600);var outside=s.enemies.spawn(EnemyType.SLIME,740,600);
        level(s,ULTIMATE,5);s.player.stats.addDamage(100);check(s.skills.ultimateActivationLevel()==1 && s.skills.ultimateRadius()==105,"old radius/level retained through active upgrades");
        s.skills.advance(.4f,0,0,s.player,open());s.skills.resolve(s.player,s.enemies,open(),null);
        check(inside.hp()==990 && outside.hp()==1000,"old damage snapshot and radius used once");close(inside.x,715,"old knockback retained");
        s.skills.advance(30,0,0,s.player,open());s.activateSkill(ULTIMATE,0,0,false);check(s.skills.ultimateActivationLevel()==5 && s.skills.ultimateRadius()==145,"next Ultimate uses new snapshot");
        s.skills.advance(.35f,0,0,s.player,open());s.skills.resolve(s.player,s.enemies,open(),null);check(outside.hp()==1000-cfgDamage(s),"Lv5 damage applies next cast");
    }
    static int cfgDamage(GameSession s){return s.config.skills.ultimate(5).damage(s.player.stats.damage());}
    static void combat(){
        for(int n=1;n<=5;n++) {
            var s=session();level(s,DASH,n);s.activateSkill(DASH,1,0,false);float distance=s.config.skills.dash(n).distance;
            s.skills.advance(.2f,0,0,s.player,open());close(s.player.x,600+distance,"distance all levels");check(s.player.stats.speed()==10,"normal speed unchanged");
            for(int flag:new int[]{CollisionGrid.TREE,CollisionGrid.ROCK,CollisionGrid.WATER,CollisionGrid.BOUNDARY})for(float dt:new float[]{.02f,.1f,.5f}) {
                byte[] wall=new byte[6400];for(int y=0;y<80;y++)wall[y*80+40]=(byte)flag;var grid=new CollisionGrid(80,80,16,wall);
                var blocked=new GameSession(config(Difficulty.EASY,GameMode.NORMAL),grid,620,600,null,null,42);level(blocked,DASH,n);blocked.activateSkill(DASH,1,0,false);
                for(float t=0;t<.201;t+=dt)blocked.skills.advance(dt,0,0,blocked.player,grid);
                check(blocked.player.x<637 && grid.canOccupy(blocked.player.x,blocked.player.y,4,3),"no tunneling/stuck at every level/frame/terrain");
            }
            s=session();level(s,DASH,n);s.activateSkill(DASH,1,0,false);check(!s.player.damage(1),"iframe blocks initial damage all levels");
            s.skills.advance(s.config.skills.dash(n).invulnerability-.002f,0,0,s.player,open());check(!s.player.damage(1),"iframe timing all levels");
            s.skills.advance(.003f,0,0,s.player,open());check(s.player.damage(1),"iframe ends, no infinite immunity");close(s.player.immunity(),.7f,"original post-hit immunity");
            s=session();level(s,ULTIMATE,n);s.player.stats.addDamage(3);s.skills.advance(16,0,0,s.player,open());
            var enemy=s.enemies.spawn(EnemyType.SLIME,670,600);s.activateSkill(ULTIMATE,0,0,false);s.skills.advance(.35f,0,0,s.player,open());s.skills.resolve(s.player,s.enemies,open(),null);
            check(enemy.hp()==1000-s.config.skills.ultimate(n).damage(4),"damage table all levels");close(enemy.x,670+s.config.skills.ultimate(n).knockback,"knockback all levels");
            int hp=enemy.hp();s.skills.resolve(s.player,s.enemies,open(),null);check(enemy.hp()==hp && s.skills.ultimateImpacts==1,"one hit per activation all levels");
            byte[] wall=new byte[6400];for(int y=0;y<80;y++)wall[y*80+40]=CollisionGrid.ROCK;var grid=new CollisionGrid(80,80,16,wall);
            var e=new EnemySystem(new NavigationGrid(grid),null,s.config);var behind=e.spawn(EnemyType.SLIME,685,600);
            s=session();level(s,ULTIMATE,n);s.skills.advance(16,0,0,s.player,open());s.activateSkill(ULTIMATE,0,0,false);s.skills.advance(.35f,0,0,s.player,open());s.skills.resolve(s.player,e,grid,null);
            check(behind.hp()==1000,"LOS blocks Ultimate every level");
            var near=e.spawn(EnemyType.SLIME,630,600);s.skills.advance(30,0,0,s.player,open());s.activateSkill(ULTIMATE,0,0,false);s.skills.advance(.35f,0,0,s.player,open());s.skills.resolve(s.player,e,grid,null);
            check(near.x<636 && grid.canOccupy(near.x,near.y,near.definition.halfWidth,near.definition.halfHeight) && near.stats.speed==1,"knockback terrain/speed preserved at every level");
            var tankSession=session();level(tankSession,ULTIMATE,n);tankSession.skills.advance(16,0,0,tankSession.player,open());
            var tank=tankSession.enemies.spawn(com.example.monstersurvivor.enemy.EnemyRegistry.get("rock_golem"),670,600,0);
            tankSession.activateSkill(ULTIMATE,0,0,false);tankSession.skills.advance(.35f,0,0,tankSession.player,open());tankSession.skills.resolve(tankSession.player,tankSession.enemies,open(),null);
            close(tank.x,670+tankSession.config.skills.ultimate(n).knockback*.35f,"tank knockback scaling every level");
            var forced=new GameConfig.Builder().debug(true).playerHp(100000).population(0,40).enemy(EnemyType.SLIME,1000,1,1)
                .pickups(new PickupConfig(15,20,60,6,20,1,1,0,1,2)).stages(new GameConfig.Stage(0,0,100000,1000,0,0,0)).build();
            var death=new GameSession(forced,open(),600,600,null,null,42);level(death,ULTIMATE,n);death.skills.advance(16,0,0,death.player,open());
            var victim=death.enemies.spawn(EnemyType.SLIME,670,600);victim.damage(999);death.activateSkill(ULTIMATE,0,0,false);death.skills.advance(.35f,0,0,death.player,open());
            for(int repeat=0;repeat<20;repeat++)death.skills.resolve(death.player,death.enemies,open(),null);
            check(!victim.alive() && death.kills==1 && death.pickups.deathRolls==1 && death.pickups.healthDrops()==1 && death.skills.ultimateKills==1,"lethal Ultimate produces exactly one kill/drop at every level");
            for(var d:Difficulty.values())for(var m:GameMode.values()) {
                s=new GameSession(config(d,m),open(),600,600,null,null,42);level(s,ULTIMATE,n);s.bosses.prepare(.05f,600,s.player,460,496,740,704);
                var boss=s.bosses.lich().actor;boss.x=670;boss.y=600;int before=boss.hp();s.player.stats.addDamage(100000);s.skills.advance(16,0,0,s.player,open());
                s.activateSkill(ULTIMATE,0,0,false);s.skills.advance(.35f,0,0,s.player,open());s.skills.resolve(s.player,s.enemies,open(),null);
                check(before-boss.hp()==(int)Math.ceil(before*.2f) && boss.alive() && boss.x==670,"Boss guard/zero knockback all levels/modes/difficulties");
                check(s.skills.view(ULTIMATE).level()==n,"Boss hit gives no free level");s.end();
            }
        }
    }
    static void spawningAndTokens(){
        for(var d:Difficulty.values())for(var m:GameMode.values()) {
            var s=new GameSession(config(d,m),open(),600,600,null,null,42);float first=s.pickups.nextSkillSpawnProgress();check(first>=35 && first<=45,"first progress timing");
            s.pickups.update(.1f,first-.1f,s.player);check(s.pickups.skillBoxes()==0,"not before scheduled progress");s.pickups.update(.1f,first,s.player);check(s.pickups.skillBoxes()==1,"real scheduled spawn");
            float second=s.pickups.nextSkillSpawnProgress();check(second-first>=40 && second-first<=55,"next interval timing");
            s.pickups.update(.1f,second,s.player);check(s.pickups.skillBoxes()==2,"two skill cap");
            int spawned=s.pickups.spawnedSkillBoxes;s.pickups.update(.1f,second+1000,s.player);check(s.pickups.skillBoxes()==2 && s.pickups.spawnedSkillBoxes==spawned,"large progress cannot burst past cap");
            for(int i=0;i<s.pickups.size();i++) {var p=s.pickups.get(i);if(p.type==PickupType.SKILL_BOX)check(s.enemies.navigation.connected(s.enemies.navigation.cell(600,600),s.enemies.navigation.cell(p.x,p.y),false) && open().canOccupy(p.x,p.y,5,4),"reachable skill spawn");}
            // Ordinary cap remains independent from the additional bounded skill boxes.
            for(int x=1;x<=10;x++)s.pickups.spawn(PickupType.DAMAGE_BOX,300+x*20,400,1);
            check(s.pickups.ordinaryBoxes()==6 && s.pickups.skillBoxes()==2 && s.pickups.healthDrops()==0,"existing cap6 plus dedicated skill cap2");
        }
        var s=session();long id=box(s),token=s.skillSelection.token();s.end();var fresh=session();box(fresh);
        check(fresh.skillSelection.token()!=token && !fresh.chooseSkill(0,token),"Retry rejects stale skill token");
        long skillToken=fresh.skillSelection.token();fresh.chooseSkill(0,skillToken);fresh.pickups.spawn(PickupType.WEAPON_BOX,600,600,1);tick(fresh,.001f);
        check(fresh.choosingWeapon() && fresh.selection.token()>0 && !fresh.chooseWeapon(0,skillToken),"Weapon modal remains separate and refuses skill token");
        check(fresh.chooseWeapon(0,fresh.selection.token()) && fresh.weapons.loadout.size()==2,"Weapon Box commit regression");
    }
    public static void main(String[] args){tablesAndRewards();cooldownAndSnapshots();combat();spawningAndTokens();System.out.println("UpgradeChecks PASS "+checks+" assertions");}
}

