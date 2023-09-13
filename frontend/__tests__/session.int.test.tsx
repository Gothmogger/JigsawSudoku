import { fireEvent, waitFor } from "@testing-library/react";

import { rest } from "msw";
import { setupServer } from "msw/node";
import { gameStarted } from "../src/components/game/gameSlice";
import { renderWithProviders } from "./test_utils";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import RequireAnonym from "../src/components/RequireAnonym";
import RequireAuth from "../src/components/RequireAuth";
import App from "../src/App";
import { CSRFHeaderName } from "../src/session/sessionSlice";
import {
  boardData,
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

let currentCSRFToken = crypto.randomUUID();
const handlers = [
  rest.post(DOMAIN_URL + "/api/login", async (req, res, ctx) => {
    const reqCSRF = req.headers.get(CSRFHeaderName);
    if (reqCSRF != currentCSRFToken) {
      return res(
        ctx.status(401),
        ctx.cookie("JSESSIONID", crypto.randomUUID()),
        ctx.set(CSRFHeaderName, currentCSRFToken)
      );
    }
    const reqData = await req.text();
    if (reqData.includes("correctPass")) {
      currentCSRFToken = crypto.randomUUID();
      return res(
        ctx.status(200),
        ctx.cookie("JSESSIONID", crypto.randomUUID()),
        ctx.set(CSRFHeaderName, currentCSRFToken)
      );
    } else return res(ctx.status(400));
  }),
  rest.post(DOMAIN_URL + "/api/register", async (req, res, ctx) => {
    const reqCSRF = req.headers.get(CSRFHeaderName);
    if (reqCSRF != currentCSRFToken) {
      return res(
        ctx.status(401),
        ctx.cookie("JSESSIONID", crypto.randomUUID()),
        ctx.set(CSRFHeaderName, currentCSRFToken)
      );
    }
    const reqData = await req.text();
    if (reqData.includes("Samuel2")) {
      currentCSRFToken = crypto.randomUUID();
      return res(
        ctx.status(200),
        ctx.cookie("JSESSIONID", crypto.randomUUID()),
        ctx.set(CSRFHeaderName, currentCSRFToken)
      );
    } else return res(ctx.status(400));
  }),
  rest.post(DOMAIN_URL + "/api/logout", async (req, res, ctx) => {
    const reqCSRF = req.headers.get(CSRFHeaderName);
    if (reqCSRF != currentCSRFToken) {
      return res(
        ctx.status(401),
        ctx.cookie("JSESSIONID", crypto.randomUUID()),
        ctx.set(CSRFHeaderName, currentCSRFToken)
      );
    }
    return res(ctx.status(200));
  }),
  rest.get(DOMAIN_URL + "/api/game/new", async (_req, res, ctx) => {
    // session doesnt get created here, to test logging in and registering without created session
    return res(ctx.json({ ...newGameResponseData, gameState: "Failed" }));
  }),
];

const server = setupServer(...handlers);
beforeAll(() => server.listen());
afterEach(() => {
  server.resetHandlers();
});
afterAll(() => server.close());

describe("Session, CSRF and authentication integration", () => {
  test("If no or invalid session, login should create new one", async () => {
    let {
      getByText,
      getByRole,
      getByLabelText,
      findByText,
      findByRole,
      store,
    } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { board: boardData, boardSize: 2 },
        },
        rq: true,
        session: true,
        browser: true,
      }
    );

    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const logInNav = getByRole("link", { name: "Login" });
    fireEvent.click(logInNav); // container is rendered

    const logInButton = getByRole("button", { name: "Sign in" }); // we are on login page
    const userNameTextBox = getByRole("textbox", { name: "Username:" });
    const passwordTextBox = getByLabelText("Password:"); // Password fields dont have role

    fireEvent.change(userNameTextBox, { target: { value: "Samuel" } });
    fireEvent.change(passwordTextBox, { target: { value: "1234" } });
    fireEvent.click(logInButton);

    const errorMessage = await findByText("Invalid username or password!");
    expect(errorMessage).toBeInTheDocument();
    expect(store.getState().session.CSRFToken).toBe(currentCSRFToken);
    let oldCSRFToken = currentCSRFToken;
    expect(store.getState().session.userName).toBe("");
    expect(store.getState().game.board).toBe(undefined); // no session so store gets reset.
    store.dispatch(gameStarted(newGameResponseData, 2)); // fill store with data

    fireEvent.change(passwordTextBox, { target: { value: "correctPass" } });
    await waitFor(() => {
      expect(errorMessage).toHaveClass("offscreen");
    });

    fireEvent.click(logInButton);

    newGameButton = await findByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We were redirected to home page

    const hiText = getByText("Hi, Samuel!"); // navbar should change
    expect(hiText).toBeInTheDocument();
    expect(store.getState().session.CSRFToken).not.toBe(oldCSRFToken); // token was changed after login
    expect(store.getState().session.CSRFToken).toBe(currentCSRFToken);
    expect(store.getState().session.userName).toBe("Samuel");
    expect(store.getState().game.board).toStrictEqual(boardData); // with a valid session, loggin in does not reset store
  });

  test("If no or invalid session, register shouldlogin and create new one", async () => {
    let { getByRole, getByLabelText, findByText, store } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { board: boardData, boardSize: 2 },
        },
        rq: true,
        session: true,
        browser: true,
      }
    );

    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const registerNav = getByRole("link", { name: "Register" }); // Container is rendered
    fireEvent.click(registerNav);

    const registerButton = getByRole("button", { name: "Sign up" }); // we are on register page

    const userNameTextBox = getByRole("textbox", { name: "Username:" });
    const passwordTextBox = getByLabelText("Password:"); // Password fields dont have role
    const confirmPasswordTextBox = getByLabelText("Confirm Password:");

    fireEvent.change(userNameTextBox, { target: { value: "Samuel" } });
    fireEvent.change(passwordTextBox, { target: { value: "correctPass" } });
    fireEvent.change(confirmPasswordTextBox, {
      target: { value: "correctPass" },
    });
    fireEvent.click(registerButton);

    const errorMessage = await findByText("The name is already in use.");
    expect(errorMessage).toBeInTheDocument();
    expect(store.getState().session.CSRFToken).toBe(currentCSRFToken);
    let oldCSRFToken = currentCSRFToken;
    expect(store.getState().session.userName).toBe("");
    expect(store.getState().game.board).toBe(undefined); // no session so store gets reset.
    store.dispatch(gameStarted(newGameResponseData, 2)); // fill store with data

    fireEvent.change(userNameTextBox, { target: { value: "Samuel2" } });
    await waitFor(() => {
      expect(errorMessage).toHaveClass("offscreen");
    });

    fireEvent.click(registerButton);
    // We were redirected to home page

    const hiText = await findByText("Hi, Samuel2!"); // navbar should change
    expect(hiText).toBeInTheDocument();
    expect(store.getState().session.CSRFToken).not.toBe(oldCSRFToken); // token was changed after login
    expect(store.getState().session.CSRFToken).toBe(currentCSRFToken);
    expect(store.getState().session.userName).toBe("Samuel2");
    expect(store.getState().game.board).toStrictEqual(boardData); // with a valid session, loggin in does not reset store
  });

  test("After a request(getting newGame on visiting home) which uses a session, but doesnt chech CSRF(GET), token should be stored", async () => {
    server.use(
      // Runtime request handler override for the "GET /api/game/new".
      rest.get(DOMAIN_URL + "/api/game/new", (_req, res, ctx) => {
        return res(
          ctx.json(newGameResponseData),
          ctx.cookie("JSESSIONID", crypto.randomUUID()),
          ctx.set(CSRFHeaderName, currentCSRFToken)
        );
      })
    );

    let { queryByText, getByRole, findByText, store } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { boardSize: 2 },
        },
        rq: true,
        session: true,
        browser: true,
      }
    );

    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const navbarText = queryByText("Jigsaw Sudoku"); // navbar is rendered
    expect(navbarText).toBeInTheDocument();

    const gameState = await findByText("Game state:", { exact: false });
    expect(gameState).toBeInTheDocument();

    expect(store.getState().session.CSRFToken).toBe(currentCSRFToken);
    expect(store.getState().session.userName).toBe("");
    expect(store.getState().game.board).toStrictEqual(boardData);
  });

  test("After a request(getting newGame on visiting home) which uses a session, but doesnt chech CSRF(GET), token should be stored and old, invalid session overriden", async () => {
    server.use(
      // Runtime request handler override for the "GET /api/game/new".
      rest.get(DOMAIN_URL + "/api/game/new", (_req, res, ctx) => {
        return res(
          ctx.json({ ...newGameResponseData, gameState: "Failed" }),
          ctx.cookie("JSESSIONID", crypto.randomUUID()),
          ctx.set(CSRFHeaderName, currentCSRFToken)
        );
      })
    );

    let { getByRole, findByText, getByText, store } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { boardSize: 2, board: boardData },
          session: loggedInSession,
        },
        rq: true,
        session: true,
        browser: true,
      }
    );

    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const hiText = getByText("Hi, Samuel!"); // navbar is rendered
    expect(hiText).toBeInTheDocument();

    fireEvent.click(newGameButton);

    const gameState = await findByText("Game state: Failed");
    expect(gameState).toBeInTheDocument();

    expect(store.getState().session.CSRFToken).toBe(currentCSRFToken);
    expect(store.getState().session.userName).toBe("");
    expect(store.getState().game.board).toStrictEqual({
      ...boardData,
      gameState: "Failed",
    });
  });
});

