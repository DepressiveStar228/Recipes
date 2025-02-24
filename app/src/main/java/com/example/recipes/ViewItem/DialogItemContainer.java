package com.example.recipes.ViewItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.recipes.R;

import java.util.Objects;

@SuppressLint("ViewConstructor")
public class DialogItemContainer extends ConstraintLayout {
    private boolean dishIsAdded = false;
    private String originalText;
    private TextView role_item;
    private TextView text_item;
    private ImageView add_dish;

    public DialogItemContainer(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_item_container, this, true);
        role_item = findViewById(R.id.roleItem);
        text_item = findViewById(R.id.textItem);
        add_dish = findViewById(R.id.addDishButton);
    }

    public void setRole_item(String name) {
        role_item.setText(name);
    }

    public void setText_item(String name) {
        text_item.setText(name);
    }

    public void setOriginalText(String text) {
        originalText = text;
    }

    public void setVisibilityAddButton(int visibility) {
        add_dish.setVisibility(visibility);
    }

    public String getText_item() {
        return text_item.getText().toString();
    }

    public String getRole_item() {
        return role_item.getText().toString();
    }

    public int getVisibilityAddButton() {
        return add_dish.getVisibility();
    }

    public String getOriginalText() { return originalText; }

    public boolean isDishIsAdded() {
        return dishIsAdded;
    }

    public void setDishIsAdded(boolean dishIsAdded) {
        this.dishIsAdded = dishIsAdded;
    }
}
