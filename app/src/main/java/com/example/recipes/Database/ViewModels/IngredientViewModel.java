package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.IngredientDAO;
import com.example.recipes.Database.DAO.IngredientShopList_AmountTypeDAO;
import com.example.recipes.Item.Ingredient;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public class IngredientViewModel extends ViewModel {
    private final IngredientDAO dao;

    public IngredientViewModel(IngredientDAO dao) {
        this.dao = dao;
    }

    public LiveData<List<Ingredient>> getAllByIDDish(@NonNull Long id_dish) {
        return dao.getAllByIDDishLive(id_dish);
    }

    public LiveData<List<String>> getNamesUnique() {
        return dao.getNamesUniqueLive();
    }
}
