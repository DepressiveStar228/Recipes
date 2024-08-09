package com.example.recipes.Controller;

import android.content.Context;
import android.widget.Toast;

import okhttp3.*;

import com.example.recipes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ChatGPTClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType mediaType = MediaType.parse("application/json");;
    private Context context;
    private static final long MIN_REQUEST_INTERVAL_MS = 5000;
    private static final int MAX_REQUESTS_PER_DAY = 100;
    private static final int MAX_MESSAGE_LENGTH = 250;
    private int dailyRequestCount = 0;
    private long lastRequestTime = 0;

    public ChatGPTClient (Context context) {
        this.context = context;
    }

    public String getResponse(String prompt) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        json.put("model", "gpt-3.5-turbo");
        json.put("temperature", 1.0);
        json.put("top_p", 1.0);
        json.put("frequency_penalty", 0.0);
        json.put("presence_penalty", 0.5);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", "You are a professional chef. They will ask you for advice and recipes and you will answer in detail. Answer in the language in which they write to you. Return all recipes as a JSON string: {\"dishes\":[{\"id\":1,\"name\":\"Here is the name of the dish\",\"recipe\":\"Here write in detail how to prepare this dish. Describe all the steps needed to prepare the dish.\"}],\"ingredients\":[{\" amount\":\"Here is the amount of the ingredient without of measurement type\",\"id\":1,\"id_dish\":1,\"name\":\"Here is the name of the ingredient\",\"type\":\"Here is the type of measurement\"},{another ingredient in the same format. Add as many ingredients as in the recipe}]}"));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        json.put("messages", messages);

        RequestBody body = RequestBody.create(String.valueOf(json), mediaType);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                //.addHeader("Authorization", "Bearer " + context.getString(R.string.api_key))
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


    public boolean checkLimit(String prompt){
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
}
