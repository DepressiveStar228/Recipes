package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
@Entity(
        tableName = "dish_collection",
        foreignKeys = {@ForeignKey(
                        entity = Dish.class,
                        parentColumns = "id",
                        childColumns = "id_dish",
                        onDelete = ForeignKey.CASCADE
                ), @ForeignKey(
                        entity = Collection.class,
                        parentColumns = "id",
                        childColumns = "id_collection",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index(value = "id_dish"), @Index(value = "id_collection")}
)
public class DishCollection {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "id_dish") private long idDish;
    @ColumnInfo(name = "id_collection") private long idCollection;


    // Конструктори
    @Ignore
    public DishCollection(long id, long idDish, long idCollection) {
        this.id = id;
        this.idDish = idDish;
        this.idCollection = idCollection;
    }

    public DishCollection(long idDish, long idCollection) {
        this.idDish = idDish;
        this.idCollection = idCollection;
    }


    // Геттери і сеттери
    public long getId() {
        return id;
    }

    public long getIdCollection() {
        return idCollection;
    }

    public long getIdDish() {
        return idDish;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setIdDish(long idDish) {
        this.idDish = idDish;
    }

    public void setIdCollection(long idCollection) {
        this.idCollection = idCollection;
    }

    // Інші методи
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        DishCollection that = (DishCollection) object;
        return id == that.id && idDish == that.idDish && idCollection == that.idCollection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idDish, idCollection);
    }
}
