package com.example.recipes.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.recipes.Adapter.CustomSpinnerAdapter;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Fragments.CollectionsDishFragment;
import com.example.recipes.Fragments.RandDishFragment;
import com.example.recipes.Fragments.SearchDishFragment;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.FileUtils;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.RecipeUtils;
import com.example.recipes.R;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends FragmentActivity {
    private DrawerLayout drawerLayout;
    private LinearLayout linearLayout, importLayout, exportLayout;
    private ConstraintLayout constraintLayout;
    private PerferencesController perferencesController;
    private View headerView;
    private RecipeUtils utils;
    private Spinner languageSpinner, themeSpinner, paletteSpinner;
    private String[] languageArray, themeArray, paletteArray;
    private Button confirmButton;
    private String selectedLanguage, selectedTheme, selectedPalette;
    private ImageView img1, img2, img3, img4;
    private static int currentActivity = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);

        utils = new RecipeUtils(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchDishFragment()).commit();
        }

        loadItemsActivity();
        loadClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSearchFragment();
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
        }
    }

    private void loadItemsActivity(){
        drawerLayout = findViewById(R.id.drawerLayout);
        linearLayout = findViewById(R.id.main_menu);
        constraintLayout = findViewById(R.id.main_header);
        NavigationView navigationView = findViewById(R.id.navigationView);

        headerView = navigationView.getHeaderView(0);
        languageSpinner = headerView.findViewById(R.id.language_spinner);
        themeSpinner = headerView.findViewById(R.id.theme_spinner);
        paletteSpinner = headerView.findViewById(R.id.palette_spinner);
        confirmButton = headerView.findViewById(R.id.confirm_button);
        importLayout = headerView.findViewById(R.id.importContainer);
        exportLayout = headerView.findViewById(R.id.exportContainer);

        img1 = linearLayout.findViewById(R.id.main_home_hub);
        img2 = linearLayout.findViewById(R.id.main_rand_hub);
        img4 = linearLayout.findViewById(R.id.main_collections_hub);
        img3 = constraintLayout.findViewById(R.id.setting);

        setCurrentMenuSelect();

        ArrayAdapter<String> languageAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.language_options)));
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        ArrayAdapter<String> themeAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.theme_options)));
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        ArrayAdapter<String> paletteAdapter = new CustomSpinnerAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(getResources().getStringArray(R.array.palette_options)));
        paletteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paletteSpinner.setAdapter(paletteAdapter);

        languageArray = getStringArrayForLocale(R.array.language_values, new Locale("en"));
        themeArray = getStringArrayForLocale(R.array.theme_options, new Locale("en"));
        paletteArray = getStringArrayForLocale(R.array.palette_options, new Locale("en"));

        Log.d("MainActivity", "Завантаження всіх об'єктів активності");
    }

    private void setCurrentMenuSelect(){
        switch (currentActivity){
            case 2:
                img2.setSelected(true);
                img1.setSelected(false);
                img4.setSelected(false);
                break;
            case 4:
                img4.setSelected(true);
                img1.setSelected(false);
                img2.setSelected(false);
                break;
            default:
                img1.setSelected(true);
                img2.setSelected(false);
                img4.setSelected(false);
                break;
        }
    }

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

                Fragment fragment = null;
                if (v.getId() == R.id.main_home_hub) {
                    fragment = setSearchFragment();
                } else if (v.getId() == R.id.main_rand_hub) {
                    fragment = setRandomFragment();
                } else if (v.getId() == R.id.setting) {
                    openSetting();
                } else if (v.getId() == R.id.main_collections_hub) {
                    fragment = setCollectionsFragment();
                }

                if (fragment != null) {
                    openFragment(fragment);
                }
            }
        };

        exportLayout.setOnClickListener(v -> {
            ArrayList<Dish> dishes = utils.getDishesOrdered();
            ArrayList<Ingredient> ingredients = utils.getIngredients();

            if (!dishes.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.confirm_export))
                        .setMessage(getString(R.string.warning_export))
                        .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                            DataBox recipeData = new DataBox(dishes, ingredients);
                            Uri fileUri = ImportExportController.exportRecipeData(MainActivity.this, recipeData);

                            if (fileUri != null) {
                                FileUtils.sendFileByUri(this, fileUri);
                                Log.d("ImportExportController", "Рецепти успішно експортовані");
                            }

                            FileUtils.deleteFileByUri(this, fileUri);
                        })
                        .setNegativeButton(getString(R.string.no), null).show();
            } else {
                Toast.makeText(this, getString(R.string.error_void_dish), Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "Рецепти успішно імпортовані із файлу.");
            }
        });

        importLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/json");
            startActivityForResult(intent, 1);
            Log.d("MainActivity", "Читання з файлу");
        });

        img1.setOnClickListener(imageClickListener);
        img2.setOnClickListener(imageClickListener);
        img3.setOnClickListener(imageClickListener);
        img4.setOnClickListener(imageClickListener);

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
                        if (utils.addRecipe(recipeData, 4)){
                            Toast.makeText(this, getString(R.string.successful_import) + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            Log.d("MainActivity", "Рецепти успішно імпортовані із файлу." + file.getAbsolutePath());
                        } else {
                            Log.e("MainActivity", "Не вдалося імпортувати дані рецепту.");
                        }
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

    private Fragment setSearchFragment(){
        currentActivity = 1;
        setCurrentMenuSelect();
        Log.d("MainActivity", "Зміна фрагмента на домашню сторінку пошуку.");
        return new SearchDishFragment();
    }

    private Fragment setRandomFragment(){
        currentActivity = 2;
        setCurrentMenuSelect();
        Log.d("MainActivity", "Зміна фрагмента на рандомну сторінку");
        return new RandDishFragment();
    }

    private Fragment setCollectionsFragment(){
        currentActivity = 4;
        setCurrentMenuSelect();
        Log.d("MainActivity", "Зміна фрагмента на колекцію страв");
        return new CollectionsDishFragment();
    }

    private void openFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        Log.d("MainActivity", "Завантаження нового фрагмента");
    }

    private void updateSearchFragment(){
        Fragment fragment = setSearchFragment();
        openFragment(fragment);
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

    private String[] getStringArrayForLocale(int resId, Locale locale) {
        Resources resources = this.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        Context localizedContext = new ContextWrapper(this).createConfigurationContext(config);
        return localizedContext.getResources().getStringArray(resId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
