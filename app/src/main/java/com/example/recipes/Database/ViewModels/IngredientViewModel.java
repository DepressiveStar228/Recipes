package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.DAO.IngredientShopList_AmountTypeDAO;
import com.example.recipes.Item.Ingredient;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас, який представляє ViewModel для роботи з інгредієнтами.
 * Використовує DAO для отримання даних про інгредієнти.
 */
public class IngredientViewModel extends ViewModel {
    private final IngredientDAO dao;

    public IngredientViewModel(IngredientDAO dao) {
        this.dao = dao;
    }

    /**
     * Отримує список інгредієнтів для конкретної страви за її ідентифікатором у вигляді LiveData.
     *
     * @param id_dish Ідентифікатор страви.
     * @return LiveData, яка містить список інгредієнтів для вказаної страви.
     */
    public LiveData<List<Ingredient>> getAllByIDDish(@NonNull Long id_dish) {
        return dao.getAllByIDDishLive(id_dish);
    }

    /**
     * Отримує список унікальних назв інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список унікальних назв інгредієнтів.
     */
    public LiveData<List<String>> getNamesUnique() {
        return dao.getNamesUniqueLive();
    }
}
