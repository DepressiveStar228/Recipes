package com.example.recipes.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Утилітарний клас з різноманітними допоміжними методами.
 */
public class AnotherUtils {
    /**
     * Встановлює видимість контейнера для стану "пусто" в залежності від наявності даних.
     *
     * @param emptyLayout Контейнер для відображення стану "пусто"
     * @param isEmpty Прапорець, що вказує на відсутність даних
     */
    public static void visibilityEmptyStatus(ConstraintLayout emptyLayout, Boolean isEmpty) {
        if (emptyLayout != null) {
            if (!isEmpty) { emptyLayout.setVisibility(View.GONE); }
            else { emptyLayout.setVisibility(View.VISIBLE); }
        }
    }

    /**
     * Отримує колір з атрибутів теми.
     *
     * @param context Контекст додатка
     * @param attr ID атрибуту кольору
     * @return Колір у форматі int
     */
    @SuppressLint({"ResourceType", "ObjectAnimatorBinding"})
    public static int getAttrColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    /**
     * Отримує Drawable з ресурсів.
     *
     * @param context Контекст додатка
     * @param attr ID ресурсу Drawable
     * @return Об'єкт Drawable або null, якщо не знайдено
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawable(Context context, int attr) {
        return context.getDrawable(attr);
    }

    /**
     * Отримує масив рядків для конкретної локалізації.
     *
     * @param context Контекст додатка
     * @param resId ID ресурсу масиву рядків
     * @param locale Потрібна локаль
     * @return Масив рядків для вказаної локалі
     */
    public static String[] getStringArrayForLocale(Context context, int resId, Locale locale) {
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        Resources localizedResources = context.createConfigurationContext(config).getResources();
        return localizedResources.getStringArray(resId);
    }

    /**
     * Перевіряє наявність інтернет-з'єднання.
     *
     * @param context Контекст додатка
     * @return true, якщо є підключення до інтернету, false - в іншому випадку
     */
    public static boolean checkInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Метод для конвертації dp у px.
     *
     * @param dp Значення в dp.
     * @return Значення в px.
     */
    public static int dpToPx(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
