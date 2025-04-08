package com.example.recipes.Enum;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Системний перелік ідентифікаторів колекцій.
 * Містить фіксовані ID для основних системних колекцій додатку.
 */
public enum ID_System_Collection {
    ID_FAVORITE(1L),
    ID_MY_RECIPE(2L),
    ID_GPT_RECIPE(3L),
    ID_IMPORT_RECIPE(4L),
    ID_BLACK_LIST(5L);

    private final Long id;

    /**
     * Конструктор переліку.
     * @param id Унікальний ідентифікатор колекції
     */
    ID_System_Collection(Long id) {
        this.id = id;
    }

    /**
     * Отримує ідентифікатор колекції.
     * @return Числовий ID колекції
     */
    public Long getId() {
        return id;
    }
}
