import { act, fireEvent, render, waitFor } from "@testing-library/react";

import Tile from "../src/components/game/Tile";
import { useState } from "react";

describe("Tile component", () => {
  function setupTile(props: any) {
    // for testing controlled inputs
    let setTileValue: any;
    let setMarkings: any;
    let setTileState: any;
    function TestEnvironment() {
      const [value, setValue] = useState(props.value);
      const [markingsValue, setMarkingsValue] = useState(props.markings);
      const [state, setState] = useState(props.state);

      // Doesn't work with useCallback for some reason, so we just make sure we don't overwrite these functions.
      if (!setTileValue)
        setTileValue = vi.fn((_row, _col, value) => {
          setState("");
          setValue(value);
        });
      if (!setMarkings)
        setMarkings = vi.fn((_row, _col, value) => setMarkingsValue(value));
      if (!setTileState) setTileState = setState;

      return (
        <table>
          <tbody>
            <tr>
              <td>
                <Tile
                  row={props.row}
                  column={props.column}
                  value={value}
                  color={props.color}
                  markings={markingsValue}
                  state={state}
                  setValue={setTileValue}
                  setMarkings={setMarkings}
                />
              </td>
            </tr>
          </tbody>
        </table>
      );
    }
    const result = render(<TestEnvironment />);

    return { result, setTileValue, setMarkings, setTileState };
  }

  test("Should render main textBox and markingTextBox with correct values, color and states", () => {
    const { result, setTileState } = setupTile({
      row: 1,
      column: 1,
      value: 0,
      color: "white",
      markings: "1,2",
      state: "",
    });

    const mainTextBox = result.getByDisplayValue("");
    const markingTextBox = result.getByDisplayValue("1,2");

    expect(mainTextBox).toBeInTheDocument();
    expect(mainTextBox).toHaveStyle("background-color: rgb(255, 255, 255)");
    expect(mainTextBox).toHaveClass("tile");
    expect(mainTextBox).not.toHaveAttribute("readOnly");

    expect(markingTextBox).toBeInTheDocument();
    expect(markingTextBox).toHaveStyle("background-color: rgb(255, 255, 255)");
    expect(markingTextBox).toHaveClass("mark");
    expect(markingTextBox).not.toHaveAttribute("readOnly");

    act(() => {
      setTileState("correct");
    });
    expect(mainTextBox).toHaveClass("correct");
    expect(mainTextBox).toHaveAttribute("readOnly");
    expect(markingTextBox).toHaveAttribute("readOnly");

    act(() => {
      setTileState("incorrect");
    });
    expect(mainTextBox).toHaveClass("incorrect");
    expect(mainTextBox).not.toHaveAttribute("readOnly");
    expect(markingTextBox).not.toHaveAttribute("readOnly");

    act(() => {
      setTileState("hint");
    });
    expect(mainTextBox).toHaveAttribute("readOnly");
    expect(markingTextBox).toHaveAttribute("readOnly");
  });

  test("Should be able to change mainTextBox and markingTextBox with a number between 1 - 9", async () => {
    const { setTileValue, setMarkings, result } = setupTile({
      row: 1,
      column: 1,
      value: 0,
      color: "white",
      markings: "1,2",
      state: "incorrect",
    });

    const mainTextBox = result.getByDisplayValue("");
    const markingTextBox = result.getByDisplayValue("1,2");

    fireEvent.change(mainTextBox, { target: { value: "1" } });
    fireEvent.change(markingTextBox, { target: { value: "" } });

    await waitFor(() => {
      expect(mainTextBox).toHaveValue("1");
      expect(mainTextBox).toBeValid();
      expect(mainTextBox).not.toHaveClass("incorrect");
      expect(markingTextBox).toHaveValue("");
      expect(markingTextBox).toBeValid();

      expect(setTileValue).toHaveBeenCalledWith(1, 1, 1);
      expect(setMarkings).toHaveBeenCalledWith(1, 1, "");
    });

    vi.clearAllMocks();
    fireEvent.change(mainTextBox, { target: { value: "9" } });
    await waitFor(() => {
      expect(mainTextBox).toHaveValue("9");
      expect(mainTextBox).toBeValid();

      expect(setTileValue).toHaveBeenCalledWith(1, 1, 9);
    });
  });

  test("Should not be able to change mainTextBox and markingTextBox with a number outside of 1 - 9", async () => {
    const { setTileValue, setMarkings, result } = setupTile({
      row: 1,
      column: 1,
      value: 0,
      color: "white",
      markings: "1,2",
      state: "",
    });

    const mainTextBox = result.getByDisplayValue("");
    const markingTextBox = result.getByDisplayValue("1,2");

    fireEvent.change(mainTextBox, { target: { value: "0" } });
    fireEvent.change(markingTextBox, { target: { value: "" } });

    await waitFor(() => {
      expect(mainTextBox).toHaveValue("");
      expect(mainTextBox).toBeInvalid();
      expect(markingTextBox).toHaveValue("");
      expect(markingTextBox).toBeValid();

      expect(setTileValue).not.toHaveBeenCalled();
      expect(setMarkings).toHaveBeenCalledWith(1, 1, "");
    });

    vi.clearAllMocks();
    fireEvent.change(mainTextBox, { target: { value: "10" } });
    await waitFor(() => {
      expect(mainTextBox).toHaveValue("");
      expect(mainTextBox).toBeInvalid();

      expect(setTileValue).not.toHaveBeenCalled();
    });
  });

  test("MainTextBox and markingTextBox should be readOnly when initialized with hint state", () => {
    const { result } = setupTile({
      row: 1,
      column: 1,
      value: 5,
      color: "white",
      markings: "1,2",
      state: "hint",
    });

    const mainTextBox = result.getByDisplayValue("5");
    const markingTextBox = result.getByDisplayValue("1,2");

    expect(mainTextBox).toBeInTheDocument();
    expect(mainTextBox).toHaveStyle("background-color: rgb(255, 255, 255)");
    expect(mainTextBox).toHaveClass("tile");
    expect(mainTextBox).toHaveAttribute("readOnly");

    expect(markingTextBox).toBeInTheDocument();
    expect(markingTextBox).toHaveStyle("background-color: rgb(255, 255, 255)");
    expect(markingTextBox).toHaveClass("mark");
    expect(markingTextBox).toHaveAttribute("readOnly");
  });
});
