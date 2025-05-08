package com.example.recipes.Database.Repositories;

import android.content.Context;
import android.util.Log;

import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.DAO.IngredientShopListAmountTypeDAO;
import com.example.recipes.Database.ViewModels.IngredientShopListAmountTypeViewModel;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.IngredientShopListAmountType;
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
 * Клас для роботи з типами кількостей інгредієнтів у списках покупок.
 * Цей клас реалізує інтерфейс Utils<IngredientShopListAmountType>, що дозволяє виконувати CRUD операції над типами кількостей інгредієнтів у списках покупок.
 */
public class IngredientShopListAmountTypeRepository implements Utils<IngredientShopListAmountType> {
    private final Context context;
    private final IngredientShopListAmountTypeDAO dao;
    private final IngredientShopListAmountTypeViewModel viewModel;

    /**
     * Конструктор класу IngredientShopListAmountTypeRepository.
     * Ініціалізує DAO та ViewModel для роботи з типами кількостей інгредієнтів.
     */
    public IngredientShopListAmountTypeRepository(Context context, IngredientShopListAmountTypeDAO dao) {
        this.context = context;
        this.dao = dao;
        this.viewModel = new IngredientShopListAmountTypeViewModel(dao);
    }

    /**
     * Повертає DAO для роботи з типами кількостей інгредієнтів.
     * @return Об'єкт IngredientShopListAmountTypeDAO
     */
    public IngredientShopListAmountTypeDAO getDao() {
        return dao;
    }

    /**
     * Повертає ViewModel для роботи з типами кількостей інгредієнтів.
     * @return Об'єкт IngredientShopListAmountTypeViewModel
     */
    public IngredientShopListAmountTypeViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Додає новий тип кількості інгредієнта.
     * @param ingredient Об'єкт IngredientShopListAmountTypeRepository для додавання
     * @return Single<Long> ID створеного запису
     */
    @Override
    public Single<Long> add(IngredientShopListAmountType ingredient) {
        return dao.insert(ingredient);
    }

    /**
     * Додає список типів кількостей інгредієнтів.
     * @param items Список об'єктів IngredientShopListAmountTypeRepository для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> addAll(ArrayList<IngredientShopListAmountType> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(item -> dao.insert(item)
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
     * Отримує всі типи кількостей інгредієнтів.
     * @return Single<List<IngredientShopListAmountTypeRepository>> Список усіх типів кількостей
     */
    @Override
    public Single<List<IngredientShopListAmountType>> getAll() {
        return dao.getAll();
    }

    /**
     * Отримує тип кількості за ID.
     * @param id ID запису
     * @return Single<IngredientShopListAmountTypeRepository> Об'єкт типу кількості
     */
    @Override
    public Single<IngredientShopListAmountType> getByID(long id) {
        return dao.getByID(id).switchIfEmpty(Single.just(new IngredientShopListAmountType()));
    }

    /**
     * Отримує типи кількостей для конкретного інгредієнта.
     * @param idIngredient ID інгредієнта
     * @return Single<List<IngredientShopListAmountTypeRepository>> Список типів кількостей
     */
    public Single<List<IngredientShopListAmountType>> getByIDIngredient(long idIngredient) {
        return dao.getByIDIngredient(idIngredient).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Оновлює інформацію про тип кількості інгредієнта.
     * @param ingredient Об'єкт типу кількості для оновлення
     * @return Completable Результат операції
     */
    @Override
    public Completable update(IngredientShopListAmountType ingredient) {
        return dao.update(ingredient);
    }

    /**
     * Видаляє тип кількості інгредієнта.
     * @param ingredient Об'єкт типу кількості для видалення
     * @return Completable Результат операції
     */
    @Override
    public Completable delete(IngredientShopListAmountType ingredient) {
        return dao.delete(ingredient);
    }

    /**
     * Видаляє список типів кількостей інгредієнтів.
     * @param items Список об'єктів для видалення
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> deleteAll(ArrayList<IngredientShopListAmountType> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(item ->
                        dao.delete(item)
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
