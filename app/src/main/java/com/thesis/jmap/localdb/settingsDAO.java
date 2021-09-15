package com.thesis.jmap.localdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface settingsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setting(Settings settings);

    @Query("SELECT * FROM settings WHERE name = :name")
    Settings status(String name);

    @Query("SELECT COUNT(*) FROM settings WHERE name = :name")
    int check(String name);
}
