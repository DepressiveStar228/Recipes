package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Enum.DishRecipeType;

import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
@Entity(
        tableName = "dish_recipe",
        foreignKeys = {@ForeignKey(
                        entity = Dish.class,
                        parentColumns = "id",
                        childColumns = "id_dish",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index(value = "id_dish"), @Index(value = "type_data")}
)
public class DishRecipe {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "id_dish") private long idDish;
    @ColumnInfo(name = "text_data") private String textData = "";
    @ColumnInfo(name = "position") private int position = -1;
    @ColumnInfo(name = "type_data") private DishRecipeType typeData;


    // Конструктори
    @Ignore
    public DishRecipe() { }

    @Ignore
    public DishRecipe(long id, long idDish, String textData, int position, DishRecipeType typeData) {
        this.id = id;
        this.idDish = idDish;
        this.textData = textData;
        this.position = position;
        this.typeData = typeData;
    }

    @Ignore
    public DishRecipe(long idDish, DishRecipe dishRecipe) {
        this.idDish = idDish;
        this.textData = dishRecipe.getTextData();
        this.position = dishRecipe.getPosition();
        this.typeData = dishRecipe.getTypeData();
    }

    @Ignore
    public DishRecipe(String textData, DishRecipe dishRecipe) {
        this.idDish = dishRecipe.getIdDish();
        this.textData = textData;
        this.position = dishRecipe.getPosition();
        this.typeData = dishRecipe.getTypeData();
    }

    @Ignore
    public DishRecipe(long idDish, int position, DishRecipeType typeData) {
        this.idDish = idDish;
        this.position = position;
        this.typeData = typeData;
    }

    @Ignore
    public DishRecipe(long idDish, DishRecipeType typeData) {
        this.idDish = idDish;
        this.typeData = typeData;
    }

    @Ignore
    public DishRecipe(String textData, int position, DishRecipeType typeData) {
        this.textData = textData;
        this.position = position;
        this.typeData = typeData;
    }

    public DishRecipe(long idDish, String textData, int position, DishRecipeType typeData) {
        this.idDish = idDish;
        this.textData = textData;
        this.position = position;
        this.typeData = typeData;
    }


    // Геттери і сеттери
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdDish() {
        return idDish;
    }

    public void setIdDish(long idDish) {
        this.idDish = idDish;
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


    // Інші методи

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        DishRecipe that = (DishRecipe) object;
        return id == that.id && idDish == that.idDish && position == that.position && Objects.equals(textData, that.textData) && typeData == that.typeData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idDish, textData, position, typeData);
    }
}