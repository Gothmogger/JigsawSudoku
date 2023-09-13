import { useContext } from "react";
import SessionContext, { SessionContextType } from "../session/SessionProvider";

const useSession = (): SessionContextType => {
  return useContext(SessionContext);
};

export default useSession;
