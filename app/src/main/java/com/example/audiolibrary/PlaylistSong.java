package com.example.audiolibrary;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "playlist_song",
        primaryKeys = {"playlistId", "songId"},
        foreignKeys = {
                @ForeignKey(entity = Playlist.class,
                        parentColumns = "id",
                        childColumns = "playlistId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class,
                        parentColumns = "id",
                        childColumns = "songId")
        },
        indices = {
                @Index(value = "playlistId"),
                @Index(value = "songId"),
                @Index(value = {"playlistId", "songId"}, unique = true)
        }
)

public class PlaylistSong {
    @ColumnInfo(name = "playlistId")
    public int playlistId;

    @ColumnInfo(name = "songId")
    public int songId;

    @ColumnInfo(name = "position")
    public int position;

    // Constructor
    public PlaylistSong(int playlistId, int songId, int position) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.position = position;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}