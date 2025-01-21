package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Interface.SelectableItem;

@Entity(
        tableName = "dish",
        indices = {@Index("id")}
)
public class Dish implements SelectableItem {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "recipe") private String recipe;
    @ColumnInfo(name = "timestamp") private long timestamp;

    @Ignore
    public Dish(long id, String name, String recipe, long timestamp){
        this.id = id;
        this.name = name;
        this.recipe = recipe;
        this.timestamp = timestamp;
    }

    @Ignore
    public Dish(long id, String name, String recipe){
        this.id = id;
        this.name = name;
        this.recipe = recipe;
        this.timestamp = System.currentTimeMillis();
    }

    public Dish(String name, String recipe){
        this.name = name;
        this.recipe = recipe;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getRecipe() {
        return recipe;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Dish dish = (Dish) obj;

        if (id != dish.id) return false;
        if (!name.equals(dish.name)) return false;
        if (timestamp != dish.timestamp) return false;
        return recipe.equals(dish.recipe);
    }


    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + name.hashCode();
        result = 31 * result + recipe.hashCode();
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }

}
