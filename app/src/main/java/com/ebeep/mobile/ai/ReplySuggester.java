package com.ebeep.mobile.ai;

import android.app.Activity;

import com.ebeep.mobile.BuildConfig;
import com.ebeep.mobile.net.Protocol;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Asks Gemini (Google's AI model) for a short reply to the last message from the beeper.
 * The answer is cleaned so the beeper can show it: capital letters, at most 30 characters.
 */
public class ReplySuggester {

    /** Gets the answer on the main thread. */
    public interface Callback {
        void onSuggestion(String reply);

        void onFailed();
    }

    // Flash-Lite is fast and has a free tier
    private static final String MODEL = "gemini-3.5-flash-lite";

    private static final String URL_START =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String INSTRUCTIONS =
            "You suggest a short, friendly reply to a text message from a friend. "
                    + "Answer with the reply only, in English, at most 30 characters, "
                    + "using only capital letters A-Z, spaces and the signs . ! ? "
                    + "You may write <3 for a heart. No emoji and no quotes.";

    public static void suggest(Activity activity, String lastMessage, Callback callback) {
        // Network calls are not allowed on the main thread
        new Thread(() -> {
            String reply = askGemini(lastMessage);
            activity.runOnUiThread(() -> {
                if (reply == null || reply.isEmpty()) {
                    callback.onFailed();
                } else {
                    callback.onSuggestion(reply);
                }
            });
        }).start();
    }

    private static String askGemini(String lastMessage) {
        HttpURLConnection connection = null;
        try {
            JSONObject body = new JSONObject()
                    .put("systemInstruction", textContent(INSTRUCTIONS))
                    .put("contents", new JSONArray()
                            .put(textContent("The message I got: " + lastMessage).put("role", "user")));

            connection = (HttpURLConnection) new URL(URL_START + MODEL + ":generateContent").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("Content-Type", "application/json");
            // The key goes in a header, not the URL, so it doesn't end up in logs
            connection.setRequestProperty("x-goog-api-key", BuildConfig.GOOGLE_API_KEY);
            connection.setDoOutput(true);

            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            JSONObject response = new JSONObject(readAll(connection.getInputStream()));
            JSONArray parts = response.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts");

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                text.append(parts.getJSONObject(i).optString("text"));
            }
            // Never trust outside text: the same filter as the keyboard
            return Protocol.cleanForBeeper(text.toString());
        } catch (Exception e) {
            // No internet, a bad key, a used-up free quota or a server error: the screen just says it failed
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /** Builds {"parts": [{"text": ...}]}, the shape Gemini uses for a piece of text. */
    private static JSONObject textContent(String text) throws Exception {
        return new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", text)));
    }

    private static String readAll(InputStream in) throws Exception {
        try (InputStream stream = in) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                bytes.write(buffer, 0, read);
            }
            return bytes.toString("UTF-8");
        }
    }
}
