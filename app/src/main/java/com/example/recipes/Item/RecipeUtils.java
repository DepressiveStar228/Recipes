package com.example.recipes.Item;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Controller.FileControllerCollections;
import com.example.recipes.Controller.FileControllerDish;
import com.example.recipes.Controller.FileControllerDishCollections;
import com.example.recipes.Controller.FileControllerIngredient;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;


public class RecipeUtils {
    private Context context;
    private FileControllerDish fileControllerDish;
    private FileControllerIngredient fileControllerIngredient;
    private FileControllerCollections fileControllerCollections;
    private FileControllerDishCollections fileControllerDishCollections;

    public RecipeUtils(Context context) {
        this.context = context;
        fileControllerDish = new FileControllerDish(context);
        fileControllerIngredient = new FileControllerIngredient(context);
        fileControllerCollections = new FileControllerCollections(context);
        fileControllerDishCollections = new FileControllerDishCollections(context);
        fileControllerDish.openDb();
        fileControllerIngredient.openDb();
        fileControllerCollections.openDb();
        fileControllerDishCollections.openDb();
    }

    public boolean addDish(Dish dish, int id_collection) {
        String dishName = dish.getName();
        boolean result;
        int suffix = 1;

        while (checkDuplicateDishName(dishName)) {
            dishName = dish.getName() + " №" + suffix;
            suffix++;
        }

        fileControllerDish.beginTransaction();
        try {
            result = fileControllerDish.insert(dishName, dish.getRecipe());
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }

        if (result) {
            Dish dish_inserted = getDish(getIdDishByName(dishName));

            if (!addDishCollectionData(dish_inserted, id_collection)) {
                Log.e("RecipeUtils", "Помилка додавання страви до колекції.");
            }
        }

        return result;
    }

    public boolean addDish(Dish dish, ArrayList<Ingredient> ingredients, int id_collection) {
        String dishName = dish.getName();
        boolean result;
        int suffix = 1;

        while (checkDuplicateDishName(dishName)) {
            dishName = dish.getName() + " №" + suffix;
            suffix++;
        }

        fileControllerDish.beginTransaction();
        try {
            result = fileControllerDish.insert(dishName, dish.getRecipe());
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }

        if (result) {
            Dish dish_inserted = getDish(getIdDishByName(dishName));

            if (!addIngredients(dish_inserted, ingredients)) {
                Log.e("RecipeUtils", "Помилка додавання інгредієнтів.");
            }

            if (!addDishCollectionData(dish_inserted, id_collection)) {
                Log.e("RecipeUtils", "Помилка додавання страви до колекції.");
            }
        }

        return result;
    }


