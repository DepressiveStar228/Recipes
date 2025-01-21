package com.example.recipes.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class IngredientSetAdapter extends RecyclerView.Adapter<IngredientSetAdapter.IngredientViewHolder> {
    private Context context;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<String> allNameIngredients;
    private RecyclerView recyclerView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private PerferencesController perferencesController;
    private int currentFocusedPosition = RecyclerView.NO_POSITION;

    public IngredientSetAdapter(Context context, ArrayList<String> allNameIngredients, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.allNameIngredients = allNameIngredients;
        this.ingredients = new ArrayList<>();
        this.perferencesController = new PerferencesController();
        perferencesController.loadPreferences(context);
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
        if (holder.disposable != null) {
            holder.disposable.dispose();
        }
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
        ImageView deleteButton;
        PopupWindow popupWindow;
        RecyclerView popupRecyclerView;
        SearchController searchController;
        Disposable disposable;
        PerferencesController controller;
        boolean accessShowPopup = false, isTouch = false;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameIngredientEditText = itemView.findViewById(R.id.nameIngredientEditText);
            countIngredientEditText = itemView.findViewById(R.id.countIngredientEditText);
            spinnerTypeIngredient = itemView.findViewById(R.id.spinnerTypeIngredient);
            deleteButton = itemView.findViewById(R.id.imageButton);

            CharacterLimitTextWatcher.setCharacterLimit(context, nameIngredientEditText, Config.CHAR_LIMIT_NAME_INGREDIENT);
            CharacterLimitTextWatcher.setCharacterLimit(context, countIngredientEditText, Config.CHAR_LIMIT_AMOUNT_INGREDIENT);

            controller = new PerferencesController();
            controller.loadPreferences(context);

            ArrayAdapter<String> languageAdapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_item, Arrays.asList(context.getResources().getStringArray(R.array.options_array)));
            languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTypeIngredient.setAdapter(languageAdapter);

            popupRecyclerView = new RecyclerView(context);
            searchController = new SearchController(context, new ArrayList<>(allNameIngredients), nameIngredientEditText, popupRecyclerView, (view, item) -> {
                accessShowPopup = false;
                nameIngredientEditText.setText(item.toString());
                hidePopup();
            });
            popupWindow = new PopupWindow(searchController.getRecyclerView(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            if (Objects.equals(perferencesController.getThemeNameBySpinner(perferencesController.getIndexTheme()), "Dark")) {
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
            } else {
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            }
            popupWindow.setHeight(300);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(false);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    delIngredient(position);
                }
            });

            nameIngredientEditText.setOnFocusChangeListener((v, hasFocus) -> {
                int position = getAdapterPosition();

                if (hasFocus && position != RecyclerView.NO_POSITION) {
                    currentFocusedPosition = position;
                    isTouch = true;
                } else if (!hasFocus) {
                    hidePopup();
                    if (currentFocusedPosition == position) {
                        currentFocusedPosition = RecyclerView.NO_POSITION;
                    }
                    isTouch = false;
                }
            });

            nameIngredientEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (controller.getStatus_ing_hints() && isTouch && accessShowPopup) {
                        int position = getAdapterPosition();

                        if (searchRunnable != null) {
                            handler.removeCallbacks(searchRunnable);
                        }

                        searchRunnable = () -> {
                            if (getAdapterPosition() == position) {
                                filterIngredients(s.toString().trim());
                            }
                        };

                        handler.postDelayed(searchRunnable, 100);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        ingredients.get(position).setName(s.toString());
                        accessShowPopup = true;
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

        private void showPopup(View anchorView) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }

            if (!popupWindow.isShowing()) {
                popupWindow.showAsDropDown(anchorView);
            }
        }


        private void hidePopup() {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }

        private void filterIngredients(String query) {
            if (!query.isEmpty()) {
                disposable = Observable.create(emitter -> {
                            ArrayList<String> filteredItems = new ArrayList<>();
                            for (String item : allNameIngredients) {
                                if (item.toLowerCase().contains(query.toLowerCase())) {
                                    filteredItems.add(item);
                                }
                            }
                            emitter.onNext(filteredItems);
                            emitter.onComplete();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(filteredItems -> {
                            if (filteredItems instanceof ArrayList<?>) {
                                ArrayList<String> list = (ArrayList<String>) filteredItems;

                                searchController.getSearchResults().clear();
                                searchController.getSearchResults().addAll(list);
                                searchController.getAdapter().notifyDataSetChanged();

                                if (!list.isEmpty()) {
                                    nameIngredientEditText.post(() -> showPopup(nameIngredientEditText));
                                } else {
                                    hidePopup();
                                }
                            }
                        });
            } else {
                hidePopup();
            }
        }
    }
}
