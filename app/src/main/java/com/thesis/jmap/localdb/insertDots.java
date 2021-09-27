package com.thesis.jmap.localdb;

import java.util.List;

public class insertDots implements Runnable {

    private databasedots database;
    private List<Dot> dots;
    private long time;
    private static final int RANGE = 10;

    public insertDots(databasedots database, List<Dot> dots, long time){
        this.database = database;
        this.dots = dots;
        this.time = time;
    }

    @Override
    public void run() {
        int size = dots.size() - dots.size()%RANGE;
        for(int i=0; i<size/10;i++){
            double avg = avg1(dots.subList(RANGE*i,(i+1)*RANGE));
            double dev = dev2(dots.subList(RANGE*i,(i+1)*RANGE),avg);
            long interval = interval3(dots.subList(RANGE*i,(i+1)*RANGE));
            dots.get((RANGE-1)*(i+1)).completeDot(avg,dev,interval);
            database.dotDao().addDot(dots.get((RANGE-1)*(i+1)));
        }
    }

    public double avg1(List<Dot> subDots){
        double avg = 0;
        for(int i=0;i<subDots.size();i++){
            avg += subDots.get(i).getM();
        }
        return avg/subDots.size();
    }

    public double dev2(List<Dot> subDots, double avg){
        double dev2 = 0;
        for(int i=0;i<subDots.size();i++){
            double temp = subDots.get(i).getM()-avg;
            dev2 += temp*temp;
        }
        return dev2/subDots.size();
    }

    public long interval3(List<Dot> subDots){
        return subDots.get(0).time - subDots.get(subDots.size()-1).time;
    }
}
