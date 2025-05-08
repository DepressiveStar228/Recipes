package com.example.recipes.DataBase.Repository;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Controller.ImageController;
import com.example.recipes.Database.DAO.DishRecipeDAO;
import com.example.recipes.Database.Repositories.DishRecipeRepository;
import com.example.recipes.Database.Repositories.DishRepository;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;

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
public class DishRecipeRepositoryTest {
    private DishRecipeRepository dishRecipeRepository;
    private String testDishName = "Test Dish";
    private String testRecipe = "Test Recipe";

    @Mock
    private DishRecipeDAO dao;
    @Mock
    private DishRepository dishRepository;
    @Mock
    private ImageController imageController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        dishRecipeRepository = new DishRecipeRepository(ApplicationProvider.getApplicationContext(), dao);
        dishRecipeRepository.setDependencies(dishRepository);
        dishRecipeRepository.setImageController(imageController);
    }

    @Test
    public void addAll() {
        Dish dish = new Dish(1L, testRecipe, 1);

        DishRecipe dishRecipe1 = new DishRecipe(dish.getId(), 1, DishRecipeType.TEXT);
        DishRecipe dishRecipe2 = new DishRecipe(dish.getId(), 2, DishRecipeType.TEXT);
        DishRecipe dishRecipe3 = new DishRecipe(dish.getId(), 3, DishRecipeType.TEXT);
        DishRecipe dishRecipe4 = new DishRecipe(dish.getId(), 4, DishRecipeType.IMAGE);
        DishRecipe dishRecipe5 = new DishRecipe(0L, 5, DishRecipeType.TEXT);

        ArrayList<DishRecipe> dishRecipes1 = new ArrayList<>(List.of(dishRecipe1, dishRecipe2));
        ArrayList<DishRecipe> dishRecipes2 = new ArrayList<>(List.of(dishRecipe2, dishRecipe3));
        ArrayList<DishRecipe> dishRecipes3 = new ArrayList<>(List.of(dishRecipe1, dishRecipe4));
        ArrayList<DishRecipe> dishRecipes4 = new ArrayList<>(List.of(dishRecipe1, dishRecipe5));

        when(dishRepository.getByID(1L)).thenReturn(Single.just(dish));
        when(dao.insert(dishRecipe1)).thenReturn(Single.just(1L));
        when(dao.insert(dishRecipe2)).thenReturn(Single.just(2L));
        when(dao.insert(dishRecipe3)).thenReturn(Single.just(0L));
        when(dao.insert(dishRecipe4)).thenReturn(Single.just(4L));
        when(dao.insert(dishRecipe5)).thenReturn(Single.just(5L));

        TestObserver<Boolean> testObserver = dishRecipeRepository.addAll(dishRecipes1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver1 = dishRecipeRepository.addAll(dishRecipes2).test();
        testObserver1
                .assertNoErrors()
                .assertValue(false);

        TestObserver<Boolean> testObserver2 = dishRecipeRepository.addAll(dishRecipes3).test();
        testObserver2
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver3 = dishRecipeRepository.addAll(dishRecipes4).test();
        testObserver3
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void addAllWithDish() {
        Dish dish1 = new Dish(1L, testRecipe + "1", 1);
        Dish dish2 = new Dish(0L, testRecipe + "2", 1);
        Dish dish3 = new Dish(3L, "", 1);

        DishRecipe dishRecipe1 = new DishRecipe(dish1.getId(), 1, DishRecipeType.TEXT);
        DishRecipe dishRecipe2 = new DishRecipe(dish1.getId(), 2, DishRecipeType.TEXT);
        DishRecipe dishRecipe3 = new DishRecipe(dish1.getId(), 3, DishRecipeType.IMAGE);
        DishRecipe dishRecipe4 = new DishRecipe(dish1.getId(), 4, DishRecipeType.IMAGE);

        ArrayList<DishRecipe> dishRecipes1 = new ArrayList<>(List.of(dishRecipe1, dishRecipe2));
        ArrayList<DishRecipe> dishRecipes2 = new ArrayList<>(List.of(dishRecipe1, dishRecipe3));
        ArrayList<DishRecipe> dishRecipes3 = new ArrayList<>(List.of(dishRecipe2, dishRecipe4));

        when(dao.insert(dishRecipe1)).thenReturn(Single.just(1L));
        when(dao.insert(dishRecipe2)).thenReturn(Single.just(2L));
        when(dao.insert(dishRecipe3)).thenReturn(Single.just(3L));
        when(dao.insert(dishRecipe4)).thenReturn(Single.just(0L));

        TestObserver<Boolean> testObserver = dishRecipeRepository.addAll(dish1, dishRecipes1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);


        TestObserver<Boolean> testObserver2 = dishRecipeRepository.addAll(dish1, dishRecipes2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver3 = dishRecipeRepository.addAll(dish1, dishRecipes3).test();
        testObserver3
                .assertNoErrors()
                .assertValue(false);

        TestObserver<Boolean> testObserver4 = dishRecipeRepository.addAll(dish2, dishRecipes1).test();
        testObserver4
                .assertNoErrors()
                .assertValue(false);

        TestObserver<Boolean> testObserver5 = dishRecipeRepository.addAll(dish3, dishRecipes1).test();
        testObserver5
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void getByID() {
        DishRecipe dishRecipe = new DishRecipe(testRecipe, 1, DishRecipeType.TEXT);

        when(dao.getByID(1L)).thenReturn(Maybe.just(dishRecipe));
        when(dao.getByID(2L)).thenReturn(Maybe.empty());

        TestObserver<DishRecipe> testObserver = dishRecipeRepository.getByID(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(dishRecipe);

        TestObserver<DishRecipe> testObserver2 = dishRecipeRepository.getByID(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(new DishRecipe());
    }

    @Test
    public void getByDishID() {
        DishRecipe dishRecipe = new DishRecipe(testRecipe, 1, DishRecipeType.TEXT);

        when(dao.getByDishID(1L)).thenReturn(Maybe.just(new ArrayList<>(List.of(dishRecipe))));
        when(dao.getByDishID(2L)).thenReturn(Maybe.empty());

        TestObserver<List<DishRecipe>> testObserver = dishRecipeRepository.getByDishID(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(List.of(dishRecipe));

        TestObserver<List<DishRecipe>> testObserver2 = dishRecipeRepository.getByDishID(2L).test();
        testObserver2
                .assertNoErrors()
                .assertValue(new ArrayList<>());
    }

    @Test
    public void saveImage() {
        Dish dish = new Dish(testDishName);

        DishRecipe dishRecipe1 = new DishRecipe(testRecipe, 1, DishRecipeType.IMAGE);
        DishRecipe finalDishRecipe1 = new DishRecipe("URL", 1, DishRecipeType.IMAGE);
        DishRecipe dishRecipe2 = new DishRecipe(testRecipe, 2, DishRecipeType.TEXT);

        byte[] mockImageBytes = new byte[]{1, 2, 3, 4};
        ImageController imageController1 = new ImageController(ApplicationProvider.getApplicationContext());
        Bitmap bitmap = imageController1.convertDrawbleToBitmap(AnotherUtils.getDrawable(ApplicationProvider.getApplicationContext(), R.drawable.icon_add));

        when(imageController.getBiteArrayImageFromPath(dishRecipe1.getTextData())).thenReturn(Single.just(mockImageBytes));
        when(imageController.decodeByteArrayToBitmap(mockImageBytes)).thenReturn(Single.just(bitmap));
        when(imageController.saveImageToInternalStorage(dish.getName(), bitmap)).thenReturn(Single.just("URL"));

        TestObserver<DishRecipe> testObserver = dishRecipeRepository.saveImage(dish.getName(), dishRecipe1).test();
        testObserver
                .assertNoErrors()
                .assertValue(finalDishRecipe1);

        TestObserver<DishRecipe> testObserver2 = dishRecipeRepository.saveImage(dish.getName(), dishRecipe2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(dishRecipe2);
    }

    @Test
    public void delete() {
        DishRecipe dishRecipe = new DishRecipe(testRecipe, 1, DishRecipeType.IMAGE);

        doNothing().when(imageController).deleteFileByUri(testRecipe);
        when(dao.delete(dishRecipe)).thenReturn(Completable.complete());

        TestObserver<Void> testObserver = dishRecipeRepository.delete(dishRecipe).test();
        testObserver.assertNoErrors();
    }

    @Test
    public void deleteAll() {
        DishRecipe dishRecipe1 = new DishRecipe(testRecipe + "1", 1, DishRecipeType.IMAGE);
        DishRecipe dishRecipe2 = new DishRecipe(testRecipe + "2", 2, DishRecipeType.TEXT);
        DishRecipe dishRecipe3 = new DishRecipe(testRecipe + "3", 3, DishRecipeType.TEXT);
        ArrayList<DishRecipe> dishRecipes1 = new ArrayList<>(List.of(dishRecipe1, dishRecipe2));
        ArrayList<DishRecipe> dishRecipes2 = new ArrayList<>(List.of(dishRecipe2, dishRecipe3));

        doNothing().when(imageController).deleteFileByUri(testRecipe + "1");
        when(dao.delete(dishRecipe1)).thenReturn(Completable.complete());
        when(dao.delete(dishRecipe2)).thenReturn(Completable.complete());
        when(dao.delete(dishRecipe3)).thenReturn(Completable.error(new Throwable()));

        TestObserver<Boolean> testObserver = dishRecipeRepository.deleteAll(dishRecipes1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishRecipeRepository.deleteAll(dishRecipes2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }
}
