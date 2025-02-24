package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;

import com.example.recipes.Database.DAO.CollectionDAO;
import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;

import java.util.List;

public class CollectionViewModel {
    public final CollectionDAO dao;

    public CollectionViewModel(CollectionDAO dao) {
        this.dao = dao;
    }

    public LiveData<Collection> getCollectionByID(long id_collection) {
        return dao.getByID_Live(id_collection);
    }

    public LiveData<List<Collection>> getAllByType(String type) {
        return dao.getAllByTypeLive(type);
    }
}
