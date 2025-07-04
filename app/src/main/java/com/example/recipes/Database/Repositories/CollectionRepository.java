package com.example.recipes.Database.Repositories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;

import androidx.core.content.ContextCompat;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.ViewModels.CollectionViewModel;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.IDSystemCollection;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.ShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.ClassUtils;
import com.example.recipes.Utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для роботи зі колекціями в базі даних.
 * Цей клас реалізує інтерфейс Utils<Collection>, що дозволяє виконувати CRUD операції над колекціями.
 */
public class CollectionRepository implements Utils<Collection> {
    private final Context context;
    private final CollectionDAO dao;
    private final CollectionViewModel viewModel;
    private DishRepository dishRepository;
    private DishCollectionRepository dishCollectionRepository;
    private IngredientShopListRepository ingredientShopListRepository;

    /**
     * Конструктор класу CollectionRepository.
     * Ініціалізує DAO та ViewModel для роботи з колекціями.
     */
    public CollectionRepository(Context context, CollectionDAO dao) {
        this.context = context;
        this.dao = dao;
        this.viewModel = new CollectionViewModel(dao);
    }

    /**
     * Встановлює залежності для роботи з іншими репозиторіями.
     * @param dishRepository Репозиторій для роботи зі стравами
     * @param dishCollectionRepository Репозиторій для роботи зі зв'язками між стравами та колекціями
     * @param ingredientShopListRepository Репозиторій для роботи зі списками покупок
     */
    public void setDependencies(DishRepository dishRepository,
                              DishCollectionRepository dishCollectionRepository,
                              IngredientShopListRepository ingredientShopListRepository) {
        this.dishRepository = dishRepository;
        this.dishCollectionRepository = dishCollectionRepository;
        this.ingredientShopListRepository = ingredientShopListRepository;
    }

    /**
     * Повертає DAO для роботи з колекціями.
     * @return Об'єкт CollectionDAO
     */
    public CollectionDAO getDao() {
        return dao;
    }

    /**
     * Повертає ViewModel для роботи з колекціями.
     * @return Об'єкт CollectionViewModel
     */
    public CollectionViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Додає колекцію до бази даних.
     * @param item Об'єкт колекції для додавання
     * @return Single<Long> ID створеної колекції
     */
    @Override
    public Single<Long> add(Collection item) {
        return dao.insert(item);
    }

    /**
     * Додає список колекцій до бази даних.
     * @param items Список колекцій для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> addAll(ArrayList<Collection> items) {
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
     * Отримує всі колекції з бази даних.
     * @return Single<List<Collection>> Список усіх колекцій
     */
    @Override
    public Single<List<Collection>> getAll() {
        return getAllWithoutAdditionData()
                .flatMap(collections -> Observable.fromIterable(collections)
                        .flatMapSingle(this::getDataForCollection)
                        .toList());
    }

    /**
     * Отримує всі колекції без додаткових даних (страв).
     * @return Single<List<Collection>> Список усіх колекцій без страв
     */
    public Single<List<Collection>> getAllWithoutAdditionData() {
        return dao.getAll();
    }

    /**
     * Отримує колекції за типом.
     * @param <T> Тип колекції (Collection або ShopList)
     * @param type Тип колекції
     * @return Single<List<T>> Список колекцій вказаного типу
     */
    public <T> Single<List<T>> getAllByType(CollectionType type) {
        return dao.getAllByType(type)
                .flatMap(collections -> Observable.fromIterable(collections)
                        .flatMapSingle(collection -> {
                            if (type.equals(CollectionType.COLLECTION)) return getDataForCollection(collection).map(data -> (T) data);
                            else if (type.equals(CollectionType.SHOP_LIST)) return getDataForShopList(new ShopList(collection)).map(data -> (T) data);
                            else return Single.just((T) collection);
                        })
                        .toList());
    }

