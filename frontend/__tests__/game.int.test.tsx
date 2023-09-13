import { fireEvent, waitFor } from "@testing-library/react";
import Game from "../src/components/game/Game";

import { rest } from "msw";
import { setupServer } from "msw/node";
import { TileDto } from "../src/components/game/gameSlice";
import { renderWithProviders } from "./test_utils";
import {
  boardData,
  gameHint,
  loggedInSession,
  newGameResponseData,
} from "./mocksVariables";
import { DOMAIN_URL } from "../src/session/SessionProvider";

vi.mock("redux-persist", async (importOriginal) => {
  const real = await importOriginal<typeof import("redux-persist")>();
  return {
    ...real,
    persistReducer: vi.fn().mockImplementation((_config, reducers) => {
      return reducers;
    }),
  };
});
// If you also want to mock persistGate
//vi.mock('redux-persist/integration/react', () => ({   PersistGate: (props: any) => props.children, }));

const handlers = [
  rest.get(DOMAIN_URL + "/api/game/new", async (_req, res, ctx) => {
    //await new Promise((r) => setTimeout(r, 50));
    return res(ctx.json(newGameResponseData));
  }),
  rest.get(DOMAIN_URL + "/api/hint", async (_req, res, ctx) => {
    return res(ctx.json(gameHint));
  }),
  rest.post(DOMAIN_URL + "/api/check", async (req, res, ctx) => {
    const reqData = await req.json<TileDto[]>();
    const gameCheck = {
      correct: reqData.filter((dto) => dto.displayedValue == 2),
      incorrect: reqData.filter((dto) => dto.displayedValue == 1),
      gameState: "Playing",
      score: 20,
      maxPossibleScore: 100,
    };

    if (gameCheck.correct.length == 2) {
      gameCheck.gameState = "Solved";
    } else if (gameCheck.incorrect.length == 2) {
      gameCheck.gameState = "Failed";
      gameCheck.maxPossibleScore = 20;
    }
    return res(ctx.json(gameCheck));
  }),
  rest.get(DOMAIN_URL + "/api/score/JigsawSudoku", async (_req, res, ctx) => {
    return res(
      ctx.json([
        {
          game: "JigsawSudoku",
          player: "Name",
          points: 100,
          createdOn: new Date().toISOString(),
        },
      ])
    );
  }),
  rest.get(DOMAIN_URL + "/api/rating/JigsawSudoku", async (_req, res, ctx) => {
    return res(ctx.json(3));
  }),
  rest.post(DOMAIN_URL + "/api/rating", async (_req, res, ctx) => {
    return res(ctx.status(200));
  }),
  rest.get(DOMAIN_URL + "/api/comment/JigsawSudoku", async (_req, res, ctx) => {
    return res(
      ctx.json([
        {
          game: "JigsawSudoku",
          player: "Name",
          text: "Comment text",
          createdOn: new Date().toISOString(),
        },
      ])
    );
  }),
  rest.post(DOMAIN_URL + "/api/comment", async (_req, res, ctx) => {
    return res(ctx.status(200));
  }),
];

const server = setupServer(...handlers);
beforeAll(() => server.listen());
// if you need to add a handler after calling setupServer for some specific test
// this will remove that handler for the rest of them
// (which is important for test isolation):
afterEach(() => {
  server.resetHandlers();
});
afterAll(() => server.close());

