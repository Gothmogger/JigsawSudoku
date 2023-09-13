/*import { useState, useEffect, useRef } from "react";
import useSession from "./useAuth";


  //Switched to using React Query.


const url = "http://localhost:8080";

export const useFetch = (path: string, config: any) => {
  const [data, setData] = useState(null);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState("");

  const { extendSession, invalidateSession } = useSession();

  useEffect(() => {
    let controller = new AbortController();
    setIsPending(true);
    fetch(url + path, {
      signal: controller.signal,
      credentials: "include",
      mode: "cors",
      ...config,
    })
      .then((res) => {
        if (res.status >= 500) {
          extendSession();
          throw new Error("Server error.");
        } else if (res.status == 401) {
          let newCsrfToken = res.headers.get("X-CSRF-TOKEN") as string;
          invalidateSession(res);
          controller.abort();
          controller = new AbortController();
          if (config.headers)
            config.headers = {
              ...config.headers,
              "X-CSRF-TOKEN": newCsrfToken,
            };
          fetch(url + path, {
            signal: controller.signal,
            credentials: "include",
            mode: "cors",
            ...config,
          })
            .then((res) => {
              extendSession();
              if (res.status >= 500) throw new Error("Server error.");
              return res.json();
            })
            .then((data) => {
              setIsPending(false);
              setData(data);
              setError("");
            })
            .catch((err) => {
              setIsPending(false);
              setError(err.message);
            });
        }
        extendSession();
        return res.json();
      })
      .then((data) => {
        setIsPending(false);
        setData(data);
        setError("");
      })
      .catch((err) => {
        setIsPending(false);
        setError(err.message);
      });

    return () => {
      controller.abort();
    };
  }, [path]);

  return { data, isPending, error };
};

export const useFormFetch = (path: string, useData: boolean) => {
  const [data, setData] = useState<any>(null);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState("");

  const { extendSession, invalidateSession } = useSession();

  const submitFetchFunction = useRef<
    (
      config: any,
      query_params?: string,
      responseHandler?: (res: Response) => void,
      dataHandler?: (data: any) => void
    ) => void
  >(() => {});

  useEffect(() => {
    let controller = new AbortController();
    submitFetchFunction.current = (
      config: any,
      query_params?: string,
      responseHandler?: (res: Response) => void,
      dataHandler?: (data: any) => void
    ) => {
      setIsPending(true);
      fetch(url + path + (query_params ? query_params : ""), {
        signal: controller.signal,
        credentials: "include",
        mode: "cors",
        ...config,
      })
        .then((res) => {
          if (res.status >= 500) {
            extendSession();
            throw new Error("Server error.");
          } else if (res.status == 401) {
            let newCsrfToken = res.headers.get("X-CSRF-TOKEN") as string;
            invalidateSession(res);
            controller.abort();
            controller = new AbortController();
            if (config.headers)
              config.headers = {
                ...config.headers,
                "X-CSRF-TOKEN": newCsrfToken,
              };
            fetch(url + path + (query_params ? query_params : ""), {
              signal: controller.signal,
              credentials: "include",
              mode: "cors",
              ...config,
            })
              .then((res) => {
                extendSession();
                if (res.status >= 500) throw new Error("Server error.");
                if (!useData) setIsPending(false);
                if (responseHandler) responseHandler(res);
                if (useData) return res.json();
              })
              .then((data?) => {
                if (useData) {
                  setIsPending(false);
                  setData(data);
                  if (dataHandler) dataHandler(data);
                }
              })
              .catch((err) => {
                setIsPending(false);
                setError(err.message);
              });
          } else {
            extendSession();
            if (!useData) setIsPending(false);
            if (responseHandler) responseHandler(res);
            if (useData) return res.json();
          }
        })
        .then((data?) => {
          if (useData) {
            setIsPending(false);
            setData(data);
            if (dataHandler) dataHandler(data);
          }
        })
        .catch((err) => {
          // Keby tu bol custom errorHandler, treba differencovat medzi abort errorom a network errorom
          setIsPending(false);
          setError(err.message);
        });
    };

    return () => {
      controller.abort();
    };
  }, [path]);

  return {
    data,
    setData,
    isPending,
    error,
    setError,
    submitFetch: submitFetchFunction,
  };
};

/*let match = document.cookie.match(
            new RegExp("(?:^| )XSRF-TOKEN=([^;]+)")
          );*/
