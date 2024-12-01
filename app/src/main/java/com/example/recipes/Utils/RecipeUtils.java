package com.example.recipes.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Dish_Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import kotlin.Pair;


public class RecipeUtils {
    private Context context;
    private RecipeDatabase database;
    private DishDAO dishDAO;
    private IngredientDAO ingredientDAO;
    private CollectionDAO collectionDAO;
    private DishCollectionDAO dishCollectionDAO;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public RecipeUtils(Context context) {
        this.context = context;
        try {
            database = RecipeDatabase.getInstance(context);
        } catch (Exception e) {
            Log.e("RecipeUtils", "База даних не створилась", e);
        }

        if (database != null) {
            dishDAO = database.dishDao();
            ingredientDAO = database.ingredientDao();
            collectionDAO = database.collectionDao();
            dishCollectionDAO = database.dishCollectionDao();
        }
    }

    public RecipeUtils(RecipeDatabase database) {
        this.database = database;

        if (database != null) {
            dishDAO = database.dishDao();
            ingredientDAO = database.ingredientDao();
            collectionDAO = database.collectionDao();
            dishCollectionDAO = database.dishCollectionDao();
        }
    }



    //
    //
    //       Dish
    //
    //
    public Single<Boolean> addDish (Dish dish, long id_collection) {
        return getUniqueDishName(dish.getName())
                .flatMap(name -> dishDAO.insert(dish))
                .flatMap(id -> {
                    if (id > 0) {
                        Log.d("RecipeUtils", "Страва додалась до бази");
                        return addDishCollectionData(id, id_collection);
                    } else {
                        Log.e("RecipeUtils", "Страва не додалась до бази");
                        return Single.just(false);
                    }
                })
                .doOnSuccess(success -> {
                    if (success) {
                        Log.d("RecipeUtils", "Рецепт успішно додано до колекції.");
                    } else {
                        Log.e("RecipeUtils", "Помилка додавання рецепта в колекцію.");
                    }
                })
                .onErrorReturn(throwable -> {
                    Log.e("RecipeUtils", "Помилка. Рецепт не додано до колекції", throwable);
                    return false;
                });
    }

    public Single<Boolean> addDish (Dish dish, ArrayList<Ingredient> ingredients, long id_collection) {
        return getUniqueDishName(dish.getName())
                .flatMap(name -> dishDAO.insert(new Dish(name, dish.getRecipe())))
                .flatMap(id -> {
                    if (id > 0) {
                        Log.d("RecipeUtils", "Страва додалась до бази");
                        return Single.zip(
                                addIngredients(id, ingredients),
                                addDishCollectionData(id, id_collection),
                                (addIngredientsSuccess, addDishCollectionDataSuccess) -> addIngredientsSuccess && addDishCollectionDataSuccess
                        );
                    } else {
                        Log.e("RecipeUtils", "Страва не додалась до бази");
                        return Single.just(false);
                    }
                })
                .doOnSuccess(success -> {
                    if (success) {
                        Log.d("RecipeUtils", "Рецепт успішно додано до колекції. До рецепта успішно додани інгредієнти");
                    } else {
                        Log.e("RecipeUtils", "Помилка додавання рецепта в колекцію, а інгредієнтів до рецепта.");
                    }
                })
                .onErrorReturn(throwable -> {
                    deleteDish(dish);
                    Log.e("RecipeUtils", "Помилка під час додавання страви", throwable);
                    return false;
                });
    }

