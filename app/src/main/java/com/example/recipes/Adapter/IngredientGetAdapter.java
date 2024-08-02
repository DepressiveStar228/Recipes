package com.example.recipes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;

public class IngredientGetAdapter extends RecyclerView.Adapter<IngredientGetAdapter.IngredientViewHolder> {
    private ArrayList<Ingredient> ingredients;

    public IngredientGetAdapter(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_item, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.ingredientNameTextView.setText(ingredient.getName());
        holder.ingredientCountTypeTextView.setText(String.format("%s %s", ingredient.getAmount(), ingredient.getType()));
    }

    public void addAll(ArrayList<Ingredient> ingredients) {
        this.ingredients.addAll(ingredients);
        notifyDataSetChanged();
    }

    public void clear() {
        ingredients.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientNameTextView;
        TextView ingredientCountTypeTextView;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientNameTextView = itemView.findViewById(R.id.ingredientNameItemTextView);
            ingredientCountTypeTextView = itemView.findViewById(R.id.ingredientCountTypeItemTextView);
        }
    }
}
