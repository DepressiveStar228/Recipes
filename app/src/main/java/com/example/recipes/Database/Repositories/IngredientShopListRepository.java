package com.example.recipes.Database.Repositories;

import android.content.Context;
import android.util.Log;

import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Database.ViewModels.IngredientShopListViewModel;
import com.example.recipes.Enum.ID_System_Collection;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.IngredientShopListAmountType;
import com.example.recipes.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для роботи зі інгредієнтами для списків покупкок.
 * Цей клас реалізує інтерфейс Utils<IngredientShopList>, що дозволяє виконувати CRUD операції над інгредієнтами для списків покупкок.
 */
public class IngredientShopListRepository implements Utils<IngredientShopList> {
    private final Context context;
    private final IngredientShopListDAO dao;
    private final IngredientShopListViewModel viewModel;
    private IngredientShopListAmountTypeRepository ingredientShopListAmountTypeRepository;

    /**
     * Конструктор класу IngredientShopListRepository.
     * Ініціалізує DAO та ViewModel для роботи з інгредієнтами списків покупок.
     */
    public IngredientShopListRepository(Context context, IngredientShopListDAO dao) {
        this.context = context;
        this.dao = dao;
        this.viewModel = new IngredientShopListViewModel(dao);
    }

    /**
     * Встановлює залежності для IngredientShopListRepository.
     * @param ingredientShopListAmountTypeRepository Репозиторій типів кількостей інгредієнтів
     */
    public void setDependencies(IngredientShopListAmountTypeRepository ingredientShopListAmountTypeRepository) {
        this.ingredientShopListAmountTypeRepository = ingredientShopListAmountTypeRepository;
    }

    /**
     * Повертає DAO для роботи з інгредієнтами списків покупок.
     * @return Об'єкт IngredientShopListDAO
     */
    public IngredientShopListDAO getDao() {
        return dao;
    }

    /**
     * Повертає ViewModel для роботи з інгредієнтами списків покупок.
     * @return Об'єкт IngredientShopListViewModel
     */
    public IngredientShopListViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Додає новий інгредієнт до списку покупок.
     * @param item Об'єкт IngredientShopList для додавання
     * @return Single<Long> ID створеного інгредієнта
     */
    @Override
    public Single<Long> add(IngredientShopList item) {
        return dao.insert(item);
    }

    /**
     * Додає інгредієнт до списку покупок разом з типами кількостей.
     * @param item Об'єкт IngredientShopList для додавання
     * @param idDish ID страви (якщо інгредієнт додається зі страви)
     * @return Single<Long> ID створеного інгредієнта
     */
    public Single<Long> add(IngredientShopList item, Long idDish) {
        Map<IngredientType, ArrayList<String>> amountType = item.getGroupedAmountType();

        return dao.insert(item)
                .flatMap(id -> {
                    if (id > 0) {
                        return Observable.fromIterable(amountType.entrySet())
                                .flatMap(entry -> Observable.fromIterable(entry.getValue())
                                        .flatMapSingle(amount -> ingredientShopListAmountTypeRepository.add(new IngredientShopListAmountType(amount, entry.getKey(), id, idDish))
                                        )
                                )
                                .toList()
                                .flatMap(results -> {
                                    boolean success = results.stream().allMatch(resultId -> resultId > 0);
                                    if (success) {
                                        return Single.just(id);
                                    } else {
                                        return Single.just(-1L);
                                    }
                                });
                    } else {
                        return Single.just(-1L);
                    }
                });
    }

