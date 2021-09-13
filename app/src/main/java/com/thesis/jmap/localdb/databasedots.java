package com.thesis.jmap.localdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Il database viene creato con una tabella con la stessa struttura di Dot.class
// Sarebbe possibile aggiungere più tabelle creando più java.class come è stata creata la classe (Dot.java)
// Esempio @Database(entities = {Dot.class, classe2.class, classe3.class ...},version = 1, exportSchema = false)
@Database(entities = {Dot.class},version = 1, exportSchema = false)
public abstract class databasedots extends RoomDatabase {
    private static final String DB_NAME = "organized_dots";

    // Prima di restituire l'istanza del db vengono fatti abbastanza controlli affinché non ritorni un valore nullo
    // 1. Se il database non esiste viene creato e poi viene restituita l'istanza
    // 2. Se il database esiste viene restituita direttamente l'istanza

    private static volatile databasedots INSTANCE;
    public abstract dotDAO dotDao();
    static databasedots getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (databasedots.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), databasedots.class,DB_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
