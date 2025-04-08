package com.example.recipes.Adapter.Interfaces;

import java.util.ArrayList;

public interface Search<T> {
    void setResultItems(ArrayList<T> items);
    ArrayList<T> getResultItems();
    void clearResultItems();

}
