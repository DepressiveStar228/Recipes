package com.example.recipes.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.DishGetAdapter;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Controller.ShakeDetector;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.ID_System_Collection;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Option.CollectionOptions;
import com.example.recipes.Item.Option.DishOptions;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.ViewItem.MenuItemView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
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
    private AppCompatImageView clearSearchText;
    private SearchController<Dish> searchController;
    private RecipeUtils utils;
    private PreferencesController preferencesController;
    private String[] themeArray;
    private DishGetAdapter adapter;
    private String nameActivity;
    private ShakeDetector shakeDetector;
    private DishOptions dishOptions;
    private CollectionOptions collectionOptions;
    private CompositeDisposable compositeDisposable;
    private AtomicBoolean flagClear = new AtomicBoolean(false);
    private AtomicBoolean flagAccessDelete = new AtomicBoolean(true);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preferencesController = PreferencesController.getInstance();
        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");
        utils = RecipeUtils.getInstance(this);
        collection = new Collection("", CollectionType.VOID);
        compositeDisposable = new CompositeDisposable();
        dishOptions = new DishOptions(this, compositeDisposable);
        nameActivity = this.getClass().getSimpleName();
        collectionOptions = new CollectionOptions(this, compositeDisposable);

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
                            collection.setName(utils.ByCollection().getCustomNameSystemCollectionByName(collection.getName()));
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

        ConstraintLayout searchField = findViewById(R.id.searchDishCollection);
        searchEditText = searchField.findViewById(R.id.searchEditText);
        clearSearchText = searchField.findViewById(R.id.clearInputTextButton);

        dishRecyclerView = findViewById(R.id.dishRecyclerView);
        back = findViewById(R.id.back);
        setting = findViewById(R.id.setting);

        drawerLayout = findViewById(R.id.collectionDrawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            navigationView.removeHeaderView(navigationView.getHeaderView(0));

            if (collectionID > ID_System_Collection.values().length) {
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
        if (linearLayout1 != null) linearLayout1.setOnClickListener(v -> collectionOptions.addToCollection(collection));
        if (linearLayout2 != null) linearLayout2.setOnClickListener(v -> collectionOptions.copyToCollection(collection));
        if (linearLayout3 != null) linearLayout3.setOnClickListener(v -> collectionOptions.editNameCollection(collection));
        if (linearLayout4 != null) linearLayout4.setOnClickListener(v -> collectionOptions.shareCollection(collection));
        if (linearLayout5 != null) linearLayout5.setOnClickListener(v -> collectionOptions.clearCollection(collection));
        if (linearLayout6 != null) linearLayout6.setOnClickListener(v -> collectionOptions.deleteCollectionOnly(collection, this::finish));
        if (linearLayout7 != null) linearLayout7.setOnClickListener(v -> collectionOptions.deleteCollectionWithDish(collection, this::finish));

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
        if (clearSearchText != null) {
            clearSearchText.setOnClickListener(v -> {
                if (searchEditText != null) {
                    searchEditText.setText("");

                    if (searchController != null) {
                        searchController.search();
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
                if (clearSearchText != null) searchController.setClearSearchEditText(clearSearchText);
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
                            dishOptions.editDish(() -> {
                                Intent intent = new Intent(CollectionActivity.this, EditorDishActivity.class);
                                intent.putExtra(IntentKeys.DISH_ID.name(), dish.getId());
                                startActivity(intent);
                            });
                            return true;
                        } else if (item.getItemId() == R.id.action_remove_dish) {
                            dishOptions.removeDish(dish, collection);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_dish) {
                            dishOptions.deleteDish(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_copy_as_text_dish) {
                            dishOptions.copy_as_text(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_share_dish) {
                            dishOptions.shareDish(dish);
                            return true;
                        } else if (item.getItemId() == R.id.action_add_in_collection_dish) {
                            dishOptions.showAddDishInCollectionDialog(dish);
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
}
