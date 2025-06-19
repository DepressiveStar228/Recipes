package com.example.recipes.Item.Option;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Item.Collection;
import com.example.recipes.R;
import com.example.recipes.Utils.ClassUtils;
import com.example.recipes.Utils.Dialogues;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CollectionOptions {
    private final Activity activity;
    private final RecipeUtils utils;
    private final String nameActivity;
    private final Dialogues dialogues;
    private final ImportExportController importExportController;
    private final CompositeDisposable compositeDisposable;

    public CollectionOptions(@NonNull Activity activity, @NonNull CompositeDisposable compositeDisposable) {
        this.activity = activity;
        this.compositeDisposable = compositeDisposable;
        utils = RecipeUtils.getInstance(activity);
        importExportController = new ImportExportController(activity);
        dialogues = new Dialogues(activity);
        nameActivity = activity.getClass().getSimpleName();
    }



    /**
     * Відкриває діалог для додавання нових страв до колекції.
     * Показує список усіх страв, які ще не входять до цієї колекції.
     */
    public void addToCollection(Collection collection) {
        Disposable disposable = utils.ByCollection().getUnusedDish(collection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused_dishes -> {   // Отримаємо страви, яких немає у вибраній коллекції
                            if (dialogues != null) {
                                dialogues.dialogChooseItemsWithSearch(unused_dishes,
                                        selectedDishes -> {
                                            if (!selectedDishes.isEmpty()) {
                                                Disposable disposable1 = utils.ByDishCollection().addAll(selectedDishes, collection)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(status -> {
                                                                    if (status) {
                                                                        Toast.makeText(activity, activity.getString(R.string.successful_add_dishes), Toast.LENGTH_SHORT).show();
                                                                        Log.d(nameActivity, "Страви успішно додано до колекції (й)");
                                                                    }
                                                                },
                                                                throwable -> {
                                                                    Log.d(nameActivity, "Помилка додавання страв до колекції(й)");
                                                                }
                                                        );
                                                compositeDisposable.add(disposable1);
                                            }
                                        }, () -> { },
                                        Limits.MAX_CHAR_NAME_DISH, R.string.your_dish, R.string.button_copy);
                            }
                        },
                        throwable -> {
                            Log.d(nameActivity, "Помилка отримання страв, які не лежать в колекції");
                        }
                );

        compositeDisposable.add(disposable);
    }

    /**
     * Відкриває діалог для копіювання усіх страв з колекції в інші колекції.
     * Дозволяє обрати декілька колекцій одночасно.
     */
    public void copyToCollection(Collection collection) {
        Disposable disposable = utils.ByCollection().getAllByType(CollectionType.COLLECTION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listData -> {
                            if (ClassUtils.isListOfType(listData, Collection.class)) {
                                ArrayList<Collection> allCollections = ClassUtils.getListOfType(listData, Collection.class);
                                allCollections.remove(collection);

                                for (Collection col : allCollections) {
                                    col.setName(utils.ByCollection().getCustomNameSystemCollectionByName(col.getName()));
                                }

                                if (dialogues != null) {
                                    dialogues.dialogChooseItems(allCollections,
                                            selectedCollections -> {
                                                if (!selectedCollections.isEmpty()) {
                                                    Disposable disposable1 = utils.ByDishCollection().copyDishesToAnotherCollections(collection, selectedCollections)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(status -> {
                                                                        if (status) {
                                                                            Toast.makeText(activity, activity.getString(R.string.successful_copy_dishes), Toast.LENGTH_SHORT).show();
                                                                            Log.d(nameActivity, "Страви успішно скопійовано до колекції(й)");
                                                                        }
                                                                    },
                                                                    throwable -> {
                                                                        Log.d(nameActivity, "Помилка копіювання страв до колекції(й)");
                                                                    }
                                                            );

                                                    compositeDisposable.add(disposable1);
                                                }
                                            }, R.string.collections_dish_text, R.string.button_copy);
                                }
                            }
                        },
                        throwable -> {
                            Log.d(nameActivity, "Помилка отримання усіх колекцій");
                        }
                );

        compositeDisposable.add(disposable);
    }

    /**
     * Відкриває діалог для редагування назви колекції.
     * Перевіряє унікальність нової назви перед збереженням.
     */
    public void editNameCollection(Collection collection) {
        if (dialogues != null) {
            dialogues.dialogSetStringParamCollection(collection.getName(), Limits.MAX_CHAR_NAME_COLLECTION, collectionName -> {
                if (collectionName.isEmpty()) {
                    Toast.makeText(activity, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                }

                Disposable disposable = utils.ByCollection().getIDByName(collectionName)
                        .flatMap(
                                id -> {
                                    if (id != -1) {
                                        Toast.makeText(activity, R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                        return Single.error(new Exception(activity.getString(R.string.warning_dublicate_name_collection)));
                                    } else {
                                        collection.setName(collectionName);
                                        return utils.ByCollection().update(collection).toSingleDefault(collection);
                                    }
                                },
                                throwable -> {
                                    Log.d(nameActivity, "Помилка виконання запиту отримання айді колекції за ім'ям");
                                    return Single.error(new Exception(activity.getString(R.string.warning_dublicate_name_collection)));
                                }
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                updatedCollection -> Toast.makeText(activity, R.string.successful_edit_collection, Toast.LENGTH_SHORT).show(),
                                throwable -> {
                                    if (throwable.getMessage() != null && throwable.getMessage().equals(activity.getString(R.string.warning_dublicate_name_collection))) {
                                        Toast.makeText(activity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(activity, R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );

                compositeDisposable.add(disposable);
            }, R.string.edit_collection, R.string.edit);
        }
    }

    /**
     * Експортує усі страви з колекції у файл та відкриває вікно для надсилання файлу.
     * Спершу показує повідомлення з попередженням про експорт.
     */
    public void shareCollection(Collection collection) {
        if (!collection.getDishes().isEmpty()) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.confirm_export))
                    .setMessage(activity.getString(R.string.warning_export))
                    .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                        importExportController.exportRecipeData(activity, collection, new ExportCallbackUri() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    FileUtils.sendFileByUri(activity, uri);
                                    FileUtils.deleteFileByUri(activity, uri);
                                    Toast.makeText(activity, activity.getString(R.string.successful_export) + uri, Toast.LENGTH_LONG).show();
                                    Log.d(nameActivity, "Рецепти успішно експортовані");
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Toast.makeText(activity, activity.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                Log.e(nameActivity, "Помилка експорту", throwable);
                            }

                            @Override
                            public void getDisposable(Disposable disposable) {
                                compositeDisposable.add(disposable);
                            }
                        });
                    })
                    .setNegativeButton(activity.getString(R.string.no), null).show();
        } else {
            Toast.makeText(activity, R.string.error_empty_collection, Toast.LENGTH_SHORT).show();
            Log.d(nameActivity, "Помилка. Колекція порожня");
        }
    }

    /**
     * Видаляє усі страви з колекції, але зберігає саму колекцію.
     * Показує діалог підтвердження перед видаленням.
     */
    public void clearCollection(Collection collection) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirm_clear_collection))
                .setMessage(activity.getString(R.string.warning_clear_collection))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDishCollection().deleteAllByIDCollection(collection.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    status -> {
                                        Toast.makeText(activity, R.string.successful_clear_collerction, Toast.LENGTH_SHORT).show();
                                        Log.d(nameActivity, "Колекція успішно очищена");
                                    },
                                    throwable -> Log.e(nameActivity, "Помилка очищення колекції")
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(activity.getString(R.string.no), null).show();
    }

    /**
     * Видаляє колекцію, але зберігає страви, що були в ній.
     * Показує діалог підтвердження перед видаленням.
     */
    public void deleteCollectionOnly(Collection collection) {
        deleteCollectionOnly(collection, null);
    }

    public void deleteCollectionOnly(Collection collection, Runnable callback) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirm_delete_dish))
                .setMessage(activity.getString(R.string.warning_delete_collection))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().deleteWithDishes(collection)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(activity, activity.getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                                        if (callback != null) {
                                            callback.run();
                                        }
                                    },
                                    throwable -> {
                                        Toast.makeText(activity, activity.getString(R.string.error_delete_collection), Toast.LENGTH_SHORT).show();
                                        Log.e(nameActivity, "Помилка видалення колекції: " + throwable.getMessage());
                                    });

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(activity.getString(R.string.no), null).show();
    }

    /**
     * Видаляє колекцію разом з усіма стравами, що були в ній.
     * Показує діалог підтвердження перед видаленням.
     */
    public void deleteCollectionWithDish(Collection collection) {
        deleteCollectionWithDish(collection, null);
    }

    public void deleteCollectionWithDish(Collection collection, Runnable callback) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirm_delete_dish))
                .setMessage(activity.getString(R.string.warning_delete_collection_with_dishes))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().deleteWithDishes(collection)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(activity, activity.getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                                        if (callback != null) {
                                            callback.run();
                                        }
                                    },
                                    throwable -> {
                                        Toast.makeText(activity, activity.getString(R.string.error_delete_collection), Toast.LENGTH_SHORT).show();
                                        Log.e(nameActivity, "Помилка видалення колекції: " + throwable.getMessage());
                                    });

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(activity.getString(R.string.no), null).show();
    }
}
