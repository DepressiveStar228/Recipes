package com.example.recipes.AI;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.azure.ai.openai.assistants.AssistantsClient;
import com.azure.ai.openai.assistants.AssistantsClientBuilder;
import com.azure.ai.openai.assistants.models.ApiResponseFormat;
import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormat;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormatOption;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.MessageContent;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.ResourceNotFoundException;
import com.example.recipes.API_Keys;
import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Enum.ChatGPTRole;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Enum.IngredientType;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;


/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для взаємодії з API ChatGPT.
 * Відповідає за створення, збереження та управління асистентами та діалогами (threads) у ChatGPT.
 * Підтримує два типи асистентів: "Chef" (кухар) та "Translator" (перекладач).
 */
public class ChatGPTClient {
    private static final String PREF_CHEF_NAME = "assistant_chef_prefs";
    private static final String KEY_CHEF_ID = "chef_id";
    private static final String KEY_CHEF_THREAD_ID = "chef_thread_id";

    private static final String PREF_TRANSLATOR_NAME = "assistant_translator_prefs";
    private static final String KEY_TRANSLATOR_ID = "translator_id";
    private static final String KEY_TRANSLATOR_THREAD_ID = "translator_thread_id";

    private static final String PREFS_DAILY_LIMIT_NAME = "dailyLimitPrefs";
    private static final String KEY_DAILY_LIMIT = "dailyRequestCount";
    private static final String KEY_LAST_RESET_TIME = "lastResetTime";

    private static final long MIN_REQUEST_INTERVAL_MS = 5000;
    private static final int MAX_REQUESTS_PER_DAY = 15;
    private int dailyRequestCount = 0;
    private long lastRequestTime = 0;

    private final Context context;
    private final ChatGPTRole roleAssistant;
    private AssistantsClient client;
    private Assistant assistant;
    private AssistantThread thread;


