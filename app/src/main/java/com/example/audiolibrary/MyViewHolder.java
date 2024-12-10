package com.example.audiolibrary;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView latestSongNameTxtView;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        latestSongNameTxtView = itemView.findViewById(R.id.latestSongNameTxtView);
    }
}
