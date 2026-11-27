import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.debug.*;
import com.example.monstersurvivor.render.CameraTrace;
import com.example.monstersurvivor.weapon.*;
import com.badlogic.gdx.utils.JsonReader;

public final class DiagnosticIsolationChecks {
    static int checks;
    static void check(boolean b){if(!b)throw new AssertionError();checks++;}
    public static void main(String[] args) {
        // With no Gdx/files context at all, the normal gate must never inspect local files.
        check(SkillDiagnostics.load(DiagnosticRun.NONE)==null);check(BossDiagnostics.load(DiagnosticRun.NONE)==null);
        check(CombatProfile.load(DiagnosticRun.NONE)==null);check(CameraTrace.load(DiagnosticRun.NONE)==null);
        for(var difficulty:Difficulty.values())for(var mode:GameMode.values()) {
            var c=GameConfig.fromIntent(difficulty.name(),mode.name(),"vi");
            check(c.playerHp==10 && c.swordDamage==1 && !c.debug && c.fixedMapSeed==null);
            check(new WeaponLoadout(c.weapons).describe().equals("SWORD:1"));
        }
        // Reproduces why a restored adb error string was dangerous in the old loader.
        var scalar=new JsonReader().parse("cat: files/boss-test.json: No such file or directory\n");
        check(!scalar.isObject());check(scalar.getBoolean("holdHp",true));check(scalar.getInt("weaponLevel",1)==1);
        for(String bad:new String[]{"../boss-test.json","","legacy","../../","123"}) {
            try {DiagnosticRun.automated(bad);throw new AssertionError("Unsafe diagnostic ID");}catch(IllegalArgumentException expected){checks++;}
        }
        System.out.println("DiagnosticIsolationChecks PASS "+checks+" assertions");
    }
}
