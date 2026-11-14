import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.assets.GameAssets.State;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.spawn.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;
import java.util.*;

/** Finite per-species functional coverage; no GL readback or long-running stress workload. */
public final class EnemyRosterChecks {
    static int checks;
    static void check(boolean b,String s) {if(!b)throw new AssertionError(s);checks++;}
    static CollisionGrid open() {return new CollisionGrid(40,40,16,new byte[1600]);}
    static GameConfig config(GameMode mode) {
        var b=new GameConfig.Builder().mode(mode).population(0,60).pickups(new PickupConfig(15,20,60,6,20,1,1,0,1,2))
            .stages(new GameConfig.Stage(0,0,1000000,1,0,0,0));
        for(var d:EnemyRegistry.all())b.enemy(d,4,1,.001f);return b.build();
    }
    static void tick(GameSession s,float dt) {s.update(dt,0,0,false,32,48,464,448);}
    static void perType()throws Exception {
        Set<String> ids=new HashSet<>();StringBuilder rows=new StringBuilder("[\n");int species=0;
        for(var d:EnemyRegistry.all()) {
            check(ids.add(d.id),"unique registry id");check(!d.id.equals("lich_lord") && !d.id.contains("archmage"),"boss excluded");
            int weaponsPassed=0;
            for(var mode:GameMode.values())for(var weapon:WeaponType.values()) {
                var s=new GameSession(config(mode),open(),248,248,null,null,42);
                if(weapon!=WeaponType.SWORD)s.weapons.loadout.grant(weapon);
                var e=s.enemies.spawn(d,248+(weapon==WeaponType.SWORD?28:112),248,d.unlock);
                check(e!=null && e.hp()==4,"actual spawn/stats "+d.id);
                for(int frame=0;frame<200 && e.alive();frame++)tick(s,.1f);
                check(!e.alive(),"killed by "+weapon+" "+mode+" "+d.id);
                check(e.state()==State.DEATH && e.path.size==0,"death stops action/path "+d.id);
                check(s.kills==1 && s.pickups.deathRolls==1 && s.pickups.healthDrops()==1,"exactly one kill/drop through weapon "+weapon+" "+d.id);
                float x=e.x,y=e.y;int hp=s.player.hp();for(int n=0;n<10;n++)check(!e.damage(100),"corpse rejects repeated damage");
                for(int n=0;n<60;n++) {s.enemies.beginFrame();s.enemies.update(.1f,s.player);s.combat.contact(s.player,s.enemies);}
                check(s.enemies.size()==0 && s.kills==1 && s.pickups.deathRolls==1 && s.player.hp()==hp && e.x==x && e.y==y,"death finishes once, no dead AI/contact "+d.id);
                s.end();weaponsPassed++;
            }
            var b=new GameConfig.Builder().population(0,60).enemy(d,5,1,30).stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();
            var alive=new GameSession(b,open(),248,248,null,null,42);var e=alive.enemies.spawn(d,328,248,d.unlock);float start=e.x;
            for(int i=0;i<20;i++) {alive.enemies.beginFrame();alive.enemies.update(.05f,alive.player);}
            check(e.x<start && e.state()==State.WALK,"real chase/walk "+d.id);
            e.damage(1);e.advance(.05f);float clock=e.animationTime;e.damage(1);
            check(e.state()==State.HURT && e.animationTime==clock,"hurt cannot restart forever "+d.id);e.advance(e.duration(State.HURT));
            e.x=alive.player.x+6;e.y=alive.player.y;alive.combat.contact(alive.player,alive.enemies);
            check(alive.player.hp()==9 && e.state()==State.ATTACK,"real contact/attack "+d.id);
            alive.combat.contact(alive.player,alive.enemies);check(alive.player.hp()==9,"shared immunity "+d.id);
            e.advance(e.duration(State.ATTACK)+.01f);check(e.state()==State.IDLE,"attack ends without looping "+d.id);alive.end();
            if(species++>0)rows.append(",\n");rows.append("{\"id\":\"").append(d.id).append("\",\"weaponModeCases\":").append(weaponsPassed)
                .append(",\"chaseWalkContactAttackHurtDeath\":true,\"singleKillDrop\":true}");
        }
        rows.append("\n]\n");java.nio.file.Files.writeString(java.nio.file.Path.of("../core/build/reports/task04a/per-type-checks.json"),rows.toString());
        check(species==82,"complete current roster tested");System.out.println("All "+species+" species: actual four-weapon damage/death/drop in Normal and Demo, plus movement/contact/action states.");
    }
    static void navigation() {
        for(byte mask:new byte[]{CollisionGrid.TREE,CollisionGrid.WATER}) {
            byte[] cells=new byte[400];for(int y=0;y<20;y++)if(y<14 || y>16)cells[y*20+10]=mask;
            var grid=new CollisionGrid(20,20,16,cells);var b=new GameConfig.Builder().population(0,60);
            for(var d:EnemyRegistry.all())b.enemy(d,10,1,40);var c=b.build();var nav=new NavigationGrid(grid);var system=new EnemySystem(nav,null,c);var player=new Player(232,120);
            for(var d:EnemyRegistry.all()) {
                system.clear();var e=system.spawn(d,88,120,d.unlock);check(e!=null,"navigation fixture spawn");
                if(mask==CollisionGrid.WATER)check(grid.clearLine(88,120,232,120,d.halfWidth,d.halfHeight,d.flying)==d.flying,"only flyers cross water "+d.id);
                else check(!grid.clearLine(88,120,232,120,d.halfWidth,d.halfHeight,d.flying),"flyers still respect trees "+d.id);
                for(int frame=0;frame<300;frame++) {
                    system.beginFrame();system.update(.05f,player);
                    check(system.searchesThisFrame<=2,"shared path budget");check(grid.canOccupy(e.x,e.y,d.halfWidth,d.halfHeight,d.flying),"actual footprint never crosses blocker "+d.id);
                }
                check(Math.hypot(e.x-player.x,e.y-player.y)<20,"bounded path reaches player "+d.id);
            }
        }
    }
    static void progression() {
        var system=new EnemySystem(new NavigationGrid(open()),null,GameConfig.defaults());
        var a=new EnemySpawnProfile(42,Difficulty.EASY,40);var b=new EnemySpawnProfile(42,Difficulty.EASY,40);var other=new EnemySpawnProfile(43,Difficulty.EASY,40);
        int differences=0;Set<String> overall=new HashSet<>();
        for(float progress:new float[]{0,60,119.99f,120,239.99f,240,299.99f,300,420,539.99f,540,599}) {
            Map<String,Integer> count=new HashMap<>();
            for(int i=0;i<600;i++) {
                var x=a.pick(progress,system);var y=b.pick(progress,system);var z=other.pick(progress,system);
                check(x==y && x.unlock<=progress,"same seed reproducible and unlock valid");if(x!=z)differences++;
                check(x.unlock>=300 || !x.biome.equals("Night"),"night species not early");overall.add(x.id);count.merge(x.id,1,Integer::sum);
            }
            check(Collections.max(count.values())<300,"no species dominates empty-pool sample");
            System.out.println("POOL progress="+progress+" distinct="+count.size());
        }
        check(differences>2000,"different seeds produce distinct compositions");
        for(var d:EnemyRegistry.all())check(a.weight(d,Math.max(540,d.unlock))>0,"every registered species is naturally eligible before 600 "+d.id);
        check(a.pick(600,system)==null && a.weight(EnemyRegistry.get("tiny_slime"),600)==0,"600 natural-spawn stop contract");
        check(a.weight(EnemyRegistry.get("mc_shadow_cat"),299.9f)==0 && a.weight(EnemyRegistry.get("mc_shadow_cat"),300)>0,"night threshold");
        check(overall.size()>=78,"broad diversity in finite seeded samples");
        for(var difficulty:Difficulty.values()) {
            int cap=difficulty==Difficulty.EASY?40:60;var c=new GameConfig.Builder().difficulty(difficulty).population(cap,cap).build();
            var layout=ForestGenerator.generate(42);var s=new GameSession(c,layout.grid,1608,1608,null,null,42);
            for(int frame=0;frame<1500 && s.enemies.size()<cap;frame++)s.spawner.update(.1f,540,s.player,1388,1504,1828,1712);
            check(s.enemies.size()==cap,"fills exactly the existing resident cap "+difficulty);
            int[] counts=new int[EnemyRegistry.all().size()],families=new int[EnemyRegistry.FAMILY_COUNT];
            for(int i=0;i<s.enemies.size();i++) {
                var e=s.enemies.get(i);var d=e.definition;counts[d.index]++;families[d.familyIndex]++;
                check(layout.grid.canOccupy(e.x,e.y,d.halfWidth,d.halfHeight,d.flying),"weighted spawn on valid ground");
                check(!(e.x>=1308 && e.x<=1908 && e.y>=1424 && e.y<=1792),"off-camera spawn margin");
                check(e.x>=1196 && e.x<=2020 && e.y>=1312 && e.y<=1904,"spawn fallback stays near current camera");
            }
            for(var d:EnemyRegistry.all())check(counts[d.index]<=d.residentLimit && families[d.familyIndex]<=Math.ceil(cap*.25f),"species/family resident limits");
            s.enemies.get(0).damage(Integer.MAX_VALUE);s.spawner.update(1,540,s.player,1388,1504,1828,1712);
            check(s.enemies.size()==cap,"corpses count toward cap");s.end();
        }
        for(var mode:GameMode.values()) {
            var s=new GameSession(config(mode),open(),248,248,null,null,5);
            for(int frame=0;frame<450;frame++)tick(s,.1f);
            check(Math.abs(s.progress()-(mode==GameMode.DEMO?300:45))<.01,"shared Normal/Demo progress clock");
            check(s.night.active()==(mode==GameMode.DEMO),"night agrees with spawn clock");
        }
    }
    public static void main(String[] args)throws Exception {perType();navigation();progression();System.out.println("ENEMY ROSTER LOGIC PASSED: "+checks+" checks");}
}
