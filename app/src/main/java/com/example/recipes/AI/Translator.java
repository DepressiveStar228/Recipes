package com.example.recipes.AI;

import android.content.Context;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Enum.ChatGPTRole;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
        if (!dish.getName().isEmpty()) {
            // Надсилаємо запит на переклад до ChatGPT
            return client.sendMessage("Lang: " + preferencesController.getLanguageString() + "; Data: " + new Gson().toJson(dish))
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
}
