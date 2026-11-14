package com.example.monstersurvivor.world;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;

/** Bounded cluster placement with wide buffers, safe roads and validated connectivity. No GL/IO. */
public final class ForestGenerator {
    public static final int WIDTH=200,HEIGHT=200,TILE_SIZE=16,CELLS=WIDTH*HEIGHT,CENTER_X=WIDTH/2,CENTER_Y=HEIGHT/2,SPAWN_CELL=CENTER_Y*WIDTH+CENTER_X;
    private static final int MAX_INTERIOR_BLOCKED=4000,MAX_REPAIRS=12;
    public static final class Prop {
        public final String region;
        public final float x,y,scale;
        Prop(String region,int col,int row,float scale) {this.region=region;x=col*16+8;y=row*16+2;this.scale=scale;}
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
                float dx=(x-CENTER_X)/22f,dy=(y-CENTER_Y)/18f;if(dx*dx+dy*dy<=1)protectedTiles[id]=true;
            }
            float h=random.nextFloat()*6.283185f,v=random.nextFloat()*6.283185f;
            for(int i=1;i<WIDTH-1;i++) {
                float envelope=Math.min(1,Math.abs(i-CENTER_X)/20f);
                int row=CENTER_Y+Math.round(3*(float)Math.sin((i-CENTER_X)*.09f+h)*envelope),col=CENTER_X+Math.round(3*(float)Math.sin((i-CENTER_X)*.085f+v)*envelope);
                for(int offset=-2;offset<=2;offset++) {road(i,row+offset);road(col+offset,i);}
            }
            // Secondary wide routes connect distant quadrants without narrow chokes.
            for(int i=1;i<WIDTH-1;i++)for(int offset=-2;offset<=2;offset++) {
                road(i,HEIGHT/4+offset);road(i,HEIGHT*3/4+offset);
                road(WIDTH/4+offset,i);road(WIDTH*3/4+offset,i);
            }
        }
        void biomes() {
            for(int patch=0;patch<32;patch++) {
                int cx=12+random.nextInt(WIDTH-24),cy=12+random.nextInt(HEIGHT-24),rx=7+random.nextInt(12),ry=6+random.nextInt(10);
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
            for(int island=0;island<count;island++)for(int attempt=0;attempt<40;attempt++) {
                int cx=6+random.nextInt(WIDTH-12),cy=6+random.nextInt(HEIGHT-12),rx=minRx+random.nextInt(maxRx-minRx+1),ry=minRy+random.nextInt(maxRy-minRy+1);
                if(!room(cx,cy,rx,ry))continue;int area=0;
                for(int y=cy-ry;y<=cy+ry;y++)for(int x=cx-rx;x<=cx+rx;x++)if(inside(x,y,cx,cy,rx,ry))area++;
                if(blockedInterior+area>MAX_INTERIOR_BLOCKED)continue;
                for(int y=cy-ry;y<=cy+ry;y++)for(int x=cx-rx;x<=cx+rx;x++)if(inside(x,y,cx,cy,rx,ry))masks[y*WIDTH+x]=(byte)mask;
                blockedInterior+=area;break;
            }
        }
        boolean room(int cx,int cy,int rx,int ry) {
            if(cx-rx-3<1 || cy-ry-3<1 || cx+rx+3>WIDTH-2 || cy+ry+3>HEIGHT-2)return false;
            for(int y=cy-ry-3;y<=cy+ry+3;y++)for(int x=cx-rx-3;x<=cx+rx+3;x++)
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
            for(int y=12;y<HEIGHT-12 && camps<8;y+=11)for(int x=12;x<WIDTH-12 && camps<8;x+=13) {
                int id=y*WIDTH+x;if(protectedTiles[id] || masks[id]!=0 || masks[id+1]!=0 || masks[id-1]!=0 || hash(x+51,y+33)>.1f)continue;
                props.add(new Prop("campfire",x,y,1));props.add(new Prop("camp_barrel",x+1,y,1));props.add(new Prop("camp_stool",x-1,y,1));camps++;
            }
        }
        boolean same(int x,int y,int kind) {return x>=0 && y>=0 && x<WIDTH && y<HEIGHT && biome[y*WIDTH+x]==kind;}
        boolean water(int x,int y) {return x>=0 && y>=0 && x<WIDTH && y<HEIGHT && masks[y*WIDTH+x]==CollisionGrid.WATER;}
    }
}
