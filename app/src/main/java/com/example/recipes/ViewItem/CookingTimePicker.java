package com.example.recipes.ViewItem;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CookingTimePicker {
    private AppCompatActivity activity;
    private MaterialTimePicker cookingTimePicker;

    public CookingTimePicker(AppCompatActivity activity) {
        this.activity = activity;
        setCookingTimePicker(0, null);
    }

    @SuppressLint("DefaultLocale")
    public void setCookingTimePicker(long data, Consumer<Long> callbackTimeSet) {
        if (cookingTimePicker != null) cookingTimePicker.clearOnPositiveButtonClickListeners();

        int hours = getHours(data);
        int minutes = getMinutes(data);

        cookingTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(hours)
                .setMinute(minutes)
                .setTitleText(activity.getString(R.string.title_cooking_time_picker))
                .build();

        cookingTimePicker.addOnPositiveButtonClickListener(v -> {
            // Виклик функції після встановлення часу
            if (callbackTimeSet != null) {
                callbackTimeSet.accept((cookingTimePicker.getHour() * 60L + cookingTimePicker.getMinute()) * 60L * 1000L);
            }
        });
    }

    /**
     * Вставляє обробник кліку на вказаний View, який відкриває CookingTimePicker.
     * @param view View, на який буде встановлено обробник кліку.
     * @param onClickListener - функція, яка буде викликана при кліку на View.
     */
    public void setViewOnClickListener(View view, Runnable onClickListener) {
        if (cookingTimePicker != null && view != null) {
            view.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) view.callOnClick();
            });

            view.setOnClickListener(v -> {
                if (onClickListener != null) onClickListener.run();
                show();
            });
        }
    }

    public void show() {
        if (cookingTimePicker != null) cookingTimePicker.show(activity.getSupportFragmentManager(), "cookingTimePicker");
    }

    public int getHours(long data) {
        if (data > 0) return (int) TimeUnit.MILLISECONDS.toHours(data);
        return 0;
    }

    public int getMinutes(long data) {
        if (data > 0) return (int) TimeUnit.MILLISECONDS.toMinutes(data) % 60;
        return 0;
    }
}
