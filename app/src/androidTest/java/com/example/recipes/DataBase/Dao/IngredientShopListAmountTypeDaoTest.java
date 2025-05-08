package com.example.recipes.DataBase.Dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientShopListAmountTypeDAO;
import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishCollection;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.IngredientShopListAmountType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class IngredientShopListAmountTypeDaoTest {
    private RecipeDatabase db;
    private IngredientShopListAmountTypeDAO ingredientShopListAmountTypeDAO;
    private IngredientShopListDAO ingredientShopListDAO;
    private CollectionDAO collectionDAO;
    private DishDAO dishDAO;
    private DishCollectionDAO dishCollectionDAO;

    @Before
    public void setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RecipeDatabase.class)
                .allowMainThreadQueries()
                .build();
        ingredientShopListAmountTypeDAO = db.ingredientShopListAmountTypeDao();
        ingredientShopListDAO = db.ingredientShopListDao();
        collectionDAO = db.collectionDao();
        dishDAO = db.dishDao();
        dishCollectionDAO = db.dishCollectionDao();
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
        long ingredientId = ingredientShopListDAO.insert(ingredient).blockingGet();

        IngredientShopListAmountType amountType = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId, null);

        TestObserver<Long> testObserver = ingredientShopListAmountTypeDAO.insert(amountType).test();
        testObserver
                .assertNoErrors()
                .awaitCount(1)
                .assertValue(1L);
    }

    @Test
    public void getByID() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", "Amount", IngredientType.VOID, collectionId);
        long ingredientId = ingredientShopListDAO.insert(ingredient).blockingGet();

        IngredientShopListAmountType amountType = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId, null);
        long amountTypeId = ingredientShopListAmountTypeDAO.insert(amountType).blockingGet();
        amountType.setId(amountTypeId);

        TestObserver<IngredientShopListAmountType> testObserver = ingredientShopListAmountTypeDAO.getByID(amountTypeId).test();
        testObserver
                .assertNoErrors()
                .assertValue(amountType);
    }

    @Test
    public void update() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", "Amount", IngredientType.VOID, collectionId);
        long ingredientId = ingredientShopListDAO.insert(ingredient).blockingGet();

        IngredientShopListAmountType amountType = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId, null);
        long amountTypeId = ingredientShopListAmountTypeDAO.insert(amountType).blockingGet();
        amountType.setId(amountTypeId);
        amountType.setAmount("200");
        amountType.setType(IngredientType.MILLILITER);

        TestObserver<Void> updateObserver = ingredientShopListAmountTypeDAO.update(amountType).test();
        updateObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<IngredientShopListAmountType> testObserver = ingredientShopListAmountTypeDAO.getByID(amountTypeId).test();
        testObserver
                .assertNoErrors()
                .assertValue(amountType);
    }

    @Test
    public void delete() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", "Amount", IngredientType.VOID, collectionId);
        long ingredientId = ingredientShopListDAO.insert(ingredient).blockingGet();

        IngredientShopListAmountType amountType = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId, null);
        long amountTypeId = ingredientShopListAmountTypeDAO.insert(amountType).blockingGet();
        amountType.setId(amountTypeId);

        TestObserver<Void> deleteObserver = ingredientShopListAmountTypeDAO.delete(amountType).test();
        deleteObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<IngredientShopListAmountType> testObserver = ingredientShopListAmountTypeDAO.getByID(amountTypeId).test();
        testObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void deleteCascadeWithIngredient() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", "Amount", IngredientType.VOID, collectionId);
        long ingredientId = ingredientShopListDAO.insert(ingredient).blockingGet();
        ingredient.setId(ingredientId);

        IngredientShopListAmountType amountType = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId, null);
        long amountTypeId = ingredientShopListAmountTypeDAO.insert(amountType).blockingGet();
        amountType.setId(amountTypeId);

        TestObserver<Void> deleteObserver = ingredientShopListDAO.delete(ingredient).test();
        deleteObserver
                .assertNoErrors()
                .assertComplete();

        TestObserver<IngredientShopListAmountType> testObserver = ingredientShopListAmountTypeDAO.getByID(amountTypeId).test();
        testObserver
                .assertNoErrors()
                .assertNoValues();
    }

    @Test
    public void getAll() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient1 = new IngredientShopList("Test Ingredient 1", "Amount 1", IngredientType.VOID, collectionId);
        IngredientShopList ingredient2 = new IngredientShopList("Test Ingredient 2", "Amount 2", IngredientType.VOID, collectionId);
        long ingredientId1 = ingredientShopListDAO.insert(ingredient1).blockingGet();
        long ingredientId2 = ingredientShopListDAO.insert(ingredient2).blockingGet();

        IngredientShopListAmountType amountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId1, null);
        IngredientShopListAmountType amountType2 = new IngredientShopListAmountType("200", IngredientType.MILLILITER, ingredientId2, null);
        IngredientShopListAmountType amountType3 = new IngredientShopListAmountType("300", IngredientType.GRAM, ingredientId1, null);
        long amountTypeId1 = ingredientShopListAmountTypeDAO.insert(amountType1).blockingGet();
        long amountTypeId2 = ingredientShopListAmountTypeDAO.insert(amountType2).blockingGet();
        long amountTypeId3 = ingredientShopListAmountTypeDAO.insert(amountType3).blockingGet();
        amountType1.setId(amountTypeId1);
        amountType2.setId(amountTypeId2);
        amountType3.setId(amountTypeId3);

        TestObserver<List<IngredientShopListAmountType>> testObserver = ingredientShopListAmountTypeDAO.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedAmountTypes -> loadedAmountTypes.size() == 3);
    }

    @Test
    public void getByIDIngredient() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient1 = new IngredientShopList("Test Ingredient 1", "Amount 1", IngredientType.VOID, collectionId);
        IngredientShopList ingredient2 = new IngredientShopList("Test Ingredient 2", "Amount 2", IngredientType.VOID, collectionId);
        long ingredientId1 = ingredientShopListDAO.insert(ingredient1).blockingGet();
        long ingredientId2 = ingredientShopListDAO.insert(ingredient2).blockingGet();

        IngredientShopListAmountType amountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId1, null);
        IngredientShopListAmountType amountType2 = new IngredientShopListAmountType("200", IngredientType.MILLILITER, ingredientId2, null);
        IngredientShopListAmountType amountType3 = new IngredientShopListAmountType("300", IngredientType.GRAM, ingredientId1, null);
        long amountTypeId1 = ingredientShopListAmountTypeDAO.insert(amountType1).blockingGet();
        long amountTypeId2 = ingredientShopListAmountTypeDAO.insert(amountType2).blockingGet();
        long amountTypeId3 = ingredientShopListAmountTypeDAO.insert(amountType3).blockingGet();
        amountType1.setId(amountTypeId1);
        amountType2.setId(amountTypeId2);
        amountType3.setId(amountTypeId3);

        TestObserver<List<IngredientShopListAmountType>> testObserver = ingredientShopListAmountTypeDAO.getByIDIngredient(ingredientId1).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedAmountTypes -> loadedAmountTypes.size() == 2)
                .assertValue(loadedAmountTypes -> loadedAmountTypes.contains(amountType1) && loadedAmountTypes.contains(amountType3));
    }

    @Test
    public void getByIDDish() {
        Collection collection = new Collection("Test Collection", CollectionType.SHOP_LIST);
        long collectionId = collectionDAO.insert(collection).blockingGet();

        IngredientShopList ingredient = new IngredientShopList("Test Ingredient", "Amount", IngredientType.VOID, collectionId);
        long ingredientId = ingredientShopListDAO.insert(ingredient).blockingGet();

        Dish dish1 = new Dish("Test Dish 1");
        Dish dish2 = new Dish("Test Dish 2");
        long dishId1 = dishDAO.insert(dish1).blockingGet();
        long dishId2 = dishDAO.insert(dish2).blockingGet();

        DishCollection dishCollection1 = new DishCollection(dishId1, collectionId);
        DishCollection dishCollection2 = new DishCollection(dishId2, collectionId);
        dishCollectionDAO.insert(dishCollection1).blockingGet();
        dishCollectionDAO.insert(dishCollection2).blockingGet();

        IngredientShopListAmountType amountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, ingredientId, dishId2);
        IngredientShopListAmountType amountType2 = new IngredientShopListAmountType("200", IngredientType.MILLILITER, ingredientId, dishId1);
        IngredientShopListAmountType amountType3 = new IngredientShopListAmountType("300", IngredientType.GRAM, ingredientId, dishId1);
        long amountTypeId1 = ingredientShopListAmountTypeDAO.insert(amountType1).blockingGet();
        long amountTypeId2 = ingredientShopListAmountTypeDAO.insert(amountType2).blockingGet();
        long amountTypeId3 = ingredientShopListAmountTypeDAO.insert(amountType3).blockingGet();
        amountType1.setId(amountTypeId1);
        amountType2.setId(amountTypeId2);
        amountType3.setId(amountTypeId3);

        TestObserver<List<IngredientShopListAmountType>> testObserver = ingredientShopListAmountTypeDAO.getByIDDish(dishId1).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedAmountTypes -> loadedAmountTypes.size() == 2)
                .assertValue(loadedAmountTypes -> loadedAmountTypes.contains(amountType2) && loadedAmountTypes.contains(amountType3));
    }
}
