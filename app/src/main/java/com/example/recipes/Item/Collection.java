package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Objects;

@Entity(
        tableName = "collection",
        indices = {@Index("id")}
)
public class Collection {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "type") private String type;
    @Ignore private ArrayList<Dish> dishes = new ArrayList<>();
    @Ignore private ArrayList<IngredientShopList> ingredients = new ArrayList<>();
    @Ignore private ArrayList<Long> ids_dish_collection = new ArrayList<>();

    @Ignore
    public Collection(long id, String name, String type, ArrayList<Dish> dishes) {
        this.id = id;
        this.name = name;
        this.type = type;
        if (dishes != null) { this.dishes.addAll(dishes); }
    }

    @Ignore
    public Collection(String name, String type, ArrayList<Dish> dishes) {
        this.name = name;
        this.type = type;
        if (dishes != null) { this.dishes.addAll(dishes); }
    }

    @Ignore
    public Collection(String name, String type, ArrayList<Dish> dishes, ArrayList<IngredientShopList> ingredients) {
        this.name = name;
        this.type = type;
        if (dishes != null) { this.dishes.addAll(dishes); }
        if (ingredients != null) { this.ingredients.addAll(ingredients); }
    }

    @Ignore
    public Collection(Collection collection) {
        this.id = collection.getId();
        this.name = collection.getName();
        this.type = collection.getType();
        this.dishes.addAll(collection.getDishes());
        this.ingredients.addAll(collection.getIngredients());
        this.ids_dish_collection.addAll(collection.getIds_dish_collection());
    }

    public Collection(String name, String type) {
        this.name = name;
        this.type = type;
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

    public ArrayList<String> getNameIngredients() {
        ArrayList<String> names = new ArrayList<>();
        for (IngredientShopList ing : ingredients) {
            names.add(ing.getName());
        }
        return names;
    }

    public ArrayList<Long> getIds_dish_collection() {
        return ids_dish_collection;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) { this.type = type; }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes.addAll(dishes);
    }

    public void setIngredients(ArrayList<IngredientShopList> ingredients) {
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }

    public void setIds_dish_collection(ArrayList<Long> ids_dish_collection) {
        this.ingredients.clear();
        this.ids_dish_collection.addAll(ids_dish_collection);
    }

    public void addIngredient(IngredientShopList ing) {
        this.ingredients.add(ing);
    }

    public void addIngredients(ArrayList<IngredientShopList> ingredients) {
        this.ingredients.addAll(ingredients);
    }

    public void addDish(Dish dish) { dishes.add(dish); }

    public void deleteDish(Dish dish) {
        dishes.remove(dish);
    }

    public void addIDDish_Collection(long id) {
        ids_dish_collection.add(id);
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
