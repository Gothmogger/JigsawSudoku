import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import useSession from "../../hooks/useAuth";
import { useAppSelector } from "../../reduxStore";
import { selectUserName } from "../../session/sessionSlice";
import styles from "../../rating.module.css";
import Skeleton from "react-loading-skeleton";
import { useEffect } from "react";

interface Props {
  gameName: string;
}
/*
interface RatingDto {
  game: string;
  player: string;
  stars: number;
  createdOn: string;
}*/

type GetRatingResponseDataType = number;

const RATING_URL = "/api/rating";

function RatingBoard({ gameName }: Props) {
  const queryClient = useQueryClient();
  const { fetcherFactory } = useSession();
  const userName = useAppSelector(selectUserName);

  const { data: averageRatingData, isError: averageRatingIsError } = useQuery(
    ["rating", gameName],
    () =>
      fetcherFactory<GetRatingResponseDataType>({
        path: RATING_URL + "/" + gameName,
        config: {
          method: "GET",
        },
      })
  );

  const { data: playerRatingData, refetch: refetchPlayerRating } = useQuery(
    ["userRating", gameName],
    () =>
      fetcherFactory<GetRatingResponseDataType>({
        path: RATING_URL + "/" + gameName + "/" + userName,
        config: {
          method: "GET",
        },
      }),
    {
      enabled: false,
    }
  );

  useEffect(() => {
    if (!userName) return;

    refetchPlayerRating();
  }, [userName]);

  const setRating = useMutation({
    mutationFn: (stars: number) =>
      fetcherFactory({
        path: RATING_URL,
        config: {
          method: "POST",
          body: JSON.stringify({
            game: gameName,
            stars: stars,
            player: userName,
          }),
          headers: {
            "Content-Type": "application/json",
          },
        },
      }),
    onSuccess() {
      queryClient.invalidateQueries(["rating", gameName]);
      refetchPlayerRating();
    },
  });

  const rate = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (setRating.isLoading) return;
    setRating.mutate(parseInt(event.target.value));
  };

  return (
    <>
      {averageRatingIsError ? (
        <h2>Failed to load game ratings</h2>
      ) : averageRatingData ? (
        <>
          <div>Average game rating:</div>
          <div className={styles.rating}>
            <input
              type="radio"
              name="rating"
              value="5"
              id="5"
              checked={averageRatingData == 5}
              readOnly
            />
            <label htmlFor="5">☆</label>
            <input
              type="radio"
              name="rating"
              value="4"
              id="4"
              checked={averageRatingData == 4}
              readOnly
            />
            <label htmlFor="4">☆</label>
            <input
              type="radio"
              name="rating"
              value="3"
              id="3"
              checked={averageRatingData == 3}
              readOnly
            />
            <label htmlFor="3">☆</label>
            <input
              type="radio"
              name="rating"
              value="2"
              id="2"
              checked={averageRatingData == 2}
              readOnly
            />
            <label htmlFor="2">☆</label>
            <input
              type="radio"
              name="rating"
              value="1"
              id="1"
              checked={averageRatingData == 1}
              readOnly
            />
            <label htmlFor="1">☆</label>
          </div>
          {userName && (
            <>
              <div>Your rating:</div>
              <div className={styles.rating + " " + styles.interactive}>
                <input
                  type="radio"
                  name="UserRating"
                  value="5"
                  id="User5"
                  checked={playerRatingData == 5}
                  onChange={rate}
                />
                <label htmlFor="User5">☆</label>
                <input
                  type="radio"
                  name="UserRating"
                  value="4"
                  id="User4"
                  checked={playerRatingData == 4}
                  onChange={rate}
                />
                <label htmlFor="User4">☆</label>
                <input
                  type="radio"
                  name="UserRating"
                  value="3"
                  id="User3"
                  checked={playerRatingData == 3}
                  onChange={rate}
                />
                <label htmlFor="User3">☆</label>
                <input
                  type="radio"
                  name="UserRating"
                  value="2"
                  id="User2"
                  checked={playerRatingData == 2}
                  onChange={rate}
                />
                <label htmlFor="User2">☆</label>
                <input
                  type="radio"
                  name="UserRating"
                  value="1"
                  id="User1"
                  checked={playerRatingData == 1}
                  onChange={rate}
                />
                <label htmlFor="User1">☆</label>
              </div>
            </>
          )}
        </>
      ) : (
        <>
          <Skeleton borderRadius={10} width={"20%"} />
          <Skeleton
            borderRadius={10}
            width={"5em"}
            style={{ fontSize: "30px", lineHeight: "1.5" }}
          />
          {userName && (
            <>
              <Skeleton borderRadius={10} width={"20%"} />
              <Skeleton
                borderRadius={10}
                width={"5em"}
                style={{ fontSize: "30px", lineHeight: "1.5" }}
              />
            </>
          )}
        </>
      )}
    </>
  );
}

export default RatingBoard;