    /**
     * Додає список інгредієнтів до списку покупок.
     * @param items Список об'єктів IngredientShopList для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> addAll(ArrayList<IngredientShopList> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(item -> add(item)
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
     * Додає список інгредієнтів до вказаної колекції.
     * @param idCollection ID колекції
     * @param items Список інгредієнтів (можуть бути IngredientShopList, Ingredient або String)
     * @param <T> Тип інгредієнта
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public <T> Single<Boolean> addAll(long idCollection, ArrayList<T> items) {
        return Observable.fromIterable(items)
                .flatMapSingle(ing -> {
                    if (ing instanceof IngredientShopList) {
                        IngredientShopList ingSH = (IngredientShopList) ing;
                        return add(
                                new IngredientShopList(
                                        ingSH.getName().trim(),
                                        idCollection,
                                        ingSH.getIsBuy()
                                )).flatMap(id -> Single.just(id > 0));
                    } else if (ing instanceof Ingredient) {
                        Ingredient ingN = (Ingredient) ing;
                        return add(new IngredientShopList(
                                        ingN.getName().trim(),
                                        ingN.getAmount(),
                                        ingN.getType(),
                                        idCollection
                                )).flatMap(id -> Single.just(id > 0));
                    } else if (ing instanceof String) {
                        return add(new IngredientShopList((String) ing, idCollection)).flatMap(id -> Single.just(id > 0));
                    } else {
                        return Single.just(-1L).flatMap(id -> Single.just(id > 0));
                    }
                })
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
     * Отримує всі інгредієнти списків покупок.
     * @return Single<List<IngredientShopList>> Список усіх інгредієнтів
     */
    @Override
    public Single<List<IngredientShopList>> getAll() {
        return dao.getAll().flatMap(ingredientShopLists -> {
            if (ingredientShopLists != null) {
                return Observable.fromIterable(ingredientShopLists)
                        .flatMapSingle(this::getDataFromIngredient)
                        .toList();
            }
            else { return Single.just(new ArrayList<IngredientShopList>()); }
        }).onErrorReturnItem(new ArrayList<>());
    }

    /**
     * Отримує інгредієнти для вказаної колекції.
     * @param idCollection ID колекції
     * @return Single<List<IngredientShopList>> Список інгредієнтів
     */
    public Single<List<IngredientShopList>> getAllByIDCollection(long idCollection) {
        return dao.getAllByIDCollection(idCollection)
                .switchIfEmpty(Single.just(new ArrayList<>()))
                .flatMap(ingredientShopLists -> {
                    if (ingredientShopLists != null) {
                        return Observable.fromIterable(ingredientShopLists)
                                .flatMapSingle(this::getDataFromIngredient)
                                .toList();
                    }
                    else { return Single.just(new ArrayList<IngredientShopList>()); }
                })
                .onErrorReturnItem(new ArrayList<>());
    }

    /**
     * Отримує інгредієнти для вказаної колекції та сортує їх за статусом покупки.
     * @param idCollection ID колекції
     * @return Single<List<IngredientShopList>> Список інгредієнтів
     */
    public Single<List<IngredientShopList>> getAllByIDCollectionAndSortedByIsBuy(long idCollection) {
        return dao.getAllByIDCollectionAndSortedByIsBuy(idCollection)
                .switchIfEmpty(Single.just(new ArrayList<>()))
                .flatMap(ingredientShopLists -> {
                    if (ingredientShopLists != null) {
                        return Observable.fromIterable(ingredientShopLists)
                                .flatMapSingle(this::getDataFromIngredient)
                                .toList();
                    }
                    else { return Single.just(new ArrayList<IngredientShopList>()); }
                })
                .onErrorReturnItem(new ArrayList<>());
    }

