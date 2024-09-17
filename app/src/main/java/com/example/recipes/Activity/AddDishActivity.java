package com.example.recipes.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AddDishActivity extends Activity {
    private EditText nameEditText, recipeEditText;
    private IngredientSetAdapter ingredientAdapter;
    private ArrayList<Ingredient> ingredients;
    private RecipeUtils utils;
    private CompositeDisposable compositeDisposable;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);
        utils = new RecipeUtils(this);
        compositeDisposable = new CompositeDisposable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_dish_activity);

        loadItemsActivity();
        loadClickListeners();
        Log.d("AddDishActivity", "Активність успішно створена");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("AddDishActivity", "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void loadItemsActivity() {
        nameEditText = findViewById(R.id.nameEditText);
        recipeEditText = findViewById(R.id.recipeEditText);
        RecyclerView addIngredientRecyclerView = findViewById(R.id.addIngredientRecyclerView);

        ingredients = new ArrayList<>();
        ingredientAdapter = new IngredientSetAdapter(this, addIngredientRecyclerView);
        addIngredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addIngredientRecyclerView.setAdapter(ingredientAdapter);

        imageView = findViewById(R.id.back_add_dish_imageView);


        Log.d("AddDishActivity", "Елементи активності успішно завантажені");
    }

    private void loadClickListeners() {
        imageView.setOnClickListener(v -> { finish(); });

        CharacterLimitTextWatcher.setCharacterLimit(this, nameEditText, Config.CHAR_LIMIT_NAME_DISH);
        CharacterLimitTextWatcher.setCharacterLimit(this, recipeEditText, Config.CHAR_LIMIT_RECIPE_DISH);

        Log.d("AddDishActivity", "Слухачі активності успішно завантажені");
    }

    public void onAddButtonClick(View view) {
        String name = nameEditText.getText().toString().trim();
        String recipe = recipeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.warning_void_name_dish), Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(recipe)) {
                recipe = getString(R.string.default_recipe_text);
            }

            final String finalRecipe = recipe;
            Disposable checkDuplicateDisposable = utils.checkDuplicateDishName(name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            status -> {
                                if (status) {
                                    Toast.makeText(this, getString(R.string.warning_dublicate_name_dish), Toast.LENGTH_SHORT).show();
                                } else {
                                    ingredientAdapter.updateIngredients();
                                    ingredients = ingredientAdapter.getIngredients();
                                    boolean flagFullIngredient = true;

                                    for (Ingredient in : ingredients) {
                                        if (in.getType().isEmpty()) {
                                            in.setType(utils.getNameIngredientType(Config.VOID));
                                        }

                                        if (in.getName().isEmpty() || in.getAmount().isEmpty()) {
                                            flagFullIngredient = false;
                                            break;
                                        }
                                    }

                                    if (!flagFullIngredient) {
                                        Toast.makeText(this, getString(R.string.warning_set_all_data), Toast.LENGTH_SHORT).show();
                                    } else {
                                        addDish(new Dish(name, finalRecipe));
                                    }
                                }
                            },
                            throwable -> {
                                Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                                Log.e("AddDishActivity", "Ошибка при проверке дубликата", throwable);
                            }
                    );

            compositeDisposable.add(checkDuplicateDisposable);
        }
    }


    public void onAddIngredientButtonClick(View view) {
        ingredientAdapter.addIngredient(new Ingredient("", "", ""));
    }

    private void addDish(Dish dish) {
        Disposable addDishDisposable = utils.addDish(dish, ingredients, Config.ID_MY_RECIPE_COLLECTION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (success) {
                                Toast.makeText(this, getString(R.string.successful_add_dish), Toast.LENGTH_SHORT).show();
                                Log.d("AddDishActivity", "Страва успішно додана в бд");
                                finish();
                            } else {
                                Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                                Log.e("AddDishActivity", "Помилка при добаванні страви в бд");
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                            Log.e("AddDishActivity", "Помилка при добаванні страви в бд", throwable);
                            finish();
                        }
                );

        compositeDisposable.add(addDishDisposable);
    }
}
