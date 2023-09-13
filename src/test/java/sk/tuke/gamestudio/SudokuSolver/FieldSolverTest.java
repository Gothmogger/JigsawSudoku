package sk.tuke.gamestudio.SudokuSolver;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FieldSolverTest {
    private static final int fieldSize = 9;

    @Test
    public void testPointingCoupletsMethod() {
        Tile[][] tiles = makeTestBoard();
        FieldSolver solver = new FieldSolver(fieldSize, tiles);
        coverTile(tiles, 1, 0);
        coverTile(tiles, 1,1);
        coverTile(tiles, 1,2);
        coverTile(tiles, 1, 6);
        /*
        int[] values = {8, 2, 7, 1, 5, 4, 3, 9, 6,
                        -, -, -, 3, 2, 7, -, 4, 8,
                        3, 4, 1, 6, 8, 9, 7, 5, 2,
                        5, 9, 3, 4, 6, 8, 2, 7, 1,
                        4, 7, 2, 5, 1, 3, 6, 8, 9,
                        6, 1, 8, 9, 7, 2, 4, 3, 5,
                        7, 8, 6, 2, 3, 5, 9, 1, 4,
                        1, 5, 4, 7, 9, 6, 8, 2, 3,
                        2, 3, 9, 8, 4, 1, 5, 6, 7};
         */
        tiles[1][0].getMarkings().addAll(List.of(1, 2));
        tiles[1][1].getMarkings().addAll(List.of(1, 2));
        tiles[1][2].getMarkings().addAll(List.of(1, 2));
        tiles[1][6].getMarkings().addAll(List.of(1, 2, 3));
        solver.pointingCoupletsMethod();
        Assert.assertEquals(tiles[1][0].getMarkings().size(), 2);
        Assert.assertEquals(tiles[1][1].getMarkings().size(), 2);
        Assert.assertEquals(tiles[1][2].getMarkings().size(), 2);
        Assert.assertEquals(tiles[1][6].getMarkings().size(), 1);
    }

    @Test
    public void testConjugatePairsMethod() {
        Tile[][] tiles = makeTestBoard();
        FieldSolver solver = new FieldSolver(fieldSize, tiles);
        coverTile(tiles, 1, 0);
        coverTile(tiles, 1,1);
        coverTile(tiles, 1,2);
        coverTile(tiles, 0, 1);
        /*
        int[] values = {8, -, 7, 1, 5, 4, 3, 9, 6,
                        -, -, -, 3, 2, 7, 1, 4, 8,
                        3, 4, 1, 6, 8, 9, 7, 5, 2,
                        5, 9, 3, 4, 6, 8, 2, 7, 1,
                        4, 7, 2, 5, 1, 3, 6, 8, 9,
                        6, 1, 8, 9, 7, 2, 4, 3, 5,
                        7, 8, 6, 2, 3, 5, 9, 1, 4,
                        1, 5, 4, 7, 9, 6, 8, 2, 3,
                        2, 3, 9, 8, 4, 1, 5, 6, 7};
         */
        tiles[1][0].getMarkings().addAll(List.of(1, 2, 3, 4));
        tiles[1][1].getMarkings().addAll(List.of(1, 2, 3, 4, 7, 5));
        tiles[1][2].getMarkings().addAll(List.of(1, 2, 3, 4));
        tiles[0][1].getMarkings().addAll(List.of(1, 2, 3, 4, 9));
        solver.conjugatePairsMethod();
        Assert.assertEquals(tiles[1][0].getMarkings().size(), 4);
        Assert.assertEquals(tiles[1][1].getMarkings().size(), 4);
        Assert.assertEquals(tiles[1][2].getMarkings().size(), 4);
        Assert.assertEquals(tiles[0][1].getMarkings().size(), 4);
    }

    @Test
    public void testSinglesMethod() {
        Tile[][] tiles = makeTestBoard();
        FieldSolver solver = new FieldSolver(fieldSize, tiles);
        coverTile(tiles, 1,1);
        coverTile(tiles, 1,2);
        coverTile(tiles, 1,5);
        coverTile(tiles, 7, 1);
        coverTile(tiles, 4, 1);
        /*
        int[] values = {8, 2, 7, 1, 5, 4, 3, 9, 6,
                        9, -, -, 3, 2, -, 1, 4, 8,
                        3, 4, 1, 6, 8, 9, 7, 5, 2,
                        5, 9, 3, 4, 6, 8, 2, 7, 1,
                        4, -, 2, 5, 1, 3, 6, 8, 9,
                        6, 1, 8, 9, 7, 2, 4, 3, 5,
                        7, 8, 6, 2, 3, 5, 9, 1, 4,
                        1, -, 4, 7, 9, 6, 8, 2, 3,
                        2, 3, 9, 8, 4, 1, 5, 6, 7};
         */
        solver.setMarkings();
        Assert.assertEquals(solver.singlesMethod(), true);

        Assert.assertEquals(tiles[1][1].getDisplayedValue(), tiles[1][1].getValue());
        Assert.assertEquals(tiles[1][2].getDisplayedValue(), tiles[1][2].getValue());
        Assert.assertEquals(tiles[1][5].getDisplayedValue(), tiles[1][5].getValue());
        Assert.assertEquals(tiles[4][1].getDisplayedValue(), tiles[4][1].getValue());
        Assert.assertEquals(tiles[7][1].getDisplayedValue(), tiles[7][1].getValue());
    }

    @Test
    public void testOnlyPossibleMethod() {
        Tile[][] tiles = makeTestBoard();
        FieldSolver solver = new FieldSolver(fieldSize, tiles);
        coverTile(tiles, 1,1);
        coverTile(tiles, 1,2);
        coverTile(tiles, 1,5);
        coverTile(tiles, 7, 1);
        coverTile(tiles, 4, 1);
        /*
        int[] values = {8, 2, 7, 1, 5, 4, 3, 9, 6,
                        9, -, -, 3, 2, -, 1, 4, 8,
                        3, 4, 1, 6, 8, 9, 7, 5, 2,
                        5, 9, 3, 4, 6, 8, 2, 7, 1,
                        4, -, 2, 5, 1, 3, 6, 8, 9,
                        6, 1, 8, 9, 7, 2, 4, 3, 5,
                        7, 8, 6, 2, 3, 5, 9, 1, 4,
                        1, -, 4, 7, 9, 6, 8, 2, 3,
                        2, 3, 9, 8, 4, 1, 5, 6, 7};
         */
        solver.setMarkings();
        Assert.assertEquals(solver.onlyPossibleMethod(), true);

        Assert.assertEquals(tiles[1][1].getDisplayedValue(), 0);

        Assert.assertEquals(tiles[1][2].getDisplayedValue(), tiles[1][2].getValue());
        Assert.assertEquals(tiles[1][5].getDisplayedValue(), tiles[1][5].getValue());
        Assert.assertEquals(tiles[4][1].getDisplayedValue(), tiles[4][1].getValue());
        Assert.assertEquals(tiles[7][1].getDisplayedValue(), tiles[7][1].getValue());
    }

    @Test
    public void testMarkings() {
        Tile[][] tiles = makeTestBoard();
        FieldSolver solver = new FieldSolver(fieldSize, tiles);
        coverTile(tiles, 1,1);
        coverTile(tiles, 1,2);
        coverTile(tiles, 1,5);
        coverTile(tiles, 7, 1);
        coverTile(tiles, 4, 1);
        /*
        int[] values = {8, 2, 7, 1, 5, 4, 3, 9, 6,
                        9, -, -, 3, 2, -, 1, 4, 8,
                        3, 4, 1, 6, 8, 9, 7, 5, 2,
                        5, 9, 3, 4, 6, 8, 2, 7, 1,
                        4, -, 2, 5, 1, 3, 6, 8, 9,
                        6, 1, 8, 9, 7, 2, 4, 3, 5,
                        7, 8, 6, 2, 3, 5, 9, 1, 4,
                        1, -, 4, 7, 9, 6, 8, 2, 3,
                        2, 3, 9, 8, 4, 1, 5, 6, 7};
         */
        solver.setMarkings();
        Assert.assertEquals(tiles[0][0].getMarkings().size(), 0);

        Assert.assertEquals(tiles[1][1].getMarkings().size(), 2);
        Assert.assertEquals(tiles[1][1].getMarkings().contains(5), true);
        Assert.assertEquals(tiles[1][1].getMarkings().contains(6), true);

        Assert.assertEquals(tiles[1][2].getMarkings().size(), 1);
        Assert.assertEquals(tiles[1][2].getMarkings().contains(5), true);

        Assert.assertEquals(tiles[7][1].getMarkings().size(), 1);
        Assert.assertEquals(tiles[7][1].getMarkings().contains(5), true);

        solver.removeFromMarkings(1, 1, 5, 0);
        Assert.assertEquals(tiles[1][1].getMarkings().contains(5), false);
        Assert.assertEquals(tiles[1][2].getMarkings().contains(5), false);
        Assert.assertEquals(tiles[7][1].getMarkings().contains(5), false);
    }

    private void coverTile(Tile[][] tiles, int row, int col) {
        tiles[row][col].setClue(false);
        tiles[row][col].displayValue(0);
    }

    private Tile[][] makeTestBoard() {
        TileGroup[] groups = new TileGroup[fieldSize];
        Tile[][] tiles = new Tile[fieldSize][fieldSize];
        for (int i = 0; i < fieldSize; i++) {
            groups[i] = new TileGroup(i);
        }

        int[] values = {8, 2, 7, 1, 5, 4, 3, 9, 6,
                        9, 6, 5, 3, 2, 7, 1, 4, 8,
                        3, 4, 1, 6, 8, 9, 7, 5, 2,
                        5, 9, 3, 4, 6, 8, 2, 7, 1,
                        4, 7, 2, 5, 1, 3, 6, 8, 9,
                        6, 1, 8, 9, 7, 2, 4, 3, 5,
                        7, 8, 6, 2, 3, 5, 9, 1, 4,
                        1, 5, 4, 7, 9, 6, 8, 2, 3,
                        2, 3, 9, 8, 4, 1, 5, 6, 7};

        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = new Tile(values[row * fieldSize + col], true, groups[(row / 3) * 3 + col / 3], row, col);
                tiles[row][col] = tile;
            }
        }

        return tiles;
    }
}
