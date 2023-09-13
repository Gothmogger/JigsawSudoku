import { fireEvent, render, waitFor } from "@testing-library/react";
import Button from "../src/components/Button";

describe("Button component", () => {
  test("Render button with text and class", () => {
    const { getByRole, getByText } = render(
      <Button
        isLoading={false}
        onClick={() => {}}
        classes="btn"
        disabled={false}
        text={"Click me!"}
      />
    );

    const button = getByRole("button");
    const textSpan = getByText("Click me!");
    expect(button).toBeInTheDocument();
    expect(button).toHaveClass("btn");
    expect(textSpan).toBeInTheDocument();
    expect(button).toContainElement(textSpan);
  });

  test("Should show spinner when loading", () => {
    const { getByRole, rerender } = render(
      <Button
        isLoading={true}
        onClick={() => {}}
        classes=""
        disabled={false}
        text={""}
      />
    );

    const button = getByRole("button");
    const spinnerSpan = button.querySelector("span");
    expect(button).toBeInTheDocument();
    expect(spinnerSpan).toBeInTheDocument();

    rerender(
      <Button
        isLoading={false}
        onClick={() => {}}
        classes=""
        disabled={false}
        text={""}
      />
    );
    expect(spinnerSpan).not.toBeInTheDocument();
  });

  test("Should call onClick when clicked", () => {
    let onClick = vi.fn();
    const { getByRole } = render(
      <Button
        isLoading={false}
        onClick={onClick}
        classes=""
        disabled={false}
        text={""}
      />
    );

    const button = getByRole("button");
    fireEvent.click(button);

    waitFor(() => expect(onClick).toHaveBeenCalledTimes(1));
  });

  test("Should be disabled when disabled", () => {
    const { getByRole } = render(
      <Button
        isLoading={false}
        onClick={() => {}}
        classes=""
        disabled={true}
        text={""}
      />
    );

    const button = getByRole("button");
    expect(button).toBeDisabled();
  });

  test("Should be disabled when loading and disabled is not set", () => {
    const { getByRole } = render(
      <Button isLoading={true} onClick={() => {}} classes="" text={""} />
    );

    const button = getByRole("button");
    expect(button).toBeDisabled();
  });
});
