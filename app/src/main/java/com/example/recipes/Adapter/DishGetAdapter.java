package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

public class DishGetAdapter extends ListAdapter<Dish, DishGetAdapter.ViewHolder> {
    private final Context context;
    private PreferencesController preferencesController;
    private static String[] themeArray;
    private final DishClickListener commandClickListener;

    public DishGetAdapter(Context context, DishClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.commandClickListener = clickListener;
        preferencesController = new PreferencesController();
        preferencesController.loadPreferences(context);
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
        holder.menu.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_more));
        if (Objects.equals(preferencesController.getTheme(), themeArray[0])) {
            holder.menu.setColorFilter(R.color.white);
        }

        holder.menu.setOnClickListener(v -> {
            if (commandClickListener != null) {
                commandClickListener.onDishMenuClick(dish, holder.menu);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (commandClickListener != null) {
                commandClickListener.onDishClick(dish, v);
            }
        });
    }

    public interface DishClickListener {
        void onDishClick(Dish item, View v);
        void onDishMenuClick(Dish item, View v);
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

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView menu;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.dish_name);
            menu = itemView.findViewById(R.id.menu_dish_imageView);
        }
    }
}
