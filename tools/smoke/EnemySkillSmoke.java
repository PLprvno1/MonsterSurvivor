import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.enemy.skill.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.ui.WeaponHud;
import com.example.monstersurvivor.weapon.*;
import java.util.*;

/** Actual GL, real clips and gameplay releases. Explicit fixtures are never normal-run evidence. */
public final class EnemySkillSmoke extends ApplicationAdapter {
    static boolean failed;int checks;GameScreen screen;BitmapFont font;GameLanguage language;
    void check(boolean v,String m){if(!v)throw new AssertionError(m);checks++;}
    void tick(int n){for(int i=0;i<n;i++)screen.render(.05f);}
    void capture(String name){var p=Pixmap.createFromFrameBuffer(0,0,1140,540);PixmapIO.writePNG(Gdx.files.local("../core/build/reports/task08/"+name+"-"+language.code+".png"),p,-1,true);p.dispose();}
    void hud(String name){com.badlogic.gdx.utils.ScreenUtils.clear(.065f,.09f,.125f,1);var s=screen.getSession();var b=new SpriteBatch();screen.getHud().render(b,screen.getControls(),screen.isPaused(),s.elapsed(),.05f,s.player.state(),s.player.direction,s.player,s.kills,s.config.mode,screen.gameOver(),s.pickups,screen.getCamera(),s);capture(name);b.dispose();}
    void tap(float x,float y){var v=new Vector2(x,y);screen.getHud().project(v);int px=Math.round(v.x),py=540-Math.round(v.y);screen.getControls().touchDown(px,py,1,0);screen.getControls().touchUp(px,py,1,0);}
    GameConfig config(){var b=new GameConfig.Builder().language(language).debug(true).fixedSeed(42).playerHp(10000).population(0,40).stages(new GameConfig.Stage(0,0,100000,1000,0,0,0));for(var d:EnemyRegistry.all())b.enemy(d,1000,1,.001f);return b.build();}
    Enemy spawn(EnemySkillDefinition skill){
        var s=screen.getSession();s.enemies.diagnosticClearOrdinary();var d=EnemyRegistry.all().stream().filter(x->x.skill==skill).findFirst().orElseThrow();var grid=s.enemies.navigation.collision;
        for(int y=-6;y<=6;y++)for(int x=-6;x<=6;x++){
            float tx=s.player.x+x*16,ty=s.player.y+y*16,r=(float)Math.hypot(tx-s.player.x,ty-s.player.y);
            var camera=screen.getCamera();if(tx<camera.position.x-camera.viewportWidth/2+20 || tx>camera.position.x+camera.viewportWidth/2-20 || ty<camera.position.y-camera.viewportHeight/2+com.example.monstersurvivor.enemy.skill.EnemySkillSystem.BOTTOM_MARGIN+6 || ty>camera.position.y+camera.viewportHeight/2-com.example.monstersurvivor.enemy.skill.EnemySkillSystem.TOP_MARGIN-6)continue;
            boolean slam=skill.kind==EnemySkillDefinition.Kind.SLAM;if(r<(slam?49:68) || r>(slam?60:98) || !grid.canOccupy(tx,ty,d.halfWidth,d.halfHeight,d.flying) || !grid.clearLine(tx,ty,s.player.x,s.player.y,2,2,true))continue;
            var e=s.enemies.spawn(d,tx,ty,s.progress());if(e!=null){e.specialSkill.cooldown=0;return e;}
        }
        throw new AssertionError("No valid scene placement "+skill);
    }
    void run(GameLanguage lang)throws Exception{
        language=lang;screen=new GameScreen(config(),()->{});screen.show();screen.resize(1140,540);screen.render(0);var s=screen.getSession();screen.getHud().setDebug(false);s.diagnosticProgress(420);
        var catalog=new com.badlogic.gdx.utils.JsonReader().parse(Gdx.files.internal("game/enemy-fx.json"));int clips=0,frames=0;
        for(var clip=catalog.get("clips").child;clip!=null;clip=clip.next){clips++;for(var rect=clip.get("frames").child;rect!=null;rect=rect.next){frames++;check(rect.getInt(0)>=2 && rect.getInt(1)>=2 && rect.getInt(0)+rect.getInt(2)<512 && rect.getInt(1)+rect.getInt(3)<512,"padded atlas frame bounds");}check(screen.getAssets().enemyFrame(clip.name,1)!=null,"prebuilt real FX clip");}
        check(clips==81 && frames==274,"exact curated FX subset");
        for(var definition:EnemyRegistry.all())if(definition.skill!=EnemySkillDefinition.NONE){var sprites=screen.getAssets().sprite(definition.spriteId);for(var dir:com.example.monstersurvivor.assets.GameAssets.Direction.values())check(sprites.has(com.example.monstersurvivor.assets.GameAssets.State.SPECIAL,dir)||sprites.has(com.example.monstersurvivor.assets.GameAssets.State.ATTACK,dir),"actual Special or Attack per assigned species/direction");}
        for(var skill:new EnemySkillDefinition[]{EnemySkillDefinition.ARROW,EnemySkillDefinition.THORN_FAN,EnemySkillDefinition.FIREBALL,EnemySkillDefinition.TOXIC,EnemySkillDefinition.ICE,EnemySkillDefinition.SLAM,EnemySkillDefinition.CURSE_AOE,EnemySkillDefinition.SHADOW_BURST,EnemySkillDefinition.BUBBLE,EnemySkillDefinition.LAVA,EnemySkillDefinition.MAGIC_FAN}){
            var enemy=spawn(skill);int before=s.enemies.skills.releases;tick(2);check(enemy.specialSkill.busy(),"GL cast started "+skill);
            var state=enemy.state();check(enemy.sprites.has(state,enemy.direction),"actual clip used, no fake Special "+skill);capture("enemy-warning-"+skill);
            for(int i=0;i<35 && s.enemies.skills.releases==before;i++)tick(1);
            check(s.enemies.skills.releases==before+1,"GL gameplay release "+skill);capture("enemy-release-"+skill);tick(5);capture("enemy-flight-impact-"+skill);
            check(s.enemies.skills.projectiles.size()<=24 && s.enemies.skills.projectiles.effectCount()<=24 && s.enemies.skills.projectiles.areaCount()<=3 && s.enemies.maxSearchesObserved<=2,"GL budgets "+skill);
        }
        // Visual status fixture, separate from accepted-hit proofs in EnemySkillChecks and Android gameplay.
        for(var type:PlayerStatuses.TYPES)s.player.statuses.apply(type);screen.render(0);hud("hud-three-statuses");capture("hud-status-overlay");
        var e=spawn(EnemySkillDefinition.MAGIC_FAN);tick(2);screen.togglePause();float age=e.specialSkill.age,poison=s.player.statuses.remaining(PlayerStatuses.Type.POISON);tick(4);
        check(age==e.specialSkill.age && poison==s.player.statuses.remaining(PlayerStatuses.Type.POISON),"Pause freezes hostile casts and statuses");screen.togglePause();
        s.pickups.spawn(PickupType.SKILL_BOX,s.player.x,s.player.y,1);tick(2);check(s.choosingSkill(),"actual modal over cast");age=e.specialSkill.age;poison=s.player.statuses.remaining(PlayerStatuses.Type.POISON);tick(4);
        check(age==e.specialSkill.age && poison==s.player.statuses.remaining(PlayerStatuses.Type.POISON),"modal freezes hostile clocks");Thread.sleep(270);tap(screen.getHud().width()/2,WeaponHud.cardY(screen.getHud().height(),0)+38);screen.render(0);check(!s.choosingSkill(),"input release upgrades through existing transaction");
        // Existing evolution APIs in an explicit combat fixture; pools/teams remain independent.
        for(var type:WeaponType.values()){
            s.enemies.diagnosticClearOrdinary();s.weapons.loadout.reset();
            while(s.weapons.loadout.get(type)==null || s.weapons.loadout.get(type).level()<5)check(s.weapons.loadout.grant(type),"fixture level progression");check(WeaponOffer.evolution(type,s.config.strings).apply(s.weapons.loadout,s.player),"explicit fixture evolution");
            e=spawn(EnemySkillDefinition.THORN_FAN);tick(30);check(s.weapons.loadout.get(type).evolved() && s.enemies.skills.projectiles.size()<=24 && s.weapons.projectiles.size()<=48,"evolution + enemy cast coexist "+type);capture("enemy-evolution-"+type);
        }
        check(screen.getAssets().textureCount()==21 && screen.getAssets().textureBytes()==45596676 && Gdx.gl.glGetError()==GL20.GL_NO_ERROR,"one new bounded atlas and GL clean");
        s.enemies.diagnosticClearOrdinary();s.diagnosticProgress(600);tick(2);var lich=s.bosses.lich();check(lich!=null && lich.actor.definition.skill==EnemySkillDefinition.NONE,"Boss has no ordinary archetype");
        for(int i=0;i<8 && lich.actor.alive();i++){lich.actor.advance(.21f);lich.actor.damage(10000);}tick(35);check(screen.victory(),"real guarded Boss death reaches Victory");hud("hud-victory");
        screen.retry();screen.render(0);check(screen.getSession().weapons.loadout.describe().equals("SWORD:1") && !screen.getSession().player.statuses.active(PlayerStatuses.Type.POISON) && screen.getSession().enemies.skills.projectiles.size()==0,"Retry clears ordinary hostile/status state");
        screen.getPlayer().damage(Integer.MAX_VALUE);tick(40);check(screen.gameOver(),"GameOver regression");hud("hud-game-over");screen.home();screen.dispose();screen=null;
    }
    public void create(){try{font=new BitmapFont(Gdx.files.internal("game/fonts/game-ui.fnt"));for(var lang:GameLanguage.values()){
        for(var key:GameStrings.Key.values())for(char c:GameStrings.of(lang).get(key).toCharArray())if(!Character.isWhitespace(c))check(font.getData().getGlyph(c)!=null,"real localized glyph "+key);
        run(lang);
    }System.out.println("EnemySkillSmoke PASS "+checks+" checks (real GL/gameplay/FX/82-species clips/VI-EN)");}catch(Throwable t){failed=true;t.printStackTrace();}finally{if(screen!=null)screen.dispose();if(font!=null)font.dispose();Gdx.app.exit();}}
    public static void main(String[] args){var c=new Lwjgl3ApplicationConfiguration();c.setTitle("Task08 enemy skills");c.setWindowedMode(1140,540);c.setInitialVisible(false);c.useVsync(false);c.setAudioConfig(32,512,9);new Lwjgl3Application(new EnemySkillSmoke(),c);if(failed)System.exit(1);}
}