    public boolean addRecipe(DataBox box, int id_collection) {
        int countFalse = 0;
        ArrayList<Dish> dishes = box.getDishes();
        ArrayList<Ingredient> ingredients = box.getIngredients();

        if (dishes != null && ingredients != null) {
            for (Dish dish : dishes){
                String dishName = dish.getName();
                int suffix = 1;

                while (checkDuplicateDishName(dishName)) {
                    dishName = dish.getName() + " №" + suffix;
                    suffix++;
                }

                Dish newDish = new Dish(dishName, dish.getRecipe());

                if (addDish(newDish, id_collection)) {
                    if (!addIngredients(getIdDishByName(dishName), dish.getID(), ingredients)) { countFalse++; }
                }
            }

            Log.d("RecipeUtils", "Успішно додано страви інгредієнти: (" + (dishes.size() - countFalse) + "/" + dishes.size() + ")");
            if (countFalse == dishes.size() && dishes.isEmpty()) { return false; }
            else { return true; }
        } else {
            Toast.makeText(context, context.getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
            Log.e("RecipeUtils", "Помилка додавання рецептів.");
            return false;
        }
    }

    public boolean checkDuplicateDishName(String name){
        int id_dish = -1;

        fileControllerDish.beginTransaction();
        try {
            id_dish = fileControllerDish.getIdByName(name);
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }

        return id_dish != -1;
    }

    public Dish getDish(int id) {
        Dish dish = null;

        fileControllerDish.beginTransaction();
        try {
            dish = fileControllerDish.getById(id);
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }

        return dish;
    }

    public int getIdDishByName(String name) {
        int id = -1;

        fileControllerDish.beginTransaction();
        try {
            id = fileControllerDish.getIdByName(name);
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }

        return id;
    }

    public ArrayList<Dish> getDishesOrdered() {
        ArrayList<Dish> name_dishes = new ArrayList<>();

        fileControllerDish.beginTransaction();
        try {
            name_dishes = fileControllerDish.getAllDishOrdered();
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }

        return name_dishes;
    }

    public void updateDish(int id_dish, Dish newDish){
        fileControllerDish.beginTransaction();
        try {
            fileControllerDish.update(id_dish, newDish);
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }
    }

    public boolean deleteDish(Dish dish) {
        boolean flag1 = false, flag2 = false, flag3 = false;

        fileControllerDish.beginTransaction();
        try {
            flag1 = fileControllerDish.delete(dish.getID());
            fileControllerDish.setTransactionSuccessful();
        } finally {
            fileControllerDish.endTransaction();
        }

        ArrayList<Ingredient> ingList = fileControllerIngredient.getByDishId(dish.getID());
        if (!ingList.isEmpty()) {
            for (Ingredient in : ingList) {
                flag2 = deleteIngredient(in.getID());
            }
        } else {
            flag2 = true;
        }

        flag3 = deleteDishCollectionData(dish.getID());

        if (flag1 && flag2 && flag3) {
            Log.d("RecipeUtils", "Рецепт успішно видалено.");
            return true;
        }
        else {
            Log.e("RecipeUtils", "Помилка видалення рецепта.");
            return false;
        }
    }

    public boolean deleteRecipe(DataBox box) {
        ArrayList<Dish> dishes = box.getDishes();
        boolean flag1 = false, flag2 = false, flag3 = false;

        fileControllerDish.beginTransaction();
        try {
            fileControllerDish.delete(dishes.get(0).getID());
            fileControllerDish.setTransactionSuccessful();
            flag1 = true;
        } finally {
            fileControllerDish.endTransaction();
        }

        fileControllerIngredient.beginTransaction();
        try {
            ArrayList<Ingredient> ingList = fileControllerIngredient.getByDishId(dishes.get(0).getID());

            for (Ingredient in : ingList) {
                fileControllerIngredient.delete(in.getID());
            }

            fileControllerIngredient.setTransactionSuccessful();
            flag2 = true;
        } finally {
            fileControllerIngredient.endTransaction();
        }

        fileControllerDishCollections.beginTransaction();
        try {
            ArrayList<Integer> id_collections = fileControllerDishCollections.getAllIdCollectionByDish(dishes.get(0).getID());

            for (Integer id : id_collections) {
                int id_collection = fileControllerDishCollections.getIdByData(dishes.get(0).getID(), id);
                fileControllerDishCollections.delete(id_collection);
            }

            fileControllerDishCollections.setTransactionSuccessful();
            flag3 = true;
        } finally {
            fileControllerDishCollections.endTransaction();
        }

        if (flag1 && flag2 && flag3) {
            Log.d("RecipeUtils", "Рецепт успішно видалено.");
            return true;
        }
        else {
            Log.e("RecipeUtils", "Помилка видалення рецепта.");
            return false;
        }
    }













    public boolean addIngredients(Dish dish, ArrayList<Ingredient> ingredients) {
        int countFalse = 0;

        for (Ingredient ing : ingredients){
            fileControllerIngredient.beginTransaction();
            try {
                if (!fileControllerIngredient.insert(ing.getName(), ing.getAmount(), ing.getType(), dish.getID())){ countFalse++; }
                fileControllerIngredient.setTransactionSuccessful();
            } finally {
                fileControllerIngredient.endTransaction();
            }
        }

        Log.d("RecipeUtils", "Успішно додано інредієнтів: (" + (ingredients.size() - countFalse) + "/" + ingredients.size() + ")");
        if (countFalse == ingredients.size() && ingredients.isEmpty()) { return false; }
        else { return true; }
    }

    public boolean addIngredients(int newID, int oldID, ArrayList<Ingredient> ingredients) {
        int countFalse = 0;

        for (Ingredient ing : ingredients){
            if (oldID == ing.getID_Dish()) {
                fileControllerIngredient.beginTransaction();
                try {
                    if (!fileControllerIngredient.insert(ing.getName(), ing.getAmount(), ing.getType(), newID)){ countFalse++; }
                    fileControllerIngredient.setTransactionSuccessful();
                } finally {
                    fileControllerIngredient.endTransaction();
                }
            }
        }

        Log.d("RecipeUtils", "Успішно додано інредієнтів: (" + (ingredients.size() - countFalse) + "/" + ingredients.size() + ")");
        if (countFalse == ingredients.size() && ingredients.isEmpty()) { return false; }
        else { return true; }
    }

    public boolean addIngredients(int id_dish, ArrayList<Ingredient> ingredients) {
        int countFalse = 0;

        for (Ingredient ing : ingredients){
            if (id_dish == ing.getID_Dish()) {
                fileControllerIngredient.beginTransaction();
                try {
                    if (!fileControllerIngredient.insert(ing.getName(), ing.getAmount(), ing.getType(), id_dish)){ countFalse++; }
                    fileControllerIngredient.setTransactionSuccessful();
                } finally {
                    fileControllerIngredient.endTransaction();
                }
            }
        }

        Log.d("RecipeUtils", "Успішно додано інредієнтів: (" + (ingredients.size() - countFalse) + "/" + ingredients.size() + ")");
        if (countFalse == ingredients.size() && ingredients.isEmpty()) { return false; }
        else { return true; }
    }

    public int getIngredientId(Ingredient in) {
        int id = -1;

        fileControllerIngredient.beginTransaction();
        try {
            id = fileControllerIngredient.getIdByNameAndDishID(in.getName(), in.getID_Dish());
            fileControllerIngredient.setTransactionSuccessful();
        } finally {
            fileControllerIngredient.endTransaction();
        }

        return id;
    }

    public ArrayList<Ingredient> getIngredients() {
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        fileControllerIngredient.beginTransaction();
        try {
            ingredients = fileControllerIngredient.getAllIngredient();
            fileControllerIngredient.setTransactionSuccessful();
        } finally {
            fileControllerIngredient.endTransaction();
        }

        return ingredients;
    }

    public ArrayList<Ingredient> getIngredients(int id_dish) {
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        fileControllerIngredient.beginTransaction();
        try {
            ingredients = fileControllerIngredient.getByDishId(id_dish);
            fileControllerIngredient.setTransactionSuccessful();
        } finally {
            fileControllerIngredient.endTransaction();
        }

        return ingredients;
    }

    public ArrayList<Ingredient> getIngredientsOrdered() {
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        fileControllerIngredient.beginTransaction();
        try {
            ingredients = fileControllerIngredient.getAllIngredientNameOrdered();
            fileControllerIngredient.setTransactionSuccessful();
        } finally {
            fileControllerIngredient.endTransaction();
        }

        return ingredients;
    }

    public ArrayList<Integer> getDishIdsByNameIngredient(String nameIng) {
        ArrayList <Integer> dish_ids = new ArrayList<>();

        fileControllerIngredient.beginTransaction();
        try {
            dish_ids = fileControllerIngredient.getDishIdByName(nameIng);
            fileControllerIngredient.setTransactionSuccessful();
        } finally {
            fileControllerIngredient.endTransaction();
        }

        return dish_ids;
    }

    public void deleteIngredient(Ingredient ingredient) {
        fileControllerIngredient.beginTransaction();
        try {
            fileControllerIngredient.delete(getIngredientId(ingredient));
            fileControllerIngredient.setTransactionSuccessful();
        } finally {
            fileControllerIngredient.endTransaction();
        }
    }

    public boolean deleteIngredient(int id_ing) {
        boolean result;

        fileControllerIngredient.beginTransaction();
        try {
            result = fileControllerIngredient.delete(id_ing);
            fileControllerIngredient.setTransactionSuccessful();
        } finally {
            fileControllerIngredient.endTransaction();
        }

        return result;
    }















    public boolean addCollection(String name) {
        boolean result;

        fileControllerCollections.beginTransaction();
        try {
            result = fileControllerCollections.insert(name);
            fileControllerCollections.setTransactionSuccessful();
        }
        finally {
            fileControllerCollections.endTransaction();
        }

        return result;
    }

    public int getIdCollectionByName(String name) {
        int id = -1;

        fileControllerCollections.beginTransaction();
        try {
            id = fileControllerCollections.getIdByName(name);
            fileControllerCollections.setTransactionSuccessful();
        }
        finally {
            fileControllerCollections.endTransaction();
        }

        return id;
    }

    public Collection getCollectionByName(String name) {
        Collection collection = null;

        fileControllerCollections.beginTransaction();
        try {
            collection = fileControllerCollections.getById(getIdCollectionByName(name));
            fileControllerCollections.setTransactionSuccessful();
        }
        finally {
            fileControllerCollections.endTransaction();
        }

        return collection;
    }

    public String getNameCollection(int id) {
        String name = "";

        fileControllerCollections.beginTransaction();
        try {
            Collection collection = fileControllerCollections.getById(id);

            if (collection != null){
                name = collection.getName();
            }

            fileControllerCollections.setTransactionSuccessful();
        } finally {
            fileControllerCollections.endTransaction();
        }

        return name;
    }

    public ArrayList<Collection> getAllCollections() {
        ArrayList<Collection> collections = new ArrayList<>();

        fileControllerCollections.beginTransaction();
        try {
            collections = fileControllerCollections.getAllCollection();
            fileControllerCollections.setTransactionSuccessful();
        } finally {
            fileControllerCollections.endTransaction();
        }

        return collections;
    }

    public ArrayList<Dish> getDishesByCollection(int id_collection) {
        ArrayList<Dish> dishes = new ArrayList<>();

        fileControllerCollections.beginTransaction();
        try {
            dishes = fileControllerCollections.getDishes(id_collection);
            fileControllerCollections.setTransactionSuccessful();
        } finally {
            fileControllerCollections.endTransaction();
        }

        return dishes;
    }

    public boolean updateCollection(int id_collection, Collection newCollection) {
        boolean result;

        fileControllerCollections.beginTransaction();
        try {
            result = fileControllerCollections.update(id_collection, newCollection);
            fileControllerCollections.setTransactionSuccessful();
        } finally {
            fileControllerCollections.endTransaction();
        }

        return result;
    }

    public boolean deleteCollection(int id_collection, boolean mode) {
        boolean statusDelCollection;
        int countFalse = 0;
        ArrayList<Dish> dishes = getDishesByCollection(id_collection);

        if (mode) {
            for (Dish dish : dishes) {
                if (!deleteDishCollectionData(dish.getID())){ countFalse++; }
                if (!deleteDish(dish)){ countFalse++; }
            }
        }

        fileControllerCollections.beginTransaction();
        try {
            statusDelCollection = fileControllerCollections.delete(id_collection);
            fileControllerCollections.setTransactionSuccessful();
        }
        finally {
            fileControllerCollections.endTransaction();
        }

        if (mode) {
            if (countFalse == dishes.size() && !dishes.isEmpty() || !statusDelCollection) { return false; }
            else {
                Toast.makeText(context, context.getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            return statusDelCollection;
        }
    }










    public boolean addDishCollectionData(Dish dish, int id_collection) {
        boolean result;

        fileControllerDishCollections.beginTransaction();
        try {
            if (fileControllerDishCollections.getIdByData(dish.getID(), id_collection) == -1) {
                result = fileControllerDishCollections.insert(dish.getID(), id_collection);
                fileControllerDishCollections.setTransactionSuccessful();
            } else {
                result = false;
                Toast.makeText(context, context.getString(R.string.dish_dublicate_in_collection) + " " + getNameCollection(id_collection), Toast.LENGTH_SHORT).show();
                Log.d("RecipeUtils", "Страва вже є у колекції");
            }
        } finally {
            fileControllerDishCollections.endTransaction();
        }

        return result;
    }

    public boolean addDishCollectionData(Dish dish, ArrayList<Integer> id_collections) {
        int countFalse = 0;
        ArrayList<Integer> id_collections_duplicate = new ArrayList<>();

        fileControllerDishCollections.beginTransaction();
        try {
            for (Integer id_collection : id_collections){
                if (fileControllerDishCollections.getIdByData(dish.getID(), id_collection) == -1) {
                    if (!fileControllerDishCollections.insert(dish.getID(), id_collection)){ countFalse++; }
                } else {
                    countFalse++;
                    id_collections_duplicate.add(id_collection);
                }
            }
            fileControllerDishCollections.setTransactionSuccessful();
        } finally {
            fileControllerDishCollections.endTransaction();
        }

        if (!id_collections.isEmpty()) {
            for (int id_collection : id_collections_duplicate) {
                Toast.makeText(context, context.getString(R.string.dish_dublicate_in_collection) + " " + getCustomNameSystemCollection(getNameCollection(id_collection)), Toast.LENGTH_SHORT).show();
                Log.d("RecipeUtils", "Страва вже є у колекції");
            }
        }

        Log.d("RecipeUtils", "Успішно додано записів страва/колекція: (" + (id_collections.size() - countFalse) + "/" + id_collections.size() + ")");
        if (countFalse == id_collections.size()) { return false; }
        else { return true; }
    }

    public boolean copyDishesToAnotherCollections (int id_collection_origin, ArrayList<Integer> id_collections) {
        int countFalse = 0;
        ArrayList<Integer> id_collections_duplicate = new ArrayList<>();

        for (Integer id_collection : id_collections){
            if (id_collection != id_collection_origin) {
                ArrayList<Dish> dishes = getDishesByCollection(id_collection_origin);
                if (dishes.isEmpty()) { return false; }

                for (Dish dish : dishes) {
                    if (!checkDuplicateDishCollectionData(dish.getID(), id_collection)) {
                        if (!addDishCollectionData(dish, id_collection)){ countFalse++; }
                    } else {
                        countFalse++;
                        id_collections_duplicate.add(id_collection);
                    }
                }
            }
        }

        if (!id_collections.isEmpty()) {
            for (int id_collection : id_collections_duplicate) {
                Toast.makeText(context, context.getString(R.string.dish_dublicate_in_collection) + " " + getCustomNameSystemCollection(getNameCollection(id_collection)), Toast.LENGTH_SHORT).show();
                Log.d("RecipeUtils", "Страва вже є у колекції");
            }
        }

        Log.d("RecipeUtils", "Успішно копійовано страв у колекцію: (" + (id_collections.size() - countFalse) + "/" + id_collections.size() + ")");
        if (countFalse == id_collections.size()) { return false; }
        else { return true; }
    }

    public boolean checkDuplicateDishCollectionData (int id_dish, int id_collection) {
        boolean result = true;

        fileControllerDishCollections.beginTransaction();
        try {
            if (fileControllerDishCollections.getIdByData(id_dish, id_collection) == -1) { return false; }
            fileControllerDishCollections.setTransactionSuccessful();
        } finally {
            fileControllerDishCollections.endTransaction();
        }

        return result;
    }

    private boolean deleteDishCollectionData(int id_dish) {
        int countFalse = 0;
        ArrayList<Integer> id_collections = new ArrayList<>();

        fileControllerDishCollections.beginTransaction();
        try {
            id_collections = fileControllerDishCollections.getAllIdCollectionByDish(id_dish);

            for (Integer id_collection : id_collections) {
                int id_dishCollectionData = fileControllerDishCollections.getIdByData(id_dish, id_collection);
                if (!fileControllerDishCollections.delete(id_dishCollectionData)){ countFalse++; }
            }

            fileControllerDishCollections.setTransactionSuccessful();
        } finally {
            fileControllerDishCollections.endTransaction();
        }

        Log.d("RecipeUtils", "Успішно видалено записів страва/колекція: (" + (id_collections.size() - countFalse) + "/" + id_collections.size() + ")");
        if (countFalse == id_collections.size() && !id_collections.isEmpty()) { return false; }
        else { return true; }
    }

    public boolean deleteDishCollectionData(Collection collection) {
        int countFalse = 0;
        ArrayList<Dish> dishes = getDishesByCollection(collection.getId());

        fileControllerDishCollections.beginTransaction();
        try {
            for (Dish dish : dishes) {
                int id_dishCollectionData = fileControllerDishCollections.getIdByData(dish.getID(), collection.getId());
                if (!fileControllerDishCollections.delete(id_dishCollectionData)){ countFalse++; }
            }

            fileControllerDishCollections.setTransactionSuccessful();
        } finally {
            fileControllerDishCollections.endTransaction();
        }

        Log.d("RecipeUtils", "Успішно видалено записів страва/колекція: (" + (dishes.size() - countFalse) + "/" + dishes.size() + ")");
        if (countFalse == dishes.size() && !dishes.isEmpty()) { return false; }
        else { return true; }
    }











    public void close() {
        fileControllerDish.closeDb();
        fileControllerIngredient.closeDb();
        fileControllerCollections.closeDb();
        fileControllerDishCollections.closeDb();
    }

    public String getCustomNameSystemCollection(String name) {
        String systemTag = context.getString(R.string.system_collection_tag);

        if (Objects.equals(name, systemTag + "1")) { return context.getString(R.string.favorites); }
        else if (Objects.equals(name, systemTag + "2")) { return context.getString(R.string.my_recipes); }
        else if (Objects.equals(name, systemTag + "3")) { return context.getString(R.string.gpt_recipes); }
        else if (Objects.equals(name, systemTag + "4")) { return context.getString(R.string.import_recipes); }
        else { return name; }
    }
}
