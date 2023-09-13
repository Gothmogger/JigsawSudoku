import { useRef } from "react";
import styles from "../../field.module.css";
import { TileStateType, TileValueType } from "./gameSlice";

interface Props {
  row: number;
  column: number;
  value: TileValueType;
  color: string;
  markings: string;
  state: TileStateType;
  setValue: (row: number, column: number, value: TileValueType) => void;
  setMarkings: (row: number, column: number, markings: string) => void;
}

const numberRegex = /^\d*$/;

function Tile({
  row,
  column,
  value,
  color,
  markings,
  state,
  setValue,
  setMarkings,
}: Props) {
  const text = useRef("");

  const correctnessClass =
    state === "correct"
      ? " correct"
      : state === "incorrect"
      ? " incorrect"
      : "";

  return (
    <>
      <input
        type="text"
        className={styles.mark}
        style={{ backgroundColor: color }}
        value={markings}
        readOnly={state === "hint" || state === "correct"}
        onChange={(e) => {
          let target = e.target;
          let value = target.value;
          setMarkings(row, column, value);
        }}
      ></input>
      <input
        type="text"
        className={styles.tile + correctnessClass}
        style={{ backgroundColor: color }}
        readOnly={state === "hint" || state === "correct"}
        value={value != 0 ? value : ""}
        onChange={(e) => {
          let target = e.target;
          let value = target.value;
          let intValue = parseInt(value) as TileValueType;
          if (
            (numberRegex.test(value) && intValue > 0 && intValue < 10) ||
            !value
          ) {
            target.setCustomValidity("");
            target.classList.remove("input-error");
            text.current = value;
            setValue(row, column, value ? intValue : 0);
          } else {
            target.setCustomValidity("Only digits between 1 - 9 allowed");
            target.classList.add("input-error");
            target.reportValidity();
            target.value = text.current;
          }
        }}
      ></input>
    </>
  );
}

export default Tile;
