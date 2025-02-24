package com.example.recipes.Controller;

import android.content.Context;

import com.example.recipes.AI.ChatGPT;
import com.example.recipes.AI.ChatGPTClient;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Single;

public class TranslateController {
    private ChatGPTClient client;
    private PreferencesController preferencesController;

    public TranslateController (Context context) {
        client = new ChatGPTClient(context, ChatGPT.TRANSLATOR);
        preferencesController = new PreferencesController();
        preferencesController.loadPreferences(context);
    }

    public Single<Boolean> initialization() {
        return client.initialization();
    }

    public Single<Dish> translateRecipe(Dish dish) {
        String dataRecipes = client.getFormatStringForGPT(dish);

        if (!dataRecipes.isEmpty()) {
            return client.sendMessage("Lang: " + preferencesController.getLanguage() + "; Data: " + dataRecipes)
                    .flatMap(data -> {
                        if (!data.isEmpty()) return Single.just(client.parsedAnswerGPT(data));
                        else return Single.just(new Dish(""));
                    });
        } else return Single.just(new Dish(""));
    }
}
