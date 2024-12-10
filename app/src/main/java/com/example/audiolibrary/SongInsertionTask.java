package com.example.audiolibrary;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutionException;

public class SongInsertionTask implements Runnable{
    private final RecentSongsDao dao;
    private final RecentSongs song;
    private final SongInsertionCallback callback;

    public interface SongInsertionCallback {
        void onSongInserted(int songId);
        void onInsertionFailed(Exception e);
    }

    public SongInsertionTask(RecentSongsDao dao, RecentSongs song, SongInsertionCallback callback) {
        this.dao = dao;
        this.song = song;
        this.callback = callback;
    }

    @Override
    public void run() {
        try  {
            dao.insert(song);
            runOnUiThread(() -> callback.onSongInserted(song.getId()));
        } catch (Exception e) {
            runOnUiThread(() -> callback.onInsertionFailed(e));
        }
    }

    private void runOnUiThread(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }

}
