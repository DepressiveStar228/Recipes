package com.example.recipes.DataBase.Repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.Repositories.CollectionRepository;
import com.example.recipes.Database.Repositories.DishCollectionRepository;
import com.example.recipes.Database.Repositories.DishRecipeRepository;
import com.example.recipes.Database.Repositories.DishRepository;
import com.example.recipes.Database.Repositories.IngredientRepository;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;

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
public class DishRepositoryTest {
    private DishRepository dishRepository;
    private String testDishName = "Test Dish";
    private String testIngredientName = "Test Ingredient";
    private String testDishRecipeName = "Test Dish Recipe";
    private String testCollectionName = "Test Collection";

    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private DishRecipeRepository dishRecipeRepository;
    @Mock
    private DishCollectionRepository dishCollectionRepository;
    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private DishDAO dishDAO;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        dishRepository = new DishRepository(ApplicationProvider.getApplicationContext(), dishDAO);
        dishRepository.setDependencies(collectionRepository, dishRecipeRepository, ingredientRepository, dishCollectionRepository);

        when(collectionRepository.getByID(any(Long.class))).thenReturn(Single.just(new Collection(1L, testCollectionName, CollectionType.COLLECTION, new ArrayList<>())));
        when(ingredientRepository.addAll(any(Long.class), any(ArrayList.class))).thenReturn(Single.just(true));
        when(dishRecipeRepository.addAll(any(Dish.class), any(ArrayList.class))).thenReturn(Single.just(true));
        when(dishCollectionRepository.addAll(any(Dish.class), any(ArrayList.class))).thenReturn(Single.just(true));
        when(dishDAO.insert(any(Dish.class))).thenReturn(Single.just(1L));
        when(dishDAO.getIDByName(any(String.class))).thenReturn(Maybe.empty());
    }

    @Test
    public void addOnlyDish() {
        Dish dish = new Dish(testDishName);

        TestObserver<Long> testObserver = dishRepository.add(dish).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);
    }

    @Test
    public void addDishAndIDCollection() {
        Dish dish = new Dish(testDishName);
        long collectionID = 1L;

        TestObserver<Long> testObserver = dishRepository.add(dish, collectionID).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);
    }

    @Test
    public void addDishAndCollections() {
        Dish dish = new Dish(testDishName);

        ArrayList<Collection> collections = new ArrayList<>();
        collections.add(new Collection(1L,testCollectionName + "1", CollectionType.COLLECTION, new ArrayList<>()));
        collections.add(new Collection(2L,testCollectionName + "2", CollectionType.COLLECTION, new ArrayList<>()));

        TestObserver<Long> testObserver = dishRepository.add(dish, collections).test();
        testObserver
                .assertNoErrors()
                .assertValue(1L);
    }

    @Test
    public void addAllOnlyDishes() {
        ArrayList<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish(testDishName + "1"));
        dishes.add(new Dish(testDishName + "2"));

        when(dishDAO.getIDByName(any(String.class))).thenReturn(Maybe.empty());

        TestObserver<Boolean> testObserver = dishRepository.addAll(dishes).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void addAllDishesAndIDCollection() {
        ArrayList<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish(testDishName + "1"));
        dishes.add(new Dish(testDishName + "2"));

        Collection collection = new Collection(1L, testCollectionName, CollectionType.COLLECTION, new ArrayList<>());

        when(collectionRepository.getByID(any(Long.class))).thenReturn(Single.just(collection));
        when(ingredientRepository.addAll(any(Long.class), any(ArrayList.class))).thenReturn(Single.just(true));
        when(dishRecipeRepository.addAll(any(Dish.class), any(ArrayList.class))).thenReturn(Single.just(true));
        when(dishCollectionRepository.addAll(any(Dish.class), any(ArrayList.class))).thenReturn(Single.just(true));

        TestObserver<Boolean> testObserver = dishRepository.addAll(dishes, collection.getId()).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void getAll() {
        Ingredient ingredient = new Ingredient(1L, testIngredientName, "10", IngredientType.VOID, 1L);
        DishRecipe recipe = new DishRecipe(1L, 2L, testDishRecipeName, 1, DishRecipeType.TEXT);

        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);

        ArrayList<DishRecipe> recipes = new ArrayList<>();
        recipes.add(recipe);

        Dish dish1 = new Dish(1L, testDishName + "1", 1, ingredients, new ArrayList<>(), 0);
        Dish dish2 = new Dish(2L, testDishName + "2", 2, new ArrayList<>(), recipes, 0);

        when(ingredientRepository.getAllByIDDish(1L)).thenReturn(Single.just(ingredients));
        when(ingredientRepository.getAllByIDDish(2L)).thenReturn(Single.just(new ArrayList<>()));
        when(dishRecipeRepository.getByDishID(1L)).thenReturn(Single.just(new ArrayList<>()));
        when(dishRecipeRepository.getByDishID(2L)).thenReturn(Single.just(recipes));
        when(dishDAO.getByID(1L)).thenReturn(Maybe.just(dish1));
        when(dishDAO.getByID(2L)).thenReturn(Maybe.just(dish2));
        when(dishDAO.getAllIDs()).thenReturn(Single.just(List.of(1L, 2L)));

        TestObserver<List<Dish>> testObserver = dishRepository.getAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(dishes -> dishes.size() == 2 && dishes.contains(dish1) && dishes.contains(dish2));
    }

    @Test
    public void getAllWithIDs() {
        Ingredient ingredient = new Ingredient(1L, testIngredientName, "10", IngredientType.VOID, 1L);
        DishRecipe recipe = new DishRecipe(1L, 2L, testDishRecipeName, 1, DishRecipeType.TEXT);

        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);

        ArrayList<DishRecipe> recipes = new ArrayList<>();
        recipes.add(recipe);

        Dish dish1 = new Dish(1L, testDishName + "1", 1, ingredients, new ArrayList<>(), 0);
        Dish dish2 = new Dish(2L, testDishName + "2", 2, new ArrayList<>(), recipes, 0);

        when(ingredientRepository.getAllByIDDish(1L)).thenReturn(Single.just(ingredients));
        when(ingredientRepository.getAllByIDDish(2L)).thenReturn(Single.just(new ArrayList<>()));
        when(dishRecipeRepository.getByDishID(1L)).thenReturn(Single.just(new ArrayList<>()));
        when(dishRecipeRepository.getByDishID(2L)).thenReturn(Single.just(recipes));
        when(dishDAO.getByID(1L)).thenReturn(Maybe.just(dish1));
        when(dishDAO.getByID(2L)).thenReturn(Maybe.just(dish2));
        when(dishDAO.getAllIDs()).thenReturn(Single.just(List.of(1L, 2L)));

        ArrayList<Long> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);

        TestObserver<List<Dish>> testObserver = dishRepository.getAll(ids).test();
        testObserver
                .assertNoErrors()
                .assertValue(dishes -> dishes.size() == 2 && dishes.contains(dish1) && dishes.contains(dish2));
    }

    @Test
    public void getByID() {
        Ingredient ingredient = new Ingredient(1L, testIngredientName, "10", IngredientType.VOID, 1L);
        DishRecipe recipe = new DishRecipe(1L, 1L, testDishRecipeName, 1, DishRecipeType.TEXT);

        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);

        ArrayList<DishRecipe> recipes = new ArrayList<>();
        recipes.add(recipe);

        Dish dish = new Dish(1L, testDishName, 1, ingredients, recipes, 0);

        when(ingredientRepository.getAllByIDDish(any(Long.class))).thenReturn(Single.just(ingredients));
        when(dishRecipeRepository.getByDishID(any(Long.class))).thenReturn(Single.just(recipes));
        when(dishDAO.getByID(any(Long.class))).thenReturn(Maybe.just(dish));

        TestObserver<Dish> testObserver = dishRepository.getByID(1L).test();
        testObserver
                .assertNoErrors()
                .assertValue(dish);
    }

    @Test
    public void getCollection() {
        Dish dish = new Dish(1L, testDishName, 1, new ArrayList<>(), new ArrayList<>(), 0);

        ArrayList<Long> collectionIDs = new ArrayList<>();
        collectionIDs.add(1L);
        collectionIDs.add(2L);

        ArrayList<Collection> collections = new ArrayList<>();
        collections.add(new Collection(collectionIDs.get(0), testCollectionName + "1", CollectionType.COLLECTION, new ArrayList<>()));
        collections.add(new Collection(collectionIDs.get(1), testCollectionName + "2", CollectionType.COLLECTION, new ArrayList<>()));

        when(dishCollectionRepository.getAllIDsCollectionByIDDish(dish.getId())).thenReturn(Single.just(collectionIDs));
        when(collectionRepository.getByID(collectionIDs.get(0))).thenReturn(Single.just(collections.get(0)));
        when(collectionRepository.getByID(collectionIDs.get(1))).thenReturn(Single.just(collections.get(1)));

        TestObserver<ArrayList<Collection>> testObserver = dishRepository.getCollections(dish).test();
        testObserver
                .assertNoErrors()
                .assertValue(collections);
    }

    @Test
    public void getFilteredAndSorted() {
        RecipeUtils recipeUtils = new RecipeUtils(ApplicationProvider.getApplicationContext());

        ArrayList<String> ingredientNames = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String ingredientName = testIngredientName + (i + 1);
            ingredientNames.add(ingredientName);
        }

        Dish dish1 = new Dish(testDishName + 1, 1, new ArrayList<>(), new ArrayList<>(), 200L);
        Dish dish2 = new Dish(testDishName + 2, 2, new ArrayList<>(), new ArrayList<>(), 100L);
        Dish dish3 = new Dish(testDishName + 3, 3, new ArrayList<>(), new ArrayList<>(), 300L);
        ArrayList<Dish> dishes = new ArrayList<>(List.of(dish1, dish2, dish3));

        for (Dish dish : dishes) {
            long id = recipeUtils.ByDish().getDao().insert(dish).blockingGet();
            dish.setId(id);
        }

        Ingredient ingredient1 = new Ingredient(ingredientNames.get(0), "10", IngredientType.VOID, dishes.get(0).getId());
        Ingredient ingredient2 = new Ingredient(ingredientNames.get(3), "20", IngredientType.VOID, dishes.get(0).getId());
        Ingredient ingredient3 = new Ingredient(ingredientNames.get(0), "30", IngredientType.VOID, dishes.get(1).getId());
        Ingredient ingredient4 = new Ingredient(ingredientNames.get(1), "40", IngredientType.VOID, dishes.get(1).getId());
        Ingredient ingredient5 = new Ingredient(ingredientNames.get(2), "50", IngredientType.VOID, dishes.get(2).getId());

        ArrayList<Ingredient> ingredients = new ArrayList<>(List.of(ingredient1, ingredient2, ingredient3, ingredient4, ingredient5));

        for (Ingredient ingredient : ingredients) {
            long id = recipeUtils.ByIngredient().getDao().insert(ingredient).blockingGet();
            ingredient.setId(id);
        }

        ArrayList<Boolean> sortNullableOptions = new ArrayList<>();
        sortNullableOptions.add(null);
        sortNullableOptions.add(null);

        ArrayList<Boolean> sortOptionsTest1 = new ArrayList<>(); // Сортування за часов від найстарішого без алфавіту
        sortOptionsTest1.add(null);
        sortOptionsTest1.add(true);

        ArrayList<Boolean> sortOptionsTest2 = new ArrayList<>(); // Сортування за часов від найновішого без алфавіту
        sortOptionsTest2.add(null);
        sortOptionsTest2.add(false);

        ArrayList<Boolean> sortOptionsTest3 = new ArrayList<>(); // Сортування за алфавітом без часу
        sortOptionsTest3.add(true);
        sortOptionsTest3.add(null);

        ArrayList<Boolean> sortOptionsTest4 = new ArrayList<>(); // Сортування проти алфавіта без часу
        sortOptionsTest4.add(false);
        sortOptionsTest4.add(null);

        ArrayList<Boolean> sortOptionsTest5 = new ArrayList<>(List.of(true, true)); // Сортування за алфавітом від найстарішого
        ArrayList<Boolean> sortOptionsTest6 = new ArrayList<>(List.of(true, false)); // Сортування за алфавітом від найновішого
        ArrayList<Boolean> sortOptionsTest7 = new ArrayList<>(List.of(false, true)); // Сортування проти алфавіта від найстарішого
        ArrayList<Boolean> sortOptionsTest8 = new ArrayList<>(List.of(false, false)); // Сортування проти алфавіта від найновішого

        //
        // Тести сортування без фільтрації по інгредієнтам
        //
        TestObserver<List<Dish>> testObserver1 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest1).test();
        testObserver1
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(2)) &&
                        loadedDishes.get(1).equals(dishes.get(0)) &&
                        loadedDishes.get(2).equals(dishes.get(1)));

        TestObserver<List<Dish>> testObserver2 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(1)) &&
                        loadedDishes.get(1).equals(dishes.get(0)) &&
                        loadedDishes.get(2).equals(dishes.get(2)));

        TestObserver<List<Dish>> testObserver3 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest3).test();
        testObserver3
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(0)) &&
                        loadedDishes.get(1).equals(dishes.get(1)) &&
                        loadedDishes.get(2).equals(dishes.get(2)));

        TestObserver<List<Dish>> testObserver4 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest4).test();
        testObserver4
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(2)) &&
                        loadedDishes.get(1).equals(dishes.get(1)) &&
                        loadedDishes.get(2).equals(dishes.get(0)));

        TestObserver<List<Dish>> testObserver5 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest5).test();
        testObserver5
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(2)) &&
                        loadedDishes.get(1).equals(dishes.get(0)) &&
                        loadedDishes.get(2).equals(dishes.get(1)));

        TestObserver<List<Dish>> testObserver6 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest6).test();
        testObserver6
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(1)) &&
                        loadedDishes.get(1).equals(dishes.get(0)) &&
                        loadedDishes.get(2).equals(dishes.get(2)));

        TestObserver<List<Dish>> testObserver7 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest5).test();
        testObserver7
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(2)) &&
                                loadedDishes.get(1).equals(dishes.get(0)) &&
                                loadedDishes.get(2).equals(dishes.get(1)));

        TestObserver<List<Dish>> testObserver8 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(), sortOptionsTest6).test();
        testObserver8
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 3)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(1)) &&
                                loadedDishes.get(1).equals(dishes.get(0)) &&
                                loadedDishes.get(2).equals(dishes.get(2)));


        //
        // Тести фільтрації по інгредієнтам без сортування
        //
        TestObserver<List<Dish>> testObserver9 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(List.of(ingredientNames.get(0))), sortNullableOptions).test();
        testObserver9
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 2)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(0)) &&
                        loadedDishes.get(1).equals(dishes.get(1)));

        TestObserver<List<Dish>> testObserver10 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(List.of(ingredientNames.get(0), ingredientNames.get(1))), sortNullableOptions).test();
        testObserver10
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 1)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(1)));

        TestObserver<List<Dish>> testObserver11 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(List.of(ingredientNames.get(0), ingredientNames.get(1), ingredientNames.get(2))), sortNullableOptions).test();
        testObserver11
                .assertNoErrors()
                .assertValue(List::isEmpty);


        //
        // Тести фільтрації по інгредієнтам з сортуванням
        //
        TestObserver<List<Dish>> testObserver12 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(List.of(ingredientNames.get(0))), sortOptionsTest1).test();
        testObserver12
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 2)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(0)) &&
                        loadedDishes.get(1).equals(dishes.get(1)));

        TestObserver<List<Dish>> testObserver13 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(List.of(ingredientNames.get(0))), sortOptionsTest2).test();
        testObserver13
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 2)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(1)) &&
                        loadedDishes.get(1).equals(dishes.get(0)));

        TestObserver<List<Dish>> testObserver14 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(List.of(ingredientNames.get(0))), sortOptionsTest3).test();
        testObserver14
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 2)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(0)) &&
                        loadedDishes.get(1).equals(dishes.get(1)));

        TestObserver<List<Dish>> testObserver15 = recipeUtils.ByDish().getFilteredAndSorted(new ArrayList<>(List.of(ingredientNames.get(0))), sortOptionsTest4).test();
        testObserver15
                .assertNoErrors()
                .assertValue(loadedDishes -> loadedDishes.size() == 2)
                .assertValue(loadedDishes ->
                        loadedDishes.get(0).equals(dishes.get(1)) &&
                        loadedDishes.get(1).equals(dishes.get(0)));
    }

    @Test
    public void getUniqueName() {
        String testName1 = testDishName + "1";
        String testName2 = testDishName + "2";
        String testName3 = testDishName + "3";

        when(dishDAO.getIDByName(testName1)).thenReturn(Maybe.just(1L));
        when(dishDAO.getIDByName(testName2)).thenReturn(Maybe.empty());
        when(dishDAO.getIDByName(testName3)).thenReturn(Maybe.just(1L));
        when(dishDAO.getIDByName(testName3 + " №2")).thenReturn(Maybe.just(1L));

        TestObserver<String> testObserver = dishRepository.getUniqueName(testName1).test();
        testObserver
                .assertNoErrors()
                .assertValue(name -> name.equals(testName1 + " №2"));

        TestObserver<String> testObserver2 = dishRepository.getUniqueName(testName2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(name -> name.equals(testName2));

        TestObserver<String> testObserver3 = dishRepository.getUniqueName(testName3).test();
        testObserver3
                .assertNoErrors()
                .assertValue(name -> name.equals(testName3 + " №3"));
    }

    @Test
    public void checkDuplicateName() {
        String testName1 = testDishName + "1";
        String testName2 = testDishName + "2";

        when(dishDAO.getIDByName(testName1)).thenReturn(Maybe.just(1L));
        when(dishDAO.getIDByName(testName2)).thenReturn(Maybe.empty());

        TestObserver<Boolean> testObserver = dishRepository.checkDuplicateName(testName1).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);

        TestObserver<Boolean> testObserver2 = dishRepository.checkDuplicateName(testName2).test();
        testObserver2
                .assertNoErrors()
                .assertValue(false);
    }

    @Test
    public void deleteAllByArray() {
        ArrayList<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish(testDishName + "1"));
        dishes.add(new Dish(testDishName + "2"));

        when(dishDAO.delete(any(Dish.class))).thenReturn(new Completable() {
            @Override
            protected void subscribeActual(@NonNull CompletableObserver observer) {
                observer.onComplete();
            }
        });

        TestObserver<Boolean> testObserver = dishRepository.deleteAll(dishes).test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }

    @Test
    public void deleteAllByDB() {
        ArrayList<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish(testDishName + "1"));
        dishes.add(new Dish(testDishName + "2"));

        when(dishDAO.getAll()).thenReturn(Single.just(dishes));
        when(dishDAO.delete(any(Dish.class))).thenReturn(new Completable() {
            @Override
            protected void subscribeActual(@NonNull CompletableObserver observer) {
                observer.onComplete();
            }
        });

        TestObserver<Boolean> testObserver = dishRepository.deleteAll().test();
        testObserver
                .assertNoErrors()
                .assertValue(true);
    }
}
