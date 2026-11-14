import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;

/** Local numeric inspection only. Never exports licensed artwork to an AI service. */
public class InspectTiny {
    public static void main(String[] args) throws Exception {
        Path root=Path.of("asset_sources/tiny-questers-free-starter-pack/tiny-questers-free-starter-pack");
        for(String pack:List.of("warrior_free","slime_free","bat_free")) {
            try(var paths=Files.walk(root.resolve(pack+"/png/single"))) {
                var groups=new TreeMap<String,List<Path>>();
                paths.filter(p->p.toString().endsWith(".png")).forEach(p->groups.computeIfAbsent(p.getParent().toString(),k->new ArrayList<>()).add(p));
                for(var entry:groups.entrySet()) {
                    int minX=999,minY=999,maxX=-1,maxY=-1;var bottoms=new TreeSet<Integer>();
                    BufferedImage first=null;
                    for(Path p:entry.getValue()) {
                        BufferedImage im=ImageIO.read(p.toFile()); if(first==null)first=im;int bottom=-1;
                        for(int y=0;y<im.getHeight();y++)for(int x=0;x<im.getWidth();x++)if((im.getRGB(x,y)>>>24)>0){
                            minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);bottom=Math.max(bottom,y);
                        }bottoms.add(bottom);
                    }
                    System.out.println(root.relativize(Path.of(entry.getKey()))+" n="+entry.getValue().size()+" canvas="+first.getWidth()+"x"+first.getHeight()+" union="+minX+","+minY+".."+maxX+","+maxY+" bottoms="+bottoms);
                }
            }
        }
    }
}
