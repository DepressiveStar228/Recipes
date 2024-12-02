package com.example.recipes.Controller;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.recipes.Item.Collection;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ImportExportController {
    public static DataBox importRecipeDataToFile(Context context, File file) {
        Gson gson = new Gson();
        DataBox recipeData = null;

        try (FileReader reader = new FileReader(file)) {
            Type recipeDataType = new TypeToken<DataBox>() {}.getType();
            recipeData = gson.fromJson(reader, recipeDataType);
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.error_import), Toast.LENGTH_SHORT).show();
            Log.e("ImportExportController", "Помилка при імпорті рецептів", e);
        }

        return recipeData;
    }

    public static DataBox importRecipeDataFromGPT(Context context, String jsonResponse) {
        Gson gson = new Gson();
        DataBox recipeData = null;

        if (jsonResponse == null || jsonResponse.isEmpty()) {
            return recipeData;
        } else {
            int firstCurlyBraceIndex = jsonResponse.indexOf('{');
            int lastCurlyBraceIndex = jsonResponse.lastIndexOf('}');

            if (firstCurlyBraceIndex != -1 && lastCurlyBraceIndex != -1 && firstCurlyBraceIndex < lastCurlyBraceIndex) {
                String finalJsonResponse = jsonResponse.substring(firstCurlyBraceIndex, lastCurlyBraceIndex + 1);
                String recipe = extractRecipe(finalJsonResponse);

                finalJsonResponse = finalJsonResponse.replaceAll("\n", "")
                        .replaceAll("\\s*\\{\\s*", "{")
                        .replaceAll("\\s*\\}\\s*", "}")
                        .replaceAll("\\s*\\[\\s*", "{")
                        .replaceAll("\\s*\\]\\s*", "}")
                        .replaceAll(":\\s*", ":")
                        .replaceAll(",\\s*\"", ",\"");

                finalJsonResponse = replaceRecipe(finalJsonResponse, recipe.replaceAll("\n", ""), recipe);

                try {
                    Type recipeDataType = new TypeToken<DataBox>() {}.getType();
                    recipeData = gson.fromJson(finalJsonResponse, recipeDataType);
                } catch (Exception e) {
                    Toast.makeText(context, context.getString(R.string.error_import), Toast.LENGTH_SHORT).show();
                    Log.e("ImportExportController", "Помилка при імпорті рецептів з GPT", e);
                }
            }
        }

        return recipeData;
    }

    public static void exportRecipeData(Context context, Collection collection, ExportCallbackUri callback) {
        RecipeUtils utils = new RecipeUtils(context);
        Disposable disposable = utils.getDishesByCollection(collection.getId())
                .flatMap(
                        dishes -> {
                            if (!dishes.isEmpty()) {
                                return Observable.fromIterable(dishes)
                                        .flatMapSingle(dish -> utils.getIngredients(dish.getId())
                                                .map(ingredients -> new Pair<>(dish, ingredients))
                                        )
                                        .toList()
                                        .map(pairs -> {
                                            ArrayList<Ingredient> allIngredients = new ArrayList<>();
                                            for (Pair<Dish, List<Ingredient>> pair : pairs) {
                                                allIngredients.addAll(pair.second);
                                            }
                                            return new Pair<>(dishes, allIngredients);
                                        });
                            } else {
                                return Single.just(new Pair<>(dishes, new ArrayList<Ingredient>()));
                            }
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> {
                            try {
                                DataBox recipeData = new DataBox(new ArrayList<>(pair.first), pair.second);

                                Gson gson = new Gson();
                                String jsonString = gson.toJson(recipeData);
                                File internalDir = context.getFilesDir();
                                File jsonFile = new File(internalDir, collection.getName() + ".json");

                                try (FileOutputStream fos = new FileOutputStream(jsonFile);
                                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
                                    writer.write(jsonString);
                                }

                                Uri fileUri = FileProvider.getUriForFile(context, "com.example.recipes.file-provider", jsonFile);
                                callback.onSuccess(fileUri);

                            } catch (IOException e) {
                                callback.onError(e);
                            }
                        },
                        throwable -> {
                            callback.onError(throwable);
                        }
                );

        callback.getDisposable(disposable);
    }

    public static void exportRecipeData(Context context, DataBox recipeData, ExportCallbackUri callback) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(recipeData);
        File internalDir = context.getFilesDir();
        File jsonFile = new File(internalDir, "my_recipes.json");

        try (FileOutputStream fos = new FileOutputStream(jsonFile);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
            writer.write(jsonString);
            callback.onSuccess(FileProvider.getUriForFile(context, "com.example.recipes.file-provider", jsonFile));
        } catch (IOException e) {
            callback.onError(e);
        }
    }

    public static void exportDish(Context context, Dish dish, ExportCallbackUri callback) {
        RecipeUtils utils = new RecipeUtils(context);
        ArrayList<Dish> dishes = new ArrayList<>();
        dishes.add(dish);

        Disposable disposable = utils.getIngredients(dish.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ingredients -> {
                            Gson gson = new Gson();
                            String jsonString = gson.toJson(new DataBox(dishes, new ArrayList<>(ingredients)));
                            File internalDir = context.getFilesDir();
                            File jsonFile = new File(internalDir, dish.getName() + ".json");

                            try (FileOutputStream fos = new FileOutputStream(jsonFile);
                                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
                                writer.write(jsonString);
                                callback.onSuccess(FileProvider.getUriForFile(context, "com.example.recipes.file-provider", jsonFile));
                            } catch (IOException e) {
                                callback.onError(e);
                            }
                        }
                );

        callback.getDisposable(disposable);
    }

    private static String extractRecipe(String input) {
        String regex = "\"recipe\":(.*?)\\}\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String replaceRecipe(String input, String oldText, String newText) {
        return input.replaceFirst(Pattern.quote("\"recipe\":" + oldText + "}]"), "\"recipe\":" + newText + "}]");
    }
}
