package com.example.recipes.Decoration;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для управління анімацією завантажувального екрану.
 * Забезпечує відображення тексту з анімацією під час завантаження та його приховання після завершення.
 */
public class LoadScreenAnimation {
    private ConstraintLayout loadScreen;
    private TextView loadText;
    private TextLoadAnimation loadAnimation;
    private Activity activity;

    /**
     * Конструктор класу LoadScreenAnimation.
     *
     * @param activity Поточна активність, де буде відображатися завантажувальний екран
     * @param loadScreen ConstraintLayout, який використовується як завантажувальний екран
     */
    public LoadScreenAnimation(Activity activity, ConstraintLayout loadScreen) {
        this.activity = activity;
        this.loadScreen = loadScreen;
    }

    /**
     * Запускає анімацію завантажувального екрану.
     * Додає текст з повідомленням про завантаження та запускає анімацію тексту.
     */
    public void startLoadingScreenAnimation() {
        if (loadScreen != null) {
            loadScreen.setVisibility(View.VISIBLE);
            loadScreen.setClickable(true);
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();

            // Створення та налаштування TextView для відображення тексту завантаження
            loadText = new TextView(activity);
            loadText.setText(R.string.loading);
            loadText.setTextSize(28);
            loadText.setTextColor(AnotherUtils.getAttrColor(activity, R.attr.colorText));
            loadText.setPadding(20, 20, 20, 20);
            loadText.setGravity(Gravity.CENTER);

            // Налаштування параметрів розміщення тексту
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.CENTER;

            rootView.addView(loadText, params);
            loadAnimation = new TextLoadAnimation(loadText, activity.getString(R.string.loading));
            loadAnimation.startAnimation();
        }
    }

    /**
     * Зупиняє анімацію завантажувального екрану.
     * Видаляє текст завантаження та приховує завантажувальний екран.
     */
    public void stopLoadingScreenAnimation() {
        if (loadScreen != null && loadText != null) {
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();
            rootView.removeView(loadText);
            loadAnimation.stopAnimation();
            loadText = null;
            loadScreen.setVisibility(View.GONE);
            loadScreen.setClickable(false);
            loadScreen.setFocusable(false);
            loadScreen.clearFocus();
        }
    }
}
