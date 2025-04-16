package com.example.recipes.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.ChooseItemAdapter;
import com.example.recipes.Adapter.AddDishToCollectionsAdapter;
import com.example.recipes.Adapter.DishGetAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Controller.ShakeDetector;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.ID_System_Collection;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.Utils.ClassUtils;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.ViewItem.MenuItemView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Активність для відображення та управління колекцією страв.
 * Дозволяє користувачу переглядати, додавати, редагувати та видаляти страви,
 * а також керувати самою колекцією (перейменування, очищення, видалення).
 */
public class CollectionActivity extends AppCompatActivity {
    private MenuItemView linearLayout1, linearLayout2, linearLayout3, linearLayout4, linearLayout5, linearLayout6, linearLayout7;
    private AlertDialog dishCardDialog;
    private ImageView back, setting;
    private TextView name;
    private ConstraintLayout empty;
    private RecyclerView dishRecyclerView;
    private EditText searchEditText;
    private Collection collection;
    private DrawerLayout drawerLayout;
    private SearchController<Dish> searchController;
    private RecipeUtils utils;
    private PreferencesController preferencesController;
    private String[] themeArray;
    private DishGetAdapter adapter;
    private String nameActivity;
    private ShakeDetector shakeDetector;
    private ImportExportController importExportController;
    private CompositeDisposable compositeDisposable;
    private AtomicBoolean flagClear = new AtomicBoolean(false);
    private AtomicBoolean flagAccessDelete = new AtomicBoolean(true);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preferencesController = PreferencesController.getInstance();
        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");
        utils = RecipeUtils.getInstance(this);
        importExportController = new ImportExportController(this);
        collection = new Collection("", CollectionType.VOID);
        compositeDisposable = new CompositeDisposable();
        nameActivity = this.getClass().getSimpleName();

        super.onCreate(savedInstanceState);
        preferencesController.setPreferencesToActivity(this);
        setContentView(R.layout.collection_activity);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        long collectionID = getIntent().getLongExtra(IntentKeys.COLLECTION_ID.name(), -1);

        loadItemsActivity(collectionID);
        loadClickListeners();

        if (collectionID > 0) {
            // Слухач зміни в базі даних
            utils.ByCollection().getViewModel().getCollectionByID(collectionID).observe(this,
                    data -> {
                        if (data != null && flagAccessDelete.get() && !flagClear.get()) {
                            collection = data;
                            collection.setName(utils.getCustomNameSystemCollection(collection.getName()));
                            setDataIntoItemsActivity();
                        }
                    });
        } else {
            Toast.makeText(this, getString(R.string.error_read_get_dish), Toast.LENGTH_SHORT).show();
        }

