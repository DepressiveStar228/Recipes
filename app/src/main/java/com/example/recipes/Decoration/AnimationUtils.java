package com.example.recipes.Decoration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nonnegative;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;

public class AnimationUtils {
    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    public static final boolean SHOW = true;
    public static final boolean HIDE = false;

    private static final SparseBooleanArray animationFlags = new SparseBooleanArray();

    public static void smoothRotation(@NonNull View view, @NonNull @Nonnegative int way, @NonNull @Nonnegative int durationAnimation, @NonNull Runnable onAnimationEnd) {
        int viewId = view.getId();
        if (viewId == View.NO_ID) return;

        if (animationFlags.get(viewId, true)) {
            animationFlags.put(viewId, false);

            ObjectAnimator rotationAnimator = new ObjectAnimator();

            if (way % 2 == 0) rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 45f);
            else rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 45f, 0f);

            rotationAnimator.setDuration(durationAnimation);
            rotationAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationFlags.put(viewId, true);
                    onAnimationEnd.run();
                }
            });
            rotationAnimator.start();
        }
    }

    public static void smoothSlipVisibility(@NonNull View view, @NonNull @Nonnegative int way, @NonNull @Nonnegative boolean mode, @NonNull @Nonnegative float translationValue, @NonNull @Nonnegative int durationAnimation, @NonNull Runnable onAnimationEnd) {
        int viewId = view.getId();
        if (viewId == View.NO_ID) return;

        if (animationFlags.get(viewId, true)) {
            animationFlags.put(viewId, false);
            float firstTranslationValue = 0f, secondTranslationValue = 0f, mainTranslationValue = translationValue;
            String propertyName;

            switch (way) {
                case TOP:
                    propertyName = "translationY";
                    mainTranslationValue = -mainTranslationValue;
                    break;
                case BOTTOM:
                    propertyName = "translationY";
                    break;
                case LEFT:
                    propertyName = "translationX";
                    mainTranslationValue = -mainTranslationValue;
                    break;
                case RIGHT:
                    propertyName = "translationX";
                    break;
                default:
                    return;
            }

            ObjectAnimator alphaAnimator, translationAnimator;
            view.setVisibility(View.VISIBLE);

            if (mode) {
                alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                translationAnimator = ObjectAnimator.ofFloat(view, propertyName, mainTranslationValue, secondTranslationValue);
            }
            else {
                alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
                translationAnimator = ObjectAnimator.ofFloat(view, propertyName, firstTranslationValue, mainTranslationValue);
            }

            alphaAnimator.setDuration(durationAnimation);
            translationAnimator.setDuration(durationAnimation);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alphaAnimator, translationAnimator);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationFlags.put(viewId, true);
                    onAnimationEnd.run();

                    if (mode) { view.setVisibility(View.VISIBLE); }
                    else { view.setVisibility(View.GONE); }
                }
            });
            animatorSet.start();
        }
    }

    public static void smoothVisibility(@NonNull View view, @NonNull @Nonnegative boolean mode, @NonNull @Nonnegative int durationAnimation) {
        int viewKey  = view.hashCode();

        if (animationFlags.get(viewKey , true)) {
            if (view.getVisibility() == View.VISIBLE) view.setAlpha(1f);
            if (view.getVisibility() == View.INVISIBLE || view.getVisibility() == View.GONE) view.setAlpha(0f);

            if (mode == (view.getAlpha() == 1f)) return;
            if (!mode == (view.getAlpha() == 0f)) return;

            animationFlags.put(viewKey , false);

            ValueAnimator alphaAnimator = ValueAnimator.ofFloat(mode ? 0f : 1f, mode ? 1f : 0f);
            alphaAnimator.setDuration(durationAnimation);
            alphaAnimator.addUpdateListener(animation -> {
                float alpha = (float) animation.getAnimatedValue();
                view.setAlpha(alpha);
            });
            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mode) view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!mode) view.setVisibility(View.GONE);
                    view.setEnabled(mode);
                    animationFlags.put(viewKey, true);
                }
            });
            alphaAnimator.start();
        }
    }

    public static ValueAnimator backgroundVisibility(@NonNull View view, @NonNull @Nonnegative boolean mode) {
        int viewKey = view.hashCode();

        if (animationFlags.get(viewKey , true)) {
            boolean flag = view.getBackground().getAlpha() == 255;
            if (mode == flag) return null;

            animationFlags.put(viewKey, false);

            ValueAnimator alphaAnimator = ValueAnimator.ofInt(mode ? 0 : 255, mode ? 255 : 0);
            alphaAnimator.addUpdateListener(animation -> {
                int alpha = (int) animation.getAnimatedValue();
                view.getBackground().setAlpha(alpha);
            });
            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.getBackground().setVisible(mode, false);
                    animationFlags.put(viewKey , true);
                }
            });
            return alphaAnimator;
        } else return null;
    }
}
