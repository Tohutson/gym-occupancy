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
  if (!data || data.length === 0) return null;

  // Convert times to Date objects
  const formattedData = data.map((d) => ({
    ...d,
    timeObj: new Date(d.time),
  }));

  // Use the first data point's date to anchor the domain
  const firstDate = new Date(formattedData[0].timeObj);
  const start = new Date(firstDate);
  start.setHours(7, 0, 0, 0); // 7:00 AM same day as data
  const end = new Date(firstDate);
  end.setHours(23, 0, 0, 0); // 11:00 PM same day

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
          tickFormatter={(time) =>
            new Date(time).toLocaleTimeString([], {
              hour: "numeric",
              minute: "2-digit",
            })
          }
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
        <Line
          type="monotone"
          dataKey="count"
          stroke="#8884d8"
          dot={false}
          isAnimationActive={false}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}

export default OccupancyChart;
