package com.example.recipes.Activity;

import android.app.Application;

import com.example.recipes.AI.Translator;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Utils.RecipeUtils;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PreferencesController.getInstance().initialization(this);
        RecipeUtils.getInstance(this);
        Translator.getInstance(this).initialization();
    }
}
