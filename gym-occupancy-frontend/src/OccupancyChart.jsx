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

function formatTime(value, timezone) {
  return new Intl.DateTimeFormat("en-US", {
    timeZone: timezone,
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

function formatTick(value, timezone, range) {
  return new Intl.DateTimeFormat("en-US", {
    timeZone: timezone,
    weekday: range === "DAYS_7" ? "short" : undefined,
    hour: range === "TODAY" ? "numeric" : undefined,
    month: range === "DAYS_7" ? "numeric" : undefined,
    day: range === "DAYS_7" ? "numeric" : undefined,
  }).format(new Date(value));
}

function ChartTooltip({ active, payload, label, timezone }) {
  if (!active || !payload?.length) return null;
  const values = Object.fromEntries(payload.map((item) => [item.dataKey, item.value]));
  return (
    <div className="chart-tooltip">
      <strong>{formatTime(label, timezone)}</strong>
      {values.count != null && <span>Measured {Math.round(values.count)}</span>}
      {values.expected != null && <span>Typical {Math.round(values.expected)}</span>}
    </div>
  );
}

export default function OccupancyChart({ dashboard }) {
  const data = buildChartData(dashboard);
  const hasMeasurements = dashboard.measurements.length > 0;
  const observedMaximum = Math.max(
    ...data.map((point) => Math.max(point.count || 0, point.expectedRange?.[1] || 0))
  );
  const chartMaximum = Math.min(
    dashboard.facility.capacity || Number.POSITIVE_INFINITY,
    Math.max(50, Math.ceil((observedMaximum * 1.15) / 25) * 25)
  );

  if (!hasMeasurements) {
    return <div className="chart-empty">No measurements exist in this time range.</div>;
  }

  return (
    <div className="chart-wrap" role="img" aria-label="Occupancy over time">
      <ResponsiveContainer width="100%" height="100%">
        <ComposedChart data={data} margin={{ top: 12, right: 12, left: -18, bottom: 0 }}>
          <CartesianGrid stroke="#e7e5e4" vertical={false} />
          <XAxis
            dataKey="timestamp"
            type="number"
            scale="time"
            domain={["dataMin", "dataMax"]}
            minTickGap={48}
            tickLine={false}
            axisLine={false}
            tickFormatter={(value) => formatTick(value, dashboard.timezone, dashboard.range)}
          />
          <YAxis
            domain={[0, chartMaximum]}
            allowDecimals={false}
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
            stroke="#0f766e"
            strokeWidth={2.5}
            dot={false}
            activeDot={{ r: 4, fill: "#0f766e", stroke: "#fff", strokeWidth: 2 }}
            isAnimationActive={false}
            connectNulls={false}
          />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
