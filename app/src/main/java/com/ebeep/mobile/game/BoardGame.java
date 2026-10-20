package com.ebeep.mobile.game;

/**
 * What every two-player board game against the beeper has in common:
 * the board, whose turn it is, and the result.
 * This class has no Android code, so it can be tested with plain JUnit.
 */
public abstract class BoardGame {

    public static final int PLAYING = 0;
    public static final int WIN = 1;
    public static final int LOSS = 2;
    public static final int DRAW = 3;

    public static final char EMPTY = ' ';

    protected final char[][] board;
    protected final char mySymbol;
    protected final char opponentSymbol;
    protected boolean myTurn;
    protected int result = PLAYING;

    protected BoardGame(int rows, int cols, char mySymbol, char opponentSymbol, boolean iStart) {
        board = new char[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                board[row][col] = EMPTY;
            }
        }
        this.mySymbol = mySymbol;
        this.opponentSymbol = opponentSymbol;
        this.myTurn = iStart;
    }

    /** True if this symbol has a winning line. Each game checks differently. */
    protected abstract boolean hasWon(char symbol);

    /** Called after every move: updates the result and passes the turn. */
    protected void afterMove(char symbol) {
        if (hasWon(symbol)) {
            result = (symbol == mySymbol) ? WIN : LOSS;
        } else if (isBoardFull()) {
            result = DRAW;
        }
        myTurn = (symbol == opponentSymbol);
    }

    protected boolean isBoardFull() {
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public char getCell(int row, int col) {
        return board[row][col];
    }

    public int getRows() {
        return board.length;
    }

    public int getCols() {
        return board[0].length;
    }

    /** True when it's my turn and the game isn't over. */
    public boolean isMyTurn() {
        return myTurn && result == PLAYING;
    }

    public int getResult() {
        return result;
    }

    public boolean isOver() {
        return result != PLAYING;
    }

    public char getMySymbol() {
        return mySymbol;
    }

    public char getOpponentSymbol() {
        return opponentSymbol;
    }
}
