package com.example.recipes.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.ChooseItemAdapter;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Decoration.CustomSpinnerAdapter;
import com.example.recipes.Adapter.DishGetToShopListAdapter;
import com.example.recipes.Adapter.IngredientShopListGetAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Decoration.HorizontalSpaceItemDecoration;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Enum.Tips;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishCollection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.Option.ShopListOptions;
import com.example.recipes.Item.ShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;


/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
public class ShopListActivity extends AppCompatActivity {
    private LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4;
    private CompositeDisposable compositeDisposable, compositeByIngredients;
    private PreferencesController preferencesController;
    private DrawerLayout drawerLayout;
    private ConstraintLayout dishShopListLayout;
    private RecipeUtils utils;
    private ShopList shopList;
    private TextView name, bought_items, all_item;
    private ConstraintLayout ingredientsEmpty;
    private ImageView addDish, addIngredient, exit, setting;
    private RecyclerView ingShopList, dishShopList;
    private IngredientShopListGetAdapter ingAdapter;
    private DishGetToShopListAdapter dishAdapter;
    private SearchController<Dish> searchController;
    private ShopListOptions shopListOptions;
    private String nameActivity;

    // Флаги потрібні, щоб слухачі не підтягували елементи з БД під час видалення чи очистки списук покупків
    private AtomicBoolean flagClear = new AtomicBoolean(false);
    private AtomicBoolean flagAccessDelete = new AtomicBoolean(true);

    private long shopListID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferencesController = PreferencesController.getInstance();
        utils = RecipeUtils.getInstance(this);
        shopList = new ShopList("");
        compositeDisposable = new CompositeDisposable();
        compositeByIngredients = new CompositeDisposable();
        nameActivity = this.getClass().getSimpleName();
        shopListOptions = new ShopListOptions(this, compositeDisposable);

