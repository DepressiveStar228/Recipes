package com.example.recipes.DataBase.Dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.IngredientShopList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class IngredientShopListDaoTest {
    private RecipeDatabase db;
    private IngredientShopListDAO ingredientShopListDAO;
    private CollectionDAO collectionDAO;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        ingredientShopListDAO = db.ingredientShopListDao();
        collectionDAO = db.collectionDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void insert() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", "Amount", IngredientType.VOID, collectionId);

        TestObserver<Long> testObserver = ingredientShopListDAO.insert(ingredient).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(1L);
    }

    @Test
    public void getByID() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", collectionId);
        long id = ingredientShopListDAO.insert(ingredient).blockingGet();
        ingredient.setId(id);

        TestObserver<IngredientShopList> testObserver = ingredientShopListDAO.getByID(id).test();
        testObserver
                .assertNoErrors()
                .assertValue(ingredient);

        TestObserver<IngredientShopList> testObserver2 = ingredientShopListDAO.getByID(2L).test();
        testObserver2
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void update() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", collectionId);
        long id = ingredientShopListDAO.insert(ingredient).blockingGet();
        ingredient.setId(id);
        ingredient.setName("Updated Ingredient");

        TestObserver<Void> testObserver = ingredientShopListDAO.update(ingredient).test();
        testObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<IngredientShopList> testObserverGet = ingredientShopListDAO.getByID(id).test();
        testObserverGet
                .assertNoErrors()
                .assertValue(ingredient);
    }

    @Test
    public void delete() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", collectionId);
        long id = ingredientShopListDAO.insert(ingredient).blockingGet();
        ingredient.setId(id);

        TestObserver<Void> testObserver = ingredientShopListDAO.delete(ingredient).test();
        testObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<IngredientShopList> testObserverGet = ingredientShopListDAO.getByID(id).test();
        testObserverGet
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void deleteCascadeWithShopList() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();
        collection.setId(collectionId);

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", collectionId);
        long id = ingredientShopListDAO.insert(ingredient).blockingGet();
        ingredient.setId(id);

        TestObserver<Void> testObserver = collectionDAO.delete(collection).test();
        testObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<IngredientShopList> testObserverGet = ingredientShopListDAO.getByID(id).test();
        testObserverGet
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getAll() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient1 = new IngredientShopList("Test Ingredient 1", collectionId);
        IngredientShopList ingredient2 = new IngredientShopList("Test Ingredient 2", collectionId);
        long id1 = ingredientShopListDAO.insert(ingredient1).blockingGet();
        long id2 = ingredientShopListDAO.insert(ingredient2).blockingGet();
        ingredient1.setId(id1);
        ingredient2.setId(id2);

        TestObserver<List<IngredientShopList>> testObserver = ingredientShopListDAO.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedIngredients -> loadedIngredients.size() == 2)
                .assertValue(loadedIngredients -> loadedIngredients.contains(ingredient1) && loadedIngredients.contains(ingredient2));
    }

    @Test
    public void getByNameAndIDCollection() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.SHOP_LIST);
        Collection collection2 = new Collection("Test Collection 2", CollectionType.SHOP_LIST);
        long collectionId1 = collectionDAO.insert(collection1).blockingGet();
        long collectionId2 = collectionDAO.insert(collection2).blockingGet();

        IngredientShopList ingredient1 = new IngredientShopList("Test Ingredient", collectionId2);
        IngredientShopList ingredient2 = new IngredientShopList("Test Ingredient", collectionId1);
        long id1 = ingredientShopListDAO.insert(ingredient1).blockingGet();
        long id2 = ingredientShopListDAO.insert(ingredient2).blockingGet();
        ingredient1.setId(id1);
        ingredient2.setId(id2);

        TestObserver<IngredientShopList> testObserver = ingredientShopListDAO.getByNameAndIDCollection("Test Ingredient", collectionId2).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(ingredient1);
    }

    @Test
    public void getAllByIDCollection() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.SHOP_LIST);
        Collection collection2 = new Collection("Test Collection 2", CollectionType.SHOP_LIST);
        long collectionId1 = collectionDAO.insert(collection1).blockingGet();
        long collectionId2 = collectionDAO.insert(collection2).blockingGet();

        IngredientShopList ingredient1 = new IngredientShopList("Test Ingredient 1", collectionId2);
        IngredientShopList ingredient2 = new IngredientShopList("Test Ingredient 2", collectionId1);
        long id1 = ingredientShopListDAO.insert(ingredient1).blockingGet();
        long id2 = ingredientShopListDAO.insert(ingredient2).blockingGet();
        ingredient1.setId(id1);
        ingredient2.setId(id2);

        TestObserver<List<IngredientShopList>> testObserver = ingredientShopListDAO.getAllByIDCollection(collectionId1).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedIngredients -> loadedIngredients.size() == 1)
                .assertValue(loadedIngredients -> loadedIngredients.contains(ingredient2));
    }

    @Test
    public void getCountByIdShopList() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.SHOP_LIST);
        Collection collection2 = new Collection("Test Collection 2", CollectionType.SHOP_LIST);
        long collectionId1 = collectionDAO.insert(collection1).blockingGet();
        long collectionId2 = collectionDAO.insert(collection2).blockingGet();

        IngredientShopList ingredient1 = new IngredientShopList("Test Ingredient 1", collectionId1);
        IngredientShopList ingredient2 = new IngredientShopList("Test Ingredient 2", collectionId2);
        IngredientShopList ingredient3 = new IngredientShopList("Test Ingredient 3", collectionId1);
        ingredientShopListDAO.insert(ingredient1).blockingGet();
        ingredientShopListDAO.insert(ingredient2).blockingGet();
        ingredientShopListDAO.insert(ingredient3).blockingGet();

        TestObserver<Integer> testObserver = ingredientShopListDAO.getCountByIDShopList(collectionId1).test();
        testObserver
                .assertNoErrors()
                .assertValue(count -> count == 2);
    }

    @Test
    public void getBoughtCountByIdShopList() {
        Collection collection1 = new Collection("Test Collection 1", CollectionType.SHOP_LIST);
        Collection collection2 = new Collection("Test Collection 2", CollectionType.SHOP_LIST);
        long collectionId1 = collectionDAO.insert(collection1).blockingGet();
        long collectionId2 = collectionDAO.insert(collection2).blockingGet();

        IngredientShopList ingredient1 = new IngredientShopList("Test Ingredient 1", collectionId1, true);
        IngredientShopList ingredient2 = new IngredientShopList("Test Ingredient 2", collectionId2, false);
        IngredientShopList ingredient3 = new IngredientShopList("Test Ingredient 3", collectionId1, false);
        ingredientShopListDAO.insert(ingredient1).blockingGet();
        ingredientShopListDAO.insert(ingredient2).blockingGet();
        ingredientShopListDAO.insert(ingredient3).blockingGet();

        TestObserver<Integer> testObserver = ingredientShopListDAO.getBoughtCountByIDShopList(collectionId1).test();
        testObserver
                .assertNoErrors()
                .assertValue(count -> count == 1);
    }
}
