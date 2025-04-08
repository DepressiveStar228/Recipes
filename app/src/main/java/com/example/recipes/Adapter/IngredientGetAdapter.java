package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Enum.Limits;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку інгредієнтів.
 */
public class IngredientGetAdapter extends RecyclerView.Adapter<IngredientGetAdapter.IngredientViewHolder> {
    private Context context;
    private ConstraintLayout empty;
    private ArrayList<Ingredient> ingredients = new ArrayList<>();

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param empty Виджет для відображення порожнього стану.
     */
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
        holder.ingredientCountTypeTextView.setText(String.format("%s %s", ingredient.getAmount(), ingredient.getTypeString()));
    }

    /**
     * Встановлює новий список інгредієнтів, перевіряючи обмеження на максимальну кількість.
     *
     * @param ingredients Новий список інгредієнтів.
     */
    public void setIngredients(ArrayList<Ingredient> ingredients) {
        if (this.ingredients.size() + ingredients.size() <= Limits.MAX_COUNT_INGREDIENT.getLimit()) {
            this.ingredients.clear();
            this.ingredients.addAll(ingredients);
            notifyDataSetChanged();
            checkEmpty();
        } else {
            Toast.makeText(context, context.getString(R.string.warning_max_count_ingredients) + "(" + Limits.MAX_COUNT_INGREDIENT.getLimit() + ")", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Перевіряє, чи список інгредієнтів порожній, і оновлює відображення порожнього стану.
     */
    private void checkEmpty() {
        if (empty != null) {
            if (ingredients.isEmpty()) empty.setVisibility(View.VISIBLE);
            else empty.setVisibility(View.GONE);
        }
    }

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку інгредієнтів.
     */
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