        super.onCreate(savedInstanceState);
        preferencesController.setPreferencesToActivity(this);
        setContentView(R.layout.editor_shop_list_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        loadItemsActivity();
        loadClickListeners();

        RxJavaPlugins.setErrorHandler(error -> {
            Log.e(nameActivity, "Непіймана помилка БД: ", error);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        shopListID = getIntent().getLongExtra(IntentKeys.SHOP_LIST_ID.name(), -1);

        if (shopListID > 0) {
            // Слухач на будь-яку зміну в списку покупок та оновлення сторінки поточними даними
            utils.ByCollection().getViewModel().getCollectionByID(shopListID).observe(this,
                    data -> {
                        if (data != null && flagAccessDelete.get() && !flagClear.get()) {
                            shopList = new ShopList(data);
                            setDataIntoItemsActivity();
                            putIngredientsWithAmountTypesToAdapter();
                        }
                    });
        } else {
            Toast.makeText(this, getString(R.string.error_read_get_shop_list), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else { super.onBackPressed(); }
        } else { super.onBackPressed(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        compositeByIngredients.clear();
        Log.d(nameActivity, "Активність успішно закрита");
    }

    @SuppressLint("ResourceType")
    private void loadItemsActivity() {
        name = findViewById(R.id.nameShopList);
        bought_items = findViewById(R.id.bought_items);
        all_item = findViewById(R.id.all_item);
        ingredientsEmpty = findViewById(R.id.ingredientsEmpty);
        ingShopList = findViewById(R.id.ingShopList);
        dishShopList = findViewById(R.id.dishShopList);
        dishShopListLayout = findViewById(R.id.dishShopListLayout);
        addIngredient = findViewById(R.id.addIngredientShopListButton);
        addDish = findViewById(R.id.addDishShopListButton);
        exit = findViewById(R.id.back);
        setting = findViewById(R.id.setting);

        drawerLayout = findViewById(R.id.shopListDrawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);

            if (headerView != null) {
                linearLayout1 = headerView.findViewById(R.id.setting_edit);
                linearLayout2 = headerView.findViewById(R.id.setting_copy_as_text);
                linearLayout3 = headerView.findViewById(R.id.setting_clear);
                linearLayout4 = headerView.findViewById(R.id.setting_delete);
            }
        }

        Log.d(nameActivity, "Елементи активності успішно завантажені");
    }

    private void loadClickListeners() {
        linearLayout1.setOnClickListener(v -> shopListOptions.editName(shopList, updatedCollection -> {
            if (name != null) {
                name.setText(updatedCollection.getName());
            }
        }));
        linearLayout2.setOnClickListener(v -> shopListOptions.copyAsTest(shopList));
        linearLayout3.setOnClickListener(v -> shopListOptions.clear(flagClear, shopList, shopListID, collection -> {
            shopList = new ShopList(collection);
            setDataIntoItemsActivity();
            putIngredientsWithAmountTypesToAdapter();
        }));
        linearLayout4.setOnClickListener(v -> shopListOptions.delete(shopList, flagAccessDelete, () -> {
            Toast.makeText(this, R.string.successful_delete_shop_list, Toast.LENGTH_SHORT).show();
            finish();
        }));

        if (exit != null) { exit.setOnClickListener(v -> finish()); }
        if (setting != null) {
            setting.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            });
        }
    }

    /**
     * Вставляє дані в UI.
     */
    private void setDataIntoItemsActivity() {
        if (name != null) { name.setText(shopList.getName()); }
        if (bought_items != null) {
            // Слухач на зміну кількості куплених елементів списку покупків
            utils.ByIngredientShopList().getViewModel().getBoughtCountByIdCollection(shopList.getId()).observe(this, data -> {
                if (data != null) {
                    bought_items.setText(data.toString());
                }
            });
        }
        if (all_item != null) {
            // Слухач на зміну кількості всіх елементів списку покупків
            utils.ByIngredientShopList().getViewModel().getCountByIdCollection(shopList.getId()).observe(this, data -> {
                if (data != null) {
                    all_item.setText(data.toString());
                }
            });
        }
        if (ingShopList != null) {
            setIngredientAdapter();
        }
        if (dishShopList != null) {
            setDishAdapter();
        }
        if (addIngredient != null) {
            // Виклик підказки, що пояснює роботу кнопок додавання елементів у список покупки
            if (!preferencesController.getStatusUsedTip(Tips.SHOP_LIST_BUTTONS_KEY)) {
                new Handler(Looper.getMainLooper()).post(() -> shopTip(addIngredient)); // Викликаємо підказку після відображення кнопки "Додати інгредиєнт"
                preferencesController.useTip(Tips.SHOP_LIST_BUTTONS_KEY);
            }

            addIngredient.setOnClickListener(view -> {
                onAddIngredientButtonClick();
            });
        }
        if (addDish != null) {
            addDish.setOnClickListener(view -> {
                onAddDishButtonClick();
            });
        }
    }

    /**
     * Налаштовує адаптер для відображення страв, які були додані до списку покупків.
     * Встановлює слухач на видалення страв.
     */
    private void setDishAdapter() {
        if (dishAdapter == null) {
            dishAdapter = new DishGetToShopListAdapter(
                    dish -> {
                        if (flagAccessDelete.get()) {
                            flagAccessDelete.set(false);

                            Disposable disposable = utils.ByDishCollection().getByData(dish.getId(), shopList.getId())
                                    .flatMapCompletable(dish_collection -> utils.ByDishCollection().delete(dish_collection))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            () -> flagAccessDelete.set(true),
                                            throwable -> flagAccessDelete.set(true)
                                    );

                            compositeDisposable.add(disposable);
                        }
                    }
            );

            FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
            flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
            flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);

            dishShopList.setAdapter(dishAdapter);
            dishShopList.setLayoutManager(flexboxLayoutManager);
            dishShopList.addItemDecoration(new HorizontalSpaceItemDecoration(5));
            dishShopList.addItemDecoration(new VerticalSpaceItemDecoration(5));
            dishShopList.getAdapter().notifyDataSetChanged();

            // Зміна висоти списку страв на основі наповнення
            dishShopList.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                int size = dishShopList.getHeight();
                int maxSize = AnotherUtils.dpToPx(100, this);
                ViewGroup.LayoutParams layoutParams = dishShopList.getLayoutParams();

                if (size > maxSize) {
                    layoutParams.height = maxSize;

                } else {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                dishShopList.setLayoutParams(layoutParams);
            });
        }

