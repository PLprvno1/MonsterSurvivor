package com.example.monstersurvivor.config;

import com.example.monstersurvivor.weapon.WeaponType;

/** Immutable level tables. Damage = round(current player damage * multiplier + bonus), at least 1. */
public final class WeaponConfig {
    public static final int SLOTS=3,MAX_LEVEL=5;
    public static final class Level {
        public final float multiplier,cooldown,range,speed,lifetime,radius,chainRange;
        public final int bonus,targets,projectileCount,initialBolts;
        public final float visualScale,glow,boltWidth,fanDegrees;
        public Level(float multiplier,int bonus,float cooldown,float range,float speed,float lifetime,float radius,int targets,float chainRange) {
            this(multiplier,bonus,cooldown,range,speed,lifetime,radius,targets,chainRange,1,1,1,1,2,12);
        }
        public Level(float multiplier,int bonus,float cooldown,float range,float speed,float lifetime,float radius,int targets,float chainRange,
                     int projectileCount,int initialBolts,float visualScale,float glow,float boltWidth,float fanDegrees) {
            if(!Float.isFinite(multiplier) || multiplier<=0 || bonus<0 || !Float.isFinite(cooldown) || cooldown<=0
                || !Float.isFinite(range) || range<=0 || !Float.isFinite(speed) || speed<0 || !Float.isFinite(lifetime) || lifetime<0
                || !Float.isFinite(radius) || radius<0 || targets<1 || targets>5 || !Float.isFinite(chainRange) || chainRange<0)
                throw new IllegalArgumentException("Weapon level tuning");
            if(projectileCount<1 || projectileCount>3 || initialBolts<1 || initialBolts>2 || initialBolts>targets
                || !Float.isFinite(visualScale) || visualScale<=0 || !Float.isFinite(glow) || glow<0
                || !Float.isFinite(boltWidth) || boltWidth<=0 || !Float.isFinite(fanDegrees) || fanDegrees<0 || fanDegrees>45)
                throw new IllegalArgumentException("Weapon presentation tuning");
            this.multiplier=multiplier;this.bonus=bonus;this.cooldown=cooldown;this.range=range;this.speed=speed;
            this.lifetime=lifetime;this.radius=radius;this.targets=targets;this.chainRange=chainRange;
            this.projectileCount=projectileCount;this.initialBolts=initialBolts;this.visualScale=visualScale;
            this.glow=glow;this.boltWidth=boltWidth;this.fanDegrees=fanDegrees;
        }
        public int damage(int base) {return (int)Math.min(Integer.MAX_VALUE,Math.max(1,Math.round((double)base*multiplier+bonus)));}
    }
    private final Level[][] levels;
    public final int projectileCap=48,effectCap=48;
    public final float retrySeconds=.12f;
    public WeaponConfig() {
        levels=new Level[4][MAX_LEVEL];
        for(int i=0;i<MAX_LEVEL;i++) {
            levels[0][i]=new Level(1+i*.0625f,(i+1)/2,1-i*.055f,48+(i/2)*4,0,0,0,1,0);
            levels[1][i]=new Level(1+i*.1f,(i+1)/2,1.4f-i*.075f,220+i*8,250+i*20,1.4f,0,Math.min(3,1+i/2),0);
        }
        float[] fireDamage={1.5f,1.5f,2.5f,2.75f,3.5f},fireCd={3,3,3,3,2.1f};
        float[] lightningDamage={1,1,2,2.5f,3.5f},lightningCd={4,3.4f,3.4f,2.7f,2.7f};
        for(int i=0;i<MAX_LEVEL;i++) {
            levels[2][i]=new Level(fireDamage[i],i>=2?1:0,fireCd[i],180+i*8,150+i*10,2,35+i*3,1,0,
                i==0?1:i<3?2:3,1,1+i*.12f,1+i*.25f,2,12);
            levels[3][i]=new Level(lightningDamage[i],i>=2?1:0,lightningCd[i],170+i*8,0,0,0,2+i/2,70+i*5,
                1,i==4?2:1,1+i*.12f,1+i*.25f,i<2?2:2.5f,0);
        }
    }
    private WeaponConfig(Level[][] source) {levels=source;}
    public Level level(WeaponType type,int level) {
        if(level<1 || level>MAX_LEVEL)throw new IllegalArgumentException("Weapon level 1..5");return levels[type.ordinal()][level-1];
    }
    public WeaponConfig withLevel(WeaponType type,int level,Level value) {
        if(value==null)throw new IllegalArgumentException("Weapon level required");level(type,level);
        Level[][] copy=levels.clone();copy[type.ordinal()]=copy[type.ordinal()].clone();copy[type.ordinal()][level-1]=value;
        return new WeaponConfig(copy);
    }
}
