package com.ebeep.mobile.net;

import java.util.Locale;

/**
 * Topic names and message words the beeper firmware understands
 * (taken from the Ebeep firmware: Callbacks.h, TicTacToeState.h, Connect4State.h).
 */
public class Protocol {

    // Topics are "<device id>" + one of these, for example "Beeper_1/inbox"
    public static final String INBOX = "/inbox";
    public static final String GAMES = "/games";
    public static final String GAME_TOPIC_TTT = "/games/TTT";
    public static final String GAME_TOPIC_C4 = "/games/C4";

    // Game codes
    public static final String TTT = "TTT";
    public static final String C4 = "C4";

    // The words are built as game code + word, for example "TTT_START"
    public static final String START = "_START";
    public static final String START_ACK = "_START_ACK";
    public static final String LEFT = "_LEFT";
    public static final String REMATCH = "_REMATCH";
    public static final String REMATCH_ACK = "_REMATCH_ACK";

    // The beeper's keyboard only has these characters
    public static final String ALLOWED_CHARS = " ABCDEFGHIJKLMNOPQRSTUVWXYZ.!?<3";
    public static final int MAX_MESSAGE_LENGTH = 30;

    /** The game's own topic, where the moves are sent. */
    public static String gameTopic(String game) {
        return game.equals(TTT) ? GAME_TOPIC_TTT : GAME_TOPIC_C4;
    }

    /** Keeps only characters the beeper can show, in capitals, cut to 30. */
    public static String cleanForBeeper(String text) {
        StringBuilder clean = new StringBuilder();
        String upper = text.toUpperCase(Locale.ROOT);
        for (int i = 0; i < upper.length() && clean.length() < MAX_MESSAGE_LENGTH; i++) {
            char c = upper.charAt(i);
            if (ALLOWED_CHARS.indexOf(c) >= 0) {
                clean.append(c);
            }
        }
        return clean.toString().trim();
    }

    /**
     * When both sides send an invitation at the same moment, the firmware lets the
     * side with the smaller id start the game. The phone does the same.
     */
    public static boolean phoneStartsOnTie(String phoneId, String beeperId) {
        return phoneId.compareTo(beeperId) < 0;
    }
}
