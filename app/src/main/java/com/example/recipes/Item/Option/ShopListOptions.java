package com.example.recipes.Item.Option;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.ShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.Dialogues;
import com.example.recipes.Utils.RecipeUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ShopListOptions {
    private Activity activity;
    private RecipeUtils utils;
    private String nameActivity;
    private Dialogues dialogues;
    private CompositeDisposable compositeDisposable;

    public ShopListOptions(@NonNull Activity activity, @NonNull CompositeDisposable compositeDisposable) {
        this.activity = activity;
        this.compositeDisposable = compositeDisposable;
        utils = RecipeUtils.getInstance(activity);
        dialogues = new Dialogues(activity);
        nameActivity = activity.getClass().getSimpleName();
    }

    /**
     * Метод для обробки редагування назви списку покупок.
     * Відкриває діалогове вікно для зміни назви списку.
     */
    public void editName(ShopList shopList, Consumer<ShopList> callbackShopList) {
        if (dialogues != null) {
            dialogues.dialogSetStringParamCollection(shopList.getName(), Limits.MAX_CHAR_NAME_COLLECTION, collectionName -> {
                if (collectionName.isEmpty()) {
                    // Генерація унікальної назви, якщо поле порожнє
                    Disposable disposable = utils.ByCollection().generateUniqueNameForShopList()
                            .flatMap(name -> {
                                shopList.setName(name);
                                shopList.setType(CollectionType.SHOP_LIST);
                                return utils.ByCollection().updateAndGet(shopList);
                            })
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(collection_ -> {
                                if (collection_ != null && collection_.getId() != -1) {
                                    Toast.makeText(activity, R.string.successful_edit_collection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(activity, R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                }
                            });
                    compositeDisposable.add(disposable);
                } else if (!collectionName.equals(shopList.getName())) {
                    // Перевірка на дублікат назви та оновлення назви списку
                    Disposable disposable = utils.ByCollection().getIdByNameAndType(collectionName, CollectionType.SHOP_LIST)
                            .flatMap(
                                    id -> {
                                        if (id != -1) { // Перевірка на дублікат назви списку покупок
                                            Toast.makeText(activity, R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                            return Single.error(new Exception(activity.getString(R.string.warning_dublicate_name_collection)));
                                        } else {
                                            shopList.setName(collectionName);
                                            return utils.ByCollection().update(shopList).toSingleDefault(shopList);
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
                                    updatedCollection -> {
                                        if (callbackShopList != null) callbackShopList.accept(updatedCollection);
                                        Toast.makeText(activity, R.string.successful_edit_collection, Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        if (throwable.getMessage() != null && throwable.getMessage().equals(activity.getString(R.string.warning_dublicate_name_collection))) {
                                            Toast.makeText(activity, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(activity, R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );

                    compositeDisposable.add(disposable);
                }
            }, R.string.edit_shop_list, R.string.edit);
        }
    }

    /**
     * Метод для копіювання списку покупок у вигляді тексту.
     * Копіює текст списку покупок у буфер обміну.
     */
    public void copyAsTest(ShopList shopList) {
        String text = shopList.copyAsText(activity);

        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Метод для обробки очищення списку покупок.
     * Відкриває діалогове вікно для підтвердження очищення.
     */
    public void clear(final ShopList shopList) {
         clear(null, shopList, shopList.getId(), null);
    }

    public void clear(AtomicBoolean flagClear, final ShopList shopList, long shopListID, Consumer<Collection> callback) {
        if (flagClear != null) flagClear.set(true);

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirm_clear_shop_list))
                .setMessage(activity.getString(R.string.warning_delete_shop_list))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().clearShopList(shopList)
                            .flatMap(status -> utils.ByCollection().getByID(shopListID))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(collection -> {
                                if (collection != null) {
                                    if (callback != null) callback.accept(collection);

                                    Toast.makeText(activity, R.string.successful_clear_shop_list, Toast.LENGTH_SHORT).show();
                                    if (flagClear != null) flagClear.set(false);
                                }
                            });
                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(activity.getString(R.string.no), null).show();
    }

    /**
     * Метод для обробки видалення списку покупок.
     * Відкриває діалогове вікно для підтвердження видалення.
     */
    public void delete(ShopList shopList, Runnable callbackPositiveButton) {
        delete(shopList, null, callbackPositiveButton);
    }

    public void delete(ShopList shopList, AtomicBoolean flagAccessDelete, Runnable callbackPositiveButton) {
        if (flagAccessDelete != null) flagAccessDelete.set(true);

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.confirm_delete_dish))
                .setMessage(activity.getString(R.string.warning_delete_shop_list))
                .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().delete(shopList)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> { if (callbackPositiveButton != null) callbackPositiveButton.run(); });

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(activity.getString(R.string.no), null).show();
    }
}
