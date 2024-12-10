package com.example.audiolibrary;

import android.net.Uri;

public class Item {
    String name;
    Uri audioUri;
    int audioId;

    public Item(String name, Uri audioUri, int audioId) {
        this.name = name;
        this.audioUri = audioUri;
        this.audioId = audioId;
    }

    public int getAudioId() {
        return audioId;
    }

    public void setAudioId(int audioId) {
        this.audioId = audioId;
    }

    public String getName() {
        return name;
    }

    public Uri getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(Uri audioUri) {
        this.audioUri = audioUri;
    }

    public void setName(String name) {
        this.name = name;
    }
}
