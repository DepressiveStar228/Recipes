package com.example.recipes.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.IngredientSetAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.FileControllerDish;
import com.example.recipes.Controller.FileControllerIngredient;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Locale;

public class EditDishActivity extends Activity {
    private EditText nameEditText, recipeEditText;
    private RecyclerView addIngredientRecyclerView;
    private PerferencesController perferencesController;
    private IngredientSetAdapter ingredientAdapter;
    private ArrayList<Ingredient> ingredients;
    private ImageView imageView;
    private RecipeUtils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);

        utils = new RecipeUtils(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_dish_activity);

        loadItemsActivity();
        loadClickListeners();
        Log.d("EditDishActivity", "Активність успішно створена");
    }

    @Override
    protected void onResume(){
        super.onResume();
        int dishID = getIntent().getIntExtra("dish_id", -1);

        if (dishID != -1) {
            Dish dish = utils.getDish(dishID);

            nameEditText.setText(dish.getName());
            recipeEditText.setText(dish.getRecipe());

            ArrayList<Ingredient> ingByDish = utils.getIngredients(dishID);

            for (Ingredient in : ingByDish) {
                ingredientAdapter.addIngredient(in);
            }

            Log.d("EditDishActivity", "Активність успішно відобразилась");
        } else {
            Toast.makeText(this,  getString(R.string.error_edit_dish_null_id), Toast.LENGTH_SHORT).show();
            Log.e("EditDishActivity", "Помилка. Не вдалося отримати блюдо на редагування");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        utils.close();
        Log.d("EditDishActivity", "Активність успішно закрита");
    }

    private void loadItemsActivity(){
        nameEditText = findViewById(R.id.nameEditTextEditAct);
        recipeEditText = findViewById(R.id.recipeEditTextEditAct);
        addIngredientRecyclerView = findViewById(R.id.addIngredientRecyclerViewEditAct);

        ingredients = new ArrayList<>();
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

        Log.d("AddDishActivity", "Слухачі активності успішно завантажені");
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

            newDish = new Dish(name, recipe);
            int dishID = getIntent().getIntExtra("dish_id", -1);

            if (dishID != -1){
                utils.updateDish(dishID, newDish);

                ArrayList<Ingredient> ingByDish = utils.getIngredients(dishID);

                for (Ingredient in : ingByDish) {
                    utils.deleteIngredient(in);
                }

                ingredientAdapter.updateIngredients();
                ingredients = ingredientAdapter.getIngredients();

                if (!ingredients.isEmpty()) {
                    if (utils.addIngredients(dishID, ingredients)) {
                        Toast.makeText(this, getString(R.string.successful_edit_dish), Toast.LENGTH_SHORT).show();
                        Log.d("EditDishActivity", "Страва успішно відредагована");
                    }
                    else {
                        Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                        Log.d("EditDishActivity", "Помилка редагування страви");
                    }

                    finish();
                }
                else {
                    Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                    Log.d("EditDishActivity", "Помилка редагування страви");
                }
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

    private void updateDishOnActivity() {

    }
}
