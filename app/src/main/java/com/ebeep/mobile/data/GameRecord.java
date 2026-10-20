package com.ebeep.mobile.data;

/** The result of one finished game against a beeper (one row of the game_records table). */
public class GameRecord {

    public static final String WIN = "WIN";
    public static final String LOSS = "LOSS";
    public static final String DRAW = "DRAW";

    private final long id;
    private final String game;
    private final String result;
    private final long playedAt;

    public GameRecord(long id, String game, String result, long playedAt) {
        this.id = id;
        this.game = game;
        this.result = result;
        this.playedAt = playedAt;
    }

    public long getId() {
        return id;
    }

    /** "TTT" or "C4", the same codes as in Protocol */
    public String getGame() {
        return game;
    }

    /** WIN, LOSS or DRAW */
    public String getResult() {
        return result;
    }

    public long getPlayedAt() {
        return playedAt;
    }
}
