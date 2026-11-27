import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.gameplay.GameSession;
import com.example.monstersurvivor.world.ForestGenerator;
import com.example.monstersurvivor.enemy.*;
import java.nio.file.*;
import java.util.*;

/** Finite deterministic balance observations, not a player playtest or FPS benchmark. */
public final class BalanceProbe {
    public static void main(String[] args)throws Exception {
        StringBuilder json=new StringBuilder("{\"stats\":[");boolean comma=false;
        for(var difficulty:Difficulty.values()) {
            var c=new GameConfig.Builder().difficulty(difficulty).build();
            for(int p:new int[]{0,60,120,210,300,420,450,540,599}) {
                if(comma)json.append(',');comma=true;
                var slime=c.enemy(EnemyRegistry.get("tiny_slime"),p);var tank=c.enemy(EnemyRegistry.get("mc_moss_golem"),p);
                json.append(String.format(Locale.ROOT,"{\"difficulty\":\"%s\",\"progress\":%d,\"target\":%d,\"interval\":%.3f,\"slimeHp\":%d,\"tankHp\":%d,\"tankDamage\":%d}",difficulty,p,c.targetPopulation(p),c.spawnInterval(p),slime.hp,tank.hp,tank.damage));
            }
        }
        json.append("],\"stationary\":[");comma=false;
        for(long seed:new long[]{42,43})for(var difficulty:Difficulty.values()) {
            var c=new GameConfig.Builder().difficulty(difficulty).fixedSeed(seed).build();
            var layout=ForestGenerator.generate(seed);float sx=(ForestGenerator.CENTER_X+.5f)*ForestGenerator.TILE_SIZE,sy=(ForestGenerator.CENTER_Y+.5f)*ForestGenerator.TILE_SIZE;
            var s=new GameSession(c,layout.grid,sx,sy,null,null,seed);
            double population=0;int frames=0,max=0;float firstDamage=-1;
            for(int n=0;n<12000 && s.player.alive();n++) {
                s.update(.05f,0,0,false,sx-139,sy-104,sx+139,sy+104);
                if(firstDamage<0 && s.player.hp()<10)firstDamage=s.elapsed();
                population+=s.enemies.livingCount();frames++;max=Math.max(max,s.enemies.livingCount());
                if(s.choosingWeapon())throw new AssertionError("Stationary probe unexpectedly collected modal");
            }
            if(comma)json.append(',');comma=true;
            json.append(String.format(Locale.ROOT,"{\"seed\":%d,\"difficulty\":\"%s\",\"seconds\":%.2f,\"hp\":%d,\"firstDamageSeconds\":%.2f,\"meanLiving\":%.3f,\"maxLiving\":%d,\"kills\":%d,\"spawned\":%d,\"failedSpawnWindows\":%d,\"collected\":%d}",seed,difficulty,s.elapsed(),s.player.hp(),firstDamage,population/frames,max,s.kills,s.spawner.successfulSpawns,s.spawner.failedAttempts,s.pickups.collectedItems));s.end();
        }
        json.append("],\"heldHpPopulation\":[");comma=false;
        for(long seed:new long[]{42,43})for(var difficulty:Difficulty.values()) {
            var c=new GameConfig.Builder().difficulty(difficulty).fixedSeed(seed).playerHp(100000).build();
            var layout=ForestGenerator.generate(seed);float sx=(ForestGenerator.CENTER_X+.5f)*ForestGenerator.TILE_SIZE,sy=(ForestGenerator.CENTER_Y+.5f)*ForestGenerator.TILE_SIZE;
            var s=new GameSession(c,layout.grid,sx,sy,null,null,seed);
            int[] starts={0,60,120,300,420,540},ends={60,120,300,420,540,600};double[] sum=new double[6];int[] count=new int[6];int stage=0;
            for(int frame=0;frame<6000;frame++) {
                s.update(.1f,0,0,false,sx-139,sy-104,sx+139,sy+104);while(stage<5 && s.progress()>=ends[stage])stage++;
                sum[stage]+=s.enemies.livingCount();count[stage]++;
            }
            for(int i=0;i<6;i++) {
                if(comma)json.append(',');comma=true;
                json.append(String.format(Locale.ROOT,"{\"seed\":%d,\"difficulty\":\"%s\",\"from\":%d,\"to\":%d,\"meanLiving\":%.3f}",seed,difficulty,starts[i],ends[i],sum[i]/count[i]));
            }
            s.end();
        }
        json.append("]}");String label=System.getProperty("ms.balance.label","after");
        Path dest=Path.of("../core/build/reports/task04a_1/balance-"+label+".json");Files.writeString(dest,json);System.out.println(json);
    }
}
