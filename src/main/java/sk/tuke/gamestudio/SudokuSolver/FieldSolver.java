package sk.tuke.gamestudio.SudokuSolver;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FieldSolver {
    private final int fieldSize;
    private final Tile[][] tiles;
    private final Tile[][] groupTiles;

    private Random random;
    private Set<Tile> neccessaryClues;

    private ArrayList<Set<Tile>> temporarySetArray;

    public FieldSolver(int fieldSize, Tile[][] tiles) {
        this.fieldSize = fieldSize;
        this.tiles = tiles;
        this.groupTiles = new Tile[fieldSize][fieldSize];
        generateGroupTiles();

        this.random = new Random();
        this.neccessaryClues = new HashSet<>();
        this.temporarySetArray = new ArrayList<>(fieldSize);
        for (int i = 0; i < fieldSize; i++) {
            temporarySetArray.add(new HashSet<>());
        }
    }

    /** generate an array which maps tiles to their group
     * fieldGenerator uses the same field but we generate a new one to decouple from generator for testing
     */
    private void generateGroupTiles() {
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = tiles[row][col];
                groupTiles[tile.getTileGroup().getId()][tile.getValue() - 1] = tile;
            }
        }
    }

    public void getCluesMedium() {
        int clues = fieldSize * fieldSize;
        //while (clues > fieldSize * fieldSize / 2.3f) {
        while (clues > 50) {
            Tile tile = null;
            do {
                int row = random.nextInt(fieldSize);
                int col = random.nextInt(fieldSize);
                tile = tiles[row][col];
            } while (!tile.isClue() & !neccessaryClues.contains(tile));

            tile.setClue(false);
            tile.displayValue(0);

            if (fieldHasOneSolution()) {
                clues--;
            } else {
                tile.setClue(true);
                tile.displayValue(tile.getValue());
                neccessaryClues.add(tile);
            }
            resetNonClues();
        }
        removeMarkings();
    }

    private void removeMarkings() {
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                    tiles[row][col].getMarkings().clear();
            }
        }
    }

    private void resetNonClues() {
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                if (!tiles[row][col].isClue())
                    tiles[row][col].displayValue(0);
            }
        }
    }

    private boolean fieldHasOneSolution() {
        setMarkings();
        for (int i = 0; i < fieldSize * fieldSize; i++) {
            boolean solvedAtLeastOneTile = false;
            pointingCoupletsMethod();
            conjugatePairsMethod();
            solvedAtLeastOneTile = onlyPossibleMethod();
            if (!solvedAtLeastOneTile)
                solvedAtLeastOneTile = singlesMethod();
            if (isSolved())
                return true;
            if (!solvedAtLeastOneTile)
                return false;
        }
        return true;
    }

    private void bigLawOfLeftoversMethod() {

    }

    private void littleLawOfLeftoversMethod() {

    }

    private void XWingsMethod() {

    }

    void pointingCoupletsMethod() {
        for (int groupIndex = 0; groupIndex < fieldSize; groupIndex++) {
            for (int valueIndex = 0; valueIndex < fieldSize; valueIndex++) {
                Tile tile = groupTiles[groupIndex][valueIndex];
                if (!tile.isClue() && tile.getDisplayedValue() == 0)
                    tile.getMarkings().forEach(a -> temporarySetArray.get(a - 1).add(tile));
            }
            removeInvalidMarks(groupIndex);
        }
    }

    private void removeInvalidMarks(int groupID) {
        for (int value = 0; value < fieldSize; value++) {
            if (temporarySetArray.get(value).size() > 1) {
                Iterator<Tile> iterator = temporarySetArray.get(value).iterator();
                int row = -1;
                int column = -1;
                while (iterator.hasNext()) {
                    Tile tile = iterator.next();
                    if (row != -10) {
                        if (row == -1)
                            row = tile.getRow();
                        else if (row != tile.getRow())
                            row = -10;
                        else
                            row = tile.getRow();
                    }

                    if (column != -10) {
                        if (column == -1)
                            column = tile.getColumn();
                        else if (column != tile.getColumn())
                            column = -10;
                        else
                            column = tile.getColumn();
                    }
                }
                if (row != -10) {
                    for (int columnIndex = 0; columnIndex < fieldSize; columnIndex++)
                        if (tiles[row][columnIndex].getTileGroup().getId() != groupID)
                            tiles[row][columnIndex].getMarkings().remove(value + 1);
                }
                if (column != -10) {
                    for (int rowIndex = 0; rowIndex < fieldSize; rowIndex++)
                        if (tiles[rowIndex][column].getTileGroup().getId() != groupID)
                            tiles[rowIndex][column].getMarkings().remove(value + 1);
                }

            }
        }
        temporarySetArray.forEach(set -> set.clear());
    }

    // mostly I can find this method using 2 values and two tiles, but it could be x values and x tiles. Ill make 2 3 and 4, dont think more would help
    void conjugatePairsMethod() {
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = tiles[row][col];
                if (!tile.isClue() && tile.getDisplayedValue() == 0)
                    tile.getMarkings().forEach(a -> temporarySetArray.get(a - 1).add(tile));
            }
            keepValidMarks();
        }
        for (int col = 0; col < fieldSize; col++) {
            for (int row = 0; row < fieldSize; row++) {
                Tile tile = tiles[row][col];
                if (!tile.isClue() && tile.getDisplayedValue() == 0)
                    tile.getMarkings().forEach(a -> temporarySetArray.get(a - 1).add(tile));
            }
            keepValidMarks();
        }
        for (int groupIndex = 0; groupIndex < fieldSize; groupIndex++) {
            for (int valueIndex = 0; valueIndex < fieldSize; valueIndex++) {
                Tile tile = groupTiles[groupIndex][valueIndex];
                if (!tile.isClue() && tile.getDisplayedValue() == 0)
                    tile.getMarkings().forEach(a -> temporarySetArray.get(a - 1).add(tile));
            }
            keepValidMarks();
        }
    }

    private void keepValidMarks() {
        temporarySetArray.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((set, occurrences) -> { // set of tiles and how many of them there are (lets say two tiles have makrs 2,3, so those tiles will have occurene 2
                    if (set.size() > 1 & occurrences == set.size()) {
                        List<Set<Integer>> listOfMarkingSets = set.stream().map(Tile::getMarkings).collect(Collectors.toList());
                        Set<Integer> marksToKeep = set.stream().map(Tile::getMarkings).skip(1).collect(() -> new HashSet<>(listOfMarkingSets.get(0)), Set::retainAll, Set::retainAll);
                        set.forEach(tile -> tile.getMarkings().retainAll(marksToKeep));
                        temporarySetArray.forEach(x -> x.clear());
                    }
                });
        temporarySetArray.forEach(set -> set.clear());
    }

    // one number in markings, that is found only once in row, column or group
    boolean singlesMethod() {
        boolean solvedSomething = false;
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = tiles[row][col];
                if (!tile.isClue() && tile.getDisplayedValue() == 0)
                    tile.getMarkings().forEach(a -> temporarySetArray.get(a - 1).add(tile));
            }
            if (displayTheSingleMark())
                solvedSomething = true;
        }
        for (int col = 0; col < fieldSize; col++) {
            for (int row = 0; row < fieldSize; row++) {
                Tile tile = tiles[row][col];
                if (!tile.isClue() && tile.getDisplayedValue() == 0)
                    tile.getMarkings().forEach(a -> temporarySetArray.get(a - 1).add(tile));
            }
            if (displayTheSingleMark())
                solvedSomething = true;
        }
        for (int groupIndex = 0; groupIndex < fieldSize; groupIndex++) {
            for (int valueIndex = 0; valueIndex < fieldSize; valueIndex++) {
                Tile tile = groupTiles[groupIndex][valueIndex];
                if (!tile.isClue() && tile.getDisplayedValue() == 0)
                    tile.getMarkings().forEach(a -> temporarySetArray.get(a - 1).add(tile));
            }
            if (displayTheSingleMark())
                solvedSomething = true;
        }

        return solvedSomething;
    }

    private boolean displayTheSingleMark() {
        boolean solvedSomething = false;
        for (int i = 0; i < fieldSize; i++) {
            if (temporarySetArray.get(i).size() == 1) {
                Tile tile = temporarySetArray.get(i).stream().findFirst().get();
                tile.displayValue(i + 1);
                removeFromMarkings(tile.getRow(), tile.getColumn(), tile.getDisplayedValue(), tile.getTileGroup().getId());
                solvedSomething = true;
            }
            temporarySetArray.get(i).clear();
        }
        return solvedSomething;
    }

    // only one number in a tile marking
    boolean onlyPossibleMethod() {
        boolean solvedSomething = false;
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = tiles[row][col];
                if (!tile.isClue()) {
                    if (tile.getMarkings().size() == 1) {
                        tile.displayValue(tile.getMarkings().stream().findFirst().get());
                        removeFromMarkings(tile.getRow(), tile.getColumn(), tile.getDisplayedValue(), tile.getTileGroup().getId());
                        solvedSomething = true;
                    }
                }
            }
        }

        return solvedSomething;
    }

    void removeFromMarkings(int row, int col, int mark, int groupId) {
        for (int i = 0; i < fieldSize; i++) {
            tiles[row][i].getMarkings().remove(mark);
            tiles[i][col].getMarkings().remove(mark);
        }
        for (int valueIndex = 0; valueIndex < fieldSize; valueIndex++) {
            groupTiles[groupId][valueIndex].removeMark(mark);
        }
    }

    void setMarkings() {
        for (int row = 0; row < fieldSize; row++) {
            for (int col = 0; col < fieldSize; col++) {
                Tile tile = tiles[row][col];
                if (!tile.isClue() && tile.getDisplayedValue() == 0) {
                    for (int i = 1; i <= fieldSize; i++)
                        tile.addMark(i);
                    for (int rowUp = row - 1; rowUp >= 0; rowUp--)
                        tile.removeMark(tiles[rowUp][col].getDisplayedValue());
                    for (int rowDown = row + 1; rowDown < fieldSize; rowDown++)
                        tile.removeMark(tiles[rowDown][col].getDisplayedValue());
                    for (int colLeft = col - 1; colLeft >= 0; colLeft--)
                        tile.removeMark(tiles[row][colLeft].getDisplayedValue());
                    for (int colRight = col + 1; colRight < fieldSize; colRight++)
                        tile.removeMark(tiles[row][colRight].getDisplayedValue());
                    for (int valueDown = tile.getValue() - 1; valueDown > 0; valueDown--)
                        tile.removeMark(groupTiles[tile.getTileGroup().getId()][valueDown - 1].getDisplayedValue());
                    for (int valueUp = tile.getValue() + 1; valueUp <= fieldSize; valueUp++)
                        tile.removeMark(groupTiles[tile.getTileGroup().getId()][valueUp - 1].getDisplayedValue());
                }
            }
        }
    }

    private boolean isSolved() {
        for (int row = 0; row < fieldSize; row++) {
            for (int column = 0; column < fieldSize; column++) {
                if (tiles[row][column].getValue() != tiles[row][column].getDisplayedValue())
                    return false;
            }
        }
        return true;
    }
}
