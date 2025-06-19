package com.example.recipes.Adapter.Interfaces;

import java.util.ArrayList;

public interface ChooseItem<T> {
    /**
     * Встановлює список елементів.
     *
     * @param items Список  елементів.
     */
    void setItems(ArrayList<T> items);

    /**
     * Встановлює список вибраних елементів.
     *
     * @param selectedItems Список вибраних елементів.
     */
    void setSelectedItems(ArrayList<T> selectedItems);

    /**
     * Повертає список вибраних елементів.
     *
     * @return Список вибраних елементів.
     */
    ArrayList<T> getSelectedItem();

    /**
     * Скидає вибір усіх елементів.
     */
    void resetSelectionItems();
}