    public Single<Boolean> addRecipe(DataBox box, long id_collection) {
        ArrayList<Pair<Dish, ArrayList<Ingredient>>> dataList = box.getBox();

        if (!dataList.isEmpty()) {
            return Observable.fromIterable(dataList)
                    .concatMapSingle(data -> getUniqueDishName(data.getFirst().getName())
                            .flatMap(name -> addDish(new Dish(name, data.getFirst().getRecipe()), id_collection)
                                    .flatMap(status -> {
                                        if (status) {
                                            return getIdDishByName(name)
                                                    .flatMap(
                                                            id -> addIngredients(id, data.getSecond()),
                                                            throwable -> Single.just(false)
                                                    );
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
                    })
                    .doOnSubscribe(compositeDisposable::add)
                    .doFinally(this::clearDisposables);
        } else {
            return Single.just(false);
        }
    }

    public Single<String> getUniqueDishName(String name) {
        return Single.fromCallable(() -> name)
                .flatMap(dishName -> getUniqueDishNameRecursive(dishName, 1));
    }

    private Single<String> getUniqueDishNameRecursive(String dishName, int suffix) {
        return checkDuplicateDishName(dishName)
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        String newDishName = dishName + " №" + suffix;
                        return getUniqueDishNameRecursive(newDishName, suffix + 1);
                    } else {
                        return Single.just(dishName);
                    }
                });
    }

    public Single<Boolean> checkDuplicateDishName(String name) {
        return dishDAO.getIdByName(name)
                .map(id -> id != null)
                .switchIfEmpty(Single.just(false));
    }

    public Single<Dish> getDish(long id) {
        return dishDAO.getDishById(id)
                .toSingle()
                .onErrorResumeNext(throwable -> Single.just(new Dish("", "")));
    }

    public Single<Dish> getDishByName(String name) {
        return dishDAO.getDishByName(name)
                .toSingle()
                .onErrorResumeNext(throwable -> Single.just(new Dish("", "")));
    }

    public Single<ArrayList<Dish>> getDishes(ArrayList<Long> ids) {
        return Observable.fromIterable(ids)
                .flatMapSingle(this::getDish)
                .toList()
                .map(ArrayList::new);
    }

    public Single<Long> getIdDishByName(String name) {
        return dishDAO.getIdByName(name)
                .toSingle()
                .onErrorResumeNext(throwable -> Single.just((long) -1));
    }

    public Single<ArrayList<Collection>> getCollectionsByDish(Dish dish) {
        return dishCollectionDAO.getAllIdsCollectionByIdDish(dish.getId())
                .flatMap(ids_collection -> {
                    ArrayList<Collection> collections = new ArrayList<>();

                    return Observable.fromIterable(ids_collection)
                            .flatMapSingle(this::getCollectionById)
                            .toList()
                            .map(collectionsList -> {
                                collections.addAll(collectionsList);
                                return collections;
                            });
                });
    }

    public Single<List<Dish>> getAllDishes() {
        return dishDAO.getAllDishes();
    }

    public Single<List<Dish>> getDishesOrdered() {
        return dishDAO.getAllDishesOrdered();
    }

