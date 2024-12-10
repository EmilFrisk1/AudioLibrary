package com.example.audiolibrary;

import android.app.Application;

import androidx.room.Room;

public class DatabaseClient {
    private static DatabaseClient mInstance;
    private final AppDatabase appDatabase;

    private DatabaseClient(Application application) {
        appDatabase = Room.databaseBuilder(application, AppDatabase.class, "song_database")
                .fallbackToDestructiveMigration() // TODO - poista tämä myöhemmin
                .build();
    }

    public static synchronized DatabaseClient getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(application);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
