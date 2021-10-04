package com.thesis.jmap.localdb;

import com.thesis.jmap.MainActivity;

import java.util.List;

public class insertDots implements Runnable {

    private databasedots database;
    private List<Dot> dots;
    private static int RANGE;
    private static int Tr;
    private static int Tc = 10;

    public insertDots(databasedots database, List<Dot> dots,int Tr){
        this.database = database;
        this.dots = dots;
        this.Tr = Tr;
        RANGE = (int)(Tr/Tc);
    }


    @Override
    public void run() {
        int size = dots.size() - dots.size()%RANGE;
        for(int i=0; i<size/10;i++){
            double avg[] = avg(dots.subList(RANGE*i,(i+1)*RANGE));
            double dev[] = dev(dots.subList(RANGE*i,(i+1)*RANGE),avg);
            double strength = strength(dots.subList(RANGE*i,(i+1)*RANGE));
            int interval = (int) (dots.get((i+1)*RANGE-1).time - dots.get(RANGE*i).time);
            dots.get((RANGE-1)*(i+1)).x = avg[0];
            dots.get((RANGE-1)*(i+1)).y = avg[1];
            dots.get((RANGE-1)*(i+1)).z = avg[2];
            dots.get((RANGE-1)*(i+1)).devx = dev[0];
            dots.get((RANGE-1)*(i+1)).devy = dev[1];
            dots.get((RANGE-1)*(i+1)).devz = dev[2];
            dots.get((RANGE-1)*(i+1)).interval = interval;
            dots.get((RANGE-1)*(i+1)).strength = strength;
            database.dotDao().addDot(dots.get((RANGE-1)*(i+1)));
        }
    }

    public double[] avg(List<Dot> subDots){
        double x=0,y=0,z=0;
        for(int i=0;i<subDots.size();i++){
            x += subDots.get(i).x;
            y += subDots.get(i).y;
            z += subDots.get(i).z;
        }
        double[] avg = new double[3];
        avg[0] = x/subDots.size();
        avg[1] = y/subDots.size();
        avg[2] = z/subDots.size();
        return avg;
    }

    public double[] dev(List<Dot> subDots, double avg[]){
        double devx=0,devy=0,devz=0;
        double temp;
        for(int i=0;i<subDots.size();i++){

            temp = subDots.get(i).x-avg[0];
            devx += temp*temp;

            temp = subDots.get(i).y-avg[1];
            devy += temp*temp;

            temp = subDots.get(i).z-avg[2];
            devz += temp*temp;
        }
        double[] dev = new double[3];
        dev[0] = Math.pow(devx/subDots.size(),0.5);
        dev[1] = Math.pow(devy/subDots.size(),0.5);
        dev[2] = Math.pow(devz/subDots.size(),0.5);
        return dev;

    }

    public double strength(List<Dot> subDots){
        double top[] = new double[3];
        double bot[] = new double[3];

        top[0] = bot[0] = subDots.get(0).x;
        top[1] = bot[1] = subDots.get(0).y;
        top[2] = bot[2] = subDots.get(0).z;

        for(int i=1;i<subDots.size();i++){
            double x = subDots.get(i).x;
            double y = subDots.get(i).y;
            double z = subDots.get(i).z;

            if(top[0]>x)
                top[0] = x;
            else if(bot[0]<x)
                bot[0]=x;

            if(top[1]>y)
                top[1] = y;
            else if(bot[1]<y)
                bot[1]=y;

            if(top[2]>z)
                top[2] = z;
            else if(bot[2]<z)
                bot[2]=z;
        }
        double x=top[0]-bot[0];
        double y=top[1]-bot[1];
        double z=top[2]-bot[2];
        return Math.pow(x*x+y*y+z*z,0.5);
    }
}
