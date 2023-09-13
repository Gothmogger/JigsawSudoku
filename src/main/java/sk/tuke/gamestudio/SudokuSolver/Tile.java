package sk.tuke.gamestudio.SudokuSolver;

import java.util.Set;
import java.util.TreeSet;

// This tile makes set row and column and clue available, only to this package. as other packages of course dont need it.
public class Tile {
    private final int value;
    private int displayedValue;
    private final TileGroup group;
    private boolean isClue;
    private int row;
    private int column;

    private final Set<Integer> markings;

    public Tile(int value, boolean isClue, TileGroup group, int row, int column) {
        this.value = value;
        this.isClue = isClue;
        this.group = group;
        this.row = row;
        this.column = column;
        this.markings = new TreeSet<>();

        if (isClue)
            displayedValue = value;
        else
            displayedValue = 0;
    }

    public void addMark(int value) {
        markings.add(value);
    }

    public void removeMark(int value) {
        markings.remove(value);
    }

    public Set<Integer> getMarkings() {
        return markings;
    }

    public TileGroup getTileGroup() {
        return group;
    }

    public void displayValue(int value) {
        this.displayedValue = value;
    }

    public int getValue() {
        return value;
    }

    public int getDisplayedValue() {
        return displayedValue;
    }

    public boolean isClue() {
        return isClue;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    void setRow(int row) {
        this.row = row;
    }

    void setColumn(int column) {
        this.column = column;
    }

    void setClue(boolean isClue) {
        this.isClue = isClue;
    }
}
