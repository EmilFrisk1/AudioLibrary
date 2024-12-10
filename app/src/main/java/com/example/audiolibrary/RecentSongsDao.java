package com.example.audiolibrary;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface RecentSongsDao {
    @Insert
    void insert(RecentSongs song);

    @Query("SELECT * FROM recent_songs")
    List<RecentSongs> getAllRecentSongs();

    @Query("DELETE FROM recent_songs")
    void deleteAllRecentSongs();

    @Update
    void update(RecentSongs song);

    @Query("SELECT * FROM recent_songs WHERE id = (SELECT MAX(id) FROM song)")
    RecentSongs daoGetLatestSong();

    @Query("SELECT * FROM recent_songs ORDER BY id DESC LIMIT :limit")
    List<RecentSongs> getLatestSongs(int limit);

    @Query("SELECT * FROM recent_songs WHERE id = :songId")
    RecentSongs getSong(int songId);

    @Query("DELETE FROM recent_songs WHERE id = :songId")
    void deleteOldestSong(int songId);

    @Query("SELECT EXISTS(SELECT 1 FROM recent_songs WHERE song_uri = :songUri_)")
    int checkLatestOverlap(String songUri_);

    @Query("DELETE FROM recent_songs")
    void deleteAllSongs();
}
