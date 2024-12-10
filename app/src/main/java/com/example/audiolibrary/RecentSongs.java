package com.example.audiolibrary;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "recent_songs")
public class RecentSongs {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "song_uri")
    @NonNull
    private String songUri;

    @ColumnInfo(name = "song_name")
    @NonNull
    private String songName;

    @ColumnInfo(name = "song_progress")
    @NonNull
    private int songProgress;

    // Constructors, getters, and setters will be similar to Song class
    public RecentSongs(String songUri, String songName, int songProgress) {
        this.songUri = songUri;
        this.songName = songName;
        this.songProgress = songProgress;
    }

    public int getId() {
        return id;
    }

    public String getSongUri() {
        return songUri;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSongUri(String uri) {
        this.songUri = uri;
    }

    public void setSongName(String name) {
        this.songName = name;
    }

    public String getSongName() {
        return this.songName;
    }

    public void setSongProgress(int progress) {
        this.songProgress = progress;
    }

    public int getSongProgress() {
        return this.songProgress;
    }
}