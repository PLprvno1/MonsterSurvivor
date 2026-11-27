package com.example.monstersurvivor.config;

/** Immutable base-skill tuning. Future level tables can supply these values without changing input/HUD. */
public final class SkillConfig {
    public static final int MAX_LEVEL=5;
    public static final class Boxes {
        public final float firstMin,firstMax,intervalMin,intervalMax,lifetime;
        public final int cap;
        public Boxes() {this(18,24,18,24,60,2);}
        public Boxes(float firstMin,float firstMax,float intervalMin,float intervalMax,float lifetime,int cap) {
            if(!positive(firstMin) || !Float.isFinite(firstMax) || firstMax<firstMin || !positive(intervalMin)
                || !Float.isFinite(intervalMax) || intervalMax<intervalMin || !positive(lifetime) || lifetime>60 || cap<1 || cap>2)
                throw new IllegalArgumentException("Skill box tuning");
            this.firstMin=firstMin;this.firstMax=firstMax;this.intervalMin=intervalMin;this.intervalMax=intervalMax;this.lifetime=lifetime;this.cap=cap;
        }
    }
    public abstract static class Tuning {
        public final float cooldown,initialCooldown,duration;
        protected Tuning(float cooldown,float initialCooldown,float duration) {
            if(!positive(cooldown) || !Float.isFinite(initialCooldown) || initialCooldown<0 || initialCooldown>cooldown
                || !positive(duration) || duration>cooldown)throw new IllegalArgumentException("Skill timing");
            this.cooldown=cooldown;this.initialCooldown=initialCooldown;this.duration=duration;
        }
    }
    public static final class Dash extends Tuning {
        public final float distance,invulnerability;
        public Dash(float cooldown,float duration,float distance,float invulnerability) {
            super(cooldown,0,duration);
            if(!positive(distance) || !positive(invulnerability) || invulnerability>duration)throw new IllegalArgumentException("Dash tuning");
            this.distance=distance;this.invulnerability=invulnerability;
        }
    }
    public static final class Ultimate extends Tuning {
        public final float radius,damageMultiplier,knockback,tankKnockbackScale;
        public final int minimumDamage,hitFxCap;
        public Ultimate(float cooldown,float initialCooldown,float duration,float radius,float multiplier,int minimumDamage,
                        float knockback,float tankKnockbackScale,int hitFxCap) {
            super(cooldown,initialCooldown,duration);
            if(!positive(radius) || !positive(multiplier) || minimumDamage<1 || !Float.isFinite(knockback) || knockback<0
                || !Float.isFinite(tankKnockbackScale) || tankKnockbackScale<0 || tankKnockbackScale>1 || hitFxCap<0 || hitFxCap>16)
                throw new IllegalArgumentException("Ultimate tuning");
            this.radius=radius;damageMultiplier=multiplier;this.minimumDamage=minimumDamage;this.knockback=knockback;
            this.tankKnockbackScale=tankKnockbackScale;this.hitFxCap=hitFxCap;
        }
        public int damage(int base) {return (int)Math.min(Integer.MAX_VALUE,Math.max(minimumDamage,Math.ceil(base*(double)damageMultiplier)));}
    }
    public final Dash dash;
    public final Ultimate ultimate;
    public final Boxes boxes;
    private final Dash[] dashLevels;
    private final Ultimate[] ultimateLevels;
    public SkillConfig() {
        this(new Dash[]{new Dash(5,.2f,75,.17f),new Dash(4.6f,.2f,80,.17f),new Dash(4.2f,.2f,86,.18f),new Dash(3.8f,.2f,93,.18f),new Dash(3.5f,.2f,100,.2f)},
            new Ultimate[]{burst(30,105,2.5f,25),burst(28,115,2.9f,27),burst(26,125,3.3f,29),burst(24,135,3.7f,30),burst(22,145,4.2f,33)},new Boxes());
    }
    private static Ultimate burst(float cooldown,float radius,float multiplier,float knockback) {return new Ultimate(cooldown,15,.35f,radius,multiplier,4,knockback,.35f,12);}
    /** Flat tuning override retained for focused timing/combat checks; production uses the five-level table. */
    public SkillConfig(Dash dash,Ultimate ultimate) {
        this(new Dash[]{dash,dash,dash,dash,dash},new Ultimate[]{ultimate,ultimate,ultimate,ultimate,ultimate},new Boxes());
    }
    public SkillConfig(Dash[] dash,Ultimate[] ultimate,Boxes boxes) {
        if(dash==null || ultimate==null || boxes==null || dash.length!=MAX_LEVEL || ultimate.length!=MAX_LEVEL)throw new IllegalArgumentException("Five skill levels required");
        dashLevels=dash.clone();ultimateLevels=ultimate.clone();
        for(int i=0;i<MAX_LEVEL;i++)if(dashLevels[i]==null || ultimateLevels[i]==null)throw new IllegalArgumentException("Missing level");
        this.dash=dashLevels[0];this.ultimate=ultimateLevels[0];this.boxes=boxes;
    }
    private static int index(int level) {if(level<1 || level>MAX_LEVEL)throw new IllegalArgumentException("Skill level");return level-1;}
    public Dash dash(int level) {return dashLevels[index(level)];}
    public Ultimate ultimate(int level) {return ultimateLevels[index(level)];}
    private static boolean positive(float value) {return Float.isFinite(value) && value>0;}
}
