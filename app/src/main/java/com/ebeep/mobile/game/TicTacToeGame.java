package com.ebeep.mobile.game;

/**
 * Tic-tac-toe on a 3x3 board. The side that starts is X, like in the firmware.
 * Moves are sent as "row,col", for example "1,2".
 */
public class TicTacToeGame extends BoardGame {

    public static final int SIZE = 3;

    public TicTacToeGame(boolean iStart) {
        super(SIZE, SIZE, iStart ? 'X' : 'O', iStart ? 'O' : 'X', iStart);
    }

    /** My move. Returns false if it isn't allowed. */
    public boolean playMine(int row, int col) {
        if (!isMyTurn() || !isInside(row, col) || board[row][col] != EMPTY) {
            return false;
        }
        board[row][col] = mySymbol;
        afterMove(mySymbol);
        return true;
    }

    /** The beeper's move, from a message like "1,2". Returns false if the message isn't a legal move. */
    public boolean playOpponent(String move) {
        if (myTurn || isOver()) {
            return false;
        }
        if (move.length() != 3 || move.charAt(1) != ',') {
            return false;
        }

        int row = move.charAt(0) - '0';
        int col = move.charAt(2) - '0';
        if (!isInside(row, col) || board[row][col] != EMPTY) {
            return false;
        }

        board[row][col] = opponentSymbol;
        afterMove(opponentSymbol);
        return true;
    }

    /** The message to send for my move. */
    public static String moveToMessage(int row, int col) {
        return row + "," + col;
    }

    private boolean isInside(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    @Override
    protected boolean hasWon(char s) {
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] == s && board[i][1] == s && board[i][2] == s) {
                return true;  // row
            }
            if (board[0][i] == s && board[1][i] == s && board[2][i] == s) {
                return true;  // column
            }
        }
        return (board[0][0] == s && board[1][1] == s && board[2][2] == s)
                || (board[0][2] == s && board[1][1] == s && board[2][0] == s);
    }
}
