package com.example.recipes.Item;

import java.util.ArrayList;

public class DataBox {
    private ArrayList<Dish> dishes;
    private ArrayList<Ingredient> ingredients;

    public DataBox(ArrayList<Dish> dishes, ArrayList<Ingredient> ingredients) {
        this.dishes = dishes;
        this.ingredients = ingredients;
    }
    public void setData(ArrayList<Dish> dishes, ArrayList<Ingredient> ingredients){
        this.dishes = dishes;
        this.ingredients = ingredients;
    }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes = dishes;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}
