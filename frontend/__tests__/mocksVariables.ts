import { BoardDataType, HintResponseDataType, NewGameResponseDataType } from "../src/components/game/gameSlice";
import { CSRFHeaderName } from "../src/session/sessionSlice";

export const newGameResponseData: NewGameResponseDataType = {
    gameState: "Playing",
    tiles: [
      [
        { row: 0, column: 0, displayedValue: 1, groupId: 0 },
        { row: 0, column: 1, displayedValue: 0, groupId: 0 },
      ],
      [
        { row: 1, column: 0, displayedValue: 0, groupId: 1 },
        { row: 1, column: 1, displayedValue: 1, groupId: 1 },
      ],
    ],
    score: 0,
    maxPossibleScore: 100,
  };
  
  export const gameHint: HintResponseDataType = {
    hints: [{ row: 0, column: 1, displayedValue: 2, groupId: 0 }],
    gameState: "Playing",
    score: 20,
    maxPossibleScore: 100,
  };

export const boardData: BoardDataType = {
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
  
  export const loggedInSession = {
    userName: "Samuel",
    CSRFToken: "token",
    CSRFHeaderName: CSRFHeaderName,
    timeToLive: Date.now() + 60 * 60 * 1000,
  };