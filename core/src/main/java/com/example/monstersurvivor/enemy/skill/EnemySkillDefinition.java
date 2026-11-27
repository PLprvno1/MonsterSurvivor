package com.example.monstersurvivor.enemy.skill;

/** Immutable reusable tuning. Damage is intentionally one, independent of late contact damage floors. */
public enum EnemySkillDefinition {
    NONE(Kind.NONE,0,0,0,0,0,0,0,"",null,false),
    ARROW(Kind.SHOT,4.2f,.40f,145,135,1,0,15,"arrow",null,false),
    ROCK(Kind.SHOT,4.8f,.48f,140,115,1,0,30,"rock",null,false),
    COIN(Kind.SHOT,4.8f,.42f,140,125,1,0,45,"coin_shot",null,false),
    SEED(Kind.SHOT,4.4f,.40f,130,110,1,0,20,"seed",null,false),
    THORN_FAN(Kind.SHOT,6.2f,.52f,145,120,3,24,75,"thorn",null,false),
    SPORE(Kind.SHOT,5.6f,.50f,125,100,1,0,45,"spore",PlayerStatuses.Type.POISON,false),
    POISON(Kind.SHOT,5.4f,.48f,140,110,1,0,75,"poison_spit",PlayerStatuses.Type.POISON,false),
    TOXIC(Kind.SHOT,7.4f,.60f,135,95,1,0,150,"spore",PlayerStatuses.Type.POISON,true),
    FIREBALL(Kind.SHOT,5.2f,.45f,150,125,1,0,90,"fireball",PlayerStatuses.Type.BURN,false),
    LAVA(Kind.SHOT,7,.58f,125,100,1,0,150,"lava_glob",PlayerStatuses.Type.BURN,false),
    ICE(Kind.SHOT,5.6f,.48f,145,120,1,0,75,"ice_shard",PlayerStatuses.Type.SLOW,false),
    SNOWBALL(Kind.SHOT,4.8f,.45f,140,110,1,0,45,"snowball",PlayerStatuses.Type.SLOW,false),
    FROST_FAN(Kind.SHOT,7,.56f,145,115,3,24,150,"ice_shard",PlayerStatuses.Type.SLOW,false),
    MAGIC_FAN(Kind.SHOT,7,.55f,155,125,3,24,150,"hex_bolt",null,false),
    DUST_FAN(Kind.SHOT,6.6f,.52f,140,115,3,24,90,"rock",null,false),
    SHADOW(Kind.SHOT,5.8f,.50f,155,130,1,0,180,"shadow_orb",null,false),
    SHADOW_BURST(Kind.SHOT,8.8f,.60f,150,115,5,18,300,"shadow_orb",null,false),
    FIRE_BURST(Kind.SHOT,8.8f,.60f,150,120,5,18,300,"fireball",PlayerStatuses.Type.BURN,false),
    BUBBLE(Kind.SHOT,4.8f,.45f,135,105,1,0,30,"bubble",null,false),
    SLAM(Kind.SLAM,6,.78f,60,0,0,38,75,"quake",null,false),
    SPIRIT_SLAM(Kind.SLAM,7.4f,.82f,66,0,0,40,150,"shockwave",null,false),
    CURSE_AOE(Kind.TARGET,8,.80f,150,0,0,26,210,"magic_orb",null,false),
    TARGET_BOMB(Kind.TARGET,7.8f,.82f,150,0,0,28,120,"bomb",null,false),
    // Small physical melee hit: separate radius/gate from the heavy golem slam.
    CLAW_SLAM(Kind.SLAM,4.8f,.72f,42,0,0,30,20,"quake",null,false),
    // Reuse the imported crystal-shaped flight without granting an unrelated ice status.
    CRYSTAL_SHARD(Kind.SHOT,5.2f,.48f,145,130,1,0,90,"ice_shard",null,false),
    STINGER(Kind.SHOT,4.4f,.42f,130,135,1,0,20,"thorn",null,false);

    public enum Kind { NONE,SHOT,SLAM,TARGET }
    public final Kind kind;
    public final float cooldown,windup,range,speed,spreadOrRadius,unlock;
    public final int count,damage=1;
    public final String fx,impact;
    public final String[] directional;
    public final PlayerStatuses.Type status;
    public final boolean poisonArea;
    EnemySkillDefinition(Kind kind,float cooldown,float windup,float range,float speed,int count,float spread,float unlock,String fx,PlayerStatuses.Type status,boolean area){
        this.kind=kind;this.cooldown=cooldown;this.windup=windup;this.range=range;this.speed=speed;this.count=count;spreadOrRadius=spread;this.unlock=unlock;this.fx=fx;this.status=status;poisonArea=area;
        impact=kind==Kind.SLAM?fx:fx+"_impact";
        directional=new String[]{fx+"_down",fx+"_left",fx+"_right",fx+"_up"};
    }
    public float cooldown(boolean hard){return cooldown*(hard?.72f:1.08f);}
    public float windup(boolean hard){return windup*(hard?.92f:1.08f);}
    public float recovery(boolean hard){return kind==Kind.SHOT?(hard?.12f:.18f):(hard?.20f:.26f);}
    public float radius(){return kind==Kind.SHOT?2:spreadOrRadius;}
}
