package com.example.recipes.Utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface Utils<T> {
    Single<Long> add(T item);
    Single<Boolean> addAll(ArrayList<T> items);
    Single<List<T>> getAll();
    Single<T> getByID(long id);
    Completable update(T item);
    Completable delete(T item);
    Single<Boolean> deleteAll(ArrayList<T> items);
}
