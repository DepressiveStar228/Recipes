package com.example.recipes.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.recipes.Adapter.CustomSpinnerAdapter;
import com.example.recipes.Adapter.ViewPagerAdapter;
import com.example.recipes.Config;
import com.example.recipes.Fragments.ShoplistFragment;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Fragments.CollectionsDishFragment;
import com.example.recipes.Fragments.RandDishFragment;
import com.example.recipes.Fragments.SearchDishFragment;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Pair;

public class MainActivity extends FragmentActivity {
    private DrawerLayout drawerLayout;
    private LinearLayout importLayout;
    private LinearLayout exportLayout;
    private PerferencesController perferencesController;
    private RecipeUtils utils;
    private Spinner languageSpinner, themeSpinner, paletteSpinner;
    private Switch status_ing_hints_Switch;
    private String[] languageArray, themeArray, paletteArray;
    private Button confirmButton;
    private String selectedLanguage, selectedTheme, selectedPalette;
    private ImageView img1, img2, img3, img4, img5;
    private List<ImageView> imageViews;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewAdapter;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);

        utils = new RecipeUtils(this);
        compositeDisposable = new CompositeDisposable();
        initialDB();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new SearchDishFragment());
        fragmentList.add(new RandDishFragment());
        fragmentList.add(new CollectionsDishFragment());
        fragmentList.add(new ShoplistFragment());

        viewPager = findViewById(R.id.viewPager);
        viewAdapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(viewAdapter);

        loadItemsActivity();
        loadClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("MainActivity", "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = viewAdapter.getFragmentList().get(viewPager.getCurrentItem());
        boolean flagMayClose = false;

        if (currentFragment instanceof OnBackPressedListener) {
            flagMayClose = ((OnBackPressedListener) currentFragment).onBackPressed();
        }

        if (flagMayClose) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem(0);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.exit_app))
                        .setMessage(getString(R.string.confirm_exit_app))
                        .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                            super.onBackPressed();
                            finish();
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
        }
    }

    public void openSetting() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
            Log.d("MainActivity", "Відкриття налаштувань");
            languageSpinner.setSelection(perferencesController.getIndexLanguage());
            themeSpinner.setSelection(perferencesController.getIndexTheme());
            paletteSpinner.setSelection(perferencesController.getIndexPalette());
            status_ing_hints_Switch.setChecked(perferencesController.getStatus_ing_hints());
        }
    }

    private void loadItemsActivity(){
        drawerLayout = findViewById(R.id.drawerLayout);
        LinearLayout linearLayout = findViewById(R.id.main_menu);
        ConstraintLayout constraintLayout = findViewById(R.id.main_header);
        NavigationView navigationView = findViewById(R.id.navigationView);

        View headerView = navigationView.getHeaderView(0);
        languageSpinner = headerView.findViewById(R.id.language_spinner);
        themeSpinner = headerView.findViewById(R.id.theme_spinner);
        paletteSpinner = headerView.findViewById(R.id.palette_spinner);
        status_ing_hints_Switch = headerView.findViewById(R.id.ingredient_hints_switch);
        confirmButton = headerView.findViewById(R.id.confirm_button);
        importLayout = headerView.findViewById(R.id.importContainer);
        exportLayout = headerView.findViewById(R.id.exportContainer);

        img1 = linearLayout.findViewById(R.id.main_home_hub);
        img2 = linearLayout.findViewById(R.id.main_rand_hub);
        img4 = linearLayout.findViewById(R.id.main_collections_hub);
        img5 = linearLayout.findViewById(R.id.main_shopList_hub);
        img3 = constraintLayout.findViewById(R.id.setting);

        imageViews = new ArrayList<>();
        imageViews.add(img1);
        imageViews.add(img2);
        imageViews.add(img4);
        imageViews.add(img5);

        setCurrentMenuSelect(0);

        ArrayAdapter<String> languageAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.language_options)));
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);
        languageSpinner.setSelection(perferencesController.getIndexLanguage());

        ArrayAdapter<String> themeAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.theme_options)));
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);
        themeSpinner.setSelection(perferencesController.getIndexTheme());

        ArrayAdapter<String> paletteAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.palette_options)));
        paletteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paletteSpinner.setAdapter(paletteAdapter);
        paletteSpinner.setSelection(perferencesController.getIndexPalette());

        status_ing_hints_Switch.setChecked(perferencesController.getStatus_ing_hints());

        languageArray = perferencesController.getStringArrayForLocale(R.array.language_values, "en");
        themeArray = perferencesController.getStringArrayForLocale(R.array.theme_options,"en");
        paletteArray = perferencesController.getStringArrayForLocale(R.array.palette_options, "en");

        Log.d("MainActivity", "Завантаження всіх об'єктів активності");
    }

    private void setCurrentMenuSelect(int id) {
        for (ImageView view : imageViews) {
            view.setSelected(false);
        }

        imageViews.get(id).setSelected(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners(){
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLanguage = perferencesController.getLanguageNameBySpinner(position);
                Log.d("MainActivity", "Слухач помітив зміну мови");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLanguage = languageArray[0];
            }
        });

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTheme = perferencesController.getThemeNameBySpinner(position);
                Log.d("MainActivity", "Слухач помітив зміну теми");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTheme = themeArray[0];
            }
        });

        paletteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPalette = perferencesController.getPaletteNameBySpinner(position);
                Log.d("MainActivity", "Слушатель заметил смену палитры");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPalette = paletteArray[0];
            }
        });

        confirmButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Слухач помітив підтвердження налаштувань");
            perferencesController.setLocale(selectedLanguage);
            perferencesController.setAppTheme(selectedTheme, selectedPalette);
            perferencesController.savePreferences(selectedLanguage, selectedTheme, selectedPalette);
            perferencesController.savePreferences(status_ing_hints_Switch.isChecked());
            drawerLayout.closeDrawer(GravityCompat.END);

            if (android.os.Build.VERSION.SDK_INT >= 11){
                Log.d("MainActivity", "Рестарт активності через recreate()");
                recreate();
            } else {
                Log.d("MainActivity", "Рестарт активності через Intent");
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

            Log.d("MainActivity", "Рестарт активності завершено");
        });

        View.OnClickListener imageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Слухач помітив зміну фрагмента");
                animateImage(v);

                if (v.getId() == R.id.main_home_hub) {
                    viewPager.setCurrentItem(0);
                    setCurrentMenuSelect(0);
                } else if (v.getId() == R.id.main_rand_hub) {
                    viewPager.setCurrentItem(1);
                    setCurrentMenuSelect(1);
                } else if (v.getId() == R.id.setting) {
                    openSetting();
                } else if (v.getId() == R.id.main_collections_hub) {
                    viewPager.setCurrentItem(2);
                    setCurrentMenuSelect(2);
                } else if (v.getId() == R.id.main_shopList_hub) {
                    viewPager.setCurrentItem(3);
                    setCurrentMenuSelect(3);
                }
            }
        };

        exportLayout.setOnClickListener(v -> {
            Disposable disposable = utils.getAllDishes()
                    .flatMap(dishes -> utils.getListPairDishIng(dishes))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        list -> {
                            if (!list.isEmpty()) {
                                new AlertDialog.Builder(this)
                                        .setTitle(getString(R.string.confirm_export))
                                        .setMessage(getString(R.string.warning_export))
                                        .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                                            DataBox recipeData = new DataBox();

                                            for (Pair<Dish, ArrayList<Ingredient>> pair : list) {
                                                recipeData.addRecipe(pair);
                                            }

                                            ImportExportController.exportRecipeData(MainActivity.this, recipeData, new ExportCallbackUri() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    if (uri != null) {
                                                        FileUtils.sendFileByUri(MainActivity.this, uri);
                                                        Log.d("ImportExportController", "Рецепти успішно експортовані");
                                                    }

                                                    FileUtils.deleteFileByUri(MainActivity.this, uri);
                                                }

                                                @Override
                                                public void onError(Throwable throwable) {
                                                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                                    Log.e("ImportExportController", "Помилка при експорті рецептів", throwable);
                                                }

                                                @Override
                                                public void getDisposable(Disposable disposable) {

                                                }
                                            });
                                        })
                                        .setNegativeButton(getString(R.string.no), null).show();
                            } else {
                                Toast.makeText(this, getString(R.string.error_void_dish), Toast.LENGTH_SHORT).show();
                                Log.d("MainActivity", "Рецепти успішно імпортовані із файлу.");
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
            startActivityForResult(intent, 1);
            Log.d("MainActivity", "Читання з файлу");
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentMenuSelect(position);
            }
        });

        img1.setOnClickListener(imageClickListener);
        img2.setOnClickListener(imageClickListener);
        img3.setOnClickListener(imageClickListener);
        img4.setOnClickListener(imageClickListener);
        img5.setOnClickListener(imageClickListener);

        Log.d("MainActivity", "Завантаження всіх слухачів активності");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                String path = null;
                try {
                    path = String.valueOf(FileUtils.getFileFromUri(this, uri));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (path != null) {
                    File file = new File(path);
                    RecipeUtils utils = new RecipeUtils(this);
                    DataBox recipeData = ImportExportController.importRecipeDataToFile(this, file);
                    if (recipeData != null) {
                        Disposable disposable = utils.addRecipe(recipeData, Config.ID_IMPORT_RECIPE_COLLECTION)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(status -> {
                                    if (status){
                                        if (viewPager.getCurrentItem() == 2) {
                                            viewAdapter.updateCollectionFragment();
                                        }
                                        Toast.makeText(this, getString(R.string.successful_import) + file.getName(), Toast.LENGTH_SHORT).show();
                                        Log.d("MainActivity", "Рецепти успішно імпортовані із файлу." + file.getName());
                                    } else {
                                        Log.e("MainActivity", "Не вдалося імпортувати дані рецепту.");
                                    }
                                });

                        compositeDisposable.add(disposable);
                    } else {
                        Log.e("MainActivity", "Не вдалося імпортувати дані рецепту.");
                    }
                } else {
                    Log.e("MainActivity", "Не вдалося отримати шлях файлу з URI:" + uri.toString());
                }
            } else {
                Log.e("MainActivity", "Дані або URI дорівнюють null.");
            }
        }
    }

    private void animateImage(View view) {
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f);
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1.0f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1.0f);

        scaleXUp.setDuration(150);
        scaleYUp.setDuration(150);
        scaleXDown.setDuration(150);
        scaleYDown.setDuration(150);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.play(scaleXUp).with(scaleYUp);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleXDown).with(scaleYDown);

        AnimatorSet scale = new AnimatorSet();
        scale.playSequentially(scaleUp, scaleDown);
        scale.start();
    }

    private void initialDB() {
        Disposable disposable = utils.getAllCollections()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(collections -> {
                    if (collections.isEmpty()) {
                        ArrayList<String> names = utils.getAllNameSystemCollection();
                        Disposable disposable1 = Observable.fromIterable(names)
                                .concatMap(name -> utils.addCollection(new Collection(name, Config.COLLECTION_TYPE))
                                        .subscribeOn(Schedulers.io())
                                        .toObservable()
                                        .onErrorComplete()
                                )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(status -> {
                                    Log.d("MainActivity", "Всі колекції успішно додані");
                                }, throwable -> {
                                    Log.e("MainActivity", "Помилка при додаванні колекції", throwable);
                                });
                        compositeDisposable.add(disposable1);
                    }
                }, throwable -> {
                    Log.e("MainActivity", "Помилка при отриманні всіх колекцій", throwable);
                });

        compositeDisposable.add(disposable);
    }
}
