package com.example.monstersurvivor.enemy;

import com.badlogic.gdx.utils.JsonValue;
import com.example.monstersurvivor.entity.EnemyType;

/** One immutable species/variant definition shared by every resident of that species. */
public final class EnemyDefinition {
    public final int index,familyIndex,easyHp,hardHp,damage,hardDamage,residentLimit;
    public final String id,name,spriteId,biome,family,special;
    public final EnemyArchetype archetype;
    public final float speed,halfWidth,halfHeight,unlock,weight,hardWeight,nightWeight,scale,feetX,feetY;
    public final float attackSeconds,hurtSeconds,deathSeconds;
    public final boolean flying,elite,charge;
    /** Compatibility with the original three-type API only; null for registry-only species. */
    public final EnemyType legacyType;
    public final com.example.monstersurvivor.enemy.skill.EnemySkillDefinition skill;
    EnemyDefinition(int index,int familyIndex,JsonValue v) {
        this.index=index;this.familyIndex=familyIndex;id=v.getString("id");name=v.getString("name");spriteId=v.getString("sprite");
        biome=v.getString("biome");family=v.getString("family");special=v.getString("special");archetype=EnemyArchetype.valueOf(v.getString("archetype"));
        easyHp=v.getInt("easyHp");hardHp=v.getInt("hardHp");damage=v.getInt("damage");hardDamage=v.getInt("hardDamage");residentLimit=v.getInt("residentLimit");
        speed=v.getFloat("speed");halfWidth=v.getFloat("halfWidth");halfHeight=v.getFloat("halfHeight");unlock=v.getFloat("unlock");
        weight=v.getFloat("weight");hardWeight=v.getFloat("hardWeight");nightWeight=v.getFloat("nightWeight");
        scale=v.getFloat("scale");feetX=v.getFloat("feetX");feetY=v.getFloat("feetY");
        attackSeconds=v.getFloat("attackSeconds");hurtSeconds=v.getFloat("hurtSeconds");deathSeconds=v.getFloat("deathSeconds");
        flying=v.getBoolean("flying");elite=v.getBoolean("elite");charge=special.equals("legacy_charge");
        skill=com.example.monstersurvivor.enemy.skill.EnemySkillAssignments.get(id,family);
        EnemyType old=null;for(EnemyType type:EnemyType.values())if(type.spriteId.equals(id))old=type;legacyType=old;
        // All supported footprints fit a 16px navigation cell. Actual LOS/movement use these extents.
        if(id.isBlank() || easyHp<1 || hardHp<1 || damage<1 || hardDamage<1 || residentLimit<1 || residentLimit>15
            || !positive(speed) || !positive(halfWidth) || halfWidth>7 || !positive(halfHeight) || halfHeight>6
            || !Float.isFinite(unlock) || unlock<0 || !positive(weight) || !positive(hardWeight) || !positive(nightWeight)
            || !positive(scale) || !positive(attackSeconds) || !positive(hurtSeconds) || !positive(deathSeconds))throw new IllegalArgumentException("Enemy definition "+id);
    }
    private static boolean positive(float value) {return Float.isFinite(value) && value>0;}
    public float contactRadius() {return 5+halfWidth;}
    /** Runtime boss body; deliberately absent from EnemyRegistry and ordinary weighted pools. */
    public static EnemyDefinition boss(String id,String name,String sprite,float scale,float feetX,float feetY,float attack,float hurt,float death) {
        return new EnemyDefinition(id,name,sprite,scale,feetX,feetY,attack,hurt,death);
    }
    private EnemyDefinition(String id,String name,String sprite,float scale,float feetX,float feetY,float attack,float hurt,float death) {
        index=familyIndex=-1;this.id=id;this.name=name;spriteId=sprite;family=id;biome="Boss";special="boss";
        archetype=EnemyArchetype.SPECIAL_CANDIDATE;easyHp=hardHp=damage=hardDamage=residentLimit=1;speed=1;
        halfWidth=9;halfHeight=6;unlock=0;weight=hardWeight=nightWeight=1;this.scale=scale;this.feetX=feetX;this.feetY=feetY;
        attackSeconds=attack;hurtSeconds=hurt;deathSeconds=death;flying=elite=charge=false;legacyType=null;
        skill=com.example.monstersurvivor.enemy.skill.EnemySkillDefinition.NONE;
    }
}
