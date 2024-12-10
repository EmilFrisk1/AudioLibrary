package com.example.audiolibrary;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Song.class, RecentSongs.class, Playlist.class, PlaylistSong.class}, version = 8)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SongDao songDao();
    public abstract RecentSongsDao recentSongDao();
    public abstract PlaylistDao playlistDao();
}
