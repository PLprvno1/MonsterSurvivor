import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Local lossless packing. Pixel data never leaves this process or the filesystem. */
public class PackPng {
    public static void main(String[] args) throws Exception {
        BufferedImage out=new BufferedImage(Integer.parseInt(args[2]),Integer.parseInt(args[3]),BufferedImage.TYPE_INT_ARGB);
        int frames=0;
        var sheets=new java.util.HashMap<String,BufferedImage>();
        for(String line:Files.readAllLines(Path.of(args[0]))){
            String[] s=line.split("\t");BufferedImage im=ImageIO.read(Path.of(s[0]).toFile());
            int dx=Integer.parseInt(s[1]),dy=Integer.parseInt(s[2]),w=Integer.parseInt(s[3]),h=Integer.parseInt(s[4]);
            if(im.getWidth()!=w||im.getHeight()!=h)throw new IllegalArgumentException("Unexpected cell: "+s[0]);
            if(s.length==8) {
                BufferedImage original=sheets.get(s[5]);
                if(original==null){original=ImageIO.read(Path.of(s[5]).toFile());sheets.put(s[5],original);}
                int ax=Integer.parseInt(s[6]),ay=Integer.parseInt(s[7]);
                for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(original.getRGB(ax+x,ay+y)!=im.getRGB(x,y))
                    throw new AssertionError("Single/atlas differ: "+s[0]);
            }
            for(int y=-2;y<h+2;y++)for(int x=-2;x<w+2;x++)
                out.setRGB(dx+x,dy+y,im.getRGB(Math.max(0,Math.min(w-1,x)),Math.max(0,Math.min(h-1,y))));
            // Verify the packed cell is lossless before writing the sheet.
            for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(out.getRGB(dx+x,dy+y)!=im.getRGB(x,y))throw new AssertionError("Pixel copy");
            frames++;
        }
        Path target=Path.of(args[1]);Files.createDirectories(target.getParent());ImageIO.write(out,"png",target.toFile());
        BufferedImage decoded=ImageIO.read(target.toFile());
        for(int y=0;y<out.getHeight();y++)for(int x=0;x<out.getWidth();x++)if(decoded.getRGB(x,y)!=out.getRGB(x,y))throw new AssertionError("PNG round trip");
        System.out.println("Packed "+frames+" source frames losslessly -> "+target+" "+out.getWidth()+"x"+out.getHeight());
    }
}
