package com.example.recipes.Interface;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Callback-інтерфейс для обробки події натискання кнопки "Назад".
 * Дозволяє кастомізувати поведінку додатка при натисканні системної кнопки "Назад".
 */
public interface OnBackPressedListener {
    /**
     * Викликається при натисканні кнопки "Назад".
     *
     * @return Повертає boolean значення, яке визначає подальшу поведінку:
     *         true - подія вважається обробленою (стандартна поведінка не виконується),
     *         false - подія передається далі по ланцюжку (стандартна поведінка).
     */
    boolean onBackPressed();
}
