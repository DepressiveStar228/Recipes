package com.example.recipes.DataBase.Dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class IngredientDaoTest {
    private RecipeDatabase db;
    private DishDAO dishDao;
    private IngredientDAO ingredientDao;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        dishDao = db.dishDao();
        ingredientDao = db.ingredientDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insert() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        Ingredient ingredient = new Ingredient("Test Ingredient", "10", IngredientType.VOID, dishId);

        TestObserver<Long> insertObserver = ingredientDao.insert(ingredient).test();
        insertObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(1L);
    }

    @Test
    public void getById() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        Ingredient ingredient = new Ingredient("Test Ingredient", "10", IngredientType.VOID, dishId);
        long ingredientId = ingredientDao.insert(ingredient).blockingGet();
        ingredient.setId(ingredientId);

        TestObserver<Ingredient> getObserver = ingredientDao.getByID(ingredientId).test();
        getObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(ingredient);
    }

    @Test
    public void update() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        Ingredient ingredient = new Ingredient("Test Ingredient", "10", IngredientType.VOID, dishId);
        long ingredientId = ingredientDao.insert(ingredient).blockingGet();

        ingredient.setId(ingredientId);
        ingredient.setName("Updated Ingredient");
        ingredient.setAmount("20");

        TestObserver<Void> updateObserver = ingredientDao.update(ingredient).test();
        updateObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<Ingredient> getObserver = ingredientDao.getByID(ingredientId).test();
        getObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(ingredient);
    }

    @Test
    public void delete() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        Ingredient ingredient = new Ingredient("Test Ingredient", "10", IngredientType.VOID, dishId);
        long ingredientId = ingredientDao.insert(ingredient).blockingGet();
        ingredient.setId(ingredientId);

        TestObserver<Void> deleteObserver = ingredientDao.delete(ingredient).test();
        deleteObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<Ingredient> getObserver = ingredientDao.getByID(ingredientId).test();
        getObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void deleteCascadeWithDish() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();
        dish.setId(dishId);

        Ingredient ingredient = new Ingredient("Test Ingredient", "10", IngredientType.VOID, dishId);
        long ingredientId = ingredientDao.insert(ingredient).blockingGet();

        TestObserver<Void> deleteObserver = dishDao.delete(dish).test();
        deleteObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<Ingredient> getObserver = ingredientDao.getByID(ingredientId).test();
        getObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getAll() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        Ingredient ingredient1 = new Ingredient("Test Ingredient 1", "10", IngredientType.VOID, dishId);
        Ingredient ingredient2 = new Ingredient("Test Ingredient 2", "20", IngredientType.VOID, dishId);
        long id1 = ingredientDao.insert(ingredient1).blockingGet();
        long id2 = ingredientDao.insert(ingredient2).blockingGet();
        ingredient1.setId(id1);
        ingredient2.setId(id2);

        TestObserver<List<Ingredient>> getAllObserver = ingredientDao.getAll().test();
        getAllObserver
                .assertNoErrors()
                .assertValue(loadedIngredients -> loadedIngredients.size() == 2)
                .assertValue(loadedIngredients -> loadedIngredients.contains(ingredient1) && loadedIngredients.contains(ingredient2));
    }

    @Test
    public void getAllByDishId() {
        Dish dish1 = new Dish("Test Dish 1", 1);
        Dish dish2 = new Dish("Test Dish 2", 2);
        long dishId1 = dishDao.insert(dish1).blockingGet();
        long dishId2 = dishDao.insert(dish2).blockingGet();

        Ingredient ingredient1 = new Ingredient("Test Ingredient 1", "10", IngredientType.VOID, dishId1);
        Ingredient ingredient2 = new Ingredient("Test Ingredient 2", "20", IngredientType.VOID, dishId2);
        Ingredient ingredient3 = new Ingredient("Test Ingredient 3", "30", IngredientType.VOID, dishId1);
        long id1 = ingredientDao.insert(ingredient1).blockingGet();
        long id2 = ingredientDao.insert(ingredient2).blockingGet();
        long id3 = ingredientDao.insert(ingredient3).blockingGet();
        ingredient1.setId(id1);
        ingredient2.setId(id2);
        ingredient3.setId(id3);

        TestObserver<List<Ingredient>> getAllObserver = ingredientDao.getAllByIDDish(dishId1).test();
        getAllObserver
                .assertNoErrors()
                .assertValue(loadedIngredients -> loadedIngredients.size() == 2)
                .assertValue(loadedIngredients -> loadedIngredients.contains(ingredient1) && loadedIngredients.contains(ingredient3));
    }

    @Test
    public void getNamesUnique() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        Ingredient ingredient1 = new Ingredient("Test Ingredient 1", "10", IngredientType.VOID, dishId);
        Ingredient ingredient2 = new Ingredient("Test Ingredient 1", "20", IngredientType.VOID, dishId);
        Ingredient ingredient3 = new Ingredient("Test Ingredient 2", "30", IngredientType.VOID, dishId);
        ingredientDao.insert(ingredient1).blockingGet();
        ingredientDao.insert(ingredient2).blockingGet();
        ingredientDao.insert(ingredient3).blockingGet();

        TestObserver<List<String>> getNamesObserver = ingredientDao.getNamesUnique().test();
        getNamesObserver
                .assertNoErrors()
                .assertValue(names -> names.size() == 2)
                .assertValue(names -> names.contains("Test Ingredient 1") && names.contains("Test Ingredient 2"));
    }

    @Test
    public void getCount() {
        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        Ingredient ingredient1 = new Ingredient("Test Ingredient 1", "10", IngredientType.VOID, dishId);
        Ingredient ingredient2 = new Ingredient("Test Ingredient 2", "20", IngredientType.VOID, dishId);
        ingredientDao.insert(ingredient1).blockingGet();
        ingredientDao.insert(ingredient2).blockingGet();

        TestObserver<Integer> getCountObserver = ingredientDao.getCount().test();
        getCountObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(count -> count == 2);
    }
}
