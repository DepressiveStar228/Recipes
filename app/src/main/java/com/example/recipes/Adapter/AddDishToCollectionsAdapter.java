package com.example.recipes.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

public class AddDishToCollectionsAdapter extends RecyclerView.Adapter<AddDishToCollectionsAdapter.CollectionViewHolder> {
    private static Context context;
    private RecipeUtils utils;
    private static ArrayList<Collection> collections;
    private static ArrayList<Integer> selectedCollectionIds = new ArrayList<>();
    private static PerferencesController perferencesController;

    public AddDishToCollectionsAdapter(Context context, ArrayList<Collection> collections) {
        this.context = context;
        this.collections = collections;
        utils = new RecipeUtils(context);
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(context);
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_check__item, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        Collection collection = collections.get(position);
        holder.bind(collection);

        holder.collection_name.setText(utils.getNameCollection(collection.getId()));

        setImage(holder, collection);

        holder.collection_check.setOnCheckedChangeListener(null);
        holder.collection_check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCollectionIds.add(collection.getId());
            } else {
                selectedCollectionIds.remove(Integer.valueOf(collection.getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    public ArrayList<Integer> getSelectedCollectionIds() {
        return selectedCollectionIds;
    }

    private static void setImage(CollectionViewHolder holder, Collection collection) {
        if (Objects.equals(collection.getName(), context.getString(R.string.favorites))) {
            if (Objects.equals(perferencesController.theme, "Light")) {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_star));
            } else {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_star_darkmode));
            }
        } else {
            if (Objects.equals(perferencesController.theme, "Light")) {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book));
            } else {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book_darkmode));
            }
        }
    }

    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        TextView collection_name;
        ImageView collection_img;
        CheckBox collection_check;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            collection_name = itemView.findViewById(R.id.collection_name_checkItem);
            collection_img = itemView.findViewById(R.id.collection_imageView_checkItem);
            collection_check = itemView.findViewById(R.id.collection_check_checkItem);
        }

        void bind(Collection collection) {
            collection_name.setText(collection.getName());
            setImage(this, collection);

            collection_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        collections.get(position).setName(s.toString());
                    }
                }
            });

            collection_check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (isChecked) {
                        selectedCollectionIds.add(collections.get(position).getId());
                    } else {
                        selectedCollectionIds.remove(Integer.valueOf(collections.get(position).getId()));
                    }
                }
            });
        }
    }
}