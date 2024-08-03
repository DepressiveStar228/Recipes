package com.example.recipes.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.ChatGPTClient;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpResponseException;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class GPTFragment extends Fragment {
    private TextView GPTTextView, GPTTextView2;
    private EditText GPTEditText;
    private ImageView sendPromptImageView;
    private LinearLayout dialogContainer;
    private ConstraintLayout GPT_constraintLayout;
    private ScrollView GPT_mainLayout;
    private PerferencesController perferencesController;
    private ChatGPTClient client;
    private String[] themeArray;
    private boolean addRecipeStatus = false;
    private boolean networkStatus = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gpt_fragment, container, false);
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(getContext());

        client = new ChatGPTClient(getContext());

        loadItemsActivity(view);
        loadClickListeners();

        return view;
    }

    private void loadItemsActivity(View view){
        GPT_constraintLayout = view.findViewById(R.id.GPT_constraintLayout);
        GPT_mainLayout = view.findViewById(R.id.scrollBox_dialogContainer);
        dialogContainer = GPT_constraintLayout.findViewById(R.id.dialogContainer);

        GPTEditText = view.findViewById(R.id.GPT_edit_text_my_dish);
        sendPromptImageView = view.findViewById(R.id.send_promt_GPT_imageView);
        sendPromptImageView.setOnClickListener(v -> sendPromptToGPT());

        GPTTextView = GPT_constraintLayout.findViewById(R.id.GPT_main_textView);
        GPTTextView2 = GPT_constraintLayout.findViewById(R.id.GPT_main_textView2);

        themeArray = getStringArrayForLocale(R.array.theme_options, new Locale("en"));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners(){
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), GPTEditText, Config.CHAR_LIMIT_GPT_PROMPT);
    }

    public void openGPTContainer(){
        if (checkInternet()) {
            hideGptText();
            increaseGptContainer();
            showGPT_mainLayout();
            showGPTEditText();
            showGPTImageView();
        }
    }

    public void closeGPTContainer(){
        hideGPTEditText();
        hideGPTImageView();
        showGptText();
        hideGPT_mainLayout();
        decreaseGptContainer();
    }

    private void showGPTEditText(){
        GPTEditText.setAlpha(0f);
        GPTEditText.setVisibility(View.VISIBLE);
        GPTEditText.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
                .start();
    }

    private void hideGPTEditText() {
        GPTEditText.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        GPTEditText.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void showGPT_mainLayout(){
        GPT_mainLayout.setAlpha(0f);
        GPT_mainLayout.setVisibility(View.VISIBLE);
        GPT_mainLayout.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
                .start();
    }

    private void hideGPT_mainLayout() {
        GPT_mainLayout.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        GPT_mainLayout.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void showGPTImageView(){
        sendPromptImageView.setAlpha(0f);
        sendPromptImageView.setVisibility(View.VISIBLE);
        sendPromptImageView.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(null)
                .start();
    }

    private void hideGPTImageView() {
        sendPromptImageView.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        sendPromptImageView.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void showGptText() {
        GPTTextView.setAlpha(0f);
        GPTTextView.setVisibility(View.VISIBLE);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(GPTTextView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(300);

        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(GPTTextView, "translationY", -50f, 0f);
        translationAnimator.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translationAnimator);
        animatorSet.start();

        GPTTextView2.setAlpha(0f);
        GPTTextView2.setVisibility(View.VISIBLE);

        ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(GPTTextView2, "alpha", 0f, 1f);
        alphaAnimator2.setDuration(300);

        ObjectAnimator translationAnimator2 = ObjectAnimator.ofFloat(GPTTextView2, "translationY", -50f, 0f);
        translationAnimator2.setDuration(300);

        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.playTogether(alphaAnimator2, translationAnimator2);
        animatorSet2.start();
    }

    private void hideGptText() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(GPTTextView, "alpha", 1f, 0f);
        alphaAnimator.setDuration(300);

        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(GPTTextView, "translationY", 0f, -50f);
        translationAnimator.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alphaAnimator, translationAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                GPTTextView.setVisibility(View.INVISIBLE);
            }
        });
        animatorSet.start();

        ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(GPTTextView2, "alpha", 1f, 0f);
        alphaAnimator2.setDuration(300);

        ObjectAnimator translationAnimator2 = ObjectAnimator.ofFloat(GPTTextView2, "translationY", 0f, -50f);
        translationAnimator2.setDuration(300);

        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.playTogether(alphaAnimator2, translationAnimator2);
        animatorSet2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                GPTTextView2.setVisibility(View.INVISIBLE);
            }
        });
        animatorSet2.start();
    }

    private void increaseGptContainer() {
        int heightIncrease = dpToPx(350);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) GPT_constraintLayout.getLayoutParams();
        params.height = GPT_constraintLayout.getHeight() + heightIncrease;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(GPT_constraintLayout);
        constraintSet.constrainHeight(R.id.GPT_constraintLayout, params.height);

        TransitionManager.beginDelayedTransition((ViewGroup) GPT_constraintLayout.getParent());
        constraintSet.applyTo(GPT_constraintLayout);
    }

    private void decreaseGptContainer() {
        int heightDecrease = dpToPx(350);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) GPT_constraintLayout.getLayoutParams();
        params.height = GPT_constraintLayout.getHeight() - heightDecrease;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(GPT_constraintLayout);
        constraintSet.constrainHeight(R.id.GPT_constraintLayout, params.height);

        TransitionManager.beginDelayedTransition((ViewGroup) GPT_constraintLayout.getParent());
        constraintSet.applyTo(GPT_constraintLayout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    @SuppressLint("StaticFieldLeak")
    private void sendPromptToGPT() {
        String prompt = GPTEditText.getText().toString().trim();

        dialogContainer.addView(addMessageToDialog(getString(R.string.you) + ":\n" + prompt));
        GPTEditText.setText("");

        if (!client.checkLimit(prompt)){
            return;
        }

        AnimationDrawable animationDrawable = (AnimationDrawable) ContextCompat.getDrawable(getContext(), R.drawable.loading_animation);
        sendPromptImageView.setImageDrawable(animationDrawable);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                animationDrawable.start();

                try {
                    return client.getResponse(prompt);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();

                    if (e instanceof HttpResponseException) {
                        HttpResponseException httpException = (HttpResponseException) e;
                        if (httpException.getStatusCode() == 429) {
                            try {
                                Thread.sleep(5000);
                                return client.getResponse(prompt);
                            } catch (InterruptedException | IOException ex) {
                                ex.printStackTrace();
                            } catch (JSONException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                    return null;
                }
            }


            @Override
            protected void onPostExecute(String response) {
                animationDrawable.stop();

                if (isAdded()){
                    if (Objects.equals(perferencesController.theme, themeArray[0])) {
                        sendPromptImageView.setImageResource(R.drawable.icon_send);
                    } else {
                        sendPromptImageView.setImageResource(R.drawable.icon_send_darkmode);
                    }

                    if (response != null) {
                        DataBox recipeData = null;

                        if (response.contains("{") && response.contains("}")) {
                            recipeData = ImportExportController.importRecipeDataFromGPT(getContext(), response);
                        }

                        if (recipeData != null) {
                            dialogContainer.addView(addMessageToDialog(recipeData));
                        } else {
                            dialogContainer.addView(addMessageToDialog("GPT:\n" + response));
                        }
                    } else {
                        dialogContainer.addView(addMessageToDialog("GPT:\n" + getString(R.string.error_get_response)));
                    }
                }
            }
        }.execute();
    }

    private View addMessageToDialog(String message) {
        ConstraintLayout messageLayout = new ConstraintLayout(getContext());
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        messageLayout.setLayoutParams(layoutParams);
        messageLayout.setPadding(0, 0, 0, dpToPx(20));

        TextView textView = new TextView(getContext());
        textView.setId(View.generateViewId());
        textView.setText(message);
        textView.setLayoutParams(new ConstraintLayout.LayoutParams(800, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        if (Objects.equals(perferencesController.theme, themeArray[0])) {
            textView.setTextColor(getResources().getColor(R.color.black));
        } else {
            textView.setTextColor(getResources().getColor(R.color.white));
        }
        textView.setTextSize(16);

        messageLayout.addView(textView);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(messageLayout);
        constraintSet.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        constraintSet.connect(textView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);

        constraintSet.applyTo(messageLayout);
        return messageLayout;
    }

    private View addMessageToDialog(DataBox recipeData) {
        ConstraintLayout messageLayout = new ConstraintLayout(getContext());
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        messageLayout.setLayoutParams(layoutParams);
        messageLayout.setPadding(0, 0, 0, dpToPx(20));


        TextView textView = new TextView(getContext());
        textView.setId(View.generateViewId());
        textView.setText("GPT:\n" + parseDataBox(recipeData));
        textView.setLayoutParams(new ConstraintLayout.LayoutParams(800, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        if (Objects.equals(perferencesController.theme, themeArray[0])) {
            textView.setTextColor(getResources().getColor(R.color.black));
        } else {
            textView.setTextColor(getResources().getColor(R.color.white));
        }
        textView.setTextSize(16);


        ImageView imageView = new ImageView(getContext());
        imageView.setId(View.generateViewId());
        imageView.setVisibility(View.VISIBLE);
        if (Objects.equals(perferencesController.theme, themeArray[0])) {
            imageView.setImageResource(R.drawable.icon_add);
        } else {
            imageView.setImageResource(R.drawable.icon_add_darkmode);
        }
        imageView.setLayoutParams(new ConstraintLayout.LayoutParams(90, 90));
        imageView.setOnClickListener(v -> {
            RecipeUtils utils = new RecipeUtils(getContext());

            if (!addRecipeStatus) {
                if (utils.addRecipe(recipeData, Config.ID_GPT_RECIPE_COLLECTION)) {
                    if (Objects.equals(perferencesController.theme, themeArray[0])) {
                        imageView.setImageResource(R.drawable.icon_check_mark);
                    } else {
                        imageView.setImageResource(R.drawable.icon_check_mark_darkmode);
                    }
                    addRecipeStatus = true;
                    Toast.makeText(getContext(), getContext().getString(R.string.successful_add_dishes), Toast.LENGTH_SHORT).show();
                } else {
                    addRecipeStatus = false;
                }
            } else {
                if (utils.deleteRecipe(recipeData)) {
                    imageView.setImageResource(R.drawable.icon_add_darkmode);
                    addRecipeStatus = false;
                    Toast.makeText(getContext(), getContext().getString(R.string.successful_delete_dish), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.error_delete_dish), Toast.LENGTH_SHORT).show();
                    addRecipeStatus = true;
                }
            }
        });

        messageLayout.addView(textView);
        messageLayout.addView(imageView);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(messageLayout);
        constraintSet.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        constraintSet.connect(textView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);

        constraintSet.connect(imageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        constraintSet.connect(imageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);

        constraintSet.applyTo(messageLayout);
        return messageLayout;
    }

    private String parseDataBox(DataBox box) {
        ArrayList<Dish> dishes = box.getDishes();
        ArrayList<Ingredient> ingredients = box.getIngredients();
        String data = "";

        for (Dish dish : dishes) {
            data += dish.getName() + "\n\n" + getString(R.string.recipe) + "\n" + dish.getRecipe() + "\n\n" + getString(R.string.ingredients) + "\n";
        }

        for (Ingredient in : ingredients) {
            data += "- " + in.getAmount() + " " + in.getType() + " " + in.getName() + "\n";
        }

        return data;
    }

    private boolean checkInternet(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            if (!networkStatus) { Toast.makeText(getContext(), getContext().getString(R.string.successful_network), Toast.LENGTH_SHORT).show(); }
            networkStatus = true;
            Log.d("MainActivity", "Доступ до Інтернету є");
        } else {
            networkStatus = false;
            Toast.makeText(getContext(), getContext().getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Доступ до Інтернету немає");
        }

        return networkStatus;
    }

    private String[] getStringArrayForLocale(int resId, Locale locale) {
        Resources resources = this.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        Context localizedContext = new ContextWrapper(getContext()).createConfigurationContext(config);
        return localizedContext.getResources().getStringArray(resId);
    }
}
