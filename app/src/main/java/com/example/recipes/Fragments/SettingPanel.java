package com.example.recipes.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.AddChooseObjectsAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Decoration.CustomSpinnerAdapter;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingPanel {
    private DrawerLayout drawerLayout;
    private LinearLayout importLayout, exportLayout;
    private TextView ingredientBlackList;
    private PreferencesController preferencesController;
    private RecipeUtils utils;
    private Spinner languageSpinner, themeSpinner, paletteSpinner;
    private Switch status_ing_hints_Switch;
    private String[] languageArray, themeArray, paletteArray;
    private Button confirmButton;
    private String selectedLanguage, selectedTheme, selectedPalette;
    private String nameActivity;
    private SearchController searchController;
    private CompositeDisposable compositeDisposable;

    public SettingPanel(@NonNull Activity activity, View rootView) {
        try {
            this.drawerLayout = (DrawerLayout) rootView;
        } catch (Exception e) {}
        preferencesController = new PreferencesController();
        preferencesController.loadPreferences(activity);
        utils = new RecipeUtils(activity);
        nameActivity = activity.getClass().getSimpleName();
        compositeDisposable = new CompositeDisposable();
        loadLayoutItems(activity, rootView);
        loadClickListeners(activity);
    }

    private void loadLayoutItems(Activity activity, View rootView) {
        if (rootView != null) {
            NavigationView navigationView = rootView.findViewById(R.id.navigationView);

            if (navigationView != null) {
                View headerView = navigationView.getHeaderView(0);

                if (headerView != null) {
                    languageSpinner = headerView.findViewById(R.id.language_spinner);
                    themeSpinner = headerView.findViewById(R.id.theme_spinner);
                    paletteSpinner = headerView.findViewById(R.id.palette_spinner);
                    status_ing_hints_Switch = headerView.findViewById(R.id.ingredient_hints_switch);
                    ingredientBlackList = headerView.findViewById(R.id.ingredient_black_listText);
                    confirmButton = headerView.findViewById(R.id.confirm_button);
                    importLayout = headerView.findViewById(R.id.importContainer);
                    exportLayout = headerView.findViewById(R.id.exportContainer);
                }

                if (languageSpinner != null) {
                    ArrayAdapter<String> languageAdapter = new CustomSpinnerAdapter(activity, android.R.layout.simple_spinner_item, Arrays.asList(activity.getResources().getStringArray(R.array.language_options)));
                    languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    languageSpinner.setAdapter(languageAdapter);
                    languageSpinner.setSelection(preferencesController.getIndexLanguage());
                }

                if (themeSpinner != null) {
                    ArrayAdapter<String> themeAdapter = new CustomSpinnerAdapter(activity, android.R.layout.simple_spinner_item, Arrays.asList(activity.getResources().getStringArray(R.array.theme_options)));
                    themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    themeSpinner.setAdapter(themeAdapter);
                    themeSpinner.setSelection(preferencesController.getIndexTheme());
                }

                if (paletteSpinner != null) {
                    ArrayAdapter<String> paletteAdapter = new CustomSpinnerAdapter(activity, android.R.layout.simple_spinner_item, Arrays.asList(activity.getResources().getStringArray(R.array.palette_options)));
                    paletteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    paletteSpinner.setAdapter(paletteAdapter);
                    paletteSpinner.setSelection(preferencesController.getIndexPalette());
                }

                if (status_ing_hints_Switch != null) { status_ing_hints_Switch.setChecked(preferencesController.getStatus_ing_hints()); }

                if (ingredientBlackList != null) {
                    ingredientBlackList.setOnClickListener(v -> {
                        blackListOpen(activity);
                    });
                }

                languageArray = preferencesController.getStringArrayForLocale(R.array.language_values, "en");
                themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options,"en");
                paletteArray = preferencesController.getStringArrayForLocale(R.array.palette_options, "en");

                Log.d(nameActivity, "Завантаження всіх об'єктів налаштувань");
            }
        }
    }

    private void loadClickListeners(Activity activity) {
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguage = preferencesController.getLanguageNameBySpinner(position);
                Log.d(nameActivity, "Слухач помітив зміну мови");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLanguage = languageArray[0];
            }
        });

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTheme = preferencesController.getThemeNameBySpinner(position);
                Log.d(nameActivity, "Слухач помітив зміну теми");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTheme = themeArray[0];
            }
        });

        paletteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPalette = preferencesController.getPaletteNameBySpinner(position);
                Log.d(nameActivity, "Слушатель заметил смену палитры");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPalette = paletteArray[0];
            }
        });

        confirmButton.setOnClickListener(v -> {
            Log.d(nameActivity, "Слухач помітив підтвердження налаштувань");
            preferencesController.setLocale(selectedLanguage);
            preferencesController.setAppTheme(selectedTheme, selectedPalette);
            preferencesController.savePreferences(selectedLanguage, selectedTheme, selectedPalette);
            preferencesController.savePreferences(status_ing_hints_Switch.isChecked());
            drawerLayout.closeDrawer(GravityCompat.END);

            if (Build.VERSION.SDK_INT >= 11) {
                Log.d(nameActivity, "Рестарт активності через recreate()");
                activity.recreate();
            } else {
                Log.d(nameActivity, "Рестарт активності через Intent");
                Intent intent = activity.getIntent();
                activity.finish();
                activity.startActivity(intent);
            }

            Log.d(nameActivity, "Рестарт активності завершено");
        });

        exportLayout.setOnClickListener(v -> {
            Disposable disposable = utils.ByDish().getAll()
                    .flatMap(dishes -> utils.getListPairDishIng(dishes))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            list -> {
                                if (!list.isEmpty()) {
                                    new AlertDialog.Builder(activity)
                                            .setTitle(activity.getString(R.string.confirm_export))
                                            .setMessage(activity.getString(R.string.warning_export))
                                            .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                                                DataBox recipeData = new DataBox();

                                                for (Pair<Dish, ArrayList<Ingredient>> pair : list) {
                                                    recipeData.addRecipe(pair);
                                                }

                                                ImportExportController.exportRecipeData(activity, recipeData, new ExportCallbackUri() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        if (uri != null) {
                                                            FileUtils.sendFileByUri(activity, uri);
                                                            Log.d("ImportExportController", "Рецепти успішно експортовані");
                                                        }

                                                        FileUtils.deleteFileByUri(activity, uri);
                                                    }

                                                    @Override
                                                    public void onError(Throwable throwable) {
                                                        Toast.makeText(activity, activity.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                                        Log.e("ImportExportController", "Помилка при експорті рецептів", throwable);
                                                    }

                                                    @Override
                                                    public void getDisposable(Disposable disposable) {}
                                                });
                                            })
                                            .setNegativeButton(activity.getString(R.string.no), null).show();
                                } else {
                                    Toast.makeText(activity, activity.getString(R.string.error_void_dish), Toast.LENGTH_SHORT).show();
                                    Log.d("ImportExportController", "Рецепти успішно імпортовані із файлу.");
                                }
                            },
                            throwable -> {
                                Log.d("ImportExportController", "Помилка отримання страв та інгредієнтів");
                            }
                    );
            compositeDisposable.add(disposable);
        });

        importLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/json");
            activity.startActivityForResult(intent, 1);
            Log.d(nameActivity, "Читання з файлу");
        });
    }

    public void onClickSetting() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            openDrawer();
            languageSpinner.setSelection(preferencesController.getIndexLanguage());
            themeSpinner.setSelection(preferencesController.getIndexTheme());
            paletteSpinner.setSelection(preferencesController.getIndexPalette());
            status_ing_hints_Switch.setChecked(preferencesController.getStatus_ing_hints());
        }
    }

    public boolean isDrawerOpen() {
        if (drawerLayout != null) {
            return drawerLayout.isDrawerOpen(GravityCompat.END);
        }
        return false;
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.END);
            Log.d(nameActivity, "Відкриття налаштувань");
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.END);
            Log.d(nameActivity, "Закриття налаштувань");
        }
    }

    public void clearDisposables() { compositeDisposable.clear(); }

    private void blackListOpen(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_items_with_search, null);

        if (dialogView != null) {
            TextView textView = dialogView.findViewById(R.id.textView22);
            if (textView != null) { textView.setText(R.string.your_dish); }
            RecyclerView ingredientsRecyclerView = dialogView.findViewById(R.id.items_result_check_RecyclerView);
            EditText editText = dialogView.findViewById(R.id.search_edit_text);
            if (editText != null) { CharacterLimitTextWatcher.setCharacterLimit(activity, editText, 30); }

            if (ingredientsRecyclerView != null) {
                Disposable disposable = Single.zip(
                                utils.ByIngredient().getNamesUnique(),
                                utils.ByIngredientShopList().getAllNamesByBlackList(),
                                Pair::new
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(data -> {
                            if (data.first != null && data.second != null) {
                                searchController = new SearchController(activity, editText, ingredientsRecyclerView, (checkBox, selectedItem, item) -> {
                                    if (!checkBox.isChecked()) {
                                        selectedItem.add(item);
                                        checkBox.setChecked(true);
                                    } else {
                                        selectedItem.remove(item);
                                        checkBox.setChecked(false);
                                    }
                                });

                                searchController.setArrayData(new ArrayList<>(data.first));
                                searchController.setArraySelectedData(new ArrayList<>(data.second));
                                searchController.setSearchEditText(editText);
                                searchController.setSearchResultsRecyclerView(ingredientsRecyclerView);


                                AddChooseObjectsAdapter adapterChooseObjects = (AddChooseObjectsAdapter) searchController.getAdapter();

                                builder.setView(dialogView)
                                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                                            ArrayList<Object> selectedIngredientIds = adapterChooseObjects.getSelectedItem();
                                            ArrayList<String> selectedIngredients = new ArrayList<>();
                                            for (Object ingredient : selectedIngredientIds) {
                                                selectedIngredients.add((String)ingredient);
                                            }

                                            Disposable disposable1 = updateBlackList(data.first, selectedIngredients)
                                                    .subscribeOn(Schedulers.newThread())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(
                                                            status -> {
                                                                if (status) {
                                                                    Toast.makeText(activity, activity.getString(R.string.successfully_made_changes), Toast.LENGTH_SHORT).show();
                                                                    Log.d(nameActivity, "Інгедієнти успішно додано до чорного списку");
                                                                } else {
                                                                    Toast.makeText(activity, activity.getString(R.string.error_add_ingedients), Toast.LENGTH_SHORT).show();
                                                                    Log.d(nameActivity, "Помилка додавання інгредиєнтів до чорного списку");
                                                                }
                                                            },
                                                            throwable -> {
                                                                Toast.makeText(activity, activity.getString(R.string.error_add_ingedients), Toast.LENGTH_SHORT).show();
                                                                Log.d(nameActivity, "Помилка додавання інгредиєнтів до чорного списку");
                                                            }
                                                    );


                                            compositeDisposable.add(disposable1);
                                        })
                                        .setNeutralButton(R.string.reset, (dialog, which) -> {
                                            Disposable disposable1 = utils.ByIngredientShopList().getAllByBlackList()
                                                    .flatMap(ingredientShopLists -> utils.ByIngredientShopList().deleteAll(new ArrayList<>(ingredientShopLists)))
                                                    .subscribeOn(Schedulers.newThread())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                        if (status) {
                                                            Toast.makeText(activity, activity.getString(R.string.successful_reset), Toast.LENGTH_SHORT).show();
                                                            Log.d(nameActivity, "Успішно скинуто інгредієнти blacklist");
                                                        } else {
                                                            Toast.makeText(activity, activity.getString(R.string.error_reset), Toast.LENGTH_SHORT).show();
                                                            Log.d(nameActivity, "Помилка скидання інгредієнтів blacklist");
                                                        }
                                                    });

                                            compositeDisposable.add(disposable1);
                                        })
                                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                                builder.create().show();
                            }
                        });

                compositeDisposable.add(disposable);
            }
        }
    }

    private Single<Boolean> updateBlackList(List<String> oldList, List<String> newList) {
        ArrayList<String> addedItems = new ArrayList<>(newList);
        addedItems.removeAll(oldList);

        ArrayList<String> removedItems = new ArrayList<>(oldList);
        removedItems.removeAll(newList);

        return Single.zip(
                        utils.ByIngredientShopList().addAll(Config.ID_BLACK_LIST, addedItems),
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

    private Single<Boolean> removedIngredients(ArrayList<String> removedItems) {
        return Observable.fromIterable(removedItems)
                .flatMapSingle(item -> utils.ByIngredientShopList().getByNameAndIDCollection(item, Config.ID_BLACK_LIST)
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
