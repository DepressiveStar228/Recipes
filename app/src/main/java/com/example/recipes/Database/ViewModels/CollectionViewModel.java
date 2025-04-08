package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.IngredientShopList_AmountTypeDAO;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Item.Collection;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * ViewModel для роботи з даними колекцій.
 * Надає методи для отримання даних з бази даних через DAO.
 * Використовує LiveData для спостереження за змінами даних.
 */
public class CollectionViewModel extends ViewModel {
    private final CollectionDAO dao;

    public CollectionViewModel(CollectionDAO dao) {
        this.dao = dao;
    }

    /**
     * Отримує колекцію за її ідентифікатором.
     *
     * @param id_collection Ідентифікатор колекції.
     * @return LiveData, яке містить колекцію з вказаним ідентифікатором.
     */
    public LiveData<Collection> getCollectionByID(long id_collection) {
        return dao.getByID_Live(id_collection);
    }

    /**
     * Отримує всі колекції певного типу.
     *
     * @param type Тип колекції.
     * @return LiveData, яке містить список колекцій вказаного типу.
     */
    public LiveData<List<Collection>> getAllByType(CollectionType type) {
        return dao.getAllByTypeLive(type);
    }

    /**
     * Отримує кількість колекцій певного типу.
     *
     * @param type Тип колекції.
     * @return LiveData, яке містить кількість колекцій вказаного типу.
     */
    public LiveData<Integer> getCountByType(CollectionType type) {
        return dao.getCountByTypeLive(type);
    }
}
