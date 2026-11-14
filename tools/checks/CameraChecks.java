import com.example.monstersurvivor.render.PixelCamera;
import com.example.monstersurvivor.render.PixelCamera.Mode;
import com.example.monstersurvivor.entity.Player;
import com.example.monstersurvivor.world.CollisionGrid;

/** Projection invariants with the real Player, including fractional scaling and odd screen sizes. */
public final class CameraChecks {
    private static int checks;
    private static void check(boolean yes,String why) { if(!yes)throw new AssertionError(why);checks++; }
    public static void main(String[] args) {
        CollisionGrid grid=new CollisionGrid(100,100,16,new byte[10000]);
        float[][] axes={{0,0},{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1},{.22f,0},{.15556349f,.15556349f}};
        for(int[] size:new int[][]{{2280,1080},{1140,540},{960,540},{640,480},{961,541}}) {
            float scale=size[1]/240f,w=size[0]/scale,h=240;
            for(int fps:new int[]{24,30,34,60,120})for(float[] axis:axes) {
                PixelCamera view=new PixelCamera(Mode.SCREEN);Player p=new Player(808,808);
                float minX=9999,maxX=-9999,minY=9999,maxY=-9999;
                for(int frame=0;frame<fps;frame++) {
                    p.update(1f/fps,axis[0],axis[1],grid);
                    float x=p.x,y=p.y;
                    view.update(x,y,w,h,size[0],size[1],1600,1600);
                    check(p.x==x&&p.y==y,"Presentation must not mutate simulation");
                    check(Math.abs(view.playerX-x)*scale<=.501f&&Math.abs(view.playerY-y)*scale<=.501f,"Quantization error <= half physical pixel");
                    float px=(view.playerX-view.cameraX+w/2)*scale,py=(view.playerY-view.cameraY+h/2)*scale;
                    minX=Math.min(minX,px);maxX=Math.max(maxX,px);minY=Math.min(minY,py);maxY=Math.max(maxY,py);
                    check(maxX-minX<.003f && maxY-minY<.003f,"Player must not jitter relative to camera, including odd dimensions: "+size[0]+"x"+size[1]+" range="+(maxX-minX)+","+(maxY-minY));
                }
            }
            for(float x:new float[]{0,4,100,253,800,1350,1596,1600})for(float y:new float[]{0,3,120,800,1480,1597,1600}) {
                PixelCamera view=new PixelCamera(Mode.SCREEN);view.update(x,y,w,h,size[0],size[1],1600,1600);
                check(view.cameraX>=w/2-.001f&&view.cameraX<=1600-w/2+.001f,"Camera x clamp");
                check(view.cameraY>=h/2-.001f&&view.cameraY<=1600-h/2+.001f,"Camera y clamp");
            }
        }
        PixelCamera view=new PixelCamera(Mode.SCREEN);view.update(0,0,1800,1800,1000,1000,1600,1600);
        check(view.cameraX==800&&view.cameraY==800,"Oversized viewport centered");
        System.out.println("CAMERA CHECKS PASSED: "+checks+" assertions");
    }
}
