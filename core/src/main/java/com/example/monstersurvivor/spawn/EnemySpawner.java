package com.example.monstersurvivor.spawn;

import com.badlogic.gdx.math.RandomXS128;
import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.config.GameMode;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.enemy.*;

/** Camera-relative ring, finite attempts, and connectivity checked before accepting a spawn. */
public final class EnemySpawner {
    private final GameConfig config;
    private final EnemySystem enemies;
    private final RandomXS128 random;
    private float cooldown;
    private int fallbackCell;
    public final EnemySpawnProfile profile;
    private final int[] spawnedByType=new int[EnemyRegistry.all().size()];
    public int successfulSpawns,failedAttempts;
    public EnemySpawner(GameConfig config,EnemySystem enemies,long seed) {
        this.config=config;this.enemies=enemies;random=new RandomXS128(seed);profile=new EnemySpawnProfile(seed,config);
    }
    public int spawned(EnemyDefinition definition) {return spawnedByType[definition.index];}
    public void update(float dt,float progress,Player player,float left,float bottom,float right,float top) {
        if(config.mode!=GameMode.ENDLESS && progress>=EnemySpawnProfile.END_PROGRESS)return;
        cooldown-=dt;
        if(cooldown>0 || enemies.normalSize()>=config.targetPopulation(progress))return;
        cooldown=config.spawnInterval(progress);
        EnemyDefinition type=profile.pick(progress,enemies);if(type==null)return;
        NavigationGrid nav=enemies.navigation;
        int target=nav.cell(player.x,player.y);
        for(int attempt=0;attempt<64;attempt++) {
            float x=left-192+random.nextFloat()*(right-left+384);
            float y=bottom-192+random.nextFloat()*(top-bottom+384);
            int cell=nav.cell(x,y);
            if(accept(type,cell,target,player,left,bottom,right,top,progress))return;
        }
        // Inspect only the same local rectangle so fallback placement stays camera-relative on either map fixture.
        int tile=nav.collision.tileSize;
        int minX=Math.max(0,(int)Math.floor((left-192)/tile)),maxX=Math.min(nav.collision.width-1,(int)Math.floor((right+192)/tile));
        int minY=Math.max(0,(int)Math.floor((bottom-192)/tile)),maxY=Math.min(nav.collision.height-1,(int)Math.floor((top+192)/tile));
        int columns=maxX-minX+1,rows=maxY-minY+1,count=columns*rows;
        if(columns<=0 || rows<=0)return;
        for(int i=0;i<256;i++) {
            int offset=fallbackCell%count;fallbackCell=(offset+1)%count;
            int cell=(minY+offset/columns)*nav.collision.width+minX+offset%columns;
            if(accept(type,cell,target,player,left,bottom,right,top,progress))return;
        }
        failedAttempts++;
    }
    private boolean accept(EnemyDefinition type,int cell,int target,Player player,float left,float bottom,float right,float top,float progress) {
        NavigationGrid nav=enemies.navigation;
        if(!nav.connected(cell,target,type.flying))return false;
        float x=nav.x(cell),y=nav.y(cell);
        if(x<left-192 || x>right+192 || y<bottom-192 || y>top+192)return false;
        if(x>=left-80 && x<=right+80 && y>=bottom-80 && y<=top+80)return false;
        float dx=x-player.x,dy=y-player.y;
        if(dx*dx+dy*dy<96*96)return false;
        for(int i=0;i<enemies.size();i++) {
            Enemy e=enemies.get(i);dx=x-e.x;dy=y-e.y;
            if(dx*dx+dy*dy<24*24)return false;
        }
        if(enemies.spawn(type,x,y,progress)==null)return false;
        successfulSpawns++;spawnedByType[type.index]++;return true;
    }
}
