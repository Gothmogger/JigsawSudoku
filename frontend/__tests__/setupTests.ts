/*
import "@testing-library/jest-dom/extend-expect";
import "@testing-library/jest-dom";


import { expect, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import matchers from '@testing-library/jest-dom/matchers';

expect.extend(matchers);

afterEach(() => {
  cleanup();
});

*/
// import { vi } from "vitest";
import "@testing-library/jest-dom";
import { configure } from "@testing-library/react";

configure({
  getElementError: (message, _container) => {
    const error = new Error(message + "");
    error.name = 'TestingLibraryElementError';
    return error;
  },
});