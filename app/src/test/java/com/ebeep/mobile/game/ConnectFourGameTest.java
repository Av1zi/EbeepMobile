package com.ebeep.mobile.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Checks the Connect 4 rules without a phone or a beeper. */
public class ConnectFourGameTest {

    @Test
    public void starterIsRedAndMovesFirst() {
        ConnectFourGame game = new ConnectFourGame(true);
        assertEquals('R', game.getMySymbol());
        assertTrue(game.isMyTurn());
    }

    @Test
    public void discFallsToTheBottom() {
        ConnectFourGame game = new ConnectFourGame(true);
        game.playMine(3);
        assertEquals('R', game.getCell(ConnectFourGame.ROWS - 1, 3));
        assertEquals(ConnectFourGame.ROWS - 2, game.dropRow(3));
    }

    @Test
    public void verticalFourWins() {
        ConnectFourGame game = new ConnectFourGame(true);
        for (int i = 0; i < 3; i++) {
            game.playMine(0);
            game.playOpponent("1");
        }
        game.playMine(0);
        assertEquals(BoardGame.WIN, game.getResult());
    }

    @Test
    public void horizontalFourWins() {
        ConnectFourGame game = new ConnectFourGame(true);
        for (int col = 0; col < 3; col++) {
            game.playMine(col);
            game.playOpponent(String.valueOf(col));
        }
        game.playMine(3);
        assertEquals(BoardGame.WIN, game.getResult());
    }

    @Test
    public void diagonalFourOfOpponentIsALoss() {
        ConnectFourGame game = new ConnectFourGame(false);
        // The opponent (R) builds a rising diagonal from (bottom, 0) to (bottom-3, 3)
        game.playOpponent("0");
        game.playMine(1);
        game.playOpponent("1");
        game.playMine(2);
        game.playOpponent("2");
        game.playMine(3);
        game.playOpponent("2");
        game.playMine(3);
        game.playOpponent("3");
        game.playMine(6);
        game.playOpponent("3");
        assertEquals(BoardGame.LOSS, game.getResult());
    }

    @Test
    public void fullColumnIsRejected() {
        ConnectFourGame game = new ConnectFourGame(true);
        for (int i = 0; i < 3; i++) {
            game.playMine(5);
            game.playOpponent("5");
        }
        assertEquals(-1, game.dropRow(5));
        assertFalse(game.playMine(5));
    }

    @Test
    public void badMoveMessagesAreIgnored() {
        ConnectFourGame game = new ConnectFourGame(false);
        assertFalse(game.playOpponent("7"));
        assertFalse(game.playOpponent("x"));
        assertFalse(game.playOpponent("12"));
        assertTrue(game.playOpponent("6"));
    }

    @Test
    public void threeInARowIsNotAWin() {
        ConnectFourGame game = new ConnectFourGame(true);
        for (int i = 0; i < 2; i++) {
            game.playMine(0);
            game.playOpponent("1");
        }
        game.playMine(0);
        assertEquals(BoardGame.PLAYING, game.getResult());
    }
}
