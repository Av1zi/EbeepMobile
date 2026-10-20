package com.ebeep.mobile.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Turns a password into a SHA-256 hash, so the real password is never stored. */
public class PasswordHasher {

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Every byte becomes two hex characters
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // Every Android device has SHA-256, so this can't really happen
            throw new RuntimeException(e);
        }
    }
}
