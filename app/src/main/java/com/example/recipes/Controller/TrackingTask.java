package com.example.recipes.Controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для виконання періодичних завдань з використанням Handler.
 * Дозволяє запускати та зупиняти періодичне виконання завдання з заданим інтервалом.
 */
public class TrackingTask {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long interval;
    private Runnable task;

    /**
     * Конструктор класу TrackingTask.
     *
     * @param interval Інтервал між виконаннями завдання (у мілісекундах).
     */
    public TrackingTask(long interval) {
        this.interval = interval;
    }

    /**
     * Запускає періодичне виконання завдання.
     *
     * @param onCheck Завдання, яке буде виконуватися періодично.
     */
    public void startTracking(Runnable onCheck) {
        task = new Runnable() {
            @Override
            public void run() {
                try {
                    onCheck.run(); // Виконання завдання
                } catch (Exception e) {
                    stopTracking(); // Зупинка завдання у разі помилки
                    Log.e("TrackingTask", "Error in tracking task: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    handler.postDelayed(this, interval); // Планування повторного виконання через інтервал
                }
            }
        };
        handler.post(task); // Початковий запуск завдання
    }

    /**
     * Зупиняє періодичне виконання завдання.
     */
    public void stopTracking() {
        if (task != null) {
            handler.removeCallbacks(task);
        }
    }
}
