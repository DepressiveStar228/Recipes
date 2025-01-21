package com.example.recipes.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Dish_Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;

import io.reactivex.rxjava3.annotations.NonNull;

@Database(
        version = 7,
        entities = {
                Dish.class,
                Ingredient.class,
                Collection.class,
                Dish_Collection.class,
                IngredientShopList.class
        },
        exportSchema = false
)
public abstract class RecipeDatabase extends RoomDatabase {
    public abstract DishDAO dishDao();
    public abstract IngredientDAO ingredientDao();
    public abstract CollectionDAO collectionDao();
    public abstract DishCollectionDAO dishCollectionDao();
    public abstract IngredientShopListDAO ingredientShopListDao();

    private static volatile RecipeDatabase INSTANCE;

    public static RecipeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RecipeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RecipeDatabase.class, "recipes")
                            //.addMigrations(MIGRATION5_7)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    static final Migration MIGRATION5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE collection ADD COLUMN type TEXT DEFAULT \"C\"");
        }
    };

    static final Migration MIGRATION5_7 = new Migration(5, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE collection ADD COLUMN type TEXT DEFAULT \"C\"");
            database.execSQL("CREATE TABLE IF NOT EXISTS ingredient_shop_list " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "amount TEXT, " +
                    "type TEXT, " +
                    "id_dish INTEGER NOT NULL, " +
                    "id_collection INTEGER NOT NULL, " +
                    "is_buy INTEGER NOT NULL DEFAULT 0)");
        }
    };

    static final Migration MIGRATION6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS ingredient_shop_list " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "amount TEXT, " +
                    "type TEXT, " +
                    "id_dish INTEGER NOT NULL, " +
                    "id_collection INTEGER NOT NULL, " +
                    "is_buy INTEGER NOT NULL DEFAULT 0)");
        }
    };
}

