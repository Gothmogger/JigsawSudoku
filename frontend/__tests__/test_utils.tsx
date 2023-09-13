import React, { PropsWithChildren } from "react";
import { render } from "@testing-library/react";
import type { RenderOptions } from "@testing-library/react";
import { Provider } from "react-redux";

import { RootState, makeStore } from "../src/reduxStore.ts";
import { ToolkitStore } from "@reduxjs/toolkit/dist/configureStore";

import { SessionProvider } from "../src/session/SessionProvider";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter } from "react-router-dom";
import { PreloadedState } from "@reduxjs/toolkit";

// This type interface extends the default options for render from RTL, as well
// as allows the user to specify other things such as initialState, store.
interface ExtendedRenderOptions extends Omit<RenderOptions, "queries"> {
  preloadedState?: PreloadedState<RootState>;
  store?: ToolkitStore;
  browser?: boolean;
  rq?: boolean;
  session?: boolean;
}

export function renderWithProviders(
  ui: React.ReactElement,
  {
    preloadedState = {},
    // Automatically create a store instance if no store was passed in
    store = makeStore(preloadedState),
    browser = false,
    rq = false,
    session = false,
    ...renderOptions
  }: ExtendedRenderOptions = {}
) {
  function Wrapper({ children }: PropsWithChildren<{}>): JSX.Element {
    let wrapper = session ? (
      <SessionProvider>{children}</SessionProvider>
    ) : (
      children
    );
    wrapper = <Provider store={store}>{wrapper}</Provider>;
    if (rq) {
      const queryClient = new QueryClient({
        defaultOptions: {
          queries: {
            // ✅ turns retries off
            retry: false,
          },
        },
        logger: {
          log: console.log,
          warn: console.warn,
          // ✅ no more errors on the console for tests
          error: process.env.NODE_ENV === "test" ? () => {} : console.error,
        },
      });

      wrapper = (
        <QueryClientProvider client={queryClient}>
          {wrapper}
        </QueryClientProvider>
      );
    }
    wrapper = browser ? <BrowserRouter>{wrapper}</BrowserRouter> : wrapper;

    return wrapper;
  }

  // Return an object with the store and all of RTL's query functions
  return { store, ...render(ui, { wrapper: Wrapper, ...renderOptions }) };
}
