import com.example.monstersurvivor.ai.*;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.enemy.*;
import com.example.monstersurvivor.enemy.skill.*;
import com.example.monstersurvivor.entity.*;
import com.example.monstersurvivor.gameplay.*;
import com.example.monstersurvivor.localization.*;
import com.example.monstersurvivor.pickup.*;
import com.example.monstersurvivor.skill.*;
import com.example.monstersurvivor.world.*;
import java.util.*;

/** Finite functional coverage of all policies/archetypes; no GL, assets or root diagnostic files. */
public final class EnemySkillChecks {
    static int checks;
    static void check(boolean v,String m){if(!v)throw new AssertionError(m);checks++;}
    static void close(float a,float b,String m){check(Math.abs(a-b)<.006f,m+" actual="+a+" expected="+b);}
    static CollisionGrid open(){return new CollisionGrid(64,64,16,new byte[4096]);}
    static GameConfig config(boolean hard){var b=new GameConfig.Builder().difficulty(hard?Difficulty.HARD:Difficulty.EASY).debug(true).playerHp(10000).population(0,hard?60:40).stages(new GameConfig.Stage(0,0,100000,1000,0,0,0));for(var d:EnemyRegistry.all())b.enemy(d,1000,1,.001f);return b.build();}
    static EnemyDefinition species(EnemySkillDefinition skill){return EnemyRegistry.all().stream().filter(d->d.skill==skill).findFirst().orElseThrow();}
    static class Arena {
        final CollisionGrid grid;final GameConfig config;final EnemySystem enemies;final Player player;final Enemy enemy;final EnemySkillDefinition skill;
        Arena(EnemySkillDefinition skill,boolean hard){this(skill,hard,open());}
        Arena(EnemySkillDefinition skill,boolean hard,CollisionGrid grid){this.grid=grid;config=config(hard);enemies=new EnemySystem(new NavigationGrid(grid),null,config);player=new Player(512,512,null,config);this.skill=skill;
            enemy=enemies.spawn(species(skill),skill.kind==EnemySkillDefinition.Kind.SLAM?548:592,512,skill.unlock);check(enemy!=null,"real registered spawn");enemy.specialSkill.cooldown=0;
        }
        void prepare(float progress){enemies.skills.prepare(.01f,player,progress,0,0,1024,1024);}
        void tick(float dt){player.update(dt,0,0,grid);player.statuses.update(dt,player);enemies.beginFrame();enemies.skills.prepare(dt,player,skill.unlock,0,0,1024,1024);enemies.update(dt,player);enemies.skills.resolve(dt,player);}
        void untilRelease(){for(int i=0;i<50 && enemies.skills.releases==0;i++)tick(.05f);check(enemies.skills.releases==1,"actual release "+skill);}
        void untilCancelled(){for(int i=0;i<30;i++)tick(.05f);check(enemies.skills.releases==0 && !enemy.specialSkill.busy(),"Hurt cancels cast "+skill);}
    }
    static void coverage(){
        check(EnemyRegistry.all().size()==82,"exact82 policy coverage");int assigned=0;Set<EnemySkillDefinition> types=new HashSet<>();Map<String,Set<EnemySkillDefinition>> biomes=new TreeMap<>();
        for(var d:EnemyRegistry.all()){
            check(d.skill!=null,"explicit policy "+d.id);types.add(d.skill);biomes.computeIfAbsent(d.biome,k->new HashSet<>()).add(d.skill);
            if(d.skill!=EnemySkillDefinition.NONE){assigned++;check(d.skill.windup>=.75f && d.skill.damage==1 && d.skill.count<=5,"readable bounded tuning "+d.id);check(d.skill.cooldown(false)>d.skill.cooldown(true),"difficulty uses cooldown only");}
        }
        check(assigned>=40 && assigned<82,"many suitable species, simple melee stays NONE");check(types.size()==EnemySkillDefinition.values().length,"every configured archetype has an actual species");
        check(biomes.get("Snow").contains(EnemySkillDefinition.ICE) && biomes.get("Swamp").contains(EnemySkillDefinition.POISON),"biome gameplay differs");
        var boss=EnemyDefinition.boss("test","test","mc_moss_golem",1,16,30,1,.3f,1);check(boss.skill==EnemySkillDefinition.NONE,"registry body does not grant normal skills to Boss");
        System.out.println("Policies: "+assigned+" assigned / "+(82-assigned)+" NONE, "+types.size()+" definitions");
    }
    static void archetypes(){
        for(boolean hard:new boolean[]{false,true})for(var d:EnemySkillDefinition.values())if(d!=EnemySkillDefinition.NONE){
            var a=new Arena(d,hard);a.prepare(d.unlock-1);check(!a.enemy.specialSkill.busy(),"progress gate "+d);
            a.prepare(d.unlock);check(a.enemy.specialSkill.phase==EnemySkillInstance.Phase.PREPARE && a.enemy.aiLocked,"prepare locks locomotion "+d);
            float x=a.enemy.x,y=a.enemy.y;for(float t=0;t<d.windup(hard)-.08f;t+=.05f){a.tick(.05f);check(a.enemies.skills.releases==0,"windup cannot hit early "+d);}
            close(a.enemy.x,x,"stationary cast x");close(a.enemy.y,y,"stationary cast y");a.untilRelease();
            check(a.enemies.skills.projectiles.fired==d.count,"whole fan count "+d);
            if(d.kind==EnemySkillDefinition.Kind.SHOT){
                var p=a.enemies.skills.projectiles;int count=0;float[] vx=new float[24],vy=new float[24],px=new float[24],py=new float[24];
                for(int i=0;i<24;i++){var s=p.shot(i);if(s.active){count++;vx[i]=s.vx;vy[i]=s.vy;px[i]=s.x;py[i]=s.y;close((float)Math.hypot(s.vx,s.vy),d.speed,"constant configured speed");check(s.ownerId==a.enemy.id && s.team==EnemyProjectiles.Team.ENEMY,"ordinary ownership/team");}}
                check(count==d.count,"all lanes active");a.player.x=300;a.player.y=750;
                // Player runs across the firing line, then Dash-like displacement, then escapes out of range.
                for(int frame=0;frame<8;frame++){
                    a.player.x+=frame==2?75:4;a.player.y+=frame==3?-100:2;p.update(.05f,a.player,a.grid);
                    for(int i=0;i<24;i++){var s=p.shot(i);if(s.active){check(Float.floatToIntBits(s.vx)==Float.floatToIntBits(vx[i]) && Float.floatToIntBits(s.vy)==Float.floatToIntBits(vy[i]),"no homing over many frames "+d);close(s.x,px[i]+vx[i]*.05f,"linear x");close(s.y,py[i]+vy[i]*.05f,"linear y");px[i]=s.x;py[i]=s.y;}}
                }
                a.enemy.damage(Integer.MAX_VALUE);p.update(.01f,a.player,a.grid);check(p.size()==0,"owner death retires all ordinary shots");
            }else{
                check(a.enemies.skills.projectiles.effectCount()>0,"real impact FX event");
            }
            var b=new Arena(d,hard);b.enemy.x=800;b.prepare(d.unlock);check(!b.enemy.specialSkill.busy(),"range gate");
            b.enemy.x=592;b.enemies.skills.prepare(.01f,b.player,d.unlock,0,0,550,1024);check(!b.enemy.specialSkill.busy(),"no distant offscreen firing");
            b.enemy.x=d.kind==EnemySkillDefinition.Kind.SLAM?548:592;b.prepare(d.unlock);check(b.enemy.specialSkill.busy(),"cast can begin");
            b.enemy.damage(1);b.untilCancelled();
        }
    }
    static void collisionAndStatus(){
        for(int mask:new int[]{CollisionGrid.TREE,CollisionGrid.ROCK,CollisionGrid.BOUNDARY}){
            byte[] cells=new byte[4096];for(int y=0;y<64;y++)cells[y*64+34]=(byte)mask;var grid=new CollisionGrid(64,64,16,cells);
            var a=new Arena(EnemySkillDefinition.ARROW,true,grid);a.prepare(100);check(!a.enemy.specialSkill.busy(),"LOS rejects hard obstacle");
            var p=a.enemies.skills.projectiles;a.player.x=600;check(p.fire(a.enemy,a.skill,512,512,1,0),"swept fixture shot");int hp=a.player.hp();p.update(1.2f,a.player,grid);check(p.size()==0 && p.blocked==1 && a.player.hp()==hp,"large-delta wall before player");
        }
        for(var type:new EnemySkillDefinition[]{EnemySkillDefinition.ARROW,EnemySkillDefinition.ICE,EnemySkillDefinition.POISON,EnemySkillDefinition.TOXIC,EnemySkillDefinition.FIREBALL}){
            var a=new Arena(type,true);var p=a.enemies.skills.projectiles;int hp=a.player.hp();
            var ally=a.enemies.spawn(EnemyRegistry.get("honey_slime"),502,512);int allyHp=ally.hp();
            check(p.fire(a.enemy,type,490,512,1,0),"direct shot");p.update(.5f,a.player,a.grid);check(a.player.hp()==hp-1 && p.size()==0,"swept player hit once");
            if(type.status!=null)check(a.player.statuses.active(type.status),"status after accepted hit");
            check(ally.hp()==allyHp && a.enemy.hp()==1000,"hostile shot never damages owner/allies");
            p.update(.5f,a.player,a.grid);check(a.player.hp()==hp-1,"no repeated contact hit");
            a.player.statuses.clear();p.fire(a.enemy,type,500,512,1,0);p.update(.3f,a.player,a.grid);check(a.player.hp()==hp-1 && p.size()==0,"immunity consumes projectile");
            if(type.status!=null)check(!a.player.statuses.active(type.status),"blocked hit cannot grant status");
        }
        var a=new Arena(EnemySkillDefinition.ARROW,true);var pool=a.enemies.skills.projectiles;
        check(!pool.fire(a.enemy,a.skill,500,500,Float.NaN,1) && !pool.fire(a.enemy,a.skill,500,500,0,0),"invalid vectors cannot enter hostile pool");
        for(int i=0;i<500;i++)pool.fire(a.enemy,a.skill,700,700,1,0);check(pool.size()==24,"fixed shot pool under overload");a.player.x=50;pool.update(10,a.player,a.grid);check(pool.size()==0,"finite projectile lifetime");
        for(int i=0;i<500;i++)pool.effect("cast",20,20,20,.3f);check(pool.effectCount()==24,"fixed FX pool under overload");pool.update(.4f,a.player,a.grid);check(pool.effectCount()==0,"FX expire");
        var p=new Player(512,512);p.statuses.apply(PlayerStatuses.Type.SLOW);p.update(1,1,0,open());close(p.x,572,"Slow retains75percent normal movement");
        p=new Player(512,512);int hp=p.hp();p.statuses.apply(PlayerStatuses.Type.POISON);for(int i=0;i<200;i++)p.statuses.apply(PlayerStatuses.Type.POISON);
        for(int i=0;i<60;i++){p.update(.05f,0,0,open());p.statuses.update(.05f,p);}check(p.hp()==hp-2 && !p.statuses.active(PlayerStatuses.Type.POISON),"refresh-only poison, exactly two finite ticks");
        p.statuses.apply(PlayerStatuses.Type.BURN);for(int i=0;i<40;i++){p.update(.05f,0,0,open());p.statuses.update(.05f,p);}check(p.hp()==hp-3 && !p.statuses.active(PlayerStatuses.Type.BURN),"finite burn respects shared immunity");
    }
    static void groundAndCancel(){
        for(var d:new EnemySkillDefinition[]{EnemySkillDefinition.SLAM,EnemySkillDefinition.SPIRIT_SLAM,EnemySkillDefinition.TARGET_BOMB,EnemySkillDefinition.CURSE_AOE}){
            var a=new Arena(d,true);a.prepare(600);float tx=a.enemy.specialSkill.targetX,ty=a.enemy.specialSkill.targetY;int hp=a.player.hp();
            a.player.x=tx+100;a.player.y=ty+80;a.untilRelease();close(a.enemy.specialSkill.targetX,tx,"ground snapshot fixed x");close(a.enemy.specialSkill.targetY,ty,"ground snapshot fixed y");check(a.player.hp()==hp,"movement escapes telegraphed AoE");
            a.player.x=tx;a.player.y=ty;for(int i=0;i<5;i++)a.tick(.05f);check(a.player.hp()==hp,"one AoE cannot hit late entrants repeatedly");
            a=new Arena(d,true);a.prepare(600);check(a.enemies.knockback(a.enemy,1,0,25),"actual swept knockback");for(int i=0;i<30;i++)a.tick(.05f);check(a.enemies.skills.releases==0 && !a.enemy.specialSkill.busy(),"knockback cancels locked ground cast");
        }
        var a=new Arena(EnemySkillDefinition.TOXIC,true);var pool=a.enemies.skills.projectiles;
        a.player.x=900;a.player.y=900;
        for(int i=0;i<5;i++){pool.fire(a.enemy,a.skill,300,300,1,0);pool.shot(i).lifetime=.1f;}
        // Impacting a hard wall creates only bounded, delayed poison areas.
        byte[] cells=new byte[4096];for(int y=0;y<64;y++)cells[y*64+34]=(byte)CollisionGrid.ROCK;var grid=new CollisionGrid(64,64,16,cells);pool.clear();
        for(int i=0;i<8;i++)pool.fire(a.enemy,a.skill,530,500,1,0);pool.update(.3f,a.player,grid);check(pool.areaCount()==3,"poison areas capped independently");
        a.player.x=541;a.player.y=500;int hp=a.player.hp();pool.update(.2f,a.player,grid);check(a.player.hp()==hp,"poison area has visible grace before tick");
        a.player.x=538;pool.update(.45f,a.player,grid);check(a.player.hp()==hp-1 && a.player.statuses.active(PlayerStatuses.Type.POISON),"real delayed poison area hit/status");
        for(int i=0;i<18;i++){a.player.update(.1f,0,0,grid);pool.update(.1f,a.player,grid);}check(pool.areaCount()==0 && pool.areaHits==2,"finite poison area, shared immunity deduplicates overlapping zones");
        for(int i=0;i<3;i++)pool.fire(a.enemy,a.skill,530,500,1,0);pool.update(.3f,a.player,grid);
        a.enemy.damage(Integer.MAX_VALUE);pool.update(.1f,a.player,grid);check(pool.areaCount()==0,"owner death cleans poison areas");
    }
    static GameSession session(){return new GameSession(config(true),open(),512,512,null,null,42);}
    static void tick(GameSession s,int n){for(int i=0;i<n;i++)s.update(.05f,0,0,false,256,256,768,768);}
    static void upgrade(GameSession s,SkillType type){
        check(s.pickups.spawn(PickupType.SKILL_BOX,s.player.x,s.player.y,1)!=null,"actual upgrade pickup");tick(s,1);int at=-1;for(int i=0;i<s.skillSelection.size();i++)if(s.skillSelection.get(i).type==type)at=i;
        check(at>=0 && s.chooseSkill(at,s.skillSelection.token()),"actual upgrade transaction");
    }
    static void sessions(){
        for(int level=1;level<=5;level++){
            var s=session();for(int i=1;i<level;i++){upgrade(s,SkillType.DASH);upgrade(s,SkillType.ULTIMATE);}tick(s,310);
            check(s.activateSkill(SkillType.DASH,1,0,false),"actual Dash Lv"+level);s.skills.advance(.02f,0,0,s.player,open());int hp=s.player.hp();
            float dashOrigin=s.player.x-s.skills.config().dash(level).distance*.1f;
            var e=s.enemies.spawn(EnemyRegistry.get("mc_frost_fox"),s.player.x+70,s.player.y,200);var pool=s.enemies.skills.projectiles;
            pool.fire(e,e.definition.skill,s.player.x-2,s.player.y,1,0);pool.update(.01f,s.player,open());check(s.player.hp()==hp && !s.player.statuses.active(PlayerStatuses.Type.SLOW) && pool.size()==0,"Dash iframe blocks hit/status and consumes shot Lv"+level);
            s.player.statuses.apply(PlayerStatuses.Type.SLOW);tick(s,5);close(s.player.x,dashOrigin+s.skills.config().dash(level).distance,"Slow never shortens actual Dash Lv"+level);s.player.statuses.clear();
            s.enemies.clear();e=s.enemies.spawn(EnemyRegistry.get("sc_goblin"),s.player.x+80,s.player.y,60);s.diagnosticProgress(60);e.specialSkill.cooldown=0;tick(s,1);check(e.specialSkill.busy(),"enemy windup ready for Ultimate");
            check(s.activateSkill(SkillType.ULTIMATE,0,0,false),"actual Ultimate Lv"+level);tick(s,9);check(!e.specialSkill.busy() && pool.fired==1,"Ultimate cancels cast before new projectile Lv"+level);
            s.end();check(pool.size()==0,"Home cleanup");
        }
        var s=session();s.diagnosticProgress(240);s.player.statuses.apply(PlayerStatuses.Type.POISON);s.player.statuses.apply(PlayerStatuses.Type.SLOW);
        var e=s.enemies.spawn(EnemyRegistry.get("mc_swamp_hag"),592,512,240);e.specialSkill.cooldown=0;tick(s,1);float age=e.specialSkill.age,remaining=s.player.statuses.remaining(PlayerStatuses.Type.POISON);
        for(int i=0;i<20;i++)s.update(.1f,1,0,true,256,256,768,768);close(e.specialSkill.age,age,"Pause freezes cast");close(s.player.statuses.remaining(PlayerStatuses.Type.POISON),remaining,"Pause freezes status");
        s.pickups.spawn(PickupType.WEAPON_BOX,s.player.x,s.player.y,1);tick(s,1);check(s.choosingWeapon(),"real weapon modal");age=e.specialSkill.age;remaining=s.player.statuses.remaining(PlayerStatuses.Type.POISON);tick(s,20);close(e.specialSkill.age,age,"reward modal freezes cast");close(s.player.statuses.remaining(PlayerStatuses.Type.POISON),remaining,"reward modal freezes status");
        s.chooseWeapon(0,s.selection.token());s.player.damage(Integer.MAX_VALUE);tick(s,40);check(s.phase()==GameSession.Phase.GAME_OVER && s.enemies.skills.projectiles.size()==0 && !s.player.statuses.active(PlayerStatuses.Type.POISON),"death clears all hostile transients");s.end();
        var fresh=new GameSession(GameConfig.defaults(),open(),512,512,null);check(fresh.player.hp()==10 && fresh.weapons.loadout.describe().equals("SWORD:1") && !fresh.config.debug,"normal baseline remains unchanged");fresh.end();
        s=session();var owner=s.enemies.spawn(EnemyRegistry.get("sc_goblin"),632,512,60);var shots=s.enemies.skills.projectiles;shots.fire(owner,owner.definition.skill,620,512,-1,0);float vx=shots.shot(0).vx,vy=shots.shot(0).vy;
        check(s.activateSkill(SkillType.DASH,0,1,false),"real perpendicular Dash after enemy fire");for(int i=0;i<4;i++){s.skills.advance(.05f,0,0,s.player,open());shots.update(.05f,s.player,open());check(shots.shot(0).active && shots.shot(0).vx==vx && shots.shot(0).vy==vy,"real Dash does not steer enemy projectile");}s.end();
        for(boolean hard:new boolean[]{false,true}){
            s=new GameSession(config(hard),open(),512,512,null);for(int i=0;i<70;i++)s.enemies.spawn(EnemyRegistry.get("mc_flame_skull"),420+(i%10)*18,420+(i/10)*18,500);check(s.enemies.normalSize()==(hard?60:40),"unchanged resident cap");s.diagnosticProgress(500);
            tick(s,240);check(s.enemies.skills.projectiles.size()<=24 && s.enemies.skills.maxTelegraphs<=(hard?4:3) && s.enemies.skills.maxCasters<=(hard?10:8) && s.enemies.maxSearchesObserved<=2,"shared budgets under dense eligible crowd");s.end();
        }
    }
    public static void main(String[] args){coverage();archetypes();collisionAndStatus();groundAndCancel();sessions();System.out.println("EnemySkillChecks PASS "+checks+" checks (82 policies, all archetypes, non-homing/collision/status/session budgets)");}
}
