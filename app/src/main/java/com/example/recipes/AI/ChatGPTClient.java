package com.example.recipes.AI;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.azure.ai.openai.assistants.AssistantsClient;
import com.azure.ai.openai.assistants.AssistantsClientBuilder;
import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
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
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Item.DataBox;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class ChatGPTClient {
    private static final String PREF_CHEF_NAME = "assistant_chef_prefs";
    private static final String KEY_CHEF_ID = "chef_id";
    private static final String KEY_CHEF_THREAD_ID = "chef_thread_id";

    private static final String PREF_TRANSLATOR_NAME = "assistant_translator_prefs";
    private static final String KEY_TRANSLATOR_ID = "translator_id";
    private static final String KEY_TRANSLATOR_THREAD_ID = "translator_thread_id";

    private static final long MIN_REQUEST_INTERVAL_MS = 5000;
    private static final int MAX_REQUESTS_PER_DAY = 100;
    private static final int MAX_MESSAGE_LENGTH = 250;
    private int dailyRequestCount = 0;
    private long lastRequestTime = 0;

    private final Context context;
    private final String roleAssistant;
    private AssistantsClient client;
    private Assistant assistant;
    private AssistantThread thread;


    public ChatGPTClient (Context context, String roleAssistant) {
        this.context = context;
        this.roleAssistant = roleAssistant;

        RxJavaPlugins.setErrorHandler(throwable -> {
            if (throwable instanceof UndeliverableException) {
                Throwable cause = throwable.getCause();
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

    public Single<Boolean> initialization() {
        if (Objects.equals(roleAssistant, ChatGPT.CHEF)) return initializationChef();
        else if (Objects.equals(roleAssistant, ChatGPT.TRANSLATOR)) return initializationTranslator();
        else return Single.just(false);
    }

    private Single<Boolean> initializationChef() {
        return Single.create(emitter -> {
            SharedPreferences prefs = context.getSharedPreferences(PREF_CHEF_NAME, Context.MODE_PRIVATE);
            String assistantId = prefs.getString(KEY_CHEF_ID, null);
            String threadId = prefs.getString(KEY_CHEF_THREAD_ID, null);

            client = createClient();

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

            if (client == null) { emitter.onError(new Throwable("Client is null")); }
            if (assistant == null) { emitter.onError(new Throwable("Assistant is null")); }
            if (thread == null) { emitter.onError(new Throwable("Thread is null")); }

            emitter.onSuccess(true);
        });
    }

    private Single<Boolean> initializationTranslator() {
        return Single.create(emitter -> {
            SharedPreferences prefs = context.getSharedPreferences(PREF_TRANSLATOR_NAME, Context.MODE_PRIVATE);
            String assistantId = prefs.getString(KEY_TRANSLATOR_ID, null);
            String threadId = prefs.getString(KEY_TRANSLATOR_THREAD_ID, null);

            client = createClient();

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

            if (client == null) { emitter.onError(new Throwable("Client is null")); }
            if (assistant == null) { emitter.onError(new Throwable("Assistant is null")); }
            if (thread == null) { emitter.onError(new Throwable("Thread is null")); }

            emitter.onSuccess(true);
        });
    }

    private AssistantsClient createClient() {
        return new AssistantsClientBuilder()
                .credential(new KeyCredential(API_Keys.GPT_KEY))
                .buildClient();
    }

    private Assistant createAssistant() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-3.5-turbo");

        if (Objects.equals(roleAssistant, ChatGPT.CHEF)) assistantCreationOptions.setName("Chef").setInstructions(readTextFileFromAssets(context, "asset_chef"));
        else if (Objects.equals(roleAssistant, ChatGPT.TRANSLATOR)) assistantCreationOptions.setName("Translator").setInstructions(readTextFileFromAssets(context, "asset_translator"));
        else assistantCreationOptions = null;

        return client.createAssistant(assistantCreationOptions);
    }

    private AssistantThread createThread() {
        return client.createThread(new AssistantThreadCreationOptions());
    }

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

    public Single<String> sendMessage(String message) {
        //return Single.just(message);
        return Single.create(emitter -> {
            if (client != null && assistant != null && thread != null) {
                try {
                    client.createMessage(thread.getId(), new ThreadMessageOptions(MessageRole.USER, message));
                    ThreadRun run = client.createRun(thread.getId(), new CreateRunOptions(assistant.getId()));

                    do {
                        Thread.sleep(500);
                        run = client.getRun(run.getThreadId(), run.getId());
                    } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);

                    PageableList<ThreadMessage> messages = client.listMessages(run.getThreadId());
                    List<ThreadMessage> data = messages.getData();

                    if (!data.isEmpty()) {
                        ThreadMessage lastMessage = data.get(0);

                        for (MessageContent messageContent : lastMessage.getContent()) {
                            if (messageContent instanceof MessageTextContent) {
                                MessageTextContent messageTextContent = (MessageTextContent) messageContent;
                                String box = messageTextContent.getText().getValue();
                                if (!box.isEmpty()) {
                                    emitter.onSuccess(box);
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

    public String getFormatStringForGPT(Dish dish) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{name=[").append(dish.getName()).append("]},");
        stringBuilder.append("{recipes=<");
        for (String recipe : dish.getRecipeText()) {
            stringBuilder.append("[").append(recipe).append("]");

            if (dish.getRecipeText().size()-1 != dish.getRecipeText().indexOf(recipe)) stringBuilder.append(",");
        }
        stringBuilder.append(">}");
        stringBuilder.append("{portion=[").append(dish.getPortion()).append("]},");
        stringBuilder.append("{ingredients=<");
        for (Ingredient ing : dish.getIngredients()) {
            stringBuilder.append("[").append(ing.getName()).append("|");
            stringBuilder.append(ing.getAmount()).append("|");
            stringBuilder.append(ing.getType()).append("]");

            if (dish.getIngredients().size()-1 != dish.getIngredients().indexOf(ing)) stringBuilder.append(",");
        }
        stringBuilder.append(">}");

        return stringBuilder.toString();
    }

    public String getTextFromParsedAnswer(Dish dish) {
        if (dish != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(dish.getName() + "\n").append(context.getText(R.string.portionship) + ": " + dish.getPortion() + "\n\n");

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
                    builder.append("  - " + ing.getName() + "  " + ing.getAmount() + " " + ing.getType() + "\n");
                }
            }

            return builder.toString();
        } else return "";
    }

    public Dish parsedAnswerGPT(String answer) {
        Map<String, String> recipeMap = new HashMap<>();
        List<Map<String, String>> ingredientsList = new ArrayList<>();
        ArrayList<DishRecipe> recipes = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\{(\\w+)=\\[(.*?)\\]\\}|\\{(\\w+)=<(.*?)>\\}");
        Matcher matcher = pattern.matcher(answer.contains("Data:") ? answer.substring(16) : answer);

        while (matcher.find()) {
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
            String value = matcher.group(2) != null ? matcher.group(2) : matcher.group(4);

            if ("ingredients".equals(key)) {
                Pattern ingredientPattern = Pattern.compile("\\[(.*?)\\|(.*?)(?:\\|(.*?))?\\]");
                Matcher ingredientMatcher = ingredientPattern.matcher(value);

                while (ingredientMatcher.find()) {
                    Map<String, String> ingredient = new HashMap<>();
                    ingredient.put("name", ingredientMatcher.group(1));

                    if (ingredientMatcher.group(3) != null) {
                        ingredient.put("quantity", ingredientMatcher.group(2));
                        ingredient.put("unit", ingredientMatcher.group(3));
                    }
                    else {
                        ingredient.put("quantity", "");
                        ingredient.put("unit", ingredientMatcher.group(2));
                    }

                    ingredientsList.add(ingredient);
                }
            } else if ("recipe".equals(key) || "recipes".equals(key)) {
                Pattern recipesPattern = Pattern.compile("\\[(.*?)\\]");
                Matcher recipesMatcher = recipesPattern.matcher(value);
                int i = 0;

                while (recipesMatcher.find()) {
                    recipes.add(new DishRecipe(recipesMatcher.group(1), i, DishRecipeType.TEXT));
                    i++;
                }
            } else {
                recipeMap.put(key, value);
            }
        }

        int portion = 0;

        try {
            portion = Integer.parseInt(recipeMap.get("portion"));
        } catch (Exception e) {}

        Dish dish = new Dish(recipeMap.get("name"), portion);
        dish.setRecipes(recipes);
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        for (Map<String, String> ingredient : ingredientsList) {
            Ingredient ingredient_ = new Ingredient(ingredient.get("name"), ingredient.get("quantity"), ingredient.get("unit"));
            ingredients.add(ingredient_);
        }

        dish.setIngredients(ingredients);

        return dish;
    }

    private void saveAssistantState() {
        if (Objects.equals(roleAssistant, ChatGPT.CHEF)) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_CHEF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (assistant != null) { editor.putString(KEY_CHEF_ID, assistant.getId()); }
            if (thread != null) { editor.putString(KEY_CHEF_THREAD_ID, thread.getId()); }
            editor.apply();
        } else if (Objects.equals(roleAssistant, ChatGPT.TRANSLATOR)) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_TRANSLATOR_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            if (assistant != null) { editor.putString(KEY_TRANSLATOR_ID, assistant.getId()); }
            if (thread != null) { editor.putString(KEY_TRANSLATOR_THREAD_ID, thread.getId()); }
            editor.apply();
        }
    }

    private boolean checkLimit(String prompt){
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL_MS) {
            Toast.makeText(context, context.getString(R.string.warning_request_time), Toast.LENGTH_SHORT).show();
            return false;
        }

        lastRequestTime = currentTime;

        if (prompt.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.warning_empty_message), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (prompt.length() > MAX_MESSAGE_LENGTH) {
            Toast.makeText(context, context.getString(R.string.warning_max_length_message), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dailyRequestCount >= MAX_REQUESTS_PER_DAY) {
            Toast.makeText(context, context.getString(R.string.warning_daily_request_limit), Toast.LENGTH_SHORT).show();
            return false;
        }

        dailyRequestCount++;

        return true;
    }

    private String readTextFileFromAssets(Context context, String fileName) {
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
