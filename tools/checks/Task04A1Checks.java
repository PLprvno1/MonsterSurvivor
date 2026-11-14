import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.spawn.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.world.*;
import java.text.Normalizer;

public final class Task04A1Checks {
    static int checks;
    static void check(boolean b,String label) {if(!b)throw new AssertionError(label);checks++;}
    static void close(float a,float b,String s) {check(Math.abs(a-b)<.002f,s+" "+a+" / "+b);}
    static CollisionGrid open() {return new CollisionGrid(80,80,16,new byte[6400]);}
    static void balance() {
        var easy=GameConfig.defaults();var hard=new GameConfig.Builder().difficulty(Difficulty.HARD).build();
        int lastE=0,lastH=0;
        for(int p=0;p<=600;p++) {
            int e=easy.targetPopulation(p),h=hard.targetPopulation(p);
            check(e<=40 && h<=60 && e>=lastE && h>=lastH,"monotonic resident caps");lastE=e;lastH=h;
            check(h>e && hard.spawnInterval(p)<easy.spawnInterval(p),"Hard higher target and faster cadence "+p);
        }
        check(easy.enemy(EnemyType.SLIME,59).hp==1 && easy.enemy(EnemyType.SLIME,119).hp==1,"Easy one-hit opening slime");
        var s=new GameSession(easy,open(),600,600,null,null,42);var slime=s.enemies.spawn(EnemyType.SLIME,628,600);
        for(int i=0;i<20 && slime.alive();i++)s.update(.05f,0,0,false,460,496,739,704);
        check(!slime.alive() && s.kills==1,"actual base Sword kills opening slime once");s.end();
        var tank=EnemyRegistry.get("mc_moss_golem");var hound=EnemyRegistry.get("mc_bone_hound");
        check(hard.enemy(tank,420).hp>easy.enemy(tank,420).hp && hard.enemy(tank,420).hp<=15,"controlled durable scaling");
        check(hard.enemy(hound,120).damage==1 && hard.enemy(hound,300).damage==2,"mid/late strong contact damage only");
        for(var d:EnemyRegistry.all()) {
            if(d.unlock==0)check(hard.enemy(d,0).damage==1,"no opening two-damage spam");
            check(hard.enemy(d,540).hp>0 && hard.enemy(d,540).damage<=2,"finite HP and contact damage");
        }
        for(var c:new GameConfig[]{easy,hard}) {
            var system=new EnemySystem(new NavigationGrid(open()),null,c);var p=new Player(600,600);var e=system.spawn(EnemyType.SLIME,680,600);
            system.beginFrame();system.update(.1f,p);close(680-e.x,28*c.enemySpeedMultiplier*.1f,"effective movement multiplier");
            int hp=e.hp();system.clear();check(e.hp()==hp,"no live stat mutation");
        }
        var residents=new EnemySystem(new NavigationGrid(open()),null,hard);
        var ep=new EnemySpawnProfile(42,easy);var hp=new EnemySpawnProfile(42,hard);var repeat=new EnemySpawnProfile(42,hard);
        for(int progress:new int[]{0,60,120,210,300,420,540}) {
            double es=0,et=0,hs=0,ht=0;
            for(var d:EnemyRegistry.all()) {float ew=ep.weight(d,progress),hw=hp.weight(d,progress);et+=ew;ht+=hw;if(GameConfig.strong(d)) {es+=ew;hs+=hw;}check(d.unlock<=progress || (ew==0 && hw==0),"unlock preserved");}
            if(progress>=120)check(hs/ht>es/et,"Hard stronger weighted mix "+progress);
            for(int i=0;i<30;i++)check(hp.pick(progress,residents)==repeat.pick(progress,residents),"seed reproducible");
        }
        for(var difficulty:Difficulty.values()) {
            var c=new GameConfig.Builder().difficulty(difficulty).build();var layout=ForestGenerator.generate(42);
            var sys=new EnemySystem(new NavigationGrid(layout.grid),null,c);var p=new Player(1608,1608);
            var spawner=new EnemySpawner(c,sys,42);
            for(int i=0;i<1600;i++)spawner.update(.1f,599,p,1469,1504,1747,1712);
            check(sys.size()==c.maxEnemies,"actual spawn fills unchanged cap "+difficulty);check(sys.size()<61,"absolute cap");
            for(int i=0;i<sys.size();i++)check(layout.grid.canOccupy(sys.get(i).x,sys.get(i).y,sys.get(i).definition.halfWidth,sys.get(i).definition.halfHeight,sys.get(i).definition.flying),"valid spawn");
            int old=sys.get(0).hp();sys.get(0).damage(Integer.MAX_VALUE);check(sys.get(0).hp()==0 && old>0,"scaled enemy death HP");
        }
        for(var difficulty:Difficulty.values()) {
            var a=new GameConfig.Builder().difficulty(difficulty).population(0,60).stages(new GameConfig.Stage(0,0,99999,1,0,0,0)).build();
            var b=new GameConfig.Builder().difficulty(difficulty).mode(GameMode.DEMO).population(0,60).stages(new GameConfig.Stage(0,0,99999,1,0,0,0)).build();
            var x=new GameSession(a,open(),600,600,null,null,42);var y=new GameSession(b,open(),600,600,null,null,42);
            for(int i=0;i<20;i++) {x.update(.05f,1,0,false,460,496,739,704);y.update(.05f,1,0,false,460,496,739,704);}
            close(x.player.x,y.player.x,"Demo physical movement unchanged");close(y.progress(),x.progress()*GameMode.DEMO.progressScale,"only progress accelerated");x.end();y.end();
        }
    }
    static final class Surface implements GameInput.Surface {
        public float width(){return 800;} public float height(){return 480;}
        public void unproject(int x,int y,Vector2 v){v.set(x,480-y);}
        public boolean inPauseButton(float x,float y){return x>680 && y>420;}
        public boolean inResumeButton(float x,float y){return x>300 && x<500 && y>176 && y<240;}
        public boolean inHomeButton(float x,float y){return x>300 && x<500 && y>100 && y<164;}
        public boolean inAnimationButton(float x,float y){return x>600 && y<60;}
        public boolean inDirectionButton(float x,float y){return x>600 && y>60 && y<110;}
        public int weaponOfferAt(float x,float y,int count){return x>=90 && x<=710 && y>=294 && y<=370?0:-1;}
        public boolean weaponHomeAt(float x,float y){return x>600 && y<90;}
    }
    static final class Commands implements GameInput.Commands {
        boolean paused,modal,stopped;int selected;
        public boolean isPaused(){return paused;}public void togglePause(){paused=!paused;}
        public void cycleAnimation(){}public void cycleDirection(){}
        public boolean gameplayStopped(){return stopped;}public boolean weaponSelection(){return modal;}
        public long weaponToken(){return 1;}public int weaponOfferCount(){return 3;}
        public boolean chooseWeapon(int index,long token){selected++;modal=false;return index==0 && token==1;}
    }
    static void down(GameInput in,int x,int y,int id){in.touchDown(x,480-y,id,0);}
    static void drag(GameInput in,int x,int y,int id){in.touchDragged(x,480-y,id);in.sampleAxes();}
    static void joystick() {
        var cmd=new Commands();var in=new GameInput(new Surface(),cmd);
        for(int[] at:new int[][]{{4,40},{150,200},{350,380}}) {
            down(in,at[0],at[1],0);in.sampleAxes();check(in.joystickActive(),"wide activation");close(in.centerX(),at[0],"floating center");close(in.axisX(),0,"tap no movement");close(in.axisY(),0,"tap no movement");
            for(int[] delta:new int[][]{{100,0},{-100,0},{0,100},{0,-100},{100,100},{100,-100},{-100,100},{-100,-100}}) {
                drag(in,at[0]+delta[0],at[1]+delta[1],0);close((float)Math.hypot(in.axisX(),in.axisY()),1,"360 normalized magnitude");
                check(Math.signum(in.axisX())==Math.signum(delta[0]) && Math.signum(in.axisY())==Math.signum(delta[1]),"360 direction");
            }
            in.touchUp(0,0,0,0);close(in.axisX(),0,"release immediate");check(!in.joystickActive(),"release owner");
        }
        check(!in.inJoystickRegion(353,200) && !in.inJoystickRegion(100,390) && !in.inJoystickRegion(100,10),"right/HUD/footer excluded");
        down(in,200,200,2);drag(in,203,203,2);close(in.axisX(),0,"deadzone");down(in,250,250,3);check(in.activePointer()==2,"second pointer cannot steal");
        in.touchUp(0,0,3,0);check(in.activePointer()==2,"secondary release cannot stop owner");drag(in,790,250,2);check(in.axisX()>.9f,"owned drag crosses right half");
        in.touchCancelled(0,0,2,0);close(in.axisX(),0,"cancel immediate");check(!in.joystickActive(),"cancel clears owner");
        in.keyDown(Input.Keys.W);in.keyDown(Input.Keys.D);in.sampleAxes();close(in.axisX(),.7071f,"desktop diagonal");close(in.axisY(),.7071f,"desktop diagonal");in.reset();
        down(in,200,200,0);drag(in,300,200,0);cmd.paused=true;in.sampleAxes();check(in.activePointer()==-1 && in.axisX()==0,"pause resets held input");cmd.paused=false;in.sampleAxes();check(in.axisX()==0,"resume no stale movement");
        down(in,200,200,0);drag(in,300,200,0);cmd.modal=true;in.sampleAxes();check(in.activePointer()==-1,"modal locks owner");
        down(in,200,320,1);check(!in.joystickActive() && in.selectedOffer()==0,"modal card wins inside joystick region");in.touchUp(200,160,1,0);check(cmd.selected==1,"modal commits one choice");
        down(in,200,200,0);drag(in,300,200,0);cmd.stopped=true;in.sampleAxes();check(in.axisX()==0 && !in.joystickActive(),"DYING/reset stopped gameplay");cmd.stopped=false;
        down(in,200,200,0);drag(in,300,200,0);in.keyDown(Input.Keys.BACK);check(cmd.paused && !in.joystickActive() && in.axisX()==0,"Back clears immediately");
    }
    static void localization() {
        check(GameLanguage.parse(null)==GameLanguage.VI && GameLanguage.parse("bad")==GameLanguage.VI && GameLanguage.parse(" EN ")==GameLanguage.EN,"language fallback");
        var vi=GameConfig.fromIntent("HARD","DEMO","vi");var en=GameConfig.fromIntent("HARD","DEMO","en");
        check(vi.difficulty==en.difficulty && vi.mode==en.mode && en.language==GameLanguage.EN,"bridge isolates language");
        for(var k:GameStrings.Key.values())for(var language:GameLanguage.values()) {
            String value=GameStrings.of(language).get(k);check(!value.isBlank(),"translation exists "+k+language);check(Normalizer.isNormalized(value,Normalizer.Form.NFC),"NFC translation");
        }
        var a=new GameSession(vi,open(),600,600,null,null,42);var b=new GameSession(en,open(),600,600,null,null,42);
        a.selection.open(10,a.weapons.loadout,a.player);b.selection.open(10,b.weapons.loadout,b.player);
        for(int i=0;i<3;i++) {var x=a.selection.get(i);var y=b.selection.get(i);check(x.weapon==y.weapon && x.kind==y.kind && x.fromLevel==y.fromLevel,"language leaves offers identical");check(!x.title.equals(y.title),"localized offer name");}
        for(var type:WeaponType.values()) {check(vi.weapons.level(type,3).damage(4)==en.weapons.level(type,3).damage(4),"same damage with either locale");}
        a.end();b.end();
        var x=new GameSession(vi,open(),600,600,null,null,42);var y=new GameSession(en,open(),600,600,null,null,42);
        x.pickups.spawn(PickupType.DAMAGE_BOX,600,600,1);y.pickups.spawn(PickupType.DAMAGE_BOX,600,600,1);
        x.update(.05f,0,0,false,460,496,739,704);y.update(.05f,0,0,false,460,496,739,704);
        check(x.player.stats.damage()==y.player.stats.damage() && x.player.stats.damage()>1,"Damage Box useful/unchanged reward");
        check(!x.pickups.notice.equals(y.pickups.notice),"localized reward feedback");x.end();y.end();
    }
    public static void main(String[] args) {balance();joystick();localization();System.out.println("TASK 04A.1 LOGIC PASSED: "+checks+" checks");}
}
