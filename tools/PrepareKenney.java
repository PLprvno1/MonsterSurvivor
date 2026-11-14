import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.util.*;

/** Deterministic CC0 extraction with two-pixel edge extrusion per region. */
public class PrepareKenney {
    public static void main(String[] args) throws Exception {
        Path out=Path.of("assets/game");Files.createDirectories(out);
        BufferedImage src=ImageIO.read(Path.of("asset_sources/kenney_roguelike-rpg-pack/Spritesheet/roguelikeSheet_transparent.png").toFile());
        BufferedImage atlas=new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);
        String[] names={"grass","dirt","water","shore_nw","shore_n","shore_ne","shore_w","shore_e","shore_sw","shore_s","shore_se","tree","pine","bush","rock","stump"};
        int[][] cells={{5,0,1},{6,0,1},{0,0,1},{2,0,1},{3,0,1},{4,0,1},{2,1,1},{4,1,1},{2,2,1},{3,2,1},{4,2,1},{13,10,2},{16,10,2},{19,9,1},{54,21,1},{53,20,1}};
        int x=2,y=2,rowHeight=0;StringBuilder rects=new StringBuilder();
        for(int i=0;i<cells.length;i++){
            int[] c=cells[i];int h=c[2]*16;
            if(x+18>128){x=2;y+=rowHeight+4;rowHeight=0;}
            for(int v=-2;v<h+2;v++)for(int u=-2;u<18;u++){
                int px=Math.max(0,Math.min(15,u)),py=Math.max(0,Math.min(h-1,v));
                atlas.setRGB(x+u,y+v,src.getRGB(c[0]*17+px,(c[1]+py/16)*17+py%16));
            }
            rects.append(names[i]).append('\t').append(x).append('\t').append(y).append("\t16\t").append(h).append('\n');
            x+=20;rowHeight=Math.max(rowHeight,h);
        }
        ImageIO.write(atlas,"png",out.resolve("world.png").toFile());
        Path plan=Path.of("core/build/asset-tools");Files.createDirectories(plan);Files.writeString(plan.resolve("world-regions.tsv"),rects);
        System.out.println("Prepared extruded Kenney terrain atlas 128x128; original cells unchanged.");
    }
}
