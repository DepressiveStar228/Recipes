package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Database.DAO.IngredientShopListDAO;
import com.example.recipes.Item.Dish;

import java.util.List;

public class DishViewModel {
    public final DishDAO dao;

    public DishViewModel(DishDAO dao) {
        this.dao = dao;
    }

    public LiveData<List<Dish>> getAll() {
        return dao.getAll_Live();
    }

    public LiveData<Dish> getByID(Long id_dish) { return dao.getByIDLive(id_dish); }

    public LiveData<Integer> getCount() { return dao.getCountLive(); }
}
