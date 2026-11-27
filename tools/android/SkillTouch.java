import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;
import java.lang.reflect.Method;

/** Shell-UID MotionEvent injector for actual two-finger Android touch. Dex helper only, no APK/install. */
public final class SkillTouch {
    static Object manager;static Method inject;static long down;
    static void event(int action,int count,float x0,float y0,float x1,float y1) throws Exception {
        MotionEvent.PointerProperties[] props=new MotionEvent.PointerProperties[count];
        MotionEvent.PointerCoords[] coords=new MotionEvent.PointerCoords[count];
        for(int i=0;i<count;i++) {
            props[i]=new MotionEvent.PointerProperties();props[i].id=i;props[i].toolType=MotionEvent.TOOL_TYPE_FINGER;
            coords[i]=new MotionEvent.PointerCoords();coords[i].x=i==0?x0:x1;coords[i].y=i==0?y0:y1;coords[i].pressure=1;coords[i].size=.05f;
        }
        MotionEvent e=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,count,props,coords,0,0,1,1,0,0,InputDevice.SOURCE_TOUCHSCREEN,0);
        try {if(!(Boolean)inject.invoke(manager,e,2))throw new IllegalStateException("InputManager rejected action "+action);}finally {e.recycle();}
    }
    public static void main(String[] args)throws Exception {
        Class<?> type=Class.forName("android.hardware.input.InputManagerGlobal");manager=type.getMethod("getInstance").invoke(null);
        inject=type.getMethod("injectInputEvent",InputEvent.class,int.class);
        // Task08 can warm the shell VM before a short-lived projectile exists. No game shortcut:
        // after the trigger the exact same real MotionEvent stream is injected as before.
        if(args.length>9 && args[9].equals("armed")) {
            System.out.println("READY");System.out.flush();
            if(new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine()==null)return;
        }
        float x=Float.parseFloat(args[1]),y=Float.parseFloat(args[2]),mx=Float.parseFloat(args[3]),my=Float.parseFloat(args[4]),sx=Float.parseFloat(args[5]),sy=Float.parseFloat(args[6]);
        int hold=Integer.parseInt(args[7]),tail=Integer.parseInt(args[8]);boolean multiple=args[0].equals("multi");down=SystemClock.uptimeMillis();
        try {
            event(MotionEvent.ACTION_DOWN,1,x,y,sx,sy);SystemClock.sleep(70);event(MotionEvent.ACTION_MOVE,1,mx,my,sx,sy);SystemClock.sleep(100);
            if(multiple)event(MotionEvent.ACTION_POINTER_DOWN|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),2,mx,my,sx,sy);
            else if(args[0].equals("drag")) {mx=sx;my=sy;event(MotionEvent.ACTION_MOVE,1,mx,my,sx,sy);}
            for(int elapsed=0;elapsed<hold;elapsed+=40){SystemClock.sleep(40);event(MotionEvent.ACTION_MOVE,multiple?2:1,mx,my,sx,sy);}
            if(multiple)event(MotionEvent.ACTION_POINTER_UP|(1<<MotionEvent.ACTION_POINTER_INDEX_SHIFT),2,mx,my,sx,sy);
            for(int elapsed=0;elapsed<tail;elapsed+=40){SystemClock.sleep(40);event(MotionEvent.ACTION_MOVE,1,mx,my,sx,sy);}
            event(MotionEvent.ACTION_UP,1,mx,my,sx,sy);
            System.out.println("Injected actual Android "+(multiple?"2-pointer":"owned-drag")+" stream holdMs="+hold+" tailMs="+tail+" pointer IDs=0,1");
        }catch(Throwable error){event(MotionEvent.ACTION_CANCEL,1,mx,my,sx,sy);throw error;}
    }
}
