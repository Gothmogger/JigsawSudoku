import { parseISO, formatDistanceToNow } from "date-fns";

interface Props {
  timestamp: string;
}

function TimeAgo({ timestamp }: Props) {
  let timeAgo = "";
  if (timestamp) {
    const date = parseISO(timestamp);
    const timePeriod = formatDistanceToNow(date);
    timeAgo = `${timePeriod} ago`;
  }

  return (
    <span title={timestamp}>
      <i>{timeAgo}</i>
    </span>
  );
}

export default TimeAgo;
