package com.ebeep.mobile.ai;

import android.app.Activity;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.ebeep.mobile.Secrets;
import com.ebeep.mobile.net.Protocol;

/**
 * Asks Claude (Anthropic's AI model) for a short reply to the last message from the beeper.
 * The answer is cleaned so the beeper can show it: capital letters, at most 30 characters.
 */
public class ReplySuggester {

    /** Gets the answer on the main thread. */
    public interface Callback {
        void onSuggestion(String reply);

        void onFailed();
    }

    private static final String MODEL = "claude-opus-5-5";

    private static final String INSTRUCTIONS =
            "You suggest a short, friendly reply to a text message from a friend. "
                    + "Answer with the reply only, in English, at most 30 characters, "
                    + "using only capital letters A-Z, spaces and the signs . ! ? "
                    + "You may write <3 for a heart. No emoji and no quotes.";

    public static void suggest(Activity activity, String lastMessage, Callback callback) {
        // Network calls are not allowed on the main thread
        new Thread(() -> {
            String reply = askClaude(lastMessage);
            activity.runOnUiThread(() -> {
                if (reply == null || reply.isEmpty()) {
                    callback.onFailed();
                } else {
                    callback.onSuggestion(reply);
                }
            });
        }).start();
    }

    private static String askClaude(String lastMessage) {
        try {
            AnthropicClient client = AnthropicOkHttpClient.builder()
                    .apiKey(Secrets.CLAUDE_API_KEY)
                    .build();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(MODEL)
                    .maxTokens(2000L)
                    // A one-line reply doesn't need deep thinking; low effort keeps it fast
                    .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
                    .system(INSTRUCTIONS)
                    .addUserMessage("The message I got: " + lastMessage)
                    .build();

            Message response = client.messages().create(params);

            StringBuilder text = new StringBuilder();
            for (ContentBlock block : response.content()) {
                if (block.isText()) {
                    text.append(block.asText().text());
                }
            }
            // Never trust outside text: the same filter as the keyboard
            return Protocol.cleanForBeeper(text.toString());
        } catch (Exception e) {
            // No internet, a bad key or a server error: the screen just says it failed
            return null;
        }
    }
}