describe("Game component integration", () => {
  test("Should render buttons and fetch field when no field in state", async () => {
    const { queryByText, getAllByRole, findByText, getByRole, container } =
      renderWithProviders(<Game />, {
        preloadedState: {
          game: { boardSize: 2 },
        },
        rq: true,
        session: true,
      });

    const newGameButton = getByRole("button", { name: "New game" });
    const hintButton = getByRole("button", { name: "Hint!" });
    const checkButton = getByRole("button", { name: "Check" });
    let gameStateText = queryByText("Game state:", { exact: false });

    expect(newGameButton).toBeInTheDocument();
    expect(hintButton).toBeInTheDocument();
    expect(checkButton).toBeInTheDocument();
    expect(gameStateText).not.toBeInTheDocument();

    const skeletons = container.querySelectorAll(".react-loading-skeleton");
    expect(skeletons).toHaveLength(2 * 2 + 2 + 10 * 3 + 2 + 10 * 3);

    await waitFor(() => {
      expect(newGameButton).toBeDisabled();
    });

    gameStateText = await findByText("Game state:", { exact: false });

    await waitFor(() => {
      expect(newGameButton).toBeEnabled();
      expect(gameStateText).toBeInTheDocument();

      const tds = getAllByRole("cell");

      const tile00 = tds[0].querySelectorAll("input");
      const tile01 = tds[1].querySelectorAll("input");
      const tile10 = tds[2].querySelectorAll("input");
      const tile11 = tds[3].querySelectorAll("input");

      // first input is markingTextBox, second is mainTextBox
      expect(tile00[0].value).toBe("");
      expect(tile00[0]).toHaveAttribute("readOnly");
      expect(tile00[1].value).toBe("1");
      expect(tile00[1]).toHaveAttribute("readOnly");

      expect(tile01[0].value).toBe("");
      expect(tile01[0]).not.toHaveAttribute("readOnly");
      expect(tile01[1].value).toBe("");
      expect(tile01[1]).not.toHaveAttribute("readOnly");

      expect(tile10[0].value).toBe("");
      expect(tile10[0]).not.toHaveAttribute("readOnly");
      expect(tile10[1].value).toBe("");
      expect(tile10[1]).not.toHaveAttribute("readOnly");

      expect(tile11[0].value).toBe("");
      expect(tile11[0]).toHaveAttribute("readOnly");
      expect(tile11[1].value).toBe("1");
      expect(tile11[1]).toHaveAttribute("readOnly");
    });
  });

  test("Should be able to change tile data and then fetch new game when game is already loaded", async () => {
    const { getByText, getAllByRole, getByRole, container } =
      renderWithProviders(<Game />, {
        preloadedState: {
          game: { boardSize: 2, board: boardData },
        },
        rq: true,
        session: true,
      });

    const newGameButton = getByRole("button", { name: "New game" });
    let gameStateText = getByText("Game state:", { exact: false });

    expect(newGameButton).toBeInTheDocument();
    expect(newGameButton).toBeEnabled();
    expect(gameStateText).toBeInTheDocument();

    let skeletons = container.querySelectorAll(".react-loading-skeleton");
    expect(skeletons).toHaveLength(0 + 10 * 3 + 2 + 10 * 3);

    const tds = getAllByRole("cell");

    const tile00 = tds[0].querySelectorAll("input");
    const tile01 = tds[1].querySelectorAll("input");
    const tile10 = tds[2].querySelectorAll("input");
    const tile11 = tds[3].querySelectorAll("input");

    fireEvent.change(tile01[1], { target: { value: "2" } }); // mainTextBox
    fireEvent.change(tile10[0], { target: { value: "2," } }); // markingTextBox

    await waitFor(() => {
      // first input is markingTextBox, second is mainTextBox
      expect(tile00[0].value).toBe("");
      expect(tile00[0]).toHaveAttribute("readOnly");
      expect(tile00[1].value).toBe("1");
      expect(tile00[1]).toHaveAttribute("readOnly");

      expect(tile01[0].value).toBe("");
      expect(tile01[0]).not.toHaveAttribute("readOnly");
      expect(tile01[1].value).toBe("2");
      expect(tile01[1]).not.toHaveAttribute("readOnly");

      expect(tile10[0].value).toBe("2,");
      expect(tile10[0]).not.toHaveAttribute("readOnly");
      expect(tile10[1].value).toBe("");
      expect(tile10[1]).not.toHaveAttribute("readOnly");

      expect(tile11[0].value).toBe("");
      expect(tile11[0]).toHaveAttribute("readOnly");
      expect(tile11[1].value).toBe("1");
      expect(tile11[1]).toHaveAttribute("readOnly");
    });

    fireEvent.click(newGameButton);

    await waitFor(() => {
      expect(newGameButton).toBeDisabled();
      /*skeletons = container.querySelectorAll(".react-loading-skeleton");
      expect(skeletons).toHaveLength(2 * 2 + 2);*/
    });

    await waitFor(() => {
      // first input is markingTextBox, second is mainTextBox
      expect(tile00[0].value).toBe("");
      expect(tile00[0]).toHaveAttribute("readOnly");
      expect(tile00[1].value).toBe("1");
      expect(tile00[1]).toHaveAttribute("readOnly");

      expect(tile01[0].value).toBe("");
      expect(tile01[0]).not.toHaveAttribute("readOnly");
      expect(tile01[1].value).toBe("");
      expect(tile01[1]).not.toHaveAttribute("readOnly");

      expect(tile10[0].value).toBe("");
      expect(tile10[0]).not.toHaveAttribute("readOnly");
      expect(tile10[1].value).toBe("");
      expect(tile10[1]).not.toHaveAttribute("readOnly");

      expect(tile11[0].value).toBe("");
      expect(tile11[0]).toHaveAttribute("readOnly");
      expect(tile11[1].value).toBe("1");
      expect(tile11[1]).toHaveAttribute("readOnly");
    });
  });

  test("Board data is persisted for subsequent rerenders", async () => {
    let { getByText, unmount, getByRole, store } = renderWithProviders(
      <Game />,
      {
        preloadedState: {
          game: { boardSize: 2 },
        },
        rq: true,
        session: true,
      }
    );

    await waitFor(() => {
      const newGameButton = getByRole("button", { name: "New game" });
      let gameStateText = getByText("Game state:", { exact: false });

      expect(newGameButton).toBeInTheDocument();
      expect(gameStateText).toBeInTheDocument();
    });

    unmount();
    let screen = renderWithProviders(<Game />, {
      store,
      rq: true,
      session: true,
    });

    const newGameButton = screen.getByRole("button", { name: "New game" });
    const gameStateText = screen.queryByText("Game state:", { exact: false });

    expect(newGameButton).toBeEnabled();
    expect(gameStateText).toBeInTheDocument();

    const tds = screen.getAllByRole("cell");

    const tile00 = tds[0].querySelectorAll("input");
    const tile01 = tds[1].querySelectorAll("input");
    const tile10 = tds[2].querySelectorAll("input");
    const tile11 = tds[3].querySelectorAll("input");

    // first input is markingTextBox, second is mainTextBox
    expect(tile00[0].value).toBe("");
    expect(tile00[0]).toHaveAttribute("readOnly");
    expect(tile00[1].value).toBe("1");
    expect(tile00[1]).toHaveAttribute("readOnly");

    expect(tile01[0].value).toBe("");
    expect(tile01[0]).not.toHaveAttribute("readOnly");
    expect(tile01[1].value).toBe("");
    expect(tile01[1]).not.toHaveAttribute("readOnly");

    expect(tile10[0].value).toBe("");
    expect(tile10[0]).not.toHaveAttribute("readOnly");
    expect(tile10[1].value).toBe("");
    expect(tile10[1]).not.toHaveAttribute("readOnly");

    expect(tile11[0].value).toBe("");
    expect(tile11[0]).toHaveAttribute("readOnly");
    expect(tile11[1].value).toBe("1");
    expect(tile11[1]).toHaveAttribute("readOnly");
  });

  test("Hint! button should uncover some new tiles", async () => {
    let { getByText, getAllByRole, getByRole } = renderWithProviders(<Game />, {
      preloadedState: {
        game: { boardSize: 2, board: boardData },
      },
      rq: true,
      session: true,
    });

    const hintButton = getByRole("button", { name: "Hint!" });

    const tds = getAllByRole("cell");

    const tile00 = tds[0].querySelectorAll("input");
    const tile01 = tds[1].querySelectorAll("input");
    const tile10 = tds[2].querySelectorAll("input");
    const tile11 = tds[3].querySelectorAll("input");

    fireEvent.click(hintButton);

    await waitFor(() => {
      // first input is markingTextBox, second is mainTextBox
      expect(tile00[0].value).toBe("");
      expect(tile00[0]).toHaveAttribute("readOnly");
      expect(tile00[1].value).toBe("1");
      expect(tile00[1]).toHaveAttribute("readOnly");

      expect(tile01[0].value).toBe("");
      expect(tile01[0]).toHaveAttribute("readOnly");
      expect(tile01[1].value).toBe("2");
      expect(tile01[1]).toHaveAttribute("readOnly");

      expect(tile10[0].value).toBe("");
      expect(tile10[0]).not.toHaveAttribute("readOnly");
      expect(tile10[1].value).toBe("");
      expect(tile10[1]).not.toHaveAttribute("readOnly");

      expect(tile11[0].value).toBe("");
      expect(tile11[0]).toHaveAttribute("readOnly");
      expect(tile11[1].value).toBe("1");
      expect(tile11[1]).toHaveAttribute("readOnly");

      const progressBar = getByText("20");
      expect(progressBar).toBeInTheDocument();
    });
  });

  test("Check button should mark correct and incorrect tiles", async () => {
    let { getByText, getAllByRole, getByRole } = renderWithProviders(<Game />, {
      preloadedState: {
        game: { boardSize: 2, board: boardData },
      },
      rq: true,
      session: true,
    });

    const checkButton = getByRole("button", { name: "Check" });

    const tds = getAllByRole("cell");

    const tile00 = tds[0].querySelectorAll("input");
    const tile01 = tds[1].querySelectorAll("input");
    const tile10 = tds[2].querySelectorAll("input");
    const tile11 = tds[3].querySelectorAll("input");

    fireEvent.change(tile01[1], { target: { value: "1" } }); // mainTextBox
    fireEvent.change(tile10[0], { target: { value: "2," } }); // markingTextBox
    fireEvent.change(tile10[1], { target: { value: "2" } }); // mainTextBox

    await waitFor(() => {
      expect(tile01[1].value).toBe("1");
      expect(tile10[0].value).toBe("2,");
      expect(tile10[1].value).toBe("2");
    });

    fireEvent.click(checkButton);

    await waitFor(() => {
      // first input is markingTextBox, second is mainTextBox
      expect(tile00[0].value).toBe("");
      expect(tile00[0]).toHaveAttribute("readOnly");
      expect(tile00[1].value).toBe("1");
      expect(tile00[1]).toHaveAttribute("readOnly");

      expect(tile01[0].value).toBe("");
      expect(tile01[0]).not.toHaveAttribute("readOnly");
      expect(tile01[1].value).toBe("1");
      expect(tile01[1]).not.toHaveAttribute("readOnly");

      expect(tile10[0].value).toBe("");
      expect(tile10[0]).toHaveAttribute("readOnly");
      expect(tile10[1].value).toBe("2");
      expect(tile10[1]).toHaveAttribute("readOnly");

      expect(tile11[0].value).toBe("");
      expect(tile11[0]).toHaveAttribute("readOnly");
      expect(tile11[1].value).toBe("1");
      expect(tile11[1]).toHaveAttribute("readOnly");

      const progressBar = getByText("20");
      expect(progressBar).toBeInTheDocument();
    });
  });

  test("When game is won or lost, check and hint buttons should be disabled", async () => {
    let { getByText, getAllByRole, getByRole } = renderWithProviders(<Game />, {
      preloadedState: {
        game: { boardSize: 2, board: boardData },
      },
      rq: true,
      session: true,
    });

    const checkButton = getByRole("button", { name: "Check" });

    const tds = getAllByRole("cell");

    const tile00 = tds[0].querySelectorAll("input");
    const tile01 = tds[1].querySelectorAll("input");
    const tile10 = tds[2].querySelectorAll("input");
    const tile11 = tds[3].querySelectorAll("input");

    fireEvent.change(tile01[1], { target: { value: "2" } }); // mainTextBox
    fireEvent.change(tile10[1], { target: { value: "2" } }); // mainTextBox

    await waitFor(() => {
      expect(tile01[1].value).toBe("2");
      expect(tile10[1].value).toBe("2");
    });

    fireEvent.click(checkButton);

    await waitFor(() => {
      // first input is markingTextBox, second is mainTextBox
      expect(tile00[0].value).toBe("");
      expect(tile00[0]).toHaveAttribute("readOnly");
      expect(tile00[1].value).toBe("1");
      expect(tile00[1]).toHaveAttribute("readOnly");

      expect(tile01[0].value).toBe("");
      expect(tile01[0]).toHaveAttribute("readOnly");
      expect(tile01[1].value).toBe("2");
      expect(tile01[1]).toHaveAttribute("readOnly");

      expect(tile10[0].value).toBe("");
      expect(tile10[0]).toHaveAttribute("readOnly");
      expect(tile10[1].value).toBe("2");
      expect(tile10[1]).toHaveAttribute("readOnly");

      expect(tile11[0].value).toBe("");
      expect(tile11[0]).toHaveAttribute("readOnly");
      expect(tile11[1].value).toBe("1");
      expect(tile11[1]).toHaveAttribute("readOnly");

      const progressBar = getByText("20");
      expect(progressBar).toBeInTheDocument();
      //const gameStateText = getByText("Game state:", { exact: false });
      const hintButton = getByRole("button", { name: "Hint!" });
      expect(checkButton).toBeDisabled();
      expect(hintButton).toBeDisabled();
    });
  });
});

