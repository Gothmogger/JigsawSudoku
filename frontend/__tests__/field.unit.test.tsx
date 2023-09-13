import { render } from "@testing-library/react";
import Field from "../src/components/game/Field";
import { BoardDataType } from "../src/components/game/gameSlice";

const mocks = vi.hoisted(() => {
  return {
    tile: vi.fn(({ children }) => (
      <div id="Tile" /*data-testid="Tile"*/>{children}</div>
    )),
    skeleton: vi.fn(({ children }) => (
      <div id="Skeleton" /*data-testid="Skeleton"*/>{children}</div>
    )),
  };
});
vi.mock("../src/components/game/Tile", async (importOriginal) => {
  const mod = await importOriginal<
    typeof import("../src/components/game/Tile")
  >();
  return {
    ...mod,
    default: mocks.tile,
  };
});
vi.mock("react-loading-skeleton", async (importOriginal) => {
  const mod = await importOriginal<typeof import("react-loading-skeleton")>();
  return {
    ...mod,
    default: mocks.skeleton,
  };
});

describe("Field component", () => {
  const colors = [
    "tomato",
    "dodgerblue",
    "mediumseagreen",
    "slateblue",
    "crimson",
    "darkturquoise",
    "greenyellow",
    "yellow",
    "violet",
  ];

  test("Should render a Tile component from data", () => {
    const boardData: BoardDataType = {
      gameState: "Playing",
      tiles: [
        [
          {
            tileDto: { row: 0, column: 0, displayedValue: 1, groupId: 0 },
            info: { markings: "", state: "hint" },
          },
          {
            tileDto: { row: 0, column: 1, displayedValue: 0, groupId: 0 },
            info: { markings: "", state: "" },
          },
        ],
        [
          {
            tileDto: { row: 1, column: 0, displayedValue: 0, groupId: 1 },
            info: { markings: "", state: "" },
          },
          {
            tileDto: { row: 1, column: 1, displayedValue: 1, groupId: 1 },
            info: { markings: "", state: "hint" },
          },
        ],
      ],
    };

    const setValue = vi.fn();
    const setMarkings = vi.fn();
    const { getByRole } = render(
      <Field
        data={boardData}
        colors={colors}
        fieldSize={2}
        setTileValue={setValue}
        setTileMarkings={setMarkings}
      />
    );

    const table = getByRole("table");
    const tBody = table.querySelector("tbody") as HTMLTableSectionElement;
    const tRows = tBody.querySelectorAll(
      "tr"
    ) as NodeListOf<HTMLTableRowElement>;
    const tilesInRow1 = tRows[0].querySelectorAll("td.tileTd #Tile");
    const tilesInRow2 = tRows[1].querySelectorAll("td.tileTd #Tile");

    expect(table).toBeInTheDocument();
    expect(tBody).toBeInTheDocument();
    expect(tRows).toHaveLength(2);
    expect(tilesInRow1).toHaveLength(2);
    expect(tilesInRow2).toHaveLength(2);

    expect(mocks.tile).toHaveBeenCalledTimes(4);
    const mockedTileArgs = mocks.tile.mock.calls.map((args) => args[0]);
    expect(mockedTileArgs).toEqual([
      {
        row: 0,
        column: 0,
        value: 1,
        color: "tomato",
        markings: "",
        state: "hint",
        setValue,
        setMarkings,
      },
      {
        row: 0,
        column: 1,
        value: 0,
        color: "tomato",
        markings: "",
        state: "",
        setValue,
        setMarkings,
      },
      {
        row: 1,
        column: 0,
        value: 0,
        color: "dodgerblue",
        markings: "",
        state: "",
        setValue,
        setMarkings,
      },
      {
        row: 1,
        column: 1,
        value: 1,
        color: "dodgerblue",
        markings: "",
        state: "hint",
        setValue,
        setMarkings,
      },
    ]);
  });

  test("Should render skeletons when data is not provided", () => {
    const { getByRole } = render(
      <Field
        data={undefined}
        colors={colors}
        fieldSize={2}
        setTileValue={() => {}}
        setTileMarkings={() => {}}
      />
    );

    const table = getByRole("table");
    const tBody = table.querySelector("tbody") as HTMLTableSectionElement;
    const tRows = tBody.querySelectorAll(
      "tr"
    ) as NodeListOf<HTMLTableRowElement>;
    const skeletonsInRow1 = tRows[0].querySelectorAll("td.tileTd #Skeleton");
    const skeletonsInRow2 = tRows[1].querySelectorAll("td.tileTd #Skeleton");

    expect(table).toBeInTheDocument();
    expect(tBody).toBeInTheDocument();
    expect(tRows).toHaveLength(2);
    expect(skeletonsInRow1).toHaveLength(2);
    expect(skeletonsInRow2).toHaveLength(2);

    expect(mocks.skeleton).toHaveBeenCalledTimes(4);
  });
});