    /**
     * Конструктор класу ChatGPTClient.
     *
     * @param context       Контекст додатку.
     * @param roleAssistant Роль асистента (Chef або Translator).
     */
    public ChatGPTClient(Context context, ChatGPTRole roleAssistant) {
        this.context = context;
        this.roleAssistant = roleAssistant;

        // Налаштування обробника помилок RxJava
        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException) {
                Throwable cause = throwable.getCause();

                // Відловлючання випадків, коли запит надходить у вже знищену активність
                if (cause instanceof RuntimeException && cause.getCause() instanceof InterruptedException) {
                    Log.e("RxJavaError", "Thread interrupted: " + cause.getMessage());
                    Thread.currentThread().interrupt();
                } else {
                    Log.e("RxJavaError", "Undeliverable exception: " + cause.getMessage());
                }
            } else {
                Log.e("RxJavaError", "Unexpected error: " + throwable.getMessage());
            }
        });
    }

    /**
     * Ініціалізація асистента.
     * Перевіряє ліміт запитів та ініціалізує асистента відповідно до його ролі.
     *
     * @return Single<Boolean>, який повертає true, якщо ініціалізація успішна, або false, якщо ні.
     */
    public Single<Boolean> initialization() {
        checkResetDailyLimit();

        if (Objects.equals(roleAssistant, ChatGPTRole.CHEF)) return initializationChef();
        else if (Objects.equals(roleAssistant, ChatGPTRole.TRANSLATOR)) return initializationTranslator();
        else return Single.just(false);
    }

    /**
     * Ініціалізація асистента-кухаря.
     * Отримує або створює асистента та діалог, зберігає їх стан у SharedPreferences.
     *
     * @return Single<Boolean>, який повертає true, якщо ініціалізація успішна, або false, якщо ні.
     */
    private Single<Boolean> initializationChef() {
        return Single.create(emitter -> {
            // Отримання збережених ID асистента та діалогу
            SharedPreferences prefs = context.getSharedPreferences(PREF_CHEF_NAME, Context.MODE_PRIVATE);
            String assistantId = prefs.getString(KEY_CHEF_ID, null);
            String threadId = prefs.getString(KEY_CHEF_THREAD_ID, null);

            client = createClient();

            // Отримання або створення асистента
            if (assistantId != null) {
                try {
                    assistant = client.getAssistant(assistantId);
                } catch (ResourceNotFoundException e) {
                    assistant = createAssistant();
                    saveAssistantState();
                }
            }
            else {
                assistant = createAssistant();
                saveAssistantState();
            }

            // Отримання або створення діалогу
            if (threadId != null) {
                thread = client.getThread(threadId);

                try {
                    thread = client.getThread(threadId);
                } catch (ResourceNotFoundException e) {
                    thread = createThread();
                    saveAssistantState();
                }
            }
            else {
                thread = createThread();
                saveAssistantState();
            }

            // Перевірка наявності клієнта, асистента та діалогу
            if (client == null) { emitter.onError(new Throwable("Client is null")); }
            if (assistant == null) { emitter.onError(new Throwable("Assistant is null")); }
            if (thread == null) { emitter.onError(new Throwable("Thread is null")); }

            emitter.onSuccess(true);
        });
    }

    /**
     * Ініціалізація асистента-перекладача.
     * Отримує або створює асистента та діалог, зберігає їх стан у SharedPreferences.
     *
     * @return Single<Boolean>, який повертає true, якщо ініціалізація успішна, або false, якщо ні.
     */
    private Single<Boolean> initializationTranslator() {
        return Single.create(emitter -> {
            // Отримуємо з налаштувань ID асистента та його діалога для подальшого використання, щоб не створювати нових на сервері OpenAI
            SharedPreferences prefs = context.getSharedPreferences(PREF_TRANSLATOR_NAME, Context.MODE_PRIVATE);
            String assistantId = prefs.getString(KEY_TRANSLATOR_ID, null);
            String threadId = prefs.getString(KEY_TRANSLATOR_THREAD_ID, null);

            client = createClient();

            // Отримання збережених ID асистента та діалогу
            if (assistantId != null) {
                try {
                    assistant = client.getAssistant(assistantId);
                } catch (ResourceNotFoundException e) {
                    assistant = createAssistant();
                    saveAssistantState();
                }
            }
            else {
                assistant = createAssistant();
                saveAssistantState();
            }

            // Отримання або створення діалогу
            if (threadId != null) {
                thread = client.getThread(threadId);

                try {
                    thread = client.getThread(threadId);
                } catch (ResourceNotFoundException e) {
                    thread = createThread();
                    saveAssistantState();
                }
            }
            else {
                thread = createThread();
                saveAssistantState();
            }

            // Перевірка наявності клієнта, асистента та діалогу
            if (client == null) { emitter.onError(new Throwable("Client is null")); }
            if (assistant == null) { emitter.onError(new Throwable("Assistant is null")); }
            if (thread == null) { emitter.onError(new Throwable("Thread is null")); }

            emitter.onSuccess(true);
        });
    }

    /**
     * Створення клієнта для взаємодії з API ChatGPT.
     *
     * @return Створений клієнт AssistantsClient.
     */
    private AssistantsClient createClient() {
        return new AssistantsClientBuilder()
                .credential(new KeyCredential(API_Keys.GPT_KEY))
                .buildClient();
    }

    /**
     * Створення асистента відповідно до його ролі.
     *
     * @return Створений асистент.
     */
    private Assistant createAssistant() {
        AssistantCreationOptions assistantCreationOptions;

        if (Objects.equals(roleAssistant, ChatGPTRole.CHEF)) {
            assistantCreationOptions = new AssistantCreationOptions("o3-mini");
            assistantCreationOptions.setName("Chef").setInstructions(readTextFileFromAssets("asset_chef"));
        }
        else if (Objects.equals(roleAssistant, ChatGPTRole.TRANSLATOR)) {
            assistantCreationOptions = new AssistantCreationOptions("gpt-3.5-turbo");
            assistantCreationOptions.setName("Translator").setInstructions(readTextFileFromAssets("asset_translator"));
            assistantCreationOptions.setTemperature(0.0);
            assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.JSON_OBJECT)));
        }
        else assistantCreationOptions = null;

        return client.createAssistant(assistantCreationOptions);
    }

    /**
     * Створення нового діалогу (thread).
     *
     * @return Створений діалог.
     */
    private AssistantThread createThread() {
        return client.createThread(new AssistantThreadCreationOptions());
    }

    /**
     * Отримання всіх повідомлень з поточного діалогу.
     *
     * @return Single<List<Pair<String, String>>>, який містить список пар (роль, текст повідомлення).
     */
    public Single<List<Pair<String, String>>> getAllDialogTread() {
        return Single.create(emitter -> {
            if (thread != null) {
                ArrayList<Pair<String, String>> messagesList = new ArrayList<>();
                try {
                    PageableList<ThreadMessage> messages = client.listMessages(thread.getId());
                    List<ThreadMessage> data = messages.getData();

                    for (ThreadMessage message : data) {
                        MessageRole role = message.getRole();
                        String roleName;

                        if (Objects.equals(role.toString(), "assistant")) roleName = "GPT";
                        else roleName = context.getString(R.string.you);


                        for (MessageContent messageContent : message.getContent()) {
                            if (messageContent instanceof MessageTextContent textContent) {
                                messagesList.add(0, new Pair<>(roleName, textContent.getText().getValue()));
                            }
                        }
                    }

                    emitter.onSuccess(messagesList);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            } else emitter.onError(new Throwable("Tread is null"));
        });
    }

    /**
     * Надсилає повідомлення до ChatGPT та отримує відповідь.
     *
     * @param message Повідомлення, яке потрібно надіслати.
     * @return Single<String>, який містить відповідь від ChatGPT або помилку.
     */
    public Single<String> sendMessage(String message) {
        return Single.create(emitter -> {
            if (client != null && assistant != null && thread != null) {
                try {
                    // Надсилання повідомлення до діалогу
                    client.createMessage(thread.getId(), new ThreadMessageOptions(MessageRole.USER, message));

                    // Запуск обробки повідомлення асистентом
                    ThreadRun run = client.createRun(thread.getId(), new CreateRunOptions(assistant.getId()));

                    // Очікування завершення обробки повідомлення
                    do {
                        Thread.sleep(500);
                        run = client.getRun(run.getThreadId(), run.getId());
                    } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);

                    // Отримання відповіді від асистента
                    PageableList<ThreadMessage> messages = client.listMessages(run.getThreadId());
                    List<ThreadMessage> data = messages.getData();

                    if (!data.isEmpty()) {
                        ThreadMessage lastMessage = data.get(0);

                        // Обробка відповіді
                        for (MessageContent messageContent : lastMessage.getContent()) {
                            if (messageContent instanceof MessageTextContent) {
                                MessageTextContent messageTextContent = (MessageTextContent) messageContent;
                                String box = messageTextContent.getText().getValue();
                                if (!box.isEmpty()) {
                                    dailyRequestCount++;
                                    lastRequestTime = System.currentTimeMillis();
                                    saveDailyLimit();
                                    emitter.onSuccess(box); // Повертаємо відповідь
                                } else { emitter.onError(new Throwable("Answer is empty")); }
                            } else { emitter.onError(new Throwable("MessageContent is not MessageTextContent")); }
                        }
                    } else { emitter.onError(new Throwable("Dara is empty")); }
                } catch (Exception e) {
                    emitter.onError(e);
                }
            } else { emitter.onError(new Throwable("Data is null")); }
        });
    }

    /**
     * Форматує об'єкт Dish у зрозумілий для користувача текст.
     *
     * @param dish Об'єкт Dish, який потрібно форматувати.
     * @return Рядок у зрозумілому для користувача форматі.
     */
    public String getTextFromParsedAnswer(Dish dish) {
        if (dish != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(dish.getName() + "\n\n").append(context.getText(R.string.portionship) + ": " + dish.getPortion() + "\n\n");

            if (!dish.getRecipes().isEmpty()) {
                builder.append(context.getText(R.string.recipe) + ":\n");
                for (DishRecipe dishRecipe : dish.getRecipes()) {
                    builder.append(dishRecipe.getTextData() + "\n");
                }
                builder.append("\n");
            }

            if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
                builder.append(context.getText(R.string.ingredients) + ":\n");

                for (Ingredient ing : dish.getIngredients()) {
                    builder.append("  - " + ing.getName() + "  " + ing.getAmount() + " " + IngredientTypeConverter.fromIngredientTypeBySettingLocale(ing.getType()) + "\n");
                }
            }

            return builder.toString();
        } else return "";
    }

    /**
     * Парсить відповідь від ChatGPT у об'єкт Dish.
     *
     * @param answer Відповідь від ChatGPT.
     * @return Об'єкт Dish, створений на основі відповіді.
     */
    public Dish parsedAnswerGPT(String answer) {
        String name = "";
        int portion = 0;
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        ArrayList<DishRecipe> recipes = new ArrayList<>();

        String data = answer.contains("Data:") ? answer.substring(16) : answer;
        String[] pairs = data.split(";");

        for (String pair : pairs) {
            if (pair.contains("=")) {
                String[] keyValue = pair.split("=");

                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    if (key.equals("name")) name = value;
                    else if (key.equals("portion")) {
                        try { portion = Integer.parseInt(value); }
                        catch (Exception ignored) { }
                    }
                    else if (key.equals("recipe")) recipes.add(new DishRecipe(value, 1, DishRecipeType.TEXT));
                    else if (key.equals("ingredients")) {
                        String[] ingredientsValue = value.split(",");

                        for (String pairIngs : ingredientsValue) {
                            if (pairIngs.contains("|")) {
                                String[] ingredientValue = pairIngs.split("\\|");

                                String nameIng = "", amount = "";
                                IngredientType type = IngredientType.VOID;

                                if (ingredientValue.length == 3) {
                                    nameIng = ingredientValue[0].trim();
                                    amount = ingredientValue[1].trim();
                                    type = IngredientTypeConverter.toIngredientType(ingredientValue[2].trim());

                                    ingredients.add(new Ingredient(nameIng, amount, type));
                                }
                                else if (ingredientValue.length == 2) {
                                    nameIng = ingredientValue[0].trim();

                                    try {
                                        int box = Integer.parseInt(ingredientValue[1].trim());
                                        amount = ingredientValue[1].trim();
                                    }
                                    catch (Exception ignored) {
                                        type = IngredientTypeConverter.toIngredientType(ingredientValue[1].trim());
                                    }

                                    ingredients.add(new Ingredient(nameIng, amount, type));
                                }
                            }
                        }
                    }
                }
            }
        }

        return new Dish(name, portion, ingredients, recipes, System.currentTimeMillis());
    }

    /**
     * Зберігає стан асистента (ID асистента та діалогу) у SharedPreferences.
     */
    private void saveAssistantState() {
        if (Objects.equals(roleAssistant, ChatGPTRole.CHEF)) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_CHEF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (assistant != null) { editor.putString(KEY_CHEF_ID, assistant.getId()); }
            if (thread != null) { editor.putString(KEY_CHEF_THREAD_ID, thread.getId()); }
            editor.apply();
        } else if (Objects.equals(roleAssistant, ChatGPTRole.TRANSLATOR)) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_TRANSLATOR_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (assistant != null) { editor.putString(KEY_TRANSLATOR_ID, assistant.getId()); }
            if (thread != null) { editor.putString(KEY_TRANSLATOR_THREAD_ID, thread.getId()); }
            editor.apply();
        }
    }

    /**
     * Зберігає поточний ліміт запитів за день у SharedPreferences.
     */
    private void saveDailyLimit() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_DAILY_LIMIT_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_DAILY_LIMIT, dailyRequestCount).apply();
    }

    /**
     * Перевіряє та скидає ліміт запитів за день, якщо минула доба.
     */
    private void checkResetDailyLimit() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_DAILY_LIMIT_NAME, Context.MODE_PRIVATE);
        long currentTime = System.currentTimeMillis();
        dailyRequestCount = prefs.getInt(KEY_DAILY_LIMIT, 0);
        long lastResetTime = prefs.getLong(KEY_LAST_RESET_TIME, 0);

        if (currentTime - lastResetTime >= 86400000) {
            prefs.edit()
                    .putInt(KEY_DAILY_LIMIT, 0)
                    .putLong(KEY_LAST_RESET_TIME, currentTime)
                    .apply();
        }
    }

    /**
     * Перевіряє, чи не перевищено ліміт запитів за день.
     *
     * @return true, якщо ліміт не перевищено, інакше false.
     */
    public boolean checkDailyRequestLimit() {
        if (dailyRequestCount >= MAX_REQUESTS_PER_DAY) {
            Toast.makeText(context, context.getString(R.string.warning_daily_request_limit), Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    /**
     * Перевіряє, чи минув мінімальний інтервал між запитами.
     *
     * @return true, якщо інтервал дотримано, інакше false.
     */
    public boolean checkLastTimeRequest() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL_MS) {
            Toast.makeText(context, context.getString(R.string.warning_request_time), Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    /**
     * Зчитує текстовий файл з папки assets.
     *
     * @param fileName Назва файлу.
     * @return Зчитаний текст або порожній рядок у разі помилки.
     */
    private String readTextFileFromAssets(String fileName) {
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
