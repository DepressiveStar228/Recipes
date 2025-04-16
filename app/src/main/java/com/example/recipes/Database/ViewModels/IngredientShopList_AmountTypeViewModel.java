package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientShopList_AmountTypeDAO;
import com.example.recipes.Item.IngredientShopList_AmountType;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас, який представляє ViewModel для роботи з кількістю та типом інгредієнтів у списку покупок.
 * Використовує DAO для отримання даних про кількість та тип інгредієнтів.
 */
public class IngredientShopList_AmountTypeViewModel extends ViewModel {
    public final IngredientShopList_AmountTypeDAO dao;

    public IngredientShopList_AmountTypeViewModel(IngredientShopList_AmountTypeDAO dao) {
        this.dao = dao;
    }

    /**
     * Отримує список усіх записів про кількість та тип інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх записів.
     */
    public LiveData<List<IngredientShopList_AmountType>> getAll() {
        return dao.getAllLive();
    }

    /**
     * Отримує список усіх записів про кількість та тип інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх записів.
     */
    public LiveData<List<IngredientShopList_AmountType>> getByIDIngredient(long idIngredient) {
        return dao.getByIDIngredientLive(idIngredient);
    }

    /**
     * Отримує список усіх записів про кількість та тип інгредієнтів у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх записів.
     */
    public LiveData<List<IngredientShopList_AmountType>> getByIDDishLive(long idDish) {
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
