package com.example.recipes.Database.Repositories;

import android.content.Context;
import android.util.Log;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.ViewModels.IngredientViewModel;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;


/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для роботи з інгредієнтами в базі даних.
 * Реалізує інтерфейс Utils<Ingredient> та надає методи для CRUD операцій з інгредієнтами.
 */
public class IngredientRepository implements Utils<Ingredient> {
    private final Context context;
    private final IngredientDAO dao;
    private final IngredientViewModel viewModel;

    /**
     * Конструктор класу IngredientRepository.
     * Ініціалізує DAO та ViewModel для роботи з інгредієнтами.
     */
    public IngredientRepository(Context context, IngredientDAO dao) {
        this.context = context;
        this.dao = dao;
        this.viewModel = new IngredientViewModel(dao);
    }

    /**
     * Повертає DAO для роботи з інгредієнтами.
     * @return Об'єкт IngredientDAO
     */
    public IngredientDAO getDao() {
        return dao;
    }

    /**
     * Повертає ViewModel для роботи з інгредієнтами.
     * @return Об'єкт IngredientViewModel
     */
    public IngredientViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Додає інгредієнт до бази даних.
     * @param item Об'єкт інгредієнта для додавання
     * @return Single<Long> ID створеного інгредієнта
     */
    @Override
    public Single<Long> add(Ingredient item) {
        return dao.insert(item);
    }

    /**
     * Додає список інгредієнтів до бази даних.
     * @param items Список інгредієнтів для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> addAll(ArrayList<Ingredient> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(this::add)
                .map(id -> id > 0)
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) {
                            return false;
                        }
                    }
                    return true;
                });
    }

    /**
     * Додає список інгредієнтів для конкретної страви.
     * @param idDish ID страви
     * @param items Список інгредієнтів для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> addAll(Long idDish, ArrayList<Ingredient> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(ing -> dao.insert(new Ingredient(ing.getName().trim(), ing.getAmount(), ing.getType(), idDish))
                        .flatMap(id -> Single.just(id > 0)))
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) {
                            return false;
                        }
                    }
                    return true;
                });
    }

    /**
     * Отримує всі інгредієнти з бази даних.
     * @return Single<List<Ingredient>> Список усіх інгредієнтів
     */
    @Override
    public Single<List<Ingredient>> getAll() {
        return dao.getAll();
    }

    /**
     * Отримує інгредієнти для конкретної страви.
     * @param idDish ID страви
     * @return Single<List<Ingredient>> Список інгредієнтів
     */
    public Single<List<Ingredient>> getAllByIDDish(long idDish) {
        return dao.getAllByIDDish(idDish).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Отримує інгредієнт за ID.
     * @param id ID інгредієнта
     * @return Single<Ingredient> Об'єкт інгредієнта
     */
    @Override
    public Single<Ingredient> getByID(long id) {
        return dao.getByID(id).switchIfEmpty(Single.just(new Ingredient("", "", IngredientType.VOID)));
    }

    /**
     * Отримує список унікальних назв інгредієнтів.
     * @return Single<List<String>> Список унікальних назв інгредієнтів
     */
    public Single<List<String>> getNamesUnique() {
        return dao.getNamesUnique();
    }

    /**
     * Оновлює інформацію про інгредієнт.
     * @param item Об'єкт інгредієнта для оновлення
     * @return Completable Результат операції
     */
    @Override
    public Completable update(Ingredient item) {
        return dao.update(item);
    }

    /**
     * Видаляє інгредієнт.
     * @param item Об'єкт інгредієнта для видалення
     * @return Completable Результат операції
     */
    @Override
    public Completable delete(Ingredient item) {
        return dao.delete(item);
    }

    /**
     * Видаляє список інгредієнтів.
     * @param items Список інгредієнтів для видалення
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> deleteAll(ArrayList<Ingredient> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(ingredient ->
                        dao.delete(ingredient)
                                .andThen(Single.just(true))
                                .onErrorReturnItem(false)
                )
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) {
                            Log.d("RecipeUtils", "Помилка. Щось не видалено");
                            return false;
                        }
                    }

                    Log.d("RecipeUtils", "Всі інгредієнти видалено");
                    return true;
                })
                .onErrorReturnItem(false);
    }
}
