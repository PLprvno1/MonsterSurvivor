package com.example.monstersurvivor.pickup;

/** Pure HUD geometry; one reusable result for all indicators, no frame allocations. */
public final class PickupIndicator {
    public float x,y,dx,dy;
    public boolean locate(float targetX,float targetY,float cx,float cy,float cameraWidth,float cameraHeight,
                          float hudWidth,float hudHeight,boolean debug) {
        float vx=targetX-cx,vy=targetY-cy;
        if(Math.abs(vx)<=cameraWidth/2 && Math.abs(vy)<=cameraHeight/2)return false;
        // Match world direction to screen aspect; leave a 96px strip for HP/stats/timer/Pause.
        vx*=hudWidth/cameraWidth;vy*=hudHeight/cameraHeight;
        float length=(float)Math.sqrt(vx*vx+vy*vy);dx=vx/length;dy=vy/length;
        float originX=hudWidth/2,originY=hudHeight/2;
        float t=Float.POSITIVE_INFINITY;
        if(vx!=0)t=Math.min(t,(vx>0?hudWidth-28-originX:28-originX)/vx);
        if(vy!=0)t=Math.min(t,(vy>0?hudHeight-108-originY:56-originY)/vy);
        x=originX+vx*t;y=originY+vy*t;
        if(x<210 && y<205)y=205; // joystick + knob + margin
        if(debug && x>hudWidth-225 && y<130)y=145; // preview buttons
        if(debug && x<135 && y>hudHeight-140)y=hudHeight-158; // FPS label
        if(y>hudHeight-140 && Math.abs(x-hudWidth/2)<125)x=hudWidth/2+(vx>=0?135:-135); // pickup notice
        if(y<114 && Math.abs(x-hudWidth/2)<185)y=114; // three weapon slots
        return true;
    }
}
