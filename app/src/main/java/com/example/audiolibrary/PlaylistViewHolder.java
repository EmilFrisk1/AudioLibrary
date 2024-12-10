package com.example.audiolibrary;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistViewHolder extends RecyclerView.ViewHolder {
    TextView playlistNameTxtView;
    //ImageView playlistFldrIcon;

    public PlaylistViewHolder(@NonNull View itemView) {
        super(itemView);
        playlistNameTxtView = itemView.findViewById(R.id.playlistNameTxtView);
      //  playlistFldrIcon = itemView.findViewById(R.id.playlistFldrIcon);
    }
}
