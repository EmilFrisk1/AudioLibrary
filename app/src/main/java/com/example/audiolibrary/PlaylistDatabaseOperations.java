package com.example.audiolibrary;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

public class PlaylistDatabaseOperations {
    private final PlaylistDao dao;
    private Handler mainHandler;

    public PlaylistDatabaseOperations(PlaylistDao dao) {
        mainHandler = new Handler(Looper.getMainLooper());
        this.dao = dao;
    }

    public interface PlaylistDeleteCallback {
        void onPlaylistDeleted();
        void onQueryFailed(Exception exception);
    }

    public interface DeleteSongFromPlaylists {
        void onSongDeletedFromPlaylists();
        void onQueryFailed(Exception exception);
    }

    public interface PlaylistCreated {
        void onPlaylistCreated(int playlistId);
        void onQueryFailed(Exception exception);
    }

    public interface GetPlaylistsCallback {
        void onPlaylistsFetched(List<Playlist> playlists);
        void onQueryFailed(Exception exception);
    }

    public interface GetAllPlaylistSong {
        void onPlaylistSongsFetched(List<PlaylistSong> playlistSongs);
        void onQueryFailed(Exception exception);
    }

    public interface GetPlaylistSongsCallback {
        void onPlaylistSongsFetched(List<Song> playlistSongs);
        void onQueryFailed(Exception exception);
    }

    public interface InsertPlaylistSongCallback {
        void onPlaylistSongInserted();
        void onQueryFailed(Exception exception);
    }

    public interface PlaylistSongDeleted {
        void onPlaylistSongDeleted();
        void onQueryFailed(Exception exception);
    }

    public void insertPlaylistSong(int playlistId, int songId, InsertPlaylistSongCallback callback) {
        new Thread(() -> {
            try {
                dao.insertSongIntoPlaylist(playlistId, songId);
                runOnUiThread(() -> callback.onPlaylistSongInserted());
            } catch (Exception e) {

                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void deletePlaylist(int playlistId, PlaylistDeleteCallback callback) {
        new Thread(() -> {
            try {
                dao.deletePlaylist(playlistId);
                callback.onPlaylistDeleted();
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void deleteSongFromPlaylists(int songId, DeleteSongFromPlaylists callback) {
        new Thread(() -> {
            try {
                dao.deleteSongFromPlaylists(songId);
                callback.onSongDeletedFromPlaylists();
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void getPlaylists(GetPlaylistsCallback callback) {
        new Thread(() -> {
            try {
                callback.onPlaylistsFetched(dao.getPlaylists());
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void getAllPlaylistSongs(GetAllPlaylistSong callback) {
        new Thread(() -> {
            try {
                List<PlaylistSong> playlistSongs = dao.getAllPlaylistSong();
                callback.onPlaylistSongsFetched(playlistSongs);
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void getPlaylistSongs(int playlistId, GetPlaylistSongsCallback callback) {
        new Thread(() -> {
            try {
                callback.onPlaylistSongsFetched(dao.getSongsForPlaylist(playlistId));
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void createPlaylist(Playlist playlist, PlaylistCreated callback) {
        new Thread(() -> {
            try {
                long playlistId = dao.insert(playlist);
                runOnUiThread(() -> callback.onPlaylistCreated((int)playlistId));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void deletePlaylistSong(int playlistId, int songId, SongDao songDao, PlaylistSongDeleted callback) {
        new Thread(() -> {
            try {
                dao.deletePlaylistSong(playlistId, songId, songDao, new PlaylistSongDeleted() {
                    @Override
                    public void onPlaylistSongDeleted() {
                        runOnUiThread(() -> callback.onPlaylistSongDeleted());
                    }

                    @Override
                    public void onQueryFailed(Exception exception) {
                        runOnUiThread(() -> callback.onQueryFailed(exception));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void runOnUiThread(Runnable runnable) {
        if (mainHandler != null) {
            mainHandler.post(runnable);
        }
    }

    public void cleanup() {
        mainHandler = null;
    }
}
