package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Enum.CollectionType;

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
public class Collection {
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
    public long getId() {
        return id;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void setType(CollectionType type) { this.type = type; }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes.addAll(dishes);
    }


    // Інші методи
    public void addDish(Dish dish) { dishes.add(dish); }

    public void deleteDish(Dish dish) {
        dishes.remove(dish);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Collection collection = (Collection) obj;

        if (id != collection.id) return false;
        if (!name.equals(collection.name)) return false;
        if (!type.equals(collection.type)) return false;
        return dishes.equals(collection.dishes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, dishes);
    }
}
