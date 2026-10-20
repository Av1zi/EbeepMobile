package com.ebeep.mobile.data;

/**
 * A beeper that a user connected (one row of the beepers table).
 * Holds everything needed to talk to it: its MQTT ID, the ID the phone uses, and the broker password.
 */
public class Beeper {

    private final long id;
    private final long userId;
    private final String nickname;
    private final String beeperId;
    private final String phoneId;
    private final String mqttPassword;

    // The last message in the chat (null when there were no messages yet)
    private final String lastMessage;
    private final boolean lastMessageMine;
    private final long lastMessageTime;

    public Beeper(long id, long userId, String nickname, String beeperId, String phoneId,
                  String mqttPassword, String lastMessage, boolean lastMessageMine, long lastMessageTime) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.beeperId = beeperId;
        this.phoneId = phoneId;
        this.mqttPassword = mqttPassword;
        this.lastMessage = lastMessage;
        this.lastMessageMine = lastMessageMine;
        this.lastMessageTime = lastMessageTime;
    }

    /** A new beeper that isn't in the database yet (no id and no messages). */
    public Beeper(long userId, String nickname, String beeperId, String phoneId, String mqttPassword) {
        this(-1, userId, nickname, beeperId, phoneId, mqttPassword, null, false, 0);
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getBeeperId() {
        return beeperId;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean isLastMessageMine() {
        return lastMessageMine;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }
}
