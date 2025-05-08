package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.DishRecipeDAO;
import com.example.recipes.Item.DishRecipe;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * ViewModel для роботи з даними рецептів страв.
 * Надає методи для отримання даних з бази даних через DAO.
 * Використовує LiveData для спостереження за змінами даних.
 */
public class DishRecipeViewModel extends ViewModel {
    private final DishRecipeDAO dao;

    public DishRecipeViewModel(DishRecipeDAO dao) {
        this.dao = dao;
    }

    /**
     * Отримує список усіх рецептів страв у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх рецептів страв.
     */
    public LiveData<List<DishRecipe>> getAll() {
        return dao.getAllLive();
    }

    /**
     * Отримує рецепт страви за її ідентифікатором у вигляді LiveData.
     *
     * @param idDish Ідентифікатор страви.
     * @return LiveData, яка містить рецепт страви з вказаним ідентифікатором.
     */
    public LiveData<DishRecipe> getByID(Long idDish) { return dao.getByIDLive(idDish); }

    /**
     * Отримує список рецептів страв за ідентифікатором страви у вигляді LiveData.
     *
     * @param idDish Ідентифікатор страви.
     * @return LiveData, яка містить список рецептів страв для вказаного ідентифікатора страви.
     */
    public LiveData<List<DishRecipe>> getByDishID(Long idDish) { return dao.getByDishIDLive(idDish); }

    /**
     * Отримує кількість рецептів страв у вигляді LiveData.
     *
     * @return LiveData, яка містить кількість рецептів страв.
     */
    public LiveData<Integer> getCount() { return dao.getCountLive(); }
}
