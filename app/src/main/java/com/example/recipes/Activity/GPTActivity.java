package com.example.recipes.Activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.AI.ChatGPT;
import com.example.recipes.AI.ChatGPTClient;
import com.example.recipes.Adapter.DialogGPTAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.TrackingTask;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Decoration.AnimationUtils;
import com.example.recipes.Decoration.TextLoadAnimation;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.ViewItem.DialogItemContainer;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GPTActivity extends AppCompatActivity implements LifecycleObserver {
    private RecipeUtils utils;
    private String nameActivity;
    private PreferencesController preferencesController;
    private TextView GPTStatusLoadTextView;
    private EditText GPTEditText;
    private ImageView GPTSendButton;
    private RecyclerView GPTDialogRecyclerView;
    private CompositeDisposable compositeDisposable;
    private ChatGPTClient client;
    private DialogGPTAdapter dialogGPTAdapter;
    private TextLoadAnimation textLoadAnimation;
    private ArrayList<TrackingTask> trackingTasks;
    private String[] themeArray;
    private ArrayList<Pair<String, String>> messagesTread;
    private final AtomicBoolean flagInitializationGPTClient = new AtomicBoolean(false);
    private final AtomicBoolean flagInitializationMessagesTread = new AtomicBoolean(false);
    private final AtomicBoolean flagAccessInternet = new AtomicBoolean(false);
    private final AtomicBoolean checkChanged = new AtomicBoolean(false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferencesController = new PreferencesController();
        preferencesController.loadPreferences(this);
        themeArray = getStringArrayForLocale(R.array.theme_options, new Locale("en"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpt_activity);
        getLifecycle().addObserver(this);

        nameActivity = this.getClass().getSimpleName();
        utils = new RecipeUtils(this);
        trackingTasks = new ArrayList<>();
        messagesTread = new ArrayList<>();
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadItemsActivity();
        loadClickListeners();
        setTrackingTasks();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        try {
            super.onDestroy();
            compositeDisposable.clear();
            for (TrackingTask task : trackingTasks) {
                task.stopTracking();
            }
            textLoadAnimation.stopAnimation();

            Log.d(nameActivity, "Активність успішно закрита");
        } catch (IllegalStateException e) {
            Log.e(nameActivity, e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void loadItemsActivity() {
        GPTStatusLoadTextView = findViewById(R.id.statusLoadTextView);
        GPTEditText = findViewById(R.id.GPT_edit_text_my_dish);
        GPTSendButton = findViewById(R.id.send_promt_GPT_imageView);
        GPTDialogRecyclerView = findViewById(R.id.dialogRecyclerView);
    }

    private void loadClickListeners() {
        if (GPTSendButton != null && GPTDialogRecyclerView != null) { GPTSendButton.setOnClickListener(v -> {
            if (flagAccessInternet.get() && flagInitializationGPTClient.get()) {
                addMessageToDialog(getString(R.string.you), GPTEditText.getText().toString());
                sendPromptToGPT(GPTEditText.getText().toString());
                GPTEditText.setText("");
            }
            else if (!flagAccessInternet.get()) Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            else if (!flagInitializationGPTClient.get()) Toast.makeText(this, getString(R.string.wait_load_assistant), Toast.LENGTH_SHORT).show();
        }); }

        if (GPTEditText != null) CharacterLimitTextWatcher.setCharacterLimit(this, GPTEditText, Config.CHAR_LIMIT_GPT_PROMPT);

        if (GPTStatusLoadTextView != null) textLoadAnimation = new TextLoadAnimation(GPTStatusLoadTextView, getString(R.string.loading));
    }

    private void setTrackingTasks() {
        if (isDestroyed() || isFinishing()) return;

        TrackingTask trackingAccessInternet = new TrackingTask(1000);
        trackingAccessInternet.startTracking(() -> {
            if (flagAccessInternet.get() != checkInternet()) {
                flagAccessInternet.set(checkInternet());
                checkChanged.set(true);

                if (flagAccessInternet.get()) {
                    textLoadAnimation.setBaseText(getString(R.string.loading));

                    if (client == null || dialogGPTAdapter == null) {
                        setChatGPTClient();
                        setDialogGPTAdapter();
                    } else changeVisibilityItems(true);
                } else Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            } else if (!flagInitializationGPTClient.get() && !checkInternet()) {
                textLoadAnimation.setBaseTextIntoTextView(getString(R.string.error_network));
            }
        });
        trackingTasks.add(trackingAccessInternet);

        TrackingTask trackingCorrectColorSendButton = new TrackingTask(1000);
        trackingCorrectColorSendButton.startTracking(() -> {
            if (GPTSendButton != null && checkChanged.get()) {
                checkChanged.set(false);

                if (flagAccessInternet.get() && flagInitializationGPTClient.get()) {
                    if (Objects.equals(preferencesController.getTheme(), themeArray[1])) {
                        GPTSendButton.setColorFilter(Color.WHITE);
                    } else {
                        GPTSendButton.setColorFilter(Color.BLACK);
                    }
                } else GPTSendButton.setColorFilter(Color.GRAY);
            }
        });
        trackingTasks.add(trackingCorrectColorSendButton);
    }

    private void setChatGPTClient() {
        if (isDestroyed() || isFinishing()) return;

        if (client == null) {
            client = new ChatGPTClient(this, ChatGPT.CHEF);
            textLoadAnimation.startAnimation();

            Disposable disposable = client.initialization()
                    .flatMap(status -> {
                        if (isDestroyed() || isFinishing()) return null;

                        textLoadAnimation.setBaseText(getString(R.string.get_history_messages));
                        if (status) return client.getAllDialogTread();
                        else return null;
                    })
                    .flatMap(messages -> {
                        if (messages != null) {
                            messagesTread = new ArrayList<>(messages);
                            return Single.just(true);
                        } else return Single.just(false);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            status -> {
                                if (isDestroyed() || isFinishing()) return;

                                flagInitializationGPTClient.set(true);
                                checkChanged.set(true);
                                changeVisibilityItems(true);
                            },
                            throwable -> {
                                flagInitializationGPTClient.set(false);
                                textLoadAnimation.setBaseText(getString(R.string.error_get_data));
                                Log.e(nameActivity, "Initialization failed: " + throwable.getMessage());
                                throwable.printStackTrace();
                            }
                    );
            compositeDisposable.add(disposable);
        }
    }

    private void setDialogGPTAdapter() {
        if (isDestroyed() || isFinishing()) return;

        if (dialogGPTAdapter == null && client != null) {
            dialogGPTAdapter = new DialogGPTAdapter((text, position) -> {
                Dish dish = client.parsedAnswerGPT(text);

                if (dish != null) {
                    Disposable disposable = utils.ByDish().add(dish, Config.ID_GPT_RECIPE_COLLECTION)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(id -> {
                                        if (id > 0) {
                                            dialogGPTAdapter.dishAdded(position);
                                            Toast.makeText(this, getString(R.string.successful_add_dish), Toast.LENGTH_SHORT).show();
                                        }
                                        else Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> Toast.makeText(this, getString(R.string.error_add_dish), Toast.LENGTH_SHORT).show()
                            );
                    compositeDisposable.add(disposable);
                }
            });

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            GPTDialogRecyclerView.setAdapter(dialogGPTAdapter);
            GPTDialogRecyclerView.setLayoutManager(linearLayoutManager);
            if (GPTDialogRecyclerView.getItemDecorationCount() == 0) {
                GPTDialogRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(15));
            }
        }

        TrackingTask trackingLoadingMessages = new TrackingTask(1000);
        trackingLoadingMessages.startTracking(() -> {
            if (!flagInitializationMessagesTread.get() && !messagesTread.isEmpty()) {
                flagInitializationMessagesTread.set(true);

                for (Pair<String, String> message : messagesTread) {
                    addMessageToDialog(message.first, message.second);
                }
            }
        });
        trackingTasks.add(trackingLoadingMessages);
    }

    private void changeVisibilityItems(boolean flag) {
        if (flag) {
            textLoadAnimation.stopAnimation();
            AnimationUtils.smoothVisibility(GPTStatusLoadTextView, AnimationUtils.HIDE, 300);
        } else {
            textLoadAnimation.setBaseText(getString(R.string.error_get_data));
            textLoadAnimation.stopAnimation();
            AnimationUtils.smoothVisibility(GPTStatusLoadTextView, AnimationUtils.SHOW, 100);
        }
    }

    private void addMessageToDialog(String role, String message) {
        if (isDestroyed() || isFinishing() || GPTDialogRecyclerView == null) return;

        if (!message.isEmpty()) {
            DialogItemContainer container = new DialogItemContainer(this);
            container.setRole_item(role);
            container.setOriginalText(message);

            if (!message.contains("{")) container.setText_item(message);
            else {
                container.setVisibilityAddButton(View.VISIBLE);
                container.setText_item(client.getTextFromParsedAnswer(client.parsedAnswerGPT(message)));
            }

            if (dialogGPTAdapter != null && GPTDialogRecyclerView != null) {
                dialogGPTAdapter.addContainer(container);
                GPTDialogRecyclerView.post(() -> GPTDialogRecyclerView.scrollToPosition(dialogGPTAdapter.getItemCount() - 1));
            }
        }
    }

    public void sendPromptToGPT(String message) {
        if (isDestroyed() || isFinishing()) return;

        if (flagInitializationGPTClient.get() && !message.isEmpty()) {
            AnimationDrawable animationDrawable = (AnimationDrawable) ContextCompat.getDrawable(this, R.drawable.loading_animation);
            GPTSendButton.setImageDrawable(animationDrawable);
            animationDrawable.start();

            Disposable disposable = client.sendMessage(message)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            answer -> {
                                animationDrawable.stop();

                                if (Objects.equals(preferencesController.getTheme(), themeArray[1])) {
                                    GPTSendButton.setImageResource(R.drawable.icon_send);
                                } else {
                                    GPTSendButton.setImageResource(R.drawable.icon_send);
                                    GPTSendButton.setColorFilter(R.color.white);
                                }

                                addMessageToDialog("GPT", answer);
                            },
                            throwable -> {
                                animationDrawable.stop();

                                if (Objects.equals(preferencesController.getTheme(), themeArray[1])) {
                                    GPTSendButton.setImageResource(R.drawable.icon_send);
                                } else {
                                    GPTSendButton.setImageResource(R.drawable.icon_send);
                                    GPTSendButton.setColorFilter(R.color.white);
                                }
                                Log.e(nameActivity, "Error: " + throwable);
                            }
                    );
            compositeDisposable.add(disposable);
        }
    }

    private String[] getStringArrayForLocale(int resId, Locale locale) {
        Resources resources = this.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        Context localizedContext = new ContextWrapper(this).createConfigurationContext(config);
        return localizedContext.getResources().getStringArray(resId);
    }

    private boolean checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
