interface Props {
  isLoading: boolean;
  onClick?: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
  text: string;
  classes?: string;
  disabled?: boolean;
}

function Button({
  isLoading,
  onClick,
  text,
  classes,
  disabled = isLoading,
}: Props) {
  return (
    <button
      className={
        isLoading
          ? "position-relative" + (classes ? " " + classes : "")
          : classes
      }
      onClick={onClick}
      disabled={disabled}
    >
      {isLoading && (
        <div className="position-absolute top-50 start-50 translate-middle">
          <span className="spinner-border spinner-border-sm me-1"></span>
        </div>
      )}
      <span className={isLoading ? "opacity-0" : ""}>{text}</span>
    </button>
  );
}

export default Button;
