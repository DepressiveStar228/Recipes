package com.example.recipes.Database.TypeConverter;

import android.util.Log;

import androidx.room.TypeConverter;

import com.example.recipes.Enum.CollectionType;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас-конвертер для перетворення об'єктів типу CollectionType в рядки та навпаки.
 * Використовується для зберігання значень переліку (enum) у базі даних Room.
 */
public class CollectionTypeConverter {
    /**
     * Конвертує об'єкт типу CollectionType в рядок.
     *
     * @param type Об'єкт типу CollectionType, який потрібно конвертувати.
     * @return Рядкове представлення об'єкта CollectionType, або null, якщо type дорівнює null.
     */
    @TypeConverter
    public static String fromCollectionType(CollectionType type) {
        return type == null ? null : type.name();
    }

    /**
     * Конвертує рядок в об'єкт типу CollectionType.
     *
     * @param value Рядок, який потрібно конвертувати в CollectionType.
     * @return Об'єкт типу CollectionType, або null, якщо value дорівнює null або не відповідає жодному значенню переліку.
     */
    @TypeConverter
    public static CollectionType toCollectionType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return CollectionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            Log.e("CollectionTypeConverter", "Unknown CollectionType: " + value);
            return null;
        }
    }
}
