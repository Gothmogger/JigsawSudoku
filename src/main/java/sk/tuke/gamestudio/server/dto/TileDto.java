package sk.tuke.gamestudio.server.dto;

import sk.tuke.gamestudio.SudokuSolver.Tile;
import sk.tuke.gamestudio.core.Field;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;


/* Custom mapping, mapper wouldn't help here much */
public class TileDto {
    @Min(0)
    @Max(8)
    private int row;
    @Min(0)
    @Max(8)
    private int column;
    @Min(0)
    @Max(9)
    private int displayedValue;
    private int groupId;

    public TileDto() {}

    public static TileDto from(Tile tile) {
        TileDto tileDto = new TileDto();
        tileDto.setColumn(tile.getColumn());
        tileDto.setRow(tile.getRow());
        tileDto.setDisplayedValue(tile.getDisplayedValue());
        tileDto.setGroupId(tile.getTileGroup().getId());
        return tileDto;
    }

    public static Tile to(TileDto tileDto, Field field) {
        return field.getTile(tileDto.getRow(), tileDto.getColumn());
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setDisplayedValue(int displayedValue) {
        this.displayedValue = displayedValue;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getDisplayedValue() {
        return displayedValue;
    }
}
