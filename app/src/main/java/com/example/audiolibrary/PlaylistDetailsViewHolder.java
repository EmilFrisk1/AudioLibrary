package com.example.audiolibrary;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistDetailsViewHolder  extends RecyclerView.ViewHolder {
    TextView playlistSongNameTxtView;

    public PlaylistDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistSongNameTxtView = itemView.findViewById(R.id.playlistSongNameTxtView);
    }
}
