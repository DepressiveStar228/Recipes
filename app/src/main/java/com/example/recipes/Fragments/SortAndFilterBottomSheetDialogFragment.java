package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.ChooseItemAdapter;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Database.DishSortAndFilters;
import com.example.recipes.Decoration.AnimationUtils;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.ViewItem.CookingTimePicker;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SortAndFilterBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private Activity activity;
    private Runnable onApplyFilters;
    private View rootView;
    private ChipGroup sortByAlphabet, sortByCreationTime;
    private TextView filterBoxByIngredientsTextView;
    private RecyclerView searchIngredientRecyclerView;
    private ConstraintLayout emptyIngredient, searchIngredientContainer, resultSearchIngredientContainer, searchIngredientLayout, filterBoxByIngredients;
    private NestedScrollView sortFilterScrollView;
    private EditText searchIngredientEditText, startCookingTimeEditText, endCookingTimeEditText;
    private RangeSlider rangeCookingTimeSlider;
    private AppCompatImageView sortIngredient, clearInputTextForIngredient, closeFragment, openFilterBoxByIngredients, useOrNotIngredients;
    private AppCompatButton applyButton, resetButton;
    private SearchController<String> searchControllerForIngredient;
    private ArrayList<String> selectedUsedIngredients = new ArrayList<>();
    private ArrayList<String> selectedSkipIngredients = new ArrayList<>();
    private CookingTimePicker cookingTimePicker;
    private DishSortAndFilters dishSortAndFilters;
    private String nameFragment = "SortAndFilterBottomSheetDialogFragment";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final AtomicBoolean flagAccessOpenFilterBoxByIngredients = new AtomicBoolean(true);
    private final AtomicBoolean isOpenFilterBoxByIngredients = new AtomicBoolean(false);
    private final AtomicBoolean isUseIngredient = new AtomicBoolean(true);
    private final AtomicLong currentMinCookingTime = new AtomicLong(-1);
    private final AtomicLong currentMaxCookingTime = new AtomicLong(-1);
    private final AtomicLong minPossibleCookingTime = new AtomicLong(-1);
    private final AtomicLong maxPossibleCookingTime = new AtomicLong(-1);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.sort_and_filter_for_search_dish, container, false);
        initializeViews();
        setListeners();
        setSearchController();
        setCookingTimeSlider();
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.activity = (Activity) context;
            this.cookingTimePicker = new CookingTimePicker((AppCompatActivity) activity);
        }
    }

    @Override
    public void onDestroyView() {
        compositeDisposable.clear();
        if (searchControllerForIngredient != null) searchControllerForIngredient.clear();
        super.onDestroyView();
    }

    public static SortAndFilterBottomSheetDialogFragment newInstance(Runnable onApplyFilters) {
        SortAndFilterBottomSheetDialogFragment fragment = new SortAndFilterBottomSheetDialogFragment();
        fragment.setOnApplyFilters(onApplyFilters);
        return fragment;
    }

    private void setOnApplyFilters(Runnable onApplyFilters) {
        this.onApplyFilters = onApplyFilters;
    }

    @SuppressLint("SetTextI18n")
    private void initializeViews() {
        if (activity == null) return;

        if (rootView != null) {
            sortFilterScrollView = rootView.findViewById(R.id.sort_filter_fragment);
            filterBoxByIngredients = rootView.findViewById(R.id.filterBoxByIngredients);
            closeFragment = rootView.findViewById(R.id.close);
            sortByAlphabet = rootView.findViewById(R.id.sortByAlphabeticalOrderChipGroup);
            sortByCreationTime = rootView.findViewById(R.id.sortByCreationTimeChipGroup);
            searchIngredientContainer = rootView.findViewById(R.id.searchContainer);
            resultSearchIngredientContainer = rootView.findViewById(R.id.resultSearchContainer);
            searchIngredientRecyclerView = rootView.findViewById(R.id.ingredientRecyclerView);
            emptyIngredient = rootView.findViewById(R.id.emptyIngredient);
            openFilterBoxByIngredients = rootView.findViewById(R.id.openFilterBoxByIngredients);
            rangeCookingTimeSlider = rootView.findViewById(R.id.rangeCookingTimeSlider);
            startCookingTimeEditText = rootView.findViewById(R.id.startCookingTimeEditText);
            endCookingTimeEditText = rootView.findViewById(R.id.endCookingTimeEditText);
            filterBoxByIngredientsTextView = rootView.findViewById(R.id.filterBoxByIngredientsTextView);


            if (searchIngredientContainer != null) {
                searchIngredientLayout = searchIngredientContainer.findViewById(R.id.searchIngredientField);
                useOrNotIngredients = searchIngredientContainer.findViewById(R.id.useOrNotIngredients);

                changeModeFilterIngredients(!isUseIngredient.get());

                if (searchIngredientLayout != null) {
                    searchIngredientEditText = searchIngredientLayout.findViewById(R.id.searchEditText);
                    clearInputTextForIngredient = searchIngredientLayout.findViewById(R.id.clearInputTextButton);
                    sortIngredient = searchIngredientLayout.findViewById(R.id.sortButton);
                }
            }


            ConstraintLayout buttonsLayout = rootView.findViewById(R.id.buttonContainer);
            if (buttonsLayout != null) {
                applyButton = buttonsLayout.findViewById(R.id.yesButton);
                resetButton = buttonsLayout.findViewById(R.id.neutralButton);
            }
        }
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void setListeners() {
        if (closeFragment != null) {
            closeFragment.setOnClickListener(v -> dismiss());
            Log.d(nameFragment, "Close button listener set");
        }

        if (openFilterBoxByIngredients != null && searchIngredientContainer != null && resultSearchIngredientContainer != null
                && sortFilterScrollView != null && filterBoxByIngredients != null) {

            openFilterBoxByIngredients.setOnClickListener(v -> {
                if (flagAccessOpenFilterBoxByIngredients.get()) {
                    flagAccessOpenFilterBoxByIngredients.set(false);

                    if (resultSearchIngredientContainer.getVisibility() == View.VISIBLE) {
                        AnimationUtils.smoothRotation(openFilterBoxByIngredients, AnimationUtils.LEFT, 180, 200, () -> flagAccessOpenFilterBoxByIngredients.set(true));
                        AnimationUtils.smoothHeightChange(filterBoxByIngredients, filterBoxByIngredients.getHeight(), 110, 250, () -> changeOpenFilterBoxByIngredients(false));
                    } else {
                        AnimationUtils.smoothRotation(openFilterBoxByIngredients, AnimationUtils.RIGHT, 180, 200, () -> flagAccessOpenFilterBoxByIngredients.set(true));
                        AnimationUtils.smoothHeightChange(filterBoxByIngredients, filterBoxByIngredients.getHeight(), 1000, 250, () -> changeOpenFilterBoxByIngredients(true));
                    }
                }
            });

            if (isOpenFilterBoxByIngredients.get()) {
                // Якщо останній раз фільтр був відкритий, викликаємо клік для відображення
                openFilterBoxByIngredients.post(() -> openFilterBoxByIngredients.callOnClick());
            }
        }

        if (useOrNotIngredients != null && filterBoxByIngredientsTextView != null) {
            useOrNotIngredients.setOnClickListener(v -> {
                changeModeFilterIngredients(isUseIngredient.get());

                if (searchControllerForIngredient != null) {
                    ChooseItemAdapter<String> adapterChooseObjects = (ChooseItemAdapter) searchControllerForIngredient.getAdapter();
                    ArrayList<String> selectedItems = adapterChooseObjects.getSelectedItem();

                    if (isUseIngredient.get()) {
                        isUseIngredient.set(false);

                        selectedUsedIngredients.clear();
                        selectedUsedIngredients.addAll(selectedItems);
                        adapterChooseObjects.setSelectedItems(selectedSkipIngredients);
                    } else {
                        isUseIngredient.set(true);

                        selectedSkipIngredients.clear();
                        selectedSkipIngredients.addAll(selectedItems);
                        adapterChooseObjects.setSelectedItems(selectedUsedIngredients);
                    }
                }

                Log.d(nameFragment, "Filter ingredients mode changed: " + (isUseIngredient.get() ? "Necessary" : "Exclusion"));
            });
        }

        if (rangeCookingTimeSlider != null) {
            rangeCookingTimeSlider.addOnChangeListener((slider, value, fromUser) -> {
                if (fromUser) {
                    long min = slider.getValues().get(0).longValue();
                    long max = slider.getValues().get(1).longValue();

                    currentMinCookingTime.set(min);
                    currentMaxCookingTime.set(max);
                    startCookingTimeEditText.setText(getCookingTimeFromLongData(min));
                    endCookingTimeEditText.setText(getCookingTimeFromLongData(max));
                    Log.d(nameFragment, "Cooking time range updated: " + min + " - " + max);
                }
            });
        }

        if (startCookingTimeEditText != null && cookingTimePicker != null) {
            cookingTimePicker.setViewOnClickListener(startCookingTimeEditText, () -> {
                cookingTimePicker.setCookingTimePicker(currentMinCookingTime.get(), time -> {
                    if (time > currentMaxCookingTime.get() || time > maxPossibleCookingTime.get()) {
                        startCookingTimeEditText.setText(getCookingTimeFromLongData(currentMaxCookingTime.get()));
                        currentMinCookingTime.set(currentMaxCookingTime.get());
                        rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                        Toast.makeText(activity, R.string.error_min_value_greater_than_max, Toast.LENGTH_SHORT).show();
                        Log.e(nameFragment, "Error: min value greater than max value");
                    } else if (time < minPossibleCookingTime.get()) {
                        endCookingTimeEditText.setText(getCookingTimeFromLongData(minPossibleCookingTime.get()));
                        currentMaxCookingTime.set(minPossibleCookingTime.get());
                        rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                        Toast.makeText(activity, R.string.error_min_value_less_than_possible, Toast.LENGTH_SHORT).show();
                        Log.e(nameFragment, "Error: min value less than possible value");
                    } else {
                        startCookingTimeEditText.setText(getCookingTimeFromLongData(time));
                        currentMinCookingTime.set(time);
                        rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                        Log.d(nameFragment, "Cooking time updated: " + getCookingTimeFromLongData(time));
                    }
                });
            });
        }

        if (endCookingTimeEditText != null && cookingTimePicker != null) {
            cookingTimePicker.setViewOnClickListener(endCookingTimeEditText, () -> {
                cookingTimePicker.setCookingTimePicker(currentMaxCookingTime.get(), time -> {
                    if (time < currentMinCookingTime.get()) {
                        endCookingTimeEditText.setText(getCookingTimeFromLongData(currentMinCookingTime.get()));
                        currentMaxCookingTime.set(currentMinCookingTime.get());
                        rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                        Toast.makeText(activity, R.string.error_max_value_less_than_min, Toast.LENGTH_SHORT).show();
                        Log.e(nameFragment, "Error: max value less than min value");
                    } else if (time > maxPossibleCookingTime.get()) {
                        endCookingTimeEditText.setText(getCookingTimeFromLongData(maxPossibleCookingTime.get()));
                        currentMaxCookingTime.set(maxPossibleCookingTime.get());
                        rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                        Toast.makeText(activity, R.string.error_max_value_greater_than_possible, Toast.LENGTH_SHORT).show();
                        Log.e(nameFragment, "Error: max value greater than possible value");
                    } else {
                        endCookingTimeEditText.setText(getCookingTimeFromLongData(time));
                        currentMaxCookingTime.set(time);
                        rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                        Log.d(nameFragment, "Cooking time updated: " + getCookingTimeFromLongData(time));
                    }
                });
            });
        }

        if (applyButton != null) {
            applyButton.setOnClickListener(v -> {
                if (onApplyFilters != null) {
                    onApplyFilters.run();
                    Log.d(nameFragment, "Filters applied successfully");
                }
                dismiss();
            });
        }

        if (resetButton != null) {
            resetButton.setOnClickListener(v -> {
                sortByAlphabet.clearCheck();
                sortByCreationTime.clearCheck();

                if (searchControllerForIngredient != null) {
                    searchControllerForIngredient.setArraySelectedData(new ArrayList<>());
                    searchControllerForIngredient.getAdapter().notifyDataSetChanged();

                    selectedUsedIngredients.clear();
                    selectedSkipIngredients.clear();
                    Log.d(nameFragment, "Filters reset: ingredients cleared");
                }

                if (searchIngredientEditText != null) {
                    searchIngredientEditText.setText("");
                    Log.d(nameFragment, "Filters reset: search ingredient cleared");
                }

                currentMinCookingTime.set(minPossibleCookingTime.get());
                currentMaxCookingTime.set(maxPossibleCookingTime.get());

                if (startCookingTimeEditText != null && endCookingTimeEditText != null) {
                    startCookingTimeEditText.setText(getCookingTimeFromLongData(currentMinCookingTime.get()));
                    endCookingTimeEditText.setText(getCookingTimeFromLongData(currentMaxCookingTime.get()));
                    Log.d(nameFragment, "Filters reset: cooking time set to default values");
                }

                if (rangeCookingTimeSlider != null) {
                    rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                    Log.d(nameFragment, "Filters reset: cooking time slider set to default values");
                }

                Toast.makeText(activity, R.string.successful_reset, Toast.LENGTH_SHORT).show();
                Log.d(nameFragment, "Filters reset to default values");
            });
        }
    }

    /**
     * Встановлює контролер пошуку для інгредієнтів.
     * Використовує SearchController для управління пошуком інгредієнтів.
     * Підписується на зміни в базі даних та оновлює дані в контролері.
     */
    private void setSearchController() {
        if (searchControllerForIngredient == null) {
            searchControllerForIngredient = new SearchController<>(activity, searchIngredientEditText, searchIngredientRecyclerView, new ChooseItemAdapter<String>(activity, emptyIngredient, (checkBox, item) -> { }));
            Log.d(nameFragment, "SearchController for ingredients initialized");

            RecipeUtils.getInstance(activity).ByIngredient().getViewModel().getNamesUnique().observe((LifecycleOwner) activity, data -> {
                if (data != null) {
                    searchControllerForIngredient.setArrayData(new ArrayList<>(data));
                    searchControllerForIngredient.search();
                    Log.d(nameFragment, "SearchController for ingredients data updated: " + data.size() + " items");
                }
            });
        }

        searchControllerForIngredient.setSearchEditText(searchIngredientEditText); // Додаємо EditText для пошуку інгредієнтів
        searchControllerForIngredient.setSearchRecyclerView(searchIngredientRecyclerView); // Додаємо RecyclerView для відображення результатів пошуку
        searchControllerForIngredient.setEmptyIndicator(emptyIngredient); // Додаємо індикатор порожнього списку
        searchControllerForIngredient.setSortResultSearchButton(sortIngredient); // Додаємо кнопку сортування
        searchControllerForIngredient.setClearSearchEditText(clearInputTextForIngredient); // Додаємо кнопку очищення поля вводу
        Log.d(nameFragment, "SearchController for ingredients set up with components");
    }

    /**
     * Встановлює діапазон часу приготування для слайдера.
     * Отримує мінімальний та максимальний час приготування з бази даних
     * та встановлює їх у відповідні поля та слайдер.
     */
    private void setCookingTimeSlider() {
        Disposable disposable = Single.zip(
                        RecipeUtils.getInstance(activity).ByDish().getMinCookingTime(),
                        RecipeUtils.getInstance(activity).ByDish().getMaxCookingTime(),
                        (minTime, maxTime) -> {
                            List<Long> range = new ArrayList<>();
                            if (minTime != null) range.add(minTime);
                            if (maxTime != null) range.add(maxTime);
                            return range;
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(range -> {
                    if (range != null && range.size() == 2) {
                        if (rangeCookingTimeSlider != null && startCookingTimeEditText != null && endCookingTimeEditText != null) {
                            if (currentMinCookingTime.get() == -1 && currentMaxCookingTime.get() == -1
                                    && minPossibleCookingTime.get() == -1 && maxPossibleCookingTime.get() == -1) {
                                currentMinCookingTime.set(range.get(0));
                                currentMaxCookingTime.set(range.get(1));
                                minPossibleCookingTime.set(range.get(0));
                                maxPossibleCookingTime.set(range.get(1));
                                Log.d(nameFragment, "Cooking time range initialized: " + currentMinCookingTime.get() + " - " + currentMaxCookingTime.get());
                            }

                            startCookingTimeEditText.setText(getCookingTimeFromLongData(currentMinCookingTime.get()));
                            endCookingTimeEditText.setText(getCookingTimeFromLongData(currentMaxCookingTime.get()));

                            rangeCookingTimeSlider.setValueFrom(range.get(0).floatValue());
                            rangeCookingTimeSlider.setValueTo(range.get(1).floatValue());
                            rangeCookingTimeSlider.setStepSize(60000f);
                            rangeCookingTimeSlider.setValues(List.of((float) currentMinCookingTime.get(), (float) currentMaxCookingTime.get()));
                            Log.d(nameFragment, "Cooking time slider set with range: " + range.get(0) + " - " + range.get(1));
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }

    /**
     * Отримує час приготування у форматі "години:хвилини" з мілісекунд.
     * Якщо час менше або дорівнює нулю, повертає "00:00".
     *
     * @param data Час приготування у мілісекундах.
     * @return Форматований рядок часу приготування.
     */
    private String getCookingTimeFromLongData(Long data) {
        if (data > 0) {
            int hours = (int) TimeUnit.MILLISECONDS.toHours(data);
            int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(data) % 60;
            Log.d(nameFragment, "Cooking time formatted: " + hours + " hours, " + minutes + " minutes");
            return String.format("%02d:%02d", hours, minutes);
        }
        else {
            Log.e(nameFragment, "Cooking time is zero or negative, returning default value");
            return "00:00";
        }
    }

    /**
     * Отримує список страв з застосованими фільтрами та сортуванням.
     * @return Single<List<Dish>> Список страв, відфільтрованих та відсортованих відповідно до встановлених параметрів.
     */
    public Single<List<Dish>> getResult() {
        if (dishSortAndFilters == null) {
            dishSortAndFilters = new DishSortAndFilters(RecipeUtils.getInstance(activity).ByDish().getDao());
            Log.d(nameFragment, "DishSortAndFilters initialized");
        }

        Pair<Boolean, Boolean> sortStatus = getSortStatus();
        Pair<Long, Long> cookingTimeRange = getRangeCookingTime();
        Pair<ArrayList<String>, ArrayList<String>> filterIngredients = getFilterIngredients();

        dishSortAndFilters.enableSortByName(sortStatus.first);
        dishSortAndFilters.enableSortByCreationTime(sortStatus.second);
        dishSortAndFilters.setCookingTimeRange(cookingTimeRange);
        dishSortAndFilters.setUsedIngredients(filterIngredients.first);
        dishSortAndFilters.setSkipIngredients(filterIngredients.second);

        Log.d(nameFragment, "Sort status: Name - " + sortStatus.first + ", Creation - " + sortStatus.second
                + "; Cooking time range: " + cookingTimeRange.first + " - " + cookingTimeRange.second
                + "; Used ingredients: " + filterIngredients.first.size() + ", Skip ingredients: " + filterIngredients.second.size());
        return dishSortAndFilters.getResult();
    }

    /**
     * Отримує список вибраних інгредієнтів для фільтрації.
     * Повертає список рядків, які представляють вибрані інгредієнти.
     */
    private Pair<ArrayList<String>, ArrayList<String>> getFilterIngredients() {
        if (searchControllerForIngredient != null) {
            ChooseItemAdapter<String> adapterChooseObjects = (ChooseItemAdapter) searchControllerForIngredient.getAdapter();
            ArrayList<String> selectedItems = adapterChooseObjects.getSelectedItem();

            if (isUseIngredient.get()) {
                selectedUsedIngredients.clear();
                selectedUsedIngredients.addAll(selectedItems);
            } else {
                selectedSkipIngredients.clear();
                selectedSkipIngredients.addAll(selectedItems);
            }

            Log.d(nameFragment, "Selected ingredients for filtering: Used - " + selectedUsedIngredients.size() + ", Skip - " + selectedSkipIngredients.size());
            return new Pair<>(selectedUsedIngredients, selectedSkipIngredients);
        }

        Log.e(nameFragment, "No ingredients selected for filtering");
        return new Pair<>(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Отримує статус сортування.
     * Повертає список з двома значеннями:
     * 1. Сортування за алфавітом (true - від А до Я, false - від Я до А)
     * 2. Сортування за часом створення (true - новіші спочатку, false - старіші спочатку)
     */
    private Pair<Boolean, Boolean> getSortStatus() {
        Boolean sortByName = null;
        Boolean sortByCreation = null;

        if (sortByAlphabet != null) {
            if (sortByAlphabet.getCheckedChipId() == R.id.ordered_top) {
                sortByName = true; // Сортування за алфавітом від А до Я
            } else if (sortByAlphabet.getCheckedChipId() == R.id.ordered_bottom) {
                sortByName = false; // Сортування за алфавітом від Я до А
            }
        }

        if (sortByCreationTime != null) {
            if (sortByCreationTime.getCheckedChipId() == R.id.ordered_new) {
                sortByCreation = true; // Сортування за алфавітом від А до Я
            } else if (sortByCreationTime.getCheckedChipId() == R.id.ordered_old) {
                sortByCreation = false; // Сортування за алфавітом від Я до А
            }
        }

        Log.d(nameFragment, "Sort status: Name - " + sortByName + ", Creation - " + sortByCreation);
        return new Pair<>(sortByName, sortByCreation);
    }

    /**
     * Отримує діапазон часу приготування у мілісекундах.
     * Повертає список з двома значеннями: мінімальний та максимальний час приготування.
     * Якщо діапазон не встановлено, повертає null.
     */
    private Pair<Long, Long> getRangeCookingTime() {
        if (currentMinCookingTime.get() > -1 && currentMaxCookingTime.get() > -1) {
            Log.d(nameFragment, "Current cooking time range: " + currentMinCookingTime.get() + " - " + currentMaxCookingTime.get());
            return new Pair<>(currentMinCookingTime.get(), currentMaxCookingTime.get());
        } else {
            Log.e(nameFragment, "Cooking time range not set, returning default values");
            return new Pair<>(0L, Long.MAX_VALUE); // Діапазон часу приготування не встановлено
        }
    }

    @SuppressLint("SetTextI18n")
    private void changeModeFilterIngredients(boolean flag) {
        if (useOrNotIngredients != null && filterBoxByIngredientsTextView != null) {
            if (flag) {
                useOrNotIngredients.setImageResource(R.drawable.icon_check_box_false);
                filterBoxByIngredientsTextView.setText(getString(R.string.filter_by_ingredients) + " " + getString(R.string.exclusion));
                Log.d(nameFragment, "Filter ingredients mode changed to 'Exclusion'");
            } else {
                useOrNotIngredients.setImageResource(R.drawable.icon_check_box_true);
                filterBoxByIngredientsTextView.setText(getString(R.string.filter_by_ingredients) + " " + getString(R.string.necessary));
                Log.d(nameFragment, "Filter ingredients mode changed to 'Necessary'");
            }
        }
    }

    private void changeOpenFilterBoxByIngredients(boolean flag) {
        if (openFilterBoxByIngredients != null) {
            if (flag) {
                searchIngredientContainer.setVisibility(View.VISIBLE);
                resultSearchIngredientContainer.setVisibility(View.VISIBLE);
                isOpenFilterBoxByIngredients.set(true);
                sortFilterScrollView.setNestedScrollingEnabled(false);
                Log.d(nameFragment, "Filter box by ingredients opened");
            } else {
                searchIngredientContainer.setVisibility(View.GONE);
                resultSearchIngredientContainer.setVisibility(View.GONE);
                AnotherUtils.hideKeyboard(activity, searchIngredientEditText);
                isOpenFilterBoxByIngredients.set(false);
                sortFilterScrollView.setNestedScrollingEnabled(true);
                Log.d(nameFragment, "Filter box by ingredients closed");
            }
        }
    }
}
