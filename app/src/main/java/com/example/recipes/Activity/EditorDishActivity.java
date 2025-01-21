package com.example.recipes.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.IngredientSetAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditorDishActivity extends Activity {
    private EditText nameEditText, recipeEditText;
    private TextView headText;
    private IngredientSetAdapter ingredientAdapter;
    private ImageView imageView;
    private Button editButton;
    private Long dishID;
    private Boolean mode;
    private RecipeUtils utils;
    private ArrayList<Ingredient> ingredients;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(this);

        utils = new RecipeUtils(this);
        compositeDisposable = new CompositeDisposable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_dish_activity);

        loadItemsActivity();
        loadClickListeners();
        Log.d("EditorDishActivity", "Активність успішно створена");
    }

    @Override
    protected void onResume(){
        super.onResume();
        dishID = getIntent().getLongExtra(Config.KEY_DISH, -1);

        if (dishID > -1) {
            mode = Config.EDIT_MODE;
            headText.setText(R.string.edit_dish_text);
            editButton.setText(R.string.edit);

            Disposable disposable = utils.getDish(dishID)
                    .flatMap(dish -> {
                                if (dish != null) {
                                    nameEditText.setText(dish.getName());
                                    recipeEditText.setText(dish.getRecipe());
                                    return utils.getIngredients(dish.getId());
                                } else {
                                    Log.e("EditorDishActivity", "Помилка. Страва пуста");
                                    return Single.just(new ArrayList<Ingredient>());
                                }
                            },
                            throwable -> {
                                Log.e("EditorDishActivity", "Помилка отримання страви з бд.", throwable);
                                return Single.just(new ArrayList<Ingredient>());
                            }
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            ingredients -> {
                                if (ingredients != null && !ingredients.isEmpty()) {
                                    for (Ingredient in : ingredients) {
                                        ingredientAdapter.addIngredient(in);
                                    }

                                    Log.d("EditorDishActivity", "Активність успішно відобразилась. Всі дані рецепта отримані");
                                } else {
                                    Log.d("EditorDishActivity", "Страва немає інгредієнтів.");
                                }
                            },
                            throwable -> {
                                Log.e("EditorDishActivity", "Помилка отримання інгредієнтів з бд.", throwable);
                            }
                    );

            compositeDisposable.add(disposable);
        } else if (dishID == -2) {
            mode = Config.ADD_MODE;
            headText.setText(R.string.add_dish_text);
            nameEditText.setText("");
            recipeEditText.setText("");
            editButton.setText(R.string.button_add);
        } else {
            mode = Config.EDIT_MODE;
            headText.setText(R.string.edit_dish_text);
            editButton.setText(R.string.edit);
            Toast.makeText(this,  getString(R.string.error_edit_get_dish), Toast.LENGTH_SHORT).show();
            Log.e("EditorDishActivity", "Помилка. Не вдалося отримати блюдо на редагування");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("EditorDishActivity", "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void loadItemsActivity(){
        headText = findViewById(R.id.textView14);
        nameEditText = findViewById(R.id.nameEditTextEditAct);
        recipeEditText = findViewById(R.id.recipeEditTextEditAct);
        editButton = findViewById(R.id.editButton);
        RecyclerView addIngredientRecyclerView = findViewById(R.id.addIngredientRecyclerViewEditAct);

        ingredients = new ArrayList<>();

        Disposable disposable = utils.getNameIngredientsUnique()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        names -> {
                            ingredientAdapter = new IngredientSetAdapter(this, new ArrayList<>(names), addIngredientRecyclerView);
                            addIngredientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                            addIngredientRecyclerView.setAdapter(ingredientAdapter);
                        },
                        throwable -> {
                            Log.d("SearchDishFragment", "Помилка отримання унікальних назв інгредіентів");
                        }
                );
        compositeDisposable.add(disposable);
        imageView = findViewById(R.id.back_edit_dish_imageView);

        Log.d("EditorDishActivity", "Елементи активності успішно завантажені");
    }

    private void loadClickListeners() {
        imageView.setOnClickListener(v -> { finish(); });

        editButton.setOnClickListener(view -> {
            if (mode) {
                onAddButtonClick(view);
            } else {
                onEditButtonClick(view);
            }
        });

        CharacterLimitTextWatcher.setCharacterLimit(this, nameEditText, Config.CHAR_LIMIT_NAME_DISH);
        CharacterLimitTextWatcher.setCharacterLimit(this, recipeEditText, Config.CHAR_LIMIT_RECIPE_DISH);

        Log.d("EditorDishActivity", "Слухачі активності успішно завантажені");
    }

    public void onAddButtonClick(View view) {
        String name = nameEditText.getText().toString().trim();
        String recipe = recipeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.warning_void_name_dish), Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(recipe)) {
                recipe = getString(R.string.default_recipe_text);
            }

            final String finalRecipe = recipe;
            Disposable checkDuplicateDisposable = utils.checkDuplicateDishName(name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            status -> {
                                if (status) {
                                    Toast.makeText(this, getString(R.string.warning_dublicate_name_dish), Toast.LENGTH_SHORT).show();
                                } else {
                                    ingredientAdapter.updateIngredients();
                                    ingredients = ingredientAdapter.getIngredients();
                                    boolean flagFullIngredient = true;

                                    for (Ingredient in : ingredients) {
                                        if (in.getType().isEmpty()) {
                                            in.setType(utils.getNameIngredientType(Config.VOID));
                                        }

                                        if (in.getName().isEmpty()) {
                                            flagFullIngredient = false;
                                            break;
                                        }
                                    }

                                    if (!flagFullIngredient) {
                                        Toast.makeText(this, getString(R.string.warning_set_all_data), Toast.LENGTH_SHORT).show();
                                    } else {
                                        addDish(new Dish(name, finalRecipe));
                                    }
                                }
                            },
                            throwable -> {
                                Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                                Log.e("AddDishActivity", "Ошибка при проверке дубликата", throwable);
                            }
                    );

            compositeDisposable.add(checkDuplicateDisposable);
        }
    }

    public void onEditButtonClick(View view) {
        Dish newDish;
        String name = nameEditText.getText().toString().trim();
        String recipe = recipeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.error_edit_dish_delete_name), Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(recipe)) {
                recipe = getString(R.string.default_recipe_text);
            }

            newDish = new Dish(dishID, name, recipe);

            if (dishID != -1){
                editDish(newDish);
            }
            else {
                Toast.makeText(this, getString(R.string.error_edit_get_dish), Toast.LENGTH_SHORT).show();
                Log.d("EditorDishActivity", "Помилка передачі страви на редагування");
            }
        }
    }

    public void onAddIngredientButtonClickEditAct(View view) {
        Ingredient newIngredient = new Ingredient("", "", "");
        ingredientAdapter.addIngredient(newIngredient);
    }

    private void addDish(Dish dish) {
        Disposable addDishDisposable = utils.addDish(dish, ingredients, Config.ID_MY_RECIPE_COLLECTION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        success -> {
                            if (success) {
                                Toast.makeText(this, getString(R.string.successful_add_dish), Toast.LENGTH_SHORT).show();
                                Log.d("AddDishActivity", "Страва успішно додана в бд");
                                finish();
                            } else {
                                Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                                Log.e("AddDishActivity", "Помилка при добаванні страви в бд");
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                            Log.e("AddDishActivity", "Помилка при добаванні страви в бд", throwable);
                            finish();
                        }
                );

        compositeDisposable.add(addDishDisposable);
    }

    private void editDish(Dish newDish) {
        Disposable disposable = utils.updateDish(newDish)
                .andThen(
                        utils.getIngredients(dishID)
                                .flatMap(
                                        ingredients -> {
                                            ingredientAdapter.updateIngredients();
                                            ArrayList<Ingredient> newIngredients = ingredientAdapter.getIngredients();

                                            if (ingredients != null && !ingredients.isEmpty()) {
                                                return utils.deleteIngredient(new ArrayList<>(ingredients))
                                                        .flatMap(status -> {
                                                            if (!status) {
                                                                Log.e("EditorDishActivity", "Помилка видалення інгредієнтів страви з БД.");
                                                            }

                                                            if (newIngredients == null || newIngredients.isEmpty()) {
                                                                return Single.just(new ArrayList<Ingredient>());
                                                            } else {
                                                                return Single.just(newIngredients);
                                                            }
                                                        });
                                            } else {
                                                Log.d("EditorDishActivity", "Список інгредієнтів страви пустий.");
                                                if (newIngredients == null || newIngredients.isEmpty()) {
                                                    return Single.just(new ArrayList<Ingredient>());
                                                } else {
                                                    return Single.just(newIngredients);
                                                }
                                            }
                                        },
                                        throwable -> {
                                            Log.e("EditorDishActivity", "Помилка отримання інгредієнтів страви.", throwable);
                                            return Single.just(new ArrayList<Ingredient>());
                                        }
                                )
                                .flatMap(
                                        ingredients -> {
                                            if (ingredients != null && !ingredients.isEmpty()) {
                                                return utils.addIngredients(dishID, new ArrayList<>(ingredients));
                                            } else {
                                                Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                                Log.d("EditorDishActivity", "Помилка редагування страви");
                                                return Single.just(false);
                                            }
                                        },
                                        throwable -> {
                                            Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                            Log.d("EditorDishActivity", "Помилка редагування страви");
                                            return Single.just(false);
                                        }
                                )
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result) {
                                Toast.makeText(this, getString(R.string.successful_edit_dish), Toast.LENGTH_SHORT).show();
                                Log.d("EditorDishActivity", "Страва успішно відредагована");
                            }
                            else {
                                Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                Log.e("EditorDishActivity", "Помилка редагування страви");
                            }

                            finish();
                        },
                        throwable -> {
                            Log.e("EditorDishActivity", "Помилка редагування страви", throwable);
                            finish();
                        }
                );

        compositeDisposable.add(disposable);
    }
}
