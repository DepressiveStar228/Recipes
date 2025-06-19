package com.example.recipes.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.IngredientNameHints;
import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Decoration.CustomSpinnerAdapter;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.ViewItem.CustomPopupWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку інгредієнтів з можливістю редагування назви, кількості та типу.
 * Підтримує підказки назв інгредієнтів.
 */
public class IngredientSetAdapter extends RecyclerView.Adapter<IngredientSetAdapter.IngredientViewHolder> {
    private final Context context;
    private final ConstraintLayout empty;
    private final ArrayList<Ingredient> ingredients;
    private final ArrayList<String> allNameIngredients; // Список усіх можливих назв інгредієнтів
    private final RecyclerView recyclerView;
    private final ArrayList<String> allTypes;
    private CustomPopupWindow customPopupWindow;

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
        this.allTypes = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.ingredient_types)));
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_ingredient_item, parent, false);
        return new IngredientViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
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
                ingredient.setType(IngredientTypeConverter.toIngredientType(holder.spinnerTypeIngredientTextView.getText().toString()));
            }
        }
        checkEmpty();
    }

    private void showDropDown(View anchorView, TextView spinnerTypeIngredientTextView, int position) {
        RecyclerView dropDownRecyclerView = new RecyclerView(context);
        dropDownRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        IngredientTypeSpinnerAdapter spinnerTypeIngredientAdapter = new IngredientTypeSpinnerAdapter(allTypes);
        dropDownRecyclerView.setAdapter(spinnerTypeIngredientAdapter);

        customPopupWindow = new CustomPopupWindow(context, anchorView, dropDownRecyclerView);
        customPopupWindow.setSize(90, 200);
        customPopupWindow.showPopup();

        spinnerTypeIngredientAdapter.setOnItemClickListener(item -> {
            spinnerTypeIngredientTextView.setText(item);
            Ingredient ingredient = ingredients.get(position);
            ingredient.setType(IngredientTypeConverter.toIngredientType(item));
            customPopupWindow.hidePopup();
        });
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
        ConstraintLayout spinnerTypeIngredient;
        TextView spinnerTypeIngredientTextView;
        AppCompatImageView deleteButton;
        IngredientNameHints ingredientNameHints;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameIngredientEditText = itemView.findViewById(R.id.nameIngredientEditText);
            countIngredientEditText = itemView.findViewById(R.id.countIngredientEditText);
            spinnerTypeIngredient = itemView.findViewById(R.id.spinnerTypeIngredient);
            spinnerTypeIngredientTextView = itemView.findViewById(R.id.spinnerTypeIngredientTextView);
            deleteButton = itemView.findViewById(R.id.imageButton);

            // Ініціалізуємо IngredientNameHints для підказок назв інгредієнтів
            ingredientNameHints = new IngredientNameHints(context, allNameIngredients);

            // Встановлюємо обмеження на кількість символів у полях вводу
            CharacterLimitTextWatcher.setCharacterLimit(context, nameIngredientEditText, Limits.MAX_CHAR_NAME_INGREDIENT);
            CharacterLimitTextWatcher.setCharacterLimit(context, countIngredientEditText, Limits.MAX_CHAR_AMOUNT_INGREDIENT);

            // Обробка кліку на спінер для вибору типу інгредієнта
            spinnerTypeIngredient.setOnClickListener(v -> showDropDown(spinnerTypeIngredient, spinnerTypeIngredientTextView, getAdapterPosition()));

            // Обробка кліку на кнопку видалення
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    delIngredient(position);
                }
            });

            // Налаштовуємо підказки для назви інгредієнта
            ingredientNameHints.setAnchorView(nameIngredientEditText); // Встановлюємо як якорь поле вводу назви
            ingredientNameHints.setPopupWindow(nameIngredientEditText); // Встановлюємо спливаюче вікно для підказок

            // Обробка зміни тексту у полі для вводу назви
            nameIngredientEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION && !s.toString().isEmpty()) {
                        ingredientNameHints.search(s.toString()); // Виклик методу пошуку підказок
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
        }

        /**
         * Прив'язує дані до ViewHolder.
         *
         * @param ingredient Інгредієнт для відображення.
         */
        void bind(Ingredient ingredient) {
            nameIngredientEditText.setText(ingredient.getName());
            countIngredientEditText.setText(ingredient.getAmount());
            spinnerTypeIngredientTextView.setText(IngredientTypeConverter.fromIngredientTypeBySettingLocale(ingredient.getType()));
        }
    }
}
