package com.example.recipes;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Item.Collection;

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
public class CollectionDBTest {
    private RecipeDatabase db;
    private CollectionDAO collectionDAO;
    private String stringData = "qwe";

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        collectionDAO = db.collectionDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertCollectionTest() {
        Collection collection = new Collection(stringData);

        TestObserver<Long> testObserver = collectionDAO.insert(collection).test();
        testObserver.assertComplete();
    }

    @Test
    public void updateCollectionTest() {
        insertCollectionTest();
        Collection collection = new Collection(stringData+1);

        TestObserver<Void> testObserver = collectionDAO.update(collection).test();
        testObserver.assertComplete();
    }

    @Test
    public void deleteCollectionTest() {
        insertCollectionTest();
        Collection collection = new Collection(stringData);

        TestObserver<Void> testObserver = collectionDAO.delete(collection).test();
        testObserver.assertComplete();
    }

    @Test
    public void getAllCollectionsTest() {
        collectionDAO.insert(new Collection(stringData))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData)))
                .flatMap(id -> collectionDAO.getAllCollections())
                .test()
                .assertNoErrors()
                .assertValue(collections -> collections.size() == 2);
    }


    @Test
    public void getAllNameCollectionsTest() {
        collectionDAO.insert(new Collection(stringData+1))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData+2)))
                .flatMap(id -> collectionDAO.insert(new Collection(stringData+3)))
                .flatMap(id -> collectionDAO.getAllNameCollections())
                .test()
                .assertNoErrors()
                .assertValue(names -> {
                    return names.contains(stringData+1) && names.contains(stringData+2) && names.contains(stringData+3);
                });
    }

    @Test
    public void getCollectionByIdTest() {
        collectionDAO.insert(new Collection(stringData))
                .flatMap(id -> collectionDAO.getCollectionById(id).toSingle())
                .test()
                .assertNoErrors()
                .assertValue(collection -> collection.getName().equals(stringData));
    }

    @Test
    public void getIdByNameTest() {
        collectionDAO.insert(new Collection(stringData))
                .flatMap(id -> collectionDAO.getIdByName(stringData).toSingle())
                .test()
                .assertNoErrors()
                .assertValue(id -> id.equals(1));
    }
}
