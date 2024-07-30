package com.example.recipes.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Arrays;

public class IngredientSetAdapter extends RecyclerView.Adapter<IngredientSetAdapter.IngredientViewHolder> {
    private Context context;
    private ArrayList<Ingredient> ingredients;
    private RecyclerView recyclerView;

    public IngredientSetAdapter(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.ingredients = new ArrayList<>();
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
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public void addIngredient(Ingredient ingredient) {
        if (ingredients.size() < Config.COUNT_LIMIT_INGREDIENT) {
            ingredients.add(ingredient);
            notifyItemInserted(ingredients.size() - 1);
        } else {
            Toast.makeText(context, context.getString(R.string.warning_max_count_ingredients) + "(" + Config.COUNT_LIMIT_INGREDIENT + ")", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Рецепти успішно імпортовані із файлу.");
        }
    }

    public void delIngredient(int position) {
        ingredients.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void updateIngredients() {
        for (int i = 0; i < getItemCount(); i++) {
            IngredientViewHolder holder = (IngredientViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                Ingredient ingredient = ingredients.get(i);
                ingredient.setName(holder.nameIngredientEditText.getText().toString());
                ingredient.setAmount(holder.countIngredientEditText.getText().toString());
                ingredient.setType(holder.spinnerTypeIngredient.getSelectedItem().toString());
            }
        }
    }


    class IngredientViewHolder extends RecyclerView.ViewHolder {
        EditText nameIngredientEditText;
        EditText countIngredientEditText;
        Spinner spinnerTypeIngredient;
        ImageButton deleteButton;

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

            CharacterLimitTextWatcher.setCharacterLimit(context, nameIngredientEditText, Config.CHAR_LIMIT_NAME_INGREDIENT);
            CharacterLimitTextWatcher.setCharacterLimit(context, countIngredientEditText, Config.CHAR_LIMIT_AMOUNT_INGREDIENT);

            nameIngredientEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ingredients.get(position).setName(s.toString());
                    }
                }
            });

            countIngredientEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ingredients.get(position).setAmount(s.toString());
                    }
                }
            });

            spinnerTypeIngredient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        ingredients.get(adapterPosition).setType(parent.getItemAtPosition(position).toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        void bind(Ingredient ingredient) {
            nameIngredientEditText.setText(ingredient.getName());
            countIngredientEditText.setText(ingredient.getAmount());
            int index = getIndex(spinnerTypeIngredient, ingredient.getType());
            spinnerTypeIngredient.setSelection(index);
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
