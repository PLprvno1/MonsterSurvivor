import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.audio.*;
import com.example.monstersurvivor.boss.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.input.GameInput;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.ui.SkillHud;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;
import com.example.monstersurvivor.localization.*;
import java.nio.file.*;
import java.util.*;
import static com.example.monstersurvivor.skill.SkillType.*;

/** Deterministic active-skill contracts. Simulation/Android input are verified separately. */
public final class SkillChecks {
    static int checks;
    static void check(boolean b,String label){if(!b)throw new AssertionError(label);checks++;}
    static void close(float a,float b,String label){check(Math.abs(a-b)<.003f,label+" actual="+a+" expected="+b);}
    static CollisionGrid open(){return new CollisionGrid(80,80,16,new byte[6400]);}
    static GameConfig config(GameMode mode){return new GameConfig.Builder().mode(mode).debug(true).playerHp(100000).population(0,40).enemy(EnemyType.SLIME,100,1,1).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();}
    static GameSession session(GameMode mode){return new GameSession(config(mode),open(),600,600,null,null,42);}
    static void tick(GameSession s,int count,float dt){for(int i=0;i<count;i++)s.update(dt,0,0,false,460,496,740,704);}
    static final class Events implements AudioSink {
        EnumMap<AudioEvent,Integer> events=new EnumMap<>(AudioEvent.class);
        public void play(AudioEvent event){events.merge(event,1,Integer::sum);}
        int count(AudioEvent event){return events.getOrDefault(event,0);}
    }
    static void dash() {
        for(float[] axis:new float[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}}) {
            var grid=open();var p=new Player(600,600);var skills=new ActiveSkillSystem(new SkillConfig());p.skillInvulnerability(skills::dashInvulnerable);
            check(skills.view(DASH).phase()==ActiveSkillSystem.Phase.READY,"Dash initially ready");check(skills.activate(DASH,p,axis[0],axis[1]),"Dash accepted");
            long token=skills.view(DASH).activationId();check(token>0 && !skills.activate(DASH,p,0,1),"active spam rejected");
            for(int i=0;i<4;i++)skills.advance(.05f,-axis[0],-axis[1],p,grid);
            float length=(float)Math.hypot(axis[0],axis[1]);close(p.x,600+75*axis[0]/length,"snapshotted Dash x");close(p.y,600+75*axis[1]/length,"snapshotted Dash y");
            check(p.stats.speed()==10 && p.stats.movementSpeed()==80,"Dash never changes stats Speed");check(!skills.dashInvulnerable() && skills.view(DASH).phase()==ActiveSkillSystem.Phase.COOLDOWN,"active ends");
            close(skills.view(DASH).cooldownRemaining(),4.8f,"cooldown starts at activation");for(int i=0;i<48;i++)skills.advance(.1f,0,0,p,grid);
            check(skills.view(DASH).phase()==ActiveSkillSystem.Phase.READY && skills.activate(DASH,p,axis[0],axis[1]),"ready after five gameplay seconds");check(skills.view(DASH).activationId()>token,"activation IDs unique");
        }
        var p=new Player(600,600);var skills=new ActiveSkillSystem(new SkillConfig());p.update(.1f,.6f,.8f,open());check(skills.activate(DASH,p,0,0),"fallback accepted");close(skills.dashX(),.6f,"last actual move x");close(skills.dashY(),.8f,"last actual move y");
        p=new Player(600,600);p.direction=com.example.monstersurvivor.assets.GameAssets.Direction.LEFT;skills=new ActiveSkillSystem(new SkillConfig());skills.activate(DASH,p,0,0);close(skills.dashX(),-1,"initial facing fallback");
        p=new Player(600,600);p.startAttack(1,0);var facing=p.attackDirection;skills=new ActiveSkillSystem(new SkillConfig());skills.activate(DASH,p,0,1);skills.advance(.1f,0,1,p,open());check(p.attackDirection==facing && p.windingUp() && p.swingX()==1,"Dash preserves pending Sword aim/action");
        for(int mask:new int[]{CollisionGrid.TREE,CollisionGrid.ROCK,CollisionGrid.WATER,CollisionGrid.BOUNDARY})for(float dt:new float[]{.02f,.05f,.1f,.5f}) {
            byte[] walls=new byte[6400];for(int y=0;y<80;y++)walls[y*80+40]=(byte)mask;
            var grid=new CollisionGrid(80,80,16,walls);p=new Player(620,600);skills=new ActiveSkillSystem(new SkillConfig());skills.activate(DASH,p,1,0);
            for(float t=0;t<.201f;t+=dt)skills.advance(dt,0,0,p,grid);
            check(p.x<636.01f && grid.canOccupy(p.x,p.y,Player.HALF_WIDTH,Player.HALF_HEIGHT),"wall swept at frame "+dt+" mask="+mask);
            float old=p.y;p.update(.1f,0,1,grid);check(p.y>old,"wall never sticks after dash");
        }
        for(float[] at:new float[][]{{5,600,-1,0},{1275,600,1,0},{600,4,0,-1},{600,1276,0,1}}) {
            p=new Player(at[0],at[1]);skills=new ActiveSkillSystem(new SkillConfig());skills.activate(DASH,p,at[2],at[3]);skills.advance(.5f,0,0,p,open());
            check(open().canOccupy(p.x,p.y,4,3),"Dash remains inside world bounds");
        }
        var invalid=new ActiveSkillSystem(new SkillConfig());p=new Player(600,600);check(!invalid.activate(DASH,p,Float.NaN,1) && invalid.view(DASH).activationId()==0 && invalid.view(DASH).cooldownRemaining()==0,"invalid input leaves cooldown/token untouched");
    }
    static void iframe()throws Exception {
        var s=session(GameMode.NORMAL);Events e=new Events();s.audio(e);s.activateSkill(DASH,1,0,false);s.skills.advance(.08f,0,0,s.player,open());
        check(s.player.dashInvulnerable() && s.player.immunity()==0,"Dash immunity distinct from post-damage immunity");int hp=s.player.hp();
        var contact=s.enemies.spawn(EnemyType.SLIME,s.player.x,s.player.y);s.combat.contact(s.player,s.enemies);check(s.player.hp()==hp,"Dash blocks actual contact");
        s.bosses.prepare(.05f,600,s.player,460,496,740,704);
        for(BossInstance b:new BossInstance[]{s.bosses.mini(),s.bosses.lich()}) {
            for(Object[] setting:new Object[][]{{"cast",b.finalBoss()?BossInstance.Cast.AREA:BossInstance.Cast.SLAM},{"castRemaining",0f},{"aimX",s.player.x},{"aimY",s.player.y},{"radius",48f}}) {
                var f=BossInstance.class.getDeclaredField((String)setting[0]);f.setAccessible(true);f.set(b,setting[1]);
            }
        }
        s.bosses.fight(0,600,s.player);check(s.player.hp()==hp && s.bosses.mini().areaCasts==1 && s.bosses.lich().areaCasts==1,"Dash blocks actual Mini slam and Lich AoE");
        var shots=new BossProjectiles(1);shots.fire(1,s.player.x-4,s.player.y,1,0,100,3);shots.update(.1f,s.player,open());check(s.player.hp()==hp && shots.size()==0,"Dash blocks actual Lich projectile and consumes it");
        check(e.count(AudioEvent.PLAYER_HURT)==0 && s.player.immunity()==0,"blocked damage emits no Hurt or 0.7s immunity");
        s.skills.advance(.1f,0,0,s.player,open());check(!s.player.dashInvulnerable() && s.player.damage(1),"iframe expires at 0.17 while Dash continues");
        close(s.player.immunity(),.7f,"real damage gives original 0.7s immunity");check(e.count(AudioEvent.PLAYER_HURT)==1,"real HP loss emits one Hurt");
        s.skills.advance(.03f,0,0,s.player,open());check(!s.player.damage(1) && s.player.immunity()>.66f,"Dash ending does not overwrite post-hit immunity");
        for(int i=0;i<100;i++)check(!s.activateSkill(DASH,1,0,false),"spam cannot extend iframe");s.end();check(!s.player.dashInvulnerable(),"end clears skill protection");
    }
    static void ultimate() {
        var config=config(GameMode.NORMAL);var grid=open();var p=new Player(600,600,null,config);var enemies=new EnemySystem(new NavigationGrid(grid),null,config);var effects=new ProjectileSystem(config.weapons);
        var skills=new ActiveSkillSystem(config.skills);Events audio=new Events();skills.audio(audio);
        check(skills.view(ULTIMATE).phase()==ActiveSkillSystem.Phase.COOLDOWN && skills.view(ULTIMATE).cooldownRemaining()==15,"Ultimate initial 15s cooldown");
        check(!skills.activate(ULTIMATE,p,0,0) && skills.view(ULTIMATE).activationId()==0,"initial cast rejected without state change");
        for(int i=0;i<150;i++)skills.advance(.1f,0,0,p,grid);check(skills.view(ULTIMATE).phase()==ActiveSkillSystem.Phase.READY,"Ultimate ready after 15 real gameplay seconds");
        var a=enemies.spawn(EnemyType.SLIME,660,600);var b=enemies.spawn(EnemyType.SLIME,700,600);var outside=enemies.spawn(EnemyType.SLIME,706,600);
        a.path.add(4);a.pathIndex=1;check(skills.activate(ULTIMATE,p,0,0),"Ultimate cast accepted");p.stats.addDamage(100);
        for(int i=0;i<3;i++){skills.advance(.1f,0,0,p,grid);skills.resolve(p,enemies,grid,effects);}check(a.hp()==100 && skills.ultimateImpacts==0,"no damage before cast duration");
        skills.advance(.05f,0,0,p,grid);skills.resolve(p,enemies,grid,effects);check(a.hp()==96 && b.hp()==96 && outside.hp()==100,"snapshot base damage, radius105 and single release");
        close(a.x,685,"ordinary knockback25");check(a.path.size==0 && a.pathIndex==0 && a.lineTimer==0 && a.pathTimer==0,"knockback invalidates stale path safely");check(a.stats.speed==1,"no permanent speed change");
        for(int i=0;i<20;i++){skills.advance(.1f,0,0,p,grid);skills.resolve(p,enemies,grid,effects);}check(a.hp()==96 && skills.ultimateImpacts==1 && skills.ultimateHits==2,"no per-frame damage or repeated impact");
        check(audio.count(AudioEvent.SKILL_ULTIMATE_CAST)==1 && audio.count(AudioEvent.SKILL_ULTIMATE_IMPACT)==1,"one cast/impact audio hook");check(effects.effectCount()<=config.skills.ultimate.hitFxCap,"bounded impact FX");
        byte[] wall=new byte[6400];for(int y=0;y<80;y++)wall[y*80+40]=CollisionGrid.ROCK;grid=new CollisionGrid(80,80,16,wall);p=new Player(600,600,null,config);enemies=new EnemySystem(new NavigationGrid(grid),null,config);a=enemies.spawn(EnemyType.SLIME,665,600);
        var fast=new SkillConfig(config.skills.dash,new SkillConfig.Ultimate(30,0,.35f,105,2.5f,4,25,.35f,12));skills=new ActiveSkillSystem(fast);skills.activate(ULTIMATE,p,0,0);skills.advance(.35f,0,0,p,grid);skills.resolve(p,enemies,grid,null);check(a.hp()==100 && a.x==665,"thick wall blocks Ultimate LOS");
        p=new Player(600,600,null,config);grid=open();enemies=new EnemySystem(new NavigationGrid(grid),null,config);
        var tank=enemies.spawn(EnemyRegistry.get("rock_golem"),665,600,600);skills=new ActiveSkillSystem(fast);skills.activate(ULTIMATE,p,0,0);skills.advance(.35f,0,0,p,grid);skills.resolve(p,enemies,grid,null);close(tank.x,673.75f,"tank knockback reduced to 35 percent");
        grid=new CollisionGrid(80,80,16,wall);enemies=new EnemySystem(new NavigationGrid(grid),null,config);a=enemies.spawn(EnemyType.SLIME,630,600);
        check(enemies.knockback(a,1,0,100),"near-wall knockback can move");check(a.x<635.01f && grid.canOccupy(a.x,a.y,a.definition.halfWidth,a.definition.halfHeight),"knockback cannot tunnel across wall");
        var capped=new SkillConfig.Ultimate(30,15,.35f,105,2.5f,4,25,.35f,12);check(capped.damage(Integer.MAX_VALUE)==Integer.MAX_VALUE,"damage arithmetic saturates safely");
        for(var difficulty:Difficulty.values())for(var mode:GameMode.values()) {
            var c=new GameConfig.Builder().difficulty(difficulty).mode(mode).skills(fast).playerHp(100000).population(0,40).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();
            var s=new GameSession(c,open(),600,600,null,null,42);s.bosses.prepare(.05f,600,s.player,460,496,740,704);s.player.stats.addDamage(100000);
            var boss=s.bosses.lich().actor;boss.x=670;boss.y=600;int hp=boss.hp();s.skills.activate(ULTIMATE,s.player,0,0);s.skills.advance(.35f,0,0,s.player,open());s.skills.resolve(s.player,s.enemies,open(),null);
            check(hp-boss.hp()==(int)Math.ceil(hp*.2f) && boss.alive() && boss.x==670,"Ultimate respects Boss burst guard and no knockback "+difficulty+mode);s.end();
        }
    }
    static void lifecycle() {
        var normal=session(GameMode.NORMAL);var demo=session(GameMode.DEMO);Events audio=new Events();normal.audio(audio);
        normal.activateSkill(DASH,1,0,false);demo.activateSkill(DASH,1,0,false);tick(normal,10,.1f);tick(demo,10,.1f);
        close(normal.skills.view(DASH).cooldownRemaining(),demo.skills.view(DASH).cooldownRemaining(),"Demo cooldown not progress scaled");close(demo.progress(),normal.progress()*GameMode.DEMO.progressScale,"Demo still scales only progress");
        float cd=normal.skills.view(DASH).cooldownRemaining(),ult=normal.skills.view(ULTIMATE).cooldownRemaining(),time=normal.elapsed();for(int i=0;i<20;i++)normal.update(.1f,0,0,true,460,496,740,704);
        close(normal.elapsed(),time,"pause freezes session");close(normal.skills.view(DASH).cooldownRemaining(),cd,"pause freezes Dash");close(normal.skills.view(ULTIMATE).cooldownRemaining(),ult,"pause freezes Ultimate");check(!normal.activateSkill(ULTIMATE,0,0,true),"paused activation rejected");
        normal.pickups.spawn(PickupType.WEAPON_BOX,normal.player.x,normal.player.y,1);tick(normal,1,.05f);check(normal.choosingWeapon(),"real modal");cd=normal.skills.view(DASH).cooldownRemaining();ult=normal.skills.view(ULTIMATE).cooldownRemaining();tick(normal,30,.1f);
        check(!normal.activateSkill(DASH,1,0,false) && !normal.activateSkill(ULTIMATE,0,0,false),"modal rejects both skills");close(normal.skills.view(DASH).cooldownRemaining(),cd,"modal freezes Dash");close(normal.skills.view(ULTIMATE).cooldownRemaining(),ult,"modal freezes Ultimate");
        check(normal.chooseWeapon(0,normal.selection.token()),"original weapon commit preserved");check(normal.weapons.loadout.size()==2,"skills consume no Weapon Slots");tick(normal,150,.1f);
        check(normal.activateSkill(ULTIMATE,0,0,false),"cast ready");normal.pickups.spawn(PickupType.WEAPON_BOX,normal.player.x,normal.player.y,1);tick(normal,1,.05f);float cast=normal.skills.view(ULTIMATE).activeRemaining();tick(normal,20,.1f);
        close(normal.skills.view(ULTIMATE).activeRemaining(),cast,"modal freezes active cast");check(normal.skills.ultimateImpacts==0,"no damage while modal");normal.chooseWeapon(0,normal.selection.token());tick(normal,8,.05f);check(normal.skills.ultimateImpacts==1,"cast continues once after modal");
        normal.end();check(!normal.skills.ultimateVisible() && !normal.activateSkill(DASH,1,0,false),"Home cancels pending skill/VFX");var fresh=session(GameMode.NORMAL);check(fresh.skills.view(DASH).phase()==ActiveSkillSystem.Phase.READY && fresh.skills.view(ULTIMATE).cooldownRemaining()==15 && fresh.skills.ultimateImpacts==0,"Retry new independent skill state");
        fresh.player.damage(Integer.MAX_VALUE);tick(fresh,1,.05f);float frozen=fresh.skills.view(ULTIMATE).cooldownRemaining();tick(fresh,40,.05f);
        check(fresh.phase()==GameSession.Phase.GAME_OVER && !fresh.activateSkill(DASH,1,0,false),"DYING/GameOver reject skills");close(fresh.skills.view(ULTIMATE).cooldownRemaining(),frozen,"terminal cooldowns frozen");normal.end();demo.end();fresh.end();
        var fast=new SkillConfig(new SkillConfig.Dash(5,.2f,75,.17f),new SkillConfig.Ultimate(30,0,.35f,105,2.5f,4,25,.35f,12));var c=new GameConfig.Builder().skills(fast).debug(true).playerHp(100000).population(0,40).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();
        var victory=new GameSession(c,open(),600,600,null,null,42);victory.diagnosticProgress(600);victory.bosses.prepare(.05f,600,victory.player,460,496,740,704);var lich=victory.bosses.lich().actor;lich.x=675;lich.y=600;
        while(lich.hp()>1){lich.advance(.21f);lich.damage(lich.hp()-1);}lich.advance(.21f);int kills=victory.kills,rolls=victory.pickups.deathRolls;
        victory.activateSkill(ULTIMATE,0,0,false);tick(victory,8,.05f);check(victory.bosses.finalDefeated() && victory.kills==kills+1 && victory.pickups.deathRolls==rolls+1,"Ultimate Boss death uses one kill/drop hook");
        check(victory.phase()==GameSession.Phase.WINNING && !victory.activateSkill(DASH,1,0,false),"WINNING cancels/rejects skills");tick(victory,40,.05f);check(victory.victory() && !victory.activateSkill(ULTIMATE,0,0,false),"original Victory condition preserved");victory.end();
    }
    static class Surface implements GameInput.Surface {
        public float width(){return 800;}public float height(){return 480;}public void unproject(int x,int y,Vector2 out){out.set(x,480-y);}
        public boolean inPauseButton(float x,float y){return x>700 && y>420;}public boolean inResumeButton(float x,float y){return false;}public boolean inHomeButton(float x,float y){return false;}
        public boolean inAnimationButton(float x,float y){return false;}public boolean inDirectionButton(float x,float y){return false;}
        public int weaponOfferAt(float x,float y,int n){return 0;}public boolean weaponHomeAt(float x,float y){return false;}
        public SkillType skillAt(float x,float y){return SkillHud.at(x,y,800,480,0,0);}
    }
    static final class Commands implements GameInput.Commands {
        boolean paused,modal,stopped;int dash,ult;float x,y;
        public boolean isPaused(){return paused;}public void togglePause(){paused=!paused;}public void cycleAnimation(){}public void cycleDirection(){}
        public boolean gameplayStopped(){return stopped;}public boolean weaponSelection(){return modal;}
        public boolean activateSkill(SkillType t,float x,float y){if(t==DASH)dash++;else ult++;this.x=x;this.y=y;return true;}
        public long weaponToken(){return 1;}public int weaponOfferCount(){return 3;}
    }
    static void down(GameInput in,int x,int y,int id){in.touchDown(x,480-y,id,0);}
    static void input() {
        var command=new Commands();var in=new GameInput(new Surface(),command);down(in,100,150,0);in.touchDragged(166,480-216,0);in.sampleAxes();float x=in.axisX(),y=in.axisY();
        down(in,640,162,1);check(command.dash==1 && in.activePointer()==0,"two pointers activate Dash without stealing joystick");close(command.x,x,"Dash uses current joystick x");close(command.y,y,"Dash uses current joystick y");
        for(int i=0;i<100;i++){down(in,640,162,1);in.touchDragged(100,100,1);}check(command.dash==1,"holding/duplicate touch-down cannot spam when ready");
        in.touchUp(640,318,1,0);in.sampleAxes();check(in.activePointer()==0 && in.axisX()==x,"skill release preserves joystick");
        down(in,732,180,1);check(command.ult==1 && in.activePointer()==0,"Ultimate shares no joystick pointer");in.touchCancelled(0,0,1,0);check(in.activePointer()==0,"skill cancel preserves left finger");
        int count=command.dash+command.ult;in.touchDragged(732,300,0);check(command.dash+command.ult==count,"joystick drag across right buttons never casts");in.touchCancelled(0,0,0,0);check(!in.joystickActive(),"joystick cancel releases owner");
        in.keyDown(Input.Keys.W);in.keyDown(Input.Keys.SHIFT_LEFT);for(int i=0;i<100;i++)in.keyDown(Input.Keys.SHIFT_LEFT);check(command.dash==2 && in.axisY()==1,"Shift one activation and WASD preserved");in.keyUp(Input.Keys.SHIFT_LEFT);in.keyDown(Input.Keys.SHIFT_LEFT);check(command.dash==3,"fresh Shift press works");
        in.keyDown(Input.Keys.SPACE);for(int i=0;i<100;i++)in.keyDown(Input.Keys.SPACE);check(command.ult==2,"Space key-repeat ignored");in.keyUp(Input.Keys.SPACE);in.keyUp(Input.Keys.SHIFT_LEFT);
        in.keyDown(Input.Keys.P);down(in,640,162,2);in.keyDown(Input.Keys.SPACE);check(command.dash==3 && command.ult==2 && !in.joystickActive(),"pause rejects input and clears joystick");in.keyUp(Input.Keys.SPACE);command.paused=false;in.reset();
        command.modal=true;down(in,640,162,2);in.touchUp(640,318,2,0);check(command.dash==3,"modal has input priority over skill footprint");command.modal=false;command.stopped=true;down(in,732,180,3);check(command.ult==2,"terminal input rejected");
        for(float width:new float[]{800,1013.33f,1200})for(SkillType type:SkillType.values()) {
            float cx=SkillHud.centerX(type,width,35),cy=SkillHud.centerY(type,480,10),r=SkillHud.radius(type);
            check(cx-r>width*.44f && cx+r<=width-35 && cy-r>112 && cy+r<422,"skill layout avoids joystick/slots/debug/Pause/safe inset");check(SkillHud.at(cx,cy,width,480,35,10)==type,"same render/hit geometry");
        }
    }
    static void localization()throws Exception {
        String fnt=Files.readString(Path.of("game/fonts/game-ui.fnt"));Set<Integer> glyphs=new HashSet<>();var matcher=java.util.regex.Pattern.compile("char id=(\\d+)").matcher(fnt);while(matcher.find())glyphs.add(Integer.parseInt(matcher.group(1)));
        for(var lang:GameLanguage.values())for(var key:GameStrings.Key.values())if(key.name().startsWith("SKILL_") || key==GameStrings.Key.CONTROLS || key==GameStrings.Key.DEBUG_CONTROLS) {
            String value=GameStrings.of(lang).get(key);check(!value.isBlank() && java.text.Normalizer.isNormalized(value,java.text.Normalizer.Form.NFC),"localized NFC skill text");for(char c:value.toCharArray())check(glyphs.contains((int)c),"font contains skill glyph "+c);
        }
    }
    public static void main(String[] args)throws Exception {dash();iframe();ultimate();lifecycle();input();localization();System.out.println("SkillChecks PASS "+checks+" assertions");}
}
