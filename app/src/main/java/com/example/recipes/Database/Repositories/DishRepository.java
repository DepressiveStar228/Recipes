package com.example.recipes.Database.Repositories;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.recipes.Controller.ImageController;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.ViewModels.DishViewModel;
import com.example.recipes.Enum.IDSystemCollection;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
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
 * Клас для роботи зі стравами в базі даних.
 * Цей клас реалізує інтерфейс Utils<Dish>, що дозволяє виконувати CRUD операції над стравами.
 */
public class DishRepository implements Utils<Dish> {
    private final Context context;
    private final DishDAO dao;
    private final DishViewModel viewModel;
    private final ImageController imageController;

    private CollectionRepository collectionRepository;
    private DishRecipeRepository dishRecipeRepository;
    private IngredientRepository ingredientRepository;
    private DishCollectionRepository dishCollectionRepository;

    /**
     * Конструктор класу DishRepository.
     * Ініціалізує DAO та ViewModel для роботи зі стравами.
     */
    public DishRepository(Context context, DishDAO dao) {
        this.context = context;
        this.dao = dao;
        this.viewModel = new DishViewModel(dao);
        this.imageController = new ImageController(context);
    }

    /**
     * Встановлює залежності для роботи з іншими репозиторіями.
     * @param collectionRepository Репозиторій колекцій
     * @param dishRecipeRepository Репозиторій рецептів страв
     * @param ingredientRepository Репозиторій інгредієнтів
     * @param dishCollectionRepository Репозиторій колекцій страв
     */
    public void setDependencies(CollectionRepository collectionRepository,
                              DishRecipeRepository dishRecipeRepository,
                              IngredientRepository ingredientRepository,
                              DishCollectionRepository dishCollectionRepository) {
        this.collectionRepository = collectionRepository;
        this.dishRecipeRepository = dishRecipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.dishCollectionRepository = dishCollectionRepository;
    }

    /**
     * Повертає DAO для роботи зі стравами.
     * @return Об'єкт DishDAO
     */
    public DishDAO getDao() {
        return dao;
    }

    /**
     * Повертає ViewModel для роботи зі стравами.
     * @return Об'єкт DishViewModel
     */
    public DishViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Додає страву до колекції "Мої рецепти".
     * @param item Об'єкт страви для додавання
     * @return Single<Long> ID створеної страви
     */
    @Override
    public Single<Long> add(Dish item) {
        return add(item, -1);
    }

    /**
     * Додає страву до вказаної колекції.
     * @param item Об'єкт страви для додавання
     * @param idCollection ID колекції
     * @return Single<Long> ID створеної страви
     */
    public Single<Long> add(Dish item, long idCollection) {
        return collectionRepository.getByID(idCollection)
                .flatMap(collection -> {
                    if (collection != null) {
                        return add(item, new ArrayList<>(List.of(collection)));
                    } else {
                        Log.e("RecipeUtils", "Колекція не знайдена");
                        return Single.just(-1L);
                    }
                });
    }

    /**
     * Додає страву до бази даних та зв'язує її з колекціями.
     * @param item Об'єкт страви для додавання
     * @param collections Список колекцій для зв'язування
     * @return Single<Long> ID створеної страви
     */
    public Single<Long> add(Dish item, ArrayList<Collection> collections) {
        if (item != null) {
            item.setId(0L); // Щоб БД само видало вільний ID

            return getUniqueName(item.getName())
                    .flatMap(name -> {
                        item.setName(name);
                        return dao.insert(item);
                    })
                    .flatMap(id -> {
                        if (id > 0) {
                            item.setId(id);
                            return Single.zip(
                                    ingredientRepository.addAll(id, item.getIngredients()),
                                    dishRecipeRepository.addAll(item, item.getRecipes()),
                                    dishCollectionRepository.addAll(item, collections),
                                    (statusIngredient, statusDishRecipe, statusDishCollection) -> {
                                        if (statusIngredient && statusDishRecipe && statusDishCollection) {
                                            Log.d("RecipeUtils", "Страва додалась до бази");
                                            return id;
                                        } else {
                                            Log.e("RecipeUtils", "Страва не додалась до бази");
                                            return -1L;
                                        }
                                    });

                        } else {
                            Log.e("RecipeUtils", "Страва не додалась до бази");
                            return Single.just(-1L);
                        }
                    });
        } else return Single.just(-1L);
    }

