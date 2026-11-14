package com.example.monstersurvivor.ai;

import com.badlogic.gdx.utils.Array;
import com.example.monstersurvivor.assets.GameAssets;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.config.Difficulty;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.debug.FrameMetrics;
import static com.example.monstersurvivor.debug.FrameMetrics.Part.*;

/** Bounded navigation work, soft separation, and swept collision for every displacement. */
public final class EnemySystem {
    private final Array<Enemy> enemies=new Array<>(false,60);
    public final NavigationGrid navigation;
    private final AStarPathfinder pathfinder;
    private final GameAssets assets;
    private final GameConfig config;
    private int nextId,rotation;
    public int searchesThisFrame,totalSearches,maxSearchesObserved;
    public FrameMetrics metrics;
    private java.util.function.Consumer<Enemy> deathListener;
    public void onDeath(java.util.function.Consumer<Enemy> listener) {
        deathListener=listener;for(int i=0;i<enemies.size;i++)enemies.get(i).onDeath(listener);
    }
    public EnemySystem(NavigationGrid navigation,GameAssets assets,GameConfig config) {
        this.navigation=navigation;this.assets=assets;this.config=config;pathfinder=new AStarPathfinder(navigation);
    }
    public int size() { return enemies.size; }
    public Enemy get(int index) { return enemies.get(index); }
    public int livingCount() { int n=0;for(int i=0;i<enemies.size;i++)if(enemies.get(i).alive())n++;return n; }
    public Enemy spawn(EnemyType type,float x,float y) {
        return spawn(type,x,y,0);
    }
    public Enemy spawn(EnemyType type,float x,float y,float progress) {
        return spawn(EnemyRegistry.legacy(type),x,y,progress);
    }
    public Enemy spawn(EnemyDefinition type,float x,float y) {return spawn(type,x,y,0);}
    public Enemy spawn(EnemyDefinition type,float x,float y,float progress) {
        if(enemies.size>=config.maxEnemies || enemies.size>=60
            || !navigation.collision.canOccupy(x,y,type.halfWidth,type.halfHeight,type.flying))return null;
        Enemy e=new Enemy(++nextId,type,config.enemy(type,progress),assets==null?null:assets.sprite(type.spriteId),x,y);
        e.onDeath(deathListener);enemies.add(e);return e;
    }
    public void clear() {
        for(int i=0;i<enemies.size;i++) {Enemy e=enemies.get(i);e.path.clear();e.pathIndex=0;e.onDeath(null);}
        enemies.clear();
    }
    public void beginFrame() { searchesThisFrame=0; }
    public void update(float dt,Player player) {
        long animationStart=metrics==null?0:metrics.start();
        for(int i=enemies.size-1;i>=0;i--) {
            Enemy e=enemies.get(i);e.advance(dt);
            if(e.deathFinished())enemies.removeIndex(i);
        }
        if(metrics!=null)metrics.finish(ANIMATION,animationStart);
        if(enemies.size==0)return;
        long aiStart=metrics==null?0:metrics.start();
        long children=metrics==null?0:metrics.value(ASTAR)+metrics.value(SEPARATION)+metrics.value(COLLISION);
        long separationStart=metrics==null?0:metrics.start();
        separate();
        if(metrics!=null)metrics.finish(SEPARATION,separationStart);
        int first=rotation++%enemies.size;
        for(int n=0;n<enemies.size;n++) {
            Enemy e=enemies.get((first+n)%enemies.size);
            if(!e.alive() || e.acting())continue;
            steer(e,player,dt);
        }
        maxSearchesObserved=Math.max(maxSearchesObserved,searchesThisFrame);
        if(aiStart!=0)metrics.add(AI,System.nanoTime()-aiStart-(metrics.value(ASTAR)+metrics.value(SEPARATION)+metrics.value(COLLISION)-children));
    }
    private void separate() {
        for(int i=0;i<enemies.size;i++) {Enemy e=enemies.get(i);e.separationX=e.separationY=0;}
        for(int i=0;i<enemies.size;i++) {
            Enemy a=enemies.get(i);if(!a.alive())continue;
            for(int j=i+1;j<enemies.size;j++) {
                if(metrics!=null && metrics.active)metrics.separationPairs++;
                Enemy b=enemies.get(j);if(!b.alive())continue;
                float dx=a.x-b.x,dy=a.y-b.y,d2=dx*dx+dy*dy;
                if(d2>=24*24)continue;
                if(d2<.001f) {dx=((a.id+b.id)&1)==0?1:-1;dy=0;d2=1;}
                float distance=(float)Math.sqrt(d2),weight=(24-distance)/24;
                dx=dx/distance*weight;dy=dy/distance*weight;
                a.separationX+=dx;a.separationY+=dy;b.separationX-=dx;b.separationY-=dy;
            }
        }
    }
    private void steer(Enemy e,Player player,float dt) {
        float dx=player.x-e.x,dy=player.y-e.y,distance2=dx*dx+dy*dy;
        int target=navigation.cell(player.x,player.y);
        if(e.lineTimer<=0) {
            e.lineTimer=.22f+(e.id%5)*.02f;
            e.direct=navigation.collision.clearLine(e.x,e.y,player.x,player.y,e.definition.halfWidth,e.definition.halfHeight,e.definition.flying);
            if(e.direct) {e.path.clear();e.pathIndex=0;}
        }
        float tx=player.x,ty=player.y;
        if(!e.direct) {
            int changed=e.targetCell<0?100:Math.abs(target%navigation.collision.width-e.targetCell%navigation.collision.width)
                +Math.abs(target/navigation.collision.width-e.targetCell/navigation.collision.width);
            if((e.pathTimer<=0 || changed>=2) && searchesThisFrame<config.pathSearchesPerFrame) {
                searchesThisFrame++;totalSearches++;
                int start=navigation.cell(e.x,e.y);
                long searchStart=metrics==null?0:metrics.start();
                boolean found=pathfinder.find(start,target,e.definition.flying,e.path);
                if(metrics!=null)metrics.finish(ASTAR,searchStart);
                e.targetCell=target;e.pathIndex=0;
                // Recenter only if needed for a tight corner; periodic refresh must not pull
                // an actor backwards to the middle of its current tile.
                if(found && e.path.size>0 && !navigation.collision.clearLine(e.x,e.y,
                    navigation.x(e.path.first()),navigation.y(e.path.first()),e.definition.halfWidth,e.definition.halfHeight,e.definition.flying))e.path.insert(0,start);
                e.pathTimer=found?.8f+(e.id%5)*.08f:1.4f;
            }
            while(e.pathIndex<e.path.size) {
                int cell=e.path.get(e.pathIndex);
                tx=navigation.x(cell);ty=navigation.y(cell);
                if(Math.abs(e.x-tx)+Math.abs(e.y-ty)>2)break;
                e.pathIndex++;
            }
            if(e.pathIndex>=e.path.size) { e.locomotion(false,0,0);return; }
        }
        dx=tx-e.x;dy=ty-e.y;
        float length=(float)Math.sqrt(dx*dx+dy*dy);
        float speed=e.stats.speed*config.enemySpeedMultiplier;
        if(e.definition.charge && e.direct && distance2>45*45 && distance2<150*150 && e.chargeCooldown<=0) {
            e.chargeTime=.7f;e.chargeCooldown=3.5f;
        }
        if(e.chargeTime>0 && e.direct)speed*=1.65f;
        float vx=length>.01f?dx/length*speed:0,vy=length>.01f?dy/length*speed:0;
        if(e.direct && distance2<e.definition.contactRadius()*e.definition.contactRadius())vx=vy=0;
        vx+=e.separationX*20;vy+=e.separationY*20;
        float v2=vx*vx+vy*vy;
        if(v2>speed*speed) {float factor=speed/(float)Math.sqrt(v2);vx*=factor;vy*=factor;}
        if(!e.direct && length<speed*dt && length>.001f) {vx=dx/dt;vy=dy/dt;}
        float oldX=e.x,oldY=e.y;
        move(e,vx*dt,vy*dt);
        boolean moved=Math.abs(e.x-oldX)+Math.abs(e.y-oldY)>.005f;
        e.locomotion(moved,e.x-oldX,e.y-oldY);
        // Invalidate stalled paths with a bounded backoff; don't alternate directions every frame.
        e.stuckTime=moved?0:e.stuckTime+dt;
        if(e.stuckTime>1 && distance2>16*16) {
            e.pathTimer=0;e.direct=false;e.lineTimer=1.2f;e.path.clear();e.pathIndex=0;e.stuckTime=0;
        }
    }
    private void move(Enemy e,float dx,float dy) {
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/Player.MAX_STEP));
        dx/=steps;dy/=steps;
        for(int i=0;i<steps;i++) {
            if(navigation.collision.canOccupy(e.x+dx,e.y,e.definition.halfWidth,e.definition.halfHeight,e.definition.flying))e.x+=dx;
            if(navigation.collision.canOccupy(e.x,e.y+dy,e.definition.halfWidth,e.definition.halfHeight,e.definition.flying))e.y+=dy;
        }
    }
}
