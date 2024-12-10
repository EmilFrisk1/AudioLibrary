package com.example.audiolibrary;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;

public class SongRepository {
    private final RecentSongsDao dao;

    public SongRepository(RecentSongsDao dao) {
        this.dao = dao;
    }

    public interface SongsQueryCallback {
        void onSongsRetrieved(List<RecentSongs> songs);
        void onQueryFailed(Exception exception);
    }

    public interface SongQueryCallback {
        void onSongRetrieved(RecentSongs songs);
        void onQueryFailed(Exception exception);
    }

    public interface SongUpdateCallback {
        void onSongUpdated(RecentSongs updatedSong);
        void onQueryFailed(Exception exception);
    }

    public interface LatestSongOverlap {
        void onOverlapResult(int result);
        void onQueryFailed(Exception exception);
    }

    public interface deleteLatestSongCallback {
        void onDelete();
        void onQueryFailed(Exception exception);
    }

    public void getLatestSongs(int limit, SongsQueryCallback callback) {
        new Thread(() -> {
            try {
                List<RecentSongs> songs = dao.getLatestSongs(limit);
                runOnUiThread(() -> callback.onSongsRetrieved(songs));
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    public void getLatestSong(SongQueryCallback callback) {
        new Thread(() -> {
            try {
                RecentSongs latestSong =  dao.daoGetLatestSong();
                callback.onSongRetrieved(latestSong);
            } catch (Exception e) {
                callback.onQueryFailed(e);
            }
        }).start();
    }

    public void getSongWithId(int songId, SongQueryCallback callback) {
        new Thread(() -> {
            try {
                RecentSongs song =  dao.getSong(songId);
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
                    public void onSongRetrieved(RecentSongs song) {
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

    public void deleteOldestAudio(int songId, deleteLatestSongCallback callback) {
        new Thread(() -> {
            try {
                dao.deleteOldestSong(songId);
                runOnUiThread(() -> callback.onDelete());
            } catch (Exception e) {
                runOnUiThread(() -> callback.onQueryFailed(e));
            }
        }).start();
    }

    private void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
