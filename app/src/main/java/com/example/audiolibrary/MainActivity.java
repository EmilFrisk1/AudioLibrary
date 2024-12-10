package com.example.audiolibrary;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
//import android.content;


public class MainActivity extends AppCompatActivity implements HomeFragment.OnAudioSystemInteraction, AudioPlayerInterface.AudioPlayerI {
    private RecentlyPlayedVModel recentlyPlayedVModel;
    private static final int SELECT_FILE = 1;
    private ActivityResultLauncher<Intent> launcher;
    private SeekBar seekBar;
    private TextView  currentTimeTextView;
    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private View mediaPlayerBg;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton closeButton;
    private TextView songNameTextView;
    private Uri currentUri;
    private int currentSongId;
    private List<RecentSongs> latestSongs;
    private SongDao dao;
    private RecentSongsDao recentSongDao;
    private SongDao songDao;
    private SongRepository recentSongRepo;
    private RecentSongRepository songRepo;
    private int songStartTime = 0;
    private String currentSongName;
    private HomeFragment homeFragment;
    private PlaylistFragment playlistFragment;
    private String currentFragmentName = "";
    private Boolean playlistPlaying = false;
    private List<Song> playlistSongs = null;
    private Song currentPlaylistSong;
    private PlaylistDao playlistDao;
    private PlaylistDatabaseOperations playlistOperations;

    public interface OnSongInsertionListener {
        void OnSongInserted(Song song);
    }

    @Override
    public void onPlayAudio(Song song, List<Song> songList) {
        currentPlaylistSong = song;
        playlistPlaying = true;
        playlistSongs = songList;
        if (mediaPlayer != null) {
            hideMediaPlayer();
            cleanUpMediaPlayer();
        }
        currentUri = Uri.parse(song.getSongUri());
        currentSongId = song.getId();
        currentSongName = getFileName(currentUri);
        if (!currentSongName.equals("FILE_DELETED")) {
            playAudio();
        }
    }

    @Override
    public void onSelectAudioFile(String fragmentName) {
        if (mediaPlayer != null) {
            hideMediaPlayer();
            cleanUpMediaPlayer();
        }

        currentFragmentName = fragmentName;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launcher.launch(intent);
    }

    @Override
    public void onRecentlyPlayedClick(Item song) {
        if (mediaPlayer != null) {
            hideMediaPlayer();
            cleanUpMediaPlayer();
        }
        currentUri = song.getAudioUri();
        currentSongId = song.getAudioId();
        currentSongName = getFileName(currentUri);
        if (!currentSongName.equals("FILE_DELETED")) {
            playAudio();
        }
    }

