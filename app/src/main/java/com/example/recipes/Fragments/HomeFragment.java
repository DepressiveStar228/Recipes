package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.recipes.Activity.EditorDishActivity;
import com.example.recipes.Activity.SearchDishActivity;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.R;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Фрагмент для пошуку страв у додатку.
 */
public class HomeFragment extends Fragment implements OnBackPressedListener {
    private EditText searchEditText;
    private CompositeDisposable compositeDisposable;
    private ImageView add_dish_button;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        preferences.edit().remove("scroll_position").apply();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_page, container, false);
        loadItemsActivity(view);
        loadClickListeners();
        return view;
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    /**
     * Ініціалізація UI елементів
     * @param view кореневий View фрагмента
     */
    private void loadItemsActivity(View view) {
        ConstraintLayout mainLayout = view.findViewById(R.id.mainLayout);
        ConstraintLayout searchDishLayout = view.findViewById(R.id.search_LinearLayout);
        if (searchDishLayout != null) searchEditText = searchDishLayout.findViewById(R.id.searchEditText);

        add_dish_button = mainLayout.findViewById(R.id.add_dish);

        Log.d("HomeFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    /**
     * Налаштування обробників подій
     */
    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners() {
        // Обробник кліку на поле пошуку
        if (searchEditText != null) {
            searchEditText.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(getContext(), SearchDishActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
        }

        // Обробники кнопок
        if (add_dish_button != null) {
            add_dish_button.setOnClickListener(v -> {
                if (v.getId() == R.id.add_dish) {
                    Intent intent = new Intent(getContext(), EditorDishActivity.class);
                    intent.putExtra(IntentKeys.DISH_ID.name(), -1);
                    startActivity(intent);
                }
            });
        }
        Log.d("HomeFragment", "Слухачі фрагмента успішно завантажені.");
    }
}