package sk.tuke.gamestudio.core;

import sk.tuke.gamestudio.SudokuSolver.FieldGenerator;
import sk.tuke.gamestudio.SudokuSolver.FieldSolver;
import sk.tuke.gamestudio.SudokuSolver.Tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Field {
    private int fieldSize;
    private Tile[][] tiles;

    private GameState gameState;

    //private long timeAtTheStart;
    private int correctMoves, incorrectMoves, hints;
    private int clueAmount;

    public Field() {}

    public Field(int fieldSize) {
        initialize(fieldSize);
    }

    public void initialize(int fieldSize) {
        this.fieldSize = fieldSize;
        gameState = GameState.Playing;
        tiles = generateField();
        //timeAtTheStart = System.currentTimeMillis();
        correctMoves = 0;
        incorrectMoves = 0;
        clueAmount = 0;
        hints = 0;

        for (int row = 0; row < fieldSize; row++) {
            for (int column = 0; column < fieldSize; column++) {
                if (tiles[row][column].isClue()) {
                    clueAmount++;
                }
            }
        }
    }

    private Tile[][] generateField() {
        final Tile[][] tiles;
        FieldGenerator fieldGenerator = new FieldGenerator(fieldSize);
        tiles = fieldGenerator.generateField();
        FieldSolver solver = new FieldSolver(fieldSize, tiles);
        solver.getCluesMedium();
        return tiles;
    }

    public void setTileDisplayValue(int row, int column, int value) {
        if (!tiles[row][column].isClue()) {
            if (value == tiles[row][column].getValue())
                correctMoves++;
            else
                incorrectMoves++;
            tiles[row][column].displayValue(value);
            isSolved();
        }
    }

    public void setTileMark(int row, int column, int value, boolean add) {
        if (!tiles[row][column].isClue()) {
            if (add)
                tiles[row][column].addMark(value);
            else
                tiles[row][column].removeMark(value);
        }
    }

    public Tile getHint() {
        List<Tile> list = new ArrayList<Tile>();
        for (Tile[] array : tiles) {
            list.addAll(Arrays.asList(array));
        }
        Collections.shuffle(list);

        for (Tile tile : list) {
            if (tile.getDisplayedValue() == 0) {
                tile.displayValue(tile.getValue());
                hints++;
                isSolved();
                return tile;
            }
        }

        return null;
    }

    private boolean isSolved() {
        if (getMaxPossibleScore() == 0) {
            gameState = GameState.Failed;
            return false;
        }

        for (int row = 0; row < fieldSize; row++) {
            for (int column = 0; column < fieldSize; column++) {
                if (tiles[row][column].getValue() != tiles[row][column].getDisplayedValue()) {
                    return false;
                }
            }
        }
        gameState = GameState.Solved;
        return true;
    }

    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    public int getFieldSize() {
        return fieldSize;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getScore() {
            /*int seconds = (int) ((System.currentTimeMillis() - timeAtTheStart) / 1000);
            return 24 * 60 * 60 - seconds;*/
        int score = 20 * (correctMoves - 2 * incorrectMoves);
        return score > 0 ? score : 0;
    }

    public int getMaxPossibleScore() {
        int result = 20 * (fieldSize * fieldSize - clueAmount - hints - 2 * incorrectMoves);
        return result > 0 ? result : 0;
    }

    public boolean unInitialized() {
        return tiles == null;
    }

    public Tile[][] getTiles() {
        return tiles;
    }
}
