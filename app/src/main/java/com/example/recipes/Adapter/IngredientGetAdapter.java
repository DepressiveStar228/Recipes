package com.example.recipes.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Config;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;

public class IngredientGetAdapter extends RecyclerView.Adapter<IngredientGetAdapter.IngredientViewHolder> {
    private Context context;
    private ConstraintLayout empty;
    private ArrayList<Ingredient> ingredients = new ArrayList<>();

    public IngredientGetAdapter(Context context, ArrayList<Ingredient> ingredients) {
        this.context = context;
        this.ingredients.addAll(ingredients);
    }

    public IngredientGetAdapter(Context context, ConstraintLayout empty) {
        this.context = context;
        this.empty = empty;
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
        checkEmpty();
    }

    public void clear() {
        ingredients.clear();
        notifyDataSetChanged();
        checkEmpty();
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        if (this.ingredients.size() + ingredients.size() <= Config.COUNT_LIMIT_INGREDIENT) {
            this.ingredients.clear();
            this.ingredients.addAll(ingredients);
            notifyDataSetChanged();
            checkEmpty();
        } else {
            Toast.makeText(context, context.getString(R.string.warning_max_count_ingredients) + "(" + Config.COUNT_LIMIT_INGREDIENT + ")", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    private void checkEmpty() {
        if (empty != null) {
            if (ingredients.isEmpty()) empty.setVisibility(View.VISIBLE);
            else empty.setVisibility(View.GONE);
        }
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
