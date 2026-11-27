import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.boss.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;
import java.util.*;

public final class BossChecks {
    static int checks;
    static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    static CollisionGrid open(){return new CollisionGrid(80,80,16,new byte[6400]);}
    static GameConfig config(Difficulty d,GameMode m){return new GameConfig.Builder().difficulty(d).mode(m).playerHp(100000).population(0,d==Difficulty.HARD?60:40).stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();}
    static GameSession session(long seed){return new GameSession(config(Difficulty.EASY,GameMode.NORMAL),open(),600,600,null,null,seed);}
    static void clock(GameSession s,double value)throws Exception {var f=GameSession.class.getDeclaredField("progress");f.setAccessible(true);f.setDouble(s,value);}
    static void tick(GameSession s,int count){for(int i=0;i<count;i++)s.update(.05f,0,0,false,460,496,740,704);}
    static void prepare(GameSession s,float progress){s.bosses.prepare(.05f,progress,s.player,460,496,740,704);}
    static void kill(Enemy e){for(int i=0;i<20 && e.alive();i++){e.advance(.21f);e.damage(Integer.MAX_VALUE);}}
    static void events()throws Exception {
        var chosen=new HashSet<String>();
        for(int seed=0;seed<32;seed++) {
            var a=session(seed);var b=session(seed);prepare(a,299.99f);check(a.enemies.size()==0,"no early Mini");prepare(a,300.1f);prepare(b,300.1f);
            check(a.bosses.miniState()==BossSystem.EventState.ACTIVE,"Mini spawned");var mini=a.bosses.mini();chosen.add(mini.definition.body().spriteId);
            check(mini.definition.body().id.equals(b.bosses.mini().definition.body().id),"seed reproduces definition");
            var e=mini.actor;float distance=(float)Math.hypot(e.x-600,e.y-600);check(distance>=64 && distance<240,"spawn near player with gap");check(a.enemies.bossNavigation.connected(a.enemies.bossNavigation.cell(e.x,e.y),a.enemies.bossNavigation.cell(600,600),false),"large footprint connected");
            prepare(a,599.99f);check(a.bosses.lich()==null,"no early Lich");prepare(a,600.1f);check(a.bosses.lich()!=null && a.enemies.bossSize()==2,"Final independent while Mini alive");
            for(int i=0;i<10;i++)prepare(a,900);check(a.enemies.bossSize()==2,"once only even after threshold");a.end();b.end();
        }
        check(chosen.size()>=6,"diverse seeded pool");
        byte[] masks=new byte[6400];Arrays.fill(masks,(byte)CollisionGrid.TREE);var c=config(Difficulty.EASY,GameMode.NORMAL);
        var sys=new EnemySystem(new NavigationGrid(new CollisionGrid(80,80,16,masks)),null,c);var bosses=new BossSystem(c,sys,42);var p=new Player(600,600);
        for(int i=0;i<10;i++)bosses.prepare(.1f,650,p,460,496,740,704);
        check(bosses.miniState()==BossSystem.EventState.SPAWNING && bosses.finalState()==BossSystem.EventState.SPAWNING && bosses.spawnFailures>0,"blocked spawn retains both events with finite retries");
        var s=session(42);clock(s,299.99);s.update(.1f,0,0,true,460,496,740,704);check(s.progress()<300 && s.bosses.mini()==null,"pause freezes events");tick(s,1);check(s.bosses.mini()!=null && s.night.strength()>0,"crossed frame triggers Night and Mini");
        s.pickups.spawn(PickupType.WEAPON_BOX,600,600,1);tick(s,1);check(s.choosingWeapon(),"real modal");clock(s,599.99);float t=s.bosses.noticeTime(),anim=s.bosses.mini().actor.animationTime;tick(s,10);
        check(s.bosses.lich()==null && t==s.bosses.noticeTime() && anim==s.bosses.mini().actor.animationTime,"modal freezes boss/event/announcement clocks");s.chooseWeapon(0,s.selection.token());tick(s,1);check(s.bosses.lich()!=null,"modal release crosses Final");s.end();check(s.bosses.projectiles.size()==0 && s.enemies.size()==0,"end cleans owned actors and shots");
        var fresh=session(42);check(fresh.bosses.miniState()==BossSystem.EventState.NOT_TRIGGERED && fresh.bosses.lich()==null,"Retry session clean");fresh.end();
        var priority=session(42);prepare(priority,600);kill(priority.bosses.mini().actor);
        check(priority.bosses.noticeKey()==com.example.monstersurvivor.localization.GameStrings.Key.FINAL_BOSS_APPEARS,"Final arrival notice survives simultaneous Mini death");
        int rolls=priority.pickups.deathRolls;priority.bosses.mini().actor.damage(Integer.MAX_VALUE);check(priority.pickups.deathRolls==rolls,"Boss death never repeats drop roll");priority.end();
        priority=session(42);prepare(priority,600);var lich=priority.bosses.lich();for(int i=0;i<3;i++){lich.actor.advance(.21f);lich.actor.damage(100000);}for(int i=0;i<20;i++)prepare(priority,600);kill(priority.bosses.mini().actor);
        check(priority.bosses.noticeKey()==com.example.monstersurvivor.localization.GameStrings.Key.BOSS_PHASE_TWO,"Final phase notice survives simultaneous Mini death");priority.end();
    }
    static void projectiles() {
        var grid=open();var p=new Player(200,200);var shots=new BossProjectiles(24);
        shots.fire(7,100,200,1,0,2000,2);shots.update(.1f,p,grid);check(p.hp()==8 && shots.hits==1 && shots.size()==0,"swept single player hit");
        shots.fire(7,190,200,1,0,1000,2);shots.update(.1f,p,grid);check(p.hp()==8 && shots.size()==0,"existing immunity consumes hostile shot");p.update(.71f,0,0,grid);check(p.immunity()==0,"immunity real-time");
        byte[] masks=new byte[6400];for(int row=0;row<80;row++)masks[row*80+9]=CollisionGrid.TREE;
        shots.fire(9,100,200,1,0,2000,2);shots.update(.1f,p,new CollisionGrid(80,80,16,masks));check(p.hp()==8 && shots.blocked==1,"wall stops swept hostile shot");
        for(int i=0;i<25;i++)shots.fire(i,100,300,1,0,1,2);check(shots.size()==24,"projectile cap");check(shots.get(0).team==BossProjectiles.Team.ENEMY,"explicit hostile team");shots.clearOwner(0);check(shots.size()==23,"owner cleanup");shots.update(3.1f,p,grid);check(shots.size()==0,"lifetime cap");
    }
    static void combat() {
        for(var d:Difficulty.values())for(var m:GameMode.values()) {
            var c=config(d,m);var s=new GameSession(c,open(),600,600,null,null,42);prepare(s,600);var b=s.bosses.lich();
            check(b.maxHp()==(d==Difficulty.EASY?(m==GameMode.NORMAL?220:176):(m==GameMode.NORMAL?280:224)),"configured HP matrix");
            int hp=b.hp();b.actor.damage(100000);check(b.hp()>0 && hp-b.hp()<=Math.ceil(hp*.2f),"single high burst cannot erase boss");
            for(int i=0;i<2;i++){b.actor.advance(.21f);b.actor.damage((int)Math.ceil(hp*.2f));}
            for(int i=0;i<20;i++) {prepare(s,600);s.bosses.fight(.05f,600,s.player);s.enemies.beginFrame();s.enemies.update(.05f,s.player);}
            check(b.phase()==2 && b.phaseChanges==1,"single 50 percent phase transition");
            // Put both bosses on an open visible arena to exercise complete attack cycles.
            b.actor.x=700;b.actor.y=600;s.bosses.mini().actor.x=640;s.bosses.mini().actor.y=600;
            for(int i=0;i<600;i++) {prepare(s,600);s.bosses.fight(.05f,600,s.player);s.player.update(.05f,0,0,openGrid);s.enemies.beginFrame();s.enemies.update(.05f,s.player);check(s.enemies.searchesThisFrame<=2,"shared A star budget");}
            check(b.primaryCasts>1 && b.areaCasts>1 && b.phaseChanges==1,"phase 2 actual bolt and area casts");check(b.summoned>0 && b.summoned<= (d==Difficulty.EASY?2:3),"bounded real summons");check(s.player.hp()<s.player.maxHp(),"real boss damage");check(s.bosses.mini().areaCasts>0,"Mini heavy attack");
            int kills=s.kills;kill(b.actor);check(s.bosses.finalDefeated() && s.kills==kills+1 && b.hp()==0,"real death accounts once");int casts=b.primaryCasts+b.areaCasts;b.actor.damage(999);prepare(s,900);s.bosses.fight(.1f,900,s.player);check(s.kills==kills+1 && casts==b.primaryCasts+b.areaCasts,"dead cannot attack or double kill");s.end();
            for(var type:WeaponType.values())for(var def:new BossDefinition[]{c.bosses.miniPool.get(0),c.bosses.lich}) {
                var e=new EnemySystem(new NavigationGrid(open()),null,c);var target=e.spawnBoss(def.body(),c.bosses.stats(def,d,m),632,600);target.limitBurst(.2f,.2f);
                var loadout=new WeaponLoadout(c.weapons);if(type!=WeaponType.SWORD)loadout.grant(type);var sword=new com.example.monstersurvivor.combat.SwordCombat(c,loadout.get(WeaponType.SWORD));var ws=new WeaponSystem(c,loadout,sword);var player=new Player(600,600,null,c);int before=target.hp();
                if(type==WeaponType.SWORD)for(int i=0;i<30;i++){player.update(.05f,0,0,openGrid);target.advance(.05f);ws.update(.05f,player,e,openGrid);}
                else if(type==WeaponType.LIGHTNING) {target.x=720;for(int i=0;i<2;i++)ws.update(.05f,player,e,openGrid);check(ws.lightningHits>0,"Lightning actual target");}
                else {ws.projectiles.shoot(type,600,600,632,600,4,c.weapons.level(type,1));ws.projectiles.update(.2f,e,openGrid);check(ws.projectiles.hits>0,"ranged actual target");}
                check(target.hp()<before,"weapon damages "+type+" "+def.kind()+" "+d+" "+m);
            }
        }
    }
    static final CollisionGrid openGrid=open();
    static void victory()throws Exception {
        var s=session(42);clock(s,600);tick(s,1);check(s.phase()==GameSession.Phase.RUNNING,"600 alive is not Victory");clock(s,720);tick(s,1);check(s.phase()==GameSession.Phase.RUNNING,"12 minutes alive continues");
        var b=s.bosses.lich();kill(b.actor);tick(s,1);check(s.phase()==GameSession.Phase.WINNING && !b.actor.deathFinished(),"death animation before Victory");float p=s.progress(),elapsed=s.elapsed(),x=s.player.x;int kills=s.kills;
        s.update(.1f,1,1,true,460,496,740,704);check(!b.actor.deathFinished(),"pause freezes winning death");tick(s,30);check(s.victory() && b.actor.deathFinished() && s.bosses.primary()!=b,"Victory after actual death");
        tick(s,30);check(s.progress()==p && s.elapsed()==elapsed && s.player.x==x && s.kills==kills && s.bosses.projectiles.size()==0,"Victory freezes all systems and cleans projectiles");
        var r=s.result();check(r.victory() && r.finalBossDefeated() && !r.miniBossDefeated() && r.kills()==1 && r.survivedSeconds()==elapsed && r.progressSeconds()==p,"immutable actual result allows living Mini");s.end();
        s=session(42);clock(s,600);tick(s,1);s.bosses.projectiles.fire(1,700,600,-1,0,10,2);s.player.damage(1000000);tick(s,40);check(s.phase()==GameSession.Phase.GAME_OVER && !s.result().victory() && !s.result().finalBossDefeated() && s.bosses.projectiles.size()==0,"death before Final is GameOver and clean");s.end();
        for(var mode:GameMode.values()) {s=new GameSession(config(Difficulty.EASY,mode),open(),600,600,null,null,42);int frames=mode==GameMode.NORMAL?6000:900;tick(s,frames);check(Math.abs(s.progress()-300)<.05f && s.bosses.mini()!=null && s.bosses.lich()==null,"real clock Mini "+mode);tick(s,frames);check(Math.abs(s.progress()-600)<.05f && s.bosses.lich()!=null && !s.victory(),"real clock Final "+mode);s.end();}
    }
    static void placementAndCaps() {
        for(int seed:new int[]{1,42,12345,987654}) {
            var layout=ForestGenerator.generate(seed);var c=config(Difficulty.HARD,GameMode.NORMAL);
            float sx=(ForestGenerator.CENTER_X+.5f)*ForestGenerator.TILE_SIZE,sy=(ForestGenerator.CENTER_Y+.5f)*ForestGenerator.TILE_SIZE;
            var s=new GameSession(c,layout.grid,sx,sy,null,null,seed);
            s.bosses.prepare(.1f,600,s.player,s.player.x-139,s.player.y-104,s.player.x+139,s.player.y+104);
            check(s.bosses.mini()!=null && s.bosses.lich()!=null,"real procedural placement "+seed);
            for(int i=0;i<s.enemies.size();i++){var e=s.enemies.get(i);check(layout.grid.canOccupy(e.x,e.y,9,6,false),"large collider clear");}s.end();
        }
        var c=config(Difficulty.HARD,GameMode.NORMAL);var s=new GameSession(c,open(),600,600,null,null,1);
        for(int i=0;i<60;i++)check(s.enemies.spawn(EnemyType.SLIME,100+(i%10)*24,100+(i/10)*24)!=null,"60 normal residents");prepare(s,600);check(s.enemies.normalSize()==60 && s.enemies.bossSize()==2 && s.enemies.size()==62,"separate 60+2 budgets");check(s.enemies.spawn(EnemyType.SLIME,400,400)==null && s.enemies.spawnBoss(c.bosses.lich.body(),c.bosses.stats(c.bosses.lich,c.difficulty,c.mode),500,500)==null,"hard limits");s.end();
        byte[] masks=new byte[6400];for(int y=0;y<80;y++)for(int x=0;x<40;x++)masks[y*80+x]=(byte)CollisionGrid.TREE;
        var system=new EnemySystem(new NavigationGrid(new CollisionGrid(80,80,16,masks)),null,c);var boss=new BossSystem(c,system,1);var p=new Player(100,600);
        boss.prepare(.1f,600,p,0,496,280,704);check(boss.finalState()==BossSystem.EventState.SPAWNING,"invalid player area retains event");p.x=900;
        for(int i=0;i<10;i++)boss.prepare(.1f,600,p,760,496,1040,704);check(boss.mini()!=null && boss.lich()!=null && system.bossSize()==2,"both pending events recover after player reaches valid area");
        for(float[] position:new float[][]{{24,24},{1256,24},{24,1256},{1256,1256},{200,600}}) {
            var edge=new GameSession(c,open(),position[0],position[1],null,null,1);float cx=Math.max(140,Math.min(1140,position[0])),cy=Math.max(104,Math.min(1176,position[1]));
            edge.bosses.prepare(.1f,600,edge.player,cx-140,cy-104,cx+140,cy+104);check(edge.bosses.mini()!=null && edge.bosses.lich()!=null,"camera-clamped map-edge spawn");edge.end();
        }
    }
    static void telegraphs() {
        for(var mode:GameMode.values()) {
            var c=config(Difficulty.EASY,mode);var s=new GameSession(c,open(),600,600,null,null,42);prepare(s,600);var b=s.bosses.lich();b.actor.x=700;b.actor.y=600;s.bosses.mini().actor.x=100;s.bosses.mini().actor.y=100;
            for(int i=0;i<160;i++){prepare(s,600);s.bosses.fight(.05f,600,s.player);s.player.update(.05f,0,0,openGrid);s.enemies.beginFrame();s.enemies.update(.05f,s.player);}
            check(b.phase()==1 && b.primaryCasts>0 && b.areaCasts>0,"phase 1 real bolt + AoE "+mode);
            boolean cast=false;
            for(int i=0;i<200;i++) {
                prepare(s,600);
                if(b.cast()==BossInstance.Cast.AREA) {cast=true;int hp=s.player.hp();float aimX=b.warningX(),aimY=b.warningY();s.player.x=aimX-100;s.player.y=aimY;
                    check(b.warningProgress()<.1f,"telegraph visible before impact");
                    for(int j=0;j<25;j++){prepare(s,600);s.bosses.fight(.05f,600,s.player);s.player.update(.05f,0,0,openGrid);}
                    check(s.player.hp()==hp && b.warningX()==aimX && b.warningY()==aimY,"snapshot AoE is dodgeable and does not follow player");break;}
                s.bosses.fight(.05f,600,s.player);s.player.update(.05f,0,0,openGrid);
            }
            check(cast,"second area telegraph reached");s.end();
        }
    }
    static void balanceProbe()throws Exception {
        var rows=new StringBuilder("difficulty,mode,boss,weaponLevel,baseDamage,secondsToKill,scenario\n");
        for(var d:Difficulty.values())for(var mode:GameMode.values())for(int level:new int[]{1,5})for(int damage:new int[]{6,12})for(boolean finalBoss:new boolean[]{false,true}) {
            var c=new GameConfig.Builder().difficulty(d).mode(mode).playerHp(100000).population(0,40).sword(damage,48,1,.15f).stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();
            var system=new EnemySystem(new NavigationGrid(openGrid),null,c);var def=finalBoss?c.bosses.lich:c.bosses.miniPool.get(0);var actor=system.spawnBoss(def.body(),c.bosses.stats(def,d,mode),632,600);actor.limitBurst(c.bosses.burstFraction,c.bosses.burstWindow);
            var loadout=new WeaponLoadout(c.weapons);for(int i=0;i<level;i++){if(i>0)loadout.grant(WeaponType.SWORD);loadout.grant(WeaponType.FIREBALL);loadout.grant(WeaponType.LIGHTNING);}var combat=new com.example.monstersurvivor.combat.SwordCombat(c,loadout.get(WeaponType.SWORD));var weapons=new WeaponSystem(c,loadout,combat);var player=new Player(600,600,null,c);int frame=0;
            for(;frame<2400 && actor.alive();frame++){player.update(.05f,0,0,openGrid);actor.advance(.05f);weapons.update(.05f,player,system,openGrid);}
            check(!actor.alive() && frame*.05f>=.8f && frame*.05f<=120,"bounded weapon throughput calibration");rows.append(d).append(',').append(mode).append(',').append(finalBoss?"Lich":"Mini").append(',').append(level).append(',').append(damage).append(',').append(frame*.05f).append(",stationary_single_target_NoBossAI\n");
        }
        java.nio.file.Files.writeString(java.nio.file.Path.of(System.getProperty("ms.report","../core/build/reports/task05/"),"weapon-ttk.csv"),rows);
    }
    public static void main(String[] args)throws Exception {events();projectiles();combat();victory();placementAndCaps();telegraphs();balanceProbe();System.out.println("TASK05 BOSS LOGIC PASSED: "+checks+" assertions");}
}
