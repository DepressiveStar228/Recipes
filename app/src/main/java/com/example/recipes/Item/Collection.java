package com.example.recipes.Item;

import java.util.ArrayList;

public class Collection {
    private int id;
    private String name;
    private ArrayList<Dish> dishes;

    public Collection(int id, String name, ArrayList<Dish> dishes) {
        this.id = id;
        this.name = name;
        this.dishes = dishes;
    }

    public Collection(String name, ArrayList<Dish> dishes) {
        this.name = name;
        this.dishes = dishes;
    }

    public Collection(int id, String name) {
        this.id = id;
        this.name = name;
        this.dishes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes = dishes;
    }
}