describe("Login, Register and logout pages integration", () => {
  test("Links on login and register page should point to register and login, respectively.", async () => {
    let { queryByText, getByText, getByRole, findByText } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { board: boardData, boardSize: 2 },
        },
        rq: true,
        session: true,
        browser: true,
      }
    );
    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const logInNav = getByRole("link", { name: "Login" });
    const registerNav = queryByText("Register");
    const homeNav = getByRole("link", { name: "Home" });
    const navbarText = queryByText("Jigsaw Sudoku");

    expect(logInNav).toBeInTheDocument();
    expect(registerNav).toBeInTheDocument();
    expect(homeNav).toBeInTheDocument();
    expect(navbarText).toBeInTheDocument(); // Container is rendered

    fireEvent.click(logInNav);

    let loginPageHeader = getByText("Sign In"); // We are on login page
    expect(loginPageHeader).toBeInTheDocument();
    const dontHaveAccountLink = getByRole("link", { name: "Sign Up" });
    fireEvent.click(dontHaveAccountLink);

    const registerPageHeader = getByRole("heading", { name: "Register" }); // We are on register page
    expect(registerPageHeader).toBeInTheDocument();
    const alreadyHaveAccountLink = getByRole("link", { name: "Sign In" });
    fireEvent.click(alreadyHaveAccountLink);

    loginPageHeader = await findByText("Sign In");
    expect(loginPageHeader).toBeInTheDocument(); // back on login page

    fireEvent.click(homeNav); // Navigation browser history is shared among tests
  });

  test("Should be able to log in", async () => {
    let {
      queryByText,
      getByText,
      getByRole,
      getByLabelText,
      findByText,
      findByRole,
    } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { board: boardData, boardSize: 2 },
        },
        rq: true,
        session: true,
        browser: true,
      }
    );

    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const logInNav = getByRole("link", { name: "Login" });
    const registerNav = queryByText("Register");
    const homeNav = queryByText("Home");
    const navbarText = queryByText("Jigsaw Sudoku");

    expect(logInNav).toBeInTheDocument();
    expect(registerNav).toBeInTheDocument();
    expect(homeNav).toBeInTheDocument();
    expect(navbarText).toBeInTheDocument(); // Container is rendered

    fireEvent.click(logInNav);

    const loginPageHeader = getByText("Sign In"); // We are on login page
    expect(loginPageHeader).toBeInTheDocument();
    const logInButton = getByRole("button", { name: "Sign in" });
    expect(logInButton).toBeDisabled();

    const userNameTextBox = getByRole("textbox", { name: "Username:" });
    const passwordTextBox = getByLabelText("Password:"); // Password fields dont have role

    fireEvent.change(userNameTextBox, { target: { value: "Samuel" } });
    fireEvent.change(passwordTextBox, { target: { value: "1234" } });

    fireEvent.click(logInButton);

    const errorMessage = await findByText("Invalid username or password!");
    expect(errorMessage).toBeInTheDocument();

    fireEvent.change(passwordTextBox, { target: { value: "correctPass" } });
    await waitFor(() => {
      expect(errorMessage).toHaveClass("offscreen");
    });

    fireEvent.click(logInButton);

    newGameButton = await findByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We were redirected to home page

    const hiText = getByText("Hi, Samuel!"); // navbar should change
    expect(hiText).toBeInTheDocument();
    expect(logInNav).not.toBeInTheDocument();
    expect(registerNav).not.toBeInTheDocument();
    expect(homeNav).toBeInTheDocument();
    expect(navbarText).toBeInTheDocument();
    const logOutButton = getByRole("button", { name: "Logout" });
    expect(logOutButton).toBeInTheDocument();
  });

  test("Register should log in", async () => {
    let {
      queryByText,
      getByText,
      getByRole,
      getByLabelText,
      findByText,
      findByRole,
    } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { board: boardData, boardSize: 2 },
        },
        rq: true,
        session: true,
        browser: true,
      }
    );

    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const logInNav = getByRole("link", { name: "Login" });
    const registerNav = getByRole("link", { name: "Register" });
    const homeNav = queryByText("Home");
    const navbarText = queryByText("Jigsaw Sudoku");

    expect(logInNav).toBeInTheDocument();
    expect(registerNav).toBeInTheDocument();
    expect(homeNav).toBeInTheDocument();
    expect(navbarText).toBeInTheDocument(); // Container is rendered

    fireEvent.click(registerNav);

    const registerPageHeader = getByRole("heading", { name: "Register" }); // We are on register page
    expect(registerPageHeader).toBeInTheDocument();
    const registerButton = getByRole("button", { name: "Sign up" });
    expect(registerButton).toBeDisabled();

    const userNameTextBox = getByRole("textbox", { name: "Username:" });
    const passwordTextBox = getByLabelText("Password:"); // Password fields dont have role
    const confirmPasswordTextBox = getByLabelText("Confirm Password:");

    const usernameValidityMessage = getByText(
      "Must be 3 to 16 characters long."
    );
    expect(usernameValidityMessage).toHaveClass("offscreen");
    const passwordValidityMessage = getByText("3 to 16 characters.");
    expect(passwordValidityMessage).toHaveClass("offscreen");
    const confirmPasswordValidityMessage = getByText(
      "Must match the first password input field."
    );
    expect(confirmPasswordValidityMessage).toHaveClass("offscreen");

    fireEvent.change(userNameTextBox, { target: { value: "Sa" } });
    expect(usernameValidityMessage).not.toHaveClass("offscreen");
    fireEvent.change(userNameTextBox, { target: { value: "Samuel" } });
    expect(usernameValidityMessage).toHaveClass("offscreen");
    fireEvent.change(passwordTextBox, { target: { value: "c" } });
    expect(passwordValidityMessage).not.toHaveClass("offscreen");
    fireEvent.change(passwordTextBox, { target: { value: "correctPass" } });
    expect(passwordValidityMessage).toHaveClass("offscreen");
    fireEvent.change(confirmPasswordTextBox, { target: { value: "c" } });
    expect(confirmPasswordValidityMessage).not.toHaveClass("offscreen");
    fireEvent.change(confirmPasswordTextBox, {
      target: { value: "correctPass" },
    });
    expect(confirmPasswordValidityMessage).toHaveClass("offscreen");

    fireEvent.click(registerButton);

    const errorMessage = await findByText("The name is already in use.");
    expect(errorMessage).toBeInTheDocument();

    fireEvent.change(userNameTextBox, { target: { value: "Samuel2" } });
    await waitFor(() => {
      expect(errorMessage).toHaveClass("offscreen");
    });

    fireEvent.click(registerButton);

    newGameButton = await findByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We were redirected to home page

    const hiText = getByText("Hi, Samuel2!"); // navbar should change
    expect(hiText).toBeInTheDocument();
    expect(logInNav).not.toBeInTheDocument();
    expect(registerNav).not.toBeInTheDocument();
    expect(homeNav).toBeInTheDocument();
    expect(navbarText).toBeInTheDocument();
    const logOutButton = getByRole("button", { name: "Logout" });
    expect(logOutButton).toBeInTheDocument();
  });

  test("Clicking on logout in layout shold logout user and invalidate session(and reset game and session state)", async () => {
    let { queryByText, getByText, getByRole, store } = renderWithProviders(
      <App />, // App starts off at home page
      {
        preloadedState: {
          game: { board: boardData, boardSize: 2 },
          session: loggedInSession,
        },
        rq: true,
        session: true,
        browser: true,
      }
    );
    let newGameButton = getByRole("button", { name: "New game" });
    expect(newGameButton).toBeInTheDocument(); // We are on home page

    const homeNav = getByRole("link", { name: "Home" }); // navbar
    const navbarText = queryByText("Jigsaw Sudoku");

    const hiText = getByText("Hi, Samuel!");
    expect(hiText).toBeInTheDocument();
    expect(homeNav).toBeInTheDocument();
    expect(navbarText).toBeInTheDocument();
    const logOutButton = getByRole("button", { name: "Logout" });
    expect(logOutButton).toBeInTheDocument();

    fireEvent.click(logOutButton);

    await waitFor(() => {
      expect(store.getState().game.board).not.toEqual(boardData); // store was reset after logout
      expect(store.getState().session.userName).toBe("");
      expect(store.getState().session.CSRFToken).toBe("");
    });
  });
});

describe("Integration test of browser components RequireAnonym and RequireAuth", () => {
  test("Routes with element RequireAnonym should redirect to home if user is logged in", () => {
    let { queryByText } = renderWithProviders(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/" element={<div>Home</div>} />
          <Route element={<RequireAnonym />}>
            <Route path="/login" element={<div>Login</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
      {
        preloadedState: {
          session: loggedInSession,
        },
        rq: true,
        session: true,
      }
    );

    const login = queryByText("Login");
    expect(login).not.toBeInTheDocument();

    const home = queryByText("Home");
    expect(home).toBeInTheDocument();
  });

  test("Routes with element RequireAuth should redirect to home if user is not logged in and tries to access protected path", () => {
    let { queryByText } = renderWithProviders(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route element={<RequireAuth />}>
            <Route path="/" element={<div>Home</div>} />
          </Route>
          <Route path="/login" element={<div>Login</div>} />
        </Routes>
      </MemoryRouter>,
      {
        rq: true,
        session: true,
      }
    );

    const login = queryByText("Login");
    expect(login).toBeInTheDocument();

    const home = queryByText("Home");
    expect(home).not.toBeInTheDocument();
  });
});