        // Слухач зміни ID страв, які належать списку покупків
        utils.ByDishCollection().getViewModel().getAllDishIDs(shopList.getId()).observe(this, data -> {
            if (data != null && !flagClear.get()) {
                Disposable disposable = Observable.fromIterable(data)
                        .flatMapSingle(id -> utils.ByDish().getByID(id))
                        .toList()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dishes -> {
                            if (dishes != null) {
                                dishAdapter.setItems(new ArrayList<>(dishes));
                                shopList.setDishes(new ArrayList<>(dishes));

                                if (dishes.isEmpty() && dishShopListLayout != null) {
                                    dishShopListLayout.setVisibility(View.GONE);
                                } else {
                                    dishShopListLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                compositeDisposable.add(disposable);
            }
        });
    }

    /**
     * Налаштовує адаптер для відображення інгредієнтів.
     * Встановлює слухач на видалення інгредієнта та кліка.
     */
    private void setIngredientAdapter() {
        if (ingAdapter == null) {
            ingAdapter = new IngredientShopListGetAdapter(
                    this,
                    new IngredientShopListGetAdapter.IngredientShopListClickListener() {
                        @Override
                        public void onIngredientShopListClick(IngredientShopList ingredientShopList) {
                            compositeByIngredients.clear();

                            ingredientShopList.setIsBuy(!ingredientShopList.getIsBuy());
                            Disposable disposable = utils.ByIngredientShopList().update(ingredientShopList)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();

                            compositeByIngredients.add(disposable);
                        }

                        @Override
                        public void onDeleteClick(IngredientShopList ingredientShopList) {
                            compositeByIngredients.clear();

                            Disposable disposable = utils.ByIngredientShopList().delete(ingredientShopList)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();

                            compositeByIngredients.add(disposable);
                        }
                    }
            );

            ingShopList.setAdapter(ingAdapter);
            ingShopList.setLayoutManager(new LinearLayoutManager(this));
            ingShopList.addItemDecoration(new VerticalSpaceItemDecoration(-4));
            ingShopList.getAdapter().notifyDataSetChanged();
        }

        // Два слухача потрібно для враховування всіх можливих змін елементів списку покупків.
        // Слухач на зміну статусу купівлі елементів списку покупків
        utils.ByIngredientShopList().getViewModel().getBoughtCountByIdCollection(shopList.getId()).observe(this,
                data -> {
                    if (data != null && flagAccessDelete.get() && !flagClear.get()) {
                        putIngredientsWithAmountTypesToAdapter();
                    }
                }
        );

        // Слухач на будь-яку зміну кількості/типу інгредієнтів для списку покупків
        utils.ByIngredientShopList_AmountType().getViewModel().getAll().observe(this,
                data -> {
                    if (data != null && flagAccessDelete.get() && !flagClear.get()) {
                        putIngredientsWithAmountTypesToAdapter();
                    }
                }
        );
    }

    /**
     * Метод для додавання інгредієнтів до списку покупків
     *
     * @param ingredients інгредієнти для списку покупків
     * @param idDish ID страви
     * @return статус успішності додавання
     */
    private Single<Boolean> addIngredientShopList_AmountFromIngredientsShopList(List<IngredientShopList> ingredients, Long idDish) {
        // Перебираємо кожен новий інгредієнт та перевіряємо чи існує інгредієнт з такою ж назвою у списку покупок
        return Observable.fromIterable(ingredients)
                .flatMapSingle(ingredient -> utils.ByIngredientShopList()
                        .getByNameAndIDCollection(ingredient.getName(), shopList.getId())
                        .flatMap(item -> {
                            if (item.getId() > 0 && item.getIdCollection() > 0) { // Якщо існує, то додаємо в БД лише записи кількості/типу цього інгредієнта
                                ingredient.setId(item.getId());
                                ingredient.setIdCollection(shopList.getId());
                                return utils.ByIngredientShopList_AmountType()
                                        .addAll(utils.ByIngredientShopList().createIngredientShopListAmountTypesFromGroupedAmountType(ingredient, idDish));
                            } else {                                               // Якщо не існує, то додаємо в БД і сам інгредієнт також
                                ingredient.setIdCollection(shopList.getId());
                                return utils.ByIngredientShopList().add(ingredient, idDish)
                                        .flatMap(id -> Single.just(id > 0));
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
     * Метод для отримання інгредієнтів списку покупків
     *
     * @param idDishCollection ID списку покупків
     * @return інгредієнти списку покупків, які відфільтровані за чорним списком
     */
    private Single<List<Ingredient>> getIngredientsFromDishCollection(long idDishCollection) {
        return utils.ByDishCollection().getByID(idDishCollection)
                .flatMap(dish_collection -> {
                    if (dish_collection.getIdDish() > 0 && dish_collection.getIdCollection() > 0) {
                        return utils.ByIngredient().getAllByIDDish(dish_collection.getIdDish());
                    } else { return Single.just(new ArrayList<Ingredient>()); }
                })
                .flatMap(ingredients -> utils.ByIngredientShopList().filteredBlackList(new ArrayList<>(ingredients)));
    }

    /**
     * Метод для додавання інгредієнтів до списку покупків
     *
     * @param ingredientsBox інгредієнти
     * @param idDishCollection ID списку покупків
     * @return статус успішності додавання
     */
    private Single<Boolean> setIngredientFromDishesToShopList(List<Ingredient> ingredientsBox, Long idDishCollection) {
        flagAccessDelete.set(false);
        return utils.ByIngredientShopList().convertIngredientsToIngredientsShopList(new ArrayList<>(ingredientsBox))
                .flatMap(ingredients -> utils.ByIngredientShopList().groupIngredients(ingredients, shopList))
                .flatMap(ingredients -> addIngredientShopList_AmountFromIngredientsShopList(ingredients, idDishCollection));
    }

    /**
     * Метод для оновлення адаптера списку інгредієнтів у списку покупок.
     * Отримує інгредієнти з бази даних, видаляє порожні записи (якщо дозволено),
     * та оновлює адаптер та список інгредієнтів у відповідному об'єкті.
     */
    private void putIngredientsWithAmountTypesToAdapter() {
        Disposable disposable = utils.ByIngredientShopList().getAllByIDCollection(shopList.getId())
                .flatMap(ingredients -> {
                    if (flagAccessDelete.get()) { // Якщо видалення дозволене, то видаляємо пусті інгредієнти
                        return utils.ByIngredientShopList().deleteEmptyAmountTypeByIDCollection(shopList.getId());
                    } else { return Single.just(true); }
                })
                .flatMap(status -> utils.ByIngredientShopList().getAllByIDCollection(shopList.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ingredients -> {
                    if (ingredients != null) {
                        if (ingAdapter != null) { ingAdapter.setItems((ArrayList<IngredientShopList>) ingredients); }
                        shopList.setIngredients((ArrayList<IngredientShopList>) ingredients);
                        if (ingredientsEmpty != null) {
                            AnotherUtils.visibilityEmptyStatus(ingredientsEmpty, shopList.getIngredients().isEmpty());
                        }
                    }
                });

        compositeByIngredients.add(disposable);
    }


    /**
     * Метод для обробки натискання кнопки додавання інгредієнта.
     * Відкриває діалогове вікно для введення назви, кількості та типу інгредієнта.
     * Після введення даних додає інгредієнт до списку покупок.
     */
    public void onAddIngredientButtonClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.set_ingredient_shop_list_item, null);
        EditText name = dialogView.findViewById(R.id.nameIngredientEditText);
        EditText amount = dialogView.findViewById(R.id.countIngredientEditText);
        Spinner type = dialogView.findViewById(R.id.spinnerTypeIngredient);

        // Встановлення обмежень на кількість символів для назви та кількості інгредієнта
        if (name != null) { CharacterLimitTextWatcher.setCharacterLimit(this, name, Limits.MAX_CHAR_NAME_INGREDIENT); }
        if (amount != null) { CharacterLimitTextWatcher.setCharacterLimit(this, amount, Limits.MAX_CHAR_AMOUNT_INGREDIENT); }

        // Налаштування Spinner для вибору типу інгредієнта
        if (type != null) {
            ArrayAdapter<String> typeAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(this.getResources().getStringArray(R.array.ingredient_types)));
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            type.setAdapter(typeAdapter);
            type.setSelection(IngredientType.VOID.getPosition());
        }

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_add, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

        // Обробка натискання кнопки "Додати"
        Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                if (name != null && amount != null && type != null) {
                    String nameBox = name.getText().toString();
                    String amountBox = amount.getText().toString();
                    String typeBox = type.getSelectedItem().toString();

                    // Перевірка наявності введених даних
                    if (!nameBox.isEmpty() && !amountBox.isEmpty() && !typeBox.isEmpty()) { // Перевіряємо наявність даних
                        ArrayList<Ingredient> ingredients = new ArrayList<>();
                        ingredients.add(new Ingredient(nameBox.replaceFirst("\\s+$", ""), amountBox, IngredientTypeConverter.toIngredientType(typeBox), shopList.getId()));

                        // Додавання інгредієнта до списку покупок
                        Disposable disposable = setIngredientFromDishesToShopList(ingredients, null)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(status -> {
                                    if (status) { flagAccessDelete.set(true); }
                                });

                        dialog.dismiss();
                        compositeByIngredients.add(disposable);
                    } else {
                        Toast.makeText(this, R.string.warning_set_all_data, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Метод для обробки натискання кнопки додавання страви.
     * Відкриває діалогове вікно зі списком доступних страв для додавання до списку покупок.
     */
    private void onAddDishButtonClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_items_with_search, null);

        if (dialogView != null) {
            TextView textView = dialogView.findViewById(R.id.textView22);
            if (textView != null) { textView.setText(R.string.your_dish); }
            RecyclerView dishesRecyclerView = dialogView.findViewById(R.id.items_result_check_RecyclerView);
            EditText editText = dialogView.findViewById(R.id.searchEditText);
            ConstraintLayout empty = dialogView.findViewById(R.id.empty);
            if (editText != null) { CharacterLimitTextWatcher.setCharacterLimit(this, editText, Limits.MAX_CHAR_NAME_DISH); }

            if (dishesRecyclerView != null) {
                ArrayList<Boolean> sortStatus = new ArrayList<>();
                sortStatus.add(true);
                sortStatus.add(null);

                // Отримання відсортованих страв
                Disposable disposable = utils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortStatus)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dishes -> {
                                    if (dishes != null) {
                                        if (empty != null) AnotherUtils.visibilityEmptyStatus(empty, dishes.isEmpty());

                                        // Налаштування пошуку та вибору страв
                                        searchController = new SearchController(this, editText, dishesRecyclerView, new ChooseItemAdapter<Dish>(this, (checkBox, item) -> { }));
                                        searchController.setArrayData(new ArrayList<>(dishes));

                                        ChooseItemAdapter<Dish> adapterChooseObjects = (ChooseItemAdapter) searchController.getAdapter();

                                        builder.setView(dialogView)
                                                .setPositiveButton(R.string.button_add, (dialog, which) -> {
                                                    ArrayList<Dish> selectedDish = adapterChooseObjects.getSelectedItem();

                                                    if (!selectedDish.isEmpty()) {
                                                        // Додавання вибраних страв до списку покупок
                                                        Disposable disposable1 = setIDsDishCollectionIntoShopList(selectedDish)
                                                                .subscribeOn(Schedulers.newThread())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(
                                                                        status -> {
                                                                            flagAccessDelete.set(true);

                                                                            if (status) {
                                                                                Toast.makeText(this, getString(R.string.successful_add_dishes), Toast.LENGTH_SHORT).show();
                                                                                Log.d(nameActivity, "Страви успішно додано до списку");
                                                                            } else {
                                                                                Toast.makeText(this, R.string.error_add_dish, Toast.LENGTH_SHORT).show();
                                                                                Log.d(nameActivity, "Помилка додавання страв до колекції(й)");
                                                                            }
                                                                        },
                                                                        throwable -> {
                                                                            Log.e(nameActivity, "Error occurred", throwable);
                                                                            Toast.makeText(this, R.string.error_add_dish, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                );


                                                        compositeDisposable.add(disposable1);
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                                        builder.create().show();
                                    }
                                },
                                throwable -> Log.d(nameActivity, "Помилка отримання страв, які не лежать в колекції")
                        );

                compositeDisposable.add(disposable);
            }
        }
    }

    /**
     * Метод для додавання страв до списку покупків разом з їх інгредиєнтами
     *
     * @param dishes страви
     * @return статус успішності додавання
     */
    private Single<Boolean> setIDsDishCollectionIntoShopList(List<Dish> dishes) {
        return Observable.fromIterable(dishes)
                .flatMapSingle(selectedDish -> utils.ByDishCollection().add(new DishCollection(selectedDish.getId(), shopList.getId()))
                        .flatMap(id_dish_collection -> {
                            if (id_dish_collection > 0) {
                                shopList.addIDDish_Collection(id_dish_collection);

                                return getIngredientsFromDishCollection(id_dish_collection)
                                        .flatMap(ingredients -> setIngredientFromDishesToShopList(ingredients, id_dish_collection));
                            } else { return Single.just(false); }
                        })
                )
                .toList()
                .map(results -> {
                    for (Boolean result : results) {
                        if (!result) { return false; }
                    }
                    return true;
                })
                .onErrorReturnItem(false);
    }

    /**
     * Метод для відображення підказки щодо кнопок списку покупок.
     * Відкриває PopupWindow з підказкою.
     *
     * @param anchorView View, до якого прив'язано PopupWindow.
     */
    private void shopTip(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.tip_shop_list_buttons, null);

        int widthInPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                200,
                getResources().getDisplayMetrics());

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                widthInPixels,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupView.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAsDropDown(anchorView, -75, -anchorView.getHeight() + 200);
    }
}
