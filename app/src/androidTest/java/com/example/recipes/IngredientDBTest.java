//package com.example.recipes;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import android.content.Context;
//
//import androidx.room.Room;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import com.example.recipes.Database.RecipeDatabase;
//import com.example.recipes.Item.Collection;
//import com.example.recipes.Item.Dish;
//import com.example.recipes.Item.Ingredient;
//import com.example.recipes.Utils.RecipeUtils;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.ArrayList;
//
//@RunWith(AndroidJUnit4.class)
//public class IngredientDBTest {
//    private RecipeUtils recipeUtils;
//    private ArrayList<Ingredient> ingredients_dish1 = new ArrayList<>();
//    private ArrayList<Ingredient> ingredients_dish2 = new ArrayList<>();
//
//    private long id_collection;
//    private String text_data = "Test";
//
//    @Before
//    public void setUp() {
//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//
//        RecipeDatabase testDatabase = Room.inMemoryDatabaseBuilder(appContext, RecipeDatabase.class)
//                .allowMainThreadQueries()
//                .build();
//
//        recipeUtils = new RecipeUtils(testDatabase);
//
////        recipeUtils.addCollection(new Collection(text_data, new ArrayList<>()))
////                .test()
////                .assertComplete()
////                .assertValue(id -> true);
//
//        id_collection = recipeUtils.getIdCollectionByName(text_data).blockingGet();
//
//        boolean box1 = recipeUtils.addDish(new Dish(text_data+"1", ""), id_collection).blockingGet();
//        boolean box2 = recipeUtils.addDish(new Dish(text_data+"2", ""), id_collection).blockingGet();
//
//        ingredients_dish1.add(new Ingredient(text_data+"2", "", ""));
//        ingredients_dish1.add(new Ingredient(text_data+"3", "", ""));
//        ingredients_dish1.add(new Ingredient(text_data+"1", "", ""));
//        ingredients_dish2.add(new Ingredient(text_data+"1", "", ""));
//    }
//
//    @Test
//    public void testInsertIngredient() {
//        assertTrue(insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish1));
//        assertTrue(insertIngredient(getIdDishByName(text_data+"2"), ingredients_dish2));
//    }
//
//    @Test
//    public void testUpdateIngredient() {
//        boolean box1 = insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish1);
//        ArrayList<Ingredient> ingredients_ = (ArrayList<Ingredient>) recipeUtils.getIngredients(1).blockingGet();
//
//        Ingredient updatingIngredient = ingredients_.get(0);
//        updatingIngredient.setName("Rename");
//
//        recipeUtils.updateIngredient(updatingIngredient)
//                .test()
//                .assertComplete()
//                .assertNoErrors();
//
//        ingredients_ = (ArrayList<Ingredient>) recipeUtils.getIngredients(1).blockingGet();
//
//        assertEquals("Rename", ingredients_.get(0).getName());
//    }
//
//    @Test
//    public void testDeleteIngredient() {
//        boolean box1 = insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish2);
//        ArrayList<Ingredient> ingredients_ = (ArrayList<Ingredient>) recipeUtils.getIngredients(1).blockingGet();
//
//        recipeUtils.deleteIngredient(ingredients_.get(0))
//                .test()
//                .assertComplete()
//                .assertNoErrors();
//
//        ingredients_ = (ArrayList<Ingredient>) recipeUtils.getIngredients(1).blockingGet();
//
//        assertEquals(0, ingredients_.size());
//    }
//
//    @Test
//    public void testGetAllIngredients() {
//        boolean box1 = insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish1);
//        boolean box2 = insertIngredient(getIdDishByName(text_data+"2"), ingredients_dish2);
//        ArrayList<Ingredient> ingredients_ = (ArrayList<Ingredient>) recipeUtils.getIngredients().blockingGet();
//
//        assertEquals(4, ingredients_.size());
//    }
//
//    @Test
//    public void testGetAllIngredientsByIdDish() {
//        boolean box1 = insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish1);
//        boolean box2 = insertIngredient(getIdDishByName(text_data+"2"), ingredients_dish2);
//
//        ArrayList<Ingredient> ingredients_ = (ArrayList<Ingredient>) recipeUtils.getIngredients(2).blockingGet();
//
//        assertEquals(1, ingredients_.size());
//        assertEquals(text_data+"1", ingredients_.get(0).getName());
//    }
//
//    @Test
//    public void testGetAllNameIngredientsOrdered() {
//        boolean box1 = insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish1);
//        boolean box2 = insertIngredient(getIdDishByName(text_data+"2"), ingredients_dish2);
//
//        ArrayList<Ingredient> ingredients_ = (ArrayList<Ingredient>) recipeUtils.getIngredientsOrdered().blockingGet();
//
//        assertEquals(4, ingredients_.size());
//        assertEquals(text_data+"1", ingredients_.get(0).getName());
//        assertEquals(text_data+"1", ingredients_.get(1).getName());
//        assertEquals(text_data+"2", ingredients_.get(2).getName());
//        assertEquals(text_data+"3", ingredients_.get(3).getName());
//    }
//
//    @Test
//    public void testGetIdDishesByName() {
//        boolean box1 = insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish1);
//        boolean box2 = insertIngredient(getIdDishByName(text_data+"2"), ingredients_dish2);
//
//        ArrayList<Long> id_dishes1 = (ArrayList<Long>) recipeUtils.getDishIdsByNameIngredient(text_data+"1").blockingGet();
//        ArrayList<Long> id_dishes2 = (ArrayList<Long>) recipeUtils.getDishIdsByNameIngredient(text_data+"2").blockingGet();
//
//        assertEquals(2, id_dishes1.size());
//        assertEquals(1, id_dishes2.size());
//        assertTrue(id_dishes1.contains(1L));
//        assertTrue(id_dishes1.contains(2L));
//        assertTrue(id_dishes2.contains(1L));
//    }
//
//    @Test
//    public void testGetIngredientCount() {
//        boolean box1 = insertIngredient(getIdDishByName(text_data+"1"), ingredients_dish1);
//        boolean box2 = insertIngredient(getIdDishByName(text_data+"2"), ingredients_dish2);
//
//        int count = recipeUtils.getIngredientCount().blockingGet();
//
//        assertEquals(4, count);
//    }
//
//
//
//
//
//
//
//
//    private Boolean insertIngredient(long id_dish, ArrayList<Ingredient> ingredients) {
//        return recipeUtils.addIngredients(id_dish, ingredients).blockingGet();
//    }
//
//    private Long getIdDishByName(String name) {
//        return recipeUtils.getIdDishByName(name).blockingGet();
//    }
//}
