package com.example.recipes.AI;

import android.content.Context;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Enum.ChatGPTRole;
import com.example.recipes.Item.Dish;

import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для перекладу рецептів за допомогою ChatGPT.
 * Використовує ChatGPTClient для взаємодії з API ChatGPT.
 */
public class Translator {
    private ChatGPTClient client;
    private PreferencesController preferencesController;

    public Translator(Context context) {
        client = new ChatGPTClient(context, ChatGPTRole.TRANSLATOR);
        preferencesController = PreferencesController.getInstance();
    }

    /**
     * Ініціалізація клієнта ChatGPT.
     *
     * @return Single<Boolean>, який повертає true, якщо ініціалізація успішна, або false, якщо ні.
     */
    public Single<Boolean> initialization() {
        return client.initialization();
    }

    /**
     * Перекладає рецепт страви на вказану мову.
     *
     * @param dish Об'єкт Dish, який потрібно перекласти.
     * @return Single<Dish>, який містить перекладений об'єкт Dish або порожній об'єкт у разі помилки.
     */
    public Single<Dish> translateRecipe(Dish dish) {
        // Форматуємо дані страви для надсилання до ChatGPT
        String dataRecipes = client.getFormatStringForGPT(dish);

        if (!dataRecipes.isEmpty()) {
            // Надсилаємо запит на переклад до ChatGPT
            return client.sendMessage("Lang: " + preferencesController.getLanguageString() + "; Data: " + dataRecipes)
                    .flatMap(data -> {
                        if (!data.isEmpty()) return Single.just(client.parsedAnswerGPT(data)); // Парсимо відповідь та повертаємо перекладений об'єкт Dish
                        else return Single.just(new Dish("")); // Повертаємо порожній об'єкт Dish у разі порожньої відповіді
                    });
        } else return Single.just(new Dish(""));
    }

    public ChatGPTClient getClient() {
        return client;
    }
}
