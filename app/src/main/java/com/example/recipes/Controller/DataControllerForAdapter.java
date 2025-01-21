package com.example.recipes.Controller;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public interface DataControllerForAdapter<T> {
    void addItem(RecyclerView recyclerView, T item);
    void addItems(RecyclerView recyclerView, ArrayList<T> items);
    void delItem(RecyclerView recyclerView, T item);
    void upItem(RecyclerView recyclerView, T item);
    void setItems(ArrayList<T> items);
    int getPosition(T item);
}

