package com.example.audiolibrary;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlist")
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "playlist_name")
    @NonNull
    public String name;

    public Playlist(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
