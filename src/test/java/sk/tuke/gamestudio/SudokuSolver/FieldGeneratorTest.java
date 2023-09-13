package sk.tuke.gamestudio.SudokuSolver;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FieldGeneratorTest {
    private static final int fieldSize = 9;
    private FieldGenerator generator = new FieldGenerator(fieldSize);

    private Set<Tile> tileSet = new HashSet<>();

    @Test
    public void testBoardValidity() {
        Tile[][] tiles = generator.generateField();
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Assert.assertEquals(tileSet.add(tiles[row][col]), true);
            }
        }
        tileSet.clear();
        for (int col = 0; col < fieldSize; col++) {
            for (int row = 0; row < fieldSize; row++) {
                Assert.assertEquals(tileSet.add(tiles[row][col]), true);
            }
        }
        tileSet.clear();
        Set<Tile>[] groupTileSet = new HashSet[fieldSize];
        Arrays.fill(groupTileSet, new HashSet<>());
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = tiles[row][col];
                Assert.assertEquals(groupTileSet[tile.getTileGroup().getId()].add(tile), true);
            }
        }
    }
}
