package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientShopListAmountTypeDAO;
import com.example.recipes.Item.IngredientShopListAmountType;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас, який представляє ViewModel для роботи з кількістю та типом інгредієнтів у списку покупок.
 * Використовує DAO для отримання даних про кількість та тип інгредієнтів.
 */
public class IngredientShopListAmountTypeViewModel extends ViewModel {
    public final IngredientShopListAmountTypeDAO dao;

    public IngredientShopListAmountTypeViewModel(IngredientShopListAmountTypeDAO dao) {
        this.dao = dao;
    }

    /**
     * Отримує список усіх записів про кількість та тип інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх записів.
     */
    public LiveData<List<IngredientShopListAmountType>> getAll() {
        return dao.getAllLive();
    }

    /**
     * Отримує список усіх записів про кількість та тип інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх записів.
     */
    public LiveData<List<IngredientShopListAmountType>> getByIDIngredient(long idIngredient) {
        return dao.getByIDIngredientLive(idIngredient);
    }

    /**
     * Отримує список усіх записів про кількість та тип інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх записів.
     */
    public LiveData<List<IngredientShopListAmountType>> getByIDDishLive(long idDish) {
        return dao.getByIDDishLive(idDish);
    }

    /**
     * Отримує список усіх записів про кількість та тип інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх записів.
     */
    public LiveData<Integer> getCountByID(long idIngredient) {
        return dao.getCountByID(idIngredient);
    }
}
