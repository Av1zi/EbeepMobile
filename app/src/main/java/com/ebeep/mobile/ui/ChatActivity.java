package com.ebeep.mobile.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ebeep.mobile.R;
import com.ebeep.mobile.ai.ReplySuggester;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.data.DatabaseHelper;
import com.ebeep.mobile.net.BeeperConnection;
import com.ebeep.mobile.net.Protocol;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Screen 7: shows the last message and sends a new one to the beeper. */
public class ChatActivity extends BaseActivity {

    private Beeper beeper;

    private TextView txtSender;
    private TextView txtMessage;
    private TextView txtCounter;
    private EditText editMessage;
    private Button btnSend;
    private Button btnSuggest;
    private ProgressBar progressSuggest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        SystemBars.apply(this, findViewById(R.id.root));

        txtSender = findViewById(R.id.txtSender);
        txtMessage = findViewById(R.id.txtMessage);
        txtCounter = findViewById(R.id.txtCounter);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSuggest = findViewById(R.id.btnSuggest);
        progressSuggest = findViewById(R.id.progressSuggest);

        beeper = loadCurrentBeeper();
        setUpToolbar(beeper.getNickname(), true);
        setUpMessageField();

        btnSend.setOnClickListener(v -> sendMessage());
        btnSuggest.setOnClickListener(v -> askForSuggestion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLastMessage();
    }

    private void setUpMessageField() {
        // Hebrew letters, lowercase and emoji can't be shown on the beeper:
        // turn letters into capitals and drop everything else
        InputFilter beeperCharacters = (source, start, end, dest, dstart, dend) -> {
            StringBuilder allowed = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = Character.toUpperCase(source.charAt(i));
                if (Protocol.ALLOWED_CHARS.indexOf(c) >= 0) {
                    allowed.append(c);
                }
            }
            return allowed.toString();
        };

        editMessage.setFilters(new InputFilter[]{
                beeperCharacters,
                new InputFilter.LengthFilter(Protocol.MAX_MESSAGE_LENGTH)
        });

        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                txtCounter.setText(getString(R.string.chat_counter, s.length(), Protocol.MAX_MESSAGE_LENGTH));
                btnSend.setEnabled(!s.toString().trim().isEmpty());
            }
        });

        // The watcher only runs on changes, so set the starting state by hand
        txtCounter.setText(getString(R.string.chat_counter, 0, Protocol.MAX_MESSAGE_LENGTH));
        btnSend.setEnabled(false);
    }

    /** Reads the beeper row again, because the service may have saved a new message. */
    private void showLastMessage() {
        beeper = loadCurrentBeeper();

        if (beeper.getLastMessage() == null) {
            txtSender.setText("");
            txtMessage.setText(R.string.chat_empty);
            btnSuggest.setEnabled(false);
            return;
        }

        String sender = beeper.isLastMessageMine() ? getString(R.string.chat_me) : beeper.getNickname();
        String time = new SimpleDateFormat("HH:mm  dd/MM", Locale.getDefault())
                .format(new Date(beeper.getLastMessageTime()));
        txtSender.setText(sender + "  " + time);
        txtMessage.setText(beeper.getLastMessage());
        // A reply only makes sense to a message from the beeper
        btnSuggest.setEnabled(!beeper.isLastMessageMine());
    }

    private void sendMessage() {
        if (!BeeperConnection.get().isConnected()) {
            Toast.makeText(this, R.string.chat_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        String text = editMessage.getText().toString().trim();
        BeeperConnection.get().sendToBeeper(Protocol.INBOX, text, true);

        DatabaseHelper database = new DatabaseHelper(this);
        database.saveLastMessage(beeper.getId(), text, true);
        database.close();

        showLastMessage();
        editMessage.setText("");
    }

    private void askForSuggestion() {
        btnSuggest.setEnabled(false);
        progressSuggest.setVisibility(View.VISIBLE);

        ReplySuggester.suggest(this, beeper.getLastMessage(), new ReplySuggester.Callback() {
            @Override
            public void onSuggestion(String reply) {
                progressSuggest.setVisibility(View.GONE);
                btnSuggest.setEnabled(true);
                editMessage.setText(reply);
                editMessage.setSelection(editMessage.getText().length());
            }

            @Override
            public void onFailed() {
                progressSuggest.setVisibility(View.GONE);
                btnSuggest.setEnabled(true);
                Toast.makeText(ChatActivity.this, R.string.chat_suggest_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBeeperMessage(String subTopic, String text) {
        super.onBeeperMessage(subTopic, text);

        if (subTopic.equals(Protocol.INBOX) && !text.isEmpty()) {
            // The service already saved the message
            showLastMessage();
            BeeperConnection.get().clearMyInbox();
        }
    }
}
