import java.nio.file.*;
import javax.imageio.ImageIO;
/** Verify delivered Kenney pixels/padding, without decoding any other art pack. */
public final class PickupAssetCheck {
    public static void main(String[] args) throws Exception {
        var source=ImageIO.read(Path.of("asset_sources/kenney_roguelike-rpg-pack/Spritesheet/roguelikeSheet_transparent.png").toFile());
        var atlas=ImageIO.read(Path.of("assets/game/pickups.png").toFile());int[][] cells={{49,21},{49,18},{49,20},{54,14}};
        int checks=0;
        if(atlas.getWidth()!=128 || atlas.getHeight()!=32)throw new AssertionError("Atlas dimensions");checks++;
        for(int i=0;i<4;i++)for(int v=-2;v<18;v++)for(int u=-2;u<18;u++) {
            int expected=source.getRGB(cells[i][0]*17+Math.max(0,Math.min(15,u)),cells[i][1]*17+Math.max(0,Math.min(15,v)));
            if(atlas.getRGB(2+i*20+u,2+v)!=expected)throw new AssertionError("Source pixel / extrusion mismatch");checks++;
        }
        System.out.println("PICKUP ASSET PASSED: "+checks+" pixel/padding checks (Kenney CC0 only)");
    }
}
