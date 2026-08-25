import {
  Area,
  CartesianGrid,
  ComposedChart,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { buildChartData } from "./dashboardUtils";

function formatTick(value, timezone, range) {
  const dateFields = range === "DAYS_7"
    ? { month: "numeric", day: "numeric" }
    : range === "HOURS_24"
      ? { weekday: "short", hour: "numeric" }
      : { hour: "numeric" };
  return new Intl.DateTimeFormat("en-US", {
    timeZone: timezone,
    ...dateFields,
  }).format(new Date(value));
}

function formatTimestamp(value, timezone) {
  return new Intl.DateTimeFormat("en-US", {
    timeZone: timezone,
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

function ChartTooltip({ active, payload, label, timezone }) {
  if (!active || !payload?.length) return null;
  const values = Object.fromEntries(payload.map((item) => [item.dataKey, item.value]));
  return (
    <div className="chart-tooltip">
      <strong>{formatTimestamp(label, timezone)}</strong>
      {values.count != null && <span>Measured {Math.round(values.count)}</span>}
      {values.expected != null && <span>Typical {Math.round(values.expected)}</span>}
    </div>
  );
}

export default function OccupancyChart({ dashboard }) {
  const data = buildChartData(dashboard);
  const measurements = data.filter((point) => Number.isFinite(point.count));
  const hasMeasurements = measurements.length > 0;
  const observedMaximum = Math.max(
    0,
    ...data.map((point) => Math.max(point.count ?? 0, point.expectedRange?.[1] ?? 0))
  );
  const chartMaximum = Math.min(
    dashboard.facility.capacity || Number.POSITIVE_INFINITY,
    Math.max(50, Math.ceil((observedMaximum * 1.15) / 25) * 25)
  );
  const validTimestamps = data.map((point) => point.timestamp).filter(Number.isFinite);
  const dataMinimum = Math.min(...validTimestamps);
  const dataMaximum = Math.max(...validTimestamps);
  const xDomain = dataMinimum === dataMaximum
    ? [dataMinimum - 15 * 60 * 1000, dataMaximum + 15 * 60 * 1000]
    : ["dataMin", "dataMax"];

  if (!hasMeasurements) {
    return <div className="chart-empty">No measurements exist in this time range.</div>;
  }

  return (
    <div className="chart-wrap" role="img" aria-label="Occupancy over time">
      <ResponsiveContainer width="100%" height="100%">
        <ComposedChart data={data} margin={{ top: 12, right: 10, left: 0, bottom: 4 }}>
          <CartesianGrid stroke="#e7e5e4" vertical={false} />
          <XAxis
            dataKey="timestamp"
            type="number"
            scale="time"
            domain={xDomain}
            minTickGap={36}
            tickLine={false}
            axisLine={false}
            tickFormatter={(value) => formatTick(value, dashboard.timezone, dashboard.range)}
          />
          <YAxis
            domain={[0, chartMaximum]}
            allowDecimals={false}
            width={38}
            tickLine={false}
            axisLine={false}
          />
          <Tooltip content={<ChartTooltip timezone={dashboard.timezone} />} />
          {dashboard.range === "TODAY" && (
            <Area
              dataKey="expectedRange"
              stroke="none"
              fill="#d6e7e4"
              fillOpacity={0.7}
              isAnimationActive={false}
              connectNulls={false}
            />
          )}
          {dashboard.range === "TODAY" && (
            <Line
              dataKey="expected"
              name="Typical"
              stroke="#6b8e88"
              strokeWidth={1.5}
              strokeDasharray="5 5"
              dot={false}
              isAnimationActive={false}
              connectNulls={false}
            />
          )}
          <Line
            dataKey="count"
            name="Measured"
            type="linear"
            stroke="#0f766e"
            strokeWidth={2.5}
            dot={{ r: 3, fill: "#0f766e", stroke: "#fff", strokeWidth: 1.5 }}
            activeDot={{ r: 5, fill: "#0f766e", stroke: "#fff", strokeWidth: 2 }}
            isAnimationActive={false}
            connectNulls={false}
          />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