        // Детектор струшування для випадкової страви.
        shakeDetector = new ShakeDetector(this::openRandDishCard);
        shakeDetector.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        shakeDetector.unregister(this);
        Log.d(nameActivity, "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else { super.onBackPressed(); }
        } else { super.onBackPressed(); }
    }

    /**
     * Ініціалізує та налаштовує усі UI елементи активності в залежності від типу колекції.
     * Для системних колекцій (ID <= 4) використовується інший набір елементів меню.
     *
     * @param collectionID ID колекції для завантаження
     */
    @SuppressLint("ResourceType")
    private void loadItemsActivity(long collectionID) {
        name = findViewById(R.id.nameCollection);
        empty = findViewById(R.id.dishEmpty);
        searchEditText = findViewById(R.id.searchEditText);
        dishRecyclerView = findViewById(R.id.dishRecyclerView);
        back = findViewById(R.id.back);
        setting = findViewById(R.id.setting);

        drawerLayout = findViewById(R.id.collectionDrawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            navigationView.removeHeaderView(navigationView.getHeaderView(0));

            if (collectionID > ID_System_Collection.ID_BLACK_LIST.getId()) {
                View headerView = getLayoutInflater().inflate(R.layout.collection_menu_panel, navigationView, false);

                if (headerView != null) {
                    navigationView.addHeaderView(headerView);

                    linearLayout1 = headerView.findViewById(R.id.add_dish_to_collection);
                    linearLayout2 = headerView.findViewById(R.id.copy_to);
                    linearLayout3 = headerView.findViewById(R.id.edit);
                    linearLayout4 = headerView.findViewById(R.id.share);
                    linearLayout5 = headerView.findViewById(R.id.clear);
                    linearLayout6 = headerView.findViewById(R.id.delete_only_collection);
                    linearLayout7 = headerView.findViewById(R.id.delete_collection_with_dishes);
                }
            } else {
                View headerView = getLayoutInflater().inflate(R.layout.system_collection_menu_panel, navigationView, false);

                if (headerView != null) {
                    navigationView.addHeaderView(headerView);

                    linearLayout1 = headerView.findViewById(R.id.add_dish_to_collection);
                    linearLayout2 = headerView.findViewById(R.id.copy_to);
                    linearLayout4 = headerView.findViewById(R.id.share);
                    linearLayout5 = headerView.findViewById(R.id.clear);
                }
            }
        }

        Log.d(nameActivity, "Елементи активності успішно завантажені");
    }

    /**
     * Встановлює обробники кліків для усіх елементів меню та інтерфейсу.
     */
    private void loadClickListeners() {
        if (linearLayout1 != null) linearLayout1.setOnClickListener(v -> handleAddToCollection());
        if (linearLayout2 != null) linearLayout2.setOnClickListener(v -> handleCopyToCollection());
        if (linearLayout3 != null) linearLayout3.setOnClickListener(v -> handleEditNameCollection());
        if (linearLayout4 != null) linearLayout4.setOnClickListener(v -> handleShareCollection());
        if (linearLayout5 != null) linearLayout5.setOnClickListener(v -> handleClearCollection());
        if (linearLayout6 != null) linearLayout6.setOnClickListener(v -> handleDeleteCollectionOnly());
        if (linearLayout7 != null) linearLayout7.setOnClickListener(v -> handleDeleteCollectionWithDish());

        if (back != null) back.setOnClickListener(v -> finish());
        if (setting != null) {
            setting.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                        drawerLayout.closeDrawer(GravityCompat.END);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.END);
                    }
                }
            });
        }
    }

    /**
     * Заповнює UI елементи даними поточної колекції.
     */
    private void setDataIntoItemsActivity() {
        if (name != null) { name.setText(collection.getName()); }
        if (dishRecyclerView != null) {
            setDishAdapter();
            setSearchController();
        }
    }

    /**
     * Налаштовує контролер пошуку для фільтрації страв у колекції.
     */
    private void setSearchController() {
        if (searchEditText != null && dishRecyclerView != null) {
            if (searchController == null) {
                if (adapter == null) setDishAdapter();

                searchController = new SearchController<>(this, searchEditText, dishRecyclerView, adapter);
            }
        }
    }

    /**
     * Налаштовує адаптер для відображення списку страв.
     * Встановлює слухачів для оновлення даних при зміні страв у базі даних.
     * Оновлює інтерфейс залежно від наявності страв у колекції.
     */
    private void setDishAdapter() {
        if (adapter == null) {
            adapter = new DishGetAdapter(this, new DishGetAdapter.DishClickListener() {
                @Override
                public void onDishClick(Dish item, View v) {
                    Intent intent = new Intent(v.getContext(), EditorDishActivity.class);
                    intent.putExtra(IntentKeys.DISH_ID.name(), item.getId());
                    v.getContext().startActivity(intent);
                }

                @Override
                public void onDishMenuClick(Dish dish, View v) {
                    PopupMenu popupMenu = new PopupMenu(CollectionActivity.this, v, Gravity.END);
                    popupMenu.getMenuInflater().inflate(R.menu.context_menu_dish, popupMenu.getMenu());

                    for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                        MenuItem item = popupMenu.getMenu().getItem(i);
                        SpannableString spannableString = new SpannableString(item.getTitle());
                        if (!Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(CollectionActivity.this, R.color.white)), 0, spannableString.length(), 0);
                        } else {
                            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(CollectionActivity.this, R.color.black)), 0, spannableString.length(), 0);
                        }
                        item.setTitle(spannableString);
                    }

                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_edit_dish) {
                            handleEditDish(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_remove_dish) {
                            handleRemoveDish(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_dish) {
                            handleDeleteDish(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_copy_as_text_dish) {
                            handleCopyAsTextDish(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_share_dish) {
                            handleShareDish(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_add_in_collection_dish) {
                            handleAddInCollectionDish(dish);
                            return true;
                        }  else {
                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });

            dishRecyclerView.setAdapter(adapter);
            dishRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            dishRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(-4));
        }

        // Слухач зміни страв колекції в базі даних
        utils.ByDish_Collection().getViewModel().getAllDishIDs(collection.getId()).observe(this, data -> {
            if (data != null) {
                Disposable disposable = Observable.fromIterable(data)
                        .flatMapSingle(id -> utils.ByDish().getByID(id))
                        .toList()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dishes -> {
                            adapter.setItems(new ArrayList<>(dishes));
                            collection.setDishes(new ArrayList<>(dishes));

                            if (searchController != null) {
                                searchController.setArrayData(new ArrayList<>(dishes));
                                searchController.search();
                            }

                            AnotherUtils.visibilityEmptyStatus(empty, dishes.isEmpty());
                        });
                compositeDisposable.add(disposable);
            }
        });
    }

    /**
     * Відкриває діалогове вікно з випадково обраною стравою з колекції.
     * Викликається при струшуванні пристрою.
     */
    private void openRandDishCard() {
        if (dishCardDialog != null) { dishCardDialog.dismiss(); } // Якщо карточка вже відкрита, то вона закриється

        if (!collection.getDishes().isEmpty()) {
            Random random = new Random();
            Dish randDish = collection.getDishes().get(random.nextInt(collection.getDishes().size()));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_dish_card, null);

            if (dialogView != null) {
                builder.setView(dialogView);
                dishCardDialog = builder.create();

                ConstraintLayout dishCardLayout = dialogView.findViewById(R.id.dishCardLayout);

                if (dishCardLayout != null) {
                    dishCardLayout.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), EditorDishActivity.class);
                        intent.putExtra(IntentKeys.DISH_ID.name(), randDish.getId());
                        v.getContext().startActivity(intent);
                    });
                }

                TextView textView = dialogView.findViewById(R.id.dishName);
                if (textView != null) { textView.setText(randDish.getName()); }

                ImageView close = dialogView.findViewById(R.id.close);
                if (close != null) { close.setOnClickListener(v -> dishCardDialog.dismiss()); }

                dishCardDialog.show();
            }
        }
    }



    //
    // Handles Collection
    //

    /**
     * Відкриває діалог для додавання нових страв до поточної колекції.
     * Показує список усіх страв, які ще не входять до цієї колекції.
     */
    private void handleAddToCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_items, null);
        TextView textView = dialogView.findViewById(R.id.textView22);
        if (textView != null) { textView.setText(R.string.your_dish); }
        RecyclerView dishesRecyclerView = dialogView.findViewById(R.id.items_check_RecyclerView);
        Disposable disposable = utils.ByCollection().getUnusedDish(collection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused_dishes -> {   // Отримаємо страви, яких немає у вибраній коллекції
                            ChooseItemAdapter<Dish> chooseItemAdapter = new ChooseItemAdapter<>(this, (checkBox, item) -> { });
                            adapter.setItems(unused_dishes);
                            dishesRecyclerView.setAdapter(adapter);
                            dishesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                                        ArrayList<?> selectedDishIds = chooseItemAdapter.getSelectedItem();  // Отримаємо ID став, які були вибрані в діалозі
                                        ArrayList<Long> selectedDishLongIds = ClassUtils.getListOfType(selectedDishIds, Long.class);

                                        if (!selectedDishIds.isEmpty()) {
                                            Disposable disposable1 = utils.ByDish().getAll(selectedDishLongIds)
                                                    .subscribeOn(Schedulers.io())
                                                    .flatMap(dishes -> utils.ByDish_Collection().addAllWithCheckExist(new ArrayList<>(dishes), collection.getId()))
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                                if (status) {
                                                                    Toast.makeText(this, getString(R.string.successful_add_dishes), Toast.LENGTH_SHORT).show();
                                                                    Log.d(nameActivity, "Страви успішно додано до колекції (й)");
                                                                }
                                                            },
                                                            throwable -> {
                                                                Log.d(nameActivity, "Помилка додавання страв до колекції(й)");
                                                            }
                                                    );
                                            compositeDisposable.add(disposable1);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                            builder.create().show();
                        },
                        throwable -> {
                            Log.d(nameActivity, "Помилка отримання страв, які не лежать в колекції");
                        }
                );

        compositeDisposable.add(disposable);
    }

    /**
     * Відкриває діалог для копіювання усіх страв з поточної колекції в інші колекції.
     * Дозволяє обрати декілька колекцій одночасно.
     */
    private void handleCopyToCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);

        Disposable disposable = utils.ByCollection().getAllByType(CollectionType.COLLECTION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listData -> {
                            if (ClassUtils.isListOfType(listData, Collection.class)) {
                                ArrayList<Collection> allCollections = ClassUtils.getListOfType(listData, Collection.class);
                                allCollections.remove(collection);
                                AddDishToCollectionsAdapter addDishToCollectionsAdapter = new AddDishToCollectionsAdapter(this, allCollections);
                                collectionsRecyclerView.setAdapter(adapter);
                                collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                                builder.setView(dialogView)
                                        .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                                            // Отримаємо ID вибраних коллекцій
                                            ArrayList<Long> selectedCollectionIds = addDishToCollectionsAdapter.getSelectedCollectionIds();
                                            if (!selectedCollectionIds.isEmpty()) {
                                                Disposable disposable1 = utils.ByDish_Collection().copyDishesToAnotherCollections(collection.getId(), selectedCollectionIds)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(status -> {
                                                                    if (status) {
                                                                        Toast.makeText(this, getString(R.string.successful_copy_dishes), Toast.LENGTH_SHORT).show();
                                                                        Log.d(nameActivity, "Страви успішно скопійовано до колекції(й)");
                                                                    }
                                                                },
                                                                throwable -> {
                                                                    Log.d(nameActivity, "Помилка копіювання страв до колекції(й)");
                                                                }
                                                        );

                                                compositeDisposable.add(disposable1);
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                                builder.create().show();
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
    private void handleEditNameCollection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_collection, null);
        EditText editText = dialogView.findViewById(R.id.edit_collection_name_editText);
        editText.setText(collection.getName());
        CharacterLimitTextWatcher.setCharacterLimit(this, editText, Limits.MAX_CHAR_NAME_COLLECTION);
        builder.setView(dialogView)
                .setTitle(R.string.edit_collection)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();
                    if (collectionName.isEmpty()) {
                        Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                    }

                    Disposable disposable = utils.ByCollection().getIdByName(collectionName)
                            .flatMap(
                                    id -> {
                                        if (id != -1) {
                                            Toast.makeText(this, R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                            return Single.error(new Exception(getString(R.string.warning_dublicate_name_collection)));
                                        } else {
                                            collection.setName(collectionName);
                                            return utils.ByCollection().update(collection).toSingleDefault(collection);
                                        }
                                    },
                                    throwable -> {
                                        Log.d(nameActivity, "Помилка виконання запиту отримання айді колекції за ім'ям");
                                        return Single.error(new Exception(getString(R.string.warning_dublicate_name_collection)));
                                    }
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    updatedCollection -> Toast.makeText(this, R.string.successful_edit_collection, Toast.LENGTH_SHORT).show(),
                                    throwable -> {
                                        if (throwable.getMessage() != null && throwable.getMessage().equals(getString(R.string.warning_dublicate_name_collection))) {
                                            Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    /**
     * Експортує усі страви з колекції у файл та відкриває вікно для надсилання файлу.
     * Спершу показує повідомлення з попередженням про експорт.
     */
    private void handleShareCollection() {
        if (!collection.getDishes().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_export))
                    .setMessage(getString(R.string.warning_export))
                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                        importExportController.exportRecipeData(this, collection, new ExportCallbackUri() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    FileUtils.sendFileByUri(CollectionActivity.this, uri);
                                    FileUtils.deleteFileByUri(CollectionActivity.this, uri);
                                    Toast.makeText(CollectionActivity.this, getString(R.string.successful_export) + uri, Toast.LENGTH_LONG).show();
                                    Log.d(nameActivity, "Рецепти успішно експортовані");
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Toast.makeText(CollectionActivity.this, getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                Log.e(nameActivity, "Помилка експорту", throwable);
                            }

                            @Override
                            public void getDisposable(Disposable disposable) {
                                compositeDisposable.add(disposable);
                            }
                        });
                    })
                    .setNegativeButton(getString(R.string.no), null).show();
        } else {
            Toast.makeText(this, R.string.error_empty_collection, Toast.LENGTH_SHORT).show();
            Log.d(nameActivity, "Помилка. Колекція порожня");
        }
    }

    /**
     * Видаляє усі страви з колекції, але зберігає саму колекцію.
     * Показує діалог підтвердження перед видаленням.
     */
    private void handleClearCollection() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_clear_collection))
                .setMessage(getString(R.string.warning_clear_collection))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDish_Collection().deleteAllByIDCollection(collection.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    status -> {
                                        Toast.makeText(this, R.string.successful_clear_collerction, Toast.LENGTH_SHORT).show();
                                        Log.d(nameActivity, "Колекція успішно очищена");
                                    },
                                    throwable -> Log.e(nameActivity, "Помилка очищення колекції")
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    /**
     * Видаляє колекцію, але зберігає страви, що були в ній.
     * Показує діалог підтвердження перед видаленням.
     */
    private void handleDeleteCollectionOnly() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_collection))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().deleteWithDishes(collection)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(this, this.getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                                        finish();
                                    },
                                    throwable -> {
                                        Toast.makeText(this, this.getString(R.string.error_delete_collection), Toast.LENGTH_SHORT).show();
                                        Log.e(nameActivity, "Помилка видалення колекції: " + throwable.getMessage());
                                    });

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    /**
     * Видаляє колекцію разом з усіма стравами, що були в ній.
     * Показує діалог підтвердження перед видаленням.
     */
    private void handleDeleteCollectionWithDish() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_collection_with_dishes))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().deleteWithDishes(collection)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(this, this.getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                                        finish();
                                    },
                                    throwable -> {
                                        Toast.makeText(this, this.getString(R.string.error_delete_collection), Toast.LENGTH_SHORT).show();
                                        Log.e(nameActivity, "Помилка видалення колекції: " + throwable.getMessage());
                                    });

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }





    //
    // Handles Dish
    //

    /**
     * Відкриває діалог для додавання обраної страви до інших колекцій.
     * Показує список колекцій, які ще не містять цю страву.
     *
     * @param dish Страва для додавання в інші колекції
     */
    private void handleAddInCollectionDish(Dish dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);
        Disposable disposable = utils.ByCollection().getUnusedInDish(dish)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused_collections -> {
                            AddDishToCollectionsAdapter addDishToCollectionsAdapter = new AddDishToCollectionsAdapter(this, unused_collections);
                            collectionsRecyclerView.setAdapter(adapter);
                            collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_add, (dialog, which) -> {
                                        ArrayList<Long> selectedCollectionIds = addDishToCollectionsAdapter.getSelectedCollectionIds();
                                        if (!selectedCollectionIds.isEmpty()) {
                                            Disposable disposable1 = utils.ByDish_Collection().addAll(dish, selectedCollectionIds)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                                if (status) {
                                                                    Toast.makeText(this, getString(R.string.successful_add_dish_in_collection), Toast.LENGTH_SHORT).show();
                                                                    Log.d("CollectionsDishFragment", "Страва успішно додана в колекцію(ї)");
                                                                }
                                                            },
                                                            throwable -> {
                                                                Log.d("CollectionsDishFragment", "Помилка додавання страви в колекцію(ї)");
                                                            }
                                                    );
                                            compositeDisposable.add(disposable1);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                            builder.create().show();
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання колекцій, в яких немає поточної страви");
                        }
                );
        compositeDisposable.add(disposable);
    }


    /**
     * Відкриває активність редактора страви та передає обрану страву туди.
     *
     * @param dish Страва для редагування
     */
    private void handleEditDish(Dish dish) {
        Intent intent = new Intent(this, EditorDishActivity.class);
        intent.putExtra(IntentKeys.DISH_ID.name(), dish.getId());
        startActivity(intent);
    }

    /**
     * Копіює інформацію про страву (назву, інгредієнти, опис) у буфер обміну.
     *
     * @param dish Страва для копіювання
     */
    private void handleCopyAsTextDish(Dish dish) {
        Disposable disposable = utils.ByIngredient().getAllByIDDish(dish.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ingredients -> {
                            dish.setIngredients(new ArrayList<>(ingredients));

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", dish.getAsText(this));
                            clipboard.setPrimaryClip(clip);

                            Toast.makeText(this, getString(R.string.copy_clipboard_text), Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання інгредієнтів страви");
                        }
                );
        compositeDisposable.add(disposable);
    }

    /**
     * Експортує окрему страву у файл та відкриває вікно для надсилання файлу.
     *
     * @param dish Страва для експорту
     */
    private void handleShareDish(Dish dish) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_export))
                .setMessage(getString(R.string.warning_export))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    importExportController.exportDish(this, dish, new ExportCallbackUri() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (uri != null) {
                                FileUtils.sendFileByUri(CollectionActivity.this, uri);
                                FileUtils.deleteFileByUri(CollectionActivity.this, uri);
                                Log.d("CollectionsDishFragment", "Рецепт успішно експортовані");
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Toast.makeText(CollectionActivity.this, getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                            Log.e("ImportExportController", "Помилка при експорті рецептів", throwable);
                        }

                        @Override
                        public void getDisposable(Disposable disposable) {
                            compositeDisposable.add(disposable);
                        }
                    });
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    /**
     * Видаляє страву лише з поточної колекції, але зберігає її в базі даних.
     *
     * @param dish Страва для видалення з колекції
     */
    private void handleRemoveDish(Dish dish) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_remove_dish))
                .setMessage(getString(R.string.warning_remove_dish))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDish_Collection().getByData(dish.getId(), collection.getId())
                            .flatMapCompletable(dish_collection -> {
                                if (dish_collection.getIdDish() == 0 || dish_collection.getIdCollection() == 0) {
                                    return Completable.error(new Throwable("Error. Dish_collection was not found"));
                                } else {
                                    return utils.ByDish_Collection().delete(dish_collection);
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(this, R.string.successful_remove_collerction, Toast.LENGTH_SHORT).show();
                                        Log.d("CollectionsDishFragment", "Страва успішно прибрана з колекції");
                                    },
                                    throwable -> {
                                        Log.e("CollectionsDishFragment", "Помилка прибирання страви з колекції", throwable);
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    /**
     * Повністю видаляє страву з бази даних.
     *
     * @param dish Страва для повного видалення
     */
    private void handleDeleteDish(Dish dish) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_dish))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDish().delete(dish)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(this, getString(R.string.successful_delete_dish), Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        Toast.makeText(this, getString(R.string.error_delete_dish), Toast.LENGTH_SHORT).show();
                                    }
                            );
                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }
}
