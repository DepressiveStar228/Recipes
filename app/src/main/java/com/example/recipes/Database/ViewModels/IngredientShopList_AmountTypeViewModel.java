package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientShopList_AmountTypeDAO;
import com.example.recipes.Item.IngredientShopList_AmountType;

import java.util.List;

public class IngredientShopList_AmountTypeViewModel extends ViewModel {
    public final IngredientShopList_AmountTypeDAO dao;

    public IngredientShopList_AmountTypeViewModel(IngredientShopList_AmountTypeDAO dao) {
        this.dao = dao;
    }

    public LiveData<List<IngredientShopList_AmountType>> getAll() {
        return dao.getAllLive();
    }

    public LiveData<List<IngredientShopList_AmountType>> getByIDIngredient(long id_ingredient) {
        return dao.getByIDIngredientLive(id_ingredient);
    }

    public LiveData<List<IngredientShopList_AmountType>> getByIDDishLive(long id_dish) {
        return dao.getByIDDishLive(id_dish);
    }

    public LiveData<Integer> getCountByID(long id_ingredient) {
        return dao.getCountByID(id_ingredient);
    }
}
