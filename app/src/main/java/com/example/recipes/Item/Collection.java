package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(
        tableName = "collection",
        indices = {@Index("id")}
)
public class Collection {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "type") private String type;
    @Ignore private ArrayList<Dish> dishes;
    @Ignore private ArrayList<IngredientShopList> ingredients;

    @Ignore
    public Collection(long id, String name, String type, ArrayList<Dish> dishes) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.dishes = dishes;
        this.ingredients = new ArrayList<>();
    }

    @Ignore
    public Collection(String name, String type, ArrayList<Dish> dishes) {
        this.name = name;
        this.type = type;
        this.dishes = dishes;
        this.ingredients = new ArrayList<>();
    }

    @Ignore
    public Collection(String name, String type, ArrayList<Dish> dishes, ArrayList<IngredientShopList> ingredients) {
        this.name = name;
        this.type = type;
        this.dishes = dishes;
        this.ingredients = ingredients;
    }

    public Collection(String name, String type) {
        this.name = name;
        this.type = type;
        this.dishes = new ArrayList<>();
        this.ingredients = new ArrayList<>();
    }



    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() { return type; }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public ArrayList<IngredientShopList> getIngredients() {
        return ingredients;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) { this.type = type; }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes = dishes;
    }

    public void setIngredients(ArrayList<IngredientShopList> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(IngredientShopList ing) {
        this.ingredients.add(ing);
    }

    public void addIngredients(ArrayList<IngredientShopList> ingredients) {
        this.ingredients.addAll(ingredients);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Collection collection = (Collection) obj;

        return id == collection.id;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }
}
