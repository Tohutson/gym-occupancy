import { buildChartData, comparisonTone, formatRelativeAge } from "./dashboardUtils";

test("inserts a null point when measurements have a long gap", () => {
  const data = buildChartData({
    range: "HOURS_24",
    measurements: [
      { time: "2026-08-24T10:00:00Z", count: 10 },
      { time: "2026-08-24T11:00:00Z", count: 20 },
    ],
    typicalDay: [],
  });

  expect(data).toHaveLength(3);
  expect(data[1].count).toBeNull();
});

test("interpolates the typical curve at actual measurement times", () => {
  const data = buildChartData({
    range: "TODAY",
    measurements: [{ time: "2026-08-24T10:15:00Z", count: 40 }],
    typicalDay: [
      { time: "2026-08-24T10:00:00Z", average: 20, lowerQuartile: 15, upperQuartile: 25 },
      { time: "2026-08-24T10:30:00Z", average: 30, lowerQuartile: 25, upperQuartile: 35 },
    ],
  });

  expect(data[0].expected).toBe(25);
  expect(data[0].expectedRange).toEqual([20, 30]);
});

test("formats freshness and comparison tone without false precision", () => {
  expect(formatRelativeAge(125)).toBe("2 min ago");
  expect(comparisonTone("MUCH_QUIETER")).toBe("quiet");
  expect(comparisonTone("INSUFFICIENT_DATA")).toBe("neutral");
});
