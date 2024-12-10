package com.example.audiolibrary;

import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentlyPlayedAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<Item> items;
    private final OnItemClickListener listener;

    public RecentlyPlayedAdapter(Context context, List<Item> items, OnItemClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Item song);
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Item item = items.get(position);
        holder.latestSongNameTxtView.setText(items.get(position).getName());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
