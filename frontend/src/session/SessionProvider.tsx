import { createContext } from "react";
import { useAppDispatch, useAppSelector } from "../reduxStore";
import {
  extendSession,
  invalidateSession,
  selectCSRFHeaderName,
  selectCSRFToken,
  selectUserName,
} from "./sessionSlice";
import { LOGIN_URL } from "../pages/Login";
import { REGISTER_URL } from "../pages/Register";

export const DOMAIN_URL = "http://localhost:8080";

const SessionContext = createContext<SessionContextType>(
  {} as SessionContextType
);

export const SessionProvider = ({ children }: any) => {
  const dispatch = useAppDispatch();
  const userName = useAppSelector(selectUserName);
  const CSRFToken = useAppSelector(selectCSRFToken);
  const CSRFHeaderName = useAppSelector(selectCSRFHeaderName);
  /**
   * @param {((res: Response) => void)} responseStatusHandler - Will be called with the response. Useful when taking action only depends on status code.
   * @param {(data: any) => any} dataManipulator -  Takes in data and returnes modified version. Useful when data from the server needs to be changed.
   */
  const fetcherFactory = <T,>({
    path,
    config,
    query_params,
    responseStatusHandler,
    dataManipulator,
  }: fetcherFactoryArgsType): Promise<T> => {
    return new Promise<T>(async function (resolve, reject) {
      try {
        let res = await fetch(
          DOMAIN_URL + path + (query_params ? query_params : ""),
          {
            ...config,
            credentials: "include",
            mode: "cors",
            headers:
              config.method === "POST"
                ? { ...config.headers, [CSRFHeaderName]: CSRFToken }
                : config.headers,
          }
        );
        let newCsrfToken = res.headers.get(CSRFHeaderName);
        const isTokenPresent = newCsrfToken != null; // token will be present even when session Id got changed, after login for example
        let newSessionCreated = isTokenPresent;
        if (
          (path === LOGIN_URL || path === REGISTER_URL) &&
          res.status == 200
        ) {
          newSessionCreated = false; // session ID and token was changed, but new session wasnt created
        }
        if (newSessionCreated) dispatch(invalidateSession(res));
        else if (isTokenPresent) dispatch(extendSession(res));
        else if (res.status === 401) dispatch(invalidateSession());
        else dispatch(extendSession());

        if (res.status >= 500) {
          return reject(new Error("Server error."));
        } else if (res.status === 401 && !userName) {
          // wrong CSRF token and not logged in, so resend(if session expired and u try to log in), if logged in no need to resend since it would fail
          try {
            res = await fetch(
              DOMAIN_URL + path + (query_params ? query_params : ""),
              {
                ...config,
                credentials: "include",
                mode: "cors",
                headers:
                  config.method === "POST"
                    ? { ...config.headers, [CSRFHeaderName]: newCsrfToken }
                    : config.headers,
              }
            );

            dispatch(extendSession());
            if (res.status >= 500) return reject(new Error("Server error."));

            if (responseStatusHandler) {
              try {
                responseStatusHandler(res);
              } catch (e: any) {
                return reject(e);
              }
            }
            if (dataManipulator)
              return resolve(dataManipulator(await res.json()));
            else {
              try {
                return resolve(await res.json());
              } catch (e) {
                return resolve({} as T);
              }
            }
          } catch (e: any) {
            return reject(e);
          }
        } else {
          if (responseStatusHandler) {
            try {
              responseStatusHandler(res);
            } catch (e: any) {
              return reject(e);
            }
          }
          if (dataManipulator)
            return resolve(dataManipulator(await res.json()));
          else {
            try {
              return resolve(await res.json());
            } catch (e) {
              return resolve({} as T);
            }
          }
        }
      } catch (e: any) {
        return reject(e);
      }
    });
  };

  return (
    <SessionContext.Provider
      value={{
        fetcherFactory,
      }}
    >
      {children}
    </SessionContext.Provider>
  );
};

interface fetcherFactoryArgsType {
  path: string;
  config: any;
  query_params?: string;
  responseStatusHandler?: (res: Response) => void;
  dataManipulator?: (data: any) => any;
}
export type SessionContextType = {
  fetcherFactory: <T>(args: fetcherFactoryArgsType) => Promise<T>;
};

export default SessionContext;
