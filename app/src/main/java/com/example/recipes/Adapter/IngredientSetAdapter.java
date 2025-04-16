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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Decoration.CustomSpinnerAdapter;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку інгредієнтів з можливістю редагування назви, кількості та типу.
 * Підтримує підказки назв інгредієнтів.
 */
public class IngredientSetAdapter extends RecyclerView.Adapter<IngredientSetAdapter.IngredientViewHolder> {
    private Context context;
    private ConstraintLayout empty;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<String> allNameIngredients; // Список усіх можливих назв інгредієнтів
    private RecyclerView recyclerView;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param empty Виджет для відображення порожнього стану.
     * @param recyclerView RecyclerView для відображення списку.
     */
    public IngredientSetAdapter(Context context, ConstraintLayout empty, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.empty = empty;
        this.ingredients = new ArrayList<>();
        this.allNameIngredients = new ArrayList<>();
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

    @Override
    public void onViewRecycled(@NonNull IngredientViewHolder holder) {
        super.onViewRecycled(holder);
    }

    /**
     * Встановлює список усіх можливих назв інгредієнтів.
     *
     * @param names Список назв інгредієнтів.
     */
    public void setNamesIngredient(ArrayList<String> names) {
        allNameIngredients.clear();
        allNameIngredients.addAll(names);
        notifyDataSetChanged();
    }

    /**
     * Встановлює новий список інгредієнтів, перевіряючи обмеження на максимальну кількість.
     *
     * @param ingredients Новий список інгредієнтів.
     */
    public void setIngredients(ArrayList<Ingredient> ingredients) {
        if ((ingredients.size() + this.ingredients.size()) < Limits.MAX_COUNT_INGREDIENT.getLimit()) {
            this.ingredients.clear();
            this.ingredients.addAll(ingredients);
            notifyDataSetChanged();
            checkEmpty();
        } else {
            Toast.makeText(context, context.getString(R.string.warning_max_count_ingredients) + "(" + Limits.MAX_COUNT_INGREDIENT.getLimit() + ")", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Рецепти успішно імпортовані із файлу.");
        }
    }

    /**
     * Додає новий інгредієнт до списку.
     *
     * @param ingredient Інгредієнт для додавання.
     */
    public void addIngredient(Ingredient ingredient) {
        if (ingredients.size() < Limits.MAX_COUNT_INGREDIENT.getLimit()) {
            ingredients.add(ingredient);
            notifyItemInserted(ingredients.size() - 1);
            checkEmpty();
        } else {
            Toast.makeText(context, context.getString(R.string.warning_max_count_ingredients) + "(" + Limits.MAX_COUNT_INGREDIENT.getLimit() + ")", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Рецепти успішно імпортовані із файлу.");
        }
    }

    /**
     * Видаляє інгредієнт зі списку за позицією.
     *
     * @param position Позиція інгредієнта для видалення.
     */
    public void delIngredient(int position) {
        ingredients.remove(position);
        notifyItemRemoved(position);
        checkEmpty();
    }

    /**
     * Повертає список інгредієнтів.
     *
     * @return Список інгредієнтів.
     */
    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Оновлює дані інгредієнтів у списку на основі введених значень у ViewHolder.
     */
    public void updateIngredients() {
        for (int i = 0; i < getItemCount(); i++) {
            IngredientViewHolder holder = (IngredientViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                Ingredient ingredient = ingredients.get(i);
                ingredient.setName(holder.nameIngredientEditText.getText().toString());
                ingredient.setAmount(holder.countIngredientEditText.getText().toString());
                ingredient.setType(IngredientTypeConverter.toIngredientType(holder.spinnerTypeIngredient.getSelectedItem().toString()));
            }
        }
        checkEmpty();
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
    class IngredientViewHolder extends RecyclerView.ViewHolder {
        EditText nameIngredientEditText;
        EditText countIngredientEditText;
        Spinner spinnerTypeIngredient;
        ImageView deleteButton;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameIngredientEditText = itemView.findViewById(R.id.nameIngredientEditText);
            countIngredientEditText = itemView.findViewById(R.id.countIngredientEditText);
            spinnerTypeIngredient = itemView.findViewById(R.id.spinnerTypeIngredient);
            deleteButton = itemView.findViewById(R.id.imageButton);

            // Встановлюємо обмеження на кількість символів у полях вводу
            CharacterLimitTextWatcher.setCharacterLimit(context, nameIngredientEditText, Limits.MAX_CHAR_NAME_INGREDIENT);
            CharacterLimitTextWatcher.setCharacterLimit(context, countIngredientEditText, Limits.MAX_CHAR_AMOUNT_INGREDIENT);

            // Налаштовуємо спінер для вибору типу інгредієнта
            ArrayAdapter<String> languageAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_item, Arrays.asList(context.getResources().getStringArray(R.array.ingredient_types)));
            languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTypeIngredient.setAdapter(languageAdapter);

            // Обробка кліку на кнопку видалення
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    delIngredient(position);
                }
            });

            // Обробка зміни тексту у полі для вводу назви
            nameIngredientEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        ingredients.get(position).setName(s.toString());
                    }
                }
            });

            // Обробка зміни тексту у полі для вводу кількості
            countIngredientEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ingredients.get(position).setAmount(s.toString());
                    }
                }
            });

            // Обробка вибору типу інгредієнта
            spinnerTypeIngredient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        ingredients.get(adapterPosition).setType(IngredientTypeConverter.toIngredientType(parent.getItemAtPosition(position).toString()));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

        /**
         * Прив'язує дані до ViewHolder.
         *
         * @param ingredient Інгредієнт для відображення.
         */
        void bind(Ingredient ingredient) {
            nameIngredientEditText.setText(ingredient.getName());
            countIngredientEditText.setText(ingredient.getAmount());
            int index = getIndex(spinnerTypeIngredient, ingredient.getTypeString());
            spinnerTypeIngredient.setSelection(index);
        }

        /**
         * Повертає індекс елемента у спінері за його значенням.
         *
         * @param spinner Спинер.
         * @param myString Значення для пошуку.
         * @return Індекс елемента.
         */
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
