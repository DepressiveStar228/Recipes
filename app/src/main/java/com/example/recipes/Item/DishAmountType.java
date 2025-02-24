package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "dish_amount_type",
        indices = @Index(value = "id_dish"),
        foreignKeys = {
                @ForeignKey(
                        entity = IngredientShopList_AmountType.class,
                        parentColumns = "id",
                        childColumns = "id_dish",
                        onDelete = ForeignKey.CASCADE
                )}
)
public class DishAmountType {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "id_amount_type") private long id_ingredient = 0;
    @ColumnInfo(name = "id_dish") private long id_dish = 0;

    @Ignore
    public DishAmountType(long id, long id_ingredient, long id_dish) {
        this.id = id;
        this.id_ingredient = id_ingredient;
        this.id_dish = id_dish;
    }

    public DishAmountType(long id_ingredient, long id_dish) {
        this.id_ingredient = id_ingredient;
        this.id_dish = id_dish;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId_dish() {
        return id_dish;
    }

    public void setId_dish(long id_dish) {
        this.id_dish = id_dish;
    }

    public long getId_ingredient() {
        return id_ingredient;
    }

    public void setId_ingredient(long id_ingredient) {
        this.id_ingredient = id_ingredient;
    }
}
