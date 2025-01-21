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

import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.Activity.EditorDishActivity;
import com.example.recipes.Activity.ReadDataDishActivity;
import com.example.recipes.Adapter.AddChooseObjectsAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchDishFragment extends Fragment implements OnBackPressedListener {
    private EditText searchEditText;
    private ArrayList<Object> dishes;
    private ArrayList<Object> ingredients;
    private RecyclerView searchResultsRecyclerView;
    private AddChooseObjectsAdapter addChooseObjectsAdapter;
    private Button filtersButton, sortButton;
    private TextView head_textView;
    private ConstraintLayout mainLayout, mainArea;
    private GPTFragment childFragment;
    private FrameLayout gptFragment;
    private RecipeUtils utils;
    private CompositeDisposable compositeDisposable;
    private ImageView add_dish_button;
    private ArrayList<Boolean> sortStatus;
    private SearchController searchControllerFilters;
    private boolean flagOpenGPTContainer = false, flagOpenSearch = false, flagUseAnimSearch = true;

    private SearchController searchController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        preferences.edit().remove("scroll_position").apply();
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(getContext());
        utils = new RecipeUtils(getContext());
        sortStatus = new ArrayList<>();
        sortStatus.add(true);
        sortStatus.add(null);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_page, container, false);
        loadItemsActivity(view);
        loadClickListeners();

        if (savedInstanceState == null) {
            try {
                childFragment = new GPTFragment();
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.fragmen_GPT_container, childFragment)
                        .commit();
                Log.d("SearchDishFragment", "Фрагмент GPT успішно вставлено.");
            } catch (Exception e) {
                Log.e("SearchDishFragment", "Не вдалося вставити фрагмент GPT.");
            }
        }

        return view;
    }

    @Override
    public boolean onBackPressed() {
        if (flagOpenGPTContainer) {
            flagOpenGPTContainer = false;
            childFragment.closeGPTContainer();
            return false;
        } else if (flagOpenSearch) {
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
    public void onResume(){
        super.onResume();
        restoreFocus();
        updateRecipesData();
    }

    @Override
    public void onPause() {
        super.onPause();
        LinearLayoutManager layoutManager = (LinearLayoutManager) searchResultsRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            int position = layoutManager.findFirstVisibleItemPosition();
            getActivity().getPreferences(Context.MODE_PRIVATE).edit().putInt("scroll_position", position).apply();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        compositeDisposable.clear();
    }

    private void loadItemsActivity(View view){
        mainLayout = view.findViewById(R.id.mainLayout);
        ConstraintLayout linearLayout = view.findViewById(R.id.search_LinearLayout);
        gptFragment = view.findViewById(R.id.fragmen_GPT_container);

        searchEditText = linearLayout.findViewById(R.id.search_edit_text_my_dish);

        head_textView = view.findViewById(R.id.head_textView);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView_my_dish);
        sortButton = view.findViewById(R.id.sort_search_button);
        filtersButton = view.findViewById(R.id.filters_search_button);

        add_dish_button = mainLayout.findViewById(R.id.add_dish);
        Log.d("SearchDishFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners(){
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), searchEditText, Config.CHAR_LIMIT_NAME_DISH);

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (flagUseAnimSearch) {
                    hideHeadText();
                    showRecyclerView();
                    showSort();
                    showFilters();
                    flagOpenSearch = true;
                }
            }
        });

        mainLayout.setOnTouchListener((v, event) -> {
            int[] location = new int[2];
            gptFragment.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            int w = gptFragment.getWidth();
            int h = gptFragment.getHeight();

            if (event.getRawX() >= x && event.getRawX() <= (x + w) &&
                    event.getRawY() >= y && event.getRawY() <= (y + h)) {

                if (!flagOpenGPTContainer) {
                    flagOpenGPTContainer = true;
                    childFragment.openGPTContainer();
                }
            } else {
                if (flagOpenGPTContainer){
                    flagOpenGPTContainer = false;
                    childFragment.closeGPTContainer();
                }
            }

            if (searchEditText.hasFocus()) {
                searchEditText.clearFocus();
                showHeadText();
                hideRecyclerView();
                hideSort();
                hideFilters();
                hideKeyboard(v);
                flagOpenSearch = false;
            }

            return false;
        });

        View.OnClickListener imageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.add_dish) {
                    onAddDishClick();
                }
            }
        };

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSortClick();
            }
        });

        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFiltersClick();
            }
        });

        add_dish_button.setOnClickListener(imageClickListener);

        Log.d("SearchDishFragment", "Слухачі фрагмента успішно завантажені.");
    }

    private void restoreFocus() {
        if (flagOpenSearch) {
            flagUseAnimSearch = false;
            searchEditText.setFocusable(true);
            searchEditText.requestFocus();
            flagUseAnimSearch = true;
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Log.d("SearchDishFragment", "Клавіатура схована.");
    }

    private void showSort() {
        if (sortButton != null) {
            sortButton.setAlpha(0f);
            sortButton.setVisibility(View.VISIBLE);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(sortButton, "alpha", 0f, 1f);
            alphaAnimator.setDuration(300);

            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(sortButton, "translationY", -50f, 0f);
            translationAnimator.setDuration(300);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
            Log.d("SearchDishFragment", "Кнопка сортування з'явилася.");
        }
    }

    private void showFilters() {
        if (filtersButton != null) {
            filtersButton.setAlpha(0f);
            filtersButton.setVisibility(View.VISIBLE);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(filtersButton, "alpha", 0f, 1f);
            alphaAnimator.setDuration(300);

            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(filtersButton, "translationY", -50f, 0f);
            translationAnimator.setDuration(300);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
            Log.d("SearchDishFragment", "Кнопка фільтрів з'явилася.");
        }
    }

    private void showHeadText() {
        if (head_textView != null) {
            head_textView.setAlpha(0f);
            head_textView.setVisibility(View.VISIBLE);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(head_textView, "alpha", 0f, 1f);
            alphaAnimator.setDuration(300);

            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(head_textView, "translationY", -50f, 0f);
            translationAnimator.setDuration(300);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.start();
            Log.d("SearchDishFragment", "Головний текст з'явилася.");
        }
    }

    private void showRecyclerView() {
        searchResultsRecyclerView.setAlpha(0f);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
                .start();
        Log.d("SearchDishFragment", "Список страв/інгредієнтів з'явився.");
    }

    private void hideHeadText() {
        if (head_textView != null) {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(head_textView, "alpha", 1f, 0f);
            alphaAnimator.setDuration(300);

            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(head_textView, "translationY", 0f, -50f);
            translationAnimator.setDuration(300);

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

    private void hideSort() {
        if (sortButton != null) {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(sortButton, "alpha", 1f, 0f);
            alphaAnimator.setDuration(300);

            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(sortButton, "translationY", 0f, -50f);
            translationAnimator.setDuration(300);

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

    private void hideFilters() {
        if (filtersButton != null) {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(filtersButton, "alpha", 1f, 0f);
            alphaAnimator.setDuration(300);

            ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(filtersButton, "translationY", 0f, -50f);
            translationAnimator.setDuration(300);

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

    private void hideRecyclerView() {
        if (searchResultsRecyclerView != null) {
            searchResultsRecyclerView.animate()
                    .alpha(0f)
                    .setDuration(300)
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

    private void updateResults(SearchController controller, ArrayList<Object> dishes) {
        if (controller != null) {
            controller.getSearchResults().clear();
            controller.getSearchResults().addAll(dishes);
        }
    }

    private void updateRecipesData() {
        Disposable disposable = utils.getFilteredAndSortedDishes(ingredients, sortStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(array_dishes -> {
                    dishes = new ArrayList<>(array_dishes) ;
                    searchController = new SearchController(getContext(), dishes, searchEditText, searchResultsRecyclerView, (view, item) -> {
                        Dish dish = (Dish) item;
                        Intent intent = new Intent(view.getContext(), ReadDataDishActivity.class);
                        intent.putExtra(Config.KEY_DISH, dish.getId());
                        view.getContext().startActivity(intent);
                    });
                    updateResults(searchController, new ArrayList<>(dishes));

                    LinearLayoutManager layoutManager = (LinearLayoutManager) searchResultsRecyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int position = getActivity().getPreferences(Context.MODE_PRIVATE).getInt("scroll_position", 0);
                        layoutManager.scrollToPositionWithOffset(position, 0);
                    }
                });

        compositeDisposable.add(disposable);
    }

    private void onAddDishClick() {
        Intent intent = new Intent(getContext(), EditorDishActivity.class);
        intent.putExtra(Config.KEY_DISH, -2L);
        startActivity(intent);
    }

    private void onSortClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sort_search_result, null);
        LinearLayout sort_alphabetBox = dialogView.findViewById(R.id.sort_alphabetBox);
        LinearLayout sort_TimeBox = dialogView.findViewById(R.id.sort_TimeBox);

        if (sort_alphabetBox != null && sort_TimeBox != null) {
            ArrayList<RadioGroup> radioGroups = new ArrayList<>();
            radioGroups.add(sort_alphabetBox.findViewById(R.id.sort_alphabet_radioButtonGroup));
            radioGroups.add(sort_TimeBox.findViewById(R.id.sort_time_radioButtonGroup));

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

    private void onFiltersClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filters, null);
        TextView textView = dialogView.findViewById(R.id.textView22);
        ConstraintLayout constraintLayout = dialogView.findViewById(R.id.search_filters_LinearLayout);
        RecyclerView filtersRecyclerView = dialogView.findViewById(R.id.items_filters_check_RecyclerView);

        if (textView != null) { textView.setText(R.string.your_ingredients); }
        if (constraintLayout != null) {
            EditText editText = constraintLayout.findViewById(R.id.search_edit_text_filters);

            if (editText != null) {
                CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, 30);

                Disposable disposable = utils.getNameIngredientsUnique()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                names -> {
                                    if (names != null && !names.isEmpty()){
                                        searchControllerFilters = new SearchController(getContext(), new ArrayList<>(names), editText, filtersRecyclerView, (checkBox, selectedItem, item) -> {
                                            if (!checkBox.isChecked()) {
                                                selectedItem.add(item);
                                                checkBox.setChecked(true);
                                            } else {
                                                selectedItem.remove(item);
                                                checkBox.setChecked(false);
                                            }
                                        });
                                        updateResults(searchControllerFilters, new ArrayList<>(names));

                                        builder.setView(dialogView)
                                                .setPositiveButton(R.string.apply, (dialog, which) -> {
                                                    addChooseObjectsAdapter = (AddChooseObjectsAdapter) searchControllerFilters.getAdapter();
                                                    ingredients = addChooseObjectsAdapter.getSelectedItem();
                                                    updateRecipesData();
                                                })
                                                .setNeutralButton(R.string.reset, (dialog, which) -> {
                                                    resetFilters();
                                                })
                                                .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss());

                                        builder.create().show();
                                    } else {
                                        Toast.makeText(getContext(), getString(R.string.empty_names_ingredient), Toast.LENGTH_SHORT).show();
                                    }
                                },
                                throwable -> {
                                    Log.d("SearchDishFragment", "Помилка отримання унікальних назв інгредіентів");
                                }
                        );
                compositeDisposable.add(disposable);
            }
        }
    }

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

    private void resetSorting() {
        sortStatus.set(0, true);
        sortStatus.set(1, null);
        updateRecipesData();
    }

    private void resetFilters() {
        ingredients = new ArrayList<>();
        if (addChooseObjectsAdapter != null) { addChooseObjectsAdapter.resetSelection(); }
        updateRecipesData();
    }

    private ArrayList<ArrayList<RadioButton>> getRadioButtons(ArrayList<RadioGroup> radioGroups) {
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
