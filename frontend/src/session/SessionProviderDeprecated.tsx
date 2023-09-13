import { createContext, useState, useEffect } from "react";

/*
Switched to a new Session Provider, where I dealing with local storage to redux 
*/

const url = "http://localhost:8080";

interface authType {
  userName: string;
  CSRFToken: string;
  CSRFHeaderName: string;
}

const SessionContext = createContext<SessionContextType>(
  {} as SessionContextType
);

const sessionTimeOutInMinutes = 15;

function initializeSession(): authType {
  let oldSessionExpired = true;
  let newAuth: authType = { userName: "", CSRFToken: "", CSRFHeaderName: "" };
  let sessionInStorage = localStorage.getItem("session");
  if (sessionInStorage) {
    const sessionInStorageJson = JSON.parse(sessionInStorage);
    if (Date.now() > sessionInStorageJson.timeToLive) {
      const [token, headerName] = loadCsrfTokenFromMeta();
      newAuth.CSRFToken = token;
      newAuth.CSRFHeaderName = headerName;
    } else {
      newAuth = sessionInStorageJson.auth;

      oldSessionExpired = false;
    }
  } else {
    const [token, headerName] = loadCsrfTokenFromMeta();
    newAuth.CSRFToken = token;
    newAuth.CSRFHeaderName = headerName;
  }

  if (oldSessionExpired) localStorage.clear();

  persistSession(newAuth);

  return newAuth;
}

export const SessionProvider = ({ children }: any) => {
  const [auth, setAuth] = useState<authType>(() => initializeSession());

  useEffect(() => {
    if (!auth.userName) localStorage.removeItem("session"); //localStorage.clear();

    persistSession(auth);
  }, [auth]);

  const logIn = (userName: string, res: Response) => {
    const newCSRFToken = res.headers.get("X-CSRF-TOKEN") as string;
    setAuth((oldAuth) => ({
      ...oldAuth,
      userName: userName,
      CSRFToken: newCSRFToken,
    }));
  };

  const logOut = () => {
    setAuth((oldAuth) => ({ ...oldAuth, userName: "", CSRFToken: "" }));
  };

  const extendSession = () => {
    persistSession(auth);
  };

  const invalidateSession = (res: Response) => {
    const newCSRFToken = res.headers.get("X-CSRF-TOKEN") as string;
    setAuth((oldAuth) => ({
      ...oldAuth,
      userName: "",
      CSRFToken: newCSRFToken,
    }));
  };

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
        let res = await fetch(url + path + (query_params ? query_params : ""), {
          credentials: "include",
          mode: "cors",
          headers:
            config.method === "POST"
              ? { ...config.headers, "X-CSRF-TOKEN": auth.CSRFToken }
              : config.headers,
          ...config,
        });

        if (res.status >= 500) {
          extendSession();
          return reject(new Error("Server error."));
        } else if (res.status == 401) {
          let newCsrfToken = res.headers.get("X-CSRF-TOKEN") as string;
          invalidateSession(res);
          try {
            res = await fetch(url + path + (query_params ? query_params : ""), {
              credentials: "include",
              mode: "cors",
              headers:
                config.method === "POST"
                  ? { ...config.headers, "X-CSRF-TOKEN": newCsrfToken }
                  : config.headers,
              ...config,
            });

            extendSession();
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
          extendSession();

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
        auth,
        logIn,
        logOut,
        extendSession,
        invalidateSession,
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
  auth: authType;
  logIn: (userName: string, res: Response) => void;
  logOut: () => void;
  extendSession: () => void;
  invalidateSession: (res: Response) => void;
  fetcherFactory: <T>(args: fetcherFactoryArgsType) => Promise<T>;
};

export default SessionContext;

function loadCsrfTokenFromMeta(): [string, string] {
  /*let token = document.querySelector("meta[name='_csrf']")?.innerHTML as string;
  let headerName = document.querySelector("meta[name='_csrf_header']")
    ?.innerHTML as string;*/
  let token = "";
  let headerName = "";

  return [token, headerName];
}

function persistSession(auth: authType) {
  let timeToLive = Date.now() + sessionTimeOutInMinutes * 60 * 1000;
  localStorage.setItem("session", JSON.stringify({ auth, timeToLive }));
}
