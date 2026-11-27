import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.combat.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.*;
/** Paired stationary-target probe; not survival playtesting or a promise of effective DPS in a moving crowd. */
public class EvolutionBalance {
 public static void main(String[] args) {
  System.out.println("weapon,base,residents,evolved,totalDamage,dps,targetsHit,activations,peakProjectiles,averageDirectHitsPerActivation,averageBurnTargetsPerTick");
  for(var type:WeaponType.values())for(int base:new int[]{1,3,6})for(int residents:new int[]{10,30,60})for(boolean evolved:new boolean[]{false,true}) {
   var c=new GameConfig.Builder().population(0,60).enemy(EnemyType.SLIME,1000000,1,.01f).build();var grid=new CollisionGrid(100,100,16,new byte[10000]);
   var enemies=new EnemySystem(new NavigationGrid(grid),null,c);var p=new Player(600,600,null,c);p.stats.addDamage(base-1);
   for(int i=0;i<residents;i++){float angle=i*6.2831853f/residents,r=40+(i%3)*32;enemies.spawn(EnemyType.SLIME,600+(float)Math.cos(angle)*r,600+(float)Math.sin(angle)*r);}
   var l=new WeaponLoadout(c.weapons);while(l.canGrant(type))l.grant(type);if(evolved)WeaponOffer.evolution(type,c.strings).apply(l,p);
   var combat=new SwordCombat(c,l.get(WeaponType.SWORD));var w=new WeaponSystem(c,l,combat);int peak=0;
   // Only the selected weapon is measured. Ordinary Sword would otherwise also attack for ranged fixtures.
   for(int frame=0;frame<400;frame++) {
    if(type==WeaponType.SWORD)p.update(.05f,0,0,grid);for(int i=0;i<enemies.size();i++)enemies.get(i).advance(.05f);
    w.update(.05f,p,enemies,grid);peak=Math.max(peak,w.projectiles.size());
   }
   int damage=0,touched=0;for(int i=0;i<enemies.size();i++){var e=enemies.get(i);int n=e.stats.hp-e.hp();damage+=n;if(n>0)touched++;}
   int directHits=type==WeaponType.SWORD?damage/l.get(type).tuning().damage(base):type==WeaponType.LIGHTNING?w.lightningHits:w.projectiles.hits;
   float average=l.get(type).activations==0?0:directHits/(float)l.get(type).activations,burnAverage=w.projectiles.burnTicks==0?0:w.projectiles.burnHits/(float)w.projectiles.burnTicks;
   System.out.println(type+","+base+","+residents+","+evolved+","+damage+","+(damage/20f)+","+touched+","+l.get(type).activations+","+peak+","+average+","+burnAverage);
  }
 }
}
