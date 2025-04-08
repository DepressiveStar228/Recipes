package com.example.recipes.Database.TypeConverter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.R;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас-конвертер для перетворення об'єктів типу IngredientType в рядки та навпаки.
 * Використовується для зберігання значень переліку (enum) у базі даних Room.
 */
public class IngredientTypeConverter {
    /**
     * Конвертує об'єкт типу IngredientType в рядок.
     *
     * @param type Об'єкт типу IngredientType, який потрібно конвертувати.
     * @return Рядкове представлення об'єкта IngredientType, або null, якщо type дорівнює null.
     */
    @TypeConverter
    public static String fromIngredientType(IngredientType type) {
        return type == null ? null : type.name();
    }

    /**
     * Конвертує об'єкт типу IngredientType в рядок відповідний мові з налаштувань.
     *
     * @param type Об'єкт типу IngredientType, який потрібно конвертувати.
     * @return Рядкове представлення об'єкта IngredientType, або VOID, якщо type дорівнює null або не відповідний ніякому значенню.
     */
    public static String fromIngredientTypeBySettingLocale(IngredientType type) {
        PreferencesController controller = PreferencesController.getInstance();
        String[] typesString = controller.getStringArrayForLocale(R.array.ingredient_types, controller.getLanguageString());

        switch (type) {
            case GRAM -> {
                return typesString[0];
            }
            case KILOGRAM -> {
                return typesString[1];
            }
            case MILLILITER -> {
                return typesString[2];
            }
            case LITER -> {
                return typesString[3];
            }
            case PIECES -> {
                return typesString[4];
            }
            case TEASPOON -> {
                return typesString[5];
            }
            case TABLESPOON -> {
                return typesString[6];
            }
            case PINCH -> {
                return typesString[7];
            }
            case GLASS -> {
                return typesString[8];
            }
            case TO_TASTE -> {
                return typesString[9];
            }
            default -> {
                return typesString[10];
            }
        }
    }

    /**
     * Конвертує рядок в об'єкт типу IngredientType.
     *
     * @param value Рядок, який потрібно конвертувати в IngredientType.
     * @return Об'єкт типу IngredientType, або null, якщо value дорівнює null або не відповідає жодному значенню переліку.
     */
    @TypeConverter
    public static IngredientType toIngredientType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return IngredientType.valueOf(value);
        } catch (IllegalArgumentException e) {
            if (value.isEmpty()) return IngredientType.VOID;

            PreferencesController controller = PreferencesController.getInstance();
            String[] langs = controller.getStringArrayForLocale(R.array.language_values, "en");

            for (String lang : langs) {
                String[] typesString = controller.getStringArrayForLocale(R.array.ingredient_types, lang);

                if (typesString[0].equals(value)) { return IngredientType.GRAM; }
                else if (typesString[1].equals(value)) { return IngredientType.KILOGRAM; }
                else if (typesString[2].equals(value)) { return IngredientType.MILLILITER; }
                else if (typesString[3].equals(value)) { return IngredientType.LITER; }
                else if (typesString[4].equals(value)) { return IngredientType.PIECES; }
                else if (typesString[5].equals(value)) { return IngredientType.TEASPOON; }
                else if (typesString[6].equals(value)) { return IngredientType.TABLESPOON; }
                else if (typesString[7].equals(value)) { return IngredientType.PINCH; }
                else if (typesString[8].equals(value)) { return IngredientType.GLASS; }
                else if (typesString[9].equals(value)) { return IngredientType.TO_TASTE; }
            }

            Log.e("IngredientTypeConverter", "Unknown IngredientType: " + value);
            return IngredientType.VOID;
        }
    }
}
