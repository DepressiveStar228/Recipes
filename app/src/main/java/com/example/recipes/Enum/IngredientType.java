package com.example.recipes.Enum;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Перелік типів одиниць виміру для інгредієнтів у рецептах.
 * Визначає всі можливі способи вимірювання кількості інгредієнтів.
 */
public enum IngredientType {
    GRAM(0),
    KILOGRAM(1),
    MILLILITER(2),
    LITER(3),
    PIECES(4),
    PINCH(5),
    GLASS(6),
    TEASPOON(7),
    TABLESPOON(8),
    TO_TASTE(9),
    VOID(10);

    private final int position;

    /**
     * Конструктор переліку.
     * @param position Позиція у випадаючому списку
     */
    IngredientType(int position) {
        this.position = position;
    }

    /**
     * Отримує позицію типа інгредієнтів.
     * @return Позицію типа інгредієнтів.
     */
    public int getPosition() {
        return position;
    }
}
