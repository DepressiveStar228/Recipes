package com.example.recipes.Item;

public class Ingredient {
    private int id;
    private String name;
    private String amount;
    private String type;
    private int id_dish;

    public Ingredient(int id, String name, String amount, String type, int id_dish){
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.id_dish = id_dish;
    }

    public Ingredient(String name, String amount, String type){
        this.name = name;
        this.amount = amount;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public int getID() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public int getID_Dish() {
        return id_dish;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setID_Dish(int id_dish) {
        this.id_dish = id_dish;
    }
}