    /**
     * Отримує колекцію за ID.
     * @param id ID колекції
     * @return Single<Collection> Об'єкт колекції
     */
    @Override
    public Single<Collection> getByID(long id) {
        return dao.getByID(id)
                .switchIfEmpty(Single.just(new Collection(-1, "Unknown Collection",  CollectionType.COLLECTION, new ArrayList<>())))
                .flatMap(this::getDataForCollection);
    }

    /**
     * Отримує колекцію за назвою.
     * @param name Назва колекції
     * @return Single<Collection> Об'єкт колекції
     */
    public Single<Collection> getByName(String name) {
        return dao.getByName(name)
                .switchIfEmpty(Single.just(new Collection(-1, "Unknown Collection",  CollectionType.COLLECTION, new ArrayList<>())))
                .flatMap(this::getDataForCollection)
                .doOnError(throwable -> Log.e("RxError", "Error occurred: ", throwable))
                .onErrorResumeNext(throwable -> {
                    Log.e("RxError", "Возврат пустой коллекции из-за ошибки", throwable);
                    return Single.just(new Collection(-1, "Unknown Collection", CollectionType.COLLECTION, new ArrayList<>()));
                });
    }

    /**
     * Отримує ID колекції за назвою.
     * @param name Назва колекції
     * @return Single<Long> ID колекції (-1 якщо не знайдено)
     */
    public Single<Long> getIDByName(String name) {
        return dao.getIDByName(name)
                .toSingle()
                .onErrorResumeNext(throwable -> Single.just((long) -1));
    }

    /**
     * Отримує ID колекції за назвою та типом.
     * @param name Назва колекції
     * @param type Тип колекції
     * @return Single<Long> ID колекції (-1 якщо не знайдено)
     */
    public Single<Long> getIDByNameAndType(String name, CollectionType type) {
        return dao.getIDByNameAndType(name, type)
                .toSingle()
                .onErrorResumeNext(throwable -> Single.just((long) -1));
    }

    /**
     * Отримує страви, які не входять до колекції.
     * @param collection Колекція для перевірки
     * @return Single<ArrayList<Dish>> Список страв, які не входять до колекції
     */
    public Single<ArrayList<Dish>> getUnusedDish(Collection collection) {
        return Single.zip(
                dishRepository.getAll(),
                getDishes(collection.getId()),
                (allDishes, collectionDishes) -> {
                    ArrayList<Dish> unusedDished = new ArrayList<>();
                    for (Dish dish : allDishes) {
                        if (!collectionDishes.contains(dish)) {
                            unusedDished.add(dish);
                        }
                    }

                    return unusedDished;
                }
        );
    }

    /**
     * Отримує всі страви у колекції.
     * @param idCollection ID колекції
     * @return Single<List<Dish>> Список страв у колекції
     */
    public Single<List<Dish>> getDishes(long idCollection) {
        return dishCollectionRepository.getAllIDsDishByIDCollection(idCollection)
                .flatMap(ids -> {
                    if (ids == null || ids.isEmpty()) {
                        return Single.just(new ArrayList<>());
                    } else {
                        return Observable.fromIterable(ids)
                                .flatMapSingle(id -> dishRepository.getByID(id))
                                .toList();
                    }
                });
    }

    /**
     * Отримує колекції вказаного типу, які не містять вказану страву.
     * @param dish Страва для перевірки
     * @param collectionType Тип колекції
     * @return Single<ArrayList<Collection>> Список колекцій, які не містять страву
     */
    public Single<ArrayList<Collection>> getUnusedByTypeInDish(Dish dish, CollectionType collectionType) {
        return Single.zip(
                getAllByType(collectionType),
                dishRepository.getCollections(dish),
                (allCollection, dishesCollection) -> {
                    ArrayList<Collection> unusedCollection = new ArrayList<>();
                    for (Collection collection : ClassUtils.getListOfType(allCollection, Collection.class)) {
                        if (!dishesCollection.contains(collection)) {
                            unusedCollection.add(collection);
                        }
                    }

                    return unusedCollection;
                }
        );
    }

