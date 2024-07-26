package com.example.recipes.Controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;

public class FileControllerIngredient {
    private static final String TABLE_NAME = "Ingredient";
    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String AMOUNT = "amount";
    private static final String TYPE = "type";
    private static final String ID_DISH = "id_dish";
    private Context context;
    private DB_Confic db_confic;
    private SQLiteDatabase database;

    public FileControllerIngredient(Context context) {
        this.context = context;
        db_confic = new DB_Confic(context);
    }

    public FileControllerIngredient(Context context, SQLiteDatabase database) {
        this.context = context;
        this.database = database;
    }

    public void openDb() throws SQLException {
        database = db_confic.getWritableDatabase();
    }

    public SQLiteDatabase getDatabase(){
        return database;
    }

    public void closeDb() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public boolean insert(String name, String amount, String type, int id_dish) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, name);
        cv.put(AMOUNT, amount);
        cv.put(TYPE, type);
        cv.put(ID_DISH, id_dish);
        long result = database.insert(TABLE_NAME, null, cv);

        if (result == -1) {
            Log.e("FileControllerCollections", "Insert неуспішний");
            return false;
        } else {
            Log.d("FileControllerCollections", "Insert успішний");
            return true;
        }
    }

    public Ingredient getById(int id) {
        Cursor query = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id = " + id, null);
        Ingredient ingredient = null;

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    int _id = query.getInt(0);
                    String name = query.getString(1);
                    String amount = query.getString(2);
                    String type = query.getString(3);
                    int id_dish = query.getInt(4);

                    ingredient = new Ingredient(_id, name, amount, type, id_dish);
                }
            } finally {
                query.close();
            }
        }
        return ingredient;
    }

    public int getIdByNameAndDishID(String name, int id_dish) {
        Cursor query = database.rawQuery("SELECT _id FROM " + TABLE_NAME + " WHERE " + NAME + " = ? AND " + ID_DISH + " = " + id_dish + ";", new String[]{name});
        int id = -1;

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    id = query.getInt(0);
                }
            } finally {
                query.close();
            }
        }

        return id;
    }

    public ArrayList<Ingredient> getByDishId(int id_dish) {
        Cursor query = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE id_dish = " + id_dish, null);
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        int _id = query.getInt(0);
                        String name = query.getString(1);
                        String amount = query.getString(2);
                        String type = query.getString(3);
                        int _id_dish = query.getInt(4);
                        ingredients.add(new Ingredient(_id, name, amount, type, _id_dish));
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return ingredients;
    }

    public ArrayList<Ingredient> getAllIngredientNameOrdered() {
        Cursor query = database.rawQuery("SELECT DISTINCT " + NAME + " FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC", null);
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        String name = query.getString(0);
                        ingredients.add(new Ingredient(0, name, "0", "0", 0));
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return ingredients;
    }

    public ArrayList<Ingredient> getAllIngredient() {
        Cursor query = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        int _id = query.getInt(0);
                        String name = query.getString(1);
                        String amount = query.getString(2);
                        String type = query.getString(3);
                        int _id_dish = query.getInt(4);
                        ingredients.add(new Ingredient(_id, name, amount, type, _id_dish));
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return ingredients;
    }

    public ArrayList<Integer> getDishIdByName(String name) {
        Cursor query = database.rawQuery("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + NAME + " = ?", new String[]{name});
        ArrayList<Integer> dish_ids = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        int _id_dish = query.getInt(0);
                        dish_ids.add(_id_dish);
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return dish_ids;
    }

    public boolean delete(int id) {
        try {
            int rowsDeleted = database.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
            if (rowsDeleted > 0) {
                Log.d("FileControllerCollections", "Інгредієнт успішно видалено.");
                return true;
            } else {
                Log.d("FileControllerCollections", "Інгредієнт не знайдено видалення.");
                return false;
            }
        } catch (Exception e) {
            Log.e("FileControllerCollections", "Помилка видалення інгредієнта: " + e.getMessage());
            return false;
        }
    }

    public void update(int id, Ingredient ingredient) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, ingredient.getName());
        cv.put(AMOUNT, ingredient.getAmount());
        cv.put(TYPE, ingredient.getType());
        cv.put(ID_DISH, ingredient.getID_Dish());

        database.update(TABLE_NAME, cv, "_id=?", new String[]{String.valueOf(id)});
    }

    public void beginTransaction() {
        database.beginTransaction();
    }

    public void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    public void endTransaction() {
        database.endTransaction();
    }
}
