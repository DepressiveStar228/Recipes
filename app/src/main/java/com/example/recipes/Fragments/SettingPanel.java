package com.example.recipes.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Decoration.CustomSpinnerAdapter;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.R;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для управління панеллю налаштувань додатка.
 */
public class SettingPanel {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout importLayout, exportLayout, deleteDBLayout;
    private PreferencesController preferencesController;
    private RecipeUtils utils;
    private Spinner languageSpinner, themeSpinner, paletteSpinner;
    private String[] languageArray, themeArray, paletteArray;
    private Button confirmButton;
    private String selectedLanguage, selectedTheme, selectedPalette;
    private String nameActivity;
    private ImportExportController importExportController;
    private CompositeDisposable compositeDisposable;

    /**
     * Конструктор класу SettingPanel
     * @param activity Поточна активність
     * @param rootView Кореневий View, що містить DrawerLayout
     */
    public SettingPanel(@NonNull Activity activity, View rootView) {
        try {
            this.drawerLayout = (DrawerLayout) rootView;
        } catch (Exception e) {}
        preferencesController = PreferencesController.getInstance();
        utils = RecipeUtils.getInstance(activity);
        importExportController = new ImportExportController(activity);
        nameActivity = activity.getClass().getSimpleName();
        compositeDisposable = new CompositeDisposable();
        loadLayoutItems(activity, rootView); // Завантаження UI елементів
        loadClickListeners(activity);        // Налаштування обробників подій
    }

    /**
     * Завантажує UI елементи панелі налаштувань
     * @param activity Поточна активність
     * @param rootView Кореневий View
     */
    private void loadLayoutItems(Activity activity, View rootView) {
        if (rootView != null) {
            navigationView = rootView.findViewById(R.id.navigationView);

            if (navigationView != null) {
                View headerView = navigationView.getHeaderView(0);

                if (headerView != null) {
                    languageSpinner = headerView.findViewById(R.id.language_spinner);
                    themeSpinner = headerView.findViewById(R.id.theme_spinner);
                    paletteSpinner = headerView.findViewById(R.id.palette_spinner);
                    confirmButton = headerView.findViewById(R.id.confirm_button);
                    importLayout = headerView.findViewById(R.id.importContainer);
                    exportLayout = headerView.findViewById(R.id.exportContainer);
                    deleteDBLayout = headerView.findViewById(R.id.deleteAllDBContainer);
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

                languageArray = preferencesController.getStringArrayForLocale(R.array.language_values, "en");
                themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options,"en");
                paletteArray = preferencesController.getStringArrayForLocale(R.array.palette_options, "en");

                Log.d(nameActivity, "Завантаження всіх об'єктів налаштувань");
            }
        }
    }

    /**
     * Налаштовує обробники подій для елементів панелі налаштувань
     * @param activity Поточна активність
     */
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
            preferencesController.setLocale(selectedLanguage, activity);
            preferencesController.savePreferences(selectedLanguage, selectedTheme, selectedPalette);
            drawerLayout.closeDrawer(GravityCompat.END);

            // Перезапуск активності для застосування змін
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
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dishes -> {
                                if (!dishes.isEmpty()) {
                                    // Підтвердження експорту
                                    new AlertDialog.Builder(activity)
                                            .setTitle(activity.getString(R.string.confirm_export))
                                            .setMessage(activity.getString(R.string.warning_export))
                                            .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) -> {
                                                closeDrawer();

                                                // Виконання експорту
                                                importExportController.exportRecipeData(activity, dishes, new ExportCallbackUri() {
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

        // Обробник імпорту даних
        importLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            activity.startActivityForResult(intent, 1);
            closeDrawer();
            Log.d(nameActivity, "Читання з файлу");
        });

        // Обробник очищення бази даних
        if (deleteDBLayout != null) {
            deleteDBLayout.setOnClickListener(v -> new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.confirm_clear_db))
                    .setMessage(activity.getString(R.string.warning_clear_db))
                    .setPositiveButton(activity.getString(R.string.yes), (dialog, whichButton) ->
                            new AlertDialog.Builder(activity)                       // Додаткове підтвердження
                            .setTitle(activity.getString(R.string.confirm_clear_db))
                            .setMessage(activity.getString(R.string.double_warning_clear_db))
                            .setPositiveButton(activity.getString(R.string.yes), (dialog2, whichButton2) -> {
                                // Виконання очищення БД
                                Disposable disposable = Single.zip(
                                            utils.ByDish().deleteAll(),
                                            utils.ByCollection().deleteAllWithoutSystem(),
                                            utils.ByIngredientShopList().deleteAll(),
                                            (result1, result2, result3) -> new ArrayList<Boolean>()
                                        )
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(result -> {
                                            if (!result.contains(false)) {
                                                Toast.makeText(activity, activity.getString(R.string.successful_clear_db), Toast.LENGTH_SHORT).show();
                                                Log.d("SettingPanel", "База даних успішно очищена");
                                            } else {
                                                Toast.makeText(activity, activity.getString(R.string.error_clear_db), Toast.LENGTH_SHORT).show();
                                                Log.e("SettingPanel", "Помилка очищення бази даних");
                                            }
                                        }, throwable -> {
                                            Toast.makeText(activity, activity.getString(R.string.error_clear_db), Toast.LENGTH_SHORT).show();
                                            Log.e("SettingPanel", "Помилка очищення бази даних", throwable);
                                        });

                                compositeDisposable.add(disposable);
                            })
                            .setNegativeButton(activity.getString(R.string.no), null).show())
                    .setNegativeButton(activity.getString(R.string.no), null).show());
        }
    }

    /**
     * Обробник кліку на кнопку налаштувань
     */
    public void onClickSetting() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            openDrawer();
            languageSpinner.setSelection(preferencesController.getIndexLanguage());
            themeSpinner.setSelection(preferencesController.getIndexTheme());
            paletteSpinner.setSelection(preferencesController.getIndexPalette());
        }
    }

    /**
     * Перевіряє, чи відкрита панель налаштувань
     * @return true, якщо панель відкрита
     */
    public boolean isDrawerOpen() {
        if (drawerLayout != null) {
            return drawerLayout.isDrawerOpen(GravityCompat.END);
        }
        return false;
    }

    /**
     * Відкриває панель налаштувань
     */
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.END);
            Log.d(nameActivity, "Відкриття налаштувань");
        }
    }

    /**
     * Закриває панель налаштувань
     */
    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.END);
            Log.d(nameActivity, "Закриття налаштувань");
        }
    }

    /**
     * Очищає Rx disposables
     */
    public void clearDisposables() { compositeDisposable.clear(); }

    /**
     * Блокує панель налаштувань
     */
    public void lockSettingPanel() {
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    /**
     * Розблоковує панель налаштувань
     */
    public void unlockSettingPanel() {
        if (drawerLayout != null && navigationView != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            navigationView.bringToFront();
        }
    }
}