    private void selectAudioHook() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == MainActivity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            currentUri = data.getData();
                            if (currentUri != null) {
                                int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(currentUri, takeFlags);

                                // Save audio to db
                                currentSongName = getFileName(currentUri);

                                if (currentFragmentName.equals("HOME_FRAGMENT")) {
                                    recentSongRepo.checkLatestOverlap(currentUri.toString(), new SongRepository.LatestSongOverlap() {
                                        @Override
                                        public void onOverlapResult(int result) {
                                            if (result != 1) {
                                                RecentSongs newSong = new RecentSongs(currentUri.toString(), currentSongName, 0);
                                                new Thread(new SongInsertionTask(recentSongDao, newSong, new SongInsertionTask.SongInsertionCallback() {
                                                    @Override
                                                    public void onSongInserted(int songId) {
                                                        currentSongId = songId;

                                                        List<Item> currentItems = recentlyPlayedVModel.getItems().getValue();
                                                        // maintain only 4 recent songs
                                                        if (currentItems.size() >= 4) {
                                                            recentSongRepo.deleteOldestAudio(currentItems.get(0).getAudioId(), new SongRepository.deleteLatestSongCallback() {
                                                                @Override
                                                                public void onDelete() {
                                                                    if (currentItems != null) {
                                                                        if (currentItems.size() > 0) {
                                                                            currentItems.remove(0);
                                                                        }
                                                                    }
                                                                    currentItems.add(new Item(newSong.getSongName(), currentUri, songId));
                                                                    recentlyPlayedVModel.setItems(currentItems);
                                                                }

                                                                @Override
                                                                public void onQueryFailed(Exception exception) {
                                                                    Log.e("SongInsertion", "Failed to delete song whichs file no longer exists", exception);
                                                                }
                                                            });
                                                        } else {
                                                            currentItems.add(new Item(newSong.getSongName(), currentUri, songId));
                                                            recentlyPlayedVModel.setItems(currentItems);
                                                        }
                                                    }

                                                    @Override
                                                    public void onInsertionFailed(Exception e) {
                                                        // Handle the error
                                                        Log.e("SongInsertion", "Failed to insert song", e);
                                                    }
                                                })).start();

                                            }
                                        }

                                        @Override
                                        public void onQueryFailed(Exception exception) {

                                        }
                                    });
                                    //
                                    playAudio();
                                } else if (currentFragmentName.equals("PLAYLIST_DETAILS_FRAGMENT")) {
                                    PlaylistDetailsFragment playlistDetailsFragment = (PlaylistDetailsFragment) getSupportFragmentManager().findFragmentByTag("PlaylistDetailsTag");
                                    songRepo.getSongWURI(currentUri.toString(), new RecentSongRepository.GetSongWURICallback() {
                                        @Override
                                        public void onGetSong(Song song) {
                                            if (song == null) {
                                                Song newSong = new Song(currentUri.toString(), currentSongName, 0);
                                                songRepo.insertSong(newSong, new RecentSongRepository.SongInsertionCallback() {
                                                    @Override
                                                    public void onSongInserted(Song insertedSong) {
                                                        playlistDetailsFragment.OnSongInserted(insertedSong);
                                                    }

                                                    @Override
                                                    public void onQueryFailed(Exception e) {
                                                        Log.e("SongInsertion", "Failed to insert song", e);
                                                    }
                                                });
                                            } else {
                                                playlistDetailsFragment.OnSongInserted(song);
                                            }
                                        }

                                        @Override
                                        public void onQueryFailed(Exception exception) {
                                            Log.e("SongInsertion", "Failed to get the song", exception);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
    }

    private void setupViews() {
        seekBar = findViewById(R.id.seekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        songNameTextView = findViewById(R.id.songNameTextView);
        mediaPlayerBg = findViewById(R.id.mediaPlayerBg);
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        closeButton = findViewById(R.id.closeButton);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        playlistDao = DatabaseClient.getInstance(this.getApplication()).getAppDatabase().playlistDao();
        playlistOperations = new PlaylistDatabaseOperations(playlistDao);

        // setup fragments
        if (savedInstanceState == null) {
            playlistFragment = new PlaylistFragment();
            homeFragment = new HomeFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragmentContainer, homeFragment);
            transaction.commit();
        }

        recentlyPlayedVModel = new ViewModelProvider(this).get(RecentlyPlayedVModel.class);
        setupViews();
        hideMediaPlayer();

        dao = DatabaseClient.getInstance(getApplication()).getAppDatabase().songDao();
        recentSongDao = DatabaseClient.getInstance(getApplication()).getAppDatabase().recentSongDao();
        songDao = DatabaseClient.getInstance(getApplication()).getAppDatabase().songDao();
        recentSongRepo = new SongRepository(recentSongDao);
        songRepo = new RecentSongRepository(songDao);

        selectAudioHook();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private String getFileName(Uri uri) {
        try {
            if (uri.getScheme().equals("content")) {
                Cursor cursor = getContentResolver().query(uri,null, null,null,null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            return cursor.getString(nameIndex);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("Attempt to invoke interface method 'void android.database.Cursor.close()' on a null object reference")) {
                // Show a toast for this specific error
                Toast.makeText(this, "This audio file has been deleted from your device", Toast.LENGTH_LONG).show();
                playlistOperations.deleteSongFromPlaylists(currentSongId, new PlaylistDatabaseOperations.DeleteSongFromPlaylists() {
                    @Override
                    public void onSongDeletedFromPlaylists() {
                        //
                    }

                    @Override
                    public void onQueryFailed(Exception exception) {
                        Log.e("SongInsertion", "Failed to delete song from playlists", e);
                    }
                });

                recentSongRepo.deleteOldestAudio(currentSongId, new SongRepository.deleteLatestSongCallback() {
                    @Override
                    public void onDelete() {
                        // Update UI
                        List<Item> currentItems = recentlyPlayedVModel.getItems().getValue();
                        if (currentItems != null) {
                            for (int i = 0; i < currentItems.size(); i++){
                                if (currentItems.get(i).getAudioId() == currentSongId) {
                                    currentItems.remove(i);
                                    break;
                                }
                            }
                            recentlyPlayedVModel.setItems(currentItems);
                        }
                    }

                    @Override
                    public void onQueryFailed(Exception exception) {
                        Log.e("SongInsertion", "Failed to delete recent song", e);
                    }
                });

                return "FILE_DELETED";
            }
        }
        return null;
    }

    private void playAudio() {
        if (currentUri == null) {
            return;
        } else {
            try {
                songNameTextView.setText(currentSongName);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                );
                mediaPlayer.setDataSource(this, currentUri);

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        seekBar.setMax(mediaPlayer.getDuration());
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    mediaPlayer.seekTo(progress);
                                    updateTimeDisplay(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                                handler.removeCallbacks(updateSeekBar);
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                handler.postDelayed(updateSeekBar, 1000);
                            }
                        });
                        mediaPlayer.seekTo(songStartTime);
                        mediaPlayer.start();
                        startSeekBarUpdate();
                    }
                });
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.e("MediaPlayer", "Error: " + what + ", " + extra);
                        return true; // True indicates we've handled the error
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (playlistPlaying) {
                            int playlistSize = playlistSongs.size();
                            int currentSongIndex = -1;
                            for (int i = 0; i < playlistSize; i++) {
                                if (playlistSongs.get(i).getId() == currentPlaylistSong.getId()) {
                                    currentSongIndex = i;
                                    break;
                                }
                            }

                            if ((currentSongIndex + 1) == playlistSize) { // last song
                                playlistPlaying = false;
                                currentPlaylistSong = null;
                                playlistSongs = null;
                                cleanUpMediaPlayer();
                            } else {
                                cleanUpMediaPlayer();
                                currentPlaylistSong = playlistSongs.get(currentSongIndex + 1);
                                currentUri = Uri.parse(currentPlaylistSong.getSongUri());
                                currentSongId = currentPlaylistSong.getId();
                                currentSongName = getFileName(currentUri);
                                playAudio();
                            }
                        } else {
                            cleanUpMediaPlayer();
                        }
                    }
                });

