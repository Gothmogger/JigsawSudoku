import { PayloadAction, createSlice } from "@reduxjs/toolkit";
import { RootState } from "./../reduxStore";

const sessionTimeOutInMinutes = 15;
export const CSRFHeaderName = "X-CSRF-TOKEN";

type StateType = {userName: string, CSRFToken: string, CSRFHeaderName: string, timeToLive: number}
export const initialState = () : StateType => {
    let tokenSelector = document.querySelector<HTMLMetaElement>("meta[name='_csrf']");
    let headerNameSelector = document.querySelector<HTMLMetaElement>("meta[name='_csrf_header']");
    let token = tokenSelector?.content ? tokenSelector.content : "";
    let headerName = headerNameSelector?.content ? headerNameSelector.content : CSRFHeaderName;

    return {
        userName: "",
        CSRFToken: token,
        CSRFHeaderName: headerName,
        timeToLive: Date.now() + sessionTimeOutInMinutes * 60 * 1000
    };
};

const sessionSlice = createSlice({
    name: 'session',
    initialState,
    reducers: {
        logIn(state, action: PayloadAction<{userName: string, res: Response}>) {
            const { userName, res } = action.payload
            const newCSRFToken = res.headers.get(state.CSRFHeaderName) as string;
            state.userName = userName;
            state.CSRFToken = newCSRFToken;
            state.timeToLive = Date.now() + sessionTimeOutInMinutes * 60 * 1000;
        },
        logOut(state) {
            state.userName = "";
            state.CSRFToken = "";
        },
        extendSession(state, action: PayloadAction<Response | undefined>) {
            state.timeToLive = Date.now() + sessionTimeOutInMinutes * 60 * 1000;
            
            if (action.payload) {
                state.CSRFToken = action.payload.headers.get(state.CSRFHeaderName) as string;
            }
        },
        invalidateSession(state, action: PayloadAction<Response | undefined>) {
            state.userName = "";
            state.timeToLive = Date.now() + sessionTimeOutInMinutes * 60 * 1000;
            if (action.payload) {
                const newCSRFToken = action.payload.headers.get(state.CSRFHeaderName) as string;
                state.CSRFToken = newCSRFToken;
            }
        },
    },/*
    extraReducers: (builder) => {
        builder.addCase(REHYDRATE, (state, action : any) => {
          if (Date.now() > action.payload.session.timeToLive) 
            return initialState();
          else
            return state;
        }
        );
    }*/
})

export const selectUserName = (state: RootState) => state.session.userName;
export const selectCSRFToken = (state: RootState) => state.session.CSRFToken;
export const selectCSRFHeaderName = (state: RootState) => state.session.CSRFHeaderName;
export const selectTimeToLive = (state: RootState) => state.session.timeToLive;

export const { logIn, logOut, extendSession, invalidateSession } = sessionSlice.actions

export default sessionSlice.reducer