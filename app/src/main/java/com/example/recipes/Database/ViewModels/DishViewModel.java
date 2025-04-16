package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Item.Dish;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас, який представляє ViewModel для роботи зі стравами.
 * Використовує DAO для отримання даних про страви.
 */
public class DishViewModel extends ViewModel {
    private final DishDAO dao;

    public DishViewModel(DishDAO dao) {
        this.dao = dao;
    }
    /**
     * Отримує список усіх страв у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх страв.
     */
    public LiveData<List<Dish>> getAll() {
        return dao.getAll_Live();
    }

    /**
     * Отримує страву за її ідентифікатором у вигляді LiveData.
     *
     * @param idDish Ідентифікатор страви.
     * @return LiveData, яка містить страву з вказаним ідентифікатором.
     */
    public LiveData<Dish> getByID(Long idDish) { return dao.getByIDLive(idDish); }

    /**
     * Отримує кількість страв у вигляді LiveData.
     *
     * @return LiveData, яка містить кількість страв.
     */
    public LiveData<Integer> getCount() { return dao.getCountLive(); }
}
