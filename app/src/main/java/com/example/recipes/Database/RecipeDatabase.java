package com.example.recipes.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.DishRecipeDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Database.DAO.IngredientShopList_AmountTypeDAO;
import com.example.recipes.Database.TypeConverter.CollectionTypeConverter;
import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Dish_Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.IngredientShopList_AmountType;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Головний клас бази даних, який використовує Room для зберігання даних про страви, рецепти, інгредієнти, колекції тощо.
 * Визначає всі сутності (таблиці) та DAO для роботи з ними.
 */
@Database(
        version = 2,
        entities = {Dish.class, DishRecipe.class, Ingredient.class, Collection.class, Dish_Collection.class, IngredientShopList.class, IngredientShopList_AmountType.class},
        exportSchema = false
)
@TypeConverters({CollectionTypeConverter.class, IngredientTypeConverter.class})
public abstract class RecipeDatabase extends RoomDatabase {
    public abstract DishDAO dishDao();
    public abstract DishRecipeDAO dishRecipeDao();
    public abstract IngredientDAO ingredientDao();
    public abstract CollectionDAO collectionDao();
    public abstract DishCollectionDAO dishCollectionDao();
    public abstract IngredientShopListDAO ingredientShopListDao();
    public abstract IngredientShopList_AmountTypeDAO ingredientShopList_amountTypeDao();

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

