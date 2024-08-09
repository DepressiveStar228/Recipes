package com.example.recipes.Controller;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

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

        int firstCurlyBraceIndex = jsonResponse.indexOf('{');
        int lastCurlyBraceIndex = jsonResponse.lastIndexOf('}');

        if (firstCurlyBraceIndex != -1 && lastCurlyBraceIndex != -1 && firstCurlyBraceIndex < lastCurlyBraceIndex) {
            String finalJsonResponse = jsonResponse.substring(firstCurlyBraceIndex, lastCurlyBraceIndex + 1);
            String recipe = extractRecipe(finalJsonResponse);

            finalJsonResponse = finalJsonResponse.replaceAll("\n", "")
                    .replaceAll("\\s*\\{\\s*", "{")
                    .replaceAll("\\s*\\}\\s*", "}")
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

        return recipeData;
    }

    public static Uri exportRecipeData(Context context, Collection collection) {
        RecipeUtils utils = new RecipeUtils(context);
        ArrayList<Dish> dishes = utils.getDishesByCollection(collection.getId());
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        if (!dishes.isEmpty()) {
            for (Dish dish : dishes) {
                ArrayList<Ingredient> dishIngredients = utils.getIngredients(dish.getID());
                ingredients.addAll(dishIngredients);
            }

            DataBox recipeData = new DataBox(dishes, ingredients);

            Gson gson = new Gson();
            String jsonString = gson.toJson(recipeData);
            File internalDir = context.getFilesDir();
            File jsonFile = new File(internalDir, utils.getNameCollection(collection.getId()) + ".json");

            try (FileOutputStream fos = new FileOutputStream(jsonFile);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
                writer.write(jsonString);
                return FileProvider.getUriForFile(context, "com.example.recipes.file-provider", jsonFile);
            } catch (IOException e) {
                Toast.makeText(context, context.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                Log.e("ImportExportController", "Помилка при експорті рецептів", e);
            }
        } else {
            Toast.makeText(context, context.getString(R.string.error_empty_collection), Toast.LENGTH_SHORT).show();
            Log.e("ImportExportController", "Помилка при експорті рецептів");
        }
        return null;
    }

    public static Uri exportRecipeData(Context context, DataBox recipeData) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(recipeData);
        File internalDir = context.getFilesDir();
        File jsonFile = new File(internalDir, "my_recipes.json");

        try (FileOutputStream fos = new FileOutputStream(jsonFile);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
            writer.write(jsonString);
            return FileProvider.getUriForFile(context, "com.example.recipes.file-provider", jsonFile);
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
            Log.e("ImportExportController", "Помилка при експорті рецептів", e);
        }
        return null;
    }

    public static Uri exportDish(Context context, Dish dish) {
        RecipeUtils utils = new RecipeUtils(context);
        ArrayList<Dish> dishes = new ArrayList<>();
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        dishes.add(dish);
        ingredients.addAll(utils.getIngredients(dish.getID()));

        Gson gson = new Gson();
        String jsonString = gson.toJson(new DataBox(dishes, ingredients));
        File internalDir = context.getFilesDir();
        File jsonFile = new File(internalDir, dish.getName() + ".json");

        try (FileOutputStream fos = new FileOutputStream(jsonFile);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
            writer.write(jsonString);
            return FileProvider.getUriForFile(context, "com.example.recipes.file-provider", jsonFile);
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.error_export), Toast.LENGTH_SHORT).show();
            Log.e("ImportExportController", "Помилка при експорті рецептів", e);
        }
        return null;
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
