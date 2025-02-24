package com.example.recipes.ViewItem;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.recipes.R;

import org.jetbrains.annotations.Nullable;

public class MenuItemView extends ConstraintLayout {
    private ImageView iconImage;
    private TextView titleText;

    public MenuItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.menu_panel_item, this, true);

        iconImage = findViewById(R.id.iconImage);
        titleText = findViewById(R.id.titleText);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MenuItemView, 0, 0);
            try {
                int iconRes = a.getResourceId(R.styleable.MenuItemView_iconSrc, R.drawable.icon_add_in);
                String text = a.getString(R.styleable.MenuItemView_textTitle);

                setIcon(iconRes);
                setTitle(text);
            } finally {
                a.recycle();
            }
        }
    }

    public void setIcon(@DrawableRes int resId) {
        iconImage.setImageResource(resId);
    }

    public void setTitle(String text) {
        titleText.setText(text);
    }
}
