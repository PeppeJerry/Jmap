package com.thesis.jmap.localdb;

import com.thesis.jmap.MainActivity;

import java.util.List;

public class insertDots implements Runnable {

    private databasedots database;
    private List<Dot> dots;
    private long time;
    private static int RANGE;
    private static int Tr;
    private static int Tc = 10;

    public insertDots(databasedots database, List<Dot> dots, long time,int Tr){
        this.database = database;
        this.dots = dots;
        this.time = time;
        this.Tr = Tr;
        RANGE = (int)(Tr/Tc);
    }

    @Override
    public void run() {
        int size = dots.size() - dots.size()%RANGE;
        for(int i=0; i<size/10;i++){
            double avg = avg(dots.subList(RANGE*i,(i+1)*RANGE));
            double dev = dev(dots.subList(RANGE*i,(i+1)*RANGE),avg);
            long interval = interval(dots.subList(RANGE*i,(i+1)*RANGE));
            dots.get((RANGE-1)*(i+1)).completeDot(avg,dev,interval);
            database.dotDao().addDot(dots.get((RANGE-1)*(i+1)));
        }
    }

    public double avg(List<Dot> subDots){
        double avg = 0;
        for(int i=0;i<subDots.size();i++){
            avg += subDots.get(i).getM();
        }
        return avg/subDots.size();
    }

    public double dev(List<Dot> subDots, double avg){
        double dev = 0;
        for(int i=0;i<subDots.size();i++){
            double temp = subDots.get(i).getM()-avg;
            dev += temp*temp;
        }
        return Math.pow(dev/subDots.size(),0.5);
    }

    public long interval(List<Dot> subDots){
        return subDots.get(subDots.size()-1).time - subDots.get(0).time;
    }
}
