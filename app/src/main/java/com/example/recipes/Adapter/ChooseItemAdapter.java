package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.Interfaces.ChooseItem;
import com.example.recipes.Adapter.Interfaces.Search;
import com.example.recipes.Interface.Item;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку об'єктів з можливістю вибору (чекбокси).
 * Підтримує роботу з об'єктами типу String або об'єктами, які реалізують інтерфейс Item.
 */
public class ChooseItemAdapter<T> extends ListAdapter<T, ChooseItemAdapter.ViewHolder> implements Search<T>, ChooseItem<T> {
    private Context context;
    private OnItemClickListener listener;
    private ArrayList<T> selectedItem = new ArrayList<>();

    /**
     * Конструктор адаптера.
     *
     * @param context Список об'єктів для відображення.
     * @param listener Лістенер для обробки кліків на елементи.
     */
    public ChooseItemAdapter(Context context, OnItemClickListener listener) {
        super(createDiffCallback());
        this.context = context;
        this.listener = listener;
    }

    /**
     * Інтерфейс для обробки кліків на елементи списку.
     */
    public interface OnItemClickListener<T> {
        void onItemClick(CheckBox checkBox, T item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_check_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ChooseItemAdapter.ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;

        T item = getItem(currentPosition);
        holder.bind(item);
        holder.item_check.setOnCheckedChangeListener(null);

        // Встановлюємо стан чекбоксу в залежності від того, чи елемент вибраний
        if (item instanceof String) {
            holder.item_check.setChecked(selectedItem.contains(item.toString()));
        } else {
            holder.item_check.setChecked(selectedItem.contains(item));
        }

        // Обробка кліку на елемент списку
        holder.item_layout.setOnClickListener(v -> {
            if (!holder.item_check.isChecked()) {
                selectedItem.add(item);
                holder.item_check.setChecked(true);
            } else {
                selectedItem.remove(item);
                holder.item_check.setChecked(false);
            }

            if (listener != null) {
                listener.onItemClick(holder.item_check, item);
            }
        });
    }

    @Override
    public void setItems(ArrayList<T> items) {
        submitList(new ArrayList<>(items));
    }

    @Override
    public void setResultItems(ArrayList<T> items) {
        setItems(items);
    }

    @Override
    public ArrayList<T> getResultItems() {
        return new ArrayList<>(getCurrentList());
    }

    @Override
    public void clearResultItems() {
        setItems(new ArrayList<>());
    }

    @Override
    public void setSelectedItems(ArrayList<T> selectedItems) {
        this.selectedItem.clear();
        this.selectedItem.addAll(selectedItems);
        notifyDataSetChanged();
    }

    public void setItemsAndSelected(ArrayList<T> items, ArrayList<T> selectedItems) {
        submitList(new ArrayList<>(items), () -> {
            this.selectedItem.clear();
            this.selectedItem.addAll(selectedItems);
            notifyDataSetChanged();
        });
    }


    @Override
    public ArrayList<T> getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void resetSelectionItems() {
        selectedItem.clear();
        notifyDataSetChanged();
    }

    public static <T> DiffUtil.ItemCallback<T> createDiffCallback() {
        return new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                if (oldItem instanceof Item && newItem instanceof Item) {
                    return ((Item) oldItem).getId() == ((Item) newItem).getId();
                } else {
                    return oldItem.hashCode() == newItem.hashCode();
                }
            }

            @Override
            public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
                return Objects.equals(oldItem, newItem);
            }
        };
    }

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout item_layout;
        TextView item_name;
        CheckBox item_check;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_layout = itemView.findViewById(R.id.box_checkItem);
            item_name = itemView.findViewById(R.id.dish_name_checkItem);
            item_check = itemView.findViewById(R.id.dish_check_checkItem);
        }

        /**
         * Прив'язує дані до елементів ViewHolder.
         *
         * @param item Об'єкт, який потрібно відобразити.
         */
        void bind(Object item) {
            if (item instanceof Item) {
                Item selectableItem = (Item) item;
                item_name.setText(selectableItem.getName());
            } else if (item instanceof String) {
                String name = item.toString();
                item_name.setText(name);
            }
        }
    }
}
