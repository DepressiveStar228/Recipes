package com.example.recipes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.Interfaces.Search;
import com.example.recipes.Interface.Item;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Універсальний адаптер для відображення результатів пошуку.
 *
 * @param <T> Тип елементів у списку результатів (може бути Item або String)
 */
public class SearchResultsAdapter<T> extends ListAdapter<T, SearchResultsAdapter.ViewHolder> implements Search<T> {
    private OnItemClickListener listener;

    /**
     * Конструктор адаптера.
     *
     * @param listener Лістенер для обробки кліків на елементи
     */
    public SearchResultsAdapter(OnItemClickListener listener) {
        super(createDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;

        T item = getItem(currentPosition);

        // Обробка різних типів даних
        if (item instanceof Item) {
            holder.textView.setText(((Item) item).getName());
            holder.textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(v, item));
        } else if (item instanceof String) {
            holder.textView.setText(item.toString());
            holder.textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(v, item));
        }
    }

    /**
     * Оновлює список елементів у адаптері.
     *
     * @param newItems Новий список страв.
     */
    public void setItems(ArrayList<T> newItems) {
        submitList(new ArrayList<>(newItems));
        notifyDataSetChanged();
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

    /**
     * Інтерфейс для обробки кліків на елементи списку.
     *
     * @param <T> Тип елементів у списку
     */
    public interface OnItemClickListener<T> {
        void onItemClick(View view, T item);
    }

    /**
     * Створює DiffUtil.ItemCallback для порівняння елементів списку.
     *
     * @param <T> Тип елементів у списку
     * @return Новий екземпляр DiffUtil.ItemCallback
     */
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
     * Внутрішній клас, що представляє ViewHolder для елементів списку.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.itemResultTextView);
        }
    }
}
