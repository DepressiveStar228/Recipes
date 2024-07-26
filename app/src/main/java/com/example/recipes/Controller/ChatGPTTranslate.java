package com.example.recipes.Controller;

import android.content.Context;

import com.example.recipes.Activity.ReadDataDishActivity;
import com.example.recipes.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTTranslate {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType mediaType = MediaType.parse("application/json");
    private Context context;

    public ChatGPTTranslate(Context context) {
        this.context = context;
    }

    public String translate(String prompt) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(context);
        String lang = perferencesController.language;
        String[] langArray = context.getResources().getStringArray(R.array.language_options);
        String[] langValArray = context.getResources().getStringArray(R.array.language_values);
        int index = Arrays.asList(langValArray).indexOf(lang);

        JSONObject json = new JSONObject();
        json.put("model", "gpt-3.5-turbo");
        json.put("max_tokens", 250);
        json.put("temperature", 0.4);
        json.put("top_p", 0.4);
        json.put("frequency_penalty", 0.0);
        json.put("presence_penalty", 0.7);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", "You are a professional translator. They will give you lines to translate, and you translate as accurately as possible, while maintaining the context."));
        messages.put(new JSONObject().put("role", "user").put("content", "Translate the following text to " + langArray[index] + ": (" + prompt + "). Do not write anything other than a translation of the text and without parentheses"));
        json.put("messages", messages);

        RequestBody body = RequestBody.create(String.valueOf(json), mediaType);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + context.getString(R.string.api_key))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new IOException("Unexpected code " + response + ": " + responseBody);
            }
            JSONObject responseJson = new JSONObject(response.body().string());
            return responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }
    }
}
