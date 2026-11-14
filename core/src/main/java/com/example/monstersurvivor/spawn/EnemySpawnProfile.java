package com.example.monstersurvivor.spawn;

import com.badlogic.gdx.math.RandomXS128;
import com.example.monstersurvivor.ai.EnemySystem;
import com.example.monstersurvivor.config.Difficulty;
import com.example.monstersurvivor.config.GameConfig;
import com.example.monstersurvivor.enemy.*;
import java.util.Arrays;

/** Per-match weighted roster. Only sampled at spawn boundaries, never per resident/frame. */
public final class EnemySpawnProfile {
    public static final float NIGHT_PROGRESS=300,BOSS_PROGRESS=540,END_PROGRESS=600;
    public static final String FUTURE_BOSS_ID="lich_lord";
    private static final String[] THEMES={"Forest","Cave","Swamp","Coast","Night"};
    private final RandomXS128 random;
    private final Difficulty difficulty;
    private final GameConfig config;
    private final int familyCap;
    private final float[] matchWeight,eligible;
    private final int[] counts,families;
    private int previous=-1,streak;
    private float nextTheme,themeEnds;
    private String theme="";
    public EnemySpawnProfile(long seed,Difficulty difficulty,int residentCap) {
        this(seed,new GameConfig.Builder().difficulty(difficulty).population(0,residentCap).build());
    }
    public EnemySpawnProfile(long seed,GameConfig config) {
        this.config=config;this.difficulty=config.difficulty;random=new RandomXS128(seed^0x4A51C8L);familyCap=Math.max(3,(int)Math.ceil(config.maxEnemies*.25f));
        int size=EnemyRegistry.all().size();matchWeight=new float[size];eligible=new float[size];counts=new int[size];families=new int[EnemyRegistry.FAMILY_COUNT];
        for(EnemyDefinition d:EnemyRegistry.all())matchWeight[d.index]=.8f+random.nextFloat()*.45f;
        nextTheme=NIGHT_PROGRESS+25+random.nextFloat()*20;
    }
    /** Base eligibility/weight is also inspectable by finite gameplay tests and diagnostics. */
    public float weight(EnemyDefinition d,float progress) {
        if(progress<d.unlock || progress>=END_PROGRESS)return 0;
        float result=d.weight*matchWeight[d.index]*config.spawnWeightMultiplier(d,progress);
        if(difficulty==Difficulty.HARD)result*=d.hardWeight;
        if(progress>=NIGHT_PROGRESS)result*=d.nightWeight;
        if(progress<themeEnds && d.biome.equals(theme))result*=1.65f;
        return result;
    }
    public String theme(float progress) {return progress<themeEnds?theme:"";}
    public EnemyDefinition pick(float progress,EnemySystem residents) {
        if(progress>=END_PROGRESS)return null;
        if(progress>=nextTheme) {
            theme=THEMES[random.nextInt(THEMES.length)];themeEnds=progress+18;nextTheme=progress+60+random.nextFloat()*30;
        }
        Arrays.fill(counts,0);Arrays.fill(families,0);
        for(int i=0;i<residents.size();i++) {var d=residents.get(i).definition;counts[d.index]++;families[d.familyIndex]++;}
        float total=0;
        for(EnemyDefinition d:EnemyRegistry.all()) {
            float w=counts[d.index]>=d.residentLimit || families[d.familyIndex]>=familyCap?0:weight(d,progress);
            if(d.index==previous && streak>=2)w*=.2f;
            eligible[d.index]=w;total+=w;
        }
        if(total<=0)return null;
        float selected=random.nextFloat()*total;EnemyDefinition result=null;
        for(EnemyDefinition d:EnemyRegistry.all())if(eligible[d.index]>0) {
            result=d;selected-=eligible[d.index];if(selected<0)break;
        }
        if(result.index==previous)streak++;else {previous=result.index;streak=1;}
        return result;
    }
}
