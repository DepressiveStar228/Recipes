package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientDAO;
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
     * @param idDish Ідентифікатор страви.
     * @return LiveData, яка містить список інгредієнтів для вказаної страви.
     */
    public LiveData<List<Ingredient>> getAllByIDDish(@NonNull Long idDish) {
        return dao.getAllByIDDishLive(idDish);
    }

    /**
     * Отримує список унікальних назв інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список унікальних назв інгредієнтів.
     */
    public LiveData<List<String>> getNamesUnique() {
        return dao.getNamesUniqueLive();
    }

    /**
     * Отримує список унікальних назв інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список унікальних назв інгредієнтів.
     */
    public LiveData<List<Ingredient>> getUnique() {
        return dao.getUniqueByNameLive();
    }
}
