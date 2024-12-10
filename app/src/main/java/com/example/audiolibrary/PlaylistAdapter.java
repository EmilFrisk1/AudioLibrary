package com.example.audiolibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistViewHolder>{
    private Context context;
    private List<PlaylistItem> playlistItems;
    private final OnPlaylistItemInteraction listener;

    public PlaylistAdapter(Context context, List<PlaylistItem> playlistItems, OnPlaylistItemInteraction listener) {
        this.listener = listener;
        this.context = context;
        this.playlistItems = playlistItems;
    }

    public interface OnPlaylistItemInteraction {
        void onPlaylistItemClick(PlaylistItem playlist);
        void onPlaylistItemClickHold(PlaylistItem playlist);
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_item_view, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
            PlaylistItem item = playlistItems.get(position);
            holder.playlistNameTxtView.setText(item.getName());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaylistItemClick(item);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onPlaylistItemClickHold(item);
                    return true;
                }
                return false;
            });
    }

    @Override
    public int getItemCount() {
        return playlistItems.size();
    }
}