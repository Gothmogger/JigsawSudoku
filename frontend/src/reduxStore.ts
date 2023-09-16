import { AnyAction, PreloadedState, Reducer, combineReducers, configureStore } from "@reduxjs/toolkit";
import { REHYDRATE, persistReducer } from "redux-persist";
import thunk from "redux-thunk";
import storage from "redux-persist/lib/storage"; // defaults to localStorage for web and AsyncStorage for react-native
import gameReducer from "./components/game/gameSlice.ts";
import sessionReducer from "./session/sessionSlice.ts";
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux";

const persistConfig = {
    key: "root",
    storage,
  };
  
const combinedReducer = combineReducers({
  game: gameReducer,
  session: sessionReducer,
});
  
const persistedReducer = persistReducer(persistConfig, combinedReducer);

// If combinedReducer is not persisted, but intead gameReducer is and is then used in combinedReducer, storage stays the same after RESET, for some reason.
const rootReducer: Reducer = (state: RootState, action: AnyAction) => {
  if (
    action.type === "session/logOut" ||
    action.type === "session/invalidateSession"
  ) {
    return persistedReducer({ ...state, game: undefined as any }, action); // _persist property gets preserved, !!! IMPORTANT !!!
  } else if (action.type === REHYDRATE && action.payload) { // payload can be undefined when local storage is empty on first start
    if (Date.now() > action.payload.session.timeToLive || (state.session.CSRFToken != action.payload.session.CSRFToken && state.session.CSRFToken)) // The or condition is there because if you run the backend server in dev, it will create session, then you end and run in prod. The session does not get loaded(default settings), new session gets created but the browser request for html is not accessible to javascript. So, the only way to know session changed is by checking the token 
      return persistedReducer({ ...state, game: undefined as any, session: undefined as any }, action);
  }
  return persistedReducer(state, action);
};

export function makeStore(preloadedState?: PreloadedState<RootState>) {
  return configureStore({
    reducer: rootReducer, //{
    //game: combinedReducer,
    //},
    devTools: process.env.NODE_ENV !== "production",
    middleware: [thunk],
    preloadedState
  });
}

// Infer the `RootState` and `AppDispatch` types from the store itself
//export type RootState = ReturnType<typeof store.getState>;
export type RootState = ReturnType<typeof persistedReducer>;
//export type AppDispatch = ReturnType<typeof makeStore>.dispatch;
export type AppStore = ReturnType<typeof makeStore>;
export type AppDispatch = AppStore['dispatch'];
// Use throughout your app instead of plain `useDispatch` and `useSelector`
export const useAppDispatch: () => AppDispatch = useDispatch;
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;