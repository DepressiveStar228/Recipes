package com.example.recipes.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.recipes.Config;
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
import com.example.recipes.Database.ViewModels.CollectionViewModel;
import com.example.recipes.Database.ViewModels.DishCollectionViewModel;
import com.example.recipes.Database.ViewModels.DishRecipeViewModel;
import com.example.recipes.Database.ViewModels.DishViewModel;
import com.example.recipes.Database.ViewModels.IngredientShopListViewModel;
import com.example.recipes.Database.ViewModels.IngredientShopList_AmountTypeViewModel;
import com.example.recipes.Database.ViewModels.IngredientViewModel;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Dish_Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.IngredientShopList_AmountType;
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


public class RecipeUtils {
    private Context context;
    private RecipeDatabase database;

    public RecipeUtils(Context context) {
        this.context = context;
        try {
            database = RecipeDatabase.getInstance(context);
        } catch (Exception e) {
            Log.e("RecipeUtils", "База даних не створилась", e);
        }
    }

    public RecipeUtils(RecipeDatabase database) {
        this.database = database;
    }



    //
    //
    //       Dish
    //
    //
    public ByDish ByDish() {
        return new ByDish();
    }

    public class ByDish implements Utils<Dish> {
        private final DishDAO dao;
        private final DishViewModel viewModel;

        public ByDish() {
            this.dao = database.dishDao();
            this.viewModel = new DishViewModel(dao);
        }

        public DishViewModel getViewModel() {
            return viewModel;
        }

        @Override
        public Single<Long> add(Dish item) {
            return getUniqueName(item.getName())
                    .flatMap(name -> dao.insert(item))
                    .flatMap(id -> {
                        if (id > 0) {
                            Log.d("RecipeUtils", "Страва додалась до бази");
                            return Single.just(id);
                        } else {
                            Log.e("RecipeUtils", "Страва не додалась до бази");
                            return Single.just(-1L);
                        }
                    });
        }

