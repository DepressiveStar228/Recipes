package com.example.recipes.Item;

import java.io.DataInput;
import java.util.ArrayList;

import kotlin.Pair;

public class DataBox {
    private ArrayList<Pair<Dish, ArrayList<Ingredient>>> box;

    public DataBox(ArrayList<Dish> dishes, ArrayList<Ingredient> ingredients) {
        this.box = new ArrayList<>();

        if (!dishes.isEmpty()) {
            for (Dish dish : dishes) {
                ArrayList<Ingredient> dishIngredients = new ArrayList<>();

                if (!ingredients.isEmpty()) {
                    for (Ingredient in : ingredients) {
                        if (dish.getId() == in.getId_dish()) {
                            dishIngredients.add(in);
                        }
                    }
                }

                box.add(new Pair<>(dish, dishIngredients));
            }
        }
    }

    public ArrayList<Pair<Dish, ArrayList<Ingredient>>> getBox() {
        return box;
    }

    public void setBox(ArrayList<Pair<Dish, ArrayList<Ingredient>>> box) {
        this.box = box;
    }

    public ArrayList<Dish> getDishes() {
        ArrayList<Dish> dishes = new ArrayList<>();
        for (Pair<Dish, ArrayList<Ingredient>> pair : box) {
            dishes.add(pair.getFirst());
        }
        return dishes;
    }

    public boolean isEmpty() {
        return box.isEmpty();
    }

    public int size() {
        return box.size();
    }
}
