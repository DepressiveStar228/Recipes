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
@org.robolectric.annotation.Config(manifest = Config.NONE)
public class IngredientDBTest {
    private RecipeDatabase db;
    private DishDAO dishDAO;
    private IngredientDAO ingredientDAO;
    private String stringData = "qwe";

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
    public void insertIngredientTest() {
        dishDAO.insert(new Dish(stringData, stringData));
        Ingredient ingredient = new Ingredient(stringData, stringData, stringData, 1);

        TestObserver<Long> testObserver = ingredientDAO.insert(ingredient).test();
        testObserver.assertComplete();
    }

    @Test
    public void updateIngredientTest() {
        insertIngredientTest();
        Ingredient ingredient = new Ingredient(stringData+1, stringData+1, stringData+1, 1);

        TestObserver<Void> testObserver = ingredientDAO.update(ingredient).test();
        testObserver.assertComplete();
    }

    @Test
    public void deleteIngredientTest() {
        insertIngredientTest();
        Ingredient ingredient = new Ingredient(stringData, stringData, stringData, 1);

        TestObserver<Void> testObserver = ingredientDAO.delete(ingredient).test();
        testObserver.assertComplete();
    }

    @Test
    public void getAllIngredientsTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> dishDAO.insert(new Dish(stringData, stringData)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+1, stringData+1, stringData+1, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+2, stringData+2, stringData+2, 2)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+3, stringData+3, stringData+3, 2)))
                .flatMap(id -> ingredientDAO.getAllIngredients())
                .test()
                .assertNoErrors()
                .assertValue(ingredients -> ingredients.size() == 4);
    }

    @Test
    public void getAllIngredientsByIdDishTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> dishDAO.insert(new Dish(stringData, stringData)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+1, stringData+1, stringData+1, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+2, stringData+2, stringData+2, 2)))
                .flatMap(id -> ingredientDAO.getAllIngredientsByIdDish(1))
                .test()
                .assertNoErrors()
                .assertValue(ingredients -> ingredients.size() == 2);
    }

    @Test
    public void getAllNameIngredientsOrderedTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+3, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+1, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+2, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.getAllNameIngredientsOrdered())
                .test()
                .assertNoErrors()
                .assertValue(ingredients -> {
                    return ingredients.get(0).equals(stringData + "1") &&
                            ingredients.get(1).equals(stringData + "2") &&
                            ingredients.get(2).equals(stringData + "3");
                });
    }

    @Test
    public void getAllIngredientsNameOrderedTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+3, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+1, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData+2, stringData, stringData, 1)))
                .flatMap(id -> ingredientDAO.getAllIngredientsNameOrdered())
                .test()
                .assertNoErrors()
                .assertValue(ingredients -> {
                    return ingredients.get(0).getName().equals(stringData + "1") &&
                            ingredients.get(1).getName().equals(stringData + "2") &&
                            ingredients.get(2).getName().equals(stringData + "3");
                });
    }

    @Test
    public void getIdDishesByNameTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> ingredientDAO.insert(new Ingredient(stringData, stringData, stringData, id)))
                .flatMap(id -> ingredientDAO.getIdDishesByName(stringData))
                .test()
                .assertNoErrors()
                .assertValue(ids -> ids.get(0).equals(1));
    }
}
