package com.example.recipes.DataBase.Repository;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Database.Repositories.IngredientShopListAmountTypeRepository;
import com.example.recipes.Database.Repositories.IngredientShopListRepository;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.IDSystemCollection;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.IngredientShopListAmountType;
import com.example.recipes.Utils.RecipeUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class IngredientShopListRepositoryTest {
    private IngredientShopListRepository ingredientShopListRepository;
    private String testNameIngredient = "testNameIngredient";

    @Mock
    private IngredientShopListDAO dao;
    @Mock
    private IngredientShopListAmountTypeRepository ingredientShopListAmountTypeRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ingredientShopListRepository = new IngredientShopListRepository(ApplicationProvider.getApplicationContext(), dao);
        ingredientShopListRepository.setDependencies(ingredientShopListAmountTypeRepository);
    }

    @Test
    public void add() {
        IngredientShopList ingredientShopList = new IngredientShopList();

        when(dao.insert(ingredientShopList)).thenReturn(Single.just(1L));

        TestObserver<Long> testObserver = ingredientShopListRepository.add(ingredientShopList).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);
    }

    @Test
    public void addWithIDDish() {
        Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
        groupedAmountType.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        ingredientShopList1.setGroupedAmountType(groupedAmountType);

        when(dao.insert(ingredientShopList1)).thenReturn(Single.just(1L));
        when(dao.insert(ingredientShopList2)).thenReturn(Single.just(-1L));
        when(ingredientShopListAmountTypeRepository.add(new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, 1L))).thenReturn(Single.just(1L));
        when(ingredientShopListAmountTypeRepository.add(new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, 1L))).thenReturn(Single.just(1L));

        TestObserver<Long> testObserver = ingredientShopListRepository.add(ingredientShopList1, 1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);

        TestObserver<Long> testObserver2 = ingredientShopListRepository.add(ingredientShopList2, 1L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(-1L);
    }

    @Test
    public void addAll() {
        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        IngredientShopList ingredientShopList3 = new IngredientShopList(3L, testNameIngredient + "3");
        ArrayList<IngredientShopList> ingredientShopLists1 = new ArrayList<>(List.of(ingredientShopList1, ingredientShopList2));
        ArrayList<IngredientShopList> ingredientShopLists2 = new ArrayList<>(List.of(ingredientShopList2, ingredientShopList3));

        when(dao.insert(ingredientShopList1)).thenReturn(Single.just(1L));
        when(dao.insert(ingredientShopList2)).thenReturn(Single.just(2L));
        when(dao.insert(ingredientShopList3)).thenReturn(Single.just(-1L));

        TestObserver<Boolean> testObserver = ingredientShopListRepository.addAll(ingredientShopLists1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = ingredientShopListRepository.addAll(ingredientShopLists2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void addAllWithIDCollection() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION);
        RecipeUtils recipeUtils = new RecipeUtils(ApplicationProvider.getApplicationContext());
        long idCollection = recipeUtils.ByCollection().add(collection).blockingGet();

        IngredientShopList ingredientShopList1 = new IngredientShopList(testNameIngredient + "1", idCollection, false);
        IngredientShopList ingredientShopList2 = new IngredientShopList(testNameIngredient + "2", idCollection, true);
        ArrayList<IngredientShopList> ingredientShopLists = new ArrayList<>(List.of(ingredientShopList1, ingredientShopList2));

        Ingredient ingredient1 = new Ingredient(testNameIngredient + "3", "1", IngredientType.TEASPOON);
        Ingredient ingredient2 = new Ingredient(testNameIngredient + "4", "2", IngredientType.PIECES);
        ArrayList<Ingredient> ingredients = new ArrayList<>(List.of(ingredient1, ingredient2));
        ArrayList<String> nameIngredients = new ArrayList<>(List.of(testNameIngredient + "5", testNameIngredient + "6"));
        ArrayList<Boolean> booleanArray = new ArrayList<>(List.of(true, false));

        when(dao.insert(ingredientShopList1)).thenReturn(Single.just(1L));
        when(dao.insert(ingredientShopList2)).thenReturn(Single.just(1L));
        when(dao.insert(new IngredientShopList(ingredient1.getName().trim(), ingredient1.getAmount(), ingredient1.getType(), idCollection))).thenReturn(Single.just(1L));
        when(dao.insert(new IngredientShopList(ingredient2.getName().trim(), ingredient2.getAmount(), ingredient2.getType(), idCollection))).thenReturn(Single.just(1L));
        when(dao.insert(new IngredientShopList(testNameIngredient + "5", idCollection))).thenReturn(Single.just(1L));
        when(dao.insert(new IngredientShopList(testNameIngredient + "6", idCollection))).thenReturn(Single.just(1L));

        TestObserver<Boolean> testObserver = ingredientShopListRepository.addAll(idCollection, ingredientShopLists).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = ingredientShopListRepository.addAll(idCollection, ingredients).test();
        testObserver2
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver3 = ingredientShopListRepository.addAll(idCollection, nameIngredients).test();
        testObserver3
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver4 = ingredientShopListRepository.addAll(idCollection, booleanArray).test();
        testObserver4
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void getDataFromIngredient() {
        Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
        groupedAmountType.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        IngredientShopList finalIngredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        finalIngredientShopList1.setGroupedAmountType(groupedAmountType);

        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, null);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, null);
        ArrayList<IngredientShopListAmountType> listAmountTypes = new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2));

        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList1.getId())).thenReturn(Single.just(listAmountTypes));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList2.getId())).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<IngredientShopList> testObserver = ingredientShopListRepository.getDataFromIngredient(ingredientShopList1).test();
        testObserver
                .assertNoErrors()
                .assertValue(finalIngredientShopList1);

        TestObserver<IngredientShopList> testObserver2 = ingredientShopListRepository.getDataFromIngredient(ingredientShopList2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(ingredientShopList2);
    }

    @Test
    public void getAll() {
        Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
        groupedAmountType.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        IngredientShopList finalIngredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        finalIngredientShopList1.setGroupedAmountType(groupedAmountType);

        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, null);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, null);
        ArrayList<IngredientShopListAmountType> listAmountTypes = new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2));

        when(dao.getAll()).thenReturn(Single.just(new ArrayList<>(List.of(ingredientShopList1, ingredientShopList2))));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList1.getId())).thenReturn(Single.just(listAmountTypes));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList2.getId())).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<List<IngredientShopList>> testObserver = ingredientShopListRepository.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(finalIngredientShopList1, ingredientShopList2));
    }

    @Test
    public void getAllByIDCollection() {
        Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
        groupedAmountType.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        IngredientShopList finalIngredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        finalIngredientShopList1.setGroupedAmountType(groupedAmountType);

        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, null);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, null);
        ArrayList<IngredientShopListAmountType> listAmountTypes = new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2));

        when(dao.getAllByIDCollection(1L)).thenReturn(Maybe.just(new ArrayList<>(List.of(ingredientShopList1))));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList1.getId())).thenReturn(Single.just(listAmountTypes));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList2.getId())).thenReturn(Single.just(new ArrayList<>()));

        TestObserver<List<IngredientShopList>> testObserver = ingredientShopListRepository.getAllByIDCollection(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(finalIngredientShopList1));
    }

    @Test
    public void getAllByBlackList() {
        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");

        when(dao.getAllByIDCollection(IDSystemCollection.ID_BLACK_LIST.getId())).thenReturn(Maybe.just(new ArrayList<>(List.of(ingredientShopList1))));

        TestObserver<List<IngredientShopList>> testObserver = ingredientShopListRepository.getAllByBlackList().test();
        testObserver
                .assertNoErrors()
                .assertValue(new ArrayList<>(List.of(ingredientShopList1)));
    }

    @Test
    public void getAllNamesByBlackList() {
        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");

        when(dao.getAllByIDCollection(IDSystemCollection.ID_BLACK_LIST.getId())).thenReturn(Maybe.just(new ArrayList<>(List.of(ingredientShopList1))));

        TestObserver<List<String>> testObserver = ingredientShopListRepository.getAllNamesByBlackList().test();
        testObserver
                .assertNoErrors()
                .assertValue(new ArrayList<>(List.of(ingredientShopList1.getName())));
    }

    @Test
    public void filteredBlackList() {
        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");

        Ingredient ingredient1 = new Ingredient(testNameIngredient + "2", "1", IngredientType.TEASPOON);
        Ingredient ingredient2 = new Ingredient(testNameIngredient + "3", "2", IngredientType.PIECES);

        when(dao.getAllByIDCollection(IDSystemCollection.ID_BLACK_LIST.getId())).thenReturn(Maybe.just(new ArrayList<>(List.of(ingredientShopList1, ingredientShopList2))));

        TestObserver<List<Ingredient>> testObserver = ingredientShopListRepository.filteredBlackList(new ArrayList<>(List.of(ingredient1, ingredient2))).test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(ingredient2));
    }

    @Test
    public void convertIngredientsToIngredientsShopList() {
        Ingredient ingredient1 = new Ingredient(testNameIngredient + "2", "1", IngredientType.TEASPOON);
        Ingredient ingredient2 = new Ingredient(testNameIngredient + "3", "2", IngredientType.PIECES);

        IngredientShopList ingredientShopList1 = new IngredientShopList(ingredient1);
        IngredientShopList ingredientShopList2 = new IngredientShopList(ingredient2);

        TestObserver<List<IngredientShopList>> testObserver = ingredientShopListRepository.convertIngredientsToIngredientsShopList(new ArrayList<>(List.of(ingredient1, ingredient2))).test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(ingredientShopList1, ingredientShopList2));
    }

    @Test
    public void groupIngredients() {
        Collection collection = new Collection("Test Collection", CollectionType.COLLECTION);
        RecipeUtils recipeUtils = new RecipeUtils(ApplicationProvider.getApplicationContext());
        long idCollection = recipeUtils.ByCollection().add(collection).blockingGet();
        collection.setId(idCollection);

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient);
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient);
        IngredientShopList result = new IngredientShopList(testNameIngredient, idCollection);

        Map<IngredientType, ArrayList<String>> groupedAmountType1 = new HashMap<>();
        Map<IngredientType, ArrayList<String>> groupedAmountType2 = new HashMap<>();
        Map<IngredientType, ArrayList<String>> groupedAmountTypeResult = new HashMap<>();
        groupedAmountType1.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));
        groupedAmountType1.putIfAbsent(IngredientType.MILLILITER, new ArrayList<>(List.of("100")));
        groupedAmountType2.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("50")));

        groupedAmountTypeResult.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200", "50")));
        groupedAmountTypeResult.putIfAbsent(IngredientType.MILLILITER, new ArrayList<>(List.of("100")));

        ingredientShopList1.setGroupedAmountType(groupedAmountType1);
        ingredientShopList2.setGroupedAmountType(groupedAmountType2);
        result.setGroupedAmountType(groupedAmountTypeResult);

        TestObserver<ArrayList<IngredientShopList>> testObserver = ingredientShopListRepository.groupIngredients(List.of(ingredientShopList1, ingredientShopList2), collection).test();
        testObserver
                .assertNoErrors()
                .assertValue(new ArrayList<>(List.of(result)));
    }

    @Test
    public void getByID() {
        Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
        groupedAmountType.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        IngredientShopList ingredientShopList3 = new IngredientShopList(3L, testNameIngredient + "3");
        IngredientShopList finalIngredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        finalIngredientShopList1.setGroupedAmountType(groupedAmountType);

        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, null);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, null);
        ArrayList<IngredientShopListAmountType> listAmountTypes = new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2));

        when(dao.getByID(ingredientShopList1.getId())).thenReturn(Maybe.just(ingredientShopList1));
        when(dao.getByID(ingredientShopList2.getId())).thenReturn(Maybe.empty());
        when(dao.getByID(ingredientShopList3.getId())).thenReturn(Maybe.error(new Throwable()));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList1.getId())).thenReturn(Single.just(listAmountTypes));

        TestObserver<IngredientShopList> testObserver = ingredientShopListRepository.getByID(ingredientShopList1.getId()).test();
        testObserver
                .assertNoErrors()
                .assertValue(finalIngredientShopList1);

        TestObserver<IngredientShopList> testObserver2 = ingredientShopListRepository.getByID(ingredientShopList2.getId()).test();
        testObserver2
                .assertNoErrors()
                .assertValue(new IngredientShopList());

        TestObserver<IngredientShopList> testObserver3 = ingredientShopListRepository.getByID(ingredientShopList3.getId()).test();
        testObserver3
                .assertNoErrors()
                .assertValue(new IngredientShopList());
    }

    @Test
    public void getByNameAndIDCollection() {
        Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
        groupedAmountType.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        IngredientShopList ingredientShopList3 = new IngredientShopList(3L, testNameIngredient + "3");
        IngredientShopList finalIngredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        finalIngredientShopList1.setGroupedAmountType(groupedAmountType);

        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, null);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, null);
        ArrayList<IngredientShopListAmountType> listAmountTypes = new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2));

        when(dao.getByNameAndIDCollection(ingredientShopList1.getName(), 1L)).thenReturn(Maybe.just(ingredientShopList1));
        when(dao.getByNameAndIDCollection(ingredientShopList2.getName(), 1L)).thenReturn(Maybe.empty());
        when(dao.getByNameAndIDCollection(ingredientShopList3.getName(), 1L)).thenReturn(Maybe.error(new Throwable()));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList1.getId())).thenReturn(Single.just(listAmountTypes));

        TestObserver<IngredientShopList> testObserver = ingredientShopListRepository.getByNameAndIDCollection(ingredientShopList1.getName(), 1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(finalIngredientShopList1);

        TestObserver<IngredientShopList> testObserver2 = ingredientShopListRepository.getByNameAndIDCollection(ingredientShopList2.getName(), 1L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(new IngredientShopList());

        TestObserver<IngredientShopList> testObserver3 = ingredientShopListRepository.getByNameAndIDCollection(ingredientShopList3.getName(), 1L).test();
        testObserver3
                .assertNoErrors()
                .assertValue(new IngredientShopList());
    }

    @Test
    public void createIngredientShopListAmountTypesFromGroupedAmountType() {
        Map<IngredientType, ArrayList<String>> groupedAmountType = new HashMap<>();
        groupedAmountType.putIfAbsent(IngredientType.GRAM, new ArrayList<>(List.of("100", "200")));

        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");
        ingredientShopList1.setGroupedAmountType(groupedAmountType);

        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, 1L);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, 1L);
        ArrayList<IngredientShopListAmountType> listAmountTypes = new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2));

        ArrayList<IngredientShopListAmountType> result1 = ingredientShopListRepository.createIngredientShopListAmountTypesFromGroupedAmountType(ingredientShopList1, 1L);
        ArrayList<IngredientShopListAmountType> result2 = ingredientShopListRepository.createIngredientShopListAmountTypesFromGroupedAmountType(ingredientShopList2, 1L);

        TestObserver<ArrayList<IngredientShopListAmountType>> testObserver = Single.just(result1).test();
        testObserver.assertValue(listAmountTypes);

        TestObserver<ArrayList<IngredientShopListAmountType>> testObserver2 = Single.just(result2).test();
        testObserver2.assertValue(new ArrayList<>());
    }

    @Test
    public void deleteAll() {
        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");

        when(dao.getAll()).thenReturn(Single.just(new ArrayList<>(List.of(ingredientShopList1))));
        when(dao.delete(ingredientShopList1)).thenReturn(Completable.complete());

        TestObserver<Boolean> testObserver = ingredientShopListRepository.deleteAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void deleteEmptyAmountTypeByIDCollection() {
        IngredientShopList ingredientShopList1 = new IngredientShopList(1L, testNameIngredient + "1");
        IngredientShopList ingredientShopList2 = new IngredientShopList(2L, testNameIngredient + "2");

        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, 1L);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, 1L);
        ArrayList<IngredientShopListAmountType> listAmountTypes = new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2));

        when(dao.getAllByIDCollection(1L)).thenReturn(Maybe.just(List.of(ingredientShopList1, ingredientShopList2)));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList1.getId())).thenReturn(Single.just(listAmountTypes));
        when(ingredientShopListAmountTypeRepository.getByIDIngredient(ingredientShopList2.getId())).thenReturn(Single.just(new ArrayList<>()));
        when(dao.delete(ingredientShopList1)).thenReturn(Completable.complete());
        when(dao.delete(ingredientShopList2)).thenReturn(Completable.complete());

        TestObserver<Boolean> testObserver = ingredientShopListRepository.deleteEmptyAmountTypeByIDCollection(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        verify(dao, never()).delete(ingredientShopList1);
        verify(dao).delete(ingredientShopList2);
    }
}

