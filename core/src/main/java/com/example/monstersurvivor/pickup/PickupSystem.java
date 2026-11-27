package com.example.monstersurvivor.pickup;

import com.badlogic.gdx.utils.Array;
import com.example.monstersurvivor.ai.NavigationGrid;
import com.example.monstersurvivor.config.*;
import com.example.monstersurvivor.entity.*;
import java.util.Random;
import static com.example.monstersurvivor.localization.GameStrings.Key.*;

/** Match-owned pickups. Call only while RUNNING; all ages are real gameplay seconds. */
public final class PickupSystem {
    public static final int COIN_CAPACITY=48;
    public static final float COIN_DROP_CHANCE=.12f,COIN_LIFETIME=20,COIN_PICKUP_RADIUS=12;
    public com.example.monstersurvivor.audio.AudioSink audio=com.example.monstersurvivor.audio.AudioSink.NONE;
    /** A future modal handler returns false while its item must remain pending/unconsumed. */
    public interface CollectionHandler {boolean collect(Pickup item,Player player);}
    private final Array<Pickup> items=new Array<>(false,32),effects=new Array<>(false,8);
    private final CollectionHandler[] handlers=new CollectionHandler[PickupType.values().length];
    private final GameConfig game;
    public final PickupConfig config;
    private final NavigationGrid nav;
    private final Player owner;
    private final PickupSpawner spawner;
    private final PickupSpawner chestSpawner;
    private final Random schedule,types,rewards,drops,coinDrops;
    private java.util.function.BooleanSupplier chestAvailable=()->false,suspended=()->false;
    private java.util.function.BooleanSupplier coinDropsEnabled=()->false;
    private final Random skillSchedule;
    private float nextChestSpawn;
    private float nextSpawn;
    private long nextId;
    public int spawnedBoxes,collectedItems,expiredItems,deathRolls,noticeSerial,coinDropRolls,collectedCoins;
    public int spawnedSkillBoxes,retiredSkillBoxes;
    public int revision;
    public String notice="";
    public float noticeTime;
    public PickupType noticeType=PickupType.DAMAGE_BOX;
    public PickupSystem(GameConfig game,NavigationGrid nav,Player player,long seed) {
        this.game=game;config=game.pickups;this.nav=nav;owner=player;
        schedule=new Random(seed^0x35A100L);types=new Random(seed^0x35A200L);
        rewards=new Random(seed^0x35A300L);drops=new Random(seed^0x35A400L);
        coinDrops=new Random(seed^0xC01A5L);
        skillSchedule=new Random(seed^0x361100L);nextChestSpawn=skillInterval(true);
        spawner=new PickupSpawner(nav,config,seed^0x35A500L);nextSpawn=interval();
        chestSpawner=new PickupSpawner(nav,config,seed^0x361200L);
        for(PickupType type:PickupType.values())register(type,this::apply);
    }
    public void register(PickupType type,CollectionHandler handler) {
        if(handler==null)throw new IllegalArgumentException("Collection handler required");handlers[type.ordinal()]=handler;
    }
    public void enableTreasureChests(java.util.function.BooleanSupplier available) {chestAvailable=available==null?()->false:available;}
    /** Compatibility adapters: both retired box APIs now control the same chest eligibility. */
    public void enableWeaponBoxes(java.util.function.BooleanSupplier available) {enableTreasureChests(available);}
    public void enableSkillBoxes(java.util.function.BooleanSupplier available) {enableTreasureChests(available);}
    public void enableCoinDrops(java.util.function.BooleanSupplier enabled) {coinDropsEnabled=enabled==null?()->false:enabled;}
    public void suspendWhen(java.util.function.BooleanSupplier suspended) {this.suspended=suspended;}
    public boolean contains(long id) {for(int i=0;i<items.size;i++)if(items.get(i).id==id)return true;return false;}
    public boolean collect(long id) {
        if(!owner.alive())return false;
        for(int i=0;i<items.size;i++) {Pickup p=items.get(i);
            if(p.id==id && !p.collected && handlers[p.type.ordinal()].collect(p,owner)) {finishCollection(i,p);return true;}
        }
        return false;
    }
    private void finishCollection(int index,Pickup item) {
        items.removeIndex(index);item.collected=true;collectedItems++;revision++;
        if(effects.size==8)effects.removeIndex(0);effects.add(item);
    }
    public void announce(PickupType type,String message) {notice=message;noticeType=type;noticeTime=1.4f;noticeSerial++;}
    public int size() {return items.size;}
    public Pickup get(int i) {return items.get(i);}
    public int effectCount() {return effects.size;}
    public Pickup effect(int i) {return effects.get(i);}
    public float nextSpawnProgress() {return nextSpawn;}
    public int boxes() {int count=0;for(int i=0;i<items.size;i++)if(items.get(i).type.box)count++;return count;}
    public int treasureChests() {int count=0;for(int i=0;i<items.size;i++)if(items.get(i).type==PickupType.TREASURE_CHEST)count++;return count;}
    public int skillBoxes() {return treasureChests();}
    public int ordinaryBoxes() {return boxes()-treasureChests();}
    public int healthDrops() {int count=0;for(int i=0;i<items.size;i++)if(items.get(i).type==PickupType.HEALTH_DROP)count++;return count;}
    public int coinCount() {int count=0;for(int i=0;i<items.size;i++)if(items.get(i).type==PickupType.COIN)count++;return count;}
    public boolean occupied(float x,float y) {
        for(int i=0;i<items.size;i++) {Pickup p=items.get(i);if(Math.abs(p.x-x)<14 && Math.abs(p.y-y)<12)return true;}return false;
    }
    private float interval() {return config.intervalMin+schedule.nextFloat()*(config.intervalMax-config.intervalMin);}
    private float skillInterval(boolean first) {var c=game.skills.boxes;return first?c.firstMin+skillSchedule.nextFloat()*(c.firstMax-c.firstMin):c.intervalMin+skillSchedule.nextFloat()*(c.intervalMax-c.intervalMin);}
    public float nextSkillSpawnProgress() {return nextChestSpawn;}
    public float nextChestSpawnProgress() {return nextChestSpawn;}
    public void update(float dt,float progress,Player player) {
        if(!player.alive() || suspended.getAsBoolean())return;
        noticeTime=Math.max(0,noticeTime-dt);
        for(int i=effects.size-1;i>=0;i--)if((effects.get(i).effectAge+=dt)>=.3f) {effects.removeIndex(i);revision++;}
        for(int i=items.size-1;i>=0;i--) {
            Pickup p=items.get(i);p.age+=dt;
            if(p.age>=p.lifetime) {items.removeIndex(i);expiredItems++;revision++;continue;}
            float dx=player.x-p.x,dy=player.y-p.y;
            boolean touched=p.type==PickupType.COIN?dx*dx+dy*dy<=COIN_PICKUP_RADIUS*COIN_PICKUP_RADIUS
                :player.touchesMovement(p.x,p.y,Pickup.HALF_WIDTH,Pickup.HALF_HEIGHT,nav.collision);
            if(touched) {
                if(handlers[p.type.ordinal()].collect(p,player))finishCollection(i,p);
                if(suspended.getAsBoolean())return;
            }
        }
        if(progress>=nextSpawn) {
            // At most one event per update, including Demo and cap/failure: no catch-up burst.
            nextSpawn=progress+interval();
            if(ordinaryBoxes()<config.boxCap) {
                int cell=spawner.boxCell(player,this);
                if(cell>=0) {
                    int choice=types.nextInt(player.stats.speed()>=config.maxSpeed?2:3);
                    PickupType type=PickupType.values()[choice];
                    if(spawn(type,nav.x(cell),nav.y(cell),0)!=null)spawnedBoxes++;
                }
            }
        }
        if(progress>=nextChestSpawn) {
            // One shared chest schedule, independent from ordinary stat boxes and gated out of debug fixtures.
            nextChestSpawn=progress+skillInterval(false);
            if(!game.debug && chestAvailable.getAsBoolean() && treasureChests()<game.skills.boxes.cap) {
                int cell=chestSpawner.boxCell(player,this);
                if(cell>=0 && spawn(PickupType.TREASURE_CHEST,nav.x(cell),nav.y(cell),0)!=null) {
                    spawnedSkillBoxes++;
                }
            }
        }
    }
    /** One call per Enemy's transition to zero HP, before the corpse animation advances. */
    public void enemyDied(Enemy enemy,Player player) {
        if(!player.alive())return;
        deathRolls++;
        if(drops.nextFloat()<config.dropChance(game.difficulty,player.hp()) && healthDrops()<config.dropCap) {
            int heal=config.dropHealMin+drops.nextInt(config.dropHealMax-config.dropHealMin+1);
            int cell=spawner.dropCell(enemy.x,enemy.y,player,this);
            if(cell>=0)spawn(PickupType.HEALTH_DROP,nav.x(cell),nav.y(cell),heal);
        }
        if(!enemy.boss && enemy.summonOwner==0 && coinDropsEnabled.getAsBoolean() && coinCount()<COIN_CAPACITY) {
            coinDropRolls++;
            if(coinDrops.nextFloat()<COIN_DROP_CHANCE) {
                int coinCell=spawner.dropCell(enemy.x,enemy.y,player,this);
                if(coinCell>=0)spawn(PickupType.COIN,nav.x(coinCell),nav.y(coinCell),1);
            }
        }
    }
    /** Checked placement for scripted events and future functional item types; never bypasses caps/navigation. */
    public Pickup spawn(PickupType type,float x,float y,int heal) {
        if(type==PickupType.WEAPON_BOX || type==PickupType.SKILL_BOX)type=PickupType.TREASURE_CHEST;
        if(type==null || (type==PickupType.TREASURE_CHEST && (treasureChests()>=game.skills.boxes.cap || !chestAvailable.getAsBoolean()))
            || (type==PickupType.COIN && (!coinDropsEnabled.getAsBoolean() || coinCount()>=COIN_CAPACITY))
            || (type!=PickupType.TREASURE_CHEST && type!=PickupType.COIN && (type.box?ordinaryBoxes()>=config.boxCap:healthDrops()>=config.dropCap))
            || !nav.collision.canOccupy(x,y,Pickup.HALF_WIDTH,Pickup.HALF_HEIGHT)
            || !nav.connected(nav.cell(owner.x,owner.y),nav.cell(x,y),false) || occupied(x,y))return null;
        float lifetime=type==PickupType.COIN?COIN_LIFETIME:type==PickupType.TREASURE_CHEST?game.skills.boxes.lifetime:type.box?config.boxLifetime:config.dropLifetime;
        Pickup p=new Pickup(++nextId,type,x,y,lifetime,Math.max(1,heal));items.add(p);revision++;return p;
    }
    private boolean apply(Pickup item,Player p) {
        if(!p.alive())return false;
        PickupType type=item.type;
        // Previously spawned Speed boxes remain useful after another box reaches the cap.
        if(type==PickupType.SPEED_BOX && p.stats.speed()>=config.maxSpeed)type=PickupType.DAMAGE_BOX;
        String message;
        switch(type) {
            case DAMAGE_BOX:
                int gain=p.stats.addDamage(config.damageMin+rewards.nextInt(config.damageMax-config.damageMin+1));
                if(gain==0)return false;message=game.strings.format(PICKUP_DAMAGE,gain);break;
            case HEALTH_BOX:
                if(p.hp()==p.maxHp() || rewards.nextFloat()<config.maxHpChance) {
                    int maxGain=p.stats.addMaxHp(config.maxHpGain);p.heal(config.maxHpHeal);
                    if(maxGain==0)return false;message=game.strings.format(PICKUP_MAX_HP,maxGain);
                } else {int healed=p.heal(config.healthHeal);if(healed==0)return false;message=game.strings.format(PICKUP_HEAL,healed);}
                break;
            case SPEED_BOX:
                int speed=p.stats.addSpeed();if(speed==0)return false;message=game.strings.format(PICKUP_SPEED,speed);break;
            case HEALTH_DROP:
                int healed=p.heal(item.heal);if(healed==0)return false;message=game.strings.format(PICKUP_HEAL,healed);break;
            case COIN:
                if(collectedCoins<Integer.MAX_VALUE)collectedCoins++;
                message=game.strings.get(PICKUP_COIN);break;
            default: return false;
        }
        notice=message;noticeType=type;noticeTime=1.4f;noticeSerial++;
        if(type!=PickupType.COIN)audio.play(switch(type) {
            case DAMAGE_BOX -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_DAMAGE;
            case HEALTH_BOX -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_HEALTH;
            case SPEED_BOX -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_SPEED;
            default -> com.example.monstersurvivor.audio.AudioEvent.PICKUP_HEALTH_DROP;
        });return true;
    }
}
