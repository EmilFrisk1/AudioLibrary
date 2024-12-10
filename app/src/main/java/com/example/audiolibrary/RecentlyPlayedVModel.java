package com.example.audiolibrary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class RecentlyPlayedVModel extends ViewModel {
    private final MutableLiveData<List<Item>> items = new MutableLiveData<>(new ArrayList<>());

    public void setItems(List<Item> newItems) {
        items.setValue(newItems);
    }

    public LiveData<List<Item>> getItems() {
        return items;
    }

    public void addItem (Item item) {
        List<Item> currentItems = items.getValue();
        if (currentItems == null) {
            currentItems = new ArrayList<>();
        }
        currentItems.add(item);
        items.setValue(currentItems);
    }
}
