package com.example.recipes.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.recipes.Activity.MainActivity;
import com.example.recipes.Adapter.SearchResultsAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.OnBackPressedListener;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchDishFragment extends Fragment implements OnBackPressedListener {
    private EditText searchEditText;
    private ArrayList<Dish> dishes;
    private ArrayList<Ingredient> ingredients;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultsAdapter adapter;
    private ArrayList<Object> searchResults;
    private Switch searchSwitch;
    private ConstraintLayout mainLayout, mainArea;
    private GPTFragment childFragment;
    private FrameLayout gptFragment;
    private RecipeUtils utils;
    private Disposable disposable;
    private boolean flagOpenGPTContainer = false, flagOpenSearch = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(getContext());
        utils = new RecipeUtils(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_dish_activity, container, false);
        loadItemsActivity(view);
        loadClickListeners();

        if (savedInstanceState == null) {
            try {
                childFragment = new GPTFragment();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.fragmen_GPT_container, childFragment)
                        .commit();
                Log.d("SearchDishFragment", "Фрагмент GPT успішно вставлено.");
            } catch (Exception e) {
                Log.e("SearchDishFragment", "Не вдалося вставити фрагмент GPT.");
            }
        }

        return view;
    }

    @Override
    public boolean onBackPressed() {
        if (flagOpenGPTContainer) {
            flagOpenGPTContainer = false;
            childFragment.closeGPTContainer();
            return false;
        } else if (flagOpenSearch) {
            searchEditText.clearFocus();
            hideRecyclerView();
            hideSwitch();
            flagOpenSearch = false;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        updateRecipesData();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private void loadItemsActivity(View view){
        mainLayout = view.findViewById(R.id.mainLayout);
        ConstraintLayout linearLayout = view.findViewById(R.id.search_LinearLayout);
        gptFragment = view.findViewById(R.id.fragmen_GPT_container);

        searchEditText = linearLayout.findViewById(R.id.search_edit_text_my_dish);

        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView_my_dish);
        searchSwitch = view.findViewById(R.id.searchSwitch);

        searchResults = new ArrayList<>();
        adapter = new SearchResultsAdapter(searchResults);
        searchResultsRecyclerView.setAdapter(adapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Log.d("SearchDishFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners(){
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), searchEditText, 30);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                adapter.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateResults();
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.notifyDataSetChanged();
            }
        });

        searchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!searchSwitch.isChecked()) { searchSwitch.setText(getString(R.string.dishes)); }
            else { searchSwitch.setText(getString(R.string.ingredients)); }

            searchEditText.setText("");
        });

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                updateRecipesData();
                showRecyclerView();
                showSwitch();
                flagOpenSearch = true;
            }
        });

        mainLayout.setOnTouchListener((v, event) -> {
            int[] location = new int[2];
            gptFragment.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            int w = gptFragment.getWidth();
            int h = gptFragment.getHeight();

            if (event.getRawX() >= x && event.getRawX() <= (x + w) &&
                    event.getRawY() >= y && event.getRawY() <= (y + h)) {

                if (!flagOpenGPTContainer) {
                    flagOpenGPTContainer = true;
                    childFragment.openGPTContainer();
                }
            } else {
                if (flagOpenGPTContainer){
                    flagOpenGPTContainer = false;
                    childFragment.closeGPTContainer();
                }
            }

            if (searchEditText.hasFocus()) {
                searchEditText.clearFocus();
                hideRecyclerView();
                hideSwitch();
                hideKeyboard(v);
                flagOpenSearch = false;
            }

            return false;
        });

        Log.d("SearchDishFragment", "Слухачі фрагмента успішно завантажені.");
    }

    private ArrayList<Object> performSearch(String query) {
        if (!searchSwitch.isChecked()) {
            return performSearchByDish(query);
        } else {
            return performSearchByIngredient(query);
        }
    }

    private ArrayList<Object> performSearchByDish(String nameDish) {
        ArrayList<Object> result = new ArrayList<>();
        if (dishes != null) {
            String searchName = nameDish.toLowerCase();

            for (Dish dish : dishes) {
                String nameDishBox = dish.getName().toLowerCase();
                if (nameDishBox.contains(searchName)) {
                    result.add(dish);
                } else if (nameDishBox.isEmpty()){
                    result.add(dish);
                }
            }
        }
        Log.d("SearchDishFragment", "Пошук за стравами.");
        return result;
    }

    private ArrayList<Object> performSearchByIngredient(String nameIngredient) {
        ArrayList<Object> result = new ArrayList<>();
        if (ingredients != null) {
            String searchName = nameIngredient.toLowerCase();

            for (Ingredient ingredient : ingredients) {
                String nameIngredientBox = ingredient.getName().toLowerCase();
                if (nameIngredientBox.contains(searchName)) {
                    result.add(ingredient);
                } else if (nameIngredientBox.isEmpty()){
                    result.add(ingredient);
                }
            }
        }
        Log.d("SearchDishFragment", "Пошук за інгредієнтами.");
        return result;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Log.d("SearchDishFragment", "Клавіатура схована.");
    }

    private void showSwitch() {
        searchSwitch.setAlpha(0f);
        searchSwitch.setVisibility(View.VISIBLE);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(searchSwitch, "alpha", 0f, 1f);
        alphaAnimator.setDuration(300);

        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(searchSwitch, "translationY", -50f, 0f);
        translationAnimator.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translationAnimator);
        animatorSet.start();
        Log.d("SearchDishFragment", "Перемикач пошуку страви/інгредієнти з'явився.");
    }


    private void showRecyclerView() {
        searchResultsRecyclerView.setAlpha(0f);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
                .start();
        Log.d("SearchDishFragment", "Список страв/інгредієнтів з'явився.");
    }

    private void hideSwitch() {
        if (searchSwitch != null) {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(searchSwitch, "alpha", 1f, 0f);
            alphaAnimator.setDuration(300);

            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(searchSwitch, "translationY", 0f, -50f);
            translationAnimator.setDuration(300);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    searchSwitch.setVisibility(View.INVISIBLE);
                }
            });
            animatorSet.start();

            Log.d("SearchDishFragment", "Перемикач пошуку страви/інгредієнти захований.");
        }
    }


    private void hideRecyclerView() {
        if (searchResultsRecyclerView != null) {
            searchResultsRecyclerView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            searchResultsRecyclerView.setVisibility(View.GONE);
                        }
                    })
                    .start();

            Log.d("SearchDishFragment", "Список страв/інгредієнтів захований.");
        }
    }

    private void updateResults() {
        String query = searchEditText.getText().toString().trim();
        ArrayList<Object> searchResult = performSearch(query);
        searchResults.clear();
        searchResults.addAll(searchResult);
        adapter.notifyDataSetChanged();
    }

    private void updateRecipesData(){
        disposable = Single.zip(
                        utils.getAllDishes(),
                        utils.getIngredients(),
                        (dishes, ingredients) -> new Pair<>(dishes, ingredients)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            dishes = (ArrayList<Dish>) result.first;
                            ingredients = (ArrayList<Ingredient>) result.second;
                            updateResults();
                            Log.d("SearchDishFragment", "Список страв/інгредієнтів успішно оновлено.");
                        },
                        throwable -> {
                            dishes = new ArrayList<>();
                            ingredients = new ArrayList<>();
                            Toast.makeText(getContext(), getString(R.string.error_get_data), Toast.LENGTH_SHORT).show();
                            Log.e("SearchDishFragment", "Помилка оновлення списку страв/інгредієнтів.", throwable);
                        }
                );
    }
}
