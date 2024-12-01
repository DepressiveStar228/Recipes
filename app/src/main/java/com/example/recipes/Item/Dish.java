package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "dish",
        indices = {@Index("id")}
)
public class Dish {
    @PrimaryKey(autoGenerate = true) private int id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "recipe") private String recipe;

    @Ignore
    public Dish(int id, String name, String recipe){
        this.id = id;
        this.name = name;
        this.recipe = recipe;
    }

    public Dish(String name, String recipe){
        this.name = name;
        this.recipe = recipe;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRecipe() {
        return recipe;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Dish dish = (Dish) obj;

        if (id != dish.id) return false;
        if (!name.equals(dish.name)) return false;
        return recipe.equals(dish.recipe);
    }


    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + name.hashCode();
        result = 31 * result + recipe.hashCode();
        return result;
    }

}
