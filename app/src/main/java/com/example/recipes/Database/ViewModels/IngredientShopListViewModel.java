package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Item.IngredientShopList;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас, який представляє ViewModel для роботи з інгредієнтами у списку покупок.
 * Використовує DAO для отримання даних про інгредієнти у списку покупок.
 */
public class IngredientShopListViewModel extends ViewModel {
    public final IngredientShopListDAO dao;

    public IngredientShopListViewModel(IngredientShopListDAO dao) {
        this.dao = dao;
    }

    /**
     * Отримує список усіх інгредієнтів у списку покупок у вигляді LiveData.
     *
     * @return LiveData, яка містить список усіх інгредієнтів у списку покупок.
     */
    public LiveData<List<IngredientShopList>> getAll() {
        return dao.getAllLive();
    }

    /**
     * Отримує кількість інгредієнтів у списку покупок за ідентифікатором колекції у вигляді LiveData.
     *
     * @param idCollection Ідентифікатор колекції.
     * @return LiveData, яка містить кількість інгредієнтів для вказаної колекції.
     */
    public LiveData<Integer> getCountByIdCollection(long idCollection) {
        return dao.getCountByIDShopListLive(idCollection);
    }

    /**
     * Отримує кількість придбаних інгредієнтів у списку покупок за ідентифікатором колекції у вигляді LiveData.
     *
     * @param idCollection Ідентифікатор колекції.
     * @return LiveData, яка містить кількість придбаних інгредієнтів для вказаної колекції.
     */
    public LiveData<Integer> getBoughtCountByIdCollection(long idCollection) {
        return dao.getBoughtCountByIDShopListLive(idCollection);
    }
}
