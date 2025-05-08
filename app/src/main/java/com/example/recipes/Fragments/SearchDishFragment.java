package com.example.recipes.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.Activity.EditorDishActivity;
import com.example.recipes.Activity.GPTActivity;
import com.example.recipes.Adapter.SearchResultsAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.Dialogues;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Фрагмент для пошуку страв у додатку.
 */
public class SearchDishFragment extends Fragment implements OnBackPressedListener {
    private EditText searchEditText;
    private ArrayList<String> filterIngredients;
    private RecyclerView searchResultsRecyclerView;
    private Button filtersButton, sortButton;
    private TextView head_textView;
    private ConstraintLayout mainLayout, gptFragment;
    private RecipeUtils utils;
    private CompositeDisposable compositeDisposable;
    private ImageView add_dish_button;
    private ArrayList<Boolean> sortStatus;
    private final int durationAnimation = 200;
    private AtomicBoolean flagAccessAnimation = new AtomicBoolean(true);
    private boolean flagOpenSearch = false;

    private SearchController<Dish> searchControllerForDish;
    private Dialogues dialogues;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        preferences.edit().remove("scroll_position").apply();
        utils = RecipeUtils.getInstance(getContext());
        dialogues = new Dialogues(getActivity());
        sortStatus = new ArrayList<>();
        filterIngredients = new ArrayList<>();
        sortStatus.add(true);
        sortStatus.add(null);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_page, container, false);
        loadItemsActivity(view);
        loadClickListeners();
        return view;
    }

    @Override
    public boolean onBackPressed() {
        if (flagOpenSearch && searchEditText != null) {
            // Закриття режиму пошуку
            searchEditText.clearFocus();
            hideRecyclerView();
            hideSort();
            hideFilters();
            showHeadText();
            flagOpenSearch = false;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreFocus();
        updateRecipesData();

        // Відновлення позиції скролу
        int position = getActivity().getPreferences(Context.MODE_PRIVATE).getInt("scroll_position", 0);
        if (position >= 0 && searchResultsRecyclerView != null) {
            searchResultsRecyclerView.post(() -> searchResultsRecyclerView.smoothScrollToPosition(position));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (searchResultsRecyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) searchResultsRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                // Збереження позиції скролу
                int position = layoutManager.findFirstVisibleItemPosition();
                getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt("scroll_position", position).apply();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    /**
     * Ініціалізація UI елементів
     * @param view кореневий View фрагмента
     */
    private void loadItemsActivity(View view) {
        mainLayout = view.findViewById(R.id.mainLayout);
        ConstraintLayout linearLayout = view.findViewById(R.id.search_LinearLayout);
        gptFragment = view.findViewById(R.id.gpt_container);
        if (gptFragment != null) gptFragment.setOnClickListener(view1 -> onGPTClick());

        searchEditText = linearLayout.findViewById(R.id.search_edit_text_my_dish);

        head_textView = view.findViewById(R.id.head_textView);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView_my_dish);
        if (searchResultsRecyclerView != null) {
            setSearchController();
        }

        sortButton = view.findViewById(R.id.sort_search_button);
        filtersButton = view.findViewById(R.id.filters_search_button);

        add_dish_button = mainLayout.findViewById(R.id.add_dish);

        Log.d("SearchDishFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    /**
     * Налаштування обробників подій
     */
    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners() {
        // Обмеження довжини тексту пошуку
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), searchEditText, Limits.MAX_CHAR_NAME_DISH);

        // Обробник фокусу поля пошуку
        if (searchEditText != null) {
            searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (flagAccessAnimation.get()) {
                        flagAccessAnimation.set(false);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            flagAccessAnimation.set(true);
                        }, durationAnimation);

                        hideHeadText();
                        showRecyclerView();
                        showSort();
                        showFilters();
                        flagOpenSearch = true;
                    }
                }
            });
        }

        // Обробник дотиків до основного макету
        if (mainLayout != null) {
            mainLayout.setOnTouchListener((v, event) -> {
                int[] location = new int[2];

                if (gptFragment != null) {
                    gptFragment.getLocationOnScreen(location);

                    if (searchEditText != null && searchEditText.hasFocus()) {
                        if (flagAccessAnimation.get()) {
                            flagAccessAnimation.set(false);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                flagAccessAnimation.set(true);
                            }, durationAnimation);

                            searchEditText.clearFocus();
                            showHeadText();
                            hideRecyclerView();
                            hideSort();
                            hideFilters();
                            hideKeyboard(v);
                            flagOpenSearch = false;
                        }
                    }
                }

                return false;
            });
        }

        // Обробники кнопок
        if (sortButton != null) { sortButton.setOnClickListener(v -> onSortClick()); }
        if (filtersButton != null) { filtersButton.setOnClickListener(v -> onFiltersClick()); }
        if (add_dish_button != null) {
            add_dish_button.setOnClickListener(v -> {
                if (v.getId() == R.id.add_dish) {
                    Intent intent = new Intent(getContext(), EditorDishActivity.class);
                    intent.putExtra(IntentKeys.DISH_ID.name(), -1);
                    startActivity(intent);
                }
            });
        }
        Log.d("SearchDishFragment", "Слухачі фрагмента успішно завантажені.");
    }

    /**
     * Відновлює фокус на полі пошуку, якщо фрагмент був у режимі пошуку
     */
    private void restoreFocus() {
        if (flagOpenSearch) {
            flagAccessAnimation.set(false);
            searchEditText.setFocusable(true);
            searchEditText.requestFocus();
            flagAccessAnimation.set(true);
        }
    }

    /**
     * Ініціалізує контролер пошуку для страв
     */
    private void setSearchController() {
        if (searchControllerForDish == null) {
            searchControllerForDish = new SearchController(getContext(), searchEditText, searchResultsRecyclerView, new SearchResultsAdapter<Dish>((view, item) -> {
                Dish dish = (Dish) item;
                Intent intent = new Intent(getContext(), EditorDishActivity.class);
                intent.putExtra(IntentKeys.DISH_ID.name(), dish.getId());
                startActivity(intent); // Відкриття редактора страви
            }));
        }
    }

    /**
     * Оновлює дані рецептів з урахуванням поточних фільтрів та сортування
     */
    private void updateRecipesData() {
        utils.ByDish().getViewModel().getAll().observe(this, data -> {
            if (data != null && searchControllerForDish != null) {
                // Отримання відфільтрованих та відсортованих даних
                Disposable disposable = utils.ByDish().getFilteredAndSorted(filterIngredients, sortStatus)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(array_dishes -> {
                            if (searchControllerForDish != null) {
                                searchControllerForDish.setArrayData(new ArrayList<>(array_dishes));
                                searchControllerForDish.search(); // Оновлення результатів пошуку
                            }
                        });

                compositeDisposable.add(disposable);
            }
        });
    }

    /**
     * Обробляє клік на кнопку сортування, показує діалог сортування
     */
    private void onSortClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sort_search_result, null);

        // Отримання елементів сортування
        LinearLayout sortAlphabetBox = dialogView.findViewById(R.id.sort_alphabetBox);
        LinearLayout sortTimeBox = dialogView.findViewById(R.id.sort_TimeBox);

        if (sortAlphabetBox != null && sortTimeBox != null) {
            // Налаштування радіо-кнопок сортування
            ArrayList<RadioGroup> radioGroups = new ArrayList<>();
            radioGroups.add(sortAlphabetBox.findViewById(R.id.sort_alphabet_radioButtonGroup));
            radioGroups.add(sortTimeBox.findViewById(R.id.sort_time_radioButtonGroup));

            ArrayList<ArrayList<RadioButton>> arrayRadioButtons = getRadioButtons(radioGroups);

            ArrayList<Pair<RadioGroup, ArrayList<RadioButton>>> dataRadioGroup = new ArrayList<>();
            for (int i = 0; i < radioGroups.size(); i++) {
                final int index = i;

                dataRadioGroup.add(new Pair<>(radioGroups.get(i), arrayRadioButtons.get(i)));

                radioGroups.get(i).setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId == arrayRadioButtons.get(index).get(0).getId()) {
                        sortStatus.set(index, true);
                    } else if (checkedId == arrayRadioButtons.get(index).get(1).getId()) {
                        sortStatus.set(index, false);
                    }
                });
            }

            checkRadioButton(dataRadioGroup);

            builder.setView(dialogView)
                    .setPositiveButton(R.string.apply, (dialog, which) -> {
                        updateRecipesData();
                    })
                    .setNeutralButton(R.string.reset, (dialog, which) -> {
                        resetSorting();
                    })
                    .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
    }

    /**
     * Обробляє клік на кнопку фільтрів, показує діалог фільтрації
     */
    private void onFiltersClick() {
        Disposable disposable = utils.ByIngredient().getNamesUnique()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        names -> {
                            if (names != null) {
                                if (dialogues != null) {
                                    dialogues.dialogChooseItemsWithSearch(new ArrayList<>(names), filterIngredients, selectedItems -> {
                                        filterIngredients.clear();
                                        filterIngredients.addAll(selectedItems);
                                        updateRecipesData();
                                    }, () -> {
                                        filterIngredients.clear();
                                        updateRecipesData();
                                    }, Limits.MAX_CHAR_NAME_INGREDIENT, R.string.your_ingredients, R.string.apply);
                                }
                            } else {
                                Toast.makeText(getContext(), getString(R.string.empty_names_ingredient), Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> Log.d("SearchDishFragment", "Помилка отримання унікальних назв інгредіентів")
                );
        compositeDisposable.add(disposable);
    }

    /**
     * Обробляє клік на GPT-контейнер
     */
    private void onGPTClick() {
        Intent intent = new Intent(getContext(), GPTActivity.class);
        startActivity(intent); // Відкриття GPT-активності
    }

    /**
     * Встановлює стан радіо-кнопок відповідно до поточного стану сортування
     * @param dataRadioGroup список пар (RadioGroup, RadioButton) для налаштування
     */
    private void checkRadioButton(ArrayList<Pair<RadioGroup, ArrayList<RadioButton>>> dataRadioGroup) {
        int i = 0;

        for (Pair<RadioGroup, ArrayList<RadioButton>> pair : dataRadioGroup) {
            if (sortStatus.get(i) != null && sortStatus.get(i)) {
                pair.first.check(pair.second.get(0).getId());
            } else if (sortStatus.get(i) != null && !sortStatus.get(i)) {
                pair.first.check(pair.second.get(1).getId());
            }

            i++;
        }
    }

    /**
     * Приховує клавіатуру
     * @param view View, яке має фокус введення
     */
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Log.d("SearchDishFragment", "Клавіатура схована.");
    }

    /**
     * Показує кнопку сортування з анімацією
     */
    private void showSort() {
        if (sortButton != null) {
            sortButton.setAlpha(0f);
            sortButton.setVisibility(View.VISIBLE);

            // Анімація появи
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(sortButton, "alpha", 0f, 1f);
            alphaAnimator.setDuration(durationAnimation);

            // Анімація руху зверху вниз
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(sortButton, "translationY", -50f, 0f);
            translationAnimator.setDuration(durationAnimation);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
            Log.d("SearchDishFragment", "Кнопка сортування з'явилася.");
        }
    }

    /**
     * Показує кнопку фільтрів з анімацією
     */
    private void showFilters() {
        if (filtersButton != null) {
            filtersButton.setAlpha(0f);
            filtersButton.setVisibility(View.VISIBLE);

            // Анімація появи
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(filtersButton, "alpha", 0f, 1f);
            alphaAnimator.setDuration(durationAnimation);

            // Анімація руху зверху вниз
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(filtersButton, "translationY", -50f, 0f);
            translationAnimator.setDuration(durationAnimation);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
            Log.d("SearchDishFragment", "Кнопка фільтрів з'явилася.");
        }
    }

    /**
     * Показує головний текст фрагмента з анімацією
     */
    private void showHeadText() {
        if (head_textView != null) {
            head_textView.setAlpha(0f);
            head_textView.setVisibility(View.VISIBLE);

            // Анімація появи
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(head_textView, "alpha", 0f, 1f);
            alphaAnimator.setDuration(durationAnimation);

            // Анімація руху зверху вниз
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(head_textView, "translationY", -50f, 0f);
            translationAnimator.setDuration(durationAnimation);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
            Log.d("SearchDishFragment", "Головний текст з'явилася.");
        }
    }

    /**
     * Показує список результатів пошуку з анімацією
     */
    private void showRecyclerView() {
        searchResultsRecyclerView.setAlpha(0f);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.animate()
                .alpha(1f)
                .setDuration(durationAnimation)
                .setListener(null)
                .start();
        Log.d("SearchDishFragment", "Список страв/інгредієнтів з'явився.");
    }

    /**
     * Приховує заголовок з анімацією
     */
    private void hideHeadText() {
        if (head_textView != null) {
            // Анімація зникнення
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(head_textView, "alpha", 1f, 0f);
            alphaAnimator.setDuration(durationAnimation);

            // Анімація руху знизу вверх
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(head_textView, "translationY", 0f, -50f);
            translationAnimator.setDuration(durationAnimation);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    head_textView.setVisibility(View.INVISIBLE);
                }
            });
            animatorSet.start();

            Log.d("SearchDishFragment", "Головний текст захований.");
        }
    }

    /**
     * Приховує кнопку сортування з анімацією
     */
    private void hideSort() {
        if (sortButton != null) {
            // Анімація зникнення
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(sortButton, "alpha", 1f, 0f);
            alphaAnimator.setDuration(durationAnimation);

            // Анімація руху знизу вверх
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(sortButton, "translationY", 0f, -50f);
            translationAnimator.setDuration(durationAnimation);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sortButton.setVisibility(View.INVISIBLE);
                }
            });
            animatorSet.start();

            Log.d("SearchDishFragment", "Кнопка сортування захована.");
        }
    }

    /**
     * Приховує кнопку фільтрів з анімацією
     */
    private void hideFilters() {
        if (filtersButton != null) {
            // Анімація зникнення
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(filtersButton, "alpha", 1f, 0f);
            alphaAnimator.setDuration(durationAnimation);

            // Анімація руху знизу вверх
            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(filtersButton, "translationY", 0f, -50f);
            translationAnimator.setDuration(durationAnimation);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    filtersButton.setVisibility(View.INVISIBLE);
                }
            });
            animatorSet.start();

            Log.d("SearchDishFragment", "Кнопка фільтрів захований.");
        }
    }

    /**
     * Приховує список результатів пошуку з анімацією
     */
    private void hideRecyclerView() {
        if (searchResultsRecyclerView != null) {
            searchResultsRecyclerView.animate()
                    .alpha(0f)
                    .setDuration(durationAnimation)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            searchResultsRecyclerView.setVisibility(View.GONE);
                        }
                    })
                    .start();

            Log.d("SearchDishFragment", "Список страв/інгредієнтів захований.");
        }
    }

    /**
     * Скидає налаштування сортування до значень за замовчуванням
     */
    private void resetSorting() {
        sortStatus.set(0, true); // Сортування за алфавітом (за зростанням)
        sortStatus.set(1, null); // Без сортування за часом
        updateRecipesData();
    }

    /**
     * Створює та налаштовує радіо-кнопки для діалогу сортування
     * @param radioGroups група для додавання кнопок
     * @return список створених радіо-кнопок
     */
    private ArrayList<ArrayList<RadioButton>> getRadioButtons(ArrayList<RadioGroup> radioGroups) {
        // Отримання кольору тексту з поточної теми
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorText, typedValue, true);
        int colorText = typedValue.data;

        ArrayList<ArrayList<RadioButton>> arrayRadioButtons = new ArrayList<>();
        arrayRadioButtons.add(new ArrayList<>());
        arrayRadioButtons.add(new ArrayList<>());

        RadioButton ascendingButton = new RadioButton(getContext());
        ascendingButton.setText(R.string.ascending_alphabetical_alphabet);
        ascendingButton.setId(View.generateViewId());
        ascendingButton.setTextSize(20);
        ascendingButton.setTextColor(colorText);
        arrayRadioButtons.get(0).add(ascendingButton);

        RadioButton descendingButton = new RadioButton(getContext());
        descendingButton.setText(R.string.descending_alphabetical_alphabet);
        descendingButton.setId(View.generateViewId());
        descendingButton.setTextSize(20);
        descendingButton.setTextColor(colorText);
        arrayRadioButtons.get(0).add(descendingButton);

        RadioButton orderedByNewButton = new RadioButton(getContext());
        orderedByNewButton.setText(R.string.ordered_new);
        orderedByNewButton.setId(View.generateViewId());
        orderedByNewButton.setTextSize(20);
        orderedByNewButton.setTextColor(colorText);
        arrayRadioButtons.get(1).add(orderedByNewButton);

        RadioButton orderedByOldButton = new RadioButton(getContext());
        orderedByOldButton.setText(R.string.ordered_old);
        orderedByOldButton.setId(View.generateViewId());
        orderedByOldButton.setTextSize(20);
        orderedByOldButton.setTextColor(colorText);
        arrayRadioButtons.get(1).add(orderedByOldButton);

        radioGroups.get(0).addView(ascendingButton);
        radioGroups.get(0).addView(descendingButton);
        radioGroups.get(1).addView(orderedByNewButton);
        radioGroups.get(1).addView(orderedByOldButton);

        return arrayRadioButtons;
    }
}