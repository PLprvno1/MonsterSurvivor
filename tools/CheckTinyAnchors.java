import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
/** Reports best discrete pixel translations only; no image output. */
public class CheckTinyAnchors {
    public static void main(String[] args) throws Exception {
        Path root=Path.of("asset_sources/tiny-questers-free-starter-pack/tiny-questers-free-starter-pack");
        for(String pack:List.of("warrior_free","bat_free")) {
            Path p=root.resolve(pack+"/png/single");
            var bases=new ArrayList<BufferedImage>();
            try(var files=Files.walk(p)){for(Path f:files.filter(f->f.toString().endsWith(".png")&&(f.toString().contains("idle")||f.toString().contains("walk"))).toList())bases.add(ImageIO.read(f.toFile()));}
            try(var dirs=Files.list(pack.equals("warrior_free")?p:p.resolve("bat"))){
                for(Path d:dirs.filter(Files::isDirectory).filter(d->!d.toString().contains("idle")&&!d.toString().contains("walk")).sorted().toList()){
                    var files=Files.list(d).filter(f->f.toString().endsWith(".png")).sorted(Comparator.comparingInt(f->Integer.parseInt(f.getFileName().toString().replaceAll("\\D", "")))).toList();
                    if(files.isEmpty())continue;
                    for(Path f:List.of(files.get(0),files.get(files.size()-1))){
                        BufferedImage im=ImageIO.read(f.toFile());int best=0,bx=0,by=0;
                        for(BufferedImage b:bases){
                            int[][] counts=new int[131][97];
                            var pixels=new HashMap<Integer,ArrayList<int[]>>();
                            for(int y=0;y<im.getHeight();y++)for(int x=0;x<im.getWidth();x++){
                                int c=im.getRGB(x,y);if((c>>>24)>200&&(c&0xffffff)!=0)pixels.computeIfAbsent(c,k->new ArrayList<>()).add(new int[]{x,y});
                            }
                            for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++){
                                int c=b.getRGB(x,y);var matches=pixels.get(c);if(matches==null)continue;
                                for(int[] q:matches){int dx=q[0]-x,dy=q[1]-y;if(dx>=-32&&dx<=64&&dy>=-30&&dy<=100)counts[dy+30][dx+32]++;}
                            }
                            for(int iy=0;iy<131;iy++)for(int ix=0;ix<97;ix++)if(counts[iy][ix]>best){best=counts[iy][ix];bx=ix-32;by=iy-30;}
                        }
                        System.out.println(pack+"/"+f.getFileName()+" matchedPixels="+best+" translation="+bx+","+by);
                    }
                }
            }
        }
    }
}
