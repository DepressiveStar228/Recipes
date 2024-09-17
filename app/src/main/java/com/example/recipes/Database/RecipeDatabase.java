package com.example.recipes.Database;

import android.content.Context;
import android.util.Log;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Dish_Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.annotations.NonNull;

@Database(
        version = 4,
        entities = {
                Dish.class,
                Ingredient.class,
                Collection.class,
                Dish_Collection.class
        },
        exportSchema = false
)
public abstract class RecipeDatabase extends RoomDatabase {
    public abstract DishDAO dishDao();
    public abstract IngredientDAO ingredientDao();
    public abstract CollectionDAO collectionDao();
    public abstract DishCollectionDAO dishCollectionDao();

    private static volatile RecipeDatabase INSTANCE;

    public static RecipeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RecipeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RecipeDatabase.class, "recipes")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}

