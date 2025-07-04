package com.example.recipes.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activity.ShopListActivity;
import com.example.recipes.Adapter.ShopListGetAdapter;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.IDSystemCollection;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Option.ShopListOptions;
import com.example.recipes.Item.ShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.Dialogues;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Фрагмент для відображення та управління списками покупок.
 */
public class ShoplistFragment extends Fragment implements OnBackPressedListener {
    private RecipeUtils utils;
    private PreferencesController preferencesController;
    private CompositeDisposable compositeDisposable, compositeByIngredients;
    private TextView empty;
    private ConstraintLayout data_box;
    private RecyclerView shopListsRecyclerView;
    private ImageView addShopListButton, blackListIngredientButton;
    private ShopListGetAdapter adapter;
    private ArrayList<ShopList> shopLists;
    private Disposable shopListDisposable;
    private String nameActivity;
    private ShopListOptions shopListOptions;
    private Dialogues dialogues;
    private final AtomicBoolean accessUpdateShopListsFlag = new AtomicBoolean(true);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesController = PreferencesController.getInstance();
        utils = RecipeUtils.getInstance(getContext());
        compositeDisposable = new CompositeDisposable();
        compositeByIngredients = new CompositeDisposable();
        shopLists = new ArrayList<>();
        nameActivity = ShoplistFragment.class.getName();
        shopListOptions = new ShopListOptions(requireActivity(), compositeDisposable);
        dialogues = new Dialogues(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shoplist_page, container, false);
        loadItemsActivity(view);
        loadClickListeners();
        setAdapterShopList();
        return view;
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        compositeByIngredients.clear();
    }

    /**
     * Завантажує UI елементи фрагмента
     * @param view Кореневий view фрагмента
     */
    private void loadItemsActivity(View view) {
        data_box = view.findViewById(R.id.data_box);
        addShopListButton = view.findViewById(R.id.add_shopListButton);
        blackListIngredientButton = view.findViewById(R.id.blackListButton);

        if (data_box != null) {
            shopListsRecyclerView = data_box.findViewById(R.id.shop_listsRecyclerView);
            empty = data_box.findViewById(R.id.empty_textView);
        }
    }

    /**
     * Налаштовує обробники кліків для кнопок
     */
    private void loadClickListeners() {
        if (addShopListButton != null) addShopListButton.setOnClickListener(v -> showAddShopListDialog());
        if (blackListIngredientButton != null) blackListIngredientButton.setOnClickListener(v -> showBlackListDialog());
    }

    /**
     * Ініціалізує адаптер для списку покупок
     */
    private void setAdapterShopList() {
        if (adapter == null) {
            adapter = new ShopListGetAdapter(
                    new ShopListGetAdapter.CollectionClickListener() {
                        @Override
                        public void onCollectionClick(ShopList collection) {
                            Intent intent = new Intent(getContext(), ShopListActivity.class);
                            intent.putExtra(IntentKeys.SHOP_LIST_ID.name(), collection.getId());
                            startActivity(intent); // Відкриває активність списку покупок
                        }

                        @Override
                        public void onMenuIconClick(ShopList shopList, View v) {
                            PopupMenu popupMenu = new PopupMenu(getContext(), v, Gravity.END);
                            popupMenu.getMenuInflater().inflate(R.menu.context_menu_shop_list, popupMenu.getMenu());
                            String[] themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");

                            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                                MenuItem item = popupMenu.getMenu().getItem(i);
                                SpannableString spannableString = new SpannableString(item.getTitle());

                                if (!Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, spannableString.length(), 0);
                                } else {
                                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.black)), 0, spannableString.length(), 0);
                                }
                                item.setTitle(spannableString);
                            }

                            popupMenu.setOnMenuItemClickListener(item -> {
                                if (item.getItemId() == R.id.action_edit) {
                                    shopListOptions.editName(shopList, updatedCollection -> {
                                        int index = shopLists.indexOf(shopList);
                                        if (index != -1) {
                                            shopLists.set(index, updatedCollection);
                                            adapter.notifyItemChanged(index);
                                        }
                                    });
                                    return true;
                                }
                                else if (item.getItemId() == R.id.action_delete) {
                                    shopListOptions.delete(shopList, () -> Toast.makeText(getContext(), R.string.successful_delete_shop_list, Toast.LENGTH_SHORT).show());
                                    return true;
                                }
                                else if (item.getItemId() == R.id.action_copy_as_text) {
                                    shopListOptions.copyAsTest(shopList);
                                    return true;
                                }
                                else if (item.getItemId() == R.id.action_clear) {
                                    shopListOptions.clear(shopList);
                                    return true;
                                }
                                else { return false; }
                            });

                            popupMenu.show(); // Показує контекстне меню для списку покупок
                        }
                    }
            );

            shopListsRecyclerView.setAdapter(adapter);
            shopListsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            shopListsRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(16));

            // Спостерігач змін кількості списків покупок у БД
            utils.ByCollection().getViewModel().getCountByType(CollectionType.SHOP_LIST).observe(this, data -> {
                if (data != null) {
                    getShopListFromDataBase();
                }
            });

            // Спостерігач змін інгредієнтів списків покупок у БД
            utils.ByIngredientShopList().getViewModel().getAll().observe(this, data -> {
                if (data != null) {
                    getShopListFromDataBase();
                }
            });
        }
    }

    /**
     * Отримує оновлені дані списків покупок з бази даних
     */
    private void getShopListFromDataBase() {
        if (accessUpdateShopListsFlag.get()) { // Якщо оновлення дозволено
            accessUpdateShopListsFlag.set(false); // Блокуємо оновлення, щоб метод не викликався в момент отримання даних

            if (shopListDisposable != null) shopListDisposable.dispose();

            shopListDisposable = utils.ByCollection().getAllByType(CollectionType.SHOP_LIST)
                    .flatMapObservable(collections -> Observable.fromIterable(collections)
                            .flatMapSingle(data -> Single.zip(
                                    utils.ByIngredientShopList().getCountByIdCollection(data instanceof Collection collection ? collection.getId() : 0L),
                                    utils.ByIngredientShopList().getBoughtCountByIdCollection(data instanceof Collection collection ? collection.getId() : 0L),
                                    Pair::new
                            ).flatMap(pair -> {
                                ShopList shopList = new ShopList(data instanceof ShopList collection ? collection : new ShopList(""));

                                if (pair.first != null && pair.second != null) {
                                    shopList.setAllItems(pair.first);
                                    shopList.setAllBoughtItems(pair.second);
                                }

                                return Single.just(shopList);
                            }))
                    )
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(shopLists -> {
                        if (shopLists != null) {
                            this.shopLists.clear();
                            this.shopLists.addAll(shopLists);
                            adapter.setItems(new ArrayList<>(shopLists));

                            if (empty != null) {
                                if (!shopLists.isEmpty()) {
                                    empty.setVisibility(View.GONE);
                                } else {
                                    empty.setVisibility(View.VISIBLE);
                                }
                            }

                            accessUpdateShopListsFlag.set(true); // Розблоковуємо оновлення
                        }
                    });
        }
    }

    /**
     * Відображає діалогове вікно для додавання нового списку покупок.
     */
    private void showAddShopListDialog() {
        if (dialogues != null) {
            dialogues.dialogSetStringParamCollection(Limits.MAX_CHAR_NAME_COLLECTION, collectionName -> {
                if (collectionName.isEmpty()) {
                    Disposable disposable = utils.ByCollection().generateUniqueNameForShopList()
                            .flatMap(name -> utils.ByCollection().add(new Collection(name, CollectionType.SHOP_LIST)))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    id -> {
                                        if (id > 0) {
                                            Toast.makeText(getContext(), R.string.successful_add_collection, Toast.LENGTH_SHORT).show();
                                            Log.d("ShoplistFragment", "Колекція успішно створена");
                                        } else {
                                            Log.e("ShoplistFragment", "Помилка створення колекції");
                                        }
                                    },
                                    throwable -> {
                                        Log.d("ShoplistFragment", "Помилка отримання колекції за ім'ям");
                                    });
                    compositeDisposable.add(disposable);
                } else {
                    Disposable disposable = utils.ByCollection().getIDByName(collectionName)
                            .flatMap(
                                    id -> {
                                        if (id != -1) {
                                            Toast.makeText(getContext(), R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                            return Single.just(null);
                                        } else {
                                            return Single.just(collectionName);
                                        }
                                    },
                                    throwable -> {
                                        Log.d("ShoplistFragment", "Помилка отримання айді колекції за ім'ям");
                                        return Single.just(null);
                                    }
                            )
                            .flatMap(name -> {
                                if (name != null) {
                                    return utils.ByCollection().add(new Collection(name, CollectionType.SHOP_LIST))
                                            .map(id -> new Pair<>((id > 0), name));
                                } else {
                                    return Single.just(new Pair<>(false, name));
                                }
                            })
                            .flatMap(pair -> {
                                if (pair.first) {
                                    Single<Collection> collectionSingle = utils.ByCollection().getByName(pair.second);
                                    if (collectionSingle == null) {
                                        return Single.just(new Collection("", CollectionType.SHOP_LIST, new ArrayList<>()));
                                    } else {
                                        return collectionSingle;
                                    }
                                } else {
                                    return Single.just(new Collection("", CollectionType.SHOP_LIST, new ArrayList<>()));
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(collection -> {
                                        if (collection != null && collection.getId() != -1) {
                                            Toast.makeText(getContext(), R.string.successful_add_collection, Toast.LENGTH_SHORT).show();
                                            Log.d("ShoplistFragment", "Колекція успішно створена");
                                        } else {
                                            Log.e("ShoplistFragment", "Помилка створення колекції");
                                        }
                                    },
                                    throwable -> {
                                        Log.d("ShoplistFragment", "Помилка отримання колекції за ім'ям");
                                    }
                            );

                    compositeDisposable.add(disposable);
                }
            }, R.string.add_shop_list, R.string.button_add);
        }
    }

    /**
     * Відображає діалогове вікно для роботи з чорним списком інгредієнтів.
     * Вибрані інгредієнти не будуть потрапляти до списку покупків
     */
    private void showBlackListDialog() {
        Disposable disposable = Single.zip(
                        utils.ByIngredient().getNamesUnique(),
                        utils.ByIngredientShopList().getAllNamesByBlackList(),
                        Pair::new
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (dialogues != null) {
                        dialogues.dialogChooseItemsWithSearch(new ArrayList<>(data.first), new ArrayList<>(data.second), selectedItems -> {
                            if (selectedItems != null) {
                                Disposable disposable1 = updateBlackList(data.second, selectedItems)
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                status -> {
                                                    if (status) {
                                                        Toast.makeText(getContext(), getContext().getString(R.string.successfully_made_changes), Toast.LENGTH_SHORT).show();
                                                        Log.d(nameActivity, "Інгедієнти успішно додано до чорного списку");
                                                    } else {
                                                        Toast.makeText(getContext(), getContext().getString(R.string.error_add_ingedients), Toast.LENGTH_SHORT).show();
                                                        Log.d(nameActivity, "Помилка додавання інгредиєнтів до чорного списку");
                                                    }
                                                },
                                                throwable -> {
                                                    Toast.makeText(getContext(), getContext().getString(R.string.error_add_ingedients), Toast.LENGTH_SHORT).show();
                                                    Log.d(nameActivity, "Помилка додавання інгредиєнтів до чорного списку");
                                                }
                                        );


                                compositeDisposable.add(disposable1);
                            }
                        }, () -> {
                            Disposable disposable1 = utils.ByIngredientShopList().getAllByBlackList()
                                    .flatMap(ingredientShopLists -> utils.ByIngredientShopList().deleteAll(new ArrayList<>(ingredientShopLists)))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(status -> {
                                        if (status) {
                                            Toast.makeText(getContext(), getContext().getString(R.string.successful_reset), Toast.LENGTH_SHORT).show();
                                            Log.d(nameActivity, "Успішно скинуто інгредієнти blacklist");
                                        } else {
                                            Toast.makeText(getContext(), getContext().getString(R.string.error_reset), Toast.LENGTH_SHORT).show();
                                            Log.d(nameActivity, "Помилка скидання інгредієнтів blacklist");
                                        }
                                    });

                            compositeDisposable.add(disposable1);
                        }, Limits.MAX_CHAR_NAME_INGREDIENT, R.string.ingredient_black_list, R.string.confirm);
                    }
                });

        compositeDisposable.add(disposable);
    }

    /**
     * Оновлює чорний список інгредієнтів, додаючи нові та видаляючи старі елементи.
     *
     * @param oldList Поточний список інгредієнтів у чорному списку
     * @param newList Новий список інгредієнтів для чорного списку
     * @return Single<Boolean> Результат операції оновлення (true - успішно, false - помилка)
     */
    private Single<Boolean> updateBlackList(List<String> oldList, List<String> newList) {
        ArrayList<String> addedItems = new ArrayList<>(newList);
        addedItems.removeAll(oldList);

        ArrayList<String> removedItems = new ArrayList<>(oldList);
        removedItems.removeAll(newList);

        return Single.zip(
                        utils.ByIngredientShopList().addAll(IDSystemCollection.ID_BLACK_LIST.getId(), addedItems),
                        removedIngredients(removedItems),
                        Pair::new
                )
                .flatMap(status -> {
                    if (status.first && status.second) {
                        return Single.just(true);
                    } else {
                        return Single.just(false);
                    }
                });
    }

    /**
     * Видаляє вказані інгредієнти з чорного списку.
     *
     * @param removedItems Список інгредієнтів для видалення
     * @return Single<Boolean> Результат операції (true - всі інгредієнти успішно видалені, false - виникла помилка)
     */
    private Single<Boolean> removedIngredients(ArrayList<String> removedItems) {
        return Observable.fromIterable(removedItems)
                .flatMapSingle(item -> utils.ByIngredientShopList().getByNameAndIDCollection(item, IDSystemCollection.ID_BLACK_LIST.getId())
                        .flatMap(ingredientShopList -> {
                            if (ingredientShopList != null) {
                                return utils.ByIngredientShopList().delete(ingredientShopList).toSingleDefault(true).onErrorReturnItem(false);
                            } else { return Single.just(false); }
                        })
                )
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) { return false; }
                    }
                    return true;
                });
    }
}
