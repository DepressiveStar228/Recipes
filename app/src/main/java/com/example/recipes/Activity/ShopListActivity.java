package com.example.recipes.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
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

import com.example.recipes.Adapter.AddChooseObjectsAdapter;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Decoration.CustomSpinnerAdapter;
import com.example.recipes.Adapter.DishGetToShopListAdapter;
import com.example.recipes.Adapter.IngredientShopListGetAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Decoration.HorizontalSpaceItemDecoration;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Dish_Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import android.util.Pair;
import kotlin.Triple;

public class ShopListActivity extends AppCompatActivity {
    private LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4;
    private CompositeDisposable compositeDisposable, compositeByIngredients;
    private PreferencesController preferencesController;
    private DrawerLayout drawerLayout;
    private ConstraintLayout dishShopListLayout;
    private RecipeUtils utils;
    private Collection shopList;
    private TextView name, bought_items, all_item;
    private ConstraintLayout ingredientsEmpty;
    private ImageView addDish, addIngredient, exit, setting;
    private RecyclerView ingShopList, dishShopList;
    private IngredientShopListGetAdapter ingAdapter;
    private DishGetToShopListAdapter dishAdapter;
    private SearchController searchController;
    private String nameActivity;
    private AtomicBoolean flagClear = new AtomicBoolean(false);
    private AtomicBoolean flagAccessDelete = new AtomicBoolean(true);
    private long shopListID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferencesController = new PreferencesController();
        preferencesController.loadPreferences(this);
        utils = new RecipeUtils(this);
        shopList = new Collection("", "");
        compositeDisposable = new CompositeDisposable();
        compositeByIngredients = new CompositeDisposable();
        nameActivity = this.getClass().getSimpleName();

        super.onCreate(savedInstanceState);
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
        shopListID = getIntent().getLongExtra(Config.KEY_SHOP_LIST, -1);

