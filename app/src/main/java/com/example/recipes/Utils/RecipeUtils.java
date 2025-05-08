package com.example.recipes.Utils;

import android.content.Context;
import android.util.Log;

import com.example.recipes.Database.RecipeDatabase;
import com.example.recipes.Database.Repositories.CollectionRepository;
import com.example.recipes.Database.Repositories.DishCollectionRepository;
import com.example.recipes.Database.Repositories.DishRecipeRepository;
import com.example.recipes.Database.Repositories.DishRepository;
import com.example.recipes.Database.Repositories.IngredientRepository;
import com.example.recipes.Database.Repositories.IngredientShopListAmountTypeRepository;
import com.example.recipes.Database.Repositories.IngredientShopListRepository;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Утилітарний клас для роботи з БД.
 * Надає доступ до бази даних рецептів та реалізує патерн Singleton.
 */
public class RecipeUtils {
    private final Context context;
    private RecipeDatabase database;
    private static RecipeUtils instance;

    private DishRepository dishRepository;
    private IngredientRepository ingredientRepository;
    private CollectionRepository collectionRepository;
    private DishRecipeRepository dishRecipeRepository;
    private DishCollectionRepository dishCollectionRepository;
    private IngredientShopListRepository ingredientShopListRepository;
    private IngredientShopListAmountTypeRepository ingredientShopListAmountTypeRepository;

    /**
     * Конструктор класу RecipeUtils.
     *
     * @param context Контекст додатку для доступу до бази даних
     */
    public RecipeUtils(Context context) {
        this.context = context.getApplicationContext();
        try {
            database = RecipeDatabase.getInstance(this.context);
        } catch (Exception e) {
            Log.e("RecipeUtils", "База даних не створилась", e);
        }

        initializeRepositories();
        setDependencyRepository();
    }

    /**
     * Повертає єдиний екземпляр класу (Singleton).
     *
     * @param context Контекст додатку
     * @return Єдиний екземпляр RecipeUtils
     */
    public static synchronized RecipeUtils getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeUtils(context);
        }
        return instance;
    }

    /**
     * Ініціалізує репозиторії для роботи з базою даних.
     */
    private void initializeRepositories() {
        dishRepository = new DishRepository(context, database.dishDao());
        ingredientRepository = new IngredientRepository(context, database.ingredientDao());
        collectionRepository = new CollectionRepository(context, database.collectionDao());
        dishRecipeRepository = new DishRecipeRepository(context, database.dishRecipeDao());
        dishCollectionRepository = new DishCollectionRepository(context, database.dishCollectionDao());
        ingredientShopListRepository = new IngredientShopListRepository(context, database.ingredientShopListDao());
        ingredientShopListAmountTypeRepository = new IngredientShopListAmountTypeRepository(context, database.ingredientShopListAmountTypeDao());
    }

    /**
     * Встановлює залежності між репозиторіями.
     */
    private void setDependencyRepository() {
        dishRepository.setDependencies(collectionRepository, dishRecipeRepository, ingredientRepository, dishCollectionRepository);
        collectionRepository.setDependencies(dishRepository, dishCollectionRepository, ingredientShopListRepository);
        dishRecipeRepository.setDependencies(dishRepository);
        dishCollectionRepository.setDependencies(collectionRepository);
        ingredientShopListRepository.setDependencies(ingredientShopListAmountTypeRepository);
    }

    /**
     * Повертає екземпляр класу репозиторія страв.
     */
    public DishRepository ByDish() {
        return dishRepository;
    }

    /**
     * Повертає екземпляр класу репозиторія інгредієнтів.
     */
    public IngredientRepository ByIngredient() {
        return ingredientRepository;
    }

    /**
     * Повертає екземпляр класу репозиторія колекцій.
     */
    public CollectionRepository ByCollection() {
        return collectionRepository;
    }

    /**
     * Повертає екземпляр класу репозиторія страв та рецептів.
     */
    public DishRecipeRepository ByDishRecipe() {
        return dishRecipeRepository;
    }

    /**
     * Повертає екземпляр класу репозиторія зв'язків між стравами та колекціями.
     */
    public DishCollectionRepository ByDishCollection() {
        return dishCollectionRepository;
    }

    /**
     * Повертає екземпляр класу репозиторія інгредієнтів для списків покупкок.
     */
    public IngredientShopListRepository ByIngredientShopList() {
        return ingredientShopListRepository;
    }

    /**
     * Повертає екземпляр класу репозиторія типів кількостей інгредієнтів у списках покупок.
     */
    public IngredientShopListAmountTypeRepository ByIngredientShopList_AmountType() {
        return ingredientShopListAmountTypeRepository;
    }
}
