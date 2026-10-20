package com.ebeep.mobile.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import com.ebeep.mobile.OpenScreens;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.data.DatabaseHelper;

/**
 * Keeps the connection to the open beeper alive while the app is in the background,
 * saves incoming messages and shows notifications when no screen is open.
 */
public class BeeperService extends Service implements BeeperConnection.Listener {

    private static final String EXTRA_BEEPER_ROW_ID = "beeper_row_id";
    private static final int FOREGROUND_ID = 1;

    /** Starts (or switches) the service to this beeper. */
    public static void start(Context context, long beeperRowId) {
        Intent intent = new Intent(context, BeeperService.class);
        intent.putExtra(EXTRA_BEEPER_ROW_ID, beeperRowId);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, BeeperService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long beeperRowId = intent == null ? -1 : intent.getLongExtra(EXTRA_BEEPER_ROW_ID, -1);

        DatabaseHelper database = new DatabaseHelper(this);
        Beeper beeper = database.getBeeper(beeperRowId);
        database.close();

        if (beeper == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // The "remote messaging" service type only exists from Android 14
        int type = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            type = ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING;
        }
        ServiceCompat.startForeground(this, FOREGROUND_ID,
                Notifications.connectedNotification(this, beeper), type);

        BeeperConnection.get().setServiceListener(this);
        BeeperConnection.get().connect(beeper);
        return START_NOT_STICKY;
    }

    @Override
    public void onBeeperMessage(String subTopic, String text) {
        Beeper beeper = BeeperConnection.get().getBeeper();
        if (beeper == null) {
            return;
        }

        if (subTopic.equals(Protocol.INBOX) && !text.isEmpty()) {
            // The message is saved here, so it isn't lost even when no screen is open
            DatabaseHelper database = new DatabaseHelper(this);
            database.saveLastMessage(beeper.getId(), text, false);
            database.close();

            if (!OpenScreens.isAnyOpen()) {
                Notifications.showMessage(this, beeper, text);
            }
        } else if (subTopic.equals(Protocol.GAMES) && text.endsWith(Protocol.START)) {
            // The invitation dialog only works while a screen is open, so keep it for later
            if (!OpenScreens.isAnyOpen()) {
                String game = text.startsWith(Protocol.TTT) ? Protocol.TTT : Protocol.C4;
                BeeperConnection.get().setPendingInvitation(game);
                Notifications.showInvitation(this, beeper, game);
            }
        }
    }

    @Override
    public void onConnectionChanged(boolean connected) {
        // The screens show the connection state; nothing to do here
    }

    @Override
    public void onDestroy() {
        BeeperConnection.get().setServiceListener(null);
        BeeperConnection.get().disconnect();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  // Started service, not bound
    }
}
