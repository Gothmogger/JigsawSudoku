import styles from "../../field.module.css";
import Skeleton from "react-loading-skeleton";
import Tile from "./Tile";
import { BoardDataType, TileValueType } from "./gameSlice";

interface Props {
  data?: BoardDataType;
  colors: string[];
  fieldSize: number;
  setTileValue: (row: number, column: number, value: TileValueType) => void;
  setTileMarkings: (row: number, column: number, markings: string) => void;
}

let skeletons: [][];

function Field({
  data,
  colors,
  fieldSize,
  setTileValue,
  setTileMarkings,
}: Props) {
  if (!skeletons) skeletons = getEmpty2DArray(fieldSize);
  return (
    <table className={"mx-auto " + styles.tileTable}>
      <tbody>
        {data != undefined
          ? data.tiles.map((tileRow, rowIndex) => (
              <tr key={"tileRow" + rowIndex}>
                {tileRow.map((tile, columnIndex) => (
                  <td
                    className={styles.tileTd}
                    key={"tile" + rowIndex + columnIndex}
                  >
                    <Tile
                      row={rowIndex}
                      column={columnIndex}
                      value={tile.tileDto.displayedValue}
                      color={colors[tile.tileDto.groupId]}
                      markings={tile.info.markings}
                      state={tile.info.state}
                      setValue={setTileValue}
                      setMarkings={setTileMarkings}
                    />
                  </td>
                ))}
              </tr>
            ))
          : skeletons.map((skeletonRow, rowIndex) => (
              <tr key={"skeletonRow" + rowIndex}>
                {skeletonRow.map((_skeleton, columnIndex) => (
                  <td
                    key={"fieldSkeleton" + rowIndex + columnIndex}
                    style={{ lineHeight: 1 }}
                    className={styles.tileTd}
                  >
                    <Skeleton width={88} height={"5.5em"} borderRadius={0} />
                  </td>
                ))}
              </tr>
            ))}
      </tbody>
    </table>
  );
}

function getEmpty2DArray(size: number): [][] {
  let arr = new Array(size);

  for (let i = 0; i < size; i++) {
    arr[i] = new Array(size).fill(0);
  }
  return arr;
}

export default Field;
