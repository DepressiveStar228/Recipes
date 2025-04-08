package com.example.recipes.Controller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для виявлення поштовхів пристрою за допомогою акселерометра.
 * Відповідає за реєстрацію події "поштовх" та виклик відповідного слухача.
 */
public class ShakeDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 6.7F; // Порогове значення сили поштовху (у g)
    private static final int SHAKE_TIME_MS = 500; // Мінімальний інтервал між поштовхами (у мілісекундах)

    private long lastShakeTime = 0; // Час останнього поштовху
    private OnShakeListener listener; // Слухач події поштовху

    public interface OnShakeListener {
        void onShake(); // Метод, який викликається при виявленні поштовху
    }

    /**
     * Конструктор класу ShakeDetector.
     *
     * @param listener Слухач події поштовху.
     */
    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    /**
     * Реєструє ShakeDetector для прослуховування подій акселерометра.
     *
     * @param context Контекст додатку.
     */
    public void register(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Скасовує реєстрацію ShakeDetector для прослуховування подій акселерометра.
     *
     * @param context Контекст додатку.
     */
    public void unregister(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    /**
     * Викликається при зміні даних датчика.
     *
     * @param event Подія датчика.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Обчислення загальної сили прискорення (у g)
            float gForce = (float) Math.sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH;

            // Перевірка, чи сила прискорення перевищує порогове значення
            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                long now = System.currentTimeMillis();

                // Перевірка, чи минуло достатньо часу з моменту останнього поштовху
                if ((now - lastShakeTime) > SHAKE_TIME_MS) {
                    lastShakeTime = now;
                    if (listener != null) {
                        listener.onShake(); // Виклик слухача події поштовху
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
