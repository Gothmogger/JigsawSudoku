import { useLocation, Navigate, Outlet } from "react-router-dom";
import { useAppSelector } from "../reduxStore";
import { selectUserName } from "../session/sessionSlice";

const RequireAuth = () => {
  const userName = useAppSelector(selectUserName);
  const location = useLocation();

  return userName ? (
    <Outlet />
  ) : (
    <Navigate to="/login" state={{ from: location }} replace />
  );
};

/*
const RequireAuth = ({ allowedRoles }) => {
    const { auth } = useAuth();
    const location = useLocation();

    return (
        auth?.roles?.find(role => allowedRoles?.includes(role))
            ? <Outlet />
            : auth?.user
                ? <Navigate to="/unauthorized" state={{ from: location }} replace />
                : <Navigate to="/login" state={{ from: location }} replace />
    );
}*/

export default RequireAuth;
