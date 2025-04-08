package com.example.recipes.Enum;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Перелік системних обмежень додатку.
 */
public enum Limits {
    MAX_CHAR_NAME_DISH(50),
    MAX_CHAR_RECIPE_DISH(4000),
    MAX_CHAR_PORTION_DISH(5),
    MAX_CHAR_NAME_INGREDIENT(50),
    MAX_CHAR_AMOUNT_INGREDIENT(15),
    MAX_CHAR_NAME_COLLECTION(20),
    MAX_CHAR_GPT_PROMPT(300),
    MAX_COUNT_INGREDIENT(50),
    MAX_COUNT_RECIPE_ITEM(10),
    MAX_COUNT_SHOP_LIST(20);

    private final int limit;

    /**
     * Конструктор для встановлення обмеження
     * @param limit числове значення обмеження
     */
    Limits(int limit) {
        this.limit = limit;
    }

    /**
     * Отримання значення обмеження
     * @return числове значення обмеження
     */
    public int getLimit() {
        return limit;
    }
}
