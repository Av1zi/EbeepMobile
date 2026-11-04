package com.ebeep.mobile.ai;

import android.app.Activity;
import android.util.Log;

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
 * Asks an AI model for a short reply to the last message from the beeper.
 * The answer is cleaned so the beeper can show it: capital letters, at most 30 characters.
 *
 * The call goes to an OpenAI-compatible gateway: one POST to /v1/chat/completions
 * with a JSON body, and the answer is read from choices[0].message.content.
 */
public class ReplySuggester {

    /** Gets the answer on the main thread. */
    public interface Callback {
        void onSuggestion(String reply);

        void onFailed();
    }

    private static final String TAG = "ReplySuggester";

    // Cheapest and fastest model on the gateway (tested: about 0.7 s for a reply)
    private static final String MODEL = "qwen/qwen3.8-27b";

    private static final String URL_CHAT =
            "https://ai.kavyansh.org/v1/chat/completions";

    // 30 characters are only a few tokens; a cap keeps a bad answer short and cheap
    private static final int MAX_TOKENS = 24;

    private static final String INSTRUCTIONS =
            "You are texting a friend. Write MY reply to their last message, in first person, "
                    + "as if I am the sender. Answer with the reply only, in English, "
                    + "at most 30 characters, using only capital letters A-Z, "
                    + "spaces and the signs . ! ? "
                    + "You may write <3 for a heart. No emoji and no quotes.";

    public static void suggest(Activity activity, String lastMessage, Callback callback) {
        // Network calls are not allowed on the main thread
        new Thread(() -> {
            String reply = askModel(lastMessage);
            activity.runOnUiThread(() -> {
                if (reply == null || reply.isEmpty()) {
                    callback.onFailed();
                } else {
                    callback.onSuggestion(reply);
                }
            });
        }).start();
    }

    private static String askModel(String lastMessage) {
        HttpURLConnection connection = null;
        try {
            JSONObject body = new JSONObject()
                    .put("model", MODEL)
                    .put("max_tokens", MAX_TOKENS)
                    .put("messages", new JSONArray()
                            // The first message is the system prompt, the second one is the user
                            .put(message("system", INSTRUCTIONS))
                            .put(message("user", "The message I got: " + lastMessage)));

            connection = (HttpURLConnection) new URL(URL_CHAT).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("Content-Type", "application/json");
            // The key goes in a header, not the URL, so it doesn't end up in logs
            connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.GOOGLE_API_KEY);
            connection.setDoOutput(true);

            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "AI gateway returned HTTP " + responseCode);
                return null;
            }

            JSONObject response = new JSONObject(readAll(connection.getInputStream()));
            String text = response.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").optString("content");

            // Never trust outside text: the same filter as the keyboard
            return Protocol.cleanForBeeper(text);
        } catch (Exception e) {
            // No internet, a bad key, a used-up quota or a server error: the screen just says it failed
            Log.w(TAG, "AI suggestion request failed", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /** Builds {"role": "...", "content": "..."}, one message of the chat. */
    private static JSONObject message(String role, String content) throws Exception {
        return new JSONObject().put("role", role).put("content", content);
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
