package com.example.recipes;


import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DishDBTest {
    private RecipeDatabase db;
    private DishDAO dishDAO;
    private IngredientDAO ingredientDAO;
    private String nameDish = "qwe";

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        dishDAO = db.dishDao();
        ingredientDAO = db.ingredientDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertDishTest() {
        Dish dish = new Dish(nameDish, nameDish);

        TestObserver<Long> testObserver = dishDAO.insert(dish).test();
        testObserver.assertComplete();
    }

    @Test
    public void updateDishTest() {
        insertDishTest();
        Dish dish = new Dish(nameDish+1, nameDish+1);

        TestObserver<Void> testObserver = dishDAO.update(dish).test();
        testObserver.assertComplete();
    }

    @Test
    public void deleteDishTest() {
        insertDishTest();
        Dish dish = new Dish(nameDish, nameDish);

        TestObserver<Void> testObserver = dishDAO.delete(dish).test();
        testObserver.assertComplete();
    }

    @Test
    public void getAllDishTest() {
        dishDAO.insert(new Dish(nameDish, nameDish))
                .flatMap(id -> dishDAO.insert(new Dish(nameDish+1, nameDish+1)))
                .flatMap(id -> dishDAO.getAllDishes())
                .test()
                .assertNoErrors()
                .assertValue(dishes -> dishes.size() == 2);
    }

    @Test
    public void getAllDishOrderedTest() {
        dishDAO.insert(new Dish(nameDish+3, nameDish+3))
                .flatMap(id -> dishDAO.insert(new Dish(nameDish+1, nameDish+1)))
                .flatMap(id -> dishDAO.insert(new Dish(nameDish+2, nameDish+2)))
                .flatMap(id -> dishDAO.getAllDishesOrdered())
                .test()
                .assertNoErrors()
                .assertValue(dishes -> {
                    return dishes.get(0).getName().equals(nameDish + "1") &&
                            dishes.get(1).getName().equals(nameDish + "2") &&
                            dishes.get(2).getName().equals(nameDish + "3");
                });
    }

    @Test
    public void getAllNameDishesTest() {
        dishDAO.insert(new Dish(nameDish+3, nameDish+3))
                .flatMap(id -> dishDAO.insert(new Dish(nameDish+1, nameDish+1)))
                .flatMap(id -> dishDAO.insert(new Dish(nameDish+2, nameDish+2)))
                .flatMap(id -> dishDAO.getAllNameDishes())
                .test()
                .assertNoErrors()
                .assertValue(names -> {
                    return names.contains(nameDish+1) && names.contains(nameDish+2) && names.contains(nameDish+3);
                });
    }

    @Test
    public void getDishByNameTest() {
        dishDAO.insert(new Dish(nameDish, nameDish))
                .flatMap(id -> dishDAO.getDishByName(nameDish).toSingle())
                .test()
                .assertNoErrors()
                .assertValue(d -> d.getName().equals(nameDish));
    }

    @Test
    public void getDishByIdTest() {
        dishDAO.insert(new Dish(nameDish, nameDish))
                .flatMap(id -> dishDAO.getDishById(id).toSingle())
                .test()
                .assertNoErrors()
                .assertValue(d -> d.getName().equals(nameDish));
    }

    @Test
    public void getIdByNameAndIdDishTest() {
        dishDAO.insert(new Dish(nameDish, nameDish))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(nameDish, nameDish, nameDish, id)))
                .flatMap(id -> ingredientDAO.getIdByNameAndIdDish(nameDish, id).toSingle())
                .test()
                .assertNoErrors()
                .assertValue(id -> id.equals(1));
    }
}
