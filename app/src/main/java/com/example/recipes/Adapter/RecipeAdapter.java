package com.example.recipes.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Config;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_TEXT = 0, TYPE_IMAGE = 1;
    private final List<DishRecipe> items = new ArrayList<>();
    private int clickCurrentItem = -1;
    private Boolean isRead, firstLoad = false;
    private final Context context;
    private final ConstraintLayout empty;
    private final ImageClickListener imageClickListener;

    public RecipeAdapter(Context context, ConstraintLayout empty, boolean isRead, ImageClickListener imageClickListener) {
        this.context = context;
        this.empty = empty;
        this.isRead = isRead;
        this.imageClickListener = imageClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_TEXT) {
            View view = inflater.inflate(R.layout.edit_text_item_for_recipe, parent, false);
            return new EditTextViewHolder(view);
        } else if (viewType == TYPE_IMAGE) {
            View view = inflater.inflate(R.layout.image_item_for_recipe, parent, false);
            return new ImageViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final int HEIGHT_VOID_IMAGE = 200;
        final int HEIGHT_EDITABLE_IMAGE = 700;
        final int HEIGHT_SAVED_IMAGE = 2400;

        DishRecipe item = items.get(position);
        item.setPosition(position);

        if (holder instanceof EditTextViewHolder editTextHolder && item.getTypeData() == DishRecipeType.TEXT) {
            TextWatcher textWatcher = (TextWatcher) editTextHolder.editText.getTag();
            if (textWatcher != null) editTextHolder.editText.removeTextChangedListener(textWatcher);

            editTextHolder.editText.setText(item.getTextData());
            editTextHolder.editText.setEnabled(!isRead);

            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    item.setTextData(editTextHolder.editText.getText().toString().trim());
                }
            };

            editTextHolder.editText.addTextChangedListener(textWatcher);
            editTextHolder.editText.setTag(textWatcher);

            editTextHolder.delete.setOnClickListener(view -> delItem(item));

            editTextHolder.delete.setVisibility(isRead ? View.GONE : View.VISIBLE);
            editTextHolder.container.getBackground().setAlpha(isRead ? 0 : 255);

            if (isRead) editTextHolder.editText.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
            else {
                ColorStateList tintList = ColorStateList.valueOf(AnotherUtils.getAttrColor(context, R.attr.colorControlNormal));
                editTextHolder.editText.setBackgroundTintList(tintList);
                editTextHolder.editText.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
            }
        } else if (holder instanceof ImageViewHolder imageViewHolder && item.getTypeData() == DishRecipeType.IMAGE) {
            if (item.getBitmap() == null) {
                imageViewHolder.imageView.setColorFilter(AnotherUtils.getAttrColor(context, R.attr.colorControlNormal));
                imageViewHolder.imageView.setImageResource(R.drawable.icon_image_add);
                setHeightImageView(imageViewHolder.imageView, HEIGHT_VOID_IMAGE);
            } else {
                imageViewHolder.imageView.clearColorFilter();
                imageViewHolder.imageView.setImageBitmap(item.getBitmap());

                if (!firstLoad) {
                    imageViewHolder.imageView.post(() -> setHeightImageView(imageViewHolder.imageView, isRead ? HEIGHT_SAVED_IMAGE : HEIGHT_EDITABLE_IMAGE));
                } else setHeightImageView(imageViewHolder.imageView, isRead ? HEIGHT_SAVED_IMAGE : HEIGHT_EDITABLE_IMAGE);
            }

            imageViewHolder.imageView.setOnClickListener(view -> {
                if (imageClickListener != null && !isRead) {
                    clickCurrentItem = position;
                    imageClickListener.onImageClick(imageViewHolder.imageView, imageViewHolder.textView);
                }
            });

            imageViewHolder.delete.setOnClickListener(view -> {
                if (imageClickListener != null) {
                    imageClickListener.onDeleteClick(item);
                    delItem(item);
                }
            });

            imageViewHolder.delete.setVisibility(isRead ? View.GONE : View.VISIBLE);
            imageViewHolder.container.getBackground().setAlpha(isRead ? 0 : 255);
        }

        if (position == items.size()-1) firstLoad = true; // Адаптер закінчив оновлення списку
    }

    @Override
    public int getItemViewType(int position) {
        DishRecipe item = items.get(position);
        if (item.getTypeData() == DishRecipeType.TEXT) {
            return TYPE_TEXT;
        } else if (item.getTypeData() == DishRecipeType.IMAGE) {
            return TYPE_IMAGE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public DishRecipe getCurrentItem() {
        updateCurrentItem();
        if (!items.isEmpty() && clickCurrentItem >= 0 && clickCurrentItem < items.size()) return items.get(clickCurrentItem);
        else return null;
    }

    public ArrayList<DishRecipe> getItems() {
        updatePositionItems();
        return new ArrayList<>(items);
    }

    private void updatePositionItems() {
        for (int i = 0; i < items.size(); i++) items.get(i).setPosition(i);
    }

    private void updateCurrentItem() {
        if (!items.isEmpty() && clickCurrentItem >= 0) {
            int newCurrentItem = items.get(clickCurrentItem).getPosition();
            updatePositionItems();
            clickCurrentItem = items.get(newCurrentItem).getPosition();
        }
    }

    public void setReadMode(boolean isRead) {
        this.isRead = isRead;
        notifyDataSetChanged();
    }

    public void setItems(@NonNull ArrayList<DishRecipe> items) {
        if (this.items.size() + items.size() <= Config.COUNT_LIMIT_RECIPE_ITEM) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
            checkEmpty();
        }
    }

    public void addItem(@NonNull DishRecipe item) {
        if (items.size() < Config.COUNT_LIMIT_RECIPE_ITEM) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
            checkEmpty();
        }
    }

    public void upItem(@NonNull DishRecipe item, int position) {
        items.set(position, item);
        notifyItemChanged(position);
    }

    public void delItem(@NonNull DishRecipe item) {
        int position = items.indexOf(item);
        items.remove(item);
        notifyItemRemoved(position);
        checkEmpty();
    }

    private void checkEmpty() {
        if (empty != null) {
            if (items.isEmpty()) empty.setVisibility(View.VISIBLE);
            else empty.setVisibility(View.GONE);
        }
    }

    public ItemTouchHelper getItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();

                if (fromPosition != RecyclerView.NO_POSITION && toPosition != RecyclerView.NO_POSITION) {
                    Collections.swap(items, fromPosition, toPosition);
                    notifyItemMoved(fromPosition, toPosition);
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        });
    }

    private void setHeightImageView(AppCompatImageView imageView, int maxHeight) {
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            int imageWidth = drawable.getIntrinsicWidth();
            int imageHeight = drawable.getIntrinsicHeight();

            int viewWidth = imageView.getMeasuredWidth();
            if (viewWidth <= 0) {
                viewWidth = context.getResources().getDisplayMetrics().widthPixels;
            }
            if (viewWidth > imageWidth) viewWidth = imageWidth;

            float aspectRatio = (float) imageHeight / imageWidth;
            int newHeight = (int) (viewWidth * aspectRatio);

            if (newHeight > maxHeight) newHeight = maxHeight;

            if (imageView.getLayoutParams().height != newHeight) {
                imageView.getLayoutParams().height = newHeight;
                imageView.requestLayout();
            }
        }
    }

    public static class EditTextViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        EditText editText;
        AppCompatImageView delete;

        public EditTextViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.editTextContainerForAdapter);
            editText = itemView.findViewById(R.id.editText);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        AppCompatImageView imageView, delete;
        TextView textView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.imageViewContainerForAdapter);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    public interface ImageClickListener {
        void onImageClick(AppCompatImageView imageView, TextView textView);
        void onDeleteClick(DishRecipe dishRecipe);
    }
}
