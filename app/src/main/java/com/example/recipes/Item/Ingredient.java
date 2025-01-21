package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Interface.SelectableItem;

@Entity(
        tableName = "ingredient",
        indices = @Index(value = "id_dish"),
        foreignKeys = {
        @ForeignKey(
                entity = Dish.class,
                parentColumns = "id",
                childColumns = "id_dish",
                onDelete = ForeignKey.CASCADE
        )}
)
public class Ingredient implements SelectableItem {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "amount") private String amount;
    @ColumnInfo(name = "type") private String type;
    @ColumnInfo(name = "id_dish") private long id_dish;

    @Ignore
    public Ingredient(long id, String name, String amount, String type, long id_dish){
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.id_dish = id_dish;
    }

    @Ignore
    public Ingredient(String name, String amount, String type, long id_dish){
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
}
