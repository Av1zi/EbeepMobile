package com.ebeep.mobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.data.Session;
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
    private TextView txtBeeperId;
    private TextView txtSendsTo;

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
        txtBeeperId = findViewById(R.id.txtBeeperId);
        txtSendsTo = findViewById(R.id.txtSendsTo);

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
        showDetails();
        showLastMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_beeper_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuEdit) {
            Intent intent = new Intent(this, AddBeeperActivity.class);
            intent.putExtra(AddBeeperActivity.EXTRA_EDIT_BEEPER, Session.getCurrentBeeper(this));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The name in the top bar and the connection details. Loaded again in onResume,
     * because they may have been changed in the edit screen.
     */
    private void showDetails() {
        Beeper beeper = loadCurrentBeeper();
        if (beeper == null) {
            return;
        }
        setTitle(beeper.getNickname());
        txtBeeperId.setText(beeper.getBeeperId());
        // The beeper sends its messages to its partner, and the phone plays that partner
        txtSendsTo.setText(getString(R.string.home_this_phone, beeper.getPhoneId()));
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
