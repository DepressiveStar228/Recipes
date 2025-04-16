package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Interface.Item;

import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
@Entity(
        tableName = "ingredient",
        indices = @Index(value = "id_dish"),
        foreignKeys = {@ForeignKey(
                entity = Dish.class,
                parentColumns = "id",
                childColumns = "id_dish",
                onDelete = ForeignKey.CASCADE)
        }
)
public class Ingredient implements Item {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @ColumnInfo(name = "amount") private String amount;
    @ColumnInfo(name = "type") private IngredientType type;
    @ColumnInfo(name = "id_dish") private long idDish;


    // Конструктори
    @Ignore
    public Ingredient(long id, String name, String amount, IngredientType type, long idDish) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.idDish = idDish;
    }

    @Ignore
    public Ingredient(String name, String amount, IngredientType type, long idDish) {
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.idDish = idDish;
    }

    @Ignore
    public Ingredient() { }

    public Ingredient(String name, String amount, IngredientType type) {
        this.name = name;
        this.amount = amount;
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

    public String getAmount() {
        return amount;
    }

    public IngredientType getType() {
        return type;
    }

    public long getIdDish() {
        return idDish;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setType(IngredientType type) {
        this.type = type;
    }

    public void setIdDish(long idDish) {
        this.idDish = idDish;
    }


    // Інші методи

    /**
     * Отримаємо текстовий формат IngredientType згідно локалізації
     *
     * @return текст відповідно до IngredientType
     */
    public String getTypeString() {
        return IngredientTypeConverter.fromIngredientTypeBySettingLocale(type);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Ingredient that = (Ingredient) object;
        return id == that.id && idDish == that.idDish && Objects.equals(name, that.name) && Objects.equals(amount, that.amount) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, amount, type, idDish);
    }
}
