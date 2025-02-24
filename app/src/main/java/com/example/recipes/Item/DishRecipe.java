package com.example.recipes.Item;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Enum.DishRecipeType;

@Entity(
        tableName = "dish_recipe",
        foreignKeys = {
                @ForeignKey(
                        entity = Dish.class,
                        parentColumns = "id",
                        childColumns = "id_dish",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "id_dish")
        }
)
public class DishRecipe {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "id_dish") private long id_dish;
    @Ignore private Bitmap bitmap;
    @ColumnInfo(name = "textData") private String textData = "";
    @ColumnInfo(name = "position") private int position = -1;
    @ColumnInfo(name = "type_data") private DishRecipeType typeData;

    @Ignore
    public DishRecipe() {}

    @Ignore
    public DishRecipe(long id, long id_dish, Bitmap bitmap, String textData, int position, DishRecipeType typeData) {
        this.id = id;
        this.id_dish = id_dish;
        this.bitmap = bitmap;
        this.textData = textData;
        this.position = position;
        this.typeData = typeData;
    }

    @Ignore
    public DishRecipe(long id_dish, DishRecipe dishRecipe) {
        this.id_dish = id_dish;
        this.bitmap = dishRecipe.getBitmap();
        this.textData = dishRecipe.getTextData();
        this.position = dishRecipe.getPosition();
        this.typeData = dishRecipe.getTypeData();
    }

    @Ignore
    public DishRecipe(String textData, DishRecipe dishRecipe) {
        this.id_dish = dishRecipe.getId_dish();
        this.bitmap = dishRecipe.getBitmap();
        this.textData = textData;
        this.position = dishRecipe.getPosition();
        this.typeData = dishRecipe.getTypeData();
    }

    @Ignore
    public DishRecipe(long id_dish, Bitmap bitmap, int position, DishRecipeType typeData) {
        this.id_dish = id_dish;
        this.bitmap = bitmap;
        this.position = position;
        this.typeData = typeData;
    }

    @Ignore
    public DishRecipe(long id_dish, DishRecipeType typeData) {
        this.id_dish = id_dish;
        this.typeData = typeData;
    }

    @Ignore
    public DishRecipe(String textData, int position, DishRecipeType typeData) {
        this.textData = textData;
        this.position = position;
        this.typeData = typeData;
    }

    public DishRecipe(long id_dish, String textData, int position, DishRecipeType typeData) {
        this.id_dish = id_dish;
        this.textData = textData;
        this.position = position;
        this.typeData = typeData;
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

    public String getTextData() {
        return textData;
    }

    public void setTextData(String textData) {
        this.textData = textData;
    }

    public DishRecipeType getTypeData() {
        return typeData;
    }

    public void setTypeData(DishRecipeType typeData) {
        this.typeData = typeData;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
