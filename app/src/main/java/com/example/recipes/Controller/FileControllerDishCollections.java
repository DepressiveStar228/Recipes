package com.example.recipes.Controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class FileControllerDishCollections {
    private static final String DATABASE_NAME = "Dishes.db";
    private static final String TABLE_NAME = "Dish_Collections";
    private static final String ID = "_id";
    private static final String ID_DISH = "id_dish";
    private static final String ID_COLLECTION = "id_collection";
    private Context context;
    private DB_Confic db_confic;
    private SQLiteDatabase database;

    public FileControllerDishCollections(Context context) {
        this.context = context;
        db_confic = new DB_Confic(context);
    }

    public FileControllerDishCollections(Context context, SQLiteDatabase database) {
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

    public boolean insert(int id_dish, int id_collection) {
        ContentValues cv = new ContentValues();
        cv.put(ID_DISH, id_dish);
        cv.put(ID_COLLECTION, id_collection);
        long result = database.insert(TABLE_NAME, null, cv);

        if (result == -1) {
            Log.e("FileControllerDishCollections", "Помилка додавання страви");
            return false;
        } else {
            Log.d("FileControllerDishCollections", "Страва успішно додана");
            return true;
        }
    }

    public int getIdByData(int id_dish, int id_collection) {
        Cursor query = database.rawQuery("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = ?" + " AND " + ID_COLLECTION + " = ?", new String[]{String.valueOf(id_dish), String.valueOf(id_collection)});
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

    public ArrayList<Integer> getAllIdDishByCollection(int id) {
        Cursor query = database.rawQuery("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = " + id, null);
        ArrayList<Integer> id_dishes = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        int _id = query.getInt(0);
                        id_dishes.add(_id);
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return id_dishes;
    }

    public ArrayList<Integer> getAllIdCollectionByDish(int id) {
        Cursor query = database.rawQuery("SELECT " + ID_COLLECTION + " FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = " + id, null);
        ArrayList<Integer> id_collections = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        int _id = query.getInt(0);
                        id_collections.add(_id);
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }
        }
        return id_collections;
    }

    public boolean delete(int id) {
        try {
            int rowsDeleted = database.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(id)});
            if (rowsDeleted > 0) {
                Log.d("FileControllerDishCollections", "Запис успішно видалено.");
                return true;
            } else {
                Log.d("FileControllerDishCollections", "Запис не знайдено для видалення.");
                return false;
            }
        } catch (Exception e) {
            Log.e("FileControllerDishCollections", "Помилка при видаленні запису: " + e.getMessage());
            return false;
        }
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
