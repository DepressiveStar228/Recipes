package com.example.recipes.Adapter;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activity.ListDishActivity;
import com.example.recipes.Activity.ReadDataDishActivity;
import com.example.recipes.Controller.FileControllerDish;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private ArrayList<Object> searchResults;

    public SearchResultsAdapter(ArrayList<Object> searchResults) {
        this.searchResults = searchResults;
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
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ReadDataDishActivity.class);
                    intent.putExtra("dish_id", ((Dish) item).getID());
                    v.getContext().startActivity(intent);
                }
            });
        } else if (item instanceof Ingredient) {
            holder.textView.setText(((Ingredient) item).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ListDishActivity.class);
                    intent.putExtra("ing_name", ((Ingredient) item).getName());
                    v.getContext().startActivity(intent);
                }
            });
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

    public void addAll(ArrayList<Dish> dishes) {
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
