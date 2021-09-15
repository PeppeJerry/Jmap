package com.thesis.jmap.localdb;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import java.util.Date;
import java.util.UUID;

@Entity(tableName="settings", primaryKeys = {"name"})
public class Settings {

    public Settings(String name,boolean state){
        this.name = name;
        this.state = state;
    }

    @ColumnInfo(name = "name")
    @NonNull
    public String name;

    @ColumnInfo(name = "state")
    public boolean state;

}