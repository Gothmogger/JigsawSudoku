import { PayloadAction, createSlice } from "@reduxjs/toolkit";
import { RootState } from "../../reduxStore";

export type TileValueType = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9;
export type TileStateType = "" | "correct" | "incorrect" | "hint";
export interface TileDto {
  row: number;
  column: number;
  displayedValue: TileValueType;
  groupId: number;
}
interface TileInfoType {
  markings: string;
  state: TileStateType;
}
interface TileType {
  tileDto: TileDto;
  info: TileInfoType;
}
export interface BoardDataType {
  gameState: string;
  tiles: TileType[][];
}
interface GameStateResponseType {
  score: number;
  maxPossibleScore: number;
  gameState: string;
}
export interface NewGameResponseDataType extends GameStateResponseType {
  tiles: TileDto[][];
}
export interface HintResponseDataType extends GameStateResponseType {
  hints: TileDto[];
}
export interface CheckResponseDataType extends GameStateResponseType {
  correct: TileDto[];
  incorrect: TileDto[];
}


type StateType = {board? : BoardDataType, boardSize: number, score?: number, maxScore?: number}
export const initialState : StateType = {
    board: undefined,
    boardSize: 9,
    score: undefined,
    maxScore: undefined
};
// Even if redux-persist is used, the initial state is assigned at the beginning
// To call action inside action: mySlice.caseReducers.endGame(state)
const gameSlice = createSlice({
    name: 'game',
    initialState,
    reducers: {
        gameStarted: {
            reducer(state, action: PayloadAction<{boardSize : number, board: BoardDataType, maxScore: number}>) {
                const { boardSize, board, maxScore } = action.payload
                state.boardSize = boardSize;
                state.board = board;
                state.score = 0;
                state.maxScore = maxScore;
            },
            prepare(data: NewGameResponseDataType, boardSize : number) {
                let tiles = new Array<Array<TileType>>(boardSize);
                for (var i = 0; i < boardSize; i++) {
                  tiles[i] = new Array<TileType>(boardSize);
                  for (var j = 0; j < boardSize; j++) {
                    let tileDto = data.tiles[i][j];
                    tiles[i][j] = {
                      tileDto: tileDto,
                      info: { markings: "", state: tileDto.displayedValue == 0 ? "" : "hint" },
                    };
                  }
                }
      
                const newBoardData = { gameState: data.gameState, tiles: tiles };
                
                return {payload: {boardSize: boardSize, board: newBoardData, maxScore: data.maxPossibleScore}};
            }
        },
        processHints(state, action: PayloadAction<HintResponseDataType>) {
            const hintsResponse = action.payload
            if (state.board) {
              state.score = hintsResponse.score;
              state.maxScore = hintsResponse.maxPossibleScore;
              state.board.gameState = hintsResponse.gameState;

              hintsResponse.hints.forEach(hint => {
                const { row, column } = hint;
                if (state.board) {
                  state.board.tiles[row][column].info.state = "hint";
                  state.board.tiles[row][column].info.markings = "";
                  state.board.tiles[row][column].tileDto.displayedValue = hint.displayedValue;
                }
              });
            }
        },
        checkBoardState(state, action: PayloadAction<CheckResponseDataType>) {
            const checkResponse = action.payload
            if (state.board) {
              state.score = checkResponse.score;
              state.maxScore = checkResponse.maxPossibleScore;
              state.board.gameState = checkResponse.gameState;

              checkResponse.correct.forEach(correct => {
                const { row, column } = correct;
                if (state.board) {
                  state.board.tiles[row][column].info.state = "correct";
                  state.board.tiles[row][column].info.markings = "";
                }
              });
              checkResponse.incorrect.forEach(incorrect => {
                const { row, column } = incorrect;
                if (state.board) {
                  state.board.tiles[row][column].info.state = "incorrect";
                }
              });
            }
        },
        setValue(state, action: PayloadAction<{row : number, column: number, value: TileValueType}>) {
            const { value, row, column } = action.payload
            if (state.board) {
                state.board.tiles[row][column].tileDto.displayedValue = value;
                state.board.tiles[row][column].info.state = "";
            }
        },
        setMarkings(state, action: PayloadAction<{row : number, column: number, markings: string}>) {
            const { markings, row, column } = action.payload
            if (state.board) {
                state.board.tiles[row][column].info.markings = markings;
            }
        },
    }
})

export const selectBoardSize = (state: RootState) => state.game.boardSize;
export const selectBoard = (state: RootState) => state.game.board;
export const selectScore = (state: RootState) => state.game.score;
export const selectMaxScore = (state: RootState) => state.game.maxScore;

export const { gameStarted, setValue, setMarkings, processHints, checkBoardState } = gameSlice.actions

export default gameSlice.reducer