                mediaPlayer.prepareAsync();
                showMediaPlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanUpMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer =  null;
            hideMediaPlayer();
        }
    }

    private void startSeekBarUpdate() {

        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        updateTimeDisplay(mediaPlayer.getCurrentPosition());
        handler.postDelayed(updateSeekBar, 1000);
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                updateTimeDisplay(currentPosition);
                handler.postDelayed(this, 1000);
            } else {
                handler.removeCallbacks(this);
                return;
            }
        }
    };

    private void updateTimeDisplay(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        int hours = (milliseconds / ((1000 * 60)) / 60);
        currentTimeTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void hideMediaPlayer() {
        currentTimeTextView.setVisibility(View.INVISIBLE);
        songNameTextView.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);
        mediaPlayerBg.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.INVISIBLE);
    }

    private void showMediaPlayer() {
        currentTimeTextView.setVisibility(View.VISIBLE);
        songNameTextView.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        mediaPlayerBg.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.VISIBLE);
    }

    public void pauseButtonClick(View v) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Pause the MediaPlayer
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
            handler.removeCallbacks(updateSeekBar);
        }
    }

    public void playButtonClick(View v) {
        if (mediaPlayer != null && !(mediaPlayer.isPlaying())) {
            mediaPlayer.start(); // Pause the MediaPlayer
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            startSeekBarUpdate();
        }
    }

    public void navPlaylistClick(View v) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, playlistFragment).addToBackStack(null).commit();
    }

    public void navHomeClick(View v) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, homeFragment).addToBackStack(null).commit();
    }

    public void closeButtonClick(View v) {
        if (playlistPlaying != null) {
            playlistSongs = null;
            currentPlaylistSong = null;
            playlistPlaying = false;
        }
        // update progress in the song
        recentSongRepo.updateLatestSong(currentSongId, mediaPlayer.getCurrentPosition(), new SongRepository.SongUpdateCallback() {
            @Override
            public void onSongUpdated(RecentSongs updatedSong) {
                // mhm
            }

            @Override
            public void onQueryFailed(Exception exception) {
                Log.e("song update", "Failed to update song: " + exception.getMessage());
            }
        });

        hideMediaPlayer();
        cleanUpMediaPlayer();
    }
}