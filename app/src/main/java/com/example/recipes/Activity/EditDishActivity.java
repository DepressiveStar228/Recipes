package com.example.recipes.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.IngredientSetAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditDishActivity extends Activity {
    private EditText nameEditText, recipeEditText;
    private IngredientSetAdapter ingredientAdapter;
    private ImageView imageView;
    private int dishID;
    private RecipeUtils utils;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);

        utils = new RecipeUtils(this);
        compositeDisposable = new CompositeDisposable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_dish_activity);

        loadItemsActivity();
        loadClickListeners();
        Log.d("EditDishActivity", "Активність успішно створена");
    }

    @Override
    protected void onResume(){
        super.onResume();
        dishID = getIntent().getIntExtra("dish_id", -1);

        if (dishID != -1) {
            Disposable disposable = utils.getDish(dishID)
                    .flatMap(dish -> {
                                if (dish != null) {
                                    nameEditText.setText(dish.getName());
                                    recipeEditText.setText(dish.getRecipe());
                                    return utils.getIngredients(dish.getId());
                                } else {
                                    Log.e("EditDishActivity", "Помилка. Страва пуста");
                                    return Single.just(new ArrayList<Ingredient>());
                                }
                            },
                            throwable -> {
                                Log.e("EditDishActivity", "Помилка отримання страви з бд.", throwable);
                                return Single.just(new ArrayList<Ingredient>());
                            }
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            ingredients -> {
                                if (ingredients != null && !ingredients.isEmpty()) {
                                    for (Ingredient in : ingredients) {
                                        ingredientAdapter.addIngredient(in);
                                    }

                                    Log.d("EditDishActivity", "Активність успішно відобразилась. Всі дані рецепта отримані");
                                } else {
                                    Log.d("EditDishActivity", "Страва немає інгредієнтів.");
                                }
                            },
                            throwable -> {
                                Log.e("EditDishActivity", "Помилка отримання інгредієнтів з бд.", throwable);
                            }
                    );

            compositeDisposable.add(disposable);
        } else {
            Toast.makeText(this,  getString(R.string.error_edit_dish_null_id), Toast.LENGTH_SHORT).show();
            Log.e("EditDishActivity", "Помилка. Не вдалося отримати блюдо на редагування");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("EditDishActivity", "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void loadItemsActivity(){
        nameEditText = findViewById(R.id.nameEditTextEditAct);
        recipeEditText = findViewById(R.id.recipeEditTextEditAct);
        RecyclerView addIngredientRecyclerView = findViewById(R.id.addIngredientRecyclerViewEditAct);

        ingredientAdapter = new IngredientSetAdapter(this, addIngredientRecyclerView);
        addIngredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addIngredientRecyclerView.setAdapter(ingredientAdapter);

        imageView = findViewById(R.id.back_edit_dish_imageView);

        Log.d("EditDishActivity", "Елементи активності успішно завантажені");
    }

    private void loadClickListeners() {
        imageView.setOnClickListener(v -> { finish(); });

        CharacterLimitTextWatcher.setCharacterLimit(this, nameEditText, Config.CHAR_LIMIT_NAME_DISH);
        CharacterLimitTextWatcher.setCharacterLimit(this, recipeEditText, Config.CHAR_LIMIT_RECIPE_DISH);

        Log.d("EditDishActivity", "Слухачі активності успішно завантажені");
    }

    public void onEditButtonClick(View view) {
        Dish newDish;
        String name = nameEditText.getText().toString().trim();
        String recipe = recipeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.error_edit_dish_delete_name), Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(recipe)) {
                recipe = getString(R.string.default_recipe_text);
            }

            newDish = new Dish(dishID, name, recipe);

            if (dishID != -1){
                Disposable disposable = utils.updateDish(newDish)
                        .andThen(
                                utils.getIngredients(dishID)
                                        .flatMap(
                                                ingredients -> {
                                                    if (ingredients != null && !ingredients.isEmpty()) {
                                                        return utils.deleteIngredient(new ArrayList<>(ingredients))
                                                                .flatMap(status -> {
                                                                    if (status) {
                                                                        ingredientAdapter.updateIngredients();
                                                                        ArrayList<Ingredient> newIngredients = ingredientAdapter.getIngredients();
                                                                        return Single.just(newIngredients);
                                                                    } else {
                                                                        Log.e("EditDishActivity", "Помилка видалення інгредієнтів страви з БД.");
                                                                        return Single.just(new ArrayList<Ingredient>());
                                                                    }
                                                                });
                                                    } else {
                                                        Log.d("EditDishActivity", "Список інгредієнтів страви пустий.");
                                                        return Single.just(new ArrayList<Ingredient>());
                                                    }
                                                },
                                                throwable -> {
                                                    Log.e("EditDishActivity", "Помилка отримання інгредієнтів страви.", throwable);
                                                    return Single.just(new ArrayList<Ingredient>());
                                                }
                                        )
                                        .flatMap(
                                                ingredients -> {
                                                    if (ingredients != null && !ingredients.isEmpty()) {
                                                        return utils.addIngredients(dishID, new ArrayList<>(ingredients));
                                                    } else {
                                                        Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                                        Log.d("EditDishActivity", "Помилка редагування страви");
                                                        return Single.just(false);
                                                    }
                                                },
                                                throwable -> {
                                                    Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                                    Log.d("EditDishActivity", "Помилка редагування страви");
                                                    return Single.just(false);
                                                }
                                        )
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    if (result) {
                                        Toast.makeText(this, getString(R.string.successful_edit_dish), Toast.LENGTH_SHORT).show();
                                        Log.d("EditDishActivity", "Страва успішно відредагована");
                                    }
                                    else {
                                        Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                        Log.e("EditDishActivity", "Помилка редагування страви");
                                    }

                                    finish();
                                },
                                throwable -> {
                                    Log.e("EditDishActivity", "Помилка редагування страви", throwable);
                                    finish();
                                }
                        );

                compositeDisposable.add(disposable);
            }
            else {
                Toast.makeText(this, getString(R.string.error_edit_get_dish), Toast.LENGTH_SHORT).show();
                Log.d("EditDishActivity", "Помилка передачі страви на редагування");
            }
        }
    }

    public void onAddIngredientButtonClickEditAct(View view) {
        Ingredient newIngredient = new Ingredient("", "", "");
        ingredientAdapter.addIngredient(newIngredient);
    }
}
