package com.example.recipes.Controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.util.ArrayList;

public class FileControllerDish {
    private static final String DATABASE_NAME = "Dishes.db";
    private static final String TABLE_NAME = "Dish";
    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String RECIPE = "recipe";
    private Context context;
    private DB_Confic db_confic;
    private SQLiteDatabase database;

    public FileControllerDish(Context context) {
        this.context = context;
        db_confic = new DB_Confic(context);
    }

    public FileControllerDish(Context context, SQLiteDatabase database) {
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

    public boolean insert(String name, String recipe) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, name);
        cv.put(RECIPE, recipe);
        long result = database.insert(TABLE_NAME, null, cv);

        if (result == -1) {
            Log.e("FileControllerDish", "Помилка додавання страви");
            return false;
        } else {
            Log.d("FileControllerDish", "Страва успішно додана");
            return true;
        }
    }

    public Dish getById(int id) {
        Cursor query = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id = " + id, null);
        Dish dish = null;

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    int _id = query.getInt(0);
                    String name = query.getString(1);
                    String recipe = query.getString(2);

                    dish = new Dish(_id, name, recipe);
                }
            } finally {
                query.close();
            }
        }
        return dish;
    }

    public int getIdByName(String name) {
        Cursor query = database.rawQuery("SELECT _id FROM " + TABLE_NAME + " WHERE " + NAME + " = ?", new String[]{name});
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

    public ArrayList<Dish> getAllDishOrdered() {
        Cursor query = database.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC", null);
        ArrayList<Dish> dishes = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        int _id = query.getInt(0);
                        String name = query.getString(1);
                        String recipe = query.getString(2);

                        Dish dish = new Dish(_id, name, recipe);
                        dishes.add(dish);
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return dishes;
    }

    public ArrayList<String> getAllNames() {
        Cursor query = database.rawQuery("SELECT " + NAME + " FROM " + TABLE_NAME, null);
        ArrayList<String> nameDishes = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        String name = query.getString(0);
                        nameDishes.add(name);
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return nameDishes;
    }

    public boolean delete(int id) {
        try {
            int rowsDeleted = database.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
            if (rowsDeleted > 0) {
                Log.d("FileControllerDish", "Страва успішно вилучена.");
                return true;
            } else {
                Log.d("FileControllerDish", "Страву не знайдено для видалення.");
                return false;
            }
        } catch (Exception e) {
            Log.e("FileControllerDish", "Помилка при видаленні страви: " + e.getMessage());
            return false;
        }
    }

    public void update(int id, Dish dish) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, dish.getName());
        cv.put(RECIPE, dish.getRecipe());

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
