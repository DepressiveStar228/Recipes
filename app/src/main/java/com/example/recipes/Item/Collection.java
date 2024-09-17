package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(
        tableName = "collection",
        indices = {@Index("id")}
)
public class Collection {
    @PrimaryKey(autoGenerate = true) private int id;
    @ColumnInfo(name = "name") private String name;
    @Ignore private ArrayList<Dish> dishes;

    @Ignore
    public Collection(int id, String name, ArrayList<Dish> dishes) {
        this.id = id;
        this.name = name;
        this.dishes = dishes;
    }

    @Ignore
    public Collection(String name, ArrayList<Dish> dishes) {
        this.name = name;
        this.dishes = dishes;
    }

    public Collection(String name) {
        this.name = name;
    }



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes = dishes;
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
        return id;
    }
}
