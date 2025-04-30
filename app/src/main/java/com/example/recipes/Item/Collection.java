package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Interface.Item;

import java.util.ArrayList;
import java.util.Objects;



/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
@Entity(
        tableName = "collection",
        indices = {@Index("id")}
)
public class Collection implements Item {
    public static final String SYSTEM_COLLECTION_TAG = "#%$*@";

    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "type") private CollectionType type;
    @Ignore private ArrayList<Dish> dishes = new ArrayList<>();


    // Конструктори
    @Ignore
    public Collection(long id, String name, CollectionType type, ArrayList<Dish> dishes) {
        this.id = id;
        this.name = name;
        this.type = type;
        if (dishes != null) { this.dishes.addAll(dishes); }
    }

    @Ignore
    public Collection(String name, CollectionType type, ArrayList<Dish> dishes) {
        this.name = name;
        this.type = type;
        if (dishes != null) { this.dishes.addAll(dishes); }
    }

    @Ignore
    public Collection(Collection collection) {
        this.id = collection.getId();
        this.name = collection.getName();
        this.type = collection.getType();
        this.dishes.addAll(collection.getDishes());
    }

    public Collection(String name, CollectionType type) {
        this.name = name;
        this.type = type;
    }


    // Геттери і сеттери
    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public CollectionType getType() { return type; }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setType(CollectionType type) { this.type = type; }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes.addAll(dishes);
    }


    // Інші методи
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Collection that = (Collection) object;

        return id == that.id
                && Objects.equals(name, that.name)
                && type == that.type
                && Objects.equals(dishes, that.dishes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, dishes);
    }
}
