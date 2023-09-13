package sk.tuke.gamestudio.core;

import sk.tuke.gamestudio.SudokuSolver.Tile;
import org.junit.Assert;
import org.junit.Test;

public class FieldTest {
    private static final int fieldSize = 9;

    @Test
    public void testFieldTileOperations() {
        Field field = new Field(fieldSize);

        Tile nonClueTile = null;
        Tile clueTile = null;
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                if (field.getTile(row, col).isClue())
                    clueTile = field.getTile(row, col);
                else
                    nonClueTile = field.getTile(row, col);
            }
        }

        field.setTileMark(nonClueTile.getRow(), nonClueTile.getColumn(), 5, true);
        field.setTileMark(nonClueTile.getRow(), nonClueTile.getColumn(), 5, true);
        field.setTileMark(nonClueTile.getRow(), nonClueTile.getColumn(), 4, true);
        field.setTileMark(nonClueTile.getRow(), nonClueTile.getColumn(), 6, true);
        field.setTileMark(nonClueTile.getRow(), nonClueTile.getColumn(), 6, false);

        Assert.assertEquals(nonClueTile.getMarkings().size(), 2);
        Assert.assertEquals(nonClueTile.getMarkings().contains(4), true);
        Assert.assertEquals(nonClueTile.getMarkings().contains(5), true);

        field.setTileMark(clueTile.getRow(), clueTile.getColumn(), 5, true);
        Assert.assertEquals(clueTile.getMarkings().size(), 0);
    }

    @Test
    public void testFieldSolved() {
        Field field = new Field(fieldSize);
        Assert.assertEquals(field.getGameState(), GameState.Playing);

        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                field.setTileDisplayValue(row, col, field.getTile(row, col).getValue());
            }
        }

        Assert.assertEquals(field.getGameState(), GameState.Solved);
    }
}
