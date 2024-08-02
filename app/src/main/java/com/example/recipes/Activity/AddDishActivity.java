package com.example.recipes.Activity;

import android.app.Activity;
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

public class AddDishActivity extends Activity {
    private EditText nameEditText, recipeEditText;
    private IngredientSetAdapter ingredientAdapter;
    private ArrayList<Ingredient> ingredients;
    private RecipeUtils utils;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);
        utils = new RecipeUtils(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_dish_activity);

        loadItemsActivity();
        loadClickListeners();
        Log.d("AddDishActivity", "Активність успішно створена");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        utils.close();
        Log.d("AddDishActivity", "Активність успішно закрита");
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

            if (utils.checkDuplicateDishName(name)) {
                Toast.makeText(this, getString(R.string.warning_dublicate_name_dish), Toast.LENGTH_SHORT).show();
                return;
            }

            ingredientAdapter.updateIngredients();
            ingredients = ingredientAdapter.getIngredients();

            if (utils.addDish(new Dish(name, recipe), ingredients, Config.ID_MY_RECIPE_COLLECTION)) {
                Toast.makeText(this, getString(R.string.successful_add_dish), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    public void onAddIngredientButtonClick(View view) {
        ingredientAdapter.addIngredient(new Ingredient("", "", ""));
    }
}
