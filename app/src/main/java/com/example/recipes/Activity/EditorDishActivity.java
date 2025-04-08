package com.example.recipes.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SharedElementCallback;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.AI.Translator;
import com.example.recipes.Adapter.AddDishToCollectionsAdapter;
import com.example.recipes.Adapter.IngredientGetAdapter;
import com.example.recipes.Adapter.IngredientSetAdapter;
import com.example.recipes.Adapter.RecipeAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.ImageController;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Decoration.AnimationUtils;
import com.example.recipes.Decoration.TextLoadAnimation;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Enum.ID_System_Collection;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
 */
public class EditorDishActivity extends AppCompatActivity {
    // Режими роботи активності
    private final int ADD_MODE = -1;
    private final int EDIT_MODE = -2;
    private final int READ_MODE = -3;

    private int mode;                         // Поточний режим роботи активності
    private Dish originalDish, translateDish; // Об'єкти для роботи з оригінальною та перекладеною стравою
    private Long dishID = -3L;

    // Флаги для управління анімацією та перекладом
    private final AtomicBoolean flagAccessAnimation = new AtomicBoolean(true);
    private final AtomicBoolean flagAccessButtonAnimation = new AtomicBoolean(true);
    private final AtomicBoolean flagInitTranslationController = new AtomicBoolean(false);
    private final AtomicBoolean flagIsTranslated = new AtomicBoolean(false);

    // Налаштування анімації
    private final int durationAnimation = 200;

    // Об'єкти інтерфейсу
    private RecyclerView ingredientRecyclerView, recipeRecycler;
    private ItemTouchHelper itemTouchHelper;
    private TextView loadText;
    private TextView nameTextViewContainer, translate;
    private EditText nameEditText, portionEditText;
    private ConstraintLayout ingredientEmpty, recipeEmpty, portionEmpty, loadScreen, buttonsRecipeContainer, nameDishContainerWithBorder, nameDishContainer;
    private DrawerLayout drawerLayout;
    private ConstraintLayout editDishMenuItem, deleteDishMenuItem, shareDishMenuItem, addDishToCollectionMenuItem, copyAsTextMenuItem;
    private IngredientSetAdapter ingredientSetAdapter;
    private IngredientGetAdapter ingredientGetAdapter;
    private RecipeAdapter recipeAdapter;
    private ImageView back, setting, addIngredient, addTextToRecipe, addImageToRecipe;
    private Button setDataButton;

    private RecipeUtils utils; // Класс утиліт для роботи з рецептами через БД

    // Контролери
    private TextLoadAnimation screenLoad, imageLoad;
    private ImportExportController importExportController;
    private ImageController imageController;
    private Translator translator;
    private CompositeDisposable compositeDisposable;

    private String nameActivity; // Назва активності

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesController preferencesController = PreferencesController.getInstance();

        super.onCreate(savedInstanceState);
        preferencesController.setPreferencesToActivity(this);
        setContentView(R.layout.editor_dish_activity);

        loadItemsActivity();

        compositeDisposable = new CompositeDisposable();
        Log.d(nameActivity, "Активність успішно створена");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadScreen = findViewById(R.id.loadScreen);
        startLoadingScreenAnimation(); // Початок анімації завантаження активності

        // Асинхронне завантаження елементів активності
        compositeDisposable.add(Completable.create(emitter -> {
                    loadClickListeners();

                    translateDish = new Dish("");
                    dishID = getIntent().getLongExtra(IntentKeys.DISH_ID.name(), -1L);
                    utils = RecipeUtils.getInstance(this);

                    imageController = new ImageController(this);
                    imageController.clearCache();

                    importExportController = new ImportExportController(this);
                    nameActivity = this.getClass().getSimpleName();
                    originalDish = new Dish("");
                    imageLoad = new TextLoadAnimation(getString(R.string.loading));

                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Визначення режиму роботи активності
                    if (dishID > -1) {
                        mode = READ_MODE;
                        setObserveDish();
                        setIngredientAdapter();
                        setRecipeAdapter();
                    } else if (dishID == ADD_MODE) {
                        mode = ADD_MODE;
                        setDataButton.setText(R.string.button_add);
                        setIngredientAdapter();
                        setRecipeAdapter();
                    } else if (dishID == EDIT_MODE){
                        mode = EDIT_MODE;
                        setDataButton.setText(R.string.edit);
                        setObserveDish();
                        setIngredientAdapter();
                        setRecipeAdapter();
                    } else {
                        mode = READ_MODE;
                        setIngredientAdapter();
                        setRecipeAdapter();
                        Toast.makeText(this,  getString(R.string.error_edit_get_dish), Toast.LENGTH_SHORT).show();
                        Log.e(nameActivity, "Помилка. Не вдалося отримати блюдо на редагування");
                    }

                    stopLoadingScreenAnimation();
                    startAnimationChangeStylePage(0);
                }, Throwable::printStackTrace));

