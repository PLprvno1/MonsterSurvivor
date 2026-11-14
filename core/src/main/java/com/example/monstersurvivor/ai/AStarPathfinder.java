package com.example.monstersurvivor.ai;

import com.badlogic.gdx.utils.IntArray;
import java.util.Arrays;

/** Four-neighbor A*, Manhattan heuristic, reusable indexed heap. No corner cutting. */
public final class AStarPathfinder {
    private final NavigationGrid grid;
    private final int[] parent,cost,stamp,closed,heap,position;
    private int generation,heapSize,goal;
    public int lastExpanded;
    public AStarPathfinder(NavigationGrid grid) {
        this.grid=grid;int n=grid.collision.width*grid.collision.height;
        parent=new int[n];cost=new int[n];stamp=new int[n];closed=new int[n];heap=new int[n];position=new int[n];
    }
    public boolean find(int start,int target,boolean flying,IntArray result) {
        result.clear();lastExpanded=0;
        if(!grid.connected(start,target,flying))return false;
        if(++generation==Integer.MAX_VALUE) { Arrays.fill(stamp,0);Arrays.fill(closed,0);generation=1; }
        goal=target;heapSize=0;stamp[start]=generation;cost[start]=0;parent[start]=-1;push(start);
        // Each node closes once; at most width*height expansions per search.
        while(heapSize>0) {
            int current=pop();closed[current]=generation;lastExpanded++;
            if(current==target) {
                for(int id=current;id!=start;id=parent[id])result.add(id);
                result.reverse();return true;
            }
            for(int dir=0;dir<4;dir++) {
                int next=grid.neighbor(current,dir);
                if(!grid.walkable(next,flying) || closed[next]==generation)continue;
                int candidate=cost[current]+1;
                if(stamp[next]!=generation) {
                    stamp[next]=generation;cost[next]=candidate;parent[next]=current;push(next);
                } else if(candidate<cost[next]) { cost[next]=candidate;parent[next]=current;up(position[next]); }
            }
        }
        return false;
    }
    private int score(int id) { return cost[id]+Math.abs(id%grid.collision.width-goal%grid.collision.width)+Math.abs(id/grid.collision.width-goal/grid.collision.width); }
    private boolean less(int a,int b) { int fa=score(a),fb=score(b);return fa<fb || (fa==fb && cost[a]>cost[b]); }
    private void swap(int a,int b) { int t=heap[a];heap[a]=heap[b];heap[b]=t;position[heap[a]]=a;position[heap[b]]=b; }
    private void up(int i) { while(i>0) {int p=(i-1)/2;if(!less(heap[i],heap[p]))break;swap(i,p);i=p;} }
    private void push(int id) { heap[heapSize]=id;position[id]=heapSize;up(heapSize++); }
    private int pop() {
        int result=heap[0];heapSize--;
        if(heapSize>0) {
            heap[0]=heap[heapSize];position[heap[0]]=0;int i=0;
            while(i*2+1<heapSize) {
                int child=i*2+1;if(child+1<heapSize && less(heap[child+1],heap[child]))child++;
                if(!less(heap[child],heap[i]))break;swap(child,i);i=child;
            }
        }
        return result;
    }
}
