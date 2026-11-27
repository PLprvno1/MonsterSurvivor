import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
/** Local lossless crop/packing of licensed source sheets. */
public class PackEvolutionFx {
    public static void main(String[] args)throws Exception {
        var out=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);int count=0;
        for(String line:Files.readAllLines(Path.of(args[0]))) {
            var s=line.split("\t");var image=ImageIO.read(Path.of(s[0]).toFile());
            int sx=Integer.parseInt(s[1]),sy=Integer.parseInt(s[2]),w=Integer.parseInt(s[3]),h=Integer.parseInt(s[4]),dx=Integer.parseInt(s[5]),dy=Integer.parseInt(s[6]);
            for(int y=0;y<h;y++)for(int x=0;x<w;x++)out.setRGB(dx+x,dy+y,image.getRGB(sx+x,sy+y));count++;
        }
        out.setRGB(255,255,0xffffffff); // Shared primitive texel: sprites and geometry batch on one texture.
        ImageIO.write(out,"png",Path.of(args[1]).toFile());var decoded=ImageIO.read(Path.of(args[1]).toFile());
        for(int y=0;y<256;y++)for(int x=0;x<256;x++)if(out.getRGB(x,y)!=decoded.getRGB(x,y))throw new AssertionError("Round trip");
        System.out.println("Packed "+count+" source frames losslessly");
    }
}
