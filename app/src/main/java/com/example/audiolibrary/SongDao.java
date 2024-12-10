package com.example.audiolibrary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SongDao {
    @Insert
    long insert(Song song);

    @Query("SELECT * FROM song ORDER BY id DESC LIMIT :limit")
    List<Song> getLatestSongs(int limit);

    @Query("DELETE FROM song")
    void deleteAllSongs();

    @Query("DELETE FROM song WHERE id = :songId")
    void deleteSong(int songId);

    @Update
    void update(Song song);

    @Query("SELECT * FROM song WHERE id = (SELECT MAX(id) FROM song)")
    Song daoGetLatestSong();

    @Query("SELECT * FROM song WHERE id = :songId")
    Song getSong(int songId);

    @Query("SELECT * FROM song WHERE song_uri = :songURI")
    Song getSongWURI(String songURI);

    @Query("SELECT EXISTS(SELECT 1 FROM song WHERE song_uri = :songUri_)")
    int checkLatestOverlap(String songUri_);

    @Query("DELETE FROM song WHERE id = :songId")
    void checkLatestOverlap(int songId);
}
