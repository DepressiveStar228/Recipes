package com.example.recipes.DataBase.Repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Database.Repositories.CollectionRepository;
import com.example.recipes.Database.Repositories.DishCollectionRepository;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishCollection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class DishCollectionRepositoryTest {
    private DishCollectionRepository dishCollectionRepository;
    private String testDishName = "Test Dish";
    private String testCollectionName = "Test Collection";

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private DishCollectionDAO dao;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        dishCollectionRepository = new DishCollectionRepository(ApplicationProvider.getApplicationContext(), dao);
        dishCollectionRepository.setDependencies(collectionRepository);
    }

    @Test
    public void add() {
        DishCollection dishCollection = new DishCollection(1L, 1L);

        when(dao.insert(dishCollection)).thenReturn(Single.just(1L));

        TestObserver<Long> testObserver = dishCollectionRepository.add(dishCollection).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);
    }

    @Test
    public void addWithCheckExist() {
        DishCollection dishCollection1 = new DishCollection(1L, 1L);
        DishCollection dishCollection2 = new DishCollection(2L, 2L);
        DishCollection dishCollection3 = new DishCollection(3L, 3L);

        Collection collection1 = new Collection(testCollectionName, CollectionType.COLLECTION);
        Collection collection2 = new Collection(null, CollectionType.COLLECTION);

        when(dao.insert(any(DishCollection.class))).thenReturn(Single.just(1L));
        when(dao.getByIDDishAndIDCollection(dishCollection1.getIdDish(), dishCollection1.getIdCollection())).thenReturn(Maybe.just(dishCollection1));
        when(dao.getByIDDishAndIDCollection(dishCollection3.getIdDish(), dishCollection3.getIdCollection())).thenReturn(Maybe.just(dishCollection3));
        when(dao.getByIDDishAndIDCollection(dishCollection2.getIdDish(), dishCollection2.getIdCollection())).thenReturn(Maybe.empty());
        when(collectionRepository.getByID(dishCollection1.getIdCollection())).thenReturn(Single.just(collection1));
        when(collectionRepository.getByID(dishCollection3.getIdCollection())).thenReturn(Single.just(collection2));
        when(collectionRepository.getCustomNameSystemCollectionByName(testCollectionName)).thenReturn(testCollectionName);
        when(collectionRepository.getCustomNameSystemCollectionByName(null)).thenReturn("Unknown Collection");

        TestObserver<Boolean> testObserver = dishCollectionRepository.addWithCheckExist(dishCollection1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishCollectionRepository.addWithCheckExist(dishCollection2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver3 = dishCollectionRepository.addWithCheckExist(dishCollection3).test();
        testObserver3
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void addAllDishCollections() {
        DishCollection dishCollection1 = new DishCollection(1L, 1L);
        DishCollection dishCollection2 = new DishCollection(2L, 2L);
        DishCollection dishCollection3 = new DishCollection(3L, 3L);
        ArrayList<DishCollection> dishCollections1 = new ArrayList<>(List.of(dishCollection1, dishCollection2));
        ArrayList<DishCollection> dishCollections2 = new ArrayList<>(List.of(dishCollection2, dishCollection3));

        Collection collection1 = new Collection(testCollectionName, CollectionType.COLLECTION);
        Collection collection2 = new Collection(null, CollectionType.COLLECTION);

        when(dao.insert(any(DishCollection.class))).thenReturn(Single.just(1L));
        when(dao.getByIDDishAndIDCollection(dishCollection1.getIdDish(), dishCollection1.getIdCollection())).thenReturn(Maybe.just(dishCollection1));
        when(dao.getByIDDishAndIDCollection(dishCollection3.getIdDish(), dishCollection3.getIdCollection())).thenReturn(Maybe.just(dishCollection3));
        when(dao.getByIDDishAndIDCollection(dishCollection2.getIdDish(), dishCollection2.getIdCollection())).thenReturn(Maybe.empty());
        when(collectionRepository.getByID(dishCollection1.getIdCollection())).thenReturn(Single.just(collection1));
        when(collectionRepository.getByID(dishCollection3.getIdCollection())).thenReturn(Single.just(collection2));
        when(collectionRepository.getCustomNameSystemCollectionByName(testCollectionName)).thenReturn(testCollectionName);
        when(collectionRepository.getCustomNameSystemCollectionByName(null)).thenReturn("Unknown Collection");

        TestObserver<Boolean> testObserver = dishCollectionRepository.addAll(dishCollections1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishCollectionRepository.addAll(dishCollections2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void addAllDishAndCollections() {
        Dish dish = new Dish(1L, testDishName, 1);

        Collection collection1 = new Collection(1L, testCollectionName + "1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection(2L, testCollectionName + "2", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection3 = new Collection(3L, null, CollectionType.COLLECTION, new ArrayList<>());
        ArrayList<Collection> collections1 = new ArrayList<>(List.of(collection1, collection2));
        ArrayList<Collection> collections2 = new ArrayList<>(List.of(collection2, collection3));

        DishCollection dishCollection1 = new DishCollection(dish.getId(), collection1.getId());
        DishCollection dishCollection2 = new DishCollection(dish.getId(), collection2.getId());
        DishCollection dishCollection3 = new DishCollection(dish.getId(), collection3.getId());

        when(dao.insert(any(DishCollection.class))).thenReturn(Single.just(1L));
        when(dao.getByIDDishAndIDCollection(dishCollection1.getIdDish(), dishCollection1.getIdCollection())).thenReturn(Maybe.just(dishCollection1));
        when(dao.getByIDDishAndIDCollection(dishCollection3.getIdDish(), dishCollection3.getIdCollection())).thenReturn(Maybe.just(dishCollection3));
        when(dao.getByIDDishAndIDCollection(dishCollection2.getIdDish(), dishCollection2.getIdCollection())).thenReturn(Maybe.empty());
        when(collectionRepository.getByID(dishCollection1.getIdCollection())).thenReturn(Single.just(collection1));
        when(collectionRepository.getByID(dishCollection2.getIdCollection())).thenReturn(Single.just(collection2));
        when(collectionRepository.getByID(dishCollection3.getIdCollection())).thenReturn(Single.just(collection3));
        when(collectionRepository.getCustomNameSystemCollectionByName(testCollectionName + "1")).thenReturn(testCollectionName + "1");
        when(collectionRepository.getCustomNameSystemCollectionByName(testCollectionName + "2")).thenReturn(testCollectionName + "2");
        when(collectionRepository.getCustomNameSystemCollectionByName(null)).thenReturn("Unknown Collection");

        TestObserver<Boolean> testObserver = dishCollectionRepository.addAll(dish, collections1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishCollectionRepository.addAll(dish, collections2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void addAllDishesAndCollection() {
        Dish dish1 = new Dish(1L, testDishName + "1", 1);
        Dish dish2 = new Dish(2L, testDishName + "2", 1);
        ArrayList<Dish> dishes1 = new ArrayList<>(List.of(dish1, dish2));

        Collection collection = new Collection(1L, testCollectionName, CollectionType.COLLECTION, new ArrayList<>());

        DishCollection dishCollection1 = new DishCollection(dish1.getId(), collection.getId());
        DishCollection dishCollection2 = new DishCollection(dish2.getId(), collection.getId());

        when(dao.insert(any(DishCollection.class))).thenReturn(Single.just(1L));
        when(dao.getByIDDishAndIDCollection(dishCollection1.getIdDish(), dishCollection1.getIdCollection())).thenReturn(Maybe.just(dishCollection1));
        when(dao.getByIDDishAndIDCollection(dishCollection2.getIdDish(), dishCollection2.getIdCollection())).thenReturn(Maybe.empty());
        when(collectionRepository.getByID(collection.getId())).thenReturn(Single.just(collection));
        when(collectionRepository.getCustomNameSystemCollectionByName(testCollectionName)).thenReturn(testCollectionName);
        when(collectionRepository.getCustomNameSystemCollectionByName(null)).thenReturn("Unknown Collection");

        TestObserver<Boolean> testObserver = dishCollectionRepository.addAll(dishes1, collection).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void getByID() {
        DishCollection dishCollection = new DishCollection(1L, 1L);
        DishCollection emptyDishCollection = new DishCollection(0, 0);

        when(dao.getByID(1L)).thenReturn(Maybe.just(dishCollection));
        when(dao.getByID(2L)).thenReturn(Maybe.empty());

        TestObserver<DishCollection> testObserver = dishCollectionRepository.getByID(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(dishCollection);

        TestObserver<DishCollection> testObserver2 = dishCollectionRepository.getByID(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(emptyDishCollection);
    }

    @Test
    public void getByIDDish() {
        DishCollection dishCollection = new DishCollection(1L, 1L);

        when(dao.getByIDDish(1L)).thenReturn(Maybe.just(List.of(dishCollection)));
        when(dao.getByIDDish(2L)).thenReturn(Maybe.empty());

        TestObserver<List<DishCollection>> testObserver = dishCollectionRepository.getByIDDish(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(dishCollection));

        TestObserver<List<DishCollection>> testObserver2 = dishCollectionRepository.getByIDDish(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(List.of());
    }

    @Test
    public void getAllIDsCollectionByIDDish() {
        when(dao.getAllIDsCollectionByIDDish(1L)).thenReturn(Maybe.just(List.of(1L)));
        when(dao.getAllIDsCollectionByIDDish(2L)).thenReturn(Maybe.empty());

        TestObserver<List<Long>> testObserver = dishCollectionRepository.getAllIDsCollectionByIDDish(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(1L));

        TestObserver<List<Long>> testObserver2 = dishCollectionRepository.getAllIDsCollectionByIDDish(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(List.of());
    }

    @Test
    public void getAllIDsDishByIDCollection() {
        when(dao.getAllIDsDishByIDCollection(1L)).thenReturn(Maybe.just(List.of(1L)));
        when(dao.getAllIDsDishByIDCollection(2L)).thenReturn(Maybe.empty());

        TestObserver<List<Long>> testObserver = dishCollectionRepository.getAllIDsDishByIDCollection(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(1L));

        TestObserver<List<Long>> testObserver2 = dishCollectionRepository.getAllIDsDishByIDCollection(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(List.of());
    }

    @Test
    public void getByData() {
        DishCollection dishCollection = new DishCollection(1L, 1L);
        DishCollection emptyDishCollection = new DishCollection(0, 0);

        when(dao.getByIDDishAndIDCollection(1L, 1L)).thenReturn(Maybe.just(dishCollection));
        when(dao.getByIDDishAndIDCollection(2L, 2L)).thenReturn(Maybe.empty());

        TestObserver<DishCollection> testObserver = dishCollectionRepository.getByData(1L, 1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(dishCollection);

        TestObserver<DishCollection> testObserver2 = dishCollectionRepository.getByData(2L, 2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(emptyDishCollection);
    }

    @Test
    public void isExist() {
        DishCollection dishCollection1 = new DishCollection(1L, 1L);
        DishCollection dishCollection2 = new DishCollection(2L, 2L);

        when(dao.getByIDDishAndIDCollection(dishCollection1.getIdDish(), dishCollection1.getIdCollection())).thenReturn(Maybe.just(dishCollection1));
        when(dao.getByIDDishAndIDCollection(dishCollection2.getIdDish(), dishCollection2.getIdCollection())).thenReturn(Maybe.empty());

        TestObserver<Boolean> testObserver = dishCollectionRepository.isExist(dishCollection1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishCollectionRepository.isExist(dishCollection2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void copyDishesToAnotherCollections() {
        Dish dish1 = new Dish(1L, testDishName + "1", 1);
        Dish dish2 = new Dish(2L, testDishName + "2", 2);

        Collection originalCollection = new Collection(1L,"Original Collection", CollectionType.COLLECTION, new ArrayList<>());
        Collection fakeOriginalCollection = new Collection(4L,"Fake Original Collection", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection1 = new Collection(2L, testCollectionName + "1", CollectionType.COLLECTION, new ArrayList<>());
        Collection collection2 = new Collection(3L, testCollectionName + "2", CollectionType.COLLECTION, new ArrayList<>());
        ArrayList<Collection> collections = new ArrayList<>(List.of(collection1, collection2));

        when(collectionRepository.getDishes(originalCollection.getId())).thenReturn(Single.just(List.of(dish1, dish2)));
        when(collectionRepository.getDishes(4L)).thenReturn(Single.just(List.of()));
        when(dao.insert(new DishCollection(dish1.getId(), collection2.getId()))).thenReturn(Single.just(1L));
        when(dao.insert(new DishCollection(dish2.getId(), collection1.getId()))).thenReturn(Single.just(1L));
        when(dao.insert(new DishCollection(dish2.getId(), collection2.getId()))).thenReturn(Single.just(1L));
        when(dao.getByIDDishAndIDCollection(dish1.getId(), collection1.getId())).thenReturn(Maybe.just(new DishCollection(dish1.getId(), collection1.getId())));
        when(dao.getByIDDishAndIDCollection(dish1.getId(), collection2.getId())).thenReturn(Maybe.empty());
        when(dao.getByIDDishAndIDCollection(dish2.getId(), collection1.getId())).thenReturn(Maybe.empty());
        when(dao.getByIDDishAndIDCollection(dish2.getId(), collection2.getId())).thenReturn(Maybe.empty());

        TestObserver<Boolean> testObserver = dishCollectionRepository.copyDishesToAnotherCollections(originalCollection, collections).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishCollectionRepository.copyDishesToAnotherCollections(fakeOriginalCollection, collections).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void deleteAll() {
        DishCollection dishCollection1 = new DishCollection(1L, 1L);
        DishCollection dishCollection2 = new DishCollection(2L, 2L);
        ArrayList<DishCollection> dishCollections = new ArrayList<>(List.of(dishCollection1, dishCollection2));

        when(dao.delete(any(DishCollection.class))).thenReturn(Completable.complete());

        TestObserver<Boolean> testObserver = dishCollectionRepository.deleteAll(dishCollections).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void deleteAllByIDCollection() {
        DishCollection dishCollection1 = new DishCollection(1L, 1L);
        DishCollection dishCollection2 = new DishCollection(2L, 1L);

        when(dao.delete(any(DishCollection.class))).thenReturn(Completable.complete());
        when(dao.getAllIDsDishByIDCollection(1L)).thenReturn(Maybe.just(List.of(1L, 2L)));
        when(dao.getAllIDsDishByIDCollection(2L)).thenReturn(Maybe.just(List.of(2L, 3L)));
        when(dao.getByIDDishAndIDCollection(1L, 1L)).thenReturn(Maybe.just(dishCollection1));
        when(dao.getByIDDishAndIDCollection(2L, 1L)).thenReturn(Maybe.just(dishCollection2));
        when(dao.getByIDDishAndIDCollection(3L, 1L)).thenReturn(Maybe.empty());

        TestObserver<Boolean> testObserver = dishCollectionRepository.deleteAllByIDCollection(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishCollectionRepository.deleteAllByIDCollection(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }
}
