package com.ebeep.mobile.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

/**
 * Listens to the system broadcast that fires when the phone's network changes
 * (Wi-Fi off, airplane mode, mobile data back, ...).
 */
public class NetworkReceiver extends BroadcastReceiver {

    /** Whoever registered the receiver gets told about the change. */
    public interface Listener {
        void onNetworkChanged(boolean hasNetwork);
    }

    private final Listener listener;

    public NetworkReceiver(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.onNetworkChanged(hasNetwork(context));
    }

    public static boolean hasNetwork(Context context) {
        ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
        return manager.getActiveNetwork() != null;
    }
}
