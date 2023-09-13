import { useRef, useState, useEffect } from "react";
import {
  faCheck,
  faTimes,
  faInfoCircle,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Link, Navigate } from "react-router-dom";
import useSession from "../hooks/useAuth.tsx";
import { useMutation } from "@tanstack/react-query";
import { useAppDispatch } from "../reduxStore.ts";
import { logIn } from "../session/sessionSlice.ts";
import Button from "../components/Button.tsx";

/* standard Regex but I want simpler
const USER_REGEX = /^[A-z][A-z0-9-_]{3,23}$/;
const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,24}$/;
*/
const USER_REGEX = /^[A-Za-z0-9]{3,12}$/;
const PWD_REGEX = /^[A-Za-z0-9]{3,12}$/;
export const REGISTER_URL = "/api/register";
interface registerMutationVariables {
  userName: string;
  password: string;
}

function Register() {
  const dispatch = useAppDispatch();
  const { fetcherFactory } = useSession();

  const userRef = useRef<HTMLInputElement>(null);
  const errRef = useRef<HTMLInputElement>(null);

  const [user, setUser] = useState("");
  const [validName, setValidName] = useState(false);
  const [_userFocus, setUserFocus] = useState(false);

  const [pwd, setPwd] = useState("");
  const [validPwd, setValidPwd] = useState(false);
  const [_pwdFocus, setPwdFocus] = useState(false);

  const [matchPwd, setMatchPwd] = useState("");
  const [validMatch, setValidMatch] = useState(false);
  const [_matchFocus, setMatchFocus] = useState(false);

  const registerMutation = useMutation({
    mutationFn: async ({ userName, password }: registerMutationVariables) => {
      return fetcherFactory({
        path: REGISTER_URL,
        config: {
          method: "POST",
          body: JSON.stringify({
            userName: userName,
            password: password,
          }),
          headers: {
            "Content-Type": "application/json",
          },
        },
        responseStatusHandler: (res: Response) => {
          if (res.ok) {
            dispatch(logIn({ userName: user, res }));

            <Navigate to="/" replace={true} />;
          } else if (res.status === 400) {
            errRef.current?.focus();
            throw new Error("The name is already in use.");
          }
        },
      });
    },
  });

  useEffect(() => {
    userRef.current?.focus();
  }, []);

  useEffect(() => {
    setValidName(USER_REGEX.test(user));
  }, [user]);

  useEffect(() => {
    setValidPwd(PWD_REGEX.test(pwd));
    setValidMatch(pwd === matchPwd);
  }, [pwd, matchPwd]);

  useEffect(() => {
    if (registerMutation.isError) registerMutation.reset();
  }, [user, pwd, matchPwd]);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (registerMutation.isLoading) return;
    // if button enabled with JS hack
    const v1 = USER_REGEX.test(user);
    const v2 = PWD_REGEX.test(pwd);
    if (!v1 || !v2) {
      setValidPwd(false);
      setValidMatch(false);
      return;
    }

    registerMutation.mutate({ userName: user, password: pwd });
  };

  return (
    <section className="w-50 mx-auto">
      <div className="row mb-4">
        <p
          ref={errRef}
          className={registerMutation.isError ? "errmsg" : "offscreen"}
          aria-live="assertive"
        >
          {registerMutation.error instanceof Error
            ? registerMutation.error.message
            : "Error"}
        </p>
      </div>
      <h1>Register</h1>
      <form onSubmit={handleSubmit}>
        <div className="row">
          <div className="col-sm-6">
            <label htmlFor="username">
              Username:
              <FontAwesomeIcon
                icon={faCheck}
                className={validName ? "valid" : "hide"}
              />
              <FontAwesomeIcon
                icon={faTimes}
                className={validName || !user ? "hide" : "invalid"}
              />
            </label>
          </div>
          <div className="col-sm-6">
            <input
              type="text"
              id="username"
              ref={userRef}
              autoComplete="off"
              onChange={(e) => setUser(e.target.value)}
              value={user}
              required
              aria-invalid={validName ? "false" : "true"}
              aria-describedby="uidnote"
              onFocus={() => setUserFocus(true)}
              onBlur={() => setUserFocus(false)}
            />
          </div>
        </div>
        <p
          id="uidnote"
          className={user && !validName ? "instructions" : "offscreen"}
        >
          <FontAwesomeIcon icon={faInfoCircle} />
          Must be 3 to 16 characters long.
        </p>

        <div className="row">
          <div className="col-sm-6">
            <label htmlFor="password">
              Password:
              <FontAwesomeIcon
                icon={faCheck}
                className={validPwd ? "valid" : "hide"}
              />
              <FontAwesomeIcon
                icon={faTimes}
                className={validPwd || !pwd ? "hide" : "invalid"}
              />
            </label>
          </div>
          <div className="col-sm-6">
            <input
              type="password"
              id="password"
              onChange={(e) => setPwd(e.target.value)}
              value={pwd}
              required
              aria-invalid={validPwd ? "false" : "true"}
              aria-describedby="pwdnote"
              onFocus={() => setPwdFocus(true)}
              onBlur={() => setPwdFocus(false)}
            />
          </div>
        </div>
        <p
          id="pwdnote"
          className={pwd && !validPwd ? "instructions" : "offscreen"}
        >
          <FontAwesomeIcon icon={faInfoCircle} />3 to 16 characters.
          {/*
              Allowed special characters:{" "}
              <span aria-label="exclamation mark">!</span>{" "}
              <span aria-label="at symbol">@</span>{" "}
              <span aria-label="hashtag">#</span>{" "}
              <span aria-label="dollar sign">$</span>{" "}
      <span aria-label="percent">%</span>*/}
        </p>
        <div className="row">
          <div className="col-sm-6">
            <label htmlFor="confirm_pwd">
              Confirm Password:
              <FontAwesomeIcon
                icon={faCheck}
                className={validMatch && matchPwd ? "valid" : "hide"}
              />
              <FontAwesomeIcon
                icon={faTimes}
                className={validMatch || !matchPwd ? "hide" : "invalid"}
              />
            </label>
          </div>
          <div className="col-sm-6">
            <input
              type="password"
              id="confirm_pwd"
              onChange={(e) => setMatchPwd(e.target.value)}
              value={matchPwd}
              required
              aria-invalid={validMatch ? "false" : "true"}
              aria-describedby="confirmnote"
              onFocus={() => setMatchFocus(true)}
              onBlur={() => setMatchFocus(false)}
            />
          </div>
        </div>
        <p
          id="confirmnote"
          className={matchPwd && !validMatch ? "instructions" : "offscreen"}
        >
          <FontAwesomeIcon icon={faInfoCircle} />
          Must match the first password input field.
        </p>
        <Button
          isLoading={registerMutation.isLoading}
          text="Sign up"
          disabled={!validName || !validPwd || !validMatch ? true : false}
        ></Button>
      </form>
      <p>
        Already registered?
        <br />
        <span className="line">
          <Link to="/login">Sign In</Link>
        </span>
      </p>
    </section>
  );
}

export default Register;
