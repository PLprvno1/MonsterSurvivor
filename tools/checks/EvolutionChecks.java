import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.combat.SwordCombat;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.skill.SkillType;
import com.example.monstersurvivor.weapon.*;
import com.example.monstersurvivor.world.CollisionGrid;

/** Progression uses real pickup/modal/commit; combat probes exercise authoritative damage and terrain. */
public final class EvolutionChecks {
    static int checks;
    static void check(boolean v,String m){if(!v)throw new AssertionError(m);checks++;}
    static CollisionGrid open(){return new CollisionGrid(100,100,16,new byte[10000]);}
    static CollisionGrid wall(){byte[] masks=new byte[10000];for(int y=0;y<100;y++)masks[y*100+38]=CollisionGrid.TREE;return new CollisionGrid(100,100,16,masks);}
    static GameConfig cfg(Difficulty d,GameMode m){return new GameConfig.Builder().difficulty(d).mode(m).playerHp(100000).population(0,d==Difficulty.HARD?60:40).enemy(EnemyType.SLIME,100,1,.01f).stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();}
    static GameSession session(){return new GameSession(cfg(Difficulty.EASY,GameMode.NORMAL),open(),600,600,null,null,42);}
    static void modal(GameSession s){
        check(s.pickups.spawn(PickupType.WEAPON_BOX,s.player.x,s.player.y,1)!=null,"real box spawn");
        s.update(.001f,0,0,false,450,450,750,750);check(s.choosingWeapon(),"real collision opens weapon modal");
        check(s.selection.token()>0 && s.pickups.contains(s.selection.pendingId()),"positive token and unconsumed pending box");
    }
    static int offer(GameSession s,WeaponType t){for(int i=0;i<s.selection.size();i++)if(s.selection.get(i).weapon==t)return i;return -1;}
    static void earn(GameSession s,WeaponType t){
        if(t==WeaponType.SWORD && s.weapons.loadout.size()==1){modal(s);check(s.chooseWeapon(offer(s,WeaponType.BOW),s.selection.token()),"first unlock via box");}
        for(int attempts=0;attempts<40 && (s.weapons.loadout.get(t)==null || s.weapons.loadout.get(t).level()<5);attempts++){
            modal(s);int at=offer(s,t);if(at<0)at=0;check(s.chooseWeapon(at,s.selection.token()),"progression commits one reward");
        }
        check(s.weapons.loadout.get(t)!=null && s.weapons.loadout.get(t).level()==5,"Lv5 reached from boxes "+t);
        check(!s.weapons.loadout.get(t).evolved(),"Lv5 never automatically evolves "+t);
        modal(s);int at=offer(s,t);check(at>=0 && s.selection.get(at).kind==WeaponOffer.Kind.EVOLUTION,"next box guarantees eligible evolution "+t);
        float time=s.elapsed();for(int i=0;i<5;i++)s.update(.1f,1,0,false,450,450,750,750);
        check(s.elapsed()==time && !s.activateSkill(SkillType.DASH,1,0,false) && !s.activateSkill(SkillType.ULTIMATE,0,0,false),"evolution modal freezes time and skills");
        long token=s.selection.token();int size=s.weapons.loadout.size();
        check(!s.chooseWeapon(at,-Math.abs(token)) && !s.chooseWeapon(at,token+1),"cross kind/stale tokens rejected");
        check(s.chooseWeapon(at,token) && !s.chooseWeapon(at,token),"one evolution commit per item");
        check(s.weapons.loadout.get(t).evolved() && s.weapons.loadout.get(t).level()==5 && s.weapons.loadout.size()==size,"evolved retains level5 and slot");
    }
    static void progression(){
        for(var lang:GameLanguage.values())for(var type:WeaponType.values()){
            var s=new GameSession(new GameConfig.Builder().language(lang).population(0,40).build(),open(),600,600,null,null,42);
            check(!WeaponOffer.evolution(type,s.config.strings).apply(s.weapons.loadout,s.player),"no evolution before eligible");
            earn(s,type);check(!WeaponOffer.evolution(type,s.config.strings).apply(s.weapons.loadout,s.player),"no second evolution");
            check(!s.weapons.loadout.grant(type),"no Lv6");modal(s);
            for(int i=0;i<s.selection.size();i++)check(s.selection.get(i).weapon!=type,"evolved excluded from offers");
            long token=s.selection.token();s.end();var retry=session();modal(retry);check(!retry.chooseWeapon(0,token),"cross session token rejected");
            check(retry.weapons.loadout.describe().equals("SWORD:1"),"Retry baseline no evolution");retry.end();
        }
        var s=session();for(var t:new WeaponType[]{WeaponType.SWORD,WeaponType.BOW,WeaponType.FIREBALL})while(s.weapons.loadout.canGrant(t))s.weapons.loadout.grant(t);
        for(int i=0;i<3;i++){modal(s);check(s.selection.get(0).kind==WeaponOffer.Kind.EVOLUTION,"evolution priority");check(s.chooseWeapon(0,s.selection.token()),"all three evolve separately");}
        check(s.weapons.loadout.size()==3 && !s.weapons.loadout.canGrant(WeaponType.LIGHTNING),"never fourth slot");modal(s);
        for(int i=0;i<s.selection.size();i++)check(s.selection.get(i).weapon==null,"useful fallback after all evolved");int damage=s.player.stats.damage();check(s.chooseWeapon(0,s.selection.token()) && s.player.stats.damage()==damage+2,"fallback consumes with benefit");
        check(s.pickups.spawn(PickupType.SKILL_BOX,600,600,1)!=null,"skill box still works");s.update(.001f,0,0,false,450,450,750,750);
        String loadout=s.weapons.loadout.describe();check(s.choosingSkill() && !s.chooseWeapon(0,s.skillSelection.token()),"skill modal cannot commit evolution");
        check(s.chooseSkill(0,s.skillSelection.token()) && loadout.equals(s.weapons.loadout.describe()),"skill reward leaves weapons unchanged");s.end();
    }
    static WeaponSystem system(GameConfig c,WeaponType type,Player p){
        var l=new WeaponLoadout(c.weapons);while(l.canGrant(type))l.grant(type);
        check(WeaponOffer.evolution(type,c.strings).apply(l,p),"combat fixture explicit Lv5 reward");
        return new WeaponSystem(c,l,new SwordCombat(c,l.get(WeaponType.SWORD)));
    }
    static EnemySystem enemies(GameConfig c,CollisionGrid g){return new EnemySystem(new NavigationGrid(g),null,c);}
    static void orbit(){
        for(float dt:new float[]{.016f,.05f,.1f}){
            var c=cfg(Difficulty.EASY,GameMode.NORMAL);var g=open();var p=new Player(600,600,null,c);var es=enemies(c,g);var e=es.spawn(EnemyType.SLIME,638,600);var w=system(c,WeaponType.SWORD,p);
            int hp=e.hp();w.update(dt,p,es,g);check(e.hp()<hp && w.orbitHits==1,"orbit real blade hit");hp=e.hp();
            for(float t=0;t<.45f;t+=dt)w.update(dt,p,es,g);
            check(e.hp()==hp,"per target cooldown prevents frame damage");
            for(float t=0;t<3;t+=dt)w.update(dt,p,es,g);
            check(w.orbitHits>=3 && w.orbitHits<=7,"bounded hits across frame rates");check(!p.windingUp(),"old sword not stacked");
            float old=w.orbitX();p.x+=5;w.update(dt,p,es,g);check(w.orbitX()==old+5,"orbit follows player");
        }
        var c=cfg(Difficulty.EASY,GameMode.NORMAL);var g=wall();var p=new Player(600,600,null,c);var es=enemies(c,g);var e=es.spawn(EnemyType.SLIME,638,600);var w=system(c,WeaponType.SWORD,p);int hp=e.hp();for(int i=0;i<60;i++)w.update(.05f,p,es,g);check(e.hp()==hp,"orbit cannot hit through wall");
    }
    static void arrows(){
        var c=cfg(Difficulty.EASY,GameMode.NORMAL);var g=open();var es=enemies(c,g);var p=new ProjectileSystem(c.weapons);var level=EvolutionConfig.level(WeaponType.BOW);
        var a=es.spawn(EnemyType.SLIME,650,600);var b=es.spawn(EnemyType.SLIME,710,600);var beyond=es.spawn(EnemyType.SLIME,770,600);
        check(p.storm(600,600,900,600,3,level) && p.size()==5,"five fan lanes");float dx=p.get(2).dx,dy=p.get(2).dy;a.y=602;
        p.update(.1f,es,g);check(p.get(2).dx==dx && p.get(2).dy==dy,"moving target cannot bend direction");
        for(int i=0;i<13;i++)p.update(.1f,es,g);
        check(a.hp()==97 && b.hp()==97 && beyond.hp()==100,"two pierces, shared dedup, third unharmed");check(p.size()==0,"fan lifetime bounded");
        for(int i=0;i<9;i++)check(p.storm(600,600,900,600,3,level),"bounded fan admission");check(!p.storm(600,600,900,600,3,level) && p.size()==45,"whole fan rejected when pool cannot fit");p.clear();
        es=enemies(c,wall());a=es.spawn(EnemyType.SLIME,650,600);p.storm(600,600,900,600,3,level);for(int i=0;i<15;i++)p.update(.1f,es,wall());check(a.hp()==100,"fan blocked by hard wall");
        p.clear();es=enemies(c,g);for(int i=0;i<8;i++)es.spawn(EnemyType.SLIME,650+i*16,600);
        int before=p.hits;var max=new WeaponConfig.Level(1,0,1,400,2000,1,0,8,0);
        p.shoot(WeaponType.BOW,600,600,1000,600,1,max);p.update(.1f,es,g);check(p.hits-before==8 && p.size()==0,"expanded config target ceiling fits swept hit ledger");
    }
    static void meteor(){
        var c=cfg(Difficulty.EASY,GameMode.NORMAL);var g=open();var es=enemies(c,g);var p=new ProjectileSystem(c.weapons);var level=EvolutionConfig.level(WeaponType.FIREBALL);
        var a=es.spawn(EnemyType.SLIME,650,600);var b=es.spawn(EnemyType.SLIME,672,625);var outside=es.spawn(EnemyType.SLIME,740,600);int[] deaths={0};es.onDeath(e->deaths[0]++);
        p.meteor(600,600,650,600,3,2,level);for(int i=0;i<4;i++)p.update(.1f,es,g);
        check(p.explosions==1 && a.hp()==97 && b.hp()==97 && outside.hp()==100,"one AoE per target inside radius");check(p.zoneCount()==1,"impact leaves fire zone");
        int hp=a.hp();p.update(.01f,es,g);check(a.hp()==hp,"no every frame burn");for(int i=0;i<5;i++)p.update(.1f,es,g);check(a.hp()<hp && outside.hp()==100,"burn ticks only inside zone");
        for(int i=0;i<30;i++)p.update(.1f,es,g);check(p.zoneCount()==0 && p.burnTicks<=5 && deaths[0]<=2,"finite ground duration and one death hook");
        es=enemies(c,g);a=es.spawn(EnemyType.SLIME,650,600);p.clear();
        for(int i=0;i<5;i++){p.meteor(650,600,650,600,1,1,level);p.update(.001f,es,g);}check(p.zoneCount()==3,"fire zones capped at three");hp=a.hp();p.update(.5f,es,g);check(a.hp()==hp-1,"overlap uses maximum once per tick");
        p.clear();check(p.zoneCount()==0 && p.size()==0,"end clears fire and projectiles");
        var wg=wall();es=enemies(c,wg);a=es.spawn(EnemyType.SLIME,650,600);p.meteor(600,600,650,600,3,2,level);for(int i=0;i<30;i++)p.update(.1f,es,wg);check(a.hp()==100,"blast and burn LOS blocked");
    }
    static void web(){
        var c=cfg(Difficulty.EASY,GameMode.NORMAL);var g=open();var p=new Player(600,600,null,c);var es=enemies(c,g);
        for(int i=0;i<12;i++)es.spawn(EnemyType.SLIME,670+i%4*24,560+i/4*32);var w=system(c,WeaponType.LIGHTNING,p);w.update(.01f,p,es,g);
        check(w.lightningInitialBolts==3 && w.lightningHits==8,"three roots with shared eight target budget");int damaged=0;for(int i=0;i<es.size();i++){var e=es.get(i);if(e.hp()<100){damaged++;check(e.hp()==95,"one damage per activation");}}
        check(damaged==8 && w.projectiles.effectCount()==8,"unique targets and visible real edges");
        w.update(.1f,p,es,g);check(w.lightningHits==8,"activation cooldown");
        es=enemies(c,wall());var e=es.spawn(EnemyType.SLIME,650,600);w=system(c,WeaponType.LIGHTNING,p);w.update(.1f,p,es,wall());check(e.hp()==100 && w.lightningHits==0,"no chain through hard obstacle");
    }
    static void bosses(){
        for(var d:Difficulty.values())for(var m:GameMode.values())for(var type:WeaponType.values())for(boolean finalBoss:new boolean[]{false,true}){
            var c=cfg(d,m);var s=new GameSession(c,open(),600,600,null,null,42);s.bosses.prepare(.01f,600,s.player,450,450,750,750);
            var boss=finalBoss?s.bosses.lich():s.bosses.mini();var e=boss.actor;e.x=638;e.y=600;var w=system(c,type,s.player);int hp=e.hp();
            s.player.stats.addDamage(100000);w.update(.01f,s.player,s.enemies,open());if(type==WeaponType.BOW || type==WeaponType.FIREBALL)w.projectiles.update(.15f,s.enemies,open());
            check(e.hp()<hp && e.hp()>0 && hp-e.hp()<=Math.ceil(hp*.2f),"evolved damage obeys boss guard "+type+d+m+finalBoss);
            int kills=s.kills,rolls=s.pickups.deathRolls;for(int i=0;i<10 && e.alive();i++){e.advance(.21f);e.damage(Integer.MAX_VALUE);}e.damage(Integer.MAX_VALUE);
            check(s.kills==kills+1 && s.pickups.deathRolls==rolls+1,"boss death/roll exactly once");
            s.end();
        }
    }
    static void balance(){
        for(var d:Difficulty.values())for(var m:GameMode.values())for(float stage:new float[]{0,210,540})for(var type:WeaponType.values()){
            var c=cfg(d,m);var grid=open();var p=new Player(600,600,null,c);var es=enemies(c,grid);for(int i=0;i<12;i++)es.spawn(EnemyType.SLIME,636+i%4*18,572+i/4*24);
            p.stats.addDamage(stage>=540?3:stage>=210?1:0);var w=system(c,type,p);int hp=0;for(int i=0;i<es.size();i++)hp+=es.get(i).hp();
            for(int i=0;i<200;i++){for(int j=0;j<es.size();j++)es.get(j).advance(.05f);p.update(.05f,0,0,grid);w.update(.05f,p,es,grid);check(w.projectiles.size()<=48 && w.projectiles.effectCount()<=48 && w.projectiles.zoneCount()<=3,"combat caps in all modes/stages");}
            int after=0;for(int i=0;i<es.size();i++)after+=es.get(i).hp();check(after<hp,"effective evolution at stage/mode");
        }
    }
    static void deathAndVictory(){
        for(var type:WeaponType.values()) {
            var c=new GameConfig.Builder().playerHp(100000).population(0,40).enemy(EnemyType.SLIME,100,1,.01f).pickups(new PickupConfig(15,20,60,6,20,1,1,0,1,1)).build();
            var s=new GameSession(c,open(),600,600,null,null,42);s.player.stats.addDamage(1000);var e=s.enemies.spawn(EnemyType.SLIME,638,600);var w=system(s.config,type,s.player);
            for(int i=0;i<50;i++)w.update(.05f,s.player,s.enemies,open());
            check(!e.alive() && s.kills==1 && s.pickups.deathRolls==1 && s.pickups.healthDrops()==1,"evolution uses single real death/kill/health drop "+type);
            for(int i=0;i<10;i++){e.damage(1000);w.update(.05f,s.player,s.enemies,open());}check(s.kills==1 && s.pickups.deathRolls==1,"DoT/orbit/chain cannot duplicate corpse events");s.end();
        }
        for(var d:Difficulty.values())for(var m:GameMode.values()) {
            var c=new GameConfig.Builder().difficulty(d).mode(m).debug(true).playerHp(100000).population(0,d==Difficulty.HARD?60:40).stages(new GameConfig.Stage(0,0,1000000,1,0,0,0)).build();
            var s=new GameSession(c,open(),600,600,null,null,42);s.diagnosticProgress(600);s.bosses.prepare(.01f,600,s.player,450,450,750,750);
            for(var t:new WeaponType[]{WeaponType.SWORD,WeaponType.FIREBALL,WeaponType.LIGHTNING}){while(s.weapons.loadout.canGrant(t))s.weapons.loadout.grant(t);check(WeaponOffer.evolution(t,c.strings).apply(s.weapons.loadout,s.player),"explicit evolved boss probe");}
            s.player.stats.addDamage(1000);s.bosses.mini().actor.x=638;s.bosses.mini().actor.y=600;s.bosses.lich().actor.x=650;s.bosses.lich().actor.y=600;
            boolean phase2=false;for(int i=0;i<1000 && !s.victory();i++){s.update(.05f,0,0,false,450,450,750,750);phase2|=s.bosses.lich().phase()==2;if(!s.bosses.finalDefeated())check(!s.victory(),"no Victory while Lich lives");}
            check(phase2 && s.bosses.lich().phaseChanges==1,"evolved damage triggers existing phase transition once");
            check(s.victory() && s.bosses.finalDefeated() && !s.bosses.lich().actor.alive() && s.result().victory(),"true Victory follows evolved kill and real death animation");s.end();
        }
    }
    static void crowdBudget(){
        var c=cfg(Difficulty.HARD,GameMode.NORMAL);var g=open();var p=new Player(600,600,null,c);p.stats.addDamage(10000);var es=enemies(c,g);
        for(int i=0;i<60;i++)check(es.spawn(EnemyType.SLIME,638,600)!=null,"hard resident budget unchanged");
        var l=new WeaponLoadout(c.weapons);for(var t:new WeaponType[]{WeaponType.SWORD,WeaponType.FIREBALL,WeaponType.LIGHTNING}){while(l.canGrant(t))l.grant(t);WeaponOffer.evolution(t,c.strings).apply(l,p);}
        var w=new WeaponSystem(c,l,new SwordCombat(c,l.get(WeaponType.SWORD)));w.projectiles.meteor(638,600,638,600,100000,1,EvolutionConfig.level(WeaponType.FIREBALL));w.update(.01f,p,es,g);
        int dead=0;for(int i=0;i<es.size();i++)if(!es.get(i).alive())dead++;
        check(w.orbitHits<=6 && w.lightningHits<=8 && dead<=26 && dead>0,"three evolved weapons cannot erase all sixty in one frame");
        int before=w.projectiles.burnHits;w.projectiles.update(.5f,es,g);check(w.projectiles.burnHits-before<=12,"shared ground tick has a finite resident budget");
    }
    static void realStages(){
        for(var d:Difficulty.values())for(var m:GameMode.values())for(float progress:new float[]{0,210,540})for(var type:WeaponType.values()){
            var c=new GameConfig.Builder().difficulty(d).mode(m).playerHp(100000).population(0,d==Difficulty.HARD?60:40).build();var g=open();var p=new Player(600,600,null,c);var es=enemies(c,g);
            for(int i=0;i<12;i++)es.spawn(i%2==0?EnemyType.SLIME:EnemyType.WILD_BOAR,636+i%4*18,572+i/4*24,progress);
            var w=system(c,type,p);int hp=0;for(int i=0;i<es.size();i++)hp+=es.get(i).hp();
            for(int i=0;i<100;i++){p.update(.05f,0,0,g);for(int j=0;j<es.size();j++)es.get(j).advance(.05f);w.update(.05f,p,es,g);}
            int after=0;for(int i=0;i<es.size();i++)after+=es.get(i).hp();check(after<hp,"evolution damages real stage-scaled slime/boar "+d+m+progress+type);
        }
    }
    public static void main(String[] args){progression();orbit();arrows();meteor();web();bosses();balance();deathAndVictory();crowdBudget();realStages();System.out.println("EvolutionChecks PASS "+checks+" checks");}
}
