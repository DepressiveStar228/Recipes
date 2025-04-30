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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.AI.Translator;
import com.example.recipes.Adapter.CollectionAdapter;
import com.example.recipes.Adapter.CollectionGetAdapter;
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
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.Option.CollectionOptions;
import com.example.recipes.Item.Option.DishOptions;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.Utils.ClassUtils;
import com.example.recipes.Utils.Dialogues;
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
    private Collection myRecipeCollection;
    private ArrayList<Collection> allCollections = new ArrayList<>();

    // Флаги для управління анімацією та перекладом
    private final AtomicBoolean flagAccessAnimation = new AtomicBoolean(true);
    private final AtomicBoolean flagInitTranslationController = new AtomicBoolean(false);
    private final AtomicBoolean flagIsTranslated = new AtomicBoolean(false);

    // Налаштування анімації
    private final int durationAnimation = 200;

    // Об'єкти інтерфейсу
    private RecyclerView ingredientRecyclerView, recipeRecycler, collectionRecycler;
    private ItemTouchHelper itemTouchHelper;
    private TextView loadText;
    private TextView nameTextViewContainer, translate;
    private EditText nameEditText, portionEditText;
    private ConstraintLayout ingredientEmpty, recipeEmpty, portionEmpty, collectionEmpty, loadScreen, buttonsRecipeContainer, nameDishContainerWithBorder, nameDishContainer;
    private DrawerLayout drawerLayout;
    private ConstraintLayout editDishMenuItem, deleteDishMenuItem, shareDishMenuItem, copyAsTextMenuItem;
    private IngredientSetAdapter ingredientSetAdapter;
    private IngredientGetAdapter ingredientGetAdapter;
    private CollectionAdapter collectionAdapter;
    private RecipeAdapter recipeAdapter;
    private ImageView back, setting, addIngredient, addTextToRecipe, addImageToRecipe, addCollection;
    private Button setDataButton;

    private RecipeUtils utils; // Класс утиліт для роботи з рецептами через БД

    // Контролери
    private TextLoadAnimation screenLoad, imageLoad;
    private ImageController imageController;
    private Translator translator;
    private DishOptions dishOptions;
    private Dialogues dialogues;
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

            nameActivity = this.getClass().getSimpleName();
            originalDish = new Dish("");
            imageLoad = new TextLoadAnimation(getString(R.string.loading));
            dialogues = new Dialogues(this);
            dishOptions = new DishOptions(this, compositeDisposable);

            getAllCollection();

            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Визначення режиму роботи активності
                    if (dishID > -1) {
                        mode = READ_MODE;
                        setObserveDish();
                        setIngredientAdapter();
                        setRecipeAdapter();
                        setCollectionAdapter();
                    } else if (dishID == ADD_MODE) {
                        mode = ADD_MODE;
                        setDataButton.setText(R.string.button_add);
                        setIngredientAdapter();
                        setRecipeAdapter();
                        setCollectionAdapter();
                    } else if (dishID == EDIT_MODE) {
                        mode = EDIT_MODE;
                        setDataButton.setText(R.string.edit);
                        setObserveDish();
                        setIngredientAdapter();
                        setRecipeAdapter();
                        setCollectionAdapter();
                    } else {
                        mode = READ_MODE;
                        setIngredientAdapter();
                        setRecipeAdapter();
                        setCollectionAdapter();
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
    protected void onResume() {
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
    private void loadItemsActivity() {
        nameTextViewContainer = findViewById(R.id.nameTextViewContainer);
        nameDishContainerWithBorder = findViewById(R.id.nameDishContainerWithBorder);
        nameDishContainer = findViewById(R.id.nameDishContainer);
        buttonsRecipeContainer = findViewById(R.id.buttonsRecipeContainer);

        translate = findViewById(R.id.translate);
        ingredientEmpty = findViewById(R.id.ingredientEmpty);
        recipeEmpty = findViewById(R.id.recipeEmpty);
        portionEmpty = findViewById(R.id.portionEmpty);
        collectionEmpty = findViewById(R.id.collectionEmpty);

        nameEditText = findViewById(R.id.nameEditTextEditAct);
        portionEditText = findViewById(R.id.portionEditTextEditAct);

        setDataButton = findViewById(R.id.editButton);
        ingredientRecyclerView = findViewById(R.id.addIngredientRecyclerViewEditAct);
        recipeRecycler = findViewById(R.id.addRecipeRecycler);
        collectionRecycler = findViewById(R.id.addCollectionRecyclerView);

        setting = findViewById(R.id.imageSettingImageView);
        back = findViewById(R.id.backImageView);
        addIngredient = findViewById(R.id.addIngredientButtonEditAct);
        addTextToRecipe = findViewById(R.id.add_textContainer_to_recipe);
        addImageToRecipe = findViewById(R.id.add_imageContainer_to_recipe);
        addCollection = findViewById(R.id.addCollectionButton);

        // Бокове меню опцій
        drawerLayout = findViewById(R.id.readDrawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);

            if (headerView != null) {
                editDishMenuItem = headerView.findViewById(R.id.setting_dish_edit);
                deleteDishMenuItem = headerView.findViewById(R.id.setting_dish_delete);
                shareDishMenuItem = headerView.findViewById(R.id.setting_dish_share);
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
        if (editDishMenuItem != null) {
            editDishMenuItem.setOnClickListener(v -> dishOptions.editDish(() -> {
                if (flagAccessAnimation.get()) {
                    mode = EDIT_MODE;
                    startAnimationChangeStylePage(durationAnimation);
                }
            }));
        }
        if (deleteDishMenuItem != null) {
            deleteDishMenuItem.setOnClickListener(v -> dishOptions.deleteDish(originalDish, this::finish));
        }
        if (shareDishMenuItem != null) {
            shareDishMenuItem.setOnClickListener(v -> dishOptions.shareDish(
                    new Dish(originalDish),
                    this::startLoadingScreenAnimation,
                    this::stopLoadingScreenAnimation,
                    this::stopLoadingScreenAnimation
            ));
        }
        if (copyAsTextMenuItem != null) { copyAsTextMenuItem.setOnClickListener(v -> dishOptions.copy_as_text(originalDish)); }

        // Кнопка перекладу
        if (translate != null) {
            translate.setOnClickListener(v -> {
                if (!flagInitTranslationController.get()) {
                    Toast.makeText(this, getString(R.string.error_access_translation), Toast.LENGTH_SHORT).show();
                    Log.d(nameActivity, getString(R.string.error_access_translation));
                }
                else if (translator.getClient().checkDailyRequestLimitLimit()) {
                    if (!flagIsTranslated.get()) {
                        if (translateDish.getName().isEmpty() && translator.getClient().checkLastTimeRequest()) {
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

        // Кнопка додавання колекції
        if (addCollection != null) {
            addCollection.setOnClickListener(v -> {
                if (collectionAdapter != null) {
                    ArrayList<Collection> oldList = new ArrayList<>(collectionAdapter.getCollections());

                    if (dialogues != null) {
                        dialogues.dialogChooseItems(allCollections, oldList,
                                selectedCollections -> {
                                    ArrayList<Collection> addedItems = new ArrayList<>(selectedCollections);
                                    addedItems.removeAll(oldList);

                                    ArrayList<Collection> removedItems = new ArrayList<>(oldList);
                                    removedItems.removeAll(selectedCollections);

                                    collectionAdapter.addCollections(addedItems);
                                    collectionAdapter.deleteCollections(removedItems);
                                }, R.string.collections_dish_text, R.string.button_add);
                    }
                }
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
        if (nameEditText != null) {
            nameEditText.setText(dish.getName());
            setWightNameEditText(true);
        }
        if (ingredientRecyclerView != null) setIngredientsToIngredientAdapter(dish);
        if (recipeRecycler != null && recipeAdapter != null) recipeAdapter.setItems(dish.getRecipes());
        if (portionEmpty != null && portionEditText != null) {
            if (dish.getPortion() > 0) portionEditText.setText(String.valueOf(dish.getPortion()));
            else portionEditText.setText("");

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

    private void setCollectionAdapter() {
        if (collectionRecycler != null) {
            if (collectionAdapter == null) {
                collectionAdapter = new CollectionAdapter(this, mode == READ_MODE, collectionEmpty);
            }

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            collectionRecycler.setAdapter(collectionAdapter);
            collectionRecycler.setLayoutManager(linearLayoutManager);
            collectionRecycler.setHasFixedSize(true);

            if (dishID > 0) {
                utils.ByDish_Collection().getViewModel().getAllCollectionIDs(dishID).observe(this, data -> {
                    if (data != null) {
                        Disposable disposable = Observable.fromIterable(data)
                                .flatMapSingle(collectionID -> utils.ByCollection().getByID(collectionID))
                                .toList()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        collectionList -> {
                                            if (collectionAdapter != null) collectionAdapter.setCollections(new ArrayList<>(collectionList));
                                        },
                                        throwable -> Log.e(nameActivity, "Помилка отримання колекцій: " + throwable.getMessage())
                                );
                        compositeDisposable.add(disposable);
                    }
                });
            } else setMyRecipeCollectionToAdapter();
        }
    }

    private void setMyRecipeCollectionToAdapter() {
        if (mode != READ_MODE && collectionAdapter != null) {
            if (myRecipeCollection == null) {
                Disposable disposable = utils.ByCollection().getByID(ID_System_Collection.ID_MY_RECIPE.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                collection -> {
                                    myRecipeCollection = collection;
                                    collectionAdapter.addCollection(myRecipeCollection);
                                },
                                throwable -> Log.e(nameActivity, "Помилка отримання колекцій: " + throwable.getMessage())
                        );

                compositeDisposable.add(disposable);
            } else {
                collectionAdapter.deleteCollection(myRecipeCollection);
            }
        }
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
        catch (Exception e) { }

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
        ArrayList<Collection> collections = new ArrayList<>();
        if (collectionAdapter != null) {
            collections = collectionAdapter.getCollections();
        }

        Disposable addDishDisposable = utils.ByDish().add(dish, collections)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        id -> {
                            if (id > 0) {
                                if (dishID == null) {
                                    dishID = id;
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
                .andThen(Single.zip(
                        updateIngredient(newDish),
                        updateRecipe(newDish),
                        updateCollection(newDish),
                        (ingredientStatus, recipeStatus, collectionStatus) -> ingredientStatus && recipeStatus && collectionStatus
                ))
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
     * Оновлює колекції страви в базі даних
     * Спочатку видаляє всі поточні колекції, а потім додає нові
     *
     * @param newDish Страва для оновлення
     * @return Single<Boolean> Результат операції (успішно/неуспішно)
     */
    private Single<Boolean> updateCollection(Dish newDish) {
        final ArrayList<Collection> collections = new ArrayList<>();
        if (collectionAdapter != null) collections.addAll(collectionAdapter.getCollections());

        return utils.ByDish_Collection().getByIDDish(newDish.getId())
                .flatMap(dish_collections -> utils.ByDish_Collection().deleteAll(new ArrayList<>(dish_collections)))
                .flatMap(status -> {
                    if (!status) {
                        Toast.makeText(this, getString(R.string.error_edit_dish), Toast.LENGTH_SHORT).show();
                        Log.e(nameActivity, "Помилка видалення колекцій страви з БД.");
                    }

                    return utils.ByDish_Collection().addAll(newDish, collections);
                });
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
     * Встановлює ширину поля для введення назви страви
     * Визначає оптимальну ширину поля в залежності від напрямку анімації
     *
     * @param isRead true - режим читання, false - режим редагування
     */
    private void setWightNameEditText(boolean isRead) {
        int matchParentWidth = nameDishContainerWithBorder.getWidth();

        nameEditText.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int newWidth = nameEditText.getMeasuredWidth();

        ViewGroup.LayoutParams params = nameEditText.getLayoutParams();

        if (isRead) params.width = Math.min(matchParentWidth, newWidth);
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
            }, duration * 5L);

            if (nameEditText != null && nameDishContainerWithBorder != null && nameTextViewContainer != null && nameDishContainer != null) {
                ValueAnimator textSizeAnimator = ValueAnimator.ofFloat(isRead ? 20f : 28f, isRead ? 28f : 20f);
                textSizeAnimator.setDuration(duration == 0 ? duration + 10 : duration);
                textSizeAnimator.addUpdateListener(animation -> {
                    float animatedValue = (float) animation.getAnimatedValue();
                    nameEditText.setTextSize(animatedValue);
                    setWightNameEditText(isRead);
                });
                textSizeAnimator.start();

                if (mode == READ_MODE) {
                    if (duration > 0) {
                        ValueAnimator biasAnimator = ValueAnimator.ofFloat(0f, 0.5f);
                        biasAnimator.setDuration(duration);
                        biasAnimator.addUpdateListener(animation -> {
                            float bias = (float) animation.getAnimatedValue();
                            setBiasNameEditText(bias);
                        });
                        biasAnimator.start();
                    } else setBiasNameEditText(0.5f);
                } else {
                    ConstraintSet startSet = new ConstraintSet();
                    startSet.clone(nameDishContainer);

                    ConstraintSet endSet = new ConstraintSet();
                    endSet.clone(nameDishContainer);
                    endSet.setHorizontalBias(nameEditText.getId(), 0f);

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
                        alphaBackgroundAnimator.setStartDelay((long) (duration * 1.5));
                        alphaBackgroundAnimator.setDuration(duration * 3L);
                    }
                    else alphaBackgroundAnimator.setDuration(duration / 2L);
                    alphaBackgroundAnimator.start();
                }

                ObjectAnimator moveUpAnimator = ObjectAnimator.ofFloat(nameDishContainerWithBorder, "translationY", isRead ? 0f : -50f, isRead ? -50f : 0f);
                moveUpAnimator.setDuration(duration);

                ObjectAnimator alphaNameContainerAnimator = ObjectAnimator.ofFloat(nameTextViewContainer, "alpha", isRead ? 1f : 0f, isRead ? 0f : 1f);
                if (mode != READ_MODE) {
                    alphaNameContainerAnimator.setStartDelay((long) (duration * 1.5));
                    alphaNameContainerAnimator.setDuration(duration * 3L);
                } else alphaNameContainerAnimator.setDuration(duration / 2L);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(alphaNameContainerAnimator, moveUpAnimator);
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

            if (drawerLayout != null) {
                if (isRead) drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                else drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            switchVisibilityToMode(setDataButton, duration);
            if (setDataButton != null && !isRead) {
                if (mode == ADD_MODE) setDataButton.setText(R.string.button_add);
                else if (mode == EDIT_MODE) setDataButton.setText(R.string.edit);
            }

            switchVisibilityToModeReverse(translate, duration);
            switchVisibilityToModeReverse(setting, duration);

            switchVisibilityToMode(buttonsRecipeContainer, duration);
            switchVisibilityToMode(addIngredient, duration);
            switchVisibilityToMode(addCollection, duration);

            if (ingredientSetAdapter != null && ingredientGetAdapter != null && ingredientRecyclerView != null) {
                ViewGroup.LayoutParams layoutParams = ingredientRecyclerView.getLayoutParams();

                if (isRead) {
                    ingredientRecyclerView.setAdapter(ingredientGetAdapter);
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    ingredientRecyclerView.setNestedScrollingEnabled(false);
                }
                else {
                    ingredientRecyclerView.setAdapter(ingredientSetAdapter);
                    layoutParams.height = AnotherUtils.dpToPx(300, this);
                    ingredientRecyclerView.setNestedScrollingEnabled(true);
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
                    recipeRecycler.setNestedScrollingEnabled(false);
                }
                else {
                    itemTouchHelper.attachToRecyclerView(recipeRecycler);
                    layoutParams.height = AnotherUtils.dpToPx(400, this);
                    recipeRecycler.setNestedScrollingEnabled(true);
                }

                recipeRecycler.setLayoutParams(layoutParams);
            }
            if (collectionAdapter != null) collectionAdapter.setReadMode(isRead);
            if (collectionRecycler != null) {
                ViewGroup.LayoutParams layoutParams = collectionRecycler.getLayoutParams();

                if (isRead) {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    collectionRecycler.setNestedScrollingEnabled(false);
                } else {
                    layoutParams.height = AnotherUtils.dpToPx(200, this);
                    collectionRecycler.setNestedScrollingEnabled(true);
                }

                collectionRecycler.setLayoutParams(layoutParams);
            }
        }
    }

    /**
     * Перемикає видимість елемента в залежності від режиму
     * Якщо режим - читання, то елемент приховується, і навпаки
     *
     * @param view    Елемент, чию видимість потрібно перемкнути
     * @param duration Тривалість анімації
     */
    private void switchVisibilityToMode(View view, int duration) {
        if (view != null) {
            if (mode == READ_MODE) AnimationUtils.smoothVisibility(view, AnimationUtils.HIDE, duration);
            else AnimationUtils.smoothVisibility(view, AnimationUtils.SHOW, duration);
        }
    }

    /**
     * Перемикає видимість елемента в залежності від режиму
     * Якщо режим - читання, то елемент з'являється, і навпаки
     *
     * @param view    Елемент, чию видимість потрібно перемкнути
     * @param duration Тривалість анімації
     */
    private void switchVisibilityToModeReverse(View view, int duration) {
        if (view != null) {
            if (mode == READ_MODE) AnimationUtils.smoothVisibility(view, AnimationUtils.SHOW, duration);
            else AnimationUtils.smoothVisibility(view, AnimationUtils.HIDE, duration);
        }
    }

    /**
     * Отримує всі колекції з бази даних
     * Використовується для заповнення адаптера колекцій
     */
    private void getAllCollection() {
        Disposable disposable = utils.ByCollection().getAllByType(CollectionType.COLLECTION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listData -> {
                    if (ClassUtils.isListOfType(listData, Collection.class)) {
                        allCollections = ClassUtils.getListOfType(listData, Collection.class);
                    }
                });

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
