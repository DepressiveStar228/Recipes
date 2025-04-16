package com.example.recipes.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recipes.R;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Фрагмент, що відповідає за відображення головного меню додатка.
 * Цей фрагмент містить основні навігаційні елементи
 */
public class MainMenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_menu_panel, container, false);
    }
}
