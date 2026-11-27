package com.example.monstersurvivor.weapon;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.config.WeaponConfig;
import com.example.monstersurvivor.config.EvolutionConfig;
import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.skill.ClassSkillSystem;
import com.example.monstersurvivor.world.CollisionGrid;

/** Bounded, pooled projectiles and short effects. All damage goes through Enemy.damage. */
public final class ProjectileSystem {
    public com.example.monstersurvivor.audio.AudioSink audio=com.example.monstersurvivor.audio.AudioSink.NONE;
    public static final class Projectile implements Pool.Poolable {
        public WeaponType type;
        public float x,y,dx,dy,speed,age,lifetime,radius,visualScale,glow,travelRemaining;
        public int damage,remainingHits,hitCount;
        public int primaryDirectTargetId,directBonusDamage;
        private final int[] hitIds=new int[8];
        public boolean evolved;
        public int burnDamage;
        private Volley volley;
        @Override public void reset() {type=null;age=0;hitCount=0;evolved=false;burnDamage=0;volley=null;primaryDirectTargetId=directBonusDamage=0;}
    }
    private static final class Volley {
        final int[] ids=new int[10];int count,references;boolean impactSound;
        boolean has(int id) {for(int i=0;i<count;i++)if(ids[i]==id)return true;return false;}
    }
    public static final class FireZone {public float x,y,radius,age;public int damage;public boolean active;}
    private final Volley[] volleys=new Volley[48];
    private final FireZone[] zones=new FireZone[EvolutionConfig.FIRE_ZONES];
    private float burnClock;
    public int burnTicks,burnHits;
    public int zoneCount() {int n=0;for(var z:zones)if(z.active)n++;return n;}
    public FireZone zone(int i) {return zones[i];}
    public enum EffectType { HIT,EXPLOSION,LIGHTNING }
    public static final class Effect implements Pool.Poolable {
        public EffectType type;
        public float x,y,x2,y2,radius,age,lifetime,visualScale,glow,boltWidth;
        @Override public void reset() {type=null;age=0;}
    }
    private final Array<Projectile> projectiles;
    private final Array<Effect> effects;
    private final Pool<Projectile> projectilePool;
    private final Pool<Effect> effectPool;
    private final WeaponConfig config;
    private ClassSkillSystem classSkills;
    public int hits,explosions;
    public ProjectileSystem(WeaponConfig config) {
        this.config=config;projectiles=new Array<>(false,config.projectileCap);effects=new Array<>(false,config.effectCap);
        projectilePool=new Pool<>(config.projectileCap,config.projectileCap) {@Override protected Projectile newObject() {return new Projectile();}};
        effectPool=new Pool<>(config.effectCap,config.effectCap) {@Override protected Effect newObject() {return new Effect();}};
        projectilePool.fill(config.projectileCap);effectPool.fill(config.effectCap);
        for(int i=0;i<volleys.length;i++)volleys[i]=new Volley();
        for(int i=0;i<zones.length;i++)zones[i]=new FireZone();
    }
    public void classSkills(ClassSkillSystem skills) {classSkills=skills;}
    public int size() {return projectiles.size;}
    public int available() {return config.projectileCap-projectiles.size;}
    public Projectile get(int i) {return projectiles.get(i);}
    public int effectCount() {return effects.size;}
    public Effect effect(int i) {return effects.get(i);}
    public boolean shoot(WeaponType type,float x,float y,float tx,float ty,int damage,WeaponConfig.Level level) {
        return shoot(type,x,y,tx,ty,damage,level,0,0);
    }
    /** Optional direct-target bonus is isolated from the projectile's radial splash. */
    public boolean shoot(WeaponType type,float x,float y,float tx,float ty,int damage,WeaponConfig.Level level,int primaryTargetId,int directBonusDamage) {
        if(projectiles.size>=config.projectileCap || (type!=WeaponType.BOW && type!=WeaponType.FIREBALL))return false;
        float dx=tx-x,dy=ty-y,length=(float)Math.sqrt(dx*dx+dy*dy);
        Projectile p=projectilePool.obtain();p.type=type;p.x=x;p.y=y;p.dx=length>.001f?dx/length:1;p.dy=length>.001f?dy/length:0;
        p.damage=damage;p.speed=level.speed;p.lifetime=level.lifetime;p.radius=level.radius;p.remainingHits=level.targets;
        p.primaryDirectTargetId=primaryTargetId;p.directBonusDamage=directBonusDamage;
        p.visualScale=level.visualScale;p.glow=level.glow;
        p.travelRemaining=length;
        projectiles.add(p);return true;
    }
    /** All lanes share a hit ledger: an enemy can take at most one hit per fan activation. */
    public boolean storm(float x,float y,float tx,float ty,int damage,WeaponConfig.Level level) {
        if(available()<level.projectileCount)return false;
        Volley group=null;for(var v:volleys)if(v.references==0){group=v;break;}if(group==null)return false;
        group.count=0;group.impactSound=false;
        float aim=(float)Math.atan2(ty-y,tx-x);
        for(int i=0;i<level.projectileCount;i++) {
            float angle=aim+(i-(level.projectileCount-1)*.5f)*level.fanDegrees*com.badlogic.gdx.math.MathUtils.degreesToRadians;
            shoot(WeaponType.BOW,x,y,x+(float)Math.cos(angle)*level.range,y+(float)Math.sin(angle)*level.range,damage,level);
            var p=projectiles.peek();p.evolved=true;p.volley=group;group.references++;
        }
        effect(EffectType.HIT,x,y,0,0,5,level);return true;
    }
    public boolean meteor(float x,float y,float tx,float ty,int damage,int burnDamage,WeaponConfig.Level level) {
        if(!shoot(WeaponType.FIREBALL,x,y,tx,ty,damage,level))return false;
        var p=projectiles.peek();p.evolved=true;p.burnDamage=burnDamage;return true;
    }
    public void effect(EffectType type,float x,float y,float x2,float y2,float radius) {
        effect(type,x,y,x2,y2,radius,null);
    }
    public void effect(EffectType type,float x,float y,float x2,float y2,float radius,WeaponConfig.Level level) {
        if(effects.size==config.effectCap)effectPool.free(effects.removeIndex(0));
        Effect effect=effectPool.obtain();effect.type=type;effect.x=x;effect.y=y;effect.x2=x2;effect.y2=y2;effect.radius=radius;
        effect.visualScale=level==null?1:level.visualScale;effect.glow=level==null?1:level.glow;effect.boltWidth=level==null?2:level.boltWidth;
        effect.lifetime=type==EffectType.EXPLOSION?.32f:type==EffectType.LIGHTNING?.24f:.14f;effects.add(effect);
    }
    public void clear() {
        projectilePool.freeAll(projectiles);effectPool.freeAll(effects);projectiles.clear();effects.clear();
        for(var v:volleys)v.references=0;for(var z:zones)z.active=false;burnClock=0;
    }
    public void update(float dt,EnemySystem enemies,CollisionGrid grid) {
        updateZones(dt,enemies,grid);
        for(int i=effects.size-1;i>=0;i--) {Effect e=effects.get(i);e.age+=dt;if(e.age>=e.lifetime)effectPool.free(effects.removeIndex(i));}
        for(int i=projectiles.size-1;i>=0;i--) {
            Projectile p=projectiles.get(i);float step=Math.min(dt,Math.max(0,p.lifetime-p.age));
            if(p.type==WeaponType.FIREBALL && p.speed>0)step=Math.min(step,p.travelRemaining/p.speed);
            p.age+=step;p.travelRemaining=Math.max(0,p.travelRemaining-p.speed*step);
            float nx=p.x+p.dx*p.speed*step,ny=p.y+p.dy*p.speed*step;
            float radius=p.type==WeaponType.BOW?.75f:p.evolved?4:2;
            float minX=Math.min(p.x,nx),maxX=Math.max(p.x,nx),minY=Math.min(p.y,ny),maxY=Math.max(p.y,ny);
            float wall=CombatGeometry.wall(grid,p.x,p.y,nx,ny,radius);boolean remove=false;
            // Resolve earliest enemy before the first wall, even if one frame crosses multiple tiles.
            for(int pierce=0;pierce<8 && !remove; pierce++) {
                Enemy target=null;float first=wall;
                search:for(int n=0;n<enemies.size();n++) {
                    Enemy e=enemies.get(n);if(!e.alive())continue;
                    for(int j=0;j<p.hitCount;j++)if(p.hitIds[j]==e.id)continue search;
                    if(p.volley!=null && p.volley.has(e.id))continue;
                    float halfWidth=e.definition.halfWidth+radius,halfHeight=e.definition.halfHeight+radius;
                    if(maxX<e.x-halfWidth || minX>e.x+halfWidth || maxY<e.y-halfHeight || minY>e.y+halfHeight)continue;
                    float t=CombatGeometry.hit(p.x,p.y,nx,ny,e.x,e.y,halfWidth,halfHeight);
                    if(t<=first && (target==null || t<first)) {first=t;target=e;}
                }
                if(target==null)break;
                float hitX=p.x+(nx-p.x)*first,hitY=p.y+(ny-p.y)*first;
                if(p.type==WeaponType.FIREBALL) {explode(p,hitX,hitY,enemies,grid,target.id);remove=true;}
                else {
                    p.hitIds[p.hitCount++]=target.id;
                    int bonus=p.type==WeaponType.BOW && classSkills!=null?classSkills.markBonus(target):0;
                    int amount=p.damage>Integer.MAX_VALUE-bonus?Integer.MAX_VALUE:p.damage+bonus;
                    int before=target.hp();target.damage(amount);boolean accepted=target.hp()<before;
                    if(accepted && bonus>0)classSkills.acceptedMarkedHit(target,bonus);hits++;
                    if(p.volley!=null)p.volley.ids[p.volley.count++]=target.id;
                    if(target.hp()<before && (p.volley==null || !p.volley.impactSound)) {
                        audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_ARROW_HIT);if(p.volley!=null)p.volley.impactSound=true;
                    }
                    effect(EffectType.HIT,hitX,hitY,0,0,4);
                    if(--p.remainingHits<=0)remove=true;
                }
            }
            if(!remove && wall<1) {
                if(p.type==WeaponType.FIREBALL)explode(p,p.x+(nx-p.x)*wall,p.y+(ny-p.y)*wall,enemies,grid,0);
                remove=true;
            }
            // A fan lane can pass beside a small target: detonate at its aimed distance.
            if(!remove && p.type==WeaponType.FIREBALL && (p.travelRemaining<=.001f || p.age>=p.lifetime)) {
                explode(p,nx,ny,enemies,grid,0);remove=true;
            }
            p.x=nx;p.y=ny;
            if(remove || p.age>=p.lifetime || p.x<0 || p.y<0 || p.x>=grid.width*grid.tileSize || p.y>=grid.height*grid.tileSize)
                {if(p.volley!=null)p.volley.references--;projectilePool.free(projectiles.removeIndex(i));}
        }
    }
    private void explode(Projectile p,float x,float y,EnemySystem enemies,CollisionGrid grid,int directContactId) {
        audio.play(com.example.monstersurvivor.audio.AudioEvent.WEAPON_FIRE_EXPLOSION);
        explosions++;effect(EffectType.EXPLOSION,x,y,0,0,p.radius);
        Effect visual=effects.peek();visual.visualScale=p.visualScale;visual.glow=p.glow;
        if(p.evolved) {
            FireZone chosen=zones[0];for(var z:zones)if(!z.active){chosen=z;break;}else if(z.age>chosen.age)chosen=z;
            chosen.x=x;chosen.y=y;chosen.radius=p.radius;chosen.age=0;chosen.damage=p.burnDamage;chosen.active=true;
        }
        // One damage event per resident, with an additional evolved blast target budget.
        int damaged=0;
        for(int i=0;i<enemies.size();i++) {
            if(p.evolved && damaged>=EvolutionConfig.METEOR_TARGETS)break;
            Enemy e=enemies.get(i);if(!e.alive())continue;float dx=e.x-x,dy=e.y-y;
            if(dx*dx+dy*dy<=p.radius*p.radius && grid.clearLine(x,y,e.x,e.y,.5f,.5f,true)) {
                int bonus=!p.evolved && directContactId==p.primaryDirectTargetId && e.id==p.primaryDirectTargetId?p.directBonusDamage:0;
                int amount=p.damage>Integer.MAX_VALUE-bonus?Integer.MAX_VALUE:p.damage+bonus;
                e.damage(amount);hits++;damaged++;
            }
        }
    }
    private void updateZones(float dt,EnemySystem enemies,CollisionGrid grid) {
        for(var z:zones)if(z.active){z.age+=dt;if(z.age>=EvolutionConfig.FIRE_DURATION)z.active=false;}
        burnClock+=dt;if(burnClock<EvolutionConfig.FIRE_TICK)return;
        // No catch-up burst. Session dt is capped at .1s; overlapping zones use strongest damage once.
        burnClock%=EvolutionConfig.FIRE_TICK;boolean any=false;for(var z:zones)any|=z.active;if(!any)return;burnTicks++;
        int damaged=0;for(int i=0;i<enemies.size() && damaged<EvolutionConfig.FIRE_TICK_TARGETS;i++) {
            Enemy e=enemies.get(i);if(!e.alive())continue;int damage=0;
            for(var z:zones)if(z.active) {
                float dx=e.x-z.x,dy=e.y-z.y;
                if(dx*dx+dy*dy<=z.radius*z.radius && grid.clearLine(z.x,z.y,e.x,e.y,.5f,.5f,true))damage=Math.max(damage,z.damage);
            }
            if(damage>0){e.damage(damage);burnHits++;damaged++;}
        }
    }
}
