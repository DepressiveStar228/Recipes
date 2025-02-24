//package com.example.recipes.Activity;
//
//import android.app.AlertDialog;
//import android.content.ClipData;
//import android.content.ClipboardManager;
//import android.content.Context;
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.graphics.drawable.AnimationDrawable;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.core.content.ContextCompat;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.recipes.AI.ChatGPT;
//import com.example.recipes.AI.ChatGPTClient;
//import com.example.recipes.Adapter.AddDishToCollectionsAdapter;
//import com.example.recipes.Adapter.IngredientGetAdapter;
//import com.example.recipes.Config;
////import com.example.recipes.AI.ChatGPTTranslate;
//import com.example.recipes.Controller.TranslateController;
//import com.example.recipes.Decoration.AnimationUtils;
//import com.example.recipes.Decoration.TextLoadAnimation;
//import com.example.recipes.Interface.ExportCallbackUri;
//import com.example.recipes.Controller.ImportExportController;
//import com.example.recipes.Controller.PreferencesController;
//import com.example.recipes.Item.DataBox;
//import com.example.recipes.Item.Dish;
//import com.example.recipes.Utils.AnotherUtils;
//import com.example.recipes.Utils.FileUtils;
//import com.example.recipes.Item.Ingredient;
//import com.example.recipes.Utils.RecipeUtils;
//import com.example.recipes.R;
//import com.google.android.material.navigation.NavigationView;
//import com.google.gson.Gson;
//
//import org.json.JSONException;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Locale;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
//import io.reactivex.rxjava3.core.Single;
//import io.reactivex.rxjava3.disposables.CompositeDisposable;
//import io.reactivex.rxjava3.disposables.Disposable;
//import io.reactivex.rxjava3.schedulers.Schedulers;
//
//public class ReadDataDishActivity extends AppCompatActivity {
//    private Dish originalDish, translateDish;
//    private IngredientGetAdapter ingredientGetAdapter;
//    private ChatGPTClient client;
//    private DrawerLayout drawerLayout;
//    private RecyclerView ingredientRecyclerView;
//    private ImageView back, setting;
//    private TextView nameDish, translate, recipeText, portionText;
//    private ConstraintLayout ingredientsEmpty, recipeTextEmpty, portionTextEmpty;
//    private LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4, linearLayout5;
//    private String nameActivity;
//    private RecipeUtils utils;
//    private TranslateController translateController;
//    private CompositeDisposable compositeDisposable;
//    private AtomicBoolean flagInitializationGPTClient = new AtomicBoolean(false);
//    private AtomicBoolean flagIsTranslated = new AtomicBoolean(false);
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        PreferencesController preferencesController = new PreferencesController();
//        preferencesController.loadPreferences(this);
//        client = new ChatGPTClient(this, ChatGPT.TRANSLATOR);
//        translateController = new TranslateController(this);
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.read_data_item_activity);
//
//        nameActivity = this.getClass().getSimpleName();
//        utils = new RecipeUtils(this);
//        originalDish = new Dish("", "");
//        translateDish = new Dish("", "");
//        compositeDisposable = new CompositeDisposable();
//
//        loadItemsActivity();
//        loadClickListeners();
//        setChatGPTClient();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        long dishID = getIntent().getLongExtra(Config.KEY_DISH, -1);
//
//        if (dishID != -1) {
//            utils.ByDish().getViewModel().getByID(dishID).observe(this, data -> {
//                if (data != null) {
//                    originalDish = data;
//                    setDataIntoItemsActivity(originalDish);
//                }
//            });
//
//            utils.ByIngredient().getViewModel().getAllByIDDish(dishID).observe(this, data -> {
//                if (data != null) {
//                    originalDish.setIngredients(new ArrayList<>(data));
//                    setDataIntoItemsActivity(originalDish);
//                }
//            });
//        } else {
//            Toast.makeText(this, getString(R.string.error_read_get_dish), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
//            drawerLayout.closeDrawer(GravityCompat.END);
//        } else { super.onBackPressed(); }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        compositeDisposable.clear();
//        Log.d(nameActivity, "Активність успішно закрита");
//    }
//
//    private void loadItemsActivity() {
//        ingredientRecyclerView = findViewById(R.id.ingredientReadRecyclerView);
//
//        drawerLayout = findViewById(R.id.readDrawerLayout);
//        NavigationView navigationView = findViewById(R.id.readNavigationView);
//
//        if (navigationView != null) {
//            View headerView = navigationView.getHeaderView(0);
//
//            if (headerView != null) {
//                linearLayout1 = headerView.findViewById(R.id.setting_dish_edit);
//                linearLayout2 = headerView.findViewById(R.id.setting_dish_delete);
//                linearLayout3 = headerView.findViewById(R.id.setting_dish_share);
//                linearLayout4 = headerView.findViewById(R.id.setting_dish_add_to_collection);
//                linearLayout5 = headerView.findViewById(R.id.setting_dish_copy_as_text);
//            }
//        }
//
//        back = findViewById(R.id.back_read_dish_imageView);
//        setting = findViewById(R.id.imageSettingButton);
//        nameDish = findViewById(R.id.nameDishTextView);
//        translate = findViewById(R.id.read_data_translate_btn);
//        recipeText = findViewById(R.id.readRecipeTextView);
//        portionText = findViewById(R.id.portionShipTextView);
//
//        ingredientsEmpty = findViewById(R.id.ingredientEmpty);
//        recipeTextEmpty = findViewById(R.id.recipeEmpty);
//        portionTextEmpty = findViewById(R.id.portionShipEmpty);
//    }
//
//    private void loadClickListeners() {
//        if (linearLayout1 != null) { linearLayout1.setOnClickListener(v -> editDish()); }
//        if (linearLayout2 != null) { linearLayout2.setOnClickListener(v -> deleteDish()); }
//        if (linearLayout3 != null) { linearLayout3.setOnClickListener(v -> shareDish()); }
//        if (linearLayout4 != null) { linearLayout4.setOnClickListener(v -> showAddDishInCollectionDialog()); }
//        if (linearLayout5 != null) { linearLayout5.setOnClickListener(v -> copy_as_text()); }
//
//        if (back != null) { back.setOnClickListener(v -> finish()); }
//        if (setting != null) { setting.setOnClickListener(this::onClickSettingDish); }
//
//        if (translate != null) {
//            translate.setOnClickListener(v -> {
//                if (!flagIsTranslated.get()) {
//                    if (translateDish.getName().isEmpty()) {
//                        onTranslationDishClick();
//                    } else {
//                        flagIsTranslated.set(true);
//                        setDataIntoItemsActivity(translateDish);
//                        translate.setText(getString(R.string.translate_button_text_true));
//                    }
//                } else {
//                    flagIsTranslated.set(false);
//                    setDataIntoItemsActivity(originalDish);
//                    translate.setText(getString(R.string.translate_button_text_false));
//                    Log.d(nameActivity, "Показ оригіналу страви");
//                }
//            });
//        }
//    }
//
//    private void setChatGPTClient() {
//        if (client == null) {
//            client = new ChatGPTClient(this, ChatGPT.TRANSLATOR);
//
//            Disposable disposable = client.initialization()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            status -> {
//                                flagInitializationGPTClient.set(true);
//                            },
//                            throwable -> {
//                                Log.e(nameActivity, "Initialization failed: " + throwable.getMessage());
//                                throwable.printStackTrace();
//                            }
//                    );
//            compositeDisposable.add(disposable);
//        }
//    }
//
//    private void onTranslationDishClick() {
//        DataBox dataBox = new DataBox();
//        dataBox.addRecipe(originalDish, originalDish.getIngredients());
//
//        if (translateController != null) {
//            TextLoadAnimation textLoadAnimation = new TextLoadAnimation(translate, getString(R.string.loading));
//            textLoadAnimation.startAnimation();
//
//            Disposable disposable = translateController.translateRecipe(dataBox)
//                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(data -> {
//                        if (data != null && !data.getBox().isEmpty()) {
//                            translateDish = data.getBox().get(0).first;
//                            translateDish.setId(originalDish.getId());
//                            translateDish.setIngredients(data.getBox().get(0).second);
//                            setDataIntoItemsActivity(translateDish);
//
//                            flagIsTranslated.set(true);
//                            textLoadAnimation.setBaseText(getString(R.string.translate_button_text_true));
//                            textLoadAnimation.stopAnimation();
//                        }
//                        else {
//                            flagIsTranslated.set(false);
//                            textLoadAnimation.setBaseText(getString(R.string.translate_button_text_false));
//                            textLoadAnimation.stopAnimation();
//                        }
//                    });
//            compositeDisposable.add(disposable);
//        }
//    }
//
//    private void setDataIntoItemsActivity(Dish dish) {
//        if (nameDish != null) { nameDish.setText(dish.getName()); }
//        if (ingredientRecyclerView != null) { setIngredientAdapter(dish); }
//        if (portionText != null && dish.getPortion() > 0) { portionText.setText(String.valueOf(dish.getPortion())); }
//        if (ingredientsEmpty != null) { AnotherUtils.visibilityEmptyStatus(ingredientsEmpty, dish.getIngredients().isEmpty()); }
//        if (recipeTextEmpty != null) { AnotherUtils.visibilityEmptyStatus(recipeTextEmpty, dish.getRecipe().isEmpty()); }
//        if (portionTextEmpty != null) { AnotherUtils.visibilityEmptyStatus(portionTextEmpty, dish.getPortion() <= 0); }
//    }
//
//    private void setIngredientAdapter(Dish dish) {
//        if (ingredientGetAdapter == null) {
//            ingredientGetAdapter = new IngredientGetAdapter(this, new ArrayList<>());
//        }
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//
//        ingredientRecyclerView.setAdapter(ingredientGetAdapter);
//        ingredientRecyclerView.setLayoutManager(linearLayoutManager);
//        ingredientRecyclerView.setHasFixedSize(true);
//        ingredientRecyclerView.setNestedScrollingEnabled(false);
//
//        ingredientGetAdapter.clear();
//        ingredientGetAdapter.addAll(dish.getIngredients());
//        recipeText.setText(dish.getRecipe());
//    }
//
//    private void deleteDish() {
//        new AlertDialog.Builder(this)
//                .setTitle(getString(R.string.confirm_delete_dish))
//                .setMessage(getString(R.string.warning_delete_dish))
//                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
//                    Disposable disposable = utils.ByDish().delete(originalDish)
//                            .subscribeOn(Schedulers.newThread())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(
//                                    () -> {
//                                        Toast.makeText(this, getString(R.string.successful_delete_dish), Toast.LENGTH_SHORT).show();
//                                        finish();
//                                    },
//                                    throwable -> {
//                                        Toast.makeText(this, getString(R.string.error_delete_dish), Toast.LENGTH_SHORT).show();
//                                        finish();
//                                    }
//                            );
//
//                    compositeDisposable.add(disposable);
//                })
//                .setNegativeButton(getString(R.string.no), null).show();
//    }
//
//    private void editDish() {
//        Intent intent = new Intent(this, EditorDishActivity.class);
//        intent.putExtra(Config.KEY_DISH, originalDish.getId());
//        startActivity(intent);
//    }
//
//    private void copy_as_text() {
//        String ingredientsText = "";
//
//        for (Ingredient ingredient : originalDish.getIngredients()){
//            String ingredientText = ingredient.getName() + "  " +
//                    ingredient.getAmount() + ingredient.getType() + '\n';
//            ingredientsText = ingredientsText + ingredientText;
//        }
//
//        String text = originalDish.getName() + "\n\n" + getString(R.string.ingredients) + "\n" + ingredientsText + "\n" + getString(R.string.recipe) + "\n" + originalDish.getRecipe();
//
//        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData clip = ClipData.newPlainText("label", text);
//        clipboard.setPrimaryClip(clip);
//
//        Toast.makeText(this, getString(R.string.copy_clipboard_text), Toast.LENGTH_SHORT).show();
//    }
//
//    private void shareDish() {
//        if (originalDish != null) {
//            new AlertDialog.Builder(this)
//                    .setTitle(getString(R.string.confirm_export))
//                    .setMessage(getString(R.string.warning_export))
//                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
//                        ImportExportController.exportDish(this, originalDish, new ExportCallbackUri() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                if (uri != null) {
//                                    FileUtils.sendFileByUri(ReadDataDishActivity.this, uri);
//                                    FileUtils.deleteFileByUri(ReadDataDishActivity.this, uri);
//                                    Log.d(nameActivity, "Рецепт успішно експортовано");
//                                }
//                            }
//
//                            @Override
//                            public void onError(Throwable throwable) {
//                                Toast.makeText(ReadDataDishActivity.this, ReadDataDishActivity.this.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
//                                Log.e("ImportExportController", "Помилка при експорті рецептів", throwable);
//                            }
//
//                            @Override
//                            public void getDisposable(Disposable disposable) {
//                                compositeDisposable.add(disposable);
//                            }
//                        });
//                    })
//                    .setNegativeButton(getString(R.string.no), null).show();
//        } else {
//            Toast.makeText(this, R.string.error_read_get_dish, Toast.LENGTH_SHORT).show();
//            Log.d(nameActivity, "Помилка. Страва порожня");
//        }
//    }
//
//    private void showAddDishInCollectionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = this.getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
//        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);
//
//        Disposable disposable = utils.ByCollection().getUnusedInDish(originalDish)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        unused_collections -> {
//                            if (unused_collections != null && !unused_collections.isEmpty()) {
//                                AddDishToCollectionsAdapter adapter = new AddDishToCollectionsAdapter(this, unused_collections);
//                                collectionsRecyclerView.setAdapter(adapter);
//                                collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//                                builder.setView(dialogView)
//                                        .setPositiveButton(R.string.button_add, (dialog, which) -> {
//                                            ArrayList<Long> selectedCollectionIds = adapter.getSelectedCollectionIds();
//                                            if (!selectedCollectionIds.isEmpty()){
//                                                Disposable disposable1 = utils.ByDish_Collection().addAll(originalDish, selectedCollectionIds)
//                                                        .subscribeOn(Schedulers.newThread())
//                                                        .observeOn(AndroidSchedulers.mainThread())
//                                                        .subscribe(
//                                                                result -> {
//                                                                    if (result) {
//                                                                        Toast.makeText(this, getString(R.string.successful_add_dish_in_collection), Toast.LENGTH_SHORT).show();
//                                                                        Log.d(nameActivity, "Страва успішно додана в колекцію(ї)");
//                                                                        selectedCollectionIds.clear();
//                                                                    } else {
//                                                                        Log.e(nameActivity, "Помилка. Страва не додана в колекцію(ї)");
//                                                                        selectedCollectionIds.clear();
//                                                                    }
//                                                                },
//                                                                throwable -> {
//                                                                    Log.e(nameActivity, "Помилка додавання страви в колекцію(ї)", throwable);
//                                                                    selectedCollectionIds.clear();
//                                                                }
//                                                        );
//                                                compositeDisposable.add(disposable1);
//                                            }
//                                        })
//                                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
//
//                                builder.create().show();
//                            } else {
//                                Log.e(nameActivity, "Помилка. Список колекцій пустий");
//                            }
//                        },
//                        throwable -> {
//                            Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
//                            Log.e(nameActivity, "Помилка отримання списку колекцій з бд", throwable);
//                        }
//                );
//        compositeDisposable.add(disposable);
//    }
//
//    private void onClickSettingDish(View view) {
//        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
//            drawerLayout.closeDrawer(GravityCompat.END);
//        } else {
//            drawerLayout.openDrawer(GravityCompat.END);
//        }
//    }
//}
//
