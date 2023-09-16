import { useEffect, useRef } from "react";
import Field from "./Field";
import Skeleton from "react-loading-skeleton";
import { useMutation, useQuery } from "@tanstack/react-query";
import {
  checkBoardState,
  CheckResponseDataType,
  gameStarted,
  HintResponseDataType,
  NewGameResponseDataType,
  processHints,
  selectBoard,
  selectBoardSize,
  selectMaxScore,
  selectScore,
  setMarkings,
  setValue,
  TileDto,
  TileValueType,
} from "./gameSlice";
import { useAppDispatch, useAppSelector } from "../../reduxStore";
import useSession from "../../hooks/useAuth";
import Button from "../Button";
import { selectUserName } from "../../session/sessionSlice";
import ProgressBar from "@ramonak/react-progress-bar";
import ScoreBoard, { ScoreBoardRefType } from "./ScoreBoard";
import RatingBoard from "./RatingBoard";
import CommentsBoard from "./CommentsBoard";

const GAME_NAME = "JigsawSudoku";

const HOME_URL = "/api/game";
const HINT_URL = "/api/hint";
const CHECK_URL = "/api/check";

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

function Game() {
  const dispatch = useAppDispatch();
  const board = useAppSelector(selectBoard);
  const fieldSize = useAppSelector(selectBoardSize);
  const score = useAppSelector(selectScore);
  const maxScore = useAppSelector(selectMaxScore);
  const userName = useAppSelector(selectUserName);
  const { fetcherFactory } = useSession();

  const scoreBoardRef = useRef<ScoreBoardRefType>(null);

  const newGameQuery = useQuery(
    ["newGame"],
    () =>
      fetcherFactory<NewGameResponseDataType>({
        path: HOME_URL,
        config: {
          method: "GET",
        },
        query_params: "/new",
        dataManipulator: (data: NewGameResponseDataType) => {
          dispatch(gameStarted(data, fieldSize));
          return data;
        },
      }),
    {
      cacheTime: 0,
      enabled: false,
      staleTime: 0,
    }
  );

  const hintMutation = useMutation({
    mutationFn: () =>
      fetcherFactory<HintResponseDataType>({
        path: HINT_URL,
        config: {
          method: "GET",
        },
      }),
    onSuccess: (data: HintResponseDataType) => {
      dispatch(processHints(data));
    },
  });

  const checkMutation = useMutation({
    mutationFn: (tilesToCheck: TileDto[]) =>
      fetcherFactory<CheckResponseDataType>({
        path: CHECK_URL,
        config: {
          method: "POST",
          body: JSON.stringify(tilesToCheck),
          headers: {
            "Content-Type": "application/json",
          },
        },
      }),
    onSuccess: (data: CheckResponseDataType) => {
      dispatch(checkBoardState(data));
      if (data.gameState == "Solved" && userName) {
        scoreBoardRef.current?.saveScoreOptimistic(data.score);
      }
    },
  });

  const getFieldData = (e: React.MouseEvent<HTMLButtonElement> | null) => {
    e?.preventDefault();
    if (newGameQuery.isFetching) return;
    //await new Promise((r) => setTimeout(r, 2000));
    newGameQuery.refetch();
  };
  const getHints = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    if (hintMutation.isLoading) return;
    hintMutation.mutate();
  };
  const checkBoard = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    if (checkMutation.isLoading) return;
    if (board) {
      const tilesToCheck = board.tiles
        .flat()
        .filter(
          (tile) => tile.info.state == "" && tile.tileDto.displayedValue != 0
        )
        .map((tile) => tile.tileDto);
      if (tilesToCheck.length != 0) checkMutation.mutate(tilesToCheck);
    }
  };

  useEffect(() => {
    // In StrictMode, this runs twice. If we used mutation, which is more logical, it would run twice, which is not desirable.
    if (!board && !newGameQuery.isFetching) getFieldData(null);
  }, [userName]);

  const setTileValue = (row: number, column: number, value: TileValueType) => {
    dispatch(setValue({ row, column, value }));
  };
  const setTileMarkings = (row: number, column: number, markings: string) => {
    dispatch(setMarkings({ row, column, markings }));
  };

  return (
    <>
      <Button
        isLoading={newGameQuery.isFetching}
        disabled={
          newGameQuery.isFetching ||
          hintMutation.isLoading ||
          checkMutation.isLoading
        }
        onClick={getFieldData}
        text="New game"
      ></Button>
      <h2>
        {board != undefined ? (
          <span
            className={
              board.gameState == "Solved"
                ? "correct"
                : board.gameState == "Failed"
                ? "incorrect"
                : ""
            }
          >
            Game state: {board.gameState}
          </span>
        ) : (
          <Skeleton borderRadius={10} width={"35%"} />
        )}
      </h2>
      <Field
        data={board}
        colors={colors}
        fieldSize={fieldSize}
        setTileValue={setTileValue}
        setTileMarkings={setTileMarkings}
      ></Field>
      <div className="mx-auto w-50" style={{ lineHeight: 1 }}>
        {board != undefined ? (
          <ProgressBar completed={String(score)} maxCompleted={maxScore} />
        ) : (
          <Skeleton borderRadius={10} height={"20px"} />
        )}
      </div>
      <Button
        isLoading={checkMutation.isLoading}
        disabled={
          newGameQuery.isFetching ||
          hintMutation.isLoading ||
          checkMutation.isLoading ||
          board?.gameState != "Playing"
        }
        onClick={checkBoard}
        text="Check"
      ></Button>
      <Button
        isLoading={hintMutation.isLoading}
        disabled={
          newGameQuery.isFetching ||
          hintMutation.isLoading ||
          checkMutation.isLoading ||
          board?.gameState != "Playing"
        }
        onClick={getHints}
        text="Hint!"
      ></Button>
      <ScoreBoard ref={scoreBoardRef} gameName={GAME_NAME} />
      <RatingBoard gameName={GAME_NAME} />
      <CommentsBoard gameName={GAME_NAME} />
    </>
  );
}

export default Game;
