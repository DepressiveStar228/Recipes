package com.example.recipes.Utils;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.recipes.Controller.ImageController;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.DishRecipeDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Database.DAO.IngredientShopList_AmountTypeDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Database.ViewModels.CollectionViewModel;
import com.example.recipes.Database.ViewModels.DishCollectionViewModel;
import com.example.recipes.Database.ViewModels.DishRecipeViewModel;
import com.example.recipes.Database.ViewModels.DishViewModel;
import com.example.recipes.Database.ViewModels.IngredientShopListViewModel;
import com.example.recipes.Database.ViewModels.IngredientShopList_AmountTypeViewModel;
import com.example.recipes.Database.ViewModels.IngredientViewModel;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Enum.ID_System_Collection;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Dish_Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.IngredientShopList_AmountType;
import com.example.recipes.Item.ShopList;
import com.example.recipes.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;


/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Утилітарний клас для роботи з БД.
 * Надає доступ до бази даних рецептів та реалізує патерн Singleton.
 */
public class RecipeUtils {
    private Context context;
    private RecipeDatabase database;
    private static RecipeUtils instance;

    /**
     * Конструктор класу RecipeUtils.
     *
     * @param context Контекст додатку для доступу до бази даних
     */
    public RecipeUtils(Context context) {
        this.context = context.getApplicationContext();
        try {
            database = RecipeDatabase.getInstance(this.context);
        } catch (Exception e) {
            Log.e("RecipeUtils", "База даних не створилась", e);
        }
    }

