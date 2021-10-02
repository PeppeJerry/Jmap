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
            int interval = (int) (dots.get((i+1)*RANGE-1).time - dots.get(RANGE*i).time);
            dots.get((RANGE-1)*(i+1)).x = avg[0];
            dots.get((RANGE-1)*(i+1)).y = avg[1];
            dots.get((RANGE-1)*(i+1)).z = avg[2];
            dots.get((RANGE-1)*(i+1)).devx = dev[0];
            dots.get((RANGE-1)*(i+1)).devy = dev[1];
            dots.get((RANGE-1)*(i+1)).devz = dev[2];
            dots.get((RANGE-1)*(i+1)).interval = interval;
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

    public long interval(List<Dot> subDots){
        return subDots.get(subDots.size()-1).time - subDots.get(0).time;
    }
}
