package com.example.recipes.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.AddDishToCollectionsAdapter;
import com.example.recipes.Adapter.IngredientGetAdapter;
import com.example.recipes.Controller.ChatGPTTranslate;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.RecipeUtils;
import com.example.recipes.R;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ReadDataDishActivity extends Activity {
    private Dish dish;
    private PerferencesController perferencesController;
    private ArrayList<Ingredient> ingredients;
    private RecyclerView ingredientRecyclerView;
    private IngredientGetAdapter ingredientGetAdapter;
    private ChatGPTTranslate client;
    private DrawerLayout drawerLayout;
    private ImageView imageView, translateLoadImageView;
    private TextView nameDish, translate, recipeText;
    private LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4;
    private String translatedNameDish = "", translatedRecipeDish = "";
    private ArrayList<Ingredient> translatedIngredient = new ArrayList<>();
    private AnimationDrawable animationDrawable;
    private RecipeUtils utils;
    private boolean isTranslated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);
        client = new ChatGPTTranslate(this);
        utils = new RecipeUtils(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_data_item_activity);

        loadItemsActivity();
        loadClickListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int dishID = getIntent().getIntExtra("dish_id", -1);

        if (dishID != -1) {
            dish = utils.getDish(dishID);
            ingredients = utils.getIngredients(dishID);
            nameDish.setText(dish.getName());
            setDataRecyclerView();
        } else {
            Toast.makeText(this, getString(R.string.error_read_get_dish), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadItemsActivity(){
        ingredientRecyclerView = findViewById(R.id.ingredientReadRecyclerView);
        ingredientGetAdapter = new IngredientGetAdapter(new ArrayList<>());
        ingredientRecyclerView.setAdapter(ingredientGetAdapter);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        drawerLayout = findViewById(R.id.readDrawerLayout);
        NavigationView navigationView = findViewById(R.id.readNavigationView);
        View headerView = navigationView.getHeaderView(0);

        linearLayout1 = headerView.findViewById(R.id.setting_dish_item1);
        linearLayout2 = headerView.findViewById(R.id.setting_dish_item2);
        linearLayout3 = headerView.findViewById(R.id.setting_dish_item3);
        linearLayout4 = headerView.findViewById(R.id.setting_dish_item4);

        imageView = findViewById(R.id.back_read_dish_imageView);
        translateLoadImageView = findViewById(R.id.translate_loading_imageView);
        nameDish = findViewById(R.id.nameDishTextView);
        translate = findViewById(R.id.read_data_translate_btn);
        recipeText = findViewById(R.id.readRecipeTextView);
        animationDrawable = (AnimationDrawable) ContextCompat.getDrawable(this, R.drawable.loading_animation);
    }

    private void translateDish() {
        new TranslateTask().execute();
    }

    private void loadClickListeners(){
        linearLayout1.setOnClickListener(v -> {
            editDish();
        });
        linearLayout2.setOnClickListener(v -> {
            deleteDish();
        });
        linearLayout3.setOnClickListener(v -> {
            shareDish();
        });
        linearLayout4.setOnClickListener(v -> {
            showAddDishInCollectionDialog();
        });

        translate.setOnClickListener(v -> {
            if (!isTranslated) {
                if (translatedNameDish.isEmpty() || translatedRecipeDish.isEmpty() || translatedIngredient.isEmpty()) {
                    translateDish();
                } else {
                    isTranslated = true;
                    nameDish.setText(translatedNameDish);
                    setDataRecyclerView();
                    translate.setText(getString(R.string.translate_button_text_true));
                }
            } else {
                isTranslated = false;
                translate.setText(getString(R.string.translate_button_text_false));

                nameDish.setText(dish.getName());
                setDataRecyclerView();

                Log.d("ReadDataDishActivity", "Показ оригіналу страви");
            }
        });

        imageView.setOnClickListener(v -> { finish(); });
    }

    private void setDataRecyclerView() {
        ArrayList<Ingredient> ingredientArrayList;
        String recipe;

        if (isTranslated) {
            ingredientArrayList = new ArrayList<>(translatedIngredient);
            recipe = translatedRecipeDish;
        }
        else {
            ingredientArrayList = new ArrayList<>(ingredients);
            recipe = dish.getRecipe();
        }

        ingredientArrayList.add(0, (new Ingredient(getString(R.string.ingredients), "", "")));
        ingredientArrayList.add((new Ingredient(" ", "", "")));
        recipeText.setText(getString(R.string.recipe) + "\n" + recipe);
        ingredientGetAdapter.clear();
        ingredientGetAdapter.addAll(ingredientArrayList);
    }

    public void deleteDish() {
        RecipeUtils utils = new RecipeUtils(this);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_dish))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    if (utils.deleteDish(dish)) {
                        Toast.makeText(this, getString(R.string.successful_delete_dish), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, getString(R.string.error_delete_dish), Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    public void editDish() {
        Intent intent = new Intent(this, EditDishActivity.class);
        intent.putExtra("dish_id", dish.getID());
        startActivity(intent);
    }

    public void shareDish() {
        String ingredientsText = "";

        for (Ingredient ingredient : ingredients){
            String ingredientText = ingredient.getName() + "  " +
                    ingredient.getAmount() + ingredient.getType() + '\n';
            ingredientsText = ingredientsText + ingredientText;
        }

        String text = dish.getName() + "\n\n" + getString(R.string.ingredients) + "\n" + ingredientsText + "\n" + getString(R.string.recipe) + "\n" + dish.getRecipe();

        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, getString(R.string.copy_clipboard_text), Toast.LENGTH_SHORT).show();
    }

    private void showAddDishInCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);
        ArrayList<Collection> unused_collections = utils.getUnusedCollectionInDish(dish);
        AddDishToCollectionsAdapter adapter = new AddDishToCollectionsAdapter(this, unused_collections);
        collectionsRecyclerView.setAdapter(adapter);
        collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        builder.setView(dialogView)
                .setTitle(R.string.add_collection_dialog)
                .setPositiveButton(R.string.button_add, (dialog, which) -> {
                    ArrayList<Integer> selectedCollectionIds = adapter.getSelectedCollectionIds();
                    if (!selectedCollectionIds.isEmpty()){
                        if (utils.addDishCollectionData(dish, selectedCollectionIds)) {
                            Toast.makeText(this, getString(R.string.successful_add_dish_in_collection), Toast.LENGTH_SHORT).show();
                            Log.d("ReadDataDishActivity", "Страва успішно додана в колекцію(и)");
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    public void onClickSettingDish(View view) {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    private String getTypeIngredientForLang(String type) {
        String[] localArray = getResources().getStringArray(R.array.language_values);
        String[] optionsArray = getResources().getStringArray(R.array.options_array);

        for (String local : localArray){
            String[] optionsArrayLocal = getStringArrayForLocale(R.array.options_array, new Locale(local));
            int box = Arrays.asList(optionsArrayLocal).indexOf(type);
            if (box != -1) {
                Log.d("ReadDataDishActivity", "Успішний переклад типу мірила");
                return optionsArray[box];
            }
        }

        Log.d("ReadDataDishActivity", "Помилка перекладу типу мірила");
        return " ";
    }

    private String[] getStringArrayForLocale(int resId, Locale locale) {
        Resources resources = this.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        Resources localizedResources = this.createConfigurationContext(config).getResources();
        return localizedResources.getStringArray(resId);
    }

    private class TranslateTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            translateLoadImageView.setImageDrawable(animationDrawable);
            translateLoadImageView.setVisibility(View.VISIBLE);
            animationDrawable.start();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean flagSuccessfulTranslate = true;

            try {
                translatedNameDish = client.translate(dish.getName());
            } catch (IOException e) {
                flagSuccessfulTranslate = false;
                Log.e("ReadDataDishActivity", "Ошибка перекладу типу мірила");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            try {
                translatedRecipeDish = client.translate(dish.getRecipe());
            } catch (IOException e) {
                flagSuccessfulTranslate = false;
                Log.e("ReadDataDishActivity", "Помилка перекладу рецепту страви");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            try {
                translatedIngredient.clear();

                if (!ingredients.isEmpty()) {
                    for (Ingredient in : ingredients) {
                        String name = "";

                        if (!in.getAmount().isEmpty()){
                            if (!in.getName().isEmpty()){
                                name = client.translate(in.getName());
                            }

                            Ingredient trIn = new Ingredient(in.getID(), name, in.getAmount(), getTypeIngredientForLang(in.getType()), in.getID_Dish());
                            translatedIngredient.add(trIn);
                        }
                    }
                }
            } catch (IOException e) {
                flagSuccessfulTranslate = false;
                Log.e("ReadDataDishActivity", "Помилка переведення інгедієнтів страви");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            return flagSuccessfulTranslate;
        }

        @Override
        protected void onPostExecute(Boolean flagSuccessfulTranslate) {
            if (flagSuccessfulTranslate) {
                Log.d("ReadDataDishActivity", "Успішний переклад страви");
                isTranslated = true;

                nameDish.setText(translatedNameDish);
                setDataRecyclerView();

                translate.setText(getString(R.string.translate_button_text_true));
            } else {
                Toast.makeText(ReadDataDishActivity.this, "Помилка перекладу страви", Toast.LENGTH_SHORT).show();
            }

            translateLoadImageView.setVisibility(View.GONE);
            animationDrawable.stop();
        }
    }
}

