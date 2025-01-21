package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.recipes.Interface.SelectableItem;

@Entity(tableName = "ingredient_shop_list")
public class IngredientShopList implements SelectableItem {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "amount") private String amount;
    @ColumnInfo(name = "type") private String type;
    @ColumnInfo(name = "id_dish") private long id_dish;
    @ColumnInfo(name = "id_collection") private long id_collection;
    @ColumnInfo(name = "is_buy") private boolean isBuy = false;

    @Ignore
    public IngredientShopList(long id, String name, String amount, String type, long id_dish, long id_collection, boolean isBuy){
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.id_dish = id_dish;
        this.id_collection = id_collection;
        this.isBuy = isBuy;
    }

    @Ignore
    public IngredientShopList(String name, String amount, String type, long id_dish, long id_collection, boolean isBuy){
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.id_dish = id_dish;
        this.id_collection = id_collection;
        this.isBuy = isBuy;
    }

    public IngredientShopList(String name, String amount, String type, boolean isBuy){
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.isBuy = isBuy;
    }

    @Ignore
    public IngredientShopList(Ingredient ingredient, boolean isBuy){
        this.name = ingredient.getName();
        this.amount = ingredient.getAmount();
        this.type = ingredient.getType();
        this.isBuy = isBuy;
    }

    @Ignore
    public IngredientShopList(){}

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public long getId_dish() {
        return id_dish;
    }

    public long getId_collection() {
        return id_collection;
    }

    public boolean getIsBuy() {
        return isBuy;
    }




    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId_dish(long id_dish) {
        this.id_dish = id_dish;
    }

    public void setId_collection(long id_collection) {
        this.id_collection = id_collection;
    }

    public void setIsBuy(boolean isBuy) {
        this.isBuy = isBuy;
    }
}
