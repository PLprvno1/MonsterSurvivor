package com.example.monstersurvivor.world;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;

/** Bounded cluster placement with wide buffers, safe roads and validated connectivity. No GL/IO. */
public final class ForestGenerator {
    public static final int WIDTH=150,HEIGHT=150,TILE_SIZE=16,CELLS=WIDTH*HEIGHT,CENTER_X=WIDTH/2,CENTER_Y=HEIGHT/2,SPAWN_CELL=CENTER_Y*WIDTH+CENTER_X;
    private static final float MAP_SCALE=WIDTH/200f;
    private static final int MAX_INTERIOR_BLOCKED=4000*CELLS/(200*200),MAX_REPAIRS=12;
    private static final int ROAD_RADIUS=Math.max(1,(int)(2*MAP_SCALE));
    private static int scaled(int value) {return Math.max(1,Math.round(value*MAP_SCALE));}
    public static final class Prop {
        public final String region;
        public final float x,y,scale;
        Prop(String region,int col,int row,float scale) {this.region=region;x=col*TILE_SIZE+TILE_SIZE/2;y=row*TILE_SIZE+2;this.scale=scale;}
    }
    public static final class Layout {
        public final long seed;
        public final CollisionGrid grid;
        public final String[] terrain;
        public final Array<Prop> props;
        public final int walkableTiles,repairs;
        public final boolean fallback;
        private Layout(Builder b,boolean fallback) {
            seed=b.seed;grid=new CollisionGrid(WIDTH,HEIGHT,TILE_SIZE,b.masks);terrain=b.terrain;props=b.props;
            int free=0;for(byte mask:b.masks)if(mask==0)free++;
            walkableTiles=free;repairs=b.repairs;this.fallback=fallback;
        }
    }
    private ForestGenerator() { }
    public static Layout generate(long seed) {
        Builder b=new Builder(seed);b.base();b.biomes();b.islands(CollisionGrid.WATER,16,3,5,2,4);
        b.islands(CollisionGrid.TREE,104,2,4,2,4);b.islands(CollisionGrid.ROCK,40,1,2,1,2);
        if(!b.repair() || b.freeCount()<CELLS*.85f)return fallback(seed);
        b.finish();return new Layout(b,false);
    }
    /** Guaranteed connected, open forest-edge fallback. No recursion or unbounded retries. */
    public static Layout fallback(long seed) {Builder b=new Builder(seed);b.base();b.finish();return new Layout(b,true);}
    private static final class Builder {
        final long seed;
        final RandomXS128 random;
        final byte[] masks=new byte[CELLS];
        final String[] terrain=new String[CELLS];
        final byte[] biome=new byte[CELLS];
        final boolean[] protectedTiles=new boolean[CELLS],reached=new boolean[CELLS];
        final int[] queue=new int[CELLS];
        final Array<Prop> props=new Array<>(6000);
        int blockedInterior,repairs;
        Builder(long seed) {this.seed=seed;random=new RandomXS128(seed);}
        void base() {
            Arrays.fill(terrain,"grass");
            for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++) {
                int id=y*WIDTH+x;if(x==0 || y==0 || x==WIDTH-1 || y==HEIGHT-1)masks[id]=CollisionGrid.BOUNDARY;
                float dx=(x-CENTER_X)/(22f*MAP_SCALE),dy=(y-CENTER_Y)/(18f*MAP_SCALE);if(dx*dx+dy*dy<=1)protectedTiles[id]=true;
            }
            float h=random.nextFloat()*6.283185f,v=random.nextFloat()*6.283185f;
            for(int i=1;i<WIDTH-1;i++) {
                float normalized=(i-CENTER_X)/MAP_SCALE,envelope=Math.min(1,Math.abs(normalized)/20f);
                int row=CENTER_Y+Math.round(scaled(3)*(float)Math.sin(normalized*.09f+h)*envelope),col=CENTER_X+Math.round(scaled(3)*(float)Math.sin(normalized*.085f+v)*envelope);
                for(int offset=-ROAD_RADIUS;offset<=ROAD_RADIUS;offset++) {road(i,row+offset);road(col+offset,i);}
            }
            // Secondary wide routes connect distant quadrants without narrow chokes.
            for(int i=1;i<WIDTH-1;i++)for(int offset=-ROAD_RADIUS;offset<=ROAD_RADIUS;offset++) {
                road(i,HEIGHT/4+offset);road(i,HEIGHT*3/4+offset);
                road(WIDTH/4+offset,i);road(WIDTH*3/4+offset,i);
            }
        }
        void biomes() {
            for(int patch=0;patch<32;patch++) {
                int margin=scaled(12),minRx=scaled(7),rxRange=scaled(18)-minRx+1,minRy=scaled(6),ryRange=scaled(15)-minRy+1;
                int cx=margin+random.nextInt(WIDTH-2*margin),cy=margin+random.nextInt(HEIGHT-2*margin);
                int rx=minRx+random.nextInt(rxRange),ry=minRy+random.nextInt(ryRange);
                byte kind=(byte)(2+patch%3);
                for(int y=Math.max(1,cy-ry);y<=Math.min(HEIGHT-2,cy+ry);y++)for(int x=Math.max(1,cx-rx);x<=Math.min(WIDTH-2,cx+rx);x++)
                    if(!protectedTiles[y*WIDTH+x] && inside(x,y,cx,cy,rx,ry))biome[y*WIDTH+x]=kind;
            }
        }
        void road(int x,int y) {
            if(x<1 || y<1 || x>WIDTH-2 || y>HEIGHT-2)return;
            int id=y*WIDTH+x;masks[id]=0;protectedTiles[id]=true;biome[id]=1;terrain[id]="dirt";
        }
        void islands(int mask,int count,int minRx,int maxRx,int minRy,int maxRy) {
            minRx=scaled(minRx);maxRx=scaled(maxRx);minRy=scaled(minRy);maxRy=scaled(maxRy);
            int margin=scaled(6);
            for(int island=0;island<count;island++)for(int attempt=0;attempt<40;attempt++) {
                int cx=margin+random.nextInt(WIDTH-2*margin),cy=margin+random.nextInt(HEIGHT-2*margin),rx=minRx+random.nextInt(maxRx-minRx+1),ry=minRy+random.nextInt(maxRy-minRy+1);
                if(!room(cx,cy,rx,ry))continue;int area=0;
                for(int y=cy-ry;y<=cy+ry;y++)for(int x=cx-rx;x<=cx+rx;x++)if(inside(x,y,cx,cy,rx,ry))area++;
                if(blockedInterior+area>MAX_INTERIOR_BLOCKED)continue;
                for(int y=cy-ry;y<=cy+ry;y++)for(int x=cx-rx;x<=cx+rx;x++)if(inside(x,y,cx,cy,rx,ry))masks[y*WIDTH+x]=(byte)mask;
                blockedInterior+=area;break;
            }
        }
        boolean room(int cx,int cy,int rx,int ry) {
            int buffer=scaled(3);
            if(cx-rx-buffer<1 || cy-ry-buffer<1 || cx+rx+buffer>WIDTH-2 || cy+ry+buffer>HEIGHT-2)return false;
            for(int y=cy-ry-buffer;y<=cy+ry+buffer;y++)for(int x=cx-rx-buffer;x<=cx+rx+buffer;x++)
                if(masks[y*WIDTH+x]!=0 || protectedTiles[y*WIDTH+x])return false;
            return true;
        }
        boolean inside(int x,int y,int cx,int cy,int rx,int ry) {
            float dx=(x-cx)/(float)rx,dy=(y-cy)/(float)ry;return dx*dx+dy*dy<=.97f+(hash(x,y)-.5f)*.12f;
        }
        float hash(int x,int y) {
            long v=seed^((long)x*0x9e3779b97f4a7c15L)^((long)y*0xc2b2ae3d27d4eb4fL);
            v=(v^(v>>>30))*0xbf58476d1ce4e5b9L;v=(v^(v>>>27))*0x94d049bb133111ebL;
            return ((v^(v>>>31))>>>40)/(float)(1<<24);
        }
        int freeCount() {int n=0;for(byte mask:masks)if(mask==0)n++;return n;}
        int flood() {
            Arrays.fill(reached,false);int head=0,tail=1;queue[0]=SPAWN_CELL;reached[SPAWN_CELL]=true;
            while(head<tail) {
                int id=queue[head++],x=id%WIDTH,y=id/WIDTH;
                if(x>0)tail=visit(id-1,tail);if(x<WIDTH-1)tail=visit(id+1,tail);if(y>0)tail=visit(id-WIDTH,tail);if(y<HEIGHT-1)tail=visit(id+WIDTH,tail);
            }
            return tail;
        }
        int visit(int id,int tail) {if(masks[id]==0 && !reached[id]) {reached[id]=true;queue[tail++]=id;}return tail;}
        boolean repair() {
            for(int attempt=0;attempt<=MAX_REPAIRS;attempt++) {
                if(flood()==freeCount())return true;if(attempt==MAX_REPAIRS)return false;
                int cell=0;while(cell<CELLS && (masks[cell]!=0 || reached[cell]))cell++;
                int x=cell%WIDTH,y=cell/WIDTH;
                for(int col=Math.min(x,CENTER_X);col<=Math.max(x,CENTER_X);col++)for(int offset=-1;offset<=1;offset++)road(col,y+offset);
                for(int row=Math.min(y,CENTER_Y);row<=Math.max(y,CENTER_Y);row++)for(int offset=-1;offset<=1;offset++)road(CENTER_X+offset,row);
                repairs++;
            }
            return false;
        }
        void finish() {
            String[] decorative={"bush","stump","bush_flower","grass_tuft","flower_red","flower_white","rock_small","fence","dead_tree"};
            String[] trees={"tree","pine","tree_teal","pine_teal"};
            String[][] grounds=new String[3][16];
            for(int kind=1;kind<=3;kind++)for(int mask=0;mask<16;mask++)grounds[kind-1][mask]="ground_"+(kind==1?"dirt":kind==2?"stone":"sand")+"_"+mask;
            for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++) {
                int id=y*WIDTH+x,mask=masks[id];
                int kind=biome[id];
                if(kind>=1 && kind<=3) {
                    int neighbors=(same(x,y+1,kind)?1:0)|(same(x+1,y,kind)?2:0)|(same(x,y-1,kind)?4:0)|(same(x-1,y,kind)?8:0);
                    terrain[id]=grounds[kind-1][neighbors];
                } else if(kind==4)terrain[id]="grass_variant";
                if(mask==CollisionGrid.WATER) {
                    String vertical=!water(x,y-1)?"s":!water(x,y+1)?"n":"",horizontal=!water(x-1,y)?"w":!water(x+1,y)?"e":"";
                    terrain[id]=vertical.isEmpty() && horizontal.isEmpty()?"water":"shore_"+vertical+horizontal;
                } else if(mask==CollisionGrid.TREE || mask==CollisionGrid.BOUNDARY) {
                    props.add(new Prop(trees[(int)(hash(x,y)*trees.length)],x,y,1));
                } else if(mask==CollisionGrid.ROCK)props.add(new Prop(hash(x,y)<.5f?"rock":"rock_moss",x,y,1));
                else if(!protectedTiles[id] && hash(x+311,y+137)<.045f) {
                    // These are explicitly decoration: they never write the collision mask.
                    String region=decorative[(int)(hash(x+557,y+19)*decorative.length)];
                    props.add(new Prop(region,x,y,region.equals("dead_tree")?.75f:1));
                }
            }
            // A few small camps on non-road, non-blocking cells; no fake collectible props.
            int camps=0;
            int margin=scaled(12),stepY=scaled(11),stepX=scaled(13);
            for(int y=margin;y<HEIGHT-margin && camps<8;y+=stepY)for(int x=margin;x<WIDTH-margin && camps<8;x+=stepX) {
                int id=y*WIDTH+x;if(protectedTiles[id] || masks[id]!=0 || masks[id+1]!=0 || masks[id-1]!=0 || hash(x+51,y+33)>.1f)continue;
                props.add(new Prop("campfire",x,y,1));props.add(new Prop("camp_barrel",x+1,y,1));props.add(new Prop("camp_stool",x-1,y,1));camps++;
            }
        }
        boolean same(int x,int y,int kind) {return x>=0 && y>=0 && x<WIDTH && y<HEIGHT && biome[y*WIDTH+x]==kind;}
        boolean water(int x,int y) {return x>=0 && y>=0 && x<WIDTH && y<HEIGHT && masks[y*WIDTH+x]==CollisionGrid.WATER;}
    }
}
