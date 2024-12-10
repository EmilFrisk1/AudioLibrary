package com.example.audiolibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistFragment extends Fragment implements PlaylistAdapter.OnPlaylistItemInteraction {
    private PlaylistAdapter adapter;
    private RecyclerView playlistHolder;
    private List<PlaylistItem> playlistItems = new ArrayList<>();
    private PlaylistDao playlistDao;
    private Button createPlaylistBtn;
    private PlaylistDatabaseOperations playlistOperations;
    private RecyclerView playlistDetailsHolder;

    @Override
    public void onDetach() {
        super.onDetach();
        playlistOperations.cleanup();
    }

    @Override
    public void onPlaylistItemClickHold(PlaylistItem playlistItem) {
        // Delete playlist confirmation view
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete playlist");
        builder.setMessage("Are you sure you want to delete " + playlistItem.getName()+"?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            playlistOperations.deletePlaylist(playlistItem.getId(), new PlaylistDatabaseOperations.PlaylistDeleteCallback() {
                @Override
                public void onPlaylistDeleted() {
                    requireActivity().runOnUiThread(() -> {
                        int position = -1;
                        for (int i = 0; i < playlistItems.size(); i++) {
                            if (playlistItems.get(i).getId() == playlistItem.getId()) {
                                position = i;
                                break;
                            }
                        }

                        if (position != -1) {
                            playlistItems.removeIf(item -> item.getId() == playlistItem.getId());
                            adapter.notifyItemRemoved(position);
                        }
                    });
                }

                @Override
                public void onQueryFailed(Exception exception) {
                    Log.e("PlaylistQuery", "Failed to delete playlist", exception);
                }
            });
        });

        builder.setNegativeButton("no", (dialog, which) -> {dialog.dismiss();});

        builder.show();
    }

    @Override
    public void onPlaylistItemClick(PlaylistItem playlistItem) {
        PlaylistDetailsFragment fragment =  PlaylistDetailsFragment.newInstance(playlistItem.getId(), playlistItem.getName());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment, "PlaylistDetailsTag")
                .addToBackStack(null)
                .commit();
    }

    public PlaylistFragment() {
        // Required empty public constructor
    }

    public static PlaylistFragment newInstance() {
        return new PlaylistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistDao = DatabaseClient.getInstance(requireActivity().getApplication()).getAppDatabase().playlistDao();
        playlistOperations = new PlaylistDatabaseOperations(playlistDao);
        adapter = new PlaylistAdapter(getContext(), playlistItems, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createPlaylistBtn = view.findViewById(R.id.createPlaylistBtn);
        playlistHolder = view.findViewById(R.id.playlistHolder);
        playlistHolder.setLayoutManager(new LinearLayoutManager(getContext()));
        playlistHolder.setAdapter(adapter);

        createPlaylistBtn.setOnClickListener(v -> {
            // Inflate the layout for the dialog
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View dialogView = inflater.inflate(R.layout.create_playlist_dialog, null);

            // Setup the buttons in the custom layout
            EditText playlistNameEditText = dialogView.findViewById(R.id.playlistNameEditText);
            Button createButton = dialogView.findViewById(R.id.createButton);
            Button cancelButton = dialogView.findViewById(R.id.cancelButton);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(dialogView)
                    .setTitle("Create a new playlist");
            final AlertDialog dialog = builder.create();

            createButton.setOnClickListener(v2 -> {
                String playlistName = playlistNameEditText.getText().toString().trim();
                if (!playlistName.isEmpty()) {
                    playlistOperations.createPlaylist(new Playlist(playlistName), new PlaylistDatabaseOperations.PlaylistCreated() {
                        @Override
                        public void onPlaylistCreated(int playlistId) {
                            playlistItems.add(new PlaylistItem(playlistId, playlistName));
                            adapter.notifyItemInserted(playlistItems.size());
                        }

                        @Override
                        public void onQueryFailed(Exception exception) {
                            // Handle the failure here

                            Log.e("PlaylistQuery", "Failed to create playlist", exception);
                            // Optionally, show a toast or error message to the user
                        }
                    });
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Please enter a playlist name", Toast.LENGTH_SHORT);
                }
            });

            cancelButton.setOnClickListener(v3 -> {
                dialog.dismiss();
            });

            dialog.show();
        });

        // Fetch playlists and update recycle View
        if (playlistItems.size() < 1) {
            playlistOperations.getPlaylists(new PlaylistDatabaseOperations.GetPlaylistsCallback() {
                @Override
                public void onPlaylistsFetched(List<Playlist> playlists) {
                    // Update your RecyclerView here with the fetched playlists
                    for (int i = 0; i < playlists.size(); i++) {
                        Playlist playlist = playlists.get(i);
                        playlistItems.add(new PlaylistItem(playlist.getId(), playlist.getName()));
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onQueryFailed(Exception exception) {
                    Log.e("PlaylistQuery", "Failed to fetch playlists", exception);
                }
            });
        }
    }
}