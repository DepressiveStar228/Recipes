package com.example.recipes.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Arrays;

public class IngredientSetAdapter extends RecyclerView.Adapter<IngredientSetAdapter.IngredientViewHolder> {
    private Context context;
    private ArrayList<Ingredient> ingredients;

    public IngredientSetAdapter(Context context, ArrayList<Ingredient> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_ingredient_item, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient, position);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        notifyItemInserted(ingredients.size() - 1);
    }
    public void delIngredient(int position) {
        ingredients.remove(position);
        notifyItemRemoved(position);
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        EditText nameIngredientEditText;
        EditText countIngredientEditText;
        Spinner spinnerTypeIngredient;
        ImageButton deleteButton;
        TextWatcher nameTextWatcher;
        TextWatcher countTextWatcher;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameIngredientEditText = itemView.findViewById(R.id.nameIngredientEditText);
            countIngredientEditText = itemView.findViewById(R.id.countIngredientEditText);
            spinnerTypeIngredient = itemView.findViewById(R.id.spinnerTypeIngredient);
            deleteButton = itemView.findViewById(R.id.imageButton);

            ArrayAdapter<String> languageAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_item, Arrays.asList(context.getResources().getStringArray(R.array.options_array)));
            languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTypeIngredient.setAdapter(languageAdapter);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    delIngredient(position);
                }
            });

            CharacterLimitTextWatcher.setCharacterLimit(context, nameIngredientEditText, 30);
            CharacterLimitTextWatcher.setCharacterLimit(context, countIngredientEditText, 10);
        }

        void bind(Ingredient ingredient, int position) {
            // Remove previous TextWatchers
            if (nameTextWatcher != null) {
                nameIngredientEditText.removeTextChangedListener(nameTextWatcher);
            }
            if (countTextWatcher != null) {
                countIngredientEditText.removeTextChangedListener(countTextWatcher);
            }

            nameIngredientEditText.setText(ingredient.getName());
            countIngredientEditText.setText(ingredient.getAmount());
            int index = getIndex(spinnerTypeIngredient, ingredient.getType());
            spinnerTypeIngredient.setSelection(index);

            // Add new TextWatchers
            nameTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    ingredient.setName(s.toString());
                }
            };

            countTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    ingredient.setAmount(s.toString());
                }
            };

            nameIngredientEditText.addTextChangedListener(nameTextWatcher);
            countIngredientEditText.addTextChangedListener(countTextWatcher);
        }

        private int getIndex(Spinner spinner, String myString) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                    return i;
                }
            }
            return 0;
        }
    }
}
