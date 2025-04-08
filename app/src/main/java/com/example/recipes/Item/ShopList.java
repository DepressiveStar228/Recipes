package com.example.recipes.Item;

import android.content.Context;

import androidx.room.Ignore;

import com.example.recipes.Enum.CollectionType;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
public class ShopList extends Collection {
    private int allBoughtItems = 0;
    private int allItems = 0;
    private ArrayList<IngredientShopList> ingredients = new ArrayList<>();
    private ArrayList<Long> ids_dish_collection = new ArrayList<>();


    // Конструктори
    public <T> ShopList(T data) {
        super(data instanceof Collection ? (Collection) data : new Collection("", CollectionType.VOID));
        if (data instanceof ShopList shopList) this.ingredients.addAll(shopList.getIngredients());
    }

    public ShopList(String name, CollectionType type) {
        super(name, type);
    }



    // Геттери і сеттери
    public int getAllBoughtItems() {
        return allBoughtItems;
    }

    public void setAllBoughtItems(int allBoughtItems) {
        this.allBoughtItems = allBoughtItems;
    }

    public int getAllItems() {
        return allItems;
    }

    public void setAllItems(int allItems) {
        this.allItems = allItems;
    }

    public ArrayList<IngredientShopList> getIngredients() {
        return ingredients;
    }

    public ArrayList<Long> getIds_dish_collection() {
        return ids_dish_collection;
    }

    public void setIngredients(ArrayList<IngredientShopList> ingredients) {
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }

    public void setIds_dish_collection(ArrayList<Long> ids_dish_collection) {
        this.ingredients.clear();
        this.ids_dish_collection.addAll(ids_dish_collection);
    }


    // Інші методи
    public void addIngredient(IngredientShopList ing) {
        this.ingredients.add(ing);
    }

    public void addIngredients(ArrayList<IngredientShopList> ingredients) {
        this.ingredients.addAll(ingredients);
    }

    public void addIDDish_Collection(long id) {
        ids_dish_collection.add(id);
    }

    public String copyAsText(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.list) + " " + this.getName() + "\n");

        for (IngredientShopList ing : ingredients) {
            if (!ing.getIsBuy()) {
                stringBuilder.append("   - " + ing.getName() + ": " + ing.getGroupedAmountTypeToString(context) + "\n");
            }
        }

        return stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        ShopList shopList = (ShopList) object;
        return allBoughtItems == shopList.allBoughtItems && allItems == shopList.allItems && Objects.equals(ingredients, shopList.ingredients) && Objects.equals(ids_dish_collection, shopList.ids_dish_collection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), allBoughtItems, allItems, ingredients, ids_dish_collection);
    }
}
