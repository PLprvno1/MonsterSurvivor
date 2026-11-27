import com.example.monstersurvivor.character.CharacterId;
import com.example.monstersurvivor.character.CharacterProfile;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.weapon.WeaponSelection;
import com.example.monstersurvivor.weapon.WeaponType;
import com.example.monstersurvivor.entity.EnemyType;
import com.example.monstersurvivor.config.GameMode;
import com.example.monstersurvivor.world.CollisionGrid;

/** Small focused check for the three starter classes and legacy Warrior fallback. */
public final class CharacterClassChecks {
    private static int checks;
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);checks++;}

    public static void main(String[] args) {
        var config=new GameConfig.Builder().mode(GameMode.NORMAL).population(0,60)
            .enemy(EnemyType.SLIME,100,1,.01f).build();
        var grid=new CollisionGrid(100,100,16,new byte[10000]);
        CharacterId[] ids={CharacterId.WARRIOR,CharacterId.ARCHER,CharacterId.MAGE};
        WeaponType[] starters={WeaponType.SWORD,WeaponType.BOW,WeaponType.FIREBALL};
        String[] sprites={"player","archer","mage"};
        for(int i=0;i<ids.length;i++) {
            var profile=CharacterProfile.forId(ids[i].stableId());
            var session=new GameSession(config,grid,808,808,null,null,42,ids[i].stableId());
            var starter=session.weapons.loadout.slot(0);
            check(session.character.id==ids[i] && profile.spriteId.equals(sprites[i]),ids[i]+" resolves to its character sprite");
            check(session.weapons.loadout.size()==1 && starter.type==starters[i] && starter.level()==1,
                ids[i]+" starts with its signature weapon at Lv1");
            check(session.player.maxHp()==10 && session.player.stats.damage()==1 && session.player.stats.speed()==10,
                ids[i]+" preserves the base player stats");
            if(ids[i]!=CharacterId.WARRIOR) {
                check(!session.weapons.loadout.canGrant(WeaponType.SWORD),ids[i]+" cannot acquire an unsupported Sword");
                session.enemies.spawn(EnemyType.SLIME,888,808);
                session.weapons.update(.01f,session.player,session.enemies,grid);
                check(starter.activations==1 && session.player.state()==com.example.monstersurvivor.assets.GameAssets.State.ATTACK,
                    ids[i]+" fires its starter and plays its mapped attack pose");
                var offers=new WeaponSelection(42,config.strings);
                check(offers.open(1,session.weapons.loadout,session.player),ids[i]+" can open the normal weapon reward");
                for(int n=0;n<offers.size();n++)check(offers.get(n).weapon!=WeaponType.SWORD,ids[i]+" reward pool excludes Sword");
            }
        }
        check(CharacterId.fromStableId(null)==CharacterId.WARRIOR && CharacterId.fromStableId("invalid")==CharacterId.WARRIOR,
            "Missing or invalid class IDs fall back to Warrior");
        var warrior=new GameSession(config,grid,808,808,null,null,42);
        var target=warrior.enemies.spawn(EnemyType.SLIME,830,808);
        for(int frame=0;frame<50;frame++) {
            warrior.player.update(.05f,0,0,grid);
            warrior.weapons.update(.05f,warrior.player,warrior.enemies,grid);
        }
        check(warrior.combat.swings>0 && target.hp()<100,"Legacy Warrior still performs the original Sword combat");
        System.out.println("CHARACTER CLASS CHECKS PASSED: "+checks+" assertions");
    }
}
