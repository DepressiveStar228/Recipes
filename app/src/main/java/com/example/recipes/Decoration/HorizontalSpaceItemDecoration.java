package com.example.recipes.Decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Додає горизонтальні відступи між елементами RecyclerView, крім останнього.
 * Використовується для візуального розділення елементів у горизонтальних списках.
 */
public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int horizontalSpace;

    /**
     * Конструктор, що приймає розмір відступу.
     *
     * @param horizontalSpace розмір відступу між елементами у пікселях
     */
    public HorizontalSpaceItemDecoration(int horizontalSpace) {
        this.horizontalSpace = horizontalSpace;
    }

    /**
     * Встановлює відступи для елемента RecyclerView.
     *
     * @param outRect прямокутник, що містить відступи
     * @param view поточний елемент списку
     * @param parent RecyclerView, до якого додано декоратор
     * @param state стан RecyclerView
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        // Не додаємо відступ для останнього елемента
        if (position != parent.getAdapter().getItemCount() - 1) {
            outRect.right = horizontalSpace;
        }
    }
}
