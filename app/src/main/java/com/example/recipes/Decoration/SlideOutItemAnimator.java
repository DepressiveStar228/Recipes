package com.example.recipes.Decoration;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class SlideOutItemAnimator extends RecyclerView.ItemAnimator {

    @Override
    public boolean animateDisappearance(@NonNull RecyclerView.ViewHolder viewHolder,
                                        @NonNull ItemHolderInfo preLayoutInfo,
                                        @Nullable ItemHolderInfo postLayoutInfo) {
        final View itemView = viewHolder.itemView;

        itemView.animate()
                .translationX(itemView.getWidth()) // Элемент "выезжает" вправо
                .alpha(0f) // Элемент становится прозрачным
                .setDuration(300) // Длительность анимации
                .withEndAction(() -> {
                    itemView.setVisibility(View.GONE); // Элемент исчезает
                })
                .start();

        return true; // Возвращаем true, чтобы анимация была применена
    }

    @Override
    public boolean animateAppearance(@NonNull RecyclerView.ViewHolder viewHolder,
                                     @Nullable ItemHolderInfo preLayoutInfo,
                                     @NonNull ItemHolderInfo postLayoutInfo) {
        return false; // Нет анимации для появления элементов
    }

    @Override
    public boolean animatePersistence(@NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull ItemHolderInfo preLayoutInfo,
                                      @NonNull ItemHolderInfo postLayoutInfo) {
        return false; // Нет анимации для сохранения элементов
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
                                 @NonNull RecyclerView.ViewHolder newHolder,
                                 @NonNull ItemHolderInfo preLayoutInfo,
                                 @NonNull ItemHolderInfo postLayoutInfo) {
        return false; // Нет анимации для изменения
    }

    @Override
    public void runPendingAnimations() {
        // Пока нет ожидания анимаций
    }

    @Override
    public void endAnimation(@NonNull RecyclerView.ViewHolder item) {
        // Завершаем анимацию
    }

    @Override
    public void endAnimations() {
         // Завершаем все анимации
    }

    @Override
    public boolean isRunning() {
        return false; // Возвращаем false, чтобы анимации не были запущены повторно
    }
}

