package com.example.recipes.Interface;

import android.net.Uri;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Callback-інтерфейс для обробки результатів операції експорту даних.
 * Використовується для повернення результатів асинхронних операцій експорту.
 */
public interface ExportCallbackUri {
    /**
     * Викликається при успішному завершенні операції експорту.
     * @param uri URI експортованого файлу. Містить посилання на створений файл з даними.
     *            Може бути null, якщо експорт не вдався через помилки файлової системи.
     */
    void onSuccess(Uri uri);

    /**
     * Викликається при виникненні помилки під час експорту.
     * @param throwable Об'єкт помилки, що містить інформацію про причину невдачі.
     *                  Може містити деталі помилки IO, проблеми з серіалізацією даних тощо.
     */
    void onError(Throwable throwable);

    /**
     * Надає Disposable об'єкт для керування життєвим циклом асинхронної операції.
     * @param disposable Об'єкт Disposable, який дозволяє скасувати операцію експорту.
     */
    void getDisposable(Disposable disposable);
}
