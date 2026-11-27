import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.example.monstersurvivor.audio.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.pickup.PickupType;
import java.lang.reflect.Field;

/** Real libGDX/OpenAL resource load/play/dispose plus screen focus/modal/lifecycle contracts. */
public final class AudioSmoke extends ApplicationAdapter {
    static boolean failed;int checks;
    void check(boolean v,String message){if(!v)throw new AssertionError(message);checks++;}
    public void create() {
        GameScreen screen=null;
        try {
            AudioEnvironment state=new AudioEnvironment();
            screen=new GameScreen(new GameConfig.Builder().debug(true).playerHp(100000).fixedSeed(42).build(),()->{},state);
            screen.show();screen.resize(1140,540);screen.render(.05f);
            Field field=GameScreen.class.getDeclaredField("audio");field.setAccessible(true);GameAudioManager audio=(GameAudioManager)field.get(screen);
            check(audio.failures==0,"all 36 real files loaded by desktop backend");
            for(AudioEvent event:AudioEvent.values())if(!event.name().startsWith("BGM_")) {audio.play(event);Thread.sleep(200);}
            check(audio.failures==0 && audio.played>30,"real Sound.play IDs returned");
            state.focus(false);state.focus(true);screen.render(.05f);check(screen.isPaused(),"focus loss/gain between frames still requires explicit Resume");
            float elapsed=screen.getSession().elapsed();screen.render(.1f);check(elapsed==screen.getSession().elapsed(),"focus pause freezes gameplay");
            screen.togglePause();screen.render(.05f);check(!screen.isPaused(),"explicit resume");
            screen.pause();screen.resume();screen.render(.05f);check(screen.isPaused() && audio.activeVoices()==0,"background/foreground retains user Pause");screen.togglePause();screen.render(.05f);
            var s=screen.getSession();s.pickups.spawn(PickupType.WEAPON_BOX,s.player.x,s.player.y,1);screen.render(.05f);check(s.choosingWeapon(),"real weapon modal");
            check(audio.activeVoices()>0,"modal opening cue survives the same frame music pause");
            int before=audio.played;check(screen.chooseWeapon(0,s.selection.token()),"modal selection commits");check(audio.played==before+1,"selected cue not blocked by music modal pause");screen.render(.05f);
            s.player.damage(Integer.MAX_VALUE);for(int i=0;i<50;i++)screen.render(.05f);check(screen.gameOver(),"terminal screen");
            int starts=audio.musicStarts;screen.retry();screen.render(.05f);check(screen.getSession().phase()==com.example.monstersurvivor.gameplay.GameSession.Phase.RUNNING && audio.musicStarts==starts+1,"Retry new Day stream, no reload or overlap");
            screen.home();check(audio.activeVoices()==0,"Home stops all voices");screen.dispose();screen.dispose();screen=null;
            check(audio.failures==0,"no backend errors through disposal");
            System.out.println("AudioSmoke PASS "+checks+" checks; real OpenAL playback API, no audible listening claim");
        }catch(Throwable error){failed=true;error.printStackTrace();}finally {if(screen!=null)screen.dispose();Gdx.app.exit();}
    }
    public static void main(String[] args) {var c=new Lwjgl3ApplicationConfiguration();c.setTitle("Audio integration check");c.setWindowedMode(1140,540);c.setInitialVisible(false);c.disableAudio(false);new Lwjgl3Application(new AudioSmoke(),c);if(failed)System.exit(1);}
}
