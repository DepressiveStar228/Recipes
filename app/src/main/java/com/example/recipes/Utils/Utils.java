package com.example.recipes.Utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Узагальнений інтерфейс для роботи з базою даних.
 * Визначає стандартний набір CRUD операцій для роботи з сутностями.
 *
 * @param <T> Тип сутності, з якою працює інтерфейс
 */
public interface Utils<T> {
    /**
     * Додає новий елемент до бази даних.
     *
     * @param item Об'єкт для додавання
     * @return Single<Long> ID створеного елементу
     */
    Single<Long> add(T item);

    /**
     * Додає список елементів до бази даних.
     *
     * @param items Список об'єктів для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    Single<Boolean> addAll(ArrayList<T> items);

    /**
     * Отримує всі елементи з бази даних.
     *
     * @return Single<List<T>> Список усіх елементів
     */
    Single<List<T>> getAll();

    /**
     * Отримує елемент за його ID.
     *
     * @param id ID елементу
     * @return Single<T> Знайдений об'єкт
     */
    Single<T> getByID(long id);

    /**
     * Оновлює інформацію про елемент.
     *
     * @param item Об'єкт для оновлення
     * @return Completable Результат операції
     */
    Completable update(T item);

    /**
     * Видаляє елемент з бази даних.
     *
     * @param item Об'єкт для видалення
     * @return Completable Результат операції
     */
    Completable delete(T item);

    /**
     * Видаляє список елементів з бази даних.
     *
     * @param items Список об'єктів для видалення
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    Single<Boolean> deleteAll(ArrayList<T> items);
}
