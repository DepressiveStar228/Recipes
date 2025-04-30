package com.example.recipes.Activity;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.AI.ChatGPTClient;
import com.example.recipes.Adapter.DialogGPTAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.TrackingTask;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Decoration.AnimationUtils;
import com.example.recipes.Decoration.TextLoadAnimation;
import com.example.recipes.Enum.ChatGPTRole;
import com.example.recipes.Enum.ID_System_Collection;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.ViewItem.DialogItemContainer;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
public class GPTActivity extends AppCompatActivity implements LifecycleObserver {
    private RecipeUtils utils;
    private String nameActivity;
    private PreferencesController preferencesController;
    private TextView GPTStatusLoadTextView;
    private EditText GPTEditText;
    private ImageView GPTSendButton, GPTBack;
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
        preferencesController = PreferencesController.getInstance();
        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");

        super.onCreate(savedInstanceState);
        preferencesController.setPreferencesToActivity(this);
        setContentView(R.layout.gpt_activity);
        getLifecycle().addObserver(this);

        nameActivity = this.getClass().getSimpleName();
        utils = RecipeUtils.getInstance(this);
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

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            compositeDisposable.clear();
            for (TrackingTask task : trackingTasks) {
                task.stopTracking();
            }

            if (textLoadAnimation != null) textLoadAnimation.stopAnimation();

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
        GPTBack = findViewById(R.id.back);
        GPTDialogRecyclerView = findViewById(R.id.dialogRecyclerView);
    }

    private void loadClickListeners() {
        if (GPTSendButton != null && GPTDialogRecyclerView != null) { GPTSendButton.setOnClickListener(v -> {
            String message = GPTEditText.getText().toString();

            if (!flagAccessInternet.get()) Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            else if (!flagInitializationGPTClient.get()) Toast.makeText(this, getString(R.string.wait_load_assistant), Toast.LENGTH_SHORT).show();
            else if (message.isEmpty()) Toast.makeText(this, getString(R.string.warning_empty_message), Toast.LENGTH_SHORT).show();
            else if (client.checkDailyRequestLimitLimit() && client.checkLastTimeRequest()) {
                addMessageToDialog(getString(R.string.you), message);
                sendPromptToGPT(GPTEditText.getText().toString());
                GPTEditText.setText("");
            }
        }); }

        if (GPTEditText != null) CharacterLimitTextWatcher.setCharacterLimit(this, GPTEditText, Limits.MAX_CHAR_GPT_PROMPT);
        if (GPTStatusLoadTextView != null) textLoadAnimation = new TextLoadAnimation(GPTStatusLoadTextView, getString(R.string.loading));
        if (GPTBack != null) GPTBack.setOnClickListener(v -> finish());
    }

    /**
     * Встановлює трекери
     */
    private void setTrackingTasks() {
        // Перевіряємо підключення до інтернету кожну секунду
        TrackingTask trackingAccessInternet = new TrackingTask(1000);
        trackingAccessInternet.startTracking(() -> {
            if (flagAccessInternet.get() != AnotherUtils.checkInternet(this)) {
                flagAccessInternet.set(AnotherUtils.checkInternet(this));
                checkChanged.set(true);

                if (flagAccessInternet.get()) {
                    textLoadAnimation.setBaseText(getString(R.string.loading));

                    if (client == null || dialogGPTAdapter == null) {
                        setChatGPTClient();
                        setDialogGPTAdapter();
                    } else changeVisibilityItems(true);
                } else Toast.makeText(this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            } else if (!flagInitializationGPTClient.get() && !AnotherUtils.checkInternet(this)) {
                textLoadAnimation.setBaseTextIntoTextView(getString(R.string.error_network));
            }
        });
        trackingTasks.add(trackingAccessInternet);

        // Оновлюємо колір кнопки "Відправити" згідно доступу до інтернету та ініціалізації GPT клієнта
        TrackingTask trackingCorrectColorSendButton = new TrackingTask(1000);
        trackingCorrectColorSendButton.startTracking(() -> {
            if (GPTSendButton != null && checkChanged.get()) {
                checkChanged.set(false);

                if (flagAccessInternet.get() && flagInitializationGPTClient.get()) {
                    if (Objects.equals(preferencesController.getThemeString(), themeArray[1])) {
                        GPTSendButton.setColorFilter(Color.WHITE);
                    } else {
                        GPTSendButton.setColorFilter(Color.BLACK);
                    }
                } else GPTSendButton.setColorFilter(Color.GRAY);
            }
        });
        trackingTasks.add(trackingCorrectColorSendButton);
    }

    /**
     * Ініціалізує клієнт GPT.
     */
    private void setChatGPTClient() {
        if (client == null) {
            client = new ChatGPTClient(this, ChatGPTRole.CHEF);
            textLoadAnimation.startAnimation();

            Disposable disposable = client.initialization() // Ініціалізуємо клієнт
                    .flatMap(status -> {
                        textLoadAnimation.setBaseText(getString(R.string.get_history_messages));
                        if (status) return client.getAllDialogTread(); // Отримуємо весь діалог
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

    /**
     * Налаштовує адаптер для діалога з GPT.
     */
    private void setDialogGPTAdapter() {
        if (dialogGPTAdapter == null && client != null) {
            dialogGPTAdapter = new DialogGPTAdapter((text, position) -> {  // Слухач адаптера на клік кнопки "Додати страву"
                Dish dish = client.parsedAnswerGPT(text);

                if (dish != null) {
                    Disposable disposable = utils.ByDish().add(dish, ID_System_Collection.ID_GPT_RECIPE.getId())
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

        // Перевіряємо кожну секунду отримання історії діалогу
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

    /**
     * Додає повідомлення у чат з GPT.
     *
     * @param role відправник
     * @param message повідомлення
     */
    private void addMessageToDialog(String role, String message) {
        // Формуємо бокс повідомлення
        DialogItemContainer container = new DialogItemContainer(this);
        container.setRole_item(role);
        container.setOriginalText(message);

        if (!message.contains("{")) container.setText_item(message);
        else {  // Якщо повідомлення містить {, то отже це форматований рецепт, а не просто відповідь.
            container.setVisibilityAddButton(View.VISIBLE); // Показуємо кнопку додавання страви від GPT собі в колекцію

            // Пасимо форматований рецепт в страв, а потом в текст для відображення у повідомленні
            container.setText_item(client.getTextFromParsedAnswer(client.parsedAnswerGPT(message)));
        }

        if (dialogGPTAdapter != null && GPTDialogRecyclerView != null) {
            dialogGPTAdapter.addContainer(container);
            GPTDialogRecyclerView.post(() -> GPTDialogRecyclerView.scrollToPosition(dialogGPTAdapter.getItemCount() - 1));
        }
    }

    /**
     * Надсилає повідомлення у GPT
     *
     * @param message повідомлення
     */
    public void sendPromptToGPT(String message) {
        if (flagInitializationGPTClient.get() && !message.isEmpty()) {
            // Анімація відправки повідомлення GPT замість кнопки відправки
            AnimationDrawable animationDrawable = (AnimationDrawable) ContextCompat.getDrawable(this, R.drawable.loading_animation);
            GPTSendButton.setImageDrawable(animationDrawable);
            animationDrawable.start();

            Disposable disposable = client.sendMessage(message)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            answer -> {
                                animationDrawable.stop();

                                // Повертаємо зображення кнопки відправки замість анімації
                                if (Objects.equals(preferencesController.getThemeString(), themeArray[1])) {
                                    GPTSendButton.setImageResource(R.drawable.icon_send);
                                } else {
                                    GPTSendButton.setImageResource(R.drawable.icon_send);
                                    GPTSendButton.setColorFilter(R.color.white);
                                }

                                addMessageToDialog("GPT", answer);
                            },
                            throwable -> {
                                animationDrawable.stop();

                                // Повертаємо зображення кнопки відправки замість анімації
                                if (Objects.equals(preferencesController.getThemeString(), themeArray[1])) {
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
}
