package com.example.recipes.Adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Item.Dish;
import com.example.recipes.R;

import java.util.ArrayList;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private ArrayList<Object> searchResults;
    private OnItemClickListener listener;
    private int positionText;

    public SearchResultsAdapter(ArrayList<Object> searchResults, int positionText, OnItemClickListener listener) {
        this.searchResults = searchResults;
        this.positionText = positionText;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Object item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = searchResults.get(position);

        if (item instanceof Dish) {
            holder.textView.setText(((Dish) item).getName());
            holder.textView.setTextAlignment(positionText);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(v, item));
        } else if (item instanceof String) {
            holder.textView.setText(item.toString());
            holder.textView.setTextAlignment(positionText);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(v, item));
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void clear() {
        searchResults.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Object> dishes) {
        searchResults.addAll(dishes);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.itemResultTextView);
        }
    }
}