    /**
     * Отримує всі інгредієнти з чорного списку.
     * @return Single<List<IngredientShopList>> Список інгредієнтів чорного списку
     */
    public Single<List<IngredientShopList>> getAllByBlackList() {
        return dao.getAllByIDCollection(ID_System_Collection.ID_BLACK_LIST.getId()).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Отримує всі назви інгредієнтів з чорного списку.
     * @return Single<List<String>> Список назв інгредієнтів чорного списку
     */
    public Single<List<String>> getAllNamesByBlackList() {
        return dao.getAllByIDCollection(ID_System_Collection.ID_BLACK_LIST.getId())
                .switchIfEmpty(Single.just(new ArrayList<>()))
                .flatMap(ingredientShopLists -> Observable.fromIterable(ingredientShopLists)
                        .flatMapSingle(ingredientShopList -> Single.just(ingredientShopList.getName()))
                        .toList()
                );
    }

    /**
     * Фільтрує інгредієнти, виключаючи ті, що є в чорному списку.
     * @param ingredientShopLists Список інгредієнтів для фільтрації
     * @return Single<List<Ingredient>> Відфільтрований список інгредієнтів
     */
    public Single<List<Ingredient>> filteredBlackList(ArrayList<Ingredient> ingredientShopLists) {
        return getAllNamesByBlackList()
                .flatMap(blackListIngredient ->
                        Observable.fromIterable(ingredientShopLists)
                                .filter(ingredient -> !blackListIngredient.contains(ingredient.getName()))
                                .toList()
                );
    }

    /**
     * Конвертує список інгредієнтів у список інгредієнтів для списку покупок.
     * @param ingredients Список інгредієнтів для конвертації
     * @return Single<List<IngredientShopList>> Список інгредієнтів списку покупок
     */
    public Single<List<IngredientShopList>> convertIngredientsToIngredientsShopList(ArrayList<Ingredient> ingredients) {
        return Observable.fromIterable(ingredients)
                .flatMapSingle(ingredient -> Single.just(new IngredientShopList(ingredient)))
                .collect(Collectors.toList());
    }

    /**
     * Групує інгредієнти за назвою та типом.
     * @param ingredientShopLists Список інгредієнтів для групування
     * @param collection Колекція, до якої належать інгредієнти
     * @return Single<ArrayList<IngredientShopList>> Згрупований список інгредієнтів
     */
    public Single<ArrayList<IngredientShopList>> groupIngredients(List<IngredientShopList> ingredientShopLists, Collection collection) {
        Map<String, Map<IngredientType, ArrayList<String>>> groupedIngredients = new HashMap<>();

        for (IngredientShopList ingredient : ingredientShopLists) {
            groupedIngredients.putIfAbsent(ingredient.getName(), new HashMap<>());
            Map<IngredientType, ArrayList<String>> innerMap = groupedIngredients.get(ingredient.getName());
            ingredient.getGroupedAmountType().forEach((type, amounts) -> {
                innerMap.putIfAbsent(type, new ArrayList<>());
                innerMap.get(type).addAll(amounts);
            });
        }

        List<IngredientShopList> result = groupedIngredients.entrySet().stream()
                .map(item -> new IngredientShopList(item.getKey(), item.getValue(), collection.getId()))
                .collect(Collectors.toList());

        return Single.just((ArrayList<IngredientShopList>) result);
    }

    /**
     * Отримує інгредієнт за ID.
     * @param id ID інгредієнта
     * @return Single<IngredientShopList> Об'єкт інгредієнта
     */
    @Override
    public Single<IngredientShopList> getByID(long id) {
        return dao.getByID(id)
                .switchIfEmpty(Single.just(new IngredientShopList()))
                .flatMap(ingredient -> {
                    if (ingredient != null) { return getDataFromIngredient(ingredient); }
                    else { return Single.just(new IngredientShopList()); }
                })
                .onErrorReturnItem(new IngredientShopList());
    }

    /**
     * Отримує інгредієнт за назвою та ID колекції.
     * @param name Назва інгредієнта
     * @param idCollection ID колекції
     * @return Single<IngredientShopList> Об'єкт інгредієнта
     */
    public Single<IngredientShopList> getByNameAndIDCollection(String name, long idCollection) {
        return dao.getByNameAndIDCollection(name, idCollection)
                .switchIfEmpty(Single.just(new IngredientShopList()))
                .flatMap(this::getDataFromIngredient)
                .onErrorReturnItem(new IngredientShopList());
    }

    /**
     * Заповнює об'єкт інгредієнта даними про типи кількостей.
     * @param ingredientShopList Інгредієнт для заповнення
     * @return Single<IngredientShopList> Інгредієнт з заповненими даними
     */
    public Single<IngredientShopList> getDataFromIngredient(IngredientShopList ingredientShopList) {
        return ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList.getId())
                .flatMap(amountTypes -> {
                    for (IngredientShopListAmountType amountType : amountTypes) {
                        ingredientShopList.addAmountType(amountType);
                    }
                    return Single.just(ingredientShopList);
                });
    }

