package com.example.recipes.DataBase.Repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.Repositories.IngredientRepository;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;


@RunWith(AndroidJUnit4.class)
public class IngredientRepositoryTest {
    private IngredientRepository ingredientRepository;
    private String testIngredientName = "Test Ingredient";

    @Mock
    private IngredientDAO ingredientDAO;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        ingredientRepository = new IngredientRepository(ApplicationProvider.getApplicationContext(), ingredientDAO);
    }

    @Test
    public void addAllOnlyIngredients() {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(testIngredientName + "1", "100", IngredientType.GRAM));
        ingredients.add(new Ingredient(testIngredientName + "2", "200", IngredientType.GRAM));

        when(ingredientDAO.insert(any(Ingredient.class))).thenReturn(Single.just(1L));

        TestObserver<Boolean> testObserver = ingredientRepository.addAll(ingredients).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void addAllIngredientsWithIdDish() {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(testIngredientName + "1", "100", IngredientType.GRAM));
        ingredients.add(new Ingredient(testIngredientName + "2", "200", IngredientType.GRAM));

        when(ingredientDAO.insert(any(Ingredient.class))).thenReturn(Single.just(1L));

        TestObserver<Boolean> testObserver = ingredientRepository.addAll(1L, ingredients).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void deleteAll() {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(testIngredientName + "1", "100", IngredientType.GRAM));
        ingredients.add(new Ingredient(testIngredientName + "2", "200", IngredientType.GRAM));

        when(ingredientDAO.delete(any(Ingredient.class))).thenReturn(new Completable() {
            @Override
            protected void subscribeActual(@NonNull CompletableObserver observer) {
                observer.onComplete();
            }
        });

        TestObserver<Boolean> testObserver = ingredientRepository.deleteAll(ingredients).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }
}
