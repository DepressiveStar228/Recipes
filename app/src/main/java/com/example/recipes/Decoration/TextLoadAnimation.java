package com.example.recipes.Decoration;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для створення анімації завантаження у вигляді тексту з крапками, що змінюються.
 * Дозволяє додавати додаткові дії при старті та зупинці анімації.
 */
public class TextLoadAnimation {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView textView;
    private String baseText = "";
    private Runnable animation, start, stop;
    private final ArrayList<Runnable> startRunnable = new ArrayList<>();
    private final ArrayList<Runnable> stopRunnable = new ArrayList<>();
    private int dotCount = 0;

    /**
     * Конструктор без прив'язки до TextView.
     *
     * @param baseText Базовий текст для анімації
     */
    public TextLoadAnimation(String baseText) {
        this(null, baseText);
    }

    /**
     * Основний конструктор класу.
     *
     * @param textView TextView, в якому буде відображатись анімація
     * @param baseText Базовий текст для анімації
     */
    public TextLoadAnimation(TextView textView, String baseText) {
        this.textView = textView;
        this.baseText = baseText;
    }

    /**
     * Встановлює базовий текст для анімації.
     *
     * @param baseText Новий базовий текст
     */
    public void setBaseText(String baseText) {
        this.baseText = baseText;
    }

    /**
     * Встановлює базовий текст і відображає його у TextView.
     *
     * @param baseText Новий базовий текст
     */
    public void setBaseTextIntoTextView(String baseText) {
        this.baseText = baseText;
        if (textView != null) {
            textView.setText(baseText);
        }
    }

    /**
     * Встановлює TextView для відображення анімації.
     *
     * @param textView Об'єкт TextView
     */
    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    /**
     * Додає дію, яка виконується при старті анімації.
     *
     * @param runnable Дія для виконання
     */
    public void addStartRunnable(Runnable runnable) {
        startRunnable.add(runnable);
    }

    /**
     * Додає дію, яка виконується при зупинці анімації.
     *
     * @param runnable Дія для виконання
     */
    public void addStopRunnable(Runnable runnable) {
        stopRunnable.add(runnable);
    }

    /**
     * Очищає список дій при старті анімації.
     */
    public void clearStartRunnable() {
        startRunnable.clear();
    }

    /**
     * Очищає список дій при зупинці анімації.
     */
    public void clearStopRunnable() {
        stopRunnable.clear();
    }

    /**
     * Очищає всі списки дій.
     */
    public void clearAllRunnable() {
        startRunnable.clear();
        stopRunnable.clear();
    }

    /**
     * Виконує всі дії при старті анімації.
     */
    private void runStartRunnable() {
        for (Runnable runnable : startRunnable) runnable.run();
    }

    /**
     * Виконує всі дії при зупинці анімації.
     */
    private void runStopRunnable() {
        for (Runnable runnable : stopRunnable) runnable.run();
    }

    /**
     * Запускає анімацію завантаження.
     */
    public void startAnimation() {
        setInitialRunnable();
        dotCount = 0;
        if (start != null) start.run();
    }

    /**
     * Зупиняє анімацію завантаження.
     */
    public void stopAnimation() {
        if (stop != null) stop.run();
    }

    /**
     * Ініціалізує основні Runnable для анімації.
     */
    private void setInitialRunnable() {
        animation = new Runnable() {
            @Override
            public void run() {
                if (textView != null) {
                    dotCount = (dotCount + 1) % 4; // Циклічна зміна кількості крапок (0-3)
                    String dots = new String(new char[dotCount]).replace("\0", ".");
                    textView.setText(baseText + dots);
                    handler.postDelayed(this, 150); // Оновлення кожні 150 мс
                }
            }
        };

        start = () -> {
            if (textView != null) {
                textView.setVisibility(View.VISIBLE);
                runStartRunnable();
                handler.post(animation); // Запуск анімації
            }
        };

        stop = () -> {
            runStopRunnable();
            handler.removeCallbacks(animation); // Зупинка анімації
            if (textView != null) {
                textView.setText(baseText);
                textView.setVisibility(View.INVISIBLE);
            }
        };
    }
}
