package com.example.linked;


import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MyLocation.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    public abstract LocationDao dao();
}