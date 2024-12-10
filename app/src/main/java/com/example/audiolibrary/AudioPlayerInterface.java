package com.example.audiolibrary;

import java.util.List;

public class AudioPlayerInterface {
    public interface AudioPlayerI {
        void onPlayAudio(Song song, List<Song> songList);
    }
}
