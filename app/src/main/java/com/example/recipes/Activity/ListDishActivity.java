package com.example.recipes.Activity;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.SearchResultsAdapter;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;

public class ListDishActivity extends Activity {
    private ArrayList<Dish> dishes = new ArrayList<>();
    private TextView name_ing;
    private ArrayList<Object> listDishResults;
    private RecipeUtils utils;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);

        utils = new RecipeUtils(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_dish_by_ing_activity);

        loadItemsActivity();
        setDataRecycleView();
        Log.d("ListDishActivity", "Активність успішно створена");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        utils.close();
        Log.d("ListDishActivity", "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void loadItemsActivity() {
        RecyclerView listDishResultsRecyclerView = findViewById(R.id.listDishResultsRecyclerView);
        name_ing = findViewById(R.id.nameDishByIngredientTextView);

        listDishResults = new ArrayList<>();
        SearchResultsAdapter adapter = new SearchResultsAdapter(listDishResults);
        listDishResultsRecyclerView.setAdapter(adapter);
        listDishResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageView imageView = findViewById(R.id.back_list_dish_by_ingredient_imageView);
        imageView.setOnClickListener(v -> { finish(); });
        Log.d("ListDishActivity", "Елементи активності успішно завантажені");
    }

    private void setDataRecycleView() {
        String name = getIntent().getStringExtra("ing_name");
        ArrayList<Integer> dish_ids = new ArrayList<>();

        if (!name.isEmpty()) {
            name_ing.setText(name);

            try {
                dish_ids = utils.getDishIdsByNameIngredient(name);

                for (Integer dish_id : dish_ids){
                    dishes.add(utils.getDish(dish_id));
                }

                listDishResults.addAll(dishes);
                Log.d("ListDishActivity", "Страви успішно завантажені в RecyclerView");
            } catch (SQLException e) {
                Toast.makeText(this, getString(R.string.error_get_data), Toast.LENGTH_SHORT).show();
                Log.d("ListDishActivity", "Помилка завантаження страв у RecyclerView");
                finish();
            }
        } else {
            Log.d("ListDishActivity", "Помилка. Ім'я інгредієнта не було отримано");
        }
    }
}
