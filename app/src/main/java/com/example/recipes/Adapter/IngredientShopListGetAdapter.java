package com.example.recipes.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;

import java.util.ArrayList;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку інгредієнтів у списку покупок
 */
public class IngredientShopListGetAdapter extends ListAdapter<IngredientShopList, IngredientShopListGetAdapter.ViewHolder> {
    private final Context context;
    private int boughtItem, unBoughtItem; // Кольори для позначення куплених та некуплених інгредієнтів
    private final IngredientShopListClickListener clickListener;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param clickListener Слухач для обробки кліків на елементи.
     */
    public IngredientShopListGetAdapter(Context context, IngredientShopListClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.clickListener = clickListener;
        getColors(); // Отримуємо кольори для відображення
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ingredient_shop_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;
        IngredientShopList ingredientShopList = getItem(currentPosition);

        // Встановлюємо стан чекбоксу (куплено/не куплено)
        holder.isBuy.setChecked(ingredientShopList.getIsBuy());
        holder.ingredientName.setText(ingredientShopList.getName());
        holder.ingredientCountType.setText(ingredientShopList.getGroupedAmountTypeToString(context));
        updateStrikeThrough(holder.line, ingredientShopList.getIsBuy());

        // Встановлюємо кольори в залежності від стану (куплено/не куплено)
        if (ingredientShopList.getIsBuy()) {
            holder.ingredientName.setTextColor(boughtItem);
            holder.ingredientCountType.setTextColor(boughtItem);
            holder.delete.setColorFilter(boughtItem);
        } else {
            holder.ingredientName.setTextColor(unBoughtItem);
            holder.ingredientCountType.setTextColor(unBoughtItem);
            holder.delete.setColorFilter(unBoughtItem);
        }

        // Обробка кліку на елемент списку
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onIngredientShopListClick(ingredientShopList);
            }
        });

        // Обробка кліку на кнопку видалення
        holder.delete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeleteClick(ingredientShopList);
            }
        });
    }

    /**
     * Оновлює список елементів у адаптері.
     *
     * @param newItems Новий список інгредієнтів.
     */
    public void setItems(ArrayList<IngredientShopList> newItems) {
        ArrayList<IngredientShopList> newList = new ArrayList<>(newItems.size());
        for (IngredientShopList item : newItems) {
            newList.add(new IngredientShopList(item));
        }

        submitList(newList);
        notifyDataSetChanged();
    }

    /**
     * Оновлює лінію закреслення в залежності від стану (куплено/не куплено).
     *
     * @param line Виджет лінії закреслення.
     * @param isStriked Чи потрібно відображати лінію.
     */
    private void updateStrikeThrough(View line, boolean isStriked) {
        line.setVisibility(isStriked ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Отримує кольори для відображення куплених та некуплених інгредієнтів.
     */
    @SuppressLint("ResourceType")
    private void getColors() {
        boughtItem = context.getResources().getColor(R.color.grey2, context.getTheme());

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorText, typedValue, true);
        unBoughtItem = typedValue.data;
    }

    /**
     * Callback для порівняння елементів списку за допомогою DiffUtil.
     */
    public static final DiffUtil.ItemCallback<IngredientShopList> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull IngredientShopList oldItem, @NonNull IngredientShopList newItem) {
                    boolean box = oldItem.getId() == newItem.getId();
                    return box;
                }

                @Override
                public boolean areContentsTheSame(@NonNull IngredientShopList oldItem, @NonNull IngredientShopList newItem) {
                    boolean box = oldItem.equals(newItem);
                    return box;
                }
            };

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку інгредієнтів.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox isBuy;
        TextView ingredientName, ingredientCountType;
        ImageView delete;
        View line;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            isBuy = itemView.findViewById(R.id.isBuy_checkBox);
            ingredientName = itemView.findViewById(R.id.ingredientNameItemTextView);
            ingredientCountType = itemView.findViewById(R.id.ingredientCountTypeItemTextView);
            delete = itemView.findViewById(R.id.deleteButton);
            line = itemView.findViewById(R.id.purchaseLine);
        }
    }

    /**
     * Інтерфейс для обробки кліків на елементи списку інгредієнтів.
     */
    public interface IngredientShopListClickListener {
        void onIngredientShopListClick(IngredientShopList ingredient);
        void onDeleteClick(IngredientShopList ingredient);
    }
}