        if (shopListID > 0) {
            utils.ByCollection().getViewModel().getCollectionByID(shopListID).observe(this,
                    data -> {
                        if (data != null && flagAccessDelete.get() && !flagClear.get()) {
                            shopList = data;
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

    private void loadClickListeners(){
        linearLayout1.setOnClickListener(v -> handleEditName());
        linearLayout2.setOnClickListener(v -> handlerCopyAsTest());
        linearLayout3.setOnClickListener(v -> handleClear());
        linearLayout4.setOnClickListener(v -> handleDelete());

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

    private void setDataIntoItemsActivity() {
        if (name != null) { name.setText(shopList.getName()); }
        if (bought_items != null) {
            utils.ByIngredientShopList().getViewModel().getBoughtCountByIdCollection(shopList.getId()).observe(this, data -> {
                if (data != null) {
                    bought_items.setText(data.toString());
                }
            });
        }
        if (all_item != null) {
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
            if (!preferencesController.getStatusUsedTip(Config.TIP_SHOP_LIST_BUTTONS_KEY)) {
                new Handler(Looper.getMainLooper()).post(() -> shopTip(addIngredient));
                preferencesController.useTip(Config.TIP_SHOP_LIST_BUTTONS_KEY);
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

    private void setDishAdapter() {
        if (dishAdapter == null) {
            dishAdapter = new DishGetToShopListAdapter(
                    dish -> {
                        if (flagAccessDelete.get()) {
                            flagAccessDelete.set(false);

                            Disposable disposable = utils.ByDish_Collection().getByData(dish.getId(), shopList.getId())
                                    .flatMapCompletable(dish_collection -> utils.ByDish_Collection().delete(dish_collection))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            () -> flagAccessDelete.set(true)
                                            ,throwable -> flagAccessDelete.set(true)
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

            dishShopList.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                int size = dishShopList.getHeight();
                int maxSize = dpToPx(100);
                ViewGroup.LayoutParams layoutParams = dishShopList.getLayoutParams();

                if (size > maxSize) {
                    layoutParams.height = maxSize;

                } else {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                dishShopList.setLayoutParams(layoutParams);
            });
        }

        utils.ByDish_Collection().getViewModel().getAllDishIDs(shopList.getId()).observe(this, data -> {
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

    private void setIngredientAdapter() {
        if (ingAdapter == null) {
            ingAdapter = new IngredientShopListGetAdapter(
                    this,
                    new IngredientShopListGetAdapter.IngredientShopListClickListener() {
                        @Override
                        public void onIngredientShopListClick(IngredientShopList ingredientShopList) {
                            ingredientShopList.setIsBuy(!ingredientShopList.getIsBuy());
                            Disposable disposable = utils.ByIngredientShopList().update(ingredientShopList)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();

                            compositeDisposable.add(disposable);
                        }

                        @Override
                        public void onDeleteClick(IngredientShopList ingredientShopList) {
                            Disposable disposable = utils.ByIngredientShopList().delete(ingredientShopList)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();

                            compositeDisposable.add(disposable);
                        }
                    }
            );

            ingShopList.setAdapter(ingAdapter);
            ingShopList.setLayoutManager(new LinearLayoutManager(this));
            ingShopList.addItemDecoration(new VerticalSpaceItemDecoration(-4));
            ingShopList.getAdapter().notifyDataSetChanged();
        }

        utils.ByIngredientShopList().getViewModel().getCountByIdCollection(shopList.getId()).observe(this,
                data -> {
                    if (data != null && flagAccessDelete.get() && !flagClear.get()) {
                        putIngredientsWithAmountTypesToAdapter();
                    }
                }
        );

        utils.ByIngredientShopList_AmountType().getViewModel().getAll().observe(this,
                data -> {
                    if (data != null && flagAccessDelete.get() && !flagClear.get()) {
                        putIngredientsWithAmountTypesToAdapter();
                    }
                }
        );
    }

    private Single<Boolean> addIngredientShopList_AmountFromIngredientsShopList(List<IngredientShopList> ingredients, Long id_dish) {
        return Observable.fromIterable(ingredients)
                .flatMapSingle(ingredient -> utils.ByIngredientShopList()
                        .getByNameAndIDCollection(ingredient.getName(), shopList.getId())
                        .flatMap(item -> {
                            if (item.getId() > 0 && item.getId_collection() > 0) {
                                ingredient.setId(item.getId());
                                ingredient.setId_collection(shopList.getId());
                                return utils.ByIngredientShopList_AmountType()
                                        .addAll(utils.ByIngredientShopList().createIngredientShopList_AmountTypesFromGroupedAmountType(ingredient, id_dish));
                            } else {
                                ingredient.setId_collection(shopList.getId());
                                return utils.ByIngredientShopList().add(ingredient, id_dish)
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

    private Single<List<Ingredient>> getIngredientsFromDishCollection(long id_dish_collection) {
        return utils.ByDish_Collection().getByID(id_dish_collection)
                .flatMap(dish_collection -> {
                    if (dish_collection.getId_dish() > 0 && dish_collection.getId_collection() > 0) {
                        return utils.ByIngredient().getAllByIDDish(dish_collection.getId_dish());
                    } else { return Single.just(new ArrayList<Ingredient>()); }
                })
                .flatMap(ingredients -> utils.ByIngredientShopList().filteredBlackList(new ArrayList<>(ingredients)));
    }

    private Single<Boolean> setIngredientFromDishesToShopList(List<Ingredient> ingredients_, Long id_dish_collection) {
        flagAccessDelete.set(false);
        return utils.ByIngredientShopList().convertIngredientsToIngredientsShopList(new ArrayList<>(ingredients_))
                .flatMap(ingredients -> utils.ByIngredientShopList().groupIngredients(ingredients, shopList))
                .flatMap(ingredients -> addIngredientShopList_AmountFromIngredientsShopList(ingredients, id_dish_collection));
    }

    private void putIngredientsWithAmountTypesToAdapter() {
        Disposable disposable = utils.ByIngredientShopList().getAllByIDCollection(shopList.getId())
                .flatMap(ingredients -> {
                    if (flagAccessDelete.get()) {
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

    public void onAddIngredientButtonClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.set_ingredient_shop_list_item, null);
        EditText name = dialogView.findViewById(R.id.nameIngredientEditText);
        EditText amount = dialogView.findViewById(R.id.countIngredientEditText);
        Spinner type = dialogView.findViewById(R.id.spinnerTypeIngredient);

        if (name != null) { CharacterLimitTextWatcher.setCharacterLimit(this, name, Config.CHAR_LIMIT_NAME_INGREDIENT); }
        if (amount != null) { CharacterLimitTextWatcher.setCharacterLimit(this, amount, Config.CHAR_LIMIT_AMOUNT_INGREDIENT); }

        if (type != null) {
            ArrayAdapter<String> typeAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(this.getResources().getStringArray(R.array.options_array)));
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            type.setAdapter(typeAdapter);
        }

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_add, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                if (name != null && amount != null && type != null) {
                    String name_ = name.getText().toString();
                    String amount_ = amount.getText().toString();
                    String type_ = type.getSelectedItem().toString();

                    if (!name_.isEmpty() && !amount_.isEmpty() && !type_.isEmpty()) {
                        ArrayList<Ingredient> ingredients = new ArrayList<>();
                        ingredients.add(new Ingredient(name_.replaceFirst("\\s+$", ""), amount_, type_, shopList.getId()));

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

    private void onAddDishButtonClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_items_with_search, null);

        if (dialogView != null) {
            TextView textView = dialogView.findViewById(R.id.textView22);
            if (textView != null) { textView.setText(R.string.your_dish); }
            RecyclerView dishesRecyclerView = dialogView.findViewById(R.id.items_result_check_RecyclerView);
            EditText editText = dialogView.findViewById(R.id.search_edit_text);
            if (editText != null) { CharacterLimitTextWatcher.setCharacterLimit(this, editText, 30); }

            if (dishesRecyclerView != null) {
                ArrayList<Boolean> sortStatus = new ArrayList<>();
                sortStatus.add(true);
                sortStatus.add(null);

                Disposable disposable = utils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortStatus)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dishes -> {
                                    if (dishes != null && !dishes.isEmpty()) {
                                        searchController = new SearchController(this, editText, dishesRecyclerView, (checkBox, selectedItem, item) -> {
                                            if (!checkBox.isChecked()) {
                                                selectedItem.add(item);
                                                checkBox.setChecked(true);
                                            } else {
                                                selectedItem.remove(item);
                                                checkBox.setChecked(false);
                                            }
                                        });

                                        searchController.setArrayData(new ArrayList<>(dishes));

                                        AddChooseObjectsAdapter adapterChooseObjects = (AddChooseObjectsAdapter) searchController.getAdapter();

                                        builder.setView(dialogView)
                                                .setPositiveButton(R.string.button_add, (dialog, which) -> {
                                                    ArrayList<Object> selectedDishIds = adapterChooseObjects.getSelectedItem();
                                                    ArrayList<Long> selectedDishLongIds = new ArrayList<>();
                                                    for (Object dish : selectedDishIds) {
                                                        selectedDishLongIds.add(((Dish)dish).getId());
                                                    }

                                                    if (!selectedDishIds.isEmpty()) {
                                                        Disposable disposable1 = utils.ByDish().getAll(selectedDishLongIds)
                                                                .flatMap(this::setIDsDishCollectionIntoShopList)
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
                                throwable -> {
                                    Log.d(nameActivity, "Помилка отримання страв, які не лежать в колекції");
                                }
                        );

                compositeDisposable.add(disposable);
            }
        }
    }

    private Single<Boolean> setIDsDishCollectionIntoShopList(List<Dish> dishes) {
        return Observable.fromIterable(dishes)
                .flatMapSingle(selectedDish -> utils.ByDish_Collection().add(new Dish_Collection(selectedDish.getId(), shopList.getId()))
                        .flatMap(id_dish_collection -> {
                            shopList.addIDDish_Collection(id_dish_collection);
                            if (id_dish_collection > 0) {
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

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void handleEditName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_collection, null);
        EditText editText = dialogView.findViewById(R.id.edit_collection_name_editText);
        editText.setText(shopList.getName());
        CharacterLimitTextWatcher.setCharacterLimit(this, editText, Config.CHAR_LIMIT_NAME_COLLECTION);
        builder.setView(dialogView)
                .setTitle(R.string.edit_shop_list)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();

                    if (collectionName.isEmpty()) {
                        Disposable disposable = utils.ByCollection().generateUniqueNameForShopList()
                                .flatMap(name -> {
                                    shopList.setName(name);
                                    shopList.setType(Config.SHOP_LIST_TYPE);
                                    return utils.ByCollection().updateAndGet(shopList);
                                })
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(collection_ -> {
                                    if (collection_ != null && collection_.getId() != -1) {
                                        Toast.makeText(this, R.string.successful_edit_collection, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                    }
                                });
                        compositeDisposable.add(disposable);
                    } else if (!collectionName.equals(shopList.getName())) {
                        Disposable disposable = utils.ByCollection().getIdByNameAndType(collectionName, Config.SHOP_LIST_TYPE)
                                .flatMap(id -> {
                                            if (id != -1) {
                                                Toast.makeText(this, R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                                return Single.error(new Exception(getString(R.string.warning_dublicate_name_collection)));
                                            } else {
                                                shopList.setName(collectionName);
                                                return utils.ByCollection().update(shopList).toSingleDefault(shopList);
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
                                        updatedCollection -> {
                                            if (name != null) {
                                                name.setText(updatedCollection.getName());
                                            }
                                            Toast.makeText(this, R.string.successful_edit_collection, Toast.LENGTH_SHORT).show();
                                        },
                                        throwable -> {
                                            if (throwable.getMessage() != null && throwable.getMessage().equals(getString(R.string.warning_dublicate_name_collection))) {
                                                Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );

                        compositeDisposable.add(disposable);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void handleDelete() {
        flagAccessDelete.set(false);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_shop_list))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().delete(shopList)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(this, R.string.successful_delete_shop_list, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    private void handleClear() {
        flagClear.set(true);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_clear_shop_list))
                .setMessage(getString(R.string.warning_delete_shop_list))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().clear(shopList)
                            .flatMap(status -> utils.ByCollection().getByID(shopListID))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(collection -> {
                                if (collection != null) {
                                    shopList = collection;
                                    setDataIntoItemsActivity();
                                    putIngredientsWithAmountTypesToAdapter();

                                    Toast.makeText(this, R.string.successful_clear_shop_list, Toast.LENGTH_SHORT).show();
                                    flagClear.set(false);
                                }
                            });
                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }


    private void handlerCopyAsTest() {
        String text = "";
        text = text + getString(R.string.list) + " " + shopList.getName() + "\n";

        for (IngredientShopList ing : shopList.getIngredients()) {
            if (!ing.getIsBuy()) {
                text = text + "   - " + ing.getName() + ": " + ing.getGroupedAmountTypeToString() + "\n";
            }
        }

        text.substring(0, text.length() - 1);

        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }
}
