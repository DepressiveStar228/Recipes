package com.example.recipes.Decoration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nonnegative;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Утилітний клас для анімації View в Android додатках.
 * Надає плавні анімації для обертання, з'явлення/зникнення, ковзання та зміни фону.
 */
public class AnimationUtils {
    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    public static final boolean SHOW = true;
    public static final boolean HIDE = false;

    private static final int FULL_ALPHA = 255;
    private static final int ZERO_ALPHA = 0;

    // Відстежує стан анімацій для запобігання накладання
    private static final SparseBooleanArray ANIMATION_FLAGS = new SparseBooleanArray();

    /**
     * Виконує плавне обертання View.
     *
     * @param view View для анімації
     * @param way Напрямок обертання
     * @param durationAnimation Тривалість анімації в мілісекундах
     * @param onAnimationEnd Колбек після завершення анімації
     */
    public static void smoothRotation(View view, int way, float rotationAngle, int durationAnimation, Runnable onAnimationEnd) {
        int viewId = view.getId();
        if (viewId == View.NO_ID) return;

        if (ANIMATION_FLAGS.get(viewId, true)) {
            ANIMATION_FLAGS.put(viewId, false);

            float currentRotation = view.getRotation();
            float targetRotation;

            if (way % 2 == 0) {
                targetRotation = currentRotation + rotationAngle;
            } else {
                targetRotation = currentRotation - rotationAngle;
            }

            ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", currentRotation, targetRotation);
            rotationAnimator.setDuration(durationAnimation);
            rotationAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ANIMATION_FLAGS.put(viewId, true);
                    onAnimationEnd.run();
                }
            });
            rotationAnimator.start();
        }
    }

    /**
     * Плавне зникнення/поява View.
     *
     * @param view View для анімації
     * @param mode SHOW або HIDE
     * @param durationAnimation Тривалість в мілісекундах
     */
    public static void smoothVisibility(@NonNull View view, @NonNull @Nonnegative boolean mode, @NonNull @Nonnegative int durationAnimation) {
        int viewKey  = view.hashCode();

        if (ANIMATION_FLAGS.get(viewKey, true)) {
            if (view.getVisibility() == View.VISIBLE) view.setAlpha(1f);
            if (view.getVisibility() == View.INVISIBLE || view.getVisibility() == View.GONE) view.setAlpha(0f);

            if (mode == (view.getAlpha() == 1f)) return;
            if (!mode == (view.getAlpha() == 0f)) return;

            ANIMATION_FLAGS.put(viewKey, false);

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
                    ANIMATION_FLAGS.put(viewKey, true);
                }
            });
            alphaAnimator.start();
        }
    }

    /**
     * Анімація зникнення/показу фону View.
     *
     * @param view View з фоном для анімації
     * @param mode SHOW або HIDE
     * @return Аніматор або null, якщо анімація не може стартувати
     */
    public static ValueAnimator backgroundVisibility(View view, boolean mode) {
        int viewKey = view.hashCode();

        if (ANIMATION_FLAGS.get(viewKey, true)) {
            boolean flag = view.getBackground().getAlpha() == FULL_ALPHA;
            if (mode == flag) return null;

            ANIMATION_FLAGS.put(viewKey, false);

            ValueAnimator alphaAnimator = ValueAnimator.ofInt(mode ? ZERO_ALPHA : FULL_ALPHA, mode ? FULL_ALPHA : ZERO_ALPHA);
            alphaAnimator.addUpdateListener(animation -> {
                int alpha = (int) animation.getAnimatedValue();
                view.getBackground().setAlpha(alpha);
            });
            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.getBackground().setVisible(mode, false);
                    ANIMATION_FLAGS.put(viewKey, true);
                }
            });
            return alphaAnimator;
        } else return null;
    }

    /**
     * Плавна зміна висоти View.
     *
     * @param view View для анімації
     * @param startHeight Початкова висота
     * @param endHeight Кінцева висота
     * @param duration Тривалість анімації в мілісекундах
     */
    public static void smoothHeightChange(View view, int startHeight, int endHeight, long duration, Runnable onAnimationEnd) {
        int viewKey = view.hashCode();

        if (ANIMATION_FLAGS.get(viewKey, true)) {
            if (startHeight == endHeight) return;

            ANIMATION_FLAGS.put(viewKey, false);

            ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = animatedValue;
                view.setLayoutParams(params);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onAnimationEnd != null) {
                        onAnimationEnd.run();
                    }
                    ANIMATION_FLAGS.put(viewKey, true);
                }
            });
            animator.start();
        }
    }
}
