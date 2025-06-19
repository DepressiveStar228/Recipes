package com.example.recipes.AI;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Enum.ChatGPTRole;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.google.gson.Gson;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для перекладу рецептів за допомогою ChatGPT.
 * Використовує ChatGPTClient для взаємодії з API ChatGPT.
 */
public class Translator {
    private static Translator instance;
    private final ChatGPTClient client;
    private final PreferencesController preferencesController;
    private boolean isInitialized = false;

    public Translator(Context context) {
        client = new ChatGPTClient(context, ChatGPTRole.TRANSLATOR);
        preferencesController = PreferencesController.getInstance();
    }

    /**
     * Повертає єдиний екземпляр класу (Singleton).
     *
     * @param context Контекст додатку
     * @return Єдиний екземпляр Translator
     */
    public static synchronized Translator getInstance(Context context) {
        if (instance == null) {
            instance = new Translator(context);
        }
        return instance;
    }

    /**
     * Ініціалізація клієнта ChatGPT.
     */
    @SuppressLint("CheckResult")
    public void initialization() {
        Single.fromCallable(client::initialization)
                .flatMap(status -> status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> isInitialized = status);
    }

    /**
     * Перекладає рецепт страви на вказану мову.
     *
     * @param dish Об'єкт Dish, який потрібно перекласти.
     * @return Single<Dish>, який містить перекладений об'єкт Dish або порожній об'єкт у разі помилки.
     */
    public Single<Dish> translateRecipe(Dish dish) {
        if (!dish.getName().isEmpty()) {
            // Надсилаємо запит на переклад до ChatGPT
            return client.sendMessage("Translate to " + preferencesController.getLanguageString() + "; Data: " + new Gson().toJson(dish))
                    .flatMap(data -> {
                        if (!data.isEmpty()) {
                            Dish translatedDish = new Gson().fromJson(data, Dish.class); // Парсимо відповідь

                            // Відновлюємо типи інгредієнтів та рецептів
                            if (!translatedDish.getName().isEmpty()) {
                                if (translatedDish.getIngredients().size() == dish.getIngredients().size()) {
                                    for (int i = 0; i < translatedDish.getIngredients().size(); i++) {
                                        Ingredient ingredient = translatedDish.getIngredients().get(i);
                                        Ingredient originalIngredient = dish.getIngredients().get(i);
                                        ingredient.setType(originalIngredient.getType());
                                    }
                                }

                                if (translatedDish.getRecipes().size() == dish.getRecipes().size()) {
                                    for (int i = 0; i < translatedDish.getRecipes().size(); i++) {
                                        DishRecipe recipe = translatedDish.getRecipes().get(i);
                                        DishRecipe originalRecipe = dish.getRecipes().get(i);
                                        recipe.setTypeData(originalRecipe.getTypeData());
                                    }
                                }
                            }

                            return Single.just(translatedDish);
                        }
                        else return Single.just(new Dish("")); // Повертаємо порожній об'єкт Dish у разі порожньої відповіді
                    });
        } else return Single.just(new Dish(""));
    }

    public ChatGPTClient getClient() {
        return client;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
