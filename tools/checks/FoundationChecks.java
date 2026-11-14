package com.example.monstersurvivor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.screen.GameScreen;
import com.example.monstersurvivor.world.CollisionGrid;

/** Standalone deterministic checks; intentionally outside Gradle's framework test source set. */
public final class FoundationChecks {
    private static int checks;
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
    }
    static void close(float actual, float expected, String label) { check(Math.abs(actual-expected)<.025f,label+": "+actual+" vs "+expected); }
    public static void main(String[] args) {
        CollisionGrid open = new CollisionGrid(100,100,16,new byte[10000]);
        for (int fps : new int[]{30,60,120}) {
            Player p=new Player(400,400);
            for (int i=0;i<fps;i++) p.update(1f/fps,.6f,.8f,open);
            close(p.x,448,"FPS-independent x at "+fps); close(p.y,464,"FPS-independent y at "+fps);
            p=new Player(400,400);
            for (int i=0;i<fps;i++) p.update(1f/fps,1,1,open);
            close((float)Math.hypot(p.x-400,p.y-400),80,"No diagonal speed boost");
        }
        for (int mask : new int[]{CollisionGrid.TREE,CollisionGrid.ROCK,CollisionGrid.WATER,CollisionGrid.BOUNDARY}) {
            byte[] data=new byte[100];
            for(int y=0;y<10;y++) data[y*10+5]=(byte)mask;
            CollisionGrid wall=new CollisionGrid(10,10,16,data);
            Player p=new Player(64,40);
            p.update(2,1,0,wall);
            check(p.x<=76.001f,"No tunneling through mask "+mask);
            check(wall.canOccupy(p.x,p.y,4,3),"Valid foot rectangle after collision");
            float oldX=p.x, oldY=p.y;
            for(int i=0;i<60;i++) p.update(1f/60,1,1,wall);
            check(p.x<=76.001f&&p.x>=oldX&&p.y>oldY,"Wall sliding");
        }
        Player edge=new Player(10,10);
        edge.update(5,-1,-1,open);
        check(edge.x>=4&&edge.y>=3,"Cannot leave negative map boundary");
        edge=new Player(1590,1590);edge.update(5,1,1,open);
        check(edge.x<=1596&&edge.y<=1597,"Cannot leave positive map boundary");
        check(!open.isWalkable(-1,5)&&!open.isWalkable(100,0),"Out-of-map navigation blocked");
        close(GameScreen.clampCamera(0,160,1600),160,"Camera left clamp");
        close(GameScreen.clampCamera(2000,160,1600),1440,"Camera right clamp");
        close(GameScreen.clampCamera(100,900,1600),800,"Camera on viewport larger than world");
        JsonValue map=new JsonReader().parse(new FileHandle("game/forest.json"));
        check(map.getInt("width")==100&&map.getInt("height")==100,"Map is 100x100");
        check(map.get("ground").size==10000&&map.get("collision").size==10000,"Serialized layer lengths");
        byte[] masks=new byte[10000];int[] counts=new int[16];
        JsonValue tile=map.get("collision").child;
        for(int i=0;i<10000;i++,tile=tile.next){ masks[i]=(byte)tile.asInt(); counts[masks[i]&15]++; }
        CollisionGrid forest=new CollisionGrid(100,100,16,masks);
        check(forest.canOccupy(map.getFloat("spawnX"),map.getFloat("spawnY"),4,3),"Spawn is clear");
        for(int y=44;y<=56;y++) for(int x=41;x<=59;x++) check(forest.isWalkable(x,y),"Central meadow clear");
        check(counts[1]>0&&counts[2]>0&&counts[4]>0,"Trees, rocks and water have collision data");
        boolean[] reached=new boolean[10000];int[] queue=new int[10000];int head=0,tail=1;queue[0]=5050;reached[5050]=true;
        while(head<tail){int id=queue[head++],x=id%100,y=id/100;
            int[] candidates={id-1,id+1,id-100,id+100};
            for(int next:candidates){int nx=next%100,ny=next/100;
                if(next>=0&&next<10000&&Math.abs(nx-x)+Math.abs(ny-y)==1&&!reached[next]&&forest.isWalkable(nx,ny)){
                    reached[next]=true;queue[tail++]=next;
                }
            }
        }
        check(reached[5050]&&reached[5001]&&reached[5098]&&reached[150]&&reached[9850],"Four corridors connected to spawn");
        int walkable=0;for(byte mask:masks)if(mask==0)walkable++;
        check(tail==walkable,"Every walkable navigation cell is reachable from spawn");
        System.out.println("FOUNDATION CHECKS PASSED: "+checks+" assertions; reachable cells="+tail);
    }
}
