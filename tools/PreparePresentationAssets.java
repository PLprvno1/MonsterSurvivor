import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** CC0 Kenney sources only. Inspection sheets and lossless, extruded atlas extraction. */
public final class PreparePresentationAssets {
    static final Path OUT=Path.of("core/build/reports/task03b_1");
    static BufferedImage read(String name)throws Exception {return ImageIO.read(Path.of(name).toFile());}
    static void contact(BufferedImage src,int firstX,int firstY,int cols,int rows,int stride,String file)throws Exception {
        BufferedImage out=new BufferedImage(cols*76,rows*88,BufferedImage.TYPE_INT_RGB);Graphics2D g=out.createGraphics();
        g.setColor(new Color(34,36,45));g.fillRect(0,0,out.getWidth(),out.getHeight());g.setColor(Color.WHITE);
        for(int y=0;y<rows;y++)for(int x=0;x<cols;x++) {
            int sx=(x+firstX)*stride,sy=(y+firstY)*stride;
            g.drawImage(src,x*76+6,y*88+20,x*76+70,y*88+84,sx,sy,sx+16,sy+16,null);
            g.drawString((x+firstX)+","+(y+firstY),x*76+4,y*88+14);
        }
        g.dispose();ImageIO.write(out,"png",OUT.resolve(file).toFile());
    }
    static void pack(BufferedImage src,String file,String[] names,int[][] cells,int stride)throws Exception {
        BufferedImage atlas=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        StringBuilder json=new StringBuilder("{\n  \"license\": \"Kenney CC0\",\n  \"regions\": {\n");
        int x=2,y=2,rowHeight=0;
        for(int i=0;i<cells.length;i++) {
            int[] c=cells[i];int w=c.length>2?c[2]*16:16,h=c.length>3?c[3]*16:16;
            if(x+w+2>256) {x=2;y+=rowHeight+4;rowHeight=0;}
            if(y+h+2>256)throw new IllegalStateException("Atlas full");
            for(int v=-2;v<h+2;v++)for(int u=-2;u<w+2;u++) {
                int px=Math.max(0,Math.min(w-1,u)),py=Math.max(0,Math.min(h-1,v));
                int color=src.getRGB((c[0]+px/16)*stride+px%16,(c[1]+py/16)*stride+py%16);
                if(names[i].startsWith("ground_")) {
                    int mask=Integer.parseInt(names[i].substring(names[i].lastIndexOf('_')+1));
                    int edge=16;
                    if((mask&1)==0)edge=Math.min(edge,py);if((mask&2)==0)edge=Math.min(edge,15-px);
                    if((mask&4)==0)edge=Math.min(edge,15-py);if((mask&8)==0)edge=Math.min(edge,px);
                    // Three-pixel ordered transition into original grass, including extruded border.
                    int threshold=(px%2)*2+py%2;
                    if(edge<3 && edge*4/3<=threshold)color=src.getRGB(5*17+px,py);
                }
                atlas.setRGB(x+u,y+v,color);
            }
            json.append("    \"").append(names[i]).append("\": [").append(x).append(',').append(y).append(',').append(w).append(',').append(h).append(']').append(i==cells.length-1?"\n":",\n");
            x+=w+4;rowHeight=Math.max(rowHeight,h);
        }
        json.append("  },\n  \"sourceCells\": [");
        for(int i=0;i<cells.length;i++)json.append(java.util.Arrays.toString(cells[i])).append(i==cells.length-1?"":",");
        json.append("]\n}\n");
        ImageIO.write(atlas,"png",Path.of("assets/game/"+file+".png").toFile());Files.writeString(Path.of("assets/game/"+file+".json"),json.toString());
    }
    public static void main(String[] args)throws Exception {
        Files.createDirectories(OUT);
        BufferedImage dungeon=read("asset_sources/kenney_tiny-dungeon/Tilemap/tilemap_packed.png");
        contact(dungeon,0,0,12,11,16,"dungeon-cells.png");
        pack(dungeon,"dungeon-pickups",new String[]{"potion_red","potion_green","potion_blue","pickup_chest"},new int[][]{{7,9},{6,9},{8,9},{7,7}},16);
        BufferedImage world=read("asset_sources/kenney_roguelike-rpg-pack/Spritesheet/roguelikeSheet_transparent.png");
        contact(world,0,0,12,10,17,"world-ground-cells.png");
        contact(world,8,8,16,7,17,"world-trees-cells.png");
        contact(world,45,12,12,12,17,"world-props-cells.png");
        contact(world,24,0,16,12,17,"world-decor-cells.png");
        contact(world,24,12,16,12,17,"world-camp-cells.png");
        var names=new java.util.ArrayList<String>();var cells=new java.util.ArrayList<int[]>();
        String[] grounds={"dirt","stone","sand"};int[][] groundCells={{6,0},{7,0},{8,0}};
        for(int g=0;g<3;g++)for(int mask=0;mask<16;mask++) {names.add("ground_"+grounds[g]+"_"+mask);cells.add(groundCells[g]);}
        String[] props={"grass_variant","moss_ground","tree_teal","pine_teal","bush_flower","grass_tuft","flower_red","flower_white","dead_tree","rock_small","rock_moss","fence","campfire","camp_barrel","camp_stool"};
        int[][] propCells={{5,1},{9,1},{15,10,1,2},{18,10,1,2},{24,11},{22,10},{29,9},{31,9},{27,9,1,3},{55,21},{55,22},{48,23},{14,8},{51,16},{53,19}};
        for(int i=0;i<props.length;i++) {names.add(props[i]);cells.add(propCells[i]);}
        // Keep the entire ground/prop pass on one texture. Source pixels match the old atlas.
        String[] legacy={"grass","dirt","water","shore_nw","shore_n","shore_ne","shore_w","shore_e","shore_sw","shore_s","shore_se","tree","pine","bush","rock","stump"};
        int[][] legacyCells={{5,0},{6,0},{0,0},{2,0},{3,0},{4,0},{2,1},{4,1},{2,2},{3,2},{4,2},{13,10,1,2},{16,10,1,2},{19,9},{54,21},{53,20}};
        for(int i=0;i<legacy.length;i++) {names.add(legacy[i]);cells.add(legacyCells[i]);}
        pack(world,"world-expanded",names.toArray(String[]::new),cells.toArray(int[][]::new),17);
    }
}
