package com.example.recipes.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.recipes.Adapter.ViewPagerAdapter;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Decoration.LoadScreenAnimation;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Fragments.HomeFragment;
import com.example.recipes.Fragments.SettingPanel;
import com.example.recipes.Fragments.ShoplistFragment;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Fragments.CollectionsDishFragment;
import com.example.recipes.Item.Collection;
import com.example.recipes.Utils.Dialogues;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
public class MainActivity extends FragmentActivity {
    private DrawerLayout drawerLayout;
    private PreferencesController preferencesController;
    private RecipeUtils utils;
    private ImportExportController importExportController;
    private LoadScreenAnimation loadScreenAnimation;
    private ConstraintLayout loadScreen;
    private ImageView img1, img3, img4, img5;
    private List<ImageView> imageViews;
    private SettingPanel settingPanel;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewAdapter;
    private Dialogues dialogues;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferencesController = PreferencesController.getInstance();

        super.onCreate(savedInstanceState);
        preferencesController.setPreferencesToActivity(this);
        setContentView(R.layout.activity_main);

        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadScreen = findViewById(R.id.loadScreen);
        drawerLayout = findViewById(R.id.drawerLayout);

        if (drawerLayout != null) {
            settingPanel = new SettingPanel(this, drawerLayout);
            settingPanel.lockSettingPanel();
        }

        loadScreenAnimation = new LoadScreenAnimation(this, loadScreen);
        loadScreenAnimation.startLoadingScreenAnimation();  // Початок анімації завантаження активності