    /**
     * Генерує унікальну назву для списку покупок.
     * @param name Базова назва
     * @return Single<String> Унікальна назва
     */
    public Single<String> getUniqueCollectionName(String name) {
        return Single.fromCallable(() -> name)
                .flatMap(name_ -> getUniqueCollectionNameRecursive(name_, 1));
    }

    /**
     * Генерує унікальну назву для списку покупок.
     * @param name Базова назва
     * @return Single<String> Унікальна назва
     */
    private Single<String> getUniqueCollectionNameRecursive(String name, int suffix) {
        return checkDuplicateCollectionName(name)
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        String newDishName;

                        if (name.contains("№")) {
                            int indexNumb = name.indexOf("№");
                            String firstPart = name.substring(0, indexNumb + 1);
                            String secondPart = "";

                            try {
                                secondPart = name.substring(indexNumb + 2);
                            } catch (Exception ignored) { }

                            newDishName = firstPart + suffix + secondPart;
                        } else {
                            newDishName = name + " №" + suffix;
                        }
                        return getUniqueCollectionNameRecursive(newDishName, suffix + 1);
                    } else {
                        return Single.just(name);
                    }
                });
    }

    /**
     * Перевіряє, чи існує список покупок з такою ж назвою.
     * @param name Назва для перевірки
     * @return Single<Boolean> Результат перевірки
     */
    public Single<Boolean> checkDuplicateCollectionName(String name) {
        return dao.getIDByName(name)
                .map(Objects::nonNull)
                .switchIfEmpty(Single.just(false));
    }

    /**
     * Генерує унікальну назву для списку покупок на основі поточної дати.
     * @return Single<String> Унікальна назва
     */
    public Single<String> generateUniqueNameForShopList() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String baseName  = "(" + dateFormat.format(new Date()) + ")";
        return getUniqueCollectionName(baseName);
    }

    /**
     * Заповнює колекцію даними (стравами).
     * @param item Колекція для заповнення
     * @return Single<Collection> Колекція з заповненими даними
     */
    private Single<Collection> getDataForCollection(Collection item) {
        return getDishes(item.getId())
                .flatMap(dishes -> {
                    item.setDishes(new ArrayList<>(dishes));
                    return Single.just(item);
                });
    }

    /**
     * Заповнює список покупок даними (стравами та інгредієнтами).
     * @param item Список покупок для заповнення
     * @return Single<ShopList> Список покупок з заповненими даними
     */
    private Single<ShopList> getDataForShopList(ShopList item) {
        return getDishes(item.getId())
                .flatMap(dishes -> {
                    item.setDishes(new ArrayList<>(dishes));
                    return Single.just(item);
                })
                .flatMap(collection ->
                        ingredientShopListRepository.getAllByIDCollection(collection.getId())
                                .flatMap(ingredientShopLists -> {
                                    item.setIngredients(new ArrayList<>(ingredientShopLists));
                                    return Single.just(item);
                                })
                );
    }

    public Drawable getDrawableByName(String name) {
        if (Objects.equals(name, Collection.SYSTEM_COLLECTION_TAG + "1")) {
            return ContextCompat.getDrawable(context, R.drawable.icon_star);
        }
        else if (Objects.equals(name, Collection.SYSTEM_COLLECTION_TAG + "2")) {
            return ContextCompat.getDrawable(context, R.drawable.icon_book_a);
        }
        else if (Objects.equals(name, Collection.SYSTEM_COLLECTION_TAG + "3")) {
            return ContextCompat.getDrawable(context, R.drawable.icon_neurology);
        }
        else if (Objects.equals(name, Collection.SYSTEM_COLLECTION_TAG + "4")) {
            return ContextCompat.getDrawable(context, R.drawable.icon_download);
        }
        else return ContextCompat.getDrawable(context, R.drawable.icon_book);
    }

    /**
     * Отримує список назв системних колекцій.
     *
     * @return ArrayList<String> Список назв системних колекцій
     */
    public ArrayList<String> getAllNameSystemCollection() {
        String systemTag = context.getString(R.string.system_collection_tag);
        ArrayList<String> names = new ArrayList<>();
        for (int i = 1; i <= IDSystemCollection.values().length; i++) {
            names.add(systemTag + i);
        }
        return names;
    }

    /**
     * Отримує користувацьку назву системної колекції за її технічною назвою.
     * Якщо передана назва не є системною колекцією, повертається оригінальна назва.
     *
     * @param name Назва системної колекції для користувача
     * @return String Користувацька назва колекції або оригінальна назва
     */
    public String getCustomNameSystemCollectionByName(String name) {
        String systemTag = context.getString(R.string.system_collection_tag);

        if (name != null) {
            if (Objects.equals(name, systemTag + "1")) { return context.getString(R.string.favorites); }
            else if (Objects.equals(name, systemTag + "2")) { return context.getString(R.string.my_recipes); }
            else if (Objects.equals(name, systemTag + "3")) { return context.getString(R.string.gpt_recipes); }
            else if (Objects.equals(name, systemTag + "4")) { return context.getString(R.string.import_recipes); }
            else return name;
        }
        else { return "Unknown Collection"; }
    }

    /**
     * Оновлює інформацію про колекцію.
     * @param item Об'єкт колекції для оновлення
     * @return Completable Результат операції
     */
    @Override
    public Completable update(Collection item) {
        return dao.update(item);
    }

    /**
     * Оновлює колекцію та повертає оновлений об'єкт.
     * @param collection Колекція для оновлення
     * @return Single<Collection> Оновлений об'єкт колекції
     */
    public Single<Collection> updateAndGet(Collection collection) {
        return dao.update(collection)
                .toSingleDefault(new android.util.Pair<>(true, collection.getName()))
                .onErrorReturnItem(new android.util.Pair<>(false, ""))
                .flatMap(pair -> {
                    if (pair.first) {
                        return getByName(pair.second);
                    } else {
                        return Single.just(new Collection("", CollectionType.VOID));
                    }
                });
    }

    /**
     * Очищає список покупок (видаляє всі інгредієнти та страви).
     * @param item Список покупок для очищення
     * @return Single<Boolean> Результат операції
     */
    public Single<Boolean> clearShopList(ShopList item) {
        return Single.zip(
                        ingredientShopListRepository.deleteAll(item.getIngredients()),
                        dishCollectionRepository.deleteAllByIDCollection(item.getId()),
                        Pair::new
                )
                .flatMap(result -> {
                    if (result.first && result.second) {
                        return Single.just(true);
                    } else { return Single.just(false); }
                });
    }

    /**
     * Видаляє колекцію.
     * @param item Об'єкт колекції для видалення
     * @return Completable Результат операції
     */
    @Override
    public Completable delete(Collection item) {
        return dao.delete(item);
    }

    /**
     * Видаляє колекцію разом з усіма стравами, які до неї входять.
     * @param collection Колекція для видалення
     * @return Completable Результат операції
     */
    public Completable deleteWithDishes(Collection collection) {
        return dishCollectionRepository.getAllIDsDishByIDCollection(collection.getId())
                .flatMapCompletable(ids ->
                        Observable.fromIterable(ids)
                                .flatMapSingle(id -> dishRepository.getByID(id))
                                .filter(dish -> dish.getId() > 0)
                                .flatMapCompletable(dish -> dishRepository.delete(dish))
                )
                .andThen(delete(collection));
    }

    /**
     * Видаляє список колекцій.
     * @param items Список колекцій для видалення
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> deleteAll(ArrayList<Collection> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(collection ->
                        dao.delete(collection)
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

    /**
     * Видаляє всі колекції, крім системних.
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> deleteAllWithoutSystem() {
        return dao.getAll()
                .flatMap(collections -> Observable.fromIterable(collections)
                        .flatMapSingle(collection -> {
                            if (collection.getName().contains(Collection.SYSTEM_COLLECTION_TAG)) return Single.just(true);
                            else {
                                return dao.delete(collection)
                                        .andThen(Single.just(true))
                                        .onErrorReturnItem(false);
                            }
                        })
                        .toList()
                )
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
