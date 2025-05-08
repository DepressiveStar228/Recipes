package com.example.recipes.Database.Repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.ViewModels.DishCollectionViewModel;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishCollection;
import com.example.recipes.R;
import com.example.recipes.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для роботи зі зв'язками між стравами та колекціями.
 * Цей клас реалізує інтерфейс Utils<DishCollection>, що дозволяє виконувати CRUD операції над зв'язками між стравами та колекціями..
 */
public class DishCollectionRepository implements Utils<DishCollection> {
    private final Context context;
    private final DishCollectionDAO dao;
    private final DishCollectionViewModel viewModel;
    private CollectionRepository collectionRepository;

    /**
     * Конструктор класу DishCollectionRepository.
     * Ініціалізує DAO та ViewModel для роботи зі зв'язками страв і колекцій.
     */
    public DishCollectionRepository(Context context, DishCollectionDAO dao) {
        this.context = context;
        this.dao = dao;
        this.viewModel = new DishCollectionViewModel(dao);
    }

    /**
     * Встановлює залежність від CollectionRepository.
     * @param collectionRepository Об'єкт CollectionRepository
     */
    public void setDependencies(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    /**
     * Повертає DAO для роботи зі зв'язками страв і колекцій.
     * @return Об'єкт DishCollectionDAO
     */
    public DishCollectionDAO getDao() {
        return dao;
    }

    /**
     * Повертає ViewModel для роботи зі зв'язками страв і колекцій.
     * @return Об'єкт DishCollectionViewModel
     */
    public DishCollectionViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Додає новий зв'язок між стравою та колекцією.
     * @param item Об'єкт DishCollection для додавання
     * @return Single<Long> ID створеного зв'язку
     */
    @Override
    public Single<Long> add(DishCollection item) {
        return dao.insert(item);
    }

    /**
     * Додає зв'язок між стравою та колекцією з перевіркою на наявність.
     * @param item Об'єкт DishCollection для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> addWithCheckExist(DishCollection item) {
        return isExist(item)
                .flatMap(isInCollection -> {
                    if (!isInCollection) {
                        return add(item).map(id -> (id > 0));
                    } else {
                        return collectionRepository.getByID(item.getIdCollection())
                                .map(collection -> collectionRepository.getCustomNameSystemCollectionByName(collection.getName()))
                                .flatMap(name -> {
                                    if (name != null && !name.contains("Unknown Collection")) {
                                        Log.d("RecipeUtils", "Страва вже є у колекції");
                                        return Single.just(true);
                                    } else {
                                        return Single.just(false);
                                    }
                                });
                    }
                });
    }

    /**
     * Додає список зв'язків між стравами та колекціями.
     * @param items Список об'єктів DishCollection для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> addAll(ArrayList<DishCollection> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(this::addWithCheckExist)
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
     * Додає страву до кількох колекцій.
     * @param dish Об'єкт страви
     * @param collections Список колекцій
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> addAll(Dish dish, ArrayList<Collection> collections) {
        return Observable.fromIterable(collections)
                .flatMapSingle(collection -> addWithCheckExist(new DishCollection(dish.getId(), collection.getId())))
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
     * Додає список страв до однієї колекції.
     * @param dishes Список страв
     * @param collection Колекція
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> addAll(ArrayList<Dish> dishes, Collection collection) {
        return Observable.fromIterable(dishes)
                .flatMapSingle(dish -> add(new DishCollection(dish.getId(), collection.getId())).map(id -> id > 0))
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
     * Отримує всі зв'язки страв з колекціями з бази даних.
     * @return Single<List<Collection>> Список усіх зв'язків
     */
    @Override
    public Single<List<DishCollection>> getAll() {
        return dao.getAll();
    }

    /**
     * Отримує зв'язок за ID.
     * @param id ID зв'язку
     * @return Single<DishCollection> Об'єкт зв'язку
     */
    @Override
    public Single<DishCollection> getByID(long id) {
        return dao.getByID(id).switchIfEmpty(Single.just(new DishCollection(0, 0)));
    }

    /**
     * Отримує зв'язок за ID страви.
     * @param idDish ID страви
     * @return Single<DishCollection> Об'єкт зв'язку
     */
    public Single<List<DishCollection>> getByIDDish(long idDish) {
        return dao.getByIDDish(idDish).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Отримує всі ID колекцій для вказаної страви.
     * @param idDish ID страви
     * @return Single<List<Long>> Список ID колекцій
     */
    public Single<List<Long>> getAllIDsCollectionByIDDish(long idDish) {
        return dao.getAllIDsCollectionByIDDish(idDish).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Отримує всі ID страв для вказаної колекції.
     * @param idCollection ID колекції
     * @return Single<List<Long>> Список ID страв
     */
    public Single<List<Long>> getAllIDsDishByIDCollection(long idCollection) {
        return dao.getAllIDsDishByIDCollection(idCollection).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Отримує зв'язок за ID страви та ID колекції.
     * @param idDish ID страви
     * @param idCollection ID колекції
     * @return Single<DishCollection> Об'єкт зв'язку
     */
    public Single<DishCollection> getByData(long idDish, long idCollection) {
        return dao.getByIDDishAndIDCollection(idDish, idCollection).switchIfEmpty(Single.just(new DishCollection(0, 0)));
    }

    /**
     * Перевіряє, чи існує зв'язок між стравою та колекцією.
     * @param dishCollection Об'єкт зв'язку для перевірки
     * @return Single<Boolean> Результат перевірки (true - існує, false - не існує)
     */
    public Single<Boolean> isExist(DishCollection dishCollection) {
        return dao.getByIDDishAndIDCollection(dishCollection.getIdDish(), dishCollection.getIdCollection())
                .map(dishCollection2 -> {
                    Log.d("isDishInCollection", "Страва з айді " + dishCollection2.getIdDish() + " знайдена в колекції");
                    return true;
                })
                .defaultIfEmpty(false)
                .doOnSuccess(result -> Log.d("isDishInCollection", "Result: " + result))
                .doOnError(throwable -> Log.e("isDishInCollection", "Error: " + throwable.getMessage()));
    }

    /**
     * Копіює страви з однієї колекції до інших.
     * @param collectionOrigin вихідна колекція
     * @param collections Список цільових колекцій
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> copyDishesToAnotherCollections(Collection collectionOrigin, ArrayList<Collection> collections) {
        return collectionRepository.getDishes(collectionOrigin.getId())
                .flatMap(dishes -> {
                    if (dishes.isEmpty()) {
                        return Single.just(false);
                    }
                    return Observable.fromIterable(collections)
                            .flatMapSingle(collection ->
                                    Observable.fromIterable(dishes)
                                            .flatMapSingle(dish ->
                                                    isExist(new DishCollection(dish.getId(), collection.getId()))
                                                            .flatMap(isDuplicate -> {
                                                                if (!isDuplicate) {
                                                                    return add(new DishCollection(dish.getId(), collection.getId())).map(id -> id > 0);
                                                                } else {
                                                                    return Single.just(true);
                                                                }
                                                            })
                                            )
                                            .toList()
                            )
                            .toList()
                            .map(results -> {
                                for (List<Boolean> resultList : results) {
                                    for (Boolean result : resultList) {
                                        if (!result) {
                                            return false;
                                        }
                                    }
                                }
                                return true;
                            });
                });
    }

    /**
     * Оновлює інформацію про зв'язок між стравою та колекцією.
     * @param item Зв'язок між стравою та колекцією для оновлення
     * @return Completable Результат операції
     */
    @Override
    public Completable update(DishCollection item) {
        return null;
    }

    /**
     * Видаляє зв'язок між стравою та колекцією.
     * @param item Об'єкт зв'язку для видалення
     * @return Completable Результат операції
     */
    @Override
    public Completable delete(DishCollection item) {
        return dao.delete(item);
    }

    /**
     * Видаляє список зв'язків.
     * @param items Список об'єктів зв'язків для видалення
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> deleteAll(ArrayList<DishCollection> items) {
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

                    Log.d("RecipeUtils", "Всі страва_колекція видалено");
                    return true;
                })
                .onErrorReturnItem(false);

    }

    /**
     * Видаляє всі зв'язки для вказаної колекції.
     * @param idCollection ID колекції
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> deleteAllByIDCollection(long idCollection) {
        return dao.getAllIDsDishByIDCollection(idCollection)
                .flatMapObservable(items -> Observable.fromIterable(items)
                        .flatMapSingle(item -> dao.getByIDDishAndIDCollection(item, idCollection)
                                .toSingle()
                                .flatMap(dish_collection -> dao.delete(dish_collection)
                                        .andThen(Single.just(true))
                                        .onErrorReturnItem(false)
                                )
                        )
                )
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) {
                            Log.d("RecipeUtils", "Помилка. Щось не видалено");
                            return false;
                        }
                    }

                    Log.d("RecipeUtils", "Всі страва_колекція видалено");
                    return true;
                })
                .onErrorReturnItem(false);
    }
}
