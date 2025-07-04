package com.example.recipes.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.SearchResultsAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Fragments.SortAndFilterBottomSheetDialogFragment;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchDishActivity extends AppCompatActivity {
    private PreferencesController preferencesController;
    private AppCompatImageView back, sortFilterForDish, clearInputTextForDish;
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private ConstraintLayout searchDishLayout, emptyDish;
    private RecipeUtils utils;
    private SearchController<Dish> searchControllerForDish;
    private CompositeDisposable compositeDisposable;
    private String nameActivity;
    private SortAndFilterBottomSheetDialogFragment sortAndFilterFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preferencesController = PreferencesController.getInstance();
        utils = RecipeUtils.getInstance(this);

        super.onCreate(savedInstanceState);
        preferencesController.setPreferencesToActivity(this);
        setContentView(R.layout.search_dish_activity);

        compositeDisposable = new CompositeDisposable();
        nameActivity = this.getClass().getSimpleName();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadItemsActivity();
        loadClickListeners();
        setSearchController();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchEditText != null) searchEditText.requestFocus(); // Фокус на полі пошуку

        // Відновлення позиції скролу
        int position = getPreferences(Context.MODE_PRIVATE).getInt("scroll_position", 0);
        if (position >= 0 && searchResultsRecyclerView != null) {
            searchResultsRecyclerView.post(() -> searchResultsRecyclerView.smoothScrollToPosition(position));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (searchResultsRecyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) searchResultsRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                // Збереження позиції скролу
                int positionFirst = layoutManager.findFirstVisibleItemPosition();
                int positionLast = layoutManager.findLastVisibleItemPosition();
                int position = (positionFirst + positionLast) / 2;
                getPreferences(Context.MODE_PRIVATE).edit().putInt("scroll_position", position).apply();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (searchControllerForDish != null) searchControllerForDish.clear();
        Log.d(nameActivity, "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        if (sortAndFilterFragment != null) {
            if (sortAndFilterFragment.isVisible()) {
                sortAndFilterFragment.dismiss();
            } else { super.onBackPressed(); }
        } else { super.onBackPressed(); }
    }

    private void loadItemsActivity() {
        sortAndFilterFragment = SortAndFilterBottomSheetDialogFragment.newInstance(this::updateRecipesData);

        back = findViewById(R.id.back);
        sortFilterForDish = findViewById(R.id.sortFilter);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        searchDishLayout = findViewById(R.id.searchDishLayout);
        if (searchDishLayout != null) {
            searchEditText = searchDishLayout.findViewById(R.id.searchEditText);
            clearInputTextForDish = searchDishLayout.findViewById(R.id.clearInputTextButton);
            AppCompatImageView sort = searchDishLayout.findViewById(R.id.sortButton);

            if (sort != null) sort.setVisibility(View.GONE);

            AppCompatImageView searchImageView = searchDishLayout.findViewById(R.id.searchImageView);
            if (searchImageView != null) searchImageView.setVisibility(View.GONE);
        }

        emptyDish = findViewById(R.id.emptyDish);
    }

    private void loadClickListeners() {
        // Обмеження довжини тексту пошуку
        CharacterLimitTextWatcher.setCharacterLimit(this, searchEditText, Limits.MAX_CHAR_NAME_DISH);

        if (back != null) back.setOnClickListener(v -> finish());

        if (sortFilterForDish != null) {
            sortFilterForDish.setOnClickListener(view -> {
                if (sortAndFilterFragment != null && !sortAndFilterFragment.isVisible()) {
                    sortAndFilterFragment.show(getSupportFragmentManager(), "SortAndFilterBottomSheetDialogFragment");
                } else {
                    Log.e(nameActivity, "SortAndFilterBottomSheetDialogFragment is null");
                }
            });
        }
    }

    /**
     * Ініціалізує контролер пошуку для страв
     */
    private void setSearchController() {
        if (searchControllerForDish == null && searchEditText != null && searchResultsRecyclerView != null) {
            searchControllerForDish = new SearchController<>(this, searchEditText, searchResultsRecyclerView, new SearchResultsAdapter<Dish>(emptyDish, (view, item) -> {
                Dish dish = (Dish) item;
                Intent intent = new Intent(this, EditorDishActivity.class);
                intent.putExtra(IntentKeys.DISH_ID.name(), dish.getId());
                startActivity(intent); // Відкриття редактора страви
            }));

            if (clearInputTextForDish != null) searchControllerForDish.setClearSearchEditText(clearInputTextForDish);
        }

        utils.ByDish().getViewModel().getAll().observe(this, data -> {
            if (data != null) {
                updateRecipesData();
            }
        });
    }

    /**
     * Оновлює дані рецептів з урахуванням поточних фільтрів та сортування
     */
    private void updateRecipesData() {
        if (searchControllerForDish != null && sortAndFilterFragment != null) {
            Disposable disposable = sortAndFilterFragment.getResult()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(array_dishes -> {
                        if (searchControllerForDish != null) {
                            searchControllerForDish.setArrayData(new ArrayList<>(array_dishes));
                            searchControllerForDish.search(); // Оновлення результатів пошуку
                        }
                    });

            compositeDisposable.add(disposable);
        }
    }
}
