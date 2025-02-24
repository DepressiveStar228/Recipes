package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import io.reactivex.rxjava3.annotations.Nullable;

@Entity(
        tableName = "ingredient_shop_list_amount_type",
        indices = @Index(value = "id_ingredient"),
        foreignKeys = {
                @ForeignKey(
                        entity = IngredientShopList.class,
                        parentColumns = "id",
                        childColumns = "id_ingredient",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Dish_Collection.class,
                        parentColumns = "id",
                        childColumns = "id_dish",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class IngredientShopList_AmountType {
    @PrimaryKey(autoGenerate = true) private Long id;
    @ColumnInfo(name = "amount") private String amount = "";
    @ColumnInfo(name = "type") private String type = "";
    @ColumnInfo(name = "id_ingredient") private Long id_ingredient = 0L;
    @Nullable
    @ColumnInfo(name = "id_dish") private Long id_dish;

    @Ignore
    public IngredientShopList_AmountType() {}

    public IngredientShopList_AmountType(String amount, String type, Long id_ingredient, Long id_dish) {
        this.amount = amount;
        this.type = type;
        this.id_ingredient = id_ingredient;
        if (id_dish != null) {
            if (id_dish > 0) {
                this.id_dish = id_dish;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public Long getId_ingredient() {
        return id_ingredient;
    }

    @Nullable public Long getId_dish() {
        return id_dish;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setId_ingredient(Long id_ingredient) {
        this.id_ingredient = id_ingredient;
    }

    public void setId_dish(@Nullable Long id_dish) {
        this.id_dish = id_dish;
    }

    public void setType(String type) {
        this.type = type;
    }
}
