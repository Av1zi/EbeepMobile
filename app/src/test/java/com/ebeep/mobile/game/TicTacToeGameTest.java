package com.ebeep.mobile.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Checks the tic-tac-toe rules without a phone or a beeper. */
public class TicTacToeGameTest {

    @Test
    public void starterIsXAndMovesFirst() {
        TicTacToeGame game = new TicTacToeGame(true);
        assertEquals('X', game.getMySymbol());
        assertTrue(game.isMyTurn());

        TicTacToeGame other = new TicTacToeGame(false);
        assertEquals('O', other.getMySymbol());
        assertFalse(other.isMyTurn());
    }

    @Test
    public void rowOfThreeWins() {
        TicTacToeGame game = new TicTacToeGame(true);
        game.playMine(0, 0);
        game.playOpponent("1,0");
        game.playMine(0, 1);
        game.playOpponent("1,1");
        game.playMine(0, 2);
        assertEquals(BoardGame.WIN, game.getResult());
    }

    @Test
    public void opponentDiagonalIsALoss() {
        TicTacToeGame game = new TicTacToeGame(false);
        game.playOpponent("0,0");
        game.playMine(0, 1);
        game.playOpponent("1,1");
        game.playMine(0, 2);
        game.playOpponent("2,2");
        assertEquals(BoardGame.LOSS, game.getResult());
    }

    @Test
    public void fullBoardWithoutWinnerIsDraw() {
        TicTacToeGame game = new TicTacToeGame(true);
        // X O X
        // X O O
        // O X X
        game.playMine(0, 0);
        game.playOpponent("0,1");
        game.playMine(0, 2);
        game.playOpponent("1,1");
        game.playMine(1, 0);
        game.playOpponent("1,2");
        game.playMine(2, 1);
        game.playOpponent("2,0");
        game.playMine(2, 2);
        assertEquals(BoardGame.DRAW, game.getResult());
    }

    @Test
    public void cannotPlayOnTakenCell() {
        TicTacToeGame game = new TicTacToeGame(true);
        game.playMine(1, 1);
        game.playOpponent("0,0");
        assertFalse(game.playMine(0, 0));
        assertFalse(game.playMine(1, 1));
    }

    @Test
    public void cannotPlayOutOfTurn() {
        TicTacToeGame game = new TicTacToeGame(true);
        assertFalse(game.playOpponent("0,0"));
        game.playMine(0, 0);
        assertFalse(game.playMine(1, 1));
    }

    @Test
    public void badMoveMessagesAreIgnored() {
        TicTacToeGame game = new TicTacToeGame(false);
        assertFalse(game.playOpponent("9,9"));
        assertFalse(game.playOpponent("hello"));
        assertFalse(game.playOpponent(""));
        assertTrue(game.playOpponent("2,1"));
    }

    @Test
    public void noMovesAfterTheGameEnds() {
        TicTacToeGame game = new TicTacToeGame(true);
        game.playMine(0, 0);
        game.playOpponent("1,0");
        game.playMine(0, 1);
        game.playOpponent("1,1");
        game.playMine(0, 2);
        assertFalse(game.isMyTurn());
        assertFalse(game.playOpponent("2,2"));
    }

    @Test
    public void moveMessageFormat() {
        assertEquals("1,2", TicTacToeGame.moveToMessage(1, 2));
    }
}
