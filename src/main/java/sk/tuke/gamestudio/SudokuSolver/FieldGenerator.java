package sk.tuke.gamestudio.SudokuSolver;

import java.util.*;

public class FieldGenerator {
    private final int fieldSize;
    private final Tile[][] tiles;
    private final TileGroup[] groups;
    private final Tile[][] groupTiles;

    private Random random;

    private final Set<Tile> tempGroupSet;
    private final Map<Tile, Tile> tempTileMap;

    public FieldGenerator(int fieldSize) {
        this.fieldSize = fieldSize;
        this.tiles = new Tile[fieldSize][fieldSize];

        if (fieldSize != 9)
            throw new RuntimeException("Unsupported board size");

        this.groups = new TileGroup[fieldSize];
        this.groupTiles = new Tile[fieldSize][fieldSize];
        this.random = new Random();
        this.tempGroupSet = new HashSet<>();
        this.tempTileMap = new HashMap<>();
    }

    public Tile[][] generateField() {
        generate9x9Field();
        transformIntoJigsaw();

        return tiles;
    }

    private void transformIntoJigsaw() {
        int[] shuffledGroupTiles = new int[fieldSize];
        Arrays.fill(shuffledGroupTiles, 0);
        int step = 0;
        while (step++ < fieldSize * 100) {
            for (int groupIndex = 0; groupIndex < fieldSize; groupIndex++) {
                if (shuffledGroupTiles[groupIndex] < fieldSize / 2 + 1) {
                    for (int tileIndex = 0; tileIndex < fieldSize; tileIndex++) {
                        int groupIndexOfSwitchedTile = switchTiles(groupIndex, tileIndex);
                        if (groupIndexOfSwitchedTile != -1) {
                            shuffledGroupTiles[groupIndex]++;
                            shuffledGroupTiles[groupIndexOfSwitchedTile]++;
                        }
                    }
                }
            }
        }
        tempTileMap.clear();
    }

    // Take a valid board: 8 2 7 1 5 4 3 9 6
    //                     9 6 5 3 2 7 1 4 8
    //                     3 4 1 6 8 9 7 5 2
    //                     5 9 3 4 6 8 2 7 1
    //                     4 7 2 5 1 3 6 8 9
    //                     6 1 8 9 7 2 4 3 5
    //                     7 8 6 2 3 5 9 1 4
    //                     1 5 4 7 9 6 8 2 3
    //                     2 3 9 8 4 1 5 6 7
    // We will shuffle numbers in there later. We could just generate a board beginning with: 1 2 3 4 5 6 7 8 9
    //                                                                                        4 5 6 7 8 9 1 2 3
    //                                                                                        7 8 9 1 2 3 4 5 6
    // ... etc. but notice the repetitions of 1 2 3 and others... Shuffling numbers around might change it to say 7 5 1
    // but it will still keep on repeating. That is why using premade board is better.
    // btw. we use premade board and then hide clues, because that is faster than starting with empty board and adding
    // clues until we have 1 solution
    private void generate9x9Field() {
        for (int i = 0; i < 9; i++) {
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

        for (int i = 0; i < fieldSize * 2; i++)
            shuffleValues(values, random.nextInt(fieldSize - 1) + 1, random.nextInt(fieldSize - 1) + 1);

        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = new Tile(values[row * fieldSize + col], true, groups[(row / 3) * 3 + col / 3], row, col);
                tiles[row][col] = tile;
                groupTiles[tile.getTileGroup().getId()][tile.getValue() - 1] = tile;
            }
        }

