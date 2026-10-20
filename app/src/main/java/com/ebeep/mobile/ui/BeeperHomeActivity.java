package com.ebeep.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.net.BeeperService;
import com.ebeep.mobile.net.Protocol;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Screen 6: one beeper's home. Shows the last message,
 * and leads to the chat, the games and the history.
 */
public class BeeperHomeActivity extends BaseActivity {

    private TextView txtMessage;
    private TextView txtMessageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beeper_home);
        SystemBars.apply(this, findViewById(R.id.root));

        Beeper beeper = loadCurrentBeeper();
        if (beeper == null) {
            finish();
            return;
        }
        setUpToolbar(beeper.getNickname(), true);
        setUpViews();

        // Keeps the connection alive, also when the app is in the background
        BeeperService.start(this, beeper.getId());
    }

    private void setUpViews() {
        txtMessage = findViewById(R.id.txtMessage);
        txtMessageInfo = findViewById(R.id.txtMessageInfo);

        findViewById(R.id.tileChat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
        findViewById(R.id.tileGames).setOnClickListener(v ->
                startActivity(new Intent(this, GamesActivity.class)));
        findViewById(R.id.tileHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLastMessage();
    }

    @Override
    public void onBeeperMessage(String subTopic, String text) {
        super.onBeeperMessage(subTopic, text);
        if (subTopic.equals(Protocol.INBOX)) {
            showLastMessage();
        }
    }

    private void showLastMessage() {
        // Loaded again, because the service may have saved a new message
        Beeper beeper = loadCurrentBeeper();
        if (beeper == null || beeper.getLastMessage() == null) {
            txtMessage.setText(R.string.home_no_messages);
            txtMessageInfo.setText("");
            return;
        }

        String sender = beeper.isLastMessageMine() ? getString(R.string.home_me) : beeper.getNickname();
        String time = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                .format(new Date(beeper.getLastMessageTime()));

        txtMessage.setText(beeper.getLastMessage());
        txtMessageInfo.setText(getString(R.string.home_message_info, sender, time));
    }
}
