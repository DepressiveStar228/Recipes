package com.example.recipes.Item;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Interface.SelectableItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.annotations.NonNull;

@Entity(
        tableName = "ingredient_shop_list",
        indices = {@Index("id")},
        foreignKeys = {
            @ForeignKey(
                    entity = Collection.class,
                    parentColumns = "id",
                    childColumns = "id_collection",
                    onDelete = ForeignKey.CASCADE
            )
        }
)
public class IngredientShopList implements SelectableItem {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name;
    @Ignore private Map<String, ArrayList<String>> groupedAmountType = new HashMap<>();
    @ColumnInfo(name = "id_collection") private long id_collection;
    @ColumnInfo(name = "is_buy") private boolean isBuy = false;

    @Ignore
    public IngredientShopList(@NonNull String name, @NonNull Map<String, ArrayList<String>> groupedAmountType, @NonNull long id_collection){
        this.name = name;
        this.groupedAmountType = groupedAmountType;
        this.id_collection = id_collection;
    }

    @Ignore
    public IngredientShopList(@NonNull IngredientShopList ingredientShopList){
        this.id = ingredientShopList.getId();
        this.name = ingredientShopList.getName();
        this.groupedAmountType = ingredientShopList.getGroupedAmountType();
        this.id_collection = ingredientShopList.getId_collection();
        this.isBuy = ingredientShopList.getIsBuy();
    }

    @Ignore
    public IngredientShopList(@NonNull String name, @NonNull String amount, @NonNull String type, @NonNull long id_collection){
        this.name = name;
        addAmountType(amount, type);
        this.id_collection = id_collection;
    }

    @Ignore
    public IngredientShopList(@NonNull String name, @NonNull String amount, @NonNull String type, @NonNull boolean isBuy){
        this.name = name;
        addAmountType(amount, type);
        this.isBuy = isBuy;
    }

    @Ignore
    public IngredientShopList(Ingredient ingredient){
        if (ingredient != null) {
            this.name = ingredient.getName();
            addAmountType(ingredient.getAmount(), ingredient.getType());
        }
    }

    @Ignore
    public IngredientShopList(@NonNull String name, @NonNull long id_collection) {
        this.name = name;
        this.id_collection = id_collection;
    }

    public IngredientShopList(@NonNull String name, @NonNull long id_collection, @NonNull boolean isBuy) {
        this.name = name;
        this.id_collection = id_collection;
        this.isBuy = isBuy;
    }

    @Ignore
    public IngredientShopList(){}


    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<String, ArrayList<String>> getGroupedAmountType() { return groupedAmountType; }

    public String getGroupedAmountTypeToString() {
        List<String> amountType = groupedAmountType.entrySet().stream()
                .map(type -> {
                    StringBuilder amount = new StringBuilder();
                    float sumFloat = 0;
                    ArrayList<String> unParse = new ArrayList<>();

                    for (String amountString : type.getValue()){
                        try {
                            if (amountString.contains("/")) {
                                sumFloat += parseFraction(amountString);
                            } else {
                                sumFloat += Float.parseFloat(amountString);
                            }
                        }
                        catch (Exception e) { unParse.add(amountString); }
                    }

                    if (sumFloat > 0) {
                        if (sumFloat == Math.floor(sumFloat)) {
                            amount.append((int) sumFloat);
                        } else {
                            amount.append(String.format(Locale.US, "%.2f", sumFloat));
                        }
                    }

                    if (!unParse.isEmpty()) {
                        if (sumFloat > 0) { amount.append(" " + type.getKey() + ", "); }

                        for (int i = 0; i < unParse.size(); i++) {
                            amount.append(unParse.get(i) + " " + type.getKey());

                            if (i < unParse.size() - 1) {
                                amount.append(", ");
                            }
                        }
                    } else {
                        amount.append(" " + type.getKey());
                    }

                    return amount.toString();
                })
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < amountType.size(); i++) {
            result.append(amountType.get(i));

            if (i < amountType.size() - 1) {
                result.append(", ");
            }
        }

        return result.toString();
    }

    private static float parseFraction(String fraction) {
        try {
            String[] parts = fraction.split("/");
            if (parts.length == 2) {
                float numerator = Float.parseFloat(parts[0]);
                float denominator = Float.parseFloat(parts[1]);
                return numerator / denominator;
            }
        } catch (Exception e) {}
        return 0;
    }

    public long getId_collection() {
        return id_collection;
    }

    public boolean getIsBuy() {
        return isBuy;
    }




    public void setId(@NonNull long id) {
        this.id = id;
    }

    @Override
    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setGroupedAmountType(@NonNull Map<String, ArrayList<String>> groupedAmountType) { this.groupedAmountType = groupedAmountType; }

    public void setId_collection(@NonNull long id_collection) {
        this.id_collection = id_collection;
    }

    public void setIsBuy(@NonNull boolean isBuy) {
        this.isBuy = isBuy;
    }

    public void addAmountType(@NonNull String amount, @NonNull String type) {
        groupedAmountType.putIfAbsent(type, new ArrayList<>());
        groupedAmountType.get(type).add(amount);
    }

    public void addAmountType(@NonNull IngredientShopList_AmountType amountType) {
        groupedAmountType.putIfAbsent(amountType.getType(), new ArrayList<>());
        groupedAmountType.get(amountType.getType()).add(amountType.getAmount());
    }

    public boolean equals(@Nullable IngredientShopList item) {
        if (this.id != item.getId()) { return false; }
        if (!this.name.equals(item.getName())) { return false; }
        if (this.id_collection != item.getId_collection()) { return false; }
        if (this.isBuy != item.getIsBuy()) { return false; }
        if (!Objects.equals(this.groupedAmountType, item.getGroupedAmountType())) { return false; }
        return true;
    }
}
