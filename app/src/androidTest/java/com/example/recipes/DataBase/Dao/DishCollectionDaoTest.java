package com.example.recipes.DataBase.Dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class DishCollectionDaoTest {
    private RecipeDatabase db;
    private DishDAO dishDao;
    private CollectionDAO collectionDao;
    private DishCollectionDAO dishCollectionDao;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        dishDao = db.dishDao();
        collectionDao = db.collectionDao();
        dishCollectionDao = db.dishCollectionDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insert() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId);

        TestObserver<Long> testObserver = dishCollectionDao.insert(dishCollection).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);
    }

    @Test
    public void getById() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<DishCollection> testObserver = dishCollectionDao.getByID(dishCollectionId).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(dishCollection);
    }

    @Test
    public void update() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        Dish newDish = new Dish("Test Dish", 1);
        long newDishId = dishDao.insert(newDish).blockingGet();
        dishCollection.setIdDish(newDishId);

        TestObserver<Void> updateObserver = dishCollectionDao.update(dishCollection).test();
        updateObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<DishCollection> testObserver = dishCollectionDao.getByID(dishCollectionId).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(dishCollection);
    }

    @Test
    public void delete() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<Void> deleteObserver = dishCollectionDao.delete(dishCollection).test();
        deleteObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<DishCollection> testObserver = dishCollectionDao.getByID(dishCollectionId).test();
        testObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void deleteCascadeWithDish() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();
        dish.setId(dishId);

        DishCollection dishCollection = new DishCollection(dishId, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<Void> deleteObserver = dishDao.delete(dish).test();
        deleteObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<DishCollection> testObserver = dishCollectionDao.getByID(dishCollectionId).test();
        testObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getByIDDishAndIDCollection() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<DishCollection> testObserver = dishCollectionDao.getByIDDishAndIDCollection(dishId, collectionId).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(dishCollection);
    }

    @Test
    public void getByIDDish() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<List<DishCollection>> testObserver = dishCollectionDao.getByIDDish(dishId).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedDishCollections -> loadedDishCollections.size() == 1)
                .assertValue(loadedDishCollections -> loadedDishCollections.contains(dishCollection));
    }

    @Test
    public void getAllIDsDishByIDCollection() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish1 = new Dish("Test Dish 1", 1);
        Dish dish2 = new Dish("Test Dish 2", 1);
        long dishId1 = dishDao.insert(dish1).blockingGet();
        long dishId2 = dishDao.insert(dish2).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId1, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<List<Long>> testObserver = dishCollectionDao.getAllIDsDishByIDCollection(collectionId).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedDishIds -> loadedDishIds.size() == 1)
                .assertValue(loadedDishIds -> loadedDishIds.contains(dishId1));
    }

    @Test
    public void getAllIDsCollectionByIDDish() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection 2", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId1 = collectionDao.insert(collection1).blockingGet();
        long collectionId2 = collectionDao.insert(collection2).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId1);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<List<Long>> testObserver = dishCollectionDao.getAllIDsCollectionByIDDish(dishId).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollectionIds -> loadedCollectionIds.size() == 1)
                .assertValue(loadedCollectionIds -> loadedCollectionIds.contains(collectionId1));
    }

    @Test
    public void getCount() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long collectionId = collectionDao.insert(collection).blockingGet();

        Dish dish = new Dish("Test Dish", 1);
        long dishId = dishDao.insert(dish).blockingGet();

        DishCollection dishCollection = new DishCollection(dishId, collectionId);
        long dishCollectionId = dishCollectionDao.insert(dishCollection).blockingGet();
        dishCollection.setId(dishCollectionId);

        TestObserver<Integer> testObserver = dishCollectionDao.getCount().test();
        testObserver
                .assertNoErrors()
                .assertValue(count -> count == 1);
    }
}
