package com.thesis.jmap.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

// Come se fosse un database SQL, questa è la tabella/classe
@Entity(tableName="dots", primaryKeys = "id")
public class Dot {

    public Dot(double x,double y,double z,double lat,double lon,double alt,String model, String uid, int id){
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.model = model;
        this.uid = uid;
    }

    // Con @ColumnInfo(name = 'name') identifico le colonne della tabella
    // Sarebbe possibile utilizzare @Primarykey se per un attributo
    // Per chiavi composte invece in @Entity -> primarykey = 'col1,col2,...'

    // ID
    @ColumnInfo(name = "id")
    public int id;

    // Dati dispositivo
    @ColumnInfo(name = "model")
    public String model;
    @ColumnInfo(name = "androidid")
    public String uid;

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
        return x+" "+y+" "+z+" "+lat+" "+lon+" "+alt+" "+model+" "+uid+" "+id;
    }

    // Metodi e construct per il LAST_DOT

    public Dot(){
        this.model="LAST_DOT";
    }
    public void setid(int id){
        this.id=id;
    }
}