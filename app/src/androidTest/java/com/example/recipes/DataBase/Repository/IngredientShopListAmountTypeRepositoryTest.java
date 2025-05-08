package com.example.recipes.DataBase.Repository;

import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.IngredientShopListAmountTypeDAO;
import com.example.recipes.Database.Repositories.IngredientShopListAmountTypeRepository;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.IngredientShopListAmountType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(AndroidJUnit4.class)
public class IngredientShopListAmountTypeRepositoryTest {
    private IngredientShopListAmountTypeRepository ingredientShopListAmountTypeRepository;

    @Mock
    private IngredientShopListAmountTypeDAO dao;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ingredientShopListAmountTypeRepository = new IngredientShopListAmountTypeRepository(ApplicationProvider.getApplicationContext(), dao);
    }

    @Test
    public void addAll() {
        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, 1L);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, 1L);
        IngredientShopListAmountType ingredientShopListAmountType3 = new IngredientShopListAmountType("300", IngredientType.GRAM, 1L, 1L);

        when(dao.insert(ingredientShopListAmountType1)).thenReturn(Single.just(1L));
        when(dao.insert(ingredientShopListAmountType2)).thenReturn(Single.just(2L));
        when(dao.insert(ingredientShopListAmountType3)).thenReturn(Single.just(-1L));

        TestObserver<Boolean> testObserver = ingredientShopListAmountTypeRepository.addAll(new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2))).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = ingredientShopListAmountTypeRepository.addAll(new ArrayList<>(List.of(ingredientShopListAmountType2, ingredientShopListAmountType3))).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void getByID() {
        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, 1L);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, 1L);

        when(dao.getByID(1L)).thenReturn(Maybe.just(ingredientShopListAmountType1));
        when(dao.getByID(2L)).thenReturn(Maybe.empty());

        TestObserver<IngredientShopListAmountType> testObserver = ingredientShopListAmountTypeRepository.getByID(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(ingredientShopListAmountType1);

        TestObserver<IngredientShopListAmountType> testObserver2 = ingredientShopListAmountTypeRepository.getByID(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(new IngredientShopListAmountType());
    }

    @Test
    public void getByIDIngredient() {
        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, 1L);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, 1L);

        when(dao.getByIDIngredient(1L)).thenReturn(Maybe.just(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2)));
        when(dao.getByIDIngredient(2L)).thenReturn(Maybe.empty());

        TestObserver<List<IngredientShopListAmountType>> testObserver = ingredientShopListAmountTypeRepository.getByIDIngredient(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(new ArrayList<>(List.of(ingredientShopListAmountType1, ingredientShopListAmountType2)));

        TestObserver<List<IngredientShopListAmountType>> testObserver2 = ingredientShopListAmountTypeRepository.getByIDIngredient(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(new ArrayList<>());
    }

    @Test
    public void deleteAll() {
        IngredientShopListAmountType ingredientShopListAmountType1 = new IngredientShopListAmountType("100", IngredientType.GRAM, 1L, 1L);
        IngredientShopListAmountType ingredientShopListAmountType2 = new IngredientShopListAmountType("200", IngredientType.GRAM, 1L, 1L);

        when(dao.delete(ingredientShopListAmountType1)).thenReturn(Completable.complete());
        when(dao.delete(ingredientShopListAmountType2)).thenReturn(Completable.error(new Throwable()));

        TestObserver<Boolean> testObserver = ingredientShopListAmountTypeRepository.deleteAll(new ArrayList<>(List.of(ingredientShopListAmountType1))).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = ingredientShopListAmountTypeRepository.deleteAll(new ArrayList<>(List.of(ingredientShopListAmountType2))).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }
}
