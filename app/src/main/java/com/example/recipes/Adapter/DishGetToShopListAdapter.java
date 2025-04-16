package com.example.recipes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Item.Dish;
import com.example.recipes.R;

import java.util.ArrayList;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку страв, які використовуються у списку покупок.
 * Кожен елемент списку містить назву страви та кнопку для її видалення.
 * Використовує DiffUtil для ефективного оновлення списку.
 */
public class DishGetToShopListAdapter extends ListAdapter<Dish, DishGetToShopListAdapter.ViewHolder> {
    private DishClickListener clickListener;

    /**
     * Конструктор адаптера.
     *
     * @param clickListener Лістенер для обробки кліків на кнопку видалення.
     */
    public DishGetToShopListAdapter(DishClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.used_dish_in_shop_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;

        Dish dish = getItem(currentPosition);
        holder.dishName.setText(dish.getName());

        // Обробка кліку на кнопку видалення
        holder.delete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeleteClick(dish);
            }
        });
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

    /**
     * Callback для порівняння елементів списку за допомогою DiffUtil.
     */
    public static final DiffUtil.ItemCallback<Dish> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
                    boolean box = oldItem.getId() == newItem.getId();
                    return box;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Dish oldItem, @NonNull Dish newItem) {
                    boolean box = oldItem.equals(newItem);
                    return box;
                }
            };

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку страв.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dishName;
        ImageView delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishName = itemView.findViewById(R.id.dishNameItemTextView);
            delete = itemView.findViewById(R.id.deleteButton);
        }
    }

    /**
     * Інтерфейс для обробки кліків на кнопку видалення.
     */
    public interface DishClickListener {
        void onDeleteClick(Dish dish);
    }
}
