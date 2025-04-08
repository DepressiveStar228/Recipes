package com.example.recipes.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Утилітарний клас для роботи з типами колекцій та перетворення об'єктів.
 * Надає методи для роботи з generics та перевірки типів елементів у колекціях.
 */
public class ClassUtils {

    /**
     * Перевіряє, чи всі елементи списку належать до вказаного класу.
     *
     * @param <T> Тип елементів у списку
     * @param list Список для перевірки
     * @param clazz Клас для перевірки
     * @return true, якщо всі елементи є екземплярами вказаного класу, false - якщо ні
     */
    public static <T> boolean isListOfType(List<T> list, Class<?> clazz) {
        return list.stream().allMatch(clazz::isInstance);
    }

    /**
     * Перетворює список об'єктів на список певного типу.
     *
     * @param list  Вихідний перелік об'єктів.
     * @param clazz Клас, до якого необхідно привести елементи.
     * @return Новий список, який містить елементи типу T.
     */
    public static <T> ArrayList<T> getListOfType(List<?> list, Class<T> clazz) {
        ArrayList<T> newList = new ArrayList<>();
        for (Object item : list) {
            if (clazz.isInstance(item)) {
                newList.add(clazz.cast(item));
            } else {
                throw new IllegalArgumentException(
                        "Element " + item + " is not an instance of " + clazz.getSimpleName()
                );
            }
        }
        return newList;
    }
}
