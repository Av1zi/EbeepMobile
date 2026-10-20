package com.ebeep.mobile.net;

import android.os.Handler;
import android.os.Looper;

import com.ebeep.mobile.Secrets;
import com.ebeep.mobile.data.Beeper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

/**
 * The single MQTT connection between the phone and the open beeper.
 * The service and the open screen listen to it; anyone can send through it.
 * The phone plays the role of the beeper's partner: it listens on "phoneId/#"
 * and sends to "beeperId/...".
 */
public class BeeperConnection implements MqttCallbackExtended {

    /** Gets messages and connection changes, always on the main thread. */
    public interface Listener {
        void onBeeperMessage(String subTopic, String text);

        void onConnectionChanged(boolean connected);
    }

    private static BeeperConnection instance;

    private MqttClient client;
    private Beeper beeper;
    private boolean connected;

    // The service listens all the time; a screen listens only while it's open
    private Listener serviceListener;
    private Listener screenListener;

    // An invitation that arrived while no screen was open ("TTT" or "C4"), or null
    private String pendingInvitation;

    // Paho calls us on its own thread; this Handler moves the work to the main thread
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static BeeperConnection get() {
        if (instance == null) {
            instance = new BeeperConnection();
        }
        return instance;
    }

    private BeeperConnection() {
    }

    public void connect(Beeper beeperToOpen) {
        // Already connected (or connecting) to this beeper: nothing to do
        if (client != null && beeper != null && beeper.getId() == beeperToOpen.getId()) {
            return;
        }
        disconnect();
        beeper = beeperToOpen;

        MqttClient newClient;
        try {
            // The client id must differ from the real devices, or the broker disconnects one of them
            newClient = new MqttClient(Secrets.BROKER_URL,
                    beeper.getPhoneId() + "_phone", new MemoryPersistence());
        } catch (MqttException e) {
            notifyConnectionChanged(false);
            return;
        }
        client = newClient;
        client.setCallback(this);

        MqttConnectOptions options = createOptions(beeper.getPhoneId(), beeper.getMqttPassword());
        new Thread(() -> {
            try {
                newClient.connect(options);
                // The subscription is made in connectComplete, so it also happens after reconnects
            } catch (MqttException e) {
                notifyConnectionChanged(false);
            }
        }).start();
    }

    public void disconnect() {
        if (client == null) {
            return;
        }
        MqttClient oldClient = client;
        client = null;
        beeper = null;
        connected = false;
        pendingInvitation = null;
        new Thread(() -> {
            try {
                if (oldClient.isConnected()) {
                    oldClient.disconnect();
                }
                oldClient.close();
            } catch (MqttException e) {
                // It's being thrown away anyway
            }
        }).start();
    }

    public boolean isConnected() {
        return client != null && connected;
    }

    public Beeper getBeeper() {
        return beeper;
    }

    /** Sends to the beeper, for example sendToBeeper(Protocol.INBOX, "HI", true). */
    public void sendToBeeper(String subTopic, String text, boolean retained) {
        if (!isConnected()) {
            return;
        }
        MqttClient sendingClient = client;
        String topic = beeper.getBeeperId() + subTopic;
        new Thread(() -> {
            try {
                sendingClient.publish(topic, text.getBytes(StandardCharsets.UTF_8), 1, retained);
            } catch (MqttException e) {
                notifyConnectionChanged(false);
            }
        }).start();
    }

    /** Clears the retained message in our own inbox, like the firmware does after reading. */
    public void clearMyInbox() {
        if (!isConnected()) {
            return;
        }
        MqttClient sendingClient = client;
        String topic = beeper.getPhoneId() + Protocol.INBOX;
        new Thread(() -> {
            try {
                sendingClient.publish(topic, new byte[0], 1, true);
            } catch (MqttException e) {
                notifyConnectionChanged(false);
            }
        }).start();
    }

    /**
     * Checks that the broker accepts these login details, without keeping the connection.
     * Used by the add beeper screen. Must be called from a background thread.
     */
    public static boolean testLogin(String phoneId, String password) {
        try {
            MqttClient testClient = new MqttClient(Secrets.BROKER_URL,
                    phoneId + "_test", new MemoryPersistence());
            MqttConnectOptions options = createOptions(phoneId, password);
            options.setAutomaticReconnect(false);
            testClient.connect(options);
            testClient.disconnect();
            testClient.close();
            return true;
        } catch (MqttException e) {
            return false;
        }
    }

    private static MqttConnectOptions createOptions(String username, String password) {
        MqttConnectOptions options = new MqttConnectOptions();
        // The firmware uses the device id as the MQTT username
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        return options;
    }

    // ---- Listeners ----

    public void setServiceListener(Listener listener) {
        serviceListener = listener;
    }

    public void setScreenListener(Listener listener) {
        screenListener = listener;
    }

    public void setPendingInvitation(String game) {
        pendingInvitation = game;
    }

    /** Returns the waiting invitation (or null) and forgets it. */
    public String takePendingInvitation() {
        String game = pendingInvitation;
        pendingInvitation = null;
        return game;
    }

    // ---- MqttCallbackExtended (these run on Paho's thread) ----

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        MqttClient connectedClient = client;
        Beeper connectedBeeper = beeper;
        if (connectedClient == null || connectedBeeper == null) {
            return;
        }
        try {
            connectedClient.subscribe(connectedBeeper.getPhoneId() + "/#", 1);
        } catch (MqttException e) {
            notifyConnectionChanged(false);
            return;
        }
        notifyConnectionChanged(true);
    }

    @Override
    public void connectionLost(Throwable cause) {
        notifyConnectionChanged(false);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Beeper currentBeeper = beeper;
        if (currentBeeper == null) {
            return;
        }
        String text = new String(message.getPayload(), StandardCharsets.UTF_8);
        // "Beeper_2/games/TTT" -> "/games/TTT"
        String subTopic = topic.substring(currentBeeper.getPhoneId().length());

        mainHandler.post(() -> {
            if (serviceListener != null) {
                serviceListener.onBeeperMessage(subTopic, text);
            }
            if (screenListener != null) {
                screenListener.onBeeperMessage(subTopic, text);
            }
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not needed
    }

    private void notifyConnectionChanged(boolean isConnected) {
        mainHandler.post(() -> {
            connected = isConnected;
            if (serviceListener != null) {
                serviceListener.onConnectionChanged(isConnected);
            }
            if (screenListener != null) {
                screenListener.onConnectionChanged(isConnected);
            }
        });
    }
}
