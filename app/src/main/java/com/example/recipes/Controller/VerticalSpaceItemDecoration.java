package com.example.recipes.Controller;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для додавання вертикального відступу між елементами RecyclerView.
 * Використовується для створення простору між елементами списку.
 */
public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int verticalSpaceHeight;

    /**
     * Конструктор класу VerticalSpaceItemDecoration.
     *
     * @param verticalSpaceHeight Висота вертикального відступу між елементами (у пікселях).
     */
    public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    /**
     * Встановлює відступи для елементів RecyclerView.
     *
     * @param outRect Прямокутник, який визначає відступи для елемента.
     * @param view    Поточний елемент RecyclerView.
     * @param parent  RecyclerView, до якого додається відступ.
     * @param state   Стан RecyclerView.
     */
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = verticalSpaceHeight;
        }
    }
}
