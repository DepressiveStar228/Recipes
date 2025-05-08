package com.example.recipes.Item.Option;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;
import com.example.recipes.Utils.Dialogues;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Utils.RecipeUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DishOptions {
    private Activity activity;
    private RecipeUtils utils;
    private String nameActivity;
    private Dialogues dialogues;
    private ImportExportController importExportController;
    private CompositeDisposable compositeDisposable;

    public DishOptions(@NonNull Activity activity, @NonNull CompositeDisposable compositeDisposable) {
        this.activity = activity;
        this.compositeDisposable = compositeDisposable;
        utils = RecipeUtils.getInstance(activity);
        importExportController = new ImportExportController(activity);
        dialogues = new Dialogues(activity);
        nameActivity = activity.getClass().getSimpleName();
    }

    /**
     * Відкриває діалог для додавання страви до колекцій.
     * Показує список колекцій, які ще не містять цю страву.
     */
    public void showAddDishInCollectionDialog(@NonNull Dish dish) {
        showAddDishInCollectionDialog(dish, null);
    }

    public void showAddDishInCollectionDialog(@NonNull Dish dish, Runnable callback) {
        Disposable disposable = utils.ByCollection().getUnusedByTypeInDish(dish, CollectionType.COLLECTION)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        unused_collections -> {
                            if (unused_collections != null) {
                                if (dialogues != null) {
                                    dialogues.dialogChooseItems(unused_collections,
                                            selectedCollections -> {
                                                if (!selectedCollections.isEmpty()) {
                                                    Disposable disposable1 = utils.ByDishCollection().addAll(dish, selectedCollections)
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(status -> {
                                                                        if (status) {
                                                                            if (callback != null) {
                                                                                callback.run();
                                                                            }
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
                            } else {
                                Log.e(nameActivity, "Помилка. Список колекцій пустий");
                            }
                        },
                        throwable -> {
                            Toast.makeText(activity, activity.getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                            Log.e(nameActivity, "Помилка отримання списку колекцій з бд", throwable);
                        }
                );
        compositeDisposable.add(disposable);
    }

    /**
     * Переводить інтерфейс у режим редагування страви
     */
    public void editDish(@NonNull Runnable callback) {
        callback.run();
    }


    /**
     * Копіює інформацію про страву у вигляді тексту в буфер обміну
     */
    public void copy_as_text(@NonNull Dish dish) {
        copy_as_text(dish, null);
    }

    public void copy_as_text(@NonNull Dish dish, Runnable callback) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", dish.getAsText(activity));
        clipboard.setPrimaryClip(clip);

        Toast.makeText(activity, activity.getString(R.string.copy_clipboard_text), Toast.LENGTH_SHORT).show();

        if (callback != null) {
            callback.run();
        }
    }

    /**
     * Виконує експорт та пересилання файлу через intent
     */
    public void shareDish(@NonNull Dish dish) {
        shareDish(dish, null, null, null);
    }

    public void shareDish(@NonNull Dish dish, Runnable callback) {
        shareDish(dish, callback, null, null);
    }

    public void shareDish(@NonNull Dish dish, Runnable callback, Runnable callbackSuccessExport, Runnable callbackErrorExport) {
        if (dish != null) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.confirm_export))
                    .setMessage(activity.getString(R.string.warning_export))
                    .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                        if (callback != null) {
                            callback.run();
                        }

                        importExportController.exportDish(activity, dish, new ExportCallbackUri() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    FileUtils.sendFileByUri(activity, uri);
                                    FileUtils.deleteFileByUri(activity, uri);
                                    Log.d(nameActivity, "Рецепт успішно експортовано");
                                } else Log.d(nameActivity, "Посилання на файл експорту є null");

                                if (callbackSuccessExport != null) {
                                    callbackSuccessExport.run();
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                if (callbackErrorExport != null) {
                                    callbackErrorExport.run();
                                }
                                Toast.makeText(activity, activity.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                Log.e(nameActivity, "Помилка при експорті рецептів", throwable);
                            }

                            @Override
                            public void getDisposable(Disposable disposable) {
                                compositeDisposable.add(disposable);
                            }
                        });
                    })
                    .setNegativeButton(activity.getString(R.string.no), null).show();
        } else {
            Toast.makeText(activity, R.string.error_read_get_dish, Toast.LENGTH_SHORT).show();
            Log.d(nameActivity, "Помилка. Страва порожня");
        }
    }

    /**
     * Видаляє страву лише з колекції, але зберігає її в базі даних.
     *
     * @param dish Страва для видалення з колекції
     * @param collection Колекція, з якої буде прибрана страва
     */
    public void removeDish(Dish dish, Collection collection) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirm_remove_dish))
                .setMessage(activity.getString(R.string.warning_remove_dish))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDishCollection().getByData(dish.getId(), collection.getId())
                            .flatMapCompletable(dish_collection -> {
                                if (dish_collection.getIdDish() == 0 || dish_collection.getIdCollection() == 0) {
                                    return Completable.error(new Throwable("Error. Dish_collection was not found"));
                                } else {
                                    return utils.ByDishCollection().delete(dish_collection);
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(activity, R.string.successful_remove_collerction, Toast.LENGTH_SHORT).show();
                                        Log.d("CollectionsDishFragment", "Страва успішно прибрана з колекції");
                                    },
                                    throwable -> {
                                        Log.e("CollectionsDishFragment", "Помилка прибирання страви з колекції", throwable);
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(activity.getString(R.string.no), null).show();
    }


    /**
     * Видаляє страву з бази даних після підтвердження користувачем
     */
    public void deleteDish(@NonNull Dish dish) {
        deleteDish(dish, null);
    }

    public void deleteDish(@NonNull Dish dish, Runnable callback) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirm_delete_dish))
                .setMessage(activity.getString(R.string.warning_delete_dish))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDish().delete(dish)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        if (callback != null) {
                                            callback.run();
                                        }

                                        Toast.makeText(activity, activity.getString(R.string.successful_delete_dish), Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        Toast.makeText(activity, activity.getString(R.string.error_delete_dish), Toast.LENGTH_SHORT).show();
                                        activity.finish();
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(activity.getString(R.string.no), null).show();
    }
}