    /**
     * Повертає єдиний екземпляр класу (Singleton).
     *
     * @param context Контекст додатку
     * @return Єдиний екземпляр RecipeUtils
     */
    public static synchronized RecipeUtils getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeUtils(context);
        }
        return instance;
    }



    //
    //
    //       Dish
    //
    //

    /**
     * Повертає екземпляр внутрішній клас для роботи зі стравами.
     */
    public ByDish ByDish() {
        return new ByDish();
    }

    /**
     *
     *
     * Внутрішній клас для роботи зі стравами.
     * Реалізує інтерфейс Utils<Dish> та надає методи для CRUD операцій зі стравами.
     *
     *
     */
    public class ByDish implements Utils<Dish> {
        private final DishDAO dao;
        private final DishViewModel viewModel;

        /**
         * Конструктор класу ByDish.
         * Ініціалізує DAO та ViewModel для роботи зі стравами.
         */
        public ByDish() {
            this.dao = database.dishDao();
            this.viewModel = new DishViewModel(dao);
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
            return add(item, ID_System_Collection.ID_MY_RECIPE.getId());
        }

        /**
         * Додає страву до вказаної колекції.
         * @param item Об'єкт страви для додавання
         * @param id_collection ID колекції
         * @return Single<Long> ID створеної страви
         */
        public Single<Long> add(Dish item, long id_collection) {
            if (item != null && item.getId() != 0) item.setId(0L); // Щоб БД само видало вільний ID

            return getUniqueName(item.getName())
                    .flatMap(name -> {
                        item.setName(name);
                        return dao.insert(item);
                    })
                    .flatMap(id -> {
                        if (id > 0) {
                            item.setId(id);
                            return ByDish_Collection().addWithCheckExist(new Dish_Collection(id, id_collection))
                                    .flatMap(status -> {
                                        if (status) { return ByIngredient().addAll(id, item.getIngredients()); }
                                        else {
                                            delete(item);
                                            return Single.just(false);
                                        }
                                    })
                                    .flatMap(status -> {
                                        if (status) { return ByDishRecipe().addAll(item, item.getRecipes()); }
                                        else {
                                            delete(item);
                                            return Single.just(false);
                                        }
                                    })
                                    .flatMap(status -> {
                                        if (status) {
                                            Log.d("RecipeUtils", "Страва додалась до бази");
                                            return Single.just(id);
                                        }
                                        else {
                                            Log.e("RecipeUtils", "Страва не додалась до бази");
                                            return Single.just(-1L);
                                        }
                                    });
                        } else {
                            Log.e("RecipeUtils", "Страва не додалась до бази");
                            return Single.just(-1L);
                        }
                    });
        }

        /**
         * Додає список страв до колекції "Мої рецепти".
         * @param items Список страв для додавання
         * @return Single<Boolean> Результат операції
         */
        @Override
        public Single<Boolean> addAll(ArrayList<Dish> items) {
            return addAll(items, ID_System_Collection.ID_MY_RECIPE.getId());
        }

        /**
         * Додає список страв до вказаної колекції.
         * @param items Список страв для додавання
         * @param id_collection ID колекції
         * @return Single<Boolean> Результат операції
         */
        public Single<Boolean> addAll(ArrayList<Dish> items, long id_collection) {
            return Observable.fromIterable(items)
                    .flatMapSingle(item -> add(item, id_collection))
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
                        ByIngredient().getAllByIDDish(id),
                        ByDishRecipe().getByDishID(id),
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
         * Отримує колекції, до яких належить страва.
         * @param dish Об'єкт страви
         * @return Single<ArrayList<Collection>> Список колекцій
         */
        public Single<ArrayList<Collection>> getCollections(Dish dish) {
            return ByDish_Collection().dao.getAllIDsCollectionByIDDish(dish.getId())
                    .flatMap(ids_collection -> {
                        ArrayList<Collection> collections = new ArrayList<>();

                        return Observable.fromIterable(ids_collection)
                                .flatMapSingle(id -> ByCollection().getByID(id))
                                .toList()
                                .map(collectionsList -> {
                                    collections.addAll(collectionsList);
                                    return collections;
                                });
                    });
        }

        /**
         * Отримує відфільтровані та відсортовані страви.
         * @param ingredientNames Список інгредієнтів для фільтрації
         * @param sortStatus Статус сортування
         * @return Single<List<Dish>> Список страв
         */
        public Single<List<Dish>> getFilteredAndSorted(ArrayList<String> ingredientNames, ArrayList<Boolean> sortStatus) {
            String DISH_TABLE_NAME = "dish";
            String INGREDIENT_TABLE_NAME = "ingredient";

            StringBuilder query = new StringBuilder();
            query.append("SELECT d.* FROM " + DISH_TABLE_NAME + " d ");

            if (ingredientNames != null && !ingredientNames.isEmpty()) {
                query.append("INNER JOIN " + INGREDIENT_TABLE_NAME + " ing ON d.id = ing.id_dish ");
                query.append("WHERE ing.name IN ('");
                for (int i = 0; i < ingredientNames.size(); i++) {
                    query.append(ingredientNames.get(i));
                    if (i < ingredientNames.size() - 1) query.append("', '");
                }
                query.append("') ");
                query.append("GROUP BY d.id ");
                query.append("HAVING COUNT(DISTINCT ing.name) = ").append(ingredientNames.size());
            }

            query.append(" ORDER BY ");
            if (sortStatus.get(1) != null) {
                query.append("d.timestamp ").append(sortStatus.get(1) ? "DESC" : "ASC").append(", ");
            }

            if (sortStatus.get(0) != null) {
                query.append("d.name ").append(sortStatus.get(0) ? "ASC" : "DESC");
            } else {
                query.setLength(query.length() - 2);
            }


            return dao.getWithFiltersAndSorting(new SimpleSQLiteQuery(query.toString()));
        }

        /**
         * Генерує унікальну назву для страви.
         * @param name Базова назва
         * @return Single<String> Унікальна назва
         */
        public Single<String> getUniqueName(String name) {
            return Single.fromCallable(() -> name)
                    .flatMap(dishName -> getUniqueNameRecursive(dishName, 1));
        }

        private Single<String> getUniqueNameRecursive(String dishName, int suffix) {
            return checkDuplicateName(dishName)
                    .flatMap(isDuplicate -> {
                        if (isDuplicate) {
                            String newDishName = dishName + " №" + suffix;
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
                    .map(id -> id != null)
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
            ImageController imageController = new ImageController(context);
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









    //
    //
    //       Ingredient
    //
    //

    /**
     * Повертає екземпляр внутрішній клас для роботи із інгредієнтами.
     */
    public ByIngredient ByIngredient() {
        return new ByIngredient();
    }

    /**
     * Внутрішній клас для роботи з інгредієнтами.
     * Реалізує інтерфейс Utils<Ingredient> та надає методи для CRUD операцій з інгредієнтами.
     */
    public class ByIngredient implements Utils<Ingredient> {
        private final IngredientDAO dao;
        private final IngredientViewModel viewModel;

        /**
         * Конструктор класу ByIngredient.
         * Ініціалізує DAO та ViewModel для роботи з інгредієнтами.
         */
        public ByIngredient() {
            this.dao = database.ingredientDao();
            this.viewModel = new IngredientViewModel(dao);
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
         * @param id_dish ID страви
         * @param items Список інгредієнтів для додавання
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> addAll(Long id_dish, ArrayList<Ingredient> items) {
            return Observable.fromIterable(items)
                    .flatMapSingle(ing -> dao.insert(new Ingredient(ing.getName().trim(), ing.getAmount(), ing.getType(), id_dish))
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
         * @param id_dish ID страви
         * @return Single<List<Ingredient>> Список інгредієнтів
         */
        public Single<List<Ingredient>> getAllByIDDish(Long id_dish) {
            return dao.getAllByIDDish(id_dish);
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
         * @return Single<List<String>> Список унікальних назв
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











    //
    //
    //       Collection
    //
    //

    /**
     * Повертає екземпляр внутрішній клас для роботи з колекціями.
     */
    public ByCollection ByCollection() {
        return new ByCollection();
    }

    public class ByCollection implements Utils<Collection> {
        private final CollectionDAO dao;
        private final CollectionViewModel viewModel;

        /**
         * Конструктор класу ByCollection.
         * Ініціалізує DAO та ViewModel для роботи з колекціями.
         */
        public ByCollection() {
            dao = database.collectionDao();
            viewModel = new CollectionViewModel(dao);
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
            return dao.getAll()
                    .flatMap(collections -> Observable.fromIterable(collections)
                            .flatMapSingle(collection -> {
                                String customName = getCustomNameSystemCollection(collection.getName());
                                collection.setName(customName);
                                return Single.just(collection);
                            })
                            .flatMapSingle(this::getDataForCollection)
                            .toList());
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
                                String customName = getCustomNameSystemCollection(collection.getName());
                                collection.setName(customName);
                                return Single.just(collection);
                            })
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
            return dao.getById(id)
                    .flatMap(collection -> {
                        String customName = getCustomNameSystemCollection(collection.getName());
                        return Single.just(new Collection(collection.getId(), customName, CollectionType.COLLECTION, collection.getDishes()));
                    })
                    .flatMap(this::getDataForCollection)
                    .doOnError(throwable -> Log.e("RxError", "Error occurred: ", throwable))
                    .onErrorResumeNext(throwable -> {
                        Log.e("RxError", "Возврат пустой коллекции из-за ошибки", throwable);
                        return Single.just(new Collection(-1, "Unknown Collection",  CollectionType.COLLECTION, new ArrayList<>()));
                    });
        }

        /**
         * Отримує колекцію за назвою.
         * @param name Назва колекції
         * @return Single<Collection> Об'єкт колекції
         */
        public Single<Collection> getByName(String name) {
            return dao.getByName(name)
                    .flatMap(collection -> {
                        String customName = getCustomNameSystemCollection(collection.getName());
                        return Single.just(new Collection(collection.getId(), customName, collection.getType(), collection.getDishes()));
                    })
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
        public Single<Long> getIdByName(String name) {
            return dao.getIdByName(name)
                    .toSingle()
                    .onErrorResumeNext(throwable -> Single.just((long) -1));
        }

        /**
         * Отримує ID колекції за назвою та типом.
         * @param name Назва колекції
         * @param type Тип колекції
         * @return Single<Long> ID колекції (-1 якщо не знайдено)
         */
        public Single<Long> getIdByNameAndType(String name, CollectionType type) {
            return dao.getIdByNameAndType(name, type)
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
                    ByDish().getAll(),
                    getDishes(collection.getId()),
                    (allDishes, collectionDishes) -> {
                        ArrayList<Dish> unused_dished = new ArrayList<>();
                        for (Dish dish : allDishes) {
                            if (!collectionDishes.contains(dish)) {
                                unused_dished.add(dish);
                            }
                        }

                        return unused_dished;
                    }
            );
        }

        /**
         * Отримує всі страви у колекції.
         * @param id_collection ID колекції
         * @return Single<List<Dish>> Список страв у колекції
         */
        public Single<List<Dish>> getDishes(long id_collection) {
            return ByDish_Collection().dao.getAllIDsDishByIDCollection(id_collection)
                    .flatMap(ids -> {
                        if (ids == null || ids.isEmpty()) {
                            return Single.just(new ArrayList<>());
                        } else {
                            return Observable.fromIterable(ids)
                                    .flatMapSingle(id -> ByDish().getByID(id))
                                    .toList();
                        }
                    });
        }

        /**
         * Отримує колекції, які не містять вказану страву.
         * @param dish Страва для перевірки
         * @return Single<ArrayList<Collection>> Список колекцій, які не містять страву
         */
        public Single<ArrayList<Collection>> getUnusedInDish(Dish dish) {
            return Single.zip(
                    ByCollection().getAll(),
                    ByDish().getCollections(dish),
                    (allCollection, dishesCollection) -> {
                        ArrayList<Collection> unused_collection = new ArrayList<>();
                        for (Collection collection : allCollection) {
                            collection.setName(getCustomNameSystemCollection(collection.getName()));

                            if (!dishesCollection.contains(collection)) {
                                unused_collection.add(collection);
                            }
                        }

                        return unused_collection;
                    }
            );
        }

        /**
         * Отримує колекції вказаного типу, які не містять вказану страву.
         * @param dish Страва для перевірки
         * @param collectionType Тип колекції
         * @return Single<ArrayList<Collection>> Список колекцій, які не містять страву
         */
        public Single<ArrayList<Collection>> getUnusedByTypeInDish(Dish dish, CollectionType collectionType) {
            return Single.zip(
                    ByCollection().getAllByType(collectionType),
                    ByDish().getCollections(dish),
                    (allCollection, dishesCollection) -> {
                        ArrayList<Collection> unused_collection = new ArrayList<>();
                        for (Collection collection : ClassUtils.getListOfType(allCollection, Collection.class)) {
                            collection.setName(getCustomNameSystemCollection(collection.getName()));

                            if (!dishesCollection.contains(collection)) {
                                unused_collection.add(collection);
                            }
                        }

                        return unused_collection;
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
                                } catch (Exception e) {}

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
            return dao.getIdByName(name)
                    .map(id -> id != null)
                    .switchIfEmpty(Single.just(false));
        }

        /**
         * Генерує унікальну назву для списку покупок на основі поточної дати.
         * @return Single<String> Унікальна назва
         */
        public Single<String> generateUniqueNameForShopList() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
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
                            ByIngredientShopList().getAllByIDCollection(collection.getId())
                                    .flatMap(ingredientShopLists -> {
                                        item.setIngredients(new ArrayList<>(ingredientShopLists));
                                        return Single.just(item);
                                    })
                    );
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
                            return ByCollection().getByName(pair.second);
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
                        ByIngredientShopList().deleteAll(item.getIngredients()),
                        ByDish_Collection().deleteAllByIDCollection(item.getId()),
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
            return ByDish_Collection().dao.getAllIDsDishByIDCollection(collection.getId())
                    .flatMapCompletable(ids ->
                            Observable.fromIterable(ids)
                                    .flatMapSingle(id -> ByDish().dao.getByID(id)
                                            .defaultIfEmpty(new Dish(""))
                                    )
                                    .filter(dish -> dish.getId() > 0)
                                    .flatMapCompletable(dish -> ByDish().dao.delete(dish))
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








    //
    //
    //       Collection
    //
    //

    /**
     * Повертає екземпляр внутрішній клас для роботи зі зв'язками страв з колекціями.
     */
    public ByDish_Collection ByDish_Collection() {
        return new ByDish_Collection();
    }

    /**
     * Внутрішній клас для роботи зі зв'язками між стравами та колекціями.
     * Реалізує інтерфейс Utils<Dish_Collection> та надає методи для роботи з таблицею dish_collection.
     */
    public class ByDish_Collection implements Utils<Dish_Collection> {
        private final DishCollectionDAO dao;
        private final DishCollectionViewModel viewModel;

        /**
         * Конструктор класу ByDish_Collection.
         * Ініціалізує DAO та ViewModel для роботи зі зв'язками страв і колекцій.
         */
        public ByDish_Collection() {
            this.dao = database.dishCollectionDao();
            this.viewModel = new DishCollectionViewModel(dao);
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
         * @param item Об'єкт Dish_Collection для додавання
         * @return Single<Long> ID створеного зв'язку
         */
        @Override
        public Single<Long> add(Dish_Collection item) {
            return dao.insert(item);
        }

        /**
         * Додає зв'язок між стравою та колекцією з перевіркою на наявність.
         * @param item Об'єкт Dish_Collection для додавання
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> addWithCheckExist(Dish_Collection item) {
            return isExist(item)
                    .flatMap(isInCollection -> {
                        if (!isInCollection) {
                            return add(item).map(id -> (id > 0));
                        } else {
                            return ByCollection().getByID(item.getId_collection())
                                    .map(collection -> getCustomNameSystemCollection(collection.getName()))
                                    .flatMap(name -> {
                                        if (name != null) {
                                            Toast.makeText(context, context.getString(R.string.dish_dublicate_in_collection) + " " + name, Toast.LENGTH_SHORT).show();
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
         * @param items Список об'єктів Dish_Collection для додавання
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        @Override
        public Single<Boolean> addAll(ArrayList<Dish_Collection> items) {
            return Observable.fromIterable(items)
                    .flatMapSingle(item -> isExist(item)
                            .concatMap(isInCollection -> {
                                if (!isInCollection) {
                                    return add(new Dish_Collection(item.getId_dish(), item.getId_collection())).map(id -> id > 0);
                                } else {
                                    return ByCollection().getByID(item.getId_collection())
                                            .map(collection -> getCustomNameSystemCollection(collection.getName()))
                                            .flatMap(name -> {
                                                if (name != null) {
                                                    Toast.makeText(context, context.getString(R.string.dish_dublicate_in_collection) + " " + name, Toast.LENGTH_SHORT).show();
                                                    return Single.just(true);
                                                } else {
                                                    return Single.just(false);
                                                }
                                            });
                                }
                            })
                    )
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
         * @param id_collections Список ID колекцій
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> addAll(Dish dish, ArrayList<Long> id_collections) {
            return Observable.fromIterable(id_collections)
                    .flatMapSingle(id_collection -> isExist(new Dish_Collection(dish.getId(), id_collection))
                            .concatMap(isInCollection -> {
                                if (!isInCollection) {
                                    return add(new Dish_Collection(dish.getId(), id_collection)).map(id -> id > 0);
                                } else {
                                    return ByCollection().getByID(id_collection)
                                            .map(collection -> getCustomNameSystemCollection(collection.getName()))
                                            .flatMap(name -> {
                                                if (name != null) {
                                                    Toast.makeText(context, context.getString(R.string.dish_dublicate_in_collection) + " " + name, Toast.LENGTH_SHORT).show();
                                                    Log.d("RecipeUtils", "Страва " + dish.getName() + " вже є в колекції " + name);
                                                    return Single.just(true);
                                                } else {
                                                    return Single.just(false);
                                                }
                                            });
                                }
                            })
                    )
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
         * @param id_collection ID колекції
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> addAll(ArrayList<Dish> dishes, long id_collection) {
            return Observable.fromIterable(dishes)
                    .flatMapSingle(dish -> add(new Dish_Collection(dish.getId(), id_collection)).map(id -> id > 0))
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
         * Додає список страв до колекції з перевіркою на наявність.
         * @param dishes Список страв
         * @param id_collection ID колекції
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> addAllWithCheckExist(ArrayList<Dish> dishes, long id_collection) {
            return Observable.fromIterable(dishes)
                    .flatMapSingle(dish -> isExist(new Dish_Collection(dish.getId(), id_collection))
                            .concatMap(isInCollection -> {
                                if (!isInCollection) {
                                    return add(new Dish_Collection(dish.getId(), id_collection)).map(id -> id > 0);
                                } else {
                                    return ByCollection().getByID(id_collection)
                                            .map(collection -> getCustomNameSystemCollection(collection.getName()))
                                            .flatMap(name -> {
                                                if (name != null) {
                                                    Toast.makeText(context, context.getString(R.string.dish_dublicate_in_collection) + " " + name, Toast.LENGTH_SHORT).show();
                                                    Log.d("RecipeUtils", "Страва " + dish.getName() + " вже є в колекції " + name);
                                                    return Single.just(true);
                                                } else {
                                                    return Single.just(false);
                                                }
                                            });
                                }
                            })
                    )
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
        public Single<List<Dish_Collection>> getAll() {
            return null;
        }

        /**
         * Отримує зв'язок за ID.
         * @param id ID зв'язку
         * @return Single<Dish_Collection> Об'єкт зв'язку
         */
        @Override
        public Single<Dish_Collection> getByID(long id) {
            return dao.getByID(id)
                    .toSingle()
                    .onErrorReturnItem(new Dish_Collection(0, 0));
        }

        /**
         * Отримує зв'язок за ID страви та ID колекції.
         * @param id_dish ID страви
         * @param id_collection ID колекції
         * @return Single<Dish_Collection> Об'єкт зв'язку
         */
        public Single<Dish_Collection> getByData(long id_dish, long id_collection) {
            return dao.getByIDDishAndIDCollection(id_dish, id_collection)
                    .toSingle()
                    .onErrorReturnItem(new Dish_Collection(0, 0));
        }

        /**
         * Перевіряє, чи існує зв'язок між стравою та колекцією.
         * @param dish_collection Об'єкт зв'язку для перевірки
         * @return Single<Boolean> Результат перевірки (true - існує, false - не існує)
         */
        public Single<Boolean> isExist(Dish_Collection dish_collection) {
            return dao.getByIDDishAndIDCollection(dish_collection.getId_dish(), dish_collection.getId_collection())
                    .map(dishCollection -> {
                        Log.d("isDishInCollection", "Страва з айді " + dish_collection.getId_dish() + " знайдена в колекції");
                        return true;
                    })
                    .defaultIfEmpty(false)
                    .doOnSuccess(result -> Log.d("isDishInCollection", "Result: " + result))
                    .doOnError(throwable -> Log.e("isDishInCollection", "Error: " + throwable.getMessage()));
        }

        /**
         * Копіює страви з однієї колекції до інших.
         * @param id_collection_origin ID вихідної колекції
         * @param id_collections Список ID цільових колекцій
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> copyDishesToAnotherCollections(long id_collection_origin, ArrayList<Long> id_collections) {
            return ByCollection().getDishes(id_collection_origin)
                    .flatMap(dishes -> {
                        if (dishes.isEmpty()) {
                            return Single.just(false);
                        }
                        return Observable.fromIterable(id_collections)
                                .flatMapSingle(id_collection ->
                                        Observable.fromIterable(dishes)
                                                .flatMapSingle(dish ->
                                                        checkDuplicateData(dish.getId(), id_collection)
                                                                .flatMap(isDuplicate -> {
                                                                    if (!isDuplicate) {
                                                                        return ByDish_Collection().addWithCheckExist(new Dish_Collection(dish.getId(), id_collection));
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
         * Перевіряє наявність дубліката зв'язку.
         * @param id_dish ID страви
         * @param id_collection ID колекції
         * @return Single<Boolean> Результат перевірки (true - дублікат існує, false - немає дубліката)
         */
        public Single<Boolean> checkDuplicateData(long id_dish, long id_collection) {
            return dao.getByIDDishAndIDCollection(id_dish, id_collection)
                    .map(dishCollection -> true)
                    .defaultIfEmpty(false);
        }

        /**
         * Оновлює інформацію про зв'язок між стравою та колекцією.
         * @param item Зв'язок між стравою та колекцією для оновлення
         * @return Completable Результат операції
         */
        @Override
        public Completable update(Dish_Collection item) {
            return null;
        }

        /**
         * Видаляє зв'язок між стравою та колекцією.
         * @param item Об'єкт зв'язку для видалення
         * @return Completable Результат операції
         */
        @Override
        public Completable delete(Dish_Collection item) {
            return dao.delete(item);
        }

        /**
         * Видаляє список зв'язків.
         * @param items Список об'єктів зв'язків для видалення
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        @Override
        public Single<Boolean> deleteAll(ArrayList<Dish_Collection> items) {
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
         * @param id_collection ID колекції
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> deleteAllByIDCollection(long id_collection) {
            return dao.getAllIDsDishByIDCollection(id_collection)
                    .flatMapObservable(items -> Observable.fromIterable(items)
                            .flatMapSingle(item -> dao.getByIDDishAndIDCollection(item, id_collection)
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





    //
    //
    //       ByIngredientShopList
    //
    //

    /**
     * Повертає екземпляр внутрішній клас для роботи з інгредієнтами для списків покупків.
     */
    public ByIngredientShopList ByIngredientShopList() {
        return new ByIngredientShopList();
    }

    /**
     * Внутрішній клас для роботи з інгредієнтами списків покупок.
     * Реалізує інтерфейс Utils<IngredientShopList> та надає методи для роботи з інгредієнтами у списках покупок.
     */
    public class ByIngredientShopList implements Utils<IngredientShopList> {
        private final IngredientShopListDAO dao;
        private final IngredientShopListViewModel viewModel;

        /**
         * Конструктор класу ByIngredientShopList.
         * Ініціалізує DAO та ViewModel для роботи з інгредієнтами списків покупок.
         */
        public ByIngredientShopList() {
            this.dao = database.ingredientShopListDao();
            this.viewModel = new IngredientShopListViewModel(dao);
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
        public Single<Long> add(@NonNull IngredientShopList item) {
            return dao.insert(item);
        }

        /**
         * Додає інгредієнт до списку покупок разом з типами кількостей.
         * @param item Об'єкт IngredientShopList для додавання
         * @param id_dish ID страви (якщо інгредієнт додається зі страви)
         * @return Single<Long> ID створеного інгредієнта
         */
        public Single<Long> add(@NonNull IngredientShopList item, Long id_dish) {
            Map<IngredientType, ArrayList<String>> amountType = item.getGroupedAmountType();

            return dao.insert(item)
                    .flatMap(id -> {
                        if (id > 0) {
                            return Observable.fromIterable(amountType.entrySet())
                                    .flatMap(entry -> Observable.fromIterable(entry.getValue())
                                            .flatMapSingle(amount -> ByIngredientShopList_AmountType()
                                                    .add(new IngredientShopList_AmountType(amount, entry.getKey(), id, id_dish))
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
        public Single<Boolean> addAll(@NonNull ArrayList<IngredientShopList> items) {
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
         * @param id_collection ID колекції
         * @param items Список інгредієнтів (можуть бути IngredientShopList, Ingredient або String)
         * @param <T> Тип інгредієнта
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public <T> Single<Boolean> addAll(@NonNull long id_collection, @NonNull ArrayList<T> items) {
            return Observable.fromIterable(items)
                    .flatMapSingle(ing -> {
                        if (ing instanceof IngredientShopList) {
                            IngredientShopList ingSH = (IngredientShopList) ing;
                            return add(
                                    new IngredientShopList(
                                            ingSH.getName().trim(),
                                            id_collection,
                                            ingSH.getIsBuy()
                                    )).flatMap(id -> Single.just(id > 0));
                        } else if (ing instanceof Ingredient) {
                            Ingredient ingN = (Ingredient) ing;
                            return ByDish().getByID(ingN.getId_dish())
                                    .flatMap(dish -> add(
                                            new IngredientShopList(
                                                    ingN.getName().trim(),
                                                    ingN.getAmount(),
                                                    ingN.getType(),
                                                    id_collection
                                            )).flatMap(id -> Single.just(id > 0)));
                        } else if (ing instanceof String) {
                            return add(new IngredientShopList((String) ing, id_collection)).flatMap(id -> Single.just(id > 0));
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
                    })
                    .onErrorReturnItem(new ArrayList<>());
        }

        /**
         * Отримує інгредієнти для вказаної колекції.
         * @param id_collection ID колекції
         * @return Single<List<IngredientShopList>> Список інгредієнтів
         */
        public Single<List<IngredientShopList>> getAllByIDCollection(@NonNull long id_collection) {
            return dao.getAllByIDCollection(id_collection)
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
            return dao.getAllByIDCollection(ID_System_Collection.ID_BLACK_LIST.getId());
        }

        /**
         * Отримує всі назви інгредієнтів з чорного списку.
         * @return Single<List<String>> Список назв інгредієнтів чорного списку
         */
        public Single<List<String>> getAllNamesByBlackList() {
            return dao.getAllByIDCollection(ID_System_Collection.ID_BLACK_LIST.getId())
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
        public Single<List<Ingredient>> filteredBlackList(@NonNull ArrayList<Ingredient> ingredientShopLists) {
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
        public Single<List<IngredientShopList>> convertIngredientsToIngredientsShopList(@NonNull ArrayList<Ingredient> ingredients) {
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
        public Single<ArrayList<IngredientShopList>> groupIngredients(@NonNull List<IngredientShopList> ingredientShopLists, @NonNull Collection collection) {
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
        public Single<IngredientShopList> getByID(@NonNull long id) {
            return dao.getById(id)
                    .flatMap(ingredient -> {
                        if (ingredient != null) { return getDataFromIngredient(ingredient); }
                        else { return Single.just(new IngredientShopList()); }
                    })
                    .onErrorReturnItem(new IngredientShopList());
        }

        /**
         * Отримує інгредієнт за назвою та ID колекції.
         * @param name Назва інгредієнта
         * @param id_collection ID колекції
         * @return Single<IngredientShopList> Об'єкт інгредієнта
         */
        public Single<IngredientShopList> getByNameAndIDCollection(@NonNull String name, @NonNull Long id_collection) {
            return dao.getByNameAndIDCollection(name, id_collection)
                    .switchIfEmpty(Single.just(new IngredientShopList()))
                    .flatMap(this::getDataFromIngredient)
                    .onErrorResumeNext(error -> {
                        Log.e("RxCritical", "Критическая ошибка в БД!", error);
                        return Single.error(error);
                    });
        }

        /**
         * Заповнює об'єкт інгредієнта даними про типи кількостей.
         * @param ingredientShopList Інгредієнт для заповнення
         * @return Single<IngredientShopList> Інгредієнт з заповненими даними
         */
        public Single<IngredientShopList> getDataFromIngredient(IngredientShopList ingredientShopList) {
            return ByIngredientShopList_AmountType().getByIDIngredient(ingredientShopList.getId())
                    .flatMap(amountTypes -> {
                        for (IngredientShopList_AmountType amountType : amountTypes) {
                            ingredientShopList.addAmountType(amountType);
                        }
                        return Single.just(ingredientShopList);
                    });
        }

        /**
         * Створює список типів кількостей для інгредієнта.
         * @param ingredientShopList Інгредієнт
         * @param id_dish ID страви
         * @return ArrayList<IngredientShopList_AmountType> Список типів кількостей
         */
        public ArrayList<IngredientShopList_AmountType> createIngredientShopList_AmountTypesFromGroupedAmountType(IngredientShopList ingredientShopList, Long id_dish) {
            ArrayList<IngredientShopList_AmountType> result = ingredientShopList.getGroupedAmountType().entrySet().stream()
                    .map(type -> {
                        ArrayList<IngredientShopList_AmountType> itemType = new ArrayList<>();

                        for (String amountString : type.getValue()){
                            itemType.add(new IngredientShopList_AmountType(amountString, type.getKey(), ingredientShopList.getId(), id_dish));
                        }

                        return itemType;
                    })
                    .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

            return result;
        }

        /**
         * Отримує кількість інгредієнтів у списку покупок.
         * @param id_shopList ID списку покупок
         * @return Single<Integer> Кількість інгредієнтів
         */
        public Single<Integer> getCountByIdCollection(Long id_shopList) {
            return dao.getCountByIdShopList(id_shopList);
        }

        /**
         * Отримує кількість придбаних інгредієнтів у списку покупок.
         * @param id_shopList ID списку покупок
         * @return Single<Integer> Кількість придбаних інгредієнтів
         */
        public Single<Integer> getBoughtCountByIdCollection(Long id_shopList) {
            return dao.getBoughtCountByIdShopList(id_shopList);
        }

        /**
         * Оновлює інформацію про інгредієнт.
         * @param ingredient Об'єкт інгредієнта для оновлення
         * @return Completable Результат операції
         */
        @Override
        public Completable update(@NonNull IngredientShopList ingredient) {
            return dao.update(ingredient);
        }

        /**
         * Видаляє інгредієнт.
         * @param ingredient Об'єкт інгредієнта для видалення
         * @return Completable Результат операції
         */
        @Override
        public Completable delete(@NonNull IngredientShopList ingredient) {
            return dao.delete(ingredient);
        }

        /**
         * Видаляє список інгредієнтів.
         * @param items Список інгредієнтів для видалення
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        @Override
        public Single<Boolean> deleteAll(@NonNull ArrayList<IngredientShopList> items) {
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
         * @param id_collection ID колекції
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> deleteEmptyAmountTypeByIDCollection(long id_collection) {
            return getAllByIDCollection(id_collection)
                    .flatMapObservable(ingredients -> Observable.fromIterable(ingredients)
                            .flatMapSingle(ingredientShopList -> {
                                if (ingredientShopList.getGroupedAmountType().isEmpty()) {
                                    return delete(ingredientShopList).toSingleDefault(true).onErrorReturnItem(false);
                                } else { return Single.just(false); }
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





    //
    //
    //       IngredientShopList_AmountType
    //
    //

    /**
     * Повертає екземпляр внутрішній клас для роботи з типами кількостей інгредієнтів у списках покупок.
     */
    public ByIngredientShopList_AmountType ByIngredientShopList_AmountType() {
        return new ByIngredientShopList_AmountType();
    }

    /**
     * Внутрішній клас для роботи з типами кількостей інгредієнтів у списках покупок.
     * Реалізує інтерфейс Utils<IngredientShopList_AmountType> та надає методи для керування типами кількостей.
     */
    public class ByIngredientShopList_AmountType implements Utils<IngredientShopList_AmountType> {
        private final IngredientShopList_AmountTypeDAO dao;
        private final IngredientShopList_AmountTypeViewModel viewModel;

        /**
         * Конструктор класу ByIngredientShopList_AmountType.
         * Ініціалізує DAO та ViewModel для роботи з типами кількостей інгредієнтів.
         */
        public ByIngredientShopList_AmountType() {
            dao = database.ingredientShopList_amountTypeDao();
            viewModel = new IngredientShopList_AmountTypeViewModel(dao);
        }

        /**
         * Повертає ViewModel для роботи з типами кількостей інгредієнтів.
         * @return Об'єкт IngredientShopList_AmountTypeViewModel
         */
        public IngredientShopList_AmountTypeViewModel getViewModel() {
            return viewModel;
        }

        /**
         * Додає новий тип кількості інгредієнта.
         * @param ingredient Об'єкт IngredientShopList_AmountType для додавання
         * @return Single<Long> ID створеного запису
         */
        @Override
        public Single<Long> add(IngredientShopList_AmountType ingredient) {
            return dao.insert(ingredient);
        }

        /**
         * Додає список типів кількостей інгредієнтів.
         * @param items Список об'єктів IngredientShopList_AmountType для додавання
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        @Override
        public Single<Boolean> addAll(ArrayList<IngredientShopList_AmountType> items) {
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
         * @return Single<List<IngredientShopList_AmountType>> Список усіх типів кількостей
         */
        @Override
        public Single<List<IngredientShopList_AmountType>> getAll() {
            return dao.getAll();
        }

        /**
         * Отримує тип кількості за ID.
         * @param id ID запису
         * @return Single<IngredientShopList_AmountType> Об'єкт типу кількості
         */
        @Override
        public Single<IngredientShopList_AmountType> getByID(long id) {
            return dao.getByID(id);
        }

        /**
         * Отримує типи кількостей для конкретного інгредієнта.
         * @param id_ingredient ID інгредієнта
         * @return Single<List<IngredientShopList_AmountType>> Список типів кількостей
         */
        public Single<List<IngredientShopList_AmountType>> getByIDIngredient(long id_ingredient) {
            return dao.getByIDIngredient(id_ingredient)
                    .switchIfEmpty(Single.just(new ArrayList<>()));
        }

        /**
         * Оновлює інформацію про тип кількості інгредієнта.
         * @param ingredient Об'єкт типу кількості для оновлення
         * @return Completable Результат операції
         */
        @Override
        public Completable update(IngredientShopList_AmountType ingredient) {
            return dao.update(ingredient);
        }

        /**
         * Видаляє тип кількості інгредієнта.
         * @param ingredient Об'єкт типу кількості для видалення
         * @return Completable Результат операції
         */
        @Override
        public Completable delete(IngredientShopList_AmountType ingredient) {
            return dao.delete(ingredient);
        }

        /**
         * Видаляє список типів кількостей інгредієнтів.
         * @param items Список об'єктів для видалення
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        @Override
        public Single<Boolean> deleteAll(ArrayList<IngredientShopList_AmountType> items) {
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






    //
    //
    //       Dish_Recipe
    //
    //

    /**
     * Повертає екземпляр внутрішній клас для роботи з рецептами страв.
     */
    public ByDishRecipe ByDishRecipe() {
        return new ByDishRecipe();
    }

    /**
     * Внутрішній клас для роботи з рецептами страв.
     * Реалізує інтерфейс Utils<DishRecipe> та надає методи для керування рецептами.
     */
    public class ByDishRecipe implements Utils<DishRecipe> {
        private final DishRecipeDAO dao;
        private final DishRecipeViewModel viewModel;
        private final ImageController imageController = new ImageController(context);

        /**
         * Конструктор класу ByDishRecipe.
         * Ініціалізує DAO та ViewModel для роботи з рецептами.
         */
        public ByDishRecipe() {
            dao = database.dishRecipeDao();
            viewModel = new DishRecipeViewModel(dao);
        }

        /**
         * Повертає ViewModel для роботи з рецептами.
         * @return Об'єкт DishRecipeViewModel
         */
        public DishRecipeViewModel getViewModel() {
            return viewModel;
        }

        /**
         * Додає новий рецепт до бази даних.
         * @param item Об'єкт DishRecipe для додавання
         * @return Single<Long> ID створеного рецепту
         */
        @Override
        public Single<Long> add(DishRecipe item) {
            return dao.insert(item);
        }

        /**
         * Додає список рецептів до бази даних.
         * @param items Список об'єктів DishRecipe для додавання
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        @Override
        public Single<Boolean> addAll(ArrayList<DishRecipe> items) {
            return addAll(null, items);
        }

        /**
         * Додає список рецептів для конкретної страви.
         * @param dish Об'єкт страви
         * @param items Список рецептів для додавання
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        public Single<Boolean> addAll(Dish dish, ArrayList<DishRecipe> items) {
            if (dish != null) {
                return Observable.fromIterable(items)
                        .flatMapSingle(item -> {
                            if (item.getTypeData() == DishRecipeType.IMAGE) {
                                return saveImage(dish.getName(), item)
                                        .flatMap(newItem -> dao.insert(new DishRecipe(dish.getId(), newItem))
                                                .flatMap(id -> Single.just(id > 0)));
                            }
                            else return dao.insert(new DishRecipe(dish.getId(), item))
                                    .flatMap(id -> Single.just(id > 0));
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
            } else return Single.just(false);
        }

        /**
         * Отримує всі рецепти з бази даних.
         * @return Single<List<DishRecipe>> Список усіх рецептів
         */
        @Override
        public Single<List<DishRecipe>> getAll() {
            return dao.getAll();
        }

        /**
         * Отримує рецепт за ID.
         * @param id ID рецепту
         * @return Single<DishRecipe> Об'єкт рецепту
         */
        @Override
        public Single<DishRecipe> getByID(long id) {
            return dao.getByID(id);
        }

        /**
         * Отримує рецепти для конкретної страви.
         * @param id_dish ID страви
         * @return Single<List<DishRecipe>> Список рецептів
         */
        public Single<List<DishRecipe>> getByDishID(long id_dish) {
            return dao.getByDishID(id_dish);
        }

        /**
         * Зберігає зображення рецепту у внутрішньому сховищі.
         * @param dishName Назва страви
         * @param dishRecipe Об'єкт рецепту
         * @return Single<DishRecipe> Рецепт з оновленим шляхом до зображення
         */
        public Single<DishRecipe> saveImage(String dishName, DishRecipe dishRecipe) {
            if (!dishRecipe.getTextData().isEmpty()) {
                return imageController.getBiteArrayImageFromPath(dishRecipe.getTextData())
                        .flatMap(imageController::decodeByteArrayToBitmap)
                        .flatMap(bitmap -> imageController.saveImageToInternalStorage(dishName, bitmap))
                        .flatMap(url -> {
                            if (!url.isEmpty()) dishRecipe.setTextData(url);
                            return Single.just(dishRecipe);
                        });
            } else return Single.just(dishRecipe);
        }

        /**
         * Оновлює інформацію про рецепт.
         * @param item Об'єкт рецепту для оновлення
         * @return Completable Результат операції
         */
        @Override
        public Completable update(DishRecipe item) {
            return dao.update(item);
        }

        /**
         * Видаляє рецепт (з видаленням пов'язаного зображення).
         * @param item Об'єкт рецепту для видалення
         * @return Completable Результат операції
         */
        @Override
        public Completable delete(DishRecipe item) {
            if (item != null && item.getTypeData() == DishRecipeType.IMAGE) imageController.deleteFileByUri(item.getTextData());
            return dao.delete(item);
        }

        /**
         * Видаляє список рецептів (з видаленням пов'язаних зображень).
         * @param items Список рецептів для видалення
         * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
         */
        @Override
        public Single<Boolean> deleteAll(ArrayList<DishRecipe> items) {
            return Observable.fromIterable(items)
                    .flatMapSingle(item -> {
                        if (item != null) {
                            if (item.getTypeData() == DishRecipeType.IMAGE) imageController.deleteFileByUri(item.getTextData());

                            return dao.delete(item)
                                    .andThen(Single.just(true))
                                    .onErrorReturnItem(false);
                        } else return Single.just(false);
                    })
                    .toList()
                    .map(results -> {
                        for (Boolean result : results) {
                            if (!result) {
                                Log.d("RecipeUtils", "Помилка. Щось не видалено");
                                return false;
                            }
                        }

                        Log.d("RecipeUtils", "Всі об'єкти рецептів видалено");
                        return true;
                    })
                    .onErrorReturnItem(false);
        }
    }





    //
    //
    //       Other
    //
    //

    /**
     * Отримує список назв системних колекцій.
     *
     * @return ArrayList<String> Список назв системних колекцій
     */
    public ArrayList<String> getAllNameSystemCollection() {
        String systemTag = context.getString(R.string.system_collection_tag);
        ArrayList<String> names = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
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
    public String getCustomNameSystemCollection(String name) {
        String systemTag = context.getString(R.string.system_collection_tag);

        if (Objects.equals(name, systemTag + "1")) { return context.getString(R.string.favorites); }
        else if (Objects.equals(name, systemTag + "2")) { return context.getString(R.string.my_recipes); }
        else if (Objects.equals(name, systemTag + "3")) { return context.getString(R.string.gpt_recipes); }
        else if (Objects.equals(name, systemTag + "4")) { return context.getString(R.string.import_recipes); }
        else { return name; }
    }
}
