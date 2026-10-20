package com.ebeep.mobile.game;

/**
 * Connect 4 on a board of 6 rows and 7 columns. Row 0 is the top, like in the firmware.
 * The side that starts is red ('R'), the other side is yellow ('Y').
 * A move is sent as the column number, for example "3".
 */
public class ConnectFourGame extends BoardGame {

    public static final int ROWS = 6;
    public static final int COLS = 7;

    // The four line directions: down, right, down-right, down-left
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

    // The last disc that was dropped, so the board can highlight it (-1 before the first move)
    private int lastRow = -1;
    private int lastCol = -1;

    public ConnectFourGame(boolean iStart) {
        super(ROWS, COLS, iStart ? 'R' : 'Y', iStart ? 'Y' : 'R', iStart);
    }

    /** My move. Returns false if it isn't my turn or the column is full. */
    public boolean playMine(int col) {
        if (!isMyTurn()) {
            return false;
        }
        return drop(col, mySymbol);
    }

    /** The beeper's move, from a message like "3". Returns false if it isn't a legal move. */
    public boolean playOpponent(String move) {
        if (myTurn || isOver() || move.length() != 1) {
            return false;
        }
        return drop(move.charAt(0) - '0', opponentSymbol);
    }

    private boolean drop(int col, char symbol) {
        if (col < 0 || col >= COLS) {
            return false;
        }
        int row = dropRow(col);
        if (row == -1) {
            return false;
        }
        board[row][col] = symbol;
        lastRow = row;
        lastCol = col;
        afterMove(symbol);
        return true;
    }

    /** The lowest empty row in this column, or -1 if the column is full. */
    public int dropRow(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == EMPTY) {
                return row;
            }
        }
        return -1;
    }

    /** The message to send for my move. */
    public static String moveToMessage(int col) {
        return String.valueOf(col);
    }

    /**
     * Only the disc that was just dropped can make a new line, so the check starts from it.
     * For each direction it counts the same color forwards and backwards.
     */
    @Override
    protected boolean hasWon(char symbol) {
        if (lastRow == -1 || board[lastRow][lastCol] != symbol) {
            return false;
        }
        for (int[] direction : DIRECTIONS) {
            int forward = countInDirection(lastRow, lastCol, direction[0], direction[1], symbol);
            int backward = countInDirection(lastRow, lastCol, -direction[0], -direction[1], symbol);
            if (1 + forward + backward >= 4) {
                return true;
            }
        }
        return false;
    }

    /** Counts the same symbol in a line, starting next to (row, col) and walking (dRow, dCol). */
    private int countInDirection(int row, int col, int dRow, int dCol, char symbol) {
        int count = 0;
        int r = row + dRow;
        int c = col + dCol;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == symbol) {
            count++;
            r += dRow;
            c += dCol;
        }
        return count;
    }

    public int getLastRow() {
        return lastRow;
    }

    public int getLastCol() {
        return lastCol;
    }
}
