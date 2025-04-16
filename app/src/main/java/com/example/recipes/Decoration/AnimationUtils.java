package com.example.recipes.Decoration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.SparseBooleanArray;
import android.view.View;

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

    private static final float ROTATION_ANGLE = 45f;

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
    public static void smoothRotation(@NonNull View view, @NonNull @Nonnegative int way, @NonNull @Nonnegative int durationAnimation, @NonNull Runnable onAnimationEnd) {
        int viewId = view.getId();
        if (viewId == View.NO_ID) return;

        if (ANIMATION_FLAGS.get(viewId, true)) {
            ANIMATION_FLAGS.put(viewId, false);

            ObjectAnimator rotationAnimator = new ObjectAnimator();

            if (way % 2 == 0) rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, ROTATION_ANGLE);
            else rotationAnimator = ObjectAnimator.ofFloat(view, "rotation", ROTATION_ANGLE, 0f);

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
     * Анімація ковзання View з ефектом зникнення.
     *
     * @param view View для анімації
     * @param way Напрямок руху
     * @param mode SHOW або HIDE
     * @param translationValue Відстань ковзання
     * @param durationAnimation Тривалість в мілісекундах
     * @param onAnimationEnd Колбек після завершення
     */
    public static void smoothSlipVisibility(@NonNull View view, @NonNull @Nonnegative int way,
                                            @NonNull @Nonnegative boolean mode, @NonNull @Nonnegative float translationValue,
                                            @NonNull @Nonnegative int durationAnimation, @NonNull Runnable onAnimationEnd) {
        int viewId = view.getId();
        if (viewId == View.NO_ID) return;

        if (ANIMATION_FLAGS.get(viewId, true)) {
            ANIMATION_FLAGS.put(viewId, false);
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
                    ANIMATION_FLAGS.put(viewId, true);
                    onAnimationEnd.run();

                    if (mode) { view.setVisibility(View.VISIBLE); }
                    else { view.setVisibility(View.GONE); }
                }
            });
            animatorSet.start();
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
    public static ValueAnimator backgroundVisibility(@NonNull View view, @NonNull @Nonnegative boolean mode) {
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
}
