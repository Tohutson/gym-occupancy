import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import OccupancyChart from "./OccupancyChart";

jest.mock("recharts", () => ({
  Area: () => <div data-testid="area" />,
  CartesianGrid: () => null,
  ComposedChart: ({ children }) => <div data-testid="chart">{children}</div>,
  Line: ({ dataKey, dot }) => (
    <div data-testid={`line-${dataKey}`} data-dot-radius={dot?.r ?? "none"} />
  ),
  ResponsiveContainer: ({ children }) => <div data-testid="responsive-container">{children}</div>,
  Tooltip: () => null,
  XAxis: () => null,
  YAxis: () => null,
}));

function dashboard(measurements) {
  return {
    range: "HOURS_24",
    timezone: "America/New_York",
    facility: { capacity: 200 },
    measurements,
    typicalDay: [],
  };
}

test("renders one stored measurement as a visible point", () => {
  render(<OccupancyChart dashboard={dashboard([
    { time: "2026-08-24T10:00:00Z", count: 12 },
  ])} />);

  expect(screen.getByTestId("responsive-container")).toBeInTheDocument();
  expect(screen.getByTestId("line-count")).toHaveAttribute("data-dot-radius", "3");
});

test("renders the measured series for multiple stored measurements", () => {
  render(<OccupancyChart dashboard={dashboard([
    { time: "2026-08-24T10:00:00Z", count: 12 },
    { time: "2026-08-24T10:05:00Z", count: 18 },
  ])} />);

  expect(screen.getByTestId("chart")).toBeInTheDocument();
  expect(screen.getByTestId("line-count")).toBeInTheDocument();
});

test("renders an empty state when no valid measurements exist", () => {
  render(<OccupancyChart dashboard={dashboard([])} />);

  expect(screen.getByText("No measurements exist in this time range.")).toBeInTheDocument();
  expect(screen.queryByTestId("chart")).not.toBeInTheDocument();
});
