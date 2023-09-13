import { useLocation, Navigate, Outlet } from "react-router-dom";
import { useAppSelector } from "../reduxStore";
import { selectUserName } from "../session/sessionSlice";

const RequireAnonym = () => {
  const userName = useAppSelector(selectUserName);
  const location = useLocation();

  return userName ? (
    <Navigate to="/" state={{ from: location }} replace />
  ) : (
    <Outlet />
  );
};

export default RequireAnonym;
