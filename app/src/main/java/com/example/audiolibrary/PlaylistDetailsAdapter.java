package com.example.audiolibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistDetailsAdapter extends RecyclerView.Adapter<PlaylistDetailsViewHolder> {
    private Context context;
    private List<Song> playlistSongs;
    private OnPlaylistSongInteraction listener;

    public PlaylistDetailsAdapter(Context context, List<Song> playlistSongs, OnPlaylistSongInteraction listener) {
        this.playlistSongs = playlistSongs;
        this.context = context;
        this.listener = listener;
    }

    public interface OnPlaylistSongInteraction {
        void onPlaylistSongClick(Song song);
        void onPlaylistSongClickHold(Song song);
    }

    @NonNull
    @Override
    public PlaylistDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_song_item_view, parent, false);
        return new PlaylistDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistDetailsViewHolder holder, int position) {
        Song audio = playlistSongs.get(position);
        holder.playlistSongNameTxtView.setText(audio.getSongName());

        holder.itemView.setOnClickListener(v -> {
            listener.onPlaylistSongClick(audio);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistSongClickHold(audio);
                return true;
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return playlistSongs.size();
    }
}
