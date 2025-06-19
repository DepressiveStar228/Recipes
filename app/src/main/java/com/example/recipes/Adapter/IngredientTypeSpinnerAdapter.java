package com.example.recipes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.R;

import java.util.List;

public class IngredientTypeSpinnerAdapter extends RecyclerView.Adapter<IngredientTypeSpinnerAdapter.ViewHolder> {
    private List<String> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public IngredientTypeSpinnerAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredient_type_spinner_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String realItem = items.get(position % items.size());
        holder.bind(realItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.ingredientTypeTextView);
        }

        void bind(String item) {
            text.setText(item);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
