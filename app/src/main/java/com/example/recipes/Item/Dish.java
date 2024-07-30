package com.example.recipes.Item;

import java.util.ArrayList;

public class Dish {
    private int id;
    private String name;
    private String recipe;

    public Dish(int id, String name, String recipe){
        this.id = id;
        this.name = name;
        this.recipe = recipe;
    }

    public Dish(String name, String recipe){
        this.name = name;
        this.recipe = recipe;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Dish dish = (Dish) obj;

        return id == dish.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
