package com.thesis.jmap.localdb;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import java.util.Date;
import java.util.UUID;

// Come se fosse un database SQL, la @Entity rappresenta la tabella con le rispettive informazioni
@Entity(tableName="dots", primaryKeys = {"time","model","uuid"})
public class Dot {

    public Dot(double x,double y,double z,double lat,double lon,double alt, int id){
        this.x = x;
        this.y = y;
        this.z = z;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.model = Build.MODEL;
        this.time = new Date().getTime();
        uuid = activeUuid;
    }

    public Dot(String type){
        switch(type){
            case "LAST_STATE":
                uuid = "LAST_STATE";
                time = -1;
                model = Build.MODEL;
                break;
            default:
                uuid = "USELESS_DOT";
        }
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

    public static String activeUuid;

    // Dati dispositivo
    @ColumnInfo(name = "model")
    @NonNull
    public String model;

    // Dati accelerometro
    @ColumnInfo(name = "x")
    public double x;
    @ColumnInfo(name = "y")
    public double y;
    @ColumnInfo(name = "z")
    public double z;

    // Dati GPS
    @ColumnInfo(name = "lat")
    public double lat;
    @ColumnInfo(name = "lon")
    public double lon;
    @ColumnInfo(name = "alt")
    public double alt;


    // get veloce di tutte le informazioni
    public String all(){
        return x+" "+y+" "+z+" "+lat+" "+lon+" "+alt+" "+model+" "+time;
    }

    // Metodo per restituire un ID casuale del flusso
    public static void setupActiveUuid(){
        activeUuid = UUID.randomUUID().toString();
    }


}