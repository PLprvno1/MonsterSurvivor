import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Extract only CC0 Kenney cells; preserve every source pixel and add 2px edge extrusion. */
public final class PreparePickups {
    public static void main(String[] args) throws Exception {
        Path source=Path.of("asset_sources/kenney_roguelike-rpg-pack/Spritesheet/roguelikeSheet_transparent.png");
        BufferedImage sheet=ImageIO.read(source.toFile()),atlas=new BufferedImage(128,32,BufferedImage.TYPE_INT_ARGB);
        String[] names={"pickup_damage","pickup_health","pickup_speed","pickup_drop"};
        int[][] cells={{49,21},{49,18},{49,20},{54,14}};
        StringBuilder json=new StringBuilder("{\n  \"source\": \"Kenney Roguelike pack (CC0), 16px cells / 1px margin\",\n  \"texture\": \"game/pickups.png\",\n  \"regions\": {\n");
        for(int i=0;i<cells.length;i++) {
            int x=2+i*20,y=2;
            for(int v=-2;v<18;v++)for(int u=-2;u<18;u++)
                atlas.setRGB(x+u,y+v,sheet.getRGB(cells[i][0]*17+Math.max(0,Math.min(15,u)),cells[i][1]*17+Math.max(0,Math.min(15,v))));
            json.append("    \"").append(names[i]).append("\": [").append(x).append(",2,16,16]").append(i==3?"\n":",\n");
        }
        json.append("  },\n  \"sourceCells\": [[49,21],[49,18],[49,20],[54,14]]\n}\n");
        ImageIO.write(atlas,"png",Path.of("assets/game/pickups.png").toFile());
        Files.writeString(Path.of("assets/game/pickups.json"),json.toString());
        System.out.println("Prepared pickups.png 128x32; original world atlas/catalog untouched.");
    }
}
