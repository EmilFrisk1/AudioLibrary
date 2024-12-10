package com.example.audiolibrary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements RecentlyPlayedAdapter.OnItemClickListener {
    private RecentlyPlayedVModel recentlyPlayedVModel;
    private OnAudioSystemInteraction mListener;
    private RecyclerView latestAudios;
    private Button selectAudioClick;
    private RecentlyPlayedAdapter adapter;
    private List<RecentSongs> latestSongs;
    private RecentSongsDao recentSongDao;
    private SongRepository recentSongRepo;
    private List<Item> latestAudioItems;

    public List<Item> getLatestAudioItems() {
        return latestAudioItems;
    }

    public interface OnAudioSystemInteraction {
        void onSelectAudioFile(String fragmentName);
        void onRecentlyPlayedClick(Item song);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAudioSystemInteraction) {
            mListener = (OnAudioSystemInteraction) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement OnAudioSystemInteraction");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recentlyPlayedVModel = new ViewModelProvider(requireActivity()).get(RecentlyPlayedVModel.class);
        adapter = new RecentlyPlayedAdapter(getContext(), latestAudioItems, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup adapter w recycleView
        latestAudios = view.findViewById(R.id.latestAudios);
        selectAudioClick = view.findViewById(R.id.selectMusicButton);

        setupEventListeners(selectAudioClick);

        latestAudios.setLayoutManager(new LinearLayoutManager(getContext()));
        latestAudios.setAdapter(adapter);

        // setup latest audios list
        recentlyPlayedVModel.getItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.setItems(items);
                adapter.notifyDataSetChanged();
            }
        });

        recentSongDao = DatabaseClient.getInstance(requireActivity().getApplication()).getAppDatabase().recentSongDao();
        recentSongRepo = new SongRepository(recentSongDao);

        recentSongRepo.getLatestSongs(10, new SongRepository.SongsQueryCallback() {
            @Override
            public void onSongsRetrieved(List<RecentSongs> songs) {
                if (songs != null) {
                    latestSongs = songs;
                    List<Item> latestSongs = new ArrayList<>();
                    for (int i = 0; i < songs.size(); i++) {
                        RecentSongs song = songs.get(i);
                        latestSongs.add(new Item(song.getSongName(), Uri.parse(song.getSongUri()), song.getId()));
                    }
                    recentlyPlayedVModel.setItems(latestSongs);
                }
            }

            @Override
            public void onQueryFailed(Exception exception) {
                Log.e("SongQUery", "Failed to get songs", exception);
          }
        });

    }

    @Override
    public void onItemClick(Item song) {
        mListener.onRecentlyPlayedClick(song);
    }

    private void setupEventListeners(Button button) {
        selectAudioClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSelectAudioFile("HOME_FRAGMENT");
            }
        });
    }
}

