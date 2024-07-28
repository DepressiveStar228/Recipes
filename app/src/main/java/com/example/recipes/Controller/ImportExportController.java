package com.example.recipes.Controller;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.recipes.Item.Collection;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.RecipeUtils;
import com.example.recipes.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.URI;
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
            finalJsonResponse = finalJsonResponse.replaceAll("\n", "")
                    .replaceAll("\\s*\\{\\s*", "{")
                    .replaceAll("\\s*\\}\\s*", "}")
                    .replaceAll(":\\s*", ":")
                    .replaceAll(",\\s*\"", ",\"");

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

    private static Uri getExportUri(Context context, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/recipes");

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
        }
        return uri;
    }
}
