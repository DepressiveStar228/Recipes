package com.example.recipes;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Dish_Collection;

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
public class DishCollectionDBTest {
    private RecipeDatabase db;
    private DishDAO dishDAO;
    private CollectionDAO collectionDAO;
    private DishCollectionDAO dishCollectionDAO;
    private String stringData = "qwe";

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        dishDAO = db.dishDao();
        collectionDAO = db.collectionDao();
        dishCollectionDAO = db.dishCollectionDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertDishCollectionTest() {
        long dishId = dishDAO.insert(new Dish(stringData, stringData)).blockingGet();
        long collectionId = collectionDAO.insert(new Collection(stringData)).blockingGet();
        Dish_Collection dish_collection = new Dish_Collection(dishId, collectionId);

        TestObserver<Long> testObserver = dishCollectionDAO.insert(dish_collection).test();
        testObserver.assertComplete();
    }

    @Test
    public void updateDishCollectionTest() {
        insertDishCollectionTest();
        insertDishCollectionTest();
        Dish_Collection dish_collection = new Dish_Collection(2, 2);

        TestObserver<Void> testObserver = dishCollectionDAO.update(dish_collection).test();
        testObserver.assertComplete();
    }

    @Test
    public void deleteDishCollectionTest() {
        insertDishCollectionTest();
        Dish_Collection dish_collection = new Dish_Collection(1, 1);

        TestObserver<Void> testObserver = dishCollectionDAO.delete(dish_collection).test();
        testObserver.assertComplete();
    }

    @Test
    public void getAllIdsDishByIdCollectionTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> dishDAO.insert(new Dish(stringData+1, stringData+1)))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData)))
                .flatMap(id -> dishCollectionDAO.insert(new Dish_Collection(1, 1)))
                .flatMap(id -> dishCollectionDAO.insert(new Dish_Collection(2, 1)))
                .flatMap(id -> dishCollectionDAO.getAllIdsDishByIdCollection(1))
                .test()
                .assertNoErrors()
                .assertValue(dishCollections -> dishCollections.size() == 2);
    }

    @Test
    public void getAllIdsCollectionByIdDishTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData)))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData+1)))
                .flatMap(id -> dishCollectionDAO.insert(new Dish_Collection(1, 1)))
                .flatMap(id -> dishCollectionDAO.insert(new Dish_Collection(1, 2)))
                .flatMap(id -> dishCollectionDAO.getAllIdsCollectionByIdDish(1))
                .test()
                .assertNoErrors()
                .assertValue(dishCollections -> dishCollections.size() == 2);
    }

//    @Test
//    public void getDishCollectionsByIdDishAndIdCollectionTest() {
//        dishDAO.insert(new Dish(stringData, stringData))
//                .flatMap(id -> collectionDAO.insert(new Collection(stringData)))
//                .flatMap(id -> dishCollectionDAO.insert(new Dish_Collection(1, 1)))
//                .flatMap(id -> dishCollectionDAO.getDishCollectionsByIdDishAndIdCollection(1, 1))
//                .test()
//                .assertNoErrors()
//                .assertValue(dishCollection -> {
//                    return dishCollection.getId_dish() == 1 &&
//                            dishCollection.getId_collection() == 1;
//                });
//    }

    @Test
    public void getDishCollectionsByIdDishTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData)))
                .flatMap(id -> dishCollectionDAO.insert(new Dish_Collection(1, 1)))
                .flatMap(id -> dishCollectionDAO.getDishCollectionsByIdDish(1))
                .test()
                .assertNoErrors()
                .assertValue(dishCollection -> {
                    return dishCollection.get(0).getId_dish() == 1 &&
                            dishCollection.get(0).getId_collection() == 1;
                });
    }

    @Test
    public void getDishCollectionsByIdCollectionTest() {
        dishDAO.insert(new Dish(stringData, stringData))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData)))
                .flatMap(id -> dishCollectionDAO.insert(new Dish_Collection(1, 1)))
                .flatMap(id -> dishCollectionDAO.getDishCollectionsByIdCollection(1))
                .test()
                .assertNoErrors()
                .assertValue(dishCollection -> {
                    return dishCollection.get(0).getId_dish() == 1 &&
                            dishCollection.get(0).getId_collection() == 1;
                });
    }
}