    public Single<ArrayList<Dish>> getUnusedDishInCollection(Collection collection) {
        return Single.zip(
                getAllDishes(),
                getDishesByCollection(collection.getId()),
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

    public Single<Integer> getDishCount() {
        return dishDAO.getDishCount();
    }

    public Completable updateDish(Dish newDish){
        return dishDAO.update(newDish);
    }

    public Completable deleteDish(Dish dish) {
        return dishDAO.delete(dish);
    }

    public Completable deleteRecipe(DataBox box) {
        ArrayList<Dish> dish = box.getDishes();
        return dishDAO.delete(dish.get(0));
    }









    //
    //
    //       Ingredient
    //
    //
    public Single<Boolean> addIngredients(long newID, long oldID, ArrayList<Ingredient> ingredients) {
        return Observable.fromIterable(ingredients)
                .flatMapSingle(
                        ing -> {
                            if (oldID == ing.getId_dish()) {
                                return ingredientDAO.insert(new Ingredient(ing.getName(), ing.getAmount(), ing.getType(), newID))
                                        .flatMap(
                                                id -> Single.just(id > 0),
                                                throwable -> Single.just(false)
                                        );
                            } else {
                                return Single.just(true);
                            }
                        }
                )
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) {
                            return false;
                        }
                    }
                    return true;
                })
                .doOnSubscribe(compositeDisposable::add)
                .doFinally(this::clearDisposables);
    }

    public Single<Boolean> addIngredients(long id_dish, ArrayList<Ingredient> ingredients) {
        return Observable.fromIterable(ingredients)
                .flatMapSingle(ing -> ingredientDAO.insert(new Ingredient(ing.getName(), ing.getAmount(), ing.getType(), id_dish))
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

    public Single<Long> getIdIngredient(Ingredient in) {
        return ingredientDAO.getIdByNameAndIdDish(in.getName(), in.getId_dish()).toSingle();
    }

    public Single<List<Ingredient>> getIngredients() {
        return ingredientDAO.getAllIngredients();
    }

    public Single<List<Ingredient>> getIngredients(long id_dish) {
        return ingredientDAO.getAllIngredientsByIdDish(id_dish);
    }

    public Single<List<Ingredient>> getIngredientsOrdered() {
        return ingredientDAO.getAllIngredientsNameOrdered();
    }

    public Single<List<Long>> getDishIdsByNameIngredient(String nameIng) {
        return ingredientDAO.getIdDishesByName(nameIng);
    }

    public Single<Integer> getIngredientCount() {
        return ingredientDAO.getIngredientCount();
    }

    public Completable updateIngredient(Ingredient ingredient) {
        return ingredientDAO.update(ingredient);
    }

    public Completable deleteIngredient(Ingredient ingredient) {
        return ingredientDAO.delete(ingredient);
    }

    public Single<Boolean> deleteIngredient(ArrayList<Ingredient> ingredients) {
        return Observable.fromIterable(ingredients)
                .flatMapSingle(ingredient ->
                            ingredientDAO.delete(ingredient)
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








    //
    //
    //       Collection
    //
    //
    public Single<Boolean> addCollection(Collection collection) {
        return collectionDAO.insert(collection).map(id -> id > 0);
    }

    public Single<Collection> getCollectionById(long id_collection) {
        return collectionDAO.getCollectionById(id_collection)
                .flatMap(collection -> {
                    String customName = getCustomNameSystemCollection(collection.getName());
                    return Maybe.just(new Collection(collection.getId(), customName, collection.getDishes()));
                })
                .flatMap(collection ->
                        getDishesByCollection(collection.getId())
                                .map(dishes -> {
                                    collection.setDishes(new ArrayList<>(dishes));
                                    return collection;
                                })
                                .toMaybe()
                )
                .toSingle()
                .doOnError(throwable -> Log.e("RxError", "Error occurred: ", throwable))
                .onErrorReturnItem(null);
    }



    public Single<Collection> getCollectionByName(String name) {
        return collectionDAO.getCollectionByName(name)
                .flatMap(collection -> {
                    String customName = getCustomNameSystemCollection(collection.getName());
                    return Maybe.just(new Collection(collection.getId(), customName, collection.getDishes()));
                })
                .flatMap(collection ->
                        getDishesByCollection(collection.getId())
                                .map(dishes -> {
                                    collection.setDishes(new ArrayList<>(dishes));
                                    return collection;
                                })
                                .toMaybe()
                )
                .toSingle()
                .doOnError(throwable -> Log.e("RxError", "Error occurred: ", throwable))
                .onErrorReturnItem(null);
    }

    public Single<Long> getIdCollectionByName(String name) {
        return collectionDAO.getIdByName(name)
                .toSingle()
                .onErrorResumeNext(throwable -> Single.just((long) -1));
    }

    public Single<List<Collection>> getAllCollections() {
        return collectionDAO.getAllCollections()
                .flatMap(collections -> Observable.fromIterable(collections)
                        .flatMapSingle(collection ->
                            getDishesByCollection(collection.getId())
                                    .map(dishes -> {
                                        String customName = getCustomNameSystemCollection(collection.getName());
                                        collection.setName(customName);
                                        collection.setDishes(new ArrayList<>(dishes));
                                        return collection;
                                    })
                        )
                        .toList());
    }

    public Single<List<String>> getAllNameCollections() {
        return collectionDAO.getAllNameCollections()
                .map(names -> {
                    for (String name : names) {
                        String customName = getCustomNameSystemCollection(name);
                        names.set(names.indexOf(name), customName);
                    }
                    return names;
                });
    }

    public Single<List<Dish>> getDishesByCollection(long id_collection) {
        return dishCollectionDAO.getAllIdsDishByIdCollection(id_collection)
                .flatMap(ids -> {
                    if (ids == null || ids.isEmpty()) {
                        return Single.just(new ArrayList<>());
                    } else {
                        return Observable.fromIterable(ids)
                                .flatMapSingle(this::getDish)
                                .toList();
                    }
                });
    }

    public Single<ArrayList<Collection>> getUnusedCollectionInDish(Dish dish) {
        return Single.zip(
                getAllCollections(),
                getCollectionsByDish(dish),
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

    public Single<Integer> getCollectionCount() {
        return collectionDAO.getCollectionCount();
    }

    public Completable updateCollection(Collection collection) {
        return collectionDAO.update(collection);
    }

    public Completable deleteCollection(Collection collection, boolean mode) {
        if (mode) {
            return dishCollectionDAO.getAllIdsDishByIdCollection(collection.getId())
                    .flatMapCompletable(ids ->
                            Observable.fromIterable(ids)
                                    .flatMapSingle(id -> dishDAO.getDishById(id)
                                            .defaultIfEmpty(new Dish(0, "", ""))
                                    )
                                    .filter(dish -> dish.getId() > 0)
                                    .flatMapCompletable(dish -> dishDAO.delete(dish))
                    )
                    .andThen(collectionDAO.delete(collection));
        } else {
            return collectionDAO.delete(collection);
        }
    }












    //
    //
    //       Dish_Collection
    //
    //
    public Single<Boolean> addDishCollectionData(long id_dish, long id_collection) {
        return isDishInCollection(id_dish, id_collection)
                .flatMap(isInCollection -> {
                    if (!isInCollection) {
                        return addDishToCollection(id_dish, id_collection);
                    } else {
                        return getCollectionById(id_collection)
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

    public Single<Boolean> addDishCollectionData(Dish dish, ArrayList<Long> id_collections) {
        return Observable.fromIterable(id_collections)
                .flatMapSingle(id_collection -> isDishInCollection(dish.getId(), id_collection)
                        .concatMap(isInCollection -> {
                            if (!isInCollection) {
                                return addDishToCollection(dish.getId(), id_collection);
                            } else {
                                return getCollectionById(id_collection)
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
                })
                .doOnSubscribe(compositeDisposable::add)
                .doFinally(this::clearDisposables);
    }

    public Single<Boolean> addDishCollectionData(ArrayList<Dish> dishes, long id_collection) {
        return Observable.fromIterable(dishes)
                .flatMapSingle(dish -> isDishInCollection(dish.getId(), id_collection)
                        .concatMap(isInCollection -> {
                            if (!isInCollection) {
                                return addDishToCollection(dish.getId(), id_collection);
                            } else {
                                return getCollectionById(id_collection)
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
                })
                .doOnSubscribe(compositeDisposable::add)
                .doFinally(this::clearDisposables);
    }

    public Single<Boolean> isDishInCollection(long dishId, long id_collection) {
        return dishCollectionDAO.getDishCollectionsByIdDishAndIdCollection(dishId, id_collection)
                .map(dishCollection -> {
                    Log.d("isDishInCollection", "Страва з айді " + dishId + " знайдена в колекції");
                    return true;
                })
                .defaultIfEmpty(false)
                .doOnSuccess(result -> Log.d("isDishInCollection", "Result: " + result))
                .doOnError(throwable -> Log.e("isDishInCollection", "Error: " + throwable.getMessage()));
    }


    public Single<Boolean> addDishToCollection(long dishId, long id_collection) {
        return dishCollectionDAO.insert(new Dish_Collection(dishId, id_collection)).map(id -> id > 0);
    }

    public Single<Boolean> copyDishesToAnotherCollections(long id_collection_origin, ArrayList<Long> id_collections) {
        return getDishesByCollection(id_collection_origin)
                .flatMap(dishes -> {
                    if (dishes.isEmpty()) {
                        return Single.just(false);
                    }
                    return Observable.fromIterable(id_collections)
                            .flatMapSingle(id_collection ->
                                    Observable.fromIterable(dishes)
                                            .flatMapSingle(dish ->
                                                    checkDuplicateDishCollectionData(dish.getId(), id_collection)
                                                            .flatMap(isDuplicate -> {
                                                                if (!isDuplicate) {
                                                                    return addDishCollectionData(dish.getId(), id_collection);
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
                })
                .doOnSubscribe(compositeDisposable::add)
                .doFinally(this::clearDisposables);
    }

    public Single<Integer> getDishCollectionCount() {
        return dishCollectionDAO.getDishCollectionCount();
    }

    public Single<Boolean> checkDuplicateDishCollectionData (long id_dish, long id_collection) {
        return dishCollectionDAO.getDishCollectionsByIdDishAndIdCollection(id_dish, id_collection)
                .map(dishCollection -> true)
                .defaultIfEmpty(false);
    }

    public Completable updateDishCollection(Dish_Collection dish_collection) {
        return dishCollectionDAO.update(dish_collection);
    }

    public Completable deleteDishCollection(Dish_Collection dish_collection) {
        return dishCollectionDAO.delete(dish_collection);
    }

    public Completable deleteDishCollectionData(Collection collection) {
        return dishCollectionDAO.getAllIdsDishByIdCollection(collection.getId())
                .flatMapCompletable(ids ->
                        Observable.fromIterable(ids)
                                .flatMapMaybe(id -> dishCollectionDAO.getDishCollectionsByIdDishAndIdCollection(id, collection.getId()))
                                .flatMapCompletable(dishCollection -> dishCollectionDAO.delete(dishCollection))
                );
    }









    //
    //
    //       Other
    //
    //
    public ArrayList<String> getAllNameSystemCollection() {
        String systemTag = context.getString(R.string.system_collection_tag);
        ArrayList<String> names = new ArrayList<>();
        names.add(systemTag + "1");
        names.add(systemTag + "2");
        names.add(systemTag + "3");
        names.add(systemTag + "4");

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

    private ArrayList<String> getCustomNameSystemCollection(ArrayList<String> names) {
        ArrayList<String> customNames = new ArrayList<>();

        for (String name : names) {
            customNames.add(getCustomNameSystemCollection(name));
        }

        return customNames;
    }

    private String setCustomNameSystemCollection(String name) {
        String systemTag = context.getString(R.string.system_collection_tag);

        if (Objects.equals(name, context.getString(R.string.favorites))) { return systemTag + "1"; }
        else if (Objects.equals(name, context.getString(R.string.my_recipes))) { return systemTag + "2"; }
        else if (Objects.equals(name, context.getString(R.string.gpt_recipes))) { return systemTag + "3"; }
        else if (Objects.equals(name, context.getString(R.string.import_recipes))) { return systemTag + "4"; }
        else { return name; }
    }

    public String getNameIngredientType(int id) {
        PerferencesController controller = new PerferencesController();
        controller.loadPreferences(context);
        String[] types = controller.getStringArrayForLocale(R.array.options_array, controller.language);
        return types[id];
    }

    private void clearDisposables() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }
}
