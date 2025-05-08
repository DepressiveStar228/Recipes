package com.example.recipes.DataBase.Dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Item.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class CollectionDaoTest {
    private RecipeDatabase db;
    private CollectionDAO collectionDAO;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        collectionDAO = db.collectionDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insert() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());

        TestObserver<Long> testObserver = collectionDAO.insert(collection).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);
    }

    @Test
    public void getById() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long id = collectionDAO.insert(collection).blockingGet();
        collection.setId(id);

        TestObserver<Collection> testObserver = collectionDAO.getByID(id).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(collection);
    }

    @Test
    public void update() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long id = collectionDAO.insert(collection).blockingGet();
        collection.setId(id);
        collection.setName("Updated Collection");

        TestObserver<Void> updateObserver = collectionDAO.update(collection).test();
        updateObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<Collection> getObserver = collectionDAO.getByID(id).test();
        getObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(collection);
    }

    @Test
    public void delete() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long id = collectionDAO.insert(collection).blockingGet();
        collection.setId(id);

        TestObserver<Void> deleteObserver = collectionDAO.delete(collection).test();
        deleteObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<Collection> getObserver = collectionDAO.getByID(id).test();
        getObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getAll() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection 2", CollectionType.COLLECTION, new ArrayList<>());
        long id1 = collectionDAO.insert(collection1).blockingGet();
        long id2 = collectionDAO.insert(collection2).blockingGet();
        collection1.setId(id1);
        collection2.setId(id2);

        TestObserver<List<Collection>> testObserver = collectionDAO.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollection -> loadedCollection.size() == 2)
                .assertValue(loadedCollection -> loadedCollection.contains(collection1) && loadedCollection.contains(collection2));
    }

    @Test
    public void getAllNames() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection 2", CollectionType.COLLECTION, new ArrayList<>());
        collectionDAO.insert(collection1).blockingGet();
        collectionDAO.insert(collection2).blockingGet();

        TestObserver<List<String>> testObserver = collectionDAO.getAllName().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollection -> loadedCollection.size() == 2)
                .assertValue(loadedCollection -> loadedCollection.contains("Test Collection 1") && loadedCollection.contains("Test Collection 2"));
    }

    @Test
    public void getAllNamesByType() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection 2", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection3 = new Collection("Test Collection 3", CollectionType.SHOP_LIST, new ArrayList<>());
        collectionDAO.insert(collection1).blockingGet();
        collectionDAO.insert(collection2).blockingGet();
        collectionDAO.insert(collection3).blockingGet();

        TestObserver<List<String>> testObserver = collectionDAO.getAllNameByType(CollectionType.SHOP_LIST).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollection -> loadedCollection.size() == 1)
                .assertValue(loadedCollection -> loadedCollection.contains("Test Collection 3"));
    }

    @Test
    public void getAllByType() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection 2", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection3 = new Collection("Test Collection 3", CollectionType.SHOP_LIST, new ArrayList<>());
        long id1 = collectionDAO.insert(collection1).blockingGet();
        long id2 = collectionDAO.insert(collection2).blockingGet();
        long id3 = collectionDAO.insert(collection3).blockingGet();
        collection1.setId(id1);
        collection2.setId(id2);
        collection3.setId(id3);

        TestObserver<List<Collection>> testObserver = collectionDAO.getAllByType(CollectionType.COLLECTION).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollection -> loadedCollection.size() == 2)
                .assertValue(loadedCollection -> loadedCollection.contains(collection1) && loadedCollection.contains(collection2));
    }

    @Test
    public void getByName() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        long id = collectionDAO.insert(collection).blockingGet();
        collection.setId(id);

        TestObserver<Collection> testObserver = collectionDAO.getByName("Test Collection").test();
        testObserver
                .assertNoErrors()
                .assertValue(collection);
    }

    @Test
    public void getIdByNameAndType() {
        Collection collection1 = new Collection("Test Collection", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection", CollectionType.SHOP_LIST, new ArrayList<>());
        long id1 = collectionDAO.insert(collection1).blockingGet();
        long id2 = collectionDAO.insert(collection2).blockingGet();

        TestObserver<Long> testObserver = collectionDAO.getIDByNameAndType("Test Collection", CollectionType.COLLECTION).test();
        testObserver
                .assertNoErrors()
                .assertValue(id1);
    }

    @Test
    public void getCount() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection 2", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection3 = new Collection("Test Collection 3", CollectionType.SHOP_LIST, new ArrayList<>());
        collectionDAO.insert(collection1).blockingGet();
        collectionDAO.insert(collection2).blockingGet();
        collectionDAO.insert(collection3).blockingGet();

        TestObserver<Integer> testObserver = collectionDAO.getCount().test();
        testObserver
                .assertNoErrors()
                .assertValue(count -> count == 3);
    }

    @Test
    public void getCountByType() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection("Test Collection 2", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection3 = new Collection("Test Collection 3", CollectionType.SHOP_LIST, new ArrayList<>());
        collectionDAO.insert(collection1).blockingGet();
        collectionDAO.insert(collection2).blockingGet();
        collectionDAO.insert(collection3).blockingGet();

        TestObserver<Integer> testObserver = collectionDAO.getCountByType(CollectionType.COLLECTION).test();
        testObserver
                .assertNoErrors()
                .assertValue(count -> count == 2);
    }
}
