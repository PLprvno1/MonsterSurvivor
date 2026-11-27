import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.ui.*;
import java.lang.reflect.*;

/** UI03 actual GL/font bounds and real input transactions. Explicit test configs are not normal-run evidence. */
public final class HudPolishSmoke extends ApplicationAdapter {
    static boolean failed;int checks,maxDraws;GameScreen screen;BitmapFont font;SpriteBatch batch;GLProfiler profiler;
    void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
    void tick(int n){for(int i=0;i<n;i++)screen.render(.05f);}
    Object field(Object o,String key)throws Exception {var f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}
    GlyphLayout layout(Object o,String key)throws Exception{return (GlyphLayout)field(o,key);}
    void create(GameLanguage lang,boolean diagnostic){
        var b=new GameConfig.Builder().language(lang).population(0,40).stages(new GameConfig.Stage(0,0,100000,1000,0,0,0));
        if(diagnostic)b.debug(true).playerHp(100000).fixedSeed(42);
        screen=new GameScreen(b.build(),()->{});screen.show();screen.resize(1140,540);screen.render(0);
    }
    void close(){screen.home();screen.dispose();screen=null;}
    Vector2 point(float x,float y){var v=new Vector2(x,y);screen.getHud().project(v);v.y=540-v.y;return v;}
    void down(float x,float y,int id){var v=point(x,y);screen.getControls().touchDown(Math.round(v.x),Math.round(v.y),id,0);}
    void up(float x,float y,int id){var v=point(x,y);screen.getControls().touchUp(Math.round(v.x),Math.round(v.y),id,0);}
    void tap(float x,float y,int id){down(x,y,id);up(x,y,id);}
    void capture(String name)throws Exception{
        com.badlogic.gdx.utils.ScreenUtils.clear(.065f,.09f,.125f,1);var s=screen.getSession();var p=s.player;
        profiler.reset();screen.getHud().render(batch,screen.getControls(),screen.isPaused(),s.elapsed(),.05f,p.state(),p.direction,p,s.kills,s.config.mode,screen.gameOver(),s.pickups,screen.getCamera(),s);
        maxDraws=Math.max(maxDraws,profiler.getDrawCalls());check(profiler.getDrawCalls()<=12,"bounded HUD draw calls "+name);
        var image=Pixmap.createFromFrameBuffer(0,0,1140,540);
        if(screen.getControls().selectedOffer()>=0){
            var at=point(screen.getHud().width()/2+220,WeaponHud.cardY(screen.getHud().height(),screen.getControls().selectedOffer())+38);
            int rgba=image.getPixel(Math.round(at.x),540-Math.round(at.y));
            check((rgba>>>24)<120 && ((rgba>>>16)&255)<120 && ((rgba>>>8)&255)<120,"highlight keeps a dark readable card interior");
        }
        PixmapIO.writePNG(Gdx.files.local("../core/build/reports/ui03/"+name+".png"),image,-1,true);image.dispose();
        check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"GL clean "+name);
    }
    void insetLayout()throws Exception{
        Graphics original=Gdx.graphics;
        Gdx.graphics=(Graphics)Proxy.newProxyInstance(Graphics.class.getClassLoader(),new Class<?>[]{Graphics.class},(p,m,a)->switch(m.getName()){
            case "getSafeInsetLeft"->24;case "getSafeInsetRight"->80;case "getSafeInsetTop"->16;case "getSafeInsetBottom"->36;
            default->m.invoke(original,a);
        });
        try{screen.resize(1140,540);}finally{Gdx.graphics=original;}
        var h=screen.getHud();check(!h.inPauseButton(h.width()-20,h.height()-30),"pause excludes simulated right cutout");
        check(h.inPauseButton(h.width()-80/1.125f-64,h.height()-16/1.125f-35),"pause drawn and hit-tested inside safe bounds");
        capture("hud-insets-"+screen.getSession().config.language.code);screen.resize(1140,540);
        for(float width:new float[]{800,1013.3333f,1137.7778f})for(float right:new float[]{0,36,72}){
            float dashX=SkillHud.centerX(SkillType.DASH,width,right),ultX=SkillHud.centerX(SkillType.ULTIMATE,width,right);
            check(dashX-SkillHud.radius(SkillType.DASH)>width*.44f,"Dash outside joystick activation zone");
            check(ultX-dashX>SkillHud.radius(SkillType.DASH)+SkillHud.radius(SkillType.ULTIMATE),"skill circles never overlap");
            for(var type:SkillType.values())check(SkillHud.at(SkillHud.centerX(type,width,right),SkillHud.centerY(type,480,32),width,480,right,32)==type,"draw center resolves exact hit-test");
        }
    }
    void normal(GameLanguage lang)throws Exception{
        create(lang,false);var s=screen.getSession();var h=screen.getHud();var input=screen.getControls();
        check(s.player.hp()==10 && s.weapons.loadout.describe().equals("SWORD:1") && !s.config.debug,"unmodified default gameplay values");
        capture("hud-normal-"+lang.code);insetLayout();
        down(108,108,0);var v=point(160,108);input.touchDragged(Math.round(v.x),Math.round(v.y),0);input.sampleAxes();
        check(input.activePointer()==0 && input.axisX()>.5f,"real joystick owns pointer");
        tap(h.skillX(SkillType.DASH),h.skillY(SkillType.DASH),1);check(s.skills.view(SkillType.DASH).activations()==1 && input.activePointer()==0,"second pointer Dash retains joystick owner");
        screen.render(.01f);capture("hud-dash-active-"+lang.code);up(160,108,0);tick(3);capture("hud-skill-cooldown-"+lang.code);
        screen.togglePause();float cd=s.skills.view(SkillType.DASH).cooldownRemaining();tap(h.skillX(SkillType.DASH),h.skillY(SkillType.DASH),1);tick(2);
        check(cd==s.skills.view(SkillType.DASH).cooldownRemaining() && s.skills.view(SkillType.DASH).activations()==1,"Pause blocks and freezes skills");capture("hud-pause-"+lang.code);
        tap(h.width()/2,h.height()/2-30,1);check(!screen.isPaused(),"actual Resume hit-test");
        // Upgrade through real pickups, including press-highlight, outside release and one-choice max state.
        for(var type:SkillType.values())for(int level=2;level<=5;level++){
            check(s.pickups.spawn(PickupType.SKILL_BOX,s.player.x,s.player.y,1)!=null,"real Skill Box");tick(2);check(s.choosingSkill(),"actual Skill modal");
            int at=-1;for(int i=0;i<s.skillSelection.size();i++)if(s.skillSelection.get(i).type==type)at=i;
            check(at>=0,"eligible skill card exists");if(s.skills.view(SkillType.DASH).level()==5)check(s.skillSelection.size()==1,"max skill removed from interactive offers");
            long token=s.skillSelection.token();float time=s.progress();tick(2);check(time==s.progress(),"modal freezes simulation");
            tap(h.width()/2-325,h.height()/2,1);check(s.choosingSkill() && s.skillSelection.token()==token,"outside tap cannot commit");
            Thread.sleep(270);down(h.width()/2,WeaponHud.cardY(h.height(),at)+38,1);check(s.choosingSkill() && input.selectedOffer()==at,"press highlights, no early commit");
            if(level==2)capture("hud-skill-modal-"+type+"-"+lang.code);
            up(h.width()/2,WeaponHud.cardY(h.height(),at)+38,1);screen.render(0);
            check(s.skills.view(type).level()==level && !screen.chooseWeapon(at,token),"release commits once; stale token rejected");
        }
        check(s.weapons.loadout.describe().equals("SWORD:1"),"skill rewards preserve weapon loadout");tick(310);
        down(108,108,0);tap(h.skillX(SkillType.ULTIMATE),h.skillY(SkillType.ULTIMATE),1);check(s.skills.view(SkillType.ULTIMATE).activations()==1 && input.activePointer()==0,"second pointer Ultimate retains owner");
        capture("hud-ultimate-active-"+lang.code);up(108,108,0);tick(15);capture("hud-skills-max-"+lang.code);
        s.player.damage(Integer.MAX_VALUE);tick(40);check(screen.gameOver(),"real GameOver");capture("hud-game-over-"+lang.code);
        var result=s.result();check(result!=null && !result.victory(),"actual immutable failure result");
        tap(h.width()/2,h.height()/2-30,1);screen.render(0);check(screen.getSession().skills.view(SkillType.DASH).level()==1,"Retry resets skill levels");
        screen.togglePause();tap(h.width()/2,h.height()/2-105,1);check(screen.getSession().phase()==com.example.monstersurvivor.gameplay.GameSession.Phase.ENDED,"actual Home callback");screen.dispose();screen=null;
    }
    void bosses(GameLanguage lang)throws Exception{
        create(lang,true);var s=screen.getSession();screen.getHud().setDebug(false);
        check(layout(screen.getHud(),"hp").width<=230,"HP100000 fits its dedicated row");
        s.diagnosticProgress(299.99f);tick(1);check(s.bosses.mini()!=null,"diagnostic Mini state");capture("hud-mini-"+lang.code);
        s.diagnosticProgress(599.99f);tick(1);var lich=s.bosses.lich();check(lich!=null,"diagnostic Lich state");capture("hud-lich-phase1-"+lang.code);
        for(int i=0;i<3;i++){lich.actor.advance(.21f);lich.actor.damage(10000);}tick(20);check(lich.phase()==2,"real phase transition");capture("hud-lich-phase2-"+lang.code);
        float oldX=lich.actor.x;lich.actor.x=screen.getCamera().position.x+1000;
        capture("hud-boss-direction-"+lang.code);var bossHud=field(screen.getHud(),"bossHud");
        check((Boolean)field(bossHud,"marker"),"offscreen Boss has direction marker");
        check((Float)field(bossHud,"markerX")+21<screen.getHud().skillX(SkillType.DASH)-SkillHud.radius(SkillType.DASH),"Boss direction marker clears skill controls");
        lich.actor.x=oldX;
        for(int i=0;i<10 && lich.actor.alive();i++){lich.actor.advance(.21f);lich.actor.damage(10000);}tick(35);check(screen.victory() && s.result().finalBossDefeated(),"real Victory and result");capture("hud-victory-"+lang.code);close();
    }
    public void create(){try{
        font=new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));batch=new SpriteBatch();profiler=new GLProfiler(Gdx.graphics);profiler.enable();
        for(var lang:GameLanguage.values()){
            for(var key:GameStrings.Key.values())for(char c:GameStrings.of(lang).get(key).toCharArray())if(!Character.isWhitespace(c))check(font.getData().getGlyph(c)!=null,"actual localized glyph "+key);
            normal(lang);bosses(lang);
        }
        System.out.println("HudPolishSmoke PASS "+checks+" checks; max HUD GL draw calls="+maxDraws);
    }catch(Throwable t){failed=true;t.printStackTrace();}finally{if(screen!=null)screen.dispose();if(profiler!=null)profiler.disable();if(batch!=null)batch.dispose();if(font!=null)font.dispose();Gdx.app.exit();}}
    public static void main(String[] args){var c=new Lwjgl3ApplicationConfiguration();c.setTitle("UI03 HUD verification");c.setWindowedMode(1140,540);c.setInitialVisible(false);new Lwjgl3Application(new HudPolishSmoke(),c);if(failed)System.exit(1);}
}
