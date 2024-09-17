package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "dish_collection",
        foreignKeys = {
                @ForeignKey(
                        entity = Dish.class,
                        parentColumns = "id",
                        childColumns = "id_dish",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Collection.class,
                        parentColumns = "id",
                        childColumns = "id_collection",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "id_dish"),
                @Index(value = "id_collection")
        }
)
public class Dish_Collection {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "id_dish") private long id_dish;
    @ColumnInfo(name = "id_collection") private long id_collection;

    @Ignore
    public Dish_Collection(long id, long id_dish, long id_collection) {
        this.id = id;
        this.id_dish = id_dish;
        this.id_collection = id_collection;
    }

    public Dish_Collection(long id_dish, long id_collection) {
        this.id_dish = id_dish;
        this.id_collection = id_collection;
    }

    public long getId() {
        return id;
    }

    public long getId_collection() {
        return id_collection;
    }

    public long getId_dish() {
        return id_dish;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId_dish(long id_dish) {
        this.id_dish = id_dish;
    }

    public void setId_collection(long id_collection) {
        this.id_collection = id_collection;
    }
}