describe("Game leaderboard, comments and rating components integration", () => {
  test("When game is won and user is logged in, score should be added", async () => {
    let { getAllByText, getAllByRole, getByRole } = renderWithProviders(
      <Game />,
      {
        preloadedState: {
          game: { boardSize: 2, board: boardData },
          session: loggedInSession,
        },
        rq: true,
        session: true,
      }
    );

    const checkButton = getByRole("button", { name: "Check" });

    const tds = getAllByRole("cell");

    const tile00 = tds[0].querySelectorAll("input");
    const tile01 = tds[1].querySelectorAll("input");
    const tile10 = tds[2].querySelectorAll("input");
    const tile11 = tds[3].querySelectorAll("input");

    fireEvent.change(tile01[1], { target: { value: "2" } }); // mainTextBox
    fireEvent.change(tile10[1], { target: { value: "2" } }); // mainTextBox

    await waitFor(() => {
      expect(tile01[1].value).toBe("2");
      expect(tile10[1].value).toBe("2");
    });

    fireEvent.click(checkButton);

    await waitFor(() => {
      // first input is markingTextBox, second is mainTextBox
      expect(tile00[0].value).toBe("");
      expect(tile00[0]).toHaveAttribute("readOnly");
      expect(tile00[1].value).toBe("1");
      expect(tile00[1]).toHaveAttribute("readOnly");

      expect(tile01[0].value).toBe("");
      expect(tile01[0]).toHaveAttribute("readOnly");
      expect(tile01[1].value).toBe("2");
      expect(tile01[1]).toHaveAttribute("readOnly");

      expect(tile10[0].value).toBe("");
      expect(tile10[0]).toHaveAttribute("readOnly");
      expect(tile10[1].value).toBe("2");
      expect(tile10[1]).toHaveAttribute("readOnly");

      expect(tile11[0].value).toBe("");
      expect(tile11[0]).toHaveAttribute("readOnly");
      expect(tile11[1].value).toBe("1");
      expect(tile11[1]).toHaveAttribute("readOnly");

      const progressBar = getAllByText("20")[0];
      expect(progressBar).toBeInTheDocument();
      const hintButton = getByRole("button", { name: "Hint!" });
      expect(checkButton).toBeDisabled();
      expect(hintButton).toBeDisabled();

      const rows = getAllByRole("row"); // skip first 2 board rows and header row
      const alreadyExistingScore = rows[3];
      const alreadyExistingScoreCells =
        alreadyExistingScore.querySelectorAll("td");
      expect(alreadyExistingScoreCells[0].textContent).toBe("1");
      expect(alreadyExistingScoreCells[1].textContent).toBe("Name");
      expect(alreadyExistingScoreCells[2].textContent).toBe("100");
      expect(alreadyExistingScoreCells[3].textContent).toBe(
        "less than a minute ago"
      );

      const addedScore = rows[4];
      const addedScoreCells = addedScore.querySelectorAll("td");
      expect(addedScoreCells[0].textContent).toBe("2");
      expect(addedScoreCells[1].textContent).toBe("Samuel");
      expect(addedScoreCells[2].textContent).toBe("20");
      expect(addedScoreCells[3].textContent).toBe("less than a minute ago");
    });
  });

  test("When user rates game, average rating should change", async () => {
    let { findAllByRole } = renderWithProviders(<Game />, {
      preloadedState: {
        game: { boardSize: 2, board: boardData },
        session: loggedInSession,
      },
      rq: true,
      session: true,
    });

    const stars = await findAllByRole("radio");

    expect(stars[0]).toHaveAttribute("readOnly");
    expect(stars[1]).toHaveAttribute("readOnly");
    expect(stars[2]).toHaveAttribute("readOnly");
    expect(stars[2] as HTMLInputElement).toBeChecked(); // 3 stars average rating
    expect(stars[3]).toHaveAttribute("readOnly");
    expect(stars[4]).toHaveAttribute("readOnly");
    expect(stars[5]).not.toHaveAttribute("readOnly");
    expect(stars[6]).not.toHaveAttribute("readOnly");
    expect(stars[7]).not.toHaveAttribute("readOnly");
    expect(stars[8]).not.toHaveAttribute("readOnly");
    expect(stars[9]).not.toHaveAttribute("readOnly");

    server.use(
      rest.get(DOMAIN_URL + "/api/rating/JigsawSudoku", (_req, res, ctx) => {
        return res(ctx.json(5));
      })
    );

    fireEvent.click(stars[5]); // give 5 stars

    await waitFor(() => {
      expect(stars[0] as HTMLInputElement).toBeChecked(); // average rating is now 5
    });
  });

  test("When user adds a comment, comment board should change", async () => {
    let { getByPlaceholderText, getByRole, getByText, findByText } =
      renderWithProviders(<Game />, {
        preloadedState: {
          game: { boardSize: 2, board: boardData },
          session: loggedInSession,
        },
        rq: true,
        session: true,
      });

    const addCommentTextBox = getByPlaceholderText("Write your comment here");
    const addCommentButton = getByRole("button", { name: "Add comment" });

    const firstComment = await findByText("Comment text");
    expect(firstComment).toBeInTheDocument();

    server.use(
      rest.get(DOMAIN_URL + "/api/comment/JigsawSudoku", (_req, res, ctx) => {
        return res(
          ctx.json([
            {
              game: "JigsawSudoku",
              player: "Name",
              text: "Comment text",
              createdOn: new Date().toISOString(),
            },
            {
              game: "JigsawSudoku",
              player: "Name",
              text: "New comment",
              createdOn: new Date().toISOString(),
            },
          ])
        );
      })
    );

    fireEvent.change(addCommentTextBox, { target: { value: "New comment" } });
    fireEvent.click(addCommentButton);

    await waitFor(() => {
      expect(firstComment).toBeInTheDocument();
      const addedComment = getByText("New comment");
      expect(addedComment).toBeInTheDocument();
    });
  });

  test("When user is not logged in, he should not be able to add a comment", async () => {
    let { queryByPlaceholderText, queryByRole } = renderWithProviders(
      <Game />,
      {
        preloadedState: {
          game: { boardSize: 2, board: boardData },
        },
        rq: true,
        session: true,
      }
    );

    const addCommentTextBox = queryByPlaceholderText("Write your comment here");
    const addCommentButton = queryByRole("button", { name: "Add comment" });

    expect(addCommentTextBox).not.toBeInTheDocument();
    expect(addCommentButton).not.toBeInTheDocument();
  });
});
