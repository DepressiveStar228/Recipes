package com.example.recipes.ViewItem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.recipes.R;

public class CustomContainer extends ConstraintLayout {

    private TextView containerName;
    private FrameLayout contentFrame;

    public CustomContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.container_with_border_and_name, this, true);
        containerName = findViewById(R.id.customContainerName);
        contentFrame = findViewById(R.id.customContent);
    }

    public void setContainerName(String name) {
        containerName.setText(name);
    }

    public void addViewToContainer(View view) {
        contentFrame.addView(view);
    }

    public void clearContainer() {
        contentFrame.removeAllViews();
    }
}
