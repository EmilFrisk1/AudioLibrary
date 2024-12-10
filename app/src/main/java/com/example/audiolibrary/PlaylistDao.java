package com.example.audiolibrary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Insert
    long insert(Playlist playlist);

    @Query("INSERT INTO playlist_song (playlistId, songId, position) " +
            "VALUES (:playlistId, :songId, (SELECT COALESCE(MAX(position), 0) + 1 FROM playlist_song WHERE playlistId = :playlistId))")
    void insertSongIntoPlaylist(int playlistId, int songId);

    @Query("SELECT * FROM playlist")
    List<Playlist> getPlaylists();

    @Query("SELECT * FROM playlist_song")
    List<PlaylistSong> getAllPlaylistSong();

    @Query("SELECT * FROM playlist_song WHERE playlistId = :playlistId ORDER BY position")
    List<PlaylistSong> getPlaylistSongs(int playlistId);

    @Query("SELECT COUNT(*) FROM playlist_song WHERE songId = :songId")
    int songPartOfPlaylist(int songId);

    @Query("SELECT s.* " +
            "FROM song s " +
            "INNER JOIN playlist_song ps ON s.id = ps.songId " +
            "WHERE ps.playlistId = :playlistId " +
            "ORDER BY ps.position")
    List<Song> getSongsForPlaylist(int playlistId);

    @Query("DELETE FROM playlist WHERE id = :playlistId")
    void deletePlaylist(int playlistId);

    @Query("DELETE FROM playlist_song WHERE playlistId = :playlistId AND songId = :songId")
    void deletePlaylistSong(int playlistId, int songId);

    @Query("DELETE FROM playlist_song WHERE songId = :songId")
    void deleteSongFromPlaylists(int songId);

    @Transaction
    default void deletePlaylistSong(int playlistId, int songId, SongDao songDao, PlaylistDatabaseOperations.PlaylistSongDeleted callback) {
        try {
            deletePlaylistSong(playlistId, songId);
            int count = songPartOfPlaylist(songId);
            if (count == 0) {
                songDao.deleteSong(songId);
            }
            callback.onPlaylistSongDeleted();
        } catch (Exception e) {
            callback.onQueryFailed(e);
        }
    }
}
