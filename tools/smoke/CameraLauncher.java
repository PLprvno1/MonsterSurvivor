import com.badlogic.gdx.backends.lwjgl3.*;
import com.example.monstersurvivor.MonsterSurvivorGame;

/** Runs the actual screen with a local opt-in trace, no substitute movement implementation. */
public final class CameraLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();
        c.setTitle("MonsterSurvivor camera measurement");c.setWindowedMode(1140,540);
        c.disableAudio(true);c.useVsync(true);c.setForegroundFPS(60);
        new Lwjgl3Application(new MonsterSurvivorGame(),c);
    }
}
