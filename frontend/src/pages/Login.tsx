import { useRef, useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import useSession from "../hooks/useAuth.tsx";
import { useMutation } from "@tanstack/react-query";
import { useAppDispatch } from "../reduxStore.ts";
import { logIn } from "../session/sessionSlice.ts";
import Button from "../components/Button.tsx";

export const LOGIN_URL = "/api/login";
interface logInMutationVariables {
  formData: FormData;
  userName: string;
}

const Login = () => {
  const { fetcherFactory } = useSession();
  const dispatch = useAppDispatch();

  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || "/";

  const userRef = useRef<HTMLInputElement>(null);
  const errRef = useRef<HTMLParagraphElement>(null);

  const [user, setUser] = useState("");
  const [pwd, setPwd] = useState("");

  const logInMutation = useMutation({
    mutationFn: async ({ formData, userName }: logInMutationVariables) => {
      return fetcherFactory({
        path: LOGIN_URL,
        config: {
          method: "POST",
          body: formData,
        },
        responseStatusHandler: (res: Response) => {
          if (res.ok) {
            dispatch(logIn({ userName, res }));
            navigate(from, { replace: true });
          } else if (res.status === 400) {
            errRef.current?.focus();
            throw new Error("Invalid username or password!");
          }
        },
      });
    },
  });

  useEffect(() => {
    userRef.current?.focus();
  }, []);

  useEffect(() => {
    if (logInMutation.isError) logInMutation.reset();
  }, [user, pwd]);

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (logInMutation.isLoading) return;
    let formData = new FormData();
    formData.set("userName", user);
    formData.set("password", pwd);

    logInMutation.mutate({ formData, userName: user });
  };

  return (
    <section className="w-50 mx-auto">
      <div className="row mb-4">
        <p
          ref={errRef}
          className={logInMutation.isError ? "errmsg" : "offscreen"}
          aria-live="assertive"
        >
          {logInMutation.error instanceof Error
            ? logInMutation.error.message
            : "Error"}
        </p>
      </div>
      <h1>Sign In</h1>
      <form onSubmit={handleSubmit}>
        <div className="row">
          <div className="col-sm-6">
            <label htmlFor="username">Username:</label>
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
            />
          </div>
        </div>

        <div className="row">
          <div className="col-sm-6">
            <label htmlFor="password">Password:</label>
          </div>
          <div className="col-sm-6">
            <input
              type="password"
              id="password"
              onChange={(e) => setPwd(e.target.value)}
              value={pwd}
              required
            />
          </div>
        </div>
        <Button
          isLoading={logInMutation.isLoading}
          text="Sign in"
          disabled={!user || !pwd ? true : false}
        ></Button>
      </form>
      <p>
        Need an Account?
        <br />
        <span className="line">
          <Link to="/register">Sign Up</Link>
        </span>
      </p>
    </section>
  );
};

export default Login;
