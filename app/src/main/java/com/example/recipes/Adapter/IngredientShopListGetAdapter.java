package com.example.recipes.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Config;
import com.example.recipes.Controller.DataControllerForAdapter;
import com.example.recipes.Controller.DiffCallback;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;

import java.util.ArrayList;

public class IngredientShopListGetAdapter extends ListAdapter<IngredientShopList, IngredientShopListGetAdapter.ViewHolder> {
    private final Context context;
    private int boughtItem, unBoughtItem;
    private final IngredientShopListClickListener clickListener;

    public IngredientShopListGetAdapter(Context context, IngredientShopListClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.clickListener = clickListener;
        getColors();
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

        holder.isBuy.setChecked(ingredientShopList.getIsBuy());
        holder.ingredientName.setText(ingredientShopList.getName());
        holder.ingredientCountType.setText(ingredientShopList.getGroupedAmountTypeToString());
        updateStrikeThrough(holder.line, ingredientShopList.getIsBuy());

        if (ingredientShopList.getIsBuy()) {
            holder.ingredientName.setTextColor(boughtItem);
            holder.ingredientCountType.setTextColor(boughtItem);
            holder.delete.setColorFilter(boughtItem);
        } else {
            holder.ingredientName.setTextColor(unBoughtItem);
            holder.ingredientCountType.setTextColor(unBoughtItem);
            holder.delete.setColorFilter(unBoughtItem);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onIngredientShopListClick(ingredientShopList);
            }
        });

        holder.delete.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDeleteClick(ingredientShopList);
            }
        });
    }

    public void setItems(ArrayList<IngredientShopList> newItems) {
        ArrayList<IngredientShopList> newList = new ArrayList<>(newItems.size());
        for (IngredientShopList item : newItems) {
            newList.add(new IngredientShopList(item));
        }

        submitList(newList);
        notifyDataSetChanged();
    }

    private void updateStrikeThrough(View line, boolean isStriked) {
        line.setVisibility(isStriked ? View.VISIBLE : View.INVISIBLE);
    }

    @SuppressLint("ResourceType")
    private void getColors() {
        boughtItem = context.getResources().getColor(R.color.grey2, context.getTheme());

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorText, typedValue, true);
        unBoughtItem = typedValue.data;
    }

    public static final DiffUtil.ItemCallback<IngredientShopList> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<IngredientShopList>() {
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

    public interface IngredientShopListClickListener {
        void onIngredientShopListClick(IngredientShopList ingredient);
        void onDeleteClick(IngredientShopList ingredient);
    }
}