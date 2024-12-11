package com.example.audiolibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaylistDetailsFragment extends Fragment implements MainActivity.OnSongInsertionListener, PlaylistDetailsAdapter.OnPlaylistSongInteraction {
    private static final String ARG_PLAYLIST_ID = "playlist_id";
    private static final String ARG_PLAYLIST_NAME = "playlist_name";
    private int playlistId;
    private String playlistName;
    private TextView playlistDetailsTxtView;
    private PlaylistDatabaseOperations playlistDatabaseOperations;
    private PlaylistDao playlistDao;
    private List<PlaylistSong> playlistSongsList;
    private List<Song> songList = new ArrayList<>();
    private RecyclerView playlistDetailsHolder;
    private PlaylistDetailsAdapter adapter;
    private Button addAudioBtn;
    private HomeFragment.OnAudioSystemInteraction mListener;
    private AudioPlayerInterface.AudioPlayerI audioPlayerListener;
    private ImageButton shuffleIcon;

    @Override
    public void onPlaylistSongClickHold(Song song) {
        // Delete playlist confirmation view
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete song");
        builder.setMessage("Are you sure you want to delete " + song.getSongName() +"from the playlist?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            SongDao songDao = DatabaseClient.getInstance(requireActivity().getApplication()).getAppDatabase().songDao();
             playlistDatabaseOperations.deletePlaylistSong(playlistId, song.getId(), songDao, new PlaylistDatabaseOperations.PlaylistSongDeleted() {
                 @Override
                 public void onPlaylistSongDeleted() {
                     int removedSongPosition = -1;
                     for (int i = 0; i < songList.size(); i++) {
                         if (songList.get(i).getId() == song.getId()) {
                             removedSongPosition = i;
                             break;
                         }
                     }
                     songList.remove(removedSongPosition);
                     adapter.notifyDataSetChanged();
                 }

                 @Override
                 public void onQueryFailed(Exception exception) {

                 }
             });
        });

        builder.setNegativeButton("no", (dialog, which) -> {dialog.dismiss();});

        builder.show();
    }

    @Override
    public void onPlaylistSongClick(Song song) {
        audioPlayerListener.onPlayAudio(song, songList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeFragment.OnAudioSystemInteraction) {
            mListener = (HomeFragment.OnAudioSystemInteraction) context;
        }
        if (context instanceof AudioPlayerInterface.AudioPlayerI) {
            audioPlayerListener = (AudioPlayerInterface.AudioPlayerI) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        playlistDatabaseOperations.cleanup();
        mListener = null;
        audioPlayerListener = null;
    }

    @Override
    public void OnSongInserted(Song song) {
        insertPlaylistSong(song.getId(), song);
    }

    public PlaylistDetailsFragment() {
        // Required empty public constructor
    }

    public static PlaylistDetailsFragment newInstance(int playlistId, String playlistName) {
        PlaylistDetailsFragment fragment = new PlaylistDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYLIST_ID, playlistId);
        args.putString(ARG_PLAYLIST_NAME, playlistName);
        fragment.setArguments(args);
        return fragment;
    }

    private void shufflePlaylist() {
        if (songList != null) {
            Random rand = new Random();
            List<Integer> newSongOrderPositions = new ArrayList<>();
            int songListSize = songList.size();

            for (int i = 0; i < songListSize; i++) {
                Boolean newUniquePostion = false;
                while (!newUniquePostion) {
                    int randomNum = rand.nextInt(songListSize);
                    if (!newSongOrderPositions.contains(randomNum)) {
                        newSongOrderPositions.add(randomNum);
                        break;
                    }
                }
            }

            List<Song> shuffledSongs = new ArrayList<>();

            for (int position : newSongOrderPositions) {
                shuffledSongs.add(songList.get(position));
            }
            songList.clear();
            songList.addAll(shuffledSongs);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (getArguments() != null) {
            playlistId = getArguments().getInt(ARG_PLAYLIST_ID);
            playlistName = getArguments().getString(ARG_PLAYLIST_NAME);
        }
        adapter = new PlaylistDetailsAdapter(getContext(), songList, this);
        playlistDao = DatabaseClient.getInstance(requireActivity().getApplication()).getAppDatabase().playlistDao();
        playlistDatabaseOperations = new PlaylistDatabaseOperations(playlistDao);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playlistDetailsHolder = view.findViewById(R.id.playlistDetailsHolder);
        playlistDetailsTxtView = view.findViewById(R.id.playlistDetailsTxtView);
        shuffleIcon = view.findViewById(R.id.playlistShuffleIcon);

        addAudioBtn = view.findViewById(R.id.addAudioBtn);

        playlistDetailsHolder.setLayoutManager(new LinearLayoutManager(getContext()));
        playlistDetailsHolder.setAdapter(adapter);

        playlistDetailsTxtView.setText(playlistDetailsTxtView.getText() + playlistName);
        playlistDao = DatabaseClient.getInstance(requireActivity().getApplication()).getAppDatabase().playlistDao();

        addAudioBtn.setOnClickListener(v -> {
            mListener.onSelectAudioFile("PLAYLIST_DETAILS_FRAGMENT");
        });

        if (songList.size() < 1) {
            fetchSongsForPlaylist(playlistId);
        }

        shuffleIcon.setOnClickListener(v -> {
            shufflePlaylist();
        });
    }

    private void fetchSongsForPlaylist(int playlistId) {
        playlistDatabaseOperations.getPlaylistSongs(playlistId, new PlaylistDatabaseOperations.GetPlaylistSongsCallback() {
            @Override
            public void onPlaylistSongsFetched(List<Song> playlistSongs) {
                if (playlistSongs.size() > 0) {
                    songList.addAll(playlistSongs);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onQueryFailed(Exception exception) {
                Log.e("PlaylistQuery", "Failed to fetch playlists", exception);
            }
        });
    }

    private void insertPlaylistSong(int songId, Song song) {
        playlistDatabaseOperations.insertPlaylistSong(playlistId, songId, new PlaylistDatabaseOperations.InsertPlaylistSongCallback() {
            @Override
            public void onPlaylistSongInserted() {
                songList.add(song);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onQueryFailed(Exception exception) {
                String message = exception.getMessage();
                if (message != null && message.contains("UNIQUE constraint failed: playlist_song.playlistId, playlist_song.songId")) {
                    // Show a toast for this specific error
                    Toast.makeText(requireContext(), "This song is already in the playlist!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}