package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Item.IngredientShopList;

import java.util.List;

public class IngredientShopListViewModel extends ViewModel {
    public final IngredientShopListDAO dao;

    public IngredientShopListViewModel(IngredientShopListDAO dao) {
        this.dao = dao;
    }

    public LiveData<List<IngredientShopList>> getAllByIdCollection(long id_collection) {
        return dao.getAllByIDCollection_Live(id_collection);
    }

    public LiveData<Integer> getCountByIdCollection(long id_collection) {
        return dao.getCountByIdCollection_Live(id_collection);
    }

    public LiveData<Integer> getBoughtCountByIdCollection(long id_collection) {
        return dao.getBoughtCountByIdCollection_Live(id_collection);
    }
}
