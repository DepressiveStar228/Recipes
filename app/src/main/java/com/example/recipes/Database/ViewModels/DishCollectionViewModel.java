package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.DishCollectionDAO;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * ViewModel для роботи з даними записів става-колекція.
 * Надає методи для отримання даних з бази даних через DAO.
 * Використовує LiveData для спостереження за змінами даних.
 */
public class DishCollectionViewModel extends ViewModel {
    private final DishCollectionDAO dao;

    public DishCollectionViewModel(DishCollectionDAO dao) {
        this.dao = dao;
    }

    /**
     * Отримує колекцію за її ідентифікатором.
     *
     * @return LiveData, яке містить кількість записів става-колекція у базі даних.
     */
    public LiveData<Integer> getCount() {
        return dao.getCountLive();
    }

    /**
     * Отримує ідентифікатори став за ідентифікатором колекції.
     *
     * @param idCollection Ідентифікатор колекції.
     * @return LiveData, яке містить ідентифікатори страв.
     */
    public LiveData<List<Long>> getAllDishIDs(long idCollection) {
        return dao.getAllDishIDsLive(idCollection);
    }

    /**
     * Отримує ідентифікатори колекцій за ідентифікатором страви.
     *
     * @param idDish Ідентифікатор страви.
     * @return LiveData, яке містить ідентифікатори колекцій.
     */
    public LiveData<List<Long>> getAllCollectionIDs(long idDish) {
        return dao.getAllIDsCollectionLive(idDish);
    }
}
