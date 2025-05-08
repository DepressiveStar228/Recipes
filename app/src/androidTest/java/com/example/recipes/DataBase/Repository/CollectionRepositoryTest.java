package com.example.recipes.DataBase.Repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.Repositories.CollectionRepository;
import com.example.recipes.Database.Repositories.DishCollectionRepository;
import com.example.recipes.Database.Repositories.DishRepository;
import com.example.recipes.Database.Repositories.IngredientShopListRepository;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.ShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.ClassUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class CollectionRepositoryTest {
    private CollectionRepository collectionRepository;
    private String testCollectionName = "Test Collection";

    @Mock
    private CollectionDAO collectionDAO;
    @Mock
    private DishRepository dishRepository;
    @Mock
    private DishCollectionRepository dishCollectionRepository;
    @Mock
    private IngredientShopListRepository ingredientShopListRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        collectionRepository = new CollectionRepository(ApplicationProvider.getApplicationContext(), collectionDAO);
        collectionRepository.setDependencies(dishRepository, dishCollectionRepository, ingredientShopListRepository);
    }

    @Test
    public void addAll() {
        Collection collection1 = new Collection(testCollectionName + "1", CollectionType.COLLECTION);
        Collection collection2 = new Collection(testCollectionName + "2", CollectionType.COLLECTION);
        ArrayList<Collection> collections = new ArrayList<>(List.of(collection1, collection2));

        when(collectionDAO.insert(any(Collection.class))).thenReturn(Single.just(1L));

        TestObserver<Boolean> testObserver = collectionRepository.addAll(collections).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void getAll() {
        Collection collection1 = new Collection(Collection.SYSTEM_COLLECTION_TAG + "1", CollectionType.COLLECTION);
        Collection collection2 = new Collection(testCollectionName + "2", CollectionType.COLLECTION);
        Collection collection1WithCustomName = new Collection(ApplicationProvider.getApplicationContext().getString(R.string.favorites), CollectionType.COLLECTION);
        ArrayList<Collection> collections = new ArrayList<>(List.of(collection1, collection2));
        ArrayList<Collection> collectionsTest = new ArrayList<>(List.of(collection1WithCustomName, collection2));

        when(collectionDAO.getAll()).thenReturn(Single.just(collections));
        when(dishCollectionRepository.getAllIDsDishByIDCollection(any(Long.class))).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<List<Collection>> testObserver = collectionRepository.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollection -> loadedCollection.size() == 2)
                .assertValue(loadedCollection -> loadedCollection.equals(collectionsTest));
    }

    @Test
    public void getAllByType() {
        Collection collection1 = new Collection(testCollectionName + "1", CollectionType.COLLECTION);
        Collection collection2 = new Collection(testCollectionName + "2", CollectionType.COLLECTION);
        ArrayList<Collection> collections = new ArrayList<>(List.of(collection1, collection2));

        ShopList shopList1 = new ShopList(testCollectionName + "3");
        ShopList shopList2 = new ShopList(testCollectionName + "4");
        ArrayList<ShopList> shopLists = new ArrayList<>(List.of(shopList1, shopList2));

        when(collectionDAO.getAllByType(CollectionType.COLLECTION)).thenReturn(Single.just(collections));
        when(collectionDAO.getAllByType(CollectionType.SHOP_LIST)).thenReturn(Single.just(new ArrayList<>(List.of(shopList1, shopList2))));
        when(ingredientShopListRepository.getAllByIDCollection(any(Long.class))).thenReturn(Single.just(new ArrayList<>()));
        when(dishCollectionRepository.getAllIDsDishByIDCollection(any(Long.class))).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<List<Object>> testObserver = collectionRepository.getAllByType(CollectionType.COLLECTION).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedData -> loadedData.size() == 2)
                .assertValue(loadedData -> {
                    if (ClassUtils.isListOfType(loadedData, Collection.class)) {
                        ArrayList<Collection> loadedCollections = ClassUtils.getListOfType(loadedData, Collection.class);
                        return loadedCollections.equals(collections);
                    } else return false;
                });

        TestObserver<List<Object>> testObserver2 = collectionRepository.getAllByType(CollectionType.SHOP_LIST).test();
        testObserver2
                .assertNoErrors()
                .assertValue(loadedData -> loadedData.size() == 2)
                .assertValue(loadedData -> {
                    if (ClassUtils.isListOfType(loadedData, ShopList.class)) {
                        ArrayList<ShopList> loadedShopLists = ClassUtils.getListOfType(loadedData, ShopList.class);
                        return loadedShopLists.equals(shopLists);
                    } else return false;
                });
    }

    @Test
    public void getByID() {
        Collection collection = new Collection(testCollectionName, CollectionType.COLLECTION);
        Collection emptyCollection = new Collection(-1L, "Unknown Collection",  CollectionType.COLLECTION, new ArrayList<>());

        when(collectionDAO.getByID(1L)).thenReturn(Maybe.just(collection));
        when(collectionDAO.getByID(-1L)).thenReturn(Maybe.empty());
        when(dishCollectionRepository.getAllIDsDishByIDCollection(any(Long.class))).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<Collection> testObserver = collectionRepository.getByID(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollection -> loadedCollection.equals(collection));

        TestObserver<Collection> testObserver2 = collectionRepository.getByID(-1L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(loadedCollection -> loadedCollection.equals(emptyCollection));
    }

    @Test
    public void getByName() {
        Collection collection = new Collection(1L, testCollectionName, CollectionType.COLLECTION, new ArrayList<>());
        Collection emptyCollection = new Collection(-1L, "Unknown Collection",  CollectionType.COLLECTION, new ArrayList<>());

        when(collectionDAO.getByName(testCollectionName)).thenReturn(Maybe.just(collection));
        when(collectionDAO.getByName(testCollectionName + "1")).thenReturn(Maybe.empty());
        when(collectionDAO.getByID(1L)).thenReturn(Maybe.just(collection));
        when(dishCollectionRepository.getAllIDsDishByIDCollection(any(Long.class))).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<Collection> testObserver = collectionRepository.getByName(testCollectionName).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedData -> loadedData.equals(collection));

        TestObserver<Collection> testObserver2 = collectionRepository.getByName(testCollectionName + "1").test();
        testObserver2
                .assertNoErrors()
                .assertValue(loadedData -> loadedData.equals(emptyCollection));
    }

    @Test
    public void getIDByName() {
        when(collectionDAO.getIDByName(testCollectionName)).thenReturn(Maybe.just(1L));
        when(collectionDAO.getIDByName(testCollectionName + "1")).thenReturn(Maybe.empty());

        TestObserver<Long> testObserver = collectionRepository.getIDByName(testCollectionName).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedID -> loadedID == 1L);

        TestObserver<Long> testObserver2 = collectionRepository.getIDByName(testCollectionName + "1").test();
        testObserver2
                .assertNoErrors()
                .assertValue(loadedID -> loadedID == -1L);
    }

    @Test
    public void getIDByNameAndType() {
        when(collectionDAO.getIDByNameAndType(testCollectionName, CollectionType.COLLECTION)).thenReturn(Maybe.just(1L));
        when(collectionDAO.getIDByNameAndType(testCollectionName + "1", CollectionType.COLLECTION)).thenReturn(Maybe.empty());
        when(collectionDAO.getIDByNameAndType(testCollectionName, CollectionType.SHOP_LIST)).thenReturn(Maybe.empty());
        when(collectionDAO.getIDByNameAndType(testCollectionName + "1", CollectionType.SHOP_LIST)).thenReturn(Maybe.just(2L));

        TestObserver<Long> testObserver = collectionRepository.getIDByNameAndType(testCollectionName, CollectionType.COLLECTION).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedID -> loadedID == 1L);

        TestObserver<Long> testObserver2 = collectionRepository.getIDByNameAndType(testCollectionName + "1", CollectionType.COLLECTION).test();
        testObserver2
                .assertNoErrors()
                .assertValue(loadedID -> loadedID == -1L);

        TestObserver<Long> testObserver3 = collectionRepository.getIDByNameAndType(testCollectionName, CollectionType.SHOP_LIST).test();
        testObserver3
                .assertNoErrors()
                .assertValue(loadedID -> loadedID == -1L);

        TestObserver<Long> testObserver4 = collectionRepository.getIDByNameAndType(testCollectionName + "1", CollectionType.SHOP_LIST).test();
        testObserver4
                .assertNoErrors()
                .assertValue(loadedID -> loadedID == 2L);
    }

    @Test
    public void getUnusedDish() {
        Collection collection = new Collection(1L, testCollectionName, CollectionType.COLLECTION, new ArrayList<>());
        Dish dish1 = new Dish(1L, "Test Dish 1", 1);
        Dish dish2 = new Dish(2L, "Test Dish 2", 2);
        Dish dish3 = new Dish(3L, "Test Dish 3", 3);
        Dish dish4 = new Dish(4L, "Test Dish 4", 4);
        Dish dish5 = new Dish(5L, "Test Dish 5", 5);
        ArrayList<Dish> dishes = new ArrayList<>(List.of(dish1, dish2, dish3, dish4, dish5));

        when(dishRepository.getAll()).thenReturn(Single.just(dishes));
        when(dishCollectionRepository.getAllIDsDishByIDCollection(any(Long.class))).thenReturn(Single.just(List.of(1L, 2L, 5L)));
        when(dishRepository.getByID(1L)).thenReturn(Single.just(dish1));
        when(dishRepository.getByID(2L)).thenReturn(Single.just(dish2));
        when(dishRepository.getByID(5L)).thenReturn(Single.just(dish5));

        TestObserver<ArrayList<Dish>> testObserver = collectionRepository.getUnusedDish(collection).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 2)
                .assertValue(loadedDishes -> loadedDishes.equals(new ArrayList<>(List.of(dish3, dish4))));
    }

    @Test
    public void getDishes() {
        Collection collection = new Collection(1L, testCollectionName, CollectionType.COLLECTION, new ArrayList<>());
        Dish dish1 = new Dish(1L, "Test Dish 1", 1);
        Dish dish2 = new Dish(2L, "Test Dish 2", 2);
        Dish dish3 = new Dish(3L, "Test Dish 3", 3);
        ArrayList<Dish> dishes = new ArrayList<>(List.of(dish1, dish2, dish3));

        when(dishCollectionRepository.getAllIDsDishByIDCollection(any(Long.class))).thenReturn(Single.just(List.of(1L, 2L, 3L)));
        when(dishRepository.getByID(1L)).thenReturn(Single.just(dish1));
        when(dishRepository.getByID(2L)).thenReturn(Single.just(dish2));
        when(dishRepository.getByID(3L)).thenReturn(Single.just(dish3));

        TestObserver<List<Dish>> testObserver = collectionRepository.getDishes(collection.getId()).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes -> loadedDishes.equals(dishes));
    }

    @Test
    public void getUnusedByTypeInDish() {
        Collection collection1 = new Collection(1L, testCollectionName + "1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection(2L, testCollectionName + "2", CollectionType.COLLECTION, new ArrayList<>());
        ShopList shopList1 = new ShopList(3L, testCollectionName + "1");
        ShopList shopList2 = new ShopList(4L, testCollectionName + "2");
        ArrayList<Collection> collections = new ArrayList<>(List.of(collection1, collection2));
        ArrayList<Collection> shopLists = new ArrayList<>(List.of(shopList1, shopList2));

        Dish dish = new Dish(1L, "Test Dish", 1);

        when(collectionDAO.getAllByType(CollectionType.COLLECTION)).thenReturn(Single.just(collections));
        when(collectionDAO.getAllByType(CollectionType.SHOP_LIST)).thenReturn(Single.just(shopLists));
        when(dishRepository.getCollections(dish)).thenReturn(Single.just(new ArrayList<>(List.of(collection1, shopList1))));
        when(dishCollectionRepository.getAllIDsDishByIDCollection(any(Long.class))).thenReturn(Single.just(new ArrayList<>()));
        when(ingredientShopListRepository.getAllByIDCollection(any(Long.class))).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<ArrayList<Collection>> testObserver = collectionRepository.getUnusedByTypeInDish(dish, CollectionType.COLLECTION).test();
        testObserver
                .assertNoErrors()
                .assertValue(loadedCollections -> loadedCollections.size() == 1)
                .assertValue(loadedCollections -> loadedCollections.contains(collection2));

        TestObserver<ArrayList<Collection>> testObserver2 = collectionRepository.getUnusedByTypeInDish(dish, CollectionType.SHOP_LIST).test();
        testObserver2
                .assertNoErrors()
                .assertValue(loadedCollections -> loadedCollections.size() == 1)
                .assertValue(loadedCollections -> loadedCollections.contains(shopList2));
    }
}
