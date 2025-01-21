package com.example.recipes.Controller;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activity.ReadDataDishActivity;
import com.example.recipes.Adapter.AddChooseObjectsAdapter;
import com.example.recipes.Adapter.SearchResultsAdapter;
import com.example.recipes.Config;
import com.example.recipes.Interface.SelectableItem;
import com.example.recipes.Item.Dish;

import java.util.ArrayList;
import java.util.Objects;

public class SearchController {
    private Context context;
    private ArrayList<Object> arrayData, searchResults;
    private EditText searchEditText;
    private RecyclerView.Adapter<?> adapter;
    private RecyclerView searchResultsRecyclerView;

    public SearchController(Context context, ArrayList<Object> arrayData, EditText searchEditText, RecyclerView searchResultsRecyclerView, AddChooseObjectsAdapter.OnItemClickListener listener) {
        this.context = context;
        this.arrayData = arrayData;
        this.searchEditText = searchEditText;
        this.searchResultsRecyclerView = searchResultsRecyclerView;
        this.searchResults = new ArrayList<>();
        adapter = new AddChooseObjectsAdapter(context, searchResults, listener);

        initializeRecyclerView();
        setListener();
    }

    public SearchController(Context context, ArrayList<Object> arrayData, EditText searchEditText, RecyclerView searchResultsRecyclerView, SearchResultsAdapter.OnItemClickListener listener) {
        this.context = context;
        this.arrayData = arrayData;
        this.searchEditText = searchEditText;
        this.searchResultsRecyclerView = searchResultsRecyclerView;
        this.searchResults = new ArrayList<>();
        adapter = new SearchResultsAdapter(searchResults, View.TEXT_ALIGNMENT_TEXT_START, listener);

        initializeRecyclerView();
        setListener();
    }

    private void initializeRecyclerView() {
        searchResultsRecyclerView.setAdapter(adapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    private void setListener() {
        CharacterLimitTextWatcher.setCharacterLimit(context, searchEditText, Config.CHAR_LIMIT_NAME_INGREDIENT);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchResults.clear();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateResults(performSearch(searchEditText.getText().toString().trim()), adapter);
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private ArrayList<Object> performSearch(String searchQuery) {
        ArrayList<Object> result = new ArrayList<>();

        if (arrayData != null) {
            String query  = searchQuery.toLowerCase();

            for (Object item : arrayData) {
                String searchBox = "";
                if (item instanceof SelectableItem) { searchBox = ((SelectableItem)item).getName().toLowerCase(); }
                else if (item instanceof String) { searchBox = item.toString().toLowerCase(); }

                if (searchBox.contains(query)) {
                    result.add(item);
                } else if (searchBox.isEmpty()){
                    result.add(item);
                }
            }
        }
        Log.d("SearchController", "Проведено пошук за даними.");

        return result;
    }

    private <T extends RecyclerView.Adapter<?>> void updateResults(ArrayList<Object> searchResult, T adapter) {
        searchResults.clear();
        searchResults.addAll(searchResult);
        adapter.notifyDataSetChanged();
    }

    public ArrayList<Object> getSearchResults() {
        return searchResults;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return adapter;
    }

    public RecyclerView getRecyclerView() { return searchResultsRecyclerView; }
}
