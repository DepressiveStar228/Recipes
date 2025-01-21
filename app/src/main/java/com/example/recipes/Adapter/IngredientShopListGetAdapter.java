package com.example.recipes.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Config;
import com.example.recipes.Controller.DataControllerForAdapter;
import com.example.recipes.Controller.DiffCallback;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;

import java.util.ArrayList;

public class IngredientShopListGetAdapter extends RecyclerView.Adapter<IngredientShopListGetAdapter.ViewHolder> implements DataControllerForAdapter<IngredientShopList> {
    private final Context context;
    private final IngredientShopListClickListener ingredientShopListClickListener;
    private ArrayList<IngredientShopList> ingredients;

    public IngredientShopListGetAdapter(Context context, ArrayList<IngredientShopList> ingredients, IngredientShopListGetAdapter.IngredientShopListClickListener clickListener) {
        this.context = context;
        this.ingredients = ingredients;
        this.ingredientShopListClickListener = clickListener;
    }

    @NonNull
    @Override
    public IngredientShopListGetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_shop_list_item, parent, false);
        return new IngredientShopListGetAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IngredientShopList ingredientShopList = ingredients.get(position);
        holder.isBuy.setChecked(ingredientShopList.getIsBuy());
        holder.ingredientName.setText(ingredientShopList.getName());
        holder.ingredientCountType.setText(String.format("%s %s", ingredientShopList.getAmount(), ingredientShopList.getType()));
        updateStrikeThrough(holder.line, ingredientShopList.getIsBuy());

        holder.itemView.setOnClickListener(v -> {
            holder.isBuy.setChecked(!holder.isBuy.isChecked());
            updateStrikeThrough(holder.line, holder.isBuy.isChecked());
            ingredients.get(position).setIsBuy(!ingredientShopList.getIsBuy());
            ingredientShopList.setIsBuy(!ingredientShopList.getIsBuy());

            if (ingredientShopListClickListener != null) {
                ingredientShopListClickListener.onIngredientShopListClick(ingredientShopList, holder.itemView);
            }
        });

        holder.delete.setOnClickListener(v -> {
            ingredients.remove(position);
            notifyItemRemoved(position);
            Log.d("IngredientShopListGetAdapter", "Адаптер видалив інгредієнт");

            if (ingredientShopListClickListener != null) {
                ingredientShopListClickListener.onDeleteClick(ingredientShopList, holder.delete);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    @Override
    public void addItem(RecyclerView recyclerView, IngredientShopList item) {
        if (ingredients.size() < Config.COUNT_LIMIT_INGREDIENT) {
            ingredients.add(item);
            notifyItemInserted(ingredients.size() - 1);

            recyclerView.postDelayed(() -> {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(getPosition(item));
                if (holder != null) {
                    holder.itemView.setAlpha(0f);
                    holder.itemView.animate()
                            .alpha(1f)
                            .setDuration(250)
                            .start();
                }
            }, 50);

            Log.d("ShopListGetAdapter", "Адаптер додав колекцію");
        } else {
            Toast.makeText(context, context.getString(R.string.warning_max_count_ingredients) + "(" + Config.COUNT_LIMIT_INGREDIENT + ")", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void addItems(RecyclerView recyclerView, ArrayList<IngredientShopList> items) {
        if ((ingredients.size() + items.size()) < Config.COUNT_LIMIT_INGREDIENT) {
            for (IngredientShopList ing : items) {
                addItem(recyclerView, ing);
            }
        } else {
            Toast.makeText(context, context.getString(R.string.warning_max_count_ingredients) + "(" + Config.COUNT_LIMIT_INGREDIENT + ")", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void delItem(RecyclerView recyclerView, IngredientShopList item) {
        int position = getPosition(item);
        if (position != -1) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

            if (holder != null) {
                holder.itemView.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction(() -> {
                            ingredients.remove(position);
                            notifyItemRemoved(position);
                        })
                        .start();
                Log.d("IngredientShopListGetAdapter", "Адаптер видалив інгредієнт");
            }
        }
    }

    @Override
    public void upItem(RecyclerView recyclerView, IngredientShopList item) {
        int position = getPosition(item);
        if (position != -1) {
            ingredients.set(position, item);
            notifyItemChanged(position);
        }
    }

    @Override
    public void setItems(ArrayList<IngredientShopList> items) {
        this.ingredients = items;
        notifyDataSetChanged();
    }

    @Override
    public int getPosition(IngredientShopList item) {
        return ingredients.indexOf(item);
    }

    private void updateStrikeThrough(View line, boolean isStriked) {
        if (isStriked) {
            line.setVisibility(View.VISIBLE);
        } else {
            line.setVisibility(View.INVISIBLE);
        }
    }

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
        void onIngredientShopListClick(IngredientShopList ingredientShopList, View view);
        void onDeleteClick(IngredientShopList ingredientShopList, View view);
    }
}
