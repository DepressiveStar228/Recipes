package com.example.recipes.DataBase.Dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Item.Dish;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class DishDaoTest {
    private RecipeDatabase db;
    private DishDAO dishDao;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        dishDao = db.dishDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insert() {
        Dish dish = new Dish("Test Dish", 1);

        TestObserver<Long> testObserver = dishDao.insert(dish).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(1L);
    }

    @Test
    public void getById() {
        Dish dish = new Dish("Test Dish", 1);
        long id = dishDao.insert(dish).blockingGet();
        dish.setId(id);

        TestObserver<Dish> testObserver = dishDao.getByID(id).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(dish);
    }

    @Test
    public void update() {
        Dish dish = new Dish("Test Dish", 1);
        long id = dishDao.insert(dish).blockingGet();

        dish.setId(id);
        dish.setName("Updated Dish");
        dish.setPortion(2);

        TestObserver<Void> updateObserver = dishDao.update(dish).test();
        updateObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertComplete();

        TestObserver<Dish> getDishObserver = dishDao.getByID(id).test();
        getDishObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(dish);
    }

    @Test
    public void delete() {
        Dish dish = new Dish("Test Dish", 1);
        long id = dishDao.insert(dish).blockingGet();
        dish.setId(id);

        TestObserver<Void> deleteObserver = dishDao.delete(dish).test();
        deleteObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertComplete();

        TestObserver<Dish> getDishObserver = dishDao.getByID(id).test();
        getDishObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getAll() {
        Dish dish1 = new Dish("Test Dish 1", 1);
        Dish dish2 = new Dish("Test Dish 2", 2);
        long id1 = dishDao.insert(dish1).blockingGet();
        long id2 = dishDao.insert(dish2).blockingGet();
        dish1.setId(id1);
        dish2.setId(id2);

        TestObserver<List<Dish>> testObserver = dishDao.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 2)
                .assertValue(loadedDishes -> loadedDishes.contains(dish1) && loadedDishes.contains(dish2));
    }

    @Test
    public void getAllIDs() {
        Dish dish1 = new Dish("Test Dish 1", 1);
        Dish dish2 = new Dish("Test Dish 2", 2);
        long id1 = dishDao.insert(dish1).blockingGet();
        long id2 = dishDao.insert(dish2).blockingGet();

        TestObserver<List<Long>> testObserver = dishDao.getAllIDs().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedIDs -> loadedIDs.size() == 2)
                .assertValue(loadedIDs -> loadedIDs.get(0).equals(id1) && loadedIDs.get(1).equals(id2));
    }

    @Test
    public void getByName() {
        Dish dish = new Dish("Test Dish", 1);
        long id = dishDao.insert(dish).blockingGet();
        dish.setId(id);

        TestObserver<Dish> testObserver = dishDao.getByName("Test Dish").test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(dish);
    }

    @Test
    public void getIDByName() {
        Dish dish = new Dish("Test Dish", 1);
        long id = dishDao.insert(dish).blockingGet();

        TestObserver<Long> testObserver = dishDao.getIDByName("Test Dish").test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(loadedId -> loadedId.equals(id));
    }

    @Test
    public void getByNameNotFound() {
        Dish dish = new Dish("Test Dish", 1);
        dishDao.insert(dish).blockingGet();

        TestObserver<Dish> testObserver = dishDao.getByName("Nonexistent Dish").test();
        testObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getCount() {
        Dish dish1 = new Dish("Test Dish 1", 1);
        Dish dish2 = new Dish("Test Dish 2", 2);
        dishDao.insert(dish1).blockingGet();
        dishDao.insert(dish2).blockingGet();

        TestObserver<Integer> testObserver = dishDao.getCount().test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(count -> count == 2);
    }
}
