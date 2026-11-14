import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.assets.GameAssets.*;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.spawn.EnemySpawner;
import com.example.monstersurvivor.world.CollisionGrid;

/** Deterministic behavioral scenarios, no GL context or test framework required. */
public final class CombatChecks {
    private static int checks;
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message);checks++; }
    private static void close(float a,float b,float tolerance,String message) {check(Math.abs(a-b)<=tolerance,message+": "+a+" / "+b);}
    private static CollisionGrid open() {return new CollisionGrid(100,100,16,new byte[10000]);}
    private static GameConfig quiet() {return new GameConfig.Builder().population(0,60).build();}
    private static void tick(GameSession s,float dt,boolean paused) {s.update(dt,0,0,paused,640,688,960,928);}
    private static CollisionGrid forest() {
        JsonValue v=new JsonReader().parse(new FileHandle("game/forest.json"));byte[] data=new byte[10000];
        JsonValue mask=v.get("collision").child;for(int i=0;i<data.length;i++,mask=mask.next)data[i]=(byte)mask.asInt();
        return new CollisionGrid(100,100,16,data);
    }
    private static void pathfinding() {
        byte[] data=new byte[20*20];for(int y=0;y<19;y++)if(y!=15)data[y*20+10]=CollisionGrid.TREE;
        CollisionGrid wall=new CollisionGrid(20,20,16,data);NavigationGrid nav=new NavigationGrid(wall);
        AStarPathfinder astar=new AStarPathfinder(nav);IntArray path=new IntArray();int start=nav.cell(120,88),goal=nav.cell(216,88);
        check(!wall.clearLine(120,88,216,88,5,3,false),"Wall blocks direct steering");
        check(astar.find(start,goal,false,path),"A* finds detour to gap");
        int previous=start;boolean gap=false;
        for(int i=0;i<path.size;i++) {
            int id=path.get(i);check(nav.walkable(id,false),"Every path node is walkable");
            check(Math.abs(id%20-previous%20)+Math.abs(id/20-previous/20)==1,"Path has cardinal edges, no corner cuts");
            check(wall.clearLine(nav.x(previous),nav.y(previous),nav.x(id),nav.y(id),5,3,false),"Footprint clears each path edge");
            if(id==15*20+10)gap=true;previous=id;
        }
        check(gap && previous==goal,"Path uses the only nearby wall gap");
        // Closing all gaps divides the graph; a failed search clears any previous path.
        for(int y=0;y<20;y++)data[y*20+10]=CollisionGrid.ROCK;
        nav=new NavigationGrid(new CollisionGrid(20,20,16,data));astar=new AStarPathfinder(nav);
        check(!astar.find(start,goal,false,path) && path.size==0,"No-path result is safe and clears stale waypoints");
        check(!astar.find(-1,goal,false,path),"Out-of-bounds path request fails safely");
        for(int y=0;y<20;y++)data[y*20+10]=CollisionGrid.WATER;
        CollisionGrid water=new CollisionGrid(20,20,16,data);nav=new NavigationGrid(water);astar=new AStarPathfinder(nav);
        check(!astar.find(start,goal,false,path),"Ground cannot navigate water");
        check(astar.find(start,goal,true,path),"Bat can navigate water");
        check(!water.canOccupy(168,88,5,3) && water.canOccupy(168,88,5,3,true),"Flying occupancy ignores water only");
        check(!water.canOccupy(-1,88,5,3,true),"Flying still respects map boundary");
        data[5*20+10]=(byte)(CollisionGrid.WATER|CollisionGrid.TREE);
        check(!new CollisionGrid(20,20,16,data).canOccupy(168,88,5,3,true),"Bat cannot fly through trees on water");
        // Exercise production steering around the wall, not only graph output.
        EnemySystem system=new EnemySystem(new NavigationGrid(wall),null,quiet());
        Enemy e=system.spawn(EnemyType.SLIME,120,88);Player target=new Player(216,88);
        for(int frame=0;frame<1800;frame++) {system.beginFrame();system.update(1f/60,target);check(wall.canOccupy(e.x,e.y,5,3),"AI detour never penetrates obstacles");}
        check(Math.hypot(e.x-target.x,e.y-target.y)<14,"AI reaches player after navigating wall");
        check(system.totalSearches>0,"AI actually invokes A*");
        EnemySystem flyers=new EnemySystem(new NavigationGrid(water),null,quiet());Enemy bat=flyers.spawn(EnemyType.BAT,120,88);
        for(int i=0;i<240;i++) {flyers.beginFrame();flyers.update(1f/60,target);}
        check(bat.x>176,"Bat physically crosses water stripe");
    }
    private static void healthAndCombat() {
        GameConfig config=quiet();CollisionGrid grid=open();Player p=new Player(808,808,null,config);
        check(p.hp()==10 && p.maxHp()==10,"GDD initial HP");
        check(p.damage(1) && p.hp()==9 && p.state()==State.HURT,"Contact damage starts Hurt");
        float t=p.animationTime;
        check(!p.damage(1) && p.hp()==9 && p.animationTime==t,"Same-frame crowd hit rejected");
        p.update(.699f,0,0,grid);check(!p.damage(1),"Invulnerability lasts at least .699s");
        p.update(.002f,0,0,grid);check(p.damage(1) && p.hp()==8,"Next hit allowed after .7s");
        p.update(.701f,0,0,grid);check(p.damage(100) && p.hp()==0 && p.state()==State.DEATH,"Lethal hit clamps HP and starts Death");
        check(!p.damage(1),"Dead player rejects damage");
        p.update(1.4f,1,0,grid);check(!p.deathFinished() && p.x==808,"Dead player stays put while animation plays");
        p.update(.11f,0,0,grid);check(p.deathFinished(),"Player death animation completes");
        p=new Player(808,808,null,config);EnemySystem enemies=new EnemySystem(new NavigationGrid(grid),null,config);
        Enemy near=enemies.spawn(EnemyType.SLIME,828,808),far=enemies.spawn(EnemyType.SLIME,840,808),behind=enemies.spawn(EnemyType.SLIME,784,808);
        SwordCombat sword=new SwordCombat(config);sword.update(p,enemies,grid);
        check(p.state()==State.ATTACK && p.direction==Direction.RIGHT,"Auto-sword faces nearest enemy");
        p.update(.44f,0,0,grid);sword.update(p,enemies,grid);check(near.hp()==2,"Windup has no premature hit");
        p.update(.02f,0,0,grid);sword.update(p,enemies,grid);
        check(near.hp()==1 && far.hp()==1 && behind.hp()==2,"Single sword event damages frontal sector only");
        for(int i=0;i<20;i++){p.update(.05f,0,0,grid);sword.update(p,enemies,grid);}
        check(near.hp()==1 && sword.hitEvents==1,"A swing cannot apply damage every frame");
        check(p.state()==State.ATTACK && p.animationTime>1.4f,"Attack is not reset every render");
        p.update(2,0,0,grid);sword.update(p,enemies,grid);p.update(.46f,0,0,grid);sword.update(p,enemies,grid);
        check(!near.alive() && !far.alive() && sword.kills==2,"Second swing kills exactly once per enemy");
        float death=near.animationTime;check(!near.damage(1) && near.animationTime==death,"Dead enemy cannot be hit or reset");
        enemies.beginFrame();enemies.update(1,p);check(enemies.size()==3,"Corpse retained before slime Death completes");
        enemies.beginFrame();enemies.update(1.05f,p);check(enemies.size()==1,"Corpse reclaimed after full source duration");
        Enemy e=enemies.get(0);e.damage(1);e.advance(.2f);float before=e.animationTime;e.damage(1);
        check(e.state()==State.DEATH && e.animationTime==0,"Lethal damage transitions Hurt to Death once");
        // Trees prevent both targeting and contact across a wall.
        byte[] data=new byte[100];data[5*10+5]=CollisionGrid.TREE;CollisionGrid blocked=new CollisionGrid(10,10,16,data);
        p=new Player(76,88,null,config);enemies=new EnemySystem(new NavigationGrid(blocked),null,config);
        enemies.spawn(EnemyType.SLIME,101,88);sword=new SwordCombat(config);sword.update(p,enemies,blocked);
        check(sword.swings==0,"Sword cannot acquire target through tree");
    }
    private static void spawningAndBudget() {
        CollisionGrid grid=forest();GameConfig config=GameConfig.defaults();NavigationGrid nav=new NavigationGrid(grid);
        EnemySystem enemies=new EnemySystem(nav,null,config);EnemySpawner spawner=new EnemySpawner(config,enemies,42);
        Player p=new Player(808,808);
        for(int frame=0;frame<600;frame++)spawner.update(1f/60,0,p,640,688,960,928);
        check(enemies.size()==7,"Initial target population is 5-10 enemies");
        for(int i=0;i<enemies.size();i++) {
            Enemy e=enemies.get(i);
            check(e.x<560 || e.x>1040 || e.y<608 || e.y>1008,"Spawn fully outside camera with sprite margin");
            check(nav.connected(nav.cell(e.x,e.y),nav.cell(p.x,p.y),e.type.flying),"Spawn connected to player");
            check(grid.canOccupy(e.x,e.y,5,3,e.type.flying),"Spawn has valid footprint");
        }
        EnemySystem fullView=new EnemySystem(nav,null,config);EnemySpawner impossible=new EnemySpawner(config,fullView,42);
        for(int i=0;i<30;i++)impossible.update(.1f,0,p,0,0,1600,1600);
        check(fullView.size()==0 && impossible.failedAttempts>0,"Full-map viewport fails spawn finitely");
        for(int cap:new int[]{10,30,60}) {
            GameConfig tuning=new GameConfig.Builder().population(0,cap).pathBudget(2).build();enemies=new EnemySystem(nav,null,tuning);
            for(int id=0;id<10000 && enemies.size()<cap;id++)if(nav.connected(id,5050,false))enemies.spawn(EnemyType.SLIME,nav.x(id),nav.y(id));
            check(enemies.size()==cap && enemies.spawn(EnemyType.BAT,808,808)==null,"Absolute/configured resident cap "+cap);
            long start=System.nanoTime();
            for(int frame=0;frame<360;frame++) {
                enemies.beginFrame();enemies.update(1f/60,p);
                check(enemies.searchesThisFrame<=2,"Frame A* budget at population "+cap);
                for(int i=0;i<enemies.size();i++){Enemy e=enemies.get(i);check(grid.canOccupy(e.x,e.y,5,3,e.type.flying),"Crowd remains outside static obstacles");}
            }
            System.out.printf(java.util.Locale.ROOT,"LOGIC %d enemies: %.3f ms/update over 360 steps (CPU only, no rendering)%n",cap,(System.nanoTime()-start)/1e6/360);
            check(enemies.totalSearches>0,"Stress case exercises obstacle navigation "+cap);
        }
        // Separation resolves exact overlaps without moving either actor through static geometry.
        enemies=new EnemySystem(new NavigationGrid(open()),null,quiet());Enemy a=enemies.spawn(EnemyType.SLIME,808,808),b=enemies.spawn(EnemyType.SLIME,808,808);
        for(int i=0;i<120;i++){enemies.beginFrame();enemies.update(1f/60,p);}
        check(Math.hypot(a.x-b.x,a.y-b.y)>8,"Soft separation disperses coincident enemies");
    }
    private static void sessionAndModes() {
        GameConfig normal=quiet(),demo=new GameConfig.Builder().mode(GameMode.DEMO).population(0,60).build();
        GameSession a=new GameSession(normal,open(),808,808,null),b=new GameSession(demo,open(),808,808,null);
        for(int i=0;i<600;i++) {a.update(1f/60,1,0,false,0,0,1600,1600);b.update(1f/60,1,0,false,0,0,1600,1600);}
        close(a.elapsed(),10,.01f,"Normal uses gameplay seconds");close(b.progress(),10*600/90f,.02f,"Demo has separate accelerated progress");
        close(a.player.x,b.player.x,.001f,"Demo leaves physical movement speed unchanged");
        close(a.player.animationTime,b.player.animationTime,.001f,"Demo leaves animation speed unchanged");
        GameConfig hard=new GameConfig.Builder().difficulty(Difficulty.HARD).build();
        check(hard.spawnInterval<normal.spawnInterval && hard.targetPopulation(120)>GameConfig.defaults().targetPopulation(120),"HARD has greater spawn pressure");
        check(GameConfig.fromIntent("HARD","DEMO").mode==GameMode.DEMO && GameConfig.fromIntent(null,"bad").difficulty==Difficulty.EASY,"Intent mapping/defaults");
        GameSession s=new GameSession(normal,open(),808,808,null);Enemy e=s.enemies.spawn(EnemyType.SLIME,830,808);tick(s,.1f,false);
        float elapsed=s.elapsed(),progress=s.progress(),x=e.x,animation=e.animationTime,cooldown=s.player.cooldown();int size=s.enemies.size(),hp=s.player.hp();
        for(int i=0;i<120;i++)tick(s,.1f,true);
        check(s.elapsed()==elapsed && s.progress()==progress && e.x==x && e.animationTime==animation && s.enemies.size()==size
            && s.player.cooldown()==cooldown && s.player.hp()==hp,"Pause freezes timer, AI, animation, combat cooldown, HP and spawn");
        tick(s,.1f,false);check(s.elapsed()>elapsed,"Resume advances simulation");
        s.player.damage(100);tick(s,.1f,false);elapsed=s.elapsed();x=e.x;
        check(s.phase()==GameSession.Phase.DYING,"Lethal HP starts DYING");
        for(int i=0;i<13;i++)tick(s,.1f,false);
        check(s.phase()==GameSession.Phase.DYING,"Game Over waits for full Death clip");
        for(int i=0;i<3;i++)tick(s,.1f,false);
        check(s.phase()==GameSession.Phase.GAME_OVER && s.elapsed()==elapsed && e.x==x,"Death freezes gameplay then produces Game Over");
        tick(s,.1f,false);check(s.elapsed()==elapsed,"Game Over never resumes combat");
        // No boss exists: crossing the nominal NORMAL/DEMO end time cannot award Victory.
        for(GameConfig c:new GameConfig[]{normal,demo}) {
            s=new GameSession(c,open(),808,808,null);
            for(int i=0;i<6101;i++)s.update(.1f,0,0,false,0,0,1600,1600);
            check(s.phase()==GameSession.Phase.RUNNING && s.progress()>600,"No automatic Victory at timeline threshold "+c.mode);
        }
        float[] positions=new float[3];int n=0;
        for(int fps:new int[]{30,60,120}) {
            EnemySystem system=new EnemySystem(new NavigationGrid(open()),null,normal);Enemy mover=system.spawn(EnemyType.SLIME,400,400);Player target=new Player(800,400);
            for(int i=0;i<fps*2;i++){system.beginFrame();system.update(1f/fps,target);}positions[n++]=mover.x;
        }
        close(positions[0],positions[1],.1f,"Enemy movement independent of FPS 30/60");close(positions[1],positions[2],.1f,"Enemy movement independent of FPS 60/120");
    }
    public static void main(String[] args) {
        pathfinding();healthAndCombat();spawningAndBudget();sessionAndModes();
        System.out.println("COMBAT CHECKS PASSED: "+checks+" assertions");
    }
}
