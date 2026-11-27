import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.ui.WeaponHud;
import com.example.monstersurvivor.weapon.*;

/** Real GL, input release, localized fit, VFX and Retry. Distinct from Android touch evidence. */
public class HudBeforeSmoke extends ApplicationAdapter {
    static boolean failed;int checks;GameScreen screen;BitmapFont font;
    void check(boolean v,String m){if(!v)throw new AssertionError(m);checks++;}
    void tick(int n){for(int i=0;i<n;i++)screen.render(.05f);}
    void capture(String name){var p=Pixmap.createFromFrameBuffer(0,0,1140,540);PixmapIO.writePNG(Gdx.files.local("../core/build/reports/ui03/before/"+name+".png"),p,-1,true);p.dispose();}
    void hudOnly(String name){
        com.badlogic.gdx.utils.ScreenUtils.clear(.08f,.12f,.1f,1);var s=screen.getSession();var p=s.player;var batch=new SpriteBatch();
        screen.getHud().render(batch,screen.getControls(),screen.isPaused(),s.elapsed(),.05f,p.state(),p.direction,p,s.kills,s.config.mode,false,s.pickups,screen.getCamera(),s);
        capture(name);batch.dispose();
    }
    void modal()throws Exception {
        var s=screen.getSession();check(s.pickups.spawn(PickupType.WEAPON_BOX,s.player.x,s.player.y,1)!=null,"real GL box");tick(2);check(s.choosingWeapon(),"real GL modal");
        for(int i=0;i<s.selection.size();i++){
            var o=s.selection.get(i);font.getData().setScale(.85f);check(new GlyphLayout(font,o.title).width<518,"localized title fits "+o.title);
            font.getData().setScale(.75f);check(new GlyphLayout(font,o.description).width<518,"localized description fits");font.getData().setScale(1);
        }
        Thread.sleep(265);
    }
    void choose(int at){
        var v=new Vector2(screen.getHud().width()/2,WeaponHud.cardY(screen.getHud().height(),at)+38);screen.getHud().project(v);int x=Math.round(v.x),y=540-Math.round(v.y);
        long token=screen.weaponToken();screen.getControls().touchDown(x,y,1,0);check(screen.weaponSelection(),"no press commit");screen.getControls().touchUp(x,y,1,0);
        check(!screen.weaponSelection() && !screen.chooseWeapon(at,token),"release commits once");screen.render(0);
    }
    int index(WeaponType type){var s=screen.getSession();for(int i=0;i<s.selection.size();i++)if(s.selection.get(i).weapon==type)return i;return -1;}
    void run(GameLanguage lang,WeaponType type)throws Exception {
        var c=new GameConfig.Builder().language(lang).fixedSeed(42).playerHp(100000).population(0,40).enemy(EnemyType.SLIME,100,1,.01f).stages(new GameConfig.Stage(0,0,100000,1,0,0,0)).build();
        screen=new GameScreen(c,()->{});screen.show();screen.resize(1140,540);screen.render(0);var s=screen.getSession();
        if(type==WeaponType.SWORD){modal();choose(index(WeaponType.BOW));}
        for(int i=0;i<40 && (s.weapons.loadout.get(type)==null || s.weapons.loadout.get(type).level()<5);i++){modal();int at=index(type);choose(at<0?0:at);}
        check(!s.weapons.loadout.get(type).evolved(),"no automatic evolution");modal();int at=index(type);check(at>=0 && s.selection.get(at).kind==WeaponOffer.Kind.EVOLUTION,"actual evolution offer");
        capture("evolution-offer-"+type+"-"+lang.code);hudOnly("hud-offer-"+type+"-"+lang.code);screen.togglePause();check(!screen.chooseWeapon(at,s.selection.token()),"Pause blocks evolution");screen.togglePause();screen.render(0);choose(at);
        check(s.weapons.loadout.get(type).evolved(),"touch earned evolution");
        hudOnly("hud-evolved-"+type+"-"+lang.code);
        var g=screen.getMap().grid;
        for(int y=-3;y<=3;y++)for(int x=2;x<=7;x++){
            float tx=s.player.x+x*16,ty=s.player.y+y*16;if(g.canOccupy(tx,ty,6,4) && g.clearLine(s.player.x,s.player.y,tx,ty,1,1,true))s.enemies.spawn(EnemyType.SLIME,tx,ty);
        }
        if(type!=WeaponType.SWORD)s.weapons.loadout.get(type).cooldownRemaining=0;
        tick(type==WeaponType.LIGHTNING?1:type==WeaponType.FIREBALL?8:3);capture("evolution-vfx-"+type+"-"+lang.code);
        tick(12);if(type==WeaponType.FIREBALL)capture("evolution-burning-"+lang.code);
        check(screen.getAssets().textureCount()==20 && screen.getAssets().textureBytes()==44548100 && Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"bounded atlas and no GL error");
        String owned=s.weapons.loadout.describe();screen.pause();screen.resume();check(screen.isPaused() && owned.equals(s.weapons.loadout.describe()),"lifecycle preserves evolved loadout");screen.togglePause();tick(16);
        s.player.damage(Integer.MAX_VALUE);tick(40);check(screen.gameOver(),"death reaches GameOver");screen.retry();screen.render(0);
        check(screen.getSession().weapons.loadout.describe().equals("SWORD:1"),"Retry clears all evolutions");screen.home();screen.dispose();screen=null;
    }
    public void create(){try {
        font=new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));
        for(var lang:GameLanguage.values()){
            var strings=GameStrings.of(lang);for(var key:GameStrings.Key.values())for(char ch:strings.get(key).toCharArray())if(!Character.isWhitespace(ch))check(font.getData().getGlyph(ch)!=null,"actual glyph "+ch);
            for(var type:WeaponType.values())run(lang,type);
        }
        System.out.println("HudBeforeSmoke PASS "+checks+" checks (desktop GL/touch only)");
    }catch(Throwable e){failed=true;e.printStackTrace();}finally {if(screen!=null)screen.dispose();if(font!=null)font.dispose();Gdx.app.exit();}}
    public static void main(String[] args){var c=new Lwjgl3ApplicationConfiguration();c.setTitle("Evolution verification");c.setWindowedMode(1140,540);c.setInitialVisible(false);new Lwjgl3Application(new HudBeforeSmoke(),c);if(failed)System.exit(1);}
}
