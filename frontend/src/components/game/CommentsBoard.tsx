import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import useSession from "../../hooks/useAuth";
import { useAppSelector } from "../../reduxStore";
import { selectUserName } from "../../session/sessionSlice";
import styles from "../../comments.module.css";
import Skeleton from "react-loading-skeleton";
import TimeAgo from "../TimeAgo";
import Button from "../Button";
import { useRef } from "react";

interface Props {
  gameName: string;
}

interface CommentDto {
  game: string;
  player: string;
  text: string;
  createdOn: string;
}

type GetCommentsResponseDataType = CommentDto[];

const COMMENT_URL = "/api/comment";

const comment10SizeArray = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];

function RatingBoard({ gameName }: Props) {
  const queryClient = useQueryClient();
  const { fetcherFactory } = useSession();
  const userName = useAppSelector(selectUserName);
  const commentRef = useRef<HTMLTextAreaElement>(null);

  const { data, isError } = useQuery(["comments", gameName], () =>
    fetcherFactory<GetCommentsResponseDataType>({
      path: COMMENT_URL + "/" + gameName,
      config: {
        method: "GET",
      },
    })
  );

  const sendComment = useMutation({
    mutationFn: (text: string) =>
      fetcherFactory({
        path: COMMENT_URL,
        config: {
          method: "POST",
          body: JSON.stringify({
            game: gameName,
            text: text,
            player: userName,
          }),
          headers: {
            "Content-Type": "application/json",
          },
        },
      }),
    onSuccess() {
      queryClient.invalidateQueries(["comments", gameName]);
    },
  });

  const comment = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (sendComment.isLoading) return;
    if (commentRef.current?.value) sendComment.mutate(commentRef.current.value);
  };

  return (
    <>
      {isError ? (
        <h2>Failed to load comments</h2>
      ) : (
        <>
          <h2>Comments: </h2>
          {userName && (
            <form onSubmit={comment} className="w-50 mx-auto">
              <div className="row">
                <div className="col text-end">
                  <textarea
                    ref={commentRef}
                    cols={30}
                    rows={4}
                    placeholder="Write your comment here"
                  ></textarea>
                </div>
                <div className="col text-center my-auto">
                  <Button
                    isLoading={sendComment.isLoading}
                    text="Add comment"
                    disabled={sendComment.isLoading || !data}
                  ></Button>
                </div>
              </div>
            </form>
          )}
          <div className="container w-50">
            {data ? (
              <>
                {data.map((commentDto, index) => (
                  <div
                    className={"row mt-2 " + styles.mediaComment}
                    key={"comment" + gameName + index}
                  >
                    <div className="col text-start">
                      <h5>{commentDto.player}</h5>
                    </div>
                    <div className="col text-end">
                      <TimeAgo timestamp={commentDto.createdOn} />
                    </div>
                    <p className="text-start">{commentDto.text}</p>
                  </div>
                ))}
              </>
            ) : (
              <>
                {comment10SizeArray.map((index) => (
                  <div
                    className={"row mt-2 " + styles.mediaComment}
                    key={"comment" + gameName + index}
                  >
                    <div className="col text-start">
                      <h5>
                        <Skeleton borderRadius={10} width={"70%"} />
                      </h5>
                    </div>
                    <div className="col text-end">
                      <Skeleton borderRadius={10} width={"70%"} />
                    </div>
                    <p className="text-start">
                      <Skeleton
                        borderRadius={10}
                        width={"100%"}
                        height={getRndInteger(1, 4) == 1 ? "5em" : "3em"}
                        style={{ fontSize: "30px", lineHeight: "1.5" }}
                      />
                    </p>
                  </div>
                ))}
              </>
            )}
          </div>
        </>
      )}
    </>
  );
}

export default RatingBoard;

function getRndInteger(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
