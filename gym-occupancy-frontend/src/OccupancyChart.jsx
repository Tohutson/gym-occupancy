import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  ResponsiveContainer,
} from "recharts";

function OccupancyChart({ data }) {
  // Convert times to Date objects if not already
  const formattedData = data.map((d) => ({
    ...d,
    timeObj: new Date(d.time),
  }));

  // Define the fixed domain
  const date = new Date(); // today (we'll only use the time part)
  const start = new Date(date);
  start.setHours(7, 0, 0, 0); // 7:00 AM
  const end = new Date(date);
  end.setHours(23, 0, 0, 0); // 11:00 PM

  return (
    <ResponsiveContainer width="100%" height={400}>
      <LineChart
        data={formattedData}
        margin={{ top: 10, right: 30, left: 0, bottom: 0 }}
      >
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis
          dataKey="timeObj"
          domain={[start.getTime(), end.getTime()]}
          type="number"
          scale="time"
          ticks={Array.from({ length: 17 }, (_, i) =>
            new Date(
              start.getFullYear(),
              start.getMonth(),
              start.getDate(),
              7 + i
            ).getTime()
          )}
          tickFormatter={(time) => {
            const d = new Date(time);
            return d.toLocaleTimeString([], {
              hour: "numeric",
              minute: "2-digit",
            });
          }}
        />

        <YAxis />
        <Tooltip
          labelFormatter={(time) =>
            new Date(time).toLocaleTimeString([], {
              hour: "numeric",
              minute: "2-digit",
            })
          }
        />
        <Line type="monotone" dataKey="count" stroke="#8884d8" dot={false} />
      </LineChart>
    </ResponsiveContainer>
  );
}

export default OccupancyChart;
