package com.example.recipes.Database.Repositories;

import android.content.Context;
import android.util.Log;

import com.example.recipes.Controller.ImageController;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishRecipeDAO;
import com.example.recipes.Database.ViewModels.DishRecipeViewModel;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
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
 * Клас для роботи з рецептами страв.
 * Цей клас реалізує інтерфейс Utils<DishRecipe>, що дозволяє виконувати CRUD операції над рецептами страв.
 */
public class DishRecipeRepository implements Utils<DishRecipe> {
    private final Context context;
    private final DishRecipeDAO dao;
    private final DishRecipeViewModel viewModel;
    private ImageController imageController;
    private DishRepository dishRepository;

    /**
     * Конструктор класу ByDishRecipe.
     * Ініціалізує DAO та ViewModel для роботи з рецептами.
     */
    public DishRecipeRepository(Context context, DishRecipeDAO dao) {
        this.context = context;
        this.dao = dao;
        this.viewModel = new DishRecipeViewModel(dao);
        this.imageController = new ImageController(context);
    }

    /**
     * Повертає DAO для роботи з рецептами.
     * @return Об'єкт DishRecipeDAO
     */
    public DishRecipeDAO getDao() {
        return dao;
    }

    /**
     * Повертає ViewModel для роботи з рецептами.
     * @return Об'єкт DishRecipeViewModel
     */
    public DishRecipeViewModel getViewModel() {
        return viewModel;
    }

    /**
     * Встановлює залежності для роботи з іншими репозиторіями.
     * @param dishRepository Репозиторій страв
     */
    public void setDependencies(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    /**
     * Встановлює конролер для роботи зі зображеннями.
     */
    public void setImageController(ImageController imageController) {
        this.imageController = imageController;
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
        return Observable.fromIterable(items)
                .flatMapSingle(item -> {
                    if (item.getIdDish() > 0L) {
                        if (item.getTypeData() == DishRecipeType.IMAGE) {
                            return dishRepository.getByID(item.getIdDish())
                                    .flatMap(dish -> {
                                        if (dish != null && dish.getId() > 0 && !dish.getName().isEmpty()) {
                                            return saveImage(dish.getName(), item)
                                                    .flatMap(newItem -> dao.insert(new DishRecipe(dish.getId(), newItem))
                                                            .flatMap(id -> Single.just(id > 0)));
                                        } else return Single.just(false);
                                    });
                        }
                        else return dao.insert(new DishRecipe(item.getIdDish(), item))
                                .flatMap(id -> Single.just(id > 0));
                    } else return Single.just(false);
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
     * Додає список рецептів для конкретної страви.
     * @param dish Об'єкт страви
     * @param items Список рецептів для додавання
     * @return Single<Boolean> Результат операції (true - успішно, false - помилка)
     */
    public Single<Boolean> addAll(Dish dish, ArrayList<DishRecipe> items) {
        if (dish != null && dish.getId() > 0 && !dish.getName().isEmpty()) {
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
        return dao.getByID(id).switchIfEmpty(Single.just(new DishRecipe()));
    }

    /**
     * Отримує рецепти для конкретної страви.
     * @param idDish ID страви
     * @return Single<List<DishRecipe>> Список рецептів
     */
    public Single<List<DishRecipe>> getByDishID(long idDish) {
        return dao.getByDishID(idDish).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    /**
     * Зберігає зображення рецепту у внутрішньому сховищі.
     * @param dishName Назва страви
     * @param dishRecipe Об'єкт рецепту
     * @return Single<DishRecipe> Рецепт з оновленим шляхом до зображення
     */
    public Single<DishRecipe> saveImage(String dishName, DishRecipe dishRecipe) {
        if (!dishRecipe.getTextData().isEmpty() && dishRecipe.getTypeData() == DishRecipeType.IMAGE) {
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
