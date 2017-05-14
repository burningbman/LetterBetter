package com.cor31.letterbetter.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by bbadders on 8/26/2016.
 */
public class BoardTest
{
    @Test
    public void testInit() {
        Board board = new Board(1);
        assertEquals(1, board.numTilesHorizontal);
    }
}
