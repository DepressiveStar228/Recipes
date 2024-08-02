package com.example.recipes.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;

import java.util.ArrayList;

public class AddDishesToCollectionAdapter extends RecyclerView.Adapter<AddDishesToCollectionAdapter.CollectionViewHolder> {
    private static ArrayList<Dish> dishes;
    private static ArrayList<Integer> selectedDishIds = new ArrayList<>();

    public AddDishesToCollectionAdapter(Context context, ArrayList<Dish> dishes) {
        this.dishes = dishes;
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(context);
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_check_item, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        Dish dish = dishes.get(position);
        holder.bind(dish);

        holder.dish_check.setOnCheckedChangeListener(null);
        holder.dish_check.setChecked(selectedDishIds.contains(dish.getID()));
        holder.dish_check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedDishIds.add(dish.getID());
            } else {
                selectedDishIds.remove(Integer.valueOf(dish.getID()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    public ArrayList<Integer> getSelectedDishIds() {
        return selectedDishIds;
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        TextView dish_name;
        CheckBox dish_check;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            dish_name = itemView.findViewById(R.id.dish_name_checkItem);
            dish_check = itemView.findViewById(R.id.dish_check_checkItem);
        }

        void bind(Dish dish) {
            dish_name.setText(dish.getName());

            dish_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        dishes.get(position).setName(s.toString());
                    }
                }
            });

            dish_check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (isChecked) {
                        selectedDishIds.add(dishes.get(position).getID());
                    } else {
                        selectedDishIds.remove(Integer.valueOf(dishes.get(position).getID()));
                    }
                }
            });
        }
    }
}
