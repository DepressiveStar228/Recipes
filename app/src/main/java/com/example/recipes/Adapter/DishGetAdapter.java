package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.Interfaces.Search;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку страв з можливістю кліку на елементи та відображення додаткових опцій (меню).
 * Використовує DiffUtil для ефективного оновлення списку.
 */
public class DishGetAdapter extends ListAdapter<Dish, DishGetAdapter.ViewHolder> implements Search<Dish> {
    private final Context context;
    private PreferencesController preferencesController;
    private static String[] themeArray;
    private final DishClickListener commandClickListener;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param clickListener Лістенер для обробки кліків на елементи.
     */
    public DishGetAdapter(Context context, DishClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.commandClickListener = clickListener;
        preferencesController = PreferencesController.getInstance();
        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;

        Dish dish = getItem(currentPosition);
        holder.name.setText(dish.getName());
        holder.menu.setImageDrawable(context.getDrawable(R.drawable.icon_more));

        // Змінюємо колір іконки меню в залежності від теми
        if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
            holder.menu.setColorFilter(R.color.white);
        }

        // Обробка кліку на іконку меню
        holder.menu.setOnClickListener(v -> {
            if (commandClickListener != null) {
                commandClickListener.onDishMenuClick(dish, holder.menu);
            }
        });

        // Обробка кліку на елемент списку
        holder.itemView.setOnClickListener(v -> {
            if (commandClickListener != null) {
                commandClickListener.onDishClick(dish, v);
            }
        });
    }

    /**
     * Інтерфейс для обробки кліків на елементи списку страв.
     */
    public interface DishClickListener {
        void onDishClick(Dish item, View v);
        void onDishMenuClick(Dish item, View v);
    }

    /**
     * Оновлює список елементів у адаптері.
     *
     * @param newItems Новий список страв.
     */
    public void setItems(ArrayList<Dish> newItems) {
        ArrayList<Dish> newList = new ArrayList<>(newItems.size());
        for (Dish item : newItems) {
            newList.add(new Dish(item));
        }

        submitList(newList);
        notifyDataSetChanged();
    }

    @Override
    public void setResultItems(ArrayList<Dish> items) {
        setItems(items);
    }

    @Override
    public ArrayList<Dish> getResultItems() {
        return new ArrayList<>(getCurrentList());
    }

    @Override
    public void clearResultItems() {
        setItems(new ArrayList<>());
    }

    /**
     * Callback для порівняння елементів списку за допомогою DiffUtil.
     */
    public static final DiffUtil.ItemCallback<Dish> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку страв.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView menu;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.dish_name);
            menu = itemView.findViewById(R.id.menu_dish_imageView);
        }
    }
}