    /**
     * Створює список типів кількостей для інгредієнта.
     * @param ingredientShopList Інгредієнт
     * @param idDish ID страви
     * @return ArrayList<IngredientShopListAmountTypeRepository> Список типів кількостей
     */
    public ArrayList<IngredientShopListAmountType> createIngredientShopListAmountTypesFromGroupedAmountType(IngredientShopList ingredientShopList, Long idDish) {
        return ingredientShopList.getGroupedAmountType().entrySet().stream()
                .map(type -> {
                    ArrayList<IngredientShopListAmountType> itemType = new ArrayList<>();

                    for (String amountString : type.getValue()) {
                        itemType.add(new IngredientShopListAmountType(amountString, type.getKey(), ingredientShopList.getId(), idDish));
                    }

                    return itemType;
                })
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }

    /**
     * Отримує кількість інгредієнтів у списку покупок.
     * @param idShopList ID списку покупок
     * @return Single<Integer> Кількість інгредієнтів
     */
    public Single<Integer> getCountByIdCollection(long idShopList) {
        return dao.getCountByIDShopList(idShopList);
    }

    /**
     * Отримує кількість придбаних інгредієнтів у списку покупок.
     * @param idShopList ID списку покупок
     * @return Single<Integer> Кількість придбаних інгредієнтів
     */
    public Single<Integer> getBoughtCountByIdCollection(long idShopList) {
        return dao.getBoughtCountByIDShopList(idShopList);
    }

    /**
     * Оновлює інформацію про інгредієнт.
     * @param ingredient Об'єкт інгредієнта для оновлення
     * @return Completable Результат операції
     */
    @Override
    public Completable update(IngredientShopList ingredient) {
        return dao.update(ingredient);
    }

    /**
     * Видаляє інгредієнт.
     * @param ingredient Об'єкт інгредієнта для видалення
     * @return Completable Результат операції
     */
    @Override
    public Completable delete(IngredientShopList ingredient) {
        return dao.delete(ingredient);
    }

    /**
     * Видаляє список інгредієнтів.
     * @param items Список інгредієнтів для видалення
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    @Override
    public Single<Boolean> deleteAll(ArrayList<IngredientShopList> items) {
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

    /**
     * Видаляє всі інгредієнти списків покупок.
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> deleteAll() {
        return dao.getAll()
                .flatMap(ingredientShopLists -> Observable.fromIterable(ingredientShopLists)
                        .flatMapSingle(ingredient ->
                                dao.delete(ingredient)
                                        .andThen(Single.just(true))
                                        .onErrorReturnItem(false)
                        )
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

    /**
     * Видаляє інгредієнти з пустими типами кількостей для вказаної колекції.
     * @param idCollection ID колекції
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> deleteEmptyAmountTypeByIDCollection(long idCollection) {
        return getAllByIDCollection(idCollection)
                .flatMapObservable(ingredients -> Observable.fromIterable(ingredients)
                        .flatMapSingle(ingredientShopList -> {
                            if (ingredientShopList.getGroupedAmountType().isEmpty()) {
                                return delete(ingredientShopList).toSingleDefault(true).onErrorReturnItem(false);
                            } else { return Single.just(true); }
                        })
                )
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) {
                            Log.d("RecipeUtils", "Помилка. Щось не видалено");
                            return false;
                        }
                    }

                    Log.d("RecipeUtils", "Всі інгредієнти списку покупок видалено");
                    return true;
                })
                .onErrorReturnItem(false);
    }
}