        public Single<Long> add(Dish item, long id_collection) {
            if (item != null && item.getId() < 0) item.setId(0);

            return getUniqueName(item.getName())
                    .flatMap(name -> dao.insert(item))
                    .flatMap(id -> {
                        if (id > 0) {
                            item.setId(id);
                            return ByDish_Collection().addWithCheck(new Dish_Collection(id, id_collection))
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

        @Override
        public Single<Boolean> addAll(ArrayList<Dish> items) {
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

        @Override
        public Single<List<Dish>> getAll() {
            return dao.getAll();
        }

        public Single<List<Dish>> getAll(ArrayList<Long> ids) {
            return Observable.fromIterable(ids)
                    .flatMapSingle(this::getByID)
                    .toList()
                    .map(ArrayList::new);
        }

        @Override
        public Single<Dish> getByID(long id) {
            return dao.getByID(id)
                    .toSingle()
                    .onErrorResumeNext(throwable -> Single.just(new Dish("")));
        }

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

        public Single<List<Dish>> getFilteredAndSorted(ArrayList<Object> ingredientNames, ArrayList<Boolean> sortStatus) {
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

        public Single<Boolean> checkDuplicateName(String name) {
            return dao.getIDByName(name)
                    .map(id -> id != null)
                    .switchIfEmpty(Single.just(false));
        }

        public Single<Integer> getCount() {
            return dao.getCount();
        }

        @Override
        public Completable update(Dish item) {
            return dao.update(item);
        }

        @Override
        public Completable delete(Dish item) {
            return dao.delete(item);
        }

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
    }

    public Single<Boolean> addRecipe(DataBox box, long id_collection) {
        ArrayList<Pair<Dish, ArrayList<Ingredient>>> dataList = box.getBox();

        if (!dataList.isEmpty()) {
            return Observable.fromIterable(dataList)
                    .concatMapSingle(data -> ByDish().getUniqueName(data.first.getName())
                            .flatMap(name -> ByDish().add(new Dish(name), id_collection)
                                    .flatMap(id -> {
                                        if (id > 0) {
                                            return ByIngredient().addAll(id, data.second);
                                        } else {
                                            return Single.just(false);
                                        }
                                    })))
                    .toList()
                    .map(results -> {
                        for (Boolean result : results) {
                            if (!result) {
                                return false;
                            }
                        }
                        return true;
                    });
        } else {
            return Single.just(false);
        }
    }

    public Completable deleteRecipe(DataBox box) {
        ArrayList<Dish> dish = box.getDishes();
        return ByDish().delete(dish.get(0));
    }









    //
    //
    //       Ingredient
    //
    //
    public ByIngredient ByIngredient() {
        return new ByIngredient();
    }

    public class ByIngredient implements Utils<Ingredient> {
        private final IngredientDAO dao;
        private final IngredientViewModel viewModel;

        public ByIngredient() {
            this.dao = database.ingredientDao();
            this.viewModel = new IngredientViewModel(dao);
        }

        public IngredientViewModel getViewModel() {
            return viewModel;
        }

        @Override
        public Single<Long> add(Ingredient item) {
            return dao.insert(item);
        }

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

        public Single<Boolean> addAll(Long id_dish, ArrayList<Ingredient> items) {
            return Observable.fromIterable(items)
                    .flatMapSingle(ing -> dao.insert(new Ingredient(removeEndsVoidInName(ing), ing.getAmount(), ing.getType(), id_dish))
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

        @Override
        public Single<List<Ingredient>> getAll() {
            return dao.getAll();
        }

        public Single<List<Ingredient>> getAllByIDDish(Long id_dish) {
            return dao.getAllByIDDish(id_dish);
        }

        @Override
        public Single<Ingredient> getByID(long id) {
            return dao.getByID(id).switchIfEmpty(Single.just(new Ingredient("", "", "")));
        }

        public Single<List<String>> getNamesUnique() {
            return dao.getNamesUnique();
        }

        @Override
        public Completable update(Ingredient item) {
            return dao.update(item);
        }

        @Override
        public Completable delete(Ingredient item) {
            return dao.delete(item);
        }

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
    public ByCollection ByCollection() {
        return new ByCollection();
    }

    public class ByCollection implements Utils<Collection> {
        private final CollectionDAO dao;
        private final CollectionViewModel viewModel;

        public ByCollection() {
            dao = database.collectionDao();
            viewModel = new CollectionViewModel(dao);
        }

        public CollectionViewModel getViewModel() {
            return viewModel;
        }

        @Override
        public Single<Long> add(Collection item) {
            return dao.insert(item);
        }

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

        public Single<List<String>> getAllNames() {
            return dao.getAllName()
                    .map(names -> {
                        for (String name : names) {
                            String customName = getCustomNameSystemCollection(name);
                            names.set(names.indexOf(name), customName);
                        }
                        return names;
                    });
        }

        public Single<List<String>> getAllNamesByType(String type) {
            return dao.getAllNameByType(type)
                    .map(names -> {
                        for (String name : names) {
                            String customName = getCustomNameSystemCollection(name);
                            names.set(names.indexOf(name), customName);
                        }
                        return names;
                    });
        }

        public Single<List<Collection>> getAllByType(String type) {
            return dao.getAllByType(type)
                    .flatMap(collections -> Observable.fromIterable(collections)
                            .flatMapSingle(collection -> {
                                String customName = getCustomNameSystemCollection(collection.getName());
                                collection.setName(customName);
                                return Single.just(collection);
                            })
                            .flatMapSingle(this::getDataForCollection)
                            .toList());
        }

        @Override
        public Single<Collection> getByID(long id) {
            return dao.getById(id)
                    .flatMap(collection -> {
                        String customName = getCustomNameSystemCollection(collection.getName());
                        return Single.just(new Collection(collection.getId(), customName, Config.COLLECTION_TYPE, collection.getDishes()));
                    })
                    .flatMap(this::getDataForCollection)
                    .doOnError(throwable -> Log.e("RxError", "Error occurred: ", throwable))
                    .onErrorResumeNext(throwable -> {
                        Log.e("RxError", "Возврат пустой коллекции из-за ошибки", throwable);
                        return Single.just(new Collection(-1, "Unknown Collection",  Config.COLLECTION_TYPE, new ArrayList<>()));
                    });
        }

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
                        return Single.just(new Collection(-1, "Unknown Collection", Config.COLLECTION_TYPE, new ArrayList<>()));
                    });
        }

        public Single<Long> getIdByName(String name) {
            return dao.getIdByName(name)
                    .toSingle()
                    .onErrorResumeNext(throwable -> Single.just((long) -1));
        }

        public Single<Long> getIdByNameAndType(String name, String type) {
            return dao.getIdByNameAndType(name, type)
                    .toSingle()
                    .onErrorResumeNext(throwable -> Single.just((long) -1));
        }

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

        public Single<String> getUniqueShopListName(String name) {
            return Single.fromCallable(() -> name)
                    .flatMap(name_ -> getUniqueShopListNameRecursive(name_, 1));
        }

        private Single<String> getUniqueShopListNameRecursive(String name, int suffix) {
            return checkDuplicateShopListName(name)
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
                            return getUniqueShopListNameRecursive(newDishName, suffix + 1);
                        } else {
                            return Single.just(name);
                        }
                    });
        }

        public Single<Boolean> checkDuplicateShopListName(String name) {
            return dao.getIdByName(name)
                    .map(id -> id != null)
                    .switchIfEmpty(Single.just(false));
        }

        public Single<String> generateUniqueNameForShopList() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String baseName  = "(" + dateFormat.format(new Date()) + ")";
            return getUniqueShopListName(baseName);
        }

        private Single<Collection> getDataForCollection(Collection item) {
            return getDishes(item.getId())
                    .flatMap(dishes -> {
                        item.setDishes(new ArrayList<>(dishes));
                        return Single.just(item);
                    })
                    .flatMap(collection ->
                    ByIngredientShopList().getAllByIDCollection(collection.getId())
                            .flatMap(ingredientShopLists -> {
                                collection.setIngredients(new ArrayList<>(ingredientShopLists));
                                return Single.just(collection);
                            })
            );
        }

        @Override
        public Completable update(Collection item) {
            return dao.update(item);
        }

        public Single<Collection> updateAndGet(Collection collection) {
            return dao.update(collection)
                    .toSingleDefault(new android.util.Pair<>(true, collection.getName()))
                    .onErrorReturnItem(new android.util.Pair<>(false, ""))
                    .flatMap(pair -> {
                        if (pair.first) {
                            return ByCollection().getByName(pair.second);
                        } else {
                            return Single.just(new Collection("", ""));
                        }
                    });
        }

        public Single<Boolean> clear(Collection item) {
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

        @Override
        public Completable delete(Collection item) {
            return dao.delete(item);
        }

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
    }









    public ByDish_Collection ByDish_Collection() {
        return new ByDish_Collection();
    }

    public class ByDish_Collection implements Utils<Dish_Collection> {
        private final DishCollectionDAO dao;
        private final DishCollectionViewModel viewModel;

        public ByDish_Collection() {
            this.dao = database.dishCollectionDao();
            this.viewModel = new DishCollectionViewModel(dao);
        }

        public DishCollectionViewModel getViewModel() {
            return viewModel;
        }

        @Override
        public Single<Long> add(Dish_Collection item) {
            return dao.insert(item);
        }

        public Single<Boolean> addWithCheck(Dish_Collection item) {
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


        @Override
        public Single<List<Dish_Collection>> getAll() {
            return null;
        }

        @Override
        public Single<Dish_Collection> getByID(long id) {
            return dao.getByID(id)
                    .toSingle()
                    .onErrorReturnItem(new Dish_Collection(0, 0));
        }

        public Single<Dish_Collection> getByData(long id_dish, long id_collection) {
            return dao.getByIDDishAndIDCollection(id_dish, id_collection)
                    .toSingle()
                    .onErrorReturnItem(new Dish_Collection(0, 0));
        }

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
                                                                        return ByDish_Collection().addWithCheck(new Dish_Collection(dish.getId(), id_collection));
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

        public Single<Boolean> checkDuplicateData(long id_dish, long id_collection) {
            return dao.getByIDDishAndIDCollection(id_dish, id_collection)
                    .map(dishCollection -> true)
                    .defaultIfEmpty(false);
        }

        @Override
        public Completable update(Dish_Collection item) {
            return null;
        }

        @Override
        public Completable delete(Dish_Collection item) {
            return dao.delete(item);
        }

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







    public ByIngredientShopList ByIngredientShopList() {
        return new ByIngredientShopList();
    }

    public class ByIngredientShopList implements Utils<IngredientShopList> {
        private final IngredientShopListDAO dao;
        private final IngredientShopListViewModel viewModel;

        public ByIngredientShopList() {
            this.dao = database.ingredientShopListDao();
            this.viewModel = new IngredientShopListViewModel(dao);
        }

        public IngredientShopListViewModel getViewModel() {
            return viewModel;
        }

        @Override
        public Single<Long> add(@NonNull IngredientShopList item) {
            return dao.insert(item);
        }

        public Single<Long> add(@NonNull IngredientShopList item, Long id_dish) {
            Map<String, ArrayList<String>> amountType = item.getGroupedAmountType();

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

        public <T> Single<Boolean> addAll(@NonNull long id_collection, @NonNull ArrayList<T> items) {
            return Observable.fromIterable(items)
                    .flatMapSingle(ing -> {
                        if (ing instanceof IngredientShopList) {
                            IngredientShopList ingSH = (IngredientShopList) ing;
                            return add(
                                    new IngredientShopList(
                                            removeEndsVoidInName(ingSH),
                                            id_collection,
                                            ingSH.getIsBuy()
                                    )).flatMap(id -> Single.just(id > 0));
                        } else if (ing instanceof Ingredient) {
                            Ingredient ingN = (Ingredient) ing;
                            return ByDish().getByID(ingN.getId_dish())
                                    .flatMap(dish -> add(
                                            new IngredientShopList(
                                                    removeEndsVoidInName(ingN),
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

        public Single<List<IngredientShopList>> getAllByBlackList() {
            return dao.getAllByIDCollection(Config.ID_BLACK_LIST);
        }

        public Single<List<String>> getAllNamesByBlackList() {
            return dao.getAllByIDCollection(Config.ID_BLACK_LIST)
                    .flatMap(ingredientShopLists -> Observable.fromIterable(ingredientShopLists)
                            .flatMapSingle(ingredientShopList -> Single.just(ingredientShopList.getName()))
                            .toList()
                    );
        }

        public Single<List<Ingredient>> filteredBlackList(@NonNull ArrayList<Ingredient> ingredientShopLists) {
            return getAllNamesByBlackList()
                    .flatMap(blackListIngredient -> {
                        List<Ingredient> result_ingredients = new ArrayList<>();

                        for (Ingredient ing : ingredientShopLists) {
                            if (!blackListIngredient.contains(ing.getName())) {
                                result_ingredients.add(ing);
                            }
                        }

                        return Single.just(result_ingredients);
                    });
        }

        public Single<List<IngredientShopList>> convertIngredientsToIngredientsShopList(@NonNull ArrayList<Ingredient> ingredients) {
            return Observable.fromIterable(ingredients)
                    .flatMapSingle(ingredient -> Single.just(new IngredientShopList(ingredient)))
                    .collect(Collectors.toList());
        }

        public Single<ArrayList<IngredientShopList>> groupIngredients(@NonNull List<IngredientShopList> ingredientShopLists, @NonNull Collection collection) {
            Map<String, Map<String, ArrayList<String>>> groupedIngredients = new HashMap<>();

            for (IngredientShopList ingredient : ingredientShopLists) {
                groupedIngredients.putIfAbsent(ingredient.getName(), new HashMap<>());
                Map<String, ArrayList<String>> innerMap = groupedIngredients.get(ingredient.getName());
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

        @Override
        public Single<IngredientShopList> getByID(@NonNull long id) {
            return dao.getById(id)
                    .flatMap(ingredient -> {
                        if (ingredient != null) { return getDataFromIngredient(ingredient); }
                        else { return Single.just(new IngredientShopList()); }
                    })
                    .onErrorReturnItem(new IngredientShopList());
        }

        public Single<IngredientShopList> getByNameAndIDCollection(@NonNull String name, @NonNull Long id_collection) {
            return dao.getByNameAndIDCollection(name, id_collection)
                    .switchIfEmpty(Single.just(new IngredientShopList()))
                    .flatMap(this::getDataFromIngredient)
                    .onErrorResumeNext(error -> {
                        Log.e("RxCritical", "Критическая ошибка в БД!", error);
                        return Single.error(error);
                    });
        }

        public Single<IngredientShopList> getDataFromIngredient(IngredientShopList ingredientShopList) {
            return ByIngredientShopList_AmountType().getByIDIngredient(ingredientShopList.getId())
                    .flatMap(amountTypes -> {
                        for (IngredientShopList_AmountType amountType : amountTypes) {
                            ingredientShopList.addAmountType(amountType);
                        }
                        return Single.just(ingredientShopList);
                    });
        }

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

        @Override
        public Completable update(@NonNull IngredientShopList ingredient) {
            return dao.update(ingredient);
        }

        @Override
        public Completable delete(@NonNull IngredientShopList ingredient) {
            return dao.delete(ingredient);
        }

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
    public ByIngredientShopList_AmountType ByIngredientShopList_AmountType() {
        return new ByIngredientShopList_AmountType();
    }

    public class ByIngredientShopList_AmountType implements Utils<IngredientShopList_AmountType> {
        private final IngredientShopList_AmountTypeDAO dao;
        private final IngredientShopList_AmountTypeViewModel viewModel;

        public ByIngredientShopList_AmountType() {
            dao = database.ingredientShopList_amountTypeDao();
            viewModel = new IngredientShopList_AmountTypeViewModel(dao);
        }

        public IngredientShopList_AmountTypeViewModel getViewModel() {
            return viewModel;
        }

        @Override
        public Single<Long> add(IngredientShopList_AmountType ingredient) {
            return dao.insert(ingredient);
        }

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

        @Override
        public Single<List<IngredientShopList_AmountType>> getAll() {
            return dao.getAll();
        }

        @Override
        public Single<IngredientShopList_AmountType> getByID(long id) {
            return dao.getByID(id);
        }


        public Single<List<IngredientShopList_AmountType>> getByIDIngredient(long id_ingredient) {
            return dao.getByIDIngredient(id_ingredient)
                    .switchIfEmpty(Single.just(new ArrayList<>()));
        }

        public Single<List<IngredientShopList_AmountType>> getByIDDish(long id_dish) {
            return dao.getByIDDish(id_dish);
        }

        @Override
        public Completable update(IngredientShopList_AmountType ingredient) {
            return dao.update(ingredient);
        }

        @Override
        public Completable delete(IngredientShopList_AmountType ingredient) {
            return dao.delete(ingredient);
        }

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
    public ByDishRecipe ByDishRecipe() {
        return new ByDishRecipe();
    }

    public class ByDishRecipe implements Utils<DishRecipe> {
        private final DishRecipeDAO dao;
        private final DishRecipeViewModel viewModel;
        private final ImageController imageController = new ImageController(context);

        public ByDishRecipe() {
            dao = database.dishRecipeDao();
            viewModel = new DishRecipeViewModel(dao);
        }

        public DishRecipeViewModel getViewModel() {
            return viewModel;
        }


        @Override
        public Single<Long> add(DishRecipe item) {
            return dao.insert(item);
        }

        @Override
        public Single<Boolean> addAll(ArrayList<DishRecipe> items) {
            return addAll(null, items);
        }

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

        @Override
        public Single<List<DishRecipe>> getAll() {
            return dao.getAll();
        }

        @Override
        public Single<DishRecipe> getByID(long id) {
            return dao.getByID(id);
        }

        public Single<List<DishRecipe>> getByDishID(long id_dish) {
            return dao.getByDishID(id_dish);
        }

        public Single<DishRecipe> saveImage(String dishName, DishRecipe dishRecipe) {
            return imageController.saveImageToInternalStorage(dishName, dishRecipe.getBitmap())
                    .flatMap(url -> {
                        if (!url.isEmpty()) dishRecipe.setTextData(url);
                        return Single.just(dishRecipe);
                    });
        }

        @Override
        public Completable update(DishRecipe item) {
            return dao.update(item);
        }

        @Override
        public Completable delete(DishRecipe item) {
            if (item != null && item.getTypeData() == DishRecipeType.IMAGE) imageController.deleteFileByUri(item.getTextData());
            return dao.delete(item);
        }

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
    public ArrayList<String> getAllNameSystemCollection() {
        String systemTag = context.getString(R.string.system_collection_tag);
        ArrayList<String> names = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            names.add(systemTag + i);
        }
        return names;
    }

    public String getCustomNameSystemCollection(String name) {
        String systemTag = context.getString(R.string.system_collection_tag);

        if (Objects.equals(name, systemTag + "1")) { return context.getString(R.string.favorites); }
        else if (Objects.equals(name, systemTag + "2")) { return context.getString(R.string.my_recipes); }
        else if (Objects.equals(name, systemTag + "3")) { return context.getString(R.string.gpt_recipes); }
        else if (Objects.equals(name, systemTag + "4")) { return context.getString(R.string.import_recipes); }
        else { return name; }
    }

    public String getNameIngredientType(int id) {
        PreferencesController controller = new PreferencesController();
        controller.loadPreferences(context);
        String[] types = controller.getStringArrayForLocale(R.array.options_array, controller.getLanguage());
        return types[id];
    }

    private String removeEndsVoidInName(Ingredient in) {
        return in.getName().replaceFirst("\\s+$", "");
    }

    private String removeEndsVoidInName(IngredientShopList in) {
        return in.getName().replaceFirst("\\s+$", "");
    }

    public Single<List<Pair<Dish, ArrayList<Ingredient>>>> getListPairDishIng(List<Dish> dishes) {
        return Observable.fromIterable(dishes)
                .flatMapSingle(dish -> ByIngredient().getAllByIDDish(dish.getId()).map(ingredients -> new Pair<>(dish, new ArrayList<>(ingredients))))
                .toList();
    }
}
