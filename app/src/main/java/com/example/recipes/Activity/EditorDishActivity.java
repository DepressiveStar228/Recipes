package com.example.recipes.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.AI.ChatGPT;
import com.example.recipes.AI.ChatGPTClient;
import com.example.recipes.Adapter.AddDishToCollectionsAdapter;
import com.example.recipes.Adapter.IngredientGetAdapter;
import com.example.recipes.Adapter.IngredientSetAdapter;
import com.example.recipes.Adapter.RecipeAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.ImageController;
import com.example.recipes.Controller.TranslateController;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Decoration.AnimationUtils;
import com.example.recipes.Decoration.TextLoadAnimation;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Item.DataBox;
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

public class EditorDishActivity extends AppCompatActivity {
    private Dish originalDish, translateDish;
    private RecyclerView ingredientRecyclerView, recipeRecycler;
    private TextView loadText;
    private TextView nameTextViewContainer, translate;
    private EditText nameEditText, portionEditText;
    private ConstraintLayout ingredientEmpty, recipeEmpty, portionEmpty, editDishActivityContainer, loadScreen, buttonsRecipeContainer, nameDishContainerWithBorder, nameDishContainer;
    private DrawerLayout drawerLayout;
    private LinearLayout linearLayout1, linearLayout2, linearLayout3, linearLayout4, linearLayout5;
    private IngredientSetAdapter ingredientSetAdapter;
    private IngredientGetAdapter ingredientGetAdapter;
    private RecipeAdapter recipeAdapter;
    private ImageView back, setting, addIngredient, addToRecipe, addTextToRecipe, addImageToRecipe;
    private Button setDataButton;
    private Long dishID;
    private RecipeUtils utils;
    private TextLoadAnimation textLoadAnimation;
    private ImageController imageController;
    private TranslateController translateController;
    private String nameActivity;
    private final AtomicBoolean flagAccessAnimation = new AtomicBoolean(true);
    private final AtomicBoolean flagAccessButtonAnimation = new AtomicBoolean(true);
    private final AtomicBoolean flagInitTranslationController = new AtomicBoolean(false);
    private final AtomicBoolean flagIsTranslated = new AtomicBoolean(false);
    private CompositeDisposable compositeDisposable;
    private Runnable startLoading, finishLoading;
    private int mode;
    private final int durationAnimation = 200;
    private final int translationView = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesController preferencesController = new PreferencesController();
        preferencesController.loadPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.editor_dish_activity);

        compositeDisposable = new CompositeDisposable();
        Log.d(nameActivity, "Активність успішно створена");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadScreen = findViewById(R.id.loadScreen);
        startLoadingScreenAnimation();

        compositeDisposable.add(Single.create(emitter -> {
                    loadItemsActivity();
                    loadClickListeners();

                    translateDish = new Dish("");
                    dishID = getIntent().getLongExtra(Config.KEY_DISH, -1);
                    utils = new RecipeUtils(this);
                    imageController = new ImageController(this);
                    nameActivity = this.getClass().getSimpleName();
                    originalDish = new Dish("");

                    emitter.onSuccess(true);
                })
                .flatMapCompletable(status -> setDishRecipesInDish())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (dishID > -1) {
                        mode = Config.READ_MODE;
                        setObserveDish(dishID);
                    } else if (dishID == Config.ADD_MODE) {
                        mode = Config.ADD_MODE;
                        setDataButton.setText(R.string.button_add);
                        setIngredientAdapter();
                        setRecipeAdapter();
                    } else if (dishID == Config.EDIT_MODE){
                        mode = Config.EDIT_MODE;
                        setDataButton.setText(R.string.edit);
                    } else {
                        mode = Config.READ_MODE;
                        Toast.makeText(this,  getString(R.string.error_edit_get_dish), Toast.LENGTH_SHORT).show();
                        Log.e(nameActivity, "Помилка. Не вдалося отримати блюдо на редагування");
                    }

                    stopLoadingScreenAnimation();
                    startAnimationChangeStylePage(0);
                }, Throwable::printStackTrace));


        translateController = new TranslateController(this);
        compositeDisposable.add(translateController.initialization()
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
        Log.d(nameActivity, "Активність успішно закрита");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        else if (dishID >= 0 && mode != Config.READ_MODE) {
            if (flagAccessAnimation.get()) {
                mode = Config.READ_MODE;
                startAnimationChangeStylePage(durationAnimation);
            }
        } else super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof ImageView && flagAccessButtonAnimation.get()) {
                boolean isClickInsideButtons = isClickInsideView(event, addToRecipe) ||
                        isClickInsideView(event, addTextToRecipe) ||
                        isClickInsideView(event, addImageToRecipe);

                if (!isClickInsideButtons) {
                    v.clearFocus();

                    if (addTextToRecipe != null && addToRecipe != null) {
                        if (addTextToRecipe.getVisibility() == View.VISIBLE) hideButtonsRecipe(getRunnableAnimation(addToRecipe));
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1 && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    if (startLoading != null) startLoading.run();
                    imageController.setImageToImageView(selectedImageUri, recipeAdapter, finishLoading);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (imageController.isLoading()) finishLoading.run();
                    }, 10000);
                }
            } else if (requestCode == 2) {
                Uri currentPhotoUri = imageController.getCurrentPhotoUri();
                if (currentPhotoUri != null) {
                    if (startLoading != null) startLoading.run();
                    imageController.setImageToImageView(currentPhotoUri, recipeAdapter, finishLoading);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (imageController.isLoading()) finishLoading.run();
                    }, 10000);
                }
            }
        }
    }

    private boolean isClickInsideView(MotionEvent event, View view) {
        if (view == null) return false;
        Rect outRect = new Rect();
        view.getGlobalVisibleRect(outRect);
        return outRect.contains((int) event.getRawX(), (int) event.getRawY());
    }

    @SuppressLint("ResourceType")
    private void loadItemsActivity(){
        nameTextViewContainer = findViewById(R.id.nameTextViewContainer);
        editDishActivityContainer = findViewById(R.id.editDishActivityContainer);
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
        addToRecipe = findViewById(R.id.add_container_to_recipe);
        addTextToRecipe = findViewById(R.id.add_textContainer_to_recipe);
        addImageToRecipe = findViewById(R.id.add_imageContainer_to_recipe);

        drawerLayout = findViewById(R.id.readDrawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);

            if (headerView != null) {
                linearLayout1 = headerView.findViewById(R.id.setting_dish_edit);
                linearLayout2 = headerView.findViewById(R.id.setting_dish_delete);
                linearLayout3 = headerView.findViewById(R.id.setting_dish_share);
                linearLayout4 = headerView.findViewById(R.id.setting_dish_add_to_collection);
                linearLayout5 = headerView.findViewById(R.id.setting_dish_copy_as_text);
            }
        }

        Log.d(nameActivity, "Елементи активності успішно завантажені");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners() {
        if (editDishActivityContainer != null) {
            editDishActivityContainer.setOnTouchListener((view, motionEvent) -> {
                if (addToRecipe != null && flagAccessAnimation.get()) {
                    addToRecipe.clearFocus();
                    return true;
                }
                return false;
            });
        }

        if (back != null) back.setOnClickListener(v -> finish());
        if (setting != null) { setting.setOnClickListener(this::onClickSettingDish); }

        if (linearLayout1 != null) { linearLayout1.setOnClickListener(v -> editDish()); }
        if (linearLayout2 != null) { linearLayout2.setOnClickListener(v -> deleteDish()); }
        if (linearLayout3 != null) { linearLayout3.setOnClickListener(v -> shareDish()); }
        if (linearLayout4 != null) { linearLayout4.setOnClickListener(v -> showAddDishInCollectionDialog()); }
        if (linearLayout5 != null) { linearLayout5.setOnClickListener(v -> copy_as_text()); }

        if (translate != null) {
            translate.setOnClickListener(v -> {
                if (flagInitTranslationController.get()) {
                    if (!flagIsTranslated.get()) {
                        if (translateDish.getName().isEmpty()) {
                            onTranslationDishClick();
                        } else {
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
                } else {
                    Toast.makeText(this, getString(R.string.error_access_translation), Toast.LENGTH_SHORT).show();
                    Log.d(nameActivity, getString(R.string.error_access_translation));
                }
            });
        }

        if (setDataButton != null) setDataButton.setOnClickListener(view -> {
            if (flagAccessAnimation.get()) onAddOrEditButtonClick();
        });


        if (addIngredient != null) addIngredient.setOnClickListener(v -> {
            if (ingredientRecyclerView != null) {
                Ingredient newIngredient = new Ingredient("", "", "");
                if (ingredientSetAdapter != null) {
                    ingredientSetAdapter.addIngredient(newIngredient);
                    ingredientRecyclerView.smoothScrollToPosition(ingredientSetAdapter.getPositionItem(newIngredient));
                }
            }
        });

        if (addToRecipe != null && addTextToRecipe != null && addImageToRecipe != null && mode != Config.READ_MODE) {
            addToRecipe.setOnClickListener(v -> {
                if (flagAccessButtonAnimation.get()) {
                    v.setClickable(false);

                    if (!v.isFocusable()) addToRecipe.requestFocus();
                    else addToRecipe.clearFocus();
                }
            });

            addToRecipe.setOnFocusChangeListener((v, hasFocus) -> {
                Runnable onAnimationEnd = getRunnableAnimation(v);

                if (!flagAccessButtonAnimation.get()) return;

                flagAccessButtonAnimation.set(false);

                if (hasFocus) showButtonsRecipe(onAnimationEnd);
                else hideButtonsRecipe(onAnimationEnd);
            });
        }

        if (addToRecipe != null && addTextToRecipe != null) {
            addTextToRecipe.setOnClickListener(v -> {
                if (recipeRecycler != null) recipeRecycler.setVisibility(View.VISIBLE);

                recipeAdapter.addItem(new DishRecipe(dishID, DishRecipeType.TEXT));
                addToRecipe.clearFocus();
            });
        }

        if (addToRecipe != null && addImageToRecipe != null) {
            addImageToRecipe.setOnClickListener(v -> {
                if (recipeRecycler != null) recipeRecycler.setVisibility(View.VISIBLE);

                recipeAdapter.addItem(new DishRecipe(dishID, DishRecipeType.IMAGE));
                addToRecipe.clearFocus();
            });
        }

        CharacterLimitTextWatcher.setCharacterLimit(this, nameEditText, Config.CHAR_LIMIT_NAME_DISH);
        CharacterLimitTextWatcher.setCharacterLimit(this, portionEditText, Config.CHAR_LIMIT_PORTION_DISH);

        Log.d(nameActivity, "Слухачі активності успішно завантажені");
    }


    private void setDataIntoItemsActivity(Dish dish) {
        nameEditText.setText(dish.getName());
        if (ingredientRecyclerView != null) setIngredientsToIngredientAdapter(dish);
        if (portionEmpty != null && portionEditText != null) {
            if (dish.getPortion() > 0) portionEditText.setText(String.valueOf(dish.getPortion()));

            if (portionEditText.getText().toString().isEmpty()) portionEmpty.setVisibility(View.VISIBLE);
            else portionEmpty.setVisibility(View.GONE);
        }
    }

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

            if (mode == Config.READ_MODE) ingredientRecyclerView.setAdapter(ingredientGetAdapter);
            else ingredientRecyclerView.setAdapter(ingredientSetAdapter);

            ingredientRecyclerView.setLayoutManager(linearLayoutManager);

            utils.ByIngredient().getViewModel().getNamesUnique().observe(this, data -> {
                if (data != null) {
                    ingredientSetAdapter.setNamesIngredient(new ArrayList<>(data));
                }
            });
        }

        setObserveIngredients();
    }

    private void changeIngredientAdapter() {
        if (mode == Config.READ_MODE) ingredientRecyclerView.setAdapter(ingredientGetAdapter);
        else ingredientRecyclerView.setAdapter(ingredientSetAdapter);
    }

    private void setIngredientsToIngredientAdapter(Dish dish) {
        if (ingredientSetAdapter != null) ingredientSetAdapter.setIngredients(dish.getIngredients());
        if (ingredientGetAdapter != null) ingredientGetAdapter.setIngredients(dish.getIngredients());
    }

    private void setRecipeAdapter() {
        if (recipeRecycler != null) {
            if (recipeAdapter == null) {
                recipeAdapter = new RecipeAdapter(this, recipeEmpty, mode == Config.READ_MODE, new RecipeAdapter.ImageClickListener() {
                    @Override
                    public void onImageClick(AppCompatImageView imageView, TextView textView) {
                        if (imageController != null) {
                            TextLoadAnimation textLoadImageAnimation = new TextLoadAnimation(textView, getString(R.string.loading));

                            imageController.setImageView(imageView);

                            AlertDialog.Builder builder = new AlertDialog.Builder(EditorDishActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.dialog_choose_way_add_image, null);

                            if (dialogView != null) {
                                builder.setView(dialogView);
                                LinearLayout photo, gallery;
                                photo = dialogView.findViewById(R.id.photo);
                                gallery = dialogView.findViewById(R.id.gallery);

                                if (photo != null && gallery != null) {
                                    photo.setOnClickListener(view -> imageController.openCamera(EditorDishActivity.this));
                                    gallery.setOnClickListener(view -> imageController.openGallery(EditorDishActivity.this));
                                }
                            }

                            Dialog dialog =  builder.create();
                            dialog.show();

                            startLoading = () -> {
                                if (dialogView != null) dialog.dismiss();
                                imageView.setVisibility(View.INVISIBLE);
                                textLoadImageAnimation.startAnimation();
                            };

                            finishLoading = () -> {
                                textLoadImageAnimation.stopAnimation();
                                imageView.setVisibility(View.VISIBLE);
                            };
                        }
                    }

                    @Override
                    public void onDeleteClick(DishRecipe dishRecipe) {
                        Disposable disposable = utils.ByDishRecipe().delete(dishRecipe)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> {
                                            imageController.setImageView(null);
                                            Log.d("RecipeUtils", "Елемент рецепту успішно видалено");
                                        },
                                        throwable -> Log.e("RecipeUtils", "Помилка: " + throwable.getMessage())
                                );
                        compositeDisposable.add(disposable);
                    }
                });

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

                recipeRecycler.setAdapter(recipeAdapter);
                recipeRecycler.addItemDecoration(new VerticalSpaceItemDecoration(-4));
                recipeRecycler.setLayoutManager(linearLayoutManager);

                ItemTouchHelper itemTouchHelper = recipeAdapter.getItemTouchHelper();
                itemTouchHelper.attachToRecyclerView(recipeRecycler);

                recipeRecycler.getRecycledViewPool().clear();
                recipeAdapter.setItems(originalDish.getRecipes());
            }
        }
    }

    public void setObserveDish(long newID) {
        dishID = newID;

        utils.ByDish().getViewModel().getByID(dishID).observe(this, data -> {
            if (data != null) {
                data.setRecipes(originalDish.getRecipes());
                originalDish = data;
                setDataIntoItemsActivity(originalDish);
                setIngredientAdapter();
                setRecipeAdapter();
            }
        });
    }

    private void setObserveIngredients() {
        utils.ByIngredient().getViewModel().getAllByIDDish(dishID).observe(this, data -> {
            if (data != null) {
                originalDish.setIngredients(new ArrayList<>(data));
                setIngredientsToIngredientAdapter(originalDish);
            }
        });
    }

    private Completable setDishRecipesInDish() {
        return utils.ByDishRecipe().getByDishID(dishID)
                .flatMapObservable(dishRecipes -> Observable.fromIterable(dishRecipes)
                        .flatMapSingle(dishRecipe -> {
                            if (dishRecipe.getTypeData() == DishRecipeType.IMAGE) {
                                if (!dishRecipe.getTextData().isEmpty()) {
                                    return imageController.loadImageFromPath(dishRecipe.getTextData())
                                            .flatMap(bytes -> imageController.decodeByteArrayToBitmap(bytes))
                                            .flatMap(bitmap -> {
                                                if (bitmap != null && bitmap.getByteCount() != 0) {
                                                    dishRecipe.setBitmap(bitmap);
                                                    originalDish.addRecipe(dishRecipe);
                                                    return Single.just(true);
                                                } else return Single.just(false);
                                            });
                                } else {
                                    return Single.just(false);
                                }
                            } else {
                                if (!dishRecipe.getTextData().isEmpty()) {
                                    originalDish.addRecipe(dishRecipe);
                                    return Single.just(true);
                                } else return Single.just(false);
                            }
                        }))
                .toList()
                .flatMapCompletable(results -> {
                    for (Boolean result : results) {
                        if (!result) {
                            Log.d("RecipeUtils", "Помилка. Щось не завантажено");
                            return Completable.error(new Throwable());
                        }
                    }
                    Log.d("RecipeUtils", "Успішно завантажено всі рецепти");
                    return Completable.complete();
                });
    }

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
                                        if (in.getType().isEmpty()) {
                                            in.setType(utils.getNameIngredientType(Config.VOID));
                                        }

                                        if (in.getName().isEmpty()) {
                                            flagFullIngredient = false;
                                            break;
                                        }
                                    }

                                    for (DishRecipe dishRecipe : dishRecipes) {
                                        if (dishRecipe.getTypeData() == DishRecipeType.IMAGE) {
                                            if (dishRecipe.getBitmap() == null || dishRecipe.getBitmap().getByteCount() <= 0) {
                                                flagFullRecipeItem = false;
                                                break;
                                            }
                                        } else if (dishRecipe.getTypeData() == DishRecipeType.TEXT) {
                                            if (dishRecipe.getTextData().isEmpty()) {
                                                flagFullRecipeItem = false;
                                                break;
                                            }
                                        }
                                    }

                                    Dish newDish = new Dish(dishID, name, portion.get(), ingredients, dishRecipes);

                                    if (!flagFullIngredient || !flagFullRecipeItem) {
                                        stopLoadingScreenAnimation();
                                        Toast.makeText(this, getString(R.string.warning_set_all_data), Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (mode == Config.ADD_MODE) { addDish(newDish); }
                                        else if (mode == Config.EDIT_MODE) { editDish(newDish); }
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

    private void addDish(Dish dish) {
        Disposable addDishDisposable = utils.ByDish().add(dish, Integer.toUnsignedLong(Config.ID_MY_RECIPE_COLLECTION))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        id -> {
                            if (id > 0) {
                                if (dishID == null) setObserveDish(id);
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
                                mode = Config.READ_MODE;
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
            textLoadAnimation = new TextLoadAnimation(loadText, getString(R.string.loading));
            textLoadAnimation.startAnimation();
        }
    }

    private void stopLoadingScreenAnimation() {
        if (loadScreen != null && loadText != null) {
            ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
            rootView.removeView(loadText);
            textLoadAnimation.stopAnimation();
            loadText = null;
            loadScreen.setVisibility(View.GONE);
        }
    }

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

    private Single<Boolean> updateRecipe(Dish newDish) {
        return utils.ByDishRecipe().getByDishID(dishID)
                .flatMap(dishRecipes -> {
                    ArrayList<DishRecipe> newDishRecipes = newDish.getRecipes();

                    if (dishRecipes != null && !dishRecipes.isEmpty()) {
                        return utils.ByDishRecipe().deleteAll(new ArrayList<>(dishRecipes))
                                .flatMap(status -> {
                                    if (!status) {
                                        Log.e(nameActivity, "Помилка видалення елементів рецепту страви з БД.");
                                    }

                                    if (newDishRecipes == null) {
                                        return Single.just(new ArrayList<DishRecipe>());
                                    } else {
                                        return Single.just(newDishRecipes);
                                    }
                                });
                    } else {
                        Log.d(nameActivity, "Список елементів рецепту страви пустий.");
                        if (newDishRecipes == null || newDishRecipes.isEmpty()) {
                            return Single.just(new ArrayList<DishRecipe>());
                        } else {
                            return Single.just(newDishRecipes);
                        }
                    }
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

    private void onTranslationDishClick() {
        if (translateController != null) {
            TextLoadAnimation textLoadAnimation = new TextLoadAnimation(translate, getString(R.string.loading));
            textLoadAnimation.startAnimation();

            ArrayList<Integer> positionTextRecipe = new ArrayList<>();
            for (DishRecipe dishRecipe : originalDish.getRecipes()) {
                if (dishRecipe.getTypeData() == DishRecipeType.TEXT) positionTextRecipe.add(dishRecipe.getPosition());
            }

            Disposable disposable = translateController.translateRecipe(originalDish)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(dish -> {
                        if (dish != null) {
                            translateDish = new Dish(dish);
                            translateDish.setId(originalDish.getId());
                            setDataIntoItemsActivity(translateDish);
                            translateDish.setRecipes(new ArrayList<>());
                            if (positionTextRecipe.size() == dish.getRecipes().size()) {
                                for (int i = 0, j = 0; i < originalDish.getRecipes().size(); i++) {
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
                        }
                        else {
                            flagIsTranslated.set(false);
                            textLoadAnimation.setBaseText(getString(R.string.translate_button_text_false));
                            textLoadAnimation.stopAnimation();
                        }
                    });
            compositeDisposable.add(disposable);
        }
    }

    private Runnable getRunnableAnimation(View v) {
        return () -> {
            flagAccessButtonAnimation.set(true);
            v.setClickable(true);
        };
    }

    private void showButtonsRecipe(Runnable onAnimationEnd) {
        addTextToRecipe.setClickable(true);
        addImageToRecipe.setClickable(true);
        AnimationUtils.smoothRotation(addToRecipe, AnimationUtils.RIGHT, durationAnimation, onAnimationEnd);
        AnimationUtils.smoothSlipVisibility(addTextToRecipe, AnimationUtils.RIGHT, AnimationUtils.SHOW, translationView, durationAnimation, onAnimationEnd);
        AnimationUtils.smoothSlipVisibility(addImageToRecipe, AnimationUtils.LEFT, AnimationUtils.SHOW, translationView, durationAnimation, onAnimationEnd);
    }

    private void hideButtonsRecipe(Runnable onAnimationEnd) {
        addTextToRecipe.setClickable(false);
        addImageToRecipe.setClickable(false);
        AnimationUtils.smoothRotation(addToRecipe, AnimationUtils.LEFT, durationAnimation, onAnimationEnd);
        AnimationUtils.smoothSlipVisibility(addTextToRecipe, AnimationUtils.RIGHT, AnimationUtils.HIDE, translationView, durationAnimation, onAnimationEnd);
        AnimationUtils.smoothSlipVisibility(addImageToRecipe, AnimationUtils.LEFT, AnimationUtils.HIDE, translationView, durationAnimation, onAnimationEnd);
    }

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

    private void setBiasNameEditText(float bias) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) nameEditText.getLayoutParams();
        params.horizontalBias = bias;
        nameEditText.setLayoutParams(params);
        nameEditText.requestLayout();
    }

    private void setWidthNameEditText(boolean isRead) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) nameEditText.getLayoutParams();
        params.width = isRead ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
        nameEditText.setLayoutParams(params);
        nameEditText.requestLayout();
    }

    private void startAnimationChangeStylePage(int duration) {
        boolean isRead = mode == Config.READ_MODE;

        if (flagAccessAnimation.get()) {
            flagAccessAnimation.set(false);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
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

                if (mode == Config.READ_MODE) {
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
                    endSet.setHorizontalBias(nameEditText.getId(), (mode == Config.READ_MODE) ? 0.5f : 0f);

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
                    if (mode != Config.READ_MODE) {
                        alphaBackgroundAnimator.setStartDelay(duration);
                        alphaBackgroundAnimator.setDuration(duration*3L);
                    }
                    else alphaBackgroundAnimator.setDuration(duration/2L);
                    alphaBackgroundAnimator.start();
                }

                ObjectAnimator moveUpAnimator = ObjectAnimator.ofFloat(nameDishContainerWithBorder, "translationY", isRead ? 0f : -50f, isRead ? -50f : 0f);
                moveUpAnimator.setDuration(duration);

                ObjectAnimator alphaNameContainerAnimator = ObjectAnimator.ofFloat(nameTextViewContainer, "alpha", isRead ? 1f : 0f, isRead ? 0f : 1f);
                if (mode != Config.READ_MODE) {
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
                    if (mode == Config.ADD_MODE) setDataButton.setText(R.string.button_add);
                    else if (mode == Config.EDIT_MODE) setDataButton.setText(R.string.edit);

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

                if (!addToRecipe.isFocused()) hideButtonsRecipe(getRunnableAnimation(addToRecipe));
            }
            if (addIngredient != null) {
                if (isRead) AnimationUtils.smoothVisibility(addIngredient, AnimationUtils.HIDE, duration);
                else AnimationUtils.smoothVisibility(addIngredient, AnimationUtils.SHOW, duration);
            }
            if (ingredientSetAdapter != null && ingredientGetAdapter != null) changeIngredientAdapter();
            if (recipeAdapter != null) {
                recipeAdapter.setReadMode(isRead);
            }
        }
    }

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

    private void editDish() {
        if (flagAccessAnimation.get()) {
            mode = Config.EDIT_MODE;
            startAnimationChangeStylePage(durationAnimation);
        }
    }

    private void copy_as_text() {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", originalDish.getAsText(this));
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, getString(R.string.copy_clipboard_text), Toast.LENGTH_SHORT).show();
    }

    private void shareDish() {
        if (originalDish != null) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_export))
                    .setMessage(getString(R.string.warning_export))
                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                        ImportExportController.exportDish(this, originalDish, new ExportCallbackUri() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    FileUtils.sendFileByUri(EditorDishActivity.this, uri);
                                    FileUtils.deleteFileByUri(EditorDishActivity.this, uri);
                                    Log.d(nameActivity, "Рецепт успішно експортовано");
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Toast.makeText(EditorDishActivity.this, EditorDishActivity.this.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                Log.e("ImportExportController", "Помилка при експорті рецептів", throwable);
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

    private void showAddDishInCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);

        Disposable disposable = utils.ByCollection().getUnusedInDish(originalDish)
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

    private void onClickSettingDish(View view) {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }
}
