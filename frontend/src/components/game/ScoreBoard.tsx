import { useQuery, useQueryClient } from "@tanstack/react-query";
import useSession from "../../hooks/useAuth";
import TimeAgo from "../TimeAgo";
import { forwardRef, useImperativeHandle } from "react";
import { useAppSelector } from "../../reduxStore";
import { selectUserName } from "../../session/sessionSlice";
import React from "react";
import Skeleton from "react-loading-skeleton";

interface Props {
  gameName: string;
}
export type ScoreBoardRefType = {
  saveScoreOptimistic: (points: number) => void;
};

interface ScoreDto {
  game: string;
  player: string;
  points: number;
  createdOn: string;
}

type GetScoresResponseDataType = ScoreDto[];

const GET_SCORE_URL = "/api/score";

function ScoreBoard(
  { gameName }: Props,
  ref: React.ForwardedRef<ScoreBoardRefType>
) {
  const queryClient = useQueryClient();
  const { fetcherFactory } = useSession();
  const userName = useAppSelector(selectUserName);

  const { data, isError } = useQuery(["topScores", gameName], () =>
    fetcherFactory<GetScoresResponseDataType>({
      path: GET_SCORE_URL + "/" + gameName,
      config: {
        method: "GET",
      },
    })
  );

  useImperativeHandle(
    ref,
    () => ({
      saveScoreOptimistic: (points: number) => {
        queryClient.setQueryData<GetScoresResponseDataType>(
          ["topScores", gameName],
          (oldData) => {
            const newScoreEntry = {
              game: gameName,
              player: userName,
              points: points,
              createdOn: new Date().toISOString(),
            };
            const newData = oldData
              ? [...oldData, newScoreEntry]
              : [newScoreEntry];

            newData.sort((scoreDtoA, scoreDtoB) => {
              const pointsComparsion = scoreDtoB.points - scoreDtoA.points;
              if (pointsComparsion == 0) {
                return (
                  Date.parse(scoreDtoA.createdOn) -
                  Date.parse(scoreDtoB.createdOn)
                );
              }
              return pointsComparsion;
            });

            return newData;
          }
        );
      },
    }),
    []
  );

  const scoresToRender = data ? [...data] : [];
  const scorePlaceholder = {
    game: gameName,
    player: "",
    points: -1,
    createdOn: "",
  };
  for (let i = scoresToRender.length; i < 10; i++) {
    scoresToRender.push(scorePlaceholder);
  }

  return (
    <>
      {isError ? (
        <h2>Failed to load top scores</h2>
      ) : (
        <table className="table table-success table-striped table-dark w-50 mx-auto">
          <thead>
            <tr>
              <th colSpan={4} scope="col">
                Top 10 Scores
              </th>
            </tr>
          </thead>
          <tbody>
            {scoresToRender.map((scoreDto, index) => (
              <tr key={"Top10ScoreBoard" + gameName + index}>
                <td scope="row" style={{ width: "10%" }}>
                  {index + 1}
                </td>
                {data ? (
                  <>
                    <td style={{ width: "40%" }}>{scoreDto.player}</td>
                    <td style={{ width: "20%" }}>
                      {scoreDto.points != -1 ? scoreDto.points : ""}
                    </td>
                    <td style={{ width: "30%" }}>
                      {scoreDto.points != -1 ? (
                        <TimeAgo timestamp={scoreDto.createdOn} />
                      ) : (
                        ""
                      )}
                    </td>
                  </>
                ) : (
                  <>
                    <td style={{ width: "40%" }}>
                      <Skeleton borderRadius={10} />
                    </td>
                    <td style={{ width: "20%" }}>
                      <Skeleton borderRadius={10} />
                    </td>
                    <td style={{ width: "30%" }}>
                      <Skeleton borderRadius={10} />
                    </td>
                  </>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}

export default React.memo(forwardRef<ScoreBoardRefType, Props>(ScoreBoard));
