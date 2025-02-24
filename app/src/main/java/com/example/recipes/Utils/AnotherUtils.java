package com.example.recipes.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.recipes.R;

public class AnotherUtils {

    public static void visibilityEmptyStatus(ConstraintLayout emptyLayout, Boolean isEmpty) {
        if (emptyLayout != null) {
            if (!isEmpty) { emptyLayout.setVisibility(View.GONE); }
            else { emptyLayout.setVisibility(View.VISIBLE); }
        }
    }

    @SuppressLint({"ResourceType", "ObjectAnimatorBinding"})
    public static int getAttrColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawable(Context context, int attr) {
        return context.getDrawable(attr);
    }
}
