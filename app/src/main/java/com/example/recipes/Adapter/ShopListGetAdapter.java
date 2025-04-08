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

import com.example.recipes.Item.ShopList;
import com.example.recipes.R;

import java.util.ArrayList;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку покупок.
 */
public class ShopListGetAdapter extends ListAdapter<ShopList, ShopListGetAdapter.ViewHolder> {
    private CollectionClickListener collectionClickListener;

    /**
     * Конструктор адаптера.
     *
     * @param clickListener Лістенер для обробки кліків на елементи списку
     */
    public ShopListGetAdapter(CollectionClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.collectionClickListener = clickListener;
    }

    @NonNull
    @Override
    public ShopListGetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_list_item, parent, false);
        return new ShopListGetAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;

        // Встановлюємо дані у відповідні View
        ShopList shopList = getItem(currentPosition);
        holder.shop_list_name.setText(shopList.getName());
        holder.boughtItem.setText(String.valueOf(shopList.getAllBoughtItems()));
        holder.allItem.setText(String.valueOf(shopList.getAllItems()));

        // Обробка кліку на кнопку меню
        holder.menu_img.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onMenuIconClick(shopList, holder.menu_img);
            }
        });

        // Обробка кліку на весь елемент списку
        holder.itemView.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onCollectionClick(shopList);
            }
        });
    }

    /**
     * Оновлює список елементів у адаптері.
     *
     * @param items Новий список списків покупок
     */
    public void setItems(ArrayList<ShopList> items) {
        submitList(items);
    }

    /**
     * Внутрішній клас, що представляє ViewHolder для елементів списку.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView shop_list_name, boughtItem, allItem;
        ImageView menu_img;

        ViewHolder(View itemView) {
            super(itemView);
            shop_list_name = itemView.findViewById(R.id.shop_list_name);
            boughtItem = itemView.findViewById(R.id.numbBoughItem);
            allItem = itemView.findViewById(R.id.numbAllItem);
            menu_img = itemView.findViewById(R.id.menu_shop_list_imageView);
        }
    }

    /**
     * Інтерфейс для обробки кліків на елементи списку.
     */
    public interface CollectionClickListener {
        void onCollectionClick(ShopList collection);        // Викликається при кліку на елемент списку.
        void onMenuIconClick(ShopList collection, View v);  // Викликається при кліку на кнопку меню елемента.
    }

    /**
     * Callback для DiffUtil для порівняння елементів списку.
     */
    public static final DiffUtil.ItemCallback<ShopList> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ShopList oldItem, @NonNull ShopList newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ShopList oldItem, @NonNull ShopList newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
