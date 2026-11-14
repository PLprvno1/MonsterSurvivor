package com.example.monstersurvivor.weapon;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.config.WeaponConfig;
import com.example.monstersurvivor.entity.Enemy;
import com.example.monstersurvivor.world.CollisionGrid;

/** Bounded, pooled projectiles and short effects. All damage goes through Enemy.damage. */
public final class ProjectileSystem {
    public static final class Projectile implements Pool.Poolable {
        public WeaponType type;
        public float x,y,dx,dy,speed,age,lifetime,radius,visualScale,glow,travelRemaining;
        public int damage,remainingHits,hitCount;
        private final int[] hitIds=new int[5];
        @Override public void reset() {type=null;age=0;hitCount=0;}
    }
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
    public int hits,explosions;
    public ProjectileSystem(WeaponConfig config) {
        this.config=config;projectiles=new Array<>(false,config.projectileCap);effects=new Array<>(false,config.effectCap);
        projectilePool=new Pool<>(config.projectileCap,config.projectileCap) {@Override protected Projectile newObject() {return new Projectile();}};
        effectPool=new Pool<>(config.effectCap,config.effectCap) {@Override protected Effect newObject() {return new Effect();}};
        projectilePool.fill(config.projectileCap);effectPool.fill(config.effectCap);
    }
    public int size() {return projectiles.size;}
    public int available() {return config.projectileCap-projectiles.size;}
    public Projectile get(int i) {return projectiles.get(i);}
    public int effectCount() {return effects.size;}
    public Effect effect(int i) {return effects.get(i);}
    public boolean shoot(WeaponType type,float x,float y,float tx,float ty,int damage,WeaponConfig.Level level) {
        if(projectiles.size>=config.projectileCap || (type!=WeaponType.BOW && type!=WeaponType.FIREBALL))return false;
        float dx=tx-x,dy=ty-y,length=(float)Math.sqrt(dx*dx+dy*dy);
        Projectile p=projectilePool.obtain();p.type=type;p.x=x;p.y=y;p.dx=length>.001f?dx/length:1;p.dy=length>.001f?dy/length:0;
        p.damage=damage;p.speed=level.speed;p.lifetime=level.lifetime;p.radius=level.radius;p.remainingHits=level.targets;
        p.visualScale=level.visualScale;p.glow=level.glow;
        p.travelRemaining=length;
        projectiles.add(p);return true;
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
    }
    public void update(float dt,EnemySystem enemies,CollisionGrid grid) {
        for(int i=effects.size-1;i>=0;i--) {Effect e=effects.get(i);e.age+=dt;if(e.age>=e.lifetime)effectPool.free(effects.removeIndex(i));}
        for(int i=projectiles.size-1;i>=0;i--) {
            Projectile p=projectiles.get(i);float step=Math.min(dt,Math.max(0,p.lifetime-p.age));
            if(p.type==WeaponType.FIREBALL && p.speed>0)step=Math.min(step,p.travelRemaining/p.speed);
            p.age+=step;p.travelRemaining=Math.max(0,p.travelRemaining-p.speed*step);
            float nx=p.x+p.dx*p.speed*step,ny=p.y+p.dy*p.speed*step;
            float radius=p.type==WeaponType.BOW?.75f:2;
            float wall=CombatGeometry.wall(grid,p.x,p.y,nx,ny,radius);boolean remove=false;
            // Resolve earliest enemy before the first wall, even if one frame crosses multiple tiles.
            for(int pierce=0;pierce<5 && !remove; pierce++) {
                Enemy target=null;float first=wall;
                search:for(int n=0;n<enemies.size();n++) {
                    Enemy e=enemies.get(n);if(!e.alive())continue;
                    for(int j=0;j<p.hitCount;j++)if(p.hitIds[j]==e.id)continue search;
                    float t=CombatGeometry.hit(p.x,p.y,nx,ny,e.x,e.y,e.definition.halfWidth+radius,e.definition.halfHeight+radius);
                    if(t<=first && (target==null || t<first)) {first=t;target=e;}
                }
                if(target==null)break;
                float hitX=p.x+(nx-p.x)*first,hitY=p.y+(ny-p.y)*first;
                if(p.type==WeaponType.FIREBALL) {explode(p,hitX,hitY,enemies,grid);remove=true;}
                else {
                    p.hitIds[p.hitCount++]=target.id;target.damage(p.damage);hits++;
                    effect(EffectType.HIT,hitX,hitY,0,0,4);
                    if(--p.remainingHits<=0)remove=true;
                }
            }
            if(!remove && wall<1) {
                if(p.type==WeaponType.FIREBALL)explode(p,p.x+(nx-p.x)*wall,p.y+(ny-p.y)*wall,enemies,grid);
                remove=true;
            }
            // A fan lane can pass beside a small target: detonate at its aimed distance.
            if(!remove && p.type==WeaponType.FIREBALL && (p.travelRemaining<=.001f || p.age>=p.lifetime)) {
                explode(p,nx,ny,enemies,grid);remove=true;
            }
            p.x=nx;p.y=ny;
            if(remove || p.age>=p.lifetime || p.x<0 || p.y<0 || p.x>=grid.width*grid.tileSize || p.y>=grid.height*grid.tileSize)
                projectilePool.free(projectiles.removeIndex(i));
        }
    }
    private void explode(Projectile p,float x,float y,EnemySystem enemies,CollisionGrid grid) {
        explosions++;effect(EffectType.EXPLOSION,x,y,0,0,p.radius);
        Effect visual=effects.peek();visual.visualScale=p.visualScale;visual.glow=p.glow;
        // Each resident is visited once for this explosion; there is no persistent damage zone.
        for(int i=0;i<enemies.size();i++) {
            Enemy e=enemies.get(i);if(!e.alive())continue;float dx=e.x-x,dy=e.y-y;
            if(dx*dx+dy*dy<=p.radius*p.radius && grid.clearLine(x,y,e.x,e.y,.5f,.5f,true)) {e.damage(p.damage);hits++;}
        }
    }
}