        compositeDisposable.add(Completable.fromAction(() -> {
            utils = RecipeUtils.getInstance(this);
            importExportController = new ImportExportController(this);
            dialogues = new Dialogues(this);
            initialDB(); // Створення системний колекцій, якщо їх немає
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    initializeUI(); // Ініціалізація фрагментів та об'єктів на активності
                    loadScreenAnimation.stopLoadingScreenAnimation();  // Кінець анімації завантаження активності
                    if (settingPanel != null) settingPanel.unlockSettingPanel();

                }, Throwable::printStackTrace));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (settingPanel != null) { settingPanel.clearDisposables(); }
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
            if (settingPanel != null) {
                if (settingPanel.isDrawerOpen()) { // Якщо панель налаштувань відкрита, то вона закриється
                    settingPanel.closeDrawer();
                } else if (viewPager.getCurrentItem() != 0) { // Якщо фрагмент не перший (фрагмент пошуку), то перегорнеться на перший
                    viewPager.setCurrentItem(0);
                } else {
                    // Діалог підтвердження виходу із застосунку
                    dialogues.dialogExit(() -> {
                        super.onBackPressed();
                        finish();
                    }, null);
                }

            } else {
                // Діалог підтвердження виходу із застосунку
                dialogues.dialogExit(() -> {
                    super.onBackPressed();
                    finish();
                }, null);
            }
        }
    }

    private void loadItemsActivity() {
        imageViews = new ArrayList<>();

        LinearLayout linearLayout = findViewById(R.id.main_menu);
        ConstraintLayout constraintLayout = findViewById(R.id.main_header);

        if (linearLayout != null) {
            img1 = linearLayout.findViewById(R.id.main_home_hub);
            img4 = linearLayout.findViewById(R.id.main_collections_hub);
            img5 = linearLayout.findViewById(R.id.main_shopList_hub);

            if (img1 != null) { imageViews.add(img1); }
            if (img4 != null) { imageViews.add(img4); }
            if (img5 != null) { imageViews.add(img5); }
        }
        if (constraintLayout != null) {
            img3 = constraintLayout.findViewById(R.id.setting);
        }

        setCurrentMenuSelect(0);
        Log.d("MainActivity", "Завантаження всіх об'єктів активності");
    }

    /**
     * Ініціалізує UI та різні фрагменти.
     */
    private void initializeUI() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new CollectionsDishFragment());
        fragmentList.add(new ShoplistFragment());

        viewPager = findViewById(R.id.viewPager);
        viewAdapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(viewAdapter);

        loadItemsActivity();
        loadClickListeners();
    }

    /**
     * Робить фрагмент поточним
     *
     * @param id повідомлення
     */
    private void setCurrentMenuSelect(int id) {
        if (!imageViews.isEmpty()) {
            for (ImageView view : imageViews) {
                view.setSelected(false);
            }

            imageViews.get(id).setSelected(true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners() {
        View.OnClickListener imageClickListener = v -> {
            Log.d("MainActivity", "Слухач помітив зміну фрагмента");
            animateImage(v);

            if (v.getId() == R.id.main_home_hub) {
                viewPager.setCurrentItem(0);
                setCurrentMenuSelect(0);
            } else if (v.getId() == R.id.setting) {
                settingPanel.onClickSetting();
            } else if (v.getId() == R.id.main_collections_hub) {
                viewPager.setCurrentItem(1);
                setCurrentMenuSelect(1);
            } else if (v.getId() == R.id.main_shopList_hub) {
                viewPager.setCurrentItem(2);
                setCurrentMenuSelect(2);
            }
        };

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentMenuSelect(position);
            }
        });

        img1.setOnClickListener(imageClickListener);
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
                if (uri != null) {
                    if (loadScreenAnimation != null && settingPanel != null) {
                        // Початок анімації завантаження активності
                        loadScreenAnimation.startLoadingScreenAnimation();
                        settingPanel.lockSettingPanel();
                    }

                    Disposable disposable = importExportController.importRecipeDataToFile(this, uri)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        if (loadScreenAnimation != null && settingPanel != null) {
                                            // Закінчення анімації завантаження активності
                                            loadScreenAnimation.stopLoadingScreenAnimation();
                                            settingPanel.unlockSettingPanel();
                                        }
                                        Toast.makeText(this, getString(R.string.successful_import), Toast.LENGTH_SHORT).show();
                                        Log.d("MainActivity", "Рецепти успішно імпортовані.");
                                    },
                                    throwable -> Log.e("MainActivity", "Не вдалося імпортувати дані рецепту.")
                            );

                    compositeDisposable.add(disposable);
                } else {
                    Log.e("MainActivity", "Не вдалося імпортувати дані рецепту.");
                }
            } else {
                Log.e("MainActivity", "Дані або URI дорівнюють null.");
            }
        }
    }

    /**
     * Анімація переключення фрагментів.
     */
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

    /**
     * Створює системні колекції.
     */
    private void initialDB() {
        Disposable disposable = utils.ByCollection().getAllWithoutAdditionData()
                .flatMap(collections -> {
                    if (collections.isEmpty()) {
                        return Observable.fromIterable(utils.ByCollection().getAllNameSystemCollection())
                                .flatMapSingle(name -> {
                                    if (Objects.equals(name, getString(R.string.system_collection_tag) + 5)) {
                                        return utils.ByCollection().add(new Collection(name, CollectionType.BLACK_LIST_ING))
                                                .flatMap(id -> Single.just(id > 0));
                                    } else {
                                        return utils.ByCollection().add(new Collection(name, CollectionType.COLLECTION))
                                                .flatMap(id -> Single.just(id > 0));
                                    }
                                })
                                .toList()
                                .map(results -> {
                                    for (Boolean result : results) {
                                        if (!result) { return false; }
                                    }
                                    return true;
                                });
                    } else { return Single.just(true); }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    if (status) {
                        Log.d("MainActivity", "Всі колекції успішно додані");
                    } else {
                        Log.e("MainActivity", "Помилка при додаванні колекції");
                    }
                }, throwable -> {
                    Log.e("MainActivity", "Помилка при додаванні колекції", throwable);
                });

        compositeDisposable.add(disposable);
    }
}
