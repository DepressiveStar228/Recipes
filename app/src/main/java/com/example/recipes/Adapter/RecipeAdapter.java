package com.example.recipes.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.example.recipes.Controller.ImageController;
import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Decoration.TextLoadAnimation;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.ViewItem.CustomPopupWindow;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку рецептів, який може містити текстові та графічні елементи.
 * Підтримує режим читання (isRead), додавання, редагування, видалення та переміщення елементів.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_TEXT = 0, TYPE_IMAGE = 1;
    private final int HEIGHT_VOID_IMAGE = 200;
    private final int HEIGHT_EDITABLE_IMAGE = 700;
    private final int HEIGHT_SAVED_IMAGE = 2400;

    private final List<DishRecipe> items = new ArrayList<>();
    private int clickCurrentItem = -1; // Позиція поточного елемента, на який клікнули
    private AppCompatImageView currentImageView;
    private Boolean isRead; // Режим читання
    private Disposable disposableForCache;
    private final Context context;
    private final ConstraintLayout empty;
    private final ImageController imageController;
    private final ImageClickListener imageClickListener;
    private static ItemTouchHelper itemTouchHelper;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param empty Виджет для відображення порожнього стану.
     * @param isRead Чи режим читання активний.
     * @param imageClickListener Слухач для обробки кліків на зображення.
     */
    public RecipeAdapter(Context context, ConstraintLayout empty, boolean isRead, ImageClickListener imageClickListener) {
        this.context = context;
        this.empty = empty;
        this.isRead = isRead;
        this.imageClickListener = imageClickListener;
        this.imageController = new ImageController(context);
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
        DishRecipe item = items.get(position);
        item.setPosition(position);

        if (holder instanceof EditTextViewHolder editTextHolder && item.getTypeData() == DishRecipeType.TEXT) {
            TextWatcher textWatcher = (TextWatcher) editTextHolder.editText.getTag();

            // Якщо у поля для вводу текстового рецепту вже є слухач, то видаляємо його
            if (textWatcher != null) editTextHolder.editText.removeTextChangedListener(textWatcher);

            editTextHolder.editText.setText(item.getTextData());
            editTextHolder.editText.setEnabled(!isRead);

            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    item.setTextData(editTextHolder.editText.getText().toString().trim());
                }
            };

            editTextHolder.editText.addTextChangedListener(textWatcher);
            editTextHolder.editText.setTag(textWatcher);

            editTextHolder.delete.setOnClickListener(view -> delItem(item));

            editTextHolder.dragHandle.setVisibility(isRead ? View.GONE : View.VISIBLE);
            editTextHolder.delete.setVisibility(isRead ? View.GONE : View.VISIBLE);
            editTextHolder.container.getBackground().setAlpha(isRead ? 0 : 255);

            // Налаштування зовнішнього вигляду в залежності від режиму
            if (isRead) editTextHolder.editText.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
            else {
                ColorStateList tintList = ColorStateList.valueOf(AnotherUtils.getAttrColor(context, R.attr.colorControlNormal));
                editTextHolder.editText.setBackgroundTintList(tintList);
                editTextHolder.editText.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
            }
        } else if (holder instanceof ImageViewHolder imageViewHolder && item.getTypeData() == DishRecipeType.IMAGE) {
            imageViewHolder.imageView.post(() -> {
                Glide.with(imageViewHolder.imageView.getContext()).clear(imageViewHolder.imageView);

                if (!item.getTextData().isEmpty()) {
                    setImage(imageViewHolder.imageView, item.getTextData(), null);
                } else {
                    setVoidImageToImageView(imageViewHolder.imageView);
                }
            });

            imageViewHolder.imageView.setOnClickListener(view -> {
                if (imageClickListener != null && !isRead) {
                    int currentPosition = holder.getBindingAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        clickCurrentItem = currentPosition; // Запам'ятовуємо на який елемент клікнули
                        currentImageView = imageViewHolder.imageView;
                        imageClickListener.onImageClick(imageViewHolder.imageView);
                    }
                }
            });

            imageViewHolder.delete.setOnClickListener(view -> {
                if (imageClickListener != null) {
                    imageClickListener.onDeleteClick(item);
                    delItem(item);
                }
            });

            imageViewHolder.dragHandle.setVisibility(isRead ? View.GONE : View.VISIBLE);
            imageViewHolder.delete.setVisibility(isRead ? View.GONE : View.VISIBLE);
            imageViewHolder.container.getBackground().setAlpha(isRead ? 0 : 255);
        }
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

    /**
     * Повертає TextView поточного зображення для анімації завантаження
     */
    public TextView getTextViewForCurrentItem(@NonNull RecyclerView recyclerView) {
        if (clickCurrentItem != -1) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(clickCurrentItem);
            if (holder instanceof ImageViewHolder) {
                return ((ImageViewHolder) holder).textView;
            }
        }
        return null;
    }

    /**
     * Повертає елемент, на який останній раз клікали
     */
    public DishRecipe getCurrentItem() {
        if (!items.isEmpty() && clickCurrentItem >= 0 && clickCurrentItem < items.size()) return items.get(clickCurrentItem);
        else return null;
    }

    /**
     * Повертає елементи списку
     */
    public ArrayList<DishRecipe> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Встановлює режим відображення списку
     *
     * @param isRead флаг режиму відображення
     */
    public void setReadMode(boolean isRead) {
        this.isRead = isRead;
        notifyDataSetChanged();
    }

    /**
     * Встановлює елементи у список адаптера, попередньо все видаливши
     *
     * @param items елементи DishRecipe
     */
    public void setItems(@NonNull ArrayList<DishRecipe> items) {
        if (this.items.size() + items.size() <= Limits.MAX_COUNT_RECIPE_ITEM.getLimit()) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
            updateAllPositions();
            checkEmpty();
        }
    }

    /**
     * Додає елемент у список адаптера
     *
     * @param item елемент DishRecipe
     */
    public void addItem(@NonNull DishRecipe item) {
        if (items.size() < Limits.MAX_COUNT_RECIPE_ITEM.getLimit() + 20) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
            updateAllPositions();
            checkEmpty();
        }
    }

    /**
     * Видаляє елемент зі списку адаптера
     *
     * @param item елемент DishRecipe
     */
    public void delItem(@NonNull DishRecipe item) {
        int position = items.indexOf(item);
        items.remove(item);
        notifyItemRemoved(position);
        updateAllPositions();
        checkEmpty();
    }

    /**
     * Змінює відображення заставки порожності списку
     */
    private void checkEmpty() {
        if (empty != null) {
            if (items.isEmpty()) empty.setVisibility(View.VISIBLE);
            else empty.setVisibility(View.GONE);
        }
    }

    /**
     * Оновлює позиції всіх елементів у списку.
     */
    private void updateAllPositions() {
        DishRecipe currentDishRecipe = getCurrentItem();

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(currentDishRecipe)) {  // Оновлюємо індекс елемента, на який клікнули
                clickCurrentItem = i;
            }
            items.get(i).setPosition(i);
        }
    }

    /**
     * Класс, який здійснює контроль перенесення елементів
     */
    public ItemTouchHelper getItemTouchHelper() {
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
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
                    updateAllPositions();
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);

                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    if (viewHolder != null) {
                        viewHolder.itemView.setAlpha(0.7f);
                        viewHolder.itemView.setScaleX(1.05f);
                        viewHolder.itemView.setScaleY(1.05f);
                    }
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                viewHolder.itemView.setAlpha(1.0f);
                viewHolder.itemView.setScaleX(1.0f);
                viewHolder.itemView.setScaleY(1.0f);
            }
        });

        return itemTouchHelper;
    }

    /**
     * Встановлює зображення для поточного елемента.
     *
     * @param imageUri URI зображення
     * @param isLoadImage Анімація завантаження (може бути null)
     */
    public void setImageToCurrentItem(Uri imageUri, TextLoadAnimation isLoadImage) {
        if (isLoadImage == null) {
            Log.e("RecipeAdapter", "Runnable isLoadImage is null!");
            return;
        }

        if (imageUri == null) {
            Log.e("RecipeAdapter", "Image URI is null!");
            return;
        }

        setImage(currentImageView, imageUri, isLoadImage);
    }

    /**
     * Встановлює зображення у ImageView залежно від типу джерела (шлях або URI).
     *
     * @param imageView ImageView для відображення
     * @param path Шлях або URI зображення
     * @param isLoadImage Анімація завантаження
     */
    private void setImage(AppCompatImageView imageView, Object path, TextLoadAnimation isLoadImage) {
        if (path != null) {
            if (path instanceof String) setImageFromPath(imageView, (String) path, isLoadImage);
            else if (path instanceof Uri) setImageFromUri(imageView, (Uri) path, isLoadImage);
        }
    }

    /**
     * Завантажує зображення з файлового шляху.
     *
     * @param imageView ImageView для відображення
     * @param path Шлях до зображення
     * @param isLoadImage Анімація завантаження
     */
    private void setImageFromPath(AppCompatImageView imageView, String path, TextLoadAnimation isLoadImage) {
        if (!path.isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new ImageViewTarget<Drawable>(imageView) {
                        @Override
                        protected void setResource(@Nullable Drawable resource) {
                            setResourceToImageView(imageView, resource, isLoadImage);
                        }
                    });
        }
    }

    /**
     * Завантажує зображення з URI.
     *
     * @param imageView ImageView для відображення
     * @param uri URI зображення
     * @param isLoadImage Анімація завантаження
     */
    private void setImageFromUri(AppCompatImageView imageView, Uri uri, TextLoadAnimation isLoadImage) {
        if (uri != null) {
            Glide.with(imageView.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new ImageViewTarget<Drawable>(imageView) {
                        @Override
                        protected void setResource(@Nullable Drawable resource) {
                            setResourceToImageView(imageView, resource, isLoadImage);
                        }
                    });
        }
    }

    /**
     * Встановлює ресурс зображення у ImageView з обробкою розмірів.
     *
     * @param imageView ImageView для відображення
     * @param resource Drawable ресурс
     * @param isLoadImage Анімація завантаження
     */
    private void setResourceToImageView(AppCompatImageView imageView, Drawable resource, TextLoadAnimation isLoadImage) {
        if (resource == null) {
            if (isLoadImage != null) isLoadImage.stopAnimation();
            setVoidImageToImageView(imageView);

            Log.e("SetImageController", "Drawable resource is null!");
            return;
        }

        addToCache(getCurrentItem(), resource);

        // Розрахунок пропорцій зображення
        int imageWidth = resource.getIntrinsicWidth();
        int imageHeight = resource.getIntrinsicHeight();

        int viewWidth = imageView.getMeasuredWidth();
        if (viewWidth <= 0) {
            viewWidth = context.getResources().getDisplayMetrics().widthPixels;
        }
        if (viewWidth > imageWidth) viewWidth = imageWidth;

        float aspectRatio = (float) imageHeight / imageWidth;
        int newHeight = (int) (viewWidth * aspectRatio);

        imageView.clearColorFilter();

        if (imageView.getLayoutParams().height != getCorrectHeight(newHeight)) {
            imageView.getLayoutParams().height = getCorrectHeight(newHeight);
            imageView.setImageDrawable(resource);
            imageView.requestLayout();
        }

        if (isLoadImage != null) isLoadImage.stopAnimation();
    }

    /**
     * Додає зображення до кешу.
     *
     * @param item Поточний елемент рецепту
     * @param resource Drawable ресурс для кешування
     */
    private void addToCache(DishRecipe item, Drawable resource) {
        if (disposableForCache != null) disposableForCache.dispose();

        if (item != null && resource != null) {
            disposableForCache = imageController.addImageToCache(((BitmapDrawable) resource).getBitmap())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(url -> {
                        if (!url.isEmpty()) item.setTextData(url);
                    });
        }
    }

    /**
     * Встановлює стандартне зображення-заглушку.
     *
     * @param imageView ImageView для відображення
     */
    private void setVoidImageToImageView(AppCompatImageView imageView) {
        imageView.setImageDrawable(null);
        imageView.getLayoutParams().height = HEIGHT_VOID_IMAGE;
        imageView.setColorFilter(AnotherUtils.getAttrColor(context, R.attr.colorControlNormal));
        imageView.setImageResource(R.drawable.icon_image_add);
        imageView.requestLayout();
    }

    /**
     * Обчислює коректну висоту зображення залежно від режиму.
     *
     * @param imageHeight Поточна висота зображення
     * @return Коректна висота для відображення
     */
    private int getCorrectHeight(int imageHeight) {
        return Math.min(imageHeight, isRead ? HEIGHT_SAVED_IMAGE : HEIGHT_EDITABLE_IMAGE);
    }

    /**
     * Внутрішній клас, який представляє ViewHolder для текстового елемента.
     */
    public static class EditTextViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        AppCompatImageView dragHandle, delete;
        EditText editText;

        @SuppressLint("ClickableViewAccessibility")
        public EditTextViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.editTextContainerForAdapter);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            editText = itemView.findViewById(R.id.editText);
            delete = itemView.findViewById(R.id.delete);

            dragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (itemTouchHelper != null) {
                        itemTouchHelper.startDrag(EditTextViewHolder.this);
                    }
                }
                return true;
            });
        }
    }

    /**
     * Внутрішній клас, який представляє ViewHolder для графічного елемента.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        AppCompatImageView imageView, dragHandle, delete;
        TextView textView;

        @SuppressLint("ClickableViewAccessibility")
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.imageViewContainerForAdapter);
            dragHandle = itemView.findViewById(R.id.dragHandle);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
            delete = itemView.findViewById(R.id.delete);

            dragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (itemTouchHelper != null) {
                        itemTouchHelper.startDrag(ImageViewHolder.this);
                    }
                }
                return true;
            });
        }
    }

    /**
     * Інтерфейс для обробки кліків на зображення.
     */
    public interface ImageClickListener {
        void onImageClick(AppCompatImageView imageView);
        void onDeleteClick(DishRecipe dishRecipe);
    }
}