import com.example.monstersurvivor.world.ForestGenerator;
/** Export only navigation masks for bounded ADB joystick routing; no artwork or game state injection. */
public final class PickupRoute {
    public static void main(String[] args) throws Exception {
        var grid=ForestGenerator.generate(42).grid;StringBuilder text=new StringBuilder();
        for(int y=0;y<grid.height;y++) {for(int x=0;x<grid.width;x++)text.append(grid.isWalkable(x,y)?'.':'#');text.append('\n');}
        java.nio.file.Files.writeString(java.nio.file.Path.of(args.length==0?"core/build/reports/task03a/route-42.txt":args[0]),text.toString());
    }
}
