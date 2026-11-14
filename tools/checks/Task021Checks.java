import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.world.*;

/** Task 02.1 behavioral checks, split by implementation phase to avoid repeated heavy suites. */
public final class Task021Checks {
    private static int checks;
    private static void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);checks++;}
    private static void close(float a,float b,float tolerance,String label) {check(Math.abs(a-b)<=tolerance,label+": "+a+" / "+b);}
    private static CollisionGrid open() {return new CollisionGrid(100,100,16,new byte[10000]);}
    private static void combat() {
        GameConfig config=new GameConfig.Builder().population(0,60).build();CollisionGrid grid=open();
        Player p=new Player(808,808,null,config);EnemySystem enemies=new EnemySystem(new NavigationGrid(grid),null,config);
        Enemy rear=enemies.spawn(EnemyType.SLIME,778,808);int hp=rear.hp();SwordCombat sword=new SwordCombat(config);
        sword.update(p,enemies,grid);
        for(int i=0;i<10;i++){p.update(1f/60,1,0,grid);sword.update(p,enemies,grid);}
        check(p.movementDirection==Direction.RIGHT && p.attackDirection==Direction.LEFT,"Run right while attacking behind to left");
        close(p.x,808+80f/6,.01f,"Attack never stops movement");
        check(rear.hp()==hp-1 && sword.hitEvents==1,"Rear target receives one fast windup hit");
        for(int i=0;i<20;i++){p.update(1f/60,0,0,grid);sword.update(p,enemies,grid);}
        check(sword.hitEvents==1,"No repeated damage within swing");
        check(p.animationTime>.45f && p.animationTime<.6f,"Attack clock does not restart");
        check(p.renderAnimationTime()>2 && p.renderAnimationTime()<3,"Source clip playback is scaled to gameplay time");
        p=new Player(808,808,null,config);enemies=new EnemySystem(new NavigationGrid(grid),null,config);sword=new SwordCombat(config);
        Enemy first=enemies.spawn(EnemyType.WILD_BOAR,832,808),other=enemies.spawn(EnemyType.WILD_BOAR,808,838);
        sword.update(p,enemies,grid);p.update(.05f,0,0,grid);other.y=820;sword.update(p,enemies,grid);
        check(sword.target()==first,"Valid windup target remains stable when another becomes nearer");
        first.damage(100);float clock=p.animationTime;sword.update(p,enemies,grid);
        check(sword.target()==other && p.attackDirection==Direction.UP && p.animationTime==clock,"Dead target retargets immediately without resetting animation");
        int otherHp=other.hp();p.update(.11f,0,0,grid);sword.update(p,enemies,grid);check(other.hp()==otherHp-1,"Retarget uses actual current enemy position");
        p=new Player(808,808,null,config);enemies=new EnemySystem(new NavigationGrid(grid),null,config);sword=new SwordCombat(config);
        Enemy edge=enemies.spawn(EnemyType.WILD_BOAR,868,808);sword.update(p,enemies,grid);
        check(sword.target()==edge && sword.swings==0,"Detection range does not extend hit range");
        edge.x=856.01f;sword.update(p,enemies,grid);check(sword.swings==0,"No attack beyond 48 units");
        edge.x=855.9f;sword.update(p,enemies,grid);check(sword.swings==1,"Attack begins within 48 units");
        edge.x=870;p.update(.16f,0,0,grid);sword.update(p,enemies,grid);
        check(edge.hp()==config.enemy(EnemyType.WILD_BOAR).hp && sword.hitEvents==0 && sword.canceledSwings==1,"Escaped target cancels swing without fake damage");
        byte[] masks=new byte[100];masks[55]=CollisionGrid.TREE;CollisionGrid wall=new CollisionGrid(10,10,16,masks);
        p=new Player(72,88,null,config);enemies=new EnemySystem(new NavigationGrid(wall),null,config);enemies.spawn(EnemyType.SLIME,104,88);
        sword=new SwordCombat(config);sword.update(p,enemies,wall);check(sword.swings==0,"Targeting never crosses a tree");
        for(int fps:new int[]{30,60,120}) {
            p=new Player(808,808,null,config);enemies=new EnemySystem(new NavigationGrid(grid),null,config);enemies.spawn(EnemyType.WILD_BOAR,838,808);
            sword=new SwordCombat(config);sword.update(p,enemies,grid);
            for(int frame=0;frame<fps*.2f;frame++){p.update(1f/fps,0,0,grid);sword.update(p,enemies,grid);}
            check(sword.hitEvents==1,"One windup hit across FPS "+fps);
        }
    }
    public static void main(String[] args) {
        if(args.length==0 || args[0].equals("a"))combat();else if(args[0].equals("b"))balance();else if(args[0].equals("d"))maps();else collisionAndUpdate();
        System.out.println("TASK 02.1 PHASE "+(args.length==0?"a":args[0])+" PASSED: "+checks+" checks");
    }
    private static void maps() {
        long[] seeds={42,20261113,8675309};int previousHash=0;
        for(long seed:seeds) {
            long start=System.nanoTime();ForestGenerator.Layout layout=ForestGenerator.generate(seed);CollisionGrid grid=layout.grid;
            check(!layout.fallback && layout.seed==seed,"Requested seed generates normally");
            check(grid.width==200 && grid.height==200 && layout.walkableTiles>=34000,"200x200 map with at least 85 percent walkable");
            check(grid.canOccupy(1608,1608,4,3),"Player spawn is safe");
            NavigationGrid nav=new NavigationGrid(grid);int free=0,trees=0,rocks=0,water=0,hash=1;
            for(int y=0;y<200;y++)for(int x=0;x<200;x++) {
                int mask=grid.mask(x,y);hash=31*hash+mask;
                if(mask==0) {free++;check(nav.connected(y*200+x,20100,false),"All walkable nodes connect to spawn");}
                if(mask==CollisionGrid.TREE)trees++;if(mask==CollisionGrid.ROCK)rocks++;if(mask==CollisionGrid.WATER)water++;
                float dx=(x-100)/22f,dy=(y-100)/18f;
                if(dx*dx+dy*dy<=1)check(mask==0,"Large center clearing is protected");
                if(x==0 || x==199 || y==0 || y==199)check(mask==CollisionGrid.BOUNDARY,"Map boundary is closed");
                check(layout.terrain[y*200+x]!=null,"Terrain is fully assigned");
            }
            check(free==layout.walkableTiles && trees>0 && rocks>0 && water>0,"Each seed contains real forest, rocks and ponds");
            ForestGenerator.Layout again=ForestGenerator.generate(seed);
            for(int y=0;y<200;y++)for(int x=0;x<200;x++)check(grid.mask(x,y)==again.grid.mask(x,y),"Seed reproduces identical collision");
            check(java.util.Arrays.equals(layout.terrain,again.terrain) && layout.props.size==again.props.size,"Seed reproduces visuals");
            check(previousHash==0 || previousHash!=hash,"Different seeds produce different layouts");previousHash=hash;
            AStarPathfinder finder=new AStarPathfinder(nav);com.badlogic.gdx.utils.IntArray path=new com.badlogic.gdx.utils.IntArray();
            for(int corner:new int[]{201,398,39601,39798})check(finder.find(20100,corner,false,path),"A* reaches each outer map quadrant");
            GameConfig config=new GameConfig.Builder().population(6,40).build();EnemySystem enemies=new EnemySystem(nav,null,config);
            var spawner=new com.example.monstersurvivor.spawn.EnemySpawner(config,enemies,seed);Player player=new Player(1608,1608);
            for(int i=0;i<180;i++)spawner.update(.1f,0,player,1388,1504,1828,1712);
            check(enemies.size()==6,"Spawner uses generated navigation");
            for(int i=0;i<enemies.size();i++){Enemy e=enemies.get(i);check(grid.canOccupy(e.x,e.y,5,3,e.definition.flying),"Spawn never occupies generated obstacles");}
            for(int frame=0;frame<180;frame++) {
                enemies.beginFrame();enemies.update(1f/30,player);check(enemies.searchesThisFrame<=2,"Generated-map A* budget");
                for(int i=0;i<enemies.size();i++){Enemy e=enemies.get(i);check(grid.canOccupy(e.x,e.y,5,3,e.definition.flying),"AI respects generated collision");}
            }
            System.out.println("SEED "+seed+" free="+free+" trees="+trees+" rocks="+rocks+" water="+water+" props="+layout.props.size
                +" repairs="+layout.repairs+" hash="+hash+" checkMs="+(System.nanoTime()-start)/1e6);
        }
        var fallback=ForestGenerator.fallback(123);NavigationGrid nav=new NavigationGrid(fallback.grid);
        check(fallback.fallback && fallback.walkableTiles>=34000 && nav.connected(20100,201,false),"Fallback remains valid and connected");
    }
    private static void collisionAndUpdate() {
        byte[] data=new byte[400];data[10*20+10]=CollisionGrid.TREE;
        CollisionGrid grid=new CollisionGrid(20,20,16,data);
        check(!grid.clearLine(80,168,240,168,5,3,false),"DDA hits tree horizontally");
        check(!grid.clearLine(168,80,168,240,5,3,false),"DDA hits tree vertically");
        check(!grid.clearLine(120,120,220,220,5,3,false),"DDA detects diagonal corner");
        check(grid.clearLine(80,140,240,140,5,3,false),"DDA leaves clear corridor open");
        check(grid.clearLine(80,157,240,157,5,3,false),"Foot can touch obstacle edge without penetrating");
        check(!grid.clearLine(80,157.01f,240,157.01f,5,3,false),"Footprint catches shallow grazing collision");
        check(!grid.clearLine(-1,100,200,100,5,3,true),"Line cannot leave map");
        data[210]=CollisionGrid.WATER;grid=new CollisionGrid(20,20,16,data);
        check(grid.clearLine(80,168,240,168,5,3,true) && !grid.clearLine(80,168,240,168,5,3,false),"Flying line ignores water only");
        com.badlogic.gdx.math.RandomXS128 random=new com.badlogic.gdx.math.RandomXS128(42);
        for(int i=0;i<400;i++) {
            float x=8+random.nextFloat()*304,y=8+random.nextFloat()*304,tx=8+random.nextFloat()*304,ty=8+random.nextFloat()*304;
            boolean line=grid.clearLine(x,y,tx,ty,5,3,false);
            check(line==grid.clearLine(tx,ty,x,y,5,3,false),"Swept line is direction symmetric");
            if(line)for(int j=0;j<=100;j++)check(grid.canOccupy(x+(tx-x)*j/100,y+(ty-y)*j/100,5,3),"Clear sweep never crosses blocked footprint");
        }
        GameConfig c=new GameConfig.Builder().population(0,60).build();
        com.example.monstersurvivor.debug.FrameMetrics metrics=new com.example.monstersurvivor.debug.FrameMetrics();
        var session=new com.example.monstersurvivor.gameplay.GameSession(c,open(),808,808,null,metrics);
        session.enemies.spawn(EnemyType.SLIME,700,808);session.enemies.spawn(EnemyType.SLIME,730,808);
        metrics.reset();session.update(.08f,1,0,false,0,0,1600,1600);
        check(metrics.separationPairs==1,"Long frame runs separation/AI once, not three times");
        close(session.player.x,814.4f,.01f,"Long frame keeps delta-based movement");
        float elapsed=session.elapsed(),enemyX=session.enemies.get(0).x;session.update(.1f,1,0,true,0,0,1600,1600);
        check(session.elapsed()==elapsed && session.enemies.get(0).x==enemyX,"Pause still freezes simulation");
        combat();
    }
    private static void balance() {
        for(Difficulty difficulty:Difficulty.values()) {
            GameConfig c=new GameConfig.Builder().difficulty(difficulty).build();boolean hard=difficulty==Difficulty.HARD;
            check(c.playerHp==10 && c.swordDamage==1,"Player baseline retained");
            int[] hp=hard?new int[]{1,2,3}:new int[]{1,1,2};
            for(EnemyType type:EnemyType.values()) {
                check(c.enemy(type).hp==hp[type.ordinal()] && c.enemy(type,119.99f).hp==hp[type.ordinal()],"Friendly first two minutes "+difficulty+type);
                EnemySystem system=new EnemySystem(new NavigationGrid(open()),null,c);Enemy e=system.spawn(type,808,808);
                for(int hit=1;hit<hp[type.ordinal()];hit++)check(!e.damage(1) && e.alive(),"Expected nonlethal baseline hit");
                check(e.damage(1) && !e.alive() && e.hp()==0,"Expected hits to kill "+difficulty+type);
                check(c.enemy(type,600).hp<=hp[type.ordinal()]+(hard?5:4),"Task 04A.1 controlled stage HP");
            }
            check(c.initialEnemies==(hard?8:6) && c.maxEnemies==(hard?60:40),"Difficulty population limits");
            int previous=0;
            for(int second=0;second<=900;second++) {
                int target=c.targetPopulation(second);check(target>=previous && target<=c.maxEnemies,"Monotonic capped population "+difficulty);
                previous=target;
            }
            check(c.targetPopulation(119)<=(hard?15:11),"Task 04A.1 pressure begins after 60s");
            check(c.enemy(EnemyType.WILD_BOAR,210).hp==hp[2]+(hard?3:2) && c.enemy(EnemyType.SLIME,300).hp==hp[0]+(hard?2:1),"Staged HP changes");
            check(c.spawnInterval(300)<c.spawnInterval(0),"Spawn cadence ramps by stage");
        }
        GameConfig hard=new GameConfig.Builder().difficulty(Difficulty.HARD).build(),easy=GameConfig.defaults();
        for(int second:new int[]{0,120,210,300,450,600})check(hard.targetPopulation(second)>easy.targetPopulation(second),"Hard is moderately more populated");
        EnemySystem system=new EnemySystem(new NavigationGrid(open()),null,easy);
        Enemy early=system.spawn(EnemyType.SLIME,808,808,0),late=system.spawn(EnemyType.SLIME,840,808,300);
        check(early.hp()==1 && late.hp()==2,"Only newly spawned enemies receive progression HP");
    }
}
