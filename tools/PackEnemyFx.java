import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
/** Deterministic lossless crop and two-pixel edge extrusion; no AI processing or resampling. */
public class PackEnemyFx {
    public static void main(String[] args)throws Exception{
        var atlas=new BufferedImage(512,512,BufferedImage.TYPE_INT_ARGB);int count=0;
        for(String line:Files.readAllLines(Path.of(args[0]))){
            var a=line.split("\t");var image=ImageIO.read(Path.of(a[0]).toFile());if(image==null)throw new IllegalArgumentException(a[0]);
            int sx=Integer.parseInt(a[1]),w=Integer.parseInt(a[2]),h=Integer.parseInt(a[3]),dx=Integer.parseInt(a[4]),dy=Integer.parseInt(a[5]);
            for(int y=-2;y<h+2;y++)for(int x=-2;x<w+2;x++)atlas.setRGB(dx+x,dy+y,image.getRGB(sx+Math.max(0,Math.min(w-1,x)),Math.max(0,Math.min(h-1,y))));count++;
        }
        atlas.setRGB(511,511,0xffffffff);ImageIO.write(atlas,"png",Path.of(args[1]).toFile());
        var decoded=ImageIO.read(Path.of(args[1]).toFile());
        for(int y=0;y<512;y++)for(int x=0;x<512;x++)if(atlas.getRGB(x,y)!=decoded.getRGB(x,y))throw new AssertionError("PNG roundtrip");
        System.out.println("Losslessly packed/extruded "+count+" frames into 512x512");
    }
}
