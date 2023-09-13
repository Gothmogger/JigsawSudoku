import { Outlet, NavLink } from "react-router-dom";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import useSession from "../hooks/useAuth";
import { SkeletonTheme } from "react-loading-skeleton";
import { useMutation } from "@tanstack/react-query";
import { useAppDispatch, useAppSelector } from "../reduxStore";
import { logOut, selectUserName } from "../session/sessionSlice";
import Button from "../components/Button";

const LOGOUT_URL = "/api/logout";

function Layout() {
  const userName = useAppSelector(selectUserName);
  const { fetcherFactory } = useSession();
  const dispatch = useAppDispatch();

  const logOutMutation = useMutation({
    mutationFn: () =>
      fetcherFactory({
        path: LOGOUT_URL,
        config: {
          method: "POST",
        },
        responseStatusHandler: () => {
          dispatch(logOut());
        },
      }),
  });

  const handleLogout = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (logOutMutation.isLoading) return;
    logOutMutation.mutate();
  };

  return (
    <>
      <SkeletonTheme baseColor="#212326" highlightColor="#292b2e">
        <nav className="navbar navbar-expand-lg bg-dark navbar-dark">
          <div className="container-fluid">
            <span className="navbar-brand">Jigsaw Sudoku</span>
            <button
              className="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarSupportedContent"
              aria-controls="navbarSupportedContent"
              aria-expanded="false"
              aria-label="Toggle navigation"
            >
              <span className="navbar-toggler-icon"></span>
            </button>
            <div
              className="collapse navbar-collapse"
              id="navbarSupportedContent"
            >
              <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                <li className="nav-item">
                  <NavLink className="nav-link" to="/">
                    Home
                  </NavLink>
                </li>
                {!userName && (
                  <>
                    <li className="nav-item">
                      <NavLink className="nav-link" to="/login">
                        Login
                      </NavLink>
                    </li>
                    <li className="nav-item">
                      <NavLink className="nav-link" to="/register">
                        Register
                      </NavLink>
                    </li>
                  </>
                )}
                {userName && (
                  <span className="navbar-text">Hi, {userName}!</span>
                )}
              </ul>
              {userName && (
                <form className="d-flex" onSubmit={handleLogout}>
                  <Button
                    isLoading={logOutMutation.isLoading}
                    text="Logout"
                    classes="btn btn-outline-success"
                  ></Button>
                </form>
              )}
            </div>
          </div>
        </nav>
        <Outlet />
      </SkeletonTheme>
    </>
  );
}

export default Layout;
