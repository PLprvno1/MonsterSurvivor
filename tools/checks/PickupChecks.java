import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.world.*;

/** Task 03A behavior checks: actual effects, clocks, death events, ground placement and controls. */
public final class PickupChecks {
    private static int checks;
    private static void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);checks++;}
    private static void close(float actual,float expected,float epsilon,String label) {check(Math.abs(actual-expected)<=epsilon,label+": "+actual+" / "+expected);}
    private static CollisionGrid open() {return new CollisionGrid(100,100,16,new byte[10000]);}
    private static GameConfig config(GameMode mode,PickupConfig pickups) {
        // This clock fixture now crosses the live Mini Boss event; isolate pickup expiry from player death.
        return new GameConfig.Builder().mode(mode).pickups(pickups).playerHp(100000).population(0,60)
            .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();
    }
    private static void collect(PickupSystem items,Player p,CollisionGrid grid,PickupType type) {
        check(items.spawn(type,p.x,p.y,2)!=null,"Valid scripted placement");p.update(0,0,0,grid);items.update(0,0,p);
    }
    private static void stats() {
        var c=GameConfig.defaults();var grid=open();var nav=new NavigationGrid(grid);var p=new Player(808,808,null,c);
        var items=new PickupSystem(c,nav,p,42);boolean[] gains=new boolean[4];
        for(int i=0;i<36;i++) {
            int old=p.stats.damage();collect(items,p,grid,PickupType.DAMAGE_BOX);int gain=p.stats.damage()-old;
            check(gain>=1 && gain<=3,"Damage gain in [1,3]");gains[gain]=true;
        }
        check(gains[1] && gains[2] && gains[3],"All three damage rewards occur");
        int damage=p.stats.damage(),count=items.collectedItems;items.update(.1f,0,p);
        check(p.stats.damage()==damage && count==items.collectedItems,"Consumed pickup never applies twice");
        check(items.size()==0 && items.effectCount()<=8,"Effects are bounded separately from live items");
        items.update(.31f,0,p);check(items.effectCount()==0,"Collection render references retire");
        var combatConfig=new GameConfig.Builder().enemy(EnemyType.WILD_BOAR,200,1,1).build();
        var enemies=new EnemySystem(nav,null,combatConfig);var target=enemies.spawn(EnemyType.WILD_BOAR,p.x+28,p.y);
        var sword=new SwordCombat(combatConfig);sword.update(p,enemies,grid);p.update(.16f,0,0,grid);sword.update(p,enemies,grid);
        check(target.hp()==200-damage && sword.hitEvents==1,"Sword reads upgraded damage on real hit");
        var hitConfig=new GameConfig.Builder().population(0,60).enemy(EnemyType.WILD_BOAR,20,1,.01f).build();
        var hitSession=new GameSession(hitConfig,grid,808,808,null,null,42);
        var hitEnemy=hitSession.enemies.spawn(EnemyType.WILD_BOAR,840,808);
        hitSession.update(.1f,0,0,false,600,680,1000,920);hitSession.update(.1f,0,0,false,600,680,1000,920);
        hitSession.pickups.spawn(PickupType.DAMAGE_BOX,808,808,0);
        hitSession.update(.06f,0,0,false,600,680,1000,920);
        check(hitEnemy.hp()==20-hitSession.player.stats.damage() && hitSession.player.stats.damage()>1,"Same-frame contact upgrades the pending sword hit immediately");
        p=new Player(808,808,null,c);items=new PickupSystem(c,nav,p,42);
        collect(items,p,grid,PickupType.HEALTH_BOX);check(p.hp()==12 && p.maxHp()==12,"Full health box upgrades max and heals +2");
        boolean heal=false,max=false;
        for(int i=0;i<20;i++) {
            p.update(.8f,0,0,grid);p.damage(3);int hp=p.hp(),maximum=p.maxHp();
            collect(items,p,grid,PickupType.HEALTH_BOX);
            if(p.maxHp()>maximum) {max=true;check(p.maxHp()==maximum+2 && p.hp()==hp+2,"Max HP branch adds and heals two");}
            else {heal=true;check(p.hp()==Math.min(maximum,hp+3),"Heal branch caps at current max");}
            check(p.hp()<=p.maxHp(),"HP never exceeds max");p.heal(Integer.MAX_VALUE);
        }
        check(heal && max,"Both health outcomes exercised");
        p.update(.8f,0,0,grid);p.damage(1);int maxHp=p.maxHp();collect(items,p,grid,PickupType.HEALTH_DROP);
        check(p.hp()==maxHp,"Two-HP drop clamps to one missing HP");
        collect(items,p,grid,PickupType.HEALTH_DROP);check(items.healthDrops()==1,"Full-HP drop stays available instead of being wasted");
        p.update(.8f,0,0,grid);p.damage(Integer.MAX_VALUE);items.update(2,1000,p);
        check(p.hp()==0 && items.healthDrops()==1 && p.heal(100)==0,"Death forbids pickup, scheduling and resurrection");
        p=new Player(808,808,null,c);items=new PickupSystem(c,nav,p,4);
        for(int i=0;i<10;i++)collect(items,p,grid,PickupType.SPEED_BOX);
        check(p.stats.speed()==20 && p.stats.movementSpeed()==160,"Speed mapping reaches cap 20 / 160 units");
        damage=p.stats.damage();collect(items,p,grid,PickupType.SPEED_BOX);
        check(p.stats.speed()==20 && p.stats.damage()>damage,"Existing capped Speed box remains useful as Damage");
        float x=p.x,y=p.y;p.update(.1f,1,1,grid);
        close((float)Math.hypot(p.x-x,p.y-y),16,.005f,"Diagonal normalization at Speed 20");
        for(int fps:new int[]{30,60,120}) {
            p.x=p.y=808;x=p.x;for(int i=0;i<fps;i++)p.update(1f/fps,1,0,grid);
            close(p.x-x,160,.02f,"Speed remains delta based at "+fps+" FPS");
        }
        p.x=808;p.y=808;items=new PickupSystem(c,nav,p,44);check(items.spawn(PickupType.DAMAGE_BOX,816,808,0)!=null,"Fast-pass pickup placed");
        p.update(.1f,1,0,grid);damage=p.stats.damage();items.update(.1f,0,p);
        check(items.size()==0 && p.stats.damage()>damage,"Swept movement collects a box crossed at high speed");
        byte[] masks=new byte[100];for(int row=0;row<10;row++)masks[row*10+5]=CollisionGrid.TREE;
        grid=new CollisionGrid(10,10,16,masks);p=new Player(72,88,null,c);nav=new NavigationGrid(grid);items=new PickupSystem(c,nav,p,1);
        check(items.spawn(PickupType.DAMAGE_BOX,104,88,0)==null,"Scripted placement rejects disconnected ground");
        check(items.spawn(PickupType.DAMAGE_BOX,88,88,0)==null,"Scripted placement rejects blockers");
        for(int i=0;i<10;i++)p.stats.addSpeed();p.update(.1f,1,0,grid);
        check(p.x<=76.001f && grid.canOccupy(p.x,p.y,4,3),"Upgraded movement cannot tunnel through wall");
        check(!p.touchesMovement(104,88,5,4,grid),"Pickup contact never crosses the wall");
    }
    private static void clocksAndMaps() {
        for(GameMode mode:GameMode.values()) {
            var c=config(mode,new PickupConfig());var grid=open();var s=new GameSession(c,grid,808,808,null,null,42);
            float due=s.pickups.nextSpawnProgress();check(due>=15 && due<=20,"First event randomized within 15-20 progress seconds");
            for(int frame=0;s.progress()+.1f*mode.progressScale<due;frame++)s.update(.1f,0,0,false,600,680,1000,920);
            check(s.pickups.size()==0,"No early box event in "+mode);
            s.update(.1f,0,0,false,600,680,1000,920);check(s.pickups.size()==1,"One scheduled box in "+mode);
            var box=s.pickups.get(0);float age=box.age,progress=s.progress(),real=s.elapsed(),next=s.pickups.nextSpawnProgress();
            for(int i=0;i<100;i++)s.update(.1f,1,0,true,600,680,1000,920);
            check(box.age==age && s.elapsed()==real && s.progress()==progress && s.pickups.nextSpawnProgress()==next,"Pause freezes age, schedule and both clocks");
            int id=(int)box.id;boolean expired=false;
            for(int i=0;i<601;i++) {
                s.update(.1f,0,0,false,600,680,1000,920);check(s.pickups.ordinaryBoxes()<=6 && s.pickups.skillBoxes()<=2,"Independent ordinary/skill box caps hold in "+mode);
                boolean exists=false;for(int j=0;j<s.pickups.size();j++)if(s.pickups.get(j).id==id)exists=true;
                if(!exists) {check(box.age>=60 && box.age<60.11f,"Expiration uses 60 real gameplay seconds in "+mode);expired=true;break;}
            }
            check(expired,"Original box expires in "+mode);
            for(int i=0;i<10;i++)s.player.stats.addSpeed();
            int spawned=s.pickups.spawnedBoxes;
            while(s.pickups.spawnedBoxes<spawned+6) {
                s.update(.1f,0,0,false,600,680,1000,920);
                for(int j=0;j<s.pickups.size();j++)if(s.pickups.get(j).id>id+6)check(s.pickups.get(j).type!=PickupType.SPEED_BOX,"Capped speed excluded from new box choices");
            }
        }
        for(long seed:new long[]{42,20261113,8675309}) {
            var layout=ForestGenerator.generate(seed);var c=config(GameMode.DEMO,new PickupConfig());
            float sx=(ForestGenerator.CENTER_X+.5f)*ForestGenerator.TILE_SIZE,sy=(ForestGenerator.CENTER_Y+.5f)*ForestGenerator.TILE_SIZE;
            var a=new GameSession(c,layout.grid,sx,sy,null,null,seed);
            var b=new GameSession(c,layout.grid,sx,sy,null,null,seed);
            for(int i=0;i<420;i++) {
                a.update(.1f,0,0,false,sx-208,sy-128,sx+208,sy+128);b.update(.1f,0,0,false,sx-208,sy-128,sx+208,sy+128);
                check(a.pickups.size()==b.pickups.size(),"Seed reproduces schedule and caps");
                for(int j=0;j<a.pickups.size();j++) {
                    var p=a.pickups.get(j);var q=b.pickups.get(j);var nav=a.enemies.navigation;
                    check(p.type==q.type && p.x==q.x && p.y==q.y,"Seed reproduces types and positions");
                    check(nav.connected(nav.cell(sx,sy),nav.cell(p.x,p.y),false) && layout.grid.canOccupy(p.x,p.y,5,4),"Procedural item is reachable on valid ground");
                    check(Math.hypot(p.x-sx,p.y-sy)>=48,"Box never spawns under player");
                }
            }
        }
        byte[] blocked=new byte[400];java.util.Arrays.fill(blocked,(byte)CollisionGrid.ROCK);blocked[210]=0;
        var grid=new CollisionGrid(20,20,16,blocked);var c=config(GameMode.NORMAL,new PickupConfig());var p=new Player(168,168,null,c);
        var items=new PickupSystem(c,new NavigationGrid(grid),p,1);items.update(.1f,100,p);
        check(items.size()==0 && items.nextSpawnProgress()>100,"No reachable placement skips finitely and reschedules");
    }
    private static void dropsAndIndicators() {
        var tuning=new PickupConfig(15,20,60,6,20,1,1,0,1,2);var c=config(GameMode.NORMAL,tuning);var grid=open();
        var s=new GameSession(c,grid,808,808,null,null,8);var e=s.enemies.spawn(EnemyType.SLIME,840,808);
        e.damage(10);check(s.pickups.deathRolls==1 && s.pickups.healthDrops()==1,"Death transition triggers exactly one real drop roll");
        for(int i=0;i<10;i++) {e.damage(100);e.advance(.05f);}
        check(s.pickups.deathRolls==1 && s.pickups.healthDrops()==1,"Death animation and repeated damage never duplicate drops");
        for(int i=0;i<24;i++) {e=s.enemies.spawn(EnemyType.BAT,872+i%8*16,840+i/8*16);e.damage(100);}
        check(s.pickups.healthDrops()==20,"Health drop population capped independently of boxes");
        for(int i=0;i<s.pickups.size();i++)check(s.pickups.get(i).heal>=1 && s.pickups.get(i).heal<=2,"Configured heal range");
        close(new PickupConfig().dropChance(Difficulty.EASY,10),.08f,.00001f,"Easy chance");
        close(new PickupConfig().dropChance(Difficulty.HARD,10),.05f,.00001f,"Hard chance");
        close(new PickupConfig().dropChance(Difficulty.EASY,3),.12f,.00001f,"Low HP boosts chance");
        byte[] water=new byte[400];water[10*20+10]=CollisionGrid.WATER;grid=new CollisionGrid(20,20,16,water);
        s=new GameSession(c,grid,120,168,null,null,5);e=s.enemies.spawn(EnemyType.BAT,168,168);e.damage(10);
        check(s.pickups.size()==1 && grid.canOccupy(s.pickups.get(0).x,s.pickups.get(0).y,5,4),"Bat water death relocates drop to reachable ground");
        check(Math.hypot(s.pickups.get(0).x-168,s.pickups.get(0).y-168)<=96,"Relocated drop stays near corpse");
        var indicator=new PickupIndicator();
        for(boolean debug:new boolean[]{false,true})for(int degree=0;degree<360;degree+=5) {
            double angle=Math.toRadians(degree);
            check(indicator.locate((float)Math.cos(angle)*1000,(float)Math.sin(angle)*1000,0,0,506,240,1013,480,debug),"Off-camera box gets a direction");
            check(indicator.x>=28 && indicator.x<=985 && indicator.y>=56 && indicator.y<=372,"Indicator stays within safe screen bounds");
            check(!(indicator.x<210 && indicator.y<205),"Indicator avoids joystick");
        }
        check(!indicator.locate(10,10,0,0,506,240,1013,480,false),"Visible box has no edge arrow");
    }
    private static void micro() {
        // Small isolated CPU comparison, not an emulator FPS claim or a repeated AI stress suite.
        var c=config(GameMode.NORMAL,new PickupConfig());var grid=open();var p=new Player(808,808,null,c);var nav=new NavigationGrid(grid);
        var empty=new PickupSystem(c,nav,p,1);var full=new PickupSystem(c,nav,p,1);
        for(int i=0;i<26;i++)full.spawn(i<6?PickupType.DAMAGE_BOX:PickupType.HEALTH_DROP,840+i%8*16,840+i/8*16,2);
        for(int i=0;i<4000;i++) {empty.update(0,0,p);full.update(0,0,p);}
        long start=System.nanoTime();for(int i=0;i<20000;i++)empty.update(0,0,p);double base=(System.nanoTime()-start)/20000.0/1e3;
        start=System.nanoTime();for(int i=0;i<20000;i++)full.update(0,0,p);double loaded=(System.nanoTime()-start)/20000.0/1e3;
        check(full.size()==26,"CPU probe holds 6 boxes + 20 drops");
        System.out.printf(java.util.Locale.ROOT,"Pickup CPU probe (desktop JVM, 20,000 updates): empty %.3f us, 26 live %.3f us%n",base,loaded);
    }
    public static void main(String[] args) {stats();clocksAndMaps();dropsAndIndicators();micro();System.out.println("TASK 03A LOGIC PASSED: "+checks+" checks");}
}
