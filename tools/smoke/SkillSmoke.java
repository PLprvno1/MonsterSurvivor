import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.audio.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.ui.SkillHud;
import static com.example.monstersurvivor.skill.SkillType.*;

/** Real GL/audio/presentation and actual GameInput callbacks. This is desktop, not Android touch evidence. */
public final class SkillSmoke extends ApplicationAdapter {
    static boolean failed;int checks;GameScreen screen;
    void check(boolean b,String label){if(!b)throw new AssertionError(label);checks++;}
    void tick(int n){for(int i=0;i<n;i++)screen.render(.05f);}
    Vector2 at(float x,float y){var v=new Vector2(x,y);screen.getHud().project(v);v.y=Gdx.graphics.getHeight()-v.y;return v;}
    void down(float x,float y,int id){var v=at(x,y);screen.getControls().touchDown(Math.round(v.x),Math.round(v.y),id,0);}
    void drag(float x,float y,int id){var v=at(x,y);screen.getControls().touchDragged(Math.round(v.x),Math.round(v.y),id);}
    void up(float x,float y,int id){var v=at(x,y);screen.getControls().touchUp(Math.round(v.x),Math.round(v.y),id,0);}
    void capture(String label){Pixmap p=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task06/"+label+".png"),p,-1,true);p.dispose();}
    void run(GameLanguage language) throws Exception {
        var config=new GameConfig.Builder().language(language).debug(true).playerHp(100000).fixedSeed(42).population(0,40)
            .enemy(EnemyType.SLIME,100,1,1).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();
        AudioEnvironment environment=new AudioEnvironment();screen=new GameScreen(config,()->{},environment);screen.show();screen.resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());screen.render(0);
        var s=screen.getSession();var hud=screen.getHud();var input=screen.getControls();check(s.skills.view(DASH).phase()==ActiveSkillSystem.Phase.READY && s.skills.view(ULTIMATE).cooldownRemaining()==15,"baseline HUD state");
        capture("skill-hud-"+language.code);down(100,150,0);drag(166,216,0);down(hud.skillX(DASH),hud.skillY(DASH),1);screen.render(.05f);
        check(s.skills.view(DASH).activations()==1 && input.activePointer()==0,"GL multitouch keeps joystick owner");check(s.skills.trailCount()>0,"real Dash trail");capture("skill-dash-"+language.code);
        float remaining=s.skills.view(DASH).activeRemaining(),cd=s.skills.view(DASH).cooldownRemaining();screen.togglePause();tick(4);
        check(s.skills.view(DASH).activeRemaining()==remaining && s.skills.view(DASH).cooldownRemaining()==cd,"pause freezes active Dash/HUD");screen.pause();screen.resume();tick(1);check(screen.isPaused(),"lifecycle keeps explicit Pause");
        check(!screen.activateSkill(ULTIMATE,0,0),"paused screen rejects skill");screen.togglePause();tick(6);check(!s.player.dashInvulnerable(),"Dash iframe expires after Resume");
        environment.focus(false);environment.focus(true);screen.render(.05f);check(screen.isPaused() && !screen.activateSkill(DASH,1,0),"focus loss/gain latch blocks skills");screen.togglePause();tick(310);
        check(s.skills.view(ULTIMATE).phase()==ActiveSkillSystem.Phase.READY,"real-time initial cooldown completes");
        var nav=s.enemies.navigation;Enemy target=null;
        for(int y=-6;y<=6 && target==null;y++)for(int x=-6;x<=6 && target==null;x++) {float tx=s.player.x+x*16,ty=s.player.y+y*16,d=(float)Math.hypot(tx-s.player.x,ty-s.player.y);
            if(d<65 || d>90 || !nav.collision.clearLine(s.player.x,s.player.y,tx,ty,6,4,false))continue;target=s.enemies.spawn(EnemyType.SLIME,tx,ty);}
        check(target!=null,"placed actual target");down(hud.skillX(ULTIMATE),hud.skillY(ULTIMATE),1);up(hud.skillX(ULTIMATE),hud.skillY(ULTIMATE),1);tick(2);capture("skill-energy-"+language.code);
        s.pickups.spawn(PickupType.WEAPON_BOX,s.player.x,s.player.y,1);tick(1);check(s.choosingWeapon(),"real weapon modal mid-cast");remaining=s.skills.view(ULTIMATE).activeRemaining();tick(12);
        check(s.skills.view(ULTIMATE).activeRemaining()==remaining && s.skills.ultimateImpacts==0,"modal freezes cast, no hidden damage");
        down(hud.skillX(DASH),hud.skillY(DASH),2);up(hud.skillX(DASH),hud.skillY(DASH),2);check(s.skills.view(DASH).activations()==1,"modal button footprint cannot trigger skill");
        capture("skill-modal-"+language.code);screen.chooseWeapon(0,s.selection.token());tick(7);
        check(s.skills.ultimateImpacts==1 && s.skills.ultimateHits>0 && s.skills.knockbacks>0,"one real Ultimate damage/knockback release");check(s.weapons.loadout.size()==2,"skills do not consume weapon slots");
        var font=new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));font.getData().setScale(SkillHud.FONT_SCALE);
        for(var key:GameStrings.Key.values())if(key.name().startsWith("SKILL_"))for(char c:config.strings.get(key).toCharArray())check(font.getData().getGlyph(c)!=null,"localized skill glyph");
        for(SkillType type:SkillType.values())for(var key:new GameStrings.Key[]{type==DASH?GameStrings.Key.SKILL_DASH:GameStrings.Key.SKILL_ULTIMATE,GameStrings.Key.SKILL_READY,GameStrings.Key.SKILL_ACTIVE,GameStrings.Key.SKILL_DISABLED}) {
            var label=new GlyphLayout(font,config.strings.get(key));check(label.width<SkillHud.radius(type)*2-4,"button label fits circle: "+key+language);
        }
        font.dispose();s.player.damage(Integer.MAX_VALUE);tick(40);check(screen.gameOver(),"GameOver regression");screen.retry();screen.render(.05f);
        check(screen.getSession()!=s && screen.getSession().skills.view(DASH).activations()==0 && screen.getSession().skills.view(ULTIMATE).cooldownRemaining()==15,"Retry owns fresh state");
        check(screen.getAssets().textureCount()==19,"no new texture for HUD or VFX");check(Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"no GL errors");screen.home();screen.dispose();screen.dispose();screen=null;
    }
    public void create(){try {run(GameLanguage.VI);run(GameLanguage.EN);System.out.println("SkillSmoke PASS "+checks+" checks (desktop GL/input/audio; Android checked separately)");}catch(Throwable t){failed=true;t.printStackTrace();}finally {if(screen!=null)screen.dispose();Gdx.app.exit();}}
    public static void main(String[] args){var c=new Lwjgl3ApplicationConfiguration();c.setTitle("Active skills verification");c.setWindowedMode(1140,540);c.setInitialVisible(false);new Lwjgl3Application(new SkillSmoke(),c);if(failed)System.exit(1);}
}
