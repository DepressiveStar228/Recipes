package com.example.recipes.DataBase.Dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.DishRecipeDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class DishRecipeDaoTest {
    private RecipeDatabase db;
    private DishRecipeDAO dishRecipeDAO;
    private DishDAO dishDao;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        dishRecipeDAO = db.dishRecipeDao();
        dishDao = db.dishDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insert() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishRecipe recipe = new DishRecipe(dishId, "Recipe", 1, DishRecipeType.TEXT);

        TestObserver<Long> testObserver = dishRecipeDAO.insert(recipe).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(1L);
    }

    @Test
    public void getByID() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishRecipe recipe = new DishRecipe(dishId, "Recipe", 1, DishRecipeType.TEXT);
        long recipeId = dishRecipeDAO.insert(recipe).blockingGet();
        recipe.setId(recipeId);

        TestObserver<DishRecipe> testObserver = dishRecipeDAO.getByID(recipeId).test();
        testObserver
                .assertNoErrors()
                .assertValue(recipe);
    }

    @Test
    public void update() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishRecipe recipe = new DishRecipe(dishId, "Recipe", 1, DishRecipeType.TEXT);
        long recipeId = dishRecipeDAO.insert(recipe).blockingGet();
        recipe.setId(recipeId);
        recipe.setTextData("Updated Recipe");
        recipe.setPosition(2);

        TestObserver<Void> testObserver = dishRecipeDAO.update(recipe).test();
        testObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<DishRecipe> getByIdObserver = dishRecipeDAO.getByID(recipeId).test();
        getByIdObserver
                .assertNoErrors()
                .assertValue(recipe);
    }

    @Test
    public void delete() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishRecipe recipe = new DishRecipe(dishId, "Recipe", 1, DishRecipeType.TEXT);
        long recipeId = dishRecipeDAO.insert(recipe).blockingGet();
        recipe.setId(recipeId);

        TestObserver<Void> testObserver = dishRecipeDAO.delete(recipe).test();
        testObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<DishRecipe> getByIdObserver = dishRecipeDAO.getByID(recipeId).test();
        getByIdObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void deleteCascadeWithDish() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();
        dish.setId(dishId);

        DishRecipe recipe = new DishRecipe(dishId, "Recipe", 1, DishRecipeType.TEXT);
        long recipeId = dishRecipeDAO.insert(recipe).blockingGet();
        recipe.setId(recipeId);

        TestObserver<Void> testObserver = dishDao.delete(dish).test();
        testObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<DishRecipe> getByIdObserver = dishRecipeDAO.getByID(recipeId).test();
        getByIdObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getAll() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishRecipe recipe1 = new DishRecipe(dishId, "Recipe 1", 1, DishRecipeType.TEXT);
        DishRecipe recipe2 = new DishRecipe(dishId, "Recipe 2", 2, DishRecipeType.TEXT);
        long id1 = dishRecipeDAO.insert(recipe1).blockingGet();
        long id2 = dishRecipeDAO.insert(recipe2).blockingGet();
        recipe1.setId(id1);
        recipe2.setId(id2);

        TestObserver<List<DishRecipe>> testObserver = dishRecipeDAO.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedRecipes -> loadedRecipes.size() == 2)
                .assertValue(loadedRecipes -> loadedRecipes.contains(recipe1) && loadedRecipes.contains(recipe2));
    }

    @Test
    public void getByDishID() {
        Dish dish1 = new Dish("Test Dish 1", 1);
        Dish dish2 = new Dish("Test Dish 2", 1);
        long dishId1 = dishDao.insert(dish1).blockingGet();
        long dishId2 = dishDao.insert(dish2).blockingGet();
        dish1.setId(dishId1);
        dish2.setId(dishId2);

        DishRecipe recipe1 = new DishRecipe(dishId1, "Recipe 1", 1, DishRecipeType.TEXT);
        DishRecipe recipe2 = new DishRecipe(dishId2, "Recipe 2", 2, DishRecipeType.TEXT);
        long id1 = dishRecipeDAO.insert(recipe1).blockingGet();
        long id2 = dishRecipeDAO.insert(recipe2).blockingGet();
        recipe1.setId(id1);
        recipe2.setId(id2);

        TestObserver<List<DishRecipe>> testObserver = dishRecipeDAO.getByDishID(dishId1).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedRecipes -> loadedRecipes.size() == 1)
                .assertValue(loadedRecipes -> loadedRecipes.contains(recipe1));
    }

    @Test
    public void getCount() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishRecipe recipe1 = new DishRecipe(dishId, "Recipe 1", 1, DishRecipeType.TEXT);
        DishRecipe recipe2 = new DishRecipe(dishId, "Recipe 2", 2, DishRecipeType.TEXT);
        dishRecipeDAO.insert(recipe1).blockingGet();
        dishRecipeDAO.insert(recipe2).blockingGet();

        TestObserver<Integer> testObserver = dishRecipeDAO.getCount().test();
        testObserver
                .assertNoErrors()
                .assertValue(2);
    }
}
