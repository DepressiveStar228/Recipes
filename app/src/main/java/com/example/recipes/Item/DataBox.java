package com.example.recipes.Item;

import android.util.Pair;

import java.util.ArrayList;

public class DataBox {
    private ArrayList<Pair<Dish, ArrayList<Ingredient>>> box;

    public DataBox() {
        this.box = new ArrayList<>();
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
            dishes.add(pair.first);
        }
        return dishes;
    }

    public void addRecipe(Dish dish, ArrayList<Ingredient> ingredients) {
        box.add(new Pair<>(dish, ingredients));
    }

    public void addRecipe(Pair<Dish, ArrayList<Ingredient>> pair) {
        box.add(pair);
    }

    public boolean isEmpty() {
        return box.isEmpty();
    }

    public int size() {
        return box.size();
    }
}
