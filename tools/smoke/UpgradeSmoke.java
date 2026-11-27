import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.audio.AudioEnvironment;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.debug.DiagnosticRun;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.ui.*;
import java.util.*;
import static com.example.monstersurvivor.skill.SkillType.*;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** GL + genuine GameInput selection callbacks, fixture isolation and bounded VFX. Android evidence is separate. */
public final class UpgradeSmoke extends ApplicationAdapter {
    static boolean failed;int checks;GameScreen screen;
    final Map<String,byte[]> previous=new LinkedHashMap<>();
    final String runId=UUID.randomUUID().toString().replace("-","");
    void check(boolean v,String label){if(!v)throw new AssertionError(label);checks++;}
    void tick(int n){for(int i=0;i<n;i++)screen.render(.05f);}
    void capture(String name){var p=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());PixmapIO.writePNG(Gdx.files.local(System.getProperty("ms.report","../core/build/reports/task061/")+name+".png"),p,-1,true);p.dispose();}
    void save(String name,String contents){var file=Gdx.files.local(name);previous.put(name,file.exists()?file.readBytes():null);file.writeString(contents,false,"UTF-8");}
    GameConfig config(GameLanguage lang){return new GameConfig.Builder().language(lang).population(0,40).stages(new GameConfig.Stage(0,0,100000,1000,0,0,0)).build();}
    void createScreen(GameConfig c){screen=new GameScreen(c,()->{});screen.show();screen.resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());screen.render(0);}
    void tap(float x,float y,int pointer){var v=new Vector2(x,y);screen.getHud().project(v);int px=Math.round(v.x),py=Gdx.graphics.getHeight()-Math.round(v.y);screen.getControls().touchDown(px,py,pointer,0);screen.getControls().touchUp(px,py,pointer,0);}
    void levelNames(BitmapFont font,GameSession s){
        for(SkillType type:SkillType.values()) {
            font.getData().setScale(SkillHud.FONT_SCALE);String name=s.config.strings.format(SKILL_NAME_LEVEL,s.config.strings.get(type==DASH?SKILL_DASH:SKILL_ULTIMATE),s.skills.view(type).level());
            var layout=new GlyphLayout(font,name);check(layout.width<=2*SkillHud.radius(type)-4,"actual level label fits "+name+" width="+layout.width);
        }
        font.getData().setScale(1);
    }
    void run(GameLanguage lang)throws Exception {
        createScreen(config(lang));var s=screen.getSession();
        check(s.player.hp()==10 && s.weapons.loadout.describe().equals("SWORD:1") && !s.config.debug,"normal GL ignores every stale root fixture");
        check(s.progress()==0 && screen.getMap().seed!=42,"normal GL has real random map and baseline clocks");
        var font=new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));levelNames(font,s);capture("upgrade-lv1-"+lang.code);
        // Both skills advance through actual boxes and touch-down/up commits, including one-choice modal at max.
        for(SkillType type:SkillType.values())for(int level=2;level<=5;level++) {
            check(s.pickups.spawn(PickupType.SKILL_BOX,s.player.x,s.player.y,1)!=null,"GL actual Skill Box");screen.render(.001f);
            check(s.choosingSkill() && screen.weaponSelection(),"skill modal routes through input priority");
            int index=-1;for(int i=0;i<s.skillSelection.size();i++) {
                var offer=s.skillSelection.get(i);if(offer.type==type)index=i;
                font.getData().setScale(.75f);check(new GlyphLayout(font,offer.benefit).width<588 && new GlyphLayout(font,offer.detail).width<588,"localized modal benefit fits card");
                font.getData().setScale(1);check(new GlyphLayout(font,offer.title).width<588,"localized title fits card");
            }
            if(level==2)capture("upgrade-modal-"+type+"-"+lang.code);
            float cd=s.skills.view(type).cooldownRemaining();tick(2);check(s.skills.view(type).cooldownRemaining()==cd,"modal freezes cooldown in GL");
            check(!screen.activateSkill(DASH,1,0),"modal blocks skill buttons");long token=s.skillSelection.token();
            Thread.sleep(270);tap(screen.getHud().width()/2,WeaponHud.cardY(screen.getHud().height(),index)+38,1);screen.render(0);
            check(s.skills.view(type).level()==level && !s.choosingSkill(),"actual GL touch commits requested level");
            check(!screen.chooseWeapon(index,token),"repeated/stale modal callback rejected");levelNames(font,s);
            check(s.weapons.loadout.describe().equals("SWORD:1"),"weapon slots stay unchanged");
            if(level==3 || level==5)capture("upgrade-"+type+"-lv"+level+"-"+lang.code);
        }
        check(!s.skills.hasUpgrade() && s.pickups.skillBoxes()==0,"max handling GL");
        screen.activateSkill(DASH,1,0);screen.render(.05f);capture("upgrade-dash-lv5-"+lang.code);screen.togglePause();float active=s.skills.view(DASH).activeRemaining();tick(3);
        check(s.skills.view(DASH).activeRemaining()==active && s.skills.view(DASH).level()==5,"Pause freezes active skill and keeps level");screen.pause();screen.resume();check(screen.isPaused(),"background requires explicit Resume");screen.togglePause();tick(310);
        check(screen.activateSkill(ULTIMATE,0,0),"Lv5 Ultimate ready");tick(2);capture("upgrade-energy-lv5-"+lang.code);tick(8);check(s.skills.ultimateImpacts==1,"Lv5 one release in GL");
        check(screen.getAssets().textureCount()==21 && Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"one bounded evolution atlas and no GL error");
        s.player.damage(Integer.MAX_VALUE);tick(40);check(screen.gameOver(),"GameOver regression");screen.retry();screen.render(0);
        check(screen.getSession().player.hp()==10 && screen.getSession().skills.view(DASH).level()==1 && screen.getSession().skills.view(ULTIMATE).level()==1,"Retry resets baseline and levels without fixture");
        screen.home();screen.dispose();screen=null;font.dispose();
    }
    void explicitRun() {
        var run=DiagnosticRun.automated(runId);run.file("skill-test.json").writeString("{protectedHp:true,progress:600}",false,"UTF-8");
        screen=new GameScreen(config(GameLanguage.VI),()->{},new AudioEnvironment(),run);screen.show();screen.resize(1140,540);screen.render(0);
        check(screen.getSession().player.hp()==100000 && screen.getSession().progress()==600 && screen.debugEnabled(),"explicit namespaced capability enables only requested fixture");
        screen.home();screen.dispose();screen=null;
        run.file("skill-test.json").writeString("cat: No such file",false,"UTF-8");
        check(com.example.monstersurvivor.debug.SkillDiagnostics.load(run)==null,"scalar fixture cannot default-enable diagnostics");
        createScreen(config(GameLanguage.VI));check(screen.getSession().player.hp()==10 && !screen.debugEnabled(),"normal constructor ignores interrupted test namespace");screen.dispose();screen=null;
        run.file("skill-test.json").delete();run.file("unused").parent().delete();
    }
    public void create(){try {
        save("skill-test.json","{protectedHp:true,progress:600}");save("boss-test.json","{holdHp:true,weaponLevel:5,progress:600}");save("combat-profile.json","{worldProbe:true}");save("map-seed.txt","42");save("camera-check.json","{mode:WORLD}");save("asset-visual-check","");
        run(GameLanguage.VI);run(GameLanguage.EN);explicitRun();System.out.println("UpgradeSmoke PASS "+checks+" checks (desktop GL/input only)");
    }catch(Throwable error){failed=true;error.printStackTrace();}finally {
        if(screen!=null)screen.dispose();for(var entry:previous.entrySet()){var f=Gdx.files.local(entry.getKey());if(entry.getValue()==null)f.delete();else f.writeBytes(entry.getValue(),false);}Gdx.app.exit();
    }}
    public static void main(String[] args){var c=new Lwjgl3ApplicationConfiguration();c.setTitle("Skill upgrades verification");c.setWindowedMode(1140,540);c.setInitialVisible(false);new Lwjgl3Application(new UpgradeSmoke(),c);if(failed)System.exit(1);}
}
