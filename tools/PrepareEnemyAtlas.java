import com.badlogic.gdx.utils.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Lossless native MC cells; explicit uniform Nearest adapter for CC0 SpriteCanvas only. */
public final class PrepareEnemyAtlas {
    static final int PAGE=2048,PAD=2;
    static final Path OUT=Path.of("core/build/reports/task04a"),ASSETS=Path.of("assets/game");
    static final LinkedHashMap<String,BufferedImage> images=new LinkedHashMap<>(16,.75f,true) {
        protected boolean removeEldestEntry(Map.Entry<String,BufferedImage> e) {return size()>6;}
    };
    static BufferedImage read(String path)throws Exception {
        BufferedImage result=images.get(path);
        if(result==null) {result=ImageIO.read(Path.of(path).toFile());if(result==null)throw new IllegalArgumentException("Cannot decode "+path);images.put(path,result);}return result;
    }
    static final class Cell {
        final int w,h;final int[] pixels;int page,x,y;
        Cell(int w,int h,int[] pixels) {this.w=w;this.h=h;this.pixels=pixels;}
        public int hashCode() {return (w*31+h)*31+Arrays.hashCode(pixels);}
        public boolean equals(Object o) {return o instanceof Cell c && w==c.w && h==c.h && Arrays.equals(pixels,c.pixels);}
    }
    static Cell crop(JsonValue frame)throws Exception {
        BufferedImage image=read(frame.getString("source"));JsonValue r=frame.get("rect");
        int x=r.getInt(0),y=r.getInt(1),sw=r.getInt(2),sh=r.getInt(3);double factor=frame.getDouble("factor",1);
        if(x<0 || y<0 || sw<=0 || sh<=0 || x+sw>image.getWidth() || y+sh>image.getHeight())throw new IllegalArgumentException("Invalid source frame "+frame);
        int w=(int)Math.ceil(sw*factor-1e-9),h=(int)Math.ceil(sh*factor-1e-9);int[] pixels=new int[w*h];
        for(int v=0;v<h;v++)for(int u=0;u<w;u++)pixels[v*w+u]=image.getRGB(x+Math.min(sw-1,(int)(u/factor)),y+Math.min(sh-1,(int)(v/factor)));
        return new Cell(w,h,pixels);
    }
    static boolean mirror(Cell left,Cell right) {
        if(left.w!=right.w || left.h!=right.h)return false;
        for(int y=0;y<left.h;y++)for(int x=0;x<left.w;x++)if(left.pixels[y*left.w+x]!=right.pixels[y*right.w+right.w-1-x])return false;return true;
    }
    static JsonValue array(int...numbers) {JsonValue a=new JsonValue(JsonValue.ValueType.array);for(int n:numbers)a.addChild(new JsonValue(n));return a;}
    static void set(JsonValue o,String key,JsonValue v) {o.remove(key);o.addChild(key,v);}
    static int pow2(int n) {int v=1;while(v<n)v*=2;return v;}
    public static void main(String[] args)throws Exception {
        JsonValue plan=new JsonReader().parse(Files.readString(Path.of(args[0])));boolean audit=Arrays.asList(args).contains("--audit-only");
        int strips=0,compared=0,sourceFrames=0,mirrorClips=0,empty=0;
        for(JsonValue c=plan.get("stripChecks").child;c!=null;c=c.next) {
            BufferedImage sheet=read(c.getString("sheet")),strip=read(c.getString("strip"));int sx=c.getInt("x"),sy=c.getInt("y"),w=c.getInt("w"),h=c.getInt("h");
            if(strip.getWidth()!=w || strip.getHeight()!=h)throw new IllegalArgumentException("Strip size");
            for(int y=0;y<h;y++)for(int x=0;x<w;x++) {if(sheet.getRGB(sx+x,sy+y)!=strip.getRGB(x,y))throw new IllegalArgumentException("Strip/sheet pixel mismatch: "+c.getString("strip"));compared++;}strips++;
        }
        Map<Cell,Cell> unique=new HashMap<>();Map<JsonValue,List<Cell>> frames=new IdentityHashMap<>();
        for(JsonValue sprite=plan.get("sprites").child;sprite!=null;sprite=sprite.next) {
            int nonempty=0;
            for(JsonValue anim=sprite.get("animations").child;anim!=null;anim=anim.next) {
                List<Cell> cells=new ArrayList<>();
                for(JsonValue f=anim.get("frames").child;f!=null;f=f.next) {
                    Cell cell=crop(f);if(cell.w!=sprite.getInt("frameWidth") || cell.h!=sprite.getInt("frameHeight"))throw new IllegalArgumentException("Normalized canvas mismatch "+sprite.getString("id"));
                    cells.add(cell);sourceFrames++;boolean alpha=false;for(int p:cell.pixels)if((p>>>24)!=0) {alpha=true;break;}if(alpha)nonempty++;else empty++;
                }
                if(anim.getBoolean("tryMirror",false)) {
                    JsonValue right=sprite.get("animations").child;
                    while(right!=null && !(right.getString("state").equals(anim.getString("state")) && right.getString("direction").equals("right")))right=right.next;
                    List<Cell> candidates=frames.get(right);boolean matches=candidates!=null && candidates.size()==cells.size();
                    if(matches)for(int i=0;i<cells.size();i++)if(!mirror(cells.get(i),candidates.get(i))) {matches=false;break;}
                    if(matches) {cells=candidates;set(anim,"mirrorX",new JsonValue(true));mirrorClips++;}
                }
                List<Cell> stored=new ArrayList<>();for(Cell cell:cells) {Cell found=unique.putIfAbsent(cell,cell);stored.add(found==null?cell:found);}frames.put(anim,stored);
            }
            if(nonempty==0)throw new IllegalArgumentException("Empty creature: "+sprite.getString("id"));
        }
        for(JsonValue b=plan.get("bosses").child;b!=null;b=b.next)read(b.getString("source"));
        int fx=0;for(JsonValue f=plan.get("fx").child;f!=null;f=f.next) {read(f.getString("file"));fx++;}
        JsonValue validation=new JsonValue(JsonValue.ValueType.object);
        validation.addChild("stripSheetsCompared",new JsonValue(strips));validation.addChild("stripPixelsCompared",new JsonValue(compared));
        validation.addChild("sourceFramesChecked",new JsonValue(sourceFrames));validation.addChild("uniqueRuntimeCells",new JsonValue(unique.size()));
        validation.addChild("verifiedMirroredClips",new JsonValue(mirrorClips));validation.addChild("emptyFrameReferences",new JsonValue(empty));validation.addChild("fxAndBestiaryPngDecoded",new JsonValue(fx));
        validation.addChild("imported",new JsonValue(!audit));
        if(!audit) {
            List<Cell> ordered=new ArrayList<>(unique.values());ordered.sort(Comparator.comparingInt((Cell c)->c.h).reversed()
                .thenComparing(Comparator.comparingInt((Cell c)->c.w).reversed()).thenComparing((a,b)->Arrays.compareUnsigned(a.pixels,b.pixels)));
            List<BufferedImage> pages=new ArrayList<>();List<int[]> extents=new ArrayList<>();int page=-1,x=PAD,y=PAD,row=0;
            for(Cell c:ordered) {
                if(page<0 || y+c.h+PAD>PAGE) {page++;pages.add(new BufferedImage(PAGE,PAGE,BufferedImage.TYPE_INT_ARGB));extents.add(new int[2]);x=y=PAD;row=0;}
                if(x+c.w+PAD>PAGE) {x=PAD;y+=row+PAD*2;row=0;}
                if(y+c.h+PAD>PAGE) {page++;pages.add(new BufferedImage(PAGE,PAGE,BufferedImage.TYPE_INT_ARGB));extents.add(new int[2]);x=y=PAD;row=0;}
                c.page=page;c.x=x;c.y=y;BufferedImage target=pages.get(page);
                for(int v=-PAD;v<c.h+PAD;v++)for(int u=-PAD;u<c.w+PAD;u++)target.setRGB(x+u,y+v,c.pixels[Math.max(0,Math.min(c.h-1,v))*c.w+Math.max(0,Math.min(c.w-1,u))]);
                extents.get(page)[0]=Math.max(extents.get(page)[0],x+c.w+PAD);extents.get(page)[1]=Math.max(extents.get(page)[1],y+c.h+PAD);
                x+=c.w+PAD*2;row=Math.max(row,c.h);
            }
            JsonValue runtime=new JsonValue(JsonValue.ValueType.object),textures=new JsonValue(JsonValue.ValueType.array),textureInfo=new JsonValue(JsonValue.ValueType.array);
            long rgba=0;int extrusionChecks=0;
            for(int i=0;i<pages.size();i++) {
                int w=pow2(extents.get(i)[0]),h=pow2(extents.get(i)[1]);String name="game/enemies-atlas-"+i+".png";
                ImageIO.write(pages.get(i).getSubimage(0,0,w,h),"png",Path.of("assets").resolve(name).toFile());textures.addChild(new JsonValue(name));
                JsonValue info=new JsonValue(JsonValue.ValueType.object);info.addChild("file",new JsonValue(name));info.addChild("width",new JsonValue(w));info.addChild("height",new JsonValue(h));info.addChild("rgbaBytes",new JsonValue((long)w*h*4));textureInfo.addChild(info);rgba+=(long)w*h*4;
            }
            for(Cell c:ordered)for(int v=-PAD;v<c.h+PAD;v++)for(int u=-PAD;u<c.w+PAD;u++) {
                if(pages.get(c.page).getRGB(c.x+u,c.y+v)!=c.pixels[Math.max(0,Math.min(c.h-1,v))*c.w+Math.max(0,Math.min(c.w-1,u))])throw new AssertionError("Extrusion overwritten");extrusionChecks++;
            }
            JsonValue sprites=plan.remove("sprites");
            for(JsonValue sprite=sprites.child;sprite!=null;sprite=sprite.next) {
                int spritePage=frames.get(sprite.get("animations").child).get(0).page;
                for(JsonValue anim=sprite.get("animations").child;anim!=null;anim=anim.next) {
                    JsonValue packed=new JsonValue(JsonValue.ValueType.array);
                    // A clip can cross pages; each frame explicitly identifies its texture.
                    for(Cell c:frames.get(anim)) {
                        if(c.page==spritePage)packed.addChild(array(c.x,c.y,c.w,c.h));
                        else {JsonValue f=new JsonValue(JsonValue.ValueType.object);f.addChild("rect",array(c.x,c.y,c.w,c.h));f.addChild("texture",new JsonValue("game/enemies-atlas-"+c.page+".png"));packed.addChild(f);}
                    }
                    set(anim,"frames",packed);anim.remove("tryMirror");
                }
                sprite.addChild("texture",new JsonValue("game/enemies-atlas-"+spritePage+".png"));
            }
            runtime.addChild("textures",textures);runtime.addChild("sprites",sprites);runtime.addChild("definitions",plan.get("definitions"));
            Files.writeString(ASSETS.resolve("enemy-catalog.json"),runtime.prettyPrint(JsonWriter.OutputType.json,140));
            validation.addChild("textures",textureInfo);validation.addChild("rgbaBytes",new JsonValue(rgba));validation.addChild("pixelAndExtrusionChecks",new JsonValue(extrusionChecks));
            StringBuilder java=new StringBuilder("package com.example.monstersurvivor.enemy;\n\n/** Generated by tools/prepare_enemy_assets.py; tune tools/enemy_balance.json, then rebuild. */\nfinal class EnemyCatalogData {\n    static String json() {return \"[\"+String.join(\",\",new String[]{\n");
            for(JsonValue d=plan.get("definitions").child;d!=null;d=d.next) {String value=d.toJson(JsonWriter.OutputType.json).replace("\\","\\\\").replace("\"","\\\"");java.append("        \"").append(value).append("\"").append(d.next==null?"\n":",\n");}
            java.append("    })+\"]\";}\n    private EnemyCatalogData() {}\n}\n");
            Path dest=Path.of("core/src/main/java/com/example/monstersurvivor/enemy/EnemyCatalogData.java");Files.createDirectories(dest.getParent());Files.writeString(dest,java);
            Files.copy(Path.of("asset_sources/MonstersCreatures_v1.0/MonstersCreatures/LICENSE.txt"),ASSETS.resolve("licenses/MonstersCreatures.txt"),StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Path.of("asset_sources/spritecanvas-monsters-cc0/monster-goblin/LICENSE.txt"),ASSETS.resolve("licenses/SpriteCanvas-CC0.txt"),StandardCopyOption.REPLACE_EXISTING);
        }
        Files.writeString(OUT.resolve("asset-validation.json"),validation.prettyPrint(JsonWriter.OutputType.json,120));
        System.out.println(validation.prettyPrint(JsonWriter.OutputType.json,120));
    }
}