    /**
     * Додає список страв до колекції "Мої рецепти".
     * @param items Список страв для додавання
     * @return Single<Boolean> Результат операції
     */
    @Override
    public Single<Boolean> addAll(ArrayList<Dish> items) {
        return addAll(items, IDSystemCollection.ID_MY_RECIPE.getId());
    }

    /**
     * Додає список страв до вказаної колекції.
     * @param items Список страв для додавання
     * @param idCollection ID колекції
     * @return Single<Boolean> Результат операції
     */
    public Single<Boolean> addAll(ArrayList<Dish> items, long idCollection) {
        return Observable.fromIterable(items)
                .flatMapSingle(item -> add(item, idCollection))
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
     * Отримує всі страви з бази даних.
     * @return Single<List<Dish>> Список усіх страв
     */
    @Override
    public Single<List<Dish>> getAll() {
        return dao.getAllIDs().flatMap(ids -> getAll(new ArrayList<>(ids)));
    }

    /**
     * Отримує страви за списком ID.
     * @param ids Список ID страв
     * @return Single<List<Dish>> Список страв
     */
    public Single<List<Dish>> getAll(ArrayList<Long> ids) {
        return Observable.fromIterable(ids)
                .flatMapSingle(this::getByID)
                .toList()
                .map(ArrayList::new);
    }

    /**
     * Отримує страву за ID та заповнює її зв'язаними даними з інших таблиць.
     * @param id ID страви
     * @return Single<Dish> Об'єкт страви
     */
    @Override
    public Single<Dish> getByID(long id) {
        return Single.zip(
                        ingredientRepository.getAllByIDDish(id),
                        dishRecipeRepository.getByDishID(id),
                        Pair::new
                )
                .flatMap(pair -> {
                    if (pair.first != null && pair.second != null) {
                        return dao.getByID(id)
                                .toSingle()
                                .map(dish -> {
                                    if (dish != null) {
                                        dish.setIngredients(new ArrayList<>(pair.first));
                                        dish.setRecipes(new ArrayList<>(pair.second));
                                        return dish;
                                    } else return new Dish("");
                                });
                    } else return Single.just(new Dish(""));
                })
                .onErrorResumeNext(throwable -> Single.just(new Dish("")));
    }

    /**
     * Отримує страву за ID без додаткових даних.
     * @param id ID страви
     * @return Single<Dish> Об'єкт страви
     */
    public Single<Dish> getByIDWithoutAdditionData(long id) {
        return dao.getByID(id).toSingle().map(dish -> dish != null ? dish : new Dish(""));
    }

    /**
     * Отримує максимальний час приготування страви.
     * @return Single<Long> Максимальний час приготування
     */
    public Single<Long> getMaxCookingTime() {
        return dao.getMaxCookingTime().switchIfEmpty(Single.just(0L));
    }

    /**
     * Отримує мінімальний час приготування страви.
     * @return Single<Long> Мінімальний час приготування
     */
    public Single<Long> getMinCookingTime() {
        return dao.getMinCookingTime().switchIfEmpty(Single.just(0L));
    }

    /**
     * Отримує колекції, до яких належить страва.
     * @param dish Об'єкт страви
     * @return Single<ArrayList<Collection>> Список колекцій
     */
    public Single<ArrayList<Collection>> getCollections(Dish dish) {
        return dishCollectionRepository.getAllIDsCollectionByIDDish(dish.getId())
                .flatMap(ids_collection -> {
                    ArrayList<Collection> collections = new ArrayList<>();

                    return Observable.fromIterable(ids_collection)
                            .flatMapSingle(id -> collectionRepository.getByID(id))
                            .toList()
                            .map(collectionsList -> {
                                collections.addAll(collectionsList);
                                return collections;
                            });
                });
    }

    /**
     * Отримує відфільтровані та відсортовані страви.
     * @param nameIngredients Список назв інгредієнтів для фільтрації
     * @param sortStatus Список прапорців для сортування (за назвою та часом створення)
     * @param rangeCookingTime Діапазон часу приготування
     * @return Single<List<Dish>> Список страв
     */
    public Single<List<Dish>> getFilteredAndSorted(ArrayList<String> nameIngredients, ArrayList<Boolean> sortStatus, ArrayList<Long> rangeCookingTime) {
        String DISH_TABLE_NAME = "dish";
        String INGREDIENT_TABLE_NAME = "ingredient";

        StringBuilder query = new StringBuilder();
        query.append("SELECT d.* FROM " + DISH_TABLE_NAME + " d ");

        if (nameIngredients != null && !nameIngredients.isEmpty()) {
            query.append("INNER JOIN " + INGREDIENT_TABLE_NAME + " ing ON d.id = ing.id_dish ");
            query.append("WHERE ing.name IN ('");
            for (int i = 0; i < nameIngredients.size(); i++) {
                query.append(nameIngredients.get(i));
                if (i < nameIngredients.size() - 1) query.append("', '");
            }
            query.append("')");
            if (rangeCookingTime != null && rangeCookingTime.size() == 2) {
                query.append(" AND d.cooking_time BETWEEN ").append(rangeCookingTime.get(0)).append(" AND ").append(rangeCookingTime.get(1));
            }
            query.append(" GROUP BY d.id ");
            query.append("HAVING COUNT(DISTINCT ing.name) = ").append(nameIngredients.size());
        } else {
            if (rangeCookingTime != null && rangeCookingTime.size() == 2) {
                query.append(" WHERE d.cooking_time BETWEEN ").append(rangeCookingTime.get(0)).append(" AND ").append(rangeCookingTime.get(1));
            }
        }

        if (sortStatus != null) {
            if (sortStatus.get(0) != null || sortStatus.get(1) != null) {
                query.append(" ORDER BY ");

                if (sortStatus.get(1) != null) {
                    query.append("d.creation_time ").append(sortStatus.get(1) ? "DESC" : "ASC").append(", ");
                }

                if (sortStatus.get(0) != null) {
                    query.append("d.name ").append(sortStatus.get(0) ? "ASC" : "DESC");
                } else {
                    query.setLength(query.length() - 2);
                }
            }
        }

        return dao.getWithFiltersAndSorting(new SimpleSQLiteQuery(query.toString())).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Генерує унікальну назву для страви.
     * @param name Базова назва
     * @return Single<String> Унікальна назва
     */
    public Single<String> getUniqueName(String name) {
        return Single.fromCallable(() -> name)
                .flatMap(dishName -> getUniqueNameRecursive(dishName, 2));
    }

    private Single<String> getUniqueNameRecursive(String dishName, int suffix) {
        return checkDuplicateName(dishName)
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        String newDishName = dishName;

                        if (newDishName.contains("№" + (suffix - 1))) newDishName = newDishName.replaceFirst("№" + (suffix - 1), "№" + suffix);
                        else newDishName = newDishName + " №" + suffix;

                        return getUniqueNameRecursive(newDishName, suffix + 1);
                    } else {
                        return Single.just(dishName);
                    }
                });
    }

    /**
     * Перевіряє, чи існує страва з такою ж назвою.
     * @param name Назва для перевірки
     * @return Single<Boolean> Результат перевірки
     */
    public Single<Boolean> checkDuplicateName(String name) {
        return dao.getIDByName(name)
                .map(Objects::nonNull)
                .switchIfEmpty(Single.just(false));
    }

    /**
     * Отримує кількість страв у базі.
     * @return Single<Integer> Кількість страв
     */
    public Single<Integer> getCount() {
        return dao.getCount();
    }

    /**
     * Оновлює інформацію про страву.
     * @param item Об'єкт страви для оновлення
     * @return Completable Результат операції
     */
    @Override
    public Completable update(Dish item) {
        return dao.update(item);
    }

    /**
     * Видаляє страву.
     * @param item Об'єкт страви для видалення
     * @return Completable Результат операції
     */
    @Override
    public Completable delete(Dish item) {
        // Видаляє всі фото з папки страви, які використовуються в рецепті
        imageController.clearDishDirectory(item.getName());

        return dao.delete(item);
    }

    /**
     * Видаляє список страв.
     * @param items Список страв для видалення
     * @return Single<Boolean> Результат операції
     */
    @Override
    public Single<Boolean> deleteAll(ArrayList<Dish> items) {
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

                    Log.d("RecipeUtils", "Всі страви видалено");
                    return true;
                })
                .onErrorReturnItem(false);
    }

    /**
     * Видаляє всі страви з бази даних.
     * @return Single<Boolean> Результат операції
     */
    public Single<Boolean> deleteAll() {
        return dao.getAll()
                .flatMap(dishes -> Observable.fromIterable(dishes)
                        .flatMapSingle(item ->
                                dao.delete(item)
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

                    Log.d("RecipeUtils", "Всі страви видалено");
                    return true;
                })
                .onErrorReturnItem(false);
    }

}