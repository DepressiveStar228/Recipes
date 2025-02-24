package com.example.recipes.Adapter;

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

import com.example.recipes.Item.Dish;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;

import java.util.ArrayList;

public class DishGetToShopListAdapter extends ListAdapter<Dish, DishGetToShopListAdapter.ViewHolder> {
    private DishClickListener clickListener;

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
        holder.delete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeleteClick(dish);
            }
        });
    }

    public void setItems(ArrayList<Dish> newItems) {
        ArrayList<Dish> newList = new ArrayList<>(newItems.size());
        for (Dish item : newItems) {
            newList.add(new Dish(item));
        }

        submitList(newList);
        notifyDataSetChanged();
    }

    public static final DiffUtil.ItemCallback<Dish> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Dish>() {
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dishName;
        ImageView delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishName = itemView.findViewById(R.id.dishNameItemTextView);
            delete = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface DishClickListener {
        void onDeleteClick(Dish dish);
    }
}
