package com.example.recipes.Item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Enum.IngredientType;

import java.util.Objects;

import io.reactivex.rxjava3.annotations.Nullable;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
@Entity(
        tableName = "ingredient_shop_list_amount_type",
        indices = @Index(value = "id_ingredient"),
        foreignKeys = {@ForeignKey(
                        entity = IngredientShopList.class,
                        parentColumns = "id",
                        childColumns = "id_ingredient",
                        onDelete = ForeignKey.CASCADE
                ), @ForeignKey(
                        entity = DishCollection.class,
                        parentColumns = "id",
                        childColumns = "id_dish",
                        onDelete = ForeignKey.CASCADE)
        }
)
public class IngredientShopListAmountType {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "amount") private String amount = "";
    @ColumnInfo(name = "type") private IngredientType type = IngredientType.VOID;
    @ColumnInfo(name = "id_ingredient") private Long idIngredient = 0L;
    @Nullable
    @ColumnInfo(name = "id_dish") private Long idDish;

    // Конструктори
    public IngredientShopListAmountType() { }

    public IngredientShopListAmountType(String amount, IngredientType type, Long idIngredient, Long idDish) {
        this.amount = amount;
        this.type = type;
        this.idIngredient = idIngredient;
        if (idDish != null) {
            if (idDish > 0) {
                this.idDish = idDish;
            }
        }
    }

    // Геттери і сеттери
    public long getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public IngredientType getType() {
        return type;
    }

    public Long getIdIngredient() {
        return idIngredient;
    }

    @Nullable public Long getIdDish() {
        return idDish;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setIdIngredient(Long idIngredient) {
        this.idIngredient = idIngredient;
    }

    public void setIdDish(@Nullable Long idDish) {
        this.idDish = idDish;
    }

    public void setType(IngredientType type) {
        this.type = type;
    }


    // Інші методи
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        IngredientShopListAmountType that = (IngredientShopListAmountType) object;
        return Objects.equals(id, that.id) && Objects.equals(amount, that.amount) && Objects.equals(type, that.type) && Objects.equals(idIngredient, that.idIngredient) && Objects.equals(idDish, that.idDish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, type, idIngredient, idDish);
    }
}
