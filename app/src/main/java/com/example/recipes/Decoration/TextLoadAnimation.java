package com.example.recipes.Decoration;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

public class TextLoadAnimation {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView textView;
    private String baseText = "";
    private final Runnable runnable;
    private int dotCount = 0;

    public TextLoadAnimation(TextView textView, String baseText) {
        this.textView = textView;
        this.baseText = baseText;
        this.runnable = new Runnable() {
            @Override
            public void run() {
                dotCount = (dotCount + 1) % 4;
                String dots = new String(new char[dotCount]).replace("\0", ".");
                textView.setText(baseText + dots);
                handler.postDelayed(this, 150);
            }
        };
    }

    public void setBaseText(String baseText) {
        this.baseText = baseText;
    }

    public void setBaseTextIntoTextView (String baseText) {
        this.baseText = baseText;
        if (textView != null) {
            textView.setText(baseText);
        }
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public void startAnimation() {
        if (textView != null) {
            textView.setVisibility(View.VISIBLE);
            handler.postDelayed(runnable, 100);
        }
    }

    public void stopAnimation() {
        handler.removeCallbacks(runnable);
        if (textView != null) {
            textView.setText(baseText);
            textView.setVisibility(View.INVISIBLE);
        }
    }
}