        for (int repeat = 0; repeat < fieldSize / 3; repeat++) {
            for (int i = 0; i < fieldSize; i++) {
                int ranNum = random.nextInt(3); // only works with 9x9 field
                int blockNumber = i / 3;
                shuffleRows(i, blockNumber * 3 + ranNum);
            }
            for (int i = 0; i < fieldSize; i++) {
                int ranNum = random.nextInt(3); // only works with 9x9 field
                int blockNumber = i / 3;
                shuffleColumns(i, blockNumber * 3 + ranNum);
            }
        }
        /* if we wanted to use the basic algorithm in the comment up
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                tiles[row][col] = new Tile((col + row) % fieldSize + 1, false, groups[row]);
            }
        }*/
    }

    private int switchTiles(int group1Index, int tile1Index) {
        Tile tile1 = groupTiles[group1Index][tile1Index];

        for (int group2Index = 0; group2Index < fieldSize; group2Index++) {
            if (group2Index == group1Index)
                continue;

            Tile tile2 = groupTiles[group2Index][tile1.getValue() - 1];
            if (tempTileMap.get(tile1) == tile2)
                continue;

            switchTilesInAnArray(tile1, tile2);

            if (!isTileGroupWhole(tile1) | !isTileGroupWhole(tile2)) {
                switchTilesInAnArray(tile1, tile2);
                continue;
            }

            tempTileMap.put(tile1, tile2);
            tempTileMap.put(tile2, tile1);

            return group2Index;
        }

        return -1;
    }

    private void switchTilesInAnArray(Tile tile1, Tile tile2) {
        int row1 = tile1.getRow();
        int col1 = tile1.getColumn();
        tiles[tile1.getRow()][tile1.getColumn()] = tile2;
        tiles[tile2.getRow()][tile2.getColumn()] = tile1;
        tile1.setColumn(tile2.getColumn());
        tile1.setRow(tile2.getRow());
        tile2.setRow(row1);
        tile2.setColumn(col1);
    }

    private boolean isTileGroupWhole(Tile tile) {
        tempGroupSet.clear();
        if (tileGroupDepthFirstSearch(tile.getRow(), tile.getColumn(), tile.getTileGroup())) return true;

        return false;
    }

    private boolean tileGroupDepthFirstSearch(int row, int col, TileGroup group) {
        tempGroupSet.add(tiles[row][col]);
        if (row > 0 && tiles[row - 1][col].getTileGroup() == group && !tempGroupSet.contains(tiles[row - 1][col])) {
            if (tileGroupDepthFirstSearch(row - 1, col, group))
                return true;
        }
        if (row < fieldSize - 1 && tiles[row + 1][col].getTileGroup() == group && !tempGroupSet.contains(tiles[row + 1][col])) {
            if (tileGroupDepthFirstSearch(row + 1, col, group))
                return true;
        }
        if (col > 0 && tiles[row][col - 1].getTileGroup() == group && !tempGroupSet.contains(tiles[row][col - 1])) {
            if (tileGroupDepthFirstSearch(row, col - 1, group))
                return true;
        }
        if (col < fieldSize - 1 && tiles[row][col + 1].getTileGroup() == group && !tempGroupSet.contains(tiles[row][col + 1])) {
            if (tileGroupDepthFirstSearch(row, col + 1, group))
                return true;
        }
        return tempGroupSet.size() == fieldSize ? true : false;
    }

    private void shuffleValues(int[] values, int value1, int value2) {
        if (value1 == value2)
            return;
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                if (values[row * fieldSize + col] == value1)
                    values[row * fieldSize + col] = value2;
                else if (values[row * fieldSize + col] == value2)
                    values[row * fieldSize + col] = value1;
            }
        }
    }

    private void shuffleRows(int row1, int row2) {
        if (row1 == row2)
            return;

        for (int i = 0; i < fieldSize; i++) {
            Tile tempTile = tiles[row2][i];
            tiles[row2][i] = tiles[row1][i];
            tiles[row1][i] = tempTile;
        }
    }

    private void shuffleColumns(int col1, int col2) {
        if (col1 == col2)
            return;

        for (int i = 0; i < fieldSize; i++) {
            Tile tempTile = tiles[i][col2];
            tiles[i][col2] = tiles[i][col1];
            tiles[i][col1] = tempTile;
        }
    }
}
