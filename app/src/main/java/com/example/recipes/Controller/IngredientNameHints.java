package com.example.recipes.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.SearchResultsAdapter;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.ViewItem.CustomPopupWindow;

import java.util.ArrayList;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для показу підказок з назвами інгредієнтів.
 * Використовується для відображення підказок при введенні тексту в EditText.
 */
public class IngredientNameHints {
    private final Context context;
    private final RecyclerView recyclerView;
    private CustomPopupWindow customPopupWindow;
    private final PreferencesController preferencesController;
    private Runnable runnable;
    private String name;
    private SearchController<String> searchController;
    private View anchorView;
    private final ArrayList<String> allNameIngredients;
    boolean isTouch = false;

    public IngredientNameHints(@NonNull Context context, @NonNull ArrayList<String> allNameIngredients) {
        this.context = context;
        this.allNameIngredients = allNameIngredients;
        this.preferencesController = PreferencesController.getInstance();
        preferencesController.loadPreferences();

        this.recyclerView = new RecyclerView(context);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                AnotherUtils.dpToPx(200, context)
        );
        this.recyclerView.setLayoutParams(layoutParams);

        setSearchController();
    }

    /**
     * Метод для налаштування контролера пошуку.
     * Використовується для ініціалізації контролера пошуку з адаптером результатів.
     */
    private void setSearchController() {
        this.searchController = new SearchController<>(context, null, recyclerView, new SearchResultsAdapter<String>(null, (view, item) -> {
            if (runnable != null) {
                name = item.toString(); // Отримуємо назву інгредієнта
                runnable.run();
            }
        }));
        this.searchController.setArrayData(allNameIngredients);
    }

    /**
     * Метод для встановлення якоря для спливаючого підказки.
     *
     * @param anchorView View, до якого буде прив'язано вікно спливаючої підказки
     */
    public void setAnchorView(View anchorView) {
        this.anchorView = anchorView;
    }

    /**
     * Метод для налаштування вікна спливаючого підказки.
     * Використовується для ініціалізації вікна спливаючої підказки з EditText.
     *
     * @param editText EditText, до якого буде прив'язано вікно спливаючої підказки
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setPopupWindow(EditText editText) {
        if (preferencesController.getStatusIngHints()) {
            if (anchorView != null) {
                // Додаємо обробник подій для натискання на якорь
                anchorView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        isTouch = true;

                        if (!editText.getText().toString().isEmpty()) {
                            showPopup();
                        }
                    }
                    return false;
                });

                // Додаємо обробник подій для втрати фокусу
                anchorView.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) isTouch = false;
                });
            }

            // Додаємо обробник подій для натискання на елемент списку підказок
            runnable = () -> {
                editText.setText(name); // Встановлюємо вибрану назву інгредієнта в EditText
                editText.setSelection(editText.getText().length()); // Встановлюємо курсор в кінець тексту
                hidePopup();
            };

            // Ініціалізуємо вікно спливаючої підказки
            customPopupWindow = new CustomPopupWindow(context, anchorView, recyclerView);
            customPopupWindow.setSize(250, 150);
            customPopupWindow.setOnDismissListener(() -> searchController.clear());
        }
    }

    /**
     * Метод для пошуку по списку назв інгредієнтів.
     * Використовується для фільтрації списку інгредієнтів за введеним текстом.
     *
     * @param text Текст, за яким буде виконано пошук
     */
    public void search(String text) {
        if (preferencesController.getStatusIngHints()) {
            ArrayList<String> result = searchController.searchAndGetResult(text);
            if (result != null && !result.isEmpty() && isTouch) {
                showPopup();
            }
        }
    }

    /**
     * Метод для показу спливаючого підказки.
     */
    private void showPopup() {
        if (customPopupWindow != null && !customPopupWindow.isShowing()) {
            customPopupWindow.showPopup(preferencesController.getStatusIngHints() && !allNameIngredients.isEmpty());
            searchController.search();
        }
    }

    /**
     * Метод для приховування спливаючого підказки.
     */
    private void hidePopup() {
        if (customPopupWindow != null) {
            customPopupWindow.hidePopup();
        }
    }
}
