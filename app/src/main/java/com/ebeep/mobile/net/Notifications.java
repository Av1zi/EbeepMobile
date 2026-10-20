package com.ebeep.mobile.net;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ebeep.mobile.R;
import com.ebeep.mobile.data.AppSettings;
import com.ebeep.mobile.data.Beeper;
import com.ebeep.mobile.ui.BeeperHomeActivity;

/** Creates the notification channels and shows the app's notifications. */
public class Notifications {

    private static final String CHANNEL_MESSAGES = "messages";
    private static final String CHANNEL_MESSAGES_SILENT = "messages_silent";
    private static final String CHANNEL_CONNECTION = "connection";

    private static final int ID_MESSAGE = 2;
    private static final int ID_INVITATION = 3;

    /** Called once when the app starts. Creating a channel that exists does nothing. */
    public static void createChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);

        manager.createNotificationChannel(new NotificationChannel(CHANNEL_MESSAGES,
                context.getString(R.string.channel_messages), NotificationManager.IMPORTANCE_HIGH));

        NotificationChannel silent = new NotificationChannel(CHANNEL_MESSAGES_SILENT,
                context.getString(R.string.channel_messages_silent), NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(silent);

        manager.createNotificationChannel(new NotificationChannel(CHANNEL_CONNECTION,
                context.getString(R.string.channel_connection), NotificationManager.IMPORTANCE_MIN));
    }

    /** The permanent notification of the foreground service. */
    public static Notification connectedNotification(Context context, Beeper beeper) {
        return new NotificationCompat.Builder(context, CHANNEL_CONNECTION)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_connected, beeper.getNickname()))
                .setContentText(context.getString(R.string.notification_connected_text))
                .setContentIntent(openBeeperHome(context))
                .setOngoing(true)
                .build();
    }

    public static void showMessage(Context context, Beeper beeper, String text) {
        show(context, ID_MESSAGE,
                context.getString(R.string.notification_message_title, beeper.getNickname()), text);
    }

    public static void showInvitation(Context context, Beeper beeper, String game) {
        String gameName = context.getString(game.equals(Protocol.TTT)
                ? R.string.game_tic_tac_toe : R.string.game_connect_four);
        show(context, ID_INVITATION,
                context.getString(R.string.notification_invite_title, beeper.getNickname()),
                context.getString(R.string.notification_invite_text, gameName));
    }

    // Lint can't see that hasPermission() checks POST_NOTIFICATIONS first
    @SuppressLint("MissingPermission")
    private static void show(Context context, int id, String title, String text) {
        if (!AppSettings.isNotificationsOn(context) || !hasPermission(context)) {
            return;
        }
        String channel = AppSettings.isSoundOn(context) ? CHANNEL_MESSAGES : CHANNEL_MESSAGES_SILENT;

        Notification notification = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(openBeeperHome(context))
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat.from(context).notify(id, notification);
    }

    // From Android 13 the user has to allow notifications
    private static boolean hasPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Tapping a notification opens the home screen of the open beeper
    private static PendingIntent openBeeperHome(Context context) {
        Intent intent = new Intent(context, BeeperHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