        // Ініціалізація перекладача
        translator = new Translator(this);
        compositeDisposable.add(translator.initialization()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    if (status) flagInitTranslationController.set(true);
                }));
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(nameActivity, "Активність успішно створена");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (imageController != null) imageController.dispose();
        Log.d(nameActivity, "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        else if (dishID >= 0 && mode != READ_MODE) {  // Якщо сторінка перебуває в режимі редагування, вона перейде в режим читання
            if (flagAccessAnimation.get()) {
                mode = READ_MODE;
                startAnimationChangeStylePage(durationAnimation);
            }
        } else super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1 && data != null) {
                // Обробка вибраного зображення з галереї
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    if (recipeRecycler != null && recipeAdapter != null) {
                        TextView textView = recipeAdapter.getTextViewForCurrentItem(recipeRecycler);

                        if (textView != null) {
                            imageLoad.setTextView(textView);
                            imageLoad.startAnimation();
                        }

                        recipeAdapter.setImageToCurrentItem(selectedImageUri, imageLoad);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {  // Якщо з якихось причин завантаження відбувається довше 10 секунд, анімація зупиниться
                        if (imageController.isLoading()) imageLoad.stopAnimation();
                    }, 10000);
                }
            } else if (requestCode == 2) {
                // Обробка зробленого фото з камери
                Uri currentPhotoUri = imageController.getCurrentPhotoUri();
                if (currentPhotoUri != null) {
                    if (recipeRecycler != null && recipeAdapter != null) {
                        TextView textView = recipeAdapter.getTextViewForCurrentItem(recipeRecycler);

                        if (textView != null) {
                            imageLoad.setTextView(textView);
                            imageLoad.startAnimation();
                        }

                        recipeAdapter.setImageToCurrentItem(currentPhotoUri, imageLoad);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> { // Якщо з якихось причин завантаження відбувається довше 10 секунд, анімація зупиниться
                        if (imageController.isLoading()) imageLoad.stopAnimation();
                    }, 10000);
                }
            }
        }
    }

    /**
     * Видача прав
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageController.openCamera(this);
            } else {
                Toast.makeText(this, "Доступ до камери заблоковано", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Ініціалізація елементів інтерфейсу
     */
    @SuppressLint("ResourceType")
    private void loadItemsActivity(){
        nameTextViewContainer = findViewById(R.id.nameTextViewContainer);
        nameDishContainerWithBorder = findViewById(R.id.nameDishContainerWithBorder);
        nameDishContainer = findViewById(R.id.nameDishContainer);
        buttonsRecipeContainer = findViewById(R.id.buttonsRecipeContainer);

        translate = findViewById(R.id.translate);
        ingredientEmpty = findViewById(R.id.ingredientEmpty);
        recipeEmpty = findViewById(R.id.recipeEmpty);
        portionEmpty = findViewById(R.id.portionEmpty);
        nameEditText = findViewById(R.id.nameEditTextEditAct);
        portionEditText = findViewById(R.id.portionEditTextEditAct);

        setDataButton = findViewById(R.id.editButton);
        addIngredient = findViewById(R.id.addIngredientButtonEditAct);
        ingredientRecyclerView = findViewById(R.id.addIngredientRecyclerViewEditAct);
        recipeRecycler = findViewById(R.id.addRecipeRecycler);

        setting = findViewById(R.id.imageSettingImageView);
        back = findViewById(R.id.backImageView);
        addTextToRecipe = findViewById(R.id.add_textContainer_to_recipe);
        addImageToRecipe = findViewById(R.id.add_imageContainer_to_recipe);

        // Бокове меню опцій
        drawerLayout = findViewById(R.id.readDrawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);

            if (headerView != null) {
                editDishMenuItem = headerView.findViewById(R.id.setting_dish_edit);
                deleteDishMenuItem = headerView.findViewById(R.id.setting_dish_delete);
                shareDishMenuItem = headerView.findViewById(R.id.setting_dish_share);
                addDishToCollectionMenuItem = headerView.findViewById(R.id.setting_dish_add_to_collection);
                copyAsTextMenuItem = headerView.findViewById(R.id.setting_dish_copy_as_text);
            }
        }

        Log.d(nameActivity, "Елементи активності успішно завантажені");
    }

    /**
     * Встановлення слухачів подій для елементів інтерфейсу
     */
    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners() {
        // Кнопки верхнього меню
        if (back != null) back.setOnClickListener(v -> finish());
        if (setting != null) setting.setOnClickListener(v -> onClickSettingDish());

        // Кнопки бокового меню
        if (editDishMenuItem != null) { editDishMenuItem.setOnClickListener(v -> editDish()); }
        if (deleteDishMenuItem != null) { deleteDishMenuItem.setOnClickListener(v -> deleteDish()); }
        if (shareDishMenuItem != null) { shareDishMenuItem.setOnClickListener(v -> shareDish()); }
        if (addDishToCollectionMenuItem != null) { addDishToCollectionMenuItem.setOnClickListener(v -> showAddDishInCollectionDialog()); }
        if (copyAsTextMenuItem != null) { copyAsTextMenuItem.setOnClickListener(v -> copy_as_text()); }

        // Кнопка перекладу
        if (translate != null) {
            translate.setOnClickListener(v -> {
                if (!flagInitTranslationController.get()) {
                    Toast.makeText(this, getString(R.string.error_access_translation), Toast.LENGTH_SHORT).show();
                    Log.d(nameActivity, getString(R.string.error_access_translation));
                }
                else if (translator.getClient().checkLimit()) {
                    if (!flagIsTranslated.get()) {
                        if (translateDish.getName().isEmpty()) {  // Якщо страву ще не було переведено, то почнеться переклад
                            onTranslationDishClick();
                        } else {                                  // Інакше буде показано вже перекладену раніше страву
                            flagIsTranslated.set(true);
                            setDataIntoItemsActivity(translateDish);
                            translate.setText(getString(R.string.translate_button_text_true));
                        }
                    } else {
                        flagIsTranslated.set(false);
                        setDataIntoItemsActivity(originalDish);
                        translate.setText(getString(R.string.translate_button_text_false));
                        Log.d(nameActivity, "Показ оригіналу страви");
                    }
                }
            });
        }

        // Кнопка збереження даних
        if (setDataButton != null) setDataButton.setOnClickListener(view -> {
            if (flagAccessAnimation.get()) onAddOrEditButtonClick();
        });

        // Кнопка додавання інгредієнта
        if (addIngredient != null) addIngredient.setOnClickListener(v -> {
            if (ingredientRecyclerView != null) {
                Ingredient newIngredient = new Ingredient("", "", IngredientType.VOID);
                if (ingredientSetAdapter != null) {
                    ingredientSetAdapter.addIngredient(newIngredient);

                    // Автоматичний прокрут до нового елемента
                    ingredientRecyclerView.post(() -> {
                        if (ingredientSetAdapter.getItemCount() > 0) {
                            ingredientRecyclerView.scrollToPosition(ingredientSetAdapter.getItemCount() - 1);
                        }
                    });
                }
            }
        });

        // Кнопка додавання тексту до рецепту
        if (addTextToRecipe != null) {
            addTextToRecipe.setOnClickListener(v -> {
                if (recipeRecycler != null) recipeRecycler.setVisibility(View.VISIBLE);

                recipeAdapter.addItem(new DishRecipe(dishID, DishRecipeType.TEXT));

                // Автоматичний прокрут до нового елемента
                recipeRecycler.post(() -> {
                    if (recipeAdapter.getItemCount() > 0) {
                        recipeRecycler.scrollToPosition(recipeAdapter.getItemCount() - 1);
                    }
                });
            });
        }

        // Кнопка додавання зображення до рецепту
        if (addImageToRecipe != null) {
            addImageToRecipe.setOnClickListener(v -> {
                if (recipeRecycler != null) recipeRecycler.setVisibility(View.VISIBLE);

                recipeAdapter.addItem(new DishRecipe(dishID, DishRecipeType.IMAGE));

                // Автоматичний прокрут до нового елемента
                recipeRecycler.post(() -> {
                    if (recipeAdapter.getItemCount() > 0) {
                        recipeRecycler.scrollToPosition(recipeAdapter.getItemCount() - 1);
                    }
                });
            });
        }

        // Встановлення обмежень на кількість символів для полів вводу
        CharacterLimitTextWatcher.setCharacterLimit(this, nameEditText, Limits.MAX_CHAR_NAME_DISH);
        CharacterLimitTextWatcher.setCharacterLimit(this, portionEditText, Limits.MAX_CHAR_PORTION_DISH);

        Log.d(nameActivity, "Слухачі активності успішно завантажені");
    }

    /**
     * Заповнення полів інтерфейсу даними з об'єкта страви
     */
    private void setDataIntoItemsActivity(Dish dish) {
        if (nameEditText != null) nameEditText.setText(dish.getName());
        if (ingredientRecyclerView != null) setIngredientsToIngredientAdapter(dish);
        if (portionEmpty != null && portionEditText != null) {
            if (dish.getPortion() > 0) portionEditText.setText(String.valueOf(dish.getPortion()));

            if (portionEditText.getText().toString().isEmpty()) portionEmpty.setVisibility(View.VISIBLE);
            else portionEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Ініціалізація адаптера для списку інгредієнтів
     */
    private void setIngredientAdapter() {
        if (ingredientRecyclerView != null) {
            if (ingredientSetAdapter == null) {
                ingredientSetAdapter = new IngredientSetAdapter(this, ingredientEmpty, ingredientRecyclerView);
            }
            if (ingredientGetAdapter == null) {
                ingredientGetAdapter = new IngredientGetAdapter(this, ingredientEmpty);
            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            // Вибір адаптера залежно від режиму активності
            if (mode == READ_MODE) ingredientRecyclerView.setAdapter(ingredientGetAdapter);
            else ingredientRecyclerView.setAdapter(ingredientSetAdapter);

            ingredientRecyclerView.setLayoutManager(linearLayoutManager);
            ingredientRecyclerView.setHasFixedSize(true);

            // Слухач зміни унікальних назв інгредієнтів
            utils.ByIngredient().getViewModel().getNamesUnique().observe(this, data -> {
                if (data != null) {
                    ingredientSetAdapter.setNamesIngredient(new ArrayList<>(data));
                }
            });
        }

        setObserveIngredients();
    }

    /**
     * Передача списку інгредієнтів до адаптерів
     */
    private void setIngredientsToIngredientAdapter(Dish dish) {
        if (ingredientSetAdapter != null) ingredientSetAdapter.setIngredients(dish.getIngredients());
        if (ingredientGetAdapter != null) ingredientGetAdapter.setIngredients(dish.getIngredients());
    }

    /**
     * Ініціалізація адаптера для рецептів
     */
    private void setRecipeAdapter() {
        if (recipeRecycler != null) {
            if (recipeAdapter == null) {
                recipeAdapter = new RecipeAdapter(this, recipeEmpty, mode == READ_MODE, new RecipeAdapter.ImageClickListener() {
                    @Override
                    public void onImageClick(AppCompatImageView imageView) {
                        if (imageController != null) {
                            imageLoad.clearAllRunnable(); // Очищуємо всі події

                            // Створення діалогу для вибору способу додавання зображення
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditorDishActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_choose_way_add_image, null);

                            if (dialogView != null) {
                                builder.setView(dialogView);
                                LinearLayout photo, gallery;
                                photo = dialogView.findViewById(R.id.photo);
                                gallery = dialogView.findViewById(R.id.gallery);

                                if (photo != null && gallery != null) {
                                    photo.setOnClickListener(view -> {
                                        // Видача прав для камери
                                        if (ContextCompat.checkSelfPermission(EditorDishActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(EditorDishActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
                                        } else imageController.openCamera(EditorDishActivity.this);
                                    });
                                    gallery.setOnClickListener(view -> imageController.openGallery(EditorDishActivity.this));
                                }
                            }

                            Dialog dialog =  builder.create();
                            dialog.show();

                            // Налаштування анімації завантаження зображення
                            imageLoad.addStartRunnable(() -> { // Додаємо дії до події початку анімації
                                if (dialogView != null) dialog.dismiss();
                                imageView.setVisibility(View.INVISIBLE);
                            });

                            imageLoad.addStopRunnable(() -> { // Додаємо дії до події закінчення анімації
                                imageView.setVisibility(View.VISIBLE);
                            });
                        }
                    }

                    @Override
                    public void onDeleteClick(DishRecipe dishRecipe) {
                        // Видалення елемента рецепту з бази даних
                        Disposable disposable = utils.ByDishRecipe().delete(dishRecipe)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> Log.d("RecipeUtils", "Елемент рецепту успішно видалено"),
                                        throwable -> Log.e("RecipeUtils", "Помилка: " + throwable.getMessage())
                                );
                        compositeDisposable.add(disposable);
                    }
                });

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

                recipeRecycler.setAdapter(recipeAdapter);
                recipeRecycler.addItemDecoration(new VerticalSpaceItemDecoration(-4));
                recipeRecycler.setLayoutManager(linearLayoutManager);
                recipeRecycler.getRecycledViewPool().clear();
                recipeRecycler.setItemViewCacheSize(20);
                recipeRecycler.setDrawingCacheEnabled(true);
                recipeRecycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                recipeRecycler.setHasFixedSize(true);

                setObserveRecipes();
            }
        }
    }

    /**
     * Налаштування спостереження за змінами страви в базі даних
     */
    public void setObserveDish() {
        // Слухач зміни страви в базі даних
        if (dishID > -1) {
            utils.ByDish().getViewModel().getByID(dishID).observe(this, data -> {
                if (data != null) {
                    originalDish.setId(data.getId());
                    originalDish.setName(data.getName());
                    originalDish.setPortion(data.getPortion());
                    setDataIntoItemsActivity(originalDish);
                }
            });
        }
    }

    /**
     * Налаштування спостереження за змінами інгредієнтів страви
     */
    private void setObserveIngredients() {
        // Слухач зміни інгредієнтів страви в базі даних
        utils.ByIngredient().getViewModel().getAllByIDDish(dishID).observe(this, data -> {
            if (data != null) {
                originalDish.setIngredients(new ArrayList<>(data));
                setIngredientsToIngredientAdapter(originalDish);
            }
        });
    }

    /**
     * Налаштування спостереження за змінами рецептів страви
     */
    private void setObserveRecipes() {
        // Слухач зміни рецептів страви в базі даних
        utils.ByDishRecipe().getViewModel().getByDishID(dishID).observe(this, data -> {
            if (data != null) {
                originalDish.setRecipes(new ArrayList<>(data));

                if (recipeAdapter != null) recipeAdapter.setItems(new ArrayList<>(data));
            }
        });
    }

    /**
     * Обробка натискання кнопки додавання або редагування страви
     */
    private void onAddOrEditButtonClick() {
        AtomicBoolean sameName = new AtomicBoolean(false);
        String name = nameEditText.getText().toString().trim();
        AtomicInteger portion = new AtomicInteger(0);

        try { portion.set(Integer.parseInt(portionEditText.getText().toString().trim())); }
        catch (Exception e) {}

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.warning_void_name_dish), Toast.LENGTH_SHORT).show();
        } else {
            sameName.set(Objects.equals(originalDish.getName(), name));

            Disposable checkDuplicateDisposable = utils.ByDish().checkDuplicateName(name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            status -> {
                                if (status && !sameName.get()) {
                                    Toast.makeText(this, getString(R.string.warning_dublicate_name_dish), Toast.LENGTH_SHORT).show();
                                } else {
                                    startLoadingScreenAnimation();

                                    ArrayList<Ingredient> ingredients = new ArrayList<>();
                                    ArrayList<DishRecipe> dishRecipes = new ArrayList<>();

                                    if (ingredientSetAdapter != null) {
                                        ingredientSetAdapter.updateIngredients();
                                        ingredients = ingredientSetAdapter.getIngredients();
                                    }
                                    if (recipeAdapter != null) {
                                        dishRecipes = recipeAdapter.getItems();
                                    }

                                    boolean flagFullIngredient = true, flagFullRecipeItem = true;

                                    for (Ingredient in : ingredients) {
                                        if (in.getName().isEmpty()) {
                                            flagFullIngredient = false;
                                            break;
                                        }
                                    }

                                    for (DishRecipe dishRecipe : dishRecipes) {
                                        if (dishRecipe.getTextData().isEmpty()) {
                                            flagFullRecipeItem = false;
                                            break;
                                        }
                                    }

                                    Dish newDish = new Dish(dishID, name, portion.get(), ingredients, dishRecipes);

                                    // Перевірка повноти даних інгредієнтів та рецептів
                                    if (!flagFullIngredient || !flagFullRecipeItem) {
                                        stopLoadingScreenAnimation();
                                        Toast.makeText(this, getString(R.string.warning_set_all_data), Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Виклик відповідної операції в залежності від режиму
                                        if (mode == ADD_MODE) { addDish(newDish); }
                                        else if (mode == EDIT_MODE) { editDish(newDish); }
                                    }
                                }
                            },
                            throwable -> {
                                stopLoadingScreenAnimation();
                                Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                                Log.e(nameActivity, "Ошибка при проверке дубликата", throwable);
                            }
                    );

            compositeDisposable.add(checkDuplicateDisposable);
        }
    }

    /**
     * Додавання нової страви до бази даних
     */
    private void addDish(Dish dish) {
        Disposable addDishDisposable = utils.ByDish().add(dish, ID_System_Collection.ID_MY_RECIPE.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        id -> {
                            if (id > 0) {
                                if (dishID == null) {
                                    dishID = id;
                                    setObserveDish();
                                }
                                stopLoadingScreenAnimation();
                                Toast.makeText(this, getString(R.string.successful_add_dish), Toast.LENGTH_SHORT).show();
                                finish();

                            } else {
                                Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            stopLoadingScreenAnimation();
                            Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                            Log.e(nameActivity, "Помилка при добаванні страви в бд", throwable);
                        }
                );

        compositeDisposable.add(addDishDisposable);
    }

    /**
     * Редагування існуючої страви в базі даних
     */
    private void editDish(Dish newDish) {
        Disposable disposable = utils.ByDish().update(newDish)
                .andThen(updateIngredient(newDish)
                        .flatMap(status -> {
                            if (status) return updateRecipe(newDish);
                            else return Single.just(false);
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result) {
                                stopLoadingScreenAnimation();
                                mode = READ_MODE;
                                startAnimationChangeStylePage(durationAnimation);
                                Toast.makeText(this, getString(R.string.successful_edit_dish), Toast.LENGTH_SHORT).show();
                                Log.d(nameActivity, "Страва успішно відредагована");
                            }
                            else {
                                Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                Log.e(nameActivity, "Помилка редагування страви");
                            }
                        },
                        throwable -> {
                            stopLoadingScreenAnimation();
                            Log.e(nameActivity, "Помилка редагування страви", throwable);
                        }
                );

        compositeDisposable.add(disposable);
    }

    /**
     * Запускає анімацію екрану завантаження
     */
    private void startLoadingScreenAnimation() {
        if (loadScreen != null) {
            loadScreen.setVisibility(View.VISIBLE);
            ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();

            loadText = new TextView(this);
            loadText.setText(R.string.loading);
            loadText.setTextSize(28);
            loadText.setTextColor(AnotherUtils.getAttrColor(this, R.attr.colorText));
            loadText.setPadding(20, 20, 20, 20);
            loadText.setGravity(Gravity.CENTER);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;

            rootView.addView(loadText, params);
            screenLoad = new TextLoadAnimation(loadText, getString(R.string.loading));
            screenLoad.startAnimation();
        }
    }

    /**
     * Зупиняє анімацію екрану завантаження
     */
    private void stopLoadingScreenAnimation() {
        if (loadScreen != null && loadText != null) {
            ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
            rootView.removeView(loadText);
            screenLoad.stopAnimation();
            loadText = null;
            loadScreen.setVisibility(View.GONE);
        }
    }

    /**
     * Оновлює інгредієнти страви в базі даних
     * Спочатку видаляє всі поточні інгредієнти, а потім додає нові
     *
     * @param newDish Страва з оновленими інгредієнтами
     * @return Single<Boolean> Результат операції (успішно/неуспішно)
     */
    private Single<Boolean> updateIngredient(Dish newDish) {
        return utils.ByIngredient().getAllByIDDish(dishID)
                .flatMap(
                        ingredients -> {
                            ArrayList<Ingredient> newIngredients = newDish.getIngredients();

                            if (ingredients != null && !ingredients.isEmpty()) {
                                return utils.ByIngredient().deleteAll(new ArrayList<>(ingredients))
                                        .flatMap(status -> {
                                            if (!status) {
                                                Log.e(nameActivity, "Помилка видалення інгредієнтів страви з БД.");
                                            }

                                            if (newIngredients != null) {
                                                return Single.just(newIngredients);

                                            } else {
                                                return Single.just(new ArrayList<Ingredient>());
                                            }
                                        });
                            } else {
                                Log.d(nameActivity, "Список інгредієнтів страви пустий.");
                                if (newIngredients == null || newIngredients.isEmpty()) {
                                    return Single.just(new ArrayList<Ingredient>());
                                } else {
                                    return Single.just(newIngredients);
                                }
                            }
                        },
                        throwable -> {
                            Log.e(nameActivity, "Помилка отримання інгредієнтів страви.", throwable);
                            return Single.just(new ArrayList<Ingredient>());
                        }
                )
                .flatMap(
                        ingredients -> {
                            if (ingredients != null) {
                                return utils.ByIngredient().addAll(dishID, new ArrayList<>(ingredients));
                            } else {
                                Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                Log.d(nameActivity, "Помилка редагування страви");
                                return Single.just(false);
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                            Log.d(nameActivity, "Помилка редагування страви");
                            return Single.just(false);
                        }
                );
    }

    /**
     * Оновлює рецепт страви в базі даних
     * Спочатку видаляє всі поточні елементи рецепту, а потім додає нові
     *
     * @param newDish Страва з оновленим рецептом
     * @return Single<Boolean> Результат операції (успішно/неуспішно)
     */
    private Single<Boolean> updateRecipe(Dish newDish) {
        return utils.ByDishRecipe().getByDishID(dishID)
                .flatMap(dishRecipes -> {
                    ArrayList<DishRecipe> newDishRecipes = newDish.getRecipes();

                    return Observable.fromIterable(newDishRecipes)
                            .flatMapSingle(newDishRecipe -> {
                                if (newDishRecipe.getTypeData() == DishRecipeType.IMAGE && !newDishRecipe.getTextData().contains("cache")) {
                                    return imageController.addImageToCache(newDishRecipe.getTextData())
                                            .flatMap(path -> {
                                                newDishRecipe.setTextData(path);
                                                return Single.just(newDishRecipe);
                                            });
                                } else return Single.just(newDishRecipe);
                            })
                            .toList()
                            .flatMap(newDishRecipes_ -> {
                                if (dishRecipes != null && !dishRecipes.isEmpty()) {
                                    return utils.ByDishRecipe().deleteAll(new ArrayList<>(dishRecipes))
                                            .flatMap(status -> {
                                                if (!status) {
                                                    Log.e(nameActivity, "Помилка видалення елементів рецепту страви з БД.");
                                                }

                                                if (newDishRecipes_ == null) {
                                                    return Single.just(new ArrayList<DishRecipe>());
                                                } else {
                                                    return Single.just(newDishRecipes_);
                                                }
                                            });
                                } else {
                                    Log.d(nameActivity, "Список елементів рецепту страви пустий.");
                                    if (newDishRecipes_ == null || newDishRecipes_.isEmpty()) {
                                        return Single.just(new ArrayList<DishRecipe>());
                                    } else {
                                        return Single.just(newDishRecipes_);
                                    }
                                }
                            });
                })
                .flatMap(
                        dishRecipes -> {
                            if (dishRecipes != null) {
                                return utils.ByDishRecipe().addAll(originalDish, new ArrayList<>(dishRecipes));
                            } else {
                                Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                                Log.d(nameActivity, "Помилка редагування страви");
                                return Single.just(false);
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                            Log.d(nameActivity, "Помилка редагування страви");
                            return Single.just(false);
                        }
                );
    }

    /**
     * Обробляє натискання кнопки перекладу страви
     * Перекладає назву страви, інгредієнти та текстові частини рецепту
     * Зберігає оригінальні позиції та нетекстові елементи рецепту
     */
    private void onTranslationDishClick() {
        if (translator != null) {
            if (AnotherUtils.checkInternet(this)) {
                TextLoadAnimation textLoadAnimation = new TextLoadAnimation(translate, getString(R.string.loading));
                textLoadAnimation.startAnimation();

                ArrayList<Integer> positionTextRecipe = new ArrayList<>();
                for (DishRecipe dishRecipe : originalDish.getRecipes()) {  // Запам'ятовуємо позиції текстових частин рецепту
                    if (dishRecipe.getTypeData() == DishRecipeType.TEXT) positionTextRecipe.add(dishRecipe.getPosition());
                }

                Disposable disposable = translator.translateRecipe(originalDish)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(dish -> {
                            if (dish != null) {
                                translateDish = new Dish(dish);
                                translateDish.setId(originalDish.getId());

                                ArrayList<Ingredient> translateIngredient = new ArrayList<>();
                                for (int i = 0; i < originalDish.getIngredients().size(); i++) {  // На переклад подаються лише назви інгредієнтів, тому тут відновлюється інші дані
                                    Ingredient ingredient = originalDish.getIngredients().get(i);
                                    translateIngredient.add(new Ingredient(dish.getIngredients().get(i).getName(), ingredient.getAmount(), ingredient.getType()));
                                }
                                translateDish.setIngredients(translateIngredient);

                                setDataIntoItemsActivity(translateDish);

                                translateDish.setRecipes(new ArrayList<>());
                                if (positionTextRecipe.size() == dish.getRecipes().size()) {
                                    for (int i = 0, j = 0; i < originalDish.getRecipes().size(); i++) {  // Вставляємо переклад у позиції, які запам'ятали
                                        if (positionTextRecipe.contains(i)) {
                                            translateDish.addRecipe(dish.getRecipes().get(j));
                                            j++;
                                        } else translateDish.addRecipe(originalDish.getRecipes().get(i));
                                    }
                                }

                                if (recipeRecycler != null && recipeAdapter != null) recipeAdapter.setItems(translateDish.getRecipes());

                                flagIsTranslated.set(true);
                                textLoadAnimation.setBaseText(getString(R.string.translate_button_text_true));
                                textLoadAnimation.stopAnimation();
                                translate.setVisibility(View.VISIBLE);
                            }
                            else {
                                flagIsTranslated.set(false);
                                textLoadAnimation.setBaseText(getString(R.string.translate_button_text_false));
                                textLoadAnimation.stopAnimation();
                                translate.setVisibility(View.VISIBLE);
                            }
                        });
                compositeDisposable.add(disposable);
            } else Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Повертає Runnable, який після виконання анімації
     * робить об'єкт знову активним для натискання
     *
     * @param v Об'єкт, який стане активним
     * @return Runnable для виконання після анімації
     */
    private Runnable getRunnableAnimation(View v) {
        return () -> {
            flagAccessButtonAnimation.set(true);
            v.setClickable(true);
        };
    }

    /**
     * Встановлює ширину поля для введення назви страви
     * Визначає оптимальну ширину поля в залежності від напрямку анімації
     *
     * @param isAnimatingForward Напрямок анімації (true - вперед, false - назад)
     */
    private void setWightNameEditText(boolean isAnimatingForward) {
        int match_parentWidth = nameDishContainerWithBorder.getWidth();

        nameEditText.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int newWidth = nameEditText.getMeasuredWidth();

        ViewGroup.LayoutParams params = nameEditText.getLayoutParams();

        if (isAnimatingForward) params.width = Math.min(match_parentWidth, newWidth);
        else params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        nameEditText.setLayoutParams(params);
    }

    /**
     * Встановлює горизонтальне зміщення поля для введення назви страви
     *
     * @param bias Значення зміщення (від 0 до 1)
     */
    private void setBiasNameEditText(float bias) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) nameEditText.getLayoutParams();
        params.horizontalBias = bias;
        nameEditText.setLayoutParams(params);
        nameEditText.requestLayout();
    }

    /**
     * Встановлює ширину поля для введення назви страви в залежності від режиму
     *
     * @param isRead Режим читання (true) або редагування/додавання (false)
     */
    private void setWidthNameEditText(boolean isRead) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) nameEditText.getLayoutParams();
        params.width = isRead ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        nameEditText.setLayoutParams(params);
        nameEditText.requestLayout();
    }

    /**
     * Запускає анімацію зміни стилю сторінки при переключенні між режимами
     * Виконує комплексну анімацію всіх елементів інтерфейсу
     *
     * @param duration Тривалість анімації в мілісекундах
     */
    private void startAnimationChangeStylePage(int duration) {
        boolean isRead = mode == READ_MODE;

        if (flagAccessAnimation.get()) {
            flagAccessAnimation.set(false);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {  // Блокуємо можливість клікати на період анімації
                flagAccessAnimation.set(true);
                if (loadScreen != null) loadScreen.setVisibility(View.GONE);
            }, duration*5L);

            if (duration <= 0) duration = 10;

            if (nameEditText != null && nameDishContainerWithBorder != null && nameTextViewContainer != null && nameDishContainer != null) {
                setWidthNameEditText(isRead);

                ValueAnimator textSizeAnimator = ValueAnimator.ofFloat(isRead ? 20f : 28f, isRead ? 28f : 20f);
                textSizeAnimator.setDuration(duration);
                textSizeAnimator.addUpdateListener(animation -> {
                    float animatedValue = (float) animation.getAnimatedValue();
                    nameEditText.setTextSize(animatedValue);
                    setWightNameEditText(isRead);
                });

                if (mode == READ_MODE) {
                    ValueAnimator biasAnimator = ValueAnimator.ofFloat(0f, 0.5f);
                    biasAnimator.setDuration(duration);
                    biasAnimator.addUpdateListener(animation -> {
                        float bias_ = (float) animation.getAnimatedValue();
                        setBiasNameEditText(bias_);
                    });
                    biasAnimator.start();
                } else {
                    ConstraintSet startSet = new ConstraintSet();
                    startSet.clone(nameDishContainer);

                    ConstraintSet endSet = new ConstraintSet();
                    endSet.clone(nameDishContainer);
                    endSet.setHorizontalBias(nameEditText.getId(), (mode == READ_MODE) ? 0.5f : 0f);

                    TransitionManager.beginDelayedTransition(nameDishContainer);
                    endSet.applyTo(nameDishContainer);
                }

                ValueAnimator alphaBackgroundAnimator = AnimationUtils.backgroundVisibility(nameDishContainerWithBorder, isRead ? AnimationUtils.HIDE : AnimationUtils.SHOW);
                if (alphaBackgroundAnimator != null) {
                    alphaBackgroundAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (isRead) {
                                nameEditText.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
                                if (portionEditText != null) portionEditText.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
                            }
                            else {
                                ColorStateList tintList = ColorStateList.valueOf(AnotherUtils.getAttrColor(EditorDishActivity.this, R.attr.colorControlNormal));
                                nameEditText.setBackgroundTintList(tintList);
                                nameEditText.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
                                if (portionEditText != null) {
                                    portionEditText.setBackgroundTintList(tintList);
                                    portionEditText.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
                                }
                            }
                        }
                    });
                    if (mode != READ_MODE) {
                        alphaBackgroundAnimator.setStartDelay(duration);
                        alphaBackgroundAnimator.setDuration(duration*3L);
                    }
                    else alphaBackgroundAnimator.setDuration(duration/2L);
                    alphaBackgroundAnimator.start();
                }

                ObjectAnimator moveUpAnimator = ObjectAnimator.ofFloat(nameDishContainerWithBorder, "translationY", isRead ? 0f : -50f, isRead ? -50f : 0f);
                moveUpAnimator.setDuration(duration);

                ObjectAnimator alphaNameContainerAnimator = ObjectAnimator.ofFloat(nameTextViewContainer, "alpha", isRead ? 1f : 0f, isRead ? 0f : 1f);
                if (mode != READ_MODE) {
                    alphaNameContainerAnimator.setStartDelay(duration);
                    alphaNameContainerAnimator.setDuration(duration*3L);
                } else alphaNameContainerAnimator.setDuration(duration/2L);


                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(textSizeAnimator, alphaNameContainerAnimator, moveUpAnimator);
                animatorSet.start();

                nameEditText.setEnabled(!isRead);
            }

            if (portionEditText != null && portionEmpty != null) {
                if (isRead) {
                    if (portionEditText.getText().toString().isEmpty()) {
                        AnimationUtils.smoothVisibility(portionEmpty, AnimationUtils.SHOW, duration);
                    } else portionEmpty.setVisibility(View.INVISIBLE);
                }
                else {
                    AnimationUtils.smoothVisibility(portionEditText, AnimationUtils.SHOW, duration);
                    AnimationUtils.smoothVisibility(portionEmpty, AnimationUtils.HIDE, duration);
                }
                portionEditText.setEnabled(!isRead);
            }
            if (translate != null) {
                if (isRead) AnimationUtils.smoothVisibility(translate, AnimationUtils.SHOW, duration);
                else AnimationUtils.smoothVisibility(translate, AnimationUtils.HIDE, duration);
            }
            if (drawerLayout != null) {
                if (isRead) drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED );
                else drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED );
            }
            if (setDataButton != null) {
                if (isRead) AnimationUtils.smoothVisibility(setDataButton, AnimationUtils.HIDE, duration);
                else {
                    if (mode == ADD_MODE) setDataButton.setText(R.string.button_add);
                    else if (mode == EDIT_MODE) setDataButton.setText(R.string.edit);

                    AnimationUtils.smoothVisibility(setDataButton, AnimationUtils.SHOW, duration);
                }
            }
            if (setting != null) {
                if (isRead) AnimationUtils.smoothVisibility(setting, AnimationUtils.SHOW, duration);
                else AnimationUtils.smoothVisibility(setting, AnimationUtils.HIDE, duration);
            }
            if (buttonsRecipeContainer != null && recipeRecycler != null) {
                if (isRead) AnimationUtils.smoothVisibility(buttonsRecipeContainer, AnimationUtils.HIDE, duration);
                else AnimationUtils.smoothVisibility(buttonsRecipeContainer, AnimationUtils.SHOW, duration);
            }
            if (addIngredient != null) {
                if (isRead) AnimationUtils.smoothVisibility(addIngredient, AnimationUtils.HIDE, duration);
                else AnimationUtils.smoothVisibility(addIngredient, AnimationUtils.SHOW, duration);
            }
            if (ingredientSetAdapter != null && ingredientGetAdapter != null && ingredientRecyclerView != null) {
                ViewGroup.LayoutParams layoutParams = ingredientRecyclerView.getLayoutParams();

                if (isRead) {
                    ingredientRecyclerView.setAdapter(ingredientGetAdapter);
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                else {
                    ingredientRecyclerView.setAdapter(ingredientSetAdapter);
                    layoutParams.height = AnotherUtils.dpToPx(400, this);
                }

                ingredientRecyclerView.setLayoutParams(layoutParams);
            }
            if (recipeRecycler != null && recipeAdapter != null) {
                ViewGroup.LayoutParams layoutParams = recipeRecycler.getLayoutParams();

                // Можливість перетягування елементів
                if (itemTouchHelper == null) itemTouchHelper = recipeAdapter.getItemTouchHelper();

                recipeAdapter.setReadMode(isRead);

                if (isRead) {
                    itemTouchHelper.attachToRecyclerView(null);
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                else {
                    itemTouchHelper.attachToRecyclerView(recipeRecycler);
                    layoutParams.height = AnotherUtils.dpToPx(400, this);
                }

                recipeRecycler.setLayoutParams(layoutParams);
            }
        }
    }

    /**
     * Видаляє страву з бази даних після підтвердження користувачем
     */
    private void deleteDish() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_dish))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDish().delete(originalDish)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        Toast.makeText(this, getString(R.string.successful_delete_dish), Toast.LENGTH_SHORT).show();
                                        finish();
                                    },
                                    throwable -> {
                                        Toast.makeText(this, getString(R.string.error_delete_dish), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    /**
     * Переводить інтерфейс у режим редагування страви
     */
    private void editDish() {
        if (flagAccessAnimation.get()) {
            mode = EDIT_MODE;
            startAnimationChangeStylePage(durationAnimation);
        }
    }

    /**
     * Копіює інформацію про страву у вигляді тексту в буфер обміну
     */
    private void copy_as_text() {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", originalDish.getAsText(this));
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, getString(R.string.copy_clipboard_text), Toast.LENGTH_SHORT).show();
    }

    /**
     * Виконує експорт та пересилання файлу через intent
     */
    private void shareDish() {
        if (originalDish != null) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_export))
                    .setMessage(getString(R.string.warning_export))
                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                        startLoadingScreenAnimation();
                        Dish dishExport = new Dish(originalDish);

                        importExportController.exportDish(this, dishExport, new ExportCallbackUri() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    FileUtils.sendFileByUri(EditorDishActivity.this, uri);
                                    FileUtils.deleteFileByUri(EditorDishActivity.this, uri);
                                    Log.d(nameActivity, "Рецепт успішно експортовано");
                                } else Log.d(nameActivity, "Посилання на файл експорту є null");

                                stopLoadingScreenAnimation();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                stopLoadingScreenAnimation();
                                Toast.makeText(EditorDishActivity.this, EditorDishActivity.this.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                Log.e(nameActivity, "Помилка при експорті рецептів", throwable);
                            }

                            @Override
                            public void getDisposable(Disposable disposable) {
                                compositeDisposable.add(disposable);
                            }
                        });
                    })
                    .setNegativeButton(getString(R.string.no), null).show();
        } else {
            Toast.makeText(this, R.string.error_read_get_dish, Toast.LENGTH_SHORT).show();
            Log.d(nameActivity, "Помилка. Страва порожня");
        }
    }

    /**
     * Відкриває діалог для додавання страви до колекцій.
     * Показує список колекцій, які ще не містять цю страву.
     */
    private void showAddDishInCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);

        Disposable disposable = utils.ByCollection().getUnusedByTypeInDish(originalDish, CollectionType.COLLECTION)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        unused_collections -> {
                            if (unused_collections != null && !unused_collections.isEmpty()) {
                                AddDishToCollectionsAdapter adapter = new AddDishToCollectionsAdapter(this, unused_collections);
                                collectionsRecyclerView.setAdapter(adapter);
                                collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                                builder.setView(dialogView)
                                        .setPositiveButton(R.string.button_add, (dialog, which) -> {
                                            ArrayList<Long> selectedCollectionIds = adapter.getSelectedCollectionIds();
                                            if (!selectedCollectionIds.isEmpty()){
                                                Disposable disposable1 = utils.ByDish_Collection().addAll(originalDish, selectedCollectionIds)
                                                        .subscribeOn(Schedulers.newThread())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(
                                                                result -> {
                                                                    if (result) {
                                                                        Toast.makeText(this, getString(R.string.successful_add_dish_in_collection), Toast.LENGTH_SHORT).show();
                                                                        Log.d(nameActivity, "Страва успішно додана в колекцію(ї)");
                                                                        selectedCollectionIds.clear();
                                                                    } else {
                                                                        Log.e(nameActivity, "Помилка. Страва не додана в колекцію(ї)");
                                                                        selectedCollectionIds.clear();
                                                                    }
                                                                },
                                                                throwable -> {
                                                                    Log.e(nameActivity, "Помилка додавання страви в колекцію(ї)", throwable);
                                                                    selectedCollectionIds.clear();
                                                                }
                                                        );
                                                compositeDisposable.add(disposable1);
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                                builder.create().show();
                            } else {
                                Log.e(nameActivity, "Помилка. Список колекцій пустий");
                            }
                        },
                        throwable -> {
                            Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                            Log.e(nameActivity, "Помилка отримання списку колекцій з бд", throwable);
                        }
                );
        compositeDisposable.add(disposable);
    }

    /**
     * Відкриває та закриває бокову панель опцій
     */
    private void onClickSettingDish() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }
}
