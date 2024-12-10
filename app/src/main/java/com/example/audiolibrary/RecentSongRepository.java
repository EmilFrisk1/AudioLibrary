package com.example.audiolibrary;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;

public class RecentSongRepository {
    private final SongDao dao;

    public RecentSongRepository(SongDao dao) {
        this.dao = dao;
    }

    public interface SongsQueryCallback {
        void onSongsRetrieved(List<Song> songs);
        void onQueryFailed(Exception exception);
    }

    public interface SongQueryCallback {
        void onSongRetrieved(Song songs);
        void onQueryFailed(Exception exception);
    }

    public interface SongUpdateCallback {
        void onSongUpdated(Song updatedSong);
        void onQueryFailed(Exception exception);
    }

    public interface LatestSongOverlap {
        void onOverlapResult(int result);
        void onQueryFailed(Exception exception);
    }

    public interface SongInsertionCallback {
        void onSongInserted(Song song);
        void onQueryFailed(Exception exception);
    }

    public interface GetSongWURICallback {
        void onGetSong(Song song);
        void onQueryFailed(Exception exception);
    }

    public interface deleteLatestSongCallback {
        void onDelete();
        void onQueryFailed(Exception exception);
    }

    public void insertSong(Song song, SongInsertionCallback callback) {
        new Thread(() -> {
            try {
                long songId = dao.insert(song);
                Song insertedSong = dao.getSong((int)songId);
                runOnUiThread(() -> callback.onSongInserted(insertedSong));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void getSongWURI(String songURI, GetSongWURICallback callback) {
        new Thread(() -> {
            try {
                Song song = dao.getSongWURI(songURI);
                runOnUiThread(() -> callback.onGetSong(song));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void getLatestSongs(int limit, SongsQueryCallback callback) {
        new Thread(() -> {
            try {
                List<Song> songs = dao.getLatestSongs(limit);
                runOnUiThread(() -> callback.onSongsRetrieved(songs));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void getLatestSong(SongQueryCallback callback) {
        new Thread(() -> {
            try {
                Song latestSong =  dao.daoGetLatestSong();
                callback.onSongRetrieved(latestSong);
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void getSongWithId(int songId, SongQueryCallback callback) {
        new Thread(() -> {
            try {
                Song song =  dao.getSong(songId);
                callback.onSongRetrieved(song);
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void updateLatestSong(int songId, int progress, SongUpdateCallback callback) {
        new Thread(() -> {
            try {
                getSongWithId(songId, new SongQueryCallback() {
                    @Override
                    public void onSongRetrieved(Song song) {
                        song.setSongProgress(progress);
                        dao.update(song);
                        runOnUiThread(() -> callback.onSongUpdated(song));
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

    public void checkLatestOverlap(String songUri, LatestSongOverlap callback) {
        new Thread(() -> {
            try {
                int result = dao.checkLatestOverlap(songUri);
                runOnUiThread(() -> callback.onOverlapResult(result));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void deleteAllSongs() {
        new Thread(() -> {
            try {
                dao.deleteAllSongs();
            } catch (Exception e) {
                Log.e("song deletion", "failed to delete songs", e);
            }
        }).start();
    }

    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
