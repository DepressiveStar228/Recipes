package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishCollectionDAO;
import com.example.recipes.Item.Collection;

import java.util.List;

public class DishCollectionViewModel {
    public final DishCollectionDAO dao;

    public DishCollectionViewModel(DishCollectionDAO dao) {
        this.dao = dao;
    }

    public LiveData<List<Long>> getAllDishIDs(long id_collection) {
        return dao.getAllDishIDsLive(id_collection);
    }
}
