package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Interface.SelectableItem;
import com.example.recipes.R;

import java.util.ArrayList;

public class AddChooseObjectsAdapter extends RecyclerView.Adapter<AddChooseObjectsAdapter.CollectionViewHolder> {
    private ArrayList<Object> items;
    private OnItemClickListener listener;
    private ArrayList<Object> selectedItem = new ArrayList<>();

    public AddChooseObjectsAdapter(Context context, ArrayList<Object> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(context);
    }

    public interface OnItemClickListener {
        void onItemClick(CheckBox checkBox, ArrayList<Object> selectedItem, Object item);
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_check_item, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        Object item = items.get(position);
        holder.bind(item);
        holder.item_check.setOnCheckedChangeListener(null);

        if (item instanceof SelectableItem) {
            holder.item_check.setChecked(selectedItem.contains(((SelectableItem) item).getId()));

        } else if (item instanceof String) {
            holder.item_check.setChecked(selectedItem.contains(item.toString()));
        }

        holder.item_layout.setOnClickListener(v -> listener.onItemClick(holder.item_check, selectedItem, item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addSelectedItem(Object object) {
        selectedItem.add(object);
    }

    public ArrayList<Object> getSelectedItem() {
        return selectedItem;
    }

    public void resetSelection() {
        selectedItem.clear();
        notifyDataSetChanged();
    }

    public void clear() {
        selectedItem.clear();
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout item_layout;
        TextView item_name;
        CheckBox item_check;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            item_layout = itemView.findViewById(R.id.box_checkItem);
            item_name = itemView.findViewById(R.id.dish_name_checkItem);
            item_check = itemView.findViewById(R.id.dish_check_checkItem);
        }

        void bind(Object item) {
            if (item instanceof SelectableItem) {
                SelectableItem selectableItem = (SelectableItem) item;
                item_name.setText(selectableItem.getName());
            } else if (item instanceof String) {
                String name = item.toString();
                item_name.setText(name);
            }
        }
    }
}
