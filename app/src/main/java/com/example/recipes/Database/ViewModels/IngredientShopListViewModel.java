package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientShopListDAO;

public class IngredientShopListViewModel extends ViewModel {
    public final IngredientShopListDAO dao;

    public IngredientShopListViewModel(IngredientShopListDAO dao) {
        this.dao = dao;
    }

    public LiveData<Integer> getIngredientShopListCountByIdCollection(long id_collection) {
        return dao.getIngredientShopListCountByIdCollection(id_collection);
    }

    public LiveData<Integer> getBoughtIngredientShopListCountByIdCollection(long id_collection) {
        return dao.getBoughtIngredientShopListCountByIdCollection(id_collection);
    }
}
