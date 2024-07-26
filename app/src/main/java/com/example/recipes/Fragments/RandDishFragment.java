package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.Adapter.IngredientGetAdapter;
import com.example.recipes.Controller.FileControllerDish;
import com.example.recipes.Controller.FileControllerIngredient;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Random;

public class RandDishFragment extends Fragment {
    private Random random = new Random();
    private ArrayList<Dish> dishes;
    private ArrayList<Ingredient> ingredients;
    private TextView dish, recipe;
    private Button rand_button;
    private RecyclerView ingredientRecyclerView;
    private IngredientGetAdapter ingredientGetAdapter;
    private RecipeUtils utils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.rand_dish_activity, container, false);
        loadItemsActivity(view);

        utils = new RecipeUtils(getContext());
        dishes = utils.getDishesOrdered();

        onClickGetDish(view);
        return view;
    }

    private void loadItemsActivity(View view){
        dish = view.findViewById(R.id.nameDishTextView);
        recipe = view.findViewById(R.id.randRecipeTextView);
        rand_button = view.findViewById(R.id.getRandDishButton);

        ingredientRecyclerView = view.findViewById(R.id.ingredientRecyclerView);
        ingredientGetAdapter = new IngredientGetAdapter(new ArrayList<>());
        ingredientRecyclerView.setAdapter(ingredientGetAdapter);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        rand_button.setOnClickListener(this::onClickGetDish);
    }

    public void onClickGetDish(View view){
        if (dishes != null && !dishes.isEmpty()) {
            getDataDish(getRandomIndex());
        } else {
            Toast.makeText(getContext(), getString(R.string.error_void_dish), Toast.LENGTH_SHORT).show();
        }
    }

    private void getDataDish(Dish randDish) {
        dish.setText(randDish.getName());
        loadIngredients(randDish);
    }

    private Dish getRandomIndex() {
        return dishes.get(random.nextInt(dishes.size()));
    }

    private void loadIngredients(Dish randDish) {
        if (randDish != null && !randDish.getRecipe().isEmpty()){
            recipe.setVisibility(View.VISIBLE);
        }

        ingredients = utils.getIngredients(randDish.getID());
        ingredients.add(0, (new Ingredient(getString(R.string.ingredients), "", "")));
        ingredients.add((new Ingredient(" ", "", "")));
        recipe.setText(getString(R.string.recipe) + "\n" + randDish.getRecipe());

        if (ingredients != null && !ingredients.isEmpty()) {
            ingredientGetAdapter.clear();
            ingredientGetAdapter.addAll(ingredients);
        } else {
            Toast.makeText(getContext(), getString(R.string.error_get_ingredients), Toast.LENGTH_SHORT).show();
        }
    }
}
