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
import com.example.recipes.R;

import java.util.ArrayList;

public class FileControllerCollections {
    private static final String DATABASE_NAME = "Dishes.db";
    private static final String TABLE_NAME = "Collections";
    private static final String ID = "_id";
    private static final String NAME = "name";
    private FileControllerDish fileControllerDish;
    private FileControllerDishCollections fileControllerDishCollections;
    private Context context;
    private DB_Confic db_confic;
    private SQLiteDatabase database;

    public FileControllerCollections(Context context) {
        this.context = context;
        db_confic = new DB_Confic(context);
    }

    public void openDb() throws SQLException {
        database = db_confic.getWritableDatabase();
        fileControllerDish = new FileControllerDish(context, database);
        fileControllerDishCollections = new FileControllerDishCollections(context, database);
        checkBaseCollection();
        Log.d("FileControllerCollections", "БД успішно відкрита");
    }

    public SQLiteDatabase getDatabase(){
        return database;
    }

    public void closeDb() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        fileControllerDish.closeDb();
        fileControllerDishCollections.closeDb();

        Log.d("FileControllerCollections", "БД успішно закрита");
    }

    public boolean insert(String name) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, name);
        long result =  database.insert(TABLE_NAME, null, cv);

        if (result == -1) {
            Log.e("FileControllerCollections", "Insert неуспішний");
            return false;
        } else {
            Log.d("FileControllerCollections", "Insert успішний");
            return true;
        }
    }

    public Collection getById(int id) {
        Cursor query = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id = " + id, null);
        Collection collection = null;

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    int _id = query.getInt(0);
                    String name = query.getString(1);

                    collection = new Collection(_id, name, getDishes(_id));
                }
            } finally {
                query.close();
            }
        }

        Log.d("FileControllerCollections", "Повернення колекції по Айді успішне");
        return collection;
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

        Log.d("FileControllerCollections", "Повернення айді за назвою колекції успішне");
        return id;
    }

    public ArrayList<Collection> getAllCollection() {
        Cursor query = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        ArrayList<Collection> collections = new ArrayList<>();

        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        int _id = query.getInt(0);
                        String name = query.getString(1);

                        if (_id == 2){
                            String ww = "sdf";
                        }

                        Collection collection = new Collection(_id, name, getDishes(_id));
                        collections.add(collection);
                    } while (query.moveToNext());
                }
            } finally {
                query.close();
            }

            for (Collection collection : collections) {
                ArrayList<Dish> dishes = getDishes(collection.getId());
                collection.setDishes(dishes);
            }
        }

        Log.d("FileControllerCollections", "Повернення списку всіх колекцій успішне");
        return collections;
    }

    public ArrayList<Dish> getDishes(int id) {
        ArrayList<Integer> id_dishes = null;
        ArrayList<Dish> dishes = new ArrayList<>();;

        fileControllerDishCollections.beginTransaction();
        try {
            id_dishes = fileControllerDishCollections.getAllIdDishByCollection(id);
            fileControllerDishCollections.setTransactionSuccessful();
        } finally {
            fileControllerDishCollections.endTransaction();
        }

        if (id_dishes != null) {
            fileControllerDish.beginTransaction();
            try {
                for (int id_dish : id_dishes) {
                    dishes.add(fileControllerDish.getById(id_dish));
                }
                fileControllerDish.setTransactionSuccessful();
            } finally {
                fileControllerDish.endTransaction();
            }
        } else {
            Log.d("FileControllerCollections", "Страви в колекції не виявилося");
        }

        Log.d("FileControllerCollections", "Повернення списку всіх страв колекції успішне");
        return dishes;
    }

    private void checkBaseCollection() {
        boolean isCreate = true;

        for (int i = 1; i <= 4; i++) {
            int id = getIdByName(context.getString(R.string.system_collection_tag) + String.valueOf(i));

            if (id == -1) {
                insert(context.getString(R.string.system_collection_tag) + String.valueOf(i));
                isCreate = false;
            }
        }

        if (!isCreate) { Log.d("FileControllerCollections", "Створення системних колекцій"); }
    }

    public boolean delete(int id) {
        try {
            int rowsDeleted = database.delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});
            if (rowsDeleted > 0) {
                Log.d("FileControllerCollections", "Запис успішно видалено.");
                return true;
            } else {
                Log.d("FileControllerCollections", "Запис не знайдено для видалення.");
                return false;
            }
        } catch (Exception e) {
            Log.e("FileControllerCollections", "Помилка при видаленні запису:" + e.getMessage());
            return false;
        }
    }

    public boolean update(int id, Collection collection) {
        ContentValues cv = new ContentValues();
        cv.put(NAME, collection.getName());

        int rowsAffected = database.update(TABLE_NAME, cv, "_id=?", new String[]{String.valueOf(id)});
        if (rowsAffected > 0) {
            Log.d("FileControllerCollections", "Колекцію успішно оновлено.");
            return true;
        } else {
            Log.d("FileControllerCollections", "Не вдалося оновити колекцію.");
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
