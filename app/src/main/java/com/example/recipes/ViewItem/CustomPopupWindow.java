package com.example.recipes.ViewItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class CustomPopupWindow extends ConstraintLayout {
    private PopupWindow popupWindow;
    private View anchorView;
    private Context context;
    private RecyclerView recyclerView;

    public CustomPopupWindow(Context context, View anchorView, RecyclerView recyclerView) {
        super(context);
        this.context = context;
        this.anchorView = anchorView;
        this.recyclerView = recyclerView;
        init(context, 150, 70);
    }

    /**
     * Встановлює розмір спливаючого вікна.
     * @param height Висота вікна в dp
     * @param width Ширина вікна в dp
     */
    public void setSize(int height, int width) {
        init(context, height, width);
    }

    /**
     * Ініціалізує UI елементи списку.
     * @param context Контекст додатку
     * @param height Висота спливаючого вікна в dp
     * @param width Ширина спливаючого вікна в dp
     */
    private void init(Context context, int height, int width) {
        popupWindow = new PopupWindow(recyclerView, AnotherUtils.dpToPx(height, context), AnotherUtils.dpToPx(width, context), true);
        popupWindow.setBackgroundDrawable(AnotherUtils.getDrawable(context, R.drawable.border_day_night_with_background));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);
    }

    /**
     * Метод для показу спливаючого списку.
     * Використовується для відображення спливаючого вікна з даними.
     */
    public void showPopup() {
        showPopup(true);
    }

    /**
     * Метод для показу спливаючого списку.
     * @param additionalCondition Додаткова умова для показу
     */
    public void showPopup(boolean additionalCondition) {
        if (anchorView != null && !popupWindow.isShowing() && additionalCondition) {
            popupWindow.showAsDropDown(anchorView);
        } else { hidePopup(); }
    }

    /**
     * Метод для приховування спливаючого списку.
     */
    public void hidePopup() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    /**
     * Встановлює слухача для події закриття спливаючого вікна.
     * @param listener Слухач для події закриття
     */
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        if (popupWindow != null) popupWindow.setOnDismissListener(listener);
    }

    /**
     * Перевіряє, чи спливаюче вікно показано.
     * @return true, якщо вікно показано, інакше false
     */
    public boolean isShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }
}
