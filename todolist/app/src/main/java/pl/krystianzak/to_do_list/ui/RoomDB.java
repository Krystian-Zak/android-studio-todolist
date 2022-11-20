package pl.krystianzak.to_do_list.ui;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pl.krystianzak.to_do_list.JobData;

@Database(entities = {JobData.class}, version = 1, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {
    //database instance
    private static RoomDB database;

    private static String DATABASE_NAME = "jobs_db";

    public synchronized static RoomDB getInstance(Context context) {
        if (database == null) {
            //When database is null - initialize database
            database = Room.databaseBuilder(context.getApplicationContext()
                    , RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        //Return database
        return database;
    }

    //Create DAO
    public abstract MainDao mainDao();
}
