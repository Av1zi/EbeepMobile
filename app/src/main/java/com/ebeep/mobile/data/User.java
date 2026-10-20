package com.ebeep.mobile.data;

/** A user of the app (one row of the users table). */
public class User {

    private final long id;
    private final String username;
    private final String passwordHash;

    public User(long id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
