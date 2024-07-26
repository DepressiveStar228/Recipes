package com.example.recipes.Controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

public class DB_Confic extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "Dishes.db";
    private static final String TABLE_DISH = "Dish";
    private static final String TABLE_INGREDIENT = "Ingredient";
    private static final String TABLE_COLLECTIONS = "Collections";
    private static final String TABLE_DISH_COLLECTIONS = "Dish_Collections";
    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String RECIPE = "recipe";
    private static final String AMOUNT = "amount";
    private static final String TYPE = "type";
    private static final String ID_DISH = "id_dish";
    private static final String ID_COLLECTION = "id_collection";


    public DB_Confic(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createDishTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DISH +
                " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT, " +
                RECIPE + " TEXT);";

        String createIngredientTable = "CREATE TABLE IF NOT EXISTS " + TABLE_INGREDIENT +
                " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT, " +
                AMOUNT + " TEXT, " +
                TYPE + " TEXT, " +
                ID_DISH + " INTEGER);";

        String createCollectionsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_COLLECTIONS +
                " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT);";

        String createDishCollectionsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DISH_COLLECTIONS +
                " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ID_DISH + " TEXT, " +
                ID_COLLECTION + " INTEGER);";

        sqLiteDatabase.execSQL(createDishTable);
        sqLiteDatabase.execSQL(createIngredientTable);
        sqLiteDatabase.execSQL(createCollectionsTable);
        sqLiteDatabase.execSQL(createDishCollectionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DISH);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENT);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTIONS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DISH_COLLECTIONS);
        onCreate(sqLiteDatabase);
    }
}
