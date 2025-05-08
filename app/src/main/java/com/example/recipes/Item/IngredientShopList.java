package com.example.recipes.Item;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Interface.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
@Entity(
        tableName = "ingredient_shop_list",
        indices = {@Index("id")},
        foreignKeys = {@ForeignKey(
                    entity = Collection.class,
                    parentColumns = "id",
                    childColumns = "id_collection",
                    onDelete = ForeignKey.CASCADE)
        }
)
public class IngredientShopList implements Item {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @Ignore private Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
    @ColumnInfo(name = "id_collection") private long idCollection;
    @ColumnInfo(name = "is_buy") private boolean isBuy = false;


    // Конструктори
    @Ignore
    public IngredientShopList(String name, Map<IngredientType, ArrayList<String>> groupedAmountType, long idCollection) {
        this.name = name;
        this.groupedAmountType = groupedAmountType;
        this.idCollection = idCollection;
    }

    @Ignore
    public IngredientShopList(IngredientShopList ingredientShopList) {
        this.id = ingredientShopList.getId();
        this.name = ingredientShopList.getName();
        this.groupedAmountType = ingredientShopList.getGroupedAmountType();
        this.idCollection = ingredientShopList.getIdCollection();
        this.isBuy = ingredientShopList.getIsBuy();
    }

    @Ignore
    public IngredientShopList(String name, String amount, IngredientType type, long idCollection) {
        this.name = name;
        addAmountType(amount, type);
        this.idCollection = idCollection;
    }

    @Ignore
    public IngredientShopList(String name, String amount, IngredientType type, long idCollection, boolean isBuy) {
        this.name = name;
        addAmountType(amount, type);
        this.idCollection = idCollection;
    }

    @Ignore
    public IngredientShopList(String name, String amount, IngredientType type, boolean isBuy) {
        this.name = name;
        addAmountType(amount, type);
        this.isBuy = isBuy;
    }

    @Ignore
    public IngredientShopList(Ingredient ingredient) {
        if (ingredient != null) {
            this.name = ingredient.getName();
            addAmountType(ingredient.getAmount(), ingredient.getType());
        }
    }

    @Ignore
    public IngredientShopList(String name, long idCollection) {
        this.name = name;
        this.idCollection = idCollection;
    }

    @Ignore
    public IngredientShopList(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public IngredientShopList(String name, long idCollection, boolean isBuy) {
        this.name = name;
        this.idCollection = idCollection;
        this.isBuy = isBuy;
    }

    @Ignore
    public IngredientShopList() { }



    // Геттери і сеттери
    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<IngredientType, ArrayList<String>> getGroupedAmountType() { return groupedAmountType; }

    public long getIdCollection() {
        return idCollection;
    }

    public boolean getIsBuy() {
        return isBuy;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setGroupedAmountType(Map<IngredientType, ArrayList<String>> groupedAmountType) { this.groupedAmountType = groupedAmountType; }

    public void setIdCollection(long idCollection) {
        this.idCollection = idCollection;
    }

    public void setIsBuy(boolean isBuy) {
        this.isBuy = isBuy;
    }



    // Інші методи
    public void addAmountType(String amount, IngredientType type) {
        groupedAmountType.putIfAbsent(type, new ArrayList<>());
        groupedAmountType.get(type).add(amount);
    }

    public void addAmountType(IngredientShopListAmountType amountType) {
        groupedAmountType.putIfAbsent(amountType.getType(), new ArrayList<>());
        groupedAmountType.get(amountType.getType()).add(amountType.getAmount());
    }


    /**
     * Отримаємо текст суми кількості інгредієнтів за їх типом
     *
     * @return текст суми кількості інгредієнтів за їх типом
     */
    public String getGroupedAmountTypeToString(Context context) {
        List<String> amountType = groupedAmountType.entrySet().stream()
                .map(type -> {
                    StringBuilder amount = new StringBuilder();
                    float sumFloat = 0;
                    ArrayList<String> unParse = new ArrayList<>();

                    for (String amountString : type.getValue()) { // Отримуємо всі кількості інгредієнтів та перебираємо їх
                        try {
                            if (amountString.contains("/")) {
                                sumFloat += parseFraction(amountString); // Парсимо як дріб
                            } else {
                                sumFloat += Float.parseFloat(amountString); // Парсимо як число
                            }
                        }
                        catch (Exception e) { unParse.add(amountString); } // Якщо невдалося отримати число, додаємо у массив непарсованих
                    }

                    if (sumFloat > 0) {
                        if (sumFloat == Math.floor(sumFloat)) {
                            amount.append((int) sumFloat);
                        } else {
                            amount.append(String.format(Locale.US, "%.2f", sumFloat));
                        }
                    }

                    String stringType = IngredientTypeConverter.fromIngredientTypeBySettingLocale(type.getKey());

                    if (!unParse.isEmpty()) {
                        if (sumFloat > 0) { amount.append(" " + stringType + ", "); }

                        for (int i = 0; i < unParse.size(); i++) {
                            amount.append(unParse.get(i) + " " + stringType);

                            if (i < unParse.size() - 1) { // Відслідковує передостанній елемент, щоб уникнути коми в кінці
                                amount.append(", ");
                            }
                        }
                    } else {
                        amount.append(" " + stringType);
                    }

                    return amount.toString();
                })
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < amountType.size(); i++) {  // Збираємо фінальний текст кількості інгредієнта.
            result.append(amountType.get(i));

            if (i < amountType.size() - 1) {
                result.append(", ");
            }
        }

        return result.toString();
    }


    /**
     * Парсер текстових дробів (1/2), повертає число (0.5)
     *
     * @param fraction текстовий дріб
     * @return число з плаваючою точкою
     */
    private static float parseFraction(String fraction) {
        try {
            String[] parts = fraction.split("/");
            if (parts.length == 2) {
                float numerator = Float.parseFloat(parts[0]);
                float denominator = Float.parseFloat(parts[1]);
                return numerator / denominator;
            }
        } catch (Exception e) { }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        IngredientShopList that = (IngredientShopList) object;
        return id == that.id && idCollection == that.idCollection && isBuy == that.isBuy && Objects.equals(name, that.name) && Objects.equals(groupedAmountType, that.groupedAmountType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, groupedAmountType, idCollection, isBuy);
    }
}
