package com.thesis.jmap.localdb;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.Date;
import java.util.UUID;

// Come se fosse un database SQL, la @Entity rappresenta la tabella con le rispettive informazioni
@Entity(tableName="dots", primaryKeys = {"time","uuid"})
public class Dot {

    public static String activeUuid;

    // Costruttore per il database Room
    public Dot(double x,double y,double z,double lat,double lon,double alt,String uuid,long time, double devx, double devy, double devz,int interval, double strength){

        this.x = x;
        this.devx = devx;
        this.y = y;
        this.devy = devy;
        this.z = z;
        this.devz = devz;

        this.lat = lat;
        this.lon = lon;
        this.alt = alt;

        this.time = time;
        this.uuid = uuid;

        this.interval = interval;
        this.strength = strength;
    }

    // Costruttore per l'aggiunta dei campioni
    @Ignore
    public Dot(double x,double y,double z,double lat,double lon,double alt){
        if(activeUuid == null)
            setupActiveUuid();

        this.x = x;
        this.y = y;
        this.z = z;

        this.lat = lat;
        this.lon = lon;
        this.alt = alt;

        this.time = new Date().getTime();
        this.uuid = activeUuid;
    }
    // Con @ColumnInfo(name = 'name') identifico le colonne della tabella
    // Sarebbe possibile utilizzare @Primarykey se per un attributo
    // Per chiavi composte invece in @Entity -> primarykey = 'col1,col2,...'

    // Tempo di acquisizione del dato tradotto in secondi
    @ColumnInfo(name = "time")
    public long time;

    // uuid - a mo delle porte dei pacchetti TCP - UDP
    // Ogni flusso di punti viene identificato da un UUID specifico
    @ColumnInfo(name = "uuid")
    @NonNull
    public String uuid;

    // Dati accelerometro
    @ColumnInfo(name = "x")
    public double x;
    @ColumnInfo(name = "devx")
    public double devx;
    @ColumnInfo(name = "y")
    public double y;
    @ColumnInfo(name = "devy")
    public double devy;
    @ColumnInfo(name = "z")
    public double z;
    @ColumnInfo(name = "devz")
    public double devz;

    // Dati GPS
    @ColumnInfo(name = "lat")
    public double lat;
    @ColumnInfo(name = "lon")
    public double lon;
    @ColumnInfo(name = "alt")
    public double alt;

    @ColumnInfo(name = "interval")
    public int interval;
    @ColumnInfo(name = "strength")
    public double strength;

    // Metodo per restituire un ID casuale del flusso
    public static void setupActiveUuid(){
        activeUuid = UUID.randomUUID().toString();
    }
}