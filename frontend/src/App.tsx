import Game from "./components/game/Game";
import NotFound from "./pages/NotFound";
import "./App.css";
import "bootstrap/dist/css/bootstrap.css";
import { Route, Routes } from "react-router-dom";
import Layout from "./pages/Layout";
import Login from "./pages/Login";
import Register from "./pages/Register";
import RequireAnonym from "./components/RequireAnonym";
import "react-loading-skeleton/dist/skeleton.css";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Game />} />
        <Route path="*" element={<NotFound />} />
        <Route element={<RequireAnonym />}>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Route>
      </Route>
    </Routes>
  );
}

export default App;
