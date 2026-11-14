import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;

public final class Task03B1Checks {
    static int checks;
    static void check(boolean b,String s) {if(!b)throw new AssertionError(s);checks++;}
    static GameConfig config(GameMode mode) {return new GameConfig.Builder().mode(mode).population(0,60)
        .enemy(EnemyType.SLIME,100,1,.01f).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();}
    static GameSession session(GameMode mode) {return new GameSession(config(mode),new CollisionGrid(200,200,16,new byte[40000]),1608,1608,null,null,42);}
    static void tick(GameSession s,float dt,boolean pause) {s.update(dt,0,0,pause,1400,1480,1816,1736);}
    static void weapons() {
        for(WeaponType type:new WeaponType[]{WeaponType.FIREBALL,WeaponType.LIGHTNING})for(int level=1;level<=5;level++) {
            var s=session(GameMode.NORMAL);for(int i=0;i<level;i++)check(s.weapons.loadout.grant(type),"grant level");
            var a=s.enemies.spawn(EnemyType.SLIME,1730,1608);var b=s.enemies.spawn(EnemyType.SLIME,1698,1668);
            tick(s,.01f,false);var tuning=s.config.weapons.level(type,level);
            if(type==WeaponType.FIREBALL) {
                int expected=level==1?1:level<4?2:3;
                check(s.weapons.projectiles.size()==expected,"real projectile count level "+level);
                if(expected>1)check(s.weapons.projectiles.get(0).dy!=s.weapons.projectiles.get(1).dy,"distinct trajectories");
            } else {
                check(s.weapons.lightningInitialBolts==(level==5?2:1),"initial bolts level "+level);
                check(s.weapons.lightningHits==2,"two unique hits; roots cannot be stolen by chain");
                check(a.hp()==100-tuning.damage(1) && b.hp()==100-tuning.damage(1),"no duplicate chain damage");
            }
            float remaining=s.weapons.loadout.get(type).cooldownRemaining;tick(s,.1f,true);
            check(remaining==s.weapons.loadout.get(type).cooldownRemaining,"pause freezes cooldown");
            var single=session(GameMode.DEMO);for(int i=0;i<level;i++)single.weapons.loadout.grant(type);
            a=single.enemies.spawn(EnemyType.SLIME,1730,1608);tick(single,.01f,false);
            check(single.weapons.loadout.get(type).cooldownRemaining==tuning.cooldown,"Demo uses real cooldown");
            if(type==WeaponType.LIGHTNING)check(single.weapons.lightningInitialBolts==1 && a.hp()==100-tuning.damage(1),"single enemy never double hit");
            else {
                if(level>1)check(single.weapons.projectiles.get(0).dy!=single.weapons.projectiles.get(1).dy,"single target fan");
                for(int frame=0;frame<12;frame++)tick(single,.1f,false);
                check(a.hp()<100,"fan still damages a single small distant target");
                check(single.weapons.projectiles.explosions==tuning.projectileCount,"one explosion per projectile");
            }
        }
        var c=new WeaponConfig();check(c.level(WeaponType.FIREBALL,3).damage(1)>c.level(WeaponType.FIREBALL,2).damage(1),"Fire 3 damage");
        check(c.level(WeaponType.FIREBALL,5).damage(1)>c.level(WeaponType.FIREBALL,4).damage(1),"Fire 5 damage");
        check(c.level(WeaponType.LIGHTNING,3).boltWidth==2.5f,"25 percent thicker lightning");
        var s=session(GameMode.NORMAL);s.weapons.loadout.grant(WeaponType.FIREBALL);
        s.weapons.loadout.get(WeaponType.FIREBALL).cooldownRemaining=2.9f;for(int i=0;i<4;i++)s.weapons.loadout.grant(WeaponType.FIREBALL);
        check(s.weapons.loadout.get(WeaponType.FIREBALL).cooldownRemaining==2.1f,"upgrade clamps running cooldown safely");
    }
    static void night() {
        for(GameMode mode:GameMode.values()) {
            var s=session(mode);int threshold=mode==GameMode.DEMO?450:3000;
            for(int i=0;i<threshold-1;i++)tick(s,.1f,false);
            check(!s.night.active(),"day immediately before 300 progress "+mode);
            float before=s.progress();for(int i=0;i<100;i++)tick(s,.1f,true);
            check(s.progress()==before && s.night.strength()==0,"pause cannot trigger night");
            tick(s,.1f,false);check(s.night.active(),"night at exact progress threshold "+mode);
            for(int i=0;i<30;i++)tick(s,.1f,false);check(s.night.strength()==1,"three second fade");
            for(int i=0;i<s.pickups.size();i++)if(s.pickups.get(i).type.box)s.pickups.get(i).age=100;
            tick(s,.01f,false);
            s.pickups.spawn(com.example.monstersurvivor.pickup.PickupType.WEAPON_BOX,s.player.x,s.player.y,0);tick(s,.01f,false);
            check(s.choosingWeapon(),"night modal opens");before=s.progress();float strength=s.night.strength();
            for(int i=0;i<30;i++)tick(s,.1f,false);
            check(s.progress()==before && s.night.strength()==strength,"modal freezes night and progress");
            var fresh=session(mode);check(!fresh.night.active() && fresh.night.strength()==0,"Retry starts in day");
        }
    }
    static void zoom() {
        for(float zoom:new float[]{1.1f,1.15f,1.2f}) {
            var config=new WorldVisualConfig(zoom);float h=240/config.zoom,w=h*2280/1080;
            var camera=new com.example.monstersurvivor.render.PixelCamera(com.example.monstersurvivor.render.PixelCamera.Mode.SCREEN);
            for(int step=0;step<60;step++) {
                float x=1608+step*.17f;camera.update(x,1608,w,h,2280,1080,3200,3200);
                float anchor=(camera.playerX-camera.cameraX+w/2)*2280/w;
                check(Math.abs(anchor-(1140-1f/16))<.003,"stable physical pixel anchor zoom "+zoom);
            }
            camera.update(0,0,w,h,2280,1080,3200,3200);check(camera.cameraX==w/2 && camera.cameraY==h/2,"low edge clamp");
            camera.update(3200,3200,w,h,2280,1080,3200,3200);check(camera.cameraX==3200-w/2 && camera.cameraY==3200-h/2,"high edge clamp");
        }
    }
    static void maps() {
        int previous=0;
        for(long seed:new long[]{42,20261113,8675309}) {
            var layout=ForestGenerator.generate(seed);var grid=layout.grid;
            check(grid.width==200 && grid.height==200 && grid.tileSize==16,"200x200 world");
            check(layout.walkableTiles>=34000 && !layout.fallback,"85 percent with varied layout");
            var nav=new com.example.monstersurvivor.ai.NavigationGrid(grid);
            var finder=new com.example.monstersurvivor.ai.AStarPathfinder(nav);var path=new com.badlogic.gdx.utils.IntArray();
            int hash=1;
            for(int cell=0;cell<40000;cell++) {
                if(nav.walkable(cell,false))check(nav.connected(ForestGenerator.SPAWN_CELL,cell,false),"all ground connected");
                hash=hash*31+layout.terrain[cell].hashCode()+(nav.walkable(cell,false)?1:0);
            }
            check(hash!=previous,"different seeds yield different terrain/blockers");previous=hash;
            int maxExpanded=0,maxPath=0;
            for(int goal:new int[]{201,398,39601,39798,20100,20198,39700,300}) {
                check(finder.find(ForestGenerator.SPAWN_CELL,goal,false,path),"far/edge A* path");
                maxExpanded=Math.max(maxExpanded,finder.lastExpanded);maxPath=Math.max(maxPath,path.size);
                int before=ForestGenerator.SPAWN_CELL;
                for(int i=0;i<path.size;i++) {int cell=path.get(i);check(nav.walkable(cell,false) && (Math.abs(cell-before)==1 || Math.abs(cell-before)==200),"valid four-neighbor path");before=cell;}
            }
            check(grid.canOccupy(1608,1608,4,3),"safe center spawn");
            for(int goal=997;goal<40000;goal+=997)if(nav.walkable(goal,false)) {
                check(finder.find(201,goal,false,path),"long path across real clusters");
                maxExpanded=Math.max(maxExpanded,finder.lastExpanded);maxPath=Math.max(maxPath,path.size);
            }
            var enemies=new com.example.monstersurvivor.ai.EnemySystem(nav,null,GameConfig.defaults());
            var spawner=new com.example.monstersurvivor.spawn.EnemySpawner(GameConfig.defaults(),enemies,seed);
            var player=new Player(1608,1608);
            for(int frame=0;frame<180;frame++)spawner.update(.1f,0,player,1388,1504,1828,1712);
            check(enemies.livingCount()==6,"normal initial population on expanded navigation");
            for(int i=0;i<enemies.size();i++) {var e=enemies.get(i);check(grid.canOccupy(e.x,e.y,5,3,e.definition.flying),"expanded world spawn off blockers");}
            for(int frame=0;frame<60;frame++) {enemies.beginFrame();enemies.update(1f/30,player);check(enemies.searchesThisFrame<=2,"A* per-frame budget remains bounded");}
            System.out.println("MAP seed="+seed+" walkable="+layout.walkableTiles+" percent="+(layout.walkableTiles/400f)+" props="+layout.props.size+" repairs="+layout.repairs+" fallback="+layout.fallback+" hash="+hash+" longestPath="+maxPath+" maxExpanded="+maxExpanded);
        }
    }
    public static void main(String[] args) {weapons();night();zoom();maps();System.out.println("Task03B1 checks passed: "+checks);}
}
