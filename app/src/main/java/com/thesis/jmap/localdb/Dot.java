package com.thesis.jmap.localdb;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Insert;

import java.util.Date;
import java.util.UUID;

// Come se fosse un database SQL, la @Entity rappresenta la tabella con le rispettive informazioni
@Entity(tableName="dots", primaryKeys = {"time","model","uuid"})
public class Dot {

    public Dot(double x,double y,double z,double lat,double lon,double alt,String uuid,long time,String model){
        if(activeUuid == null)
            setupActiveUuid();

        this.x = x;
        this.y = y;
        this.z = z;

        this.lat = lat;
        this.lon = lon;
        this.alt = alt;

        if(model == null)
            this.model = Build.MODEL;
        else
            this.model = model;

        if(time <= 0)
            this.time = new Date().getTime();
        else
            this.time = time;

        if(uuid == null)
            this.uuid = activeUuid;
        else
            this.uuid = uuid;
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

    // Restituisce una variabile di tipo "String" contenente gli attributi dell'oggetto con sintassi JSON
    public String toJSON(){
        return "{"
                +"\"time\":"+time+","
                +"\"uuid\":\""+uuid+"\","
                +"\"model\":\""+model+"\","
                +"\"x\":"+x+","
                +"\"y\":"+y+","
                +"\"z\":"+z+","
                +"\"lat\":"+lat+","
                +"\"lon\":"+lon+","
                +"\"alt\":"+alt
                + "}";
    }
}