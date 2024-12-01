package com.example.recipes;

import android.content.Context;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DishDBTest {
    private RecipeUtils recipeUtils;
    private ArrayList<Dish> dishes = new ArrayList<>();
    private long id_collection;
    private String text_data = "Test";

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        RecipeDatabase testDatabase = Room.inMemoryDatabaseBuilder(appContext, RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();

        recipeUtils = new RecipeUtils(testDatabase);
        dishes.add(new Dish(1,text_data+"1", ""));
        dishes.add(new Dish(2,text_data+"2", ""));
        dishes.add(new Dish(3,text_data+"3", ""));

        recipeUtils.addCollection(new Collection(text_data, new ArrayList<>()))
                .test()
                .assertComplete()
                .assertValue(id -> true);

        id_collection = recipeUtils.getIdCollectionByName(text_data).blockingGet();
    }

    @Test
    public void testInsertDish() {
        assertTrue(insertDish(dishes.get(0), id_collection));
    }

    @Test
    public void testUpdateDish() {
        boolean box = insertDish(dishes.get(0), id_collection);
        Dish updatingDish = dishes.get(0);
        updatingDish.setName("Rename");

        recipeUtils.updateDish(updatingDish)
                .test()
                .assertComplete()
                .assertNoErrors();

        Dish updatedDish = getDishByName(dishes.get(0).getName());
        assertEquals("Rename", updatedDish.getName());
    }

    @Test
    public void testDeleteDish() {
        boolean box = insertDish(dishes.get(0), id_collection);

        recipeUtils.deleteDish(dishes.get(0))
                .test()
                .assertComplete()
                .assertNoErrors();

        Long id = getIdDishByName(dishes.get(0).getName());
        assertEquals(Long.valueOf(-1), id);
    }

    @Test
    public void testGetAllDishes() {
        boolean box1 = insertDish(dishes.get(0), id_collection);
        boolean box2 = insertDish(dishes.get(1), id_collection);
        boolean box3 = insertDish(dishes.get(2), id_collection);

        List<Dish> dishes_ = recipeUtils.getAllDishes().blockingGet();

        assertEquals(3 , dishes_.size());
    }

    @Test
    public void testGetDishesOrdered() {
        boolean box1 = insertDish(dishes.get(0), id_collection);
        boolean box2 = insertDish(dishes.get(2), id_collection);
        boolean box3 = insertDish(dishes.get(1), id_collection);

        List<Dish> dishes_ = recipeUtils.getAllDishes().blockingGet();

        assertEquals(3 , dishes_.size());
        assertEquals(dishes.get(0).getName(), dishes_.get(0).getName());
        assertEquals(dishes.get(1).getName(), dishes_.get(1).getName());
        assertEquals(dishes.get(2).getName(), dishes_.get(2).getName());
    }

    @Test
    public void testGetDishByName() {
        boolean box = insertDish(dishes.get(0), id_collection);
        Dish dish = getDishByName(dishes.get(0).getName());
        assertEquals(dish , dishes.get(0));
    }


    @Test
    public void testGetIdDishByName() {
        boolean box = insertDish(dishes.get(0), id_collection);
        Long id = getIdDishByName(dishes.get(0).getName());
        assertEquals(Long.valueOf(1) , id);
    }

    @Test
    public void testGetDish() {
        boolean box = insertDish(dishes.get(0), id_collection);
        Long id = getIdDishByName(dishes.get(0).getName());
        Dish dish = getDish(id);
        assertEquals(dish , dishes.get(0));
    }


    @Test
    public void testGetDishCount() {
        boolean box1 = insertDish(dishes.get(0), id_collection);
        boolean box2 = insertDish(dishes.get(1), id_collection);
        boolean box3 = insertDish(dishes.get(2), id_collection);

        int numb = recipeUtils.getDishCount().blockingGet();

        assertEquals(3 , numb);
    }







    private Boolean insertDish(Dish dish, long id_collection) {
        return recipeUtils.addDish(dish, id_collection).blockingGet();
    }

    private Dish getDish(long id) {
        return recipeUtils.getDish(id).blockingGet();
    }

    private Long getIdDishByName(String name) {
        return recipeUtils.getIdDishByName(name).blockingGet();
    }

    private Dish getDishByName(String name) {
        return recipeUtils.getDishByName(name).blockingGet();
    }
}